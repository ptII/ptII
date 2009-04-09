/* Base class for mathematical model converter.

   Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.verification.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.fmv.FmvAutomaton;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.verification.gui.MathematicalModelConverterGUIFactory;
import ptolemy.verification.kernel.maude.RTMaudeUtility;

// ////////////////////////////////////////////////////////////////////////
// // MathematicalModelConverter

/**
 *
 * @author Chihhong Patrick Cheng   (modified by: Kyungmin Bae)   Contributors: Edward A. Lee , Christopher Brooks,
 * @version $Id: MathematicalModelConverter.java,v 1.5 2008/03/06 09:16:22
 *          patrickj Exp $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red (patrickj)
 */
public class MathematicalModelConverter extends Attribute {
    /**
     * Create a new instance of the code generator.
     *
     * @param container The container.
     * @param name The name of the code generator.
     * @exception IllegalActionException
     *                    If the super class throws the exception or error
     *                    occurs when setting the file path.
     * @exception NameDuplicationException
     *                    If the super class throws the exception or an error
     *                    occurs when setting the file path.
     */
    public MathematicalModelConverter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        target = new FileParameter(this, "target", true);
        target.setDisplayName("Target File");
        
        template = new FileParameter(this, "template", true);
        template.setDisplayName("Template File");

        modelType = new ChoiceParameter(this, "modelType", ModelType.class);
        modelType.setExpression(ModelType.Maude.toString());
        modelType.setDisplayName("Model Type");

        formulaType = new ChoiceParameter(this, "Formula Type",
                FormulaType.class);
        formulaType.setExpression(FormulaType.CTL.toString());
        formulaType.setDisplayName("Formula Type");

        outputType = new ChoiceParameter(this, "Output Type", OutputType.class);
        outputType.setExpression(OutputType.Text.toString());
        outputType.setDisplayName("Output Type");

        formula = new StringParameter(this, "formula");
        formula.setDisplayName("Temporal Formula");

        span = new Parameter(this, "span");
        span.setTypeEquals(BaseType.INT);
        span.setExpression("0");
        span.setDisplayName("Variable Span Size");

        buffer = new Parameter(this, "buffer");
        buffer.setTypeEquals(BaseType.INT);
        buffer.setExpression("5");
        buffer.setDisplayName("FSMActor Buffer Size");

        _attachText("_iconDescription", "<svg>n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:pink\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nconvert system.</text></svg>");

        _model = (CompositeEntity) getContainer();

