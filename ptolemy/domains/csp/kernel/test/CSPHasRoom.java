/* CSPHasRoom

 Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.domains.csp.kernel.test;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.domains.csp.kernel.CSPReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CSPHasRoom

/**

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)

 */
public class CSPHasRoom extends CSPPut {
    /**
     */
    public CSPHasRoom(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void fire() throws IllegalActionException {
        Receiver[][] rcvrs = outputPort.getRemoteReceivers();
        CSPReceiver rcvr = null;

        for (int i = 0; i < rcvrs.length; i++) {
            for (int j = 0; j < rcvrs[i].length; j++) {
                rcvr = (CSPReceiver) rcvrs[i][j];
            }
        }

        while (!rcvr.hasRoom()) {
            ;
        }

        if (rcvr.hasRoom()) {
            _hasRoom = true;
        }
    }

    /**
     */
    public boolean hasRoom() {
        return _hasRoom;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _hasRoom = false;
}
