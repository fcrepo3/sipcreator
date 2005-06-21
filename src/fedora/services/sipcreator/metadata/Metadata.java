package fedora.services.sipcreator.metadata;

import org.xml.sax.helpers.DefaultHandler;

public abstract class Metadata extends DefaultHandler {

    private static long currentID = 0;

    public static synchronized long getNextID() {
        return currentID++;
    }
    
    private String id = Long.toString(getNextID());
    
    private String label = new String();
    
    private String type = new String();
    
    private String hint = new String();
    
    public String getID() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getType() {
        return type;
    }
    
    public String getHint() {
        return hint;
    }
    
    public void setLabel(String newLabel) {
        label = newLabel;
    }
    
    public void setType(String newType) {
        type = newType;
    }
    
    public void setHint(String newHint) {
        hint = newHint;
    }
    
    public abstract MetadataPanel getPanel();

    public abstract String getAsXML();
    
    public boolean equals(Object o) {
        if (!(o instanceof Metadata)) return false;
        Metadata m = (Metadata)o;
        return m.getID().equals(id);
    }
    
}