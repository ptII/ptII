/* Filter actors for port class changes.

 Copyright (c) 2003-2011 The Regents of the University of California.
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
package ptolemy.moml.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// PortClassChanges

/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter so that models from
 earlier releases will run in the current release.

 <p>This class will filter for actors that have had port class changes.

 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class PortClassChanges extends MoMLFilterSimple {
    /**  If the attributeName is "class" and attributeValue names a
     *   class that has had its port classes changed between releases,
     *   then substitute in the new port classes.
     *
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the value of the attributeValue argument.
     */
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        // This method gets called many times by the MoMLParser,
        // so we try to be smart about the number of comparisons
        // and we try to group comparisons together so that we
        // are not making the same comparison more than once.
        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("name")) {
            // record the name of the current attribute
            _lastNameSeen = attributeValue;

            // If the current actor has its port classes changed,
            // and if the current attribute value is a port contained
            // by the portMap, find the class mapping and record the
            // name of the port.
            if (_currentlyProcessingActorWithPortClassChanges
                    && (_portMap != null)
                    && _portMap.containsKey(attributeValue)) {
                _classMap = (HashMap) _portMap.get(attributeValue);

                _portName = attributeValue;
                _foundPort = true;
            }
        }

        if (attributeName.equals("class")) {
            // Look for lines like:
            // <entity name="VariableDelay"
            //   class="ptolemy.domains.de.lib.VariableDelay">
            if (_actorsWithPortClassChanges.containsKey(attributeValue)) {
                // We found a class (actor) with a port class change.
                _currentlyProcessingActorWithPortClassChanges = true;

                // Find the port mapping of the current actor.
                _portMap = (HashMap) _actorsWithPortClassChanges
                        .get(attributeValue);
            } else if (_foundPort && _lastNameSeen.equals(_portName)
                    && _classMap.containsKey(attributeValue)) {
                // We found the port.
                // We use the new class to replace the old class.
                String newClass = (String) _classMap.get(attributeValue);

                // Reset the state variables indicating a new start.
                _currentlyProcessingActorWithPortClassChanges = false;
                _foundPort = false;

                MoMLParser.setModified(true);
                return newClass;
            }
        }

        return attributeValue;
    }

    /** Reset private variables.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception if there is a problem substituting
     *  in the new value.
     */
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
        _foundPort = false;
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Update any actor port classes that have been\n"
                + "changed.\n"
                + "Below are the actors that are affected, along\n"
                + "with the old port class and the new port class:\n");
        Iterator actors = _actorsWithPortClassChanges.keySet().iterator();

        while (actors.hasNext()) {
            String actor = (String) actors.next();
            results.append("\t" + actor + ".");

            HashMap portMap = (HashMap) _actorsWithPortClassChanges.get(actor);
            Iterator portsMapEntries = portMap.entrySet().iterator();

            while (portsMapEntries.hasNext()) {
                Map.Entry ports = (Map.Entry) portsMapEntries.next();
                String port = (String) ports.getKey();
                results.append(port + "\n");

                HashMap classMap = (HashMap) ports.getValue();
                Iterator classChangesMapEntries = classMap.entrySet()
                        .iterator();
                while (classChangesMapEntries.hasNext()) {
                    Map.Entry classChanges = (Map.Entry) classChangesMapEntries
                            .next();
                    String oldClass = (String) classChanges.getKey();
                    String newClass = (String) classChanges.getValue();
                    results.append("\t\t" + oldClass + "\t -> " + newClass
                            + "\n");
                }
            }
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Map of actor classes a HashMap of old ports to new ports
    private static HashMap _actorsWithPortClassChanges;

    // Cache of map from old port classes to new port classes for
    // the port we are working on.
    private HashMap _classMap;

    // Set to true if we are currently processing an actor with port class
    // changes, set to false when we are done.
    private boolean _currentlyProcessingActorWithPortClassChanges = false;

    private boolean _foundPort = false;

    // Last "name" value seen, for use if we see an actor with "class" changes.
    private String _lastNameSeen;

    // Cache of map from actor to port for
    // the actor we are working on.
    private HashMap _portMap;

    // the name of the port that has its class changed.
    private String _portName;

    static {
        ///////////////////////////////////////////////////////////
        // Actors with port class changes.
        _actorsWithPortClassChanges = new HashMap();

        // VariableDelay: delay is now a ParameterPort,
        // not a DEIOPort.
        HashMap variableDelayPorts = new HashMap();
        HashMap variableDelayPortChanges = new HashMap();

        variableDelayPorts.put("ptolemy.domains.de.kernel.DEIOPort",
                "ptolemy.actor.parameters.ParameterPort");

        variableDelayPortChanges.put("delay", variableDelayPorts);

        _actorsWithPortClassChanges.put("ptolemy.domains.de.lib.VariableDelay",
                variableDelayPortChanges);

        // Server: serviceTime is now a ParameterPort,
        // not a DEIOPort.
        HashMap serverPorts = new HashMap();
        HashMap serverPortChanges = new HashMap();

        serverPorts.put("ptolemy.domains.de.kernel.DEIOPort",
                "ptolemy.actor.parameters.ParameterPort");

        serverPortChanges.put("serviceTime", serverPorts);

        _actorsWithPortClassChanges.put("ptolemy.domains.de.lib.Server",
                serverPortChanges);

        // ImagePartition input and output are now TypedIOPort
        HashMap sdfPortChanges = new HashMap();
        sdfPortChanges.put("ptolemy.domains.sdf.kernel.SDFIOPort",
                "ptolemy.actor.TypedIOPort");

        HashMap inputOutputTypedIOPortClassChanges = new HashMap();
        inputOutputTypedIOPortClassChanges.put("input", sdfPortChanges);
        inputOutputTypedIOPortClassChanges.put("output", sdfPortChanges);

        _actorsWithPortClassChanges.put(
                "ptolemy.domains.sdf.lib.vq.ImagePartition",
                inputOutputTypedIOPortClassChanges);

        // ImageUnpartition
        _actorsWithPortClassChanges.put(
                "ptolemy.domains.sdf.lib.vq.ImageUnpartition",
                inputOutputTypedIOPortClassChanges);

        // HTVQEncode
        _actorsWithPortClassChanges.put(
                "ptolemy.domains.sdf.lib.vq.HTVQEncode",
                inputOutputTypedIOPortClassChanges);

        // VQDecode
        _actorsWithPortClassChanges.put("ptolemy.domains.sdf.lib.vq.VQDecode",
                inputOutputTypedIOPortClassChanges);
    }
}
