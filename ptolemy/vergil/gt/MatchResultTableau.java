/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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
package ptolemy.vergil.gt;

import java.awt.Color;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.JVMTableau;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.TextEditorTableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.modal.CaseGraphTableau;
import ptolemy.vergil.tree.TreeTableau;

//////////////////////////////////////////////////////////////////////////
//// GTRuleGraphTableau

/** An editor tableau for graph transformation in Ptolemy II. FIXME: more

 @author  Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see CaseGraphTableau
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class MatchResultTableau extends Tableau {

    /** Create a new case editor tableau with the specified container
     *  and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the model associated with
     *   the container effigy is not an instance of Case.
     *  @exception NameDuplicationException If the container already
     *   contains an object with the specified name.
     */
    public MatchResultTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null);
    }

    /** Create a new case editor tableau with the specified container,
     *  name, and default library.
     *  @param container The container.
     *  @param name The name.
     *  @param defaultLibrary The default library, or null to not specify one.
     *  @exception IllegalActionException If the model associated with
     *   the container effigy is not an instance of Case.
     *  @exception NameDuplicationException If the container already
     *   contains an object with the specified name.
     */
    public MatchResultTableau(PtolemyEffigy container, String name,
            LibraryAttribute defaultLibrary) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();

        if (!(model instanceof CompositeEntity)) {
            throw new IllegalActionException(this,
                    "Cannot view a model that is not a CompositeEntity.");
        }

        createGraphFrame((CompositeEntity) model, defaultLibrary);
    }

    /** Create the graph frame that displays the model associated with
     *  this tableau. This method creates a GRRuleGraphFrame.
     *  @param model The Ptolemy II model to display in the graph frame.
     */
    public void createGraphFrame(CompositeEntity model) {
        createGraphFrame(model, null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the graph frame that displays the model associated with
     *  this tableau together with the specified library.
     *  This method creates a GRRuleGraphFrame. If a subclass
     *  uses another frame, this method should be overridden to create
     *  that frame.
     *  @param model The Ptolemy II model to display in the graph frame.
     *  @param defaultLibrary The default library, or null to not specify
     *   one.
     */
    public void createGraphFrame(CompositeEntity model,
            LibraryAttribute defaultLibrary) {
        MatchResultViewer frame = new MatchResultViewer(model, this,
                defaultLibrary);

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

            treeViewFactory = new TreeTableau.Factory(this, "Tree View");
            xmlViewFactory = new TextEditorTableau.Factory(this, "XML View");
            javaPropertiesFactory = new JVMTableau.Factory(this,
                    "JVM Properties");
        }

        /** Create an instance of GRRuleGraphTableau for the specified effigy,
         *  if it is an effigy for an instance of FSMActor.
         *  @param effigy The effigy for an FSMActor.
         *  @return A new GRRuleGraphTableau, if the effigy is a PtolemyEffigy
         *   that references an FSMActor, or null otherwise.
         *  @exception Exception If an exception occurs when creating the
         *   tableau.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (!(effigy instanceof PtolemyEffigy)) {
                return null;
            }

            effigy.setTableauFactory(this);

            NamedObj model = ((PtolemyEffigy) effigy).getModel();

            if (model instanceof CompositeEntity) {
                // Check to see whether this factory contains a
                // default library.
                LibraryAttribute library = (LibraryAttribute) getAttribute(
                        "_library", LibraryAttribute.class);

                MatchResultTableau tableau = new MatchResultTableau(
                        (PtolemyEffigy) effigy, effigy.uniqueName("tableau"),
                        library);
                return tableau;
            } else {
                return null;
            }
        }

        public TableauFactory javaPropertiesFactory;

        public TableauFactory treeViewFactory;

        public TableauFactory xmlViewFactory;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Background color. */
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

}
