package fedora.services.sipcreator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Observable;
import java.util.Vector;

import fedora.services.sipcreator.acceptor.SIPEntryAcceptor;
import fedora.services.sipcreator.metadata.Metadata;

public abstract class SelectableEntry extends Observable {

    protected static final Comparator fileNameComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            File f1 = (File)o1;
            File f2 = (File)o2;
            
            return f1.getName().compareTo(f2.getName());
        }
    };
    
    protected static final Comparator directoryComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            File f1 = (File)o1;
            File f2 = (File)o2;
            
            int val1 = f1.isDirectory() ? 1 : 0;
            int val2 = f2.isDirectory() ? 1 : 0;
            
            return val2 - val1;
        }
    };
    
    
    public static final int NONE = 0;
    
    public static final int ANY = -1;
    
    public static final int UNSELECTED = 1;
    
    public static final int PARTIALLY_SELECTED = 2;
    
    public static final int FULLY_SELECTED = 4;
    
    
    private final String id;
    
    protected SelectableEntry parent;
    
    protected Vector metadataList = new Vector();
    
    protected String mimeType = new String();
    
    protected int selectionLevel;
    
    
    public SelectableEntry() {
        this(Metadata.getNextID());
    }
    
    public SelectableEntry(String newID) {
        id = newID;
    }
    
    
    public String getID() {
        return id;
    }

    public int getMetadataCount() {
        return metadataList.size();
    }
    
    public Metadata getMetadata(int index) {
        return (Metadata)metadataList.get(index);
    }
    
    public int indexOfMetadata(Metadata metadata) {
        return metadataList.indexOf(metadata);
    }
    
    public void addMetadata(Metadata newMetadata) {
        metadataList.add(newMetadata);
        setChanged();
        notifyObservers();
    }
    
    public void removeMetadata(int index) {
        metadataList.remove(index);
        setChanged();
        notifyObservers();
    }
    
    public void removeMetadata(Metadata oldMetadata) {
        metadataList.remove(oldMetadata);
        setChanged();
        notifyObservers();
    }
    
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String newMimeType) {
        mimeType = newMimeType;
        setChanged();
        notifyObservers();
    }
    
    public String toString() {
        return getDescriptiveName();
    }
    
    public SelectableEntry getParent() {
        return parent;
    }
    
    public abstract boolean isDirectory();
    
    public abstract String getShortName();
    
    public abstract String getDescriptiveName();
    
    public abstract InputStream getStream() throws IOException;
        
    
    public int getSelectionLevel() {
        return selectionLevel;
    }

    public abstract void setSelectionLevel(int newSelectionLevel, SIPEntryAcceptor acceptor);
    
    public void setSelectionLevelFromChildren(SIPEntryAcceptor acceptor) {
        boolean unselected = false;
        boolean selected = false;
        boolean partially = false;
        
        for (int ctr = 0; ctr < getChildCount(acceptor); ctr++) {
            SelectableEntry entry = getChildAt(ctr, acceptor);
            switch(entry.getSelectionLevel()){
            case FileSystemEntry.UNSELECTED: unselected = true; break; 
            case FileSystemEntry.PARTIALLY_SELECTED: partially = true; break;
            case FileSystemEntry.FULLY_SELECTED: selected = true; break;
            }
        }
        
        if (unselected && !partially && !selected) {
            selectionLevel = SelectableEntry.UNSELECTED;
        } else if (selected && !partially && !unselected) {
            selectionLevel = SelectableEntry.FULLY_SELECTED;
        } else {
            selectionLevel = SelectableEntry.PARTIALLY_SELECTED;
        }
        
        if (parent != null) {
            parent.setSelectionLevelFromChildren(acceptor);
        }
    }
    

    public abstract int getChildCount(SIPEntryAcceptor acceptor);

    public abstract SelectableEntry getChildAt(int index, SIPEntryAcceptor acceptor);
    
    public abstract int getIndex(SelectableEntry entry, SIPEntryAcceptor acceptor);
        
}
