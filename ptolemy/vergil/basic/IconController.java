/* The node controller for objects with icons.

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
package ptolemy.vergil.basic;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;
import ptolemy.vergil.kernel.AnimationRenderer;
import diva.canvas.Figure;
import diva.canvas.toolbox.SVGUtilities;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.NodeRenderer;

//////////////////////////////////////////////////////////////////////////
//// IconController

/**
 This class provides interaction with nodes that represent Ptolemy II
 objects that are represented on screen as icons, such as attributes
 and entities.   It provides a double click binding to edit the parameters
 of the node, and a context menu containing a command to edit parameters
 ("Configure"). This adds to the base class the ability to render an
 icon for the object being controlled, where the icon is specified
 by a contained attribute of class EditorIcon (typically, but not
 necessarily named "_icon").

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class IconController extends ParameterizedNodeController {
    /** Create a controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public IconController(GraphController controller) {
        super(controller);
        setNodeRenderer(new IconRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Map used to keep track of icons that have been created
     *  but not yet assigned to a container.
     */
    private static Map _iconsPendingContainer = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An icon renderer. */
    public class IconRenderer implements NodeRenderer {
        /**  Render a visual representation of the given node. If the
         * StringAttribute _color of the node is set then use that color to
         * highlight the node. If the StringAttribute _explanation of the node
         * is set then use it to set the tooltip.
         * @see diva.graph.NodeRenderer#render(java.lang.Object)
         */
        public Figure render(Object n) {
            Locatable location = (Locatable) n;
            final NamedObj object = location.getContainer();

            // NOTE: this code is similar to that in PtolemyTreeCellRenderer
            Figure result = null;

            try {
                List iconList = object.attributeList(EditorIcon.class);

                // Check to see whether there is an icon that has been created,
                // but not inserted.
                if (iconList.size() == 0) {
                    XMLIcon alreadyCreated = (XMLIcon) _iconsPendingContainer
                            .get(object);

                    if (alreadyCreated != null) {
                        iconList.add(alreadyCreated);
                    }
                }

                // If there are still no icons, then we need to create one.
                if (iconList.size() == 0) {
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

                    final EditorIcon icon = XMLIcon.getXMLIcon(object
                            .workspace(), "_icon");
                    icon.setContainerToBe(object);
                    icon.setPersistent(false);
                    result = icon.createFigure();

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
                    GraphController controller = IconController.this
                            .getController();
                    GraphModel graphModel = controller.getGraphModel();
                    ChangeRequest request = new ChangeRequest(graphModel,
                            "Set the container of a new XMLIcon.") {
                        // NOTE: The KernelException should not be thrown,
                        // but if it is, it will be handled properly.
                        protected void _execute() throws KernelException {
                            _iconsPendingContainer.remove(object);

                            // If the icon already has a container, do nothing.
                            if (icon.getContainer() != null) {
                                return;
                            }

                            // If the container already has an icon, do nothing.
                            if (object.getAttribute("_icon") != null) {
                                return;
                            }

                            icon.setContainer(object);
                        }
                    };

                    request.setPersistent(false);
                    object.requestChange(request);
                } else if (iconList.size() >= 1) {
                    // Use only the last icon in the list.
                    EditorIcon icon = (EditorIcon) iconList
                            .get(iconList.size() - 1);
                    result = icon.createFigure();
                }
            } catch (KernelException ex) {
                throw new InternalErrorException(null, ex,
                        "Could not create icon " + "in " + object + " even "
                                + "though one did not previously exist.");
            }

            if (result == null) {
                throw new InternalErrorException("Failed to create icon.");
            } else {
                result.setToolTipText(object.getClassName());
            }

            // Check to see if it has
            // attributes that specify its color or an explanation.
            // Old way to specify a color.
            try {
                StringAttribute colorAttr = (StringAttribute) (object
                        .getAttribute("_color", StringAttribute.class));
                if (colorAttr != null) {
                    String color = colorAttr.getExpression();
                    AnimationRenderer animationRenderer = new AnimationRenderer(
                            SVGUtilities.getColor(color));
                    animationRenderer.renderSelected(result);
                }
            } catch (IllegalActionException e) {
                // Ignore
            }
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
                    animationRenderer.renderSelected(result);
                }
            } catch (IllegalActionException e) {
                // Ignore.
            }
            try {
                StringAttribute explanationAttribute = (StringAttribute) (object
                        .getAttribute("_explanation", StringAttribute.class));
                if (explanationAttribute != null) {
                    result.setToolTipText(explanationAttribute.getExpression());
                }
            } catch (IllegalActionException e) {
                // Ignore.
            }

            return result;
        }
    }
}
