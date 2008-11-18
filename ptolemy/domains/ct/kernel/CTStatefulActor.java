/* An interface for actors that can remembers their state and go back
 to the state that is remembered.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ct.kernel;

import ptolemy.actor.Actor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// CTStatefulActor

/**
 Interface for actors that have states. The state of the actor can be
 marked (saved). The saved state can be restored so that an actor can go
 back to its previously marked state. This feature is used for rolling
 back the simulation when needed, which is essential when embedding
 CT subsystem in a discrete event system.
 <P>
 The interface defines two methods, markState() and goToMarkedState().
 If the markState() method is called, the current state of the actor,
 for example values of the local variables, should be remembered. When the
 goToMarkedState() method is called after that, the marked states
 should be restored.
 @author  Jie Liu
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (liuj)
 @Pt.AcceptedRating Green (yuhong)
 */
public interface CTStatefulActor extends Actor {
    /** Implementations of this method should go to the marked state.
     *  If there's no marked state, throws an exception.
     *  @exception IllegalActionException If there were no marked state.
     */
    public void goToMarkedState() throws IllegalActionException;

    /** Implementations of this method should mark the current state
     *  of the actor.
     */
    public void markState();
}
