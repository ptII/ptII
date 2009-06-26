/* An actor that hold the last event and outputs a constant signal.

 Copyright (c) 1998-2007 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.domains.continuous.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ZeroOrderHold

/**
 Convert discrete events at the input to a continuous-time
 signal at the output by holding the value of the discrete
 event until the next discrete event arrives. Specifically,
 on each firing, produce the currently recorded value,
 which is <i>defaultValue</i> initially. If an input is
 present, then record the value of the input (after producing
 the output), and request a refiring at the current time.
 This actor thus introduces a microstep delay in superdense
 time.

 @author Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class ZeroOrderHold extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public ZeroOrderHold(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        defaultValue = new Parameter(this, "defaultValue", new IntToken(0));
        output.setTypeAtLeast(input);
        output.setTypeAtLeast(defaultValue);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-25,10 -15,10 -15,-10 5,-10\"/>\n"
                + "<polyline points=\"5,-10 5,0 15,0 15,10 25,10\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                       ////

    /** Default output before any input has received.
     *  The default is an integer with value 0, but any
     *  type is acceptable.
     *  The type of the output is set to at least the type of
     *  this parameter (and also at least the type of the input).
     */
    public Parameter defaultValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ZeroOrderHold newObject = (ZeroOrderHold) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(newObject.defaultValue);
        return newObject;
    }

    /** Output the latest token consumed from the consumeCurrentEvents()
     *  call.
     *  @exception IllegalActionException If the token cannot be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, _lastToken);
    }

    /** Initialize token. If there is no input, the initial token is
     *  a Double Token with the value specified by the defaultValue parameter.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastToken = defaultValue.getToken();
    }

    /** Return false to indicate that this actor can produce outputs without
     *  knowing the inputs.
     *  @return false.
     */
    public boolean isStrict() {
        return false;
    }

    /** If there is an input, record it and request a refiring at the
     *  current time.
     *  @exception IllegalActionException If the refiring request fails.
     */
    public boolean postfire() throws IllegalActionException {
        super.postfire();
        if (input.isKnown() && input.hasToken(0)) {
            _lastToken = input.get(0);
            if (_debugging) {
                _debug("Reading an input " + _lastToken.toString());
            }
            _fireAt(getDirector().getModelTime());
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Saved token.
    private Token _lastToken;
}
