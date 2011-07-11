/* An actor with a private parameter, used to test AutoAdapter.

 Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic.program.procedural.java.test;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Source;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SleepFireTwice

/**
 An actor with a private parameter, used to test AutoAdapter.
 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.1

 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ActorWithPrivateParameter extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ActorWithPrivateParameter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _myPrivateParameter = new Parameter(this, "_myPrivateParameter");
        _myPrivateParameter.setExpression("2.0");

        // This is wrong, use displayName().
        _myPrivateParameterWithADifferentName = new Parameter(this, "my Private Parameter spaces in the name");
        _myPrivateParameterWithADifferentName.setExpression("3.0");

        // It is wrong to set the name to be different this way, but
        // we have a test case that does it.  The right way is to use displayName().
        disconnectedPort = new TypedIOPort(this, "Disconnected Port", false, true);
        disconnectedPort.setTypeEquals(BaseType.DOUBLE);


        // Set the type constraint.
        output.setTypeAtLeast(_myPrivateParameter);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the token in the <i>value</i> parameter to the output.
     *  @exception IllegalActionException If it is thrown by the
     *   send() method sending out the token.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        double a = ((DoubleToken)_myPrivateParameter.getToken()).doubleValue();
        double b = ((DoubleToken)_myPrivateParameterWithADifferentName.getToken()).doubleValue();
        output.send(0, new DoubleToken(a + b));
        disconnectedPort.send(0, new DoubleToken(a + b));
    }

    public TypedIOPort disconnectedPort;

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    private Parameter _myPrivateParameter;

    private Parameter _myPrivateDisconnectedParameter;

    public Parameter _myPrivateParameterWithADifferentName;

}
