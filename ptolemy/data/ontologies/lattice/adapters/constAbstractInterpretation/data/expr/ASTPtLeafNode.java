/* The adapter class for ptolemy.data.expr.ASTPtLeafNode for constAbstractInterpretation ontology.

 Copyright (c) 2006-2010 The Regents of the University of California.
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

package ptolemy.data.ontologies.lattice.adapters.constAbstractInterpretation.data.expr;

import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.ontologies.lattice.LatticeOntologyASTNodeAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.graph.Inequality;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtLeafNode

/**
 The adapter class for ptolemy.data.expr.ASTPtRootNode for constAbstractInterpretation ontology.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ASTPtLeafNode extends LatticeOntologyASTNodeAdapter {

    /** Construct an property constraint adapter for the given ASTPtLeafNode.
     *  @param solver The given solver to get the lattice from.
     *  @param node The given ASTPtArrayConstructNode.
     *  @exception IllegalActionException Thrown if the parent construct
     *   throws it.
     */
    public ASTPtLeafNode(LatticeOntologySolver solver,
            ptolemy.data.expr.ASTPtLeafNode node)
            throws IllegalActionException {
        super(solver, node, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the constraint list for the adapter.
     *  @exception IllegalActionException If there is an error building the constraint list.
     *  @return The list of constraints for this adapter.
     */
    public List<Inequality> constraintList() throws IllegalActionException {

        ptolemy.data.expr.ASTPtLeafNode node = (ptolemy.data.expr.ASTPtLeafNode) _getNode();
        Token nodeToken = node.getToken();

        if (node.isConstant()) {
            if (nodeToken != null) {
                if (nodeToken instanceof BooleanToken) {
                    if (((BooleanToken) nodeToken).booleanValue()) {
                        setAtLeast(node, getSolver().getOntology().getEntity("BooleanTrue"));
                    } else {
                        setAtLeast(node, getSolver().getOntology().getEntity("BooleanFalse"));
                    }
                } else if (nodeToken instanceof ScalarToken) {
                    if (((ScalarToken) nodeToken).isEqualTo(nodeToken.zero()).booleanValue()) {
                        setAtLeast(node, getSolver().getOntology().getEntity("Zero"));
                    } else if (((ScalarToken) nodeToken).isGreaterThan((ScalarToken) nodeToken.zero()).booleanValue()) {
                        setAtLeast(node, getSolver().getOntology().getEntity("Positive"));
                    } else {
                        setAtLeast(node, getSolver().getOntology().getEntity("Negative"));
                    }
                } else {
                    setAtLeast(node, getSolver().getOntology().getEntity("UnknownConst"));
                }
            } else {
                setAtLeast(node, getSolver().getOntology().getEntity("UnknownConst"));
            }
        }

        return super.constraintList();
    }
}
