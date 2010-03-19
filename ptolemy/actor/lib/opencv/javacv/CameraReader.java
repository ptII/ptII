/* An actor that capture image from camera by using javaCV

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

package ptolemy.actor.lib.opencv.javacv;

import name.audet.samuel.javacv.jna.cv;
import name.audet.samuel.javacv.jna.highgui;

import com.sun.jna.Platform;

import ptolemy.actor.lib.Source;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import static name.audet.samuel.javacv.jna.cxcore.v10.*;
import static name.audet.samuel.javacv.jna.highgui.v10.*;


///////////////////////////////////////////////////////////////////
////CameraReader

/**
* A simple actor starts a video capture process using
* the Open Computer Vision (OpenCV) Library.
* @author Tatsuaki Iwata, Edward A. Lee, Christopher Brooks
* @version 
* @since 
* @Pt.ProposedRating 
* @Pt.AcceptedRating 
*/
public class CameraReader extends Source {
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
   public CameraReader(CompositeEntity container, String name)
           throws IllegalActionException, NameDuplicationException {
       super(container, name);

       // FIXME: create a separate type for OpenCV Frames or
       // create a type for ptolemy.data.AWTImageToken.
       output.setTypeEquals(BaseType.OBJECT);
   }

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////
   /** Output a frame.
    *  @exception IllegalActionException If thrown while writing to the port.   
    */
   public void fire() throws IllegalActionException {
       _frame = cvQueryFrame (_capture);
       
       // FIXME  In only OpenCV 1.0 for win, captured image is need to be flip. 
       if( Platform.isWindows() && highgui.libname.indexOf("100") > 0){
           _frame.origin = IPL_ORIGIN_TL;
           cvFlip(_frame,_frame,0);
       }
       output.send(0, new ObjectToken(_frame));
   }
  
   /** Open the video capture device.
    *  @exception IllegalActionException If thrown by the super class.
    */
   public void initialize() throws IllegalActionException {
       super.initialize();
       _capture = cvCreateCameraCapture(0);
       
       // FIXME: These setting don't work correctly on OpenCV 1.0. (OpenCV2.0 is ok)
       cvSetCaptureProperty (_capture, CV_CAP_PROP_FRAME_WIDTH, 320);
       cvSetCaptureProperty (_capture, CV_CAP_PROP_FRAME_HEIGHT, 240);
   }
   
   /** Stop the capture.
    *  @exception IllegalActionException If thrown by the super class.
    */
   public void wrapup() throws IllegalActionException {
       super.wrapup();
       cvReleaseCapture (_capture.pointerByReference());
   }
  
   ///////////////////////////////////////////////////////////////////
   ////                         private variables                 ////

   private CvCapture _capture;
   private IplImage _frame;
}
