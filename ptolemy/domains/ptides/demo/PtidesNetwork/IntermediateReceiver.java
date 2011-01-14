/* This actor implements a receiver that adds functionality to another receiver.

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

package ptolemy.domains.ptides.demo.PtidesNetwork;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.data.IntToken;
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
 *  @author Patricia Derler
 */
public class IntermediateReceiver extends AbstractReceiver {
    
    /** Construct an intermediate receiver with no container that wraps the
     *  specified receiver using the specified quantity manager.
     *  @param quantityManager The quantity manager that receives tokens received by this receiver.
     *  @param receiver The receiver wrapped by this intermediate receiver.
     */
    public IntermediateReceiver(QuantityManager quantityManager, Receiver receiver) {
        _receiver = receiver;
        _quantityManager = quantityManager;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset the quantity manager and the receiver that we delegate to.
     */
    public void clear() throws IllegalActionException {
        _quantityManager.reset();
        _receiver.reset();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     *  @throws NoTokenException If the delegated receiver throws it.
     */
    public Token get() throws NoTokenException {
        return _receiver.get();
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

    /** Forward the specified token to quantity manager specified in
     *  the constructor.
     */
    public void put(Token token) throws NoRoomException, IllegalActionException {
        _quantityManager.sendToken(_receiver, token);
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Target receiver that is wrapped by this intermediate receiver.  */
    private Receiver _receiver;
    
    /** Quantity manager that receives tokens from this receiver. */
    private QuantityManager _quantityManager;
}
