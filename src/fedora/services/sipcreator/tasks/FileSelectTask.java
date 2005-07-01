package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Hashtable;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fedora.services.sipcreator.FileSystemEntry;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.SelectableEntry;
import fedora.services.sipcreator.SelectableEntryNode;
import fedora.services.sipcreator.ZipFileEntry;
import fedora.services.sipcreator.acceptor.UniversalAcceptor;
import fedora.services.sipcreator.metadata.Metadata;
import fedora.services.sipcreator.metadata.MinimalMetadata;
import fedora.services.sipcreator.utility.CheckRenderer;
import fedora.services.sipcreator.utility.DOMUtility;
import fedora.services.sipcreator.utility.GUIUtility;

public class FileSelectTask extends JPanel {

    private static final long serialVersionUID = 4051332249108427830L;
    
    public static final String METS_NS = "http://www.loc.gov/METS/";
    
    private UniversalAcceptor acceptor = new UniversalAcceptor();
    
    private OpenFileAction openFileAction = new OpenFileAction();
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
        tempP1.add(new JButton(openFileAction), BorderLayout.EAST);
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(tempP1, BorderLayout.NORTH);
        add(new JScrollPane(fileSelectTreeDisplay), BorderLayout.CENTER);
    }

    
    public void updateTree(String rootDirectoryName, SelectableEntry newRoot) {
        fileSelectDirectoryLabel.setText(rootDirectoryName);
        if (newRoot == null) {
            fileSelectTreeModel.setRoot(null);
        } else {
            fileSelectTreeModel.setRoot(new SelectableEntryNode(newRoot, null, acceptor));
        }
    }
    
    public void refreshTree() {
        fileSelectTreeModel.nodeStructureChanged((TreeNode)fileSelectTreeModel.getRoot());
    }
    
    public SelectableEntry getRootEntry() {
        SelectableEntryNode rootNode = (SelectableEntryNode)fileSelectTreeModel.getRoot();
        return (rootNode == null ? null : rootNode.getEntry());
    }
    
    public OpenFileAction getOpenFileAction() {
        return openFileAction;
    }
    
    
    private class EventHandler extends MouseAdapter {
        
        public void mouseClicked(MouseEvent me) {
            try {
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
            } catch (Exception e) {
                GUIUtility.showExceptionDialog(parent, e);
            }
        }
        
    }

    public class OpenFileAction extends AbstractAction {

        private static final long serialVersionUID = 3763096349595678519L;

        public OpenFileAction() {
            putValue(Action.NAME, "Open File");
            putValue(Action.SHORT_DESCRIPTION, "Changes the selected root directory/zip file");
        }
        
        
        public void actionPerformed(ActionEvent ae) {
            JFileChooser fileChooser = parent.getFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            int choice = fileChooser.showOpenDialog(parent);
            if (choice != JFileChooser.APPROVE_OPTION) return;
            
            File file = fileChooser.getSelectedFile();
            
            if (fileSelectTreeModel.getRoot() != null) {
                choice = JOptionPane.showConfirmDialog(parent,
                        "This change will erase all metadata.  Continue?",
                        "Warning", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) return;
            }
            
            try {
                if (file.isDirectory()) {
                    openDirectory(file);
                } else {
                    openZipFile(file);
                }
            } catch (Exception e) {
                GUIUtility.showExceptionDialog(parent, e);
                return;
            }
        }
        
        public void openDirectory(File file) throws IOException {
            SelectableEntry rootEntry;
            String rootDirectoryName;
            
            if (file.getName().equalsIgnoreCase("METS.xml")) {
                JOptionPane.showMessageDialog(parent,
                        "The root directory cannot use METS.xml as a name, " +
                        "that name is reserved for the descriptive metadata file.");
                return;
            }
            
            rootDirectoryName = file.getCanonicalPath();
            rootEntry = new FileSystemEntry(file, null, parent);
            rootEntry.setSelectionLevel(FileSystemEntry.UNSELECTED, acceptor);

            JTabbedPane rightPanel = parent.getRightPanel();
            while (rightPanel.getTabCount() > 0) {
                rightPanel.remove(0);
            }
            parent.getMetadataEntryTask().updateTree(rootDirectoryName, rootEntry);
            updateTree(rootDirectoryName, rootEntry);
            System.gc();
        }
        
        public void openZipFile(File file) throws IOException, SAXException {
            SelectableEntry rootEntry;
            String rootDirectoryName;
            
            rootDirectoryName = file.getCanonicalPath();
            rootEntry = handleZipFile(new ZipFile(file));
            rootEntry.setSelectionLevel(FileSystemEntry.FULLY_SELECTED, acceptor);

            JTabbedPane rightPanel = parent.getRightPanel();
            while (rightPanel.getTabCount() > 0) {
                rightPanel.remove(0);
            }
            parent.getMetadataEntryTask().updateTree(rootDirectoryName, rootEntry);
            updateTree(rootDirectoryName, rootEntry);
            System.gc();
        }

        
        private ZipFileEntry handleZipFile(ZipFile zipFile) throws IOException, SAXException {
            System.out.println(zipFile.getName());
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
            
            Element metsNode = metsDocument.getDocumentElement();
            Element fileSecNode = DOMUtility.firstElementNamed(metsNode, METS_NS, "fileSec");
            
            Hashtable metadataTable = new Hashtable();
            //addMetadataToTable(metadataTable, metsNode.getElementsByTagNameNS(METS_NS, "dmdSec"), "mdWrap", "MDTYPE");
            addMetadataToTable(metadataTable, fileSecNode.getElementsByTagNameNS(METS_NS, "file"));
            
            Element structMapNode = DOMUtility.firstElementNamed(metsNode, METS_NS, "structMap");
            Element rootDiv = DOMUtility.firstElementNamed(structMapNode, METS_NS, "div");
            traverseStructMap(rootNode, rootDiv, metadataTable);
        }
        
        private void traverseStructMap(ZipFileEntry currentEntry, Element currentDiv, Hashtable mdTable) {
//            if (currentEntry.getParent() == null) {
//                String dmdidField = currentDiv.getAttribute("DMDID");
//                StringTokenizer tokenizer = new StringTokenizer(dmdidField);
//                
//                while (tokenizer.hasMoreElements()) {
//                    try {
//                        String token = tokenizer.nextToken();
//                        Object metadata = mdTable.get(token);
//                        if (metadata == null) {
//                            continue;
//                        }
//                        currentEntry.getMetadata().add(metadata);
//                    } catch (Exception e) {}
//                }
//            }
//            
            currentEntry.setLabel(currentDiv.getAttribute("LABEL"));
//            String id = currentDiv.getAttribute("CONTENTIDS");
//            if (mdTable.get(id) != null) {
//                currentEntry.setMimeType(mdTable.get(id).toString());
//            }
            
            NodeList childList = currentDiv.getChildNodes();
            for (int ctr = 0; ctr < childList.getLength(); ctr++) {
                try {
                    Element currentNode = (Element)childList.item(ctr);
                    Element child = DOMUtility.firstElementNamed(currentNode, METS_NS, "fptr", false);
                    
                    if (child != null) {
                        String fileid = child.getAttribute("FILEID");
                        
                        if (currentNode.getAttribute("TYPE").equals("content") &&
                                mdTable.get(child.getAttribute("FILEID")) != null) {
                            currentEntry.setMimeType(mdTable.get(child.getAttribute("FILEID")).toString());
                        }
                        
                        Metadata metadata = (Metadata)mdTable.get(fileid);
                        metadata.setLabel(currentNode.getAttribute("LABEL"));
                        metadata.setType(currentNode.getAttribute("TYPE"));
                        
                        currentEntry.getMetadata().add(metadata);
                        currentEntry.setLabel(currentDiv.getAttribute("LABEL"));
                    } else {
                        String name = currentNode.getAttribute("ID");
                        traverseStructMap(currentEntry.getChild(name), currentNode, mdTable);
                    }
                } catch (Exception e) {}
            }
        }
        
        private void addMetadataToTable(Hashtable table, NodeList childList) {
            for (int ctr = 0; ctr < childList.getLength(); ctr++) {
                try {
                    Element current = (Element)childList.item(ctr);
                    Element firstChild = DOMUtility.firstElementNamed(current, METS_NS, "FContent", false);
                    if (firstChild == null) {
                        //Ensures that this file entry contains an FLocat subelement
                        DOMUtility.firstElementNamed(current, METS_NS, "FLocat");
                        table.put(current.getAttribute("ID"), current.getAttribute("MIMETYPE"));
                    }
                            
                    String id = current.getAttribute("ID");
                    String mdType = firstChild.getAttribute("USE");
                    String label = firstChild.getAttribute("LABEL");
                    String type = firstChild.getAttribute("OTHERMDTYPE");
                            
                    try {
                        Class mdClass = Class.forName(mdType);
                        Constructor constructor = mdClass.getConstructor(new Class[]{Element.class});
                        Metadata metadata = (Metadata)constructor.newInstance(new Object[]{current});
                        metadata.setLabel(label);
                        metadata.setType(type);
                        table.put(id, metadata);
                    } catch (Exception e) {
                        Metadata metadata = new MinimalMetadata(current);
                        metadata.setLabel(label);
                        metadata.setType(type);
                        table.put(id, metadata);
                    }
                } catch (Exception e) {}
            }
        }
        
    }
    
}
