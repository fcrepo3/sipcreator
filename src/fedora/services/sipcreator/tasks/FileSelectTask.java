package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fedora.services.sipcreator.FileSystemEntry;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.SelectableEntry;
import fedora.services.sipcreator.SelectableEntryNode;
import fedora.services.sipcreator.ZipFileEntry;
import fedora.services.sipcreator.acceptor.UniversalAcceptor;
import fedora.services.sipcreator.utility.CheckRenderer;
import fedora.services.sipcreator.utility.GUIUtility;

public class FileSelectTask extends JPanel {

    private static final long serialVersionUID = 4051332249108427830L;
    
    public static final String METS_NS = "http://www.loc.gov/METS/";
    
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

        public ChangeDirectoryAction() {
            putValue(Action.NAME, "Browse");
            putValue(Action.SHORT_DESCRIPTION, "Changes the selected root directory");
        }
        
        public void actionPerformed(ActionEvent ae) {
            JFileChooser fileChooser = parent.getFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            int choice = fileChooser.showOpenDialog(parent);
            if (choice != JFileChooser.APPROVE_OPTION) return;
            
            File file = fileChooser.getSelectedFile();
            SelectableEntry rootEntry;
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
                if (file.isDirectory()) {
                    rootEntry = new FileSystemEntry(file, null, parent);
                    rootEntry.setSelectionLevel(FileSystemEntry.UNSELECTED, acceptor);
                } else {
                    rootEntry = handleZipFile(new ZipFile(file));
                    rootEntry.setSelectionLevel(FileSystemEntry.FULLY_SELECTED, acceptor);
                }
            } catch (Exception e) {
                GUIUtility.showExceptionDialog(parent, e);
                return;
            }

            JTabbedPane rightPanel = parent.getRightPanel();
            while (rightPanel.getTabCount() > 0) {
                rightPanel.remove(0);
            }
            parent.getMetadataEntryTask().updateTree(rootDirectoryName, rootEntry);
            updateTree(rootDirectoryName, rootEntry);
            System.gc();
        }
        
        private ZipFileEntry handleZipFile(ZipFile zipFile) throws IOException, SAXException {
            Enumeration entryEnumeration = zipFile.entries();
            ZipEntry currentEntry;
            ZipFileEntry rootNode = null;
            ZipFileEntry currentNode;
            
            //While there are entries in the zip file, grab the next entry
            while (entryEnumeration.hasMoreElements()) {
                currentEntry = (ZipEntry)entryEnumeration.nextElement();
                String name = currentEntry.getName().replaceAll("\\\\", "/");
                StringTokenizer tokenizer = new StringTokenizer(name, "/");

                //If the entry is the METS.xml file, handle that separately
                if (name.equalsIgnoreCase("METS.xml")) {
                    continue;
                }
                
                //If the root doesn't exist, figure out what it is from the current entry
                if (rootNode == null) {
                    rootNode = new ZipFileEntry(zipFile, currentEntry, null);
                    continue;
                }
                
                //Set the "current parent" to the root
                currentNode = rootNode;
                //Throw away the root's name
                tokenizer.nextToken();
                String nameElement = tokenizer.nextToken();
                
                //While the next file name isn't the leaf of the path
                while (tokenizer.hasMoreElements()) {
                    //set the current parent to be the next parent
                    currentNode = currentNode.getChild(nameElement);
                    nameElement = tokenizer.nextToken();
                }
                
                //Add the current entry as a child of the current entry
                currentNode.addChild(new ZipFileEntry(zipFile, currentEntry, currentNode));
            }
            
            handleMETS(zipFile, rootNode);
            
            return rootNode;
        }

        private void handleMETS(ZipFile zipFile, ZipFileEntry rootNode) throws IOException, SAXException {
            ZipEntry metsEntry = zipFile.getEntry("METS.xml");
            Document metsDocument = parent.getXMLParser().parse(zipFile.getInputStream(metsEntry));
            
            Element metsNode = (Element)metsDocument.getElementsByTagNameNS(METS_NS, "mets").item(0);
            Element dmdSecNode = (Element)metsNode.getElementsByTagNameNS(METS_NS, "dmdSec");
            Element fileSecNode = (Element)metsNode.getElementsByTagNameNS(METS_NS, "fileSec");
            Element structMapNode = (Element)metsNode.getElementsByTagNameNS(METS_NS, "structMap");
        }
        
    }
    
}
