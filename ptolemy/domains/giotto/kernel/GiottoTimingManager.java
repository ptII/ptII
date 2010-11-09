/* An attribute that varies the WCET values of actors.
 * 
 @Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.domains.giotto.kernel;

import java.util.List;
import java.util.Random;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.Initializable;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.DecoratedAttributes;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;

///////////////////////////////////////////////////////////////////
//// GiottoTimingManager
/**
 * An attribute that varies the WCET values of actors and throws a model 
 * error if the sum of the execution times is larger than the sum of the
 * worst case execution times. 
 *  
 * <p> This attribute helps the user to manage and  mitigate
 * timing errors in a specification that uses timing but has no
 * mechanisms in its specification for timing error handling. In 
 * this context a timing error occurs if the actor takes more than
 * the specified WCET to execute.
 * 
 * <p> The presence of the timing manager indicates a desire to 
 * incorporate execution timing as well as error handling
 * into a Giotto specification.  
 * 
 * <p> This attribute is a decorator that adds a parameter to each actor at
 * the current level.  The parameter is named <i>WCET</i>, which is the Worst 
 * Case Execution Time (WCET) for the actor whose initial default value is 0.0.
 * This indicates instantaneous execution, however, for each actor this
 * parameter can also be modified by the user with information gained
 * from an external WCET analysis tool.
 *
 * <p> This class simulates timing overruns by varying the execution time
 * of each actor. It currently uses the java.util.Random pseudo random number
 * generator to generate a random execution time in the range [0,2*<i>WCET</i>] 
 * for each iteration of each actor. The random number generator can be seeded 
 * by the <i>seed</i> parameter. The default value of the seed
 * is 0L interpreted as not having a seed. The user also has the option to reset to 
 * the seed on each run by selecting the <i>resetOnEachRun</i> parameter. 
 * A future modification could include adding a parameter to have
 * the user specify what probability distribution they wish to use.
 * 
 * <p> This attribute checks for two types of errors. First, it does a static check of 
 * (a) the sum of the worst case execution times for the actors and compares 
 * (a) to the {@link ptolemy.domains.giotto.kernel.GiottoDirector#period}
 * of the Giotto Director. If (a) is larger than the director
 * period, then the attribute throws an exception notifying the user of the discrepancy
 * and does not execute the model. Second, during execution the attribute randomly varies 
 * the execution time of the actors. The attribute then compares (b) the sum of the
 * actors execution times to (a) the sum of the actors worst case execution 
 * times. If (a) > (b) then the attribute calls
 * {@link ptolemy.kernel.util.NamedObj#handleModelError(NamedObj, IllegalActionException)},
 *  which throws a model error .
 *  
 * <p> A model error is an exception that is passed up the containment
 * hierarchy rather than being immediately thrown. Any container
 * in the containment hierarchy may choose to handle the error.
 * By default, containers will pass and delegate the error to their
 * container, if they have one, and throw an exception if they
 * don't. But some containers might do more with the error.</p>
 *  
 * <p>The attribute can be instantiated by instantiating an attribute of type
 * {@link ptolemy.domains.giotto.kernel.GiottoTimingManager}.</p>
 *
 * @author Shanna-Shaye Forbes. Based on the MonitorReceiverContents.java
 * created by Edward A. Lee and RandomSource.java by Edward A. Lee, Steve Neuendorffer, Elaine Cheong
 * @version $Id$
 * @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (sssf)
 *  @Pt.AcceptedRating Red (sssf)
 */
