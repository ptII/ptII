/** Represent an native method argument

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
package jni;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.StringTokenizer;

import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// Argument

/**
 A native method argument associated with a GenericJNIActor.

 @author V.Arnould, Thales, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 2.2
 @Pt.ProposedRating Red (vincent.arnould)
 @Pt.AcceptedRating Red (vincent.arnould)
 @see jni.GenericJNIActor
 @deprecated This code is old, hard to use and unmaintained.  See
  {@link ptolemy.actor.lib.jni.EmbeddedCActor} for a more recent implementation.
 */
public class Argument extends AbstractSettableAttribute {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public Argument() {
        super();
    }

    /** Creates a new instance of Argument with the given name
     *  for the given GenericJNIActor.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Argument(GenericJNIActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be notified when the value of this settable
     *  object changes. An implementation of this method should ignore
     *  the call if the specified listener is already on the list of
     *  listeners.  In other words, it should not be possible for the
     *  same listener to be notified twice of a value update.
     *  @param listener The listener to add, which is ignored, no
     *  listener is added.
     *  @see #removeValueListener(ValueListener)
     */
    public void addValueListener(ValueListener listener) {
    }

    /** Check that the specified type is a suitable type for
     *  this entity.
     *  @exception IllegalActionException If the Argument has not
     *   an acceptable C type.  Not thrown in this base class.
     */
    public void checkType() {
        if (_cType.startsWith("char") || _cType.startsWith("long")
                || _cType.startsWith("short") || _cType.startsWith("double")) {
            if (isOutput() && !isInput() && !_cType.endsWith("[]")) {
                // FIXME: Throw an exception here
                MessageHandler.error("An argument can't be "
                        + "output with a simple type.");
                setInput(true);
            }

            return;
        } else {
            // FIXME: Throw an exception here
            MessageHandler.error("The type : " + _cType
                    + " is not supported. Types supported:"
                    + "\nchar, long (unsigned)" + " , short, double"
                    + "\nThe JNI code generation" + " will not work");
            setCType("");
        }
    }

    /** Export the Argument in a property MoML. If this object is not
     *  persistent, then write nothing.
     *  @exception IOException If an IO error occurs
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (_isMoMLSuppressed(depth)) {
            return;
        }

        String value = getExpression();
        output.write(_getIndentPrefix(depth) + "<" + _elementName + " name=\""
                + name.trim() + "\" class=\"" + getClassName().trim()
                + "\" value=\"" + value.trim() + "\" >\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
    }

    /** Get the C type of the argument as a pointer if it is an array.
     *  @return the _cType attribute in pointer
     */
    public String getC2Type() {
        String result = _cType;

        if (_cType.endsWith("[]")) {
            result = _cType.substring(0, _cType.length() - 2) + " *";
        }

        return result;
    }

    /** Get the C2 Type Array, but if we are under SunOS, and
     *  getC2Type returns long, then return int *.  On other platforms,
     *  just return the value of getC2Type().  FIXME: This platform
     *  dependent change is necessary under Solaris 8 for some reason.
     *  @return the _cType attribute in pointer
     */
    public String getC2TypeHack() {
        String result = getC2Type();

        if (StringUtilities.getProperty("os.name").startsWith("SunOS")) {
            if (result.startsWith("long")) {
                return "int *";
            }
        } else if (StringUtilities.getProperty("os.name").startsWith("Linux")
                 || StringUtilities.getProperty("os.name").startsWith("Mac")) {
            if (result.startsWith("long")) {
                return "jlong *";
            }
        }

        return result;
    }

    /** Get the C type of the argument.
     *  @return the _cType attribute
     *  @see #setCType(String)
     */
    public String getCType() {
        return _cType;
    }

    /** Get the container entity.
     *  @return The container, which is an instance of CompositeEntity.
     *  @see #setContainer(NamedObj)
     */
    public NamedObj getContainer() {
        return _container;
    }

    /** Get the expression of the argument.
     *  The format is "_isInput, _isOutput, _isReturn, _cType".
     *  @return the string containing the argument specifications
     *  @see #setExpression(String)
     */
    public String getExpression() {
        return Boolean.valueOf(isInput()).toString() + ","
                + Boolean.valueOf(isOutput()).toString() + ","
                + Boolean.valueOf(isReturn()).toString() + "," + getCType();
    }

