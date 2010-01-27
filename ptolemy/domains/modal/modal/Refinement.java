/* Refinement for modal models.

 Copyright (c) 1999-2009 The Regents of the University of California.
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
package ptolemy.domains.modal.modal;

import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.util.Time;
import ptolemy.domains.modal.kernel.ContainmentExtender;
import ptolemy.domains.modal.kernel.RefinementActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Suspendable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

// NOTE: This is a combination of ModalController and CTStepSizeControlActor,
// but because of the inheritance hierarchy, there appears to be no convenient
// way to share the code.
///////////////////////////////////////////////////////////////////
//// Refinement

/**
 This typed composite actor supports mirroring of its ports in its container
 (which is required to be a ModalModel), which in turn assures
 mirroring of ports in each of the refinements and the controller.
 Refinements fulfills the CTStepSizeControlActor interface so that
 it can be used to construct hybrid systems using the CT domain.
 Refinements also fulfills the CTEventGenerator interface so that
 they can report events generated inside.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class Refinement extends TypedCompositeActor implements RefinementActor, Suspendable {
    /** Construct a modal controller with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Refinement(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        setClassName("ptolemy.domains.modal.modal.Refinement");

        new ContainmentExtender(this, "_containmentExtender");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return the accumulated time that the actor has been suspended
     *  since the last call to initialize(), or null if it has never
     *  been suspended.
     *  @return The total time between calls to suspend and subsequent
     *   calls to resume, or null if the actor has not been suspended.
     */
    public Time accumulatedSuspendTime() {
        return _accumulatedSuspendTime;
    }
    
    /** Perform no action but throw an IllegalActionException because a
     *  refinement cannot be created in an arbitrary actor-oriented refinement.
     *
     *  @param state The state that will contain the new refinement.
     *  @param name The name of the composite entity that stores the refinement.
     *  @param template The template used to create the refinement, or null if
     *   template is not used.
     *  @param className The class name for the refinement, which is used when
     *   template is null.
     *  @param configuration The configuration that is used to open the
     *   refinement (as a look-inside action) after it is created, or null if it
     *   is not needed to open the refinement.
     *  @exception IllegalActionException If error occurs while creating the
     *   refinement.
     */
    public void addRefinement(State state, String name, Entity template,
            String className, Configuration configuration)
            throws IllegalActionException {
        throw new IllegalActionException(this, "Unable to create a "
                + "refinement within a CompositeActor.");
    }

    /** Get the state in any ModalController within this ModalModel that has
     *  this refinement as its refinement, if any. Return null if no such state
     *  is found.
     *
     *  @return The state with this refinement as its refinement, or null.
     *  @exception IllegalActionException If the specified refinement cannot be
     *   found in a state, or if a comma-separated list is malformed.
     */
    public State getRefinedState() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof ModalModel) {
            List<?> controllers = ((ModalModel) container)
                    .entityList(ModalController.class);
            for (Object controllerObject : controllers) {
                ModalController controller = (ModalController) controllerObject;
                List<?> states = controller.entityList(State.class);
                for (Object stateObject : states) {
                    State state = (State) stateObject;
                    TypedActor[] refinements = state.getRefinement();
                    if (refinements != null) {
                        for (TypedActor refinement : refinements) {
                            if (refinement == this) {
                                return state;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Initialize this actor.  If this actor is opaque, invoke the
     *  initialize() method of its local director. Set the accumulated
     *  suspend time to zero.
     *  @exception IllegalActionException If there is no director, or
     *  if the director's initialize() method throws it, or if the
     *  actor is not opaque.
     */
    public void initialize() throws IllegalActionException {
        _accumulatedSuspendTime = null;
        _lastSuspendTime = null;
        super.initialize();
    }
    
    /** Create a new port with the specified name in the container of
     *  this refinement, which in turn creates a port in this refinement
     *  all other refinements, and the controller.
     *  This method is write-synchronized on the workspace.
     *  @param name The name to assign to the newly created port.
     *  @return The new port.
     *  @exception NameDuplicationException If the entity already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            
            ModalModel container = (ModalModel) getContainer();

            if (_mirrorDisable || container == null) {
                // We are mirroring a change above in the hierarchy
                // (or there is no above in the hierarchy),
                // so we should not mirror this change upwards.
                RefinementPort port = new RefinementPort(this, name);

                // Create the appropriate links.
                if (container != null) {
                    String relationName = name + "Relation";
                    Relation relation = container.getRelation(relationName);

                    if (relation == null) {
                        relation = container.newRelation(relationName);

                        Port containerPort = container.getPort(name);
                        containerPort.link(relation);
                    }

                    port.link(relation);
                }

                return port;
            } else {
                // We originated the change or it originated from below,
                // so we delegate upwards. The container will set
                // our _mirrorDisable to true and call our newPort(),
                // resulting in the code above executing.
                container.newPort(name);
                return getPort(name);
            }
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "Refinement.newPort: Internal error: " + ex.getMessage());
        } finally {
            _mirrorDisable = false;
            _workspace.doneWriting();
        }
    }
    
    /** Resume the actor at the specified time. If the actor has not
     *  been suspended since the last call to initialize(), then this
     *  has no effect. If this actor has a director that implements
     *  Suspendable, then this method is also delegated to it.
     *  @param time The time at which the actor is resumed.
     *  @exception IllegalActionException  If the resume cannot be completed.
     */
    public void resume(Time time) throws IllegalActionException {
        if (_lastSuspendTime != null) {
            if (_accumulatedSuspendTime == null) {
                _accumulatedSuspendTime = time.subtract(_lastSuspendTime);
            } else {
                _accumulatedSuspendTime
                        = _accumulatedSuspendTime.add(time.subtract(_lastSuspendTime));                
            }
            _lastSuspendTime = null;
        }
        Director director = getDirector();
        if (director instanceof Suspendable) {
            ((Suspendable)director).resume(time);
        }
    }
    
    /** Suspend the actor at the specified time.
     *  If the actor is already suspended, then it remains suspended
     *  but the accumulated suspend time is incremented by the time
     *  since it was last suspended. If the actor contains a director
     *  that implements Suspendable, then its suspend() method is also
     *  called.
     *  @param time The time at which the actor is suspended.
     *  @exception IllegalActionException If the suspend cannot be completed.
     */
    public void suspend(Time time) throws IllegalActionException {
        resume(time);
        _lastSuspendTime = time;
        Director director = getDirector();
        if (director instanceof Suspendable) {
            ((Suspendable)director).suspend(time);
        }
    }

    /** Control whether adding a port should be mirrored in the modal
     *  model and the mode controller.
     *  This is added to allow control by the UI.
     *  @param disable 0 if mirroring should occur, -1
     *   if mirroring should not occur downwards in the hierarchy,
     *   1 if mirroring should not occur upwards in the hierarchy.
     */
    public void setMirrorDisable(int disable) {
        _mirrorDisable = disable != 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container
     *  is a ModalModel or null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedActor, or if the base class throws it.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof ModalModel) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "Refinement can only be contained by "
                            + "ModalModel objects.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    // These are protected to be accessible to ModalModel.

    /** Indicator that we are processing a newPort request. */
    protected boolean _mirrorDisable = false;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Accumulated time that this actor has been suspended since
     *  initialize().
     */
    private Time _accumulatedSuspendTime;

    /** The environment time when this refinement was last suspended
     *  (that is, the enclosing state was exited). This is null if
     *  the actor has not been suspended since initialize() or
     *  resume() has been called more recently than suspend().
     */
    private Time _lastSuspendTime;
}
