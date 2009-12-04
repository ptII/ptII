/* An adapter class for ptolemy.actor.lib.Expression.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.dimensionSystem.actor.lib;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.dimensionSystem.actor.AtomicActor;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Expression

/**
 An adapter class for ptolemy.actor.lib.Expression.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Expression extends AtomicActor {

    /**
     * Construct a Expression adapter for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given Expression actor
     * @exception IllegalActionException
     */
    public Expression(PropertyConstraintSolver solver,
            ptolemy.actor.lib.Expression actor) throws IllegalActionException {

        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.Expression actor = (ptolemy.actor.lib.Expression) getComponent();

        setAtLeast(actor.output, actor.expression);
        return super.constraintList();
    }

    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();

        ptolemy.actor.lib.Expression actor = (ptolemy.actor.lib.Expression) getComponent();
        result.add(actor.expression);
        return result;
    }
}
