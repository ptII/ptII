/* A adapter class for ptolemy.actor.lib.Accumulator

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib;

import java.util.ArrayList;
import java.util.Set;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

/**
 * An adapter class for ptolemy.actor.lib.Accumulator.
 *
 * @author Man-Kit Leung, Gang Zhou
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class Accumulator extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct an Accumulator adapter.
     * @param actor the associated actor.
     */
    public Accumulator(ptolemy.actor.lib.Accumulator actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Accumulator.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.Accumulator actor = (ptolemy.actor.lib.Accumulator) getComponent();

        CodeStream codeStream = getTemplateParser().getCodeStream();
        
        ArrayList args = new ArrayList();
        args.add(Integer.toString(0));
        String type = getCodeGenerator().codeGenType(actor.output.getType());

        if (actor.reset.isOutsideConnected()) {
            codeStream.appendCodeBlock("initReset");
            for (int i = 1; i < actor.reset.getWidth(); i++) {
                args.set(0, Integer.toString(i));
                codeStream.appendCodeBlock("readReset", args);
            }
            codeStream.appendCodeBlock("ifReset");

            codeStream
                    .appendCodeBlock((type.equals("String")) ? "StringInitSum"
                            : "InitSum");
        }

        if (!getCodeGenerator().isPrimitive(type)) {
            type = "Token";
        }
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.toString(i));
            codeStream.appendCodeBlock(type + "FireBlock", args);
        }
        codeStream.appendCodeBlock("sendBlock");

        return processCode(codeStream.toString());
    }

    /** Generate the initialize code.
     *  The method reads in <code>initBlock</code> from Accumulator.c,
     *  replaces macros with their values and returns the processed code
     *  block.
     *  @return The initialize code.
     *  @exception IllegalActionException If the code stream encounters an
     *   error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.Accumulator actor = (ptolemy.actor.lib.Accumulator) getComponent();

        CodeStream codeStream = getTemplateParser().getCodeStream();

        codeStream
                .appendCodeBlock((actor.output.getType() == BaseType.STRING) ? "StringInitSum"
                        : "InitSum");

        return processCode(codeStream.toString());
    }

    /** Generate the preinitialize code.
     *  The method reads in <code>preinitBlock</code> from Accumulator.c,
     *  replaces macros with their values and returns the processed code
     *  block.
     *  @return The preinitialize code.
     *  @exception IllegalActionException If the code stream encounters an
     *   error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.Accumulator actor = (ptolemy.actor.lib.Accumulator) getComponent();

        CodeStream codeStream = getTemplateParser().getCodeStream();

        if (actor.reset.isOutsideConnected()) {
            codeStream.appendCodeBlock("preinitReset");
        }

        return processCode(codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * Accumulator actor.
     * @return A set of Strings that are names of the header files
     *  needed by the code generated for the StringCompare actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<string.h>");
        return files;
    }
}
