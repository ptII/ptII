/* A adapter class for ptolemy.actor.lib.AddSubtract

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AddSubtract

/**
 A adapter class for ptolemy.actor.lib.AddSubtract.

 @author Man-Kit (Jackie) Leung, Gang Zhou, Bert Rodiers
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (mankit) Pending FIXME in AddSubtract.c: need to deallocate Tokens
 @Pt.AcceptedRating Yellow (cxh)
 */
public class AddSubtract extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct an AddSubtract adapter.
     * @param actor the associated actor
     */
    public AddSubtract(ptolemy.actor.lib.AddSubtract actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method generates code that loops through each
     * input [multi-ports] and combines (add or subtract) them.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.AddSubtract actor = (ptolemy.actor.lib.AddSubtract) getComponent();

        String outputType = getCodeGenerator().codeGenType(actor.output.getType());
        String plusType = getCodeGenerator().codeGenType(actor.plus.getType());
        String minusType = getCodeGenerator().codeGenType(actor.minus.getType());

        boolean minusOnly = !actor.plus.isOutsideConnected();

        ArrayList<String> args = new ArrayList<String>();

        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.appendCodeBlock("initSum");

        args.add("");
        args.add(outputType);
        args.add(plusType);

        for (int i = 1; i < actor.plus.getWidth(); i++) {
            args.set(0, Integer.valueOf(i).toString());
            codeStream.appendCodeBlock("plusBlock", args);
        }

        for (int i = minusOnly ? 1 : 0; i < actor.minus.getWidth(); i++) {
            args.set(0, Integer.valueOf(i).toString());
            args.set(2, minusType);
            codeStream.appendCodeBlock("minusBlock", args);
        }
        if (actor.output.isOutsideConnected()
                && actor.output.numberOfSinks() > 0) {
            // If the AddSubtract is in a Composite and the output is connected
            // to a port that is not connected, then don't generate code
            // for the output.  See test/auto/CompositeWithUnconnectedPort.xml
            codeStream.appendCodeBlock("outputBlock");
        }
        return processCode(codeStream.toString());
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code> from AddSubtract.c,
     * replace macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.AddSubtract actor = (ptolemy.actor.lib.AddSubtract) getComponent();

        ArrayList<String> args = new ArrayList<String>();

        Type type = actor.output.getType();
        args.add(targetType(type));

        CodeStream codeStream = _templateParser.getCodeStream();

        if (codeStream.isEmpty()) {
            codeStream.append(_eol
                    + getCodeGenerator().comment(
                            "preinitialize " + getComponent().getName()));
        }

        codeStream.appendCodeBlock("preinitBlock", args);

        return processCode(codeStream.toString());
    }
}
