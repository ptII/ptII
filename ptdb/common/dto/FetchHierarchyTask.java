/*
@Copyright (c) 2010 The Regents of the University of California.
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
/*
 *
 */
package ptdb.common.dto;

import java.util.ArrayList;

///////////////////////////////////////////////////////////////////
//// FetchHierarchyTask

/**
 * Contain the list of models to fetch the parent hierarchy for them.
 *
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class FetchHierarchyTask extends Task {

    /**
     *  Construct an instance of FetchHierarchyTask
     *  and set it as a select task.
     */
    public FetchHierarchyTask() {
        _isUpdateTask = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                                         ////

    /**
     * Return the list of models for which we need
     * to fetch parent hierarchy.
     * @see #setModelsList
     * @return List of models for which we need to
     * fetch hierarchy.
     */
    public ArrayList<XMLDBModel> getModelsList() {
        return _modelsList;
    }

    /**
     * Set the list of models for which we need
     * to fetch parent hierarchy.
     * @see #getModelsList
     * @param modelsList List of models for which we need to
     * fetch hierarchy.
     */
    public void setModelsList(ArrayList<XMLDBModel> modelsList) {
        _modelsList = modelsList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                                ////
    /**
     * List of models for which we need to
     * fetch hierarchy.
     */
    private ArrayList<XMLDBModel> _modelsList;

}
