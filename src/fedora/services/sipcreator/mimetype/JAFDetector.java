package fedora.services.sipcreator.mimetype;

import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;

import beowulf.gui.Utility;
import fedora.services.sipcreator.SIPCreator;

public class JAFDetector extends MimetypeDetector {

    private MimetypesFileTypeMap mimetypesFileTypeMap;
    
    public JAFDetector(SIPCreator newCreator){
        super(newCreator);
        
        String prop = "sipcreator.mimetype.jaf.config";
        String filename = creator.getProperties().getProperty(prop);
        
        try {
            mimetypesFileTypeMap = new MimetypesFileTypeMap(filename);
        } catch (IOException ioe) {
            Utility.showExceptionDialog(creator, ioe, "JAF Failed Initialization");
        }
    }
    
    public String getMimeType(File file) {
        return mimetypesFileTypeMap.getContentType(file);
    }

}
