package fedora.services.sipcreator.utility;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

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
import fedora.services.sipcreator.SelectableEntryNode;

public class CheckRenderer extends JPanel implements TreeCellRenderer, Constants {
    
    private static final long serialVersionUID = 3256722900836234808L;

    private static final ImageIcon METADATA_ICON = new ImageIcon(IMAGE_DIR_NAME + "metadata.png");
    
    private Border selectedBorder = BorderFactory.createLineBorder(UIManager.getColor("Tree.selectionBorderColor"));
    
    private Border unselectedBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    
    private boolean checkShowing = true;
    
    private JCheckBox check = new JCheckBox();
    
    private JLabel label = new JLabel();
    
    public CheckRenderer() {
        this(true);
    }
    
    public CheckRenderer(boolean newCheckShowing) {
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
            icon = METADATA_ICON;
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

    public boolean isCheckShowing() {
        return checkShowing;
    }

    public void setCheckShowing(boolean newCheckShowing) {
        checkShowing = newCheckShowing;
    }
    
    public int getCheckBoxWidth() {
        return check.getWidth();
    }
    
    
}
