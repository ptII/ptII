/* Ptolemy server which manages the servlet container and simulation requests.

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

package ptserver.control;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import org.eclipse.jetty.http.security.Constraint;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ptolemy.actor.Manager.State;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelListener;
import ptserver.communication.RemoteModel.RemoteModelType;
import ptserver.communication.RemoteModelResponse;
import ptserver.data.ServerEventToken;
import ptserver.data.ServerEventToken.EventType;
import ptserver.data.TokenParser;
import ptserver.data.TokenParser.HandlerData;
import ptserver.util.PtolemyModuleJavaSEInitializer;

///////////////////////////////////////////////////////////////////
//// PtolemyServer

/** The PtolemyServer class is responsible for fulfilling simulation
 *  requests.  To do this, the server hosts a servlet through the Jetty
 *  servlet container that enables clients to administer control commands
 *  for starting, pausing, resuming, and stopping simulations.  The server
 *  relies on an MQTT message broker for transmitting tokens between the
 *  client and server.
 *
 * <p>PtolemyServer uses mosquitto from <a href="http://mosquitto.org/download/#in_browser">http://mosquitto.org/download</a>.</p>
 *
 * <p>Under Windows, there is a mosquitto binary.</p>
 * <p>Under Mac OS X:</p>
 * <ol>
 * <li>Install cmake from <a href="http://www.cmake.org/cmake/resources/software.html#in_browser">http://www.cmake.org/cmake/resources/software.html</a> or from
 * <a href="http://mxcl.github.com/homebrew/#in_browser">http://mxcl.github.com/homebrew</a></li>
 * <li>Download and untar the mosquitto sources:
 * <pre>
 * cd $PTII/vendors
 * wget http://mosquitto.org/files/source/mosquitto-0.10.2.tar.gz
 * tar -zxf mosquitto-0.10.2.tar.gz
 * cd mosquitto-0.10.2
 * </pre>
 * </li>
 * <li> Run:
 * <pre>
 * cmake .
 * make
 * sudo make install
 * </pre>
 * </li>
 * </ol>
 *
 * @author Justin Killian
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
 */
