/* This actor implements a Network Bus.

@Copyright (c) 2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */

package ptolemy.actor.lib.qm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.qm.QuantityManagerListener.EventType;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.lib.Server;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException; 
import ptolemy.kernel.util.Workspace;

/** This actor is an {@link QuantityManager} that, when its
 *  {@link #sendToken(Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule. Specifically, if the actor is
 *  not currently servicing a previous token, then it delivers
 *  the token with a delay given by the <i>serviceTime</i> parameter.
 *  If the actor is currently servicing a previous token, then it waits
 *  until it has finished servicing that token (and any other pending
 *  tokens), and then delays an additional amount given by <i>serviceTime</i>.
 *  This is similar to the {@link Server} actor.
 *  Tokens are processed in FIFO order.
 *  <p>
 *  This actor will be used on any communication where the receiving
 *  port has a parameter named "QuantityManager" that refers by name
 *  to the instance of this actor.
 *  <p>
 *  FIXME: This receiver behaves differently for Continuous and DE. Allowing
 *  the use of this actor across hierarchies might therefore be problematic.
 *  @author Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class Bus extends ColoredQuantityManager implements
        MonitoredQuantityManager {

    /** Construct a Bus with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Bus(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _tokens = new FIFOQueue();
        _receiversAndTokensToSendTo = new HashMap();

        serviceTime = new Parameter(this, "serviceTime");
        serviceTime.setExpression("0.1");
        serviceTime.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     */
    public IntermediateReceiver getReceiver(Receiver receiver) {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        return intermediateReceiver;
    }

    /** If the attribute is <i>serviceTime</i>, then ensure that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serviceTime) {
            double value = ((DoubleToken) serviceTime.getToken()).doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero serviceTime: " + value);
            }
            _serviceTimeValue = value;
        }
        super.attributeChanged(attribute);
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new Bus.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Bus newObject = (Bus) super.clone(workspace);
        newObject._nextReceiver = null;
        newObject._nextTimeFree = null;
        newObject._receiversAndTokensToSendTo = new HashMap();
        newObject._serviceTimeValue = 0.1;
        newObject._tokens = new FIFOQueue();
        return newObject;
    }

    /** Initialize the actor.
     *  @throws IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _receiversAndTokensToSendTo.clear();
        _tokens.clear();
        _nextTimeFree = null;
    }

    /** Send first token in the queue to the target receiver.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        // In a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null.
        if (_nextTimeFree != null && _tokens.size() > 0
                && currentTime.compareTo(_nextTimeFree) == 0) {
            Object[] output = (Object[]) _tokens.get(0);
            Receiver receiver = (Receiver) output[0];
            Token token = (Token) output[1];
            //receiver.put(token);
            
            // FIXME: See the FIXME's below. The commented
            // out code below is an attempt to address it, but a
            // questionable one.
            //
            // What comes next is complicated. Hold onto your hat.
            // The scope of a quantity manager includes everything
            // below its container (actually, its global, if you
            // use a fully qualified name), even across opaque composite
            // boundaries. This is because shared resources are
            // shared, and could be used anywhere in a model.
            // There are major complications when a Bus is being
            // used to send data to an output port from the inside,
            // and when a Bus is being used to send data to an
            // input port from the outside of an opaque composite
            // actor. The following code deals with these two cases.
            //
            // If the receiver is contained by an output port,
            // then sendToken() must have been called as a result
            // of depositing a token into its _inside_ receiver.
            // We can now deliver the token to director's receiver,
            // but there is no assurance that the director's
            // container will fire to handle that token.
            // We handle this by requesting a firing of the composite.

            if (!(receiver instanceof IntermediateReceiver)) {
                Actor container = (Actor) receiver.getContainer()
                        .getContainer();
                if (receiver.getContainer().isOutput()) {
                    receiver.put(token);
                    // The fire that results from the following fireAt()
                    // call, at a minimum, will result in a
                    // transfer outputs to the outside of the composite.
                    Actor containerOfComposite = (Actor) container
                            .getContainer();
                    if (containerOfComposite != null) {
                        containerOfComposite.getDirector().fireAt(container,
                                currentTime);
                    }
                } else {
                    // If the recipient is an input, then 
                    if (receiver.getContainer().isInput()) {
                        // the container must have the correct model time before putting the token
                        ((Actor) container.getContainer()).getDirector()
                                .fireAt(container, currentTime);
                        receiver.put(token);
                        // making sure the input is transferred inside.
                        ((Actor) container.getContainer()).getDirector()
                                .fireAt(container, currentTime);
                    }
                }
            } else {
                receiver.put(token);
            }

            if (_debugging) {
                _debug("At time " + currentTime + ", completing send to "
                        + receiver.getContainer().getFullName() + ": " + token);
            }
        }
    }

    /** If there are still tokens in the queue and a token has been produced in the fire, 
     *  schedule a refiring.
     */
    public boolean postfire() throws IllegalActionException {
        // This method contains two places where refirings can be
        // scheduled. We only want to schedule a refiring once. 
        Time currentTime = getDirector().getModelTime();

        // If a token was actually sent to a delegated receiver
        // by the fire() method, then remove that token from
        // the queue and, if there are still tokens in the queue,
        // request another firing at the time those tokens should
        // be delivered to the delegated receiver.
        if (_nextTimeFree != null && _tokens.size() > 0
                && currentTime.compareTo(_nextTimeFree) == 0) {
            // Discard the token that was sent to the output in fire().
            _tokens.take();
            sendTaskExecutionEvent((Actor) null, 0, _tokens.size(), EventType.SENT);
            //            if (_tokens.size() > 0) { 
            //                _scheduleRefire();
            //                refiringScheduled = true;
            //                // FIXME:
            //                // Not only does this bus need to be fired
            //                // at the _nextTimeFree, but so does the destination
            //                // actor. In particular, that actor may be under
            //                // the control of a _different director_ than the
            //                // bus, and the order in which that actor is fired
            //                // vs. this Bus is important. How to control this?
            //                // Maybe global scope of a QuantityManager is not
            //                // a good idea, but we really do want to be able
            //                // to share a QuantityManager across modes of a
            //                // modal model. How to fix???
            //            } else {
            //                refiringScheduled = false;
            //            }
        }
        // If sendToken() was called in the current iteration,
        // then append the token to the queue. If this is the
        // only token on the queue, then request a firing at
        // the time that token should be delivered to the
        // delegated receiver.
        if ((getDirector() instanceof FixedPointDirector)
                && _receiversAndTokensToSendTo != null) {
            for (Receiver receiver : _receiversAndTokensToSendTo.keySet()) {
                Token token = _receiversAndTokensToSendTo.get(receiver);
                if (token != null) {
                    _tokens.put(new Object[] { receiver, token });
                }
            }
            _receiversAndTokensToSendTo.clear();
        }
        // if there was no token in the queue, schedule a refiring.
        // FIXME: wrong, more than one token can be received at a time instant! if (_tokens.size() == 1) { 
        if (_tokens.size() > 0
                && (_nextTimeFree == null || currentTime
                        .compareTo(_nextTimeFree) >= 0)) {
            _scheduleRefire();
            // FIXME:
            // Not only does this bus need to be fired
            // at the _nextTimeFree, but so does the destination
            // actor. In particular, that actor may be under
            // the control of a _different director_ than the
            // bus, and the order in which that actor is fired
            // vs. this Bus is important. How to control this?
            // Maybe global scope of a QuantityManager is not
            // a good idea, but we really do want to be able
            // to share a QuantityManager across modes of a
            // modal model. How to fix???
        }

        return super.postfire();
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled. 
     *  @param receiver The receiver to send to.
     *  @param token The token to send.
     *  @throws IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        // FIXME: Why is the following needed?
        if (_nextTimeFree == null || _tokens.size() == 0
                || currentTime.compareTo(_nextTimeFree) != 0
                || receiver != _nextReceiver) {
            // At the current time, there is no token to send.
            // At least in the Continuous domain, we need to make sure
            // the delegated receiver knows this so that it becomes
            // known and absent.

            if (getDirector() instanceof FixedPointDirector) {
                receiver.put(null);
            }
        }

        // If previously in the current iteration we have
        // sent a token, then we require the token to have the
        // same value. Thus, this Bus can be used only in domains
        // that either call fire() at most once per iteration,
        // or domains that have a fixed-point semantics.
        Token tokenToSend = _receiversAndTokensToSendTo.get(receiver);
        if (tokenToSend != null) {
            if (!tokenToSend.equals(token)) {
                throw new IllegalActionException(this, receiver.getContainer(),
                        "Previously initiated a transmission with value "
                                + tokenToSend
                                + ", but now trying to send value " + token
                                + " in the same iteration.");
            }
        } else {

            // In the Continuous domain, this actor gets fired if tokens are available
            // or not. In the DE domain we need to schedule a refiring. 
            if (getDirector() instanceof FixedPointDirector) {
                _receiversAndTokensToSendTo.put(receiver, token);
            } else {
                _tokens.put(new Object[] { receiver, token });
                sendTaskExecutionEvent((Actor) source.getContainer()
                        .getContainer(), 0, _tokens.size(), EventType.RECEIVED);
                if (_tokens.size() == 1) { // no refiring has been scheduled
                    _scheduleRefire();
                }
            }
        }

        // If the token is null, then this means there is not actually
        // something to send. Do not take up bus resources for this.
        if (token == null) {
            return;
        }
        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }

    /**
     * Reset the quantity manager and clear the tokens.
     */
    public void reset() {
        _tokens.clear();
    }
    
    /** Add a quantity manager monitor to the list of listeners.
     *  @param monitor The quantity manager monitor.
     */
    public void registerListener(QuantityManagerMonitor monitor) {
        if (_listeners == null) {
            _listeners = new ArrayList<QuantityManagerListener>();
        }
        _listeners.add(monitor);
    }

    /** Notify the monitor that an event happened.
     *  @param source The source actor that caused the event in the 
     *      quantity manager. 
     *  @param messageId The ID of the message that caused the event in 
     *      the quantity manager.
     *  @param messageCnt The amount of messages currently being processed
     *      by the quantity manager.
     *  @param time The time when the event happened.
     *  @param event The type of the event. e.g. message received, message sent, ... 
     */
    public void sendTaskExecutionEvent(Actor source, int messageId,
            int messageCnt, EventType eventType) {
        if (_listeners != null) {
            Iterator listeners = _listeners.iterator(); 
            while (listeners.hasNext()) {
                ((QuantityManagerListener) listeners.next()).event(this,
                        source, messageId, messageCnt, getDirector().getModelTime()
                                .getDoubleValue(), eventType);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //                          public variables                     //

    /** The service time. This is a double with default 0.1.
     *  It is required to be positive.
     */
    public Parameter serviceTime;
    
    ///////////////////////////////////////////////////////////////////
    //                          protected methods                    //
    
    /** Schedule a refiring of the actor.
     *  @exception IllegalActionException Thrown if the actor cannot be rescheduled
     */
    protected void _scheduleRefire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        _nextTimeFree = currentTime.add(_serviceTimeValue);
        _nextReceiver = (Receiver) ((Object[]) _tokens.get(0))[0];
        _fireAt(_nextTimeFree);
    }

    ///////////////////////////////////////////////////////////////////
    //                           private variables                   //

    /** Next receiver to which the next token to be sent is destined. */
    private Receiver _nextReceiver;

    /** Next time a token is sent and the next token can be processed. */
    private Time _nextTimeFree;

    /** Map of receivers and tokens to which the token provided via sendToken() should
     *  be sent to.
     */
    private HashMap<Receiver, Token> _receiversAndTokensToSendTo;

    /** Delay imposed on every token. */
    private double _serviceTimeValue;

    /** Tokens stored for processing. */
    private FIFOQueue _tokens;

    /** Listeners registered to receive events from this object. */
    private ArrayList<QuantityManagerListener> _listeners;

}
