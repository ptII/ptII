/* Top-level window for Ptolemy models with a menubar and status bar.

 Copyright (c) 1998-2009 The Regents of the University of California.
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

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.gui.StatusBar;
import ptolemy.gui.Top;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLParser;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// TableauFrame

/**
 This is a top-level window associated with a tableau that has
 a menubar and status bar. Derived classes should add components
 to the content pane using a line like:
 <pre>
 getContentPane().add(component, BorderLayout.CENTER);
 </pre>
 The base class provides generic features for menubars and toolbars,
 and this class specializes the base class for Ptolemy II.
 <p>
 A help menu is provided with two entries, About and Help. In both
 cases, an HTML file is opened.  The configuration can specify which
 HTML file to open by containing an instance of FileParameter with
 name "_about" or "_help".  The value of this attribute is a file
 name (which may begin with the keywords $CLASSPATH or $PTII to
 specify that the file is located relative to the CLASSPATH or to
 the Ptolemy II installation directory).

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (celaine)
 */
public class TableauFrame extends Top {
    /** Construct an empty top-level frame.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  It may also be desirable to call centerOnScreen().
     */
    public TableauFrame() {
        this(null);
    }

    /** Construct an empty top-level frame managed by the specified
     *  tableau and the default status bar. After constructing this,
     *  it is necessary to call setVisible(true) to make the frame appear.
     *  It may also be desirable to call centerOnScreen().
     *  @param tableau The managing tableau.
     */
    public TableauFrame(Tableau tableau) {
        this(tableau, new StatusBar());
    }

    /** Construct an empty top-level frame managed by the specified
     *  tableau with the specified status bar. After constructing this,
     *  it is necessary to call setVisible(true) to make the frame appear.
     *  It may also be desirable to call centerOnScreen().
     *  @param tableau The managing tableau.
     *  @param statusBar The status bar, or null to not include one.
     */
    public TableauFrame(Tableau tableau, StatusBar statusBar) {
        this(tableau, statusBar, null);
    }

