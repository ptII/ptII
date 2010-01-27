/* A library of statistical operations on arrays of doubles.

 Copyright (c) 1998-2007 The Regents of the University of California.
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
package ptolemy.math;

import java.util.Random;

///////////////////////////////////////////////////////////////////
//// DoubleArrayStat

/**
 This class provides a library for statistical operations on arrays of
 doubles.
 Unless explicitly noted otherwise, all array arguments are assumed to be
 non-null. If a null array is passed to a method, a NullPointerException
 will be thrown in the method or called methods.

 @author Jeff Tsay
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (ctsay)
 @Pt.AcceptedRating Red (ctsay)
 */
public class DoubleArrayStat extends DoubleArrayMath {
    // Protected constructor prevents construction of this class.
    protected DoubleArrayStat() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new array that is the auto-correlation of the
     *  argument array, starting and ending at user-specified lag values.
     *  The output array will have length (endLag - startLag + 1).  The first
     *  element of the output will have the auto-correlation at a lag of
     *  startLag. The last element of the output will have the
     *  auto-correlation at a lag of endLag.
     *  @param x An array of doubles.
     *  @param N An integer indicating the number of samples to sum over.
     *  @param startLag An int indicating at which lag to start (may be
     *  negative).
     *  @param endLag An int indicating at which lag to end.
     *  @return A new array of doubles.
     */
    public static final double[] autoCorrelation(double[] x, int N,
            int startLag, int endLag) {
        int outputLength = endLag - startLag + 1;
        double[] returnValue = new double[outputLength];

        for (int lag = startLag; lag <= endLag; lag++) {
            // Find the most efficient and correct place to start the summation
            int start = Math.max(0, -lag);

            // Find the most efficient and correct place to end the summation
            int limit = Math.min(x.length, N);

            limit = Math.min(limit, x.length - lag);

            double sum = 0.0;

            for (int i = start; i < limit; i++) {
                sum += (x[i] * x[i + lag]);
            }

            returnValue[lag - startLag] = sum;
        }

        return returnValue;
    }

    /** Return the auto-correlation of an array at a certain lag value,
     *  summing over a certain number of samples
     *  defined by :
     *  Rxx[d] = sum of i = 0 to N - 1 of x[i] * x[i + d]
     *  N must be non-negative, but large numbers are ok because this
     *  routine will not overrun reading of the arrays.
     *  @param x An array of doubles.
     *  @param N An integer indicating the number of samples to sum over.
     *  @param lag An integer indicating the lag value (may be negative).
     *  @return A double, Rxx[lag].
     */
    public static double autoCorrelationAt(double[] x, int N, int lag) {
        // Find the most efficient and correct place to start the summation
        int start = Math.max(0, -lag);

        // Find the most efficient and correct place to end the summation
        int limit = Math.min(x.length, N);

        limit = Math.min(limit, x.length - lag);

        double sum = 0.0;

        for (int i = start; i < limit; i++) {
            sum += (x[i] * x[i + lag]);
        }

        return sum;
    }

    /** Return a new array that is the cross-correlation of the two
     *  argument arrays, starting and ending at user-specified lag values.
     *  The output array will have length (endLag - startLag + 1).  The first
     *  element of the output will have the cross-correlation at a lag of
     *  startLag. The last element of the output will have the
     *  cross-correlation at a lag of endLag.
     *  @param x The first array of doubles.
     *  @param y The second array of doubles.
     *  @param N An integer indicating the number of samples to sum over.
     *  @param startLag An int indicating at which lag to start (may be
     *  negative).
     *  @param endLag An int indicating at which lag to end.
     *  @return A new array of doubles.
     */
    public static final double[] crossCorrelation(double[] x, double[] y,
            int N, int startLag, int endLag) {
        int outputLength = endLag - startLag + 1;
        double[] returnValue = new double[outputLength];

        for (int lag = startLag; lag <= endLag; lag++) {
            // Find the most efficient and correct place to start the summation
            int start = Math.max(0, -lag);

            // Find the most efficient and correct place to end the summation
            int limit = Math.min(x.length, N);

            limit = Math.min(limit, y.length - lag);

            double sum = 0.0;

            for (int i = start; i < limit; i++) {
                sum += (x[i] * y[i + lag]);
            }

            returnValue[lag - startLag] = sum;
        }

        return returnValue;
    }

