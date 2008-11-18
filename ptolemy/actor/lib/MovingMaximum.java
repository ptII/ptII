/* An actor that outputs the maximum value that it has received since the start
of execution.

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.data.ScalarToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 On each firing, this actor consumes exactly one scalar token at its input port.
 The value of the token is compared to the maximum value maintained since the
 start of the execution. The greater of the two is output to the output port in
 the same firing, and the maximum value is set with that greater value in
 postfire().

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class MovingMaximum extends Transformer {

    /** Construct an actor with the specified container and name.
     *
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MovingMaximum(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeAtMost(BaseType.SCALAR);
        output.setTypeSameAs(input);
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
     *  @return A new ComponentEntity.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MovingMaximum newObject = (MovingMaximum) super.clone(workspace);
        newObject.input.setTypeAtMost(BaseType.SCALAR);
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Consume a token at the input port, and produce the greater of that value
     *  and the maintained maximum value to the output port.
     *
     *  @exception IllegalActionException If getting token from input or
     *  sending token to output throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        _value = (ScalarToken) input.get(0);
        if (_maximum == null || _value.isGreaterThan(_maximum).booleanValue()) {
            output.broadcast(_value);
        } else {
            output.broadcast(_maximum);
        }
    }

    /** Initialize the maintained maximum value to be null so it will be set
     *  with the first input at the input port.
     *
     *  @exception IllegalActionException If the initialize() method of the
     *  superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _maximum = null;
    }

    /** Commit the maximum value observed since the start of execution to the
     *  maximum field to be compared with later inputs.
     *
     *  @exception IllegalActionException If the postfire() method of the
     *  superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();

        if (_maximum == null || _value.isGreaterThan(_maximum).booleanValue()) {
            _maximum = _value;
        }

        return result;
    }

    /** Return true if the prefire() method of the superclass returns true, and
     *  there is at least one token at the input port.
     *
     *  @exception IllegalActionException If the prefire() method of the
     *  superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        return super.prefire() && input.hasToken(0);
    }

    // The maximum value observed so far.
    private ScalarToken _maximum;

    // The value observed in the current firing.
    private ScalarToken _value;
}
