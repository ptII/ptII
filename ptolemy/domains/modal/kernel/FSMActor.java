/* An actor containing a finite state machine (FSM).

 Copyright (c) 1999-2009 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.DefaultCausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.UndefinedConstantOrIdentifierException;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.HasTypeConstraints;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.domains.ptera.kernel.PteraModalModel;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FSMActor

/**
 An FSMActor contains a set of states and transitions. A transition has
 a <i>guard expression</i>, any number of <i>output actions</i>, and any
 number of <i>set actions</i>. It has an <i>initial state</i>, which is
 the unique state whose <i>isInitialState</i> parameter is true.
 In outline, a firing of this actor is a sequence of steps as
 follows. In the fire() method:
 <ol>
 <li> Read inputs.
 <li> Evaluate guards on outgoing transitions of the current state.
 <li> Choose a transitions whose guard is true.
 <li> Execute the output actions.
 </ol>
 In the postfire() method:
 <ol>
 <li> Execute the set actions of the chosen transition.
 <li> Change the current state to the destination of the chosen transition.
 </ol>
 The fire() method may be invoked more than once
 in an iteration, for example in a fixedpoint iteration.
 This actor makes no persistent state changes in
 its fire()  method, so actor conforms
 with the <i>actor abstract semantics</i>, and hence can be used in any
 Ptolemy II domain.
 <p>
 After reading the inputs, this actor examines
 the outgoing transitions of the current state, evaluating their
 guard expressions. A transition is <i>enabled</i> if its guard
 expression evaluates to true. A blank guard expression is
 interpreted to be always true. The guard expression may refer to any
 input port and any variable in scope.
 <p>
 If an input port name <i>portName</i> is used in a guard expression,
 it refers to the current input on that port on channel zero.
 If the input port status is not known, or if the input is absent,
 then a guard expression referring to <i>portName</i> will not be evaluated.
 The guard expression may alternatively refer to <i>portName</i>_<i>isPresent</i>,
 which is a boolean that is true if an input is present on the specified
 port. Again, if the input port status is not known, such a guard
 would not be evaluated. The status of an input port may not be
 known during firings under a director with fixed-point semantics,
 such as SR or Continuous.
 <p>
 To refer to a channel specificatlly, a guard expression may use
 <i>portName</i>_<i>channelIndex</i>, which has value equal to the token
 received on the port on the given channel. Similarly, it may refer
 to <i>portName</i>_<i>channelIndex</i>_<i>isPresent</i>.
 <p>
 FIXME: Document multirate behavior.
 <p>
 The identifier <i>portName</i>Array or
 <i>portName</i>_<i>channelIndex</i>Array refers the
 array of all tokens consumed from the port in the last firing.  This
 identifier has an array type whose element type is the type of the
 corresponding input port.
 <p>
 Nondeterministic transitions are allowed if all enabled transitions
 are marked <i>nondeterministic</i>. If more than one transition is
 enabled and they are all marked nondeterministic, then one is chosen
 at random in the fire() method. If the fire() method is invoked more
 than once in an iteration, then subsequent invocations in the same
 iteration will always choose the same transition.  However,
 if more transitions become enabled in subsequent firings and
 they are not all marked nondeterminate, then an
 exception will thrown. Note that this
 means that if some input is unknown on the first invocation
 of fire(), and a guard refers to that input, then that transition
 will not be chosen. As a consequence, for nondeterministic state
 machines, the behavior may depend on the order of firings in
 a fixed-point iteration. This is in fact unavoidable (it is
 related to the celebrated Brock-Ackerman anomaly, which demonstrates
 that the input/output relations of a nondeterministic system do
 not completely determine its behavior; the context in which it
 is used can also affect the behavior; specifically, the context
 may make it impossible to know the value of input on the first
 invocation of fire() because of a feedback loop).
 <p>
 If no transition is
 enabled and all their guard expressions have been evaluated (all relevant
 inputs are known), then if there is a transition marked as a
 <i>default transition</i>, then that transition is chosen. If
 there is more than one default transition and they are all marked
 nondeterministic, then one is chosen at random.
 <p>
 Once a transition is chosen, its output actions are executed.
 Typically, these will write values to output ports. The form of an output
 action is typically <i>y</i> = <i>expression</i>, where expression may
 refer to any variable defined as above or any parameter in scope
 (and also to outputs of state refinements, see below).
 This gives the behavior of a Mealy machine, where
 outputs are produced by transitions rather than by states. Moore machine
 behavior is also achievable using state refinements that produce
 outputs (see FSMDirector documentation).
 Multiple output actions may be given by separating them with semicolons.
 Also, output actions may take the form of <i>d.p</i> = <i>expression</i>,
 where <i>d</i> is the name of the destination state and <i>p</i> is a
 parameter of the destination refinement.
  <p>
 After a transition is taken, this actor calls fireAtCurrentTime()
 on its enclosing director. This ensures that if the destination
 state has an enabled transition, that transition will be taken
 at the same time (in the next superdense time index). It also
 supports continuous-time models, where the destination state
 refinement, if any, should produce an output at the next superdense
 time index.
 <p>
 A final state is a state that has its <i>isFinalState</i> parameter
 set to true. When the actor reaches a final state, then the
 postfire method will return false, indicating that the actor does not
 wish to be fired again.
 <p>
 An FSMActor can be used in a modal model to represent the mode
 control logic.  In this case, the states and transitions have
 refinements, and this actor works in concert with the FSMDirector
 to execute those refinements. See the documentation for
 FSMDirector for details on how that works.
 <p>
 By default, this actor has a conservative causality interface,
 implemented by the {@link DefaultCausalityInterface}, which declares
 that all outputs depend on all inputs. If, however, the enclosing
 director and all state refinement directors implement the
 strict actor semantics (as indicated by their
 implementsStrictActorSemantics() method), then the returned
 causality interface is
 implemented by the {@link FSMCausalityInterface} class. If
 the <i>stateDependentCausality</i> is false (the default),
 then this causality interface in conservative and valid in all
 states. If it is true, then the causality interface will show
 different input/output dependencies depending on the state.
 See {@link FSMCausalityInterface} for details.

 @author Edward A. Lee, Xiaojun Liu, Haiyang Zheng, Ye Zhou
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Yellow (kienhuis)
 @see State
 @see Transition
 @see Action
 @see FSMDirector
 */
