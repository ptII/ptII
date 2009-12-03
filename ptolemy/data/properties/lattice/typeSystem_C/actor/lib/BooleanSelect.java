/* An adapter class for ptolemy.actor.lib.BooleanSelect.

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
package ptolemy.data.properties.lattice.typeSystem_C.actor.lib;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.typeSystem_C.Lattice;
import ptolemy.data.properties.lattice.typeSystem_C.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// BooleanSelect

/**
 An adapter class for ptolemy.actor.lib.BooleanSelect.

 @author Thomas Mandl, Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class BooleanSelect extends AtomicActor {
    /**
     * Construct an BooleanSelect adapter.
     * @param solver The associated solver.
     * @param actor The associated actor.
     * @exception IllegalActionException If thrown by the super class. 
     */
    public BooleanSelect(PropertyConstraintSolver solver,
            ptolemy.actor.lib.BooleanSelect actor)
            throws IllegalActionException {

        super(solver, actor, false);
        _lattice = (Lattice) getSolver().getLattice();
        _actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the constraints of this component. The constraints are a list of
     * inequalities.
     * @return The constraints of this component.
     * @exception IllegalActionException If thrown while manipulating the lattice
     * or getting the solver.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        setAtLeast(_actor.output, _actor.trueInput);
        setAtLeast(_actor.output, _actor.falseInput);
        setEquals(_actor.control, _lattice.getElement("BOOLEAN"));

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ptolemy.actor.lib.BooleanSelect _actor;
    private Lattice _lattice;
}
