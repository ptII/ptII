/* An actor that reads from images using Open Computer Vision (OpenCV)

 Copyright (c) 2010-2011 The Regents of the University of California.
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

package ptolemy.actor.lib.opencv.javacv;

import static name.audet.samuel.javacv.jna.cv.cvEqualizeHist;
import static name.audet.samuel.javacv.jna.cxcore.IPL_DEPTH_8U;
import name.audet.samuel.javacv.jna.cxcore.IplImage;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ImageEqualizeHist

/**
 * Equalize histogram of image (only for 8-bit single channel image)
  * @author Tatsuaki Iwata, Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ImageEqualizeHist extends Transformer {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>step</i> parameter and the <i>step</i>
     *  port. Initialize <i>init</i>
     *  to IntToken with value 0, and <i>step</i> to IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageEqualizeHist(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    public void fire() throws IllegalActionException {

        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof IplImage)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + inputObject.getClass());
            }
            _srcFrame = (IplImage) inputObject;

            if ((_srcFrame.depth != IPL_DEPTH_8U) || (_srcFrame.nChannels != 1)) {
                throw new IllegalActionException(this,
                        "Input is required to be an 8-bit single channel image. Got depth"
                                + _srcFrame.depth + ", channel "
                                + _srcFrame.nChannels);
            }
            cvEqualizeHist(_srcFrame, _srcFrame);

            output.send(0, new ObjectToken(_srcFrame));
        }
    }

    /** Initialize internal frame.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        //        _dstFrame = null;
    }

    /** Release image.
    *  @exception IllegalActionException If thrown by the super class.
    */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        //        if (_dstFrame != null) {
        //            _dstFrame.release();
        //        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IplImage _srcFrame;
    //    private IplImage _dstFrame;
}
