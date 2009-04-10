/* Base class for Ptolemy configurations.

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
package ptolemy.actor.gui;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.ApplicationConfigurer;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.Token;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// Configuration

/**
 The configuration of an application that uses Ptolemy II classes.
 An instance of this class is in charge of the user interface,
 and coordinates multiple views of multiple models. One of its
 functions, for example, is to manage the opening of new models,
 ensuring that an appropriate view is used. It also makes sure that
 if a model is opened that is already open, then existing views are
 shown rather than creating new views.
 <p>
 The applications <i>vergil</i> and <i>moml</i> (at least) use
 configurations defined in MoML files, typically located in
 ptII/ptolemy/configs. The <i>moml</i> application takes as
 command line arguments a list of MoML files, the first of which
 is expected to define an instance of Configuration and its contents.
 That configuration is then used to open subsequent MoML files on the
 command line, and to manage the user interface.
 <p>
 Rather than performing all these functions itself, this class
 is a container for a model directory, effigy factories, and tableau
 factories that actually realize these functions. An application
 is configured by populating an instance of this class with
 a suitable set of these other classes. A minimal configuration
 defined in MoML is shown below:
 <pre>
 &lt;?xml version="1.0" standalone="no"?&gt;
 &lt;!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
 "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;
 &lt;entity name="configuration" class="ptolemy.actor.gui.Configuration"&gt;
 &lt;doc&gt;Configuration to run but not edit Ptolemy II models&lt;/doc&gt;
 &lt;entity name="directory" class="ptolemy.actor.gui.ModelDirectory"/&gt;
 &lt;entity name="effigyFactory" class="ptolemy.actor.gui.PtolemyEffigy$Factory"/&gt;
 &lt;property name="tableauFactory" class="ptolemy.actor.gui.RunTableau$Factory"/&gt;
 &lt;/entity&gt;
 </pre>
 <p>
 It must contain, at a minimum, an instance of ModelDirectory, named
 "directory", and an instance of EffigyFactory, named "effigyFactory".
 The openModel() method delegates to the effigy factory the opening of a model.
 It may also contain an instance of TextEditorTableauFactory, named "tableauFactory".
 A tableau is a visual representation of the model in a top-level window.
 The above minimal configuration can be used to run Ptolemy II models
 by opening a run panel only.
 <p>
 When the directory becomes empty (all models have been closed),
 it removes itself from the configuration. When this happens, the
 configuration calls System.exit() to exit the application.

 <p>To access the configuration from a random place, if you have a
 NamedObj <code>foo</code>, then you can call:
 <pre>
 Effigy effigy = Configuration.findEffigy(foo.toplevel());
 Configuration configuration = effigy.toplevel();
 </pre>


 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (celaine)
 @see EffigyFactory
 @see ModelDirectory
 @see Tableau
 @see TextEditorTableau
 */
