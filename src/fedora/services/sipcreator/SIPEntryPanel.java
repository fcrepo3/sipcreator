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

public class SIPEntryPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 4049918281383228216L;

    private JTextField mimeTypeBox = new JTextField(15);
    
    private JComboBox classBox;
    
    private JTabbedPane metadataPane = new JTabbedPane(JTabbedPane.TOP);
    
    private SIPEntry entry;
    
    private SIPCreator creator;
    
    public SIPEntryPanel(SIPEntry newEntry, SIPCreator newCreator) {
        super(new BorderLayout(5, 5));

        entry = newEntry;
        creator = newCreator;
        classBox = new JComboBox(creator.getKnownMetadataClassNames());
        
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
        
        result.add(tempP1);
        
        
        tempP1 = Box.createHorizontalBox();
        button = new JButton("Rename Tab");
        button.addActionListener(this);
        tempP1.add(button);
        
        tempP1.add(Box.createHorizontalStrut(5));
        
        tempP1.add(GUIUtility.addLabelLeft("Mime Type: ", mimeTypeBox));
        result.add(tempP1);

        
        return result;
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
            
            while (entry.getMetadata().contains(newMetadata)) {
                String result = JOptionPane.showInputDialog
                (this, "Tab name conflict, please enter the new tab's name", newMetadata.getName());
                if (result == null || result.length() == 0) return;
                newMetadata.setName(result);
            }
            
            entry.getMetadata().add(newMetadata);
            MetadataPanel panel = newMetadata.getPanel();
            metadataPane.addTab(newMetadata.getName(), panel);
            metadataPane.setSelectedComponent(panel);
        } catch (Exception e) {
            GUIUtility.showExceptionDialog(this, e);
        }
    }
    
    private void removeMetadataAction(int index) {
        MetadataPanel panel = (MetadataPanel)metadataPane.getComponentAt(index);
        metadataPane.remove(index);
        entry.getMetadata().remove(panel.getMetadata());
    }
    
    private void renameTabAction(int index) {
        String result = JOptionPane.showInputDialog
        (this, "Please enter the new tab name", metadataPane.getTitleAt(index));
        
        if (result == null || result.length() == 0) return;
        
        MetadataPanel panel = (MetadataPanel)metadataPane.getComponentAt(index);
        panel.getMetadata().setName(result);
        metadataPane.setTitleAt(index, result);
    }
    
    public void updateFromMetadata() {
        Vector metadataList = entry.getMetadata();
        mimeTypeBox.setText(entry.getMimeType());
        
        for (int ctr = 0; ctr < metadataList.size(); ctr++) {
            Metadata metadata = (Metadata)metadataList.get(ctr);
            metadataPane.addTab(metadata.getName(), metadata.getPanel());
        }
    }
    
    public void updateMetadata() {
        entry.setMimeType(mimeTypeBox.getText());
        for (int ctr = 0; ctr < metadataPane.getTabCount(); ctr++) {
            MetadataPanel mp = (MetadataPanel)metadataPane.getComponentAt(ctr);
            mp.updateMetadata();
            mp.getMetadata().setName(metadataPane.getTitleAt(ctr));
        }
    }
    
}
