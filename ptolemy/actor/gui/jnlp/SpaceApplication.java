/* Wrapper class to start up the Space version of Vergil

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.actor.gui.jnlp;

//////////////////////////////////////////////////////////////////////////
//// SPaceApplication

/** A wrapper class that calls eventually calls
 ptolemy.vergil.VergilApplication for use with Java Network Launching
 Protocol (JNLP) aka Web Start to run the Office Space application

 <p>This class is very similar to other classes that invoke
 Vergil applications under Web Start because each application
 needs to have its own jar file.

 <p>In Web Start 1.0.1, it is necessary to sign the application
 if it is to have access to the local disk etc.  The way that this is
 handled is that the .jnlp file that defines the application
 is copied to the .jar file that defines the main() method for
 the application and the .jar file is signed.  Unfortunately, this means
 that two Web Start applications cannot share one jar file, so
 we create these wrapper classes that call the appropriate main class.
 <p>For more information about JNLP, see $PTII/mk/jnlp.in.

 @see MenuApplication

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SpaceApplication extends MenuApplication {
    /** Main method that sets user.dir as necessary and calls
     *  MenuApplication.main().
     *  @param args Arguments to be passed on to MenuApplication.main()
     */
    public static void main(final String[] args) {
        // See the class comment of MenuApplication
        // about why we set the security manager to null.
        System.setSecurityManager(null);
        MenuApplication.main(args);
    }
}
