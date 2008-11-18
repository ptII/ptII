package ptolemy.data.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

public abstract class PropertyHelper {

    public Entity getContainerEntity(ASTPtRootNode node) {
        Attribute attribute = _solver.getAttribute(node);
        NamedObj container = attribute.getContainer();

        while (!(container instanceof Entity)) {
            container = container.getContainer();
        }
        return (Entity) container;
    }

    public String getName() {
        return "Helper_" + _component.toString();
    }

    public String toString() {
        return getName() + " " + super.toString();
    }

    /**
     * Return the root ast node for the given attribute.
     * @param attribute The given attribute.
     * @return The root ast node for the given attribute.
     * @throws IllegalActionException Thrown if 
     * PropertySolver.getParseTree(Attribute) throws it.
     */
    protected ASTPtRootNode getParseTree(Attribute attribute) 
    throws IllegalActionException {
        return _solver.getParseTree(attribute);
    }

    /**
     * Return the associated property solver
     * @return The property solver associated with this helper.
     */
    public PropertySolver getSolver() {
        return _solver;
    }


    /**
     * Record the association between the given ast node and the
     * given attribute.
     * @param node The given ast node.
     * @param attribute The given attribute.
     */
    protected void putAttribute(ASTPtRootNode node, Attribute attribute) {
        _solver.getSharedUtilities().putAttribute(node, attribute);
    }


    public static void resetAll() {
    }

    protected ParseTreeAnnotationEvaluator _annotationEvaluator;

    /** The associated component of this helper. */
    private Object _component;

    /** The associated property lattice. */
    protected PropertySolver _solver;

    /** Create an new use-case specific annotation evaluator */
    protected abstract ParseTreeAnnotationEvaluator _annotationEvaluator();    

    protected final ParseTreeAnnotationEvaluator _getAnnotationEvaluator() {
        if (_annotationEvaluator == null) {
            _annotationEvaluator = _annotationEvaluator();
        }
        return _annotationEvaluator;
    }

    /**
     * Return a list of property-able object(s) for this helper.
     * @return a list of property-able objects.
     * @throws IllegalActionException 
     */
    public abstract List<Object> getPropertyables() throws IllegalActionException;

    /**
     * Return the list of sub-helpers.
     * @return The list of sub-helpers.
     * @throws IllegalActionException
     */
    protected abstract List<PropertyHelper> _getSubHelpers() throws IllegalActionException;

    
    protected static List<Port> _getSinkPortList(IOPort port) {
        List<Port> result = new ArrayList<Port>();

        for (IOPort connectedPort : (List<IOPort>) port.connectedPortList()) {
            boolean isInput = connectedPort.isInput();
            boolean isCompositeOutput = 
                (connectedPort.getContainer() instanceof CompositeEntity)
                && !isInput &&            
                port.depthInHierarchy() > connectedPort.depthInHierarchy();            

                if (isInput || isCompositeOutput) {
                    result.add(connectedPort);
                }
        }
        return result;
    }

    protected static List<Port> _getSourcePortList(IOPort port) {
        List<Port> result = new ArrayList<Port>();

        for (IOPort connectedPort : (List<IOPort>) port.connectedPortList()) {
            boolean isInput = connectedPort.isInput();
            boolean isCompositeInput = 
                (connectedPort.getContainer() instanceof CompositeEntity)
                && isInput &&
                port.depthInHierarchy() > connectedPort.depthInHierarchy();            

                if (!isInput || isCompositeInput) {
                    result.add(connectedPort);
                }
        }
        return result;
    }    

    /**
     * Create a constraint that set the given object to be equal
     * to the given property. Mark the property of the given object
     * to be non-settable. 
     * @param object The given object.
     * @param property The given property.
     */
    public void setEquals(Object object, Property property) {
        _solver.setDeclaredProperty(object, property);
        _solver.setResolvedProperty(object, property);
        _solver.addNonSettable(object);        
    }

