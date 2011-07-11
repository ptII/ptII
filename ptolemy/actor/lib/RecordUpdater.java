/* An actor that updates fields in a RecordToken.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// RecordUpdater

/**
 On each firing, read one token from each input port and assemble them
 into a RecordToken that contains the union of the original input record
 and each of the update ports.  To use this class, instantiate it, and
 then add input ports (instances of TypedIOPort).  This actor is polymorphic.
 The type constraint is that the output record contains all the labels in
 the input record plus the names of added input ports. The type of a field
 in the output is the same as the type of the added input port, if that field
 is updated by an added input port. If a field in the output is not updated
 by an input port, its type is the same as the corresponding field in the
 input record. For example, if the input record has type
 {item: string, value: int}, and this actor has two added input ports with
 name/type: value/double and id/int, then the output record will have type
 {item: string, value: double, id: int}

 @author Michael Shilman, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red (cxh)
 @see RecordAssembler
 */
public class RecordUpdater extends TypedAtomicActor {
    /** Construct a RecordUpdater with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RecordUpdater(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        input = new TypedIOPort(this, "input", true, false);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"0\" y=\"0\" width=\"6\" "
                + "height=\"40\" style=\"fill:red\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. Its type is constrained to be a RecordType. */
    public TypedIOPort output;

    /** The input port. Its type is constrained to be a RecordType. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        RecordUpdater newObject = (RecordUpdater) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.new FunctionTerm());
        return newObject;
    }

    /** Read one token from each input port, assemble them into a
     *  RecordToken that contains the union of the original input record
     *  and each of the update ports.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        // Pack a HashMap with all of the record entries from
        // the original record and all of the updating ports.
        HashMap outputMap = new HashMap();

        RecordToken record = (RecordToken) input.get(0);
        Set recordLabels = record.labelSet();

        for (Iterator i = recordLabels.iterator(); i.hasNext();) {
            String name = (String) i.next();
            Token value = record.get(name);
            outputMap.put(name, value);
        }

        List inputPorts = inputPortList();
        Iterator inputPortsIterator = inputPorts.iterator();

        while (inputPortsIterator.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPortsIterator.next();

            if (inputPort != input) {
                outputMap.put(inputPort.getName(), inputPort.get(0));
            }
        }

        // Construct a RecordToken and fill it with the values
        // in the HashMap.
        String[] labels = new String[outputMap.size()];
        Token[] values = new Token[outputMap.size()];

        int j = 0;

        for (Iterator i = outputMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            labels[j] = (String) entry.getKey();
            values[j] = (Token) entry.getValue();
            j++;
        }

        RecordToken result = new RecordToken(labels, values);
        output.send(0, result);
    }

    /** Return true if all input ports have tokens, false if some input
     *  ports do not have a token.
     *  @return True if all input ports have tokens.
     *  @exception IllegalActionException If the hasToken() call to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int)
     */
    public boolean prefire() throws IllegalActionException {
        Iterator ports = inputPortList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();

            if (!port.hasToken(0)) {
                return false;
            }
        }

        return true;
    }

    /** Return the type constraints of this actor. The type constraint is
     *  that the type of the output port is no less than the type of the
     *  input port, and contains additional fields for each input port.
     *  @return a list of Inequality.
     */
    public Set<Inequality> typeConstraints() {
        String[] labels = new String[0];
        Type[] types = new Type[0];

        RecordType declaredType = new RecordType(labels, types);
        input.setTypeAtMost(declaredType);

        // Set the constraints between record fields and output ports
        Set<Inequality> constraints = new HashSet<Inequality>();

        // Since the input port has a clone of the above RecordType, need to
        // get the type from the input port.
        Inequality inequality = new Inequality(new FunctionTerm(),
                output.getTypeTerm());
        constraints.add(inequality);

        return constraints;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the input port
    // types. The value of the function is a record type that contains
    // all the labels in the input record, plus the names of the added
    // input ports. The type of a field in the function value is the
    // same as the type of an added input port, if the label of that
    // field is the same as that input port, or, the type of a field
    // is the same as that of the corresponding field in the input
    // record.
    // To ensure that this function is monotonic, the value of the function
    // is bottom if the type of the port with name "input" is bottom. If
    // the type of this port is not bottom (it must be a record), the value
    // of the function is computed as described above.
    private class FunctionTerm extends MonotonicFunction {
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         */
        public Object getValue() {
            Type inputType = input.getType();

            if (!(inputType instanceof RecordType)) {
                return BaseType.UNKNOWN;
            }

            RecordType recordType = (RecordType) inputType;
            Map outputMap = new HashMap();
            Set recordLabels = recordType.labelSet();
            Iterator iterator = recordLabels.iterator();

            while (iterator.hasNext()) {
                String label = (String) iterator.next();
                Type type = recordType.get(label);
                outputMap.put(label, type);
            }

            List inputPorts = inputPortList();
            iterator = inputPorts.iterator();

            while (iterator.hasNext()) {
                TypedIOPort port = (TypedIOPort) iterator.next();

                if (port != input) {
                    outputMap.put(port.getName(), port.getType());
                }
            }

            // Construct the RecordType
            Object[] labelsObj = outputMap.keySet().toArray();
            String[] labels = new String[labelsObj.length];
            Type[] types = new Type[labelsObj.length];

            for (int i = 0; i < labels.length; i++) {
                labels[i] = (String) labelsObj[i];
                types[i] = (Type) outputMap.get(labels[i]);
            }

            return new RecordType(labels, types);
        }

        /** Return all the InequalityTerms for all input ports in an array.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            Iterator inputPorts = inputPortList().iterator();
            LinkedList result = new LinkedList();
            while (inputPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort) inputPorts.next();
                InequalityTerm term = port.getTypeTerm();
                if (term.isSettable()) {
                    result.add(term);
                }
            }
            InequalityTerm[] variables = new InequalityTerm[result.size()];
            Iterator results = result.iterator();
            int i = 0;
            while (results.hasNext()) {
                variables[i] = (InequalityTerm) results.next();
                i++;
            }
            return variables;
        }
    }
}
