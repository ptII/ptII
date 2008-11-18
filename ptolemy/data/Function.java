/* Token that contains a function.

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// FunctionToken

/** The interface for functions contained by function tokens.

 @author Xiaojun Liu, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 */
public interface Function {
    /** Apply the function to the list of arguments, which are tokens.
     *  @param arguments The list of arguments.
     *  @return The result of applying the function to the given
     *   arguments.
     *  @exception IllegalActionException If thrown during evaluating
     *   the function.
     */
    public Token apply(Token[] arguments) throws IllegalActionException;

    /** Return the number of arguments of the function.
     *  @return The number of arguments of the function.
     */
    public int getNumberOfArguments();

    /** Return true if this function is congruent to the given
     *  function.  Classes should implement this method so that
     *  two functions are congruent under any renaming of the
     *  bound variables of the function.  For simplicity, a
     *  function need only be congruent to other functions of the
     *  same class.
     *  @param function The function to check congruency against.
     *  @return True if this function is congruent with the given function.
     */
    public boolean isCongruent(Function function);

    /** Return a string representation.
     *  @return A string representation of this function.
     */
    public String toString();
}
