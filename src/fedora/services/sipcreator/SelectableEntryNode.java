package fedora.services.sipcreator;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.tree.TreeNode;

import fedora.services.sipcreator.acceptor.SIPEntryAcceptor;

public class SelectableEntryNode implements TreeNode {
    
    private SelectableEntry entry;
    
    private SelectableEntryNode parent;
    
    private Hashtable childrenNodeTable = new Hashtable();
    
    private SIPEntryAcceptor acceptor;
    
    public SelectableEntryNode(SelectableEntry newEntry, SelectableEntryNode newParent, SIPEntryAcceptor newAcceptor) {
        entry = newEntry;
        parent = newParent;
        acceptor = newAcceptor;
    }
    
    public SelectableEntry getEntry() {
        return entry;
    }

    public int getChildCount() {
        return getEntry().getChildCount(acceptor);
    }

    public boolean getAllowsChildren() {
        return entry.isDirectory();
    }

    public boolean isLeaf() {
        return !entry.isDirectory();
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
        SelectableEntryNode casted = (SelectableEntryNode)node;
        return getEntry().getIndex(casted.getEntry(), acceptor);
    }

    public TreeNode getParent() {
        return parent;
    }

    public TreeNode getChildAt(int index) {
        SelectableEntry childEntry = getEntry().getChildAt(index, acceptor);
        SelectableEntryNode node = (SelectableEntryNode)childrenNodeTable.get(childEntry.getID());
        if (node == null) {
            node = new SelectableEntryNode(childEntry, this, acceptor);
            childrenNodeTable.put(childEntry.getID(), node);
        }
        return node;
    }

    public String toString() {
        return entry.toString();
    }
    
}
