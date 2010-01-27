/* Actor that serves as a placeholder for PtinyOS nesC modules and
   configurations.

 @Copyright (c) 2005-2010 The Regents of the University of California.
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
package ptolemy.domains.ptinyos.lib;

import ptolemy.actor.AtomicActor;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.NameIcon;

///////////////////////////////////////////////////////////////////
//// NCComponentBase

/**
  Actor that serves as a placeholder for PtinyOS nesC modules and
  configurations.  This class is a base class for nesC component
  classes.  These are classes with source code defined in a .nc file
  intended for use with TinyOS to program the Berkeley Motes.  This
  class provides a parameter <i>source</i> that is used to identify
  the nesC source file. It works in conjunction with the NCComponent
  MoML class, which attaches a tableau factory so that selecting the
  "Open Actor" menu choice will open the nesC source file.

 @author Elaine Cheong, Edward A. Lee, Yang Zhao
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Green (celaine)
 @Pt.AcceptedRating Green (cxh)
 */
public class NCComponentBase extends AtomicActor {
    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public NCComponentBase(Workspace workspace) {
        super(workspace);

        try {
            _init();
        } catch (KernelException ex) {
            throw new InternalErrorException(this, ex,
                    "Error constructing parameters of NCComponentBase.");
        }
    }

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NCComponentBase(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init();

        // Set the displayed name.  "_displayedName" is a specially
        // named attribute that is used in {@link
        // ptolemy.vergil.icon.ActorNameIcon} to display the value of
        // the attribute on the actor icon.
        displayedName = new StringAttribute(this, "_displayedName");
        displayedName.setExpression(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The source code file or URL.  The default value is
     *  "$PTII/ptolemy/domains/ptinyos/lib/NCComponent.nc"
     */
    public FileParameter source;

    /** Relative orientation of ports on the icon of this actor.  The
     *  default value is an IntegerToken of value 90.
     */
    public Parameter rotatePorts;

    /** Icon for this component.  The default value is a NameIcon with
     *  name "_icon", which will display the value of the {@link
     *  #displayedName} attribute on the icon.
     */
    public NameIcon icon;

    /** Displayed name on icon.  The value is set to the name of the
     *  nesC component being represented.  Otherwise, the name of this
     *  class is displayed.
     */
    public StringAttribute displayedName;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the actor. */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        source = new FileParameter(this, "source");
        source.setExpression(
                "$PTII/ptolemy/domains/ptinyos/lib/NCComponent.nc");
        source.setVisibility(Settable.EXPERT);

        // Set port orientation so that input ports are on top.
        // "_rotatePorts" is a specially named Parameter that is used
        // in {@link ptolemy.vergil.actor.IOPortControlle} and {@link
        // ptolemy.vergil.toolbox.RotateOrFlipPorts} to change the
        // location of the ports on the actor icon.
        rotatePorts = new Parameter(this, "_rotatePorts", new IntToken(90));

        // Create an icon that displays the value of the
        // "_displayedName" attribute on the icon.
        icon = new NameIcon(this, "_icon");
    }
}
