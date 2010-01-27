/* A adapter class for ptolemy.actor.lib.Differential

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;

//////////////////////////////////////////////////////////////////////////
//// Differential

/**
 A adapter class for ptolemy.actor.lib.Differential.

 @author Jeff C. Jensen
@version $Id$
@since Ptolemy II 8.0
 */
public class Differential extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct a Differential adapter.
     * @param actor the associated actor
     */
    public Differential(ptolemy.actor.lib.Differential actor) {
        super(actor);
    }
}