        new MathematicalModelConverterGUIFactory(this,
                "_codeGeneratorGUIFactory");

    }

    /////////////////////////////////////////////////////////////////
    // parameters ////

    /////////////////////////////////////////////////////////////////
    // public methods ////

    public StringBuffer generateCode(ModelType modelType,
            String inputTemporalFormula, FormulaType formulaType,
            int variableSpanSize, int FSMBufferSize)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        StringBuffer systemDescription = new StringBuffer("");

        switch (modelType) {
        case Kripke:
            if (_model instanceof CompositeActor)
                systemDescription.append(
                        SMVUtility.advancedGenerateSMVDescription(
                                (CompositeActor) _model,
                                inputTemporalFormula, formulaType,
                                variableSpanSize));
            else // FSMActor
                systemDescription.append(
                        ((FmvAutomaton)_model).convertToSMVFormat(
                            inputTemporalFormula, formulaType,
                            variableSpanSize));
            break;
        case CTA:
            systemDescription.append(
                    REDUtility.generateREDDescription(
                            (CompositeActor) _model,
                            inputTemporalFormula, formulaType,
                            variableSpanSize, FSMBufferSize));
            break;
        case Maude:
            if (_model instanceof CompositeActor)
                if (template.getExpression().trim().equals("")) {
                    systemDescription.append(RTMaudeUtility
                            .generateRTMDescription((CompositeActor) _model,
                                    inputTemporalFormula, true));
                } else {
                    systemDescription.append(RTMaudeUtility
                            .generateRTMDescription(template.openForReading(),
                                    (CompositeActor) _model,
                                    inputTemporalFormula));
                }
            /*
            else // FSMActor
                systemDescription.append(
                        ((FmvAutomaton)_model).convertToSMVFormat(
                            inputTemporalFormula, formulaType,
                            variableSpanSize));
            */
            break;
        }

        return systemDescription;
    }

    /**
     * Generate the model description for the system. This is the main entry
     * point.
     *
     * @return Textual format of the converted model based on the specification
     *         given.
     */
    public StringBuffer generateFile(File file, ModelType modelType,
            String inputTemporalFormula, FormulaType formulaType,
            int variableSpanSize, OutputType outputChoice, int FSMBufferSize)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException, IOException {
        StringBuffer returnStringBuffer = new StringBuffer("");
        // Perform deep traversal in order to generate .smv files.
        _codeFile = null;

        if (_model instanceof CompositeActor || _model instanceof FSMActor) {

            if (REDUtility.isValidModelForVerification((CompositeActor) _model)
                    || SMVUtility.isValidModelForVerification((CompositeActor) _model)
                    || _model instanceof FSMActor) {

                StringBuffer systemDescription = generateCode(modelType,
                        inputTemporalFormula, formulaType, variableSpanSize,
                        FSMBufferSize);
                if (outputChoice == OutputType.Text) {
                    FileWriter writer = null;
                    try {
                        writer = new FileWriter(file);
                        writer.write(systemDescription.toString());
                        _codeFile = file;
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }
                } else {
                    if (modelType == ModelType.Kripke) {
                        // Invoke NuSMV. Create a temporal file and
                        // later delete it. We first create a new folder
                        // which contains nothing. Then generate the System
                        // in format .smv, and perform model checking.
                        // If the system fails, all information would be
                        // stored in the folder. We can delete everything
                        // in the folder then delete the folder.
                        // The temporal file uses a random number generator
                        // to generate its name.

                        Random rd = new Random();
                        String folderName = "SystemGeneratedTempFolder"
                                + Integer.toString(rd.nextInt(10000)) + "/";
                        File smvFolder = new File(folderName);
                        if (smvFolder.exists()) {
                            while (smvFolder.exists() == true) {
                                folderName = "SystemGeneratedTempFolder"
                                        + Integer.toString(rd
                                                .nextInt(10000)) + "/";
                                smvFolder = new File(folderName);
                            }
                            // Now create the directory.
                            boolean isOpened = smvFolder.mkdir();
                            if (isOpened == false) {
                                throw new IllegalActionException("Failed to " +
                                        "invoke NuSMV correctly: \nUnable to " +
                                        "open a temp folder.");
                            }
                        } else {
                            boolean isOpened = smvFolder.mkdir();
                            if (isOpened == false) {
                                throw new IllegalActionException("Failed to " +
                                        "invoke NuSMV correctly:\nUnable to " +
                                        "open a temp folder.");
                            }

                        }
                        // Now establish the file.
                        File smvFile = new File(folderName + "System.smv");
                        String fileAbsolutePath = smvFile.getAbsolutePath();

                        FileWriter writer = null;
                        try {
                            writer = new FileWriter(smvFile);
                            writer.write(systemDescription
                                    .toString());

                        } finally {
                            if (writer != null) {
                                writer.close();
                            }
                        }

                        BufferedReader reader = null;
                        try {
                            Runtime rt = Runtime.getRuntime();
                            Process pr = rt.exec("NuSMV " + "\""
                                    + fileAbsolutePath + "\"");
                            InputStreamReader inputStream = new InputStreamReader(
                                    pr.getInputStream());
                            reader = new BufferedReader(inputStream);
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                returnStringBuffer.append(line + "\n");
                            }

                        } finally {
                            reader.close();
                        }
                        _deleteFolder(smvFolder);
                        return returnStringBuffer;
                    } else
                        MessageHandler
                        .error("The functionality for invoking RED is not implemented.\n");
                }

            } else {
                MessageHandler
                        .error("The execution director is not SR or DE.\nCurrently it is beyond our scope of analysis.");
            }

        }

        return returnStringBuffer;
    }

    /**
     * This is the main entry point to generate the graphical spec of the system.
     * It would invoke SMVUtility.generateGraphicalSpecification and return
     * the spec.
     *
     * @param formulaType The type of the graphical spec. It may be either "Risk"
     *                    or "Reachability".
     * @return The textual format of the graphical spec.
     * @throws IllegalActionException
     */
    public String generateGraphicalSpec(FormulaType formulaType)
            throws IllegalActionException {

        if (_model instanceof CompositeActor) {
            return SMVUtility.generateGraphicalSpecification(
                    (CompositeActor) _model, formulaType);
        } else {
            throw new IllegalActionException(
                    "SMVUtility.generateGraphicalSpec error:\nModel not instance of CompositeActor");
        }
    }

    public File getCodeFile() {
        return _codeFile;
    }

    public enum ModelType {
        CTA {
            public String toString() {
                return "Communicating Timed Automata (Acceptable by RED " +
                        "under DE)";
            }
        }, Kripke {
            public String toString() {
                return "Kripke Structures (Acceptable by NuSMV under SR)";
            }
        }, Maude {
            public String toString() {
                return "Real-time Maude Translation(under SR or DE)";
            }
        }
    }

    public enum FormulaType {
        CTL,
        LTL,
        TCTL,
        Buffer {
            public String toString() {
                return "Buffer Overflow";
            }
        },
        Risk,
        Reachability
    }

    public enum OutputType {
        Text {
            public String toString() {
                return "Text Only";
            }
        },
        SMV {
            public String toString() {
                return "Invoke NuSMV";
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    // protected variables ////

    /** The name of the file that was written. If no file was written, then the
     * value is null.
     */
    protected File _codeFile = null;

    protected File _directory;

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    /** The model we for which we are generating code. */
    protected CompositeEntity _model;

    /** This is used to delete recursively the folder and files within.
     */
    private void _deleteFolder(File folder) throws IllegalActionException,
            IOException {

        if (folder.list() == null || folder.list().length <= 0) {
            boolean isDeleted = folder.delete();
            if (isDeleted == false) {
                throw new IllegalActionException(
                        "Temporary subfolder delete unsuccessful");
            }
        } else {
            for (int i = 0; i < folder.list().length; i++) {
                String childName = folder.list()[i];
                String childPath = folder.getPath() + File.separator
                        + childName;
                File filePath = new File(childPath);
                if (filePath.exists() && filePath.isFile()) {
                    boolean isDeleted = filePath.delete();
                    if (isDeleted == false) {
                        throw new IllegalActionException(
                                "Temporary file delete unsuccessful");
                    }
                } else if (filePath.exists() && filePath.isDirectory()) {
                    _deleteFolder(filePath);
                }
            }
            boolean isDeleted = folder.delete();
            if (isDeleted == false) {
                throw new IllegalActionException(
                        "Temporary folder delete unsuccessful");
            }
        }
    }

    public FileParameter target;
    
    public FileParameter template;

    public ChoiceParameter modelType;

    public ChoiceParameter formulaType;

    public ChoiceParameter outputType;

    public StringParameter formula;

    public Parameter span;

    public Parameter buffer;
}
