package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import fedora.services.sipcreator.SIPEntryPanel;
import fedora.services.sipcreator.acceptor.FilterAcceptor;
import fedora.services.sipcreator.acceptor.IntersectionAcceptor;
import fedora.services.sipcreator.acceptor.SelectionAcceptor;
import fedora.services.sipcreator.utility.GUIUtility;
import fedora.services.sipcreator.utility.PopupListener;

public class MetadataEntryTask extends JPanel {

    private static final long serialVersionUID = 3257850999698698808L;
    
    private FilterAction filterAction = new FilterAction();
    private EventHandler eventHandler = new EventHandler();
    
    private FilterAcceptor filterAcceptor = new FilterAcceptor();
    private IntersectionAcceptor acceptor = new IntersectionAcceptor(); 
    
    //Data structures and UI components involved with the metadata entry task
    private JLabel metadataDirectoryLabel = new JLabel();
    private DefaultTreeModel metadataTreeModel = new DefaultTreeModel(null);
    private JTree metadataTreeDisplay = new JTree(metadataTreeModel);

    private JCheckBox filterEnabledBox = new JCheckBox(filterAction);
    private JTextField filterField = new JTextField();
    
    private SIPCreator parent;
    
    public MetadataEntryTask(SIPCreator newParent) {
        parent = newParent;
        
        acceptor.getAcceptorList().add(filterAcceptor);
        acceptor.getAcceptorList().add(new SelectionAcceptor(SIPEntry.FULLY_SELECTED | SIPEntry.PARTIALLY_SELECTED));
        
        metadataTreeDisplay.addMouseListener(eventHandler);
        metadataTreeDisplay.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        metadataTreeDisplay.addMouseListener(new PopupListener(createPopupMenu()));
        
        filterField.addActionListener(filterAction);
        
        //Minimum sizes are explicitly set so that labels with long text entries
        //will not keep the containing JSplitPane from resizing down past the point
        //at which all the text on the label is visible
        metadataDirectoryLabel.setMinimumSize(new Dimension(1, 1));
        
        JPanel tempP1 = new JPanel(new GridLayout(2, 1, 5, 5));
        tempP1.add(metadataDirectoryLabel);
        
        JPanel tempP2 = new JPanel(new BorderLayout());
        tempP2.add(filterEnabledBox, BorderLayout.EAST);
        tempP2.add(filterField, BorderLayout.CENTER);
        tempP1.add(tempP2);
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(tempP1, BorderLayout.NORTH);
        add(new JScrollPane(metadataTreeDisplay), BorderLayout.CENTER);
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu result = new JPopupMenu();
        
        return result;
    }
    
    
    public void updateTree(String rootDirectoryName, SIPEntry newRoot) {
        metadataDirectoryLabel.setText(rootDirectoryName);
        metadataDirectoryLabel.setToolTipText(rootDirectoryName);
        metadataTreeModel.setRoot(new FileTreeNode(newRoot, null, acceptor));
    }
    
    public void refreshTree() {
        metadataTreeModel.nodeStructureChanged((FileTreeNode)metadataTreeModel.getRoot());
    }
    
    
    private class EventHandler extends MouseAdapter {
        
        public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() != 2) return;
            
            TreePath path = metadataTreeDisplay.getSelectionPath();
            if (path == null) return;
            if (!(path.getLastPathComponent() instanceof FileTreeNode)) return;
            FileTreeNode node = (FileTreeNode)path.getLastPathComponent();
            
            String canonicalPath = "";
            try {
                canonicalPath = node.getEntry().getFile().getCanonicalPath();
            } catch (IOException ioe) {
                GUIUtility.showExceptionDialog(parent, ioe);
            }
            
            int index;
            JTabbedPane rightPanel = parent.getRightPanel();
            for (index = 0; index < rightPanel.getTabCount(); index++) {
                if (rightPanel.getToolTipTextAt(index).equals(canonicalPath)) {
                    break;
                }
            }
            
            if (index == rightPanel.getTabCount()) {
                SIPEntryPanel listPanel = new SIPEntryPanel(node.getEntry(), parent);
                rightPanel.addTab(node.toString(), null, listPanel, canonicalPath);
                rightPanel.setSelectedComponent(listPanel);
            } else {
                rightPanel.setSelectedIndex(index);
            }
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
            
            filterAcceptor.setFilter(text);
            filterAcceptor.setEnabled(filterEnabledBox.isSelected());
            
            refreshTree();
        }
        
    }
    
}
