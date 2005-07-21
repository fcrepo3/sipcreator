package fedora.services.sipcreator;

/**
 * This class defines a number of constants that are in use across the entire
 * SIPCreator system.  All file locations should be relative to the codebase.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public interface Constants {

    /** This is the location of the config file */
    public static final String CONFIG_FILE_NAME = "config/sipcreator.properties";
    
    /** The property key for the file of the list of metadata classes */
    public static final String METADATA_CLASS_LIST = "sipcreator.metadata.classlist";
    
    /** The property key for the default conversion rules file */
    public static final String DEFAULT_CRULES = "sipcreator.default.crules";
    
    /** The property key for the default mime type detection tool class */
    public static final String MIMETYPE_CLASS_NAME = "sipcreator.mimetype.detector";
    
    /** The location of the image used to show metadata */
    public static final String METADATA_IMAGE_NAME = "images/metadata.png";
    /** The location of the image used to show the opening action */
    public static final String FOLDER_IMAGE_NAME = "images/dropline/gnome-folder.png";
    /** The location of the image used to show the opening web action */
    public static final String WEB_FOLDER_IMAGE_NAME = "images/dropline/gftp.png";
    /** The location of the image used to show the saving action */
    public static final String SAVE_IMAGE_NAME = "images/dropline/gnome-dev-floppy.png";
    /** The location of the image used to show the refresh action */
    public static final String RELOAD_IMAGE_NAME = "images/dropline/stock-reload.png";
    /** The location of the image used to show the opening a zip action */
    public static final String ZIP_FILE_IMAGE_NAME = "images/dropline/gnome-mime-application-zip.png";
    /** The location of the image used to show the closing action */
    public static final String CLOSE_IMAGE_NAME = "images/dropline/stock-close.png";

    /** This is the URI of the METS namespace */
    public static final String METS_NS = "http://www.loc.gov/METS/";
    /** This is the URI of the OAI Dublin Core namespace */
    public static final String OAIDC_NS = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    /** This is the URI of the Dublin Core namespace */
    public static final String DC_NS = "http://purl.org/dc/elements/1.1/";
    
}
