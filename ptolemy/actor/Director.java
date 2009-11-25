/* A Director governs the execution of a CompositeActor.

 Copyright (c) 1997-2009 The Regents of the University of California.
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
package ptolemy.actor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.ExtendedMath;

//////////////////////////////////////////////////////////////////////////
//// Director

/**
 A Director governs the execution within a CompositeActor.  A composite actor
 that contains a director is said to be <i>opaque</i>, and the execution model
 within the composite actor is determined by the contained director.   This
 director is called the <i>local director</i> of a composite actor.
 A composite actor is also aware of the director of its container,
 which is referred to as its <i>executive director</i>.
 A director may also be contained by a CompositeEntity that is not a
 CompositeActor, in which case it acts like any other entity within
 that composite.
 <p>
 A top-level composite actor is generally associated with a <i>manager</i>
 as well as a local director.  The Manager has overall responsibility for
 executing the application, and is often associated with a GUI.   Top-level
 composite actors have no executive director and getExecutiveDirector() will
 return null.
 <p>
 A local director is responsible for invoking the actors contained by the
 composite.  If there is no local director, then the executive director
 is given the responsibility.  The getDirector() method of CompositeActor,
 therefore, returns the local director, if there is one, and otherwise
 returns the executive director.  Thus, it returns whichever director
 is responsible for executing the contained actors, or null if there is none.
 Whatever it returns is called simply the <i>director</i> (vs. local
 director or executive director).
 <p>
 A director implements the action methods (preinitialize(),
 initialize(), prefire(), fire(), postfire(), iterate(),
 and wrapup()).  In this base class, default implementations
 are provided that may or may not be useful in specific domains.   In general,
 these methods will perform domain-dependent actions, and then call the
 respective methods in all contained actors.
 <p>
 The director also provides methods to optimize the iteration portion of an
 execution. This is done by setting the workspace to be read-only during
 an iteration. In this base class, the default implementation results in
 a read/write workspace. Derived classes (e.g. domain specific
 directors) should override the _writeAccessRequired() method to report
 that write access is not required. If none of the directors in a simulation
 require write access, then it is safe to set the workspace to be read-only,
 which will result in faster execution.
 <p>
 This class also specifies a parameter <i>timeResolution</i>. This is a double
 with default 1E-10, which is 10<sup>-10</sup>.
 All time values are rounded to the nearest multiple of this
 value. If the value is changed during a run, an exception is thrown.
 This is a shared parameter, which means
 that all instances of Director in the model will have the same value for
 this parameter. Changing one of them changes all of them.
 <p>
 The <i>timeResolution</i> parameter is not visible to the user
 by default. Subclasses can make it visible by calling
 <pre>
 timeResolution.setVisibility(Settable.FULL);
 </pre>
 in their constructors.

 @author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer, John Reekie
 @version $Id$
 @since Ptolemy II 0.2

 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class Director extends Attribute implements Executable {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public Director() {
        super();
        _addIcon();
        _initializeParameters();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  Create the timeResolution parameter.
     *
     *  @param container The container.
     *  @param name The name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container, or if
     *   the time resolution parameter is malformed.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public Director(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _addIcon();
        _initializeParameters();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public Director(Workspace workspace) {
        super(workspace);
        _addIcon();
        _initializeParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The time precision used by this director. All time values are
     *  rounded to the nearest multiple of this number. This is a double
     *  that defaults to "1E-10" which is 10<sup>-10</sup>.
     *  This is a shared parameter, meaning that changing one instance
     *  in a model results in all instances being changed.
     */
    public SharedParameter timeResolution;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified object to the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#addPiggyback(Executable)
     */
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
    }

    /** Override the base class to update local variables.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If timeResolution is
     *   being changed and the model is executing (and not in
     *   preinitialize()).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == timeResolution) {
            // This is extremely frequently used, so cache the value.
            // Prevent this from changing during a run!
            double newResolution = ((DoubleToken) timeResolution.getToken())
                    .doubleValue();

            // FindBugs reports this comparison as a problem, but it
            // is not an issue because we usually don't calculate
            // _timeResolution, we set it.
            if (newResolution != _timeResolution) {
                NamedObj container = getContainer();

                if (container instanceof Actor) {
                    Manager manager = ((Actor) container).getManager();

                    if (manager != null) {
                        Manager.State state = manager.getState();

                        if ((state != Manager.IDLE)
                                && (state != Manager.PREINITIALIZING)) {
                            throw new IllegalActionException(this,
                                    "Cannot change timePrecision during a run.");
                        }
                    }
                }

                if (newResolution <= ExtendedMath.DOUBLE_PRECISION_SMALLEST_NORMALIZED_POSITIVE_DOUBLE) {
                    throw new IllegalActionException(
                            this,
                            "Invalid timeResolution: "
                                    + newResolution
                                    + "\n The value must be "
                                    + "greater than the smallest, normalized, "
                                    + "positive, double value with a double "
                                    + "precision: "
                                    + ExtendedMath.DOUBLE_PRECISION_SMALLEST_NORMALIZED_POSITIVE_DOUBLE);
                }

                _timeResolution = newResolution;
            }
        }

        super.attributeChanged(attribute);
    }

    /** Create the schedule for this director, if necessary.
     *  In this base class nothing is done.
     *  @exception IllegalActionException If the schedule can't be created.
     */
    public void createSchedule() throws IllegalActionException {
    }

    /** Return a default dependency to use between input input
     *  ports and output ports.
     *  Director subclasses may override this if
     *  need specialized dependency. This base class
     *  returns {@link BooleanDependency}.OTIMES_IDENTITY.
     *  @see Dependency
     *  @see CausalityInterface
     *  @see Actor#getCausalityInterface()
     *  @return A default dependency between input ports
     *   and output ports.
     */
    public Dependency defaultDependency() {
        if (_isEmbedded()) {
            return ((CompositeActor) getContainer()).getExecutiveDirector()
                    .defaultDependency();
        } else {
            return BooleanDependency.OTIMES_IDENTITY;
        }
    }
    
    /** Request that after the current iteration finishes postfire() returns
     *  false, indicating to the environment that no more iterations should
     *  be invoked. To support domains where actor firings do not necessarily
     *  terminate, such as PN, you may wish to call stopFire() as well to request
     *  that those actors complete their firings.
     */
    public void finish() {
        _finishRequested = true;
    }

    /** Iterate all the deeply contained actors of the
     *  container of this director exactly once. This method is not functional,
     *  since an iteration of the deeply contained actors may change
     *  state in their postfire() method. The actors are iterated
     *  in the order that they appear on the list returned by deepEntityList(),
     *  which is normally the order in which they were created.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  <p>
     *  In this base class, an attempt is made to fire each actor exactly
     *  once, in the order they were created.  Prefire is called once, and
     *  if prefire returns true, then fire is called once, followed by
     *  postfire.  The return value from postfire is ignored. If the
     *  container is not an instance of CompositeActor, however, then
     *  this method does nothing.
     *
     *  @exception IllegalActionException If any called method of one
     *  of the associated actors throws it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Director: Called fire().");
        }

        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();
            int iterationCount = 1;

            while (actors.hasNext() && !_stopRequested) {
                Actor actor = (Actor) actors.next();

                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.BEFORE_ITERATE, iterationCount));
                }

                if (actor.iterate(1) == Executable.STOP_ITERATING) {
                    if (_debugging) {
                        _debug("Actor requests halt: "
                                + ((Nameable) actor).getFullName());
                    }

                    break;
                }

                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.AFTER_ITERATE, iterationCount));
                }
            }
        }
    }

    /** Request a firing of the given actor at the given absolute
     *  time.  This method is only intended to be called from within
     *  main simulation thread.  Actors that create their own
     *  asynchronous threads should used the fireAtCurrentTime()
     *  method to schedule firings.
     *
     *  This method calls {@link #fireAt(Actor, Time)} method.
     *
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     *  @deprecated Instead of using double as time argument, use a
     *  time object instead. As of Ptolemy 4.1, replaced by
     *  {@link #fireAt(Actor, Time)}
     */
    public void fireAt(Actor actor, double time) throws IllegalActionException {
        fireAt(actor, new Time(this, time));
    }

    /** Request a firing of the given actor at the given model
     *  time.  This base class ignores the request and returns a Time
     *  with value Double.NEGATIVE_INFINITY, unless this director
     *  is embedded within a model (and has an executive director),
     *  in which case, this base class requests that the executive
     *  director fire the container of this director at the requested
     *  time.
     *  <p>
     *  The intent of this method is to request a firing of the actor
     *  at the specified time, but this implementation does not assure
     *  that. In particular, if there is no executive director, it
     *  completely ignores the request. If there is an executive director,
     *  then it is not required to do the firing at the specified time.
     *  In particular, derived classes may override this method
     *  and modify the time of the firing, for example to prevent
     *  attempts to fire an actor in the past.
     *  <p>
     *  Derived classes should override this method to return the time at which
     *  they expect to fire the specified actor. It is up to the actor
     *  to throw an exception if it is not acceptable for the time
     *  to differ from the requested time.
     *  <p>
     *  Note that it is not correct behavior for a director to override
     *  this method to simply fire the specified actor. The actor needs
     *  to be fired as part of the regular execution cycle of that director,
     *  and that needs to occur after this method has returned.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @return An instance of Time with value NEGATIVE_INFINITY, or
     *   if there is an executive director, the time at which the
     *   container of this director will next be fired
     *   in response to this request.
     *  @see #fireAtCurrentTime(Actor)
     *  @exception IllegalActionException If there is an executive director
     *   and it throws it. Derived classes may choose to throw this
     *   exception for other reasons.
     */
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        // If this director is enclosed within another model, then we
        // pass the request up the chain. If the enclosing executive director
        // respects fireAt(), this will result in the container of this
        // director being iterated again at the requested time. This is
        // reasonable, for example, in FSM, DE, SR, CT, or any domain that
        // can safely fire actors other than the one that requested the
        // refiring at the specified time.
        if (_isEmbedded()) {
            CompositeActor container = (CompositeActor) getContainer();
            return container.getExecutiveDirector().fireAt(container, time);
        }
        // If we are not embedded, then return a value indicating that the
        // fireAt() request is being ignored. If the caller is OK with that,
        // then no problem.
        // All derived classes of Director for which the fireAt() method is
        // useful, should override this behavior.
        return new Time(this, Double.NEGATIVE_INFINITY);
    }

    /** Request a firing of the given actor at the current model time.
     *  This base class simply calls fireAt(actor, getModelTime())
     *  and returns whatever that returns. Note that fireAt() will modify
     *  the requested time if it happens to be in the past by the time
     *  it is to be posted on the event queue, which is exactly what we
     *  want. Note that the returned time may not match the value
     *  returned by getModelTime() prior to this call.
     *  <p>
     *  Note that it is not correct behavior for a director to override
     *  this method to simply fire the specified actor. The actor needs
     *  to be fired as part of the regular execution cycle of that director,
     *  and that needs to occur after this method has returned.
     *  @param actor The actor to be fired.
     *  @return The time at which the specified actor will be fired,
     *   which in this case is whatever fireAt() returns.
     *  @see #fireAt(Actor, Time)
     *  @exception IllegalActionException If this method is called
     *  before the model is running.
     */
    public Time fireAtCurrentTime(Actor actor) throws IllegalActionException {
        return fireAt(actor, getModelTime());
    }

    /** Return a causality interface for the composite actor that
     *  contains this director. This base class returns an
     *  instance of {@link CausalityInterfaceForComposites}, but
     *  subclasses may override this to return a domain-specific
     *  causality interface.
     *  @return A representation of the dependencies between input ports
     *   and output ports of the container.
     */
    public CausalityInterface getCausalityInterface() {
        return new CausalityInterfaceForComposites((Actor) getContainer(),
                defaultDependency());
    }

    /** Return the current time value of the model being executed by this
     *  director. This time can be set with the setCurrentTime method.
     *  In this base class, time never increases, and there are no restrictions
     *  on valid times.
     *
     *  @return The current time value.
     *  @deprecated As of Ptolemy II 4.1, replaced by
     *  {@link #getModelTime()}
     *  @see #setCurrentTime(double)
     */
    public double getCurrentTime() {
        return getModelTime().getDoubleValue();
    }

    /** Return the error tolerance, if any, of this director.
     *  By default, a director has no error tolerance, so this method
     *  returns 0.0. Some directors override this to allow computed
     *  values to be approximate with a specified precision.
     *  @return the error tolerance.
     */
    public double getErrorTolerance() {
        return 0.0;
    }
    
    /** Return the global time for this model. The global time is
     *  defined to be the value returned by the @link{#getModelTime()}
     *  method of the top-level director in the model.
     *  @return The time of the top-level director in the model.
     * @throws IllegalActionException If the top level is not an Actor.
     */
    public Time getGlobalTime() throws IllegalActionException {
        NamedObj toplevel = toplevel();
        if (!(toplevel instanceof Actor)) {
            throw new IllegalActionException(this,
                    "Cannot get a global time because the top level is not an actor."
                    + " It is "
                    + toplevel);
        }
        return (((Actor)toplevel).getDirector()).getModelTime();
    }

    /** Return the next time of interest in the model being executed by
     *  this director or the director of any enclosing model up the
     *  hierarchy. If this director is at the top level, then this
     *  default implementation simply returns the current time, since
     *  this director does not advance time. If this director is not
     *  at the top level, then return whatever the enclosing director
     *  returns.
     *  <p>
     *  This method is useful for domains that perform
     *  speculative execution (such as CT).  Such a domain in a hierarchical
     *  model (i.e. CT inside DE) uses this method to determine how far
     *  into the future to execute.
     *  <p>
     *  Derived classes should override this method to provide an appropriate
     *  value, if possible. For example, the DEDirector class returns the
     *  time value of the next event in the event queue.
     *  @return The time of the next iteration.
     *  @see #getModelTime()
     */
    public Time getModelNextIterationTime() {
        NamedObj container = getContainer();
        // NOTE: the container may not be a composite actor.
        // For example, the container may be an entity as a library,
        // where the director is already at the top level.
        if (container instanceof CompositeActor) {
            Director executiveDirector = ((CompositeActor) container)
                    .getExecutiveDirector();
            if (executiveDirector != null) {
                return executiveDirector.getModelNextIterationTime();
            }
        }
        return _currentTime;
    }

    /** Get the start time of the model. This base class returns
     *  a Time object with 0.0 as the value of the start time.
     *  Subclasses need to override this method to get a different
     *  start time.
     *  For example, CT director and DE director use the value of
     *  the <i>startTime</i> parameter to specify the real start time.
     *  @return The start time of the model.
     *  @exception IllegalActionException If the specified start time
     *   is invalid.
     */
    public Time getModelStartTime() throws IllegalActionException {
        return new Time(this);
    }

    /** Get the stop time of the model. This base class returns
     *  a new Time object with Double.POSITIVE_INFINITY as the value of the
     *  stop time.
     *  Subclasses need to override this method to get a different
     *  stop time.
     *  For example, CT director and DE director use the value of
     *  the stopTime parameter to specify the real stop time.
     *  @return The stop time of the model.
     *  @exception IllegalActionException If the specified stop time
     *   is invalid.
     */
    public Time getModelStopTime() throws IllegalActionException {
        return new Time(this, Double.POSITIVE_INFINITY);
    }

    /** Return the current time object of the model being executed by this
     *  director.
     *  This time can be set with the setModelTime method. In this base
     *  class, time never increases, and there are no restrictions on valid
     *  times.
     *
     *  @return The current time.
     *  @see #setModelTime(Time)
     */
    public Time getModelTime() {
        return _currentTime;
    }

    /** Return the next time of interest in the model being executed by
     *  this director. This method is useful for domains that perform
     *  speculative execution (such as CT).  Such a domain in a hierarchical
     *  model (i.e. CT inside DE) uses this method to determine how far
     *  into the future to execute.
     *  <p>
     *  In this base class, we return the current time.
     *  Derived classes should override this method to provide an appropriate
     *  value, if possible.
     *  <p>
     *  Note that this method is not made abstract to facilitate the use
     *  of the test suite.
     *  @return The time of the next iteration.
     *  @deprecated As of Ptolemy II 4.1, replaced by
     *  {@link #getModelNextIterationTime}
     */
    public double getNextIterationTime() {
        return getModelNextIterationTime().getDoubleValue();
    }

    /** Get the start time of the model. This base class returns
     *  a Time object with 0.0 as the value of the start time.
     *  Subclasses need to override this method to get a different
     *  start time.
     *  For example, CT director and DE director use the value of
     *  the startTime parameter to specify the real start time.
     *  @return The start time of the model.
     *  @deprecated As of Ptolemy II 4.1, replaced by
     *  {@link #getModelStartTime}
     *  @exception IllegalActionException If the specified start time
     *   is invalid.
     */
    public double getStartTime() throws IllegalActionException {
        return 0.0;
    }

    /** Get the stop time of the model. This base class returns
     *  a new Time object with Double.MAX_VALUE as the value of the
     *  stop time.
     *  Subclasses need to override this method to get a different
     *  stop time.
     *  For example, CT director and DE director use the value of
     *  the stopTime parameter to specify the real stop time.
     *  @return The stop time of the model.
     *  @deprecated As of Ptolemy II 4.1, replaced by
     *  {@link #getModelStopTime}
     *  @exception IllegalActionException If the specified stop time
     *   is invalid.
     */
    public double getStopTime() throws IllegalActionException {
        return getModelStopTime().getDoubleValue();
    }

    /** Get the time resolution of the model. The time resolution is
     *  the value of the <i>timeResolution</i> parameter. This is the
     *  smallest time unit for the model.
     *  @return The time resolution of the model.
     */
    public final double getTimeResolution() {
        // This method is final for performance reason.
        return _timeResolution;
    }

    /** Return true if this director assumes and exports
     *  the strict actor semantics, as described in this paper:
     *  <p>
     *  A. Goderis, C. Brooks, I. Altintas, E. A. Lee, and C. Goble,
     *  "Heterogeneous Composition of Models of Computation,"
     *  EECS Department, University of California, Berkeley,
     *  Tech. Rep. UCB/EECS-2007-139, Nov. 2007.
     *  http://www.eecs.berkeley.edu/Pubs/TechRpts/2007/EECS-2007-139.html
     *  <p>
     *  In particular, a director that implements this interface guarantees
     *  that it will not invoke the postfire() method of an actor until all
     *  its inputs are known at the current tag.  Moreover, it it will only
     *  do so in its own postfire() method, and in its prefire() and fire()
     *  methods, it does not change its own state.  Thus, such a director
     *  can be used within a model of computation that has a fixed-point
     *  semantics, such as SRDirector and ContinuousDirector.
     *  This base class returns false.
     *  @return True if the director assumes and exports strict actor semantics.
     */
    public boolean implementsStrictActorSemantics() {
        return false;
    }

    /** Initialize the model controlled by this director.  Set the
     *  current time to the start time or the current time of the
     *  executive director, and then invoke the initialize() method
     *  of this director on each actor that is controlled by this director.
     *  If the container is not an instance of CompositeActor, do nothing.
     *
     *  This method should typically be invoked once per execution, after the
     *  preinitialization phase, but before any iteration.  It may be
     *  invoked in the middle of an execution, if reinitialization is
     *  desired.  Since type resolution has been completed and the
     *  current time is set, the initialize() method of a contained
     *  actor may produce output or schedule events.  If stop() is
     *  called during this methods execution, then stop initializing
     *  actors immediately.
     *
     *  This method is <i>not</i> synchronized on the workspace,
     *  so the caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize().");
        }

        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }

        _actorsFinishedExecution = new HashSet();

        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Nameable containersContainer = container.getContainer();

            // Initialize the current time.
            // NOTE: Some (weird) directors pretend they are not embedded even
            // if they are (e.g. in Ptides), so we call _isEmbedded() to give
            // the subclass the option of pretending it is not embedded.
            if (_isEmbedded()
                    && (containersContainer instanceof CompositeActor)) {
                // The container is an embedded model.
                Time currentTime = ((CompositeActor) containersContainer)
                        .getDirector().getModelTime();
                _currentTime = currentTime;
            } else {
                // The container is at the top level.
                // There is no reason to set the current time to 0.0.
                // Instead, it has to be set to start time of a model.
                _currentTime = getModelStartTime();
            }

            // Initialize the contained actors.
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            while (actors.hasNext() && !_stopRequested) {
                Actor actor = (Actor) actors.next();

                if (_debugging) {
                    _debug("Invoking initialize(): ", ((NamedObj) actor)
                            .getFullName());
                }

                initialize(actor);
            }
        }
    }

    /** Initialize the given actor.  This method is generally called
     *  by the initialize() method of the director, and by the manager
     *  whenever an actor is added to an executing model as a
     *  mutation.  This method will generally perform domain-specific
     *  initialization on the specified actor and call its
     *  initialize() method.  In this base class, only the actor's
     *  initialize() method of the actor is called and no
     *  domain-specific initialization is performed.  Typical actions
     *  a director might perform include starting threads to execute
     *  the actor or checking to see whether the actor can be managed
     *  by this director.  For example, a time-based domain (such as
     *  CT) might reject sequence based actors.
     *  @param actor The actor that is to be initialized.
     *  @exception IllegalActionException If the actor is not
     *  acceptable to the domain.  Not thrown in this base class.
     */
    public void initialize(Actor actor) throws IllegalActionException {
        if (_debugging) {
            _debug("Initializing actor: " + ((Nameable) actor).getFullName()
                    + ".");
        }

        actor.initialize();
    }

    /** Indicate that resolved types in the model may no longer be valid.
     *  This will force type resolution to be redone on the next iteration.
     *  This method simply defers to the manager, notifying it.  If there
     *  is no container, or the container is not an instance of
     *  CompositeActor, or if it has no manager, do nothing.
     */
    public void invalidateResolvedTypes() {
        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            Manager manager = ((CompositeActor) container).getManager();

            if (manager != null) {
                manager.invalidateResolvedTypes();
            }
        }
    }

    /** Indicate that a schedule for the model may no longer be valid, if
     *  there is a schedule.  This method should be called when topology
     *  changes are made, or for that matter when any change that may
     *  invalidate the schedule is made.  In this base class, the method
     *  does nothing. In derived classes, it will cause any static
     *  schedule information to be recalculated in the prefire method
     *  of the director.
     */
    public void invalidateSchedule() {
    }

    /** Return false. This director iterates actors in its fire()
     *  method, which includes an invocation of their postfire()
     *  methods, so its fire method changes the state of the model.
     *
     *  @return False.
     */
    public boolean isFireFunctional() {
        return false;
    }

    /** Return true. The transferInputs() method does not check whether
     *  the inputs are known before calling hasToken(), and consequently
     *  will throw an exception if inputs are not known. Thus, this
     *  director requires that inputs be known in order to be able to
     *  iterate.  Derived classes that can tolerate unknown inputs
     *  should override this method to return false.
     *
     *  @return True.
     *  @exception IllegalActionException Thrown by subclass.
     */
    public boolean isStrict() throws IllegalActionException {
        return true;
    }

    /** Return true if stop has been requested.
     *  @return True if stop() has been called.
     *  @see #stop()
     */
    public boolean isStopRequested() {
        return _stopRequested;
    }

    /** Invoke a specified number of iterations of this director. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED.  Also, if the stop() is
     *  called during this execution, then immediately stop iterating
     *  and return STOP_ITERATING.
     *  <p>
     *  This base class method actually invokes prefire(), fire(),
     *  and postfire(), as described above, but a derived class
     *  may override the method to execute more efficient code.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    public int iterate(int count) throws IllegalActionException {
        int n = 0;

        while ((n++ < count) && !_stopRequested) {
            if (prefire()) {
                fire();

                if (!postfire()) {
                    return Executable.STOP_ITERATING;
                }
            } else {
                return Executable.NOT_READY;
            }
        }

        if (_stopRequested) {
            return Executable.STOP_ITERATING;
        } else {
            return Executable.COMPLETED;
        }
    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    public Receiver newReceiver() {
        return new Mailbox();
    }

    /** Return true if the director wishes to be scheduled for another
     *  iteration.  This method is called by the container of
     *  this director to see whether the director wishes to execute anymore.
     *  It should <i>not</i>, in general, call postfire() on the contained
     *  actors.
     *  <p>
     *  In this base class, return the false if stop() has been called
     *  since preinitialize(), and true otherwise. Derived classes that
     *  override this method need to respect this semantics. The
     *  protected variable _stopRequested indicates whether stop()
     *  has been called.
     *
     *  @return True to continue execution, and false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Director: Called postfire().");
        }
        return !_stopRequested && !_finishRequested;
    }

    /** Return true if the director is ready to fire. This method is
     *  called by the container of this director to determine whether the
     *  director is ready to execute. It does <i>not</i>
     *  call prefire() on the contained actors.
     *  If this director is not at the top level of the hierarchy,
     *  and the current time of the enclosing model is greater than the
     *  current time of this director, then this base class updates
     *  current time to match that of the enclosing model.
     *  <p>
     *  In this base class, assume that the director is always ready to
     *  be fired, and so return true. Domain directors should probably
     *  override this method to provide domain-specific behavior.
     *  However, they should call super.prefire() if they
     *  wish to propagate time as done here.
     *
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Director: Called prefire().");
        }

        Nameable container = getContainer();

        if (container instanceof Actor) {
            Director executiveDirector = ((Actor) container)
                    .getExecutiveDirector();

            if (executiveDirector != null) {
                Time outTime = executiveDirector.getModelTime();

                if (_currentTime.compareTo(outTime) < 0) {
                    setModelTime(outTime);
                }
            }
        }

        return true;
    }

    /** Validate the attributes and then invoke the preinitialize()
     *  methods of all its deeply contained actors.
     *  This method is invoked once per execution, before any
     *  iteration, and before the initialize() method.
     *  Time is not set during this stage. So preinitialize() method
     *  of actors should not make use of time. They should wait
     *  until the initialize phase of the execution.
     *  <p>This method also resets the protected variable _stopRequested
     *  to false, so if a derived class overrides this method, then it
     *  should also do that.
     *  <p>This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (_debugging) {
            _debug(getFullName(), "Preinitializing ...");
        }

        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }

        // validate all settable attributes.
        Iterator<?> attributes = attributeList(Settable.class).iterator();
        while (attributes.hasNext()) {
            Settable attribute = (Settable) attributes.next();
            attribute.validate();
        }
        // preinitialize protected variables.
        _currentTime = getModelStartTime();
        _stopRequested = false;
        _finishRequested = false;
        // preinitialize all the contained actors.
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                if (_debugging) {
                    _debug("Invoking preinitialize(): ", ((NamedObj) actor)
                            .getFullName());
                }
                actor.preinitialize();
            }
        }
        _createReceivers(); // Undid this change temporarily since the move of createReceivers breaks HDF
        if (_debugging) {
            _debug(getFullName(), "Finished preinitialize().");
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#removePiggyback(Executable)
     */
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    /** Queue an initialization request with the manager.
     *  The specified actor will be initialized at an appropriate time,
     *  between iterations, by calling its preinitialize() and initialize()
     *  methods. This method is called by CompositeActor when an actor
     *  sets its container to that composite actor.  Typically, that
     *  will occur when a model is first constructed, and during the
     *  execute() method of a ChangeRequest.
     *  In this base class, the request is delegated
     *  to the manager. If there is no manager, or if the container
     *  is not an instance of CompositeActor, then do nothing.
     *  @param actor The actor to initialize.
     */
    public void requestInitialization(Actor actor) {
        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            Manager manager = ((CompositeActor) container).getManager();

            if (manager != null) {
                manager.requestInitialization(actor);
            }
        }
    }

    /** Specify the container.  If the specified container is an instance
     *  of CompositeActor, then this becomes the active director for
     *  that composite.  Otherwise, this is an attribute like any other within
     *  the container. If the container is not in the same
     *  workspace as this director, throw an exception.
     *  If this director is already an attribute of the container,
     *  then this has the effect only of making it the active director.
     *  If this director already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then remove it from its container.
     *  This director is not added to the workspace directory, so calling
     *  this method with a null argument could result in
     *  this director being garbage collected.
     *  <p>
     *  If this method results in removing this director from a container
     *  that is a CompositeActor, then this director ceases to be the active
     *  director for that CompositeActor.  Moreover, if the composite actor
     *  contains any other directors, then the most recently added of those
     *  directors becomes the active director.
     *  <p>
     *  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this director and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it.
     *  @exception NameDuplicationException If the name of this director
     *   collides with a name already in the container.  This will not
     *   be thrown if the container argument is an instance of
     *   CompositeActor.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            Nameable oldContainer = getContainer();

            if (oldContainer instanceof CompositeActor
                    && (oldContainer != container)) {
                // Need to remove this director as the active one of the
                // old container. Search for another director contained
                // by the composite.  If it contains more than one,
                // use the most recently added one.
                Director previous = null;
                CompositeActor castContainer = (CompositeActor) oldContainer;
                Iterator<?> directors = castContainer.attributeList(
                        Director.class).iterator();

                while (directors.hasNext()) {
                    Director altDirector = (Director) directors.next();

                    // Since we haven't yet removed this director, we have
                    // to be sure to not just set it to the active
                    // director again.
                    if (altDirector != this) {
                        previous = altDirector;
                    }
                }

                castContainer._setDirector(previous);
            }

            super.setContainer(container);

            if (container instanceof CompositeActor) {
                // Set cached value in composite actor.
                ((CompositeActor) container)._setDirector(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set a new value to the current time of the model, where
     *  the new time must be no earlier than the current time.
     *  Derived classes will likely override this method to ensure that
     *  the time is valid.
     *
     *  @exception IllegalActionException If the new time is less than
     *   the current time returned by getCurrentTime().
     *  @param newTime The new current simulation time.
     *  @deprecated As of Ptolemy 4.1, replaced by
     *  {@link #setModelTime}
     *  @see #getCurrentTime()
     */
    public void setCurrentTime(double newTime) throws IllegalActionException {
        setModelTime(new Time(this, newTime));
    }

    /** Set a new value to the current time of the model, where
     *  the new time must be no earlier than the current time.
     *  Derived classes will likely override this method to ensure that
     *  the time is valid.
     *
     *  @exception IllegalActionException If the new time is less than
     *   the current time returned by getCurrentTime().
     *  @param newTime The new current simulation time.
     *  @see #getModelTime()
     */
    public void setModelTime(Time newTime) throws IllegalActionException {
        int comparisonResult = _currentTime.compareTo(newTime);

        if (comparisonResult > 0) {
            throw new IllegalActionException(this, "Attempt to move current "
                    + "time backwards. (new time = " + newTime
                    + ") < (current time = " + getModelTime() + ")");
        } else if (comparisonResult < 0) {
            if (_debugging) {
                _debug("==== Set current time to: " + newTime);
            }

            _currentTime = newTime;
        } else {
            // the new time is equal to the current time, do nothing.
        }
    }

    /** Request that the director cease execution altogether.
     *  This causes a call to stop() on all actors contained by
     *  the container of this director, and sets a flag
     *  so that the next call to postfire() returns false.
     */
    public void stop() {
        // Set _stopRequested first before looping through actors below
        // so isStopRequested() more useful while we are still looping
        // below.  Kepler's EML2000DataSource needed this.
        _stopRequested = true;

        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                actor.stop();
            }
        }
    }

    /** Request that execution of the current iteration stop.
     *  In this base class, the request is simply passed on to all actors
     *  that are deeply contained by the container of this director.
     *  For most domains, an iteration is a finite computation, so nothing
     *  further needs to be done here.  However, for some process-oriented
     *  domains, the fire() method of the director is an unbounded computation.
     *  Those domains should override this method so that when it is called,
     *  it does whatever it needs to do to get the fire() method to return.
     *  Typically, it will set flags that will cause all executing threads
     *  to suspend.  These domains should suspend execution in such a way
     *  that if the fire() method is called again, execution will
     *  resume at the point where it was suspended.  However, they should
     *  not assume the fire() method will be called again.  It is possible
     *  that the wrapup() method will be called next.
     *  If the container is not an instance of CompositeActor, then this
     *  method does nothing.
     */
    public void stopFire() {
        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                actor.stopFire();
            }
        }
    }

    /** Return an array of suggested directors to be used with
     *  ModalModel. Each director is specified by its full class
     *  name.  The first director in the array will be the default
     *  director used by a modal model. This base class delegates
     *  to the executive director, if there is one, and otherwise
     *  returns an array with only one element,
     *  "ptolemy.domains.fsm.kernel.FSMDirector".
     *  @return An array of suggested directors to be used with ModalModel.
     */
    public String[] suggestedModalModelDirectors() {
        NamedObj container = getContainer();
        if (container instanceof Actor) {
            Director executiveDirector = ((Actor) container)
                    .getExecutiveDirector();
            if (executiveDirector != null) {
                return executiveDirector.suggestedModalModelDirectors();
            }
        }
        // Default is just one suggestion.
        String[] defaultSuggestions = { "ptolemy.domains.fsm.kernel.FSMDirector" };
        return defaultSuggestions;
    }

    /** Return a boolean to indicate whether a ModalModel under control
     *  of this director supports multirate firing. In this class, false
     *  is always returned. Subclasses may override this method to return true.
     *  @return False indicating a ModalModel under control of this director
     *  does not support multirate firing.
     */
    public boolean supportMultirateFiring() {
        return false;
    }

    /** Terminate any currently executing model with extreme prejudice.
     *  This method is not intended to be used as a normal route of
     *  stopping execution. To normally stop execution, call the finish()
     *  method instead. This method should be called only
     *  when execution fails to terminate by normal means due to certain
     *  kinds of programming errors (infinite loops, threading errors, etc.).
     *  There is no assurance that the topology will be in a consistent
     *  state after this method returns.  The
     *  topology should probably be recreated before attempting any
     *  further operations.
     *  <p>
     *  This base class recursively calls terminate() on all actors deeply
     *  contained by the container of this director. Derived classes should
     *  override this method to release all resources in use and kill
     *  any sub-threads.  Derived classes should not synchronize this
     *  method because it should execute as soon as possible.
     *  If the container is not an instance of CompositeActor, then
     *  this method does nothing.
     *  <p>
     */
    public void terminate() {
        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                actor.terminate();
            }
        }
    }

    /** Transfer data from an input port of the container to the ports
     *  it is connected to on the inside.  The implementation in this
     *  base class transfers at most one token.  Derived classes may override
     *  this method to transfer a domain-specific number of tokens.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        // Use a strategy pattern here so that the code that transfers
        // at most one token is available to any derived class.
        return _transferInputs(port);
    }

    /** Transfer data from an output port of the container to the
     *  ports it is connected to on the outside.  The implementation
     *  in this base class transfers at most
     *  one token, but derived classes may transfer more than one
     *  token.
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        // Use a strategy pattern here so that the code that transfers
        // at most one token is available to any derived class.
        return _transferOutputs(port);
    }

    /** Invoke the wrapup() method of all the actors contained in the
     *  director's container.   In this base class wrapup() is called on the
     *  associated actors in the order of their creation.  If the container
     *  is not an instance of CompositeActor, then this method does nothing.
     *  <p>
     *  This method should be invoked once per execution.  None of the other
     *  action methods should be invoked after it in the execution.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Director: Called wrapup().");
        }

        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }

        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                actor.wrapup();
            }
        }
    }
    


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     * @exception IllegalActionException
     */
    protected String _description(int detail, int indent, int bracket)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            String result;

            if ((bracket == 1) || (bracket == 2)) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }

            // FIXME: Add director-specific information here, like
            // what is the state of the director.
            // if ((detail & FIXME) != 0 ) {
            //  if (result.trim().length() > 0) {
            //      result += " ";
            //  }
            //  result += "FIXME {\n";
            //  result += _getIndentPrefix(indent) + "}";
            // }
            if (bracket == 2) {
                result += "}";
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Request a firing of the container of this director at the specified time
     *  and throw an exception if the executive director does not agree to
     *  do it at the requested time. If there is no executive director (this
     *  director is at the top level), then ignore the request.
     *  This is a convenience method provided because several directors need it.
     *  @param time The requested time.
     *  @return The time that the executive director indicates it will fire this
     *   director, or an instance of Time with value Double.NEGATIVE_INFINITY
     *   if there is no executive director.
     *  @exception IllegalActionException If the director does not
     *   agree to fire the actor at the specified time, or if there
     *   is no director.
     */
    protected Time _fireContainerAt(Time time) throws IllegalActionException {
        Actor container = (Actor) getContainer();
        if (container != null) {
            Director director = container.getExecutiveDirector();
            if (director != null) {
                if (_debugging) {
                    _debug("**** Requesting that enclosing director refire me at "
                            + time);
                }
                Time result = director.fireAt(container, time);
                if (!result.equals(time)) {
                    throw new IllegalActionException(this,
                            director.getName()
                                    + " is unable to fire "
                                    + container.getName()
                                    + " at the requested time: "
                                    + time
                                    + ". It responds it will fire it at: "
                                    + result);
                }
                return result;
            }
        }
        return new Time(this, Double.NEGATIVE_INFINITY);
    }

    /** Return true if this director is embedded inside an opaque composite
     *  actor contained by another composite actor.
     *  @return True if this directory is embedded inside an opaque composite
     *  actor contained by another composite actor.
     */
    protected boolean _isEmbedded() {
        return !_isTopLevel();
    }

    /** Return true if this is a top-level director.
     *  Parts of this method is read synchronized on the workspace.
     * @return True if this director is at the top-level.
     */
    protected boolean _isTopLevel() {
        NamedObj container = getContainer();

        // NOTE: the container may not be a composite actor.
        // For example, the container may be an entity as a library,
        // where the director is already at the top level.
        if (container instanceof CompositeActor) {
            if (((CompositeActor) container).getExecutiveDirector() == null) {
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    /** Transfer at most one data token from the given input port of
     *  the container to the ports it is connected to on the inside.
     *  This method delegates the operation to the IOPort, so that the
     *  subclass of IOPort, TypedIOPort, can override this method to
     *  perform run-time type conversion.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @see IOPort#transferInputs
     */
    protected boolean _transferInputs(IOPort port)
            throws IllegalActionException {
        if (_debugging) {
            _debug("Calling transferInputs on port: " + port.getFullName());
        }

        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque"
                            + "input port.");
        }

        boolean wasTransferred = false;

        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (i < port.getWidthInside()) {
                    if (port.hasToken(i)) {
                        Token t = port.get(i);

                        if (_debugging) {
                            _debug("Transferring input " + t + " from "
                                    + port.getName());
                        }

                        port.sendInside(i, t);
                        wasTransferred = true;
                    }
                } else {
                    // No inside connection to transfer tokens to.
                    // In this case, consume one input token if there is one.
                    if (_debugging) {
                        _debug(getName(), "Dropping single input from "
                                + port.getName());
                    }

                    if (port.hasToken(i)) {
                        port.get(i);
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }

        return wasTransferred;
    }

    /** Transfer at most one data token from the given output port of
     *  the container to the ports it is connected to on the outside.
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if the port has an inside token that was successfully
     *  transferred.  Otherwise return false (or throw an exception).
     *
     */
    protected boolean _transferOutputs(IOPort port)
            throws IllegalActionException {
        boolean result = false;
        if (_debugging) {
            _debug("Calling transferOutputs on port: " + port.getFullName());
        }

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque input port.");
        }

        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                if (port.hasTokenInside(i)) {
                    Token t = port.getInside(i);

                    if (_debugging) {
                        _debug(getName(), "transferring output " + t + " from "
                                + port.getName());
                    }

                    port.send(i, t);
                    result = true;
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** Set of actors that have returned false from  postfire(),
     *  indicating that they do not wish to be iterated again.
     */
    protected Set _actorsFinishedExecution;

    /** The current time of the model. */
    protected Time _currentTime;
    
    /** Indicator that finish() has been called. */
    protected boolean _finishRequested;

    /** List of objects whose (pre)initialize() and wrapup() methods
     *  should be slaved to these.
     */
    protected transient List<Initializable> _initializables;

    /** Indicator that a stop has been requested by a call to stop(). */
    protected boolean _stopRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Add an XML graphic as a hint to UIs for rendering the director.
    private void _addIcon() {
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-15\" " + "width=\"100\" height=\"30\" "
                + "style=\"fill:green\"/>\n" + "</svg>\n");
    }

    /** Create receivers for all contained actors.
     *  @exception IllegalActionException If any port of a contained
     *  actor throws it when its receivers are created.
     *  @see Actor#createReceivers
     */
    private void _createReceivers() throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            for (Object actor : ((CompositeActor) container).deepEntityList()) {
                ((Actor) actor).createReceivers();
            }
        }
    }

    // Initialize parameters.
    private void _initializeParameters() {
        // This must happen first before any time objects get created.
        try {
            timeResolution = new SharedParameter(this, "timeResolution",
                    Director.class, "1E-10");

            // Since by default directors have no time, this is
            // invisible.
            timeResolution.setVisibility(Settable.NONE);

            // Make sure getCurrentTime() never returns null.
            _currentTime = new Time(this, Double.NEGATIVE_INFINITY);
        } catch (Throwable throwable) {
            // This is the only place to create
            // the timeResolution parameter, no exception should ever
            // be thrown.

            // If we are rethrowing an exception, don't include it in
            // the message, include it as a parameter to the throw so
            // making that we don't have lots of duplicate info in
            // the error message.

            throw new InternalErrorException(this, throwable,
                    "Cannot set timeResolution parameter");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** Time resolution cache, with a reasonable default value. */
    private double _timeResolution = 1E-10;

   
}
