/* A source for testing RTPDirector.

 Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.rtp.kernel.test;

import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Source;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TestSource

/**
 This actor produces a ramp at 2Hz.

 @author Jie Liu
 @version $Id$
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestSource extends Source {
    public TestSource(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setTypeEquals(BaseType.DOUBLE);
        frequency = new Parameter(this, "frequency", new DoubleToken(2.0));
        frequency.setTypeEquals(BaseType.DOUBLE);
        attributeChanged(frequency);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          Parameters                        ////

    /** The execution frequency, in terms of Hz. Default is 2.0.
     */
    public Parameter frequency;

    ///////////////////////////////////////////////////////////////////
    ////                          Public Methods                     ////

    /** Once the frequency is updated, calculate the execution period.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == frequency) {
            double f = ((DoubleToken) (frequency.getToken())).doubleValue();

            if (f > 1000) {
                throw new IllegalActionException(this,
                        "does not support frequency higher than 1000.");
            }

            _period = 1000.0 / f;
        }
    }

    public void fire() throws IllegalActionException {
        output.broadcast(new DoubleToken(value));
        value += 1.0;

        Director director = getDirector();
        Time time = director.getModelTime();
        System.out.println("Firing round " + value);
        director.fireAt(this, time.add(_period));
    }

    public void initialize() throws IllegalActionException {
        value = 0.0;
        super.initialize();

        // Director director = getDirector();
        // director.fireAt(this, director.getCurrentTime());
    }

    private double value = 0.0;

    private double _period = 1.0;
}
