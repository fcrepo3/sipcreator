package fedora.services.sipcreator.mimetype;

import java.io.File;

import fedora.services.sipcreator.SIPCreator;

public abstract class MimetypeDetector {

    protected SIPCreator creator;
    
    public MimetypeDetector(SIPCreator newCreator) {
        creator = newCreator;
    }
    
    public abstract String getMimeType(File file);
    
}
