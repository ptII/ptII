/* An actor that outputs the average of the input array.

 Copyright (c) 2003-2006 The Regents of the University of California.
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

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayAverage

/**
 Compute the average of the elements in an array.  This actor reads an
 array from the <i>input</i> port and sends the average of its elements
 to the <i>output</i> port. The output data type is at least the
 type of the elements of the input array.  The elements of the input
 array have to support addition and division by an integer, or an
 exception will be thrown in the fire() method.

 @author Mark Oliver, Edward A. Lee
 @version $ID: ArrayAverage.java,v0.1 2003/07/01
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayAverage extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayAverage(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // set type constraints.
        output.setTypeAtLeast(ArrayType.elementType(input));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new instance of ArrayAverage.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayAverage newObject = (ArrayAverage) super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.input));
        } catch (IllegalActionException e) {
            // Should have been caught before.
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Consume at most one array from the input port and produce
     *  the average of its elements on the <i>output</i> port.
     *  If there is no token on the input, or if the input array
     *  is empty, then no output is produced.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken) input.get(0);

            if (token.length() == 0) {
                return;
            }

            Token sum = token.getElement(0);

            for (int i = 1; i < token.length(); i++) {
                sum = sum.add(token.getElement(i));
            }

            output.send(0, sum.divide(new IntToken(token.length())));
        }
    }
}
