/* Adapter for FunctionalIfNodes in the monotonicity analysis.

 Copyright (c) 2010 The Regents of the University of California.
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

package ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.data.expr;

import java.util.ArrayList;
import java.util.LinkedList;

import java.util.List;

import ptolemy.data.ConceptToken;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.ExpressionConceptFunctionParseTreeEvaluator;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.lattice.LatticeOntologyASTNodeAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConceptFunction;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtFunctionalIfNode

/**
 Adapter for FunctionalIfNodes in the monotonicity analysis.

 @author Ben Lickly
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ASTPtFunctionalIfNode extends LatticeOntologyASTNodeAdapter {

    /**
     * Construct an property constraint adapter for the given ASTPtArrayConstructNode.
     * @param solver The given solver to get the lattice from.
     * @param node The given ASTPtArrayConstructNode.
     * @exception IllegalActionException Thrown if the parent construct
     *  throws it.
     */
    public ASTPtFunctionalIfNode(LatticeOntologySolver solver,
            ptolemy.data.expr.ASTPtFunctionalIfNode node)
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

        InequalityTerm[] childNodeTerms = _getChildNodeTerms();
        List<Ontology> argumentDomainOntologies = new ArrayList<Ontology>(childNodeTerms.length);
        for (int i = 0; i < childNodeTerms.length; i++) {
            argumentDomainOntologies.add(getSolver().getOntology());
        }

        ASTPtFunctionalIfNodeFunction astIfFunction = new ASTPtFunctionalIfNodeFunction(
                (ptolemy.data.expr.ASTPtFunctionalIfNode) _getNode(),
                argumentDomainOntologies,
                // FIXME: This assumption that the ontology of the expression
                // is the same as this solver is bogus.  It should be
                // parameterized somewhere instead.
                getSolver().getOntology());

        if (!astIfFunction.isMonotonic()) {
            throw new IllegalActionException(
                    _solver,
                    "The concept function for determining the "
                            + "PtIfNode concept is not monotonic. All concept functions used for a "
                            + "lattice ontology solver must be monotonic.");
        }

        setAtLeast(_getNode(), new ConceptFunctionInequalityTerm(
                astIfFunction, childNodeTerms));

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private inner class                    ////

    
    /** A representation of the monotonic function used to infer the
     *  monotonicity of conditional nodes (if nodes) in the abstract
     *  syntax trees of Ptolemy expressions.
     */
    private class ASTPtFunctionalIfNodeFunction extends MonotonicityConceptFunction {

        /** Create a new function from the given ifNode and
         *  over the given ontologies.
         *  
         *  @param ifNode The AST node being constrained by this function. 
         *  @param argumentDomainOntologies A list of ontologies that the
         *     arguments of the expression of this AST are drawn from.
         *     Can be null (or the empty list) in the case of an
         *     argumentless expression.
         *  @param outputRangeOntology The ontology that forms the codomain
         *     of this function.
         *  @throws IllegalActionException If a function cannot be created.
         */
        public ASTPtFunctionalIfNodeFunction(
                ptolemy.data.expr.ASTPtFunctionalIfNode ifNode,
                List<Ontology> argumentDomainOntologies,
                Ontology outputRangeOntology)
                    throws IllegalActionException {
            super("defaultASTPtFunctionalIfNodeFunction", true,
                    argumentDomainOntologies, outputRangeOntology);
            _ifNode = ifNode;
        }

        /** Return the monotonicity concept that results from analyzing the
         *  conditional statement.  Note that the analysis is sound but
         *  conservative, so it is possible for a monotonic function to be
         *  reported as nonmonotonic, but not the other way around.
         *  
         *  @param inputConceptValues The list of concept inputs to the function.
         *    (i.e. The monotonicity of each of the conditional's branches)
         *  @return Either Constant, Monotonic, Antimonotonic, or
         *    Nonmonotonic, depending on the result of the analysis.
         *  @exception IllegalActionException If there is an error evaluating the function.
         *  @see ptolemy.data.ontologies.ConceptFunction#_evaluateFunction(java.util.List)
         */
        protected Concept _evaluateFunction(List<Concept> inputConceptValues)
                throws IllegalActionException {
            ConceptGraph monotonicityLattice = _monotonicityAnalysisOntology.getGraph();

            // This represents the if rule. (from p144)
            Concept conditional = inputConceptValues.get(0);
            Concept me3 = inputConceptValues.get(1);
            Concept me4 = inputConceptValues.get(2);
            // Case 5 (my case) from the simple if table
            // i.e.    0    0    a    a   none    a
            if (conditional == _constantConcept) {
                return (Concept) monotonicityLattice.leastUpperBound(me3, me4);
            }
            
            boolean bothBranchesMonotonic = _monotonicConcept.isAboveOrEqualTo(me3) && _monotonicConcept.isAboveOrEqualTo(me4);
            boolean bothBranchesAntimonotonic = _antimonotonicConcept.isAboveOrEqualTo(me3) && _antimonotonicConcept.isAboveOrEqualTo(me4);
            // FIXME: The lattice here should be the input lattice (the domain of "x"),
            // which in general is not necessarily the monotonicityLattice
            ConceptGraph inputLattice = monotonicityLattice;
            if (_antimonotonicConcept.isAboveOrEqualTo(conditional)) {

                Concept e3Bot = _evaluateChild(1, (Concept)inputLattice.bottom());
                Concept e4Top = _evaluateChild(2, (Concept)inputLattice.top());
                if (bothBranchesMonotonic && e3Bot.isAboveOrEqualTo(e4Top)) {
                    // Case 1: \phi = e3(bot) >= e4(top)
                    return _monotonicConcept;
                } else if (bothBranchesAntimonotonic && e4Top.isAboveOrEqualTo(e3Bot)) {
                    // Case 2: \phi = e3(bot) <= e4(top)
                    return _antimonotonicConcept;
                }
            } else if (_monotonicConcept.isAboveOrEqualTo(conditional)) {
                Concept e3Top = _evaluateChild(1, (Concept)inputLattice.top());
                Concept e4Bot = _evaluateChild(2, (Concept)inputLattice.bottom());
                if (bothBranchesMonotonic && e4Bot.isAboveOrEqualTo(e3Top)) {
                    // Case 3: \phi = e3(top) <= e4(bot)
                    return _monotonicConcept;
                } else if (bothBranchesAntimonotonic && e3Top.isAboveOrEqualTo(e4Bot)) {
                    // Case 4: \phi = e3(top) >= e4(bot)
                    return _antimonotonicConcept;
                }
            }
            
            return _nonmonotonicConcept;
        }

        /** Evaluate a branch of the if statement pointed to by _ifNode and
         *  return the result.
         *  @param childNumber 1 for the then branch, and 2 for the
         *      else branch.
         *  @return The concept that the given child evaluates to.
         *  @throws IllegalActionException If there is a problem while
         *      evaluating the parse tree, or an invalid childNumber is
         *      passed.
         */
        private Concept _evaluateChild(int childNumber, Concept xValue) throws IllegalActionException {
            ASTPtRootNode childNode = (ASTPtRootNode) _ifNode.jjtGetChild(childNumber);
            
            // FIXME: Refactor so that we can have multiple argument names,
            // and not all named "x"
            List<String> argumentNames = new LinkedList<String>();
            argumentNames.add("x");
            List<Concept> argumentValues = new LinkedList<Concept>();
            argumentValues.add(xValue);
            
            ParseTreeEvaluator evaluator = new ExpressionConceptFunctionParseTreeEvaluator(
                    argumentNames,
                    argumentValues,
                    null,
                    _argumentDomainOntologies);
            ConceptToken evaluatedToken = (ConceptToken)evaluator.evaluateParseTree(childNode);
            return evaluatedToken.conceptValue();
        }
        
        /** The AST node for the conditional expression that this
         *  function is defined over.
         */
        private ptolemy.data.expr.ASTPtFunctionalIfNode _ifNode;

    }

}
