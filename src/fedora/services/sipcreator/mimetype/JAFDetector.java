package fedora.services.sipcreator.mimetype;

import java.io.File;

import javax.activation.MimetypesFileTypeMap;

import fedora.services.sipcreator.SIPCreator;

/**
 * This class uses the Sun Microsystems, Inc. provided Java Activation
 * Framework (JAF) library to provide mime type information about files.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class JAFDetector extends MimetypeDetector {

    /** The main JAF object used to provide mime type information */
    private MimetypesFileTypeMap mimetypesFileTypeMap;
    
    /**
     * This constructor produces a JAF mime type detection tool.  The
     * property "sipcreator.mimetype.jaf.config" in the SIPCreator properties
     * is assumed to hold the configuration file for this object. 
     * <br><br>
     * @param newCreator The SIPCreator from which to obtain the config file
     * location.
     */
    public JAFDetector(SIPCreator newCreator){
        super(newCreator);
        
        String prop = "sipcreator.mimetype.jaf.config";
        String filename = getCreator().getProperties().getProperty(prop);
        
        mimetypesFileTypeMap = new MimetypesFileTypeMap(getCreator().getInputStream(filename));
    }
    
    public String getMimeType(File file) {
        return mimetypesFileTypeMap.getContentType(file);
    }

}
