/*
 @Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.caltrop.ddi;

//////////////////////////////////////////////////////////////////////////
//// DDIException

/**
 A general-purpose exception used in the <tt>ddi</tt> package, used to
 indicate an error during domain dependent interpretation.

 @author Christopher Chang
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class DDIException extends RuntimeException {
    /**
     * Create a <tt>DDIException</tt>.
     */
    public DDIException() {
    }

    /**
     * Create a <tt>DDIException</tt> with an error message.
     * @param msg The error message.
     */
    public DDIException(String msg) {
        super(msg);
    }

    /**
     * Create a <tt>DDIException</tt> with an error message and a cause.
     * @param msg The error message.
     * @param cause The cause.
     */
    public DDIException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a <tt>DDIException</tt> with a cause.
     * @param cause The cause.
     */
    public DDIException(Throwable cause) {
        super(cause);
    }
}
