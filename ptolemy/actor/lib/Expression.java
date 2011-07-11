/* An actor that evaluates expressions.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Expression

/**
 <p>On each firing, evaluate an expression that may include references
 to the inputs, current time, and a count of the firing.  The ports
 are referenced by the identifiers that have the same name as the
 port.  To use this class, instantiate it, then add ports (instances
 of TypedIOPort).  In vergil, you can add ports by right clicking on
 the icon and selecting "Configure Ports".  In MoML you can add
 ports by just including ports of class TypedIOPort, set to be
 inputs, as in the following example:</p>

 <pre>
 &lt;entity name="exp" class="ptolemy.actor.lib.Expression"&gt;
 &lt;port name="in" class="ptolemy.actor.TypedIOPort"&gt;
 &lt;property name="input"/&gt;
 &lt;/port&gt;
 &lt;/entity&gt;
 </pre>

 <p> This actor is type-polymorphic.  The types of the inputs can be
 arbitrary and the types of the outputs are inferred from the
 expression based on the types inferred for the inputs.</p>

 <p> The <i>expression</i> parameter specifies an expression that
 can refer to the inputs by name.  By default, the expression is
 empty, and attempting to execute the actor without setting it
 triggers an exception.  This actor can be used instead of many of
 the arithmetic actors, such as AddSubtract, MultiplyDivide, and
 TrigFunction.  However, those actors will be usually be more
 efficient, and sometimes more convenient to use.</p>

 <p> The expression language understood by this actor is the same as
 <a href="../../../../expressions.htm">that used to set any
 parameter value</a>, with the exception that the expressions
 evaluated by this actor can refer to the values of inputs, and to
 the current time by the identifier name "time", and to the current
 iteration count by the identifier named "iteration."</p>

 <p> This actor requires its all of its inputs to be present.  If
 inputs are not all present, then an exception will be thrown.</p>

 <p> NOTE: There are a number of limitations in the current
 implementation.  Primarily, multiports are not supported.</p>

 @author Xiaojun Liu, Edward A. Lee, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Green (neuendor)
 */
