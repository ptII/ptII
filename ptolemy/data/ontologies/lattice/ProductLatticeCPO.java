/* A complete partial order for product lattice-based ontologies.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 */

package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ProductLatticeCPO

/** A complete partial order for product lattice-based ontologies.
 *  Given a product lattice defined by a list of {@link ProductLatticeConcept}s,
 *  this class provides the implementation for all complete partial order
 *  operations on the product lattice. Note that this complete partial order
 *  implementation is not derived from a graph of the concepts, but rather by
 *  doing comparison operations that depend on the structure of the individual
 *  lattices that comprise the product lattice.  For example, take a product
 *  lattice P that is composed of two lattices L1 and L2.  Each lattice element
 *  concept in P is a tuple of the form &lt;C(L1), C(L2)&gt;.  To decide the
 *  relationship between two concepts in p1 and p2 in P, it is determined by
 *  the relationships of the individual concepts in their tuples. So:
 *  p1 >= p2 iff C1(L1) >= C2(L1) and C1(L2) >= C2(L2)
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeCPO implements CPO {
    
    /** Create a new ProductLatticeCPO from the given list of
     *  ProductLatticeConcepts.
     *  @param productOntology The product lattice ontology for which this CPO
     *   is a complete partial order.
     */
    public ProductLatticeCPO(ProductLatticeOntology productOntology) {
        _productOntology = productOntology;          
        try {
            _ontologyList = _productOntology.getLatticeOntologies();
        } catch (IllegalActionException ex) {
            throw new IllegalArgumentException("Invalid product lattice ontology; " +
            		"could not get the list of tuple ontologies for the product " +
            		"lattice ontology.", ex);
        }
        
        _findBottom();
        _findTop();
        _cachedGLBs = new HashMap<List<ProductLatticeConcept>, ProductLatticeConcept>();
        _cachedLUBs = new HashMap<List<ProductLatticeConcept>, ProductLatticeConcept>();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the bottom element of this CPO.
     *  The bottom element is the element in the CPO that is lower than
     *  all the other elements.
     *  @return An Object representing the bottom element, or
     *   <code>null</code> if the bottom does not exist.
     */
    public Object bottom() {
        return _bottomConcept;
    }

    /** Compare two concepts in the product lattice ontology. The arguments must be
     *  instances of {@link ProductLatticeConcept}, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER, ptolemy.graph.CPO.SAME,
     *  ptolemy.graph.CPO.HIGHER, ptolemy.graph.CPO.INCOMPARABLE, indicating the
     *  first argument is lower than, equal to, higher than, or incomparable with
     *  the second argument in the product lattice hierarchy, respectively.
     *  @param e1 An instance of {@link ProductLatticeConcept}.
     *  @param e2 An instance of {@link ProductLatticeConcept}.
     *  @return One of CPO.LOWER, CPO.SAME, CPO.HIGHER, CPO.INCOMPARABLE.
     *  @exception IllegalArgumentException If one or both arguments are not
     *   instances of {@link ProductLatticeConcept}, the arguments are not from
     *   the same ontology, or either argument has an empty or null concept tuple list.
     */
    public int compare(Object e1, Object e2) {
        if (e1 == null || e2 == null) {
            return CPO.INCOMPARABLE;
        }
        
        _validateInputArguments(e1, e2);            
        List<Concept> leftArgTuple = ((ProductLatticeConcept) e1).getConceptTuple();
        List<Concept> rightArgTuple = ((ProductLatticeConcept) e2).getConceptTuple();
        int tupleSize = leftArgTuple.size();
        int numSame = 0;
        int numHigher = 0;
        int numLower = 0;

        // For each pair of concepts in the tuple
        // track which ones are higher, same, or lower.
        for (int i = 0; i < tupleSize; i++) {
            Ontology tupleOntology = leftArgTuple.get(i).getOntology();
            int comparison = tupleOntology.getGraph().compare(leftArgTuple.get(i), rightArgTuple.get(i));
            
            if (comparison == CPO.HIGHER) {
                numHigher++;
            } else if (comparison == CPO.SAME) {
                numSame++;
            } else if (comparison == CPO.LOWER) {
                numLower++;
            }
        }        
            
        // If all concepts in the tuple are the same, the product lattice concepts
        // are the same.  
        if (numSame == tupleSize) {
            return CPO.SAME;

        // If all concepts in the tuple are higher or the same, the product lattice concept
        // is higher.
        } else if (numHigher == tupleSize || numHigher + numSame == tupleSize) {
            return CPO.HIGHER;

        // If all concepts in the tuple are lower or the same, the product lattice concept
        // is lower.   
        } else if (numLower == tupleSize || numLower + numSame == tupleSize) {
            return CPO.LOWER;

        // Otherwise the product lattice concepts are incomparable.
        } else {
            return CPO.INCOMPARABLE;
        }
    }
    
    /** Compute the down-set of an element in this CPO.
     *  The down-set of an element is the subset consisting of
     *  all the elements lower than or the same as the specified element.
     *  @param e An Object representing an element in this CPO.
     *  @return An array of Objects representing the elements in the
     *   down-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element in this CPO, or the resulting set is infinite.
     */
    public Object[] downSet(Object e) {
        throw new IllegalArgumentException("Method not implemented!");
    }

    /** Compute the greatest element of a subset.
     *  The greatest element of a subset is an element in the
     *  subset that is higher than all the other elements in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return An Object representing the greatest element of the subset,
     *   or <code>null</code> if the greatest element does not exist.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this CPO.
     */
    public Object greatestElement(Object[] subset) {
        throw new IllegalArgumentException("Method not implemented!");
    }

    /** Compute the greatest lower bound (GLB) of two elements.
     *  The GLB of two elements is the greatest element in the CPO
     *  that is lower than or the same as both of the two elements.
     *  @param e1 An Object representing an element in this CPO.
     *  @param e2 An Object representing an element in this CPO.
     *  @return An Object representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public Object greatestLowerBound(Object e1, Object e2) {
        List<ProductLatticeConcept> inputs = new ArrayList<ProductLatticeConcept>();
        inputs.add((ProductLatticeConcept) e1);
        inputs.add((ProductLatticeConcept) e2);
        
        ProductLatticeConcept glb = _cachedGLBs.get(inputs);        
        if (glb == null) {
            if (e1 == null || e2 == null) {
                return null;
            }
            
            _validateInputArguments(e1, e2);            
            List<Concept> leftArgTuple = ((ProductLatticeConcept) e1).getConceptTuple();
            List<Concept> rightArgTuple = ((ProductLatticeConcept) e2).getConceptTuple();
            int tupleSize = leftArgTuple.size();

            String glbName = new String("");
            for (int i = 0; i < tupleSize; i++) {
                Ontology tupleOntology = leftArgTuple.get(i).getOntology();
                glbName += ((Concept) (tupleOntology.getCompletePartialOrder().
                        greatestLowerBound(leftArgTuple.get(i), rightArgTuple.get(i)))).getName();
            }

            glb = (ProductLatticeConcept) _productOntology.getEntity(glbName);
            _cachedGLBs.put(inputs, glb);
        }        
        return glb;
    }

    /** Compute the greatest lower bound (GLB) of a subset.
     *  The GLB of a subset is the greatest element in the CPO that
     *  is lower than or the same as all the elements in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object greatestLowerBound(Object[] subset) {
        throw new IllegalArgumentException("Method not implemented!");
    }

    /** Test if this CPO is a lattice. A Product Lattice CPO is a lattice if
     *  all of its component ontologies are lattices.
     *  @return True if this CPO is a lattice;
     *   <code>false</code> otherwise.
     */
    public boolean isLattice() {
        for(Ontology ontology : _ontologyList) {
            if (!ontology.isLattice()) {
                return false;
            }
        }        
        return true;
    }

    /** Compute the least element of a subset.
     *  The least element of a subset is an element in the
     *  subset that is lower than all the other element in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object leastElement(Object[] subset) {
        throw new IllegalArgumentException("Method not implemented!");
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  The LUB of two elements is the least element in the CPO
     *  that is greater than or the same as both of the two elements.
     *  @param e1 An Object representing an element in this CPO.
     *  @param e2 An Object representing an element in this CPO.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object leastUpperBound(Object e1, Object e2) {
        List<ProductLatticeConcept> inputs = new ArrayList<ProductLatticeConcept>();
        inputs.add((ProductLatticeConcept) e1);
        inputs.add((ProductLatticeConcept) e2);
        
        ProductLatticeConcept lub = _cachedLUBs.get(inputs);        
        if (lub == null) {
            if (e1 == null || e2 == null) {
                return null;
            }
            
            _validateInputArguments(e1, e2);            
            List<Concept> leftArgTuple = ((ProductLatticeConcept) e1).getConceptTuple();
            List<Concept> rightArgTuple = ((ProductLatticeConcept) e2).getConceptTuple();
            int tupleSize = leftArgTuple.size();

            String lubName = new String("");
            for (int i = 0; i < tupleSize; i++) {
                Ontology tupleOntology = leftArgTuple.get(i).getOntology();
                lubName += ((Concept) (tupleOntology.getCompletePartialOrder().
                        leastUpperBound(leftArgTuple.get(i), rightArgTuple.get(i)))).getName();
            }

            lub = (ProductLatticeConcept) _productOntology.getEntity(lubName);
            _cachedLUBs.put(inputs, lub);
        }        
        return lub;
    }

    /** Compute the least upper bound (LUB) of a subset.
     *  The LUB of a subset is the least element in the CPO that
     *  is greater than or the same as all the elements in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object leastUpperBound(Object[] subset) {
        throw new IllegalArgumentException("Method not implemented!");
    }

    /** Return the top element of this CPO.
     *  The top element is the element in the CPO that is higher than
     *  all the other elements.
     *  @return An Object representing the top element, or
     *   <code>null</code> if the top does not exist.
     */
    public Object top() {
        return _topConcept;
    }

    /** Compute the up-set of an element in this CPO.
     *  The up-set of an element is the subset consisting of
     *  all the elements higher than or the same as the specified element.
     *  @param e An Object representing an element in this CPO.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object[] upSet(Object e) {
        throw new IllegalArgumentException("Method not implemented!");
    }    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Set the bottom concept of the product lattice.
     */
    private void _findBottom() {
        String productLatticeConceptName = new String("");
        for (Ontology ontology : _ontologyList) {
            productLatticeConceptName += ((Concept) ontology.getCompletePartialOrder().bottom()).getName();
        }
        
        _bottomConcept = (ProductLatticeConcept) _productOntology.getEntity(productLatticeConceptName);
    }
    
    /** Set the top concept of the product lattice.
     */
    private void _findTop() {
        String productLatticeConceptName = new String("");
        for (Ontology ontology : _ontologyList) {
            productLatticeConceptName += ((Concept) ontology.getCompletePartialOrder().top()).getName();
        }
        
        _topConcept = (ProductLatticeConcept) _productOntology.getEntity(productLatticeConceptName);
    }
    
    /** Validate that the input arguments are valid ProductLatticeConcepts in the same
     *  ontology before trying to compare them or find the greatest lower or least upper
     *  bound.  Throw an IllegalArgumentException if either argument is invalid.
     *  @param e1 The first ProductLatticeConcept argument.
     *  @param e2 The second ProductLatticeConcept argument.
     *  @throws IllegalArgumentException Thrown if either argument is invalid.
     */
    private void _validateInputArguments(Object e1, Object e2) {
        if (!(e1 instanceof ProductLatticeConcept) || !(e2 instanceof ProductLatticeConcept)) {
            throw new IllegalArgumentException("ProductLatticeCPO.compare: "
                    + "Arguments are not instances of ProductLatticeConcept: "
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }

        if (!(((ProductLatticeConcept) e1).getOntology().equals(((ProductLatticeConcept) e2).getOntology()))) {
            throw new IllegalArgumentException("Attempt to compare elements from two distinct ontologies: "
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }       

        List<Concept> leftArgTuple = ((ProductLatticeConcept) e1).getConceptTuple();
        List<Concept> rightArgTuple = ((ProductLatticeConcept) e2).getConceptTuple();

        if (leftArgTuple == null || leftArgTuple.isEmpty()) {
            throw new IllegalArgumentException("Attempt to compare ProductLatticeConcept " +
                    "elements where one does not have a valid " +
                    "concept tuple: arg1 = " + e1);
        }

        if (rightArgTuple == null || rightArgTuple.isEmpty()) {
            throw new IllegalArgumentException("Attempt to compare ProductLatticeConcept " +
                    "elements where one does not have a valid " +
                    "concept tuple: arg2 = " + e2);
        }

        if (leftArgTuple.size() != rightArgTuple.size()) {
            throw new IllegalArgumentException("Attempt to compare " +
                    "ProductLatticeConcept elements that do not have the same size " +
                    "concept tuple arrays even though they are in the same " +
                    "Ontology. This is an error."
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The bottom concept of the product lattice. */
    private ProductLatticeConcept _bottomConcept;
    
    /** The map of cached greatest lower bounds that have already been calculated by the CPO. */
    private Map<List<ProductLatticeConcept>, ProductLatticeConcept> _cachedGLBs;
    
    /** The map of cached least upper bounds that have already been calculated by the CPO. */
    private Map<List<ProductLatticeConcept>, ProductLatticeConcept> _cachedLUBs;
    
    /** The list of Ontologies for each element in the concept tuple of
     *  each ProductLatticeConcept in the product lattice.
     */
    private List<Ontology> _ontologyList;
    
    /** The product lattice ontology for which this is a complete partial order. */
    private ProductLatticeOntology _productOntology; 
    
    /** The bottom concept of the product lattice. */
    private ProductLatticeConcept _topConcept;
}
