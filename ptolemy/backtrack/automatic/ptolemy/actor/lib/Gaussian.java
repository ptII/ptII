/* An actor that outputs a random sequence with a Gaussian distribution.

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
//////////////////////////////////////////////////////////////////////////
//// Gaussian
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** 
 * Produce a random sequence with a Gaussian distribution.  On each
 * iteration, a new random number is produced.  The output port is of
 * type DoubleToken.  The values that are generated are independent
 * and identically distributed with the mean and the standard
 * deviation given by parameters.  In addition, the seed can be
 * specified as a parameter to control the sequence that is generated.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 0.2
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Green (bilung)
 */
public class Gaussian extends RandomSource implements Rollbackable {

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**     
     * The mean of the random number.
     * This has type double, initially with value 0.
     */
    public PortParameter mean;

    /**     
     * The standard deviation of the random number.
     * This has type double, initially with value 1.
     */
    public PortParameter standardDeviation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////
    /**     
     * The random number for the current iteration. 
     */
    protected double _current;

    /**     
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public Gaussian(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output.setTypeEquals(BaseType.DOUBLE);
        mean = new PortParameter(this, "mean", new DoubleToken(0.0));
        mean.setTypeEquals(BaseType.DOUBLE);
        standardDeviation = new PortParameter(this, "standardDeviation");
        standardDeviation.setExpression("1.0");
        standardDeviation.setTypeEquals(BaseType.DOUBLE);
    }

    /**     
     * Send a random number with a Gaussian distribution to the output.
     * This number is only changed in the prefire() method, so it will
     * remain constant throughout an iteration.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        mean.update();
        standardDeviation.update();
        output.send(0, new DoubleToken(_current));
    }

    /**     
     * Generate a new random number.
     * @exception IllegalActionException If parameter values are incorrect.
     */
    protected void _generateRandomNumber() throws IllegalActionException  {
        double meanValue = ((DoubleToken)(mean.getToken())).doubleValue();
        double standardDeviationValue = ((DoubleToken)(standardDeviation.getToken())).doubleValue();
        double rawNum = _random.nextGaussian();
        _current = (rawNum * standardDeviationValue) + meanValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        super.$RESTORE(timestamp, trim);
    }

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
        };

}

