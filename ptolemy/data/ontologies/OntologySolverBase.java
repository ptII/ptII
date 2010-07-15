/*  The base abstract class for an ontology solver.

 Copyright (c) 2008-2010 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Node;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.Configurer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLModelAttribute;

///////////////////////////////////////////////////////////////////
////OntologySolverBase

/**
The base abstract class for an ontology solver.

<p>The base class provides the core functionality for ontology
solver resolution.  It provides a method to create an OntologyAdapter for any
given model component. The model component can be an object of any
Ptolemy class (e.g. ASTPtRootNode, Sink, Entity, and FSMActor). A
model component, in turn, may have one or multiple objects to which
ontology concepts can be attached.

<p>Every OntologySolver is linked together by the SharedParameter called
"ontologySolverUtilitiesWrapper", which contains the shared utility object.
This allows every OntologySolver to find other solvers in the model.

<p>Subclasses needs to implement
{@link ptolemy.data.ontologies.OntologySolverBase#resolveProperties()}
to specify exactly how to perform the ontology concept resolution. For example,
one may gather all the constraints from the OntologyAdapters and feed them
into a constraint solver.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */
public abstract class OntologySolverBase extends MoMLModelAttribute {

    /**
     * Construct an OntologySolverBase with the specified container and
     * name. If this is the first OntologySolver created in the model,
     * the shared utility object will also be created.
     *
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the OntologySolverBase is
     * not of an acceptable attribute for the container.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public OntologySolverBase(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        ontologySolverUtilitiesWrapper = new SharedParameter(this,
                "ontologySolverUtilitiesWrapper", OntologySolver.class);
        ontologySolverUtilitiesWrapper.setPersistent(false);
        ontologySolverUtilitiesWrapper.setVisibility(Settable.NONE);

        // **We can only create a new shared utilities object
        // only once per model.
        if (ontologySolverUtilitiesWrapper.getExpression().length() == 0) {
            ontologySolverUtilitiesWrapper.setToken(new ObjectToken(
                    new OntologySolverUtilities()));
        }

        Collection<SharedParameter> parameters = ontologySolverUtilitiesWrapper
                .sharedParameterSet();
        for (SharedParameter parameter : parameters) {
            parameters = parameter.sharedParameterSet();
        }

        _ontologySolverUtilities = (OntologySolverUtilities) ((ObjectToken) ontologySolverUtilitiesWrapper
                .getToken()).getValue();

    }

    ///////////////////////////////////////////////////////////////////
    ////                    ports and parameters                   ////

    /**
     * The shared parameter that links together every solver in the
     * same model.
     */
    public SharedParameter ontologySolverUtilitiesWrapper;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Clear the resolved property for the specified object. The
     * object is assumed to be property-able; otherwise, nothing
     * happens.
     *
     * @param object The specified object.
     */
    public void clearResolvedProperty(Object object) {
        _resolvedProperties.remove(object);
    }

    /**
     * Return the list of all PropertyHelpers associated with this
     * ontology solver.
     *
     * @return The list of PropertyHelpers.
     * @exception IllegalActionException If there is an exception from getting
     * all the subHelpers. 
     */
    public List<OntologyAdapter> getAllAdapters() throws IllegalActionException {
        NamedObj topLevel = _toplevel();
        List<OntologyAdapter> result = new LinkedList<OntologyAdapter>();
        List<OntologyAdapter> subHelpers = new LinkedList<OntologyAdapter>();

        result.add(getAdapter(topLevel));
        subHelpers.add(getAdapter(topLevel));

        while (!subHelpers.isEmpty()) {
            OntologyAdapter adapter = subHelpers.remove(0);
            subHelpers.addAll(adapter._getSubAdapters());
            result.add(adapter);
        }

        return result;
    }

    /** Return the subset of all concept-able objects that are NamedObjs.
     *
     * @return The set of all concept-able NamedObjs.
     * @exception IllegalActionException Thrown if
     * an error occurs when getting the adapters or the concept-able
     * objects from them.
     */
    public Set<NamedObj> getAllConceptableNamedObjs()
            throws IllegalActionException {
        Set<NamedObj> result = new HashSet<NamedObj>();

        for (Object conceptable : getAllPropertyables()) {
            if (conceptable instanceof NamedObj) {
                result.add((NamedObj) conceptable);
            }
        }
        return result;
    }

