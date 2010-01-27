/* Type hierarchy of token classes.

 Copyright (c) 1997-2008 The Regents of the University of California.
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
package ptolemy.data.type;

import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// TypeLattice

/**
 Type hierarchy for token classes.
 Note that all public methods are static and synchronized.
 There is exactly one instance of the type lattice.
 Although the type lattice is constructed once and then typically
 does not change during execution, the methods need to be synchronized
 because there are various data structures used to cache results that
 are expensive to compute. These data structures do change during
 execution. Multiple threads may be accessing the type lattice
 simultaneously and modifying these data structures. To ensure
 thread safety, the methods need to be synchronized.

 <p><a href="http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html">The Java Language Spec, 3rd ed.</a>
 says:
 <blockquote>
 "The integral types are byte, short, int, and long,
 whose values are 8-bit, 16-bit, 32-bit and 64-bit
 signed two's-complement integers, respectively, and
 char, whose values are 16-bit unsigned integers
 representing UTF-16 code units (3.1).

 The floating-point types are float, whose values
 include the 32-bit IEEE 754 floating-point numbers,
 and double, whose values include the 64-bit IEEE
 754 floating-point numbers"
 </blockquote>

 Thus,
 <menu>
 <li>16-bit shorts are not lossly convertable into
 16-bit chars (unsigned integers).
 <li>32-bit ints are not lossly convertable into
 32-bit floats.
 <li>64-bit longs are not lossly convertable into
 64-bit doubles.
 </menu>

 @author Yuhong Xiong, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red
 @see ptolemy.graph.CPO
 */
public class TypeLattice {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the an instance of CPO representing the basic type
     *  lattice.
     *  @return an instance of CPO.
     */
    public static CPO basicLattice() {
        return _lattice._basicLattice;
    }

    /** Compare the types of the two specified tokens in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the the type of the
     *  first argument is lower than, equal to, higher than, or
     *  incomparable with that of the second in the type hierarchy,
     *  respectively.
     *  @param token1 a Token.
     *  @param token2 a Token.
     *  @return An integer.
     */
    public synchronized static int compare(Token token1, Token token2) {
        if ((token1 == null) || (token2 == null)) {
            throw new IllegalArgumentException(
                    "TypeLattice.compare(Token, Token): "
                            + "one or both of the argument tokens is null: "
                            + " token1 = " + token1 + ", token2 = " + token2);
        }

        return compare(token1.getType(), token2.getType());
    }

    /** Compare the types of the two specified tokens in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the the type of the
     *  first argument is lower than, equal to, higher than, or
     *  incomparable with that of the second in the type hierarchy,
     *  respectively.
     *  @param token a Token.
     *  @param type a Type.
     *  @return An integer.
     */
    public synchronized static int compare(Token token, Type type) {
        if (token == null) {
            throw new IllegalArgumentException(
                    "TypeLattice.compare(Token, Type): "
                            + "token argument is null");
        }

        return compare(token.getType(), type);
    }

    /** Compare the types of the two specified tokens in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the the type of the
     *  first argument is lower than, equal to, higher than, or
     *  incomparable with that of the second in the type hierarchy,
     *  respectively.
     *  @param token a Token.
     *  @param type a Type.
     *  @return An integer.
     */
    public synchronized static int compare(Type type, Token token) {
        if (token == null) {
            throw new IllegalArgumentException(
                    "TypeLattice.compare(Type, Token): "
                            + "token argument is null");
        }

        return compare(type, token.getType());
    }

    /** Compare two types in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the first argument
     *  is lower than, equal to, higher than, or incomparable with the
     *  second argument in the type hierarchy, respectively.
     *  @param type1 an instance of Type.
     *  @param type2 an instance of Type.
     *  @return An integer.
     */
    public synchronized static int compare(Type type1, Type type2) {
        if ((type1 == null) || (type2 == null)) {
            throw new IllegalArgumentException(
                    "TypeLattice.compare(Type, Type): "
                            + "one or both of the argument types is null: "
                            + " type1 = " + type1 + ", type2 = " + type2);
        }
        // System.out.println("compare " + type1 + " and " + type2 + " = " + _lattice.compare(type1, type2));

        int i1 = type1.getTypeHash();
        int i2 = type2.getTypeHash();

        // Uncommment the false below to measure the impact of
        // _lattice.compare() on ptolemy.data package performance... Run
        // ptolemy/data/type/test/performance.xml before and after...(zk)
        if ( /*false &&*/
        (i1 != Type.HASH_INVALID) && (i2 != Type.HASH_INVALID)) {
            if (_getCachedTypeComparisonResult(i1, i2) == Type.HASH_INVALID) {
                _setCachedTypeComparisonResult(i1, i2, _lattice.compare(type1,
                        type2));
            }

            return _getCachedTypeComparisonResult(i1, i2);
        }

        return _lattice.compare(type1, type2);
    }