    /** Return the cross-correlation of two arrays at a certain lag value.
     *  The cross-correlation is defined by :
     *  Rxy[d] = sum of i = 0 to N - 1 of x[i] * y[i + d]
     *  @param x The first array of doubles.
     *  @param y The second array of doubles.
     *  @param N An integer indicating the number of samples to sum over.
     *  This must be non-negative, but large numbers are ok because this
     *  routine will not overrun reading of the arrays.
     *  @param lag An integer indicating the lag value (may be negative).
     *  @return A double, Rxy[lag].
     */
    public static double crossCorrelationAt(double[] x, double[] y, int N,
            int lag) {
        // Find the most efficient and correct place to start the summation
        int start = Math.max(0, -lag);

        // Find the most efficient and correct place to end the summation
        int limit = Math.min(x.length, N);

        limit = Math.min(limit, y.length - lag);

        double sum = 0.0;

        for (int i = start; i < limit; i++) {
            sum += (x[i] * y[i + lag]);
        }

        return sum;
    }

    /** Given an array of probabilities, treated as a probability mass
     *  function (pmf), calculate the entropy (in bits). The pmf is
     *  a discrete function that gives the probability of each element.
     *  The sum of the elements in the pmf should be 1, and each element
     *  should be between 0 and 1.
     *  This method does not check to see if the pmf is valid, except for
     *  checking that each entry is non-negative.
     *  The function computed is :
     *  <p>
     *   H(p) = - sum (p[x] * log<sup>2</sup>(p[x]))
     *  </p>
     *  The entropy is always non-negative.
     *  Throw an IllegalArgumentException if the length of the array is 0,
     *  or a negative probability is encountered.
     *  @param p The array of probabilities.
     *  @return The entropy of the array of probabilities.
     */
    public static final double entropy(double[] p) {
        int length = _nonZeroLength(p, "DoubleArrayStat.entropy");

        double h = 0.0;

        for (int i = 0; i < length; i++) {
            if (p[i] < 0.0) {
                throw new IllegalArgumentException(
                        "ptolemy.math.DoubleArrayStat.entropy() : "
                                + "Negative probability encountered.");
            } else if (p[i] == 0.0) {
                // do nothing
            } else {
                h -= (p[i] * ExtendedMath.log2(p[i]));
            }
        }

        return h;
    }

    /** Return the geometric mean of the elements in the array. This
     *  is defined to be the Nth root of the product of the elements
     *  in the array, where N is the length of the array.
     *  This method is only useful for arrays of non-negative numbers. If
     *  the product of the elements in the array is negative, throw an
     *  IllegalArgumentException. However, the individual elements of the array
     *  are not verified to be non-negative.
     *  Return 1.0 if the length of the array is 0.
     *  @param array An array of doubles.
     *  @return A double.
     */
    public static final double geometricMean(double[] array) {
        if (array.length < 1) {
            return 1.0;
        }

        return Math.pow(productOfElements(array), 1.0 / array.length);
    }

    /** Return the maximum value in the array.
     *  Throw an exception if the length of the array is 0.
     *  @param array The array of doubles.
     *  @return The maximum value in the array.
     */
    public static final double max(double[] array) {
        Object[] maxReturn = maxAndIndex(array);
        return ((Double) maxReturn[0]).doubleValue();
    }

