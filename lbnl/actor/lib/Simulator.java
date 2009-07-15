// Actor that calls a simulation program of a dynamic system that is coupled to Ptolemy II

/*
********************************************************************
Copyright Notice
----------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy). All rights reserved.

If you have questions about your rights to use or distribute this
software, please contact Berkeley Lab's Technology Transfer Department
at TTD@lbl.gov

NOTICE.  This software was developed under partial funding from the U.S.
Department of Energy.  As such, the U.S. Government has been granted for
itself and others acting on its behalf a paid-up, nonexclusive,
irrevocable, worldwide license in the Software to reproduce, prepare
derivative works, and perform publicly and display publicly.  Beginning
five (5) years after the date permission to assert copyright is obtained
from the U.S. Department of Energy, and subject to any subsequent five
(5) year renewals, the U.S. Government is granted for itself and others
acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide
license in the Software to reproduce, prepare derivative works,
distribute copies to the public, perform publicly and display publicly,
and to permit others to do so.


Modified BSD License agreement
------------------------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in
      the documentation and/or other materials provided with the
      distribution.
   3. Neither the name of the University of California, Lawrence
      Berkeley National Laboratory, U.S. Dept. of Energy nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes,
patches, or upgrades to the features, functionality or performance of
the source code ("Enhancements") to anyone; however, if you choose to
make your Enhancements available either publicly, or directly to
Lawrence Berkeley National Laboratory, without imposing a separate
written license agreement for such Enhancements, then you hereby grant
the following license: a non-exclusive, royalty-free perpetual license
to install, use, modify, prepare derivative works, incorporate into
other computer software, distribute, and sublicense such enhancements or
derivative works thereof, in binary and source code form.

********************************************************************
*/

package lbnl.actor.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import lbnl.actor.lib.net.Server;
import lbnl.util.ClientProcess;
import lbnl.util.WarningWindow;
import lbnl.util.XMLWriter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * Actor that calls a simulation program of a dynamic system 
 * that is coupled to Ptolemy II. At the start of the simulation,
 * this actor fires a system command that is defined by the parameter
 * <code>programName</code> with arguments <code>programArguments</code>.
 * It then initiates a socket connection and uses the socket to
 * exchange data with the external simulation program each time
 * the actor is fired.
 *
 * @author Michael Wetter
 * @version $Id$
 * @since BCVTB 0.1
 *
 */
public class Simulator extends SDFTransformer {