    /** Return the an instance of CPO representing the infinite type
     *  lattice.
     *  @return an instance of CPO.
     */
    public static CPO lattice() {
        return _lattice;
    }

    /** Return the least upper bound of the two given types.
     *  @param type1 The first given type.
     *  @param type2 The second given type.
     *  @return The least upper bound of type1 and type2.
     */
    public synchronized static Type leastUpperBound(Type type1, Type type2) {
        return (Type) _lattice.leastUpperBound(type1, type2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the result for the types that have the given two
     * indexes as hashes.
     */
    private static final int _getCachedTypeComparisonResult(int index1,
            int index2) {
        return _compareCache[index1][index2];
    }

    /** Set the result for the types that have the given two
     *  indexes as hashes.
     */
    private static final void _setCachedTypeComparisonResult(int index1,
            int index2, int value) {
        _compareCache[index1][index2] = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    // The infinite type lattice
    private static class TheTypeLattice implements CPO {
        /** Return the bottom element of the type lattice, which is UNKNOWN.
         *  @return The Type object representing UNKNOWN.
         */
        public Object bottom() {
            synchronized (TypeLattice.class) {
                return _basicLattice.bottom();
            }
        }

        /** Compare two types in the type lattice. The arguments must be
         *  instances of Type, otherwise an exception will be thrown.
         *  This method returns one of ptolemy.graph.CPO.LOWER,
         *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
         *  ptolemy.graph.CPO.INCOMPARABLE, indicating the first argument
         *  is lower than, equal to, higher than, or incomparable with the
         *  second argument in the type hierarchy, respectively.
         *  @param t1 an instance of Type.
         *  @param t2 an instance of Type.
         *  @return An integer.
         *  @exception IllegalArgumentException If one or both arguments
         *   are not instances of Type.
         */
        public int compare(Object t1, Object t2) {
            synchronized (TypeLattice.class) {
                if (!(t1 instanceof Type) || !(t2 instanceof Type)) {
                    throw new IllegalArgumentException(
                            "TheTypeLattice.compare: "
                                    + "Arguments are not instances of Type: "
                                    + " type1 = " + t1 + ", type2 = " + t2);
                }

                Type ct1 = (Type) t1;
                Type ct2 = (Type) t2;

                Type t1Rep = _toRepresentative(ct1);
                Type t2Rep = _toRepresentative(ct2);

                if (t1Rep.equals(t2Rep) && t1Rep instanceof StructuredType) {
                    return ((StructuredType) t1)._compare((StructuredType) t2);
                } else if (t1Rep instanceof ArrayType
                        && !(t2Rep instanceof ArrayType)
                        && !t2.equals(BaseType.UNKNOWN)
                        && !t2.equals(BaseType.GENERAL)
                        && !t2.equals(BaseType.ARRAY_BOTTOM)) {
                    // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                    ArrayType arrayType = (ArrayType) t1;
                    if (arrayType.hasKnownLength() && arrayType.length() != 1
                            && !t2.equals(BaseType.GENERAL)) {
                        // If we have a Const with {1,2,3} -> Display
                        // then we used to fail here.
                        return INCOMPARABLE;
                    }
                    int elementComparison = compare(((ArrayType) ct1)
                            .getElementType(), t2Rep);
                    if (elementComparison == SAME
                            || elementComparison == HIGHER) {
                        return HIGHER;
                    } else {
                        if (t2Rep == BaseType.GENERAL) {
                            return LOWER;
                        } else {
                            return INCOMPARABLE;
                        }
                    }
                } else if (t2Rep instanceof ArrayType
                        && !(t1Rep instanceof ArrayType)
                        && !t1.equals(BaseType.UNKNOWN)
                        && !t1.equals(BaseType.GENERAL)
                        && !t1.equals(BaseType.ARRAY_BOTTOM)) {
                    // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                    ArrayType arrayType = (ArrayType) t2;
                    if (arrayType.hasKnownLength() && arrayType.length() != 1
                            && !t1.equals(BaseType.GENERAL)) {
                        return INCOMPARABLE;
                    }
                    int elementComparison = compare(((ArrayType) ct2)
                            .getElementType(), t1Rep);
                    if (elementComparison == SAME
                            || elementComparison == HIGHER) {
                        return LOWER;
                    } else {
                        if (t1Rep == BaseType.GENERAL) {
                            return HIGHER;
                        } else {
                            return INCOMPARABLE;
                        }
                    }
                } else if (_basicLattice.containsNodeWeight(t1Rep)
                        && _basicLattice.containsNodeWeight(t2Rep)) {
                    // Both are neither the same structured type, nor an array
                    // and non-array pair, so their type relation is defined
                    // by the basic lattice.
                    return _basicLattice.compare(t1Rep, t2Rep);
                } else {
                    // Both arguments are not the same structured type, and
                    // at least one is user defined, so their relation is
                    // rather simple.
                    if (t1Rep.equals(t2Rep)) {
                        return SAME;
                    } else if ((t1Rep == BaseType.UNKNOWN)
                            || (t2Rep == BaseType.GENERAL)) {
                        return LOWER;
                    } else if ((t2Rep == BaseType.UNKNOWN)
                            || (t1Rep == BaseType.GENERAL)) {
                        return HIGHER;
                    } else {
                        return INCOMPARABLE;
                    }
                }
            }
        }

        /** Throw an exception. This operation is not supported since the
         *  type lattice is infinite,
         *  @exception UnsupportedOperationException Always thrown.
         */
        public Object[] downSet(Object e) {
            throw new UnsupportedOperationException(
                    "TheTypeLattice.downSet(): operation not supported for "
                            + "the type lattice.");
        }

        /** Return the greatest lower bound of two types.
         *  @param t1 an instance of Type.
         *  @param t2 an instance of Type.
         *  @return an instance of Type.
         *  @exception IllegalArgumentException If one or both of the
         *   specified arguments are not instances of Type.
         */
        public Object greatestLowerBound(Object t1, Object t2) {
            synchronized (TypeLattice.class) {
                if (!(t1 instanceof Type) || !(t2 instanceof Type)) {
                    throw new IllegalArgumentException(
                            "TheTypeLattice.greatestLowerBound: "
                                    + "Arguments are not instances of Type.");
                }

                Type ct1 = (Type) t1;
                Type ct2 = (Type) t2;

                Type t1Rep = _toRepresentative(ct1);
                Type t2Rep = _toRepresentative(ct2);

                if (t1Rep.equals(t2Rep) && t1Rep instanceof StructuredType) {
                    return ((StructuredType) t1)
                            ._greatestLowerBound((StructuredType) t2);
                } else if (t1Rep instanceof ArrayType
                        && !(t2Rep instanceof ArrayType)
                        && !t2.equals(BaseType.UNKNOWN)
                        && !t2.equals(BaseType.ARRAY_BOTTOM)) {
                    // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                    ArrayType arrayType = (ArrayType) t1;
                    int elementComparison = compare(((ArrayType) ct1)
                            .getElementType(), t2Rep);
                    if (elementComparison == SAME
                            || elementComparison == HIGHER) {
                        if (arrayType.hasKnownLength()
                                && arrayType.length() != 1) {
                            return BaseType.UNKNOWN;
                        } else {
                            return t2;
                        }
                    } else {
                        if (t2Rep == BaseType.GENERAL) {
                            return t1;
                        } else {
                            // INCOMPARABLE
                            if (_basicLattice.containsNodeWeight(t2Rep)) {
                                return _basicLattice.greatestLowerBound(t1Rep,
                                        t2Rep);
                            } else {
                                // t2 is a user type (has no representative in the
                                // basic lattice). Arrays of this type are not supported.
                                return BaseType.UNKNOWN;
                            }
                        }
                    }
                } else if (t2Rep instanceof ArrayType
                        && !(t1Rep instanceof ArrayType)
                        && !t1.equals(BaseType.UNKNOWN)
                        && !t1.equals(BaseType.ARRAY_BOTTOM)) {
                    // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                    ArrayType arrayType = (ArrayType) t2;
                    int elementComparison = compare(((ArrayType) ct2)
                            .getElementType(), t1Rep);
                    if (elementComparison == SAME
                            || elementComparison == HIGHER) {
                        if (arrayType.hasKnownLength()
                                && arrayType.length() != 1) {
                            return BaseType.UNKNOWN;
                        } else {
                            return t1;
                        }
                    } else {
                        if (t1Rep == BaseType.GENERAL) {
                            return t2;
                        } else {
                            // INCOMPARABLE
                            if (_basicLattice.containsNodeWeight(t1Rep)) {
                                return _basicLattice.greatestLowerBound(t1Rep,
                                        t2Rep);
                            } else {
                                // t1 is a user type (has no representative in the
                                // basic lattice). Arrays of this type are not supported.
                                return BaseType.UNKNOWN;
                            }
                        }
                    }
                } else if (_basicLattice.containsNodeWeight(t1Rep)
                        && _basicLattice.containsNodeWeight(t2Rep)) {
                    // Both are neither the same structured type, nor an array
                    // and non-array pair, so their type relation is defined
                    // by the basic lattice.
                    int relation = _basicLattice.compare(t1Rep, t2Rep);

                    if (relation == SAME) {
                        return t1;
                    } else if (relation == LOWER) {
                        return t1;
                    } else if (relation == HIGHER) {
                        return t2;
                    } else { // INCOMPARABLE
                        return _basicLattice.greatestLowerBound(t1Rep, t2Rep);
                    }
                } else {
                    // Both arguments are not the same structured type, and
                    // at least one is user defined, so their relation is
                    // rather simple.
                    if (t1Rep.equals(t2Rep)) {
                        return t1;
                    } else if ((t1Rep == BaseType.UNKNOWN)
                            || (t2Rep == BaseType.GENERAL)) {
                        return t1;
                    } else if ((t2Rep == BaseType.UNKNOWN)
                            || (t1Rep == BaseType.GENERAL)) {
                        return t2;
                    } else {
                        return bottom();
                    }
                }
            }
        }

        /** Return the greatest lower bound of a subset.
         *  @param subset an array of Types.
         *  @return an instance of Type.
         */
        public Object greatestLowerBound(Object[] subset) {
            synchronized (TypeLattice.class) {
                if (subset.length == 0) {
                    return BaseType.GENERAL;
                }

                Object glb = subset[0];

                // start looping from index 0 so that subset[0] is checked for
                // possible exception, in case the subset has only one element.
                for (int i = 0; i < subset.length; i++) {
                    glb = greatestLowerBound(glb, subset[i]);
                }

                return glb;
            }
        }

        /** Return the greatest type of a set of types, or null if the
         *  greatest one does not exist.
         *  @param subset an array of Types.
         *  @return A Type or null.
         */
        public Object greatestElement(Object[] subset) {
            synchronized (TypeLattice.class) {
                // Compare each element with all of the other elements to search
                // for the greatest one. This is a simple, brute force algorithm,
                // but may be inefficient. A more efficient one is used in
                // the graph package, but more complex.
                for (int i = 0; i < subset.length; i++) {
                    boolean isGreatest = true;

                    for (int j = 0; j < subset.length; j++) {
                        int result = compare(subset[i], subset[j]);

                        if ((result == CPO.LOWER)
                                || (result == CPO.INCOMPARABLE)) {
                            isGreatest = false;
                            break;
                        }
                    }

                    if (isGreatest == true) {
                        return subset[i];
                    }
                }
                // FIXME: Shouldn't this return GENERAL?
                return null;
            }
        }

        /** Return true.
         *  @return true.
         */
        public boolean isLattice() {
            return true;
        }

        /** Return the least type of a set of types, or null if the
         *  least one does not exist.
         *  @param subset an array of Types.
         *  @return A Type or null.
         */
        public Object leastElement(Object[] subset) {
            synchronized (TypeLattice.class) {
                // Compare each element with all of the other elements to search
                // for the least one. This is a simple, brute force algorithm,
                // but may be inefficient. A more efficient one is used in
                // the graph package, but more complex.
                for (int i = 0; i < subset.length; i++) {
                    boolean isLeast = true;

                    for (int j = 0; j < subset.length; j++) {
                        int result = compare(subset[i], subset[j]);

                        if ((result == CPO.HIGHER)
                                || (result == CPO.INCOMPARABLE)) {
                            isLeast = false;
                            break;
                        }
                    }

                    if (isLeast == true) {
                        return subset[i];
                    }
                }
                // FIXME: Shouldn't thir return bottom?
                return null;
            }
        }

        /** Return the least upper bound of two types.
         *  @param t1 an instance of Type.
         *  @param t2 an instance of Type.
         *  @return an instance of Type.
         */
        public Object leastUpperBound(Object t1, Object t2) {
            synchronized (TypeLattice.class) {
                if (!(t1 instanceof Type) || !(t2 instanceof Type)) {
                    throw new IllegalArgumentException(
                            "TheTypeLattice.leastUpperBound: "
                                    + "Arguments are not instances of Type.");
                }

                // System.out.println("LUB of " + t1 + " and " + t2);
                Type ct1 = (Type) t1;
                Type ct2 = (Type) t2;

                Type t1Rep = _toRepresentative(ct1);
                Type t2Rep = _toRepresentative(ct2);

                if (t1Rep.equals(t2Rep) && t1Rep instanceof StructuredType) {
                    return ((StructuredType) t1)
                            ._leastUpperBound((StructuredType) t2);
                } else if (t1Rep instanceof ArrayType
                        && !(t2Rep instanceof ArrayType)
                        && !t2.equals(BaseType.UNKNOWN)
                        && !t2.equals(BaseType.GENERAL)
                        && !t2.equals(BaseType.ARRAY_BOTTOM)) {
                    // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                    ArrayType arrayType = (ArrayType) t1;
                    Type elementType = ((ArrayType) ct1).getElementType();
                    int elementComparison = compare(elementType, t2Rep);
                    if (elementComparison == SAME
                            || elementComparison == HIGHER) {
                        if (arrayType.hasKnownLength()
                                && arrayType.length() != 1) {
                            // Least upper bound is unsized type.
                            return new ArrayType(elementType);
                        } else {
                            return t1;
                        }
                    } else {
                        if (t2Rep == BaseType.GENERAL) {
                            return t2;
                        } else {
                            // INCOMPARABLE
                            if (_basicLattice.containsNodeWeight(t2Rep)
                                    && _basicLattice
                                            .containsNodeWeight(elementType)) {
                                // The least upper bound is an array of the LUB
                                // of t2Rep and the element type of t1.
                                return new ArrayType((Type) _basicLattice
                                        .leastUpperBound(elementType, t2Rep));
                            } else {
                                // t2 is a user type (has no representative in the
                                // basic lattice). Arrays of this type are not supported.
                                return BaseType.GENERAL;
                            }
                        }
                    }
                } else if (t2Rep instanceof ArrayType
                        && !(t1Rep instanceof ArrayType)
                        && !t1.equals(BaseType.UNKNOWN)
                        && !t1.equals(BaseType.GENERAL)
                        && !t1.equals(BaseType.ARRAY_BOTTOM)) {
                    // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                    ArrayType arrayType = (ArrayType) t2;
                    Type elementType = ((ArrayType) ct2).getElementType();
                    int elementComparison = compare(elementType, t1Rep);
                    if (elementComparison == SAME
                            || elementComparison == HIGHER) {
                        if (arrayType.hasKnownLength()
                                && arrayType.length() != 1) {
                            // Least upper bound is unsized type.
                            return new ArrayType(elementType);
                        } else {
                            return t2;
                        }
                    } else {
                        if (t1Rep == BaseType.GENERAL) {
                            return t1;
                        } else {
                            // INCOMPARABLE
                            if (_basicLattice.containsNodeWeight(t1Rep)
                                    && _basicLattice
                                            .containsNodeWeight(elementType)) {
                                // The least upper bound is an array of the LUB
                                // of t2Rep and the element type of t1.
                                return new ArrayType((Type) _basicLattice
                                        .leastUpperBound(elementType, t1Rep));
                            } else {
                                // t1 is a user type (has no representative in the
                                // basic lattice). Arrays of this type are not supported.
                                return BaseType.GENERAL;
                            }
                        }
                    }
                } else if (_basicLattice.containsNodeWeight(t1Rep)
                        && _basicLattice.containsNodeWeight(t2Rep)) {
                    // Both are neither the same structured type, nor an array
                    // and non-array pair, so their type relation is defined
                    // by the basic lattice.
                    int relation = _basicLattice.compare(t1Rep, t2Rep);

                    if (relation == SAME) {
                        return t1;
                    } else if (relation == LOWER) {
                        return t2;
                    } else if (relation == HIGHER) {
                        return t1;
                    } else { // INCOMPARABLE
                        return _basicLattice.leastUpperBound(t1Rep, t2Rep);
                    }
                } else {
                    // Both arguments are not the same structured type, and
                    // at least one is user defined, so their relation is
                    // rather simple.
                    if (t1Rep.equals(t2Rep)) {
                        return t1;
                    } else if ((t1Rep == BaseType.UNKNOWN)
                            || (t2Rep == BaseType.GENERAL)) {
                        return t2;
                    } else if ((t2Rep == BaseType.UNKNOWN)
                            || (t1Rep == BaseType.GENERAL)) {
                        return t1;
                    } else {
                        return top();
                    }
                }
            }
        }

        /** Return the least upper bound of a subset.
         *  @param subset an array of Types.
         *  @return an instance of Type.
         */
        public Object leastUpperBound(Object[] subset) {
            synchronized (TypeLattice.class) {
                if (subset.length == 0) {
                    return BaseType.UNKNOWN;
                }

                Object lub = subset[0];

                // start looping from index 0 so that subset[0] is checked for
                // possible exception, in case the subset has only one element.
                for (int i = 0; i < subset.length; i++) {
                    lub = leastUpperBound(lub, subset[i]);
                }

                return lub;
            }
        }

        /** Return the top element of the type lattice, which is General.
         *  @return The Type object representing General.
         */
        public Object top() {
            synchronized (TypeLattice.class) {
                return _basicLattice.top();
            }
        }

        /** Throw an exception. This operation is not supported since the
         *  type lattice is infinite,
         *  this operation is not supported.
         *  @exception UnsupportedOperationException Always thrown.
         */
        public Object[] upSet(Object e) {
            throw new UnsupportedOperationException(
                    "TheTypeLattice.upSet(): operation not supported for "
                            + "the type lattice.");
        }

        ///////////////////////////////////////////////////////////////
        ////                    private constructor                ////
        // the constructor is private so only the outer class can use it.
        private TheTypeLattice() {
            synchronized (TypeLattice.class) {
                _basicLattice = new DirectedAcyclicGraph();

                StructuredType arrayRep = (new ArrayType(BaseType.UNKNOWN))
                        ._getRepresentative();

                String[] labels = new String[0];
                Type[] types = new Type[0];
                StructuredType recordRep = (new RecordType(labels, types))
                        ._getRepresentative();
                StructuredType unionRep = (new UnionType(labels, types))
                        ._getRepresentative();

                /*StructuredType functionRep = */new ptolemy.data.type.FunctionType(
                        new ptolemy.data.type.Type[0],
                        ptolemy.data.type.BaseType.UNKNOWN)
                        ._getRepresentative();

                _basicLattice.addNodeWeight(BaseType.BOOLEAN);
                _basicLattice.addNodeWeight(BaseType.BOOLEAN_MATRIX);
                _basicLattice.addNodeWeight(BaseType.UNSIGNED_BYTE);
                _basicLattice.addNodeWeight(BaseType.COMPLEX);
                _basicLattice.addNodeWeight(BaseType.COMPLEX_MATRIX);
                _basicLattice.addNodeWeight(BaseType.DOUBLE);
                _basicLattice.addNodeWeight(BaseType.DOUBLE_MATRIX);
                _basicLattice.addNodeWeight(BaseType.UNSIZED_FIX);
                _basicLattice.addNodeWeight(BaseType.SIZED_FIX);
                _basicLattice.addNodeWeight(BaseType.FIX_MATRIX);
                _basicLattice.addNodeWeight(BaseType.FLOAT);
                _basicLattice.addNodeWeight(BaseType.INT);
                _basicLattice.addNodeWeight(BaseType.INT_MATRIX);
                _basicLattice.addNodeWeight(BaseType.LONG);
                _basicLattice.addNodeWeight(BaseType.LONG_MATRIX);
                _basicLattice.addNodeWeight(BaseType.MATRIX);
                _basicLattice.addNodeWeight(BaseType.UNKNOWN);
                // NOTE: Removed NUMERICAL from the type lattice, EAL 7/22/06.
                // _basicLattice.addNodeWeight(BaseType.NUMERICAL);
                _basicLattice.addNodeWeight(BaseType.OBJECT);

                // Strange bug here, see moml/test/aJVMBug.xml
                // and ptdevel email from 7/21.
                //_basicLattice.addNodeWeight(BaseType.ACTOR);
                _basicLattice.addNodeWeight(ActorToken.TYPE); 

                _basicLattice.addNodeWeight(BaseType.XMLTOKEN);
                _basicLattice.addNodeWeight(BaseType.SCALAR);
                _basicLattice.addNodeWeight(BaseType.SHORT);
                _basicLattice.addNodeWeight(BaseType.STRING);
                _basicLattice.addNodeWeight(BaseType.EVENT);
                _basicLattice.addNodeWeight(BaseType.GENERAL);
                _basicLattice.addNodeWeight(BaseType.PETITE);
                _basicLattice.addNodeWeight(BaseType.NIL);

                _basicLattice.addNodeWeight(arrayRep);
                _basicLattice.addNodeWeight(BaseType.ARRAY_BOTTOM);
                _basicLattice.addNodeWeight(recordRep);
                _basicLattice.addNodeWeight(unionRep);

                _basicLattice.addEdge(BaseType.XMLTOKEN, BaseType.GENERAL);
                _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.XMLTOKEN);
                _basicLattice.addEdge(BaseType.OBJECT, BaseType.GENERAL);
                _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.OBJECT);

                // More of the strange jvm bug, see above.
                //_basicLattice.addEdge(BaseType.ACTOR, BaseType.GENERAL);
                //_basicLattice.addEdge(BaseType.UNKNOWN, BaseType.ACTOR);
                _basicLattice.addEdge(ActorToken.TYPE, BaseType.GENERAL);
                _basicLattice.addEdge(BaseType.UNKNOWN, ActorToken.TYPE);

                _basicLattice.addEdge(BaseType.STRING, BaseType.GENERAL);
                _basicLattice.addEdge(BaseType.MATRIX, BaseType.STRING);
                _basicLattice.addEdge(BaseType.BOOLEAN_MATRIX, BaseType.MATRIX);
                _basicLattice
                        .addEdge(BaseType.BOOLEAN, BaseType.BOOLEAN_MATRIX);
                _basicLattice.addEdge(BaseType.BOOLEAN, BaseType.SCALAR);
                _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.BOOLEAN);

                // NOTE: Removed NUMERICAL from the type lattice, EAL 7/22/06.
                // _basicLattice.addEdge(BaseType.NUMERICAL, BaseType.MATRIX);
                _basicLattice.addEdge(BaseType.FIX_MATRIX, BaseType.MATRIX);
                _basicLattice.addEdge(BaseType.SCALAR, BaseType.MATRIX);
                _basicLattice.addEdge(BaseType.LONG_MATRIX, BaseType.MATRIX);
                _basicLattice.addEdge(BaseType.COMPLEX_MATRIX, BaseType.MATRIX);

                _basicLattice
                        .addEdge(BaseType.UNSIZED_FIX, BaseType.FIX_MATRIX);
                _basicLattice.addEdge(BaseType.SIZED_FIX, BaseType.UNSIZED_FIX);
                _basicLattice.addEdge(BaseType.UNSIZED_FIX, BaseType.SCALAR);
                _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.SIZED_FIX);
                _basicLattice.addEdge(BaseType.LONG, BaseType.SCALAR);
                _basicLattice.addEdge(BaseType.LONG, BaseType.LONG_MATRIX);
                _basicLattice
                        .addEdge(BaseType.INT_MATRIX, BaseType.LONG_MATRIX);
                _basicLattice.addEdge(BaseType.INT, BaseType.LONG);
                _basicLattice.addEdge(BaseType.INT, BaseType.INT_MATRIX);
                _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.UNSIGNED_BYTE);
                _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.PETITE);
                _basicLattice.addEdge(BaseType.INT_MATRIX,
                        BaseType.DOUBLE_MATRIX);
                _basicLattice.addEdge(BaseType.DOUBLE_MATRIX,
                        BaseType.COMPLEX_MATRIX);
                _basicLattice.addEdge(BaseType.DOUBLE, BaseType.DOUBLE_MATRIX);
                _basicLattice.addEdge(BaseType.INT, BaseType.DOUBLE);
                _basicLattice.addEdge(BaseType.DOUBLE, BaseType.SCALAR);

