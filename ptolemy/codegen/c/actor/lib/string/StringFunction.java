/* A code generation helper class for actor.lib.string.StringFunction

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
package ptolemy.codegen.c.actor.lib.string;

import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.string.StringFunction.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class StringFunction extends CCodeGeneratorHelper {

    /**
     * Construct the StringFunction helper.
     * @param actor The associated actor.
     */
    public StringFunction(ptolemy.actor.lib.string.StringFunction actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from StringFunction.c,
     * replace macros with their values and return the processed code
     * block.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        ptolemy.actor.lib.string.StringFunction actor = (ptolemy.actor.lib.string.StringFunction) getComponent();
        String function = actor.function.getExpression();
        if (function.equals("trim")) {
            _codeStream.appendCodeBlock("trimBlock");
        } else if (function.equals("toLowerCase")) {
            _codeStream.appendCodeBlock("toLowerCaseBlock");
        } else if (function.equals("toUpperCase")) {
            _codeStream.appendCodeBlock("toUpperCaseBlock");
        } else {
            throw new IllegalActionException("Unhandled function \"" + function
                    + "\".");
        }
        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * StringFunction actor.
     * @return A set of Strings that are names of the header files
     *  needed by the code generated for the StringFunction actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<ctype.h>");
        files.add("<strings.h>");
        return files;
    }
}
