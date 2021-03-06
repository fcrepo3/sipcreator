package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import beowulf.gui.Utility;
import beowulf.util.DOMUtility;
import fedora.services.sipcreator.Constants;
import fedora.services.sipcreator.ConversionRules;
import fedora.services.sipcreator.FileSystemEntry;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.SelectableEntry;
import fedora.services.sipcreator.SelectableEntryNode;
import fedora.services.sipcreator.ZipFileEntry;
import fedora.services.sipcreator.acceptor.UniversalAcceptor;
import fedora.services.sipcreator.metadata.Metadata;
import fedora.services.sipcreator.metadata.MinimalMetadata;
import fedora.services.sipcreator.utility.CheckRenderer;

public class FileSelectTask extends JPanel implements Constants {

    private static final long serialVersionUID = 4051332249108427830L;
    
    public static final String METS_NS = "http://www.loc.gov/METS/";
    
    private UniversalAcceptor acceptor = new UniversalAcceptor();
    
    private OpenFolderAction openFolderAction;
    private OpenZipFileAction openZipFileAction;
    private EventHandler eventHandler = new EventHandler();
    
    //Data structures and UI components involved with the file browsing task
    private CheckRenderer fileSelectTreeRenderer;
    private DefaultTreeModel fileSelectTreeModel = new DefaultTreeModel(null);
    private JTree fileSelectTreeDisplay = new JTree(fileSelectTreeModel);
    
    private SIPCreator creator;
    
    public FileSelectTask(SIPCreator newCreator) {
        creator = newCreator;
        
        fileSelectTreeRenderer = new CheckRenderer(creator);
        
        openFolderAction = new OpenFolderAction();
        openZipFileAction = new OpenZipFileAction();
        
        fileSelectTreeDisplay.setCellRenderer(fileSelectTreeRenderer);
        fileSelectTreeDisplay.addMouseListener(eventHandler);
        fileSelectTreeDisplay.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        JPanel tempP1 = new JPanel(new BorderLayout());
        tempP1.add(new JPanel(), BorderLayout.CENTER);
        JPanel tempP2 = new JPanel(new GridLayout(1, 0));
        tempP2.add(new JButton(openFolderAction));
        tempP2.add(new JButton(openZipFileAction));
        tempP1.add(tempP2, BorderLayout.EAST);
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(tempP1, BorderLayout.NORTH);
        add(new JScrollPane(fileSelectTreeDisplay), BorderLayout.CENTER);
    }

    
    public void updateTree(SelectableEntry newRoot) {
        if (newRoot == null) {
            fileSelectTreeModel.setRoot(null);
        } else {
            fileSelectTreeModel.setRoot(new SelectableEntryNode(newRoot, null, acceptor, fileSelectTreeModel));
        }
    }
    
    public void refreshTree() {
        fileSelectTreeModel.nodeStructureChanged((TreeNode)fileSelectTreeModel.getRoot());
    }
    
    public SelectableEntry getRootEntry() {
        SelectableEntryNode rootNode = (SelectableEntryNode)fileSelectTreeModel.getRoot();
        return (rootNode == null ? null : rootNode.getEntry());
    }
    
    public OpenFolderAction getOpenFolderAction() {
        return openFolderAction;
    }
    
    public OpenZipFileAction getOpenZipFileAction() {
        return openZipFileAction;
    }
    
    
    private class ExploreChildren implements Runnable {
        
        public void run() {
            Vector queue = new Vector();
            queue.add(getRootEntry());
            
            creator.getProgressBar().setIndeterminate(true);
            
            while (queue.size() > 0) {
                SelectableEntry entry = (SelectableEntry)queue.remove(0);
                
                for (int ctr = 0; ctr < entry.getChildCount(acceptor); ctr++) {
                    queue.add(entry.getChildAt(ctr, acceptor));
                    Thread.yield();
                }
                
                creator.getProgressBar().setValue(1);
            }
            
            creator.getProgressBar().setIndeterminate(false);
            creator.getProgressBar().setMaximum(1);
            creator.getProgressBar().setMinimum(0);
        }
        
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
                int previousBaseSelection = node.getEntry().getSelectionLevel(); 
                boolean fullySelected = previousBaseSelection == SelectableEntry.FULLY_SELECTED; 
                int selectionLevel = fullySelected ? SelectableEntry.UNSELECTED : SelectableEntry.FULLY_SELECTED;
                node.getEntry().setSelectionLevel(selectionLevel, acceptor);

