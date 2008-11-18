/*
 @Copyright (c) 2007 The Regents of the University of California.
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
package ptolemy.codegen.java.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.java.kernel.JavaCodeGeneratorHelper;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Generate C code for an actor that produces an output token on
 * on each firing with a value that is equal to the absolute value of
 * the input.
 *
 * @see ptolemy.actor.lib.VectorAssembler
 * @author Man-Kit Leung
 * @version $Id: VectorAssembler.java 47495 2007-12-06 21:57:21Z cxh $
 * @since Ptolemy II 6.1
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 *
 */
public class VectorAssembler extends JavaCodeGeneratorHelper {
    /**
     * Constructor method for the VectorAssembler helper.
     * @param actor the associated actor
     */
    public VectorAssembler(ptolemy.actor.lib.VectorAssembler actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from VectorAssembler.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.VectorAssembler actor = (ptolemy.actor.lib.VectorAssembler) getComponent();

        StringBuffer fireCode = new StringBuffer();
        ArrayList args = new ArrayList();
        args.add(Integer.valueOf(0));

        Type type = actor.input.getType();
        if (isPrimitive(type)) {
            args.add(codeGenType(type));
        }

        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            fireCode.append(_generateBlockCode("fireBlock", args));
        }

        return processCode(fireCode.toString());
    }
}
