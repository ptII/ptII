/* A Scheduler for the Jogl domain

 Copyright (c) 2000-2006 The Regents of the University of California.
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
package ptolemy.domains.jogl.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// GraphicsScheduler

/**

 A scheduler that implements scheduling of the active parts of a GRO
 scene graph. The scene graph is assumed to be a directed acyclic graph.
 Scheduling is done by performing a reversed topological sort on
 all the actors.

 @see ptolemy.actor.sched.Scheduler

 @author Yasemin Demir
 @version $Id: GraphicsScheduler.java 55069 2009-07-16 21:00:33Z ydemir$
 @since Ptolemy II 1.0
 */
public class JoglScheduler extends Scheduler {
    /** Construct a scheduler with no container (director)
     *  in the default workspace, the name of the scheduler is
     *  "GraphicsScheduler".
     */
    public JoglScheduler() {
        this(null);
    }

    /** Construct a scheduler in the given workspace with the name
     *  "GraphicsScheduler". If the workspace argument is null, use the default
     *  workspace. The scheduler is added to the list of objects in the
     *  workspace. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public JoglScheduler(Workspace workspace) {
        super(workspace);

        try {
            setName(_STATIC_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(
                    "Internal error when setting name to a GROScheduler");
        }
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public JoglScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the scheduling sequence.  An exception will be thrown if the
     *  graph is not schedulable.  This occurs in the following circumstances:
     *  <ul>
     *  <li>The graph is not a connected graph.
     *  <li>The graph is not acyclic
     *  <li>Multiple output ports are connected to the same broadcast
     *  relation. (equivalent to a non-deterministic merge)
     *  </ul>
     *
     * @return A Schedule type of the deeply contained opaque entities
     *  in the firing order.
     * @exception NotSchedulableException If the CompositeActor is not
     *  schedulable.
     */
    protected Schedule _getSchedule() {
        // FIXME: should check whether graph is connected
        // FIXME: should check whether multiple output ports are
        //        connected to the same broadcast relation.
        // Clear the graph
        DirectedAcyclicGraph dag = new DirectedAcyclicGraph();

        JoglDirector director = (JoglDirector) getContainer();

        if (director == null) {
            return null;
        }

        // If there is no container, there are no actors
        CompositeActor container = (CompositeActor) (director.getContainer());

        if (container == null) {
            return null;
        }

        CompositeActor castContainer = container;

        // First, include all actors as nodes in the graph.
        // get all the contained actors.
        List entities = castContainer.deepEntityList();
        Iterator actors = entities.iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            dag.addNodeWeight(actor);
        }

        actors = castContainer.deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            // Find the successors of the actor
            Set successors = new HashSet();
            Iterator outports = actor.outputPortList().iterator();

            while (outports.hasNext()) {
                IOPort outPort = (IOPort) outports.next();
                Iterator sinkPorts = outPort.sinkPortList().iterator();

                while (sinkPorts.hasNext()) {
                    IOPort sinkPort = (IOPort) sinkPorts.next();

                    if (sinkPort.isOutput()) {
                        // Skip this port, since its part of the container
                        continue;
                    }

                    Actor sinkActor = (Actor) sinkPort.getContainer();
                    successors.add(sinkActor);
                }
            }

            // Add the edge in the DAG
            Iterator succeedingActors = successors.iterator();

            while (succeedingActors.hasNext()) {
                Actor connectedActor = (Actor) succeedingActors.next();
                dag.addEdge(connectedActor, actor);
            }
        }

        // NOTE: The following may be a very costly test, which is why
        // it is done at the end.  However, this means that we cannot
        // report an actor in the directed cycle.  Probably DirectedGraph
        // should be modified to enable such reporting.
        if (!dag.isAcyclic()) {
            Object[] cycleNodes = dag.cycleNodes();
            StringBuffer names = new StringBuffer();

            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) {
                        names.append(", ");
                    }

                    names.append(((Nameable) cycleNodes[i]).getFullName());
                }
            }

            throw new NotSchedulableException(this,
                    "GRO graph is not acyclic: " + names.toString());
        }

        if (dag.top() == null) {
            // FIXME: throw exception here
        }

        Schedule schedule = new Schedule();

        _constructDepthFirstSchedule(schedule, dag, dag.bottom());

        if (_debugging) {
            _debug("Schedule is:");
            _debug(schedule.toString());
        }

        setValid(true);
        return schedule;
    }

    /**
     * Construct a depth-first firing schedule recursively. It
     * first creates a new firing to add to the given schedule
     * for the given actor. Then, it make a recursive call on
     * each of the successors (sinks connected to the given
     * actor). The successors are specified in the given directed
     * acyclic graph constructed previously. If the given actor
     * is null, return and do nothing.
     * @param schedule The given schedule to add the firings to.
     * @param dag The given directed acyclic graph of the model.
     * @param actorToAdd The given actor to add a firing to the
     *  schedule.
     */
    private void _constructDepthFirstSchedule(Schedule schedule,
            DirectedAcyclicGraph dag, Object actorToAdd) {

        if (actorToAdd == null) {
            return;
        }

        Firing firing = new Firing();
        firing.setActor((Actor) actorToAdd);
        schedule.add(firing);

        for (Object node : dag.successors(dag.node(actorToAdd))) {
            _constructDepthFirstSchedule(schedule, dag,
                    ((Node) node).getWeight());
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The static name of the scheduler
    private static final String _STATIC_NAME = "GROScheduler";
}
