/* An actor that assembles multiple inputs to an [Ordered]RecordToken.

 Copyright (c) 2009-2010 The Regents of the University of California.
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

import ptolemy.actor.IOPort;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// OrderedRecordAssembler

/**
 On each firing, read one token from each input port and assemble them
 into a RecordToken. The labels for the RecordToken are the names of the
 input ports.  To use this class, instantiate it, and then add input ports
 (instances of TypedIOPort).  This actor is polymorphic. The type constraint
 is that the type of each record field is no less than the type of the
 corresponding input port.

 @author Ben Leinfelder
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.actor.lib.RecordAssembler
 @see ptolemy.actor.lib.RecordDisassembler
 */
public class OrderedRecordAssembler extends RecordAssembler {
    /** Construct an OrderedRecordAssembler with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public OrderedRecordAssembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input port, assemble them into a RecordToken,
     *  and send the RecordToken to the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        Object[] portArray = inputPortList().toArray();
        int size = portArray.length;

        // construct the RecordToken and to output
        String[] labels = new String[size];
        Token[] values = new Token[size];

        for (int i = 0; i < size; i++) {
            IOPort port = (IOPort) portArray[i];
            labels[i] = port.getName();
            values[i] = port.get(0);
        }

        RecordToken result = new OrderedRecordToken(labels, values);

        output.send(0, result);
    }

}
