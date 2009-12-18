/* An adapter class for ptolemy.actor.lib.Source.

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
package ptolemy.data.ontologies.adapters.dimensionSystem.actor.lib;

import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.OntologySolver;
import ptolemy.data.ontologies.adapters.dimensionSystem.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

////Source

/**
 An adapter class for ptolemy.actor.lib.Source.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class Source extends AtomicActor {

    /**
     * Construct a Source adapter for the dimensionSystem lattice.
     * @param solver The given solver.
     * @param actor The given Source actor
     * @exception IllegalActionException
     */
    public Source(OntologySolver solver,
            ptolemy.actor.lib.Source actor) throws IllegalActionException {

        super(solver, actor, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                            public methods                 ////

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.Source actor = (ptolemy.actor.lib.Source) getComponent();
        // add default constraints if no constraints specified in actor adapter

        if (_ownConstraints.isEmpty()) {
            // force outputs to UNKNOWN by default; overwrite in actor specific adapter class
            setAtLeast(actor.output, (Concept) getSolver().getOntology().getEntity("Unknown"));
        }

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /* 12/17/09 Charles Shelton
     * Commented out because ptolemy.graph.InequalityTerm does
     * not have a setEffective() method
     * 
    
    protected void _setEffectiveTerms() {
        ptolemy.actor.lib.Source actor = (ptolemy.actor.lib.Source) getComponent();

        for (TypedIOPort port : (List<TypedIOPort>) actor.portList()) {
            if ((port.numLinks() <= 0)
                    && (port.isInput())
                    && (interconnectConstraintType == ConstraintType.SINK_EQUALS_GREATER)) {

                if (!isAnnotated(port)) {
                    getPropertyTerm(port).setEffective(false);
                }
            }
        }
    }
    
    */
}