public final class PtolemyServer implements IServerManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The ResourceBundle containing configuration parameters.
     */
    public static final ResourceBundle CONFIG = ResourceBundle
            .getBundle("ptserver.PtolemyServerConfig");

    /** The logger that will record Ptolemy errors to the log file.
     */
    public static final Logger LOGGER;

    /** The file to which the logger records system messages.
     */
    public static final String LOG_FILENAME = CONFIG.getString("LOG_FILENAME");

    /** The virtual path of the command servlet.
     */
    public static final String SERVLET_NAME = "PtolemyServer";

    /** Roles to classify users during basic authentication.
     */
    public static final String SERVLET_ROLE = "user";

    /** Export format of the model image.
     */
    public static final String IMAGE_FORMAT = "PNG";

    /** Buffer size to be used when exporting the model image.
     */
    public static final int IMAGE_BUFFER_SIZE = 50000;

    /** Initialize the logger used for error handling.
     */
    static {
        PtolemyModuleJavaSEInitializer.initializeInjector();
        Logger logger = null;
        FileHandler logFile = null;

        try {
            logger = Logger.getLogger(PtolemyServer.class.getSimpleName());
            logFile = new FileHandler(LOG_FILENAME, true);
            logFile.setFormatter(new XMLFormatter());
            logger.addHandler(logFile);
            logger.setLevel(Level.ALL);
        } catch (SecurityException e) {
            throw new ExceptionInInitializerError(e);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        LOGGER = logger;
    }

    ///////////////////////////////////////////////////////////////////
    ////                  constructor                              ////

    /** Create an instance of the Ptolemy server. This class is a singleton so
     *  only one instance should ever exist at a time.  An embedded servlet container
     *  is initialized for the servlet (synchronous command handler).
     *  @param servletPort The port on which the servlet operates.
     *  @param brokerPath The path to the broker executable.
     *  @param brokerAddress The host address of the MQTT broker.
     *  @param brokerPort The port of the broker.
     *  @param modelDirectory The root directory of where model files are stored.
     *  @exception IllegalActionException If the server was unable to load the default
     *  configuration from the resource file.
     */
    private PtolemyServer(int servletPort, String brokerPath,
            String brokerAddress, int brokerPort, String modelDirectory)
            throws IllegalActionException {

        try {
            // If not passed, attempt to pull from configuration.
            brokerPath = (brokerPath != null ? brokerPath : CONFIG
                    .getString("BROKER_PATH"));
            brokerAddress = (brokerAddress != null ? brokerAddress : (CONFIG
                    .getString("BROKER_ADDRESS") != null ? CONFIG
                    .getString("BROKER_ADDRESS") : InetAddress.getLocalHost()
                    .getHostAddress()));
            brokerPort = (brokerPort > 0 ? brokerPort : Integer.parseInt(CONFIG
                    .getString("BROKER_PORT")));
            servletPort = (servletPort > 0 ? servletPort : Integer
                    .parseInt(CONFIG.getString("SERVLET_PORT")));
            modelDirectory = (modelDirectory != null ? modelDirectory : CONFIG
                    .getString("MODELS_DIRECTORY"));

            // If path is specified, attempt to launch the broker process.
            if (_configureBroker(brokerPath, brokerPort)) {
                brokerAddress = InetAddress.getLocalHost().getHostAddress();
            }

            _brokerUrl = String
                    .format("tcp://%s@%s", brokerAddress, brokerPort);
            _servletUrl = String
                    .format("http://%s:%s/%s", InetAddress.getLocalHost()
                            .getHostAddress(), servletPort, SERVLET_NAME);
            _modelsDirectory = modelDirectory;
            _servletHost = new Server(servletPort);
            _servletHost.setHandler(_configureServlet());
            _servletHost.start();
            _executor = Executors.newCachedThreadPool();
            _requests = new ConcurrentHashMap<Ticket, SimulationTask>();
        } catch (Exception e) {
            _handleException("Unable to initialize Ptolemy server.", e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the singleton with non-default configuration values.
     *  @param servletPort The port on which the servlet operates.
     *  @param brokerPath The path to the broker executable.
     *  @param brokerAddress The host address of the MQTT broker.
     *  @param brokerPort The port of the broker.
     *  @param modelDirectory The root directory of where model files are stored.
     *  @exception IllegalActionException If the server could not be created.
     */
    public static synchronized void createInstance(int servletPort,
            String brokerPath, String brokerAddress, int brokerPort,
            String modelDirectory) throws IllegalActionException {
        _instance = new PtolemyServer(servletPort, brokerPath, brokerAddress,
                brokerPort, modelDirectory);
    }

    /** Shut down the simulation thread by calling the finish() method
     *  on its Manager and removing the task from the server.
     *  @param ticket  Ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to destroy the simulation thread.
     */
    public synchronized void close(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);
            stop(ticket);

            // FindBugs is wrong here since _checkTicket ensures that task is not null
            SimulationTask task = _requests.get(ticket);
            task.close();

            _requests.remove(ticket);
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Download the selected model to the client.
     *  @param url URL of the model file.
     *  @return Byte array containing the model data.
     *  @exception IllegalActionException If the server encountered an error opening the model file.
     */
    public byte[] downloadModel(String url) throws IllegalActionException {
        byte[] modelData = null;
        try {
            modelData = ptolemy.util.FileUtilities
                    .binaryReadURLToByteArray(new URL(url));
        } catch (Exception e) {
            _handleException("Unable to read the model URL: " + url, e);
        }

        return modelData;
    }

    /** Get the full URL to the message broker.
     *  @return The URL to the servlet including port.
     */
    public String getBrokerUrl() {
        return _brokerUrl;
    }

    /** Get the singleton instance of the Ptolemy server. If it does not already
     *  exist, the singleton will be instantiated using the default
     *  configuration.
     *  @return The PtolemyServer singleton.
     *  @exception IllegalActionException If the server could not be launched.
     */
    public static PtolemyServer getInstance() throws IllegalActionException {
        if (_instance == null) {
            synchronized (PtolemyServer.class) {
                if (_instance == null) {
                    createInstance(-1, null, null, -1, null);
                }
            }
        }

        return _instance;
    }

    /** Get a listing of the layouts for a specific model available on the
     *  server in either the database or the local file system.
     *  @param url Address of the model file for which layouts are found.
     *  @return An array of URLs for the layouts available for the model on the server.
     *  @exception IllegalActionException If there was a problem discovering available layouts.
     */
    public String[] getLayoutListing(final String url)
            throws IllegalActionException {
        FilenameFilter layoutFilter = new FilenameFilter() {
            public boolean accept(File file, String filename) {
                String modelName = url.substring(url.lastIndexOf("/") + 1,
                        url.lastIndexOf(".xml"));

                return ((filename.startsWith(modelName)) && (filename
                        .endsWith(".layout.xml")));
            }
        };

        ArrayList<String> urls = new ArrayList<String>();
        File modelDirectory = new File(_modelsDirectory);

        for (File filterResult : modelDirectory.listFiles(layoutFilter)) {
            if (!filterResult.isDirectory()) {
                try {
                    urls.add(filterResult.toURI().toURL().toExternalForm());
                } catch (Exception e) {
                    _handleException(
                            "Unable to construct the URL for layout file: "
                                    + filterResult.getName(), e);
                }
            }
        }

        String[] returnItems = new String[urls.size()];
        return urls.toArray(returnItems);
    }

    /** Get a listing of the models available on the server in either the
     *  database or the local file system.
     *  @return An array of URLs for the models available on the server.
     *  @exception IllegalActionException If there was a problem discovering available models.
     */
    public String[] getModelListing() throws IllegalActionException {

        FilenameFilter modelFilter = new FilenameFilter() {
            public boolean accept(File file, String filename) {
                return ((filename.endsWith(".xml")) && (!filename
                        .endsWith(".layout.xml")));
            }
        };

        ArrayList<String> urls = new ArrayList<String>();
        File modelDirectory = new File(_modelsDirectory);

        for (File filterResult : modelDirectory.listFiles(modelFilter)) {
            if (!filterResult.isDirectory()) {
                try {
                    urls.add(filterResult.toURI().toURL().toExternalForm());
                } catch (Exception e) {
                    _handleException(
                            "Unable to construct the URL for model file: "
                                    + filterResult.getName(), e);
                }
            }
        }

        String[] returnItems = new String[urls.size()];
        return urls.toArray(returnItems);
    }

    /** Get the full URL to the servlet application.
     *  @return The URL to the servlet including port.
     */
    public String getServletUrl() {
        return _servletUrl;
    }

    /** Get the simulation task of the provided ticket.
     *  @param ticket The ticket associated with the simulation task.
     *  @return the simulation task associated with the provided ticket.
     *  @exception IllegalActionException if the ticket is invalid.
     */
    public synchronized SimulationTask getSimulationTask(Ticket ticket)
            throws IllegalActionException {
        SimulationTask task = null;
        try {
            task = _requests.get(ticket);
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }

        return task;
    }

    /** Get the current state of a specific simulation based on the simulation manager's state.
     *  @param ticket The ticket reference to the simulation request.
     *  @return The state of the queried simulation.
     *  @exception IllegalActionException If the ticket is invalid or the state
     *  of the running situation could not be determined.
     */
    public synchronized State getStateOfSimulation(Ticket ticket)
            throws IllegalActionException {
        State state = null;
        try {
            _checkTicket(ticket);
            SimulationTask task = _requests.get(ticket);

            // FindBugs is wrong here since _checkTicket ensures that task is not null
            state = task.getManager().getState();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }

        return state;
    }

    /** Get the token handlers loaded on the server so that they can be
     *  set up on the client.
     *  @return The token handler map from the server.
     *  @exception IllegalActionException If the server was unable to get the handler map.
     */
    public synchronized LinkedHashMap<String, String> getTokenHandlerMap()
            throws IllegalActionException {
        LinkedHashMap<String, String> tokenHandlerMap = null;
        try {
            tokenHandlerMap = new LinkedHashMap<String, String>();
            for (HandlerData<?> data : TokenParser.getInstance()
                    .getHandlerList()) {
                tokenHandlerMap.put(data.getTokenType().getName(), data
                        .getTokenHandler().getClass().getName());
            }
        } catch (Exception e) {
            _handleException(
                    "Problem sending token handler map: " + e.getMessage(), e);
        }

        return tokenHandlerMap;
    }

    /** Initialize the Ptolemy server, set up the servlet host, and wait for simulation requests.
     *  The following optional command line switches may be used with their accompanying value:
     *  -servlet_port, -broker_path (if launching local broker), -broker_address, -broker_port,
     *  and -model_dir. The port numbers must be integers, the broker path must be the path
     *  to the MQTT broker executable, and the broker address must be the host address.
     *
     *  For example: java -classpath ptserver.PtolemyServer -broker_address
     *  192.168.125.169 -broker_port 1883
     *
     *  @param args Optional command line arguments.
     *  @exception IllegalActionException If the server could not be launched.
     */
    public static void main(String[] args) throws IllegalActionException {
        try {
            int servletPort = -1;
            String brokerPath = null;
            String brokerAddress = null;
            int brokerPort = -1;
            String modelDirectory = null;

            // Set all provided configuration parameters.
            for (int i = 0; i < args.length; i++) {
                if ((args[i].startsWith("-")) && (i + 1 < args.length)) {
                    if (args[i].toLowerCase().equals("-servlet_port")) {
                        servletPort = Integer.parseInt(args[i + 1]);
                    } else if (args[i].toLowerCase().equals("-broker_path")) {
                        brokerPath = args[i + 1];
                    } else if (args[i].toLowerCase().equals("-broker_address")) {
                        brokerAddress = args[i + 1];
                    } else if (args[i].toLowerCase().equals("-broker_port")) {
                        brokerPort = Integer.parseInt(args[i + 1]);
                    } else if (args[i].toLowerCase().equals("-model_dir")) {
                        modelDirectory = args[i + 1];
                    }
                }
            }

            // Create the singleton.
            createInstance(servletPort, brokerPath, brokerAddress, brokerPort,
                    modelDirectory);
        } catch (NumberFormatException e) {
            String message = "Port must be a numeric value.  The default value will be used.";
            PtolemyServer.LOGGER.log(Level.SEVERE, message, e);
            throw new IllegalActionException(null, e, message);
        } catch (Exception e) {
            String message = "Failed to launch Ptolemy server.";
            PtolemyServer.LOGGER.log(Level.SEVERE, message, e);
            throw new IllegalActionException(null, e, message);
        }
    }

    /** Get the number of simulation on the server.  The current thread pool implementation
     *  allows for an infinite number of concurrent simulations rather than limiting the server to
     *  finite amount.
     *  @return The number of active simulations as well as the queued requests that have not
     *  yet been fulfilled.
     */
    public synchronized int numberOfSimulations() {
        if (_requests == null) {
            return 0;
        }

        return _requests.size();
    }

    /** Open a model with the provided model URL and wait for the user to request
     *  the execution of the simulation.
     *  @param modelUrl The path to the model file
     *  @param layoutUrl The path to a model's layout file
     *  @exception IllegalActionException  If the model fails to load from the provided URL.
     *  @return The user's reference to the simulation task
     */
    public synchronized RemoteModelResponse open(String modelUrl,
            String layoutUrl) throws IllegalActionException {
        RemoteModelResponse response = null;
        Ticket ticket = null;

        try {
            // Generate a unique ticket for the request.
            ticket = Ticket.generateTicket(modelUrl, layoutUrl);
            while (_requests.contains(ticket)) {
                ticket = Ticket.generateTicket(modelUrl, layoutUrl);
            }

            // Save the simulation request.
            RemoteModel clientModel = new RemoteModel(RemoteModelType.CLIENT);
            SimulationTask simulationTask = new SimulationTask(ticket);
            simulationTask.getRemoteModel().addRemoteModelListener(
                    _remoteModelListener);

            String modelXML = new String(downloadModel(ticket.getLayoutUrl()));
            HashMap<String, String> resolvedTypes = simulationTask
                    .getRemoteModel().getResolvedTypes();

            clientModel.initModel(modelXML, resolvedTypes);
            simulationTask.getRemoteModel().createRemoteAttributes(
                    clientModel.getSettableAttributesMap().keySet());
            simulationTask.getRemoteModel().setUpInfrastructure(ticket,
                    _brokerUrl);

            // Populate the response.
            response = new RemoteModelResponse();
            response.setTicket(ticket);

            new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);

            //response.setModelImage(export.toByteArray());
            response.setModelTypes(resolvedTypes);
            response.setModelXML(modelXML);
            response.setBrokerUrl(_brokerUrl);

            _requests.put(ticket, simulationTask);
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
        return response;
    }

    /** Pause the execution of the simulation by calling the pause() method
     *  on its Manager.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to pause the running simulation.
     */
    public synchronized void pause(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);

            // FindBugs is wrong here since _checkTicket ensures that task is not null
            _requests.get(ticket).getManager().pause();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Resume the execution of the simulation by calling the resume() method
     *  on its Manager.
     *  @param ticket  The ticket reference to the simulation request.
     *  @exception IllegalActionException  If the server was unable to resume the execution of the
     *  simulation.
     */
    public synchronized void resume(Ticket ticket)
            throws IllegalActionException {
        try {
            _checkTicket(ticket);

            // FindBugs is wrong here since _checkTicket ensures that task is not null
            _requests.get(ticket).getManager().resume();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Shut down the broker process and stop all active simulation
     *  threads by calling their Managers.
     *  @exception IllegalActionException If the servlet, broker, or thread pool cannot be stopped.
     */
    public synchronized void shutdown() throws IllegalActionException {
        for (SimulationTask task : _requests.values()) {
            try {
                // Notify the client that the server is shutting down.
                task.getRemoteModel()
                        .getTokenPublisher()
                        .sendToken(
                                new ServerEventToken(EventType.SERVER_SHUTDOWN,
                                        "The Ptolemy server you are currently connected is shutting down."));

                // Shut down the locally running simulation.
                task.getRemoteModel().getManager().finish();
            } catch (Throwable error) {
                PtolemyServer.LOGGER.log(Level.SEVERE,
                        "Failed to close the simulation"
                                + task.getRemoteModel().getTicket(), error);
            }
        }

        // If running launching broker locally, shut it down.
        if (_broker != null) {
            try {
                _broker.destroy();
                _broker = null;
            } catch (Exception e) {
                _handleException("The broker could not be stopped.", e);
            }
        }

        // Shut down the servlet container.
        try {
            _servletHost.stop();
            _servletHost.destroy();
            _servletHost = null;
        } catch (Exception e) {
            _handleException("The servlet could not be stopped.", e);
        }

        // Shut down the thread pool that's hosting simulations.
        try {
            _executor.shutdown();
        } catch (Exception e) {
            _handleException("The thread pool could not be shutdown.", e);
        }

        // FindBugs is wrong here, it's OK to set the instance to null since the server is shutting down.
        _requests.clear();
        _instance = null;
    }

    /** Start the execution of the simulation by utilizing a
     *  free thread within the pool.
     *  @param ticket  The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to start the simulation.
     */
    public synchronized void start(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);
            _executor.execute(_requests.get(ticket));
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Stop the execution of the simulation by calling the finish() method
     *  on its Manager.
     *  @param ticket  The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to stop the simulation.
     */
    public synchronized void stop(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);

            // FindBugs is wrong here since _checkTicket ensures that task is not null
            _requests.get(ticket).getManager().finish();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check to ensure that the provided ticket is valid and refers to
     *  a current simulation request on the server.
     *  @param ticket The ticket to be validated.
     *  @exception IllegalActionException If the ticket is null or does not
     *  reference a valid simulation request on the server.
     */
    private void _checkTicket(Ticket ticket) throws IllegalActionException {
        if (ticket == null) {
            throw new IllegalActionException("The ticket was null.");
            // TODO: create InvalidTicketException
        }
        if (_requests.get(ticket) == null) {
            throw new IllegalActionException(
                    "The ticket does not reference a simulation: "
                            + ticket.getTicketID());
            // TODO: create InvalidTicketException
        }
    }

    /** Configure and launch the broker if path has been specified.
     *  @param brokerPath The path to the broker.
     *  @param brokerPort The port of the broker.
     *  @return True if the broker was started locally, false otherwise.
     *  @exception IllegalActionException If the broker could not be started.
     */
    private boolean _configureBroker(String brokerPath, int brokerPort)
            throws IllegalActionException {

        if ((brokerPath != null) && (brokerPath.length() > 0)) {
            try {
                _broker = new ProcessBuilder(new String[] { brokerPath, "-p",
                        String.valueOf(brokerPort) }).redirectErrorStream(true)
                        .start();

                return true;
            } catch (IOException e) {
                _handleException(
                        "Unable to spawn MQTT broker process at '" + brokerPath
                                + "' on port " + String.valueOf(brokerPort)
                                + ".", e);
            }
        }

        return false;
    }

    /** Configure the context handler of the servlet.
     *  @return The servlet context handler.
     *  @exception MalformedURLException If the servlet address is invalid.
     */
    private ServletContextHandler _configureServlet()
            throws MalformedURLException {
        // Set up the servlet context and security settings.
        // Optional: use com.eclipse.Util.Password for hashing

        // Define the constraints for this servlet.
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setConstraint(new Constraint(Constraint.__BASIC_AUTH,
                SERVLET_ROLE));
        mapping.setPathSpec("/" + SERVLET_NAME);
        mapping.getConstraint().setAuthenticate(true);

        // Define the security context.
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        security.setLoginService(new HashLoginService("PtolemyServer",
                getClass().getResource(
                        "/ptserver/PtolemyServerUsers.properties")
                        .toExternalForm()));
        security.setConstraintMappings(new ConstraintMapping[] { mapping });

        // Assign the servlet container context.
        ServletContextHandler context = new ServletContextHandler(_servletHost,
                "/", ServletContextHandler.SESSIONS);
        context.setSecurityHandler(security);
        context.addServlet(new ServletHolder(ServerManager.class), "/"
                + SERVLET_NAME);

        return context;
    }

    /** Log the message and exception into the Ptolemy server log.
     *  @param message  Descriptive message about what caused the error.
     *  @param error The exception that was raised.
     *  @exception IllegalActionException Always throws the exception in order to propogate
     *  the error through the Ptolemy hierarchy.
     */
    private void _handleException(String message, Exception error)
            throws IllegalActionException {
        PtolemyServer.LOGGER.log(Level.SEVERE, message, error);
        throw new IllegalActionException(null, error, message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private final RemoteModelListener _remoteModelListener = new RemoteModelListener() {

        public void modelConnectionExpired(RemoteModel remoteModel) {
            System.out.println("Removing model " + remoteModel.getTicket());
            System.out.println("Last pong was "
                    + (System.currentTimeMillis() - remoteModel
                            .getLastPongToken().getTimestamp()) + " ms ago");
            try {
                close(remoteModel.getTicket());
            } catch (IllegalActionException e) {
                // TODO handle exception, note this exception comes from worker thread.
                e.printStackTrace();
            }
        }

        public void modelException(RemoteModel remoteModel, String message,
                Throwable exception) {
            exception.printStackTrace();
        }

        public void modelEvent(RemoteModel remoteModel, String message,
                EventType type) {
            System.out.println(type);
        }
    };

    /** The broker process should the user choose to launch locally.
     */
    private Process _broker;

    /** The full URL of the MQTT message broker.
     */
    private String _brokerUrl;

    /** The service that manages the simulation thread pool.
     */
    private ExecutorService _executor;

    /** The Ptolemy server singleton.
     */
    private static PtolemyServer _instance;

    /** The server's internal reference to the list of simulation requests.
     */
    private ConcurrentHashMap<Ticket, SimulationTask> _requests;

    /** The local directory in which model files are stored.
     */
    private String _modelsDirectory;

    /** The embedded Jetty servlet container that hosts the command servlet.
     */
    private Server _servletHost;

    /** The URL to the servlet application.
     */
    private String _servletUrl;
}
