/* Test for ChangeRequest in the DE domain.

 Copyright (c) 1998-2007 The Regents of the University of California.
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
package ptolemy.kernel.util.test;

import java.util.Collections;
import java.util.Enumeration;

import ptolemy.actor.IORelation;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.Recorder;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.lib.Merge;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TestDE

/**
 Test for ChangeRequest in the DE domain.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 @see ptolemy.kernel.util.ChangeRequest
 */
public class TestDE {
    /** Constructor.
     */
    public TestDE() throws IllegalActionException, NameDuplicationException {
        _top = new TypedCompositeActor();
        _top.setName("top");
        _manager = new Manager();
        _director = new DEDirector();
        _top.setDirector(_director);
        _top.setManager(_manager);

        _clock = new Clock(_top, "clock");
        _clock.values.setExpression("{1.0}");
        _clock.offsets.setExpression("{0.0}");
        _clock.period.setExpression("1.0");
        _rec = new Recorder(_top, "rec");
        IORelation relation = (IORelation) _top.connect(_clock.output, _rec.input);
        relation.setWidth(1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Double the period of the clock.  Note that this will take
     *  after the next event is processed, because the next firing
     *  has already been queued.
     */
    public void doublePeriod() {
        // Create an anonymous inner class
        ChangeRequest change = new ChangeRequest(this, "test") {
            protected void _execute() throws Exception {
                _clock.period.setExpression("2.0");
                _clock.period.validate();
            }
        };

        _top.requestChange(change);
    }

    /** Finish a run.  Return the time of the output events.
     */
    public Enumeration finish() throws KernelException {
        while (_director.getModelTime().getDoubleValue() <= 10.0) {
            _manager.iterate();
        }

        _manager.wrapup();
        return Collections.enumeration(_rec.getTimeHistory());
    }

    /** Insert a new clock.
     */
    public void insertClock() {
        // Create an anonymous inner class
        ChangeRequest change = new ChangeRequest(this, "test2") {
            protected void _execute() throws Exception {
                _clock.output.unlinkAll();
                _rec.input.unlinkAll();

                Clock clock2 = new Clock(_top, "clock2");
                clock2.values.setExpression("{2.0}");
                clock2.offsets.setExpression("{0.5}");
                clock2.period.setExpression("2.0");

                Merge merge = new Merge(_top, "merge");
                _top.connect(_clock.output, merge.input);
                _top.connect(clock2.output, merge.input);
                _top.connect(merge.output, _rec.input);

                // Any pre-existing input port whose connections
                // are modified needs to have this method called.
                _rec.input.createReceivers();
            }
        };

        _top.requestChange(change);
    }

    /** Start a run.
     */
    public void start() throws KernelException {
        _manager.initialize();

        // Process up to time 1.0
        while (_director.getModelTime().getDoubleValue() <= 1.0) {
            _manager.iterate();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Manager _manager;

    private Recorder _rec;

    private Clock _clock;

    private TypedCompositeActor _top;

    private DEDirector _director;
}
