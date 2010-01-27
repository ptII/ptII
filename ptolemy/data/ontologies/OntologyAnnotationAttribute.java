/*
 * An annotation attribute that specifies ontology constraints in the model.
 * 
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2008-2010 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.ontologies;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

/**
 * An annotation attribute that specifies ontology constraints in the model. The
 * name of the attribute is prefixed by the ontology's name.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class OntologyAnnotationAttribute extends StringAttribute {

    /**
     * Construct an OntologyAnnotationAttribute with the specified name, and container.
     * 
     * @param container Container
     * @param name The given name for the attribute.
     * @exception IllegalActionException If the attribute is not of an
     * acceptable class for the container, or if the name contains a period.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public OntologyAnnotationAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Set the name of the attribute and error-check for name format. A proper
     * name should contain an ontology identifier and an attribute label,
     * separated by "::".
     * 
     * @param name The new name that the attribute should be set to.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     * @exception IllegalActionException If the name does not have the correct syntax
     * or does not refer to a valid ontology.
     */
    public void setName(String name) throws IllegalActionException,
            NameDuplicationException {
        super.setName(name);
        //_checkAttributeName(name);
    }

    //     /**
    //      * Check the name of the annotation attribute. The given name contains
    //      * an use case identifier and the annotation label. The two parts are
    //      * separated by the symbol "::" (two consecutive semicolons). The use
    //      * case identifier needs to be associated with a PropertySolver in
    //      * the model. Bad
    //      * @param name The given name of the annotation attribute.
    //      * @exception IllegalActionException Thrown if no PropertySolver can
    //      *  be found using the given name.
    //      * @exception NameDuplicationException Not thrown in this method.
    //      */
    //    private void _checkAttributeName(String name)
    //    throws IllegalActionException, NameDuplicationException {
    //        /*String usecaseName =*/ getUseCaseIdentifier();
    //
    //        // FIXME: Cannot check if there is an associated solver
    //        // because it may not be instantiated yet.
    //
    //        List solvers = toplevel().attributeList(PropertySolver.class);
    //        if (solvers.isEmpty()) {
    //            throw new IllegalActionException(
    //                    "No use case found for annotation: " + usecaseName + ".");
    //        } else {
    //            try {
    //                ((PropertySolver)solvers.get(0)).findSolver(usecaseName);
    //
    //            } catch (IllegalActionException ex) {
    //                throw new IllegalActionException(
    //                        "No use case found for annotation: " + usecaseName + ".");
    //            }
    //        }
    //    }

    /** 
     * Returns the name of the ontology for which this annotation
     * attribute is a constraint.
     *
     * @return A String representing the name of the referred ontology.
     * @exception IllegalActionException If a valid ontology identifier cannot be
     * found in the attribute name.
     */
    public String getOntologyIdentifier() throws IllegalActionException {
        String[] tokens = getName().split("::");

        if (tokens.length == 2) {
            return tokens[0];

        } else if (tokens.length == 3) {
            // If it is an extended use-case identifier,
            // which would contain an extra "::" symbol.
            return tokens[0] + "::" + tokens[1];
        }

        throw new IllegalActionException("Invalid ontology annotation attribute name: "
                + getName() + ". (should have form ONTOLOGY_NAME::LABEL)");
    }
}
