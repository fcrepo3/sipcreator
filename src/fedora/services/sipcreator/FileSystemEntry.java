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
    
    private SIPCreator creator;

    
    public FileSystemEntry(File newFile, FileSystemEntry newParent, SIPCreator newCreator) {
        creator = newCreator;
        
        file = newFile;
        try { setMimeType(creator.getMimeTypeTool().getMimeType(file)); }
        catch (Exception me) {}
        
        parent = newParent;
        if (!file.isDirectory()) {
            childrenFiles = new File[0];
        } else {
            childrenFiles = file.listFiles();
        }
        childrenEntries = new FileSystemEntry[childrenFiles.length];
        
        Arrays.sort(childrenFiles, fileNameComparator);
        Arrays.sort(childrenFiles, directoryComparator);
        
        boolean parentSelected = newParent != null && newParent.getSelectionLevel() == FULLY_SELECTED;
        selectionLevel = parentSelected ? FULLY_SELECTED : UNSELECTED;
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
