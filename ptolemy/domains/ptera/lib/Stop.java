/* An event to stop execution of the whole model.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.domains.ptera.lib;

import ptolemy.actor.Manager;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.domains.ptera.kernel.PteraController;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// Stop

/**
 An event to stop execution of the whole model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Stop extends Event {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public Stop(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        isFinalEvent.setToken(BooleanToken.TRUE);
        isFinalEvent.setVisibility(Settable.NOT_EDITABLE);
        isFinalEvent.setPersistent(false);
    }

    /** Process this event and stop the execution.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If a manager cannot be found, or if
     *   thrown by the superclass.
     */
    public RefiringData fire(Token arguments) throws IllegalActionException {
        PteraController controller = getController();

        Manager manager = controller.getManager();
        if (manager != null) {
            manager.finish();
        } else {
            throw new IllegalActionException(this,
                    "Cannot stop without a Manager.");
        }

        return null;
    }
}
