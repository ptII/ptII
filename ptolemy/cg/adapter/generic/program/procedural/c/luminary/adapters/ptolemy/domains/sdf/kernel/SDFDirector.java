package ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.sdf.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.lib.CompiledCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

public class SDFDirector
        extends
        ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel.SDFDirector {
    
    /** Construct the code generator adapter associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "SDFDirector: " + "Transfer tokens to the inside.")));
        int rate = DFUtilities.getTokenConsumptionRate(inputPort);

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) getCodeGenerator().generateEmbeddedCode
                        .getToken()).booleanValue()) {

            // FindBugs wants this instanceof check.
            if (!(inputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(inputPort, null,
                        " is not an instance of TypedIOPort.");
            }

            ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort portAdapter 
                = (ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort) 
                    getAdapter(inputPort);
            
            // FIXME: not sure what to do with this offset here.
            String offset = "";
            
            for (int i = 0; i < inputPort.getWidth(); i++) {
                // the following code is of the form:
                // if ($hasToken(i)) {
                //     input = Event_Head->data;
                // }
                code.append("if (");
                code.append(portAdapter.generateHasTokenCode(Integer.toString(i), offset));
                code.append(") {" + _eol);
                
                // the input port to transfer the data to was declared earlier, and is of this name:
                StringBuffer inputCode = new StringBuffer();
                boolean dynamicReferencesAllowed = allowDynamicMultiportReference();
                inputCode.append(NamedProgramCodeGeneratorAdapter.generateName(inputPort));
                int bufferSize = _ports.getBufferSize(inputPort);
                if (inputPort.isMultiport()) {
                    inputCode.append("[" + Integer.toString(i) + "]");
                    if (bufferSize > 1 || dynamicReferencesAllowed) {
                        throw new InternalErrorException("Generation of input transfer code" +
                        		"requires the knowledge of offset in the buffer, this" +
                        		"is not yet supported.");
//                        inputCode.append("[" + bufferSize + "]");
                    }
                } else {
                    if (bufferSize > 1) {
                        throw new InternalErrorException("Generation of input transfer code" +
                                "requires the knowledge of offset in the buffer, this" +
                                "is not yet supported.");
//                        inputCode.append("[" + bufferSize + "]");
                    }
                }

                code.append(inputCode);
                code.append(" = ");
                code.append(portAdapter.generateGetCode(Integer.toString(i), offset));
                code.append(";" + _eol);
                code.append("}" + _eol);
            }
        } else {
            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {
                    String name = inputPort.getName();

                    if (inputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorAdapter.getReference("@"
                                + name + "," + k));
                        code.append(" = " + _eol);
                        code.append(compositeActorAdapter.getReference(name
                                + "," + k));
                        code.append(";" + _eol);
                    }
                }
            }
        }

        // Generate the type conversion code before fire code.
        code.append(compositeActorAdapter.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, rate);
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "SDFDirector: " + "Transfer tokens to the outside.")));

        int rate = DFUtilities.getTokenProductionRate(outputPort);

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) getCodeGenerator().generateEmbeddedCode
                        .getToken()).booleanValue()) {

            ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort portAdapter 
            = (ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort) 
                getAdapter(outputPort);
            
            // FIXME: not sure what to do with this offset here.
            String offset = "";

            for (int i = 0; i < outputPort.getWidth(); i++) {
                
                
                StringBuffer outputCode = new StringBuffer();
                outputCode.append(NamedProgramCodeGeneratorAdapter.generateName(outputPort));

                if (outputPort.isMultiport()) {
                    outputCode.append("[" + Integer.toString(i) + "]");
                }

                int bufferSize = _ports.getBufferSize(outputPort);

                if (bufferSize > 1) {
                    throw new InternalErrorException("Generation of input transfer code" +
                            "requires the knowledge of offset in the buffer, this" +
                            "is not yet supported.");
//                    outputCode.append("[" + bufferSize + "]");
                }

                code.append(portAdapter.
                        generatePutCode(Integer.toString(i), offset, outputCode.toString()));
            }
            
        } else {
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {
                    String name = outputPort.getName();

                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(CodeStream.indent(compositeActorAdapter
                                .getReference(name + "," + k)));
                        code.append(" =" + _eol);
                        code.append(CodeStream.indent(compositeActorAdapter
                                .getReference("@" + name + "," + k)));
                        code.append(";" + _eol);
                    }
                }
            }
        }

        // The offset of the ports connected to the output port is
        // updated by outside director.
        _updatePortOffset(outputPort, code, rate);
    }

}
