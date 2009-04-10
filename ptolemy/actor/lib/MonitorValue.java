/* Monitor input values.

 Copyright (c) 1998-2009 The Regents of the University of California.
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

import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// MonitorValue

/**
 Monitor inputs by setting the <i>value</i> parameter equal
 to each arriving token.  This actor can be used with
 an icon that displays the value of a parameter to get
 on-screen display of values in a diagram. The value is
 updated only in postfire.

 <p>Note that the icon for this actor is defined in
 <code>ptolemy/actor/lib/genericsinks.xml</code>, which looks something
 like
 <pre>
&lt;entity name="MonitorValue" class="ptolemy.actor.lib.MonitorValue"&gt;
&lt;doc&gt;Monitor and display values&lt;/doc&gt;
   &lt;property name="displayWidth" class="ptolemy.data.expr.Parameter" value="20"/&gt;
   &lt;property name="_icon" class="ptolemy.vergil.icon.UpdatedValueIcon"&gt;
      &lt;property name="attributeName" value="value"/&gt;
      &lt;property name="displayWidth" value="displayWidth"/&gt;
   &lt;/property&gt;
&lt;/entity&gt;
 </pre>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (bilung)
 */
public class MonitorValue extends Sink {
    /** Construct an actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MonitorValue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        initial = new Parameter(this, "initial");

        value = new Parameter(this, "value");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The most recently seen input value.  This parameter has the same
     *  type as the input.
     */
    public Parameter value;

    /** The initial value to be displayed.
     */
    public Parameter initial;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the actor by clearing the display. */
    public void initialize() throws IllegalActionException {
        super.initialize();
        value.setExpression(initial.getExpression());
        value.validate();
    }

    /** Read at most one token from the input and record its value.
     *  @exception IllegalActionException If there is no director.
     *  @return True.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token oldToken = value.getToken();
            Token newToken = input.get(0);
            if (oldToken == null || !oldToken.equals(newToken)) {
                value.setToken(newToken);
                value.validate();
            }
        }

        return true;
    }
}