    /** Return the maximum value of the array and the index at which the
     *  maximum occurs in the array. The maximum value is wrapped with
     *  the Double class, the index is wrapped with the Integer class,
     *  and an Object array is returned containing these values.
     *  If the array is of length zero, throw an IllegalArgumentException.
     *  @param array An array of doubles.
     *  @return An array of two Objects, returnValue[0] is the Double
     *  representation of the maximum value, returnValue[1] is the Integer
     *  representation of the corresponding index.
     */
    public static final Object[] maxAndIndex(double[] array) {
        int length = _nonZeroLength(array, "DoubleArrayStat.maxAndIndex");
        int maxIndex = 0;

        double maxElement = array[0];

        for (int i = 1; i < length; i++) {
            if (array[i] > maxElement) {
                maxElement = array[i];
                maxIndex = i;
            }
        }

        return new Object[] { Double.valueOf(maxElement),
                Integer.valueOf(maxIndex) };
    }

    /** Return the arithmetic mean of the elements in the array.
     *  If the length of the array is 0, return a NaN.
     *  @param array The array of doubles.
     *  @return The mean value in the array.
     */
    public static final double mean(double[] array) {
        _nonZeroLength(array, "DoubleArrayStat.mean");
        return sumOfElements(array) / array.length;
    }

    /** Return the minimum value in the array.
     *  Throw an exception if the length of the array is 0.
     *  @param array The array of doubles.
     *  @return The minimum value in the array.
     */
    public static final double min(double[] array) {
        Object[] minReturn = minAndIndex(array);
        return ((Double) minReturn[0]).doubleValue();
    }

    /** Return the minimum value of the array and the index at which the
     *  minimum occurs in the array. The minimum value is wrapped with
     *  the Double class, the index is wrapped with the Integer class,
     *  and an Object array is returned containing these values.
     *  If the array is of length zero, throw an IllegalArgumentException.
     *  @param array An array of doubles.
     *  @return An array of two Objects, returnValue[0] is the Double
     *  representation of the minimum value, returnValue[1] is the Integer
     *  representation of the corresponding index.
     */
    public static final Object[] minAndIndex(double[] array) {
        int length = _nonZeroLength(array, "DoubleArrayStat.minAndIndex");
        int minIndex = 0;

        double minElement = array[0];

        for (int i = 1; i < length; i++) {
            if (array[i] < minElement) {
                minElement = array[i];
                minIndex = i;
            }
        }

        return new Object[] { Double.valueOf(minElement),
                Integer.valueOf(minIndex) };
    }

    /** Return the product of all of the elements in the array.
     *  Return 1.0 if the length of the array is 0.
     *  @param array The array of doubles.
     *  @return The product of the elements in the array.
     */
    public static final double productOfElements(double[] array) {
        double product = 1.0;

        for (int i = 0; i < array.length; i++) {
            product *= array[i];
        }

        return product;
    }

    /** Return a new array of Bernoulli random variables with a given
     *  probability of success p. On success, the random variable has
     *  value 1.0; on failure the random variable has value 0.0.
     *  @param p The probability, which should be a double between 0.0
     *  and 1.0.  The probability is compared to the output of
     *  java.lang.Random.nextDouble().  If the output is less than p, then
     *  the array element will be 1.0.  If the output is greater than or
     *  equal to p, then the array element will be 0.0.
     *  @param N The number of elements to allocate.
     *  @return The array of Bernoulli random variables.
     */
    public static final double[] randomBernoulli(double p, int N) {
        double[] returnValue = new double[N];

        if (_random == null) {
            _random = new Random();
        }

        for (int i = 0; i < N; i++) {
            returnValue[i] = (_random.nextDouble() < p) ? 1.0 : 0.0;
        }

        return returnValue;
    }

    /** Return a new array of exponentially distributed doubles with parameter
     *  lambda. The number of elements to allocate is given by N.
     *  @param lambda The lambda, which may not by 0.0.
     *  @param N The number of elements to allocate.
     *  @return The array of exponential random variables.
     */
    public static final double[] randomExponential(double lambda, int N) {
        double[] returnValue = new double[N];

        if (_random == null) {
            _random = new Random();
        }

        for (int i = 0; i < N; i++) {
            double r;

            do {
                r = _random.nextDouble();
            } while (r == 0.0);

            returnValue[i] = -Math.log(r) / lambda;
        }

        return returnValue;
    }

