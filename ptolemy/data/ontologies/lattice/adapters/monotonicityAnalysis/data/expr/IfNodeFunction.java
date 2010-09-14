/* 
 * 
 * Copyright (c) 2010 The Regents of the University of California. All
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
package ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.data.expr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.data.Token;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.ConceptToken;
import ptolemy.data.ontologies.ExpressionConceptFunctionParseTreeEvaluator;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConceptFunction;
import ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityCounterexamples;
import ptolemy.kernel.util.IllegalActionException;

/** A representation of the monotonic function used to infer the
 *  monotonicity of conditional nodes (if nodes) in the abstract
 *  syntax trees of Ptolemy expressions. 
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class IfNodeFunction extends MonotonicityConceptFunction {



    /** Create a new function from the given ifNode and
     *  over the given monotonicity ontology.
     *  
     *  @param ifNode The AST node being constrained by this function. 
     *  @param monotonicityOntology The monotonicity ontology.
     *  @param domainOntology The ontology over which the expression
     *   should be interpreted. 
     *  @throws IllegalActionException If a function cannot be created.
     */
    public IfNodeFunction(
            ptolemy.data.expr.ASTPtFunctionalIfNode ifNode,
            Ontology monotonicityOntology,
            Ontology domainOntology)
    throws IllegalActionException {
        super("defaultASTPtFunctionalIfNodeFunction", 3,
                monotonicityOntology);
        _ifNode = ifNode;
        _domainOntology = domainOntology;
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
        _nodeToCounterexamples.remove(_ifNode);
        Concept result = _standardIfAnalysis(inputConceptValues);
        if (result.isAboveOrEqualTo(_generalConcept)) {
            if (_checkConditionalStructure(inputConceptValues)) {
                ptolemy.data.expr.ASTPtRelationalNode condition = (ptolemy.data.expr.ASTPtRelationalNode) _ifNode.jjtGetChild(0);
                Concept constant = _extractConstant(condition);
                result = _specialIfAnalysis(constant);
            }
        }
        return result;
    }

    /** Check that an expression is of the form:
     *    (x <= c) ? e1 : e2
     *  where e1 and e2 are monotonic, and c is a constant.
     *  
     *  @param inputConceptValues The monotonicity of the conditional
     *    and branches.
     *  @return True, if the expression meets all checks. False, otherwise.
     *  @exception IllegalActionException If there is a problem
     *    evaluating the constant.
     */
    private boolean _checkConditionalStructure(List<Concept> inputConceptValues) throws IllegalActionException {
        Concept mconditional = inputConceptValues.get(0);
        Concept me3 = inputConceptValues.get(1);
        Concept me4 = inputConceptValues.get(2);

        if (!_monotonicConcept.isAboveOrEqualTo(mconditional)
                || !_monotonicConcept.isAboveOrEqualTo(me3)) {
            return false;
        }
        if (!_monotonicConcept.isAboveOrEqualTo(me4)
                && !_nodeToCounterexamples.containsKey(_ifNode.jjtGetChild(2))) {
            return false;
        }

        ptolemy.data.expr.ASTPtRelationalNode condition = (ptolemy.data.expr.ASTPtRelationalNode) _ifNode.jjtGetChild(0);
        ptolemy.data.expr.ASTPtRootNode rhs = (ptolemy.data.expr.ASTPtRootNode) condition.jjtGetChild(1);
        String conditionalType = condition.getOperator().toString();
        if (conditionalType != "<=" || !(rhs instanceof ptolemy.data.expr.ASTPtLeafNode)) {
            return false;
        }
        List<Ontology> argumentDomains = new LinkedList<Ontology>();
        argumentDomains.add(_domainOntology);
        ParseTreeEvaluator evaluator = new ExpressionConceptFunctionParseTreeEvaluator(
                new LinkedList<String>(), new LinkedList<Concept>(), null,
                argumentDomains);
        Token rhsToken = evaluator.evaluateParseTree(rhs);    
        if (!(rhsToken instanceof ConceptToken)) {
            return false;
        }
        return true;
    }
    
    /** Assuming the right hand side of a conditional is constant, evaluate
     *  and return that constant.
     *  @param condition The AST node for the conditional expression.
     *  @return The concept on the right hand side of the conditional.
     *  @throws IllegalActionException If the right hand side of the
     *   conditional is not properly formed.
     */
    private Concept _extractConstant(ptolemy.data.expr.ASTPtRelationalNode condition) throws IllegalActionException {
        ptolemy.data.expr.ASTPtRootNode rhs = (ptolemy.data.expr.ASTPtRootNode) condition.jjtGetChild(1);
        List<Ontology> argumentDomains = new LinkedList<Ontology>();
        argumentDomains.add(_domainOntology);
        ParseTreeEvaluator evaluator = new ExpressionConceptFunctionParseTreeEvaluator(
                new LinkedList<String>(), new LinkedList<Concept>(), null,
                argumentDomains);
        Token rhsToken = evaluator.evaluateParseTree(rhs);
        Concept constant = ((ConceptToken)rhsToken).conceptValue();
        return constant;
    }

    /** Evaluate a branch of the if statement pointed to by _ifNode and
     *  return the result.
     *  @param childNumber 1 for the then branch, and 2 for the
     *      else branch.
     *  @param xValue The value of the variable "x" during evaluation.
     *  @return The concept that the given child evaluates to.
     *  @exception IllegalActionException If there is a problem while
     *      evaluating the parse tree, or an invalid childNumber is
     *      passed.
     */
    private Concept _evaluateChild(int childNumber, Concept xValue) throws IllegalActionException {
        ptolemy.data.expr.ASTPtRootNode childNode = (ptolemy.data.expr.ASTPtRootNode) _ifNode.jjtGetChild(childNumber);

        Map<String, Concept> arguments = new HashMap<String, Concept>();
        arguments.put("x", xValue);

        return _evaluateNode(childNode, arguments);
    }

    /** Evaluate the given node as a concept expression over the domain
     *  ontology, using the the given value for "x", and return the
     *  result.
     *  
     *  @param node The Ptolemy AST node to evaluate.
     *  @param arguments The value of "x" in this evaluation.
     *  @return The computed concept value.
     *  @throws IllegalActionException If there is an error during evaluation.
     */
    private Concept _evaluateNode(ptolemy.data.expr.ASTPtRootNode node,
            Map<String, Concept> arguments) throws IllegalActionException {

        ParseTreeEvaluator evaluator = new ExpressionConceptFunctionParseTreeEvaluator(
                arguments, null, _domainOntology);
        ConceptToken evaluatedToken = (ConceptToken)evaluator.evaluateParseTree(node);
        return evaluatedToken.conceptValue();
    }

    /** Return the monotonicity of the conditional being analyzed given a
     *  few extra assumptions.  The if statement must be of the form:
     *    (x <= c) ? e1 : e2
     *  where both e1 and e2 are montonic, and c is a constant.
     *  If these assumptions are not met, this function may return an
     *  unsound analysis.
     *  
     *  Note that this function performs an analysis that is approximately
     *  equivalent to the ifc analysis of "Static Monotonicity Analysis
     *  for lambda-definable Functions over Lattices" (Murawski and Yi,
     *  2002), except that that analysis is unsound.  This analysis
     *  is a corrected form of that one.
     *  
     *  @param constant The constant c.
     *  @return Monotonic, if the function is monotonic.
     *    Nonmonotonic, otherwise.
     *  @exception IllegalActionException If there is a problem
     *    evaluating the subexpressions of the conditional.
     */
    private Concept _specialIfAnalysis(Concept constant) throws IllegalActionException {
        // Get counterexamples to check from subexpressions
        MonotonicityCounterexamples toCheck = _nodeToCounterexamples.get(_ifNode.jjtGetChild(2));
        if (toCheck == null) {
            toCheck = new MonotonicityCounterexamples();
        }
        // Get counterexamples to check from this predicate's border
        ConceptGraph inputLattice = _domainOntology.getGraph();
        List downsetList = Arrays.asList(inputLattice.downSet(constant));
        List<Concept> downset = (List<Concept>) downsetList;
        for (Concept b : downset) {
            for (Concept d : b.getStrictDominators()) {
                if (downset.contains(d)) {
                    continue;
                }
                toCheck.add(b, d);
            }
        }
        // Check for counterexamples
        MonotonicityCounterexamples counterexamples = new MonotonicityCounterexamples();
        for (MonotonicityCounterexamples.ConceptPair pair : toCheck.entrySet()) {
            Map<String, Concept> bArguments = new HashMap<String, Concept>();
            bArguments.put("x", pair.getKey());
            Map<String, Concept> dArguments = new HashMap<String, Concept>();
            dArguments.put("x", pair.getValue());
            Concept fb = _evaluateNode(_ifNode, bArguments);
            Concept fd = _evaluateNode(_ifNode, dArguments);
            if (!fd.isAboveOrEqualTo(fb)) {
                counterexamples.add(pair.getKey(), pair.getValue());
            } 
        }
        if (counterexamples.containsCounterexamples()) {
            _nodeToCounterexamples.put(_ifNode, counterexamples);
            return _generalConcept;
        } else {
            return _monotonicConcept;
        }
    }


    /** Perform the most general type of conditional analysis.
     *  This version does not make any assumptions about the structure
     *  of the conditional statement.
     *  
     *  Note that the analysis is sound but conservative, so it is
     *  possible for a monotonic function to be reported as nonmonotonic,
     *  for example.
     *  
     *  @param inputConceptValues The monotonicity of each of the
     *    subexpressions of this node.
     *  @return Either Constant, Monotonic, Antimonotonic, or
     *    Nonmonotonic, depending on the result of the analysis.
     *  @exception IllegalActionException If there is an error evaluating the function.
     */
    private Concept _standardIfAnalysis(List<Concept> inputConceptValues)
    throws IllegalActionException {
        ConceptGraph monotonicityLattice = _monotonicityAnalysisOntology.getGraph();
        ConceptGraph inputLattice = _domainOntology.getGraph();

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

        return _generalConcept;
    }

    /** The AST node for the conditional expression that this
     *  function is defined over.
     */
    private ptolemy.data.expr.ASTPtFunctionalIfNode _ifNode;

    /** The Ontology over which the expression under consideration's
     *  variables and constants are drawn from.
     */
    private Ontology _domainOntology;
    
    /** A static Map that keeps track of the counterexamples at different
     *  nodes in the Ptolemy AST.  This doesn't seem like the right way
     *  to keep track of these things, and should probably be redesigned.
     *  FIXME: Rethink this approach.
     */
    private static Map<ptolemy.data.expr.ASTPtFunctionalIfNode,
                       MonotonicityCounterexamples> _nodeToCounterexamples =
           new HashMap<ptolemy.data.expr.ASTPtFunctionalIfNode,
                       MonotonicityCounterexamples>();   
}
