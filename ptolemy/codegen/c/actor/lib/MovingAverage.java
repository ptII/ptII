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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Generate C code for an actor that computes the moving average.
 *
 * @see ptolemy.actor.lib.MovingAverage
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 6.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (cxh)
 *
 */
public class MovingAverage extends CCodeGeneratorHelper {
    /**
     * Constructor method for the MovingAverage helper.
     * @param actor the associated actor
     */
    public MovingAverage(ptolemy.actor.lib.MovingAverage actor) {
        super(actor);
    }

    /**
     * Generate preinitialize code.
     * Read the <code>CommonPreinitBlock</code> from MovingAverage.c
     * replace macros with their values and return the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.MovingAverage actor = (ptolemy.actor.lib.MovingAverage) getComponent();

        ArrayList args = new ArrayList();

        Type type = actor.output.getType();
        if (isPrimitive(type)) {
            args.add(targetType(type));
            _codeStream.appendCodeBlock("CommonPreinitBlock", args);
        } else {
            throw new IllegalActionException("Non-primitive types " + type
                    + " not yet supported by MovingAverage");
        }

        return processCode(_codeStream.toString());
    }
}
