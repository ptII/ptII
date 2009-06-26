/* An event representing the various changes in the state of a process in PN.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.domains.pn.kernel.event;

import ptolemy.actor.Actor;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// PNProcessEvent

/**
 An event passed from a process executing under the PN semantics to a
 PNProcessListener. This is used to
 represent an event that happened during the execution of a topology.
 This event contains two pieces of information:  the actor under the control
 of the process and an exception that might be thrown.
 The exception might not be a valid reference.

 @author Mudit Goel
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (mudit)
 @Pt.AcceptedRating Red (cxh)
 */
public class PNProcessEvent {
    /** Create a new event.
     *  @param actor The actor
     *  @param state The state of the actor, should be one of
     *  PROCESS_BLOCKED, PROCESS_FINISHED, PROCESS_PAUSED
     *  or PROCESS_RUNNING.
     */
    public PNProcessEvent(Actor actor, int state) {
        this(actor, state, 0);
    }

    /** Create a new event that corresponds to an exception
     *  caught by the process.
     *  @param actor The actor.
     *  @param state The state of the actor, should be one of
     *  PROCESS_BLOCKED, PROCESS_FINISHED, PROCESS_PAUSED
     *  or PROCESS_RUNNING.
     *  @param cause The cause of the state.
     */
    public PNProcessEvent(Actor actor, int state, int cause) {
        _actor = actor;

        if ((state < PROCESS_BLOCKED) || (state > PROCESS_RUNNING)) {
            throw new InternalErrorException("state '" + state
                    + "' is incorrect, it must be one of " + PROCESS_BLOCKED
                    + ", " + PROCESS_FINISHED + ", " + PROCESS_PAUSED + " or "
                    + PROCESS_RUNNING);
        }

        _state = state;
        _cause = cause;
    }

    /** Create a new event that corresponds to an exception
     *  caught by the process.
     *  @param actor The actor.
     *  @param exception The exception.
     */
    public PNProcessEvent(Actor actor, Exception exception) {
        _actor = actor;
        _state = PROCESS_FINISHED;
        _cause = FINISHED_WITH_EXCEPTION;
        _exception = exception;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public methods                   /////

    /** Return the actor corresponding to the process that generated
     *  the event.
     *  @return the actor corresponding to the process that generated
     *  the event.
     */
    public Actor getActor() {
        return _actor;
    }

    /** Return the cause of the blocking state.
     *  @return an integer representing the cause of the blocking state.
     */
    public int getBlockingCause() {
        return _cause;
    }

    /** Return the current state.
     *  @return an integer representing the current state.
     */
    public int getCurrentState() {
        return _state;
    }

    /** Return the exception associated with the event.
     *  @return The exception associated with the event.
     */
    public Exception getException() {
        return _exception;
    }

    /** Return the cause of the finishing state.
     *  @return an integer representing the cause of the finishing state
     */
    public int getFinishingCause() {
        return _cause;
    }

    /** Return the string value of this event.
     *  @return The string value of this event.
     */
    public String toString() {
        String result;
        String state = null;
        String cause = null;

        if (_state == PROCESS_BLOCKED) {
            state = "PROCESS_BLOCKED";

            if (_cause == BLOCKED_ON_DELAY) {
                cause = "BLOCKED_ON_DELAY";
            } else if (_cause == BLOCKED_ON_MUTATION) {
                cause = "BLOCKED_ON_MUTATION";
            } else if (_cause == BLOCKED_ON_READ) {
                cause = "BLOCKED_ON_READ";
            } else if (_cause == BLOCKED_ON_WRITE) {
                cause = "BLOCKED_ON_WRITE";
            } else {
                cause = "BLOCKING_CAUSE_UNKNOWN";
            }

            result = "State of " + ((Entity) _actor).getFullName() + " is "
                    + state + " and the cause = " + cause;
        } else if (_state == PROCESS_FINISHED) {
            state = "PROCESS_FINISHED";

            if (_cause == FINISHED_ABRUPTLY) {
                cause = "FINISHED_ABRUPTLY";
            } else if (_cause == FINISHED_PROPERLY) {
                cause = "FINISHED_PROPERLY";
            } else if (_cause == FINISHED_WITH_EXCEPTION) {
                cause = "FINISHED_WITH_EXCEPTION with "
                        + ((_exception == null) ? "null exception"
                                : ("exception " + _exception));
            } else {
                cause = "FINISHED_CAUSE_UNKNOWN";
            }

            result = "State of " + ((Entity) _actor).getFullName() + " is "
                    + state + " and the cause = " + cause;
        } else if (_state == PROCESS_PAUSED) {
            state = "PROCESS_PAUSED";
            result = "State of " + ((Entity) _actor).getFullName() + " is "
                    + state;
        } else if (_state == PROCESS_RUNNING) {
            state = "PROCESS_RUNNING";
            result = "State of " + ((Entity) _actor).getFullName() + " is "
                    + state;
        } else {
            state = "UNKNOWN_PROCESS_STATE";
            result = "State of " + ((Entity) _actor).getFullName() + " is "
                    + state;
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   public variables                  /////

    /** A process is blocked on a delay. */
    public static final int BLOCKED_ON_DELAY = 111;

    /** A process is blocked on a mutation. */
    public static final int BLOCKED_ON_MUTATION = 112;

    /** A process is blocked on a read. */
    public static final int BLOCKED_ON_READ = 113;

    /** A process is blocked on a write. */
    public static final int BLOCKED_ON_WRITE = 114;

    /** A process finished abruptly. */
    public static final int FINISHED_ABRUPTLY = 734;

    /** A process finished properly. */
    public static final int FINISHED_PROPERLY = 735;

    /** A process finished with an exception. */
    public static final int FINISHED_WITH_EXCEPTION = 736;

    // These are legitimate states
    /** The process is in the blocked state. */
    public static final int PROCESS_BLOCKED = 367;

    /** The process is in the finished state. */
    public static final int PROCESS_FINISHED = 368;

    /** The process is in the paused state. */
    public static final int PROCESS_PAUSED = 369;

    /** The process is in the running state. */
    public static final int PROCESS_RUNNING = 370;

    ///////////////////////////////////////////////////////////////////
    ////                   private variables                      /////
    private Actor _actor = null;

    private int _cause = 0;

    private Exception _exception = null;

    private int _state = 0;
}
