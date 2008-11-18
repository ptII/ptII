/* A debug listener that sends messages to a stream or to standard out.

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
package ptolemy.kernel.util;

import java.io.OutputStream;
import java.io.PrintStream;

//////////////////////////////////////////////////////////////////////////
//// StreamListener

/**
 A debug listener that sends messages to a stream or to the standard output.

 @author  Edward A. Lee, Christopher Hylands
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 @see NamedObj
 @see RecorderListener

 */
public class StreamListener implements DebugListener {
    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a debug listener that sends messages to the standard output.
     */
    public StreamListener() {
        _output = System.out;
    }

    /** Create a debug listener that sends messages to the specified stream.
     *  @param out The output stream used to create the debug listener.
     */
    public StreamListener(OutputStream out) {
        _output = new PrintStream(out);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Print a string representation of the event to the stream
     *  associated with this listener.
     */
    public void event(DebugEvent event) {
        _output.println(event.toString());
    }

    /** Copy the message argument to the stream associated with
     *  the listener.  Note that a newline is appended to the
     *  end of the message.
     */
    public void message(String message) {
        _output.println(message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private PrintStream _output;
}
