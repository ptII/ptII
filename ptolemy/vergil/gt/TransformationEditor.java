/* A graph editor frame for ptolemy graph transformation models.

@Copyright (c) 2007-2008 The Regents of the University of California.
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

                        PT_COPYRIGHT_VERSION_2
                        COPYRIGHTENDKEY


 */
package ptolemy.vergil.gt;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.CreationAttribute;
import ptolemy.actor.gt.DefaultDirectoryAttribute;
import ptolemy.actor.gt.DefaultModelAttribute;
import ptolemy.actor.gt.GTEntity;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.GraphMatcher;
import ptolemy.actor.gt.IgnoringAttribute;
import ptolemy.actor.gt.MatchingAttribute;
import ptolemy.actor.gt.NegationAttribute;
import ptolemy.actor.gt.OptionAttribute;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.PatternObjectAttribute;
import ptolemy.actor.gt.PortMatcher;
import ptolemy.actor.gt.PreservationAttribute;
import ptolemy.actor.gt.Replacement;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gt.data.CombinedCollection;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.util.RecursiveFileFilter;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorController;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.actor.IOPortController;
import ptolemy.vergil.actor.LinkController;
import ptolemy.vergil.basic.OffsetMoMLChangeRequest;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.fsm.FSMGraphController;
import ptolemy.vergil.fsm.StateController;
import ptolemy.vergil.fsm.TransitionController;
import ptolemy.vergil.kernel.PortDialogAction;
import ptolemy.vergil.kernel.RelationController;
import ptolemy.vergil.toolbox.ConfigureAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.MenuItemListener;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.Site;
import diva.canvas.connector.AbstractConnector;
import diva.canvas.connector.Connector;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.RoundedRectangle;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.JContextMenu;

//////////////////////////////////////////////////////////////////////////
//// TransformationEditor

