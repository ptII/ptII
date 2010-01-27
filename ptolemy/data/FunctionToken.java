/* Token that contains a function.

 Copyright (c) 2002-2010 The Regents of the University of California.
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
package ptolemy.data;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// FunctionToken

/**
 A token that contains a function. The function takes a fixed number of
 arguments, supplied as a list of tokens.

 Currently, no operations between function tokens (add, multiply, etc.)
 are supported.

 @author Xiaojun Liu, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 */
public class FunctionToken extends Token {
    /** Create a new FunctionToken that applies the given function.
     *  The type of the function token will be the same as the given
     *  type.
     *  @param f The function.
     *  @param type The function type.
     */
    public FunctionToken(Function f, FunctionType type) {
        _function = f;
        _type = type;
    }

    /** Create a new FunctionToken from the given string.
     *  @param init The initialization string, for example
     *  <code>function(x,y) 4+x+y</code>.
     *  @exception IllegalActionException If an error occurs, or the
     *  string cannot be parsed into a function.
     */
    public FunctionToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        ParseTreeTypeInference inference = new ParseTreeTypeInference();
        inference.inferTypes(tree);

        Token token = (new ParseTreeEvaluator()).evaluateParseTree(tree);

        if (token instanceof FunctionToken) {
            _function = ((FunctionToken) token)._function;
            _type = ((FunctionToken) token)._type;
        } else {
            throw new IllegalActionException("A function token cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply this function to the given list of arguments.
     *  @param args the arguments to which the function is applied
     *  @return The results of applying the arguments to this function.
     *  @exception IllegalActionException If the arguments are not
     *  compatible with this function, or an error occurs during
     *  evaluation.
     */
    public Token apply(Token[] args) throws IllegalActionException {
        return _function.apply(args);
    }

    /** Return the function of this token.
     *  @return A Function.
     */
    public Function getFunction() {
        return _function;
    }

    /** Return the number of arguments of the function.
     *  @return The number of arguments of the function.
     */
    public int getNumberOfArguments() {
        return _function.getNumberOfArguments();
    }

    /** Return the type of this token.
     *  @return A FunctionType.
     */
    public Type getType() {
        return _type;
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  For function tokens, checking for closeness is the same
     *  as checking for equality.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon This argument is ignored in this method.
     *  @return A true-valued token if the first argument is equal to
     *  this token.
     */
    public BooleanToken isCloseTo(Token rightArgument, double epsilon) {
        return isEqualTo(rightArgument);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  Two function tokens are equal if they correspond to the
     *  same expression, under renaming of any bound variables.
     *  @param rightArgument The token to compare to this token.
     *  @return A token containing true if the value element of the first
     *  argument is equal to the value of this token.
     */
    public BooleanToken isEqualTo(Token rightArgument) {
        FunctionToken convertedArgument = (FunctionToken) rightArgument;
        return BooleanToken.getInstance(convertedArgument._function
                .isCongruent(_function));
    }

    /** Return a String representation of this function.
     */
    public String toString() {
        return _function.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    // The object that implements the function.
    private Function _function;

    // The type of this function.
    private FunctionType _type;
}
