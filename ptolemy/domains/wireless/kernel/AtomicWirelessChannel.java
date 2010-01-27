/* The base class of communication channels in the wireless domain.

 Copyright (c) 2003-2009 The Regents of the University of California.
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
package ptolemy.domains.wireless.kernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.NameParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// AtomicWirelessChannel

/**

 The base class for communication channels in the wireless domain.

 <p>To use this class, place it in a wireless model that contains
 a wireless director and wireless actors (actors whose ports are
 instances of WirelessIOPort).  Then set the <i>outsideChannel</i>
 parameter of those ports to match the name of this channel.  The
 model can also itself contain ports that are instances of
 WirelessIOPort, in which case their <i>insideChannel</i> parameter
 should contain the name of this channel if they should use this
 channel.

 <p>
 In this base class, transmission on a channel reaches all ports
 at the same level of the hierarchy that are instances of
 WirelessIOPort and that specify that they use this channel. These
 ports include those contained by entities that have the container
 as this channel and that have their <i>outsideChannel</i>
 parameter set to the name of this channel.  They also include
 those ports whose containers are the same as the container of
 this channel and whose <i>insideChannel</i> parameter matches
 this channel name.
 <p>
 Derived classes will typically limit the range of the transmission,
 using for example location information from the ports. They
 may also introduce random losses or corruption of data.  To do this,
 derived classes can override the _isInRange() protected method,
 or the transmit() public method.

 <p>
 Other classes may register a property transformer that allows them
 to modify meta data prior to transmission of the data token on the channel.

 @author Xiaojun Liu, Edward A. Lee, Yang Zhao, Heather Taylor, Elaine Cheong
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class AtomicWirelessChannel extends TypedAtomicActor implements
        WirelessChannel, ValueListener {

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public AtomicWirelessChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        defaultProperties = new Parameter(this, "defaultProperties");

        this.name = new NameParameter(this, "name");

        // Force this to be a record type without specifying the fields.
        defaultProperties.setTypeAtMost(BaseType.RECORD);
        defaultProperties.setToken(RecordToken.EMPTY_RECORD);

        _channelPort = new ChannelPort(this, "_channelPort");

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-25,0 8,-8 2,2 25,0 -8,8 -2,-2 -25,0\" "
                + "style=\"fill:red\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////

    /** The default properties for transmission. In this base class,
     *  the type is constrained to be a record and the default value
     *  is set to an empty record.  Derived classes
     *  will define this to be a record.
     */
    public Parameter defaultProperties;

    /** When set to a non-empty value, this NameParameter will cause
     *  the name of this channel to be set to this value.
     */
    public NameParameter name;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a channel listener to listen for transmissions on this channel.
     *  A ChannelListener can read the transmission property and token that
     *  was transmitted on this channel.
     *
     *  If the channel listener has already been added, then no changes
     *  are made.
     *
     *  If multiple channel listeners are registered that can operate on a
     *  given transmission, then the order in which they are applied is arbitrary.
     *
     *  @param listener The channel listener to add.
     *  @see #removeChannelListener(ChannelListener)
     */
    public void addChannelListener(ChannelListener listener) {
        if (_channelListeners == null) {
            _channelListeners = new HashSet();
        }
        _channelListeners.add(listener);
    }

    /** Notify any channel listeners that have been added.
     *  @param properties The transmission properties.
     *  @param token The token to be processed.
     *  @param source The sending port.
     *  @param destination The receiving port.
     *  @see #addChannelListener(ChannelListener)
     */
    public void channelNotify(RecordToken properties, Token token,
            WirelessIOPort source, WirelessIOPort destination) {
        if (_channelListeners != null) {
            Iterator iterator = _channelListeners.iterator();

            while (iterator.hasNext()) {
                ChannelListener listener = (ChannelListener) iterator.next();
                listener.channelNotify(properties, token, source, destination);
            }
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AtomicWirelessChannel newObject = (AtomicWirelessChannel) super
                .clone(workspace);
        newObject._receiversInRangeCacheValid = false;
        newObject._listeningInputPorts = null;
        newObject._listeningInputPortsVersion = -1L;
        newObject._listeningOutputPorts = null;
        newObject._listeningOutputPortsVersion = -1L;
        newObject._propertyTransformers = null;
        newObject._propertyTransformersByPort = null;
        newObject._channelListeners = null;
        newObject._receiversInRangeCache = null;
        newObject._receiversInRangeCacheVersion = null;
        newObject._sendingInputPorts = null;
        newObject._sendingInputPortsVersion = -1L;
        newObject._sendingOutputPorts = null;
        newObject._sendingOutputPortsVersion = -1L;
        newObject._channelPort = (ChannelPort) newObject
                .getPort("_channelPort");
        return newObject;
    }

    /** Return a channel port that can be used to set type constraints
     *  between senders and receivers. An channel contains a single port,
     *  which is an instance of ChannelPort. The port is merely used to
     *  set up n type constrains instead of n*n, where n is the number of
     *  ports using the channel.
     *  @return The channel port.
     */
    public ChannelPort getChannelPort() {
        return _channelPort;
    }

    /** Return a list of input ports that can potentially receive data
     *  from this channel.  This must include input ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel. Transparent hierarchy is not supported.
     *  @return The list of input ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>outsideChannel</i> parameter cannot be evaluated.
     */
    public List listeningInputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();

            // NOTE: This caching relies on the fact that WirelessIOPort
            // will increment the workspace version number if any
            // parameter identifying the channel changes.
            if (workspace().getVersion() == _listeningInputPortsVersion) {
                return _listeningInputPorts;
            }

            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity) getContainer();
            Iterator entities = container.entityList().iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();
                Iterator ports = entity.portList().iterator();

                while (ports.hasNext()) {
                    Port port = (Port) ports.next();

                    if (port instanceof WirelessIOPort) {
                        WirelessIOPort castPort = (WirelessIOPort) port;

                        if (castPort.isInput()) {
                            String channelName = castPort.outsideChannel
                                    .stringValue();

                            if (channelName.equals(getName())) {
                                result.add(port);
                            }
                        }
                    }
                }
            }

            _listeningInputPorts = result;
            _listeningInputPortsVersion = workspace().getVersion();
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return a list of output ports that can potentially receive data
     *  from this channel.  This must include output ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return The list of output ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>insideChannel</i> parameter cannot be evaluated.
     */
    public List listeningOutputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();

            // NOTE: This caching relies on the fact that WirelessIOPort
            // will increment the workspace version number if any
            // parameter identifying the channel changes.
            if (workspace().getVersion() == _listeningOutputPortsVersion) {
                return _listeningOutputPorts;
            }

            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity) getContainer();
            Iterator ports = container.portList().iterator();

            while (ports.hasNext()) {
                Port port = (Port) ports.next();

                if (port instanceof WirelessIOPort) {
                    WirelessIOPort castPort = (WirelessIOPort) port;

                    if (castPort.isOutput()) {
                        String channelName = castPort.insideChannel
                                .stringValue();

                        if (channelName.equals(getName())) {
                            result.add(port);
                        }
                    }
                }
            }

            _listeningOutputPorts = result;
            _listeningOutputPortsVersion = workspace().getVersion();
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Override the base class to declare that the dummy port
     *  returned by getChannelPort() does not depend on itself
     *  in a firing. This port is both an input and an output,
     *  so by default there would be a self dependency.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Declare that output does not immediately depend on the input,
        // though there is no lower bound on the time delay.
        declareDelayDependency(_channelPort, _channelPort, 0.0);
    }

    /** Register a property transformer for transmissions from the specified
     *  port.  If null is given for the port, then the property transformer
     *  will be used for all transmissions through this channel.
     *  If the property transformer is already registered, then no changes
     *  are made.
     *  @param transformer The property transformer to be registered.
     *  @param port The port whose transmissions should be subject to the
     *   property transformer, or null to make them subject to all
     *   transmissions through this channel.
     */
    public void registerPropertyTransformer(PropertyTransformer transformer,
            WirelessIOPort port) {
        if (port != null) {
            if (_propertyTransformersByPort == null) {
                _propertyTransformersByPort = new HashMap();
            }

            Set transformers = (Set) _propertyTransformersByPort.get(port);

            if (transformers == null) {
                transformers = new HashSet();
                _propertyTransformersByPort.put(port, transformers);
            }

            transformers.add(transformer);
        } else {
            if (_propertyTransformers == null) {
                _propertyTransformers = new HashSet();
            }

            _propertyTransformers.add(transformer);
        }
    }

    /** Remove a channel listener for transmissions on this channel.
     *  If the listener has not been added, then do nothing.
     *  @param listener The channel listener to remove.
     *  @see #addChannelListener(ChannelListener)
     */
    public void removeChannelListener(ChannelListener listener) {
        if (_channelListeners != null) {
            _channelListeners.remove(listener);
        }
    }

    /** Return a list of input ports that can potentially send data
     *  to this channel.  This must include input ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return The list of input ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>insideChannel</i> parameter cannot be evaluated.
     */
    public List sendingInputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();

            // NOTE: This caching relies on the fact that WirelessIOPort
            // will increment the workspace version number if any
            // parameter identifying the channel changes.
            if (workspace().getVersion() == _sendingInputPortsVersion) {
                return _sendingInputPorts;
            }

            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity) getContainer();
            Iterator ports = container.portList().iterator();

            while (ports.hasNext()) {
                Port port = (Port) ports.next();

                if (port instanceof WirelessIOPort) {
                    WirelessIOPort castPort = (WirelessIOPort) port;

                    if (castPort.isInput()) {
                        String channelName = castPort.insideChannel
                                .stringValue();

                        if (channelName.equals(getName())) {
                            result.add(port);
                        }
                    }
                }
            }

            _sendingInputPorts = result;
            _sendingInputPortsVersion = workspace().getVersion();
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return a list of output ports that can potentially send data
     *  to this channel.  This must include output ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return The list of output ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>outsideChannel</i> parameter cannot be evaluated.
     */
    public List sendingOutputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();

            // NOTE: This caching relies on the fact that WirelessIOPort
            // will increment the workspace version number if any
            // parameter identifying the channel changes.
            if (workspace().getVersion() == _sendingOutputPortsVersion) {
                return _sendingOutputPorts;
            }

            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity) getContainer();
            Iterator entities = container.entityList().iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();
                Iterator ports = entity.portList().iterator();

                while (ports.hasNext()) {
                    Port port = (Port) ports.next();

                    if (port instanceof WirelessIOPort) {
                        WirelessIOPort castPort = (WirelessIOPort) port;

                        if (castPort.isOutput()) {
                            String channelName = castPort.outsideChannel
                                    .stringValue();

                            if (channelName.equals(getName())) {
                                result.add(port);
                            }
                        }
                    }
                }
            }

            _sendingOutputPorts = result;
            _sendingOutputPortsVersion = workspace().getVersion();
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Transform the transmission property to take into account
     *  channel losses, noise, etc., for transmission between the
     *  specified source and the specified destination.  In this
     *  base class, the specified properties are merged with the
     *  defaultProperties so that the resulting properties contain
     *  at least all the fields of the defaultProperties. In addition,
     *  any property transformers that have been registered are applied.
     *  @param properties The transmission properties.
     *  @param source The sending port.
     *  @param destination The receiving port.
     *  @return The transformed properties.
     *  @exception IllegalActionException If the properties cannot
     *   be transformed. Not thrown in this base class.
     *  @see #registerPropertyTransformer(PropertyTransformer, WirelessIOPort)
     */
    public RecordToken transformProperties(RecordToken properties,
            WirelessIOPort source, WirelessIOPort destination)
            throws IllegalActionException {
        RecordToken result = properties;
        Token defaultPropertiesValue = defaultProperties.getToken();

        if (defaultPropertiesValue instanceof RecordToken) {
            if (properties != null) {
                result = RecordToken.merge(properties,
                        (RecordToken) defaultPropertiesValue);
            } else {
                result = (RecordToken) defaultPropertiesValue;
            }
        }

        if (_propertyTransformersByPort != null) {
            //Apply property transformer for the sender.
            Set transformers = (Set) _propertyTransformersByPort.get(source);

            if (transformers != null) {
                Iterator iterator = transformers.iterator();

                while (iterator.hasNext()) {
                    PropertyTransformer transformer = (PropertyTransformer) iterator
                            .next();
                    result = transformer.transformProperties(result, source,
                            destination);
                }
            }

            //Apply property transformers for the receiver.
            transformers = (Set) _propertyTransformersByPort.get(destination);

            if (transformers != null) {
                Iterator iterator = transformers.iterator();

                while (iterator.hasNext()) {
                    PropertyTransformer transformer = (PropertyTransformer) iterator
                            .next();
                    result = transformer.transformProperties(result, source,
                            destination);
                }
            }
        }

        if (_propertyTransformers != null) {
            Iterator iterator = _propertyTransformers.iterator();

            while (iterator.hasNext()) {
                PropertyTransformer transformer = (PropertyTransformer) iterator
                        .next();
                result = transformer.transformProperties(result, source,
                        destination);
            }
        }

        if (_debugging) {
            if (result != null) {
                _debug(" * transmission properties: \"" + result.toString()
                        + "\".");
            } else {
                _debug(" * no transmission properties.\"");
            }
        }

        return result;
    }

    /** Transmit the specified token from the specified port with the
     *  specified properties.  All ports that are in range will receive
     *  the token if they have room in their receiver.
     *  Note that in this base class, a port is in range if it refers to
     *  this channel by name and is at the right place in the hierarchy.
     *  This base class makes no use of the properties argument.
     *  But derived classes may limit the range or otherwise change
     *  transmission properties using this argument.
     *  @param token The token to transmit, or null to clear all
     *   receivers that are in range.
     *  @param port The port from which this is being transmitted.
     *  @param properties The transmission properties (ignored in this base class).
     *  @exception IllegalActionException If a type conflict occurs, or the
     *   director is not a WirelessDirector.
     */
    public void transmit(Token token, WirelessIOPort port,
            RecordToken properties) throws IllegalActionException {
        try {
            workspace().getReadAccess();

            // The following check will ensure that receivers are of type
            // WirelessReceiver.
            if (!(getDirector() instanceof WirelessDirector)) {
                throw new IllegalActionException(this,
                        "AtomicWirelessChannel can only work "
                                + "with a WirelessDirector.");
            }

            Iterator receivers = _receiversInRange(port, properties).iterator();

            if (_debugging) {
                _debug("----\nTransmitting from port: " + port.getFullName());
                _debug("Token value: " + token.toString());

                if (receivers.hasNext()) {
                    _debug("Receivers in range:");
                } else {
                    _debug("No receivers in range.");
                }
            }

            while (receivers.hasNext()) {
                WirelessReceiver receiver = (WirelessReceiver) receivers.next();
                _transmitTo(token, port, receiver, properties);
            }
        } finally {
            workspace().doneReading();
        }
    }

    /** Unregister a property transformer for transmissions from the specified
     *  port (or from null for a generic property transformer). If the transformer
     *  has not been registered, then do nothing.
     *  @param transformer The property transformer to be unregistered.
     *  @param port The port whose transmissions should be subject to the
     *   property transformer, or null for a generic transformer.
     *  @see #registerPropertyTransformer(PropertyTransformer, WirelessIOPort)
     */
    public void unregisterPropertyTransformer(PropertyTransformer transformer,
            WirelessIOPort port) {
        if (port != null) {
            if (_propertyTransformersByPort != null) {
                Set transformers = (Set) _propertyTransformersByPort.get(port);

                if (transformers != null) {
                    transformers.remove(transformer);
                }
            }
        } else {
            if (_propertyTransformers != null) {
                _propertyTransformers.remove(transformer);
            }
        }
    }

    /** React to changes of the specified Settable.
     *  This base class registers as a listener to attributes that
     *  specify the location of objects (and implement the Locatable
     *  interface) so that it is notified by a call to this method
     *  when the location changes.  In this base class, this method
     *  only sets a flag to invalidate its cached list of receivers
     *  in range.  Subclass may do more, for example to determine
     *  whether a receiver that is in process of receiving a message
     *  with a non-zero duration is still in range.
     *  @param settable The object that has changed value.
     */
    public void valueChanged(Settable settable) {
        if (settable instanceof Locatable) {
            _receiversInRangeCacheValid = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the distance between two ports.  This is a convenience
     *  method provided to make it easier to write subclasses that
     *  limit transmission range using position information. In this
     *  base class. locations are specified in 2 dimensions. Subclass
     *  might overwrite this method to deal with 3 dimensions.
     *  FIXME: We may also want this method to be dimension independent.
     *  @param port1 The first port.
     *  @param port2 The second port.
     *  @return The distance between the two ports.
     *  @exception IllegalActionException If the distance
     *   cannot be determined.
     */
    protected double _distanceBetween(WirelessIOPort port1, WirelessIOPort port2)
            throws IllegalActionException {
        double[] p1 = _locationOf(port1);
        double[] p2 = _locationOf(port2);
        return Math.sqrt(((p1[0] - p2[0]) * (p1[0] - p2[0]))
                + ((p1[1] - p2[1]) * (p1[1] - p2[1])));
    }

    /** Return true if the specified destination port is in range of the
     *  specified source port, assuming the source port transmits with
     *  the specified properties.  In this base class, this method returns
     *  true always.  The method assumes that the two ports are
     *  communicating on the same channel, but it does not check
     *  this.  This should be checked by the calling method.
     *  Derived classes will typically use position information
     *  in the source or destination to determine whether ports
     *  are in range.
     *  @param source The source port.
     *  @param destination The destination port.
     *  @param properties The transmission properties (ignored in this base class).
     *  @return True if the destination is in range of the source.
     *  @exception IllegalActionException If it cannot be determined
     *   whether the destination is in range (not thrown in this base
     *   class).
     */
    protected boolean _isInRange(WirelessIOPort source,
            WirelessIOPort destination, RecordToken properties)
            throws IllegalActionException {
        return true;
    }

    /** Return the location of the given port. If the container of the
     *  specified port is the container of this channel, then use the
     *  "_location" attribute of the port.  Otherwise, use the
     *  "_location" attribute of its container. In either case,
     *  register a listener to the location attribute so that valueChanged()
     *  will be called if and when that location changes.
     *  Note that transparent hierarchy is not supported in getting the
     *  location.
     *  The calling method is expected to have read access on the workspace.
     *  Subclasses may override this method to provide some other way of
     *  obtaining location information.
     *  @param port A port with a location.
     *  @return The location of the port.
     *  @exception IllegalActionException If a valid location attribute cannot
     *   be found.
     */
    protected double[] _locationOf(IOPort port) throws IllegalActionException {
        Entity container = (Entity) port.getContainer();
        Locatable location = null;

        if (container == getContainer()) {
            location = (Locatable) port.getAttribute(LOCATION_ATTRIBUTE_NAME,
                    Locatable.class);
        } else {
            location = (Locatable) container.getAttribute(
                    LOCATION_ATTRIBUTE_NAME, Locatable.class);
        }

        if (location == null) {
            throw new IllegalActionException(
                    "Cannot determine location for port " + port.getName()
                            + " with container\n" + container + ".");
        }

        // NOTE: We assume here that the implementation
        // of addValueListener() is smart enough to not add
        // this if it is already a listener.
        location.addValueListener(this);
        return location.getLocation();
    }

    /** Return the list of receivers that can receive from the specified
     *  port with the specified transmission properties. Ports that are contained
     *  by the same container as the specified <i>sourcePort</i> are
     *  not included.  Note that this method does
     *  not guarantee that those receivers will receive.  That is determined
     *  by the transmit() method, which subclasses may override to, for
     *  example, introduce probabilistic message losses.
     *  The calling method is expected to have read access on the workspace.
     *  @param sourcePort The sending port.
     *  @param properties The transmission properties (ignored in this base class).
     *  @return A list of instances of WirelessReceiver.
     *  @exception IllegalActionException If a location of a port cannot be
     *   evaluated.
     */
    protected List _receiversInRange(WirelessIOPort sourcePort,
            RecordToken properties) throws IllegalActionException {
        // This information is carefully cached in
        // a hashtable indexed by the source port.  The cache should
        // be invalidated if:
        //  1) The workspace version changes (which will happen if
        //     any node changes the channel it uses, or if nodes
        //     appear or disappear).
        //  2) The sourcePort has changed its properties parameters
        //     (because this could affect whether other ports are in range).
        //     This is handled by a subclass that uses these properties, like
        //     LimitedRangeChannel.
        //  3) Any listening port has changed its location.  Any
        //     subclass that is using location information needs to
        //     listen for changes in that location information and
        //     invalidate the cache if it changes.
        //  Use the performance.xml test to determine whether/how much
        //  this helps.
        if ((_receiversInRangeCache != null)
                && _receiversInRangeCache.containsKey(sourcePort)
                && (((Long) _receiversInRangeCacheVersion.get(sourcePort))
                        .longValue() == workspace().getVersion())
                && _receiversInRangeCacheValid) {
            // Cached list is valid. Return that.
            return (List) _receiversInRangeCache.get(sourcePort);
        }

        List receiversInRangeList = new LinkedList();
        Iterator ports = listeningInputPorts().iterator();

        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort) ports.next();

            // Skip ports contained by the same container as the source.
            if (port.getContainer() == sourcePort.getContainer()) {
                continue;
            }

            if (_isInRange(sourcePort, port, properties)) {
                Receiver[][] receivers = port.getReceivers();

                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        receiversInRangeList.add(receivers[i][j]);
                    }
                }
            }
        }

        ports = listeningOutputPorts().iterator();

        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort) ports.next();

            if (_isInRange(sourcePort, port, properties)) {
                Receiver[][] receivers = port.getInsideReceivers();

                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        receiversInRangeList.add(receivers[i][j]);
                    }
                }
            }
        }

        if (_receiversInRangeCache == null) {
            _receiversInRangeCache = new HashMap();
            _receiversInRangeCacheVersion = new HashMap();
        }

        _receiversInRangeCache.put(sourcePort, receiversInRangeList);
        _receiversInRangeCacheVersion.put(sourcePort, Long.valueOf(workspace()
                .getVersion()));
        _receiversInRangeCacheValid = true;
        return receiversInRangeList;
    }

    /** Transmit the specified token to the specified receiver.
     *  If necessary, the token will be converted to the resolved
     *  type of the port containing the specified receiver.
     *  @param token The token to transmit, or null to clear
     *   the specified receiver.
     *  @param sender The sending port.
     *  @param receiver The receiver to which to transmit.
     *  @param properties The transmission properties (ignored in this base class).
     *  @exception IllegalActionException If the token cannot be converted
     *   or if the token argument is null and the destination receiver
     *   does not support clear.
     */
    protected void _transmitTo(Token token, WirelessIOPort sender,
            WirelessReceiver receiver, RecordToken properties)
            throws IllegalActionException {
        if (_debugging) {
            _debug(" * transmitting to: "
                    + receiver.getContainer().getFullName());
        }

        if (token != null) {
            if (receiver.hasRoom()) {
                WirelessIOPort destination = (WirelessIOPort) receiver
                        .getContainer();
                Token newToken = destination.convert(token);

                // Transform the properties.
                Token transformedProperties = transformProperties(properties,
                        sender, destination);
                receiver.put(newToken, transformedProperties);
                // Notify any channel listeners after the transmission occurs.
                channelNotify(properties, token, sender, destination);
            }
        } else {
            receiver.clear();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Flag indicating that the cached list of receivers in range
     *  is valid.  This gets set to false whenever
     *  a location for some object whose location
     *  has been obtained by _locationOf() has changed since
     *  the last time this cached list was constructed. In addition,
     *  subclasses may invalidate this if anything else that affects
     *  whether a receiver is in range changes (such as the transmit
     *  properties of a port).
     */
    protected boolean _receiversInRangeCacheValid = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Caches of port lists.
    private List _listeningInputPorts;

    private long _listeningInputPortsVersion = -1L;

    private List _listeningOutputPorts;

    private long _listeningOutputPortsVersion = -1L;

    /** The property transformers that have been registered without
     *  specifying a port.
     */
    private Set _propertyTransformers;

    /** The property transformers that have been registered to
     *  operate on transmissions from a particular port,
     *  indexed by port.
     */
    private HashMap _propertyTransformersByPort;

    /** The channel listeners that have been added.
     */
    private Set _channelListeners;

    private HashMap _receiversInRangeCache;

    private HashMap _receiversInRangeCacheVersion;

    private List _sendingInputPorts;

    private long _sendingInputPortsVersion = -1L;

    private List _sendingOutputPorts;

    private long _sendingOutputPortsVersion = -1L;

    // Name of the location attribute.
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";

    /** Dummy port used to reduce the type constraints to 2N
     *  rather than N^2.  This port is returned by instances of
     *  WirelessIOPort when asked for sink ports.  Do not send
     *  data to this port, however. Instead, use the transmit()
     *  method.
     */
    private ChannelPort _channelPort;
}
