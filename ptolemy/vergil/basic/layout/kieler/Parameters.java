/*

Copyright (c) 2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package ptolemy.vergil.basic.layout.kieler;

import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.layout.LayoutConfiguration;
import ptolemy.vergil.modal.FSMGraphModel;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.EdgeRouting;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.layered.properties.Properties;
import diva.graph.GraphModel;

/**
 * Responsible for translating layout configuration parameters into the KIELER format.
 * Parameters are read from an instance of the {@link LayoutConfiguration} attribute,
 * which is attached to composite entities when the configuration dialog is opened.
 * 
 * @see LayoutConfiguration
 * @author Miro Spoenemann
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (msp)
 * @Pt.AcceptedRating Red (msp)
 */
public class Parameters {

    /**
     * Create a parameters instance.
     * 
     * @param compositeEntity the composite entity for which parameters are retrieved.
     */
    public Parameters(CompositeEntity compositeEntity) {
        _compositeEntity = compositeEntity;
    }
    
    /**
     * Configure the KIELER layout using a property holder.
     * 
     * @param parentLayout the layout of the parent node
     * @param options property holder from which user options are copied
     * @param graphModel the graph model of the current diagram
     * @throws IllegalActionException if one of the parameters has the wrong type
     */
    public void configureLayout(KShapeLayout parentLayout, GraphModel graphModel)
            throws IllegalActionException {
        // Set general default values.
        parentLayout.setProperty(LayoutOptions.DIRECTION, Direction.RIGHT);
        parentLayout.setProperty(LayoutOptions.BORDER_SPACING, 5.0f);
        parentLayout.setProperty(Properties.EDGE_SPACING_FACTOR, 1.5f);
        
        // Copy values specified by user.
        List<LayoutConfiguration> configAttributes = _compositeEntity
                .attributeList(LayoutConfiguration.class);
        if (!configAttributes.isEmpty()) {
            LayoutConfiguration configuration = configAttributes.get(0);
            
            BooleanToken decorationsToken = BooleanToken.convert(
                    configuration.includeDecorations.getToken());
            parentLayout.setProperty(DECORATIONS, decorationsToken.booleanValue());
            
            BooleanToken routeToken = BooleanToken.convert(
                    configuration.routeEdges.getToken());
            parentLayout.setProperty(ROUTE_EDGES, routeToken.booleanValue());
            
            DoubleToken spacingToken = DoubleToken.convert(
                    configuration.spacing.getToken());
            parentLayout.setProperty(SPACING, (float) spacingToken.doubleValue());
            
            DoubleToken logAspectToken = DoubleToken.convert(
                    configuration.logAspectRatio.getToken());
            parentLayout.setProperty(ASPECT_RATIO, (float) Math.pow(
                    10, logAspectToken.doubleValue()));
            
        } else {
            parentLayout.setProperty(LayoutOptions.SPACING, SPACING.getDefault());
            parentLayout.setProperty(LayoutOptions.ASPECT_RATIO, ASPECT_RATIO.getDefault());
        }
        
        if (graphModel instanceof ActorGraphModel) {
            // Set default values for actor models.
            parentLayout.setProperty(LayoutOptions.EDGE_ROUTING, EdgeRouting.ORTHOGONAL);
        } else if (graphModel instanceof FSMGraphModel) {
            // Set default values for modal models.
            parentLayout.setProperty(LayoutOptions.EDGE_ROUTING, EdgeRouting.POLYLINE);
            float spacing = parentLayout.getProperty(SPACING);
            parentLayout.setProperty(SPACING, 2 * spacing);
        }
    }
    
    /** Layout option that determines whether decoration nodes are included in layout. */
    public static final IProperty<Boolean> DECORATIONS = new Property<Boolean>(
            "ptolemy.vergil.basic.layout.decorations", LayoutConfiguration.DEF_DECORATIONS);
    
    /** Layout option that determines whether edges are routed. */
    public static final IProperty<Boolean> ROUTE_EDGES = new Property<Boolean>(
            "ptolemy.vergil.basic.layout.routeEdges", LayoutConfiguration.DEF_ROUTE_EDGES);
    
    /** Layout option for the overall spacing between elements. */
    public static final IProperty<Float> SPACING = new Property<Float>(
            LayoutOptions.SPACING, (float) LayoutConfiguration.DEF_SPACING);

    /** Layout option for the aspect ratio of connected components. */
    public static final IProperty<Float> ASPECT_RATIO = new Property<Float>(
            LayoutOptions.ASPECT_RATIO, (float) LayoutConfiguration.DEF_ASPECT_RATIO);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** the parent entity from which the layout configuration is read. */
    private CompositeEntity _compositeEntity;

}