    /** Get the JNI type of the argument.
     *  @return the corresponding JNI type.
     */
    public String getJNIType() {
        String returnJNIType = "";

        if (_cType.endsWith("[]")) {
            returnJNIType = "Array";
        }

        if (_cType.equals("char") || _cType.startsWith("char")) {
            returnJNIType = "jboolean" + returnJNIType;
        } else if (_cType.equals("short") || _cType.startsWith("short")) {
            // a C char is 8 bits. a java char is 16 bits,
            // so we use jbyte
            returnJNIType = "jchar" + returnJNIType;
        } else if (_cType.equals("long") || _cType.startsWith("long")) {
            // a C long is 32 bits. a java long is 64 bits,
            // so we use jint
            returnJNIType = "jlong" + returnJNIType;
        } else if (_cType.equals("double") || _cType.startsWith("double")) {
            returnJNIType = "jdouble" + returnJNIType;
        } else if (_cType.equals("void") || _cType.startsWith("void")) {
            returnJNIType = "void" + returnJNIType;
        } else {
            MessageHandler.error("JNIType unavailable for '" + _cType
                    + "': not convertible JNI type");
            returnJNIType = "void";
        }

        return returnJNIType;
    }

    /** Get the Java type of the argument.
     *  @return the corresponding Java type
     */
    public String getJType() {
        String returnJType = "";

        // If it's an array.
        if (_cType.endsWith("[]")) {
            returnJType = "[]";
        }

        // A C char is 8 bits unsigned. a java char is 16 bits,
        // so we use boolean which is 8 bits unsigned.
        if (_cType.equals("char") || _cType.startsWith("char")) {
            returnJType = "boolean" + returnJType;
        } else if (_cType.equals("short") || _cType.startsWith("short")) {
            // a C short is 16 bits unsigned, a java short is 16 bits
            // but not unsigned,
            // so we use char which is 16bits unsigned.
            returnJType = "char" + returnJType;
        } else if (_cType.equals("long") || _cType.startsWith("long")) {
            // a C long is 32 bits unsigned. a java long is 64 bits,
            // so we use int which is 32 , but not unsigned !! TBF
            returnJType = "long" + returnJType;
        } else if (_cType.equals("double") || _cType.startsWith("double")) {
            // double is 64 bits in C and in Java, so no problem
            returnJType = "double" + returnJType;
        } else if (_cType.equals("void") || _cType.startsWith("void")) {
            // for the functions with a void return
            returnJType = "void";
        } else {
            // FIXME: I guess we are printing a warning using
            // the MessageHandler and then returning void here?
            try {
                MessageHandler.warning("Type = " + _cType
                        + " not convertible in JavaType");
            } catch (Throwable throwable) {
            }

            returnJType = "void";
        }

        return returnJType;
    }

    /** Get the kind as a comma separated list.
     *  @return "input", "output" "input, output" or "return"
     *  @see #setKind(String)
     */
    public String getKind() {
        String returnValue = "";

        // Set Kind
        if (isInput()) {
            returnValue = "input";
        }

        if (isOutput() && !isInput()) {
            returnValue = "output";
        }

        if (isOutput() && isInput()) {
            returnValue = "input,output";
        }

        if (isReturn()) {
            returnValue = "return";
        }

        return returnValue;
    }

    /** Get the Java class corresponding to the Java Type.
     *  @return the corresponding Java class
     */
    public String getType() {
        String returnCType = "";

        if (_cType.endsWith("[]")) {
            returnCType = "[]";
        }

        if (_cType.equals("char") || _cType.startsWith("char")) {
            returnCType = "Boolean" + returnCType;
        } else if (_cType.equals("short") || _cType.startsWith("short")) {
            returnCType = "Byte" + returnCType;
        } else if (_cType.equals("long") || _cType.startsWith("long")) {
            returnCType = "Long" + returnCType;
        } else if (_cType.equals("double") || _cType.startsWith("double")) {
            returnCType = "Double" + returnCType;
        } else if (_cType.equals("void") || _cType.startsWith("void")) {
            returnCType = "Object" + returnCType;
        } else {
            // FIXME: why is this code not like the code above
            MessageHandler.error("Type = " + _cType
                    + " not convertible in JavaClass");
            returnCType = "Object";
        }

        return returnCType;
    }

