/* A simple graph view for Ptolemy models

 Copyright (c) 1998-2008 The Regents of the University of California.
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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import ptolemy.actor.DesignPatternGetMoMLAction;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.modal.ModalPort;
import ptolemy.domains.fsm.modal.RefinementPort;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.LibraryAttribute;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.icon.DesignPatternIcon;
import diva.canvas.DamageRegion;
import diva.graph.GraphPane;
import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// FSMGraphFrame

/**
 This is a graph editor frame for ptolemy FSM models.  Given a composite
 entity and a tableau, it creates an editor and populates the menus
 and toolbar.  This overrides the base class to associate with the
 editor an instance of FSMGraphController.

 @author  Steve Neuendorffer, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (johnr)
 */
public class FSMGraphFrame extends ExtendedGraphFrame
        implements ActionListener {

    /** Construct a frame associated with the specified FSM model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public FSMGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified FSM model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public FSMGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        // Override the default help file.
        helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
        _layoutAction = new LayoutAction();
    }

    /** React to the actions specific to this FSM graph frame.
     *
     *  @param e The action event.
     */
    public void actionPerformed(ActionEvent e) {
        JMenuItem target = (JMenuItem) e.getSource();
        String actionCommand = target.getActionCommand();
        if (actionCommand.equals("Save As Design Pattern")) {
            saveAsDesignPattern();
        }
    }

    /** Save the current submodel as a design pattern using a method similar to
     *  Save As.
     */
    public synchronized void saveAsDesignPattern() {
        Tableau tableau = getTableau();
        PtolemyEffigy effigy = (PtolemyEffigy) tableau.getContainer();
        NamedObj model = effigy.getModel();
        _initialSaveAsFileName = model.getName() + ".xml";
        if (_initialSaveAsFileName.length() == 4) {
            _initialSaveAsFileName = "model.xml";
        }

        FSMActor actor = (FSMActor) getModel();
        StringAttribute alternateGetMoml = null;
        DesignPatternIcon icon = null;
        List<IOPort> modifiedPorts = null;
        try {
            alternateGetMoml = new StringAttribute(actor,
                    "_alternateGetMomlAction");
            alternateGetMoml.setExpression(
                    DesignPatternGetMoMLAction.class.getName());

            icon = new DesignPatternIcon(actor, "_designPatternIcon");

            List<IOPort> ports = actor.portList();
            modifiedPorts = new LinkedList<IOPort>();
            for (IOPort port : ports) {
                if (port instanceof RefinementPort && port.isInput()
                        && port.isOutput()) {
                    List<IOPort> connectedPorts = port.connectedPortList();
                    for (IOPort connectedPort : connectedPorts) {
                        if (connectedPort instanceof ModalPort
                                && !connectedPort.isInput()) {
                            modifiedPorts.add(port);
                            port.setInput(false);
                            break;
                        }
                    }
                }
            }

            super._saveAs(".xml", true, false);
        } catch (KernelException e) {
            throw new InternalErrorException(actor, e,
                    "Unable to export model.");
        } finally {
            if (alternateGetMoml != null) {
                try {
                    alternateGetMoml.setContainer(null);
                } catch (KernelException e) {
                    // Ignore.
                }
            }
            if (icon != null) {
                try {
                    icon.setContainer(null);
                } catch (KernelException e) {
                    // Ignore.
                }
            }
            if (modifiedPorts != null) {
                for (IOPort port : modifiedPorts) {
                    try {
                        port.setInput(true);
                    } catch (IllegalActionException e) {
                        // Ignore.
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    ///////////////////////////////////////////////////////////////////
    //// DebugMenuListener

    /** Listener for debug menu commands. */
    public class DebugMenuListener implements ActionListener {
        /** React to a menu command. */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();

            try {
                if (actionCommand.equals("Listen to Director")) {
                    Effigy effigy = (Effigy) getTableau().getContainer();

                    // Create a new text effigy inside this one.
                    Effigy textEffigy = new TextEffigy(effigy, effigy
                            .uniqueName("debug listener"));
                    DebugListenerTableau tableau = new DebugListenerTableau(
                            textEffigy, textEffigy.uniqueName("debugListener"));
                    tableau.setDebuggable(((FSMActor) getModel())
                            .getDirector());
                } else if (actionCommand.equals("Listen to State Machine")) {
                    Effigy effigy = (Effigy) getTableau().getContainer();

                    // Create a new text effigy inside this one.
                    Effigy textEffigy = new TextEffigy(effigy, effigy
                            .uniqueName("debug listener"));
                    DebugListenerTableau tableau = new DebugListenerTableau(
                            textEffigy, textEffigy.uniqueName("debugListener"));
                    tableau.setDebuggable(getModel());
                } else if (actionCommand.equals("Animate States")) {
                    // Dialog to ask for a delay time.
                    Query query = new Query();
                    query.addLine("delay", "Time (in ms) to hold highlight",
                            Long.toString(_lastDelayTime));

                    ComponentDialog dialog = new ComponentDialog(
                            FSMGraphFrame.this, "Delay for Animation", query);

                    if (dialog.buttonPressed().equals("OK")) {
                        try {
                            _lastDelayTime = Long.parseLong(query
                                    .getStringValue("delay"));
                            _controller.setAnimationDelay(_lastDelayTime);

                            NamedObj model = getModel();

                            if ((model != null) && (_listeningTo != model)) {
                                if (_listeningTo != null) {
                                    _listeningTo
                                            .removeDebugListener(_controller);
                                }

                                _listeningTo = model;
                                _listeningTo.addDebugListener(_controller);
                            }
                        } catch (NumberFormatException ex) {
                            MessageHandler.error(
                                    "Invalid time, which is required "
                                            + "to be an integer: ", ex);
                        }
                    }
                } else if (actionCommand.equals("Stop Animating")
                        && (_listeningTo != null)) {
                    _listeningTo.removeDebugListener(_controller);
                    _controller.clearAnimation();
                    _listeningTo = null;
                }
            } catch (KernelException ex) {
                try {
                    MessageHandler.warning("Failed to create debug listener: "
                            + ex);
                } catch (CancelException exception) {
                }
            }
        }

        private NamedObj _listeningTo;
    }

    ///////////////////////////////////////////////////////////////////
    //// FSMGraphPane

    /** Subclass that updates the background color on each repaint if
     *  there is a preferences attribute.
     */
    public static class FSMGraphPane extends GraphPane {

        /** Construct a pane that updates the background color on each
         *  repait if there is a preference attribute.
         * @param controller The graph controller
         * @param model The model
         * @param entity The entity
         */
        public FSMGraphPane(FSMGraphController controller, FSMGraphModel model,
                NamedObj entity) {
            super(controller, model);
            _entity = entity;
        }

        /** If the entity has a PtolemyPreference, set the background color.
         */
        public void repaint() {
            _setBackground();
            super.repaint();
        }

        /** If the entity has a PtolemyPreference, set the background color.
         */
        public void repaint(DamageRegion damage) {
            _setBackground();
            super.repaint(damage);
        }

        private void _setBackground() {
            if (_entity != null) {
                List list = _entity.attributeList(PtolemyPreferences.class);
                if (list.size() > 0) {
                    // Use the last preferences.
                    PtolemyPreferences preferences = (PtolemyPreferences) list
                            .get(list.size() - 1);
                    getCanvas().setBackground(
                            preferences.backgroundColor.asColor());
                }
            }
        }

        private NamedObj _entity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        _graphMenu = new JMenu("Graph");
        _graphMenu.setMnemonic(KeyEvent.VK_G);
        _menubar.add(_graphMenu);
        GUIUtilities.addHotKey(_getRightComponent(), _layoutAction);
        GUIUtilities.addMenuItem(_graphMenu, _layoutAction);

        // Add any commands to graph menu and toolbar that the controller
        // wants in the graph menu and toolbar.
        _graphMenu.addSeparator();
        _controller.addToMenuAndToolbar(_graphMenu, _toolbar);

        // Add debug menu.
        JMenuItem[] debugMenuItems = {
                new JMenuItem("Listen to Director", KeyEvent.VK_D),
                new JMenuItem("Listen to State Machine", KeyEvent.VK_L),
                new JMenuItem("Animate States", KeyEvent.VK_A),
                new JMenuItem("Stop Animating", KeyEvent.VK_S), };

        // NOTE: This has to be initialized here rather than
        // statically because this method is called by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _debugMenu = new JMenu("Debug");
        _debugMenu.setMnemonic(KeyEvent.VK_D);

        DebugMenuListener debugMenuListener = new DebugMenuListener();

        // Set the action command and listener for each menu item.
        for (int i = 0; i < debugMenuItems.length; i++) {
            debugMenuItems[i].setActionCommand(debugMenuItems[i].getText());
            debugMenuItems[i].addActionListener(debugMenuListener);
            _debugMenu.add(debugMenuItems[i]);
        }

        _menubar.add(_debugMenu);
    }

    /** Close the window.  Override the base class to remove the debug
     *  listener, if there is one.
     *  @return False if the user cancels on a save query.
     */
    protected boolean _close() {
        getModel().removeDebugListener(_controller);
        return super._close();
    }

    /** Create the items in the File menu.
     *
     *  @return The items in the File menu.
     */
    protected JMenuItem[] _createFileMenuItems() {
        JMenuItem[] fileMenuItems = super._createFileMenuItems();
        int i = 0;
        for (JMenuItem item : fileMenuItems) {
            i++;
            if (item.getActionCommand().equals("Save As")) {
                // Add a SaveAsDesignPattern here.
                JMenuItem newItem = new JMenuItem(
                        "Save As Design Pattern", KeyEvent.VK_D);
                JMenuItem[] newItems = new JMenuItem[fileMenuItems.length + 1];
                System.arraycopy(fileMenuItems, 0, newItems, 0, i);
                newItems[i] = newItem;
                newItem.addActionListener(this);
                System.arraycopy(fileMenuItems, i, newItems, i + 1,
                        fileMenuItems.length - i);
                return newItems;
            }
        }
        return fileMenuItems;
    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be careful to not reference
     *  local variables that may not have yet been created.
     *  @param entity The object to be displayed in the pane (which must be
     *   an instance of CompositeEntity).
     *  @return The pane that is created.
     */
    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new FSMGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // NOTE: The cast is safe because the constructor accepts
        // only CompositeEntity.
        final FSMGraphModel graphModel = new FSMGraphModel(
                (CompositeEntity) entity);
        return new FSMGraphPane(_controller, graphModel, entity);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The controller.
     *  The controller is protected so that the subclass
     * (InterfaceAutomatonGraphFrame) can set it to a more specific
     * controller.
     */
    protected FSMGraphController _controller;

    /** Debug menu for this frame. */
    protected JMenu _debugMenu;

    /** The graph menu. */
    protected JMenu _graphMenu;

    /** The action for automatically laying out the graph. */
    protected Action _layoutAction;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The delay time specified that last time animation was set.
    private long _lastDelayTime = 0;

    ///////////////////////////////////////////////////////////////////
    ////                       private inner classes               ////

    ///////////////////////////////////////////////////////////////////
    //// LayoutAction

    /** Action to automatically lay out the graph. */
    private class LayoutAction extends AbstractAction {

        // FIXME: consider refactoring this code, see also
        // vergil/actor/ActorGraphFrame.java

        /** Create a new action to automatically lay out the graph. */
        public LayoutAction() {
            super("Automatic Layout");
            putValue("tooltip", "Layout the Graph (Ctrl+T)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_T, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        }

        /** Lay out the graph. */
        public void actionPerformed(ActionEvent e) {
            try {
                layoutGraph();
            } catch (Exception ex) {
                MessageHandler.error("Layout failed", ex);
            }
        }
    }
}
