/* Plot sequences.

 @Copyright (c) 1998-2010 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.gui;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
//// SequencePlotter

/**
 A sequence plotter.  This plotter contains an instance of the Plot
 class from the Ptolemy plot package as a public member. Data at
 the input, which can consist of any number of channels, are plotted
 on this instance.  Each channel is plotted as a separate data set.
 The horizontal axis represents the count of the iterations, scaled
 by the <i>xUnit</i> parameter.  The horizontal increment between
 samples is given by the <i>xUnit</i> parameter.
 Its default value is 1.0. The horizontal value
 of the first sample is given by the <i>xInit</i> parameter.
 Its default value is 0.0. The input is of type DoubleToken.

 @author  Edward A. Lee, Bart Kienhuis
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 */
public class SequencePlotter extends Plotter implements SequenceActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequencePlotter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);

        // set the parameters
        xInit = new Parameter(this, "xInit", new DoubleToken(0.0));
        xInit.setTypeEquals(BaseType.DOUBLE);
        xUnit = new Parameter(this, "xUnit", new DoubleToken(1.0));
        xUnit.setTypeEquals(BaseType.DOUBLE);

        // initialize the parameters
        attributeChanged(xInit);
        attributeChanged(xUnit);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type DoubleToken. */
    public TypedIOPort input;

    /** The increment of the X axis. */
    public Parameter xUnit;

    /** The start point of the X axis. */
    public Parameter xInit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notification that an attribute has changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the expression of the
     *   attribute cannot be parsed or cannot be evaluated.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == xInit) {
            _xInit = ((DoubleToken) xInit.getToken()).doubleValue();
        } else {
            if (attribute == xUnit) {
                _xUnit = ((DoubleToken) xUnit.getToken()).doubleValue();
            } else {
                super.attributeChanged(attribute);
            }
        }
    }

    /** Reset the x axis counter, and call the base class.
     *  Also, clear the datasets that this actor will use.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _xValue = _xInit;

        int width = input.getWidth();
        _connected = new boolean[width];
        for (int i = 0; i < width; i++) {
            _connected[i] = true;
        }
    }

    /** Read at most one token from each input channel and plot it as
     *  a function of the iteration number, scaled by <i>xUnit</i>.
     *  The first point is plotted at the horizontal position given by
     *  <i>xInit</i>. The increments on the position are given by
     *  <i>xUnit</i>. The input data are plotted in postfire() to
     *  ensure that the data have settled.
     *  @exception IllegalActionException If there is no director,
     *   or if the base class throws it.
     *  @return True if it is OK to continue.
     */
    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();
        int offset = ((IntToken) startingDataset.getToken()).intValue();

        for (int i = width - 1; i >= 0; i--) {
            if (input.hasToken(i)) {
                DoubleToken curToken = (DoubleToken) input.get(i);
                if (curToken.isNil()) {
                    _connected[i] = false;
                } else {
                    double curValue = curToken.doubleValue();

                    // NOTE: We assume the superclass ensures this
                    // cast is safe.

                    ((Plot) plot).addPoint(i + offset, _xValue, curValue,
                            _connected[i]);
                    _connected[i] = true;
                }
            }
        }

        _xValue += _xUnit;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** X axis counter. */
    protected double _xValue;

    /** Start of the X axis counter. */
    protected double _xInit;

    /** Increment of the X axis counter. */
    protected double _xUnit;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** An array of booleans, where if an element is true, then the
     *  next point that is plotted for that channel will be connected.
     *  If a channel receives a nil token, then the value for the
     *  corresponding element will be false, meaning the next point will
     *  not be connected to the previous point.
     */
    boolean[] _connected;
}
