package fedora.services.sipcreator.utility;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;

import fedora.services.sipcreator.SelectableEntryNode;
import fedora.services.sipcreator.FileSystemEntry;

public class CheckRenderer extends JPanel implements TreeCellRenderer {
    
    private static final long serialVersionUID = 3256722900836234808L;

    private Border selectedBorder = BorderFactory.createLineBorder(UIManager.getColor("Tree.selectionBorderColor"));
    
    private Border unselectedBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    
    private boolean checkBoxHidden = false;
    
    private JCheckBox check = new JCheckBox();
    
    private JLabel label = new JLabel();
    
    public CheckRenderer() {
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
        
        if (!checkBoxHidden && cNodeValue != null) {
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
        
        if (tree.isEnabled()) {
            if (leaf) {
                label.setIcon(UIManager.getIcon("Tree.leafIcon"));
            } else if (expanded) {
                label.setIcon(UIManager.getIcon("Tree.openIcon"));
            } else {
                label.setIcon(UIManager.getIcon("Tree.closedIcon"));
            }
        } else {
            if (leaf) {
                label.setDisabledIcon(UIManager.getIcon("Tree.leafIcon"));
            } else if (expanded) {
                label.setDisabledIcon(UIManager.getIcon("Tree.openIcon"));
            } else {
                label.setDisabledIcon(UIManager.getIcon("Tree.closedIcon"));
            }
        }
        
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

    public boolean isCheckBoxHidden() {
        return checkBoxHidden;
    }

    public void setCheckBoxHidden(boolean newCheckBoxHidden) {
        checkBoxHidden = newCheckBoxHidden;
    }
    
    public int getCheckBoxWidth() {
        return check.getWidth();
    }
    
    
}
