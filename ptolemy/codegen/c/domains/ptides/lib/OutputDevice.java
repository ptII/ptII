/* A code generation helper class for domains.ptides.lib.ActuatorTransmitter
 @Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.ptides.lib;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.domains.ptides.lib.OutputDevice

 @author Jeff C. Jensen, Jia Zou
 @version $Id$
 @since Ptolemy II 7.1
 */
public abstract class OutputDevice extends CCodeGeneratorHelper {

    /**
     * Construct a ActorTransmitter helper.
     * @param actor The associated actor.
     */
    public OutputDevice(ptolemy.domains.ptides.lib.OutputDevice actor) {
        super(actor);
    }

    public String generateActuatorActuationFuncCode() throws IllegalActionException {
        return "";
    }
    
    public String generateHardwareInitializationCode() throws IllegalActionException {
        return "";
    }
}
