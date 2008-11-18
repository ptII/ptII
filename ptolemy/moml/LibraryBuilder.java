/**
 *  '$RCSfile$'
 *  '$Author$'
 *  '$Date$'
 *  '$Revision$'
 *
 *  For Details:
 *  http://www.kepler-project.org
 *
 *  Copyright (c) 2006 The Regents of the
 *  University of California. All rights reserved. Permission is hereby granted,
 *  without written agreement and without license or royalty fees, to use, copy,
 *  modify, and distribute this software and its documentation for any purpose,
 *  provided that the above copyright notice and the following two paragraphs
 *  appear in all copies of this software. IN NO EVENT SHALL THE UNIVERSITY OF
 *  CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
 *  OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 *  DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY
 *  DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE
 *  SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 *  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 *  ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.moml;

import java.util.List;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;

/**
 * An abstract class that defines the interface for LibraryBuilder.
 * The main purpose of a library builder is to create a moml library
 * from something other than a moml document.  The first
 * implementation of this class is going to be to create a library
 * from a directory of ksw files.
 * @author Chad Berkley
 * @version $Id$
 * @since Ptolemy II 5.2
 * @Pt.ProposedRating Red (berkley)
 * @Pt.AcceptedRating Red (berkley)
 */
public abstract class LibraryBuilder {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Construct a library builder.
     */
    public LibraryBuilder() {
        // Nothing to do, but makes it easier to use reflection.
    }

    /**
     * Add a list of Attributes.
     * @param attributeList The list of Attributes.
     */
    public void addAttributes(List attributeList) {
        _attributes = attributeList;
    }

    /**
     * Get the list of Attributes associated with this LibraryBuilder.
     * @return The list of Attributes.
     */
    public List getAttributes() {
        return _attributes;
    }

    /**
     * Build the library.  This should be built in the form of a
     * ComponentEntity See the VergilApplication code if you want an
     * example of what the ComponentEntity should look like.
     * @param workspace The workspace.
     * @return ComponentEntity
     * @exception Exception
     */
    public abstract CompositeEntity buildLibrary(Workspace workspace)
            throws Exception;

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                   ////

    /** Attributes that can be added to a LibraryBuilder via moml configuration.
     */
    protected List _attributes;
}
