/* Code generator adapter class associated with the PtidesBasicDirector class.

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
package ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.domains.ptides.lib.luminary.GPInputHandler;
import ptolemy.domains.ptides.lib.luminary.LuminarySensorHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtidesPreemptiveEDFDirector

/**
 Code generator adapter associated with the PtidesPreemptiveEDFDirector class.
 This adapter generates Renesas specific code.

 FIXME: add renesas code
 @author Patricia Derler
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating red (derler)
 @Pt.AcceptedRating red (derler)
 */
public class PtidesPreemptiveEDFDirector
        extends
        ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel.PtidesPreemptiveEDFDirector {

    /** Construct the code generator adapter associated with the given
     *  PtidesBasicDirector.
     *  @param ptidesPreemptiveEDFDirector The associated director
     *  ptolemy.domains.ptides.kernel.PtidesBasicDirector
     */
    public PtidesPreemptiveEDFDirector(
            ptolemy.domains.ptides.kernel.PtidesPreemptiveEDFDirector ptidesPreemptiveEDFDirector) {
        super(ptidesPreemptiveEDFDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the assembly file associated for this PtidyOS program.
     *  @return The generated assembly file code.
     *  @exception IllegalActionException
     */
    public Map<String, StringBuffer> generateAdditionalCodeFiles() throws IllegalActionException {
        Map<String, StringBuffer> list = new HashMap();
        
        // FIXME add  assembly code
        return list;
    }

    /**
     * Generate the director fire code.
     * The code creates a new task for each actor according to
     * their specified parameters (e.g. stack depth, priority,
     * and etc.). The code also initiates the task scheduler.
     * @return The generated fire code.
     * @exception IllegalActionException Not thrown in this class.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(getCodeGenerator().comment("Create a task for each actor."));

        for (Actor actor : (List<Actor>) (((CompositeActor) getComponent()
                .getContainer()).deepEntityList())) {
            NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(actor);
            code.append(adapter.generateFireCode());
        }

        return code.toString();
    }

    /**
     * Generate the fire function code.
     * The code contains the function code for each actor. It is a collection
     * of global functions, one for each actor that is visible to this
     * director adapter. Creating each new task requires one of these
     * function as parameter. It is the code that the task executes.
     * When the inline parameter is checked, the task function code is
     * generated in {@link #generatePreinitializeCode()} which is
     * outside the main function.
     * @return The fire function code.
     * @exception IllegalActionException If there is an exception in
     *  generating the task function code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        return "";
    }

    /**
     * Generate the initialize code.
     * This generates the hardware initialization code and creates
     * the queues for all referrable port channels.
     * @return The generated initialize code.
     * @exception IllegalActionException If the adapter associated with
     *  an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(super.generateInitializeCode());
        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesBasicDirector) {
            return code.toString();
        }

        code.append(getCodeGenerator()
                .comment(
                        "Platform dependent initializatoin code of the PtidesDirector."));

        code.append(_templateParser.getCodeStream().getCodeBlock("initPDBlock"));

        return code.toString();
    }

    /** Generate the preinitialize code for the associated Ptides director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating preinitialize code for the actor.
     *   FIXME: Take care of platform dependent code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(super.generatePreinitializeCode());

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesBasicDirector) {
            return code.toString();
        }

        code.append(_templateParser.getCodeStream().getCodeBlock(
                "preinitPDBlock"));

        code.append(_templateParser.getCodeStream().getCodeBlock(
                "initPDCodeBlock"));

        code.append(_generateInitializeHardwareCode());

        return code.toString();
    }

    /**
     * Generate variable initialization for the referenced parameters.
     * This overrides the super class method and returns an empty
     * string. It avoids generating any offset variables.
     * @return code The empty string.
     * @exception IllegalActionException Not thrown in this class.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        return "";
    }

    /**
     * Generate the shared code. This is the first generate method invoked out
     * of all, so any initialization of variables of this adapter should be done
     * in this method. In this base class, return an empty set. Subclasses may
     * generate code for variable declaration, defining constants, etc.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesBasicDirector) {
            return sharedCode;
        }

        _modelStaticAnalysis();

        _templateParser.getCodeStream().clear();

        // define the number of actuators in the system as a macro.
        _templateParser.getCodeStream().append(
                "#define numActuators " + actuators.size() + _eol);

        _templateParser.getCodeStream().appendCodeBlocks("CommonTypeDefinitions");
        _templateParser.getCodeStream().appendCodeBlocks("StructDefBlock");
        _templateParser.getCodeStream().appendCodeBlocks("FuncProtoBlock");

        // prototypes for actor functions
        _templateParser.getCodeStream().append(_generateActorFuncProtoCode());

        // prototypes for actuator functions.
        _templateParser.getCodeStream().append(
                _generateActuatorActuationFuncArrayCode());

        // the only supported sensor inputs are GPIO inputs.
        List args = new LinkedList();
        for (Actor sensor : sensors.keySet()) {
            if (sensor instanceof GPInputHandler) {
                args.add("IntDisable(INT_GPIO"
                        + ((GPInputHandler) sensor).pad.stringValue() + ");"
                        + _eol);
            } else {
                args.add("IntDisable(INT_OTHER" + 
                        (sensor)+ ");" + _eol);
                //throw new IllegalActionException("Only GPIO inputs are supported " +
                //		"as sensors.");
            }
        }
        for (int i = 0; i < maxNumSensorInputs - sensors.size(); i++) {
            args.add("");
        }
        for (Actor sensor : sensors.keySet()) {
            if (sensor instanceof GPInputHandler) {
                args.add("IntEnable(INT_GPIO"
                        + ((GPInputHandler) sensor).pad.stringValue() + ");"
                        + _eol);
            } else {
                args.add("IntEnable(INT_OTHER" + 
                        (sensor) + ");" + _eol);
                //throw new IllegalActionException("Only GPIO inputs are supported " +
                  //              "as sensors.");

            }
        }
        for (int i = 0; i < maxNumSensorInputs - sensors.size(); i++) {
            args.add("");
        }
        _templateParser.getCodeStream()
                .append(_templateParser.getCodeStream().getCodeBlock(
                        "FuncBlock", args));

        if (!_templateParser.getCodeStream().isEmpty()) {
            sharedCode.add(processCode(_templateParser.getCodeStream()
                    .toString()));
        }

        return sharedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate the initialization code for any hardware component that is used.
     *  @return code initialization code for hardware peripherals
     *  @exception IllegalActionException
     */
    protected String _generateInitializeHardwareCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesBasicDirector) {
            return code.toString();
        }

        // FIXME: output initialization always needs to happen before input initialization.
        code.append("void initializeHardware() {" + _eol);
        for (Actor actor : actuators.keySet()) {
            code.append(((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.OutputDevice) getAdapter(actor))
                    .generateHardwareInitializationCode());
        }
        for (Actor actor : sensors.keySet()) {
            code.append(((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.InputDevice) getAdapter(actor))
                    .generateHardwareInitializationCode());
        }

        code.append("}" + _eol);
        return code.toString();
    }

    /** The maximum number of sensor inputs that is supported.
     */
    private static int maxNumSensorInputs = 8;
}
