/* Code generator helper class associated with the SDFDirector class.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.ptides.kernel;

import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.actor.Director;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////
////PtidesDirector

/**
 Code generator helper associated with the PTIDESDirector class. This class
 is also associated with a code generator.
 Top-level director for PTIDES models.
 Unlike in the ptolemy implementation, this level generates code for
 communication between different platforms. Such that events are transmitted to the
 correct platform, and the platforms can distinguish from which actor should fire
 these events
 @author Jia Zou
 @version $Id$
 @since Ptolemy II 7.0
 @Pt.ProposedRating red (jiazou)
 @Pt.AcceptedRating
 */
public class PtidesDirector extends Director
/* CompositeProcessDirector implements TimedDirector*/ {

    /** Construct the code generator helper associated with the given
     *  PTIDESDirector.
     *  @param ptidesDirector The associated
     *  ptolemy.domains.ptides.kernel.PtidesDirector
     */
    public PtidesDirector(ptolemy.domains.ptides.kernel.PtidesDirector ptidesDirector) {
        super(ptidesDirector);
    }

    /** Generate a main loop for an execution under the control of
     *  this director.  In this base class, this simply delegates
     *  to generateFireCode() and generatePostfireCOde().
     *  @return Whatever generateFireCode() returns.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(CodeGeneratorHelper.generateName(_director
                .getContainer()) + "();" + _eol);

        return code.toString();
    }

    public String generateFireFunctionCode() throws IllegalActionException {
        return "";
    }
}
