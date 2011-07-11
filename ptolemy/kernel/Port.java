/* A Port is an aggregation of links to relations.

 Copyright (c) 1997-2011 The Regents of the University of California.
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
import ptolemy.kernel.util.CrossRefList;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Port

/**
 A Port is the interface of an Entity to any number of Relations.
 Normally, a Port is contained by an Entity, although a port
 may exist with no container.  The role of a port is to aggregate
 a set of links to relations.  Thus, for example, to represent
 a directed graph, entities can be created with two ports, one for
 incoming arcs and one for outgoing arcs.  More generally, the arcs
 to an entity may be divided into any number of subsets, with one port
 representing each subset.
 <p>

 A Port can link to any instance of Relation.  Derived classes may wish
 to constrain links to a subclass of Relation.  To do this, subclasses
 should override the protected method {@link #_checkLink(Relation)} to
 throw an exception if its argument is a relation that is not of the
 appropriate subclass.  Similarly, if a subclass wishes to constrain
 the containers of the port to be of a subclass of Entity, they should
 override the protected method {@link #_checkContainer(Entity)}.

 @author Mudit Goel, Edward A. Lee, Jie Liu
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 @see Entity
 @see Relation
 */
public class Port extends NamedObj {
    /** Construct a port in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public Port() {
        super();
        _elementName = "port";
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public Port(Workspace workspace) {
        super(workspace);
        _elementName = "port";
    }

    /** Construct a port with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This port will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the workspace directory,
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The parent entity.
     *  @param name The name of the Port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public Port(Entity container, String name) throws IllegalActionException,
            NameDuplicationException {
        super(container.workspace(), name);
        _elementName = "port";
        setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return A new Port.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Port newObject = (Port) super.clone(workspace);
        newObject._relationsList = new CrossRefList(newObject);
        newObject._insideLinks = new CrossRefList(newObject);
        newObject._container = null;
        return newObject;
    }

    /** List the connected ports.  Note that a port may be listed
     *  more than once if more than one connection to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of Port objects.
     */
    public List connectedPortList() {
        try {
            _workspace.getReadAccess();

            LinkedList result = new LinkedList();
            Iterator relations = linkedRelationList().iterator();

            while (relations.hasNext()) {
                Relation relation = (Relation) relations.next();

                // A null link (supported since indexed links) might
                // yield a null relation here. EAL 7/19/00.
                if (relation != null) {
                    result.addAll(relation.linkedPortList(this));
                }
            }

            return Collections.unmodifiableList(result);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the connected ports.  Note that a port may be listed
     *  more than once if more than one connection to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use connectedPortList() instead.
     *  @return An enumeration of Port objects.
     */
    public Enumeration connectedPorts() {
        return Collections.enumeration(connectedPortList());
    }

    /** Get the container entity.
     *  @return An instance of Entity.
     *  @see #setContainer(Entity)
     */
    public NamedObj getContainer() {
        return _container;
    }

    /** Insert a link to the specified relation at the specified index,
     *  and notify the container by calling its connectionsChanged() method.
     *  The relation is required to be at the same level of the hierarchy
     *  as the entity that contains this port, meaning that the container
     *  of the relation is the same as the container of the container of
     *  the port. That is, level-crossing links are not allowed.
     *  <p>
     *  The specified index can be any non-negative integer.
     *  Any links with indices larger than or equal to the one specified
     *  here will henceforth have indices that are larger by one.
     *  If the index is larger than the number of existing
     *  links (as returned by numLinks()), then empty links
     *  are inserted (these will be null elements in the list returned
     *  by linkedRelationsList() or in the enumeration returned by
     *  linkedRelations()). If the specified relation is null, then
     *  an empty link is inserted at the specified index.
     *  <p>
     *  Note that a port may be linked to the same relation more than
     *  once, in which case the link will be reported more than once
     *  by the linkedRelations() method.
     *  <p>
     *  In derived classes, the relation may be required to be an
     *  instance of a particular subclass of Relation (this is checked
     *  by the _checkLink() protected method).
     *  <p>
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param index The index at which to insert the link.
     *  @param relation The relation to link to this port.
     *  @exception IllegalActionException If the link would cross levels of
     *   the hierarchy, or the relation is incompatible,
     *   or the port has no container, or the port is not in the
     *   same workspace as the relation, or if the port is contained
     *   by a class definition.
     */
    public void insertLink(int index, Relation relation)
            throws IllegalActionException {
        if (_workspace != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }

        try {
            _workspace.getWriteAccess();
            _checkLink(relation);
            _relationsList.insertLink(index, relation._linkList);

            if (_container != null) {
                _container.connectionsChanged(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return true if the given relation or one in its relation
     *  group is linked to this port.
     *  @param r The relation.
     *  @return True if the given relation is linked to this port.
     *  @see #isLinked(Relation)
     */
    public boolean isGroupLinked(Relation r) {
        try {
            _workspace.getReadAccess();

            Iterator relations = r.relationGroupList().iterator();

            while (relations.hasNext()) {
                Relation groupRelation = (Relation) relations.next();

                if (_relationsList.isLinked(groupRelation)) {
                    return true;
                }
            }

            return false;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if the given relation is linked to this port.
     *  Note that this returns true only if the relation is directly
     *  linked to the port. There is no support here for relation groups.
     *  This method is read-synchronized on the workspace.
     *  @param r The relation.
     *  @return True if the given relation is linked to this port.
     *  @see #isGroupLinked(Relation)
     */
    public boolean isLinked(Relation r) {
        try {
            _workspace.getReadAccess();
            return _relationsList.isLinked(r);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Link this port with a relation, and notify the container by
     *  calling its connectionsChanged() method.  The relation is required
     *  to be at the same level of the hierarchy as the entity that contains
     *  this port, meaning that the container of the relation
     *  is the same as the container of the container of the port.
     *  That is, level-crossing links are not allowed.
     *  <p>
     *  If the argument is null, then create a null link. Note that a port
     *  may be linked to the same relation more than once, in which case
     *  the link will be reported more than once by the linkedRelations()
     *  method. In derived classes, the relation may be required to be an
     *  instance of a particular subclass of Relation (this is checked
     *  by the _checkLink() protected method).
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param relation The relation to link to this port.
     *  @exception IllegalActionException If the link would cross levels of
     *   the hierarchy, or the relation is incompatible,
     *   or the port has no container, or the port is not in the
     *   same workspace as the relation, or if the port is contained
     *   by a class definition.
     */
    public void link(Relation relation) throws IllegalActionException {
        if ((relation != null) && (_workspace != relation.workspace())) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }

        try {
            _workspace.getWriteAccess();

            if (relation != null) {
                _checkLink(relation);
                _relationsList.link(relation._linkList);
            } else {
                _relationsList.link(null);
            }

            if (_container != null) {
                _container.connectionsChanged(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** List the linked relations.  Note that a relation may appear
     *  more than once if more than one link to it has been established.
     *  Also, some entries in the list may be null, indicating a <b>null
     *  link</b>, where there is no linked relation. A null link causes
     *  a skip in the link indexes.
     *  This method is read-synchronized on the workspace.
     *  @return A list of Relation objects.
     */
    public List linkedRelationList() {
        try {
            _workspace.getReadAccess();

            // Unfortunately, CrossRefList returns an enumeration only.
            // Use it to construct a list.
            // NOTE: This list should be cached.
            LinkedList result = new LinkedList();
            Enumeration relations = _relationsList.getContainers();

            while (relations.hasMoreElements()) {
                result.add(relations.nextElement());
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the linked relations.  Note that a relation may appear
     *  more than once if more than one link to it has been established.
     *  Also, some entries in the enumeration may be null, indicating a
     *  <b>null link</b>, where there is no linked relation. A null link
     *  causes a skip in the link indexes.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of Relation objects.
     */
    public Enumeration linkedRelations() {
        // NOTE: There is no reason to deprecate this because it does not
        // depend on Doug Lea's collections, and it is more efficient than
        // the list version.
        try {
            _workspace.getReadAccess();
            return _relationsList.getContainers();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Move this object down by one in the list of ports of
     *  its container. If this object is already last, do nothing.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveDown() throws IllegalActionException {
        Entity container = (Entity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._portList.moveDown(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (Entity) derived.getContainer();
                container._portList.moveDown(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the first position in the list
     *  of ports of the container. If this object is already first,
     *  do nothing. Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveToFirst() throws IllegalActionException {
        Entity container = (Entity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._portList.moveToFirst(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (Entity) derived.getContainer();
                container._portList.moveToFirst(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the specified position in the list
     *  of ports of the container. If this object is already at the
     *  specified position, do nothing. Increment the version of the
     *  workspace.
     *  @param index The position to move this object to.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container or if the index is out of bounds.
     */
    public int moveToIndex(int index) throws IllegalActionException {
        Entity container = (Entity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._portList.moveToIndex(this, index);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (Entity) derived.getContainer();
                container._portList.moveToIndex(derived, index);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the last position in the list
     *  of ports of the container.  If this object is already last,
     *  do nothing. Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveToLast() throws IllegalActionException {
        Entity container = (Entity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._portList.moveToLast(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (Entity) derived.getContainer();
                container._portList.moveToLast(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object up by one in the list of
     *  ports of the container. If this object is already first, do
     *  nothing. Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveUp() throws IllegalActionException {
        Entity container = (Entity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._portList.moveUp(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (Entity) derived.getContainer();
                container._portList.moveUp(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return the number of links to relations.
     *  This method is read-synchronized on the workspace.
     *  @return The number of links, a non-negative integer.
     */
    public int numLinks() {
        try {
            _workspace.getReadAccess();
            return _relationsList.size();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Specify the container entity, adding the port to the list of ports
     *  in the container.  If the container already contains
     *  a port with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this port, throw an exception. If the port is
     *  a class element and the proposed container does not match
     *  the current container, then also throw an exception.
     *  If the port is already contained by the entity, do nothing.
     *  If the port already has a container, remove
     *  this port from its port list first.  Otherwise, remove it from
     *  the workspace directory, if it is present.
     *  If the argument is null, then
     *  unlink the port from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this port being garbage collected. This method validates all
     *  deeply contained instances of Settable, since they may no longer
     *  be valid in the new context.
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param entity The container.
     *  @exception IllegalActionException If this port is not of the
     *   expected class for the container, or it has no name,
     *   or the port and container are not in the same workspace, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it, or if this port is a class element and the argument
     *   does not match the current container.
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     *  @see #getContainer()
     *  @see #_checkContainer(Entity)
     */
    public void setContainer(Entity entity) throws IllegalActionException,
            NameDuplicationException {
        if ((entity != null) && (_workspace != entity.workspace())) {
            throw new IllegalActionException(this, entity,
                    "Cannot set container because workspaces are different.");
        }

        try {
            _workspace.getWriteAccess();
            _checkContainer(entity);

            Entity previousContainer = _container;

            if (previousContainer == entity) {
                return;
            }

            _container = entity;

            // Do this first, because it may throw an exception.
            if (entity != null) {
                try {
                    entity._addPort(this);
                } catch (IllegalActionException ex) {
                    _container = previousContainer;
                    throw ex;
                } catch (NameDuplicationException ex) {
                    _container = previousContainer;
                    throw ex;
                }

                if (previousContainer == null) {
                    _workspace.remove(this);
                }

                // We have successfully set a new container for this
                // object. Mark it modified to ensure MoML export.
                // FIXME: Inappropriate?
                // setOverrideDepth(0);
            }

            if (previousContainer != null) {
                previousContainer._removePort(this);
            }

            if (entity == null) {
                unlinkAll();
            } else {
                // Transfer any queued change requests to the
                // new container.  There could be queued change
                // requests if this component is deferring change
                // requests.
                if (_changeRequests != null) {
                    Iterator requests = _changeRequests.iterator();

                    while (requests.hasNext()) {
                        ChangeRequest request = (ChangeRequest) requests.next();
                        entity.requestChange(request);
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

    /** Set the name of the port. If there is already an port
     *  of the container entity with the same name, then throw an
     *  exception.
     *  @exception IllegalActionException If the name has a period.
     *  @exception NameDuplicationException If there is already a port
     *   with the same name in the container.
     */
    public void setName(String name) throws IllegalActionException,
            NameDuplicationException {
        if (name == null) {
            name = "";
        }

        Entity container = (Entity) getContainer();

        if ((container != null)) {
            Port another = container.getPort(name);

            if ((another != null) && (another != this)) {
                throw new NameDuplicationException(container,
                        "Name duplication: " + name);
            }
        }

        super.setName(name);
    }

    /** Unlink whatever relation is currently linked at the specified index
     *  number. If there is no such relation, do nothing.
     *  If a link is removed, then any links at higher index numbers
     *  will have their index numbers decremented by one.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param index The index number of the link to remove.
     */
    public void unlink(int index) {
        try {
            _workspace.getWriteAccess();
            _relationsList.unlink(index);

            if (_container != null) {
                _container.connectionsChanged(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink the specified Relation. If the Relation
     *  is not linked to this port, do nothing. If the relation is linked
     *  more than once, then unlink all occurrences.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param relation The relation to unlink.
     */
    public void unlink(Relation relation) {
        try {
            _workspace.getWriteAccess();
            _relationsList.unlink(relation);

            if (_container != null) {
                _container.connectionsChanged(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink all relations.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     */
    public void unlinkAll() {
        try {
            _workspace.getWriteAccess();
            _relationsList.unlinkAll();

            if (_container != null) {
                _container.connectionsChanged(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this port.  In this base class, this method returns immediately
     *  without doing anything.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.  Not thrown in this base class.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
    }

    /** Check that this port is compatible with the specified relation,
     *  that it has a container. If the argument is null, do nothing.
     *  If this port has no container, throw an exception.
     *  Derived classes may constrain the argument to be a subclass of
     *  Relation. Level-crossing links are allowed.
     *  This port and the relation are assumed to be in the same workspace,
     *  but this is not checked here.  The caller should check.
     *  This method is used in a "strategy pattern," where the link
     *  methods call it to check the validity of a link, and derived
     *  classes perform more elaborate checks.
     *  This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container,
     *   or if this port is not an acceptable port for the specified
     *   relation.
     */
    protected void _checkLink(Relation relation) throws IllegalActionException {
        if (relation != null) {
            if (_container == null) {
                throw new IllegalActionException(this, relation,
                        "Port must have a container to establish a link.");
            }

            // Throw an exception if this port is not of an acceptable
            // class for the relation.
            relation._checkPort(this);
        }
    }

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

            StringBuffer result = new StringBuffer();

            if ((bracket == 1) || (bracket == 2)) {
                result.append(super._description(detail, indent, 1));
            } else {
                result.append(super._description(detail, indent, 0));
            }

            if ((detail & LINKS) != 0) {
                if (result.toString().trim().length() > 0) {
                    result.append(" ");
                }

                // To avoid infinite loop, turn off the LINKS flag
                // when querying the Ports.
                detail &= ~LINKS;
                result.append("links {\n");

                Enumeration linkedRelations = linkedRelations();

                while (linkedRelations.hasMoreElements()) {
                    Relation relation = (Relation) linkedRelations
                            .nextElement();

                    if (relation != null) {
                        result.append(relation._description(detail, indent + 1,
                                2) + "\n");
                    } else {
                        // A null link (supported since indexed links) might
                        // yield a null relation here. EAL 7/19/00.
                        result.append(_getIndentPrefix(indent + 1) + "null\n");
                    }
                }

                result.append(_getIndentPrefix(indent) + "}");
            }

            if (bracket == 2) {
                result.append("}");
            }

            return result.toString();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get a port with the specified name in the specified container.
     *  The returned object is assured of being an
     *  instance of the same class as this object.
     *  @param relativeName The name relative to the container.
     *  @param container The container expected to contain the object, which
     *   must be an instance of Entity.
     *  @return An object of the same class as this object, or null if there
     *   is none.
     *  @exception IllegalActionException If the object exists
     *   and has the wrong class, or if the specified container is not
     *   an instance of CompositeEntity.
     */
    protected NamedObj _getContainedObject(NamedObj container,
            String relativeName) throws IllegalActionException {
        if (!(container instanceof Entity)) {
            throw new IllegalActionException(this, "Expected "
                    + container.getFullName()
                    + " to be an instance of ptolemy.kernel.Entity,"
                    + " but it is " + container.getClass().getName());
        }

        Port candidate = ((Entity) container).getPort(relativeName);

        if ((candidate != null) && !getClass().isInstance(candidate)) {
            throw new IllegalActionException(this, "Expected "
                    + candidate.getFullName() + " to be an instance of "
                    + getClass().getName() + ", but it is "
                    + candidate.getClass().getName());
        }

        return candidate;
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
            Port newObject = (Port) super._propagateExistence(container);
            // FindBugs warns that the cast of container is
            // unchecked.
            if (!(container instanceof Entity)) {
                throw new InternalErrorException(container
                        + " is not a Entity.");
            } else {
                newObject.setContainer((Entity) container);
            }
            return newObject;
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    // NOTE: This is defined here in the base class rather than in
    // ComponentPort even though it is not used until ComponentPort
    // so that derived classes can safely create links to ports in
    // the _addPort() method.  This is a bit of a kludge, but I see
    // no other way to make this possible. EAL

    /** The list of inside relations for this port. */
    protected CrossRefList _insideLinks = new CrossRefList(this);

    /** The list of relations for this port. */
    protected CrossRefList _relationsList = new CrossRefList(this);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The entity that contains this port. */
    private Entity _container;
}
