/* A token that contains a reference to an arbitrary object.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.data;

import ptolemy.actor.Actor;
import ptolemy.data.type.Type;

/**
 * Token sent to a ResourceActor containing the Task to be scheduled and the execution 
 * time for that task. 
 * @author Patricia Derler
 * @version $Id: ObjectToken.java 50178 2008-07-25 23:10:40Z tfeng $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 *
 */
public class ResourceToken extends Token {

    /** Construct a token that contains the taks to be scheduled and the execution time for
     *  that task.
     *  @param actorToSchedule The actor to be scheduled.
     *  @param requestedValue The requested execution time.
     */
    public ResourceToken(Actor actorToSchedule, Object requestedValue) {
        super();
        this.actorToSchedule = actorToSchedule;
        this.requestedValue = requestedValue;
    }
    
    /** The actor to be scheduled. */
    public Actor actorToSchedule;

    /** The requested execution time. */
    public Object requestedValue;
    
    @Override
    public Type getType() {
        // TODO Auto-generated method stub
        return super.getType();
    }
    
}
