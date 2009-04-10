/* A drag interactor for locatable nodes

 Copyright (c) 1998-2007 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLUndoEntry;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.SelectionModel;
import diva.graph.NodeDragInteractor;

//////////////////////////////////////////////////////////////////////////
//// LocatableNodeDragInteractor

/**
 An interaction role that drags nodes that have locatable objects
 as semantic objects.  When the node is dragged, this interactor
 updates the location in the locatable object with the new location of the
 figure.
 <p>
 The dragging of a selection is undoable, and is based on the difference
 between the point where the mouse was pressed and where the mouse was
 released. This information is used to create MoML to undo the move if
 requested.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class LocatableNodeDragInteractor extends NodeDragInteractor {
    /** Create a new interactor contained within the given controller.
     *  @param controller The controller.
     */
    public LocatableNodeDragInteractor(LocatableNodeController controller) {
        super(controller.getController());
        _controller = controller;

        // Create a snap constraint with the default snap resolution.
        _snapConstraint = new SnapConstraint();
        appendConstraint(_snapConstraint);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** When the mouse is pressed before dragging, store a copy of the
     *  pressed point location so that a relative move can be
     *  evaluated for undo purposes.
     *  @param  e  The press event.
     */
    public void mousePressed(LayerEvent e) {
        super.mousePressed(e);
        _dragStart = _getConstrainedPoint(e);
    }

    /** When the mouse is released after dragging, mark the frame modified
     *  and update the panner, and generate an undo entry for the move.
     *  If no movement has occurred, then do nothing.
     *  @param e The release event.
     */
    public void mouseReleased(LayerEvent e) {

        // We should factor out the common code in this method and in
        // transform().
        // Work out the transform the drag performed.
        double[] dragEnd = _getConstrainedPoint(e);
        double[] transform = new double[2];
        transform[0] = _dragStart[0] - dragEnd[0];
        transform[1] = _dragStart[1] - dragEnd[1];

        if ((transform[0] == 0.0) && (transform[1] == 0.0)) {
            return;
        }

        BasicGraphController graphController = (BasicGraphController) _controller
                .getController();
        BasicGraphFrame frame = graphController.getFrame();

        SelectionModel model = graphController.getSelectionModel();
        AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) graphController
                .getGraphModel();
        Object[] selection = model.getSelectionAsArray();
        Object[] userObjects = new Object[selection.length];

        // First get the user objects from the selection.
        for (int i = 0; i < selection.length; i++) {
            userObjects[i] = ((Figure) selection[i]).getUserObject();
        }

        // First make a set of all the semantic objects as they may
        // appear more than once
        HashSet<NamedObj> namedObjSet = new HashSet<NamedObj>();

        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof Figure) {
                Object userObject = ((Figure) selection[i]).getUserObject();

                if (graphModel.isEdge(userObject)
                        || graphModel.isNode(userObject)) {
                    NamedObj actual = (NamedObj) graphModel
                            .getSemanticObject(userObject);

                    if (actual != null) {
                        namedObjSet.add(actual);
                    } else {
                        // Special case, may need to handle by not going to
                        // MoML and which may not be undoable.
                        // FIXME: This is no way to handle it...
                        System.out
                                .println("Object with no semantic object , class: "
                                        + userObject.getClass().getName());
                    }
                }
            }
        }

        // Generate the MoML to carry out move.
        // Note that the location has already been set by the mouseMoved()
        // call, but we need to do this so that the undo is generated and
        // so that the change propagates.
        // The toplevel is the container being edited.
        final NamedObj toplevel = (NamedObj) graphModel.getRoot();

        StringBuffer moml = new StringBuffer();
        StringBuffer undoMoml = new StringBuffer();
        moml.append("<group>\n");
        undoMoml.append("<group>\n");

        for (NamedObj element : namedObjSet) {
            List<?> locationList = element.attributeList(Locatable.class);

            if (locationList.isEmpty()) {
                // Nothing to do as there was no previous location
                // attribute (applies to "unseen" relations)
                continue;
            }

            // Set the new location attribute.
            Locatable locatable = (Locatable) locationList.get(0);

            // Give default values in case the previous locations value
            // has not yet been set
            double[] newLocation = new double[] { 0, 0 };

            if (locatable.getLocation() != null) {
                newLocation = locatable.getLocation();
            }

            // NOTE: we use the transform worked out for the drag to
            // set the original MoML location
            double[] oldLocation = new double[2];
            oldLocation[0] = newLocation[0] + transform[0];
            oldLocation[1] = newLocation[1] + transform[1];

            // Create the MoML, wrapping the new location attribute
            // in an element referring to the container
            String containingElementName = element.getElementName();
            String elementToMove = "<" + containingElementName + " name=\""
                    + element.getName() + "\" >\n";
            moml.append(elementToMove);
            undoMoml.append(elementToMove);

            // NOTE: use the moml info element name here in case the
            // location is a vertex
            String momlInfo = ((NamedObj) locatable).getElementName();
            moml.append("<" + momlInfo + " name=\"" + locatable.getName()
                    + "\" value=\"[" + newLocation[0] + ", " + newLocation[1]
                    + "]\" />\n");
            undoMoml.append("<" + momlInfo + " name=\"" + locatable.getName()
                    + "\" value=\"[" + oldLocation[0] + ", " + oldLocation[1]
                    + "]\" />\n");
            moml.append("</" + containingElementName + ">\n");
            undoMoml.append("</" + containingElementName + ">\n");
        }

        moml.append("</group>\n");
        undoMoml.append("</group>\n");

        final String finalUndoMoML = undoMoml.toString();

        // Request the change.
        MoMLChangeRequest request = new MoMLChangeRequest(this, toplevel, moml
                .toString()) {
            protected void _execute() throws Exception {
                super._execute();

                // Next create and register the undo entry;
                // The MoML by itself will not cause an undo
                // to register because the value is not changing.
                // Note that this must be done inside the change
                // request because write permission on the
                // workspace is required to push an entry
                // on the undo stack. If this is done outside
                // the change request, there is a race condition
                // on the undo, and a deadlock could result if
                // the model is running.
                MoMLUndoEntry newEntry = new MoMLUndoEntry(toplevel,
                        finalUndoMoML);
                UndoStackAttribute undoInfo = UndoStackAttribute
                        .getUndoInfo(toplevel);
                undoInfo.push(newEntry);
            }
        };

        toplevel.requestChange(request);

        if (frame != null) {
            // NOTE: Use changeExecuted rather than directly calling
            // setModified() so that the panner is also updated.
            frame.changeExecuted(null);
        }
    }

    /** Specify the snap resolution. The default snap resolution is 5.0.
     *  @param resolution The snap resolution.
     */
    public void setSnapResolution(double resolution) {
        _snapConstraint.setResolution(resolution);
    }

    /** Drag all selected nodes and move any attached edges.
     *  Update the locatable nodes with the current location.
     *  @param e The event triggering this translation.
     *  @param x The horizontal delta.
     *  @param y The vertical delta.
     */
    public void translate(LayerEvent e, double x, double y) {
        // NOTE: To get snap to grid to work right, we have to do some work.
        // It is not sufficient to quantize the translation.  What we do is
        // find the location of the first locatable node in the selection,
        // and find a translation for it that will lead to an acceptable
        // quantized position.  Then we use that translation.  This does
        // not ensure that all nodes in the selection get to an acceptable
        // quantized point, but there is no way to do that without
        // changing their relative positions.
        // NOTE: We cannot use the location attribute of the target objects
        // The problem is that the location as set during a drag is a
        // queued mutation.  So the translation we get isn't right.
        Iterator<?> targets = targets();
        double[] originalUpperLeft = null;

        while (targets.hasNext()) {
            Figure figure = (Figure) targets.next();
            originalUpperLeft = new double[2];
            originalUpperLeft[0] = figure.getOrigin().getX();
            originalUpperLeft[1] = figure.getOrigin().getY();

            // Only snap the first figure in the set.
            break;
        }

        double[] snapTranslation;

        if (originalUpperLeft == null) {
            // No location found in the selection, so we just quantize
            // the translation.
            double[] oldTranslation = new double[2];
            oldTranslation[0] = x;
            oldTranslation[1] = y;
            snapTranslation = _snapConstraint.constrain(oldTranslation);
        } else {
            double[] newUpperLeft = new double[2];
            newUpperLeft[0] = originalUpperLeft[0] + x;
            newUpperLeft[1] = originalUpperLeft[1] + y;

            double[] snapLocation = _snapConstraint.constrain(newUpperLeft);
            snapTranslation = new double[2];
            snapTranslation[0] = snapLocation[0] - originalUpperLeft[0];
            snapTranslation[1] = snapLocation[1] - originalUpperLeft[1];
        }

        // NOTE: The following seems no longer necessary, since the
        // translation occurs as a consequence of setting the location
        // attribute. However, for reasons that I don't understand,
        // without this, drag doesn't work.  The new location ends
        // up identical to the old because of the snap, so no translation
        // occurs.  Oddly, the superclass call performs a translation
        // even if the snapTranslation is zero.  Beats me.  EAL 7/31/02.
        super.translate(e, snapTranslation[0], snapTranslation[1]);

        // Set the location attribute of each item that is translated.
        // NOTE: this works only because all the nodes that allow
        // dragging are location nodes.  If nodes can be dragged that
        // aren't locatable nodes, then they shouldn't be able to be
        // selected at the same time as a locatable node.
        try {
            targets = targets();

            while (targets.hasNext()) {
                Figure figure = (Figure) targets.next();
                Object node = figure.getUserObject();

                if (_controller.getController().getGraphModel().isNode(node)) {
                    // NOTE: This used to get the location and then set it,
                    // but since the returned value is the internal array,
                    // then setLocation() believed there was no change,
                    // so the change would not be persistent.
                    double[] location = new double[2];
                    location[0] = figure.getOrigin().getX();
                    location[1] = figure.getOrigin().getY();
                    _controller.setLocation(node, location);
                }
            }
        } catch (IllegalActionException ex) {
            MessageHandler.error("could not set location", ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Returns a constrained point from the given event
    private double[] _getConstrainedPoint(LayerEvent e) {
        Iterator<?> targets = targets();
        double[] result = new double[2];

        if (targets.hasNext()) {
            //Figure figure = (Figure) targets.next();

            // The transform context is always (0,0) so no use
            // NOTE: this is a bit of hack, needed to allow the undo of
            // the movement of vertexes by themselves
            result[0] = e.getLayerX();
            result[1] = e.getLayerY();
            return _snapConstraint.constrain(result);
        }

        /*
         * else {
         * AffineTransform transform
         * = figure.getTransformContext().getTransform();
         * result[0] = transform.getTranslateX();
         * result[1] = transform.getTranslateY();
         * }
         * Only snap the first figure in the set.
         * break;
         */
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private LocatableNodeController _controller;

    // Used to undo a locatable node movement
    private double[] _dragStart;

    // Locally defined snap constraint.
    private SnapConstraint _snapConstraint;
}
