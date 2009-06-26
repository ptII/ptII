/*

@Copyright (c) 2007-2009 The Regents of the University of California.
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

import java.util.Collection;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RelationHidingAttribute extends Parameter implements GTAttribute {

    /**
     * @param container
     * @param name
     * @exception NameDuplicationException
     * @exception IllegalActionException
     */
    public RelationHidingAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setTypeEquals(BaseType.BOOLEAN);
        setToken(BooleanToken.getInstance(!DEFAULT));
    }

    /** If this variable is not lazy (the default) then evaluate
     *  the expression contained in this variable, and notify any
     *  value dependents. If those are not lazy, then they too will
     *  be evaluated.  Also, if the variable is not lazy, then
     *  notify its container, if there is one, by calling its
     *  attributeChanged() method.
     *  <p>
     *  If this variable is lazy, then mark this variable and any
     *  of its value dependents as needing evaluation and for any
     *  value dependents that are not lazy, evaluate them.
     *  Note that if there are no value dependents,
     *  or if they are all lazy, then this will not
     *  result in evaluation of this variable, and hence will not ensure
     *  that the expression giving its value is valid.  Call getToken()
     *  or getType() to accomplish that.
     *  @return The current list of value listeners, which are evaluated
     *   as a consequence of this call to validate().
     *  @exception IllegalActionException If this variable or a
     *   variable dependent on this variable cannot be evaluated (and is
     *   not lazy) and the model error handler throws an exception.
     *   Also thrown if the change is not acceptable to the container.
     */
    public Collection validate() throws IllegalActionException {
        Collection collection = super.validate();
        try {
            BooleanToken token = (BooleanToken) getToken();
            if (token == null || token.equals(BooleanToken.TRUE)) {
                GTTools.setIconDescription(this, _HIDING_ICON);
            } else {
                GTTools.setIconDescription(this, _NOT_HIDING_RELATION);
            }
        } catch (IllegalActionException e) {
            throw new KernelRuntimeException(e,
                    "Cannot get token from the attribute.");
        }
        return collection;
    }

    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            GTTools.checkContainerClass(this, container, Replacement.class,
                    true);
            GTTools.checkUniqueness(this, container);
        }
    }

    public static final boolean DEFAULT = true;

    private static final String _HIDING_ICON = "<svg>"
            + "<rect x=\"0\" y=\"0\" width=\"94\" height=\"32\""
            + "  style=\"fill:#00FFFF\"/>"
            + "<line x1=\"8\" y1=\"7\" x2=\"20\" y2=\"7\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"20\" y1=\"7\" x2=\"20\" y2=\"25\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"20\" y1=\"25\" x2=\"32\" y2=\"25\""
            + "  style=\"stroke:#000000\"/>"
            + "<polygon points=\"20,10 14,16 20,22 26,16\" style=\"fill:#000000\"/>"
            + "<line x1=\"39\" y1=\"14\" x2=\"52\" y2=\"14\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"39\" y1=\"18\" x2=\"52\" y2=\"18\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"11\" x2=\"55\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"21\" x2=\"55\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"62\" y1=\"7\" x2=\"74\" y2=\"7\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"74\" y1=\"7\" x2=\"74\" y2=\"25\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"74\" y1=\"25\" x2=\"86\" y2=\"25\""
            + "  style=\"stroke:#000000\"/>" + "</svg>";

    private static final String _NOT_HIDING_RELATION = "<svg>"
            + "<rect x=\"0\" y=\"0\" width=\"94\" height=\"32\""
            + "  style=\"fill:#00FFFF\"/>"
            + "<line x1=\"8\" y1=\"7\" x2=\"20\" y2=\"7\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"20\" y1=\"7\" x2=\"20\" y2=\"25\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"20\" y1=\"25\" x2=\"32\" y2=\"25\""
            + "  style=\"stroke:#000000\"/>"
            + "<polygon points=\"20,10 14,16 20,22 26,16\" style=\"fill:#000000\"/>"
            + "<line x1=\"39\" y1=\"14\" x2=\"52\" y2=\"14\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"39\" y1=\"18\" x2=\"52\" y2=\"18\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"11\" x2=\"55\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"21\" x2=\"55\" y2=\"16\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"62\" y1=\"7\" x2=\"74\" y2=\"7\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"74\" y1=\"7\" x2=\"74\" y2=\"25\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"74\" y1=\"25\" x2=\"86\" y2=\"25\""
            + "  style=\"stroke:#000000\"/>"
            + "<line x1=\"41\" y1=\"9\" x2=\"49\" y2=\"23\""
            + "  style=\"stroke:#C00000; stroke-width:3\"/>" + "</svg>";

}
