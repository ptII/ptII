/* A helper class for actor.lib.conversions.StringToUnsignedByteArray

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
package ptolemy.codegen.c.actor.lib.conversions;

import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.conversions.StringToIntArray.
 *
 * @author Teale Fristoe
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (tbf)
 * @Pt.AcceptedRating
 */
public class StringToUnsignedByteArray extends CCodeGeneratorHelper {
    /**
     * Construct the StringToUnsignedByteArray helper.
     * @param actor the associated actor.
     */
    public StringToUnsignedByteArray(ptolemy.actor.lib.conversions.StringToUnsignedByteArray actor) {
        super(actor);
    }

    /**
     * Get the files needed by the code generated for the
     * StringToUnsignedByteArray actor.
     * @return A set of Strings that are names of the header files
     *  needed by the code generated for the StringSubstring actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<string.h>");
        files.add("<stddef.h>");
        return files;
    }
}
