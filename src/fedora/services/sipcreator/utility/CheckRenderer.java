package fedora.services.sipcreator.utility;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;

import fedora.services.sipcreator.Constants;
import fedora.services.sipcreator.FileSystemEntry;
import fedora.services.sipcreator.MetadataNode;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.SelectableEntryNode;

/**
 * 
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class CheckRenderer extends JPanel implements TreeCellRenderer, Constants {
   
    /** */
    private static final long serialVersionUID = 3256722900836234808L;

    /** The image used on Metadata elements in the tree */
    private ImageIcon metadataIcon;
    
    /** The border to draw on the selected elements */
    private Border selectedBorder = BorderFactory.createLineBorder(UIManager.getColor("Tree.selectionBorderColor"));
    
    /** The border to draw on unselected elements*/
    private Border unselectedBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    
    /** Whether or not to draw the checkbox */
    private boolean checkShowing = true;
    
    /** The checkbox used to indicate selection status */
    private JCheckBox check = new JCheckBox();
    
    /** The label to disaply other information about the element */
    private JLabel label = new JLabel();
    
    /**
     * This constructor produces a CheckRenderer object with a visible check
     * box.  The SIPCreator parameter is used to resolve relative URLs against
     * the code base location (which is accessible through the Applet API).
     * <br><br>
     * @param creator The SIPCreator applet in which this CheckRenderer shall
     * live.
     */
    public CheckRenderer(SIPCreator creator) {
        this(true, creator);
    }
    
    /**
     * This constructor produces a CheckRenderer object whose checkbox
     * visibility is indicated by the first parameter.  The SIPCreator
     * parameter is used to resolve relative URLs against the code base
     * location (which is accessible through the Applet API).
     * <br><br>
     * @param newCheckShowing Whether or not to initially draw the checkbox.
     * @param creator The SIPCreator applet in which this CheckRenderer shall
     * live.
     */
    public CheckRenderer(boolean newCheckShowing, SIPCreator creator) {
        URL imgURL = creator.getURL(METADATA_IMAGE_NAME);
        metadataIcon = new ImageIcon(creator.getImage(imgURL));
        setCheckShowing(newCheckShowing);
        label.setOpaque(true);
        setLayout(new BorderLayout());
        add(check, BorderLayout.WEST);
        add(label, BorderLayout.CENTER);
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        SelectableEntryNode cNodeValue = value instanceof SelectableEntryNode ? (SelectableEntryNode)value : null;
        String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);
        setEnabled(tree.isEnabled());
        
        if (checkShowing && cNodeValue != null) {
            check.setSelected(cNodeValue.getEntry().getSelectionLevel() != FileSystemEntry.UNSELECTED);
            check.setEnabled(cNodeValue.getEntry().getSelectionLevel() != FileSystemEntry.PARTIALLY_SELECTED);
            check.setForeground(UIManager.getColor("Tree.textForeground"));
            check.setVisible(true);
        } else {
            check.setVisible(false);
        }
        
        label.setFont(tree.getFont());
        label.setText(stringValue);
        if (isSelected && tree.isEnabled()) {
            label.setBackground(UIManager.getColor("Tree.selectionBackground"));
            check.setBackground(UIManager.getColor("Tree.selectionBackground"));
            setBorder(selectedBorder);
            revalidate();
        } else {
            label.setBackground(UIManager.getColor("Tree.textBackground"));
            check.setBackground(UIManager.getColor("Tree.textBackground"));
            setBorder(unselectedBorder);
            revalidate();
        }
        label.setForeground(UIManager.getColor("Tree.textForeground"));

        Icon icon;
        if (value instanceof MetadataNode) {
            icon = metadataIcon;
        } else if (!cNodeValue.getEntry().isDirectory()) {
            icon = UIManager.getIcon("Tree.leafIcon");
        } else if (expanded) {
            icon = UIManager.getIcon("Tree.openIcon");
        } else {
            icon = UIManager.getIcon("Tree.closedIcon");
        }
        
        if (tree.isEnabled()) {
            label.setIcon(icon);
        } else {
            label.setDisabledIcon(icon);
        }
//        if (tree.isEnabled()) {
//            if (leaf) {
//                label.setIcon(UIManager.getIcon("Tree.leafIcon"));
//            } else if (expanded) {
//                label.setIcon(UIManager.getIcon("Tree.openIcon"));
//            } else {
//                label.setIcon(UIManager.getIcon("Tree.closedIcon"));
//            }
//        } else {
//            if (leaf) {
//                label.setDisabledIcon(UIManager.getIcon("Tree.leafIcon"));
//            } else if (expanded) {
//                label.setDisabledIcon(UIManager.getIcon("Tree.openIcon"));
//            } else {
//                label.setDisabledIcon(UIManager.getIcon("Tree.closedIcon"));
//            }
//        }
        
        setComponentOrientation(tree.getComponentOrientation());
        label.setComponentOrientation(tree.getComponentOrientation());
        check.setComponentOrientation(tree.getComponentOrientation());

        return this;
    }
    
    public void setBackground(Color color) {
        if (color instanceof ColorUIResource)
            color = null;
        super.setBackground(color);
    }

    /**
     * Returns whether or not the checkbox is visible.
     * <br><br>
     * @return Whether or not the checkbox is visible.
     */
    public boolean isCheckShowing() {
        return checkShowing;
    }

    /**
     * Sets the checkbox visibility status.
     * <br><br>
     * @param newCheckShowing The new checkbox visibility status.
     */
    public void setCheckShowing(boolean newCheckShowing) {
        checkShowing = newCheckShowing;
    }
    
    /**
     * Returns the width of the checkbox.  This is useful for determining
     * if the user clicked somewhere in the checkbox, or somewhere else (say
     * on the label).
     * <br><br>
     * @return The width of the checkbox.
     */
    public int getCheckBoxWidth() {
        return check.getWidth();
    }
    
    
}
