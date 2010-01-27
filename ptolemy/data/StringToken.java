/* A token that contains a string.

 Copyright (c) 1997-2007 The Regents of the University of California.
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

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// StringToken

/**
 A token that contains a string, or more specifically, a reference
 to an instance of String.  The reference is never null, although it may
 be an empty string ("").
 Note that when this token is cloned, the clone will refer to exactly
 the same String object.  However, a String object in Java is immutable,
 so there is no risk when two tokens refer to the same string that
 one of the strings will be changed.

 @author Edward A. Lee, Neil Smyth, Steve Neuendorffer, contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh) nil token code
 */
public class StringToken extends AbstractConvertibleToken {
    /** Construct a token with an empty string.
     */
    public StringToken() {
        this("");
    }

    /** Construct a token with the specified string.
     *  If the value argument is null then the empty string is created.
     *  If the value argument is the string "nil", then the Token is not a
     *  nil token it is the string "nil".
     *  @param value The specified string.
     */
    public StringToken(String value) {
        if (value == null) {
            _value = "";
        } else {
            _value = value;
        }
        _toString = "\"" + StringUtilities.escapeString(_value) + "\"";

        /* The following logic resulted in a bit too much "smarts"
         * and toString() would not exactly get reversed by the expression
         * parser.  EAL 2/1/07.
        // If a String token is "has an embedded " quote", then
        // toString() should return "has an embedded \" quote"
        if (_value.indexOf('"') == -1) {
            _toString = "\"" + _value + "\"";
        } else {
            if (_value.indexOf("\\\"") == -1) {
                // Note that using backslashes in regexs results in
                // onset of psychosis.  If you change this, be sure to
                // test your changes.  We used to use the
                // StringUtilities.substitute() method, but this is
                // much faster for large strings.
                _toString = "\"" + _value.replaceAll("\\\"", "\\\\\"") + "\"";
            } else {
                // The string already has a \" in it.
                // 1. Substitute a special word for every instance of \"
                String backslashed = _value.replaceAll("\\\\\"",
                        "MaGiCBakSlash");

                // 2. Substitute \" for every remaining "
                String backslashed2 = backslashed.replaceAll("\"", "\\\\\"");

                // 3. Add the leading and trailing " and substitute
                //    \" for every instance of the special word
                _toString = "\""
                        + backslashed2.replaceAll("MaGiCBakSlash", "\\\\\"")
                        + "\"";
            }
        }
        */
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an instance of StringToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of StringToken,
     *  it is returned without any change.
     *  If the argument is a nil token, then
     *  {@link #NIL} is returned.
     *  Otherwise, if the argument is below StringToken in the type
     *  hierarchy, it is converted to an instance of StringToken or
     *  one of the subclasses of StringToken and returned. If none of
     *  the above condition is met, an exception is thrown.
     *  @param token The token to be converted to a StringToken.
     *  @return A StringToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static StringToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof StringToken) {
            return (StringToken) token;
        }

        if (token == null || token.isNil()) {
            return StringToken.NIL;
        }

        int compare = TypeLattice.compare(BaseType.STRING, token);

        if ((compare == CPO.LOWER) || (compare == CPO.INCOMPARABLE)) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(token, "string"));
        }

        if (token instanceof MatrixToken || token instanceof ScalarToken
                || token instanceof BooleanToken || token instanceof FixToken) {
            String str = token.toString();
            return new StringToken(str);
        }

        // The argument is below StringToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(notSupportedConversionMessage(token,
                "string"));
    }

    /** Return true if the argument is an instance of StringToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an IntToken with the same
     *  value. If either this object or the argument is nil, return
     *  false.
     */
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
            return false;
        }

        if (isNil() || ((StringToken) object).isNil()) {
            return false;
        }

        if (((StringToken) object).stringValue().equals(_value)) {
            return true;
        }

        return false;
    }

    /** Return the type of this token.
     *  @return BaseType.STRING
     */
    public Type getType() {
        return BaseType.STRING;
    }

    /** Return a hash code value for this token. This method returns the
     *  hash code of the contained string.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        return _value.hashCode();
    }

    /** Return true if the token is nil, (aka null or missing).
     *  Nil or missing tokens occur when a data source is sparsely populated.
     *  @return True if the token is the {@link #NIL} token.
     */
    public boolean isNil() {
        // We use a method here so that we can easily change how
        // we determine if a token is nil without modify lots of classes.
        // Can't use equals() here, or we'll go into an infinite loop.
        return this == StringToken.NIL;
    }

    /** Return the string that this token contains.  Note that this is
     *  different than the toString method, which returns a string expression
     *  that has double quotes around it.
     *  @return The contained string.
     */
    public String stringValue() {
        if (isNil()) {
            return super.toString();
        }
        return _value;
    }

    /** Return the value of this Token as a string.  If the value of
     *  the Token contains double quotes, then a backslash is inserted
     *  before each double quote and then double quotes are added to
     *  the beginning and the end, indicating a string constant in the
     *  expression language.
     *  @return A String.
     */
    public String toString() {
        if (isNil()) {
            return super.toString();
        }
        return _toString;
    }

    /** Return a StringToken containing an empty string, which is considered
     *  as the additive identity of string.
     *  @return A new StringToken containing an empty string.
     */
    public Token zero() {
        return new StringToken("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A token that represents a missing value.
     *  Null or missing tokens are common in analytical systems
     *  like R and SAS where they are used to handle sparsely populated data
     *  sources.  In database parlance, missing tokens are sometimes called
     *  null tokens.  Since null is a Java keyword, we use the term "nil".
     *  The toString() method on a nil token returns the string "nil".
     */
    public static final StringToken NIL = new StringToken("nil");

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.   It is assumed
     *  that the type of the argument is StringToken.
     *  @param rightArgument The token whose value we add to the value of
     *   this token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected Token _add(Token rightArgument) throws IllegalActionException {
        String result = _value + ((StringToken) rightArgument).stringValue();
        return new StringToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed
     *  that the type of the argument is StringToken.
     *  @param rightArgument The token to divide this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected Token _divide(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("divide", this,
                rightArgument));
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  StringToken.
     *  @param rightArgument The token to add to this token.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.  This parameter is ignored by this class.
     *  @return A BooleanToken containing the result.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     */
    protected BooleanToken _isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        return _isEqualTo(rightArgument);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  StringToken.
     *  @param rightArgument The token to add to this token.
     *  @return A BooleanToken containing the result.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     */
    protected BooleanToken _isEqualTo(Token rightArgument)
            throws IllegalActionException {
        StringToken convertedArgument = (StringToken) rightArgument;
        return BooleanToken.getInstance(toString().compareTo(
                convertedArgument.toString()) == 0);
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed
     *  that the type of the argument is StringToken.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result that is of the same
     *  class as this token.
     */
    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("modulo", this,
                rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is StringToken.
     *  classes to provide type specific actions for multiply.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("multiply", this,
                rightArgument));
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed
     *  that the type of the argument is StringToken.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("subtract", this,
                rightArgument));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The string contained in this token.
    private String _value;

    // The string contained in this token, with double quotes on either side.
    private String _toString;
}