    /** Get the visibility of this Settable.
     *  @return Always return Settable.NONE, indicating that the user
     *  interface should not make an instance visible.
     *  @see #setVisibility(Visibility)
     */
    public Visibility getVisibility() {
        return Settable.NONE;
    }

    /** Return true if it is an input.
     *  @return true is it is an input, or false if not.
     */
    public boolean isInput() {
        return _isInput;
    }

    /** Return true if it an output.
     *  @return true is it is an output, or false if not.
     */
    public boolean isOutput() {
        return _isOutput;
    }

    /** Return true if it is a return.
     *  @return true is it is an return, or false if not.
     */
    public boolean isReturn() {
        return _isReturn;
    }

    /** Remove a listener to be notified when the value of
     *  this settable object changes.
     *  @param listener The listener to remove.
     *  @see #addValueListener(ValueListener)
     */
    public void removeValueListener(ValueListener listener) {
    }

    /** Set the C type of the argument with the given string.
     *  @param cType The C type of argument.
     *  @see #getCType()
     */
    public void setCType(String cType) {
        _cType = cType.trim();
    }

    /** Specify the container, adding the entity to the list
     *  of entities in the container.
     *
     *  <p>If the container already contains an entity with the same
     *  name, then throw an exception and do not make any changes.
     *  Similarly, if the container is not in the same workspace as
     *  this entity, throw an exception.
     *
     *  <br>If the entity is already contained by the container,
     *  do nothing.
     *
     *  <br>If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *
     *  <br> If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *
     *  <br> It is not added to the workspace directory, so this could
     *  result in this entity being garbage collected.
     *
     *  <br>Derived classes may further constrain the container to
     *  subclasses of CompositeEntity by overriding the protected
     *  method _checkContainer(). This method is write-synchronized to
     *  the workspace and increments its version number.
     *
     *  @param container The proposed container, which must be
     *  a GenericJNIActor.
     *  @exception IllegalActionException If the action would result
     *  in a recursive containment structure, or if this entity and
     *  container are not in the same workspace, or if the container
     *  is not an instance of GenericJNIActor.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     *   @see #getContainer()
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        if ((container != null) && (_workspace != container.workspace())) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }

        try {
            _workspace.getWriteAccess();
            _checkContainer(container);

            // NOTE: The following code is quite tricky.
            // It is very careful
            // to leave a consistent state even
            // in the face of unexpected
            // exceptions.  Be very careful if modifying it.
            GenericJNIActor previousContainer = (GenericJNIActor) getContainer();

            if (previousContainer == container) {
                return;
            }

            // Do this first, because it may throw an exception, and
            // we have not yet changed any state.
            if (container != null) {
                if (!(container instanceof GenericJNIActor)) {
                    throw new InternalErrorException("Expecting a container "
                            + "of type GenericJNIActor, got " + container);
                } else {
                    _container = (GenericJNIActor) container;
                    if (((GenericJNIActor) container).getArgument(getName()) == null) {
                        // Only add if the argument is not already present.
                        ((GenericJNIActor) container)._addArgument(this);
                        if (previousContainer == null) {
                            _workspace.remove(this);
                        }
                    }
                }
            }

            if (previousContainer != null) {
                // This is safe now because it does not
                // throw an exception.
                previousContainer.removeArgument(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set the expression of the argument.
     *  The format is "_isInput,_isOutput,_isReturn,_cType".
     *  @see #getExpression()
     */
    public void setExpression(String expression) {
        try {
            super.setExpression(expression);
            _value = expression;

            StringTokenizer tokenizer = new StringTokenizer(_value, ",");

            try {
                setInput(Boolean.valueOf(tokenizer.nextToken()).booleanValue());
                setOutput(Boolean.valueOf(tokenizer.nextToken()).booleanValue());
                setReturn(Boolean.valueOf(tokenizer.nextToken()).booleanValue());
                setCType(tokenizer.nextToken());
            } catch (java.util.NoSuchElementException e) {
            }

            validate();
        } catch (IllegalActionException e) {
            MessageHandler.error("TRT error! Bad expression for Argument "
                    + getName(), e);
        }
    }

