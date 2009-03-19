/* A code generation helper class for actor.lib.Quantizer

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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.Quantizer.
 *
 * @author Man-Kit Leung, Shamik B
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public class Quantizer extends CCodeGeneratorHelper {
    /**
     * Construct the Quantizer helper.
     * @param actor The associated actor.
     */
    public Quantizer(ptolemy.actor.lib.Quantizer actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from Quantizer.c,
     * replace macros with their values and return the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.Quantizer actor = (ptolemy.actor.lib.Quantizer) getComponent();

        ArrayList arguments = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(actor.levels
                .getExpression(), ",");
        arguments.add(Integer.valueOf(tokenizer.countTokens()));

        _codeStream.appendCodeBlock("fireBlock", arguments);
        return processCode(_codeStream.toString());
    }

    /**
     * Generate initialize code.
     * Read the <code>initBlock</code> from Quantizer.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.Quantizer actor = (ptolemy.actor.lib.Quantizer) getComponent();

        ArrayList arguments = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(actor.levels
                .getExpression(), ",");
        arguments.add(Integer.valueOf(tokenizer.countTokens()));

        _codeStream.appendCodeBlock("initBlock", arguments);
        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * Quantizer actor.
     * @return A set of Strings that are names of the header files
     *  needed by the code generated for the Quantizer actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<stdio.h>");
        files.add("<math.h>");
        return files;
    }
}
