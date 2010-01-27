/* An actor that hold the last event and outputs a constant signal.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.domains.continuous.lib;

import java.util.Set;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ZeroOrderHold

/**
 Convert discrete events at the input to a continuous-time
 signal at the output by holding the value of the discrete
 event until the next discrete event arrives. Specifically,
 on each firing, if an input is present, then record the
 value of the input. Then produce the recorded value.
 This actor will throw an exception if the input is not
 purely discrete. Specifically, this means that when the input
 is present, the step size of the solver has to be 0.0.

 @author Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class ZeroOrderHold extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public ZeroOrderHold(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        defaultValue = new Parameter(this, "defaultValue");
        
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-25,10 -15,10 -15,-10 5,-10\"/>\n"
                + "<polyline points=\"5,-10 5,0 15,0 15,10 25,10\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                       ////

    /** Default output before any input has received.
     *  The default is empty, indicating
     *  that no output is produced until an input is received.
     *  The type of the output is set to at least the type of
     *  this parameter (and also at least the type of the input).
     */
    public Parameter defaultValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output the latest token consumed from the consumeCurrentEvents()
     *  call.
     *  @exception IllegalActionException If the token cannot be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        if (input.hasToken(0)) {
            ContinuousDirector dir = (ContinuousDirector) getDirector();
            double stepSize = dir.getCurrentStepSize();
            if (stepSize != 0.0) {
                throw new IllegalActionException(this,
                        "Signal at the input port is not purely discrete.");
            }
            _lastToken = input.get(0);
            if (_debugging) {
                _debug("Input value " + _lastToken + " read at time "
                        + dir.getModelTime() + " and microstep "
                        + dir.getIndex());
            }
        }
        if (_lastToken != null) {
            output.send(0, _lastToken);
            if (_debugging) {
                ContinuousDirector dir = (ContinuousDirector) getDirector();
                _debug("Output value " + _lastToken + " sent at time "
                        + dir.getModelTime() + " and microstep "
                        + dir.getIndex());
            }
        }
    }

    /** Initialize token. If there is no input, the initial token is
     *  a Double Token with the value specified by the defaultValue parameter.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastToken = defaultValue.getToken();
    }
    
    /** Override the method in the base class so that the type
     *  constraint for the <i>initialValue</i> parameter will be set
     *  if it contains a value.
     *  @return a list of Inequality objects.
     *  @see ptolemy.graph.Inequality
     */
    public Set<Inequality> typeConstraints() {
        Set<Inequality> typeConstraints = super.typeConstraints();

        try {
            if (defaultValue.getToken() != null) {
                Inequality ineq = new Inequality(defaultValue.getTypeTerm(),
                        output.getTypeTerm());
                typeConstraints.add(ineq);
            }

            Inequality ineq2 = new Inequality(input.getTypeTerm(), output
                    .getTypeTerm());
            typeConstraints.add(ineq2);
        } catch (IllegalActionException ex) {
            // Errors in the initialValue parameter should
            // already have been caught in getAttribute() method
            // of the base class.
            throw new InternalErrorException("Bad defaultValue value!\n" + ex);
        }

        return typeConstraints;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Saved token.
    private Token _lastToken;
}
