package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import beowulf.gui.PopupListener;
import beowulf.gui.Utility;
import beowulf.util.StreamUtility;
import fedora.services.sipcreator.Constants;
import fedora.services.sipcreator.ConversionRules;
import fedora.services.sipcreator.FileSystemEntry;
import fedora.services.sipcreator.MetadataNode;
import fedora.services.sipcreator.MetadataPanelWrapper;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.SelectableEntry;
import fedora.services.sipcreator.SelectableEntryNode;
import fedora.services.sipcreator.SelectableEntryPanel;
import fedora.services.sipcreator.ZipFileEntry;
import fedora.services.sipcreator.acceptor.FilterAcceptor;
import fedora.services.sipcreator.acceptor.IntersectionAcceptor;
import fedora.services.sipcreator.acceptor.SelectionAcceptor;
import fedora.services.sipcreator.metadata.Metadata;
import fedora.services.sipcreator.utility.CheckRenderer;

public class MetadataEntryTask extends JPanel implements Constants {

    /** */
    private static final long serialVersionUID = 3257850999698698808L;
    
    private FilterAction filterAction;
    private RemoveMetadataAction removeMetadataAction;
    private AddMetadataAction addMetadataAction;
    private SaveSIPAction saveSIPAction;
    private CloseCurrentTabAction closeCurrentTabAction;
    private EventHandler eventHandler = new EventHandler();
    
    private FilterAcceptor filterAcceptor = new FilterAcceptor();
    private IntersectionAcceptor acceptor = new IntersectionAcceptor(); 
    
    //Data structures and UI components involved with the metadata entry task
    private DefaultTreeModel metadataTreeModel = new DefaultTreeModel(null);
    private JTree metadataTreeDisplay = new JTree(metadataTreeModel);

    private JCheckBox filterEnabledBox;
    private JTextField filterField = new JTextField();
    private JTabbedPane metadataView = new JTabbedPane();
    
    private SIPCreator creator;
    
    public MetadataEntryTask(SIPCreator newCreator) {
        creator = newCreator;
        
        filterAction = new FilterAction();
        removeMetadataAction = new RemoveMetadataAction();
        addMetadataAction = new AddMetadataAction();
        saveSIPAction = new SaveSIPAction();
        closeCurrentTabAction = new CloseCurrentTabAction();
        
        acceptor.addAcceptor(filterAcceptor);
        acceptor.addAcceptor(new SelectionAcceptor(FileSystemEntry.FULLY_SELECTED | FileSystemEntry.PARTIALLY_SELECTED));
        acceptor.setAcceptsMetadata(true);
        
        metadataTreeDisplay.addMouseListener(eventHandler);
        metadataTreeDisplay.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        metadataTreeDisplay.addMouseListener(new PopupListener(createPopupMenu()));
        metadataTreeDisplay.setCellRenderer(new CheckRenderer(false, creator));
        
        filterEnabledBox = new JCheckBox(filterAction);
        filterField.getDocument().addDocumentListener(filterAction);
        
        JSplitPane centerPane = new JSplitPane();
        centerPane.setLeftComponent(createLeftPanel());
        centerPane.setRightComponent(createRightPanel());
        centerPane.setOneTouchExpandable(true);
        centerPane.setResizeWeight(0.5);
        
        setLayout(new BorderLayout());
        add(centerPane, BorderLayout.CENTER);
    }

    private JComponent createRightPanel() {
        return metadataView;
    }
    
