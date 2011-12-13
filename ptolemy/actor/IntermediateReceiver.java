/* This actor implements a receiver that adds functionality to another receiver.

@Copyright (c) 2011-2011 The Regents of the University of California.
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

package ptolemy.actor;

import java.util.List;

import ptolemy.actor.lib.qm.CompositeQuantityManager;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/** A receiver that delegates to another receiver all method calls except
 *  {@link #put(Token)} (and its variants), for which it delegates to a
 *  quantity manager. The delegated receiver and the quantity manager are
 *  specified as constructor arguments.
 *  <p>
 *  This can be used, for example, when multiple communication links share
 *  resources. The quantity manager can, for example, delay the delivery
 *  of tokens to the delegated receiver to take into account resource
 *  availability. It could also be used to make a centralized record
 *  of various communications.
 *  <p>
 *  Subclasses of this receiver may also intervene on method calls other
 *  than put().
 *  @author Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class IntermediateReceiver extends AbstractReceiver {

    /** Construct an intermediate receiver with no container that wraps the
     *  specified receiver using the specified quantity manager.
     *  @param qm The quantity manager that receives tokens received by this receiver.
     *  @param receiver The receiver wrapped by this intermediate receiver.
     */
    public IntermediateReceiver(QuantityManager qm, Receiver receiver) {
        _receiver = receiver;
        quantityManager = qm;
    }
    
    public IntermediateReceiver(QuantityManager qm, Receiver receiver, IOPort port) {
        _receiver = receiver;
        quantityManager = qm;
        _port = port;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables               ////
    
    /** Quantity manager that receives tokens from this receiver. */
    public QuantityManager quantityManager;

    /** The source actor that sent a token to this receiver. */
    public Actor source;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset the quantity manager and the receiver that we delegate to.
     */
    public void clear() throws IllegalActionException {
        quantityManager.reset();
        //_receiver.reset();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     *  @return A list of instances of Token.
     *  @exception IllegalActionException Always thrown in this base class.
     */
    public List<Token> elementList() throws IllegalActionException {
        return _receiver.elementList();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     *  @exception NoTokenException If the delegated receiver throws it.
     */
    public Token get() throws NoTokenException {
        return _receiver.get();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     *  @return The port containing the internal receiver.
     *  @see #setContainer(IOPort)
     */
    public IOPort getContainer() {
        return _receiver.getContainer();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    public boolean hasRoom() {
        return _receiver.hasRoom();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    public boolean hasRoom(int numberOfTokens) {
        return _receiver.hasRoom(numberOfTokens);
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    public boolean hasToken() {
        return _receiver.hasToken();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    public boolean hasToken(int numberOfTokens) {
        return _receiver.hasToken(numberOfTokens);
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    public boolean isKnown() {
        return _receiver.isKnown();
    }

    /** Forward the specified token to quantity manager specified in
     *  the constructor.
     */
    public void put(Token token) throws NoRoomException, IllegalActionException {
        if (_port != null) { 
            ((CompositeQuantityManager)quantityManager)
                    .sendToken(this, _receiver, token, _port);
        } else {
            quantityManager.sendToken(this, _receiver, token);
        }
    }

    /** Set the container of the internal receiver.
     *  @param port The container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort. Not thrown in this base class,
     *   but may be thrown in derived classes.
     *  @see #getContainer()
     */
    public void setContainer(IOPort port) throws IllegalActionException {
        _receiver.setContainer(port);
    }
     
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Target receiver that is wrapped by this intermediate receiver.  */
    protected Receiver _receiver;
    
    protected IOPort _port;
}
