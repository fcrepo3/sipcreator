package fedora.services.sipcreator;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import beowulf.gui.Utility;
import beowulf.util.ExtensionFileFilter;
import fedora.services.sipcreator.metadata.Metadata;
import fedora.services.sipcreator.mimetype.MimetypeDetector;
import fedora.services.sipcreator.tasks.ConversionRulesTask;
import fedora.services.sipcreator.tasks.FileSelectTask;
import fedora.services.sipcreator.tasks.MetadataEntryTask;

public class SIPCreator extends JApplet implements Constants {

    private static final long serialVersionUID = 3690478008922224183L;

    private JLabel currentFileLabel = new JLabel();
    private JProgressBar progressBar = new JProgressBar();
    
    //Tool for parsing XML documents
    private DocumentBuilder documentBuilder;

    //Tool for file selection
    private JFileChooser fileChooser = new JFileChooser(".");
    private FileFilter xmlFilter = new ExtensionFileFilter("xml");
    private FileFilter zipFilter = new ExtensionFileFilter("zip");
    
    //Global data structure for storing system wide properties
    private Properties sipCreatorProperties = new Properties();
    
    //UI component for displaying/editing file metadata
    //private JGraph graphView = new JGraph(); 
    //private MetadataView metadataView = new MetadataView();
    
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
        
        fileChooser.addChoosableFileFilter(zipFilter);
        fileChooser.addChoosableFileFilter(xmlFilter);
        
        //Add the JSplitPane
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(5, 5));
        cp.add(createCenterPanel(), BorderLayout.CENTER);
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
    
    private JComponent createCenterPanel() {
        JTabbedPane leftPanel = new JTabbedPane();
        
        leftPanel.addTab("File Selection", fileSelectTask);
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
            conversionRulesTask.getLoadConversionRulesWebAction().openURL(value);
        } catch (Exception mue) {
            conversionRulesTask.getLoadConversionRulesAction().openFile(new File(value));
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
    
    
    public Properties getProperties() {
        return sipCreatorProperties;
    }
    
    public MimetypeDetector getMimeTypeTool() {
        return mimetypeDetector;
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
    
    public FileFilter getXMLFilter() {
        return xmlFilter;
    }
    
    public FileFilter getZIPFilter() {
        return zipFilter;
    }
    
}
