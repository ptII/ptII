/* The pattern for visitors to a UnitEquation.

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

import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// EquationVisitor

/**
 An Abstract class that specifies all the necessary aspects of visitors to a
 UnitEquation.

 <p> The generic version of the method to visit a UnitEquation,
 UnitExpr, and UnitTerm are specified here. To do a specific kind of
 visit create a class that extends EquationVisitor that overrides
 some one or more of these methods.  These methods are specified as
 throwing an IllegalActionException to make it possible for the
 overridden methods in a subclass to throw IllegalActionException.

 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
 */
public abstract class EquationVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Visit a UnitEquation by visiting the left and right sides of the
     * equation.
     * @param uEquation The UnitEquation to visit.
     * @return Null, can be overridden in a concrete visitor.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected Object _visitUnitEquation(UnitEquation uEquation)
            throws IllegalActionException {
        _visitUnitExpr(uEquation.getLhs());
        _visitUnitExpr(uEquation.getRhs());
        return null;
    }

    /** Visit a UnitExpr by visiting the UnitRerms.
     * @param unitExpr The UnitExpr to visit.
     * @return Null, can be overridden in a concrete visitor.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected Object _visitUnitExpr(UnitExpr unitExpr)
            throws IllegalActionException {
        Iterator iter = unitExpr.getUTerms().iterator();

        while (iter.hasNext()) {
            UnitTerm term = (UnitTerm) (iter.next());
            term.visit(this);
        }

        return null;
    }

    /** Visit a UnitTerm by visiting the UnitExpr if there is one. This method
     * will almost certainly be overridden in a subclass.
     * @param uTerm The UnitTerm to visit.
     * @return Null, can be overridden in a concrete visitor.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected Object _visitUnitTerm(UnitTerm uTerm)
            throws IllegalActionException {
        if (uTerm.isUnitExpr()) {
            UnitExpr uExpr = uTerm.getUnitExpr();

            if (uExpr != null) {
                _visitUnitExpr(uExpr);
            }
        }

        return null;
    }
}
