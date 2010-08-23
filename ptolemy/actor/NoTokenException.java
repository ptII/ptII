/* A class indicating that a receiver failed to contain a token to return.

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
package ptolemy.actor;

import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// NoTokenException

/**
 This exception is thrown when an attempt is made to get a token
 from a receiver that does not contain one.
 To avoid this exception, code should use the hasToken() method in the
 Receiver interface to determine if there is a token waiting.

 @author Lukito Muliadi
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (lmuliadi)
 @Pt.AcceptedRating Green (neuendor)
 @see Receiver
 */
public class NoTokenException extends KernelRuntimeException {
    /** Construct an exception with the given message.
     *  @param message The message.
     */
    public NoTokenException(String message) {
        super(message);
    }

    /** Construct an exception originating from the given object,
     *  with the given error message.
     *  @param object The originating object.
     *  @param message The message.
     */
    public NoTokenException(Nameable object, String message) {
        super(object, message);
    }


    /** Construct an exception originating from the given object,
     *  with the given error message.
     *  @param object The originating object.
     *  @param cause The cause of this exception.
     *  @param message The message.
     */
    public NoTokenException(Nameable object, Throwable cause, String detail) {
        super(object, null, cause, detail);
    }
}
