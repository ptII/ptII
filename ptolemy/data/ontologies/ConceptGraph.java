/* An abstract class that defines the ordering relationships in an ontology.
 *
 * Copyright (c) 2007-2010 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
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
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 */
package ptolemy.data.ontologies;

import ptolemy.graph.CPO;

///////////////////////////////////////////////////////////////////
//// ConceptGraph

/** An abstract class that defines the ordering relationships in an ontology.
 *  An ontology is a set of concepts and the relationships between them.  In a
 *  general ontology the graph describing the relationships between concepts
 *  need not be a complete partial order (CPO).  But we restrict our
 *  implementation to a CPO because we currently deal only with ontologies that
 *  can be partially ordered. This is particularly important for an ontology
 *  whose graph is a lattice, where we can use the Rehof and Mogensen algorithm
 *  to do a scalable analysis and inference on a model to assign concepts from
 *  the ontology to each element in the model.  This specialization is
 *  implemented as a
 *  {@linkplain ptolemy.data.ontologies.lattice.LatticeOntologySolver
 *  LatticeOntologySolver}, a subclass of {@linkplain OntologySolver}.
 *
 *  @see Ontology
 *  @author Thomas Mandl, Man-Kit Leung, Edward A. Lee, Ben Lickly, Dai Bui,
 *    Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 *  @see ptolemy.graph.CPO
 */
public abstract class ConceptGraph implements CPO {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the least element of this concept graph.
     *  @return The least element of this graph.
     */
    public abstract Concept bottom();

    /** Compare two concepts in the ontology. The arguments must be instances
     *  of {@link Concept}, otherwise an exception will be thrown.  This
     *  method returns one of ptolemy.graph.CPO.LOWER, ptolemy.graph.CPO.SAME,
     *  ptolemy.graph.CPO.HIGHER, ptolemy.graph.CPO.INCOMPARABLE, indicating
     *  the first argument is lower than, equal to, higher than, or
     *  incomparable with the second argument in the property hierarchy,
     *  respectively.
     *  @param e1 An instance of {@link Concept}.
     *  @param e2 An instance of {@link Concept}.
     *  @return One of CPO.LOWER, CPO.SAME, CPO.HIGHER, CPO.INCOMPARABLE.
     *  @exception IllegalArgumentException If one or both arguments are not
     *   instances of {@link Concept}.
     */
    public abstract int compare(Object e1, Object e2);

    /** Compute the down-set of an element in this concept graph.
     *
     *  Not implemented in this base class.
     *
     *  @param e An Object representing a concept in this concept graph.
     *  @return An array of Concepts of the down-set of the specified element.
     *  @exception IllegalArgumentException Always thrown in this base class.
     */
    public Concept[] downSet(Object e) {
        throw new IllegalArgumentException(_notImplementedMessage());
    }

    /** Compute the greatest element of a subset.
     *
     *  @see #leastElement(Object[])
     *  @param subset An array of Objects representing the subset.
     *  @return An Object representing the greatest element of the subset,
     *   or <code>null</code> if the greatest element does not exist.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this concept graph.
     */
    public Concept greatestElement(Object[] subset) {
        return _superlativeElement(subset, CPO.HIGHER);
    }



    /** Compute the greatest lower bound (GLB) of two elements.
     *
     *  Not implemented in this base class.
     *
     *  @param e1 An Object representing an element in this concept graph.
     *  @param e2 An Object representing an element in this concept graph.
     *  @return An Object representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException Always thrown in this base class.
     */
    public Concept greatestLowerBound(Object e1, Object e2) {
        throw new IllegalArgumentException(_notImplementedMessage());
    }

    /** Compute the greatest lower bound (GLB) of a subset.
     *
     *  @see #leastUpperBound(Object[])
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException If at least one Object is not
     *    an element of this concept graph, or greatestLowerBound is not
     *    implemented.
     */
    public Concept greatestLowerBound(Object[] subset) {
        if (subset != null && subset.length > 0) {
            Concept glb = (Concept) subset[0];
            for (Object concept : subset) {
                glb = greatestLowerBound(glb, concept);
            }
            return glb;
        } else {
            return null;
        }
    }

