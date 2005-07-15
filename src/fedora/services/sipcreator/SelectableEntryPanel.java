package fedora.services.sipcreator;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import beowulf.gui.Utility;

public class SelectableEntryPanel extends JPanel {

    private static final long serialVersionUID = 4049918281383228216L;

    
    private JTextField mimeTypeField = new JTextField(10);
    
    private JTextField entryIDField = new JTextField(10);
    
    private SelectableEntry entry;
    
    
    public SelectableEntryPanel(SelectableEntry newEntry) {
        super(new BorderLayout(5, 5));

        entry = newEntry;
        entryIDField.setEditable(false);
        entryIDField.setText(entry.getID());
        
        add(createNorthPanel(), BorderLayout.NORTH);
        add(new JPanel(), BorderLayout.CENTER);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        updateFromMetadata();
    }
    
    private JComponent createNorthPanel() {
        Box tempP1 = Box.createHorizontalBox();
        tempP1.add(Utility.addLabelLeft("ID: ", entryIDField));
        tempP1.add(Box.createHorizontalStrut(5));
        tempP1.add(Utility.addLabelLeft("Mime Type: ", mimeTypeField));
        
        return tempP1;
    }
    
    
    public void updateFromMetadata() {
        mimeTypeField.setText(entry.getMimeType());
    }
    
    public void updateMetadata() {
        entry.setMimeType(mimeTypeField.getText());
    }
    
}
