package lbnl.lib.labview;

import java.io.IOException;

import lbnl.actor.lib.Simulator;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Actor that works with a LabVIEW simulation program. To use this
 * actor, the LabVIEW program must follow a fixed semantics, which is
 * specified later in this documentation.
 * <p>
 * This actor communicates the LabVIEW simulation program, as well
 * as synchronizes simulated physical time between these two platforms.
 * This actor assumes the Ptolemy program dictates what time to advance
 * to, while the LabVIEW program proposes times to advance to.
 * </p><p>
 * This actor can be invoked in two cases, one, when an input is received
 * from the Ptolemy II simulation; two, when this actor triggers itself to
 * fire at some future time through the use of pure events. In either case,
 * outputs may or may not be produced by this actor. However, since the
 * timeline this actor deals with is the simulated physical time,
 * the outputs produced should not be a response of consumed input at
 * the same time. The LabVIEW program should ensure this behavior.
 * </p><p>
 * At initialization, this actor reads input from the LabVIEW program,
 * which proposes a simulated physical time (hereby referred to as "time",
 * unless otherwise stated) to advance to. This actor then produces a
 * pure event (by calling fireAt() of the director, with this time as 
 * its timestamp. Notice this time may or may not be different from 
 * the current time of the Ptolemy simulation environment.
 * </p><p>
 * The director will invoke this actor either when a trigger event
 * arrives at this actor's input port, or when the pure event produced
 * earlier triggers this event. In the first case, the input is consumed,
 * and this data is transmitted into the LabVIEW program. The LabVIEW
 * program should then react to this input and propose the next time to
 * advance to, and send it back to the Ptolemy actor. Also, if a previous
 * input has decided to produce an output at the current time, then an output
 * will be produced by the LabVIEW program at the current time, and that
 * output will be produced by this actor. 
 * </p><p>
 * The key assumption we make about the LabVIEW program is that it always
 * has information about what is the next time it wants to advance to.
 * Thus at any point in time when the LabVIEW program is invoked, it
 * proposes a new time to advance to, and send that time to the Ptolemy
 * program.
 *
 * @author Jia Zou
 * @version $Id: Simulator.java 55766 2009-09-25 14:22:32Z mwetter $
 * @since BCVTB 0.1
 *
 */
public class LabVIEWSimulator extends Simulator {
    /** Constructs an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LabVIEWSimulator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }
    
    /** Start the simulation program. Currently we do this manually, but
     *  there should be a way to run a labview model through command line.
     *
     *  @exception IllegalActionException If the simulation process arguments
     *                           are invalid.
     */
   protected void _startSimulation() throws IllegalActionException {
   }

   /** We do not output any init token at startup.
    */
   protected void _outputInitToken() throws IllegalActionException {
       Token token = new DoubleToken(0.0);
       output.send(0, token);
   }

   /** Send the input token to the client program and send the
    *  output from the client program to the output port. However
    *  if the received proposed time to advance to from LabVIEW
    *  is less than 0, it is interpreted as an indication to
    *  terminate the program. In which case this actor does not 
    *  send an event to the output port. Nor do we call fireAt().
    *  Note the other way to stop execution is to set the stop time
    *  of the DE actor to a finite value.
    *
    *  @exception IllegalActionException If the simulation time 
    *  between Ptolemy and the client program is not synchronized.
    */
   public void fire() throws IllegalActionException {

       // tokTim is the current time of in the Ptolemy world. This time
       // is to be sent to the LabVIEW program to ensure time advances
       // to the same value.
       tokTim = getDirector().getModelTime().getDoubleValue();
       //           if (!firstFire && server.getClientFlag() == 0) {
       if (server.getClientFlag() == 0) {
           // If clientflag is non-zero, do not read anymore

           // this method write the tokTim to the LabVIEW program.
           // Also, if data is present at the input of this actor,
           // that data is sent to the LabVIEW program. If no data
           // is present, then no data is sent to the LabVIEW
           // program.
           _writeToServer();

           // Reading data from the LabVIEW program, which gets
           // the next proposed time to advance to, as well as
           // (possibly) data from the LabVIEW program.
           // After reading, we call fireAt() to ensure this actor
           // fires again at the proposed future time. Also, if data is read
           // from the LabVIEW program, that data is sent to the
           // output port of this actor.
           _readFromServer();
           double[] dblRea = server.getDoubleArray();
           double nextSimulationTime = server.getSimulationTimeReadFromClient();
           // If nextSimulationTime is negative, this implies the program
           // should be stopped. Thus we simply return from fire().
           if (nextSimulationTime < 0) {
               return;
           }
           getDirector().fireAt(this, nextSimulationTime);
           if (dblRea.length == 1) {
               output.send(0, new DoubleToken(dblRea[0]));
           } else if (dblRea.length != 0) {
               throw new IllegalActionException(this, "Received data from " +
               "LabVIEW, the only supported data lenght right now is 1.");
           }
       } else { // Either client is down or this is the first time step. Consume token
           input.get(0);
           firstFire = false;
       }
       //////////////////////////////////////////////////////
       // send output token
//       output.send(0, outTok);
   }
   
   /** Write the data to the server instance, which will send it to
    * the client program.
    *
    * @exception IllegalActionException If there was an error when
    * writing to the server.
    */
   protected void _writeToServer() throws IllegalActionException {
       //////////////////////////////////////////////////////
       // Write data to server
       Token token = null;
       if (input.hasToken(0)) {
           token = input.get(0);
       }
       dblWri = _getDoubleArray(token);

       try {
           //                          Thread.sleep(1000); // in milliseconds
           server.write(0, tokTim, dblWri);
       } catch (IOException e) {
           String em = "Error while writing to client: " + LS + e.getMessage();
           throw new IllegalActionException(this, em);
       }
       // get tokens' time stamp. This time will be written to the 
       // client in the next time step, this time step read from the client
       // the output which will be sent to clients in the next time step
       // as inputs
       tokTim = getDirector().getModelTime().getDoubleValue();
       System.out.println("the current time is " + tokTim);
   }
   
   /** Return true. Overwrites the prefire method in SDFTransformer.
    *  This actor extends Simulator, which unfortunately extends
    *  SDFTransformer.
    *  @return True if this actor is ready for firing, false otherwise.
    *  @exception IllegalActionException Not thrown in this base class.
    */
   public boolean prefire() throws IllegalActionException {
       if (_debugging) {
           _debug("Called prefire()");
       }

       return true;
   }

   
   /** Get a double array from the Token.
   *
   * @param t the token which must be a type that can be converted to an ArrayToken
   * @return the double[] array with the elements of the Token
   * @exception IllegalActionException If the base class throws it.
   */
  protected double[] _getDoubleArray(ptolemy.data.Token t)
          throws IllegalActionException {
      
      double[] result;
      if (t == null) {
          result = new double[0];
      } else {
          result = new double[1];
          if (t instanceof DoubleToken) {
              result[0] = ((DoubleToken)t).doubleValue();
          } else {
              throw new IllegalActionException(this, "Data received at the " +
              		"input of this actor must be of type double");
          }
      }
      return result;
  }
}
