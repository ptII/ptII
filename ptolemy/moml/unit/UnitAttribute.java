/* UnitAttribute used to specify either a Unit Expression or Unit Equations.

 Copyright (c) 2003-2010 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_3
 COPYRIGHTENDKEY
 */
package ptolemy.moml.unit;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// UnitAttribute

/**
 This class is used to implement the Unit Attribute. A
 UnitsAttribute is either a UnitExpr, or a vector of
 UnitConstraints.

 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
 */
public class UnitAttribute extends AbstractSettableAttribute {
    /** Construct a UnitsAttribute with no specific name, or container.
     *  @exception IllegalActionException If the attribute is not of an
     *  acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *  an attribute already in the container.
     */
    public UnitAttribute() throws IllegalActionException,
            NameDuplicationException {
        super();
    }

    /** Construct a UnitsAttribute with the specified name, and container.
     * @param container Container
     * @param name Name
     * @exception IllegalActionException If the attribute is not of an
     *  acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *  an attribute already in the container.
     */
    public UnitAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be notified when the value of this attribute changes.
     *  If the listener is already on the list of listeners, then do nothing.
     *  @param listener The listener to add.
     */
    public void addValueListener(ValueListener listener) {
        if (_valueListeners == null) {
            _valueListeners = new LinkedList();
        }

        if (!_valueListeners.contains(listener)) {
            _valueListeners.add(listener);
        }
    }

    /** Write a MoML description of the UnitsAttribute.  Nothing is
     *  written if the value is null or "".
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     *  @see ptolemy.kernel.util.NamedObj#exportMoML(Writer, int, String)
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        String value = getExpression();
        String valueTerm = "";

        if ((value != null) && !value.equals("")) {
            valueTerm = " value=\"" + StringUtilities.escapeForXML(value)
                    + "\"";

            output.write(_getIndentPrefix(depth) + "<" + _elementName
                    + " name=\"" + name + "\" class=\"" + getClassName() + "\""
                    + valueTerm + ">\n");
            _exportMoMLContents(output, depth + 1);
            output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
        }
    }

    /** Get the descriptive form of this attribute.
     * @return a String that represents the descriptive form.
     * @see ptolemy.kernel.util.Settable#getExpression()
     */
    public String getExpression() {
        switch (_type) {
        case _EXPRESSION: {
            if (_unitExpr == null) {
                return "";
            }

            return _unitExpr.descriptiveForm();
        }

        case _CONSTRAINTS: {
            if (_unitConstraints == null) {
                return "";
            }

            return getUnitConstraints().descriptiveForm();
        }
        }

        return null;
    }

    /** Get the UnitConstraints.
     * @return The UnitConstraints.
     */
    public UnitConstraints getUnitConstraints() {
        return _unitConstraints;
    }

    /** Get the Unit Expression.
     * @return The UnitExpr.
     */
    public UnitExpr getUnitExpr() {
        return _unitExpr;
    }

    /** Get the visibility of this attribute, as set by setVisibility().
     * The visibility is set by default to NONE.
     * @return The visibility of this attribute.
     * @see ptolemy.kernel.util.Settable#getVisibility()
     */
    public Visibility getVisibility() {
        return _visibility;
    }

    /** Remove a listener from the list of listeners that is
     *  notified when the value of this attribute changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     */
    public void removeValueListener(ValueListener listener) {
        if (_valueListeners != null) {
            _valueListeners.remove(listener);
        }
    }

    /** Set the expression. This method takes the descriptive form and
     * determines the internal form (by parsing the descriptive form) and stores
     * it.
     * @param expression A String that is the descriptive form of either a Unit
     * or a UnitEquation.
     * @see ptolemy.kernel.util.Settable#setExpression(java.lang.String)
     */
    public void setExpression(String expression) throws IllegalActionException {
        super.setExpression(expression);

        try {
            if (getName().equals("_unitConstraints")) {
                Vector uEquations = UnitLibrary.getParser().parseEquations(
                        expression);
                UnitConstraints uConstraints = new UnitConstraints();

                for (int i = 0; i < uEquations.size(); i++) {
                    uConstraints.addConstraint((UnitEquation) (uEquations
                            .elementAt(i)));
                }

                setUnitConstraints(uConstraints);
            }

            if (getName().equals("_units")) {
                UnitExpr uExpr;
                uExpr = UnitLibrary.getParser().parseUnitExpr(expression);
                setUnitExpr(uExpr);
            }
        } catch (ParseException ex) {
            throw new IllegalActionException(this, ex,
                    "Can't parse the expression " + expression);
        }
    }

    /** Set the UnitConstraints.
     * @param constraints The UnitConstraints.
     */
    public void setUnitConstraints(UnitConstraints constraints) {
        _unitConstraints = constraints;
        _type = _CONSTRAINTS;
    }

    /** Set the Unit Expression.
     * @param expr A UnitExpr.
     */
    public void setUnitExpr(UnitExpr expr) {
        _unitExpr = expr;
        _type = _EXPRESSION;
    }

    /** Set the visibility of this attribute.  The argument should be one
     *  of the public static instances in Settable.
     *  @param visibility The visibility of this attribute.
     *  @see ptolemy.kernel.util.Settable#setVisibility(Settable.Visibility)
     */
    public void setVisibility(Visibility visibility) {
        _visibility = visibility;
    }

    /* Not really relevant to current capability.
     * But has to be included this class implements the Settable interface.
     * @return Null, indicating that no other instances of Settable are
     *   validated.
     * @see ptolemy.kernel.util.Settable#validate()
     */
    public Collection validate() throws IllegalActionException {
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Propagate the value of this object to the
     *  specified object. The specified object is required
     *  to be an instance of the same class as this one, or
     *  a ClassCastException will be thrown.
     *  @param destination Object to which to propagate the
     *   value.
     *  @exception IllegalActionException If the value cannot
     *   be propagated.
     */
    protected void _propagateValue(NamedObj destination)
            throws IllegalActionException {
        ((Settable) destination).setExpression(getExpression());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    Visibility _visibility = Settable.NONE;

    // Listeners for changes in value.
    private List _valueListeners;

    private UnitExpr _unitExpr = null;

    private UnitConstraints _unitConstraints = null;

    private int _type = -1;

    private static final int _EXPRESSION = 0;

    private static final int _CONSTRAINTS = 1;
}
