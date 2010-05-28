/* An interface for tokens that can be partially ordered.

 Copyright (c) 2010 The Regents of the University of California.
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

import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.kernel.util.IllegalActionException;

/** An interface for tokens that can be partially ordered.

 FIXME: Should this be a class below {@link ptolemy.data.Token} but
   above {@link ptolemy.data.ScalarToken} and
   {@link ptolemy.data.ConceptToken} rather than an interface?

 The default implementation of visitRelationalNode
 {@link ptolemy.data.expr.ParseTreeEvaluator#visitRelationalNode(ASTPtRelationalNode)}
 uses this interface, so any tokens that implement this interface can be used
 with the inequality operators (<, <=, >, and >=) in the Ptolemy expression
 language.

 @author Ben Lickly
 @since Ptolemy II 9.0
 @Pt.ProposedRating Red (blickly)
 @Pt.AcceptedRating Red (blickly)
 */
public interface PartiallyOrderedToken {
    /** Check whether the value of this token is strictly less than that of the
     *  argument token.
     *  
     *  Only a partial order is assumed, so !(a < b) need not imply (a >= b).
     *
     *  @param rightArgument The token on greater than side of the inequality.
     *  @return BooleanToken.TRUE, if this token is less than the
     *    argument token. BooleanToken.FALSE, otherwise. 
     *  @throws IllegalActionException If the tokens are incomparable.
     */
    public abstract BooleanToken isLessThan(PartiallyOrderedToken rightArgument)
            throws IllegalActionException;
}