    /** Return whether this concept graph is a lattice.
     *  Should be true for all existing concept graphs.
     *
     *  Not implemented in this base class.
     *
     *  @return True, if the concept graph is a lattice.
     *  @exception IllegalArgumentException Always thrown in this base class.
     */
    public boolean isLattice() {
        throw new IllegalArgumentException(_notImplementedMessage());
    }

    /** Compute the least element of a subset.
     *  The least element of a subset is an element in the
     *  subset that is lower than all the other elements in the
     *  subset.
     *  @see #greatestElement(Object[])
     *  @param subset An array of Objects representing the subset.
     *  @return The least element of the subset, if it exists, and null
     *   if there is no least element of the given subset.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this concept graph.
     */
    public Concept leastElement(Object[] subset) {
        return _superlativeElement(subset, CPO.LOWER);
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  The LUB of two elements is the least element in the concept graph
     *  that is greater than or equal to both of the two elements.
     *
     *  Not implemented in this base class.
     *
     *  @param e1 An Object representing a concept in this concept graph.
     *  @param e2 An Object representing a concept in this concept graph.
     *  @return A Concept representing the LUB of the two specified
     *   elements, or <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this concept graph.
     */
    public abstract Concept leastUpperBound(Object e1, Object e2);

    /** Compute the least upper bound (LUB) of a subset.
     *  The LUB of a subset is the least element in the concept graph that
     *  is greater than or equal to all the elements in the
     *  subset.
     *  @see #greatestLowerBound(Object[])
     *  @param subset An array of Objects representing the subset.
     *  @return The least upper bound of the given subset, if it exists,
     *   and null if it does not.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this concept graph.
     */
    public Concept leastUpperBound(Object[] subset) {
        if (subset != null && subset.length > 0) {
            Concept lub = (Concept) subset[0];
            for (Object concept : subset) {
                lub = leastUpperBound(lub, concept);
            }
            return lub;
        } else {
            return null;
        }
    }

    /** Return the greatest element in this concept graph.
     *  @return The greatest element in this concept graph.
     */
    public abstract Concept top();

    /** Compute the up-set of an element in this concept graph.
     *  The up-set of an element is the subset consisting of
     *  all the elements greater than or equal to the specified element.
     *  @param e An Object representing an element in this concept graph.
     *  @return An array of Concepts of the up-set of the
     *   specified element.
     *  @exception IllegalArgumentException Always thrown in this base class.
     */
    public Concept[] upSet(Object e) {
        throw new IllegalArgumentException(_notImplementedMessage());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////

    /** Return the concept from the given subset of concepts that is the
     *  least or greatest.
     *
     *  @param subset The set of concepts in question. 
     *  @param direction The directionality of the extremity. CPO.HIGHER
     *    for greatest, and CPO.LOWER for least.
     *  @return The concept at the extremity, if it exists. Null, if no
     *    such concept exists.
     */
    private Concept _superlativeElement(Object[] subset, int direction) {
        if (subset != null && subset.length > 0) {
            Concept superlative = (Concept) subset[0];
            for (Object concept : subset) {
                if (compare(concept, superlative) == CPO.INCOMPARABLE) {
                    return null;
                } else if (compare(concept, superlative) == direction) {
                    superlative = (Concept) concept;
                }
            }
            return superlative;
        }
        return null;
    }

    /** Return a string indicating that the calling method is unimplemented.
     *  @return The string with the unimplemented error message.
     */
    private String _notImplementedMessage() {
        String methodName = Thread.currentThread().getStackTrace()[1]
                .getMethodName();
        String className = this.getClass().getSimpleName();
        return methodName + " method not implemented in class " + className
                + "!";
    }
}
