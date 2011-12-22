/* Attribute for inserting HTML text into the page exported by Export to Web.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.html;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


///////////////////////////////////////////////////////////////////
//// HTMLText
/**
 * Attribute for inserting HTML text into the page exported by Export to Web.
 * Drag its icon onto the background of a model, and specify the HTML text to
 * export (double click on the attribute to set the text).
 * By default, this text will be placed before the image for the model,
 * after the title, but you can change the position by setting the
 * <i>textPosition</i> parameter. You can also separately control what
 * text is displayed in the model, or make the attribute disappear altogether
 * in the model (for this, just set <i>displayText</i> to an empty string).
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class HTMLText extends WebContent {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public HTMLText(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        textPosition = new HTMLTextPosition(this, "textPosition");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Parameter specifying the position into which to export HTML text.
     * The parameter offers the following possibilities:
     *  <ul>
     *  <li><b>end</b>: Put the text at the end of the HTML file.
     *  <li><b>header</b>: Put the text in the header section.
     *  <li><b>start</b>: Put the text at the start of the body section.
     *  <li><i>anything_else</i>: Put the text in a separate HTML file
     *   named <i>anything_else</i>.
     *  </ul>
     *  The default is "start".
     */
    public HTMLTextPosition textPosition;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This may include, for example, HTML or header
     *  content, including for example JavaScript definitions that
     *  may be needed by the area attributes.
     *  @throws IllegalActionException If parameters cannot be evaluated.
     */
    public void provideContent(WebExporter exporter) throws IllegalActionException {
        String content = stringValue();
        String position = textPosition.stringValue();
        exporter.addContent(position, false, content);
    }

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class does not provide any such content.
     */
    public void provideOutsideContent(WebExporter exporter) {
        // This class does not provide outside content.
    }
}
