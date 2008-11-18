/* An entity that implements an explicit change context declares that it modifies variable values.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.actor.util;

import java.util.List;

import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// ExplicitChangeContext

/**
 An entity that implements an explicit change context declares a
 change context, in which parameters are modified.  If the
 ExplicitChangeContext is an Entity, then the change context is
 assumed to be the entity.  If the ExplicitChangeContext is not an
 entity, but directly contained by an entity, then that entity is
 considered to be the change context.  This capability allows
 directors to easily declare change contexts.  The information
 declared by this class is used by the ConstVariableModelAnalysis to
 determine what parameters might be modified by a given entity.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 @see ptolemy.actor.util.ConstVariableModelAnalysis
 */
public interface ExplicitChangeContext extends Nameable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list of variables that this entity modifies.  The
     *  variables are assumed to have a change context of the given
     *  entity.
     *  @return A list of variables.
     *  @exception IllegalActionException If the list of modified
     *  variables cannot be returned.
     */
    public List getModifiedVariables() throws IllegalActionException;

    /** Return the change context being made explicit.  In simple cases, this
     *  will simply be the entity implementing this interface.  However, in
     *  more complex cases, directors may implement this interface, or entities
     *  may modify parameters according to a different change context (i.e. HDF)
     *  @return The change context being made explicit.
     *  @exception IllegalActionException If the context is not available.
     */
    public Entity getContext() throws IllegalActionException;
}
