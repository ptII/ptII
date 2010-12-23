package ptolemy.domains.jogl.renderingActive;


// CubeGL.java
// Andrew Davison, November 2006, ad@fivedots.coe.psu.ac.th

/* A JFrame contains a JPanel which holds a Canvas subclass called
   CubeCanvasGL. The canvas displays a rotating coloured cube
   using active rendering.

   The application is paused when it is minimized or deactivated. 
   The window can be resized.

   The code uses the JSR-231 1.0.0 release build of JOGL, 
   14th September 2006.

   The canvas could be added directly to the JFrame, but this 
   approach allows other components can be included in the JFrame. 
   In this example, there's a textfield which reports the cube 
   rotations in the x,y,z- axes.
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; 
import java.text.DecimalFormat;

import javax.media.opengl.*;


public class CubeGL extends JFrame implements WindowListener
{
  private static int DEFAULT_FPS = 80;

  private static final int PWIDTH = 512;   // size of panel
  private static final int PHEIGHT = 512; 

  private CubeCanvasGL canvas;

  private JTextField rotsTF;   // displays cube rotations
  private DecimalFormat df = new DecimalFormat("0.#");  // 1 dp


  public CubeGL(long period) 
  {
    super("CubeGL (Active)");

    Container c = getContentPane();
    c.setLayout( new BorderLayout() );
    c.add( makeRenderPanel(period), BorderLayout.CENTER);

    rotsTF = new JTextField("Rotations: ");
    rotsTF.setEditable(false);
    c.add(rotsTF, BorderLayout.SOUTH);

    addWindowListener(this);

    pack();
    setVisible(true);
  } // end of CubeGL()


  private JPanel makeRenderPanel(long period)
  // construct the canvas, inside a JPanel
  {
    JPanel renderPane = new JPanel();
    renderPane.setLayout( new BorderLayout() );
    renderPane.setOpaque(false);
    renderPane.setPreferredSize( new Dimension(PWIDTH, PHEIGHT));

    canvas = makeCanvas(period);
    renderPane.add(canvas, BorderLayout.CENTER);

    canvas.setFocusable(true);
    canvas.requestFocus();    // the canvas now has focus, so receives key events

    // detect window resizes, and reshape the canvas accordingly
    renderPane.addComponentListener( new ComponentAdapter() {
      public void componentResized(ComponentEvent evt)
      { Dimension d = evt.getComponent().getSize();
        // System.out.println("New size: " + d);
        canvas.reshape(d.width, d.height);
      } // end of componentResized()
    });

    return renderPane;
  }  // end of makeRenderPanel()


  private CubeCanvasGL makeCanvas(long period)
  {
    // get a configuration suitable for an AWT Canvas (for CubeCanvasGL)
    GLCapabilities caps = new GLCapabilities();

    AWTGraphicsDevice dev = new AWTGraphicsDevice(null);
    AWTGraphicsConfiguration awtConfig = (AWTGraphicsConfiguration)
       GLDrawableFactory.getFactory().chooseGraphicsConfiguration(caps, null, dev);

    GraphicsConfiguration config = null;
    if (awtConfig != null)
      config = awtConfig.getGraphicsConfiguration();

    return new CubeCanvasGL(this, period, PWIDTH, PHEIGHT, config, caps);
  } // end of makeCanvas()


  public void setRots(float rotX, float rotY, float rotZ)
  // called from CubeCanvasGL to show cube rotations
  {  rotsTF.setText("Rotations: (" + df.format(rotX) + ", " + 
                                     df.format(rotY) + ", " + 
                                     df.format(rotZ) + ")");  }



  // ----------------- window listener methods -------------

  public void windowActivated(WindowEvent e) 
  { canvas.resumeGame();  }

  public void windowDeactivated(WindowEvent e) 
  {  canvas.pauseGame();  }

  public void windowDeiconified(WindowEvent e) 
  {  canvas.resumeGame();  }

  public void windowIconified(WindowEvent e) 
  {  canvas.pauseGame(); }

  public void windowClosing(WindowEvent e)
  {  canvas.stopGame();  }

  public void windowClosed(WindowEvent e) {}
  public void windowOpened(WindowEvent e) {}

// -----------------------------------------

  public static void main(String[] args)
  { 
    int fps = DEFAULT_FPS;
    if (args.length != 0)
      fps = Integer.parseInt(args[0]);

    long period = (long) 1000.0/fps;
    System.out.println("fps: " + fps + "; period: " + period + " ms");

    new CubeGL(period*1000000L);    // ms --> nanosecs 
  } // end of main()


} // end of CubeGL class