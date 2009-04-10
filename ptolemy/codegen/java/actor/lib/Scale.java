/* A helper class for ptolemy.actor.lib.Scale

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
package ptolemy.codegen.java.actor.lib;

import ptolemy.codegen.java.kernel.JavaCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Scale

/**
 A helper class for ptolemy.actor.lib.Scale.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class Scale extends JavaCodeGeneratorHelper {
    /**
     *  Construct a Scale helper.
     *  @param actor The given ptolemy.actor.lib.Scale actor.
     */
    public Scale(ptolemy.actor.lib.Scale actor) {
        super(actor);
    }

    /**
     * Generate fire code for the Scale actor.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.Scale actor = (ptolemy.actor.lib.Scale) getComponent();

        String type = isPrimitive(actor.input.getType()) ? "" : "Token";

        _codeStream.appendCodeBlock(type + "FireBlock", false);
        return processCode(_codeStream.toString());
    }
}
