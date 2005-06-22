package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
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

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fedora.services.sipcreator.FileSystemEntry;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.SelectableEntry;
import fedora.services.sipcreator.SelectableEntryNode;
import fedora.services.sipcreator.SelectableEntryPanel;
import fedora.services.sipcreator.acceptor.FilterAcceptor;
import fedora.services.sipcreator.acceptor.IntersectionAcceptor;
import fedora.services.sipcreator.acceptor.SelectionAcceptor;
import fedora.services.sipcreator.utility.GUIUtility;
import fedora.services.sipcreator.utility.PopupListener;

public class MetadataEntryTask extends JPanel {

    private static final long serialVersionUID = 3257850999698698808L;
    
    private FilterAction filterAction = new FilterAction();
    private AddMetadataAction addMetadataAction = new AddMetadataAction();
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
        acceptor.getAcceptorList().add(new SelectionAcceptor(FileSystemEntry.FULLY_SELECTED | FileSystemEntry.PARTIALLY_SELECTED));
        
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
    
    
    public void updateTree(String rootDirectoryName, SelectableEntry newRoot) {
        metadataDirectoryLabel.setText(rootDirectoryName);
        metadataDirectoryLabel.setToolTipText(rootDirectoryName);
        metadataTreeModel.setRoot(new SelectableEntryNode(newRoot, null, acceptor));
    }
    
    public void refreshTree() {
        metadataTreeModel.nodeStructureChanged((SelectableEntryNode)metadataTreeModel.getRoot());
    }
    
    public AddMetadataAction getAddMetadataAction() {
        return addMetadataAction;
    }
    
    
    private class EventHandler extends MouseAdapter {
        
        public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() != 2) return;
            
            TreePath path = metadataTreeDisplay.getSelectionPath();
            if (path == null) return;
            if (!(path.getLastPathComponent() instanceof SelectableEntryNode)) return;
            SelectableEntryNode node = (SelectableEntryNode)path.getLastPathComponent();
            
            int index;
            JTabbedPane rightPanel = parent.getRightPanel();
            for (index = 0; index < rightPanel.getTabCount(); index++) {
                if (rightPanel.getToolTipTextAt(index).equals(node.getEntry().toString())) {
                    break;
                }
            }
            
            if (index == rightPanel.getTabCount()) {
                SelectableEntryPanel listPanel = new SelectableEntryPanel(node.getEntry(), parent);
                rightPanel.addTab(node.toString(), null, listPanel, node.getEntry().toString());
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
    
    private class AddMetadataAction extends AbstractAction {
        
        private static final long serialVersionUID = 3690479112745529654L;

        public AddMetadataAction() {
            putValue(Action.NAME, "Add Metadata");
            putValue(Action.SHORT_DESCRIPTION, "Adds in the metadata in a METS.xml file");
        }
        
        public void actionPerformed(ActionEvent ae) {
            JFileChooser fileChooser = parent.getFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            int choice = fileChooser.showOpenDialog(parent);
            if (choice != JFileChooser.APPROVE_OPTION) return;
            
            try {
                addMetadata(new FileInputStream(fileChooser.getSelectedFile()));
            } catch (Exception e) {
                GUIUtility.showExceptionDialog(parent, e);
            }
        }
        
        public void addMetadata(InputStream stream) throws IOException, SAXException {
            Document xmlDocument = parent.getXMLParser().parse(stream);
        }
        
    }
    
}
