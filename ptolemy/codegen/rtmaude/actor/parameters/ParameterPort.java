/* RTMaude Code generator helper class for the ParameterPort class.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.actor.parameters;

import ptolemy.codegen.rtmaude.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ParameterPort

/**
 * Generate RTMaude code for a ParameterPort
 *
 * @see ptolemy.actor.parameters.ParameterPort
 * @author Kyungmin Bae
 * @version $Id: ParameterPort.java 53821 2009-04-12 19:12:45Z cxh $
 * @Pt.ProposedRating Red (kquine)
 *
 */
public class ParameterPort extends IOPort {
    
    public ParameterPort(ptolemy.actor.parameters.ParameterPort component) {
        super(component);
    }
    
    @Override
    public String generateTermCode() throws IllegalActionException {
        ptolemy.actor.parameters.ParameterPort p = 
            (ptolemy.actor.parameters.ParameterPort) getComponent();
        if (p.isOutput())
            throw new IllegalActionException("Not Input Port Parametor");
        
        return _generateBlockCode(defaultTermBlock,
                p.getName(), 
                p.getParameter().getName()
        );
    }
}
