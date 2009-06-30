/* Superclass of any exception to be thrown in model transformation.

@Copyright (c) 2007-2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt;

import ptolemy.kernel.util.KernelException;

//////////////////////////////////////////////////////////////////////////
//// GraphTransformationException

/**
 Superclass of any exception to be thrown in model transformation.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GraphTransformationException extends KernelException {

    /** Construct an exception with a no specific detail message.
     */
    public GraphTransformationException() {
    }

    /** Construct an exception with a detail message.
     *
     *  @param message The message.
     */
    public GraphTransformationException(String message) {
        super(null, null, null, message);
    }

    /** Construct an exception with a detail message and a cause.
     *
     *  @param cause The cause of this exception.
     *  @param message The message.
     */
    public GraphTransformationException(String message, Throwable cause) {
        super(null, null, cause, message);
    }

    /** Construct an exception with a cause.
     *
     *  @param cause The cause of this exception.
     */
    public GraphTransformationException(Throwable cause) {
        super(null, null, cause, null);
    }

}
