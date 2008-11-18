/* Discrete Integrator for use with ECSL.

Copyright (c) 2005 The Regents of the University of California.
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

package vendors.ecsl_dp.ptolemy;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.IIR;
import ptolemy.data.type.BaseType;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// ECSLDiscreteIntegrator
/**
   Discrete Integrator for use with ECSL.
   Currently, this actor is a special case of IIR and only supports
   cases where the <i>IntegratorMethod</i> parameter has the value
   "ForwardEuler".

   @author Christopher Brooks.
   @version : ECSLAbs.java,v 1.1 2004/10/25 22:56:07 cxh Exp $
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/

public class ECSLDiscreteIntegrator extends IIR {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ECSLDiscreteIntegrator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);

        // FIXME: properly handle ExternalReset
        ExternalReset= new StringParameter(this, "ExternalReset");
        IntegratorMethod= new StringParameter(this, "IntegratorMethod");
        InitialConditionSource =
            new StringParameter(this, "InitialConditionSource");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Names the external reset.  A common value is "none"
     *  FIXME: this value is currently ignored.
     */
    public StringParameter ExternalReset;

    /** The name of the integrator method.  Currently, if
     *  the value is anything other than the string "Forward Euler",
     *  an exception is thrown.
     */
    public StringParameter IntegratorMethod;

    /** The name of the initial condition source.
     *  A common value is the string "internal".
     *  FIXME: this value is currently ignored.
     */
    public StringParameter InitialConditionSource;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the value of an attribute.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == IntegratorMethod) {
            if (!IntegratorMethod.getExpression().equals("Forward Euler")) {
                throw new IllegalActionException("Sorry, IntegratorMethod "
                        + "be set to \"Forward Euler\", instead it was set "
                        + "to \"" + IntegratorMethod.getExpression() + "\".");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** FIXME: noop
     *  @exception IllegalActionException If there is no director,
     *   or if addition and subtraction are not supported by the
     *   available tokens.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }
}

