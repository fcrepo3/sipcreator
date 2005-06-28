package fedora.services.sipcreator.metadata;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
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

import fedora.services.sipcreator.Constants;
import fedora.services.sipcreator.utility.DOMUtility;
import fedora.services.sipcreator.utility.StreamUtility;
import fedora.services.sipcreator.utility.TableSorter;

/**
 *
 * <p><b>Title:</b> DCFields.java</p>
 * <p><b>Description:</b> </p>
 *
 * @author cwilper@cs.cornell.edu
 * @version $Id$
 */
public class DCFields extends Metadata implements Constants {

	public static final String OAIDC_PREFIX = "oai_dc";
	public static final String DC_PREFIX = "dc";
			
    public static final String[] DC_FIELDS =
    {"title", "creator", "subject", "description", "publisher", 
    "contributor", "date", "type", "format", "identifier", 
    "source", "language", "relation", "coverage", "rights"};
    
    private Vector titles = new Vector();
    private Vector creators = new Vector();
    private Vector subjects = new Vector();
    private Vector descriptions = new Vector();
    private Vector publishers = new Vector();
    private Vector contributors = new Vector();
    private Vector dates = new Vector();
    private Vector types = new Vector();
    private Vector formats = new Vector();
    private Vector identifiers = new Vector();
    private Vector sources = new Vector();
    private Vector languages = new Vector();
    private Vector relations = new Vector();
    private Vector coverages = new Vector();
    private Vector rights = new Vector();

    public DCFields() {
    }
    
    public DCFields(Element xmlNode) {
        super(xmlNode);
        
        Element xmlDataNode = (Element)xmlNode.getElementsByTagNameNS(METS_NS, "xmlData").item(0);
        Element oaiDCNode = DOMUtility.firstElementNamed(xmlDataNode, OAIDC_NS, "dc");
        NodeList childList = oaiDCNode.getChildNodes();
        for (int ctr = 0; ctr < childList.getLength(); ctr++) {
            try {
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
            } catch (Exception e) {}
        }
    }

    public Vector titles() {
        return titles;
    }

    public Vector creators() {
        return creators;
    }

    public Vector subjects() {
        return subjects;
    }

    public Vector descriptions() {
        return descriptions;
    }

    public Vector publishers() {
        return publishers;
    }

    public Vector contributors() {
        return contributors;
    }

    public Vector dates() {
        return dates;
    }

    public Vector types() {
        return types;
    }

    public Vector formats() {
        return formats;
    }

    public Vector identifiers() {
        return identifiers;
    }

    public Vector sources() {
        return sources;
    }

    public Vector languages() {
        return languages;
    }

    public Vector relations() {
        return relations;
    }

    public Vector coverages() {
        return coverages;
    }

    public Vector rights() {
        return rights;
    }

    /**
     * Get the DCFields as a String in namespace-qualified XML form,
     * matching the oai_dc schema.... but without the xml declaration.
     */
    public String getAsXML() {
        StringBuffer out=new StringBuffer();
		out.append("<" + OAIDC_PREFIX + ":dc"
			+ " xmlns:"	+ OAIDC_PREFIX + "=\"" + OAIDC_NS + "\""
			+ " xmlns:"	+ DC_PREFIX + "=\"" + DC_NS + "\">\n");
        appendXML(titles(), "title", out);
        appendXML(creators(), "creator", out);
        appendXML(subjects(), "subject", out);
        appendXML(descriptions(), "description", out);
        appendXML(publishers(), "publisher", out);
        appendXML(contributors(), "contributor", out);
        appendXML(dates(), "date", out);
        appendXML(types(), "type", out);
        appendXML(formats(), "format", out);
        appendXML(identifiers(), "identifier", out);
        appendXML(sources(), "source", out);
        appendXML(languages(), "language", out);
        appendXML(relations(), "relation", out);
        appendXML(coverages(), "coverage", out);
        appendXML(rights(), "rights", out);
        out.append("</oai_dc:dc>\n");
        return out.toString();
    }

