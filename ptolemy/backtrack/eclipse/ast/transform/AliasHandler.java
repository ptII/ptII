/* Handler of alias points in the AST.

 Copyright (c) 2005 The Regents of the University of California.
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

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ptolemy.backtrack.eclipse.ast.TypeAnalyzer;
import ptolemy.backtrack.eclipse.ast.TypeAnalyzerState;

//////////////////////////////////////////////////////////////////////////
//// AliasHandler

/**
 Interface of the alias handlers called by {@link TypeAnalyzer}.
 Users may register alias handlers (and other kinds of supported
 handlers) to the {@link TypeAnalyzer} used to analyze Java source code.
 When the analyzer detects an alias point, it calls back those alias
 handlers after proper types are assigned to both the left-hand side and
 the right-hand side of the assignment.
 <p>
 Alias handlers are allowed to modify the alias AST node, either by
 modifying its children in the AST, or by replacing the whole alias node
 with another expression or statement. This is because the handler is called
 after the subtree rooted at the alias node is completely visited by the
 analyzer. However, modifying any node out of this subtree (e.g., changing
 the parent of this alias node to another one) may cause unexpected effect.
 <p>
 Alias points in Java include assignment from an array field to a local
 variable, calling a method with an array field (so that it may be changed
 with another name within the method), returning an array field from a
 method, etc.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface AliasHandler {
    /** Handle an assignment. It may or may not be an aliasing.
     *
     *  @param node The node to be handled.
     *  @param state The current state of the type analyzer.
     */
    public void handle(Assignment node, TypeAnalyzerState state);

    /** Handle a class instance creation (with the <tt>new</tt> operator). The
     *  arguments to the class constructor may or may not be alias of fields.
     *
     *  @param node The node to be handled.
     *  @param state The current state of the type analyzer.
     */
    public void handle(ClassInstanceCreation node, TypeAnalyzerState state);

    /** Handle a method invocation. The arguments of the invocation may or may
     *  not be alias of fields.
     *
     *  @param node The node to be handled.
     *  @param state The current state of the type analyzer.
     */
    public void handle(MethodInvocation node, TypeAnalyzerState state);

    /** Handle a return statement. The return expression may or may not be an
     *  aliased field.
     *
     *  @param node The node to be handled.
     *  @param state The current state of the type analyzer.
     */
    public void handle(ReturnStatement node, TypeAnalyzerState state);

    /** Handle a variable declaration fragment. Its initializer may or may not
     *  be an aliased field.
     *
     *  @param node The node to be handled.
     *  @param state The current state of the type analyzer.
     */
    public void handle(VariableDeclarationFragment node, TypeAnalyzerState state);
}
