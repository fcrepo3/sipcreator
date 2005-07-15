package fedora.services.sipcreator;

import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import fedora.services.sipcreator.metadata.Metadata;

public class MetadataNode implements TreeNode, Observer {

    private static final Enumeration EMPTY_ENUMERATION = new Enumeration() {

        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            return null;
        }
        
    };

    
    private SelectableEntryNode parent;
    
    private Metadata metadata;
    
    private DefaultTreeModel model;
    
    public MetadataNode(SelectableEntryNode newParent, Metadata newMetadata, DefaultTreeModel newModel) {
        model = newModel;
        parent = newParent;
        metadata = newMetadata;
        metadata.addObserver(this);
    }
    
    
    public Metadata getMetadata() {
        return metadata;
    }
    
    
    public int getChildCount() {
        return 0;
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public Enumeration children() {
        return EMPTY_ENUMERATION;
    }

    public TreeNode getParent() {
        return parent;
    }

    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    public int getIndex(TreeNode node) {
        return -1;
    }

    public String toString() {
        return "(" + metadata.getID() + ") " + metadata.getShortName();
    }
    

    public void update(Observable o, Object arg) {
        model.nodeChanged(this);
    }
    
}
