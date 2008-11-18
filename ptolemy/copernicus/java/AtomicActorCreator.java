/* An interface for classes that replaces port methods.

 Copyright (c) 2001-2007 The Regents of the University of California.
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
package ptolemy.copernicus.java;

import java.util.Map;

import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import soot.SootClass;

//////////////////////////////////////////////////////////////////////////
//// AtomicActorCreator

/**
 An interface for classes that replaces port methods.
 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public interface AtomicActorCreator {
    /** Generate a new class with the given name that can take the
     *  place of the given actor.  Use the given options when
     *  necessary.
     * @param entity the entity.
     * @param newClassName the name of the new class.
     * @param constAnalysis a ConstVariableModelAnalysis object
     * @param options the options
     * @return a SootClass object
     * @throws IllegalActionException 
     * @throws InvalidStateException 
     */
    public SootClass createAtomicActor(Entity entity, String newClassName,
            ConstVariableModelAnalysis constAnalysis, Map options) throws InvalidStateException, IllegalActionException;
}
