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
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import beowulf.gui.JGraph;
import beowulf.gui.Utility;
import beowulf.util.ExtensionFileFilter;
import beowulf.util.StreamUtility;
import fedora.services.sipcreator.acceptor.SelectionAcceptor;
import fedora.services.sipcreator.metadata.Metadata;
import fedora.services.sipcreator.mimetype.MimetypeDetector;
import fedora.services.sipcreator.tasks.ConversionRulesTask;
import fedora.services.sipcreator.tasks.FileSelectTask;
import fedora.services.sipcreator.tasks.MetadataEntryTask;

public class SIPCreator extends JApplet {

    private static final long serialVersionUID = 3690478008922224183L;

    private static final String CONFIG_FILE_NAME = "config" + File.separator + "sipcreator.properties";
    
    //These are event handling classes.
    private SaveSIPAction saveSIPAction = new SaveSIPAction();
    private GenerateGraphAction generateGraphAction = new GenerateGraphAction();
    
    private JLabel currentFileLabel = new JLabel();
    private JProgressBar progressBar = new JProgressBar();
    
    //Tool for parsing XML documents
    private DocumentBuilder documentBuilder;

    //Tool for file selection
    private JFileChooser fileChooser = new JFileChooser(".");
    
    //Global data structure for storing system wide properties
    private Properties sipCreatorProperties = new Properties();
    
    //UI component for displaying/editing file metadata
    private JGraph graphView = new JGraph(); 
    private MetadataView metadataView = new MetadataView();
    
    //Tool for determining MIME type
    private MimetypeDetector mimetypeDetector;
    
    //List of known metadata formats and their display names
    private Vector knownMetadataClasses = new Vector();
    private Vector knownMetadataDisplayNames = new Vector();
    
    //Task elements, these are larger classes that encapsulate everything necessary
    //for the end user to perform a certain task.
    private ConversionRulesTask conversionRulesTask = new ConversionRulesTask(this);
    private MetadataEntryTask metadataEntryTask = new MetadataEntryTask(this);
    private FileSelectTask fileSelectTask = new FileSelectTask(this);
    
    
    public void init() {
        try {
            sipCreatorProperties.load(new FileInputStream(CONFIG_FILE_NAME));
        } catch (IOException ioe) {
            Utility.showExceptionDialog(this, ioe, "Properties not loaded");
        }
        
        //Instantiate the XML Parser
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            Utility.showExceptionDialog(this, pce, "XML Parser failed initialization");
        }
        
        fileChooser.addChoosableFileFilter(new ExtensionFileFilter("zip"));
        
        //Create the central JSplitPane
        JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topPane.setLeftComponent(createLeftPanel());
        topPane.setRightComponent(createRightPanel());
        topPane.setOneTouchExpandable(true);

