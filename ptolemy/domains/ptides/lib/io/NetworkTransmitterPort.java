/* Network Transmitter port.

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



package ptolemy.domains.ptides.lib.io;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeLattice;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.graph.CPO;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  This port provides a specialized TypedIOPort for network transmitters
 *  used in Ptides. This port just specializes parameters.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class NetworkTransmitterPort extends PtidesPort {
 
    
    /** Create a new NetworkTransmitterPort with a given container and a name.
     * @param container The container of the port. 
     * @param name The name of the port.
     * @throws IllegalActionException If parameters cannot be set.
     * @throws NameDuplicationException If name already exists.
     */
    public NetworkTransmitterPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        this.setOutput(true);
        
        deviceDelay = new Parameter(this, "deviceDelay");
        deviceDelay.setToken(new DoubleToken(0.0));
        deviceDelay.setTypeEquals(BaseType.DOUBLE);
        
        deviceDelayBound = new Parameter(this, "deviceDelayBound");
        deviceDelayBound.setExpression("0.0");
        deviceDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        platformDelayBound = new Parameter(this, "platformDelayBound");
        platformDelayBound.setExpression("0.0");
        platformDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        admissionControlFunction = new StringParameter(this, "admissionControlFunction"); 
    }
    
    /** Return the custom shape for this port.
     *  @return List of coordinates representing the shape.
     */
    public List<Integer[]> getCoordinatesForShape() {
        List<Integer[]> coordinates = new ArrayList<Integer[]>();
        coordinates.add(new Integer[]{-8, 8});
        coordinates.add(new Integer[]{8, 8});
        coordinates.add(new Integer[]{8, -8});
        coordinates.add(new Integer[]{-8, -8});
        coordinates.add(new Integer[]{-8, -4});
        coordinates.add(new Integer[]{-12, -4});
        coordinates.add(new Integer[]{-12, 4});
        coordinates.add(new Integer[]{-8, 4});  
        return coordinates;
    }
    
    /** Device delay parameter that defaults to the double value 0.0. */
    public Parameter deviceDelay;
    
    /** Device delay bound parameter that defaults to the double value 0.0. */
    public Parameter deviceDelayBound;
    
    /** Platform delay bound parameter that defaults to the double value 0.0. */
    public Parameter platformDelayBound;
    
    /** Admission control function parameter that defaults to the double value 0.0. */
    public Parameter admissionControlFunction;
 
    /** Send new Recordtoken with timestamp, microstep and the original token
     *  as the payload to actors outside Ptides platforms.
     *  @param channelIndex Cannel to send token to.
     *  @param token RecordToken that is sent.
     *  @throws IllegalActionException If RecordToken cannot be created.
     *  @throws NoRoomException If Token cannot be sent.
     */
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException { 
        PtidesBasicDirector director = (PtidesBasicDirector) ((CompositeActor)getContainer()).getDirector();
        
        String[] labels = new String[] { timestamp, microstep, payload };
        Token[] values = new Token[] {
                new DoubleToken(director.getModelTime()
                        .getDoubleValue()),
                new IntToken(director.getMicrostep()), token };
        RecordToken record = new RecordToken(labels, values); 
        try {
        super.send(channelIndex, record);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(this, ex.getMessage());
        }
    }
    
    
    /** Override Type checking to compare type of payload with resolvedType. 
     *  FIXME: Is this right?
     *  @param token Token to be type-checked.
     */
    protected void _checkType(Token token) throws IllegalActionException {
        int compare = TypeLattice.compare((((RecordToken)token).get(payload)).getType(), _resolvedType);

        if ((compare == CPO.HIGHER) || (compare == CPO.INCOMPARABLE)) {
            throw new IllegalActionException(this,
                    "Run-time type checking failed. Token " + token
                            + " with type " + token.getType()
                            + " is incompatible with port type: "
                            + _resolvedType.toString());
        }  
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Label of the timestamp that is transmitted within the RecordToken.
     */
    private static final String timestamp = "timestamp";

    /** Label of the microstep that is transmitted within the RecordToken.
     */
    private static final String microstep = "microstep";

    /** Label of the payload that is transmitted within the RecordToken.
     */
    private static final String payload = "payload";
    
}
