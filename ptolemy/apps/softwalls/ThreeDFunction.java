/* Read from a file and create a function of three variables.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
 @ProposedRating Red (acataldo)
 @AcceptedRating Red (reviewmoderator)
 */
package ptolemy.apps.softwalls;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.StringTokenizer;

import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ThreeDFunction

/**
 This creates a function of three variables, defined over some subset
 of R^3.  The function is read from a file which stores the value of the
 function along a lattice of gridpoints.  The begining of the file also
 specifes the index value at each gridpoint, which in turn specifies the
 subset of R^3 for which the dataset is defined.

 @author Adam Cataldo, Christopher X. Brooks
 @version $Id$
 @since Ptolemy II 2.0.1
 */
public class ThreeDFunction implements Serializable {
    /** Construct the functional representation of the 3D dataset by
     *  reading a compressed file.
     *
     *  @param fileName name of file storing the dataset.
     *  @exception IllegalActionException If any exception is
     *     is generated during file I/O.
     */

    //     public ThreeDFunction(String fileName) throws IllegalActionException {
    //         BufferedReader bufferedReader = null;
    //         try {
    //             new BufferedReader(new FileReader(fileName));
    //         } catch (IOException ex) {
    //             throw new IllegalActionException(null, ex,
    //                     "Failed to open '" + fileName + "'");
    //         }
    //         // Read uncompressed data and do not write out compressed data.
    //         this(bufferedReader, false, null);
    //         // Read compressed data and do not write out uncompressed data.
    //         //this(bufferedReader, true, null);
    //     }
    /** Construct the functional representation of the 3D dataset by
     *  reading a compressed file.
     *
     *  @param fileName name of file storing the dataset.
     *  @exception IllegalActionException If any exception is
     *     is generated during file I/O.
     */
    public ThreeDFunction(FileParameter fileParameter)
            throws IllegalActionException {
        // Read uncompressed data and do not write out compressed data.
        this(fileParameter, false, false);

        // Read compressed data and do not write out uncompressed data.
        //this(fileParameter, true, false);
    }

