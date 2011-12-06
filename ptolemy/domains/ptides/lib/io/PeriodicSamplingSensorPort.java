/* Sensor port.

@Copyright (c) 2008-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.lib.io;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.continuous.lib.PeriodicSampler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  This port provides a specialized TypedIOPort for sensors that 
 *  periodically sample data
 *  used in Ptides. This port just specializes parameters.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class PeriodicSamplingSensorPort extends SensorPort {
    
    public Parameter samplingTime; 

    /** Create a new PeriodicSamplingSensorPort with a given container and a name.
     * @param container The container of the port. 
     * @param name The name of the port.
     * @throws IllegalActionException If parameters cannot be set.
     * @throws NameDuplicationException If name already exists.
     */
    public PeriodicSamplingSensorPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        samplingTime = new Parameter(this, "samplingTime");
        samplingTime.setToken(new DoubleToken(1.0));
        samplingTime.setTypeEquals(BaseType.DOUBLE); 
    }
    
    
    
    
}
