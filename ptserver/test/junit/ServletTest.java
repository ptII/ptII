/* Test cases for the Hessian servlets.

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

package ptserver.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ptolemy.actor.Manager;
import ptserver.communication.RemoteModelResponse;
import ptserver.control.IServerManager;
import ptserver.control.PtolemyServer;
import ptserver.control.Ticket;
import ptserver.util.PtolemyModuleJavaSEInitializer;

import com.caucho.hessian.client.HessianProxyFactory;

///////////////////////////////////////////////////////////////////
//// ServletTest

/** Test the ability to open, start, stop, and close a simulation request
 *  on the Ptolemy server using the Hessian protocol.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class ServletTest {
    static {
        PtolemyModuleJavaSEInitializer.initializeInjector();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Access the ResourceBundle containing configuration parameters.
     */
    public static final ResourceBundle CONFIG = PtolemyServer.CONFIG;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set up the initial singleton reference and Hessian proxy factory
     *  that will be used within the JUnit test cases.
     *  @exception Exception If there is an error creating the Hessian proxy.
     */
    @Before
    public void setUp() throws Exception {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setUser("guest");
        factory.setPassword("guest");

        _ptolemyServer = PtolemyServer.getInstance();
        _servletProxy = (IServerManager) factory.create(IServerManager.class,
                _ptolemyServer.getServletUrl());
    }

    /** Test the ability to create a new simulation request and to ensure that
     *  a non-null ticket is returned, the returned ticket has an id, the number
     *  of simulations stored has increased by one, and the simulation is in Idle
     *  state. 
     *  @exception Exception If there is an problem opening the model URL or
     *  communicating with the command servlet.
     */
    @Test
    public void openSimulation() throws Exception {
        int simulations = _ptolemyServer.numberOfSimulations();
        _response = _openRemoteModel();

        assertNotNull(_response);
        assertNotNull(_response.getTicket().getTicketID());
        assertEquals(simulations + 1, _ptolemyServer.numberOfSimulations());
        assertEquals(Manager.IDLE,
                _ptolemyServer.getStateOfSimulation(_response.getTicket()));
    }

    /** Test the ability to start a newly created simulation request and to ensure that
     *  the state of the execution changed to preinitialize and iterating.
     *  @exception Exception If there is an problem opening the model URL, starting the
     *  simulation, or communicating with the command servlet.
     */
    @Test
    public void startSimulation() throws Exception {
        RemoteModelResponse response = _openRemoteModel();
        Ticket ticket = response.getTicket();
        int simulations = _ptolemyServer.numberOfSimulations();

        // Start the thread and verify that doing so has not altered the thread count.
        _servletProxy.start(ticket);
        assertEquals(simulations, _ptolemyServer.numberOfSimulations());
        // Wait until the simulation had time to start iterating.
        Thread.sleep(1000);
        assertEquals(Manager.ITERATING,
                _ptolemyServer.getStateOfSimulation(ticket));
    }

    /** Test the ability to pause and resume a newly created and started simulation request
     *  and to ensure that the state of the execution changed to paused and back.
     *  
     *  This test can fail if the execution cannot pause. For example, some models using the
     *  PN director cannot get into the paused state.
     *  
     *  @exception Exception If there is an problem opening the model URL, starting, pausing,
     *  or resuming the simulation, or communicating with the command servlet.
     */
    @Test(expected = AssertionError.class)
    public void pauseAndResumeSimulation() throws Exception {
        RemoteModelResponse response = _openRemoteModel();
        Ticket ticket = response.getTicket();
        int simulations = _ptolemyServer.numberOfSimulations();

        _servletProxy.start(ticket);
        // Wait until the simulation had time to start iterating.
        Thread.sleep(1000);
        assertEquals(Manager.ITERATING,
                _ptolemyServer.getStateOfSimulation(ticket));

        // Pause the thread, verify that doing so has not altered the thread count,
        // and also check for valid state transition in the simulation.
        _servletProxy.pause(ticket);
        assertEquals(simulations, _ptolemyServer.numberOfSimulations());
        // Wait a bit for the simulation to pause as every actor needs to get into
        // the right state.
        Thread.sleep(1000);
        assertEquals(Manager.PAUSED,
                _ptolemyServer.getStateOfSimulation(ticket));
        // Wait a bit and restart the simulation.
        Thread.sleep(1000);
        _servletProxy.resume(ticket);
        assertEquals(Manager.ITERATING,
                _ptolemyServer.getStateOfSimulation(ticket));
    }

    /** Test the ability to stop an already iterating simulation request and to ensure
     *  that the state of the execution changed to idle.
     *  @exception Exception If there is an problem opening the model URL, starting the
     *  simulation, stopping it, or communicating with the command servlet.
     */
    @Test
    public void stopSimulation() throws Exception {
        RemoteModelResponse response = _openRemoteModel();
        Ticket ticket = response.getTicket();
        int simulations = _ptolemyServer.numberOfSimulations();

        // Start the thread
        _servletProxy.start(ticket);
        // Wait until the simulation had time to start iterating.
        Thread.sleep(1000);

        // Stop the thread and verify that doing so has not altered the thread count.
        _servletProxy.stop(ticket);
        assertEquals(simulations, _ptolemyServer.numberOfSimulations());
        assertEquals(Manager.IDLE, _ptolemyServer.getStateOfSimulation(ticket));
    }

    /** Test the ability to close an existing simulation request and to ensure
     *  that the simulation is removed from the server.
     *  @exception Exception If there is an problem opening the model URL, starting the
     *  simulation, closing it, or communicating with the command servlet.
     */
    @Test
    public void closeSimulation() throws Exception {
        RemoteModelResponse response = _openRemoteModel();
        Ticket ticket = response.getTicket();
        int simulations = _ptolemyServer.numberOfSimulations();

        // Start the thread
        _servletProxy.start(ticket);
        // Wait until the simulation had time to start iterating.
        Thread.sleep(1000);

        // Close the thread and verify that doing so decreased the thread count,
        // and the ticket is no longer valid.
        _servletProxy.close(ticket);
        assertEquals(simulations - 1, _ptolemyServer.numberOfSimulations());

        // Try to start the ticket again. This should result in an exception.
        assertEquals(null, PtolemyServer.getInstance()
                .getSimulationTask(ticket));
    }

    /** Call the shutdown() method on the singleton and destroy all
     *  references to it.
     *  @exception Exception If there was an error shutting down the broker or
     *  servlet.
     */
    @After
    public void shutdown() throws Exception {
        if (_response != null) {
            _ptolemyServer.close(_response.getTicket());
        }
        _ptolemyServer.shutdown();
        _ptolemyServer = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Submit a request to the servlet to open a simulation and acquire
     *  the ticket reference.
     *  @exception Exception If there is an problem opening the model URL or
     *  communicating with the command servlet.
     *  @return Ticket The ticket reference to the simulation request.
     */
    private RemoteModelResponse _openRemoteModel() throws Exception {
        URL modelUrl = ServletTest.class
                .getResource("/ptserver/test/junit/addermodel.xml");
        URL layoutUrl = ServletTest.class
                .getResource("/ptserver/test/junit/addermodel_test.layout.xml");
        RemoteModelResponse response = _servletProxy.open(
                modelUrl.toExternalForm(), layoutUrl.toExternalForm());
        return response;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Handle to the Ptolemy server singleton.
     */
    private PtolemyServer _ptolemyServer;

    /** Handle to the Hessian proxy.
     */
    private IServerManager _servletProxy;

    private RemoteModelResponse _response;
}
