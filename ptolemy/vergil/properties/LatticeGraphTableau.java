package ptolemy.vergil.properties;

import java.awt.Color;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;


public class LatticeGraphTableau extends Tableau {
    
    /** Create a new FSM editor tableau with the specified container
     *  and name, with no default library.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the model associated with
     *   the container effigy is not an FSMActor.
     *  @exception NameDuplicationException If the container already
     *   contains an object with the specified name.
     */
    public LatticeGraphTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null);
    }

    /** Create a new FSM editor tableau with the specified container,
     *  name, and default library.
     *  @param container The container.
     *  @param name The name.
     *  @param defaultLibrary The default library, or null to not specify one.
     *  @exception IllegalActionException If the model associated with
     *   the container effigy is not an FSMActor.
     *  @exception NameDuplicationException If the container already
     *   contains an object with the specified name.
     */
    public LatticeGraphTableau(PtolemyEffigy container, String name,
            LibraryAttribute defaultLibrary) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();

        if (!model.getClassName().equals(
                "ptolemy.domains.properties.PropertyLatticeComposite")) {
            throw new IllegalActionException(this,
                    "Cannot edit a model that is not an FSMActor.");
        }

        createGraphFrame((FSMActor) model, defaultLibrary);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the graph frame that displays the model associated with
     *  this tableau. This method creates a FSMGraphFrame. If subclass
     *  uses another frame, this method should be overridden to create
     *  that frame.
     *  @param model The Ptolemy II model to display in the graph frame.
     */
    public void createGraphFrame(CompositeEntity model) {
        createGraphFrame(model, null);
    }

    /** Create the graph frame that displays the model associated with
     *  this tableau together with the specified library.
     *  This method creates a FSMGraphFrame. If a subclass
     *  uses another frame, this method should be overridden to create
     *  that frame.
     *  @param model The Ptolemy II model to display in the graph frame.
     *  @param defaultLibrary The default library, or null to not specify
     *   one.
     */
    public void createGraphFrame(CompositeEntity model,
            LibraryAttribute defaultLibrary) {
        LatticeGraphFrame frame = new LatticeGraphFrame(model, this, defaultLibrary);

        try {
            setFrame(frame);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }

        frame.setBackground(BACKGROUND_COLOR);
        frame.pack();
        frame.centerOnScreen();
        frame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                           public fields                   ////

    /** The default background color.  To change the background color,
     *  use a {@link ptolemy.actor.gui.PtolemyPreferences}.
     */
    public static final Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates graph editing tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create an factory with the given name and container.
         *  @param container The container.
         *  @param name The name of the entity.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create an instance of FSMGraphTableau for the specified effigy,
         *  if it is an effigy for an instance of FSMActor.
         *  @param effigy The effigy for an FSMActor.
         *  @return A new FSMGraphTableau, if the effigy is a PtolemyEffigy
         *   that references an FSMActor, or null otherwise.
         *  @exception Exception If an exception occurs when creating the
         *   tableau.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (!(effigy instanceof PtolemyEffigy)) {
                return null;
            }

            Tableau tableau = (Tableau) effigy.getEntity("latticeTableau");
            if (tableau != null) {
                return tableau;
            }
            
            NamedObj model = ((PtolemyEffigy) effigy).getModel();

            if (model.getClassName().equals(
                    "ptolemy.domains.properties.PropertyLatticeComposite")) {
                
                // Check to see whether this factory contains a
                // default library.
                LibraryAttribute library = (LibraryAttribute) getAttribute(
                        "_library", LibraryAttribute.class);

                tableau = new LatticeGraphTableau((PtolemyEffigy) effigy,
                        "latticeTableau", library);
                return tableau;
            } else {
                return null;
            }
        }
    }
}
