/* Base class for actors implementing the Placeable interface.

 Copyright (c) 2007-2008 The Regents of the University of California.
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

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JFrame;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// AbstractPlaceableActor

/**
 Base class for actors that implement the Placeable interface.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (cxh)
 */
public abstract class AbstractPlaceableActor extends TypedAtomicActor implements
        Placeable {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AbstractPlaceableActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _windowProperties = new WindowPropertiesAttribute(this,
                "_windowProperties");
        // Note that we have to force this to be persistent because
        // there is no real mechanism for the value of the properties
        // to be updated when the window is moved or resized. By
        // making it persistent, when the model is saved, the
        // attribute will determine the current size and position
        // of the window and save it.
        _windowProperties.setPersistent(true);
        _paneSize = new SizeAttribute(this, "_paneSize");
        _paneSize.setPersistent(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Specify the container into which this object should be placed.
     *  Obviously, this method needs to be called before the object
     *  is actually placed in a container.  Otherwise, the object will be
     *  expected to create its own frame into which to place itself.
     *  For actors, this method should be called before initialize().
     *  @param container The container in which to place the object, or
     *   null to specify that there is no current container.
     */
    public abstract void place(Container container);

    /** Specify the associated frame and set its properties (size, etc.)
     *  to match those stored in the _windowProperties attribute.
     *  @param frame The associated frame.
     */
    public void setFrame(JFrame frame) {
        _frame = frame;
        _windowProperties.setProperties(_frame);

        // Regrettably, since setSize() in swing doesn't actually
        // set the size of the frame, we have to also set the
        // size of the internal component.
        Component[] components = _frame.getContentPane().getComponents();

        if (components.length > 0) {
            _paneSize.setSize(components[0]);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object. This
     *  overrides the base class to make sure that the current frame
     *  properties, if there is a frame, are recorded.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        // Make sure that the current position of the frame, if any,
        // is up to date.
        if (_frame != null) {
            _windowProperties.recordProperties(_frame);

            // Regrettably, have to also record the size of the contents
            // because in Swing, setSize() methods do not set the size.
            // Only the first component size is recorded.
            Component[] components = _frame.getContentPane().getComponents();

            if (components.length > 0) {
                _paneSize.recordSize(components[0]);
            }
        }

        super._exportMoMLContents(output, depth);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The associated frame. */
    protected JFrame _frame;

    /** A specification of the size of the pane if it's in its own window. */
    protected SizeAttribute _paneSize;

    /** A specification for the window properties of the frame. */
    protected WindowPropertiesAttribute _windowProperties;
}
