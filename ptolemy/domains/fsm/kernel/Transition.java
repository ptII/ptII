/* A transition in an FSMActor.

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Transition

/**
 A Transition has a source state and a destination state. A
 transition has a guard expression, which is evaluated to a boolean value.
 Whenever a transition is enabled, it must be taken immediately.
 That is, unlike some state machines formalisms, our guard is not just
 an enabler for the transition but rather a trigger for the transition.

 <p> A transition can contain actions. The way to specify actions is
 to give value to the <i>outputActions</i> parameter and the
 <i>setActions</i> parameter.

 The value of these parameters is a string of the form:
 <pre>
 <i>command</i>; <i>command</i>; ...
 </pre>
 where each <i>command</i> has the form:
 <pre>
 <i>destination</i> = <i>expression</i>
 </pre>
 For the <i>outputActions</i> parameter, <i>destination</i> is either
 <pre>
 <i>portName</i>
 </pre>
 or
 <pre>
 <i>portName</i>(<i>channelNumber</i>)
 </pre>
 Here, <i>portName</i> is the name of a port of the FSM actor,
 If no <i>channelNumber</i> is given, then the value
 is broadcasted to all channels of the port.
 <p>
 For the <i>setActions</i> parameter, <i>destination</i> is
 <pre>
 <i>variableName</i>
 </pre>
 <i>variableName</i> identifies either a variable or parameter of
 the FSM actor, or a variable or parameter of the refinement of the
 destination state of the transition. To give a variable of the
 refinement, use a dotted name, as follows:
 <pre>
 <i>refinementName</i>.<i>variableName</i>
 </pre>
 The <i>expression</i> is a string giving an expression in the usual
 Ptolemy II expression language. The expression may include references
 to variables and parameters contained by the FSM actor.
 <p>
 The <i>outputActions</i> and <i>setActions</i> parameters are not the only
 ways to specify actions. In fact, you can add action attributes that are
 instances of anything that inherits from Action.
 (Use the Add button in the Edit Parameters dialog).
 <p>
 An action is either a ChoiceAction or a CommitAction. The <i>setActions</i>
 parameter is a CommitAction, whereas the <i>outputActions</i> parameter is a
 ChoiceAction. A commit action is executed when the transition is taken to
 change the state of the FSM, in the postfire() method of FSMActor.
 A choice action, by contrast, is executed in the fire() method
 of the FSMActor when the transition is chosen, but not yet taken.
 The difference is subtle, and for most domains, irrelevant.
 A few domains, however, such as CT, which have fixed point semantics,
 where the fire() method may be invoked several times before the
 transition is taken (committed). For such domains, it is useful
 to have actions that fulfill the ChoiceAction interface.
 Such actions participate in the search for a fixed point, but
 do not change the state of the FSM.
 <p>
 A transition can be preemptive or non-preemptive. When a preemptive transition
 is chosen, the refinement of its source state is not fired. A non-preemptive
 transition is only chosen after the refinement of its source state is fired.
 <p>
 The <i>reset</i> parameter specifies whether the refinement of the destination
 state is reset when the transition is taken. There is no reset() method in the
 Actor interface, so the initialize() method of the refinement is called. Please
 note that this feature is still under development.
 <p>
 The <i>nondeterministic</i> parameter specifies whether this transition is
 nondeterministic. Here nondeterministic means that this transition may not
 be the only enabled transition at a time. The default value is a boolean
 token with value as false, meaning that if this transition is enabled, it
 must be the only enabled transition.
 <p>
 The <i>defaultTransition</i> parameter, if given a value true, specifies
 that this transition is enabled if and only if no other non-default
 transition is enabled.

 @author Xiaojun Liu, Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 @see State
 @see Action
 @see ChoiceAction
 @see CommitAction
 @see CommitActionsAttribute
 @see FSMActor
 @see OutputActionsAttribute
 */
