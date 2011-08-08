/* An application that executes non-graphical
 models specified on the command line with a 5 second timeout

 Copyright (c) 2011 The Regents of the University of California.
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
package ptolemy.moml;

import java.io.File;

import ptolemy.actor.ActorModuleInitializer;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// MoMLSimpleTimeoutApplication

/** A simple application that reads in a .xml file as a command
 line argument, runs it and terminates the run after a time out.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (eal)
 */
public class MoMLSimpleTimeoutApplication extends MoMLSimpleApplication {

    /** Parse the xml file and run it.  After 5 seconds, call wrapup
     *  on the manager.
     *  @param xmlFileName A string that refers to an MoML file that
     *  contains a Ptolemy II model.  The string should be
     *  a relative pathname.
     *  @exception Throwable If there was a problem parsing
     *  or running the model.
     */
    public MoMLSimpleTimeoutApplication(String xmlFileName) throws Throwable {
        super(xmlFileName);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Public methods                    ////

    /** Wait for all executing runs to finish or 5 seconds to pass,
     *  then return.
     */
    public synchronized void waitForFinish() {
        long timeout = 5000;
        try {
            System.out.print("Waiting for " + timeout/1000.0 + " seconds. . .");
            wait(timeout);
            _toplevel.getDirector().finish();
            _toplevel.getDirector().stopFire();

            System.out.println(" Done.");
        } catch (InterruptedException ex) {
            // Ignore.
        }
    }
}