/* An Icon is the graphical representation of an entity or attribute.

 Copyright (c) 1999-2010 The Regents of the University of California.
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
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.SwingConstants;

import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.kernel.attributes.FilledShapeAttribute;
import diva.canvas.CanvasUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;
import diva.gui.toolbox.FigureIcon;

///////////////////////////////////////////////////////////////////
//// EditorIcon

/**
 An icon is the visual representation of an entity or attribute.
 The visual representation is a Diva Figure. This class is an attribute
 that serves as a factory for such figures. This base class creates the
 figure by composing the figures of any contained attributes that have
 icons.  If there are no such contained attributes, then it creates a
 default figure that is a white rectangle. This class also provides
 a facility for generating a Swing icon (i.e. an instance of
 javax.swing.Icon) from that figure (the createIcon() method).
 <p>
 The icon consists of a background figure, created by the
 createBackgroundFigure() method, and a decorated version, created
 by the createFigure() method.  The decorated version has, in this
 base class, a label showing the name of the entity, unless the entity
 contains a parameter called "_hideName" with value true.
 The swing icon created by createIcon() does not include the
 decorations, but rather is only the background figure.
 <p>
 The decorated version can also optionally show parameter values
 below the icon.  If the preference named "_showParameters"
 has value "All", then all parameters are shown. If it has
 value "Overridden parameters only", then it will show
 only overridden parameters. In either case, only
 parameters that are visible and settable (see the Settable
 interface) will be shown, regardless of whether they are overridden.
 <p>
 When the preference "_showParameters" has value
 "Overridden parameters only", then some parameter values
 may be suppressed even if they are overridden. In particular,
 if an attribute contains a parameter named "_hide" with value
 true, then that parameter is now shown even if requested.
 If the container of the attribute contains a parameter named
 "_hideAllParameters" with value true, then none of its
 parameters are shown.
 This is useful, for example, if the icon itself shows
 the parameter, as with decorative visual elements.
 <p>
 Derived classes may simply populate this attribute with other
 visible attributes (attributes that contain icons), or they can
 override the createBackgroundFigure() method.  This will affect
 both the Diva Figure and the Swing Icon representations.
 Derived classes can also create the figure or the icon in a
 different way entirely (for example, starting with a Swing
 icon and creating the figure using a SwingWrapper) by overriding
 both createBackgroundFigure() and createIcon(). However, the
 icon editor provided by EditIconFrame and EditIconTableau
 will only show (and allow editing) of those icon components
 created by populating this attribute with other visible
 attributes.
 <p>
 This attribute contains another attribute that is an
 instance of EditIconTableau. This has the effect that
 an instance of Configuration, when it attempts to open
 an instance of this class, will use EditIconTableau,
 which in turn uses EditIconFrame to provide an icon
 editor.

 @author Steve Neuendorffer, John Reekie, Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (johnr)
 @see EditIconFrame
 @see EditIconTableau
 @see ptolemy.actor.gui.Configuration
 */
public class EditorIcon extends Attribute {
    /** Construct an icon in the specified workspace and name.
     *  This constructor is typically used in conjunction with
     *  setContainerToBe() and createFigure() to create an icon
     *  and generate a figure without having to have write access
     *  to the workspace.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  @see #setContainerToBe(NamedObj)
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the specified name contains
     *   a period.
     */
    public EditorIcon(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace);

