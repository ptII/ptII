/* Tokens that contain java.security.Keys

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.actor.lib.security;

import java.io.Serializable;
import java.security.Key;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// KeyToken

/**
 Tokens that contain java.security.Keys.

 @author Christopher Hylands Brooks, Based on TestToken by Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class KeyToken extends Token {
    /** Construct a token with a specified java.security.Key.
     *  @param value The specified java.security.Key type to construct
     *  the token with.
     */
    public KeyToken(Key value) {
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the type of this token.
     *  @return {@link #KEY}, the least upper bound of all the cryptographic
     *  key types.
     */
    public Type getType() {
        return KEY;
    }

    /** Return the java.security.Key.
     *  @return The java.security.Key that this Token was created with.
     */
    public java.security.Key getValue() {
        return _value;
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  Two KeyTokens are considered equals if the strings
     *  that name their corresponding algorithms and formats are the same
     *  and the byte arrays that contain the encoding have the same contents.
     *  Consult the java.security.Key documentation for the meaning of these
     *  terms.  If the value of this token or the value of the rightArgument
     *  token is null, then we return False.
     *
     *  @param rightArgument The Token to test against.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @return A boolean token that contains the value true if the
     *  algorithms, formats and encodings are the same.
     */
    public final BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        java.security.Key rightKey = ((KeyToken) rightArgument).getValue();
        java.security.Key leftKey = getValue();

        if ((rightKey == null) || (leftKey == null)) {
            return BooleanToken.FALSE;
        }

        if (!rightKey.getAlgorithm().equals(leftKey.getAlgorithm())) {
            return BooleanToken.FALSE;
        }

        if (!rightKey.getFormat().equals(leftKey.getFormat())) {
            return BooleanToken.FALSE;
        }

        byte[] rightEncoded = rightKey.getEncoded();
        byte[] leftEncoded = leftKey.getEncoded();

        if (rightEncoded.length != leftEncoded.length) {
            return BooleanToken.FALSE;
        }

        for (int i = 0; i < rightEncoded.length; i++) {
            if (rightEncoded[i] != leftEncoded[i]) {
                return BooleanToken.FALSE;
            }
        }

        return BooleanToken.TRUE;
    }

    /** Return a String representation of the KeyToken.
     *  @return A String representation of the KeyToken that includes
     *  the value of the algorithm, format and encoding.
     */
    public String toString() {
        // FIXME: we print this token in a format similar to RecordToken.
        // Perhaps this token should be a RecordToken?
        // FIXME: Should we have a constructor that reads in string?
        StringBuffer result = new StringBuffer("{ algorithm = "
                + _value.getAlgorithm() + ", format = " + _value.getFormat()
                + ", encoded = ");
        result.append(" Encoded: ");

        byte[] encoded = _value.getEncoded();

        for (int i = 0; i < (encoded.length - 1); i++) {
            result.append(" " + encoded[i] + ",");
        }

        result.append(" " + encoded[encoded.length - 1] + ")");
        return result.toString();
    }

    /** The cryptographic key type.
     */
    public static class KeyType implements Cloneable, Type, Serializable {
        ///////////////////////////////////////////////////////////////////
        ////                         constructors                      ////
        // The constructor is private to make a type safe enumeration.
        // We could extend BaseType, yet the BaseType(Class, String)
        // Constructor is private.
        private KeyType() {
            super();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return a new type which represents the type that results from
         *  adding a token of this type and a token of the given argument
         *  type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type add(Type rightArgumentType) {
            return this;
        }

        /** Return this, that is, return the reference to this object.
         *  @return A KeyType
         */
        public Object clone() {
            // FIXME: Note that we do not call super.clone() here.  Is that right?
            return this;
        }

        /** Convert the specified token to a token having the type
         *  represented by this object.
         *  @param token A token.
         *  @return A token.
         *  @exception IllegalActionException If lossless conversion cannot
         *   be done.
         */
        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof KeyToken) {
                return token;
            } else {
                throw new IllegalActionException("Attempt to convert token "
                        + token + " into a Key token, which is not possible.");
            }
        }

        /** Return a new type which represents the type that results from
         *  dividing a token of this type and a token of the given
         *  argument type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type divide(Type rightArgumentType) {
            return this;
        }

        /** Return the class for tokens that this basetype represents.
         *  @return the class for tokens that this basetype represents.
         */
        public Class getTokenClass() {
            return KeyToken.class;
        }

        /** Return true if this type does not correspond to a single token
         *  class.  This occurs if the type is not instantiable, or it
         *  represents either an abstract base class or an interface.
         *  @return Always return false, this token is instantiable.
         */
        public boolean isAbstract() {
            return false;
        }

        /** Test if the argument type is compatible with this type.
         *  The method returns true if this type is UNKNOWN, since any type
         *  is a substitution instance of it. If this type is not UNKNOWN,
         *  this method returns true if the argument type is less than or
         *  equal to this type in the type lattice, and false otherwise.
         *  @param type An instance of Type.
         *  @return True if the argument type is compatible with this type.
         */
        public boolean isCompatible(Type type) {
            return type == this;
        }

        /** Test if this Type is UNKNOWN.
         *  @return True if this Type is not UNKNOWN; false otherwise.
         */
        public boolean isConstant() {
            return true;
        }

        /** Return this type's node index in the (constant) type lattice.
         * @return this type's node index in the (constant) type lattice.
         */
        public int getTypeHash() {
            return Type.HASH_INVALID;
        }

        /** Determine if this type corresponds to an instantiable token
         *  classes. A BaseType is instantiable if it does not correspond
         *  to an abstract token class, or an interface, or UNKNOWN.
         *  @return True if this type is instantiable.
         */
        public boolean isInstantiable() {
            return true;
        }

        /** Return true if the argument is a
         *  substitution instance of this type.
         *  @param type A Type.
         *  @return True if this type is UNKNOWN; false otherwise.
         */
        public boolean isSubstitutionInstance(Type type) {
            return this == type;
        }

        /** Return a new type which represents the type that results from
         *  moduloing a token of this type and a token of the given
         *  argument type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type modulo(Type rightArgumentType) {
            return this;
        }

        /** Return a new type which represents the type that results from
         *  multiplying a token of this type and a token of the given
         *  argument type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type multiply(Type rightArgumentType) {
            return this;
        }

        /** Return the type of the multiplicative identity for elements of
         *  this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type one() {
            return this;
        }

        /** Return a new type which represents the type that results from
         *  subtracting a token of this type and a token of the given
         *  argument type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type subtract(Type rightArgumentType) {
            return this;
        }

        /** Return the string representation of this type.
         *  @return A String.
         */
        public String toString() {
            return "Key";
        }

        /** Return the type of the additive identity for elements of
         *  this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type zero() {
            return this;
        }
    }

    /** The Key type: the least upper bound of all the cryptographic
     *  key types.
     */
    public static final Type KEY = new KeyType();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The java.security.Key */
    private java.security.Key _value;
}
