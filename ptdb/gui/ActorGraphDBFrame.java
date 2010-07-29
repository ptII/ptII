/*
@Copyright (c) 2010 The Regents of the University of California.
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
package ptdb.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.util.Utilities;
import ptdb.kernel.bl.load.LoadManager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.gui.UndeferredGraphicalMessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorGraphFrame;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// ActorGraphDBFrame

/**
 * An extended graph editor frame containing the ability to interface with a
 * model database via the Database menu.
 * 
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

public class ActorGraphDBFrame extends ActorGraphFrame implements
        ActionListener {

    /** Name of the parameter that prevents changes to database. */
    public static final String DB_NO_EDIT_ATTR = "DBNoEdit";
    
    /**
     * Construct a frame associated with the specified Ptolemy II model. After
     * constructing this, it is necessary to call setVisible(true) to make the
     * frame appear. This is typically done by calling show() on the controlling
     * tableau. This constructor results in a graph frame that obtains its
     * library either from the model (if it has one) or the default library
     * defined in the configuration.
     * 
     * @see Tableau#show()
     * @param entity The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     */
    public ActorGraphDBFrame(CompositeEntity entity, Tableau tableau) {

        this(entity, tableau, null);

    }

    /**
     * Construct a frame associated with the specified Ptolemy II model. After
     * constructing this, it is necessary to call setVisible(true) to make the
     * frame appear. This is typically done by calling show() on the controlling
     * tableau. This constructor results in a graph frame that obtains its
     * library either from the model (if it has one), or the
     * <i>defaultLibrary</i> argument (if it is non-null), or the default
     * library defined in the configuration.
     * 
     * @see Tableau#show()
     * @param entity The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     * @param defaultLibrary An attribute specifying the default library to use
     * if the model does not have a library.
     */
    public ActorGraphDBFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {

        super(entity, tableau, defaultLibrary);
        _initActorGraphDBFrame();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Initialize this class. Various actions are instantiated.
     */
    protected void _initActorGraphDBFrame() {

        //        _openSearchFrameAction = new OpenSearchFrameAction(this.getTableau());
        _saveModelToDBAction = new SaveModelToDBAction(this);
        _openDatabaseSetupAction = new DatabaseSetupAction();
        _simpleSearchAction = new SimpleSearchAction(getModel(), this,
                getConfiguration());
        _configureAttributesAction = new ConfigureAttributesAction();

        _openModelMigrationFrameAction = new OpenModelMigrationFrameAction();

        _renameModelAction = new RenameModelAction();

    }

    /**
     * Create the menus that are used by this frame. It is essential that
     * _createGraphPane() be called before this.
     */
    protected void _addMenus() {

        super._addMenus();

        // Until some useful functionality is complete:
        if (true) {

            // Create database menu.
            _dbMenu = new JMenu("Database");
            _dbMenu.setMnemonic(KeyEvent.VK_B);
            _menubar.add(_dbMenu);
            
            JMenu recentModelMenu = new JMenu("Recently Opened Models");
            recentModelMenu.setMnemonic(KeyEvent.VK_R);
            _dbMenu.add(recentModelMenu);
            
            _dbMenu.addSeparator();
            
            GUIUtilities.addHotKey(_getRightComponent(), _simpleSearchAction);
            GUIUtilities.addMenuItem(_dbMenu, _simpleSearchAction);
            
            GUIUtilities.addHotKey(_getRightComponent(), _saveModelToDBAction);
            GUIUtilities.addMenuItem(_dbMenu, _saveModelToDBAction);
            
            GUIUtilities.addHotKey(_getRightComponent(), _renameModelAction);
            GUIUtilities.addMenuItem(_dbMenu, _renameModelAction);
            
            GUIUtilities.addHotKey(_getRightComponent(),
                    _openModelMigrationFrameAction);
            GUIUtilities.addMenuItem(_dbMenu, _openModelMigrationFrameAction);

            
            
            _dbMenu.addSeparator();
            

            // Add menu items if database connection has been established.
            // TODO: if (DB IS CONNECTED) {

            

            // Create search menu.
            //            JMenu searchMenu = new JMenu("Search");
            //            searchMenu.setMnemonic(KeyEvent.VK_C);
            //            _dbMenu.add(searchMenu);

            

            //            GUIUtilities.addMenuItem(searchMenu, _simpleSearchAction);

            //            GUIUtilities
            //                    .addHotKey(_getRightComponent(), _openSearchFrameAction);
            //            GUIUtilities.addMenuItem(searchMenu, _openSearchFrameAction);

            

            GUIUtilities.addHotKey(_getRightComponent(),
                    _openDatabaseSetupAction);
            GUIUtilities.addMenuItem(_dbMenu, _openDatabaseSetupAction);

            GUIUtilities.addHotKey(_getRightComponent(),
                    _configureAttributesAction);
            GUIUtilities.addMenuItem(_dbMenu, _configureAttributesAction);

            

            
            try {

                if (getModel().getAttribute(XMLDBModel.DB_MODEL_ID_ATTR) != null) {

                    updateDBModelHistory(getModel().getName(), false);

                } else {

                    updateDBModelHistory(getModel().getName(), true);

                }

            } catch (Exception ex) {

                MessageHandler.error("Cannot read model history.", ex);

            }
            // TODO: }
        }
        
        if(getModel().getAttribute(DB_NO_EDIT_ATTR) != null){
            _dbMenu.setEnabled(false);
        } else {
            _dbMenu.setEnabled(true);            
        }

    }

    /**
     * Open a dialog to prompt the user to save the data. If Save to Database is
     * selected, the SaveModelToDBFrame is opened. In this case, _CANCELED is
     * returned. This keeps the editing frame open until the complex saving is
     * complete. For filed-based saving, return false if the user clicks
     * "cancel", and otherwise return true. If the user clicks "Save", this also
     * saves the data.
     * @return _SAVED if the file is saved, _DISCARDED if the modifications are
     * discarded, _CANCELED if the operation is canceled by the user (or if
     * saving to the Database), and _FAILED if the user selects save and the
     * save fails.
     */
    protected int _queryForSave() {

        if(getModel().getAttribute(DB_NO_EDIT_ATTR) != null){
            
            return _CANCELED;
           
        }
        
        Object[] options = { "Save to Database", "Save to File System",
                "Discard changes", "Cancel" };

        String query = "Save changes to " + getModel().getName() + "?";

        // Show the MODAL dialog
        int selected = JOptionPane.showOptionDialog(this, query,
                "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (selected == 0) {
            _saveModelToDBAction.actionPerformed(null);
            return _CANCELED;
        } else if (selected == 1) {

            if (_save()) {
                return _SAVED;
            } else {
                return _FAILED;
            }
        }

        if (selected == 2) {
            return _DISCARDED;
        }

        return _CANCELED;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The action for opening the attributes configuration frame.
     */
    protected Action _configureAttributesAction;

    /** The database menu. */
    protected JMenu _dbMenu;

    //    /** The action for opening the pattern match search frame. */
    //    protected Action _openSearchFrameAction;

    protected Action _renameModelAction;

    /** The action for saving a model to the database. */
    protected Action _saveModelToDBAction;

    /** The action for performing a simple database search. */
    protected Action _simpleSearchAction;

    /** The action for saving a model to the database. */
    protected Action _openDatabaseSetupAction;

    /** The action for opening the model migration frame. */
    protected Action _openModelMigrationFrameAction;

    ///////////////////////////////////////////////////////////////////
    ////                private inner classes                      ////

    ///////////////////////////////////////////////////////////////////
    //// ConfigureAttributesAction

    //TODO Consider making static inner class.
    private class ConfigureAttributesAction extends AbstractAction {

        public ConfigureAttributesAction() {
            super("Configure Attributes");

            putValue("tooltip", "Configure Attributes");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        }

        ///////////////////////////////////////////////////////////////
        ////            public methods                          //////

        public void actionPerformed(ActionEvent e) {

            ConfigureAttributesFrame configureAttributesFrame = new ConfigureAttributesFrame();

            configureAttributesFrame.pack();
            configureAttributesFrame
                    .setLocationRelativeTo(ActorGraphDBFrame.this);
            configureAttributesFrame.setVisible(true);

        }
    }

    //    ///////////////////////////////////////////////////////////////////
    //    //// OpenSearchFrameAction
    //
    //    private class OpenSearchFrameAction extends AbstractAction {
    //
    //        public OpenSearchFrameAction(Tableau tableau) {
    //            super("Pattern Search");
    //            _tableau = tableau;
    //            putValue("tooltip", "Pattern Search");
    //            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_P));
    //        }
    //
    //        ///////////////////////////////////////////////////////////////
    //        ////            public methods                          //////
    //
    //        public void actionPerformed(ActionEvent e) {
    //
    //            //            URL toRead = getClass().getClassLoader().getResource(
    //            //                    "ptolemy/actor/gt/controller/ModelBasedTransformation.xml");
    //
    //            URL toRead = getClass().getClassLoader().getResource(
    //                    "ptolemy.actor.gt.controller.Match");
    //
    //            try {
    //
    //                EffigyFactory effigyFactory = new EffigyFactory(
    //                        ActorGraphDBFrame.this.getConfiguration().workspace());
    //
    //                PtolemyEffigy.Factory ptolemyEffigyFactory = new PtolemyEffigy.Factory(
    //                        effigyFactory, "new effigy factory");
    //
    //                Effigy effigy = ptolemyEffigyFactory.createEffigy(
    //                        ActorGraphDBFrame.this.getConfiguration()
    //                                .getDirectory(), null, toRead);
    //
    //                CompositeEntity compositeEntity = new TransformationRule(
    //                        effigy, "transformation rule");
    //
    //                ExtendedGraphFrame frame = new GraphPatternSearchEditor(
    //                        compositeEntity, new Tableau(effigy, "DBSearchframe"),
    //                        ((ActorGraphDBTableau) _tableau).getGtLibrary(),
    //                        getModel(), ActorGraphDBFrame.this);
    //
    //                frame.setBackground(BACKGROUND_COLOR);
    //                frame.pack();
    //                frame.centerOnScreen();
    //                frame.setVisible(true);
    //
    //            } catch (Exception e2) {
    //
    //                e2.printStackTrace();
    //            }
    //
    //        }
    //
    //        private Tableau _tableau;
    //    }

    ///////////////////////////////////////////////////////////////////
    //// SaveModelToDBAction

    /**
     * Save the model to database.
     */
    private class SaveModelToDBAction extends AbstractAction {
        /**
         * Create a new action to save a model to the database.
         * @param source The frame from which the save frame will be opened.
         * 
         */
        public SaveModelToDBAction(ActorGraphDBFrame source) {

            super("Save to Database");

            putValue("tooltip", "Save to Database");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));

            _source = source;
        }

        public void actionPerformed(ActionEvent e) {

            JFrame frame = new SaveModelToDBFrame(getModel(), _source);
            frame.pack();
            frame.setLocationRelativeTo(_source);
            frame.setVisible(true);

        }

        private ActorGraphDBFrame _source;
    }

    ///////////////////////////////////////////////////////////////////
    //// DatabaseSetupAction

    /**
     * Setup database connection.
     */
    //TODO Consider making a static inner class.
    private class DatabaseSetupAction extends AbstractAction {
        /**
         * Create a new action to setup database connection.
         * 
         */
        public DatabaseSetupAction() {

            super("Setup Database Connection");

            putValue("tooltip", "Setup Database Connection");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_C));

        }

        public void actionPerformed(ActionEvent e) {

            JFrame frame = new DatabaseSetupFrame();
            frame.pack();
            frame.setLocationRelativeTo(ActorGraphDBFrame.this);
            frame.setVisible(true);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// OpenModelMigrationFrameAction

    /**
     * Open model migration frame.
     */
    //TODO Consider making a static inner class.
    private class OpenModelMigrationFrameAction extends AbstractAction {
        /**
         * Create a new action to setup database connection.
         * 
         */
        public OpenModelMigrationFrameAction() {

            super("Migrate Models");

            putValue("tooltip", "Migrate Models");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_M));

        }

        public void actionPerformed(ActionEvent e) {

            JFrame frame = new ModelMigrationFrame();
            frame.pack();
            frame.setVisible(true);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// RenameModelAction

    /**
     * Action for performing model renaming. 
     */
    private class RenameModelAction extends AbstractAction {

        /**
         * Default constructor. 
         */
        public RenameModelAction() {

            super("Rename Model");

            putValue("tooltip", "Rename Model");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_N));

        }

        public void actionPerformed(ActionEvent e) {

            // Check whether the model is an existing model.
            // If it is not an existing model, show text message to tell the user.
            if (Utilities.getIdFromModel(getModel()) == null) {
                // A new model that does not exist in the database. 

                JOptionPane.showMessageDialog(ActorGraphDBFrame.this,
                        "This model hasn't been saved to the database"
                                + " yet, so you cannot rename it.");

            } else {
                // If the model is an existing model, show the rename frame. 

                RenameModelFrame renameModelFrame = new RenameModelFrame(
                        getModel(), ActorGraphDBFrame.this);

                renameModelFrame.pack();
                renameModelFrame.setLocationRelativeTo(ActorGraphDBFrame.this);
                renameModelFrame.setVisible(true);
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    //// SimpleSearchAction

    /**
     * Perform a simple database search.
     */
    private class SimpleSearchAction extends AbstractAction {
        /**
         * Create a new action to save a model to the database.
         * @param model The model into which search results would be imported.
         * @param frame The editing frame from which the simple search window
         * will open.
         * @param configuration The configuration under which models from the
         * database will be loaded.
         * 
         */
        public SimpleSearchAction(NamedObj model, JFrame frame,
                Configuration configuration) {

            super("Search Database");

            putValue("tooltip", "Search Database");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_B));

            _containerModel = model;
            _sourceFrame = frame;
            _configuration = configuration;

        }

        public void actionPerformed(ActionEvent e) {

            JFrame frame = new SimpleSearchFrame(_containerModel, _sourceFrame,
                    _configuration, getTableau());
            frame.pack();
            frame.setLocationRelativeTo(_sourceFrame);
            frame.setVisible(true);

        }

        private NamedObj _containerModel;
        private JFrame _sourceFrame;
        private Configuration _configuration;

    }

    ///////////////////////////////////////////////////////////////////
    //// DBHistoryMenuListener

    /** Listener for help menu commands. */
    private class DBHistoryMenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // Make this the default context for modal messages.
            UndeferredGraphicalMessageHandler
                    .setContext(ActorGraphDBFrame.this);

            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();

            try {

                PtolemyEffigy effigy = LoadManager.loadModel(((JMenuItem) e
                        .getSource()).getText(), getConfiguration());

                if (effigy != null) {

                    effigy.showTableaux();

                } else {

                    JOptionPane
                            .showMessageDialog(ActorGraphDBFrame.this,
                                    "The specified model could "
                                            + "not be found in the database.",
                                    "Load Error",
                                    JOptionPane.INFORMATION_MESSAGE, null);

                }

            } catch (Exception ex) {

                MessageHandler.error("Cannot read model history.", ex);

                try {

                    updateDBModelHistory(actionCommand, true);

                } catch (IOException ex2) {

                    // Ignore

                }

            }

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private String _getDBModelHistoryFileName() throws IOException {

        return StringUtilities.preferencesDirectory() + "DBModelHistory.txt";

    }

    private void _populateDBModelHistory(List historyList) {

        Component[] components = _dbMenu.getMenuComponents();
        JMenu history = null;

        for (Component component : components) {

            if (component instanceof JMenu
                    && ((JMenu) component).getText().equals(
                            "Recently Opened Models")) {

                history = (JMenu) component;

            }

        }

        if (history == null) {
            throw new KernelRuntimeException(

            "Unexpected loss of Recently Opened Models menu.");

        }

        DBHistoryMenuListener listener = new DBHistoryMenuListener();

        history.removeAll();

        for (int i = 0; i < historyList.size(); i++) {

            JMenuItem item = new JMenuItem((String) historyList.get(i));
            item.addActionListener(listener);
            history.add(item);

        }

    }

    /**
     * Get the history from the file that contains names Always return a list,
     * that can be empty
     * @return list of file history
     */
    private List<String> _readHistory() throws IOException {
        ArrayList<String> historyList = new ArrayList<String>();
        String historyFileName = _getDBModelHistoryFileName();
        if (!new File(historyFileName).exists()) {
            // No history file, so just return
            return historyList;
        }
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(historyFileName);
            // FindBugs flags, but FileReader is closed below.
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                historyList.add(line);
            }
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }

        return historyList;
    }

    /**
     * Update the DBModelHistory.
     * 
     * @param modelName The model name to add to the history.
     * @param delete Indication if the history should be updated with model name
     * or if it should be deleted.  If true, the opened model will not be added
     * to the history.  If false, it will.
     * @exception IOException Thrown if the history cannot be read from or
     * written to.
     * 
     */
    public void updateDBModelHistory(String modelName, boolean delete)
            throws IOException {

        List<String> historyList = _readHistory();

        // Remove if already present (then added to first position)
        for (int i = 0; i < historyList.size(); i++) {

            if (historyList.get(i).equals(modelName)) {

                historyList.remove(i);

            }

        }
        
        // Add to fist position
        if (!delete) {

            historyList.add(0, modelName);

        }
        
        // Remove if depth > limit
        if (historyList.size() > _historyDepth) {

            historyList.remove(historyList.size() - 1);

        }

        // Serialize history
        _writeDBModelHistory(historyList);

        // Update submenu
        _populateDBModelHistory(historyList);

    }

    private void _writeDBModelHistory(List<String> historyList)
            throws IOException {

        FileWriter fileWriter = null;
        try {

            fileWriter = new FileWriter(_getDBModelHistoryFileName());
            for (String line : historyList) {

                fileWriter.write(line + "\n");

            }

        } finally {

            if (fileWriter != null) {

                fileWriter.close();

            }

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // History depth
    private int _historyDepth = 4;

}
