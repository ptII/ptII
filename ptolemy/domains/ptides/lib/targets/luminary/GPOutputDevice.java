/*
@Copyright (c) 2008-2009 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib.targets.luminary;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.ptides.lib.ActuatorOutputDevice;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * This is a class for GPIO pins on the Luminary Micro.
 * This actor will have no effect in model simulations, but
 * allows for code generators to generate the actors.
 *
 * @author Jia Zou, Jeff C. Jensen
 * @version $ld$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating
 *
 */
public class GPOutputDevice extends ActuatorOutputDevice {

    /**
     * Constructs a GPOutputDevice object.
     *
     * @param container The container.
     * @param name The name of this actor within the container.
     * @throws IllegalActionException if the super constructor throws it.
     * @throws NameDuplicationException if the super constructor throws it.
     */
    public GPOutputDevice(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        pin = new Parameter(this, "pin");
        //FIXME: GPIO A7 is an easy-to-use output, but should it be default?
        pin.setExpression("7");
        pad = new StringParameter(this, "pad");
        pad.setExpression("A");
}

///////////////////////////////////////////////////////////////////
////                       parameters                          ////

    /** Which pad (A-G) and pin (0-7) of GPIO to use.
     * FIXME: Verify user has set value between 0 and 7
     */
    public Parameter pin;

    /** Which pad (A-G) of GPIO to use.
     *  FIXME: Verify user has set value between A and H.
     */
    public StringParameter pad;
}
