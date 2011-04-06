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
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.domains.de.lib.Server;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This actor is an {@link QuantityManager} that, when its
 *  {@link #sendToken(Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule. 
 *  
 *  <p>This quantity manager implements a simple switch. It has a parameter
 *  specifying the number of ports. On each port, an actor is connected.
 *  Note that these ports are not represented as ptolemy actor ports. 
 *  This actor can send tokens to the switch and receive tokens from the
 *  switch. The mapping of ports to actors is done via parameters of this
 *  quantity manager.
 *  
 *  <p>Internally, this switch has a buffer for every input, a buffer
 *  for the switch fabric and a buffer for every output. The delays
 *  introduced by the buffers are configured via parameters. Tokens are
 *  processed simultaneously on the buffers.
 *  
 *  <p> This switch implements a very basic switch fabric consisting
 *  of a FIFO queue. 
 *  
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class BasicSwitch extends ColoredQuantityManager {

    /** Construct a Bus with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.f
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public BasicSwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _inputTokens = new HashMap();
        _outputTokens = new HashMap();
        _switchFabricQueue = new TreeSet();
        _receivers = new Hashtable<Receiver, IntermediateReceiver>();
        _actorPorts = new HashMap<Actor, Integer>();

        inputBufferDelay = new Parameter(this, "inputBufferDelay");
        inputBufferDelay.setExpression("0.1");
        inputBufferDelay.setTypeEquals(BaseType.DOUBLE);
        _inputBufferDelay = 0.1;

        outputBufferDelay = new Parameter(this, "outputBufferDelay");
        outputBufferDelay.setExpression("0.1");
        outputBufferDelay.setTypeEquals(BaseType.DOUBLE);
        _outputBufferDelay = 0.1;

        switchFabricDelay = new Parameter(this, "switchFabricDelay");
        switchFabricDelay.setExpression("0.1");
        switchFabricDelay.setTypeEquals(BaseType.DOUBLE);
        _switchFabricDelay = 0.1;

        numberOfPorts = new Parameter(this, "numberOfPorts");
        numberOfPorts.setExpression("4");
        numberOfPorts.setTypeEquals(BaseType.INT);
        _numberOfPorts = 4;
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
    
    /** Make sure that this quantity manager is only used in the DE domain. 
     *  FIXME: this actor should be used in other domains later as well. 
     *  @param container The container of this actor.
     *  @exception IllegalActionException Thrown by the super class or if the 
     *  director of this actor is not a DEDirector.
     *  @exception NameDuplicationException Thrown by the super class.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (getDirector() != null && !(getDirector() instanceof DEDirector)) {
            throw new IllegalActionException(this,
                    "This quantity manager is currently only supported in the DE domain.");
        }
    }

    /** If the attribute is <i>serviceTime</i>, then ensure that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == inputBufferDelay) {
            double value = ((DoubleToken) inputBufferDelay.getToken())
                    .doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero serviceTime: " + value);
            }
            _inputBufferDelay = value;
        } else if (attribute == outputBufferDelay) {
            double value = ((DoubleToken) outputBufferDelay.getToken())
                    .doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero serviceTime: " + value);
            }
            _outputBufferDelay = value;
        } else if (attribute == switchFabricDelay) {
            double value = ((DoubleToken) switchFabricDelay.getToken())
                    .doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero serviceTime: " + value);
            }
            _switchFabricDelay = value;
        } else if (attribute == numberOfPorts) {
            int ports = ((IntToken) numberOfPorts.getToken()).intValue();
            for (int i = 0; i < ports; i++) {
                _inputTokens.put(i, new TreeSet());
                _outputTokens.put(i, new TreeSet());
            }
        }
        super.attributeChanged(attribute);
    }

    /** Initialize the actor variables.
     *  @throws IllegalActionException If the superclass throws it or 
     *  the switch table could not be parsed from the actor parameters.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextFireTime = null;
        for (int i = 0; i < _numberOfPorts; i++) {
            _inputTokens.put(i, new TreeSet());
            _outputTokens.put(i, new TreeSet());
        }

        // read the switching table from the parameters
        for (int i = 0; i < attributeList().size(); i++) {
            Attribute attribute = (Attribute) attributeList().get(i);
            try {
                int portNumber = Integer.parseInt(attribute.getName());
                Parameter param = (Parameter) attribute;
                Token token = param.getToken();
                Actor actor = (Actor) ((ObjectToken) token).getValue();
                _actorPorts.put(actor, portNumber);
            } catch (NumberFormatException ex) {
                // Parameter was not a number and therefore not a part of
                // the routing table.
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalActionException(this, "There was an error"
                        + "in the routing table information for "
                        + this.getName());
            }
        }
        for (int i = 0; i < _numberOfPorts; i++) {
            _inputTokens.put(i, new TreeSet());
            _outputTokens.put(i, new TreeSet());
        }
        _switchFabricQueue = new TreeSet();
    }

    /** Move tokens from the input queue to the switch fabric, move tokens
     *  from the switch fabric queue to the output queues and send tokens from the 
     *  output queues to the target receivers. When moving tokens between 
     *  queues the appropriate delays are considered. 
     *  @exception IllegalActionException Thrown if token cannot be sent to
     *  target receiver.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        // In a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null. 
        if (_nextFireTime != null && currentTime.compareTo(_nextFireTime) == 0) {

            // move tokens from input queue to switch fabric

            TimedEvent event;
            for (int i = 0; i < _numberOfPorts; i++) {
                if (_inputTokens.get(i).size() > 0) {
                    event = _inputTokens.get(i).first();
                    if (event.timeStamp.compareTo(currentTime) == 0) {
                        Time lastTimeStamp = currentTime;
                        if (_switchFabricQueue.size() > 0) {
                            lastTimeStamp = _switchFabricQueue.last().timeStamp;
                        }
                        _switchFabricQueue.add(new TimedEvent(lastTimeStamp
                                .add(_switchFabricDelay), event.contents));
                        _inputTokens.get(i).remove(event);
                    }
                }
            }

            // move tokens from switch fabric to output queue

            if (_switchFabricQueue.size() > 0) {
                event = _switchFabricQueue.first();
                if (event.timeStamp.compareTo(currentTime) == 0) {
                    Object[] output = (Object[]) event.contents;
                    Receiver receiver = (Receiver) output[0];

                    Actor actor;
                    if (receiver instanceof IntermediateReceiver) {
                        actor = (Actor) ((IntermediateReceiver) receiver).quantityManager;
                    } else {
                        actor = (Actor) receiver.getContainer().getContainer();
                    }
                    int actorPort = _actorPorts.get(actor);
                    Time lastTimeStamp = currentTime;
                    if (_outputTokens.get(actorPort).size() > 0) {
                        lastTimeStamp = _outputTokens.get(actorPort).last().timeStamp;
                    }
                    _outputTokens.get(actorPort).add(
                            new TimedEvent(lastTimeStamp
                                    .add(_outputBufferDelay), event.contents));
                    _switchFabricQueue.remove(event);
                }
            }

            // send tokens to target receiver

            for (int i = 0; i < _numberOfPorts; i++) {
                if (_outputTokens.get(i).size() > 0) {
                    event = _outputTokens.get(i).first();
                    if (event.timeStamp.compareTo(currentTime) == 0) {
                        Object[] output = (Object[]) event.contents;
                        Receiver receiver = (Receiver) output[0];
                        Token token = (Token) output[1];
                        if (receiver instanceof IntermediateReceiver) {
                            ((IntermediateReceiver) receiver).source = this;
                        }
                        receiver.put(token);
                        _outputTokens.get(i).remove(event);
                    }
                }
            }

            if (_debugging) {
                _debug("At time " + currentTime + ", completing send");
            }
        }
    }

    /** If there are still tokens in the queue and a token has been 
     *  produced in the fire, schedule a refiring.
     *  @exception IllegalActionExecption Thrown if refiring cannot be scheduled or 
     *  by super class.
     */
    public boolean postfire() throws IllegalActionException {
        _scheduleRefire();
        return super.postfire();
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param source The sending receiver.
     *  @param target receiver to send to.
     *  @param token The token to send.
     *  @throws IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        // FIXME add Continuous support.

        IntermediateReceiver ir = (IntermediateReceiver) source;

        int actorPortId = 0;
        if (ir.source != null) {
            Actor sender = ir.source;
            actorPortId = _actorPorts.get(sender);
        } else {
            throw new IllegalActionException(this, "The receiver " + receiver
                    + "does not have a source");
        }

        Time lastTimeStamp = currentTime;
        if (_inputTokens.get(actorPortId).size() > 0) {
            lastTimeStamp = _inputTokens.get(actorPortId).last().timeStamp;
        }
        _inputTokens.get(actorPortId).add(
                new TimedEvent(lastTimeStamp.add(_inputBufferDelay),
                        new Object[] { receiver, token }));
        _scheduleRefire();

        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }

    /** Reset the quantity manager and clear the tokens.
     */
    public void reset() {
        _inputTokens.clear();
        _outputTokens.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Number of ports on the switch. This parameter must contain an
     *  IntToken.  The value defaults to 4. */
    public Parameter numberOfPorts;

    /** Time it takes for a token to be put into the input queue.
     *  This parameter must contain a DoubleToken. The value defaults 
     *  to 0.1. */ 
    public Parameter inputBufferDelay;

    /** Time it takes for a token to be put into the output queue.
     *  This parameter must contain a DoubleToken. The value defaults 
     *  to 0.1. */ 
    public Parameter outputBufferDelay;

    /** Time it takes for a token to be processed by the switch fabric.
     *  This parameter must contain a DoubleToken. The value defaults 
     *  to 0.1. */ 
    public Parameter switchFabricDelay;
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Get next fire time for a set of tokens which is either the minimum 
     *  next fire time passed as an argument or the smallest timestamp of
     *  the tokens in the set.
     *  @param nextFireTime Minimum next fire time.
     *  @param tokens The set of tokens.
     *  @return The next time this actor should be fired based on the tokens
     *  in the queue.
     */
    protected Time _getNextFireTime(Time nextFireTime,
            TreeSet<TimedEvent> tokens) {
        if (tokens.size() > 0) {
            TimedEvent event = tokens.first();
            if (event.timeStamp.compareTo(nextFireTime) < 0) {
                nextFireTime = event.timeStamp;
            }
        }
        return nextFireTime;
    }

    /** Schedule a refiring of this actor based on the tokens in the queues.
     *  @throws IllegalActionException Thrown if actor cannot be refired 
     *  at the computed time.
     */
    protected void _scheduleRefire() throws IllegalActionException {
        _nextFireTime = Time.POSITIVE_INFINITY;
        for (int i = 0; i < _numberOfPorts; i++) {
            _nextFireTime = _getNextFireTime(_nextFireTime, _inputTokens.get(i));
            _nextFireTime = _getNextFireTime(_nextFireTime,
                    _outputTokens.get(i));
        }
        _nextFireTime = _getNextFireTime(_nextFireTime, _switchFabricQueue);
        _fireAt(_nextFireTime);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////    
    
    /** Time it takes for a token to be put into the input queue. */
    protected double _inputBufferDelay;

    
    /** Time it takes for a token to be put into the output queue. */
    protected double _outputBufferDelay;

    
    /** Time it takes for a token to be processed by the switch fabric. */
    protected double _switchFabricDelay;

    /** Mapping of actors to switch ports */
    protected HashMap<Actor, Integer> _actorPorts;

    /** Map target receivers to intermediate receivers. */
    protected Hashtable<Receiver, IntermediateReceiver> _receivers;

    /** Next time a token is sent and the next token can be processed. */
    protected Time _nextFireTime;

    /** Tokens received by the switch. */
    protected HashMap<Integer, TreeSet<TimedEvent>> _inputTokens;
    
    /** Tokens to be sent to outputs. */
    protected HashMap<Integer, TreeSet<TimedEvent>> _outputTokens;

    /** Number of switch ports. */
    protected int _numberOfPorts;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////    
    
    /** Tokens processed by the switch fabric. */
    private TreeSet<TimedEvent> _switchFabricQueue;
}
