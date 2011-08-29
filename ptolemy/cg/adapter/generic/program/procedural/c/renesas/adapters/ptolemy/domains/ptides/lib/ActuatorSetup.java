/* A code generation adapter class for ptolemy.domains.ptides.lib.targets.luminary.GPOutputDevice

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
package ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.domains.ptides.lib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.OutputDevice;
import ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.domains.ptides.kernel.RenesasUtilities;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter; 
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A code generation adapter class for ptolemy.domains.ptides.lib.targets.luminary.GPOutputDevice.
 * @author Jia Zou, Isaac Liu, Jeff C. Jensen
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */

public class ActuatorSetup extends OutputDevice {
    
    /** Construct an adapter with the given
     *  ptolemy.domains.ptides.lib.GPOutputDevice actor.
     *  @param actor The given ptolemy.domains.ptides.lib.targets.luminary.GPOutputDevice actor.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public ActuatorSetup(ptolemy.domains.ptides.lib.ActuatorSetup actor)
            throws IllegalActionException, NameDuplicationException {
        super(actor);  
        _number = ((IntToken) ((Parameter) actor
                .getAttribute("InterruptHandlerID")).getToken())
                .intValue();
        _letter = RenesasUtilities._getLetter(_number); 
        if (_letter == null) {
            throw new IllegalActionException(actor, "The interrupt handler number is not supported.");
        }
    }
    
    @Override
    public String generateFireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        List<String> args = new ArrayList<String>();
        args.add(_letter + ""); 
        codeStream.appendCodeBlock("fireBlock", args);
        return processCode(codeStream.toString());
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
 
    private int _number;
    private Character _letter;
}
