package fedora.services.sipcreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import fedora.services.sipcreator.acceptor.SIPEntryAcceptor;

public class FileSystemEntry extends SelectableEntry {

    private File file;
    
    private File[] childrenFiles;
    
    private FileSystemEntry[] childrenEntries;
    
    private FileSystemEntry parent;
    
    private SIPCreator creator;
    
    //public SIPEntry(File newFile, SIPEntry newParent, int newSelectionLevel) {
    public FileSystemEntry(File newFile, FileSystemEntry newParent, SIPCreator newCreator) {
        creator = newCreator;
        
        file = newFile;
        try {
            setMimeType(creator.getMimeTypeTool().getMagicMatch(file).getMimeType());
        } catch (Exception me) {
            setMimeType("");
        }
        parent = newParent;
        childrenFiles = file.listFiles();
        if (childrenFiles == null) {
            childrenFiles = new File[0];
        }
        childrenEntries = new FileSystemEntry[childrenFiles.length];
        
        Arrays.sort(childrenFiles, fileNameComparator);
        Arrays.sort(childrenFiles, directoryComparator);
        
        boolean parentSelected = newParent != null && newParent.getSelectionLevel() == FULLY_SELECTED;
        selectionLevel = parentSelected ? FULLY_SELECTED : UNSELECTED;
    }
    
    
    public SelectableEntry getParent() {
        return parent;
    }
    
    public File getFile() {
        return file;
    }
    
    public boolean isDirectory() {
        return file.isDirectory();
    }
    
    public String getShortName() {
        return file.getName();
    }
 
    public String getDescriptiveName() {
        return file.getAbsolutePath();
    }
    
    public InputStream getStream() throws IOException {
        return new FileInputStream(file);
    }
    
    
    public void setSelectionLevel(int newSelectionLevel, SIPEntryAcceptor acceptor) {
        selectionLevel = newSelectionLevel;
        
        for (int ctr = 0; ctr < childrenFiles.length; ctr++) {
            FileSystemEntry child = childrenEntries[ctr];
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
            FileSystemEntry entry = (FileSystemEntry)getChildAt(ctr, acceptor);
            switch(entry.selectionLevel){
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
    
    
    public int getChildCount(SIPEntryAcceptor acceptor) {
        int count = 0;
        
        for (int ctr = 0; ctr < childrenFiles.length; ctr++) {
            if (childrenEntries[ctr] == null) {
                childrenEntries[ctr] = new FileSystemEntry(childrenFiles[ctr], this, creator);
            }
            
            if (acceptor.isEntryAcceptable(childrenEntries[ctr])) {
                count++;
            }
        }
        
        return count;
    }

    public SelectableEntry getChildAt(int index, SIPEntryAcceptor acceptor) {
        int count = 0;
        
        for (int ctr = 0; ctr < childrenFiles.length; ctr++) {
            if (childrenEntries[ctr] == null) {
                childrenEntries[ctr] = new FileSystemEntry(childrenFiles[ctr], this, creator);
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
    
    public int getIndex(SelectableEntry entry, SIPEntryAcceptor acceptor) {
        int count = 0;
        for (int ctr = 0; ctr < childrenFiles.length; ctr++) {
            if (childrenEntries[ctr] == null) {
                childrenEntries[ctr] = new FileSystemEntry(childrenFiles[ctr], this, creator);
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
