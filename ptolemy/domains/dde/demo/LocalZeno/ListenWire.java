/* A Wire actor that can notify an ExecEventListener of ExecEvents.

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
package ptolemy.domains.dde.demo.LocalZeno;

import ptolemy.domains.dde.lib.Wire;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ListenWire

/**
 A Wire actor that can notify an ExecEventListener of ExecEvents.
 In particular, the listener will be notified each time the prefire(),
 postfire() and wrapup() methods of this actor are invoked. Such
 notification is enabled by adding an ExecEventListener to this actor's
 listener list via the addListeners() method. Listeners can be removed
 via the removeListeners() method. ExecEventListeners are currently
 implemented to serve as conduits between Ptolemy II and the Diva
 graphical user interface.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Yellow (davisj)
 @see ptolemy.actor.gui.ExecEvent
 @see ptolemy.actor.gui.ExecEventListener
 */
public class ListenWire extends Wire {
    /** Construct a ListenWire actor with the specified container
     *  and name.
     * @param cont The TypedCompositeActor that contains this actor.
     * @param name The name of this actor.
     * @exception NameDuplicationException If the name of this actor
     *  duplicates that of a actor already contained by the container
     *  of this actor.
     * @exception IllegalActionException If there are errors in
     *  instantiating and specifying the type of this actor's ports.
     */
    public ListenWire(CompositeEntity cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate an ExecEvent with a state value of 2. Return the
     *  value of the postfire method of this actor's superclass.
     *  Return true if this actor is enabled to call fire(); return
     *  false otherwise.
     * @return True if this actor is enabled to call fire(); return
     *  false otherwise.
     * @exception IllegalActionException If there is an exception
     *  with the thread activity of this method.
     */
    public boolean postfire() throws IllegalActionException {
        _debug(new ExecEvent(this, ExecEvent.WAITING));

        LocalZenoUtilities.sleepProcess(this);
        return super.postfire();
    }

    /** Generate an ExecEvent with a state value of 1, cause the
     *  calling thread to sleep for 100 milliseconds and then call
     *  the superclass prefire() method. Return true if this actor
     *  is enabled to call fire(); return false otherwise.
     * @return True if this actor is enabled to call fire(); return
     *  false otherwise.
     * @exception IllegalActionException if there is an exception
     *  with the thread activity of this method.
     */
    public boolean prefire() throws IllegalActionException {
        _debug(new ExecEvent(this, ExecEvent.ACCESSING));

        LocalZenoUtilities.sleepProcess(this);
        return super.prefire();
    }

    /** Generate an ExecEvent with a state value of 3. Invoke the
     *  wrapup() method of this actor's superclass.
     * @exception IllegalActionException If there is an exception in
     *  the execution of the wrapup method of this actor's superclass.
     */
    public void wrapup() throws IllegalActionException {
        _debug(new ExecEvent(this, ExecEvent.BLOCKED));
        super.wrapup();
    }
}
