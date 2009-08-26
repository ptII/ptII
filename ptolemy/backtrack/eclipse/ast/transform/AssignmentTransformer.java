/* Assignment transformer.

 Copyright (c) 2005-2007 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.ast.transform;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.eclipse.ast.ASTClassNotFoundException;
import ptolemy.backtrack.eclipse.ast.Type;
import ptolemy.backtrack.eclipse.ast.TypeAnalyzer;
import ptolemy.backtrack.eclipse.ast.TypeAnalyzerState;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;

//////////////////////////////////////////////////////////////////////////
//// AssignmentTransformer

/**
 The assignment transformer to transform Java source programs into new
 programs that support backtracking.
 <p>
 In this approach, each assignment to a <em>state variable</em> is
 refactored to become a method call, which, before actually assigning
 the new value, back up the old value of the field in a record (see
 {@link FieldRecord}).
 <p>
 This transformer implements several handlers. It does the refactoring as the
 type analyzer ({@link TypeAnalyzer}) analyzes the original source code. The
 analyzer calls back methods defined in the handlers after assigning
 appropriate types to AST nodes. Those callback methods (implemented in this
 class) refactors the given nodes on-the-fly as the type analysis goes on.
 <p>
 This class must be used at the same time as {@link ConstructorTransformer}
 to get the correct refactoring result.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AssignmentTransformer extends AbstractTransformer implements
        AliasHandler, AssignmentHandler, ClassHandler, CrossAnalysisHandler,
        FieldDeclarationHandler, MethodDeclarationHandler {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Enter an anonymous class declaration. Nothing is done in this method.
     *
     *  @param node The AST node of the anonymous class declaration.
     *  @param state The current state of the type analyzer.
     */
    public void enter(AnonymousClassDeclaration node, TypeAnalyzerState state) {
    }

    /** Enter a field declaration. If the field is static, a flag is set so
     *  that handling of other nodes (such as assignment) may be different from
     *  those in non-static fields.
     *
     *  @param node The AST node of the field declaration.
     *  @param state The current state of the type analyzer.
     */
    public void enter(FieldDeclaration node, TypeAnalyzerState state) {
        if (Modifier.isStatic(node.getModifiers())) {
            _isInStatic.push(Boolean.TRUE);
        } else {
            _isInStatic.push(Boolean.FALSE);
        }
    }

    /** Enter a method declaration. If the method is static, a flag is set so
     *  that handling of other nodes (such as assignment) may be different from
     *  those in non-static methods.
     *
     *  @param node The AST node of the method declaration.
     *  @param state The current state of the type analyzer.
     */
    public void enter(MethodDeclaration node, TypeAnalyzerState state) {
        if (Modifier.isStatic(node.getModifiers())) {
            _isInStatic.push(Boolean.TRUE);
        } else {
            _isInStatic.push(Boolean.FALSE);
        }
    }

    /** Enter an class declaration. Nothing is done in this method.
     *
     *  @param node The AST node of the anonymous class declaration.
     *  @param state The current state of the type analyzer.
     */
    public void enter(TypeDeclaration node, TypeAnalyzerState state) {
    }

    /** Exit an anonymous class declaration, and add extra methods and fields
     *  to it.
     *  <p>
     *  This function is called after all the existing methods and fields in
     *  the same class has been visited, and all the field assignments in it
     *  are handled with {@link #handle(Assignment, TypeAnalyzerState)}.
     *
     *  @param node The AST node of the anonymous class declaration.
     *  @param state The current state of the type analyzer.
     */
    public void exit(AnonymousClassDeclaration node, TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
    }

    /** Exit a field declaration. The flag of static field is set back to its
     *  old value before the field was entered.
     *
     *  @param node The AST node of the field declaration.
     *  @param state The current state of the type analyzer.
     */
    public void exit(FieldDeclaration node, TypeAnalyzerState state) {
        _isInStatic.pop();
    }

    /** Exit a method declaration. The flag of static method is set back to its
     *  old value before the method was entered.
     *
     *  @param node The AST node of the method declaration.
     *  @param state The current state of the type analyzer.
     */
    public void exit(MethodDeclaration node, TypeAnalyzerState state) {
        _isInStatic.pop();
    }

    /** Exit a class declaration, and add extra methods and fields to it.
     *  <p>
     *  This function is called after all the existing methods and fields in
     *  the same class has been visited, and all the field assignments in it
     *  are handled with {@link #handle(Assignment, TypeAnalyzerState)}.
     *
     *  @param node The AST node of the anonymous class declaration.
     *  @param state The current state of the type analyzer.
     */
    public void exit(TypeDeclaration node, TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
    }

    /** Handle an assignment, and refactor it to be a special method call if the
     *  left-hand side of the assignment is a state variable. The accessed field
     *  is also recorded in a list so that extra private methods used to back up
     *  fields are only created for those used fields. If the field on the
     *  left-hand side is a multi-dimensional array, access with different
     *  numbers of indices is recorded with multiple entries. For each of those
     *  entries, an extra method is created.
     *  <p>
     *  An assignment may also be an alias point, in which case an array field
     *  is aliased with a local variable. For example: Suppose <tt>field</tt>
     *  is an array field recognized as a state variable. Expression <tt>buffer
     *  = field</tt> aliases the field with a local variable <tt>buffer</tt>.
     *  After that, the method may modify the <tt>field</tt> array (with the
     *  name <tt>buffer</tt>) without letting <tt>field</tt>'s owner class
     *  know. So, this is an alias point. This function refactors this alias
     *  point and replaces <tt>field</tt> with a function call.
     *
     *  @param node The assignment to be refactored.
     *  @param state The current state of the type analyzer.
     */
    public void handle(Assignment node, TypeAnalyzerState state) {
        Expression newExpression = _handleAlias(node.getRightHandSide(), state);

        if (newExpression == null) {
            _handleAssignment(node, state);
        }
    }

    /** Handle a class instance creation. If there are arguments that are
     *  aliased array fields, complete backup arrays are created for them and
     *  stored in memory.
     *  <p>
     *  For example: Suppose <tt>field</tt> is an array field recognized as a
     *  state variable. Expression <tt>new MyClass(field)</tt> instantiates an
     *  object of class <tt>MyClass</tt> with argument <tt>field</tt>. The
     *  constructor of <tt>MyClass</tt> may modify the <tt>field</tt> array
     *  without letting <tt>field</tt>'s owner class know. So, this is an alias
     *  point. This function refactors this alias point and replaces
     *  <tt>field</tt> with a function call.
     *
     *  @param node The class instance creation to be refactored.
     *  @param state The current state of the type analyzer.
     */
    public void handle(ClassInstanceCreation node, TypeAnalyzerState state) {
        Iterator arguments = node.arguments().iterator();

        while (arguments.hasNext()) {
            Expression argument = (Expression) arguments.next();
            _handleAlias(argument, state);
        }
    }

    /** Handle a method invocation. If there are arguments that are aliased
     *  array fields, complete backup arrays are created for them and stored in
     *  memory.
     *  <p>
     *  For example: Suppose <tt>field</tt> is an array field recognized as a
     *  state variable. Expression <tt>func(field)</tt> invokes method
     *  <tt>func</tt> an with argument <tt>field</tt>. <tt>func</tt> may modify
     *  the <tt>field</tt> array in its body without letting <tt>field</tt>'s
     *  owner class know. So, this is an alias point. This function refactors
     *  this alias point and replaces <tt>field</tt> with a function call.
     *
     *  @param node The method invocation to be refactored.
     *  @param state The current state of the type analyzer.
     */
    public void handle(MethodInvocation node, TypeAnalyzerState state) {
        Iterator arguments = node.arguments().iterator();

        while (arguments.hasNext()) {
            Expression argument = (Expression) arguments.next();
            _handleAlias(argument, state);
        }
    }

    /** Handle a postfix expression, and refactor it to be a special method
     *  call if its subexpression evaluates to be a state variable. The
     *  accessed field is also recorded in a list so that extra private
     *  methods used to back up fields are only created for those used fields.
     *  If the field in the subexpression is a multi-dimensional array, access
     *  with different numbers of indices is recorded with multiple entries.
     *  For each of those entries, an extra method is created.
     *
     *  @param node The postfix expression to be refactored.
     *  @param state The current state of the type analyzer.
     */
    public void handle(PostfixExpression node, TypeAnalyzerState state) {
        _handleAssignment(node, state);
    }

    /** Handle a prefix expression, and refactor it to be a special method
     *  call if its subexpression evaluates to be a state variable. The
     *  accessed field is also recorded in a list so that extra private
     *  methods used to back up fields are only created for those used fields.
     *  If the field in the subexpression is a multi-dimensional array, access
     *  with different numbers of indices is recorded with multiple entries.
     *  For each of those entries, an extra method is created.
     *
     *  @param node The prefix expression to be refactored.
     *  @param state The current state of the type analyzer.
     */
    public void handle(PrefixExpression node, TypeAnalyzerState state) {
        _handleAssignment(node, state);
    }

    /** Handle a return statement. If the expression to be returned is an
     *  aliased array field, a complete backup array is created for it and
     *  stored in memory.
     *  <p>
     *  For example: Suppose <tt>field</tt> is an array field recognized as a
     *  state variable. Statement <tt>return field;</tt> returns the field to
     *  the calling method. The calling method may modify the <tt>field</tt>
     *  array in its body without letting <tt>field</tt>'s owner class know.
     *  So, this is an alias point. This function refactors this alias point
     *  and replaces <tt>field</tt> with a function call.
     *
     *  @param node The return statement to be refactored.
     *  @param state The current state of the type analyzer.
     */
    public void handle(ReturnStatement node, TypeAnalyzerState state) {
        if (node.getExpression() != null) {
            _handleAlias(node.getExpression(), state);
        }
    }

    /** Fix the refactoring result when the set of cross-analyzed types
     *  changes.
     *  <p>
     *  The set of cross-analyzed types defines the growing set of types that
     *  are analyzed in a run. Special care is taken for cross-analyzed types
     *  because they are monitored by checkpoint objects, and
     *  checkpoint-related extra methods are added to them. Unfortunately, it
     *  is not possible to know all the cross-analyzed types at the beginning.
     *  It is then necessary to fix the refactoring result when more
     *  cross-analyzed types are discovered later.
     *
     *  @param state The current state of the type analyzer.
     */
    public void handle(TypeAnalyzerState state) {
        Set crossAnalyzedTypes = state.getCrossAnalyzedTypes();
        Iterator crossAnalysisIter = crossAnalyzedTypes.iterator();

        while (crossAnalysisIter.hasNext()) {
            String nextClassName = (String) crossAnalysisIter.next();

            List<Block> blockList = _fixSetCheckpoint.get(nextClassName);

            if (blockList != null) {
                Iterator nodesIter = blockList.iterator();

                while (nodesIter.hasNext()) {
                    Block body = (Block) nodesIter.next();
                    AST ast = body.getAST();
                    Statement invocation = _createSetCheckpointInvocation(ast);
                    List<Statement> statements = body.statements();
                    statements.add(statements.size() - 2, invocation);
                    nodesIter.remove();
                }
            }

            List<RehandleDeclarationRecord> recordList = _rehandleDeclaration
                    .get(nextClassName);

            if (recordList != null) {
                Iterator<RehandleDeclarationRecord> recordsIter = recordList
                        .iterator();

                while (recordsIter.hasNext()) {
                    RehandleDeclarationRecord record = recordsIter.next();
                    Iterator<ASTNode> extendedIter = record
                            ._getExtendedDeclarations().iterator();

                    while (extendedIter.hasNext()) {
                        ASTNode declaration = extendedIter.next();

                        if (declaration != null) {
                            removeNode(declaration);
                        }
                    }

                    Iterator<ASTNode> fixedIter = record
                            ._getFixedDeclarations().iterator();

                    while (fixedIter.hasNext()) {
                        ASTNode declaration = fixedIter.next();

                        if (declaration != null) {
                            record._getBodyDeclarations().add(declaration);
                        }
                    }

                    recordsIter.remove();
                }
            }

            List<NodeReplace> list = _nodeSubstitution.get(nextClassName);

            if (list != null) {
                Iterator<NodeReplace> replaceIter = list.iterator();

                while (replaceIter.hasNext()) {
                    NodeReplace nodeReplace = replaceIter.next();

                    if (nodeReplace._getToNode() == null) {
                        removeNode(nodeReplace._getFromNode());
                    } else {
                        replaceNode(nodeReplace._getFromNode(), nodeReplace
                                ._getToNode());
                    }

                    replaceIter.remove();
                }
            }
        }
    }

    /** Handle a variable declaration fragment. If the frament has an
     *  initializer and the initializer evaluates to an aliased array field,
     *  a complete backup array is created for it and stored in memory.
     *  <p>
     *  For example: Suppose <tt>field</tt> is an array field recognized as a
     *  state variable. Variable declaration <tt>int[] buffer = field</tt>
     *  (with one fragment only) aliases the field to with name
     *  <tt>buffer</tt>. The declaring method may modify the <tt>field</tt>
     *  array (with name <tt>buffer</tt>) in its body without letting
     *  <tt>field</tt>'s owner class know. So, this is an alias point. This
     *  function refactors this alias point and replaces <tt>field</tt> with a
     *  function call.
     *
     *  @param node The variable declaration fragment to be refactored.
     *  @param state The current state of the type analyzer.
     */
    public void handle(VariableDeclarationFragment node, TypeAnalyzerState state) {
        if (node.getInitializer() != null) {
            _handleAlias(node.getInitializer(), state);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public fields                       ////

    // Findbugs asks that we make these final.

    /** The name of commit methods.
     */
    public static final String COMMIT_NAME = "$COMMIT";

    /** Whether to refactor private static fields.
     */
    public static final boolean HANDLE_STATIC_FIELDS = false;

    /** Whether to optimize method calls. (Not implemented yet.)
     */
    public static final boolean OPTIMIZE_CALL = true;

    /** The name of the proxy class created in each anonymous class.
     */
    public static final String PROXY_NAME = "_PROXY_";

    /** The name of the record array.
     */
    public static final String RECORDS_NAME = "$RECORDS";

    /** The prefix of records (new fields to be added to a class).
     */
    public static final String RECORD_PREFIX = "$RECORD$";

    /** The name of restore methods.
     */
    public static final String RESTORE_NAME = "$RESTORE";

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    /** Create assignment methods for each accessed field that has been
     *  recorded. If a field is a multi-dimensional array, access with
     *  different numbers of indices is recorded with multiple entries.
     *  For each of those entries, an extra method is created for each
     *  different number of indices.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @param fieldName The name of the field to be handled.
     *  @param fieldType The type of the field to be handled.
     *  @param indices The number of indices.
     *  @param special Whether to handle special assign operators.
     *  @param isStatic Whether the field is static.
     *  @return The declaration of the method that handles assignment to
     *   the field.
     */
    private MethodDeclaration _createAssignMethod(AST ast,
            CompilationUnit root, TypeAnalyzerState state, String fieldName,
            Type fieldType, int indices, boolean special, boolean isStatic) {
        Class currentClass = state.getCurrentClass();
        ClassLoader loader = state.getClassLoader();
        String methodName = _getAssignMethodName(fieldName, special);

        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (_isMethodDuplicated(currentClass, methodName, fieldType, indices,
                isStatic, loader, true)) {
            throw new ASTDuplicatedMethodException(currentClass.getName(),
                    methodName);
        }

        MethodDeclaration method = ast.newMethodDeclaration();

        // Get the type of the new value. The return value has the same
        // type.
        for (int i = 0; i < indices; i++) {
            try {
                fieldType = fieldType.removeOneDimension();
            } catch (ClassNotFoundException e) {
                throw new ASTClassNotFoundException(fieldType);
            }
        }

        String fieldTypeName = fieldType.getName();

        // Set the name and return type.
        SimpleName name = ast.newSimpleName(methodName);
        org.eclipse.jdt.core.dom.Type type;
        method.setName(name);

        String typeName = getClassName(fieldTypeName, state, root);

        if (special && _assignOperators.containsKey(fieldTypeName)) {
            PrimitiveType.Code code = _rightHandTypes.get(fieldTypeName);
            type = ast.newPrimitiveType(code);
        } else {
            type = createType(ast, typeName);
        }

        method.setReturnType2(createType(ast, typeName));

        // If the field is static, add a checkpoint object argument.
        if (isStatic) {
            // Add a "$CHECKPOINT" argument.
            SingleVariableDeclaration checkpoint = ast
                    .newSingleVariableDeclaration();
            String checkpointType = getClassName(Checkpoint.class, state, root);
            checkpoint.setType(ast
                    .newSimpleType(createName(ast, checkpointType)));
            checkpoint.setName(ast.newSimpleName(CHECKPOINT_NAME));
            method.parameters().add(checkpoint);
        }

        if (special && _assignOperators.containsKey(fieldTypeName)) {
            // Add an operator parameter.
            SingleVariableDeclaration operator = ast
                    .newSingleVariableDeclaration();
            operator.setType(ast.newPrimitiveType(PrimitiveType.INT));
            operator.setName(ast.newSimpleName("operator"));
            method.parameters().add(operator);
        }

        // Add all the indices.
        for (int i = 0; i < indices; i++) {
            SingleVariableDeclaration index = ast
                    .newSingleVariableDeclaration();
            index.setType(ast.newPrimitiveType(PrimitiveType.INT));
            index.setName(ast.newSimpleName("index" + i));
            method.parameters().add(index);
        }

        // Add a new value argument with name "newValue".
        SingleVariableDeclaration argument = ast.newSingleVariableDeclaration();
        argument.setType((org.eclipse.jdt.core.dom.Type) ASTNode.copySubtree(
                ast, type));
        argument.setName(ast.newSimpleName("newValue"));
        method.parameters().add(argument);

        // If the field is static, the method is also static; the method
        // is also private.
        List modifiers = method.modifiers();
        modifiers
                .add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        modifiers.add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        if (isStatic) {
            modifiers.add(ast
                    .newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        }

        // Create the method body.
        Block body = _createAssignmentBlock(ast, state, fieldName, fieldType,
                indices, special);
        method.setBody(body);

        return method;
    }

    /** Create the body of an assignment method, which backs up the field
     *  before a new value is assigned to it.
     *
     *  @param ast The {@link AST} object.
     *  @param state The current state of the type analyzer.
     *  @param fieldName The name of the field.
     *  @param fieldType The type of the left-hand side (with <tt>indices</tt>
     *   dimensions less than the original field type).
     *  @param indices The number of indices.
     *  @param special Whether to handle special assign operators.
     *  @return The body of the assignment method.
     */
    private Block _createAssignmentBlock(AST ast, TypeAnalyzerState state,
            String fieldName, Type fieldType, int indices, boolean special) {
        Block block = ast.newBlock();

        // Test if the checkpoint object is not null.
        IfStatement ifStatement = ast.newIfStatement();
        InfixExpression testExpression = ast.newInfixExpression();

        InfixExpression condition1 = ast.newInfixExpression();
        condition1.setLeftOperand(ast.newSimpleName(CHECKPOINT_NAME));
        condition1.setOperator(InfixExpression.Operator.NOT_EQUALS);
        condition1.setRightOperand(ast.newNullLiteral());

        InfixExpression condition2 = ast.newInfixExpression();
        MethodInvocation getTimestamp = ast.newMethodInvocation();
        getTimestamp.setExpression(ast.newSimpleName(CHECKPOINT_NAME));
        getTimestamp.setName(ast.newSimpleName("getTimestamp"));
        condition2.setLeftOperand(getTimestamp);
        condition2.setOperator(InfixExpression.Operator.GREATER);
        condition2.setRightOperand(ast.newNumberLiteral("0"));

        testExpression.setLeftOperand(condition1);
        testExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
        testExpression.setRightOperand(condition2);
        ifStatement.setExpression(testExpression);

        // The "then" branch.
        Block thenBranch = ast.newBlock();

        // Method call to store old value.
        MethodInvocation recordInvocation = ast.newMethodInvocation();
        recordInvocation.setExpression(ast
                .newSimpleName(_getRecordName(fieldName)));
        recordInvocation.setName(ast.newSimpleName("add"));

        // If there are indices, create an integer array of those indices,
        // and add it as an argument.
        if (indices == 0) {
            recordInvocation.arguments().add(ast.newNullLiteral());
        } else {
            ArrayCreation arrayCreation = ast.newArrayCreation();
            ArrayType arrayType = ast.newArrayType(ast
                    .newPrimitiveType(PrimitiveType.INT));
            ArrayInitializer initializer = ast.newArrayInitializer();

            for (int i = 0; i < indices; i++) {
                initializer.expressions().add(ast.newSimpleName("index" + i));
            }

            arrayCreation.setType(arrayType);
            arrayCreation.setInitializer(initializer);
            recordInvocation.arguments().add(arrayCreation);
        }

        // If there are indices, add them ("index0", "index1", ...) after the
        // field.
        Expression field = ast.newSimpleName(fieldName);

        if (indices > 0) {
            for (int i = 0; i < indices; i++) {
                ArrayAccess arrayAccess = ast.newArrayAccess();
                arrayAccess.setArray(field);
                arrayAccess.setIndex(ast.newSimpleName("index" + i));
                field = arrayAccess;
            }
        }

        // Set the field as the next argument.
        recordInvocation.arguments().add(field);

        // Get current timestamp from the checkpoint object.
        MethodInvocation timestampGetter = ast.newMethodInvocation();
        timestampGetter.setExpression(ast.newSimpleName(CHECKPOINT_NAME));
        timestampGetter.setName(ast.newSimpleName("getTimestamp"));

        // Set the timestamp as the next argument.
        recordInvocation.arguments().add(timestampGetter);

        // The statement of the method call.
        ExpressionStatement recordStatement = ast
                .newExpressionStatement(recordInvocation);
        thenBranch.statements().add(recordStatement);

        ifStatement.setThenStatement(thenBranch);
        block.statements().add(ifStatement);

        // Finally, assign the new value to the field.
        Assignment assignment = ast.newAssignment();
        assignment
                .setLeftHandSide((Expression) ASTNode.copySubtree(ast, field));
        assignment.setRightHandSide(ast.newSimpleName("newValue"));
        assignment.setOperator(Assignment.Operator.ASSIGN);

        // Set the checkpoint object of the new value, if necessary.
        Class c;

        try {
            c = fieldType.toClass(state.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ASTClassNotFoundException(fieldType.getName());
        }

        if (hasMethod(c, _getSetCheckpointMethodName(false),
                new Class[] { Checkpoint.class })
                || state.getCrossAnalyzedTypes().contains(c.getName())) {
            block.statements().add(_createSetCheckpointInvocation(ast));
        } else {
            addToLists(_fixSetCheckpoint, c.getName(), block);
        }

        // Return the result of the assignment.
        if (special && _assignOperators.containsKey(fieldType.getName())) {
            String[] operators = _assignOperators.get(fieldType.getName());

            SwitchStatement switchStatement = ast.newSwitchStatement();
            switchStatement.setExpression(ast.newSimpleName("operator"));

            boolean isPostfix = true;

            for (int i = 0; i < operators.length; i++) {
                String operator = operators[i];

                SwitchCase switchCase = ast.newSwitchCase();
                switchCase.setExpression(ast.newNumberLiteral(Integer
                        .toString(i)));
                switchStatement.statements().add(switchCase);

                ReturnStatement returnStatement = ast.newReturnStatement();

                if (operator.equals("=")) {
                    Assignment newAssignment = (Assignment) ASTNode
                            .copySubtree(ast, assignment);
                    returnStatement.setExpression(newAssignment);
                } else if (operator.equals("++") || operator.equals("--")) {
                    Expression expression;

                    if (isPostfix) {
                        PostfixExpression postfix = ast.newPostfixExpression();
                        postfix.setOperand((Expression) ASTNode.copySubtree(
                                ast, assignment.getLeftHandSide()));
                        postfix.setOperator(PostfixExpression.Operator
                                .toOperator(operator));
                        expression = postfix;

                        // Produce prefix operators next time.
                        if (operator.equals("--")) {
                            isPostfix = false;
                        }
                    } else {
                        PrefixExpression prefix = ast.newPrefixExpression();
                        prefix.setOperand((Expression) ASTNode.copySubtree(ast,
                                assignment.getLeftHandSide()));
                        prefix.setOperator(PrefixExpression.Operator
                                .toOperator(operator));
                        expression = prefix;
                    }

                    returnStatement.setExpression(expression);
                } else {
                    Assignment newAssignment = (Assignment) ASTNode
                            .copySubtree(ast, assignment);
                    newAssignment.setOperator(Assignment.Operator
                            .toOperator(operator));
                    returnStatement.setExpression(newAssignment);
                }

                switchStatement.statements().add(returnStatement);
            }

            // The default statement: just return the old value.
            // This case should not be reached.
            SwitchCase defaultCase = ast.newSwitchCase();
            defaultCase.setExpression(null);
            switchStatement.statements().add(defaultCase);

            ReturnStatement defaultReturn = ast.newReturnStatement();
            defaultReturn.setExpression((Expression) ASTNode.copySubtree(ast,
                    assignment.getLeftHandSide()));
            switchStatement.statements().add(defaultReturn);

            block.statements().add(switchStatement);
        } else {
            ReturnStatement returnStatement = ast.newReturnStatement();
            returnStatement.setExpression(assignment);
            block.statements().add(returnStatement);
        }

        return block;
    }

    /** Create a backup method that backs up an array (possibly with some given
     *  indices) in memory, and return the same array to be aliased.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @param fieldName The name of the field to be backed up.
     *  @param fieldType The type of the field.
     *  @param indices The number of indices.
     *  @param isStatic Whether the field is static.
     *  @return The declaration of the backup method.
     */
    private MethodDeclaration _createBackupMethod(AST ast,
            CompilationUnit root, TypeAnalyzerState state, String fieldName,
            Type fieldType, int indices, boolean isStatic) {
        Class currentClass = state.getCurrentClass();
        ClassLoader loader = state.getClassLoader();
        String methodName = _getBackupMethodName(fieldName);

        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (_isMethodDuplicated(currentClass, methodName, fieldType, indices,
                isStatic, loader, false)) {
            throw new ASTDuplicatedMethodException(currentClass.getName(),
                    methodName);
        }

        // Get the type of the new value. The return value has the same
        // type.
        for (int i = 0; i < indices; i++) {
            try {
                fieldType = fieldType.removeOneDimension();
            } catch (ClassNotFoundException e) {
                throw new ASTClassNotFoundException(fieldType);
            }
        }

        String fieldTypeName = fieldType.getName();

        MethodDeclaration method = ast.newMethodDeclaration();
        method.setName(ast.newSimpleName(methodName));
        method.setReturnType2(createType(ast, getClassName(fieldTypeName,
                state, root)));

        // If the field is static, add a checkpoint object argument.
        if (isStatic) {
            // Add a "$CHECKPOINT" argument.
            SingleVariableDeclaration checkpoint = ast
                    .newSingleVariableDeclaration();
            String checkpointType = getClassName(Checkpoint.class, state, root);
            checkpoint.setType(ast
                    .newSimpleType(createName(ast, checkpointType)));
            checkpoint.setName(ast.newSimpleName(CHECKPOINT_NAME));
            method.parameters().add(checkpoint);
        }

        // Add all the indices.
        for (int i = 0; i < indices; i++) {
            SingleVariableDeclaration index = ast
                    .newSingleVariableDeclaration();
            index.setType(ast.newPrimitiveType(PrimitiveType.INT));
            index.setName(ast.newSimpleName("index" + i));
            method.parameters().add(index);
        }

        Block body = ast.newBlock();
        method.setBody(body);

        // The first statement: backup the whole array.
        MethodInvocation backup = ast.newMethodInvocation();
        backup.setExpression(ast.newSimpleName(_getRecordName(fieldName)));
        backup.setName(ast.newSimpleName("backup"));

        if (indices == 0) {
            backup.arguments().add(ast.newNullLiteral());
        } else {
            ArrayCreation arrayCreation = ast.newArrayCreation();
            ArrayType arrayType = ast.newArrayType(ast
                    .newPrimitiveType(PrimitiveType.INT));
            ArrayInitializer initializer = ast.newArrayInitializer();

            for (int i = 0; i < indices; i++) {
                initializer.expressions().add(ast.newSimpleName("index" + i));
            }

            arrayCreation.setType(arrayType);
            arrayCreation.setInitializer(initializer);
            backup.arguments().add(arrayCreation);
        }

        //If there are indices, add them ("index0", "index1", ...) after the
        // field.
        Expression field = ast.newSimpleName(fieldName);

        if (indices > 0) {
            for (int i = 0; i < indices; i++) {
                ArrayAccess arrayAccess = ast.newArrayAccess();
                arrayAccess.setArray(field);
                arrayAccess.setIndex(ast.newSimpleName("index" + i));
                field = arrayAccess;
            }
        }

        // Set the field as the next argument.
        backup.arguments().add(field);

        // Get current timestamp from the checkpoint object.
        MethodInvocation timestampGetter = ast.newMethodInvocation();
        timestampGetter.setExpression(ast.newSimpleName(CHECKPOINT_NAME));
        timestampGetter.setName(ast.newSimpleName("getTimestamp"));

        // Set the timestamp as the next argument.
        backup.arguments().add(timestampGetter);

        body.statements().add(ast.newExpressionStatement(backup));

        // The second statement: return the array.
        ReturnStatement returnStatement = ast.newReturnStatement();
        returnStatement.setExpression((Expression) ASTNode.copySubtree(ast,
                field));
        body.statements().add(returnStatement);

        // If the field is static, the method is also static; the method
        // is also private.
        List modifiers = method.modifiers();
        modifiers
                .add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        modifiers.add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        if (isStatic) {
            modifiers.add(ast
                    .newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        }

        return method;
    }

    /** Create the checkpoint field declaration for the current class.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @return The field declaration, or <tt>null</tt> if the field already
     *   exists in the class or its superclasses.
     */
    private FieldDeclaration _createCheckpointField(AST ast,
            CompilationUnit root, TypeAnalyzerState state) {
        Class currentClass = state.getCurrentClass();
        Class parent = currentClass.getSuperclass();

        if ((parent != null)
                && (state.getCrossAnalyzedTypes().contains(parent.getName()) || isFieldDuplicated(
                        parent, CHECKPOINT_NAME))) {
            return null;
        }

        VariableDeclarationFragment fragment = ast
                .newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(CHECKPOINT_NAME));

        ClassInstanceCreation checkpoint = ast.newClassInstanceCreation();
        String typeName = getClassName(Checkpoint.class, state, root);
        checkpoint.setType(ast.newSimpleType(createName(ast, typeName)));
        checkpoint.arguments().add(ast.newThisExpression());
        fragment.setInitializer(checkpoint);

        FieldDeclaration checkpointField = ast.newFieldDeclaration(fragment);
        checkpointField.setType(createType(ast, typeName));

        checkpointField.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD));
        checkpointField.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD));

        if (parent != null) {
            addToLists(_nodeSubstitution, parent.getName(), new NodeReplace(
                    checkpointField, null));
        }

        return checkpointField;
    }

    /** Create the field declaration for the checkpoint record.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @return The field declaration, or <tt>null</tt> if the field already
     *   exists in the class or its superclasses.
     */
    private FieldDeclaration _createCheckpointRecord(AST ast,
            CompilationUnit root, TypeAnalyzerState state) {
        Class currentClass = state.getCurrentClass();
        Class parent = currentClass.getSuperclass();

        if ((parent != null
                && state.getCrossAnalyzedTypes().contains(parent.getName()) || isFieldDuplicated(
                parent, CHECKPOINT_RECORD_NAME))) {
            return null;
        }

        VariableDeclarationFragment fragment = ast
                .newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(CHECKPOINT_RECORD_NAME));

        ClassInstanceCreation creation = ast.newClassInstanceCreation();
        String typeName = getClassName(CheckpointRecord.class, state, root);
        creation.setType(ast.newSimpleType(createName(ast, typeName)));
        fragment.setInitializer(creation);

        FieldDeclaration record = ast.newFieldDeclaration(fragment);
        record.setType(createType(ast, typeName));
        record.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD));
        record.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD));

        if (parent != null) {
            addToLists(_nodeSubstitution, parent.getName(), new NodeReplace(
                    record, null));
        }

        return record;
    }

    /** Create a commit method for a class, which commits its state up to the
     *  given time.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @param fieldNames The list of all the accessed fields.
     *  @param fieldTypes The types corresponding to the accessed fields.
     *  @param isAnonymous Whether the current class is anonymous.
     *  @param isInterface Whether the current type is an interface.
     *  @return The declaration of the method that restores the old value
     *   of all the private fields.
     */
    private MethodDeclaration _createCommitMethod(AST ast,
            CompilationUnit root, TypeAnalyzerState state, List fieldNames,
            List fieldTypes, boolean isAnonymous, boolean isInterface) {
        Class currentClass = state.getCurrentClass();
        Class parent = currentClass.getSuperclass();
        String methodName = _getCommitMethodName(isAnonymous);

        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (hasMethod(currentClass, methodName, new Class[] { int.class }, true)) {
            throw new ASTDuplicatedMethodException(currentClass.getName(),
                    methodName);
        }

        MethodDeclaration method = ast.newMethodDeclaration();

        // Set the method name.
        method.setName(ast.newSimpleName(methodName));

        // Add a timestamp parameter.
        SingleVariableDeclaration timestamp = ast
                .newSingleVariableDeclaration();
        timestamp.setType(ast.newPrimitiveType(PrimitiveType.LONG));
        timestamp.setName(ast.newSimpleName("timestamp"));
        method.parameters().add(timestamp);

        // Return type default to "void".
        if (!isInterface) {
            // The method body.
            Block body = ast.newBlock();
            method.setBody(body);

            // Add a call to the static commit method of FieldRecord.
            MethodInvocation commitFields = ast.newMethodInvocation();
            commitFields.setExpression(createName(ast, getClassName(
                    FieldRecord.class.getName(), state, root)));
            commitFields.setName(ast.newSimpleName("commit"));
            commitFields.arguments().add(ast.newSimpleName(RECORDS_NAME));
            commitFields.arguments().add(ast.newSimpleName("timestamp"));

            MethodInvocation topTimestamp = ast.newMethodInvocation();
            topTimestamp.setExpression(ast
                    .newSimpleName(CHECKPOINT_RECORD_NAME));
            topTimestamp.setName(ast.newSimpleName("getTopTimestamp"));
            commitFields.arguments().add(topTimestamp);
            body.statements().add(ast.newExpressionStatement(commitFields));

            // Add a call to the commit method in the superclass, if necessary.
            SuperMethodInvocation superRestore = ast.newSuperMethodInvocation();
            superRestore
                    .setName(ast.newSimpleName(_getCommitMethodName(false)));
            superRestore.arguments().add(ast.newSimpleName("timestamp"));

            if ((parent != null)
                    && (state.getCrossAnalyzedTypes()
                            .contains(parent.getName()) || hasMethod(parent,
                            methodName,
                            new Class[] { int.class, boolean.class }))) {
                body.statements().add(ast.newExpressionStatement(superRestore));
            } else {
                // Commit the checkpoint record.
                MethodInvocation commitCheckpoint = ast.newMethodInvocation();
                commitCheckpoint.setExpression(ast
                        .newSimpleName(CHECKPOINT_RECORD_NAME));
                commitCheckpoint.setName(ast.newSimpleName("commit"));
                commitCheckpoint.arguments()
                        .add(ast.newSimpleName("timestamp"));
                body.statements().add(
                        ast.newExpressionStatement(commitCheckpoint));

                if (parent != null) {
                    addToLists(_nodeSubstitution, parent.getName(),
                            new NodeReplace(commitCheckpoint, superRestore));
                }
            }
        }

        method.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        return method;
    }

    /** Create the record of a field. The record is stored in an extra private
     *  field of the current class.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @param fieldName The name of the field.
     *  @param dimensions The number of dimensions of the field.
     *  @param isStatic Whether the field is static.
     *  @return The field declaration to be added to the current class
     *   declaration.
     */
    private FieldDeclaration _createFieldRecord(AST ast, CompilationUnit root,
            TypeAnalyzerState state, String fieldName, int dimensions,
            boolean isStatic) {
        Class currentClass = state.getCurrentClass();
        String recordName = _getRecordName(fieldName);

        // Check if the field is duplicated (possibly because the source
        // program is refactored twice).
        if (isFieldDuplicated(currentClass, recordName)) {
            throw new ASTDuplicatedFieldException(currentClass.getName(),
                    recordName);
        }

        String typeName = getClassName(FieldRecord.class, state, root);

        // The only fragment of this field declaration.
        VariableDeclarationFragment fragment = ast
                .newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(recordName));

        // Create the initializer, and use the number of dimensions as its
        // argument.
        ClassInstanceCreation initializer = ast.newClassInstanceCreation();
        initializer.setType(ast.newSimpleType(createName(ast, typeName)));
        initializer.arguments().add(
                ast.newNumberLiteral(Integer.toString(dimensions)));
        fragment.setInitializer(initializer);

        // The field declaration.
        FieldDeclaration field = ast.newFieldDeclaration(fragment);
        field.setType(createType(ast, typeName));

        // If the field is static, the record field is also static; the record
        // field is also private.
        List modifiers = field.modifiers();
        modifiers
                .add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        modifiers.add(ast
                .newModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD));
        if (isStatic) {
            modifiers.add(ast
                    .newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        }

        return field;
    }

    /** Create a get checkpoint method that returns the checkpoint object.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @param isAnonymous Whether the current class is anonymous.
     *  @param isInterface Whether the current type is an interface.
     *  @return The declaration of the get checkpoint method.
     */
    private MethodDeclaration _createGetCheckpointMethod(AST ast,
            CompilationUnit root, TypeAnalyzerState state, boolean isAnonymous,
            boolean isInterface) {
        String methodName = _getGetCheckpointMethodName(isAnonymous);

        Class currentClass = state.getCurrentClass();
        Class parent = currentClass.getSuperclass();

        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (hasMethod(currentClass, methodName, new Class[0])) {
            throw new ASTDuplicatedMethodException(currentClass.getName(),
                    methodName);
        }

        if (!isAnonymous
                && (parent != null)
                && (state.getCrossAnalyzedTypes().contains(parent.getName()) || hasMethod(
                        parent, methodName, new Class[] {}))) {
            return null;
        }

        MethodDeclaration method = ast.newMethodDeclaration();
        method.setName(ast.newSimpleName(methodName));

        String typeName = getClassName(Checkpoint.class, state, root);
        method.setReturnType2(createType(ast, typeName));

        if (!isInterface) {
            // The body, just to return the checkpoint object.
            Block body = ast.newBlock();
            method.setBody(body);

            ReturnStatement returnStatement = ast.newReturnStatement();
            returnStatement.setExpression(ast.newSimpleName(CHECKPOINT_NAME));
            body.statements().add(returnStatement);
        }

        method.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        if (!isInterface) {
            method.modifiers().add(
                    ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        }

        if (!isAnonymous && (parent != null)) {
            addToLists(_nodeSubstitution, parent.getName(), new NodeReplace(
                    method, null));
        }

        return method;
    }

    /** Create a proxy class for an anonymous class. The proxy class implements
     *  the {@link Rollbackable} interface.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @return The type declaration of the proxy class.
     */
    private TypeDeclaration _createProxyClass(AST ast, CompilationUnit root,
            TypeAnalyzerState state) {
        // Create the nested class.
        TypeDeclaration classDeclaration = ast.newTypeDeclaration();
        classDeclaration.setName(ast.newSimpleName(_getProxyName()));

        String rollbackType = getClassName(Rollbackable.class, state, root);
        classDeclaration.superInterfaceTypes().add(
                ast.newSimpleType(createName(ast, rollbackType)));

        // Add a commit method.
        MethodDeclaration commit = ast.newMethodDeclaration();
        commit.setName(ast.newSimpleName(_getCommitMethodName(false)));

        // Add two parameters.
        SingleVariableDeclaration timestamp = ast
                .newSingleVariableDeclaration();
        timestamp.setType(ast.newPrimitiveType(PrimitiveType.LONG));
        timestamp.setName(ast.newSimpleName("timestamp"));
        commit.parameters().add(timestamp);

        // Add a call to the restore method in the enclosing anonymous class.
        MethodInvocation invocation = ast.newMethodInvocation();
        invocation.setName(ast.newSimpleName(_getCommitMethodName(true)));
        invocation.arguments().add(ast.newSimpleName("timestamp"));

        Block body = ast.newBlock();
        body.statements().add(ast.newExpressionStatement(invocation));
        commit.setBody(body);

        commit.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        commit.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        classDeclaration.bodyDeclarations().add(commit);

        // Add a restore method.
        MethodDeclaration restore = ast.newMethodDeclaration();
        restore.setName(ast.newSimpleName(_getRestoreMethodName(false)));

        // Add two parameters.
        timestamp = ast.newSingleVariableDeclaration();
        timestamp.setType(ast.newPrimitiveType(PrimitiveType.LONG));
        timestamp.setName(ast.newSimpleName("timestamp"));
        restore.parameters().add(timestamp);

        SingleVariableDeclaration trim = ast.newSingleVariableDeclaration();
        trim.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
        trim.setName(ast.newSimpleName("trim"));
        restore.parameters().add(trim);

        // Add a call to the restore method in the enclosing anonymous class.
        invocation = ast.newMethodInvocation();
        invocation.setName(ast.newSimpleName(_getRestoreMethodName(true)));
        invocation.arguments().add(ast.newSimpleName("timestamp"));
        invocation.arguments().add(ast.newSimpleName("trim"));

        body = ast.newBlock();
        body.statements().add(ast.newExpressionStatement(invocation));
        restore.setBody(body);

        restore.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        restore.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        classDeclaration.bodyDeclarations().add(restore);

        // Add a get checkpoint method.
        MethodDeclaration getCheckpoint = ast.newMethodDeclaration();
        String checkpointType = getClassName(Checkpoint.class, state, root);
        getCheckpoint.setName(ast
                .newSimpleName(_getGetCheckpointMethodName(false)));
        getCheckpoint.setReturnType2(createType(ast, checkpointType));
        invocation = ast.newMethodInvocation();
        invocation
                .setName(ast.newSimpleName(_getGetCheckpointMethodName(true)));
        body = ast.newBlock();

        ReturnStatement returnStatement = ast.newReturnStatement();
        returnStatement.setExpression(invocation);
        body.statements().add(returnStatement);
        getCheckpoint.setBody(body);

        getCheckpoint.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        getCheckpoint.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        classDeclaration.bodyDeclarations().add(getCheckpoint);

        // Add a set checkpoint method.
        MethodDeclaration setCheckpoint = ast.newMethodDeclaration();
        setCheckpoint.setName(ast
                .newSimpleName(_getSetCheckpointMethodName(false)));
        setCheckpoint.setReturnType2(createType(ast, getClassName(Object.class,
                state, root)));

        // Add a single checkpoint parameter.
        SingleVariableDeclaration checkpoint = ast
                .newSingleVariableDeclaration();
        checkpoint.setType(createType(ast, checkpointType));
        checkpoint.setName(ast.newSimpleName("checkpoint"));
        setCheckpoint.parameters().add(checkpoint);

        // Add a call to the setcheckpoint method in the enclosing anonymous
        // class.
        invocation = ast.newMethodInvocation();
        invocation
                .setName(ast.newSimpleName(_getSetCheckpointMethodName(true)));
        invocation.arguments().add(ast.newSimpleName("checkpoint"));

        // Return this object.
        returnStatement = ast.newReturnStatement();
        returnStatement.setExpression(ast.newThisExpression());

        body = ast.newBlock();
        body.statements().add(ast.newExpressionStatement(invocation));
        body.statements().add(returnStatement);
        setCheckpoint.setBody(body);

        setCheckpoint.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        setCheckpoint.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        classDeclaration.bodyDeclarations().add(setCheckpoint);

        classDeclaration.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        return classDeclaration;
    }

    /** Create a field that stores all the records as an array.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @param fieldNames The names of all the fields.
     *  @return The field declaration of the record array.
     */
    private FieldDeclaration _createRecordArray(AST ast, CompilationUnit root,
            TypeAnalyzerState state, List fieldNames) {
        VariableDeclarationFragment fragment = ast
                .newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(RECORDS_NAME));

        ArrayCreation initializer = ast.newArrayCreation();
        String typeName = getClassName(FieldRecord.class, state, root);
        initializer.setType(ast.newArrayType(createType(ast, typeName)));

        Iterator fields = fieldNames.iterator();
        ArrayInitializer arrayInitializer = ast.newArrayInitializer();
        initializer.setInitializer(arrayInitializer);

        List expressions = arrayInitializer.expressions();

        while (fields.hasNext()) {
            String fieldName = (String) fields.next();

            String recordName = _getRecordName(fieldName);
            expressions.add(ast.newSimpleName(recordName));
        }

        fragment.setInitializer(initializer);

        FieldDeclaration array = ast.newFieldDeclaration(fragment);
        array.setType(ast.newArrayType(createType(ast, typeName)));

        array.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        array.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD));
        return array;
    }

    /** Create a restore method for a class, which restores all its state
     *  variables.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @param fieldNames The list of all the accessed fields.
     *  @param fieldTypes The types corresponding to the accessed fields.
     *  @param isAnonymous Whether the current class is anonymous.
     *  @param isInterface Whether the current type is an interface.
     *  @return The declaration of the method that restores the old value
     *   of all the private fields.
     */
    private MethodDeclaration _createRestoreMethod(AST ast,
            CompilationUnit root, TypeAnalyzerState state, List fieldNames,
            List fieldTypes, boolean isAnonymous, boolean isInterface) {
        Class currentClass = state.getCurrentClass();
        Class parent = currentClass.getSuperclass();
        String methodName = _getRestoreMethodName(isAnonymous);

        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (hasMethod(currentClass, methodName, new Class[] { int.class,
                boolean.class }, true)) {
            throw new ASTDuplicatedMethodException(currentClass.getName(),
                    methodName);
        }

        MethodDeclaration method = ast.newMethodDeclaration();

        // Set the method name.
        method.setName(ast.newSimpleName(methodName));

        // Add a timestamp parameter.
        SingleVariableDeclaration timestamp = ast
                .newSingleVariableDeclaration();
        timestamp.setType(ast.newPrimitiveType(PrimitiveType.LONG));
        timestamp.setName(ast.newSimpleName("timestamp"));
        method.parameters().add(timestamp);

        // Add a trim parameter.
        SingleVariableDeclaration trim = ast.newSingleVariableDeclaration();
        trim.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
        trim.setName(ast.newSimpleName("trim"));
        method.parameters().add(trim);

        // Return type default to "void".
        if (!isInterface) {
            // The method body.
            Block body = ast.newBlock();
            method.setBody(body);

            // Create a restore statement for each managed field.
            Iterator namesIter = fieldNames.iterator();
            Iterator typesIter = fieldTypes.iterator();

            while (namesIter.hasNext()) {
                String fieldName = (String) namesIter.next();
                Type fieldType = (Type) typesIter.next();

                MethodInvocation restoreMethodCall = ast.newMethodInvocation();
                restoreMethodCall.setExpression(ast
                        .newSimpleName(_getRecordName(fieldName)));

                // Set the restore method name.
                restoreMethodCall.arguments().add(ast.newSimpleName(fieldName));
                restoreMethodCall.setName(ast.newSimpleName("restore"));

                // Add two arguments to the restore method call.
                restoreMethodCall.arguments().add(
                        ast.newSimpleName("timestamp"));
                restoreMethodCall.arguments().add(ast.newSimpleName("trim"));

                boolean isFinal = false;

                try {
                    Field field = currentClass.getDeclaredField(fieldName);

                    if (java.lang.reflect.Modifier
                            .isFinal(field.getModifiers())) {
                        isFinal = true;
                    }
                } catch (NoSuchFieldException e) {
                }

                if (isFinal) {
                    if ((_getAccessedField(currentClass.getName(), fieldName) != null)
                            || !Type.isPrimitive(Type.getElementType(fieldType
                                    .getName()))) {
                        body.statements().add(
                                ast.newExpressionStatement(restoreMethodCall));
                    }
                } else {
                    Expression rightHandSide;

                    if (fieldType.isPrimitive()) {
                        rightHandSide = restoreMethodCall;
                    } else {
                        CastExpression castExpression = ast.newCastExpression();
                        String typeName = getClassName(fieldType.getName(),
                                state, root);
                        castExpression.setType(createType(ast, typeName));
                        castExpression.setExpression(restoreMethodCall);
                        rightHandSide = castExpression;
                    }

                    Assignment assignment = ast.newAssignment();
                    assignment.setLeftHandSide(ast.newSimpleName(fieldName));
                    assignment.setRightHandSide(rightHandSide);
                    body.statements().add(
                            ast.newExpressionStatement(assignment));
                }
            }

            // Add a call to the restore method in the superclass, if necessary.
            SuperMethodInvocation superRestore = ast.newSuperMethodInvocation();
            superRestore.setName(ast
                    .newSimpleName(_getRestoreMethodName(false)));
            superRestore.arguments().add(ast.newSimpleName("timestamp"));
            superRestore.arguments().add(ast.newSimpleName("trim"));

            Statement superRestoreStatement = ast
                    .newExpressionStatement(superRestore);

            if ((parent != null)
                    && (state.getCrossAnalyzedTypes()
                            .contains(parent.getName()) || hasMethod(parent,
                            methodName,
                            new Class[] { int.class, boolean.class }))) {
                body.statements().add(superRestoreStatement);
            } else {
                // Restore the previous checkpoint, if necessary.
                IfStatement restoreCheckpoint = ast.newIfStatement();

                InfixExpression timestampTester = ast.newInfixExpression();
                timestampTester.setLeftOperand(ast.newSimpleName("timestamp"));
                timestampTester
                        .setOperator(InfixExpression.Operator.LESS_EQUALS);

                MethodInvocation topTimestamp = ast.newMethodInvocation();
                topTimestamp.setExpression(ast
                        .newSimpleName(CHECKPOINT_RECORD_NAME));
                topTimestamp.setName(ast.newSimpleName("getTopTimestamp"));
                timestampTester.setRightOperand(topTimestamp);
                restoreCheckpoint.setExpression(timestampTester);

                Block restoreBlock = ast.newBlock();
                restoreCheckpoint.setThenStatement(restoreBlock);

                // Assign the old checkpoint.
                Assignment assignCheckpoint = ast.newAssignment();
                assignCheckpoint.setLeftHandSide(ast
                        .newSimpleName(CHECKPOINT_NAME));

                MethodInvocation restoreCheckpointInvocation = ast
                        .newMethodInvocation();
                restoreCheckpointInvocation.setExpression(ast
                        .newSimpleName(CHECKPOINT_RECORD_NAME));
                restoreCheckpointInvocation.setName(ast
                        .newSimpleName("restore"));
                restoreCheckpointInvocation.arguments().add(
                        ast.newSimpleName(CHECKPOINT_NAME));
                restoreCheckpointInvocation.arguments().add(
                        _createRollbackableObject(ast, isAnonymous));
                restoreCheckpointInvocation.arguments().add(
                        ast.newSimpleName("timestamp"));
                restoreCheckpointInvocation.arguments().add(
                        ast.newSimpleName("trim"));
                assignCheckpoint.setRightHandSide(restoreCheckpointInvocation);
                restoreBlock.statements().add(
                        ast.newExpressionStatement(assignCheckpoint));

                // Pop the old states.
                MethodInvocation popStates = ast.newMethodInvocation();
                String recordType = getClassName(FieldRecord.class, state, root);
                popStates.setExpression(createName(ast, recordType));
                popStates.setName(ast.newSimpleName("popState"));
                popStates.arguments().add(ast.newSimpleName(RECORDS_NAME));
                restoreBlock.statements().add(
                        ast.newExpressionStatement(popStates));

                // Recall the restore method.
                MethodInvocation recursion = ast.newMethodInvocation();
                recursion.setName(ast.newSimpleName(methodName));
                recursion.arguments().add(ast.newSimpleName("timestamp"));
                recursion.arguments().add(ast.newSimpleName("trim"));
                restoreBlock.statements().add(
                        ast.newExpressionStatement(recursion));

                body.statements().add(restoreCheckpoint);

                if (parent != null) {
                    addToLists(_nodeSubstitution, parent.getName(),
                            new NodeReplace(restoreCheckpoint,
                                    superRestoreStatement));
                }
            }
        }

        method.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        return method;
    }

    /** Create an expression that evaluates to a {@link Rollbackable} object
     *  for the current class.
     *  <p>
     *  {@link Rollbackable} is the interfact that every rollbackable class
     *  must implement. For an ordinary class, the {@link Rollbackable} object
     *  is <tt>this</tt> object, because the class, after refactoring,
     *  implements {@link Rollbackable}. For an anonymous class, a special
     *  proxy class is created as a nested class in it. The proxy class
     *  implements {@link Rollbackable} and deligates the calls to its methods
     *  to the enclosing anonymous class. In that case, the {@link
     *  Rollbackable} object is an object of the proxy class.
     *
     *  @param ast The {@link AST} object.
     *  @param isAnonymous Whether the current class is anonymous.
     *  @return The expression that evaluates to a {@link Rollbackable} object
     *   at run-time.
     */
    private Expression _createRollbackableObject(AST ast, boolean isAnonymous) {
        if (isAnonymous) {
            ClassInstanceCreation proxy = ast.newClassInstanceCreation();
            proxy
                    .setType(ast.newSimpleType(ast
                            .newSimpleName(_getProxyName())));
            return proxy;
        } else {
            return ast.newThisExpression();
        }
    }

    /** Create a set checkpoint method invocation for an assignment block.
     *
     *  @param ast The {@link AST} object.
     *  @return The statement that tests whether the checkpoint objects of the
     *   current object and the new value are the same; if not, assign the
     *   checkpoint object of the current object to the checkpoint of the new
     *   value.
     */
    private Statement _createSetCheckpointInvocation(AST ast) {
        InfixExpression test = ast.newInfixExpression();

        InfixExpression condition1 = ast.newInfixExpression();
        condition1.setLeftOperand(ast.newSimpleName("newValue"));
        condition1.setOperator(InfixExpression.Operator.NOT_EQUALS);
        condition1.setRightOperand(ast.newNullLiteral());

        InfixExpression condition2 = ast.newInfixExpression();
        condition2.setLeftOperand(ast.newSimpleName(CHECKPOINT_NAME));
        condition2.setOperator(InfixExpression.Operator.NOT_EQUALS);

        MethodInvocation getCheckpoint = ast.newMethodInvocation();
        getCheckpoint.setExpression(ast.newSimpleName("newValue"));
        getCheckpoint.setName(ast
                .newSimpleName(_getGetCheckpointMethodName(false)));
        condition2.setRightOperand(getCheckpoint);

        test.setLeftOperand(condition1);
        test.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
        test.setRightOperand(condition2);

        IfStatement ifStatement = ast.newIfStatement();
        ifStatement.setExpression(test);

        Block thenBranch = ast.newBlock();
        ifStatement.setThenStatement(thenBranch);

        MethodInvocation setCheckpoint = ast.newMethodInvocation();
        setCheckpoint.setExpression(ast.newSimpleName("newValue"));
        setCheckpoint.setName(ast.newSimpleName(SET_CHECKPOINT_NAME));
        setCheckpoint.arguments().add(ast.newSimpleName(CHECKPOINT_NAME));
        thenBranch.statements().add(ast.newExpressionStatement(setCheckpoint));

        return ifStatement;
    }

    /** Create a set checkpoint method for a class, which sets its checkpoint
     *  object.
     *
     *  @param ast The {@link AST} object.
     *  @param root The root of the AST.
     *  @param state The current state of the type analyzer.
     *  @param isAnonymous Whether the current class is anonymous.
     *  @param isInterface Whether the current type is an interface.
     *  @return The declaration of the method that sets the checkpoint object.
     */
    private MethodDeclaration _createSetCheckpointMethod(AST ast,
            CompilationUnit root, TypeAnalyzerState state, boolean isAnonymous,
            boolean isInterface) {
        String methodName = _getSetCheckpointMethodName(isAnonymous);

        Class currentClass = state.getCurrentClass();
        Class parent = currentClass.getSuperclass();

        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (hasMethod(currentClass, methodName,
                new Class[] { Checkpoint.class }, true)) {
            throw new ASTDuplicatedMethodException(currentClass.getName(),
                    methodName);
        }

        if (!isAnonymous
                && (parent != null)
                && (state.getCrossAnalyzedTypes().contains(parent.getName()) || hasMethod(
                        parent, methodName, new Class[] { Checkpoint.class }))) {
            return null;
        }

        MethodDeclaration method = ast.newMethodDeclaration();

        // Set the method name.
        method.setName(ast.newSimpleName(methodName));

        // Add a checkpoint parameter.
        SingleVariableDeclaration checkpoint = ast
                .newSingleVariableDeclaration();
        String checkpointType = getClassName(Checkpoint.class, state, root);
        checkpoint.setType(createType(ast, checkpointType));
        checkpoint.setName(ast.newSimpleName("checkpoint"));
        method.parameters().add(checkpoint);

        // Return type is Object.
        method.setReturnType2(createType(ast, getClassName(Object.class, state,
                root)));

        if (!isInterface) {
            // The test.
            IfStatement test = ast.newIfStatement();
            InfixExpression testExpression = ast.newInfixExpression();
            testExpression.setLeftOperand(ast.newSimpleName(CHECKPOINT_NAME));
            testExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
            testExpression.setRightOperand(ast.newSimpleName("checkpoint"));
            test.setExpression(testExpression);

            // The "then" branch of the test.
            Block thenBranch = ast.newBlock();
            test.setThenStatement(thenBranch);

            Block body = ast.newBlock();
            body.statements().add(test);
            method.setBody(body);

            // Backup the old checkpoint.
            VariableDeclarationFragment fragment = ast
                    .newVariableDeclarationFragment();
            fragment.setName(ast.newSimpleName("oldCheckpoint"));
            fragment.setInitializer(ast.newSimpleName(CHECKPOINT_NAME));

            VariableDeclarationStatement tempDeclaration = ast
                    .newVariableDeclarationStatement(fragment);
            tempDeclaration.setType(createType(ast, checkpointType));
            thenBranch.statements().add(tempDeclaration);

            // Record the old checkpoint if the new checkpoint is not null.
            // If it is null, it is impossible to roll back to the previous
            // checkpoint.
            IfStatement testNewCheckpoint = ast.newIfStatement();
            InfixExpression testNull = ast.newInfixExpression();
            testNull.setLeftOperand(ast.newSimpleName("checkpoint"));
            testNull.setOperator(InfixExpression.Operator.NOT_EQUALS);
            testNull.setRightOperand(ast.newNullLiteral());
            testNewCheckpoint.setExpression(testNull);

            Block testNewCheckpointBody = ast.newBlock();
            testNewCheckpoint.setThenStatement(testNewCheckpointBody);

            MethodInvocation record = ast.newMethodInvocation();
            record.setExpression(ast.newSimpleName(CHECKPOINT_RECORD_NAME));
            record.setName(ast.newSimpleName("add"));
            record.arguments().add(ast.newSimpleName(CHECKPOINT_NAME));

            MethodInvocation getTimestamp = ast.newMethodInvocation();
            getTimestamp.setExpression(ast.newSimpleName("checkpoint"));
            getTimestamp.setName(ast.newSimpleName("getTimestamp"));
            record.arguments().add(getTimestamp);
            testNewCheckpointBody.statements().add(
                    ast.newExpressionStatement(record));

            MethodInvocation pushStates = ast.newMethodInvocation();
            String recordType = getClassName(FieldRecord.class, state, root);
            pushStates.setExpression(createName(ast, recordType));
            pushStates.setName(ast.newSimpleName("pushState"));
            pushStates.arguments().add(ast.newSimpleName(RECORDS_NAME));
            testNewCheckpointBody.statements().add(
                    ast.newExpressionStatement(pushStates));
            thenBranch.statements().add(testNewCheckpoint);

            // Assign the new checkpoint.
            Assignment assignment = ast.newAssignment();
            assignment.setLeftHandSide(ast.newSimpleName(CHECKPOINT_NAME));
            assignment.setRightHandSide(ast.newSimpleName("checkpoint"));

            ExpressionStatement statement = ast
                    .newExpressionStatement(assignment);
            thenBranch.statements().add(statement);

            // Propagate the change to other objects monitored by the same old
            // checkpoint.
            MethodInvocation propagate = ast.newMethodInvocation();
            propagate.setExpression(ast.newSimpleName("oldCheckpoint"));
            propagate.setName(ast.newSimpleName("setCheckpoint"));
            propagate.arguments().add(ast.newSimpleName("checkpoint"));
            thenBranch.statements().add(ast.newExpressionStatement(propagate));

            // Add this object to the list in the checkpoint.
            MethodInvocation addInvocation = ast.newMethodInvocation();
            addInvocation.setExpression(ast.newSimpleName("checkpoint"));
            addInvocation.setName(ast.newSimpleName("addObject"));
            addInvocation.arguments().add(
                    _createRollbackableObject(ast, isAnonymous));
            thenBranch.statements().add(
                    ast.newExpressionStatement(addInvocation));

            // Return this object.
            ReturnStatement returnStatement = ast.newReturnStatement();
            returnStatement.setExpression(ast.newThisExpression());
            body.statements().add(returnStatement);
        }

        method.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        if (!isInterface) {
            method.modifiers().add(
                    ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        }

        if (!isAnonymous && (parent != null)) {
            addToLists(_nodeSubstitution, parent.getName(), new NodeReplace(
                    method, null));
        }

        return method;
    }

    /** Get the list of indices of an accessed field. If the field is not of an
     *  array type but it is accessed at least once, the returned list contains
     *  only integer 0, which means no index is ever used. If the field is an
     *  array, the returned list is the list of numbers of indices that have
     *  ever been used.
     *
     *  @param table The hash table to be used.
     *  @param className The full name of the current class.
     *  @param fieldName The field name.
     *  @return The list of indices, or <tt>null</tt> if the field is not found
     *   or has never been used.
     */
    private List _getAccessedField(Hashtable table, String className,
            String fieldName) {
        Hashtable classTable = (Hashtable) table.get(className);

        if (classTable == null) {
            return null;
        }

        List indicesList = (List) classTable.get(fieldName);
        return indicesList;
    }

    /** Get the list of indices of an accessed field. If the field is not of an
     *  array type but it is accessed at least once, the returned list contains
     *  only integer 0, which means no index is ever used. If the field is an
     *  array, the returned list is the list of numbers of indices that have
     *  ever been used.
     *
     *  @param className The full name of the current class.
     *  @param fieldName The field name.
     *  @return The list of indices, or <tt>null</tt> if the field is not found
     *   or has never been used.
     */
    private List _getAccessedField(String className, String fieldName) {
        return _getAccessedField(_accessedFields, className, fieldName);
    }

    /** Get the name of the commit method.
     *
     *  @param isAnonymous Whether the current class is an anonymous class.
     *  @return The name of the commit method.
     */
    private String _getCommitMethodName(boolean isAnonymous) {
        return COMMIT_NAME + (isAnonymous ? "_ANONYMOUS" : "");
    }

    /** Get the name of the get checkpoint method.
     *
     *  @param isAnonymous Whether the current class is an anonymous class.
     *  @return The name of the get checkpoint method.
     */
    private String _getGetCheckpointMethodName(boolean isAnonymous) {
        return GET_CHECKPOINT_NAME + (isAnonymous ? "_ANONYMOUS" : "");
    }

    /** Get the name of the proxy class to be created in each anonymous class.
     *
     *  @return The proxy class name.
     */
    private String _getProxyName() {
        return PROXY_NAME;
    }

    /** Get the name of the record.
     *
     *  @param fieldName The field name.
     *  @return The record name.
     */
    private String _getRecordName(String fieldName) {
        return RECORD_PREFIX + fieldName;
    }

    /** Get the name of the restore method.
     *
     *  @param isAnonymous Whether the current class is an anonymous class.
     *  @return The name of the restore method.
     */
    private String _getRestoreMethodName(boolean isAnonymous) {
        return RESTORE_NAME + (isAnonymous ? "_ANONYMOUS" : "");
    }

    /** Get the name of the set checkpoint method.
     *
     *  @param isAnonymous Whether the current class is an anonymous class.
     *  @return The name of the set checkpoint method.
     */
    private String _getSetCheckpointMethodName(boolean isAnonymous) {
        return SET_CHECKPOINT_NAME + (isAnonymous ? "_ANONYMOUS" : "");
    }

    /** Handle an alias expression. If the expression is not aliased (e.g.,
     *  an expression corresponding to an object or a primitive integer
     *  cannot be aliased), nothing is done. If it can be aliased, in the
     *  refactored result, the expression's <tt>clone</tt> method is called
     *  and the copy is used as the record.
     *
     *  @param node The node that may be aliased.
     *  @param state The current state of the type analyzer.
     *  @return The new node if the given node is an alias, or <tt>null</tt>
     *   otherwise.
     */
    private Expression _handleAlias(Expression node, TypeAnalyzerState state) {
        Type owner = Type.getOwner(node);

        if (owner == null) { // Not a field.
            return null;
        }

        @SuppressWarnings("unused")
        String ownerName = owner.getName();
        Type type = Type.getType(node);
        @SuppressWarnings("unused")
        String typeName = type.getName();

        Expression array = node;
        Type arrayType = type;

        // Check if we need to refactor the argument.
        boolean needRefactor = type.isArray();

        if (node instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) node;

            while (arrayAccess.getArray() instanceof ArrayAccess) {
                arrayAccess = (ArrayAccess) arrayAccess.getArray();
                arrayType = arrayType.addOneDimension();
            }

            array = arrayAccess.getArray();
            arrayType = arrayType.addOneDimension();

            if (array instanceof MethodInvocation) {
                needRefactor = false;
            }
        }

        if (needRefactor) {
            // Refactor the expression.
            AST ast = node.getAST();
            //CompilationUnit root = (CompilationUnit) node.getRoot();
            //String typeClassName = getClassName(typeName, state, root);

            int nIndices = 0;

            Expression nodeIterator = node;

            while (nodeIterator instanceof ParenthesizedExpression) {
                nodeIterator = ((ParenthesizedExpression) nodeIterator)
                        .getExpression();
            }

            List indices = new LinkedList();

            while (nodeIterator instanceof ArrayAccess) {
                nIndices++;

                ArrayAccess arrayAccess = (ArrayAccess) nodeIterator;
                indices
                        .add(0, ASTNode
                                .copySubtree(ast, arrayAccess.getIndex()));
                nodeIterator = arrayAccess.getArray();

                while (nodeIterator instanceof ParenthesizedExpression) {
                    nodeIterator = ((ParenthesizedExpression) nodeIterator)
                            .getExpression();
                }
            }

            Expression newObject = null;
            SimpleName name;

            if (nodeIterator instanceof FieldAccess) {
                Expression object = ((FieldAccess) nodeIterator)
                        .getExpression();
                name = ((FieldAccess) nodeIterator).getName();
                newObject = (Expression) ASTNode.copySubtree(ast, object);
            } else if (nodeIterator instanceof QualifiedName) {
                Name object = ((QualifiedName) nodeIterator).getQualifier();
                name = ((QualifiedName) nodeIterator).getName();
                newObject = (Expression) ASTNode.copySubtree(ast, object);
            } else if (nodeIterator instanceof SimpleName) {
                name = (SimpleName) nodeIterator;
            } else {
                return null;
            }

            // Get the class of the owner and test the modifiers of the field.
            Class ownerClass;
            boolean isStatic;

            try {
                ownerClass = owner.toClass(state.getClassLoader());

                Field field = ownerClass.getDeclaredField(name.getIdentifier());
                int modifiers = field.getModifiers();

                if (!java.lang.reflect.Modifier.isPrivate(modifiers)) {
                    return null; // Not handling non-private fields.
                }

                isStatic = java.lang.reflect.Modifier.isStatic(modifiers);
            } catch (ClassNotFoundException e) {
                throw new ASTClassNotFoundException(owner.getName());
            } catch (NoSuchFieldException e) {
                // The field is not defined in this class.
                return null;
            }

            if (isStatic && !HANDLE_STATIC_FIELDS) {
                return null;
            }

            MethodInvocation backup = ast.newMethodInvocation();

            if (newObject != null) {
                backup.setExpression(newObject);
            }

            SimpleName newName = ast.newSimpleName(_getBackupMethodName(name
                    .getIdentifier()));
            backup.setName(newName);

            // If the field is static, add the checkpoint object as the first
            // argument.
            if (isStatic) {
                backup.arguments().add(ast.newSimpleName(CHECKPOINT_NAME));
            }

            // Add all the indices into the argument list.
            backup.arguments().addAll(indices);

            replaceNode(node, backup);

            _recordField(_backupFields, owner.getName(), name.getIdentifier(),
                    nIndices);

            return backup;
        }

        return null;
    }

    /** Handle an explicit or implicit assignment node. An explicit assignment
     *  is one that uses the "=" operator or any special assign operator (such
     *  as "+=" and "-="). An implicit a prefix expression or postfix
     *  expression with the "++" operator or the "--" operator.
     *
     *  @param node The AST node of the assignment. It must be of one of the
     *   following types: {@link Assignment}, {@link PostfixExpression}, and
     *   {@link PrefixExpression}.
     *  @param state The current state of the type analyzer.
     */
    private void _handleAssignment(ASTNode node, TypeAnalyzerState state) {
        AST ast = node.getAST();
        boolean isSpecial;

        if (node instanceof Assignment) {
            isSpecial = ((Assignment) node).getOperator() != Assignment.Operator.ASSIGN;
        } else {
            isSpecial = true;
        }

        // Get the left-hand side and right-hand side.
        Expression leftHand;

        // Get the left-hand side and right-hand side.
        Expression rightHand;

        if (node instanceof Assignment) {
            leftHand = ((Assignment) node).getLeftHandSide();
            rightHand = ((Assignment) node).getRightHandSide();
        } else if (node instanceof PrefixExpression) {
            leftHand = rightHand = ((PrefixExpression) node).getOperand();
        } else {
            leftHand = rightHand = ((PostfixExpression) node).getOperand();
        }

        while (leftHand instanceof ParenthesizedExpression) {
            leftHand = ((ParenthesizedExpression) leftHand).getExpression();
        }

        // If left-hand side is an array access, store the indices.
        List indices = new LinkedList();

        while (leftHand instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) leftHand;
            indices.add(0, ASTNode.copySubtree(ast, arrayAccess.getIndex()));
            leftHand = arrayAccess.getArray();

            while (leftHand instanceof ParenthesizedExpression) {
                leftHand = ((ParenthesizedExpression) leftHand).getExpression();
            }
        }

        // For expression.name on the left-hand side, set newObject to be the
        // expression and name to be the name. newObject may be null.
        Expression newObject = null;
        SimpleName name;

        if (leftHand instanceof FieldAccess) {
            Expression object = ((FieldAccess) leftHand).getExpression();
            name = ((FieldAccess) leftHand).getName();

            Type type = Type.getType(object);

            if (!type.getName().equals(state.getCurrentClass().getName())) {
                return;
            }

            newObject = (Expression) ASTNode.copySubtree(ast, object);
        } else if (leftHand instanceof QualifiedName) {
            Name object = ((QualifiedName) leftHand).getQualifier();
            name = ((QualifiedName) leftHand).getName();

            Type type = Type.getType(object);

            if (!type.getName().equals(state.getCurrentClass().getName())) {
                return;
            }

            newObject = (Expression) ASTNode.copySubtree(ast, object);
        } else if (leftHand instanceof SimpleName) {
            name = (SimpleName) leftHand;
        } else {
            return; // Some unknown situation.
        }

        // Get the owner of the left-hand side, if it is a field.
        Type owner = Type.getOwner(leftHand);

        if (owner == null) { // Not a field.
            return;
        }

        // Get the class of the owner and test the modifiers of the field.
        Class ownerClass;
        boolean isStatic;

        try {
            ownerClass = owner.toClass(state.getClassLoader());

            Field field = ownerClass.getDeclaredField(name.getIdentifier());
            int modifiers = field.getModifiers();

            if (!java.lang.reflect.Modifier.isPrivate(modifiers)) {
                return; // Not handling non-private fields or final fields.
            }

            if (java.lang.reflect.Modifier.isFinal(modifiers)) {
                if (!field.getType().isArray()) {
                    return;
                }
            }

            isStatic = java.lang.reflect.Modifier.isStatic(modifiers);
        } catch (ClassNotFoundException e) {
            throw new ASTClassNotFoundException(owner.getName());
        } catch (NoSuchFieldException e) {
            // The field is not defined in this class.
            return;
        }

        if (isStatic && !HANDLE_STATIC_FIELDS) {
            return;
        }

        // The new method invocation to replace the assignment.
        MethodInvocation invocation = ast.newMethodInvocation();

        // Set the expression and name of the method invocation.
        if (newObject != null) {
            invocation.setExpression(newObject);
        }

        SimpleName newName = ast.newSimpleName(_getAssignMethodName(name
                .getIdentifier(), isSpecial));
        invocation.setName(newName);

        // If the field is static, add the checkpoint object as the first
        // argument.
        if (isStatic) {
            invocation.arguments().add(ast.newSimpleName(CHECKPOINT_NAME));
        }

        // Add an operator, if necessary.
        Type type = Type.getType(node);

        if (isSpecial && _assignOperators.containsKey(type.getName())) {
            int i = 0;
            String[] operators = _assignOperators.get(type.getName());

            String operator;

            if (node instanceof Assignment) {
                operator = ((Assignment) node).getOperator().toString();
            } else if (node instanceof PrefixExpression) {
                operator = ((PrefixExpression) node).getOperator().toString();
            } else {
                operator = ((PostfixExpression) node).getOperator().toString();
            }

            for (; i < operators.length; i++) {
                if (operators[i].equals(operator)) {
                    break;
                }
            }

            if (node instanceof PrefixExpression) {
                i += 2;
            }

            invocation.arguments().add(
                    ast.newNumberLiteral(Integer.toString(i)));
        }

        // Add all the indices into the argument list.
        invocation.arguments().addAll(indices);

        // Add the right-hand side expression to the argument list.
        Type rightHandType = Type.getType(rightHand);

        if (!isSpecial && type.isPrimitive() && !type.equals(rightHandType)) {
            // Require an explicit conversion.
            CastExpression castExpression = ast.newCastExpression();
            castExpression.setType(createType(ast, type.getName()));
            castExpression.setExpression((Expression) ASTNode.copySubtree(ast,
                    rightHand));
            rightHand = castExpression;
        } else {
            rightHand = (Expression) ASTNode.copySubtree(ast, rightHand);

            if (isSpecial && type.getName().equals(String.class.getName())
                    && !type.equals(rightHandType)) {
                InfixExpression extraPlus = ast.newInfixExpression();
                extraPlus.setLeftOperand(ast.newStringLiteral());
                extraPlus.setOperator(InfixExpression.Operator.PLUS);
                extraPlus.setRightOperand(rightHand);
                rightHand = extraPlus;
            }
        }

        invocation.arguments().add(rightHand);

        // Set the type of this invocation.
        Type.propagateType(invocation, node);

        // Replace the assignment node with this method invocation.
        replaceNode(node, invocation);

        // Record the field access (a corresponding method will be generated
        // later.
        Hashtable table = (node instanceof Assignment && (((Assignment) node)
                .getOperator() == Assignment.Operator.ASSIGN)) ? _accessedFields
                : _specialAccessedFields;
        _recordField(table, owner.getName(), name.getIdentifier(), indices
                .size());
    }

    /** Handle a class declaration or anonymous class declaration. Records and
     *  assignment methods are added to the declaration.
     *
     *  @param node The AST node of class declaration or anonymous class
     *   declaration.
     *  @param bodyDeclarations The list of body declarations in the class.
     *  @param state The current state of the type analyzer.
     */
    private void _handleDeclaration(ASTNode node, List bodyDeclarations,
            TypeAnalyzerState state) {
        Class currentClass = state.getCurrentClass();
        @SuppressWarnings("unused")
        Class parent = currentClass.getSuperclass();
        List newMethods = new LinkedList();
        List newFields = new LinkedList();
        AST ast = node.getAST();
        CompilationUnit root = (CompilationUnit) node.getRoot();

        List fieldNames = new LinkedList();
        List fieldTypes = new LinkedList();

        // Iterate over all the body declarations.
        Iterator bodyIter = bodyDeclarations.iterator();

        while (bodyIter.hasNext()) {
            Object nextDeclaration = bodyIter.next();

            // Handle only field declarations.
            if (nextDeclaration instanceof FieldDeclaration) {
                FieldDeclaration fieldDecl = (FieldDeclaration) nextDeclaration;
                boolean isStatic = Modifier.isStatic(fieldDecl.getModifiers());

                // If HANDLE_STATIC_FIELDS is set to false, do not refactor
                // static fields.
                if (isStatic && (HANDLE_STATIC_FIELDS != true)) {
                    continue;
                }

                // Handle only private fields or the $CHECKPOINT special field.
                if (Modifier.isPrivate(fieldDecl.getModifiers())) {
                    Type type = Type.getType(fieldDecl);

                    // Iterate over all the fragments in the field declaration.
                    Iterator fragmentIter = fieldDecl.fragments().iterator();

                    while (fragmentIter.hasNext()) {
                        VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragmentIter
                                .next();
                        String fieldName = fragment.getName().getIdentifier();

                        // Get the list of numbers of indices.
                        Hashtable[] tables = new Hashtable[] { _accessedFields,
                                _specialAccessedFields, _backupFields };

                        for (int i = 0; i < tables.length; i++) {
                            List indicesList = _getAccessedField(tables[i],
                                    currentClass.getName(), fieldName);

                            if (indicesList == null) {
                                continue;
                            }

                            // Iterate over all the numbers of indices.
                            Iterator indicesIter = indicesList.iterator();

                            while (indicesIter.hasNext()) {
                                int indices = ((Integer) indicesIter.next())
                                        .intValue();

                                // Create an extra method for every different
                                // number of indices.
                                if (tables[i] == _backupFields) {
                                    newMethods.add(_createBackupMethod(ast,
                                            root, state, fieldName, type,
                                            indices, isStatic));
                                } else {
                                    newMethods
                                            .add(_createAssignMethod(
                                                    ast,
                                                    root,
                                                    state,
                                                    fieldName,
                                                    type,
                                                    indices,
                                                    tables[i] == _specialAccessedFields,
                                                    isStatic));
                                }
                            }
                        }

                        fieldNames.add(fieldName);
                        fieldTypes.add(type);

                        // Create a record field.
                        FieldDeclaration field = _createFieldRecord(ast, root,
                                state, fieldName, type.dimensions(), isStatic);

                        if (field != null) {
                            newFields.add(field);
                        }
                    }
                }
            }
        }

        boolean isInterface = node instanceof TypeDeclaration
                && ((TypeDeclaration) node).isInterface();

        boolean isAnonymous = node instanceof AnonymousClassDeclaration;

        if (isAnonymous) {
            Class[] interfaces = currentClass.getInterfaces();

            for (int i = 0; i < interfaces.length; i++) {
                if (state.getCrossAnalyzedTypes().contains(
                        interfaces[i].getName())) {
                    isAnonymous = false;
                }
            }
        }

        RehandleDeclarationRecord declarationRecord = null;

        if (isAnonymous) {
            Class[] interfaces = currentClass.getInterfaces();

            if (interfaces.length == 1) {
                declarationRecord = new RehandleDeclarationRecord(
                        bodyDeclarations);
                addToLists(_rehandleDeclaration, interfaces[0].getName(),
                        declarationRecord);
            }
        }

        // Do not handle anonymous class declarations in a static method.
        boolean ignore = !_isInStatic.isEmpty()
                && (_isInStatic.peek() == Boolean.TRUE) && isAnonymous;

        // Add an array of all the records.
        if (!isInterface && !ignore) {
            newFields.add(_createRecordArray(ast, root, state, fieldNames));
        }

        // Add a commit method.
        MethodDeclaration commitMethod = null;

        if (!ignore) {
            commitMethod = _createCommitMethod(ast, root, state, fieldNames,
                    fieldTypes, isAnonymous, isInterface);
            newMethods.add(commitMethod);
        }

        if (declarationRecord != null) {
            if (!ignore) {
                declarationRecord._addExtendedDeclaration(commitMethod);
            }

            MethodDeclaration fixedCommitMethod = _createCommitMethod(ast,
                    root, state, fieldNames, fieldTypes, false, isInterface);
            declarationRecord._addFixedDeclaration(fixedCommitMethod);
        }

        // Add a restore method.
        MethodDeclaration restoreMethod = null;

        if (!ignore) {
            restoreMethod = _createRestoreMethod(ast, root, state, fieldNames,
                    fieldTypes, isAnonymous, isInterface);
            newMethods.add(restoreMethod);
        }

        if (declarationRecord != null) {
            if (!ignore) {
                declarationRecord._addExtendedDeclaration(restoreMethod);
            }

            MethodDeclaration fixedRestoreMethod = _createRestoreMethod(ast,
                    root, state, fieldNames, fieldTypes, false, isInterface);
            declarationRecord._addFixedDeclaration(fixedRestoreMethod);
        }

        // Get checkpoint method.
        MethodDeclaration getCheckpoint = null;

        if (!ignore) {
            getCheckpoint = _createGetCheckpointMethod(ast, root, state,
                    isAnonymous, isInterface);

            if (getCheckpoint != null) {
                newMethods.add(getCheckpoint);
            }
        }

        if (declarationRecord != null) {
            if (!ignore) {
                declarationRecord._addExtendedDeclaration(getCheckpoint);
            }

            MethodDeclaration fixedGetCheckpoint = _createGetCheckpointMethod(
                    ast, root, state, false, isInterface);
            declarationRecord._addFixedDeclaration(fixedGetCheckpoint);
        }

        // Set checkpoint method.
        MethodDeclaration setCheckpoint = null;

        if (!ignore) {
            setCheckpoint = _createSetCheckpointMethod(ast, root, state,
                    isAnonymous, isInterface);

            if (setCheckpoint != null) {
                newMethods.add(setCheckpoint);
            }
        }

        if (declarationRecord != null) {
            if (!ignore) {
                declarationRecord._addExtendedDeclaration(setCheckpoint);
            }

            MethodDeclaration fixedSetCheckpoint = _createSetCheckpointMethod(
                    ast, root, state, false, isInterface);
            declarationRecord._addFixedDeclaration(fixedSetCheckpoint);
        }

        // Add an interface.
        if (!ignore) {
            if (isAnonymous) {
                TypeDeclaration proxy = _createProxyClass(ast, root, state);
                bodyDeclarations.add(proxy);

                if (declarationRecord != null) {
                    declarationRecord._addExtendedDeclaration(proxy);
                }
            } else {
                // Set the class to implement Rollbackable.
                if (node instanceof TypeDeclaration) {
                    String rollbackType = getClassName(Rollbackable.class,
                            state, root);
                    ((TypeDeclaration) node).superInterfaceTypes().add(
                            ast.newSimpleType(createName(ast, rollbackType)));
                }

                if (!isInterface) {
                    // Create a checkpoint field.
                    FieldDeclaration checkpointField = _createCheckpointField(
                            ast, root, state);

                    if (checkpointField != null) {
                        bodyDeclarations.add(0, checkpointField);
                    }

                    // Create a record for the checkpoint field.
                    FieldDeclaration record = _createCheckpointRecord(ast,
                            root, state);

                    if (record != null) {
                        newFields.add(0, record);
                    }
                }
            }
        }

        // Add all the methods and then all the fields.
        if (!ignore) {
            bodyDeclarations.addAll(newMethods);
            bodyDeclarations.addAll(newFields);
        } else {
            if (declarationRecord != null) {
                declarationRecord._addFixedDeclarations(newMethods);
                declarationRecord._addFixedDeclarations(newFields);
            }
        }

        if (isAnonymous && !ignore) {
            // Create a simple initializer.
            Initializer initializer = ast.newInitializer();
            Block body = ast.newBlock();
            initializer.setBody(body);

            MethodInvocation addInvocation = ast.newMethodInvocation();
            addInvocation.setExpression(ast.newSimpleName(CHECKPOINT_NAME));
            addInvocation.setName(ast.newSimpleName("addObject"));

            ClassInstanceCreation proxy = ast.newClassInstanceCreation();
            proxy
                    .setType(ast.newSimpleType(ast
                            .newSimpleName(_getProxyName())));
            addInvocation.arguments().add(proxy);
            body.statements().add(ast.newExpressionStatement(addInvocation));
            bodyDeclarations.add(initializer);

            if (declarationRecord != null) {
                declarationRecord._addExtendedDeclaration(initializer);
            }
        }
    }

    /** Test if a method to be added already exists.
     *
     *  @param c The current class.
     *  @param methodName The method name.
     *  @param fieldType The type of the field which the method manages.
     *  @param indices The number of indices.
     *  @param isStatic Whether the field is static.
     *  @param loader The class loader to be used.
     *  @param hasNewValue Whether there is a <tt>newValue</tt> parameter.
     *  @return <tt>true</tt> if the method is already in the class.
     */
    private boolean _isMethodDuplicated(Class c, String methodName,
            Type fieldType, int indices, boolean isStatic, ClassLoader loader,
            boolean hasNewValue) {
        try {
            for (int i = 0; i < indices; i++) {
                fieldType = fieldType.removeOneDimension();
            }

            int nArguments = indices;

            if (hasNewValue) {
                nArguments++;
            }

            if (isStatic) {
                nArguments++;
            }

            Class[] arguments = new Class[nArguments];
            int start = 0;

            if (isStatic) {
                arguments[start++] = Checkpoint.class;
            }

            for (int i = start; i < (nArguments - 1); i++) {
                arguments[i] = int.class;
            }

            if (hasNewValue) {
                arguments[nArguments - 1] = fieldType.toClass(loader);
            }

            try {
                c.getDeclaredMethod(methodName, arguments);
                return true;
            } catch (NoSuchMethodException e) {
                return false;
            }
        } catch (ClassNotFoundException e) {
            throw new ASTClassNotFoundException(fieldType);
        }
    }

    /** Record an accessed field (and possible indices after it) in a list.
     *  Extra methods and fields for these field accesses will be added to
     *  class declarations later when the traversal on the class finishes.
     *
     *  @param table The hash table to be used.
     *  @param className The name of the current class.
     *  @param fieldName The field name.
     *  @param indices The number of indices.
     */
    private void _recordField(
            Hashtable<String, Hashtable<String, List<Integer>>> table,
            String className, String fieldName, int indices) {
        Hashtable<String, List<Integer>> classTable = table.get(className);

        if (classTable == null) {
            classTable = new Hashtable<String, List<Integer>>();
            table.put(className, classTable);
        }

        List<Integer> indicesList = classTable.get(fieldName);

        if (indicesList == null) {
            indicesList = new LinkedList<Integer>();
            classTable.put(fieldName, indicesList);
        }

        Integer iIndices = Integer.valueOf(indices);

        if (!indicesList.contains(iIndices)) {
            indicesList.add(iIndices);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The table of access fields and their indices. Keys are class names;
     *  valus are hash tables. In each table, keys are field names, values
     *  are lists of indices.
     */
    private Hashtable<String, Hashtable<String, List<Integer>>> _accessedFields = new Hashtable<String, Hashtable<String, List<Integer>>>();

    /** The Java operators that modify special types of values.
     */
    private static Hashtable<String, String[]> _assignOperators = new Hashtable<String, String[]>();

    /** The table of backup of fields and their indices. Keys are class names;
     *  values are hash tables. In each table, keys are field names, values
     *  are lists of indices.
     */
    private Hashtable<String, Hashtable<String, List<Integer>>> _backupFields = new Hashtable<String, Hashtable<String, List<Integer>>>();

    /** Keys are names of classes; values are {@link Block} nodes of assign
     *  method bodies. If the classes are cross-analyzed, calls to set
     *  checkpoints need to be added to those blocks.
     */
    private Hashtable<String, List<Block>> _fixSetCheckpoint = new Hashtable<String, List<Block>>();

    /** Whether the analyzer is currently analyzing a static method or a static
     *  field.
     */
    private Stack<Boolean> _isInStatic = new Stack<Boolean>();

    private Hashtable<String, List<NodeReplace>> _nodeSubstitution = new Hashtable<String, List<NodeReplace>>();

    /** Keys are names of classes; values are lists of {@link
     *  RehandleDeclarationRecord} objects. If the classes are cross-analyzed,
     *  declarations of anonymous classes recorded in this table must be fixed.
     */
    private Hashtable<String, List<RehandleDeclarationRecord>> _rehandleDeclaration = new Hashtable<String, List<RehandleDeclarationRecord>>();

    /** The types of values on the right-hand side of the special operators.
     */
    private static Hashtable<String, PrimitiveType.Code> _rightHandTypes = new Hashtable<String, PrimitiveType.Code>();

    /** The table of fields accessed with special assign operators and their
     *  indices. Keys are class names; valus are hash tables. In each value,
     *  keys are field names, values are lists of indices.
     */
    private Hashtable<String, Hashtable<String, List<Integer>>> _specialAccessedFields = new Hashtable<String, Hashtable<String, List<Integer>>>();

    private static class NodeReplace {
        NodeReplace(ASTNode fromNode, ASTNode toNode) {
            _fromNode = fromNode;
            _toNode = toNode;
        }

        ASTNode _getFromNode() {
            return _fromNode;
        }

        ASTNode _getToNode() {
            return _toNode;
        }

        private ASTNode _fromNode;

        private ASTNode _toNode;
    }

    private static class RehandleDeclarationRecord {
        RehandleDeclarationRecord(List<ASTNode> bodyDeclarations) {
            _bodyDeclarations = bodyDeclarations;
        }

        void _addExtendedDeclaration(ASTNode node) {
            _extendedDeclarations.add(node);
        }

        void _addExtendedDeclarations(List<ASTNode> nodes) {
            _extendedDeclarations.addAll(nodes);
        }

        void _addFixedDeclaration(ASTNode node) {
            _fixedDeclarations.add(node);
        }

        void _addFixedDeclarations(List<ASTNode> nodes) {
            _fixedDeclarations.addAll(nodes);
        }

        List<ASTNode> _getBodyDeclarations() {
            return _bodyDeclarations;
        }

        List<ASTNode> _getExtendedDeclarations() {
            return _extendedDeclarations;
        }

        List<ASTNode> _getFixedDeclarations() {
            return _fixedDeclarations;
        }

        private List<ASTNode> _bodyDeclarations;

        private List<ASTNode> _extendedDeclarations = new LinkedList<ASTNode>();

        private List<ASTNode> _fixedDeclarations = new LinkedList<ASTNode>();
    }

    static {
        _assignOperators.put("boolean", new String[] { "&=", "|=", "^=" });
        _assignOperators.put("byte", new String[] { "+=", "-=", "*=", "/=",
                "&=", "|=", "^=", "%=", "<<=", ">>=", ">>>=", "++", "--", "++",
                "--" });
        _assignOperators.put("char", new String[] { "+=", "-=", "*=", "/=",
                "&=", "|=", "^=", "%=", "<<=", ">>=", ">>>=", "++", "--", "++",
                "--" });
        _assignOperators.put("double", new String[] { "+=", "-=", "*=", "/=",
                "%=", "++", "--", "++", "--" });
        _assignOperators.put("float", new String[] { "+=", "-=", "*=", "/=",
                "%=", "++", "--", "++", "--" });
        _assignOperators.put("int", new String[] { "+=", "-=", "*=", "/=",
                "&=", "|=", "^=", "%=", "<<=", ">>=", ">>>=", "++", "--", "++",
                "--" });
        _assignOperators.put("long", new String[] { "+=", "-=", "*=", "/=",
                "&=", "|=", "^=", "%=", "<<=", ">>=", ">>>=", "++", "--", "++",
                "--" });
        _assignOperators.put("short", new String[] { "+=", "-=", "*=", "/=",
                "&=", "|=", "^=", "%=", "<<=", ">>=", ">>>=", "++", "--", "++",
                "--" });

        _rightHandTypes.put("boolean", PrimitiveType.BOOLEAN);
        _rightHandTypes.put("byte", PrimitiveType.LONG);
        _rightHandTypes.put("char", PrimitiveType.LONG);
        _rightHandTypes.put("double", PrimitiveType.DOUBLE);
        _rightHandTypes.put("float", PrimitiveType.DOUBLE);
        _rightHandTypes.put("int", PrimitiveType.LONG);
        _rightHandTypes.put("long", PrimitiveType.LONG);
        _rightHandTypes.put("short", PrimitiveType.LONG);
    }
}
