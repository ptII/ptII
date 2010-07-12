/* Top level adapter class for all DimensionSystem ontology adapters.

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
package ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis;

import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// DimensionSystemAdapter

/**
 The top level adapter class for MonotonicityAnalysis adapters.

 @author Ben Lickly (based on DimensionSystemAdapter)
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public abstract class MonotonicityConceptFunction extends ConceptFunction {

    /** Create the concept function with the number of arguments it takes
     *  and the ontologies from which input and output concepts can be taken.
     *  @param name The name of the concept function.
     *  @param numArgsIsFixed True if the number of arguments for this function
     *   is fixed and cannot change, false otherwise.
     *  @param argumentDomainOntologies The list of ontologies that
     *   represent the concept domain for each input concept argument.
     *  @param outputRangeOntology The ontology that represents the
     *   range of output concepts for this concept function.
     *  @exception IllegalActionException If the ontology inputs are null
     *   or the length of the array of domain ontologies does not
     *   match the number of arguments for the function.
     */
    public MonotonicityConceptFunction(String name, boolean numArgsIsFixed,
            List<Ontology> argumentDomainOntologies,
            Ontology outputRangeOntology) throws IllegalActionException {
        super(name, numArgsIsFixed, argumentDomainOntologies, outputRangeOntology);

        _monotonicityAnalysisOntology = outputRangeOntology;
        
        // FIXME: Should we hard code all the Concept name strings here?
        // Instantiate all the concepts for the monotonicityAnalysis ontology
        // Throw an exception if any of them are not found
        _constantConcept = (Concept) _monotonicityAnalysisOntology.getEntity("Constant");
        if (_constantConcept == null) {
            throw new IllegalActionException(_monotonicityAnalysisOntology,
                    "Concept Constant not found in monotonicityAnalysis ontology.");
        }

        _monotonicConcept = (Concept) _monotonicityAnalysisOntology.getEntity("Monotonic");
        if (_monotonicConcept == null) {
            throw new IllegalActionException(_monotonicityAnalysisOntology,
                    "Concept Monotonic not found in monotonicityAnalysis ontology.");
        }

        _antimonotonicConcept = (Concept) _monotonicityAnalysisOntology.getEntity("Antimonotonic");
        if (_antimonotonicConcept == null) {
            throw new IllegalActionException(_monotonicityAnalysisOntology,
                    "Concept Antimonotonic not found in monotonicityAnalysis ontology.");
        }

        _nonmonotonicConcept = (Concept) _monotonicityAnalysisOntology.getEntity("Nonmonotonic");
        if (_nonmonotonicConcept == null) {
            throw new IllegalActionException(_monotonicityAnalysisOntology,
                    "Concept Nonmonotonic not found in monotonicityAnalysis ontology.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    // The ontology for all the monotonicityAnalysis adapters

    /** The monotonicityAnalysis ontology referred to by all monotonicityAnalysis adapters. */
    protected Ontology _monotonicityAnalysisOntology;

    // Get all the Concepts from the ontology to use in all the monotonicityAnalysis adapters   

    /** The "Constant" Concept from the monotonicityAnalysis ontology. */
    protected Concept _constantConcept;

    /** The "Monotonic" Concept from the monotonicityAnalysis ontology. */
    protected Concept _monotonicConcept;

    /** The "Antimonotonic" Concept from the monotonicityAnalysis ontology. */
    protected Concept _antimonotonicConcept;

    /** The "Nonmonotonic" Concept from the monotonicityAnalysis ontology. */
    protected Concept _nonmonotonicConcept;
}