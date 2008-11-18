/* An interface for classes that replaces port methods.

 Copyright (c) 2001-2007 The Regents of the University of California.
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
 @ProposedRating Red (cxh@eecs.berkeley.edu)
 @AcceptedRating Red (cxh@eecs.berkeley.edu)
 */
package ptolemy.copernicus.java;

import java.io.FileWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.giotto.kernel.GiottoCodeGenerator;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;
import soot.ArrayType;
import soot.BooleanType;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Expr;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.typing.TypeResolver;
import soot.toolkits.scalar.LocalSplitter;
import soot.util.Chain;

//////////////////////////////////////////////////////////////////////////
//// GiottoDirectorInliner

/**

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 */
public class GiottoDirectorInliner implements DirectorInliner {
    public void inlineDirector(CompositeActor model, SootClass modelClass,
            String phaseName, Map options) throws IllegalActionException {
        // FIXME: what if giotto is someplace else?
        MakefileWriter.addMakefileSubstitution("@extraClassPath@",
                "$(CLASSPATHSEPARATOR)$(ROOT)/vendors/giotto/giotto.jar");

        GiottoPortInliner portInliner = new GiottoPortInliner(modelClass,
                model, options);
        InlinePortTransformer.setPortInliner(model, portInliner);

        // Create the Giotto communication buffers so we can reference
        // them here.
        portInliner.createBuffers();

        System.out.println("Inlining director for " + model.getFullName());

        Type actorType = RefType.v(PtolemyUtilities.actorClass);

        //         SootField postfireReturnsField = new SootField("_postfireReturns",
        //                 BooleanType.v(), Modifier.PRIVATE);
        //         modelClass.addField(postfireReturnsField);
        // First, write out Giotto Code for the model.
        String directory = PhaseOptions.getString(options, "outDir");

        try {
            GiottoCodeGenerator giottoCodeGenerator = new GiottoCodeGenerator();
            String giottoCode = giottoCodeGenerator
                    .generateGiottoCode((TypedCompositeActor) model);
            FileWriter writer = null;
            try {
                writer = new FileWriter(directory + "/" + model.getName()
                        + ".giotto");
                writer.write(giottoCode);
            } finally {
                writer.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        // Create a  static field,  so we can  get from  the generated
        // driver code back to the model.  NOTE: This prevents us from
        // having more than one instance of this generated code!
        SootField modelField = new SootField("_GiottoModel", RefType
                .v(modelClass), Modifier.PUBLIC | Modifier.STATIC);
        modelClass.addField(modelField);

        SootClass giottoParameterClass = Scene.v().loadClassAndSupport(
                "giotto.functionality.table.Parameter");
        SootClass giottoTokenPortVariableClass = Scene.v().loadClassAndSupport(
                "giotto.functionality.code.Token_port");
        SootMethod getPortVariableMethod = SootUtilities.searchForMethodByName(
                giottoParameterClass, "getPortVariable");
        SootMethod getPortVariableTokenMethod = SootUtilities
                .searchForMethodByName(giottoTokenPortVariableClass, "getToken");
        SootMethod setPortVariableTokenMethod = SootUtilities
                .searchForMethodByName(giottoTokenPortVariableClass, "setToken");
        SootMethod objectConstructor = SootUtilities.searchForMethodByName(
                PtolemyUtilities.objectClass, "<init>");
        SootClass serializationInterface = Scene.v().loadClassAndSupport(
                "java.io.Serializable");

        // Create a Task class for each actor in the model
        for (Iterator entities = model.deepEntityList().iterator(); entities
                .hasNext();) {
            Entity entity = (Entity) entities.next();
            String entityClassName = ModelTransformer.getInstanceClassName(
                    entity, options);
            SootClass entityClass = Scene.v().loadClassAndSupport(
                    entityClassName);
            //String fieldName = ModelTransformer.getFieldNameForEntity(entity,
            //        model);
            //SootField field = modelClass.getFieldByName(fieldName);

            String taskClassName = entityClassName + "_Task";
            SootClass taskInterface = Scene.v().loadClassAndSupport(
                    "giotto.functionality.interfaces.TaskInterface");
            SootMethod runInterface = taskInterface.getMethodByName("run");

            // create a class for the entity instance.
            SootClass taskClass = new SootClass(taskClassName, Modifier.PUBLIC);

            taskClass.setSuperclass(PtolemyUtilities.objectClass);
            taskClass.addInterface(taskInterface);
            taskClass.addInterface(serializationInterface);
            Scene.v().addClass(taskClass);
            taskClass.setApplicationClass();

            // Create a super constructor.
            PtolemyUtilities.createSuperConstructor(taskClass,
                    objectConstructor);
            // Implement the run method.
            {
                SootMethod runMethod = new SootMethod(runInterface.getName(),
                        runInterface.getParameterTypes(), runInterface
                                .getReturnType(), Modifier.PUBLIC);
                taskClass.addMethod(runMethod);

                JimpleBody body = Jimple.v().newBody(runMethod);
                runMethod.setActiveBody(body);
                body.insertIdentityStmts();

                Chain units = body.getUnits();

                Local localPostfireReturnsLocal = Jimple.v().newLocal(
                        "localPostfireReturns", BooleanType.v());
                body.getLocals().add(localPostfireReturnsLocal);

                Local actorLocal = Jimple.v().newLocal("actor", actorType);
                body.getLocals().add(actorLocal);

                Local modelLocal = Jimple.v().newLocal("model",
                        RefType.v(modelClass));
                body.getLocals().add(modelLocal);

                Local paramLocal = Jimple.v().newLocal("params",
                        RefType.v(giottoParameterClass));
                body.getLocals().add(paramLocal);

                Local portVarLocal = Jimple
                        .v()
                        .newLocal(
                                "portVar",
                                RefType
                                        .v("giotto.functionality.interfaces.PortVariable"));
                body.getLocals().add(portVarLocal);

                Local tokenPortVarLocal = Jimple.v().newLocal("tokenPortVar",
                        RefType.v(giottoTokenPortVariableClass));
                body.getLocals().add(tokenPortVarLocal);

                Local tokenLocal = Jimple.v().newLocal("token",
                        PtolemyUtilities.tokenType);
                body.getLocals().add(tokenLocal);

                Local bufferLocal = Jimple.v().newLocal("buffer",
                        ArrayType.v(PtolemyUtilities.tokenType, 1));
                body.getLocals().add(bufferLocal);

                SootMethod actorPrefireMethod = SootUtilities
                        .searchForMethodByName(entityClass, "prefire");
                SootMethod actorFireMethod = SootUtilities
                        .searchForMethodByName(entityClass, "fire");
                SootMethod actorPostfireMethod = SootUtilities
                        .searchForMethodByName(entityClass, "postfire");

                Stmt insertPoint = Jimple.v().newNopStmt();
                units.add(insertPoint);

                // Get a reference to the actor.
                units.insertBefore(Jimple.v().newAssignStmt(modelLocal,
                        Jimple.v().newStaticFieldRef(modelField.makeRef())),
                        insertPoint);
                units.insertBefore(Jimple.v().newAssignStmt(
                        actorLocal,
                        Jimple.v().newVirtualInvokeExpr(modelLocal,
                                PtolemyUtilities.getEntityMethod.makeRef(),
                                StringConstant.v(entity.getName()))),
                        insertPoint);

                // Copy the inputs...
                List inputPortList = ((Actor) entity).inputPortList();
                List outputPortList = ((Actor) entity).outputPortList();
                int inputCount = inputPortList.size();
                int outputCount = outputPortList.size();

                // Get the Parameter argument.
                units.insertBefore(Jimple.v().newAssignStmt(paramLocal,
                        body.getParameterLocal(0)), insertPoint);

                for (int i = 0; i < inputCount; i++) {
                    TypedIOPort port = (TypedIOPort) inputPortList.get(i);

                    // Get the port variable from the parameter.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            portVarLocal,
                            Jimple.v().newVirtualInvokeExpr(paramLocal,
                                    getPortVariableMethod.makeRef(),
                                    IntConstant.v(i))), insertPoint);

                    // Cast the port variable to the correct type.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            tokenPortVarLocal,
                            Jimple.v().newCastExpr(portVarLocal,
                                    RefType.v(giottoTokenPortVariableClass))),
                            insertPoint);

                    // Get the token from the port variable.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            tokenLocal,
                            Jimple.v().newVirtualInvokeExpr(tokenPortVarLocal,
                                    getPortVariableTokenMethod.makeRef())),
                            insertPoint);