        //Add the JSplitPane
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(5, 5));
        cp.add(topPane, BorderLayout.CENTER);
        cp.add(createToolBar(), BorderLayout.NORTH);
        cp.add(createStatusBar(), BorderLayout.SOUTH);
        //Perform default loading activities
        loadDefaults();
    }

    private JComponent createStatusBar() {
        JPanel result = new JPanel(new BorderLayout());
        
        result.add(currentFileLabel, BorderLayout.CENTER);
        result.add(Utility.addLabelLeft("Loading Status: ", progressBar), BorderLayout.EAST);
        
        return result;
    }
    
    private JComponent createRightPanel() {
        JTabbedPane result = new JTabbedPane();
        
        result.addTab("Metadata View", null, metadataView, "Shows the active metadata");
        result.addTab("Graph View", null, graphView, "Shows the relationship graph");
        
        return result;
    }
    
    private JToolBar createToolBar() {
    	JToolBar result = new JToolBar();
    	
    	result.add(saveSIPAction);
    	result.add(metadataView.getCloseCurrentTabAction());
    	result.add(generateGraphAction);
        
    	return result;
    }
    
    private JComponent createLeftPanel() {
        JTabbedPane leftPanel = new JTabbedPane();
        
        leftPanel.addTab("File Selection", null, fileSelectTask, "blarg");
        leftPanel.addTab("Metadata Entry", metadataEntryTask);
        leftPanel.addTab("Conversion Rules", conversionRulesTask);
        
        return leftPanel;
    }

    
    private void loadDefaults() {
        String value, property;
        
        try {
            property = "sipcreator.default.crules";
            value = sipCreatorProperties.getProperty(property);
            if (value != null) {
                loadDefaultConversionRules(value);
            }
        } catch (Exception e) {
            Utility.showExceptionDialog(this, e, "Conversion Rules failed initialization");
        }
        
        try {
            property = "sipcreator.metadata.classList";
            value = sipCreatorProperties.getProperty(property);
            if (value != null) {
                loadDefaultMetadataClasses(value);
            }
        } catch (Exception e) {
            Utility.showExceptionDialog(this, e, "Metadata class detection failed initialization");
        }
        
        try {
            property = "sipcreator.mimetype.detector";
            value = sipCreatorProperties.getProperty(property);
            if (value != null) {
                loadDefaultDetector(value);
            }
        } catch (Exception e) {
            Utility.showExceptionDialog(this, e, "Mimetype detection tool failed initialization");
        }
    }
    
    private void loadDefaultConversionRules(String value) throws Exception {
        try {
            conversionRulesTask.openURL(value);
        } catch (Exception mue) {
            conversionRulesTask.openFile(new File(value));
        }
    }
    
    private void loadDefaultMetadataClasses(String value) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(value));
        while (br.ready()) {
            try {
                String displayName = br.readLine();
                if (!br.ready()) throw new IOException("Improperly formatted classlist file"); 
                String className = br.readLine();
                Class metadataClass = Class.forName(className);
                if (!Metadata.class.isAssignableFrom(metadataClass)) continue;
                knownMetadataClasses.add(metadataClass);
                knownMetadataDisplayNames.add(displayName);
            } catch (ClassNotFoundException e) {
                continue;
            }
        }
    }
    
    private void loadDefaultDetector(String value) throws Exception {
        Class detectorClass = Class.forName(value);
            
        if (!MimetypeDetector.class.isAssignableFrom(detectorClass)) {
            throw new RuntimeException
            ("Mimetype detection tool failed initialization:\n" +
             "Given detector does not extend MimetypeDetector");
        }
            
        Constructor constructor = detectorClass.getConstructor
        (new Class[]{SIPCreator.class});
        mimetypeDetector = (MimetypeDetector)constructor.newInstance
        (new Object[]{this});
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
                saveFile(file);
            } catch (IOException ioe) {
                Utility.showExceptionDialog(SIPCreator.this, ioe, "Error saving zip file");
            }
    	}
        
        public void saveFile(File file) throws IOException {
            boolean savingSameFile = false;
            if (fileSelectTask.getRootEntry() instanceof ZipFileEntry && file.exists()) {
                String sourceName = ((ZipFileEntry)fileSelectTask.getRootEntry()).getSourceFile().getName();
                if (sourceName.equals(file.getAbsolutePath())) {
                    savingSameFile = true;
                }                    
            }
            
            if (savingSameFile) {
                file = File.createTempFile("zip", ".tmp");
            }
            
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));

            metadataView.updateMetadata();
            
            StringBuffer fileMapBuffer = new StringBuffer("<METS:fileSec><METS:fileGrp>");
            StringBuffer structMapBuffer = new StringBuffer("<METS:structMap>");
            walkTree(zos, fileMapBuffer, structMapBuffer, "", fileSelectTask.getRootEntry());
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
            xmlReader = new StringReader(conversionRulesTask.getRules().toXML());
            while ((byteRead = xmlReader.read()) != -1) {
                zos.write(byteRead);
            }
            zos.closeEntry();
            
            zos.close();
            
            if (savingSameFile) {
                ((ZipFileEntry)fileSelectTask.getRootEntry()).getSourceFile().close();
                try {
                    fileChooser.getSelectedFile().delete();
                    file.renameTo(fileChooser.getSelectedFile());
                    fileSelectTask.getOpenFileAction().openZipFile(fileChooser.getSelectedFile());
                } catch (Exception e) {
                    Utility.showExceptionDialog(SIPCreator.this, e);
                }
            }
            
            JOptionPane.showMessageDialog(SIPCreator.this, "ZIP File successfully written");
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
            
            Vector metadataList = entry.getMetadata();
            for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                Metadata metadata = (Metadata)metadataList.get(ctr);
                
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
            
            Vector metadataList = entry.getMetadata();
            for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                Metadata metadata = (Metadata)metadataList.get(ctr);
                
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
            Vector metadataList = entry.getMetadata();
            if (metadataList.size() == 0) return;
            
            for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                Metadata metadata = (Metadata)metadataList.get(ctr);
                
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
            
            Vector metadataList = entry.getMetadata();
            for (int ctr = 0; ctr < metadataList.size(); ctr++) {
                Metadata metadata = (Metadata)metadataList.get(ctr);
                
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
    
    public class GenerateGraphAction extends AbstractAction {
        
        private static final long serialVersionUID = 5055044742911980919L;

        private GenerateGraphAction() {
            putValue(Action.NAME, "Generate Graph");
            putValue(Action.SHORT_DESCRIPTION, "Generate a graph based on the current rules and tree");
        }
        
        public void actionPerformed(ActionEvent ae) {
            ConversionRulesGraph crg = new ConversionRulesGraph(conversionRulesTask.getRules(), fileSelectTask.getRootEntry());
            graphView.setModel(crg);
        }
        
    }
    
    
    public Properties getProperties() {
        return sipCreatorProperties;
    }
    
    public MimetypeDetector getMimeTypeTool() {
        return mimetypeDetector;
    }
    
    public MetadataView getMetadataView() {
        return metadataView;
    }
    
    public MetadataEntryTask getMetadataEntryTask() {
        return metadataEntryTask;
    }
    
    public ConversionRulesTask getConversionRulesTask() {
        return conversionRulesTask;
    }
    
    public JLabel getCurrentFileLabel() {
        return currentFileLabel;
    }
    
    public JProgressBar getProgressBar() {
        return progressBar;
    }
    
    public FileSelectTask getFileSelectTask() {
        return fileSelectTask;
    }
    
    public Vector getKnownMetadataClasses() {
        return knownMetadataClasses;
    }
    
    public Vector getKnownMetadataDisplayNames() {
        return knownMetadataDisplayNames;
    }
    
    public DocumentBuilder getXMLParser() {
        return documentBuilder;
    }
    
    public JFileChooser getFileChooser() {
        return fileChooser;
    }
    
    
    public SaveSIPAction getSaveSIPAction() {
        return saveSIPAction;
    }
    
}
