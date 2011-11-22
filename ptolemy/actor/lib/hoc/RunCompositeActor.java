/* A composite actor that executes a submodel in fire().

 Copyright (c) 2003-2010 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import java.util.Iterator;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// RunCompositeActor

/**
 This is a composite actor that can execute the contained model
 completely, as if it were a top-level model, on each firing.
 This can be used to define an actor whose firing behavior
 is given by a complete execution of a submodel.
 <p>
 An instance of this actor can have ports added to it.  If it has
 input ports, then on each firing, before executing the referenced
 model, this actor will read an input token from the input port, if
 there is one, and use it to set the value of a top-level parameter
 in the contained model that has the same name as the port, if there
 is one.  The simplest way to ensure that there is a matching parameter
 is to use a PortParameter for inputs.  However, this actor will work
 also for ordinary ports. In this case, if this actor has a
 parameter with the same name as the port, and it is an instance
 of Variable (or its derived class Parameter), then the token
 read at the input is moved into it using its setToken() method.
 Otherwise, if it is an instance of Settable, then a string representation
 of the token is copied using the setExpression() method.
 Input ports should not be multiports, and if they are, then
 all but the first channel will be ignored.
 <p>
 If this actor has output ports and the contained model is executed,
 then upon completion of that execution, if this actor has parameters
 whose names match those of the output ports, then the final value of
 those parameters is sent to the output ports. If such a parameter is an
 instance of Variable (or its derived class Parameter), then its
 contained token is sent to the output token. Otherwise, if it is an
 instance of Settable, then a string token is produced on the output
 with its value equal to that returned by getExpression() of the
 Settable. Output ports should not be multiports. If they are,
 then all but the first channel will be ignored.
 A typical use of this actor will use the SetVariable actor
 inside to define the value of the output port.
 <p>
 In preinitialize(), type constraints are set up so that input
 and output ports with (name) matching parameters are constrained
 to have compatible types. Note that if the ports or parameters
 are changed during execution, then it will be necessary to set
 up matching type constraints by hand.  Since this isn't possible
 to do from Vergil, the ports and parameters of this actor
 should not be changed using Vergil during execution.
 <p>
 This actor also overrides the requestChange() method and the
 executeChangeRequests() method to execute the given change. It does not
 delegate the change request to the container, but executes the request
 immediately or records it, depending on whether setDeferringChangeRequests()
 has been called with a true argument.

 @author Edward A. Lee, Yang Zhao, Elaine Cheong
 @version $Id$
 @since Ptolemy II 4.0
 @see ModelReference
 @see ptolemy.actor.lib.SetVariable
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class RunCompositeActor extends LifeCycleManager {
    /** Construct an actor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     *  @throws NameDuplicationException If there is already a parameter with
     *   name firingCountLimit.
     *  @throws IllegalActionException If the firingCountLimit cannot be
     *   initialized.
     */
    public RunCompositeActor() throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a RunCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     *  @throws NameDuplicationException If there is already a parameter with
     *   name firingCountLimit.
     *  @throws IllegalActionException If the firingCountLimit cannot be
     *   initialized.
     */
    public RunCompositeActor(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a RunCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public RunCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Indicator to run the contained model a limited number of times.
     *  If this parameter has a value greater than zero, then after
     *  firing the inside model the specified number of times,
     *  {@link #postfire()} will return false. This is an int that
     *  defaults to 0.
     */
    public Parameter firingCountLimit;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Run a complete execution of the contained model.  A complete
     *  execution consists of invocation of super.initialize(),
     *  repeated invocations of super.prefire(), super.fire(), and
     *  super.postfire(), followed by super.wrapup().  The invocations
     *  of prefire(), fire(), and postfire() are repeated until either
     *  the model indicates it is not ready to execute (prefire()
     *  returns false), or it requests a stop (postfire() returns
     *  false or stop() is called).  Before running the complete
     *  execution, this method calls the director's transferInputs()
     *  method to read any available inputs.  After running the
     *  complete execution, it calls transferOutputs().  The subclass
     *  of this can set the <i>_isSubclassOfRunCompositeActor</i> to be
     *  true to call the fire method of the superclass of this.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("---- calling fire(), which will execute a subsystem.");
        }

        // FIXME: Return result should be used to set what postfire() returns?
        _executeInsideModel();
    }
    
    /** Return null, indicating that there is no executive director.
     *  In fact, there is, but since the inside model is supposed to run
     *  as if there were not.
     *  @return null.
     */
    public Director getExecutiveDirector() {
        return null;
    }

    /** Initialize this actor, which in this case, does nothing.  The
     *  initialization of the submodel is accomplished in fire().  The
     *  subclass of this can set the
     *  <i>_isSubclassOfRunCompositeActor</i> to be true to call the
     *  initialize method of the superclass of this.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize(), which does nothing.");
        }
        _iteration = 0;
    }
    
    /** Override the base class to make sure we actually use the
     *  executive director instead of null as returned by
     *  {@link #getExecutiveDirector()}.
     *  @exception IllegalActionException If there is no executive director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director director = super.getExecutiveDirector();
        if (director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without an executive director.");
        }
        return director.newReceiver();
    }

    /** Return true, indicating that execution can continue.  The
     *  subclass of this can set the
     *  <i>_isSubclassOfRunCompositeActor</i> to be true to call the
     *  postfire method of the superclass of this.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public boolean postfire() throws IllegalActionException {
        int limitValue = ((IntToken)firingCountLimit.getToken()).intValue();
        if (limitValue > 0) {
            _iteration++;
            if (_iteration >= limitValue) {
                if (_debugging) {
                    _debug("Called postfire(), which returns false.");
                }
                return false;
            }
        }
        if (_debugging) {
            _debug("Called postfire(), which returns true.");
        }
        return true;
    }

    /** Return true, indicating that this actor is always ready to fire.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called prefire(), which returns true.");
        }

        return true;
    }

    /** Override the base class to set type constraints between the
     *  output ports and parameters of this actor whose name matches
     *  the output port. If there is no such parameter, then create
     *  an instance of Variable with a matching name and set up the
     *  type constraints to that instance.  The type of the output
     *  port is constrained to be at least that of the parameter
     *  or variable.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's preinitialize() method throws it, or if this actor
     *   is not opaque.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        Iterator ports = outputPortList().iterator();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();

            // Ensure that the production rate is one.
            // FIXME: This may not be right if there is no
            // actual source of data for this port (e.g. no
            // SetVariable actor).
            Variable rate = (Variable) port.getAttribute("tokenProductionRate");

            if (rate == null) {
                try {
                    rate = new Variable(port, "tokenProductionRate");
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }
            }

            rate.setToken(new IntToken(1));

            String portName = port.getName();
            Attribute attribute = getAttribute(portName);

            if (attribute == null) {
                try {
                    workspace().getWriteAccess();
                    attribute = new Variable(this, portName);
                } catch (NameDuplicationException ex) {
                    throw new InternalErrorException(ex);
                } finally {
                    workspace().doneWriting();
                }
            }

            // attribute is now assured to be non-null.
            if (attribute instanceof Variable) {
                port.setTypeAtLeast((Variable) attribute);
            } else {
                // Assume the port type must be a string.
                port.setTypeEquals(BaseType.STRING);
            }
        }
    }

    /** Override the base class to do nothing.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Called wrapup(), which does nothing.");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the class name and create the parameters.
     *  @throws NameDuplicationException If there is already a parameter with
     *   name firingCountLimit.
     *  @throws IllegalActionException If the firingCountLimit cannot be
     *   initialized.
     */
    private void _init() throws IllegalActionException, NameDuplicationException {
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is RunCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as RunCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be RunCompositeActor.
        setClassName("ptolemy.actor.lib.hoc.RunCompositeActor");
        
        firingCountLimit = new Parameter(this, "firingCountLimit");
        firingCountLimit.setTypeEquals(BaseType.INT);
        firingCountLimit.setExpression("0");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The count of iterations. */
    private int _iteration;
}
