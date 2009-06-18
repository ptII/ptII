/*
 * 
 * Copyright (c) 1997-2009 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 * 
 */

package ptolemy.vergil.properties;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.domains.properties.kernel.ModelAttribute;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

/**
 * A controller that provides binding of an attribute and a refinement model.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class ModelAttributeController extends AttributeController {

    /**
     * Create a model attribute controller associated with the specified graph
     * controller.
     * @param controller The specified graph controller.
     */
    public ModelAttributeController(GraphController controller) {
        this(controller, FULL);
    }

    /**
     * Create a model attribute controller associated with the specified graph
     * controller.
     * @param controller The associated graph controller.
     * @param access The access level.
     */
    public ModelAttributeController(GraphController controller, Access access) {
        super(controller, access);

        _lookInsideAction = new LookInsideAction();
        _menuFactory
                .addMenuItemFactory(new MenuActionFactory(_lookInsideAction));

    }

    /**
     * Add hot keys to the look inside action in the given JGraph. It would be
     * better that this method was added higher in the hierarchy.
     * @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _lookInsideAction);
    }

    /**
     * A factory attribute that creates ModelAttributeControllers.
     */
    public static class Factory extends NodeControllerFactory {

        /**
         * Create a new factory with the specified name and container.
         * @param container The specified container.
         * @param name The specified name.
         * @exception IllegalActionException If the attribute cannot be
         * contained by the proposed container.
         * @exception NameDuplicationException If the container already has an
         * attribute with this name.
         */
        public Factory(NamedObj container, String name)
                throws NameDuplicationException, IllegalActionException {
            super(container, name);
        }

        /**
         * Create a new ModelAttributeController with the specified graph
         * controller.
         * @param controller The specified graph controller.
         * @return A new ModelAttributeController.
         */
        @Override
        public NamedObjController create(GraphController controller) {
            return new ModelAttributeController(controller);
        }

    }

    private static class LookInsideAction extends FigureAction {

        public LookInsideAction() {
            super("Open Model");

            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_L, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            super.actionPerformed(event);

            ModelAttribute attribute = (ModelAttribute) getTarget();
            TableauFrame frame = (TableauFrame) getFrame();
            Configuration configuration = frame.getConfiguration();
            try {
                CompositeEntity model = attribute.getContainedModel();
                configuration.openInstance(model);
            } catch (Exception e) {
                throw new InternalErrorException(null, e, "Unable to create "
                        + "transformation editor for " + attribute.getName());
            }
        }
    }

    private final LookInsideAction _lookInsideAction;
}
