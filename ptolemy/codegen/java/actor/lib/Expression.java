/* A code generation helper class for actor.lib.Expression
 @Copyright (c) 2005-2009 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.java.actor.lib;

import java.util.Set;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.java.kernel.JavaCodeGeneratorHelper;
import ptolemy.codegen.java.kernel.JavaParseTreeCodeGenerator;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

/**
 * A code generation helper class for ptolemy.actor.lib.Expression.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit) Needs 2nd pass for array children of different types
 * @Pt.AcceptedRating Red (mankit)
 */
public class Expression extends JavaCodeGeneratorHelper {
    /**
     * Constructor method for the Expression helper.
     * @param actor The associated actor.
     */
    public Expression(ptolemy.actor.lib.Expression actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Expression.c,
     * replaces macros with their values and appends the processed code
     * block to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        //        Type portType = ((ptolemy.actor.lib.Expression) this.getComponent()).output
        //                .getType();
        //
        //        // if port type is not primitive, then we use token type
        //        if ((portType != BaseType.DOUBLE) && (portType != BaseType.INT)
        //                && (portType != BaseType.STRING)
        //                && (portType != BaseType.BOOLEAN)) {
        //            portType = BaseType.GENERAL;
        //        }

        //code.append(processCode("    $ref(output)." + portType + "Port = ("
        //        + _javaParseTreeCodeGenerator.generateFireCode()) + ");" + _eol);
        if (_javaParseTreeCodeGenerator == null) {
            throw new NullPointerException("_javaParseTreeCodeGenerator is null? "
                                           + " This can happen if there is an odd order of execution");
        }
        if (_javaParseTreeCodeGenerator.generateFireCode() == null) {
            throw new NullPointerException("calling generateFireCode on "         
                    + _javaParseTreeCodeGenerator + " returned null");
        }
        code.append(processCode("    $ref(output) = "
                + _javaParseTreeCodeGenerator.generateFireCode())
                + ";" + _eol);
        return code.toString();
    }

    /**
     * Generate initialize code.
     * This method reads the <code>initBlock</code> from Expression.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());
        code.append(processCode(_javaParseTreeCodeGenerator
                .generateInitializeCode()));
        return code.toString();
    }

    /**
     * Generate preinitialize code.
     * This method reads the <code>preinitBlock</code> from Expression.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        if (_javaParseTreeCodeGenerator == null) {
            // FIXME: why does this need to be done here?
            _javaParseTreeCodeGenerator = new JavaParseTreeCodeGenerator();
        }
        ptolemy.actor.lib.Expression actor = (ptolemy.actor.lib.Expression) getComponent();

        try {
            // Note that the parser is NOT retained, since in most
            // cases the expression doesn't change, and the parser
            // requires a large amount of memory.
//            if (1 == 0) {
//                // Debugging
//                PtParser parser = new PtParser();
//                ASTPtRootNode parseTree = parser
//                        .generateParseTree(actor.expression.getExpression());
//
//                System.out.println("Expression trace:\n"
//                        + _javaParseTreeCodeGenerator.traceParseTreeEvaluation(
//                                parseTree, new VariableScope(actor)));
//            }
            PtParser parser = new PtParser();
            ASTPtRootNode parseTree = parser.generateParseTree(actor.expression
                    .getExpression());

            _javaParseTreeCodeGenerator.evaluateParseTree(parseTree,
                    new VariableScope(actor));
        } catch (IllegalActionException ex) {
            // Chain exceptions to get the actor that threw the exception.
            throw new IllegalActionException(actor, ex, "Expression \""
                    + actor.expression.getExpression() + "\" invalid.");
        }

        code.append(processCode(_javaParseTreeCodeGenerator
                .generatePreinitializeCode()));
        return code.toString();
    }

    /**
     * Get shared code.  This method reads the
     * <code>sharedBlock</code> from Expression.c, replaces macros
     * with their values and returns the processed code string.
     * @return A set of strings that are code shared by multiple instances of
     *  the same actor.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public Set getSharedCode() throws IllegalActionException {
        _javaParseTreeCodeGenerator = new JavaParseTreeCodeGenerator();

        Set codeBlocks = super.getSharedCode();
        codeBlocks.add(processCode(_javaParseTreeCodeGenerator
                .generateSharedCode()));
        return codeBlocks;
    }

    /**
     * Generate wrap up code.
     * This method reads the <code>wrapupBlock</code>
     * from Expression.c,
     * replaces macros with their values and appends the processed code block
     * to the given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateWrapupCode());
        code.append(processCode(_javaParseTreeCodeGenerator
                .generateWrapupCode()));

        // Free up memory
        _javaParseTreeCodeGenerator = null;
        return code.toString();
    }

    /** The parse tree code generator. */
    protected JavaParseTreeCodeGenerator _javaParseTreeCodeGenerator;

