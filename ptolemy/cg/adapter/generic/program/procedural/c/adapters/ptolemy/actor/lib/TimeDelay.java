/*
 @Copyright (c) 2005-2006 The Regents of the University of California.
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

import java.util.LinkedList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A adapter class for ptolemy.actor.lib.TimeDelay.
 *
 * @author Jia Zou
 * @version $Id: TimedDelay.java 54721 2009-06-26 22:32:23Z cxh $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */
public class TimeDelay extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the TimedDelay adapter.
     * @param actor the associated actor
     */
    public TimeDelay(ptolemy.actor.lib.TimeDelay actor) {
        super(actor);
    }

    public String generateFireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();
        Parameter delay = ((ptolemy.actor.lib.TimeDelay) getComponent()).delay;
        double value = ((DoubleToken) delay.getToken()).doubleValue();

        int intPart = (int) value;
        int fracPart = (int) ((value - intPart) * 1000000000.0);
        args.add(Integer.toString(intPart));
        args.add(Integer.toString(fracPart));

        codeStream.appendCodeBlock("fireBlock", args);
        return processCode(codeStream.toString());
    }
}
