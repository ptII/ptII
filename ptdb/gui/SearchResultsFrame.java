package ptdb.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import ptdb.common.dto.XMLDBModel;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SearchResultsFrame

/**
 * An extended JFrame displaying all search results in a scroll panel.  
 * Each search result is contained in a SearchResultPanel.  This is also an observer of 
 * the SearchResultsBuffer, where it can get search results on the fly by calling getResults().
 * The _cancelButton will notify the search classes that the search has been canceled.  
 * 
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

public class SearchResultsFrame extends JFrame implements Observer {

    /**
     * Construct a panel associated with a search result. 
     *
     * @param model
     *        The model into which search results would be imported.      
     * @param frame
     *        The frame from which this frame was opened.  It is here to allow repainting.
     * 
     * @param configuration
     *        The configuration under which an effigy of models would be generated.
     */
    public SearchResultsFrame(NamedObj model, JFrame frame,
            Configuration configuration) {

        _configuration = configuration;
        _cancelObservable = new CancelObservable();

        _scrollPane = new JScrollPane();
        add(_scrollPane);

        _resultPanelList = new ArrayList();

        _loadByRefButton = new JButton("Import By Reference");
        add(_loadByRefButton);

        _loadByValButton = new JButton("Import By Value");
        add(_loadByValButton);

        _cancelButton = new JButton("Cancel Search");
        add(_cancelButton);

        _loadByRefButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                //TODO

            }

        });

        _loadByValButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                //TODO

            }

        });

        _cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                _cancelObservable.notifyObservers();

            }

        });

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Display new search results in the scroll pane.
     * 
     * @param modelList
     *        The list of models that will be displayed as search results.
     */
    public void display(ArrayList<XMLDBModel> modelList) {

        for (XMLDBModel model : modelList) {

            SearchResultPanel newResultPanel = new SearchResultPanel(model,
                    _configuration);
            _resultPanelList.add(newResultPanel);
            _scrollPane.add(newResultPanel);

        }

        repaint();

    }

    /** Register an observer to allow notification upon canceling by the user.
     * 
     * @param buffer
     *        The observer.  Only added if it is an instance of SearchResultBuffer.
     */
    public void registerCancelObserver(Observer buffer) {

        if (buffer instanceof SearchResultBuffer) {

            _cancelObservable.addObserver(buffer);

        }

    }

    /** Implement the update method for Observer interface.
     *  Call display to display search results.
     * 
     * @param buffer
     *        The observer.  Only handled if it is an instance of SearchResultBuffer.
     * @param arg
     *        Option argument.  This is unused, but included by Java conventions.
     */
    public void update(Observable buffer, Object arg) {

        if (buffer instanceof SearchResultBuffer) {

            display(((SearchResultBuffer) buffer).getResults());

        }

    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private JScrollPane _scrollPane;
    private JButton _loadByRefButton;
    private JButton _loadByValButton;
    private JButton _cancelButton;
    private ArrayList<SearchResultPanel> _resultPanelList;
    private CancelObservable _cancelObservable;
    private Configuration _configuration;

}
