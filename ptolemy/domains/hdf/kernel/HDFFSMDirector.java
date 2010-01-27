/* An HDFFSM director extends MultirateFSMDirector by restricting that state
 transitions could only occur on each global iteration.

 Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.hdf.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.MultirateFSMDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// HDFFSMDirector

/**
 This director extends MultirateFSMDirector by restricting that state
 transitions could only occur between global iterations.
 The choice and commitment of the transition is deferred by sending
 a change request to the manager.
 <p>
 An HDFFSMDirector is often used in heterochronous dataflow (HDF) models.
 The HDF model of computation is a generalization of synchronous dataflow
 (SDF). In SDF, the set of port rates of an actor (called the rate
 signatures) are constant. In HDF, however, rate signatures are allowed
 to change between iterations of the HDF schedule. The change of rate
 signatures can be modeled by state transitions of a modal model, in which
 each state refinement infers a set of rate signatures.
 <p>
 <b>References</b>
 <p>
 <OL>
 <LI>
 A. Girault, B. Lee, and E. A. Lee,
 ``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">
 Hierarchical Finite State Machines with Multiple Concurrency Models</A>,
 '' April 13, 1998.</LI>
 </ol>

 @author Ye Zhou. Contributor: Brian K. Vogel
 @version $Id$
 @since Ptolemy II 5.0
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 @see MultirateFSMDirector
 @see HDFDirector
 */
public class HDFFSMDirector extends MultirateFSMDirector {
    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public HDFFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the modal model.
     *  If the refinement of the current state of the mode controller
     *  is ready to fire, then fire the current refinement. Overrides
     *  the base class method by sending a request to choose a
     *  transition to the manager.
     *  @exception IllegalActionException If there is no controller,
     *   or if the current state has no or more than one refinement.
     */
    public void fire() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        FSMActor controller = getController();
        controller.setNewIteration(_sendRequest);
        _readInputs();

        State currentState = controller.currentState();

        Actor[] actors = currentState.getRefinement();

        // NOTE: Paranoid coding.
        if ((actors == null) || (actors.length != 1)) {
            throw new IllegalActionException(this,
                    "Current state is required to have exactly one refinement: "
                            + currentState.getName());
        }

        if (!_stopRequested) {
            if (actors[0].prefire()) {
                if (_debugging) {
                    _debug(getFullName(), " fire refinement",
                            ((ptolemy.kernel.util.NamedObj) actors[0])
                                    .getName());
                }

                actors[0].fire();
                _refinementPostfire = actors[0].postfire();
            }
        }

        _readOutputsFromRefinement();

        if (_sendRequest) {
            ChangeRequest request = new ChangeRequest(this,
                    "choose a transition") {
                protected void _execute() throws KernelException,
                        IllegalActionException {
                    FSMActor controller = getController();
                    State currentState = controller.currentState();
                    chooseNextNonTransientState(currentState);
                }
            };

            request.setPersistent(false);
            container.requestChange(request);
        }
    }

    /** Return the change context being made explicit.  This class
     *  overrides the implementation in the FSMDirector base class to
     *  report that modal models using HDFFSMDirector only make state
     *  transitions between toplevel iterations.
     */
    public Entity getContext() {
        try {
            _getEnclosingDomainActor();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }

        return (Entity) toplevel();
    }

    /** Initialize the modal model. Set the _sendRequest flag to be true
     *  to indicate the modal model can send a change request to the manager.
     *  Set the controller flag to indicate a new iteration begins.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        FSMActor controller = getController();
        _sendRequest = true;
        controller.setNewIteration(_sendRequest);
        super.initialize();
    }

    /** Request a change of state transition to the manager.
     *  @return True if the postfire of the current state refinement
     *   returns true.
     *  @exception IllegalActionException If a refinement throws it,
     *   if there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();

        if (_sendRequest) {
            _sendRequest = false;

            ChangeRequest request = new ChangeRequest(this, "make a transition") {
                protected void _execute() throws KernelException {
                    _sendRequest = true;

                    // The super.postfire() method is called here.
                    makeStateTransition();
                }
            };

            request.setPersistent(false);
            container.requestChange(request);
        }

        return _refinementPostfire && !_stopRequested && !_finishRequested;
    }

    /** Preinitialize the modal model. Set the _sendRequest flag to be true
     *  to indicate the modal model can send a change request to the manager.
     */
    public void preinitialize() throws IllegalActionException {
        _sendRequest = true;
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A flag indicating whether the FSM can send a change request.
     *  The controller in HDFFSMDirector can only send one request per
     *  global iteration.
     */
    private boolean _sendRequest;
}
