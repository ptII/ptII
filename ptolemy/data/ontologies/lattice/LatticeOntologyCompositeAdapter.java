/* Code generator adapter for typed composite actor.

 Copyright (c) 2005-2011 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver.ConstraintType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

////TypedCompositeActor

/**
 Code generator adapter for composite actor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class LatticeOntologyCompositeAdapter extends LatticeOntologyAdapter {

    /** Construct the property constraint adapter associated
     *  with the given TypedCompositeActor.
     * @param solver The lattice ontology solver used for this adapter.
     * @param component The associated component.
     * @exception IllegalActionException If the adapter cannot be intialized.
     */
    public LatticeOntologyCompositeAdapter(LatticeOntologySolver solver,
            CompositeEntity component) throws IllegalActionException {
        super(solver, component, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add default constraints for the composite actor referred to by this
     * LatticeOntologyCompositeAdapter based on the given ConstraintType.
     * This method iteratively adds all the default constraints for all model components
     * contained by the composite component referred to by this adapter.
     *
     * @see ConstraintType
     * @param actorConstraintType The given ConstraintType for the default constraints
     * for the composite actor referred to by this LatticeOntologyCompositeAdapter
     * @exception IllegalActionException If an exception is thrown
     */
    protected void _addDefaultConstraints(ConstraintType actorConstraintType)
            throws IllegalActionException {

        for (OntologyAdapter adapter : _getSubAdapters()) {

            ((LatticeOntologyAdapter) adapter)
                    ._addDefaultConstraints(actorConstraintType);
        }
    }

    /**
     * Return the list of sub-adapters. In this base class, it
     * returns the list of ASTNode adapters and the adapters for
     * the contained entities.
     * @return The list of sub-adapters.
     * @exception IllegalActionException Thrown if there is an error
     *  getting the adapter for any contained entities.
     */
    protected List<OntologyAdapter> _getSubAdapters()
            throws IllegalActionException {
        List<OntologyAdapter> adapters = super._getSubAdapters();

        CompositeEntity component = (CompositeEntity) getComponent();

        for (Object actor : component.entityList()) {
            adapters.add(_solver.getAdapter(actor));
        }
        return adapters;
    }

    /** Return all constraints of this component.  The constraints is
     *  a list of inequalities.
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        CompositeEntity actor = (CompositeEntity) getComponent();

        // Set up inter-actor constraints.
        for (Entity entity : (List<Entity>) actor.entityList()) {
            LatticeOntologyAdapter adapter = (LatticeOntologyAdapter) _solver
                    .getAdapter(entity);

            boolean constraintSource = adapter.isConstraintSource();

            for (TypedIOPort port : (List<TypedIOPort>) adapter
                    ._getConstraintedPorts(constraintSource)) {

                // If the port is a multiport with more than one channel,
                // don't add any constraints.  These will be added
                // by the actor's adapter.
                if (!((port).isMultiport() && (port).getWidth() > 1)) {
                    _constrainObject(adapter.interconnectConstraintType, port,
                            _getConstraintingPorts(constraintSource, port));
                }
            }
        }

        // Set up inner composite connection constraints.
        for (TypedIOPort port : (List<TypedIOPort>) _getConstraintedInsidePorts(isConstraintSource())) {
            _constrainObject(interconnectConstraintType, port,
                    port.insidePortList());
        }

        return super.constraintList();
    }

    /**
     * Return the list of constrained ports given the flag
     * whether source or sink ports should be constrainted.
     * If source ports are constrained, it returns the list
     * of input ports of the assoicated actor; otherwise, it
     * returns the list of output ports.
     * @param constraintSource The flag that indicates whether
     *  source or sink ports are constrainted.
     * @return The list of constrainted ports.
     */
    protected List _getConstraintedInsidePorts(boolean constraintSource) {
        Actor actor = (Actor) getComponent();
        return constraintSource ? actor.inputPortList() : actor
                .outputPortList();
    }
}
