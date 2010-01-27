/** A map that associates a key with multiple values.

 Copyright (c) 1997-2009 The Regents of the University of California.
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
package ptolemy.data.properties.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

///////////////////////////////////////////////////////////////////
//// MultiHashMap

/**
MultiHashMap is an implementation of the MultiMap interface. It
associates a collection of objects to each key. Putting a new
object under a key adds to the associated collection. Likewise,
removing a object removes from the collection. It is possible
that the given object to remove is not contained by the collection.
In which case, no changes is made and null is returned. The items
in each collection are ordered by insertion, and duplicates are
stored in the collections.

For example, given a key K and object O1, and O2:

    MultiHashMap map = new MultiHashMap();
    map.put(K, O1);
    map.put(K, O1);
    map.put(K, O2);

then, map.size(K) would return 3. Iterating through the map returns
O1, O1, and O2 in order.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */

public class MultiHashMap extends HashMap implements MultiMap {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add the value to the collection associated with the specified key.
     * @param key The specified key.
     * @param value The specified value to add to the collection.
     * @return The value added.
     */
    public Object put(Object key, Object value) {
        ArrayList values = (ArrayList) get(key);
        if (values == null) {
            values = new ArrayList();
            super.put(key, values);
        }
        values.add(value);
        return value;
    }

    /**
     * Remove a specified value from the map. The value is removed
     * from the collection mapped to the specified key. If this is
     * the last value removed from the given key, the specified key
     * is also removed from the map. Subsequent call to get(key) will
     * return null.
     * @param key The specified key to remove the value from.
     * @param value The specified value to remove.
     * @return The value removed, or null if nothing is removed.
     */
    public Object remove(Object key, Object value) {
        Collection values = (Collection) get(key);

        if (values == null) {
            return null;
        } else {
            Object object = values.remove(value);
            if (values.size() == 0) {
                remove(key);
            }
            return object;
        }
    }

    /**
     * Return the size of the collection mapped to the specified key.
     * @param key The specified key.
     * @return The size of the collection, or zero if key is
     *  not in the map.
     */
    public int size(Object key) {
        Collection values = (Collection) get(key);

        if (values == null) {
            return 0;
        } else {
            return values.size();
        }
    }

    /**
     * Return a view of the collection containing all values in the map.
     * This is a collection containing the union of each collection
     * mapped to the keys.
     * @return A view of all values contained in this map.
     */
    public Collection values() {
        Collection result = new ArrayList();
        for (Object object : super.values()) {
            Collection values = (Collection) object;
            result.addAll(values);
        }
        return result;
    }
}
