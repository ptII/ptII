/* The node controller for states.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.vergil.modal;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.KeyStroke;

import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.NodeRenderer;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// StateController

/**
 This class provides interaction with nodes that represent states in an
 FSM graph.  It provides a double click binding to edit the parameters
 of the state, and a context menu containing a commands to edit parameters
 ("Configure"), rename, get documentation, and look inside.  The looks
 inside command opens the refinement of the state, if it exists.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class StateController extends AttributeController {

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public StateController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public StateController(GraphController controller, Access access) {
        super(controller, access);

        setNodeRenderer(new StateRenderer(controller.getGraphModel()));

        _appearanceMenuActionFactory.addAction(_editIconAction);
        _appearanceMenuActionFactory.addAction(_removeIconAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     Render the state as a circle.

     @version $Id$
     */
    public static class StateRenderer implements NodeRenderer {
        /** Construct a state renderer.
         *  @param model The GraphModel.
         */
        public StateRenderer(GraphModel model) {
            super();
            _model = model;
        }

        /** Render an object.
         *  @param n The object to be rendered.  This object should
         *  of type Locatable.
         *  @return A Figure.
         */
        public Figure render(Object n) {
            Locatable location = (Locatable) n;
            final NamedObj object = location.getContainer();
            EditorIcon icon;

            try {
                // In theory, there shouldn't be more than one
                // icon, but if there are, use the last one.
                List icons = object.attributeList(EditorIcon.class);

                // Check to see whether there is an icon that has been created,
                // but not inserted.
                if (icons.size() == 0) {
                    XMLIcon alreadyCreated = (XMLIcon) _iconsPendingContainer
                            .get(object);

                    if (alreadyCreated != null) {
                        icons.add(alreadyCreated);
                    }
                }

                if (icons.size() > 0) {
                    icon = (EditorIcon) icons.get(icons.size() - 1);
                } else {
                    // NOTE: This code is the same as in
                    // IconController.IconRenderer.
                    // NOTE: This used to directly create an XMLIcon within
                    // the container "object". However, this is not cosher,
                    // since we may not be able to get write access on the
                    // workspace. We instead use a hack supported by XMLIcon
                    // to create an XMLIcon with no container (this does not
                    // require write access to the workspace), and specify
                    // to it what the container will eventually be. Then
                    // we queue a change request to make that the container.
                    // Further, we have to make a record of the figure, indexed
                    // by the object, in case some other change request is
                    // executed before this gets around to setting the
                    // container.  Otherwise, that second change request
                    // will result in the creation of a second figure.
                    icon = new XMLIcon(object.workspace(), "_icon");
                    icon.setContainerToBe(object);
                    icon.setPersistent(false);

                    // NOTE: Make sure this is done before the change request
                    // below is executed, which may be as early as when it is
                    // requested.
                    _iconsPendingContainer.put(object, icon);

                    // NOTE: Make sure the source of this change request is
                    // the graph model. Otherwise, this change request will
                    // trigger a redraw of the entire graph, which will result
                    // in another call to this very same method, which will
                    // result in creation of yet another figure before this
                    // method even returns!
                    final EditorIcon finalIcon = icon;
                    ChangeRequest request = new ChangeRequest(_model,
                            "Set the container of a new XMLIcon.") {
                        // NOTE: The KernelException should not be thrown,
                        // but if it is, it will be handled properly.
                        protected void _execute() throws KernelException {
                            _iconsPendingContainer.remove(object);

                            // If the icon already has a container, do nothing.
                            if (finalIcon.getContainer() != null) {
                                return;
                            }

                            // If the container already has an icon, do nothing.
                            if (object.getAttribute("_icon") != null) {
                                return;
                            }

                            finalIcon.setContainer(object);
                        }
                    };

                    request.setPersistent(false);
                    object.requestChange(request);
                }
            } catch (KernelException ex) {
                throw new InternalErrorException("could not create icon "
                        + "in " + object + " even "
                        + "though one did not exist");
            }

            Figure figure = icon.createFigure();
            figure.setToolTipText(object.getName());
            
            // New way to specify a highlight color.
            try {
                ColorAttribute highlightAttribute = (ColorAttribute) (object
                        .getAttribute("_highlightColor", ColorAttribute.class));
                if (highlightAttribute != null
                        && !highlightAttribute.getExpression().trim()
                                .equals("")) {
                    Color color = highlightAttribute.asColor();
                    AnimationRenderer animationRenderer = new AnimationRenderer(
                            color);
                    animationRenderer.renderSelected(figure);
                }
            } catch (IllegalActionException e) {
                // Ignore.
            }
            
            return figure;
        }

        private GraphModel _model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The edit custom icon action. */
    protected EditIconAction _editIconAction = new EditIconAction();

    /** The remove custom icon action. */
    protected RemoveIconAction _removeIconAction = new RemoveIconAction();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An action to look inside a state at its refinement, if it has one.
     *  NOTE: This requires that the configuration be non null, or it
     *  will report an error with a fairly cryptic message.
     */
    protected class LookInsideAction extends FigureAction {
        public LookInsideAction() {
            super("Look Inside");

            // If we are in an applet, so Control-L or Command-L will
            // be caught by the browser as "Open Location", so we don't
            // supply Control-L or Command-L as a shortcut under applets.
            if (!StringUtilities.inApplet()) {
                // For some inexplicable reason, the I key doesn't work here.
                // So we use L.
                putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                                KeyEvent.VK_L, Toolkit.getDefaultToolkit()
                                .getMenuShortcutKeyMask()));
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler
                        .error("Cannot look inside without a configuration.");
                return;
            }

            super.actionPerformed(e);

            NamedObj target = getTarget();

            // If the target is not an instance of State, do nothing.
            if (target instanceof State) {
                try {
                    TypedActor[] refinements = ((State) target).getRefinement();

                    if ((refinements != null) && (refinements.length > 0)) {
                        for (int i = 0; i < refinements.length; i++) {
                            // Open each refinement.
                            _configuration
                                    .openInstance((NamedObj) refinements[i]);
                        }
                    } else {
                        MessageHandler.error("State has no refinement.");
                    }
                } catch (Exception ex) {
                    MessageHandler.error("Look inside failed: ", ex);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Map used to keep track of icons that have been created
     *  but not yet assigned to a container.
     */
    private static Map _iconsPendingContainer = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     Customize the icon of the state.

     @version $Id$
     */
    private class EditIconAction extends FigureAction {

        /** Create an action to edit an icon. */
        public EditIconAction() {
            super("Edit Custom Icon");
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Process the edit icon command.
         *  @param e The event.
         */
        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler.error("Cannot edit icon without a "
                        + "configuration.");
                return;
            }

            // Determine which entity was selected for the action.
            super.actionPerformed(e);

            final NamedObj object = getTarget();

            // Do this as a change request since it may add a new icon.
            ChangeRequest request = new ChangeRequest(this, "Edit Custom Icon") {
                protected void _execute() throws Exception {
                    EditorIcon icon = null;
                    List<EditorIcon> iconList = object
                            .attributeList(EditorIcon.class);
                    for (EditorIcon oldIcon : iconList) {
                        if (oldIcon.getClass().equals(EditorIcon.class)) {
                            icon = oldIcon;
                            break;
                        }
                    }

                    if (icon == null) {
                        icon = new EditorIcon(object, "_icon");
                        Parameter hideName = (Parameter) object
                                .getAttribute("_hideName");
                        if (((BooleanToken) hideName.getToken()).booleanValue()) {
                            hideName.setToken(BooleanToken.FALSE);
                        }
                    }

                    _configuration.openModel(icon);
                }
            };

            object.requestChange(request);
        }
    }

    /** Action to remove a custom icon.

    @version $Id$
    */
    private static class RemoveIconAction extends FigureAction {
        public RemoveIconAction() {
            super("Remove Custom Icon");
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Process the remove icon command.
         *  @param e The event.
         */
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj object = getTarget();
            EditorIcon icon = null;
            List<EditorIcon> iconList = object.attributeList(EditorIcon.class);
            for (EditorIcon oldIcon : iconList) {
                if (oldIcon.getClass().equals(EditorIcon.class)) {
                    icon = oldIcon;
                    break;
                }
            }

            if (icon != null) {
                String moml = "<deleteProperty name=\"" + icon.getName()
                        + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, object,
                        moml);
                object.requestChange(request);
            }
        }
    }
}
