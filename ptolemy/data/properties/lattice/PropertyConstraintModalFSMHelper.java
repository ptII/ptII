/* An adapter class for ptolemy.domains.modal.kernel.FSMActor.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import ptolemy.actor.IOPort;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver.ConstraintType;
import ptolemy.domains.modal.kernel.AbstractActionsAttribute;
import ptolemy.domains.modal.kernel.CommitActionsAttribute;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.OutputActionsAttribute;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// FSMActor

/**
 An adapter class for ptolemy.domains.fsm.kernel.FSMActor.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintModalFSMHelper extends
        PropertyConstraintCompositeHelper {

    /**
     * Construct an adapter for the given FSMActor. This is the
     * base adapter class for any FSMActor that does not have a
     * specific defined adapter class. Default actor constraints
     * are set for this adapter.
     * @param solver The given solver.
     * @param actor The given ActomicActor.
     * @exception IllegalActionException Thrown if super class throws it.
     */
    public PropertyConstraintModalFSMHelper(PropertyConstraintSolver solver,
            ptolemy.domains.modal.kernel.FSMActor actor)
            throws IllegalActionException {

        super(solver, actor);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        // FIMXE: cannot call super here, because PropertyConstraintCompositeHelper
        // recursively call constraintList() of its children.
        //super.constraintList();

        HashMap<NamedObj, List<ASTPtRootNode>> outputActionMap = new HashMap<NamedObj, List<ASTPtRootNode>>();

        HashMap<NamedObj, List<ASTPtRootNode>> setActionMap = new HashMap<NamedObj, List<ASTPtRootNode>>();

        ptolemy.domains.modal.kernel.FSMActor actor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();

        List propertyableList = getPropertyables();

        Iterator states = actor.entityList(State.class).iterator();

        while (states.hasNext()) {
            State state = (State) states.next();
            Iterator transitions = state.outgoingPort.linkedRelationList()
                    .iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();

                Iterator propertyables = propertyableList.iterator();

                while (propertyables.hasNext()) {
                    Object propertyable = propertyables.next();
                    if (propertyable instanceof NamedObj) {

                        NamedObj namedObj = (NamedObj) propertyable;

                        OutputActionsAttribute outputActions = transition.outputActions;
                        if (outputActions.getDestinationNameList().contains(
                                namedObj.getName())) {
                            // do not consider multiple assigenments to same output from one action since
                            // only the last assignment in the expression is actually effective
                            ASTPtRootNode parseTree = outputActions
                                    .getParseTree(namedObj.getName());
                            if (!outputActionMap.containsKey(namedObj)) {
                                outputActionMap.put(namedObj,
                                        new ArrayList<ASTPtRootNode>());
                            }
                            outputActionMap.get(namedObj).add(parseTree);
                        }

                        CommitActionsAttribute setActions = transition.setActions;
                        if (setActions.getDestinationNameList().contains(
                                namedObj.getName())) {
                            // do not consider multiple assigenments to same output from one action since
                            // only the last assignment in the expression is actually effective
                            ASTPtRootNode parseTree = setActions
                                    .getParseTree(namedObj.getName());
                            if (!setActionMap.containsKey(namedObj)) {
                                setActionMap.put(namedObj,
                                        new ArrayList<ASTPtRootNode>());
                            }
                            setActionMap.get(namedObj).add(parseTree);
                        }
                    }
                }
            }
        }

        boolean constraintSource = (interconnectConstraintType == ConstraintType.SRC_EQUALS_MEET)
                || (interconnectConstraintType == ConstraintType.SRC_EQUALS_GREATER);

        Iterator outputActions = outputActionMap.entrySet().iterator();
        while (outputActions.hasNext()) {
            Entry entry = (Entry) outputActions.next();
            Object destination = entry.getKey();
            List<Object> expressions = (List<Object>) entry.getValue();

            if (constraintSource) {
                Iterator roots = expressions.iterator();

                while (roots.hasNext()) {
                    ASTPtRootNode root = (ASTPtRootNode) roots.next();
                    List<Object> sinkAsList = new ArrayList<Object>();
                    sinkAsList.add(destination);

                    _constraintObject(interconnectConstraintType, root,
                            sinkAsList);
                }
            } else {
                _constraintObject(interconnectConstraintType, destination,
                        expressions);
            }
        }

        Iterator setActions = setActionMap.entrySet().iterator();
        while (setActions.hasNext()) {
            Entry entry = (Entry) setActions.next();
            Object destination = entry.getKey();
            List<Object> expressions = (List<Object>) entry.getValue();

            if (constraintSource) {
                Iterator roots = expressions.iterator();

                while (roots.hasNext()) {
                    ASTPtRootNode root = (ASTPtRootNode) roots.next();
                    List<Object> sinkAsList = new ArrayList<Object>();
                    sinkAsList.add(destination);

                    _constraintObject(interconnectConstraintType, root,
                            sinkAsList);
                }
            } else {
                _constraintObject(interconnectConstraintType, destination,
                        expressions);
            }
        }
        _checkIneffectiveOutputPorts(actor, outputActionMap.keySet(),
                setActionMap.keySet());

        return _union(_ownConstraints, _subHelperConstraints);
    }

    public List<ASTPtRootNode> getParseTrees(State state) {
        List<ASTPtRootNode> result = new LinkedList<ASTPtRootNode>();
        Iterator transitions = state.outgoingPort.linkedRelationList()
                .iterator();

        while (transitions.hasNext()) {
            Transition transition = (Transition) transitions.next();

            result.addAll(_getParseTrees(transition.outputActions));
            result.addAll(_getParseTrees(transition.setActions));
        }
        return result;
    }

    public void setAtLeastByDefault(Object term1, Object term2) {
        setAtLeast(term1, term2);

        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 1);
            _solver.incrementStats("# of composite default constraints", 1);
        }
    }

    public void setSameAsByDefault(Object term1, Object term2) {
        setSameAs(term1, term2);

        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 2);
            _solver.incrementStats("# of composite default constraints", 2);
        }
    }

    /**
     * @exception IllegalActionException
     *
     */
    protected List<ASTPtRootNode> _getAttributeParseTrees()
            throws IllegalActionException {
        List<ASTPtRootNode> result = super._getAttributeParseTrees();

        ptolemy.domains.modal.kernel.FSMActor actor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();

        Iterator states = actor.entityList(State.class).iterator();
        while (states.hasNext()) {
            State state = (State) states.next();

            result.addAll(getParseTrees(state));
        }
        return result;
    }

    /**
     * Get the list of propertyable attributes for this adapter.
     * In this base adapter class for FSM, it considers all guard
     * expressions as propertyable attributes.
     * @return The list of propertyable attributes.
     */
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();

        ptolemy.domains.modal.kernel.FSMActor actor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();

        Iterator states = actor.entityList(State.class).iterator();
        while (states.hasNext()) {
            State state = (State) states.next();

            Iterator transitions = state.outgoingPort.linkedRelationList()
                    .iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();
                result.add(transition.guardExpression);
            }
        }

        return result;
    }

    /**
     * Return the list of sub-adapters. In this base class, it
     * returns the list of ASTNode adapters that are associated
     * with the expressions of the propertyable attributes.
     * @return The list of sub-adapters.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected List<PropertyHelper> _getSubHelpers()
            throws IllegalActionException {
        return _getASTNodeHelpers();
    }

    private void _checkIneffectiveOutputPorts(FSMActor actor,
            Set<NamedObj> setDestinations1, Set<NamedObj> setDestinations2) {

        Iterator outputs = actor.outputPortList().iterator();
        while (outputs.hasNext()) {
            IOPort output = (IOPort) outputs.next();
            if ((!setDestinations1.isEmpty()) && (!setDestinations2.isEmpty())) {
                if ((!setDestinations1.contains(output))
                        && (!setDestinations2.contains(output))) {
                    getPropertyTerm(output).setEffective(false);
                }
            } else if (setDestinations1.isEmpty()) {
                if (!setDestinations2.contains(output)) {
                    getPropertyTerm(output).setEffective(false);
                }
            } else if (setDestinations2.isEmpty()) {
                if (!setDestinations1.contains(output)) {
                    getPropertyTerm(output).setEffective(false);
                }
            }
        }
    }

    /** Return the parse tree that corresponds with actions.
     * @param actions The actions.
     * @return The parse tree.
     */
    private List<ASTPtRootNode> _getParseTrees(AbstractActionsAttribute actions) {
        List<ASTPtRootNode> parseTrees = actions.getParseTreeList();

        Iterator iterator = parseTrees.iterator();
        while (iterator.hasNext()) {
            putAttribute((ASTPtRootNode) iterator.next(), actions);
        }
        return parseTrees;
    }
}
