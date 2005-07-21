package fedora.services.sipcreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import fedora.services.sipcreator.acceptor.SelectableEntryAcceptor;

/**
 * This class represents a selectable entry whose content and information
 * is stored on a local filesystem.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class FileSystemEntry extends SelectableEntry {

    /** The file represented by this object */
    private File file;
    
    /** The children of this object, or a zero element array if none */
    private File[] childrenFiles;
    
    /** The corresponding entry objects for the child files */
    private FileSystemEntry[] childrenEntries;
    
    /** The SIP creator, used to determine mime type */
    private SIPCreator creator;


    /**
     * This constructor requires that the file this object represents, its
     * parent, and the containing SIPCreator reference be known during
     * instantiation.  Only the parent may be null, and only if this is the
     * root entry in the system.
     * <br><br>
     * @param newFile The file this entry represents.
     * @param newParent The parent entry, or null if this is the root.
     * @param newCreator The SIPCreator reference, used to obtain mime type
     * information.
     */
    public FileSystemEntry(File newFile, FileSystemEntry newParent, SIPCreator newCreator) {
        creator = newCreator;
        
        file = newFile;
        try { setMimeType(creator.getMimeType(file)); }
        catch (Exception me) {}
        
        setParent(newParent);
        if (!file.isDirectory()) {
            childrenFiles = new File[0];
        } else {
            childrenFiles = file.listFiles();
        }
        childrenEntries = new FileSystemEntry[childrenFiles.length];
        
        Arrays.sort(childrenFiles, fileNameComparator);
        Arrays.sort(childrenFiles, directoryComparator);
        
        boolean parentSelected = newParent != null && newParent.getSelectionLevel() == FULLY_SELECTED;
        setSelectionLevel(parentSelected ? FULLY_SELECTED : UNSELECTED);
    }
    
    
    /**
     * Returns the file that this entry represents.
     * <br><br>
     * @return The file that this entry represents.
     */
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
    
    
    public void setSelectionLevel(int newSelectionLevel, SelectableEntryAcceptor acceptor) {
        setSelectionLevel(newSelectionLevel);
        
        for (int ctr = 0; ctr < childrenFiles.length; ctr++) {
            FileSystemEntry child = childrenEntries[ctr];
            if (child == null) continue;
            if (acceptor.isEntryAcceptable(child)) {
                child.setSelectionLevel(getSelectionLevel(), acceptor);
            } else {
                child.setSelectionLevel(UNSELECTED, acceptor);
            }
        }
    }
    
    
    public int getChildCount(SelectableEntryAcceptor acceptor) {
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

    public SelectableEntry getChildAt(int index, SelectableEntryAcceptor acceptor) {
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
    
    public int getIndex(SelectableEntry entry, SelectableEntryAcceptor acceptor) {
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
