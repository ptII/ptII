/*
@Copyright (c) 2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.vergil.actor;

import diva.graph.GraphController;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.ActorController;
import ptolemy.vergil.toolbox.FigureAction;

/**
 * An interface for actor interaction
 * 
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */
public interface ActorInteractionAddon {

    /** Determine of a given actor is of interest for a desired action.
     * 
     * @param actor The actor of interest.
     * @return True if the actor is of interest for the "Look Inside" action for
     * actors, False otherwise.
     */
    abstract boolean isActorOfInterestForLookInside(NamedObj actor);
    
    /**
     * The action to be taken when looking inside an actor.
     * @param figureAction The FigureAction from which the call is being made.
     * @param actor The actor being opened.
     * @throws IllegalActionException Thrown for illegal action.
     * @throws NameDuplicationException Thrown for name duplication.
     */
    abstract void lookInsideAction(FigureAction figureAction, NamedObj actor) 
    throws IllegalActionException, NameDuplicationException;
    
    
    /** Determine of a given actor is of interest for a desired action.
     * 
     * @param actor The actor of interest.
     * @return True if the actor is of interest for the "Open Instance" action for
     * actors, False otherwise.
     */
    abstract boolean isActorOfInterestForOpenInstance(NamedObj actor);
    
    /**
     * The action to be taken when looking inside an actor.
     * @param figureAction The FigureAction from which the call is being made.
     * @param actor The actor being opened.
     * @throws IllegalActionException  Thrown for illegal action.
     * @throws NameDuplicationException  Thrown for name duplication.
     */
    abstract void openInstanceAction(FigureAction figureAction, NamedObj actor) 
        throws IllegalActionException, NameDuplicationException;
    
    /**
     * Get an instance of the controller for a given actor.  This assumes
     * Full access.
     * @param controller The associated graph controller.
     * @return An instance of the appropriate controller.
     */
    abstract ActorController getControllerInstance(GraphController controller);
    
    /**
     * Get an instance of the controller for a given actor.  This assumes
     * full access.
     * @param controller The associated graph controller.
     * @param fullAccess Indication if the controller should be instantiated
     *                  with Full access.
     * @return An instance of the appropriate controller.
     */
    abstract ActorController getControllerInstance(GraphController controller, boolean fullAccess);
    
    
    /** Determine of a given actor is of interest for a desired action.
     * 
     * @param actor The actor of interest.
     * @return True if the actor is of interest for use of a special controller,
     * False otherwise.
     */
    abstract boolean isActorOfInterestForAddonController(NamedObj actor);
    
}
