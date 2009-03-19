/* A helper class for actor.lib.javasound.AudioPlayer

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
package ptolemy.codegen.c.actor.lib.javasound;

import java.util.ArrayList;

import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.javasound.AudioPlayer.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (mankit)
 * @Pt.AcceptedRating Yellow (mankit)
 */
public class AudioPlayer extends AudioSDLActor {
    /**
     * Constructor method for the AudioPlayer helper.
     * @param actor the associated actor.
     */
    public AudioPlayer(ptolemy.actor.lib.javasound.AudioPlayer actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Check the bitsPerSample parameter of the actor. If bitsPerSample is
     * equals to 8, then read the <code>fireBlock_8</code>, else read the
     * <code>fireBlock_16</code> from AudioPlayer.c, replace macros with
     * their values and append to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());
        ptolemy.actor.lib.javasound.AudioPlayer actor = (ptolemy.actor.lib.javasound.AudioPlayer) getComponent();
        ArrayList args = new ArrayList();
        args.add("");
        // FIXME: not sure if this is really right.  How do we handle
        // multiple ports?
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            String codeBlock;
            if (Integer.parseInt(actor.bitsPerSample.getExpression()) == 8) {
                codeBlock = "fireBlock_8";
            } else { // assume bitsPerSample == 16
                codeBlock = "fireBlock_16";
            }
            code.append(_generateBlockCode(codeBlock, args));
        }
        return processCode(code.toString());
    }

    /**
     * Generate preinitialization code.
     * Check the bitsPerSample parameter of the actor. If bitsPerSample is
     * equals to 8, then read the <code>preinitBlock_8</code>, else read the
     * <code>preinitBlock_16</code> from AudioPlayer.c, replace macros with
     * their values and return the processed code string.
     * @return The processed code block.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        ptolemy.actor.lib.javasound.AudioPlayer actor = (ptolemy.actor.lib.javasound.AudioPlayer) getComponent();

        if (Integer.parseInt(actor.bitsPerSample.getExpression()) == 8) {
            _codeStream.appendCodeBlock("preinitBlock_8");
        } else { // assume bitsPerSample == 16
            _codeStream.appendCodeBlock("preinitBlock_16");
        }

        return processCode(_codeStream.toString());
    }
}
