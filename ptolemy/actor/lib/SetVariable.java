/* Set the value of a variable contained by the container.

 Copyright (c) 2003-2007 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// SetVariable

/**
 <p>Set the value of a variable contained by the container.  The result
 may occur at two different times, depending on the value of the
 <it>delayed</it> parameter.
 </p><p>
 If <i>delayed</i> is true, then the change to
 the value of the variable is implemented in a change request, and
 consequently will not take hold until the end of the current
 toplevel iteration.  This helps ensure that users of value of the
 variable will see changes to the value deterministically
 (independent of the schedule of execution of the actors).
 However, there is a substantial performance penalty associated
 with this when running within Vergil because change requests
 trigger repainting the Vergil window.
 </p><p>
 If <i>delayed</i> is false, then the change to the value of
 the variable is performed immediately.  This allows more frequent
 reconfiguration, and can mimic the operation of PGM's graph
 variables. However, this can result in nondeterminism if
 the variable values are observed by any other actor in
 the system. If you are trying to communicate with another
 actor without wiring, use the Publisher and Subscriber
 actors instead.
 </p><p>
 The <i>output</i> port produces the same token provided at
 the <i>input</i> port when the actor fires, after the
 specified variable has been set. This can be used, even with
 <i>delayed</i> set to false, to ensure determinacy, by
 triggering downstream actions only after the variable has
 been set.
 </p><p>
 The variable can be either any attribute that implements
 the Settable interface. If it is in addition an instance of
 Variable, then the input token is used directly to set the
 value, and the type of the variable is constrained to be
 the same as the type of the input. Otherwise, then input
 token is converted to a string and the setExpression() method
 on the variable is used to set the value.

 </p><p>The variable can occur anywhere in the hierarchy above
 the current level.  If the variable is not found in the container,
 then the container of the container is checked until we reach the
 top level.</p>

 @author Edward A. Lee, Steve Neuendorffer, Contributor: Blanc, Bert Rodiers
 @see Publisher
 @see Subscriber
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red (cxh)
 */
