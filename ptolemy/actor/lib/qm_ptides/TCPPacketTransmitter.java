/* TCPPacketTransmitter simulates a device that builds variable-size
 * TCP packets that contain PTIDES events as the payload

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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import ptolemy.domains.ptides.lib.OutputDevice;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

/** Build a TCP Packet containing a user-defined number of PTIDES events.
 * 
 * 
 *  This actor should be directly connected to a network output port and be
 *  used with a PTIDES director.
 *  
 *  This actor is expected to be used in pairs with the {@link TCPPacketReceiver}.
 *  
 *  {@link TCPPacketTransmitter} builds TCP packets containing PTIDES events,
 *  where each PTIDES event itself is assigned a data length in bits by the user.
 *  The bits/event parameter is constant right now and is an actor Parameter.
 *  At the destination platform, {@link TCPPacketReceiver} consumes packages produced
 *  by this actor and releases PTIDES events into its enclosing director.
 *  
 *  </p><p>
 *  This actor consumes <i>frameSize</i> number of input tokens and creates a 
 *  RecordToken with two fields labeled as TCPlabel and tokens. Here, tokens itself is
 *  an array of RecordTokens, where each of the entries is a PTIDES RecordToken with
 *  labels: timestamp, microstep and payload. Once the number of received tokens equals
 *  frameSize, the RecordToken simulating the TCP Packet is sent to output port. During
 *  wrapup, the remaining tokens(if any) are sent to the output.
 *
 *  @author Ilge Akkaya
 *  @version 
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating 
 *  @Pt.AcceptedRating
*/
public class TCPPacketTransmitter extends OutputDevice {

