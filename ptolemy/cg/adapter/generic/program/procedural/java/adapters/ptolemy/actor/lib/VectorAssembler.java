/* A adapter class for ptolemy.actor.lib.VectorAssembler

 Copyright (c) 2006-2010 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib;

//////////////////////////////////////////////////////////////////////////
//// VectorAssembler
/**
 A adapter class for VectorAssembler.

 @author Christopher Brooks
 @version Id: VectorAssembler.java 57046 2010-01-27 23:35:53Z cxh $
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class VectorAssembler
        extends
        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.VectorAssembler {
    /**
     *  Construct a VectorAssembler adapter.
     *  @param actor The given ptolemy.actor.lib.VectorAssembler actor.
     */
    public VectorAssembler(ptolemy.actor.lib.VectorAssembler actor) {
        super(actor);
    }
}
