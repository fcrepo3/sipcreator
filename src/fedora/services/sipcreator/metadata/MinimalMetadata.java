package fedora.services.sipcreator.metadata;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class MinimalMetadata extends Metadata {

    private String xmlString;
    
    public MinimalMetadata() {
        setName("Minimal Metadata");
    }
    
    public MetadataPanel getPanel() {
        return new MinimalMetadataPanel(this);
    }

    public void setXMLString(String newXMLString) {
        xmlString = newXMLString;
    }
    
    public String getXMLString() {
        return xmlString;
    }
    
    public String getAsXML() {
        return xmlString;
    }

    private static class MinimalMetadataPanel extends MetadataPanel {

        private static final long serialVersionUID = 4048798970023065657L;
        
        
        private JTextArea textArea;
        
        private MinimalMetadata metadata;
        
        
        public MinimalMetadataPanel(MinimalMetadata newMetadata) {
            setLayout(new BorderLayout());
            
            metadata = newMetadata;
            textArea = new JTextArea();
            add(new JScrollPane(textArea), BorderLayout.CENTER);
            
            updateFromMetadata();
        }

        
        public Metadata getMetadata() {
            return metadata;
        }

        public void updateFromMetadata() {
            textArea.setText(metadata.getAsXML());
        }

        public void updateMetadata() {
            metadata.setXMLString(textArea.getText());
        }
        
    }
    
}
