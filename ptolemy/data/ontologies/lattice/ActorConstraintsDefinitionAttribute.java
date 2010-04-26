/* Attribute that defines the lattice ontology solver constraints for an actor.

 Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.OntologySolverModel;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonConfigurableAttribute;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// ActorConstraintsDefinitionAttribute

/** Attribute that defines the lattice ontology solver constraints for an actor.
 *  
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ActorConstraintsDefinitionAttribute extends Attribute {

    /** Construct the ActorConstraintsDefinitionAttribute attribute
     *  with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ActorConstraintsDefinitionAttribute(CompositeEntity container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        if (!(container instanceof OntologySolverModel)) {
            throw new IllegalActionException(
                    this,
                    "An ActorConstraintsDefinitionAttribute "
                            + "must be contained by an OntologySolverModel entity.");
        }

        actorClassName = new StringParameter(this, "actorClassName");

        foundActorClassName = new StringParameter(this, "foundActorClassName");
        foundActorClassName.setVisibility(Settable.NONE);
        foundActorClassName.setPersistent(true);

        _constraintTermExpressions = new LinkedList<StringParameter>();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-40\" width=\"75\" height=\"40\" "
                + "style=\"fill:white\"/>" + "<text x=\"-45\" y=\"-25\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "      Actor\nConstraints</text></svg>");

        SingletonConfigurableAttribute description = (SingletonConfigurableAttribute) this
                .getAttribute("_iconDescription");
        description.setPersistent(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////

    /** The string that represents the class name of the actor for which
     *  this attribute defines lattice ontology solver constraints.
     */
    public StringParameter actorClassName;

    /** The parameter that contains the last valid actor class name found. */
    public StringParameter foundActorClassName;

    /** The string suffix for attribute names that represent constraint definitions
     *  for actor attributes.
     */
    public static final String ATTR_SUFFIX = "AttrTerm";

    /** String representing an equal to constraint choice. */
    public static final String EQ = "==";

    /** String representing a greater than or equal to constraint choice. */
    public static final String GTE = ">=";

    /** String representing that the actor port or attribute should be
     *  ignored for the ontology analysis and not have a concept assigned to it.
     */
    public static final String IGNORE = "IGNORE_ELEMENT";

    /** String representing a less than or equal to constraint choice. */
    public static final String LTE = "<=";

    /** String representing that the actor port or attribute has no constraints
     *  but should have a concept assigned to it.
     */
    public static final String NO_CONSTRAINTS = "NO_CONSTRAINTS";

    /** The string suffix for attribute names that represent constraint definitions
     *  for actor ports.
     */
    public static final String PORT_SUFFIX = "PortTerm";

    /** String representing the separator character ";" between
     *  constraint expressions in the constraint expression string.
     */
    public static final String SEPARATOR = ";";

    /** The length of the attribute and port suffix strings. */
    public static final int SUFFIX_LENGTH = 8;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the attributeChanged method so that if the actor
     *  class name changes, the attribute interface adds and removes
     *  fields for constraints for the actor's ports and attributes.
     *  @param attribute The attribute that has been changed.
     *  @throws IllegalActionException If there is a problem changing the attribute.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == actorClassName) {

            // Collect all the existing constraint parameter fields
            // before checking if we need to remove them because the
            // actor class name changed. This is necessary for the
            // case when the attribute is loaded from a MoML file.
            _constraintTermExpressions.clear();
            for (Object constraintParameter : attributeList(StringParameter.class)) {
                if (isActorElementAnAttribute((StringParameter) constraintParameter)
                        || isActorElementAPort((StringParameter) constraintParameter)) {
                    _constraintTermExpressions
                            .add((StringParameter) constraintParameter);
                }
            }
            
            StringToken actorClassNameToken = (StringToken) actorClassName.getToken();
            String actorClassNameString = actorClassNameToken.stringValue();
            StringToken foundActorClassNameToken = (StringToken) foundActorClassName.getToken();
            String foundActorClassNameString = foundActorClassNameToken.stringValue();

            if (!actorClassNameString.equals("")
                    && !actorClassNameString.equals(
                            foundActorClassNameString)) {
                
                Class actorClass = null;                
                try {
                    // Verify that the actorClassName correctly
                    // specifies an existing class.
                    actorClass = Class.forName(actorClassNameString);
                } catch (ClassNotFoundException classEx) {
                    throw new IllegalActionException(this, classEx,
                            "Actor class " + actorClassNameString
                                    + " not found.");
                }
                
                // Instantiate a temporary actor from this class in order
                // to get all the port and attribute information.
                ComponentEntity tempActorInstance = (ComponentEntity) _createTempActorInstance(actorClass);
                try {
                    // Remove all the old constraint parameters
                    // that no longer apply since the actor class
                    // name has changed.
                    for (StringParameter constraintParameter : _constraintTermExpressions) {
                        constraintParameter.setContainer(null);
                    }
                    _constraintTermExpressions.clear();

                    // Create a constraint expression parameter
                    // for every port and attribute in the actor.
                    for (Object actorPort : tempActorInstance
                            .portList()) {
                        StringParameter constraintExpression = new StringParameter(
                                    this, ((Port) actorPort).getName()
                                        + PORT_SUFFIX);
                        _constraintTermExpressions
                                .add(constraintExpression);
                    }

                    for (Object actorAttribute : tempActorInstance
                                .attributeList()) {
                        if (!((Attribute) actorAttribute).getName()
                                    .startsWith("_")) {
                            StringParameter constraintExpression = new StringParameter(
                                    this, ((Attribute) actorAttribute)
                                            .getName()
                                            + ATTR_SUFFIX);
                            _constraintTermExpressions
                                    .add(constraintExpression);
                        }
                    }                      
                    tempActorInstance.setContainer(null);
                } catch (NameDuplicationException ex) {
                    throw new IllegalActionException(
                            this,
                            ex,
                            "Error when trying to "
                                    + "create a string attribute "
                                    + "in ActorConstraintsDefinitionAttribute "
                                    + this + " because an attribute of the same name already exisits.");
                }

                // Set the icon for the attribute so that it
                // looks like the actor for which it defines
                // an OntologyAdapter.
                _setActorIcon(actorClassNameString, tempActorInstance);

                // Set the found actor name parameter so that
                // we know what the previous actor class was
                // set to the next time attributeChanged is
                // called.
                foundActorClassName.setToken(actorClassNameToken);
            }
        }

        super.attributeChanged(attribute);
    }
    
    /** Get the name of the element contained by the actor (either a
     *  port or an attribute) for which the specified parameter
     *  defines a constraint.
     *  @param expressionParameter The string parameter that defines
     *   the element's constraints.
     *  @return The string name of the element (either a port or an attribute).
     *  @throws IllegalActionException If the expressionParameter passed in is null.
     */
    public static String getActorElementName(StringParameter expressionParameter)
        throws IllegalActionException {
        if (expressionParameter == null) {
            throw new IllegalActionException("The constraint expression for the actor" +
                        " element cannot be null.");
        }
        String elementName = expressionParameter.getName();
        elementName = elementName.substring(0, elementName.length()
                - SUFFIX_LENGTH);
        return elementName;
    }

    /** Get the adapter defined by this attribute.
     *  @param component The model component for which the adapter will be created.
     *  @return The ActorConstraintsDefinitionAdapter specified by this attribute.
     *  @exception IllegalActionException If the container model's
     *   solver cannot be found or there is a problem initializing the
     *   adapter.
     */
    public ActorConstraintsDefinitionAdapter createAdapter(ComponentEntity component)
            throws IllegalActionException {
        String actorClassNameString = ((StringToken) actorClassName.getToken()).stringValue();
        try {
            if (component.getClass() != Class.forName(actorClassNameString)) {
                throw new IllegalActionException(this, "The component "
                        + component
                        + " passed in for the adapter is not of class "
                        + actorClassName.getExpression() + ".");
            }
        } catch (ClassNotFoundException classEx) {
            throw new IllegalActionException(this, classEx, "Actor class "
                    + actorClassNameString + " not found.");
        }

        LatticeOntologySolver solver = (LatticeOntologySolver) ((OntologySolverModel) getContainer())
                .getContainerSolver();

        // If the solver is null, throw an exception.
        if (solver == null) {
            throw new IllegalActionException(this, "The OntologySolverModel "
                    + " does not have an associated OntologySolver so no "
                    + " OntologyAdapter can be created.");
        }

        // Get the adapter for the actor.
        return new ActorConstraintsDefinitionAdapter(solver, component,
                _constraintTermExpressions);
    }
    
    /** Return the string constraint direction for the given constraint expression specified
     *  as a string.
     *  @param constraintExpressionString The string that specifies a single constraint
     *   expression for an actor element.
     *  @return The string '<=', '>=', or '==' depending on the specified constraint
     *   direction, false otherwise.
     */
    public static List<String> getConstraintDirAndRHSStrings(String constraintExpressionString) {
        if (constraintExpressionString == null) {
            return null;
        } else {
            List<String> dirAndRHSStrings = new ArrayList<String>(2);
            if (constraintExpressionString.startsWith(GTE)) {
                dirAndRHSStrings.add(GTE);
                dirAndRHSStrings.add(constraintExpressionString.substring(GTE.length()));
            } else if (constraintExpressionString.startsWith(LTE)) {
                dirAndRHSStrings.add(LTE);
                dirAndRHSStrings.add(constraintExpressionString.substring(LTE.length()));
            } else if (constraintExpressionString.startsWith(EQ)) {
                dirAndRHSStrings.add(EQ);
                dirAndRHSStrings.add(constraintExpressionString.substring(EQ.length()));
            } else {
                return null;
            }
            return dirAndRHSStrings;
        }
    }
    
    /** Return true if the actor element constraint expression is for an attribute,
     *  false otherwise.
     *  @param actorElementConstraintExpression The constraint expression for the actor element.
     *  @return true if the actor element constraint expression is for an attribute,
     *   false otherwise.
     *  @throws IllegalActionException If the constrain expression parameter is null.
     */
    public static boolean isActorElementAnAttribute(StringParameter actorElementConstraintExpression)
        throws IllegalActionException {
        if (actorElementConstraintExpression == null) {
            throw new IllegalActionException("The constraint expression for the actor" +
                        " element cannot be null.");
        }
        return actorElementConstraintExpression.getName().endsWith(ATTR_SUFFIX);
    }

    /** Return true if the actor element constraint expression is for a port,
     *  false otherwise.
     *  @param actorElementConstraintExpression The constraint expression for the actor element.
     *  @return true if the actor element constraint expression is for a port,
     *   false otherwise.
     *  @throws IllegalActionException If the constrain expression parameter is null.
     */
    public static boolean isActorElementAPort(StringParameter actorElementConstraintExpression)
        throws IllegalActionException {
        if (actorElementConstraintExpression == null) {
            throw new IllegalActionException("The constraint expression for the actor" +
                        " element cannot be null.");
        }
        return actorElementConstraintExpression.getName().endsWith(PORT_SUFFIX);
    }

    /** Return true if the actor element is set to be ignored by
     *  the ontology analysis, false otherwise.
     *  @param actorElementConstraintExpression The constraint expression
     *   for the actor element.
     *  @return true if the actor element is set to be ignored by
     *   the ontology analysis, false otherwise.
     *  @throws IllegalActionException If the constrain expression parameter is null.
     */
    public static boolean isActorElementIgnored(StringParameter actorElementConstraintExpression)
        throws IllegalActionException {
        if (actorElementConstraintExpression == null) {
            throw new IllegalActionException("The constraint expression for the actor" +
                        " element cannot be null.");
        }
        return actorElementConstraintExpression.getExpression().trim().equals(IGNORE);
    }

    /** Return true if the actor element is set to have no constraints for
     *  the ontology analysis, false otherwise.
     *  @param actorElementConstraintExpression The constraint expression
     *   for the actor element.
     *  @return true if the actor element is set to have no constraints for
     *   the ontology analysis, false otherwise.
     *  @throws IllegalActionException If the constrain expression parameter is null.
     */
    public static boolean isActorElementUnconstrained(StringParameter actorElementConstraintExpression)
        throws IllegalActionException {
        if (actorElementConstraintExpression == null) {
            throw new IllegalActionException("The constraint expression for the actor" +
                        " element cannot be null.");
        }
        return actorElementConstraintExpression.getExpression().trim().equals(NO_CONSTRAINTS);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Return a temporary instance of the actor class in the current container.  This
     *  instance will be used to find all the ports and attributes for the actor which
     *  we need to populate the fields of the ActorConstraintsDefinitionAttribute.
     *  @param actorClass The class of the actor to be instantiated.
     *  @return A new instance of the specified actor class.
     *  @throws IllegalActionException If the actor class cannot be instantiated.
     */
    protected Object _createTempActorInstance(Class actorClass)
        throws IllegalActionException {
        
        Constructor actorConstructor = null;        
        try {
            actorConstructor = actorClass
                .getConstructor(new Class[] {
                        CompositeEntity.class, String.class });
        } catch (NoSuchMethodException ex) {
            throw new IllegalActionException(this, ex, "Could not find the constructor" +
            		" method for the actor class " + actorClass + ".");
        }
        
        Object actorInstance = null;
        try {
            actorInstance = actorConstructor
                .newInstance(new Object[] {
                        (CompositeEntity) this.getContainer(),
                        "tempActor" });
        } catch (InvocationTargetException ex) {
            throw new IllegalActionException(this, ex, "Exception thrown when trying to call" +
            		" the constructor for the actor class " + actorClass + ".");
        } catch (IllegalArgumentException ex) {
            throw new IllegalActionException(this, ex, "Invalid argument passed to" +
                    " the constructor for the actor class " + actorClass + ".");
        } catch (InstantiationException ex) {
            throw new IllegalActionException(this, ex, "Unable to instantiate" +
                    " the actor class " + actorClass + ".");
        } catch (IllegalAccessException ex) {
            throw new IllegalActionException(this, ex, "Do not have access " +
                    " the constructor for the actor class " + actorClass +
                    " within this method.");
        }
        
        return actorInstance;        
    }
    
    /** Set the ActorConstraintsDefinitionAttribute icon to be the same icon as that of
     *  the actor for which it is defining constraints.  The actor icon can be found either from
     *  an XML file that defines the actor icon, or its _iconDescription attribute.
     *  @param actorClassNameString The fully qualified name of the class with its package prefix.
     *  @param tempActorInstance A temporary instance of the actor from which its icon description attribute can
     *   be taken.
     *  @throws IllegalActionException If a problem occurs when trying to set the actor icon.
     */
    protected void _setActorIcon(String actorClassNameString, ComponentEntity tempActorInstance)
        throws IllegalActionException {
        
        // First look for an icon file for the actor.        
        String iconFile = actorClassNameString
                .replace('.', '/')
                + "Icon.xml";
        URL xmlFile = tempActorInstance.getClass().getClassLoader().getResource(
                iconFile);
        if (xmlFile != null) {
            MoMLParser parser = new MoMLParser(this.workspace());
            parser.setContext(this);
            try {
                parser.parse(xmlFile, xmlFile);
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex, "Failed to parse the actor icon's XML" +
                		" file when trying to set the actor icon.");
            }
        }

        // If no actor icon file was found, then use the _iconDescription
        // attribute.
        if (xmlFile == null) {
            ConfigurableAttribute actorIconAttribute = (ConfigurableAttribute) ((ComponentEntity) tempActorInstance)
                    .getAttribute("_iconDescription");
            if (actorIconAttribute != null) {
                String iconDescription = actorIconAttribute
                        .getConfigureText();
                SingletonConfigurableAttribute description = (SingletonConfigurableAttribute) this
                        .getAttribute("_iconDescription");
                try {
                    description.configure(null, null,
                            iconDescription);
                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex, "Failed to configure the _iconDescription" +
                    		"attribute when trying to set the actor icon.");
                }
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of expressions that represent the constraint
     *  terms for each constraint in the actor.
     */
    protected List<StringParameter> _constraintTermExpressions;
}
