package fedora.services.sipcreator;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import fedora.services.sipcreator.metadata.Metadata;
import fedora.services.sipcreator.metadata.MetadataPanel;
import fedora.services.sipcreator.utility.GUIUtility;
import fedora.services.sipcreator.utility.HideablePanel;

public class SelectableEntryPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 4049918281383228216L;

    
    private JTextField mimeTypeField = new JTextField(10);
    
    private JTextField labelField = new JTextField(10);
    
    private JComboBox classBox;
    
    private JTabbedPane metadataPane = new JTabbedPane(JTabbedPane.TOP);
    
    private SelectableEntry entry;
    
    private SIPCreator creator;
    
    
    public SelectableEntryPanel(SelectableEntry newEntry, SIPCreator newCreator) {
        super(new BorderLayout(5, 5));

        entry = newEntry;
        creator = newCreator;
        classBox = new JComboBox(creator.getKnownMetadataClassNames());
        
        metadataPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        add(createNorthPanel(), BorderLayout.NORTH);
        add(metadataPane, BorderLayout.CENTER);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        updateFromMetadata();
    }
    
    private JComponent createNorthPanel() {
        JPanel result = new JPanel(new GridLayout(3, 1, 5, 5));
        Box tempP1;
        JButton button;
        
        result.add(classBox);
        
        
        tempP1 = Box.createHorizontalBox();
        button = new JButton("Add Metadata");
        button.addActionListener(this);
        tempP1.add(button);
        
        tempP1.add(Box.createHorizontalStrut(5));
        
        button = new JButton("Remove Metadata");
        button.addActionListener(this);
        tempP1.add(button);
        
        tempP1.add(Box.createHorizontalStrut(5));

        button = new JButton("Rename Tab");
        button.addActionListener(this);
        tempP1.add(button);
        
        result.add(tempP1);
        
        
        tempP1 = Box.createHorizontalBox();
        
        tempP1.add(GUIUtility.addLabelLeft("Mime Type: ", mimeTypeField));
        
        tempP1.add(Box.createHorizontalStrut(5));
        
        tempP1.add(GUIUtility.addLabelLeft("Label: ", labelField));

        result.add(tempP1);
        
        HideablePanel hPanel = new HideablePanel(result, "Edit Metadata Options");
        hPanel.setResizeable(false);
        hPanel.setBorder(null);
        
        return hPanel;
    }
    
    
    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        int index = metadataPane.getSelectedIndex();
        
        if (cmd == null) {
            return;
        } else if (cmd.equals("Add Metadata")) {
            addMetadataAction();
        } else if (cmd.equals("Remove Metadata")) {
            if (index == -1) return;
            removeMetadataAction(index);
        } else if (cmd.equals("Rename Tab")) {
            if (index == -1) return;
            renameTabAction(index);
        }
    }
    
    private void addMetadataAction() {
        try {
            int index = classBox.getSelectedIndex();
            if (index == -1) return;
            Class selectedClass = (Class)creator.getKnownMetadataClasses().get(index);
            Constructor constructor = selectedClass.getConstructor(null);
            Metadata newMetadata = (Metadata)constructor.newInstance(null);
            
            entry.getMetadata().add(newMetadata);
            MetadataPanelWrapper panel = new MetadataPanelWrapper(newMetadata.getPanel());
            metadataPane.addTab(newMetadata.getHint(), panel);
            metadataPane.setSelectedComponent(panel);
        } catch (Exception e) {
            GUIUtility.showExceptionDialog(this, e);
        }
    }
    
    private void removeMetadataAction(int index) {
        MetadataPanelWrapper panel = (MetadataPanelWrapper)metadataPane.getComponentAt(index);
        metadataPane.remove(index);
        entry.getMetadata().remove(panel.getMetadata());
    }
    
    private void renameTabAction(int index) {
        String result = JOptionPane.showInputDialog
        (this, "Please enter the new tab name", metadataPane.getTitleAt(index));
        
        if (result == null || result.length() == 0) return;
        
        MetadataPanelWrapper panel = (MetadataPanelWrapper)metadataPane.getComponentAt(index);
        panel.getMetadata().setHint(result);
        metadataPane.setTitleAt(index, result);
    }
    
    
    public void updateFromMetadata() {
        while (metadataPane.getTabCount() > 0) {
            metadataPane.remove(0);
        }
        
        Vector metadataList = entry.getMetadata();
        mimeTypeField.setText(entry.getMimeType());
        labelField.setText(entry.getLabel());
        
        for (int ctr = 0; ctr < metadataList.size(); ctr++) {
            Metadata metadata = (Metadata)metadataList.get(ctr);
            metadataPane.addTab(metadata.getHint(), new MetadataPanelWrapper(metadata.getPanel()));
        }
    }
    
    public void updateMetadata() {
        entry.setMimeType(mimeTypeField.getText());
        entry.setLabel(labelField.getText());
        for (int ctr = 0; ctr < metadataPane.getTabCount(); ctr++) {
            MetadataPanelWrapper mpw = (MetadataPanelWrapper)metadataPane.getComponentAt(ctr);
            mpw.updateMetadata();
        }
    }
    
    
    private class MetadataPanelWrapper extends JPanel implements ActionListener {
    
        private static final long serialVersionUID = 3257002172494263094L;

        private MetadataPanel metadataPanel;
        
        private JTextField labelField = new JTextField();
        
        private JComboBox typeBox = new JComboBox(creator.getConversionRulesTask().getDatastreamTemplates());
        
        private JTextField nameField = new JTextField();
        
        public MetadataPanelWrapper(MetadataPanel newMetadataPanel) {
            super(new BorderLayout());
            
            metadataPanel = newMetadataPanel;
            
            JPanel temp = new JPanel(new GridLayout(3, 1, 5, 5));
            temp.add(GUIUtility.addLabelLeft("Label: ", labelField));
            temp.add(GUIUtility.addLabelLeft("Type: ", typeBox));
            temp.add(GUIUtility.addLabelLeft("ID: ", nameField));
            
            HideablePanel htemp = new HideablePanel(temp, "Common Metadata Attributes");
            htemp.setResizeable(false);
            htemp.setBorder(null);
            add(htemp, BorderLayout.NORTH);
            add(metadataPanel, BorderLayout.CENTER);
            
            updateFromMetadata();
            
            labelField.addActionListener(this);
            typeBox.addActionListener(this);
            typeBox.setEditable(true);
            nameField.setEnabled(false);
        }
        
        
        public MetadataPanel getMetadataPanel() {
            return metadataPanel;
        }
        
        public Metadata getMetadata() {
            return metadataPanel.getMetadata();
        }
        
        
        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == typeBox && ae.getActionCommand().equals("comboBoxEdited") && typeBox.getSelectedIndex() == -1) {
                int choice = JOptionPane.showConfirmDialog(creator,
                        "Would you like to add \"" + typeBox.getSelectedItem() + "\" as a new datastream template?",
                        "Add New Template", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    ConversionRules.DatastreamTemplate newDT = new ConversionRules.DatastreamTemplate();
                    newDT.nodeType = typeBox.getSelectedItem().toString();
                    creator.getConversionRulesTask().addDatastreamTemplate(newDT);
                    typeBox.setSelectedItem(newDT);
                }
            }
            updateMetadata();
        }
        
        
        public void updateFromMetadata() {
            Object template = creator.getConversionRulesTask().getDatastreamTemplate(metadataPanel.getMetadata().getType());
            if (template != null) {
                typeBox.setSelectedItem(template);
            } else {
                typeBox.setSelectedItem(metadataPanel.getMetadata().getType());
            }
            
            labelField.setText(metadataPanel.getMetadata().getLabel());
            nameField.setText(metadataPanel.getMetadata().getID());
            
            metadataPanel.updateFromMetadata();
        }
        
        public void updateMetadata() {
            getMetadata().setLabel(labelField.getText());
            
            if (typeBox.getSelectedItem() != null) {
                getMetadata().setType(typeBox.getSelectedItem().toString());
            } else {
                getMetadata().setType("");
            }
            
            metadataPanel.updateMetadata();
        }
        
    }
    
}