    /** Return a new array of Gaussian distributed doubles with a given
     *  mean and standard deviation. The number of elements to allocate
     *  is given by N.
     *  This algorithm is from [1].
     *  @param mean The mean of the new array.
     *  @param standardDeviation The standard deviation of the new array.
     *  @param N The number of elements to allocate.
     *  @return The array of random Gaussian variables.
     */
    public static final double[] randomGaussian(double mean,
            double standardDeviation, int N) {
        double[] returnValue = new double[N];

        if (_random == null) {
            _random = new Random();
        }

        for (int i = 0; i < N; i++) {
            returnValue[i] = mean
                    + (_random.nextGaussian() * standardDeviation);
        }

        return returnValue;
    }

    /** Return a new array of Poisson random variables (as doubles) with
     *  a given mean. The number of elements to allocate is given by N.
     *  This algorithm is from [1].
     *  @param mean The mean of the new array.
     *  @param N The number of elements to allocate.
     *  @return The array of random Poisson variables.
     */
    public static final double[] randomPoisson(double mean, int N) {
        double[] returnValue = new double[N];

        if (_random == null) {
            _random = new Random();
        }

        for (int i = 0; i < N; i++) {
            double j;
            double u;
            double p;
            double f;

            j = 0.0;
            f = p = Math.exp(-mean);
            u = _random.nextDouble();

            while (f <= u) {
                p *= (mean / (j + 1.0));
                f += p;
                j += 1.0;
            }

            returnValue[i] = j;
        }

        return returnValue;
    }

    /** Return a new array of uniformly distributed doubles ranging from
     *  a to b. The number of elements to allocate is given by N.
     *  @param a A double indicating the lower bound.
     *  @param b A double indicating the upper bound.
     *  @param N An int indicating how many elements to generate.
     *  @return A new array of doubles.
     */
    public static double[] randomUniform(double a, double b, int N) {
        double range = b - a;
        double[] returnValue = new double[N];

        if (_random == null) {
            _random = new Random();
        }

        for (int i = 0; i < N; i++) {
            returnValue[i] = (_random.nextDouble() * range) + a;
        }

        return returnValue;
    }

    /** Given two array's of probabilities, calculate the relative entropy
     *  aka Kullback Leibler distance, D(p || q), (in bits) between the
     *  two probability mass functions. The result will be POSITIVE_INFINITY if
     *  q has a zero probability for a symbol for which p has a non-zero
     *  probability.
     *  The function computed is :
     *  <p>
     *   D(p||q) = - sum (p[x] * log<sup>2</sup>(p[x]/q[x]))
     *  </p>
     *  Throw an IllegalArgumentException if either array has length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     *  @param p An array of doubles representing the first pmf, p.
     *  @param q An array of doubles representing the second pmf, q.
     *  @return A double representing the relative entropy of the
     *  random variable.
     */
    public static final double relativeEntropy(double[] p, double[] q) {
        _nonZeroLength(p, "DoubleArrayStat.relativeEntropy");

        int length = _commonLength(p, q, "DoubleArrayStat.relativeEntropy");

        double d = 0.0;

        for (int i = 0; i < length; i++) {
            if ((p[i] < 0.0) || (q[i] < 0.0)) {
                throw new IllegalArgumentException(
                        "ptolemy.math.DoubleArrayStat.relativeEntropy() : "
                                + "Negative probability encountered.");
            } else if (p[i] == 0.0) {
                // do nothing
            } else if (q[i] == 0.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                d += (p[i] * ExtendedMath.log2(p[i] / q[i]));
            }
        }

        return d;
    }

