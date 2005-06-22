package fedora.services.sipcreator;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.jmimemagic.Magic;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fedora.services.sipcreator.acceptor.SelectionAcceptor;
import fedora.services.sipcreator.metadata.Metadata;
import fedora.services.sipcreator.tasks.ConversionRulesTask;
import fedora.services.sipcreator.tasks.FileSelectTask;
import fedora.services.sipcreator.tasks.MetadataEntryTask;
import fedora.services.sipcreator.utility.ExtensionFileFilter;
import fedora.services.sipcreator.utility.GUIUtility;

public class SIPCreator extends JApplet {

    private static final long serialVersionUID = 3690478008922224183L;

    private static final String CONFIG_FILE_NAME = "config" + File.separator + "sipcreator.properties";
    
    //These are event handling classes.
    private CloseCurrentTabAction closeCurrentTabAction = new CloseCurrentTabAction();
    private SaveSIPAction saveSIPAction = new SaveSIPAction();
//    private ChangeUIAction changeUIAction = new ChangeUIAction();
//    private AddNewMetadataAction addNewMetadataAction = new AddNewMetadataAction();
    
    //Tool for parsing XML documents
    private DocumentBuilder documentBuilder;

    //Tool for file selection
    private JFileChooser fileChooser = new JFileChooser(".");
    
    //Global data structure for storing system wide properties
    private Properties sipCreatorProperties = new Properties();
    
    //UI component for displaying/editing file metadata
    private JTabbedPane rightPanel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    
    //Tool for determining MIME type
    private Magic magic;
    
    //List of known metadata formats and their display names
    private Vector knownMetadataClasses = new Vector();
    private Vector knownMetadataClassNames = new Vector();
    
    //Task elements, these are larger classes that encapsulate everything necessary
    //for the end user to perform a certain task.
    private ConversionRulesTask conversionRulesTask = new ConversionRulesTask(this);
    private MetadataEntryTask metadataEntryTask = new MetadataEntryTask(this);
    private FileSelectTask fileSelectTask = new FileSelectTask(this);
    
    
    public void init() {
        try {
            sipCreatorProperties.load(new FileInputStream(CONFIG_FILE_NAME));
        } catch (IOException ioe) {
            GUIUtility.showExceptionDialog(this, ioe, "Properties not loaded");
        }
        
        //Instantiate the XML Parser
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            GUIUtility.showExceptionDialog(this, pce, "XML Parser failed initialization");
        }
        
        try {
            Logger.getLogger("net.sf.jmimemagic").setLevel(Level.OFF);
            magic = new Magic(sipCreatorProperties.getProperty("sipcreator.mimetype.magic"));
        } catch (Exception e) {
            GUIUtility.showExceptionDialog(this, e, "Mime type tool failed initialization");
        }

        fileChooser.addChoosableFileFilter(new ExtensionFileFilter("zip"));
        
        //Create the central JSplitPane
        JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topPane.setLeftComponent(createLeftPanel());
        topPane.setRightComponent(rightPanel);
        topPane.setOneTouchExpandable(true);
        
