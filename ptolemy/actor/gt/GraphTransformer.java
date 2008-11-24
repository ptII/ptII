/*

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

package ptolemy.actor.gt;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.data.Pair;
import ptolemy.actor.gt.data.SequentialTwoWayHashMap;
import ptolemy.actor.gt.data.TwoWayHashMap;
import ptolemy.actor.gt.ingredients.operations.Operation;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.Vertex;
import ptolemy.vergil.kernel.attributes.VisibleAttribute;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GraphTransformer extends ChangeRequest {

    public GraphTransformer(TransformationRule transformationRule,
            List<MatchResult> matchResults) throws TransformationException {
        super(null, "Apply graph transformation to model.");

        _pattern = transformationRule.getPattern();
        _replacement = transformationRule.getReplacement();
        _matchResults = matchResults;
    }

    public GraphTransformer(TransformationRule transformationRule,
            MatchResult matchResult) throws TransformationException {
        super(null, "Apply graph transformation to model.");

        _pattern = transformationRule.getPattern();
        _replacement = transformationRule.getReplacement();
        _matchResults = new LinkedList<MatchResult>();
        _matchResults.add(matchResult);
    }

    public MatchResult getMatchResult() {
        return _matchResult;
    }

    public Pattern getPattern() {
        return _pattern;
    }

    public Replacement getReplacement() {
        return _replacement;
    }

    public static void startUndoableTransformation(boolean mergeWithPrevious) {
        _undoable = true;
        _mergeWithPrevious = mergeWithPrevious;
    }

    public static void stopUndoableTransformation() {
        _undoable = false;
        _mergeWithPrevious = false;
    }

    public static void transform(TransformationRule transformationRule,
            List<MatchResult> matchResults) throws TransformationException {
        if (matchResults.isEmpty()) {
            return;
        }

        GraphTransformer transformer = new GraphTransformer(transformationRule,
                matchResults);
        MatchResult matchResult = matchResults.get(0);
        NamedObj host = (NamedObj) matchResult.get(transformationRule
                .getPattern());
        if (host == null) {
            throw new TransformationException(
                    "Match result is invalid because "
                            + "it does not include the pattern.");
        }
        host.requestChange(transformer);
    }

    public static void transform(TransformationRule transformationRule,
            MatchResult matchResult) throws TransformationException {
        GraphTransformer transformer = new GraphTransformer(transformationRule,
                matchResult);
        NamedObj host = (NamedObj) matchResult.get(transformationRule
                .getPattern());
        if (host == null) {
            throw new TransformationException("Match result is invalid because "
                    + "it does not include the pattern.");
        }
        host.requestChange(transformer);
    }

    @SuppressWarnings("unchecked")
    protected void _addConnections() throws TransformationException {
        for (NamedObj replacement : _replacementToHost.keySet()) {
            NamedObj host = _replacementToHost.get(replacement);
            List<NamedObj> replacementLinkedList;
            List<NamedObj> hostLinkdList;
            if (host instanceof Port) {
                replacementLinkedList = new LinkedList<NamedObj>(
                        ((Port) replacement).linkedRelationList());
                hostLinkdList = new LinkedList<NamedObj>(
                        ((Port) host).linkedRelationList());
            } else if (host instanceof Relation) {
                replacementLinkedList = new LinkedList<NamedObj>();
                for (Object relation : ((Relation) replacement)
                        .linkedObjectsList()) {
                    if (relation instanceof Relation) {
                        replacementLinkedList.add((Relation) relation);
                    }
                }
                hostLinkdList = new LinkedList<NamedObj>();
                for (Object relation : ((Relation) host).linkedObjectsList()) {
                    if (relation instanceof Relation) {
                        replacementLinkedList.add((Relation) relation);
                    }
                }
            } else {
                continue;
            }

            for (NamedObj replacementLinkedObject : replacementLinkedList) {
                NamedObj hostLinkedObject = _replacementToHost.get(
                        replacementLinkedObject);
                // FIXME: hostRelation shouldn't be null, but it seems if a
                // Publisher appears in the host model, then an extra relation
                // created by it remains after it is deleted, so there is a
                // relation that has no match in the match result.
                // Needs to fix this in ptolemy.actor.lib.Publisher.
                if (hostLinkedObject != null
                        && !hostLinkdList.contains(hostLinkedObject)) {
                    Relation relation = (hostLinkedObject instanceof Relation) ?
                            (Relation) hostLinkedObject : (Relation) host;

                    NamedObj hostContainer = relation.getContainer();
                    String moml;
                    if (relation == hostLinkedObject) {
                        moml = _getLinkMoML(host, relation);
                    } else {
                        moml = _getLinkMoML(hostLinkedObject, relation);
                    }
                    MoMLChangeRequest request = _createChangeRequest(
                            hostContainer, moml);
                    request.execute();
                }
            }

            if (replacement instanceof ComponentPort
                    && host instanceof ComponentPort) {
                ComponentPort replacementComponentPort =
                    (ComponentPort) replacement;
                ComponentPort hostComponentPort = (ComponentPort) host;
                try {
                    replacementComponentPort.workspace().getReadAccess();
                    for (Object replacementRelationObject
                            : replacementComponentPort.insideRelationList()) {
                        Relation replacementRelation =
                            (Relation) replacementRelationObject;
                        Relation hostRelation = (Relation) _replacementToHost
                                .get(replacementRelation);
                        if (!hostComponentPort.insideRelationList().contains(
                                hostRelation)) {
                            // There is no link between hostPort and
                            // hostRelation, so create a new link.
                            NamedObj hostContainer =
                                hostRelation.getContainer();
                            String moml = _getLinkMoML(host, hostRelation);
                            MoMLChangeRequest request = _createChangeRequest(
                                    hostContainer, moml);
                            request.execute();
                        }
                    }
                } finally {
                    replacementComponentPort.workspace().doneReading();
                }
            }
        }
    }

    protected void _addObjects() throws TransformationException {
        _addObjectsWithCreationAttributes(_pattern);
        _addObjects(_replacement, _host, true);
    }

    protected void _execute() throws TransformationException {
        for (MatchResult matchResult : _matchResults) {
            _matchResult = (MatchResult) matchResult.clone();
            _host = (CompositeEntity) _matchResult.get(_pattern);
            if (_host == null) {
                throw new TransformationException("Match result is invalid "
                        + "because it does not include the pattern.");
            }

            Hashtable<ValueIterator, Token> records =
                new Hashtable<ValueIterator, Token>();
            try {
                GTTools.saveValues(_pattern, records);
            } catch (IllegalActionException e) {
                throw new KernelRuntimeException(e, "Unable to save values.");
            }

            _restoreParameterValues();
            _init();
            _recordMoML();
            _addObjects();
            _performOperations();
            _removeLinks();
            _removeObjects();
            _addConnections();
            _wrapup();

            try {
                GTTools.restoreValues(_pattern, records);
            } catch (IllegalActionException e) {
                throw new KernelRuntimeException(e, "Unable to restore values.");
            }
        }

        _hideRelations();
    }

    protected void _hideRelations() throws TransformationException {
        _hideRelations(_host);
    }

    protected void _init() throws TransformationException {
        _patternToReplacement = new TwoWayHashMap<NamedObj, NamedObj>();
        _initPatternToReplacement(_replacement);
        _initReplacementToHost();
        _initReplacementObjectAttributes(_replacement);
        _initPreservedObjects(_pattern);
    }

    protected void _performOperations() throws TransformationException {
        for (Map.Entry<NamedObj, NamedObj> entry : _replacementToHost
                .entrySet()) {
            NamedObj replacement = entry.getKey();
            NamedObj host = entry.getValue();
            NamedObj pattern = _patternToReplacement.getKey(replacement);

            try {
                GTIngredientList ingredientList;
                if (replacement instanceof GTEntity) {
                    ingredientList = ((GTEntity) replacement)
                            .getOperationsAttribute().getIngredientList();
                } else {
                    try {
                        replacement.workspace().getReadAccess();
                        List<?> attributes = replacement.attributeList(
                                GTIngredientsAttribute.class);
                        if (attributes.isEmpty()) {
                            continue;
                        } else {
                            ingredientList = ((GTIngredientsAttribute)
                                    attributes.get(0)).getIngredientList();
                        }
                    } finally {
                        replacement.workspace().doneReading();
                    }
                }
                for (GTIngredient ingredient : ingredientList) {
                    ChangeRequest request;
                    try {
                        request = ((Operation) ingredient).getChangeRequest(
                                _pattern, _replacement, _matchResult, pattern,
                                replacement, host);
                        if (request != null) {
                            request.execute();
                        }
                    } catch (IllegalActionException e) {
                        throw new TransformationException(
                                "Unable to obtain change request.", e);
                    }
                }
            } catch (MalformedStringException e) {
                throw new TransformationException(
                        "Cannot parse operation list.", e);
            }
        }
    }

    protected void _recordMoML() throws TransformationException {
        _moml = new HashMap<NamedObj, String>();
        for (Map.Entry<NamedObj, NamedObj> entry : _replacementToHost
                .entrySet()) {
            NamedObj replacement = entry.getKey();
            NamedObj host = entry.getValue();
            String moml = _getMoML(host);
            _moml.put(replacement, moml);
        }
    }

    protected void _removeLinks() throws TransformationException {
        _removeLinks(_pattern);
    }

    protected void _removeObjects() throws TransformationException {
        _removeObjects(_host);
    }

    protected void _restoreParameterValues() throws TransformationException {
        SequentialTwoWayHashMap<ValueIterator, Token> parameterValues =
            _matchResult.getParameterValues();
        for (ValueIterator key : parameterValues.keys()) {
            try {
                key.setToken(parameterValues.get(key));
                key.validate();
            } catch (IllegalActionException e) {
                throw new TransformationException(
                        "Unable to set parameter value.", e);
            }
        }
    }

    protected void _wrapup() throws TransformationException {
        _removeReplacementObjectAttributes(_host);
        _removeReplacementObjectAttributes(_replacement);
    }

    private void _addObjects(NamedObj replacement, NamedObj host,
            boolean copyAttributes) throws TransformationException {
        try {
            replacement.workspace().getReadAccess();

            if (copyAttributes) {
                // Copy attributes for composite entities.
                if (replacement instanceof CompositeEntity) {
                    for (Object attributeObject : replacement.attributeList()) {
                        Attribute attribute = (Attribute) attributeObject;
                        if (!_isAttributeCopied(attribute)) {
                            continue;
                        }

                        String moml = "<group name=\"auto\">"
                            + attribute.exportMoML() + "</group>";
                        MoMLChangeRequest request = _createChangeRequest(host,
                                moml);
                        request.execute();
                    }
                }
            }

            // Copy entities and relations.
            Collection<?> children = GTTools.getChildren(replacement, false,
                    true, true, true);
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                if (GTTools.isIgnored(child)) {
                    continue;
                }
                NamedObj hostChild = _replacementToHost.get(child);
                String moml = null;
                if (hostChild == null) {
                    moml = _getMoML(child);
                } else {
                    if (hostChild.getContainer() != host) {
                        moml = _moml.get(child);
                    }
                }
                if (moml != null && !moml.equals("")) {
                    moml = "<group name=\"auto\">\n" + moml + "</group>";
                    CreateObjectChangeRequest request =
                        new CreateObjectChangeRequest(host, moml);
                    request.execute();
                    hostChild = request.getCreatedObjects().get(0);
                    _addReplacementToHostEntries(hostChild);
                }
                if (hostChild != null) {
                    _addObjects(child, hostChild, false);
                }
            }
        } finally {
            replacement.workspace().doneReading();
        }
    }

    private void _addObjectsWithCreationAttributes(NamedObj pattern)
    throws TransformationException {
        Collection<?> children = GTTools.getChildren(pattern, false, true, true,
                true);
        for (Object childObject : children) {
            NamedObj child = (NamedObj) childObject;
            if (GTTools.isIgnored(child)) {
                continue;
            }
            if (GTTools.isCreated(child)) {
                String moml = child.exportMoMLPlain();
                NamedObj host = _findChangeContext(pattern);
                moml = "<group name=\"auto\">\n" + moml + "</group>";
                CreateObjectChangeRequest request =
                    new CreateObjectChangeRequest(host, moml);
                request.execute();
                NamedObj hostChild = request.getCreatedObjects().get(0);
                try {
                    GTTools.deepRemoveAttributes(hostChild,
                            MatchingAttribute.class);
                } catch (KernelException e) {
                    throw new TransformationException("Unable to remove " +
                            "matching attributes.", e);
                }
                _recordMirroredObjects(child, hostChild);
            } else {
                _addObjectsWithCreationAttributes(child);
            }
        }
    }

    private void _addReplacementToHostEntries(NamedObj host) {
        ReplacementObjectAttribute attribute = _getReplacementObjectAttribute(
                host);
        if (attribute != null) {
            String replacementCode = attribute.getExpression();
            NamedObj replacement = GTTools.getObjectFromCode(replacementCode,
                    _replacement);
            _replacementToHost.put(replacement, host);
        }

        try {
            host.workspace().getReadAccess();
            Collection<?> children = GTTools.getChildren(host, false, true,
                    true, true);
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                _addReplacementToHostEntries(child);
            }
        } finally {
            host.workspace().doneReading();
        }
    }

    private MoMLChangeRequest _createChangeRequest(NamedObj context,
            String moml) {
        MoMLChangeRequest request = new MoMLChangeRequest(this, context, moml);
        if (_undoable) {
            request.setUndoable(true);
            if (_mergeWithPrevious) {
                request.setMergeWithPreviousUndo(true);
            } else {
                _mergeWithPrevious = true;
            }
        }
        return request;
    }

    private NamedObj _findChangeContext(NamedObj pattern) {
        Object host = null;
        while (host == null && pattern != null) {
            host = _matchResult.get(pattern);
            pattern = pattern.getContainer();
        }
        return (NamedObj) host;
    }

    private Token _getAttribute(NamedObj container, String name,
            Class<? extends GTAttribute> attributeClass) {

        while (container != null) {
            if (_replacementToHost.containsValue(container)) {
                container = _replacementToHost.getKey(container);
            }
            Attribute attribute = container.getAttribute(name);
            if (attribute != null && attributeClass.isInstance(attribute)) {
                try {
                    attribute.workspace().getReadAccess();
                    Parameter parameter = (Parameter) attribute.attributeList()
                            .get(0);
                    try {
                        return parameter == null ? null : parameter.getToken();
                    } catch (IllegalActionException e) {
                        return null;
                    }
                } finally {
                    attribute.workspace().doneReading();
                }
            }
            container = container.getContainer();
        }

        return null;
    }

    private double[] _getBestLocation(List<?> linkedObjectList) {
        double x = 0;
        double y = 0;
        int i;
        int num = 0;
        for (i = 0; i < linkedObjectList.size(); i++) {
            NamedObj object = (NamedObj) linkedObjectList.get(i);
            if (object instanceof Port) {
                object = object.getContainer();
            }
            Location location = (Location) object.getAttribute("_location");
            if (location != null) {
                double[] coordinate = location.getLocation();
                x += coordinate[0];
                y += coordinate[1];
                num++;
            }
        }
        if (num == 0) {
            num = 1;
        }
        return new double[] { x / num, y / num };
    }

    private String _getLinkMoML(NamedObj object, Relation relation) {
        String moml = null;
        if (object instanceof Port) {
            NamedObj portContainer = object.getContainer();
            moml = "<link port=\"";
            if (portContainer != relation.getContainer()) {
                moml += portContainer.getName() + ".";
            }
            moml += object.getName() + "\" relation=\"" + relation.getName()
                    + "\"/>";
        } else if (object instanceof Relation) {
            moml = "<link relation1=\"" + object.getName() + "\" relation2=\""
                    + relation.getName() + "\"/>";
        }
        return moml;
    }

    private String _getMoML(NamedObj host) {
        if (host instanceof CompositeEntity) {
            CompositeEntity entity = (CompositeEntity) host;
            Set<NamedObj> objectsChanged = new HashSet<NamedObj>();
            Collection<?> children = GTTools.getChildren(host, false, false,
                    true, true);
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                if (child.isPersistent()) {
                    objectsChanged.add(child);
                    child.setPersistent(false);
                }
            }
            String moml = entity.exportMoMLPlain();
            for (NamedObj child : objectsChanged) {
                child.setPersistent(true);
            }
            return moml;
        } else {
            return host.exportMoMLPlain();
        }
    }

    private ReplacementObjectAttribute _getReplacementObjectAttribute(
            NamedObj object) {
        Attribute attribute = object.getAttribute("replacementObject");
        if (attribute instanceof ReplacementObjectAttribute) {
            return (ReplacementObjectAttribute) attribute;
        } else {
            return null;
        }
    }

    private void _hideRelations(CompositeEntity host) {
        try {
            host.workspace().getReadAccess();

            // Remove dangling relations.
            Collection<?> relations = GTTools.getChildren(host, false, false,
                    false, true);
            for (Object relationObject : relations) {
                Relation relation = (Relation) relationObject;
                List<?> linkedObjects = relation.linkedObjectsList();
                if (linkedObjects.size() == 1) {
                    String moml = "<deleteRelation name=\""
                            + relation.getName() + "\"/>";
                    MoMLChangeRequest request = _createChangeRequest(
                            relation.getContainer(), moml);
                    request.execute();
                }
            }

            Token relationHidingAttribute = _getAttribute(host,
                    "RelationHiding", RelationHidingAttribute.class);
            boolean relationHiding = relationHidingAttribute == null ?
                    RelationHidingAttribute.DEFAULT
                    : ((BooleanToken) relationHidingAttribute).booleanValue();

            // Combine relations if possible.
            if (relationHiding) {
                relations = new LinkedList<Object>(GTTools.getChildren(host,
                        false, false, false, true));
                Set<Relation> removed = new HashSet<Relation>();
                for (Object relationObject : relations) {
                    Relation relation = (Relation) relationObject;
                    if (removed.contains(relation)) {
                        continue;
                    }
                    List<?> linkedObjects = relation.linkedObjectsList();
                    for (Object linkedObject : linkedObjects) {
                        if (linkedObject instanceof Relation) {
                            Relation other = (Relation) linkedObject;
                            if (_relink(relation, other)) {
                                removed.add(other);
                            }
                        }
                    }
                }
            }

            relations = GTTools.getChildren(host, false, false, false, true);
            for (Object relationObject : relations) {
                Relation relation = (Relation) relationObject;
                List<?> vertices = relation.attributeList(Vertex.class);
                List<?> linkedObjects = relation.linkedObjectsList();
                if (vertices.isEmpty()) {
                    if (linkedObjects.size() == 2) {
                        NamedObj head = (NamedObj) linkedObjects.get(0);
                        NamedObj tail = (NamedObj) linkedObjects.get(1);
                        if (head instanceof Relation
                                || tail instanceof Relation) {
                            String moml = "<deleteRelation name=\""
                                    + relation.getName() + "\"/>";
                            MoMLChangeRequest request = _createChangeRequest(
                                    relation.getContainer(), moml);
                            request.execute();

                            if (tail instanceof Relation) {
                                moml = _getLinkMoML(head, (Relation) tail);
                                request = _createChangeRequest(
                                        tail.getContainer(), moml);
                                request.execute();
                            } else {
                                moml = _getLinkMoML(tail, (Relation) head);
                                request = _createChangeRequest(
                                        head.getContainer(), moml);
                                request.execute();
                            }
                        }
                    } else if (linkedObjects.size() > 2) {
                        double[] location = _getBestLocation(relation
                                .linkedObjectsList());
                        String moml = "<group name=\"auto\">"
                                + "<vertex name=\"vertex\" value=\"["
                                + location[0] + ", " + location[1] + "]\"/>"
                                + "</group>";
                        MoMLChangeRequest request = _createChangeRequest(
                                relation, moml);
                        request.execute();
                    }
                } else if (relationHiding && linkedObjects.size() == 2) {
                    for (Object vertexObject : vertices) {
                        Vertex vertex = (Vertex) vertexObject;
                        String moml = "<deleteProperty name=\""
                            + vertex.getName() + "\"/>";
                        MoMLChangeRequest request = _createChangeRequest(
                                relation, moml);
                        request.execute();
                    }
                }
            }

            for (Object compositeChild : host.entityList(
                    CompositeEntity.class)) {
                _hideRelations((CompositeEntity) compositeChild);
            }
        } finally {
            host.workspace().doneReading();
        }
    }

    private void _initPatternToReplacement(NamedObj replacement) {
        NamedObj pattern;
        if (replacement == _replacement) {
            pattern = _pattern;
        } else {
            pattern = GTTools.getCorrespondingPatternObject(replacement);
        }
        if (pattern != null) {
            _patternToReplacement.put(pattern, replacement);
            if (pattern instanceof Entity && replacement instanceof Entity) {
                Entity patternEntity = (Entity) pattern;
                Entity replacementEntity = (Entity) replacement;
                try {
                    patternEntity.workspace().getReadAccess();
                    List<?> patternPortList = patternEntity.portList();
                    for (int i = 0; i < patternPortList.size(); i++) {
                        Port patternPort = (Port) patternPortList.get(i);
                        String portName = patternPort.getName();
                        Port replacementPort = replacementEntity.getPort(
                                portName);
                        if (replacementPort != null) {
                            _patternToReplacement.put(patternPort,
                                    replacementPort);
                        }
                    }
                } finally {
                    patternEntity.workspace().doneReading();
                }
            }
        }

        try {
            replacement.workspace().getReadAccess();
            Collection<?> children = GTTools.getChildren(replacement, false,
                    false, true, true);
            for (Object child : children) {
                _initPatternToReplacement((NamedObj) child);
            }
        } finally {
            replacement.workspace().doneReading();
        }
    }

    private void _initPreservedObjects(NamedObj pattern) {
        if (GTTools.isPreserved(pattern)) {
            NamedObj host = (NamedObj) _matchResult.get(pattern);
            if (host != null) {
                _recordMirroredObjects(pattern, host);
            }
        } else {
            for (Object child : GTTools.getChildren(pattern, false, true, true,
                    true)) {
                _initPreservedObjects((NamedObj) child);
            }
        }
    }

    private void _initReplacementObjectAttributes(NamedObj replacement)
            throws TransformationException {
        _setReplacementObjectAttribute(replacement, GTTools.getCodeFromObject(
                replacement, _replacement));

        try {
            replacement.workspace().getReadAccess();
            Collection<?> children = GTTools.getChildren(replacement, false,
                    true, true, true);
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                _initReplacementObjectAttributes(child);
            }
        } finally {
            replacement.workspace().doneReading();
        }
    }

    private void _initReplacementToHost() throws TransformationException {
        _replacementToHost = new TwoWayHashMap<NamedObj, NamedObj>();
        for (Map.Entry<NamedObj, NamedObj> entry : _patternToReplacement.entrySet()) {
            NamedObj pattern = entry.getKey();
            NamedObj replacement = entry.getValue();
            NamedObj host = (NamedObj) _matchResult.get(pattern);
            if (host != null) {
                _replacementToHost.put(replacement, host);
                _setReplacementObjectAttribute(host, GTTools.getCodeFromObject(
                        replacement, _replacement));
            }
        }
        for (Map.Entry<Object, Object> entry : _matchResult.entrySet()) {
            Object patternObject = entry.getKey();
            if (!(patternObject instanceof NamedObj)) {
                continue;
            }
            NamedObj pattern = (NamedObj) patternObject;
            if (!GTTools.isPreserved(pattern)) {
                continue;
            }
            NamedObj host = (NamedObj) entry.getValue();
            _replacementToHost.put(pattern, host);
        }
    }

    private boolean _isAttributeCopied(Attribute attribute) {
        if (!attribute.isPersistent()
                || attribute instanceof GTAttribute) {
            return false;
        }

        if (attribute instanceof Director || attribute instanceof Variable
                || attribute instanceof VisibleAttribute) {
            return true;
        }

        try {
            attribute.workspace().getReadAccess();
            if (!attribute.attributeList(Location.class).isEmpty()) {
                return true;
            }
        } finally {
            attribute.workspace().doneReading();
        }

        return false;
    }

    private void _recordMirroredObjects(NamedObj pattern, NamedObj host) {
        _matchResult.put(pattern, host);
        _replacementToHost.put(pattern, host);
        _patternToReplacement.put(pattern, pattern);
        for (Object child : GTTools.getChildren(pattern, false, true, true,
                true)) {
            if (child instanceof Port) {
                Port port = (Port) child;
                _recordMirroredObjects(port,
                        ((Entity) host).getPort(port.getName()));
            } else if (child instanceof Entity) {
                Entity entity = (Entity) child;
                _recordMirroredObjects(entity,
                        ((CompositeEntity) host).getEntity(entity.getName()));
            } else if (child instanceof Relation) {
                Relation relation = (Relation) child;
                _recordMirroredObjects(relation,
                        ((CompositeEntity) host).getRelation(
                                relation.getName()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean _relink(Relation preserved, Relation removed) {
        // FIXME: Because we can't find a nice way to preserve the channel index
        // of the ports, the "removed" relation won't be removed if it is
        // connected to a port that is connected to more than one relations.
        List<?> removedLinkedObjects =
            new LinkedList<Object>(removed.linkedObjectsList());
        for (Object removedLinkedObject : removedLinkedObjects) {
            if (removedLinkedObject instanceof Port) {
                if (((Port) removedLinkedObject).linkedRelationList().size()
                        > 1) {
                    return false;
                }
            }
        }

        // Remove the relation to be removed.
        String moml = "<deleteRelation name=\"" + removed.getName() + "\"/>";
        MoMLChangeRequest request = _createChangeRequest(removed.getContainer(),
                moml);
        request.execute();

        // Reconnect the objects previously linked to the removed relation to
        // the preserved relation.
        for (Object removedLinkedObject : removedLinkedObjects) {
            if (removedLinkedObject == preserved) {
                continue;
            }

            // Reconnect the preserved relation.
            moml = _getLinkMoML((NamedObj) removedLinkedObject, preserved);
            request = _createChangeRequest(preserved.getContainer(), moml);
            request.execute();
        }

        return true;
    }

    private void _removeLinks(CompositeEntity pattern) {
        Set<Pair<Relation, Object>> linksToRemove =
            new HashSet<Pair<Relation, Object>>();
        try {
            pattern.workspace().getReadAccess();
            Collection<?> relations = GTTools.getChildren(pattern, false, false,
                    false, true);
            for (Object relationObject : relations) {
                Relation relation = (Relation) relationObject;
                Relation replacementRelation = (Relation) _patternToReplacement
                        .get(relation);
                if (replacementRelation == null) {
                    continue;
                }

                List<?> linkedObjectList = relation.linkedObjectsList();
                for (Object linkedObject : linkedObjectList) {
                    Object replacementLinkedObject = _patternToReplacement
                            .get(linkedObject);
                    if (replacementLinkedObject == null) {
                        continue;
                    }

                    boolean linkRemoved;
                    if (replacementLinkedObject instanceof Port) {
                        linkRemoved = !((Port) replacementLinkedObject)
                                .isLinked(replacementRelation);
                    } else {
                        linkRemoved = !replacementRelation.linkedObjectsList()
                                .contains(replacementLinkedObject);
                    }

                    if (linkRemoved) {
                        linksToRemove.add(new Pair<Relation, Object>(relation,
                                linkedObject));
                    }
                }
            }
        } finally {
            pattern.workspace().doneReading();
        }

        for (Pair<Relation, Object> link : linksToRemove) {
            Relation hostRelation = (Relation) _matchResult
                    .get(link.getFirst());
            if (hostRelation == null) {
                continue;
            }
            Object hostObject = _matchResult.get(link.getSecond());
            if (hostObject == null) {
                continue;
            }

            String name;
            if (hostObject instanceof Port && hostRelation.getContainer()
                    != ((Port) hostObject).getContainer()) {
                Port port = (Port) hostObject;
                name = port.getContainer().getName() + "." + port.getName();
            } else {
                name = ((NamedObj) hostObject).getName();
            }

            String moml = "<unlink port=\"" + name + "\" relation=\""
                    + hostRelation.getName() + "\"/>";
            MoMLChangeRequest request = _createChangeRequest(
                    hostRelation.getContainer(), moml);
            request.execute();
        }

        try {
            pattern.workspace().getReadAccess();
            Collection<?> entities = GTTools.getChildren(pattern, false, false,
                    true, false);
            for (Object entityObject : entities) {
                if (entityObject instanceof CompositeEntity) {
                    _removeLinks((CompositeEntity) entityObject);
                }
            }
        } finally {
            pattern.workspace().doneReading();
        }
    }

    @SuppressWarnings("unchecked")
    private Set<NamedObj> _removeObject(NamedObj object, boolean shallowRemoval)
            throws TransformationException {
        if (shallowRemoval && object instanceof CompositeEntity) {
            CompositeEntity entity = (CompositeEntity) object;
            CompositeEntity container = (CompositeEntity) entity.getContainer();
            TwoWayHashMap<NamedObj, NamedObj> entityMap =
                new TwoWayHashMap<NamedObj, NamedObj>();
            Map<Port, List<Object>> portLinks =
                new HashMap<Port, List<Object>>();

            try {
                entity.workspace().getReadAccess();

                // Record the object codes in a temporary map.
                Collection<?> preservedChildren = GTTools.getChildren(entity,
                        false, false, true, true);

                // Record the connections to the composite entity's ports.
                for (Object portObject : entity.portList()) {
                    ComponentPort port = (ComponentPort) portObject;
                    List<Object> linkedRelations = new LinkedList<Object>();
                    linkedRelations.addAll(port.linkedRelationList());
                    linkedRelations.addAll(port.insideRelationList());
                    portLinks.put(port, linkedRelations);
                }

                // Remove the composite entity here because when the objects
                // are added back, their names will not conflict with the name
                // of this composite entity.
                MoMLChangeRequest request = GTTools.getDeletionChangeRequest(
                        this, object);
                if (_undoable) {
                    request.setUndoable(true);
                    if (_mergeWithPrevious) {
                        request.setMergeWithPreviousUndo(true);
                    } else {
                        _mergeWithPrevious = true;
                    }
                }
                request.execute();
                _removeReplacementToHostEntries(object);

                // Move the entities and relations inside to the outside.
                for (Object childObject : preservedChildren) {
                    NamedObj child = (NamedObj) childObject;
                    String moml = "<group name=\"auto\">\n"
                            + child.exportMoMLPlain() + "</group>";
                    CreateObjectChangeRequest request2 =
                        new CreateObjectChangeRequest(container, moml);
                    request2.execute();
                    //NamedObj newlyAddedObject = _getNewlyAddedObject(container,
                    //        child.getClass());
                    NamedObj newlyAddedObject = request2.getCreatedObjects().get(0);
                    _addReplacementToHostEntries(newlyAddedObject);
                    _replaceMatchResultEntries(child, newlyAddedObject);
                    entityMap.put(child, newlyAddedObject);
                }
            } finally {
                entity.workspace().doneReading();
            }

            // Create new relations for the ports.
            for (Map.Entry<Port, List<Object>> entry : portLinks.entrySet()) {
                Port port = entry.getKey();
                List<Object> linkedRelations = entry.getValue();
                int width = 1;
                for (Object relationObject : linkedRelations) {
                    Relation relation = (Relation) relationObject;
                    Parameter widthParameter = (Parameter) relation
                            .getAttribute("width");
                    if (widthParameter != null) {
                        try {
                            int thisWidth = ((IntToken) widthParameter
                                    .getToken()).intValue();
                            if (thisWidth > width) {
                                width = thisWidth;
                            }
                        } catch (IllegalActionException e) {
                            throw new TransformationException("Cannot get "
                                    + "width of relation " + relation.getName()
                                    + ".", e);
                        }
                    }
                }

                String moml = "<group name=\"auto\">"
                        + "<relation name=\"relation\" "
                        + "  class=\"ptolemy.actor.TypedIORelation\">"
                        + "</relation>" + "</group>";
                CreateObjectChangeRequest request =
                    new CreateObjectChangeRequest(container, moml);
                request.execute();
                //Relation newRelation = (Relation) _getNewlyAddedObject(
                //        container, Relation.class);
                Relation newRelation = (Relation) request.getCreatedObjects().get(0);

                entityMap.put(port, newRelation);
            }

            // Fix the connections between the moved entities and relations.
            for (Map.Entry<NamedObj, NamedObj> entry : entityMap.entrySet()) {
                NamedObj originalObject = entry.getKey();
                NamedObj newObject = entry.getValue();
                if (originalObject instanceof Relation
                        || originalObject instanceof Port
                        && newObject instanceof Relation) {
                    List<?> linkedObjectList;
                    if (originalObject instanceof Relation) {
                        linkedObjectList = ((Relation) originalObject)
                                .linkedObjectsList();
                    } else {
                        linkedObjectList = portLinks.get(originalObject);
                    }
                    Relation relation2 = (Relation) newObject;
                    for (Object linkedObject : linkedObjectList) {
                        if (linkedObject instanceof Relation) {
                            Relation relation1 = (Relation) entityMap
                                    .get(linkedObject);
                            if (relation1 == null) {
                                relation1 = (Relation) linkedObject;
                            }
                            String moml = _getLinkMoML(relation1, relation2);
                            MoMLChangeRequest request = _createChangeRequest(
                                    container, moml);
                            request.execute();
                        } else if (linkedObject instanceof Port) {
                            Port originalPort = (Port) linkedObject;
                            Entity linkedEntity = (Entity) entityMap
                                    .get(originalPort.getContainer());
                            if (linkedEntity != null) {
                                Port port1 = linkedEntity.getPort(originalPort
                                        .getName());
                                String moml = _getLinkMoML(port1, relation2);
                                MoMLChangeRequest request =
                                    _createChangeRequest(container, moml);
                                request.execute();
                            }
                        }
                    }
                }
            }

            return entityMap.values();
        } else {
            MoMLChangeRequest request = GTTools.getDeletionChangeRequest(this,
                    object);
            if (_undoable) {
                request.setUndoable(true);
                if (_mergeWithPrevious) {
                    request.setMergeWithPreviousUndo(true);
                } else {
                    _mergeWithPrevious = true;
                }
            }
            request.execute();
            return null;
        }
    }

    private void _removeObjects(CompositeEntity host)
            throws TransformationException {
        try {
            NamedObj replacement = _replacementToHost.getKey(host);
            Map<NamedObj, Boolean> childrenToRemove =
                new HashMap<NamedObj, Boolean>();
            Set<NamedObj> newChildren = new HashSet<NamedObj>();
            host.workspace().getReadAccess();
            Collection<?> children = GTTools.getChildren(host, false, true,
                    true, true);
            while (!children.isEmpty()) {
                childrenToRemove.clear();
                for (Object childObject : children) {
                    NamedObj child = (NamedObj) childObject;
                    NamedObj patternChild = (NamedObj) _matchResult.getKey(
                            child);
                    if (patternChild == null
                            || GTTools.isPreserved(patternChild)) {
                        continue;
                    }
                    NamedObj replacementChild = _replacementToHost
                            .getKey(child);
                    if (replacementChild == null && patternChild != null
                            && !GTTools.isCreated(patternChild)) {
                        Boolean shallowRemoval =
                            patternChild instanceof CompositeEntity ?
                                    Boolean.TRUE : Boolean.FALSE;
                        childrenToRemove.put(child, shallowRemoval);
                    } else if (replacementChild != null
                            && replacementChild.getContainer() != replacement
                            && replacementChild != patternChild) {
                        Boolean shallowRemoval =
                            replacementChild instanceof CompositeEntity ?
                                    Boolean.TRUE : Boolean.FALSE;
                        childrenToRemove.put(child, shallowRemoval);
                    }
                }
                newChildren.clear();
                for (Map.Entry<NamedObj, Boolean> entry
                        : childrenToRemove.entrySet()) {
                    NamedObj child = entry.getKey();
                    Set<NamedObj> newlyAddedChildren = _removeObject(child,
                            entry.getValue());
                    if (newlyAddedChildren != null) {
                        newChildren.addAll(newlyAddedChildren);
                    }
                }
                children = newChildren;
            }

            for (Object compositeChild : host.entityList(
                    CompositeEntity.class)) {
                _removeObjects((CompositeEntity) compositeChild);
            }
        } finally {
            host.workspace().doneReading();
        }
    }

    private void _removeReplacementObjectAttributes(NamedObj object) {
        ReplacementObjectAttribute attribute = _getReplacementObjectAttribute(
                object);
        if (attribute != null) {
            try {
                attribute.setContainer(null);
            } catch (IllegalActionException e) {
                // Impossible.
            } catch (NameDuplicationException e) {
                // Impossible.
            }
        }

        try {
            object.workspace().getReadAccess();
            Collection<?> children = GTTools.getChildren(object, false, true,
                    true, true);
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                _removeReplacementObjectAttributes(child);
            }
        } finally {
            object.workspace().doneReading();
        }
    }

    private void _removeReplacementToHostEntries(NamedObj host) {
        ReplacementObjectAttribute attribute = _getReplacementObjectAttribute(
                host);
        if (attribute != null) {
            String replacementCode = attribute.getExpression();
            NamedObj replacement = GTTools.getObjectFromCode(replacementCode,
                    _replacement);
            _replacementToHost.remove(replacement);
        }

        try {
            host.workspace().getReadAccess();
            Collection<?> children = GTTools.getChildren(host, false, true,
                    true, true);
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                _removeReplacementToHostEntries(child);
            }
        } finally {
            host.workspace().doneReading();
        }
    }

    private void _replaceMatchResultEntries(NamedObj oldHost,
            NamedObj newHost) {
        NamedObj pattern = (NamedObj) _matchResult.getKey(oldHost);
        if (pattern != null) {
            _matchResult.put(pattern, newHost);
        }

        try {
            newHost.workspace().getReadAccess();
            Collection<?> children = GTTools.getChildren(newHost, false, true,
                    true, true);
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                String code = GTTools.getCodeFromObject(child, newHost);
                NamedObj oldChild = GTTools.getObjectFromCode(code, oldHost);
                if (oldChild != null) {
                    _replaceMatchResultEntries(oldChild, child);
                }
            }
        } finally {
            newHost.workspace().doneReading();
        }
    }

    private void _setReplacementObjectAttribute(NamedObj object,
            String replacementObjectCode) throws TransformationException {
        try {
            ReplacementObjectAttribute attribute =
                _getReplacementObjectAttribute(object);
            if (attribute == null) {
                attribute = new ReplacementObjectAttribute(object,
                        "replacementObject");
            }
            attribute.setExpression(replacementObjectCode);
        } catch (KernelException e) {
            throw new TransformationException("Cannot set replacementObject "
                    + "attributes", e);
        }
    }

    private CompositeEntity _host;

    private MatchResult _matchResult;

    private List<MatchResult> _matchResults;

    private static boolean _mergeWithPrevious = false;

    private Map<NamedObj, String> _moml;

    private Pattern _pattern;

    private TwoWayHashMap<NamedObj, NamedObj> _patternToReplacement;

    private Replacement _replacement;

    private TwoWayHashMap<NamedObj, NamedObj> _replacementToHost;

    private static boolean _undoable = false;

    private class CreateObjectChangeRequest extends MoMLChangeRequest {

        public CreateObjectChangeRequest(NamedObj context, String request) {
            super(GraphTransformer.this, context, request);

            if (_undoable) {
                setUndoable(true);
                if (_mergeWithPrevious) {
                    setMergeWithPreviousUndo(true);
                } else {
                    _mergeWithPrevious = true;
                }
            }
        }

        public List<NamedObj> getCreatedObjects() {
            return _createdObjects;
        }

        @SuppressWarnings("unchecked")
        protected void _postParse(MoMLParser parser) {
            super._postParse(parser);

            _createdObjects = new LinkedList<NamedObj>(parser
                    .topObjectsCreated());
            parser.clearTopObjectsList();
        }

        protected void _preParse(MoMLParser parser) {
            super._preParse(parser);
            parser.clearTopObjectsList();
        }

        private List<NamedObj> _createdObjects;
    }

}
