/* An abstract ancestor for filter actors acting on shared buffers

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

package ptolemy.domains.sdf.optimize;

import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////SharedBufferTransformer

/**
<h1>Class comments</h1>
SharedBufferTrasnformer is an abstract ancestor class to be used for filters
using references to shared data frames.
It implements a default version of the BufferingProfile interface.
<p>
See {@link ptolemy.domains.sdf.optimize.OptimizingSDFDirector} and 
{@link ptolemy.domains.sdf.optimize.BufferingProfile} for more information.
</p>
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector
@see ptolemy.domains.sdf.optimize.BufferingProfile

@author Marc Geilen
@version $Id$
@since Ptolemy II 0.2
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
*/

public abstract class SharedBufferTransformer extends Transformer implements BufferingProfile {

    /**
     * Constructor 
     * @param container container
     * @param name name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public SharedBufferTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }
    
    /**
     * initialize. Set the default firing to non exclusive.
     */
    public void initialize() throws IllegalActionException {
        // default to copying firing
        _nextIterationExclusive = false;
        super.initialize();
    }

    /**
     * Fire according to the value _nextIterationExclusive in shared or exclusive 
     * firing mode
     */
    public void fire() throws IllegalActionException {
        if(_nextIterationExclusive){
            fireExclusive();
        } else {
            fireCopying();
        }
    }

    /**
     * Default value for number of frame buffers required for shared firing.
     */
    public int sharedBuffers() {
        return 1;
    }

    /**
     * Default value for number of frame buffers required for exclusive firing.
     */
    public int exclusiveBuffers() {
        return 0;
    }

    /**
     * Default value for execution time for shared firing.
     */
    public int sharedExecutionTime() {
        return 1;
    }

    /**
     * Default value for execution time for exclusive firing.
     */
    public int exclusiveExecutionTime() {
        return 2;
    }

    /**
     * exclusive firing method to be implemented in subclasses
     * @throws IllegalActionException
     */
    protected abstract void fireExclusive() throws IllegalActionException;

    /**
     * shared firing method to be implemented in subclasses
     * @throws IllegalActionException
     */
    protected abstract void fireCopying() throws IllegalActionException;
 
    /** 
     * Invoke a specified number of iterations of the actor in either shared or 
     * exclusive mode as indicated by the fireExclusive argument.
     * @param iterationCount The number of iterations to perform.
     * @param fireExclusive whether to fire exclusive or not.
     * @return NOT_READY, STOP_ITERATING, or COMPLETED.
     * @exception IllegalActionException If iterating is not
     *  permitted, or if prefire(), fire(), or postfire() throw it.
     **/
    public int iterate(int iterationCount, boolean fireExclusive)
        throws IllegalActionException {
        _nextIterationExclusive  = fireExclusive;
        int result = super.iterate(iterationCount);
        // default to copying firing
        _nextIterationExclusive = false;
        return result;
    }
    
    //// private fields
    /**
     * determines whether the next firing will be exclusive or shared
     */
    private boolean _nextIterationExclusive;
    
}
