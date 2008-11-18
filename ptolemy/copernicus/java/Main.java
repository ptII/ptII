/* Transform Actors using Soot

 Copyright (c) 2001-2006 The Regents of the University of California.
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
package ptolemy.copernicus.java;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.ClassWriter;
import ptolemy.copernicus.kernel.GeneratorAttribute;
import ptolemy.copernicus.kernel.GrimpTransformer;
import ptolemy.copernicus.kernel.InvocationBinder;
import ptolemy.copernicus.kernel.JimpleWriter;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.LibraryUsageReporter;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.copernicus.kernel.SideEffectFreeInvocationRemover;
import ptolemy.copernicus.kernel.TransformerAdapter;
import ptolemy.copernicus.kernel.UnusedFieldRemover;
import ptolemy.copernicus.kernel.WatchDogTimer;
import soot.Pack;
import soot.PackManager;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.scalar.LocalSplitter;

//////////////////////////////////////////////////////////////////////////
//// Main

/**
 Read in a MoML model and generate Java classes for that model.

 @author Stephen Neuendorffer, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Main extends KernelMain {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add to the scene a standard set of transformations that are useful
     *  for optimizing efficiency.
     *  @param toplevel The composite actor we are generating code for.
     */
    public static void addStandardTransforms(CompositeActor toplevel) {
        Pack pack = PackManager.v().getPack("wjtp");

        // Set up a watch dog timer to exit after a certain amount of time.
        // For example, to time out after 5 minutes, or 300000 ms:
        // -p wjtp.watchDog time:30000
        addTransform(pack, "wjtp.watchDog", WatchDogTimer.v(), "time:"
                + _watchDogTimeout);

        // Create a class for composite actor of a model, and
        // additional classes for all actors (both composite and
        // atomic) used by the model.
        addTransform(pack, "wjtp.mt", ModelTransformer.v(toplevel),
                "targetPackage:" + _targetPackage);

        // Inline director into composite actors.
        addTransform(pack, "wjtp.idt", InlineDirectorTransformer.v(toplevel),
                "targetPackage:" + _targetPackage + " outDir:"
                        + _outputDirectory);

        // Add a command line interface (i.e. Main)
        addTransform(pack, "wjtp.clt", CommandLineTransformer.v(toplevel),
                "targetPackage:" + _targetPackage);

        addTransform(pack, "wjtp.ta1", new TransformerAdapter(TypeAssigner.v()));
        addStandardOptimizations(pack, 1);

        if (_snapshots) {
            addTransform(pack, "wjtp.snapshot1jimple", JimpleWriter.v(),
                    "outDir:" + _outputDirectory + "/jimple1");
            addTransform(pack, "wjtp.snapshot1", ClassWriter.v(), "outDir:"
                    + _outputDirectory + "/jimple1");
            addTransform(pack, "wjtp.lur1", LibraryUsageReporter.v(),
                    "outFile:" + _outputDirectory + "/jimple1/jarClassList.txt");
        }

        addTransform(pack, "wjtp.ib1", InvocationBinder.v());

        addTransform(pack, "wjtp.ls7",
                new TransformerAdapter(LocalSplitter.v()));
        addTransform(pack, "wjtp.ffet", FieldsForEntitiesTransformer
                .v(toplevel), "targetPackage:" + _targetPackage);

        // Infer the types of locals again, since replacing attributes
        // depends on the types of fields
        addTransform(pack, "wjtp.ta2", new TransformerAdapter(TypeAssigner.v()));
        addTransform(pack, "wjtp.cie1", new TransformerAdapter(
                CastAndInstanceofEliminator.v()));
        addStandardOptimizations(pack, 2);

        // In each actor and composite actor, ensure that there
        // is a field for every attribute, and replace calls
        // to getAttribute with references to those fields.
        addTransform(pack, "wjtp.ffat1", FieldsForAttributesTransformer
                .v(toplevel), "targetPackage:" + _targetPackage);

        // In each actor and composite actor, ensure that there
        // is a field for every port, and replace calls
        // to getPort with references to those fields.
        addTransform(pack, "wjtp.ffpt", FieldsForPortsTransformer.v(toplevel),
                "targetPackage:" + _targetPackage);

        addTransform(pack, "wjtp.ls2",
                new TransformerAdapter(LocalSplitter.v()));
        addTransform(pack, "wjtp.lns", new TransformerAdapter(
                LocalNameStandardizer.v()));

        addStandardOptimizations(pack, 3);

        addTransform(pack, "wjtp.ls3",
                new TransformerAdapter(LocalSplitter.v()));
        addTransform(pack, "wjtp.ta3", new TransformerAdapter(TypeAssigner.v()));
        addTransform(pack, "wjtp.ib2", InvocationBinder.v());

        // Remove casts and instanceof Checks.
        addTransform(pack, "wjtp.cie2", new TransformerAdapter(
                CastAndInstanceofEliminator.v()));

        addStandardOptimizations(pack, 4);

        addTransform(pack, "wjtp.rcp", ReplaceComplexParameters.v(toplevel),
                "targetPackage:" + _targetPackage);

        //        addTransform(pack, "wjtp.umr0", UnreachableMethodRemover.v(), "debug:true");

        addTransform(pack, "wjtp.cs", ConstructorSpecializer.v(toplevel),
                "targetPackage:" + _targetPackage);

        // Infer the types of locals again, since replacing attributes
        // depends on the types of fields
        addTransform(pack, "wjtp.ta12",
                new TransformerAdapter(TypeAssigner.v()));

        addTransform(pack, "wjtp.cie21", new TransformerAdapter(
                CastAndInstanceofEliminator.v()));

        // Set about removing reference to attributes and parameters.
        // Anywhere where a method is called on an attribute or
        // parameter, replace the method call with the return value
        // of the method.  This is possible, since after
        // initialization attribute values are assumed not to
        // change.  (Note: There are certain cases where this is
        // not true, i.e. the expression actor.  Those will be
        // specially handled before this point, or we should detect
        // assignments to attributes and handle them differently.)
        addTransform(pack, "wjtp.iat", InlineParameterTransformer.v(toplevel),
                "targetPackage:" + _targetPackage);

        // Remove equality checks, which arise from inlining attributeChanged.
        //  pack.add(
        //                 new Transform("wjtp.ta",
        //                         new TransformerAdapter(TypeAssigner.v()));
        //         pack.add(
        //                 new Transform("wjtp.nee",
        //                         NamedObjEqualityEliminator.v(toplevel));
        // Anywhere we have a method call on a token that can be
        // statically evaluated (usually, these will have been
        // created by inlining parameters), inline those calls.
        // We do this before port transformation, since it
        // often allows us to statically determine the channels
        // of port reads and writes.
        addTransform(pack, "wjtp.itt1", InlineTokenTransformer.v(toplevel),
                "targetPackage:" + _targetPackage);

        addTransform(pack, "wjtp.ls4",
                new TransformerAdapter(LocalSplitter.v()));

        // While we still have references to ports, use the
        // resolved types of the ports and run a typing
        // algorithm to specialize the types of type
        // polymorphic actors.  After this step, no
        // uninstantiable types should remain.
        addTransform(pack, "wjtp.tie", new TransformerAdapter(
                TokenInstanceofEliminator.v()));
        addTransform(pack, "wjtp.ta4", new TransformerAdapter(TypeAssigner.v()));

        addTransform(pack, "wjtp.cp1", new TransformerAdapter(CopyPropagator
                .v()));
        addStandardOptimizations(pack, 5);

        if (_snapshots) {
            addTransform(pack, "wjtp.snapshot2jimple", JimpleWriter.v(),
                    "outDir:" + _outputDirectory + "/jimple2");
            addTransform(pack, "wjtp.snapshot2", ClassWriter.v(), "outDir:"
                    + _outputDirectory + "/jimple2");
            addTransform(pack, "wjtp.lur2", LibraryUsageReporter.v(),
                    "outFile:" + _outputDirectory + "/jimple2/jarClassList.txt");
        }

        // Set about removing references to ports.
        // Anywhere where a method is called on a port, replace the
        // method call with an inlined version of the method.
        // Currently this only deals with SDF, and turns
        // all gets and puts into reads and writes from circular
        // buffers.
        addTransform(pack, "wjtp.ipt", InlinePortTransformer.v(toplevel),
                "targetPackage:" + _targetPackage);

        // This appears again because Inlining the parameters
        // also inlines calls to connectionsChanged, which by default
        // calls getDirector...  This transformer removes
        // these method calls.
        // FIXME: This should be done in a better way...
        addTransform(pack, "wjtp.ffat2", FieldsForAttributesTransformer
                .v(toplevel), "targetPackage:" + _targetPackage);

        // Deal with any more statically analyzeable token
        // references that were created.
        addTransform(pack, "wjtp.itt2", InlineTokenTransformer.v(toplevel),
                "targetPackage:" + _targetPackage);

        //pack.add(new Transform("wjtp.ta",
        //        new TransformerAdapter(TypeAssigner.v()));
        // pack.add(new Transform("wjtp.ibg",
        //        InvokeGraphBuilder.v());
        // pack.add(new Transform("wjtp.si",
        //        StaticInliner.v());
        if (_snapshots) {
            addTransform(pack, "wjtp.snapshot3jimple", JimpleWriter.v(),
                    "outDir:" + _outputDirectory + "/jimple3");
            addTransform(pack, "wjtp.snapshot3", ClassWriter.v(), "outDir:"
                    + _outputDirectory + "/jimple3");
            addTransform(pack, "wjtp.lur3", LibraryUsageReporter.v(),
                    "outFile:" + _outputDirectory + "/jimple3/jarClassList.txt");
        }

        // Unroll loops with constant loop bounds.
        //Scene.v().getPack("jtp").add(new Transform("jtp.clu",
        //        ConstantLoopUnroller.v());
        //     _addStandardOptimizations(pack, 5);
        addTransform(pack, "wjtp.ls5",
                new TransformerAdapter(LocalSplitter.v()));
        addTransform(pack, "wjtp.ta5", new TransformerAdapter(TypeAssigner.v()));
        addTransform(pack, "wjtp.ib3", InvocationBinder.v());

        addTransform(pack, "wjtp.nee", NamedObjEqualityEliminator.v(toplevel),
                "targetPackage:" + _targetPackage);

        // Remove casts and instanceof Checks.
        addTransform(pack, "wjtp.cie3", new TransformerAdapter(
                CastAndInstanceofEliminator.v()));

        addTransform(pack, "wjtp.ta7", new TransformerAdapter(TypeAssigner.v()));
        addStandardOptimizations(pack, 6);

        // Remove Unreachable methods.  This happens BEFORE
        // NamedObjElimination so that we don't have to pick between
        // multiple constructors, if there are more than one.  I'm
        // lazy and instead of trying to pick one, lets use the only
        // one that is reachable.
        addTransform(pack, "wjtp.umr1", UnreachableMethodRemover.v());

        // Remove references to named objects.
        addTransform(pack, "wjtp.ee1", ExceptionEliminator.v(toplevel));
        addTransform(pack, "wjtp.ls6",
                new TransformerAdapter(LocalSplitter.v()));
        addTransform(pack, "wjtp.cie4", new TransformerAdapter(
                CastAndInstanceofEliminator.v()));
        addTransform(pack, "wjtp.ta6", new TransformerAdapter(TypeAssigner.v()));
        addTransform(pack, "wjtp.ib4", InvocationBinder.v());

        addTransform(pack, "wjtp.noe", NamedObjEliminator.v(toplevel));

        addTransform(pack, "wjtp.umr2", UnreachableMethodRemover.v());

        // Some cleanup.
        // Remove object creations that are now dead (i.e. aren't used
        // and have no side effects).  This currently only deals with
        // Token and Type constructors, since we know that these will
        // have no interesting side effects.  More complex analysis
        // is possible here, but not likely worth it.
        addTransform(pack, "wjtp.doe1", new TransformerAdapter(
                DeadObjectEliminator.v()));
        addTransform(pack, "wjtp.dae1", new TransformerAdapter(
                DeadAssignmentEliminator.v()));
        addTransform(pack, "wjtp.doe2", new TransformerAdapter(
                DeadObjectEliminator.v()));
        addTransform(pack, "wjtp.dae2", new TransformerAdapter(
                DeadAssignmentEliminator.v()));
        addTransform(pack, "wjtp.doe3", new TransformerAdapter(
                DeadObjectEliminator.v()));
        addStandardOptimizations(pack, 7);

        if (_snapshots) {
            addTransform(pack, "wjtp.snapshot4jimple", JimpleWriter.v(),
                    "outDir:" + _outputDirectory + "/jimple4");
            addTransform(pack, "wjtp.snapshot4", ClassWriter.v(), "outDir:"
                    + _outputDirectory + "/jimple4");
            addTransform(pack, "wjtp.lur4", LibraryUsageReporter.v(),
                    "outFile:" + _outputDirectory + "/jimple4/jarClassList.txt");
        }

        if (_unboxing) {
            addTransform(pack, "wjtp.ttn", TokenToNativeTransformer.v(toplevel)); //, "debug:true level:2");

            addStandardOptimizations(pack, 8);

            addTransform(pack, "wjtp.ufr", UnusedFieldRemover.v());

            addTransform(pack, "wjtp.smr", SideEffectFreeInvocationRemover.v());

            // Remove references to named objects.
            addTransform(pack, "wjtp.ee2", ExceptionEliminator.v(toplevel));

            addTransform(pack, "wjtp.doe4", new TransformerAdapter(
                    DeadObjectEliminator.v()));
            addStandardOptimizations(pack, 9);

            addTransform(pack, "wjtp.smr2", SideEffectFreeInvocationRemover.v());

            addTransform(pack, "wjtp.ffu", FinalFieldUnfinalizer.v());
            addTransform(pack, "wjtp.umr3", UnreachableMethodRemover.v());
            addTransform(pack, "wjtp.cp2", new TransformerAdapter(
                    CopyPropagator.v()));
            addTransform(pack, "wjtp.ufr2", UnusedFieldRemover.v());
            addStandardOptimizations(pack, 10);
            addTransform(pack, "wjtp.doe5", new TransformerAdapter(
                    DeadObjectEliminator.v()));
            addStandardOptimizations(pack, 11);
            addTransform(pack, "wjtp.doe6", new TransformerAdapter(
                    DeadObjectEliminator.v()));
            addTransform(pack, "wjtp.cie5", new TransformerAdapter(
                    CastAndInstanceofEliminator.v()));
            addStandardOptimizations(pack, 12);
            addTransform(pack, "wjtp.doe7", new TransformerAdapter(
                    DeadObjectEliminator.v()));
            addTransform(pack, "wjtp.cie6", new TransformerAdapter(
                    CastAndInstanceofEliminator.v()));
            addStandardOptimizations(pack, 13);
            addTransform(pack, "wjtp.cie7", new TransformerAdapter(
                    CastAndInstanceofEliminator.v()));
            addStandardOptimizations(pack, 14);
            addStandardOptimizations(pack, 15);

            //             addTransform(pack, "wjtp.ptr1", PtolemyTypeRemover.v(toplevel));
            //             addStandardOptimizations(pack, 16);
            //             addTransform(pack, "wjtp.ufr3", UnusedFieldRemover.v());
            //             addTransform(pack, "wjtp.doe8", new TransformerAdapter(
            //                     DeadObjectEliminator.v()));
            //              addStandardOptimizations(pack, 17);

            // The library usage reporter also pulls in all depended
            // classes for analysis.
            addTransform(pack, "wjtp.lur", LibraryUsageReporter.v(), "outFile:"
                    + _outputDirectory + "/jarClassList.txt "
                    + "analyzeAllReachables:false");

            // Note: We want to analyze all reachables here!
            //        addTransform(pack, "wjtp.umr4",
            //                UnreachableMethodRemover.v());
            //        addTransform(pack, "wjtp.ufr3",
            //                UnusedFieldRemover.v());
            //        addStandardOptimizations(pack, 11);
            //        addTransform(pack, "wjtp.umr5",
            //                UnreachableMethodRemover.v());

        }

        /* */
    }

    /** Add transforms to the Scene.
     */
    public void addTransforms() {
        addStandardTransforms(_toplevel);

        Pack pack = PackManager.v().getPack("wjtp");

        // Convert to grimp.
        addTransform(pack, "wjtp.gt", GrimpTransformer.v());

        // This snapshot should be last...
        addTransform(pack, "wjtp.finalSnapshotJimple", JimpleWriter.v(),
                "outDir:" + _outputDirectory);
        addTransform(pack, "wjtp.finalSnapshot", ClassWriter.v(), "outDir:"
                + _outputDirectory);

        // And write C!
        //      pack.add(
        //                 new Transform("wjtp.finalSnapshot", CWriter.v());
        // Generate the makefile files in outDir
        addTransform(pack, "wjtp.makefileWriter", MakefileWriter.v(_toplevel),
                "_generatorAttributeFileName:" + _generatorAttributeFileName
                        + " targetPackage:" + _targetPackage
                        + " templateDirectory:" + _templateDirectory
                        + " outDir:" + _outputDirectory);

        addTransform(pack, "wjtp.watchDogCancel", WatchDogTimer.v(),
                "cancel:true");
    }

    /** Parse any code generator specific arguments.
     */
    protected String[] _parseArgs(GeneratorAttribute attribute)
            throws Exception {
        if (attribute.hasParameter("snapshots")) {
            String snapshots = attribute.getParameter("snapshots");

            if (snapshots.equals("true")) {
                _snapshots = true;
            } else {
                _snapshots = false;
            }
        }

        if (attribute.hasParameter("unboxing")) {
            String unboxing = attribute.getParameter("unboxing");

            if (unboxing.equals("false")) {
                _unboxing = false;
            } else {
                _unboxing = true;
            }
        }

        _targetPackage = attribute.getParameter("targetPackage");
        _templateDirectory = attribute.getParameter("templateDirectory");
        _watchDogTimeout = attribute.getParameter("watchDogTimeout");
        _outputDirectory = attribute.getParameter("outputDirectory");
        _generatorAttributeFileName = attribute
                .getParameter("generatorAttributeFileName");

        //  String sootArgs = attribute.getParameter("sootArgs");
        String sootArgs[] = new String[1];
        sootArgs[0] = "-debug-resolver";
        return sootArgs;
    }

    private static boolean _snapshots = false;

    private static boolean _unboxing = true;

    private static String _generatorAttributeFileName = "unsetParameter";

    private static String _watchDogTimeout = "unsetParameter";

    private static String _targetPackage = "unsetParameter";

    private static String _templateDirectory = "ptolemy/copernicus/java";

    private static String _outputDirectory = "unsetParameter";
}
