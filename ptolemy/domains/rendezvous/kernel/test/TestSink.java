/* This star discards whatever it receives at the input

 Copyright (c) 1997-2007 The Regents of the University of California.
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
package ptolemy.domains.rendezvous.kernel.test;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TestSink

/**

 @author Mudit Goel
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestSink extends AtomicActor {
    /** Constructor Adds ports to the star
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public TestSink(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the record, and reset the iteration count to zero.
     */
    public void clear() {
        _list = new StringBuffer(1024);
    }

    /** Writes successive integers to the output
     */
    public void fire() throws IllegalActionException {
        Token data;

        while (true) {
            data = _input.get(0);
            _list.append(((IntToken) data).intValue());

            //System.out.println("Sink discarded "+data.intValue());
        }
    }

    public static String getData() {
        return _list.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* Input port */
    private IOPort _input;

    private static StringBuffer _list = new StringBuffer(1024);
}
