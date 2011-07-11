/* Merge streams according to a boolean parameter value.

 Copyright (c) 1997-2010 The Regents of the University of California.
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

 Adapted from the BooleanSelect actor.
 */
package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ConfigurationSelect

/**
 Conditionally merge the streams at two input ports
 depending on the value of the boolean parameter.
 The token in the <i>selector</i> parameter specifies the
 input port that should be read from in the next firing.
 If the <i>selector</i> parameter
 token is false, then the <i>falseInput</i> port is used,
 otherwise the <i>trueInput</i> port is used. In the next
 firing, tokens are consumed from the specified
 port and sent to the <i>output</i> port.
 <p>
 The actor is able to fire if the <i>selector</i> parameter
 is either has a true or false token and there is a token on every
 channel of the specified input port.
 <p>
 If the input port that is read has width greater than an output port, then
 some input tokens will be discarded (those on input channels for which
 there is no corresponding output channel).
 <p>
 Because tokens are immutable, the same Token is sent
 to the output, rather than a copy.  The <i>trueInput</i> and
 <i>falseInput</i> port may receive Tokens of any type.
 <p>
 This actor is designed to be used with the DDF or PN director,
 but it can also be used with SR, DE, and possibly other domains.
 It should not be used with
 SDF because the number of tokens it consumes is not fixed.
 <p>
 This actor is similar to the BooleanSelect actor, except that
 it uses a parameter rather than a control port to determine which input
 to use.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ConfigurationSelect extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ConfigurationSelect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        trueInput = new TypedIOPort(this, "trueInput", true, false);
        trueInput.setMultiport(true);
        falseInput = new TypedIOPort(this, "falseInput", true, false);
        falseInput.setMultiport(true);

        // Default selector value to false
        selector = new Parameter(this, "selector", new BooleanToken(false));
        selector.setTypeEquals(BaseType.BOOLEAN);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(trueInput);
        output.setTypeAtLeast(falseInput);
        output.setMultiport(true);
        output.setWidthEquals(trueInput, true);
        output.setWidthEquals(falseInput, true);

        // For the benefit of the DDF director, this actor sets
        // consumption rate values.
        trueInput_tokenConsumptionRate = new Parameter(trueInput,
                "tokenConsumptionRate");
        trueInput_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        trueInput_tokenConsumptionRate.setTypeEquals(BaseType.INT);

        falseInput_tokenConsumptionRate = new Parameter(falseInput,
                "tokenConsumptionRate");
        falseInput_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        falseInput_tokenConsumptionRate.setTypeEquals(BaseType.INT);

        /** Make the icon show T and F for trueInput and falseInput.
         */
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" " + "width=\"100\" height=\"40\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"-47\" y=\"-3\" "
                + "style=\"font-size:14\">\n" + "T \n" + "</text>\n"
                + "<text x=\"-47\" y=\"15\" " + "style=\"font-size:14\">\n"
                + "F \n" + "</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for tokens on the true path.  The type can be anything.
     */
    public TypedIOPort trueInput;

    /** Input for tokens on the false path.  The type can be anything.
     */
    public TypedIOPort falseInput;

    /** Parameter that selects one of the two input ports.  The type is
     *  BooleanToken that defaults to false.
     */
    public Parameter selector;

    /** The output port.  The type is at least the type of
     *  <i>trueInput</i> and <i>falseInput</i>
     */
    public TypedIOPort output;

    /** This parameter provides token consumption rate for <i>trueInput</i>.
     *  The type is int.
     */
    public Parameter trueInput_tokenConsumptionRate;

    /** This parameter provides token consumption rate for <i>falseInput</i>.
     *  The type is int.
     */
    public Parameter falseInput_tokenConsumptionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ConfigurationSelect newObject = (ConfigurationSelect) super
                .clone(workspace);

        // Set the selector parameter for the cloned actor to the parameter value
        // of the original actor.
        try {
            if (selector != null) {
                if (selector.getToken().equals(BooleanToken.TRUE)) {
                    newObject.selector.setToken(BooleanToken.TRUE);
                } else {
                    newObject.selector.setToken(BooleanToken.FALSE);
                }
            }
        } catch (IllegalActionException ex) {
            throw new CloneNotSupportedException(
                    "Problem with selector parameter "
                            + "in clone method of ConfigurationSelect");
        }

        newObject.output.setTypeAtLeast(newObject.trueInput);
        newObject.output.setTypeAtLeast(newObject.falseInput);
        newObject.output.setWidthEquals(newObject.trueInput, true);
        newObject.output.setWidthEquals(newObject.falseInput, true);

        return newObject;
    }

    /** Read a token from each input port.  If the
     *  <i>selector</i> parameter is true, then output the token consumed from the
     *  <i>trueInput</i> port, otherwise output the token from the
     *  <i>falseInput</i> port.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // Throw an exception if the selector parameter is null or not
        // a boolean value. This should never happen.
        if (selector == null || selector.getToken() == null
                || !(selector.getToken() instanceof BooleanToken)) {
            throw new IllegalActionException(
                    this,
                    "In ConfigurationSelect actor "
                            + getName()
                            + "the selector parameter must be set to a boolean value.");
        } else {
            if (((BooleanToken) selector.getToken()).booleanValue()) {
                for (int i = 0; i < trueInput.getWidth(); i++) {
                    if (output.getWidth() > i) {
                        output.send(i, trueInput.get(i));
                    }
                }
            } else {
                for (int i = 0; i < falseInput.getWidth(); i++) {
                    if (output.getWidth() > i) {
                        output.send(i, falseInput.get(i));
                    }
                }
            }
        }
    }

    /** Initialize this actor so that the <i>falseInput</i> is read
     *  from until a token arrives on the <i>selector</i> input.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        trueInput_tokenConsumptionRate.setToken(_zero);
        falseInput_tokenConsumptionRate.setToken(_zero);
    }

    /** Return true, unless stop() has been called, in which case,
     *  return false.
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {

        // Throw an exception if the selector parameter is null or not
        // a boolean value. This should never happen.
        if (selector == null || selector.getToken() == null
                || !(selector.getToken() instanceof BooleanToken)) {
            throw new IllegalActionException(
                    this,
                    "In ConfigurationSelect actor "
                            + getName()
                            + "the selector parameter must be set to a boolean value.");
        } else {
            if (((BooleanToken) selector.getToken()).booleanValue()) {
                trueInput_tokenConsumptionRate.setToken(_one);
                falseInput_tokenConsumptionRate.setToken(_zero);
            } else {
                trueInput_tokenConsumptionRate.setToken(_zero);
                falseInput_tokenConsumptionRate.setToken(_one);
            }
        }
        return super.postfire();
    }

    /** If the mode is to read a selector token, then return true
     *  if the <i>selector</i> input has a token. Otherwise, return
     *  true if every channel of the input port specified by the most
     *  recently read selector input has a token.
     *  @return False if there are not enough tokens to fire.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();

        // Throw an exception if the selector parameter is null or not
        // a boolean value. This should never happen.
        if (selector == null || selector.getToken() == null
                || !(selector.getToken() instanceof BooleanToken)) {
            throw new IllegalActionException(
                    this,
                    "In ConfigurationSelect actor "
                            + getName()
                            + "the selector parameter must be set to a boolean value.");
        } else {
            if (((BooleanToken) selector.getToken()).booleanValue()) {
                for (int i = 0; i < trueInput.getWidth(); i++) {
                    if (!trueInput.hasToken(i)) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < falseInput.getWidth(); i++) {
                    if (!falseInput.hasToken(i)) {
                        return false;
                    }
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A final static IntToken with value 0. */
    private final static IntToken _zero = new IntToken(0);

    /** A final static IntToken with value 1. */
    private final static IntToken _one = new IntToken(1);
}
