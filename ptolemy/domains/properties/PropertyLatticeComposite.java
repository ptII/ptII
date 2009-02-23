package ptolemy.domains.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

public class PropertyLatticeComposite extends FSMActor {

    public PropertyLatticeComposite(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public PropertyLatticeComposite(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return true if the contained elements form a lattice;
     * false, otherwise.
     */
    public boolean isLattice() {
        List<LatticeElement> elements = (List<LatticeElement>) deepEntityList();
        
        _clearHighlightColor(elements);
        
        PropertyLattice lattice = new Lattice(elements);
        
        
        if ((lattice.top() == null)) {            
            MessageHandler.error("Cannot find an unique top element.");
            return false;
        } else {
            NamedObj top = (NamedObj) lattice.top();
            _debug("Top is: " + top.getName());            
        }
        
        if ((lattice.bottom() == null)) {
            MessageHandler.error("Cannot find an unique bottom element.");
            return false;
        } else {
            NamedObj bottom = (NamedObj) lattice.bottom();
            _debug("Bottom is: " + bottom.getName());            
        }

        // This is the same check done in ptolemy.graph.DirectedAcyclicGraph.
        for (int i = 0; i < (elements.size() - 1); i++) {
            for (int j = i + 1; j < elements.size(); j++) {
                NamedObj lub = (NamedObj) lattice.leastUpperBound(elements.get(i), elements.get(j)); 

                if (lub == null) {
                    // FIXME: add highlight color?
                    
                    // The offending nodes.
                    MessageHandler.error("\"" + elements.get(i).getName()
                            + "\" and \"" + elements.get(j).getName() + "\""
                            + " does not have an unique least upper bound (LUB).");                
                    
                    return false;
                } else {
                    _debug("LUB(" + elements.get(i).getName()
                            + ", " + elements.get(j).getName() + "): "
                            + lub.getName());                    
                }
                
                
            }
        }
        return true;
    }

    private void _clearHighlightColor(List<LatticeElement> elements) {
        // TODO Auto-generated method stub
        
    }

    public static class Lattice extends PropertyLattice {
        private List<LatticeProperty> _properties;
        
        public Lattice(List<LatticeElement> elements) {
            _properties = new ArrayList();
            
            HashMap map = new HashMap();

            // First add the property nodes to the lattice.
            for (LatticeElement element : elements) {
                LatticeProperty property = 
                    new LatticeProperty(this, element.getName());
                
                _properties.add(property);
                
                addNodeWeight(property); 
                map.put(element, property);
            }

            // Create edges to connect the nodes.
            for (LatticeElement element : elements) {
                // for each outgoing edge.
                for (Port port : (List<Port>) element.outgoingPort.connectedPortList()) {                
                    addEdge(map.get(element), map.get(port.getContainer()));
                }
            }
        }
        
        public Property getElement(String name) 
        throws IllegalActionException {
            for (LatticeProperty property : _properties) {
                if (name.equalsIgnoreCase(property.toString())) {
                    return property;
                }
            }
            return null;
        }

        
    }
}
