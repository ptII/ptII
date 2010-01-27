/* A helper class for ptolemy.actor.lib.logic.LogicFunction

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.codegen.java.actor.lib.logic;

import ptolemy.codegen.java.kernel.JavaCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// LogicFunction

/**
 A helper class for ptolemy.actor.lib.logic.LogicFunction.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class LogicFunction extends JavaCodeGeneratorHelper {
    /** Constructor method for the LogicFunction helper.
     *  @param actor the associated actor.
     */
    public LogicFunction(ptolemy.actor.lib.logic.LogicFunction actor) {
        super(actor);
    }

    /** Generate fire code.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append(super._generateFireCode());

        ptolemy.actor.lib.logic.LogicFunction actor = (ptolemy.actor.lib.logic.LogicFunction) getComponent();

        String function = actor.function.getExpression();

        codeBuffer.append(_eol + "    ");
        codeBuffer.append("$ref(output) = ");

        if (function.equals("nand") || function.equals("nor")
                || function.equals("xnor")) {
            codeBuffer.append("!");
        }

        codeBuffer.append("((");

        for (int i = 0; i < actor.input.getWidth(); i++) {
            codeBuffer.append("$ref(input#" + i + ")");

            if (i < (actor.input.getWidth() - 1)) {
                if (function.equals("and") || function.equals("nand")) {
                    codeBuffer.append(" & ");
                } else if (function.equals("or") || function.equals("nor")) {
                    codeBuffer.append(" | ");
                } else if (function.equals("xor") || function.equals("xnor")) {
                    codeBuffer.append(" ^ ");
                }
            }
        }

        codeBuffer.append(")");

        codeBuffer.append(");" + _eol);
        return processCode(codeBuffer.toString());
    }
}