public class Configuration extends CompositeEntity implements
        ApplicationConfigurer {
    /** Construct an instance in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the instance to the workspace directory.
     *  Increment the version number of the workspace.
     *  Note that there is no constructor that takes a container
     *  as an argument; a Configuration is always
     *  a top-level entity (this is enforced by the setContainer()
     *  method).
     *  @param workspace The workspace that will list the entity.
     */
    public Configuration(Workspace workspace) {
        super(workspace);
        _configurations.add(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check the configuration for common style problems.
     *  @return HTML describing the problems
     *  @exception Exception If there is a problem cloning the configuration.
     */
    public String check() throws Exception {
        StringBuffer results = new StringBuffer();
        Configuration cloneConfiguration = (Configuration) clone();

        // Check TypedAtomicActors and Attributes
        Iterator containedObjects = containedObjectsIterator();
        while (containedObjects.hasNext()) {
            NamedObj containedObject = (NamedObj) containedObjects.next();
            // Check the clone fields on AtomicActors and Attributes.
            // Note that Director extends Attribute, so we get the
            // Directors as well
            if (containedObject instanceof TypedAtomicActor
                    || containedObject instanceof Attribute) {
                results.append(_checkCloneFields(containedObject));
            }
        }

        // Check atomic actors for clone problems related to types
        List entityList = allAtomicEntityList();
        Iterator entities = entityList.iterator();
        while (entities.hasNext()) {
            Object entity = entities.next();
            if (entity instanceof TypedAtomicActor) {
                // Check atomic actors for clone problems
                results.append(_checkCloneFields((TypedAtomicActor) entity));
                TypedAtomicActor actor = (TypedAtomicActor) entity;
                String fullName = actor.getName(this);
                TypedAtomicActor clone = (TypedAtomicActor) cloneConfiguration
                        .getEntity(fullName);
                if (clone == null) {
                    results.append("Actor " + fullName + " was not cloned!");
                } else {

                    // First, check type constraints
                    Set<Inequality> constraints = actor.typeConstraints();
                    Set<Inequality> cloneConstraints = clone.typeConstraints();

                    // Make sure the clone has the same number of constraints.
                    if (constraints.size() != cloneConstraints.size()) {
                        results.append(actor.getFullName() + " has "
                                + constraints.size() + " constraints that "
                                + "differ from " + cloneConstraints.size()
                                + " constraints its clone has.\n"
                                + " Constraints: \nMaster Constraints:\n");
                        Iterator constraintIterator = constraints.iterator();
                        while (constraintIterator.hasNext()) {
                            Inequality constraint = (Inequality) constraintIterator
                                    .next();
                            results.append(constraint.toString() + "\n");
                        }
                        results.append("Clone constraints:\n");
                        Iterator cloneConstraintIterator = cloneConstraints.iterator();
                        while (cloneConstraintIterator.hasNext()) {
                            Inequality constraint = (Inequality) cloneConstraintIterator
                                    .next();
                            results.append(constraint.toString() + "\n");
                        }
                        
                        
                    }

                    // Make sure the constraints are the same.
                    // First, iterate through the constraints of the master
                    // and create a HashSet of string descriptions.  
                    // This is likely to work since the problem is usually
                    // the the clone is missing constraints.
                    HashSet<String> constraintsDescription = new HashSet<String>();
                    try {
                        Iterator constraintIterator = constraints.iterator();
                        while (constraintIterator.hasNext()) {
                            Inequality constraint = (Inequality) constraintIterator
                                    .next();
                            constraintsDescription
                                    .add(constraint.toString());
                        }
                    } catch (Throwable throwable) {
                        throw new IllegalActionException(actor, throwable,
                                "Failed to iterate through constraints.");
                    }
                    // Make sure that each constraint in the clone is present
                    // in the master
                    Iterator cloneConstraintIterator = cloneConstraints
                            .iterator();
                    while (cloneConstraintIterator.hasNext()) {
                        Inequality constraint = (Inequality) cloneConstraintIterator
                                .next();
                        if (!constraintsDescription.contains(constraint
                                .toString())) {
                            results.append("Master object of "
                                    + actor.getFullName()
                                    + " is missing constraint:\n"
                                    + constraint.toString()
                                    + ".\n");
                        }
                    }


                    // Now do the same for the clone.
                    HashSet<String> cloneConstraintsDescription = new HashSet<String>();
                    try {
                        Iterator constraintIterator = cloneConstraints.iterator();
                        while (constraintIterator.hasNext()) {
                            Inequality constraint = (Inequality) constraintIterator
                                    .next();
                            cloneConstraintsDescription
                                    .add(constraint.toString());
                        }
                    } catch (Throwable throwable) {
                        throw new IllegalActionException(actor, throwable,
                                "Failed to iterate through constraints.");
                    }
                    // Make sure that each constraint in the clone is present
                    // in the master
                    Iterator constraintIterator = constraints.iterator();
                    while (constraintIterator.hasNext()) {
                        Inequality constraint = (Inequality) constraintIterator
                                .next();
                        if (!cloneConstraintsDescription.contains(constraint
                                .toString())) {
                            results.append("Clone of "
                                    + actor.getFullName()
                                    + " is missing constraint:\n"
                                    + constraint.toString()
                                    + ".\n");
                        }
                    }

                    // Check that the type constraint is between ports
                    // and Parameters of the same object.

                    cloneConstraintIterator = cloneConstraints.iterator();
                    while (cloneConstraintIterator.hasNext()) {
                        Inequality constraint = (Inequality) cloneConstraintIterator
                                .next();
                        InequalityTerm greaterTerm = constraint
                                .getGreaterTerm();
                        InequalityTerm lesserTerm = constraint.getLesserTerm();

                        Object greaterAssociatedObject = greaterTerm
                                .getAssociatedObject();
                        Object lesserAssociatedObject = lesserTerm
                                .getAssociatedObject();
                        if (greaterAssociatedObject instanceof NamedObj
                                && lesserAssociatedObject instanceof NamedObj) {
                            // FIXME: what about non-NamedObjs?
                            NamedObj greaterNamedObj = (NamedObj) greaterAssociatedObject;
                            NamedObj lesserNamedObj = (NamedObj) lesserAssociatedObject;
                            if (greaterNamedObj != null
                                    && lesserNamedObj != null
                                    && (greaterNamedObj.getContainer() != lesserNamedObj
                                            .getContainer())) {
                                results
                                        .append(clone.getFullName()
                                                + " has type constraints with "
                                                + "associated objects that don't have "
                                                + "the same container:\n"
                                                + greaterNamedObj.getFullName()
                                                + " has a container:\n"
                                                + greaterNamedObj
                                                        .getContainer()
                                                + "\n"
                                                + lesserNamedObj.getFullName()
                                                + " has a container:\n"
                                                + lesserNamedObj.getContainer()
                                                + "\n"
                                                + "This can occur if the clone(Workspace) "
                                                + "method is not present or does not set "
                                                + "the constraints like the constructor "
                                                + "does or if a Parameter or Port is not "
                                                + "declared public.\n");
                            }
                        }
                    }
                }
            }
        }
        return results.toString();
    }

    /** Return a list of all the configurations that have been created.
     *  Note  that if this method is called before a configuration
     *  is created, then it will return an empty linked list.
     *  @return A list of configurations, where each element of the list
     *  is of type Configuration.
     */
    public static List configurations() {
        return _configurations;
    }

    /** Create the first tableau for the given effigy, using the
     *  tableau factory.  This is called after an effigy is first opened,
     *  or when a new effigy is created.  If the method fails
     *  to create a tableau, then it removes the effigy from the directory.
     *  This prevents us from having lingering effigies that have no
     *  user interface.
     *  @return A tableau for the specified effigy, or null if none
     *   can be opened.
     *  @param effigy The effigy for which to create a tableau.
     */
    public Tableau createPrimaryTableau(final Effigy effigy) {
        // NOTE: It used to be that the body of this method was
        // actually executed later, in the event thread, so that it can
        // safely interact with the user interface.
        // However, this does not appear to be necessary, and it
        // makes it impossible to return the tableau.
        // So we no longer do this.
        // If the object referenced by the effigy contains
        // an attribute that is an instance of TextEditorTableauFactory,
        // then use that factory to create the tableau.
        // Otherwise, use the first factory encountered in the
        // configuration that agrees to represent this effigy.
        TableauFactory factory = null;

        if (effigy instanceof PtolemyEffigy) {
            NamedObj model = ((PtolemyEffigy) effigy).getModel();

            if (model != null) {
                Iterator factories = model.attributeList(TableauFactory.class)
                        .iterator();

                // If there are more than one of these, use the first
                // one that agrees to open the model.
                while (factories.hasNext() && (factory == null)) {
                    factory = (TableauFactory) factories.next();

                    try {
                        Tableau tableau = factory.createTableau(effigy);

                        if (tableau != null) {
                            // The first tableau is a master if the container
                            // of the containing effigy is the model directory.
                            // Used to do this:
                            // if (effigy.getContainer() instanceof ModelDirectory) {
                            if (effigy.masterEffigy() == effigy) {
                                tableau.setMaster(true);
                            }

                            tableau.setEditable(effigy.isModifiable());
                            tableau.show();
                            return tableau;
                        }
                    } catch (Exception ex) {
                        // Ignore so we keep trying.
                        // NOTE: Uncomment this line to detect bugs when
                        // you try to open a model and you get a text editor.
                        // ex.printStackTrace();
                        factory = null;
                    }
                }
            }
        }

        // Defer to the configuration.
        // Create a tableau if there is a tableau factory.
        factory = (TableauFactory) getAttribute("tableauFactory");

        if (factory != null) {
            // If this fails, we do not want the effigy to linger
            try {
                Tableau tableau = factory.createTableau(effigy);

                if (tableau == null) {
                    throw new Exception("Tableau factory returns null.");
                }

                // The first tableau is a master if the container
                // of the containing effigy is the model directory.
                if (effigy.getContainer() instanceof ModelDirectory) {
                    tableau.setMaster(true);
                }

                tableau.setEditable(effigy.isModifiable());
                tableau.show();
                return tableau;
            } catch (Exception ex) {
                // NOTE: Uncomment this line to detect bugs when
                // you try to open a model and you get a text editor.
                // ex.printStackTrace();
                // Remove the effigy.  We were unable to open a tableau for it.

                // If we have a link to a missing .htm file, we want to
                // avoid popping up two MessageHandlers.
                boolean calledMessageHandler = false;
                try {
                    if (effigy.getContainer() instanceof ModelDirectory) {
                        // This is the master.
                        // Calling setContainer() = null will exit, so
                        // we display the error message here.
                        //
                        // We will get to here if
                        // diva.graph.AbstractGraphController.rerender()
                        // throws an NullPointerException when starting
                        // vergil.
                        if (effigy instanceof PtolemyEffigy) {
                            MessageHandler.error("Failed to open "
                                    + ((PtolemyEffigy) effigy).getModel()
                                            .getFullName(), ex);
                            calledMessageHandler = true;
                        } else {
                            // Opening a link to a non-existant .htm file
                            // might get us to here because the effigy is
                            // not a PtolemyEffigy.
                            //
                            // Note that because we call MessageHandler here,
                            // this means that putting a try/catch around
                            // configuration.openModel() does not do much good
                            // if a .htm file is not found because the
                            // MessageHandler pops up before we return
                            // from the exception.

                            MessageHandler.error("Failed to open "
                                    + effigy.identifier.getExpression(), ex);
                            calledMessageHandler = true;
                        }
                    }

                    effigy.setContainer(null);
                } catch (Throwable throwable) {
                    calledMessageHandler = false;
                    throw new InternalErrorException(this, throwable, null);
                }

                // As a last resort, attempt to open source code
                // associated with the object.
                if (effigy instanceof PtolemyEffigy) {
                    NamedObj object = ((PtolemyEffigy) effigy).getModel();

                    // Source code is found by name.
                    String filename = StringUtilities
                            .objectToSourceFileName(object);

                    try {
                        URL toRead = getClass().getClassLoader().getResource(
                                filename);

                        if (toRead != null) {
                            return openModel(null, toRead, toRead
                                    .toExternalForm());
                        } else {
                            MessageHandler
                                    .error("Cannot find a tableau or the source code for "
                                            + object.getFullName());
                        }
                    } catch (Exception exception) {
                        MessageHandler.error(
                                "Failed to open the source code for "
                                        + object.getFullName(), exception);
                    }
                }

                // Note that we can't rethrow the exception here
                // because removing the effigy may result in
                // the application exiting.
                if (!calledMessageHandler) {
                    MessageHandler.error("Failed to open tableau for "
                            + effigy.identifier.getExpression(), ex);
                }
            }
        }

        return null;
    }

    /** Find an effigy for the specified model by searching all the
     *  configurations that have been created. Although typically there is
     *  only one, in principle there may be more than one.  This can be used
     *  to find a configuration, which is typically the result of calling
     *  toplevel() on the effigy.
     *  @param model The model for which to find an effigy.
     *  @return An effigy, or null if none can be found.
     */
    public static Effigy findEffigy(NamedObj model) {
        Iterator configurations = _configurations.iterator();

        while (configurations.hasNext()) {
            Configuration configuration = (Configuration) configurations.next();
            Effigy effigy = configuration.getEffigy(model);

            if (effigy != null) {
                return effigy;
            }
        }

        return null;
    }

    /** Get the model directory.
     *  @return The model directory, or null if there isn't one.
     */
    public ModelDirectory getDirectory() {
        Entity directory = getEntity(_DIRECTORY_NAME);

        if (directory instanceof ModelDirectory) {
            return (ModelDirectory) directory;
        }

        return null;
    }

    /** Get the effigy for the specified Ptolemy model.
     *  This searches all instances of PtolemyEffigy deeply contained by
     *  the directory, and returns the first one it encounters
     *  that is an effigy for the specified model.
     *  @param model The Ptolemy model.
     *  @return The effigy for the model, or null if none is found.
     */
    public PtolemyEffigy getEffigy(NamedObj model) {
        Entity directory = getEntity(_DIRECTORY_NAME);

        if (directory instanceof ModelDirectory) {
            return _findEffigyForModel((ModelDirectory) directory, model);
        } else {
            return null;
        }
    }

    /** Open the specified instance. If the instance already has
     *  open tableaux, then put those in the foreground and
     *  return the first one.  Otherwise, create a new tableau and if
     *  necessary, a new effigy.  Unless there is a more natural container
     *  for the effigy (e.g. it is a hierarchical model), then if a new
     *  effigy is created, it is put into the directory of the configuration.
     *  Any new tableau created will be contained by that effigy.
     *  @param entity The entity to open.
     *  @return The tableau that is created, or the first one found,
     *   or null if none is created or found.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     */
    public Tableau openInstance(NamedObj entity) throws IllegalActionException,
            NameDuplicationException {
        return openInstance(entity, null);
    }

    /** Open the specified instance. If the instance already has
     *  open tableaux, then put those in the foreground and
     *  return the first one.  Otherwise, create a new tableau and,
     *  if necessary, a new effigy. Unless there is a more natural
     *  place for the effigy (e.g. it is a hierarchical model), then if a new
     *  effigy is created, it is put into the <i>container</i> argument,
     *  or if that is null, into the directory of the configuration.
     *  Any new tableau created will be contained by that effigy.
     *  @param entity The model.
     *  @param container The container for any new effigy.
     *  @return The tableau that is created, or the first one found,
     *   or null if none is created or found.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     */
    public Tableau openInstance(NamedObj entity, CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        return _openModel(entity, container);
    }

    /** Open the specified URL.
     *  If a model with the specified identifier is present in the directory,
     *  then find all the tableaux of that model and make them
     *  visible; otherwise, read a model from the specified URL <i>in</i>
     *  and create a default tableau for the model and add the tableau
     *  to this directory.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @param identifier The identifier that uniquely identifies the model.
     *  @return The tableau that is created, or null if none.
     *  @exception Exception If the URL cannot be read.
     */
    public Tableau openModel(URL base, URL in, String identifier)
            throws Exception {
        return openModel(base, in, identifier, null);
    }

    /** Open the specified URL using the specified effigy factory.
     *  If a model with the specified identifier is present in the directory,
     *  then find all the tableaux of that model and make them
     *  visible; otherwise, read a model from the specified URL <i>in</i>
     *  and create a default tableau for the model and add the tableau
     *  to this directory.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @param identifier The identifier that uniquely identifies the model.
     *  @param factory The effigy factory to use.
     *  @return The tableau that is created, or null if none.
     *  @exception Exception If the URL cannot be read.
     */
    public Tableau openModel(URL base, URL in, String identifier,
            EffigyFactory factory) throws Exception {
        ModelDirectory directory = (ModelDirectory) getEntity(_DIRECTORY_NAME);

        if (directory == null) {
            throw new InternalErrorException("No model directory!");
        }

        // Check to see whether the model is already open.
        Effigy effigy = directory.getEffigy(identifier);

        if (effigy == null) {
            // No previous effigy exists that is identified by this URL.
            // Find an effigy factory to read it.
            if (factory == null) {
                factory = (EffigyFactory) getEntity("effigyFactory");
            }

            if (factory == null) {
                throw new InternalErrorException(
                        "No effigy factories in the configuration!");
            }

            effigy = factory.createEffigy(directory, base, in);

            if (effigy == null) {
                MessageHandler
                        .error("Unsupported file type or connection not available: "
                                + in.toExternalForm());
                return null;
            }

            if (effigy.identifier.getExpression().compareTo("Unnamed") == 0) {
                // If the value identifier field of the effigy we just
                // created is "Unnamed", then set it to the value of
                // the identifier parameter.
                //
                // HSIFEffigyFactory sets effiigy.identifier because it
                // converts the file we specified from HSIF to MoML and then
                // opens up a file other than the one we specified.
                effigy.identifier.setExpression(identifier);
            }

            // Check the URL to see whether it is a file,
            // and if so, whether it is writable.
            if (in != null && in.getProtocol().equals("file")) {
                String filename = in.getFile();
                File file = new File(filename);

                try {
                    if (!file.canWrite()) {
                        // FIXME: we need a better way to check if
                        // a URL is writable.  

                        // Sigh.  If the filename has spaces in it,
                        // then the URL will have %20s.  However,
                        // the file does not have %20s.
                        // See 
                        // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=153
                        filename = StringUtilities.substitute(
                            filename, "%20", " ");
                        file = new File(filename);
                        if (!file.canWrite()) {
                            effigy.setModifiable(false);
                        }
                    }
                } catch (java.security.AccessControlException accessControl) {
                    // If we are running in a sandbox, then canWrite()
                    // may throw an AccessControlException.
                    effigy.setModifiable(false);
                }
            } else {
                effigy.setModifiable(false);
            }

            return createPrimaryTableau(effigy);
        } else {
            // Model already exists.
            return effigy.showTableaux();
        }
    }

    /** Open the specified Ptolemy II model. If a model already has
     *  open tableaux, then put those in the foreground and
     *  return the first one.  Otherwise, create a new tableau and if
     *  necessary, a new effigy.  Unless there is a more natural container
     *  for the effigy (e.g. it is a hierarchical model), then if a new
     *  effigy is created, it is put into the directory of the configuration.
     *  Any new tableau created will be contained by that effigy.
     *  @param entity The model.
     *  @return The tableau that is created, or the first one found,
     *   or null if none is created or found.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     */
    public Tableau openModel(NamedObj entity) throws IllegalActionException,
            NameDuplicationException {
        return openModel(entity, null);
    }

    /** Open the specified Ptolemy II model. If a model already has
     *  open tableaux, then put those in the foreground and
     *  return the first one.  Otherwise, create a new tableau and,
     *  if necessary, a new effigy. Unless there is a more natural
     *  place for the effigy (e.g. it is a hierarchical model), then if a new
     *  effigy is created, it is put into the <i>container</i> argument,
     *  or if that is null, into the directory of the configuration.
     *  Any new tableau created will be contained by that effigy.
     *  @param entity The model.
     *  @param container The container for any new effigy.
     *  @return The tableau that is created, or the first one found,
     *   or null if none is created or found.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     */
    public Tableau openModel(NamedObj entity, CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        // If the entity defers its MoML definition to another,
        // then open that other, unless this is a class extending another,
        // and also unless this is an object that contains a TableauFactory.
        // I.e., by default, when you open an instance of a class, what
        // is opened is the class definition, not the instance, unless
        // the instance contains a TableauFactory, in which case, we defer
        // to that TableauFactory.
        InstantiableNamedObj deferredTo = null;
        boolean isClass = false;

        if (entity instanceof InstantiableNamedObj) {
            deferredTo = (InstantiableNamedObj) ((InstantiableNamedObj) entity)
                    .getParent();
            isClass = ((InstantiableNamedObj) entity).isClassDefinition();
        }

        if ((deferredTo != null) && !isClass) {
            entity = deferredTo;
        }

        return _openModel(entity, container);
    }

    /** If the argument is not null, then throw an exception.
     *  This ensures that the object is always at the top level of
     *  a hierarchy.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the argument is not null.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException {
        if (container != null) {
            throw new IllegalActionException(this,
                    "Configuration can only be at the top level "
                            + "of a hierarchy.");
        }
    }

    /** Find all instances of Tableau deeply contained in the directory
     *  and call show() on them.  If there is no directory, then do nothing.
     */
    public void showAll() {
        final ModelDirectory directory = (ModelDirectory) getEntity(_DIRECTORY_NAME);

        if (directory == null) {
            return;
        }

        _showTableaux(directory);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the model directory. */
    static public final String _DIRECTORY_NAME = "directory";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Remove the specified entity; if that entity is the model directory,
     *  then exit the application.  This method should not be called
     *  directly.  Call the setContainer() method of the entity instead with
     *  a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param entity The entity to remove.
     */
    protected void _removeEntity(ComponentEntity entity) {
        super._removeEntity(entity);
        if (entity.getName().equals(_DIRECTORY_NAME)) {
            // If the ptolemy.ptII.exitAfterWrapup property is set,
            // then we don't actually exit.
            StringUtilities.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check that clone(Workspace) method properly sets the fields.
     *  In a cloned Director, Attribute or Actor, all
     *  private fields should either point to null or to
     *  distinct objects.
     *  @param namedObj The NamedObj, usually a Director, Attribute
     *  or actor to be checked.
     *  @return A string containing an error message if there is a problem,
     *  otherwise return the empty string.
     *  @exception CloneNotSupportedException If namedObj does not support
     *  clone(Workspace).
     *  @exception IllegalAccessException If there is a problem getting
     *  a field.
     *  @exception ClassNotFoundException If a class cannot be found.
     */
    private String _checkCloneFields(NamedObj namedObj) 
            throws CloneNotSupportedException, IllegalAccessException, ClassNotFoundException {
        NamedObj namedObjClone = (NamedObj) namedObj.clone(new Workspace());

        StringBuffer results = new StringBuffer();

        Class namedObjClass = namedObj.getClass();
        // We check only the fields declared in this class.  
        // FIXME: should we check all fields?
        Field [] namedObjFields = namedObjClass.getDeclaredFields();
        for(int i = 0; i < namedObjFields.length; i++) {
            Field field = namedObjFields[i];
            // Tell the security manager we want to read private fields.
            // This will fail in an applet.
            field.setAccessible(true);
            Class fieldType = field.getType();
            if ( !fieldType.isPrimitive()
                    && field.get(namedObj) != null
                    && !Modifier.isStatic(field.getModifiers())
                    /*&& !fieldType.isArray()*/
                    // Skip fields introduced by javascope
                    && !fieldType.toString().equals("COM.sun.suntest.javascope.database.CoverageUnit")
                    && !field.getName().equals("js$p")
                    // Skip fields introduced by backtracking
                    && !(field.getName().indexOf("$RECORD$") != -1)
                    && !(field.getName().indexOf("$RECORDS") != -1)
                    && !(field.getName().indexOf("$CHECKPOINT") != -1)
                    // Skip immutables
                    && !fieldType.equals(java.net.InetAddress.class)
                    && !fieldType.equals(java.util.regex.Pattern.class)
                    && !fieldType.equals(String.class)
                    && !fieldType.equals(Token.class)
                    && !fieldType.equals(Settable.Visibility.class)) { 

                // If an object is equal and the default hashCode() from
                // Object is the same, then we have a problem.
                if ( (field.get(namedObj)).equals(field.get(namedObjClone))
                        && (System.identityHashCode(field.get(namedObj)) 
                                == System.identityHashCode(field.get(namedObjClone)))) {

                    // Determine what code should go in clone(W)
                    String assignment = field.getName();
                    // FIXME: extend this to more types
                    if (Class.forName("ptolemy.kernel.Port").isAssignableFrom(fieldType)) { 
                        assignment = ".getPort(\"" + assignment + "\")";
                        //                       } else if (fieldType.isInstance( new Attribute())) {
                    } else if (Class.forName("ptolemy.kernel.util.Attribute").isAssignableFrom(fieldType)) { 
                        Attribute fieldAttribute = (Attribute)field.get(namedObjClone); 

                        if (fieldAttribute.getContainer() != namedObjClone) {
                            // If the attribute is actually contained by a Port
                            // and not by the AtomicActor, then get its value.
                            // SDF actors that have ports that have
                            // tokenConsumptionRate and tokenProductionRate 
                            // such as ConvolutionalCoder need this.
                            assignment = "."
                                + fieldAttribute.getContainer().getName()
                                + ".getAttribute(\"" + fieldAttribute.getName()
                                + "\")";
                        } else {
                            assignment = ".getAttribute(\"" + assignment + "\")";
                        }
                    } else {
                        assignment = "\n\t/* Get the object method "
                            + "or null?  */ "
                            + assignment;
                    }
                    
                    String shortClassName = field.getType().getName()
                        .substring(
                                field.getType().getName().lastIndexOf(".")
                                + 1); 

                    results.append( "The " + field.getName()
                            + " " + field.getType().getName()
                            + " field"
                            + "\n\tin the clone of \""
                            + namedObjClass.getName()
                            + "\"\n\tdoes not point to an "
                            + "object distinct from the "
                            + "master.  \n\tThis may cause problems with "
                            + "actor oriented classes."
                            + "\n\tThe clone(Workspace) "
                            + "method should have a line "
                            + "like:\n newObject."
                            + field.getName() + " = ("
                            + shortClassName
                            + ")newObject" + assignment
                            + ";\n");
                }
            }
        }
        return results.toString();
    }    

    /** Return an identifier for the specified effigy based on its
     *  container (if any) and its name.
     *  @return An identifier for the effigy.
     */
    private String _effigyIdentifier(Effigy effigy, NamedObj entity) {
        // Set the identifier of the effigy to be that
        // of the parent with the model name appended.
        NamedObj parent = effigy.getContainer();

        if (!(parent instanceof Effigy)) {
            return effigy.getFullName();
        }

        Effigy parentEffigy = (Effigy) parent;

        // Note that we add a # the first time, and
        // then add . after that.  So
        // file:/c:/foo.xml#bar.bif is ok, but
        // file:/c:/foo.xml#bar#bif is not
        // If the title does not contain a legitimate
        // way to reference the submodel, then the user
        // is likely to look at the title and use the wrong
        // value if they xml edit files by hand. (cxh-4/02)
        String entityName = parentEffigy.identifier.getExpression();
        String separator = "#";

        if (entityName.indexOf("#") >= 0) {
            separator = ".";
        }

        return (entityName + separator + entity.getName());
    }

    // Recursively search the specified composite for an instance of
    // PtolemyEffigy that matches the specified model.
    private PtolemyEffigy _findEffigyForModel(CompositeEntity composite,
            NamedObj model) {
        if (composite != null) {
            Iterator effigies = composite.entityList(PtolemyEffigy.class)
                    .iterator();

            while (effigies.hasNext()) {
                PtolemyEffigy effigy = (PtolemyEffigy) effigies.next();

                // First see whether this effigy matches.
                if (effigy.getModel() == model) {
                    return effigy;
                }

                // Then see whether any effigy inside this one matches.
                PtolemyEffigy inside = _findEffigyForModel(effigy, model);

                if (inside != null) {
                    return inside;
                }
            }
        }

        return null;
    }

    /** Open the specified model without deferring to its class definition.
     *  @param entity The model to open.
     *  @param container The container for any new effigy.
     *  @return The tableau that is created, or the first one found,
     *   or null if none is created or found.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     */
    private Tableau _openModel(NamedObj entity, CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        // Search the model directory for an effigy that already
        // refers to this model.
        PtolemyEffigy effigy = getEffigy(entity);

        if (effigy != null) {
            // Found one.  Display all open tableaux.
            return effigy.showTableaux();
        } else {
            // There is no pre-existing effigy.  Create one.
            effigy = new PtolemyEffigy(workspace());
            effigy.setModel(entity);

            // Look to see whether the model has a URIAttribute.
            List attributes = entity.attributeList(URIAttribute.class);

            if (attributes.size() > 0) {
                // The entity has a URI, which was probably
                // inserted by MoMLParser.
                URI uri = ((URIAttribute) attributes.get(0)).getURI();

                // Set the URI and identifier of the effigy.
                effigy.uri.setURI(uri);

                // NOTE: The uri might be null, which results in
                // a null pointer exception below. In particular,
                // the class Effigy always has a URI attribute, but
                // the value might not get set.
                if (uri == null) {
                    effigy.identifier.setExpression(_effigyIdentifier(effigy,
                            entity));
                } else {
                    effigy.identifier.setExpression(uri.toString());
                }

                if (container == null) {
                    // Put the effigy into the directory
                    ModelDirectory directory = getDirectory();
                    effigy.setName(directory.uniqueName(entity.getName()));
                    effigy.setContainer(directory);
                } else {
                    effigy.setName(container.uniqueName(entity.getName()));
                    effigy.setContainer(container);
                }

                // Create a default tableau.
                return createPrimaryTableau(effigy);
            } else {
                // If we get here, then we are looking inside a model
                // that is defined within the same file as the parent,
                // probably.  Create a new PtolemyEffigy
                // and open a tableau for it.
                // Put the effigy inside the effigy of the parent,
                // rather than directly into the directory.
                NamedObj parent = entity.getContainer();
                PtolemyEffigy parentEffigy = null;

                // Find the first container above in the hierarchy that
                // has an effigy.
                while ((parent != null) && (parentEffigy == null)) {
                    parentEffigy = getEffigy(parent);
                    parent = parent.getContainer();
                }

                boolean isContainerSet = false;

                if (parentEffigy != null) {
                    // OK, we can put it into this other effigy.
                    effigy.setName(parentEffigy.uniqueName(entity.getName()));
                    effigy.setContainer(parentEffigy);

                    // Set the uri of the effigy to that of
                    // the parent.
                    effigy.uri.setURI(parentEffigy.uri.getURI());

                    // Indicate success.
                    isContainerSet = true;
                }

                // If the above code did not find an effigy to put
                // the new effigy within, then put it into the
                // directory directly or the specified container.
                if (!isContainerSet) {
                    if (container == null) {
                        CompositeEntity directory = getDirectory();
                        effigy.setName(directory.uniqueName(entity.getName()));
                        effigy.setContainer(directory);
                    } else {
                        effigy.setName(container.uniqueName(entity.getName()));
                        effigy.setContainer(container);
                    }
                }

                effigy.identifier.setExpression(_effigyIdentifier(effigy,
                        entity));

                return createPrimaryTableau(effigy);
            }
        }
    }

    // Call show() on all instances of Tableaux contained by the specified
    // container.
    private void _showTableaux(CompositeEntity container) {
        Iterator entities = container.entityList().iterator();

        while (entities.hasNext()) {
            Object entity = entities.next();

            if (entity instanceof Tableau) {
                ((Tableau) entity).show();
            } else if (entity instanceof CompositeEntity) {
                _showTableaux((CompositeEntity) entity);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of configurations that have been created. */
    private static List _configurations = new LinkedList();
}