    /**
     * Return the set of all property-able objects obtained from
     * all PropertyHelper.
     *
     * @return The set of all property-able objects.
     * @exception IllegalActionException Thrown if
     * an error occurs when getting the adapters or the property-able
     * objects from them.
     */
    public Set getAllPropertyables() throws IllegalActionException {
        HashSet result = new HashSet();

        for (OntologyAdapter adapter : getAllAdapters()) {
            result.addAll(adapter.getPropertyables());
        }
        return result;
    }

    /**
     * Return the list of all solvers that are in the same model. They
     * are linked by the specified SharedParameter.
     *
     * @param sharedParameter The specified SharedParameter links
     * together the solvers.
     * @return A list of PropertySolvers.
     */
    public static List<OntologySolver> getAllSolvers(
            SharedParameter sharedParameter) {
        List<NamedObj> parameters = new ArrayList<NamedObj>(sharedParameter
                .sharedParameterSet());
        List<OntologySolver> solvers = new LinkedList<OntologySolver>();
        for (NamedObj parameter : parameters) {
            Object container = parameter.getContainer();
            if (container instanceof OntologySolver) {
                solvers.add((OntologySolver) container);
            }
        }
        return solvers;
    }

    /**
     * Get the attribute that corresponds to the specified
     * ASTPtRootNode. This assumes that the correspondence is recorded
     * previously through calling
     * {@link ptolemy.data.properties.PropertyHelper#putAttribute(ASTPtRootNode, Attribute)}.
     *
     * @param node The specified ASTPtRootNode.
     * @return The attribute associated with the specified ASTPtRootNode.
     * @exception AssertionError Thrown if the specified node does not
     * have a corresponding attribute.
     */
    public Attribute getAttribute(ASTPtRootNode node) {
        Node root = node;
        Map<ASTPtRootNode, Attribute> attributes = getOntologySolverUtilities()
                .getAttributes();

        while (root.jjtGetParent() != null) {
            if (attributes.containsKey(root)) {
                return attributes.get(root);
            }
            root = root.jjtGetParent();
        }

        if (!attributes.containsKey(root)) {
            throw new AssertionError(node.toString()
                    + " does not have a corresponding attribute.");
        }

        return attributes.get(root);
    }

    /**
     * Return the property adapter for the specified component.
     *
     * @param object The specified component.
     * @return The property adapter for the component.
     * @exception IllegalActionException Thrown if the adapter cannot
     * be found or instantiated.
     */
    public OntologyAdapter getAdapter(Object object)
            throws IllegalActionException {
        return _getAdapter(object);
    }

    /** Return a list of all the ontologies contained in this solver.
     *  If it contains no ontologies, then return an empty list.
     *  @return A list containing all ontologies in this solver.
     */
    @SuppressWarnings("unchecked")
    public List<Ontology> getAllContainedOntologies() {
        NamedObj containedModel = getContainedModel();
        if (containedModel instanceof CompositeEntity) {
            return ((CompositeEntity) containedModel)
                    .entityList(Ontology.class);
        }
        return new LinkedList<Ontology>();        
    }

    /** Return the ontology for this constraint solver.
     *  If this solver contains more than one ontology, then return the
     *  last one added. If it contains no ontologies, then return null.
     *  @return The ontology for this constraint solver.
     *  @exception IllegalActionException If the structure is not a lattice.
     */
    public Ontology getOntology() throws IllegalActionException {
        List<Ontology> ontologies = getAllContainedOntologies();
        if (ontologies.size() > 0) {
            return ontologies.get(ontologies.size() - 1);
        }
        return null;
    }

    /**
     * Return the expression parser.
     *
     * @return The expression parser.
     */
    public static PtParser getParser() {
        if (_parser == null) {
            _parser = new PtParser();
        }
        return _parser;
    }

    /**
     * Return the root ASTPtRootNode associated with the specified
     * attribute.
     *
     * @param attribute The specified attribute.
     * @return The root ASTPtRootNode associated with the specified
     * attribute.
     * @exception IllegalActionException
     */
    // FIXME: Why is this here? It does not seem to belong...
    public ASTPtRootNode getParseTree(Attribute attribute)
            throws IllegalActionException {
        Map<Attribute, ASTPtRootNode> parseTrees = getOntologySolverUtilities()
                .getParseTrees();

        if (!parseTrees.containsKey(attribute)) {
            String expression = null;
            if (attribute instanceof Settable) {
                expression = ((Settable) attribute).getExpression().trim();
            }

            if (expression == null || expression.length() == 0) {
                return null;
            }

            ASTPtRootNode parseTree;
            // if ((attribute instanceof StringAttribute) ||
            // ((attribute instanceof Variable
            // && ((Variable) attribute).isStringMode()))) {
            if (attribute instanceof Variable
                    && ((Variable) attribute).isStringMode()) {

                parseTree = getParser().generateStringParseTree(expression);

            } else {
                parseTree = getParser().generateParseTree(expression);
            }

            parseTrees.put(attribute, parseTree);
            getOntologySolverUtilities().putAttribute(parseTree, attribute);
        }
        return parseTrees.get(attribute);
    }

