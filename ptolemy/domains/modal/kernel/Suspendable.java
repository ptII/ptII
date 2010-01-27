/* An interface for actors that can be suspended and resumed.

 Copyright (c) 1999-2009 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import ptolemy.actor.Executable;
import ptolemy.actor.util.Time;
import ptolemy.kernel.util.IllegalActionException;

/** An interface for actors that can be suspended and resumed.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating red (eal)
 @Pt.AcceptedRating red (eal)
 */
public interface Suspendable extends Executable {
    
    /** Return the accumulated time that the actor has been suspended
     *  since the last call to initialize(), or null if it has never
     *  been suspended.
     *  @return The total time between calls to suspend and subsequent
     *   calls to resume, or null if the actor has not been suspended.
     */
    public Time accumulatedSuspendTime();
    
    /** Resume the actor at the specified time. If the actor has not
     *  been suspended since the last call to initialize(), then this
     *  has no effect.
     *  @param time The time at which the actor is resumed.
     *  @exception IllegalActionException If the resume cannot be completed.
     */
    public void resume(Time time) throws IllegalActionException;
    
    /** Suspend the actor at the specified time.
     *  If the actor is already suspended, then it remains suspended
     *  but the accumulated suspend time is incremented by the time
     *  since it was last suspended.
     *  @param time The time at which the actor is suspended.
     *  @exception IllegalActionException If the suspend cannot be completed.
     */
    public void suspend(Time time) throws IllegalActionException;
}
