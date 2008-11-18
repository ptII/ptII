/* Code generator helper for multi-composite port.

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
package ptolemy.codegen.c.actor.lib.hoc;

import ptolemy.codegen.c.actor.IOPort;

/**
Code generator helper.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 7.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
*/
public class MultiCompositePort extends IOPort {

    /** Construct the code generator helper associated
     *  with the given RefinementPort.
     *  @param component The associated component.
     */
    public MultiCompositePort(ptolemy.actor.lib.hoc.MultiCompositePort component) {
        super(component);
    }
}
