/* An IOPort for controllers and refinements in modal models.

 Copyright (c) 1998-2010 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.modal.modal;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TransitionRefinementPort

/**
 A port for transition refinements in modal models.  This port
 mirrors certain changes to it in the ports of the container of the container.
 That container in turn mirrors those changes in other refinements and/or
 controllers.
 <p>
 For output ports, this class creates a sibling "input" port. The sibling
 port is normally treated like any other port but mirrors changes via it's
 output sibling. This is so that TransitionRefinement instances can get the
 outputs from the State refinements without having the port be an input/output.
 This sibling port is labeled as "port_in" where port is the name of the
 corresponding output port.

 @see ModalPort
 @see TransitionRefinement
 @author David Hermann, Research In Motion Limited
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (liuxj)
 */
public class TransitionRefinementPort extends RefinementPort {
    /** Construct a port in the given workspace.
     *  @param workspace The workspace.
     */
    public TransitionRefinementPort(Workspace workspace) {
        super(workspace);
    }

    /** Construct a port with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the TypedActor interface, or an exception will be
     *  thrown.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public TransitionRefinementPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to avoid linking a sibling input port to
     *  the same relation as the sibling output port multiple times.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If the link crosses levels of
     *   the hierarchy, or the port has no container, or the relation
     *   is not an instance of IORelation.
     */
    public void link(Relation relation) throws IllegalActionException {
        if (isInput() && _hasSibling && isLinked(relation)) {
            return;
        }

        super.link(relation);
    }

