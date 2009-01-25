/* This actor opens a window to display the specified model and applies its
inputs to the model.

@Copyright (c) 2007-2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.StringWriter;
import java.net.URI;

import javax.swing.JFrame;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ActorToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.gt.GTFrameTools;

//////////////////////////////////////////////////////////////////////////
//// ModelView

/**
 This actor opens a window to display the specified model.
 If inputs are provided, they are expected to be MoML strings
 that are to be applied to the model. This can be used, for
 example, to create animations.

 @author  Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
*/
public class ModelView extends TypedAtomicActor implements WindowListener {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModelView(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(ActorToken.TYPE);

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
        output.setTypeEquals(ActorToken.TYPE);

        title = new PortParameter(this, "title");
        title.setStringMode(true);
        title.setExpression("");

        screenLocation = new Parameter(this, "screenLocation");
        screenLocation.setTypeEquals(BaseType.INT_MATRIX);
        screenLocation.setToken("[-1, -1]");

        screenSize = new Parameter(this, "screenSize");
        screenSize.setTypeEquals(BaseType.INT_MATRIX);
        screenSize.setToken("[-1, -1]");

        reopenWindow = new Parameter(this, "reopenWindow");
        reopenWindow.setTypeEquals(BaseType.BOOLEAN);
        reopenWindow.setToken(BooleanToken.FALSE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Read the input, if there is any, and issue a change
     *  request to apply the MoML in the input to the displayed model.
     *  @exception IllegalActionException If there is an error reading
     *   the input.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        title.update();
        String titleValue = ((StringToken) title.getToken()).stringValue();
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                synchronized (this) {
                    ActorToken token = (ActorToken) input.get(i);
                    // Sometimes the model cannot be shown correctly if it is
                    // not reparsed.
                    // Entity model = token.getEntity(new Workspace());
                    MoMLParser parser = new MoMLParser(new Workspace());
                    Entity model;
                    try {
                        StringWriter writer = new StringWriter();
                        token.getMoML(writer);
                        model = (Entity) parser.parse(writer.getBuffer()
                                .toString());
                    } catch (Exception e) {
                        throw new IllegalActionException(this, e,
                                "Unable to reparse model.");
                    }

                    Effigy effigy = GTEntityUtils.findToplevelEffigy(this);
                    if (effigy == null) {
                        // The effigy may be null if the model is closed.
                        return;
                    }
                    Configuration configuration = (Configuration) effigy
                            .toplevel();
                    try {
                        // Compute size of the new frame.
                        IntMatrixToken size = (IntMatrixToken) screenSize
                                .getToken();
                        int width = size.getElementAt(0, 0);
                        int height = size.getElementAt(0, 1);
                        Dimension newSize = null;
                        if (width >= 0 && height >= 0) {
                            newSize = new Dimension(width, height);
                            SizeAttribute sizeAttribute =
                                (SizeAttribute) model.getAttribute(
                                        "_vergilSize", SizeAttribute.class);
                            if (sizeAttribute == null) {
                                sizeAttribute = new SizeAttribute(model,
                                        "_vergilSize");
                            }
                            sizeAttribute.setExpression("[" + newSize.width +
                                    ", " + newSize.height + "]");
                        }

                        Tableau tableau = _tableaus[i];
                        boolean reopen = ((BooleanToken) reopenWindow
                                .getToken()).booleanValue();
                        boolean modelChanged;
                        if (tableau == null || reopen
                                || !(tableau.getFrame() instanceof
                                        BasicGraphFrame)) {
                            if (tableau != null) {
                                tableau.close();
                            }
                            tableau = configuration.openInstance(model, effigy);
                            ((Effigy) tableau.getContainer()).uri.setURI(null);
                            _tableaus[i] = tableau;
                            modelChanged = false;
                        } else {
                            GTFrameTools.changeModel(
                                    (BasicGraphFrame) tableau.getFrame(),
                                    (CompositeEntity) model, true, true);
                            modelChanged = true;
                        }

                        if (!modelChanged) {
                            JFrame frame = tableau.getFrame();

                            // Compute location of the new frame.
                            IntMatrixToken location =
                                (IntMatrixToken) screenLocation.getToken();
                            int x = location.getElementAt(0, 0);
                            int y = location.getElementAt(0, 1);
                            Point newLocation;
                            if (x >= 0 && y >= 0) {
                                newLocation = new Point(x, y);
                            } else {
                                newLocation = frame.getLocation();
                            }

                            if (newSize == null) {
                                newSize = frame.getSize();
                            }

                            // Move the frame to the edge if it exceeds the
                            // screen.
                            Toolkit toolkit = Toolkit.getDefaultToolkit();
                            Dimension screenSize = toolkit.getScreenSize();
                            newLocation.x = Math.min(newLocation.x,
                                    screenSize.width - newSize.width);
                            newLocation.y = Math.min(newLocation.y,
                                    screenSize.height - newSize.height);
                            frame.setLocation(newLocation);
                            frame.addWindowListener(this);
                        }

                        String titleString = null;
                        String modelName = model.getName();
                        URI uri = URIAttribute.getModelURI(model);
                        if (titleValue.equals("")) {
                            if (uri == null || modelName.equals("")) {
                                titleString = "Unnamed";
                            } else {
                                titleString = uri.toString();
                            }
                            titleString += " (" + getName() + ")";
                        } else {
                            titleString = titleValue;
                        }
                        tableau.setTitle(titleString);
                        model.setDeferringChangeRequests(false);
                        output.send(i, token);
                    } catch (NameDuplicationException e) {
                        throw new IllegalActionException(this, e,
                                "Cannot open model.");
                    } catch (Exception e) {
                        throw new IllegalActionException(this, e,
                                "Cannot parse model.");
                    }
                }
            }
        }
    }

    public void initialize() throws IllegalActionException {
        super.initialize();

        synchronized (this) {
            if (_tableaus != null) {
                for (Tableau tableau : _tableaus) {
                    if (tableau != null) {
                        tableau.close();
                    }
                }
            }

            _tableaus = new Tableau[input.getWidth()];
        }
    }

    public void windowActivated(WindowEvent e) {
    }

    public synchronized void windowClosed(WindowEvent e) {
        Window window = (Window) e.getSource();
        if (_tableaus != null) {
            for (int i = 0; i < _tableaus.length; i++) {
                if (_tableaus[i] != null) {
                    JFrame frame = _tableaus[i].getFrame();
                    if (frame == window) {
                        frame.removeWindowListener(this);
                        _tableaus[i] = null;
                    }
                }
            }
        }
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public TypedIOPort input;

    public TypedIOPort output;

    public Parameter reopenWindow;

    public Parameter screenLocation;

    public Parameter screenSize;

    public PortParameter title;

    private Tableau[] _tableaus;
}
