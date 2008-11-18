/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.util.List;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.MoMLChangeRequest;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class GTAttribute extends Attribute {

    public GTAttribute() {
    }

    public GTAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    public GTAttribute(Workspace workspace) {
        super(workspace);
    }

    protected void _checkContainerClass(NamedObj container,
            Class<? extends CompositeEntity> containerClass, boolean deep)
            throws IllegalActionException {
        while (deep && container != null
                && !containerClass.isInstance(container)
                && !(container instanceof EntityLibrary)) {
            container = container.getContainer();
            if (container instanceof EntityLibrary) {
                return;
            }
        }

        if (container == null ||
                !containerClass.isInstance(container) &&
                !(container instanceof EntityLibrary)) {
            _deleteThis();
            throw new IllegalActionException(getClass().getSimpleName()
                    + " can only be added to " + containerClass.getSimpleName()
                    + ".");
        }
    }

    protected void _checkUniqueness(NamedObj container)
            throws IllegalActionException {
        if (container instanceof EntityLibrary) {
            return;
        }

        try {
            container.workspace().getReadAccess();
            List<?> attributeList = container.attributeList(getClass());
            for (Object attributeObject : attributeList) {
                if (attributeObject != this) {
                    _deleteThis();
                    throw new IllegalActionException("Only 1 "
                            + getClass().getSimpleName() + " can be used.");
                }
            }
        } finally {
            container.workspace().doneReading();
        }
    }

    protected void _setIconDescription(String iconDescription) {
        String moml = "<property name=\"_iconDescription\" class="
                + "\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "  <configure>" + iconDescription + "</configure>"
                + "</property>";
        MoMLChangeRequest request = new MoMLChangeRequest(this, this, moml);
        request.execute();
    }

    private void _deleteThis() {
        String moml = "<deleteProperty name=\"" + getName() + "\"/>";
        requestChange(new MoMLChangeRequest(this, getContainer(), moml));
    }
}
