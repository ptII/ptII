/*

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.util.Collection;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.gt.IterativeParameterIcon;

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class IterativeParameter extends Parameter implements MatchCallback,
        ValueIterator {

    public IterativeParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        initial = new Parameter(this, "initial");
        constraint = new Parameter(this, "constraint");
        constraint.setTypeAtMost(BaseType.BOOLEAN);
        next = new Parameter(this, "next");
        mode = new ChoiceParameter(this, "mode", Mode.class);

        new IterativeParameterIcon(this, "_icon");

        setTypeAtLeast(initial);
        setTypeAtLeast(next);
    }

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == initial) {
            setToken(initial.getToken());
            validate();
        } else if (attribute == constraint) {
            _validateConstraint();
        }
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        IterativeParameter newObject = (IterativeParameter) super
                .clone(workspace);
        newObject.setTypeAtLeast(newObject.initial);
        newObject.setTypeAtLeast(newObject.next);
        return newObject;
    }

    public boolean foundMatch(GraphMatcher matcher) {
        _foundMatch = true;
        return true;
    }

    public Token initial() throws IllegalActionException {
        Token initialToken = initial.getToken();
        setToken(initialToken);
        _foundMatch = false;
        return initialToken;
    }

    public Token next() throws IllegalActionException {
        Object mode = this.mode.getChosenValue();
        if (mode == Mode.STOP_WHEN_MATCH && _foundMatch) {
            throw new IllegalActionException("Stop because the last match "
                    + "was successful.");
        } else if (mode == Mode.STOP_WHEN_NOT_MATCH && !_foundMatch) {
            throw new IllegalActionException("Stop because the last match "
                    + "was not successful.");
        }

        Token nextToken = next.getToken();
        setToken(nextToken);
        validate();
        _foundMatch = false;
        return nextToken;
    }

    public Collection<?> validate() throws IllegalActionException {
        Collection<?> result = super.validate();
        _validateConstraint();
        return result;
    }

    public Parameter constraint;

    public Parameter initial;

    public ChoiceParameter mode;

    public Parameter next;

    public class ConstraintViolationException extends IllegalActionException {

        ConstraintViolationException() {
            super("Constraint " + constraint.getExpression()
                    + " is not satisfied.");
        }
    }

    public enum Mode {
        ALL_VALUES {
            public String toString() {
                return "try all values";
            }
        },
        STOP_WHEN_MATCH {
            public String toString() {
                return "stop when match";
            }
        },
        STOP_WHEN_NOT_MATCH {
            public String toString() {
                return "stop when not match";
            }
        },
    }

    protected void _validateConstraint() throws IllegalActionException {
        String constraintExpression = constraint.getExpression();
        if (constraintExpression != null && !constraintExpression.equals("")) {
            if (!((BooleanToken) constraint.getToken()).booleanValue()) {
                throw new ConstraintViolationException();
            }
        }
    }

    private boolean _foundMatch = false;
}
