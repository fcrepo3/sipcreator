package fedora.services.sipcreator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Observable;
import java.util.Vector;

import fedora.services.sipcreator.acceptor.SelectableEntryAcceptor;
import fedora.services.sipcreator.metadata.Metadata;

/**
 * This class defines the main data model object for much of what goes one in
 * the SIPCreator system.  A SelectableEntry is roughly equivalent to a file,
 * although there is not strict constraint that this be the case.  An entry
 * may contain other entries, as well as its own metadata objects.  Entry
 * objects have their own unique ID, and the uniqueness constraint is
 * guaranteed across the union of all SelectableEntry and Metadata objects.
 * <br>
 * Apart from possibly having a set of child entry objects, each
 * SelectableEntry also has a mime type, a parent (null only if it is the root
 * object), and a selection level.  The selection level is really the key
 * reason for implementing this class.  The semantics of what it means to be
 * selected are left up to the context in which this class is used.  The
 * definition of PARTIALLY_SELECTED is that there exists some node underneath
 * the current node which is FULLY_SELECTED, and there exists some node
 * underneath the current node which is UNSELECTED.
 * <br>
 * The selection status values are not guaranteed to be bitwise disjoint, so
 * the text <code>boolean selected = value & SELECTED != 0;</code> could
 * generate incorrect results.  The correct way to test is <code>boolean
 * selected = value & SELECTED == SELECTED;</code>.  To test to see if
 * a value has no selection status, use <code>boolean noStatus = value
 * == NONE;</code>.  To see if a value has any selection status, use
 * <code>boolean someStatus = value & ANY != 0;</code>. 
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public abstract class SelectableEntry extends Observable {

    /** This comparator is used to sort by name */
    protected static final Comparator fileNameComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            File f1 = (File)o1;
            File f2 = (File)o2;
            
            return f1.getName().compareTo(f2.getName());
        }
    };
    
    /** This comparator is used to sort by directory status */
    protected static final Comparator directoryComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            File f1 = (File)o1;
            File f2 = (File)o2;
            
            int val1 = f1.isDirectory() ? 1 : 0;
            int val2 = f2.isDirectory() ? 1 : 0;
            
            return val2 - val1;
        }
    };
    
    
    /** Value indicating no, or undefined, selection status */
    public static final int NONE = 0;
    
    /** Value used in testing for any selection status */
    public static final int ANY = 0xFFFFFFFF;
    
    /** Value used to indicate a resource is completely unselected */
    public static final int UNSELECTED = 1;
    
    /** Value used to indicate a resource is partially selected */
    public static final int PARTIALLY_SELECTED = 2;
    
    /** Value used to indicate a resource is completely selected */
    public static final int FULLY_SELECTED = 4;
    
    
    /** This field holds the id of the entry */
    private final String id;
    
    /** This field holds the parent entry */
    private SelectableEntry parent;
    
    /** This field holds the list of Metadata objects about this entry */
    private Vector metadataList = new Vector();
    
    /** This field holds the mime type of this entry */
    private String mimeType = new String();
    
    /** This field holds the selection status of this entry */
    private int selectionLevel;
    
    
    /**
     * The default constructor produces an Entry object whose id is the
     * result of calling <code>Metadata.getNextID()</code>
     */
    public SelectableEntry() {
        id = Metadata.getNextID();
    }
    
    
    /**
     * Returns the ID of this entry.
     * <br><br>
     * @return The ID of this entry.
     */
    public String getID() {
        return id;
    }

    /**
     * Returns the number of metadata objects associated with this entry.
     * <br><br>
     * @return The number of metadata objects associated with this entry.
     */
    public int getMetadataCount() {
        return metadataList.size();
    }

    /**
     * Returns the index-th metadata associated with this entry.
     * <br><br>
     * @param index The index of the desired Metadata object.
     * @return The index-th metadata associated with this entry.
     */
    public Metadata getMetadata(int index) {
        return (Metadata)metadataList.get(index);
    }
    
    /**
     * Returns the index of the given piece of metadata, or -1 if that
     * metadata does not describe this SelectableEntry.
     * <br><br>
     * @param metadata The metadata whose index is desired.
     * @return The index of the given metadata.
     */
    public int indexOfMetadata(Metadata metadata) {
        return metadataList.indexOf(metadata);
    }
    
    /**
     * This adds a new piece of metadata to this entry.  This method will
     * also set the metadata's entry to be this object.
     * <br><br>
     * @param newMetadata The metadata to add.
     */
    public void addMetadata(Metadata newMetadata) {
        metadataList.add(newMetadata);
        newMetadata.setEntry(this);
        setChanged();
        notifyObservers();
    }

    /**
     * This method will remove the index-th metadata entry.  Note that this
     * method could result in changes for indices of the other metadata
     * entrys.  The removed metadata will have its entry set to null.
     * <br><br>
     * @param index The index of the element to remove.
     */
    public void removeMetadata(int index) {
        Metadata metadata = (Metadata)metadataList.remove(index);
        metadata.setEntry(null);
        setChanged();
        notifyObservers();
    }

    /**
     * This method will remove the indicated metadata.  Note that this could
     * result in changes for the indices of other metadata entrys.  The removed
     * metadata will have its entry set to null.
     * <br><br>
     * @param oldMetadata The metadata to remove.
     */
    public void removeMetadata(Metadata oldMetadata) {
        removeMetadata(metadataList.indexOf(oldMetadata));
    }
    
    /**
     * Returns the mime type of this entry. 
     * <br><br>
     * @return The mime type of this entry.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the mime type of this entry.
     * <br><br>
     * @param newMimeType The new mime type of this entry.
     */
    public void setMimeType(String newMimeType) {
        mimeType = newMimeType;
        setChanged();
        notifyObservers();
    }
    
    /**
     * Returns the parent entry of this entry.
     * <br><br>
     * @return The parent entry of this entry.
     */
    public SelectableEntry getParent() {
        return parent;
    }
    
    /**
     * Sets the parent for this entry.
     * <br><br>
     * @param newParent The new parent for this entry.
     */
    public void setParent(SelectableEntry newParent) {
        parent = newParent;
    }
    
    /**
     * This value should be true if this entry has the ability to be a parent
     * for other SelectableEntry objects.  This does not necessarily mean that
     * this entry currently has children.
     * <br><br>
     * @return True if and only if this entry can hold other SelectableEntry
     * objects
     */
    public abstract boolean isDirectory();
    
    /**
     * Returns a short descriptive name about this entry.  No uniqueness
     * guarantees exist on this name, although it is recommended that the
     * name be unique for this entry amongst its sibling entries.
     * <br><br>
     * @return A short descriptive name about this entry.
     */
    public abstract String getShortName();
    
    /**
     * Returns a descriptive and unique name about this entry.  The name
     * should be unique across all of the SelectableEntry objects loaded
     * into the SIPCreator system at this time.
     * <br><br>
     * @return Returns a descriptive and unique name about this entry.
     */
    public abstract String getDescriptiveName();
    
    /**
     * This should return the InputStream object containing the content
     * associated with this SelectableEntry.
     * <br><br>
     * @return The InputStream containing this entry's content.
     * @throws IOException If there is a problem in the underlying data source.
     */
    public abstract InputStream getStream() throws IOException;
        
    
    public String toString() {
        return getDescriptiveName();
    }
    
    
    /**
     * Returns the selection status of this entry.
     * <br><br>
     * @return The selection status of this entry.
     */
    public int getSelectionLevel() {
        return selectionLevel;
    }

    /**
     * Sets the selection status for this entry.  Note that this does not
     * update this node's ancestor's selection status.  For that, use
     * <code>setSelectionLevel(int, SelectableEntryAcceptor)</code>
     * <br><br>
     * @param newSelectionLevel The new selection status of this entry.
     */
    public void setSelectionLevel(int newSelectionLevel) {
        selectionLevel = newSelectionLevel;
    }
    
    /**
     * This method will set the selection level for this entry, and propogate
     * that status down to its children.  The only valid values for the new
     * selection level are <code>SelectableEntry.FULLY_SELECTED</code> and
     * <code>SelectableEntry.UNSELECTED</code>.  It is meaningless to propogate
     * the PARTIALLY_SELECTED value down to children.  The acceptor paramter
     * defines which children the values are propogated to.
     * <br><br>
     * @param newSelectionLevel The new selection level.  Must be either
     * FULLY_SELECTED or UNSELECTED.
     * @param acceptor The filter defining the set of descendants to which
     * the value is propogated.
     */
    public abstract void setSelectionLevel(int newSelectionLevel, SelectableEntryAcceptor acceptor);
    
    /**
     * This method infers the selection level for this node based on the 
     * selection status of its children.  If this node has no children, the
     * selection status remains unchanged.  In the even that a change was
     * made to this node's selection status and it has a parent, then the
     * parent's selection status is also reinferred from its children.  The
     * acceptor object passed as a parameter is used to indicate the children
     * from which to infer selection status.  Only children accepted by the
     * acceptor will be used.
     * <br><br>
     * @param acceptor Used to determine which children to use to determine
     * selection status.
     */
    public void setSelectionLevelFromChildren(SelectableEntryAcceptor acceptor) {
        boolean unselected = false;
        boolean selected = false;
        boolean partially = false;
        boolean changed = false;
        
        for (int ctr = 0; ctr < getChildCount(acceptor); ctr++) {
            SelectableEntry child = getChildAt(ctr, acceptor);
            switch(child.getSelectionLevel()){
            case UNSELECTED: unselected = true; break; 
            case PARTIALLY_SELECTED: partially = true; break;
            case FULLY_SELECTED: selected = true; break;
            }
        }
        
        if (unselected && !partially && !selected) {
            changed = selectionLevel != UNSELECTED;
            selectionLevel = UNSELECTED;
        } else if (selected && !partially && !unselected) {
            changed = selectionLevel != FULLY_SELECTED;
            selectionLevel = FULLY_SELECTED;
        } else {
            changed = selectionLevel != PARTIALLY_SELECTED;
            selectionLevel = PARTIALLY_SELECTED;
        }
        
        if (changed && parent != null) {
            parent.setSelectionLevelFromChildren(acceptor);
        }
    }
    

    /**
     * This method returns the number of immediate children of this entry
     * that are accepted by the given acceptor.
     * <br><br>
     * @param acceptor The filter defining the valid child nodes.
     * @return The number of immediate children of this node which are
     * accepted by the given filter.
     */
    public abstract int getChildCount(SelectableEntryAcceptor acceptor);

    /**
     * This method returns the index-th child of this entry, counting according
     * to the given acceptor.  For instance if this node had 6 children, and
     * children 1, 3, and 5 (numbering starting from 0) were accepted by a
     * given filter, then calling <code>getChildAt(2, acceptor)</code> would
     * return node 3.
     * <br><br>
     * @param index The index of the child to be returned.
     * @param acceptor The acceptor defining which children are valid.
     * @return The index-th accepted child.
     */
    public abstract SelectableEntry getChildAt(int index, SelectableEntryAcceptor acceptor);
    
    /**
     * This method returns the index of a given child, for a given filter. See
     * <code>SelectableEntry#getChildAt(int, SelectableEntryAcceptor)</code>
     * for an example of how to count children with a filter.
     * <br><br>
     * @param entry The child entry whose index is desired.
     * @param acceptor The filter defining the set of valid children.
     * @return The index of the given child under the acceptor's criterion.
     */
    public abstract int getIndex(SelectableEntry entry, SelectableEntryAcceptor acceptor);
        
}
