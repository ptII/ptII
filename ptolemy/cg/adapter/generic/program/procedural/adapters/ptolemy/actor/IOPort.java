/* Code generator adapter for IOPort.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor;

import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.PortCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////IOPort

/**
Code generator adapter for {@link ptolemy.actor.IOPort}.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */

public class IOPort extends NamedProgramCodeGeneratorAdapter implements
        PortCodeGenerator {

    /** Construct the code generator adapter
     *  for the given IOPort.
     *  @param component The IOPort.
     */
    public IOPort(ptolemy.actor.IOPort component) {
        super(component);
    }

    /////////////////////////////////////////////////////////////////////
    ////                           public methods                    ////

    /** 
     * Generate code for replacing the get() macro.
     * This delegates to the receiver adapter for the specified
     * channel and asks it to generate the get code.
     * 
     * @param channel The channel for which to generate the get code.
     * @return The code that gets data from the specified channel.
     * @exception IllegalActionException If the receiver adapter is
     *  not found or it encounters an error while generating the
     *  get code.
     */
    public String generateGetCode(String channel, String offset) throws IllegalActionException {
        Receiver[][] receivers = getReceiverAdapters();
        int channelIndex = Integer.parseInt(channel);
        // FIXME: take care of the offset, and why are we getting all the receivers all the time?
        // FIXME: Don't know why would a channel have more than one relations
        // Thus for now to make sure we don't run into such problems, have a check to ensure
        // this is not true. IF THIS IS TRUE HOWEVER, then the generated code in the receivers would
        // need to change to ensure no name collisions between multiple receivers within the same 
        // channel would occur.
        if (receivers.length != 0) {
            if (receivers[channelIndex].length > 1) {
                throw new IllegalActionException(
                        "Didn't take care of the case where one channel "
                        + "has more than one receiver");
            }
            if (receivers[channelIndex].length > 0) {
                return receivers[channelIndex][0].generateGetCode(offset);
            }
        }
        Type type = ((TypedIOPort)getComponent()).getType();
        String typeString = getCodeGenerator().codeGenType(type);
        // The component port is not connected to anything, so hasToken should
        // always return something trivial;
        return "$convert_Integer_" + typeString + "(0)";
    }

    /** 
     * Generate code for replacing the hasToken() macro.
     * This delegates to the receiver adapter for the specified
     * channel and asks it to generate the hasToken code.
     * 
     * @param channel The channel for which to generate the hasToken code.
     * @return The code that checks whether there is data in the 
     *  specified channel.
     * @exception IllegalActionException If the receiver adapter is
     *  not found or it encounters an error while generating the
     *  hasToken code.
     */
    public String generateHasTokenCode(String channel, String offset)
            throws IllegalActionException {
        Receiver[][] receivers = getReceiverAdapters();
        int channelNumber = Integer.parseInt(channel);
        // FIXME: take care of the offset, and why are we getting all the receivers all the time?
        if (receivers.length != 0) {
            if (receivers[channelNumber].length > 1) {
                throw new IllegalActionException(
                        "Didn't take care of the case where one channel "
                        + "has more than one receiver");
            }
            if (receivers[channelNumber].length > 0) {
                return receivers[channelNumber][0].generateHasTokenCode(offset);
            }
        }
        // The component port is not connected to anything, so hasToken should
        // always return false;
        return "false";
    }

    /** 
     * Generate code for replacing the send() macro.
     * This delegates to the receiver adapter for the specified
     * channel and asks it to generate the send code.
     * 
     * @param channel The channel for which to generate the send code.
     * @return The code that sends data to the specified channel.
     * @exception IllegalActionException If the receiver adapter is
     *  not found or it encounters an error while generating the
     *  send code.
     */
    public String generatePutCode(String channel, String offset, String dataToken)
            throws IllegalActionException {

        Receiver[][] remoteReceivers = getRemoteReceiverAdapters();
        int channelIndex = Integer.parseInt(channel);
        // FIXME: take care of the offset, and why are we getting all the receivers all the time?
        if ((remoteReceivers == null)
                || (remoteReceivers.length <= channelIndex)
                || (remoteReceivers[channelIndex] == null)) {
            return "";
        }
        StringBuffer code = new StringBuffer();

        for (int i = 0; i < remoteReceivers[channelIndex].length; i++) {
            code.append(remoteReceivers[channelIndex][i].generatePutCode(
                    (ptolemy.actor.IOPort)this.getComponent(), offset, dataToken));
        }
        return code.toString();
    }

    /** Generate the initialize code for this IOPort.
     *  The initialize code is generated by appending the
     *  initialize code for each receiver contained by this
     *  IOPort.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If an error occurs
     *  when getting the receiver adapters or 
     *  generating their initialize code.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Receiver[][] receivers = getReceiverAdapters();

        for (int i = 0; i < receivers.length; i++) {
            for (int j = 0; j < receivers[i].length; j++) {
                code.append(receivers[i][j].generateInitializeCode());
            }
        }
        return code.toString();
    }

//    /** Generate the preinitialize code for this IOPort.
//     *  The preinitialize code is generated by appending the
//     *  preinitialize code for each receiver contained by this
//     *  IOPort.
//     *  @return The generated preinitialize code.
//     *  @exception IllegalActionException If an error occurs
//     *  when getting the receiver adapters or 
//     *  generating their preinitialize code.
//     */
//    public String generatePreinitializeCode() throws IllegalActionException {
//        StringBuffer code = new StringBuffer();
//
//        Receiver[][] receivers = getReceiverAdapters();
//
//        for (int i = 0; i < receivers.length; i++) {
//            for (int j = 0; j < receivers[i].length; j++) {
//                code.append(receivers[i][j].generatePreinitializeCode());
//            }
//        }
//        return code.toString();
//    }

//    /** Generate the wrapup code for this IOPort.
//     *  The wrapup code is generated by appending the
//     *  wrapup code for each receiver contained by this
//     *  IOPort.
//     *  @return The generated wrapup code.
//     *  @exception IllegalActionException If an error occurs
//     *  when getting the receiver adapters or 
//     *  generating their wrapup code.
//     */
//    public String generateWrapupCode() throws IllegalActionException {
//        StringBuffer code = new StringBuffer();
//
//        Receiver[][] receivers = getReceiverAdapters();
//
//        for (int i = 0; i < receivers.length; i++) {
//            for (int j = 0; j < receivers[i].length; j++) {
//                code.append(receivers[i][j].generateWrapupCode());
//            }
//        }
//        return code.toString();
//    }

//    /** Generate the shared code for this IOPort.
//     *  The shared code is generated by appending the
//     *  shared code for each receiver contained by this
//     *  IOPort.
//     *  @return The generated shared code.
//     *  @exception IllegalActionException If an error occurs
//     *  when getting the receiver adapters or 
//     *  generating their shared code.
//     */
//    public Set getSharedCode() throws IllegalActionException {
//        Set code = new HashSet();
//
//        Receiver[][] receivers = getReceiverAdapters();
//
//        for (int i = 0; i < receivers.length; i++) {
//            for (int j = 0; j < receivers[i].length; j++) {
//                code.addAll(receivers[i][j].getSharedCode());
//            }
//        }
//        return code;
//    }

    /** 
     * Get the adapters for receiver contained in this port.
     * @return The adapters.
     * @throws IllegalActionException Thrown if {@link #getAdapter(Object)}
     * throws it.
     */
    public Receiver[][] getReceiverAdapters() throws IllegalActionException {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        ptolemy.actor.Receiver[][] receivers = port.getReceivers();
        Receiver[][] receiverAdapters = new Receiver[receivers.length][];
        for (int i = 0; i < receivers.length; i++) {
            receiverAdapters[i] = new Receiver[receivers[i].length];
            for (int j = 0; j < receivers[i].length; j++) {
                receiverAdapters[i][j] = (Receiver) getAdapter(receivers[i][j]);
            }
        }
        return receiverAdapters;
    }

    /** 
     * Get the adapters for the remote receivers connected to this port.
     * @return The adapters.
     * @throws IllegalActionException Thrown if {@link #getAdapter(Object)}
     * throws it.
     */
    public Receiver[][] getRemoteReceiverAdapters()
            throws IllegalActionException {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();

        ptolemy.actor.Receiver[][] farReceivers = port.getRemoteReceivers();
        Receiver[][] receiverAdapters = new Receiver[farReceivers.length][];
        for (int i = 0; i < farReceivers.length; i++) {
            receiverAdapters[i] = new Receiver[farReceivers[i].length];
            for (int j = 0; j < farReceivers[i].length; j++) {
                receiverAdapters[i][j] = (Receiver) getAdapter(farReceivers[i][j]);
            }
        }
        return receiverAdapters;
    }
}