    /** Constructs an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Simulator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        input.setMultiport(false);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);
        output.setMultiport(false);

        programName = new FileParameter(this, "programName");
        new Parameter(programName, "allowFiles", BooleanToken.TRUE);
        new Parameter(programName, "allowDirectories", BooleanToken.FALSE);

        programArguments = new Parameter(this, "programArguments");
        programArguments.setTypeEquals(BaseType.STRING);
        programArguments.setExpression("");

        workingDirectory = new FileParameter(this, "workingDirectory");
        new Parameter(workingDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(workingDirectory, "allowDirectories", BooleanToken.TRUE);
        workingDirectory.setExpression(".");

        simulationLogFile = new FileParameter(this, "simulationLogFile");
        simulationLogFile.setTypeEquals(BaseType.STRING);
        simulationLogFile.setExpression("simulation.log");
        new Parameter(simulationLogFile, "allowFiles", BooleanToken.TRUE);
        new Parameter(simulationLogFile, "allowDirectories", BooleanToken.FALSE);

        socketTimeout = new Parameter(this, "socketTimeout");
        socketTimeout.setDisplayName("socketTimeout [milliseconds]");
        socketTimeout.setExpression("5000");
        socketTimeout.setTypeEquals(BaseType.INT);

        // expert settings
        socketPortNumber = new Parameter(this, "socketPortNumber");
        socketPortNumber.setDisplayName("socketPortNumber (used if non-negative)");
        socketPortNumber.setExpression("-1");
        socketPortNumber.setTypeEquals(BaseType.INT);
        socketPortNumber.setVisibility(Settable.EXPERT);

        socketConfigurationFile = new FileParameter(this, "socketConfigurationFile");
        socketConfigurationFile.setTypeEquals(BaseType.STRING);
        socketConfigurationFile.setExpression("socket.cfg");
        new Parameter(socketConfigurationFile, "allowFiles", BooleanToken.TRUE);
        new Parameter(socketConfigurationFile, "allowDirectories", BooleanToken.FALSE);
        socketConfigurationFile.setVisibility(Settable.EXPERT);

        // we produce one (DOUBLE_MATRIX) token as the initial output
        output_tokenInitProduction.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>init</code> and <code>step</code>
     *  public members to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Simulator newObject = (Simulator) super.clone(workspace);

        newObject.programArguments = (Parameter) newObject
                .getAttribute("programArguments");
        newObject.programName = (FileParameter) newObject
                .getAttribute("programName");
        newObject.socketPortNumber = (Parameter) newObject
                .getAttribute("socketPortNumber");
        newObject.simulationLogFile = (FileParameter) newObject
                .getAttribute("simulationLogFile");
        newObject.socketConfigurationFile = (FileParameter) newObject
                .getAttribute("socketConfigurationFile");
        newObject.socketTimeout = (Parameter) newObject
                .getAttribute("socketTimeout");
        newObject.workingDirectory = (FileParameter) newObject
                .getAttribute("workingDirectory");

        return newObject;
    }

    /** Send the input token to the client program and send the
     * output from the client program to the output port.
     *
     *  @exception IllegalActionException If the simulation time between Ptolemy 
     *    and the client program is not synchronized.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        // Check the input port for a token.
        if (input.hasToken(0)) {
            if (!firstFire && server.getClientFlag() == 0) {
                // If clientflag is non-zero, do not read anymore
                _writeToServer();
                // before the read happens, the client program will advance one time step
                _readFromServer();
                if (server.getClientFlag() == 0) {
                    // make sure that time is synchronized.
                    final double simTimRea = server
                            .getSimulationTimeReadFromClient();
                    final double simTim = getDirector().getModelTime()
                            .getDoubleValue();

                    if (Math.abs(simTim - simTimRea) > 0.0001) {
                        final String em = "Simulation time of "
                                + this.getFullName() + " is not synchronized."
                                + LS + "Time in Ptolemy = " + simTim + LS
                                + "Time in client  = " + simTimRea;
                        throw new IllegalActionException(em);
                    }

                    double[] dblRea = server.getDoubleArray();
                    outTok = new DoubleMatrixToken(dblRea, dblRea.length, 1);
                }
            } else { // Either client is down or this is the first time step. Consume token
                input.get(0);
                firstFire = false;
            }
        }
        //////////////////////////////////////////////////////
        // send output token
        output.send(0, outTok);
    }

    /** Write the data to the server instance, which will send it to the client program.
     *
     * @exception IllegalActionException If there was an error when writing to the server.
     */
    private void _writeToServer() throws IllegalActionException {
        //////////////////////////////////////////////////////
        // Write data to server
        dblWri = _getDoubleArray(input.get(0));
        try {
            //	    	   		Thread.sleep(1000); // in milliseconds
            server.write(0, tokTim, dblWri);
        } catch (java.io.IOException e) {
            String em = "Error while writing to server: " + LS + e.getMessage();
            throw new IllegalActionException(this, em);
        }
        // get tokens' time stamp. This time will be written to the 
        // client in the next time step, this time step read from the client
        // the output which will be sent to clients in the next time step
        // as inputs
        tokTim = getDirector().getModelTime().getDoubleValue();
    }

    /** Read the data from the server instance, which will read it from the client program.
     *
     * @exception IllegalActionException If there was an error when reading from the server.
     */
    private void _readFromServer() throws IllegalActionException {
        //////////////////////////////////////////////////////
        // Read data from server
        try {
            //		Thread.sleep(100); // in milliseconds
            server.read();
            int fla = server.getClientFlag();
            if (fla < 0) {
                final String em = "Error: Client " + this.getFullName()
                        + " terminated communication by sending flag = " + fla
                        + " at time "
                        + getDirector().getModelTime().getDoubleValue() + ".";
                throw new IllegalActionException(em);
            }
            if (fla > 0) {
                final String msg = "Warning: "
                        + this.getFullName()
                        + " terminated communication by sending flag = "
                        + fla
                        + " at time "
                        + getDirector().getModelTime().getDoubleValue()
                        + "."
                        + LS
                        + "Simulation will continue withouth updated values from client program.";
                // Start a new thread for the warning window so that the simulation can continue.
                new Thread(new WarningWindow(msg)).start();
                System.err.println("*** " + msg);
            }
        } catch (java.net.SocketTimeoutException e) {
            String em = "SocketTimeoutException while reading from server in "
                    + this.getFullName()
                    + ": "
                    + LS
                    + e.getMessage()
                    + "."
                    + LS
                    + "Try to increase the value of the parameter 'socketTimeout'";
            try {
                server.close();
            } catch (java.io.IOException e2) {
            }
            ; // do nothing here
            throw new IllegalActionException(em);
        } catch (java.io.IOException e) {
            String em = "IOException while reading from server: " + LS
                    + e.getMessage();
            try {
                server.close();
            } catch (java.io.IOException e2) {
            }
            ; // do nothing here
            throw new IllegalActionException(em);
        }
    }

