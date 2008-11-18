/* Code generator helper for typed composite actor.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.codegen.kernel.Director;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator helper for typed composite actor.

 @author Gang Zhou, Contributors: Teale Fristoe
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends CCodeGeneratorHelper {
    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    /**
     * For each actor in this typed composite actor, determine which ports
     * need type conversion.
     * @exception IllegalActionException If any of the helpers of the
     * inside actors is unavailable.
     * @see ptolemy.codegen.kernel.CodeGeneratorHelper#analyzeTypeConvert
     */
    public void analyzeTypeConvert() throws IllegalActionException {
        super.analyzeTypeConvert();
        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            try {
                CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                helperObject.analyzeTypeConvert();
            } catch (Throwable throwable) {
                throw new IllegalActionException(actor, throwable,
                        "Failed to determine which ports need type conversion.");
            }
        }
    }

    /** Create read and write offset variables if needed for the associated
     *  composite actor. It delegates to the director helper of the local
     *  director.
     *  @return A string containing declared read and write offset variables.
     *  @exception IllegalActionException If the helper class cannot be found
     *   or the director helper throws it.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        return directorHelper.createOffsetVariablesIfNeeded();
    }

    /** Generate the fire code of the associated composite actor. This method
     *  first generates code for transferring any data from the input
     *  ports of this composite to the ports connected on the inside
     *  by calling the generateTransferInputsCode() method of the
     *  local director helper. It then invokes the generateFireCode()
     *  method of its local director helper.  After the
     *  generateFireCode() method of the director helper returns,
     *  generate code for transferring any output data created by
     *  calling the local director helper's
     *  generateTransferOutputsCode() method.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the helper associated
     *  with an actor throws it while generating fire code for the
     *  actor, or the director helper throws it while generating code
     *  for transferring data.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_codeGenerator.comment(2,
                        "Fire Composite "
                        + getComponent().getName()));

        code.append(super.generateFireCode());

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        Iterator inputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .inputPortList().iterator();

        // Update port parameters.
        StringBuffer tempCode = new StringBuffer();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (inputPort instanceof ParameterPort && inputPort.isOutsideConnected()) {

                PortParameter portParameter = ((ParameterPort) inputPort)
                        .getParameter();
                tempCode.append(CodeStream.indent(_codeGenerator
                        .generateVariableName(portParameter)));
                // FIXME: The = sign is language specific.
                tempCode.append(" = ");
                tempCode.append(getReference(inputPort.getName()));
                tempCode.append(";" + _eol);
            }
        }
        if (tempCode.length() > 0) {
            code.append(CodeStream.indent(_codeGenerator.comment("Update "
                    + getComponent().getName() + "'s port parameters")));
            code.append(tempCode);
        }

        // Transfer the data to the inside.
        inputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (!(inputPort instanceof ParameterPort)) {
                directorHelper.generateTransferInputsCode(inputPort, code);
                //generateTransferInputsCode(inputPort, code);
            }
        }

        // Generate the fire code by the director helper.
        code.append(directorHelper.generateFireCode());

        // Transfer the data to the outside.
        Iterator outputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            directorHelper.generateTransferOutputsCode(outputPort, code);
            //generateTransferOutputsCode(outputPort, code);
        }
        return processCode(code.toString());
    }

//    private void generateTransferOutputsCode(IOPort outputPort, StringBuffer code) {
//        // TODO Auto-generated method stub
//        
//    }

//    private void generateTransferInputsCode(IOPort inputPort, StringBuffer code) throws IllegalActionException {
//        int rate = DFUtilities.getTokenConsumptionRate(inputPort);
//        boolean targetCpp = ((BooleanToken) _codeGenerator.generateCpp
//                .getToken()).booleanValue();
//
//        CompositeActor actor = (CompositeActor) getComponent();
//        if (actor instanceof CompiledCompositeActor
//                && ((BooleanToken) _codeGenerator.generateJNI.getToken())
//                        .booleanValue()) {
//
//            // FindBugs wants this instanceof check.
//            if (!(inputPort instanceof TypedIOPort)) {
//                throw new InternalErrorException(inputPort, null,
//                        " is not an instance of TypedIOPort.");
//            }
//            Type type = ((TypedIOPort) inputPort).getType();
//            String portName = inputPort.getName();
//
//            for (int i = 0; i < inputPort.getWidth(); i++) {
//                if (i < inputPort.getWidthInside()) {
//
//                    String tokensFromOneChannel = "tokensFromOneChannelOf"
//                            + portName + i;
//                    String pointerToTokensFromOneChannel = "pointerTo"
//                            + tokensFromOneChannel;
//
//                    code.append("jobject "
//                            + tokensFromOneChannel
//                            + " = "
//                            + CCodegenUtilities.jniGetObjectArrayElement(
//                                    portName, String.valueOf(i), targetCpp)
//                            + ";" + _eol);
//
//                    if (type == BaseType.INT) {
//                        code.append("jint * "
//                                + pointerToTokensFromOneChannel
//                                + " = "
//                                + CCodegenUtilities.jniGetArrayElements("Int",
//                                        tokensFromOneChannel, targetCpp) + ";"
//                                + _eol);
//                    } else if (type == BaseType.DOUBLE) {
//                        code.append("jdouble * "
//                                + pointerToTokensFromOneChannel
//                                + " = "
//                                + CCodegenUtilities.jniGetArrayElements(
//                                        "Double", tokensFromOneChannel,
//                                        targetCpp) + ";" + _eol);
//                    } else if (type == PointerToken.POINTER) {
//                        code.append("jint * "
//                                + pointerToTokensFromOneChannel
//                                + " = "
//                                + CCodegenUtilities.jniGetArrayElements("Int",
//                                        tokensFromOneChannel, targetCpp) + ";"
//                                + _eol);
//                    } else if (type == BaseType.BOOLEAN) {
//                        code.append("jboolean * "
//                                + pointerToTokensFromOneChannel
//                                + " = "
//                                + CCodegenUtilities.jniGetArrayElements(
//                                        "Boolean", tokensFromOneChannel,
//                                        targetCpp) + ";" + _eol);
//                    } else {
//                        // FIXME: need to deal with other types
//                    }
//                    String portNameWithChannelNumber = portName;
//                    if (inputPort.isMultiport()) {
//                        portNameWithChannelNumber = portName + '#' + i;
//                    }
//                    for (int k = 0; k < rate; k++) {
//
//                        code.append(getReference("@"
//                                + portNameWithChannelNumber + "," + k));
//                        if (type == PointerToken.POINTER) {
//                            code.append(" = (void *) "
//                                    + pointerToTokensFromOneChannel + "[" + k
//                                    + "];" + _eol);
//                        } else {
//                            code.append(" = " + pointerToTokensFromOneChannel
//                                    + "[" + k + "];" + _eol);
//                        }
//                    }
//
//                    if (type == BaseType.INT) {
//                        code.append(CCodegenUtilities.jniReleaseArrayElements(
//                                "Int", tokensFromOneChannel,
//                                pointerToTokensFromOneChannel, targetCpp)
//                                + ";" + _eol);
//                    } else if (type == BaseType.DOUBLE) {
//                        code.append(CCodegenUtilities.jniReleaseArrayElements(
//                                "Double", tokensFromOneChannel,
//                                pointerToTokensFromOneChannel, targetCpp)
//                                + ";" + _eol);
//                    } else if (type == PointerToken.POINTER) {
//                        code.append(CCodegenUtilities.jniReleaseArrayElements(
//                                "Int", tokensFromOneChannel,
//                                pointerToTokensFromOneChannel, targetCpp)
//                                + ";" + _eol);
//                    } else if (type == BaseType.BOOLEAN) {
//                        code.append(CCodegenUtilities.jniReleaseArrayElements(
//                                "Boolean", tokensFromOneChannel,
//                                pointerToTokensFromOneChannel, targetCpp)
//                                + ";" + _eol);
//                    } else {
//                        // FIXME: need to deal with other types
//                    }
//                }
//            }
//        } else {
//            Director outsideDirector = (Director) _getHelper(actor.getExecutiveDirector());
//            Director insideDirector = (Director) _getHelper(actor.getDirector());
//            
//            for (int i = 0; i < inputPort.getWidth(); i++) {
//                if (i < inputPort.getWidthInside()) {
//                    String name = inputPort.getName();
//
//                    if (inputPort.isMultiport()) {
//                        name = name + '#' + i;
//                    }
//
//                    for (int k = 0; k < rate; k++) {
//                        //code.append(insideDirector.getReference("@" + name + "," + k, false, this));
//                        code.append(insideDirector.getReference((TypedIOPort) inputPort, new String[]{"" + i, "" + k}, true, false, this));
//                        code.append(" = " + _eol);
//                        //code.append(outsideDirector.getReference(name + "," + k, false, this));
//                        code.append(outsideDirector.getReference((TypedIOPort) inputPort, new String[]{"" + i, "" + k}, false, false, this));
//                        code.append(";" + _eol);
//                    }
//                }
//            }
//        }
//
//        // Generate the type conversion code before fire code.
//        code.append(generateTypeConvertFireCode(true));
//    }

    public String generateGlobalCode() throws IllegalActionException {
        CompositeActor compositeActor = (CompositeActor) getComponent();
        ptolemy.actor.Director director = compositeActor.getDirector();
        Director directorHelper = (Director) _getHelper(director);

        return directorHelper.generateGlobalCode();
    }
    
    /** Generate The fire function code. This method is called when
     *  the firing code of each actor is not inlined. Each actor's
     *  firing code is in a function with the same name as that of the
     *  actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeActor compositeActor = (CompositeActor) getComponent();
        ptolemy.actor.Director director = compositeActor.getDirector();
        Director directorHelper = (Director) _getHelper(director);
        code.append(directorHelper.generateFireFunctionCode());
        if (!(compositeActor instanceof ptolemy.actor.lib.jni.CompiledCompositeActor && ((BooleanToken) _codeGenerator.generateJNI
                .getToken()).booleanValue())) {
            code.append(super.generateFireFunctionCode());
        }
        return processCode(code.toString());
    }

    /** Generate the initialize code of the associated composite actor. It
     *  first resets the read and write offset of all input ports of all
     *  contained actors and all output ports. It then gets the result of
     *  generateInitializeCode() method of the local director helper.
     *
     *  @return The initialize code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor
     *   or while resetting read and write offset.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer initializeCode = new StringBuffer();
        //initializeCode.append(_codeGenerator.comment(1,
        //        "Initialize composite "
        //        + getComponent().getName()));

        //initializeCode.append(super.generateInitializeCode());

        // Reset the offset for all of the contained actors' input ports.
        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(actor);
            String code = actorHelper.resetInputPortsOffset();
            if (code.length() > 0) {
                initializeCode.append(_eol
                        + _codeGenerator.comment(1, actor.getName()
                                + "'s input offset initialization"));
                initializeCode.append(code);
            }
        }

        // Reset the offset for all of the output ports.
        String code = resetOutputPortsOffset();
        if (code.length() > 0) {
            initializeCode.append(_eol
                    + _codeGenerator.comment(
                            getComponent().getName()
                                    + "'s output offset initialization"));
            initializeCode.append(code);
        }

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        // Generate the initialize code by the director helper.
        initializeCode.append(directorHelper.generateInitializeCode());

        return processCode(initializeCode.toString());
    }

    /** Generate mode transition code. It delegates to the director helper
     *  of the local director. The mode transition code generated in this
     *  method is executed after each global iteration, e.g., in HDF model.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the director helper throws it
     *   while generating mode transition code.
     */
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException {
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        directorHelper.generateModeTransitionCode(code);
    }

    /** Generate the postfire code of the associated composite
     *  actor. It returns the result of generatePostfireCode() method
     *  of the local director helper.
     *
     *  @return The postfire code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating postfire code for the actor.
     */
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append(super.generatePostfireCode());
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        code.append(directorHelper.generatePostfireCode());
        return processCode(code.toString());
    }

    /** Generate the preinitialize code of the associated composite actor.
     *  It first creates buffer size and offset map for its input ports and
     *  output ports. It then gets the result of generatePreinitializeCode()
     *  method of the local director helper.
     *
     *  @return The preinitialize code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor
     *   or while creating buffer size and offset map.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        code.append(directorHelper.generatePreinitializeCode());

        return processCode(code.toString());
    }

    /** Generate variable declarations for input ports, output ports and
     *  parameters if necessary, as well as for the director and the
     *  contained actors.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating variable declarations for
     *   the actor.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(super.generateVariableDeclaration());

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        code.append(directorHelper.generateVariableDeclaration());

        return processCode(code.toString());
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating variable declarations for
     *   the actor.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        // code.append(_eol + _codeGenerator.comment(1, "Composite actor "
        //                + getComponent().getName()
        //                + "'s variable initialization."));

        code.append(super.generateVariableInitialization());

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        code.append(directorHelper.generateVariableInitialization());

        return processCode(code.toString());
    }

    /** Generate the wrapup code of the associated composite actor. It returns
     *  the result of generateWrapupCode() method of the local director helper.
     *
     *  @return The wrapup code of the associated composite actor.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating wrapup code for the actor.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append(super.generateWrapupCode());
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        code.append(directorHelper.generateWrapupCode());
        return processCode(code.toString());
    }

    /** Return an int array of firings per global iteration. For each
     *  internal configuration of this composite actor, the array
     *  contains a corresponding element representing the number of
     *  firings of this composite actor per global iteration.
     *
     *  @return An int array of firings per global iteration.
     *  @see #setFiringsPerGlobalIteration(int[])
     */
    public int[] getFiringsPerGlobalIteration() {
        return _firingsPerGlobalIteration;
    }

    /** Get the header files needed by the code generated from this helper
     *  class. It returns the result of calling getHeaderFiles() method of
     *  the helpers of all contained actors.
     *
     *  @return A set of strings that are header files.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating header files for the actor.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new LinkedHashSet();
        files.addAll(super.getHeaderFiles());

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            files.addAll(helperObject.getHeaderFiles());
        }

        // Get headers needed by the director helper.
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        files.addAll(directorHelper.getHeaderFiles());

        return files;
    }

    /** Return the include directories specified in the "includeDirectories"
     *  blocks in the templates of the actors included in this CompositeActor.
     *  @return A Set of the include directories.
     *  @exception IllegalActionException If thrown when gathering
     *  include directories.
     */
    public Set getIncludeDirectories() throws IllegalActionException {
        Set includeDirectories = new LinkedHashSet();
        includeDirectories.addAll(super.getIncludeDirectories());

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            includeDirectories.addAll(helperObject.getIncludeDirectories());
        }

        // Get include directories needed by the director helper.
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        includeDirectories.addAll(directorHelper.getIncludeDirectories());

        return includeDirectories;
    }

    /** Return the libraries specified in the "libraries" blocks in the
     *  templates of the actors included in this CompositeActor.
     *  @return A Set of libraries.
     *  @exception IllegalActionException If thrown when gathering libraries.
     */
    public Set getLibraries() throws IllegalActionException {
        Set libraries = new LinkedHashSet();
        libraries.addAll(super.getLibraries());

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            libraries.addAll(helperObject.getLibraries());
        }

        // Get libraries needed by the director helper.
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        libraries.addAll(directorHelper.getLibraries());

        return libraries;
    }

    /** Return a set of parameters that will be modified during the
     *  execution of the model. These parameters are those returned by
     *  getModifiedVariables() method of directors or actors that
     *  implement ExplicitChangeContext interface.
     *
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If the helper associated with an actor
     *   or director throws it while getting modified variables.
     */
    public Set getModifiedVariables() throws IllegalActionException {
        Set set = new HashSet();
        set.addAll(super.getModifiedVariables());

        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        set.addAll(directorHelper.getModifiedVariables());
        return set;
    }

    /** Return a two-dimensional int array of rates of this actor. For
     *  each internal configuration of this composite actor, the array
     *  contains a corresponding one-dimensional int array
     *  representing the rates of all ports of this composite
     *  actor. It returns null when there is only one internal
     *  configuration, e.g., when the internal model is an SDF model.
     *
     *  @return A two-dimensional int array of rates of this actor or null.
     *  @see #setRates(int[][])
     */
    public int[][] getRates() {
        return _rates;
    }

    /** Generate a set of shared code fragments of the associated
     *  composite actor.  It returns the result of calling
     *  getSharedCode() method of the helpers of all contained actors.
     *
     *  @return a set of shared code fragments.
     *  @exception IllegalActionException If the helper associated with
     *  an actor throws it while generating shared code for the actor.
     */
    public Set getSharedCode() throws IllegalActionException {

        // Use LinkedHashSet to give order to the shared code.
        Set sharedCode = new LinkedHashSet();
        sharedCode.addAll(super.getSharedCode());

        Iterator actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            sharedCode.addAll(helperObject.getSharedCode());
        }

        // Get shared code used by the director helper.
        Director directorHelper = (Director) _getHelper(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        sharedCode.addAll(directorHelper.getSharedCode());

        return sharedCode;
    }

    /** Reset the offsets of all inside buffers of all output ports of the
     *  associated composite actor to the default value of 0.
     *
     *  @return The reset code of the associated composite actor.
     *  @exception IllegalActionException If thrown while getting or
     *   setting the offset.
     */
    public String resetOutputPortsOffset() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator outputPorts = ((Actor) getComponent()).outputPortList()
                .iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();

            for (int i = 0; i < port.getWidthInside(); i++) {
                Object readOffset = getReadOffset(port, i);
                if (readOffset instanceof Integer) {
                    // Read offset is a number.
                    setReadOffset(port, i, Integer.valueOf(0));
                } else {
                    // Read offset is a variable.
                    code.append(CodeStream.indent(((String) readOffset)
                            + " = 0;" + _eol));
                }
                Object writeOffset = getWriteOffset(port, i);
                if (writeOffset instanceof Integer) {
                    // Write offset is a number.
                    setWriteOffset(port, i, Integer.valueOf(0));
                } else {
                    // Write offset is a variable.
                    code.append(CodeStream.indent(((String) writeOffset)
                            + " = 0;" + _eol));
                }
            }
        }
        return code.toString();
    }

    /** Set the int array of firings per global iteration. For each
     *  internal configuration of this composite actor, the array
     *  contains a corresponding element representing the number of
     *  firings of this composite actor per global iteration.
     *
     *  @param firingsPerGlobalIteration An int array of firings per
     *   global iteration
     *  @see #getFiringsPerGlobalIteration()
     */
    public void setFiringsPerGlobalIteration(int[] firingsPerGlobalIteration) {
        _firingsPerGlobalIteration = firingsPerGlobalIteration;
    }

    /** Set the two-dimensional int array of rates of this actor. For
     *  each internal configuration of this composite actor, the array
     *  contains a corresponding one-dimensional int array
     *  representing the rates of all ports of this composite actor.
     *
     *  @param rates A two-dimensional int array of rates of this actor.
     *  @see #getRates()
     */
    public void setRates(int[][] rates) {
        _rates = rates;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** Create and initialize the buffer size and offset maps for this
     *  composite actor. A key of the map is an IOPort of the actor.
     *  The corresponding value is an array of buffer sizes or an
     *  array of offsets. The i-th element in the array corresponds to
     *  the i-th channel of that IOPort.
     *
     * @exception IllegalActionException If thrown while getting helper
     *  or buffer size.
     */
    protected void _createBufferSizeAndOffsetMap()
            throws IllegalActionException {

        _createInputBufferSizeAndOffsetMap();

        // For the inside receivers of the output ports.
        _createOutputBufferSizeAndOffsetMap();

    }

    /** Create the output buffer and offset map.
     *  @exception IllegalActionException If thrown while getting the
     *  director helper or while getting the buffer size or read offset
     *  or write offset.
     */
    protected void _createOutputBufferSizeAndOffsetMap()
            throws IllegalActionException {

        Iterator outputPorts = ((Actor) getComponent()).outputPortList()
                .iterator();

        while (outputPorts.hasNext()) {

            IOPort port = (IOPort) outputPorts.next();
            int length = port.getWidthInside();

            int[] bufferSizes = new int[length];
            _bufferSizes.put(port, bufferSizes);

            Director directorHelper = (Director) _getHelper((((Actor) getComponent())
                    .getDirector()));

            for (int i = 0; i < port.getWidthInside(); i++) {
                // If the local director is an SDF director, then the buffer
                // size got from the director helper is final. Otherwise
                // the buffer size will be updated later on with the maximum
                // for all possible schedules.
                int bufferSize = directorHelper.getBufferSize(port, i);
                setBufferSize(port, i, bufferSize);
            }

            Object[] readOffsets = new Object[length];
            _readOffsets.put(port, readOffsets);

            Object[] writeOffsets = new Object[length];
            _writeOffsets.put(port, writeOffsets);

            for (int i = 0; i < length; i++) {
                setReadOffset(port, i, Integer.valueOf(0));
                setWriteOffset(port, i, Integer.valueOf(0));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** An int array of firings per global iteration.
     */
    private int[] _firingsPerGlobalIteration;

    /** A two-dimensional int array of rates of this actor.
     */
    private int[][] _rates;
}
