/*

 Copyright (c) 1997-2008 The Regents of the University of California.
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

package ptolemy.vergil.ptera;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ptera.kernel.PteraController;
import ptolemy.domains.ptera.kernel.PteraModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.fsm.FSMGraphTableau;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PteraGraphTableau extends FSMGraphTableau {

    /**
     * @param container
     * @param name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public PteraGraphTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * @param container
     * @param name
     * @param defaultLibrary
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public PteraGraphTableau(PtolemyEffigy container, String name,
            LibraryAttribute defaultLibrary) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, defaultLibrary);
    }

    public void createGraphFrame(CompositeEntity model,
            LibraryAttribute defaultLibrary) {
        PteraGraphFrame frame = new PteraGraphFrame(model, this, defaultLibrary);

        try {
            setFrame(frame);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }

        frame.setBackground(BACKGROUND_COLOR);
        frame.pack();
        frame.centerOnScreen();
        frame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates graph editing tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create an factory with the given name and container.
         *  @param container The container.
         *  @param name The name of the entity.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create an instance of FSMGraphTableau for the specified effigy,
         *  if it is an effigy for an instance of FSMActor.
         *  @param effigy The effigy for an FSMActor.
         *  @return A new FSMGraphTableau, if the effigy is a PtolemyEffigy
         *   that references an FSMActor, or null otherwise.
         *  @exception Exception If an exception occurs when creating the
         *   tableau.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (!(effigy instanceof PtolemyEffigy)) {
                return null;
            }

            Tableau tableau = (Tableau) effigy.getEntity("ergGraphTableau");
            if (tableau != null) {
                return tableau;
            }

            NamedObj model = ((PtolemyEffigy) effigy).getModel();

            if (model instanceof PteraModalModel) {
                Configuration configuration = (Configuration) effigy.toplevel();
                PteraController controller = (PteraController)
                        ((PteraModalModel) model).getController();
                tableau = configuration.openModel(controller);
                tableau.setContainer(effigy);

                if (model.getContainer() == null) {
                    // Record the size in its controller.
                    Attribute windowsPropertiesAttribute = controller
                            .getAttribute("_windowProperties",
                                    WindowPropertiesAttribute.class);
                    if (windowsPropertiesAttribute != null) {
                        windowsPropertiesAttribute.setPersistent(true);
                    }

                    Attribute sizeAttribute = controller.getAttribute(
                            "_vergilSize", SizeAttribute.class);
                    if (sizeAttribute != null) {
                        sizeAttribute.setPersistent(true);
                    }

                    Attribute zoomFactorAttribute = controller.getAttribute(
                            "_vergilZoomFactor", Parameter.class);
                    if (zoomFactorAttribute != null) {
                        zoomFactorAttribute.setPersistent(true);
                    }

                    Attribute centerAttribute = controller.getAttribute(
                            "_vergilCenter", Parameter.class);
                    if (centerAttribute != null) {
                        centerAttribute.setPersistent(true);
                    }
                }
                return tableau;
            } else if (model instanceof PteraController) {
                // Check to see whether this factory contains a
                // default library.
                LibraryAttribute library = (LibraryAttribute) getAttribute(
                        "_library", LibraryAttribute.class);

                tableau = new PteraGraphTableau((PtolemyEffigy) effigy,
                        "ergGraphTableau", library);
                return tableau;
            } else {
                return null;
            }
        }
    }
}
