/* A formatter to output Java source code from eclipse AST.

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
/******************************************************************************
 Copyright (c) 2000, 2004 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - initial API and implementation
 *******************************************************************************/
package ptolemy.backtrack.eclipse.ast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

///////////////////////////////////////////////////////////////////
//// ASTFormatter

/**
 An AST visitor that traverses an eclipse AST from a root node
 (usually a {@link org.eclipse.jdt.core.dom.CompilationUnit} object), and outputs the formatted
 Java source code. It is modified from 
 org.eclipse.jdt.core.dom.NaiveASTFlattener in Eclipse 3.0.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 @see org.eclipse.jdt.core.dom.ASTVisitor
 */
public class ASTFormatter extends ASTVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct an AST formatter with a {@link StringBuffer} where the
     *  formatter output will be added.
     *
     *  @param buffer The string buffer to be used.
     */
    public ASTFormatter(StringBuffer buffer) {
        _buffer = buffer;
    }

    /** Construct an AST formatter with a {@link StringBuffer} where the
     *  formatter output will be added.
     *
     *  @param buffer The string buffer to be used.
     *  @param sourceStream The input stream from which the Java source can be
     *   read in.
     */
    public ASTFormatter(StringBuffer buffer, InputStream sourceStream) {
        _buffer = buffer;
        _sourceStream = sourceStream;
    }

    /** Construct an AST formatter with a {@link StringBuffer} where the
     *  formatter output will be added.
     *
     *  @param buffer The string buffer to be used.
     *  @param source The array that contains the Java source.
     */
    public ASTFormatter(StringBuffer buffer, char[] source) {
        _buffer = buffer;
        _source = source;
    }

    /** Construct an AST formatter with a writer to which the formatted
     *  output will be written.
     *
     *  @param writer The writer to write to.
     */
    public ASTFormatter(Writer writer) {
        _writer = writer;
    }

    /** Construct an AST formatter with a writer to which the formatted
     *  output will be written.
     *
     *  @param writer The writer to write to.
     *  @param sourceStream The input stream from which the Java source can be
     *   read in.
     */
    public ASTFormatter(Writer writer, InputStream sourceStream) {
        _writer = writer;
        _sourceStream = sourceStream;
    }

    /** Construct an AST formatter with a writer to which the formatted
     *  output will be written.
     *
     *  @param writer The writer to write to.
     *  @param source The array that contains the Java source.
     */
    public ASTFormatter(Writer writer, char[] source) {
        _writer = writer;
        _source = source;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read in one or more Java source files, parse them with the
     *  Eclipse parser, format their AST, and print out to the standard
     *  output.
     *
     *  @param args The names of Java source files.
     *  @exception Exception If error occurs.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("USAGE:"
                    + "java ptolemy.backtrack.eclipse.ast.ASTFormatter"
                    + " [.java files...]");
        } else {
            Writer writer = new OutputStreamWriter(System.out);

            for (int i = 0; i < args.length; i++) {
                String fileName = args[i];
                FileInputStream stream = new FileInputStream(fileName);
                CompilationUnit root = ASTBuilder.parse(fileName);
                ASTFormatter formatter = new ASTFormatter(writer, stream);
                root.accept(formatter);
                stream.close();
            }

            writer.close();
        }
    }

    /** Handle an AST node before it is visited.
     *
     *  @param node The AST node.
     */
    public void postVisit(ASTNode node) {
        super.postVisit(node);

        if (!(node instanceof BlockComment || node instanceof LineComment)) {
            _checkComments(node.getStartPosition() + node.getLength());
        }
    }

    /** Handle an AST node after it is visited.
     *
     *  @param node The AST node.
     */
    public void preVisit(ASTNode node) {
        if (!(node instanceof BlockComment || node instanceof LineComment)) {
            _checkComments(node.getStartPosition());
        }

        super.preVisit(node);
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(AnnotationTypeDeclaration node) {
        if (node.getJavadoc() != null) {
            node.getJavadoc().accept(this);
        }

        _output(_indent);
        _outputModifiers(node.modifiers());
        _output("@interface ");
        node.getName().accept(this);
        _openBrace();

        for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
            BodyDeclaration d = (BodyDeclaration) it.next();
            d.accept(this);
        }

        _checkComments((node.getStartPosition() + node.getLength()) - 1);
        _closeBrace();
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(AnnotationTypeMemberDeclaration node) {
        if (node.getJavadoc() != null) {
            node.getJavadoc().accept(this);
        }

        _output(_indent);
        _outputModifiers(node.modifiers());
        node.getType().accept(this);
        _output(" ");
        node.getName().accept(this);
        _output("()");

        if (node.getDefault() != null) {
            _output(" default ");
            node.getDefault().accept(this);
        }

        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(AnonymousClassDeclaration node) {
        _output(" ");
        _openBrace();

        for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
            BodyDeclaration b = (BodyDeclaration) it.next();
            b.accept(this);
        }

        _checkComments((node.getStartPosition() + node.getLength()) - 1);
        _closeBrace(false);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ArrayAccess node) {
        node.getArray().accept(this);
        _output("[");
        node.getIndex().accept(this);
        _output("]");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ArrayCreation node) {
        _output("new ");

        ArrayType at = node.getType();
        int dims = at.getDimensions();
        Type elementType = at.getElementType();
        elementType.accept(this);

        for (Iterator it = node.dimensions().iterator(); it.hasNext();) {
            _output("[");

            Expression e = (Expression) it.next();
            e.accept(this);
            _output("]");
            dims--;
        }

        // add empty "[]" for each extra array dimension
        for (int i = 0; i < dims; i++) {
            _output("[]");
        }

        if (node.getInitializer() != null) {
            _newLineAfterBlock = false;
            _increaseIndent();
            node.getInitializer().accept(this);
            _decreaseIndent();
            _newLineAfterBlock = true;
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ArrayInitializer node) {
        _output(" ");
        _openBrace();

        for (Iterator it = node.expressions().iterator(); it.hasNext();) {
            _output(_indent);

            Expression e = (Expression) it.next();
            e.accept(this);

            if (it.hasNext()) {
                _output(",");
            }

            _output("\n");
        }

        _checkComments((node.getStartPosition() + node.getLength()) - 1);
        _closeBrace(false);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ArrayType node) {
        node.getComponentType().accept(this);
        _output("[]");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(AssertStatement node) {
        _output(_indent);
        _output("assert ");
        node.getExpression().accept(this);

        if (node.getMessage() != null) {
            _output(" : ");
            node.getMessage().accept(this);
        }

        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(Assignment node) {
        node.getLeftHandSide().accept(this);
        _output(" " + node.getOperator().toString() + " ");
        node.getRightHandSide().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(Block node) {
        boolean newLineAfterBlock = _newLineAfterBlock;

        // Indent if it is in a list of statements.
        if (node.getLocationInParent().isChildListProperty()) {
            _output(_indent);
        }

        _openBrace();

        for (Iterator it = node.statements().iterator(); it.hasNext();) {
            Statement s = (Statement) it.next();
            s.accept(this);
        }

        _checkComments((node.getStartPosition() + node.getLength()) - 1);
        _closeBrace(newLineAfterBlock);
        _newLineAfterBlock = true;
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(BlockComment node) {
        _output(_indent);
        _output(_commentString + "\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(BooleanLiteral node) {
        if (node.booleanValue() == true) {
            _output("true");
        } else {
            _output("false");
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(BreakStatement node) {
        _output(_indent);
        _output("break");

        if (node.getLabel() != null) {
            _output(" ");
            node.getLabel().accept(this);
        }

        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(CastExpression node) {
        _output("(");
        node.getType().accept(this);
        _output(")");
        node.getExpression().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(CatchClause node) {
        _output(" catch (");
        node.getException().accept(this);
        _output(") ");
        node.getBody().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(CharacterLiteral node) {
        _output(node.getEscapedValue());
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ClassInstanceCreation node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
            _output(".");
        }

        _output("new ");

        if (!node.typeArguments().isEmpty()) {
            _output("<");

            Iterator it;
            for (it = node.typeArguments().iterator(); it.hasNext();) {
                Type t = (Type) it.next();
                t.accept(this);
                if (it.hasNext()) {
                    _output(", ");
                }
            }
            _output(">");
        }
        node.getType().accept(this);

        _output("(");

        for (Iterator it = node.arguments().iterator(); it.hasNext();) {
            Expression e = (Expression) it.next();
            e.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(")");

        if (node.getAnonymousClassDeclaration() != null) {
            node.getAnonymousClassDeclaration().accept(this);
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(CompilationUnit node) {
        // Set up the comment iterator.
        List comments = node.getCommentList();

        if (!comments.isEmpty()) {
            _commentIterator = comments.iterator();

            try {
                _nextComment();
            } catch (IOException e) {
                throw new ASTIORuntimeException(e);
            }
        }

        // Sort all the importations.
        List imports = node.imports();
        int length = imports.size();
        AST ast = node.getAST();

        for (int i = 0; i < (length - 1); i++) {
            for (int j = i + 1; j < length; j++) {
                ImportDeclaration import1 = (ImportDeclaration) imports.get(i);
                ImportDeclaration import2 = (ImportDeclaration) imports.get(j);

                if (import1.toString().compareTo(import2.toString()) > 0) {
                    // Swap.
                    imports.remove(j);
                    imports.remove(i);
                    imports.add(i, ASTNode.copySubtree(ast, import2));
                    imports.add(j, ASTNode.copySubtree(ast, import1));
                }
            }
        }

        if (node.getPackage() != null) {
            node.getPackage().accept(this);
        }

        for (Iterator it = node.imports().iterator(); it.hasNext();) {
            ImportDeclaration d = (ImportDeclaration) it.next();
            d.accept(this);
        }

        _output("\n");

        for (Iterator it = node.types().iterator(); it.hasNext();) {
            AbstractTypeDeclaration d = (AbstractTypeDeclaration) it.next();
            d.accept(this);
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ConditionalExpression node) {
        node.getExpression().accept(this);
        _output("?");
        node.getThenExpression().accept(this);
        _output(":");
        node.getElseExpression().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ConstructorInvocation node) {
        _output(_indent);

        if (node.getAST().apiLevel() >= AST.JLS3) {
            if (!node.typeArguments().isEmpty()) {
                _output("<");

                Iterator it;

                for (it = node.typeArguments().iterator(); it.hasNext();) {
                    Type t = (Type) it.next();
                    t.accept(this);

                    if (it.hasNext()) {
                        _output(", ");
                    }
                }

                _output(">");
            }
        }

        _output("this(");

        for (Iterator it = node.arguments().iterator(); it.hasNext();) {
            Expression e = (Expression) it.next();
            e.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(");\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ContinueStatement node) {
        _output(_indent);
        _output("continue");

        if (node.getLabel() != null) {
            _output(" ");
            node.getLabel().accept(this);
        }

        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(DoStatement node) {
        _output(_indent);
        _output("do ");

        if (node.getBody() instanceof Block) {
            _newLineAfterBlock = false;
        } else {
            _output("\n");
            _increaseIndent();
        }

        node.getBody().accept(this);

        if (!(node.getBody() instanceof Block)) {
            _decreaseIndent();
            _output(_indent);
        } else {
            _output(" ");
        }

        _output("while (");
        node.getExpression().accept(this);
        _output(");\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(EmptyStatement node) {
        _output(_indent);
        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(EnhancedForStatement node) {
        _output(_indent);
        _output("for (");
        node.getParameter().accept(this);
        _output(" : ");
        node.getExpression().accept(this);
        _output(") ");
        node.getBody().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(EnumConstantDeclaration node) {
        if (node.getJavadoc() != null) {
            node.getJavadoc().accept(this);
        }

        _output(_indent);
        _outputModifiers(node.modifiers());
        node.getName().accept(this);

        if (!node.arguments().isEmpty()) {
            _output("(");

            for (Iterator it = node.arguments().iterator(); it.hasNext();) {
                Expression e = (Expression) it.next();
                e.accept(this);

                if (it.hasNext()) {
                    _output(", ");
                }
            }

            _output(")");
        }

        // bodyDeclarations() no longer exists in JDT 3.1.

        /*if (!node.bodyDeclarations().isEmpty()) {
         _openBrace();
         Iterator it;
         for (it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
         BodyDeclaration d = (BodyDeclaration) it.next();
         d.accept(this);
         }
         _closeBrace();
         }*/
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(EnumDeclaration node) {
        if (node.getJavadoc() != null) {
            node.getJavadoc().accept(this);
        }

        _output(_indent);
        _outputModifiers(node.modifiers());
        _output("enum ");
        node.getName().accept(this);
        _output(" ");

        if (!node.superInterfaceTypes().isEmpty()) {
            _output("implements ");

            Iterator it;

            for (it = node.superInterfaceTypes().iterator(); it.hasNext();) {
                Type t = (Type) it.next();
                t.accept(this);

                if (it.hasNext()) {
                    _output(", ");
                }
            }

            _output(" ");
        }

        _openBrace();

        BodyDeclaration prev = null;
        Iterator it;

        for (it = node.bodyDeclarations().iterator(); it.hasNext();) {
            BodyDeclaration d = (BodyDeclaration) it.next();

            if (prev instanceof EnumConstantDeclaration) {
                // enum constant declarations do not include punctuation
                if (d instanceof EnumConstantDeclaration) {
                    // enum constant declarations are separated by commas
                    _output(", ");
                } else {
                    // semicolon separates last enum constant declaration from
                    // first class body declarations
                    _output("; ");
                }
            }

            d.accept(this);
        }

        _checkComments((node.getStartPosition() + node.getLength()) - 1);
        _closeBrace();
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ExpressionStatement node) {
        _output(_indent);
        node.getExpression().accept(this);
        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(FieldAccess node) {
        node.getExpression().accept(this);
        _output(".");
        node.getName().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(FieldDeclaration node) {
        if (node.getJavadoc() != null) {
            node.getJavadoc().accept(this);
        }

        _output(_indent);

        _outputModifiers(node.modifiers());

        node.getType().accept(this);
        _output(" ");

        for (Iterator it = node.fragments().iterator(); it.hasNext();) {
            VariableDeclarationFragment f = (VariableDeclarationFragment) it
                    .next();
            f.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(";\n");
        _output("\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ForStatement node) {
        _output(_indent);
        _output("for (");

        for (Iterator it = node.initializers().iterator(); it.hasNext();) {
            Expression e = (Expression) it.next();
            e.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output("; ");

        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }

        _output("; ");

        for (Iterator it = node.updaters().iterator(); it.hasNext();) {
            Expression e = (Expression) it.next();
            e.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(") ");

        if (!(node.getBody() instanceof Block)) {
            _output("\n");
            _increaseIndent();
        }

        _newLineAfterBlock = true;
        node.getBody().accept(this);

        if (!(node.getBody() instanceof Block)) {
            _decreaseIndent();
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(IfStatement node) {
        Statement thenStatement = node.getThenStatement();
        Statement elseStatement = node.getElseStatement();

        if (_indentIfStatement) {
            _output(_indent);
        } else {
            _indentIfStatement = true;
        }

        _output("if (");
        node.getExpression().accept(this);
        _output(")");

        if (thenStatement instanceof Block) {
            _output(" ");
            _newLineAfterBlock = elseStatement == null;
            node.getThenStatement().accept(this);
            _newLineAfterBlock = true;
        } else {
            _output("\n");
            _increaseIndent();
            node.getThenStatement().accept(this);
            _decreaseIndent();
        }

        if (elseStatement != null) {
            if (thenStatement instanceof Block) {
                _output(" ");
            } else {
                _output(_indent);
            }

            if (elseStatement instanceof Block) {
                _output("else ");
                elseStatement.accept(this);
            } else if (elseStatement instanceof IfStatement) {
                _indentIfStatement = false;
                _output("else ");
                elseStatement.accept(this);
            } else {
                _output("else\n");
                _increaseIndent();
                elseStatement.accept(this);
                _decreaseIndent();
            }
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ImportDeclaration node) {
        _output(_indent);
        _output("import ");

        if (node.getAST().apiLevel() >= AST.JLS3) {
            if (node.isStatic()) {
                _output("static ");
            }
        }

        node.getName().accept(this);

        if (node.isOnDemand()) {
            _output(".*");
        }

        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(InfixExpression node) {
        node.getLeftOperand().accept(this);
        _output(" "); // for cases like x = i - -1; or x = i++ + ++i;
        _output(node.getOperator().toString());
        _output(" ");
        node.getRightOperand().accept(this);

        for (Iterator it = node.extendedOperands().iterator(); it.hasNext();) {
            _output(node.getOperator().toString());

            Expression e = (Expression) it.next();
            e.accept(this);
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(Initializer node) {
        if (node.getJavadoc() != null) {
            node.getJavadoc().accept(this);
        }

        _outputModifiers(node.modifiers());

        _output(_indent);
        node.getBody().accept(this);
        _output("\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(InstanceofExpression node) {
        node.getLeftOperand().accept(this);
        _output(" instanceof ");
        node.getRightOperand().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(Javadoc node) {
        _output(_indent);
        _output("/** ");
        _output(_indent);

        for (Iterator it = node.tags().iterator(); it.hasNext();) {
            ASTNode e = (ASTNode) it.next();
            e.accept(this);
        }

        _output("\n");
        _output(_indent);
        _output(" */\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(LabeledStatement node) {
        _output(_indent);
        node.getLabel().accept(this);
        _output(":\n");
        node.getBody().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(LineComment node) {
        _output(_indent);
        _output(_commentString + "\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(MarkerAnnotation node) {
        _output("@");
        node.getTypeName().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(MemberRef node) {
        if (node.getQualifier() != null) {
            node.getQualifier().accept(this);
        }

        _output("#");
        node.getName().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(MemberValuePair node) {
        node.getName().accept(this);
        _output(" = ");
        node.getValue().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(MethodDeclaration node) {
        if (node.getJavadoc() != null) {
            node.getJavadoc().accept(this);
        }

        _output(_indent);

        _outputModifiers(node.modifiers());
        if (!node.typeParameters().isEmpty()) {
            _output("<");

            Iterator it;
            for (it = node.typeParameters().iterator(); it.hasNext();) {
                TypeParameter t = (TypeParameter) it.next();
                t.accept(this);
                if (it.hasNext()) {
                    _output(", ");
                }
            }

            _output(">");
        }

        if (!node.isConstructor()) {
            if (node.getReturnType2() != null) {
                node.getReturnType2().accept(this);
            } else {
                // methods really ought to have a return type
                _output("void");
            }

            _output(" ");
        }

        node.getName().accept(this);
        _output("(");

        for (Iterator it = node.parameters().iterator(); it.hasNext();) {
            SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
            v.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(")");

        for (int i = 0; i < node.getExtraDimensions(); i++) {
            _output("[]");
        }

        if (!node.thrownExceptions().isEmpty()) {
            _output(" throws ");

            Iterator it;

            for (it = node.thrownExceptions().iterator(); it.hasNext();) {
                Name n = (Name) it.next();
                n.accept(this);

                if (it.hasNext()) {
                    _output(", ");
                }
            }

            _output(" ");
        }

        if (node.getBody() == null) {
            _output(";\n");
        } else {
            _output(" ");
            node.getBody().accept(this);
        }

        _output("\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
            _output(".");
        }

        if (node.getAST().apiLevel() >= AST.JLS3) {
            if (!node.typeArguments().isEmpty()) {
                _output("<");

                Iterator it;

                for (it = node.typeArguments().iterator(); it.hasNext();) {
                    Type t = (Type) it.next();
                    t.accept(this);

                    if (it.hasNext()) {
                        _output(", ");
                    }
                }

                _output(">");
            }
        }

        node.getName().accept(this);
        _output("(");

        for (Iterator it = node.arguments().iterator(); it.hasNext();) {
            Expression e = (Expression) it.next();
            e.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(")");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(MethodRef node) {
        if (node.getQualifier() != null) {
            node.getQualifier().accept(this);
        }

        _output("#");
        node.getName().accept(this);
        _output("(");

        for (Iterator it = node.parameters().iterator(); it.hasNext();) {
            MethodRefParameter e = (MethodRefParameter) it.next();
            e.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(")");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(MethodRefParameter node) {
        node.getType().accept(this);

        if (node.getName() != null) {
            _output(" ");
            node.getName().accept(this);
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(Modifier node) {
        _output(node.getKeyword().toString());
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(NormalAnnotation node) {
        _output("@");
        node.getTypeName().accept(this);
        _output("(");

        for (Iterator it = node.values().iterator(); it.hasNext();) {
            MemberValuePair p = (MemberValuePair) it.next();
            p.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(")");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(NullLiteral node) {
        _output("null");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(NumberLiteral node) {
        _output(node.getToken());
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(PackageDeclaration node) {
        if (node.getAST().apiLevel() >= AST.JLS3) {
            if (node.getJavadoc() != null) {
                node.getJavadoc().accept(this);
            }

            _output(_indent);

            for (Iterator it = node.annotations().iterator(); it.hasNext();) {
                Annotation p = (Annotation) it.next();
                p.accept(this);
                _output(" ");
            }
        } else {
            _output(_indent);
        }

        _output("package ");
        node.getName().accept(this);
        _output(";\n\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ParameterizedType node) {
        node.getType().accept(this);
        _output("<");

        for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
            Type t = (Type) it.next();
            t.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(">");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ParenthesizedExpression node) {
        _output("(");
        node.getExpression().accept(this);
        _output(")");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(PostfixExpression node) {
        node.getOperand().accept(this);
        _output(node.getOperator().toString());
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(PrefixExpression node) {
        _output(node.getOperator().toString());
        node.getOperand().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(PrimitiveType node) {
        _output(node.getPrimitiveTypeCode().toString());
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(QualifiedName node) {
        node.getQualifier().accept(this);
        _output(".");
        node.getName().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(QualifiedType node) {
        node.getQualifier().accept(this);
        _output(".");
        node.getName().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ReturnStatement node) {
        _output(_indent);
        _output("return");

        if (node.getExpression() != null) {
            _output(" ");
            node.getExpression().accept(this);
        }

        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SimpleName node) {
        _output(node.getIdentifier());
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SimpleType node) {
        return true;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SingleMemberAnnotation node) {
        _output("@");
        node.getTypeName().accept(this);
        _output("(");
        node.getValue().accept(this);
        _output(")");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SingleVariableDeclaration node) {
        _outputModifiers(node.modifiers());

        node.getType().accept(this);

        if (node.isVarargs()) {
            _output("...");
        }

        _output(" ");
        node.getName().accept(this);

        for (int i = 0; i < node.getExtraDimensions(); i++) {
            _output("[]");
        }

        if (node.getInitializer() != null) {
            _output(" = ");
            node.getInitializer().accept(this);
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(StringLiteral node) {
        _output(node.getEscapedValue());
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SuperConstructorInvocation node) {
        _output(_indent);

        if (node.getExpression() != null) {
            node.getExpression().accept(this);
            _output(".");
        }

        if (node.getAST().apiLevel() >= AST.JLS3) {
            if (!node.typeArguments().isEmpty()) {
                _output("<");

                Iterator it;

                for (it = node.typeArguments().iterator(); it.hasNext();) {
                    Type t = (Type) it.next();
                    t.accept(this);

                    if (it.hasNext()) {
                        _output(", ");
                    }
                }

                _output(">");
            }
        }

        _output("super(");

        for (Iterator it = node.arguments().iterator(); it.hasNext();) {
            Expression e = (Expression) it.next();
            e.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(");\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SuperFieldAccess node) {
        if (node.getQualifier() != null) {
            node.getQualifier().accept(this);
            _output(".");
        }

        _output("super.");
        node.getName().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SuperMethodInvocation node) {
        if (node.getQualifier() != null) {
            node.getQualifier().accept(this);
            _output(".");
        }

        _output("super.");

        if (node.getAST().apiLevel() >= AST.JLS3) {
            if (!node.typeArguments().isEmpty()) {
                _output("<");

                Iterator it;

                for (it = node.typeArguments().iterator(); it.hasNext();) {
                    Type t = (Type) it.next();
                    t.accept(this);

                    if (it.hasNext()) {
                        _output(", ");
                    }
                }

                _output(">");
            }
        }

        node.getName().accept(this);
        _output("(");

        for (Iterator it = node.arguments().iterator(); it.hasNext();) {
            Expression e = (Expression) it.next();
            e.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(")");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SwitchCase node) {
        if (node.isDefault()) {
            _output(_indent);
            _output("default:\n");
        } else {
            _output(_indent);
            _output("case ");
            node.getExpression().accept(this);
            _output(":\n");
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SwitchStatement node) {
        _output(_indent);
        _output("switch (");
        node.getExpression().accept(this);
        _output(") ");
        _openBrace();

        for (Iterator it = node.statements().iterator(); it.hasNext();) {
            Statement s = (Statement) it.next();

            if (!(s instanceof SwitchCase)) {
                _increaseIndent();
            }

            s.accept(this);

            if (!(s instanceof SwitchCase)) {
                _decreaseIndent();
            }
        }

        _checkComments((node.getStartPosition() + node.getLength()) - 1);
        _closeBrace();
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(SynchronizedStatement node) {
        _output(_indent);
        _output("synchronized (");
        node.getExpression().accept(this);
        _output(") ");
        node.getBody().accept(this);
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(TagElement node) {
        if (node.isNested()) {
            // nested tags are always enclosed in braces
            _openBrace();
        } else {
            // top-level tags always begin on a new line
            _output("\n");
            _output(_indent);
            _output(" * ");
        }

        boolean previousRequiresWhiteSpace = false;

        if (node.getTagName() != null) {
            _output(node.getTagName());
            previousRequiresWhiteSpace = true;
        }

        boolean previousRequiresNewLine = false;

        for (Iterator it = node.fragments().iterator(); it.hasNext();) {
            ASTNode e = (ASTNode) it.next();

            // assume text elements include necessary leading and trailing
            // whitespace but Name, MemberRef, MethodRef, and nested
            // TagElement do not include white space
            boolean currentIncludesWhiteSpace = (e instanceof TextElement);

            if (previousRequiresNewLine && currentIncludesWhiteSpace) {
                _output("\n");
                _output(_indent);
                _output(" * ");
            }

            previousRequiresNewLine = currentIncludesWhiteSpace;

            // add space if required to separate
            if (previousRequiresWhiteSpace && !currentIncludesWhiteSpace) {
                _output(" ");
            }

            e.accept(this);
            previousRequiresWhiteSpace = !currentIncludesWhiteSpace
                    && !(e instanceof TagElement);
        }

        if (node.isNested()) {
            _checkComments((node.getStartPosition() + node.getLength()) - 1);
            _closeBrace();
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(TextElement node) {
        _output(node.getText());
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ThisExpression node) {
        if (node.getQualifier() != null) {
            node.getQualifier().accept(this);
            _output(".");
        }

        _output("this");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(ThrowStatement node) {
        _output(_indent);
        _output("throw ");
        node.getExpression().accept(this);
        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(TryStatement node) {
        _output(_indent);
        _output("try ");
        _newLineAfterBlock = false;
        node.getBody().accept(this);

        for (Iterator it = node.catchClauses().iterator(); it.hasNext();) {
            CatchClause cc = (CatchClause) it.next();
            _newLineAfterBlock = !it.hasNext() && (node.getFinally() == null);
            cc.accept(this);
        }

        if (node.getFinally() != null) {
            _output(" finally ");
            node.getFinally().accept(this);
        }

        _newLineAfterBlock = true;
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(TypeDeclaration node) {
        Javadoc javadoc = node.getJavadoc();

        if (javadoc != null) {
            javadoc.accept(this);
        }

        _output(_indent);

        _outputModifiers(node.modifiers());

        _output(node.isInterface() ? "interface " : "class ");

        //$NON-NLS-2$
        node.getName().accept(this);

        if (node.getAST().apiLevel() >= AST.JLS3) {
            if (!node.typeParameters().isEmpty()) {
                _output("<");

                Iterator it;

                for (it = node.typeParameters().iterator(); it.hasNext();) {
                    TypeParameter t = (TypeParameter) it.next();
                    t.accept(this);

                    if (it.hasNext()) {
                        _output(", ");
                    }
                }

                _output(">");
            }
        }

        _output(" ");

        if (node.getSuperclassType() != null) {
            _output("extends ");
            node.getSuperclassType().accept(this);
            _output(" ");
        }

        if (!node.superInterfaceTypes().isEmpty()) {
            _output(node.isInterface() ? "extends " : "implements ");
            //$NON-NLS-2$
            Iterator it;
            for (it = node.superInterfaceTypes().iterator(); it.hasNext();) {
                Type t = (Type) it.next();
                t.accept(this);
                if (it.hasNext()) {
                    _output(", ");
                }
            }
            _output(" ");
        }

        _openBrace();
        _output("\n");

        BodyDeclaration prev = null;
        Iterator it;

        for (it = node.bodyDeclarations().iterator(); it.hasNext();) {
            BodyDeclaration d = (BodyDeclaration) it.next();

            if (prev instanceof EnumConstantDeclaration) {
                // enum constant declarations do not include punctuation
                if (d instanceof EnumConstantDeclaration) {
                    // enum constant declarations are separated by commas
                    _output(", ");
                } else {
                    // semicolon separates last enum constant declaration from
                    // first class body declarations
                    _output("; ");
                }
            }

            d.accept(this);
        }

        _checkComments((node.getStartPosition() + node.getLength()) - 1);
        _closeBrace();
        _output("\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(TypeDeclarationStatement node) {
        node.getDeclaration().accept(this);

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(TypeLiteral node) {
        node.getType().accept(this);
        _output(".class");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(TypeParameter node) {
        node.getName().accept(this);

        if (!node.typeBounds().isEmpty()) {
            _output(" extends ");

            for (Iterator it = node.typeBounds().iterator(); it.hasNext();) {
                Type t = (Type) it.next();
                t.accept(this);

                if (it.hasNext()) {
                    _output(" & ");
                }
            }
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(VariableDeclarationExpression node) {
        _outputModifiers(node.modifiers());

        node.getType().accept(this);
        _output(" ");

        for (Iterator it = node.fragments().iterator(); it.hasNext();) {
            VariableDeclarationFragment f = (VariableDeclarationFragment) it
                    .next();
            f.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(VariableDeclarationFragment node) {
        node.getName().accept(this);

        for (int i = 0; i < node.getExtraDimensions(); i++) {
            _output("[]");
        }

        if (node.getInitializer() != null) {
            _output(" = ");
            node.getInitializer().accept(this);
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(VariableDeclarationStatement node) {
        _output(_indent);

        _outputModifiers(node.modifiers());

        node.getType().accept(this);
        _output(" ");

        for (Iterator it = node.fragments().iterator(); it.hasNext();) {
            VariableDeclarationFragment f = (VariableDeclarationFragment) it
                    .next();
            f.accept(this);

            if (it.hasNext()) {
                _output(", ");
            }
        }

        _output(";\n");
        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(WhileStatement node) {
        _output(_indent);
        _output("while (");
        node.getExpression().accept(this);
        _output(") ");

        if (!(node.getBody() instanceof Block)) {
            _output("\n");
            _increaseIndent();
        }

        node.getBody().accept(this);

        if (!(node.getBody() instanceof Block)) {
            _decreaseIndent();
        }

        return false;
    }

    /** Visit an ast node, and return whether its children should be further
     *  visited.
     *
     *  @param node The AST node.
     *  @return Whether its children should be further visited.
     */
    public boolean visit(WildcardType node) {
        _output("?");

        Type bound = node.getBound();

        if (bound != null) {
            if (node.isUpperBound()) {
                _output(" extends ");
            } else {
                _output(" super ");
            }

            bound.accept(this);
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check the comments from the start position.
     *
     *  @param startPosition The start position.
     */
    private void _checkComments(int startPosition) {
        while ((_comment != null) && (_commentStartPosition <= startPosition)) {
            // Output the comment.
            _comment.accept(this);
            startPosition += _comment.getLength();

            try {
                _nextComment();
            } catch (IOException e) {
                throw new ASTIORuntimeException(e);
            }
        }
    }

    /** Output a closing brase and a new line character after it, and
     *  also decrease the indent amount.
     *  <p>
     *  This is the same as <tt>_closeBrace(true)</tt>.
     */
    private void _closeBrace() {
        _closeBrace(true);
    }

    /** Output a closing brase and a new line character after it if
     *  <tt>newLineAfter</tt> is true, and also decrease the indent
     *  amount.
     *
     *  @param newLineAfter Whether to output a new line character
     *   after the closing brace.
     */
    private void _closeBrace(boolean newLineAfter) {
        _indent.setLength(_indent.length() - 4);
        _output(_indent);
        _output("}");

        if (newLineAfter) {
            _output("\n");
        }
    }

    /** Decrease the indent amount.
     */
    private void _decreaseIndent() {
        _indent.setLength(_indent.length() - 4);
    }

    /** Return the source code from the start position of the given length.
     *
     *  @param startPosition The start position.
     *  @param length The length.
     *  @return The source code.
     *  @exception IOException If error occurs while reading from the source.
     */
    private String _getSource(int startPosition, int length) throws IOException {
        if (_sourceStream != null) {
            byte[] skipContent;

            if (_sourceStreamPosition < startPosition) {
                int skipLength = startPosition - _sourceStreamPosition;
                skipContent = new byte[skipLength];
                if (_sourceStream.read(skipContent) != skipLength) {
                    throw new IOException("Failed to read " + skipLength
                            + " bytes from source stream");
                }
            } else {
                skipContent = new byte[0];
            }

            byte[] buffer = new byte[length];
            int readCount = _sourceStream.read(buffer);
            _sourceStreamPosition = startPosition + readCount;
            _commentStartPosition = startPosition;

            for (int i = skipContent.length - 1; i >= 0; i--) {
                if ((skipContent[i] == ' ') || (skipContent[i] == '\t')
                        || (skipContent[i] == '\n') || (skipContent[i] == '\r')) {
                    _commentStartPosition--;
                } else {
                    break;
                }
            }

            return new String(buffer);
        } else if (_source != null) {
            _commentStartPosition = startPosition;

            for (int i = startPosition - 1; i >= 0; i--) {
                if ((_source[i] == ' ') || (_source[i] == '\t')
                        || (_source[i] == '\n') || (_source[i] == '\r')) {
                    _commentStartPosition--;
                } else {
                    break;
                }
            }

            return new String(_source, startPosition, length);
        }

        return null;
    }

    /** Increase the indent amount.
     */
    private void _increaseIndent() {
        _indent.append("    ");
    }

    /** Output the next comment.
     *
     *  @exception IOException If error occurs while writing to the output.
     */
    private void _nextComment() throws IOException {
        // Get the next comment.
        if (_commentIterator.hasNext()) {
            _comment = (Comment) _commentIterator.next();

            while (_comment instanceof Javadoc && _commentIterator.hasNext()) {
                _comment = (Comment) _commentIterator.next();
            }

            if (_comment instanceof Javadoc) {
                _comment = null;
                _commentIterator = null;
            } else {
                _commentString = _getSource(_comment.getStartPosition(),
                        _comment.getLength());
            }
        } else {
            _comment = null;
            _commentIterator = null;
        }
    }

    /** Output an open brace and increase the indent amount.
     */
    private void _openBrace() {
        _indent.append("    ");
        _output("{\n");
    }

    /** Output a message. If a {@link StringBuffer} is used, the output
     *  is appended to the buffer; if a {@link Writer} is provided, the
     *  output is written to the writer.
     *
     *  @param message The message to be output.
     *  @exception ASTIORuntimeException If a writer is provided but
     *   IO exception occurs when trying to write to the writer.
     */
    private void _output(String message) throws ASTIORuntimeException {
        if (_buffer != null) {
            _buffer.append(message);
        }

        if (_writer != null) {
            try {
                _writer.write(message);
            } catch (IOException e) {
                throw new ASTIORuntimeException(e);
            }
        }
    }

    /** Output a message. If a {@link StringBuffer} is used, the output
     *  is appended to the buffer; if a {@link Writer} is provided, the
     *  output is written to the writer.
     *
     *  @param message The message to be output.
     *  @exception ASTIORuntimeException If a writer is provided but
     *   IO exception occurs when trying to write to the writer.
     */
    private void _output(StringBuffer message) throws ASTIORuntimeException {
        if (_buffer != null) {
            _buffer.append(message);
        }

        if (_writer != null) {
            try {
                _writer.write(message.toString());
            } catch (IOException e) {
                throw new ASTIORuntimeException(e);
            }
        }
    }

    /**
     * Appends the text representation of the given modifier flags,
     * followed by a single space.
     * Used for 3.0 modifiers and annotations.
     *
     * @param ext the list of modifier and annotation nodes
     * (element type: <code>IExtendedModifiers</code>)
     */
    private void _outputModifiers(List ext) {
        for (Iterator it = ext.iterator(); it.hasNext();) {
            ASTNode p = (ASTNode) it.next();
            p.accept(this);
            _output(" ");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The string buffer, where the output is added to.
     */
    private StringBuffer _buffer;

    /** The next comment to be output, or <tt>null</tt> if there is no more
     *  comment.
     */
    private Comment _comment;

    /** The iterator of comments in the whole program.
     */
    private Iterator _commentIterator;

    /** The start position of the comment.
     */
    private int _commentStartPosition;

    /** The content of the next comment.
     */
    private String _commentString;

    /** The current indentation, a string of spaces.
     */
    private StringBuffer _indent = new StringBuffer();

    /** Whether to indent the next if statement. It is
     *  <tt>false</tt> if the if statement is preceded by
     *  an "else".
     */
    private boolean _indentIfStatement = true;

    /** Whether to output a new line character after the
     *  next block. It is <tt>false</tt> if the block is
     *  followed by "catch", "finally" and so on.
     */
    private boolean _newLineAfterBlock = true;

    /** The array that contains Java source, if not <tt>null</tt>. It is used
     *  to generate comments.
     */
    private char[] _source;

    /** The Java source input stream, if not <tt>null</tt>. It is used to
     *  generate comments.
     */
    private InputStream _sourceStream;

    /** The current position in the source input stream.
     */
    private int _sourceStreamPosition = 0;

    /** The writer, where the output is written to.
     */
    private Writer _writer;
}