public class Transition extends ComponentRelation {
    /** Construct a transition with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This transition will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  @param container The container.
     *  @param name The name of the transition.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public Transition(FSMActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct a transition in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version
     *  tracking.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public Transition(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>preemptive</i> parameter, evaluate the parameter. If the
     *  parameter is given an expression that does not evaluate to a
     *  boolean value, throw an exception; otherwise increment the
     *  version number of the workspace.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or the changed attribute is the
     *   <i>preemptive</i> parameter and is given an expression that
     *   does not evaluate to a boolean value.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == preemptive) {
            // evaluate the parameter to make sure it is given a valid
            // expression
            preemptive.getToken();
            workspace().incrVersion();
        } else if (attribute == nondeterministic) {
            _nondeterministic = ((BooleanToken) nondeterministic.getToken())
                    .booleanValue();
        } else if (attribute == guardExpression) {
            // The guard expression can only be evaluated at run
            // time, because the input variables it can reference are created
            // at run time. The guardExpression is a string
            // attribute used to convey expressions without being evaluated.
            // _guard is the variable that does the evaluation.
            _guardParseTree = null;
            _guardParseTreeVersion = -1;
            _parseTreeEvaluatorVersion = -1;
        } else if (attribute == refinementName) {
            _refinementVersion = -1;
        } else if ((attribute == outputActions) || (attribute == setActions)) {
            _actionListsVersion = -1;
        } else {
            super.attributeChanged(attribute);
        }

        if ((attribute == outputActions) && _debugging) {
            outputActions.addDebugListener(new StreamListener());
        } else if ((attribute == setActions) && _debugging) {
            setActions.addDebugListener(new StreamListener());
        }
    }

    /** Return the list of choice actions contained by this transition.
     *  @return The list of choice actions contained by this transition.
     */
    public List choiceActionList() {
        if (_actionListsVersion != workspace().getVersion()) {
            _updateActionLists();
        }

        return _choiceActionList;
    }

    /** Clone the transition into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer to
     *  the attributes of the new transition.
     *  @param workspace The workspace for the new transition.
     *  @return A new transition.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Transition newObject = (Transition) super.clone(workspace);
        newObject.guardExpression = (StringAttribute) newObject
                .getAttribute("guardExpression");
        newObject.preemptive = (Parameter) newObject.getAttribute("preemptive");
        newObject.refinementName = (StringAttribute) newObject
                .getAttribute("refinementName");
        newObject._guardParseTree = null;
        newObject._guardParseTreeVersion = -1;
        newObject._actionListsVersion = -1;
        newObject._choiceActionList = new LinkedList();
        newObject._commitActionList = new LinkedList();
        newObject._stateVersion = -1;
        return newObject;
    }

    /** Return the list of commit actions contained by this transition.
     *  @return The list of commit actions contained by this transition.
     */
    public List commitActionList() {
        if (_actionListsVersion != workspace().getVersion()) {
            _updateActionLists();
        }

        return _commitActionList;
    }

    /** Return the destination state of this transition.
     *  @return The destination state of this transition.
     */
    public State destinationState() {
        if (_stateVersion != workspace().getVersion()) {
            _checkConnectedStates();
        }

        return _destinationState;
    }

    /** Return the guard expression. The guard expression should evaluate
     *  to a boolean value.
     *  @return The guard expression.
     *  @see #setGuardExpression
     */
    public String getGuardExpression() {
        return guardExpression.getExpression();
    }

