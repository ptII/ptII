/*
 PtolemyInjector contains a static reference to the Guice Injector.

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
package ptolemy.actor.injection;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

///////////////////////////////////////////////////////////////////
//// PtolemyInjector
/**
 * PtolemyInjector contains a static reference to the Guice Injector.
 * The rationale for having a static reference is to avoid hurdle of passing
 * the injector to all needed parties.
 * 
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PtolemyInjector {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create an injector for the given set of modules.
     * @param modules the array of modules that contain binding that Guice Injector
     * uses to implement dependency injection.
     */
    public static synchronized void createInjector(Module... modules) {
        _instance = Guice.createInjector(modules);
    }

    /**
     * Return the PtolemyInjector.  Note that {@link #createInjector(Module...)} 
     * must be called prior to using this method.
     * @return the PtolemyInjector that was created with the supplied modules.
     */
    public static Injector getInjector() {
        if (_instance == null) {
            throw new IllegalStateException("The injector is not created.");
        }
        return _instance;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The Injector instance that is shared with all users of the PtolemyInjector class.
     */
    private static volatile Injector _instance;
}
