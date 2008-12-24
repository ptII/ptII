/* A polymorphic distributor.

 Copyright (c) 1997-2007 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.actor.IOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Distributor

/**
 A polymorphic distributor, which splits an input stream into a set of
 output streams. The distributor has an input port and an output port,
 the latter of which is a multiport.
 The types of the ports are undeclared and will be resolved by the type
 resolution mechanism, with the constraint that the output type must be
 greater than or equal to the input type. On each call to the fire method, the
 actor reads at most <i>N</i> tokens from the input, where <i>N</i> is
 the width of the output port times the <i>blockSize</i> parameter,
 and writes <i>blockSize</i> tokens to each output channel,
 in the order of the channels.  If there are fewer than <i>N</i> tokens
 at the input, then the all available input tokens are sent to the output
 channels, and the fire() method returns.  In the next iteration of this
 actor, it will begin producing outputs on the first channel that did
 not have enough tokens in the previous iteration.
 <p>
 For the benefit of domains like SDF, which need to know the token consumption
 or production rate for all ports before they can construct a firing schedule,
 this actor sets the tokenConsumptionRate parameter for the input port
 to equal the number of output channels times the <i>blockSize</i> parameter,
 and the output production rate is set to the <i>blockSize</i> parameter.
 The consumption rate parameter is set each time that a link is established with
 the input port, or when a link is removed.  The director is notified
 that the schedule is invalid, so that if the link is modified at
 run time, the schedule will be recalculated if necessary.

 @author Mudit Goel, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (mudit)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class Distributor extends Transformer implements SequenceActor {
    /** Construct an actor in the specified container with the specified
     *  name. Create ports and make the input port a multiport. Create
     *  the actor parameters.
     *
     *  @param container The container.
     *  @param name This is the name of this distributor within the container.
     *  @exception NameDuplicationException If an actor
     *  with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *  by the proposed container.
     */
    public Distributor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        blockSize = new Parameter(this, "blockSize");
        blockSize.setTypeEquals(BaseType.INT);
        blockSize.setExpression("1");

        // These parameters are required for SDF
        input_tokenConsumptionRate = new WidthDependentParameter(input,
                "tokenConsumptionRate", new IntToken(0), output);

        input_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        input_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate.setPersistent(false);

        output_tokenProductionRate = new Parameter(output,
                "tokenProductionRate");
        output_tokenProductionRate.setVisibility(Settable.NOT_EDITABLE);
        output_tokenProductionRate.setTypeEquals(BaseType.INT);
        output_tokenProductionRate.setExpression("blockSize");
        output_tokenProductionRate.setPersistent(false);

        output.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of tokens produced on each output channel on each firing.
     *  This is an integer with default value 0.
     */
    public Parameter blockSize;

    /** The parameter controlling the input port consumption rate.
     *  This is an integer, initially with value 0. Whenever a connection
     *  is made to the output, the value of this parameter is changed to
     *  equal the width of the output times the <i>blockSize</i> parameter.
     */
    public Parameter input_tokenConsumptionRate;

    /** The parameter specifying the output port production rate.
     *  This is an integer, equal to the value of <i>blockSize</i>.
     */
    public Parameter output_tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the base
     *  class method and sets the public variables to point to the new ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   attributes that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Distributor newObject = (Distributor) super.clone(workspace);
        newObject.input_tokenConsumptionRate = (Parameter) (newObject.input
                .getAttribute("tokenConsumptionRate"));
        ((WidthDependentParameter) newObject.input_tokenConsumptionRate).setPort(newObject.output);
        return newObject;
    }

    /** Notify this entity that the links to the specified port have
     *  been altered.  This sets the consumption rate of the input port
     *  and notifies the director that the schedule is invalid, if there
     *  is a director.  This also sets the current output position to zero,
     *  which means that the next consumed input token will be produced
     *  on channel zero of the output.
     */
    public void connectionsChanged(Port port) {
        super.connectionsChanged(port);

        if (port == output) {
            _currentOutputPosition = 0;

            // NOTE: schedule is invalidated automatically already
            // by the changed connections.
        }
    }

    /** Read at most <i>N</i> tokens from the input port, where <i>N</i>
     *  is the width of the output port times the <i>blockSize</i> parameter.
     *  Write <i>blockSize</i> tokens to each of the
     *  output channels. If  there are not <i>N</i> tokens available,
     *  then read all the tokens and send them to the outputs.
     *  On the next iteration, the actor will pick up where it left off.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        _tentativeOutputPosition = _currentOutputPosition;

        int width = output.getWidth();
        int blockSizeValue = ((IntToken) blockSize.getToken()).intValue();

        for (int i = 0; i < width; i++) {
            if (!input.hasToken(0, blockSizeValue)) {
                break;
            }
            Token[] tokens = input.get(0, blockSizeValue);
            output.send(_tentativeOutputPosition++, tokens, blockSizeValue);

            if (_tentativeOutputPosition >= width) {
                _tentativeOutputPosition = 0;
            }
        }
    }

    /** Begin execution by setting the current output channel to zero.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentOutputPosition = 0;
    }

    /** Update the output position to equal that determined by the most
     *  recent invocation of the fire() method.  The output position is
     *  the channel number of the output port to which the next input
     *  will be sent.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        _currentOutputPosition = _tentativeOutputPosition;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The channel number for the next output.
    private int _currentOutputPosition;

    // The new channel number for the next output as determined by fire().
    private int _tentativeOutputPosition;
    
    /**
     * This class will set _port.getWidth() + " * blockSize" as expression
     * of the parameter, but will only do it when the token is requested to
     * delay the triggering of the width.
     */
    private class WidthDependentParameter extends Parameter {
        public WidthDependentParameter(NamedObj container, String name, Token token,
                IOPort port) throws IllegalActionException, NameDuplicationException {
            super(container, name, token);
            _port = port;
        }

        public ptolemy.data.Token getToken() throws IllegalActionException {
            setExpression(_port.getWidth() + " * blockSize");
            return super.getToken();
        }
        void setPort(IOPort port) {
            _port = port;
        }
        
        private IOPort _port;
    };     
}
