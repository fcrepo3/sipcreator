package fedora.services.sipcreator.metadata;

import org.w3c.dom.Element;

import fedora.services.sipcreator.Constants;

public abstract class Metadata implements Constants {

    private static long lastID = 0;

    public static synchronized String getNextID() {
        long nextID = System.currentTimeMillis();
        while (nextID == lastID) {
            try { Thread.sleep(10); }
            catch (InterruptedException ie) {}
            nextID = System.currentTimeMillis();
        }
        lastID = nextID;
        return "_" + Long.toString(lastID);
    }
    
    private String id;
    
    private String label = new String();
    
    private String type = new String();
    
    public Metadata() {
        id = getNextID();
    }
    
    public Metadata(Element xmlNode) {
        if (xmlNode.getNamespaceURI().equals(METS_NS) && xmlNode.getLocalName().equals("dmdSec")) {
            id = xmlNode.getAttribute("ID");
        } else if (xmlNode.getNamespaceURI().equals(METS_NS) && xmlNode.getLocalName().equals("file")) {
            id = xmlNode.getAttribute("ID");
        } else {
            id = getNextID();
        }
    }
    
    public String getID() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getType() {
        return type;
    }
    
    public abstract String getHint();
    
    public void setLabel(String newLabel) {
        label = newLabel;
    }
    
    public void setType(String newType) {
        type = newType;
    }
    
    public abstract MetadataPanel getPanel();

    public abstract String getAsXML();
    
    public boolean equals(Object o) {
        if (!(o instanceof Metadata)) return false;
        Metadata m = (Metadata)o;
        return m.getID().equals(id);
    }
    
}