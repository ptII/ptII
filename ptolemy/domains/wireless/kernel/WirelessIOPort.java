/* A port for sending and receiving in the wireless domain.

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

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// WirelessIOPort

/**

 This port communicates via channels without wired connections.
 Channels are instances of WirelessChannel. The port references
 channels by name, where the name is specified by the
 <i>outsideChannel</i> or <i>insideChannel</i> parameter.
 <p>
 This port can be used on the boundary of wireless domain models.
 A port is outside wireless if an outside channel name is given and
 a wireless channel with the given name is contained by the container
 of the port's container (transparent hierarchy is not supported).
 Specially, it will use the specified wireless channel to communicate
 on the outside. A port is inside wireless if an inside channel
 name is given and a wireless channel with the given name is
 contained by the container of this port. It will use the specified
 wireless channel to communicate on the inside. If no outside channel
 or inside channel name is given or the named channel does not exist,
 then the behavior of the port reverts to that of the base class.
 Specifically, it will only communicate if it is wired.
 <p>
 It is valid for a model using the wireless director to have both
 wireless communication and wired communication, i.e. it may contain
 actors with ports using wireless communication (by specifying a
 wireless channel) and actors with ports using wired communication.
 If a port is outside/inside wireless(a wireless channel is specified),
 it will ignore all the communication through the wired connections
 to it if there is any on the outside/inside of it.
 <p>
 The width of this port on either side that is using wireless
 communication is fixed at one. Otherwise, it depends on the
 number of links to the port.
 <p>
 When this port is used for wireless communications, nothing is
 connected to it.  Consequently, methods that access the topology such
 as connectedPortList() and deepConnectedInPortList() return an empty
 list. There are no deeply connected ports.  However, sinkPortList()
 returns the port of the specified wireless channel. A consequence of
 this is that type constraints are automatically set up between ports
 that send on the channel and the channel port.

 @author Edward A. Lee and Xiaojun Liu
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class WirelessIOPort extends TypedIOPort {
    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     *  @exception IllegalActionException If creating the parameters
     *  of this port throws it.
     *  @exception NameDuplicationException If creating the parameters
     *  of this port throws it.
     */
    public WirelessIOPort(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        outsideChannel = new StringParameter(this, "outsideChannel");
        outsideChannel.setExpression("");

        outsideTransmitProperties = new Parameter(this,
                "outsideTransmitProperties");

        insideChannel = new StringParameter(this, "insideChannel");
        insideChannel.setExpression("");

        insideTransmitProperties = new Parameter(this,
                "insideTransmitProperties");
    }

    /** Construct a port with the specified container and name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public WirelessIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, false, false);
    }

    /** Construct a port with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public WirelessIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, isInput, isOutput);

        outsideChannel = new StringParameter(this, "outsideChannel");
        outsideChannel.setExpression("");

        outsideTransmitProperties = new Parameter(this,
                "outsideTransmitProperties");

        insideChannel = new StringParameter(this, "insideChannel");
        insideChannel.setExpression("");

        insideTransmitProperties = new Parameter(this,
                "insideTransmitProperties");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the inside channel.  This is a string that defaults to
     *  the empty string, indicating that communication is not wireless.
     */
    public StringParameter insideChannel;

    /** The transmit properties of this port for inside transmissions.
     *  This field may be used by the channel to determine transmission
     *  range or other properties of the transmission. By default, this
     *  has no value, which indicates to channels to use their default
     *  properties, whatever those might be. When its value is set,
     *  it is required to be a record type.
     */
    public Parameter insideTransmitProperties;

    /** The name of the outside channel.  This is a string that defaults to
     *  the empty string, indicating that communication is not wireless.
     */
    public StringParameter outsideChannel;

    /** The transmit properties of this port for outside transmissions.
     *  This field may be used by the channel to determine transmission
     *  range or other properties of the transmission. By default, this
     *  has no value, which indicates to channels to use their default
     *  properties, whatever those might be. When its value is set,
     *  it is required to be a record type.
     */
    public Parameter outsideTransmitProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is one of the properties attributes, make sure
     *  its value is a record token.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == insideTransmitProperties) {
            Token value = insideTransmitProperties.getToken();

            if ((value != null) && !(value instanceof RecordToken)) {
                throw new IllegalActionException(this,
                        "Expected a record for insideTransmitProperties "
                                + "but got: " + value);
            }
        } else if (attribute == outsideTransmitProperties) {
            Token value = outsideTransmitProperties.getToken();

            if ((value != null) && !(value instanceof RecordToken)) {
                throw new IllegalActionException(this,
                        "Expected a record for outsideTransmitProperties "
                                + "but got: " + value);
            }
        } else if ((attribute == insideChannel)
                || (attribute == outsideChannel)) {
            // Since the channel parameters affect connectivity, we should
            // treat changes to their values as changes to the topology.
            // To do that, we listen for changes and increment the version
            // number of the workspace.
            workspace().incrVersion();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to delegate to the channel if there is
     *  one. If there is no outside channel, then defer to the base
     *  class.
     *  @param token The token to send.
     *  @exception IllegalActionException If the port is not an output,
     *   or if the <i>outsideChannel</i> parameter cannot be evaluated
     *   or if the transmit() method throws an IllegalActionException.
     */
    public void broadcast(Token token) throws IllegalActionException {
        WirelessChannel channel = getOutsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("broadcast to wireless channel " + channel.getName()
                        + ": " + token);
            }

            channel.transmit(token, this,
                    (RecordToken) outsideTransmitProperties.getToken());
        } else {
            super.broadcast(token);
        }
    }

    /** Override the base class to delegate to the channel if there is
     *  one. If there is no outside channel, then defer to the base
     *  class.
     *  @param tokenArray The token array to send
     *  @param vectorLength The number of elements of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the tokens to be sent cannot
     *   be converted to the type of this port
     */
    public void broadcast(Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        WirelessChannel channel = getOutsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("broadcast array of tokens to wireless channel "
                        + channel.getName());
            }

            for (int i = 0; i < tokenArray.length; i++) {
                Token token = tokenArray[i];
                _checkType(token);
                channel.transmit(token, this,
                        (RecordToken) outsideTransmitProperties.getToken());
            }
        } else {
            super.broadcast(tokenArray, vectorLength);
        }
    }

    /** Override the base class to delegate to the channel if there is
     *  one. If there is no outside channel, then clear as in the base
     *  class.
     *  @exception IllegalActionException If a receiver does not support
     *   clear().
     */
    public void broadcastClear() throws IllegalActionException {
        WirelessChannel channel = getOutsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("broadcast clear.");
            }

            channel.transmit(null, this,
                    (RecordToken) outsideTransmitProperties.getToken());
        } else {
            super.broadcastClear();
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
        WirelessIOPort newObject = (WirelessIOPort) super.clone(workspace);
        newObject._receivers = null;
        newObject._insideReceivers = null;
        newObject._insideChannel = null;
        newObject._insideChannelVersion = -1L;
        newObject._outsideChannel = null;
        newObject._outsideChannelVersion = -1L;
        return newObject;
    }

    /** Override the base class to create receivers for WirelessIOPort.
     *  If there is an outside channel, create a receiver for outside
     *  communication. If there is an inside channel, create a receiver
     *  for inside communication.
     *  @exception IllegalActionException If this port is not
     *   an opaque input port or if there is no director.
     */
    public void createReceivers() throws IllegalActionException {
        // This call will create receivers based on relations that
        // are linked to the port.
        super.createReceivers();

        if (getOutsideChannel() != null) {
            _receivers = new Receiver[1][1];
            _receivers[0][0] = _newReceiver();
        }

        if (getInsideChannel() != null) {
            _insideReceivers = new Receiver[1][1];
            _insideReceivers[0][0] = _newInsideReceiver();
        }
    }

    /** Get the channel specified by the <i>insideChannel</i> parameter.
     *  The channel is contained by the container of this port.
     *  Transparent hierarchy is ignored in getting the inside channel.
     *  @return A wireless channel, or null if no channel is specified
     *  or if the specified channel does not exist.
     *  @exception IllegalActionException If the <i>insideChannel</i>
     *  parameter value cannot be evaluated.
     */
    public WirelessChannel getInsideChannel() throws IllegalActionException {
        if (workspace().getVersion() == _insideChannelVersion) {
            return _insideChannel;
        }

        _insideChannel = null;

        String channelName = insideChannel.stringValue();
        Nameable container = getContainer();

        if (container instanceof CompositeEntity) {
            ComponentEntity entity = ((CompositeEntity) container)
                    .getEntity(channelName);

            if (entity instanceof WirelessChannel) {
                _insideChannel = (WirelessChannel) entity;
            }
        }

        _insideChannelVersion = workspace().getVersion();
        return _insideChannel;
    }

    /** Override the base class to return the inside receiver for wireless
     *  communication if wireless communication is being used. Otherwise,
     *  defer to the base class.
     *  @return The local inside receivers, or an empty array if there are
     *   none.
     */
    public Receiver[][] getInsideReceivers() {
        try {
            if (getInsideChannel() != null) {
                return _insideReceivers;
            } else {
                return super.getInsideReceivers();
            }
        } catch (IllegalActionException e) {
            // We should not get this far without being able
            // to parse the inside channel specification.
            throw new InternalErrorException(e);
        }
    }

    /** Get the channel specified by the <i>outsideChannel</i> parameter.
     *  The channel is contained by the container of the container of this
     *  port. Transparent hierarchy is ignored in getting the outside
     *  channel.
     *  @return A wireless channel, or null if no channel is specified
     *  or if the specified channel does not exist.
     *  @exception IllegalActionException If the <i>outsideChannel</i>
     *  parameter value cannot be evaluated.
     */
    public WirelessChannel getOutsideChannel() throws IllegalActionException {
        if (workspace().getVersion() == _outsideChannelVersion) {
            return _outsideChannel;
        }

        _outsideChannel = null;

        String channelName = outsideChannel.stringValue();
        Nameable container = getContainer();

        if (container != null) {
            Nameable containersContainer = container.getContainer();

            if (containersContainer instanceof CompositeEntity) {
                ComponentEntity channel = ((CompositeEntity) containersContainer)
                        .getEntity(channelName);

                if (channel instanceof WirelessChannel) {
                    _outsideChannel = (WirelessChannel) channel;
                }
            }
        }

        _outsideChannelVersion = workspace().getVersion();
        return _outsideChannel;
    }

    /** Get the properties token associated with the data token most
     *  recently retrieved using get().
     *  @param channelIndex The channel index.
     *  @see #get(int)
     *  @see #get(int, int)
     *  @return The properties token of the most recently received
     *   data token, or null if there hasn't been one.
     */
    public Token getProperties(int channelIndex) {
        try {
            _workspace.getReadAccess();

            Receiver[][] localReceivers = getReceivers();

            // NOTE: The checks of the base class get() aren't necessary
            // because we assume get() has just been called.
            Token token = null;

            for (int j = 0; j < localReceivers[channelIndex].length; j++) {
                Token localToken = ((WirelessReceiver) localReceivers[channelIndex][j])
                        .getProperties();

                if (token == null) {
                    token = localToken;
                }
            }

            return token;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get the properties token associated with the data token most
     *  recently retrieved using getInside().
     *  @param channelIndex The channel index.
     *  @see #getInside(int)
     *  @return The properties token of the most recently received
     *   data token, or null if there hasn't been one.
     */
    public Token getPropertiesInside(int channelIndex) {
        try {
            _workspace.getReadAccess();

            Receiver[][] localReceivers = getInsideReceivers();

            // NOTE: The checks of the base class getInside() aren't necessary
            // because we assume get() has just been called.
            Token token = null;

            for (int j = 0; j < localReceivers[channelIndex].length; j++) {
                Token localToken = ((WirelessReceiver) localReceivers[channelIndex][j])
                        .getProperties();

                if (token == null) {
                    token = localToken;
                }
            }

            return token;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Override the base class to return the outside receiver for wireless
     *  communication if wireless communication is being used. Otherwise,
     *  defer to the base class.
     *  @return The local receivers, or an empty array if there are none.
     */
    public Receiver[][] getReceivers() {
        try {
            if (getOutsideChannel() != null) {
                if (_receivers == null) {
                    return _EMPTY_RECEIVERS;
                }

                return _receivers;
            } else {
                return super.getReceivers();
            }
        } catch (IllegalActionException e) {
            // We should not get to here if the channel name cannot be
            // parsed.
            throw new InternalErrorException(e);
        }
    }

    /** Get the width of the port. If the outside is wireless, then
     *  the width is always 1. Otherwise, it depends on the number of
     *  links to the port.
     *  @return The width of the port.
     * @exception IllegalActionException
     */
    public int getWidth() throws IllegalActionException {
        if (_outsideIsWireless()) {
            return 1;
        } else {
            return super.getWidth();
        }
    }

    /** Return the inside width of this port. If the inside is wireless,
     *  then the width is always 1. Otherwise, the width is determined by
     *  the number of links to the port.
     *  @return The inside width of this port.
     * @exception IllegalActionException
     */
    public int getWidthInside() throws IllegalActionException {
        if (_insideIsWireless()) {
            return 1;
        } else {
            return super.getWidthInside();
        }
    }

    /** Override the base class to always return true if there is
     *  a wireless channel, and otherwise, defer to the base class.
     *  For wireless channels, if a particular receiver does not have
     *  room, the channel treats it as if it were not in range.
     *  @param channelIndex The channel index.
     *  @return True if there is room for a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if this is not an output port, or if the channel index
     *   is out of range.
     */
    public boolean hasRoom(int channelIndex) throws IllegalActionException {
        WirelessChannel channel = getOutsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("hasRoom on channel " + channelIndex + " returns true.");
            }

            return true;
        } else {
            return super.hasRoom(channelIndex);
        }
    }

    /** Override the base class to always return true if there is
     *  a wireless channel, and otherwise, defer to the base class.
     *  For wireless channels, if a particular receiver does not have
     *  room, the channel treats it as if it were not in range.
     *  @param channelIndex The channel index.
     *  @return True if there is room for a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if this is not an output port, or if the channel index
     *   is out of range.
     */
    public boolean hasRoomInside(int channelIndex)
            throws IllegalActionException {
        WirelessChannel channel = getInsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("hasRoomInside on channel " + channelIndex
                        + " returns true.");
            }

            return true;
        } else {
            return super.hasRoomInside(channelIndex);
        }
    }

    /** Return a list of the ports that can potentially accept data
     *  from this port when it sends on the inside.  If the port is
     *  inside wireless, then this includes only the wireless channel
     *  port. Otherwise, this includes opaque input ports that are
     *  connected on the outside to this port and opaque output ports
     *  that are connected on the inside to this one.
     *  @return A list of IOPort objects.
     */
    public List insideSinkPortList() {
        try {
            WirelessChannel channel = getInsideChannel();

            if (channel != null) {
                List result = new LinkedList();
                result.add(channel.getChannelPort());
                return result;
            } else {
                return super.insideSinkPortList();
            }
        } catch (IllegalActionException e) {
            // This is not ideal, but the base class doesn't
            // declare exceptions here.
            throw new InternalErrorException(e);
        }
    }

    /** Return a list of the ports that can potentially send data to
     *  this port from the inside.  If the port is inside wireless,
     *  then this includes only the wireless channel port.  Otherwise,
     *  this includes opaque output ports that are connected on the
     *  outside to this port and opaque input ports that are connected
     *  on the inside to this one.
     *  @return A list of IOPort objects.
     */
    public List insideSourcePortList() {
        try {
            WirelessChannel channel = getInsideChannel();

            if (channel != null) {
                List result = new LinkedList();
                result.add(channel.getChannelPort());
                return result;
            } else {
                return super.insideSourcePortList();
            }
        } catch (IllegalActionException e) {
            // This is not ideal, but the base class doesn't
            // declare exceptions here.
            throw new InternalErrorException(e);
        }
    }

    // FIXME: numberOfInsideSinks?
    // FIXME: numberOfInsideSources?
    // Apparently, these are not implemented in the base class.

    /** Return 1 if the port is outside wireless, which represents the
     *  channel port of the outside wireless channel. If not,
     *  defer to the base class.
     *  @return The number of ports that can receive data from this one.
     */
    public int numberOfSinks() {
        try {
            WirelessChannel channel = getOutsideChannel();

            if (channel != null) {
                return 1;
            } else {
                return super.numberOfSinks();
            }
        } catch (IllegalActionException e) {
            // This is not ideal, but the base class doesn't
            // throw this.
            throw new InternalErrorException(e);
        }
    }

    /** Return 1 if the port is outside wireless,, which represents the
     *  channel port of the outside wireless channel. If not, defer to
     *  the base class.
     *  @return The number of ports that can receive data from this one.
     */
    public int numberOfSources() {
        try {
            WirelessChannel channel = getOutsideChannel();

            if (channel != null) {
                return 1;
            } else {
                return super.numberOfSources();
            }
        } catch (IllegalActionException e) {
            // This is not ideal, but the base class doesn't
            // throw this.
            throw new InternalErrorException(e);
        }
    }

    /** Override the base class to delegate to the wireless channel if
     *  the port is outside wireless. If there is no outside channel,
     *  then defer to the base class.
     *  @param channelIndex The index of the channel, from 0 to width-1.
     *  If there is an outside channel, then this argument is ignored.
     *  @param token The token to send.
     *  @exception IllegalActionException If the port is not an output,
     *   or if the token to be sent cannot
     *   be converted to the type of this port, or if the token is null.
     *  @exception NoRoomException If there is no room in the receiver.
     *   This should not occur in the DE domain.
     */
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        WirelessChannel channel = getOutsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("send to wireless channel " + channel.getName() + ": "
                        + token);
            }
            if (token != null) {
                _checkType(token);
                channel.transmit(token, this,
                        (RecordToken) outsideTransmitProperties.getToken());
            }
        } else {
            super.send(channelIndex, token);
        }
    }

    /** Override the base class to delegate to the wireless channel if
     *  the port is outside wireless. If there is no outside channel,
     *  then defer to the base class.
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  If there is an outside channel, then this argument is ignored.
     *  @param tokenArray The token array to send
     *  @param vectorLength The number of elements of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the tokens to be sent cannot
     *   be converted to the type of this port, or if the <i>vectorLength</i>
     *   argument is greater than the length of the <i>tokenArray</i>
     *   argument.
     */
    public void send(int channelIndex, Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        WirelessChannel channel = getOutsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("broadcast array of tokens to wireless channel "
                        + channel.getName());
            }

            for (int i = 0; i < tokenArray.length; i++) {
                Token token = tokenArray[i];
                _checkType(token);
                channel.transmit(token, this,
                        (RecordToken) outsideTransmitProperties.getToken());
            }
        } else {
            super.send(channelIndex, tokenArray, vectorLength);
        }
    }

    /** Override the base class to delegate to the wireless channel if
     *  the port is outside wireless. If there is no outside channel,
     *  then defer to the base class.
     *  @param channelIndex The index of the channel, from 0 to width-1.
     *  If there is an outside channel, then this argument is ignored.
     *  @exception IllegalActionException If a receiver does not support
     *   clear().
     */
    public void sendClear(int channelIndex) throws IllegalActionException {
        WirelessChannel channel = getOutsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("send clear.");
            }

            channel.transmit(null, this,
                    (RecordToken) outsideTransmitProperties.getToken());
        } else {
            super.sendClear(channelIndex);
        }
    }

    /** Override the base class to delegate to the wireless channel if
     *  the port is inside wireless. If there is no inside channel,
     *  then defer to the base class.
     *  @param channelIndex The index of the channel, from 0 to width-1.
     *  If there is an inside channel, then this argument is ignored.
     *  @exception IllegalActionException If a receiver does not support
     *   clear().
     */
    public void sendClearInside(int channelIndex) throws IllegalActionException {
        WirelessChannel channel = getInsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("send clear inside.");
            }

            channel.transmit(null, this,
                    (RecordToken) outsideTransmitProperties.getToken());
        } else {
            super.sendClearInside(channelIndex);
        }
    }

    // FIXME: Where is the sendInside vector version?

    /** Override the base class to delegate to the wireless channel if
     *  the port is inside wireless. If there is no inside channel,
     *  then defer to the base class.
     *  @param channelIndex The index of the channel, from 0 to width-1.
     *  If there is an inside channel, then this argument is ignored.
     *  @param token The token to send
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If conversion to the type of
     *   the destination port cannot be done.
     */
    public void sendInside(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        WirelessChannel channel = getInsideChannel();

        if (channel != null) {
            if (_debugging) {
                _debug("send inside to wireless channel " + channel.getName()
                        + ": " + token);
            }

            _checkType(token);
            channel.transmit(token, this,
                    (RecordToken) insideTransmitProperties.getToken());
        } else {
            super.sendInside(channelIndex, token);
        }
    }

    /** Return a list of the ports that can potentially accept data
     *  from this port when it sends on the outside.  If the port is
     *  outside wireless, then this includes only the channel
     *  port. Otherwise, this includes opaque input ports that are
     *  connected on the outside to this port and opaque output ports
     *  that are connected on the inside to this one.
     *  @return A list of IOPort objects.
     */
    public List sinkPortList() {
        try {
            WirelessChannel channel = getOutsideChannel();

            if (channel != null) {
                List result = new LinkedList();
                result.add(channel.getChannelPort());
                return result;
            } else {
                return super.sinkPortList();
            }
        } catch (IllegalActionException e) {
            // This is not ideal, but the base class doesn't
            // declare exceptions here.
            throw new InternalErrorException(e);
        }
    }

    /** Return a list of the ports that can potentially send data to
     *  this port from the outside.  If the port is outside wireless,
     *  then this includes only the channel port. Otherwise, this includes
     *  opaque output ports that are connected on the outside to this port
     *  and opaque input ports that are connected on the inside to this one.
     *  @return A list of IOPort objects.
     */
    public List sourcePortList() {
        try {
            WirelessChannel channel = getOutsideChannel();

            if (channel != null) {
                List result = new LinkedList();
                result.add(channel.getChannelPort());
                return result;
            } else {
                return super.sourcePortList();
            }
        } catch (IllegalActionException e) {
            // This is not ideal, but the base class doesn't
            // declare exceptions here.
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if the port is inside wireless.
     *  @return True if the inside is wireless.
     */
    protected boolean _insideIsWireless() {
        try {
            return (getInsideChannel() != null);
        } catch (IllegalActionException e) {
            return false;
        }
    }

    /** Return true if the port is outside wireless.
     *  @return True if the outside is wireless.
     */
    protected boolean _outsideIsWireless() {
        try {
            return (getOutsideChannel() != null);
        } catch (IllegalActionException e) {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // To ensure that getReceivers() and variants never return null.
    private static Receiver[][] _EMPTY_RECEIVERS = new Receiver[0][0];

    // Receivers for this port for outside wireless connections.
    private Receiver[][] _receivers;

    // Receivers for this port for inside wireless connections.
    private Receiver[][] _insideReceivers;

    // Cached versions.
    private WirelessChannel _insideChannel;

    private long _insideChannelVersion = -1L;

    private WirelessChannel _outsideChannel;

    private long _outsideChannelVersion = -1L;
}
