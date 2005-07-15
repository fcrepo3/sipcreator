package fedora.services.sipcreator.metadata;

import javax.swing.JPanel;

public abstract class MetadataPanel extends JPanel {

    public abstract Metadata getMetadata();
    
    public abstract void updateFromMetadata();

    public abstract void updateMetadata();

}