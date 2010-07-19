/* An dynamic rate multidimensional SDF actor.

 Copyright (c) 2009-2010 The Regents of the University of California.
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

package ptolemy.domains.pthales.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.domains.pthales.kernel.PthalesReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 A composite actor imposes the use of dynamic PthalesReceivers
 strips header information and computes the its iteration automatically.
 A PthalesCompositeActor can contain actors from different model (as SDF),
 but the port must be a PthalesIOPort, because of the ArrayOL parameters. 

 @author Dai Bui
 @see ptolemy.actor.TypedIOPort
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (daib)
 @Pt.AcceptedRating Red (daib)
 */

public class PthalesDynamicCompositeActor extends PthalesCompositeActor {

    /**
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public PthalesDynamicCompositeActor() throws NameDuplicationException,
            IllegalActionException {
    }

    /** Constructor
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public PthalesDynamicCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * @param workspace
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public PthalesDynamicCompositeActor(Workspace workspace)
            throws NameDuplicationException, IllegalActionException {
        super(workspace);
    }

    /** Compute the number of iterations of the actor
     * based on multiple input ports. The iteraions is the 
     * minimum of legal iterations from the ports.
     * 
     * @return The number of iterations of the actor.
     * @throws NoTokenException
     * @throws IllegalActionException
     */
    public int computeIterations() throws NoTokenException,
            IllegalActionException {

        int minIterations = -1;

        for (Object port : inputPortList()) {
            IOPort portIn = (IOPort) port;

            // Header
            int nDims = ((IntToken) portIn.get(0)).intValue();
            int nbTokens = ((IntToken) portIn.get(0)).intValue();
            Token[] headerIn = portIn.get(0, nDims * 2);

            //            int iterations = nbTokens;
            LinkedHashMap<String, Integer> sizes = new LinkedHashMap<String, Integer>();
            for (int i = 0; i < nDims; i++) {
                sizes.put(((StringToken) headerIn[2 * i]).stringValue(),
                        ((IntToken) headerIn[2 * i + 1]).intValue());
            }

            Integer[] repetitions = computeIterations(portIn, sizes);

            int iterations = nbTokens;

            for (int i = 0; i < repetitions.length; i++) {
                iterations *= repetitions[i];
            }

            if (minIterations < 0 || minIterations > iterations) {
                _repetitions = repetitions;
                minIterations = iterations;
            }
        }

        if (minIterations < 0) {
            minIterations = 0;
            //FIXME How to set the repetitions?
        }

        return minIterations;
    }

    /** Create receivers for each port. If the port is an
     *  input port, then receivers are created for outside
     *  connections. If it is an output port, then receivers
     *  are created for inside connections. This method replaces
     *  any pre-existing receivers, so any data they contain
     *  will be lost.
     *  @exception IllegalActionException If any port throws it.
     */
    public void createReceivers() throws IllegalActionException {
        super.createReceivers();
        for (Object port : inputPortList()) {
            //FIXME We could check if the port is set to a dynamic port?

            Receiver[][] receivers = ((TypedIOPort) port).getReceivers();

            for (int i = 0; i < ((IOPort) port).getWidth(); i++) {
                ((PthalesReceiver) receivers[i][0]).setDynamic(true);
            }
        }
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED.  If stop() is called during
     *  this iteration, then cease iterating and return STOP_ITERATING.
     *  <p>
     *  This base class method actually invokes prefire(), fire(),
     *  and postfire(), as described above, but a derived class
     *  may override the method to execute more efficient code.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    public int iterate(int count) throws IllegalActionException {
        int iterations = count;

        //check if we need to compute the iterations
        if (count == 0) {
            iterations = computeIterations();
        }

        for (Object portIn : inputPortList()) {
            IOPort port = (IOPort) portIn;
            Receiver[][] receivers = port.getReceivers();
            if (receivers != null && receivers.length > 0) {
                for (Receiver[] receiverss : receivers) {
                    if (receiverss != null && receiverss.length > 0) {
                        for (Receiver receiver : receiverss) {
                            // FIXME: Is the cast to LinkedHashSet
                            // safe?  Depends on the Java
                            // implementation of LinkedHashMap.
                            ((PthalesReceiver) receiver)
                                    .setReadParameters(_repetitions);
                        }
                    }
                }
            }
        }

        _headerSent = false;

        return super.iterate(iterations);
    }

    /** If this actor is opaque, invoke the prefire() method of the local
     *  director. This method returns true if the actor is ready to fire
     *  (determined by the prefire() method of the director).
     *  It is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if the director's prefire() method throws it, or if this actor
     *   is not opaque.
     */
    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();

        if (!_headerSent) {
            //send header information to the next actor
            Iterator<?> outports = outputPortList().iterator();

            while (outports.hasNext() && !_stopRequested) {
                //FIXME check if the port is dynamic

                IOPort p = (IOPort) outports.next();
                LinkedHashMap<String, Integer> sizes = PthalesIOPort
                        .getArraySizes(p, _repetitions);

                //Header construction
                List<Token> header = new ArrayList<Token>();

                int nbDims = PthalesIOPort.getDimensions(p).length;

                header.add(new IntToken(nbDims));
                header.add(new IntToken(PthalesIOPort.getNbTokenPerData(p)));

                for (String dim : sizes.keySet()) {
                    header.add(new StringToken(dim));
                    header.add(new IntToken(sizes.get(dim)));
                }

                // then sent to output
                for (int i = 0; i < p.getWidth(); i++) {
                    for (int j = 0; j < header.size(); j++) {
                        p.send(i, header.get(j));
                    }
                }

            }

            _headerSent = true;
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* flag indicating if header has been sent or not */
    private boolean _headerSent = false;

    /* cached value for the repetition parameter */
    private Integer[] _repetitions;
}
