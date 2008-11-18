/* An actor whose output is controlled by a slider in the run window.

 @Copyright (c) 2001-2007 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.gui;

import java.awt.Color;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.Source;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// SliderSource

/**
 The output of this actor is controlled by a slider in the run window.
 The range of the output is specified by two parameters, <i>minimum</i> and
 <i>maximum</i>. The type of these parameters and the output is integer.

 @author Xiaojun Liu, Gang Zhou
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 */
public class SliderSource extends Source implements ChangeListener, Placeable {
    /** Construct an actor with an input multiport of type GENERAL.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   entity with this name.
     */
    public SliderSource(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the output port.
        output.setTypeEquals(BaseType.INT);

        minimum = new Parameter(this, "minimum", new IntToken(-10));
        minimum.setTypeEquals(BaseType.INT);
        maximum = new Parameter(this, "maximum", new IntToken(10));
        maximum.setTypeEquals(BaseType.INT);
        majorTickSpacing = new Parameter(this, "majorTickSpacing",
                new IntToken(10));
        majorTickSpacing.setTypeEquals(BaseType.INT);
        minorTickSpacing = new Parameter(this, "minorTickSpacing",
                new IntToken(1));
        minorTickSpacing.setTypeEquals(BaseType.INT);

        title = new StringAttribute(this, "title");
        title.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The slider that controls the output of this actor. */
    public JSlider slider;

    /** The minimum value of the slider. The value must be an integer.
     *  The default value is -10.
     */
    public Parameter minimum;

    /** The maximum value of the slider. The value must be an integer.
     *  The default value is 10.
     */
    public Parameter maximum;

    /** The major tick spacing of the slider. The value must be an integer.
     *  The default value is 10.
     */
    public Parameter majorTickSpacing;

    /** The minor tick spacing of the slider. The value must be an integer.
     *  The default value is 1.
     */
    public Parameter minorTickSpacing;

    /** The title to put on top. */
    public StringAttribute title;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>minimum</i> or <i>maximum</i>,
     *  then set the range of the slider.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified range for the
     *   slider is invalid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == minimum || attribute == maximum
                || attribute == majorTickSpacing
                || attribute == minorTickSpacing) {
            int min = ((IntToken) minimum.getToken()).intValue();
            int max = ((IntToken) maximum.getToken()).intValue();
            int major = ((IntToken) majorTickSpacing.getToken()).intValue();
            int minor = ((IntToken) minorTickSpacing.getToken()).intValue();

            if ((min > max)) {
                throw new IllegalActionException(this, "The minimum value "
                        + "of the slider cannot be larger than the maximum "
                        + "value.");
            }

            if (slider != null) {
                slider.setMaximum(max);
                slider.setMinimum(min);
                slider.setMajorTickSpacing(major);
                slider.setMinorTickSpacing(minor);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the slider public variable to null.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SliderSource newObject = (SliderSource) super.clone(workspace);
        newObject.slider = null;
        newObject._frame = null;
        return newObject;
    }

    /** Output the value of the slider recorded when prefire() is last called.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, _outputVal);
    }

    /** Return the background.
     *  @return The background color.
     *  @see #setBackground(Color)
     */
    public Color getBackground() {
        return _panel.getBackground();
    }

    /** Create a slider on the screen, if necessary. If a graphical container
     *  has not been specified, place the slider into its own frame.
     *  Otherwise, place it in the specified container.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        if (slider == null) {
            int min = ((IntToken) minimum.getToken()).intValue();
            int max = ((IntToken) maximum.getToken()).intValue();
            int major = ((IntToken) majorTickSpacing.getToken()).intValue();
            int minor = ((IntToken) minorTickSpacing.getToken()).intValue();
            String titleSpec = title.getExpression();

            // place the slider in its own frame.
            // FIXME: This probably needs to be a PtolemyFrame, when one
            // exists, so that the close button is dealt with, etc.
            _frame = new SliderFrame(min, max, major, minor, titleSpec);
            _panel = (JPanel) _frame.getContentPane().getComponent(0);
            slider = (JSlider) _panel.getComponent(0);
            slider.addChangeListener(this);
        }

        if (_frame != null) {
            // Do not use show() as it overrides manual placement.
            // FIXME: So does setVisible()... But with neither one used,
            // then if the user dismisses the window, it does not reappear
            // on re-running!
            _frame.pack();
            _frame.setVisible(true);
            // _frame.toFront();
        }
    }

    /** Specify the container in which the slider should be displayed.
     *  An instance of JSlider will be added to that container.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, an instance of JSlider will be placed in its own frame.
     *  The slider is also placed in its own frame if this method
     *  is called with a null argument.
     *  The background of the slider is set equal to that of the container
     *  (unless it is null).
     *  @param container The container into which to place the slider.
     */
    public void place(Container container) {

        _container = container;

        if (_container == null) {
            if (_frame != null) {
                _frame.dispose();
            }

            _frame = null;
            _panel = null;
            slider = null;
            return;
        }

        int min = -10;
        int max = 10;
        int major = 10;
        int minor = 1;
        String titleSpec = title.getExpression();

        try {
            min = ((IntToken) minimum.getToken()).intValue();
            max = ((IntToken) maximum.getToken()).intValue();
            major = ((IntToken) majorTickSpacing.getToken()).intValue();
            minor = ((IntToken) minorTickSpacing.getToken()).intValue();

        } catch (IllegalActionException ex) {
            // ignore
        }

        _panel = SliderFrame.createSliderPanel(min, max, major, minor,
                titleSpec);
        _container.add(_panel);

        // java.awt.Component.setBackground(color) says that
        // if the color "parameter is null then this component
        // will inherit the  background color of its parent."
        // plot.setBackground(_container.getBackground());
        // _scrollPane.setBackground(_container.getBackground());
        _panel.setBackground(null);
        _panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        _panel.setBorder(new LineBorder(Color.black));
        slider = (JSlider) _panel.getComponent(0);
        slider.addChangeListener(this);
    }

    /** Record the current value of the slider. This value is output in the
     *  subsequent firings of this actor.
     */
    public boolean prefire() throws IllegalActionException {
        _outputVal = new IntToken(slider.getValue());
        return super.prefire();
    }

    /** Set the background color of the panel that contains the slider.
     *  @param background The background color.
     *  @see #getBackground()
     */
    public void setBackground(Color background) {
        _panel.setBackground(background);
    }

    /** Override the base class to remove the display from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        if (container == null) {
            _remove();
        }
    }

    /** The value of the slider changed, record the new value. */
    public void stateChanged(ChangeEvent e) {
        slider.getValue();
    }

    /** The frame for the slider. */
    public static class SliderFrame extends JFrame {

        /**  Create a frame for the slider.
         * @param minimum the minimum value.
         * @param maximum the maximum value.
         * @param majorTickSpacing the space between major ticks.
         * @param minorTickSpacing the space between minor ticks.
         * @param title the title.
         */
        public SliderFrame(int minimum, int maximum, int majorTickSpacing,
                int minorTickSpacing, String title) {

            JPanel panel = createSliderPanel(minimum, maximum,
                    majorTickSpacing, minorTickSpacing, title);
            _slider = (JSlider) panel.getComponent(0);
            getContentPane().add(panel);
            pack();
            setVisible(true);
        }

        /** Create a slider panel.
         * @param minimum the minimum value.
         * @param maximum the maximum value.
         * @param majorTickSpacing the space between major ticks.
         * @param minorTickSpacing the space between minor ticks.
         * @param title the title.
         * @return The slider panel.
         */
        public static JPanel createSliderPanel(int minimum, int maximum,
                int majorTickSpacing, int minorTickSpacing, String title) {

            JSlider slider = new JSlider(SwingConstants.HORIZONTAL, minimum,
                    maximum, (maximum + minimum) / 2);
            slider.setBackground(null);
            slider.setMajorTickSpacing(majorTickSpacing);
            slider.setMinorTickSpacing(minorTickSpacing);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);

            //slider.addChangeListener(this);
            JPanel panel = new JPanel();
            panel.add(slider);
            if (!title.trim().equals("")) {
                panel.setBorder(BorderFactory.createTitledBorder(title));
            }

            return panel;
        }

        /** Get the value of the slider.
         *  @return the slider value.
         */
        public int getValue() {
            return _slider.getValue();
        }

        private JSlider _slider;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the display from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (slider != null) {
                    if (_container != null) {
                        _container.remove(_panel);
                        _container.invalidate();
                        _container.repaint();
                    } else if (_frame != null) {
                        _frame.dispose();
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The JPanel that contains the slider. */
    private JPanel _panel;

    private Container _container;

    private IntToken _outputVal;

    // The frame into which to put the text widget, if any.
    private JFrame _frame;
}