    /**
     * Reinitialize the helper before each round of property resolution.
     * It resets the resolved property of all propertyable objects. This
     * method is called in the beginning of the
     * PropertyConstraintSolver.resolveProperties() method.
     * @throws IllegalActionException Thrown if 
     */
    public void reinitialize() throws IllegalActionException {
        boolean record = _solver.isResolve() || _solver._isInvoked;
        boolean clearShowInfo = _solver.isResolve();


        if (_solver.isManualAnnotate()) {
            for (AnnotationAttribute annotation : _getAnnotationAttributes()) {
                _evaluateAnnotation(annotation);
            }
        }


        for (Object propertyable : getPropertyables()) {

            // Remove all PropertyAttributes.
            if (propertyable instanceof NamedObj) {
                NamedObj namedObj = (NamedObj) propertyable; 
                PropertyAttribute attribute = (PropertyAttribute) 
                namedObj.getAttribute(_solver.getExtendedUseCaseName());

                if (attribute != null) {
                    // Clear the property under two cases:
                    // 1. The (invoked or auxilary) solver is in trainingMode.
                    // 2. The solver is invoked under testing mode.
                    if (record) {

                        _solver.recordPreviousProperty(
                                propertyable, attribute.getProperty());

                        // Remove the property attribute.
                        try {
                            attribute.setContainer(null);

                        } catch (NameDuplicationException e) {
                            // This shouldn't happen since we are removing it.
                            assert false;
                        }

                    }
                }

                if (_solver.isSettable(propertyable)) {
                    _solver.clearResolvedProperty(propertyable);
                }

                if (clearShowInfo) {
                    // Clear the expression of the _showInfo attribute.
                    // It will be updated later.
                    StringParameter showAttribute = 
                        (StringParameter) namedObj.getAttribute("_showInfo");            
                    if (showAttribute != null) {                
                        showAttribute.setExpression("");
                    }
                }         
            }            
        }        

        // The recursive case.
        for (PropertyHelper helper : _getSubHelpers()) {
            helper.reinitialize();
        }
    }

    /**
     * 
     * @param annotation
     * @throws IllegalActionException
     */
    private void _evaluateAnnotation(AnnotationAttribute annotation)
    throws IllegalActionException {
        Map map;
        try {
            ASTPtRootNode parseTree = PropertySolver.getParser()
            .generateParseTree(annotation.getExpression());

            map = new HashMap();
            map.put(parseTree, parseTree);
        } catch (IllegalActionException ex) {
            map = PropertySolver.getParser()
            .generateAssignmentMap(annotation.getExpression());

        }
        
        // Evaluate each assignment constraints.
        for (Object assignment : map.values()) {
            ASTPtRootNode parseTree = (ASTPtRootNode) assignment;
            _getAnnotationEvaluator().evaluate(parseTree, this);
        }
    }