    /** Move this object down by one in the list of attributes of
     *  its container. If this object is already last, do nothing.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveDown() throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || (getContainer() == null)) {
                result = super.moveDown();
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveDown();
                            success = true;
                        }
                    }
                }

                // The mirror port(s), if there are any,
                // will have called this method and achieved
                // the moveDown. But if there are no mirror
                // ports, we need to do it here.
                if (!success) {
                    result = super.moveDown();
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Move this object to the first position in the list
     *  of attributes of the container. If  this object is already first,
     *  do nothing. Increment the version of the workspace.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveToFirst() throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || (getContainer() == null)) {
                result = super.moveToFirst();
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveToFirst();
                            success = true;
                        }
                    }
                }

                // The mirror port(s), if there are any,
                // will have called this method and achieved
                // the moveToFirst. But if there are no mirror
                // ports, we need to do it here.
                if (!success) {
                    result = super.moveToFirst();
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Move this object to the specified position in the list
     *  of attributes of the container. If this object is already at the
     *  specified position, do nothing.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @param index The position to move this object to.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container or if the index is out of bounds.
     */
    public int moveToIndex(int index) throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || (getContainer() == null)) {
                result = super.moveToIndex(index);
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveToIndex(index);
                            success = true;
                        }
                    }
                }

                // The mirror port(s), if there are any,
                // will have called this method and achieved
                // the moveToIndex. But if there are no mirror
                // ports, we need to do it here.
                if (!success) {
                    result = super.moveToIndex(index);
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Move this object to the last position in the list
     *  of attributes of the container.  If this object is already last,
     *  do nothing. This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveToLast() throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || (getContainer() == null)) {
                result = super.moveToLast();
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveToLast();
                            success = true;
                        }
                    }
                }

                if (!success) {
                    result = super.moveToLast();
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Move this object up by one in the list of
     *  attributes of the container. If  this object is already first, do
     *  nothing.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveUp() throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            int result = -1;

            if (_mirrorDisable || (getContainer() == null)) {
                result = super.moveUp();
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).moveUp();
                            success = true;
                        }
                    }
                }

                // The mirror port(s), if there are any,
                // will have called this method and achieved
                // the moveUp. But if there are no mirror
                // ports, we need to do it here.
                if (!success) {
                    result = super.moveUp();
                }
            }

            return result;
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Override the base class so that if the port is being removed
     *  from the current container, then it is also removed from the
     *  controller and from each of the refinements.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement Actor, or has no name,
     *   or the port and container are not in the same workspace. Or
     *   it's not null
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    public void setContainer(Entity container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj oldContainer = getContainer();

        if (container == oldContainer) {
            // Nothing to do.
            return;
        }

        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || (getContainer() == null)) {
                // process request for the sibling
                if (_hasSibling && isOutput() && (getContainer() != null)) {
                    TransitionRefinement transContainer = (TransitionRefinement) oldContainer;
                    TransitionRefinementPort sibling = (TransitionRefinementPort) transContainer
                            .getPort(getName() + "_in");

                    sibling._mirrorDisable = true;
                    sibling.setContainer(container);
                    sibling._mirrorDisable = false;
                }

                // Have already called the super class.
                // This time, process the request.
                super.setContainer(container);
            } else {
                // if this is the input port of a pair of siblings,
                // then forward request to the output port of the pair
                _mirrorDisable = true;

                boolean success = false;
                String portName = getName();

                if (_hasSibling && isInput() && !isOutput()) {
                    // we are the input sibling, extract "real" port name
                    portName = getName().substring(0, getName().length() - 3);
                }

                if (oldContainer != null) {
                    Nameable modal = oldContainer.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(portName);

                        if (port != null) {
                            port.setContainer(null);
                            success = true;
                        }
                    }
                }

                if (!success) {
                    super.setContainer(container);
                }
            }
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** If the argument is true, make the port an input port.
     *  If the argument is false, make the port not an input port.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isInput True to make the port an input.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    public void setInput(boolean isInput) throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || (getContainer() == null)) {
                // Have already called the super class.
                // This time, process the request.
                super.setInput(isInput);
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).setInput(isInput);
                            success = true;
                        }
                    }
                }

                if (!success) {
                    super.setInput(isInput);
                }
            }
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** If the argument is true, make the port a multiport.
     *  If the argument is false, make the port not a multiport.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isMultiport True to make the port a multiport.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    public void setMultiport(boolean isMultiport) throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || (getContainer() == null)) {
                // Have already called the super class.
                // This time, process the request.
                // process request for the sibling
                if (_hasSibling && isOutput() && (getContainer() != null)) {
                    TransitionRefinement container = (TransitionRefinement) getContainer();
                    TransitionRefinementPort sibling = (TransitionRefinementPort) container
                            .getPort(getName() + "_in");

                    sibling._mirrorDisable = true;
                    sibling.setMultiport(isMultiport);
                    sibling._mirrorDisable = false;
                }

                super.setMultiport(isMultiport);
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).setMultiport(isMultiport);
                            success = true;
                        }
                    }
                }

                if (!success) {
                    super.setMultiport(isMultiport);
                }
            }
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** Set the name of the port, and mirror the change in all the
     *  mirror ports.
     *  This method is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @exception IllegalActionException If the name has a period.
     *  @exception NameDuplicationException If there is already a port
     *   with the same name in the container.
     */
    public void setName(String name) throws IllegalActionException,
            NameDuplicationException {
        boolean disableStatus = _mirrorDisable;

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || (getContainer() == null)) {
                //change sibling
                if (_hasSibling && isOutput() && (getContainer() != null)) {
                    TransitionRefinement container = (TransitionRefinement) getContainer();
                    TransitionRefinementPort sibling = (TransitionRefinementPort) container
                            .getPort(getName() + "_in");
                    sibling._mirrorDisable = true;
                    sibling.setName(name + "_in");
                    sibling._mirrorDisable = false;
                }

                // Have already called the super class.
                // This time, process the request.
                super.setName(name);
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port != null) {
                            port.setName(name);
                            success = true;
                        }
                    }
                }

                if (!success) {
                    super.setName(name);
                }
            }
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    /** If the argument is true, make the port an output port.
     *  If the argument is false, make the port not an output port.
     *  In addition, if the container is an instance of Refinement,
     *  and the argument is true, find the corresponding port of the
     *  controller and make it an input and not an output.  This makes
     *  it possible for the controller to see the outputs of the refinements.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isOutput True to make the port an output.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    public void setOutput(boolean isOutput) throws IllegalActionException {
        boolean disableStatus = _mirrorDisable;

        // check first that this isn't an input sibling port,
        // if it is then it *cannot* be set as an output too
        if (_hasSibling && isInput() && !isOutput()) {
            if (isOutput) {
                throw new InternalErrorException(
                        "TransitionRefinementPort.setOutput:"
                                + " cannot set input sibling port to be an output");
            } else {
                return;
            }
        }

        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || (getContainer() == null)) {
                // Have already called the super class.
                // This time, process the request.
                super.setOutput(isOutput);

                // now create a sibling if we
                // don't otherwise have one
                if (!_hasSibling && isOutput) {
                    try {
                        TransitionRefinement container = (TransitionRefinement) getContainer();
                        // Temporarily turn off mirroring while we create a port.
                        // Needed by $PTII/ptolemy/domains/modal/demo/ModalBSC/ModalBSC.xml
                        // and $PTII/ptolemy/domains/modal/demo/StateTracker/StateTracker.xml
                        TransitionRefinementPort sibling = null;
                        try {
                            container.setMirrorDisable(-1);
                            sibling = new TransitionRefinementPort(container,
                                    getName() + "_in");
                        } finally {
                            container.setMirrorDisable(0);
                        }

                        sibling._hasSibling = true;
                        sibling._mirrorDisable = true;

                        // set attributes of sibling
                        sibling.setInput(true);
                        sibling.setMultiport(isMultiport());

                        sibling._mirrorDisable = false;

                        // link the port relation should already exist
                        // from this port's creation in newPort()
                        String relationName = getName() + "Relation";
                        ModalModel model = (ModalModel) container
                                .getContainer();
                        Relation relation = model.getRelation(relationName);

                        if (relation != null) {
                            sibling.link(relation);
                        }

                        _hasSibling = true;
                    } catch (NameDuplicationException ex) {
                        throw new InternalErrorException(this, ex,
                                "TransitionRefinementPort.setOutput: Internal error.");
                    }
                }
            } else {
                _mirrorDisable = true;

                boolean success = false;
                Nameable container = getContainer();

                if (container != null) {
                    Nameable modal = container.getContainer();

                    if (modal instanceof ModalModel) {
                        Port port = ((ModalModel) modal).getPort(getName());

                        if (port instanceof IOPort) {
                            ((IOPort) port).setOutput(isOutput);
                            success = true;
                        }
                    }
                }

                if (!success) {
                    super.setOutput(isOutput);
                }
            }
        } finally {
            _mirrorDisable = disableStatus;
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Flag indicating if this port has a sibling.
     *  This flag should be set to true for all output ports and their
     *  associated input port siblings.
     */
    protected boolean _hasSibling = false;
}