                    // Get the buffer to put the token into.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            bufferLocal,
                            Jimple.v().newInstanceFieldRef(
                                    actorLocal,
                                    portInliner.getBufferField(port,
                                            port.getType()).makeRef())),
                            insertPoint);

                    // Store the token.
                    units
                            .insertBefore(Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(bufferLocal,
                                            IntConstant.v(0)), tokenLocal),
                                    insertPoint);
                }

                // Create the code to actually fire the actor.
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorPrefireMethod.makeRef())), insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorFireMethod.makeRef())), insertPoint);
                units.insertBefore(Jimple.v().newAssignStmt(
                        localPostfireReturnsLocal,
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorPostfireMethod.makeRef())), insertPoint);

                // The Parameter.
                units.insertBefore(Jimple.v().newAssignStmt(paramLocal,
                        body.getParameterLocal(0)), insertPoint);

                // Copy the outputs
                // FIXME! loop
                for (int i = 0; i < outputCount; i++) {
                    TypedIOPort port = (TypedIOPort) outputPortList.get(i);

                    // Get the buffer to retrieve the token from.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            bufferLocal,
                            Jimple.v().newInstanceFieldRef(
                                    actorLocal,
                                    portInliner.getBufferField(port,
                                            port.getType()).makeRef())),
                            insertPoint);

                    // Retrieve the token.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            tokenLocal,
                            Jimple.v().newArrayRef(bufferLocal,
                                    IntConstant.v(0))), insertPoint);

                    // Get the right output Port variable.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            portVarLocal,
                            Jimple.v().newVirtualInvokeExpr(paramLocal,
                                    getPortVariableMethod.makeRef(),
                                    IntConstant.v(inputCount + i))),
                            insertPoint);

                    // Cast to a Token port variable.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            tokenPortVarLocal,
                            Jimple.v().newCastExpr(portVarLocal,
                                    RefType.v(giottoTokenPortVariableClass))),
                            insertPoint);

                    // Set the token.
                    units.insertBefore(Jimple.v().newInvokeStmt(
                            Jimple.v().newVirtualInvokeExpr(tokenPortVarLocal,
                                    setPortVariableTokenMethod.makeRef(),
                                    tokenLocal)), insertPoint);
                }

                body.getUnits().add(Jimple.v().newReturnVoidStmt());
            }

            // For each output port in the actor, create an initial
            // value driver.
            List outputPortList = ((Actor) entity).outputPortList();
            int outputCount = outputPortList.size();

            for (int i = 0; i < outputCount; i++) {
                TypedIOPort port = (TypedIOPort) outputPortList.get(i);
                String portID = StringUtilities.sanitizeName(port
                        .getName(model));
                String driverClassName = PhaseOptions.getString(options,
                        "targetPackage")
                        + ".CG" + "init_" + portID;
                SootClass driverInterface = Scene.v().loadClassAndSupport(
                        "giotto.functionality.interfaces.DriverInterface");
                SootMethod driverRunInterface = driverInterface
                        .getMethodByName("run");

                // create a class for the entity instance.
                SootClass driverClass = new SootClass(driverClassName,
                        Modifier.PUBLIC);

                driverClass.setSuperclass(PtolemyUtilities.objectClass);
                driverClass.addInterface(driverInterface);
                driverClass.addInterface(serializationInterface);
                Scene.v().addClass(driverClass);
                driverClass.setApplicationClass();

                // Create a super constructor.
                PtolemyUtilities.createSuperConstructor(driverClass,
                        objectConstructor);

                // Implement the run method.
                SootMethod driverRunMethod = new SootMethod(driverRunInterface
                        .getName(), driverRunInterface.getParameterTypes(),
                        driverRunInterface.getReturnType(), Modifier.PUBLIC);
                driverClass.addMethod(driverRunMethod);

                JimpleBody body = Jimple.v().newBody(driverRunMethod);
                driverRunMethod.setActiveBody(body);
                body.insertIdentityStmts();

                Chain units = body.getUnits();

                Local actorLocal = Jimple.v().newLocal("actor", actorType);
                body.getLocals().add(actorLocal);

                Local modelLocal = Jimple.v().newLocal("model",
                        RefType.v(modelClass));
                body.getLocals().add(modelLocal);

                Local paramLocal = Jimple.v().newLocal("params",
                        RefType.v(giottoParameterClass));
                body.getLocals().add(paramLocal);

                Local portVarLocal = Jimple
                        .v()
                        .newLocal(
                                "portVar",
                                RefType
                                        .v("giotto.functionality.interfaces.PortVariable"));
                body.getLocals().add(portVarLocal);

                Local tokenPortVarLocal = Jimple.v().newLocal("tokenPortVar",
                        RefType.v(giottoTokenPortVariableClass));
                body.getLocals().add(tokenPortVarLocal);

                Stmt insertPoint = Jimple.v().newNopStmt();
                units.add(insertPoint);

                // Get a reference to the actor.
                units.insertBefore(Jimple.v().newAssignStmt(modelLocal,
                        Jimple.v().newStaticFieldRef(modelField.makeRef())),
                        insertPoint);
                units.insertBefore(Jimple.v().newAssignStmt(
                        actorLocal,
                        Jimple.v().newVirtualInvokeExpr(modelLocal,
                                PtolemyUtilities.getEntityMethod.makeRef(),
                                StringConstant.v(entity.getName()))),
                        insertPoint);

                Local initialValueLocal = Jimple.v()
                        .newLocal("initialValueAttribute",
                                PtolemyUtilities.attributeType);
                body.getLocals().add(initialValueLocal);

                Local initialValueVariableLocal = Jimple.v().newLocal(
                        "initialValueVariable",
                        RefType.v(PtolemyUtilities.variableClass));
                body.getLocals().add(initialValueVariableLocal);

                Parameter initialValueParameter = (Parameter) ((NamedObj) port)
                        .getAttribute("initialValue");

                if (initialValueParameter != null) {
                    String initialValueNameInContext = initialValueParameter
                            .getName(entity);

                    body
                            .getUnits()
                            .insertBefore(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    initialValueLocal,
                                                    Jimple
                                                            .v()
                                                            .newVirtualInvokeExpr(
                                                                    actorLocal,
                                                                    PtolemyUtilities.getAttributeMethod
                                                                            .makeRef(),
                                                                    StringConstant
                                                                            .v(initialValueNameInContext))),
                                    insertPoint);

                    // cast to Variable.
                    body
                            .getUnits()
                            .insertBefore(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    initialValueVariableLocal,
                                                    Jimple
                                                            .v()
                                                            .newCastExpr(
                                                                    initialValueLocal,
                                                                    RefType
                                                                            .v(PtolemyUtilities.variableClass))),
                                    insertPoint);

                    Local tokenLocal = Jimple.v().newLocal("initialValueToken",
                            RefType.v(PtolemyUtilities.tokenClass));
                    body.getLocals().add(tokenLocal);

                    // call getToken.
                    body
                            .getUnits()
                            .insertBefore(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    tokenLocal,
                                                    Jimple
                                                            .v()
                                                            .newVirtualInvokeExpr(
                                                                    initialValueVariableLocal,
                                                                    PtolemyUtilities.variableGetTokenMethod
                                                                            .makeRef())),
                                    insertPoint);

                    // Get the Parameter argument.
                    units.insertBefore(Jimple.v().newAssignStmt(paramLocal,
                            body.getParameterLocal(0)), insertPoint);

                    // Get the right output Port variable.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            portVarLocal,
                            Jimple.v().newVirtualInvokeExpr(paramLocal,
                                    getPortVariableMethod.makeRef(),
                                    IntConstant.v(0))), insertPoint);

                    // Cast to a Token port variable.
                    units.insertBefore(Jimple.v().newAssignStmt(
                            tokenPortVarLocal,
                            Jimple.v().newCastExpr(portVarLocal,
                                    RefType.v(giottoTokenPortVariableClass))),
                            insertPoint);

                    // Set the token.
                    units.insertBefore(Jimple.v().newInvokeStmt(
                            Jimple.v().newVirtualInvokeExpr(tokenPortVarLocal,
                                    setPortVariableTokenMethod.makeRef(),
                                    tokenLocal)), insertPoint);
                }

                units.add(Jimple.v().newReturnVoidStmt());
            }
        }
        // Inline the director
        {
            // populate the preinitialize method
            SootMethod classMethod = modelClass
                    .getMethodByName("preinitialize");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // Set the static field pointing to the model.
            units.insertBefore(Jimple.v().newAssignStmt(
                    Jimple.v().newStaticFieldRef(modelField.makeRef()),
                    thisLocal), insertPoint);

            // Add code to the beginning of the preinitialize method that
            // initializes the attributes.
            //             ModelTransformer.initializeAttributesBefore(body, insertPoint,
            //                     model, body.getThisLocal(),
            //                     model, body.getThisLocal(),
            //                     modelClass);
            for (Iterator entities = model.deepEntityList().iterator(); entities
                    .hasNext();) {
                Entity entity = (Entity) entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className = ModelTransformer.getInstanceClassName(
                        entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod preinitializeMethod = SootUtilities
                        .searchForMethodByName(theClass, "preinitialize");
                Local actorLocal = Jimple.v().newLocal("actor",
                        RefType.v(theClass));
                body.getLocals().add(actorLocal);

                // Get the actor.
                units.insertBefore(Jimple.v().newAssignStmt(
                        actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal,
                                field.makeRef())), insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                preinitializeMethod.makeRef())), insertPoint);
            }

            //           units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the initialize method
            SootMethod classMethod = modelClass.getMethodByName("initialize");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);

            for (Iterator entities = model.deepEntityList().iterator(); entities
                    .hasNext();) {
                Entity entity = (Entity) entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className = ModelTransformer.getInstanceClassName(
                        entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod initializeMethod = SootUtilities
                        .searchForMethodByName(theClass, "initialize");

                // Set the field.
                units.insertBefore(Jimple.v().newAssignStmt(
                        actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal,
                                field.makeRef())), insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                initializeMethod.makeRef())), insertPoint);
            }

            //           units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the prefire method
            SootMethod classMethod = modelClass.getMethodByName("prefire");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            /*Local thisLocal =*/body.getThisLocal();

            Local prefireReturnsLocal = Jimple.v().newLocal("preReturns",
                    BooleanType.v());
            body.getLocals().add(prefireReturnsLocal);

            //             // Prefire the controller.
            //             Local actorLocal = Jimple.v().newLocal("actor", actorType);
            //             body.getLocals().add(actorLocal);
            //             String fieldName = ModelTransformer.getFieldNameForEntity(
            //                     controller, model);
            //             SootField field = modelClass.getFieldByName(fieldName);
            //             String className =
            //                 ModelTransformer.getInstanceClassName(controller, options);
            //             SootClass theClass = Scene.v().loadClassAndSupport(className);
            //             SootMethod actorPrefireMethod =
            //                 SootUtilities.searchForMethodByName(
            //                         theClass, "prefire");
            //             units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
            //                     Jimple.v().newInstanceFieldRef(thisLocal, field)),
            //                     insertPoint);
            //             units.insertBefore(Jimple.v().newAssignStmt(prefireReturnsLocal,
            //                               Jimple.v().newVirtualInvokeExpr(actorLocal,
            //                                       actorPrefireMethod)),
            //                     insertPoint);
            units.insertBefore(Jimple.v().newAssignStmt(prefireReturnsLocal,
                    IntConstant.v(0)), insertPoint);
            units.insertBefore(Jimple.v().newReturnStmt(prefireReturnsLocal),
                    insertPoint);

            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the fire method
            SootMethod classMethod = modelClass.getMethodByName("fire");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local indexLocal = Jimple.v().newLocal("index", IntType.v());
            body.getLocals().add(indexLocal);

            Local tokenLocal = Jimple.v().newLocal("token",
                    PtolemyUtilities.tokenType);
            body.getLocals().add(tokenLocal);

            // Transfer Inputs from input ports.
            for (Iterator ports = model.inputPortList().iterator(); ports
                    .hasNext();) {
                IOPort port = (IOPort) ports.next();
                int rate = 1;

                String fieldName = ModelTransformer.getFieldNameForPort(port,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);

                // Get a reference to the port.
                Local portLocal = Jimple.v().newLocal("port",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(portLocal);

                Local tempPortLocal = Jimple.v().newLocal("tempPort",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(tempPortLocal);
                units.insertBefore(Jimple.v().newAssignStmt(
                        tempPortLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal,
                                field.makeRef())), insertPoint);
                units.insertBefore(Jimple.v().newAssignStmt(
                        portLocal,
                        Jimple.v().newCastExpr(tempPortLocal,
                                PtolemyUtilities.ioportType)), insertPoint);

                for (int i = 0; i < port.getWidth(); i++) {
                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(Jimple.v().newAssignStmt(indexLocal,
                            IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();

                    // Read
                    bodyList.add(Jimple.v().newAssignStmt(
                            tokenLocal,
                            Jimple.v().newVirtualInvokeExpr(portLocal,
                                    PtolemyUtilities.getMethod.makeRef(),
                                    IntConstant.v(i))));

                    // Write
                    bodyList.add(Jimple.v().newInvokeStmt(
                            Jimple.v()
                                    .newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.sendInsideMethod
                                                    .makeRef(),
                                            IntConstant.v(i), tokenLocal)));

                    // Increment the index.
                    bodyList.add(Jimple.v()
                            .newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newAddExpr(indexLocal,
                                            IntConstant.v(1))));

                    Expr conditionalExpr = Jimple.v().newLtExpr(indexLocal,
                            IntConstant.v(rate));

                    SootUtilities.createForLoopBefore(body, insertPoint,
                            initializerList, bodyList, conditionalExpr);
                }
            }
            // Start the Emachine...
            {
                // Fire the controller.
                SootClass emulatorClass = Scene.v().loadClassAndSupport(
                        "platform.emachine.java.Emulator");
                SootMethod theRunMethod = emulatorClass
                        .getMethodByName("runAndWait");
                String fileName = directory + "/out.ecd";
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newStaticInvokeExpr(theRunMethod.makeRef(),
                                StringConstant.v(fileName))), insertPoint);

                //                 Local actorLocal = Jimple.v().newLocal("actor", actorType);
                //                 body.getLocals().add(actorLocal);
                //                 String fieldName = ModelTransformer.getFieldNameForEntity(
                //                         controller, model);
                //                 SootField field = modelClass.getFieldByName(fieldName);
                //                 String className =
                //                     ModelTransformer.getInstanceClassName(controller, options);
                //                 SootClass theClass = Scene.v().loadClassAndSupport(className);
                //                 SootMethod actorFireMethod =
                //                     SootUtilities.searchForMethodByName(
                //                             theClass, "fire");
                //                 units.insertBefore(
                //                         Jimple.v().newAssignStmt(actorLocal,
                //                                 Jimple.v().newInstanceFieldRef(thisLocal, field)),
                //                         insertPoint);
                //                 units.insertBefore(
                //                         Jimple.v().newInvokeStmt(
                //                                 Jimple.v().newVirtualInvokeExpr(actorLocal,
                //                                         actorFireMethod)),
                //                         insertPoint);
            }

            // Transfer outputs from output ports
            for (Iterator ports = model.outputPortList().iterator(); ports
                    .hasNext();) {
                IOPort port = (IOPort) ports.next();
                int rate = DFUtilities.getTokenProductionRate(port);

                String fieldName = ModelTransformer.getFieldNameForPort(port,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);

                // Get a reference to the port.
                Local portLocal = Jimple.v().newLocal("port",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(portLocal);

                Local tempPortLocal = Jimple.v().newLocal("tempPort",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(tempPortLocal);
                units.insertBefore(Jimple.v().newAssignStmt(
                        tempPortLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal,
                                field.makeRef())), insertPoint);
                units.insertBefore(Jimple.v().newAssignStmt(
                        portLocal,
                        Jimple.v().newCastExpr(tempPortLocal,
                                PtolemyUtilities.ioportType)), insertPoint);

                for (int i = 0; i < port.getWidth(); i++) {
                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(Jimple.v().newAssignStmt(indexLocal,
                            IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();

                    // Read
                    bodyList.add(Jimple.v().newAssignStmt(
                            tokenLocal,
                            Jimple.v().newVirtualInvokeExpr(portLocal,
                                    PtolemyUtilities.getInsideMethod.makeRef(),
                                    IntConstant.v(i))));

                    // Write
                    bodyList.add(Jimple.v().newInvokeStmt(
                            Jimple.v().newVirtualInvokeExpr(portLocal,
                                    PtolemyUtilities.sendMethod.makeRef(),
                                    IntConstant.v(i), tokenLocal)));

                    // Increment the index.
                    bodyList.add(Jimple.v()
                            .newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newAddExpr(indexLocal,
                                            IntConstant.v(1))));

                    Expr conditionalExpr = Jimple.v().newLtExpr(indexLocal,
                            IntConstant.v(rate));

                    SootUtilities.createForLoopBefore(body, insertPoint,
                            initializerList, bodyList, conditionalExpr);
                }
            }

            // Return.
            //            units.add(Jimple.v().newReturnVoidStmt());
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the postfire method
            SootMethod classMethod = modelClass.getMethodByName("postfire");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            /*Local thisLocal =*/body.getThisLocal();

            Local postfireReturnsLocal = Jimple.v().newLocal("postfireReturns",
                    BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);

            // Postfire the controller.
            //             Local actorLocal = Jimple.v().newLocal("actor", actorType);
            //             body.getLocals().add(actorLocal);
            //             String fieldName = ModelTransformer.getFieldNameForEntity(
            //                     controller, model);
            //             SootField field = modelClass.getFieldByName(fieldName);
            //             String className =
            //                 ModelTransformer.getInstanceClassName(controller, options);
            //             SootClass theClass = Scene.v().loadClassAndSupport(className);
            //             SootMethod actorPostfireMethod =
            //                 SootUtilities.searchForMethodByName(
            //                         theClass, "postfire");
            //             units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
            //                     Jimple.v().newInstanceFieldRef(thisLocal, field)),
            //                     insertPoint);
            //             units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
            //                               Jimple.v().newVirtualInvokeExpr(actorLocal,
            //                                       actorPostfireMethod.makeRef())),
            //                     insertPoint);
            units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                    IntConstant.v(0)), insertPoint);

            units.insertBefore(Jimple.v().newReturnStmt(postfireReturnsLocal),
                    insertPoint);
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the wrapup method
            SootMethod classMethod = modelClass.getMethodByName("wrapup");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);

            for (Iterator entities = model.deepEntityList().iterator(); entities
                    .hasNext();) {
                Entity entity = (Entity) entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className = ModelTransformer.getInstanceClassName(
                        entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod wrapupMethod = SootUtilities.searchForMethodByName(
                        theClass, "wrapup");

                // Set the field.
                units.insertBefore(Jimple.v().newAssignStmt(
                        actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal,
                                field.makeRef())), insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                wrapupMethod.makeRef())), insertPoint);
            }

            //           units.insertBefore(Jimple.v().newReturnVoidStmt(),
            //                   insertPoint);
        }
    }
}
