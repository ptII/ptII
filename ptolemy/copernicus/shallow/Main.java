/* Main for shallow code generation.

 Copyright (c) 2001-2009 The Regents of the University of California.
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
package ptolemy.copernicus.shallow;

import ptolemy.copernicus.kernel.ClassWriter;
import ptolemy.copernicus.kernel.GeneratorAttribute;
import ptolemy.copernicus.kernel.GrimpTransformer;
import ptolemy.copernicus.kernel.InvocationBinder;
import ptolemy.copernicus.kernel.JimpleWriter;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.copernicus.kernel.TransformerAdapter;
import ptolemy.copernicus.kernel.WatchDogTimer;
import soot.Pack;
import soot.PackManager;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.scalar.LocalSplitter;

//////////////////////////////////////////////////////////////////////////
//// Main

/**
 Read in a MoML model and generate a Java class that creates the
 same model.  (i.e. shallow code generation)
 No attempt is made to analyze actor code.  This is primarily
 useful for using the Java compiler to find bugs, and removing
 MoML from shipped code.

 @author Stephen Neuendorffer, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Main extends KernelMain {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add transforms to the Scene.
     */
    public void addTransforms() {
        Pack pack = PackManager.v().getPack("wjtp");

        // Set up a watch dog timer to exit after a certain amount of time.
        // For example, to time out after 5 minutes, or 300000 ms:
        // -p wjtp.watchDog time:30000
        addTransform(pack, "wjtp.watchDog", WatchDogTimer.v());

        // Generate the makefile files in outputDirectory
        addTransform(pack, "wjtp.makefileWriter", MakefileWriter.v(_toplevel),
                "_generatorAttributeFileName:" + _generatorAttributeFileName
                        + " targetPackage:" + _targetPackage
                        + " templateDirectory:" + _templateDirectory
                        + " outputDirectory:" + _outputDirectory);

        // Create a class for the composite actor of the model
        addTransform(pack, "wjtp.mt", ShallowModelTransformer.v(_toplevel),
                "targetPackage:" + _targetPackage);

        addTransform(pack, "wjtp.ls7",
                new TransformerAdapter(LocalSplitter.v()));

        addTransform(pack, "wjtp.ta5", new TransformerAdapter(TypeAssigner.v()));
        addTransform(pack, "wjtp.ib3", InvocationBinder.v());

        // Run the standard soot optimizations.  We explicitly specify
        // this instead of using soot's -O flag so that we can
        // have access to the result.
        addStandardOptimizations(pack, 1);

        // Convert to grimp.
        addTransform(pack, "wjtp.gt", GrimpTransformer.v());

        /*   */
        // This snapshot should be last...
        addTransform(pack, "wjtp.finalSnapshotJimple", JimpleWriter.v(),
                "outputDirector:" + _outputDirectory);
        addTransform(pack, "wjtp.finalSnapshot", ClassWriter.v(),
                "outputDirectory:" + _outputDirectory);

        // Disable the watch dog timer
        addTransform(pack, "wjtp.watchDogCancel", WatchDogTimer.v(),
                "cancel:true");
    }

    /** Parse any code generator specific arguments.
     */
    protected String[] _parseArgs(GeneratorAttribute attribute)
            throws Exception {
        _targetPackage = attribute.getParameter("targetPackage");
        _templateDirectory = attribute.getParameter("templateDirectory");
        //_watchDogTimeout = attribute.getParameter("watchDogTimeout");
        _outputDirectory = attribute.getParameter("outputDirectory");
        _generatorAttributeFileName = attribute
                .getParameter("generatorAttributeFileName");

        //String sootArgs = attribute.getParameter("sootArgs");
        return new String[0];
    }

    private static String _generatorAttributeFileName = "unsetParameter";

    //private static String _watchDogTimeout = "unsetParameter";

    private static String _targetPackage = "unsetParameter";

    private static String _templateDirectory = "ptolemy/copernicus/shallow";

    private static String _outputDirectory = "unsetParameter";
}