        //Add the JSplitPane
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(5, 5));
        cp.add(topPane, BorderLayout.CENTER);
        cp.add(createToolBar(), BorderLayout.NORTH);
        
        //Perform default loading activities
        loadDefaults();
    }

    private JToolBar createToolBar() {
    	JToolBar result = new JToolBar();
    	
    	result.add(saveSIPAction);
    	result.add(closeCurrentTabAction);
    	
    	return result;
    }
    
    private JComponent createLeftPanel() {
        JTabbedPane leftPanel = new JTabbedPane();
        
        leftPanel.addTab("File Selection", fileSelectTask);
        leftPanel.addTab("Metadata Entry", metadataEntryTask);
        leftPanel.addTab("Conversion Rules", conversionRulesTask);
        
        return leftPanel;
    }

    
    private void loadDefaults() {
        String value;
        
        value = sipCreatorProperties.getProperty("sipcreator.default.crules");
        if (value != null) {
            loadDefaultConversionRules(value);
        }
        
        value = sipCreatorProperties.getProperty("sipcreator.metadata.classList");
        if (value != null) {
            loadDefaultMetadataClasses(value);
        }
    }
    
    private void loadDefaultConversionRules(String value) {
        try {
            conversionRulesTask.openURL(value);
        } catch (Exception mue) {
            try {
                conversionRulesTask.openFile(new File(value));
            } catch (Exception e) {
                GUIUtility.showExceptionDialog(this, e, mue.toString());
            }
        }
    }
    
    private void loadDefaultMetadataClasses(String value) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(value));
            while (br.ready()) {
                try {
                    String displayName = br.readLine();
                    if (!br.ready()) throw new IOException("Improperly formatted classlist file"); 
                    String className = br.readLine();
                    Class metadataClass = Class.forName(className);
                    if (!Metadata.class.isAssignableFrom(metadataClass)) continue;
                    knownMetadataClasses.add(metadataClass);
                    knownMetadataClassNames.add(displayName);
                } catch (ClassNotFoundException e) {
                    continue;
                }
            }
        } catch (Exception e) {
            GUIUtility.showExceptionDialog(this, e);
        }
    }
    
    
    private class CloseCurrentTabAction extends AbstractAction {
    	
		private static final long serialVersionUID = -1317113261942287869L;

		public CloseCurrentTabAction() {
			putValue(Action.NAME, "Close Tab");
			putValue(Action.SHORT_DESCRIPTION, "Closes the current tab");
		}
		
		public void actionPerformed(ActionEvent ae) {
			int index = rightPanel.getSelectedIndex();
			if (index < 0) return;
			
			SelectableEntryPanel mlp = (SelectableEntryPanel)rightPanel.getComponentAt(index);
            mlp.updateMetadata();
			rightPanel.remove(index);
    	}
    	
    }
    
    private class SaveSIPAction extends AbstractAction {
    	
		private static final long serialVersionUID = 7374330582160746169L;

        private static final int BUFFER_SIZE = 4096;
        
        private static final String HEADER = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<METS:mets xmlns:METS=\"http://www.loc.gov/METS/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n";// +
//            "  <METS:dmdSec ID=\"DC\">\n" +
//            "    <METS:mdWrap MDTYPE=\"OTHER\">\n" +
//            "      <METS:xmlData>\n" +
//            "        <oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"/>\n" +
//            "      </METS:xmlData>\n" +
//            "    </METS:mdWrap>\n" +
//            "  </METS:dmdSec>\n";
        
        private static final String FOOTER = "</METS:mets>";
        
        private SelectionAcceptor acceptor = new SelectionAcceptor(FileSystemEntry.FULLY_SELECTED | FileSystemEntry.PARTIALLY_SELECTED);
        
		public SaveSIPAction() {
    		putValue(Action.NAME, "Save SIP");
    		putValue(Action.SHORT_DESCRIPTION, "Save the current files and metadata as a SIP file");
    	}
    	
    	public void actionPerformed(ActionEvent ae) {
            fileChooser.setFileFilter(null);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            
            int choice = fileChooser.showSaveDialog(SIPCreator.this);
            if (choice != JFileChooser.APPROVE_OPTION) return;
            
            File file = fileChooser.getSelectedFile();
            if (file.exists()) {
                choice = JOptionPane.showConfirmDialog(SIPCreator.this, "Overwrite existing file?");
                if (choice != JOptionPane.YES_OPTION) return;
            }
            
            try {
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
                //zos.setLevel(9);

                for (int ctr = 0; ctr < rightPanel.getTabCount(); ctr++) {
                    ((SelectableEntryPanel)rightPanel.getComponentAt(ctr)).updateMetadata();
                }
                
                StringBuffer fileMapBuffer = new StringBuffer("<METS:fileSec>");
                StringBuffer structMapBuffer = new StringBuffer("<METS:structMap>");
                walkTree(zos, fileMapBuffer, structMapBuffer, "", fileSelectTask.getRootEntry());
                structMapBuffer.append("</METS:structMap>");
                fileMapBuffer.append("</METS:fileSec>");
                
                StringBuffer xmlBuffer = new StringBuffer(HEADER);
                xmlBuffer.append(getDMDSec(fileSelectTask.getRootEntry()));
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
                
                zos.close();
                
                JOptionPane.showMessageDialog(SIPCreator.this, "ZIP File successfully written");
            } catch (IOException ioe) {
                GUIUtility.showExceptionDialog(SIPCreator.this, ioe, "Error saving zip file");
            }
    	}
        
        private StringBuffer getDMDSec(SelectableEntry root) {
            StringBuffer result = new StringBuffer();
            
            Vector metadataList = root.getMetadata();
            for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                Metadata metadata = (Metadata)metadataList.get(ctr);
                
                result.append("<METS:dmdSec ID=\"");
                result.append(metadata.getID());
                result.append("\"><METS:mdWrap MDTYPE=\"OTHER\"><METS:xmlData>");
                result.append(metadata.getAsXML());
                result.append("</METS:xmlData></METS:mdWrap></METS:dmdSec>");
            }
            
            return result;
        }
        
        private void walkTree(ZipOutputStream zos, StringBuffer fileMap, StringBuffer structMap, String name, SelectableEntry entry) throws IOException {
            name += entry.getShortName();
            
            handleFile(zos, name, entry.isDirectory() ? null : entry.getStream());
            if (!entry.isDirectory()) {
                handleFileData(fileMap, name, entry);
                handleFileStructure(structMap,  entry);
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
            buffer.append("<METS:fileGrp><METS:fileGrp>");
            
            buffer.append("<METS:file ID=\"");
            buffer.append(entry.getID());
            buffer.append("\" MIMETYPE=\"");
            buffer.append(entry.getMimeType());
            buffer.append("\">");
            buffer.append("<METS:FLocat LOCTYPE=\"URL\" xlink:href=\"file:///");
            buffer.append(name.replaceAll("\\\\", "/"));
            buffer.append("\"/>");
            buffer.append("</METS:file>");
            
            Vector metadataList = entry.getMetadata();
            for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                Metadata metadata = (Metadata)metadataList.get(ctr);
                
                buffer.append("<METS:file ID=\"");
                buffer.append(metadata.getID());
                buffer.append("\" MIMETYPE=\"text/xml\">");
                buffer.append("<METS:FContent><METS:xmlData>");
                buffer.append(metadata.getAsXML());
                buffer.append("</METS:xmlData></METS:FContent>");
                buffer.append("</METS:file>");
            }

            buffer.append("</METS:fileGrp></METS:fileGrp>");
        }
        
        private void handleFileStructure(StringBuffer buffer, SelectableEntry entry) {
            buffer.append("<METS:div LABEL=\"");
            buffer.append(entry.getLabel());
            buffer.append("\" TYPE=\"file\">");
            
            buffer.append("<METS:div LABEL=\"Content\" TYPE=\"content\">");
            buffer.append("<METS:fptr FILEID=\"");
            buffer.append(entry.getID());
            buffer.append("\"/>");
            buffer.append("</METS:div>");
            
            Vector metadataList = entry.getMetadata();
            for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                Metadata metadata = (Metadata)metadataList.get(ctr);
                
                buffer.append("<METS:div LABEL=\"");
                buffer.append(metadata.getLabel());
                buffer.append("\" TYPE=\"");
                buffer.append(metadata.getType());
                buffer.append("\">");
                
                buffer.append("<METS:fptr FILEID=\"");
                buffer.append(metadata.getID());
                buffer.append("\"/>");
                
                buffer.append("</METS:div>");
            }

            buffer.append("</METS:div>");
        }
        
        private void handleDirectoryData(StringBuffer buffer, SelectableEntry entry) {
            if (entry.getParent() == null) return;
            
            Vector metadataList = entry.getMetadata();
            if (metadataList.size() == 0) return;
            
            buffer.append("<METS:fileGrp>");
            
            for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                Metadata metadata = (Metadata)metadataList.get(ctr);
                
                buffer.append("<METS:file ID=\"");
                buffer.append(metadata.getID());
                buffer.append("\" MIMETYPE=\"text/xml\">");
                buffer.append("<METS:FContent><METS:xmlData>");
                buffer.append(metadata.getAsXML());
                buffer.append("</METS:xmlData></METS:FContent>");
                buffer.append("</METS:file>");
            }
            
            buffer.append("</METS:fileGrp>");
        }
        
        private void startDirectoryStructure(StringBuffer buffer, SelectableEntry entry) {
            buffer.append("<METS:div LABEL=\"");
            buffer.append(entry.getLabel());
            buffer.append("\" TYPE=\"folder\"");
            
            if (entry.getParent() == null) {
                Vector metadataList = entry.getMetadata();
                for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                    if (ctr == 0) {
                        buffer.append(" DMDID=\"");
                    } else {
                        buffer.append(" ");
                    }
                    
                    buffer.append(((Metadata)metadataList.get(ctr)).getID());
                    
                    if (ctr == metadataList.size() - 1) {
                        buffer.append("\">");
                    }
                }
                return;
            }
            
            buffer.append(">");
            
            Vector metadataList = entry.getMetadata();
            for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                Metadata metadata = (Metadata)metadataList.get(ctr);
                
                buffer.append("<METS:div LABEL=\"");
                buffer.append(metadata.getLabel());
                buffer.append("\" TYPE=\"\">");
                buffer.append("<METS:fptr FILEID=\"");
                buffer.append(metadata.getID());
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
    
    
    public Magic getMimeTypeTool() {
        return magic;
    }
    
    public JTabbedPane getRightPanel() {
        return rightPanel;
    }
    
    public MetadataEntryTask getMetadataEntryTask() {
        return metadataEntryTask;
    }
    
    public ConversionRulesTask getConversionRulesTask() {
        return conversionRulesTask;
    }
    
    public FileSelectTask getFileSelectTask() {
        return fileSelectTask;
    }
    
    public Vector getKnownMetadataClasses() {
        return knownMetadataClasses;
    }
    
    public Vector getKnownMetadataClassNames() {
        return knownMetadataClassNames;
    }
    
    public DocumentBuilder getXMLParser() {
        return documentBuilder;
    }
    
    public JFileChooser getFileChooser() {
        return fileChooser;
    }
    
    public CloseCurrentTabAction getCloseCurrentTabAction() {
        return closeCurrentTabAction;
    }

    public SaveSIPAction getSaveSIPAction() {
        return saveSIPAction;
    }
    
}

