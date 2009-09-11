/* A scheduler for the sequence domain.

 Copyright (c) 1998-2005 The Regents of the University of California.
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

 FIXME: Caching should move into StaticSchedulingDirector.
 */
package ptolemy.domains.sequence.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.TestExceptionHandler;
import ptolemy.domains.sequence.lib.ControlActor;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Scheduler;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// SequenceScheduler

/**
The SequenceScheduler is responsible for creating and maintaining a
schedule for the sequence models of computation.
The scheduler accepts a list of elements from the director.

This scheduler assembles three data structures that are used by SequenceSchedule:
- A directed graph containing all actors scheduled by this scheduler
- A hash table mapping each sequenced actor to an ordered list of dependent upstream actors
- A control table, which identifies the sequenced actors that are dependent
on other actors (for example, an If: Then (dependent actors) and :Else (dependent actors)
-   
- Sequence numbers are reused within a composite actor.  So, 
- Things with a sequence number but not a process number should be
treated as upstream actors 
(note for SequenceDirector and ProcessDirector)
- When the boundary (port) of a class or module is reached, no further items
inside that module are executed

 The base class for schedulers. A scheduler schedules the execution
 order of the containees of a CompositeActor.  <p>

 A SequenceScheduler is contained by a SequencedModelDirector, and provides
 the schedule for it.  The director will use this schedule to govern
 the execution of a CompositeActor. <p>

 A schedule is represented by the Schedule class, and determines the
 order of the firing of the actors in a particular composite actor.  In
 this base class, the default schedule fires the deeply
 contained actors in the order of their construction.  A domain specific
 scheduler will override this to provide a different order. <p>

 The schedule, once constructed, is cached and reused as long as the
 schedule is still valid.  The validity of the schedule is set by the
 setValid() method.  If the current schedule is not valid, then the
 schedule will be recomputed the next time the getSchedule() method is
 called.  However, derived classes will usually override only the
 protected _getSchedule() method. <p>

 The scheduler does not perform any mutations, and it does not listen
 for changes in the model.  Directors that use this scheduler should
 normally invalidate the schedule when mutations occur.

 @author Elizabeth Latronico (Bosch), Thomas Mandl (Bosch), Edward A. Lee 
 @version $Id$
 @since Ptolemy II 8.2
 @Pt.ProposedRating Red (beth)
 @Pt.AcceptedRating Red (beth)
 @see ptolemy.actor.sched.Schedule
 */
public class SequenceScheduler extends Scheduler {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public SequenceScheduler() {
        super();

