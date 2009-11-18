/* Code generator for the Chaco graph partitioner.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.chaco.kernel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

////ChacoCodeGenerator

/** Base class for Chaco code generator.
 *  Chaco is a graph partitioning software. The ChacoCodeGenerator takes a 
 *  Ptolemy model and generates the corresponding input file for Chaco.
 *  
 *  @author Jia Zou, Isaac Liu, Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (mankit)
 */

public class ChacoCodeGenerator extends CodeGenerator {

    /** Create a new instance of the C code generator.
     *  @param container The container.
     *  @param name The name of the C code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public ChacoCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        generatorPackage.setExpression("ptolemy.codegen.chaco");

        action = new StringParameter(this, "action");
        action.addChoice("GENERATE");
        action.addChoice("TRANSFORM");
        action.addChoice("CLEAR");
        action.setExpression("GENERATE");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    StringParameter action;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate an array of distinct colors. The distinctness of two
     * colors is determined by the absolute difference in their RGB
     * components, which range from 0.0 to 1.0f. Therefore, the maximum
     * distance between any two colors is 3. Generating a number of 
     * distinct colors is to evenly spread the distance across their
     * RGB values. <p> 
     * 
     * It first calculates the numberOfDiscreteValues from approximating
     * the cube root of the given numberOfColors. Then, it generates
     * a table of discrete colorValues. For example, if the
     * numberOfDiscreteValues is 3, then the table would contain
     * the values {1.0, 0.5, 0.0}. Finally, the RBG values are generated
     * by picking the permutation out of these values. <p>
     * 
     * The algorithm is learned from the Berkeley SWAMI (Shared Wisdom 
     * through the Amalgamation of Many Interpretations) project.
     * 
     * <p>See Fisher D, Hildrum K, Hong J, Newman M and Vuduc R, SWAMI: 
     * a framework for collaborative filtering algorithm development 
     * and evaluation 1999. <a href=http://guir.berkeley.edu/projects/swami/>http://guir.berkeley.edu/projects/swami/</a>. 
     * @param  numberOfColors The number of distinct colors to generate.
     * @return An array size numberOfColors filled with distinct colors.
     */
    public static final String[] getDistinctColors(int numberOfColors) {

        String[] colors = new String[numberOfColors];

        // Approximate the cube root of numberOfColors.
        int numberOfDiscreteValues = 0;
        int cube = 0;
        while (numberOfColors > cube) {
            numberOfDiscreteValues++;
            cube = numberOfDiscreteValues * numberOfDiscreteValues
                    * numberOfDiscreteValues;
        }

        float[] colorValues = new float[numberOfDiscreteValues];
        int rIndex;
        int gIndex;
        int bIndex;

        // Generate a table of discrete values for RGB.
        for (int i = 0; i < numberOfDiscreteValues; i++) {
            colorValues[i] = 1.0f - (i) / ((float) numberOfDiscreteValues - 1);

            // Fix the values just in case.
            if (Float.isNaN(colorValues[i]) || Float.isInfinite(colorValues[i])) {
                colorValues[i] = 1.0f;
            } else if (colorValues[i] < 0.0f) {
                colorValues[i] = 0.0f;
            } else if (colorValues[i] > 1.0f) {
                colorValues[i] = 1.0f;
            }
        }

        // Now generate the colors.
        rIndex = 0;
        gIndex = 0;
        bIndex = 0;

        for (int i = 0; i < colors.length; i++) {
            colors[i] = "{" + colorValues[rIndex] + ", " + colorValues[gIndex]
                    + ", " + colorValues[bIndex] + ", 1.0}";

            // Now go to the next color values.
            bIndex++;
            if (bIndex >= numberOfDiscreteValues) {
                gIndex++;
                if (gIndex >= numberOfDiscreteValues) {
                    rIndex++;
                    rIndex %= numberOfDiscreteValues;
                }
                gIndex %= numberOfDiscreteValues;
            }
            bIndex %= numberOfDiscreteValues;
        }
        return colors;
    }

