/* A visitor for parse trees of the expression language that generates soot code.

 Copyright (c) 1998-2008 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.


 */
package ptolemy.copernicus.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.expr.ASTPtArrayConstructNode;
import ptolemy.data.expr.ASTPtBitwiseNode;
import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtFunctionDefinitionNode;
import ptolemy.data.expr.ASTPtFunctionalIfNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtMatrixConstructNode;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.ASTPtPowerNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRecordConstructNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtShiftNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ASTPtUnaryNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.data.expr.CachedMethod;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.data.expr.ParseTreeWriter;
import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.RecordType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Constant;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.util.Chain;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeCodeGenerator

/**
 This class visits parse trees and generates soot instructions that evaluate the parse tree.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeCodeGenerator extends AbstractParseTreeVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public Local generateCode(ASTPtRootNode node, JimpleBody body,
            CodeGenerationScope scope) throws IllegalActionException {
        Stmt insertPoint = Jimple.v().newNopStmt();
        body.getUnits().add(insertPoint);
        return generateCode(node, body, insertPoint, scope);
    }

    public Local generateCode(ASTPtRootNode node, JimpleBody body,
            Unit insertPoint, CodeGenerationScope scope)
            throws IllegalActionException {
        ParseTreeTypeInference typeInference = new ParseTreeTypeInference();
        typeInference.inferTypes(node, scope);
        _scope = scope;
        _body = body;
        _nodeToLocal = new HashMap();
        _units = body.getUnits();
        _insertPoint = insertPoint;
        node.visit(this);

        Local local = (Local) _nodeToLocal.get(node);
        _insertPoint = null;
        _nodeToLocal = null;
        _units = null;
        _scope = null;
        return local;
    }

    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _debug(node);

        _generateAllChildren(node);

        Local local = _getChildTokensLocal(0, node);

        ptolemy.data.type.ArrayType type = (ptolemy.data.type.ArrayType) node
                .getType();
        Local elementTypeLocal = PtolemyUtilities.buildConstantTypeLocal(_body,
                _insertPoint, type.getElementType());

        Local tokenLocal = Jimple.v().newLocal("token",
                RefType.v(PtolemyUtilities.arrayTokenClass));
        _body.getLocals().add(tokenLocal);

        _units.insertBefore(Jimple.v().newAssignStmt(
                tokenLocal,
                Jimple.v().newNewExpr(
                        RefType.v(PtolemyUtilities.arrayTokenClass))),
                _insertPoint);
        _units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(
                        tokenLocal,
                        PtolemyUtilities.arrayTokenWithTypeConstructor
                                .makeRef(), elementTypeLocal, local)),
                _insertPoint);

        _nodeToLocal.put(node, tokenLocal);
    }

    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _debug(node);

        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        Local resultLocal = (Local) _nodeToLocal.get(node.jjtGetChild(0));

        // Make sure that exactly one of AND, OR, XOR is set.
        _assert(node.isBitwiseAnd() ^ node.isBitwiseOr() ^ node.isBitwiseXor(),
                node, "Invalid operation");

        RefType bitwiseType = PtolemyUtilities
                .getSootTypeForTokenType(((ASTPtRootNode) node.jjtGetChild(0))
                        .getType());
        SootClass tokenClass = bitwiseType.getSootClass();

        Local tokenLocal = Jimple.v().newLocal("token", bitwiseType);
        _body.getLocals().add(tokenLocal);

        _units
                .insertBefore(Jimple.v().newAssignStmt(tokenLocal,
                        Jimple.v().newCastExpr(resultLocal, bitwiseType)),
                        _insertPoint);

        for (int i = 1; i < numChildren; i++) {
            Local nextLocal = (Local) _nodeToLocal.get(node.jjtGetChild(i));

            SootMethod method;

            if (node.isBitwiseAnd()) {
                method = SootUtilities.searchForMethodByName(tokenClass,
                        "bitwiseAnd");
            } else if (node.isBitwiseOr()) {
                method = SootUtilities.searchForMethodByName(tokenClass,
                        "bitwiseOr");
            } else if (node.isBitwiseXor()) {
                method = SootUtilities.searchForMethodByName(tokenClass,
                        "bitwiseXor");
            } else {
                throw new RuntimeException("Unrecognized node");
            }

            _units.insertBefore(Jimple.v().newAssignStmt(
                    tokenLocal,
                    Jimple.v().newVirtualInvokeExpr(tokenLocal,
                            method.makeRef(), nextLocal)), _insertPoint);
        }

        Local tokenCastLocal = Jimple.v().newLocal("token",
                PtolemyUtilities.tokenType);
        _body.getLocals().add(tokenCastLocal);
        _units.insertBefore(
                Jimple.v().newAssignStmt(
                        tokenCastLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                PtolemyUtilities.tokenType)), _insertPoint);

        _nodeToLocal.put(node, tokenCastLocal);
    }

    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        _debug(node);

        // First check to see if the name references a valid variable.
        Local local = null;
        String functionName = node.getFunctionName();

        if ((functionName != null) && _isValidName(functionName)) {
            local = _getLocalForName(node.getFunctionName());
        }

        // Method calls are generally not cached...  They are repeated
        // every time the tree is evaluated.
        int numChildren = node.jjtGetNumChildren();
        int argCount = numChildren - 1;

        for (int i = 1; i < numChildren; i++) {
            _generateChild(node, i);
        }

        if ((local != null) || (functionName == null)) {
            Local resultLocal = Jimple.v().newLocal("token",
                    RefType.v(PtolemyUtilities.tokenClass));
            _body.getLocals().add(resultLocal);

            // Evaluate it, if necessary.
            if (local == null) {
                _generateChild(node, 0);
                local = (Local) _nodeToLocal.get(node.jjtGetChild(0));
            }

            ptolemy.data.type.Type type = ((ASTPtRootNode) node.jjtGetChild(0))
                    .getType();

            if (type instanceof ptolemy.data.type.ArrayType) {
                if (argCount == 1) {
                    // array..
                    Local tokenCastLocal = Jimple.v().newLocal("indexToken",
                            RefType.v(PtolemyUtilities.arrayTokenClass));
                    _body.getLocals().add(tokenCastLocal);

                    _units
                            .insertBefore(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    tokenCastLocal,
                                                    Jimple
                                                            .v()
                                                            .newCastExpr(
                                                                    local,
                                                                    RefType
                                                                            .v(PtolemyUtilities.arrayTokenClass))),
                                    _insertPoint);

                    Local indexTokenLocal = (Local) _nodeToLocal.get(node
                            .jjtGetChild(1));
                    _units
                            .insertBefore(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    indexTokenLocal,
                                                    Jimple
                                                            .v()
                                                            .newCastExpr(
                                                                    indexTokenLocal,
                                                                    RefType
                                                                            .v(PtolemyUtilities.intTokenClass))),
                                    _insertPoint);

                    Local indexLocal = Jimple.v()
                            .newLocal("index", IntType.v());
                    _body.getLocals().add(indexLocal);
                    _units.insertBefore(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            indexTokenLocal,
                                            PtolemyUtilities.intValueMethod
                                                    .makeRef())), _insertPoint);

                    _units.insertBefore(Jimple.v().newAssignStmt(
                            resultLocal,
                            Jimple.v().newVirtualInvokeExpr(
                                    tokenCastLocal,
                                    PtolemyUtilities.arrayGetElementMethod
                                            .makeRef(), indexLocal)),
                            _insertPoint);
                } else {
                    //FIXME need better error message when the first child
                    // is, say, an array expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if (type instanceof MatrixType) {
                if (argCount == 2) {
                    // matrix..
                    Local tokenCastLocal = Jimple.v().newLocal("indexToken",
                            RefType.v(PtolemyUtilities.matrixTokenClass));
                    _body.getLocals().add(tokenCastLocal);

                    _units
                            .insertBefore(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    tokenCastLocal,
                                                    Jimple
                                                            .v()
                                                            .newCastExpr(
                                                                    local,
                                                                    RefType
                                                                            .v(PtolemyUtilities.matrixTokenClass))),
                                    _insertPoint);

                    Local rowIndexTokenLocal = (Local) _nodeToLocal.get(node
                            .jjtGetChild(1));
                    Local columnIndexTokenLocal = (Local) _nodeToLocal.get(node
                            .jjtGetChild(2));
                    _units
                            .insertBefore(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    rowIndexTokenLocal,
                                                    Jimple
                                                            .v()
                                                            .newCastExpr(
                                                                    rowIndexTokenLocal,
                                                                    RefType
                                                                            .v(PtolemyUtilities.intTokenClass))),
                                    _insertPoint);
                    _units
                            .insertBefore(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    columnIndexTokenLocal,
                                                    Jimple
                                                            .v()
                                                            .newCastExpr(
                                                                    columnIndexTokenLocal,
                                                                    RefType
                                                                            .v(PtolemyUtilities.intTokenClass))),
                                    _insertPoint);

                    Local rowIndexLocal = Jimple.v().newLocal("rowIndex",
                            IntType.v());
                    _body.getLocals().add(rowIndexLocal);

                    Local columnIndexLocal = Jimple.v().newLocal("columnIndex",
                            IntType.v());
                    _body.getLocals().add(columnIndexLocal);
                    _units.insertBefore(
                            Jimple.v().newAssignStmt(
                                    rowIndexLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            rowIndexTokenLocal,
                                            PtolemyUtilities.intValueMethod
                                                    .makeRef())), _insertPoint);

                    _units.insertBefore(
                            Jimple.v().newAssignStmt(
                                    columnIndexLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            columnIndexTokenLocal,
                                            PtolemyUtilities.intValueMethod
                                                    .makeRef())), _insertPoint);

                    _units
                            .insertBefore(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    resultLocal,
                                                    Jimple
                                                            .v()
                                                            .newVirtualInvokeExpr(
                                                                    tokenCastLocal,
                                                                    PtolemyUtilities.matrixGetElementAsTokenMethod
                                                                            .makeRef(),
                                                                    rowIndexLocal,
                                                                    columnIndexLocal)),
                                    _insertPoint);
                } else {
                    //FIXME need better error message when the first child
                    // is, say, a matrix expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if (type instanceof FunctionType) {
                FunctionType functionType = (FunctionType) type;

                // check number of children against number of arguments of
                // function
                if (functionType.getArgCount() != (numChildren - 1)) {
                    throw new IllegalActionException("Wrong number of "
                            + "arguments when applying function with type "
                            + functionType.toString());
                }

                // Cas the function.
                Local tokenCastLocal = Jimple.v().newLocal("functionToken",
                        RefType.v(PtolemyUtilities.functionTokenClass));
                _body.getLocals().add(tokenCastLocal);

                _units
                        .insertBefore(
                                Jimple
                                        .v()
                                        .newAssignStmt(
                                                tokenCastLocal,
                                                Jimple
                                                        .v()
                                                        .newCastExpr(
                                                                local,
                                                                RefType
                                                                        .v(PtolemyUtilities.functionTokenClass))),
                                _insertPoint);

                // Get the function arguments
                Local argumentsLocal = _getChildTokensLocal(1, node);

                // Invoke the apply method.
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(
                                tokenCastLocal,
                                PtolemyUtilities.functionTokenApplyMethod
                                        .makeRef(), argumentsLocal)),
                        _insertPoint);
            } else {
                throw new IllegalActionException("Wrong number of indices "
                        + "when referencing " + node.getFunctionName()
                        + " with type " + type);
            }

            _nodeToLocal.put(node, resultLocal);
            return;
        }

        if (node.getFunctionName().compareTo("eval") == 0) {
            throw new IllegalActionException("unimplemented case");
        }

        if (node.getFunctionName().compareTo("matlab") == 0) {
            throw new IllegalActionException("unimplemented case");
        }

        // Otherwise, try to reflect the method name.
        // The array of token types that the method takes.
        ptolemy.data.type.Type[] argTypes = new ptolemy.data.type.Type[argCount];

        for (int i = 0; i < argCount; i++) {
            argTypes[i] = ((ASTPtRootNode) node.jjtGetChild(i + 1)).getType();
        }

        // Find the method...
        CachedMethod cachedMethod = CachedMethod.findMethod(node
                .getFunctionName(), argTypes, CachedMethod.FUNCTION);

        if (!cachedMethod.isValid()) {
            throw new IllegalActionException("Function " + cachedMethod
                    + " not found.");
        }

        if (cachedMethod instanceof CachedMethod.BaseConvertCachedMethod
                || cachedMethod instanceof CachedMethod.ArrayMapCachedMethod
                || cachedMethod instanceof CachedMethod.MatrixMapCachedMethod) {
            throw new IllegalActionException(
                    "CodeGeneration not supported for "
                            + cachedMethod.getClass());
        }

        Method method = cachedMethod.getMethod();

        // Find the corresponding soot method.
        SootMethod sootMethod = SootUtilities.getSootMethodForMethod(method);

        // The list of locals that are arguments to the function.
        List args = new LinkedList();

        CachedMethod.ArgumentConversion[] conversions = cachedMethod
                .getConversions();

        for (int i = 0; i < argCount; i++) {
            Local tokenLocal = (Local) _nodeToLocal
                    .get(node.jjtGetChild(i + 1));

            // Insert the appropriate conversion.
            Local argLocal = _convertTokenArgToJavaArg(tokenLocal, argTypes[i],
                    conversions[i]);
            args.add(argLocal);
        }

        Type returnType = sootMethod.getReturnType();
        Local returnLocal = Jimple.v().newLocal("returnValue", returnType);
        _body.getLocals().add(returnLocal);

        // Actually invoke the method.
        _units.insertBefore(Jimple.v().newAssignStmt(returnLocal,
                Jimple.v().newStaticInvokeExpr(sootMethod.makeRef(), args)),
                _insertPoint);

        // Convert the result back to a token.
        Local tokenLocal = _convertJavaResultToToken(returnLocal, returnType);

        //       Jimple.v().newStaticInvokeExpr(
        //                                 methodCallEvaluationMethod,
        //                                 argList)));
        /*
         RefType objectType = RefType.v(PtolemyUtilities.objectClass);
         Local argValuesLocal = Jimple.v().newLocal("tokenArray",
         ArrayType.v(objectType, 1));
         _body.getLocals().add(argValuesLocal);
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         argValuesLocal,
         Jimple.v().newNewArrayExpr(
         objectType,
         IntConstant.v(node.jjtGetNumChildren()))), _insertPoint);

         ptolemy.data.type.Type[] argTypes =
         new ptolemy.data.type.Type[node.jjtGetNumChildren()];
         for (int i = 0; i < node.jjtGetNumChildren(); i++) {
         argTypes[i] = ((ASTPtRootNode)node.jjtGetChild(i)).getType();
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         Jimple.v().newArrayRef(
         argValuesLocal,
         IntConstant.v(i)),
         (Local)_nodeToLocal.get(node.jjtGetChild(i))), _insertPoint);
         }

         RefType typeType = RefType.v("ptolemy.data.type.Type");
         Local argTypesLocal = Jimple.v().newLocal("tokenTypes",
         ArrayType.v(typeType, 1));
         _body.getLocals().add(argTypesLocal);
         Local typeLocal = Jimple.v().newLocal("classType",
         typeType);
         _body.getLocals().add(typeLocal);
         Local tokenLocal = Jimple.v().newLocal("token",
         PtolemyUtilities.tokenType);
         _body.getLocals().add(tokenLocal);

         _units.insertBefore(
         Jimple.v().newAssignStmt(
         argTypesLocal,
         Jimple.v().newNewArrayExpr(
         typeType,
         IntConstant.v(argCount))), _insertPoint);

         Local indexLocal = Jimple.v().newLocal("index", IntType.v());
         _body.getLocals().add(indexLocal);

         // The list of initializer instructions.
         List initializerList = new LinkedList();
         initializerList.add(
         Jimple.v().newAssignStmt(
         indexLocal,
         IntConstant.v(0)), _insertPoint);

         // The list of body instructions.
         List bodyList = new LinkedList();
         bodyList.add(
         Jimple.v().newAssignStmt(
         tokenLocal,
         Jimple.v().newArrayRef(
         argValuesLocal,
         indexLocal)));
         bodyList.add(
         Jimple.v().newAssignStmt(
         tokenLocal,
         Jimple.v().newCastExpr(
         tokenLocal,
         PtolemyUtilities.tokenType)));
         bodyList.add(
         Jimple.v().newAssignStmt(
         typeLocal,
         Jimple.v().newVirtualInvokeExpr(
         tokenLocal,
         PtolemyUtilities.tokenGetTypeMethod.makeRef())));
         bodyList.add(
         Jimple.v().newAssignStmt(
         Jimple.v().newArrayRef(
         argTypesLocal,
         indexLocal),
         typeLocal));


         // Increment the index.
         bodyList.add(
         Jimple.v().newAssignStmt(
         indexLocal,
         Jimple.v().newAddExpr(
         indexLocal,
         IntConstant.v(1))));

         Expr conditionalExpr =
         Jimple.v().newLtExpr(
         indexLocal,
         IntConstant.v(argCount));

         Stmt stmt = Jimple.v().newNopStmt();
         _units.insertBefore(stmt);

         SootUtilities.createForLoopBefore(_body,
         stmt,
         initializerList,
         bodyList,
         conditionalExpr);

         SootMethod methodCallEvaluationMethod =
         Scene.v().getMethod("<ptolemy.data.expr.ParseTreeEvaluator: ptolemy.data.Token functionCall(java.lang.String,int,ptolemy.data.type.Type[],java.lang.Object[])>");
         List argList = new ArrayList();
         argList.add(StringConstant.v(node.getFunctionName()));
         argList.add(IntConstant.v(argCount));
         argList.add(argTypesLocal);
         argList.add(argValuesLocal);

         _units.insertBefore(
         Jimple.v().newAssignStmt(
         tokenLocal,
         Jimple.v().newStaticInvokeExpr(
         methodCallEvaluationMethod.makeRef(),
         argList)));

         */
        _nodeToLocal.put(node, tokenLocal);
    }

    // Add code to the method being generated to convert the given
    // returnLocal, with the given returnType to a token type.  Return
    // the new token local.
    private Local _convertJavaResultToToken(Local returnLocal, Type returnType)
            throws IllegalActionException {
        Local tokenLocal;

        if (returnType instanceof RefType
                && SootUtilities.derivesFrom(((RefType) returnType)
                        .getSootClass(), PtolemyUtilities.tokenClass)) {
            tokenLocal = returnLocal;
        } else if (returnType
                .equals(ArrayType.v(PtolemyUtilities.tokenType, 1))) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.arrayTokenClass,
                    PtolemyUtilities.arrayTokenConstructor, returnLocal);
        } else if (returnType.equals(IntType.v())) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.intTokenClass,
                    PtolemyUtilities.intTokenConstructor, returnLocal);
        } else if (returnType.equals(DoubleType.v())) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.doubleTokenClass,
                    PtolemyUtilities.doubleTokenConstructor, returnLocal);
        } else if (returnType.equals(FloatType.v())) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.floatTokenClass,
                    PtolemyUtilities.floatTokenConstructor, returnLocal);
        } else if (returnType.equals(ShortType.v())) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.shortTokenClass,
                    PtolemyUtilities.shortTokenConstructor, returnLocal);
        } else if (returnType.equals(LongType.v())) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.longTokenClass,
                    PtolemyUtilities.longTokenConstructor, returnLocal);
        } else if (returnType.equals(RefType.v("java.lang.String"))) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.stringTokenClass,
                    PtolemyUtilities.stringTokenConstructor, returnLocal);
        } else if (returnType.equals(BooleanType.v())) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.booleanTokenClass,
                    PtolemyUtilities.booleanTokenConstructor, returnLocal);
        } else if (returnType.equals(RefType.v("ptolemy.math.Complex"))) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.complexTokenClass,
                    PtolemyUtilities.complexTokenConstructor, returnLocal);
        } else if (returnType.equals(RefType.v("ptolemy.math.FixPoint"))) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token", PtolemyUtilities.fixTokenClass,
                    PtolemyUtilities.fixTokenConstructor, returnLocal);
        } else if (returnType.equals(ArrayType.v(BooleanType.v(), 2))) {
            tokenLocal = PtolemyUtilities
                    .addTokenLocalBefore(_body, _insertPoint, "token",
                            PtolemyUtilities.booleanMatrixTokenClass,
                            PtolemyUtilities.booleanMatrixTokenConstructor,
                            returnLocal);
        } else if (returnType.equals(ArrayType.v(IntType.v(), 2))) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token",
                    PtolemyUtilities.intMatrixTokenClass,
                    PtolemyUtilities.intMatrixTokenConstructor, returnLocal);
        } else if (returnType.equals(ArrayType.v(LongType.v(), 2))) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token",
                    PtolemyUtilities.doubleMatrixTokenClass,
                    PtolemyUtilities.doubleMatrixTokenConstructor, returnLocal);
        } else if (returnType.equals(ArrayType.v(DoubleType.v(), 2))) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token",
                    PtolemyUtilities.doubleMatrixTokenClass,
                    PtolemyUtilities.doubleMatrixTokenConstructor, returnLocal);
        } else if (returnType.equals(ArrayType.v(RefType
                .v("ptolemy.math.Complex"), 2))) {
            tokenLocal = PtolemyUtilities
                    .addTokenLocalBefore(_body, _insertPoint, "token",
                            PtolemyUtilities.complexMatrixTokenClass,
                            PtolemyUtilities.complexMatrixTokenConstructor,
                            returnLocal);
        } else if (returnType.equals(ArrayType.v(RefType
                .v("ptolemy.math.FixPoint"), 2))) {
            tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                    _insertPoint, "token",
                    PtolemyUtilities.fixMatrixTokenClass,
                    PtolemyUtilities.fixMatrixTokenConstructor, returnLocal);
        } else {
            throw new IllegalActionException("unrecognized case: " + returnType);
        }

        return tokenLocal;
    }

    private Local _convertTokenArgToJavaArg(Local tokenLocal,
            ptolemy.data.type.Type tokenType,
            CachedMethod.ArgumentConversion conversion)
            throws IllegalActionException {
        if (conversion == CachedMethod.IDENTITY_CONVERSION) {
            Local tempLocal = Jimple.v().newLocal("arg",
                    PtolemyUtilities.tokenType);
            _body.getLocals().add(tempLocal);

            // Add the new local to the list of arguments
            _units.insertBefore(
                    Jimple.v().newAssignStmt(tempLocal, tokenLocal),
                    _insertPoint);
            return tempLocal;
        } else if (conversion == CachedMethod.ARRAYTOKEN_CONVERSION) {
            Local tempLocal = Jimple.v().newLocal("arg",
                    RefType.v(PtolemyUtilities.arrayTokenClass));
            _body.getLocals().add(tempLocal);

            Local resultLocal = Jimple.v().newLocal("arg",
                    ArrayType.v(RefType.v(PtolemyUtilities.objectClass), 1));
            _body.getLocals().add(resultLocal);

            // Add the new local to the list of arguments
            _units.insertBefore(Jimple.v().newAssignStmt(
                    tempLocal,
                    Jimple.v().newCastExpr(tokenLocal,
                            RefType.v(PtolemyUtilities.arrayTokenClass))),
                    _insertPoint);
            _units.insertBefore(Jimple.v().newAssignStmt(
                    resultLocal,
                    Jimple.v().newVirtualInvokeExpr(tempLocal,
                            PtolemyUtilities.arrayValueMethod.makeRef())),
                    _insertPoint);
            return resultLocal;
        } else if (conversion == CachedMethod.NATIVE_CONVERSION) {
            if (tokenType == ptolemy.data.type.BaseType.DOUBLE) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.doubleTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg", DoubleType.v());
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v(PtolemyUtilities.doubleTokenClass))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.doubleValueMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.FLOAT) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.floatTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg", FloatType.v());
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v(PtolemyUtilities.floatTokenClass))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.floatValueMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.SHORT) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.shortTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg", ShortType.v());
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.ShortToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.shortValueMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.UNSIGNED_BYTE) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.unsignedByteTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg", ByteType.v());
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units
                        .insertBefore(
                                Jimple
                                        .v()
                                        .newAssignStmt(
                                                tempLocal,
                                                Jimple
                                                        .v()
                                                        .newCastExpr(
                                                                tokenLocal,
                                                                RefType
                                                                        .v(PtolemyUtilities.unsignedByteTokenClass))),
                                _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(
                                tempLocal,
                                PtolemyUtilities.unsignedByteValueMethod
                                        .makeRef())), _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.INT) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.intTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg", IntType.v());
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.IntToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.intValueMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.LONG) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.longTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg", LongType.v());
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.LongToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.longValueMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.STRING) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.stringTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg",
                        RefType.v("java.lang.String"));
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.StringToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.stringValueMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.BOOLEAN) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.booleanTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg", BooleanType.v());
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.BooleanToken"))),
                        _insertPoint);
                _units.insertBefore(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.booleanValueMethod
                                                .makeRef())), _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.COMPLEX) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.complexTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg",
                        RefType.v("ptolemy.math.Complex"));
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.ComplexToken"))),
                        _insertPoint);
                _units.insertBefore(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.complexValueMethod
                                                .makeRef())), _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.FIX) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.intTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg",
                        RefType.v("ptolemy.math.FixPoint"));
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.FixToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.fixValueMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.DOUBLE_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.doubleMatrixTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg",
                        ArrayType.v(DoubleType.v(), 2));
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.DoubleMatrixToken"))),
                        _insertPoint);
                _units.insertBefore(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.doubleMatrixMethod
                                                .makeRef())), _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.INT_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.intMatrixTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg",
                        ArrayType.v(IntType.v(), 2));
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.IntMatrixToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.intMatrixMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.LONG_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.longMatrixTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg",
                        ArrayType.v(LongType.v(), 2));
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.LongMatrixToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        resultLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.longMatrixMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.BOOLEAN_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.booleanMatrixTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg",
                        ArrayType.v(BooleanType.v(), 2));
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.BooleanMatrixToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v()
                        .newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.booleanMatrixMethod
                                                .makeRef())), _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.COMPLEX_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.complexMatrixTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg",
                        ArrayType.v(RefType.v("ptolemy.math.Complex"), 2));
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.ComplexMatrixToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v()
                        .newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.complexMatrixMethod
                                                .makeRef())), _insertPoint);
                return resultLocal;
            } else if (tokenType == ptolemy.data.type.BaseType.FIX_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg",
                        RefType.v(PtolemyUtilities.fixMatrixTokenClass));
                _body.getLocals().add(tempLocal);

                Local resultLocal = Jimple.v().newLocal("arg",
                        ArrayType.v(RefType.v("ptolemy.math.FixPoint"), 2));
                _body.getLocals().add(resultLocal);

                // Add the new local to the list of arguments
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newCastExpr(tokenLocal,
                                RefType.v("ptolemy.data.FixMatrixToken"))),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tempLocal,
                        Jimple.v().newVirtualInvokeExpr(tempLocal,
                                PtolemyUtilities.fixMatrixMethod.makeRef())),
                        _insertPoint);
                return resultLocal;
            } else {
                throw new IllegalActionException(
                        "Code generation not supported for native "
                                + "conversion of type " + tokenType);
            }
        } else {
            throw new IllegalActionException(
                    "CodeGeneration not supported for argument "
                            + "conversion " + conversion);
        }
    }

    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        _debug(node);

        // Note that we take care to have short-circuit evaluation here.
        _generateChild(node, 0);

        Local conditionTokenLocal = (Local) _nodeToLocal.get(node
                .jjtGetChild(0));

        Local booleanTokenLocal = Jimple.v().newLocal("result",
                RefType.v(PtolemyUtilities.booleanTokenClass));
        _body.getLocals().add(booleanTokenLocal);

        Local flagLocal = Jimple.v().newLocal("result", BooleanType.v());
        _body.getLocals().add(flagLocal);

        Local resultLocal = Jimple.v().newLocal("result",
                PtolemyUtilities.tokenType);
        _body.getLocals().add(resultLocal);

        Stmt startTrue = Jimple.v().newNopStmt();
        Stmt endTrue = Jimple.v().newNopStmt();

        // Check the condition
        _units.insertBefore(Jimple.v().newAssignStmt(
                booleanTokenLocal,
                Jimple.v().newCastExpr(conditionTokenLocal,
                        RefType.v(PtolemyUtilities.booleanTokenClass))),
                _insertPoint);
        _units.insertBefore(Jimple.v().newAssignStmt(
                flagLocal,
                Jimple.v().newVirtualInvokeExpr(booleanTokenLocal,
                        PtolemyUtilities.booleanValueMethod.makeRef())),
                _insertPoint);

        // If condition is true then skip to start of true branch.
        _units.insertBefore(Jimple.v().newIfStmt(
                Jimple.v().newEqExpr(flagLocal, IntConstant.v(1)), startTrue),
                _insertPoint);

        // Otherwise, do the false branch,
        _generateChild(node, 2);

        // Assign the false result
        _units.insertBefore(Jimple.v().newAssignStmt(resultLocal,
                (Local) _nodeToLocal.get(node.jjtGetChild(2))), _insertPoint);

        // And continue on.
        _units.insertBefore(Jimple.v().newGotoStmt(endTrue), _insertPoint);

        _units.insertBefore(startTrue, _insertPoint);

        // Otherwise, do the true branch,
        _generateChild(node, 1);

        // Assign the true result
        _units.insertBefore(Jimple.v().newAssignStmt(resultLocal,
                (Local) _nodeToLocal.get(node.jjtGetChild(1))), _insertPoint);
        _units.insertBefore(endTrue, _insertPoint);

        _nodeToLocal.put(node, resultLocal);
    }

    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        _debug(node);

        // Get the free variables of the function definition.  These
        // will be turned into constructor arguments of the class
        // representing the function closure.  This takes the role of
        // Specialization of parse trees.
        ParseTreeFreeVariableCollector collector = new ParseTreeFreeVariableCollector();
        List freeVariableList = new LinkedList(collector
                .collectFreeVariables(node));
        Collections.sort(freeVariableList);
        System.out.println("freeVariableList = " + freeVariableList);

        // Infer the type of the function.
        ParseTreeTypeInference inference = new ParseTreeTypeInference();
        inference.inferTypes(node, _scope);

        FunctionType functionType = (FunctionType) node.getType();

        ASTPtRootNode cloneTree = node.getExpressionTree(); //.clone();

        //         ParseTreeSpecializer specializer = new ParseTreeSpecializer();
        //         cloneTree = specializer.specialize(node.getExpressionTree(),
        //                 node.getArgumentNameList(), _scope);
        // Generate a new Function class
        SootClass functionClass = new SootClass("Function_"
                + (_functionCount++), Modifier.PUBLIC);
        Scene.v().addClass(functionClass);
        functionClass.setSuperclass(PtolemyUtilities.objectClass);
        functionClass.setApplicationClass();
        functionClass.addInterface(PtolemyUtilities.functionInterface);

        // Construct the scope, starting with free variables that are
        // represented by constructor arguments and stored in fields
        // of the closure.
        Map nameToLocal = new HashMap();
        Map nameToType = new HashMap();

        // A list of types corresponding to the Soot type for every
        // freeVariable
        List argumentSootTypes = new LinkedList();

        for (Iterator freeVariables = freeVariableList.iterator(); freeVariables
                .hasNext();) {
            String freeVariable = (String) freeVariables.next();
            ptolemy.data.type.Type type = _scope.getType(freeVariable);
            Type sootType = PtolemyUtilities.getSootTypeForTokenType(type);
            SootField field = new SootField(freeVariable, sootType,
                    Modifier.PUBLIC);
            functionClass.addField(field);
            argumentSootTypes.add(sootType);
            nameToLocal.put(freeVariable, field);
            nameToType.put(freeVariable, type);
        }

        // Create the constructor.  The constructor takes one argument
        // for every free variable of the function closure that is not
        // bound to a formal argument.
        SootMethod functionConstructor = new SootMethod("<init>",
                argumentSootTypes, VoidType.v(), Modifier.PUBLIC);
        functionClass.addMethod(functionConstructor);

        {
            JimpleBody body = Jimple.v().newBody(functionConstructor);
            functionConstructor.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(
                    Jimple.v()
                            .newInvokeStmt(
                                    Jimple.v().newSpecialInvokeExpr(
                                            body.getThisLocal(),
                                            PtolemyUtilities.objectConstructor
                                                    .makeRef(),
                                            Collections.EMPTY_LIST)));

            // Read the parameters to the closure constructor.
            int i = 0;

            for (Iterator freeVariables = freeVariableList.iterator(); freeVariables
                    .hasNext();) {
                String freeVariable = (String) freeVariables.next();
                Local local = body.getParameterLocal(i);
                SootField field = (SootField) nameToLocal.get(freeVariable);
                body.getUnits().add(
                        Jimple.v().newAssignStmt(
                                Jimple.v().newInstanceFieldRef(
                                        body.getThisLocal(), field.makeRef()),
                                local));
                i++;
            }

            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        // Create the apply method.
        {
            List argTypes = new LinkedList();
            argTypes.add(ArrayType.v(PtolemyUtilities.tokenType, 1));

            SootMethod functionApplyMethod = new SootMethod("apply", argTypes,
                    PtolemyUtilities.tokenType, Modifier.PUBLIC);
            functionClass.addMethod(functionApplyMethod);

            JimpleBody body = Jimple.v().newBody(functionApplyMethod);
            functionApplyMethod.setActiveBody(body);
            body.insertIdentityStmts();

            Stmt insertPoint = Jimple.v().newNopStmt();
            body.getUnits().add(insertPoint);

            // Loop over all the arguments and populate the map from
            // identifier to local.
            List list = node.getArgumentNameList();
            Local paramLocal = body.getParameterLocal(0);
            int count = 0;

            for (Iterator names = list.iterator(); names.hasNext(); count++) {
                String name = (String) names.next();
                Local argLocal = Jimple.v().newLocal("argument_" + name,
                        RefType.v(PtolemyUtilities.tokenClass));
                body.getLocals().add(argLocal);
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                argLocal,
                                Jimple.v().newArrayRef(paramLocal,
                                        IntConstant.v(count))), insertPoint);
                nameToLocal.put(name, argLocal);
                nameToType.put(name, node.getArgumentTypes()[count]);
            }

            // FIXME: bound arguments?
            DataUtilities.ActorCodeGenerationScope scope = new DataUtilities.ActorCodeGenerationScope(
                    null, null, nameToLocal, nameToType, body, insertPoint);
            ParseTreeCodeGenerator generator = new ParseTreeCodeGenerator();
            Local local = generator.generateCode(cloneTree, body, insertPoint,
                    scope);

            body.getUnits().add(Jimple.v().newReturnStmt(local));
        }
        // Create the getNumberOfArguments method.
        {
            List argTypes = new LinkedList();

            SootMethod getNumberOfArgumentsMethod = new SootMethod(
                    "getNumberOfArguments", argTypes, IntType.v(),
                    Modifier.PUBLIC);
            functionClass.addMethod(getNumberOfArgumentsMethod);

            JimpleBody body = Jimple.v().newBody(getNumberOfArgumentsMethod);
            getNumberOfArgumentsMethod.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(
                    Jimple.v().newReturnStmt(
                            IntConstant.v(functionType.getArgCount())));
        }
        // Create the isCongruent method.
        {
            List argTypes = new LinkedList();
            argTypes.add(RefType.v(PtolemyUtilities.functionInterface));

            SootMethod isCongruentMethod = new SootMethod("isCongruent",
                    argTypes, BooleanType.v(), Modifier.PUBLIC);
            functionClass.addMethod(isCongruentMethod);

            JimpleBody body = Jimple.v().newBody(isCongruentMethod);
            isCongruentMethod.setActiveBody(body);
            body.insertIdentityStmts();

            // Never congruent
            // FIXME: really this should compare the string values.
            body.getUnits().add(Jimple.v().newReturnStmt(IntConstant.v(0)));
        }
        // Create the toString method.
        {
            SootMethod toStringMethod = new SootMethod("toString",
                    Collections.EMPTY_LIST, RefType.v("java.lang.String"),
                    Modifier.PUBLIC);
            functionClass.addMethod(toStringMethod);

            JimpleBody body = Jimple.v().newBody(toStringMethod);
            toStringMethod.setActiveBody(body);
            body.insertIdentityStmts();

            StringBuffer buffer = new StringBuffer("(function(");
            int n = node.getArgumentNameList().size();

            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }

                buffer.append((String) node.getArgumentNameList().get(i));

                ptolemy.data.type.Type argType = node.getArgumentTypes()[i];

                if (argType != BaseType.GENERAL) {
                    buffer.append(":");
                    buffer.append(argType.toString());
                }
            }

            buffer.append(") ");

            // FIXME: really this should include runtime references to f and g.
            ParseTreeWriter writer = new ParseTreeWriter();
            String string = writer.printParseTree(node.getExpressionTree());
            buffer.append(string);
            buffer.append(")");

            body.getUnits().add(
                    Jimple.v().newReturnStmt(
                            StringConstant.v(buffer.toString())));
        }

        // Now instantiate the function class.
        Local resultFunctionLocal = Jimple.v().newLocal("resultFunction",
                RefType.v(PtolemyUtilities.functionTokenClass));
        _body.getLocals().add(resultFunctionLocal);

        // Build a list of args for the instantiation.
        List argList = new ArrayList();

        for (Iterator freeVariables = freeVariableList.iterator(); freeVariables
                .hasNext();) {
            String freeVariable = (String) freeVariables.next();
            Local local = _scope.getLocal(freeVariable);
            argList.add(local);
        }

        _units.insertBefore(Jimple.v().newAssignStmt(resultFunctionLocal,
                Jimple.v().newNewExpr(RefType.v(functionClass))), _insertPoint);
        _units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(resultFunctionLocal,
                        functionConstructor.makeRef(), argList)), _insertPoint);

        // The type of the function being created.
        Local resultTypeLocal = PtolemyUtilities.buildConstantTypeLocal(_body,
                _insertPoint, functionType);
        Local resultLocal = Jimple.v().newLocal("result",
                PtolemyUtilities.tokenType);
        _body.getLocals().add(resultLocal);

        // Lastly, wrap it in a token.
        _units.insertBefore(Jimple.v().newAssignStmt(
                resultLocal,
                Jimple.v().newNewExpr(
                        RefType.v(PtolemyUtilities.functionTokenClass))),
                _insertPoint);
        _units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(resultLocal,
                        PtolemyUtilities.functionTokenConstructor.makeRef(),
                        resultFunctionLocal, resultTypeLocal)), _insertPoint);

        _nodeToLocal.put(node, resultLocal);

        //         throw new IllegalActionException("Cannot generate code" +
        //                 " for function definitions!");
    }

    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        _debug(node);

        if (node.isConstant() && node.isEvaluated()) {
            Stmt insertPoint = Jimple.v().newNopStmt();
            _units.insertBefore(insertPoint, _insertPoint);

            Local local = PtolemyUtilities.buildConstantTokenLocal(_body,
                    insertPoint, node.getToken(), "token");
            _nodeToLocal.put(node, local);
            return;
        }

        Local local;

        try {
            local = _getLocalForName(node.getName());
        } catch (IllegalActionException ex) {
            // Must be a constant.  FIXME: Catching the exception is a
            // lousy way to figure this out.
            // Look up for constants.
            if (ptolemy.data.expr.Constants.get(node.getName()) != null) {
                //System.err.println("tested!");
                // A named constant that is recognized by the parser.
                Stmt insertPoint = Jimple.v().newNopStmt();
                _units.insertBefore(insertPoint, _insertPoint);
                local = PtolemyUtilities.buildConstantTokenLocal(_body,
                        insertPoint, ptolemy.data.expr.Constants.get(node
                                .getName()), "token");
            } else {
                throw ex;
            }
        }

        _nodeToLocal.put(node, local);
    }

    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _debug(node);

        int numChildren = node.jjtGetNumChildren();

        // Note that we take care to have short-circuit evaluation here.
        Local conditionLocal = Jimple.v()
                .newLocal("condition", BooleanType.v());
        _body.getLocals().add(conditionLocal);

        Local booleanTokenLocal = Jimple.v().newLocal("result",
                RefType.v(PtolemyUtilities.booleanTokenClass));
        _body.getLocals().add(booleanTokenLocal);

        Local flagLocal = Jimple.v().newLocal("result", BooleanType.v());
        _body.getLocals().add(flagLocal);

        Stmt failedStmt = Jimple.v().newNopStmt();
        Stmt satisfiedStmt = Jimple.v().newNopStmt();

        // Determine if we are doing AND or OR
        Constant conditionConstant = node.isLogicalAnd() ? IntConstant.v(1)
                : IntConstant.v(0);
        _units.insertBefore(Jimple.v().newAssignStmt(conditionLocal,
                conditionConstant), _insertPoint);

        for (int i = 0; i < numChildren; i++) {
            _generateChild(node, i);

            Local childLocal = (Local) _nodeToLocal.get(node.jjtGetChild(i));

            // Check the condition
            _units.insertBefore(Jimple.v().newAssignStmt(
                    booleanTokenLocal,
                    Jimple.v().newCastExpr(childLocal,
                            RefType.v(PtolemyUtilities.booleanTokenClass))),
                    _insertPoint);
            _units.insertBefore(Jimple.v().newAssignStmt(
                    flagLocal,
                    Jimple.v().newVirtualInvokeExpr(booleanTokenLocal,
                            PtolemyUtilities.booleanValueMethod.makeRef())),
                    _insertPoint);

            // If condition is true then skip to start of true branch.
            _units.insertBefore(Jimple.v().newIfStmt(
                    Jimple.v().newNeExpr(flagLocal, conditionConstant),
                    failedStmt), _insertPoint);
        }

        // If we fall through, then must be satisfied.
        _units
                .insertBefore(Jimple.v().newGotoStmt(satisfiedStmt),
                        _insertPoint);
        _units.insertBefore(failedStmt, _insertPoint);

        Constant notConditionConstant = node.isLogicalAnd() ? IntConstant.v(0)
                : IntConstant.v(1);
        _units.insertBefore(Jimple.v().newAssignStmt(conditionLocal,
                notConditionConstant), _insertPoint);

        _units.insertBefore(satisfiedStmt, _insertPoint);

        // Take the result and turn it back into a BooleanToken
        Local resultLocal = PtolemyUtilities.addTokenLocalBefore(_body,
                _insertPoint, "token", PtolemyUtilities.booleanTokenClass,
                PtolemyUtilities.booleanTokenConstructor, conditionLocal);

        _nodeToLocal.put(node, resultLocal);
    }

    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        _debug(node);

        _generateAllChildren(node);

        Local resultLocal = Jimple.v().newLocal("tokenResult",
                PtolemyUtilities.tokenType);
        _body.getLocals().add(resultLocal);

        if (node.getForm() == 1) {
            Local local = _getChildTokensLocal(0, node);
            List args = new LinkedList();
            args.add(local);
            args.add(IntConstant.v(node.getRowCount()));
            args.add(IntConstant.v(node.getColumnCount()));

            ptolemy.data.type.Type type = node.getType();

            if (type == BaseType.UNKNOWN) {
                throw new IllegalActionException("Cannot resolve type for "
                        + "matrix construction.");
            } else if (type == BaseType.BOOLEAN_MATRIX) {
                _createMatrix(resultLocal,
                        PtolemyUtilities.booleanMatrixTokenClass,
                        PtolemyUtilities.booleanMatrixTokenArrayConstructor,
                        args);
            } else if (type == BaseType.INT_MATRIX) {
                _createMatrix(resultLocal,
                        PtolemyUtilities.intMatrixTokenClass,
                        PtolemyUtilities.intMatrixTokenArrayConstructor, args);
            } else if (type == BaseType.LONG_MATRIX) {
                _createMatrix(resultLocal,
                        PtolemyUtilities.longMatrixTokenClass,
                        PtolemyUtilities.longMatrixTokenArrayConstructor, args);
            } else if (type == BaseType.DOUBLE_MATRIX) {
                _createMatrix(resultLocal,
                        PtolemyUtilities.doubleMatrixTokenClass,
                        PtolemyUtilities.doubleMatrixTokenArrayConstructor,
                        args);
            } else if (type == BaseType.COMPLEX_MATRIX) {
                _createMatrix(resultLocal,
                        PtolemyUtilities.complexMatrixTokenClass,
                        PtolemyUtilities.complexMatrixTokenArrayConstructor,
                        args);
            } else if (type == BaseType.FIX_MATRIX) {
                _createMatrix(resultLocal,
                        PtolemyUtilities.fixMatrixTokenClass,
                        PtolemyUtilities.fixMatrixTokenArrayConstructor, args);
            } else {
                throw new IllegalActionException("Unrecognized type " + type
                        + " for matrix creation.");
            }

            // This would be ideal, but it uses leastUpperBound(array), which is hard to handle.
            //             _units.insertBefore(
            //                     Jimple.v().newAssignStmt(
            //                             resultLocal,
            //                             Jimple.v().newStaticInvokeExpr(
            //                                     PtolemyUtilities.matrixTokenCreateMethod,
            //                                     args)), _insertPoint);
        } else {
            throw new IllegalActionException("unimplemented case");
        }

        _nodeToLocal.put(node, resultLocal);
    }

    private void _createMatrix(Local resultLocal, SootClass matrixClass,
            SootMethod matrixArrayConstructor, List args) {
        _units.insertBefore(Jimple.v().newAssignStmt(resultLocal,
                Jimple.v().newNewExpr(RefType.v(matrixClass))), _insertPoint);

        _units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(resultLocal,
                        matrixArrayConstructor.makeRef(), args)), _insertPoint);
        return;
    }

    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _debug(node);

        // Method calls are generally not cached...  They are repeated
        // every time the tree is evaluated.
        int argCount = node.jjtGetNumChildren();
        _generateAllChildren(node);

        // The first child is the token on which to invoke the method.
        // Handle indexing into a record.
        ptolemy.data.type.Type baseTokenType = ((ASTPtRootNode) node
                .jjtGetChild(0)).getType();

        if ((argCount == 1) && baseTokenType instanceof RecordType) {
            RecordType type = (RecordType) baseTokenType;

            if (type.labelSet().contains(node.getMethodName())) {
                Local originalBaseLocal = (Local) _nodeToLocal.get(node
                        .jjtGetChild(0));
                Local baseLocal = Jimple.v().newLocal("base",
                        RefType.v(PtolemyUtilities.recordTokenClass));
                _body.getLocals().add(baseLocal);

                // Cast the record.
                _units.insertBefore(Jimple.v().newAssignStmt(
                        baseLocal,
                        Jimple.v().newCastExpr(originalBaseLocal,
                                RefType.v(PtolemyUtilities.recordTokenClass))),
                        _insertPoint);

                // invoke get()
                Local returnLocal = Jimple.v().newLocal("returnValue",
                        RefType.v(PtolemyUtilities.tokenClass));
                _body.getLocals().add(returnLocal);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        returnLocal,
                        Jimple.v().newVirtualInvokeExpr(baseLocal,
                                PtolemyUtilities.recordGetMethod.makeRef(),
                                StringConstant.v(node.getMethodName()))),
                        _insertPoint);
                _nodeToLocal.put(node, returnLocal);

                return;
            }
        }

        // The array of token types that the method takes.
        ptolemy.data.type.Type[] argTypes = new ptolemy.data.type.Type[node
                .jjtGetNumChildren()];

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            argTypes[i] = ((ASTPtRootNode) node.jjtGetChild(i)).getType();
        }

        // Find the method...
        CachedMethod cachedMethod = CachedMethod.findMethod(node
                .getMethodName(), argTypes, CachedMethod.METHOD);

        if (!cachedMethod.isValid()) {
            throw new IllegalActionException("Function " + cachedMethod
                    + " not found.");
        }

        if (cachedMethod instanceof CachedMethod.ArrayMapCachedMethod
                || cachedMethod instanceof CachedMethod.MatrixMapCachedMethod) {
            throw new IllegalActionException(
                    "CodeGeneration not supported for "
                            + cachedMethod.getClass());
        }

        Method method = cachedMethod.getMethod();

        // Find the corresponding soot method.
        SootMethod sootMethod = SootUtilities.getSootMethodForMethod(method);

        //     System.out.println("generating code to invoke " + sootMethod);
        Local originalBaseLocal = (Local) _nodeToLocal.get(node.jjtGetChild(0));
        RefType baseType = RefType.v(sootMethod.getDeclaringClass());
        Local baseLocal = Jimple.v().newLocal("base", baseType);
        _body.getLocals().add(baseLocal);

        if (cachedMethod instanceof CachedMethod.BaseConvertCachedMethod) {
            /*RefType tempBaseType =*/PtolemyUtilities
                    .getSootTypeForTokenType(argTypes[0]);
            Local tempBaseLocal = _convertTokenArgToJavaArg(originalBaseLocal,
                    argTypes[0],
                    ((CachedMethod.BaseConvertCachedMethod) cachedMethod)
                            .getBaseConversion());
            _units.insertBefore(Jimple.v().newAssignStmt(baseLocal,
                    Jimple.v().newCastExpr(tempBaseLocal, baseType)),
                    _insertPoint);
        } else {
            _units.insertBefore(Jimple.v().newAssignStmt(baseLocal,
                    Jimple.v().newCastExpr(originalBaseLocal, baseType)),
                    _insertPoint);
        }

        // The list of locals that are arguments to the function.
        List args = new LinkedList();

        CachedMethod.ArgumentConversion[] conversions = cachedMethod
                .getConversions();

        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            Local tokenLocal = (Local) _nodeToLocal.get(node.jjtGetChild(i));

            //  System.out.println("converting argument " + conversions[i-1]);
            // Insert the appropriate conversion.
            Local argLocal = _convertTokenArgToJavaArg(tokenLocal, argTypes[i],
                    conversions[i - 1]);
            args.add(argLocal);
        }

        Type returnType = sootMethod.getReturnType();
        Local returnLocal = Jimple.v().newLocal("returnValue", returnType);
        _body.getLocals().add(returnLocal);

        // Actually invoke the method.
        _units.insertBefore(Jimple.v().newAssignStmt(
                returnLocal,
                Jimple.v().newVirtualInvokeExpr(baseLocal,
                        sootMethod.makeRef(), args)), _insertPoint);

        // Convert the result back to a token.
        Local tokenLocal = _convertJavaResultToToken(returnLocal, returnType);

        //      RefType objectType = RefType.v(PtolemyUtilities.objectClass);
        //         Local argValuesLocal = Jimple.v().newLocal("tokenArray",
        //                 ArrayType.v(objectType, 1));
        //         _body.getLocals().add(argValuesLocal);
        //         _units.insertBefore(
        //                 Jimple.v().newAssignStmt(
        //                         argValuesLocal,
        //                         Jimple.v().newNewArrayExpr(
        //                                 objectType,
        //                                 IntConstant.v(node.jjtGetNumChildren()))));
        //         for (int i = 0; i < node.jjtGetNumChildren(); i++) {
        //             _units.insertBefore(
        //                     Jimple.v().newAssignStmt(
        //                             Jimple.v().newArrayRef(
        //                                     argValuesLocal,
        //                                     IntConstant.v(i)),
        //                             (Local)_nodeToLocal.get(node.jjtGetChild(i))));
        //         }
        //         RefType typeType = RefType.v("ptolemy.data.type.Type");
        //         Local argTypesLocal = Jimple.v().newLocal("tokenTypes",
        //                 ArrayType.v(typeType, 1));
        //         _body.getLocals().add(argTypesLocal);
        //         Local typeLocal = Jimple.v().newLocal("classType",
        //                 typeType);
        //         _body.getLocals().add(typeLocal);
        //         Local tokenLocal = Jimple.v().newLocal("token",
        //                 PtolemyUtilities.tokenType);
        //         _body.getLocals().add(tokenLocal);
        //         _units.insertBefore(
        //                 Jimple.v().newAssignStmt(
        //                         argTypesLocal,
        //                         Jimple.v().newNewArrayExpr(
        //                                 typeType,
        //                                 IntConstant.v(argCount))));
        //         Local indexLocal = Jimple.v().newLocal("index", IntType.v());
        //         _body.getLocals().add(indexLocal);
        //         // The list of initializer instructions.
        //         List initializerList = new LinkedList();
        //         initializerList.add(
        //                 Jimple.v().newAssignStmt(
        //                         indexLocal,
        //                         IntConstant.v(0)));
        //         // The list of body instructions.
        //         List bodyList = new LinkedList();
        //         bodyList.add(
        //                 Jimple.v().newAssignStmt(
        //                         tokenLocal,
        //                         Jimple.v().newArrayRef(
        //                                 argValuesLocal,
        //                                 indexLocal)));
        //         bodyList.add(
        //                 Jimple.v().newAssignStmt(
        //                         typeLocal,
        //                         Jimple.v().newVirtualInvokeExpr(
        //                                 tokenLocal,
        //                                 PtolemyUtilities.tokenGetTypeMethod.makeRef())));
        //         bodyList.add(
        //                 Jimple.v().newAssignStmt(
        //                         Jimple.v().newArrayRef(
        //                                 argTypesLocal,
        //                                 indexLocal),
        //                         typeLocal));
        //         // Increment the index.
        //         bodyList.add(
        //                 Jimple.v().newAssignStmt(
        //                         indexLocal,
        //                         Jimple.v().newAddExpr(
        //                                 indexLocal,
        //                                 IntConstant.v(1))));
        //         Expr conditionalExpr =
        //             Jimple.v().newLtExpr(
        //                     indexLocal,
        //                     IntConstant.v(argCount));
        //         Stmt stmt = Jimple.v().newNopStmt();
        //         _units.insertBefore(stmt);
        //         SootUtilities.createForLoopBefore(_body,
        //                 stmt,
        //                 initializerList,
        //                 bodyList,
        //                 conditionalExpr);
        //         SootMethod methodCallEvaluationMethod =
        //             Scene.v().getMethod("<ptolemy.data.expr.ParseTreeEvaluator: ptolemy.data.Token methodCall(java.lang.String,int,ptolemy.data.type.Type[],java.lang.Object[])>");
        //         List argList = new ArrayList();
        //         argList.add(StringConstant.v(node.getMethodName()));
        //         argList.add(IntConstant.v(argCount));
        //         argList.add(argTypesLocal);
        //         argList.add(argValuesLocal);
        //         _units.insertBefore(
        //                 Jimple.v().newAssignStmt(
        //                         tokenLocal,
        //                         Jimple.v().newStaticInvokeExpr(
        //                                 methodCallEvaluationMethod.makeRef(),
        //                                 argList)));
        _nodeToLocal.put(node, tokenLocal);
    }

    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _debug(node);

        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        Local resultLocal = Jimple.v().newLocal("result",
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(resultLocal);

        Local tokenLocal = Jimple.v().newLocal("token",
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(tokenLocal);

        Local timesLocal = Jimple.v().newLocal("times", IntType.v());
        _body.getLocals().add(timesLocal);

        _units.insertBefore(Jimple.v().newAssignStmt(resultLocal,
                (Local) _nodeToLocal.get(node.jjtGetChild(0))), _insertPoint);

        for (int i = 1; i < numChildren; i++) {
            _units.insertBefore(Jimple.v().newAssignStmt(tokenLocal,
                    (Local) _nodeToLocal.get(node.jjtGetChild(i))),
                    _insertPoint);
            _units.insertBefore(Jimple.v().newAssignStmt(
                    tokenLocal,
                    Jimple.v().newCastExpr(tokenLocal,
                            RefType.v(PtolemyUtilities.scalarTokenClass))),
                    _insertPoint);
            _units.insertBefore(Jimple.v().newAssignStmt(
                    timesLocal,
                    Jimple.v().newVirtualInvokeExpr(tokenLocal,
                            PtolemyUtilities.tokenIntValueMethod.makeRef())),
                    _insertPoint);

            _units.insertBefore(Jimple.v().newAssignStmt(
                    resultLocal,
                    Jimple.v().newVirtualInvokeExpr(resultLocal,
                            PtolemyUtilities.tokenPowMethod.makeRef(),
                            timesLocal)), _insertPoint);
        }

        _nodeToLocal.put(node, resultLocal);
    }

    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        Local tokenLocal = Jimple.v().newLocal("token",
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(tokenLocal);

        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == (lexicalTokenList.size() + 1), node,
                "The number of child nodes is "
                        + "not equal to number of operators plus one");

        Local resultLocal = (Local) _nodeToLocal.get(node.jjtGetChild(0));

        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token) lexicalTokenList.get(i - 1);
            Local nextLocal = (Local) _nodeToLocal.get(node.jjtGetChild(i));

            if (operator.kind == PtParserConstants.MULTIPLY) {
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(resultLocal,
                                PtolemyUtilities.tokenMultiplyMethod.makeRef(),
                                nextLocal)), _insertPoint);
                resultLocal = tokenLocal;
            } else if (operator.kind == PtParserConstants.DIVIDE) {
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(resultLocal,
                                PtolemyUtilities.tokenDivideMethod.makeRef(),
                                nextLocal)), _insertPoint);
                resultLocal = tokenLocal;
            } else if (operator.kind == PtParserConstants.MODULO) {
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(resultLocal,
                                PtolemyUtilities.tokenModuloMethod.makeRef(),
                                nextLocal)), _insertPoint);
                resultLocal = tokenLocal;
            } else {
                _assert(false, node, "Invalid operation");
            }
        }

        _nodeToLocal.put(node, resultLocal);
    }

    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        _debug(node);

        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        _assert(node.getFieldNames().size() == numChildren, node,
                "The number of labels and values does not "
                        + "match in parsing a record expression.");

        String[] labels = (String[]) node.getFieldNames().toArray(
                new String[numChildren]);
        RefType stringType = RefType.v("java.lang.String");

        Local labelsLocal = Jimple.v().newLocal("labelArray",
                ArrayType.v(stringType, 1));
        _body.getLocals().add(labelsLocal);
        _units.insertBefore(Jimple.v().newAssignStmt(
                labelsLocal,
                Jimple.v().newNewArrayExpr(stringType,
                        IntConstant.v(labels.length))), _insertPoint);

        for (int i = 0; i < labels.length; i++) {
            _units.insertBefore(Jimple.v().newAssignStmt(
                    Jimple.v().newArrayRef(labelsLocal, IntConstant.v(i)),
                    StringConstant.v(labels[i])), _insertPoint);
        }

        Local valuesLocal = _getChildTokensLocal(0, node);

        Local tokenLocal = Jimple.v().newLocal("token",
                RefType.v(PtolemyUtilities.recordTokenClass));
        _body.getLocals().add(tokenLocal);

        _units.insertBefore(Jimple.v().newAssignStmt(
                tokenLocal,
                Jimple.v().newNewExpr(
                        RefType.v(PtolemyUtilities.recordTokenClass))),
                _insertPoint);
        _units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(tokenLocal,
                        PtolemyUtilities.recordTokenConstructor.makeRef(),
                        labelsLocal, valuesLocal)), _insertPoint);

        _nodeToLocal.put(node, tokenLocal);
    }

    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _debug(node);

        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node, "The number of child nodes must be two");

        Token operator = node.getOperator();
        Local leftLocal = (Local) _nodeToLocal.get(node.jjtGetChild(0));
        Local rightLocal = (Local) _nodeToLocal.get(node.jjtGetChild(1));

        Type scalarType = RefType.v("ptolemy.data.ScalarToken");
        Local tokenLocal = Jimple.v().newLocal("token",
                PtolemyUtilities.tokenType);
        _body.getLocals().add(tokenLocal);

        Local leftScalarLocal = Jimple.v().newLocal("leftScalar", scalarType);
        _body.getLocals().add(leftScalarLocal);

        Local rightScalarLocal = Jimple.v().newLocal("rightScalar", scalarType);
        _body.getLocals().add(rightScalarLocal);

        if (operator.kind == PtParserConstants.EQUALS) {
            _units.insertBefore(Jimple.v().newAssignStmt(
                    tokenLocal,
                    Jimple.v().newVirtualInvokeExpr(leftLocal,
                            PtolemyUtilities.tokenEqualsMethod.makeRef(),
                            rightLocal)), _insertPoint);
        } else if (operator.kind == PtParserConstants.NOTEQUALS) {
            _units.insertBefore(Jimple.v().newAssignStmt(
                    tokenLocal,
                    Jimple.v().newVirtualInvokeExpr(leftLocal,
                            PtolemyUtilities.tokenEqualsMethod.makeRef(),
                            rightLocal)), _insertPoint);
            _units.insertBefore(Jimple.v().newAssignStmt(
                    tokenLocal,
                    Jimple.v().newVirtualInvokeExpr(tokenLocal,
                            PtolemyUtilities.tokenNotMethod.makeRef())),
                    _insertPoint);
        } else {
            _units.insertBefore(Jimple.v().newAssignStmt(leftScalarLocal,
                    Jimple.v().newCastExpr(leftLocal, scalarType)),
                    _insertPoint);
            _units.insertBefore(Jimple.v().newAssignStmt(rightScalarLocal,
                    Jimple.v().newCastExpr(rightLocal, scalarType)),
                    _insertPoint);

            if (operator.kind == PtParserConstants.GTE) {
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(
                                leftScalarLocal,
                                PtolemyUtilities.tokenIsLessThanMethod
                                        .makeRef(), rightScalarLocal)),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(tokenLocal,
                                PtolemyUtilities.tokenNotMethod.makeRef())),
                        _insertPoint);
            } else if (operator.kind == PtParserConstants.GT) {
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(
                                rightScalarLocal,
                                PtolemyUtilities.tokenIsLessThanMethod
                                        .makeRef(), leftScalarLocal)),
                        _insertPoint);
            } else if (operator.kind == PtParserConstants.LTE) {
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(
                                rightScalarLocal,
                                PtolemyUtilities.tokenIsLessThanMethod
                                        .makeRef(), leftScalarLocal)),
                        _insertPoint);
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(tokenLocal,
                                PtolemyUtilities.tokenNotMethod.makeRef())),
                        _insertPoint);
            } else if (operator.kind == PtParserConstants.LT) {
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(
                                leftScalarLocal,
                                PtolemyUtilities.tokenIsLessThanMethod
                                        .makeRef(), rightScalarLocal)),
                        _insertPoint);
            } else {
                throw new IllegalActionException("Invalid operation "
                        + operator.image);
            }
        }

        _nodeToLocal.put(node, tokenLocal);
    }

    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _debug(node);

        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node, "The number of child nodes must be two");

        Type scalarType = RefType.v("ptolemy.data.ScalarToken");
        Token operator = node.getOperator();
        Local tokenLocal = (Local) _nodeToLocal.get(node.jjtGetChild(0));
        Local bitsLocal = (Local) _nodeToLocal.get(node.jjtGetChild(1));
        Local resultLocal = Jimple.v().newLocal("tokenResult",
                PtolemyUtilities.tokenType);
        _body.getLocals().add(resultLocal);

        Local scalarLocal = Jimple.v().newLocal("scalar", scalarType);
        _body.getLocals().add(scalarLocal);

        Local intLocal = Jimple.v().newLocal("bits", IntType.v());
        _body.getLocals().add(intLocal);

        _units.insertBefore(Jimple.v().newAssignStmt(scalarLocal,
                Jimple.v().newCastExpr(bitsLocal, scalarType)), _insertPoint);
        _units.insertBefore(Jimple.v().newAssignStmt(
                intLocal,
                Jimple.v().newVirtualInvokeExpr(scalarLocal,
                        PtolemyUtilities.tokenIntValueMethod.makeRef())),
                _insertPoint);
        _units.insertBefore(Jimple.v().newAssignStmt(scalarLocal,
                Jimple.v().newCastExpr(tokenLocal, scalarType)), _insertPoint);

        if (operator.kind == PtParserConstants.SHL) {
            _units.insertBefore(Jimple.v().newAssignStmt(
                    resultLocal,
                    Jimple.v().newVirtualInvokeExpr(scalarLocal,
                            PtolemyUtilities.tokenLeftShiftMethod.makeRef(),
                            intLocal)), _insertPoint);
        } else if (operator.kind == PtParserConstants.SHR) {
            _units.insertBefore(Jimple.v().newAssignStmt(
                    resultLocal,
                    Jimple.v().newVirtualInvokeExpr(scalarLocal,
                            PtolemyUtilities.tokenRightShiftMethod.makeRef(),
                            intLocal)), _insertPoint);
        } else if (operator.kind == PtParserConstants.LSHR) {
            _units.insertBefore(Jimple.v().newAssignStmt(
                    resultLocal,
                    Jimple.v().newVirtualInvokeExpr(
                            scalarLocal,
                            PtolemyUtilities.tokenLogicalRightShiftMethod
                                    .makeRef(), intLocal)), _insertPoint);
        } else {
            _assert(false, node, "Invalid operation");
        }

        _nodeToLocal.put(node, resultLocal);
    }

    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _debug(node);

        _generateAllChildren(node);

        Local tokenLocal = Jimple.v().newLocal("token",
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(tokenLocal);

        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == (lexicalTokenList.size() + 1), node,
                "The number of child nodes is "
                        + "not equal to number of operators plus one");

        Local resultLocal = (Local) _nodeToLocal.get(node.jjtGetChild(0));

        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token) lexicalTokenList.get(i - 1);
            Local nextLocal = (Local) _nodeToLocal.get(node.jjtGetChild(i));

            if (operator.kind == PtParserConstants.PLUS) {
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(resultLocal,
                                PtolemyUtilities.tokenAddMethod.makeRef(),
                                nextLocal)), _insertPoint);
                resultLocal = tokenLocal;
            } else if (operator.kind == PtParserConstants.MINUS) {
                _units.insertBefore(Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newVirtualInvokeExpr(resultLocal,
                                PtolemyUtilities.tokenSubtractMethod.makeRef(),
                                nextLocal)), _insertPoint);
                resultLocal = tokenLocal;
            } else {
                _assert(false, node, "Invalid operation");
            }
        }

        _nodeToLocal.put(node, resultLocal);
    }

    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _debug(node);

        _generateAllChildren(node);
        _assert(node.jjtGetNumChildren() == 1, node,
                "Unary node must have exactly one child!");

        Local resultLocal = Jimple.v().newLocal("token",
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(resultLocal);

        Local tokenLocal = (Local) _nodeToLocal.get(node.jjtGetChild(0));

        if (node.isMinus()) {
            Local zeroLocal = Jimple.v().newLocal("token",
                    RefType.v(PtolemyUtilities.tokenClass));
            _body.getLocals().add(zeroLocal);

            _units.insertBefore(Jimple.v().newAssignStmt(
                    zeroLocal,
                    Jimple.v().newVirtualInvokeExpr(tokenLocal,
                            PtolemyUtilities.tokenZeroMethod.makeRef())),
                    _insertPoint);
            _units.insertBefore(Jimple.v().newAssignStmt(
                    resultLocal,
                    Jimple.v().newVirtualInvokeExpr(zeroLocal,
                            PtolemyUtilities.tokenSubtractMethod.makeRef(),
                            tokenLocal)), _insertPoint);
        } else if (node.isNot()) {
            Type booleanType = RefType.v("ptolemy.data.BooleanToken");
            Local booleanLocal = Jimple.v().newLocal("token", booleanType);
            _body.getLocals().add(booleanLocal);
            _units.insertBefore(Jimple.v().newAssignStmt(booleanLocal,
                    Jimple.v().newCastExpr(tokenLocal, booleanType)),
                    _insertPoint);

            _units.insertBefore(Jimple.v().newAssignStmt(
                    booleanLocal,
                    Jimple.v().newInterfaceInvokeExpr(booleanLocal,
                            PtolemyUtilities.tokenBitwiseNotMethod.makeRef())),
                    _insertPoint);

            _units.insertBefore(Jimple.v().newAssignStmt(
                    resultLocal,
                    Jimple.v().newCastExpr(booleanLocal,
                            PtolemyUtilities.tokenType)), _insertPoint);
        } else if (node.isBitwiseNot()) {
            Type bitwiseType = RefType.v("ptolemy.data.BitwiseOperationToken");
            Local bitwiseLocal = Jimple.v().newLocal("token", bitwiseType);
            _body.getLocals().add(bitwiseLocal);
            _units.insertBefore(Jimple.v().newAssignStmt(bitwiseLocal,
                    Jimple.v().newCastExpr(tokenLocal, bitwiseType)),
                    _insertPoint);

            _units.insertBefore(Jimple.v().newAssignStmt(
                    bitwiseLocal,
                    Jimple.v().newInterfaceInvokeExpr(bitwiseLocal,
                            PtolemyUtilities.tokenBitwiseNotMethod.makeRef())),
                    _insertPoint);

            _units.insertBefore(Jimple.v().newAssignStmt(
                    resultLocal,
                    Jimple.v().newCastExpr(bitwiseLocal,
                            PtolemyUtilities.tokenType)), _insertPoint);
        } else {
            _assert(false, node, "Unrecognized unary node");
        }

        _nodeToLocal.put(node, resultLocal);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Assert that the given boolean value, which describes the given
     * parse tree node is true.  If it is false, then throw a new
     * InternalErrorException that describes the node that includes
     * the given message.
     */
    protected void _assert(boolean flag, ASTPtRootNode node, String message) {
        if (!flag) {
            throw new InternalErrorException(message + ": " + node.toString());
        }
    }

    /** Print a debugging message.
     */
    protected void _debug(ASTPtRootNode node) {
        if (false) {
            System.out.println("Visiting node of type " + node.getClass());
        }
    }

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     */
    protected void _generateAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();

        for (int i = 0; i < numChildren; i++) {
            _generateChild(node, i);
        }
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     */
    protected void _generateChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        child.visit(this);
    }

    protected Local _getLocalForName(String name) throws IllegalActionException {
        if (_scope != null) {
            Local local = _scope.getLocal(name);

            if (local != null) {
                return local;
            }
        }

        throw new IllegalActionException("The ID " + name + " is undefined.");
    }

    protected boolean _isValidName(String name) throws IllegalActionException {
        if (_scope != null) {
            try {
                return (_scope.getType(name) != null);
            } catch (Exception ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    /** Create a new local that references an array of tokens.  The
     * array will have the size of the number of children of the given
     * node, minus the given start position.  Add code to the body to
     * initialize and populate the array.
     */
    protected Local _getChildTokensLocal(int start, ASTPtRootNode node) {
        Local arrayLocal = Jimple.v().newLocal("tokenArray",
                ArrayType.v(PtolemyUtilities.tokenType, 1));
        _body.getLocals().add(arrayLocal);
        _units.insertBefore(Jimple.v().newAssignStmt(
                arrayLocal,
                Jimple.v().newNewArrayExpr(PtolemyUtilities.tokenType,
                        IntConstant.v(node.jjtGetNumChildren() - start))),
                _insertPoint);

        for (int i = start; i < node.jjtGetNumChildren(); i++) {
            _units.insertBefore(Jimple.v().newAssignStmt(
                    Jimple.v()
                            .newArrayRef(arrayLocal, IntConstant.v(i - start)),
                    (Local) _nodeToLocal.get(node.jjtGetChild(i))),
                    _insertPoint);
        }

        return arrayLocal;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A map from the node to the local variable. */
    protected HashMap _nodeToLocal;

    /** The code body under construction. */
    protected JimpleBody _body;

    /** The units of the parse tree. */
    protected Chain _units;

    /** The scope of the parse tree. */
    protected CodeGenerationScope _scope;

    /** The insertion point. */
    protected Unit _insertPoint;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The number of the current function, used to generate a unique name. */
    private static int _functionCount = 0;
}
