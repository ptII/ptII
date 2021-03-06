/* A code generation helper class for domains.sr.lib.gui.NonStrictDisplay
 @Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.sr.lib.gui;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.gui.Display;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.domains.sr.lib.gui.NonStrictDisplay.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class NonStrictDisplay extends Display {

    /**
     * Construct a NonStrictDisplay helper.
     * @param actor The associated actor.
     */
    public NonStrictDisplay(ptolemy.domains.sr.lib.gui.NonStrictDisplay actor) {
        super(actor);
    }

    /**
     * Get the files needed by the code generated for the
     * NonStrictDisplay actor.
     * @return A set of Strings that are names of the header files
     *  needed by the code generated for the NonStrictDisplay actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.addAll(super.getHeaderFiles());
        files.add("<stdio.h>");

        return files;
    }
}
