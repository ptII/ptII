/* An analysis for extracting the constructors of named objects.

 Copyright (c) 2003-2007 The Regents of the University of California.
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
package ptolemy.copernicus.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.util.NamedObj;
import soot.Local;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.CastExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.JimpleBody;

//////////////////////////////////////////////////////////////////////////
//// NamedObjAnalysis

/**
 An analysis that establishes a correspondence between each local
 variable that refers to a named obj in a method an the named object
 that it refers to.  This information is used to inline methods on
 named object locals.

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class NamedObjAnalysis {
    /** Create a NamedObjAnalysis.
     *  @param method  The Soot method.
     *  @param thisBinding The NamedObj.
     */
    public NamedObjAnalysis(SootMethod method, NamedObj thisBinding) {
        _errorObject = new NamedObj();

        try {
            _errorObject.setName("Error");
        } catch (Exception ex) {
            // Ignore..
        }

        JimpleBody body = (JimpleBody) method.getActiveBody();
        _localToObject = new HashMap();

        if (method.isStatic()) {
            //           System.out.println("Ignoring this binding for static method: "
            //                     + method);
        } else {
            _set(body.getThisLocal(), thisBinding);
        }

        _notDone = true;

        while (_notDone) {
            _notDone = false;

            for (Iterator units = body.getUnits().iterator(); units.hasNext();) {
                Unit unit = (Unit) units.next();

                if (unit instanceof DefinitionStmt) {
                    DefinitionStmt stmt = (DefinitionStmt) unit;
                    Value rightValue = stmt.getRightOp();

                    if (stmt.getLeftOp() instanceof Local) {
                        Local local = (Local) stmt.getLeftOp();

                        if (rightValue instanceof Local) {
                            _update(local, (Local) rightValue);
                        } else if (rightValue instanceof CastExpr) {
                            Value value = ((CastExpr) rightValue).getOp();

                            if (value instanceof Local) {
                                _update(local, (Local) value);
                            }
                        } else if (rightValue instanceof FieldRef) {
                            SootField field = ((FieldRef) rightValue)
                                    .getField();
                            _set(local, _getFieldObject(field));
                        }
                    } else if (stmt.getLeftOp() instanceof FieldRef) {
                        if (rightValue instanceof Local) {
                            SootField field = ((FieldRef) stmt.getLeftOp())
                                    .getField();
                            _set((Local) rightValue, _getFieldObject(field));
                        }
                    } else {
                        // Ignore..  probably not a named obj anyway.
                    }
                }
            }
        }
    }

    /** Return the NameObj that corresponds with the local argument.
     *  @param local The local variable
     *  @return The corresponding NamedObj.
     */
    public NamedObj getObject(Local local) {
        Object current = _localToObject.get(local);

        if ((current != null) && current.equals(_errorObject)) {
            throw new RuntimeException(
                    "Could not determine the static value of " + local);
        } else {
            return (NamedObj) current;
        }
    }

    /** Retrieve the field value tag for the given field and
     *  return its value.  If the field does not point to a namedObj,
     *  then return null.  If the field does not have a value tag, then
     *  return a unique namedObj.
     */
    private NamedObj _getFieldObject(SootField field) {
        if (field.getType() instanceof RefType
                && SootUtilities.derivesFrom(((RefType) field.getType())
                        .getSootClass(), PtolemyUtilities.namedObjClass)) {
            ValueTag tag = (ValueTag) field.getTag("_CGValue");

            if (tag == null) {
                return _errorObject;
            } else {
                return (NamedObj) tag.getObject();
            }
        } else {
            return null;
        }
    }

    private void _update(Local local, Local toLocal) {
        _set(local, (NamedObj) _localToObject.get(toLocal));
        _set(toLocal, (NamedObj) _localToObject.get(local));
    }

    private void _set(Local local, NamedObj object) {
        //         System.out.println("setting local " + local +
        //                 " to value of " + object);
        Object current = _localToObject.get(local);

        //         System.out.println("current = " + current);
        if (object == null) {
            // No new information.
            return;
        } else if (current != null) {
            if (current.equals(_errorObject) || current.equals(object)) {
                return;
            } else {
                _localToObject.put(local, _errorObject);
                _notDone = true;
            }
        } else {
            // Current == null && object != null
            _localToObject.put(local, object);
            _notDone = true;
        }
    }

    private NamedObj _errorObject;

    private Map _localToObject;

    private boolean _notDone;
}