    private void appendXML(List values, String name, StringBuffer out) {
        for (int i=0; i<values.size(); i++) {
            out.append("  <dc:" + name + ">");
            out.append(StreamUtility.enc((String)values.get(i)));
            out.append("</dc:" + name + ">\n");
        }
    }

    public String getHint() {
        return "DC: " + getLabel();
    }
    
    public MetadataPanel getPanel() {
        return new DCFieldsPanel(this);
    }
    
    private static class DCFieldsPanel extends MetadataPanel implements ActionListener {

        private static final long serialVersionUID = 2151470252809182456L;
        
        private DCFields metadata;
        
        private JTable dcDataDisplay;
        private DefaultTableModel dcDataModel;
        
        public DCFieldsPanel(DCFields initialData) {
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
            updateTableField(DC_FIELDS[ 0], metadata.titles());
            updateTableField(DC_FIELDS[ 1], metadata.creators());
            updateTableField(DC_FIELDS[ 2], metadata.subjects());
            updateTableField(DC_FIELDS[ 3], metadata.descriptions());
            updateTableField(DC_FIELDS[ 4], metadata.publishers());
            updateTableField(DC_FIELDS[ 5], metadata.contributors());
            updateTableField(DC_FIELDS[ 6], metadata.dates());
            updateTableField(DC_FIELDS[ 7], metadata.types());
            updateTableField(DC_FIELDS[ 8], metadata.formats());
            updateTableField(DC_FIELDS[ 9], metadata.identifiers());
            updateTableField(DC_FIELDS[10], metadata.sources());
            updateTableField(DC_FIELDS[11], metadata.languages());
            updateTableField(DC_FIELDS[12], metadata.relations());
            updateTableField(DC_FIELDS[13], metadata.coverages());
            updateTableField(DC_FIELDS[14], metadata.rights());
        }
        
        public void updateMetadata() {
            transferDataTypeToDCFields(DC_FIELDS[ 0], metadata.titles());
            transferDataTypeToDCFields(DC_FIELDS[ 1], metadata.creators());
            transferDataTypeToDCFields(DC_FIELDS[ 2], metadata.subjects());
            transferDataTypeToDCFields(DC_FIELDS[ 3], metadata.descriptions());
            transferDataTypeToDCFields(DC_FIELDS[ 4], metadata.publishers());
            transferDataTypeToDCFields(DC_FIELDS[ 5], metadata.contributors());
            transferDataTypeToDCFields(DC_FIELDS[ 6], metadata.dates());
            transferDataTypeToDCFields(DC_FIELDS[ 7], metadata.types());
            transferDataTypeToDCFields(DC_FIELDS[ 8], metadata.formats());
            transferDataTypeToDCFields(DC_FIELDS[ 9], metadata.identifiers());
            transferDataTypeToDCFields(DC_FIELDS[10], metadata.sources());
            transferDataTypeToDCFields(DC_FIELDS[11], metadata.languages());
            transferDataTypeToDCFields(DC_FIELDS[12], metadata.relations());
            transferDataTypeToDCFields(DC_FIELDS[13], metadata.coverages());
            transferDataTypeToDCFields(DC_FIELDS[14], metadata.rights());
        }
        
        private void transferDataTypeToDCFields(String type, List dest) {
            dest.clear();
            for (int ctr = 0; ctr < dcDataModel.getRowCount(); ctr++) {
                if (dcDataModel.getValueAt(ctr, 0).equals(type)) {
                    dest.add(dcDataModel.getValueAt(ctr, 1));
                }
            }
        }
        
        private void updateTableField(String name, List values) {
            Iterator i = values.iterator();
            while (i.hasNext()) {
                dcDataModel.addRow(new Object[]{name, i.next()});
            }
        }

    }
    
}
