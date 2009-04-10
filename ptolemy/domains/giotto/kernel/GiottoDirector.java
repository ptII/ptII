/* Director for the Giotto model of computation.

 Copyright (c) 2000-2008 The Regents of the University of California.
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


 // NOTE: Downgraded to red due to extensive changes.  EAL
 */
package ptolemy.domains.giotto.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// GiottoDirector

/**
 This class implements a director for the Giotto model of computation
 without Giotto modes. Schedules are generated according to the Giotto
 semantics. The GiottoScheduler class contains methods to compute the
 schedules. The GiottoReceiver class implements the data flow between
 actors using double-buffering.
 <p>
 If the parameter <i>synchronizeToRealTime</i> is set to <code>true</code>,
 then the director will not process events until the real time elapsed
 since the model started matches the time stamp of the event.
 This ensures that the director does not get ahead of real time,
 but, of course, it does not ensure that the director keeps up with
 real time.
 <p>
 @author  Christoph Meyer Kirsch, Edward A. Lee and Haiyang Zheng
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (cm)
 @Pt.AcceptedRating Red (eal)
 @see GiottoScheduler
 @see GiottoReceiver
 */
public class GiottoDirector extends StaticSchedulingDirector implements
        TimedDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public GiottoDirector() {
        super();
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public GiottoDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     */
    public GiottoDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>filename</i>, then close
     *  the current file (if there is one) and open the new one.
     *  If the specified attribute is <i>period</i> or
     *  <i>synchronizeToRealTime</i>, then cache the new values.
     *  If the specified attribute is <i>timeResolution</i>,
     *  then cache the new value.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>filename</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == period) {
            _periodValue = ((DoubleToken) period.getToken()).doubleValue();
        } else if (attribute == synchronizeToRealTime) {
            _synchronizeToRealTime = ((BooleanToken) synchronizeToRealTime
                    .getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return a boolean dependency representing a model-time delay
     *  of the specified amount.
     *  @param delay A non-negative delay.
     *  @return A boolean dependency representing a delay.
     */
    public Dependency delayDependency(double delay) {
        return BooleanDependency.OTIMES_IDENTITY;
    }

    /** Fire a complete iteration and advance time to the current time plus
     *  the period value. A complete iteration consists of several minor cycles.
     *  At each minor cycle, iterate actors in the corresponding minor cycle
     *  schedule. After iterating the actors, increment time by the minor cycle
     *  time. Also, update the receivers that are destinations of all actors
     *  that will be invoked in the next minor cycle of the schedule.
     *  This works because all actors in Giotto are invoked periodically,
     *  and the ones that will be invoked in the next cycle are the ones
     *  that are completing invocation at the end of this cycle.
     *  @exception IllegalActionException If this director does not have a
     *   container.
     */
    public void fire() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container!");
        }

        if (_debugging) {
            _debug("Giotto director firing!");
        }

        if (!_readyToFire) {
            return;
        }

        while ((_unitIndex < _schedule.size()) && !_stopRequested) {
            setModelTime(_expectedNextIterationTime);

            // Grab the next minor cycle (unit) schedule to execute.
            Schedule unitSchedule = (Schedule) _schedule.get(_unitIndex);

            // We only do synchronization to real time here and leave time
            // update to upper level directors or the postfire() method.
            if (_synchronizeToRealTime) {
                long elapsedTime = System.currentTimeMillis() - _realStartTime;
                double elapsedTimeInSeconds = elapsedTime / 1000.0;

                if (_expectedNextIterationTime.getDoubleValue() > elapsedTimeInSeconds) {
                    long timeToWait = (long) ((_expectedNextIterationTime
                            .getDoubleValue() - elapsedTimeInSeconds) * 1000.0);

                    if (timeToWait > 0) {
                        if (_debugging) {
                            _debug("Waiting for real time to pass: "
                                    + timeToWait);
                        }

                        // Synchronize on the scheduler.
                        Scheduler scheduler = getScheduler();

                        synchronized (scheduler) {
                            try {
                                scheduler.wait(timeToWait);
                            } catch (InterruptedException ex) {
                                // Continue executing.
                            }
                        }
                    }
                }
            }

            // Find the actors that will be invoked in this minor cycle (unit)
            // and update the receivers that are their destinations.
            // The reason that the destinations are updated is that any
            // actor that is to be run in this unit is also presumably
            // just completing a run from the previous iteration (because
            // of the periodic semantics of Giotto).
            // Of course, in the first iteration, the actors are not
            // (obviously) completing any previous cycle.  However, according
            // to Giotto semantics, the first unit always involves a firing
            // of every actor.  Hence, the first unit schedule will include
            // every actor.  Thus, in the first iteration, we will be
            // committing the outputs of every actor.  This has the effect
            // of committing any data values that the actor may have produced
            // in its initialize() method.
            Iterator scheduleIterator = unitSchedule.iterator();

            while (scheduleIterator.hasNext()) {
                Actor actor = ((Firing) scheduleIterator.next()).getActor();

                if (_debugging) {
                    _debug("Updating destination receivers of "
                            + ((NamedObj) actor).getFullName());
                }

                List outputPortList = actor.outputPortList();
                Iterator outputPorts = outputPortList.iterator();

                while (outputPorts.hasNext()) {
                    IOPort port = (IOPort) outputPorts.next();
                    Receiver[][] channelArray = port.getRemoteReceivers();

                    for (int i = 0; i < channelArray.length; i++) {
                        Receiver[] receiverArray = channelArray[i];

                        for (int j = 0; j < receiverArray.length; j++) {
                            GiottoReceiver receiver = (GiottoReceiver) receiverArray[j];
                            receiver.update();
                        }
                    }
                }
            }

            scheduleIterator = unitSchedule.iterator();

            while (scheduleIterator.hasNext()) {
                Actor actor = ((Firing) scheduleIterator.next()).getActor();

                if (_debugging) {
                    _debug("Iterating " + ((NamedObj) actor).getFullName());
                }

                if (actor.iterate(1) == STOP_ITERATING) {
                    // FIXME: How to handle this?
                    // put the actor on a no-fire hashtable?
                    System.err.println("Warning: Giotto iterate returned "
                            + "STOP_ITERATING for actor \""
                            + actor.getFullName() + "\"");
                }
            }

            _unitIndex++;

            _expectedNextIterationTime = _expectedNextIterationTime
                    .add(_unitTimeIncrement);

            if (_debugging) {
                _debug("next Iteration time " + _expectedNextIterationTime
                        + "\n");
            }
        }

        if (_unitIndex >= _schedule.size()) {
            _unitIndex = 0;

            // Iteration is complete when the unit index wraps around.
            if (_debugging) {
                _debug("===== Director completing unit of iteration: "
                        + _iterationCount);
            }

            _iterationCount++;
        }
    }

    /** Request a firing of the given actor at the given absolute
     *  time.  This method calculates the period of invocation of
     *  the specified actor (which is the period of this director
     *  divided by the actor's frequency), and if the requested time
     *  is ahead of current time by some multiple of the actor's period,
     *  then return the requested time.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @return The time at which the actor passed as an argument
     *   will be fired.
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     */
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        // No executive director. Return current time plus the period divided
        // by the frequency of the specified actor,
        // or some multiple of that number.
        // NOTE: this is potentially very expensive to compute precisely
        // because the Time class has an infinite range and only supports
        // precise addition. Determining whether the argument satisfies
        // the criterion seems difficult. Hence, we check to be sure
        // that the test is worth doing.
        Time currentTime = getModelTime();
        int frequencyValue = _getActorFrequency((NamedObj)actor);
        double actorPeriod = _periodValue/frequencyValue;
        Time nextFiringTime = currentTime.add(actorPeriod);
        // First check to see whether we are in the initialize phase, in
        // which case, return the start time.
        NamedObj container = getContainer();
        if (container != null) {
            Manager manager = ((CompositeActor) container).getManager();
            if (manager.getState().equals(Manager.INITIALIZING)) {
                return currentTime;
            }
        }
        // Check the most common cases next.
        if (time.equals(currentTime) || time.equals(nextFiringTime)) {
            return nextFiringTime;
        }
        if (time.isInfinite() || currentTime.compareTo(time) > 0) {
            // Either the requested time is infinite or it is in the past.
            return currentTime.add(nextFiringTime);
        }
        Time futureTime = currentTime;
        while (time.compareTo(futureTime) > 0) {
            futureTime = futureTime.add(actorPeriod);
            if (futureTime.equals(time)) {
                return time;
            }
        }
        return currentTime.add(nextFiringTime);
    }

    /** Get the period of the giotto director in ms.
     *  @return int value of period in ms.
     */
    public int getIntPeriod() {
        // In ptolemy model, for simulation, time is double with unit
        // Second however, for giotto code, we need integer and its
        // unit is microSecond
        return (int) (_periodValue * 1000);
    }

    /** Return the next time that this director expects activity.
     *  @return The time of the next iteration.
     */
    public Time getModelNextIterationTime() {
        return getModelTime().add(_unitTimeIncrement);
    }

    /** Get the period of the giotto director in ms.
     *
     *  @return double value of period in ms.
     */
    public double getPeriod() {
        // In ptolemy models, for simulation, time is double with seconds
        // unit; however, for giotto code, we need integer and its
        // unit is milliSecond.
        return _periodValue;
    }

    /** Initialize the actors associated with this director.
     *  The order in which the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        _iterationCount = 0;
        _unitIndex = 0;
        _expectedNextIterationTime = getModelTime();

        // The receivers should be reset before their initialization.
        ListIterator receivers = _receivers.listIterator();
        while (receivers.hasNext()) {
            GiottoReceiver receiver = (GiottoReceiver) receivers.next();
            if (receiver.getContainer() != null) {
                receiver.reset();
            } else {
                // Receiver is no longer in use.
                receivers.remove();
            }
        }

        // FIXME: SampleDelay does not call update().
        //        Need an explicit initialValue??
        super.initialize();

        // Iterate through all output ports to see if any have initialValue
        // parameters or init values from initialization.
        CompositeActor compositeActor = (CompositeActor) (getContainer());
        List actorList = compositeActor.deepEntityList();
        ListIterator actors = actorList.listIterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            List outputPortList = actor.outputPortList();
            Iterator outputPorts = outputPortList.iterator();

            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
                Parameter initialValueParameter = (Parameter) ((NamedObj) port)
                        .getAttribute("initialValue");

                if (initialValueParameter != null) {
                    // Since we delay the transfer of outputs, we have to
                    // make the receivers of the port call 'update'
                    // instead of 'put' only.
                    port.broadcast(initialValueParameter.getToken());

                    Receiver[][] channelArray = port.getRemoteReceivers();

                    for (int i = 0; i < channelArray.length; i++) {
                        Receiver[] receiverArray = channelArray[i];

                        for (int j = 0; j < receiverArray.length; j++) {
                            GiottoReceiver receiver = (GiottoReceiver) receiverArray[j];
                            receiver.update();
                        }
                    }
                }
            }
        }

        // Set the initial time.
        setModelTime(_expectedNextIterationTime);

        _realStartTime = System.currentTimeMillis();
    }

    /** Return a new receiver consistent with the Giotto domain.
     *  @return A new GiottoReceiver.
     */
    public Receiver newReceiver() {
        Receiver receiver = new GiottoReceiver();
        _receivers.add(receiver);
        return receiver;
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or by having an actor in the model
     *  return false in postfire.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Giotto director postfiring!");
        }

        if (!_readyToFire) {
            return true;
        }

        int numberOfIterations = ((IntToken) (iterations.getToken()))
                .intValue();

        if ((numberOfIterations > 0) && (_iterationCount >= numberOfIterations)) {
            // iterations limit is reached
            _iterationCount = 0;

            if (_isEmbedded()) {
                return true;
            } else {
                return false;
            }
        } else {
            // continue iterations
            if (_isEmbedded()) {
                // unless the iteration counts are met,
                // keep requesting to fire itself.
                _requestFiring();
            }
        }

        return true;
    }

    /** This method always return true.
     *  If this director is at the top level, returning true means always
     *  ready to fire.
     *  If embedded, return true usually means that the current time of
     *  the outside domain is greater than or equal to the current time.
     *  However, when a giotto model is used inside a CT model, its inputs
     *  may either be DISCRETE or CONTINUOUS. When the inputs are of type
     *  CONTINUOUS, this method should always return true. To accommodate
     *  this requirement, the prefire method still returns true but
     *  an internal flag will be set to false and the fire and postfire
     *  methods are forced to do nothing.
     *
     *  @return True if the director is ready to run for one iteration.
     *  @exception IllegalActionException If time is set backwards.
     */
    public boolean prefire() throws IllegalActionException {
        if (_isEmbedded()) {
            CompositeActor container = (CompositeActor) getContainer();
            Time outsideCurrentTime = ((Actor) container)
                    .getExecutiveDirector().getModelTime();

            if (outsideCurrentTime.compareTo(_expectedNextIterationTime) < 0) {
                // not the scheduled time to fire.
                _readyToFire = false;
            } else if (outsideCurrentTime.compareTo(_expectedNextIterationTime) == 0) {
                // the outside time is equal to the expected
                // next iteration time...
                setModelTime(outsideCurrentTime);

                if (_debugging) {
                    _debug("Set current time as: " + getModelTime());
                }

                _readyToFire = true;
            } else {
                // the outside time is later than the expected next iteration
                // time. This may happen when a giotto model stops firing at
                // some time and resumes firing after a while. See the giotto
                // composite demo.
                _expectedNextIterationTime = outsideCurrentTime;
                setModelTime(outsideCurrentTime);

                if (_debugging) {
                    _debug("Set current time as: " + getModelTime());
                }

                _readyToFire = true;
            }
        } else {
            _readyToFire = true;
        }

        return true;
    }

    /** Preinitialize the actors associated with this director.
     *  Generate the giotto schedule.
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // before initialize the contained actors, reset the period parameter
        // if the model is embedded inside another giotto model.
        CompositeActor compositeActor = (CompositeActor) (getContainer());

        if (_isEmbedded()) {
            Director executiveDirector = compositeActor.getExecutiveDirector();

            if (executiveDirector instanceof GiottoDirector) {
                double periodValue = ((GiottoDirector) executiveDirector)
                        .getPeriod();
                int frequencyValue = _getActorFrequency(compositeActor);

                _periodValue = periodValue / frequencyValue;
                period.setExpression(Double.toString(_periodValue));
            }
        }

        // Next, construct the schedule.
        // FIXME: Note that mutations will not be supported since the
        // schedule is constructed only once.
        GiottoScheduler scheduler = (GiottoScheduler) getScheduler();
        _schedule = scheduler.getSchedule();
        _unitTimeIncrement = scheduler._getMinTimeStep(_periodValue);
    }

    /** Return an array of suggested directors to be used with
     *  ModalModel. Each director is specified by its full class
     *  name.  The first director in the array will be the default
     *  director used by a modal model.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    public String[] suggestedModalModelDirectors() {
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[0] = "ptolemy.domains.fsm.kernel.NonStrictFSMDirector";
        defaultSuggestions[1] = "ptolemy.domains.fsm.kernel.FSMDirector";
        return defaultSuggestions;
    }

    /** Transfer data from an input port of the container to the ports
     *  it is connected to on the inside. The port argument must be an
     *  opaque input port. If any channel of the input port has no data,
     *  then that channel is ignored. This method will transfer exactly
     *  one token on each input channel that has at least one token
     *  available. Update all receivers to which a token is transferred.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque"
                            + "input port.");
        }

        boolean transfer = false;
        Receiver[][] insideReceivers = port.deepGetReceivers();

        for (int i = 0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                Token t = port.get(i);

                if ((insideReceivers != null) && (insideReceivers[i] != null)) {
                    if (_debugging) {
                        _debug(getName(), "transferring input from "
                                + port.getName() + " channel " + i);
                    }

                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        if (_debugging) {
                            _debug("Sending token to receiver of "
                                    + insideReceivers[i][j].getContainer());
                        }

                        insideReceivers[i][j].put(t);
                        ((GiottoReceiver) insideReceivers[i][j]).update();
                    }

                    transfer = true;
                }
            }
        }

        return transfer;
    }

    /** Transfer data from this port to the ports it is connected to on
     *  the outside.
     *  This port must be an opaque output port.  If any
     *  channel of this port has no data, then that channel is
     *  ignored. This method will transfer exactly one token on
     *  each output channel that has at least one token available.
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(port,
                    "transferOutputs: this port is not "
                            + "an opaque output port.");
        }

        boolean wasTransferred = false;
        Receiver[][] insideReceivers = port.getInsideReceivers();

        if (insideReceivers != null) {
            for (int i = 0; i < insideReceivers.length; i++) {
                if (insideReceivers[i] != null) {
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        try {
                            if (insideReceivers[i][j].isKnown()) {
                                if (insideReceivers[i][j].hasToken()) {
                                    if (_debugging) {
                                        _debug(getName(),
                                                "transferring output from "
                                                        + port.getName()
                                                        + " to channel " + i);
                                    }

                                    Token t = ((GiottoReceiver) insideReceivers[i][j])
                                            .remove();
                                    port.send(i, t);
                                    wasTransferred = true;
                                }
                            }
                        } catch (NoTokenException ex) {
                            throw new InternalErrorException(port, ex, null);
                        }
                    }
                }
            }
        }

        return wasTransferred;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of times that postfire may be called before it
     *  returns false. If the value is less than or equal to zero,
     *  then the execution will never return false in postfire,
     *  and thus the execution can continue forever.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    /** The period of an iteration. This is a double that defaults to
     *  <I>0.1</I>.
     */
    public Parameter period;

    /** Specify whether the execution should synchronize to the
     *  real time. This parameter must contain a BooleanToken.
     *  If this parameter is true, then do not process events until the
     *  elapsed real time matches the time stamp of the events.
     *  The value defaults to false.
     */
    public Parameter synchronizeToRealTime;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The static default Giotto period is 100ms.
     */
    protected static final double _DEFAULT_GIOTTO_PERIOD = 0.1;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the frequency of the specified actor by accessing a
     *  parameter named "frequency". If there is no such parameter,
     *  return 1.
     */
    private int _getActorFrequency(NamedObj actor)
            throws IllegalActionException {
        int frequencyValue = 1;
        Attribute frequency = actor.getAttribute("frequency");
        if (frequency instanceof Parameter) {
            Token result = ((Parameter)frequency).getToken();
            if (result instanceof IntToken) {
                frequencyValue = ((IntToken) result).intValue();
            }
        }
        return frequencyValue;
    }

    // Initialize the director by creating a scheduler and parameters.
    private void _init() {
        try {
            GiottoScheduler scheduler = new GiottoScheduler(workspace());
            setScheduler(scheduler);

            period = new Parameter(this, "period");
            period.setToken(new DoubleToken(_DEFAULT_GIOTTO_PERIOD));
            iterations = new Parameter(this, "iterations", new IntToken(0));

            synchronizeToRealTime = new Parameter(this,
                    "synchronizeToRealTime", new BooleanToken(false));

            timeResolution.setVisibility(Settable.FULL);
        } catch (KernelException ex) {
            throw new InternalErrorException("Cannot initialize director: "
                    + ex.getMessage());
        }
    }

    // Request that the container of this director be refired in the future.
    // This method is used when the director is embedded inside an opaque
    // composite actor. If the outside director is a Giotto director, this
    // method has no effect. If the outside director is a DE director, this
    // method will cause the container of this director to be fired again.
    private void _requestFiring() throws IllegalActionException {
        if (_debugging) {
            _debug("Request refiring of opaque composite actor at "
                    + _expectedNextIterationTime);
        }
        // Enqueue a refire for the container of this director.
        _fireContainerAt(_expectedNextIterationTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The time for next iteration.
    private Time _expectedNextIterationTime;

    // The count of iterations executed.
    private int _iterationCount = 0;

    // Period of the director.
    private double _periodValue = 0.0;

    // Flag to indicate whether the current director is ready to fire.
    private boolean _readyToFire = true;

    // The real time at which the initialize() method was invoked.
    private long _realStartTime = 0;

    // List of all receivers this director has created.
    private LinkedList _receivers = new LinkedList();

    // Schedule to be executed.
    private Schedule _schedule;

    // Specify whether the director should wait for elapsed real time to
    // catch up with model time.
    private boolean _synchronizeToRealTime = false;

    // Counter for minimum time steps.
    private int _unitIndex = 0;

    // Minimum time step size (a Giotto "unit").
    private double _unitTimeIncrement = 0.0;
}
