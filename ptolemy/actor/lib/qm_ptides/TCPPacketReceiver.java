/* TCPPacketReceiver simulates a device that resolves variable-size
 * TCP packets to release their token contents

@Copyright (c) 2008-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.lib.qm_ptides;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

import ptolemy.actor.TypedIOPort;
import ptolemy.domains.ptides.lib.InputDevice;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.domains.ptides.lib.NetworkReceiver;
//import ptolemy.domains.ptides.lib.NetworkReceiver.PortFunction;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** 
 * 
 *  This actor receives RecordTokens from a network input port and decomposes the
 *  packet into several tokens that are contained within the packet.
 *  This actor should used with a PTIDES director and in pairswith the {@link TCPPacketReceiver},
 *  typically with a network fabric model in between.
 *  
 *  {@link TCPPacketReceiver} decomposes TCP packets containing PTIDES events
 *  
 *  </p><p>
 *  This actor consumes one input token and creates a stream of output tokens, each with
 *  the timestamp specified as a part of the token.
 *   
 *
 *  @author Ilge Akkaya
 *  @version 
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating 
 *  @Pt.AcceptedRating
*/
public class TCPPacketReceiver extends InputDevice {

    public TCPPacketReceiver(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        input.setTypeEquals(BaseType.RECORD);
        //output.setTypeEquals(BaseType.DOUBLE);
    }
    
    /* public parameters */
    public TypedIOPort input;

    public TypedIOPort output;
    
    
    
    ////////////
    
    /** Decompose RecordToken into its components.
     *
     * 
     */
    
    public void fire() throws IllegalActionException {
        
        super.fire();
        Director director = getDirector();

        if (director == null || !(director instanceof PtidesBasicDirector)) {
            throw new IllegalActionException(this, "Director expected to"
                    + "be a Ptides director, but it's not.");
        }

        PtidesBasicDirector ptidesDirector = (PtidesBasicDirector) director;

        // consume input
        if (input.hasToken(0)) {
            
            RecordToken fullTCPFrame = (RecordToken) input.get(0);
            
            RecordToken TCPHeader = (RecordToken)fullTCPFrame.get(TCPlabel);
            
            RecordToken dataContent = (RecordToken)fullTCPFrame.get(tokens);

            if (fullTCPFrame.labelSet().size() != 2) {
                throw new IllegalActionException(
                        "the input record token has a size not equal to 2: "
                                + "Here we assume the Record is of types: TCPHeader + tokens");
            }
         

            //if(record.get(payload).length == BaseType.RECORD)
            
                
                // produce different events for each token within the record
                //RecordToken payloadToken = (RecordToken)record.get(payload);
                // make sure the TCPFrameTransmitter wraps the event labels with name "payloadLabels"
                
                Iterator pLabels = dataContent.labelSet().iterator();
                // send all the received tokens to the outputs according to their timestamps
                while (pLabels.hasNext()) 
                {
               
                    Time lastModelTime = ptidesDirector.getModelTime();
                    int lastMicrostep = ptidesDirector.getMicrostep();
                    
                    String singleEventLabel = (String) pLabels.next();
                    //String singleEventLabel = Integer.toString(i);
                    RecordToken singleEventRecord = (RecordToken)dataContent.get(singleEventLabel);
                    Time tokenTimestamp = new Time(getDirector(),
                            ((DoubleToken)(singleEventRecord.get(timestamp))).doubleValue());
                    
                    int tokenMicrostep = ((IntToken)(singleEventRecord.get(microstep))).intValue();
                    
                    if (output != null) {
                        ptidesDirector.setTag(tokenTimestamp, tokenMicrostep);
                        output.send(0, singleEventRecord.get(payload));
                    }
                    ptidesDirector.setTag(lastModelTime, lastMicrostep); 
                }
        }
        //ptidesDirector.setTag(lastModelTime, lastMicrostep);
    }

