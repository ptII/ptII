/* Class providing additional functions in the Ptolemy II expression language.

 Copyright (c) 1998-2009 The Regents of the University of California.
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
package ptolemy.data.expr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import ptolemy.data.ActorToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongMatrixToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.UnsizedArrayToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.ComplexMatrixMath;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// UtilityFunctions

/**
 This class provides additional functions for use in the Ptolemy II
 expression language.  All of the methods in this class are static
 and return an instance of Token.  The expression language identifies
 the appropriate method to use by using reflection, matching the
 types of the arguments.

 @author Christopher Hylands Brooks, Tobin Fricke, Bart Kienhuis, Edward A. Lee,
 Steve Neuendorffer, Neil Smyth, Yang Zhao.  Contributor: Daniel Crawl
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class UtilityFunctions {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new UnsizedArrayToken whose element type is the same
     * as the given type.
     * @return a new UnsizedArrayToken.
     */
    public static ArrayToken arrayType(Token t) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        return new UnsizedArrayToken(t.getType());
    }

    /** Return a new ArrayType whose element type is the same as the given
     *  type.
     *  @param type The type of the array.
     *  @return a new ArrayType.
     */
    public static Type arrayTypeReturnType(Type type) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        return new ArrayType(type);
    }

    /** Return a new UnsizedArrayToken whose element type is the same
     *  as the given type, and whose length is the given length.
     *  @param t The element type of the array.
     *  @param numberOfTimes The array length.
     *  @return a new ArrayToken.
     */
    public static ArrayToken arrayType(Token t, IntToken numberOfTimes) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        return repeat(numberOfTimes, t);
    }

    /** Return the (not quite exact) return type of the arrayType
     *  function above.  This function always returns an ArrayType
     *  whose element type is the first argument, with length equal to
     *  the second argument.  The result type is reported as being
     *  unsized, which may result in an inexact type being inferred
     *  for expressions including the arrayType function.
     *  @param type1 The type of the first argument to the
     *  corresponding function.
     *  @param type2 The type of the second argument to the
     *  corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type arrayTypeReturnType(Type type1, Type type2) {
        // FIXME: The method comment makes no sense, the type1
        // argument is not used.

        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        return new ArrayType(type2);
    }

    /** Convert the argument from a fileName to a URL that begins with
     *  "file:".
     *  @param fileName The name of the file to be converted.
     *  @return the URL that is equivalent to the file
     */
    public static String asURL(String fileName) {
        File file = new File(fileName);

        try {
            URL url = file.toURI().toURL();
            return url.toString();
        } catch (java.net.MalformedURLException malformed) {
            throw new RuntimeException("could not convert '" + file
                    + "' to a URL", malformed);
        }
    }

    /** Convert the second token to the type of the first.
     *  @exception IllegalActionException If the token cannot be converted.
     */
    public static Token cast(Token token1, Token token2)
            throws IllegalActionException {
        return token1.getType().convert(token2);
    }

    /** Concatenate two arrays.
     *  The arrays must have the same type.
     *  Example: concatenate({1,2,3},{4,5,6}) == {1,2,3,4,5,6}.
     *  @param token1 First array from which tokens are copied to the result.
     *  @param token2 Second array from which tokens are copied to the result.
     *  @return An array containing all of the elements of the first argument
     *   (in order), followed by all of the arguments of the second argument
     *   (in order).
     *  @exception IllegalActionException If the arrays do not have
     *   compatible types.
     *  @since Ptolemy II 4.1
     */
    public static ArrayToken concatenate(ArrayToken token1, ArrayToken token2)
            throws IllegalActionException {
        Token[] array1 = token1.arrayValue();
        Token[] array2 = token2.arrayValue();

        int nElements = array1.length + array2.length;
        Token[] resultArray = new Token[nElements];

        System.arraycopy(array1, 0, resultArray, 0, array1.length);
        System.arraycopy(array2, 0, resultArray, array1.length, array2.length);

        return new ArrayToken(resultArray);
    }

    /** Concatenate an array of arrays into a single array.
     *  Example: concatenate({{1,2,3},{4,5},{6,7}}) == {1,2,3,4,5,6,7}.
     *  @param token Array of arrays which are to be concatenated.
     *  @exception IllegalActionException If the argument is not an array of
     *   arrays.
     *  @since Ptolemy II 4.1
     */
    public static ArrayToken concatenate(ArrayToken token)
            throws IllegalActionException {
        if (!(token.getElementType() instanceof ArrayType)) {
            throw new IllegalActionException(
                    "The argument to concatenate(ArrayToken) "
                            + "must be an array of arrays.");
        }

        int nElements = 0;

        for (int i = 0; i < token.length(); i++) {
            nElements += ((ArrayToken) (token.getElement(i))).length();
        }

        Token[] result = new Token[nElements];
        int cursor = 0;

        for (int i = 0; i < token.length(); i++) {
            Token[] array = ((ArrayToken) (token.getElement(i))).arrayValue();
            System.arraycopy(array, 0, result, cursor, array.length);
            cursor += array.length;
        }

        return new ArrayToken(result);
    }

    /** Return a record token that contains the names of all the
     *  constants and their values.
     *  @return A token containing the names of all the constants
     *   and their values.
     *  @since Ptolemy II 2.1
     */
    public static RecordToken constants() {
        return Constants.constants();
    }

    /** Return an empty record.
     *  @return An empty record.
     */
    public static RecordToken emptyRecord() throws IllegalActionException {
        return new RecordToken(new String[0], new Token[0]);
    }

    /** Return an empty array with its element type matching
     *  the specified token.
     *  @param prototype A token specifying the element type.
     *  @return An empty array.
     */
    public static ArrayToken emptyArray(Token prototype) {
        return new ArrayToken(prototype.getType());
    }

    /** Extract a sub-array consisting of all of the elements of an
     *  array for which the given predicate function returns true.
     *  If the given array has type {X}, then the given function should
     *  have type function(x:X)(boolean).  Example usage:
     *  <p><code>even = function(x:int)(x%2==0)
     *  filter(even,[1:1:20].toArray)</code></p>
     *  @param predicate A function that takes exactly one parameter (of the
     *   same type as the elements of the given array) and returns
     *   a boolean value.
     *  @param array An array, on whose elements the given function
     *   will operate.
     *  @return The subarray of this array containing exactly those
     *   elements, in order, for which the given function returns
     *   a BooleanToken with value true.  Any other return value
     *   results in the element being omitted. If the given function
     *   never returns true, then return an empty array with an
     *   element type that matches the specified array.
     *  @exception IllegalActionException If applying the function
     *   triggers an exception.
     *  @since Ptolemy II 4.1
     */
    public static ArrayToken filter(FunctionToken predicate, ArrayToken array)
            throws IllegalActionException {
        return filter(predicate, array, new IntToken(-1));
    }

    /** Extract a sub-array consisting of all of the elements of an
     *  array for which the given predicate function returns true.
     *  If the given array has type {X}, then the given function should
     *  have type function(x:X)(boolean).  Example usage:
     *  <p><code>even = function(x:int)(x%2==0)
     *  filter(even,[1:1:20].toArray)</code></p>
     *  @param predicate A function that takes exactly one parameter (of the
     *   same type as the elements of the given array) and returns
     *   a boolean value.
     *  @param array An array, on whose elements the given function
     *   will operate.
     *  @param sizeLimit The maximum size of the resulting array,
     *   or a negative number to specify no limit.
     *  @return The subarray of this array containing exactly those
     *   elements, in order, for which the given function returns
     *   a BooleanToken with value true.  Any other return value
     *   results in the element being omitted. If the given function
     *   never returns true, then return an empty array with an
     *   element type that matches the specified array.
     *  @exception IllegalActionException If applying the function
     *   triggers an exception.
     *  @since Ptolemy II 4.1
     */
    public static ArrayToken filter(FunctionToken predicate, ArrayToken array,
            IntToken sizeLimit) throws IllegalActionException {
        List result = new LinkedList();
        int arity = predicate.getNumberOfArguments();

        if (arity != 1) {
            throw new IllegalActionException(
                    "The predicate argument of filter() must be a function"
                            + " that takes one argument.");
        }

        int sizeLimitValue = sizeLimit.intValue();

        for (int i = 0; i < array.length(); i++) {
            Token element = array.getElement(i);
            Token[] elementList = { element };
            Token include = predicate.apply(elementList);

            if ((include instanceof BooleanToken)
                    && ((BooleanToken) include).booleanValue()) {
                result.add(element);
            }

            if ((sizeLimitValue >= 0) && (result.size() >= sizeLimitValue)) {
                break;
            }
        }

        if (result.size() > 0) {
            Token[] resultArray = new Token[result.size()];
            resultArray = (Token[]) (result.toArray(resultArray));
            return new ArrayToken(resultArray);
        } else {
            return new ArrayToken(array.getElementType());
        }
    }

    /** Return the return type of the filter method, given the types
     *  of the argument.
     *  @param predicateType The type of the predicate function.
     *  @param arrayTokenType The type of the array to be filtered.
     *  @return The type of the result, which is the same as the array
     *   type to be filtered.
     *  @exception IllegalActionException If the specified function does not
     *   take exactly one argument, or if the type signature of the function
     *   is not compatible with the other array argument.
     */
    public static Type filterReturnType(Type predicateType, Type arrayTokenType)
            throws IllegalActionException {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        return filterReturnType(predicateType, arrayTokenType, null);
    }

    /** Return the return type of the filter method, given the types
     *  of the argument.
     *  @param predicateType The type of the predicate function.
     *  @param arrayTokenType The type of the array to be filtered.
     *  @param sizeLimitType The type of the sizeLimit argument, or
     *   null if there is no specified size limit.
     *  @return The type of the result, which is the same as the array
     *   type to be filtered.
     *  @exception IllegalActionException If the specified function does not
     *   take exactly one argument, or if the type signature of the function
     *   is not compatible with the other array argument.
     */
    public static Type filterReturnType(Type predicateType,
            Type arrayTokenType, Type sizeLimitType)
            throws IllegalActionException {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        if (predicateType instanceof FunctionType) {
            FunctionType castPredicateType = (FunctionType) predicateType;

            if (castPredicateType.getArgCount() != 1) {
                throw new IllegalActionException(
                        "filter() can only be used on functions that take "
                                + "one argument.");
            } else {
                Type argType = castPredicateType.getArgType(0);
                int comparison = TypeLattice.compare(
                        ((ArrayType) arrayTokenType).getElementType(), argType);

                if ((comparison != CPO.LOWER) && (comparison != CPO.SAME)) {
                    throw new IllegalActionException(
                            "filter(): specified array element is not "
                                    + "compatible with function argument type.");
                }

                // Force size to be unknown
                return new ArrayType(((ArrayType) arrayTokenType)
                        .getElementType());
            }
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** Find all true-valued elements in an array of boolean values,
     *  returning an array containing the indices (in ascending order)
     *  of all occurrences of the value 'true'.
     *  @param array An array of boolean tokens.
     *  @return An array of integers giving the indices of 'true' elements
     *   in the array given as an argument.
     *  @since Ptolemy II 4.1
     *  @see #find(ArrayToken, Token)
     *  @exception IllegalActionException If the specified array is not
     *   a boolean array.
     */
    public static ArrayToken find(ArrayToken array)
            throws IllegalActionException {
        if (!(array.getElementType() == BaseType.BOOLEAN)) {
            throw new IllegalActionException(
                    "The argument must be an array of boolean tokens.");
        }

        return find(array, BooleanToken.TRUE);
    }

    /** Find all elements in an array that match the specified token
     *  and return an array containing their indices (in ascending order).
     *  @param array An array.
     *  @return An array of integers giving the indices of elements
     *   in the specified array that match the specified token.
     *  @since Ptolemy II 4.1
     *  @see #find(ArrayToken)
     */
    public static ArrayToken find(ArrayToken array, Token match) {
        List result = new LinkedList();

        for (int i = 0; i < array.length(); i++) {
            if (array.getElement(i).equals(match)) {
                result.add(new IntToken(i));
            }
        }

        if (result.size() > 0) {
            Token[] resultArray = new Token[result.size()];
            resultArray = (Token[]) (result.toArray(resultArray));

            try {
                return new ArrayToken(BaseType.INT, resultArray);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(null, ex,
                        "UtilityFunctions.find: "
                                + "cannot create an an Array of Integers");
            }
        } else {
            return new ArrayToken(array.getElementType());
        }
    }

    /** Find a file or directory. If the file does not exist as is, then
     *  search the current working directory, the user's home directory,
     *  and finally, the classpath.
     *  @param name Path of a file or directory to find.
     *  @return Canonical absolute path if the file or directory is found,
     *   otherwise the argument is returned unchanged.
     */
    public static String findFile(String name) {
        File file = new File(name);

        // File has problems if we change user.dir, which we do in
        // ptolemy/actor/gui/jnlp/MenuApplication.java if we are running
        // under Web Start or InstallAnywhere.  What happens is that
        // File ignores changes to user.dir, so findFile("ptmatlab.dll")
        // will look in the old value of user.dir instead of the new
        // value of user.dir.  The hack is to get the canonical path
        // and use that instead.
        if (file.exists()) {
            try {
                file = new File(file.getCanonicalPath());
            } catch (IOException ex) {
                file = file.getAbsoluteFile();
            }
        }

        if (!file.exists()) {
            String curDir = StringUtilities.getProperty("user.dir");
            file = new File(curDir, name);
        }

        if (!file.exists()) {
            String curDir = StringUtilities.getProperty("user.home");
            file = new File(curDir, name);
        }

        if (!file.exists()) {
            String cp = System.getProperty("java.class.path");
            StringTokenizer tokens = new StringTokenizer(cp, System
                    .getProperty("path.separator"));

            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                file = new File(token, name);

                if (file.exists()) {
                    break;
                }
            }
        }

        if (file.exists()) {
            try {
                return file.getCanonicalPath();
            } catch (java.io.IOException ex) {
                return file.getAbsolutePath();
            }
        } else {
            return name;
        }
    }

    /** Return the approximate number of bytes available for future
     *  object allocation.  Note that requesting a garbage collection
     *  may change this value.
     *  @return The approximate number of bytes available.
     *  @see #totalMemory()
     */
    public static LongToken freeMemory() {
        return new LongToken(Runtime.getRuntime().freeMemory());
    }

    /** Return a Gaussian random number.
     *  @param mean The mean.
     *  @param standardDeviation The standard deviation.
     *  @return An observation of a Gaussian random variable.
     */
    public static DoubleToken gaussian(double mean, double standardDeviation) {
        if (_random == null) {
            _random = new Random();
        }

        double raw = _random.nextGaussian();
        double result = (raw * standardDeviation) + mean;
        return new DoubleToken(result);
    }

    /** Return an array of Gaussian random numbers.
     *  @param mean The mean.
     *  @param standardDeviation The standard deviation.
     *  @param length The length of the array.
     *  @return An array of doubles with IID Gaussian random variables.
     */
    public static ArrayToken gaussian(double mean, double standardDeviation,
            int length) {
        if (_random == null) {
            _random = new Random();
        }

        DoubleToken[] result = new DoubleToken[length];

        for (int i = 0; i < length; i++) {
            double raw = _random.nextGaussian();
            result[i] = new DoubleToken((raw * standardDeviation) + mean);
        }

        try {
            return new ArrayToken(BaseType.DOUBLE, result);
        } catch (IllegalActionException ex) {
            // This should not happen since result should not be null.
            throw new InternalErrorException(null, ex,
                    "UtilityFunction.gaussian: "
                            + "Cannot create the array that contains "
                            + "Gaussian random numbers.");
        }
    }

    /** Return a matrix of Gaussian random numbers.
     *  @param mean The mean.
     *  @param standardDeviation The standard deviation.
     *  @param rows The number of rows.
     *  @param columns The number of columns.
     *  @return A matrix of observations of a Gaussian random variable.
     */
    public static DoubleMatrixToken gaussian(double mean,
            double standardDeviation, int rows, int columns) {
        if (_random == null) {
            _random = new Random();
        }

        double[][] result = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double raw = _random.nextGaussian();
                result[i][j] = (raw * standardDeviation) + mean;
            }
        }

        try {
            return new DoubleMatrixToken(result);
        } catch (IllegalActionException ex) {
            // This should not happen since result should not be null.
            throw new InternalErrorException(
                    null,
                    ex,
                    "UtilityFunction.gaussian: "
                            + "Cannot create the DoubleMatrixToken that contains "
                            + "Gaussian random numbers.");
        }
    }

    /** Get a variable from the environment.
     *  @param variableName The name of the environment variable to get
     *  @return the environment variable, or null if the variable is not defined.
     */
    public static StringToken getenv(String variableName) {
        return new StringToken(System.getenv(variableName));
    }

    /** Get a variable from the environment.
     *  @return the environment variable, or null if the variable is not defined.
     */
    public static RecordToken getenv() throws IllegalActionException {

        Map<String, Token> environmentMap = new HashMap<String, Token>();

        Map<String, String> environment = System.getenv();

        Iterator environmentVariables = environment.entrySet().iterator();
        while (environmentVariables.hasNext()) {
            Map.Entry pairs = (Map.Entry) environmentVariables.next();
            environmentMap.put((String) (pairs.getKey()), new StringToken(
                    (String) (pairs.getValue())));
        }
        return new RecordToken(environmentMap);
    }

    /** Get the specified property from the environment. An empty string
     *  is returned if the argument environment variable does not exist,
     *  though if certain properties are not defined, then we
     *  make various attempts to determine them and then set them.
     *  See the javadoc page for java.util.System.getProperties() for
     *  a list of system properties.
     *  <p>The following properties are handled specially
     *  <dl>
     *  <dt> "ptolemy.ptII.dir"
     *  <dd> vergil usually sets the ptolemy.ptII.dir property to the
     *  value of $PTII.  However, if we are running under Web Start,
     *  then this property might not be set, in which case we look
     *  for "ptolemy/kernel/util/NamedObj.class" and set the
     *  property accordingly.
     *  <dt> "ptolemy.ptII.dirAsURL"
     *  <dd> Return $PTII as a URL.  For example, if $PTII was c:\ptII,
     *  then return file:/c:/ptII/.
     *  <dt> "user.dir"
     *  <dd> Return the canonical path name to the current working directory.
     *  This is necessary because under JDK1.4.1 System.getProperty()
     *  returns <code><b>c</b>:/<i>foo</i></code>
     *  whereas most of the other methods that operate
     *  on path names return <code><b>C</b>:/<i>foo</i></code>.
     *  </dl>
     *  @param propertyName The name of property.
     *  @return A String containing the string value of the property.
     *  @deprecated Use
     *  {@link ptolemy.util.StringUtilities#getProperty(String)}
     *  instead
     */
    public static String getProperty(String propertyName) {
        return StringUtilities.getProperty(propertyName);
    }

    /** Infer the type of the given string as an expression in the
     *  expression language. Return a string representation of the
     *  given type.
     *  @param string The string to be parsed and evaluated.
     *  @return A string representing an inferred type.
     */
    public static String inferType(String string) throws IllegalActionException {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree = parser.generateParseTree(string);
        ParseTreeTypeInference typeInference = new ParseTreeTypeInference();
        return typeInference.inferTypes(parseTree).toString();
    }

    /** Find the intersection of two records. The field names are
     *  intersected, and the values are taken from the first
     *  record.
     *  <p>Example: intersect({a = 1, b = 2}, {a = 3, c = 4}) returns {a = 1}.
     *  @param record1 The first record to intersect. The result gets
     *  its values from this record.
     *  @param record2 The second record to intersect.
     *  @return A RecordToken containing the result.
     */
    public static Token intersect(RecordToken record1, RecordToken record2)
            throws IllegalActionException {
        Set commonNames = new HashSet(record1.labelSet());
        commonNames.retainAll(record2.labelSet());

        Token[] values = new Token[commonNames.size()];
        String[] names = new String[values.length];
        int i = 0;

        for (Iterator iterator = commonNames.iterator(); iterator.hasNext(); i++) {
            String name = (String) iterator.next();
            values[i] = record1.get(name);
            names[i] = name;
        }

        return new RecordToken(names, values);
    }

    /** Iterate the specified function to produce an array of the specified
     *  length.  The first element of the output array is the <i>initial</i>
     *  argument, the second is the result of applying the function to
     *  <i>initial</i>, the third is the result of applying the function to
     *  that result, etc.  The <i>length</i> argument is required to be
     *  greater than 1.
     *  @param function A single-argument function to iterate.
     *  @param length The length of the resulting array.
     *  @param initial The first element of the result.
     *  @return A new array that is the result of applying the function
     *   repeatedly.
     *  @exception IllegalActionException If the specified function does not
     *  take exactly one argument, or if an error occurs applying the function.
     */
    public static ArrayToken iterate(FunctionToken function, int length,
            Token initial) throws IllegalActionException {
        int arity = function.getNumberOfArguments();

        if (arity != 1) {
            throw new IllegalActionException(
                    "iterate() can only be used on functions that take "
                            + "one argument.");
        } else if (length < 2) {
            throw new IllegalActionException(
                    "iterate() requires the length argument to be greater "
                            + "than 1.");
        } else {
            Token[] result = new Token[length];
            Token iterate = initial;
            result[0] = initial;

            for (int i = 1; i < length; i++) {
                Token[] args = new Token[1];
                args[0] = iterate;
                iterate = function.apply(args);
                result[i] = iterate;
            }

            return new ArrayToken(result);
        }
    }

    /** Return the return type of the iterate method, given the types
     *  of the argument.
     *  @param functionType The type of the function to iterate.
     *  @param lengthType The type of the length argument.
     *  @param initialType The type of the first element of the result.
     *  @return The type of the result.
     *  @exception IllegalActionException If the specified function does not
     *   take exactly one argument, or if the type signature of the function
     *   is not compatible with the other arguments.
     */
    public static Type iterateReturnType(Type functionType, Type lengthType,
            Type initialType) throws IllegalActionException {
        if (functionType instanceof FunctionType) {
            FunctionType castFunctionType = (FunctionType) functionType;

            if (castFunctionType.getArgCount() != 1) {
                throw new IllegalActionException(
                        "iterate() can only be used on functions that take "
                                + "one argument.");
            } else {
                Type argType = castFunctionType.getArgType(0);
                int comparison = TypeLattice.compare(initialType, argType);

                if ((comparison != CPO.LOWER) && (comparison != CPO.SAME)) {
                    throw new IllegalActionException(
                            "iterate(): specified initial value is not "
                                    + "compatible with function argument type.");
                }

                Type resultType = castFunctionType.getReturnType();
                int comparison2 = TypeLattice.compare(resultType, argType);

                if ((comparison2 != CPO.LOWER) && (comparison2 != CPO.SAME)) {
                    throw new IllegalActionException(
                            "iterate(): invalid function: function return "
                                    + "type is not "
                                    + "compatible with function argument type.");
                }

                return new ArrayType(TypeLattice.leastUpperBound(resultType,
                        initialType));
            }
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** Load a library by first using the default platform dependent
     *  System.loadLibrary() method.  If the library cannot be loaded
     *  using System.loadLibrary(), then search for the library using
     *  {@link #findFile(String)} and if the library is found,
     *  load it using System.load().  If the library is not found
     *  by findFile() and the pathname contains a /
     *  then we call System.loadLibrary() the filename after the last
     *  /.  This step is necessary to support native methods under
     *  Webstart, which looks for libraries at the top level.
     *  If all of the above fails, we throw the initial exception.
     *
     *  @param library the name of the library to be loaded.  The name
     *  should not include the platform dependent suffix.
     */
    public static void loadLibrary(String library) {
        try {
            if ((library.indexOf("/") == -1) && (library.indexOf("\\") == -1)) {
                // loadLibrary does not work if the library has a \ or / in it.
                // Unfortunately, pathnames tend to get specified with
                // a forward slash, even under windows
                System.loadLibrary(library);
            } else {
                // load() does not work with relative paths.
                System.load(library);
            }
        } catch (UnsatisfiedLinkError ex) {
            String sharedLibrarySuffix = "dll";

            // The name of the library (everything after the last /)
            String shortLibraryName = null;

            String osName = StringUtilities.getProperty("os.name");

            if (osName.startsWith("SunOS") || osName.startsWith("Linux")
                    || osName.startsWith("Mac OS X")) {

                if (osName.startsWith("Mac OS X")) {
                    sharedLibrarySuffix = "dylib";
                } else {
                    sharedLibrarySuffix = "so";
                }

                // Under Solaris, libraries start with lib, so
                // we find the last /, and if the next chars are not "lib"
                // then we insert "lib".
                int index = library.lastIndexOf("/");

                if (index == -1) {
                    if (!library.startsWith("lib")) {
                        library = "lib" + library;
                    }

                    shortLibraryName = library;
                } else {
                    if (!library.substring(index, index + 4).equals("/lib")) {
                        if (osName.startsWith("Linux")) {
                            library = library.substring(index + 1);
                            shortLibraryName = library;
                        } else {
                            // Under SunOS and Mac OS X, we add lib to
                            // the path.  If we don't do this on the Mac,
                            // then libptymatlab.dynlib will not be found.
                            shortLibraryName = "/lib"
                                    + library.substring(index + 1);
                            library = library.substring(0, index)
                                    + shortLibraryName;
                        }
                    }
                }
            } else {
                // Windows
                int index = library.lastIndexOf("/");

                if (index != -1) {
                    // Everything after the trailing /
                    shortLibraryName = library.substring(index + 1);
                }
            }

            String libraryWithSuffix = library + "." + sharedLibrarySuffix;

            String libraryPath = UtilityFunctions.findFile(libraryWithSuffix);

            boolean libraryPathExists = false;

            try {
                // It turns out that when looking for libraries under
                // InstallAnywhere, findFile() can somehow end up returning
                // a bogus value in C:/Documents and Settings
                File libraryPathFile = new File(libraryPath);

                if (libraryPathFile.exists()) {
                    libraryPathExists = true;
                }
            } catch (Throwable throwable) {
                // Ignore, the file can't be found
            }

            if (libraryPath.equals(libraryWithSuffix) || !libraryPathExists) {
                try {
                    // findFile did not find the library, so we try using
                    // just the short library name.  This is necessary
                    // for Web Start because Web Start requires that
                    // native code be in the top level of a jar file
                    // that is specially marked.  Matlab under Web Start
                    // requires this.
                    if (shortLibraryName != null) {
                        System.loadLibrary(shortLibraryName);
                    }
                    return;
                } catch (UnsatisfiedLinkError ex2) {
                    // We ignore ex2 and report the original error.
                    // UnsatisfiedLinkError does not have a (String, Throwable)
                    // constructor, so we call initCause().
                    String userDir = "<<user.dir unknown>>";

                    try {
                        userDir = System.getProperty("user.dir");
                    } catch (Throwable throwable) {
                        // Ignore.
                    }

                    String userHome = "<<user.home unknown>>";

                    try {
                        userHome = System.getProperty("user.home");
                    } catch (Throwable throwable) {
                        // Ignore.
                    }

                    String classpath = "<<classpath unknown>>";

                    try {
                        classpath = System.getProperty("java.class.path");
                    } catch (Throwable throwable) {
                        // Ignore.
                    }

                    Error error = new UnsatisfiedLinkError("Did not find '"
                            + library + "' in path, searched " + "user.home ("
                            + userDir + ") user.dir (" + userHome
                            + ") and the classpath for '" + libraryPath
                            + "', but that was not found either.\n"
                            + "The Java classpath was: " + classpath
                            + "\nIn addition, loadLibrary(\""
                            + shortLibraryName
                            + "\") was called, the exception for the "
                            + "loadLibrary() call was: " + ex2);

                    error.initCause(ex);
                    throw error;
                }
            }

            // System.loadLibrary() does not handle pathnames with separators.
            // If we get to here and load a library that includes references
            // to libraries not in the PATH or LD_LIBRARY_PATH, then we will
            // get and UnsatisfiedLinkError on the file we depend on.
            // For example, if liba.so uses libb.so and we call this
            // method on a, then libb.so will not be found.
            System.load(libraryPath);
        }
    }

    /** Apply the specified function to the specified array and return
     *  an array with the results. The function must take at least one
     *  argument. If the function takes more than one argument, then
     *  the specified array should be an array of arrays, where each
     *  subarray is a set of arguments.  Since arrays in the expression
     *  language can only contain elements of the same type, this method
     *  will only work for functions whose arguments are all of the same
     *  type.
     *  @param function A function with at least one argument.
     *  @param array The array to which to apply the function.
     *  @return A new array that is the result of applying the function
     *   to the specified array.
     *  @exception IllegalActionException If the specified function does not
     *          take at least one argument, or if an error occurs applying the
     *   function, or if the number of arguments does not match the subarray
     *   lengths.
     */
    public static ArrayToken map(FunctionToken function, ArrayToken array)
            throws IllegalActionException {
        int arity = function.getNumberOfArguments();
        Token[] result = new Token[array.length()];

        if (arity == 1) {
            for (int i = 0; i < array.length(); i++) {
                Token arg = array.getElement(i);
                Token[] args = new Token[1];
                args[0] = arg;
                result[i] = function.apply(args);
            }
        } else if (arity > 1) {
            for (int i = 0; i < array.length(); i++) {
                Token args = array.getElement(i);

                if (!(args instanceof ArrayToken)) {
                    throw new IllegalActionException(
                            "Invalid arguments to map(): mismatched arity.");
                }

                Token[] invokeArgs = new Token[arity];
                ArrayToken castArgs = (ArrayToken) args;

                if (castArgs.length() != arity) {
                    throw new IllegalActionException(
                            "Invalid arguments to map(): mismatched arity.");
                } else {
                    for (int j = 0; j < arity; j++) {
                        invokeArgs[j] = castArgs.getElement(j);
                    }

                    result[i] = function.apply(invokeArgs);
                }
            }
        } else {
            throw new IllegalActionException(
                    "map() can only be used on functions that take at least "
                            + "one argument.");
        }

        return new ArrayToken(result);
    }

    /** Return the return type of the map method, given the types
     *  of the argument.
     *  @param functionType The type of the function to map.
     *  @param arrayTokenType The type of array to apply the
     *  specified function on.
     *  @return The type of the result.
     *  @exception IllegalActionException If the specified function does not
     *   take at least one argument, or if the type signature of the function
     *   is not compatible with the array elements, or if the specified
     *   function has different argument type.
     */
    public static Type mapReturnType(Type functionType, Type arrayTokenType)
            throws IllegalActionException {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        if (functionType instanceof FunctionType) {
            FunctionType castFunctionType = (FunctionType) functionType;

            if (castFunctionType.getArgCount() == 1) {
                Type argType = castFunctionType.getArgType(0);
                int comparison = TypeLattice.compare(
                        ((ArrayType) arrayTokenType).getElementType(), argType);

                if ((comparison != CPO.LOWER) && (comparison != CPO.SAME)) {
                    throw new IllegalActionException(
                            "map(): specified array token is not compatible "
                                    + "with function argument type.");
                }
            } else if (castFunctionType.getArgCount() > 1) {
                Type firstArgType = castFunctionType.getArgType(0);
                boolean flag = true;

                for (int i = 1; i < castFunctionType.getArgCount(); i++) {
                    Type argType = castFunctionType.getArgType(i);

                    if (argType != firstArgType) {
                        i = castFunctionType.getArgCount();
                        flag = false;
                        throw new IllegalActionException("map() can only work "
                                + "for functions whose arguments are all of "
                                + "the same type.");
                    }
                }

                if (flag) {
                    Type argType = castFunctionType.getArgType(0);
                    Type elementType = ((ArrayType) arrayTokenType)
                            .getElementType();

                    if (!(elementType instanceof ArrayType)) {
                        throw new IllegalActionException(
                                "map(): specified array token is not "
                                        + "compatible with function arity.");
                    } else {
                        int comparison = TypeLattice.compare(
                                ((ArrayType) elementType).getElementType(),
                                argType);

                        if ((comparison != CPO.LOWER)
                                && (comparison != CPO.SAME)) {
                            throw new IllegalActionException(
                                    "map(): specified array token is not "
                                            + "compatible with function "
                                            + "argument type.");
                        }
                    }
                }
            }

            Type resultType = castFunctionType.getReturnType();
            return new ArrayType(resultType);
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** Return the maximum of two unsigned bytes.
     *  @param x An unsigned byte.
     *  @param y An unsigned byte.
     *  @return The maximum of x and y.
     */
    public static UnsignedByteToken max(UnsignedByteToken x, UnsignedByteToken y) {
        if (x.intValue() > y.intValue()) {
            return x;
        } else {
            return y;
        }
    }

    /** Return the maximum of the contents of the array.
     *  @param array An array of scalar tokens.
     *  @return The largest element of the array.
     *  @exception IllegalActionException If the array is empty or
     *   it contains tokens that are not scalar or it contains complex tokens.
     */
    public static ScalarToken max(ArrayToken array)
            throws IllegalActionException {
        if ((array.length() == 0)
                || !BaseType.SCALAR.isCompatible(array.getElementType())) {
            throw new IllegalActionException(
                    "max function can only be applied to arrays of scalars.");
        }

        ScalarToken result = (ScalarToken) array.getElement(0);

        for (int i = 1; i < array.length(); i++) {
            ScalarToken element = (ScalarToken) array.getElement(i);

            if ((element.isGreaterThan(result)).booleanValue()) {
                result = element;
            }
        }

        return result;
    }

    /** Return the (exact) return type of the max function above.
     *  If the argument is an array type, then return its element type,
     *  otherwise return BaseType.UNKNOWN.
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding
     *  function.
     */
    public static Type maxReturnType(Type type) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            return arrayType.getElementType();
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** Return the minimum of two unsigned bytes.
     *  @param x An unsigned byte.
     *  @param y An unsigned byte.
     *  @return The minimum of x and y.
     */
    public static UnsignedByteToken min(UnsignedByteToken x, UnsignedByteToken y) {
        if (x.intValue() < y.intValue()) {
            return x;
        } else {
            return y;
        }
    }

    /** Return the minimum of the contents of the array.
     *  @param array An array of scalar tokens.
     *  @return The largest element of the array.
     *  @exception IllegalActionException If the array is empty or
     *   it contains tokens that are not scalar or it contains complex tokens.
     */
    public static ScalarToken min(ArrayToken array)
            throws IllegalActionException {
        if ((array.length() == 0)
                || !BaseType.SCALAR.isCompatible(array.getElementType())) {
            throw new IllegalActionException(
                    "min function can only be applied to arrays of scalars.");
        }

        ScalarToken result = (ScalarToken) array.getElement(0);

        for (int i = 1; i < array.length(); i++) {
            ScalarToken element = (ScalarToken) array.getElement(i);

            if ((element.isLessThan(result)).booleanValue()) {
                result = element;
            }
        }

        return result;
    }

    /** Return the (exact) return type of the min function above.
     *  If the argument is an array type, then return its element type,
     *  otherwise return BaseType.UNKNOWN.
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type minReturnType(Type type) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            return arrayType.getElementType();
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** FIXME. Placeholder for a function that will return a model.
     */
    public static ObjectToken model(String classname)
            throws IllegalActionException {
        return new ObjectToken(classname);
    }

    /** Parse the string provided and return the result wrapped in a token.
     *  @param moml The MoML string.
     *  @return The result of parsing the MoML.
     *  @throws Exception If the MoML is invalid or the results is not an
     *   instance of Entity.
     */
    public static ActorToken parseMoML(String moml) throws Exception {
        return MoMLUtilities.parseMoML(moml);
    }

    /** Get the specified property from the environment. An empty string
     *  is returned if the argument environment variable does not exist.
     *  See the javadoc page for java.util.System.getProperties() for
     *  a list of system properties.  Example properties include:
     *  <dl>
     *  <dt> "java.version"
     *  <dd> the version of the JDK.
     *  <dt> "ptolemy.ptII.dir"
     *  <dd> The value of $PTII, which is the name of the directory in
     *       which Ptolemy II is installed.
     *  <dt> "ptolemy.ptII.dirAsURL"
     *  <dd> The value of $PTII as a URL, which is the name of the directory in
     *       which Ptolemy II is installed.
     *  <dt> "user.dir"
     *  <dd> The canonical path name to the current working directory.
     *  </dl>
     *
     *  @param propertyName The name of property.
     *  @return A token containing the string value of the property.
     *  @see ptolemy.util.StringUtilities#getProperty(String)
     */
    public static StringToken property(String propertyName) {
        return new StringToken(StringUtilities.getProperty(propertyName));
    }

    /** Return an array of IID random numbers with value greater than
     *  or equal to 0.0 and less than 1.0.
     *  @param length The length of the array.
     *  @return An array of doubles with IID random variables.
     */
    public static ArrayToken random(int length) {
        DoubleToken[] result = new DoubleToken[length];

        for (int i = 0; i < length; i++) {
            result[i] = new DoubleToken(Math.random());
        }

        try {
            return new ArrayToken(BaseType.DOUBLE, result);
        } catch (IllegalActionException ex) {
            // This should not happen since result should not be null.
            throw new InternalErrorException(null, ex,
                    "UtilityFunction.random: "
                            + "Cannot create the array that contains "
                            + "random numbers.");
        }
    }

    /** Return a matrix of IID random numbers with value greater than
     *  or equal to 0.0 and less than 1.0.
     *  @param rows The number of rows.
     *  @param columns The number of columns.
     *  @return A matrix of IID random variables.
     */
    public static DoubleMatrixToken random(int rows, int columns) {
        double[][] result = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = Math.random();
            }
        }

        try {
            return new DoubleMatrixToken(result);
        } catch (IllegalActionException ex) {
            // This should not happen since result should not be null.
            throw new InternalErrorException(
                    null,
                    ex,
                    "UtilityFunction.random: "
                            + "Cannot create the DoubleMatrixToken that contains "
                            + "random numbers.");
        }
    }

    /** Return the (exact) return type of the random function above.
     *  If the argument is BaseType.INT, then return and ArrayType of
     *  BaseType.DOUBLE, otherwise return BaseType.UNKNOWN.
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type randomReturnType(Type type) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        if (type.equals(BaseType.INT)) {
            return new ArrayType(BaseType.DOUBLE);
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** Get the string text contained in the specified file. The argument
     *  is first interpreted using findFile(), so file names relative to
     *  the current working directory, the user's home directory, or the
     *  classpath are understood. If the file contains text that is a
     *  valid expression in the expression language, then that text can
     *  interpreted using the eval() function in
     *  ptolemy.data.expr.ASTPtFunctionApplicationNode.
     *  For example: <code>eval(readFile("<i>filename</i>"))</code><p>
     *  Note that readFile() does not work inside applets, use
     *  {@link #readResource(String)} instead.
     *
     *  @param filename The name of the file to read from.
     *  @return A StringToken containing the text contained in
     *   the specified file.
     *  @exception IllegalActionException If the file cannot be opened.
     *  @see ptolemy.data.expr.ASTPtFunctionApplicationNode
     *  @see #readResource(String)
     */
    public static StringToken readFile(String filename)
            throws IllegalActionException {
        File file = new File(findFile(filename));

        //System.out.println("Trying to open file: " + file.toString());
        BufferedReader fin = null;
        String line;
        StringBuffer result = new StringBuffer("");
        String newline = System.getProperty("line.separator");

        try {
            fin = new BufferedReader(new FileReader(file));

            while (true) {
                try {
                    line = fin.readLine();
                } catch (IOException e) {
                    break;
                }

                if (line == null) {
                    break;
                }

                result.append(line + newline);
            }
        } catch (FileNotFoundException ex) {
            throw new IllegalActionException(null, ex, "File not found");
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(null, ex,
                            "Problem closing '" + file + "'");
                }
            }
        }

        return new StringToken(result.toString());
    }

    /** Read a file that contains a matrix of reals in Matlab notation.
     *
     *  @param filename The filename.
     *  @return The matrix defined in the file.
     *  @exception IllegalActionException If the file cannot be opened.
     *  @deprecated Use eval(readFile()) instead.
     */
    public static DoubleMatrixToken readMatrix(String filename)
            throws IllegalActionException {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is deprecated.

        DoubleMatrixToken returnMatrix = null;

        File file = new File(findFile(filename));
        if (!file.exists()) {
            throw new IllegalActionException("readMatrix: File " + filename
                    + " not found.");
        }

        // Vector containing the matrix
        Vector k = null;

        // Parameters for the Matrix
        int row = -1;
        int column = -1;

        int rowPosition = 0;
        int columnPosition = 0;
        double[][] mtr = null;

        FileReader fin = null;
        try {
            try {
                // Open the matrix file
                fin = new FileReader(file);
            } catch (FileNotFoundException e) {
                throw new IllegalActionException("readMatrix: file \""
                        + file.getName() + "\" not found");
            }

            // Read the file and convert it into a matrix
            if (_matrixParser == null) {
                _matrixParser = new MatrixParser(System.in);
            }

            MatrixParser.ReInit(fin);
            k = _matrixParser.readMatrix();

            if (column == -1) {
                // The column size of the matrix
                column = k.size();
            }

            Iterator i = k.iterator();

            while (i.hasNext()) {
                Vector l = (Vector) i.next();

                if (row == -1) {
                    // the row size.
                    row = l.size();

                    // create a new matrix definition
                    mtr = new double[column][row];
                } else {
                    if (row != l.size()) {
                        throw new IllegalActionException(" The Row"
                                + " size needs to be the same for all"
                                + " rows");
                    }
                }

                Iterator j = l.iterator();

                while (j.hasNext()) {
                    Double s = (Double) j.next();
                    mtr[columnPosition][rowPosition++] = s.doubleValue();
                }

                rowPosition = 0;
                columnPosition++;
            }

            // Vectors have now become obsolete, data is stored
            // in double[][].
            k.clear();
            returnMatrix = new DoubleMatrixToken(mtr);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(null, ex,
                            "Problem closing '" + file + "'");
                }
            }
        }
        return returnMatrix;
    }

    /** Get the string text contained in the specified resource, which
     *  is a file that is specified relative to the Java classpath.
     *  Resource strings look like filenames without a leading slash.
     *  If the file contains text that is a
     *  valid expression in the expression language, then that text can
     *  interpreted using the eval() function in
     *  ptolemy.data.expr.ASTPtFunctionApplicationNode.
     *  For example: <code>eval(readFile("<i>filename</i>"))</code><p>
     *
     *  @param name The name of the resource to read from.
     *  @return A StringToken containing the text contained in
     *   the specified resource.
     *  @exception IllegalActionException If the resource cannot be opened.
     *  @see ptolemy.data.expr.ASTPtFunctionApplicationNode
     *  @see #readFile(String)
     */
    public static StringToken readResource(String name)
            throws IllegalActionException {
        URL url = ClassLoader.getSystemResource(name);
        StringBuffer result = new StringBuffer("");
        BufferedReader fin = null;

        try {
            InputStream stream = url.openStream();
            String line;
            String newline = System.getProperty("line.separator");
            fin = new BufferedReader(new InputStreamReader(stream));

            while (true) {
                try {
                    line = fin.readLine();
                } catch (IOException e) {
                    break;
                }

                if (line == null) {
                    break;
                }

                result.append(line + newline);
            }
        } catch (IOException ex) {
            throw new IllegalActionException(null, ex, "File not found");
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(null, ex,
                            "Failed to close '" + name + "'");
                }
            }
        }

        return new StringToken(result.toString());
    }

    /** Create an array that contains the specified element
     *  repeated the specified number of times.
     *  @param numberOfTimes The number of times to repeat the element.
     *  @param element The element to repeat.
     *  @return A new array containing the specified element repeated the
     *   specified number of times.
     */
    public static ArrayToken repeat(IntToken numberOfTimes, Token element) {
        int length = numberOfTimes.intValue();
        Token[] result = new Token[length];

        for (int i = 0; i < length; i++) {
            result[i] = element;
        }

        ArrayToken arrayToken;

        try {
            arrayToken = new ArrayToken(element.getType(), result);
        } catch (IllegalActionException ex) {
            // This should not happen since the elements of the array always
            // have the same type.
            throw new InternalErrorException(null, ex,
                    "UtilityFunctions.repeat: "
                            + "Cannot construct an ArrayToken.");
        } catch (IllegalArgumentException ex2) {
            // This should not happen since the elements of the array always
            // have the same type.
            throw new InternalErrorException(null, ex2,
                    "UtilityFunctions.repeat: "
                            + "Cannot construct an ArrayToken.");
        }

        return arrayToken;
    }

    /** Return the (exact) return type of the repeat function above.
     *  This function always returns an ArrayType whose element type
     *  is the second argument.
     *  @param type1 The type of the first argument to the
     *  corresponding function.
     *  @param type2 The type of the second argument to the
     *  corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type repeatReturnType(Type type1, Type type2) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        return new ArrayType(type2);
    }

    /** Return a new array that is the sorted contents of a specified
     *  array, in ascending order. The specified array can contain
     *  scalar tokens (except complex) or string tokens. If the
     *  specified array is empty, then it is returned.
     *  @param array An array of scalar tokens.
     *  @return A new sorted array.
     *  @exception IllegalActionException If the array contains
     *   tokens that are complex or are not scalar or string tokens.
     */
    public static ArrayToken sort(ArrayToken array)
            throws IllegalActionException {
        if (array.length() == 0) {
            return array;
        }

        // NOTE: The following method returns a copy, so we can modify it.
        Token[] value = array.arrayValue();
        Arrays.sort(value, _ASCENDING);
        return new ArrayToken(array.getElementType(), value);
    }

    /** Return the (exact) return type of the sort function above.
     *  If the argument is an array type with elements that are strings
     *  or scalars other than complex, then return the argument;
     *  otherwise return BaseType.UNKNOWN.
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type sortReturnType(Type type) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.

        // NOTE: This logic indicates which tokens are comparable
        // by the TokenComparator inner class. If that class is changed,
        // then this logic needs to be changed too.
        if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            Type elementType = arrayType.getElementType();

            if (elementType.equals(BaseType.COMPLEX)) {
                return BaseType.UNKNOWN;
            } else if (elementType.equals(BaseType.STRING)
                    || BaseType.SCALAR.isCompatible(elementType)) {
                return type;
            }
        }

        return BaseType.UNKNOWN;
    }

    /** Return a new array that is the sorted contents of a specified
     *  array, in ascending order. The specified array can contain
     *  scalar tokens (except complex) or string tokens. If the
     *  specified array is empty, then it is returned.
     *  This method is identical to sort(), and is provided only
     *  for naming uniformity.
     *  @see #sort(ArrayToken)
     *  @see #sortDescending(ArrayToken)
     *  @param array An array of scalar tokens.
     *  @return A new sorted array.
     *  @exception IllegalActionException If the array contains
     *   tokens that are complex or are not scalar or string tokens.
     */
    public static ArrayToken sortAscending(ArrayToken array)
            throws IllegalActionException {
        return sort(array);
    }

    /** Return the (exact) return type of the sortAscending function above.
     *  If the argument is an array type with elements that are strings
     *  or scalars other than complex, then return the argument;
     *  otherwise return BaseType.UNKNOWN.
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type sortAscendingReturnType(Type type) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        return sortReturnType(type);
    }

    /** Return a new array that is the sorted contents of a specified
     *  array, in descending order. The specified array can contain
     *  scalar tokens (except complex) or string tokens. If the
     *  specified array is empty, then it is returned.
     *  @param array An array of scalar tokens.
     *  @return A new sorted array.
     *  @exception IllegalActionException If the array contains
     *   tokens that are complex or are not scalar or string tokens.
     */
    public static ArrayToken sortDescending(ArrayToken array)
            throws IllegalActionException {
        if (array.length() == 0) {
            return array;
        }

        // NOTE: The following method returns a copy, so we can modify it.
        Token[] value = array.arrayValue();
        Arrays.sort(value, _DESCENDING);
        return new ArrayToken(array.getElementType(), value);
    }

    /** Return the (exact) return type of the sortDescending function above.
     *  If the argument is an array type with elements that are strings
     *  or scalars other than complex, then return the argument;
     *  otherwise return BaseType.UNKNOWN.
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type sortDescendingReturnType(Type type) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        return sortReturnType(type);
    }

    /** Return the contiguous subarray of the specified array
     *  starting at the specified index and of the specified length.
     *  If the specified index is out of range,
     *  or if the specified length extends beyond the end of the array,
     *  then return an empty array with the same type as the specified array.
     *  @param array The array.
     *  @param index The index of the beginning of the subarray.
     *  @param count The length of the subarray.
     *  @return The extracted subarray.
     *  @exception IllegalActionException If the index argument is less
     *   than zero.
     *  @since Ptolemy II 4.1
     */
    public static ArrayToken subarray(ArrayToken array, IntToken index,
            IntToken count) throws IllegalActionException {
        return array.subarray(index.intValue(), count.intValue());
    }

    /** Return the return type of the subarray() method, which is the
     *  same as the array type.
     *  @param arrayType The type of the array argument.
     *  @param indexType The type of the index argument.
     *  @param countType The type of the count argument.
     *  @return The extracted subarray.
     *  @exception IllegalActionException If the index argument is less
     *   than zero.
     *  @since Ptolemy II 4.1
     */
    public static Type subarrayReturnType(Type arrayType, Type indexType,
            Type countType) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        return arrayType;
    }

    /** Return the sum of the elements in the specified array.
     *  This method is polymorphic in that it can sum any array
     *  whose elements support addition.
     *  There is special support for strings, which are added
     *  not via their add method (which concatenates two strings),
     *  but are accumulated in a StringBuffer, which is much more
     *  efficient.
     *  @param array An array.
     *  @return The sum of the elements of the array.
     *  @exception IllegalActionException If the length of the
     * array is zero, or if the array elements do not support
     * addition.
     */
    public static final Token sum(ArrayToken array)
            throws IllegalActionException {
        if ((array == null) || (array.length() < 1)) {
            throw new IllegalActionException(
                    "sum() function cannot be applied to an empty array");
        }

        if (array.getElement(0) instanceof StringToken) {
            int length = 0;

            for (int i = 0; i < array.length(); i++) {
                length += ((StringToken) (array.getElement(i))).stringValue()
                        .length();
            }

            StringBuffer buffer = new StringBuffer(length);

            for (int i = 0; i < array.length(); i++) {
                buffer.append(((StringToken) (array.getElement(i)))
                        .stringValue());
            }

            return new StringToken(buffer.toString());
        }

        Token result = array.getElement(0);

        for (int i = 1; i < array.length(); i++) {
            result = result.add(array.getElement(i));
        }

        return result;
    }

    /** Return the (exact) return type of the sum function above.
     *  If the argument is an array type, then return its element type,
     *  otherwise return BaseType.UNKNOWN.
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type sumReturnType(Type type) {
        // Note that this method is deliberately not listed in the Expression
        // chapter because it is very specialized.
        if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            return arrayType.getElementType();
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** Return the approximate number of bytes used by current objects
     *  and available for future object allocation.
     *  @return The total number of bytes used by the JVM.
     *  @see #freeMemory()
     */
    public static LongToken totalMemory() {
        return new LongToken(Runtime.getRuntime().totalMemory());
    }

    /** Evaluate the given string as an expression in the expression
     *  language.  Instead of returning the resulting value, return a
     *  trace of the evaluation, including such useful information as
     *  what registered method is actually invoked.
     *  @param string The string to be parsed and evaluated.
     *  @return A string representing an evaluation trace.
     */
    public static String traceEvaluation(String string)
            throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree = parser.generateParseTree(string);
        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
        return evaluator.traceParseTreeEvaluation(parseTree, null);
    }

    /** Return true if the first argument is close in value to the second,
     *  where "close" means that it is within the distance given by the
     *  third argument. Exactly what this means depends on the data type.
     *  This method uses the isCloseTo() method of the first token.
     *  @param token1 The first token.
     *  @param token2 The second token.
     *         @param distance The distance criterion.
     *  @return a true-valued token if the first two arguments are close
     *   enough.
     *  @exception IllegalActionException If the first two arguments cannot
     *   be compared.
     */
    public static BooleanToken within(Token token1, Token token2,
            double distance) throws IllegalActionException {
        return token1.isCloseTo(token2, distance);
    }

    /** Return a double zero matrix with the given number of rows and
     *  columns.
     *  @return The zero matrix with the given number of rows and
     *   columns.
     *  @deprecated Use zeroMatrixDouble instead.
     */
    public static DoubleMatrixToken zeroMatrix(int rows, int columns) {
        return zeroMatrixDouble(rows, columns);
    }

    /** Return a complex zero matrix with the given number of rows and
     *  columns.
     *  @return The zero matrix with the given number of rows and
     *   columns.
     */
    public static ComplexMatrixToken zeroMatrixComplex(int rows, int columns) {
        try {
            return new ComplexMatrixToken(ComplexMatrixMath.zero(rows, columns));
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(null, ex,
                    "UtilityFunctions.zeroMatrixComplex: "
                            + "Cannot create a ComplexMatrixToken.");
        }
    }

    /** Return a double zero matrix with the given number of rows and
     *  columns.
     *  @return The zero matrix with the given number of rows and
     *   columns.
     */
    public static DoubleMatrixToken zeroMatrixDouble(int rows, int columns) {
        double[][] mtr = new double[rows][columns];
        DoubleMatrixToken result = null;

        try {
            result = new DoubleMatrixToken(mtr, DoubleMatrixToken.DO_NOT_COPY);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(null, ex,
                    "UtilityFunctions.zeroMatrixDouble: "
                            + "Cannot create a DoubleMatrixToken.");
        }

        return result;
    }

    /** Return a int zero matrix with the given number of rows and
     *  columns.
     *  @return The zero matrix with the given number of rows and
     *   columns.
     */
    public static IntMatrixToken zeroMatrixInt(int rows, int columns) {
        int[][] mtr = new int[rows][columns];
        IntMatrixToken result = null;

        try {
            result = new IntMatrixToken(mtr, IntMatrixToken.DO_NOT_COPY);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(null, ex,
                    "UtilityFunctions.zeroMatrixInt: "
                            + "Cannot create a IntMatrixToken.");
        }

        return result;
    }

    /** Return a long zero matrix with the given number of rows and
     *  columns.
     *  @return The zero matrix with the given number of rows and
     *   columns.
     */
    public static LongMatrixToken zeroMatrixLong(int rows, int columns) {
        long[][] mtr = new long[rows][columns];
        LongMatrixToken result = null;

        try {
            result = new LongMatrixToken(mtr, LongMatrixToken.DO_NOT_COPY);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(null, ex,
                    "UtilityFunctions.zeroMatrixLong: "
                            + "Cannot create a LongMatrixToken.");
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Comparator for ascending sort. */
    private static TokenComparator _ASCENDING = new TokenComparator(true);

    /** Comparator for descending sort. */
    private static TokenComparator _DESCENDING = new TokenComparator(false);

    /** The Matrix Parser. The Matrix parser is recreated for the standard
     *  in. However, we use ReInit for the specific matrix files.
     */
    private static MatrixParser _matrixParser;

    /** The random number generator.
     */
    private static Random _random;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Comparator for tokens.
     */
    private static class TokenComparator implements Comparator {
        /** Construct a new comparator.
         *  @param ascending True for ascending comparisons.
         */
        public TokenComparator(boolean ascending) {
            super();
            _ascending = ascending;
        }

        /** Return -1, 0, or 1 depending on whether the first
         *  argument is less than, equal to, or greater
         *  than the second. If the argument to the constructor
         *  was false, then return the converse (to get descending
         *  sorts). The arguments are expected to be instances of
         *  Token, and in particular, to be instances of ScalarToken
         *  (except ComplexToken) or StringToken; otherwise a
         *  ClassCastException will be thrown.
         *  @param arg0 The first argument to compare.
         *  @param arg1 The second argument to compare.
         *  @return The result of comparison.
         *  @exception ClassCastException If the arguments cannot
         *   be compared.
         */
        public int compare(Object arg0, Object arg1) throws ClassCastException {
            // NOTE: This logic indicates which tokens are comparable
            // and is replicated in the sortReturnType() method. If
            // this changes, then that method should also be changed.
            if (arg0 instanceof StringToken && arg1 instanceof StringToken) {
                String string0 = ((StringToken) arg0).stringValue();
                String string1 = ((StringToken) arg1).stringValue();
                int result = string0.compareTo(string1);

                if (_ascending) {
                    return result;
                } else {
                    return -1 * result;
                }
            } else if (arg0 instanceof ScalarToken
                    && arg1 instanceof ScalarToken
                    && !(arg0 instanceof ComplexToken)
                    && !(arg1 instanceof ComplexToken)) {
                ScalarToken cast0 = (ScalarToken) arg0;
                ScalarToken cast1 = (ScalarToken) arg1;

                try {
                    if (cast0.isEqualTo(cast1).booleanValue()) {
                        return 0;
                    } else {
                        if (cast0.isLessThan(cast1).booleanValue()) {
                            if (_ascending) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else {
                            if (_ascending) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    }
                } catch (IllegalActionException ex) {
                    // Fall through to exception below.
                }
            }

            // If we get here, then arguments are not of the correct type.
            // NOTE: The error message is appropriate only for the use of
            // this inside the sort() methods.
            throw new ClassCastException(
                    "Sorting only works on arrays of strings"
                            + " or non-complex scalars.");
        }

        private boolean _ascending;
    }
}