    /** Return the standard deviation of the elements in the array.
     *  Simply return standardDeviation(array, false).
     *  @param array The input array.
     *  @return The standard deviation.
     */
    public static double standardDeviation(double[] array) {
        return Math.sqrt(variance(array, false));
    }

    /** Return the standard deviation of the elements in the array.
     *  The standard deviation is computed as follows :
     *  <p>
     *  <pre>
     *  stdDev = sqrt(variance)
     *  </pre>
     *  <p>
     *  The sample standard deviation is computed as follows
     *  <p>
     *  <pre>
     *  stdDev = sqrt(variance<sub>sample</sub>)
     *  </pre>
     *  <p>
     *  Throw an exception if the array is of length 0, or if the
     *  sample standard deviation is taken on an array of length less than 2.
     *  @param array An array of doubles.
     *  @param sample True if the sample standard deviation is desired.
     *  @return A double.
     */
    public static double standardDeviation(double[] array, boolean sample) {
        return Math.sqrt(variance(array, sample));
    }

    /** Return the sum of all of the elements in the array.
     *  Return 0.0 of the length of the array is 0.
     *  @param array An array of doubles.
     *  @return The sum of all of the elements in the array.
     */
    public static final double sumOfElements(double[] array) {
        double sum = 0.0;

        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }

        return sum;
    }

    /** Return the variance of the elements in the array, assuming
     *  sufficient statistics.
     *  Simply return variance(array, false).
     *  Throw a runtime exception if the array is of length 0, or if the
     *  sample variance is taken on an array of length less than 2.
     *  @param array An array of doubles.
     *  @return The variance of the elements in the array.
     */
    public static double variance(double[] array) {
        return variance(array, false);
    }

    /** Return the variance of the elements in the array, assuming
     *  sufficient statistics.
     *  The variance is computed as follows :
     *  <p>
     *  <pre>
     *  variance = (sum(X<sup>2</sup>) - (sum(X) / N)<sup>2</sup>) / N
     *  </pre>
     *  <p>
     *  The sample variance is computed as follows :
     *  <p>
     *  <pre>
     *  variance<sub>sample</sub> =
     *       (sum(X<sup>2</sup>) - (sum(X) / N)<sup>2</sup>) / (N - 1)
     *  </pre>
     *  <p>
     *
     *  Throw a runtime exception if the array is of length 0, or if the
     *  sample variance is taken on an array of length less than 2.
     *  @param array An array of doubles.
     *  @param sample True if the sample standard deviation is desired.
     *  @return The variance of the elements in the array.
     */
    public static double variance(double[] array, boolean sample) {
        int length = _nonZeroLength(array, "DoubleArrayStat.variance");

        if (sample && (array.length < 2)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.DoubleArrayStat.variance() : "
                            + "sample variance and standard deviation of an array "
                            + "of length less than 2 are not defined.");
        }

        double ex2 = 0.0;
        double sum = 0.0;

        for (int i = 0; i < length; i++) {
            ex2 += (array[i] * array[i]);
            sum += array[i];
        }

        double norm = sample ? (length - 1) : length;
        double sumSquaredOverLength = (sum * sum) / length;
        return (ex2 - sumSquaredOverLength) / norm;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the array is null or length 0.
     *  Otherwise return the length of the array.
     *  @param array An array of doubles.
     *  @param methodName A String representing the method name of the caller,
     *  without parentheses.
     *  @return The length of the array.
     */
    protected static final int _nonZeroLength(final double[] array,
            String methodName) {
        if (array == null) {
            throw new IllegalArgumentException("ptolemy.math." + methodName
                    + "() : input array is null.");
        }

        if (array.length <= 0) {
            throw new IllegalArgumentException("ptolemy.math." + methodName
                    + "() : input array has length 0.");
        }

        return array.length;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Common instance of Random to be shared by all methods that need
    // a random number.  If we do not share a single Random, then
    // under Windows, closely spaced calls to nextGaussian() on two
    // separate Randoms could yield the same return value.
    private static Random _random;
}
