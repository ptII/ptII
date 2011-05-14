/* A relation supporting clustered graphs.

 Copyright (c) 1997-2010 The Regents of the University of California.
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
package ptolemy.kernel;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ComponentRelation

/**
 This class defines a relation supporting hierarchy (clustered graphs).
 Specifically, a method is added for defining a container and for
 performing deep traversals of
 a graph. Most importantly, however, instances of this class refuse to link
 to ports that are not instances of ComponentPort.  Thus, this class
 ensures that ComponentPort instances are only connected to other
 ComponentPort instances.
 <p>
 Derived classes may wish to further constrain linked ports to a subclass
 of ComponentPort, or to disallow links under other circumstances,
 for example if the relation cannot support any more links.
 Such derived classes should override the protected method _checkPort()
 to throw an exception.
 <p>
 To link a ComponentPort to a ComponentRelation, use the link() or
 liberalLink() method in the ComponentPort class.  To remove a link,
 use the unlink() method.
 <p>
 The container for instances of this class can only be instances of
 ComponentEntity.  Derived classes may wish to further constrain the
 container to subclasses of ComponentEntity.  To do this, they should
 override the protected _checkContainer() method.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (johnr)
 */
public class ComponentRelation extends Relation {
    /** Construct a relation in the default workspace with an empty string
     *  as its name. Add the relation to the directory of the workspace.
     */
    public ComponentRelation() {
        super();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the relation to the workspace directory.
     *
     *  @param workspace The workspace that will list the relation.
     */
    public ComponentRelation(Workspace workspace) {
        super(workspace);
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public ComponentRelation(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container.workspace(), name);
        setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new relation with no links and no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new ComponentRelation.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ComponentRelation newObject = (ComponentRelation) super
                .clone(workspace);
        newObject._container = null;
        return newObject;
    }

    /** Deeply list the ports linked to this relation. Look through
     *  all transparent ports and return only opaque ports.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of ComponentPorts.
     */
    public List deepLinkedPortList() {
        try {
            _workspace.getReadAccess();

            if (_deepLinkedPortsVersion == _workspace.getVersion()) {
                // Cache is valid.  Use it.
                return _deepLinkedPorts;
            }

            Iterator nearPorts = linkedPortList().iterator();
            _deepLinkedPorts = new LinkedList();

            while (nearPorts.hasNext()) {
                ComponentPort port = (ComponentPort) nearPorts.next();

                if (port._isInsideLinkable(this.getContainer())) {
                    // Port is above me in the hierarchy.
                    if (port.isOpaque()) {
                        // Port is opaque.  Append it to list.
                        _deepLinkedPorts.add(port);
                    } else {
                        // Port is transparent.  See through it.
                        _deepLinkedPorts.addAll(port.deepConnectedPortList());
                    }
                } else {
                    // Port below me in the hierarchy.
                    if (port.isOpaque()) {
                        _deepLinkedPorts.add(port);
                    } else {
                        _deepLinkedPorts.addAll(port.deepInsidePortList());
                    }
                }
            }

            _deepLinkedPortsVersion = _workspace.getVersion();
            return Collections.unmodifiableList(_deepLinkedPorts);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Deeply enumerate the ports linked to this relation. Look through
     *  all transparent ports and return only opaque ports.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of ComponentPorts.
     *  @deprecated Use deepLinkedPortList() instead.
     */
    public Enumeration deepLinkedPorts() {
        return Collections.enumeration(deepLinkedPortList());
    }

    /** Get the container entity.
     *  @return An instance of CompositeEntity.
     *  @see #setContainer(CompositeEntity)
     */
    public NamedObj getContainer() {
        return _container;
    }

    /** Move this object down by one in the list of relations of
     *  its container. If this object is already last, do nothing.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveDown() throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedRelations.moveDown(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedRelations.moveDown(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the first position in the list
     *  of relations of the container. If this object is already first,
     *  do nothing. Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveToFirst() throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedRelations.moveToFirst(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedRelations.moveToFirst(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the specified position in the list
     *  of relations of the container. If  this object is already at
     *  the specified position, do nothing. Increment the version of the
     *  workspace.
     *  @param index The position to move this object to.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container or if the index is out of bounds.
     */
    public int moveToIndex(int index) throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedRelations.moveToIndex(this, index);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedRelations.moveToIndex(derived, index);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the last position in the list
     *  of relations of the container.  If this object is already last,
     *  do nothing. Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveToLast() throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedRelations.moveToLast(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedRelations.moveToLast(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object up by one in the list of
     *  relations of the container. If this object is already first, do
     *  nothing. Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveUp() throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedRelations.moveUp(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedRelations.moveUp(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Specify the container entity, adding the relation to the list
     *  of relations in the container.  If the container already contains
     *  a relation with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this relation, throw an exception. If the relation is
     *  a class element and the proposed container does not match
     *  the current container, then also throw an exception.
     *  If the relation is already contained by the container, do nothing.
     *  If this relation already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the workspace directory, if it is present.
     *  If the argument is null, then unlink the ports from the relation and
     *  remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this relation being garbage collected.
     *  Derived classes may further constrain the class of the container
     *  to a subclass of CompositeEntity. This method validates all
     *  deeply contained instances of Settable, since they may no longer
     *  be valid in the new context. This method is write-synchronized
     *  on the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If this entity and the container
     *   are not in the same workspace, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contents list of the container.
     *  @see #getContainer()
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if ((container != null) && (_workspace != container.workspace())) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }

        try {
            _workspace.getWriteAccess();
            _checkContainer(container);

            CompositeEntity previousContainer = (CompositeEntity) getContainer();

            if (previousContainer == container) {
                return;
            }

            // Do this first, because it may throw an exception.
            if (container != null) {
                container._addRelation(this);

                if (previousContainer == null) {
                    _workspace.remove(this);
                }
            }

            _container = container;

            if (previousContainer != null) {
                previousContainer._removeRelation(this);
            }

            if (container == null) {
                unlinkAll();
            } else {
                // We have successfully set a new container for this
                // object. Mark it modified to ensure MoML export.
                // Transfer any queued change requests to the
                // new container.  There could be queued change
                // requests if this component is deferring change
                // requests.
                if (_changeRequests != null) {
                    Iterator requests = _changeRequests.iterator();

                    while (requests.hasNext()) {
                        ChangeRequest request = (ChangeRequest) requests.next();
                        container.requestChange(request);
                    }

                    _changeRequests = null;
                }
            }

            // Validate all deeply contained settables, since
            // they may no longer be valid in the new context.
            validateSettables();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set the name of the ComponentRelation. If there is already
     *  a ComponentRelation of the container with the same name, throw an
     *  exception.
     *  @exception IllegalActionException If the name has a period.
     *  @exception NameDuplicationException If there is already a relation
     *   with the same name in the container.
     */
    public void setName(String name) throws IllegalActionException,
            NameDuplicationException {
        if (name == null) {
            name = "";
        }

        CompositeEntity container = (CompositeEntity) getContainer();

        if ((container != null)) {
            ComponentRelation another = container.getRelation(name);

            if ((another != null) && (another != this)) {
                throw new NameDuplicationException(container,
                        "Name duplication: " + name);
            }
        }

        super.setName(name);
    }

    /** Override the base class to break inside links on ports as well
     *  as outside lists.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     */
    public void unlinkAll() {
        // NOTE: Do not just use _portList.unlinkAll() because then the
        // containers of the ports are not notified of the change.
        // Also, have to first copy the ports references, then remove
        // them, to avoid a corrupted enumeration exception.
        // Unlink the outside links of linked ports.
        super.unlinkAll();
        try {

            // Next, remove the links that are inside links of ports.
            _workspace.getWriteAccess();

            LinkedList ports = new LinkedList();
            Enumeration links = _linkList.getContainers();

            while (links.hasMoreElements()) {
                Object link = links.nextElement();

                if (link instanceof ComponentPort) {
                    ports.add(link);
                }
            }

            Iterator portsIterator = ports.iterator();

            while (portsIterator.hasNext()) {
                ((ComponentPort) (portsIterator.next())).unlinkInside(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this relation.  In this base class, this method returns immediately
     *  without doing anything.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.  Not thrown in this base class.
     */
    protected void _checkContainer(CompositeEntity container)
            throws IllegalActionException {
    }

    /** Throw an exception if the specified port cannot be linked to this
     *  relation (is not of class ComponentPort).
     *  @param port The port to link to.
     *  @exception IllegalActionException If the port is not a ComponentPort.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
        if (!(port instanceof ComponentPort)) {
            throw new IllegalActionException(this, port,
                    "ComponentRelation can only link to a ComponentPort.");
        }
    }

    /** Throw an exception if the specified relation is not an instance
     *  of ComponentRelation.
     *  @param relation The relation to link to.
     *  @param symmetric If true, the call _checkRelation on the specified
     *   relation with this as an argument.
     *  @exception IllegalActionException If this port has no container,
     *   or if this port is not an acceptable port for the specified
     *   relation.
     */
    protected void _checkRelation(Relation relation, boolean symmetric)
            throws IllegalActionException {
        if (!(relation instanceof ComponentRelation)) {
            throw new IllegalActionException(this, relation,
                    "ComponentRelation can only link to a ComponentRelation.");
        }

        super._checkRelation(relation, symmetric);
    }

    /** Propagate existence of this object to the
     *  specified object. This overrides the base class
     *  to set the container.
     *  @param container Object to contain the new object.
     *  @exception IllegalActionException If the object
     *   cannot be cloned.
     *  @return A new object of the same class and name
     *   as this one.
     */
    protected NamedObj _propagateExistence(NamedObj container)
            throws IllegalActionException {
        try {
            ComponentRelation newObject = (ComponentRelation) super
                    ._propagateExistence(container);
            // FindBugs warns that the cast of container is
            // unchecked.  
            if (!(container instanceof CompositeEntity)) {
                throw new InternalErrorException(container 
                        + " is not a CompositeEntity.");
            } else {
                newObject.setContainer((CompositeEntity) container);
            }
            return newObject;
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The entity that contains this entity. */
    private CompositeEntity _container;

    // A cache of the deeply linked ports, and the version used to
    // construct it.
    // 'transient' means that the variable will not be serialized.
    private transient List _deepLinkedPorts;

    private transient long _deepLinkedPortsVersion = -1;
}