                _basicLattice.addEdge(BaseType.PETITE, BaseType.DOUBLE);
                _basicLattice.addEdge(BaseType.COMPLEX, BaseType.SCALAR);
                _basicLattice
                        .addEdge(BaseType.COMPLEX, BaseType.COMPLEX_MATRIX);

                _basicLattice.addEdge(BaseType.DOUBLE, BaseType.COMPLEX);
                _basicLattice.addEdge(BaseType.UNSIGNED_BYTE, BaseType.SHORT);
                _basicLattice.addEdge(BaseType.SHORT, BaseType.INT);
                _basicLattice.addEdge(BaseType.SHORT, BaseType.FLOAT);
                _basicLattice.addEdge(BaseType.FLOAT, BaseType.DOUBLE);

                _basicLattice.addEdge(BaseType.EVENT, BaseType.GENERAL);
                _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.EVENT);

                _basicLattice.addEdge(arrayRep, BaseType.STRING);
                _basicLattice.addEdge(BaseType.ARRAY_BOTTOM, arrayRep);
                _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.ARRAY_BOTTOM);

                _basicLattice.addEdge(recordRep, BaseType.STRING);
                _basicLattice.addEdge(BaseType.UNKNOWN, recordRep);

                _basicLattice.addEdge(unionRep, BaseType.GENERAL);
                _basicLattice.addEdge(BaseType.UNKNOWN, unionRep);

                _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.NIL);
                _basicLattice.addEdge(BaseType.NIL, BaseType.BOOLEAN);
                // NOTE: Redundant, given edge to UnsignedByte
                // _basicLattice.addEdge(BaseType.NIL, BaseType.DOUBLE);
                // _basicLattice.addEdge(BaseType.NIL, BaseType.LONG);
                // _basicLattice.addEdge(BaseType.NIL, BaseType.INT);
                _basicLattice.addEdge(BaseType.NIL, BaseType.UNSIGNED_BYTE);

                // FIXME: Replace this with an assert when we move to 1.5
                if (!_basicLattice.isLattice()) {
                    throw new InternalErrorException("TheTypeLattice: The "
                            + "type hierarchy is not a lattice.");
                }
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                      private methods                  ////
        // If the argument is a structured type, return its representative;
        // otherwise, return the argument. In the latter case, the argument
        // is either a base type or a user defined type that is not a
        // structured type.
        private Type _toRepresentative(Type t) {
            if (t instanceof StructuredType) {
                return ((StructuredType) t)._getRepresentative();
            } else {
                return t;
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                     private variables                 ////
        private DirectedAcyclicGraph _basicLattice;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The infinite type lattice. */
    private static TheTypeLattice _lattice = new TheTypeLattice();

    /** The result cache for parts of the type lattice. */
    private static int[][] _compareCache;

    static {
        synchronized (TypeLattice.class) {
            _compareCache = new int[Type.HASH_MAX + 1][Type.HASH_MAX + 1];

            for (int i = 0; i <= Type.HASH_MAX; i++) {
                for (int j = 0; j <= Type.HASH_MAX; j++) {
                    _compareCache[i][j] = Type.HASH_INVALID;
                }
            }
        }
    }
}
