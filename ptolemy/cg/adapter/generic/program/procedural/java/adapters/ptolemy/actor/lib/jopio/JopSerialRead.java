/* A code generation adapter class for actor.lib.jopio.JopSerialRead.

 @Copyright (c) 2009-2010 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.jopio;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation adapter class for ptolemy.actor.lib.jopio.JopSerialRead.
 *
 * @author Martin Schoeberl
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mschoebe)
 * @Pt.AcceptedRating Red (mschoebe)
 */
public class JopSerialRead extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct a JopWatchDog adapter.
     * @param actor The associated actor.
     */
    public JopSerialRead(ptolemy.actor.lib.jopio.JopSerialRead actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>BooleanOutput</code> from JopWatchDog.j,
     * replaces macros with their values and returns the results.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        ptolemy.actor.lib.jopio.JopSerialRead actor = (ptolemy.actor.lib.jopio.JopSerialRead) getComponent();

        String type = getCodeGenerator().codeGenType(actor.output.getType());
        if (!getCodeGenerator().isPrimitive(type)) {
            throw new IllegalActionException("must be primitive");
        }

        ArrayList args = new ArrayList();

        //        args.add(Integer.valueOf(0));
        args.add("0");
        code.append(_templateParser.generateBlockCode("FireBlock", args));

        return code.toString();
    }
}
