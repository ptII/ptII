/* A code generation adapter class for ptolemy.domains.ptides.lib.targets.luminary.GPInputDevice

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
package ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.ptides.lib.luminary;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.InputDevice;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A code generation adapter class for ptolemy.domains.ptides.lib.targets.luminary.GPInputDevice.
 * @author Jia Zou, Isaac Liu, Jeff C. Jensen
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */

public class GPInputDevice extends InputDevice {
    /** Construct an adapter with the given
     *  ptolemy.domains.ptides.lib.GPInputDevice actor.
     *  @param actor The given ptolemy.domains.ptides.lib.targets.luminary.GPInputDevice actor.
     *  @exception IllegalActionException 
     * @exception NameDuplicationException 
     */
    public GPInputDevice(ptolemy.domains.ptides.lib.luminary.GPInputDevice actor)
            throws IllegalActionException, NameDuplicationException {
        super(actor);

        Parameter pinParameter = actor.pin;
        StringParameter padParameter = actor.pad;
        _pinID = null;
        _padID = null;

        if (pinParameter != null) {
            _pinID = ((IntToken) pinParameter.getToken()).toString();
        } else {
            throw new IllegalActionException(
                    "does not know to which pin this output device is associated.");
        }
        if (padParameter != null) {
            _padID = padParameter.stringValue();
        } else {
            throw new IllegalActionException(
                    "does not know to which pin this output device is associated.");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the code for the sensing function.
     * @return the code for the sensing function, which is read from the
     * "sensingBlock" 
     * @exception IllegalActionException If thrown while appending the code
     * block or processing the code stream.
     */
    public String generateSensorSensingFuncCode() throws IllegalActionException {
        List args = new LinkedList();
        CodeStream _codeStream = _templateParser.getCodeStream();

        args.add(NamedProgramCodeGeneratorAdapter.generateName(getComponent()));
        args.add(_padID);
        args.add(_pinID);

        _codeStream.clear();
        _codeStream.appendCodeBlock("sensingBlock", args);

        return processCode(_codeStream.toString());
    }

    /**
     * Return the code for the hardware initialization function.
     * @return the code for the hardare initialization function.
     * @exception IllegalActionException If thrown while appending the code
     * block or processing the code stream.
     */
    public String generateHardwareInitializationCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        List args = new ArrayList();
        args.add(_padID);
        args.add(_pinID);
        code.append(processCode(_templateParser.getCodeStream().getCodeBlock(
                "initializeGPInput", args)));
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _pinID;
    private String _padID;
}
