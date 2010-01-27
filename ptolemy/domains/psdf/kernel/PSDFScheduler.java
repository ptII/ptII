/* A Scheduler for the PSDF domain

 Copyright (c) 2003-2010 The Regents of the University of California.
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
package ptolemy.domains.psdf.kernel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mapss.dif.psdf.PSDFAPGANStrategy;
import mapss.dif.psdf.PSDFEdgeWeight;
import mapss.dif.psdf.PSDFGraph;
import mapss.dif.psdf.PSDFGraphReader;
import mapss.dif.psdf.PSDFGraphs;
import mapss.dif.psdf.PSDFNodeWeight;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.ScheduleElement;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.BaseSDFScheduler;
import ptolemy.graph.Edge;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PSDFScheduler

/**

 A scheduler that implements basic scheduling of PSDF graphs.  PSDF
 scheduling is similar to SDF scheduling, EXCEPT:

 <p> 1) Because parameter values may change, the solution to the
 balance equation is computed symbolically.  i.e. the repetitions
 vector is a function of the parameter values.

 <p> 2) Because the firing vector may change, the schedule determined
 by this class can only be a quasi-static, or parameterized schedule.
 Note that parameterized schedules cannot generally be constructed for
 models with feedback or with unconstrained parameter values.

 <p> This class uses a ConstVariableModelAnalysis to determine which
 scheduling parameters are constants and which may change during
 execution of the model.  Rate parameters that can change are checked
 to ensure that their change context is not strictly contained by the
 model being scheduled.  If this is the case, then the actor is not
 locally synchronous, and cannot be statically scheduled.  Dynamic
 parameters with a valid changed context are treated symbolically when
 computing the repetitions vector.

 <p> After computing a schedule, this scheduler determines the external
 rate of each of the model's external ports.  Since the firing vector
 is only computed symbolically, these rates can also only be computed
 symbolically.  The dependence of these external rates on the rates of
 ports in the model is declared using a DependenceDeclaration.  Higher
 level directors may use this dependence information to determine the
 change context of those rate variables and may refuse to schedule the
 composite actor if those rates imply that this model is not locally
 synchronous.

 <p> This scheduler uses a version of the P-APGAN scheduling algorithm
 described in [1].

 <p> [1] B. Bhattacharya and S. S. Bhattacharyya. Quasi-static scheduling of
 reconfigurable dataflow graphs for DSP systems. In <em> Proceedings of the
 International Workshop on Rapid System Prototyping </em>,
 pages 84-89, Paris, France, June 2000.

 @see ptolemy.actor.sched.Scheduler
 @see ptolemy.domains.sdf.lib.SampleDelay
 @see ptolemy.domains.sdf.kernel.SDFScheduler

 @author Stephen Neuendorffer, Shuvra S. Bhattacharyya
 @version $Id$
 @since Ptolemy II 3.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class PSDFScheduler extends BaseSDFScheduler {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PSDFScheduler() throws IllegalActionException,
            NameDuplicationException {
        super();
        _init();
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PSDFScheduler(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PSDFScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Declare the rate dependency on any external ports of the model.
     *  SDF directors should invoke this method once during preinitialize.
     */
    public void declareRateDependency() throws IllegalActionException {
        // Not necessary, since rates are declared symbolically.
    }

    /** Return a string representation of the buffer sizes of the relations
     *  in the model. This diagnostic method shows the buffer size
     *  expression for each relation along with the relation itself.
     *
     *  @return A string representation of the buffer sizes.
     */
    public String displayBufferSizes() {
        StringBuffer result = new StringBuffer();
        PSDFDirector director = (PSDFDirector) getContainer();
        CompositeActor model = (CompositeActor) director.getContainer();
        Iterator relations = model.relationList().iterator();

        while (relations.hasNext()) {
            Relation relation = (Relation) relations.next();
            Variable variable = (Variable) relation.getAttribute("bufferSize");
            result.append(relation.getName() + ": ");

            if (variable == null) {
                result.append("null");
            } else {
                result.append(variable.getExpression());
            }

            result.append("\n");
        }

        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the parameterized scheduling sequence.
     *  An exception will be thrown if the graph is not schedulable.
     *
     *  @return A schedule of the deeply contained opaque entities
     *  in the firing order.
     *  @exception NotSchedulableException If a parameterized schedule
     *  cannot be derived for the model.
     *  @exception IllegalActionException If the rate parameters
     *  of the model are not correct, or the computed rates for
     *  external ports are not correct.
     */
    protected Schedule _getSchedule() throws NotSchedulableException,
            IllegalActionException {
        PSDFDirector director = (PSDFDirector) getContainer();
        CompositeActor model = (CompositeActor) director.getContainer();

        // Get the vectorization factor.
        String vectorizationFactorExpression = "1";

        String vectorizationName = director.vectorizationFactor.getName(model);
        vectorizationFactorExpression = vectorizationName.replaceAll("\\.",
                "::");

        if (vectorizationFactorExpression.indexOf(" ") != -1) {
            throw new InternalErrorException("The vectorizationFactor "
                    + "PSDFDirector parameter must "
                    + "not have spaces in its value.  The original value "
                    + "was \"" + vectorizationName
                    + "\". Try changing the name of " + "director.");
        }

        PSDFGraphReader graphReader = new PSDFGraphReader();
        PSDFGraph graph = (PSDFGraph) graphReader.convert(model);
        _debug("PSDF graph = \n" + graph.toString());

        if (_debugFlag) {
            graph.printEdgeRateExpressions();
        }

        PSDFAPGANStrategy strategy = new PSDFAPGANStrategy(graph);
        ptolemy.graph.sched.Schedule graphSchedule = strategy.schedule();
        _debug("P-APGAN schedule = \n" + graphSchedule.toString());

        SymbolicScheduleElement resultSchedule = _expandAPGAN(graph, strategy
                .getClusterManager().getRootNode(), strategy);
        resultSchedule.setIterationCount(vectorizationFactorExpression);

        _debug("Final schedule = \n" + resultSchedule.toString());

        if (_debugging) {
            _debug("The buffer size map:\n");

            Iterator relations = _bufferSizeMap.keySet().iterator();

            while (relations.hasNext()) {
                Relation relation = (Relation) relations.next();
                _debug(relation.getName() + ": " + _bufferSizeMap.get(relation)
                        + "\n");
            }
        }

        _saveBufferSizes(_bufferSizeMap);

        // Crazy hack to infer firing counts for each actor.
        try {
            _inferFiringCounts(resultSchedule, null);
        } catch (NameDuplicationException ex) {
            throw new NotSchedulableException(new LinkedList(), ex,
                    "Error recording firing counts");
        }

        // Crazy hack to Infer port production: FIXME: This should be
        // done as part of the APGAN expansion where the rates of
        // external ports are unknown The reason is that it will make
        // rate information propagate from an actor input port to
        // another actors input port that are connected on the inside
        // to the same external input port.  See
        // BaseSDFScheduler.setContainerRates.
        Iterator ports = model.portList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();

            if (_debugging && VERBOSE) {
                _debug("External Port " + port.getName());
            }

            if (port.isInput() && port.isOutput()) {
                throw new NotSchedulableException(port,
                        "External port is both an input and an output, "
                                + "which is not allowed in SDF.");
            } else if (port.isInput()) {
                List sinks = port.insideSinkPortList();

                if (sinks.size() > 0) {
                    IOPort connectedPort = (IOPort) sinks.get(0);
                    Entity entity = (Entity) connectedPort.getContainer();
                    String name = connectedPort.getName(model);
                    String identifier = name.replaceAll("\\.", "::");

                    String sinkExpression;
                    Variable sinkRateVariable = DFUtilities.getRateVariable(
                            connectedPort, "tokenConsumptionRate");

                    if (sinkRateVariable == null) {
                        sinkExpression = "1";
                    } else {
                        sinkExpression = identifier + "::"
                                + sinkRateVariable.getName();
                    }

                    String expression = sinkExpression + " * "
                            + entity.getName() + "::firingsPerIteration";

                    DFUtilities.setExpressionIfNotDefined(port,
                            "tokenConsumptionRate", expression);

                    if (_debugging && VERBOSE) {
                        _debug("Setting tokenConsumptionRate to " + expression);
                    }
                }
            } else if (port.isOutput()) {
                List sources = port.insideSourcePortList();

                if (sources.size() > 0) {
                    IOPort connectedPort = (IOPort) sources.get(0);
                    Entity entity = (Entity) connectedPort.getContainer();
                    String name = connectedPort.getName(model);
                    String identifier = name.replaceAll("\\.", "::");
                    Variable sourceRateVariable = DFUtilities.getRateVariable(
                            connectedPort, "tokenProductionRate");
                    String sourceExpression;

                    if (sourceRateVariable == null) {
                        sourceExpression = "1";
                    } else {
                        sourceExpression = identifier + "::"
                                + sourceRateVariable.getName();
                    }

                    String expression = sourceExpression + " * "
                            + entity.getName() + "::firingsPerIteration";

                    DFUtilities.setExpressionIfNotDefined(port,
                            "tokenProductionRate", expression);

                    if (_debugging && VERBOSE) {
                        _debug("Setting tokenProductionRate to " + expression);
                    }
                }

                // Infer init production.
                // Note that this is a very simple type of inference...
                // However, in general, we don't want to try to
                // flatten this model...
                //  Iterator connectedPorts =
                //                     port.insideSourcePortList().iterator();
                //                 IOPort foundOutputPort = null;
                //                 int inferredRate = 0;
                //                 while (connectedPorts.hasNext()) {
                //                     IOPort connectedPort = (IOPort) connectedPorts.next();
                //                     int newRate;
                //                     if (connectedPort.isOutput()) {
                //                         newRate =
                //                             DFUtilities.getTokenInitProduction(connectedPort);
                //                     } else {
                //                         newRate = 0;
                //                     }
                //                     // If we've already set the rate, then check that the
                //                     // rate for any other internal port is correct.
                //                     if (foundOutputPort != null &&
                //                             newRate != inferredRate) {
                //                         throw new NotSchedulableException(
                //                                 "External output port " + port
                //                                 + " is connected on the inside to ports "
                //                                 + "with different initial production: "
                //                                 + foundOutputPort + " and "
                //                                 + connectedPort);
                //                     }
                //                     foundOutputPort = connectedPort;
                //                     inferredRate = newRate;
                //                 }
                //                 DFUtilities._setIfNotDefined(
                //                         port, "tokenInitProduction", inferredRate);
                //                 if (_debugging && VERBOSE) {
                //                     _debug("Setting tokenInitProduction to "
                //                             + inferredRate);
                //                 }
            } else {
                throw new NotSchedulableException(port,
                        "External port is neither an input and an output, "
                                + "which is not allowed in SDF.");
            }
        }

        // Set the schedule to be valid.
        setValid(true);

        if (resultSchedule instanceof Schedule) {
            return (Schedule) resultSchedule;
        } else {
            // Must be ScheduleElement.
            Schedule schedule = new Schedule();
            schedule.add((ScheduleElement) resultSchedule);
            return schedule;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Evaluate the given parse tree in the scope of the the model
    // being scheduled, resolving "::" scoping syntax inside the
    // model.
    private Token _evaluateExpressionInModelScope(ASTPtRootNode node)
            throws IllegalActionException {
        if (_parseTreeEvaluator == null) {
            _parseTreeEvaluator = new ParseTreeEvaluator();
        }

        if (_parserScope == null) {
            _parserScope = new ScheduleScope();
        }

        Token result = _parseTreeEvaluator
                .evaluateParseTree(node, _parserScope);
        return result;
    }

    // Expand the P-APGAN-clustered graph. The schedule element that is
    // returned has an iteration count of 1. This iteration count expression
    // can be changed by the caller to iterate the schedule computed in
    // this method.
    // @param graph The graph containing the node.
    // @param node The super node to expand.
    // @param apgan The scheduler that was used to build the cluster hierarchy.
    // @return The schedule saving the expansion result.
    private SymbolicScheduleElement _expandAPGAN(PSDFGraph graph,
            ptolemy.graph.Node node, PSDFAPGANStrategy strategy) {
        PSDFGraph childGraph = (PSDFGraph) strategy.getClusterManager()
                .getSubgraph(node);

        try {
            // Atomic node
            if (childGraph == null) {
                PSDFNodeWeight weight = (PSDFNodeWeight) node.getWeight();
                SymbolicFiring firing = new SymbolicFiring((Actor) weight
                        .getComputation(), "1");
                return firing;

                // Super node
            } else {
                // FIXME: why call new Schedule here?
                /*Schedule schedule = */new Schedule();

                // Expand the super node with adjacent nodes contained
                // within it.
                Edge edge = (Edge) childGraph.edges().iterator().next();
                ptolemy.graph.Node source = edge.source();
                ptolemy.graph.Node sink = edge.sink();
                SymbolicScheduleElement first = _expandAPGAN(childGraph,
                        source, strategy);
                SymbolicScheduleElement second = _expandAPGAN(childGraph, sink,
                        strategy);

                // Determine the iteration counts of the source and
                // sink clusters.
                String producedExpression = strategy.producedExpression(edge);
                String consumedExpression = strategy.consumedExpression(edge);

                // These errors should not occur.
                if (producedExpression == null) {
                    throw new RuntimeException("Internal error: null "
                            + "production rate expression. The offending edge "
                            + "follows.\n" + edge);
                } else if (consumedExpression == null) {
                    throw new RuntimeException(
                            "Internal error: null "
                                    + "consumption rate expression. The offending edge "
                                    + "follows.\n" + edge);
                }

                String denominator = PSDFGraphs.gcdExpression(
                        producedExpression, consumedExpression);
                String firstIterations = "(" + consumedExpression + ") / ("
                        + denominator + ")";
                String secondIterations = "(" + producedExpression + ") / ("
                        + denominator + ")";

                first.setIterationCount(firstIterations);
                second.setIterationCount(secondIterations);

                SymbolicSchedule symbolicSchedule = new SymbolicSchedule("1");
                symbolicSchedule.add((ScheduleElement) first);
                symbolicSchedule.add((ScheduleElement) second);

                // Compute buffer sizes and associate them with the
                // corresponding relations.
                Iterator edges = childGraph.edges().iterator();

                while (edges.hasNext()) {
                    Edge nextEdge = (Edge) edges.next();
                    PSDFEdgeWeight weight = (PSDFEdgeWeight) nextEdge
                            .getWeight();
                    IOPort sourcePort = weight.getSourcePort();
                    List relationList = sourcePort.linkedRelationList();

                    if (relationList.size() != 1) {
                        // FIXME: Need to generalize this?
                        throw new RuntimeException("Cannot handle relation "
                                + "lists that are not singletons.\n"
                                + "The size of this relation list is "
                                + relationList.size()
                                + "\nA dump of the offending edge follows.\n"
                                + nextEdge + "\n");
                    }

                    Iterator relations = relationList.iterator();
                    Relation relation = (Relation) relations.next();
                    String produced = strategy.producedExpression(nextEdge);
                    String consumed = strategy.consumedExpression(nextEdge);
                    String bufferSizeExpression = "((" + produced + ") * ("
                            + consumed + ")) / "
                            + PSDFGraphs.gcdExpression(produced, consumed);

                    // Due to the bottom-up traversal in _expandAPGAN,
                    // relations that are linked to multiple sink
                    // nodes will have their buffer sizes
                    // progressively replaced by those of outer
                    // clusterings, and will end up with the buffer
                    // size determined by the outermost clustering.
                    _debug("Associating buffer size expression '"
                            + bufferSizeExpression + "' with relation '"
                            + relation.getName() + "'\n");
                    _bufferSizeMap.put(relation, bufferSizeExpression);
                }

                return symbolicSchedule;
            }
        } catch (Throwable throwable) {
            throw new KernelRuntimeException(throwable,
                    "Error converting cluster hierarchy to " + "schedule.\n");
        }
    }

    private void _inferFiringCounts(SymbolicScheduleElement element,
            String expression) throws IllegalActionException,
            NameDuplicationException {
        String recursiveExpression;

        if (expression == null) {
            recursiveExpression = element.expression();
        } else {
            recursiveExpression = expression + "*" + element.expression();
        }

        if (element instanceof SymbolicFiring) {
            SymbolicFiring firing = (SymbolicFiring) element;
            Entity actor = (Entity) firing.getActor();
            Variable parameter = (Variable) actor
                    .getAttribute("firingsPerIteration");

            if (parameter == null) {
                parameter = new Parameter(actor, "firingsPerIteration");
                parameter.setVisibility(Settable.NOT_EDITABLE);
                parameter.setPersistent(false);
            }

            parameter.setExpression(recursiveExpression);
        } else if (element instanceof SymbolicSchedule) {
            SymbolicSchedule schedule = (SymbolicSchedule) element;

            for (Iterator i = schedule.iterator(); i.hasNext();) {
                _inferFiringCounts((SymbolicScheduleElement) i.next(),
                        recursiveExpression);
            }
        } else {
            throw new RuntimeException("Unexpected Schedule Element");
        }
    }

    // Initialize the object.
    private void _init() {
        if (_debugFlag) {
            addDebugListener(new StreamListener());
        }

        _bufferSizeMap = new HashMap();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An actor firing with an iteration count that is determined by
     *  a symbolic expression.
     */
    private class SymbolicFiring extends Firing implements
            SymbolicScheduleElement {
        /** Construct a firing with the given actor and the given
         *  expression.  The given actor is assumed to fire the number
         *  of times determined by evaluating the given expression.
         *  @param actor The actor in the firing.
         *  @param expression A string expression representing the
         *  number of times to fire the actor.
         */
        public SymbolicFiring(Actor actor, String expression) {
            super(actor);
            setIterationCount(expression);
        }

        /** Return the most recent expression that was used to set the
         *  iteration count of this symbolic firing.
         *  @return The most recent expression.
         *  @see #setIterationCount(String).
         */
        public String expression() {
            return _expression;
        }

        /** Return the current iteration count of this firing.
         */
        public int getIterationCount() {
            try {
                IntToken token = (IntToken) _evaluateExpressionInModelScope(_parseTree);
                _debug("firing " + getActor() + " " + token.intValue()
                        + " times");
                return token.intValue();
            } catch (Exception ex) {
                // FIXME: this isn't very nice.
                throw new RuntimeException(
                        "Error evaluating parse tree for expression" + ": "
                                + expression(), ex);
            }
        }

        /** Get the parse tree of the iteration expression.
         *  @return The parse tree.
         */
        public ASTPtRootNode parseTree() {
            return _parseTree;
        }

        /** Set the expression associated with the iteration count.
         *  The expression will probably be something like
         *  "a2::in::tokenConsumptionRate/gcd(a2::in::tokenConsumptionRate,
         *  a::out::tokenProductionRate)."
         *
         *  @param expression The expression to be associated with the
         *  iteration count.
         */
        public void setIterationCount(String expression) {
            _expression = expression;

            // FIXME: Need better exception handling.
            try {
                PtParser parser = new PtParser();
                _parseTree = parser.generateParseTree(expression);
            } catch (Exception exception) {
                throw new RuntimeException("Error setting iteration count to "
                        + expression + ".\n" + exception.getMessage());
            }
        }

        /**
         * Output a string representation of this symbolic firing.
         */
        public String toString() {
            return "Fire Actor " + getActor().toString() + "[" + expression()
                    + "] times";
        }

        // The iteration expression. This is stored separately for
        // diagnostic purposes only.
        private String _expression;

        // The parse tree of the iteration expression.
        private ASTPtRootNode _parseTree;
    }

    /** A schedule whose iteration count is given by an expression.
     */
    private class SymbolicSchedule extends Schedule implements
            SymbolicScheduleElement {
        /** Construct a symbolic schedule with the given expression.
         *  This schedule is assumed to fire the number of times determined
         *  by evaluating the given expression.
         *  @param expression A string expression representing the
         *  number of times to execute the schedule.
         */
        public SymbolicSchedule(String expression) {
            setIterationCount(expression);
        }

        /** Return the most recent expression that was used to set the
         *  iteration count of this symbolic firing.
         *  @return The most recent expression.
         *  @see #setIterationCount(String).
         */
        public String expression() {
            return _expression;
        }

        /** Return the current iteration count of this symbolic schedule.
         */
        public int getIterationCount() {
            try {
                IntToken token = (IntToken) _evaluateExpressionInModelScope(_parseTree);
                return token.intValue();
            } catch (Exception ex) {
                // FIXME: this isn't very nice.
                throw new RuntimeException(
                        "Error evaluating parse tree for expression" + ": "
                                + expression(), ex);
            }
        }

        /** Get the parse tree of the iteration expression.
         *  @return The parse tree.
         */
        public ASTPtRootNode parseTree() {
            return _parseTree;
        }

        /** Set the expression associated with the iteration count.
         *  The expression will probably be something like
         *  "a2::in::tokenConsumptionRate/gcd(a2::in::tokenConsumptionRate,
         *  a::out::tokenProductionRate)."
         *
         *  @param expression The expression to be associated with the
         *  iteration count.
         */
        public void setIterationCount(String expression) {
            _expression = expression;

            // FIXME: Need better exception handling.
            try {
                PtParser parser = new PtParser();
                _parseTree = parser.generateParseTree(expression);
            } catch (Exception exception) {
                throw new RuntimeException("Error setting iteration count to "
                        + expression + ".\n" + exception.getMessage());
            }
        }

        /** Return a string representation of this symbolic schedule.
         *  @return The string representation.
         */
        public String toString() {
            StringBuffer result = new StringBuffer(
                    "Execute Symbolic Schedule{\n");
            Iterator elements = iterator();

            while (elements.hasNext()) {
                ScheduleElement element = (ScheduleElement) elements.next();
                result.append(element + "\n");
            }

            result.append("}");
            result.append("[" + expression() + "] times");
            return result.toString();
        }

        // The iteration expression. This is stored separately for
        // diagnostic purposes only.
        private String _expression;

        // The parse tree of the iteration expression.
        private ASTPtRootNode _parseTree;
    }

    /** An interface for schedule elements whose iteration counts are
     *  in terms of symbolic expressions.
     */
    private interface SymbolicScheduleElement {
        // FIXME: populate with more methods as appropriate.

        /** Return the most recent expression that was used to set the
         *  iteration count of this symbolic firing.
         *  @return The most recent expression.
         *  @see #setIterationCount(String).
         */
        public String expression();

        /** Get the parse tree of the iteration expression.
         *  @return The parse tree.
         */
        public ASTPtRootNode parseTree();

        /** Set the expression associated with the iteration count.
         *  The expression will probably be something like
         *  "a2::in::tokenConsumptionRate/gcd(a2::in::tokenConsumptionRate,
         *  a::out::tokenProductionRate)."
         *
         *  @param expression The expression to be associated with the
         *  iteration count.
         */
        public void setIterationCount(String expression);
    }

    private class ScheduleScope extends ModelScope {
        /** Construct a scope consisting of the variables
         *  of the container of the the enclosing instance of
         *  Variable and its containers and their scope-extending
         *  attributes.
         */
        public ScheduleScope() {
        }

        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.Token get(String name)
                throws IllegalActionException {
            PSDFDirector director = (PSDFDirector) getContainer();
            CompositeActor reference = (CompositeActor) director.getContainer();
            Variable result = getScopedVariable(null, reference, name);

            if (result != null) {
                return result.getToken();
            } else {
                return null;
            }
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            PSDFDirector director = (PSDFDirector) getContainer();
            CompositeActor reference = (CompositeActor) director.getContainer();
            Variable result = getScopedVariable(null, reference, name);

            if (result != null) {
                return result.getType();
            } else {
                return null;
            }
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            PSDFDirector director = (PSDFDirector) getContainer();
            CompositeActor reference = (CompositeActor) director.getContainer();
            Variable result = getScopedVariable(null, reference, name);

            if (result != null) {
                return result.getTypeTerm();
            } else {
                return null;
            }
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of variable names within the scope.
         */
        public Set identifierSet() {
            PSDFDirector director = (PSDFDirector) getContainer();
            NamedObj reference = director.getContainer();
            return getAllScopedVariableNames(null, reference);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A map from relations into expressions that give symbolic buffer sizes
    // of the relations. Keys are of type Relation and values are of type
    // String.
    private HashMap _bufferSizeMap;

    private boolean _debugFlag = false;

    private ParseTreeEvaluator _parseTreeEvaluator;

    private ParserScope _parserScope;
}
