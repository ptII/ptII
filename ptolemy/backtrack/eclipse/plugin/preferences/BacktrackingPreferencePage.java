/* The class to create the backtracking preference page.

 Copyright (c) 2005-2007 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.plugin.preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ptolemy.backtrack.eclipse.plugin.widgets.DirectoryFieldEditor;
import ptolemy.backtrack.eclipse.plugin.widgets.SaveFileFieldEditor;
import ptolemy.backtrack.util.Strings;

//////////////////////////////////////////////////////////////////////////
//// BacktrackingPreferencePage

/**
 The class to create the backtracking preference page.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class BacktrackingPreferencePage extends SectionPreferencePage implements
        IWorkbenchPreferencePage {

    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct a backtracking preference page.
     */
    public BacktrackingPreferencePage() {
        super("Ptolemy II backtracking settings.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Create the contents of the preference page with the parent as its
     *  container.
     *
     *  @param parent The parent container.
     *  @return The parent itself.
     */
    public Control createContents(Composite parent) {
        super.createContents(parent);

        _createSection1();
        _createSection2();
        _createSection3();
        _createSection4();
        _createSection5();

        initialize();
        checkState();

        return parent;
    }

    /** Set whether the backtracking preference page is visible.
     *
     *  @param visible Whether the backtracking preference page is visible.
     */
    public void setVisible(boolean visible) {
        if (visible) {
            _updateSources();
        }

        super.setVisible(visible);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////

    /** Create the first section named "Sources for Batch Refactoring" in the
     *  preference page.
     */
    private void _createSection1() {
        Composite composite = _createSection(
                "Sources for Batch Refactoring",
                "Set the sources of refactoring. A source list file stores "
                        + "the complete list of Java source files to be refactored. "
                        + "A single Java source file name is written on each line of "
                        + "the source list. The source file names may use paths "
                        + "relative to the path where the source list file is in.");

        Composite currentComposite = _newComposite(composite);
        _sourceList = new FileFieldEditor(
                PreferenceConstants.BACKTRACK_SOURCE_LIST,
                "Source &list file:", currentComposite) {
            protected boolean checkState() {
                String currentValue = getStringValue();
                boolean superResult = super.checkState();

                if (!getTextControl(_getParent(this)).isEnabled()) {
                    return superResult;
                }

                if (getStringValue().equals(_lastString)) {
                    if (superResult) {
                        return true;
                    }
                } else {
                    _lastString = currentValue;
                    _sourcesModified = false;

                    if (superResult || currentValue.equals("")) {
                        return _updateSources();
                    }
                }

                List list = _sources.getListControl(_getParent(_sources));
                list.removeAll();
                return false;
            }

            private String _lastString = null;
        };

        GridData gridData = new GridData();
        gridData.widthHint = 0;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        _sourceList.getTextControl(currentComposite).setLayoutData(gridData);
        _sourceList.setFileExtensions(new String[] { "*.lst", "*.*" });
        _setParent(_sourceList, currentComposite);
        addField(_sourceList);

        currentComposite = _newComposite(composite);
        _sources = new ListEditor(PreferenceConstants.BACKTRACK_SOURCES,
                "&Sources", currentComposite) {
            protected String createList(String[] items) {
                return Strings.encodeFileNames(items);
            }

            protected void doStore() {
                if (_sourcesModified) {
                    List list = getListControl(_getParent(this));
                    String[] items = list.getItems();
                    String fileName = _sourceList.getStringValue();

                    try {
                        PrintWriter writer = new PrintWriter(
                                new FileOutputStream(fileName));

                        for (int i = 0; i < items.length; i++) {
                            writer.write(items[i] + "\n");
                        }

                        writer.close();
                    } catch (Exception e) {
                        MessageDialog.openError(getShell(),
                                "Error writing file", e.getMessage());
                    }
                }
            }

            protected String getNewInputObject() {
                String sourceList = _sourceList.getStringValue();
                String sourceListPath = new File(sourceList).getParent();
                FileDialog dialog = new FileDialog(getShell());
                dialog
                        .setText("Please choose a file to be added to the source list");
                dialog.setFilterPath(sourceListPath);
                dialog.setFilterExtensions(new String[] { "*.java" });

                String file = dialog.open();

                if (file != null) {
                    file = file.trim();

                    if (file.length() == 0) {
                        return null;
                    }
                }

                return file;
            }

            protected String[] parseString(String stringList) {
                return Strings.decodeFileNames(stringList);
            }
        };
        gridData = new GridData();
        gridData.widthHint = 0;
        gridData.heightHint = _LIST_HEIGHT;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        _sources.getListControl(currentComposite).setLayoutData(gridData);
        _setParent(_sources, currentComposite);
        addField(_sources);
    }

    /** Create the second section named "Location" in the preference page.
     */
    private void _createSection2() {
        Composite composite = _createSection(
                "Location",
                "Set the location to store the refactored Java code. The "
                        + "location of the output files is defined by the root of the "
                        + "classes, and packages where the classes are in. A prefix "
                        + "may be added to existing package declarations at the time "
                        + "of refactoring.");

        Composite currentComposite = _newComposite(composite);
        _root = new DirectoryFieldEditor(PreferenceConstants.BACKTRACK_ROOT,
                "&Root of refactoring:", currentComposite, true);

        GridData gridData = new GridData();
        gridData.widthHint = 0;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        _root.getTextControl(currentComposite).setLayoutData(gridData);
        _setParent(_root, currentComposite);
        addField(_root);

        currentComposite = _newComposite(composite);
        _prefix = new StringFieldEditor(PreferenceConstants.BACKTRACK_PREFIX,
                "&Extra prefix:", currentComposite);
        _setParent(_prefix, currentComposite);
        addField(_prefix);
    }

    /** Create the third section named "Extra Class Paths" in the preference
     *  page.
     */
    private void _createSection3() {
        Composite composite = _createSection(
                "Extra Class Paths",
                "Add class paths to locate classes in name resolving. The "
                        + "directories of the projects in the current workspace are "
                        + "added automatically and need not be specified explicitly.");

        Composite currentComposite = _newComposite(composite);
        _extraClassPaths = new PathEditor(
                PreferenceConstants.BACKTRACK_EXTRA_CLASSPATHS,
                "Extra &classpaths:", "Add a path to the classpaths",
                currentComposite);

        GridData gridData = new GridData();
        gridData.widthHint = 0;
        gridData.heightHint = _LIST_HEIGHT;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        _extraClassPaths.getListControl(currentComposite).setLayoutData(
                gridData);
        _setParent(_extraClassPaths, currentComposite);
        addField(_extraClassPaths);
    }

    /** Create the fourth section called "Actor Library Configuration" in the
     *  preference page.
     */
    private void _createSection4() {
        Composite composite = _createSection(
                "Actor Library Configuration",
                "Set the file name of the XML configuration to be generated."
                        + "This configuration can be linked to the Ptolemy II actor "
                        + "library.");

        Group group = _newGroup(composite, "Configuration");

        Composite currentComposite = _newComposite(group);
        _generateConfiguration = new BooleanFieldEditor(
                PreferenceConstants.BACKTRACK_GENERATE_CONFIGURATION,
                "&Generate configuration", currentComposite) {
            protected void doLoadDefault() {
                super.doLoadDefault();
                _configuration.setEnabled(getBooleanValue(),
                        _getParent(_configuration));
            }

            protected void valueChanged(boolean oldValue, boolean newValue) {
                super.valueChanged(oldValue, newValue);
                _configuration.setEnabled(newValue, _getParent(_configuration));
            }
        };
        _setParent(_generateConfiguration, currentComposite);
        addField(_generateConfiguration);

        currentComposite = _newComposite(group);
        _configuration = new SaveFileFieldEditor(
                PreferenceConstants.BACKTRACK_CONFIGURATION, "&Configuration:",
                currentComposite, true);

        GridData gridData = new GridData();
        gridData.widthHint = 0;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        _configuration.getTextControl(currentComposite).setLayoutData(gridData);
        _configuration.setFileExtensions(new String[] { "*.xml", "*.*" });
        _setParent(_configuration, currentComposite);
        addField(_configuration);
    }

    /** Create the fifth section named "Miscellaneous" in the preference page.
     */
    private void _createSection5() {
        Composite composite = _createSection("Miscellaneous",
                "Set other options.");

        Composite currentComposite = _newComposite(composite);
        _overwrite = new BooleanFieldEditor(
                PreferenceConstants.BACKTRACK_OVERWRITE,
                "&Overwrite existing files", currentComposite);
        _setParent(_overwrite, currentComposite);
        addField(_overwrite);
    }

    /** Update the source files to be refactored in the list.
     *
     *  @return true if the update is successful; false, otherwise.
     */
    private boolean _updateSources() {
        String fileName = _sourceList.getStringValue();
        List list = _sources.getListControl(_getParent(_sources));
        list.removeAll();

        if (fileName.equals("")) {
            return true;
        }

        File sourceListFile = new File(fileName);
        File sourceListPath = sourceListFile.getParentFile();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(fileName)));
            String line;

            while ((line = reader.readLine()) != null) {
                File file = new File(sourceListPath, line);

                if (!file.exists()) {
                    MessageDialog.openError(getShell(), "Error",
                            "Cannot open source file \"" + line + "\".");
                    throw new FileNotFoundException();
                }

                list.add(line);
            }

            reader.close();
            return true;
        } catch (Exception e) {
            list.removeAll();
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** Height of the source list.
     */
    private static final int _LIST_HEIGHT = 100;

    /** Editor for the configuration file to be generated.
     */
    private FileFieldEditor _configuration;

    /** Editor for extra class paths.
     */
    private PathEditor _extraClassPaths;

    /** Check box for whether to generate a configuration.
     */
    private BooleanFieldEditor _generateConfiguration;

    /** Check box for whether to overwrite existing files.
     */
    private BooleanFieldEditor _overwrite;

    /** Editor for the class prefix.
     */
    private StringFieldEditor _prefix;

    /** Selector for the root directory.
     */
    private DirectoryFieldEditor _root;

    /** Editor for the source list file.
     */
    private FileFieldEditor _sourceList;

    /** List of the source files to be refactored.
     */
    private ListEditor _sources;

    /** Whether the sources list file is modified.
     */
    private boolean _sourcesModified = false;
}
