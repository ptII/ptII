/* Code generator helper for FSMActor.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.modal.kernel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.ComponentCodeGenerator;
import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.domains.modal.kernel.AbstractActionsAttribute;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// FSMActor

/**
 Code generator helper for FSMActor.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class FSMActor extends CCodeGeneratorHelper {
    /** Construct the code generator helper associated with the given FSMActor.
     *  @param component The associated component.
     */
    public FSMActor(ptolemy.domains.modal.kernel.FSMActor component) {
        super(component);
    }

    /////////////////////////////////////////////////////////////////////
    ////                           public methods                    ////

    /** Generate the fire code of the associated FSMActor.  It provides
     *  generateTransitionCode(StringBuffer, TransitionRetriever) with an
     *  anonymous class implementing a method which returns an iterator of
     *  all outgoing transitions of the current state.
     *
     *  @return The generated fire code.
     *  @exception IllegalActionException If thrown while generating
     *  firing code.
     */
    protected String _generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        ptolemy.domains.modal.kernel.FSMActor fsmActor =
            (ptolemy.domains.modal.kernel.FSMActor) getComponent();

        // FIXME: not handling multirate inputs yet.
        // FIXME: how should we handle in-out ports?
        for (IOPort input : (List<IOPort>) fsmActor.inputPortList()) {
            for (int channel = 0; !input.isOutput() &&
            channel < input.getWidth(); channel++) {

                code.append("$get(" + generateSimpleName(input)
                        + ", " + channel + ")" + _eol);
            }
        }

        generateTransitionCode(code, new TransitionRetriever() {
            public Iterator retrieveTransitions(State state) {
                return state.outgoingPort.linkedRelationList().iterator();
            }
        });
        return processCode(code.toString());
    }

    /** Generate the initialize code of the associated FSMActor. It
     *  generates code for initializing current state with initial
     *  state, and initializing current configuration of the container
     *  when it applies (i.e., when this FSMActor works as a modal
     *  controller for a MultirateFSMDirector).
     *
     *  @return The initialize code of the associated FSMActor.
     *  @exception IllegalActionException If initial state cannot be found,
     *   configuration number cannot be updated or code cannot be processed.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append(super.generateInitializeCode());

        ptolemy.domains.modal.kernel.FSMActor fsmActor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();
        State initialState = fsmActor.getInitialState();

        _updateCurrentState(codeBuffer, initialState);

        return processCode(codeBuffer.toString());
    }

    /** Generate the preinitialize code of the associated FSMActor. It
     *  declares two variables for this actor: currentState and transitionFlag.
     *  currentState is an int representing this actor's current state.
     *  transitionFlag is an unsigned char to indicate if a preemptive
     *  transition is taken. It also defines a symbolic constant to each
     *  state.
     *  @return The preinitialize code of the associated FSMActor.
     *  @exception IllegalActionException If thrown when creating buffer
     *   size and offset map or processing code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        ptolemy.domains.modal.kernel.FSMActor fsmActor =
            (ptolemy.domains.modal.kernel.FSMActor) getComponent();

        ArrayList args = new ArrayList(2);
        int index = 0;
        args.add("");
        args.add("");
        for (State state : (List<State>) fsmActor.entityList()) {
            args.set(0, _generateStateConstantLabel(state));
            args.set(1, index++);
            code.append(_generateBlockCode("defineState", args));
        }

        // FIXME: Having this code here breaks the test cases under
        // $PTII/codegen/c/domains/modal/test/. This code is probably
        // specific to a target (e.g. OpenRTOS or PRET), so it should
        // be moved into an subclass which overrides this method. The
        // subclass should be put under the target-specific packages.

//        code.append("//I should generate the methods for my internal actors here. My actors are:");
//
//        for (Actor actor : (List<Actor>)
//                ((TypedCompositeActor)  this.getDirector().getContainer()).deepEntityList()) {
//            code.append(_eol+"void "+_getActorName(actor)+"(){"+_eol);
//           //CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
//           //code.append( actorHelper.generateFireCode());
//           code.append("}"+_eol);
//
//        }

        return processCode(code.toString());
    }

    private Object _generateStateConstantLabel(State state) {
        return "STATE_" + generateName(state);
    }

    /** Generate code for making transition. It generates code for both choice
     *  action and commit action.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @param transitionRetriever An instance of a class implementing
     *  a method.
     *   which returns an iterator of all, preemptive or non-preemptive
     *   transitions of the current state.
     *  @exception IllegalActionException If thrown while generating
     *  transition code.
     */
    public void generateTransitionCode(StringBuffer code,
            TransitionRetriever transitionRetriever)
    throws IllegalActionException {
        StringBuffer codeBuffer = new StringBuffer();

        ptolemy.domains.modal.kernel.FSMActor fsmActor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();

        // The default value 1 of transitionFlag means the transition
        // will be taken. If no transition is actually taken, it will be
        // set to value 0.
        codeBuffer.append("$actorSymbol(transitionFlag) = 1;" + _eol);

        // States are numbered according to the order they are created,
        // i.e., the same order as in list returned by the method entityList().
        codeBuffer.append("switch ($actorSymbol(currentState)) {" + _eol);

        for (State state : (List<State>) fsmActor.entityList()) {
            // For each state...
            codeBuffer.append("case " + _generateStateConstantLabel(state) + ":" + _eol);

            // The transitions (all, preemptive or non-preemptive
            // depending on the instance of TransitionRetriever given)
            // that need to be tried.
            Iterator transitions = transitionRetriever
            .retrieveTransitions(state);
            // Reorder transitions so that default transitions are at
            // the end of the list.
            List reOrderedTransitions = new LinkedList();
            List defaultTransitions = new LinkedList();
            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();
                if (generateSimpleName(transition).equals("default")) {
                    defaultTransitions.add(transition);
                } else {
                    reOrderedTransitions.add(transition);
                }
            }
            reOrderedTransitions.addAll(defaultTransitions);
            transitions = reOrderedTransitions.iterator();

            int transitionCount = 0;

            boolean hasDefaultCase = false;
            while (!hasDefaultCase && transitions.hasNext()) {

                Transition transition = (Transition) transitions.next();

                String guard = transition.getGuardExpression();

                if (transition.isDefault() ||
                        guard.toLowerCase().equals("true")) {

                    // We don't need to generate if-predicate for this,
                    // and we can skip the rest of the transitions.
                    // FIXME: Need to handle nondeterministic transitions.
                    hasDefaultCase = true;

                } else {
                    // generate code for guard expression
                    if (transitionCount == 0) {
                        codeBuffer.append("if (");
                    } else {
                        codeBuffer.append("else if (");
                    }
                    transitionCount++;

                    PtParser parser = new PtParser();
                    ASTPtRootNode guardParseTree =
                        parser.generateParseTree(guard);

                    ParseTreeCodeGenerator parseTreeCodeGenerator =
                        getParseTreeCodeGenerator();
                    parseTreeCodeGenerator.evaluateParseTree(
                            guardParseTree, _scope);

                    codeBuffer.append(
                            parseTreeCodeGenerator.generateFireCode());

                    codeBuffer.append(") ");
                }
                codeBuffer.append("{" + _eol);

                // generate code for choice action
                for (AbstractActionsAttribute action :
                    (List <AbstractActionsAttribute>) transition.choiceActionList()) {

                    Iterator channelNumberList = action.getChannelNumberList()
                    .iterator();
                    Iterator parseTreeList = action.getParseTreeList()
                    .iterator();

                    for (String destinationName :
                        (List<String>) action.getDestinationNameList()) {

                        Integer channelNumber = (Integer) channelNumberList
                        .next();
                        ASTPtRootNode parseTree = (ASTPtRootNode) parseTreeList
                        .next();
                        NamedObj destination = action
                        .getDestination(destinationName);

                        int channel = -1;
                        if (channelNumber != null) {
                            channel = channelNumber.intValue();
                        }

                        StringBuffer sendCode = new StringBuffer();

                        // Note in choice action only output can be generated
                        // and no parameter be changed.
                        if (channel >= 0) {
                            codeBuffer.append("$ref(" + destinationName + "#"
                                    + channel + ") = ");

                            // During choice action, an output port
                            // receives token sent by itself when it
                            // is also an input port, i.e., when this
                            // FSMActor is used as a modal controller.

                            if (((IOPort) destination).isInput()) {
                                ComponentCodeGenerator containerHelper = _getHelper(((IOPort) destination)
                                        .getContainer().getContainer());
                                StringBuffer containerReference = new StringBuffer();

                                containerReference.append("$ref("
                                        + generateSimpleName(destination));

                                if (((IOPort) destination).isMultiport()) {
                                    containerReference.append("#" + channel);
                                }

                                containerReference.append(")");

                                codeBuffer
                                .append(((CodeGeneratorHelper) containerHelper)
                                        .processCode(containerReference
                                                .toString())
                                                + " = ");

                                sendCode.append("$send(" + destinationName + ", " + channel + ")" + _eol);
                            }
                        } else { // broadcast

                            int width = ((IOPort) action
                                    .getDestination(destinationName))
                                    .getWidth();

                            for (int i = 0; i < width; i++) {
                                codeBuffer.append("$ref(" + destinationName
                                        + "#" + i + ") = ");

                                sendCode.append("$send(" + destinationName + ", " + i + ")" + _eol);

                                // During choice action, an output
                                // port receives token sent by itself
                                // when it is also an input port,
                                // i.e., when this FSMActor is used as
                                // a modal controller.

                                if (((IOPort) destination).isInput()) {
                                    ComponentCodeGenerator containerHelper = _getHelper(((IOPort) destination)
                                            .getContainer().getContainer());
                                    StringBuffer containerReference = new StringBuffer();

                                    containerReference.append("$ref("
                                            + generateSimpleName(destination));

                                    if (((IOPort) destination).isMultiport()) {
                                        containerReference.append("#" + i);
                                    }

                                    containerReference.append(")");

                                    codeBuffer
                                    .append(((CodeGeneratorHelper) containerHelper)
                                            .processCode(containerReference
                                                    .toString())
                                                    + " = ");

                                    sendCode.append("$send(" + generateSimpleName(destination) + ", " + i + ")" + _eol);
                                }
                            }
                        }

                        ParseTreeCodeGenerator parseTreeCodeGenerator = getParseTreeCodeGenerator();
                        parseTreeCodeGenerator.evaluateParseTree(parseTree,
                                _scope);
                        codeBuffer.append(parseTreeCodeGenerator
                                .generateFireCode());
                        codeBuffer.append(";" + _eol);
                        codeBuffer.append(sendCode);
                    }
                }

                // generate code for transition refinement
                Actor[] actors = transition.getRefinement();

                if (actors != null) {
                    for (int i = 0; i < actors.length; i++) {
                        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actors[i]);
                        // fire the actor
                        codeBuffer.append(helper.generateFireCode());
                    }
                }

                // generate code for commit action
                for (AbstractActionsAttribute action :
                    (List <AbstractActionsAttribute>) transition.commitActionList()) {

                    Iterator channelNumberList = action.getChannelNumberList()
                    .iterator();
                    Iterator parseTreeList = action.getParseTreeList()
                    .iterator();

                    for (String destinationName :
                        (List<String>) action.getDestinationNameList()) {

                        Integer channelNumber = (Integer) channelNumberList
                        .next();
                        ASTPtRootNode parseTree = (ASTPtRootNode) parseTreeList
                        .next();
                        NamedObj destination =
                            action.getDestination(destinationName);

                        int channel = -1;
                        if (channelNumber != null) {
                            channel = channelNumber.intValue();
                        }

                        if (destination instanceof IOPort) {
                            if (channel >= 0) {
                                codeBuffer.append("$ref(" + destinationName
                                        + "#" + channel + ") = ");
                            } else { // broadcast

                                int width = ((IOPort) action
                                        .getDestination(destinationName))
                                        .getWidth();

                                for (int i = 0; i < width; i++) {
                                    codeBuffer.append("$ref(" + destinationName
                                            + "#" + i + ") = ");
                                }
                            }
                        } else if (destination instanceof Variable) {
                            codeBuffer
                            .append(_codeGenerator
                                    .generateVariableName(destination)
                                    + " = ");
                        }

                        ParseTreeCodeGenerator parseTreeCodeGenerator = getParseTreeCodeGenerator();
                        parseTreeCodeGenerator.evaluateParseTree(parseTree,
                                _scope);
                        codeBuffer.append(parseTreeCodeGenerator
                                .generateFireCode());
                        codeBuffer.append(";" + _eol);
                    }
                }

                // generate code for updating current state
                State destinationState = transition.destinationState();
                _updateCurrentState(codeBuffer, destinationState);

                // generate code for reinitialization if reset is
                // true.  we assume the value of reset itself cannot
                // be changed dynamically

                BooleanToken resetToken = (BooleanToken) transition.reset
                .getToken();
                if (resetToken.booleanValue()) {
                    actors = destinationState.getRefinement();

                    if (actors != null) {
                        for (int i = 0; i < actors.length; ++i) {
                            ActorCodeGenerator helper = (ActorCodeGenerator) _getHelper((NamedObj) actors[i]);
                            codeBuffer.append(helper.generateInitializeCode());
                        }
                    }
                }

                // Generate code for updating configuration number of
                // this FSMActor's container.  The code is generated
                // only when this FSMActor is used as a modal
                // controller for an instance of MultirateFSMDirector.

                Director director = fsmActor.getExecutiveDirector();
                if (director instanceof ptolemy.domains.modal.kernel.MultirateFSMDirector) {
                    MultirateFSMDirector directorHelper = (MultirateFSMDirector) _getHelper(director);
                    directorHelper._updateConfigurationNumber(codeBuffer,
                            destinationState);
                }
                codeBuffer.append("} ");
            }

            if (!hasDefaultCase) {
                if (transitionCount > 0) {
                    codeBuffer.append("else {" + _eol);
                } else {
                    codeBuffer.append(_eol);
                }

                // indicates no transition is taken.
                codeBuffer.append("$actorSymbol(transitionFlag) = 0;" + _eol);

                // Generate code for updating configuration number of this
                // FSMActor's container.  Note we need this because the
                // configuration of the current refinement may have been
                // changed even when there is no state transition.  The
                // code is generated only when this FSMActor is used as a
                // modal controller for an instance of
                // MultirateFSMDirector.

                Director director = fsmActor.getExecutiveDirector();
                if (director instanceof ptolemy.domains.modal.kernel.MultirateFSMDirector) {
                    MultirateFSMDirector directorHelper = (MultirateFSMDirector) _getHelper(director);
                    directorHelper._updateConfigurationNumber(codeBuffer, state);
                }

                if (transitionCount > 0) {
                    codeBuffer.append("} " + _eol); // end of if statement
                }
            }
            codeBuffer.append(_eol + "break;" + _eol); // end of case statement
        }

        codeBuffer.append("}" + _eol); // end of switch statement
        code.append(processCode(codeBuffer.toString()));
    }

    /** A class implementing this interface implements a method to
     *  retrieve transitions of a given state. Depending on
     *  implementation, it could return all transitions, only
     *  preemptive transitions or only non-preemptive transitions.
     */
    public static interface TransitionRetriever {

        /** Returns an iterator of (some or all) transitions from the
         * given state.
         *  @param state The given state.
         *  @return An iterator of (some or all) transitions from the
         *  given state.
         */
        public Iterator retrieveTransitions(State state);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for updating current state of this FSMActor. The
     *  states are numbered according to the order in the list
     *  returned by entityList().
     *
     *  @param codeBuffer The string buffer that the generated code is
     *  appended to.
     *  @param state The current state.
     * @exception IllegalActionException Thrown if the corresponding code
     *  block cannot be fetched.
     */
    protected void _updateCurrentState(StringBuffer codeBuffer, State state)
    throws IllegalActionException {
        ArrayList args = new ArrayList(1);
        args.add(_generateStateConstantLabel(state));
        codeBuffer.append(_generateBlockCode("updateCurrentState", args));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    /** The scope to generate code for guard expression, choice action
     *  and commit action.
     */
    protected PortScope _scope = new PortScope();

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** This class implements a scope, which is used to generate the
     *  parsed expressions in target language.
     */
    protected class PortScope extends VariableScope {
        /** Construct a scope consisting of the variables of the containing
         *  actor and its containers and their scope-extending attributes.
         */
        public PortScope() {
            super();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Look up and return the macro or expression in the target language
         *  corresponding to the specified name in the scope.
         *  @param name The given name string.
         *  @return The macro or expression with the specified name in the scope.
         *  @exception IllegalActionException If thrown while getting buffer
         *   sizes or creating ObjectToken.
         */
        public Token get(String name) throws IllegalActionException {
            Iterator inputPorts = ((Actor) getComponent()).inputPortList()
            .iterator();

            // try input port
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();

                StringBuffer code = new StringBuffer();
                boolean found = false;
                int channelNumber = 0;
                // try input port name only
                if (name.equals(generateSimpleName(inputPort))) {
                    found = true;
                    code.append(generateName(inputPort));
                    if (inputPort.isMultiport()) {
                        code.append("[0]");
                    }
                } else {
                    for (int i = 0; i < inputPort.getWidth(); i++) {
                        // try the format: inputPortName_channelNumber
                        if (name.equals(generateSimpleName(inputPort) + "_" + i)) {
                            found = true;
                            channelNumber = i;
                            code.append(generateName(inputPort));
                            code.append("[" + i + "]");
                            break;
                        }
                    }
                }
                if (found) {
                    int bufferSize = getBufferSize(inputPort);
                    if (bufferSize > 1) {
                        int bufferSizeOfChannel = getBufferSize(inputPort,
                                channelNumber);
                        String writeOffset = getWriteOffset(inputPort,
                                channelNumber).toString();
                        // Note here inputPortNameArray in the original expression
                        // is converted to
                        // inputPortVariable[(writeOffset - 1
                        // + bufferSizeOfChannel)&(bufferSizeOfChannel-1)]
                        // in the generated C code.
                        code.append("[(" + writeOffset + " + "
                                + (bufferSizeOfChannel - 1) + ")&"
                                + (bufferSizeOfChannel - 1) + "]");
                    }
                    return new ObjectToken(code.toString());
                }

                // try the format: inputPortNameArray
                found = false;
                channelNumber = 0;
                if (name.equals(generateSimpleName(inputPort) + "Array")) {
                    found = true;
                    code.append(generateName(inputPort));
                    if (inputPort.isMultiport()) {
                        code.append("[0]");
                    }
                } else {
                    for (int i = 0; i < inputPort.getWidth(); i++) {
                        // try the format: inputPortName_channelNumberArray
                        if (name.equals(generateSimpleName(inputPort) + "_" + i + "Array")) {
                            found = true;
                            channelNumber = i;
                            code.append(generateName(inputPort));
                            code.append("[" + i + "]");
                            break;
                        }
                    }
                }
                if (found) {
                    int bufferSize = getBufferSize(inputPort);
                    if (bufferSize > 1) {
                        int bufferSizeOfChannel = getBufferSize(inputPort,
                                channelNumber);
                        String writeOffset = (String) getWriteOffset(inputPort,
                                channelNumber);
                        // '@' represents the array index in the parsed expression.
                        // It will be replaced by actual array index in
                        // the method visitFunctionApplicationNode() in
                        // ParseTreeCodeGenerator.
                        // Note here inputPortNameArray(i) in the original expression
                        // is converted to
                        // inputPortVariable[(writeOffset - i - 1
                        // + bufferSizeOfChannel)&(bufferSizeOfChannel-1)]
                        // in the generated C code.
                        code.append("[(" + writeOffset + " - (@)" + " + "
                                + (bufferSizeOfChannel - 1) + ")&"
                                + (bufferSizeOfChannel - 1) + "]");
                    }
                    return new ObjectToken(code.toString());
                }

            }

            // try variable
            return super.get(name);
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @param name The name of the attribute.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.type.Type getType(String name)
        throws IllegalActionException {
            return ((ptolemy.domains.modal.kernel.FSMActor) getComponent())
            .getPortScope().getType(name);
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @param name The name of the attribute.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
        throws IllegalActionException {
            return ((ptolemy.domains.modal.kernel.FSMActor) getComponent())
            .getPortScope().getTypeTerm(name);
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of variable names within the scope.
         *  @exception IllegalActionException If thrown while getting
         *  the identifier set from associated component.
         */
        public Set identifierSet() throws IllegalActionException {
            return ((ptolemy.domains.modal.kernel.FSMActor) getComponent())
            .getPortScope().identifierSet();
        }
    }
}
