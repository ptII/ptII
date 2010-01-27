/* An icon that renders the value of all attributes of the container.

 Copyright (c) 2003-2010 The Regents of the University of California.
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

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;

///////////////////////////////////////////////////////////////////
//// BoxedValuesIcon

/**
 This icon displays the value of all visible attributes of class Settable
 contained by the container of this icon. Visible attributes are those
 whose visibility is Settable.FULL. The names and values of the attributes
 are displayed in a box that resizes as necessary. If any line is longer
 than <i>displayWidth</i> (in characters), then it is truncated.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class BoxedValuesIcon extends XMLIcon {
    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public BoxedValuesIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        displayWidth = new Parameter(this, "displayWidth");
        displayWidth.setExpression("80");
        displayWidth.setTypeEquals(BaseType.INT);

        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of characters to display. This is an integer, with
     *  default value 80.
     */
    public Parameter displayWidth;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new background figure.  This overrides the base class
     *  to draw a box around the value display, where the width of the
     *  box depends on the value.
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        String displayString = _displayString();
        double width = 60;
        double heigth = 30;

        if (displayString != null) {
            // Measure width of the text.  Unfortunately, this
            // requires generating a label figure that we will not use.
            LabelFigure label = new LabelFigure(displayString, _labelFont, 1.0,
                    SwingConstants.CENTER);
            Rectangle2D stringBounds = label.getBounds();

            // NOTE: Padding of 20.
            width = stringBounds.getWidth() + 20;
            heigth = stringBounds.getHeight() + 10;
        }

        BasicRectangle result = new BasicRectangle(0, 0, width, heigth,
                Color.white, 1);

        // FIXME: Doesn't do the right thing.
        // result.setCentered(false);
        return result;
    }

    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of LabelFigure that renders the
     *  values of the attributes of the container.
     *  @return A new CompositeFigure consisting of the label.
     */
    public Figure createFigure() {
        CompositeFigure result = (CompositeFigure) super.createFigure();
        String truncated = _displayString();

        // If there is no string to display now, then create a string
        // with a single blank.
        if (truncated == null) {
            truncated = " ";
        }

        // NOTE: This violates the Diva MVC architecture!
        // This attribute is part of the model, and should not have
        // a reference to this figure.  By doing so, it precludes the
        // possibility of having multiple views on this model.
        LabelFigure label = new LabelFigure(truncated, _labelFont, 1.0,
                SwingConstants.CENTER);
        Rectangle2D backBounds = result.getBackgroundFigure().getBounds();
        label.translateTo(backBounds.getCenterX(), backBounds.getCenterY());
        result.add(label);

        _addLiveFigure(label);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the string to render in the icon.  This string is the
     *  expression giving the value of the attribute of the container
     *  having the name <i>attributeName</i>, truncated so that it is
     *  no longer than <i>displayWidth</i> characters.  If it is truncated,
     *  then the string has a trailing "...".  If the string is empty,
     *  then return a string with one space (diva fails on empty strings).
     *  @return The string to display, or null if none is found.
     */
    protected String _displayString() {
        NamedObj container = getContainer();

        if (container != null) {
            StringBuffer buffer = new StringBuffer();
            Iterator settables = container.attributeList(Settable.class)
                    .iterator();

            while (settables.hasNext()) {
                Settable settable = (Settable) settables.next();

                if ((settable.getVisibility() != Settable.FULL)
                        && (settable.getVisibility() != Settable.NOT_EDITABLE)) {
                    continue;
                }

                String name = settable.getDisplayName();
                String value = settable.getExpression();
                String line = name + ": " + value;
                String truncated = line;

                try {
                    int width = ((IntToken) displayWidth.getToken()).intValue();

                    if (line.length() > width) {
                        truncated = line.substring(0, width) + "...";
                    }
                } catch (IllegalActionException ex) {
                    // Ignore... use whole string.
                }

                buffer.append(truncated);

                if (settables.hasNext()) {
                    buffer.append("\n");
                }
            }

            return buffer.toString();
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The font used. */
    protected static final Font _labelFont = new Font("Dialog", Font.PLAIN, 12);
}
