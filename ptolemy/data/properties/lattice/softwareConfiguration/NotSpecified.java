/** A base class representing a property.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.softwareConfiguration;

import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.PropertyLattice;


//////////////////////////////////////////////////////////////////////////
//// NotSpecified

/**
 A lattice element for the softwareConfiguration lattice.

 @author Thomas Mandl
 @version $Id: AtomicActor.java 55837 2009-10-13 23:09:28Z cxh $
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class NotSpecified extends LatticeProperty {
        
    /**
     * Create a new lattice property with the name "NotSpecified" and the specified
     * lattice.
     * @param lattice The specified lattice where this property resides.
     */
    public NotSpecified(PropertyLattice lattice) {
        super(lattice, "NotSpecified");
    }

    /**
     * Return false, indicating that this property is not an acceptable solution.
     * @return False, indicating that this property is not an acceptable solution.
     */
    public boolean isAcceptableSolution() {
        return false;
    }
}