    public TCPPacketTransmitter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.RECORD);
        
        frameSize = new PortParameter(this, "frameSize");
        frameSize.setExpression("5");
        frameSize.setTypeEquals(BaseType.INT);
        
        defaultFrameSize = new Parameter(this, "Default Frame Size");
        defaultFrameSize.setExpression("5");
        defaultFrameSize.setTypeEquals(BaseType.INT);
        _frameSize = 5;
        
        priority = new Parameter(this,"Packet Priority");
        priority.setExpression("1");
        priority.setTypeEquals(BaseType.INT);
        _priority = 1;
        
        _completePayload = new RecordToken();
        _tokenLabels = new ArrayList<String>();
        _tokenValues = new ArrayList<Token>();
        _packetLength = 0;
    }
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute == defaultFrameSize) {
            int value = ((IntToken) defaultFrameSize.getToken())
                .intValue();
            if (value <= 0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero frame size: " + value);
            }
        _frameSize = value;
        } else if (attribute == priority) {
            int value = ((IntToken)priority.getToken())
                .intValue();
                if (value <= 0) {
                    throw new IllegalActionException(this,
                            "Cannot have negative or zero frame size: " + value);
                }
                _priority = value;
        }
        
    super.attributeChanged(attribute);
    
   
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     public variables                 ////

    /* the data input port */
    public TypedIOPort input;

    /* the data output port */
    public TypedIOPort output;
    
    /* Default TCP Packet size parameter */
    public Parameter defaultFrameSize;
    
    /* Default TCP Packet priority parameter */
    public Parameter priority;
    
    /* User-Defined frame size port parameter*/
    public PortParameter frameSize;
    
    
    
    
    
    /** Fill-in and return fields of the TCP header as a RecordToken
     * 
     */
    public RecordToken getTCPHeader() throws IllegalActionException
    {
        String[] TCPHeaderLabels = new String[] {sourcePort, destinationPort, sequenceNumber, acknowledgementNumber,
                offsetControlBits, windowSize, checksum, urgentPointer,options}; 
        short sourcePortContents = 0;
        short destinationPortContents = 0;
        int sequenceNumberContents = 0;
        int ackNumberContents = 0;
        short offsetControlContents = 0;
        short windowSizeContents = 0;
        short checksumContents = 0;
        short urgentPointerContents = 0;
        // do it so for now. (should be short)
        int optionsContents =_priority;
        Token[] TCPHeaderValues = new Token[] {
                new IntToken(sourcePortContents),
                new IntToken(destinationPortContents),
                new IntToken(sequenceNumberContents),
                new IntToken(ackNumberContents),
                new IntToken(offsetControlContents),
                new IntToken(windowSizeContents),
                new IntToken(checksumContents),
                new IntToken(urgentPointerContents),
                new IntToken(optionsContents)
                };
        RecordToken TCPHeaderToken = new RecordToken(TCPHeaderLabels, TCPHeaderValues);
        return TCPHeaderToken;
    }
    
    /** Create a RecordToken with two labels: TCPlabel and tokens
     *  tokens is a token array that contains the input tokens consumed
     *  TCPlabel is the TCP Header structure containing the fields defined
     *  in <i>getTCPHeader()</i>
     *  Once the number of received tokens equals specified frame size, 
     *  an output packet is sent to the output port.
     *  @exception IllegalActionException Thrown in case of no director,
     *  input token cannot be read or output token cannot be
     *  sent.
     */
    public void fire() throws IllegalActionException {
        
        super.fire();
        Director director = getDirector();
        

        if (director == null || !(director instanceof PtidesBasicDirector)) {
            throw new IllegalActionException(this, "Director expected to"
                    + "be a Ptides director, but it's not.");
        }

        PtidesBasicDirector ptidesDirector = (PtidesBasicDirector) director;

        // add input tokens into the incompletePacket as long as packet length < MAX_SIZE
        if (input.hasToken(0)) {
            
            int _proposedFrameSize = _frameSize;
            
            // get a new token from frameSize port
            try {
                frameSize.update();
                
                 _proposedFrameSize = ((IntToken)frameSize.getToken()).intValue();
                 if(_proposedFrameSize > _packetLength)
                 {
                     //safe to apply frame size;
                     _frameSize = _proposedFrameSize;
                     
                 }
                 else
                 {
                     //cut the frame as it is.
                     _frameSize = _packetLength +1;
                     
                 }
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(this, ex,
                        "Should not be thrown because we have already "
                                + "verified that the tokens can be added");
            }

            /* construct new token to be transmitted with the current model time
               as its timestamp*/
            String[] labels = new String[] { timestamp, microstep, payload };
            Token[] values = new Token[] {
                    new DoubleToken(ptidesDirector.getModelTime()
                            .getDoubleValue()),
                    new IntToken(ptidesDirector.getMicrostep()),  input.get(0) };
            RecordToken record = new RecordToken(labels, values);
            
            // add the token into packet values List
            _tokenLabels.add(Integer.toString(_packetLength));
            _tokenValues.add(record);
            _packetLength ++;
            
            
            
            if( _packetLength >= _frameSize)
            {
                RecordToken TCPHeader = getTCPHeader();
                // form the packet that is ready to be sent
                _completePayload = new RecordToken((String[])(_tokenLabels.toArray(new String[0])), (Token[])(_tokenValues.toArray(new Token[0])));
                String[] fullTCPlabels = new String[]{TCPlabel, tokens};
                Token[] fullTCPvalues = new Token[]{ TCPHeader, _completePayload};
                
                RecordToken TCPFrame = new RecordToken(fullTCPlabels,fullTCPvalues);
                // create packet to be sent;
                output.send(0, TCPFrame);
                _packetLength = 0;
                // reset the incomplete packet contents
                _tokenLabels.clear();
                _tokenValues.clear();
                // set the new frame size, if possible
                if((_proposedFrameSize > 0) && (_proposedFrameSize < MAX_FRAME_SIZE))
                {
                    _frameSize = _proposedFrameSize;
                }
                else
                {
                    
                    // ignore proposed frame size.
                }
                
                
            }
            else
            {
                
                // keep saving.
            }
                
        }
        else
        {
            _packetLength = 0;
        }
    }
    

    /** Perform a check to see if this device is connected to a network
     *  port on the outside. If not, throw an exception. Also call
     *  preinitialize of the super class.
     *  @exception IllegalActionException If there are no outside sink
     *  ports, or if any of the outside sink ports is not a network
     *  port.
     */
    public void preinitialize() throws IllegalActionException {

        super.preinitialize();

        boolean flag = false;
        for (IOPort output : (List<IOPort>) outputPortList()) {
            for (IOPort sinkPort : (List<IOPort>) output.sinkPortList()) {
                if (sinkPort.getContainer() == getContainer()) {
                    flag = true;
                    break;
                }
            }
        }
        if (!flag) {
            throw new IllegalActionException(
                    this,
                    "A NetworkTransmitter must be connected to a port "
                            + "on the outside, and that port should be a network port "
                            + "(a port with the parameter networkPort).");
        }
    }

    /** Send the remaining tokens to the output port during wrapup.
     * These most likely will not be displayed but should not be left
     * within the actor, since they affect correct execution in the next
     * run
     */
    public void wrapup() throws IllegalActionException {
        
        // send last packet
        if( _packetLength > 0 ){
            _frameSize = _packetLength;
            
            RecordToken TCPHeader = getTCPHeader();
            // form the packet that is ready to be sent
            _completePayload = new RecordToken((String[])(_tokenLabels.toArray(new String[0])), (Token[])(_tokenValues.toArray(new Token[0])));
            String[] fullTCPlabels = new String[]{TCPlabel, tokens};
            Token[] fullTCPvalues = new Token[]{ TCPHeader, _completePayload};
            
            RecordToken TCPFrame = new RecordToken(fullTCPlabels,fullTCPvalues);
            // create packet to be sent;
            output.send(0, TCPFrame);
            _packetLength = 0;
            // reset the incomplete packet contents
            _tokenLabels.clear();
            _tokenValues.clear();
        }
        
    }
    
/*TCP Frame Field Labels */
    
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
    
    // data tokens
    private static final String tokens = "tokens";
    
    private static final String TCPpriority = "priority";
    
    

    /* The TCP Packet Token To be sent to the network fabric*/
    private RecordToken _completePayload;
    
    /* labels of the tokens to be included in the packet*/
    private List<String> _tokenLabels;
    
    /* values of the tokens to be included in the packet*/
    private List<Token> _tokenValues;
    
    /* current length of the packet*/
    private int _packetLength;
    
    /* frame size */
    private int _frameSize;
    
    /* assigned packet priority*/
    private int _priority;
    
    /* Limit on Maximum Frame Size*/
    private static final int MAX_FRAME_SIZE = 20;
    
    
}
