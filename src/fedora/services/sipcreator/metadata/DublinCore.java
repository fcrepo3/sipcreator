package fedora.services.sipcreator.metadata;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import beowulf.util.DOMUtility;
import beowulf.util.StreamUtility;
import fedora.services.sipcreator.Constants;
import fedora.services.sipcreator.utility.TableSorter;

/**
 * This class implements the abstract class Metadata and represents a Dublin
 * Core metadata entry in the SIPCreator system.  All 15 fields are present
 * and fully editable.  This code is modified from the DCFields class in
 * Fedora.
 * <br><br>
 * @author cwilper@cs.cornell.edu
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 * @version $Id$
 */
public class DublinCore extends Metadata implements Constants {

    /**
     * This string is the prefix used to represent the OAI DC namespace
     * when DublinCore instances are transformed into XML.
     */
	private static final String OAIDC_PREFIX = "oai_dc";
    
    /**
     * This string is the prefix used to represent the DC namespace when
     * DublineCore instances are transformed into XML.
     */
	private static final String DC_PREFIX = "dc";
	
    /**
     * This array of strings defines the tags that are used in declaring
     * values for particular fields in a DublineCore entry.
     */
    private static final String[] DC_FIELDS =
    {"title", "creator", "subject", "description", "publisher", 
    "contributor", "date", "type", "format", "identifier", 
    "source", "language", "relation", "coverage", "rights"};
    
    /** The list of title entrys in this DublinCore record */
    private Vector titles = new Vector();
    
    /** The list of creator entrys in this DublinCore record */
    private Vector creators = new Vector();
    
    /** The list of subject entrys in this DublinCore record */
    private Vector subjects = new Vector();
    
    /** The list of description entrys in this DublinCore record */
    private Vector descriptions = new Vector();
    
    /** The list of publisher entrys in this DublinCore record */
    private Vector publishers = new Vector();
    
    /** The list of contributor entrys in this DublinCore record */
    private Vector contributors = new Vector();
    
    /** The list of date entrys in this DublinCore record */
    private Vector dates = new Vector();
    
    /** The list of type entrys in this DublinCore record */
    private Vector types = new Vector();
    
    /** The list of format entrys in this DublinCore record */
    private Vector formats = new Vector();
    
    /** The list of identifier entrys in this DublinCore record */
    private Vector identifiers = new Vector();
    
    /** The list of source entrys in this DublinCore record */
    private Vector sources = new Vector();
    
    /** The list of language entrys in this DublinCore record */
    private Vector languages = new Vector();
    
    /** The list of relation entrys in this DublinCore record */
    private Vector relations = new Vector();
    
    /** The list of coverage entrys in this DublinCore record */
    private Vector coverages = new Vector();
    
    /** The list of rights entrys in this DublinCore record */
    private Vector rights = new Vector();

    /**
     * This is the default constructor, which produces an empty DC record.
     */
    public DublinCore() {
    }
    
