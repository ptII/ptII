/* Actor that allows "dynamic" implementation of Pthales domain.

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
import java.util.LinkedHashMap;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * Add and/or propagate informations through relations to another
 * actor that can understand and apply modifications.
 *
 * @see ptolemy.domains.pthales.lib.PthalesRemoveHeaderActor

 * @author R&eacute;mi Barr&egrave;re
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PthalesAddHeaderActor extends PthalesAtomicActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesAddHeaderActor() throws IllegalActionException,
            NameDuplicationException {
        super();
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PthalesAddHeaderActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesAddHeaderActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the contents of the array, add a header containing the
     * number of dimensions and the size of each dimension at the
     * beginning of the array
     */
    public void fire() throws IllegalActionException {

        // Variables
        IOPort portIn = (IOPort) getPort("in");
        IOPort portOut = (IOPort) getPort("out");

        // One port in theory
        IOPort previousPort = (IOPort) portIn.connectedPortList().get(0);
        int nbDims = PthalesIOPort.getDimensions(previousPort).length;

        // Token Arrays from simulation
        Token[] tokensIn = null;

        // Input ports created and filled before elementary task called 
        int dataSize = PthalesIOPort.getDataProducedSize(portIn)
                * PthalesIOPort.getNbTokenPerData(portIn);
        tokensIn = new FloatToken[dataSize];
        tokensIn = portIn.get(0, dataSize);

        // Header construction
        List<Token> header = new ArrayList<Token>();

        LinkedHashMap<String, Integer> sizes = PthalesIOPort
                .getArraySizes(previousPort);

        header.add(new IntToken(nbDims));
        for (String dim : PthalesIOPort.getDimensions(previousPort)) {
            header.add(new IntToken(sizes.get(dim)));
        }

        // then sent to output
        for (int i = 0; i < portOut.getWidth(); i++) {
            for (int j = 0; j < header.size(); j++) {
                portOut.send(i, header.get(j));
            }
            portOut.send(i, tokensIn, dataSize);
        }
    }

    /** Create receivers. 
     * Propagates array sizes to every actors that are connected to
     * the associated PthalesRemoveActor. 
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Header
        IOPort portOut = (IOPort) getPort("out");
        IOPort portIn = (IOPort) getPort("in");

        // One port in theory
        IOPort port = (IOPort) portIn.connectedPortList().get(0);

        // 1 tokens per dimension + 1 token for the number of dimensions
        String[] dims = PthalesIOPort.getDimensions(port);
        int[] sizes = new int[dims.length];
        Object[] sizesString = PthalesIOPort.getArraySizes(port).values()
                .toArray();
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = (Integer) sizesString[i];
        }

        // Ports modifications
        PthalesIOPort.modifyPattern(portIn, dims, sizes);
        PthalesIOPort.modifyPattern(portOut, "global", 1
                + PthalesIOPort.getDimensions(port).length
                + PthalesIOPort.getArraySize(port));

        PthalesIOPort.propagateHeader(portOut, dims, sizes, 1 + PthalesIOPort
                .getDimensions(port).length, PthalesIOPort
                .getArraySizes(portIn));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    protected void _initialize() throws IllegalActionException,
            NameDuplicationException {
        super._initialize();

        // input port
        new TypedIOPort(this, "in", true, false);

        // output port
        new TypedIOPort(this, "out", false, true);
    }

}
