/* A factory for representatives of an HTML file.

 Copyright (c) 1998-2009 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.net.URL;
import java.net.URLConnection;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HTMLEffigyFactory

/**
 A factory for creating new effigies for HTML pages.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class HTMLEffigyFactory extends EffigyFactory {
    /** Create a factory in the specified workspace.
     *  @param workspace The workspace.
     */
    public HTMLEffigyFactory(Workspace workspace) {
        super(workspace);
    }

    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public HTMLEffigyFactory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return false, indicating that this effigy factory is not
     *  capable of creating an effigy without a URL being specified.
     *  There is no point in creating an unmodifiable blank HTML page.
     *  @return False.
     */
    public boolean canCreateBlankEffigy() {
        return false;
    }

    /** Create a new effigy in the given container by reading the specified
     *  URL. The extension of the URL must be ".htm" or ".html", or
     *  the content type must be "text/html" or "text/rtf". Otherwise,
     *  this returns null.  It will also return null if there is no
     *  access to the network.
     *  @param container The container for the effigy.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.  This is ignored in this
     *   class.
     *  @param in The input URL.
     *  @return A new instance of HTMLEffigy, or null if one cannot
     *   be created.
     *  @exception Exception If the URL cannot be read, or if the data
     *   is malformed in some way.
     */
    public Effigy createEffigy(CompositeEntity container, URL base, URL in)
            throws Exception {
        if (in == null) {
            return null;
        }

        String extension = getExtension(in);

        // Here, if it has an "http" protocol, we agree to
        // open it.  The reason is that many main HTML pages are
        // referenced by a string like "http://ptolemy.eecs.berkeley.edu".
        // Here, the extension will be "edu" rather than HTML.
        // Note that this means that if we add effigies for, say,
        // PDF files or images, their factories should be listed before
        // this one.
        if (!extension.equals("htm") && !extension.equals("html")) {
            // The extension doesn't match.  Try the content type.
            URLConnection connection = in.openConnection();

            if (connection == null) {
                return null;
            }

            String contentType;
            try {
                contentType = connection.getContentType();
            } catch (SecurityException ex) {
                throw new SecurityException(
                        "Failed to open " + base + " " + in, ex);
            }

            if (contentType == null) {
                return null;
            }

            if (!contentType.startsWith("text/html")
                    && !contentType.startsWith("text/rtf")) {
                return null;
            }
        }

        // Create a new effigy.
        HTMLEffigy effigy = new HTMLEffigy(container, container
                .uniqueName("effigy"));
        effigy.uri.setURL(in);

        // FIXME: What to do about the base?
        return effigy;
    }
}