    /**
     * Return the property value associated with the specified object.
     * @param object The specified object.
     * @return The property of the specified object.
     */
    public Concept getProperty(Object object) {
        return getResolvedProperty(object);
    }

    /**
     * Return the resolved property for the specified object. This forces
     * resolution to happen if the object's property is not present.
     * @param object The specified object
     * @return The resolved property for the specified object.
     * @see #setResolvedProperty
     */
    public Concept getResolvedProperty(Object object) {
        return getResolvedConcept(object, true);
    }

    /**
     * Return the resolved property for the specified object. The specified
     * resolve flag indicates whether to force resolution to happen.
     * @param object The specified object.
     * @param resolve Whether or not to force resolution.
     * @return The resolved property for the specified object.
     * @see #setResolvedProperty
     */
    public Concept getResolvedConcept(Object object, boolean resolve) {
        Concept property = _resolvedProperties.get(object);

        // See if it is already resolved.
        if (property != null) {
            return property;
        }

        // Get from the PropertyAttribute in the model.
        /* FIXME: Not using ConceptAttribute.
        if (object instanceof NamedObj) {
            ConceptAttribute attribute = (ConceptAttribute) ((NamedObj) object)
                    .getAttribute(getExtendedUseCaseName());

            if ((attribute != null) && (attribute.getProperty() != null)) {
                return attribute.getProperty();
            }
        }
        */

        // Try resolve the property.
        try {
            if (resolve
                    && !getOntologySolverUtilities().getRanSolvers().contains(
                            this)) {
                resolveProperties();
            }
        } catch (KernelException ex) {
            throw new InternalErrorException(KernelException
                    .stackTraceToString(ex));
        }

        return _resolvedProperties.get(object);
    }

    /**
     * Return the shared utility object.
     * @return The shared utility object.
     */
    public OntologySolverUtilities getOntologySolverUtilities() {
        return _ontologySolverUtilities;
    }

    /**
     * Mark the property of the specified object as non-settable. The
     * specified object has a fixed assigned property.
     * @param object The specified object.
     */
    public void markAsNonSettable(Object object) {
        _nonSettables.add(object);
    }

    /**
     * Reset the solver.
     */
    public void reset() {
        _resolvedProperties = new HashMap<Object, Concept>();
        _nonSettables = new HashSet<Object>();
        _adapterStore = new HashMap<Object, OntologyAdapter>();
    }

    /**
     * Reset every solver in the model.
     */
    public void resetAll() {
        _resetParser();
        for (OntologySolver solver : getAllSolvers(ontologySolverUtilitiesWrapper)) {
            solver.reset();
        }
        getOntologySolverUtilities().resetAll();

        // Don't clear the lattices; otherwise, we'll have multiple
        // copies of a lattice element when we recreate a lattice.
        // This causes regression testing to fail.
        //PropertyLattice.resetAll();
    }

    /**
     * Perform property resolution.
     * @exception KernelException Thrown if sub-class throws it.
     */
    public abstract void resolveProperties() throws KernelException;

