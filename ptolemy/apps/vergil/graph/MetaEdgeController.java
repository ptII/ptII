/*
 * $Id$
 *
 * Copyright (c) 1998-2005 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package ptolemy.apps.vergil.graph;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import diva.canvas.CanvasComponent;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.Site;
import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.CenterSite;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorListener;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.connector.Terminal;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.BasicSelectionRenderer;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.graph.*;
import diva.util.Filter;

import java.awt.event.InputEvent;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * A basic implementation of GraphController, which works with
 * simple graphs that have edges connecting simple nodes. It
 * sets up some simple interaction on its view's pane.
 *
 * @author         Michael Shilman
 * @version        $Revision$
 * @Pt.AcceptedRating      Red
 */
public class MetaEdgeController extends CompositeEntity
    implements EdgeController {
    /** The selection interactor for drag-selecting nodes
     */
    private SelectionDragger _selectionDragger;

    /** The connector target
     */
    private ConnectorTarget _connectorTarget;

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter(InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /**
     * Create a new edge controller with basic interaction.  Specifically,
     * this method creates an edge interactor and initializes its menipulator
     * so that edges get attached appropriately.  Furthermore, the edge
     * interactor is initialized with the selection model of the graph
     * controller.  The manipulator is activated by either a regular click
     * or a control click.  Also initialize a basic connector target that
     * generally attaches to the perimeter of nodes, except that it is
     * smart enough to properly handle terminals.
     */
    public MetaEdgeController(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        //SelectionModel sm = getController().getSelectionModel();
        setEdgeInteractor(new MetaInteractor(this, "_interactor"));

        // Create and set up the manipulator for connectors
        //       ConnectorManipulator manipulator = new ConnectorManipulator();
        //manipulator.setSnapHalo(4.0);
        //manipulator.addConnectorListener(new EdgeDropper());
        //getEdgeInteractor().setPrototypeDecorator(manipulator);
        // The mouse filter needs to accept regular click or control click
        //  MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        //manipulator.setHandleFilter(handleFilter);
        // Create and set up the target for connectors

        /*    PerimeterTarget ct = new PerimeterTarget() {
        // Accept the head if the model graph model allows it.
        public boolean acceptHead (Connector c, Figure f) {
        Object node = f.getUserObject();
        Object edge = c.getUserObject();
        MutableGraphModel model =
        (MutableGraphModel)getController().getGraphModel();
        if (model.isNode(node) &&
        model.isEdge(edge) &&
        model.acceptHead(edge, node)) {
        return super.acceptHead(c, f);
        } else return false;
        }

        // Accept the tail if the model graph model allows it.
        public boolean acceptTail (Connector c, Figure f) {
        Object node = f.getUserObject();
        Object edge = c.getUserObject();
        MutableGraphModel model =
        (MutableGraphModel)getController().getGraphModel();
        if (model.isNode(node) &&
        model.isEdge(edge) &&
        model.acceptTail(edge, node)) {
        return super.acceptTail(c, f);
        } else return false;
        }

        // If we have any terminals, then return the connection
        //  site of the terminal instead of a new perimeter site.
        public Site getHeadSite(Figure f, double x, double y) {
        if (f instanceof Terminal) {
        Site site = ((Terminal)f).getConnectSite();
        return site;
        } else {
        return super.getHeadSite(f, x, y);
        }
        }
        };
        setConnectorTarget(ct);
        */
    }

    /** Add an edge to this graph editor and render it
     * from the given tail node to an autonomous site at the
     * given location. Give the new edge the given semanticObject.
     * The "end" flag is either HEAD_END
     * or TAIL_END, from diva.canvas.connector.ConnectorEvent.
     * @return The new edge.
     * @exception GraphException If the connector target cannot return a
     * valid site on the node's figure.
     */
    public void addEdge(Object edge, Object node, int end, double x, double y) {
        MutableGraphModel model = (MutableGraphModel) getController()
                                                                  .getGraphModel();
        Figure nf = getController().getFigure(node);
        FigureLayer layer = getController().getGraphPane().getForegroundLayer();
        Site headSite;
        Site tailSite;

        // Temporary sites.  One of these will get blown away later.
        headSite = new AutonomousSite(layer, x, y);
        tailSite = new AutonomousSite(layer, x, y);

        // Render the edge.
        Connector c = render(edge, layer, tailSite, headSite);

        try {
            //Attach the appropriate end of the edge to the node.
            if (end == ConnectorEvent.TAIL_END) {
                tailSite = getConnectorTarget().getTailSite(c, nf, x, y);

                if (tailSite == null) {
                    throw new RuntimeException("Invalid connector target: "
                        + "no valid site found for tail of new connector.");
                }

                model.setEdgeTail(getController(), edge, node);
                c.setTailSite(tailSite);
            } else {
                headSite = getConnectorTarget().getHeadSite(c, nf, x, y);

                if (headSite == null) {
                    throw new RuntimeException("Invalid connector target: "
                        + "no valid site found for head of new connector.");
                }

                model.setEdgeHead(getController(), edge, node);
                c.setHeadSite(headSite);
            }
        } catch (GraphException ex) {
            // If an error happened then blow away the edge, and rethrow
            // the exception
            removeEdge(edge);
            throw ex;
        }
    }

    /**
     * Add an edge to this graph between the given tail and head
     * nodes.  Give the new edge the given semanticObject.
     */
    public void addEdge(Object edge, Object tail, Object head) {
        // Connect the edge
        MutableGraphModel model = (MutableGraphModel) getController()
                                                                  .getGraphModel();
        model.connectEdge(getController(), edge, tail, head);

        drawEdge(edge);
    }

    /**
     * Remove the figure for the given edge, but do not remove the
     * edge from the graph model.
     */
    public void clearEdge(Object edge) {
        Figure f = getController().getFigure(edge);

        if (f != null) {
            CanvasComponent container = f.getParent();
            f.setUserObject(null);
            getController().setFigure(edge, null);

            if (container instanceof FigureLayer) {
                ((FigureLayer) container).remove(f);
            } else if (container instanceof CompositeFigure) {
                ((CompositeFigure) container).remove(f);
            }
        }
    }

    /**
     * Draw the edge and add it to the layer, establishing
     * a two-way correspondence between the model and the
     * view.  If the edge already has been associated with some figure in
     * the view, then use any information in that figure to help draw the
     * edge.
     */
    public Figure drawEdge(Object edge) {
        GraphModel model = getController().getGraphModel();
        FigureLayer layer = getController().getGraphPane().getForegroundLayer();
        Object tail = model.getTail(edge);
        Object head = model.getHead(edge);

        Connector connector = (Connector) getController().getFigure(edge);
        Figure tailFigure = getController().getFigure(tail);
        Figure headFigure = getController().getFigure(head);

        Site tailSite;
        Site headSite;

        // If the tail is not attached,
        if (tailFigure == null) {
            // Then try to find the old tail site.
            if (connector != null) {
                tailSite = connector.getTailSite();
            } else {
                // FIXME try to manufacture a site.
                //throw new RuntimeException("drawEdge failed: could not find" +
                //                           " a tail site.");
                return null;
            }
        } else {
            // Get a new tail site based on the tail figure.
            Rectangle2D bounds = tailFigure.getBounds();
            tailSite = getConnectorTarget().getTailSite(tailFigure,
                    bounds.getCenterX(), bounds.getCenterY());
        }

        // If the head is not attached,
        if (headFigure == null) {
            // Then try to find the old head site.
            if (connector != null) {
                headSite = connector.getHeadSite();
            } else {
                // FIXME try to manufacture a site.
                //throw new RuntimeException("drawEdge failed: could not find" +
                //                           " a head site.");
                return null;
            }
        } else {
            // Get a new head site based on the head figure.
            Rectangle2D bounds = headFigure.getBounds();
            headSite = getConnectorTarget().getHeadSite(headFigure,
                    bounds.getCenterX(), bounds.getCenterY());
        }

        // If we did have an old figure, throw it away.
        if (connector != null) {
            clearEdge(edge);
        }

        // Create the figure
        Connector c = render(edge, layer, tailSite, headSite);
        getController().dispatch(new GraphViewEvent(this,
                GraphViewEvent.EDGE_DRAWN, edge));
        return c;
    }

    /**
     * Get the target used to find sites on nodes to connect to.
     */
    public ConnectorTarget getConnectorTarget() {
        return _connectorTarget;
    }

    /**
     * Get the graph controller that this controller is contained in.
     */
    public GraphController getController() {
        return (GraphController) getContainer();
    }

    /**
     * Get the interactor given to edge figures.
     */
    public Interactor getEdgeInteractor() {
        List list = entityList(Interactor.class);

        if (list.size() > 0) {
            return (Interactor) list.get(0);
        }

        return null;
    }

    /**
     * Return the edge renderer for this view.
     */
    public EdgeRenderer getEdgeRenderer() {
        List list = entityList(NodeRenderer.class);

        if (list.size() > 0) {
            return (EdgeRenderer) list.get(0);
        }

        return null;
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method of the superclass.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected void initializeInteraction() {
        GraphPane pane = getController().getGraphPane();
    }

    /**
     * Remove the edge.
     */
    public void removeEdge(Object edge) {
        clearEdge(edge);

        MutableGraphModel model = (MutableGraphModel) getController()
                                                                  .getGraphModel();
        model.setEdgeHead(getController(), edge, null);
        model.setEdgeTail(getController(), edge, null);
        getController().getGraphPane().repaint();
    }

    /**
     * Set the target used to find sites on nodes to connect to.  This
     * sets the local connector target (which is often used to find the
     * starting point of an edge) and the manipulator's connector target, which
     * is used after the connector is being dragged.
     */
    public void setConnectorTarget(ConnectorTarget t) {
        _connectorTarget = t;

        // FIXME: This is rather dangerous because it assumes a
        // basic selection renderer.

        /*        BasicSelectionRenderer selectionRenderer = (BasicSelectionRenderer)
                  getEdgeInteractor().getSelectionRenderer();
                  ConnectorManipulator manipulator = (ConnectorManipulator)
                  selectionRenderer.getDecorator();
                  manipulator.setConnectorTarget(t);
        */
    }

    /**
     * Set the interactor given to edge figures.
     */
    public void setEdgeInteractor(Interactor interactor) {
        if (!(interactor instanceof ComponentEntity)) {
            throw new RuntimeException("Interactor " + interactor
                + " is not a component entity");
        }

        try {
            Iterator iterator = entityList(Interactor.class).iterator();

            while (iterator.hasNext()) {
                ((ComponentEntity) iterator.next()).setContainer(null);
            }

            ((ComponentEntity) interactor).setContainer(this);
        } catch (KernelException ex) {
            throw new RuntimeException("Interactor " + interactor
                + " could not be set");
        }
    }

    /**
     * Set the edge renderer for this view.
     */
    public void setEdgeRenderer(EdgeRenderer renderer) {
        if (!(renderer instanceof ComponentEntity)) {
            throw new RuntimeException("Renderer " + renderer
                + " is not a component entity");
        }

        try {
            Iterator iterator = entityList(EdgeRenderer.class).iterator();

            while (iterator.hasNext()) {
                ((ComponentEntity) iterator.next()).setContainer(null);
            }

            ((ComponentEntity) renderer).setContainer(this);
        } catch (KernelException ex) {
            throw new RuntimeException("Renderer " + renderer
                + " could not be set");
        }
    }

    /** Render the edge on the given layer between the two sites.
     */
    public Connector render(Object edge, FigureLayer layer, Site tailSite,
        Site headSite) {
        Connector ef = getEdgeRenderer().render(edge, tailSite, headSite);
        ef.setInteractor(getEdgeInteractor());
        ef.setUserObject(edge);
        getController().setFigure(edge, ef);

        layer.add(ef);
        ef.route();
        return ef;
    }

    ///////////////////////////////////////////////////////////////////
    //// EdgeDropper

    /** An inner class that handles interactive changes to connectivity.
     */
    protected class EdgeDropper extends ConnectorAdapter {
        /**
         * Called when a connector end is dropped--attach or
         * detach the edge as appropriate.
         */
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Object edge = c.getUserObject();
            Object node = (f == null) ? null : f.getUserObject();
            MutableGraphModel model = (MutableGraphModel) getController()
                                                                      .getGraphModel();

            try {
                switch (evt.getEnd()) {
                case ConnectorEvent.HEAD_END:
                    model.setEdgeHead(getController(), edge, node);
                    break;

                case ConnectorEvent.TAIL_END:
                    model.setEdgeTail(getController(), edge, node);
                    break;

                default:
                    throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
                }
            } catch (GraphException ex) {
                SelectionModel selectionModel = getController()
                                                            .getSelectionModel();

                // If it is illegal then blow away the edge.
                if (selectionModel.containsSelection(c)) {
                    selectionModel.removeSelection(c);
                }

                removeEdge(edge);
                throw ex;
            }
        }
    }
}
