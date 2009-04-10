/* A transformer that writes an applet version of the model.

 Copyright (c) 2001-2008 The Regents of the University of California.
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
package ptolemy.copernicus.applet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.codegen.kernel.CodeGeneratorUtilities;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.ClassUtilities;
import ptolemy.util.StringUtilities;
import soot.HasPhaseOptions;
import soot.PhaseOptions;
import soot.SceneTransformer;

/**
 A transformer that writes an applet version of a model.
 For a model called Foo, we generate Foo/makefile, Foo/Foo.xml,
 Foo/Foo.htm Foo/FooVergil.htm in the directory named by the
 outputDirectory parameter.

 <p>Potential future enhancements
 <menu>
 <li> Pull out the top level annotation and add the text to the web page.
 </menu>

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class AppletWriter extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private AppletWriter(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     * the given model. The model is assumed to already have been
     * properly initialized so that resolved types and other static
     * properties of the model can be inspected.
     * @param model The model that this class will operate on.
     * @return An instance of the AppletWriter transformer.
     */
    public static AppletWriter v(CompositeActor model) {
        return new AppletWriter(model);
    }

    /** Return the default options.  The default options for
     *  this transformer is the string:
     *  <pre>
     *  templateDirectory: ptolemy/copernicus/applet/
     *  </pre>
     *  @return the default options.
     */
    public String getDefaultOptions() {
        String ptolemyHome = "";
        try {
            // If ptolemyHome has spaces in it, then we have problems.
            ptolemyHome = StringUtilities.getProperty("ptolemy.ptII.dir").replace(" ", "\\ ");
        } catch (SecurityException security) {
            throw new InternalErrorException(null, security, "Could not find "
                    + "'ptolemy.ptII.dir'" + " property.  Vergil should be "
                    + "invoked with -Dptolemy.ptII.dir" + "=\"$PTII\"");

        }
        return " templateDirectory:" + TEMPLATE_DIRECTORY_DEFAULT
            + " ptIIJarsPath:" + ptolemyHome;
    }

    /** Return the declared options.  The declared options for
     *  this transformer is the string
     *  <pre>
     *  modelPath outputDirectory ptIIJarsPath ptIIUserDirectory targetPackage targetPath templateDirectory
     *  </pre>
     *  @return the declared options.
     */
    public String getDeclaredOptions() {
        return "modelPath outputDirectory ptIIJarsPath ptIIUserDirectory targetPackage targetPath templateDirectory";
    }

    /** Return the phase name.  The phase name of
     *  this transformer is the empty string.
     *  @return the phase name.
     */
    public String getPhaseName() {
        return "";
    }

    /** Save the model as an applet.
     *  <p>For example, if the model is called MyModel, and
     *  this phase is called with:
     *  <pre>
     *        -p wjtp.appletWriter targetPackage:foo.bar
     *  </pre>
     *  Then we will create the directory $PTII/foo/bar/MyModel and
     *  place MyModel.xml, MyModel.htm, MyModelVergil.htm in that
     *  directory.
     *
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.appletWriter</code>.
     *  @param options The options Map.  This method uses the
     *  <code>targetPackage</code> option to specify package
     *  to generate code in.
     */
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("AppletWriter.internalTransform(" + phaseName + ", "
                + options + ")");

        // URL that names the model.
        _modelPath = PhaseOptions.getString(options, "modelPath");


        // The directory that code will be generated in.
        // $ptIIUserDirectory/$targetPath"
        _outputDirectory = PhaseOptions.getString(options, "outputDirectory");

          // Determine where $PTII is so that we can find the right directory.
        _ptIIJarsPath = PhaseOptions.getString(options, "ptIIJarsPath");
        try {
            if (_ptIIJarsPath == null) {
                // NOTE: getProperty() will probably fail in applets, which
                // is why this is in a try block.
                // NOTE: This property is set by the vergil startup script.
                _ptIIJarsPath = StringUtilities.getProperty("ptolemy.ptII.dir");
            } else {
                System.out.println("AppletWriter: ptIIJarsPath = "
                        + _ptIIJarsPath);
            }
        } catch (SecurityException security) {
            throw new InternalErrorException(null, security, "Could not find "
                    + "'ptolemy.ptII.dir'" + " property.  Vergil should be "
                    + "invoked with -Dptolemy.ptII.dir" + "=\"$PTII\"");
        }

        _ptIIUserDirectory = PhaseOptions.getString(options, "ptIIUserDirectory");
        try {
            if (_ptIIUserDirectory == null) {
                // NOTE: getProperty() will probably fail in applets, which
                // is why this is in a try block.
                // NOTE: This property is set by the vergil startup script.
                _ptIIUserDirectory = StringUtilities.getProperty("ptolemy.ptII.dir");
            } else {
                System.out.println("AppletWriter: ptIIUserDirectory = "
                        + _ptIIUserDirectory);
            }
        } catch (SecurityException security) {
            throw new InternalErrorException(null, security, "Could not find "
                    + "'ptolemy.ptII.dir'" + " property.  Vergil should be "
                    + "invoked with -Dptolemy.ptII.dir" + "=\"$PTII\"");
        }

        // If the targetPackage is foo.bar, and the model is Bif,
        // the we will do mkdir $PTII/foo/bar/Bif/
        _targetPackage = PhaseOptions.getString(options, "targetPackage");

        // The path relative to the ptIIUserDirectory to generate code in.
        // Defaults to ptolemy/copernicus/$codeGenerator/cg/$modelName
        _targetPath = PhaseOptions.getString(options, "targetPath");
        if (_targetPath.length() > 0 && !_targetPath.substring(_targetPath.length() - 1).equals("/")) {
            System.out.println("AppletWriter: appending / to targetPath");
            _targetPath += "/";
        }


        // Determine the value of _domainJar, which is the
        // path to the domain specific jar, e.g. "ptolemy/domains/sdf/sdf.jar"
        System.out.println("AppletWriter: _model: " + _model);

        Director director = _model.getDirector();
        System.out.println("AppletWriter: director: " + director);

        String directorPackage = director.getClass().getPackage().getName();

        if (!directorPackage.endsWith(".kernel")) {
            System.out.println("Warning: the directorPackage does not end "
                    + "with '.kernel', it is :" + directorPackage);
        }

        _domainJar = _getDomainJar(directorPackage);

        _sanitizedModelName = StringUtilities.sanitizeName(_model.getName());

        _codeBase = MakefileWriter.codeBase(_targetPackage, _outputDirectory,
                _ptIIJarsPath);

        // Create the directory where we will create the files.
        File outputDirectoryFile = new File(_outputDirectory);

        if (!outputDirectoryFile.isDirectory()) {
            // MakefileWriter should have already created the directory
            if (!outputDirectoryFile.mkdirs()) {
                System.out.println("Warning: Failed to create directory \""
                        + outputDirectoryFile + "\"");
            }
        }

        // The JNLP file to be created
        String jnlpSourceFileName = _outputDirectory + "/" + _sanitizedModelName + ".jnlp";
        String jnlpJarFileName = _outputDirectory + "/signed_" + _sanitizedModelName + ".jar";
        String jnlpUnsignedJarFileName = _outputDirectory + "/" + _sanitizedModelName + ".jar";

        try {
            // If the code base is the current directory, then we
            // copy the jar files over and set the value of _domainJar
            // to the names of the jar files separated by commas.
            // We always want to generate the list of jar files so
            // that if for example we use fsm, then we are sure to
            // include diva.jar
            // We do both the applet jar files and the jnlp jar files
            StringBuffer jarFilesResults = new StringBuffer();
            StringBuffer jnlpJarFilesResults = new StringBuffer();


            // This is the signed jar file that includes the .jnlp file
            // FIXME: what if we don't want a signed jar?
            jnlpJarFilesResults.insert(0, "        <jar href=\""
                                       + _targetPath + "signed_" + _sanitizedModelName + ".jar\""
                                       + _jarFileLengthAttribute(jnlpSourceFileName)
                                       + "\n             main=\"true\"/>\n");

            boolean sawSignedOnce = false;

            Iterator jarFileNames = _findModelJarFiles(director).iterator();

            while (jarFileNames.hasNext()) {
                String jarFileName = (String) jarFileNames.next();

                if (jarFilesResults.length() > 0) {
                    jarFilesResults.append(",");
                }
                jarFilesResults.append(jarFileName);

                // If the ptII/signed directory contains the jar file, then set
                // signed to "signed/".  Otherwise, set signed to "".
                String signed =  "";
                String signedJarFileName = _ptIIJarsPath + File.separator + "signed"
                    + File.separator + jarFileName;
                if (new File(signedJarFileName).exists()) {;
                    signed = "signed" + File.separator;
                    sawSignedOnce = true;
                }
                if (signed == "" && sawSignedOnce) {
                    // We saw something in the signed directory, but this file
                    // is not there, so we sign it.
                    try {
                        _signJarFile(_ptIIJarsPath + File.separator + jarFileName,
                                signedJarFileName);
                        signed = "signed" + File.separator;
                    } catch (Exception ex) {
                        throw new InternalErrorException(null, ex, "Failed to sign \""
                                + _ptIIJarsPath + File.separator + jarFileName
                                + "\" and create \"" + signedJarFileName + "\"");
                    }
                }
                System.out.println("signedJarFile: " + signedJarFileName + " signed: \"" + signed + "\"");
                jnlpJarFilesResults.append("        <jar href=\""
                                    + signed + jarFileName + "\""
                                     + _jarFileLengthAttribute(_ptIIJarsPath + File.separator + signed + jarFileName)
                                    + "\n             download=\"eager\"/>\n");
            }

            _modelJarFiles = jarFilesResults.toString();

            sawSignedOnce = false;

            // Get the vergil jar files for the applet
            jarFilesResults = new StringBuffer();
            // We don't reset the jar files for jnlp because we assume
            // all JNLP files run vergil.
            jarFileNames = _findVergilJarFiles(director).iterator();

            while (jarFileNames.hasNext()) {
                String jarFileName = (String) jarFileNames.next();

                if (jarFilesResults.length() > 0) {
                    jarFilesResults.append(",");
                }

                jarFilesResults.append(jarFileName);

                // If the ptII/signed directory contains the jar file, then set
                // signed to "signed/".  Otherwise, set signed to "".
                String signed =  "";
                String signedJarFileName = _ptIIJarsPath + File.separator + "signed"
                    + File.separator + jarFileName;
                if (new File(signedJarFileName).exists()) {;
                    signed = "signed" + File.separator;
                    sawSignedOnce = true;
                }
                if (signed == "" && sawSignedOnce) {
                    // We saw something in the signed directory, but this file
                    // is not there, so we sign it.
                    try {
                        _signJarFile(_ptIIJarsPath + File.separator + jarFileName,
                                _ptIIJarsPath + File.separator + "signed"
                                + File.separator + jarFileName);
                        signed = "signed" + File.separator;
                    } catch (Exception ex) {
                        throw new InternalErrorException(null, ex, "Failed to sign \""
                                + _ptIIJarsPath + File.separator + jarFileName
                                + "\" and create \"" + signedJarFileName + "\"");
                    }
                }
                jnlpJarFilesResults.append("        <jar href=\""
                                           + signed + jarFileName  + "\""
                                           + _jarFileLengthAttribute(_ptIIJarsPath + File.separator + signed + jarFileName)
                                           + "\n             download=\"eager\"/>\n");
            }

            _vergilJarFiles = jarFilesResults.toString();
            _jnlpJars = jnlpJarFilesResults.toString();

        } catch (IOException ex) {
            // This exception tends to get eaten by soot, so we print as well.
            System.err.println("Problem writing makefile or html files:");
            ex.printStackTrace();
            throw new InternalErrorException(null, ex,
                    "Problem writing the makefile or htm files.");
        }

        // Get the size of the vergil window from the model.
        int appletWidth = 400;
        int appletHeight = 450;
        int vergilWidth = 600;
        int vergilHeight = 800;

        try {
            WindowPropertiesAttribute windowProperties = (WindowPropertiesAttribute) _model
                    .getAttribute("_windowProperties");
            ArrayToken boundsToken = (ArrayToken) ((RecordToken) windowProperties
                    .getToken()).get("bounds");

            appletWidth = ((IntToken) boundsToken.getElement(2)).intValue();
            appletHeight = ((IntToken) boundsToken.getElement(3)).intValue();
        } catch (Exception ex) {
            System.out.println("Warning: Failed to get applet width "
                    + "and height, using defaults: " + ex.getMessage());
        }

        try {
            SizeAttribute vergilSize = (SizeAttribute) _model
                    .getAttribute("_vergilSize");

            IntMatrixToken vergilSizeToken = (IntMatrixToken) vergilSize
                    .getToken();

            vergilWidth = vergilSizeToken.getElementAt(0, 0);
            vergilHeight = vergilSizeToken.getElementAt(0, 1);
        } catch (Exception ex) {
            System.out.println("Warning: Failed to get vergil width "
                    + "and height, using defaults: " + ex.getMessage());
        }

        // The vergil applet shows the model and the top level window.
        vergilHeight += appletHeight;

        // Add 200 to the applet height to include the control panels.
        appletHeight += 200;

        // Set up the HashMap we will use when we read in files like
        // model.htm.in and search for strings like @codebase@ and
        // substitute in the value of _codeBase.
        _substituteMap = new HashMap();
        _substituteMap.put("@appletHeight@", Integer.toString(appletHeight));
        _substituteMap.put("@appletWidth@", Integer.toString(appletWidth));
        _substituteMap.put("@codeBase@", _codeBase);
        _substituteMap.put("@jnlpJars@", _jnlpJars);
        _substituteMap.put("@modelJarFiles@", _modelJarFiles);
        _substituteMap.put("@outputDirectory@", _outputDirectory);
        _substituteMap.put("@sanitizedModelName@", _sanitizedModelName);
        _substituteMap.put("@ptIIJarsPath@", _ptIIJarsPath);
        _substituteMap.put("@ptIIUserDirectory@", _ptIIUserDirectory);
        try {
            _substituteMap.put("@ptIILocalURL@", new URL(new File(_outputDirectory).toURI().toURL(), _codeBase).toString());
        } catch (Exception ex) {
            throw new InternalErrorException(null, ex, "Failed to create URL for \""
                                             + _outputDirectory + "\"");
        }
        _substituteMap.put("@targetPath@", _targetPath);
        _substituteMap.put("@vergilHeight@", Integer.toString(vergilHeight));
        _substituteMap.put("@vergilJarFiles@", _vergilJarFiles);
        _substituteMap.put("@vergilWidth@", Integer.toString(vergilWidth));

        // Print out the map for debugging purposes
        Iterator keys = _substituteMap.keySet().iterator();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            System.out.println("AppletWriter: '" + key + "' '"
                    + (String) _substituteMap.get(key) + "'");
        }

        // Generate the .xml file.
        String newModelFileName = _outputDirectory + "/" + _sanitizedModelName
                + ".xml";

        try {
            // Since we strip out the graphical information in
            // Copernicus.readInModel(), we need to copy the original
            // model so that the vergil applet has the layout info.
            File newModelFile = new File(newModelFileName);

            URL modelPathURL = new URL(_modelPath);

            if (!modelPathURL.sameFile(newModelFile.getCanonicalFile().toURI()
                    .toURL())) {
                // Here, _modelPath probably has the GeneratorAttribute
                // in it, which is not what we want because when we load
                // the applet, the copernicus class files will not be present.
                // So, we strip out GeneratorAttribute
                try {
                    _copyModelRemoveGeneratorTableau(modelPathURL, newModelFile);
                } catch (Exception ex) {
                    IOException io = new IOException("Problem reading '"
                            + _modelPath + "' or " + "writing '"
                            + newModelFileName + "'");
                    io.initCause(ex);
                    throw io;
                }

                System.out.println("AppletWriter: removed GeneratorAttribute "
                        + "while copying '" + modelPathURL + "' to '"
                        + newModelFile + "'");
            } else {
                System.out.println("AppletWriter: No need to copy the .xml "
                        + "file,\n the source (" + _modelPath
                        + ")\n and destination (" + newModelFile
                        + ")\n refer to the same file.");
            }
        } catch (IOException ex) {
            System.out.println("AppletWriter: WARNING: Problem reading '"
                    + _modelPath + "' and writing '" + newModelFileName
                    + "', instead we call exportMoML(), which will lose "
                    + "vergil layout information: " + ex.getMessage());
            System.out.println("AppletWriter: about to write '"
                    + newModelFileName + "'");

            Writer modelFileWriter = null;

            try {
                modelFileWriter = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(newModelFileName)));
                _model.exportMoML(modelFileWriter);

                // FIXME: need finally?
                modelFileWriter.close();
            } catch (IOException ex2) {
                // Rethrow original exception ex.
                throw new InternalErrorException(null, ex, "Problem reading '"
                        + _modelPath + "' or " + "writing '" + newModelFileName
                        + "'\n" + "Also tried calling exportMoML():"
                        + ex2.getMessage());
            } finally {
                if (modelFileWriter != null) {
                    try {
                        modelFileWriter.close();
                    } catch (IOException ex2) {
                        throw new InternalErrorException(null, ex2,
                                "Failed to close '" + newModelFileName + "'");
                    }
                }
            }
        }

        // The directory that contains the templates.
        // FIXME: this could be a Ptolemy parameter?
        //_templateDirectory =
        //    StringUtilities.substitute(PhaseOptions.getString(options,
        //            "templateDirectory"),
        //            "$PTII", _ptIIJarsPath);
        _templateDirectory = PhaseOptions.getString(options,
                "templateDirectory");

        System.out.println("AppletWriter: _templateDirectory: '"
                + _templateDirectory + "'");

        // Read in the templates and generate new files.
        try {
            CodeGeneratorUtilities.substitute(_templateDirectory
                    + "model.htm.in", _substituteMap, _outputDirectory + "/"
                    + _sanitizedModelName + ".htm");
            CodeGeneratorUtilities.substitute(_templateDirectory
                    + "modelVergil.htm.in", _substituteMap, _outputDirectory
                    + "/" + _sanitizedModelName + "Vergil.htm");
            CodeGeneratorUtilities.substitute(_templateDirectory
                    + "model.jnlp.in", _substituteMap, jnlpSourceFileName);

            _createJarFile(new File(jnlpUnsignedJarFileName),
                           new String [] {
                               "JNLP-INF/APPLICATION.JNLP",
                               _sanitizedModelName + ".xml",
                               "ptolemy/copernicus/applet/JNLPApplication.class",
                               "ptolemy/actor/gui/jnlp/MenuApplication.class"},
                           new File [] {
                               new File(_outputDirectory + "/" + _sanitizedModelName + ".jnlp"),
                               new File(newModelFileName),
                               new File(_ptIIJarsPath + "/ptolemy/copernicus/applet/JNLPApplication.class"),
                               new File(_ptIIJarsPath + "/ptolemy/actor/gui/jnlp/MenuApplication.class"),
                           });

            _signJarFile(jnlpUnsignedJarFileName,
                    jnlpJarFileName);

            // Copy $PTII/doc/default.css as well.
            File defaultStyleSheetDirectory = new File(_outputDirectory
                    + "/doc");
            if (!defaultStyleSheetDirectory.isDirectory()) {
                if (!defaultStyleSheetDirectory.mkdirs()) {
                    throw new InternalErrorException(
                            "Failed to create directory \""
                            + defaultStyleSheetDirectory + "\"");
                }
            }

            CodeGeneratorUtilities.substitute(_templateDirectory
                    + "default.css", _substituteMap, defaultStyleSheetDirectory
                    .toString()
                    + "/default.css");
        } catch (Exception ex) {
            // This exception tends to get eaten by soot, so we print as well.
            System.err.println("Problem writing makefile or html files:" + ex);
            ex.printStackTrace();
            throw new InternalErrorException(null, ex,
                    "Problem writing the makefile or htm files");
        }
    }

    // Return a Map that maps classes to jar files for all the AtomicEntities
    // and Directors in the model
    private Map _allAtomicEntityJars() {
        HashMap results = new HashMap();
        Iterator atomicEntities = _model.allAtomicEntityList().iterator();

        while (atomicEntities.hasNext()) {
            Object object = atomicEntities.next();
            results.put(object.getClass().getName(), _getDomainJar(object
                    .getClass().getPackage().getName()));

            if (object instanceof AtomicActor) {
                // Add in the Managers.
                results.put(((AtomicActor) object).getDirector().getClass()
                        .getName(), _getDomainJar(((AtomicActor) object)
                        .getDirector().getClass().getPackage().getName()));
            }
        }

        return results;
    }

    // copy the model and remove the GeneratorTableau
    private static void _copyModelRemoveGeneratorTableau(URL modelPathURL,
            File newModelFile) throws Exception {
        // Create a parser.
        MoMLParser parser = new MoMLParser();

        // Since we strip out the graphical information in
        // Copernicus.readInModel(), we need to copy the original
        // model so that the vergil applet has the layout info.

        // So, we do reset here.  If we don't, then we get the old
        // model that was parsed in Copernicus.readInModel()
        parser.reset();
        MoMLParser.purgeAllModelRecords();

        // Get the old filters, save them, add our own
        // filters, use them, remove our filters,
        // and then readd the old filters in the finally clause.
        List oldFilters = MoMLParser.getMoMLFilters();
        MoMLParser.setMoMLFilters(null);

        // Parse the model and get the name of the model.
        try {
            // Handle Backward Compatibility.
            MoMLParser.addMoMLFilters(BackwardCompatibility.allFilters());

            // This is a bit of a misnomer, we remove only
            // GeneratorTableauAttribute here so that the Vergil applet has
            // the graphical classes
            RemoveGraphicalClasses removeGraphicalClasses = new RemoveGraphicalClasses();
            RemoveGraphicalClasses.clear();
            removeGraphicalClasses.put(
                    "ptolemy.copernicus.gui.GeneratorTableauAttribute", null);
            MoMLParser.addMoMLFilter(removeGraphicalClasses);

            // Parse the model.
            CompositeActor toplevel = null;
            toplevel = (CompositeActor) parser
                    .parse(modelPathURL, modelPathURL);

            FileWriter writer = null;

            try {
                writer = new FileWriter(newModelFile);
                toplevel.exportMoML(writer);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } finally {
            MoMLParser.setMoMLFilters(oldFilters);
        }
    }

    // If jarFile exists, optionally copy it and return true.
    // If jarFile does not exist, return false.
    private boolean _copyPotentialJarFile(String jarFile, String className,
            HashSet jarFilesThatHaveBeenRequired) throws IOException {
        File potentialSourceJarFile = new File(_ptIIJarsPath, jarFile);

        System.out.println("AppletWriter: className: " + className
                + "\tpotentialSourceJarFile: " + potentialSourceJarFile);

        if (potentialSourceJarFile.exists()) {
            jarFilesThatHaveBeenRequired.add(jarFile);

            if (_codeBase.equals(".")) {
                // If the codeBase is equal to the current directory,
                // we copy the jar file.
                // Ptolemy II development trees will have jar files
                // if 'make install' was run.
                String signedSourceFileName = _ptIIJarsPath + File.separator
                    + "signed" + File.separator + jarFile;
                File signedSourceFile = new File(signedSourceFileName);
                if (signedSourceFile.isFile()) {
                    _copyFile(signedSourceFileName,
                        _outputDirectory, jarFile);
                } else {
                    String sourceJarFileName = _ptIIJarsPath + File.separator + jarFile;
                    try {
                        // FIXME: this will try to sign the applet jar files.
                        // Is this right?  It means applets will have more permissions
                        _signJarFile (sourceJarFileName,
                                _outputDirectory + File.separator + jarFile);
                    } catch (Exception ex) {
                        System.out.println("Warning, could not sign \""
                                + sourceJarFileName + "\"");
                        ex.printStackTrace();
                        _copyFile(sourceJarFileName,
                                _outputDirectory, jarFile);
                    }

                }
            }

            return true;
        }

        return false;
    }

    /**
     * Copy the jar files listed in the map.
     * @param classMap A map consisting of String keys that are dot separated
     * class name and a value that is a String naming a jar file.
     * @param jarFilesThatHaveBeenRequired A set of strings that is set
     * to the names of the jar files that have been found.
     * @return true if there was a problem and the jar files need to be
     * fixed because "make install" was not run.
     */
    private boolean _copyJarFiles(Map classMap,
            HashSet jarFilesThatHaveBeenRequired) throws IOException {

        // Set to true if we need to fix up jar files because
        // jar files are not present probably because
        // 'make install' was not run.
        boolean fixJarFiles = false;

        Iterator classNames = classMap.entrySet().iterator();

        while (classNames.hasNext()) {
            Map.Entry entry = (Map.Entry) classNames.next();
            String className = (String) entry.getKey();

            if (jarFilesThatHaveBeenRequired.contains(classMap.get(className))) {
                // If we have already possibly copied the jar file, then skip
                continue;
            }

            if (!_copyPotentialJarFile((String) entry.getValue(), className,
                    jarFilesThatHaveBeenRequired)) {
                // The className could not be found in the classMap
                // Under Web Start, the resource that contains a class
                // will have a mangled name, so we copy the jar file.
                String classResource = ClassUtilities
                        .lookupClassAsResource(className);

                if (classResource == null) {
                    throw new IOException("Could not find '" + className
                            + "' as a resource.\n"
                            + "Try adding this class to the "
                            + "necessaryClasses parameter");
                }

                // Under Web Start, if there was a space in the
                // pathname, the space will have been converted to %20
                // but looking up a file will fail if the file has
                // a space in it and we are looking for a %20.
                classResource = StringUtilities.substitute(classResource,
                        "%20", " ");

                // We need to actually look up the file to deal with
                // the various C:/ptII, c:/ptII, c:\ptII, C:\ptII possibilities
                String canonicalClassResource = UtilityFunctions
                        .findFile(classResource);

                String canonicalPtIIDirectory = UtilityFunctions
                        .findFile(_ptIIJarsPath);

                if (canonicalClassResource.equals(canonicalPtIIDirectory)) {
                    // Failed to find the jar file.
                    // Look for a jar file under $PTII in the directory
                    // where the class is found.  If the class is foo.bar.biz,
                    // the we look for $PTII/foo/bar/bar.jar
                    String pathName = className.replace('.', '/');
                    String directoryName = pathName.substring(0, pathName
                            .lastIndexOf("/"));
                    String jarFileName = directoryName
                            + directoryName.substring(directoryName
                                    .lastIndexOf("/")) + ".jar";

                    if (_copyPotentialJarFile(jarFileName, className,
                            jarFilesThatHaveBeenRequired)) {
                    } else {
                        String warning = "Looking up '" + className
                                + "'\nreturned the $PTII directory '"
                                + _ptIIJarsPath
                                + "' instead of a jar file.\n'" + jarFileName
                                + "' was not present?\n";

                        if (_codeBase.equals(".")) {
                            // We only need print an error message if
                            // we are actually trying to copy the file
                            throw new IOException(warning
                                    + "Since the applet directory is not "
                                    + "inside the Ptolemy II tree, we need "
                                    + "to have\n"
                                    + "access to the jar files. If the jar "
                                    + "files are not present, then we cannot"
                                    + "copy\n"
                                    + "them to the new location and the java "
                                    + "classes will not be found by the "
                                    + "applet.\n"
                                    + "One solution is to run \" cd $PTII;"
                                    + "make install\" to create the jar files."
                                    + "\nAnother solution is to place the "
                                    + "applet directory under the\nPtolemy II "
                                    + "directory.");
                        } else {
                            // Print it so that the user knows that running
                            // make install would be a good job
                            System.out.println("Warning: " + warning
                                    + "Perhaps you need to run "
                                    + "'make install' to create the "
                                    + "jar files?"
                                    + "\nIf the jar files are not "
                                    + "present, then the archive "
                                    + "applet parameter will not "
                                    + "include all of the jar files.");
                            fixJarFiles = true;
                            continue;
                        }
                    }
                }

                //                 System.out.println("AppletWriter: "
                //                         + "\n\tclassResource:    " + classResource
                //                         + "\n\t_outputDirectory: " + _outputDirectory
                //                         + "\n\tclassName:        " + className
                //                         + "\n\tclassMap.get():   "
                //                         + (String)classMap.get(className));
                //                 jarFilesThatHaveBeenRequired
                //                     .add((String)classMap.get(className));
                //                 if (_codeBase.equals(".")) {
                //                     // If the codeBase is equal to the current directory,
                //                     // we copy the jar file.
                //                     _copyFile(classResource, _outputDirectory,
                //                             (String)classMap.get(className));
                //                 }
            }
        }
        return fixJarFiles;
    }

    // Copy sourceFile to the destinationFile in destinationDirectory.
    private void _copyFile(String sourceFileName, String destinationDirectory,
            String destinationFileName) throws IOException {
        File sourceFile = new File(sourceFileName);

        if (!sourceFile.isFile()) {
            throw new FileNotFoundException("'" + sourceFileName
                    + "' is not a file or cannot be found."
                    + "\nPerhaps you need " + "to run 'make install'?");
        }

        File destinationFile = new File(destinationDirectory,
                destinationFileName);
        File destinationParent = new File(destinationFile.getParent());
        if (!destinationParent.exists()) {
            if (!destinationParent.mkdirs()) {
                throw new IOException("Failed to make directory \""
                        + destinationParent.getName() + "\"");
            }
        }

        System.out.println("AppletWriter: Copying " + sourceFile + " ("
                + (sourceFile.length() / 1024) + "K) to " + destinationFile);

        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(new FileInputStream(sourceFile));

            BufferedOutputStream out = null;

            try {
                out = new BufferedOutputStream(new FileOutputStream(
                        destinationFile));

                int c;

                // Avoid end of line and localization issues.
                while ((c = in.read()) != -1) {
                    out.write(c);
                }
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on '" + destinationFile + "'");
                        throwable.printStackTrace();
                    }
                }
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable throwable) {
                    System.out.println("Ignoring failure to close stream "
                            + "on '" + sourceFile + "'");
                    throwable.printStackTrace();
                }
            }
        }
    }

    /** Return the file size as a JNLP file attribute
     */
    private String _jarFileLengthAttribute(String fileName) throws IOException {
        String fileLength = "";
        try {
            File file = new File(fileName);
            //System.out.println("AppletWriter: " + fileName + " " + file.length());
            return "\n             size=\"" + file.length() + "\"";
        } catch (Exception ex) {
            System.out.println("Warning, could not find size of \"" + fileName
                               + "\" : " + ex.getMessage());
        }
        return "";
    }

    // find jar necessary jar files and optionally copy jar files into
    // _outputDirectory.  Note that we need look for jar files to find
    // diva.jar if we are using the FSM domain.
    // Return the jar files that have been copied
    private Set _findModelJarFiles(Director director) throws IOException {
        // In the perfect world, we would run tree shaking here, or
        // look up classes as resources.  However, if we are running
        // in a devel tree, then the ptII directory will be returned
        // as the resource when we look up a class, which is not
        // at all what we want.
        // appletviewer -J-verbose could be used for tree shaking.
        // We use a HashMap that maps class names to destination jar
        // files.
        Map classMap = _allAtomicEntityJars();


        // Create a map of classes and their dependencies
        Map auxiliaryJarMap = new HashMap();
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtBeta", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtBinomial", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtBinomialSelector",
                "lib/ptcolt.jar");
        auxiliaryJarMap
                .put("ptolemy.actor.lib.colt.ColtBreitWigner", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtChiSquare", "lib/ptcolt.jar");
        auxiliaryJarMap
                .put("ptolemy.actor.lib.colt.ColtExponential", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtExponentialPower",
                "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtGamma", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtHyperGeometric",
                "lib/ptcolt.jar");
        auxiliaryJarMap
                .put("ptolemy.actor.lib.colt.ColtLogarithmic", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtNegativeBinomial",
                "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtNormal", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtPoisson", "lib/ptcolt.jar");
        auxiliaryJarMap
                .put("ptolemy.actor.lib.colt.ColtPoissonSlow", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtRandomSource",
                "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtSeedParameter",
                "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtStudentT", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtVonMises", "lib/ptcolt.jar");
        auxiliaryJarMap.put("ptolemy.actor.lib.colt.ColtZeta", "lib/ptcolt.jar");

        classMap.put("ptolemy.actor.gui.MoMLApplet", "ptolemy/ptsupport.jar");

        // Ptalon requires multiple jar files
        String ptalonJar = "ptolemy/actor/ptalon/ptalon.jar";
        auxiliaryJarMap.put("ptolemy.actor.ptalon.PtalonActor", ptalonJar);

        // auxiliaryJarMap.put("ptolemy.actor.lib.python.PythonScript",
        //                 "lib/jython.jar");
        // auxiliaryJarMap.put("caltrop.ptolemy.actors.CalInterpreter",
        //                 "lib/ptCal.jar");
        classMap.put(director.getClass().getName(), _domainJar);

        // database requires multiple jars
        String databaseJar = "ptolemy/actor/lib/database/database.jar";
        auxiliaryJarMap.put("ptolemy.actor.lib.database.DatabaseManager",
                databaseJar);

        // classes from domains/space
        // domains/space requires multiple jars
        String spaceJar = "ptolemy/domains/space/space.jar";
        auxiliaryJarMap.put("ptolemy.domains.space.Occupants",
                spaceJar);
        auxiliaryJarMap.put("ptolemy.domains.space.Region",
                spaceJar);
        auxiliaryJarMap.put("ptolemy.domains.space.Room",
                spaceJar);

        // If classMap has any keys that match keys in auxiliaryJarMap,
        // then add the corresponding key and value;
        Map jarsToAdd = null;
        Iterator classNames = classMap.entrySet().iterator();

        while (classNames.hasNext()) {
            Map.Entry entry = (Map.Entry) classNames.next();
            String className = (String) entry.getKey();
            if (auxiliaryJarMap.containsKey(className)) {
                if (jarsToAdd == null) {
                    jarsToAdd = new HashMap();
                }
                jarsToAdd.put(className, auxiliaryJarMap.get(className));

            }
        }
        if (jarsToAdd != null) {
            classMap.putAll(jarsToAdd);
        }

        // First, we search for the jar file, then we try
        // getting the class as a resource.
        // FIXME: we don't handle the case where there are no
        // individual jar files because the user did not run 'make install'.

        HashSet jarFilesThatHaveBeenRequired = new HashSet();

        // Add jar files that are contained in ptsupport.jar.
        // FIXME: we could open ptsupport.jar here and get the complete
        // list of directories.  Instead, we get the primary offenders.
        jarFilesThatHaveBeenRequired.add("ptolemy/actor/actor.jar");
        jarFilesThatHaveBeenRequired.add("ptolemy/actor/lib/lib.jar");

        boolean fixJarFiles = _copyJarFiles(classMap,
                jarFilesThatHaveBeenRequired);

        jarFilesThatHaveBeenRequired.remove("ptolemy/actor/actor.jar");
        jarFilesThatHaveBeenRequired.remove("ptolemy/actor/lib/lib.jar");

        File potentialDomainJarFile = new File(_ptIIJarsPath, _domainJar);

        if (!potentialDomainJarFile.exists()) {
            // If we are running under the Windows installer, then
            // the domain specific jar files might not be present
            // so we add ptolemy/domains/domains.jar
            // We don't always require domains.jar because if
            // the domain specific jar file is present, then the
            // domain specific jar file will be much smaller.
            System.out.println("AppletWriter: Warning: could not find '"
                    + _domainJar + "', '" + potentialDomainJarFile
                    + "' does not exist, " + "adding domains.jar to jarfiles");
            jarFilesThatHaveBeenRequired.add("ptolemy/domains/domains.jar");
        }


        // Certain jar files require other jar files, so we add
        // them to auxliaryClassMap and copy them.
        Map auxiliaryClassMap = new HashMap();
        if (jarFilesThatHaveBeenRequired.contains(ptalonJar)) {
            // Ptalon requires multiple jar files
            auxiliaryClassMap.put("ptalon jar needs antlr jar",
                    "ptolemy/actor/ptalon/antlr/antlr.jar");
        }

        // actor.lib.database and domains.space require ojdbc6.jar
        if (jarFilesThatHaveBeenRequired.contains(databaseJar)) {
            auxiliaryClassMap.put("database jar needs ojdbc6 jar",
                    "ptolemy/actor/lib/database/ojdbc6.jar");
        } else if (jarFilesThatHaveBeenRequired.contains(spaceJar)) {
            auxiliaryClassMap.put("space jar needs ojdbc6 jar",
                    "ptolemy/actor/lib/database/ojdbc6.jar");
        }

        boolean fixAuxiliaryJarFiles = _copyJarFiles(auxiliaryClassMap,
                jarFilesThatHaveBeenRequired);

        if (fixJarFiles || fixAuxiliaryJarFiles) {
            // If the code generator was run but codeBase != . and
            // make install was not run, then we will not have figured
            // out very many jar files.  So, we fix up the list
            //
            jarFilesThatHaveBeenRequired.add("ptolemy/ptsupport.jar");
            jarFilesThatHaveBeenRequired.add(_domainJar);
        }

        return jarFilesThatHaveBeenRequired;
    }


    // find jar necessary jar files and optionally copy jar files into
    // _outputDirectory.  Note that we need look for jar files to find
    // diva.jar if we are using the FSM domain.
    // Return the jar files that have been copied
    private Set _findVergilJarFiles(Director director) throws IOException {
        Map classMap = new HashMap();

        classMap.put("ptolemy.vergil.MoMLViewerApplet",
                "ptolemy/vergil/vergilApplet.jar");

        // FIXME: unfortunately, vergil depends on FSM now.
        classMap.put("ptolemy.domains.fsm.kernel.FSMActor",
                "ptolemy/domains/fsm/fsm.jar");

        // FIXME: vergil.fsm.modal.ModalModel depends on CTStepSizeControlActor
        classMap.put("ptolemy.domains.ct.kernel.CTStepSizeControlActor",
                "ptolemy/domains/ct/ct.jar");
        classMap.put("diva.graph.GraphController", "lib/diva.jar");

        HashSet jarFilesThatHaveBeenRequired = new HashSet();
        _copyJarFiles(classMap, jarFilesThatHaveBeenRequired);

        return jarFilesThatHaveBeenRequired;
    }

    // Given a domain package, return the corresponding jar file
    private static String _getDomainJar(String domainPackage) {
        String domainPackageDomain = domainPackage.substring(0, domainPackage
                .lastIndexOf("."));

        String domainDomain = domainPackageDomain.substring(domainPackageDomain
                .lastIndexOf(".") + 1);

        return StringUtilities.substitute(domainPackageDomain, ".", "/") + "/"
                + domainDomain + ".jar";
    }

    /** Sign a jar file.
     */
    private static void _signJarFile(String jarFileName, String signedJarFileName) throws Exception {
        // FIXME: Hardwired paths and passwords here.
        String keystoreFileName = StringUtilities.getProperty("ptolemy.ptII.dir")
            + File.separator + "ptKeystore";
        char[] storePassword = {'t', 'h', 'i', 's', '.', 'i', 's', '.', 't', 'h', 'e', '.', 's', 't', 'o', 'r', 'e', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd', ',', 'c', 'h', 'a', 'n', 'g', 'e', '.', 'i', 't'};
        char[] keyPassword = {'t', 'h', 'i', 's', '.', 'i', 's', '.', 't', 'h', 'e', '.', 'k', 'e', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd', ',', 'c', 'h', 'a', 'n', 'g', 'e', '.', 'i', 't'};
        String alias = "claudius";

        System.out.println("About to sign \"" + jarFileName + "\" and create \""
                + signedJarFileName + "\"");
        JarSigner.sign (jarFileName, signedJarFileName,
                keystoreFileName, alias, storePassword, keyPassword);
    }

    /** Create a jar file.
     *        Based on http://www.java2s.com/Code/Java/File-Input-Output/CreateJarfile.htm
     */
    private void _createJarFile(File jarFile, String [] jarFileNames, File[] filesToBeJared)
        throws Exception {

        byte buffer[] = new byte[1024];

        // Open archive file
        FileOutputStream outputStream = null;
        JarOutputStream jarOutputStream = null;

        try {
            outputStream = new FileOutputStream(jarFile);
            jarOutputStream = new JarOutputStream(outputStream, new Manifest());

            for (int i = 0; i < filesToBeJared.length; i++) {
                if (filesToBeJared[i] == null || !filesToBeJared[i].exists()
                    || filesToBeJared[i].isDirectory()) {
                    continue; // Just in case...
                }
                System.out.println("Adding " + filesToBeJared[i].getName());

                // Add archive entry
                JarEntry jarEntry = new JarEntry(jarFileNames[i]);
                jarEntry.setTime(filesToBeJared[i].lastModified());
                jarOutputStream.putNextEntry(jarEntry);

                // Write file to archive
                FileInputStream in = null;
                try {
                    in = new FileInputStream(filesToBeJared[i]);
                    while (true) {
                        int nRead = in.read(buffer, 0, buffer.length);
                        if (nRead <= 0) {
                            break;
                        }
                        jarOutputStream.write(buffer, 0, nRead);
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
        } finally {
            if (jarOutputStream != null) {
                try {
                    jarOutputStream.close();
                } catch (IOException ex) {
                    System.out.println("Failed to close \"" + jarFile.getCanonicalPath() + "\"");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The relative path to $PTII, for example "../../..".
    private String _codeBase = null;

    // The path to the jar file containing the domain classes,
    // for example "ptolemy/domains/sdf/sdf.jar".
    private String _domainJar;

    // The model we are generating code for.
    private CompositeActor _model;

    // The jar files that are necessary to run the model if the codebase
    // is ".".
    // For example: "ptolemy/ptsupport.jar,ptolemy/domains/sdf/sdf.jar".
    private String _modelJarFiles;

    // URL that names the model.
    private String _modelPath;

    // The full path to the directory where we are creating the model
    private String _outputDirectory;

    // The sanitized modelName
    private String _sanitizedModelName;

    // The location of the Ptolemy II jars, which defaults to
    // the value of the ptolemy.ptII.dir property, but could be
    // $PTII/signed.
    private String _ptIIJarsPath;

    // The top level directory in which to write code.  The default value is the value
    // of the ptolemy.ptII.dir Java sytem property.  The code app
    private String _ptIIUserDirectory;

    // Map used to map @model@ to MyModel.
    private Map _substituteMap;

    // The parent package relative to $PTII to generate the code in
    // The code itself is generated in a child package of the parent package
    // with the same name as the model.  So if the _targetPackage
    // is foo.bar, and the model is MyModel, we will create the code
    // in foo.bar.MyModel.
    private String _targetPackage;

    // The path relative to the ptIIUserDirectory to generate code in.
    // Defaults to ptolemy/copernicus/$codeGenerator/cg/$modelName
    private String _targetPath;

    // The directory that contains the templates (makefile.in,
    // model.htm.in, modelApplet.htm.in)
    private String _templateDirectory;

    // Initial default for _templateDirectory;
    private static final String TEMPLATE_DIRECTORY_DEFAULT = "ptolemy/copernicus/applet/";

    // The jar files that are necessary to run vergil if the codebase
    // is ".".
    // For example:
    // "lib/diva.jar,ptolemy/domains/fsm/fsm.jar,ptolemy/domains/ct/ct.jar,ptolemy/vergil/vergilApplet.jar"
    private String _vergilJarFiles;

    // The jar files that are necessary for the jnlp file.
    private String _jnlpJars;
}
