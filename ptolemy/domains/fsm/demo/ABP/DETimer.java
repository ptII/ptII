/* An actor that implements a resettable timer.

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
package ptolemy.domains.fsm.demo.ABP;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DETimer

/**
 The input sets the time interval before the next expire.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class DETimer extends TypedAtomicActor {
    /** Constructor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DETimer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        expired = new TypedIOPort(this, "expired", false, true);
        expired.setTypeEquals(BaseType.GENERAL);
        set = new TypedIOPort(this, "set", true, false);
        set.setTypeEquals(BaseType.DOUBLE);

        //        set.delayTo(expired);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to declare that the <i>expired</i>
     *  port does not depend on the <i>set</i> port in a firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void declareDelayDependency() throws IllegalActionException {
        // Declare that output does not immediately depend on the input,
        // though there is no lower bound on the time delay.
        _declareDelayDependency(set, expired, 0.0);
    }
    
    /** Reset the timer if there is a token in port set. Otherwise send
     *  a token to port expire if the current time agrees with the time
     *  the timer is set to expire.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        DEDirector dir = (DEDirector) getDirector();
        Time now = dir.getModelTime();

        if (set.hasToken(0)) {
            // reset timer
            double delay = ((DoubleToken) set.get(0)).doubleValue();

            if (delay > 0.0) {
                _expireTime = now.add(delay);
                dir.fireAt(this, _expireTime);
            } else {
                // disable timer
                _expireTime = new Time(dir, -1.0);
            }

            //System.out.println("Reset DETimer " + this.getFullName() +
            //        " to expire at " + _expireTime);
        } else if (now.equals(_expireTime)) {
            // timer expires
            expired.broadcast(_outToken);

            //System.out.println("DETimer " + this.getFullName() +
            //        " expires at " + getCurrentTime());
        }
    }

    /** Initialize the timer.
     *  @exception IllegalActionException If the initialize() of the parent
     *   class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _expireTime = new Time(getDirector(), -1.0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** @serial Set port. */
    public TypedIOPort set;

    /** @serial Expired port. */
    public TypedIOPort expired;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial So we don't need to create the token every time the
     *  timer expires.
     */
    private static final Token _outToken = new Token();

    /** @serial The time to expire.*/
    private Time _expireTime;
}
