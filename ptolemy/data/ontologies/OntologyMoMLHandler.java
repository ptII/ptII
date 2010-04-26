/* An attribute that helps an OntologySolver to issue MoML requests and
 make changes to the model.

 Copyright (c) 2006-2010 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import java.util.List;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// OntologyMoMLHandler

/**
 This is an attribute used by the PropertySolver to issue MoML requests and
 make changes to the model. These changes include addition, update, or deletion
 of property annotations and display of the property results.
 This is designed to be contained by an instance of PropertySolver
 or a subclass of PropertySolver. It contains parameters that allow
 users to configure the display of the property annotation results.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class OntologyMoMLHandler extends Attribute {

    /** Construct an OntologyMoMLHandler with the specified container and name.
     *  @param container The container.
     *  @param name The name of the OntologyMoMLHandler.
     *  @exception IllegalActionException If the OntologyMoMLHandler is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public OntologyMoMLHandler(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Remove the highlighting and visible annotations
     * for all property-able objects.
     * @throws IllegalActionException If getting the resolved concept fails.
     */
    public void clearDisplay() throws IllegalActionException {
        // Get the PropertySolver.
        OntologySolver solver = (OntologySolver) getContainer();
        for (Object propertyable : solver.getAllPropertyables()) {
            if (propertyable instanceof NamedObj) {
                Concept concept = solver
                        .getResolvedConcept(propertyable, false);
                if (concept != null
                        || ((((NamedObj) propertyable)
                                        .getAttribute("_showInfo") != null) || (((NamedObj) propertyable)
                                .getAttribute("_highlightColor") != null))) {
                    String request = "<group>";
                    if (((NamedObj) propertyable).getAttribute("_showInfo") != null) {
                        request += "<deleteProperty name=\"_showInfo\"/>";
                    }
                    if (((NamedObj) propertyable)
                            .getAttribute("_highlightColor") != null) {
                        request += "<deleteProperty name=\"_highlightColor\"/>";
                    }
                    request += "</group>";
                    MoMLChangeRequest change = new MoMLChangeRequest(this,
                            (NamedObj) propertyable, request, false);
                    ((NamedObj) propertyable).requestChange(change);
                }
            }
        }
        // Force a single repaint after all the above requests have been processed.
        solver.requestChange(new MoMLChangeRequest(this, solver, "<group/>"));
    }

    /** Highlight all property-able objects with
     *  the specified colors for their property values.
     *  @throws IllegalActionException If getting the resolved concept fails.
     */
    public void highlightProperties() throws IllegalActionException {
        // Get the PropertySolver.
        OntologySolver solver = (OntologySolver) getContainer();
        for (Object propertyable : solver.getAllPropertyables()) {
            if (propertyable instanceof NamedObj) {
                Concept concept = solver.getResolvedConcept(propertyable, true);
                if (concept != null) {
                    // Use the color in the concept instance.
                    List<ColorAttribute> colors = concept
                            .attributeList(ColorAttribute.class);
                    if (colors != null && colors.size() > 0) {
                        // ConceptIcon renders the first found ColorAttribute,
                        // so we use that one here as well.
                        ColorAttribute conceptColor = colors.get(0);
                        String request = "<property name=\"_highlightColor\" "
                                + "class=\"ptolemy.actor.gui.ColorAttribute\" value=\""
                                + conceptColor.getExpression() + "\"/>";
                        MoMLChangeRequest change = new MoMLChangeRequest(this,
                                (NamedObj) propertyable, request, false);
                        ((NamedObj) propertyable).requestChange(change);
                    }
                }
            }
        }
        // Force a single repaint after all the above requests have been processed.
        solver.requestChange(new MoMLChangeRequest(this, solver, "<group/>"));
    }

    /**
     * If the value of the showText parameter is set to
     * true, show all property values visually.
     * Otherwise, do nothing.
     * @throws IllegalActionException If getting the resolved concept fails.
     */
    public void showProperties() throws IllegalActionException {
        // Get the PropertySolver.
        OntologySolver solver = (OntologySolver) getContainer();
        for (Object propertyable : solver.getAllPropertyables()) {
            if (propertyable instanceof NamedObj) {
                Concept concept = solver.getResolvedConcept(propertyable, true);
                if (concept != null) {
                    String request = "<property name=\"_showInfo\" class=\"ptolemy.data.expr.StringParameter\" value=\""
                            + concept.toString() + "\"/>";
                    MoMLChangeRequest change = new MoMLChangeRequest(this,
                            (NamedObj) propertyable, request, false);
                    ((NamedObj) propertyable).requestChange(change);
                }
            }
        }
        // Force a single repaint after all the above requests have been processed.
        solver.requestChange(new MoMLChangeRequest(this, solver, "<group/>"));
    }

}
