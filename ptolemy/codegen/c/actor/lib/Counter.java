/* A code generation helper class for actor.lib.Commutator

 @Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.Counter.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class Counter extends CCodeGeneratorHelper {
    /**
     * Construct the Counter helper.
     * @param actor the associated actor
     */
    public Counter(ptolemy.actor.lib.Counter actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * If both the increment and decrement ports are connected, the counter
     * is offset. Otherwise, if either the increment port or decrement port
     * is connected, read in the <code>incrementBlock</code> or
     * <code>decrementBlock</code> from Counter.c respectively. Replace macros
     * with their values and return the processed code block.
     *
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.Counter actor = (ptolemy.actor.lib.Counter) getComponent();

        boolean doDecrement = actor.decrement.isOutsideConnected();
        boolean doIncrement = actor.increment.isOutsideConnected();

        if (doDecrement && !doIncrement) {
            _codeStream.appendCodeBlock("decrementBlock");
        } else if (!doDecrement && doIncrement) {
            _codeStream.appendCodeBlock("incrementBlock");
        }
        return processCode(_codeStream.toString());
    }
}