    /** Initializes the data members and checks if the parameters of the actor are valid.
     *
     * @exception IllegalActionException If the parameters of the actor are invalid, or 
     *     if the file with the socket information cannot be written to disk.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Working directory
        String worDir = cutQuotationMarks(workingDirectory.getExpression());
        // If directory is not set, set it to current directory.
        if (worDir.length() == 0) {
            worDir = ".";
        }
        // Verify that directory exist
        if (!new File(worDir).isDirectory()) {
            String em = "Error: Working directory does not exist." + LS
                    + "Working directory is set to: '" + worDir + "'" + LS
                    + "Check configuration of '" + this.getFullName() + "'.";
            throw new IllegalActionException(em);
        }

        // Command that starts the simulation
        final String simCon = socketConfigurationFile.stringValue();
        // Assign BSD port number
        porNo = Integer.valueOf(socketPortNumber.getExpression());
        //////////////////////////////////////////////////////////////	
        // Instantiate server for IPC
        try {
            // time out in milliseconds
            final int timOutMilSec = Integer.valueOf(socketTimeout.getExpression());
            if (timOutMilSec <= 0) {
                final String em = "Parameter for socket time out must be positive."
                        + LS + "Received " + timOutMilSec + " milliseconds";
                throw new IllegalActionException(em);
            }
            if (porNo < 0) {
                server = new Server(timOutMilSec); // server uses any free port number
            } else {
                server = new Server(porNo, timOutMilSec);
            }
            // get port number
            porNo = server.getLocalPort();
        } catch (java.io.IOException e) {
            // try to close server unless it is still a null pointer
            if (server != null) {
                try {
                    server.close();
                } catch (java.io.IOException e2) {
                }
            }
            // throw original exception
            throw new IllegalActionException(e.getMessage());
        }
        ////////////////////////////////////////////////////////////// 
        // Write xml file for client
        XMLWriter xmlWri = new XMLWriter(worDir, simCon, porNo);
        try {
            xmlWri.write();
        }
        //catch(InterruptedException e){}
        catch (java.io.FileNotFoundException e) {
            String em = "FileNotFoundException when trying to write '"
                    + new File(worDir, simCon).getAbsolutePath() + "'.";
            throw new IllegalActionException(em);
        } catch (java.io.IOException e) {
            throw new IllegalActionException(e.toString());
        }
    }

    /** Start the simulation program.
     *
     *  @exception IllegalActionException If the simulation process arguments
     *                           are invalid.
     */
    private void _startSimulation() throws IllegalActionException {
        //////////////////////////////////////////////////////////////	
        // If porNo > 0, write client configuration file.
        // Else we assume there is already such a file provided by the user
        // Working directory
        final String worDir = cutQuotationMarks(workingDirectory
                .getExpression());
        //////////////////////////////////////////////////////////////	
        // start the simulation process
        // Get the command as a File in case it has $CLASSPATH in it.
        File commandFile = programName.asFile();
        final String comArg;
        if (commandFile.exists()) {
            // Maybe the user specified $CLASSPATH/lbnl/demo/CRoom/client
            comArg = commandFile.toString();
        } else {
            // Maybe the user specfied "matlab"
            comArg = programName.getExpression();
        }

        final String argLin = cutQuotationMarks(programArguments.getExpression());
        List<String> com = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(comArg);
        while (st.hasMoreTokens()) {
            com.add(st.nextToken());
        }
        st = new StringTokenizer(argLin);
        while (st.hasMoreTokens()) {
            com.add(st.nextToken());
        }
        cliPro = new ClientProcess();
        cliPro.setProcessArguments(com, worDir);
        File slf = simulationLogFile.asFile();
        try {
            if (slf.exists()) {
                if (!slf.delete()) {
                    throw new Exception("Cannot delete file.");
                }
            }
            slf.createNewFile(); // make sure we can write new file
            if (!slf.canWrite()) {
                throw new Exception("Cannot write to file.");
            }
        } catch (Exception e) {
            String em = "Error: Cannot write to simulation log file." + LS
                    + "Simulation log file is '" + slf.getAbsolutePath() + "'"
                    + LS + "Check configuration of '" + this.getFullName()
                    + "'.";
            throw new IllegalActionException(em);
        }
        cliPro.setSimulationLogFile(slf);
        cliPro.run();
        if (!cliPro.processStarted()) {
            String em = "Error: Simulation process did not start." + LS
                    + cliPro.getErrorMessage() + LS
                    + "Check configuration of '" + this.getFullName() + "'.";
            throw new IllegalActionException(em);
        }
    }

