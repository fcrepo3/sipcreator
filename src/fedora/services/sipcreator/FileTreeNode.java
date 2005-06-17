package fedora.services.sipcreator;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.tree.TreeNode;

import fedora.services.sipcreator.acceptor.SIPEntryAcceptor;

public class FileTreeNode implements TreeNode {
    
    private SIPEntry entry;
    
    private FileTreeNode parent;
    
    private Hashtable childrenNodeTable = new Hashtable();
    
    private SIPEntryAcceptor acceptor;
    
    public FileTreeNode(SIPEntry newEntry, FileTreeNode newParent, SIPEntryAcceptor newAcceptor) {
        entry = newEntry;
        parent = newParent;
        acceptor = newAcceptor;
    }
    
    public SIPEntry getEntry() {
        return entry;
    }

    public int getChildCount() {
        return getEntry().getChildCount(acceptor);
    }

    public boolean getAllowsChildren() {
        return entry.getFile().isDirectory();
    }

    public boolean isLeaf() {
        return !entry.getFile().isDirectory();
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
        FileTreeNode casted = (FileTreeNode)node;
        return getEntry().getIndex(casted.getEntry(), acceptor);
    }

    public TreeNode getParent() {
        return parent;
    }

    public TreeNode getChildAt(int index) {
        SIPEntry childEntry = getEntry().getChildAt(index, acceptor);
        FileTreeNode node = (FileTreeNode)childrenNodeTable.get(childEntry.getFile());
        if (node == null) {
            node = new FileTreeNode(childEntry, this, acceptor);
            childrenNodeTable.put(childEntry.getFile(), node);
        }
        return node;
    }

    public String toString() {
        return entry.getFile().getName();
    }
    
}
