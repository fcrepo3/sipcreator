package fedora.services.sipcreator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Vector;

import fedora.services.sipcreator.acceptor.SIPEntryAcceptor;
import fedora.services.sipcreator.metadata.Metadata;

public abstract class SelectableEntry {

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
    
    
    private String id;
    
    protected Vector metadataList = new Vector();
    
    protected String mimeType = new String();
    
    protected String label = new String();
    
    protected int selectionLevel;
    
    
    public SelectableEntry() {
        id = Metadata.getNextID();
    }
    
    public SelectableEntry(String newID) {
        id = newID;
    }
    
    
    public String getLabel() {
        return label;
    }
    
    public String getID() {
        return id;
    }
    
    public Vector getMetadata() {
        return metadataList;
    }
    
    public String getMimeType() {
        return mimeType;
    }

    public String toString() {
        return getDescriptiveName();
    }
    
    public abstract SelectableEntry getParent();
    
    public abstract boolean isDirectory();
    
    public abstract String getShortName();
    
    public abstract String getDescriptiveName();
    
    public abstract InputStream getStream() throws IOException;
    
    
    public void setMimeType(String newMimeType) {
        mimeType = newMimeType;
    }
    
    public void setLabel(String newLabel) {
        label = newLabel;
    }
    
    
    public int getSelectionLevel() {
        return selectionLevel;
    }

    public abstract void setSelectionLevel(int newSelectionLevel, SIPEntryAcceptor acceptor);
    
    public abstract void setSelectionLevelFromChildren(SIPEntryAcceptor acceptor);
    

    public abstract int getChildCount(SIPEntryAcceptor acceptor);

    public abstract SelectableEntry getChildAt(int index, SIPEntryAcceptor acceptor);
    
    public abstract int getIndex(SelectableEntry entry, SIPEntryAcceptor acceptor);
        
}
