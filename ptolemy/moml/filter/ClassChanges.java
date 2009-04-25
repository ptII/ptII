/* Filter for simple class name changes

 Copyright (c) 2002-2009 The Regents of the University of California.
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
import java.util.HashSet;
import java.util.Iterator;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// ClassChanges

/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter so that models from
 earlier releases will run in the current release.

 <p>This class will filter moml for simple class changes where
 the context of the class name to be changed does not matter - all
 occurrences of the class name will be changed.  This class
 can be though of as a primitive form of sed.

 <p> If a class within an actor is what has changed, use (@see
 PropertyClassChanges) instead.

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ClassChanges implements MoMLFilter {
    /** Clear the map of class renames and the set of class removals.
     */
    public static void clear() {
        _classChanges = new HashMap();
        _classesToRemove = new HashSet();
    }

    /** If the attributeName is "class" and attributeValue names a
     *  class that needs to be renamed, then substitute in the new class
     *  name. If the attributeValue names a class that needs to be removed,
     *  then return null, which will cause the MoMLParser to skip the
     *  rest of the element;
     *
     *  @param container  The container for this attribute.
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

        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.
        if (attributeName.equals("class")) {
            if (_classChanges.containsKey(attributeValue)) {
                // We found a class with a class change.
                MoMLParser.setModified(true);
                return (String) _classChanges.get(attributeValue);
            } else if (_classesToRemove.contains(attributeValue)) {
                // We found a class to remove.
                return null;
            }
        }

        return attributeValue;
    }

    /** In this class, do nothing.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception Not thrown in this base class.
     */
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
    }

    /** Add a class to be filtered. Note that if you add a class with this
     *  method, then you must remove it with {@link #remove(String)},
     *  calling "new ClassChanges()" will not remove a class that was
     *  added with this method.
     *  @param oldName The old name of the class to be filtered.
     *  @param newName The new name of the class to be filtered. If
     *  the value is null, then the class in oldName will be removed.
     *  @see #remove(String)
     */
    public void put(String oldName, String newName) {
        if (newName == null) {
            _classesToRemove.add(oldName);
        } else {
            _classChanges.put(oldName, newName);
        }
    }

    /** Remove a class to be filtered.
     *  @param className The name of the class to be filtered
     *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
     *  @see #put(String, String)
     */
    public void remove(String className) {
        if (_classChanges.containsKey(className)) {
            _classChanges.remove(className);
        } else if (_classesToRemove.contains(className)) {
            _classesToRemove.remove(className);
        }
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": change any class names that have been "
                + "renamed and remove obsolete classes.\n"
                + "Below are original class names followed by "
                + "the new class names:\n");
        Iterator classNames = _classChanges.keySet().iterator();

        while (classNames.hasNext()) {
            String className = (String) classNames.next();
            results.append("\t" + className + "\t -> "
                    + _classChanges.get(className) + "\n");
        }

        results.append("\nBelow are the classes to remove:\n");

        Iterator classesToRemove = _classesToRemove.iterator();

        while (classesToRemove.hasNext()) {
            String className = (String) classesToRemove.next();
            results.append("\t" + className + "\n");
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Map of actor names a HashMap of property names to new classes.
    private static HashMap _classChanges;

    static {
        ///////////////////////////////////////////////////////////
        // Actors and attributes that have changed names.
        _classChanges = new HashMap();

        // Location
        _classChanges.put("ptolemy.moml.Location",
                "ptolemy.kernel.util.Location");

        // New in 2.1-devel-2
        _classChanges.put("ptolemy.kernel.util.VersionAttribute",
                "ptolemy.kernel.attributes.VersionAttribute");

        // New in 2.3-devel
        _classChanges.put("ptolemy.actor.lib.comm.SerialComm",
                "ptolemy.actor.lib.io.comm.SerialComm");

        // New in 3.1-devel
        _classChanges.put("ptolemy.domains.fsm.lib.RelationList",
                "ptolemy.domains.fsm.kernel.RelationList");

        // Renamed in 3.1-devel
        _classChanges.put("ptolemy.vergil.icon.ImageEditorIcon",
                "ptolemy.vergil.icon.ImageIcon");

        // Replaced FileAttribute with FileParameter in 3.2-devel
        _classChanges.put("ptolemy.kernel.attributes.FileAttribute",
                "ptolemy.data.expr.FileParameter");

        // SDFIOPort is obsolete as of 3.2-devel
        _classChanges.put("ptolemy.domains.sdf.kernel.SDFIOPort",
                "ptolemy.actor.TypedIOPort");

        // Moved MultiInstanceComposite
        _classChanges.put("ptolemy.actor.hoc.MultiInstanceComposite",
                "ptolemy.actor.lib.hoc.MultiInstanceComposite");

        // Moved ModalModel
        _classChanges.put("ptolemy.vergil.fsm.modal.ModalModel",
                "ptolemy.domains.fsm.modal.ModalModel");

        // Moved InterfaceAutomatonTransition
        _classChanges.put(
                "ptolemy.domains.fsm.kernel.InterfaceAutomatonTransition",
                "ptolemy.domains.fsm.kernel.ia.InterfaceAutomatonTransition");

        // Moved InterfaceAutomatonTransition
        _classChanges.put(
                "ptolemy.domains.fsm.kernel.InterfaceAutomaton",
                "ptolemy.domains.fsm.kernel.ia.InterfaceAutomaton");

        // Moved ModalTableauFactory
        _classChanges.put(
                "ptolemy.vergil.fsm.modal.ModalModel$ModalTableauFactory",
                "ptolemy.vergil.fsm.modal.ModalTableauFactory");

        // Moved ModalPort
        _classChanges.put("ptolemy.vergil.fsm.modal.ModalPort",
                "ptolemy.domains.fsm.modal.ModalPort");

        // Moved ModalController
        _classChanges.put("ptolemy.vergil.fsm.modal.ModalController",
                "ptolemy.domains.fsm.modal.ModalController");

        // Moved Refinement
        _classChanges.put("ptolemy.vergil.fsm.modal.Refinement",
                "ptolemy.domains.fsm.modal.Refinement");

        // Moved RefinementPort
        _classChanges.put("ptolemy.vergil.fsm.modal.RefinementPort",
                "ptolemy.domains.fsm.modal.RefinementPort");

        // Moved TransitionRefinement
        _classChanges.put("ptolemy.vergil.fsm.modal.TransitionRefinement",
                "ptolemy.domains.fsm.modal.TransitionRefinement");

        // Moved TransitionRefinementPort
        _classChanges.put("ptolemy.vergil.fsm.modal.TransitionRefinementPort",
                "ptolemy.domains.fsm.modal.TransitionRefinementPort");

        // Moved IDAttribute from ptolemy.kernel.attributes to
        // ptolemy.vergil.kernel.atttributes
        _classChanges.put("ptolemy.kernel.attributes.IDAttribute",
                "ptolemy.vergil.kernel.attributes.IDAttribute");

        _classChanges.put("ptolemy.domains.gr.lib.ViewScreen",
                "ptolemy.domains.gr.lib.ViewScreen3D");

        _classChanges.put("ptolemy.domains.sr.lib.Latch",
                "ptolemy.domains.sr.lib.Current");

        // Renamed VergilPreferences
        _classChanges.put("ptolemy.vergil.VergilPreferences",
                "ptolemy.actor.gui.PtolemyPreferences");

        // Use FPScheduler instead of SROptimizedScheduler
        _classChanges.put("ptolemy.domains.sr.kernel.SROptimizedScheduler",
                "ptolemy.actor.sched.FixedPointScheduler");

        // Moved HSFSMDirector
        _classChanges.put("ptolemy.domains.fsm.kernel.HSFSMDirector",
                "ptolemy.domains.ct.kernel.HSFSMDirector");

        // Moved ParameterSet
        _classChanges.put("ptolemy.data.expr.ParameterSet",
                "ptolemy.actor.parameters.ParameterSet");

        // ColtSeedParameter is obsolete
        _classChanges.put("ptolemy.actor.lib.colt.ColtSeedParameter",
                "ptolemy.moml.SharedParameter");

        // 1/08: Moved SingleTokenCommutator so that DDF can extend it
        _classChanges.put("ptolemy.domains.sr.lib.SingleTokenCommutator",
                "ptolemy.actor.lib.SingleTokenCommutator");

        // DependencyHighlighter is really a vergil class
        _classChanges.put("ptolemy.actor.gui.DependencyHighlighter",
                "ptolemy.vergil.basic.DependencyHighlighter");

        _classChanges.put("ptolemy.moml.SharedParameter",
                "ptolemy.actor.parameters.SharedParameter");

        _classChanges.put("ptolemy.actor.lib.gui.LEDMatrix",
                "ptolemy.vergil.actor.lib.LEDMatrix");

        _classChanges.put("ptolemy.actor.lib.gui.ModelDisplay",
                "ptolemy.vergil.actor.lib.ModelDisplay");

        _classChanges.put("ptolemy.domains.sr.lib.ButtonTime",
                          "ptolemy.domains.sr.lib.gui.ButtonTime");

        _classChanges.put("ptolemy.domains.sr.lib.NonStrictDisplay",
                          "ptolemy.domains.sr.lib.gui.NonStrictDisplay");


        // Look for Kepler's NamedObjId, and if we don't find it, then
        // add it to the filter.  This makes it much easier to open
        // Kepler models in Ptolemy.
        try {
            Class.forName("org.kepler.moml.NamedObjId");
        } catch (ClassNotFoundException ex) {
            _classChanges.put("org.kepler.moml.NamedObjId",
                    "ptolemy.kernel.util.StringAttribute");
        }

        _classChanges.put("ptolemy.data.unit.UnitAttribute",
                          "ptolemy.moml.unit.UnitAttribute");
    }

    // Set of class names that are obsolete and should be simply
    // removed.
    private static HashSet _classesToRemove;

    static {
        ////////////////////////////////////////////////////////////
        // Classes that are obsolete and should be removed.
        _classesToRemove = new HashSet();

        // NotEditableParameter
        _classesToRemove.add("ptolemy.data.expr.NotEditableParameter");

        //DEIOPort
        _classesToRemove.add("ptolemy.domains.de.kernel.DEIOPort");

        // SROptimizedScheduler
        _classesToRemove.add("ptolemy.domains.sr.kernel.SROptimizedScheduler");

        // SRRandomizedScheduler
        _classesToRemove.add("ptolemy.domains.sr.kernel.SRRandomizedScheduler");
    }
}
