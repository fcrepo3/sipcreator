package fedora.services.sipcreator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fedora.services.sipcreator.acceptor.SIPEntryAcceptor;

public class ZipFileEntry extends SelectableEntry {

    private ZipFile file;
    
    private ZipEntry entry;
    
    private String name;
    
    private boolean isDirectory;
    
    private ZipFileEntry parent;
    
    private Vector childList = new Vector();
    
    private Hashtable childTable = new Hashtable();
    
    public ZipFileEntry(ZipFile newFile, ZipEntry newEntry, ZipFileEntry newParent) {
        file = newFile;
        entry = newEntry;
        parent = newParent;
        isDirectory = entry.getName().endsWith("/");
        name = entry.getName();
        if (isDirectory) {
            name = name.substring(0, name.length() - 1);
        }
    }
    
    
    public void addChild(ZipFileEntry child) {
        childList.add(child);
        childTable.put(child.getShortName(), child);
    }
    
    public ZipFileEntry getChild(String name) {
        return (ZipFileEntry)childTable.get(name);
    }
    
    
    public SelectableEntry getParent() {
        return parent;
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
        return file.getInputStream(entry);
    }

    
    public void setSelectionLevel(int newSelectionLevel, SIPEntryAcceptor acceptor) {
        selectionLevel = newSelectionLevel;
        
        for (int ctr = 0; ctr < childList.size(); ctr++) {
            ZipFileEntry child = (ZipFileEntry)childList.get(ctr);
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
            ZipFileEntry entry = (ZipFileEntry)getChildAt(ctr, acceptor);
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
        
        for (int ctr = 0; ctr < childList.size(); ctr++) {
            if (acceptor.isEntryAcceptable((ZipFileEntry)childList.get(ctr))) {
                count++;
            }
        }
        
        return count;
    }

    public SelectableEntry getChildAt(int index, SIPEntryAcceptor acceptor) {
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

    public int getIndex(SelectableEntry entry, SIPEntryAcceptor acceptor) {
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