public class SetVariable extends TypedAtomicActor implements ChangeListener,
        ExplicitChangeContext {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SetVariable(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);

        variableName = new StringAttribute(this, "variableName");

        delayed = new Parameter(this, "delayed");
        delayed.setTypeEquals(BaseType.BOOLEAN);
        delayed.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Do nothing.
     *  @param change The change that executed.
     */
    public void changeExecuted(ChangeRequest change) {
    }

    /** React to the fact that a change failed by setting a flag
     *  that causes an exception to be thrown in next call to prefire()
     *  or wrapup().
     *  @param change The change request.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, java.lang.Exception exception) {
        _setFailed = true;
        MessageHandler.error("Failed to set variable.", exception);
    }

    /**
     * Return the change context being made explicit.  In this case,
     * the change context returned is this actor.
     * @return The change context being made explicit
     */
    public Entity getContext() {
        try {
            if (delayed.getToken().equals(BooleanToken.TRUE)) {
                return (Entity) toplevel();
            } else {
                return this;
            }
        } catch (IllegalActionException ex) {
            return this;
        }
    }

    /** Return the (presumably Settable) attribute modified by this
     *  actor.  This is the attribute in the container of this actor
     *  with the name given by the variableName attribute.  If no such
     *  attribute is found, then this method creates a new variable in
     *  the actor's container with the correct name.  This method
     *  gets write access on the workspace.
     *  @exception IllegalActionException If the variable cannot be found.
     *  @return The attribute modified by this actor.
     */
    public Attribute getModifiedVariable() throws IllegalActionException {
        if (_workspace.getVersion() == _attributeVersion) {
            return _attribute;
        }
        NamedObj container = getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "No container.");
        }

        String variableNameValue = variableName.getExpression();
        _attribute = null;

        if (!variableNameValue.equals("")) {
            // Look for the variableName anywhere in the hierarchy
            _attribute = ModelScope.getScopedAttribute(null, container,
                    variableNameValue);

            if (_attribute == null) {
                try {
                    workspace().getWriteAccess();

                    // container might be null, so create the variable
                    // in the container of this actor.
                    _attribute = new Variable(getContainer(), variableNameValue);
                } catch (NameDuplicationException ex) {
                    throw new InternalErrorException(ex);
                } finally {
                    workspace().doneWriting();
                }
            }
            _attributeVersion = _workspace.getVersion();
        }
        return _attribute;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list of variables that this entity modifies.  The
     * variables are assumed to have a change context of the given
     * entity.
     * @return A list of variables.
     * @exception IllegalActionException If the list of modified
     * variables cannot be returned.
     */
    public List getModifiedVariables() throws IllegalActionException {
        Attribute attribute = getModifiedVariable();
        List list = new ArrayList(1);

        if (attribute instanceof Variable) {
            list.add(attribute);
        }

        return list;
    }

    /** Read at most one token from the input port and issue a change
     *  request to update variables as indicated by the input.
     *  @exception IllegalActionException If thrown reading the input.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            final Token value = input.get(0);

            if (delayed.getToken().equals(BooleanToken.TRUE)) {
                // If the previous set failed, then return false.
                // We cannot just proceed with another request because
                // it will likely trigger the same exception again,
                // and the model will just repeatedly pop up exception
                // windows, with no escape.
                if (_setFailed) {
                    return false;
                }
                // We can't filter change request here with an extra condition
                // _updateNecessary(value) since it might be that there is
                // more than one SetVariable setting the same variable with
                // a different priority.
                // Suppose you have variable a. a currently has value 1. a is set to 2
                // by a lower priority SetVariable actor and to one by the higher one.
                // With the test "if (_updateNecessary(value))", the value would not be
                // set to 1 and hence the value would change this two instead of one.
                // $PTII/ptolemy/actor/parameters/test/auto/Priority4.xml
                // tests this behavior.
                
                ChangeRequest request = new ChangeRequest(this,
                        "SetVariable change request") {
                    protected void _execute() throws IllegalActionException {
                        _setValue(value);
                    }
                };

                // To prevent prompting for saving the model, mark this
                // change as non-persistent.
                request.setPersistent(false);
                request.addChangeListener(this);
                requestChange(request);
            } else {
                _setValue(value);
            }
            if (output.isOutsideConnected()) {
                output.send(0, value);
            }
        }

        return true;
    }

    /** If there is no variable with the specified name, then create one.
     *  This is done in preinitialize() so that we can set up a type
     *  constraint that ensures that the type of the variable is at
     *  least that of the input port.
     *  @exception IllegalActionException If the superclass throws it,
     *   or if there is no container.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        Attribute attribute = getModifiedVariable();

        if (attribute instanceof Variable) {
            ((Variable) attribute).setTypeAtLeast(input);
        }

        _setFailed = false;
    }

    /** Parameter that determines when reconfiguration occurs. */
    public Parameter delayed;

    /** The input port. */
    public TypedIOPort input;

    /** The output port. */
    public TypedIOPort output;

    /** The name of the variable in the container to set. */
    public StringAttribute variableName;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

//    /**Test based on the value of the new token whether 
//     * an update is necessary.
//     * This method is conversative in that sense that it might say
//     * that an update is necessary, while strictly it isn't. This for
//     * example when it is difficult to determine whether something will
//     * actually change (for example with expressions, where the expression
//     * might remain the same, but the actual values might have changed).
//     * @param newToken The new token. 
//     * @return True when an update needs to be done.
//     */
//    private boolean _updateNecessary(Token newToken) throws IllegalActionException {
//        Attribute variable = getModifiedVariable();
//
//        if (variable instanceof Variable) {
//            Token oldToken = ((Variable) variable).getToken();
//            
//            return oldToken == null || !oldToken.equals(newToken);
//        }
//        return true;
//    }
    
    
    /** Set the value of the associated container's variable.
     *  @param value The new value.
     */
    private void _setValue(Token value) throws IllegalActionException {
        Attribute variable = getModifiedVariable();

        if (variable instanceof Variable) {
            Token oldToken = ((Variable) variable).getToken();
            
            if (oldToken == null || !oldToken.equals(value)) {
                ((Variable) variable).setToken(value);

                // NOTE: If we don't call validate(), then the
                // change will not propagate to dependents.
                ((Variable) variable).validate();
            }
        } else if (variable instanceof Settable) {
            ((Settable) variable).setExpression(value.toString());

            // NOTE: If we don't call validate(), then the
            // change will not propagate to dependents.
            ((Settable) variable).validate();
        } else {
            throw new IllegalActionException(SetVariable.this,
                    "Cannot set the value of the variable " + "named: "
                            + variableName.getExpression());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** Cached reference to the associated variable. */
    private Attribute _attribute;

    /** Workspace version for the cached attribute reference. */
    private long _attributeVersion = -1;

    /** Indicator that setting the variable failed. */
    private boolean _setFailed = false;
}