    private JComponent createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        left.add(createNorthPanel(), BorderLayout.NORTH);
        left.add(new JScrollPane(metadataTreeDisplay), BorderLayout.CENTER);
        return left;
    }
    
    private JPanel createNorthPanel() {
        JPanel tempP2 = new JPanel(new GridLayout(1, 0));
        tempP2.add(filterEnabledBox);
        tempP2.add(new JButton(closeCurrentTabAction));
        tempP2.add(new JButton(saveSIPAction));
        
        JPanel result = new JPanel(new BorderLayout());
        result.add(filterField, BorderLayout.CENTER);
        result.add(tempP2, BorderLayout.EAST);
        
        return result;
    }
    
    private JPopupMenu createPopupMenu() {
        JPopupMenu result = new JPopupMenu();
        
        result.add(addMetadataAction);
        result.add(removeMetadataAction);
        
        return result;
    }
    
    
    public void updateTree(SelectableEntry newRoot) {
        metadataTreeModel.setRoot(new SelectableEntryNode(newRoot, null, acceptor, metadataTreeModel));
    }
    
    public void refreshTree() {
        metadataTreeModel.nodeStructureChanged((SelectableEntryNode)metadataTreeModel.getRoot());
    }
    
    public void closeCurrentTab() {
        closeCurrentTabAction.closeCurrentTab();
    }
        
    public void closeAllTabs() {
        while (metadataView.getTabCount() > 0) {
            metadataView.remove(0);
        }
    }
        
    public int getIndexByToolTip(String tip) {
        int index = 0;
        for (index = 0; index < metadataView.getTabCount(); index++) {
            if (metadataView.getToolTipTextAt(index).equals(tip)) {
                return index;
            }
        }
        return -1;
    }
        
    public void updateMetadata() {
        for (int ctr = 0; ctr < metadataView.getTabCount(); ctr++) {
            Component c = metadataView.getComponent(ctr);
            if (c instanceof SelectableEntryPanel) {
                ((SelectableEntryPanel)c).updateMetadata();
            } else if (c instanceof MetadataPanelWrapper) {
                ((MetadataPanelWrapper)c).updateMetadata();
            }
        }
    }
    
    public TreeNode getSelectedNode() {
        TreePath path = metadataTreeDisplay.getSelectionPath();
        if (path == null) {
            return null;
        }
        return (TreeNode)path.getLastPathComponent();
    }
    
    public FilterAction getFilterAction() {
        return filterAction;
    }
    
    public CloseCurrentTabAction getCloseCurrentTabAction() {
        return closeCurrentTabAction;
    }
    
    public SaveSIPAction getSaveSIPAction() {
        return saveSIPAction;
    }
    
    public AddMetadataAction getAddMetadataAction() {
        return addMetadataAction;
    }
    
    public RemoveMetadataAction getRemoveMetadataAction() {
        return removeMetadataAction;
    }
    
    
    private class EventHandler extends MouseAdapter {
        
        public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() != 2) return;
            
            TreePath path = metadataTreeDisplay.getSelectionPath();
            if (path == null) return;
            
            if (path.getLastPathComponent() instanceof SelectableEntryNode) {
                SelectableEntryNode node = (SelectableEntryNode)path.getLastPathComponent();
                int index = getIndexByToolTip(node.getEntry().getDescriptiveName());
                
                if (index == -1) {
                    SelectableEntryPanel listPanel = new SelectableEntryPanel(node.getEntry());
                    metadataView.addTab(node.getEntry().getShortName(), null,
                            listPanel, node.getEntry().getDescriptiveName());
                    metadataView.setSelectedComponent(listPanel);
                } else {
                    metadataView.setSelectedIndex(index);
                }
            } else if (path.getLastPathComponent() instanceof MetadataNode) {
                MetadataNode node = (MetadataNode)path.getLastPathComponent();
                int index = getIndexByToolTip(node.getMetadata().getDescriptiveName());
                
                if (index == -1) {
                    MetadataPanelWrapper panel = new MetadataPanelWrapper(node.getMetadata(), creator);
                    metadataView.addTab(node.getMetadata().getShortName(), null,
                            panel, node.getMetadata().getDescriptiveName());
                    metadataView.setSelectedComponent(panel);
                } else {
                    metadataView.setSelectedIndex(index);
                }
            }
        }
        
    }
    
    public class RemoveMetadataAction extends AbstractAction {
        
        /**  */
        private static final long serialVersionUID = -7240338781145021743L;

        public RemoveMetadataAction() {
            putValue(Action.NAME, "Remove Metadata");
            putValue(Action.SHORT_DESCRIPTION, "Remove metadata from it's parent entry");
        }
        
        public void actionPerformed(ActionEvent ae) {
            try {
                TreeNode lastElement = getSelectedNode();
                if (!(lastElement instanceof MetadataNode)) return;
                Metadata metadata = ((MetadataNode)lastElement).getMetadata();
                SelectableEntry entry = metadata.getEntry();
                metadata.setEntry(null);
                entry.removeMetadata(metadata);
                
                int index = getIndexByToolTip(metadata.getDescriptiveName());
                if (index != -1) {
                    metadataView.removeTabAt(index);
                }
            } catch (Exception e) {
                Utility.showExceptionDialog(creator, e);
            }
        }
        
    }
    
    public class AddMetadataAction extends AbstractAction {
        
        /** */
        private static final long serialVersionUID = 414125917858160620L;

        public AddMetadataAction() {
            putValue(Action.NAME, "Add Metadata");
            putValue(Action.SHORT_DESCRIPTION, "Add new metadata to the selected file");
        }
        
        public void actionPerformed(ActionEvent ae) {
            try {
                TreeNode lastElement = getSelectedNode();
                if (!(lastElement instanceof SelectableEntryNode)) return;
                SelectableEntry entry = ((SelectableEntryNode)lastElement).getEntry();
                
                String msg = "Choose the new metadata's class";
                String title = "Add Metadata";
                Object[] options = creator.getKnownMetadataClasses().keySet().toArray();
                if (options.length == 0) {
                    JOptionPane.showMessageDialog(creator, "No known metadata types, cannot add metadata");
                    return;
                }
                Object selection = JOptionPane.showInputDialog
                (creator, msg, title, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (selection == null) return;
                String selectedClassName = (String)creator.getKnownMetadataClasses().get(selection);
                Class selectedClass = Class.forName(selectedClassName);
                Constructor constructor = selectedClass.getConstructor(null);
                Metadata metadata = (Metadata)constructor.newInstance(null);
                
                addMetadataAction(entry, metadata);
            } catch (Exception e) {
                Utility.showExceptionDialog(creator, e);
            }
        }
        
        public void addMetadataAction(SelectableEntry entry, Metadata newMetadata) {
            ConversionRules rules = creator.getConversionRulesTask().getRules();
            if (rules.getDatastreamTemplateCount() == 0) {
                String msg = "There are currently no datastream templates.  You must add one.\n" +
                "What is the nodeType for the new datastream template?";
                String newDT = JOptionPane.showInputDialog(creator, msg);
                if (newDT == null) return;
                rules.addDatastreamTemplate(new ConversionRules.DatastreamTemplate(newDT));
                creator.getConversionRulesTask().updateRules();
            }
            newMetadata.setType(rules.getDatastreamTemplate(0).getNodeType());
            
            for (int ctr = 0; ctr < entry.getMetadataCount(); ctr++) {
                if (entry.getMetadata(ctr).getType().equals(newMetadata.getType())) {
                    String msg = "There is already a node in the current entry with a\n" +
                    "type of \"" + newMetadata.getType() + "\".  We recommend\n" +
                    "each metadata type unique per entry.";
                    JOptionPane.showMessageDialog(creator, msg);
                    break;
                }
            }
            
            entry.addMetadata(newMetadata);
        }
        
    }
    
    public class CloseCurrentTabAction extends AbstractAction {
        
        private static final long serialVersionUID = -1317113261942287869L;

        private CloseCurrentTabAction() {
            //putValue(Action.NAME, "Close Tab");
            URL imgURL = creator.getURL(CLOSE_IMAGE_NAME);
            putValue(Action.SMALL_ICON, new ImageIcon(creator.getImage(imgURL)));
            putValue(Action.SHORT_DESCRIPTION, "Closes the current tab");
        }
            
        public void actionPerformed(ActionEvent ae) {
            closeCurrentTab();
        }
            
        public void closeCurrentTab() {
            int index = metadataView.getSelectedIndex();
            if (index < 0) return;
                
            Component c = metadataView.getComponentAt(index);
            if (c instanceof SelectableEntryPanel) {
                ((SelectableEntryPanel)c).updateMetadata();
            } else if (c instanceof MetadataPanelWrapper) {
                ((MetadataPanelWrapper)c).updateMetadata();
            }
            metadataView.remove(index);
        }
            
    }

    public class FilterAction extends AbstractAction implements DocumentListener {
        
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

        public void changedUpdate(DocumentEvent e) {
            if (!filterEnabledBox.isSelected()) {
                return;
            }
            actionPerformed(null);
        }

        public void insertUpdate(DocumentEvent e) {
            if (!filterEnabledBox.isSelected()) {
                return;
            }
            actionPerformed(null);
        }

        public void removeUpdate(DocumentEvent e) {
            if (!filterEnabledBox.isSelected()) {
                return;
            }
            actionPerformed(null);
        }
        
    }
    
    public class SaveSIPAction extends AbstractAction {
        
        private static final long serialVersionUID = 7374330582160746169L;

        private static final int BUFFER_SIZE = 4096;
        
        private static final String HEADER = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<METS:mets xmlns:METS=\"http://www.loc.gov/METS/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n";
        
        private static final String FOOTER = "</METS:mets>";
        
        private SelectionAcceptor acceptor = new SelectionAcceptor(FileSystemEntry.FULLY_SELECTED | FileSystemEntry.PARTIALLY_SELECTED);
        
        private SaveSIPAction() {
            //putValue(Action.NAME, "Save SIP");
            URL imgURL = creator.getURL(SAVE_IMAGE_NAME);
            putValue(Action.SMALL_ICON, new ImageIcon(creator.getImage(imgURL)));
            putValue(Action.SHORT_DESCRIPTION, "Save the current files and metadata as a SIP file");
        }
        
        public void actionPerformed(ActionEvent ae) {
            JFileChooser fileChooser = creator.getFileChooser();
            fileChooser.setFileFilter(null);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            
            int choice = fileChooser.showSaveDialog(creator);
            if (choice != JFileChooser.APPROVE_OPTION) return;
            
            File file = fileChooser.getSelectedFile();
            if (file.exists()) {
                choice = JOptionPane.showConfirmDialog(creator, "Overwrite existing file?");
                if (choice != JOptionPane.YES_OPTION) return;
            }
            
            try {
                saveFile(file);
            } catch (IOException ioe) {
                Utility.showExceptionDialog(creator, ioe, "Error saving zip file");
            }
        }
        
        public void saveFile(File file) throws IOException {
            boolean savingSameFile = false;
            SelectableEntry root = creator.getFileSelectTask().getRootEntry();
            if (root instanceof ZipFileEntry && file.exists()) {
                String sourceName = ((ZipFileEntry)root).getSourceFile().getName();
                if (sourceName.equals(file.getAbsolutePath())) {
                    savingSameFile = true;
                }                    
            }
            
            if (savingSameFile) {
                file = File.createTempFile("zip", ".tmp");
            }
            
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));

            updateMetadata();
            
            StringBuffer fileMapBuffer = new StringBuffer("<METS:fileSec><METS:fileGrp>");
            StringBuffer structMapBuffer = new StringBuffer("<METS:structMap>");
            walkTree(zos, fileMapBuffer, structMapBuffer, "", root);
            structMapBuffer.append("</METS:structMap>");
            fileMapBuffer.append("</METS:fileGrp></METS:fileSec>");
            
            StringBuffer xmlBuffer = new StringBuffer(HEADER);
            xmlBuffer.append(fileMapBuffer);
            xmlBuffer.append(structMapBuffer);
            xmlBuffer.append(FOOTER);
            
            ZipEntry entry = new ZipEntry("METS.xml");
            entry.setTime(System.currentTimeMillis());
            zos.putNextEntry(entry);
            StringReader xmlReader = new StringReader(xmlBuffer.toString());
            int byteRead;
            while ((byteRead = xmlReader.read()) != -1) {
                zos.write(byteRead);
            }
            zos.closeEntry();
            
            entry = new ZipEntry("crules.xml");
            entry.setTime(System.currentTimeMillis());
            zos.putNextEntry(entry);
            xmlReader = new StringReader(creator.getConversionRulesTask().getRules().toXML());
            while ((byteRead = xmlReader.read()) != -1) {
                zos.write(byteRead);
            }
            zos.closeEntry();
            
            zos.close();
            
            if (savingSameFile) {
                ((ZipFileEntry)root).getSourceFile().close();
                try {
                    JFileChooser fileChooser = creator.getFileChooser();
                    fileChooser.getSelectedFile().delete();
                    file.renameTo(fileChooser.getSelectedFile());
                    creator.getFileSelectTask().getOpenZipFileAction().openZipFile(fileChooser.getSelectedFile());
                } catch (Exception e) {
                    Utility.showExceptionDialog(creator, e);
                }
            }
            
            JOptionPane.showMessageDialog(creator, "ZIP File successfully written");
        }
        
        private void walkTree(ZipOutputStream zos, StringBuffer fileMap, StringBuffer structMap, String name, SelectableEntry entry) throws IOException {
            name += entry.getShortName();
            
            handleFile(zos, name, entry.isDirectory() ? null : entry.getStream());
            if (!entry.isDirectory()) {
                handleFileData(fileMap, name, entry);
                handleFileStructure(structMap, entry);
                return;
            }
            
            handleDirectoryData(fileMap, entry);
            startDirectoryStructure(structMap, entry);
            
            name += File.separator;
            int childCount = entry.getChildCount(acceptor);
            for (int ctr = 0; ctr < childCount; ctr++) {
                walkTree(zos, fileMap, structMap, name, entry.getChildAt(ctr, acceptor));
            }
            
            endDirectoryStructure(structMap);
        }
        
        private void handleFileData(StringBuffer buffer, String name, SelectableEntry entry) {
            buffer.append("<METS:file ID=\"");
            buffer.append(StreamUtility.enc(entry.getID()));
            buffer.append("\" MIMETYPE=\"");
            buffer.append(StreamUtility.enc(entry.getMimeType()));
            buffer.append("\">");
            buffer.append("<METS:FLocat LOCTYPE=\"URL\" xlink:href=\"file:///");
            buffer.append(StreamUtility.enc(name.replaceAll("\\\\", "/")));
            buffer.append("\"/>");
            buffer.append("</METS:file>");
            
            for (int ctr = 0; ctr < entry.getMetadataCount(); ctr++) {
                Metadata metadata = entry.getMetadata(ctr);
                buffer.append("<METS:file ID=\"");
                buffer.append(StreamUtility.enc(metadata.getID()));
                buffer.append("\" MIMETYPE=\"text/xml\">");
                buffer.append("<METS:FContent USE=\"");
                buffer.append(StreamUtility.enc(metadata.getClass().getName()));
                buffer.append("\"><METS:xmlData>");
                buffer.append(metadata.getAsXML());
                buffer.append("</METS:xmlData></METS:FContent>");
                buffer.append("</METS:file>");
            }
        }
        
        private void handleFileStructure(StringBuffer buffer, SelectableEntry entry) {
            buffer.append("<METS:div LABEL=\"");
            buffer.append(StreamUtility.enc(entry.getShortName()));
            buffer.append("\" TYPE=\"file\">");
            
            buffer.append("<METS:div LABEL=\"Content\" TYPE=\"content\">");
            buffer.append("<METS:fptr FILEID=\"");
            buffer.append(StreamUtility.enc(entry.getID()));
            buffer.append("\"/>");
            buffer.append("</METS:div>");
            
            for (int ctr = 0; ctr < entry.getMetadataCount(); ctr++) {
                Metadata metadata = entry.getMetadata(ctr);
                
                buffer.append("<METS:div LABEL=\"");
                buffer.append(StreamUtility.enc(metadata.getLabel()));
                buffer.append("\" TYPE=\"");
                buffer.append(StreamUtility.enc(metadata.getType()));
                buffer.append("\">");
                
                buffer.append("<METS:fptr FILEID=\"");
                buffer.append(StreamUtility.enc(metadata.getID()));
                buffer.append("\"/>");
                
                buffer.append("</METS:div>");
            }

            buffer.append("</METS:div>");
        }
        
        private void handleDirectoryData(StringBuffer buffer, SelectableEntry entry) {
            if (entry.getMetadataCount() == 0) return;
            
            for (int ctr = 0; ctr < entry.getMetadataCount(); ctr++) {
                Metadata metadata = entry.getMetadata(ctr);
                
                buffer.append("<METS:file ID=\"");
                buffer.append(StreamUtility.enc(metadata.getID()));
                buffer.append("\" MIMETYPE=\"text/xml\">");
                buffer.append("<METS:FContent USE=\"");
                buffer.append(StreamUtility.enc(metadata.getClass().getName()));
                buffer.append("\"><METS:xmlData>");
                buffer.append(metadata.getAsXML());
                buffer.append("</METS:xmlData></METS:FContent>");
                buffer.append("</METS:file>");
            }
        }
        
        private void startDirectoryStructure(StringBuffer buffer, SelectableEntry entry) {
            buffer.append("<METS:div LABEL=\"");
            buffer.append(StreamUtility.enc(entry.getShortName()));
            buffer.append("\" TYPE=\"");
            buffer.append("folder");
            buffer.append("\">");
            
            for (int ctr = 0; ctr < entry.getMetadataCount(); ctr++) {
                Metadata metadata = entry.getMetadata(ctr);
                
                buffer.append("<METS:div LABEL=\"");
                buffer.append(StreamUtility.enc(metadata.getLabel()));
                buffer.append("\" TYPE=\"");
                buffer.append(StreamUtility.enc(metadata.getType()));
                buffer.append("\">");
                
                buffer.append("<METS:fptr FILEID=\"");
                buffer.append(StreamUtility.enc(metadata.getID()));
                buffer.append("\"/>");
                
                buffer.append("</METS:div>");
            }
        }
        
        private void endDirectoryStructure(StringBuffer buffer) {
            buffer.append("</METS:div>");
        } 
        
        private void handleFile(ZipOutputStream zos, String name, InputStream stream) throws IOException {
            if (stream == null) {
                ZipEntry entry = new ZipEntry(name + "/");
                //entry.setTime(file.lastModified());
                //no need to setCRC, or setSize as they are computed automatically.
                
                zos.putNextEntry(entry);
                zos.closeEntry();
                return;
            }
            
            ZipEntry entry = new ZipEntry(name);
            //entry.setTime(file.lastModified());
            //no need to setCRC, or setSize as they are computed automatically.
            
            zos.putNextEntry(entry);
            //FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            while (stream.available() > 0) {
                int bytesRead = stream.read(buffer, 0, BUFFER_SIZE);
                if (bytesRead == -1) {
                    break;
                }
                zos.write(buffer, 0, bytesRead);
            }
            
            stream.close();
            zos.closeEntry();
        }
        
    }
    
}