/**
 A graph editor frame for ptolemy graph transformation models.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see ptolemy.vergil.actor.ActorGraphFrame
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationEditor extends GTFrame implements ActionListener,
        MenuItemListener, TableModelListener, ValueListener {

    ///////////////////////////////////////////////////////////////////
    ////                          constructors                     ////

    /** Construct a frame associated with the specified case actor.
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
    public TransformationEditor(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified case actor.
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
    public TransformationEditor(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, entity instanceof FSMActor ? defaultLibrary
                : _importActorLibrary(tableau, defaultLibrary));

        // Override the default help file.
        // FIXME
        // helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("add")) {
            addRow();
        } else if (command.equals("remove")) {
            int[] rows = _table.getSelectedRows();
            if (rows.length > 0) {
                removeRows(rows);
            }
        }
    }

    public void addRow() {
        int index = _tableModel.getRowCount() + 1;
        _tableModel.addRow(new Object[] {
                _createCellPanel(Integer.toString(index)),
                _createCellPanel(""), _createCellPanel("") });
    }

    public void cancelFullScreen() {
        if (!getFrameController().hasTabs()) {
            super.cancelFullScreen();
            return;
        }

        _screen.dispose();

        // Put the component back into the original window.
        _splitPane.setRightComponent(_getRightComponent());
        JTabbedPane tabbedPane = getFrameController().getTabbedPane();
        tabbedPane.add(_fullScreenComponent, _selectedIndexBeforeFullScreen);
        tabbedPane.setSelectedIndex(_selectedIndexBeforeFullScreen);

        // Restore association with the graph panner.
        if (_fullScreenComponent instanceof JGraph) {
            _graphPanner.setCanvas((JGraph) _fullScreenComponent);
        } else {
            _graphPanner.setCanvas(null);
        }

        _fullScreenComponent.removeKeyListener(getFrameController());
        if (_selectedIndexBeforeFullScreen == 2) {
            _setOrUnsetKeyListenersForAllComponents(
                    (JPanel) _fullScreenComponent, false);
        }
        pack();
        show();
        GraphicalMessageHandler.setContext(_previousDefaultContext);
        toFront();
        _getRightComponent().requestFocus();
    }

    public void changeExecuted(ChangeRequest change) {
        super.changeExecuted(change);
        _refreshTable();
    }

    public void copy() {
        if (!getFrameController().isTableActive()) {
            CompositeEntity model = getFrameController().getActiveModel();
            String header = "";
            if (GTTools.isInPattern(model)) {
                header = _COPY_FROM_PATTERN_HEADER;

                try {
                    model.workspace().getReadAccess();
                    _setOrClearPatternObjectAttributes(model, true,
                            _getSelectionSet());
                } finally {
                    model.workspace().doneReading();
                }

                super.copy();

                try {
                    model.workspace().getReadAccess();
                    _setOrClearPatternObjectAttributes(model, false,
                            _getSelectionSet());
                } finally {
                    model.workspace().doneReading();
                }
            } else if (GTTools.isInReplacement(model)) {
                header = _COPY_FROM_REPLACEMENT_HEADER;
                super.copy();
            }

            Clipboard clipboard = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            Transferable transferable = clipboard.getContents(this);
            if (transferable == null) {
                return;
            }
            try {
                String data = (String) transferable.getTransferData(DataFlavor
                        .stringFlavor);
                clipboard.setContents(new StringSelection(header + data), this);
            } catch (Exception e) {
                // Ignore. Do not add the header.
            }

        }
    }

    public void delete() {
        if (!getFrameController().isTableActive()) {
            super.delete();
        }
    }

    public void fullScreen() {
        if (!getFrameController().hasTabs()) {
            super.fullScreen();
            return;
        }

        _screen = new JDialog();
        _screen.getContentPane().setLayout(new BorderLayout());

        // Set to full-screen size.
        Toolkit toolkit = _screen.getToolkit();
        int width = toolkit.getScreenSize().width;
        int height = toolkit.getScreenSize().height;
        _screen.setSize(width, height);

        JTabbedPane tabbedPane = getFrameController().getTabbedPane();
        _selectedIndexBeforeFullScreen = tabbedPane.getSelectedIndex();
        _fullScreenComponent = tabbedPane.getSelectedComponent();
        _screen.setUndecorated(true);
        _screen.getContentPane().add(_fullScreenComponent, BorderLayout.CENTER);

        // NOTE: Have to avoid the following, which forces the
        // dialog to resize the preferred size of _jgraph, which
        // nullifies the call to setSize() above.
        // _screen.pack();
        _screen.setVisible(true);

        // Make the new screen the default context for modal messages.
        Component _previousDefaultContext = GraphicalMessageHandler
                .getContext();
        GraphicalMessageHandler.setContext(_screen);

        // NOTE: As usual with swing, what the UI does is pretty
        // random, and doesn't correlate much with the documentation.
        // The following two lines do not work if _screen is a
        // JWindow instead of a JDialog.  There is no apparent
        // reason for this, but this is why we use JDialog.
        // Unfortunately, apparently the JDialog does not appear
        // in the Windows task bar.
        _screen.toFront();
        _fullScreenComponent.requestFocus();

        _screen.setResizable(false);

        _fullScreenComponent.addKeyListener(getFrameController());
        if (_selectedIndexBeforeFullScreen == 2) {
            // The correspondence table is selected before full screen.
            // Set the key listener for the table.
            _setOrUnsetKeyListenersForAllComponents(
                    (JPanel) _fullScreenComponent, true);
        }

        // Remove association with the graph panner.
        _graphPanner.setCanvas(null);

        setVisible(false);
        GraphicalMessageHandler.setContext(_previousDefaultContext);
    }

    public void menuItemCreated(JContextMenu menu, NamedObj object,
            JMenuItem menuItem) {
        if (menuItem instanceof JMenu) {
            JMenu submenu = (JMenu) menuItem;
            Component[] menuItems = submenu.getMenuComponents();
            for (Component itemComponent : menuItems) {
                JMenuItem item = (JMenuItem) itemComponent;
                if (object instanceof PortMatcher) {
                    // Disable all the items for a PortMatcher, which should be
                    // configured by double-clicking the containing
                    // CompositeActor.
                    item.setEnabled(false);
                }
                // Allow to configure ports as usual. Extra properties of ports
                // are stored in the moml with
                // GTEntityUtils.ExportExtraProperties().
                /* else if (item.getAction() instanceof PortDialogAction
                        && object instanceof GTEntity) {
                    // Disable the PortDialogAction from the context menu.
                    item.setEnabled(false);
                } */
            }
        } else {
            Action action = menuItem.getAction();
            if (action instanceof PortDialogAction
                    && object instanceof GTEntity) {
                // Disable the PortDialogAction from the context menu.
                menuItem.setEnabled(false);
            }
        }
    }

    public void paste() {
        if (!getFrameController().isTableActive()) {
            Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            Transferable transferable = clipboard.getContents(this);
            GraphModel model = _getGraphModel();

            if (transferable == null) {
                return;
            }

            try {
                NamedObj container = (NamedObj) model.getRoot();
                String moml = (String) transferable.getTransferData(DataFlavor
                        .stringFlavor);
                MoMLChangeRequest change = new PasteMoMLChangeRequest(this,
                        container, moml);
                change.setUndoable(true);
                container.requestChange(change);
            } catch (Exception ex) {
                MessageHandler.error("Paste failed", ex);
            }
        }
    }

    public void redo() {
        if (getFrameController().isTableActive() && _cellEditor != null) {
            _cellEditor.stopCellEditing();
        }
        super.redo();
    }

    public void removeRows(int[] rows) {
        if (_cellEditor != null) {
            // Stop editing so that the current edit value will be recorded in
            // the undo history.
            _cellEditor.stopCellEditing();
        }
        TransformationRule transformer = (TransformationRule) getModel();
        CompositeActorMatcher replacement = transformer.getReplacement();
        List<ComponentEntity> entities = new LinkedList<ComponentEntity>();
        for (int i = 0; i < rows.length; i++) {
            String replacementName = _getCellEditorValue((JPanel) _tableModel
                    .getValueAt(rows[i], 2));
            ComponentEntity entity = replacement.getEntity(replacementName);
            if (entity != null) {
                entities.add(entity);
            }
        }
        int i = 0;
        for (ComponentEntity entity : entities) {
            _setPatternObject(entity, "", i++ > 0);
        }
    }

    public void tableChanged(TableModelEvent event) {
        if (event.getType() != TableModelEvent.UPDATE) {
            return;
        }

        int row = event.getFirstRow();
        int column = event.getColumn();
        if (column != TableModelEvent.ALL_COLUMNS
                && row == event.getLastRow()) {
            // Get the value in the transformer's correspondence attribute.
            TransformationRule transformer = (TransformationRule) getModel();
            Pattern pattern = transformer.getPattern();
            Replacement replacement = transformer.getReplacement();
            String newValue = _getCellEditorValue((JPanel) _tableModel
                    .getValueAt(row, column));
            String previousString = _cellEditor.getPreviousString();
            if (previousString.equals(newValue)) {
                return;
            }

            if (column == 1) {
                String patternObjectName = newValue;
                if (patternObjectName.length() > 0) {
                    NamedObj patternObject = pattern
                            .getEntity(patternObjectName);
                    if (patternObject == null) {
                        patternObject = pattern.getRelation(patternObjectName);
                    }
                    if (patternObject == null) {
                        String message = "Entity or relation with name \""
                                + patternObjectName
                                + "\" cannot be found in the pattern of the "
                                + "transformation rule.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }
                }

                String replacementObjectName = _getCellEditorValue(
                        (JPanel) _tableModel.getValueAt(row, 2));
                if (replacementObjectName.length() > 0) {
                    // Updated the pattern object.
                    NamedObj replacementObject = replacement
                            .getEntity(replacementObjectName);
                    if (replacementObject == null) {
                        replacementObject = replacement
                                .getRelation(replacementObjectName);
                    }

                    if (replacementObject == null) {
                        String message = "Entity or relation with name \""
                                + replacementObjectName
                                + "\" cannot be found in the replacement of "
                                + "the transformation rule.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }

                    PatternObjectAttribute attribute = GTTools
                            .getPatternObjectAttribute(replacementObject);
                    if (attribute == null) {
                        try {
                            attribute = new PatternObjectAttribute(
                                    replacementObject, "patternObject");
                        } catch (KernelException e) {
                            throw new KernelRuntimeException(e, "Unable to "
                                    + "create patternObject attribute.");
                        }
                    }
                    if (!attribute.getExpression().equals(patternObjectName)) {
                        _setPatternObject(replacementObject, patternObjectName,
                                false);
                    }
                }

            } else if (column == 2) {
                String replacementObjectName = newValue;
                if (replacementObjectName.length() > 0) {
                    NamedObj replacementObject = replacement
                            .getEntity(replacementObjectName);
                    if (replacementObject == null) {
                        replacementObject = replacement
                                .getRelation(replacementObjectName);
                    }

                    if (replacementObject == null) {
                        String message = "Entity or relation with name \""
                                + replacementObjectName
                                + "\" cannot be found in the replacement of the "
                                + "transformation rule.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }

                    PatternObjectAttribute attribute = GTTools
                            .getPatternObjectAttribute(replacementObject);
                    if (attribute == null) {
                        String message = "Entity or relation with name \""
                                + replacementObject
                                + "\" in the replacement part of the "
                                + "transformation rule does not have a "
                                + "\"patternObject\" attribute.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }

                    _cellEditor.setPreviousString(replacementObjectName);
                    String patternObjectName = _getCellEditorValue(
                            (JPanel) _tableModel.getValueAt(row, 1));

                    if (previousString.length() > 0) {
                        NamedObj previousObject = replacement
                                .getEntity(previousString);
                        _setPatternObject(previousObject, "", false);
                    }
                    _setPatternObject(replacementObject, patternObjectName,
                            true);
                }
            }
        }
    }

    public void undo() {
        if (getFrameController().isTableActive() && _cellEditor != null) {
            _cellEditor.stopCellEditing();
        }
        super.undo();
    }

    public void valueChanged(Settable settable) {
        if (_cellEditor != null) {
            _cellEditor.stopCellEditing();
        }
    }

    public void zoom(double factor) {
        if (!getFrameController().isTableActive()) {
            super.zoom(factor);
        }
    }

    public void zoomFit() {
        if (!getFrameController().isTableActive()) {
            super.zoomFit();
        }
    }

    public void zoomReset() {
        if (!getFrameController().isTableActive()) {
            super.zoomReset();
        }
    }

    public static final String[] OPTIONAL_ACTORS = {
        "ptolemy.actor.ptalon.gt.PtalonMatcher"
    };

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        _ruleMenu = new JMenu("Rule");
        _ruleMenu.setMnemonic(KeyEvent.VK_R);
        _menubar.add(_ruleMenu);

        SingleMatchAction singleMatchAction = new SingleMatchAction();
        GUIUtilities.addMenuItem(_ruleMenu, singleMatchAction);

        BatchMatchAction batchMatchAction = new BatchMatchAction();
        GUIUtilities.addMenuItem(_ruleMenu, batchMatchAction);

        _ruleMenu.addSeparator();

        LayoutAction layoutAction = new LayoutAction();
        GUIUtilities.addMenuItem(_ruleMenu, layoutAction);

        GraphController controller = _getGraphController();
        if (controller instanceof RunnableGraphController) {
            ((RunnableGraphController) controller).addToMenuAndToolbar(
                    _ruleMenu, _toolbar);
        }
        _removeUnusedToolbarButtons();

        GUIUtilities.addToolBarButton(_toolbar, singleMatchAction);
        GUIUtilities.addToolBarButton(_toolbar, batchMatchAction);
    }

    protected RunnableGraphController _createActorGraphController() {
        return new TransformationActorGraphController();
    }

    protected RunnableGraphController _createFSMGraphController() {
        return new TransformationFSMGraphController();
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    protected JComponent _createRightComponent(NamedObj entity) {
        JComponent component = super._createRightComponent(entity);
        if (component instanceof JTabbedPane) {
            _createTable((TransformationRule) entity);
        }
        return component;
    }

    @SuppressWarnings("unchecked")
    protected HashSet _getSelectionSet(boolean includeLinks) {
        HashSet selectionSet = super._getSelectionSet();

        if (includeLinks) {
            GraphController controller = _getGraphController();
            GraphModel graphModel = controller.getGraphModel();
            SelectionModel model = controller.getSelectionModel();
            Object[] selection = model.getSelectionAsArray();
            for (int i = 0; i < selection.length; i++) {
                if (selection[i] instanceof Figure) {
                    Object userObject = ((Figure) selection[i]).getUserObject();

                    if (graphModel.isEdge(userObject)) {
                        Object semanticObject = graphModel.getSemanticObject(
                                userObject);
                        if (semanticObject instanceof Relation) {
                            selectionSet.add(semanticObject);
                        }
                    }
                }
            }
        }
        return selectionSet;
    }

    /** The case menu. */
    protected JMenu _ruleMenu;

    protected class MatchingAttributeActionsFactory implements MenuItemFactory {

        /** Add an item to the given context menu that will configure the
         *  parameters on the given target.
         *  @param menu The context menu to add to.
         *  @param object The object that the menu item command will operate on.
         *  @return A menu item, or null to decline to provide a menu item.
         */
        public JMenuItem create(JContextMenu menu, NamedObj object) {
            int location = 0;
            for (int i = 0; i < menu.getComponentCount(); i++) {
                if (!(menu.getComponent(i) instanceof JMenu)) {
                    location = i;
                    break;
                }
            }

            JMenu submenu = new JMenu("Transformation");
            menu.add(submenu, location);

            if (GTTools.isInPattern(object)) {
                _add(submenu, new ConfigureCriteriaAction(), false);
                submenu.addSeparator();

                Collection<?> objects = _getSelectionSet(true);
                if (objects.isEmpty()) {
                    HashSet<NamedObj> set = new HashSet<NamedObj>();
                    set.add(object);
                    objects = set;
                }

                if (!(object instanceof Port)) {
                    ButtonGroup group = new ButtonGroup();
                    JMenuItem hiddenItem = _add(submenu, new FigureAction(
                            "hidden"), true);
                    hiddenItem.setVisible(false);
                    group.add(hiddenItem);

                    MatchingAttributeAction[] radioActions =
                        new MatchingAttributeAction[4];
                    radioActions[0] = new CreationAttributeAction("Created",
                            radioActions);
                    radioActions[1] = new IgnoringAttributeAction("Ignored",
                            radioActions);
                    radioActions[2] = new NegationAttributeAction("Negated",
                            radioActions);
                    radioActions[3] = new PreservationAttributeAction(
                            "Preserved", radioActions);
                    JMenuItem[] radioItems = new JMenuItem[radioActions.length];
                    int i = 0;
                    for (Action radioAction : radioActions) {
                        radioItems[i] = _add(submenu, radioAction, true);
                        group.add(radioItems[i]);
                        i++;
                    }

                    JMenuItem noneItem = _add(submenu,
                            new MatchingAttributeAction("None", radioActions),
                            true);
                    group.add(noneItem);

                    boolean setHidden = false;
                    Class<? extends MatchingAttribute> attributeClass = null;
                    i = 0;
                    for (Object childObject : objects) {
                        NamedObj child = (NamedObj) childObject;
                        MatchingAttribute attribute = _getRadioAttribute(child,
                                radioActions);
                        if (attributeClass == null && attribute != null &&
                                i == 0) {
                            attributeClass = attribute.getClass();
                        } else if (attributeClass == null && attribute != null
                                || attributeClass != null &&
                                        !attributeClass.isInstance(attribute)) {
                            hiddenItem.setSelected(true);
                            attributeClass = null;
                            setHidden = true;
                            break;
                        }
                        i++;
                    }
                    if (!setHidden) {
                        if (attributeClass == null) {
                            noneItem.setSelected(true);
                        } else {
                            i = 0;
                            for (MatchingAttributeAction radioAction
                                    : radioActions) {
                                if (radioAction.getAttributeClass().equals(
                                        attributeClass)) {
                                    radioItems[i].setSelected(true);
                                    break;
                                }
                                i++;
                            }
                        }
                    }

                    submenu.addSeparator();
                }

                OptionAttributeAction action = new OptionAttributeAction(
                        "Optional", null);
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
                submenu.add(item);
                boolean selected = true;
                for (Object childObject : objects) {
                    NamedObj child = (NamedObj) childObject;
                    if (GTTools.findMatchingAttribute(child,
                            action.getAttributeClass(), false) == null) {
                        selected = false;
                        break;
                    }
                }
                action._setToggleAction(!selected);
                if (selected) {
                    item.setSelected(true);
                }
            }

            if (GTTools.isInReplacement(object)) {
                _add(submenu, new ConfigureOperationsAction(), false);
            }

            return submenu;
        }

        private JMenuItem _add(JMenu menu, Action action, boolean isRadio) {
            JMenuItem item;
            if (isRadio) {
                item = new JRadioButtonMenuItem(action);
            } else {
                item = new JMenuItem(action);
            }
            menu.add(item);
            action.putValue("", item);
            return item;
        }

        private MatchingAttribute _getRadioAttribute(NamedObj object,
                MatchingAttributeAction[] radioActions) {
            MatchingAttribute attribute = null;
            for (Object attributeObject : object.attributeList(
                    MatchingAttribute.class)) {
                boolean inArray = false;
                for (MatchingAttributeAction action : radioActions) {
                    if (action.getAttributeClass().isInstance(
                            attributeObject)) {
                        inArray = true;
                    }
                }
                if (!inArray) {
                    continue;
                }
                if (attribute == null) {
                    attribute = (MatchingAttribute) attributeObject;
                } else if (!attribute.getClass().equals(attributeObject
                        .getClass())) {
                    return null;
                }
            }
            return attribute;
        }
    }

    protected class TransformationActorController extends ActorController {

        protected Figure _renderNode(Object node) {
            Figure nf = super._renderNode(node);

            if ((node != null) && !_hide(node)) {
                GraphModel model = getController().getGraphModel();
                Object object = model.getSemanticObject(node);
                CompositeFigure cf = _getCompositeFigure(nf);
                _renderNamedObj(cf, object);
            }

            return nf;
        }

        TransformationActorController(GraphController controller) {
            super(controller);

            _menuFactory.addMenuItemFactory(
                    new MatchingAttributeActionsFactory());

            Action oldConfigureAction = _configureAction;
            _configureAction = new GTEntityConfigureAction("Configure");
            _configureMenuFactory.substitute(oldConfigureAction,
                    _configureAction);
            _configureMenuFactory.addMenuItemListener(
                    TransformationEditor.this);
        }
    }

    protected class TransformationActorGraphController
    extends ActorEditorGraphController {

        protected TransformationActorGraphController() {
            _newRelationAction = new NewRelationAction(new String[][] {
                    { "/ptolemy/vergil/actor/img/relation.gif",
                        GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/relation_o.gif",
                        GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/relation_ov.gif",
                        GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/relation_on.gif",
                        GUIUtilities.SELECTED_ICON } });
        }

        protected void _addHotKeys(JGraph jgraph) {
            List<JGraph> jgraphs = getFrameController().getJGraphs();
            if (jgraphs == null) {
                super._addHotKeys(jgraph);
            } else {
                for (JGraph g : jgraphs) {
                    super._addHotKeys(g);
                }
            }
        }

        protected void _createControllers() {
            super._createControllers();

            _entityController = new TransformationActorController(this);
            _entityPortController = new TransformationPortController(this);
            _linkController = new TransformationLinkController(this);
            _portController = new TransformationExternalPortController(this);
            _relationController = new TransformationRelationController(this);
        }

        private class NewRelationAction extends
                ActorEditorGraphController.NewRelationAction {

            public void actionPerformed(ActionEvent e) {
                if (getFrameController().isTableActive()) {
                    return;
                } else {
                    super.actionPerformed(e);
                }
            }

            private NewRelationAction(String[][] iconRoles) {
                super(iconRoles);
            }
        }
    }

    protected class TransformationExternalPortController
    extends ExternalIOPortController {

        TransformationExternalPortController(GraphController controller) {
            super(controller);

            _menuFactory.addMenuItemFactory(
                    new MatchingAttributeActionsFactory());

            Action oldConfigureAction = _configureAction;
            _configureAction = new GTEntityConfigureAction("Configure");
            _configureMenuFactory.substitute(oldConfigureAction,
                    _configureAction);

            getConfigureMenuFactory().addMenuItemListener(
                    TransformationEditor.this);

            setNodeRenderer(new Renderer());
        }

        private class Renderer extends PortRenderer {

            public Figure render(Object node) {
                if ((node != null) && !_hide(node)) {
                    Figure nf = super.render(node);
                    GraphModel graphModel = getController().getGraphModel();
                    Object object = graphModel.getSemanticObject(node);
                    CompositeFigure cf = _getCompositeFigure(nf);
                    if (cf == null) {
                        cf = new CompositeFigure(nf);
                        _renderNamedObj(cf, object);
                        return cf;
                    } else {
                        _renderNamedObj(cf, object);
                        return nf;
                    }
                }
                return null;
            }
        }
    }

    protected class TransformationFSMGraphController
    extends FSMGraphController {

        protected void _addHotKeys(JGraph jgraph) {
            List<JGraph> jgraphs = getFrameController().getJGraphs();
            if (jgraphs == null) {
                super._addHotKeys(jgraph);
            } else {
                for (JGraph g : jgraphs) {
                    super._addHotKeys(g);
                }
            }
        }

        protected void _createControllers() {
            super._createControllers();

            _stateController = new TransformationStateController(this);
            _transitionController = new TransformationTransitionController(
                    this);
        }
    }

    protected class TransformationLinkController extends LinkController {

        public Connector render(Object edge, FigureLayer layer, Site tailSite,
                Site headSite) {
            Connector connector = super.render(edge, layer, tailSite, headSite);
            if (connector instanceof AbstractConnector) {
                GraphModel graphModel = getController().getGraphModel();
                Object semanticObject = graphModel.getSemanticObject(edge);
                _renderLink(connector, semanticObject);
            }
            return connector;
        }

        TransformationLinkController(GraphController controller) {
            super(controller);

            _menuFactory.addMenuItemFactory(
                    new MatchingAttributeActionsFactory());

            Action oldConfigureAction = _configureAction;
            _configureAction = new GTEntityConfigureAction("Configure");
            _configureMenuFactory.substitute(oldConfigureAction,
                    _configureAction);
        }
    }

    protected class TransformationPortController extends IOPortController {

        TransformationPortController(GraphController controller) {
            super(controller);

            _menuFactory.addMenuItemFactory(
                    new MatchingAttributeActionsFactory());

            Action oldConfigureAction = _configureAction;
            _configureAction = new GTEntityConfigureAction("Configure");
            _configureMenuFactory.substitute(oldConfigureAction,
                    _configureAction);

            setNodeRenderer(new Renderer());
        }

        private class Renderer extends EntityPortRenderer {

            protected Figure _decoratePortFigure(Object node, Figure figure) {
                GraphModel graphModel = getController().getGraphModel();
                Object object = graphModel.getSemanticObject(node);
                CompositeFigure composite = _getCompositeFigure(figure);
                if (composite == null) {
                    composite = new CompositeFigure(figure);
                    _renderNamedObj(composite, object);
                    return composite;
                } else {
                    _renderNamedObj(composite, object);
                    return figure;
                }
            }
        }
    }

    protected class TransformationRelationController
            extends RelationController {

        protected Figure _renderNode(Object node) {
            if ((node != null) && !_hide(node)) {
                Figure nf = super._renderNode(node);
                GraphModel graphModel = getController().getGraphModel();
                Object object = graphModel.getSemanticObject(node);
                CompositeFigure cf = _getCompositeFigure(nf);
                _renderNamedObj(cf, object);
                return nf;
            }

            return super._renderNode(node);
        }

        TransformationRelationController(GraphController controller) {
            super(controller);

            _menuFactory.addMenuItemFactory(
                    new MatchingAttributeActionsFactory());

            Action oldConfigureAction = _configureAction;
            _configureAction = new GTEntityConfigureAction("Configure");
            _configureMenuFactory.substitute(oldConfigureAction,
                    _configureAction);
        }
    }

    protected class TransformationStateController extends StateController {

        TransformationStateController(GraphController controller) {
            super(controller);

            setNodeRenderer(new Renderer(controller.getGraphModel()));

            _menuFactory.addMenuItemFactory(
                    new MatchingAttributeActionsFactory());

            Action oldConfigureAction = _configureAction;
            _configureAction = new GTEntityConfigureAction("Configure");
            _configureMenuFactory.substitute(oldConfigureAction,
                    _configureAction);
        }

        private class Renderer extends StateRenderer {

            public Renderer(GraphModel model) {
                super(model);
            }

            public Figure render(Object node) {
                Figure nf = super.render(node);

                if ((node != null) && !_hide(node)) {
                    GraphModel model = getController().getGraphModel();
                    Object object = model.getSemanticObject(node);
                    CompositeFigure cf = _getCompositeFigure(nf);
                    _renderState(cf, object);
                }

                return nf;
            }
        }
    }

    protected class TransformationTransitionController
    extends TransitionController {

        public TransformationTransitionController(GraphController controller) {
            super(controller);

            _menuFactory.addMenuItemFactory(
                    new MatchingAttributeActionsFactory());

            Action oldConfigureAction = _configureAction;
            _configureAction = new GTEntityConfigureAction("Configure");
            _configureMenuFactory.substitute(oldConfigureAction,
                    _configureAction);
        }

        public Connector render(Object edge, FigureLayer layer, Site tailSite,
                Site headSite) {
            Connector connector = super.render(edge, layer, tailSite, headSite);
            if (connector instanceof AbstractConnector) {
                GraphModel graphModel = getController().getGraphModel();
                Object semanticObject = graphModel.getSemanticObject(edge);
                _renderLink(connector, semanticObject);
            }
            return connector;
        }
    }

    private JPanel _createCellPanel(String value) {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField textField = new JTextField(value, SwingConstants.CENTER);
        textField.setBorder(_EMPTY_BORDER);
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        textField.setOpaque(false);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private void _createTable(TransformationRule transformer) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("Correspondence");

        _tableModel = new DefaultTableModel(new Object[] { "",
                "Pattern Entity", "Replacement Entity" }, 0) {
            public boolean isCellEditable(int row, int column) {
                if (column == 0) {
                    return false;
                } else {
                    return super.isCellEditable(row, column);
                }
            }
        };

        _table = new JTable(_tableModel);
        _table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _table.setEnabled(true);
        _table.setRowHeight(22);
        _table.setSelectionBackground(_SELECTION_COLOR);
        _table.setSelectionForeground(Color.BLACK);

        TableColumnModel model = _table.getColumnModel();
        TableColumn indexColumn = model.getColumn(0);
        indexColumn.setMinWidth(10);
        indexColumn.setPreferredWidth(15);
        indexColumn.setMaxWidth(30);
        DefaultTableCellRenderer indexRenderer = new DefaultTableCellRenderer();
        indexRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        _cellEditor = new CellEditor();
        for (int i = 0; i < 3; i++) {
            TableColumn column = model.getColumn(i);
            column.setCellRenderer(_cellEditor);
            if (i > 0) {
                column.setCellEditor(_cellEditor);
            }
        }

        JTableHeader header = _table.getTableHeader();
        header.setFont(new Font("Dialog", Font.BOLD, 11));
        header.setForeground(Color.BLUE);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 25));

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header
                .getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(new EtchedBorder());
        JButton addButton = new JButton("add");
        addButton.addActionListener(this);
        JButton removeButton = new JButton("remove");
        removeButton.addActionListener(this);
        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);

        panel.add(header, BorderLayout.NORTH);
        panel.add(_table, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = getFrameController().getTabbedPane();
        int index = tabbedPane.getComponentCount();
        tabbedPane.add(panel, index);

        _refreshTable();
    }

    private static String _getCellEditorValue(JPanel editorPanel) {
        JTextField textField = (JTextField) editorPanel.getComponent(0);
        return textField.getText();
    }

    private Color _getHighlightColor(NamedObj object) {
        if (!object.attributeList(CreationAttribute.class).isEmpty()) {
            return _CREATION_COLOR;
        } else if (!object.attributeList(IgnoringAttribute.class).isEmpty()) {
            return _IGNORING_COLOR;
        } else if (!object.attributeList(NegationAttribute.class).isEmpty()) {
            return _NEGATION_COLOR;
        } else if (!object.attributeList(PreservationAttribute.class).isEmpty()) {
            return _PRESERVATION_COLOR;
        } else if (!object.attributeList(OptionAttribute.class).isEmpty()) {
            return _OPTION_COLOR;
        } else {
            return null;
        }
    }

    private static String _getNameWithinContainer(NamedObj object,
            CompositeEntity container) {
        StringBuffer name = new StringBuffer(object.getName());
        NamedObj parent = object.getContainer();
        while (parent != null && parent != container) {
            name.insert(0, '.');
            name.insert(0, parent.getName());
            parent = parent.getContainer();
        }
        if (parent == null) {
            return null;
        } else {
            return name.toString();
        }
    }

    private TransformationEditor _getToplevelFrame() {
        NamedObj toplevel = getFrameController().getTransformationRule();
        TransformationEditor editor = null;
        for (Frame frame : getFrames()) {
            if (frame instanceof TransformationEditor) {
                TransformationEditor gtGraphFrame = (TransformationEditor) frame;
                if (gtGraphFrame.getModel() == toplevel) {
                    editor = gtGraphFrame;
                }
            }
        }
        return editor;
    }

    private static LibraryAttribute _importActorLibrary(Tableau tableau,
            LibraryAttribute gtLibrary) {
        if (gtLibrary != null) {
            try {
                Configuration configuration =
                    (Configuration) tableau.toplevel();
                CompositeEntity actorLibrary = (CompositeEntity) configuration
                        .getEntity("actor library");
                CompositeEntity library = gtLibrary.getLibrary();

                for (String optionalActorClass : OPTIONAL_ACTORS) {
                    try {
                        Class<?> clazz = Class.forName(optionalActorClass);
                        boolean ignore = false;
                        for (Object entity : library.entityList()) {
                            if (entity.getClass().equals(clazz)) {
                                ignore = true;
                                break;
                            }
                        }
                        if (ignore) {
                            continue;
                        }
                        Constructor<?>[] constructors = clazz.getConstructors();
                        NamedObj object = null;
                        String name = null;
                        for (Constructor<?> constructor : constructors) {
                            Class<?>[] types = constructor.getParameterTypes();
                            if (types.length == 2 && types[0].isInstance(
                                    library) && types[1].equals(String.class)) {
                                name = library.uniqueName(
                                        clazz.getSimpleName());
                                object = (NamedObj) constructor.newInstance(
                                        library, name);
                                break;
                            }
                        }
                        if (object != null) {
                            List<?> entities = library.entityList();
                            int i = 0;
                            for (Object entity : entities) {
                                if (entity instanceof EntityLibrary
                                        || ((NamedObj) entity).getName()
                                                .compareTo(name) > 0) {
                                    break;
                                }
                                i++;
                            }
                            object.moveToIndex(i);
                        }

                        String iconFile = optionalActorClass.replace('.', '/') +
                                "Icon.xml";
                        URL xmlFile = clazz.getClassLoader().getResource(
                                iconFile);
                        if (xmlFile != null) {
                            MoMLParser parser = new MoMLParser(
                                    object.workspace());
                            parser.setContext(object);
                            parser.parse(xmlFile, xmlFile);
                        }
                    } catch (Throwable t) {
                        // Do not add the optional actor to the library.
                    }
                }

                Workspace workspace = actorLibrary.workspace();
                try {
                    workspace.getReadAccess();
                    for (Object entityObject : actorLibrary.entityList()) {
                        try {
                            ComponentEntity libraryEntity =
                                (ComponentEntity) entityObject;
                            ComponentEntity entity =
                                (ComponentEntity) libraryEntity.clone(
                                        library.workspace());
                            entity.setContainer(library);
                        } catch (Exception e) {
                            // Ignore this entity in the actor library because
                            // we don't know how to import it.
                        }
                    }
                } finally {
                    workspace.doneReading();
                }

                EntityLibrary utilitiesLibrary =
                    (EntityLibrary) library.getEntity("Utilities");
                for (Object entityObject : utilitiesLibrary.entityList()) {
                    if (entityObject instanceof CompositeActor) {
                        CompositeActor actor = (CompositeActor) entityObject;
                        if (actor.attributeList(GTTableau.Factory.class)
                                .isEmpty()) {
                            new GTTableau.Factory(actor, actor.uniqueName(
                                    "_tableauFactory"));
                        }
                    }
                }

                gtLibrary.setLibrary(library);
            } catch (Exception e) {
                // Ignore, just return a library without any actors or
                // directors.
            }
        }
        return gtLibrary;
    }

    private void _refreshTable() {
        TransformationEditor frame = _getToplevelFrame();
        DefaultTableModel tableModel = frame._tableModel;
        if (tableModel == null) {
            return;
        }
        if (frame._cellEditor != null) {
            frame._cellEditor.stopCellEditing();
        }
        tableModel.removeTableModelListener(frame);

        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        TransformationRule transformer =
            getFrameController().getTransformationRule();
        _refreshTable(frame, transformer, 1, transformer);

        tableModel.addTableModelListener(frame);
    }

    private int _refreshTable(TransformationEditor topLevelFrame,
            TransformationRule transformer, int index,
            CompositeEntity container) {
        try {
            Replacement replacement = transformer.getReplacement();
            Pattern pattern = transformer.getPattern();

            container.workspace().getReadAccess();
            Collection<?> objectCollection = new CombinedCollection<Object>(
                    new Collection<?>[] { container.entityList(),
                            container.relationList() });
            for (Object entityObject : objectCollection) {
                NamedObj object = (NamedObj) entityObject;
                PatternObjectAttribute attribute = GTTools
                        .getPatternObjectAttribute(object);
                if (attribute != null) {
                    attribute.addValueListener(this);
                    String patternObject = attribute.getExpression();
                    if (!patternObject.equals("")) {

                        String patternObjectName = attribute.getExpression();
                        if (patternObjectName.equals("")) {
                            continue;
                        }

                        boolean found = false;
                        if (object instanceof Entity) {
                            found = pattern.getEntity(patternObjectName)
                                    != null;
                        } else if (object instanceof Relation) {
                            found = pattern.getRelation(patternObjectName)
                                    != null;
                        }
                        if (!found) {
                            // If the entity or relation is not found in the
                            // pattern, remove the correspondence.
                            String moml = "<property name=\"" +
                                    attribute.getName() + "\" value=\"\"/>";
                            MoMLChangeRequest request = new MoMLChangeRequest(
                                    this, object, moml);
                            request.setUndoable(true);
                            request.setMergeWithPreviousUndo(true);
                            object.requestChange(request);
                            continue;
                        }
                        String name = _getNameWithinContainer(object,
                                replacement);
                        topLevelFrame._tableModel.addRow(new Object[] {
                                _createCellPanel(Integer.toString(index++)),
                                _createCellPanel(patternObject),
                                _createCellPanel(name) });
                    }
                }
                if (object instanceof CompositeEntity) {
                    index = _refreshTable(topLevelFrame, transformer, index,
                            (CompositeEntity) object);
                }
            }
        } finally {
            container.workspace().doneReading();
        }
        return index;
    }

    private void _removeUnusedToolbarButtons() {
        Component[] components = _toolbar.getComponents();
        int i = 0;
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                Action action = button.getAction();
                String className = action.getClass().getName();
                if (className.endsWith("$RunModelAction")
                        || className.endsWith("$PauseModelAction")
                        || className.endsWith("$StopModelAction")) {
                    _toolbar.remove(i);
                    continue;
                }
            }
            i++;
        }
    }

    private void _renderLink(Connector connector, Object semanticObject) {
        if (semanticObject instanceof NamedObj && connector != null) {
            Color color = _getHighlightColor((NamedObj) semanticObject);
            if (color != null) {
                AbstractConnector c = (AbstractConnector) connector;
                c.setStrokePaint(color);

                if (GTTools.isOptional(semanticObject)) {
                    c.setStroke(new BasicStroke(_OPTION_HIGHLIGHT_THICKNESS));
                    c.setDashArray(new float[] {3.0f, 7.0f});
                } else {
                    c.setStroke(new BasicStroke(_HIGHLIGHT_THICKNESS));
                }
            }
        }
    }

    private void _renderNamedObj(CompositeFigure figure,
            Object semanticObject) {
        if (semanticObject instanceof NamedObj && figure != null) {
            Color color = _getHighlightColor((NamedObj) semanticObject);
            if (color != null) {
                Rectangle2D bounds = figure.getBackgroundFigure().getBounds();
                float padding = _HIGHLIGHT_PADDING;
                boolean isOptional = GTTools.isOptional(semanticObject);
                float thickness = isOptional ? _OPTION_HIGHLIGHT_THICKNESS
                        : _HIGHLIGHT_THICKNESS;
                BasicRectangle bf = new BasicRectangle(bounds.getX() - padding,
                        bounds.getY() - padding,
                        bounds.getWidth() + padding * 2.0,
                        bounds.getHeight() + padding * 2.0,
                        thickness);
                bf.setStrokePaint(color);

                if (isOptional) {
                    bf.setDashArray(new float[] {3.0f, 5.0f});
                }

                int index = figure.getFigureCount();
                if (index < 0) {
                    index = 0;
                }
                figure.add(index, bf);
            }
        }
    }

    private void _renderState(CompositeFigure figure, Object semanticObject) {
        if (semanticObject instanceof NamedObj && figure != null) {
            Color highlightColor =
                _getHighlightColor((NamedObj) semanticObject);
            if (highlightColor != null) {
                Rectangle2D bounds = figure.getBackgroundFigure().getBounds();
                float padding = _HIGHLIGHT_PADDING;
                boolean isOptional = GTTools.isOptional(semanticObject);
                float thickness = isOptional ? _OPTION_HIGHLIGHT_THICKNESS
                        : _HIGHLIGHT_THICKNESS;
                RoundedRectangle rf = new RoundedRectangle(
                        bounds.getX() - padding, bounds.getY() - padding,
                        bounds.getWidth() + padding * 2.0,
                        bounds.getHeight() + padding * 2.0,
                        null, thickness, 32.0, 32.0);
                rf.setStrokePaint(highlightColor);

                if (isOptional) {
                    rf.setDashArray(new float[] {3.0f, 7.0f});
                }

                int index = figure.getFigureCount();
                if (index < 0) {
                    index = 0;
                }
                figure.add(index, rf);
            }
        }
    }

    private void _setCellEditorValue(JPanel editorPanel, String value) {
        JTextField textField = (JTextField) editorPanel.getComponent(0);
        textField.setText(value);
    }

    private void _setCellString(int row, int column, String cellString) {
        JPanel panel = (JPanel) _tableModel.getValueAt(row, column);
        _setCellEditorValue(panel, cellString);
        _tableModel.fireTableCellUpdated(row, column);
    }

    private void _setOrClearPatternObjectAttributes(NamedObj object,
            boolean isSet, Collection<?> filter) {
        try {
            Collection<?> children;
            if (filter == null) {
                children = GTTools.getChildren(object, false, true, true, true);
            } else {
                children = filter;
            }
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                PatternObjectAttribute patternObject = GTTools
                        .getPatternObjectAttribute(child);
                if (isSet) {
                    if (patternObject == null) {
                        patternObject = new PatternObjectAttribute(child,
                                "patternObject");
                    }
                    String name = _getNameWithinContainer(child,
                            getFrameController().getTransformationRule()
                            .getPattern());
                    patternObject.setPersistent(true);
                    patternObject.setExpression(name);
                } else if (patternObject != null) {
                    patternObject.setPersistent(false);
                    patternObject.setExpression("");
                }
                if (child instanceof CompositeEntity) {
                    _setOrClearPatternObjectAttributes((CompositeEntity) child,
                            isSet, null);
                }
            }
        } catch (KernelException e) {
            throw new KernelRuntimeException(e, "Cannot set attribute.");
        }
    }

    private void _setOrUnsetKeyListenersForAllComponents(Container container,
            boolean isSet) {
        for (Component component : container.getComponents()) {
            if (isSet) {
                component.addKeyListener(getFrameController());
            } else {
                component.removeKeyListener(getFrameController());
            }
            if (component instanceof Container) {
                _setOrUnsetKeyListenersForAllComponents((Container) component,
                        isSet);
            }
        }
    }

    private void _setPatternObject(NamedObj replacementObject,
            String patternObjectName, boolean mergeWithPrevious) {
        String moml = "<property name=\"patternObject\" value=\""
                + patternObjectName + "\"/>";
        MoMLChangeRequest request = new MoMLChangeRequest(this,
                replacementObject, moml);
        request.setUndoable(true);
        request.setMergeWithPreviousUndo(mergeWithPrevious);
        replacementObject.requestChange(request);
    }

    private void _showTableError(String message, final int row,
            final int column, final String previousString) {
        String[] options = new String[] { "Edit", "Revert" };
        int selected = JOptionPane.showOptionDialog(null, message,
                "Validation Error", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.ERROR_MESSAGE, null, options, options[1]);
        if (selected == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (_table.isEditing()
                            && (_table.getEditingRow() != row || _table
                                    .getEditingColumn() != column)) {
                        _cellEditor.cancelCellEditing();
                    }
                    _table.editCellAt(row, column);
                    _cellEditor.setPreviousString(previousString);
                }
            });
        } else if (selected == 1) {
            _setCellString(row, column, previousString);
        }
    }

    private static final String _COPY_FROM_PATTERN_HEADER =
        "<!-- From Pattern -->\n";

    private static final String _COPY_FROM_REPLACEMENT_HEADER =
        "<!-- From Replacement -->\n";

    private static final Color _CREATION_COLOR = new Color(0, 192, 0);

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    private static final Border _EMPTY_BORDER = BorderFactory
            .createEmptyBorder();

    private static final float _HIGHLIGHT_PADDING = 1.0f;

    private static final float _HIGHLIGHT_THICKNESS = 3.0f;

    private static final Color _IGNORING_COLOR = Color.GRAY;

    private static final Color _NEGATION_COLOR = new Color(255, 32, 32);

    private static final Color _OPTION_COLOR = Color.BLACK;

    private static final float _OPTION_HIGHLIGHT_THICKNESS = 1.5f;

    private static final Color _PRESERVATION_COLOR = new Color(64, 64, 255);

    private static final Color _SELECTION_COLOR = new Color(230, 230, 255);

    private CellEditor _cellEditor;

    private Component _fullScreenComponent;

    private Component _previousDefaultContext;

    private JDialog _screen;

    private int _selectedIndexBeforeFullScreen;

    private JTable _table;

    private DefaultTableModel _tableModel;

    private class BatchMatchAction extends MatchAction {

        public BatchMatchAction() {
            super("Match Models in a Directory");

            _attribute = new DefaultDirectoryAttribute((Workspace) null);
            _attribute.directory.setBaseDirectory(URIAttribute
                    .getModelURI(getModel()));

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/batchmatch.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/batchmatch_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/batchmatch_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/batchmatch_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Match Ptolemy models in a directory (Ctrl+2)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_2, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            new MultipleViewController();
        }

        private File[] _getModelFiles() {
            TransformationRule rule =
                getFrameController().getTransformationRule();
            Pattern pattern = rule.getPattern();
            DefaultDirectoryAttribute attribute = (DefaultDirectoryAttribute) pattern
                    .getAttribute("DefaultDirectory");
            File directoryFile = null;
            String fileFilter = "";
            boolean recursive = true;
            if (attribute != null) {
                try {
                    directoryFile = attribute.directory.asFile();
                    fileFilter = attribute.fileFilter.stringValue();
                    recursive = ((BooleanToken) attribute.subdirs.getToken())
                            .booleanValue();
                } catch (IllegalActionException e) {
                    throw new KernelRuntimeException(e,
                            "Unable to get boolean " + "token.");
                }
            }

            if (directoryFile == null) {
                ComponentDialog dialog = new ComponentDialog(
                        TransformationEditor.this, "Select Model Directory",
                        new Configurer(_attribute));
                if (dialog.buttonPressed().equalsIgnoreCase("OK")) {
                    try {
                        directoryFile = _attribute.directory.asFile();
                        fileFilter = _attribute.fileFilter.getExpression();
                        recursive = ((BooleanToken) _attribute.subdirs
                                .getToken()).booleanValue();
                    } catch (IllegalActionException e) {
                        throw new KernelRuntimeException(e, "Unable to get "
                                + "boolean token.");
                    }
                }
            }

            if (directoryFile != null && !directoryFile.exists()) {
                MessageHandler.error("Directory " + directoryFile.getPath()
                        + " does not exist.");
                return null;
            }

            if (directoryFile == null) {
                return null;
            } else {
                File[] files = RecursiveFileFilter.listFiles(directoryFile,
                        recursive, true, false, fileFilter);
                return files;
            }
        }

        private DefaultDirectoryAttribute _attribute;

        private class MultipleViewController extends ViewController {

            public void windowDeactivated(WindowEvent e) {
                Window window = e.getWindow();
                if (!(window instanceof MatchResultViewer)) {
                    return;
                }

                MatchResultViewer viewer = (MatchResultViewer) window;
                if (viewer.isVisible() || viewer.isFullscreen()) {
                    return;
                }

                MatchResultViewer.FileSelectionStatus status = viewer
                        .getFileSelectionStatus();
                viewer.clearFileSelectionStatus();
                switch (status) {
                case PREVIOUS:
                    _index = _previousIndex;
                    _viewCurrentModel();
                    break;
                case NEXT:
                    _index = _nextIndex;
                    _viewCurrentModel();
                    break;
                default:
                    _close();
                }
            }

            protected void _close() {
                super._close();
                if (_viewers != null) {
                    for (int i = 0; i < _viewers.length; i++) {
                        MatchResultViewer viewer = _viewers[i];
                        if (viewer != null) {
                            viewer.removeWindowListener(this);
                            viewer.setModified(false);
                            viewer.close();
                            _viewers[i] = null;
                        }
                    }
                    _viewers = null;
                }
            }

            @SuppressWarnings("unchecked")
            MultipleViewController() {
                _files = _getModelFiles();
                if (_files == null) {
                    return;
                }

                _viewers = new MatchResultViewer[_files.length];
                _models = new CompositeEntity[_files.length];
                _allResults = new List[_files.length];

                try {
                    _index = _findNextMatch(-1);
                    if (_index < 0) {
                        MessageHandler.message("No match found.");
                        return;
                    }
                    _viewCurrentModel();
                } catch (Throwable throwable) {
                    _handleErrors(throwable);
                }
            }

            private int _findNextMatch(int index) throws MalformedURLException,
                    Exception {
                for (int i = index + 1; i < _files.length; i++) {
                    List<MatchResult> currentResult = _allResults[i];
                    if (currentResult == null) {
                        CompositeEntity model = _getModel(_files[i]);
                        currentResult = _getMatchResult(model);

                        _models[i] = model;
                        _allResults[i] = currentResult;

                        if (!currentResult.isEmpty()) {
                            return i;
                        }
                    } else if (!currentResult.isEmpty()) {
                        return i;
                    }
                }
                return -1;
            }

            private int _findPreviousMatch(int index) {
                for (int i = index - 1; i >= 0; i--) {
                    if (!_allResults[i].isEmpty()) {
                        return i;
                    }
                }
                return -1;
            }

            private void _viewCurrentModel() {
                try {
                    _previousIndex = _findPreviousMatch(_index);
                    _nextIndex = _findNextMatch(_index);

                    if (_viewers[_index] != null) {
                        _viewers[_index].setVisible(true);
                    } else {
                        _viewers[_index] = _showViewer(_models[_index],
                                _allResults[_index], _files[_index]
                                        .getCanonicalPath());
                        _viewers[_index].setBatchMode(true);
                        _viewers[_index]
                                .setPreviousFileEnabled(_previousIndex >= 0);
                        _viewers[_index].setNextFileEnabled(_nextIndex >= 0);
                        _viewers[_index].addWindowListener(this);
                    }
                } catch (Throwable throwable) {
                    _handleErrors(throwable);
                }
            }

            private List<MatchResult>[] _allResults;

            private File[] _files;

            private int _index;

            private CompositeEntity[] _models;

            private int _nextIndex;

            private int _previousIndex;

            private MatchResultViewer[] _viewers;

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private inner classes                ////

    private static class CellEditor extends CellPanelEditor {

        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            JPanel panel = (JPanel) super.getTableCellEditorComponent(table,
                    value, isSelected, row, column);
            _previousString = _getCellEditorValue(panel);
            return panel;
        }

        private String getPreviousString() {
            return _previousString;
        }

        private void setPreviousString(String previousString) {
            _previousString = previousString;
        }

        private String _previousString;
    }

    private class CreationAttributeAction extends MatchingAttributeAction {

        public CreationAttributeAction(String name,
                MatchingAttributeAction[] group) {
            super(name, group);
        }

        public Class<? extends MatchingAttribute> getAttributeClass() {
            return CreationAttribute.class;
        }
    }

    private static class GTEntityConfigureAction extends ConfigureAction {

        public GTEntityConfigureAction(String description) {
            super(description);
        }

        protected void _openDialog(Frame parent, NamedObj target,
                ActionEvent event) {
            EditorFactory factory = null;
            if (target instanceof GTEntity) {
                try {
                    target.workspace().getReadAccess();
                    List<?> attributeList = target.attributeList(
                            EditorFactory.class);
                    if (!attributeList.isEmpty()) {
                        factory = (EditorFactory) attributeList.get(0);
                    }
                } finally {
                    target.workspace().doneReading();
                }
            }
            if (factory != null) {
                factory.createEditor(target, parent);
            } else {
                super._openDialog(parent, target, event);
            }
        }
    }

    private class IgnoringAttributeAction extends MatchingAttributeAction {

        public IgnoringAttributeAction(String name,
                MatchingAttributeAction[] group) {
            super(name, group);
        }

        public Class<? extends MatchingAttribute> getAttributeClass() {
            return IgnoringAttribute.class;
        }
    }

    /** Action to automatically layout the graph.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 6.1
     @see ActorGraphFrame.LayoutAction
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class LayoutAction extends AbstractAction {

        /** Create a new action to automatically lay out the graph. */
        public LayoutAction() {
            super("Automatic Layout");
            putValue("tooltip", "Layout the Graph (Ctrl+T)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_T, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        }

        /** Lay out the graph.
         *  @param e The action event.
         */
        public void actionPerformed(ActionEvent e) {
            try {
                layoutGraph();
            } catch (Exception ex) {
                MessageHandler.error("Layout failed", ex);
            }
        }
    }

    private class MatchAction extends FigureAction {

        protected List<MatchResult> _getMatchResult(CompositeEntity model) {
            TransformationRule rule =
                getFrameController().getTransformationRule();
            Pattern pattern = rule.getPattern();
            MatchResultRecorder recorder = new MatchResultRecorder();
            _matcher.setMatchCallback(recorder);
            _matcher.match(pattern, model);
            return recorder.getResults();
        }

        protected CompositeEntity _getModel(File file)
                throws MalformedURLException, Exception {
            if (!file.exists()) {
                MessageHandler.error("Model file " + file.getPath()
                        + " does not exist.");
                return null;
            }

            _parser.reset();
            InputStream stream = file.toURI().toURL().openStream();
            CompositeEntity model = (CompositeEntity) _parser.parse(null,
                    file.getCanonicalPath(), stream);
            return model;
        }

        protected MatchResultViewer _showViewer(CompositeEntity model,
                List<MatchResult> results, String sourceFileName)
                throws IllegalActionException, NameDuplicationException {

            MatchResultViewer._setTableauFactory(this, model);
            Configuration configuration =
                getFrameController().getConfiguration();
            if (configuration == null) {
                throw new InternalErrorException("Cannot get configuration.");
            }

            Tableau tableau = configuration.openModel(model);
            MatchResultViewer viewer = (MatchResultViewer) tableau.getFrame();
            viewer.setMatchResult(getFrameController().getTransformationRule(),
                    sourceFileName, results);
            return viewer;
        }

        MatchAction(String name) {
            super(name);
        }

        private GraphMatcher _matcher = new GraphMatcher();

        private MoMLParser _parser = new MoMLParser();
    }

    private class MatchingAttributeAction extends FigureAction {

        public MatchingAttributeAction(String name,
                MatchingAttributeAction[] group) {
            super(name);
            _group = group;
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            LinkedList<MoMLChangeRequest> changeRequests =
                new LinkedList<MoMLChangeRequest>();
            _removeAttributes(changeRequests);

            Class<? extends MatchingAttribute> attributeClass =
                getAttributeClass();
            if (attributeClass != null) {
                _addAttribute(attributeClass, changeRequests);
            }

            _commitChangeRequests(changeRequests);
        }

        public Class<? extends MatchingAttribute> getAttributeClass() {
            return null;
        }

        protected void _addAttribute(Class<? extends MatchingAttribute>
                attributeClass, List<MoMLChangeRequest> changeRequests) {
            Collection<?> objects = _getSelectedObjects();
            String attributeName = attributeClass.getSimpleName();
            for (Object object : objects) {
                NamedObj namedObj = (NamedObj) object;
                String moml = "<group name=\"auto\">" + "<property name=\""
                    + attributeName + "\" class=\""
                    + attributeClass.getName() + "\"/></group>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        namedObj, moml);
                request.setUndoable(true);
                request.setMergeWithPreviousUndo(true);
                changeRequests.add(request);
            }
        }

        protected void _commitChangeRequests(List<MoMLChangeRequest>
                changeRequests) {
            boolean first = true;
            for (MoMLChangeRequest request : changeRequests) {
                if (first) {
                    request.setMergeWithPreviousUndo(false);
                    first = false;
                }
                request.getContext().requestChange(request);
            }
        }

        protected Collection<?> _getSelectedObjects() {
            Collection<?> objects = _getSelectionSet(true);
            NamedObj target = getTarget();
            if (objects.isEmpty() && target != null) {
                HashSet<NamedObj> set = new HashSet<NamedObj>();
                set.add(target);
                objects = set;
            }
            return objects;
        }

        protected void _removeAttributes(List<MoMLChangeRequest>
                changeRequests) {
            Collection<?> objects = _getSelectedObjects();
            List<Class<? extends MatchingAttribute>> attributeClasses =
                new LinkedList<Class<? extends MatchingAttribute>>();
            if (_group == null) {
                attributeClasses.add(getAttributeClass());
            } else {
                for (MatchingAttributeAction action : _group) {
                    attributeClasses.add(action.getAttributeClass());
                }
            }
            for (Class<? extends MatchingAttribute> attributeClass
                    : attributeClasses) {
                for (Object object : objects) {
                    NamedObj namedObj = (NamedObj) object;
                    for (Object attribute : namedObj.attributeList(
                            attributeClass)) {
                        String moml = "<deleteProperty name=\""
                            + ((NamedObj) attribute).getName() + "\"/>";
                        MoMLChangeRequest request = new MoMLChangeRequest(this,
                                namedObj, moml);
                        request.setUndoable(true);
                        request.setMergeWithPreviousUndo(true);
                        changeRequests.add(request);
                    }
                }
            }
        }

        private MatchingAttributeAction[] _group;
    }

    private class NegationAttributeAction extends MatchingAttributeAction {

        public NegationAttributeAction(String name,
                MatchingAttributeAction[] group) {
            super(name, group);
        }

        public Class<? extends MatchingAttribute> getAttributeClass() {
            return NegationAttribute.class;
        }
    }

    private class OptionAttributeAction extends MatchingAttributeAction {

        public OptionAttributeAction(String name,
                MatchingAttributeAction[] group) {
            super(name, group);
        }

        public Class<? extends MatchingAttribute> getAttributeClass() {
            return OptionAttribute.class;
        }

        protected void _addAttribute(Class<? extends MatchingAttribute>
                attributeClass, List<MoMLChangeRequest> changeRequests) {
            if (_isSet) {
                super._addAttribute(attributeClass, changeRequests);
            }
        }

        protected void _setToggleAction(boolean isSet) {
            _isSet = isSet;
        }

        private boolean _isSet;
    }

    private class PasteMoMLChangeRequest extends OffsetMoMLChangeRequest {

        public PasteMoMLChangeRequest(Object originator, NamedObj context,
                String request) {
            super(originator, context, "<group name=\"auto\">\n" + request +
                    "</group>\n");

            _isFromPattern = request.startsWith(_COPY_FROM_PATTERN_HEADER);
            _isFromReplacement = request.startsWith(
                    _COPY_FROM_REPLACEMENT_HEADER);

            CompositeEntity model = getFrameController().getActiveModel();
            _isToPattern = GTTools.isInPattern(model);
            _isToReplacement = GTTools.isInReplacement(model);
        }

        protected void _postParse(MoMLParser parser) {
            Iterator<?> topObjects = parser.topObjectsCreated().iterator();
            while (topObjects.hasNext()) {
                NamedObj topObject = (NamedObj) topObjects.next();
                if (_isToPattern || _isFromReplacement && _isToReplacement) {
                    try {
                        topObject.workspace().getReadAccess();
                        _setOrClearPatternObjectAttributes(topObject, false,
                                null);
                    } finally {
                        topObject.workspace().doneReading();
                    }
                }
            }
            if (_isFromPattern && _isToReplacement || _isFromReplacement
                    && _isToPattern) {
                parser.clearTopObjectsList();
            } else {
                super._postParse(parser);
            }
        }

        private boolean _isFromPattern;

        private boolean _isFromReplacement;

        private boolean _isToPattern;

        private boolean _isToReplacement;
    }

    private class PreservationAttributeAction extends MatchingAttributeAction {

        public PreservationAttributeAction(String name,
                MatchingAttributeAction[] group) {
            super(name, group);
        }

        public Class<? extends MatchingAttribute> getAttributeClass() {
            return PreservationAttribute.class;
        }
    }

    private class SingleMatchAction extends MatchAction {

        public SingleMatchAction() {
            super("Match Model");

            _attribute = new DefaultModelAttribute((Workspace) null);
            ((FileParameter) _attribute.parameter)
                    .setBaseDirectory(URIAttribute.getModelURI(getModel()));

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/match.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/match_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/match_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/match_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Match a Ptolemy model in an external file "
                    + "(Ctrl+1)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_1, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            new SingleViewController();
        }

        private File _getModelFile() {
            File modelFile = null;
            try {
                TransformationRule rule =
                    getFrameController().getTransformationRule();
                Pattern pattern = rule.getPattern();
                DefaultModelAttribute attribute = (DefaultModelAttribute)
                        pattern.getAttribute("DefaultModel");
                if (attribute != null) {
                    FileParameter parameter =
                        (FileParameter) attribute.parameter;
                    if (parameter.getExpression() != null) {
                        modelFile = parameter.asFile();
                    }
                }

                if (modelFile == null) {
                    ComponentDialog dialog = new ComponentDialog(
                            TransformationEditor.this, "Select Model File",
                            new Configurer(_attribute));
                    if (dialog.buttonPressed().equalsIgnoreCase("OK")) {
                        modelFile = ((FileParameter) _attribute.parameter)
                                .asFile();
                    }
                }

                if (modelFile != null && !modelFile.exists()) {
                    MessageHandler.error("Model file " + modelFile.getPath()
                            + " does not exist.");
                    return null;
                }

                return modelFile;
            } catch (IllegalActionException e) {
                throw new KernelRuntimeException(e, "Cannot obtain model file.");
            }
        }

        private DefaultModelAttribute _attribute;

        private class SingleViewController extends ViewController {

            public void windowDeactivated(WindowEvent e) {
                Window window = e.getWindow();
                if (!(window instanceof MatchResultViewer)) {
                    return;
                }

                MatchResultViewer viewer = (MatchResultViewer) window;
                if (viewer.isVisible()) {
                    return;
                }

                _close();
            }

            protected void _close() {
                super._close();
                if (_viewer != null) {
                    _viewer.removeWindowListener(this);
                    _viewer.close();
                    _viewer = null;
                }
            }

            SingleViewController() {
                try {
                    File file = _getModelFile();
                    if (file == null) {
                        return;
                    }

                    CompositeEntity model = _getModel(file);
                    if (model == null) {
                        return;
                    }

                    List<MatchResult> results = _getMatchResult(model);
                    if (results.isEmpty()) {
                        MessageHandler.message("No match found.");
                    } else {
                        _viewer = _showViewer(model, results, file
                                .getCanonicalPath());
                        _viewer.addWindowListener(this);
                    }
                } catch (MalformedURLException ex) {
                    MessageHandler.error("Unable to obtain URL from the "
                            + "input file name.", ex);
                } catch (Exception ex) {
                    throw new InternalErrorException(ex);
                }
            }

            private MatchResultViewer _viewer;
        }
    }

    private class ViewController implements WindowListener {

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
            Window window = e.getWindow();
            if (window == TransformationEditor.this) {
                _close();
            }
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }

        protected void _close() {
            removeWindowListener(this);
        }

        protected void _handleErrors(Throwable throwable) {
            if (throwable instanceof MalformedURLException) {
                MessageHandler.error("Unable to obtain URL from the input "
                        + "file name.", throwable);
            } else {
                throw new InternalErrorException(throwable);
            }
        }

        ViewController() {
            addWindowListener(this);
        }
    }
}
