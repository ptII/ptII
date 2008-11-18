/*
 @Copyright (c) 1998-2005 The Regents of the University of California.
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
package diva.graph;

import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;

/**
 * An interactor for edges.
 *
 * @author         Michael Shilman
 * @author         John Reekie
 * @version        $Id$
 * @Pt.AcceptedRating Red
 */
public class EdgeInteractor extends SelectionInteractor {
    /** Create a new edge interactor.
     */
    public EdgeInteractor() {
        super();
    }

    /** Create a new edge interactor that belongs to the given
     * controller and that uses the given selection model
     */
    public EdgeInteractor(SelectionModel sm) {
        this();
        super.setSelectionModel(sm);
    }
}
