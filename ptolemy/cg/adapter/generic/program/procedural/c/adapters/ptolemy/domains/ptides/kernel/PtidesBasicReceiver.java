/* Code generator adapter class associated with the PtidesBasicReceiver class.

 Copyright (c) 2009 The Regents of the University of California.
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

package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////////
////PtidesBasicReceiver

/** The adapter for ptides basic recevier.
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */
public class PtidesBasicReceiver extends ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.Receiver {

    /** Construct a ptides basic receiver.
     *  @param receiver The ptolemy.domains.ptides.kernel.PtidesBasicReceiver
     *  that corresponds with this adapter.
     *  @throws IllegalActionException If throw by the superclass.
     */
    public PtidesBasicReceiver (
            ptolemy.domains.ptides.kernel.PtidesBasicReceiver receiver) throws IllegalActionException {
        super(receiver);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     * Generate code for getting tokens from the receiver.
     * @param offset The offset in the array representation of the port.
     * @return generate get code.
     * @exception IllegalActionException If the receiver adapter is
     * not found or it encounters an error while generating the
     * get code.
     */
    public String generateGetCode(String offset) throws IllegalActionException {
        TypedIOPort port = (TypedIOPort) getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        return "Event_Head_" + getAdapter(port).getName() + "[" + channel + "]->Val."
                + port.getType().toString() + "_Value";
    }

    /** Generate code to check if the receiver has a token.
     *  @param offset The offset in the array representation of the port.
     *  @return generate hasToken code.
     *  @exception IllegalActionException If an error occurs when
     *  getting the receiver adapters or generating their initialize
     *  code.
     */
    public String generateHasTokenCode(String offset)
            throws IllegalActionException {
        IOPort port = getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        return "Event_Head_" + getAdapter(port).getName() + "[" + channel + "] != NULL";
    }

    /** 
     * Generate code for putting tokens from the receiver.
     * @param sourcePort The port for which to generate the send code.
     * @param offset The offset in the array representation of the port.
     * @param token The token to be sent.
     * @return generate type conversion as well as put code.
     * @exception IllegalActionException If the receiver adapter is
     * not found or it encounters an error while generating the send
     * code.
     */
    public String generatePutCode(IOPort sourcePort, String offset, String token)
            throws IllegalActionException {
        TypedIOPort sinkPort = (TypedIOPort) getComponent().getContainer();
        int sinkChannel = sinkPort.getChannelForReceiver(getComponent());

        Channel source = new Channel(sourcePort, 0);
        Channel sink = new Channel(sinkPort, sinkChannel);
        
        token = ((NamedProgramCodeGeneratorAdapter)getAdapter(
                getComponent().getContainer().getContainer())).getTemplateParser()
                .generateTypeConvertStatement(source, sink, 0, token);

        token = _removeSink(token);
        
        Actor actor = (Actor) sinkPort.getContainer();
        Director director = actor.getDirector();
        // Getting depth.
        String depth = Integer.toString(((CausalityInterfaceForComposites) director
                .getCausalityInterface()).getDepthOfActor(actor));
        // Getting deadline.
        Parameter relativeDeadline = (Parameter) sinkPort
                .getAttribute("relativeDeadline");
        String deadlineSecsString = null;
        String deadlineNsecsString = null;
        if (relativeDeadline != null) {
            double value = ((DoubleToken) relativeDeadline.getToken())
                    .doubleValue();
            int intPart = (int) value;
            int fracPart = (int) ((value - intPart) * 1000000000.0);
            deadlineSecsString = Integer.toString(intPart);
            deadlineNsecsString = Integer.toString(fracPart);
        } else {
            deadlineSecsString = new String("0");
            deadlineNsecsString = new String("0");
        }

        // Getting offsetTime.
        Parameter offsetTime = (Parameter) sinkPort.getAttribute("minDelay");
        String offsetSecsString = null;
        String offsetNsecsString = null;
        if (offsetTime != null) {
            double value = ((DoubleToken) ((ArrayToken) offsetTime
                    .getToken()).arrayValue()[sinkChannel]).doubleValue();
            int intPart = (int) value;
            int fracPart = (int) ((value - intPart) * 1000000000.0);
            offsetSecsString = Integer.toString(intPart);
            offsetNsecsString = Integer.toString(fracPart);
        } else {
            throw new IllegalActionException(sinkPort, "Cannot get the minDelay Parameter.");
        }

        // FIXME: not sure whether we should check if we are putting into an input port or
        // output port.
        // Generate a new event.
        String sinkName = NamedProgramCodeGeneratorAdapter
        .generateName((NamedObj)sinkPort.getContainer());
        List args = new ArrayList();
        args.add(sinkPort.getType().toString());
        args.add(token);
        args.add(sinkName);
        args.add("Event_Head_" + sinkName + "_" + sinkPort.getName() + "["
                + sinkPort.getChannelForReceiver(getComponent()) + "]");
        args.add(depth);//depth
        args.add(deadlineSecsString);//deadline
        args.add(deadlineNsecsString);
        args.add(offsetSecsString);//offsetTime
        args.add(offsetNsecsString);
        return _templateParser.generateBlockCode("createEvent", args);
    }

    /**
     * Generate the type conversion statement for the particular offset of
     * the two given channels. This assumes that the offset is the same for
     * both channel. Advancing the offset of one has to advance the offset of
     * the other.
     * @param source The given source channel.
     * @return The type convert statement for assigning the converted source
     *  variable to the sink variable with the given offset.
     * @exception IllegalActionException If there is a problem getting the
     * adapters for the ports or if the conversion cannot be handled.
     */
    protected String _generateTypeConvertStatement(Channel source)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }
}
