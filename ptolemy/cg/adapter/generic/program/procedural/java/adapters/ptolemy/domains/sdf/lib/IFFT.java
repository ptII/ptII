/* A code generation adapter class for domains.sdf.lib.IFFT
 @Copyright (c) 2007-2010 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.sdf.lib;

import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation adapter class for ptolemy.domains.sdf.lib.IFFT.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class IFFT extends ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.lib.IFFT {

    /**
     * Construct a IFFT adapter.
     * @param actor The associated actor.
     */
    public IFFT(ptolemy.domains.sdf.lib.IFFT actor) {
        super(actor);
    }
    /** Generate the initialize code for the IFFT actor by
     *  declaring the initial values of the sink channels of the
     *  output port of the IFFT actor.
     *  @return The generated initialize code for the IFFT actor.
     *  @exception IllegalActionException If the base class throws it,
     *   or if the initial
     *   outputs of the IFFT actor is not defined.
     */
    public String generateInitializeCode() throws IllegalActionException {
        String results = super.generateInitializeCode();
        ((ProceduralCodeGenerator) getCodeGenerator()).addInclude("ptolemy.math.Complex");
        ((ProceduralCodeGenerator) getCodeGenerator()).addInclude("ptolemy.math.SignalProcessing");
        return results;
    }

}