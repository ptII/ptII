/* A GR scene viewer

 Copyright (c) 2004-2010 The Regents of the University of California.
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
package ptolemy.domains.gr.lib.quicktime;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;

import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.lib.ViewScreen3D;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import quicktime.Errors;
import quicktime.QTSession;
import quicktime.app.display.QTCanvas;
import quicktime.app.image.Paintable;
import quicktime.app.image.QTImageDrawer;
import quicktime.app.image.Redrawable;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.image.CSequence;
import quicktime.std.image.CodecComponent;
import quicktime.std.image.CompressedFrameInfo;
import quicktime.std.image.ImageDescription;
import quicktime.std.image.QTImage;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.VideoMedia;
import quicktime.util.QTHandle;
import quicktime.util.RawEncodedImage;

import com.sun.j3d.utils.universe.SimpleUniverse;

///////////////////////////////////////////////////////////////////
//// MovieViewScreen2D

/**
 A sink actor that renders a two-dimensional scene into a display screen, and
 saves it as a movie using Apple's Quicktime for Java.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
@SuppressWarnings("deprecation")
public class MovieViewScreen3D extends ViewScreen3D implements StdQTConstants,
        Errors {
    /** Construct a ViewScreen2D in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this ViewScreen2D.
     *  @exception IllegalActionException If this actor
     *   is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public MovieViewScreen3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fileName = new FileParameter(this, "fileName");
        fileName.setExpression("System.out");

        frameRate = new Parameter(this, "frameRate");
        frameRate.setTypeEquals(BaseType.INT);
        frameRate.setExpression("30");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////

    /** The file name to write.  This is a string with
     *  any form accepted by FileParameter.  The default value is
     *  "System.out".
     *  @see FileParameter
     */
    public FileParameter fileName;

    /** The frame rate of the resulting video sequence, in frames per
     *  second.  The default is 30 frames per second.  The type is
     *  integer, which must be positive.
     */
    public Parameter frameRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        _frameNumber++;

        try {
            // Paint the frame.
            _imageDrawer.redraw(null);

            // Compress it.
            CompressedFrameInfo info = _videoSequence.compressFrame(_gw,
                    _videoSize, codecFlagUpdatePrevious, _compressedFrame);
            boolean isKeyFrame = info.getSimilarity() == 0;
            System.out.println("f#:" + _frameNumber + ",kf=" + isKeyFrame
                    + ",sim=" + info.getSimilarity());

            ImageDescription desc = _videoSequence.getDescription();

            // Add it to the video stream.
            _videoMedia.addSample(_imageHandle, 0, // dataOffset,
                    info.getDataSize(), 600 / _frameRateValue, // frameDuration, in 1/600ths of a second.
                    desc, 1, // one sample
                    (isKeyFrame ? 0 : mediaSampleNotSync)); // no flags
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Initialize the execution.  Create the MovieViewScreen3D frame if
     *  it hasn't been set using the place() method.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _frameNumber = 0;
        _frameWidth = ((IntToken) horizontalResolution.getToken()).intValue();
        _frameHeight = ((IntToken) verticalResolution.getToken()).intValue();

        try {
            QTSession.open();

            Frame frame = new Frame("foo");
            QTCanvas canv = new QTCanvas(QTCanvas.kInitialSize, 0.5F, 0.5F);
            frame.add("Center", canv);

            Painter painter = new Painter();
            _imageDrawer = new QTImageDrawer(painter, new Dimension(
                    _frameWidth, _frameHeight), Redrawable.kMultiFrame);
            _imageDrawer.setRedrawing(true);

            canv.setClient(_imageDrawer, true);

            frame.pack();
            _file = new QTFile(fileName.asFile());
            _movie = Movie.createMovieFile(_file, kMoviePlayer,
                    createMovieFileDeleteCurFile
                            | createMovieFileDontCreateResFile);

            //
            // add content
            //
            System.out.println("Doing Video Track");

            int kNoVolume = 0;
            int kVidTimeScale = 600;

            _videoTrack = _movie.addTrack(_frameWidth, _frameHeight, kNoVolume);
            _videoMedia = new VideoMedia(_videoTrack, kVidTimeScale);

            _videoMedia.beginEdits();

            _videoSize = new QDRect(_frameWidth, _frameHeight);
            _gw = new QDGraphics(_videoSize);

            int size = QTImage.getMaxCompressionSize(_gw, _videoSize, _gw
                    .getPixMap().getPixelSize(), codecNormalQuality,
                    kAnimationCodecType, CodecComponent.anyCodec);
            _imageHandle = new QTHandle(size, true);
            _imageHandle.lock();
            _compressedFrame = RawEncodedImage.fromQTHandle(_imageHandle);

            _frameRateValue = ((IntToken) frameRate.getToken()).intValue();
            _videoSequence = new CSequence(_gw, _videoSize, _gw.getPixMap()
                    .getPixelSize(), kAnimationCodecType,
                    CodecComponent.bestFidelityCodec, codecNormalQuality,
                    codecNormalQuality, _frameRateValue, //1 key frame every second
                    null, //cTab,
                    0);

            _imageDrawer.setRedrawing(true);

            //redraw first...
            _imageDrawer.redraw(null);

            _imageDrawer.setGWorld(_gw);
            _imageDrawer.setDisplayBounds(_videoSize);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /** Wrapup an execution.  This method completes capture of the
     * video sequence and writes it to the output file.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        try {
            _videoMedia.endEdits();

            int kTrackStart = 0;
            int kMediaTime = 0;
            int kMediaRate = 1;
            _videoTrack.insertMedia(kTrackStart, kMediaTime,
                    _videoMedia.getDuration(), kMediaRate);

            // Save movie to file.
            OpenMovieFile outStream = OpenMovieFile.asWrite(_file);
            _movie.addResource(outStream, movieInDataForkResID, _file.getName());
            outStream.close();
            System.out.println("Finished movie");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        QTSession.close();
    }

    /** Create the view screen component.  If place() was called with
     * a container, then use the container.  Otherwise, create a new
     * frame and use that.
     */
    protected void _createViewScreen() {
        super._createViewScreen();

        GraphicsConfiguration config = SimpleUniverse
                .getPreferredConfiguration();

        _offScreenCanvas = new Canvas3D(config, true);
        _offScreenCanvas.getScreen3D().setSize(_canvas.getScreen3D().getSize());
        _offScreenCanvas.getScreen3D().setPhysicalScreenWidth(
                _canvas.getScreen3D().getPhysicalScreenWidth());
        _offScreenCanvas.getScreen3D().setPhysicalScreenHeight(
                _canvas.getScreen3D().getPhysicalScreenHeight());

        // attach the offscreen canvas to the view
        _canvas.getView().addCanvas3D(_offScreenCanvas);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    private class Painter implements Paintable {
        private Rectangle[] ret = new Rectangle[1];

        public void newSizeNotified(QTImageDrawer drawer, Dimension d) {
            ret[0] = new Rectangle(_frameWidth, _frameHeight);
        }

        public Rectangle[] paint(Graphics g) {
            Point location = _canvas.getLocationOnScreen();
            _offScreenCanvas.setOffScreenLocation(location);

            BufferedImage image = new BufferedImage(_frameWidth, _frameHeight,
                    BufferedImage.TYPE_INT_ARGB);

            ImageComponent2D buffer = new ImageComponent2D(
                    ImageComponent.FORMAT_RGBA, image);

            _offScreenCanvas.setOffScreenBuffer(buffer);
            _offScreenCanvas.renderOffScreenBuffer();
            _offScreenCanvas.waitForOffScreenRendering();
            image = _offScreenCanvas.getOffScreenBuffer().getImage();

            g.drawImage(image, 0, 0, null);

            ret[0] = new Rectangle(_frameWidth, _frameHeight);
            return ret;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private Canvas3D _offScreenCanvas;

    private Movie _movie;

    private QTImageDrawer _imageDrawer;

    private QTHandle _imageHandle;

    private QTFile _file;

    private QDGraphics _gw;

    private QDRect _videoSize;

    private CSequence _videoSequence;

    private RawEncodedImage _compressedFrame;

    private VideoMedia _videoMedia;

    private Track _videoTrack;

    private int _frameWidth = 400;

    private int _frameHeight = 400;

    private int _frameNumber;

    private int _frameRateValue;
}
