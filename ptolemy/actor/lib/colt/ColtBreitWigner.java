/* An actor that outputs a random sequence with a BreitWigner distribution.

 Copyright (c) 2004-2010 The Regents of the University of California.
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
package ptolemy.actor.lib.colt;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import cern.jet.random.BreitWigner;

///////////////////////////////////////////////////////////////////
//// BreitWigner

/**
 Produce a random sequence with a BreitWigner distribution.  On each
 iteration, a new random number is produced.  The output port is of
 type DoubleToken.  The values that are generated are independent
 and identically distributed with the mean and the standard
 deviation given by parameters.  In addition, the seed can be
 specified as a parameter to control the sequence that is generated.

 <p> This actor instantiates a
 <a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/random/BreitWigner.html">cern.jet.random.BreitWigner</a> object with
 mean, gamma and cut set to 1.0.

 <p>Breit-Wigner is a also know as the Lorentz distribution.
 The Breit-Wigner distribution is a more generate form of the
 Cauchy distribution.

 <p>A definition of the Breit-Wigner distribution can be found at
 <a href="http://rd11.web.cern.ch/RD11/rkb/AN16pp/node23.html#SECTION000230000000000000000"><code>http://rd11.web.cern.ch/RD11/rkb/AN16pp/node23.html#SECTION000230000000000000000</code></a>

 @author David Bauer and Kostas Oikonomou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ColtBreitWigner extends ColtRandomSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColtBreitWigner(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        mean = new PortParameter(this, "mean", new DoubleToken(1.0));
        mean.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(mean.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        gamma = new PortParameter(this, "gamma", new DoubleToken(1.0));
        gamma.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(gamma.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        cut = new PortParameter(this, "cut", new DoubleToken(1.0));
        cut.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(cut.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        cut.moveToFirst();
        gamma.moveToFirst();
        mean.moveToFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** mean.
     *  This is a double with default 1.0.
     */
    public PortParameter mean;

    /** gamma.
     *  This is a double with default 1.0.
     */
    public PortParameter gamma;

    /** cut.
     *  This is a double with default 1.0.
     */
    public PortParameter cut;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a BreitWigner distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        mean.update();
        gamma.update();
        cut.update();
        super.fire();
        output.send(0, new DoubleToken(_current));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method that is called after _randomNumberGenerator is changed.
     */
    protected void _createdNewRandomNumberGenerator() {
        _generator = new BreitWigner(1.0, 1.0, 1.0, _randomNumberGenerator);
    }

    /** Generate a new random number.
     *  @exception IllegalActionException If parameter values are incorrect.
     */
    protected void _generateRandomNumber() throws IllegalActionException {
        double meanValue = ((DoubleToken) mean.getToken()).doubleValue();
        double gammaValue = ((DoubleToken) gamma.getToken()).doubleValue();
        double cutValue = ((DoubleToken) cut.getToken()).doubleValue();

        _current = _generator.nextDouble(meanValue, gammaValue, cutValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The random number for the current iteration. */
    private double _current;

    /** The random number generator. */
    private BreitWigner _generator;
}