    /**
     * 
     */
    public int generateCode(StringBuffer code) throws KernelException {

        if (action.getExpression().equals("CLEAR")) {
            transformGraph();
            return 0;
        }

        if (action.getExpression().equals("TRANSFORM")) {
            if (_generated == false) {
                throw new IllegalActionException(this, (Throwable) null,
                        "GENERATE first before TRANSFORM");
            }
            transformGraph();
            return 0;
        }

        _reset();

        _codeFileName = _writeCode(code);

        StringBuffer codeBuffer = new StringBuffer();

        // Traversing through the actor list to see all actors
        CompositeEntity compositeActor = (CompositeEntity) getContainer();
        for (Actor actor : (List<Actor>) compositeActor.deepEntityList()) {
            _numVertices++;
            _HashActorKey.put(actor, _numVertices);
            _HashNumberKey.put(_numVertices, actor);
        }
        code.append(_numVertices + " ");

        // Traverse through the actors again to get all edges
        for (Actor actor : (List<Actor>) compositeActor.deepEntityList()) {
            int actorId = (Integer) _HashActorKey.get(actor);
            codeBuffer.append(actorId + " ");

            // Get vertex weights from the model
            codeBuffer.append(_getVertexWeight(actor) + " ");

            // for each input port within the current actor being analyzed
            for (TypedIOPort inputPort : (List<TypedIOPort>) actor
                    .inputPortList()) {

                // for each source port that sends event to this input port
                for (TypedIOPort sourcePort : (List<TypedIOPort>) inputPort
                        .sourcePortList()) {
                    Actor sourceActor = (Actor) sourcePort.getContainer();
                    int outInt = (Integer) _HashActorKey.get(sourceActor);
                    codeBuffer.append(outInt + " ");
                    HashMap partialEdgeWeights = new HashMap();
                    partialEdgeWeights.put(inputPort, "0");

                    LinkedList middlePortList = new LinkedList();
                    middlePortList.add(inputPort);
                    Iterator middlePortListIt = middlePortList.iterator();
                    // Add the edge weight from the model by traversing through
                    // the relations and ports connected to these relations
                    boolean foundFlag = false;
                    while (foundFlag == false && middlePortListIt.hasNext()) {
                        TypedIOPort middlePort = (TypedIOPort) middlePortListIt
                                .next();

                        if (middlePort.equals(sourcePort)) {
                            int temp = Integer
                                    .parseInt((String) partialEdgeWeights
                                            .get(middlePort));
                            codeBuffer.append(Integer.toString(temp) + " ");
                            foundFlag = true;
                        } else {
                            for (Relation relation : (List<Relation>) middlePort
                                    .linkedRelationList()) {
                                List nextMiddlePortList = relation
                                        .linkedPortList(middlePort);
                                Iterator nextMiddlePortListIt = nextMiddlePortList
                                        .listIterator();
                                while (foundFlag == false
                                        && nextMiddlePortListIt.hasNext()) {
                                    TypedIOPort nextMiddlePort = (TypedIOPort) nextMiddlePortListIt
                                            .next();
                                    if (partialEdgeWeights.get(nextMiddlePort) == null) {
                                        // if this port is part of a composite actor
                                        if (!nextMiddlePort.isOpaque()) {
                                            int temp = Integer
                                                    .parseInt(_getEdgeWeight(relation))
                                                    + Integer
                                                            .parseInt((String) partialEdgeWeights
                                                                    .get(middlePort));
                                            partialEdgeWeights.put(
                                                    nextMiddlePort, Integer
                                                            .toString(temp));
                                            // get the ports the port is connected to
                                            if (nextMiddlePort.isInput()) {
                                                middlePortList
                                                        .add(nextMiddlePort);
                                            } else {
                                                for (TypedIOPort hierarchicalConnectedPort : (List<TypedIOPort>) nextMiddlePort
                                                        .deepInsidePortList()) {
                                                    for (Relation hierarchicalRelation : (List<Relation>) hierarchicalConnectedPort
                                                            .linkedRelationList()) {
                                                        for (TypedIOPort relationPort : (List<TypedIOPort>) hierarchicalRelation
                                                                .linkedPortList(hierarchicalConnectedPort)) {
                                                            if (relationPort
                                                                    .equals(nextMiddlePort)) {
                                                                int temp2 = Integer
                                                                        .parseInt(_getEdgeWeight(hierarchicalRelation))
                                                                        + Integer
                                                                                .parseInt((String) partialEdgeWeights
                                                                                        .get(nextMiddlePort));
                                                                partialEdgeWeights
                                                                        .put(
                                                                                hierarchicalConnectedPort,
                                                                                Integer
                                                                                        .toString(temp2));
                                                            }
                                                        }
                                                    }
                                                    middlePortList
                                                            .add(hierarchicalConnectedPort);
                                                }
                                            }
                                            //List hierarchicalConnectedPortList = nextMiddlePort.deepConnectedPortList();
                                            //                                          List debugList = nextMiddlePort.deepInsidePortList();
                                            //                                          Iterator debugListIt = debugList.iterator();
                                            //                                          while(debugListIt.hasNext()) {
                                            //                                          TypedIOPort debugPort = (TypedIOPort)debugListIt.next();
                                            //                                          for(Relation debugRelation : (List<Relation>) debugPort.linkedRelationList()) {
                                            //                                          List debugPort2 = debugRelation.linkedPortList();
                                            //                                          debugPort2.get(1);
                                            //                                          }
                                            //                                          }
                                            //                                            int temp = Integer.parseInt((String)partialEdgeWeights.get(middlePort)) + 
                                            //                                            Integer.parseInt(_getEdgeWeight(relation));
                                            //                                            partialEdgeWeights.put(nextMiddlePort, Integer.toString(temp));
                                            //
                                            //                                            // go into where this input port is connected to.
                                            //                                            middlePortList.add(nextMiddlePort);
                                            middlePortList.remove(middlePort);
                                            middlePortListIt = middlePortList
                                                    .iterator();
                                        } else if (nextMiddlePort
                                                .equals(sourcePort)) {
                                            int temp = Integer
                                                    .parseInt((String) partialEdgeWeights
                                                            .get(middlePort))
                                                    + Integer
                                                            .parseInt(_getEdgeWeight(relation));
                                            codeBuffer.append(Integer
                                                    .toString(temp)
                                                    + " ");
                                            foundFlag = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //                        
            //                        for (Relation relation : (List<Relation>) middlePort.linkedRelationList()) {
            //                            List nextMiddlePortList = relation.linkedPortList(middlePort);
            //                            Iterator nextMiddlePortListIt = (Iterator) nextMiddlePortList.listIterator();
            //                            while (foundFlag == false && nextMiddlePortListIt.hasNext()) {
            //                                TypedIOPort nextMiddlePort = (TypedIOPort)nextMiddlePortListIt.next();
            //                                if (partialEdgeWeights.get(nextMiddlePort) == null) {
            //                                    if (!nextMiddlePort.isOpaque()) {
            //
            //                                        int temp = Integer.parseInt((String)partialEdgeWeights.get(middlePort)) + 
            //                                        Integer.parseInt(_getEdgeWeight(relation));
            //                                        partialEdgeWeights.put(nextMiddlePort, Integer.toString(temp));
            //
            //                                        // go into where this input port is connected to.
            //                                        middlePortList.add(nextMiddlePort);
            //                                        middlePortList.remove(middlePort);
            //                                        middlePortListIt = middlePortList.iterator();
            //
            //                                    } else if (nextMiddlePort.equals(sourcePort)) {
            //                                        int temp = Integer.parseInt( (String)partialEdgeWeights.get(middlePort)) + 
            //                                        Integer.parseInt(_getEdgeWeight(relation));
            //                                        codeBuffer.append(Integer.toString(temp) + " ");
            //                                        foundFlag = true;
            //                                    }
            //                                }
            //                            }
            //                        }
            //                    }
            //                }
            //            }

            for (TypedIOPort outputPort : (List<TypedIOPort>) actor
                    .outputPortList()) {

                for (TypedIOPort sinkPort : (List<TypedIOPort>) outputPort
                        .sinkPortList()) {

                    Actor tempActor = (Actor) sinkPort.getContainer();
                    int inInt = (Integer) _HashActorKey.get(tempActor);
                    codeBuffer.append(inInt + " ");

                    HashMap partialEdgeWeights = new HashMap();
                    partialEdgeWeights.put(outputPort, "0");
                    LinkedList middlePortList = new LinkedList();
                    middlePortList.add(outputPort);
                    Iterator middlePortListIt = middlePortList.iterator();
                    // Add the edge weight from the model by traversing through
                    // the relations and ports connected to these relations
                    boolean foundFlag = false;
                    while (foundFlag == false && middlePortListIt.hasNext()) {
                        TypedIOPort middlePort = (TypedIOPort) middlePortListIt
                                .next();
                        if (middlePort.equals(sinkPort)) {
                            int temp = Integer
                                    .parseInt((String) partialEdgeWeights
                                            .get(middlePort));
                            codeBuffer.append(Integer.toString(temp) + " ");
                            foundFlag = true;
                        } else {
                            for (Relation relation : (List<Relation>) middlePort
                                    .linkedRelationList()) {
                                List nextMiddlePortList = relation
                                        .linkedPortList(middlePort);
                                Iterator nextMiddlePortListIt = nextMiddlePortList
                                        .listIterator();
                                while (foundFlag == false
                                        && nextMiddlePortListIt.hasNext()) {
                                    TypedIOPort nextMiddlePort = (TypedIOPort) nextMiddlePortListIt
                                            .next();
                                    if (partialEdgeWeights.get(nextMiddlePort) == null) {
                                        // iF this port is part of a composite actor
                                        if (!nextMiddlePort.isOpaque()) {
                                            int temp = Integer
                                                    .parseInt(_getEdgeWeight(relation))
                                                    + Integer
                                                            .parseInt((String) partialEdgeWeights
                                                                    .get(middlePort));
                                            partialEdgeWeights.put(
                                                    nextMiddlePort, Integer
                                                            .toString(temp));
                                            // get the ports the port is connected to
                                            if (!nextMiddlePort.isInput()) {
                                                middlePortList
                                                        .add(nextMiddlePort);
                                            } else {
                                                for (TypedIOPort hierarchicalConnectedPort : (List<TypedIOPort>) nextMiddlePort
                                                        .deepInsidePortList()) {
                                                    for (Relation hierarchicalRelation : (List<Relation>) hierarchicalConnectedPort
                                                            .linkedRelationList()) {
                                                        for (TypedIOPort relationPort : (List<TypedIOPort>) hierarchicalRelation
                                                                .linkedPortList(hierarchicalConnectedPort)) {
                                                            if (relationPort
                                                                    .equals(nextMiddlePort)) {
                                                                int temp2 = Integer
                                                                        .parseInt(_getEdgeWeight(hierarchicalRelation))
                                                                        + Integer
                                                                                .parseInt((String) partialEdgeWeights
                                                                                        .get(nextMiddlePort));
                                                                partialEdgeWeights
                                                                        .put(
                                                                                hierarchicalConnectedPort,
                                                                                Integer
                                                                                        .toString(temp2));
                                                            }
                                                        }
                                                    }
                                                    middlePortList
                                                            .add(hierarchicalConnectedPort);
                                                }
                                            }
                                            //List hierarchicalConnectedPortList = nextMiddlePort.deepConnectedPortList();
                                            //                                          List debugList = nextMiddlePort.deepInsidePortList();
                                            //                                          Iterator debugListIt = debugList.iterator();
                                            //                                          while(debugListIt.hasNext()) {
                                            //                                          TypedIOPort debugPort = (TypedIOPort)debugListIt.next();
                                            //                                          for(Relation debugRelation : (List<Relation>) debugPort.linkedRelationList()) {
                                            //                                          List debugPort2 = debugRelation.linkedPortList();
                                            //                                          debugPort2.get(1);
                                            //                                          }
                                            //                                          }
                                            //                                            int temp = Integer.parseInt((String)partialEdgeWeights.get(middlePort)) + 
                                            //                                            Integer.parseInt(_getEdgeWeight(relation));
                                            //                                            partialEdgeWeights.put(nextMiddlePort, Integer.toString(temp));
                                            //
                                            //                                            // go into where this input port is connected to.
                                            //                                            middlePortList.add(nextMiddlePort);
                                            middlePortList.remove(middlePort);
                                            middlePortListIt = middlePortList
                                                    .iterator();

                                        } else if (nextMiddlePort
                                                .equals(sinkPort)) {
                                            int temp = Integer
                                                    .parseInt((String) partialEdgeWeights
                                                            .get(middlePort))
                                                    + Integer
                                                            .parseInt(_getEdgeWeight(relation));
                                            codeBuffer.append(Integer
                                                    .toString(temp)
                                                    + " ");
                                            foundFlag = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    _numEdges++;
                }
            }
            codeBuffer.append(_eol);
        }
        code.append(_numEdges + " 111" + _eol);

        code.append(codeBuffer);

        _writeChacoInputFile(code.toString());

        _generated = true;

        return 0;
    }

    /**
     * Testing main for the ChacoCodeGenerator.
     * @param args The given command-line arguments.
     */
    public static void main(String[] args) {
        System.out.println(_arrayToString(getDistinctColors(26)));
        System.out.println(_arrayToString(getDistinctColors(27)));
        System.out.println(_arrayToString(getDistinctColors(28)));
    }

    public void transformGraph() throws KernelException {

        if (!_isClearMode()) {
            _readChacoOutputFile();
        }

        // Traverse through the graph and annotate each port whether
        // it is a _isMpiBuffer (send/receive)
        _annotateMpiPorts();

        // Repaint the GUI.
        getContainer().requestChange(
                new ChangeRequest(this, "Repaint the GUI.") {
                    protected void _execute() throws Exception {
                    }
                });
    }

    /**
     * Return the String representation of a Object array.
     * @param array The given Object array.
     * @return The String representation of the Object array.
     */
    private static String _arrayToString(Object[] array) {
        StringBuffer result = new StringBuffer();
        result.append("{");
        for (Object element : array) {
            result.append(element + ", ");
        }
        return result.toString() + "}";
    }

    private void _annotateMpiPorts() throws IllegalActionException {
        int count = 1;
        int mpiBufferId = 0;
        int localBufferId = 0;

        _highlightPartitions();

        while (count <= _numVertices) {
            Actor actor = (Actor) _HashNumberKey.get(count);

            Parameter partitionAttribute = _getPartitionParameter(actor);

            for (TypedIOPort inputPort : (List<TypedIOPort>) actor
                    .inputPortList()) {

                // Clear the _isMpiBuffer parameter if it already exists
                _removeAttribute(inputPort, "_mpiBuffer");
                _removeAttribute(inputPort, "_localBuffer");
                _removeAttribute(inputPort, "_showInfo");

                if (_isClearMode()) {
                    continue;
                }

                int channel = 0;
                for (TypedIOPort sourcePort : (List<TypedIOPort>) inputPort
                        .sourcePortList()) {
                    Actor sourceActor = (Actor) sourcePort.getContainer();

                    Parameter attrTemp = _getPartitionParameter(sourceActor);

                    if (!partitionAttribute.getExpression().equals(
                            attrTemp.getExpression())) {

                        StringAttribute mpiBuffer = _getMpiAttribute(inputPort);

                        StringAttribute showInfo = _getShowInfoAttribute(inputPort);

                        String mpiBufferValue = mpiBuffer.getExpression();
                        if (mpiBufferValue.equals("")) {
                            mpiBufferValue = "receiver";
                        }
                        mpiBufferValue = mpiBufferValue.concat("_ch[" + channel
                                + "]" + "id[" + mpiBufferId + "]");

                        mpiBuffer.setExpression(mpiBufferValue);
                        showInfo.setExpression(mpiBufferValue);
                        // Keep track of the number of MPI connections
                        mpiBufferId++;
                    } else {
                        // Annotate local buffer.
                        StringAttribute localBuffer = _getLocalAttribute(inputPort);
                        if (!_isClearMode()) {
                            String localBufferValue = localBuffer
                                    .getExpression();

                            localBufferValue = localBufferValue.concat("_ch["
                                    + channel + "]" + "id[" + localBufferId
                                    + "]");
                            localBuffer.setExpression(localBufferValue);
                        }

                        localBufferId++;
                    }
                    channel++;
                }
            }

            for (TypedIOPort outputPort : (List<TypedIOPort>) actor
                    .outputPortList()) {

                // Clear the _isMpiBuffer parameter if it already exists
                _removeAttribute(outputPort, "_mpiBuffer");

                if (!_isClearMode()) {
                    continue;
                }

                int sinkIndex = 0;
                for (TypedIOPort sinkPort : (List<TypedIOPort>) outputPort
                        .sinkPortList()) {

                    Actor sinkActor = (Actor) sinkPort.getContainer();
                    Parameter sinkPartitionAttribute = _getPartitionParameter(sinkActor);

                    if (!partitionAttribute.getExpression().equals(
                            sinkPartitionAttribute.getExpression())) {

                        StringAttribute mpiAttribute = _getMpiAttribute(outputPort);
                        String mpiValue = mpiAttribute.getExpression();

                        if (mpiValue.equals("")) {
                            mpiValue = "sender";
                        }
                        mpiValue = mpiValue.concat("_ch[" + sinkIndex + "]");
                        mpiAttribute.setExpression(mpiValue);
                    }
                }
                sinkIndex++;
            }

            count++;
        }
        Parameter numConnections = _getNumConnectionsParameter();
        numConnections.setExpression(Integer.toString(mpiBufferId));
    }

    private String _getVertexWeight(Actor actor) {
        Parameter vertexParam = (Parameter) ((NamedObj) actor)
                .getAttribute("_vertexWeight");

        if (vertexParam == null) {
            return "1";
        } else {
            return vertexParam.getExpression();
        }
    }

    private static String _getEdgeWeight(Relation relation) {
        Parameter edgeParam = (Parameter) relation.getAttribute("_edgeWeight");

        if (edgeParam == null) {
            return "1";
        } else {
            return edgeParam.getExpression();
        }
    }

    /** Write the code with the sanitized model name postfixed with "_tb"
     *  if the current generate file is the testbench module; Otherwise,
     *  write code with the sanitized model name (as usual). 
     *  @param code The StringBuffer containing the code.
     *  @return The name of the file that was written.
     *  @exception IllegalActionException  If the super class throws it.
     */
    protected String _writeCode(StringBuffer code)
            throws IllegalActionException {
        _sanitizedModelName = CodeGeneratorHelper.generateName(_model);
        String tempCodeFileName = super._writeCode(code);
        if (tempCodeFileName.endsWith(".chaco")) {
            //tempCodeFileName.substring(0, tempCodeFileName.indexOf(".")).concat(".graph");
            tempCodeFileName = tempCodeFileName.replaceAll(".chaco", ".graph");
        }
        return tempCodeFileName;
    }

    protected void _writeChacoInputFile(String code)
            throws IllegalActionException {
        BufferedWriter out = null;
        try {
            // Create file 
            FileWriter fstream = new FileWriter(_codeFileName);
            out = new BufferedWriter(fstream);
            out.write(code);
        } catch (Exception ex) {//Catch exception if any
            throw IllegalActionException(getContainer(), ex, "Failed to write to "
                    + _codeFileName);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                    throw IllegalActionException(getContainer(), ex, "Failed to close "
                    + _codeFileName);
                }
            }
        }
    }

    protected void _readChacoOutputFile() throws IllegalActionException {

        String codeFileNameWritten = this.getCodeFileName();
        codeFileNameWritten = codeFileNameWritten.replaceAll(".graph", ".out");

        File file = new File(codeFileNameWritten);
        FileInputStream fis = null;
        BufferedReader reader = null;

        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis));

            int actorNum = 1;
            String rankString = null;
            while ((rankString = reader.readLine()) != null) {

                // this statement reads the line from the file and print it to
                // the console.

                Actor actor = (Actor) _HashNumberKey.get(actorNum);

                Parameter parameter = _getPartitionParameter(actor);
                parameter.setExpression(rankString);

                actorNum++;

                _rankNumbers.add(rankString);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ex) {
                    System.err.println("Failed to close " + file + " " + ex);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    System.err.println("Failed to close " + file + " " + ex);
                }
            }
        }

    }

    private void _reset() {
        _HashActorKey.clear();
        _HashNumberKey.clear();
        _colorMap.clear();
        _rankNumbers.clear();
        _numVertices = 0;
        _numEdges = 0;
    }

    private void _highlightPartitions() throws IllegalActionException {
        if (!_isClearMode()) {
            // Generate and map the distinct colors to partitions.
            Iterator rankNumbers = _rankNumbers.iterator();
            for (String color : getDistinctColors(_rankNumbers.size() - 1)) {
                //System.out.println("color: " + color + ", rank: " + rankNumbers.next());
                _colorMap.put(rankNumbers.next(), color);
            }
        }

        // Insert the highlight color attributes.
        CompositeEntity compositeActor = (CompositeEntity) getContainer();
        for (Actor actor : (List<Actor>) compositeActor.deepEntityList()) {

            if (_isClearMode()) {
                _removeAttribute((NamedObj) actor, "_partition");
                _removeAttribute((NamedObj) actor, "_highlightColor");

            } else {
                Parameter actorPartition = _getPartitionParameter(actor);
                String color = (String) _colorMap.get(actorPartition
                        .getExpression());

                ColorAttribute colorAttribute = _getHighlightAttribute((NamedObj) actor);
                colorAttribute.setExpression(color);
            }
        }
    }

    private ColorAttribute _getHighlightAttribute(NamedObj actor)
            throws IllegalActionException {
        ColorAttribute attribute = (ColorAttribute) actor
                .getAttribute("_highlightColor");

        try {
            if (_isClearMode() && attribute != null) {
                attribute.setContainer(null);
                return null;
            }

            return (attribute != null) ? attribute : new ColorAttribute(actor,
                    "_highlightColor");
        } catch (NameDuplicationException e) {
            assert false;
        }
        return null;
    }

    private boolean _isClearMode() {
        return action.getExpression().equals("CLEAR");
    }

    private Parameter _getPartitionParameter(Actor actor)
            throws IllegalActionException {
        Parameter attribute = (Parameter) ((NamedObj) actor)
                .getAttribute("_partition");

        try {
            if (attribute == null) {
                attribute = new Parameter((NamedObj) actor, "_partition");
            }
        } catch (NameDuplicationException e) {
            assert false;
        }
        return attribute;
    }

    private Parameter _getNumConnectionsParameter()
            throws IllegalActionException {

        Director director = ((TypedCompositeActor) getContainer())
                .getDirector();

        Parameter attribute = (Parameter) director
                .getAttribute("_numberOfMpiConnections");

        try {
            if (attribute == null) {
                attribute = new Parameter(director, "_numberOfMpiConnections");
            }
        } catch (NameDuplicationException e) {
            assert false;
        }
        return attribute;
    }

    private StringAttribute _getLocalAttribute(TypedIOPort port)
            throws IllegalActionException {
        StringAttribute attribute = (StringAttribute) port
                .getAttribute("_localBuffer");

        try {
            if (attribute == null) {
                attribute = new StringAttribute(port, "_localBuffer");
            }
        } catch (NameDuplicationException e) {
            assert false;
        }
        return attribute;
    }

    private StringAttribute _getMpiAttribute(TypedIOPort port)
            throws IllegalActionException {
        StringAttribute result = (StringAttribute) ((NamedObj) port)
                .getAttribute("_mpiBuffer");

        if (result == null) {
            try {
                result = new StringAttribute(port, "_mpiBuffer");
            } catch (NameDuplicationException e) {
                assert false;
            }
        }
        return result;
    }

    private StringAttribute _getShowInfoAttribute(TypedIOPort port)
            throws IllegalActionException {
        StringAttribute attribute = (StringAttribute) port
                .getAttribute("_showInfo");

        try {
            if (attribute == null) {
                attribute = new StringAttribute(port, "_showInfo");
            }
        } catch (NameDuplicationException e) {
            assert false;
        }
        return attribute;
    }

    private void _removeAttribute(NamedObj namedObj, String attributeName) {
        Attribute attribute = namedObj.getAttribute(attributeName);
        if (attribute != null) {
            try {
                attribute.setContainer(null);
            } catch (KernelException e) {
                assert false;
            }
        }
    }

    private HashMap _colorMap = new HashMap();

    // These hashtables save the mapping between Actor and an Integer
    // value used for Chaco identification
    private HashMap _HashActorKey = new HashMap();

    private HashMap _HashNumberKey = new HashMap();

    private HashSet _rankNumbers = new HashSet();

    private int _numVertices = 0;

    private int _numEdges = 0;

    private boolean _generated = false;

}
