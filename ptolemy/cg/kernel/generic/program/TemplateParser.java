/* A class to parse the template macro constructs in a code generation scope.

Copyright (c) 2009-2010 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.program;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.CGException;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.ParseTreeCodeGenerator;
import ptolemy.cg.kernel.generic.PortCodeGenerator;
import ptolemy.data.ArrayToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////TemplateParser

/**
A class that allows to parse macros of templates in a code generator
perspective.


@author Bert Rodiers
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public class TemplateParser {

    /** Construct the TemplateParser associated
     *  with the given component and the given adapter.
     */
    public TemplateParser() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a function to the Set of function used thus far.
     *  @param functionName A string naming the function.
     */
    public void addFunctionUsed(String functionName) {
        _codeGenerator._typeFuncUsed.add(functionName);
    }

    /**
     * Generate expression that evaluates to a result of equivalent
     * value with the cast type.
     * @param expression The given variable expression.
     * @param castType The given cast type.
     * @param refType The given type of the variable.
     * @return The variable expression that evaluates to a result of
     *  equivalent value with the cast type.
     * @exception IllegalActionException
     */
    public String generateTypeConvertMethod(String expression, String castType,
            String refType) throws IllegalActionException {

        if (castType == null || refType == null || castType.equals(refType)) {
            return expression;
        }

        expression = "$convert_" + refType + "_" + castType + "(" + expression
                + ")";

        return processCode(expression);
    }

    /**
     * Generate the type conversion statement for the particular offset of
     * the two given channels. This assumes that the offset is the same for
     * both channel. Advancing the offset of one has to advance the offset of
     * the other.
     * <p>
     * If alternativeSourceRef is not null, then we use this instead of the source
     * itself. Thus we generate the type conversion statement with alternativeSourceRef
     * on the right side of the equal sign, and a reference to the sink on the left
     * side.
     * @param source The given source channel.
     * @param sink The given sink channel.
     * @param offset The given offset._generateTypeConvertStatement
     * @param alternativeSourceRef The alternative source reference for
     * the port.  If alternativeSourceRef is null, then the adapter for the port
     * of the source channel is used.
     * @return The type convert statement for assigning the converted source
     *  variable to the sink variable with the given offset.
     * @exception IllegalActionException If there is a problem getting the
     * adapters for the ports or if the conversion cannot be handled.
     */
    public String generateTypeConvertStatement(
            ProgramCodeGeneratorAdapter.Channel source,
            ProgramCodeGeneratorAdapter.Channel sink, int offset,
            String alternativeSourceRef) throws IllegalActionException {

        Type sourceType = ((TypedIOPort) source.port).getType();
        Type sinkType = ((TypedIOPort) sink.port).getType();

        // In a modal model, a refinement may have an output port which is
        // not connected inside, in this case the type of the port is
        // unknown and there is no need to generate type conversion code
        // because there is no token transferred from the port.
        if (sourceType == BaseType.UNKNOWN) {
            return "";
        }

        // The references are associated with their own adapter, so we need
        // to find the associated adapter.
        String sourcePortChannel = source.port.getName() + "#"
                + source.channelNumber + ", " + offset;
        String sourceRef;

        if (alternativeSourceRef == null) {
            sourceRef = ((NamedProgramCodeGeneratorAdapter) _codeGenerator
                    .getAdapter(source.port.getContainer()))
                    .getReference(sourcePortChannel, true);
        } else {
            sourceRef = alternativeSourceRef;
        }

        String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
                + ", " + offset;

        // For composite actor, generate a variable corresponding to
        // the inside receiver of an output port.
        // FIXME: I think checking sink.port.isOutput() is enough here.
        if (sink.port.getContainer() instanceof CompositeActor
                && sink.port.isOutput()) {
            sinkPortChannel = "@" + sinkPortChannel;
        }
        String sinkRef = ((NamedProgramCodeGeneratorAdapter) _codeGenerator
                .getAdapter(sink.port.getContainer())).getReference(
                sinkPortChannel, false);

        // When the sink port is contained by a modal controller, it is
        // possible that the port is both input and output port. we need
        // to pay special attention. Directly calling getReference() will
        // treat it as output port and this is not correct.
        // FIXME: what about offset?
        if (sink.port.getContainer() instanceof ModalController) {
            sinkRef = NamedProgramCodeGeneratorAdapter.generateName(sink.port);
            if (sink.port.isMultiport()) {
                sinkRef = sinkRef + "[" + sink.channelNumber + "]";
            }
        }

        String result = sourceRef;

        if (!sinkType.equals(sourceType)) {
            if (_codeGenerator.isPrimitive(sinkType)) {
                result = _codeGenerator.codeGenType(sourceType) + "to"
                        + _codeGenerator.codeGenType(sinkType) + "(" + result
                        + ")";

            } else if (_codeGenerator.isPrimitive(sourceType)) {
                result = "$new(" + _codeGenerator.codeGenType(sourceType) + "("
                        + result + "))";
            }

            if (sinkType != BaseType.SCALAR && sinkType != BaseType.GENERAL
                    && !_codeGenerator.isPrimitive(sinkType)) {
                if (sinkType instanceof ArrayType) {
                    if (_codeGenerator.isPrimitive(sourceType)) {
                        result = "$new(" + _codeGenerator.codeGenType(sinkType)
                                + "(1, 1, " + result + ", TYPE_"
                                + _codeGenerator.codeGenType(sourceType) + "))";
                    }

                    // Deep converting for ArrayType.
                    Type elementType = ((ArrayType) sinkType).getElementType();
                    while (elementType instanceof ArrayType) {
                        elementType = ((ArrayType) elementType)
                                .getElementType();
                    }

                    if (elementType != BaseType.SCALAR
                            && elementType != BaseType.GENERAL) {
                        result = "$typeFunc(TYPE_"
                                + _codeGenerator.codeGenType(sinkType)
                                + "::convert("
                                + result
                                + ", /*CGH*/ TYPE_"
                                + _codeGenerator
                                        .codeGenType(((ArrayType) sinkType)
                                                .getElementType()) + "))";
                    }

                } else {
                    result = "$typeFunc(TYPE_"
                            + _codeGenerator.codeGenType(sinkType)
                            + "::convert(" + result + "))";
                }
            }
        }
        return sinkRef + " = " + result + ";"
                + StringUtilities.getProperty("line.separator");
    }

    /** Return the code stream.
     * @return The code stream.
     */
    final public CodeStream getCodeStream() {
        return _codeStream;
    }

    /** Get the files needed by the code generated from this adapter class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this adapter class.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public Set<String> getHeaderFiles() throws IllegalActionException {
        Set<String> files = new HashSet<String>();

        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("includeFiles", true);
        String includeFilesString = codeStream.toString();

        if (includeFilesString.length() > 0) {
            LinkedList<String> includeFilesList = null;
            try {
                includeFilesList = StringUtilities
                        .readLines(includeFilesString);
            } catch (IOException e) {
                CGException.throwException(_component,
                        "Unable to read include files for ");
            }
            files.addAll(includeFilesList);
        }

        return files;
    }

    /** Return the value or an expression in the target language for
     *  the specified parameter of the associated actor.  If the
     *  parameter is specified by an expression, then the expression
     *  will be parsed. If any parameter referenced in that expression
     *  is specified by another expression, the parsing continues
     *  recursively until either a parameter is directly specified by
     *  a constant or a parameter can be directly modified during
     *  execution in which case a reference to the parameter is
     *  generated.
     *
     *  @param name The name of the parameter.
     *  @param container The container to search upwards from.
     *  @return The value or expression as a string.
     *  @exception IllegalActionException If the parameter does not exist or
     *   does not have a value.
     */
    final public String getParameterValue(String name, NamedObj container)
            throws IllegalActionException {
        if (name.contains("$")) {
            name = processCode(name);
        }

        StringTokenizer tokenizer = new StringTokenizer(name, ",");

        String attributeName = tokenizer.nextToken().trim();
        String offset = null;
        String castType = null;

        if (tokenizer.hasMoreTokens()) {
            offset = tokenizer.nextToken().trim();

            if (tokenizer.hasMoreTokens()) {
                CGException.throwException(_component, name
                        + " does not have the correct format for"
                        + " accessing the parameter value.");
            }
        }

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(attributeName, "()",
                false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            CGException.throwException(_component, "Invalid cast type: "
                    + attributeName);
        }

        if (tokenizer2.countTokens() == 2) {
            castType = tokenizer2.nextToken().trim();
            attributeName = tokenizer2.nextToken().trim();
        }

        Attribute attribute = ModelScope.getScopedVariable(null, container,
                attributeName);

        if (attribute == null) {
            attribute = container.getAttribute(attributeName);
            if (attribute == null) {
                CGException.throwException(container, "No attribute named: "
                        + name);
            }
        }

        if (offset == null) {
            if (attribute instanceof Variable) {
                // FIXME: need to ensure that the returned string
                // is correct syntax for the target language.
                Variable variable = (Variable) attribute;

                /*
                 if (_codeGenerator._modifiedVariables.contains(variable)) {
                 return generateVariableName(variable);
                 } else if (variable.isStringMode()) {
                 return "\"" + variable.getExpression() + "\"";
                 }
                 */

                ParseTreeCodeGenerator parseTreeCodeGenerator = getParseTreeCodeGenerator();
                if (variable.isStringMode()) {
                    return generateTypeConvertMethod("\""
                            + StringUtilities.escapeString(parseTreeCodeGenerator
                                    .escapeForTargetLanguage(variable
                                            .getExpression()))
                            + "\"", castType,
                            "String");
                }

                PtParser parser = new PtParser();
                ASTPtRootNode parseTree = null;
                try {
                    parseTree = parser.generateParseTree(variable
                            .getExpression());
                } catch (Throwable throwable) {
                    CGException.throwException(variable, throwable,
                            "Failed to generate parse tree for \"" + name
                                    + "\". in \"" + container + "\"");
                }
                try {
                    parseTreeCodeGenerator.evaluateParseTree(parseTree,
                            new VariableScope(variable));
                } catch (Exception ex) {
                    StringBuffer results = new StringBuffer();
                    Iterator<?> allScopedVariableNames = ModelScope
                            .getAllScopedVariableNames(variable, container)
                            .iterator();
                    while (allScopedVariableNames.hasNext()) {
                        results.append(allScopedVariableNames.next().toString()
                                + "\n");
                    }
                    CGException.throwException(_component, ex,
                            "Failed to find " + variable.getFullName() + "\n"
                                    + results.toString());

                }

                String fireCode = processCode(parseTreeCodeGenerator
                        .generateFireCode());

                //if (castType == null && codeGenType(variable.getType()).equals("Array")) {
                // FIXME: this is a gross hack necessary for Case.
                // The problem is that if the refinement is named "{0}", then
                // we get into trouble because {0} is "false"?  sigh.
                //    return "Array_new(1, 1, " + fireCode + ");";
                //}
                return generateTypeConvertMethod(fireCode, castType,
                        _getCodeGenerator().codeGenType(variable.getType()));

            } else /* if (attribute instanceof Settable)*/{
                return ((Settable) attribute).getExpression();
            }

            // FIXME: Are there any other values that a
            // parameter might have?
            //CGException.throwException(_component,
            //        "Attribute does not have a value: " + name);
        } else {
            // FIXME: if offset != null, for now we assume the value of
            // the parameter is fixed during execution.
            if (attribute instanceof Parameter) {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof ArrayToken) {
                    Token element = ((ArrayToken) token).getElement(Integer
                            .valueOf(offset).intValue());

                    /////////////////////////////////////////////////////
                    ParseTreeCodeGenerator parseTreeCodeGenerator = getParseTreeCodeGenerator();
                    PtParser parser = new PtParser();
                    ASTPtRootNode parseTree = null;
                    try {
                        parseTree = parser
                                .generateParseTree(element.toString());
                    } catch (Throwable throwable) {
                        CGException.throwException(attribute, throwable,
                                "Failed to generate parse tree for \"" + name
                                        + "\". in \"" + container + "\"");
                    }
                    parseTreeCodeGenerator.evaluateParseTree(parseTree,
                            new VariableScope((Parameter) attribute));

                    String elementCode = processCode(parseTreeCodeGenerator
                            .generateFireCode());
                    /////////////////////////////////////////////////////

                    return generateTypeConvertMethod(elementCode, castType,
                            _getCodeGenerator().codeGenType(element.getType()));
                }

                CGException.throwException(_component, attributeName
                        + " does not contain an ArrayToken.");
            }

            CGException.throwException(_component, attributeName
                    + " is not a parameter.");
        }
        return ""; // We never get here
    }

    /** Return the parse tree to use with expressions.
     *  @return the parse tree to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        return _parseTreeCodeGenerator;
    }

    /**
     * Return the index of a specific character in the string starting
     * from the given index. It find the first occurence of the character
     * that is not embedded inside parentheses "()".
     * @param ch The character to search for.
     * @param string The given string to search from.
     * @param fromIndex The index to start the search.
     * @return The first occurence of the character in the string that
     *  is not embedded in parentheses.
     */
    public static int indexOf(String ch, String string, int fromIndex) {

        int parenIndex = fromIndex;
        int result = -1;
        int closedParenIndex = parenIndex;

        do {
            result = string.indexOf(ch, closedParenIndex);

            parenIndex = string.indexOf('(', closedParenIndex);

            if (parenIndex >= 0) {
                try {
                    closedParenIndex = _findClosedParen(string, parenIndex);
                } catch (IllegalActionException e) {
                    closedParenIndex = -1;
                }
            }
        } while (result > parenIndex && result < closedParenIndex);

        return result;
    }

    /** Parse the list of comma separted parameters.
     *  @param parameters A comma separate list of parameters.   
     *  @return A list of parameters.
     */
    public static List<String> parseList(String parameters) {
        // FIXME: Why not use StringTokenizer here?
        List<String> result = new ArrayList<String>();
        int previousCommaIndex = 0;
        int commaIndex = indexOf(",", parameters, 0);

        while (commaIndex >= 0) {
            String item = parameters.substring(previousCommaIndex, commaIndex)
                    .trim();

            result.add(item);

            previousCommaIndex = commaIndex + 1;
            commaIndex = indexOf(",", parameters, previousCommaIndex);
        }

        String item = parameters.substring(previousCommaIndex,
                parameters.length()).trim();

        if (item.trim().length() > 0) {
            result.add(item);
        }

        return result;
    }

    /** Init the TemplateParser with the associated
     *  given component and the given adapter.
     *  @param component The associated component.
     *  @param adapter The associated adapter.
     */
    public void init(Object component, ProgramCodeGeneratorAdapter adapter) {
        _component = component;
        _codeGenerator = adapter.getCodeGenerator();
        _codeStream = new CodeStream(adapter);

        _parseTreeCodeGenerator = new ParseTreeCodeGenerator() {
            /** Given a string, escape special characters as necessary for the
             *  target language.
             *  @param string The string to escape.
             *  @return A new string with special characters replaced.
             */
            public String escapeForTargetLanguage(String string) {
                return string;
            }

            /** Evaluate the parse tree with the specified root node using
             *  the specified scope to resolve the values of variables.
             *  @param node The root of the parse tree.
             *  @param scope The scope for evaluation.
             *  @return The result of evaluation.
             *  @exception IllegalActionException If an error occurs during
             *   evaluation.
             */
            public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node,
                    ParserScope scope) {
                return new Token();
            }

            /** Generate code that corresponds with the fire() method.
             *  @return The generated code.
             */
            public String generateFireCode() {
                return "/* ParseTreeCodeGenerator.generateFireCode() "
                        + "not implemented in codegen.kernel.GenericCodeGenerator */";
            }
        };
    }

    /** Process the specified code, replacing macros with their values.
     * @param code The code to process.
     * @return The processed code.
     * @exception IllegalActionException If illegal macro names are found.
     */
    final public String processCode(String code) throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        int currentPos = _getMacroStartIndex(code, 0);

        if (currentPos < 0) {
            // No "$" in the string
            return code;
        }

        result.append(code.substring(0, currentPos));

        while (currentPos < code.length()) {
            int openParenIndex = code.indexOf("(", currentPos + 1);
            if (openParenIndex == -1) {
                CGException.throwException(_component,
                        "Failed to find open paren in \"" + code + "\".");
            }
            int closeParenIndex = _findClosedParen(code, openParenIndex);

            if (closeParenIndex < 0) {
                // No matching close parenthesis is found.
                result.append(code.substring(currentPos));
                return result.toString();
            }

            int nextPos = _getMacroStartIndex(code, closeParenIndex + 1);

            if (nextPos < 0) {
                //currentPos is the last "$"
                nextPos = code.length();
            }

            String subcode = code.substring(currentPos, nextPos);

            if ((currentPos > 0) && (code.charAt(currentPos - 1) == '\\')) {
                // found "\$", do not make replacement.
                // FIXME: This is wrong. subcode may contain other macros
                // to be processed.
                // Should be result.append(processCode(subcode.substring(1)));
                result.append(subcode);
                currentPos = nextPos;
                continue;
            }

            String macro = code.substring(currentPos + 1, openParenIndex);
            macro = macro.trim();

            //if (!_codeGenerator.getMacros().contains(macro)) {
            //    result.append(subcode.substring(0, 1));
            //    result.append(processCode(subcode.substring(1)));
            //} else {
            String name = code.substring(openParenIndex + 1, closeParenIndex);

            name = processCode(name.trim());

            //List arguments = parseArgumentList(name);

            try {
                result.append(_replaceMacro(macro, name));
            } catch (Throwable throwable) {
                CGException.throwException(this, throwable,
                        "Failed to replace the parameter \"" + name
                                + "\" in the macro \"" + macro
                                + "\".\nInitial code was:\n" + code);
            }

            String string = code.substring(closeParenIndex + 1, nextPos);
            result.append(string);
            //}
            currentPos = nextPos;
        }

        return result.toString();
    }

    /** Given a block name, generate code for that block.
     *  This method is called by actors adapters that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @param args The arguments for the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the adapter .c file.
     */
    public String generateBlockCode(String blockName, List<String> args)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        CodeStream codeStream = getCodeStream();
        codeStream.clear();
        codeStream.appendCodeBlock(blockName, args);
        return processCode(codeStream.toString());
    }

    /** Return the translated token instance function invocation string.
     *  @param functionString The string within the $tokenFunc() macro.
     *  @param isStatic True if the method is static.
     *  @return The translated type function invocation string.
     *  @exception IllegalActionException The given function string is
     *   not well-formed.
     */
    public String getFunctionInvocation(String functionString, boolean isStatic)
            throws IllegalActionException {
        String initialFunctionString = functionString;

        functionString = processCode(functionString);

        // i.e. "$tokenFunc(token::add(arg1, arg2, ...))"
        // this transforms to ==>
        // "functionTable[token.type][FUNC_add] (token, arg1, arg2, ...)"
        // FIXME: we need to do some more smart parsing to find the following
        // indexes.
        int commaIndex = functionString.indexOf("::");
        int openFuncParenIndex = functionString.indexOf('(', commaIndex);
        int closeFuncParenIndex = functionString.lastIndexOf(')');

        // Syntax checking.
        if (commaIndex == -1) {
                CGException
                    .throwException("Bad Syntax with the $tokenFunc / $typeFunc macro. "
                            + "[i.e. -- $tokenFunc(typeOrToken::func(arg1, ...))].  "
                            + "The string \"::\" was not found. "
                            + "Processed String was:\n:" + functionString
                            + "Initial String was:\n:" + initialFunctionString);
        }

        if (openFuncParenIndex == -1) {
            CGException
                    .throwException("Bad Syntax with the $tokenFunc / $typeFunc macro. "
                            + "[i.e. -- $tokenFunc(typeOrToken::func(arg1, ...))].  "
                            + "No \"(\" found after \"::\". "
                            + "Processed String was:\n:" + functionString
                            + "Initial String was:\n:" + initialFunctionString);
        }

        if ((closeFuncParenIndex != (functionString.length() - 1))) {
            CGException
                    .throwException("Bad Syntax with the $tokenFunc / $typeFunc macro. "
                            + "[i.e. -- $tokenFunc(typeOrToken::func(arg1, ...))].  "
                            + "The last \")\" was not last character? "
                            + "The last \")\" was at " + closeFuncParenIndex
                            + "Processed String was:\n:" + functionString
                            + "Initial String was:\n:" + initialFunctionString);
        }

        String typeOrToken = functionString.substring(0, commaIndex).trim();
        String functionName = functionString.substring(commaIndex + 2,
                openFuncParenIndex).trim();

        String argumentList = functionString.substring(openFuncParenIndex + 1)
                .trim();

        if (isStatic) {
            // Record the referenced type function in the infoTable.
            if (_codeGenerator == null) {
                throw new NullPointerException("Call TemplateParser.setCodeGenerator() "
                        + "before calling getFunctionInvocation()");
            }
            _codeGenerator._typeFuncUsed.add(functionName);

            if (argumentList.length() == 0) {
                CGException
                        .throwException("Static type function requires at least one argument(s).");
            }

            return "functionTable[(int)" + typeOrToken + "][FUNC_"
                    + functionName + "](" + argumentList;

        } else {
            // Record the referenced type function in the infoTable.
            _codeGenerator._tokenFuncUsed.add(functionName);

            // if it is more than just a closing paren
            if (argumentList.length() > 1) {
                argumentList = ", " + argumentList;
            }

            return "functionTable[(int)" + typeOrToken + ".type][FUNC_"
                    + functionName + "](" + typeOrToken + argumentList;
        }
    }

    /** Return the translated new constructor invocation string. Keep the types
     *  referenced in the info table of this adapter. The kernel will retrieve
     *  this information to determine the total number of referenced types in
     *  the model.
     *  @param constructorString The string within the $new() macro.
     *  @return The translated new constructor invocation string.
     *  @exception IllegalActionException The given constructor string is
     *   not well-formed.
     */
    public String getNewInvocation(String constructorString)
            throws IllegalActionException {
        constructorString = processCode(constructorString);

        // i.e. "$new(Array(8, 8, arg1, arg2, ...))"
        // this transforms to ==>
        // "Array_new(8, arg1, arg2, ...)"
        int openFuncParenIndex = constructorString.indexOf('(');
        int closeFuncParenIndex = constructorString.lastIndexOf(')');

        // Syntax checking.
        if ((openFuncParenIndex == -1)
                || (closeFuncParenIndex != (constructorString.length() - 1))) {
            CGException
                    .throwException("Bad Syntax with the $new() macro. "
                            + "[i.e. -- $new([elementType]Array(8, 8, arg1, arg2, ...))]");
        }

        String typeName = constructorString.substring(0, openFuncParenIndex)
                .trim();

        // Record the referenced type function in the infoTable.
        _codeGenerator._newTypesUsed.add(typeName);

        // new() should not be a polymorphic function.
        //_codeGenerator._typeFuncUsed.add("new");

        // Transform this to a function call that needs
        // implementation code.
        return processCode("$" + typeName + "_new"
                + constructorString.substring(openFuncParenIndex));
    }

    /**
     * Get the port that has the given name.
     * @param refName The given name.
     * @return The port that has the given name.
     */
    final public TypedIOPort getPort(String refName) {
        Actor actor = (Actor) _component;
        Iterator<?> inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            // The channel is specified as $ref(port#channelNumber).
            if (inputPort.getName().equals(refName)) {
                return inputPort;
            }
        }

        Iterator<?> outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // The channel is specified as $ref(port#channelNumber).
            if (outputPort.getName().equals(refName)) {
                return outputPort;
            }
        }

        return null;
    }

    /** Get the size of a parameter. The size of a parameter
     *  is the length of its array if the parameter's type is array,
     *  and 1 otherwise.
     *  @param name The name of the parameter.
     *  @return The expression that represents the size of a parameter or port.
     *  @exception IllegalActionException If no port or parameter of
     *   the given name is found.
     */
    public String getSize(String name) throws IllegalActionException {

        // Try if the name is a parameter.
        Variable attribute = ModelScope.getScopedVariable(null,
                (NamedObj) _component, name);

        if (attribute != null) {
            Token token = attribute.getToken();

            if (token instanceof ArrayToken) {
                return String.valueOf(((ArrayToken) token).length());
            }

            return "1";
        } else {
            TypedIOPort port = getPort(name);
            if (port != null) {
                if (port.isMultiport()) {
                    return String.valueOf(port.getWidth());
                } else {
                    Type type = port.getType();
                    if (type instanceof ArrayType) {
                        if (((ArrayType) type).hasKnownLength()) {
                            return String.valueOf(((ArrayType) type).length());
                        } else {
                            // TODO: This is a workaround and should be removed.
                            // Probably we should remove the $size feature, or at least,
                            // not support it on ports, but instead on tokens.
                            return ((NamedProgramCodeGeneratorAdapter) _codeGenerator
                                    .getAdapter(_component)).getReference(name, true)
                                    + ".payload."
                                    + _getCodeGenerator().codeGenType(type)
                                    + "->size";
                        }
                    }
                }
            }
        }

        CGException.throwException(_component, "Attribute not found: " + name);
        return ""; //We never get here.
    }

    /** Set the associated code generator.
     *  @param codeGenerator The code generator associated with this class.
     *  @see #_getCodeGenerator()
     */
    final public void setCodeGenerator(ProgramCodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(ProgramCodeGenerator)
     */
    protected ProgramCodeGenerator _getCodeGenerator() {
        return _codeGenerator;
    }

    /** Return the replacement string of the given macro. Subclass
     * of GenericCodeGenerator may overriding this method to extend or support
     * a different set of macros.
     * @param macro The given macro.
     * @param parameter The given parameter to the macro.
     * @return The replacement string of the given macro.
     * @exception IllegalActionException Thrown if the given macro or
     *  parameter is not valid.
     */
    protected String _replaceMacro(String macro, String parameter)
            throws IllegalActionException {
        if (_codeGenerator == null) {
            throw new NullPointerException("_codeGenerator was null,  be sure that "
                    + "the adapter calls templateParser.setCodeGenerator(GenericCodeGenerator)");
        }
        // $$def(abc)
        // ==> abc$def(abc)
        int indexOfDollarSign = macro.indexOf('$');
        if (indexOfDollarSign >= 0) {
            String result = "$" + macro.substring(0, indexOfDollarSign);
            String innerMacro = macro.substring(indexOfDollarSign + 1, macro
                    .length());
            result += _replaceMacro(innerMacro, parameter);
            return result;
        }
        if (macro.equals("get")) {
            return _replaceGetMacro(parameter);
        } else if (macro.equals("put")) {
            return _replacePutMacro(parameter);
        } else if (macro.equals("hasToken")) {
            return _replaceHasTokenMacro(parameter);
        } else if (macro.equals("ref")) {
            CGException
                    .throwException("$ref is no longer supported as a macro. To reference "
                            + "input/output ports, use $get() and $put(). For parameters, use $param()");
        } else if (macro.equals("param")) {
            return _replaceParameter(parameter);
        } else if (macro.equals("targetType")) {

            TypedIOPort port = getPort(parameter);
            if (port != null) {
                return _getCodeGenerator().targetType(port.getType());
            }

            Variable variable = _getVariable(parameter);
            if (variable != null) {
                return _getCodeGenerator().targetType(variable.getType());
            }
            CGException.throwException(parameter
                    + " is not a port. $type macro takes in a port.");

        } else if (macro.equals("type") || macro.equals("cgType")) {

            String type = "";
            if (macro.equals("type")) {
                type = "TYPE_";
            }

            TypedIOPort port = getPort(parameter);
            if (port != null) {
                return type + _getCodeGenerator().codeGenType(port.getType());
            }

            Variable variable = _getVariable(parameter);
            if (variable != null) {
                return type
                        + _getCodeGenerator().codeGenType(variable.getType());
            }
            CGException.throwException(parameter
                    + " is not a port. $type macro takes in a port.");

        } else if (macro.equals("val")) {
            return getParameterValue(parameter, (NamedObj) _component);

        } else if (macro.equals("size")) {
            return "" + getSize(parameter);

        } else if (macro.equals("actorSymbol")) {
            if (parameter.trim().length() == 0) {
                return _codeGenerator
                        .generateVariableName(((NamedObj) _component));
            } else {
                return _codeGenerator
                        .generateVariableName(((NamedObj) _component))
                        + "_" + processCode(parameter);
            }
        } else if (macro.equals("actorClass")) {
            return ((NamedObj) _component).getClassName().replace('.', '_')
                    + "_" + processCode(parameter);

            // Handle type function macros.
        } else if (macro.equals("new")) {
            return getNewInvocation(parameter);

        } else if (macro.equals("tokenFunc")) {
            return getFunctionInvocation(parameter, false);

        } else if (macro.equals("typeFunc")) {
            return getFunctionInvocation(parameter, true);

        } else {
            // Try calling a method defined in the adapter first.
            try {
                Method method = getClass().getMethod(macro, new Class[0]);
                return (String) method.invoke(this, new Object[0]);
            } catch (Exception ex) {
                // Don't print out error, since this is probably not an user macro.
            }

            // Try to treat this as an user macro class.
            Method handler = null;
            Method checker = null;
            Class<?> userMacro = null;
            try {
                userMacro = Class.forName("ptolemy.codegen.kernel.userMacro."
                        + macro);

                handler = userMacro.getMethod("handleMacro",
                        new Class[] { List.class });
                checker = userMacro.getMethod("checkArguments",
                        new Class[] { List.class });
            } catch (Exception ex) {
                // Don't print out error, since this is probably not an user macro.
                return null;
            }

            try {
                checker
                        .invoke(userMacro,
                                new Object[] { parseList(parameter) });
                return (String) handler.invoke(userMacro,
                        new Object[] { parseList(parameter) });
            } catch (Exception ex) {
                CGException.throwException(_component, ex,
                        "Failed to invoke user macro ($" + macro + ").");
            }
        }
        return ""; // We never get here.
    }

    /** Find the paired close parenthesis given a string and an index
     *  which is the position of an open parenthesis. Return -1 if no
     *  paired close parenthesis is found.
     *  @param string The given string.
     *  @param pos The given index.
     *  @return The index which indicates the position of the paired
     *   close parenthesis of the string.
     *  @exception IllegalActionException If the character at the
     *   given position of the string is not an open parenthesis or
     *   if the index is less than 0 or past the end of the string.
     */
    protected static int _findClosedParen(String string, int pos)
            throws IllegalActionException {
        if (pos < 0 || pos >= string.length()) {
            CGException.throwException("The character index " + pos
                    + " is past the end of string \"" + string
                    + "\", which has a length of " + string.length() + ".");
        }

        if (string.charAt(pos) != '(') {
            CGException.throwException("The character at index " + pos
                    + " of string: " + string + " is not a open parenthesis.");
        }

        int nextOpenParen = string.indexOf("(", pos + 1);

        if (nextOpenParen < 0) {
            nextOpenParen = string.length();
        }

        int nextCloseParen = string.indexOf(")", pos);

        if (nextCloseParen < 0) {
            return -1;
        }

        int count = 1;
        int beginIndex = pos + 1;

        while (beginIndex > 0) {
            if (nextCloseParen < nextOpenParen) {
                count--;

                if (count == 0) {
                    return nextCloseParen;
                }

                beginIndex = nextCloseParen + 1;
                nextCloseParen = string.indexOf(")", beginIndex);

                if (nextCloseParen < 0) {
                    return -1;
                }
            }

            if (nextOpenParen < nextCloseParen) {
                count++;
                beginIndex = nextOpenParen + 1;
                nextOpenParen = string.indexOf("(", beginIndex);

                if (nextOpenParen < 0) {
                    nextOpenParen = string.length();
                }
            }
        }

        return -1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         packaged methods                   ////

    /** Return the actual CodeStream associated with the given Actor and
     *  GenericCodeGenerator.  Generally, this will come from the Actor's template
     *  file, but EmbeddedCActors get their code from the embeddedCCode parameter.
     * @param component The actor whose code to return.
     * @param codeGenerator The actor's GenericCodeGenerator.
     * @return The actor's actual CodeStream.
     * @exception IllegalActionException If thrown when getting the actor's adapter.
     */
    static CodeStream _getActualCodeStream(Object component,
            GenericCodeGenerator codeGenerator) throws IllegalActionException {

        ProgramCodeGeneratorAdapter adapter = null;
        CodeStream codeStream = null;
        if (component != null
                && component instanceof ptolemy.actor.lib.jni.EmbeddedCActor) {
            adapter = (ProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(codeGenerator.getContainer());
            codeStream = new CodeStream(adapter);
            // We have an EmbeddedCActor, read the codeBlocks from
            // the embeddedCCode parameter.
            codeStream
                    .setCodeBlocks(((ptolemy.actor.lib.jni.EmbeddedCActor) component).embeddedCCode
                            .getExpression());
        } else {
            adapter = (ProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(component);
            codeStream = new CodeStream(adapter);
        }

        return codeStream;
    }

    /** Return the actual CodeStream for this Adapter.
     * @return The actual CodeStream.
     * @exception IllegalActionException If thrown by a called method.
     */
    CodeStream _getActualCodeStream() throws IllegalActionException {
        return _getActualCodeStream(_component, _codeGenerator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Return the position of the first occurence of the "&" sign in
     * the given code string, starting from the given from position.
     * If the "&" sign found is escaped by "\\", it will be ignored.
     * @param code The given code string.
     * @param from The given position to start searching from.
     * @return The next position of the "&" sign.
     */
    static private int _getMacroStartIndex(String code, int from) {
        int position = from - 1;

        do {
            position = code.indexOf("$", position + 1);

        } while (position > 0 && code.charAt(position - 1) == '\\');

        return position;
    }

    /***
     * Return a variable that matches the given label.
     * Null is returned, if no such variable cannot found.
     * @param refName The given label.
     * @return A variable that matches the given label, or
     *  null if no such variable is found.
     */
    private Variable _getVariable(String refName) {
        NamedObj actor = (NamedObj) _component;

        for (Object attribute : actor.attributeList()) {

            if (attribute instanceof Variable) {

                if (((Variable) attribute).getName().equals(refName)) {
                    return (Variable) attribute;
                }
            }
        }
        return null;
    }

    /** Checks if the string is just a String of port. If true, then
     *  return a pair of strings, where the second index is a string
     *  "0", indicating the channel is 0. If not, check if the string
     *  if of the form port#channel. If not,
     *  throws an exception, if it is, put the port and channel into
     *  two different indices of an array of strings, and return that
     *  array.
     *  @param name
     *  @return port and channel
     *  @exception IllegalActionException
     */
    private String[] _parsePortChannel(String name)
            throws IllegalActionException {

        String[] result = { "", "" };

        // Given expression of forms:
        //     "port", or
        //     "port#channel".

        int commaIndex = TemplateParser.indexOf(",", name, 0);

        if (commaIndex >= 0) {
            throw new IllegalActionException("Parsing the string:" + name
                    + ". However we are expecting a string"
                    + "of the form: port, or port#channel.");
        }

        int poundIndex = indexOf("#", name, 0);
        if (poundIndex < 0) {
            result[0] = name;
            result[1] = "0";
        } else {
            result[0] = name.substring(0, poundIndex);
            result[1] = name.substring(poundIndex + 1);
        }
        return result;
    }

    private String _replaceGetMacro(String parameter)
            throws IllegalActionException {
        // e.g. $get(input#channel, offset); or
        // $get(input, offset); or,
        // $get(input#channel); or,
        // $get(input);
        List<String> parameters = parseList(parameter);
        String offset;
        if (parameters.size() == 1) {
            offset = "0";
        } else {
            offset = parameters.get(1);
        }

        String[] portChannel = _parsePortChannel(parameters.get(0));

        TypedIOPort port = getPort(portChannel[0]);
        String channel = portChannel[1];

        if (port == null || channel.length() == 0) {
            CGException.throwException(parameter
                    + " is not acceptable by $get(). "
                    + "The $get macro can accept the following forms: "
                    + "$get(input#channel, offset); or, "
                    + "$get(input, offset); or, " + "$get(input#channel); or, "
                    + "$get(input);");
        }

        PortCodeGenerator portAdapter = (PortCodeGenerator) _codeGenerator
                .getAdapter(port);

        return processCode(portAdapter.generateGetCode(channel, offset));
    }

    /** replace the $hasToken() with the corresponding parameter 
     *  @param parameter The name and offset of the parameter
     *  @return the hasToken parameter is returned
     *  @exception IllegalActionException
     */
    private String _replaceHasTokenMacro(String parameter)
            throws IllegalActionException {
        // e.g. $hasToken(input#channel, offset); or
        // $hasToken(input, offset); or,
        // $hasToken(input#channel); or,
        // $hasToken(input);
        List<String> parameters = parseList(parameter);
        String offset;
        if (parameters.size() == 1) {
            offset = "0";
        } else {
            offset = parameters.get(1);
        }

        String[] portChannel = _parsePortChannel(parameters.get(0));

        TypedIOPort port = getPort(portChannel[0]);
        String channel = portChannel[1];

        if (port == null || channel.length() == 0) {
            CGException.throwException(parameter
                    + " is not acceptable by $hasToken(). "
                    + "The $hasToken macro can accept the following forms: "
                    + "$hasToken(input#channel, offset); or, "
                    + "$hasToken(input, offset); or, "
                    + "$hasToken(input#channel); or, " + "$hasToken(input);");
        }

        PortCodeGenerator portAdapter = (PortCodeGenerator) _codeGenerator
                .getAdapter(port);

        return processCode(portAdapter.generateHasTokenCode(channel, offset));
    }

    /** replace the $param() with the corresponding parameter
     *  @param parameter The name and offset of the parameter.
     *  @return the parameter to be returned.
     *  @exception IllegalActionException
     */
    private String _replaceParameter(String parameter)
            throws IllegalActionException {
        // e.g. $param(thisParam, 0), or $param(thisParam).
        // First is the name of the parameter, second is the offset of the parameter.
        if (!(_component instanceof Actor)) {
            CGException.throwException(_component,
                    "Parameters are only supported for"
                            + "actors, but this component is not one.");
        }
        Director directorAdapter = (Director) _codeGenerator
                .getAdapter(((Actor) _component).getDirector());
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) _codeGenerator
                .getAdapter(_component);
        List<String> parameters = parseList(parameter);
        String paramName = parameters.get(0);
        String[] offset = new String[] { "", "" };
        if (parameters.size() == 2) {
            offset[1] = parameters.get(1);
        } else if (parameters.size() != 1) {
            CGException.throwException(_component,
                    "$param() can be used as follows:"
                            + "$param(name), or, $param(name, offset)");
        }
        Attribute attribute = adapter.getComponent().getAttribute(paramName);
        if (attribute == null) {
            throw new NullPointerException("Could not find attribute \""
                    + paramName + "\", perhaps it is a port, try using $get("
                    + paramName + ") instead of $param(" + paramName + ")");
        }
        return directorAdapter.getParameter(adapter, attribute, offset);
    }

    private String _replacePutMacro(String parameter)
            throws IllegalActionException {
        // e.g. $put(input#channel, token); or 
        // $put(input, token); or
        // $put(input#channel, offset, token)
        // $put(input, offset, token)
        List<String> parameters = parseList(parameter);

        String offset = null;
        String dataToken = null;

        if (parameters.size() == 2) {
            offset = "0";
            dataToken = parameters.get(1);
        } else if (parameters.size() == 3) {
            offset = parameters.get(1);
            dataToken = parameters.get(2);
        } else {
            CGException
                .throwException("\"" + parameter
                            + "\" is not acceptable by $put(). "
                                    + "$put could be used in the following ways: "
                                    + "$put(output#channel, token); or, $put(output, token); or,"
                                    + "$put(input#channel, offset, token); or, $put(input, offset, token)");
        }

        TypedIOPort port = null;
        String channel = "";

        String[] portAndChannel = _parsePortChannel(parameters.get(0));

        port = getPort(portAndChannel[0]);
        channel = portAndChannel[1];

        if (port == null) {
            CGException
                    .throwException("parameter is not acceptable by $put(). "
                            + "$put could be used in the following ways: "
                            + "$put(output#channel, token); or, $put(output, token); or,"
                            + "$put(input#channel, offset, token); or, $put(input, offset, token)");
        }

        PortCodeGenerator portAdapter = (PortCodeGenerator) _codeGenerator
                .getAdapter(port);

        return processCode(portAdapter.generatePutCode(channel, offset,
                dataToken));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private ProgramCodeGenerator _codeGenerator;

    /**
     * The code stream associated with this adapter.
     */
    private CodeStream _codeStream = null;

    private Object _component;

    /** The parse tree to use with expressions. */
    protected ParseTreeCodeGenerator _parseTreeCodeGenerator;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class implements a scope, which is used to generate the
     *  parsed expressions in target language.
     */
    private class VariableScope extends ModelScope {

        /** Construct a scope consisting of the variables of the container
         *  of the given instance of Variable and its containers and their
         *  scope-extending attributes.
         *  @param variable The variable whose expression is under code
         *   generation using this scope.
         */
        public VariableScope(Variable variable) {
            _variable = variable;
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

            NamedObj container = (NamedObj) _component;
            if (_variable != null) {
                container = _variable.getContainer();
            }

            Variable result = getScopedVariable(_variable, container, name);

            if (result != null) {
                // If the variable found is a modified variable, which means
                // its value can be directly changed during execution
                // (e.g., in commit action of a modal controller), then this
                // variable is declared in the target language and should be
                // referenced by the name anywhere it is used.
                if (_codeGenerator._modifiedVariables.contains(result)) {
                    return new ObjectToken(_codeGenerator
                            .generateVariableName(result));
                } else {
                    // This will lead to recursive call until a variable found
                    // is either directly specified by a constant or it is a
                    // modified variable.
                    PtParser parser = new PtParser();
                    String parameterValue = getParameterValue(name, result
                            .getContainer());
                    try {
                        ASTPtRootNode parseTree = parser
                                .generateParseTree(parameterValue);

                        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();

                        return evaluator.evaluateParseTree(parseTree, this);
                    } catch (IllegalActionException ex) {
                        // Could not evaluate the expression. This means that
                        // the parameter value contains a variable expression.
                        // So, we'll won't try to evaluate it.
                        return new ObjectToken(parameterValue);
                    }
                }
            } else {
                return null;
            }
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @param name The name of the attribute to look up.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            if (_variable != null) {
                return _variable.getParserScope().getType(name);
            }
            return null;
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @param name The name of the type term to look up.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            if (_variable != null) {
                return _variable.getParserScope().getTypeTerm(name);
            }
            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of variable names within the scope.
         *  @exception IllegalActionException If there is a problem
         *  getting the identifier set from the variable.
         */
        public Set identifierSet() throws IllegalActionException {
            if (_variable != null) {
                return _variable.getParserScope().identifierSet();
            }
            return null;
        }

        public String toString() {
            return super.toString()
                    + " variable: "
                    + _variable
                    + " variable.parserScope: "
                    + (_variable == null ? "N/A, _variable is null" : _variable
                            .getParserScope());
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /** If _variable is not null, then the adapter scope created is
         *  for parsing the expression specified for this variable and
         *  generating the corresponding code in target language.
         */
        private Variable _variable = null;
    }

}
