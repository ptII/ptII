/* An actor that detects level crossings of its trigger input signal.

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
package ptolemy.domains.continuous.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.domains.continuous.kernel.ContinuousStepSizeController;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// LevelCrossingDetector

/**
 An event detector that converts continuous signals to discrete events when
 the input <i>trigger</i> signal crosses a threshold specified by the <i>level</i>
 parameter. The <i>direction</i> parameter
 can constrain the actor to detect only rising or falling transitions.
 It has three possible values, "rising", "falling", and "both", where
 "both" is the default. This actor will produce an output whether the
 input is continuous or not. That is, if a discontinuity crosses the
 threshold in the right direction, it produces an output at the time
 of the discontinuity.  If the input is continuous,
 then the output, then the output is produced when the input is
 within <i>errorTolerance</i> of the level.
 The value of the output is always the same
 as the value of the <i>level</i> parameter.
 <p>
 This actor will not produce an event on its very first firing.
 If you need an output at time zero, then you need generate a
 level crossing discontinuity at time zero.
 <p>
 When the <i>trigger</i> equals the level threshold (within the specified
 <i>errorTolerance</i>), this actor outputs a discrete event with value
 <i>defaultEventValue</i> if <i>useEventValue</i> is selected. Otherwise, the
 actor outputs a discrete event with the value as the level threshold.
 <p>
 This actor controls the step size such that level crossings never
 occur during an integration. So, this actor is only used in Continuous-Time
 domain.

 @author Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class LevelCrossingDetector extends TypedAtomicActor implements
        ContinuousStepSizeController {
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public LevelCrossingDetector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(false);
        trigger.setTypeEquals(BaseType.DOUBLE);

        level = new Parameter(this, "level", new DoubleToken(0.0));
        level.setTypeEquals(BaseType.DOUBLE);

        // By default, this director detects both directions of level crossings.
        direction = new StringParameter(this, "direction");
        direction.setExpression("both");
        _detectRisingCrossing = true;
        _detectFallingCrossing = true;

        direction.addChoice("both");
        direction.addChoice("falling");
        direction.addChoice("rising");

        output.setTypeAtLeast(level);

        _errorTolerance = 1e-4;
        errorTolerance = new Parameter(this, "errorTolerance", new DoubleToken(
                _errorTolerance));
        errorTolerance.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A parameter that can be used to limit the detected level crossings
     *  to rising or falling. There are three choices: "falling", "rising", and
     *  "both". The default value is "both".
     */
    public StringParameter direction;

    /** The error tolerance specifying how close the value of a continuous
     *  input needs to be to the specified level to produce the output event.
     *  Note that this indirectly affects the accuracy of the time of the
     *  output since the output can be produced at any time after the
     *  level crossing occurs while it is still within the specified
     *  error tolerance of the level. This is a double with default 1e-4.
     */
    public Parameter errorTolerance;

    /** The parameter that specifies the level threshold. By default, it
     *  contains a double with value 0.0. Note, a change of this
     *  parameter at run time will not be applied until the next
     *  iteration.
     */
    public Parameter level;

    /** The output port. The type is at least the type of the
     *  <i>level</i> parameter.
     */
    public TypedIOPort output;

    /** The trigger port. This is an input port with type double.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the attribute if it has been changed. If the attribute
     *  is <i>errorTolerance</i> or <i>level</i>, then update the local cache.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the attribute change failed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == errorTolerance) {
            double tolerance = ((DoubleToken) errorTolerance.getToken())
                    .doubleValue();

            if (tolerance <= 0.0) {
                throw new IllegalActionException(this,
                        "Error tolerance must be greater than 0.");
            }

            _errorTolerance = tolerance;
        } else if (attribute == direction) {
            String crossingDirections = direction.stringValue();

            if (crossingDirections.equalsIgnoreCase("falling")) {
                _detectFallingCrossing = true;
                _detectRisingCrossing = false;
            } else if (crossingDirections.equalsIgnoreCase("rising")) {
                _detectFallingCrossing = false;
                _detectRisingCrossing = true;
            } else if (crossingDirections.equalsIgnoreCase("both")) {
                _detectFallingCrossing = true;
                _detectRisingCrossing = true;
            } else {
                throw new IllegalActionException("Unknown direction: "
                        + crossingDirections);
            }
        } else if (attribute == level) {
            _level = ((DoubleToken) level.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        LevelCrossingDetector newObject = (LevelCrossingDetector) super
                .clone(workspace);

        // Set the type constraints.
        newObject.output.setTypeAtLeast(newObject.level);
        return newObject;
    }

    /** Produce an output event if the current input compared to the input
     *  on the last iteration indicates that a level crossing in the
     *  appropriate direction has occurred, if the time is within
     *  <i>errorTolerance</i> of the time at which the crossing occurs.
     *  @exception IllegalActionException If can not get token from the trigger
     *  port or can not send token through the output port.
     */
    public void fire() throws IllegalActionException {
        ContinuousDirector dir = (ContinuousDirector) getDirector();
        double currentStepSize = dir.getCurrentStepSize();

        if (_debugging) {
            _debug("Called fire() at time " + dir.getModelTime()
                    + " with step size " + currentStepSize);
        }
        // If the trigger input is not connected, or there is no
        // token, then produce no output.
        if (trigger.getWidth() < 1 || !trigger.hasToken(0)) {
            return;
        }
        // Record the input.
        _thisTrigger = ((DoubleToken) trigger.get(0)).doubleValue();
        if (_debugging) {
            _debug("-- Consumed a trigger input: " + _thisTrigger);
            _debug("-- Last trigger is: " + _lastTrigger);
        }

        // First firing. Do not produce an output.
        if (_lastTrigger == Double.NEGATIVE_INFINITY) {
            return;
        }

        boolean inputIsIncreasing = _thisTrigger > _lastTrigger;

        // If a crossing has occurred, and either the current step
        // size is zero or the current input is within error tolerance
        // of the level, produce an output.
        if (((_lastTrigger - _level) * (_thisTrigger - _level)) < 0.0
                || _thisTrigger == _level) {
            // Crossing has occurred.
            if (currentStepSize == 0.0
                    || Math.abs(_thisTrigger - _level) < _errorTolerance) {
                // The current time is close enough to when the event happens.
                // If the direction is right, produce an output.
                _eventMissed = false;
                if ((_detectFallingCrossing && !inputIsIncreasing)
                        || (_detectRisingCrossing && inputIsIncreasing)) {
                    if (_debugging) {
                        _debug("-- Event is detected. Produce output.");
                    }
                    output.send(0, new DoubleToken(_level));
                }
            } else {
                // Step size is nonzero and the current input is not
                // close enough. If the direction is right, we have missed an event.
                if ((_detectFallingCrossing && !inputIsIncreasing)
                        || (_detectRisingCrossing && inputIsIncreasing)) {
                    if (_debugging) {
                        _debug("-- Missed an event. Step size will be adjusted.");
                    }
                    _eventMissed = true;
                }
            }
        } else {
            // Level crossing has not occurred.
            _eventMissed = false;
        }
    }

    /** Initialize the execution.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _eventMissed = false;
        _level = ((DoubleToken) level.getToken()).doubleValue();
        _thisTrigger = 0.0;
        _lastTrigger = Double.NEGATIVE_INFINITY;
    }

    /** Return false if with the current step size we miss a level crossing.
     *  @return False if the step size needs to be refined.
     */
    public boolean isStepSizeAccurate() {
        return !_eventMissed;
    }

    /** Prepare for the next iteration, by making the current trigger
     *  token to be the history trigger token.
     *  @return True always.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public boolean postfire() throws IllegalActionException {
        _lastTrigger = _thisTrigger;
        _eventMissed = false;
        return super.postfire();
    }

    /** Make sure the actor runs with a ContinuousDirector.
     *  @exception IllegalActionException If the director is not
     *  a ContinuousDirector or the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getDirector() instanceof ContinuousDirector)) {
            throw new IllegalActionException("LevelCrossingDetector can only"
                    + " be used inside Continuous domain.");
        }
        super.preinitialize();
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        ContinuousDirector dir = (ContinuousDirector) getDirector();
        double refinedStep = dir.getCurrentStepSize();

        if (_eventMissed) {
            // The refined step size is a linear interpolation.
            // NOTE: we always to get a little overshoot to make sure the
            // level crossing happens. The little overshoot chosen here
            // is half of the error tolerance.
            refinedStep = ((Math.abs(_lastTrigger - _level) + (_errorTolerance / 2)) * dir
                    .getCurrentStepSize())
                    / Math.abs(_thisTrigger - _lastTrigger);

            if (_debugging) {
                _debug(getFullName() + "-- Event Missed: refine step to "
                        + refinedStep);
            }
        }
        return refinedStep;
    }

    /** Return the maximum Double value. This actor does not suggest
     *  or constrain the step size for the next iteration.
     *  @return java.Double.MAX_VALUE.
     */
    public double suggestedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The level threshold this actor detects. */
    protected double _level;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag indicating whether this actor detects the level crossing
    // when the input value is rising.
    private boolean _detectRisingCrossing;

    // Flag indicating whether this actor detects the level crossing
    // when the input value is falling.
    private boolean _detectFallingCrossing;

    // Cache of the value of errorTolerance.
    private double _errorTolerance;

    // Flag indicating a missed event.
    private boolean _eventMissed = false;

    // Last trigger input.
    private double _lastTrigger;

    // This trigger input.
    private double _thisTrigger;
}
