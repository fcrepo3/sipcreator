package fedora.services.sipcreator;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

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

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import beowulf.gui.Utility;
import beowulf.util.ExtensionFileFilter;
import fedora.services.sipcreator.mimetype.MimetypeDetector;
import fedora.services.sipcreator.tasks.ConversionRulesTask;
import fedora.services.sipcreator.tasks.FileSelectTask;
import fedora.services.sipcreator.tasks.MetadataEntryTask;

public class SIPCreator extends JApplet implements Constants {

    /** */
    private static final long serialVersionUID = 3690478008922224183L;

    /** The label used to display the currently open file/directory */
    private JLabel currentFileLabel = new JLabel();
    /** The progress bar used to display ongiong activity progress */
    private JProgressBar progressBar = new JProgressBar();
    
    /** The data structure for storing non-java system wide properties */
    private Properties sipCreatorProperties = new Properties();
    
    /** The tool for determining MIME type */
    private MimetypeDetector mimetypeDetector;
    
    /** The tool for parsing XML documents */
    private DocumentBuilder documentBuilder;

    /** The tool for file selection */
    private JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
    /** The filter which accepts XML files and directories */
    private FileFilter xmlFilter = new ExtensionFileFilter("xml");
    /** The filter which accepts ZIP files and directories */
    private FileFilter zipFilter = new ExtensionFileFilter("zip");
    
    /** The mapping between metadata classes and display names */
    private Properties knownMetadataClasses = new Properties();
    
