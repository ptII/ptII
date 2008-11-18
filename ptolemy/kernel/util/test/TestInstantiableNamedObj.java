/*
 Copyright (c) 2006 The Regents of the University of California.
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
 */

package ptolemy.kernel.util.test;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class TestInstantiableNamedObj extends NamedObj implements Instantiable {

    public TestInstantiableNamedObj() {
        super();
    }

    public TestInstantiableNamedObj(String name) throws IllegalActionException {
        super(name);
    }

    public TestInstantiableNamedObj(Workspace workspace) {
        super(workspace);
    }

    public TestInstantiableNamedObj(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
    }

    public List getChildren() {
        List results = new LinkedList();
        Iterator attributes = attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            results.add(new WeakReference(attribute));
        }
        return results;
    }

    public Instantiable getParent() {
        return null;
    }

    public Instantiable instantiate(NamedObj container, String name)
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
        return null;
    }

    public boolean isClassDefinition() {
        return false;
    }

}