    /** Set the expression of the argument from its attributes.
     *  @see #getExpression()
     */
    public void setExpression() {
        setExpression(Boolean.valueOf(isInput()).toString() + ","
                + Boolean.valueOf(isOutput()).toString() + ","
                + Boolean.valueOf(isReturn()).toString() + "," + getCType());
    }

    /** Set to true if the attribute is an input.
     *  @param input True if this is an input, false if it is not.
     */
    public void setInput(boolean input) {
        _isInput = input;
    }

    /** Set the kind of the argument with the given string.
     *  @param selectedValues A string describing the type of Argument.
     *  valid values are "input", "output", "return", "input, output".
     *  @see #getKind()
     */
    public void setKind(String selectedValues) {
        if (selectedValues.equals("input")) {
            this.setInput(true);
        } else {
            this.setInput(false);
        }

        if (selectedValues.equals("output")) {
            this.setOutput(true);
        } else {
            this.setOutput(false);
        }

        if (selectedValues.equals("return")) {
            this.setReturn(true);
        } else {
            this.setReturn(false);
        }

        if (selectedValues.equals("input, output")) {
            this.setInput(true);
            this.setOutput(true);
        }

        // FIXME: this should throw an exception not call MessageHandler
        // directly.
        if (selectedValues.equals("input, return")) {
            MessageHandler.error("An argument can't be input "
                    + "and return at the same time.");
        }

        if (selectedValues.equals("output, return")) {
            MessageHandler.error("An argument can't be output "
                    + "and return or in at the same time.");
        }

        if (selectedValues.equals("input, output, return")) {
            MessageHandler.error("An argument can't be in-out "
                    + "and return at the same time.");
        }
    }

    /** Set to true if the attribute is an output.
     *  @param output True if this is an output, false if it is not.
     */
    public void setOutput(boolean output) {
        _isOutput = output;
    }

    /** Set to true if the attribute is a return.
     *  @param returnFlag True if this is an input, false if it is not.
     */
    public void setReturn(boolean returnFlag) {
        _isReturn = returnFlag;
    }

    /** Set the visibility of this attribute in the user interface.
     *  @param visibility Ignored, the visibility of this attribute is
     *  always {@link ptolemy.kernel.util.Settable#NONE}.
     *  @see #getVisibility()
     */
    public void setVisibility(ptolemy.kernel.util.Settable.Visibility visibility) {
    }

    /** Notify the container that an attribute has changed.
     *  @return A collection of settables that are also validated as a
     *  side effect, or null if there are none.
     *  @exception IllegalActionException If a error occurs
     */
    public Collection validate() throws IllegalActionException {
        NamedObj container = getContainer();

        if (container != null) {
            container.attributeChanged(this);
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this entity.  In this base class, this method returns immediately
     *  without doing anything.
     *  Derived classes may override it to constrain
     *  the container.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.  Not thrown in this base class.
     */
    protected void _checkContainer(NamedObj container)
            throws IllegalActionException {
        if (!(container instanceof GenericJNIActor)) {
            throw new IllegalActionException(this, container,
                    "Cannot place arguments on entities "
                            + container.getClass().getName()
                            + ", which are not GenericJNIActor.");
        }
    }

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
        ((Settable) destination).setExpression(getExpression());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** A boolean that specified if the argument is an Input
     */
    private boolean _isInput;

    /** A boolean that specified if the argument is an Output
     */
    private boolean _isOutput;

    /** A boolean that specified if the argument is a return.
     */
    private boolean _isReturn;

    /** A String that specified the argument type, in C language.
     */
    private String _cType;

    /** A String that is the argument value, ie its expression
     */
    private String _value;

    /** @serial The entity that contains this entity.
     */
    private GenericJNIActor _container;
}
