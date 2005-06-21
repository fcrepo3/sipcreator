package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fedora.services.sipcreator.SelectableEntryNode;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.FileSystemEntry;
import fedora.services.sipcreator.SelectableEntry;
import fedora.services.sipcreator.acceptor.UniversalAcceptor;
import fedora.services.sipcreator.utility.CheckRenderer;
import fedora.services.sipcreator.utility.GUIUtility;

public class FileSelectTask extends JPanel {

    private static final long serialVersionUID = 4051332249108427830L;
    
    private UniversalAcceptor acceptor = new UniversalAcceptor();
    
    private ChangeDirectoryAction changeDirectoryAction = new ChangeDirectoryAction();
    private EventHandler eventHandler = new EventHandler();
    
    //Data structures and UI components involved with the file browsing task
    private JLabel fileSelectDirectoryLabel = new JLabel();
    private CheckRenderer fileSelectTreeRenderer = new CheckRenderer();
    private DefaultTreeModel fileSelectTreeModel = new DefaultTreeModel(null);
    private JTree fileSelectTreeDisplay = new JTree(fileSelectTreeModel);
    
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

        JPanel tempP1 = new JPanel(new BorderLayout());
        
        tempP1.add(fileSelectDirectoryLabel, BorderLayout.CENTER);
        tempP1.add(new JButton(changeDirectoryAction), BorderLayout.EAST);
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(tempP1, BorderLayout.NORTH);
        add(new JScrollPane(fileSelectTreeDisplay), BorderLayout.CENTER);
    }

    
    public void updateTree(String rootDirectoryName, SelectableEntry newRoot) {
        fileSelectDirectoryLabel.setText(rootDirectoryName);
        fileSelectTreeModel.setRoot(new SelectableEntryNode(newRoot, null, acceptor));
    }
    
    public void refreshTree() {
        fileSelectTreeModel.nodeStructureChanged((TreeNode)fileSelectTreeModel.getRoot());
    }
    
    public SelectableEntry getRootEntry() {
        SelectableEntryNode rootNode = (SelectableEntryNode)fileSelectTreeModel.getRoot();
        return (rootNode == null ? null : rootNode.getEntry());
    }
    
    public void setEnabled(boolean newEnabled) {
        super.setEnabled(newEnabled);

        changeDirectoryAction.setEnabled(newEnabled);
    }
    
    
    private class EventHandler extends MouseAdapter {
        
        public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() > 1) return;
            if (!isEnabled()) return;
            
            int x = me.getX();
            int y = me.getY();
            
            TreePath path = fileSelectTreeDisplay.getPathForLocation(x, y);
            if (path == null) return;
            
            //The next two lines ensure that the user clicked on the checkbox, not the
            //icon or the text.  The "-2" is a hack, but seems to work
            Rectangle bounds = fileSelectTreeDisplay.getPathBounds(path);
            if (x > bounds.x + fileSelectTreeRenderer.getCheckBoxWidth() - 2) return;
            
            SelectableEntryNode node = (SelectableEntryNode)path.getLastPathComponent();
            boolean fullySelected = node.getEntry().getSelectionLevel() == FileSystemEntry.FULLY_SELECTED; 
            int selectionLevel = fullySelected ? FileSystemEntry.UNSELECTED : FileSystemEntry.FULLY_SELECTED;
            node.getEntry().setSelectionLevel(selectionLevel, acceptor);
            
            SelectableEntryNode nodeParent = (SelectableEntryNode)node.getParent();
            if (nodeParent != null) {
                nodeParent.getEntry().setSelectionLevelFromChildren(acceptor);
            }
            
            fileSelectTreeModel.nodeChanged(node);
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
            
            File file = fileChooser.getSelectedFile();
            FileSystemEntry rootEntry;
            String rootDirectoryName;
            
            if (file.getName().equalsIgnoreCase("METS.xml")) {
                JOptionPane.showMessageDialog(parent,
                        "The root directory cannot use METS.xml as a name, " +
                        "that name is reserved for the descriptive metadata file.");
                return;
            }
            
            if (fileSelectTreeModel.getRoot() != null) {
                choice = JOptionPane.showConfirmDialog(parent,
                        "This change will erase all metadata.  Continue?",
                        "Warning", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) return;
            }
            
            try {
                rootDirectoryName = file.getCanonicalPath();
                rootEntry = new FileSystemEntry(file, null, parent);
                rootEntry.setSelectionLevel(FileSystemEntry.UNSELECTED, acceptor);
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
