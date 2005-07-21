package fedora.services.sipcreator.metadata;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.axis.encoding.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fedora.services.sipcreator.Constants;

/**
 * This very simple Metadata subclass contains a string.  That string can have
 * any format, information, etc, as it will be base 64 encoded and dumped into
 * a CDATA section when requested to be turned into XML.  Freedom comes with
 * the cost of forcing the users of this class to bear the responsibility of
 * ensuring and validating any syntax/semantics they impose on the data in
 * these objects. 
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class MinimalMetadata extends Metadata implements Constants {

    /** The string containing the user data */
    private String string = new String();
    
    /**
     * The default constructor produces a MinimalMetadata instance with an
     * empty user data string. 
     */
    public MinimalMetadata() {
    }

    /**
     * This constructor reads in the data for the user defined string from the
     * given XML Element.
     * <br><br>
     * @param xmlNode The Element from which to read in the user defined
     * string.
     */
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
    
    /**
     * This methods sets the user defined string.
     * <br><br>
     * @param newString The user defined string.
     */
    public void setString(String newString) {
        string = newString;
    }
    
    /**
     * Returns the user defined string.
     * <br><br>
     * @return The user defined string.
     */
    public String getString() {
        return string;
    }
    
    public String getAsXML() {
        return "<plainText>" + Base64.encode(string.getBytes()) + "</plainText>";
    }

    /**
     * This subclass of MetadataPanel presents the user with a text area for
     * editing the user defined string.
     * <br><br>
     * @author Andy Scukanec - (ags at cs dot cornell dot edu)
     */
    private static class MinimalMetadataPanel extends MetadataPanel {

        /** */
        private static final long serialVersionUID = 4048798970023065657L;
        
        
        /** The text area for display and interacting with the data */
        private JTextArea textArea;
        /** The actual metadata object providing the data for this view */
        private MinimalMetadata metadata;
        
        
        /**
         * This constructor performs the GUI setup and forces the calling
         * function to provide a reference to some metadata.
         * <br><br>
         * @param newMetadata The underlying metadata.
         */
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
