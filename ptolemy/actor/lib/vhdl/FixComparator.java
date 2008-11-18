/** An actor that outputs the fixpoint value of the concatenation of
 the input bits.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.actor.lib.vhdl;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.Precision;

//////////////////////////////////////////////////////////////////////////
//// FixComparator

/**
 Produce an output token on each firing with a FixPoint value that is
 equal to the sum of all the inputs at the plus port minus the inputs at the
 minus port.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class FixComparator extends SynchronousFixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FixComparator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        A = new TypedIOPort(this, "A", true, false);
        A.setTypeEquals(BaseType.FIX);

        B = new TypedIOPort(this, "B", true, false);
        B.setTypeEquals(BaseType.FIX);

        operation = new StringParameter(this, "operation");
        operation.setExpression("=");
        operation.addChoice("=");
        operation.addChoice("!=");
        operation.addChoice("<");
        operation.addChoice("<=");
        operation.addChoice(">");
        operation.addChoice(">=");

        _setAndHideQuantizationParameters("U1.0", "CLIP", "HALF_EVEN");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for tokens to be added.  This is a multiport of fix point
     *  type
     */
    public TypedIOPort A;

    /** Input for tokens to be subtracted.  This is a multiport of fix
     *  point type.
     */
    public TypedIOPort B;

    /** Indicate whether addition or subtraction needs to be performed.
     */
    public Parameter operation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output the fixpoint value of the sum of the input bits.
     *  If there is no inputs, then produce null.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        FixToken result = null;
        Precision precision = new Precision(0, 1, 0);

        if (A.isKnown() && B.isKnown()) {
            result = new FixToken(0, precision);
            FixToken inputA = new FixToken();
            FixToken inputB = new FixToken();

            if (A.hasToken(0)) {
                inputA = (FixToken) A.get(0);
            }

            if (B.hasToken(0)) {
                inputB = (FixToken) B.get(0);
            }

            if (inputA.fixValue().getPrecision().getNumberOfBits() != inputB
                    .fixValue().getPrecision().getNumberOfBits()) {

                throw new IllegalActionException(this,
                        "Input A has different width than Input B port");
            }

            if (operation.getExpression().equals("=")) {
                if (inputA.equals(inputB)) {
                    result = new FixToken(1, precision);
                }
            } else if (operation.getExpression().equals("!=")) {
                if (!inputA.equals(inputB)) {
                    result = new FixToken(1, precision);
                }
            } else if (operation.getExpression().equals("<")) {
                if (inputA.isLessThan(inputB).booleanValue()) {
                    result = new FixToken(1, precision);
                }
            } else if (operation.getExpression().equals("<=")) {
                if (inputA.equals(inputB)
                        || inputA.isLessThan(inputB).booleanValue()) {
                    result = new FixToken(1, precision);
                }
            } else if (operation.getExpression().equals(">")) {
                if (inputA.isGreaterThan(inputB).booleanValue()) {
                    result = new FixToken(1, precision);
                }
            } else if (operation.getExpression().equals(">=")) {
                if (inputA.equals(inputB)
                        || inputA.isGreaterThan(inputB).booleanValue()) {
                    result = new FixToken(1, precision);
                }
            }

            sendOutput(output, 0, result);
        }

        else {
            output.resend(0);
        }
    }

    /** Override the base class to declare that the <i>A</i> and
     *  <i>B</i> ports do not depend on the <i>output</i> in a firing.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(A, output);
        removeDependency(B, output);
    }

}
