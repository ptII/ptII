/* Interface for parameters that provide web export content.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.html;

import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.vergil.icon.ValueIcon;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;


///////////////////////////////////////////////////////////////////
//// DefaultTitle
/**
 * A parameter specifying default title to associate
 * with a model and with components in the model.
 * By default, this attribute uses the model name
 * and component names as the title.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DefaultTitle extends StringParameter implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public DefaultTitle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
                
        showTitleInHTML = new Parameter(this, "showTitleInHTML");
        showTitleInHTML.setExpression("true");
        showTitleInHTML.setTypeEquals(BaseType.BOOLEAN);

        include = new StringParameter(this, "include");
        include.addChoice("Entities");
        include.addChoice("Attributes");
        include.addChoice("All");
        include.addChoice("None");
        include.setExpression("Entities");
        
        instancesOf = new StringParameter(this, "instancesOf");
        
        // Add parameters that ensure this is rendered correctly in Vergil.
        new SingletonAttribute(this, "_hideName");
        new ValueIcon(this, "_icon");
        ConfigurableAttribute smallIcon = new ConfigurableAttribute(this, "_smallIconDescription");
        try {
            smallIcon.configure(null, null,
                    "<svg><text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\">title</text></svg>");
        } catch (Exception e) {
            // Show exception on the console. Should not occur.
            e.printStackTrace();
        }
        new VisibleParameterEditorFactory(this, "_editorFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** If non-empty (the default), specifies a class name.
     *  Only entities or attributes (depending on <i>include</i>)
     *  implementing the specified
     *  class will be assigned the title defined by this
     *  DefaultTitle parameter.
     */
    public StringParameter instancesOf;

    /** Specification of whether to provide the title for
     *  Attributes, Entities, or both. This is either "Entities" (the
     *  default), "Attributes", "All", or "None".
     */
    public StringParameter include;
    
    /** If set to true, then the title given by this parameter
     *  will be shown in the HTML prior to the image of the model
     *  (as well as in the image of the model, if it is visible
     *  when the export to web occurs). This is a boolean that
     *  defaults to true.
     */
    public Parameter showTitleInHTML;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class provides a default title for the web page
     *  and for each object
     *  as specified by <i>include</i> and <i>instancesOf</i>.
     *  @throws IllegalActionException If a subclass throws it.
     */
    public void provideContent(WebExporter exporter) throws IllegalActionException {
        
        String titleValue = stringValue();
        if (titleValue == null || titleValue.equals("")) {
            // Use the model name as the default title.
            titleValue = getContainer().getName();
        }
        exporter.setTitle(titleValue, ((BooleanToken)showTitleInHTML.getToken()).booleanValue());
        
        boolean entities = false, attributes = false;
        String includeValue = include.stringValue().toLowerCase();
        if (includeValue.equals("all")) {
            entities = true;
            attributes = true;
        } else if (includeValue.equals("entities")) {
            entities = true;
        } else if (includeValue.equals("attributes")) {
            attributes = true;
        }
        List<NamedObj> objects;
        String instances = instancesOf.stringValue();
        NamedObj container = getContainer();
        if (entities && container instanceof CompositeEntity) {
            if (instances.trim().equals("")) {
                objects = ((CompositeEntity)container).entityList();
            } else {
                try {
                    Class restrict = Class.forName(instances);
                    objects = ((CompositeEntity)container).entityList(restrict);
                } catch (ClassNotFoundException e) {
                    throw new IllegalActionException(this,
                            "No such class: " + instances);
                }
            }
            for (NamedObj object : objects) {
                _provideOutsideContent(exporter, object);
            }
        }
        if (attributes) {
            if (instances.trim().equals("")) {
                objects = ((CompositeEntity)container).attributeList();
            } else {
                try {
                    Class restrict = Class.forName(instances);
                    objects = ((CompositeEntity)container).attributeList(restrict);
                } catch (ClassNotFoundException e) {
                    throw new IllegalActionException(this,
                            "No such class: " + instances);
                }
            }
            for (NamedObj object : objects) {
                _provideOutsideContent(exporter, object);
            }
        }
    }
    
    /** Override the base class to not provide a title area attribute that is
     *  the name of the component.
     *  @throws IllegalActionException If a subclass throws it.
     */
    public void provideOutsideContent(WebExporter exporter) throws IllegalActionException {
        String titleValue = stringValue();
        if (titleValue == null || titleValue.equals("")) {
            // Use the model name as the default title.
            titleValue = getContainer().getName();
        }
        exporter.defineAreaAttribute(getContainer(), "title", titleValue, true);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Provide a title area attribute to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  @param exporter The exporter.
     *  @param object The object.
     *  @throws IllegalActionException If evaluating the value
     *   of this parameter fails.
     */
    protected void _provideOutsideContent(WebExporter exporter, NamedObj object)
            throws IllegalActionException {
        if (object != null) {
            exporter.defineAreaAttribute(object, "title", object.getName(), true);
        }
    }
}