        try {
            setName(name);

            // Create a tableau factory so that an instance
            // of this class is opened using the EditIconTableau.
            Attribute tableauFactory = new EditIconTableau.Factory(this,
                    "_tableauFactory");
            tableauFactory.setPersistent(false);
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(ex);
        }
    }

    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public EditorIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create a tableau factory so that an instance
        // of this class is opened using the EditIconTableau.
        Attribute tableauFactory = new EditIconTableau.Factory(this,
                "_tableauFactory");
        tableauFactory.setPersistent(false);
    }

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
        EditorIcon newObject = (EditorIcon) super.clone(workspace);
        newObject._containerToBe = null;
        newObject._iconCache = null;
        return newObject;
    }

    /** Create a new background figure.  This figure is a composition of
     *  the figures of any contained visible attributes. If there are no such
     *  visible attributes, then this figure is a simple white box.
     *  If you override this method, keep in mind that this method is expected
     *  to manufacture a new figure each time, since figures are
     *  inexpensive and contain their own location and transformations.
     *  This method should never return null.
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        // If this icon itself contains any visible attributes, then
        // compose their background figures to make this one.
        CompositeFigure figure = null;
        Iterator attributes = attributeList().iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();

            // There is a level of indirection where the "subIcon" is a
            // "visible attribute" containing an attribute named "_icon"
            // that actually has the icon.
            Iterator subIcons = attribute.attributeList(EditorIcon.class)
                    .iterator();

            while (subIcons.hasNext()) {
                EditorIcon subIcon = (EditorIcon) subIcons.next();

                if (figure == null) {
                    // NOTE: This used to use a constructor that
                    // takes a "background figure" argument, which would
                    // then treat this first figure specially.  This is not
                    // right since getShape() on the figure will then return
                    // only the shape of the composite, which results in the
                    // wrong selection region being highlighted.
                    figure = new CompositeFigure();
                }

                Figure subFigure = subIcon.createBackgroundFigure();

                // Translate the figure to the location of the subfigure,
                // if there is a location. Also, center it if necessary.
                try {
                    // NOTE: This is inelegant, but only the subclasses
                    // have the notion of centering.
                    // FIXME: Don't use FilledShapeAttribute... promote
                    // centered capability to a base class.
                    if (attribute instanceof FilledShapeAttribute
                            && subFigure instanceof BasicFigure) {
                        boolean centeredValue = ((BooleanToken) ((FilledShapeAttribute) attribute).centered
                                .getToken()).booleanValue();

                        if (centeredValue) {
                            ((BasicFigure) subFigure).setCentered(true);
                        }
                    }

                    Locatable location = (Locatable) attribute.getAttribute(
                            "_location", Locatable.class);

                    if (location != null) {
                        double[] locationValue = location.getLocation();
                        CanvasUtilities.translateTo(subFigure,
                                locationValue[0], locationValue[1]);
                    }
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }

                figure.add(subFigure);
            }
        }

        if (figure == null) {
            // There are no component figures.
            return _createDefaultBackgroundFigure();
        } else {
            return figure;
        }
    }

    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of CompositeFigure with the
     *  figure returned by createBackgroundFigure() as its background.
     *  This method adds a LabelFigure to the CompositeFigure that
     *  contains the name of the container of this icon, unless the
     *  container has a parameter called "_hideName" with value true.
     *  If the container has an attribute called "_centerName" with
     *  value true, then the name is rendered
     *  in the center of the background figure, rather than above it.
     *  This method should never return null, even if the icon has
     *  not been properly initialized.
     *  @return A new CompositeFigure consisting of the background figure
     *   and a label.
     */
    public Figure createFigure() {
        Figure background = createBackgroundFigure();
        Rectangle2D backBounds;
        try {
            // Applets can throw a NPE here if there are problems getting
            // the image.
            backBounds = background.getBounds();
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Failed to get the bounds of the background figure \""
                            + (background == null ? "null" : background));
        }
        CompositeFigure figure = new CompositeFigure(background);

        NamedObj container = (NamedObj) getContainerOrContainerToBe();

        // Create the label, unless this is a visible attribute,
        // which typically carries no label.
        // NOTE: backward compatibility problem...
        // Old style annotations now have labels...
        if (!_isPropertySet(container, "_hideName")) {
            String name = container.getDisplayName();

            // Do not add a label figure if the name is null.
            if ((name != null) && !name.equals("")) {
                if (!_isPropertySet(container, "_centerName")) {
                    LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                            SwingConstants.SOUTH_WEST);

                    // Shift the label slightly right so it doesn't
                    // collide with ports.
                    label.translateTo(backBounds.getX() + 5, backBounds.getY());
                    figure.add(label);
                } else {
                    LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                            SwingConstants.CENTER);
                    label.translateTo(backBounds.getCenterX(), backBounds
                            .getCenterY());
                    figure.add(label);
                }
            }
        }

        // If specified by a preference, then show parameters.
        Token show = PtolemyPreferences.preferenceValueLocal(container,
                "_showParameters");

        if (show instanceof StringToken) {
            String value = ((StringToken) show).stringValue();
            boolean showOverriddenParameters = value
                    .equals("Overridden parameters only");
            boolean showAllParameters = value.equals("All");

            if ((showOverriddenParameters && !_isPropertySet(container,
                    "_hideAllParameters"))
                    || showAllParameters) {
                StringBuffer parameters = new StringBuffer();
                Iterator settables = container.attributeList(Settable.class)
                        .iterator();

                while (settables.hasNext()) {
                    Settable settable = (Settable) settables.next();

                    if (settable.getVisibility() != Settable.FULL) {
                        continue;
                    }

                    if (!showAllParameters
                            && !((NamedObj) settable).isOverridden()) {
                        continue;
                    }

                    if (!showAllParameters
                            && _isPropertySet((NamedObj) settable, "_hide")) {
                        continue;
                    }

                    String name = settable.getName();
                    String displayName = settable.getDisplayName();
                    parameters.append(displayName);

                    if (showAllParameters && !name.equals(displayName)) {
                        parameters.append(" (" + name + ")");
                    }

                    parameters.append(": ");
                    parameters.append(settable.getExpression());

                    if (settables.hasNext()) {
                        parameters.append("\n");
                    }
                }

                LabelFigure label = new LabelFigure(parameters.toString(),
                        _parameterFont, 1.0, SwingConstants.NORTH_WEST);

                // Shift the label slightly right so it doesn't
                // collide with ports.
                label.translateTo(backBounds.getX() + 5, backBounds.getY()
                        + backBounds.getHeight());
                figure.add(label);
            }
        }

        return figure;
    }

    /** Create a new Swing icon.  In this base class, this icon is created
     *  from the background figure returned by createBackgroundFigure().
     *  Note that the background figure does NOT include a label for the name.
     *  This method might be suitable, for example, for creating a small icon
     *  for use in a library.
     *  @return A new Swing Icon.
     */
    public javax.swing.Icon createIcon() {
        // In this class, we cache the rendered icon, since creating icons from
        // figures is expensive.
        if (_iconCache != null) {
            return _iconCache;
        }

        // No cached object, so rerender the icon.
        Figure figure = createBackgroundFigure();
        _iconCache = new FigureIcon(figure, 20, 15);
        return _iconCache;
    }

    /** Return the container of this object, if there is one, or
     *  if not, the container specified by setContainerToBe(), if
     *  there is one, or if not, null. This rather specialized method is
     *  used to create an icon and generate a figure without having
     *  to have write access to the workspace. To use it, use the
     *  constructor that takes a workspace and a name, then call
     *  setContainerToBe() to indicate what the container will be. You
     *  can then call createFigure() or createBackgroundFigure(),
     *  and the appropriate figure for the container specified here
     *  will be used.  Then queue a ChangeRequest that sets the
     *  container to the same specified container. Once the container
     *  has been set by calling setContainer(), then the object
     *  specified to this method is no longer relevant.
     *
     *  @see #setContainerToBe(NamedObj)
     *  @return The container of this object, if there is one, or
     *  if not hte container specified by setContainerToBe().
     */
    public Nameable getContainerOrContainerToBe() {
        Nameable container = getContainer();

        if (container != null) {
            return container;
        } else {
            return _containerToBe;
        }
    }

    /** Indicate that the container of this icon will eventually
     *  be the specified object. This rather specialized method is
     *  used to create an icon and generate a figure without having
     *  to have write access to the workspace. To use it, use the
     *  constructor that takes a workspace and a name, then call
     *  this method to indicate what the container will be. You
     *  can then call createFigure() or createBackgroundFigure(),
     *  and the appropriate figure for the container specified here
     *  will be used.  Then queue a ChangeRequest that sets the
     *  container to the same specified container. Once the container
     *  has been set by calling setContainer(), then the object
     *  specified to this method is no longer relevant.
     *  @param container The container that will eventually be set.
     *  @see #getContainerOrContainerToBe()
     */
    public void setContainerToBe(NamedObj container) {
        _containerToBe = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new default background figure, which is a white box.
     *  Subclasses of this class should generally override
     *  the createBackgroundFigure method instead.  This method is provided
     *  so that subclasses are always able to create a default figure even if
     *  an error occurs or the subclass has not been properly initialized.
     *  @return A figure representing a rectangular white box.
     */
    protected Figure _createDefaultBackgroundFigure() {
        // NOTE: It is tempting to create a RectangleAttribute for
        // the rectangle, but that won't work...  It has to be created
        // as a change request, and we can't get the figure until the
        // change request is processed.  This is what the code would
        // look like:

        /*
         StringBuffer moml = new StringBuffer();
         moml.append("<group name=\"auto\">" +
         "<property name=\"defaultFigure\" " +
         "class=\"ptolemy.vergil.kernel.attributes.RectangleAttribute\">\n" +
         "<property name=\"width\" value=\"60\"/>\n" +
         "<property name=\"height\" value=\"40\"/>\n" +
         "<property name=\"centered\" value=\"true\"/>\n" +
         "<property name=\"fillColor\" value=\"{1.0, 1.0, 1.0, 1.0}\"/>\n" +
         "</property></group>" );
         MoMLChangeRequest request = new MoMLChangeRequest(
         this, this, moml.toString());
         requestChange(request);
         */
        return new BasicRectangle(-30, -20, 60, 40, Color.white, 1);
    }

    /** Return true if the property of the specified name is set for
     *  the specified object. A property is specified if the specified
     *  object contains an attribute with the specified name and that
     *  attribute is either not a boolean-valued parameter, or it is
     *  a boolean-valued parameter with value true.
     *  @param object The object.
     *  @param name The property name.
     *  @return True if the property is set.
     */
    protected boolean _isPropertySet(NamedObj object, String name) {
        Attribute attribute = object.getAttribute(name);

        if (attribute == null) {
            return false;
        }

        if (attribute instanceof Parameter) {
            try {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof BooleanToken) {
                    if (!((BooleanToken) token).booleanValue()) {
                        return false;
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore, using default of true.
            }
        }

        return true;
    }

    /** Recreate the figure.  Call this to cause createIcon() to call
     *  createBackgroundFigure() to obtain a new figure.
     */
    protected void _recreateFigure() {
        _iconCache = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The container to be eventually the container for this icon. */
    protected NamedObj _containerToBe;

    /** The cached Swing icon. */
    protected javax.swing.Icon _iconCache = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Font for name labels. */
    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);

    /** Font for parameter values. */
    private static Font _parameterFont = new Font("SansSerif", Font.PLAIN, 9);
}
