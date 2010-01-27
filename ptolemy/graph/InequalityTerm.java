/** An interface for a term in an inequality over a CPO.

 Copyright (c) 1997-2008 The Regents of the University of California.
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
package ptolemy.graph;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// InequalityTerm

/**
 An interface for a term in an inequality over a CPO.
 A term is either a constant, a variable, or a function.
 In some applications, a term may be associated with an Object. For
 example, the value of the term may represent a certain characteristic
 of an Object, and it is necessary to get a reference of that Object
 from a term. This can be done through the getAssociatedObject() method
 of this interface.

 @author Yuhong Xiong
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (yuhong)
 @Pt.AcceptedRating Green (kienhuis)
 @see CPO
 */
public interface InequalityTerm {
    /** Return the Object associated with this term. If this term is
     *  not associated with a particular Object, or it is not necessary
     *  to obtain the reference of the associated Object, this method
     *  can return <code>null</code>.
     *  @return The associated Object.
     */
    public Object getAssociatedObject();

    /** Return the value of this term.  If this term is a constant,
     *  return that constant; if this term is a variable, return the
     *  current value of that variable; if this term is a function,
     *  return the evaluation of that function based on the current
     *  value of variables in the function.
     *  @return An Object representing an element in the underlying CPO.
     *  @exception IllegalActionException If the value of this
     *  inequality term is not valid.
     *  @see #setValue(Object)
     */
    public Object getValue() throws IllegalActionException;

    /** Return an array of variables contained in this term.
     *  If this term is a constant, return an array of size zero;
     *  if this term is a variable, return an array of size one that
     *  contains this variable; if this term is a function, return an
     *  array containing all the variables in the function.
     *  @return An array of InequalityTerms
     */
    public InequalityTerm[] getVariables();

    /** Initialize the value of this term to the specified CPO element.
     *  If this InequalityTerm is a simple variable that can be set to any
     *  CPO element, set the value of the variable to the specified argument.
     *  In this case, this method is equivalent to <code>setValue()</code>
     *  with the same argument.
     *  In some applications, this term is a structured object that only part
     *  of it is a simple variable. In this case, set that variable part to
     *  the specified argument.
     *  @param e An Object representing an element in the underlying CPO.
     *  @exception IllegalActionException If this term is not a variable.
     */
    public void initialize(Object e) throws IllegalActionException;

    /** Check whether this term can be set to a specific element of the
     *  underlying CPO. Only variable terms are settable, constant
     *  and function terms are not.
     *  @return True if this term is a variable;
     *  false otherwise.
     */
    public boolean isSettable();

    /** Check whether the current value of this term is acceptable,
     *  and return true if it is.
     *  @return True if the current value is acceptable.
     */
    public boolean isValueAcceptable();

    /** Set the value of this term to the specified CPO element.
     *  Only terms consisting of a single variable can have their
     *  values set.
     *  @param e An Object representing an element in the
     *   underlying CPO.
     *  @exception IllegalActionException If this term is not a variable.
     *  @see #getValue()
     */
    public void setValue(Object e) throws IllegalActionException;
}
