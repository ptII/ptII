/* A tableau for controlling code generation.

 Copyright (c) 2000-2007 The Regents of the University of California.
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
package ptolemy.copernicus.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.codegen.kernel.CodeGeneratorUtilities;
import ptolemy.copernicus.kernel.Copernicus;
import ptolemy.copernicus.kernel.GeneratorAttribute;
import ptolemy.gui.JTextAreaExec;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// GeneratorTableau

/**
 A tableau that creates a new control panel for code generation.

 @author Shuvra Bhattacharyya, Edward A. Lee, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class GeneratorTableau extends Tableau {
    /** Create a new control panel for code generation.
     *  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public GeneratorTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();

        if (model instanceof CompositeActor) {
            GeneratorFrame frame = new GeneratorFrame((CompositeActor) model,
                    this);
            setFrame(frame);
            frame.setBackground(BACKGROUND_COLOR);
        } else {
            throw new IllegalActionException(model,
                    "Can only generate code for "
                            + "instances of CompositeEntity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Default background color is a light grey.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of GeneratorTableau.
     */
    public class GeneratorFrame extends PtolemyFrame {
        /** Construct a frame to control code generation for
         *  the specified Ptolemy II model.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
         *  @param model The model to put in this frame, or null if none.
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
         */
        public GeneratorFrame(final CompositeActor model, Tableau tableau)
                throws IllegalActionException, NameDuplicationException {
            super(model, tableau);

            // If the model has been modified, then save it.  When we
            // run codegen, we often run a separate java process and
            // read a .xml file so that xml file should be updated.
            if (isModified()) {
                _save();
            }

            if ((getEffigy() == null) || (getEffigy().uri == null)
                    || (getEffigy().uri.getURI() == null)) {
                // If the user does File -> New -> GraphEditor,
                // View -> Code Generator, then we might end up
                // dealing with an Effigy that has a null url.
                // FIXME: This should work, but TableauFrame._saveAs()
                // calls effigy.setContainer(null), which means
                // that getEffigy() returns null
                throw new IllegalActionException(
                        model,
                        (Throwable) null,
                        "Could not get the Effigy or read the URL of this "
                                + "model.  Because of a bug, you may need to try "
                                + "invoking the code generator again.");
            }

            // Caveats panel.
            JPanel caveatsPanel = new JPanel();
            caveatsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            caveatsPanel
                    .setLayout(new BoxLayout(caveatsPanel, BoxLayout.X_AXIS));

            JTextArea messageArea = new JTextArea(
                    "NOTE: This is a highly preliminary "
                            + "code generator facility, with many "
                            + "limitations.  It is best viewed as "
                            + "a concept demonstration.", 2, 10);
            messageArea.setEditable(false);
            messageArea.setBorder(BorderFactory.createEtchedBorder());
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            caveatsPanel.add(messageArea);

            JButton moreInfoButton = new JButton("More Info");
            moreInfoButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Configuration configuration = getConfiguration();
                    URL infoURL = getClass().getResource(
                            "../../../doc/codegen.htm");

                    try {
                        configuration.openModel(null, infoURL, infoURL
                                .toExternalForm());
                    } catch (Exception ex) {
                        throw new InternalErrorException(model, ex,
                                "Failed to open doc/codegen.htm: ");
                    }
                }
            });
            caveatsPanel.add(moreInfoButton);

            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            caveatsPanel.setMaximumSize(new Dimension(500, 100));
            left.add(caveatsPanel);

            // Panel for push buttons.
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 4));

            // Button panel first.
            JButton parametersButton = new JButton("Parameters");
            parametersButton
                    .setToolTipText("Sanity check the Parameters and then "
                            + "display a summary.");
            buttonPanel.add(parametersButton);

            JButton goButton = new JButton("Generate");
            goButton.setToolTipText("Generate code");
            buttonPanel.add(goButton);

            JButton stopButton = new JButton("Cancel");
            stopButton.setToolTipText("Terminate executing processes");
            buttonPanel.add(stopButton);

            JButton clearButton = new JButton("Clear");
            clearButton.setToolTipText("Clear Log");
            buttonPanel.add(clearButton);

            buttonPanel.setMaximumSize(new Dimension(500, 50));
            left.add(buttonPanel);

            // Next, put in a panel to configure the code generator.
            // If the model contains an attribute with tableau
            // configuration information, use that.  Otherwise, make one.
            GeneratorAttribute attribute = (GeneratorAttribute) model
                    .getAttribute(Copernicus.GENERATOR_NAME,
                            GeneratorAttribute.class);

            if (attribute == null) {
                attribute = new GeneratorAttribute(model,
                        Copernicus.GENERATOR_NAME);

                // Read the default parameters for this model.
                attribute.initialize();
            }

            Configurer configurer = new Configurer(attribute);
            final GeneratorAttribute options = attribute;

            JPanel controlPanel = new JPanel();
            controlPanel.add(configurer);

            JScrollPane scrollPane = new JScrollPane(controlPanel);

            left.add(scrollPane, BorderLayout.CENTER);

            // Create a JTextAreaExec without Start and Cancel buttons.
            final JTextAreaExec exec = new JTextAreaExec(
                    "Code Generator Commands", false);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    left, exec);
            splitPane.setOneTouchExpandable(true);

            // Adjust the divider so that the control panel does not
            // have a horizontal scrollbar.
            Dimension preferred = left.getPreferredSize();
            splitPane.setDividerLocation(preferred.width + 20);

            getContentPane().add(splitPane, BorderLayout.CENTER);

            // ActionListeners for the buttons
            parametersButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        options.sanityCheckAndUpdateParameters(null);
                    } catch (Exception ex) {
                        exec.appendJTextArea(ex.toString());
                    }

                    exec.appendJTextArea(options.toString());
                }
            });

            stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    exec.cancel();
                }
            });

            clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    exec.clear();
                }
            });

            goButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        // The code generator to run.  The value of this
                        // parameter should name a subdirectory of
                        // ptolemy/copernicus such as "java" or "shallow".
                        String codeGenerator = options
                                .getParameter("codeGenerator");

                        String targetPath = options.getParameter("targetPath");

                        String ptIIUserDirectory = options
                                .getParameter("ptIIUserDirectory");

                        // Check that we will be able to write
                        File directory = new File(ptIIUserDirectory, targetPath);

                        if (!directory.isDirectory()) {
                            throw new IllegalActionException(model,
                                    "Not a directory: " + ptIIUserDirectory
                                            + "/" + targetPath
                                            + "\n.Try hitting the "
                                            + "Parameters button to "
                                            + "create the directory.");
                        }

                        if (!directory.canWrite()) {
                            throw new IllegalActionException(model,
                                    "Can't write: " + ptIIUserDirectory + "/"
                                            + targetPath);
                        }

                        exec.updateStatusBar("Starting " + codeGenerator
                                + " code generation.");

                        // Run the code generator in a separate process
                        try {
                            List commands = _generateCodeGeneratorCommands(
                                    model, options, codeGenerator);
                            exec.setCommands(commands);
                            exec.start();
                        } catch (Exception ex) {
                            throw new IllegalActionException(model, ex, null);
                        }

                        // FIXME: Above is asynchronous: Do in listener?
                        exec.updateStatusBar("Code generation " + "complete.");
                    } catch (Exception ex) {
                        MessageHandler.error("Code generation failed.", ex);
                    }
                }
            });
        }
    }

    /** A factory that creates a control panel for code generation.
     */
    public static class Factory extends TableauFactory {
        /** Create an factory with the given name and container.
         *  @param container The container entity.
         *  @param name The name of the entity.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a new instance of GeneratorTableau in the specified
         *  effigy. If the specified effigy is not an
         *  instance of PtolemyEffigy, then do not create a tableau
         *  and return null. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *
         *  @param effigy The model effigy.
         *  @return A new control panel tableau if the effigy is
         *    a PtolemyEffigy, or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a tableau
                GeneratorTableau tableau = (GeneratorTableau) effigy
                        .getEntity("generatorTableau");

                if (tableau == null) {
                    tableau = new GeneratorTableau((PtolemyEffigy) effigy,
                            "generatorTableau");
                }

                // Don't call show() here, it is called for us in
                // TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
            } else {
                return null;
            }
        }
    }

    // Return a List consisting of the command string that will
    // generate code Java for model and the command string that will
    // run the generated code.
    //
    // @param model The model to generate code for.
    // @param generatorAttribute The GeneratorAttribute that
    // controls the compilation of the model.
    // @param codeGenerator The directory that contains
    // the generator we are running.  Usually, something like
    // "applet" or "java" or "shallow".
    private List _generateCodeGeneratorCommands(CompositeEntity model,
            GeneratorAttribute generatorAttribute, String codeGenerator)
            throws IllegalArgumentException, InternalErrorException {
        List results = new LinkedList();

        try {
            // Write the model to a temporary file.
            File temporaryFile = File.createTempFile("ptCopernicus", ".xml");
            temporaryFile.deleteOnExit();

            FileWriter writer = null;
            try {
                writer = new FileWriter(temporaryFile);
                model.exportMoML(writer);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable throwable) {
                        throw new RuntimeException("Failed to close "
                                + temporaryFile, throwable);
                    }
                }
            }

            // Set the temporary modelPath.
            generatorAttribute.sanityCheckAndUpdateParameters(temporaryFile
                    .toURI().toString());

            // Generate the command to run copernicus.
            results.add(CodeGeneratorUtilities.substitute(
                    "ptolemy/copernicus/gui/compileCommandTemplate.txt",
                    generatorAttribute));

            // Replace the original modelPath.
            generatorAttribute
                    .sanityCheckAndUpdateParameters(((GeneratorFrame) getFrame())
                            .getEffigy().uri.getURI().toString());
        } catch (Exception ex) {
            throw new InternalErrorException(model, ex, "Failed to generate "
                    + "command strings");
        }

        return results;
    }
}
