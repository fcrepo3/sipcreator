package fedora.services.sipcreator.metadata;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.axis.encoding.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fedora.services.sipcreator.Constants;

public class MinimalMetadata extends Metadata implements Constants {

    private String string = new String();
    
    public MinimalMetadata() {
    }

    public MinimalMetadata(Element xmlNode) {
        super(xmlNode);

        Node xmlDataNode = xmlNode.getElementsByTagNameNS(METS_NS, "xmlData").item(0);
        //Element xmlDataNode = DOMUtility.firstElementNamed(xmlNode, METS_NS, "xmlData");
        if (xmlDataNode.getFirstChild() != null) {
            Node plainTextNode = xmlDataNode.getFirstChild().getFirstChild();
            setString(new String(Base64.decode(plainTextNode.getNodeValue())));
        }
    }
    
    public MetadataPanel getPanel() {
        return new MinimalMetadataPanel(this);
    }

    public String getShortName() {
        return "Plain: " + getLabel();
    }
    
    public void setString(String newString) {
        string = newString;
    }
    
    public String getString() {
        return string;
    }
    
    public String getAsXML() {
        return "<plainText>" + Base64.encode(string.getBytes()) + "</plainText>";
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
            textArea.setText(metadata.getString());
        }

        public void updateMetadata() {
            metadata.setString(textArea.getText());
        }
        
    }
    
}
