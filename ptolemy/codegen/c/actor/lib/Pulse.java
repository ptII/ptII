/* A helper class for ptolemy.actor.lib.Pulse

 Copyright (c) 2006-2007 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.ArrayToken;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Pulse

/**
 A helper class for ptolemy.actor.lib.Pulse.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class Pulse extends CCodeGeneratorHelper {

    /** Construct a  Pulse helper.
     *  @param actor the associated actor
     */
    public Pulse(ptolemy.actor.lib.Pulse actor) {
        super(actor);
    }

    /**
     * Generate initialize code.
     * Reads the <code>preinitBlock</code> from Chop.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());

        ArrayList args = new ArrayList();

        ptolemy.actor.lib.Pulse actor = (ptolemy.actor.lib.Pulse) getComponent();

        Type type = ((ArrayToken) actor.values.getToken()).getElementType();

        if (!isPrimitive(type)) {
            args.add("$tokenFunc($ref(values, 0)::zero())");
        } else {
            args.add("0");
        }

        code.append(_generateBlockCode("initBlock", args));
        return code.toString();
    }

    /**
     * Generate preinitialize code.
     * Reads the <code>preinitBlock</code> from Chop.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        ArrayList args = new ArrayList();

        ptolemy.actor.lib.Pulse actor = (ptolemy.actor.lib.Pulse) getComponent();

        Type type = ((ArrayToken) actor.values.getToken()).getElementType();

        args.add(targetType(type));

        code.append(_generateBlockCode("preinitBlock", args));
        return code.toString();
    }
}
