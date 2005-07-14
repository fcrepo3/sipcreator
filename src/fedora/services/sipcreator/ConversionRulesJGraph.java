package fedora.services.sipcreator;

import beowulf.gui.JGraph;
import beowulf.gui.JNode;
import beowulf.gui.JNodeFactory;

public class ConversionRulesJGraph extends JGraph {

    private static final long serialVersionUID = 5372087783013767449L;

    public ConversionRulesJGraph() {
        setJNodeFactory(new SelectableEntryGraphNodeFactory());
    }
    
    private class SelectableEntryGraphNodeFactory extends JNodeFactory {
        
        public JNode produceJNode(Object o) {
            return new SelectableEntryGraphNode((SelectableEntry)o);
        }
        
    }
    
    private class SelectableEntryGraphNode extends JNode {
        
        private static final long serialVersionUID = 6902636368917496444L;
        
        private SelectableEntry entry;
        
        public SelectableEntryGraphNode(SelectableEntry newEntry) {
            super(newEntry);
            entry = newEntry;
            setText(entry.getShortName());
        }
        
    }
    
}