    /**
     *  Constructs the functional representation of the 3D dataset by
     *  reading a compressed or uncompressed file.
     *
     *  <p>The human readable data file has the following format:
     *
     *  <p>The first line is the integer dimension of the state space, which is
     *  should be 3, but is currently ignored
     *
     *  <p>The second line consists of three doubles that define the
     *  x grid information.  The first field is the lower bound, the second
     *  is the step size, the third is the upper bound
     *
     *  <p>The third line consists of three doubles that define the y
     *  grid information.  The format is the same as the x grid
     *  information.
     *
     *  <p>The fourth line consists of three doubles that define the
     *  theta grid information. The format is the same as the x grid
     *  information.
     *
     *  <p> The fifth and successive lines consist of the array.
     *  Each line is one double.

     *  <p> The data can be stored in two formats, uncompressed and
     *  compressed.  In the uncompressed format, a 101 x 101 x 101
     *  array will be about 10Mb.  In compressed format, the same
     *  array will be about 3Mb.
     *
     *  <p>In the compressed format, the 5th line is the initial
     *  value of the [0, 0, 0]th element of the array, and each
     *  successive line is the difference from the previous element.
     *  Values in the compressed format stored with three or four
     *  digits of precision as integers, so there are likely to be
     *  rounding errors.  However, usually this level of precision
     *  is sufficient for our needs.
     *
     *  @param fileParameter FileParameter that names the file to be read
     *  @param compressed True if the input data is compressed
     *  @param writeOutData If true, then write out a version of the
     *  data in the other format.If we read in compressed data,
     *  we write out uncompressed. If we read in uncompressed data,
     *  we write out compressed.  The output file is created in
     *  the current directory with a ".out" extension appended.
     *
     *  @exception IllegalActionException If any exception is
     *     is generated during file I/O.
     */
    public ThreeDFunction(FileParameter fileParameter, boolean compressed,
            boolean writeOutData) throws IllegalActionException {
        int xPoints;
        int yPoints;
        int thetaPoints;
        double xSpan;
        double ySpan;
        double thetaSpan;
        //double dimension;

        BufferedReader reader = null;

        try {
            reader = fileParameter.openForReading();
            System.out.println("Opening file: " + fileParameter.stringValue());
        } catch (IllegalActionException ex) {
            // If we can't open the file, then try it in the classpath
            // The reason we don't just use $CLASSPATH:
            // 1) I think this is only supported after Ptolemy II 3.0.1
            // 2) The softwalls data set is usually 40Mb, and is usually
            // in a separate directory that might not be in the classpath,
            // so we first look as a regular file and then look in the
            // classpath.
            URL url = getClass().getClassLoader().getResource(
                    fileParameter.stringValue());

            if (url == null) {
                throw new IllegalActionException(fileParameter, ex,
                        "Cannot find file '" + fileParameter
                                + "'. Also looked in classpath for '"
                                + fileParameter.stringValue() + "'");
            }

            try {
                reader = new BufferedReader(new InputStreamReader(url
                        .openStream()));
                System.out.println("Opening URL: " + url);
            } catch (IOException ex2) {
                throw new IllegalActionException(fileParameter, ex2,
                        "Cannot find file '" + fileParameter
                                + "', failed to open '" + url + "'");
            }
        }

        try {
            // Read the dimension of the state space and ignore it,
            // since we know it's value is 3.
            /*dimension =*/_readDouble(reader);

            // Read x grid information.
            _xLowerBound = _readDouble(reader);
            _xStepSize = _readDouble(reader);
            _xUpperBound = _readDouble(reader);

            // Read y grid information.
            _yLowerBound = _readDouble(reader);
            _yStepSize = _readDouble(reader);
            _yUpperBound = _readDouble(reader);

            // Read theta grid information.
            _thetaLowerBound = _readDouble(reader);
            _thetaStepSize = _readDouble(reader);
            _thetaUpperBound = _readDouble(reader);

            //             //Complain if the theta values don't make sense
            //             if ((_thetaLowerBound != 0.0) || (_thetaUpperBound >= Math.PI)) {
            //                 throw new IllegalActionException("Bad bounds on theta");
            //             }
            // Initialize the values array;
            xSpan = _xUpperBound - _xLowerBound;
            ySpan = _yUpperBound - _yLowerBound;
            thetaSpan = _thetaUpperBound - _thetaLowerBound;
            xPoints = (int) Math.round(xSpan / _xStepSize) + 1;
            yPoints = (int) Math.round(ySpan / _yStepSize) + 1;
            thetaPoints = (int) Math.round(thetaSpan / _thetaStepSize) + 1;
            _values = new double[xPoints][yPoints][thetaPoints];

            int last = Integer.MIN_VALUE;

            // Fill in the values array with values, sorted in
            // reverse lexicographical order.
            for (int t = 0; t < thetaPoints; t = t + 1) {
                for (int y = 0; y < yPoints; y = y + 1) {
                    for (int x = 0; x < xPoints; x = x + 1) {
                        if (compressed) {
                            // The data is stored in a delta format,
                            // where we record the difference between
                            // the last and the current values.
                            if (last == Integer.MIN_VALUE) {
                                // First data point
                                last = _readInteger(reader);
                                _values[x][y][t] = last / 1000.0;
                            } else {
                                last = last - _readInteger(reader);
                                _values[x][y][t] = last / 1000.0;
                            }
                        } else {
                            _values[x][y][t] = _readDouble(reader);
                        }
                    }
                }
            }

            if (writeOutData) {
                // Write out the other form of data.
                // If we read in compressed data, we write out uncompressed.
                // If we read in uncompressed data, we write out compressed.
                String outputFileName = null;
                String baseName = fileParameter.stringValue();

                // Write into the current directory.
                // We could try to be more crafty here, but writing data
                // is really only for experts
                // The fileParameter might point to something in a jar file
                // so we get the basename by hand.
                if (baseName.indexOf("/") != -1) {
                    outputFileName = baseName.substring(baseName
                            .lastIndexOf("/") + 1, baseName.length())
                            + ".out";
                } else if (baseName.indexOf("\\") != -1) {
                    outputFileName = baseName.substring(baseName
                            .lastIndexOf("\\") + 1, baseName.length())
                            + ".out";
                } else {
                    outputFileName = baseName + ".out";
                }

                System.out.println("Writing " + outputFileName);

                BufferedWriter output = null;

                try {
                    output = new BufferedWriter(new FileWriter(outputFileName));
                    write(output, !compressed);
                } finally {
                    if (output != null) {
                        output.close();
                    }
                }
            }
        } catch (Throwable ex) {
            throw new IllegalActionException(null, ex, "Failed to parse '"
                    + reader + "'");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(null, ex,
                            "Failed to close '" + reader + "'");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Return the approximate value of f(x, y, theta) using trilinear
     *  interpolation.  If x < _xLowerBound, or x >= _xUpperBound, or
     *  y < _yLowerBound, or y >= _yUpperBound, this returns Infinity.
     *
     *  @param x double x input
     *  @param y double y input
     *  @param theta double theta input
     *  @return double represention of f(x, y, theta)
     */
    public double getValue(double x, double y, double theta) {
        int x0Index;
        int x1Index;
        int y0Index;
        int y1Index;
        int theta0Index;
        int theta1Index;
        double xDis;
        double yDis;
        double thetaDis;
        double value;
        double xWeight;
        double yWeight;
        double thetaWeight;
        int xIndex;
        int yIndex;
        int thetaIndex;
        double point;

        // Get the proper value of theta
        theta = _angleWrap(theta);

        if (_inRange(x, y)) {
            // Get the indices for the neighboring points.  x0Index
            // is the x index of the nearest gridpoint less than x,
            // and x1Index is the x index of the nearest gridpoint
            // greater than x.
            x0Index = (int) ((x - _xLowerBound) / _xStepSize);
            x1Index = x0Index + 1;
            y0Index = (int) ((y - _yLowerBound) / _yStepSize);
            y1Index = y0Index + 1;
            theta0Index = (int) ((theta - _thetaLowerBound) / _thetaStepSize);

            // theta1Index will be 0 if the nearest gridpoint greater
            // than theta is 2*pi
            //             double thetaSpan = _thetaUpperBound - _thetaLowerBound;
            //             int maxThetaIndex = (int)Math.round(thetaSpan / _thetaStepSize);
            //             if (theta0Index == maxThetaIndex) {
            //                 theta1Index = 0;
            //             }
            //  When the proper dataset is loaded, use the previous
            //  method.  This will be more error prone.
            if (((2 * Math.PI) - theta) < _thetaStepSize) {
                theta1Index = 0;
            } else {
                theta1Index = theta0Index + 1;
            }

            // Get the normalized distance of x, y, and theta from
            //  the point corresponding to x0Index, y0Index, and
            //  theta0Index.  The distance is scaled by the step size
            //  in each dimension, so these numbers will be between 0
            //  and 1.
            xDis = ((x - _xLowerBound) / _xStepSize) - x0Index;
            yDis = ((y - _yLowerBound) / _yStepSize) - y0Index;
            thetaDis = ((theta - _thetaLowerBound) / _thetaStepSize)
                    - theta0Index;

            // Through a for loop, compute the value.  At each step
            // of the for loop, add the contribution from one of the
            // gridpoints.
            value = 0;

            for (int i = 0; i <= 1; i = i + 1) {
                for (int j = 0; j <= 1; j = j + 1) {
                    for (int k = 0; k <= 1; k = k + 1) {
                        if (i == 0) {
                            xWeight = 1 - xDis;
                            xIndex = x0Index;
                        } else {
                            xWeight = xDis;
                            xIndex = x1Index;
                        }

                        if (j == 0) {
                            yWeight = 1 - yDis;
                            yIndex = y0Index;
                        } else {
                            yWeight = yDis;
                            yIndex = y1Index;
                        }

                        if (k == 0) {
                            thetaWeight = 1 - thetaDis;
                            thetaIndex = theta0Index;
                        } else {
                            thetaWeight = thetaDis;
                            thetaIndex = theta1Index;
                        }

                        point = _values[xIndex][yIndex][thetaIndex];
                        value = value
                                + (point * xWeight * yWeight * thetaWeight);
                    }
                }
            }

            return value;
        } else {
            // The value is out of range, return Infinity.
            return Double.POSITIVE_INFINITY;
        }
    }

    /** Write the data out in human readable uncompressed format
     *  @param output The output file.
     */
    public void write(BufferedWriter output) throws IOException {
        write(output, false);
    }

    /** Write out the data in the human readable format that is
     *  either compressed or uncompressed.
     *  @param output The output file.
     *  @param compressed True if the output should be compressed.
     */
    public void write(BufferedWriter output, boolean compressed)
            throws IOException {
        output.write("3" + "\n");
        output.write(_xLowerBound + "   " + _xStepSize + "   " + _xUpperBound
                + "\n");

        output.write(_yLowerBound + "   " + _yStepSize + "   " + _yUpperBound
                + "\n");

        output.write(_thetaLowerBound + "   " + _thetaStepSize + "   "
                + _thetaUpperBound + "\n");

        // FIXME: we assume the array is regular, that is that
        // all the rows have the same length.
        long last = Math.round(_values[0][0][0] * 1000.0);
        boolean sawFirst = false;

        for (int t = 0; t < _values[0][0].length; t = t + 1) {
            for (int y = 0; y < _values[0].length; y = y + 1) {
                for (int x = 0; x < _values.length; x = x + 1) {
                    if (compressed) {
                        if (!sawFirst) {
                            sawFirst = true;
                            output.write(last + "\n");
                        } else {
                            long current = Math
                                    .round(_values[x][y][t] * 1000.0);
                            output.write(last - current + "\n");
                            last = current;
                        }
                    } else {
                        output.write(_values[x][y][t] + "\n");
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Lower bound for each dimension.
    private double _xLowerBound;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Lower bound for each dimension.
    private double _yLowerBound;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Lower bound for each dimension.
    private double _thetaLowerBound;

    // Step size for each dimension.
    private double _xStepSize;

    // Step size for each dimension.
    private double _yStepSize;

    // Step size for each dimension.
    private double _thetaStepSize;

    // Upper bound for each dimension.
    private double _xUpperBound;

    // Upper bound for each dimension.
    private double _yUpperBound;

    // Upper bound for each dimension.
    private double _thetaUpperBound;

    // The matrix of values on the gridpoint.
    private double[][][] _values;

    // The StringTokenizer being used to read the next double.
    private transient StringTokenizer _tokenizer = new StringTokenizer("");

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     *  Takes in an angular value and returns the equivalant value in
     *  the range [0, 2*Pi).
     *
     *  @param angle radian value of the angle as any real number
     *  @return double in range [0, 2*Pi].
     */
    private double _angleWrap(double angle) {
        double pi = Math.PI;

        while (angle < 0) {
            angle = angle + (2 * pi);
        }

        while (angle >= (2 * pi)) {
            angle = angle - (2 * pi);
        }

        return angle;
    }

    /**
     *  Returns true if the input is in the range stored by the array.
     *  That is, it returns true if x in [_xLowerBound, _xUpperBound),
     *  y in [_yLowerBound, _yUpperBound), and theta in Reals.
     *
     *  The upper bounds are excluded to make the interpolation
     *  routine simpler.
     *
     *  @param x double storing x value
     *  @param y double storing y value
     *  @return boolean
     */
    private boolean _inRange(double x, double y) {
        boolean xOK;
        boolean yOK;

        /* xOK is true if x is in the allowable range. */
        xOK = ((x >= _xLowerBound) && (x < _xUpperBound));
        yOK = ((y >= _yLowerBound) && (y < _yUpperBound));

        return (xOK && yOK);
    }

    /**
     *  If a line has no data, it tries to return the next line.
     *  If no next line exists, it returns null,
     *
     *  @param reader BufferedReader storing the file of interest
     *  @exception IOException if reader throws an IOException
     *  @return double value read from the file
     **/
    private double _readDouble(BufferedReader reader) throws IOException {
        if (_tokenizer.hasMoreTokens()) {
            return (new Double(_tokenizer.nextToken())).doubleValue();
        } else {
            _tokenizer = new StringTokenizer(reader.readLine());
            return _readDouble(reader);
        }
    }

    /**
     *  If a line has no data, it tries to return the next line.
     *  If no next line exists, it returns null,
     *
     *  @param reader BufferedReader storing the file of interest
     *  @exception IOException if reader throws an IOException
     *  @return double value read from the file
     **/
    private int _readInteger(BufferedReader reader) throws IOException {
        if (_tokenizer.hasMoreTokens()) {
            return (new Integer(_tokenizer.nextToken())).intValue();
        } else {
            _tokenizer = new StringTokenizer(reader.readLine());
            return _readInteger(reader);
        }
    }
}