public class Expression extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Expression(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        expression = new StringAttribute(this, "expression");
        expression.setExpression("");

        Parameter hide = new Parameter(expression, "_hide");
        hide.setExpression("true");

        _setOutputTypeConstraint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. */
    public TypedIOPort output;

    /** The expression that is evaluated to produce the output.
     */
    public StringAttribute expression;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the value of an attribute.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == expression) {
            _parseTree = null;
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Expression newObject = (Expression) super.clone(workspace);
        newObject._iterationCount = 1;
        newObject._parseTree = null;
        newObject._parseTreeEvaluator = null;
        newObject._scope = null;
        newObject._setOutputTypeConstraint();
        newObject._tokenMap = null;
        return newObject;
    }

    /** Evaluate the expression and send its result to the output.
     *  @exception IllegalActionException If the evaluation of the expression
     *   triggers it, or the evaluation yields a null result, or the evaluation
     *   yields an incompatible type, or if there is no director, or if a
     *   connected input has no tokens.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) (inputPorts.next());

            // FIXME: Handle multiports
            if (port.isOutsideConnected()) {
                if (port.hasToken(0)) {
                    Token inputToken = port.get(0);
                    _tokenMap.put(port.getName(), inputToken);
                } else {
                    throw new IllegalActionException(this, "Input port "
                            + port.getName() + " has no data.");
                }
            }
        }

        Token result;

        try {
            // Note: this code parallels code in the OutputTypeFunction class
            // below.
            if (_parseTree == null) {
                // Note that the parser is NOT retained, since in most
                // cases the expression doesn't change, and the parser
                // requires a large amount of memory.
                PtParser parser = new PtParser();
                _parseTree = parser.generateParseTree(expression
                        .getExpression());
            }

            if (_parseTreeEvaluator == null) {
                _parseTreeEvaluator = new ParseTreeEvaluator();
            }

            if (_scope == null) {
                _scope = new VariableScope();
            }

            result = _parseTreeEvaluator.evaluateParseTree(_parseTree, _scope);
        } catch (Throwable throwable) {
            // Chain exceptions to get the actor that threw the exception.
            // Note that if evaluateParseTree does a divide by zero, we
            // need to catch an ArithmeticException here.
            throw new IllegalActionException(this, throwable,
                    "Expression invalid.");
        }

        if (result == null) {
            throw new IllegalActionException(this,
                    "Expression yields a null result: "
                            + expression.getExpression());
        }

        output.send(0, result);
    }

    /** Initialize the iteration count to 1.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (getPort("time") != null) {
            throw new IllegalActionException(
                    this,
                    "This actor has a port named \"time\", "
                            + "which will not be read, instead the "
                            + "reserved system variable \"time\" will be read. "
                            + "Delete the \"time\" port to avoid this message.");
        }
        if (getPort("iteration") != null) {
            throw new IllegalActionException(
                    this,
                    "This actor has a port named \"iteration\", "
                            + "which will not be read, instead the "
                            + "reserved system variable \"iteration\" will be read. "
                            + "Delete the \"iteration\" port to avoid this message.");
        }
        _iterationCount = 1;
    }

    /** Increment the iteration count.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _iterationCount++;

        // This actor never requests termination.
        return true;
    }

    /** Prefire this actor.  Return false if an input port has no
     *  data, otherwise return true.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) (inputPorts.next());

            // FIXME: Handle multiports
            if (port.isOutsideConnected()) {
                if (!port.hasToken(0)) {
                    return false;
                }
            }
        }

        return super.prefire();
    }

    /** Preinitialize this actor.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _tokenMap = new HashMap();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Add a constraint to the type output port of this object.
    private void _setOutputTypeConstraint() {
        output.setTypeAtLeast(new OutputTypeFunction());
    }

    private class VariableScope extends ModelScope {
        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Token get(String name) throws IllegalActionException {
            if (name.equals("time")) {
                return new DoubleToken(getDirector().getModelTime()
                        .getDoubleValue());
            } else if (name.equals("iteration")) {
                return new IntToken(_iterationCount);
            }

            Token token = (Token) _tokenMap.get(name);

            if (token != null) {
                return token;
            }

            Variable result = getScopedVariable(null, Expression.this, name);

            if (result != null) {
                return result.getToken();
            }

            return null;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Type getType(String name) throws IllegalActionException {
            if (name.equals("time")) {
                return BaseType.DOUBLE;
            } else if (name.equals("iteration")) {
                return BaseType.INT;
            }

            // Check the port names.
            TypedIOPort port = (TypedIOPort) getPort(name);

            if (port != null) {
                return port.getType();
            }

            Variable result = getScopedVariable(null, Expression.this, name);

            if (result != null) {
                return (Type) result.getTypeTerm().getValue();
            }

            return null;
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
            if (name.equals("time")) {
                return new TypeConstant(BaseType.DOUBLE);
            } else if (name.equals("iteration")) {
                return new TypeConstant(BaseType.INT);
            }

            // Check the port names.
            TypedIOPort port = (TypedIOPort) getPort(name);

            if (port != null) {
                return port.getTypeTerm();
            }

            Variable result = getScopedVariable(null, Expression.this, name);

            if (result != null) {
                return result.getTypeTerm();
            }

            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        public Set identifierSet() {
            return getAllScopedVariableNames(null, Expression.this);
        }
    }

    // This class implements a monotonic function of the type of
    // the output port.
    // The function value is determined by type inference on the
    // expression, in the scope of this Expression actor.
    private class OutputTypeFunction extends MonotonicFunction {
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         *  @exception IllegalActionException If inferring types for the
         *  expression fails.
         */
        public Object getValue() throws IllegalActionException {
            try {
                // Deal with the singularity at UNKNOWN..  Assume that if
                // any variable that the expression depends on is UNKNOWN,
                // then the type of the whole expression is unknown..
                // This allows us to properly find functions that do exist
                // (but not for UNKNOWN arguments), and to give good error
                // messages when functions are not found.
                InequalityTerm[] terms = getVariables();

                for (int i = 0; i < terms.length; i++) {
                    InequalityTerm term = terms[i];

                    if ((term != this) && (term.getValue() == BaseType.UNKNOWN)) {
                        return BaseType.UNKNOWN;
                    }
                }

                // Note: This code is similar to the token evaluation
                // code above.
                if (_parseTree == null) {
                    // Note that the parser is NOT retained, since in most
                    // cases the expression doesn't change, and the parser
                    // requires a large amount of memory.
                    PtParser parser = new PtParser();
                    _parseTree = parser.generateParseTree(expression
                            .getExpression());
                }

                if (_scope == null) {
                    _scope = new VariableScope();
                }

                Type type = _typeInference.inferTypes(_parseTree, _scope);
                return type;
            } catch (Exception ex) {
                throw new IllegalActionException(Expression.this, ex,
                        "An error occurred during expression type inference of \""
                                + expression.getExpression() + "\".");
            }
        }

        /** Return the type variable in this inequality term. If the type
         *  of input ports are not declared, return an one element array
         *  containing the inequality term representing the type of the port;
         *  otherwise, return an empty array.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            // Return an array that contains type terms for all of the
            // inputs and all of the parameters that are free variables for
            // the expression.
            try {
                if (_parseTree == null) {
                    PtParser parser = new PtParser();
                    _parseTree = parser.generateParseTree(expression
                            .getExpression());
                }

                if (_scope == null) {
                    _scope = new VariableScope();
                }

                Set set = _variableCollector.collectFreeVariables(_parseTree,
                        _scope);
                List termList = new LinkedList();

                for (Iterator elements = set.iterator(); elements.hasNext();) {
                    String name = (String) elements.next();
                    InequalityTerm term = _scope.getTypeTerm(name);

                    if ((term != null) && term.isSettable()) {
                        termList.add(term);
                    }
                }

                return (InequalityTerm[]) termList
                        .toArray(new InequalityTerm[termList.size()]);
            } catch (IllegalActionException ex) {
                return new InequalityTerm[0];
            }
        }

        /** Override the base class to give a description of this term.
         *  @return A description of this term.
         */
        public String getVerboseString() {
            return expression.getExpression();
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////
        private ParseTreeTypeInference _typeInference = new ParseTreeTypeInference();

        private ParseTreeFreeVariableCollector _variableCollector = new ParseTreeFreeVariableCollector();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _iterationCount = 1;

    private ASTPtRootNode _parseTree = null;

    private ParseTreeEvaluator _parseTreeEvaluator = null;

    private VariableScope _scope = null;

    private Map _tokenMap;
}
