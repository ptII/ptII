/*

@Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.ingredients.criteria.PortCriterion;
import ptolemy.actor.gt.ingredients.criteria.SubclassCriterion;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.EditorIcon;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTEntityUtils {

    public static void exportExtraProperties(GTEntity entity, Writer output,
            int depth) throws IOException {
        if (entity instanceof Entity) {
            Entity ptEntity = (Entity) entity;
            for (Object portObject : ptEntity.portList()) {
                Port port = (Port) portObject;
                if (port.isPersistent()) {
                    continue;
                }
                boolean outputStarted = false;
                for (Object attributeObject : port.attributeList()) {
                    Attribute attribute = (Attribute) attributeObject;
                    if (attribute.isPersistent()) {
                        if (!outputStarted) {
                            output.write(StringUtilities.getIndentPrefix(depth)
                                    + "<port name=\"" + port.getName()
                                    + "\">\n");
                            outputStarted = true;
                        }
                        attribute.exportMoML(output, depth + 1);
                    }
                }
                if (outputStarted) {
                    output.write(StringUtilities.getIndentPrefix(depth)
                            + "</port>\n");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void updateAppearance(final GTEntity entity,
            GTIngredientsAttribute attribute) {

        NamedObj object = (NamedObj) entity;
        Workspace workspace = object.workspace();
        try {
            workspace.getWriteAccess();

            List<?> icons = object.attributeList(EditorIcon.class);
            boolean foundPersistentIcon = false;
            for (Object iconObject : icons) {
                EditorIcon icon = (EditorIcon) iconObject;
                if (icon.isPersistent()) {
                    foundPersistentIcon = true;
                    break;
                }
            }

            Set<String> preservedPortNames = new HashSet<String>();
            boolean isIconSet = false;
            int i = 1;
            GTIngredientList list = attribute.getIngredientList();
            for (GTIngredient ingredient : list) {
                if (ingredient instanceof PortCriterion) {
                    PortCriterion criterion = (PortCriterion) ingredient;
                    String portID = criterion.getPortID(list);
                    preservedPortNames.add(portID);

                    TypedIOPort port = (TypedIOPort)
                            ((ComponentEntity) entity).getPort(portID);
                    boolean isInput = criterion.isInput();
                    boolean isOutput = criterion.isOutput();
                    boolean isMultiport = !criterion.isMultiportEnabled()
                            || criterion.isMultiport();
                    if (port != null) {
                        if (port instanceof PortMatcher) {
                            port.setInput(isInput);
                            port.setOutput(isOutput);
                        } else {
                            MoMLChangeRequest request =
                                new MoMLChangeRequest(entity, object,
                                        "<deletePort name=\"" + port.getName()
                                        + "\"/>");
                            request.setUndoable(true);
                            request.setMergeWithPreviousUndo(true);
                            request.execute();
                            port = new PortMatcher(criterion,
                                    (ComponentEntity) entity, portID, isInput,
                                    isOutput);
                            port.setPersistent(false);
                        }
                    } else {
                        port = new PortMatcher(criterion,
                                (ComponentEntity) entity, portID, isInput,
                                isOutput);
                        port.setPersistent(false);
                    }
                    port.setMultiport(isMultiport);
                } else if (ingredient instanceof SubclassCriterion
                        && !isIconSet && !foundPersistentIcon) {
                    SubclassCriterion criterion =
                        (SubclassCriterion) ingredient;
                    final String superclass = criterion.getSuperclass();
                    object.requestChange(new ChangeRequest(entity,
                            "Deferred load actor icon action.") {
                        protected void _execute() throws Exception {
                            _loadActorIcon(entity, superclass);
                        }
                    });
                    isIconSet = true;
                }
                i++;
            }
            if (!isIconSet && !foundPersistentIcon) {
                object.requestChange(new RestoreAppearanceChangeRequest(
                        entity));
            }

            ComponentEntity component = (ComponentEntity) entity;
            try {
                component.workspace().getReadAccess();
                List<?> portList = new LinkedList<Object>(component.portList());
                for (i = 0; i < portList.size(); i++) {
                    Port port = (Port) portList.get(i);
                    if (port instanceof PortMatcher
                            && !preservedPortNames.contains(port.getName())) {
                        ((PortMatcher) port)._setPortCriterion(null);
                        port.setContainer(null);
                    }
                }
            } finally {
                component.workspace().doneReading();
            }

        } catch (KernelException e) {
            throw new KernelRuntimeException(e, "Cannot update appearance for "
                    + "actor " + entity.getName() + ".");

        } finally {
            workspace.doneWriting();
        }
    }

    public static void valueChanged(GTEntity entity, Settable settable) {
        GTIngredientsAttribute criteria = entity.getCriteriaAttribute();

        if (settable == criteria) {
            if (GTTools.isInPattern((NamedObj) entity)) {
                // criteria attribute is used to set the matching criteria for
                // this actor. It is used only for actors in the pattern of
                // a transformation rule. If the actor is in the
                // replacement, this attribute is ignored.
                entity.updateAppearance(criteria);

                // Update the appearance of corresponding entities in the
                // replacement.
                Pattern pattern = (Pattern) GTTools
                        .getContainingPatternOrReplacement((NamedObj) entity);
                NamedObj container = pattern.getContainer();
                if (container instanceof TransformationRule) {
                    Replacement replacement = ((TransformationRule) container)
                            .getReplacement();
                    replacement.updateEntitiesAppearance(criteria);
                }
            }
        }
    }

    private static void _loadActorIcon(GTEntity entity, String actorClassName)
    throws Exception {
        CompositeActor container = new CompositeActor();
        String moml = "<group><entity name=\"NewActor\" class=\""
                + actorClassName + "\"/></group>";

        boolean isModified = MoMLParser.isModified();
        try {
            new MoMLChangeRequest(entity, container, moml).execute();
            new LoadActorIconChangeRequest(entity, container).execute();
        } catch (Throwable t) {
            if (_removeEditorIcons(entity)) {
                _setIconDescription(entity, entity.getDefaultIconDescription());
            }
        } finally {
            MoMLParser.setModified(isModified);
        }
    }

    private static boolean _removeEditorIcons(GTEntity entity)
    throws KernelException {
        NamedObj object = (NamedObj) entity;
        boolean foundPersistentIcon = false;
        try {
            object.workspace().getReadAccess();
            for (Object iconObject : object.attributeList(EditorIcon.class)) {
                EditorIcon icon = (EditorIcon) iconObject;
                if (icon.isPersistent()) {
                    foundPersistentIcon = true;
                } else {
                    icon.setContainer(null);
                }
            }
        } finally {
            object.workspace().doneReading();
        }
        return !foundPersistentIcon;
    }

    private static void _setIconDescription(GTEntity entity,
            String iconDescription) {
        String moml = "<property name=\"_iconDescription\" class="
                + "\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "  <configure>" + iconDescription + "</configure>"
                + "</property>";
        MoMLChangeRequest request =
            new MoMLChangeRequest(entity, (NamedObj) entity, moml);
        request.execute();
    }

    private static class LoadActorIconChangeRequest extends ChangeRequest {

        public LoadActorIconChangeRequest(GTEntity entity,
                CompositeEntity container) {
            super(container, "Load the icon of the newly created actor");

            _entity = entity;
            _container = container;
        }

        protected void _execute() throws Exception {
            ComponentEntity actor;
            try {
                _container.workspace().getReadAccess();
                actor = (ComponentEntity) _container.entityList().get(0);
            } finally {
                _container.workspace().doneReading();
            }

            if (!_removeEditorIcons(_entity)) {
                return;
            }

            ConfigurableAttribute actorAttribute = (ConfigurableAttribute) actor
                    .getAttribute("_iconDescription");
            String iconDescription = actorAttribute.getConfigureText();
            _setIconDescription(_entity, iconDescription);

            try {
                actor.workspace().getReadAccess();
                List<?> editorIconList = actor.attributeList(EditorIcon.class);
                for (Object editorIconObject : editorIconList) {
                    EditorIcon editorIcon = (EditorIcon) editorIconObject;
                    EditorIcon icon = (EditorIcon) editorIcon.clone(
                            ((NamedObj) _entity).workspace());
                    icon.setName("_icon");
                    EditorIcon oldIcon =
                        (EditorIcon) ((NamedObj) _entity).getAttribute("_icon");
                    if (oldIcon != null) {
                        oldIcon.setContainer(null);
                    }
                    icon.setContainer((NamedObj) _entity);
                    icon.setPersistent(false);
                    break;
                }
            } finally {
                actor.workspace().doneReading();
            }
        }

        private CompositeEntity _container;

        private GTEntity _entity;
    }

    private static class RestoreAppearanceChangeRequest extends ChangeRequest {

        protected void _execute() throws Exception {
            if (_removeEditorIcons(_entity)) {
                _setIconDescription(_entity, _entity
                        .getDefaultIconDescription());
            }
        }

        RestoreAppearanceChangeRequest(GTEntity entity) {
            super(entity, "Restore the default appearance.");
            _entity = entity;
        }

        private GTEntity _entity;
    }
}
