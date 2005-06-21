package fedora.services.sipcreator.metadata;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

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
public class DCFields extends Metadata {

	public static final String OAIDC_PREFIX = "oai_dc";
	public static final String OAIDC_NS = "http://www.openarchives.org/OAI/2.0/oai_dc/";
	public static final String DC_PREFIX = "dc";
	public static final String DC_NS = "http://purl.org/dc/elements/1.1/";
			
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

    private StringBuffer currentContent;

    public DCFields() {
        setHint("Dublin Core");
    }

    public DCFields(InputStream in)
    throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser parser = spf.newSAXParser();

        parser.parse(in, this);
        setHint("Dublin Core");
    }

    public void startElement(String uri, String localName, String qName, Attributes attrs) {
        currentContent = new StringBuffer();
    }

    public void characters(char[] ch, int start, int length) {
        currentContent.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) {
        if (localName.equals("title")) {
            titles().add(currentContent.toString());
        } else if (localName.equals("creator")) {
            creators().add(currentContent.toString());
        } else if (localName.equals("subject")) {
            subjects().add(currentContent.toString());
        } else if (localName.equals("description")) {
            descriptions().add(currentContent.toString());
        } else if (localName.equals("publisher")) {
            publishers().add(currentContent.toString());
        } else if (localName.equals("contributor")) {
            contributors().add(currentContent.toString());
        } else if (localName.equals("date")) {
            dates().add(currentContent.toString());
        } else if (localName.equals("type")) {
            types().add(currentContent.toString());
        } else if (localName.equals("format")) {
            formats().add(currentContent.toString());
        } else if (localName.equals("identifier")) {
            identifiers().add(currentContent.toString());
        } else if (localName.equals("source")) {
            sources().add(currentContent.toString());
        } else if (localName.equals("language")) {
            languages().add(currentContent.toString());
        } else if (localName.equals("relation")) {
            relations().add(currentContent.toString());
        } else if (localName.equals("coverage")) {
            coverages().add(currentContent.toString());
        } else if (localName.equals("rights")) {
            rights().add(currentContent.toString());
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
            out.append(StreamUtility.enc((String) values.get(i)));
            out.append("</dc:" + name + ">\n");
        }
    }

    public MetadataPanel getPanel() {
        return new DCFieldsPanel(this);
    }
    
    private static class DCFieldsPanel extends MetadataPanel implements ActionListener {

        private static final long serialVersionUID = 2151470252809182456L;
        
        public static final String[] DC_TYPES =
        {"title", "creator", "subject", "description", "publisher", 
        "contributor", "date", "type", "format", "identifier", 
        "source", "language", "relation", "coverage", "right"};
        
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
            dcDataDisplay.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JComboBox(DC_TYPES)));
            
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
                dcDataModel.addRow(new Object[]{DC_TYPES[0], ""});
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
            updateTableField(DC_TYPES[ 0], metadata.titles());
            updateTableField(DC_TYPES[ 1], metadata.creators());
            updateTableField(DC_TYPES[ 2], metadata.subjects());
            updateTableField(DC_TYPES[ 3], metadata.descriptions());
            updateTableField(DC_TYPES[ 4], metadata.publishers());
            updateTableField(DC_TYPES[ 5], metadata.contributors());
            updateTableField(DC_TYPES[ 6], metadata.dates());
            updateTableField(DC_TYPES[ 7], metadata.types());
            updateTableField(DC_TYPES[ 8], metadata.formats());
            updateTableField(DC_TYPES[ 9], metadata.identifiers());
            updateTableField(DC_TYPES[10], metadata.sources());
            updateTableField(DC_TYPES[11], metadata.languages());
            updateTableField(DC_TYPES[12], metadata.relations());
            updateTableField(DC_TYPES[13], metadata.coverages());
            updateTableField(DC_TYPES[14], metadata.rights());
        }
        
        public void updateMetadata() {
            transferDataTypeToDCFields(DC_TYPES[ 0], metadata.titles());
            transferDataTypeToDCFields(DC_TYPES[ 1], metadata.creators());
            transferDataTypeToDCFields(DC_TYPES[ 2], metadata.subjects());
            transferDataTypeToDCFields(DC_TYPES[ 3], metadata.descriptions());
            transferDataTypeToDCFields(DC_TYPES[ 4], metadata.publishers());
            transferDataTypeToDCFields(DC_TYPES[ 5], metadata.contributors());
            transferDataTypeToDCFields(DC_TYPES[ 6], metadata.dates());
            transferDataTypeToDCFields(DC_TYPES[ 7], metadata.types());
            transferDataTypeToDCFields(DC_TYPES[ 8], metadata.formats());
            transferDataTypeToDCFields(DC_TYPES[ 9], metadata.identifiers());
            transferDataTypeToDCFields(DC_TYPES[10], metadata.sources());
            transferDataTypeToDCFields(DC_TYPES[11], metadata.languages());
            transferDataTypeToDCFields(DC_TYPES[12], metadata.relations());
            transferDataTypeToDCFields(DC_TYPES[13], metadata.coverages());
            transferDataTypeToDCFields(DC_TYPES[14], metadata.rights());
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
