/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2009 The Regents of the University of California.
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
package ptolemy.data.properties;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.util.StringUtilities;

public class PropertyAttribute extends AbstractSettableAttribute {

    /** Construct a PropertyAttribute with the specified name, and container.
     * @param container Container
     * @param name Name
     * @exception IllegalActionException If the attribute is not of an
     *  acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *  an attribute already in the container.
     */
    public PropertyAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public void addValueListener(ValueListener listener) {
        // no listeners supported so far
        return;
    }

    /** Write a MoML description of the PropertyAttribute.  Nothing is
     *  written if the value is null or "".
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     *  @see ptolemy.kernel.util.NamedObj#exportMoML(Writer, int, String)
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        String value = getExpression();
        String valueTerm = "";

        if ((value != null) && !value.equals("")) {
            valueTerm = " value=\"" + StringUtilities.escapeForXML(value)
                    + "\"";

            output.write(_getIndentPrefix(depth) + "<" + _elementName
                    + " name=\"" + name + "\" class=\"" + getClassName() + "\""
                    + valueTerm + ">\n");
            _exportMoMLContents(output, depth + 1);
            output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
        }
    }

    public String getExpression() {
       return (_property == null) ? "" : _property.toString();
    }

    public Property getProperty() {
        return _property;
    }

    public Visibility getVisibility() {
        return _visibility;
    }

    public void removeValueListener(ValueListener listener) {
        // no listeners supported so far
        return;
    }

    public void setProperty(Property property) {
        _property = property;
    }
    public void setVisibility(Visibility visibility) {
        _visibility = visibility;
   }

    public Collection validate() throws IllegalActionException {
        // not relevant
        return null;
    }
//    private Visibility _visibility = Settable.NONE;
    private Visibility _visibility = Settable.FULL;
//    private Visibility _visibility = Settable.NOT_EDITABLE;
    protected Property _property;

}