        try {
            setName(_DEFAULT_SCHEDULER_NAME);
        } catch (KernelException ex) {
            // Should not be thrown.
            throw new InternalErrorException(this, ex, null);
        }
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking.
     */
    public SequenceScheduler(Workspace workspace) {
        super(workspace);

        try {
            setName(_DEFAULT_SCHEDULER_NAME);
        } catch (KernelException ex) {
            // Should not be thrown.
            throw new InternalErrorException(this, ex, null);
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
    public SequenceScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the scheduler into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new scheduler with no container, and no valid schedule.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Scheduler.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SequenceScheduler newObject = (SequenceScheduler) super.clone(workspace);
        // Invalidate the schedule, so that a new schedule must be calculated
        newObject.setValid(false);
        newObject._cachedGetSchedule = null;
        
        // Set other data members that refer to original model to null
        // The data structures hold references to objects from the original model, 
        // so they are not relevant for the clone
        // The new cloned director will need to call getSchedule()
        newObject._actorGraph = null;
        newObject._actorGraphNodeList = null;
        newObject._controlTable = null;
        newObject._dependentList = null;
        newObject._independentList = null;
        newObject._sequencedActorNodesToSubgraph = null;
  
        return newObject;
    }

    /**  Have to override this function in order to return a SequenceSchedule
     *   All additional code copied from Scheduler.java
     *   Have to also introduce own local variable, _cachedGetSchedule,
     *   to be of type SequenceSchedule 
     * 
     * The Sequence Scheduler takes one list as input
     *  - A list of independent sequenced actor attributes
     *    
     *    The sequence scheduler determines an additional list:
     *  - A list of sequenced actors that are dependent on control actors,
     *    or other actors
     *     
     *   It is possible to get the actor name from a sequence attribute by
     *   calling attributename.getContainer()
     * 
     *  Return the scheduling sequence as an instance of the Schedule class.
     *  For efficiency, this method returns a cached version of the
     *  schedule, if it is valid.  Otherwise, it calls the protected
     *  method _getSchedule() to update the schedule.  Derived classes
     *  normally override the protected method, not this one.
     *  The validity of the current schedule is set by the setValid()
     *  method.  This method is read-synchronized on the workspace.
     *  @param independentList List of independent and dependent
     *  sequence attributes (and, associated actors, found by calling
     *  getContainer() on a SequenceAttribute) There must be at least
     *  one sequence attribute in the _independentList.
     *  @return The Schedule returned by the _getSchedule() method.
     *  @exception IllegalActionException If the scheduler has no container
     *  (a director), or the director has no container (a CompositeActor),
     *  or the scheduling algorithm throws it.
     *  @exception NotSchedulableException If the _getSchedule() method
     *  throws it. Not thrown in this base class, but may be needed
     *  by the derived schedulers.
     */
    public SequenceSchedule getSchedule(List<SequenceAttribute> independentList) throws IllegalActionException,
            NotSchedulableException {
    	
    	// Check the list of sequence attributes
    	// There must be at least one actor with a sequence attribute in the model
        if (independentList == null || independentList.isEmpty()) {
            throw new IllegalActionException(this, "A model or composite actor with a SequencedModelDirector must have at least one actor, not dependent on other actors such as control actors, with a SequenceAttribute or ProcessAttribute.  No SequenceAttributes or ProcessAttributes were found.");
        }
        
        // Set class variables _independentList and _dependentList
        _independentList = independentList;
        _dependentList = new ArrayList<SequenceAttribute>();
    	
    	// From Scheduler.java
        try {
            workspace().getReadAccess();            
            
            // Changed this to SequencedModelDirector instead of StaticSchedulingDirector
            SequencedModelDirector director = (SequencedModelDirector) getContainer();

            if (director == null) {
                throw new IllegalActionException(this,
                        "SequenceScheduler has no director.");
            }

            CompositeActor compositeActor = (CompositeActor) (director
                    .getContainer());

            if (compositeActor == null) {
                throw new IllegalActionException(this,
                        "Director has no container.");
            }

            if (!isValid() || (_cachedGetSchedule == null)) {
                _cachedGetSchedule = _getSchedule();
            }

            return _cachedGetSchedule;
        } finally {
            workspace().doneReading();
        }
    
    }

    /** Returns a list of unreachable actors found in the most recent call to
     *  {@link #getSchedule()}. Actors are unreachable if they
     *  are not connected FIXME and have no sequence number?.
     *  , that are unreachable due to a lack of 
     *  connected actors.  These will be 'upstream' actors that are not upstream
     *  of any sequenced actor.  
     *  This is done by returning a list of all actors with a 'false' entry 
     *  in the hashtable _visitedNodes. 
     *  This method is meant to be called after processGraph() has been called for
     *  all schedules.  However, the SequenceScheduler itself does not know if there
     *  are any more schedules remaining.  So, the director can call this function,
     *  once all schedules have been handled.
     * @return (Possible empty) List of unreachable upstream actors
     */
    public List<Actor> unreachableActorList() {
        ArrayList<Actor> unreachables = new ArrayList<Actor>();
        
        // Get key set from hash table populated
        // in the most recent call to getSchedule().
        for (Node key : _visitedNodes.keySet()) {
            boolean visited = _visitedNodes.get(key);
            if (!visited) {
                unreachables.add((Actor) key.getWeight());
            }
        }
        return unreachables;
    }

    /** FIXME:  Keep this or change?
     *  Should the SequencedModelDirector be a sub-class of StaticSchedulingDirector?
     *  If it is, then can just use superclass function
     * 
     *  Specify the container.  If the specified container is an instance
     *  of Director, then this becomes the active scheduler for
     *  that director.  Otherwise, this is an attribute like any other within
     *  the container. If the container is not in the same
     *  workspace as this director, throw an exception.
     *  If this scheduler is already an attribute of the container,
     *  then this has the effect only of making it the active scheduler.
     *  If this scheduler already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then remove it from its container.
     *  This director is not added to the workspace directory, so calling
     *  this method with a null argument could result in
     *  this director being garbage collected.
     *  <p>
     *  If this method results in removing this director from a container
     *  that is a Director, then this scheduler ceases to be the active
     *  scheduler for that CompositeActor.  Moreover, if the director
     *  contains any other schedulers, then the most recently added of those
     *  schedulers becomes the active scheduler.
     *  <p>
     *  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this scheduler and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it.
     *  @exception NameDuplicationException If the name of this scheduler
     *   collides with a name already in the container.  This will not
     *   be thrown if the container argument is an instance of
     *   CompositeActor.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            Nameable oldContainer = getContainer();

            if (oldContainer instanceof Director && (oldContainer != container)) {
                // Need to remove this scheduler as the active one of the
                // old container. Search for another scheduler contained
                // by the composite.  If it contains more than one,
                // use the most recently added one.
            	// Beth - 10/29/08 - Changes this to a SequenceScheduler instead of just Scheduler
                SequenceScheduler previous = null;
                
                // Beth - changed to SequencedModelDirector
                //StaticSchedulingDirector castContainer = (StaticSchedulingDirector) oldContainer;
                
                SequencedModelDirector castContainer = (SequencedModelDirector) oldContainer;
                Iterator schedulers = castContainer.attributeList(
                        Scheduler.class).iterator();

                while (schedulers.hasNext()) {
                    SequenceScheduler altScheduler = (SequenceScheduler) schedulers.next();

                    // Since we haven't yet removed this director, we have
                    // to be sure to not just set it to the active
                    // director again.
                    if (altScheduler != this) {
                        previous = altScheduler;
                    }
                }
                castContainer._setScheduler(previous);
            }

            super.setContainer(container);

            // FIXME: What to do with this?
            
            if (container instanceof SequencedModelDirector) {
                // Set cached value in director
                ((SequencedModelDirector) container)._setScheduler(this);
            }
            
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The default name.
     */
    protected static final String _DEFAULT_SCHEDULER_NAME = "SequenceScheduler";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Beth - The SequenceScheduler assembles the structures that the
     *  SequenceSchedule will need to determine the next actor to fire.
     *  Note that, due to control branches, the exact firing order cannot
     *  be known before the control actors are actually fired.
     *  
     *  FIXME:  In the future, some of this code should be factored out to
     *  execute when a new Scheduler is created (for example, creating the
     *  actor graph).  The Process Director uses the same scheduler, but will 
     *  have multiple schedules.
     *  
     *  Reschedule the model.  In this base class, this method returns
     *  the actors contained by the CompositeActor in the order of
     *  their construction, i.e. the same order as returned by the
     *  CompositeActor.deepGetEntities() method.  Derived classes
     *  should override this method to provide a domain-specific
     *  scheduling algorithm.  This method is not intended to be
     *  called directly, but is called in turn by the getSchedule()
     *  method.  This method is not synchronized on the workspace, because
     *  the getSchedule() method is.
     *
     *  @return A Schedule of the deeply contained opaque entities
     *  in the firing order.
     *  @exception IllegalActionException If the scheduling algorithm
     *  throws it. Not thrown in this base class, but may be thrown
     *  by derived classes.
     *  @exception NotSchedulableException If the CompositeActor is not
     *  schedulable. Not thrown in this base class, but may be thrown
     *  by derived classes.
     *  @see ptolemy.kernel.CompositeEntity#deepEntityList()
     */
    
    protected SequenceSchedule _getSchedule() throws IllegalActionException,
            NotSchedulableException {
        
        // Sequenced actors should all share the same opaque container
        // Contained composite entities that are opaque may have a sequence attribute
        // If they do not, they are processed as upstream of the next opaque actor 
        // Contained composite entities that are transparent must not have a 
        // sequence number
        
        // Create a graph of all actors
        // so that we know which are connected to each other
        // This graph must use all opaque entities (i.e., those
        // returned by container.deepEntityList
    
    	// The director is the container of the SequenceScheduler
    	SequencedModelDirector director = (SequencedModelDirector) getContainer();
    	
        // Get the deepEntityList from the container of the director (the composite actor it's in)
        _createActorGraph(((CompositeEntity) director.getContainer()).deepEntityList());
        
        // Assemble control table
        // Hashtable <SequenceAttribute controlActor, Hashtable<String portName, ArrayList<SequenceAttribute> connectedActors>> 
        _controlTable = new Hashtable();
        
        // Create a new table to store the subgraphs
        // Hashtable <Actor, DirectedAcyclicGraph>
        _sequencedActorNodesToSubgraph = new Hashtable<Actor,DirectedAcyclicGraph>();
        
        Iterator seqAttribute = _independentList.iterator();
        
        // Beth 01/26/09
        // Added list for additional actors that have e.g. a process name of "None", so are not
        // included in the independent list, but that also need their upstream actors calculated
        // This happens for actors connected to control actors
        ArrayList moreActors = new ArrayList();
        
        while (seqAttribute.hasNext())
        {
            // Get sequence attribute
            SequenceAttribute seq = (SequenceAttribute) seqAttribute.next();
            
            // If it is a control actor, add information to the control table
            if (seq.getContainer() instanceof ControlActor)

            {
                // Get actor
                ControlActor act = (ControlActor) seq.getContainer();
                
                // Create a new hashtable to store the outgoing port names
                // and connected actors
                // Hashtable <String portName, ArrayList<SequenceAttribute> connected actors>
                Hashtable portToActors = new Hashtable();
                
                // Find the actors connected to its output ports
                // (if any actors and if any output ports)
                Iterator outPorts = act.outputPortList().iterator();
                
                while (outPorts.hasNext())
                {
                    // Create an arraylist for actors connected to this port
                    // This list may be empty
                    List connectedActorSeqNums = new ArrayList();
                    
                    // Get port and name
                    IOPort out = (IOPort) outPorts.next();
                    String outName = out.getName();
                    
                    // Get connected actors
                    // FIXME: Do I need the deep connected port list?
                    List connectedPorts = out.connectedPortList();
                    
                    if (connectedPorts != null)
                    {
                        Iterator conPorts = connectedPorts.iterator();
                        while (conPorts.hasNext())
                        {
                            // Get the sequence numbers for the actors
                            // corresponding to the connected actors
                            // Each connected actor must have a sequence number
                            IOPort con = (IOPort) conPorts.next();
                            
                            // Get the first sequence attribute (there should be only one)
                            // Make sure there is one; otherwise, throw an exception
                            if ( ((Entity) con.getContainer()).attributeList(SequenceAttribute.class).isEmpty())
                            {
                                throw new IllegalActionException(this, "Error:  downstream actor: " + ((Actor) con.getContainer()).getName() + " from control actor: " + act.getName() + " does not have a sequence or process attribute.");
                            }
                               
                            else
                            {
                            SequenceAttribute conAttribute = (SequenceAttribute) ((Entity) con.getContainer()).attributeList(SequenceAttribute.class).get(0); 
                            
                                // Otherwise, add this sequence attribute to the list
                                connectedActorSeqNums.add(conAttribute);
                                
                                // Beth added 01/26/09
                                // Check to make sure 
                                // For the process director, if the process name for this is "None",
                                // (which it should be), the actor will not be in the original list
                                // Add it so that its upstream actors will be calculated
                                if (!_independentList.contains(conAttribute) && !moreActors.contains(conAttribute))
                                {	
                                	moreActors.add(conAttribute);
                                }
                                
                                // Also, add these sequence attributes to the _dependentList
                                // so that they can later be removed from the _independentList
                                // after the control table has completely assembled
                                _dependentList.add(conAttribute);
                            }
                        }
                    }  // End list of actors connected to this port
                    
                    // If the connected actor list is not null and not empty, sort it
                    if (connectedActorSeqNums != null && !connectedActorSeqNums.isEmpty())
                    {
                        Collections.sort(connectedActorSeqNums); 
                        
                        // Check for duplicates in these lists
                        _identifyDuplicateSequences(connectedActorSeqNums);
                    }
                    
                    // Add the list to the hashtable of port to actors
                    portToActors.put(outName, connectedActorSeqNums);
                    
                }  // End while there are more output ports
                    
                // Add the port to actors hashtable to the control table
                // The key here is the sequence attribute for the control actor
                _controlTable.put(seq, portToActors);
                    
                } // End if control actor
            }  // End while more sequence attributes
        
        // Beth added 01/26/09 
        // Add everything in the moreActors list to the independentList
        // so that the subgraphs will be calculated
        _independentList.addAll(moreActors);
        
        // This function also creates a control graph now, starting
        // at the actor with the lowest sequence number in the current
        // director's container
        // Need to include the dependent actors here, because they might 
        // also have upstream actors
        _createSubGraphFiringScheduleList();
        
        // Now, remove all actors from the _independentList that have been
        // added to the _dependentList, so that the scheduler does not 
        // execute them on their own
        seqAttribute = _dependentList.iterator();
        
        while (seqAttribute.hasNext())
        {
            // Find entry in _independentList.  
            // It should exist if this is called by a SequenceDirector
            // However, if called by the ProcessDirector, it may not exist,
            // since actors with no process names are not included in the _independentList
            // from the ProcessDirector
            int removeMe = _independentList.indexOf(seqAttribute.next());
            
            if (removeMe != -1)
            {
                _independentList.remove(removeMe);
            }
            else
            {  
                System.out.println("SequenceScheduler can't find dependent sequence actor to remove it."); 
            }
        }
        
        // Check the remaining sequence actors in the _independentList for duplicates
        _identifyDuplicateSequences(_independentList);
        
        // Return a new schedule based on these data structures
        return new SequenceSchedule(_independentList, _controlTable, _sequencedActorNodesToSubgraph);
        
    }   // End function
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a graph of all the actors.
     *  The reachable nodes do not include the argument unless
     *  there is a loop from the specified node back to itself.
     *  
     *  This function also clears some of the data structures used
     *  that are re-used potentially over multiple processes
     *  (could be moved to a separate function)
     *  @param actorList  The list of actors.
     * 
     */
    private void _createActorGraph(List<Entity> actorList) {
    
        _actorGraph = new DirectedGraph();
        
        // Create also additional data structures
        // FIXME: Optimize in the future?
        _actorGraphNodeList = new Hashtable<Actor,Node>();
        _visitedNodes = new Hashtable<Node,Boolean>();
        
        // Create the edges.
        Iterator actors = actorList.iterator();
        while (actors.hasNext()) {
            Actor sa = (Actor) actors.next();
            if (!_actorGraph.containsNodeWeight(sa))
                _actorGraph.addNodeWeight(sa);
    
            // Add also an entry in the hashtable to easily look
            // up the node that goes with this actor
            // Get the node that was just added
            Iterator nodeIterator = _actorGraph.nodes(sa).iterator();
            if (nodeIterator.hasNext())
            {
                Node n = (Node) nodeIterator.next();
                _actorGraphNodeList.put(sa, n);
                // Also, mark this node as not yet visited
                // Don't add TestExceptionHandler actors
                // These are typically not connected in the model, 
                // but this isn't an error
                if (! (sa instanceof TestExceptionHandler))
                {
                    _visitedNodes.put(n, false);
                }
            }
            
            // Find all the predecessors of actor
            Iterator predecessors = _predecessorList(sa).iterator();
    
            while (predecessors.hasNext()) {
                Actor ps = (Actor) predecessors.next();
                if (!_actorGraph.containsNodeWeight(ps))
                {
                    _actorGraph.addNodeWeight(ps);
                }
                _actorGraph.addEdge(ps, sa);
            }
        }
    }

    /** Traverse the actor graph created from the model to check for cycles in the 
     *  subGraphs and populate subgraphList of creating firing schedule
     *  
     *  For the schedule, we only need to figure out the subgraphs for this particular
     *  set of sequenced nodes.  However, we need to consider all sequenced nodes in the
     *  model when deciding where to stop backtracking (done by checking if the actor
     *  has a SequenceAttribute (which will also match a ProcessAttribute).  
     *  
     * @exception IllegalActionException
     */
    private void _createSubGraphFiringScheduleList() throws IllegalActionException {

        // This should still be OK
        List<Object[]> subGraphList = new ArrayList<Object[]>();
        //FIXME:  Sort or change to e.g. hashtable for O(1) access
        // Could reuse hashtable for our 'visited' metric?
        // Could further improve by only storing e.g. name instead of whole actor
        // Also, only initialize this list in the constructor, so 
        // that with the process director, subsequent schedules will
        // add to it
        // But, what if we re-do one of the schedules and not the others?
        // How to remove nodes that are no longer there?
        // Make a 'clear' function to clear both the actor graph and this list?
        
        // Set this when the actorGraph is created 
        // (maybe in 'clear'?)
        //List<Node> actorGraphNodeList = new ArrayList<Node>(_actorGraph.nodes());
        
        
        List<Node> sequencedActorGraphNodes = new ArrayList<Node>(_independentList.size() + _dependentList.size());
        
        // Determine which nodes in the graph correspond to actors with a 
        // sequence number.  Assumes that the director has removed any 
        // invalid sequence attributes, or sequence attributes that don't
        // matter (for example an actor with a sequence attribute value zero)
        
        for ( SequenceAttribute sequenceAtt  : _independentList ) {
            Actor sequenceActorNode = (Actor)sequenceAtt.getContainer();
            
            // Find node in the hash table
            Node graphNode = _actorGraphNodeList.get(sequenceActorNode);
            
            if (graphNode != null)
            {
                sequencedActorGraphNodes.add(graphNode);
            }
        }
        
        // Process the list of sequence attributes to determine which actors
        // are associated with which upstream attributes
        // This list must contain ALL nodes with sequence numbers in the whole model
        // since the backtrack function checks to see if a node has a sequence number
        // so as not to include it in a list of upstream actors
        _processGraph(subGraphList, sequencedActorGraphNodes);
        
        // Return from this method
        // A SequenceSchedule will determine its own set of firings (since they are
        // dynamically determined)
    }
    
    /** Return the predecessors of the given actor in the same level of
     *  hierarchy. If the argument is null, return null. If the actor is
     *  a source, return an empty list.
     *  @param actor The given actor.
     *  @return The list of predecessors, unordered.
     */
    private List<Actor> _predecessorList(Actor actor) {
        if (actor == null) {
            return null;
        }
    
        LinkedList<Actor> predecessors = new LinkedList<Actor>();
        Iterator inPorts = (actor).inputPortList().iterator();
    
        while (inPorts.hasNext()) {
            IOPort port = (IOPort) inPorts.next();
            Iterator outPorts = port.deepConnectedOutPortList().iterator();
            
            while (outPorts.hasNext()) {
                IOPort outPort = (IOPort) outPorts.next();
                Actor pre = (Actor)outPort.getContainer();
                
                // NOTE: This could be done by using
                // NamedObj.depthInHierarchy() instead of comparing the
                // executive directors, but its tested this way, so we
                // leave it alone.
                if ((actor.getExecutiveDirector() == (pre).getExecutiveDirector())
                        && !predecessors.contains(pre)) {
                    predecessors.addLast(pre);
                }
            }
        }
    
        return predecessors;
    }

    /**
     * Process the actor graph to find out cycles and create a subGraphList of 
     * subGraphs containing SequencedActor with directed upstream actors. 
     * @param subGraphList
     * @param sequencedActorGraphNodes
     * @throws IllegalActionException
     */
    private void _processGraph(List<Object[]> subGraphList, List<Node> sequencedActorGraphNodes) throws IllegalActionException {

	 // From original SequenceDirector 

	 // Added: Create a hash table
	 //  of actors to directed graphs of upstream actors

        DirectedAcyclicGraph subGraph;

        //For each sequenced Actor in the graph
        //System.out.println("Number of sequenceActorGraphNodes: " + sequencedActorGraphNodes.size());
        for (Node seqActorNode : sequencedActorGraphNodes ) {

            //For each sequence actor,  remove outgoing edges
            // Beth - edges are no longer removed.  Note that 
            // _backwardReachableNodes does not process past sequenced actors
            // removeEdges(_actorGraph,seqActorNode);
            
            // Instead of removing the nodes, we mark them as 'visited' in the hash table
            // This lets the scheduler determine in a separate function whether or
            // not there are any unreachable nodes
            subGraph = (DirectedAcyclicGraph)_backwardReachableNodes(seqActorNode,sequencedActorGraphNodes);

            if (!subGraph.isAcyclic()) {// if cycles throw exception
                Object[] cycleNodes = subGraph.cycleNodes();
                LinkedList<Object> nodesAsList = new LinkedList<Object>();
                StringBuffer inCycle = new StringBuffer("Cycle includes: ");
                for (int i = 0; i < cycleNodes.length; i++) {
                    inCycle.append(cycleNodes[i]);
                    if (i < cycleNodes.length - 1) {
                        inCycle.append(", ");
                    }
                    nodesAsList.add(cycleNodes[i]);
                }
                throw new IllegalActionException("There is a cycle of the actors in the model. " + nodesAsList.toString());
            } else {// else if no cycles: subgraph => list

            	// Uncomment this section to print the subgraph
                
            	/*
                printSubGraph(subGraph);
                
                System.out.println("For actor: " + seqActorNode.getWeight().toString());
                System.out.println("Printing subGraph edges -- >");

                List<Edge> subGraphData = new ArrayList<Edge>(subGraph.edges());
                for (Edge edge : subGraphData){
                    System.out.println("SubGraph Edges: "+ edge );                    
                }
                System.out.println(" Printing subGraph Nodes -- >" );

                List<Node> subGraphNode = new ArrayList<Node>(subGraph.nodes());

                for (Node node : subGraphNode){
                    System.out.println("SubGraph Node: "+ node );                    
                }
                */
                
                
            }
            
            // If there is a subgraph, keep track of which sequenced actor this graph goes
            // with for our 
            // There should be at most one subgraph per sequenced actor
            // The subgraph will need to be sorted later
            // Table is <Actor, DirectedAcyclicGraph>
            if (subGraph != null)
            {
                _sequencedActorNodesToSubgraph.put((Actor) seqActorNode.getWeight(), subGraph);
                // Mark all nodes in the subgraph as visited.  
                // The subgraph should always include at least one node, 
                // the sequence node itself
                // Also, we do not remove the nodes anymore, but instead mark them as visited
                // OK to have upstream actors be upstream of multiple sequenced actors
                
                for (Node n: (Collection<Node>) subGraph.nodes())
                {
                    // This will overwrite the current value in the hashtable
                    _visitedNodes.put(n, true);
                }
            }
        }
    }
    
    /** Prints a list of unreachable actors, that are unreachable due to a lack of 
     *  connected actors.  These will be 'upstream' actors that are not upstream
     *  of any sequenced actor.  
     *  This is done by returning a list of all actors with a 'false' entry 
     *  in the hashtable _visitedNodes. 
     *  This method is meant to be called after processGraph() has been called for
     *  all schedules.  However, the SequenceScheduler itself does not know if there
     *  are any more schedules remaining.  So, the director can call this function,
     *  once all schedules have been handled.
     *
     * @return True if there is an unreachable actor
     */
    
    public boolean unreachableActorExists()
    {
        if (_visitedNodes.containsValue(false))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    /** Print the subGraph edges and nodes.
     * @param subGraph  The directed acyclic graph to print.
     */
    public void printSubGraph(DirectedAcyclicGraph subGraph) {
        // From original SequenceDirector - copied directly 
        if (_debugging) {
            _debug("Printing subGraph edges -- >");

            List<Edge> subGraphData = new ArrayList<Edge>(subGraph.edges());
            for (Edge edge : subGraphData){
                _debug("SubGraph Edges: "+ edge );                    
            }
            _debug(" Printing subGraph Nodes -- >" );

            List<Node> subGraphNode = new ArrayList<Node>(subGraph.nodes());

            for (Node node : subGraphNode){
                _debug("SubGraph Node: "+ node );                    
            }
        }        
    }

    /** From original SequenceDirector - copied directly 
     * Check if the actorNode is a Sequenced Actor node 
     * @param sequencedActorGraphNodes
     * @param node
     * @return true if node is a Sequenced Actor node
     * FIXME:  Replace this check with a check of the hashtable to improve performance
     */
    private boolean isSequencedGraphNode(List<Node> sequencedActorGraphNodes, Node node) {

        for ( Node graphNode  : sequencedActorGraphNodes ) {
            if ( graphNode.getWeight().equals(node.getWeight())){
                return true;
            }
        }
        return false;
    }
    
    /** From original SequenceDirector - copied directly
     * Remove all the out going directed edges of a Sequenced Actor node 
     * @param graph
     * @param node
     */
    private void removeEdges(DirectedGraph graph, Node node) {

        List edgeList = (List)graph.outputEdges(node); 
        for ( int i=0; i < graph.outputEdgeCount(node);i++){
            Edge edge = (Edge)edgeList.get(i);
            _actorGraph.removeEdge(edge);
        }
    }  

    /** From original SequenceDirector - copied directly
     *  Find all the nodes that can be reached backward from the
     *  specified node.
     *  The reachable nodes do not include the argument unless
     *  there is a loop from the specified node back to itself.
     * @param node
     * @param sequencedActorGraphNodes
     * @return subGraph of Sequenced Actor alongwith the upstream actor List directed to it. 
     */
    private DirectedAcyclicGraph _backwardReachableNodes(Node node, List<Node> sequencedActorGraphNodes) {

        DirectedAcyclicGraph subGraph =  new DirectedAcyclicGraph();
        // In Java the graph is altered by reference through methods
        _connectedSubGraph(node, subGraph,sequencedActorGraphNodes);
        return subGraph;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**  From original SequenceDirector - copied directly; slightly modified
     *  Given a node, get all the edges and nodes that are connected
     *  to it directly and/or indirectly. Add them in the given graph.
     *  Remove the nodes from the remaining nodes.
     * @param node
     * @param graph
     * @param sequencedActorGraphNodes
     */
    private void _connectedSubGraph(Node node, DirectedAcyclicGraph graph, List<Node> sequencedActorGraphNodes) {

        if (!graph.containsNode(node)) {
            graph.addNode(node);
        }

        // Handle source nodes.
        Collection inputEdgeCollection = _actorGraph.inputEdges(node);
        if ( inputEdgeCollection.size() > 0) {
            Iterator inputEdges = inputEdgeCollection.iterator();

            while (inputEdges.hasNext()) {
                Edge inputEdge = (Edge) inputEdges.next();

                if (!graph.containsEdge(inputEdge)) {
                    Node sourceNode = inputEdge.source();
                    
                    // Added to check actor for sequence attributes/process attributes
                    Actor a = (Actor) sourceNode.getWeight();
                    
                    // if new subgraph does not contain the node and it is not a sequenced Actor node
                    // if (!graph.containsNode(sourceNode) && !( isSequencedGraphNode(sequencedActorGraphNodes,sourceNode) )) {
                   
                    // Beth re-arranged 11/24/08
                    // If the connected node is not a sequenced actor
                    if (((Entity) a).attributeList(SequenceAttribute.class).isEmpty())
                    {
                    	// Check if the source node is already in the graph.  If not, add it, and 
                    	// process source node's connected nodes.
                    	if (!graph.containsNode(sourceNode)) {    
                        	graph.addNode(sourceNode);// then add node to new graph
                        	_connectedSubGraph(sourceNode, graph, sequencedActorGraphNodes);
                        	
                        	// Must add the edge after we add the node
                        	if (!graph.containsEdge(inputEdge) ) {

                            	graph.addEdge(sourceNode, node); // Else add nodes and edges to new graph

                            	if (_debugging) {
                                	_debug("Adding Edge node to SubGraph : sourceNode" + sourceNode.getWeight() + " sinkNode" + node.getWeight());
                            	}
                        	}
                    	}
                    	
                    	// Else, if the graph contains the node, but not the edge, add just the edge
                    	else
                    	{
                    		
                    		// Beth added this 11/25/08 as a bug fix 
                    		// New functional regression test case added under sequence director folder
                    		// If the subgraph already contains the source node, just add the edge
                    		// This would occur for a graph with edges:
                    		// A -> B; A -> C; B -> D; C -> D
                    		// so node A would be processed as a predecessor of either B or C
                    		// Otherwise, without this edge, it is possible that the topological 
                    		// sort will be incorrect, and that B or C would be fired before A
                    		if (!graph.containsEdge(inputEdge) ) {

                    			graph.addEdge(sourceNode, node); // Else add nodes and edges to new graph

                    			if (_debugging) {
                    				_debug("Adding Edge node to SubGraph : sourceNode" + sourceNode.getWeight() + " sinkNode" + node.getWeight());
                    			}
                    		}
                    	}

                    }
                }
            }
        } 
    }    

    /** From original SequenceDirector - copied directly
     *  Remove duplicate sequence numbers
     * @param sequenceList
     * @throws IllegalActionException
     */
    private void _identifyDuplicateSequences(List sequenceList) throws IllegalActionException {
        HashSet<Integer> found = new HashSet<Integer>();
        Iterator seqIterator = sequenceList.iterator();
        SequenceAttribute obj = null;
        while (seqIterator.hasNext()){
            obj = (SequenceAttribute)seqIterator.next();
            
            // Replaced with call to .getSequenceNumber() 
            // which should call the correct function from either 
            // SequenceAttribute or ProcessAttribute
            //Integer sequenceNumber = new Integer(obj.getExpression());
            
            Integer sequenceNumber = new Integer(obj.getSequenceNumber());
            if (!found.add(sequenceNumber)){
                throw new IllegalActionException(this,"Attempted to use duplicate sequence number : " + sequenceNumber + " in sequenceAttribute "+obj.getContainer());
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return the initialValueParameter name for each of the port .
     * @param port The port.
     * @param channel The channel of the port.
     * @return THe initialValueParameter name.
     */
    protected static String _getInitialValueParameterName(TypedIOPort port, int channel) {
        //From original SequenceDirector - copied directly.


        if (port.isMultiport()) {
            return port.getName() + "_" + channel + "_InitialValue";            
        } else {
            return port.getName() + "_InitialValue";
        }
    }

///////////////////////////////////////////////////////////////////
////package friendly variables                 ////

    /** Cache of the value of allowDisconnectedGraphs. */
    boolean _allowDisconnectedGraphs = false;

    // Use superclass here
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The flag that indicate whether the current schedule is valid.
    // Beth - this is in superclass - use accessor functions
    // private boolean _valid = false;

    // The cached schedule for getSchedule().
    // Need to have own here, instead of using superclass, because we
    // want to save a SequenceSchedule and not just a Schedule
    private SequenceSchedule _cachedGetSchedule = null;
       
    /** The DirectedGraph for the model. */
    public DirectedGraph _actorGraph;

    /** List of independent and dependent sequence attributes 
     * (and, associated actors, found by calling .getContainer 
     *  on a SequenceAttribute)  
     *  There must be at least one sequence attribute in the _independentList 
     *  The _dependentList may be empty */
    private List<SequenceAttribute> _independentList;
    private List<SequenceAttribute> _dependentList;
    
    /** A hash table mapping a sequence actor name to a hash table of 
     *  branches and a list of dependent actors (if any) for each branch
     *
     *  Each list in the hash table is then sorted by sequence number
     *  and checked for duplicates
     */
    private Hashtable<SequenceAttribute, Hashtable> _controlTable; 
    
    /** Hash table of actors to their subgraphs 
     *  Used in conjunction with the control graph for determining the
     *  schedule
     *  FIXME:  Check for null graphs?  There should not be any null graphs in the table.
     *  A graph should always include the sequenced actor itself.
     */
    private Hashtable<Actor,DirectedAcyclicGraph> _sequencedActorNodesToSubgraph;
    
    /** Hashtable of actors to graph nodes
     *  FIXME:  Is there a better way to do this?
     */
    Hashtable<Actor,Node> _actorGraphNodeList;
    
    /** Hashtable of graph nodes to a boolean value
     *  Keeps track of which nodes in the graph have been visited
     *  (in other words, can be scheduled somewhere)
     *  Anything that has not been visited is unreachable.
     *  Note:  TestExceptionHandler actors are not included in this hashtable
     *  That way, we can use the easy .contains(false) on the hash table 
     *  to see if an unreachable node exists, instead of having to check 
     *  each node and make sure it's not a TestExceptionHandler
     */
    Hashtable<Node,Boolean> _visitedNodes;

}
