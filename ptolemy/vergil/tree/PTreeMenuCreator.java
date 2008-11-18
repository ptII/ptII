/* An abstract menu factory that creates context menus using item factories

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.vergil.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreePath;

import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.MenuItemFactory;
import diva.gui.toolbox.JContextMenu;

//////////////////////////////////////////////////////////////////////////
//// PTreeMenuCreator

/**
 A mouse listener that creates context menus for a PTree using menu
 item factories.  When asked to create a context menu, This class
 determines the ptolemy object associated with the point in the tree
 that was clicked on.  Then it passes the ptolemy object to each menu
 item factory that it contains to add the menu items.  Lastly, it pops
 up the resulting menu.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class PTreeMenuCreator extends MouseAdapter {
    /** Create a new menu factory that contains no menu item factories.
     */
    public PTreeMenuCreator() {
        _factoryList = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a menu item factory to this creator.
     *  @param factory The menu item factory to add.
     */
    public void addMenuItemFactory(MenuItemFactory factory) {
        _factoryList.add(factory);
    }

    /** Create an instance of the menu associated with this factory.
     *  @param e The mouse event.  If the mouse event is
     *  a popup event, then menu is created and shown.
     */
    public void mousePressed(MouseEvent e) {
        _doEvent(e);
    }

    /** Create an instance of the menu associated with this factory.
     *  @param e The mouse event.  If the mouse event is
     *  a popup event, then menu is created and shown.
     */
    public void mouseReleased(MouseEvent e) {
        _doEvent(e);
    }

    /** Return the list of menu item factories.
     * @return An unmodifiable list.
     */
    public List menuItemFactoryList() {
        return Collections.unmodifiableList(_factoryList);
    }

    /** Remove the given menu item factory from the factory list.
     *  @param factory The factory to be removed.
     */
    public void removeMenuItemFactory(MenuItemFactory factory) {
        _factoryList.remove(factory);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Process a mousePressed or mouseReleased event.
     */
    private void _doEvent(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }

        PTree tree = (PTree) e.getComponent();
        TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
        Object object = treePath.getLastPathComponent();

        if (object instanceof NamedObj) {
            NamedObj namedObj = (NamedObj) object;
            JContextMenu menu = new JContextMenu(namedObj, namedObj
                    .getFullName());
            Iterator i = menuItemFactoryList().iterator();

            while (i.hasNext()) {
                MenuItemFactory factory = (MenuItemFactory) i.next();
                factory.create(menu, namedObj);
            }

            menu.show(tree, e.getX(), e.getY());
            e.consume();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    /** The menu item factories.
     */
    private List _factoryList;
}
