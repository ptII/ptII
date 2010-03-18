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
import java.lang.Object;

import processing.core.PImage;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


///////////////////////////////////////////////////////////////////
//// Read

/**
 * Read a frame
 * @author Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version $Id: OpenCVToAWTImage.java 57204 2010-02-12 00:56:07Z eal $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Copy extends Transformer {
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
    public Copy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeAtLeast(input);
        output.setMultiport(true);
        output.setTypeEquals(BaseType.OBJECT);
      //  output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output an OpenCV object
     *  @exception IllegalActionException If thrown while writing to the port.   
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken)input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof OpenCV)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of OpenCV. Got "
                        + inputObject.getClass());
            }
            // Read the next frame.
            OpenCV openCV = (OpenCV)inputObject;
            output.send(0, new ObjectToken(openCV));
            PImage image_to_copy_one = new PImage();
            PImage image_to_copy_two = new PImage();
            image_to_copy_one = openCV.image();
            OpenCV my_copy = new OpenCV();
            my_copy.allocate(640,480);
            my_copy.copy(image_to_copy_one);
            output.send(1, new ObjectToken(my_copy));
            image_to_copy_two = openCV.image();
            OpenCV third_copy = new OpenCV();
            third_copy.allocate(640,480);
            third_copy.copy(image_to_copy_two);
            output.send(2, new ObjectToken(third_copy));
        }
    }
   
   
}