    /** Perform a check to see if this device is connected to a network
     *  port on the outside. If not, throw an exception. Also call
     *  preinitialize of the super class.
     *  @exception IllegalActionException If there are no outside source
     *  ports, or if any of the outside source ports is not a network
     *  port.
     */
    public void preinitialize() throws IllegalActionException {

        super.preinitialize();

        boolean flag = false;
        for (IOPort input : (List<IOPort>) inputPortList()) {
            for (IOPort sourcePort : (List<IOPort>) input.sourcePortList()) {
                if (sourcePort.getContainer() == getContainer()) {
                    flag = true;
                }
            }
        }
        if (!flag) {
            throw new IllegalActionException(
                    this,
                    "A NetworkReceiver must be connected to a port "
                            + "on the outside, and that port should be a network port "
                            + "(a port with the parameter networkPort).");
        }
    }
    

  /*  *//** Return the type constraints of this actor. The type constraint is
     *  that the input RecordToken has two fields, a "TCPlabel" of type
     *  double and a "tokens" of type Token[].
     *  @return a list of Inequality.
     *
     *//*
    public Set<Inequality> typeConstraints() {
        String[] labels = { TCPlabel, tokens };
        Type[] types = { BaseType.RECORD, BaseType.RECORD };
        RecordType type = new RecordType(labels, types);
        input.setTypeAtMost(type);

        HashSet typeConstraints = new HashSet<Inequality>();
        Inequality inequality = new Inequality(new PortFunction(),
                output.getTypeTerm());
        typeConstraints.add(inequality);
        return typeConstraints;
    }*/
    
///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // This is fashioned after RecordDisassembler.
    // See that class for an explanation (such as it is).
    private class PortFunction extends MonotonicFunction {
        public Object getValue() throws IllegalActionException {
            if (input.getType() == BaseType.UNKNOWN) {
                return BaseType.UNKNOWN;
            } else if (input.getType() instanceof RecordType) {
                RecordType type = (RecordType) input.getType();
                Type fieldType = type.get(payload);
                if (fieldType == null) {
                    return BaseType.UNKNOWN;
                } else {
                    return fieldType;
                }
            } else {
                throw new IllegalActionException(TCPPacketReceiver.this,
                        "Invalid type for input port");
            }
        }

        /** Return the type variable in this inequality term. If the
         *  type of the input port is not declared, return an one
         *  element array containing the inequality term representing
         *  the type of the port; otherwise, return an empty array.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            InequalityTerm portTerm = input.getTypeTerm();
            if (portTerm.isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = portTerm;
                return variable;
            }

            return (new InequalityTerm[0]);
        }
    }

    //*************** TCP  FIELDS ***************//
    /** label of the source port Field -- 16 bits.
     */
    private static final String sourcePort = "sourcePort";

    /** label of the destination port Field -- 16 bits.
     */
    private static final String destinationPort = "destinationPort";
    
    /** label of the sequenceNumber  Field -- 32 bits.
     */
    private static final String sequenceNumber = "sequenceNumber";
    
    /** label of the acknowledgementNumber  Field -- 32 bits.
     */
    private static final String acknowledgementNumber = "ackNumber";

    /** label of the OFFSET/Control Bits - 16 bits in total. Decomposed as;
     * Data Offset  (4 bits)
     * Reserved     (3 bits)
     * ECN          (3 bits) ( Explicit Congestion Notification)
     * Control Bits ( 6 bits)
     */
    
    private static final String offsetControlBits = "offsetControlBits";

    /** label of the window size  Field -- 16 bits.
     */
    private static final String windowSize = "windowSize";
    
    /** label of the checksum   Field -- 16 bits.
     */
    private static final String checksum = "checksum";
    
    /** label of the Urgent Pointer   Field -- 16 bits.
     */
    private static final String urgentPointer = "urgentPointer";
    
    /** defining the options field but not including to the RecordToken as of now
     *  8 bytes --
     */
    private static final String options =  "options";
    
    /** label of the timestamp that is transmitted within the RecordToken.
     */
    private static final String timestamp = "timestamp";

    /** label of the microstep that is transmitted within the RecordToken.
     */
    private static final String microstep = "microstep";

    /** label of the payload that is transmitted within the RecordToken.
     */
    private static final String payload = "payload";
    
    private static final String TCPlabel = "TCPlabel";
    
    private static final int max_packet_length = 5;
    

    // data tokens
    private static final String tokens = "tokens";

}
