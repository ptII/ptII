/* Preemptive EDF Ptides director that allows users to define deadlines to govern preemptive behavior.

@Copyright (c) 2008-2009 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */

package ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.TimeDelay;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.de.lib.TimedDelay;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This director allows users to define deadlines themselves. Unlike 
 *  PtidesPreemptiveEDFDirector, which automatically calculates the deadline
 *  using model time delays, this director can work with arbitrary deadlines.
 *  The safe to process analysis in this director guarantees the correct
 *  DE semantics disregarding what deadlines are used.
 *  @author Jia Zou, Slobodan Matic
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 *
 */

public class PtidesPreemptiveUserEDFDirector extends
        PtidesPreemptiveEDFDirector {

    /** Construct a director with the specified container and name.
     *  @param container The container
     *  @param name The name
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public PtidesPreemptiveUserEDFDirector(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        calculateDeadlineFromModelDelay = new Parameter(this, "calculateDeadlineFromModelDelay");
        calculateDeadlineFromModelDelay.setExpression("false");
        calculateDeadlineFromModelDelay.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then user the super's method to calculate the deadline
     *  using model time delays.
     */
    public Parameter calculateDeadlineFromModelDelay;
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Calculates dependencies between each pair of ports in the composite
     *  actor governed by this director. These values are cached and later
     *  used in safe to process analysis.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _inputPairDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();
        _calculatePortDependencies();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Only calculate the deadline using model time delay if asked to.
     *  Calculates the deadline for each channel in each input port within the 
     *  composite actor governed by this Ptides director. Deadlines are calculated 
     *  with only model time delays, not worst-case-execution-times (WCET). 
     */
    protected void _calculateDeadline() throws IllegalActionException {
        BooleanToken token = (BooleanToken)calculateDeadlineFromModelDelay.getToken();
        if (token != null && token.booleanValue()) {
            super._calculateDeadline();
        }
    }
    
    /** Calculates the dependencies between each pair of input ports within the composite
     *  actor that is governed by this director.
     *  @throws IllegalActionException
     */
    protected void _calculatePortDependencies() throws IllegalActionException {
        // Initializes the input pairs. By default, each input port pair has oPlusIdentity
        // dependency. However, if two input ports reside in the same finite equivalence
        // class, they have oTimesIdentity dependency. Also, for each input port i, we need
        // to find input ports (j_1, j_2, ...) where each j_n needs to satisfy the following
        // relationship: there exists an output port o where i, o are finitely dependent,
        // AND, there exists another port j', where j' is in o's sinkPortList(), and either
        // j' == j_n, or j' resides in the same finite equivalence class as j_n.
        List<IOPort> startInputs = new ArrayList<IOPort>();
        for (Actor actor : ((List<Actor>)((TypedCompositeActor)getContainer()).deepEntityList())) {
            if (actor == getContainer()) {
                break;
            }
            for (IOPort firstInput : (List<IOPort>)actor.inputPortList()) {
                startInputs.add(firstInput);
                // FIXME: we assume events at the same finite equivalent classes should be processed
                // in timestamp order regardless of the timestamps of events at the outputs. Thus
                // we do not need to know the dependency between the equivalent input ports.
                for (IOPort secondInput : (PtidesBasicDirector._finiteEquivalentPorts(firstInput))) {
                    Map<IOPort, Dependency> portDependency = _inputPairDependencies.get(firstInput);
                    if (portDependency == null) {
                        portDependency = new HashMap<IOPort, Dependency>();
                    }
                    portDependency.put(secondInput, SuperdenseDependency.OTIMES_IDENTITY);
                    _inputPairDependencies.put(firstInput, portDependency);
                }
                for (IOPort output : PtidesBasicDirector._finiteDependentPorts(firstInput)) {
                    Dependency dependency = actor.getCausalityInterface().getDependency(firstInput, output);
                    for (IOPort secondInput : (List<IOPort>)output.sinkPortList()) {
                        if (secondInput.getContainer() != getContainer()) {
                            Map<IOPort, Dependency> portDependency = _inputPairDependencies.get(firstInput);
                            if (portDependency == null) {
                                portDependency = new HashMap<IOPort, Dependency>();
                            }
                            SuperdenseDependency prevDependency = (SuperdenseDependency)portDependency.get(secondInput);
                            if (prevDependency != null && prevDependency.compareTo(dependency) < 0) {
                                dependency = prevDependency;
                            }
                            portDependency.put(secondInput, dependency);
                            for (IOPort equivSecondInput : (PtidesBasicDirector._finiteEquivalentPorts(secondInput))) {
                                portDependency.put(equivSecondInput, dependency);
                            }
                            _inputPairDependencies.put(firstInput, portDependency);
                        }
                    }
                }
            }
        }
        
        // Given the initialized input pair dependencies, use Floyd-Warshall algorithm to calculate
        // the dependency between all pairs of input ports in the composite actor governed by this
        // director.
        IOPort[] allInputs= (IOPort[])(startInputs.toArray(new IOPort[startInputs.size()]));
        int length = allInputs.length; 
        for (int i = 0; i < length; i++) {
            IOPort middleInput = allInputs[i];
            for (int j = 0; j < length; j++) {
                IOPort startInput = allInputs[j];
                for (int k = 0; k < length; k++) {
                    IOPort endInput = allInputs[k];
                    Dependency middleEndDependency = null;
                    Dependency startMiddleDependency = null;
                    Dependency prevDependency = null;
                    Map<IOPort, Dependency> middlePortDependency = _inputPairDependencies.get(middleInput);
                    if (middlePortDependency != null) {
                        middleEndDependency = middlePortDependency.get(endInput);
                        if (middleEndDependency != null) {
                            Map<IOPort, Dependency> startPortDependency = _inputPairDependencies.get(startInput);
                            if (startPortDependency != null) {
                                startMiddleDependency = startPortDependency.get(middleInput);
                                if (startMiddleDependency != null) {
                                    prevDependency = startPortDependency.get(endInput);
                                    Dependency newDependency = startMiddleDependency.oTimes(middleEndDependency);
                                    if (prevDependency != null) {
                                        if (newDependency.compareTo(prevDependency) < 0) {
                                            startPortDependency.put(endInput, newDependency);
                                        }
                                    } else {
                                        startPortDependency.put(endInput, newDependency);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** This method first uses the super of _safeToProcess(). If it's true, that means
     *  there are no events from the outside of the platform that may result in this
     *  event unsafe. If false, then this method should return false.
     *  Then we use _inputPairDependencies as well as the event queue
     *  to check if there are any events in the same platform that may result in
     *  this event unsafe to process. If so, return false, else return true.
     *  @param event The event of interest.
     *  @throws IllegalActionException
     */
    protected boolean _safeToProcess(DEEvent event) throws IllegalActionException {
        boolean result = super._safeToProcess(event);
        if (result == false) {
            return false;
        } else {
            IOPort port = event.ioPort();
            if (port != null) {
                List<DEEvent> eventList = new ArrayList();
                for (DoubleTimedEvent timedEvent : _currentlyExecutingStack) {
                    eventList.addAll((List<DEEvent>)timedEvent.contents);
                }
                for (int eventIndex = 0; eventIndex < _eventQueue.size(); eventIndex++) {
                    DEEvent earlierEvent = ((DEListEventQueue)_eventQueue).get(eventIndex);
                    // Assuming all actors are causal, we only need to search until this event
                    // in the event queue. Even if there are other events of the same timestamp
                    // in the event queue, those of smaller depth are before this event, thus
                    // they must be earlier in the event queue, since the event queue is
                    // also sorted by depth.
                    if (event == earlierEvent) {
                        break;
                    }
                    eventList.add(earlierEvent);
                }
                for (DEEvent earlierEvent : eventList) {
                    IOPort earlierPort = earlierEvent.ioPort();
                    double delay = 0;
                    if (earlierPort == null) {
                        // pure event.
                        Actor actor = earlierEvent.actor();
                        // FIXME: HACK.
                        if (actor instanceof TimedDelay) {
                            TimedDelay timedDelay = (TimedDelay) actor;
                            delay = ((DoubleToken)(timedDelay.delay).getToken()).doubleValue();
                            earlierPort = ((TimedDelay) actor).input;
                        } else if (actor instanceof TimeDelay) {
                            TimeDelay timeDelay = (TimeDelay) actor;
                            delay = ((DoubleToken)(timeDelay.delay).getToken()).doubleValue();
                            earlierPort = timeDelay.input;
                        } else {
                            throw new IllegalActionException(actor, "The safe to process analysis " +
                                    "cannot work with actors that produce pure events " +
                            "and are not either TimedDelay or TimeDelay actors.");
                        }
                    }
                    Map<IOPort, Dependency> portDependency = _inputPairDependencies
                    .get(earlierPort);
                    if (portDependency != null) {
                        SuperdenseDependency dependency = (SuperdenseDependency)portDependency.get(port);
                        Time timeDifference = event.timeStamp().subtract(earlierEvent.timeStamp());
                        timeDifference = timeDifference.add(delay);
                        if (dependency != null) {
                            // if the difference in model time between these two events is smaller
                            // or equal to the dependency between the two residing ports, then the
                            // event is unsafe.
                            if (timeDifference.getDoubleValue() >= dependency.timeValue()) {
                                return false;
                            }
                        }
                    }
                    //                        throw new IllegalActionException(earlierPort, port, "The dependency " +
                    //                                "between these two ports are not calculated, safe to " +
                    //                                "process analysis cannot be performed.");
                }
            } else {
                return true;
            }
        }
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    /** Computed dependencies between each pair of input ports of actors in the
     *  composite actor that is governed by this director. */
    private Map<IOPort, Map<IOPort, Dependency>> _inputPairDependencies;


}
