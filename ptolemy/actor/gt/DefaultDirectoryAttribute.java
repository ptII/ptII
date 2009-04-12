/* An attribute to locate the default directory containing models to be
   transformed.

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

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DefaultDirectoryAttribute

/**
 An attribute to locate the default directory containing models to be
 transformed. It should be placed in the pattern of a transformation rule.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DefaultDirectoryAttribute extends ParameterAttribute implements
        ValueListener {

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
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
    public DefaultDirectoryAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public DefaultDirectoryAttribute(Workspace workspace) {
        super(workspace);
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DefaultDirectoryAttribute newObject =
            (DefaultDirectoryAttribute) super.clone(workspace);
        newObject.parameter = (Parameter) newObject.getAttribute("display");
        return newObject;
    }

    /** Specify the container NamedObj, adding this attribute to the
     *  list of attributes in the container.  If the container already
     *  contains an attribute with the same name, then throw an exception
     *  and do not make any changes.  Similarly, if the container is
     *  not in the same workspace as this attribute, throw an exception.
     *  If this attribute is already contained by the NamedObj, do nothing.
     *  If the attribute already has a container, remove
     *  this attribute from its attribute list first.  Otherwise, remove
     *  it from the directory of the workspace, if it is there.
     *  If the argument is null, then remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this object being garbage collected.
     *  Note that since an Attribute is a NamedObj, it can itself have
     *  attributes.  However, recursive containment is not allowed, where
     *  an attribute is an attribute of itself, or indirectly of any attribute
     *  it contains.  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  <p>
     *  Subclasses may constrain the type of container by overriding
     *  {@link #setContainer(NamedObj)}.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            _checkContainerClass(container, Pattern.class, false);
            _checkUniqueness(container);
        }
    }

    /** React to the fact that the specified Settable has changed, and update
     *  the displayed string.
     *  @param settable The object that has changed value.
     */
    public void valueChanged(Settable settable) {
        String display = directory.getExpression() + "/";
        String filter = fileFilter.getExpression();
        if (filter.equals("")) {
            display += "*.xml";
        } else {
            display += filter;
        }
        try {
            if (subdirs.getToken().equals(BooleanToken.TRUE)) {
                display += " [...]";
            }
        } catch (IllegalActionException e) {
        }
        parameter.setExpression(display);
    }

    /** The default directory where model files are searched.
     */
    public FileParameter directory;

    /** The filter used to search the files, such as "*.xml".
     */
    public StringParameter fileFilter;

    /** A Boolean parameter to determine whether subdirectories of the directory
     *  are also searched.
     */
    public Parameter subdirs;

    /** Initialize the parameter used to contain the value of this attribute.
     *
     *  @exception IllegalActionException If value of the parameter cannot be
     *   set.
     *  @exception NameDuplicationException If the parameter cannot be created.
     */
    protected void _initParameter() throws IllegalActionException,
            NameDuplicationException {
        parameter = new StringParameter(this, "display");
        parameter.setDisplayName("Display (./)");
        parameter.setPersistent(false);
        parameter.setVisibility(NONE);

        directory = new FileParameter(this, "directory");
        directory.setDisplayName("Directory");
        directory.setExpression(".");
        directory.addValueListener(this);
        Parameter allowFiles = new Parameter(directory, "allowFiles");
        allowFiles.setTypeEquals(BaseType.BOOLEAN);
        allowFiles.setToken(BooleanToken.FALSE);
        Parameter allowDirectories = new Parameter(directory,
                "allowDirectories");
        allowDirectories.setTypeEquals(BaseType.BOOLEAN);
        allowDirectories.setToken(BooleanToken.TRUE);

        fileFilter = new StringParameter(this, "filter");
        fileFilter.setDisplayName("File filter (*.xml)");
        fileFilter.setExpression("");
        fileFilter.addValueListener(this);

        subdirs = new Parameter(this, "subdirs");
        subdirs.setDisplayName("Include subdirs");
        subdirs.setTypeEquals(BaseType.BOOLEAN);
        subdirs.setExpression("true");
        subdirs.addValueListener(this);
    }

}
