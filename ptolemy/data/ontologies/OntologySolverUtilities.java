/** A class representing shared utilities for ontology solvers in the ontologies package.

 Copyright (c) 1997-2010 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.kernel.util.Attribute;

//////////////////////////////////////////////////////////////////////////
//// SharedUtilities.

/**
 A class representing shared utilities for ontology solvers in the ontologies package.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
*/
public class OntologySolverUtilities {

    /**
     * Construct a new SharedUtilities object.
     */
    public OntologySolverUtilities() {
        // Since this is a shared (singleton) object per model,
        // it is important that all model-specific references
        // need to be reset when cloned. Otherwise, it will lead
        // to bugs that are hard to detect, and inconsistency
        // will occur.
        _id = _count++;
    }

    /**
     * Record the given error message.
     * @param error The error message to record.
     */
    public void addErrors(String error) {
        _errors.add(error);
    }

    /**
     * Mark the given property solver as already activated.
     * @param solver The given solver.
     */
    public void addRanSolvers(OntologySolver solver) {
        _ranSolvers.add(solver);
    }
    

    /**
     * Return the map that maps root ast node (keys) to the corresponding
     * attribute (values).
     * @return The mappings for root ast nodes to attributes.
     */
    public Map<ASTPtRootNode, Attribute> getAttributes() {
        return _attributes;
    }

    /**
     * Return the list of error strings.
     * @return The list of error strings.
     */
    public List<String> getErrors() {
        Collections.sort(_errors);
        return _errors;
    }

    /**
     * Return the map that maps attributes (keys) to their root ast nodes
     * (values).
     * @return The mappings for attributes to their root ast nodes.
     */
    public Map<Attribute, ASTPtRootNode> getParseTrees() {
        return _parseTrees;
    }

    /**
     * Return the set of solvers that were marked activated.
     * @return The set of solvers that were activated previously.
     */
    public Set<OntologySolver> getRanSolvers() {
        return _ranSolvers;
    }

    /**
     * Clear and return the previously recorded errors.
     * @return The list of previously recorded errors.
     */
    public List<String> removeErrors() {
        List<String> result = new ArrayList<String>(_errors);
        _errors.clear();
        return result;
    }
    
    /**
     * Removes the specified solver from the set of ran solvers, if this
     * solver was in the set.  Does nothing otherwise.
     * @param solver  The solver to remove from the set of ran solvers
     */
    public void removeRanSolver(OntologySolver solver) {
        _ranSolvers.remove(solver);
    }
    

    /**
     * Clear the states of this shared object. The states include all previously
     * recorded information.
     */
    public void resetAll() {
        _ranSolvers = new HashSet<OntologySolver>();
        _parseTrees = new HashMap<Attribute, ASTPtRootNode>();
        _attributes = new HashMap<ASTPtRootNode, Attribute>();
        _errors = new ArrayList<String>();

    }

    /**
     * Return the representation for the SharedUtilities object.
     * 
     * @return The string representation of the SharedUtilities object
     */
    public String toString() {
        String result = "sharedUtilities#" + _id;
        return result;
    }

    /**
     * Record the association between the given ast node and the given
     * attribute.
     * @param node The given ast node.
     * @param attribute The given attribute.
     */
    protected void putAttribute(ASTPtRootNode node, Attribute attribute) {
        _attributes.put(node, attribute);
    }

    /**
     * The set of solvers that have already been invoked.
     */
    private HashSet<OntologySolver> _ranSolvers = new HashSet<OntologySolver>();

    private Map<Attribute, ASTPtRootNode> _parseTrees = new HashMap<Attribute, ASTPtRootNode>();

    private Map<ASTPtRootNode, Attribute> _attributes = new HashMap<ASTPtRootNode, Attribute>();

    private ArrayList<String> _errors = new ArrayList<String>();

    private static int _count = 0;

    private final int _id;
}