    /** Construct an empty top-level frame managed by the specified
     *  tableau with the specified status bar and associated Placeable
     *  object. Associating an instance of Placeable with this
     *  frame has the effect that when this frame is closed,
     *  if the placeable contains instances of WindowSizeAttribute
     *  and/or SizeAttribute, then the window sizes are recorded.
     *  After constructing this,
     *  it is necessary to call setVisible(true) to make the frame appear.
     *  It may also be desirable to call centerOnScreen().
     *  @param tableau The managing tableau.
     *  @param statusBar The status bar, or null to not include one.
     *  @param placeable The associated Placeable.
     */
    public TableauFrame(Tableau tableau, StatusBar statusBar,
            Placeable placeable) {
        super(statusBar);

        // Set this frame in the tableau so that later in the constructor we can
        // invoke tableau.getFrame() to get back this frame instead of null.
        // -- tfeng (01/15/2009)
        try {
            if (tableau != null) {
                tableau.setFrame(this);
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException("This frame of class " + getClass()
                    + " is not compatible with tableau " + tableau.getName());
        }

        setTableau(tableau);
        setIconImage(_getDefaultIconImage());
        _placeable = placeable;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the alternative pack() interface for the ptolemy.gui.Top JFrame.
     * @return the alternative pack() interface if one was set by the
     * _alternateTopPackClass in the Configuration.  If one there is no TopPack,
     * then return null.
     * @see #pack()
     */
    public TopPack getAlternateTopPack() {
      return _topPack;
    }

    /** Get the configuration at the top level of the hierarchy.
     *  @return The configuration controlling this frame, or null
     *   if there isn't one.
     */
    public Configuration getConfiguration() {
        NamedObj tableau = getTableau();

        if (tableau != null) {
            NamedObj toplevel = tableau.toplevel();

            if (toplevel instanceof Configuration) {
                return (Configuration) toplevel;
            }
        }

        return null;
    }

    /** Get the model directory in the top level configuration.
     *  @return The model directory, or null if there isn't one.
     */
    public ModelDirectory getDirectory() {
        Configuration configuration = getConfiguration();

        if (configuration != null) {
            return configuration.getDirectory();
        } else {
            return null;
        }
    }

    /** Get the effigy for the model associated with this window.
     *  @return The effigy for the model, or null if none exists.
     */
    public Effigy getEffigy() {
        if (_tableau != null) {
            return (Effigy) _tableau.getContainer();
        }

        return null;
    }

    /** Get the effigy for the specified Ptolemy model.
     *  This searches all instances of PtolemyEffigy deeply contained by
     *  the directory, and returns the first one it encounters
     *  that is an effigy for the specified model.
     *  @param model The model for which an effigy is desired.
     *  @return The effigy for the model, or null if none exists.
     */
    public PtolemyEffigy getEffigy(NamedObj model) {
        Configuration configuration = getConfiguration();

        if (configuration != null) {
            return configuration.getEffigy(model);
        } else {
            return null;
        }
    }

    /** Get the tableau associated with this frame.
     *  @return The tableau associated with this frame.
     *  @see #setTableau(Tableau)
     */
    public Tableau getTableau() {
        return _tableau;
    }

    /** Return true if the data associated with this window has been
     *  modified since it was first read or last saved.  This returns
     *  the value set by calls to setModified(), or false if that method
     *  has not been called.
     *  @return True if the data has been modified.
     */
    public boolean isModified() {
        Effigy effigy = getEffigy();

        if (effigy != null) {
            return effigy.isModified();
        } else {
            return super.isModified();
        }
    }

    /** Record whether the data associated with this window has been
     *  modified since it was first read or last saved.  If you call
     *  this with a true argument, then subsequent attempts to close
     *  the window will trigger a dialog box to confirm the closing.
     *  This overrides the base class to delegate to the effigy.
     *  @param modified True if the data has been modified.
     */
    public void setModified(boolean modified) {
        Effigy effigy = getEffigy();

        if (effigy != null) {
            effigy.setModified(modified);
        } else {
            super.setModified(modified);
        }
    }

    /** Set the tableau associated with this frame.
     *  @param tableau The tableau associated with this frame.
     *  @see #getTableau()
     */
    public void setTableau(Tableau tableau) {
        _tableau = tableau;
    }

    /**
     * Optionally invoke an alternative pack() method.  If the
     * _alternateTopPackClass attribute in the Configuration is set to
     * the name of a class that implements the TopPack interface, then
     * {@link ptolemy.actor.gui.TopPack#pack(Top, boolean)} is called.
     * If the _alternateTopPackClass attribute is not set or set
     * improperly, then Top.pack() is called from this method.
     */
    public void pack() {
        super.pack();
        Configuration configuration = getConfiguration();
        // Check to see if we have a configuration because
        // if we do "Listen to Actor", then getConfiguration()
        // is returning null?
        if (configuration != null) {
            StringAttribute alternateTopPackClassAttribute = (StringAttribute) configuration
                    .getAttribute("_alternateTopPackClass");

            // If the _alternateTopPackClass attribute is present,
            // then we use the specified class to pack the gui
            // if it is not set, just use Top.pack().

            if (alternateTopPackClassAttribute != null) {
                // Get the class that will build the library from the plugins
                String topPackClassName = "";
                try {
                    topPackClassName = alternateTopPackClassAttribute
                            .getExpression();
                    Class topPackClass = Class.forName(topPackClassName);
                    _topPack = (TopPack) topPackClass.newInstance();
                    // Do the alternate pack
                    _topPack.pack(this, _packCalled);
                    _packCalled = true;
                } catch (Exception e) {
                    System.out
                            .println("Could not get the alternate top pack class \""
                                    + topPackClassName
                                    + "\" named in the configuration by the "
                                    + "_alternateTopPackClass attribute: "
                                    + e.getMessage()
                                    + "\nPlease check your configuration and try again.  Using the default "
                                    + "Top pack().");
                }
            } else {
                super.pack();
            }
        }
    }

    /** If a PDF printer is available print to it.
     *  @exception PrinterException If a printer with the string "PDF"
     * cannot be found or if the job cannot be set to the PDF print
     * service or if there is another problem printing.
     */
    public void printPDF() throws PrinterException {
        _printPDF();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the default file to open when About is invoked.
     *  This file should be relative to the home installation directory.
     *  This file is used if the configuration does not specify an about file.
     */
    public String aboutFile = "ptolemy/configs/intro.htm";

    /** The name of the default file to open when Help is invoked.
     *  This file should be relative to the home installation directory.
     *  This file is used if the configuration does not specify a help file.
     */
    public String helpFile = "ptolemy/configs/doc/basicHelp.htm";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to open the intro.htm splash window,
     *  which is in the directory ptolemy/configs.
     */
    protected void _about() {
        // NOTE: We take some care here to ensure that this window is
        // only opened once.
        ModelDirectory directory = getDirectory();

        if (directory != null) {
            try {
                Configuration configuration = getConfiguration();
                FileParameter aboutAttribute = (FileParameter) configuration
                        .getAttribute("_about", FileParameter.class);
                URL doc;

                if (aboutAttribute != null) {
                    doc = aboutAttribute.asURL();
                } else {
                    doc = getClass().getClassLoader().getResource(aboutFile);
                }

                // The usual mechanism for opening a file is:
                // configuration.openModel(null, doc, doc.toExternalForm());
                // However, in this case, we want a smaller size, so we do
                // something custom.
                // Check to see whether the model is already open.
                Effigy effigy = directory.getEffigy(doc.toExternalForm());

                if (effigy == null) {
                    // No main welcome window.  Create one.
                    EffigyFactory effigyFactory = new HTMLEffigyFactory(
                            directory.workspace());
                    effigy = effigyFactory.createEffigy(directory, (URL) null,
                            doc);

                    effigy.identifier.setExpression(doc.toExternalForm());
                    effigy.uri.setURL(doc);

                    // If this fails, we do not want the effigy
                    // in the directory.
                    try {
                        // Create a tableau if there is a tableau factory.
                        TableauFactory factory = (TableauFactory) getConfiguration()
                                .getAttribute("tableauFactory");

                        if (factory != null) {
                            Tableau tableau = factory.createTableau(effigy);

                            if (tableau == null) {
                                throw new Exception("Can't create Tableau.");
                            }

                            // The first tableau is a master.
                            tableau.setMaster(true);

                            // NOTE: This size is the same as what's in
                            // the welcome window XML files in configs.
                            tableau.size.setExpression("[650, 350]");
                            tableau.show();
                            return;
                        }
                    } catch (Exception ex) {
                        // Remove effigy.
                        effigy.setContainer(null);
                    }
                } else {
                    // Model already exists.
                    effigy.showTableaux();
                    return;
                }
            } catch (Exception ex) {
            }
        }

        // Don't report any errors.  Just use the default.
        super._about();
    }

    /** Add a View menu and items to the File:New menu
     *  if a tableau was given in the constructor.
     *  <p>If the configuration has a _disableFileNew parameter that
     *  is set to true, then we do not populate the File-&gt;New menu.
     */
    protected void _addMenus() {
        super._addMenus();

        if (_tableau != null) {
            // Start with the File:New menu.
            // Check to see if we have an effigy factory, and whether it
            // is capable of creating blank effigies.
            final Configuration configuration = getConfiguration();
            EffigyFactory effigyFactory = (EffigyFactory) configuration
                    .getEntity("effigyFactory");
            boolean canCreateBlank = false;
            final ModelDirectory directory = getDirectory();


            // If the configuration has a _disableFileNew parameter that
            // is set to true, then we do not populate the File->New menu.
            boolean disableFileNew = false;
            try {
                Parameter disableFileNewParameter = (Parameter) configuration
                    .getAttribute("_disableFileNew", Parameter.class);
                if (disableFileNewParameter != null) {
                    Token token = disableFileNewParameter.getToken();
                    if (token instanceof BooleanToken) {
                        disableFileNew = ((BooleanToken) token).booleanValue();
                    }
                }
            } catch (Exception ex) {
                // Ignore, there was a problem reading _disableFileNew,
                // so we enable the File->New Menu choice
            }

            if ((effigyFactory != null) && (directory != null)
                    && !disableFileNew) {
                List factoryList = effigyFactory
                        .entityList(EffigyFactory.class);
                Iterator factories = factoryList.iterator();

                while (factories.hasNext()) {
                    final EffigyFactory factory = (EffigyFactory) factories
                            .next();

                    if (!factory.canCreateBlankEffigy()) {
                        continue;
                    }

                    canCreateBlank = true;

                    String name = factory.getName();
                    ActionListener menuListener = new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            Effigy effigy = null;

                            try {
                                effigy = factory.createEffigy(directory);
                            } catch (Exception ex) {
                                MessageHandler.error(
                                        "Could not create new effigy", ex);
                            }

                            configuration.createPrimaryTableau(effigy);
                        }
                    };

                    JMenuItem item = new JMenuItem(name);
                    item.setActionCommand(name);
                    item.setMnemonic(name.charAt(0));
                    item.addActionListener(menuListener);
                    if (name.equals("Graph Editor")) {
                        // From Daniel Crawl for Kepler
                        item.setAccelerator(
                            KeyStroke.getKeyStroke(
                                KeyEvent.VK_N,
                                Toolkit.getDefaultToolkit()
                                    .getMenuShortcutKeyMask()));
                    }
                    ((JMenu) _fileMenuItems[2]).add(item);
                }
            }

            if (canCreateBlank) {
                // Enable the "New" item in the File menu.
                _fileMenuItems[2].setEnabled(true);
            }

            // Next do the View menu.
            Effigy tableauContainer = (Effigy) _tableau.getContainer();

            if (tableauContainer != null) {
                _factoryContainer = tableauContainer.getTableauFactory();

                if (_factoryContainer != null) {
                    // If setTableau() has been called on the effigy,
                    // then there are multiple possible views of data
                    // represented in this top-level window.
                    // Thus, we create a View menu here.
                    _viewMenu = new JMenu("View");
                    _viewMenu.setMnemonic(KeyEvent.VK_V);
                    _menubar.add(_viewMenu);

                    ViewMenuListener viewMenuListener = new ViewMenuListener();
                    Iterator factories = _factoryContainer.attributeList(
                            TableauFactory.class).iterator();

                    while (factories.hasNext()) {
                        TableauFactory factory = (TableauFactory) factories
                                .next();
                        String name = factory.getName();
                        JMenuItem item = new JMenuItem(name);

                        // The "action command" is available to the listener.
                        item.setActionCommand(name);
                        item.setMnemonic(name.charAt(0));
                        item.addActionListener(viewMenuListener);
                        _viewMenu.add(item);
                    }
                }
            }
        }
    }

    /** Close the window.  Derived classes should override this to
     *  release any resources or remove any listeners.  In this class,
     *  if the data associated with this window have been modified,
     *  and there are no other tableaux in the parent effigy or
     *  any effigy that contains it,
     *  then ask the user whether to save the data before closing.
     *  @return False if the user cancels on a save query.
     */
    protected boolean _close() {
        // Record window properties, if appropriate.
        if (_placeable instanceof NamedObj) {
            Iterator properties = ((NamedObj) _placeable).attributeList(
                    WindowPropertiesAttribute.class).iterator();
            while (properties.hasNext()) {
                WindowPropertiesAttribute windowProperties = (WindowPropertiesAttribute) properties
                        .next();
                windowProperties.recordProperties(this);
            }
            // Regrettably, have to also record the size of the contents
            // because in Swing, setSize() methods do not set the size.
            // Only the first component size is recorded.
            properties = ((NamedObj) _placeable).attributeList(
                    SizeAttribute.class).iterator();
            while (properties.hasNext()) {
                SizeAttribute size = (SizeAttribute) properties.next();
                Component[] components = getContentPane().getComponents();
                if (components.length > 0) {
                    size.recordSize(components[0]);
                }
            }
        }
        boolean result = true;
        // If we were given no tableau, then just close the window
        if (getEffigy() == null) {
            result = super._close();
        } else {
            Effigy masterEffigy = getEffigy().masterEffigy();

            // If the top-level effigy has any open tableau that
            // is not this one, and this one is not a master,
            // then simply close.  No need to prompt
            // for save, as that will be done when that tableau is closed.
            if (!_tableau.isMaster()) {
                List tableaux = masterEffigy.entityList(Tableau.class);
                Iterator tableauxIterator = tableaux.iterator();

                while (tableauxIterator.hasNext()) {
                    Tableau tableau = (Tableau) tableauxIterator.next();

                    if (!(tableau instanceof DialogTableau)
                            && (tableau != _tableau)) {
                        // NOTE: We use dispose() here rather than just hiding the
                        // window.  This ensures that derived classes can react to
                        // windowClosed events rather than overriding the
                        // windowClosing behavior given here.
                        dispose();
                        if (_placeable != null) {
                            _placeable.place(null);
                        }
                        return true;
                    }
                }
            }

            // If we get here, there was no other tableau.
            // NOTE: Do not use the superclass method so we can
            // check for children of the model.
            // NOTE: We use dispose() here rather than just hiding the
            // window.  This ensures that derived classes can react to
            // windowClosed events rather than overriding the
            // windowClosing behavior given here.
            if (isModified()) {
                int reply = _queryForSave();

                if ((reply == _DISCARDED) || (reply == _FAILED)) {
                    // If the model has children, then
                    // issue a warning that those children will
                    // persist.  Give the user the chance to cancel.
                    if (!_checkForDerivedObjects()) {
                        if (_placeable != null) {
                            _placeable.place(null);
                        }
                        return false;
                    }
                }

                if (reply == _SAVED) {
                    dispose();
                } else if (reply == _DISCARDED) {
                    dispose();

                    // If the changes were discarded, then we want
                    // to mark the model unmodified, so we don't get
                    // asked again if somehow the model is re-opened.
                    setModified(false);

                    // Purge any record of the model, since we have
                    // chosen to not save the changes, so the next time
                    // this is opened, it should be read again from the file.
                    try {
                        MoMLParser.purgeModelRecord(masterEffigy.uri.getURL());
                    } catch (MalformedURLException e) {
                        // Ignore... Hopefully will be harmless.
                    }
                } else {
                    result = false;
                }
            } else {
                // Window is not modified, so just dispose.
                dispose();
            }
        }
        if (_placeable != null) {
            _placeable.place(null);
        }
        return result;
    }

    /** Confirm that writing the specified model to the specified file is OK.
     *  In particular, if the file exists, ask the user whether it is OK
     *  to overwrite. If there is an open model from the specified file,
     *  determine whether it has been modified, and prompt to discard changes
     *  if it has.  Close the previously open model. If the previously open
     *  model on this file contains the specified model, the it is never
     *  OK to do the write, so return false.
     *  @param model The model to write to the file, or null specify
     *   that this will be delegated to the effigy associated with this
     *   tableau.
     *  @param file The file to write to.
     *  @return True if it is OK to write the model to the file.
     *  @exception MalformedURLException  If the file cannot be converted
     *  to a URL.
     */
    protected boolean _confirmFile(Entity model, File file)
            throws MalformedURLException {
        URL newURL = file.toURI().toURL();
        String newKey = newURL.toExternalForm();
        Effigy previousOpen = getDirectory().getEffigy(newKey);

        // If there is a previous open, and it's not the same,
        // then we need to close the previous.
        // If we do save as to the same file, then we will get
        // the current effigy, and we don't want to close it.
        if ((previousOpen != null) && (previousOpen != getEffigy())) {
            // The destination file is already open.
            // NOTE: If the model being saved is a submodel of the
            // model associated with previousOpen, then we will close
            // it before we save it, which will result in an error
            // like "can't find an effigy to delegate writing to."
            // I don't have a good workaround for this, so for now,
            // disallow this type of save.  If the model argument
            // is specified, then check it. Otherwise check the
            // effigies.
            boolean containmentError = false;

            if (model != null) {
                if (previousOpen instanceof PtolemyEffigy) {
                    NamedObj possibleContainer = ((PtolemyEffigy) previousOpen)
                            .getModel();

                    if ((possibleContainer != null)
                            && possibleContainer.deepContains(model)) {
                        containmentError = true;
                    }
                }
            } else {
                if (previousOpen.deepContains(getEffigy())) {
                    containmentError = true;
                }
            }

            if (containmentError) {
                MessageHandler.error("Cannot replace a model with a submodel."
                        + " Please choose a different file name.");
                return false;
            }

            if (previousOpen.isModified()) {
                // Bring any visible tableaux to the foreground,
                // then ask if it's OK to discard the changes?
                previousOpen.showTableaux();

                String confirm = "Unsaved changes in " + file.getName()
                        + ". OK to discard changes?";

                // Show a MODAL dialog
                int selected = JOptionPane.showOptionDialog(this, confirm,
                        "Discard changes?", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);

                if (selected == 1) {
                    return false;
                }

                // If the model has children, then
                // issue a warning that those children will
                // persist.  Give the user the chance to cancel.
                if (!_checkForDerivedObjects()) {
                    return false;
                }

                // Mark unmodified so that we don't get another
                // query when it is closed.
                previousOpen.setModified(false);
            }

            previousOpen.closeTableaux();
        }

        if (file.exists()) {
            // Ask for confirmation before overwriting a file.
            String query = "Overwrite " + file.getName() + "?";

            // Show a MODAL dialog
            int selected = JOptionPane.showOptionDialog(this, query,
                    "Overwrite file?", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, null, null);

            if (selected == 1) {
                return false;
            }
        }

        return true;
    }

    /** Close all open tableaux, querying the user as necessary to save data,
     *  and then exit the application.  If the user cancels on any save,
     *  then do not exit.
     *  @see Tableau#close()
     */
    protected void _exit() {
        ModelDirectory directory = getDirectory();

        if (directory == null) {
            return;
        }

        Iterator effigies = directory.entityList(Effigy.class).iterator();

        while (effigies.hasNext()) {
            Effigy effigy = (Effigy) effigies.next();

            if (!effigy.closeTableaux()) {
                return;
            }

            try {
                effigy.setContainer(null);
            } catch (Exception ex) {
                throw new InternalErrorException(
                        "Unable to set effigy container to null! " + ex);
            }
        }

        // Some of the effigies closed may have triggered other
        // effigies being opened (if they were unnamed, and a saveAs()
        // was triggered).  So we need to close those now.
        // This is just a repeat of the above.
        effigies = directory.entityList(Effigy.class).iterator();

        while (effigies.hasNext()) {
            Effigy effigy = (Effigy) effigies.next();

            if (!effigy.closeTableaux()) {
                return;
            }

            try {
                effigy.setContainer(null);
            } catch (Exception ex) {
                throw new InternalErrorException(
                        "Unable to set effigy container to null! " + ex);
            }
        }
    }

    /** Return the default icon image, or null if there is none.  Note
     *  that Frame.setIconImage(null) will set the image to the
     *  default platform dependent image.  If the configuration
     *  contains a FileAttribute called _applicationIcon, then the
     *  value of the _applicationIcon is used.  Otherwise, the default
     *  value is ptolemy/actor/gui/PtolemyIISmallIcon.gif, which
     *  is looked for in the classpath.
     *  @return The default icon image, or null if there is none.
     */
    protected Image _getDefaultIconImage() {
        if (_defaultIconImage == null) {
            URL url = null;
            try {
                Configuration configuration = getConfiguration();
                FileParameter iconAttribute = (FileParameter) configuration
                        .getAttribute("_applicationIcon", FileParameter.class);

                if (iconAttribute != null) {
                    url = iconAttribute.asURL();
                }
            } catch (Throwable ex) {
                // Ignore, we will set url to the default.
            }
            if (url == null) {
                // Note that PtolemyIISmallIcon.gif is also in doc/img.
                // We place a duplicate copy here to make it easy to ship
                // jar files that contain all the appropriate images.
                try {
                    // Use nameToURL so this will work with WebStart
                    // and so that this class can be extended and we
                    // will still find the gif.
                    url = FileUtilities.nameToURL(
                            "$CLASSPATH/ptolemy/actor/gui/PtolemyIISmallIcon.gif", null,
                            getClass().getClassLoader());
                } catch (Throwable throwable) {
                    // Ignore, stick with the default
                }
            }
            if (url == null) {
                return null;
            }

            // FIXME: For awhile under kepler if we had no _applicationIcon
            // parameter and the PtolemyIISmallIcon.gif image was somewhere
            // not in the ptolemy tree but still in the classpath, the
            // icon would only partially render.  This could be because
            // in Kepler, VergilApplication does not run everything in
            // the Swing Event thread.  Nandita suggested using this as
            // a workaround:
            // setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
            // Another thing to check for is whether the icon is in the
            // same directory as TableauFrame.class
            Toolkit tk = Toolkit.getDefaultToolkit();
            _defaultIconImage = tk.createImage(url);
        }

        return _defaultIconImage;
    }

    /** Get the name of this object, which in this class is the URI
     *  associated with the effigy, or the string "Unnamed" if none.
     *  This overrides the base class to provide a reasonable name
     *  for the title of the window.
     *  @return The name.
     */
    protected String _getName() {
        Effigy effigy = getEffigy();

        if (effigy != null) {
            URI uri = effigy.uri.getURI();

            if (uri != null) {
                return uri.toString();
            }
        }

        return "Unnamed";
    }

    /** Display the help file given by the configuration, or if there is
     *  none, then the file specified by the public variable helpFile.
     *  To specify a default help file in the configuration, create
     *  a FileParameter named "_help" whose value is the name of the
     *  file.  If the specified file fails to open, then invoke the
     *  _about() method.
     *  @see FileParameter
     */
    protected void _help() {
        try {
            Configuration configuration = getConfiguration();
            FileParameter helpAttribute = (FileParameter) configuration
                    .getAttribute("_help", FileParameter.class);
            URL doc;

            if (helpAttribute != null) {
                doc = helpAttribute.asURL();
            } else {
                doc = getClass().getClassLoader().getResource(helpFile);
            }

            configuration.openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            _about();
        }
    }

    /** Read the specified URL.  This delegates to the ModelDirectory
     *  to ensure that the preferred tableau of the model is opened, and
     *  that a model is not opened more than once.
     *  @param url The URL to read.
     *  @exception Exception If the URL cannot be read, or if there is no
     *   tableau.
     */
    protected void _read(URL url) throws Exception {
        if (_tableau == null) {
            throw new Exception("No associated Tableau!"
                    + " Can't open a file.");
        }

        // NOTE: Used to use for the first argument the following, but
        // it seems to not work for relative file references:
        // new URL("file", null, _directory.getAbsolutePath()
        Nameable configuration = _tableau.toplevel();

        if (configuration instanceof Configuration) {
            ((Configuration) configuration).openModel(url, url, url
                    .toExternalForm());
        } else {
            throw new InternalErrorException(
                    "Expected top-level to be a Configuration: "
                            + _tableau.toplevel().getFullName());
        }
    }

    /** Save the model to the current file, determined by the
     *  <i>uri</i> parameter of the associated effigy, or if
     *  that has not been set or is not a writable file, or if the
     *  effigy has been set non-modifiable, then invoke
     *  _saveAs(). This calls _writeFile() to perform the save.
     *  @return True if the save succeeds.
     */
    protected boolean _save() {
        if (_tableau == null) {
            throw new InternalErrorException(
                    "No associated Tableau! Can't save.");
        }

        Effigy effigy = getEffigy();
        File file = effigy.getWritableFile();

        if (( /*(effigy != null) && */!effigy.isModifiable())
                || (file == null)) {
            return _saveAs();
        } else {
            try {
                _writeFile(file);
                setModified(false);
                return true;
            } catch (IOException ex) {
                report("Error writing file", ex);
                return false;
            }
        }
    }

    /** Query the user for a filename, save the model to that file,
     *  and open a new window to view the model.
     *  This overrides the base class to update the entry in the
     *  ModelDirectory and to rename the model to match the file name.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
        return _saveAs(null);
    }

    /** Query the user for a filename, save the model to that file,
     *  and open a new window to view the model.
     *  This overrides the base class to update the entry in the
     *  ModelDirectory and to rename the model to match the file name.
     *  @param extension If non-null, then the extension that is
     *  appended to the file name if there is no extension.
     *
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs(String extension) {
        if (_tableau == null) {
            throw new InternalErrorException(
                    "No associated Tableau! Can't save.");
        }

        // Use the strategy pattern here to create the actual
        // dialog so that subclasses can customize this dialog.
        JFileChooser fileDialog = _saveAsFileDialog();
        if (_initialSaveAsFileName != null) {
            fileDialog.setSelectedFile(new File(fileDialog
                    .getCurrentDirectory(), _initialSaveAsFileName));
        }

        // Show the dialog.
        int returnVal = fileDialog.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileDialog.getSelectedFile();
            if (extension != null && file.getName().indexOf(".") == -1) {
                // if the user has not given the file an extension, add it
                file = new File(file.getAbsolutePath() + extension);
            }

            try {
                if (!_confirmFile(null, file)) {
                    return false;
                }

                URL newURL = file.toURI().toURL();
                String newKey = newURL.toExternalForm();

                _directory = fileDialog.getCurrentDirectory();
                _writeFile(file);

                // The original file will still be open, and has not
                // been saved, so we do not change its modified status.
                // setModified(false);
                // Open a new window on the model.
                getConfiguration().openModel(newURL, newURL, newKey);

                // If the tableau was unnamed before, then we need
                // to close this window after doing the save.
                Effigy effigy = getEffigy();

                if (effigy != null) {
                    String id = effigy.identifier.getExpression();

                    if (id.equals("Unnamed")) {
                        // This will have the effect of closing all the
                        // tableaux associated with the unnamed model.
                        effigy.setContainer(null);
                    }
                }

                return true;
            } catch (Exception ex) {
                report("Error in save as.", ex);
                return false;
            }
        }

        // FIXME: Is this right? Presumably the user hit cancel...
        return true;
    }

    /** Write the model to the specified file.  This method delegates
     *  to the effigy containing the associated Tableau, if there
     *  is one, and otherwise throws an exception.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        Tableau tableau = getTableau();

        if (tableau != null) {
            Effigy effigy = (Effigy) tableau.getContainer();

            if (effigy != null) {
                // Ensure that if we do ever try to call this method,
                // that it is the top effigy that is written.
                effigy.writeFile(file);
                return;
            }
        }

        throw new IOException("Cannot find an effigy to delegate writing.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The initial filename to use in the SaveAs dialog. */
    protected String _initialSaveAsFileName = null;

    /** The view menu. Note that this is only created if there are multiple
     *  views, so if derived classes use it, they must test to see whether
     *  it is null.
     */
    protected JMenu _viewMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the model has children, then issue a warning that those
     *  children will persist in modified form.  Give the user the
     *  chance to cancel.
     *  @return False if there are children and
     *   the user cancels. True otherwise.
     */
    private boolean _checkForDerivedObjects() {
        Effigy effigy = getEffigy();

        if (effigy instanceof PtolemyEffigy) {
            NamedObj model = ((PtolemyEffigy) effigy).getModel();

            if (model instanceof Instantiable) {
                // NOTE: We are assuming that only the top-level
                // can defer outside the model. This is currently
                // true. Will it always be true?
                List children = ((Instantiable) model).getChildren();

                if ((children != null) && (children.size() > 0)) {
                    StringBuffer confirm = new StringBuffer(
                            "Warning: This model defines a class, "
                                    + "and there are open models with instances\n"
                                    + "or subclasses the modified version of "
                                    + "this model:\n");
                    Iterator instances = children.iterator();
                    int length = confirm.length();

                    while (instances.hasNext()) {
                        WeakReference reference = (WeakReference) instances
                                .next();
                        Instantiable instance = (Instantiable) reference.get();

                        if (instance != null) {
                            confirm.append(instance.getFullName());
                            confirm.append(";");

                            int newLength = confirm.length();

                            if ((newLength - length) > 50) {
                                confirm.append("\n");
                                length = confirm.length();
                            }
                        }
                    }

                    confirm.append("\nContinue?");

                    // Show a MODAL dialog
                    int selected = JOptionPane.showOptionDialog(this, confirm,
                            "Warning: Instances or Subclasses",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, null, null);

                    if (selected == 1) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The container of view factories, if one has been found.
    private TableauFactory _factoryContainer = null;

    // The tableau that created this frame.
    private Tableau _tableau = null;

    // The singleton icon image used for all ptolemy frames.
    private static Image _defaultIconImage = null;

    /** Associated placeable. */
    private Placeable _placeable;

    /** Set to true when the pack() method is called.  Used by TopPack.pack(). */
    private boolean _packCalled = false;

    /** Set in pack() if an alternate topPack is used. */
    private TopPack _topPack = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** File filter that filters out files that do not have one of a
     *  pre-specified list of extensions.
     */
    protected static class ExtensionFileFilter extends FileFilter {
        // NetBeans wants this protected.  If it is package visibility,
        // then there are problems accessing it from the same package
        // but a different jar.

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct a file filter that filters out all files that do
         *  not have one of the extensions in the given list.
         *  @param extensions A list of extensions, each of which is
         *   a String.
         */
        public ExtensionFileFilter(List extensions) {
            _extensions = extensions;
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Accept only files with one of the extensions given in the
         *  constructor.
         *  @param fileOrDirectory The file to be checked.
         *  @return True if the file is a directory or has one of the
         *   specified extensions.
         */
        public boolean accept(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory()) {
                return true;
            }

            String fileOrDirectoryName = fileOrDirectory.getName();
            int dotIndex = fileOrDirectoryName.lastIndexOf('.');

            if (dotIndex == -1) {
                return false;
            }

            String extension = fileOrDirectoryName.substring(dotIndex + 1);

            if (extension != null) {
                Iterator extensions = _extensions.iterator();

                while (extensions.hasNext()) {
                    String matchExtension = (String) extensions.next();

                    if (extension.equalsIgnoreCase(matchExtension)) {
                        return true;
                    }
                }
            }

            return false;
        }

        /**  The description of this filter. */
        public String getDescription() {
            StringBuffer result = new StringBuffer();
            Iterator extensions = _extensions.iterator();
            int extensionNumber = 1;
            int size = _extensions.size();

            while (extensions.hasNext()) {
                String extension = (String) extensions.next();
                result.append(".");
                result.append(extension);

                if (extensionNumber < (size - 1)) {
                    result.append(", ");
                } else if (extensionNumber < size) {
                    result.append(" and ");
                }

                extensionNumber++;
            }

            result.append(" files");
            return result.toString();
        }

        ///////////////////////////////////////////////////////////////
        ////                     private variables                 ////
        // The list of acceptable file extensions.
        private List _extensions;
    }

    /** Listener for view menu commands. */
    class ViewMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Make this the default context for modal messages.
            GraphicalMessageHandler.setContext(TableauFrame.this);
            if (_factoryContainer != null) {
                JMenuItem target = (JMenuItem) e.getSource();
                String actionCommand = null;
                Action action = target.getAction();
                if (action != null) {

                    //the following should be OK because
                    //GUIUtilities.addMenuItem() automatically adds
                    //each incoming JMenuItems as a property of the
                    //Action itself - see
                    //diva.gui.GUIUtilities.addMenuItem(), line 202,
                    //ans so does kepler/src/exp/ptolemy/vergil/basic/
                    //BasicGraphFrame.storeSubMenus() line 2519...

                    JMenuItem originalMenuItem = (JMenuItem) action
                            .getValue("menuItem");
                    if (originalMenuItem != null) {
                        actionCommand = originalMenuItem.getActionCommand();
                    } else {
                        actionCommand = target.getActionCommand();
                    }
                } else {
                    actionCommand = target.getActionCommand();
                }
                TableauFactory factory = (TableauFactory) _factoryContainer
                        .getAttribute(actionCommand);
                if (factory != null) {
                    Effigy tableauContainer = (Effigy) _tableau.getContainer();

                    try {
                        Tableau tableau = factory
                                .createTableau(tableauContainer);
                        tableau.show();
                    } catch (Throwable throwable) {
                        // Copernicus might throw a java.lang.Error if
                        // jhdl.Main cannot be resolved
                        MessageHandler.error("Cannot create view", throwable);
                    }
                }
            }

            // NOTE: The following should not be needed, but jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }
}
