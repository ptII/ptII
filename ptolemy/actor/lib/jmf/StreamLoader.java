/* Load a datasource from a URL.

 Copyright (c) 2003-2011 The Regents of the University of California.
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
package ptolemy.actor.lib.jmf;

import java.net.URL;

import javax.media.Manager;
import javax.media.protocol.DataSource;

import ptolemy.actor.Director;
import ptolemy.actor.lib.Source;
import ptolemy.actor.util.Time;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// StreamLoader

/** Load a datasource from a URL.
 <p> This actor generates an ObjectToken that contains a DataSource.
 <p> This actor is typically connected to a {@link VideoPlayer} actor.

 @author James Yeh
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class StreamLoader extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StreamLoader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.OBJECT);
        fileOrURL = new FileParameter(this, "fileOrURL");
    }

    /** The file or URL that it to be read.
     */
    public FileParameter fileOrURL;

    public void initialize() throws IllegalActionException {
        super.initialize();

        URL url = fileOrURL.asURL();

        if (url == null) {
            throw new IllegalActionException("URLToken was null");
        } else {
            try {
                _dataSource = Manager.createDataSource(url);
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Invalid URL " + url);
            }
        }

        Director director = getDirector();

        if (director != null) {
            director.fireAt(this, new Time(director));
        } else {
            throw new IllegalActionException(this, "No director");
        }
    }

    public boolean postfire() throws IllegalActionException {
        if (getDirector().getModelTime().getDoubleValue() == 0.0) {
            output.send(0, new ObjectToken(_dataSource));
        }

        return super.postfire();
    }

    private DataSource _dataSource;
}