    /**
     * Variable scope class customized for the JavaParseTreeCodeGenerator.
     */
    protected static class VariableScope extends ModelScope {
        // Findbugs suggests that this should be static.
        /**
         * Constructor of a VariableScope.
         * @param actor The named ptolemy actor.
         */
        public VariableScope(AtomicActor actor) {
            _actor = actor;
        }

        /** Look up and return the attribute with the specified name.
         *  Return null if such an attribute does not exist.
         *  @param name The name to look up.
         *  @return The attribute with the specified name in the scope.
         */
        public Token get(String name) {
            try {
                if (name.equals("time")) {
                    // If the director has the period set to something other
                    // than 0, then return the value of _iteration * PERIOD
                    // See codegen/kernel/StaticSchedulingCodeGenerator.java.
                    // FIXME: should we check for period being set anywhere
                    // in the hierarchy?
                    Director director = _actor.getDirector();
                    Attribute period = director.getAttribute("period");
                    if (period != null) {
                        Double periodValue = ((DoubleToken) ((Variable) period)
                                .getToken()).doubleValue();
                        if (periodValue != 0.0) {
                            return new ObjectToken("_iteration * PERIOD");
                        }
                    }
                    return new DoubleToken("0.0");
                } else if (name.equals("iteration")) {
                    return new ObjectToken("$actorSymbol(iterationCount)");
                }

                for (int i = 0; i < _actor.inputPortList().size(); i++) {
                    if (generateSimpleName(
                            ((IOPort) _actor.inputPortList().get(i))).equals(
                            name)) {
                        return new ObjectToken("$ref(" + name + ")");
                    }
                }

                Attribute attribute = _actor.getAttribute(name);

                if (attribute == null) {
                    attribute = ModelScope
                            .getScopedVariable(null, _actor, name);
                }

                if (attribute != null) {
                    return new ObjectToken("$val(" + name + ")");
                }

                /*
                 for (int i = 0; i < _actor.attributeList().size(); i++) {
                 if (((Attribute) _actor.attributeList().get(i))
                 .getName().equals(name)) {
                 return new ObjectToken("$val(" + name + ")");
                 }
                 }
                 */
            } catch (IllegalActionException ex) {
                // Not thrown here.
                throw new InternalErrorException(ex);
            }

            return null;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @param name The type to look up.  Note that if name
         *  is "time", then the type is BaseType.DOUBLE and if the
         *  name is "iterations", then the type is BaseType.INT.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If thrown whil getting
         *  the port or scoped value.
         */
        public Type getType(String name) throws IllegalActionException {
            if (name.equals("time")) {
                return BaseType.DOUBLE;
            } else if (name.equals("iteration")) {
                return BaseType.INT;
            }

            // Check the port names.
            TypedIOPort port = (TypedIOPort) _actor.getPort(name);

            if (port != null) {
                return port.getType();
            }

            Variable result = getScopedVariable(null, _actor, name);

            if (result != null) {
                return (Type) result.getTypeTerm().getValue();
            }

            return null;
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @param name The name to look up.
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
            TypedIOPort port = (TypedIOPort) _actor.getPort(name);

            if (port != null) {
                return port.getTypeTerm();
            }

            Variable result = getScopedVariable(null, _actor, name);

            if (result != null) {
                return result.getTypeTerm();
            }

            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        public Set identifierSet() {
            return getAllScopedVariableNames(null, _actor);
        }

        private AtomicActor _actor;
    }
}