    /**
     * Return the list of annotation attributes relevant to this use-case. 
     * @return The list of annotation attributes.
     * @exception IllegalActionException Thrown if there is a problem
     *  obtaining the use-case identifier for an annotation attribute.
     */
    private List<AnnotationAttribute> _getAnnotationAttributes() 
    throws IllegalActionException {
        List result = new LinkedList();
        if (_component instanceof NamedObj) {

            for (Object attribute : ((NamedObj) _component).attributeList()) {

                if (AnnotationAttribute.class.isInstance(attribute)) {
                    String usecase = ((AnnotationAttribute) 
                            attribute).getUseCaseIdentifier();

                    if (_solver.isIdentifiable(usecase)) {
                        result.add(attribute);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the list of propertyable attributes for this helper.
     * @return The list of propertyable attributes.
     */
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = new LinkedList<Attribute>();
        Iterator attributes = 
            ((Entity) getComponent()).attributeList().iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();

            // only consider StringAttributes, ignore all subclasses
            if (attribute.getClass().equals(ptolemy.kernel.util.StringAttribute.class))  {
                if ((((StringAttribute)attribute).getName().equalsIgnoreCase("guardTransition")) ||
                        ((((StringAttribute)attribute).getContainer() instanceof Expression)) && 
                        ((StringAttribute)attribute).getName().equalsIgnoreCase("expression")) {

                    result.add((Attribute) attribute);
                }             
            } else if (attribute instanceof Variable) {
                if (((Variable) attribute).getVisibility() == Settable.FULL) {

                    // filter Parameters with certain names; ignore all subclasses
                    if (attribute instanceof PortParameter) {
                        result.add((Attribute) attribute);
                    } else if ((attribute.getClass().equals(ptolemy.data.expr.Parameter.class)) ||
                            (attribute.getClass().equals(ptolemy.data.expr.StringParameter.class))) {

//                      FIXME: implement filter interface, so that helper classes can specify which attributes
//                      need to be filtered (either by name or by class)
//                      Currently all filtered attributes need to be specified here                        
                        if (((Parameter)attribute).getName().equals("firingCountLimit") ||
                                ((Parameter)attribute).getName().equals("NONE") ||
                                ((Parameter)attribute).getName().equals("_hideName") ||
                                ((Parameter)attribute).getName().equals("_showName") ||
                                ((Parameter)attribute).getName().equals("conservativeAnalysis") ||
                                ((Parameter)attribute).getName().equals("directorClass") ||
                                ((Parameter)attribute).getName().equals("displayWidth")) {

                            // do nothing, ignore the parameter
                        } else {
                            result.add((Attribute) attribute);
                        }
                    }
                }
            }
        }        

        return result;
    }

    public List<Object> getPropertyables(Class filter) throws IllegalActionException {
        List<Object> list = new LinkedList<Object>();

        for (Object object : getPropertyables()) {
            if (filter.isInstance(object)) {
                list.add(object);
            }
        }
        return list;
    }

    /**
     * @param _component the _component to set
     */
    public void setComponent(Object _component) {
        this._component = _component;
    }

    /**
     * @return the _component
     */
    public Object getComponent() {
        return _component;
    }

    protected List<PropertyHelper> _getASTNodeHelpers() {
        List<PropertyHelper> astHelpers = new ArrayList<PropertyHelper>();
        ParseTreeASTNodeHelperCollector collector = 
            new ParseTreeASTNodeHelperCollector();

        for (ASTPtRootNode root : _getAttributeParseTrees()) {
            if (root != null) {
                try {
                    List<PropertyHelper> helpers = collector.collectHelpers(root, getSolver());
                    astHelpers.addAll(helpers);
                } catch (IllegalActionException ex) {
                    // This means the expression is not parse-able.
                    // FIXME: So, we will discard it for now.
                    throw new AssertionError(ex);
                }
            }
        }
        return astHelpers;
    }

    /**
     * Return the list of parse trees for all settable Attributes
     * of the component. 
     * @return The list of ASTPtRootNodes.
     */
    protected List<ASTPtRootNode> _getAttributeParseTrees() {
        List<ASTPtRootNode> result = new ArrayList<ASTPtRootNode>();

        Iterator attributes = null;

        attributes = _getPropertyableAttributes().iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();

            try {
                ASTPtRootNode pt = getParseTree(attribute);
                if (pt != null) {
                    result.add(pt);
                }                
            } catch (IllegalActionException ex) {
                // This means the expression is not parse-able.
                // FIXME: So, we will discard it for now.
                //System.out.println(KernelException.stackTraceToString(ex));
                throw new AssertionError(ex);
            }
        }
        return result;
    }

    /////////////////////////////////////////////////////////////////////
    ////                      public inner classes                   ////

    /** A class that defines a channel object. A channel object is
     *  specified by its port and its channel index in that port.
     */
    public static class Channel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct the channel with the given port and channel number.
         * @param portObject The given port.
         * @param channel The channel number of this object in the given port.
         */
        public Channel(IOPort portObject, int channel) {
            port = portObject;
            channelNumber = channel;
        }

        /**
         * Whether this channel is the same as the given object.
         * @param object The given object.
         * @return True if this channel is the same reference as the given
         *  object, otherwise false;
         */
        public boolean equals(Object object) {
            return object instanceof Channel
            && port.equals(((Channel) object).port)
            && channelNumber == ((Channel) object).channelNumber;
        }

        /**
         * Return the string representation of this channel.
         * @return The string representation of this channel.
         */
        public String toString() {
            return port.getName() + "_" + channelNumber;
        }

        /**
         * Return the hash code for this channel. Implementing this method
         * is required for comparing the equality of channels.
         * @return Hash code for this channel.
         */
        public int hashCode() {
            return port.hashCode() + channelNumber;
        }

        /** The port that contains this channel.
         */
        public IOPort port;

        /** The channel number of this channel.
         */
        public int channelNumber;
    }

}