    /**
     * Set the resolved property of the specified object.
     * (See {@link #getResolvedProperty(Object)}).
     * @param object The specified object.
     * @param property The specified property.
     * @see #getResolvedProperty
     */
    public void setResolvedProperty(Object object, Concept property) {
        _resolvedProperties.put(object, property);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return the PropertyHelper for the specified component. This
     * instantiates a new PropertyHelper if it does not already exist
     * for the specified component.
     * @param component The specified component.
     * @return The PropertyHelper for the specified component.
     * @exception IllegalActionException Thrown if the PropertyHelper
     * cannot be instantiated.
     */
    protected OntologyAdapter _getAdapter(Object component)
            throws IllegalActionException {

        if (_adapterStore.containsKey(component)) {
            return _adapterStore.get(component);
        }

        if ((component instanceof IOPort) || (component instanceof Attribute)) {
            if (((NamedObj) component).getContainer() == null) {
                System.err.println("component container is null: " + component);
            }
            return _getAdapter(((NamedObj) component).getContainer());
        }

        /* FIXME
        if (getContainer() instanceof OntologyComposite) {
            _compileHelperClasses();
        }
        */

        String packageName = _getPackageName();
        String defaultAdaptersPackageName = getClass().getPackage().getName()
                + ".adapters.defaultAdapters";

        Class componentClass = component.getClass();

        Class adapterClass = null;
        while (adapterClass == null) {
            try {

                // FIXME: Is this the right error message?
                if (!componentClass.getName().contains("ptolemy")) {
                    throw new IllegalActionException("There is no "
                            + "property adapter for " + component.getClass());
                }

                adapterClass = Class.forName(componentClass.getName()
                        .replaceFirst("ptolemy", packageName));

            } catch (ClassNotFoundException e) {
                // If adapter class cannot be found, search the adapter class
                // in the default adapters package.
                try {
                    adapterClass = Class
                            .forName(componentClass.getName().replaceFirst(
                                    "ptolemy", defaultAdaptersPackageName));

                } catch (ClassNotFoundException e2) {
                    // If adapter class cannot be found, search the adapter class
                    // for parent class instead.
                    componentClass = componentClass.getSuperclass();
                }
            }
        }

        Constructor constructor = null;
        Class solverClass = getClass();
        while (constructor == null && solverClass != null) {
            try {
                constructor = adapterClass.getConstructor(new Class[] {
                        solverClass, componentClass });

            } catch (NoSuchMethodException ex) {
                solverClass = solverClass.getSuperclass();
            }
        }

        if (constructor == null) {
            throw new IllegalActionException(
                    "Cannot find constructor method in "
                            + adapterClass.getName());
        }

        Object adapterObject = null;

        try {
            adapterObject = constructor.newInstance(new Object[] { this,
                    component });

        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Failed to create the adapter class for property constraints.");
        }

        if (!(adapterObject instanceof OntologyAdapter)) {
            throw new IllegalActionException(
                    "Cannot resolve property for this component: " + component
                            + ". Its adapter class does not"
                            + " implement PropertyHelper.");
        }
        _adapterStore.put(component, (OntologyAdapter) adapterObject);

        return (OntologyAdapter) adapterObject;
    }

    /**
     * Return the package name that contains the class of this solver.
     *
     * @return The package name.
     * @exception IllegalActionException 
     */
    protected String _getPackageName() throws IllegalActionException {
        // FIXME: Is it a good idea to hard code the adapters string in the package name?
        // 12/17/09 Charles Shelton
        // This was missing adapters directory for the correct package name.
        return getClass().getPackage().getName() + ".adapters."
                + getOntology().getName();
    }

    /**
     * Return the top level of the model hierarchy for the model
     * we want to analyze.
     * 
     * @return The top level model object as a NamedObj
     */
    protected NamedObj _toplevel() {
        NamedObj toplevel = toplevel();

        // If the solver is in an OntologyAttribute, we
        // want to analyze the outside model.
        while (toplevel instanceof Configurer) {
            NamedObj configuredObject = ((Configurer) toplevel)
                    .getConfiguredObject();

            if (configuredObject == null) {
                return toplevel;
            }
            toplevel = configuredObject.toplevel();
        }
        return toplevel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The HashMap that caches components and their PropertyHelper objects.
     */
    protected HashMap<Object, OntologyAdapter> _adapterStore = new HashMap<Object, OntologyAdapter>();

    /**
     * The set of property-able objects that have non-settable property. A
     * non-settable property results from setting an object with a fixed
     * property through PropertyHelper.setEquals().
     */
    protected HashSet<Object> _nonSettables = new HashSet<Object>();

    /**
     * The HashMap that caches property-able objects and their
     * Property values.  Each mapping is a pair of Object and
     * Property.
     */
    protected HashMap<Object, Concept> _resolvedProperties = new HashMap<Object, Concept>();

    /**
     * The utilities shared between all solvers.
     */
    protected OntologySolverUtilities _ontologySolverUtilities;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _resetParser() {
        // Avoid FindBugs: Write to static field from instance method.
        _parser = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The expression parser.
     */
    private static PtParser _parser;

}
