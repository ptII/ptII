/*
 @Copyright (c) 2005-2010 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;

/**
 * A adapter class for ptolemy.actor.lib.DiscreteClock.
 *
 * @author Jia Zou
 * @version $Id: TimeDelay.java 57044 2010-01-27 22:41:05Z cxh $
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */
public class DiscreteClock extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the DiscreteClock adapter.
     * @param actor the associated actor
     */
    public DiscreteClock(ptolemy.actor.lib.DiscreteClock actor) {
        super(actor);
    }

//    public String generateFireCode() throws IllegalActionException {
//        CodeStream codeStream = _templateParser.getCodeStream();
//        codeStream.clear();
//        LinkedList args = new LinkedList();
//        Parameter delay = ((ptolemy.actor.lib.TimeDelay) getComponent()).delay;
//        double value = ((DoubleToken) delay.getToken()).doubleValue();
//
//        int intPart = (int) value;
//        int fracPart = (int) ((value - intPart) * 1000000000.0);
//        args.add(Integer.toString(intPart));
//        args.add(Integer.toString(fracPart));
//
//        codeStream.appendCodeBlock("fireBlock", args);
//        return processCode(codeStream.toString());
//    }
}