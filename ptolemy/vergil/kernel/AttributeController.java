/* The node controller for icons of attributes

 Copyright (c) 1998-2009 The Regents of the University of California.
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
package ptolemy.vergil.kernel;

import javax.swing.Action;

import ptolemy.actor.gui.Configuration;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.CustomizeDocumentationAction;
import ptolemy.vergil.basic.GetDocumentationAction;
import ptolemy.vergil.basic.IconController;
import ptolemy.vergil.basic.RemoveCustomDocumentationAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MoveAction;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// AttributeController

/**
 This class provides interaction with nodes that represent Ptolemy II
 attributes.  It provides a double click binding and context menu
 entry to edit the parameters of the node ("Configure") and a
 command to get documentation.
 It can have one of two access levels, FULL or PARTIAL.
 If the access level is FULL, the the context menu also
 contains a command to rename the node.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class AttributeController extends IconController {
    /** Create an attribute controller associated with the specified graph
     *  controller.  The attribute controller is given full access.
     *  @param controller The associated graph controller.
     */
    public AttributeController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create an attribute controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public AttributeController(GraphController controller, Access access) {
        super(controller);

        if (access == FULL) {
            // Add to the context menu, configure submenu.
            _renameAction = new RenameDialogAction("Rename");
            _configureMenuFactory.addAction(_renameAction, "Customize");

            Action[] actions = { _getDocumentationAction,
                    new CustomizeDocumentationAction(),
                    new RemoveCustomDocumentationAction() };
            _menuFactory.addMenuItemFactory(new MenuActionFactory(actions,
                    "Documentation"));

            // Note that we also have "Send to Back" and "Bring to Front" in
            // vergil/basic/BasicGraphFrame.java
            Action[] appearanceActions = {
                    new MoveAction("Send to Back", MoveAction.TO_FIRST),
                    new MoveAction("Bring to Front", MoveAction.TO_LAST) };
            _appearanceMenuActionFactory = new MenuActionFactory(
                    appearanceActions, "Appearance");
            _menuFactory.addMenuItemFactory(_appearanceMenuActionFactory);

            _listenToAction = new ListenToAction(
                    (BasicGraphController) getController(), _getComponentType());
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    _listenToAction));
            _listenToAction.setConfiguration(_configuration);

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add hot keys to the actions in the given JGraph.
    *   It would be better that this method was added higher in the hierarchy. Now
    *   most controllers
    *  @param jgraph The JGraph to which hot keys are to be added.
    */
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _renameAction);
    }

    /** Set the configuration.  This is used in derived classes to
     *  to open files (such as documentation).  The configuration is
     *  is important because it keeps track of which files are already
     *  open and ensures that there is only one editor operating on the
     *  file at any one time.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _getDocumentationAction.setConfiguration(configuration);
        _listenToAction.setConfiguration(_configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Get the class label of the component.
     * @return the class label of the component.
     */
    protected String _getComponentType() {
        return "Attribute";
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public members                        ////

    /** Indicator to give full access to the attribute. */
    public static final Access FULL = new Access();

    /** Indicator to give partial access to the attribute. */
    public static final Access PARTIAL = new Access();

    ///////////////////////////////////////////////////////////////////
    ////                     protected members                     ////

    /** The appearance menu factory. */
    protected MenuActionFactory _appearanceMenuActionFactory;

    /** Action to launch rename dialog. */
    protected RenameDialogAction _renameAction;

    /** Action to listen to debug messages. */
    protected ListenToAction _listenToAction;

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    /** The "get documentation" action. */
    private GetDocumentationAction _getDocumentationAction = new GetDocumentationAction();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A static enumerator for constructor arguments. */
    protected static class Access {
    }

}