    //Task elements, these are larger classes that encapsulate everything
    //necessary for the end user to perform a certain task.
    /** All the UI views and handlers to deal with conversion rule editing */
    private ConversionRulesTask conversionRulesTask;
    /** All the UI views and handlers to deal with metadata entry */
    private MetadataEntryTask metadataEntryTask;
    /** All the UI views and handlers to deal with selecting data sources */
    private FileSelectTask fileSelectTask;
    
    
    public void init() {
        try {
            sipCreatorProperties.load(getInputStream(CONFIG_FILE_NAME));
        } catch (IOException ioe) {
            Utility.showExceptionDialog(this, ioe, "Properties not loaded");
        }
        
        conversionRulesTask = new ConversionRulesTask(this);
        metadataEntryTask = new MetadataEntryTask(this);
        fileSelectTask = new FileSelectTask(this);
        
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

    /**
     * This method returns a JPanel containing the label displaying the
     * currently open file and the progress bar.  Collectively, they form
     * the status bar.
     * <br><br>
     * @return A JPanel displaying system wide information that is visible
     * regardless of which task is active.
     */
    private JComponent createStatusBar() {
        JPanel result = new JPanel(new BorderLayout());
        
        result.add(currentFileLabel, BorderLayout.CENTER);
        result.add(Utility.addLabelLeft("Loading Status: ", progressBar), BorderLayout.EAST);
        
        return result;
    }
    
    /**
     * This method returns a JTabbedPane showing with a single tab for each
     * task.  This is just a convenience method. 
     * <br><br>
     * @return A JTabbedPane where each tab represents a task.
     */
    private JComponent createCenterPanel() {
        JTabbedPane leftPanel = new JTabbedPane();
        
        leftPanel.addTab("File Selection", fileSelectTask);
        leftPanel.addTab("Metadata Entry", metadataEntryTask);
        leftPanel.addTab("Conversion Rules", conversionRulesTask);
        
        return leftPanel;
    }

    
    /**
     * This interprets the information stored in the properties file for the
     * system.  It will handle things such as loading in the default conversion
     * rules file and loading in a list of known metadata classes.  All errors
     * will result in a message dialog being displayed to the user.
     */
    private void loadDefaults() {
        String value;
        
        try {
            value = sipCreatorProperties.getProperty(DEFAULT_CRULES);
            if (value != null) {
                loadDefaultConversionRules(value);
            }
        } catch (Exception e) {
            Utility.showExceptionDialog(this, e, "Conversion Rules failed initialization");
        }
        
        try {
            value = sipCreatorProperties.getProperty(METADATA_CLASS_LIST);
            if (value != null) {
                loadDefaultMetadataClasses(value);
            }
        } catch (Exception e) {
            Utility.showExceptionDialog(this, e, "Metadata class detection failed initialization");
        }
        
        try {
            value = sipCreatorProperties.getProperty(MIMETYPE_CLASS_NAME);
            if (value != null) {
                loadDefaultDetector(value);
            }
        } catch (Exception e) {
            Utility.showExceptionDialog(this, e, "Mimetype detection tool failed initialization");
        }
    }
    
    /**
     * This method handles loading in the default conversion rules file. The
     * parameter is expected to be the path of the conversion rules file
     * relative to the codebase, or an absolute URL.
     * <br><br>
     * @param value The relative path or absolute URL of the conversion rules.
     * @throws Exception Thrown if there is a problem locating the file or
     * parsing the file.
     */
    private void loadDefaultConversionRules(String value) throws Exception {
        try {
            ConversionRules crules = new ConversionRules(documentBuilder.parse(getURL(value).toString()));
            conversionRulesTask.updateRules(value, crules);
        } catch (Exception mue) {
            InputSource is = new InputSource(getInputStream(value));
            ConversionRules crules = new ConversionRules(documentBuilder.parse(is));
            conversionRulesTask.updateRules(value, crules);
        }
    }
    
    /**
     * This method handles loading in the mapping of display names to metadata
     * class names. The parameter is a path of the file defining the mapping
     * using the java.util.Properties syntax, and is relative to the codebase.
     * <br><br>
     * @param value The relative path to the file containing the mapping.
     * @throws IOException Thrown if there is a problem reading the file
     * indicated.
     */
    private void loadDefaultMetadataClasses(String value) throws IOException {
        knownMetadataClasses.load(getInputStream(value));
    }
    
    /**
     * This method is responsible for instantiating the default mime type
     * detection tool.  The parameter is expected to be a fully qualified
     * class name.  The tool is instantiated using reflection, and the given
     * class must be a subclass of MimetypeDetector and have a single argument
     * constructor taking in a SIPCreator reference.
     * <br><br>
     * @param value The fully qualified class name of the detection class.
     * @throws Exception Thrown if there is a problem with the underlying
     * reflection methods.
     */
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
    
    
    /**
     * This method takes a relative path in string form, resolves it against
     * the codebase URL, and returns the input stream associated with the
     * resulting resource.  If there is a problem opening the stream or
     * creating the absolute URL, null is returned and an error message is
     * printed to System.err.
     * <br><br>
     * @param filename The name to resolve against the code base.
     * @return The input stream of the associated resource.
     */
    public InputStream getInputStream(String filename) {
        try {
            return (new URL(getCodeBase(), filename)).openStream();
        } catch (IOException ioe) {
            System.err.println("IOException in SIPCreator#getInputStream: " + ioe.getMessage());
            return null;
        }
    }
    
    /**
     * This method takes a relative path in string form, resolves it against
     * the code base URL, and returns the resulting URL. If there is a problem
     * creating the URL, null is returned and an error message is printed to
     * System.err.
     * <br><br>
     * @param filename The name to resolve against the code base.
     * @return The absolute URL of the associated resource.
     */
    public URL getURL(String filename) {
        try {
            return new URL(getCodeBase(), filename);
        } catch (MalformedURLException mue) {
            System.err.println("MalformedURLException in SIPCreator#getURL: " + mue.getMessage());
            return null;
        }
    }
    
    public Properties getProperties() {
        return sipCreatorProperties;
    }
    
    public String getMimeType(File file) {
        return mimetypeDetector.getMimeType(file);
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
    
    public void setFileLabelText(String text) {
        currentFileLabel.setText(text);
    }
    
    public JProgressBar getProgressBar() {
        return progressBar;
    }
    
    public Properties getKnownMetadataClasses() {
        return knownMetadataClasses;
    }
    
    public Document parseXML(InputSource is) throws IOException, SAXException {
        return documentBuilder.parse(is);
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
