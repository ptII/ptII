/* Extract minimum element from an array.

 Copyright (c) 2003-2010 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayMinimum

/**
 Extract the minimum element from an array.  This actor reads an array
 from the <i>input</i> port and sends the smallest of its elements to the
 <i>output</i> port.  The index of the smallest element (closest to minus
 infinity) is sent to the <i>index</i> output port. If there is more than
 one entry in the array with the minimum value, then the index of the
 first such entry is what is produced.

 @author Mark Oliver and Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayMinimum extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayMinimum(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create index port
        index = new TypedIOPort(this, "index", false, true);
        index.setTypeEquals(BaseType.INT);

        // Type constraints.
        output.setTypeAtLeast(ArrayType.elementType(input));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The port producing the index of the largest element.
     *  This is port has type int.
     */
    public TypedIOPort index;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new instance of ArrayElement.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayMinimum newObject = (ArrayMinimum) super.clone(workspace);
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
     *  the minimum of its elements on the <i>output</i> port and the index
     *  of that element on the <i>index</i> port.  If there is no token
     *  on the input, then no output is produced.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int indexValue = 0;

        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken) input.get(0);
            ScalarToken currentMin = (ScalarToken) token.getElement(indexValue);
            ScalarToken temp = null;
            int i;

            for (i = indexValue + 1; i < token.length(); i++) {
                temp = (ScalarToken) token.getElement(i);

                if (currentMin.isGreaterThan(temp).booleanValue() == true) {
                    indexValue = i;
                    currentMin = temp;
                }
            }

            output.send(0, currentMin);
            index.broadcast(new IntToken(indexValue));
        }
    }
}