                SelectableEntryNode parent = (SelectableEntryNode)node.getParent();
                if (parent != null) {
                    parent.getEntry().setSelectionLevelFromChildren(acceptor);
                }
                fileSelectTreeModel.nodeChanged(node);
                creator.getMetadataEntryTask().refreshTree();
            } catch (Exception e) {
                Utility.showExceptionDialog(creator, e);
            }
        }
        
    }

    public class OpenFolderAction extends AbstractAction {

        private static final long serialVersionUID = 3763096349595678519L;
        
        private OpenFolderAction() {
            //putValue(Action.NAME, "Open File");
            URL imgURL = creator.getURL(FOLDER_IMAGE_NAME);
            putValue(Action.SMALL_ICON, new ImageIcon(creator.getImage(imgURL)));
            putValue(Action.SHORT_DESCRIPTION, "Changes the selected root directory file");
        }
        
        
        public void actionPerformed(ActionEvent ae) {
            JFileChooser fileChooser = creator.getFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int choice = fileChooser.showOpenDialog(creator);
            if (choice != JFileChooser.APPROVE_OPTION) return;
            
            File file = fileChooser.getSelectedFile();
            
            if (fileSelectTreeModel.getRoot() != null) {
                choice = JOptionPane.showConfirmDialog(creator,
                        "This change will erase all metadata.  Continue?",
                        "Warning", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) return;
            }
            
            try {
                if (file.isDirectory()) {
                    openDirectory(file);
                    ExploreChildren explorer = new ExploreChildren();
                    Thread t = new Thread(explorer, "FileSystemExplorer");
                    t.setPriority(Thread.MIN_PRIORITY);
                    t.start();
                } else {
                    JOptionPane.showMessageDialog(creator, "You must choose a directory to open!");
                }
                
                creator.setFileLabelText(file.getCanonicalPath());
            } catch (Exception e) {
                Utility.showExceptionDialog(creator, e);
                return;
            }
        }
        
        public void openDirectory(File file) {
            SelectableEntry rootEntry;
            
            if (file.getName().equalsIgnoreCase("METS.xml")) {
                JOptionPane.showMessageDialog(creator,
                        "The root directory cannot use METS.xml as a name, " +
                        "that name is reserved for the descriptive metadata file.");
                return;
            }
            
            rootEntry = new FileSystemEntry(file, null, creator);
            rootEntry.setSelectionLevel(FileSystemEntry.UNSELECTED, acceptor);

            creator.getMetadataEntryTask().closeAllTabs();
            creator.getMetadataEntryTask().updateTree(rootEntry);
            updateTree(rootEntry);
            System.gc();
        }
        
    }
    
    public class OpenZipFileAction extends AbstractAction {
        
        private static final long serialVersionUID = 3937836713440462314L;

        private OpenZipFileAction() {
            //putValue(Action.NAME, "Open ZIP");
            URL imgURL = creator.getURL(ZIP_FILE_IMAGE_NAME);
            putValue(Action.SMALL_ICON, new ImageIcon(creator.getImage(imgURL)));
            putValue(Action.SHORT_DESCRIPTION, "Open a previously saved SIP file");
        }
        
        public void actionPerformed(ActionEvent ae) {
            JFileChooser fileChooser = creator.getFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(creator.getZIPFilter());

            int choice = fileChooser.showOpenDialog(creator);
            if (choice != JFileChooser.APPROVE_OPTION) return;
            
            File file = fileChooser.getSelectedFile();
            
            if (fileSelectTreeModel.getRoot() != null) {
                choice = JOptionPane.showConfirmDialog(creator,
                        "This change will erase all metadata.  Continue?",
                        "Warning", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) return;
            }
            
            try {
                openZipFile(file);
                creator.setFileLabelText(file.getCanonicalPath());
            } catch (Exception e) {
                Utility.showExceptionDialog(creator, e);
                return;
            }
        }
        
        public void openZipFile(File file) throws IOException, SAXException {
            SelectableEntry rootEntry;
            
            rootEntry = handleZipFile(new ZipFile(file));
            rootEntry.setSelectionLevel(FileSystemEntry.FULLY_SELECTED, acceptor);

            creator.getMetadataEntryTask().closeAllTabs();
            creator.getMetadataEntryTask().updateTree(rootEntry);
            updateTree(rootEntry);
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
                if (name.equals("METS.xml") || name.equals("crules.xml")) {
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
            
            handleCRules(zipFile);
            
            return rootNode;
        }

        private void handleCRules(ZipFile zipFile) throws IOException, SAXException {
            ZipEntry crulesEntry = zipFile.getEntry("crules.xml");
            InputSource is = new InputSource(zipFile.getInputStream(crulesEntry));
            Document crulesDocument = creator.parseXML(is);
            
            creator.getConversionRulesTask().updateRules("crules.xml", new ConversionRules(crulesDocument));
        }
        
        private void handleMETS(ZipFile zipFile, ZipFileEntry rootNode) throws IOException, SAXException {
            ZipEntry metsEntry = zipFile.getEntry("METS.xml");
            InputSource is = new InputSource(zipFile.getInputStream(metsEntry));
            Document metsDocument = creator.parseXML(is);
            
            Element metsNode = metsDocument.getDocumentElement();
            Element fileSecNode = DOMUtility.firstElementNamed(metsNode, METS_NS, "fileSec");
            
            Hashtable metadataTable = new Hashtable();
            addMetadataToTable(metadataTable, fileSecNode.getElementsByTagNameNS(METS_NS, "file"));
            
            Element structMapNode = DOMUtility.firstElementNamed(metsNode, METS_NS, "structMap");
            Element rootDiv = DOMUtility.firstElementNamed(structMapNode, METS_NS, "div");
            traverseStructMap(rootNode, rootDiv, metadataTable);
        }
        
        private void traverseStructMap(ZipFileEntry currentEntry, Element currentDiv, Hashtable mdTable) {
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
                        
                        currentEntry.addMetadata(metadata);
                    } else {
                        String name = currentNode.getAttribute("LABEL");
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
