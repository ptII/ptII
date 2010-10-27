/* The default adapter class for ptolemy.data.expr.ASTPtSumNode.

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

package ptolemy.data.ontologies.lattice.adapters.defaultAdapters.data.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.lattice.AddConceptFunctionDefinition;
import ptolemy.data.ontologies.lattice.LatticeOntologyASTNodeAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.SubtractConceptFunctionDefinition;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtSumNode

/**
 The default adapter class for ptolemy.data.expr.ASTPtSumNode.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ASTPtSumNode extends LatticeOntologyASTNodeAdapter {

    /** Construct an property constraint adapter for the given ASTPtSumNode.
     *  @param solver The given solver to get the lattice from.
     *  @param node The given ASTPtSumNode.
     *  @exception IllegalActionException Thrown if the parent construct
     *   throws it.
     */
    public ASTPtSumNode(LatticeOntologySolver solver,
            ptolemy.data.expr.ASTPtSumNode node)
            throws IllegalActionException {
        super(solver, node, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the constraint list for the adapter.
     *  @throws IllegalActionException If there is an error building the constraint list.
     *  @return The list of constraints for this adapter.
     */
    public List<Inequality> constraintList() throws IllegalActionException {

        // Find the add and subtract concept functions that
        // are needed for the PtSumNode monotonic function.        
        ConceptFunction addFunction = null;
        AddConceptFunctionDefinition addDefinition = (AddConceptFunctionDefinition) (_solver
                .getContainedModel())
                .getAttribute(LatticeOntologySolver.ADD_FUNCTION_NAME);
        if (addDefinition != null) {
            addFunction = addDefinition.createConceptFunction();
        }

        ConceptFunction subtractFunction = null;
        SubtractConceptFunctionDefinition subtractDefinition = (SubtractConceptFunctionDefinition) (_solver
                .getContainedModel())
                .getAttribute(LatticeOntologySolver.SUBTRACT_FUNCTION_NAME);
        if (subtractDefinition != null) {
            subtractFunction = subtractDefinition.createConceptFunction();
        }        

        InequalityTerm[] childNodeTerms = _getChildNodeTerms();
        List<Ontology> argumentDomainOntologies = new ArrayList<Ontology>(childNodeTerms.length);
        for (int i = 0; i < childNodeTerms.length; i++) {
            argumentDomainOntologies.add(getSolver().getOntology());
        }
        
        List operatorTokenList = ((ptolemy.data.expr.ASTPtSumNode) _getNode())
                .getLexicalTokenList();

        ASTPtSumNodeFunction astSumFunction = new ASTPtSumNodeFunction(
                argumentDomainOntologies, getSolver()
                        .getOntology(), addFunction, subtractFunction,
                operatorTokenList);

        if (!astSumFunction.isMonotonic()) {
            throw new IllegalActionException(
                    _solver,
                    "The concept function for determining the "
                            + "PtProductNode concept is not monotonic. All concept functions used for a "
                            + "lattice ontology solver must be monotonic.");
        }

        setAtLeast(_getNode(), new ConceptFunctionInequalityTerm(
                astSumFunction, childNodeTerms));

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                  private inner classes                    ////

    /** The Concept Function that outputs the concept for
     *  the sum node based on the input child nodes.
     *  This is a general function that covers any combination
     *  of addition and subtraction operators in the sum node.
     */
    private class ASTPtSumNodeFunction extends ConceptFunction {

        /** Initialize the ASTPtProductNodeFunction.
         * @param argumentDomainOntologies The array of domain ontologies
         *  for the function arguments.
         * @param outputRangeOntology The ontology range for the output.
         * @param addFunction The addition concept function to be
         *  used when calculating the sum node function.  If this is null,
         *  then the simple least upper bound is used between two addition
         *  operands.
         * @param subtractFunction The subtraction concept function to be
         *  used when calculating the sum node function.  If this is null,
         *  then the simple least upper bound is used between two subtraction
         *  operands.
         * @param operatorTokenList The list of operator tokens for the sum node
         *  expression.
         * @throws IllegalActionException If the class cannot be initialized.
         */
        public ASTPtSumNodeFunction(List<Ontology> argumentDomainOntologies,
                Ontology outputRangeOntology, ConceptFunction addFunction,
                ConceptFunction subtractFunction, List operatorTokenList)
                throws IllegalActionException {
            super("defaultASTPtSumNodeFunction", true,
                    argumentDomainOntologies, outputRangeOntology);

            _addFunction = addFunction;
            _subtractFunction = subtractFunction;
            _operatorTokenList = operatorTokenList;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         protected inner methods           ////

        /** Return the function result.
         *  @param inputConceptValues The array of input arguments for the function.
         *  @return The concept value that is output from this function. 
         *  @throws IllegalActionException If there is a problem evaluating the function
         */
        protected Concept _evaluateFunction(List<Concept> inputConceptValues)
                throws IllegalActionException {
            
            // Loop through all the child node concepts in the sum node
            // and get the correct result property by calling the add concept function
            // or subtract concept function depending on the operator used.
            // If the add function or subtract function is null, then just
            // get the least upper bound of the concepts.

            // Initialize the result to the first node in the product node
            Concept result = inputConceptValues.get(0);

            // Iterate through the operator tokens
            Iterator lexicalTokenIterator = _operatorTokenList.iterator();

            for (int i = 1; i < inputConceptValues.size(); i++) {
                if (lexicalTokenIterator.hasNext()) {
                    Token lexicalToken = (Token) lexicalTokenIterator.next();
                    Concept nodeChildConcept = inputConceptValues.get(i);
                    List<Concept> conceptInputs = new ArrayList<Concept>(2);
                    conceptInputs.add(result);
                    conceptInputs.add(nodeChildConcept);

                    // If operator token is '*' call the MultiplyMonotonicFunction
                    if (lexicalToken.kind == PtParserConstants.PLUS) {
                        if (_addFunction != null) {
                            result = _addFunction
                                    .evaluateFunction(conceptInputs);
                        } else {
                            // FIXME: Implement LUB and change this
                            result = (Concept) _outputRangeOntology.getConceptGraph()
                                    .leastUpperBound(result, nodeChildConcept);
                        }

                        // If operator token is '/' call the DivideMonotonicFunction
                    } else {
                        if (_subtractFunction != null) {
                            result = _subtractFunction
                                    .evaluateFunction(conceptInputs);
                        } else {
                            // FIXME: Implement LUB and change this
                            result = (Concept) _outputRangeOntology.getConceptGraph()
                                    .leastUpperBound(result, nodeChildConcept);
                        }
                    }

                } else {
                    throw new IllegalActionException(
                            ASTPtSumNode.this.getSolver(),
                            "Error in the product expression; "
                                    + "the number of operators don't match the number of operands.");
                }
            }

            return result;
        }

        ///////////////////////////////////////////////////////////////////
        ////                      private inner variables              ////

        /** The add concept function to be
         *  used when calculating the sum node function.
         */
        private ConceptFunction _addFunction;

        /** The subtract concept function to be
         *  used when calculating the sum node function.
         */
        private ConceptFunction _subtractFunction;

        /** The list of operator tokens '+' and '-' that
         *  are contained in the Ptolemy AST sum node.
         */
        private List _operatorTokenList;
    }

}
