/* The abstract base class for the unitSystem ontology base and derived unit
 * concepts.

 Copyright (c) 1998-2011 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.unit;

import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.ontologies.FlatTokenInfiniteConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// UnitConcept

/** The abstract base class for the unitSystem ontology base and derived unit
 *  concepts.

@author Charles Shelton
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
*/
public abstract class UnitConcept extends FlatTokenInfiniteConcept {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    
    /** The name label for the unit record token information when constructing
     *  a new BaseUnitConcept.
     */
    public static final String unitNameLabel = "Name";
    
    /** The factor label for the unit record token information when constructing
     *  a new BaseUnitConcept.
     */
    public static final String unitFactorLabel = "Factor";
    
    /** The offset label for the unit record token information when constructing
     *  a new BaseUnitConcept.
     */
    public static final String unitOffsetLabel = "Offset";
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return true if this unit can be converted to the specified unit. 
     *  @param unit The other unit concept to compare to this one.
     *  @return true if the units can be converted, false otherwise.
     */
    public boolean canBeConvertedTo(UnitConcept unit) {
        if (getRepresentative().equals(unit.getRepresentative())) {
            return true;
        } else {
            return false;
        }
    }
    
    /** Return the the dimension concept for this unit concept.
     *  @return The dimension concept to which this unit concept belongs.
     */
    public DimensionRepresentativeConcept getDimension() {
        return (DimensionRepresentativeConcept) _representative;
    }
    
    /** Return the multiplication factor that converts a value in this unit to the
     *  SI unit for this dimension.
     *  @return The unit factor as a double value.
     */
    public double getUnitFactor() {
        return _unitFactor;
    }
    
    /** Return the name of the unit.
     *  @return The name of the unit.
     */
    public String getUnitName() {
        return _unitName;
    }
    
    /** Return the offset factor that converts a value in this unit to the SI
     *  unit for this dimension. Currently this is only used for temperature
     *  unit conversions.
     *  @return The unit offset as a double value.
     */
    public double getUnitOffset() {
        return _unitOffset;
    }
    
    /** Return the string representation of this base unit concept.
     *  It concatenates the name of the representative concept physical
     *  dimension name with the name of the unit.
     *  
     *  @return The string representation of this concept.
     */
    public String toString() {
        return _representative.getName() + "_" + _unitName;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    protected constructors                 ////
    
    /** Create a new BaseUnitConcept, belonging to the given
     *  ontology.
     * 
     *  @param ontology The ontology to which this concept belongs.
     *  @param representative The finite concept that represents the physical
     *   dimension for the set infinite concepts that represent units for
     *   this dimension in the ontology lattice.
     *  @param unitInfo The record token value that has the name and scale
     *   factor information for this unit.
     *  @throws NameDuplicationException Should never be thrown.
     *  @throws IllegalActionException If the base class throws it.
     */
    protected UnitConcept(Ontology ontology,
            DimensionRepresentativeConcept representative,
            RecordToken unitInfo) 
                throws IllegalActionException, NameDuplicationException {
        super(ontology, representative, unitInfo);
        
        Token unitName = unitInfo.get(unitNameLabel);            
        if (unitName instanceof StringToken) {
            _unitName = ((StringToken) unitName).stringValue();
        } else {
            throw new IllegalActionException(this,
                    "Invalid unit name token (must be a String token): " +
                    unitName);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** The multiplication factor for converting this unit to the SI
     *  unit for this physical dimension.
     */
    protected double _unitFactor;
    
    /** The offset factor for converting this unit to the SI
     *  unit for this physical dimension.
     */
    protected double _unitOffset;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The name of the unit represented by this unit concept. */
    private String _unitName;
}
