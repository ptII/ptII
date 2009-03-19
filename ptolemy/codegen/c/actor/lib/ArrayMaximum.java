/* A code generation helper class for actor.lib.ArrayMaximum

 @Copyright (c) 2006-2007 The Regents of the University of California.
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

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.ArrayMaximum.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class ArrayMaximum extends CCodeGeneratorHelper {

    /**
     * Construct an ArrayMaximum helper.
     * @param actor The associated actor.
     */
    public ArrayMaximum(ptolemy.actor.lib.ArrayMaximum actor) {
        super(actor);
    }
}
