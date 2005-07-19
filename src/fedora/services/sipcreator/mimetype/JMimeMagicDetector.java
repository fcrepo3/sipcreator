package fedora.services.sipcreator.mimetype;

import java.io.File;
import java.io.FileNotFoundException;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fedora.services.sipcreator.SIPCreator;

public class JMimeMagicDetector extends MimetypeDetector {

    private Magic magic;
    
    public JMimeMagicDetector(SIPCreator newCreator) throws MagicParseException, FileNotFoundException {
        super(newCreator);
        
        Logger.getLogger("net.sf.jmimemagic").setLevel(Level.OFF);
        
        String property = "sipcreator.mimetype.jmimemagic.config";
        String filename = creator.getProperties().getProperty(property);
        
        magic = new Magic(filename);
    }
    
    public String getMimeType(File file) {
        try {
            return magic.getMagicMatch(file).getMimeType();
        } catch (MagicException me) {
            return "jmimemagic failure";
        } catch (MagicMatchNotFoundException mmnfe) {
            return "unknown";
        }
    }

}
