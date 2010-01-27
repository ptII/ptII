/* A recursive lattice filter with a port that sets the reflection coefficients.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.sdf.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.RecursiveLattice;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// VariableRecursiveLattice

/**
 This actor implements a recursive (all-pole) filter with a lattice
 structure where the reflection coefficients are supplied at an input port.
 The <i>blockSize</i> parameter specifies the number of inputs
 of the filter that are processed per coefficient set provided on the
 <i>newCoefficients</i> input. If no coefficients are provided on that
 input port, then the default coefficients defined in the base class
 are used. In all other respects, the behavior of
 this actor is the same as that of the base class.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (eal)
 */
public class VariableRecursiveLattice extends RecursiveLattice implements
        ExplicitChangeContext {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VariableRecursiveLattice(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        blockSize = new Parameter(this, "blockSize");
        blockSize.setExpression("1");

        newCoefficients = new TypedIOPort(this, "newCoefficients");
        newCoefficients.setInput(true);
        newCoefficients.setTypeSameAs(reflectionCoefficients);

        output.setTypeSameAs(input);

        input_tokenConsumptionRate = new Parameter(input,
                "tokenConsumptionRate");
        input_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate.setExpression("blockSize");

        output_tokenProductionRate = new Parameter(output,
                "tokenProductionRate");
        output_tokenProductionRate.setTypeEquals(BaseType.INT);
        output_tokenProductionRate.setExpression("blockSize");

        // The reflectionCoefficients parameter is no longer
        // of any use, so it is hidden.
        reflectionCoefficients.setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The number of inputs that use each each coefficient set.
     *  This is an integer that defaults to 1.
     */
    public Parameter blockSize;

    /** The input for new coefficient values.  The type is an array
     *  of doubles.
     */
    public TypedIOPort newCoefficients;

    /** The number of tokens consumed from the input port.
     */
    public Parameter input_tokenConsumptionRate;

    /** The number of tokens produced from the output port.
     */
    public Parameter output_tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Ensure that the blockSize parameter is positive.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the block size is invalid,
     *   or if the base class throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == blockSize) {
            IntToken blockSizeToken = (IntToken) (blockSize.getToken());
            _blockSizeValue = blockSizeToken.intValue();

            if (_blockSizeValue < 1) {
                throw new IllegalActionException(this, "Invalid blockSize: "
                        + _blockSizeValue);
            }
        }

        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        VariableRecursiveLattice newObject = (VariableRecursiveLattice) (super
                .clone(workspace));

        newObject.newCoefficients
                .setTypeSameAs(newObject.reflectionCoefficients);

        // FIXME: Is this needed?  If so, shouldn't it be in the base class?
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Consume the inputs and produce the outputs of the filter.
     *  @exception IllegalActionException If parameter values are invalid,
     *   or if there is no director.
     */
    public void fire() throws IllegalActionException {
        if (newCoefficients.hasToken(0)) {
            ArrayToken coefficientsToken = (ArrayToken) (newCoefficients.get(0));
            reflectionCoefficients.setToken(coefficientsToken);
        }

        for (int i = 0; i < _blockSizeValue; i++) {
            super.fire();
        }
    }

    /** Return a list of variables that this entity modifies.  The
     * variables are assumed to have a change context of the given
     * entity.
     * @return A list of variables.
     * @exception IllegalActionException If the list of modified
     * variables cannot be returned.
     */
    public List getModifiedVariables() throws IllegalActionException {
        List list = new LinkedList();
        list.add(reflectionCoefficients);
        return list;
    }

    /**
     * Return the change context being made explicit.  In simple cases, this
     * will simply be the entity implementing this interface.  However, in
     * more complex cases, directors may implement this interface, or entities
     * may modify parameters according to a different change context (i.e. HDF)
     * @return The change context being made explicit.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Entity getContext() throws IllegalActionException {
        return this;
    }

    /** Return false if the input does not have enough tokens to fire.
     *  Otherwise, return true.
     *  @return False if the number of input tokens available is not at least
     *   equal to the <i>blockSize</i> parameter.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (input.hasToken(0, _blockSizeValue) && newCoefficients.hasToken(0)) {
            return super.prefire();
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _blockSizeValue = 1;
}
