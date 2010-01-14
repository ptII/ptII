/* An attribute with a reference to an image.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.vergil.pdfrenderer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.ClassUtilities;
import ptolemy.util.FileUtilities;
import ptolemy.vergil.kernel.attributes.VisibleAttribute;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

//////////////////////////////////////////////////////////////////////////
//// PDFAttribute

/**
 <p>This is an attribute that renders the first page of
 a specified PDF file.  Its <i>source</i>
 parameter specifies a file containing the PDF, and
 its <i>scale</i> attribute specifies a scaling factor, as a percentage.
 </p>
 <p>
 This class uses pdf-renderer, obtainable from
 <a href="https://pdf-renderer.dev.java.net/#in_browser">https://pdf-renderer.dev.java.net/</a>.
 This is an "an open source, all Java library which renders PDF documents
 to the screen using Java2D." This attribute can be put into a
 Vergil diagram and its visual appearance will be be defined
 by a PDF file.  Using this attribute requires that
 PDFRenderer.jar in the classpath, it is usually
 found in $PTII/lib/PDFRenderer.jar.
 </p>
 
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class PDFAttribute extends VisibleAttribute {
    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PDFAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _icon = new PDFIcon(this, "_icon");
        _icon.setPersistent(false);

        source = new FileParameter(this, "source");

        // Put the sample PDF in the local directory so that it stays with this actor.
        // Use $CLASSSPATH intstead of $PTII so that this class can find sample.pdf
        // under Web Start.
        source.setExpression("$CLASSPATH/ptolemy/vergil/pdfrenderer/sample.pdf");
        
        scale = new Parameter(this, "scale");
        scale.setTypeEquals(BaseType.DOUBLE);
        scale.setExpression("100.0");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The scale, as a percentage.
     * This is a double that defaults to 100.0.
     */
    public Parameter scale;

    /** The source image file. This is a file name or URL, where the default
     *  is "$CLASSPATH/ptolemy/vergil/pdfrenderer/sample.pdf".
     */
    public FileParameter source;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the source or scale attributes by changing
     *  the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == source) {
            try {
                ByteBuffer byteBuffer = null;
                try {
                    File file = source.asFile();
                    RandomAccessFile raf = new RandomAccessFile(file, "r");
                    FileChannel channel = raf.getChannel();
                    byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                } catch (Exception ex) {
                    URL jarURL = null;
                    // We might be under WebStart.  In theory, we should be able to read
                    // the URL and create a ByteBuffer, but there are problems with the non-ascii bytes
                    // in the pdf file.  The basic idea was to new BufferedOutputStream(new ByteArrayOutputStream()).
                    try {
                        jarURL = FileUtilities.nameToURL(source.getExpression(), null, null);
                    } catch (Exception ex2) {
                        throw new IllegalActionException(this, ex, "Failed to open " + source.getExpression()
                                                         + ". Tried opening as URL, exception was: " + ex2);
                    }
                    if (! jarURL.toString().startsWith("jar:")) {
                        throw new IllegalActionException(this, ex, "Failed to open " + source.getExpression());
                    } else {
                        try {
                            byte [] contents = FileUtilities.binaryReadURLToByteArray(jarURL);
                            byteBuffer = ByteBuffer.wrap(contents);
                        } catch (Exception ex3) {
                            throw new IllegalActionException(this, ex, "Failed to open " + source.getExpression()
                                    + ".  Also, tried to open jar URL " + jarURL + ", exception was: \n"
                                    + KernelException.stackTraceToString(ex3));
                        }
                    }
                }

                PDFFile pdffile = new PDFFile(byteBuffer);
    
                // draw the first page to an image
                PDFPage page = pdffile.getPage(0);
                
                _icon.setPage(page);
            } catch (IOException ex) {
                // FIXME: Better would be to show an ERROR icon
                // like ImageAttribute does.
                throw new IllegalActionException(this, ex, "Cannot read PDF file.");
            }
        } else if (attribute == scale) {
            double scaleValue = ((DoubleToken) scale.getToken()).doubleValue();
            _icon.setScale(scaleValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ///                         private variables                 ////

    /** The PDF icon. */
    private PDFIcon _icon;
}
