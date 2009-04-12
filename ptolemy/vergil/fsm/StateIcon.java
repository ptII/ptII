/* An icon specialized for states of a state machine.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.vergil.fsm;

import java.awt.Color;
import java.awt.Paint;

import javax.swing.Icon;

import ptolemy.actor.TypedActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.icon.NameIcon;
import diva.canvas.toolbox.RoundedRectangle;
import diva.gui.toolbox.FigureIcon;

//////////////////////////////////////////////////////////////////////////
//// StateIcon

/**
 An icon that displays the name of the container in an appropriately
 sized rounded box. This is designed to be contained by an instance
 of State, and if it is, and if the state is the initial state, then
 the rounded box will be bold. If it is a final state, then it will
 be double.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class StateIcon extends NameIcon {

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
    public StateIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Change the default rounding to 20.
        rounding.setExpression("20");
    }

    /** Create an icon.
     *
     *  @return The icon.
     */
    public Icon createIcon() {
        if (_iconCache != null) {
            return _iconCache;
        }

        RoundedRectangle figure = new RoundedRectangle(0, 0, 20, 10, _getFill(),
                        1.0f, 5.0, 5.0);
        _iconCache = new FigureIcon(figure, 20, 15);
        return _iconCache;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the paint to use to fill the icon.
     *  This class returns Color.white, unless the refinement name
     *  is not empty, in which case it returns a light green.
     *  @return The paint to use to fill the icon.
     */
    protected Paint _getFill() {
        Parameter colorParameter;
        try {
            colorParameter = (Parameter) (getAttribute("fill",
                    Parameter.class));
            if (colorParameter != null) {
                ArrayToken array = (ArrayToken) colorParameter.getToken();
                if (array.length() == 4) {
                    Color color = new Color(
                            (float) ((ScalarToken) array.getElement(0))
                                    .doubleValue(),
                            (float) ((ScalarToken) array.getElement(1))
                                    .doubleValue(),
                            (float) ((ScalarToken) array.getElement(2))
                                    .doubleValue(),
                            (float) ((ScalarToken) array.getElement(3))
                                    .doubleValue());
                    return color;
                }
            }
        } catch (Throwable t) {
            // Ignore and return the default.
        }

        NamedObj container = getContainer();
        if (container instanceof State) {
            try {
                TypedActor[] refinement = ((State) container).getRefinement();
                if (refinement != null && refinement.length > 0) {
                    return _REFINEMENT_COLOR;
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }

        return Color.white;
    }

    /** Return the line width to use in rendering the box.
     *  This returns 1.0f, unless the container is an instance of State
     *  and its <i>isInitialState</i> parameter is set to true.
     *  @return The line width to use in rendering the box.
     */
    protected float _getLineWidth() {
        NamedObj container = getContainer();
        if (container instanceof State) {
            try {
                if (((BooleanToken) (((State) container).isInitialState
                        .getToken())).booleanValue()) {
                    return 2.0f;
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }
        return 1.0f;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The fill color for states with refinements. */
    private static Color _REFINEMENT_COLOR = new Color(0.8f, 1.0f, 0.8f, 1.0f);
}