    /** Initialize state variables.
     *
     *  @exception IllegalActionException If the parent class throws it or
     *                              if the server socket cannot be opened
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        tokTim = getDirector().getModelTime().getDoubleValue();
        firstFire = true;

        _startSimulation();
        //////////////////////////////////////////////////////////////	
        // New code since 2008-01-05
        // Send initial output token. See also domains/sdf/lib/SampleDelay.java
        _readFromServer();
        double[] dblRea = server.getDoubleArray();
        outTok = new DoubleMatrixToken(dblRea, dblRea.length, 1);
        output.send(0, outTok);
    }

    /** Closes sockets and shuts down the simulator.
     * 
     *  @exception IllegalActionException if the base class throws it or
     *        if an I/O error occurs when closing the socket.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        try {
            // Send signal to the client, indicating that we are done with the time stepping.
            // This allows the client to terminate gracefully.
            server.write(1, tokTim, dblWri);
            // Close the server.
            server.close();
        } catch (java.io.IOException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    /** Cut the leading and terminating quotation marks if present.
     *
     *@param str The string.
     *@return The string with leading and terminating quotation marks removed if present
     */
    public static String cutQuotationMarks(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        } else {
            return str;
        }
    }

    /** Get a double array from the Token.
     *
     * @param t the token which must be a type that can be converted to an ArrayToken
     * @return the double[] array with the elements of the Token
     * @exception IllegalActionException If the base class throws it.
     */
    private double[] _getDoubleArray(ptolemy.data.Token t)
            throws IllegalActionException {
        final DoubleMatrixToken arrTok = (DoubleMatrixToken) t;
        final int n = arrTok.getRowCount();
        double[] ret = new double[n];
        for (int i = 0; i < n; i++) {
            final DoubleToken scaTok = (DoubleToken) arrTok.getElementAsToken(
                    i, 0);
            ret[i] = scaTok.doubleValue();
            if (Double.isNaN(ret[i])) {
                final String em = "Actor " + this.getFullName() + ": " + LS
                        + "Token number " + i + " is NaN at time "
                        + getDirector().getModelTime().getDoubleValue();
                throw new IllegalActionException(em);
            }
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                   ////

    // FIXME: Ports and Parameters are usually public and the
    // names of the variable should match the assigned name, otherwise
    // there are problems with cloning.

    /** Arguments of program that starts the simulation */
    protected Parameter programArguments;

    /** Name of program that starts the simulation */
    protected FileParameter programName;

    /** Double values that were written to the socket */
    protected double[] dblWri;

    /** Thread that runs the simulation */
    protected ClientProcess cliPro;

    /** Port number for BSD socket (used if non-negative) */
    protected Parameter socketPortNumber;

    /** Port number that is actually used for BSD socket */
    protected int porNo;

    /** Server used for data exchange */
    protected Server server;

    /** File name to which this actor writes the simulation log */
    protected FileParameter simulationLogFile;

    /** Process that runs the simulation */
    protected Process simProJav;

    /** File name to which this actor writes the socket configuration */
    protected FileParameter socketConfigurationFile;

    /** Socket time out in milliseconds */
    protected Parameter socketTimeout;

    /** Working directory of the simulation */
    protected FileParameter workingDirectory;

    /** Output tokens */
    protected DoubleMatrixToken outTok;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    /** Flag that is true during the first firing of this actor */
    private boolean firstFire;

    /** System dependent line separator */
    private final static String LS = System.getProperty("line.separator");

    /** Time of token that will be written to the client.

        This is equal to the Ptolemy time minus one time step, because at time t_k,
        a client gets the output of other clients at t_{k-1}, which allows the client to
        compute the states and outputs at t_k
    */
    private double tokTim;
}
