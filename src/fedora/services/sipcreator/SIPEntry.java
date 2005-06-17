package fedora.services.sipcreator;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import fedora.services.sipcreator.acceptor.SIPEntryAcceptor;

public class SIPEntry {

    private static final Comparator fileNameComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            File f1 = (File)o1;
            File f2 = (File)o2;
            
            return f1.getName().compareTo(f2.getName());
        }
    };
    
    private static final Comparator directoryComparator = new Comparator() {
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
    
    private Vector metadataList = new Vector();
    
    private String mimeType;
    
    private File file;
    
    private File[] childrenFiles;
    
    private SIPEntry[] childrenEntries;
    
    private SIPEntry parentEntry;
    
    private SIPCreator creator;
    
    private int selectionLevel;
    
    //public SIPEntry(File newFile, SIPEntry newParent, int newSelectionLevel) {
    public SIPEntry(File newFile, SIPEntry newParent, SIPCreator newCreator) {
        creator = newCreator;
        
        file = newFile;
        try {
            mimeType = creator.getMimeTypeTool().getMagicMatch(file).getMimeType();
        } catch (Exception me) {
            mimeType = "";
        }
        parentEntry = newParent;
        childrenFiles = file.listFiles();
        if (childrenFiles == null) {
            childrenFiles = new File[0];
        }
        childrenEntries = new SIPEntry[childrenFiles.length];
        
        Arrays.sort(childrenFiles, fileNameComparator);
        Arrays.sort(childrenFiles, directoryComparator);
        
        boolean parentSelected = newParent != null && newParent.getSelectionLevel() == FULLY_SELECTED;
        boolean acceptorApproved = creator.getFileSelectTask().getFilterAcceptor().isEntryAcceptable(this);
        selectionLevel = parentSelected && acceptorApproved ? FULLY_SELECTED : UNSELECTED;
    }
    
    
    public SIPEntry getParent() {
        return parentEntry;
    }
    
    public Vector getMetadata() {
        return metadataList;
    }
    
    public File getFile() {
        return file;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    
    public void setMimeType(String newMimeType) {
        mimeType = newMimeType;
    }
    
    
    public int getSelectionLevel() {
        return selectionLevel;
    }
    
    public void setSelectionLevel(int newSelectionLevel, SIPEntryAcceptor acceptor) {
        selectionLevel = newSelectionLevel;
        
        for (int ctr = 0; ctr < childrenFiles.length; ctr++) {
            SIPEntry child = childrenEntries[ctr];
            if (child == null) continue;
            if (acceptor.isEntryAcceptable(child)) {
                child.setSelectionLevel(selectionLevel, acceptor);
            } else {
                child.setSelectionLevel(UNSELECTED, acceptor);
            }
        }
    }
    
    public void setSelectionLevelFromChildren(SIPEntryAcceptor acceptor) {
        boolean unselected = false;
        boolean selected = false;
        boolean partially = false;
        
        for (int ctr = 0; ctr < getChildCount(acceptor); ctr++) {
            SIPEntry entry = getChildAt(ctr, acceptor);
            switch(entry.selectionLevel){
            case SIPEntry.UNSELECTED: unselected = true; break; 
            case SIPEntry.PARTIALLY_SELECTED: partially = true; break;
            case SIPEntry.FULLY_SELECTED: selected = true; break;
            }
        }
        
        if (unselected && !partially && !selected) {
            selectionLevel = SIPEntry.UNSELECTED;
        } else if (selected && !partially && !unselected) {
            selectionLevel = SIPEntry.FULLY_SELECTED;
        } else {
            selectionLevel = SIPEntry.PARTIALLY_SELECTED;
        }
        
        if (parentEntry != null) {
            parentEntry.setSelectionLevelFromChildren(acceptor);
        }
    }
    
    
    public int getChildCount(SIPEntryAcceptor acceptor) {
        int count = 0;
        
        for (int ctr = 0; ctr < childrenFiles.length; ctr++) {
            if (childrenEntries[ctr] == null) {
                childrenEntries[ctr] = new SIPEntry(childrenFiles[ctr], this, creator);
            }
            
            if (acceptor.isEntryAcceptable(childrenEntries[ctr])) {
                count++;
            }
        }
        
        return count;
    }

    public SIPEntry getChildAt(int index, SIPEntryAcceptor acceptor) {
        int count = 0;
        
        for (int ctr = 0; ctr < childrenFiles.length; ctr++) {
            if (childrenEntries[ctr] == null) {
                childrenEntries[ctr] = new SIPEntry(childrenFiles[ctr], this, creator);
            }
            
            if (acceptor.isEntryAcceptable(childrenEntries[ctr])) {
                if (count == index) {
                    return childrenEntries[ctr];
                }
                count++;
            }
        }
        
        return null;
    }
    
    public int getIndex(SIPEntry entry, SIPEntryAcceptor acceptor) {
        int count = 0;
        for (int ctr = 0; ctr < childrenFiles.length; ctr++) {
            if (childrenEntries[ctr] == null) {
                childrenEntries[ctr] = new SIPEntry(childrenFiles[ctr], this, creator);
            }
            
            if (acceptor.isEntryAcceptable(childrenEntries[ctr])) {
                if (childrenEntries[ctr] == entry) {
                    return count;
                }
                count++;
            }
        }
        return -1;
    }
        
}
