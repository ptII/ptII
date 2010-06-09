package ptdb.gui;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ptdb.common.dto.XMLDBModel;
import ptolemy.actor.gui.Configuration;

///////////////////////////////////////////////////////////////////
//// ParentHierarchyPanel

/**
 * An extended JPanel displaying a single branch of parents for the search result. 
 * A parent hierarchy is one branch of parents that contain the model matched by the model search. 
 * The panel layout is taken care of in the constructor.  A public method getSelections()
 * is available for getting the list of parent model names for models that have been selected for
 * loading.
 * 
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

public class ParentHierarchyPanel extends JPanel {

    /**
     * Construct a panel associated with a parent hierarchy. 
     *
     * @param hierarchy
     *        The model returned as a search result.
     * @param searchResultModel
     *        The name of the model that was matched by the search.  
     *        Displayed at the end of the hierarchy.        
     * @param configuration
     *        The configuration under which an effigy of this model would be generated.
     */
    public ParentHierarchyPanel(ArrayList<XMLDBModel> hierarchy,
            String searchResultModel, Configuration configuration) {

        setLayout(new FlowLayout());

        _configuration = configuration;
        _parentList = new ArrayList();

        for (XMLDBModel parent : hierarchy) {

            _parentList.add(new ParentPanel(parent.getModelName(),
                    _configuration));

        }

        ListIterator listIterator = _parentList.listIterator();

        while (listIterator.hasNext()) {

            add((ParentPanel) listIterator.next());

        }

        JLabel labelResultModel = new JLabel(searchResultModel);
        add(labelResultModel);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Traverse the list of ParentPanels.  If a ParentPanel's checkbox is
     * checked, add the model name to the returned list of model names (strings).
     * 
     * @return ArrayList<String> 
     *         A list of parent models that have been selected for loading.
     */
    public ArrayList<String> getSelections() {

        ArrayList<String> returnList = new ArrayList();

        for (ParentPanel panel : _parentList) {

            if (panel.getValue()) {

                returnList.add(panel.getParentModelName());

            }

        }

        return returnList;
    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private ArrayList<ParentPanel> _parentList;
    private Configuration _configuration;

}
