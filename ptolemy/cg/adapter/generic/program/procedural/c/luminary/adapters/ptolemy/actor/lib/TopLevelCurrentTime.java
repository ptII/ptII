/* A adapter class for ptolemy.actor.lib.Scale

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
package ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.actor.lib;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;

//////////////////////////////////////////////////////////////////////////
//// TopLevelCurrentTime

/**
 An adapter class for ptolemy.actor.lib.TopLevelCurrentTime.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class TopLevelCurrentTime extends NamedProgramCodeGeneratorAdapter {
    /**
     *  Construct a Scale adapter.
     *  @param actor The given ptolemy.actor.lib.Scale actor.
     */
    public TopLevelCurrentTime(ptolemy.actor.lib.TopLevelCurrentTime actor) {
        super(actor);
    }
}