    /** Return a string describing this transition. The string has up to
     *  three lines. The first line is the guard expression, preceded
     *  by "guard: ".  The second line is the <i>outputActions</i> preceded
     *  by the string "output: ". The third line is the
     *  <i>setActions</i> preceded by the string "set: ". If any of these
     *  is missing, then the corresponding line is omitted.
     *  @return A string describing this transition.
     */
    public String getLabel() {
        StringBuffer buffer = new StringBuffer("");

        boolean hasAnnotation = false;
        String text;
        try {
            text = annotation.stringValue();
        } catch (IllegalActionException e) {
            text = "Exception evaluating annotation: " + e.getMessage();
        }
        if (!text.trim().equals("")) {
            hasAnnotation = true;
            buffer.append(text);
        }

        String guard = getGuardExpression();
        if ((guard != null) && !guard.trim().equals("")) {
            if (hasAnnotation) {
                buffer.append("\n");
            }
            buffer.append("guard: ");
            buffer.append(guard);
        }

        String expression = outputActions.getExpression();
        if ((expression != null) && !expression.trim().equals("")) {
            buffer.append("\n");
            buffer.append("output: ");
            buffer.append(expression);
        }

        expression = setActions.getExpression();
        if ((expression != null) && !expression.trim().equals("")) {
            buffer.append("\n");
            buffer.append("set: ");
            buffer.append(expression);
        }

        return buffer.toString();
    }

    /** Return the parse tree evaluator used by this transition to evaluate
     *  the guard expression.
     *  @return ParseTreeEvaluator for evaluating the guard expression.
     */
    public ParseTreeEvaluator getParseTreeEvaluator() {
        if (_parseTreeEvaluatorVersion != workspace().getVersion()) {
            // If there is no current parse tree evaluator,
            // then create one. If this transition is under the control
            // of an FSMDirector, then delegate creation to that director.
            // Otherwise, create a default instance of ParseTreeEvaluator.
            FSMDirector director = _getDirector();
            if (director != null) {
                _parseTreeEvaluator = director.getParseTreeEvaluator();
            } else {
                // When this transition is used inside an FSMActor.
                if (_parseTreeEvaluator == null) {
                    _parseTreeEvaluator = new ParseTreeEvaluator();
                }
            }
            _parseTreeEvaluatorVersion = workspace().getVersion();
        }
        return _parseTreeEvaluator;
    }

