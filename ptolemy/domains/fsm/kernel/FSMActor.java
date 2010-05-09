/* An actor containing a finite state machine (FSM).

 Copyright (c) 1999-2010 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
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
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.HasTypeConstraints;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.domains.modal.kernel.Suspendable;
import ptolemy.domains.ptera.kernel.PteraModalModel;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
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

////FSMActor

/**
 An FSMActor contains a set of states and transitions. A transition has
 a guard expression and a trigger expression. A transition is enabled and
 can be taken when its guard is true. A transition is triggered and must be
 taken when its trigger is true. A transition can contain a set of actions.

 <p> When an FSMActor is fired, the outgoing transitions of the current state
 are examined. An IllegalActionException is thrown if there is more than one
 enabled transition. If there is exactly one enabled transition then it is
 chosen and the choice actions contained by the transition are executed.
 An FSMActor does not change state during successive firings in one iteration
 in order to support domains that iterate to a fixed point. When the FSMActor
 is postfired, the chosen transition of the latest firing of the actor is
 committed. The commit actions contained by the transition are executed and
 the current state of the actor is set to the destination state of the
 transition.

 <p> An FSMActor enters its initial state during initialization. The
 initial state is the unique state whose <i>isInitialState</i> parameter
 is true. A final state is a state that has its <i>isFinalState</i> parameter
 set to true. When the actor reaches a final state, then the
 postfire method will return false, indicating that the actor does not
 wish to be fired again.

 <p> The guards and actions of FSM transitions are specified using
 expressions.  These expressions are evaluated in the scope returned by
 getPortScope.  This scope binds identifiers for FSM ports as defined
 in the following paragraph.  These identifiers are in the scope of
 guard and action expressions prior to any variables, and may shadow
 variables with appropriately chosen names.  Given appropriately chosen
 port names, there may be conflicts between these various identifiers.
 These conflicts are detected and an exception is thrown during
 execution.

 <p> For every input port, the identifier
 "<i>portName</i>_<i>channelIndex</i>" refers to the last input
 received from the port on the given channel.  The type of this
 identifier is the same as the type of the port.  This token may have
 been consumed in the current firing or in a previous firing.  The
 identifier "<i>portName</i>_<i>channelIndex</i>_isPresent" is true if
 the port consumed an input on the given channel in the current firing
 of the FSM.  The type of this identifier is always boolean.  Lastly,
 the identifier "<i>portName</i>_<i>channelIndex</i>Array" refers the
 array of all tokens consumed from the port in the last firing.  This
 identifier has an array type whose element type is the type of the
 corresponding input port.  Additionally, for conciseness when
 referencing single ports, the first channel may be referred to without
 the channel index, i.e. by the identifiers "<i>portName</i>",
 "<i>portName</i>_<i>isPresent</i>", and "<i>portName</i>Array".

 <p> An FSMActor can be used in a modal model to represent the mode
 control logic.  A state can have a TypedActor refinement. A transition
 in an FSMActor can be preemptive or non-preemptive. When a preemptive
 transition is chosen, the refinement of its source state is not
 fired. A non-preemptive transition can only be chosen after the
 refinement of its source state is fired.

 <p> By default, this actor has a conservative causality interface,
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

 @author Xiaojun Liu, Haiyang Zheng, Ye Zhou
 @version $Id$
 @since Ptolemy II 0.4
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

    /** React to a change in an attribute.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        if (attribute == finalStateNames) {
            _parseFinalStates(finalStateNames.getExpression());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return an enabled transition among the given list of transitions.
     *  If there is only one transition enabled, return that transition.
     *  In case there are multiple enabled transitions, if any of
     *  them is not nondeterministic, throw an exception. See {@link Transition}
     *  for the explanation of "nondeterministic". Otherwise, randomly choose
     *  one from the enabled transitions and return it.
     *  <p>
     *  Execute the choice actions contained by the returned transition.
     *  @param transitionList A list of transitions.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled and not all of them are nondeterministic.
     */
    public Transition chooseTransition(List transitionList)
    throws IllegalActionException {
        Transition result = null;

        List enabledTransitions = enabledTransitions(transitionList);
        int length = enabledTransitions.size();

        if (length == 1) {
            result = (Transition) enabledTransitions.get(0);
        } else if (length > 1) {
            // Ensure that if there are multiple enabled transitions, all of them
            // must be nondeterministic.
            Iterator transitions = enabledTransitions.iterator();

            while (transitions.hasNext()) {
                Transition enabledTransition = (Transition) transitions.next();

                if (!enabledTransition.isNondeterministic()) {
                    throw new MultipleEnabledTransitionsException(
                            currentState(),
                            "Nondeterministic FSM error: "
                            + "Multiple enabled transitions found but not all"
                            + " of them are marked nondeterministic. Transition "
                            + enabledTransition.getName()
                            + " is deterministic.");
                }
            }

            // Randomly choose one transition from the list of the
            // enabled transitions.

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
        }

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
        newObject._identifierToPort = new HashMap();
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
        newObject._finalStateNames = null;
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
     *  transitions.
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

        while (transitionRelations.hasNext() && !_stopRequested) {
            Transition transition = (Transition) transitionRelations.next();
            if (transition.isDefault()) {
                defaultTransitions.add(transition);
            } else if (transition.isEnabled()) {
                enabledTransitions.add(transition);
            }
        }

        // NOTE: It is the _chooseTransition method that decides which
        // enabled transition is actually taken. This method simply returns
        // all enabled transitions.
        if (enabledTransitions.size() > 0) {
            return enabledTransitions;
        } else {
            return defaultTransitions;
        }
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
     *  Otherwise, execute the choice actions contained by the chosen
     *  transition.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    public void fire() throws IllegalActionException {
        // NOTE: this method is not called in the FSMDirector class.
        // This FSMActor is a mealy machine in that it produces outputs
        // when taking transitions.
        readInputs();
        List transitionList = _currentState.outgoingPort.linkedRelationList();
        chooseTransition(transitionList);
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

        // Reset the visited status of all states to not visited.
        Iterator states = deepEntityList().iterator();
        while (states.hasNext()) {
            State state = (State) states.next();
            state.setVisited(false);
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

    /** Return false if there is any output does not depend
     *  directly on all inputs.
     *  @return False if there is any output that does not
     *   depend directly on an input.
     *  @exception IllegalActionException Thrown if causality interface
     *  cannot be computed.
     */
    public boolean isStrict() throws IllegalActionException {
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
        return !_reachedFinalState && !_stopRequested;
    }

    /** Return true.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        _lastChosenTransition = null;
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

        // Populate a map from identifier to the input port represented.
        _identifierToPort.clear();

        for (Iterator inputPorts = inputPortList().iterator(); inputPorts
        .hasNext();) {
            IOPort inPort = (IOPort) inputPorts.next();
            _setIdentifierToPort(inPort.getName(), inPort);
            _setIdentifierToPort(inPort.getName() + "_isPresent", inPort);
            _setIdentifierToPort(inPort.getName() + "Array", inPort);

            for (int i = 0; i < inPort.getWidth(); i++) {
                _setIdentifierToPort(inPort.getName() + "_" + i, inPort);
                _setIdentifierToPort(inPort.getName() + "_" + i + "_isPresent",
                        inPort);
                _setIdentifierToPort(inPort.getName() + "_" + i + "Array",
                        inPort);
            }
        }

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
     * @param transition The last chosen transition.
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

    public void setModelError(){
        if(_modelError == false){
            try{
                modelError.setExpression("true");
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            if(_debugging){
                _debug("I've set the model error");
            }
            System.out.println("I've set the model error");
            _modelError = true;
        }
    }

    public void clearModelError(){
        if(_modelError){
            if(_debugging){
                _debug("I've cleared the model error");
            }

            System.out.println("I've cleared the model error");
            _modelError = false;
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
            Port port = (Port) _identifierToPort.get(name);

            if ((port != null) && port instanceof Typeable) {
                if (name.endsWith("_isPresent")) {
                    return BaseType.BOOLEAN;

                } else if (name.endsWith("Array")) {

                    // We need to explicit return an ArrayType here
                    // because the port type may not be an ArrayType.
                    String portName = name.substring(0, name.length() - 5);
                    if (port == _identifierToPort.get(portName)) {
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
            Port port = (Port) _identifierToPort.get(name);

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
         */
        public Set identifierSet() {
            Set set = getAllScopedVariableNames(null, FSMActor.this);
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

    /** Execute all commit actions contained by the transition chosen
     *  during the last call to _chooseTransition(). Change current state
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

        // Set current time of the director of the destination
        // refinement before executing actions because there may
        // be attributeChanged() methods that are invoked that depend
        // on current time.
        Actor[] actors = _lastChosenTransition.destinationState()
        .getRefinement();
        if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                _setTimeForRefinement(actors[i]);
            }
        }

        Iterator actions = _lastChosenTransition.commitActionList().iterator();

        while (actions.hasNext() && !_stopRequested) {
            Action action = (Action) actions.next();
            action.execute();
        }

        // Before committing the new state, record whether it changed.
        boolean stateChanged = _currentState != _lastChosenTransition
        .destinationState();
        _currentState = _lastChosenTransition.destinationState();

        if (((BooleanToken) _currentState.isFinalState.getToken())
                .booleanValue()) {
            _reachedFinalState = true;
        } else if (_finalStateNames != null) {
            // Backward compatibility for the old way of specifying it.
            if (_finalStateNames.contains(_currentState.getName())) {
                _reachedFinalState = true;
            }
        }

        if (_debugging) {
            _debug(new StateEvent(this, _currentState));
        }

        BooleanToken resetToken = (BooleanToken) _lastChosenTransition.reset
        .getToken();

        if (resetToken.booleanValue()) {
            actors = _currentState.getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_debugging) {
                        _debug(getFullName() + " initialize refinement: "
                                + ((NamedObj) actors[i]).getName());
                    }

                    actors[i].initialize();
                }
            }

            // NOTE: The initialize method clears all receivers, we have to
            // set the visited flag to false to enforce the reconstruction of
            // continuous signals.
            _currentState.setVisited(false);
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
        String portChannelName = portName + "_" + channel;

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
                int width = port.getWidth();

                // If we're in a new iteration, reallocate arrays to keep
                // track of hdf data.
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

                // FIXME: The "portName_isPresent" is true if there
                // is at least one token.
                int length = tokenListArray[channel].size();

                if (length > 0) {
                    Token[] tokens = new Token[length];
                    tokenListArray[channel].toArray(tokens);

                    _setInputTokenMap(portName + "_isPresent", port,
                            BooleanToken.TRUE);
                    _setInputTokenMap(portChannelName + "_isPresent", port,
                            BooleanToken.TRUE);
                    _setInputTokenMap(portName, port, tokens[0]);
                    _setInputTokenMap(portChannelName, port, tokens[0]);

                    ArrayToken arrayToken = new ArrayToken(tokens);
                    _setInputTokenMap(portName + "Array", port, arrayToken);
                    _setInputTokenMap(portChannelName + "Array", port,
                            arrayToken);
                } else {
                    _setInputTokenMap(portName + "_isPresent", port,
                            BooleanToken.FALSE);
                    _setInputTokenMap(portChannelName + "_isPresent", port,
                            BooleanToken.FALSE);

                    if (_debugging) {
                        _debug("---", port.getName(), "(" + channel
                                + ") has no token at time "
                                + getDirector().getModelTime());
                    }
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

                    _setInputTokenMap(portName + "_isPresent", port,
                            BooleanToken.TRUE);
                    _setInputTokenMap(portChannelName + "_isPresent", port,
                            BooleanToken.TRUE);
                    _setInputTokenMap(portName, port, token);
                    _setInputTokenMap(portChannelName, port, token);
                } else {
                    _setInputTokenMap(portName + "_isPresent", port,
                            BooleanToken.FALSE);
                    _setInputTokenMap(portChannelName + "_isPresent", port,
                            BooleanToken.FALSE);

                    if (_debugging) {
                        _debug("---", port.getName(), "(" + channel
                                + ") has no token at time "
                                + getDirector().getModelTime());
                    }
                }
            }
        } else {
            // If we assume this actor is strict, we do not need to handle
            // unknowns.
            // FIXME how to deal with unknown?
            //             shadowVariables[channel][0].setUnknown(true);
            //             shadowVariables[channel][1].setUnknown(true);
            //             shadowVariables[channel][2].setUnknown(true);
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

    /** A map that associates each identifier with the unique port that that
     *  identifier describes.  This map is used to detect port names that result
     *  in ambiguous identifier bindings.
     */
    protected HashMap _identifierToPort;

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

        _identifierToPort = new HashMap();

        /*
         try {
         tokenHistorySize =
         new Parameter(this, "tokenHistorySize", new IntToken(1));
         } catch (Exception e) {
         throw new InternalErrorException(
         "cannot create default tokenHistorySize parameter:\n" + e);
         }
         */
        try{
            modelError= new Parameter(this, "modelError");
            modelError.setTypeEquals(BaseType.BOOLEAN);
            //modelError.setVisibililty
            modelError.setExpression("false");
            // =new TypedIOPort(this, "modelError", true, true);
            //modelError.setTypeEquals(BaseType.BOOLEAN);
        }catch(IllegalActionException ex){
            ex.printStackTrace();
        }
        catch(NameDuplicationException ex){
            ex.printStackTrace();
        }
        //mass.setTypeEquals(BaseType.STRING);

    }

    /** Parse the final state names. This method handles backward
     *  compatibility, because it used to be that final states were
     *  specified in a parameter of the FSMActor, as a comma-separated
     *  list of names. Now, each state has an attribute that indicates
     *  whether it is a final state. This method parses the list of names,
     *  sets the attribute of each state, and then sets the list to null.
     *  If the model is then saved, it will have switched to the modern
     *  method for recording final states.
     */
    private void _parseFinalStates(String names) {
        StringTokenizer nameTokens = new StringTokenizer(names, ",");
        while (nameTokens.hasMoreElements()) {
            String name = (String) nameTokens.nextElement();
            name = name.trim();
            //State finalState = (State) getEntity(name);
            // State has not been created. Record that it is
            // final state to mark it when it is created.
            if (_finalStateNames == null) {
                _finalStateNames = new HashSet<String>();
            }
            _finalStateNames.add(name);
        }
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

    // Associate the given identifier as referring to some aspect of
    // the given input port.  If the given identifier is already
    // associated with another port, then throw an exception.
    private void _setIdentifierToPort(String name, Port inputPort)
    throws IllegalActionException {
        Port previousPort = (Port) _identifierToPort.get(name);

        if ((previousPort != null) && (previousPort != inputPort)) {
            throw new IllegalActionException("Name conflict in finite state"
                    + " machine.  The identifier \"" + name
                    + "\" is associated with the port " + previousPort
                    + " and with the port " + inputPort);
        }

        _identifierToPort.put(name, inputPort);
    }

    private void _setInputTokenMap(String name, Port inputPort, Token token)
    throws IllegalActionException {
        _setIdentifierToPort(name, inputPort);
        _inputTokenMap.put(name, token);
    }

    /** If the specified refinement implements Suspendable, then set its
     *  current time equal to the current environment time minus the refinement's
     *  total accumulated suspension time. Otherwise, set current time to
     *  match that of the environment. If there is no environment, do nothing.
     *  @param refinement The refinement.

     */
    private void _setTimeForRefinement(Actor refinement)
    throws IllegalActionException {
        Time environmentTime = getExecutiveDirector().getModelTime();
        Director director = refinement.getDirector();
        if (refinement instanceof Suspendable) {
            // Adjust current time to be the environment time minus
            // the accumulated suspended time of the refinement.
            Time suspendedTime = ((Suspendable)refinement).accumulatedSuspendTime();


            if (suspendedTime != null) {
                director.setModelTime(environmentTime.subtract(suspendedTime));
                return;
            }
        }
        director.setModelTime(environmentTime);
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

    // Map of final state names recording in the obsolete finalStateNames
    // parameter.
    private HashSet<String> _finalStateNames = null;

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

    private Parameter modelError;
    private boolean _modelError = false;
}
