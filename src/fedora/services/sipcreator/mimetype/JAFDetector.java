package fedora.services.sipcreator.mimetype;

import java.io.File;

import javax.activation.MimetypesFileTypeMap;

import fedora.services.sipcreator.SIPCreator;

public class JAFDetector extends MimetypeDetector {

    private MimetypesFileTypeMap mimetypesFileTypeMap;
    
    public JAFDetector(SIPCreator newCreator){
        super(newCreator);
        
        String prop = "sipcreator.mimetype.jaf.config";
        String filename = creator.getProperties().getProperty(prop);
        
        mimetypesFileTypeMap = new MimetypesFileTypeMap(creator.getInputStream(filename));
    }
    
    public String getMimeType(File file) {
        return mimetypesFileTypeMap.getContentType(file);
    }

}
