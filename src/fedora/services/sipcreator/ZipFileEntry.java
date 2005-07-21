package fedora.services.sipcreator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fedora.services.sipcreator.acceptor.SelectableEntryAcceptor;

/**
 * This class allows us to read SelectableEntry objects out of a zip file.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class ZipFileEntry extends SelectableEntry {

    /** The zip file from which this entry was read */
    private ZipFile zipFile;
    
    /** The entry in the given zip file */
    private ZipEntry entry;
    
    /** This is the full path and filename of the zip entry */
    private String name;
    
    /** True if this zip entry represents a directory */
    private boolean isDirectory;
    
    /** A list of ZipFileEntry children */
    private Vector childList = new Vector();
    
    /**
     * A hashtable going from a name to a child.  For instance, if this entry
     * was the directory identified by "songs/rock/" then it's name would be
     * "songs/rock" and the ZipFileEntry objects known as "songs/rock/test.mp3"
     * and "songs/rock/Final.mp3" would be inserted into this hashtable with
     * the keys "test.mp3" and "Final.mp3" respectively.
     */
    private Hashtable childTable = new Hashtable();
    
    /**
     * This constructor requires that the zip file and zip entry objects be
     * known upon instantiating a ZipFileEntry.  The parent is also required
     * although it may be null.
     * <br><br>
     * @param newFile The ZipFile from which this entry was read.
     * @param newEntry The ZipEntry in the ZipFile that this object represents.
     * @param newParent This ZipFileEntry's parent, or null if it is the root.
     */
    public ZipFileEntry(ZipFile newFile, ZipEntry newEntry, ZipFileEntry newParent) {
        zipFile = newFile;
        entry = newEntry;
        setParent(newParent);
        name = entry.getName().replaceAll("\\\\", "/");
        isDirectory = name.endsWith("/");
        if (isDirectory) {
            name = name.substring(0, name.length() - 1);
        }
    }
    
    
    /**
     * This method adds a child to the current entry.
     * <br><br>
     * @param child The child to be added.
     */
    public void addChild(ZipFileEntry child) {
        childList.add(child);
        childTable.put(child.getShortName(), child);
    }
    
    /**
     * Given a name, return the child associated with that name. If this entry
     * was the directory identified by "songs/rock/" and the ZipFileEntry
     * objects known as "songs/rock/test.mp3" and "songs/rock/Final.mp3" would
     * be retrievable by using the keys "test.mp3" and "Final.mp3" respectively.
     * <br><br>
     * @param name The key to be used in retrieving the child node.
     * @return The child node, or null if it isn't found.
     */
    public ZipFileEntry getChild(String name) {
        return (ZipFileEntry)childTable.get(name);
    }
    
    /**
     * Returns the source zip file from which this entry was created.
     * <br><br>
     * @return The source zip file from which this entry was created.
     */
    public ZipFile getSourceFile() {
        return zipFile;
    }
    
    
    public boolean isDirectory() {
        return isDirectory;
    }

    public String getShortName() {
        if (name.lastIndexOf('/') == -1) {
            return name;
        }
        return name.substring(name.lastIndexOf('/') + 1);
    }

    public String getDescriptiveName() {
        return name;
    }

    public InputStream getStream() throws IOException {
        return zipFile.getInputStream(entry);
    }

    
    public void setSelectionLevel(int newSelectionLevel, SelectableEntryAcceptor acceptor) {
        setSelectionLevel(newSelectionLevel);
        
        for (int ctr = 0; ctr < childList.size(); ctr++) {
            ZipFileEntry child = (ZipFileEntry)childList.get(ctr);
            if (acceptor.isEntryAcceptable(child)) {
                child.setSelectionLevel(getSelectionLevel(), acceptor);
            } else {
                child.setSelectionLevel(UNSELECTED, acceptor);
            }
        }
    }

    
    public int getChildCount(SelectableEntryAcceptor acceptor) {
        int count = 0;
        
        for (int ctr = 0; ctr < childList.size(); ctr++) {
            if (acceptor.isEntryAcceptable((ZipFileEntry)childList.get(ctr))) {
                count++;
            }
        }
        
        return count;
    }

    public SelectableEntry getChildAt(int index, SelectableEntryAcceptor acceptor) {
        int count = 0;
        
        for (int ctr = 0; ctr < childList.size(); ctr++) {
            if (acceptor.isEntryAcceptable((ZipFileEntry)childList.get(ctr))) {
                if (count == index) {
                    return (ZipFileEntry)childList.get(ctr);
                }
                count++;
            }
        }
        
        return null;
    }

    public int getIndex(SelectableEntry entry, SelectableEntryAcceptor acceptor) {
        int count = 0;
        for (int ctr = 0; ctr < childList.size(); ctr++) {
            if (acceptor.isEntryAcceptable((ZipFileEntry)childList.get(ctr))) {
                if (childList.get(ctr) == entry) {
                    return count;
                }
                count++;
            }
        }
        return -1;
    }

}
