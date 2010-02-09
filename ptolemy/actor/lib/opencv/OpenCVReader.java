/* An actor that reads from images using Open Computer Vision (OpenCV)

 Copyright (c) 2010 The Regents of the University of California.
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

package ptolemy.actor.lib.opencv;

import hypermedia.video.OpenCV;

import java.awt.Frame;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Source;
import ptolemy.data.AWTImageToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


///////////////////////////////////////////////////////////////////
//// OpenCVReader

/**
 * A simple actor that uses the Open Computer Vision (OpenCV) Library.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class OpenCVReader extends Source {
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
    public OpenCVReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: create a separate type for OpenCV Frames or
        // create a type for ptolemy.data.AWTImageToken.
        output.setTypeEquals(BaseType.OBJECT);

        _dummyFrame = new Frame();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.   
     */
    public void fire() throws IllegalActionException {
        _openCV.read();
        // create a new image from cv pixels data
        MemoryImageSource mis = new MemoryImageSource( _openCV.width, _openCV.height, _openCV.pixels(), 0, _openCV.width );
        Image image = _dummyFrame.createImage( mis );

        output.send(0, new AWTImageToken(image));
    }
   
    /** Open the video capture device.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _openCV = new OpenCV();
        _openCV.capture( 640, 480 );
        _openCV.cascade( OpenCV.CASCADE_FRONTALFACE_ALT );
    }
   
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The OpenCV object from which we read. */
    private OpenCV _openCV;

    /** The dummy frame, needed to create an image.
     *  FIXME: can't we create an image without this?
     *  Otherwise, this actor might not work in a headless environment.
     *  Since we are reading from a camera, this might be moot, but
     *  a good design would not have this dependency on Frame.   If
     *  this dependency persists, then this actor should be movedd
     *  to actor.lib.gui.opencv.
     */
    private Frame _dummyFrame;
}
