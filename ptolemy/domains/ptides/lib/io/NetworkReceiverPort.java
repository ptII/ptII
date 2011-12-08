/* Network receiver port.

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;  
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  This port provides a specialized TypedIOPort for network receivers
 *  used in Ptides. This port just specializes parameters.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class NetworkReceiverPort extends PtidesPort {
    
    /** Create a new NetworkReceiverPort with a given container and a name.
     * @param container The container of the port. 
     * @param name The name of the port.
     * @throws IllegalActionException If parameters cannot be set.
     * @throws NameDuplicationException If name already exists.
     */
    public NetworkReceiverPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        this.setInput(true);  
        
        deviceDelay = new Parameter(this, "deviceDelay");
        deviceDelay.setToken(new DoubleToken(0.0));
        deviceDelay.setTypeEquals(BaseType.DOUBLE);
        
        deviceDelayBound = new Parameter(this, "deviceDelayBound");
        deviceDelayBound.setExpression("0.0");
        deviceDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        sourcePlatformDelayBound = new Parameter(this, "sourcePlatformDelayBound");
        sourcePlatformDelayBound.setExpression("0.0");
        sourcePlatformDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        networkDelayBound = new Parameter(this, "networkDelayBound");
        networkDelayBound.setExpression("0.0");
        networkDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
    }
    
    /** Return the custom shape for this port.
     *  @return List of coordinates representing the shape.
     */
    public List<Integer[]> getCoordinatesForShape() {
        List<Integer[]> coordinates = new ArrayList<Integer[]>();
        coordinates.add(new Integer[]{-8, 8});
        coordinates.add(new Integer[]{8, 8});
        coordinates.add(new Integer[]{8, 4});
        coordinates.add(new Integer[]{12, 4});
        coordinates.add(new Integer[]{12, -4});
        coordinates.add(new Integer[]{8, -4});
        coordinates.add(new Integer[]{8, -8}); 
        coordinates.add(new Integer[]{-8, -8});
        return coordinates;
    }
    
    /** Device delay parameter that defaults to the double value 0.0. */
    public Parameter deviceDelay;
    
    /** Device delay bound parameter that defaults to the double value 0.0. */
    public Parameter deviceDelayBound;
    
    /** Network delay bound parameter that defaults to the double value 0.0. */
    public Parameter networkDelayBound; 
    
    /** Source platform delay bound parameter that defaults to the double value 0.0. */
    public Parameter sourcePlatformDelayBound; 
    
     
    /** Send Token inside. Tokens received on this port are recordTokens. Only the
     *  payload of the RecordToken should be sent inside. 
     *  @param channelIndex Channel token is sent to.
     *  @param token Token to be sent.
     *  @throws IllegalActionException If received token is not a record token 
     *  with the fields timestamp, microstep and payload.
     */
    public void sendInside(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        PtidesBasicDirector director = (PtidesBasicDirector) ((CompositeActor)getContainer()).getDirector();

        if (!(token instanceof RecordToken) || ((RecordToken)token).labelSet().size() != 3) {
            throw new IllegalActionException(this, 
                    "The input token is not a RecordToken or " +
                    "does not have a size not equal to 3: "
                            + "Here we assume the Record is of types: timestamp"
                            + " + microstep + token");
        }
        
        RecordToken record = (RecordToken) token;

        Time recordTimeStamp = new Time(director,
                ((DoubleToken) (record.get(timestamp))).doubleValue());

        int recordMicrostep = ((IntToken) (record.get(microstep)))
                .intValue(); 
        
        Time lastModelTime = director.getModelTime();
        int lastMicrostep = director.getMicrostep();
        director.setTag(recordTimeStamp, recordMicrostep);
        
        director.setTag(lastModelTime, lastMicrostep);
        director.setModelTime(recordTimeStamp);
        super.sendInside(channelIndex, record.get(payload));
    }
    
    /** Override conversion such that only payload of recordtoken is
     *  converted. 
     *  FIXME: Is this enough?.
     *  @param token Token to be converted.
     *  @throws IllegalActionException If payload token cannot be converted.
     */
    public Token convert(Token token) throws IllegalActionException { 
        Type type = getType();
        if (type.equals((((RecordToken)token).get(payload)).getType())) {
            return token;
        } else {
            Token newToken = type.convert(((RecordToken)token).get(payload));
            String[] labels = new String[] { timestamp, microstep, payload };
            Token[] values = new Token[] {
                    (((RecordToken)token).get(timestamp)),
                    (((RecordToken)token).get(microstep)), newToken };
            RecordToken record = new RecordToken(labels, values); 
            return record;
        }
    }
    
    /** Return the type constraints on all connections starting from the
     *  specified source port to all the ports in a group of destination
     *  ports. This overrides the base class to ensure that if the source
     *  port or the destination port is a port of this composite, then
     *  the port is forced to be an array type and the proper constraint
     *  on the element type of the array is made. If the source port
     *  has no possible sources of data, then no type constraints are
     *  added for it.
     *  @param sourcePort The source port.
     *  @param destinationPortList The destination port list.
     *  @return A list of instances of Inequality.
     */
    protected List _typeConstraintsFromTo(TypedIOPort sourcePort,
            List destinationPortList) {
        List result = new LinkedList();

        boolean srcUndeclared = sourcePort.getTypeTerm().isSettable();
        Iterator destinationPorts = destinationPortList.iterator();

        while (destinationPorts.hasNext()) {
            TypedIOPort destinationPort = (TypedIOPort) destinationPorts.next();
            boolean destUndeclared = destinationPort.getTypeTerm().isSettable();

            if (srcUndeclared || destUndeclared) {
                // At least one of the source/destination ports does
                // not have declared type, form type constraint.
                if ((sourcePort.getContainer() == this)
                        && (destinationPort.getContainer() == this)) {
                    // Both ports belong to this, so their type must be equal.
                    // Represent this with two inequalities.
                    Inequality ineq1 = new Inequality(sourcePort.getTypeTerm(),
                            destinationPort.getTypeTerm());
                    result.add(ineq1);

                    Inequality ineq2 = new Inequality(
                            destinationPort.getTypeTerm(),
                            sourcePort.getTypeTerm());
                    result.add(ineq2);
                } else if (sourcePort.getContainer().equals(this)) {
                    if (sourcePort.sourcePortList().size() == 0) {
                        // Skip this port. It is not connected on the outside.
                        continue;
                    }

                    // Require the source port to be an array.
                    Inequality arrayInequality = new Inequality(
                            RecordType.EMPTY_RECORD.getTypeTerm(payload), sourcePort.getTypeTerm());
                    result.add(arrayInequality);

                    Inequality ineq = new Inequality(
                            RecordType.EMPTY_RECORD.getTypeTerm(payload),
                            destinationPort.getTypeTerm());
                    result.add(ineq);
                } else if (destinationPort.getContainer().equals(this)) {
                    Inequality ineq = new Inequality(
                            RecordType.EMPTY_RECORD.getTypeTerm(payload),
                            destinationPort.getTypeTerm());
                    result.add(ineq);
                }
            }
        }

        return result;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////



    /** Label of the timestamp that is transmitted within the RecordToken.
     */
    private static final String timestamp = "timestamp";

    /** Label of the microstep that is transmitted within the RecordToken.
     */
    private static final String microstep = "microstep";

    /** Label of the payload that's transmitted within the RecordToken.
     */
    private static final String payload = "payload";
    
}
