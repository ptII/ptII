/* Generate discrete events by periodically sampling a CT signal.

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
package ptolemy.domains.ct.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTEventGenerator;
import ptolemy.domains.ct.kernel.CTExecutionPhase;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CTPeriodicSampler

/**
 This actor generates discrete events by periodically sampling a CT signal.
 The events have the values of the input signal at the sampling times. The
 sampling rate is given by parameter "samplePeriod", which has default value
 0.1. The actor has a multi-input port and a multi-output port. Signals in
 each input channel are sampled and produced to corresponding output
 channel.

 @author Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (cxh)
 */
public class CTPeriodicSampler extends Transformer implements CTEventGenerator {
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  The actor can be either dynamic, or not.  It must be set at the
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
     *
     *  @param container The container of this actor.
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public CTPeriodicSampler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        new Parameter(input, "signalType", new StringToken("CONTINUOUS"));
        output.setMultiport(true);
        new Parameter(output, "signalType", new StringToken("DISCRETE"));
        _samplePeriod = 0.1;
        samplePeriod = new Parameter(this, "samplePeriod", new DoubleToken(
                _samplePeriod));

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-30,0 -20,0 -10,0 10,-7\"/>\n"
                + "<polyline points=\"10,0 30,0\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The parameter for the sampling period. It contains a double token
     *  whose default value is 0.1.
     */
    public Parameter samplePeriod;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the local cache of the sampling period if it has been changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the sampling period is
     *  less than or equal to 0.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == samplePeriod) {
            double p = ((DoubleToken) samplePeriod.getToken()).doubleValue();

            if (p <= 0) {
                throw new IllegalActionException(this,
                        " Sample period must be greater than 0.");
            } else {
                _samplePeriod = p;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Generate a discrete event if the current time is one of the sampling
     *  times. The value of the event is the value of the input signal at the
     *  current time.
     *  @exception IllegalActionException If the transfer of tokens failed.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        CTDirector director = (CTDirector) getDirector();

        if ((director.getExecutionPhase() == CTExecutionPhase.GENERATING_EVENTS_PHASE)
                && hasCurrentEvent()) {
            for (int i = 0; i < Math.min(input.getWidth(), output.getWidth()); i++) {
                if (input.hasToken(i)) {
                    Token token = input.get(i);
                    output.send(i, token);

                    if (_debugging) {
                        _debug(getFullName(), " sends event: " + token
                                + " to channel " + i + ", at: "
                                + getDirector().getModelTime());
                    }
                }
            }
        }
    }

    /** Return true if there is a current event. In other words, the current
     *  time is one of the sampling times.
     *  @return If there is a discrete event to emit.
     */
    public boolean hasCurrentEvent() {
        CTDirector director = (CTDirector) getDirector();

        if (director.getModelTime().compareTo(_nextSamplingTime) == 0) {
            _hasCurrentEvent = true;

            if (_debugging && _verbose) {
                _debug(getFullName(), " has an event at: "
                        + director.getModelTime() + ".");
            }
        } else {
            _hasCurrentEvent = false;
        }

        return _hasCurrentEvent;
    }

    /** Set the next sampling time as the start time (i.e. the current time).
     *  We do not register the start time as a breakpoint, since the
     *  director will fire at the start time any way.
     *  @exception IllegalActionException If thrown by the supper class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        CTDirector dir = (CTDirector) getDirector();
        _nextSamplingTime = dir.getModelTime();

        if (_debugging) {
            _debug("Next sampling time is at " + _nextSamplingTime);
        }
    }

    /** Set the next sampling time and return true.
     *  It computes the next sampling time, and registers it as a breakpoint.
     *  @return True.
     *  @exception IllegalActionException If the next sampling time can not be
     *  set as a breakpoint.
     */
    public boolean postfire() throws IllegalActionException {
        CTDirector director = (CTDirector) getDirector();

        if ((director.getExecutionPhase() == CTExecutionPhase.GENERATING_EVENTS_PHASE)
                && hasCurrentEvent()) {
            // register for the next event.
            _nextSamplingTime = _nextSamplingTime.add(_samplePeriod);

            if (_debugging) {
                _debug("Request refiring at " + _nextSamplingTime);
            }

            _fireAt(_nextSamplingTime);
        }

        return true;
    }


    /** Make sure the actor runs inside a CT domain.
     *  @exception IllegalActionException If the director is not
     *  a CTDirector or the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getDirector() instanceof CTDirector)) {
            throw new IllegalActionException("CTPeriodicSampler can only"
                    + " be used inside CT domain.");
        }

        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // flag indicating if there is a current event.
    // NOTE: this variable should be only used inside the hasCurrentEvent
    // method. Other methods can only access the status of this variable
    // via the hasCurrentEvent method.
    private boolean _hasCurrentEvent = false;

    // the next sampling time.
    private Time _nextSamplingTime;

    // the local copy of the sample period.
    private double _samplePeriod;
}
