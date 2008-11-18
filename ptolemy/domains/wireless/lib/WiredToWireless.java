/* An actor with wired input ports and a wireless output port.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.domains.wireless.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// WiredToWireless

/**
 On each firing, this actor reads at most one token from the <i>payload</i>
 and <i>properties</i> input ports, and outputs the payload on the wireless
 <i>output</i> port with the specified transmit properties.
 If there are no properties, then the output is sent without properties.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class WiredToWireless extends TypedAtomicActor {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public WiredToWireless(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        payload = new TypedIOPort(this, "payload", true, false);
        (new SingletonParameter(payload, "_showName"))
                .setToken(BooleanToken.TRUE);

        properties = new TypedIOPort(this, "properties", true, false);
        (new SingletonParameter(properties, "_showName"))
                .setToken(BooleanToken.TRUE);

        // FIXME: This should be constrained to be a record token.
        // How to do that?
        // Create and configure the parameters.
        outputChannelName = new StringParameter(this, "outputChannelName");
        outputChannelName.setExpression("AtomicWirelessChannel");

        // Create and configure the ports.
        output = new WirelessIOPort(this, "output", false, true);
        output.outsideChannel.setExpression("$outputChannelName");
        output.setTypeSameAs(payload);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port that receives the payload to be transmitted on the
     *  <i>output</i> port.
     */
    public TypedIOPort payload;

    /** Port that sends a wireless output. This has the same type as
     *  the <i>payload</i> port.
     */
    public WirelessIOPort output;

    /** Name of the output channel. This is a string that defaults to
     *  "AtomicWirelessChannel".
     */
    public StringParameter outputChannelName;

    /** input port that receives the properties to be used for transmission
     *  on the <i>output</i> port. The type of this port is a record type.
     */
    public TypedIOPort properties;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        WiredToWireless newObject = (WiredToWireless) (super.clone(workspace));

        // set the type constraints
        newObject.output.setTypeSameAs(newObject.payload);
        return newObject;
    }

    /** Read at most one token from the <i>payload</i> and <i>properties</i>
     *  ports and transmit the payload on the <i>output</i> port with the
     *  specified properties.  If there are no properties, then send with
     *  no properties.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        if (payload.hasToken(0)) {
            Token inputValue = payload.get(0);

            if (_debugging) {
                _debug("Input data received: " + inputValue.toString());
            }

            if (properties.hasToken(0)) {
                Token propertiesValue = properties.get(0);

                // The following will throw an exception if the value is
                // not a RecordToken.
                output.outsideTransmitProperties.setToken(propertiesValue);
            } else {
                output.outsideTransmitProperties.setToken((RecordToken) null);
            }

            output.send(0, inputValue);
        }
    }
}
