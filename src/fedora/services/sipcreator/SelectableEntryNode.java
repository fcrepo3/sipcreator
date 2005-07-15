package fedora.services.sipcreator;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import fedora.services.sipcreator.acceptor.SIPEntryAcceptor;
import fedora.services.sipcreator.metadata.Metadata;

public class SelectableEntryNode implements TreeNode, Observer {
    
    private SelectableEntry entry;
    
    private SelectableEntryNode parent;
    
    private Hashtable childrenNodeTable = new Hashtable();
    
    private Hashtable metadataNodeTable = new Hashtable();
    
    private SIPEntryAcceptor acceptor;
    
    private DefaultTreeModel model;
    
    public SelectableEntryNode(SelectableEntry newEntry, SelectableEntryNode newParent, SIPEntryAcceptor newAcceptor, DefaultTreeModel newModel) {
        model = newModel;
        entry = newEntry;
        entry.addObserver(this);
        parent = newParent;
        acceptor = newAcceptor;
    }
    
    public SelectableEntry getEntry() {
        return entry;
    }

    public int getChildCount() {
        if (!acceptor.acceptsMetadata()) {
            return entry.getChildCount(acceptor);
        }
        
//        if (entry.isDirectory()) {
//            return 1 + entry.getChildCount(acceptor);
//        }
        
        return entry.getMetadataCount() + entry.getChildCount(acceptor);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    public Enumeration children() {
        return new Enumeration() {

            private int ctr = 0;
            
            public boolean hasMoreElements() {
                return ctr < getChildCount();
            }

            public Object nextElement() {
                return getChildAt(ctr++);
            }
            
        };
    }

    public int getIndex(TreeNode node) {
        if (acceptor.acceptsMetadata() && node instanceof SelectableEntryNode) {
            SelectableEntryNode casted = (SelectableEntryNode)node;
            return entry.getIndex(casted.getEntry(), acceptor) - entry.getMetadataCount();
        } else if (acceptor.acceptsMetadata()) {
            MetadataNode metadataNode  = (MetadataNode)node;
            return entry.indexOfMetadata(metadataNode.getMetadata());
        } else {
            SelectableEntryNode casted = (SelectableEntryNode)node;
            return entry.getIndex(casted.getEntry(), acceptor);
        }
    }

    public TreeNode getParent() {
        return parent;
    }

    public TreeNode getChildAt(int index) {
        if (acceptor.acceptsMetadata() && index >= entry.getMetadataCount()) {
            SelectableEntry childEntry = getEntry().getChildAt(index - entry.getMetadataCount(), acceptor);
            SelectableEntryNode node = (SelectableEntryNode)childrenNodeTable.get(childEntry.getID());
            if (node == null) {
                node = new SelectableEntryNode(childEntry, this, acceptor, model);
                childrenNodeTable.put(childEntry.getID(), node);
            }
            return node;
        } else if (acceptor.acceptsMetadata()) {
            Metadata metadata = entry.getMetadata(index);
            MetadataNode node = (MetadataNode)metadataNodeTable.get(metadata.getID());
            if (node == null) {
                node = new MetadataNode(this, metadata, model);
                metadataNodeTable.put(metadata.getID(), node);
            }
            return node;
        } else {
            SelectableEntry childEntry = getEntry().getChildAt(index, acceptor);
            SelectableEntryNode node = (SelectableEntryNode)childrenNodeTable.get(childEntry.getID());
            if (node == null) {
                node = new SelectableEntryNode(childEntry, this, acceptor, model);
                childrenNodeTable.put(childEntry.getID(), node);
            }
            return node;
        }
    }

    public String toString() {
        return entry.getShortName();
    }

    public void update(Observable o, Object arg) {
        model.nodeStructureChanged(this);
    }
    
}
