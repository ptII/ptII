/* A frame for presenting a dialog

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
package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ptolemy.kernel.Entity;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// PtolemyDialog

/**
 Ptolemy specific dialog.

 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
 */
public abstract class PtolemyDialog extends JFrame implements ActionListener {
    /**
     * Construct a PtolemyDialog.
     *
     * @param title The title of the PtolemyDialog.
     * @param dialogTableau  The dialogTableau, used to set the title.
     * @param owner  The frame.
     * @param target The model
     * @param configuration  a Configuration object
     */
    public PtolemyDialog(String title, DialogTableau dialogTableau,
            Frame owner, Entity target, Configuration configuration) {
        super(title);
        _owner = owner;
        _dialogTableau = dialogTableau;
        _target = target;
        _configuration = configuration;
        _dialogTableau.setTitle(title);

        // This JDialog consists of two components. On top is a JComponent
        // inside of a JScrolPane that displays the ports and their attributes.
        // On bottom is a JPanel that contains buttons that cause various
        // actions.
        JPanel _buttons = _createButtonsPanel();
        getContentPane().add(_buttons, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                _cancel();
            }
        });

        _owner.addWindowListener(new WindowAdapter() {
            public void windowIconified(WindowEvent e) {
                _iconify();
            }

            public void windowDeiconified(WindowEvent e) {
                _deiconify();
            }
        });

        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice[] SCREENS = ge.getScreenDevices();
        Point ownerLoc = owner.getLocation();
        Rectangle screenBounds = null;

        for (int screen = 0; screen < SCREENS.length; screen++) {
            GraphicsConfiguration gc = SCREENS[screen]
                    .getDefaultConfiguration();
            Rectangle bounds = gc.getBounds();

            if (bounds.contains(ownerLoc)) {
                screenBounds = bounds;
                break;
            }
        }

        if (screenBounds != null) {
            Dimension size = getPreferredSize();
            int x = (screenBounds.x + (screenBounds.width / 2))
                    - (size.width / 2);
            int y = (screenBounds.y + (screenBounds.height / 2))
                    - (size.height / 2);
            setLocation(x, y);
        } else {
            setLocationRelativeTo(_owner);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the action event is a JButton, process the button press.
     *  @param aEvent The event.
     */
    public void actionPerformed(ActionEvent aEvent) {
        String command = aEvent.getActionCommand();

        if (aEvent.getSource() instanceof JButton) {
            _processButtonPress(command);
        }
    }

    /** Return the target.
     *  @return The target.
     *  @see #setTarget(Entity)
     */
    public Entity getTarget() {
        return _target;
    }

    /** If necessary save any state.
     *  In this base class, do nothing.  Derived classes should extend
     *  this method so that the {@link #_cancel()} method save state
     *  if necessary.
     */
    public void saveIfRequired() {
    }

    /** Set the contents of this dialog.
     *  @param contents The contents.
     */
    public void setContents(JComponent contents) {
        _contents = contents;
        getContentPane().add(_contents, BorderLayout.CENTER);
    }

    /** Set the contents of this dialog.
     *  @param contents The contents.
     */
    public void setScrollableContents(JComponent contents) {
        _contents = contents;

        JScrollPane scrollPane = new JScrollPane(_contents);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    /** Set the target of this dialog.
     * @param entity Target of this dialog.
     *  @see #getTarget()
     */
    public void setTarget(Entity entity) {
        _target = entity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Cancel this dialog, saving if necessary.
     */
    protected void _cancel() {
        saveIfRequired();
        dispose();
    }

    /** Created extended buttons.
     *  @param _buttons The buttons to be created.
     */
    protected abstract void _createExtendedButtons(JPanel _buttons);

    /** Get the URL that is the help for this dialog.
     * @return URL that is the help for this dialog.
     */
    protected abstract URL _getHelpURL();

    /** Return true if any of the values have been changed, but the state
     *  has not yet been saved.
     *  @return True if values have been changed but not saved.
     */
    protected boolean _isDirty() {
        return _dirty;
    }

    /** Process button presses.
     *  The button semantics are
     *  <br>Commit - Apply and then cancel the dialog.
     *  <br>Apply  - make the changes that have been expressed thus far.
     *  <br>Help   - Show the associated help.
     *  <br>Cancel - Remove the dialog without making any pending changes.
     * @param button The name of the button to process.
     */
    protected void _processButtonPress(String button) {
        if (button.equals("Cancel")) {
            _cancel();
        } else if (button.equals("Help")) {
            _showHelp();
        } else {
            //TODO throw an exception here
        }
    }

    /** Set the boolean that determines if the GUI has a change that has not
     *  applied to the system.
     * @param dirty True if the GUI has a change that has not been applied.
     */
    protected void _setDirty(boolean dirty) {
        _dirty = dirty;
    }

    /** Display the help URL.
     *  @see #_getHelpURL()
     */
    protected void _showHelp() {
        URL toRead = _getHelpURL();

        if ((toRead != null) && (_configuration != null)) {
            try {
                _configuration.openModel(null, toRead, toRead.toExternalForm());
            } catch (Exception ex) {
                MessageHandler.error("Help screen failure", ex);
            }
        } else {
            MessageHandler.error("No help available.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                   ////

    /** The configuration that corresponds with this dialog.
     *  The configuration is used to properly display the help text.
     */
    protected Configuration _configuration;

    /** The help button. */
    protected JButton _helpButton;

    /** The cancel button. */
    protected JButton _cancelButton;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the buttons panel. */
    private JPanel _createButtonsPanel() {
        JPanel _buttons = new JPanel();

        _createExtendedButtons(_buttons);
        _helpButton = new JButton("Help");
        _buttons.add(_helpButton);
        _cancelButton = new JButton("Cancel");
        _buttons.add(_cancelButton);

        for (int i = 0; i < _buttons.getComponentCount(); i++) {
            ((JButton) (_buttons.getComponent(i))).addActionListener(this);
        }

        return _buttons;
    }

    private void _deiconify() {
        setExtendedState(Frame.NORMAL);
    }

    private void _iconify() {
        setExtendedState(Frame.ICONIFIED);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private JComponent _contents;

    /** The following is true if any of the values have been changed but not
     * applied.
     */
    private boolean _dirty = false;

    private DialogTableau _dialogTableau;

    private Frame _owner;

    private Entity _target;
}
