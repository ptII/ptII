/* An icon that copies the icon of an entity with the same container.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonConfigurableAttribute;
import ptolemy.kernel.util.Workspace;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;

//////////////////////////////////////////////////////////////////////////
//// CopyCatIcon

/**
 This is an icon that copies the icon of the last entity contained by
 the same container, if there is one, and behaves like the base class
 if not.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class CopyCatIcon extends XMLIcon {
    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public CopyCatIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        echos = new Parameter(this, "echos");
        echos.setTypeEquals(BaseType.INT);
        echos.setExpression("2");
        
        echoBoxColor = new ColorAttribute(this, "echoBoxColor");
        echoBoxColor.setExpression("{1.0, 1.0, 1.0, 1.0}");
        
        includeName = new Parameter(this, "includeName");
        includeName.setTypeEquals(BaseType.BOOLEAN);
        includeName.setExpression("false");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** Color of the echo boxes. This defaults to white. */
    public ColorAttribute echoBoxColor;

    /** The number of echos of the bounding box to draw. This is an
     *  int that defaults to 2.
     */
    public Parameter echos;
    
    /** If true, include the name of the copied actor in the icon
     *  if the name is included normally in its icon. NOTE: This
     *  will not include the name if the inside actor does not
     *  have an icon attribute, but only has an _iconDescription,
     *  so it's far from perfect.  This is a boolean
     *  that defaults to false.
     */
    public Parameter includeName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CopyCatIcon newObject = (CopyCatIcon) super.clone(workspace);
        newObject._originalDescription = null;
        return newObject;
    }

    /** Create a new background figure.  This method looks for entities
     *  contained by the same container, and if there are any, copies
     *  the icon of the last such entity.  If there are none, then it
     *  behaves like the base class.
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        Figure result = null;
        Nameable container = getContainer();

        if (container instanceof CompositeEntity) {
            CompositeEntity myContainer = ((CompositeEntity) container);
            ComponentEntity entity = null;
            Iterator entities = myContainer.entityList().iterator();

            while (entities.hasNext()) {
                entity = (ComponentEntity) entities.next();
            }

            try {
                if (entity != null) {
                    // Look for an icon within the entity.
                    EditorIcon icon = null;
                    Iterator icons = entity.attributeList(EditorIcon.class)
                            .iterator();

                    while (icons.hasNext()) {
                        icon = (EditorIcon) icons.next();
                    }

                    if (icon != null) {
                        if (((BooleanToken)includeName.getToken()).booleanValue()) {
                            result = icon.createFigure();
                        } else {
                            result = icon.createBackgroundFigure();
                        }
                    } else {
                        // If there is no icon, then maybe there is an
                        // _iconDescription attribute.
                        SingletonConfigurableAttribute description = (SingletonConfigurableAttribute) entity
                                .getAttribute("_iconDescription",
                                        SingletonConfigurableAttribute.class);

                        if (description != null) {
                            // Look for an icon description in my container.
                            SingletonConfigurableAttribute myDescription = (SingletonConfigurableAttribute) myContainer
                                    .getAttribute(
                                            "_iconDescription",
                                            SingletonConfigurableAttribute.class);

                            if (myDescription != null) {
                                // Save my original description, in case I go
                                // back to having nothing inside.
                                if (_originalDescription == null) {
                                    _originalDescription = myDescription
                                            .getConfigureText();
                                }

                                myDescription.configure(null, null, description
                                        .getConfigureText());
                            }
                        }
                    }
                } else {
                    // Restore the original description if we don't have
                    // one now.
                    if ((result == null) && (_originalDescription != null)) {
                        // Restore the original icon description.
                        // Look for an icon description in my container.
                        SingletonConfigurableAttribute myDescription = (SingletonConfigurableAttribute) myContainer
                                .getAttribute("_iconDescription",
                                        SingletonConfigurableAttribute.class);

                        if (myDescription != null) {
                            myDescription.configure(null, null,
                                    _originalDescription);
                        }
                    }
                }
            } catch (Throwable throwable) {
                // Ignore and use default icon.
            }
        }

        // If all else fails, behave like the superclass.
        if (result == null) {
            result = super.createBackgroundFigure();
        }

        // Wrap in a CompositeFigure with echos of the bounding box.
        // Note that the bounds here are actually bigger than the
        // bounding box, which may be OK in this case.
        int numberOfEchos = 2;
        try {
            numberOfEchos = ((IntToken)echos.getToken()).intValue();
        } catch (IllegalActionException ex) {
            // Ignore and use the default.
        }
        Rectangle2D bounds = result.getBounds();
        CompositeFigure composite = new CompositeFigure();
        for (int i = numberOfEchos; i > 0; i--) {
            BasicRectangle rectangle = new BasicRectangle(
                    bounds.getX() + 5.0*i - _MARGIN,
                    bounds.getY() + 5.0*i - _MARGIN,
                    bounds.getWidth() + 2 * _MARGIN,
                    bounds.getHeight() + 2 * _MARGIN,
                    echoBoxColor.asColor());
            composite.add(rectangle);            
        }
        BasicRectangle rectangle3 = new BasicRectangle(bounds.getX() - _MARGIN,
                bounds.getY() - _MARGIN,
                bounds.getWidth() + _MARGIN * 2,
                bounds.getHeight() + _MARGIN * 2,
                Color.white);
        composite.add(rectangle3);
        composite.add(result);
        return composite;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Original description of the icon. */
    private String _originalDescription = null;
    
    /** Margin around the inside icon. */
    private static int _MARGIN = 2;
    
}
