package fedora.services.sipcreator.metadata;

import org.xml.sax.helpers.DefaultHandler;

public abstract class Metadata extends DefaultHandler {

    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String newName) {
        name = newName;
    }
    
    public abstract MetadataPanel getPanel();

    public abstract String getAsXML();
    
    public boolean equals(Object o) {
        if (!(o instanceof Metadata)) return false;
        Metadata m = (Metadata)o;
        return m.getName().equals(name);
    }
    
}