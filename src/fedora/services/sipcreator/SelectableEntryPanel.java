package fedora.services.sipcreator;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import beowulf.gui.Utility;

/**
 * This is the UI view class to be used in displaying and interacting
 * with SelectableEntry nodes.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class SelectableEntryPanel extends JPanel {

    /** */
    private static final long serialVersionUID = 4049918281383228216L;


    /** This is the text field where the mime type can be edited */
    private JTextField mimeTypeField = new JTextField(10);
    
    /** This is the text field that displays the entry's id */
    private JTextField entryIDField = new JTextField(10);
    
    /** This field contains the entry object that this UI view represents */
    private SelectableEntry entry;
    
    
    /**
     * This constructor produces a SelectableEntryPanel for a given
     * SelectableEntry.  That entry must not be null.
     * <br><br>
     * @param newEntry The entry which this view will display.
     */
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
    
    /**
     * This method creates and assembles the "north panel" portion of this
     * UI view.  The north panel in this case actually contains all of the
     * UI widgets visible to the user.  The center panel is just an empty
     * JPanel.
     * <br><br>
     * @return The created and assmbled "north panel" of this UI view.
     */
    private JComponent createNorthPanel() {
        Box tempP1 = Box.createHorizontalBox();
        tempP1.add(Utility.addLabelLeft("ID: ", entryIDField));
        tempP1.add(Box.createHorizontalStrut(5));
        tempP1.add(Utility.addLabelLeft("Mime Type: ", mimeTypeField));
        
        return tempP1;
    }
    
    
    /**
     * This method refreshes the UI view from the metadata data model. 
     */
    public void updateFromMetadata() {
        mimeTypeField.setText(entry.getMimeType());
    }
    
    /**
     * This method causes all unpushed metadata in the UI view to be pushed
     * to the underlying metadata model.
     */
    public void updateMetadata() {
        entry.setMimeType(mimeTypeField.getText());
    }
    
}