//private class ChangeUIAction extends AbstractAction {
//
//private static final long serialVersionUID = 6434604639170194137L;
//
//private String current = UIManager.getCrossPlatformLookAndFeelClassName();
//
//public ChangeUIAction() {
//  putValue(Action.NAME, "Change UI");
//  putValue(Action.SHORT_DESCRIPTION, "Toggles between Java and System look and feel");
//}
//
//public void actionPerformed(ActionEvent ae) {
//  if (current.equals(UIManager.getCrossPlatformLookAndFeelClassName())) {
//      current = UIManager.getSystemLookAndFeelClassName();
//  } else {
//      current = UIManager.getCrossPlatformLookAndFeelClassName();
//  }
//  
//  try {
//      UIManager.setLookAndFeel(current);
//      SwingUtilities.updateComponentTreeUI(SIPCreator.this);
//        SwingUtilities.updateComponentTreeUI(fileChooser);
//  } catch (Exception e) {
//        GUIUtility.showExceptionDialog(SIPCreator.this, e);
//  }
//}
//
//}
//
//private class AddNewMetadataAction extends AbstractAction {
//
//    private static final long serialVersionUID = 3256728364034437681L;
//
//    private File lastDirectory = new File("."); 
//    
//    public AddNewMetadataAction() {
//        putValue(Action.NAME, "Add New Metadata Format");
//        putValue(Action.SHORT_DESCRIPTION, "Adds a new metadata format to the list");
//    }
//    
//    public void actionPerformed(ActionEvent ae) {
//        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        fileChooser.setCurrentDirectory(lastDirectory);
//        int choice = fileChooser.showOpenDialog(SIPCreator.this);
//        lastDirectory = fileChooser.getCurrentDirectory();
//        if (choice != JFileChooser.APPROVE_OPTION) return;
//        
//        try {
//            File selectedFile = SIPCreator.this.fileChooser.getSelectedFile();
//            
//            URL url = new URL("file", "", selectedFile.getCanonicalPath() + "/");
//            ClassLoader loader = new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
//            Vector addedClass = handleFile("", selectedFile, false, loader);
//            JOptionPane.showMessageDialog(SIPCreator.this, "Added the following classes:\n" + addedClass);
//        } catch (Exception e) {
//            GUIUtility.showExceptionDialog(SIPCreator.this, e);
//        }
//    }
//    
//    private Vector handleFile(String packageName, File file, boolean addName, ClassLoader loader) {
//        Vector result = new Vector();
//        
//        if (file.isDirectory()) {
//            File[] fileList = file.listFiles();
//            for (int ctr = 0; ctr < fileList.length; ctr++) {
//                if (addName) {
//                    String newPackageName = packageName + file.getName() + ".";
//                    result.addAll(handleFile(newPackageName, fileList[ctr], true, loader));
//                } else {
//                    result.addAll(handleFile(packageName, fileList[ctr], true, loader));
//                }
//            }
//            return result;
//        }
//        
//        if (!file.getName().endsWith(".class")) return result;
//        String className = packageName + file.getName().substring(0, file.getName().lastIndexOf('.'));
//        Class loadedClass = null;
//        try {
//            loadedClass = loader.loadClass(className);
//        } catch (ClassNotFoundException cnfe) {
//            return result;
//        }
//        
//        if (loadedClass.isInterface()) return result;
//        if (!Metadata.class.isAssignableFrom(loadedClass)) return result;
//        
//        result.add(loadedClass);
//        knownMetadataClasses.add(loadedClass);
//        return result;
//    }
//    
//}
//
//
//public AddNewMetadataAction getAddNewMetadataAction() {
//    return addNewMetadataAction;
//}
//
//public ChangeUIAction getChangeUIAction() {
//    return changeUIAction;
//}
//
