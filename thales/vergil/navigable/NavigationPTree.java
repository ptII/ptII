/* Navigation P tree
 Copyright (c) 2003-2005 THALES.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THALES BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
 OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THALES HAS BEEN
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 THALES SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THALES HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

 Created on 01 sept. 2003

 */
package thales.vergil.navigable;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.tree.PTree;
import thales.vergil.SingleWindowApplication;

/**
 <p>Titre : NavigationPTree</p>
 <p>Description : A navigation tree to browse a Ptolemy model</p>
 <p>Soci&eacute;t&eacute; : Thales Research and technology</p>
 @author J&eacute;r&ocirc;me Blanc & Benoit Masson  01 sept. 2003
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (jerome.blanc)
 @Pt.AcceptedRating Red (cxh)
 */
public class NavigationPTree extends PTree {
    /**
     * Most of the time, we use the tree of the ptolemy.vergil.tree package,
     * according to the chosen model, detail level can vary
     *
     * @param model the used model to create the tree
     */
    public NavigationPTree(TreeModel model) {
        super(model);
        addTreeSelectionListener(new selectionListener(this));
        addTreeExpansionListener(new expandListener(this));

        if (model instanceof NavigationTreeModel) {
            NavigationTreeModel navModel = (NavigationTreeModel) model;
            navModel.register(this);
        }
    }

    /**
     * This listener intends to get the selection from the user, open the correst model but
     * also to inform all the other referenced Navigation tree of this event
     *
     * @author masson
     *
     */
    private static class selectionListener implements TreeSelectionListener {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        private NavigationPTree _jTree = null;

        public selectionListener(NavigationPTree sptree) {
            _jTree = sptree;
        }

        public void valueChanged(TreeSelectionEvent e) {
            NamedObj obj = (((NamedObj) e.getPath().getLastPathComponent()));

            if ((obj != null) && (_jTree.getSelectionPath() != null)) {
                if (obj instanceof CompositeEntity) {
                    try {
                        SingleWindowApplication._mainFrame.getConfiguration()
                                .openModel(obj);
                        ((NavigationTreeModel) _jTree.getModel())
                                .setSelectedItem(_jTree.getSelectionPath());
                    } catch (IllegalActionException e1) {
                        e1.printStackTrace();
                    } catch (NameDuplicationException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * This expandListener inform all other Tree of the Path expanded or collapse so that all Tree
     * have the same expand/collapse state.
     *
     * @author masson
     *
     */
    private static class expandListener implements TreeExpansionListener {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
        private NavigationPTree _jTree = null;

        public expandListener(NavigationPTree sptree) {
            _jTree = sptree;
        }

        /* (non-Javadoc)
         * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
         */
        public void treeCollapsed(TreeExpansionEvent event) {
            TreePath aPath = event.getPath();

            if (aPath != null) {
                ((NavigationTreeModel) _jTree.getModel()).expandPath(aPath,
                        true);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
         */
        public void treeExpanded(TreeExpansionEvent event) {
            TreePath aPath = event.getPath();

            if (aPath != null) {
                ((NavigationTreeModel) _jTree.getModel()).expandPath(aPath,
                        false);
            }
        }
    }
}
