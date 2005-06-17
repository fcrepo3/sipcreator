package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fedora.services.sipcreator.FileTreeNode;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.SIPEntry;
import fedora.services.sipcreator.acceptor.FilterAcceptor;
import fedora.services.sipcreator.utility.CheckRenderer;
import fedora.services.sipcreator.utility.GUIUtility;

public class FileSelectTask extends JPanel {

    private static final long serialVersionUID = 4051332249108427830L;
    
    private FilterAcceptor acceptor = new FilterAcceptor();
    
    private ChangeDirectoryAction changeDirectoryAction = new ChangeDirectoryAction();
    private EventHandler eventHandler = new EventHandler();
    private FilterAction filterAction = new FilterAction();
    
    //Data structures and UI components involved with the file browsing task
    private JLabel fileSelectDirectoryLabel = new JLabel();
    private CheckRenderer fileSelectTreeRenderer = new CheckRenderer();
    private DefaultTreeModel fileSelectTreeModel = new DefaultTreeModel(null);
    private JTree fileSelectTreeDisplay = new JTree(fileSelectTreeModel);
    
    private JCheckBox filterEnabledBox = new JCheckBox(filterAction);
    private JTextField filterField = new JTextField();
    
    private SIPCreator parent;
    
    public FileSelectTask(SIPCreator newParent) {
        parent = newParent;
        
        fileSelectTreeDisplay.setCellRenderer(fileSelectTreeRenderer);
        fileSelectTreeDisplay.addMouseListener(eventHandler);
        fileSelectTreeDisplay.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        //Minimum sizes are explicitly set so that labels with long text entries
        //will not keep the containing JSplitPane from resizing down past the point
        //at which all the text on the label is visible
        fileSelectDirectoryLabel.setMinimumSize(new Dimension(1, 1));

        filterField.addActionListener(filterAction);
        
        JPanel tempP1 = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel tempP2;
        
        tempP2 = new JPanel(new BorderLayout());
        tempP2.add(fileSelectDirectoryLabel, BorderLayout.CENTER);
        tempP2.add(new JButton(changeDirectoryAction), BorderLayout.EAST);
        tempP1.add(tempP2);
        
        tempP2 = new JPanel(new BorderLayout());
        tempP2.add(filterEnabledBox, BorderLayout.EAST);
        tempP2.add(filterField, BorderLayout.CENTER);
        tempP1.add(tempP2);
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(tempP1, BorderLayout.NORTH);
        add(new JScrollPane(fileSelectTreeDisplay), BorderLayout.CENTER);
    }

    
    public void updateTree(String rootDirectoryName, SIPEntry newRoot) {
        fileSelectDirectoryLabel.setText(rootDirectoryName);
        fileSelectTreeModel.setRoot(new FileTreeNode(newRoot, null, acceptor));
    }
    
    public void refreshTree() {
        fileSelectTreeModel.nodeStructureChanged((FileTreeNode)fileSelectTreeModel.getRoot());
    }
    
    public SIPEntry getRootEntry() {
        FileTreeNode rootNode = (FileTreeNode)fileSelectTreeModel.getRoot();
        return (rootNode == null ? null : rootNode.getEntry());
    }
    
    public FilterAcceptor getFilterAcceptor() {
        return acceptor;
    }
    
    
    private class EventHandler extends MouseAdapter {
        
        public void mouseClicked(MouseEvent me) {
            //We don't want to interpret a click on the JTree as a selection/deselection
            //of a tree node when only selected nodes are visible.
            if (me.getClickCount() > 1) return;
            
            int x = me.getX();
            int y = me.getY();
            
            TreePath path = fileSelectTreeDisplay.getPathForLocation(x, y);
            if (path == null) return;
            //The next two lines ensure that the user clicked on the checkbox, not the
            //icon or the text.  The "-2" is a hack, but seems to work
            Rectangle bounds = fileSelectTreeDisplay.getPathBounds(path);
            if (x > bounds.x + fileSelectTreeRenderer.getCheckBoxWidth() - 2) return;
            
            FileTreeNode node = (FileTreeNode)path.getLastPathComponent();
            boolean fullySelected = node.getEntry().getSelectionLevel() == SIPEntry.FULLY_SELECTED; 
            int selectionLevel = fullySelected ? SIPEntry.UNSELECTED : SIPEntry.FULLY_SELECTED;
            node.getEntry().setSelectionLevel(selectionLevel, acceptor);
            
            FileTreeNode nodeParent = (FileTreeNode)node.getParent();
            if (nodeParent != null) {
                nodeParent.getEntry().setSelectionLevelFromChildren(acceptor);
            }
            
            fileSelectTreeModel.nodeChanged(node);
            parent.getMetadataEntryTask().refreshTree();
        }
        
    }

    private class FilterAction extends AbstractAction {
        
        private static final long serialVersionUID = 3256441395794162737L;

        public FilterAction() {
            putValue(Action.NAME, "Filter On");
            putValue(Action.SHORT_DESCRIPTION, "Turns filtering on or off");
        }
        
        public void actionPerformed(ActionEvent ae) {
            String text = filterField.getText();
            text = text.replaceAll("\\.", "\\\\.");
            text = text.replaceAll("\\*", ".*");
            if (text.length() == 0) {
                text = ".*";
            }
            
            acceptor.setFilter(text);
            acceptor.setEnabled(filterEnabledBox.isSelected());            
            
            refreshTree();
            parent.getMetadataEntryTask().refreshTree();
        }
        
    }
    
    private class ChangeDirectoryAction extends AbstractAction {

        private static final long serialVersionUID = 3763096349595678519L;

        private JFileChooser fileChooser = new JFileChooser(".");
        
        public ChangeDirectoryAction() {
            putValue(Action.NAME, "Browse");
            putValue(Action.SHORT_DESCRIPTION, "Changes the selected root directory");
        }
        
        public void actionPerformed(ActionEvent ae) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int choice = fileChooser.showOpenDialog(parent);
            if (choice != JFileChooser.APPROVE_OPTION) return;
            SIPEntry rootEntry = null;
            String rootDirectoryName = null;
            
            if (fileSelectTreeModel.getRoot() != null) {
                choice = JOptionPane.showConfirmDialog(parent,
                        "This change will erase all metadata.  Continue?",
                        "Warning", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) return;
            }
            
            try {
                File file = fileChooser.getSelectedFile();
                rootDirectoryName = file.getCanonicalPath();
                rootEntry = new SIPEntry(file, null, parent);
                rootEntry.setSelectionLevel(SIPEntry.UNSELECTED, acceptor);
            } catch (IOException ioe) {
                GUIUtility.showExceptionDialog(parent, ioe);
                return;
            }

            JTabbedPane rightPanel = parent.getRightPanel();
            while (rightPanel.getTabCount() > 0) {
                rightPanel.remove(0);
            }
            parent.getMetadataEntryTask().updateTree(rootDirectoryName, rootEntry);
            updateTree(rootDirectoryName, rootEntry);
        }
        
    }

}
