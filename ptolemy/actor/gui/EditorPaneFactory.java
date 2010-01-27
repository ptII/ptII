/* An attribute that creates an editor pane to configure its container.

 Copyright (c) 1998-2010 The Regents of the University of California.
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import ptolemy.kernel.DecoratedAttributesImplementation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DecoratedAttributes;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// EditorPaneFactory

/**
 This is an attribute that can create a pane (called a "configuration
 widget") for interactively configuring its container.  To use this,
 place an instance of this class (or a derived class) inside a Ptolemy II
 object.  When the user double clicks on the icon for that object,
 or selects Configure from the context menu, then a dialog is opened
 containing the pane returned by createEditorPane().
 <p>
 In this base class, the createEditorPane() method creates an
 instance of PtolemyQuery with one entry for each parameter in
 the container. This is the default mechanism
 for editing parameters.  Derived classes may override this
 method to present radically different interfaces to the user.
 For example, a digital filter actor could present a filter
 design interface.  A plotter actor could present a panel for
 configuring a plot.  A file reader actor could present a file
 browser.

 @see Configurer
 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class EditorPaneFactory extends Attribute {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public EditorPaneFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new widget for configuring the container.  In this
     *  base class, this method defers to the static createEditorPane method.
     *  Subclasses that implement specialized interaction should override
     *  this method to create an appropriate type of component.
     *  @return A new widget for configuring the container.
     */
    public Component createEditorPane() {
        return createEditorPane(getContainer());
    }

    /** Return a new default widget for configuring the specified object.
     *  This is used by the Configurer for objects that do not contain
     *  an instance of EditorPaneFactory as an attribute.  The resulting
     *  component is an instance of the PtolemyQuery class that
     *  @param object The object to be configured.
     *  @return An instance of the PtolemyQuery class that is created
     *  with styles according to the type given in each visible attribute.
     */
    public static Component createEditorPane(NamedObj object) {
        PtolemyQuery query = new PtolemyQuery(object);
        query.setTextWidth(40);
        return createEditorPane(object, query);
    }

    /** Return a new default widget for configuring the specified object.
     *  This is used by the Configurer for objects that do not contain
     *  an instance of EditorPaneFactory as an attribute.  The resulting
     *  component is an instance of the PtolemyQuery class that
     *  @param object The object to be configured.
     *  @param query The query to which new entries for the object are to be
     *  added.
     *  @return An instance of the PtolemyQuery class that is created
     *  with styles according to the type given in each visible attribute.
     */
    public static Component createEditorPane(NamedObj object,
            PtolemyQuery query) {

        // Get decorated attributes
        
        JTabbedPane tabs = new JTabbedPane();
        boolean foundOne = false;
        
        List<Settable> parameters = new LinkedList<Settable>(object.attributeList(Settable.class));
        int nbrOfTabs = 1;
        PtolemyQuery mainTab = new PtolemyQuery(object);
        mainTab.setTextWidth(40);
        tabs.addTab(object.getDisplayName(), mainTab);
        
        List<Decorator> decorators = DecoratedAttributesImplementation.findDecorators(object);
        for (Decorator decorator : decorators) {            
            DecoratedAttributes decoratedAttributes = object.getDecoratorAttributes(decorator);
            
            PtolemyQuery decoratorQuery = new PtolemyQuery(object);
            decoratorQuery.setAlignmentY(PtolemyQuery.TOP_ALIGNMENT);
            decoratorQuery.setTextWidth(40);
            boolean foundDecoratorAttribute = false;
            
            for (Object attribute : decoratedAttributes.attributeList()) {
                if (attribute instanceof Settable) {
                    Settable settable = (Settable) attribute;
                    if (Configurer.isVisible(object, settable)) {                        
                        foundDecoratorAttribute = true;
                        decoratorQuery.addStyledEntry(settable);
                    }                    
                }
            }
            foundOne = foundOne || foundDecoratorAttribute;
            if (foundDecoratorAttribute) {
                tabs.addTab(decorator.getFullName(), decoratorQuery);
                nbrOfTabs += 1;
            }
        }
        
        if (nbrOfTabs > 1) {
            query.add(tabs, BorderLayout.CENTER);
        }

        PtolemyQuery queryForMainAttributes = (nbrOfTabs > 1) ? mainTab : query; 
        
        for (Settable parameter : parameters) {
            if (Configurer.isVisible(object, parameter)) {
                foundOne = true;
                queryForMainAttributes.addStyledEntry(parameter);
            }
        }
        
        if (!foundOne) {
            return new JLabel(object.getName() + " has no parameters.");
        }

        return query;
    }
}
