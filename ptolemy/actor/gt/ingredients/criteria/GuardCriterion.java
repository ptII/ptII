/* A criterion to constrain a guard of a transition in an FSM or a Ptera
   controller.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.actor.gt.ingredients.criteria;

import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.ValidationException;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// GuardCriterion

/**
 A criterion to constrain a guard of a transition in an FSM or a Ptera
 controller.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GuardCriterion extends Criterion {

    /** Construct a criterion within the given list as its owner. All elements
     *  are enabled and are initialized to empty at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     */
    public GuardCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    /** Construct a criterion within the given list as its owner and initialize
     *  all the elements with the given values, which are a string encoding of
     *  those elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param values The string encoding of the values of the elements.
     */
    public GuardCriterion(GTIngredientList owner, String values) {
        super(owner, 1);
        setValues(values);
    }

    /** Get the array of elements defined in this GTIngredient.
     *
     *  @return The array of elements.
     */
    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    /** Get the value of the index-th elements.
     *
     *  @param index The index.
     *  @return The value.
     *  @see #setValue(int, Object)
     */
    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _guardValue;
        default:
            return null;
        }
    }

    /** Get a string that describes the values of all the elements.
     *
     *  @return A string that describes the values of all the elements.
     *  @see #setValues(String)
     */
    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _guardValue);
        return buffer.toString();
    }

    /** Check whether this GTIngredient is applicable to the object.
     *
     *  @param object The object.
     *  @return true if this GTIngredient is applicable; false otherwise.
     */
    public boolean isApplicable(NamedObj object) {
        return super.isApplicable(object) && object instanceof Transition;
    }

    /** Return whether this criterion can check the given object.
     *
     *  @param object The object.
     *  @return true if the object can be checked.
     */
    public boolean match(NamedObj object) {
        Variable guardVariable = null;
        try {
            guardVariable = new Variable(object,
                    object.uniqueName("guardVariable"));
            String guard = ((Transition) object).guardExpression
                    .getExpression();
            String guardTester = "(" + guard + ") == (" + _guardValue + ")";
            guardVariable.setExpression(guardTester);
            BooleanToken result = (BooleanToken) guardVariable.getToken();
            return result.booleanValue();
        } catch (Exception e) {
            return false;
        } finally {
            if (guardVariable != null) {
                try {
                    guardVariable.setContainer(null);
                } catch (Exception e) {
                    throw new InternalErrorException(
                            "Failed to set container of " + guardVariable
                                    + " to null");
                }
            }
        }
    }

    /** Set the value of the index-th element.
     *
     *  @param index The index.
     *  @param value The value.
     *  @see #getValue(int)
     */
    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _guardValue = (String) value;
            break;
        }
    }

    /** Set the values of all the elements with a string that describes them.
     *
     *  @param values A string that describes the new values of all the
     *   elements.
     *  @see #getValues()
     */
    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _guardValue = _decodeStringField(0, fieldIterator);
    }

    /** Validate the enablements and values of all the elements.
     *
     *  @exception ValidationException If some elements are invalid.
     */
    public void validate() throws ValidationException {
        if (_guardValue.equals("")) {
            throw new ValidationException("guardvalue name must not be empty.");
        }
    }

    /** The elements.
     */
    private static final CriterionElement[] _ELEMENTS = { new StringCriterionElement(
            "GuardValue", false, false, true) };

    /** Value of the guardValue element.
     */
    private String _guardValue;
}
