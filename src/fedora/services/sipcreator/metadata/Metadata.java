package fedora.services.sipcreator.metadata;

import org.w3c.dom.Element;

public abstract class Metadata {

    private static long currentID = 0;

    public static synchronized long getNextID() {
        return currentID++;
    }
    
    private String id;
    
    private String label = new String();
    
    private String type = new String();
    
    public Metadata() {
        id = Long.toString(getNextID());
    }
    
    public Metadata(Element xmlNode) {
        if (xmlNode.getNamespaceURI().equals("http://www.loc.gov/METS/") && xmlNode.getLocalName().equals("dmdSec")) {
            id = xmlNode.getAttribute("ID");
        } else if (xmlNode.getNamespaceURI().equals("http://www.loc.gov/METS/") && xmlNode.getLocalName().equals("file")) {
            id = xmlNode.getAttribute("ID");
        } else {
            id = Long.toString(getNextID());
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