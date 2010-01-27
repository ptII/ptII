/* NetworkInputDevice simulates a hardware device that reads data from the network.

@Copyright (c) 2008-2009 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////NetworkInputDevice

/**
 *  <p>
 *  Note this actor (or some other subclass of this class) should
 *  be directly connected to a network input port in a PtidesBasicDirector.
 *  <\p>
 *  <p>
 *  Unlike SensorReceiver for example, this actor is necessarily needed for
 *  both simulation and code generation purposes.
 *  <\p>
 *  <p>
 *  This actor assumes the incoming token is a RecordToken, and includes a
 *  token value as well as a timestamp associated with the token value. Thus
 *  this actor parses the RecordToken and sends the output token with the
 *  timestamp equal to the timestamp stored in the RecordToken.
 *  In other words, we assume the RecordToken has these three labels: timestamp,
 *  microstep, and payload.
 *
 *  @author Jia Zou, Slobodan Matic
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating
 */
public class NetworkInputDevice extends InputDevice {

    /**
     * Constructs a NetworkInputDevice object.
     *
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException if the super constructor throws it.
     * @exception NameDuplicationException if the super constructor throws it.
     */
    public NetworkInputDevice(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port. By default, the type of this port is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;

    /** label of the timestamp that's transmitterd within the RecordToken.
     */
    private static final String timestamp = "timestamp";

    /** label of the microstep that's transmitterd within the RecordToken.
     */
    private static final String microstep = "microstep";

    /** label of the payload that's transmitterd within the RecordToken.
     */
    private static final String payload = "payload";

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////
    /** Parses the input RecordToken and produces an output token of a timestamp
     *  equal to the timestamp specified within the RecordToken.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent. Or, if the
     *  record has size != 2.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null || !(director instanceof PtidesBasicDirector)) {
            throw new IllegalActionException(this, "Director not recognizable!");
        }

        PtidesBasicDirector ptidesDirector = (PtidesBasicDirector) director;

        // consume input
        if (input.hasToken(0)) {
            RecordToken record = (RecordToken) input.get(0);

            if (record.labelSet().size() != 3) {
                throw new IllegalActionException(
                        "the record has a size not equal to 3: "
                                + "Here we assume the Record is of types: timestamp"
                                + " + microstep + token");
            }

            Time recordTimeStamp = new Time(getDirector(),
                    ((DoubleToken) (record.get(timestamp))).doubleValue());

            int recordMicrostep = ((IntToken) (record.get(microstep)))
                    .intValue();

            // The NetworkInputDevice parses the incoming token from
            // the network, which is a 3 element RecordToken, and
            // produces an event of the token value equal to the payload,
            // and tag equal to the tag as stored in the RecordToken.
            Time lastModelTime = ptidesDirector.getModelTime();
            int lastMicrostep = ptidesDirector.getMicrostep();
            ptidesDirector.setTag(recordTimeStamp, recordMicrostep);
            output.send(0, record.get(payload));
            ptidesDirector.setTag(lastModelTime, lastMicrostep);
        }
    }

    /** Return the type constraints of this actor. The type constraint is
     *  that the input RecordToken has two fields, a "timestamp" of type
     *  double and a "tokenValue" of type same as the output type.
     *  @return a list of Inequality.
     */
    public Set<Inequality> typeConstraints() {
        String[] labels = { timestamp, microstep, payload };
        Type[] types = { BaseType.DOUBLE, BaseType.INT, BaseType.GENERAL };
        RecordType type = new RecordType(labels, types);
        input.setTypeAtMost(type);

        HashSet typeConstraints = new HashSet<Inequality>();
        Inequality inequality = new Inequality(new PortFunction(), output
                .getTypeTerm());
        typeConstraints.add(inequality);
        return typeConstraints;
    }

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
                throw new IllegalActionException(NetworkInputDevice.this,
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
}