    /** Return the refinements of this transition. The names of the refinements
     *  are specified by the <i>refinementName</i> attribute. The refinements
     *  must be instances of TypedActor and have the same container as
     *  the FSMActor containing this state, otherwise an exception is thrown.
     *  This method can also return null if there is no refinement.
     *  This method is read-synchronized on the workspace.
     *  @return The refinements of this state, or null if there are none.
     *  @exception IllegalActionException If the specified refinement
     *   cannot be found, or if a comma-separated list is malformed.
     */
    public TypedActor[] getRefinement() throws IllegalActionException {
        if (_refinementVersion == workspace().getVersion()) {
            return _refinement;
        }

        try {
            workspace().getReadAccess();

            String names = refinementName.getExpression();

            if ((names == null) || names.trim().equals("")) {
                _refinementVersion = workspace().getVersion();
                _refinement = null;
                return null;
            }

            StringTokenizer tokenizer = new StringTokenizer(names, ",");
            int size = tokenizer.countTokens();

            if (size <= 0) {
                _refinementVersion = workspace().getVersion();
                _refinement = null;
                return null;
            }

            _refinement = new TypedActor[size];

            Nameable container = getContainer();
            TypedCompositeActor containerContainer = (TypedCompositeActor) container
                    .getContainer();
            int index = 0;

            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken().trim();

                if (name.equals("")) {
                    throw new IllegalActionException(this,
                            "Malformed list of refinements: " + names);
                }

                TypedActor element = (TypedActor) containerContainer
                        .getEntity(name);

                if (element == null) {
                    throw new IllegalActionException(this, "Cannot find "
                            + "refinement with name \"" + name + "\" in "
                            + containerContainer.getFullName());
                }

                _refinement[index++] = element;
            }

            _refinementVersion = workspace().getVersion();
            return _refinement;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return true if this transition is a default transition. Return false
     *  otherwise.
     *  @return True if this transition is a default transition.
     *  @exception IllegalActionException If the defaultTransition parameter
     *   cannot be evaluated.
     */
    public boolean isDefault() throws IllegalActionException {
        return ((BooleanToken) defaultTransition.getToken()).booleanValue();
    }

    /** Return true if the transition is enabled, that is the guard is true, or
     *  some event has been detected due to crossing some level.
     *  @return True if the transition is enabled and some event is detected.
     *  @exception IllegalActionException If thrown when evaluating the guard.
     */
    public boolean isEnabled() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof FSMActor) {
            return isEnabled(((FSMActor) container).getPortScope());
        } else {
            return false;
        }
    }

    /** Return true if the transition is enabled, that is the guard is true, or
     *  some event has been detected due to crossing some level.
     *  @param scope The parser scope in which the guard is to be evaluated.
     *  @return True If the transition is enabled and some event is detected.
     *  @exception IllegalActionException If thrown when evaluating the guard.
     */
    public boolean isEnabled(ParserScope scope) throws IllegalActionException {
        ParseTreeEvaluator parseTreeEvaluator = getParseTreeEvaluator();
        if (_guardParseTree == null
                || _guardParseTreeVersion != _workspace.getVersion()) {
            String expr = getGuardExpression();
            // Parse the guard expression.
            PtParser parser = new PtParser();
            try {
                _guardParseTree = parser.generateParseTree(expr);
                _guardParseTreeVersion = _workspace.getVersion();
            } catch (IllegalActionException ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to parse guard expression \"" + expr + "\"");
            }
        }
        Token token = parseTreeEvaluator.evaluateParseTree(_guardParseTree,
                scope);
        if (token == null) {
            // FIXME: when could this happen??
            return false;
        }
        boolean result = ((BooleanToken) token).booleanValue();
        return result;
    }

    /** Return true if this transition is nondeterministic. Return false
     *  otherwise.
     *  @return True if this transition is nondeterministic.
     */
    public boolean isNondeterministic() {
        return _nondeterministic;
    }

    /** Return true if this transition is preemptive. Whether this transition
     *  is preemptive is specified by the <i>preemptive</i> parameter.
     *  @return True if this transition is preemptive.
     */
    public boolean isPreemptive() {
        try {
            return ((BooleanToken) preemptive.getToken()).booleanValue();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(preemptive.getFullName()
                    + ": The parameter does not have a valid value, \""
                    + preemptive.getExpression() + "\".");
        }
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of FSMActor or null; if it is null, then
     *  remove it from the container, and also remove any refinement(s)
     *  that it references that are not referenced by some other
     *  transition or state.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the transition would result
     *   in a recursive containment structure, or if
     *   this transition and container are not in the same workspace, or
     *   if the argument is not a FSMActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an relation with the name of this transition.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null) {
            if (!(container instanceof FSMActor)) {
                throw new IllegalActionException(container, this,
                        "Transition can only be contained by instances of "
                                + "FSMActor.");
            }
        }

        super.setContainer(container);
    }

    /** Set the guard expression. The guard expression should evaluate
     *  to a boolean value.
     *  @param expression The guard expression.
     *  @see #getGuardExpression
     */
    public void setGuardExpression(String expression) {
        try {
            guardExpression.setExpression(expression);
            guardExpression.validate();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Error in setting the "
                    + "guard expression of a transition.");
        }
    }

    /** Return the source state of this transition.
     *  @return The source state of this transition.
     */
    public State sourceState() {
        if (_stateVersion != workspace().getVersion()) {
            _checkConnectedStates();
        }

        return _sourceState;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** An annotation that describes the transition. If this is non-empty,
     *  then a visual editor will be expected to put this annotation on
     *  or near the transition to document its function. This is a string
     *  that defaults to the empty string. Note that it can reference
     *  variables in scope using the notation $name.
     */
    public StringParameter annotation;

    /** Indicator that this transition is a default transition. A
     *  default transition is enabled only if no other non-default
     *  transition is enabled.  This is a boolean with default value
     *  false. If the value is true, then the guard expression is
     *  ignored.
     */
    public Parameter defaultTransition = null;

    /** Attribute the exit angle of a visual rendition.
     *  This parameter contains a DoubleToken, initially with value PI/5.
     *  It must lie between -PI and PI.  Otherwise, it will be truncated
     *  to lie within this range.
     */
    public Parameter exitAngle;

    /** Attribute giving the orientation of a self-loop. This is equal to
     * the tangent at the midpoint (more or less).
     *  This parameter contains a DoubleToken, initially with value 0.0.
     */
    public Parameter gamma;

    /** Attribute specifying the guard expression.
     */
    public StringAttribute guardExpression = null;

    /** Parameter specifying whether this transition is nondeterministic.
     *  Here nondeterministic means that this transition may not be the only
     *  enabled transition at a time. The default value is a boolean token
     *  with value as false, meaning that if this transition is enabled, it
     *  must be the only enabled transition.
     */
    public Parameter nondeterministic = null;

    /** The action commands that produce outputs when the transition is taken.
     */
    public OutputActionsAttribute outputActions;

    /** Parameter specifying whether this transition is preemptive.
     */
    public Parameter preemptive = null;

    /** Attribute specifying one or more names of refinements. The
     *  refinements must be instances of TypedActor and have the same
     *  container as the FSMActor containing this state, otherwise
     *  an exception will be thrown when getRefinement() is called.
     *  Usually, the refinement is a single name. However, if a
     *  comma-separated list of names is provided, then all the specified
     *  refinements will be executed.
     *  This attribute has a null expression or a null string as
     *  expression when the state is not refined.
     */
    public StringAttribute refinementName = null;

    /** Parameter specifying whether the refinement of the destination
     *  state is reset when the transition is taken.
     */
    public Parameter reset = null;

    /** The action commands that set parameters when the transition is taken.
     */
    public CommitActionsAttribute setActions;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an IllegalActionException if the port cannot be linked
     *  to this transition. A transition has a source state and a
     *  destination state. A transition is only linked to the outgoing
     *  port of its source state and the incoming port of its destination
     *  state.
     *  @exception IllegalActionException If the port cannot be linked
     *   to this transition.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
        super._checkPort(port);

        if (!(port.getContainer() instanceof State)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "Transition can only connect to instances of State.");
        }

        State st = (State) port.getContainer();

        if ((port != st.incomingPort) && (port != st.outgoingPort)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "Transition can only be linked to incoming or outgoing "
                            + "port of State.");
        }

        if (numLinks() == 0) {
            return;
        }

        if (numLinks() >= 2) {
            throw new IllegalActionException(this,
                    "Transition can only connect two States.");
        }

        Iterator ports = linkedPortList().iterator();
        Port pt = (Port) ports.next();
        State s = (State) pt.getContainer();

        if (((pt == s.incomingPort) && (port == st.incomingPort))
                || ((pt == s.outgoingPort) && (port == st.outgoingPort))) {
            throw new IllegalActionException(this,
                    "Transition can only have one source and one destination.");
        }

        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Check the states connected by this transition, cache the result.
    // This method is read-synchronized on the workspace.
    private void _checkConnectedStates() {
        try {
            workspace().getReadAccess();

            Iterator ports = linkedPortList().iterator();
            _sourceState = null;
            _destinationState = null;

            while (ports.hasNext()) {
                Port p = (Port) ports.next();
                State s = (State) p.getContainer();

                if (p == s.incomingPort) {
                    _destinationState = s;
                } else {
                    _sourceState = s;
                }
            }

            _stateVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the FSMDirector in charge of this transition,
     *  or null if there is none.
     *  @return The director in charge of this transition.
     */
    private FSMDirector _getDirector() {
        // Get the containing FSMActor.
        NamedObj container = getContainer();
        if (container != null) {
            // Get the containing modal model.
            CompositeActor modalModel = (CompositeActor) container
                    .getContainer();
            if (modalModel != null) {
                // Get the director for the modal model.
                Director director = modalModel.getDirector();
                if (director instanceof FSMDirector) {
                    return (FSMDirector) director;
                }
            }
        }
        return null;
    }

    // Initialize the variables of this transition.
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        annotation = new StringParameter(this, "annotation");
        annotation.setExpression("");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        Variable variable = new Variable(annotation, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);

        guardExpression = new StringAttribute(this, "guardExpression");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        variable = new Variable(guardExpression, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);

        outputActions = new OutputActionsAttribute(this, "outputActions");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        variable = new Variable(outputActions, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);

        setActions = new CommitActionsAttribute(this, "setActions");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        variable = new Variable(setActions, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);

        exitAngle = new Parameter(this, "exitAngle");
        exitAngle.setVisibility(Settable.NONE);
        exitAngle.setExpression("PI/5.0");
        exitAngle.setTypeEquals(BaseType.DOUBLE);
        gamma = new Parameter(this, "gamma");
        gamma.setVisibility(Settable.NONE);
        gamma.setExpression("0.0");
        gamma.setTypeEquals(BaseType.DOUBLE);
        reset = new Parameter(this, "reset");
        reset.setTypeEquals(BaseType.BOOLEAN);
        reset.setToken(BooleanToken.FALSE);
        preemptive = new Parameter(this, "preemptive");
        preemptive.setTypeEquals(BaseType.BOOLEAN);
        preemptive.setToken(BooleanToken.FALSE);

        // default attributes.
        defaultTransition = new Parameter(this, "defaultTransition");
        defaultTransition.setTypeEquals(BaseType.BOOLEAN);
        defaultTransition.setToken(BooleanToken.FALSE);

        // Nondeterministic attributes.
        nondeterministic = new Parameter(this, "nondeterministic");
        nondeterministic.setTypeEquals(BaseType.BOOLEAN);
        nondeterministic.setToken(BooleanToken.FALSE);

        // Add refinement name parameter
        refinementName = new StringAttribute(this, "refinementName");
    }

    // Update the cached lists of actions.
    // This method is read-synchronized on the workspace.
    private void _updateActionLists() {
        try {
            workspace().getReadAccess();
            _choiceActionList.clear();
            _commitActionList.clear();

            Iterator actions = attributeList(Action.class).iterator();

            while (actions.hasNext()) {
                Action action = (Action) actions.next();

                if (action instanceof ChoiceAction) {
                    _choiceActionList.add(action);
                }

                if (action instanceof CommitAction) {
                    _commitActionList.add(action);
                }
            }

            _actionListsVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Version of cached lists of actions.
    private long _actionListsVersion = -1;

    // Cached list of choice actions contained by this transition.
    private List _choiceActionList = new LinkedList();

    // Cached list of commit actions contained by this Transition.
    private List _commitActionList = new LinkedList();

    // Cached destination state of this transition.
    private State _destinationState = null;

    // The parse tree for the guard expression.
    private ASTPtRootNode _guardParseTree;

    // Version of the cached guard parse tree
    private long _guardParseTreeVersion = -1;

    // Cached nondeterministic attribute value.
    private boolean _nondeterministic = false;

    // The parse tree evaluator for the transition.
    // Note that this variable should not be accessed directly even inside
    // this class. Instead, always use the getParseTreeEvaluator() method.
    private ParseTreeEvaluator _parseTreeEvaluator;

    // Version of the cached parse tree evaluator
    private long _parseTreeEvaluatorVersion = -1;

    // Cached reference to the refinement of this state.
    private TypedActor[] _refinement = null;

    // Version of the cached reference to the refinement.
    private long _refinementVersion = -1;

    // Cached source state of this transition.
    private State _sourceState = null;

    // Version of cached source/destination state.
    private long _stateVersion = -1;
}
