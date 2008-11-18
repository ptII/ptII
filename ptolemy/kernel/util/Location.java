/* An attribute that represents a location of a node in a schematic.

 Copyright (c) 2002-2007 The Regents of the University of California.
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
package ptolemy.kernel.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// Location

/**
 An attribute that represents a location of a node in a schematic.

 <p>By default, an instance of this class is not visible in a user
 interface.  This is indicated to the user interface by returning NONE
 to the getVisibility() method.  The location is specified by calling
 setExpression() with a string that has the form "x,y" or "[x,y]" or
 "{x,y}", where x and y can be parsed into doubles.

 <p>The default location is a two dimensional location with value {0.0, 0.0}.
 This class can also handle locations with greater than two dimensions.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class Location extends SingletonAttribute implements Locatable {
    // FIXME: Note that this class does not extend from StringAttribute
    // because it is a singleton.  Thus, there is a bunch of code
    // duplication here.  The fix would be to modify StringAttribute
    // so that we could have a singleton.

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public Location(Workspace workspace) {
        super(workspace);
    }

    /** Construct an attribute with the given container and name.
     *  @param container The container.
     *  @param name The name of the vertex.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Location(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be notified when the value of this attribute changes.
     *  If the listener is already on the list of listeners, then do nothing.
     *  @param listener The listener to add.
     *  @see #removeValueListener(ValueListener)
     */
    public void addValueListener(ValueListener listener) {
        if (_valueListeners == null) {
            _valueListeners = new LinkedList();
        }

        if (!_valueListeners.contains(listener)) {
            _valueListeners.add(listener);
        }
    }

    /** Clone the location into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the base class throws it.
     *  @return A new Location.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Location newObject = (Location) super.clone(workspace);

        // Copy the location so that the reference in the new object
        // does not refer to the same array.
        // _location can never be null because setLocation() will
        // not handle it.
        int length = _location.length;
        newObject._location = new double[length];
        System.arraycopy(_location, 0, newObject._location, 0, length);

        newObject._valueListeners = null;
        return newObject;
    }

    /** Write a MoML description of this object.
     *  MoML is an XML modeling markup language.
     *  In this class, the object is identified by the "property"
     *  element, with "name", "class", and "value" (XML) attributes.
     *  The body of the element, between the "&lt;property&gt;"
     *  and "&lt;/property&gt;", is written using
     *  the _exportMoMLContents() protected method, so that derived classes
     *  can override that method alone to alter only how the contents
     *  of this object are described.
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline. If this object is non-persistent,
     *  then nothing is written.
     *  @param output The output writer to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     *  @see #isPersistent()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        // If the object is not persistent, and we are not
        // at level 0, do nothing.
        if (_isMoMLSuppressed(depth)) {
            return;
        }

        String value = getExpression();
        String valueTerm = "";

        if ((value != null) && !value.equals("")) {
            valueTerm = " value=\"" + StringUtilities.escapeForXML(value)
                    + "\"";
        }

        // It might be better to use multiple writes here for performance.
        output.write(_getIndentPrefix(depth) + "<" + _elementName + " name=\""
                + name + "\" class=\"" + getClassName() + "\"" + valueTerm
                + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
    }

    /** Return the default value of this Settable,
     *  if there is one.  If this is a derived object, then the default
     *  is the value of the object from which this is derived (the
     *  "prototype").  If this is not a derived object, then the default
     *  is the first value set using setExpression(), or null if
     *  setExpression() has not been called.
     *  @return The default value of this attribute, or null
     *   if there is none.
     *  @see #setExpression(String)
     */
    public String getDefaultExpression() {
        try {
            List prototypeList = getPrototypeList();

            if (prototypeList.size() > 0) {
                return ((Settable) prototypeList.get(0)).getExpression();
            }
        } catch (IllegalActionException e) {
            // This should not occur.
            throw new InternalErrorException(e);
        }

        return _default;
    }

    /** Return a name to present to the user, which
     *  is the same as the name returned by getName().
     *  @return A name to present to the user.
     */
    public String getDisplayName() {
        return getName();
    }

    /** Get the value that has been set by setExpression() or by
     *  setLocation(), whichever was most recently called, or return
     *  an empty string if neither has been called.
     *
     *  <p>If setExpression(String value) was called, then the return
     *  value is exactly what ever was passed in as the argument to
     *  setExpression.  This means that there is no guarantee that
     *  the return value of getExpression() is a well formed Ptolemy
     *  array expression.
     *
     *  <p>If setLocation(double[] location) was called, then the
     *  return value is a well formed Ptolemy array expression that
     *  starts with "{" and ends with "}", for example "{0.0, 0.0}"
     *
     *  @return The expression.
     *  @see #setExpression(String)
     */
    public String getExpression() {
        if (_expressionSet) {
            // FIXME: If setExpression() was called with a string that does
            // not begin and end with curly brackets, then getExpression()
            // will not return something that is parseable by setExpression()
            return _expression;
        }

        if ((_location == null) || (_location.length == 0)) {
            return "";
        }

        // We tack on { } around the value that is returned so that it
        // can be passed to setExpression().
        StringBuffer result = new StringBuffer("{");

        for (int i = 0; i < (_location.length - 1); i++) {
            result.append(_location[i]);
            result.append(", ");
        }

        result.append(_location[_location.length - 1] + "}");
        return result.toString();
    }

    /** Get the location in some cartesian coordinate system.
     *  @return The location.
     *  @see #setLocation(double[])
     */
    public double[] getLocation() {
        return _location;
    }

    /** Get the value of the attribute, which is the evaluated expression.
     *  @return The same as getExpression().
     *  @see #getExpression()
     */
    public String getValueAsString() {
        return getExpression();
    }

    /** Get the visibility of this attribute, as set by setVisibility().
     *  The visibility is set by default to NONE.
     *  @return The visibility of this attribute.
     *  @see #setVisibility(Settable.Visibility)
     */
    public Settable.Visibility getVisibility() {
        return _visibility;
    }

    /** Remove a listener from the list of listeners that is
     *  notified when the value of this variable changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     *  @see #addValueListener(ValueListener)
     */
    public void removeValueListener(ValueListener listener) {
        if (_valueListeners != null) {
            _valueListeners.remove(listener);
        }
    }

    /** Set the value of the attribute by giving some expression.
     *  This expression is not parsed until validate() is called, and
     *  the container and value listeners are not notified until validate()
     *  is called.  See the class comment for a description of the format.
     *  @param expression The value of the attribute.
     *  @see #getExpression()
     */
    public void setExpression(String expression) {
        if (_default == null) {
            _default = expression;
        }

        _expression = expression;
        _expressionSet = true;
    }

    /** Set the location in some cartesian coordinate system, and notify
     *  the container and any value listeners of the new location. Setting
     *  the location involves maintaining a local copy of the passed
     *  parameter. No notification is done if the location is the same
     *  as before. This method propagates the value to any derived objects.
     *  @param location The location.
     *  @exception IllegalActionException If throw when attributeChanged()
     *  is called.
     *  @see #getLocation()
     */
    public void setLocation(double[] location) throws IllegalActionException {
        _expressionSet = false;

        if (_setLocation(location)) {
            // If the location was modified in _setLocation(),
            // then make sure the new value is exported in MoML.
            setPersistent(true);
        }

        propagateValue();
    }

    /** Set the visibility of this attribute.  The argument should be one
     *  of the public static instances in Settable.
     *  @param visibility The visibility of this attribute.
     *  @see #getVisibility()
     */
    public void setVisibility(Settable.Visibility visibility) {
        _visibility = visibility;
    }

    /** Get a description of the class, which is the class name and
     *  the location in parentheses.
     *  @return A string describing the object.
     */
    public String toString() {
        String className = getClass().getName();

        if (_location == null) {
            return "(" + className + ", Location = null)";
        }

        return "(" + className + ", Location = " + getExpression() + ")";
    }

    /** Parse the location specification given by setExpression(), if there
     *  has been one, and otherwise set the location to 0.0, 0.0.
     *  Notify the container and any value listeners of the new location,
     *  if it has changed.
     *  See the class comment for a description of the format.
     *  @return Null, indicating that no other instances of Settable are
     *   validated.
     *  @exception IllegalActionException If the expression is invalid.
     */
    public Collection validate() throws IllegalActionException {
        // If the value has not been set via setExpression(), there is
        // nothing to do.
        if (!_expressionSet) {
            return null;
        }

        double[] location;

        if (_expression == null) {
            location = new double[2];
            location[0] = 0.0;
            location[1] = 0.0;
        } else {
            // Parse the specification: a comma specified list of doubles,
            // optionally surrounded by square or curly brackets.
            StringTokenizer tokenizer = new StringTokenizer(_expression,
                    ",[]{}");
            location = new double[tokenizer.countTokens()];

            int count = tokenizer.countTokens();

            for (int i = 0; i < count; i++) {
                String next = tokenizer.nextToken().trim();
                location[i] = Double.parseDouble(next);
            }
        }

        // Set and notify.
        _setLocation(location);

        // FIXME: If _setLocation() returns true, should we call
        // setModifiedFromClass() like we do elsewhere?

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Propagate the value of this object to the
     *  specified object. The specified object is required
     *  to be an instance of the same class as this one, or
     *  a ClassCastException will be thrown.
     *  @param destination Object to which to propagate the
     *   value.
     *  @exception IllegalActionException If the value cannot
     *   be propagated.
     */
    protected void _propagateValue(NamedObj destination)
            throws IllegalActionException {
        // NOTE: Cannot use the _location value because the
        // expression may not have yet been evaluated.
        ((Location) destination).setExpression(getExpression());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the location without altering the modified status.
     *  @param location The location.
     *  @return True if the location was modified.
     *  @exception IllegalActionException If the call to attributeChanged()
     *  throws it.
     */
    private boolean _setLocation(double[] location)
            throws IllegalActionException {
        // If the location is unchanged, return false.
        if (_location != null) {
            if (_location.length == location.length) {
                boolean match = true;

                for (int i = 0; i < location.length; i++) {
                    if (_location[i] != location[i]) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    return false;
                }
            } else {
                // _location.length != location.length
                // If location is of size 3, then we end up here.
                _location = new double[location.length];
            }
        } else {
            // _location == null
            _location = new double[location.length];
        }

        // FindBugs: _location cannot be null here, so don't check.

        // Copy location array into member array _location.
        // Just referencing _location to location isn't enough, we need
        // to maintain a local copy of the double array.
        for (int i = 0; i < location.length; i++) {
            _location[i] = location[i];
        }

        NamedObj container = getContainer();

        if (container != null) {
            container.attributeChanged(this);
        }

        if (_valueListeners != null) {
            Iterator listeners = _valueListeners.iterator();

            while (listeners.hasNext()) {
                ValueListener listener = (ValueListener) listeners.next();
                listener.valueChanged(this);
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The default value.
    private String _default = null;

    // The expression given in setExpression().
    private String _expression;

    // Indicator that the expression is the most recent spec for the location.
    private boolean _expressionSet = false;

    // The location.
    private double[] _location = { 0.0, 0.0 };

    // Listeners for changes in value.
    private List _valueListeners;

    // The visibility of this attribute, which defaults to NONE.
    private Settable.Visibility _visibility = Settable.NONE;
}
