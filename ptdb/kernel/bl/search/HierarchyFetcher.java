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
package ptdb.kernel.bl.search;

import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.exception.DBExecutionException;

///////////////////////////////////////////////////////////////////
//// HierarchyFetcher

/**
 * Fetch the referencing hierarchy for the models.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class HierarchyFetcher extends AbstractSearcher implements
        ResultHandler, AbstractDBSearcher {

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected boolean _isSearchCriteriaSet() {

        // There is no criteria need to be set in this searcher, so always
        // returns true.
        return true;
    }

    /**
     * Handle the model results passed to this class.
     * Go to the database to fetch all the referencing hierarchy
     * for the passed results.
     *
     * @exception DBExecutionException Thrown by the DBConnection when
     * unexpected problem happens during the execution of DB query tasks.
     */
    @Override
    protected void _search() throws DBExecutionException {

        // create the FetchHierarchyTask
        FetchHierarchyTask fetchHierarchyTask = new FetchHierarchyTask();

        fetchHierarchyTask.setModelsList(_previousResults);

        // call the executeFetchHierarchyTask() method from the DBConnection class
        // get the results returned by the executeFetchHierarchyTask() method
        _currentResults = _dbConnection
                .executeFetchHierarchyTask(fetchHierarchyTask);

    }

}
