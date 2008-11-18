/* A factory that creates graph editing tableaux for NCCompositeActor's.

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
package ptolemy.domains.ptinyos.gui;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.domains.ptinyos.kernel.NCCompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.ActorGraphTableau;

//////////////////////////////////////////////////////////////////////////
//// NCCompositeActorTableauFactory

/**
 * A factory that creates graph editing tableaux for NCCompositeActor's.
 *
 * @author Elaine Cheong
 * @version $Id: NCCompositeActor.java,v 1.73 2004/04/13 05:12:39 cxh
 *          Exp $
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 * @since Ptolemy II 5.1
 * @see ptolemy.vergil.actor.TypeOpaqueCompositeActorTableauFactory
 */
public class NCCompositeActorTableauFactory extends ActorGraphTableau.Factory {
    /** Create an factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public NCCompositeActorTableauFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Create a tableau in the default workspace with no name for the
     *  given Effigy.  The tableau will created with a new unique name
     *  in the given model effigy.  If this factory cannot create a
     *  tableau for the given effigy (if it is not of type
     *  NCCompositeActor) then return null.  It is the responsibility
     *  of callers of this method to check the return value and call
     *  show().
     *
     *  @param effigy The model effigy.
     *  @return A new ActorGraphTableau, if the effigy is a
     *  PtolemyEffigy, or null otherwise.
     *  @exception Exception If an exception occurs when creating the
     *  tableau.
     */
    public Tableau createTableau(Effigy effigy) throws Exception {
        if (effigy instanceof PtolemyEffigy) {
            if (((PtolemyEffigy) effigy).getModel() instanceof NCCompositeActor) {
                return super.createTableau(effigy);
            }
        }

        return null;
    }
}