public class FSMActor extends CompositeEntity implements TypedActor,
        ExplicitChangeContext {
    /** Construct an FSMActor in the default workspace with an empty string
     *  as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public FSMActor() {
        super();
        _init();
    }

    /** Create an FSMActor in the specified container with the specified
     *  name. The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public FSMActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct an FSMActor in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public FSMActor(Workspace workspace) {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified object to the list of objects whose
     *  preinitialize(), intialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#addPiggyback(Executable)
     */
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
    }

    /** Return an enabled transition among the given list of transitions
     *  for which both the guard expression and the output actions can
     *  be evaluated (the inputs referred by these are known).
     *  If there is only one transition enabled, return that transition.
     *  In case there are multiple enabled transitions, if any of
     *  them is not nondeterministic, throw an exception. See {@link Transition}
     *  for the explanation of "nondeterministic". Otherwise, randomly choose
     *  one from the enabled transitions and return it if the output actions
     *  can be evaluated.
     *  Execute the output actions contained by the returned
     *  transition before returning.
     *  @param transitionList A list of transitions.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled and not all of them are nondeterministic.
     */
    public Transition chooseTransition(List transitionList)
            throws IllegalActionException {

        List<Transition> enabledTransitions = enabledTransitions(transitionList);
        int length = enabledTransitions.size();

        // Ensure that if there are multiple enabled transitions, all of them
        // are nondeterministic.
        if (length > 1) {
            for (Transition enabledTransition : enabledTransitions) {
                if (!enabledTransition.isNondeterministic()) {
                    throw new MultipleEnabledTransitionsException(
                            currentState(),
                            "Nondeterministic FSM error: "
                                    + "Multiple enabled transitions found but not all"
                                    + " of them are nondeterministic. Transition "
                                    + enabledTransition.getName()
                                    + " is deterministic.");
                }
            }
        }

        // If a transition has been previously chosen in this iteration,
        // then we have to return the same transition here. Otherwise we
        // get a very odd bug with nondeterminate machines where an output
        // action may be executed on a transition that is not taken.
        // NOTE: We have to be careful to not mask nondeterminism! In particular, if
        // two transitions are enabled, but on a previous invocation fire()
        // only one of the two guards could be evaluated (because inputs
        // were not known), then no nondeterminism would be detected.
        // This is a problem because we could have two valid schedules,
        // one of which detects nondeterminism and one of which does not.
        // We guard against that by checking above for multiple enabled
        // transitions. That check must be done before returning.
        if (_lastChosenTransition != null) {
            // Guard against non-monotonic behavior.
            // NOTE: This check means that it is essential that if a guard
            // evaluates to true at one firing, it must evaluate to true at
            // all subsequent firings in the same iteration. This creates
            // a complication for Continuous, where time advances between
            // steps, so guards may become false again. We have fixed this
            // by evaluating guards only at the last stage of iteration.
            if (!enabledTransitions.contains(_lastChosenTransition)) {
                throw new IllegalActionException(this, _lastChosenTransition,
                        "Transition was enabled in an earlier firing of this "
                                + "iteration, but is no longer enabled!");
            }
            return _lastChosenTransition;
        }
        Transition result = null;

        if (length == 1) {
            result = (Transition) enabledTransitions.get(0);
        } else if (length > 1) {
            // Randomly choose one transition from the list of the
            // enabled transitions. Note that it is possible that the
            // chosen transition cannot be executed because inputs needed
            // by its output actions are not known.
            // In that case, we have to choose a different transition.
            while (enabledTransitions.size() > 0) {
                // Since the size of the list of enabled transitions usually (almost
                // always) is less than the maximum value of integer. We can safely
                // do the cast from long to int in the following statement.
                int randomChoice = (int) Math.floor(Math.random() * length);

                // There is a tiny chance that randomChoice equals length.
                // When this happens, we deduct 1 from the randomChoice.
                if (randomChoice == length) {
                    randomChoice--;
                }

                result = (Transition) enabledTransitions.get(randomChoice);
                if (_referencedInputPortsByOutputKnown(result)) {
                    break;
                } else {
                    // Cannot make this choice.
                    enabledTransitions.remove(result);
                    result = null;
                }
            }
        }

        if (result != null) {
            if (_debugging) {
                _debug("Enabled transition: ", result.getFullName());
            }

            Iterator actions = result.choiceActionList().iterator();

            while (actions.hasNext()) {
                Action action = (Action) actions.next();
                action.execute();
            }
            
            // If the current state has no refinement and there
            // outputs that remain unknown, make them absent.
            if (_currentState.getRefinement() == null) {
                List<IOPort> outputs = outputPortList();
                for (IOPort port : outputs) {
                    for (int channel = 0; channel < port.getWidth(); channel++) {
                        if (!port.isKnown(channel)) {
                            port.sendClear(channel);
                        }
                    }
                }
            }
        }
        // Commit to this transition even if it cannot be
        _lastChosenTransition = result;

        return result;
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new actor.
     *  @param workspace The workspace for the new actor.
     *  @return A new FSMActor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        List<Initializable> oldInitializables = _initializables;
        _initializables = null;
        FSMActor newObject = (FSMActor) super.clone(workspace);
        _initializables = oldInitializables;

        newObject._currentState = null;
        newObject._identifierToPort = new HashMap<String, Port>();
        newObject._inputTokenMap = new HashMap();
        newObject._lastChosenTransition = null;

        if (_initialState != null) {
            newObject._initialState = (State) newObject.getEntity(_initialState
                    .getName());
        }

        newObject._inputPortsVersion = -1;
        newObject._cachedInputPorts = null;
        newObject._outputPortsVersion = -1;
        newObject._cachedOutputPorts = null;
        newObject._causalityInterface = null;
        newObject._causalityInterfaces = null;
        newObject._causalityInterfacesVersions = null;
        newObject._causalityInterfaceDirector = null;
        newObject._connectionMaps = null;
        newObject._connectionMapsVersion = -1;
        newObject._currentConnectionMap = null;
        newObject._receiversVersion = -1;
        newObject._receiversVersion = -1;
        newObject._tokenListArrays = null;

        return newObject;
    }

    /** Create receivers for each input port. In case the receivers
     *  don't need to be created they are reset
     *  @exception IllegalActionException If any port throws it.
     */
    public void createReceivers() throws IllegalActionException {
        if (_receiversVersion != workspace().getVersion()) {
            _createReceivers();
            _receiversVersion = workspace().getVersion();
        } else {
            _resetReceivers();
        }

        _receiversVersion = workspace().getVersion();
    }

    /** Return the current state of this actor.
     *  @return The current state of this actor.
     */
    public State currentState() {
        return _currentState;
    }

    /** Return a list of enabled transitions among the given list of
     *  transitions. This includes all transitions whose guards can
     *  can be evaluated and evaluate to true, plus, if all guards can
     *  be evaluated and evaluate to false, all default transitions.
     *  @param transitionList A list of transitions.
     *  @return A list of enabled transition.
     *  @exception IllegalActionException If the guard expression of any
     *  transition can not be evaluated.
     */
    public List enabledTransitions(List transitionList)
            throws IllegalActionException {
        LinkedList enabledTransitions = new LinkedList();
        LinkedList defaultTransitions = new LinkedList();

        Iterator transitionRelations = transitionList.iterator();

        boolean foundUnknown = false;
        while (transitionRelations.hasNext() && !_stopRequested) {
            Transition transition = (Transition) transitionRelations.next();
            if (transition.isDefault()) {
                if (transition.isEnabled()) {
                    defaultTransitions.add(transition);
                }
            } else {
                foundUnknown = foundUnknown || !_referencedInputPortsByGuardKnown(transition);
                // Try to evaluate the guard whether the inputs are known
                // or not. An unknown input might be in a part of the
                // guard expression that is not evaluated, e.g. if the
                // guard expression is "true || in == 1".
                try {
                    if (transition.isEnabled()) {
                        enabledTransitions.add(transition);
                    }
                } catch (RuntimeException ex) {
                    if (!foundUnknown) {
                        // All referenced inputs are known, so the exception is real.
                        throw ex;
                    }
                } catch (IllegalActionException ex) {
                    if (!foundUnknown) {
                        // All referenced inputs are known, so the exception is real.
                        throw ex;
                    }
                }
            }
        }

        // NOTE: It is the chooseTransition method that decides which
        // enabled transition is actually taken. This method simply returns
        // all enabled transitions.
        if (enabledTransitions.size() > 0) {
            return enabledTransitions;
        } else {
            // No enabled transitions. Check for default transitions.
            // Default transitions cannot become enabled until all
            // guard expressions can be evaluated.
            if (!foundUnknown) {
                // If there is no default transition,
                // the current state has no refinement, and there
                // outputs that remain unknown, make them absent.
                if (defaultTransitions.size() == 0 && _currentState.getRefinement() == null) {
                    List<IOPort> outputs = outputPortList();
                    for (IOPort port : outputs) {
                        for (int channel = 0; channel < port.getWidth(); channel++) {
                            if (!port.isKnown(channel)) {
                                port.sendClear(channel);
                            }
                        }
                    }
                }
                return defaultTransitions;
            }
        }
        // No enabled transitions were found, but some are not yet
        // known to disabled, so we cannot return a transition (even the
        // default transition).
        return new LinkedList();
    }

    /** Write this FSMActor into the output writer as a submodel. All
     *  refinements of the events in this FSMActor will be exported as
     *  configurations of those events, not as composite entities belonging to
     *  the closest modal model.
     *
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     */
    public void exportSubmodel(Writer output, int depth, String name)
            throws IOException {
        try {
            List<State> stateList = deepEntityList();
            for (State state : stateList) {
                state.saveRefinementsInConfigurer.setToken(BooleanToken.TRUE);
            }
            if (depth == 0 && getContainer() != null) {
                output.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE " + _elementName + " PUBLIC "
                        + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                        + "    \"http://ptolemy.eecs.berkeley.edu"
                        + "/xml/dtd/MoML_1.dtd\">\n");
            }
            super.exportMoML(output, depth, name);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(this, e, "Unable to set "
                    + "attributes for the states.");
        } finally {
            List<State> stateList = deepEntityList();
            for (State state : stateList) {
                try {
                    state.saveRefinementsInConfigurer
                            .setToken(BooleanToken.FALSE);
                } catch (IllegalActionException e) {
                    // Ignore.
                }
            }
        }
    }

    /** Set the values of input variables. Choose the enabled transition
     *  among the outgoing transitions of the current state. Throw an
     *  exception if there is more than one transition enabled.
     *  Otherwise, execute the output actions contained by the chosen
     *  transition.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    public void fire() throws IllegalActionException {
        // NOTE: this method is not called in the FSMDirector class.
        readInputs();
        List transitionList = _currentState.outgoingPort.linkedRelationList();
        chooseTransition(transitionList);
    }

    /** Notify the refinements of the current state, if any,
     *  that a {@link Director#fireAt(Actor,Time)}
     *  request was skipped, and that current time has passed the
     *  requested time. A director calls this method when in a modal
     *  model it was inactive at the time of the request, and it
     *  became active again after the time of the request had
     *  expired. This base class delegates the current state refinements,
     *  if there are any.
     *  @param time The time of the request that was skipped.
     *  @exception IllegalActionException If skipping the request
     *   is not acceptable to a refinement.
     */
    public void fireAtSkipped(Time time) throws IllegalActionException {
        Actor[] actors = _currentState.getRefinement();
        if (actors != null) {
            for (int i = 0; i < actors.length; i++) {
                actors[i].fireAtSkipped(time);
            }
        }
    }

    /** Return a causality interface for this actor. This
     *  method returns an instance of class
     *  {@link FSMCausalityInterface} if the enclosing director
     *  returns true in its implementsStrictActorSemantics() method.
     *  Otherwise, it returns an interface of class
     *  {@link DefaultCausalityInterface}.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    public CausalityInterface getCausalityInterface() {
        Director director = getDirector();
        Dependency defaultDependency = BooleanDependency.OTIMES_IDENTITY;
        if (director != null) {
            defaultDependency = director.defaultDependency();
            if (!director.implementsStrictActorSemantics()) {
                if (_causalityInterface != null
                        && _causalityInterfaceDirector == director) {
                    return _causalityInterface;
                }
                _causalityInterface = new DefaultCausalityInterface(this,
                        defaultDependency);
                _causalityInterfaceDirector = director;
                return _causalityInterface;
            }
        }
        boolean stateDependent = false;
        try {
            stateDependent = ((BooleanToken) stateDependentCausality.getToken())
                    .booleanValue();
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        if (!stateDependent) {
            if (_causalityInterface != null
                    && _causalityInterfaceDirector == director) {
                return _causalityInterface;
            }
            _causalityInterface = new FSMCausalityInterface(this,
                    defaultDependency);
            _causalityInterfaceDirector = director;
            return _causalityInterface;
        }
        // We need to return a different causality interface for each state.
        // Construct one for the current state if necessary.
        if (_causalityInterfacesVersions == null) {
            _causalityInterfacesVersions = new HashMap<State, Long>();
            _causalityInterfaces = new HashMap<State, FSMCausalityInterface>();
        }
        Long version = _causalityInterfacesVersions.get(_currentState);
        FSMCausalityInterface causality = _causalityInterfaces
                .get(_currentState);
        if (version == null || causality == null
                || version.longValue() != workspace().getVersion()) {
            // Need to create or update a causality interface for the current state.
            causality = new FSMCausalityInterface(this, defaultDependency);
            _causalityInterfaces.put(_currentState, causality);
            _causalityInterfacesVersions.put(_currentState, Long
                    .valueOf(workspace().getVersion()));
        }
        return causality;
    }

    /**
     * Return the change context being made explicit.  This class returns
     * this.
     * @return The change context being made explicit
     */
    public Entity getContext() {
        return this;
    }

    /** Return the director responsible for the execution of this actor.
     *  In this class, this is always the executive director.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    public Director getDirector() {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container instanceof CompositeActor) {
            return ((CompositeActor) container).getDirector();
        }

        return null;
    }

    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        return getDirector();
    }

    /** Return the initial state of this actor. The initial state is
     *  the unique state with its <i>isInitialState</i> parameter set
     *  to true. An exception is thrown if this actor does not contain
     *  an initial state.
     *  This method is read-synchronized on the workspace.
     *  @return The initial state of this actor.
     *  @exception IllegalActionException If this actor does not contain
     *   a state with the specified name.
     */
    public State getInitialState() throws IllegalActionException {
        // For backward compatibility, if the initialStateName
        // parameter and has been given, then use it to determine
        // the initial state.
        String name = initialStateName.getExpression();
        if (!name.equals("")) {
            try {
                workspace().getReadAccess();
                State state = (State) getEntity(name);
                if (state == null) {
                    throw new IllegalActionException(this, "Cannot find "
                            + "initial state with name \"" + name + "\".");
                }
                state.isInitialState.setToken("true");
                state.isInitialState.setPersistent(true);
                _initialState = state;
                return _initialState;
            } finally {
                workspace().doneReading();
            }
        }
        if (_initialState == null) {
            throw new IllegalActionException(this,
                    "No initial state has been specified.");
        }
        return _initialState;
    }

    /** Get the last chosen transition.
     *  @return The last chosen transition.
     *  @see #setLastChosenTransition(Transition)
     */
    public Transition getLastChosenTransition() {
        return _lastChosenTransition;
    }

    /** Return the Manager responsible for execution of this actor,
     *  if there is one. Otherwise, return null.
     *  @return The manager.
     */
    public Manager getManager() {
        try {
            _workspace.getReadAccess();

            CompositeEntity container = (CompositeEntity) getContainer();

            if (container instanceof CompositeActor) {
                return ((CompositeActor) container).getManager();
            }

            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of variables that this entity modifies.  The
     * variables are assumed to have a change context of the given
     * entity.  This method returns the destinations of all choice and
     * commit identifiers that are deeply contained by this actor.
     * Note that this actor is also used as the controller of modal
     * models and FSMDirector reports destinations of all choice and
     * commit identifiers, even those not contained by the finite
     * state machine.
     * @return A list of variables.
     * @exception IllegalActionException If a valid destination object can not
     * be found.
     * @see FSMDirector#getModifiedVariables()
     */
    public List getModifiedVariables() throws IllegalActionException {
        List list = new LinkedList();

        // Collect assignments from FSM transitions
        for (Iterator states = entityList().iterator(); states.hasNext();) {
            State state = (State) states.next();

            for (Iterator transitions = state.outgoingPort.linkedRelationList()
                    .iterator(); transitions.hasNext();) {
                Transition transition = (Transition) transitions.next();

                for (Iterator actions = transition.choiceActionList()
                        .iterator(); actions.hasNext();) {
                    AbstractActionsAttribute action = (AbstractActionsAttribute) actions
                            .next();

                    for (Iterator names = action.getDestinationNameList()
                            .iterator(); names.hasNext();) {
                        String name = (String) names.next();
                        NamedObj object = action.getDestination(name);

                        if (object instanceof Variable && deepContains(object)) {
                            list.add(object);
                        }
                    }
                }

                for (Iterator actions = transition.commitActionList()
                        .iterator(); actions.hasNext();) {
                    AbstractActionsAttribute action = (AbstractActionsAttribute) actions
                            .next();

                    for (Iterator names = action.getDestinationNameList()
                            .iterator(); names.hasNext();) {
                        String name = (String) names.next();
                        NamedObj object = action.getDestination(name);

                        if (object instanceof Variable && deepContains(object)) {
                            list.add(object);
                        }
                    }
                }
            }
        }

        return list;
    }

    /** Return a scope object that has current values from input ports
     *  of this FSMActor in scope.  This scope is used to evaluate
     *  guard expressions and set and output actions.
     *  @return A scope object that has current values from input ports of
     *  this FSMActor in scope.
     */
    public ParserScope getPortScope() {
        // FIXME: this could be cached.
        return new PortScope();
    }

    /** Test whether new input tokens have been received at the input ports.
     *
     *  @return true if new input tokens have been received.
     */
    public boolean hasInput() {
        Iterator<?> inPorts = ((PteraModalModel) getContainer())
                .inputPortList().iterator();
        while (inPorts.hasNext() && !_stopRequested) {
            Port port = (Port) inPorts.next();
            if (hasInput(port)) {
                return true;
            }
        }
        return false;
    }

    /** Test whether new input tokens have been received at the given input
     *  port.
     *
     *  @param port The input port.
     *  @return true if new input tokens have been received.
     */
    public boolean hasInput(Port port) {
        Token token = (Token) _inputTokenMap.get(port.getName() + "_isPresent");
        return token != null && BooleanToken.TRUE.equals(token);
    }

    /** Initialize this actor by setting the current state to the
     *  initial state.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {

        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }

        // Even though reset() is called in preinitialize(),
        // we have to call it again because if a reset transition is
        // taken, preinitialize() is not called.
        reset();

        _lastChosenTransition = null;

        // If there is a transition enabled in the initial state, then request a
        // refiring at the current time. The initial state may be transient.
        //
        // There is no current state when the FSMActor is in fact a Ptera
        // controller. (tfeng 05/12/2009)
        if (_currentState != null) {
            List transitionList = _currentState.outgoingPort
                    .linkedRelationList();
            try {
                List enabledTransitions = enabledTransitions(transitionList);
                if (enabledTransitions.size() > 0) {
                    if (_debugging) {
                        _debug("FSMActor requesting refiring by at "
                                + getDirector().getModelTime());
                    }
                    getDirector().fireAtCurrentTime(this);
                }
            } catch (UndefinedConstantOrIdentifierException ex) {
                // An identifier in a guard expression could not be evaluated.
                // We interpret this to mean that we should not fire at time zero.
                // An alternative would be to always request a firing at time zero.
                // Would that be correct?
            }
        }
    }

    /** Return a list of the input ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of input IOPort objects.
     */
    public List inputPortList() {
        if (_inputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();

                // Update the cache.
                LinkedList inPorts = new LinkedList();
                Iterator ports = portList().iterator();

                while (ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();

                    if (p.isInput()) {
                        inPorts.add(p);
                    }
                }

                _cachedInputPorts = inPorts;
                _inputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }

        return _cachedInputPorts;
    }

    /** Return false. During the fire() method, if a transition is enabled,
     *  it will be taken and the actions associated with this transition are
     *  executed. We assume the actions will change states of this actor.
     *
     *  @return False.
     */
    public boolean isFireFunctional() {
        return false;
    }

    /** Return true.
     *  @return True.
     */
    public boolean isOpaque() {
        return true;
    }

    /** Return false. This actor checks inputs to see whether
     *  they are known before evaluating guards, so it can fired
     *  even if it has unknown inputs.
     *  @return False.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean isStrict() throws IllegalActionException {
        return false;
        /* NOTE: This used to return a value as follows based
         * on the causality interface. But this is conservative
         * and prevents using the actor in some models.
        CausalityInterface causality = getCausalityInterface();
        int numberOfOutputs = outputPortList().size();
        Collection<IOPort> inputs = inputPortList();
        for (IOPort input : inputs) {
            // If the input is also output, skip it.
            // This is the output of a refinement.
            if (input.isOutput()) {
                continue;
            }
            try {
                if (causality.dependentPorts(input).size() < numberOfOutputs) {
                    return false;
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        }
        return true;
        */
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED. If stop() is called while
     *  this is executing, then cease executing and return STOP_ITERATING.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    public int iterate(int count) throws IllegalActionException {
        int n = 0;

        while ((n++ < count) && !_stopRequested) {
            if (prefire()) {
                fire();

                if (!postfire()) {
                    return STOP_ITERATING;
                }
            } else {
                return NOT_READY;
            }
        }

        if (_stopRequested) {
            return Executable.STOP_ITERATING;
        } else {
            return Executable.COMPLETED;
        }
    }

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            //TypedIOPort p = new TypedIOPort(this, name);
            //return p;
            return new TypedIOPort(this, name);
        } catch (IllegalActionException ex) {
            // This exception should not occur.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: "
                            + ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return a new receiver obtained from the director.
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }

        return director.newReceiver();
    }

    /** Create a new instance of Transition with the specified name in
     *  this actor, and return it.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the new transition.
     *  @return A transition with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a transition already in this actor.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();

            //Director director = getDirector();
            Transition tr = new Transition(this, name);
            return tr;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return a list of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of output IOPort objects.
     */
    public List outputPortList() {
        if (_outputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                _cachedOutputPorts = new LinkedList();

                Iterator ports = portList().iterator();

                while (ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();

                    if (p.isOutput()) {
                        _cachedOutputPorts.add(p);
                    }
                }

                _outputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }

        return _cachedOutputPorts;
    }

    /** Execute actions on the last chosen transition. Change state
     *  to the destination state of the last chosen transition.
     *  @return True, unless stop() has been called, in which case, false.
     *  @exception IllegalActionException If any action throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _commitLastChosenTransition();
        _lastChosenTransition = null;
        return !_reachedFinalState && !_stopRequested;
    }

    /** Return true.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Create receivers and input variables for the input ports of
     *  this actor, and validate attributes of this actor, and
     *  attributes of the ports of this actor. Set current state to
     *  the initial state.
     *  @exception IllegalActionException If this actor does not contain an
     *   initial state.
     */
    public void preinitialize() throws IllegalActionException {
        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }

        _stopRequested = false;
        _reachedFinalState = false;

        _newIteration = true;
        _tokenListArrays = new Hashtable();

        _inputTokenMap.clear();

        // In case any further static analysis depends on the initial
        // state, reset to that state here.
        reset();
    }

    /** Set the value of the shadow variables for input ports of this actor.
     *  @exception IllegalActionException If a shadow variable cannot take
     *   the token read from its corresponding channel (should not occur).
     */
    public void readInputs() throws IllegalActionException {
        Iterator inPorts = inputPortList().iterator();

        while (inPorts.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) inPorts.next();
            int width = p.getWidth();

            for (int channel = 0; channel < width; ++channel) {
                _readInputs(p, channel);
            }
        }
    }

    /** Set the input variables for channels that are connected to an
     *  output port of the refinement of current state.
     *  @exception IllegalActionException If a value variable cannot take
     *   the token read from its corresponding channel.
     */
    public void readOutputsFromRefinement() throws IllegalActionException {
        Iterator inPorts = inputPortList().iterator();

        while (inPorts.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) inPorts.next();
            int width = p.getWidth();

            for (int channel = 0; channel < width; ++channel) {
                if (_isRefinementOutput(p, channel)) {
                    _readInputs(p, channel);
                }
            }
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), intialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#removePiggyback(Executable)
     */
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    /** Reset current state to the initial state.
     *  @exception IllegalActionException If thrown while
     *  getting the initial state or setting the current connection map.
     */
    public void reset() throws IllegalActionException {
        _currentState = getInitialState();
        if (_debugging && _currentState != null) {
            _debug("Resetting to initial state: " + _currentState.getName());
        }
        _setCurrentConnectionMap();
    }

    /** Set the last chosen transition.
     *  @param transition The last chosen transition.
     *  @see #getLastChosenTransition()
     */
    public void setLastChosenTransition(Transition transition) {
        // This method is used by the TDL director.
        _lastChosenTransition = transition;
    }

    /** Set the flag indicating whether we are at the start of
     *  a new iteration (firing).  Normally, the flag is set to true.
     *  It is only set to false in HDF.
     *  @param newIteration A boolean variable indicating whether this is
     *  a new iteration.
     */
    public void setNewIteration(boolean newIteration) {
        _newIteration = newIteration;
    }

    /** Set true indicating that this actor supports multirate firing.
     *  @param supportMultirate A boolean variable indicating whether this
     *  actor supports multirate firing.
     */
    public void setSupportMultirate(boolean supportMultirate) {
        _supportMultirate = supportMultirate;
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible.  In this class, we set a flag indicating that
     *  this request has been made (the protected variable _stopRequested).
     *  This will result in postfire() returning false.
     */
    public void stop() {
        _stopRequested = true;
    }

    /** Do nothing.
     */
    public void stopFire() {
    }

    /** Call stop().
     */
    public void terminate() {
        stop();
    }

    /** Return the type constraints of this actor. The constraints
     *  have the form of a set of inequalities. This method first
     *  creates constraints such that the type of any input port that
     *  does not have its type declared must be less than or equal to
     *  the type of any output port that does not have its type
     *  declared. Type constraints from the contained Typeables
     *  (ports, variables, and parameters) are collected. In addition,
     *  type constraints from all the transitions are added. These
     *  constraints are determined by the guard and trigger expressions
     *  of transitions, and actions contained by the transitions.
     *  This method is read-synchronized on the workspace.
     *  @return A list of inequalities.
     *  @see ptolemy.graph.Inequality
     */
    public Set<Inequality> typeConstraints() {
        try {
            _workspace.getReadAccess();

            Set<Inequality> result = new HashSet<Inequality>();

            // Collect constraints from contained Typeables.
            Iterator ports = portList().iterator();

            while (ports.hasNext()) {
                Typeable port = (Typeable) ports.next();
                result.addAll(port.typeConstraints());
            }

            // Collect constraints from contained HasTypeConstraints
            // attributes.
            Iterator attributes = attributeList(HasTypeConstraints.class)
                    .iterator();

            while (attributes.hasNext()) {
                HasTypeConstraints typeableAttribute = (HasTypeConstraints) attributes
                        .next();
                result.addAll(typeableAttribute.typeConstraints());
            }

            // Collect constraints from all transitions.
            Iterator transitionRelations = relationList().iterator();

            while (transitionRelations.hasNext()) {
                Relation tr = (Relation) transitionRelations.next();
                attributes = tr.attributeList(HasTypeConstraints.class)
                        .iterator();

                while (attributes.hasNext()) {
                    HasTypeConstraints typeableAttribute = (HasTypeConstraints) attributes
                            .next();
                    result.addAll(typeableAttribute.typeConstraints());
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Do nothing except invoke the wrapup method of any objects
     *  that have been added using addInitializable().
     *  Derived classes override this method to define
     *  operations to be performed exactly once at the end of a complete
     *  execution of an application.  It typically closes
     *  files, displays final results, etc.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Attribute specifying the names of the final states of this
     *  actor. This attribute is kept for backward compatibility only,
     *  and is set to expert visibility. To set the final states,
     *  set the <i>isFinalState</i> parameter of a States.
     */
    public StringAttribute finalStateNames = null;

    /** Attribute specifying the name of the initial state of this
     *  actor. This attribute is kept for backward compatibility only,
     *  and is set to expert visibility. To set the initial state,
     *  set the <i>isInitialState</i> parameter of a State.
     */
    public StringAttribute initialStateName = null;

    /** Indicate whether input/output dependencies can depend on the
     *  state. By default, this is false (the default), indicating that a conservative
     *  dependency is provided by the causality interface. Specifically,
     *  if there is a dependency in any state, then the causality interface
     *  indicates that there is a dependency. If this is true, then a less
     *  conservative dependency is provided, indicating a dependency only
     *  if there can be one in the current state.  If this is true, then
     *  upon any state transition, this actor issues a change request, which
     *  forces causality analysis to be redone. Note that this can be expensive.
     */
    public Parameter stateDependentCausality;

    /** This class implements a scope, which is used to evaluate the
     *  parsed expressions.  This class is currently rather simple,
     *  but in the future should allow the values of input ports to
     *  be referenced without having shadow variables.
     */
    public class PortScope extends ModelScope {
        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @param name The name of the variable to be looked up.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.Token get(String name)
                throws IllegalActionException {
            // Check to see if it is something we refer to.
            Token token = (Token) _inputTokenMap.get(name);

            if (token != null) {
                return token;
            }

            Variable result = getScopedVariable(null, FSMActor.this, name);

            if (result != null) {
                return result.getToken();
            } else {
                // If we still can't find a name, try to resolve it with
                // ModelScope. This will look up the names of all states, as
                // well as the names in refinements and those at higher levels
                // of the model hierarchy.
                // -- tfeng (09/26/2008)
                NamedObj object = ModelScope.getScopedObject(FSMActor.this,
                        name);
                if (object instanceof Variable) {
                    token = ((Variable) object).getToken();
                } else if (object != null) {
                    token = new ObjectToken(object, object.getClass());
                }
                return token;
            }
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @param name The name of the variable to be looked up.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public Type getType(String name) throws IllegalActionException {
            // Check to see if this is something we refer to.
            Port port = _getPortForIdentifier(name);

            if ((port != null) && port instanceof Typeable) {
                if (name.endsWith("_isPresent")) {
                    return BaseType.BOOLEAN;

                } else if (name.endsWith("Array")) {

                    // We need to explicit return an ArrayType here
                    // because the port type may not be an ArrayType.
                    String portName = name.substring(0, name.length() - 5);
                    if (port == _getPortForIdentifier(portName)) {
                        Type portType = ((Typeable) port).getType();
                        return new ArrayType(portType);
                    }
                }
                return ((Typeable) port).getType();
            }

            Variable result = getScopedVariable(null, FSMActor.this, name);

            if (result != null) {
                return result.getType();
            } else {
                // If we still can't find a name, try to resolve it with
                // ModelScope. This will look up the names of all states, as
                // well as the names in refinements and those at higher levels
                // of the model hierarchy.
                // -- tfeng (09/26/2008)
                Type type = null;
                NamedObj object = ModelScope.getScopedObject(FSMActor.this,
                        name);
                if (object instanceof Variable) {
                    type = ((Variable) object).getType();
                } else if (object != null) {
                    type = new ObjectType(object, object.getClass());
                }
                return type;
            }
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @param name The name of the variable to be looked up.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            // Check to see if this is something we refer to.
            Port port = _getPortForIdentifier(name);

            if ((port != null) && port instanceof Typeable) {
                return ((Typeable) port).getTypeTerm();
            }

            Variable result = getScopedVariable(null, FSMActor.this, name);

            if (result != null) {
                return result.getTypeTerm();
            } else {
                return null;
            }
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         *  @exception IllegalActionException If getting the width of
         *   some port fails.
         */
        public Set identifierSet() throws IllegalActionException {
            Set set = getAllScopedVariableNames(null, FSMActor.this);

            // Make sure the identifier set is up to date.
            if (workspace().getVersion() != _identifierToPortVersion) {
                _setIdentifierToPort();
                _identifierToPortVersion = workspace().getVersion();
            }

            set.addAll(_identifierToPort.keySet());
            // If we still can't find a name, try to resolve it with
            // ModelScope. This will look up the names of all states, as
            // well as the names in refinements and those at higher levels
            // of the model hierarchy.
            // -- tfeng (09/26/2008)
            set.addAll(ModelScope.getAllScopedObjectNames(FSMActor.this));
            return set;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a state to this FSMActor. This overrides the base-class
     *  method to make sure the argument is an instance of State.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity State to contain.
     *  @exception IllegalActionException If the state has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument is not an instance of State.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the state list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof State)) {
            throw new IllegalActionException(this, entity,
                    "FSMActor can only contain entities that "
                            + "are instances of State.");
        }

        super._addEntity(entity);
    }

    /** Add a transition to this FSMActor. This method should not be used
     *  directly.  Call the setContainer() method of the transition instead.
     *  This method does not set the container of the transition to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation Transition to contain.
     *  @exception IllegalActionException If the transition has no name, or
     *   is not an instance of Transition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained transitions list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof Transition)) {
            throw new IllegalActionException(this, relation,
                    "FSMActor can only contain instances of Transition.");
        }

        super._addRelation(relation);

        if (_debugging) {
            relation.addDebugListener(new StreamListener());
        }
    }

    /** Execute all set actions contained by the transition chosen
     *  during the last call to chooseTransition(). Change current state
     *  to the destination state of the transition. Reset the refinement
     *  of the destination state if the <i>reset</i> parameter of the
     *  transition is true.
     *  @exception IllegalActionException If any commit action throws it,
     *   or the last chosen transition does not have a destination state.
     */
    protected void _commitLastChosenTransition() throws IllegalActionException {
        if (_lastChosenTransition == null) {
            return;
        }

        if (_debugging) {
            _debug("Commit transition ", _lastChosenTransition.getFullName()
                    + " at time " + getDirector().getModelTime());
            _debug("  Guard evaluating to true: "
                    + _lastChosenTransition.guardExpression.getExpression());
        }
        if (_lastChosenTransition.destinationState() == null) {
            throw new IllegalActionException(this, _lastChosenTransition,
                    "The transition is enabled but does not have a "
                            + "destination state.");
        }

        // If the chosen transition is a reset transition, initialize the destination
        // refinement. Otherwise, set the current time and index in the destination
        // refinement. Note that initializing the director will normally also have
        // the side effect of setting its time and time to match the enclosing
        // director. This is done before invoking the set actions because (1)
        // the initialization may reverse the set actions or, (2)
        // the set actions may trigger attributeChanged() calls that depend on
        // the current time or index.
        BooleanToken resetToken = (BooleanToken) _lastChosenTransition.reset
                .getToken();
        Actor[] actors = _lastChosenTransition.destinationState()
                .getRefinement();
        if (actors != null) {
            Director executiveDirector = getExecutiveDirector();
            for (int i = 0; i < actors.length; ++i) {
                // If this is a reset transition, then we also need to initialize
                // the destination refinement.
                if (resetToken.booleanValue()) {
                    if (_debugging) {
                        _debug(getFullName() + " initialize refinement: "
                                + ((NamedObj) actors[i]).getName());
                    }
                    // NOTE: For a modal model, the executive director will normally
                    // be an FSMDirector. Here we communicate with that director
                    // to ensure that it reports the correct superdense time index
                    // during initialization, which is one greater than the current
                    // superdense index of its context. If the enclosing director
                    // is not an FSMDirector, then the initialize() method below
                    // will likely set the index to one less than it should be.
                    // I don't have a solution for this, but this situation is
                    // unlikely to arise except in very weird models, since the
                    // standard pattern is for FSMDirector and FSMActor to work
                    // together.
                    if (executiveDirector instanceof FSMDirector) {
                        try {
                            ((FSMDirector) executiveDirector)._indexOffset = 1;
                            actors[i].initialize();
                        } finally {
                            ((FSMDirector) executiveDirector)._indexOffset = 0;
                        }
                    } else {
                        actors[i].initialize();
                    }
                } else {
                    // Set current time of the director of the destination
                    // refinement before executing actions because there may
                    // be attributeChanged() methods that are invoked that depend
                    // on current time.
                    actors[i].getDirector().setModelTime(
                            executiveDirector.getModelTime());

                    // Need also to set the superdense time index of the destination director
                    // one greater than the one of the enclosing director.
                    Director destinationDirector = actors[i].getDirector();
                    int index = 1;
                    if (executiveDirector instanceof SuperdenseTimeDirector) {
                        index = ((SuperdenseTimeDirector) executiveDirector)
                                .getIndex() + 1;
                    }
                    if (destinationDirector instanceof SuperdenseTimeDirector) {
                        ((SuperdenseTimeDirector) destinationDirector)
                                .setIndex(index);
                    }
                }
            }
        }

        // Next execute the commit actions.
        Iterator actions = _lastChosenTransition.commitActionList().iterator();
        while (actions.hasNext() && !_stopRequested) {
            Action action = (Action) actions.next();
            action.execute();
        }

        // Commit to the new state.
        // Before committing the new state, record whether it changed.
        boolean stateChanged = _currentState != _lastChosenTransition
                .destinationState();
        _currentState = _lastChosenTransition.destinationState();
        if (_debugging) {
            _debug(new StateEvent(this, _currentState));
        }

        // If we have reached a final state, make a record of that fact
        // for the postfire() method.
        if (((BooleanToken) _currentState.isFinalState.getToken())
                .booleanValue()) {
            _reachedFinalState = true;
        }

        _setCurrentConnectionMap();

        // If the causality interface is state-dependent and the state
        // has changed, invalidate the schedule. This is done in a ChangeRequest
        // because the current iteration (processing all events with the same
        // time stamp and microstep) has to be allowed to complete. Otherwise,
        // the analysis for causality loops will be redone before other state
        // machines have been given a chance to switch states.
        boolean stateDependent = ((BooleanToken) stateDependentCausality
                .getToken()).booleanValue();
        if (stateDependent && stateChanged) {
            ChangeRequest request = new ChangeRequest(this,
                    "Invalidate schedule") {
                protected void _execute() {
                    // Indicate to the director that the current schedule is invalid.
                    getDirector().invalidateSchedule();
                }
            };
            requestChange(request);
        }

        // Finally, request a refiring at the current time. This ensures that
        // if the new state is transient, zero time is spent in it.
        // If the new state has a refinement, it is up to that refinement
        // to examine the superdense time index if appropriate and not produce
        // output if it should not produce output. This has implications on
        // the design of actors like DiscreteClock.
        getDirector().fireAtCurrentTime(this);
    }

    /** Given an identifier, return a channel number i if the identifier is of
     *  the form portName_i, portName_i_isPresent, portName_iArray.
     *  Otherwise, return -1.
     *  @param identifier An identifier.
     *  @return A channel index, if the identifier refers to one.
     *  @exception IllegalActionException If getting the width of the port fails.
     */
    protected int _getChannelForIdentifier(String identifier)
            throws IllegalActionException {
        Port port = _getPortForIdentifier(identifier);
        if (port != null) {
            String portName = port.getName();
            if (identifier.startsWith(portName + "_")) {
                String channel = identifier.substring(portName.length() + 1);
                if (channel.endsWith("Array")) {
                    channel = channel.substring(0, channel.length() - 5);
                }
                if (channel.endsWith("isPresent")) {
                    channel = channel.substring(0, channel.length() - 9);
                }
                // Apparently, the syntax has been variably name_index_isPresent
                // and name_indexisPresent (without the second underscore).
                // Tolerate both syntaxes.
                if (channel.endsWith("_")) {
                    channel = channel.substring(0, channel.length() - 1);
                }
                if (channel.length() > 0) {
                    return Integer.decode(channel);
                }
            }
        }
        return -1;
    }

    /** Get the port for the specified identifier, which may be of
     *  form portName, portName_isPresent, portName_i, portName_i_isPresent,
     *  etc.
     *  @param identifier The specified identifier.
     *  @return The port that corresponds with teh specified identifier.
     *  @exception IllegalActionException If getting the width of the port fails.
     */
    protected Port _getPortForIdentifier(String identifier)
            throws IllegalActionException {
        if (workspace().getVersion() != _identifierToPortVersion) {
            _setIdentifierToPort();
            _identifierToPortVersion = workspace().getVersion();
        }
        return _identifierToPort.get(identifier);
    }

    /** Return true if the channel of the port is connected to an output
     *  port of the refinement of current state. If the current state
     *  does not have refinement, return false.
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @return True if the channel of the port is connected to an output
     *   port of the refinement of current state.
     *  @exception IllegalActionException If the refinement specified for
     *   one of the states is not valid.
     */
    protected boolean _isRefinementOutput(IOPort port, int channel)
            throws IllegalActionException {
        TypedActor[] refinements = _currentState.getRefinement();

        if ((refinements == null) || (refinements.length == 0)) {
            return false;
        }

        if (_connectionMapsVersion != workspace().getVersion()) {
            _setCurrentConnectionMap();
        }

        boolean[] flags = (boolean[]) _currentConnectionMap.get(port);
        return flags[channel];
    }

    /** Read tokens from the given channel of the given input port and
     *  make them accessible to the expressions of guards and
     *  transitions through the port scope.  If the specified port is
     *  not an input port, then do nothing.
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @exception IllegalActionException If the port is not contained by
     *   this actor.
     */
    protected void _readInputs(IOPort port, int channel)
            throws IllegalActionException {
        String portName = port.getName();

        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port,
                    "Cannot read inputs from port "
                            + "not contained by this FSMActor.");
        }

        if (!port.isInput()) {
            return;
        }

        if (port.isKnown(channel)) {
            if (_supportMultirate) {
                // FIXME: The following implementation to support multirate is
                // rather expensive. Try to optimize it.

                // FIXME: This does not look right. It reads all available tokens.
                // Shouldn't it read exactly the number to consume?
                // It could end up consuming tokens that will be needed on a
                // subsequent firing!
                int width = port.getWidth();

                // If we're in a new iteration, reallocate arrays to keep
                // track of HDF data.
                if (_newIteration && (channel == 0)) {
                    List[] tokenListArray = new LinkedList[width];

                    for (int i = 0; i < width; i++) {
                        tokenListArray[i] = new LinkedList();
                    }

                    _tokenListArrays.put(port, tokenListArray);
                }

                // Get the list of tokens for the given port.
                List[] tokenListArray = (LinkedList[]) _tokenListArrays
                        .get(port);

                // Update the value variable if there is/are token(s) in
                // the channel. The HDF(SDF) schedule will guarantee there
                // are always enough tokens.

                while (port.hasToken(channel)) {
                    Token token = port.get(channel);

                    if (_debugging) {
                        _debug("---", port.getName(), "(" + channel + ") has ",
                                token.toString() + " at time "
                                        + getDirector().getModelTime());
                    }

                    tokenListArray[channel].add(0, token);
                }

                if (_debugging) {
                    _debug("Total tokens available at port: "
                            + port.getFullName() + "  ");
                }

                int length = tokenListArray[channel].size();
                if (length > 0) {
                    Token[] tokens = new Token[length];
                    tokenListArray[channel].toArray(tokens);
                    _setInputTokenMap(port, channel, tokens[0], tokens);
                } else {
                    // There is no data. Just set the _isPresent variables to false.
                    _setInputTokenMap(port, channel, null, null);
                }
            } else {
                // If not supporting multirate firing,
                // Update the value variable if there is a token in the channel.
                if (port.hasToken(channel)) {
                    Token token = port.get(channel);

                    if (_debugging) {
                        _debug("---", port.getName(), "(" + channel + ") has ",
                                token.toString() + " at time "
                                        + getDirector().getModelTime());
                    }
                    _setInputTokenMap(port, channel, token, null);
                } else {
                    // There is no data. Just set the _isPresent variables to false.
                    _setInputTokenMap(port, channel, null, null);
                }
            }
        } else {
            // Remove identifiers so that previous values are not erroneously
            // read.
            _removePortVariables(portName, channel);
        }
    }

    /** Set the map from input ports to boolean flags indicating whether a
     *  channel is connected to an output port of the refinement of the
     *  current state.
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid.
     */
    protected void _setCurrentConnectionMap() throws IllegalActionException {
        if (_connectionMapsVersion != workspace().getVersion()) {
            _buildConnectionMaps();
        }

        _currentConnectionMap = (Map) _connectionMaps.get(_currentState);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////

    /** Current state. */
    protected State _currentState = null;

    /** List of objects whose (pre)initialize() and wrapup() methods
     *  should be slaved to these.
     */
    protected transient List<Initializable> _initializables;

    /** A map from ports to corresponding input variables. */
    protected Map _inputTokenMap = new HashMap();

    /** The last chosen transition. */
    protected Transition _lastChosenTransition = null;

    /** Indicator that a stop has been requested by a call to stop(). */
    protected boolean _stopRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                package friendly variables                 ////

    /** The initial state. This is package friendly so that State can
     *  access it.
     */
    State _initialState = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Build for each state a map from input ports to boolean flags
     *  indicating whether a channel is connected to an output port
     *  of the refinement of the state.
     *  This method is read-synchronized on the workspace.
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid.
     */
    private void _buildConnectionMaps() throws IllegalActionException {
        try {
            workspace().getReadAccess();

            if (_connectionMaps == null) {
                _connectionMaps = new HashMap();
            } else {
                // Remove any existing maps.
                _connectionMaps.clear();
            }

            // Create a map for each state.
            Iterator states = entityList().iterator();
            State state = null;

            while (states.hasNext()) {
                state = (State) states.next();

                Map stateMap = new HashMap();
                TypedActor[] actors = state.getRefinement();

                // Determine the boolean flags for each input port.
                Iterator inPorts = inputPortList().iterator();

                while (inPorts.hasNext()) {
                    IOPort inPort = (IOPort) inPorts.next();
                    boolean[] flags = new boolean[inPort.getWidth()];

                    if ((actors == null) || (actors.length == 0)) {
                        java.util.Arrays.fill(flags, false);
                        stateMap.put(inPort, flags);
                        continue;
                    }

                    Iterator relations = inPort.linkedRelationList().iterator();
                    int channelIndex = 0;

                    while (relations.hasNext()) {
                        IORelation relation = (IORelation) relations.next();
                        boolean linked = false;

                        for (int i = 0; i < actors.length; ++i) {
                            Iterator outports = actors[i].outputPortList()
                                    .iterator();

                            while (outports.hasNext()) {
                                IOPort outport = (IOPort) outports.next();
                                linked = linked | outport.isLinked(relation);
                            }
                        }

                        for (int j = 0; j < relation.getWidth(); ++j) {
                            flags[channelIndex + j] = linked;
                        }

                        channelIndex += relation.getWidth();
                    }

                    stateMap.put(inPort, flags);
                }

                _connectionMaps.put(state, stateMap);
            }

            _connectionMapsVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    /** Create receivers for each input port.
     *  This method gets write permission on the workspace.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _createReceivers() throws IllegalActionException {
        try {
            workspace().getWriteAccess();
            Iterator inputPorts = inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inPort = (IOPort) inputPorts.next();
                inPort.createReceivers();
            }
        } finally {
            // Note that this does not increment the workspace version.
            // We have not changed the structure of the model.
            workspace().doneTemporaryWriting();
        }
    }

    /*  Initialize the actor.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _init() {
        // Create a more reasonable default icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
                + "height=\"40\" style=\"fill:red\"/>\n"
                + "<rect x=\"-28\" y=\"-18\" width=\"56\" "
                + "height=\"36\" style=\"fill:lightgrey\"/>\n"
                + "<ellipse cx=\"0\" cy=\"0\"" + " rx=\"15\" ry=\"10\"/>\n"
                + "<circle cx=\"-15\" cy=\"0\""
                + " r=\"5\" style=\"fill:white\"/>\n"
                + "<circle cx=\"15\" cy=\"0\""
                + " r=\"5\" style=\"fill:white\"/>\n" + "</svg>\n");

        try {
            stateDependentCausality = new Parameter(this,
                    "stateDependentCausality");
            stateDependentCausality.setTypeEquals(BaseType.BOOLEAN);
            stateDependentCausality.setExpression("false");

            initialStateName = new StringAttribute(this, "initialStateName");
            initialStateName.setExpression("");
            initialStateName.setVisibility(Settable.EXPERT);

            finalStateNames = new StringAttribute(this, "finalStateNames");
            finalStateNames.setExpression("");
            finalStateNames.setVisibility(Settable.EXPERT);
        } catch (KernelException ex) {
            // This should never happen.
            throw new InternalErrorException("Constructor error "
                    + ex.getMessage());
        }

        _identifierToPort = new HashMap<String, Port>();
    }

    /** Remove all variable definitions associated with the specified
     *  port name and channel number.
     *  @param portName The name of the port
     *  @param channel The channel number
     */
    private void _removePortVariables(String portName, int channel) {
        String portChannelName = portName + "_" + channel;
        _inputTokenMap.remove(portName);
        _inputTokenMap.remove(portChannelName);
        _inputTokenMap.remove(portName + "_isPresent");
        _inputTokenMap.remove(portName + "Array");
        _inputTokenMap.remove(portChannelName + "Array");
    }

    /** Reset receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _resetReceivers() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inPort = (IOPort) inputPorts.next();
            Receiver[][] receivers = inPort.getReceivers();
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            receivers[i][j].reset();
                        }
                    }
                }
            }
        }
    }

    /** Given a transition, find any input ports
     *  referenced in the guard expressions of the
     *  transitions, and if any of those input ports has status
     *  unknown, return false.
     *  FIXME: bogus:
     *  Also, if the port identifier does
     *  not end with "_isPresent", then return false if port
     *  identifier with "_isPresent" appended is false. There is no data on
     *  the port "in" then the identifier "in" will be undefined, or worse,
     *  will resolve to the port object itself.
     *  Otherwise, return true.
     *  These are the input ports whose status must be known
     *  for this transition to be enabled.
     *  @param transition A transition
     *  @return A set of input ports.
     *  @exception IllegalActionException If the guard expression cannot
     *   be parsed.
     */
    private boolean _referencedInputPortsByGuardKnown(Transition transition)
            throws IllegalActionException {

        // FIXME: This method is wrong!
        // E.g., if the guard expression is (!in_isPresent || in == 1)
        // then this method will return false when in is absent!
        // The next method below may also be wrong!

        String string = transition.getGuardExpression();
        if (string.trim().equals("")) {
            return true;
        }
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree = parser.generateParseTree(string);
        ParseTreeFreeVariableCollector variableCollector = new ParseTreeFreeVariableCollector();
        ParserScope scope = getPortScope();
        // Get a set of free variable names.
        Set<String> nameSet = variableCollector.collectFreeVariables(parseTree,
                scope);

        for (String name : nameSet) {
            Port port = _getPortForIdentifier(name);
            if (port instanceof IOPort) {
                int channel = _getChannelForIdentifier(name);
                if (channel >= 0) {
                    if (!((IOPort) port).isKnown(channel)) {
                        return false;
                    }
                    /* FIXME: bogus? */
                    if (!name.endsWith("_isPresent")) {
                        Token token = scope.get(port.getName() + "_" + channel
                                + "_isPresent");
                        if (!(token instanceof BooleanToken)
                                || !((BooleanToken) token).booleanValue()) {
                            return false;
                        }
                    }
                } else {
                    // No specified channel.
                    if (!((IOPort) port).isKnown()) {
                        return false;
                    }
                    /* FIXME: bogus? */
                    if (!name.endsWith("_isPresent")) {
                        Token token = scope.get(port.getName() + "_isPresent");
                        if (!(token instanceof BooleanToken)
                                || !((BooleanToken) token).booleanValue()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /** Given a transition, find any input ports
     *  referenced in the output actions of the
     *  transitions, and if any of those input ports has status
     *  unknown, return false. Otherwise, return true.
     *  These are the input ports whose status must be known
     *  to choose this transition.
     *  @param transition A transition
     *  @return A set of input ports.
     *  @exception IllegalActionException If the guard expression cannot
     *   be parsed.
     */
    private boolean _referencedInputPortsByOutputKnown(Transition transition)
            throws IllegalActionException {
        String outputActionsExpression = transition.outputActions
                .getExpression();
        PtParser parser = new PtParser();
        ParseTreeFreeVariableCollector variableCollector = new ParseTreeFreeVariableCollector();
        ParserScope scope = getPortScope();
        if (!outputActionsExpression.trim().equals("")) {
            Map map = parser.generateAssignmentMap(outputActionsExpression);
            for (Iterator names = map.entrySet().iterator(); names.hasNext();) {
                Map.Entry entry = (Map.Entry) names.next();
                ASTPtAssignmentNode node = (ASTPtAssignmentNode) entry
                        .getValue();
                ASTPtRootNode parseTree = node.getExpressionTree();
                Set<String> nameSet = variableCollector.collectFreeVariables(
                        parseTree, scope);

                for (String name : nameSet) {
                    Port port = _getPortForIdentifier(name);
                    if (port instanceof IOPort) {
                        int channel = _getChannelForIdentifier(name);
                        if (channel >= 0) {
                            if (!((IOPort) port).isKnown(channel)) {
                                return false;
                            }
                        } else {
                            if (!((IOPort) port).isKnown()) {
                                return false;
                            }
                        }
                        // Port status is known, but the referenced
                        // identifier may be undefined (e.g. "in" when
                        // in is absent).
                        /* FIXME: Bogus. Could be in a part of the
                         * output that will not be evaluated.
                        if (scope.get(name) == null) {
                            return false;
                        }
                        */
                    }
                }
            }
        }
        return true;
    }

    /** For each input port of this actor, associate all identifiers
     *  with that port.
     *  @exception IllegalActionException If getting the width of the port fails.
     */
    private void _setIdentifierToPort() throws IllegalActionException {
        _identifierToPort.clear();

        for (Iterator inputPorts = inputPortList().iterator(); inputPorts
                .hasNext();) {
            IOPort inPort = (IOPort) inputPorts.next();
            String portName = inPort.getName();
            _identifierToPort.put(portName, inPort);
            _identifierToPort.put(portName, inPort);
            _identifierToPort.put(portName + "_isPresent", inPort);
            _identifierToPort.put(portName + "Array", inPort);

            for (int i = 0; i < inPort.getWidth(); i++) {
                _identifierToPort.put(portName + "_" + i, inPort);
                _identifierToPort
                        .put(portName + "_" + i + "_isPresent", inPort);
                _identifierToPort.put(portName + "_" + i + "Array", inPort);
            }
        }
    }

    /** Set the port variables for the specified port as follows:
     *  The portName_isPresent variable is set to true if the token
     *  argument is non-null, and false otherwise.
     *  If the token argument is non-null, then the portName variable
     *  is set to have the value of the token.
     *  If the tokenArray variable is non-null, then the portNameArray
     *  variable is set to have its value.  In addition, for each
     *  of these cases, another (up to) three variables are set
     *  with portName replaced by portName_i, where i is the channel
     *  number. If token is null, then the variable portName
     *  is set to have the value Token.NIL. Note that this yields
     *  far better error messages than leaving it unset, because if
     *  if it not set, portName will resolve to the port itself,
     *  which can be very confusing.
     *  @param port The port.
     *  @param channel The channel.
     *  @param token If not null, the data token at the port.
     *  @param tokenArray If not null, an array of tokens at the port.
     *  @exception IllegalActionException If the identifier is
     *   already associated with another port.
     */
    private void _setInputTokenMap(Port port, int channel, Token token,
            Token[] tokenArray) throws IllegalActionException {
        String portName = port.getName();
        String portChannelName = portName + "_" + channel;

        String name = portName + "_isPresent";
        if (token != null) {
            _inputTokenMap.put(name, BooleanToken.TRUE);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to true.");
            }
        } else {
            _inputTokenMap.put(name, BooleanToken.FALSE);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to false.");
            }
        }

        name = portChannelName + "_isPresent";
        if (token != null) {
            _inputTokenMap.put(name, BooleanToken.TRUE);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to true.");
            }
        } else {
            _inputTokenMap.put(name, BooleanToken.FALSE);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to false.");
            }
        }

        name = portName;
        if (token != null) {
            _inputTokenMap.put(name, token);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to " + token);
            }
        } else {
            // Set the identifier to have value Token.NIL.
            _inputTokenMap.put(name, Token.NIL);
        }

        name = portChannelName;
        if (token != null) {
            _inputTokenMap.put(name, token);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to " + token);
            }
        } else {
            // Remove the identifier.
            _inputTokenMap.remove(name);
        }

        name = portName + "Array";
        if (tokenArray != null) {
            ArrayToken arrayToken = new ArrayToken(tokenArray);
            _inputTokenMap.put(name, arrayToken);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to " + arrayToken);
            }
        } else {
            // Remove the identifier.
            _inputTokenMap.remove(name);
        }

        name = portChannelName + "Array";
        if (tokenArray != null) {
            ArrayToken arrayToken = new ArrayToken(tokenArray);
            _inputTokenMap.put(name, arrayToken);
            if (_debugging) {
                _debug("--- Setting variable ", name, " to " + arrayToken);
            }
        } else {
            // Remove the identifier.
            _inputTokenMap.remove(name);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private transient LinkedList _cachedInputPorts;

    private transient LinkedList _cachedOutputPorts;

    /** The causality interface, if it has been created,
     *  for the case where the causality interface is not state
     *  dependent.
     */
    private CausalityInterface _causalityInterface;

    /** The director for which the causality interface was created. */
    private Director _causalityInterfaceDirector;

    /** The causality interfaces by state, for the case
     *  where the causality interface is state dependent.
     */
    private Map<State, FSMCausalityInterface> _causalityInterfaces;

    /** The workspace version for causality interfaces by state, for the case
     *  where the causality interface is state dependent.
     */
    private Map<State, Long> _causalityInterfacesVersions;

    // Stores for each state a map from input ports to boolean flags
    // indicating whether a channel is connected to an output port
    // of the refinement of the state.
    private Map _connectionMaps = null;

    // Version of the connection maps.
    private long _connectionMapsVersion = -1;

    // The map from input ports to boolean flags indicating whether a
    // channel is connected to an output port of the refinement of the
    // current state.
    private Map _currentConnectionMap = null;

    /** A map that associates each identifier with the unique port that that
     *  identifier describes.  This map is used to detect port names that result
     *  in ambiguous identifier bindings.
     */
    private HashMap<String, Port> _identifierToPort;

    /** Version number for _identifierToPort. */
    private long _identifierToPortVersion = -1;

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;

    // A flag indicating whether this is at the beginning
    // of one iteration (firing). Normally it is set to true.
    // It is only set to false in HDF.
    private boolean _newIteration = true;

    private transient long _outputPortsVersion = -1;

    // True if the current state is a final state.
    private boolean _reachedFinalState;

    // Indicator of when the receivers were last updated.
    private long _receiversVersion = -1;

    // A flag indicating whether this actor supports multirate firing.
    private boolean _supportMultirate = false;

    // Hashtable to save an array of tokens for each port.
    // This is used in HDF when multiple tokens are consumed
    // by the FSMActor in one iteration.
    private Hashtable _tokenListArrays;
}