public class GiottoTimingManager extends SingletonAttribute implements
        Decorator { //extends SingletonAttribute 

    /** Construct an instance with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GiottoTimingManager(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-60\" y=\"-10\" " + "width=\"180\" height=\"20\" "
                + "style=\"fill:#00FFFE\"/>\n" + "<text x=\"-55\" y=\"5\" "
                + "style=\"font-size:15; font-family:SansSerif; fill:blue\">\n"
                + "Timing Manager\n" + "</text>\n" + "</svg>\n");

        // Hide the name.
        SingletonParameter hideName = new SingletonParameter(this, "_hideName");
        hideName.setToken(BooleanToken.TRUE);
        hideName.setVisibility(Settable.EXPERT);

        seed = new SharedParameter(this, "seed", GiottoTimingManager.class,
                "0L");
        seed.setTypeEquals(BaseType.LONG);

        resetOnEachRun = new SharedParameter(this, "resetOnEachRun",
                GiottoTimingManager.class, "false");
        resetOnEachRun.setTypeEquals(BaseType.BOOLEAN);

        probabilityDistribution = new StringParameter(this,
                "probabilityDistribution");
        probabilityDistribution.setExpression("none");
        probabilityDistribution.addChoice("none");
        
        _overRunThusFar= 0.0;

    }

    ///////////////////////////////////////////////////////////////////
    ////                     public ports and parameters           ////

    /** If true, this parameter specifies that the random number
     *  generator should be reset on each run of the model (in
     *  the initialize() method). It is a boolean that defaults
     *  to false. This is a shared parameter, meaning that changing
     *  it somewhere in the model causes it to be changed everywhere
     *  in the model.
     */
    public SharedParameter resetOnEachRun;
    /** The seed that controls the random number generation.
     *  This is a shared parameter, meaning that all instances of
     *  RandomSource or derived classes in the same model share the
     *  same value.
     *  A seed of zero is interpreted to mean that no seed is specified,
     *  which means that each execution of the model could result in
     *  distinct data. For the value 0, the seed is set to
     *  System.currentTimeMillis() + hashCode(), which means that
     *  with extremely high probability, two distinct actors will have
     *  distinct seeds.  However, current time may not have enough
     *  resolution to ensure that two subsequent executions of the
     *  same model have distinct seeds. For a value other than zero,
     *  the seed is set to that value plus the hashCode() of the
     *  full name of the actor. This means that with high probability,
     *  two distinct actors will have distinct, but repeatable seeds.
     *
     *  This parameter contains a LongToken, initially with value 0.
     */
    public SharedParameter seed;
    /**
     *  The value that controls the type of the probability distribution
     *  used for random number generation.
     *  The probability distribution defaults to none, which is interpreted 
     *  as an equal distribution of numbers between 0 and 1.
     */
    public StringParameter probabilityDistribution;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>seed</i>
     *  then create the base random number generator.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == seed) {
            long seedValue = ((LongToken) (seed.getToken())).longValue();

            if (seedValue != _generatorSeed) {
                _needNewGenerator = true;
            }
        }
        if (attribute == probabilityDistribution) {

        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Specify the container. If the container is not the same as the
     *  previous container, then start monitoring the new container.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    public void setContainer(final NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        if (_debugging) {
            _debug("set container method called");
        }
        NamedObj previousContainer = getContainer();
        if (previousContainer == container) {
            return;
        }

        if ((previousContainer != null)
                && (previousContainer instanceof CompositeActor)) {
            // _piggybackContainer should be non-null, but we check anyway.
            if (_piggybackContainer != null) {
                _piggybackContainer.removePiggyback(_executable);
            }
            _executable = null;
            String name;
            try {
                workspace().getWriteAccess();
                List<Actor> entities = ((CompositeActor) previousContainer)
                        .deepEntityList();
                for (Actor entity : entities) {
                    List<Attribute> paramList = ((Entity) entity)
                            .attributeList();
                    for (Attribute param : paramList) {
                        name = param.getDisplayName();
                        if (name.equals("WCET") || name.equals("executionTime")) {
                            // param.setPersistent(false);
                        }
                    }
                }
            } catch (Exception ex) { // this should later be replaced with a more specific exception
                throw new InternalErrorException(ex);
            } finally {
                workspace().doneTemporaryWriting();
            }
            if (_debugging) {
                _debug("I should remove all the attributes that were added here.");
            }

        }
        super.setContainer(container);

        if (container != null && container instanceof CompositeActor) {
            if (_executable == null) {

                _executable = new Executable() {

                    public void initialize() throws IllegalActionException {
                        if (_random == null
                                || ((BooleanToken) resetOnEachRun.getToken())
                                        .booleanValue()) {
                            _createGenerator();
                        }
                        _needNew = true;

                    }

                    public boolean postfire() throws IllegalActionException {
                        //System.out.println("After firing physical time is now "+_myPhysicalTime +" and model time is now "+((CompositeActor)container).getDirector().getModelTime().getDoubleValue());
                        if (_debugging) {
                            _debug("I should now check to see if there are cumulative overruns");
                        }
                        _needNew = true;

                        // here check to see if there were cumulative timing overruns
                        // cumulative execution times
                        double actorExecutionTimes = 0;
                        // cumulative worst case execution times
                        double actorWorstCaseExecutionTimes = 0;

                        List<Actor> entities = ((CompositeActor) container)
                                .deepEntityList();
                        for (Actor actor : entities) {

                            Attribute executionTime = ((Entity) actor)
                                    .getAttribute("executionTime");
                            Attribute WCET = ((Entity) actor)
                                    .getAttribute("WCET");
                            try {
                                actorWorstCaseExecutionTimes += ((DoubleToken) ((Variable) WCET)
                                        .getToken()).doubleValue();

                                actorExecutionTimes += ((DoubleToken) ((Variable) executionTime)
                                        .getToken()).doubleValue();
                            } catch (IllegalActionException ex) {
                                ex.printStackTrace(); // replace later with more appropriate behavior
                            }

                        }
                        if (_debugging) {
                            _debug("execution times are: "
                                    + actorExecutionTimes
                                    + " actor wcet times are: "
                                    + actorWorstCaseExecutionTimes);
                        }
                        if (actorExecutionTimes > actorWorstCaseExecutionTimes) {
                            if (_debugging) {
                                _debug("There was a timing overrun");
                            }
                            System.out.println("There was a timing overrun");

                            handleModelError(
                                    container,
                                    new IllegalActionException(
                                            container,
                                            "total ExecutionTime  of ("
                                                    + actorExecutionTimes
                                                    + ") is larger than WCET ("
                                                    + actorWorstCaseExecutionTimes
                                                    + ")  for actor "
                                                    + container
                                                            .getDisplayName()));

                        }

                        ChangeRequest request = new ChangeRequest(this,
                                "SetVariable change request", true /*Although this not a structural change in my point of view
                                                                   , we however for some reason need to specify it is, otherwise the GUI won't update.*/
                        ) {
                            protected void _execute()
                                    throws IllegalActionException {
                            }
                        };
                        // To prevent prompting for saving the model, mark this
                        // change as non-persistent.
                        request.setPersistent(false);
                        requestChange(request);
                        if (_debugging) {
                            _debug("Finished checking for overruns");
                        }
                        return true;
                    }

                    public void wrapup() {
                        ChangeRequest request = new ChangeRequest(this,
                                "SetVariable change request", true) {
                            protected void _execute()
                                    throws IllegalActionException {
                            }
                        };
                        // To prevent prompting for saving the model, mark this
                        // change as non-persistent.
                        request.setPersistent(false);
                        requestChange(request);
                    }

                    // All other methods are empty.
                    public void fire() throws IllegalActionException {
                       
                        if (_debugging) {
                            _debug("Fire method called in the quantity manager");
                        }
                        if (_needNewGenerator) {
                            _createGenerator();
                        }

                        if (_needNew) {
                            _generateRandomNumber();
                            _needNew = false;
                        }

                        // assign an execution time to each actor
                        List<Actor> entities = ((CompositeActor) container)
                                .deepEntityList();
                        for (Actor actor : entities) {
                            
                            
                                _myPhysicalTime = actor.getDirector().getModelTime().getDoubleValue()+_overRunThusFar;
                            
                            if (_debugging) {
                                _debug("checking for actor named "
                                        + actor.getName());
                            }
                            double actorWCET;
                            Attribute executionTime = ((Entity) actor)
                                    .getAttribute("executionTime");

                            Attribute WCET = ((Entity) actor)
                                    .getAttribute("WCET");
                            actorWCET = ((DoubleToken) ((Variable) WCET)
                                    .getToken()).doubleValue();
                            double t = _random.nextDouble() * 2 * actorWCET; // I multiply by actorWCET in an attempt to scale
                            if(t>actorWCET){
                                _overRunThusFar += (t - actorWCET);
                           _myPhysicalTime+=t;//(_overRunThusFar;
                            System.out.println("the actor WCET estimate was "+actorWCET+" and the actual execution time was "+ t);
                            System.out.println("there was an error at model time "+(actor.getDirector().getModelTime().getDoubleValue()+actorWCET)+"physical time is actually "+_myPhysicalTime);
                           
                            }
                            Parameter dummyP = (Parameter) executionTime;
                            dummyP.setExpression(Double.toString(t));
                            
                        }

                        if (_debugging) {
                            _debug("At the end of the fire method in"
                                    + " the timing manager");
                        }
                    }

                    public boolean isFireFunctional() {
                        return true;
                    }

                    public boolean isStrict() {
                        return true;
                    }

                    public int iterate(int count) {
                        return Executable.COMPLETED;
                    }

                    public boolean prefire() throws IllegalActionException {

                        return true;
                    }

                    public void stop() {
                    }

                    public void stopFire() {
                    }

                    public void terminate() {
                    }

                    public void addInitializable(Initializable initializable) {
                    }

                    public void preinitialize() throws IllegalActionException {
                        double wcet = 0;
                        double _periodValue = 0;

                        wcet = _getDirectorWCET(container);
                        _periodValue = _getDirectorPeriod(container);

                        if (_debugging) {
                            _debug("the WCET time seen by the director is "
                                    + wcet + " and the period is "
                                    + _periodValue);
                        }
                        if (wcet > _periodValue) {

                            if (_debugging) {
                                _debug("throw an exception");
                            }
                            // this is the static check before execution
                            throw new IllegalActionException(
                                    container,
                                    "total WCET of ("
                                            + wcet
                                            + ") is larger than period ("
                                            + _periodValue
                                            + ") for actor "
                                            + ((CompositeActor) (getContainer()))
                                                    .getDisplayName());

                        } //end of if  
                        if (_debugging) {
                            _debug("at the end of preinitialize in the timing quantity manager.");
                        }
                    }

                    public void removeInitializable(Initializable initializable) {
                    }
                };
            }

            _piggybackContainer = (CompositeActor) container;
            _piggybackContainer.addPiggyback(_executable);
        }

    }

    /** Set the current type of the decorated attributes.
     *  The type information of the parameters are not saved in the
     *  model hand hence this has to be reset when reading the model
     *  again.
     *  @param decoratedAttributes The decorated attributes.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     */
    public void setTypesOfDecoratedVariables(
            DecoratedAttributes decoratedAttributes)
            throws IllegalActionException {
    }

    /** Remove the decorated attributes.
     *  @param target The decorated attribute to remove   
     */
    public void removeDecoratedAttributes(NamedObj target) {
        if (_debugging) {
            _debug("create decorated attributes to be called for Giotto quantityManager");
        }
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) target
                .getContainer()).deepEntityList()) {
            NamedObj temp = (NamedObj) actor;
            if (_debugging) {
                _debug("temp has name " + temp.getDisplayName());
            }

            List<Parameter> paramList = temp.attributeList();

            for (Parameter param : paramList) {
                if (param.getDisplayName().equals("WCET")) {
                    param.setPersistent(false);
                }
            }
        }
    }

    /** Return the decorated attributes for the target NamedObj.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj. 
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DecoratedAttributes createDecoratedAttributes(NamedObj target)
            throws IllegalActionException, NameDuplicationException {
        if (_debugging) {
            _debug("create decorated attributes to be called for Giotto quantityManager");
        }
        return new GiottoDecoratedAttributesImplementation2(target, this);
    }

    /** Handle a model error.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return True if the error has been handled, or false if the
     *   error is not handled.
     *  @exception IllegalActionException If the handler handles the
     *   error by throwing an exception.
     */
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {

        if (_debugging) {
            _debug("Handle Model Error Called for Timing Manager");
        }

        NamedObj parentContainer = getContainer().getContainer();

        if (parentContainer != null) {
            return parentContainer.handleModelError(context, exception);
        } else {
            throw new IllegalActionException(this,
                    "Unable to set error transition. This is the top most level ");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                   ////

    /** Create the random number generator using current parameter values.
     *  @exception IllegalActionException If thrown while reading the
     *  seed Token.
     */
    protected void _createGenerator() throws IllegalActionException {
        long seedValue = ((LongToken) (seed.getToken())).longValue();
        _generatorSeed = seedValue;

        if (seedValue == 0L) {
            seedValue = System.currentTimeMillis() + hashCode();
        } else {
            seedValue = seedValue + getFullName().hashCode();
        }
        _random = new Random(seedValue);
        _needNewGenerator = false;
        _needNew = true;
    }
    /**
     * Generate the next random number.
     * @exception IllegalActionException If a call to nextDouble() throws an 
     * IllegalActionException
     */
    protected void _generateRandomNumber() throws IllegalActionException {
        _current = _random.nextDouble();
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    /** The current value of the seed parameter. */
    protected long _generatorSeed = 0L;

    /** Indicator that a new generator is needed. */
    protected boolean _needNewGenerator = true;

    /** The Random object. */
    protected Random _random;

    /** Indicator that a new random number is needed. */
    protected boolean _needNew = false;

    /** The Current value of the randomly generated number.*/
    protected double _current = 0.0;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                  ////
    /** Determine the period of the director of the associated container.
     *  @param container The container.
     *  @exception IllegalActionException If the director or an actor seen 
     *  by the director does not contain an particular attribute
     */

    private double _getDirectorPeriod(NamedObj container)
            throws IllegalActionException {
        double thePeriod = 0.0;
        Director director = ((CompositeActor) container).getDirector();
        Parameter period = (Parameter) director.getAttribute("period");
        thePeriod = ((DoubleToken) period.getToken()).doubleValue();

        if (director.isEmbedded()) {
            int frequencyValue = 1;
            Director executiveDirector = ((CompositeActor) container)
                    .getExecutiveDirector();

            if (executiveDirector instanceof GiottoDirector) {
                double periodValue = ((GiottoDirector) executiveDirector)
                        .getPeriod();
                Attribute frequency = ((CompositeActor) container)
                        .getAttribute("frequency");
                if (frequency != null) {
                    if (frequency instanceof Parameter) {
                        Token result = ((Parameter) frequency).getToken();
                        if (result instanceof IntToken) {
                            frequencyValue = ((IntToken) result).intValue();
                        }
                    }
                }

                thePeriod = periodValue / frequencyValue;
            }
        }

        return thePeriod;
    }

    /**
     * Return the cumulative WCET of the actors seen by the director
     * The Worst-Case Execution Time (WCET) of an actor is the  estimated
     * maximum length of time the task could take to execute on a
     * particular platform
     * @return A double containing the WCET of the actors
     * @exception IllegalActionException If an actor does not contain a WCET 
     * parameter.
     */
    private double _getDirectorWCET(NamedObj container)
            throws IllegalActionException {

        // This stores the sum of the worst case execution times
        double actorWorstCaseExecutionTimes = 0;

        List<Actor> entities = ((CompositeActor) container).deepEntityList();
        for (Actor actor : entities) {
            Attribute WCET = ((Entity) actor).getAttribute("WCET");
            if (WCET != null) {
                actorWorstCaseExecutionTimes += ((DoubleToken) ((Variable) WCET)
                        .getToken()).doubleValue();
            }

        }

        return actorWorstCaseExecutionTimes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The executable that creates the monitor attributes in initialize(). */
    private Executable _executable;

    /** The last container on which we piggybacked. */
    private CompositeActor _piggybackContainer;
    
    private double _myPhysicalTime;
    private double _overRunThusFar;

}