    /**
     * This constructor deserializes a DublinCore object from an
     * org.w3c.dom.Element object.  The fields are read and the data is placed
     * in the appropriate list.
     * <br><br>
     * @param xmlNode The node object from which to construct the DC entry.
     */
    public DublinCore(Element xmlNode) {
        super(xmlNode);
        
        Element xmlDataNode = (Element)xmlNode.getElementsByTagNameNS(METS_NS, "xmlData").item(0);
        Element oaiDCNode = DOMUtility.firstElementNamed(xmlDataNode, OAIDC_NS, "dc");
        NodeList childList = oaiDCNode.getChildNodes();
        for (int ctr = 0; ctr < childList.getLength(); ctr++) {
            Node currentNode = childList.item(ctr);
            
            if (!currentNode.getNamespaceURI().equals(DC_NS)) {
                continue;
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 0])) {
                titles.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 1])) {
                creators.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 2])) {
                subjects.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 3])) {
                descriptions.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 4])) {
                publishers.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 5])) {
                contributors.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 6])) {
                dates.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 7])) {
                types.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 8])) {
                formats.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[ 9])) {
                identifiers.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[10])) {
                sources.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[11])) {
                languages.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[12])) {
                relations.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[13])) {
                coverages.add(currentNode.getFirstChild().getNodeValue());
            } else if (currentNode.getLocalName().equals(DC_FIELDS[14])) {
                rights.add(currentNode.getFirstChild().getNodeValue());
            }
        }
    }

    /**
     * Returns the list of title entries in this DC record.
     * <br><br>
     * @return The list of title entries in this DC record.
     */
    public Vector getTitles() {
        return titles;
    }

    /**
     * Returns the list of creator entries in this DC record.
     * <br><br>
     * @return The list of creator entries in this DC record.
     */
    public Vector getCreators() {
        return creators;
    }

    /**
     * Returns the list of subect entries in this DC record.
     * <br><br>
     * @return The list of subject entries in this DC record.
     */
    public Vector getSubjects() {
        return subjects;
    }

    /**
     * Returns the list of description entries in this DC record.
     * <br><br>
     * @return The list of description entries in this DC record.
     */
    public Vector getDescriptions() {
        return descriptions;
    }

    /**
     * Returns the list of publisher entries in this DC record.
     * <br><br>
     * @return The list of publisher entries in this DC record.
     */
    public Vector getPublishers() {
        return publishers;
    }

    /**
     * Returns the list of contributor entries in this DC record.
     * <br><br>
     * @return The list of contributor entries in this DC record.
     */
    public Vector getContributors() {
        return contributors;
    }

    /**
     * Returns the list of date entries in this DC record.
     * <br><br>
     * @return The list of date entries in this DC record.
     */
    public Vector getDates() {
        return dates;
    }

    /**
     * Returns the list of type entries in this DC record.
     * <br><br>
     * @return The list of type entries in this DC record.
     */
    public Vector getTypes() {
        return types;
    }

    /**
     * Returns the list of format entries in this DC record.
     * <br><br>
     * @return The list of format entries in this DC record.
     */
    public Vector getFormats() {
        return formats;
    }

    /**
     * Returns the list of identifier entries in this DC record.
     * <br><br>
     * @return The list of identifier entries in this DC record.
     */
    public Vector getIdentifiers() {
        return identifiers;
    }

    /**
     * Returns the list of source entries in this DC record.
     * <br><br>
     * @return The list of source entries in this DC record.
     */
    public Vector getSources() {
        return sources;
    }

    /**
     * Returns the list of language entries in this DC record.
     * <br><br>
     * @return The list of language entries in this DC record.
     */
    public Vector getLanguages() {
        return languages;
    }

    /**
     * Returns the list of relation entries in this DC record.
     * <br><br>
     * @return The list of relation entries in this DC record.
     */
    public Vector getRelations() {
        return relations;
    }

    /**
     * Returns the list of coverage entries in this DC record.
     * <br><br>
     * @return The list of coverage entries in this DC record.
     */
    public Vector getCoverages() {
        return coverages;
    }

    /**
     * Returns the list of rights entries in this DC record.
     * <br><br>
     * @return The list of rights entries in this DC record.
     */
    public Vector getRights() {
        return rights;
    }

    public String getAsXML() {
        StringBuffer out = new StringBuffer();
		out.append("<");
        out.append(OAIDC_PREFIX);
        out.append(":dc");
        
        out.append(" xmlns:");
        out.append(OAIDC_PREFIX);
        out.append("=\"");
        out.append(OAIDC_NS);
        out.append("\"");
        
        out.append(" xmlns:");
        out.append(DC_PREFIX);
        out.append("=\"");
        out.append(DC_NS);
        
        out.append("\">\n");
        
        appendXML(getTitles(),       DC_FIELDS[ 0], out);
        appendXML(getCreators(),     DC_FIELDS[ 1], out);
        appendXML(getSubjects(),     DC_FIELDS[ 2], out);
        appendXML(getDescriptions(), DC_FIELDS[ 3], out);
        appendXML(getPublishers(),   DC_FIELDS[ 4], out);
        appendXML(getContributors(), DC_FIELDS[ 5], out);
        appendXML(getDates(),        DC_FIELDS[ 6], out);
        appendXML(getTypes(),        DC_FIELDS[ 7], out);
        appendXML(getFormats(),      DC_FIELDS[ 8], out);
        appendXML(getIdentifiers(),  DC_FIELDS[ 9], out);
        appendXML(getSources(),      DC_FIELDS[10], out);
        appendXML(getLanguages(),    DC_FIELDS[11], out);
        appendXML(getRelations(),    DC_FIELDS[12], out);
        appendXML(getCoverages(),    DC_FIELDS[13], out);
        appendXML(getRights(),       DC_FIELDS[14], out);
        
        out.append("</oai_dc:dc>\n");
        
        return out.toString();
    }

    /**
     * This is a convenience method used to deal with the repetetive nature
     * of the XML generated by an OAI DC record.
     * <br><br>
     * @param values The list of values to be placed into the buffer.
     * @param name The name of the values to be placed into the buffer.
     * @param out The buffer in which to place the resulting XML.
     */
    private void appendXML(List values, String name, StringBuffer out) {
        for (int i = 0; i < values.size(); i++) {
            out.append("  <dc:");
            out.append(name);
            out.append(">");
            
            out.append(StreamUtility.enc((String)values.get(i)));
            
            out.append("</dc:");
            out.append(name);
            out.append(">\n");
        }
    }

    public String getShortName() {
        return "DC: " + getLabel();
    }
    
    public MetadataPanel getPanel() {
        return new DublinCorePanel(this);
    }
    
    /**
     * This class displays a simple table where each row in the table is an
     * entry in the underlying dublin core record.  The first column is the
     * name of the field and the second is its value.
     * <br><br>
     * @author Andy Scukanec - (ags at cs dot cornell dot edu)
     */
    private static class DublinCorePanel extends MetadataPanel implements ActionListener {

        /** */
        private static final long serialVersionUID = 2151470252809182456L;
        
        /** The underlying dublin core metadata record */
        private DublinCore metadata;
        
        /** The view for the table */
        private JTable dcDataDisplay;
        /** The model for the table */
        private DefaultTableModel dcDataModel;
        
        /**
         * This constructor performs the GUI setup and forces the calling
         * function to provide a reference to some dublin core metadata.
         * <br><br>
         * @param initialData The underlying dublin core metadata.
         */
        public DublinCorePanel(DublinCore initialData) {
            metadata = initialData;
            
            dcDataModel = new DefaultTableModel(new Object[]{"Field", "Value"}, 0);
            TableSorter sorter = new TableSorter(dcDataModel);
            dcDataDisplay = new JTable(sorter);
            sorter.setTableHeader(dcDataDisplay.getTableHeader());
            dcDataDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            dcDataDisplay.setCellSelectionEnabled(false);
            dcDataDisplay.setRowSelectionAllowed(true);
            dcDataDisplay.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JComboBox(DC_FIELDS)));
            
            JScrollPane scrollPane = new JScrollPane(dcDataDisplay);
            scrollPane.setBorder(null);

            JComponent northPanel = Box.createHorizontalBox();
            JButton button;
            button = new JButton("Add Row");
            button.addActionListener(this);
            northPanel.add(button);
            northPanel.add(Box.createHorizontalStrut(5));
            button = new JButton("Remove Row");
            button.addActionListener(this);
            northPanel.add(button);
            
            setLayout(new BorderLayout(5, 5));
            add(northPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            updateFromMetadata();
        }
        
        public Metadata getMetadata() {
            return metadata;
        }
        
        public void actionPerformed(ActionEvent ae) {
            String cmd = ae.getActionCommand();
            
            if (cmd == null) {
                return;
            } else if (cmd.equals("Add Row")) {
                dcDataModel.addRow(new Object[]{DC_FIELDS[0], ""});
            } else if (cmd.equals("Remove Row")) {
                int index = dcDataDisplay.getSelectedRow();
                if (index < 0) return;
                
                dcDataModel.removeRow(index);
            }
        }
        
        
        public void updateFromMetadata() {
            while (dcDataModel.getRowCount() > 0) {
                dcDataModel.removeRow(0);
            }
            updateTableField(DC_FIELDS[ 0], metadata.getTitles());
            updateTableField(DC_FIELDS[ 1], metadata.getCreators());
            updateTableField(DC_FIELDS[ 2], metadata.getSubjects());
            updateTableField(DC_FIELDS[ 3], metadata.getDescriptions());
            updateTableField(DC_FIELDS[ 4], metadata.getPublishers());
            updateTableField(DC_FIELDS[ 5], metadata.getContributors());
            updateTableField(DC_FIELDS[ 6], metadata.getDates());
            updateTableField(DC_FIELDS[ 7], metadata.getTypes());
            updateTableField(DC_FIELDS[ 8], metadata.getFormats());
            updateTableField(DC_FIELDS[ 9], metadata.getIdentifiers());
            updateTableField(DC_FIELDS[10], metadata.getSources());
            updateTableField(DC_FIELDS[11], metadata.getLanguages());
            updateTableField(DC_FIELDS[12], metadata.getRelations());
            updateTableField(DC_FIELDS[13], metadata.getCoverages());
            updateTableField(DC_FIELDS[14], metadata.getRights());
        }
        
        public void updateMetadata() {
            transferDataTypeToDCFields(DC_FIELDS[ 0], metadata.getTitles());
            transferDataTypeToDCFields(DC_FIELDS[ 1], metadata.getCreators());
            transferDataTypeToDCFields(DC_FIELDS[ 2], metadata.getSubjects());
            transferDataTypeToDCFields(DC_FIELDS[ 3], metadata.getDescriptions());
            transferDataTypeToDCFields(DC_FIELDS[ 4], metadata.getPublishers());
            transferDataTypeToDCFields(DC_FIELDS[ 5], metadata.getContributors());
            transferDataTypeToDCFields(DC_FIELDS[ 6], metadata.getDates());
            transferDataTypeToDCFields(DC_FIELDS[ 7], metadata.getTypes());
            transferDataTypeToDCFields(DC_FIELDS[ 8], metadata.getFormats());
            transferDataTypeToDCFields(DC_FIELDS[ 9], metadata.getIdentifiers());
            transferDataTypeToDCFields(DC_FIELDS[10], metadata.getSources());
            transferDataTypeToDCFields(DC_FIELDS[11], metadata.getLanguages());
            transferDataTypeToDCFields(DC_FIELDS[12], metadata.getRelations());
            transferDataTypeToDCFields(DC_FIELDS[13], metadata.getCoverages());
            transferDataTypeToDCFields(DC_FIELDS[14], metadata.getRights());
        }
        
        /**
         * This is another convenience method.  For a given type name, this
         * method will iterate over the entries in the table.  If their
         * type name matches the given type name, those matching values will
         * be added to the list.
         * <br><br>
         * @param type The type name to match.
         * @param dest The list to add the matching values.
         */
        private void transferDataTypeToDCFields(String type, List dest) {
            dest.clear();
            for (int ctr = 0; ctr < dcDataModel.getRowCount(); ctr++) {
                if (dcDataModel.getValueAt(ctr, 0).equals(type)) {
                    dest.add(dcDataModel.getValueAt(ctr, 1));
                }
            }
        }
        
        /**
         * This is another convenience method.  This method will iterate over
         * a list of values and add for each value it will add the row
         * {name, value} to the table.
         * <br><br>
         * @param name The name of all the values being added.
         * @param values The list of values being added.
         */
        private void updateTableField(String name, List values) {
            for (int ctr = 0; ctr < values.size(); ctr++) {
                dcDataModel.addRow(new Object[]{name, values.get(ctr)});
            }
        }

    }
    
}
