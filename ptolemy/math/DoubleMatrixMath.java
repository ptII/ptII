/* A library for mathematical operations on matrices of doubles.

 This file was automatically generated with a preprocessor, so that
 similar matrix operations are supported on ints, longs, floats, and doubles.

 Copyright (c) 1998-2011 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// DoubleMatrixMath

/**
 This class provides a library for mathematical operations on
 matrices of doubles.
 All calls expect matrix arguments to be non-null. In addition, all
 rows of the matrix are expected to have the same number of columns.
 <p>
 Some algorithms are from:
 <p>
 [1] Embree, Paul M. and Bruce Kimble. "C Language Algorithms for Digital
 Signal Processing," Prentice Hall, Englewood Cliffs, NJ, 1991.

 @author Jeff Tsay
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (ctsay)
 @Pt.AcceptedRating Yellow (ctsay)
 */
public class DoubleMatrixMath {
    // private constructor prevents construction of this class.
    private DoubleMatrixMath() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new matrix that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param matrix A matrix of doubles.
     *  @param z The double number to add.
     *  @return A new matrix of doubles.
     */
    public static final double[][] add(double[][] matrix, double z) {
        double[][] returnValue = new double[_rows(matrix)][_columns(matrix)];

        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j] + z;
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second matrix to the first one.  If the two
     *  matrices are not the same size, throw an
     *  IllegalArgumentException.
     *
     *  @param matrix1 The first matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] add(final double[][] matrix1,
            final double[][] matrix2) {
        _checkSameDimension("add", matrix1, matrix2);

        double[][] returnValue = new double[_rows(matrix1)][_columns(matrix1)];

        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                returnValue[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is a copy of the matrix argument.
     *  @param matrix A matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] allocCopy(final double[][] matrix) {
        return crop(matrix, 0, 0, _rows(matrix), _columns(matrix));
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleBinaryOperation to each element in the input matrix,
     *  using z as the left operand in all cases and the matrix elements
     *  as the right operands (op.operate(z, matrix[i][j])).
     */
    public static final double[][] applyBinaryOperation(
            DoubleBinaryOperation op, final double z, final double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(z, matrix[i][j]);
            }
        }

        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleBinaryOperation to each element in the input matrix,
     *  using the matrix elements as the left operands and z as the right
     *  operand in all cases (op.operate(matrix[i][j], z)).
     */
    public static final double[][] applyBinaryOperation(
            DoubleBinaryOperation op, final double[][] matrix, final double z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(matrix[i][j], z);
            }
        }

        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleBinaryOperation to the two matrices, element by element,
     *  using the elements of the first matrix as the left operands
     *  and the elements of the second matrix as the right operands.
     *  (op.operate(matrix1[i][j], matrix2[i][j])).  If the matrices
     *  are not the same size, throw an IllegalArgumentException.
     */
    public static final double[][] applyBinaryOperation(
            DoubleBinaryOperation op, final double[][] matrix1,
            final double[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("applyBinaryOperation", matrix1, matrix2);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(matrix1[i][j], matrix2[i][j]);
            }
        }

        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleUnaryOperation to each element in the input matrix
     *  (op.operate(matrix[i][j])).
     */
    public static final double[][] applyUnaryOperation(
            final DoubleUnaryOperation op, final double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(matrix[i][j]);
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is a sub-matrix of the input
     *  matrix argument. The row and column from which to start
     *  and the number of rows and columns to span are specified.
     *  @param matrix A matrix of doubles.
     *  @param rowStart An int specifying which row to start on.
     *  @param colStart An int specifying which column to start on.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final double[][] crop(final double[][] matrix,
            final int rowStart, final int colStart, final int rowSpan,
            final int colSpan) {
        double[][] returnValue = new double[rowSpan][colSpan];

        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(matrix[rowStart + i], colStart, returnValue[i], 0,
                    colSpan);
        }

        return returnValue;
    }

    /** Return the determinant of a square matrix.
     *  If the matrix is not square, throw an IllegalArgumentException.
     *  This algorithm uses LU decomposition, and is taken from [1]
     */
    public static final double determinant(final double[][] matrix) {
        _checkSquare("determinant", matrix);

        double[][] a;
        double det = 1.0;
        int n = _rows(matrix);

        a = allocCopy(matrix);

        for (int pivot = 0; pivot < (n - 1); pivot++) {
            // find the biggest absolute pivot
            double big = Math.abs(a[pivot][pivot]);
            int swapRow = 0; // initialize for no swap

            for (int row = pivot + 1; row < n; row++) {
                double absElement = Math.abs(a[row][pivot]);

                if (absElement > big) {
                    swapRow = row;
                    big = absElement;
                }
            }

            // unless swapRow is still zero we must swap two rows
            if (swapRow != 0) {
                double[] aPtr = a[pivot];
                a[pivot] = a[swapRow];
                a[swapRow] = aPtr;

                // change sign of determinant because of swap
                det *= -a[pivot][pivot];
            } else {
                // calculate the determinant by the product of the pivots
                det *= a[pivot][pivot];
            }

            // if almost singular matrix, give up now
            // If almost singular matrix, give up now.
            if (Math.abs(det) <= Complex.EPSILON) {
                return det;
            }

            double pivotInverse = 1.0 / a[pivot][pivot];

            for (int col = pivot + 1; col < n; col++) {
                a[pivot][col] *= pivotInverse;
            }

            for (int row = pivot + 1; row < n; row++) {
                double temp = a[row][pivot];

                for (int col = pivot + 1; col < n; col++) {
                    a[row][col] -= (a[pivot][col] * temp);
                }
            }
        }

        // Last pivot, no reduction required.
        det *= a[n - 1][n - 1];

        return det;
    }

    /** Return a new matrix that is constructed by placing the
     *  elements of the input array on the diagonal of the square
     *  matrix, starting from the top left corner down to the bottom
     *  right corner. All other elements are zero. The size of of the
     *  matrix is n x n, where n is the length of the input array.
     */
    public static final double[][] diag(final double[] array) {
        int n = array.length;

        double[][] returnValue = new double[n][n];

        // Assume the matrix is zero-filled.
        for (int i = 0; i < n; i++) {
            returnValue[i][i] = array[i];
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  dividing the second argument to every element.
     *  @param matrix A matrix of doubles.
     *  @param z The double number to divide.
     *  @return A new matrix of doubles.
     */
    public static final double[][] divide(double[][] matrix, double z) {
        double[][] returnValue = new double[_rows(matrix)][_columns(matrix)];

        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j] / z;
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed by element by element
     *  division of the two matrix arguments. Each element of the
     *  first matrix is divided by the corresponding element of the
     *  second matrix.  If the two matrices are not the same size,
     *  throw an IllegalArgumentException.
     */
    public static final double[][] divideElements(final double[][] matrix1,
            final double[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("divideElements", matrix1, matrix2);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] / matrix2[i][j];
            }
        }

        return returnValue;
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The doubles are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m, n) matrix :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param matrix A matrix of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] fromMatrixToArray(final double[][] matrix) {
        return fromMatrixToArray(matrix, _rows(matrix), _columns(matrix));
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The maximum numbers of rows and columns to copy are specified so
     *  that entries lying outside of this range can be ignored. The
     *  maximum rows to copy cannot exceed the number of rows in the matrix,
     *  and the maximum columns to copy cannot exceed the number of columns
     *  in the matrix.
     *  The doubles are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a matrix, limited to m rows and n columns :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param matrix A matrix of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] fromMatrixToArray(final double[][] matrix,
            int maxRow, int maxCol) {
        double[] returnValue = new double[maxRow * maxCol];

        for (int i = 0; i < maxRow; i++) {
            System.arraycopy(matrix[i], 0, returnValue, i * maxCol, maxCol);
        }

        return returnValue;
    }

    /** Return a new matrix, which is defined by Aij = 1/(i+j+1),
     *  the Hilbert matrix. The matrix is square with one
     *  dimension specifier required. This matrix is useful because it always
     *  has an inverse.
     */
    public static final double[][] hilbert(final int dim) {
        double[][] returnValue = new double[dim][dim];

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                returnValue[i][j] = 1.0 / (i + j + 1);
            }
        }

        return returnValue;
    }

    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     *  Note that this method does the same thing as identityDouble(),
     *  but the latter is more useful in the expression language.
     */
    public static final double[][] identity(final int dim) {
        double[][] returnValue = new double[dim][dim];

        // we rely on the fact Java fills the allocated matrix with 0's
        for (int i = 0; i < dim; i++) {
            returnValue[i][i] = 1.0;
        }

        return returnValue;
    }

    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     */
    public static final double[][] identityMatrixDouble(final int dim) {
        return identity(dim);
    }

    /** Return a new matrix that is constructed by inverting the input
     *  matrix. If the input matrix is singular, throw an exception.
     *  This method is from [1].
     *  @exception IllegalArgumentException If the matrix is singular.
     */
    public static final double[][] inverse(final double[][] A) {
        _checkSquare("inverse", A);

        int n = _rows(A);

        double[][] Ai = allocCopy(A);

        // We depend on each of the elements being initialized to 0
        int[] pivotFlag = new int[n];
        int[] swapCol = new int[n];
        int[] swapRow = new int[n];

        int irow = 0;
        int icol = 0;

        for (int i = 0; i < n; i++) { // n iterations of pivoting

            // find the biggest pivot element
            double big = 0.0;

            for (int row = 0; row < n; row++) {
                if (pivotFlag[row] == 0) {
                    for (int col = 0; col < n; col++) {
                        if (pivotFlag[col] == 0) {
                            double absElement = Math.abs(Ai[row][col]);

                            if (absElement >= big) {
                                big = absElement;
                                irow = row;
                                icol = col;
                            }
                        }
                    }
                }
            }

            pivotFlag[icol]++;

            // swap rows to make this diagonal the biggest absolute pivot
            if (irow != icol) {
                for (int col = 0; col < n; col++) {
                    double temp = Ai[irow][col];
                    Ai[irow][col] = Ai[icol][col];
                    Ai[icol][col] = temp;
                }
            }

            // store what we swapped
            swapRow[i] = irow;
            swapCol[i] = icol;

            // if the pivot is zero, the matrix is singular
            if (Ai[icol][icol] == 0.0) {
                throw new IllegalArgumentException(
                        "Attempt to invert a singular matrix.");
            }

            // divide the row by the pivot
            double pivotInverse = 1.0 / Ai[icol][icol];
            Ai[icol][icol] = 1.0; // pivot = 1 to avoid round off

            for (int col = 0; col < n; col++) {
                Ai[icol][col] *= pivotInverse;
            }

            // fix the other rows by subtracting
            for (int row = 0; row < n; row++) {
                if (row != icol) {
                    double temp = Ai[row][icol];
                    Ai[row][icol] = 0.0;

                    for (int col = 0; col < n; col++) {
                        Ai[row][col] -= (Ai[icol][col] * temp);
                    }
                }
            }
        }

        // fix the effect of all the swaps for final answer
        for (int swap = n - 1; swap >= 0; swap--) {
            if (swapRow[swap] != swapCol[swap]) {
                for (int row = 0; row < n; row++) {
                    double temp = Ai[row][swapRow[swap]];
                    Ai[row][swapRow[swap]] = Ai[row][swapCol[swap]];
                    Ai[row][swapCol[swap]] = temp;
                }
            }
        }

        return Ai;
    }

    /** Replace the destinationMatrix argument elements with the values of
     *  the sourceMatrix argument. The destinationMatrix argument must be
     *  large enough to hold all the values of sourceMatrix argument.
     *  @param destinationMatrix A matrix of doubles, used as the destination.
     *  @param sourceMatrix A matrix of doubles, used as the source.
     */
    public static final void matrixCopy(final double[][] sourceMatrix,
            final double[][] destinationMatrix) {
        matrixCopy(sourceMatrix, 0, 0, destinationMatrix, 0, 0,
                _rows(sourceMatrix), _columns(sourceMatrix));
    }

    /** Replace the destinationMatrix argument's values, in the specified row
     *  and column range, with the sourceMatrix argument's values, starting
     *  from specified row and column of the second matrix.
     *  @param sourceMatrix A matrix of doubles, used as the destination.
     *  @param sourceRowStart An int specifying the starting row of the source.
     *  @param sourceColStart An int specifying the starting column of the
     *  source.
     *  @param destinationMatrix A matrix of doubles, used as the destination.
     *  @param destinationRowStart An int specifying the starting row of the
     *  destination.
     *  @param destinationColumnStart An int specifying the starting column
     *  of the destination.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param columnSpan An int specifying how many columns to copy.
     */
    public static final void matrixCopy(final double[][] sourceMatrix,
            final int sourceRowStart, final int sourceColStart,
            final double[][] destinationMatrix, final int destinationRowStart,
            final int destinationColumnStart, final int rowSpan,
            final int columnSpan) {
        // We should verify the parameters here
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(sourceMatrix[sourceRowStart + i], sourceColStart,
                    destinationMatrix[destinationRowStart + i],
                    destinationColumnStart, columnSpan);
        }
    }

    /** Return a new matrix that is constructed from the argument after
     *  performing modulo operation by the second argument to every element.
     *  @param matrix A matrix of doubles.
     *  @param z The double number to divide.
     *  @return A new matrix of doubles.
     */
    public static final double[][] modulo(double[][] matrix, double z) {
        double[][] returnValue = new double[_rows(matrix)][_columns(matrix)];

        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j] % z;
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a scaleFactor.
     */
    public static final double[][] multiply(final double[][] matrix,
            final double scaleFactor) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j] * scaleFactor;
            }
        }

        return returnValue;
    }

    /** Return a new array that is constructed from the argument by
     *  pre-multiplying the array (treated as a row vector) by a matrix.
     *  The number of rows of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of columns of the matrix.
     */
    public static final double[] multiply(final double[][] matrix,
            final double[] array) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (rows != array.length) {
            throw new IllegalArgumentException(
                    "preMultiply : array does not have the same number of "
                            + "elements (" + array.length
                            + ") as the number of rows " + "of the matrix ("
                            + rows + ")");
        }

        double[] returnValue = new double[columns];

        for (int i = 0; i < columns; i++) {
            double sum = 0.0;

            for (int j = 0; j < rows; j++) {
                sum += (matrix[j][i] * array[j]);
            }

            returnValue[i] = sum;
        }

        return returnValue;
    }

    /** Return a new array that is constructed from the argument by
     *  post-multiplying the matrix by an array (treated as a row vector).
     *  The number of columns of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of rows of the matrix.
     */
    public static final double[] multiply(final double[] array,
            final double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (columns != array.length) {
            throw new IllegalArgumentException(
                    "postMultiply() : array does not have the same number "
                            + "of elements (" + array.length
                            + ") as the number of " + "columns of the matrix ("
                            + columns + ")");
        }

        double[] returnValue = new double[rows];

        for (int i = 0; i < rows; i++) {
            double sum = 0.0;

            for (int j = 0; j < columns; j++) {
                sum += (matrix[i][j] * array[j]);
            }

            returnValue[i] = sum;
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  multiplying the first matrix by the second one.
     *  Note this operation is not commutative,
     *  so care must be taken in the ordering of the arguments.
     *  The number of columns of matrix1
     *  must equal the number of rows of matrix2. If matrix1 is of
     *  size m x n, and matrix2 is of size n x p, the returned matrix
     *  will have size m x p.
     *  <p>Note that this method is different from the other multiply()
     *  methods in that this method does not do pointwise multiplication.
     *
     *  @see #multiplyElements(double[][], double[][])
     *  @param matrix1 The first matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] multiply(double[][] matrix1,
            double[][] matrix2) {
        double[][] returnValue = new double[_rows(matrix1)][matrix2[0].length];

        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                double sum = 0.0;

                for (int k = 0; k < matrix2.length; k++) {
                    sum += (matrix1[i][k] * matrix2[k][j]);
                }

                returnValue[i][j] = sum;
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed by element by element
     *  multiplication of the two matrix arguments.  If the two
     *  matrices are not the same size, throw an
     *  IllegalArgumentException.
     *  <p>Note that this method does pointwise matrix multiplication.
     *  See {@link #multiply(double[][], double[][])} for standard
     *  matrix multiplication.
     */
    public static final double[][] multiplyElements(final double[][] matrix1,
            final double[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("multiplyElements", matrix1, matrix2);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] * matrix2[i][j];
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is the additive inverse of the
     *  argument matrix.
     */
    public static final double[][] negative(final double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = -matrix[i][j];
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is formed by orthogonalizing the
     *  columns of the input matrix (the column vectors are
     *  orthogonal). If not all columns are linearly independent, the
     *  output matrix will contain a column of zeros for all redundant
     *  input columns.
     */
    public static final double[][] orthogonalizeColumns(final double[][] matrix) {
        Object[] orthoInfo = _orthogonalizeRows(transpose(matrix));
        return transpose((double[][]) orthoInfo[0]);
    }

    /** Return a new matrix that is formed by orthogonalizing the rows of the
     *  input matrix (the row vectors are orthogonal). If not all rows are
     *  linearly independent, the output matrix will contain a row of zeros
     *  for all redundant input rows.
     */
    public static final double[][] orthogonalizeRows(final double[][] matrix) {
        Object[] orthoInfo = _orthogonalizeRows(matrix);
        return (double[][]) orthoInfo[0];
    }

    /** Return a new matrix that is formed by orthonormalizing the
     *  columns of the input matrix (the column vectors are orthogonal
     *  and have norm 1). If not all columns are linearly independent,
     *  the output matrix will contain a column of zeros for all
     *  redundant input columns.
     */
    public static final double[][] orthonormalizeColumns(final double[][] matrix) {
        return transpose(orthogonalizeRows(transpose(matrix)));
    }

    /** Return a new matrix that is formed by orthonormalizing the
     *  rows of the input matrix (the row vectors are orthogonal and
     *  have norm 1). If not all rows are linearly independent, the
     *  output matrix will contain a row of zeros for all redundant
     *  input rows.
     */
    public static final double[][] orthonormalizeRows(final double[][] matrix) {
        int rows = _rows(matrix);

        Object[] orthoInfo = _orthogonalizeRows(matrix);
        double[][] orthogonalMatrix = (double[][]) orthoInfo[0];
        double[] oneOverNormSquaredArray = (double[]) orthoInfo[2];

        for (int i = 0; i < rows; i++) {
            orthogonalMatrix[i] = DoubleArrayMath.scale(orthogonalMatrix[i],
                    Math.sqrt(oneOverNormSquaredArray[i]));
        }

        return orthogonalMatrix;
    }

    /** Return a pair of matrices that are the decomposition of the
     *  input matrix (which must have linearly independent column
     *  vectors), which is m x n, into the matrix product of Q, which
     *  is m x n with orthonormal column vectors, and R, which is an
     *  invertible n x n upper triangular matrix.
     *  @exception IllegalArgumentException if the columns vectors of the input
     *  matrix are not linearly independent.
     *
     *  @param matrix The input matrix of doubles.
     *  @return The pair of newly allocated matrices of doubles,
     *  out[0] = Q, out[1] = R.
     */
    public static final double[][][] qr(final double[][] matrix) {
        int columns = _columns(matrix);

        /* Find an orthogonal basis using _orthogonalizeRows().  Note
         * that _orthogonalizeRows() orthogonalizes row vectors, so
         * we have use the transpose of input matrix to orthogonalize
         * its columns vectors. The output will be the transpose of
         * Q.
         */
        Object[] orthoRowInfo = _orthogonalizeRows(transpose(matrix));

        double[][] qT = (double[][]) orthoRowInfo[0];

        // get the dot product matrix, dp[j][i] = <inColumn[i], outColumn[j]>
        double[][] dotProducts = (double[][]) orthoRowInfo[1];

        // Normalize the row vectors of qT (column vectors of Q) by
        // dividing by the norm of each row vector.  To compute R,
        // normalize each row of dotProducts by dividing each row the
        // norm of each column vector of Q.
        double[] oneOverNormSquaredArray = (double[]) orthoRowInfo[2];

        // check that all columns were linearly independent
        Integer nullity = (Integer) orthoRowInfo[3];

        if (nullity.intValue() > 0) {
            throw new IllegalArgumentException("qr() : not all "
                    + "column vectors are linearly independent.");
        }

        for (int i = 0; i < columns; i++) {
            double oneOverNorm = Math.sqrt(oneOverNormSquaredArray[i]);
            qT[i] = DoubleArrayMath.scale(qT[i], oneOverNorm);

            // R is upper triangular, so normalize only upper elements
            for (int j = i; j < columns; j++) {
                dotProducts[i][j] *= oneOverNorm;
            }
        }

        return new double[][][] { transpose(qT), dotProducts };
    }

    /** Return a new matrix that is constructed from the argument by
     *  subtracting the second matrix from the first one.  If the two
     *  matrices are not the same size, throw an
     *  IllegalArgumentException.
     */
    public static final double[][] subtract(final double[][] matrix1,
            final double[][] matrix2) {
        _checkSameDimension("subtract", matrix1, matrix2);

        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }

        return returnValue;
    }

    /** Return the sum of the elements of a matrix.
     *  @return The sum of the elements of the matrix.
     */
    public static final double sum(final double[][] matrix) {
        double sum = 0.0;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sum += matrix[i][j];
            }
        }

        return sum;
    }

    /** Return a new matrix that is formed by converting the doubles
     *  in the argument matrix to complex numbers. Each complex number
     *  has real part equal to the value in the argument matrix and a
     *  zero imaginary part.
     *
     *  @param matrix An matrix of double.
     *  @return A new matrix of complex numbers.
     */
    public static final Complex[][] toComplexMatrix(final double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = new Complex(matrix[i][j], 0.0);
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is formed by converting the doubles in
     *  the argument matrix to floats.
     *  @param matrix An matrix of double.
     *  @return A new matrix of floats.
     */
    public static final float[][] toFloatMatrix(final double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        float[][] returnValue = new float[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = (float) matrix[i][j];
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is formed by converting the doubles in
     *  the argument matrix to integers.
     *  @param matrix An matrix of double.
     *  @return A new matrix of integers.
     */
    public static final int[][] toIntegerMatrix(final double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = (int) matrix[i][j];
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is formed by converting the doubles in
     *  the argument matrix to longs.
     *  @param matrix An matrix of double.
     *  @return A new matrix of longs.
     */
    public static final long[][] toLongMatrix(final double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        long[][] returnValue = new long[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = (long) matrix[i][j];
            }
        }

        return returnValue;
    }

    /** Return a new matrix of doubles that is initialized from a 1-D array.
     *  The format of the array must be (0, 0), (0, 1), ..., (0, n-1), (1, 0),
     *  (1, 1), ..., (m-1, n-1) where the output matrix is to be m x n and
     *  entries are denoted by (row, column).
     *  @param array An array of doubles.
     *  @param rows An integer representing the number of rows of the
     *  new matrix.
     *  @param cols An integer representing the number of columns of the
     *  new matrix.
     *  @return A new matrix of doubles.
     */
    public static final double[][] toMatrixFromArray(double[] array, int rows,
            int cols) {
        double[][] returnValue = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, returnValue[i], 0, cols);
        }

        return returnValue;
    }

    /** Return a new String representing the matrix, formatted as
     *  in Java array initializers.
     */
    public static final String toString(final double[][] matrix) {
        return toString(matrix, ", ", "{", "}", "{", ", ", "}");
    }

    /** Return a new String representing the matrix, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final double[][] matrix,
            String elementDelimiter, String matrixBegin, String matrixEnd,
            String vectorBegin, String vectorDelimiter, String vectorEnd) {
        StringBuffer sb = new StringBuffer();
        sb.append(matrixBegin);

        for (int i = 0; i < _rows(matrix); i++) {
            sb.append(vectorBegin);

            for (int j = 0; j < _columns(matrix); j++) {
                sb.append(Double.toString(matrix[i][j]));

                if (j < (_columns(matrix) - 1)) {
                    sb.append(elementDelimiter);
                }
            }

            sb.append(vectorEnd);

            if (i < (_rows(matrix) - 1)) {
                sb.append(vectorDelimiter);
            }
        }

        sb.append(matrixEnd);

        return new String(sb);
    }

    /** Return the trace of a square matrix, which is the sum of the
     *  diagonal entries A<sub>11</sub> + A<sub>22</sub> + ... + A<sub>nn</sub>
     *  Throw an IllegalArgumentException if the matrix is not square.
     *  Note that the trace of a matrix is equal to the sum of its eigenvalues.
     */
    public static final double trace(final double[][] matrix) {
        int dim = _checkSquare("trace", matrix);
        double sum = 0.0;

        for (int i = 0; i < dim; i++) {
            sum += matrix[i][i];
        }

        return sum;
    }

    /** Return a new matrix that is constructed by transposing the input
     *  matrix. If the input matrix is m x n, the output matrix will be
     *  n x m.
     */
    public static final double[][] transpose(final double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[columns][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[j][i] = matrix[i][j];
            }
        }

        return returnValue;
    }

    /** Return true if the elements of the two matrices differ by no more
     *  than the specified distance. The specified distance must be non-negative.
     *  If <i>distance</i> is negative, return false.
     *  @param matrix1 The first matrix.
     *  @param matrix2 The second matrix.
     *  @param distance The distance to use for comparison.
     *  @return True if the elements of the two matrices are within the
     *   specified distance.
     *  @exception IllegalArgumentException If the matrices do not have the same dimension.
     *          This is a run-time exception, so it need not be declared explicitly.
     */
    public static final boolean within(final double[][] matrix1,
            final double[][] matrix2, double distance) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("within", matrix1, matrix2);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if ((matrix1[i][j] > (matrix2[i][j] + distance))
                        || (matrix1[i][j] < (matrix2[i][j] - distance))) {
                    return false;
                }
            }
        }

        return true;
    }

    /** Return true if the elements of the two matrices differ by no more
     *  than the specified distances. The specified distances must all
     *  be non-negative. If any element of <i>errorMatrix</i> is negative,
     *  return false.
     *  @param matrix1 The first matrix.
     *  @param matrix2 The second matrix.
     *  @param errorMatrix The distance to use for comparison.
     *  Note that if errorMatrix contains an element that is negative,
     *  then this method will return false.
     *  @return True if the elements of the two matrices are within the
     *   specified distance.
     *  @exception IllegalArgumentException If the matrices do not have the same dimension.
     *          This is a run-time exception, so it need not be declared explicitly.
     */
    public static final boolean within(final double[][] matrix1,
            final double[][] matrix2, final double[][] errorMatrix) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("within", matrix1, matrix2);
        _checkSameDimension("within", matrix1, errorMatrix);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if ((matrix1[i][j] > (matrix2[i][j] + errorMatrix[i][j]))
                        || (matrix1[i][j] < (matrix2[i][j] - errorMatrix[i][j]))) {
                    return false;
                }
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the two matrix arguments are of the same dimension.
     *  If they are not, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     *  @exception IllegalArgumentException If the arguments do not
     *   have the same dimension.  This is a run-time exception, so it
     *   need not be declared explicitly.
     */
    protected static final void _checkSameDimension(final String caller,
            final double[][] matrix1, final double[][] matrix2)
            throws IllegalArgumentException {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        if ((rows != _rows(matrix2)) || (columns != _columns(matrix2))) {
            throw new IllegalArgumentException("ptolemy.math.DoubleMatrixMath."
                    + caller + "() : one matrix " + _dimensionString(matrix1)
                    + " is not the same size as another matrix "
                    + _dimensionString(matrix2) + ".");
        }
    }

    /** Check that the argument matrix is a square matrix. If the matrix is not
     *  square, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix A matrix of doubles.
     *  @return The dimension of the square matrix.
     *  @exception IllegalArgumentException If the argument is not square.
     *   This is a run-time exception, so it need not be declared explicitly.
     */
    protected static final int _checkSquare(final String caller,
            final double[][] matrix) throws IllegalArgumentException {
        if (_rows(matrix) != _columns(matrix)) {
            throw new IllegalArgumentException("ptolemy.math.DoubleMatrixMath."
                    + caller + "() : matrix argument "
                    + _dimensionString(matrix) + " is not a square matrix.");
        }

        return _rows(matrix);
    }

    /** Return the number of columns of a matrix.
     *  @param matrix The matrix.
     *  @return The number of columns.
     */
    protected static final int _columns(final double[][] matrix) {
        return matrix[0].length;
    }

    /** Return a string that describes the number of rows and columns.
     *  @param matrix The matrix that is to be described.
     *  @return a string describing the dimensions of this matrix.
     */
    protected static final String _dimensionString(final double[][] matrix) {
        return ("[" + _rows(matrix) + " x " + _columns(matrix) + "]");
    }

    /** Given a set of row vectors rowArrays[0] ... rowArrays[n-1], compute :
     * <ol>
     *  <li>A new set of row vectors out[0] ... out[n-1] which are the
     *  orthogonalized versions of each input row vector. If a row
     *  vector rowArray[i] is a linear combination of the last 0 .. i
     *  - 1 row vectors, set array[i] to an array of 0's (array[i]
     *  being the 0 vector is a special case of this). Put the result
     *  in returnValue[0].<br>
     *
     * <li> An n x n matrix containing the dot products of the input
     *     row vectors and the output row vectors,
     *     dotProductMatrix[j][i] = &lt;rowArray[i], outArray[j]&gt;.  Put
     *     the result in returnValue[1].<br>
     *
     * <li> An array containing 1 / (norm(outArray[i])<sup>2</sup>),
     *     with n entries.  Put the result in returnValue[2].<br>
     *
     * <li> A count of the number of rows that were found to be linear
     *     combinations of previous rows. Replace those rows with rows
     *     of zeros. The count is equal to the nullity of the
     *     transpose of the input matrix. Wrap the count with an
     *     Integer, and put it in returnValue[3].
     * </ol>
     *  Orthogonalization is done with the Gram-Schmidt process.
     */
    protected static final Object[] _orthogonalizeRows(
            final double[][] rowArrays) {
        int rows = rowArrays.length;
        int columns = rowArrays[0].length;
        int nullity = 0;

        double[][] orthogonalMatrix = new double[rows][];

        double[] oneOverNormSquaredArray = new double[rows];

        // A matrix containing the dot products of the input row
        // vectors and output row vectors, dotProductMatrix[j][i] =
        // <rowArray[i], outArray[j]>
        double[][] dotProductMatrix = new double[rows][rows];

        for (int i = 0; i < rows; i++) {
            // Get a reference to the row vector.
            double[] refArray = rowArrays[i];

            // Initialize row vector.
            double[] rowArray = refArray;

            // Subtract projections onto all previous vectors.
            for (int j = 0; j < i; j++) {
                // Save the dot product for future use for QR decomposition.
                double dotProduct = DoubleArrayMath.dotProduct(refArray,
                        orthogonalMatrix[j]);

                dotProductMatrix[j][i] = dotProduct;

                rowArray = DoubleArrayMath.subtract(
                        rowArray,
                        DoubleArrayMath.scale(orthogonalMatrix[j], dotProduct
                                * oneOverNormSquaredArray[j]));
            }

            // Compute the dot product between the input and output vector
            // for the diagonal entry of dotProductMatrix.
            dotProductMatrix[i][i] = DoubleArrayMath.dotProduct(refArray,
                    rowArray);

            // Check the norm to find zero rows, and save the 1 /
            // norm^2 for later computation.
            double normSquared = DoubleArrayMath.sumOfSquares(rowArray);

            if (normSquared == 0.0) {
                if (i == 0) {
                    // The input row was the zero vector, we now have
                    // a reference to it.  Set the row to a new zero
                    // vector to ensure the output memory is entirely
                    // disjoint from the input memory.
                    orthogonalMatrix[i] = new double[columns];
                } else {
                    // Reuse the memory allocated by the last
                    // subtract() call -- the row is all zeros.
                    orthogonalMatrix[i] = rowArray;
                }

                // set the normalizing factor to 0.0 to avoid division by 0,
                // it works because the projection onto the zero vector yields
                // zero
                oneOverNormSquaredArray[i] = 0.0;

                nullity++;
            } else {
                orthogonalMatrix[i] = rowArray;
                oneOverNormSquaredArray[i] = 1.0 / normSquared;
            }
        }

        return new Object[] { orthogonalMatrix, dotProductMatrix,
                oneOverNormSquaredArray, Integer.valueOf(nullity) };
    }

    /** Return the number of rows of a matrix.
     *  @param matrix The matrix.
     *  @return The number of rows.
     */
    protected static final int _rows(final double[][] matrix) {
        return matrix.length;
    }
}
