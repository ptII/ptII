/* Interface for actors.

 Copyright (c) 1997-2009 The Regents of the University of California.
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
package ptolemy.actor;

import java.util.List;

import ptolemy.actor.util.CausalityInterface;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// Actor

/**
 An Actor is an executable entity. This interface defines the common
 functionality in AtomicActor and CompositeActor.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (davisj)
 @see ptolemy.actor.CompositeActor
 @see ptolemy.actor.AtomicActor
 */
public interface Actor extends Executable, Nameable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create receivers for all necessary ports.
     *  For Atomic Actors this is done for each input port, for
     *  Composite Actors for both the input and the output ports.
     *  @exception IllegalActionException If any port throws it.
     *  @see AtomicActor#createReceivers
     *  @see CompositeActor#createReceivers
     */
    public void createReceivers() throws IllegalActionException;

    /** Return the local director, if there is one, otherwise the executive
     *  director, if there is one, otherwise null.
     *  @return The director.
     */
    public Director getDirector();

    /** Return the executive director, if there is one, otherwise return null.
     *  @return The executive director.
     */
    public Director getExecutiveDirector();

    /** Return a causality interface for this actor.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     * @exception IllegalActionException Thrown if causality interface cannot be computed.
     */
    public CausalityInterface getCausalityInterface()
            throws IllegalActionException;

    /** Return the Manager, if there is one. Otherwise, return null.
     *  @return The Manager.
     */
    public Manager getManager();

    /** Return a list of the input ports of this actor.
     *  Note that implementations should return ports directly
     *  contained by this actor, whether they are transparent or not.
     *  @return A list of input IOPort objects.
     */
    public List inputPortList();

    /** Return a new receiver of a type compatible with the executive director.
     *  This is the receiver that should be used by ports of this actor.
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException;

    /** Return a list of the output ports of this actor.
     *  Note that implementations should return ports directly
     *  contained by this actor, whether they are transparent or not.
     *  @return A list of output IOPort objects.
     */
    public List outputPortList();
}
