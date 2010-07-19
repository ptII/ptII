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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.PTDBSearchAttribute;
import ptdb.common.dto.PTDBSearchComponentEntity;
import ptdb.common.dto.SearchCriteria;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.AtomicActorMatcher;
import ptolemy.actor.gt.AttributeMatcher;
import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.FSMMatcher;
import ptolemy.actor.gt.GTIngredient;
import ptolemy.actor.gt.GTIngredientsAttribute;
import ptolemy.actor.gt.MalformedStringException;
import ptolemy.actor.gt.ModalModelMatcher;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gt.ingredients.criteria.AttributeCriterion;
import ptolemy.actor.gt.ingredients.criteria.DynamicNameCriterion;
import ptolemy.actor.gt.ingredients.criteria.PortCriterion;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.ptalon.gt.PtalonMatcher;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.gt.TransformationEditor;

import ptolemy.vergil.toolbox.FigureAction;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// GraphPatternSearchEditor

/**
 * The UI frame for the advanced DB search window. It has the specific 
 * searching features for PTDB.
 *
 * @author Alek Wang
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class GraphPatternSearchEditor extends TransformationEditor {

    //    /**
    //     * Construct the GraphPatternSearchEditor.
    //     *
    //     * @param entity  The model to put in this frame.
    //     * @param tableau The tableau responsible for this frame.
    //     * @param containerModel The model that will import the searched results. 
    //     * @param sourceFrame The frame that contains the model to import the 
    //     * searched results. 
    //     */
    //    public GraphPatternSearchEditor(CompositeEntity entity, Tableau tableau,
    //            NamedObj containerModel, JFrame sourceFrame) {
    //        
    //        this(entity,tableau, null, containerModel,sourceFrame);
    //
    //    }

    //    /**
    //     * Construct the GraphPatternSearchEditor.
    //     *
    //     * @param entity The model to put in this frame.
    //     * @param tableau The tableau responsible for this frame.
    //     * @param defaultLibrary An attribute specifying the default library to
    //     *   use if the model does not have a library.
    //     * @param containerModel The model that will import the searched results. 
    //     * @param sourceFrame The frame that contains the model to import the 
    //     * searched results. 
    //     */
    //    public GraphPatternSearchEditor(CompositeEntity entity, Tableau tableau,
    //            LibraryAttribute defaultLibrary, NamedObj containerModel,
    //            JFrame sourceFrame) {
    //        super(entity, tableau, defaultLibrary);
    //
    //        setTitle("Database Pattern Search");
    //
    //        _containerModel = containerModel;
    //        _sourceFrame = sourceFrame;
    //        
    //        setDefaultCloseOperation(HIDE_ON_CLOSE);
    //        
    //    }

    /**
     * Construct the GraphPatternSearchEditor.
     *
     * @param entity The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     * @param defaultLibrary An attribute specifying the default library to
     *   use if the model does not have a library.
     * @param containerModel The model that will import the searched results. 
     * @param sourceFrame The frame that contains the model to import the 
     * searched results. 
     * @param simpleSearchFrame The Simple Search Frame instance that opens 
     * this frame. 
     */
    public GraphPatternSearchEditor(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary, NamedObj containerModel,
            JFrame sourceFrame, SimpleSearchFrame simpleSearchFrame) {

        //        this(entity,tableau,defaultLibrary,containerModel,sourceFrame);

        super(entity, tableau, defaultLibrary);

        setTitle("Database Pattern Search");

        _containerModel = containerModel;
        _sourceFrame = sourceFrame;

        setDefaultCloseOperation(HIDE_ON_CLOSE);

        _simpleSearchFrame = simpleSearchFrame;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Fetch the search criteria of graph pattern specified in this frame. 
     * 
     * @param searchCriteria The search criteria object that to be set with 
     * the graph pattern criteria information. 
     * @exception NameDuplicationException Thrown if there is duplicated names 
     * of the attributes. 
     * @exception IllegalActionException Thrown if there is illegal action in 
     * setting the attribute name. 
     * @exception MalformedStringException Thrown if error occurs while parsing
     *  the expression.
     */
    public void fetchSearchCriteria(SearchCriteria searchCriteria)
            throws IllegalActionException, NameDuplicationException,
            MalformedStringException {

        // Get the pattern specified by the user.
        TransformationRule rule = getFrameController().getTransformationRule();
        Pattern pattern = rule.getPattern();

        // Get or create the attributes list.
        ArrayList<Attribute> attributesList;
        if (searchCriteria.getAttributes() == null) {
            attributesList = new ArrayList<Attribute>();
        } else {
            attributesList = searchCriteria.getAttributes();
        }

        // Get the attributes from the pattern and add to the list. 
        List<NamedObj> attributes = pattern.attributeList();

        for (NamedObj attribute : attributes) {

            // Fetch the attribute criteria specified in the AttributeMatcher.
            // Since the database can only search on the attribute name, only 
            // search on these parts. 
            if (attribute instanceof AttributeMatcher) {
                GTIngredientsAttribute gtIngredientsAttribute = ((AttributeMatcher) attribute)
                        .getCriteriaAttribute();
                if (gtIngredientsAttribute != null) {

                    for (GTIngredient gtIngredient : gtIngredientsAttribute
                            .getIngredientList()) {

                        if ((gtIngredient instanceof DynamicNameCriterion)
                                && (!gtIngredient.getValue(0).toString().trim()
                                        .isEmpty())) {

                            PTDBSearchAttribute gtIngredientAttribute = new PTDBSearchAttribute();

                            gtIngredientAttribute.setName(gtIngredient
                                    .getValue(0).toString());

                            attributesList.add(gtIngredientAttribute);
                        }

                    }
                }
                // The AttributeMatcher itself should not be added as an 
                // attribute, so just skip it. 
                continue;

            }

            if (attribute instanceof Variable) {
                attributesList.add((Variable) attribute);
            }
        }

        // Create the graph pattern search criteria.
        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        // Get the ports specified by the user.
        List<Port> portsList = pattern.portList();
        ArrayList<Port> ports = new ArrayList<Port>();

        for (Iterator iterator = portsList.iterator(); iterator.hasNext();) {
            Port port = (Port) iterator.next();
            ports.add(port);
        }

        // Get the relations specified by the user.
        List<Relation> relationsList = pattern.relationList();
        ArrayList<Relation> relations = new ArrayList<Relation>();

        for (Iterator iterator = relationsList.iterator(); iterator.hasNext();) {
            Relation relation = (Relation) iterator.next();
            relations.add(relation);
        }

        dbGraphSearchCriteria.setRelationsList(relations);

        // Get the component and composite entities specified by the user.
        ArrayList<ComponentEntity> componentEntities = new ArrayList<ComponentEntity>();

        ArrayList<CompositeEntity> compositeEntities = new ArrayList<CompositeEntity>();

        for (Iterator iterator = pattern.entityList().iterator(); iterator
                .hasNext();) {
            Entity entity = (Entity) iterator.next();

            if (entity instanceof CompositeEntity) {
                if ((entity instanceof CompositeActorMatcher)
                        || (entity instanceof FSMMatcher)
                        || (entity instanceof ModalModelMatcher)
                        || (entity instanceof PtalonMatcher)) {
                    // Skip the CompositeActorMatcher.
                    continue;
                }

                compositeEntities.add((CompositeEntity) entity);

            } else if (entity instanceof ComponentEntity) {

                // Fetch the information from the AtomicActorMatcher. 
                if (entity instanceof AtomicActorMatcher) {
                    GTIngredientsAttribute gtIngredientsAttribute = ((AtomicActorMatcher) entity)
                            .getCriteriaAttribute();

                    for (GTIngredient gtIngredient : gtIngredientsAttribute
                            .getIngredientList()) {
                        if ((gtIngredient instanceof DynamicNameCriterion)
                                && (!((DynamicNameCriterion) gtIngredient)
                                        .getValue(0).toString().trim()
                                        .isEmpty())) {

                            PTDBSearchComponentEntity ptdbSearchComponentEntity = new PTDBSearchComponentEntity();
                            ptdbSearchComponentEntity
                                    .setName(((DynamicNameCriterion) gtIngredient)
                                            .getValue(0).toString());
                            componentEntities.add(ptdbSearchComponentEntity);

                        }
                    }
                    // Skip adding the AtomicActorMatching to the component 
                    // entities. 
                    continue;
                }

                componentEntities.add((ComponentEntity) entity);
            }

        }

        // The pattern GTIngredients attributes from the configure window. 
        GTIngredientsAttribute patternGtIngredientsAttribute = pattern
                .getCriteriaAttribute();

        if (patternGtIngredientsAttribute != null) {
            for (GTIngredient gtIngredient : patternGtIngredientsAttribute
                    .getIngredientList()) {

                if (gtIngredient instanceof AttributeCriterion) {

                    if (!((AttributeCriterion) gtIngredient).getValue(0)
                            .toString().trim().isEmpty()) {
                        PTDBSearchAttribute ptdbSearchAttribute = new PTDBSearchAttribute();
                        ptdbSearchAttribute
                                .setName(((AttributeCriterion) gtIngredient)
                                        .getValue(0).toString());

                        if (((AttributeCriterion) gtIngredient)
                                .isAttributeValueEnabled()) {
                            ptdbSearchAttribute
                                    .setToken(((AttributeCriterion) gtIngredient)
                                            .getValue(2).toString());
                        }

                        attributesList.add(ptdbSearchAttribute);
                    }

                } else if (gtIngredient instanceof DynamicNameCriterion) {

                    if (searchCriteria.getModelName() == null
                            || searchCriteria.getModelName().trim().isEmpty()) {

                        Object nameObject = ((DynamicNameCriterion) gtIngredient)
                                .getValue(0);

                        if (nameObject != null
                                && !nameObject.toString().trim().isEmpty()) {
                            searchCriteria.setModelName(nameObject.toString());
                        }

                    }

                } else if (gtIngredient instanceof PortCriterion) {
                    TypedIOPort typedIOPort = new TypedIOPort();

                    PortCriterion portCriterion = (PortCriterion) gtIngredient;

                    if (portCriterion.isInputEnabled()) {
                        typedIOPort.setInput(portCriterion.isInput());
                    }

                    if (portCriterion.isOutputEnabled()) {
                        typedIOPort.setOutput(portCriterion.isOutput());
                    }

                    if (portCriterion.isMultiportEnabled()) {
                        typedIOPort.setMultiport(portCriterion.isMultiport());
                    }

                    if (portCriterion.isPortNameEnabled()) {
                        typedIOPort.setName(portCriterion.getPortName());
                    }

                    ports.add(typedIOPort);
                }

            }
        }

        // Set the attributes to the search criteria accordingly.
        searchCriteria.setAttributes(attributesList);

        dbGraphSearchCriteria.setPortsList(ports);
        dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);
        dbGraphSearchCriteria.setCompositeEntities(compositeEntities);
        dbGraphSearchCriteria.setPattern(pattern);

        // Set the DBGraph search criteria to the whole search criteria.
        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected void _addMenus() {

        super._addMenus();

        // Remove the unused match actions from the rule menu. 
        _ruleMenu.remove(0);
        _ruleMenu.remove(0);
        _ruleMenu.remove(0);

        // Remove the full screen from the view menu.
        _viewMenu.remove(4);
        _viewMenu.remove(4);

        // Remove the fullscreen and open container tool bar icon. 
        _toolbar.remove(6);
        _toolbar.remove(6);

        // Remove the match actions icons from the tool bar. 
        _toolbar.remove(_toolbar.getComponentCount() - 1);
        _toolbar.remove(_toolbar.getComponentCount() - 1);

        // Add the menu of searching in the database.
        DBMatchAction dbMatchAction = new DBMatchAction();
        GUIUtilities.addToolBarButton(_toolbar, dbMatchAction);

        //        // Add the menu of opening simple search window. 
        //        SimpleSearchAction simpleSearchAction = new SimpleSearchAction();
        //        GUIUtilities.addToolBarButton(_toolbar, simpleSearchAction);

        // Remove the replacement and correspondence tabs.
        JTabbedPane tabbedPane = getFrameController().getTabbedPane();
        tabbedPane.remove(1);
        tabbedPane.remove(1);

    }

    //
    //    @Override
    //    protected RunnableGraphController _createActorGraphController() {
    //        return new DBSearchFrameController();
    //
    //    }

    /**
     * Close the pattern search window without asking anything. 
     * 
     * <p>When the user is closing this window, it just hides for the simple
     * search frame. So this does not require to ask whether the user wants to
     * save or not.</p>
     * 
     * @return true to close the window. 
     */
    @Override
    protected boolean _close() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected classes                 ////

    //    /**
    //     * Controller for this frame.
    //     */
    //    protected class DBSearchFrameController extends
    //            TransformationActorGraphController {
    //
    //        protected DBSearchFrameController() {
    //            super();
    //        }
    //
    //        protected void initializeInteraction() {
    //            super.initializeInteraction();
    //            Action oldConfigureAction = _configureAction;
    //            _configureAction = new DBSearchConfigureAction("Configure");
    //            _configureMenuFactory.substitute(oldConfigureAction,
    //                    _configureAction);
    //            _configureMenuFactory
    //                    .addMenuItemListener(GraphPatternSearchEditor.this);
    //        }
    //
    //    }

    ///////////////////////////////////////////////////////////////////
    ////                private classes                            ////

    private class DBMatchAction extends FigureAction {

        public DBMatchAction() {
            super("Match Model");

            GUIUtilities.addIcons(this,
                    new String[][] {
                            { "/ptdb/gui/img/database.gif",
                                    GUIUtilities.LARGE_ICON },
                            { "/ptdb/gui/img/database.gif",
                                    GUIUtilities.ROLLOVER_ICON },
                            { "/ptdb/gui/img/database.gif",
                                    GUIUtilities.ROLLOVER_SELECTED_ICON },
                            { "/ptdb/gui/img/database.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Search Ptolemy models in Database"
                    + "(Ctrl+1)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_1, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        //////////////////////////////////////////////////////////////////////
        ////                public  methods                               ////

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            // Perform the clicking of the search button from the simple
            // search frame to perform the search. 
            _simpleSearchFrame.clickSearchButton(e);

        }

    }

    //
    //    private static class DBSearchConfigureAction extends ConfigureAction {
    //
    //        public DBSearchConfigureAction(String description) {
    //            super(description);
    //        }
    //
    //        protected void _openDialog(Frame parent, NamedObj target,
    //                ActionEvent event) {
    //            JOptionPane.showMessageDialog(new Frame(), "Under construction.");
    //        }
    //    }

    //    private class SimpleSearchAction extends FigureAction {
    //
    //        public SimpleSearchAction() {
    //            super("Simple Search Configure");
    //
    //            GUIUtilities.addIcons(this, new String[][] {
    //                    { "/ptdb/gui/img/simplesearchfigure.gif",
    //                            GUIUtilities.LARGE_ICON },
    //                    { "/ptdb/gui/img/simplesearchfigure.gif",
    //                            GUIUtilities.ROLLOVER_ICON },
    //                    { "/ptdb/gui/img/simplesearchfigure.gif",
    //                            GUIUtilities.ROLLOVER_SELECTED_ICON },
    //                    { "/ptdb/gui/img/simplesearchfigure.gif",
    //                            GUIUtilities.SELECTED_ICON } });
    //
    //            putValue("tooltip", "Configure Simple Search Criteria" + "(Ctrl+2)");
    //            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
    //                    KeyEvent.VK_2, Toolkit.getDefaultToolkit()
    //                            .getMenuShortcutKeyMask()));
    //        }
    //
    //        //////////////////////////////////////////////////////////////////////
    //        ////                public  methods                               ////
    //
    //        public void actionPerformed(ActionEvent e) {
    //            super.actionPerformed(e);
    //
    //            if (_simpleSearchFrame == null) {
    //                _simpleSearchFrame = new AdvancedSimpleSearchFrame(
    //                        GraphPatternSearchEditor.this);
    //            }
    //
    //            _simpleSearchFrame.pack();
    //            _simpleSearchFrame.setVisible(true);
    //
    //        }
    //
    //    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private NamedObj _containerModel;
    private SimpleSearchFrame _simpleSearchFrame;
    private JFrame _sourceFrame;

}