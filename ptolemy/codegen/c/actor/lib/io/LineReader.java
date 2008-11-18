/* A code generation helper class for actor.lib.io.LineReader

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
package ptolemy.codegen.c.actor.lib.io;

import java.util.ArrayList;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.io.LineReader.
 *
 * @author Jackie
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class LineReader extends CCodeGeneratorHelper {
    /**
     * Construct the LineReader helper.
     * @param actor the associated actor.
     */
    public LineReader(ptolemy.actor.lib.io.LineReader actor) {
        super(actor);
    }

    /**
     * Generate initialize code.
     * This method reads the <code>initBlock</code> from LineReader.c,
     * replaces macros with their values and returns the processed code string.
     * @return The processed <code>initBlock</code>.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        _codeStream.clear();
        _codeStream.appendCodeBlock("initBufferBlock");

        ptolemy.actor.lib.io.LineReader actor = (ptolemy.actor.lib.io.LineReader) getComponent();

        int skipLines = Integer.parseInt(actor.numberOfLinesToSkip
                .getExpression());

        String fileNameString = actor.fileOrURL.getExpression();

        if (fileNameString.equals("System.in")) {
            _fileOpen = false;
            _codeStream.append("openForStdin");
        } else {
            _fileOpen = true;

            fileNameString = FileReader.getFileName(actor.fileOrURL);
            ArrayList args = new ArrayList();
            args.add(fileNameString);
            _codeStream.appendCodeBlock("openForRead", args);

            for (int i = 0; i < skipLines; i++) {
                _codeStream.appendCodeBlock("skipLine");
            }
        }

        return processCode(_codeStream.toString());
    }

    /**
     * Generate wrap up code.
     * This method reads the <code>wrapUpBlock</code> from LineReader.c,
     * replaces macros with their values and returns the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateWrapupCode() throws IllegalActionException {
        super.generateWrapupCode();
        if (_fileOpen) {
            _codeStream.appendCodeBlock("closeFile");
        }
        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * LineReader actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the LineReader actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<stdio.h>");
        return files;
    }

    /**
     * indicate whether or not the user requests to open a file
     * e.g. false - write to standard (console) output
     *      true - some file name is specified
     */
    private boolean _fileOpen = false;
}
