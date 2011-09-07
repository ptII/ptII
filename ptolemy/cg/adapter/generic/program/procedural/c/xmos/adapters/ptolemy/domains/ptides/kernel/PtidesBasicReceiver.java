/* Code generator adapter class associated with the PtidesBasicReceiver class.

 Copyright (c) 2009-2011 The Regents of the University of California.
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

package ptolemy.cg.adapter.generic.program.procedural.c.xmos.adapters.ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter.Channel;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////PtidesBasicReceiver

/** The adapter for ptides basic recevier.
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */
public class PtidesBasicReceiver
        extends
        ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel.PtidesBasicReceiver {

    /** Construct a ptides basic receiver.
     *  @param receiver The ptolemy.domains.ptides.kernel.PtidesBasicReceiver
     *  that corresponds with this adapter.
     *  @exception IllegalActionException If throw by the superclass.
     */
    public PtidesBasicReceiver(
            ptolemy.domains.ptides.kernel.PtidesBasicReceiver receiver)
            throws IllegalActionException {
        super(receiver);
    }
    
    
    
    public String generatePutCode(IOPort sourcePort, String offset, String token)
            throws IllegalActionException {
        TypedIOPort sinkPort = (TypedIOPort) getComponent().getContainer();
        
        if (sinkPort.isOutput()) {
            StringBuffer code = new StringBuffer();
            // This should only occur if the we have an actor governed
            // by a Ptides director inside of another actor, which is also
            // governed by a Ptides director. In this case, both this receiver,
            // and also the receiver on the outside of this receiver are
            // Ptides directors. Thus, we get the receivers
            // that are sinks of this receiver, and put tokens to those
            // receiver. Note a better place for this code may be at the port
            // level, but different kinds of receivers have different semantics
            // on how to generate put code, so it's not necessarily true that
            // we should put to the sink receivers (e.g., for SDF receivers,
            // we indeed want to put to this receiver, and generate transfer
            // output code to transfer tokens to the outside.
            ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort portAdapter = (ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort) getAdapter(sinkPort);
            for (int channel = 0; channel < sinkPort.getWidth(); channel++) {
                code.append(portAdapter.generatePutCode(
                        Integer.toString(channel), offset, token));
            }
            return code.toString();
        }
        
        int sinkChannel = sinkPort.getChannelForReceiver(getComponent());
        
        Channel source = new Channel(sourcePort, 0);
        Channel sink = new Channel(sinkPort, sinkChannel);
        
        token = ((NamedProgramCodeGeneratorAdapter) getAdapter(getComponent()
                .getContainer().getContainer())).getTemplateParser()
                .generateTypeConvertStatement(source, sink, 0, token);
        
        token = _removeSink(token);
        
        Actor actor = (Actor) sinkPort.getContainer();
        Director director = actor.getDirector(); 
        String depth = Integer
                .toString(((CausalityInterfaceForComposites) director
                        .getCausalityInterface()).getDepthOfActor(actor)); 
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
            //deadlineSecsString = "0";
            //deadlineNsecsString = "0";
            // deadline param should always be here
            throw new IllegalActionException(sinkPort,
            "Cannot get the deadline Parameter.");
        }
        
        // Getting offsetTime.
        Parameter offsetTime = (Parameter) sinkPort.getAttribute("delayOffset");
        String offsetSecsString = null;
        String offsetNsecsString = null;
        if (offsetTime != null) {
            double value = ((DoubleToken) ((ArrayToken) offsetTime.getToken())
                    .arrayValue()[sinkChannel]).doubleValue();
            int intPart = (int) value;
            int fracPart = (int) ((value - intPart) * 1000000000.0);
            offsetSecsString = Integer.toString(intPart);
            offsetNsecsString = Integer.toString(fracPart);
        } else {
            throw new IllegalActionException(sinkPort,
                    "Cannot get the delayOffset Parameter.");
        }
        
        String addTime, sourceTime;  
        
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(sourcePort.getContainer());
        addTime = adapter.getAddTimeString();
        sourceTime = adapter.getSourceTimeString("sourceTime");
        if (sourceTime == "") { // use default input output actor 
            sourceTime = "sourceTime = Event_Head_" + CodeGeneratorAdapter.generateName(sourcePort.getContainer()) + "_" + adapter.getTimeSourcePortName() + "[0]";
        }
        
        // FIXME: not sure whether we should check if we are putting into an input port or
        // output port.
        // Generate a new event.
        String sinkName = CodeGeneratorAdapter.generateName(sinkPort
                .getContainer());
        List<String> args = new ArrayList<String>();
        
        args.add(sourceTime);
        args.add(addTime);
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
    
}
