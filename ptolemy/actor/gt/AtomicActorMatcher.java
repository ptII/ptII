/* A matcher to match any AtomicActor or CompositeActor that is considered as a
   blackbox.

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

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gt.ingredients.criteria.AttributeCriterion;
import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.actor.gt.ingredients.criteria.PortCriterion;
import ptolemy.actor.gt.ingredients.operations.Operation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.vergil.gt.GTIngredientsEditor;

//////////////////////////////////////////////////////////////////////////
//// AtomicActorMatcher

/**
 A matcher to match any AtomicActor or CompositeActor that is considered as a
 blackbox. In the pattern of a {@link TransformationRule}, this matcher can be
 customized by instances of {@link Criterion}. A {@link PortCriterion}, for
 instance, customizes this matcher with an additional port, whose type and name
 can be specified. In the replacement of a {@link TransformationRule},
 operations can be specified for this matcher with instances of
 {@link Operation}. The operations will be performed on the actor that is
 matched by the corresponding matcher in the pattern, and is preserved after the
 transformation.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see CompositeActorMatcher
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AtomicActorMatcher extends TypedAtomicActor implements GTEntity,
        ValueListener {

    /** Construct an atomic actor matcher to be either contained in the pattern
     *  of a {@link TransformationRule} or in the replacement.
     *
     *  @param container The proposed container of this matcher.
     *  @param name The name of this matcher.
     *  @exception IllegalActionException If this actor cannot be contained by
     *   the proposed container.
     *  @exception NameDuplicationException If the name coincides with an entity
     *   already in the container.
     */
    public AtomicActorMatcher(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.AtomicActorMatcher");

        criteria = new GTIngredientsAttribute(this, "criteria");
        criteria.setExpression("");
        criteria.addValueListener(this);

        operations = new GTIngredientsAttribute(this, "operations");
        operations.setExpression("");
        operations.addValueListener(this);

        patternObject = new PatternObjectAttribute(this, "patternObject");
        patternObject.setExpression("");
        patternObject.addValueListener(this);

        editorFactory = new GTIngredientsEditor.Factory(this, "editorFactory");

        _attachText("_iconDescription", _ICON_DESCRIPTION);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the attribute that stores all the criteria for this matcher.
     *
     *  @return The attribute that stores all the criteria.
     */
    public GTIngredientsAttribute getCriteriaAttribute() {
        return criteria;
    }

    /** Return a string that contains the SVG icon description
     *  ("&lt;svg&gt;...&lt;/svg&gt;") for this matcher. This icon description
     *  is the default icon for the matcher, which may be changed by the
     *  criteria.
     *
     *  @return The icon description.
     */
    public String getDefaultIconDescription() {
        return _ICON_DESCRIPTION;
    }

    /** Return the attribute that stores all the operations for this matcher.
     *
     *  @return The attribute that stores all the operations.
     */
    public GTIngredientsAttribute getOperationsAttribute() {
        return operations;
    }

    /** Return the attribute that stores the name of the corresponding entity in
     *  the pattern of the same {@link TransformationRule}, if this entity is in
     *  the replacement, or <tt>null</tt> otherwise.
     *
     *  @return The attribute that stores the name of the corresponding entity.
     *  @see #labelSet()
     */
    public PatternObjectAttribute getPatternObjectAttribute() {
        return patternObject;
    }

    /** Return the set of names of ingredients contained in this entity that can
     *  be resolved.
     *
     *  @return The set of names.
     */
    public Set<String> labelSet() {
        long version = workspace().getVersion();
        if (_labelSet == null || version > _version) {
            _labelSet = new HashSet<String>();
            try {
                int i = 0;
                for (GTIngredient ingredient : criteria.getIngredientList()) {
                    i++;
                    Criterion criterion = (Criterion) ingredient;
                    if (criterion instanceof AttributeCriterion) {
                        _labelSet.add("criterion" + i);
                    }
                }
            } catch (MalformedStringException e) {
                return _labelSet;
            }
        }
        return _labelSet;
    }

    /** Test whether this AtomicActorMatcher can match the given object. The
     *  matching is shallow in the sense that objects contained by this matcher
     *  need not match the corresponding objects in the given object for the
     *  return result to be true. The return result is true if and only if the
     *  given object is an instance of {@link ComponentEntity}.
     *
     *  @param object The NamedObj.
     *  @return Whether this AtomicActorMatcher can match the given object.
     */
    public boolean match(NamedObj object) {
        return object instanceof ComponentEntity;
    }

    /** Update appearance of this entity.
     *
     *  @param attribute The attribute containing ingredients of this entity.
     *  @see GTEntityUtils#updateAppearance(GTEntity, GTIngredientsAttribute)
     */
    public void updateAppearance(GTIngredientsAttribute attribute) {
        GTEntityUtils.updateAppearance(this, attribute);
    }

    /** React to the fact that the specified Settable has changed.
     *
     *  @param settable The object that has changed value.
     *  @see GTEntityUtils#valueChanged(GTEntity, Settable)
     */
    public void valueChanged(Settable settable) {
        GTEntityUtils.valueChanged(this, settable);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /** The attribute containing all the criteria in a list
     *  ({@link GTIngredientList}).
     */
    public GTIngredientsAttribute criteria;

    /** The editor factory for ingredients in this matcher.
     */
    public GTIngredientsEditor.Factory editorFactory;

    /** The attribute containing all the operations in a list
     *  ({@link GTIngredientList}).
     */
    public GTIngredientsAttribute operations;

    /** The attribute that specifies the name of the corresponding entity in the
     *  pattern.
     */
    public PatternObjectAttribute patternObject;

    protected void _exportMoMLContents(Writer output, int depth)
    throws IOException {
        super._exportMoMLContents(output, depth);
        GTEntityUtils.exportExtraProperties(this, output, depth);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The default icon description.
     */
    private static final String _ICON_DESCRIPTION = "<svg>"
            + "<rect x=\"0\" y=\"0\" width=\"60\" height=\"40\""
            + "  style=\"fill:#C0C0C0\"/>"
            + "<rect x=\"5\" y=\"17\" width=\"16\" height=\"10\""
            + "  style=\"fill:#FFFFFF; stroke:#B00000\"/>"
            + "<rect x=\"39\" y=\"25\" width=\"16\" height=\"10\""
            + "  style=\"fill:#FFFFFF; stroke:#B00000\"/>"
            + "<line x1=\"25\" y1=\"22\" x2=\"30\" y2=\"22\""
            + "  style=\"stroke:#404040\"/>"
            + "<line x1=\"30\" y1=\"22\" x2=\"30\" y2=\"30\""
            + "  style=\"stroke:#404040\"/>"
            + "<line x1=\"30\" y1=\"30\" x2=\"35\" y2=\"30\""
            + "  style=\"stroke:#404040\"/>"
            + "<text x=\"17\" y=\"13\""
            + "  style=\"font-size:12; fill:#E00000; font-family:SansSerif\">"
            + "  match</text>" + "</svg>";

    /** Cache of the label set.
     */
    private Set<String> _labelSet;

    /** The workspace version the last time when _labelSet was updated.
     */
    private long _version = -1;
}
