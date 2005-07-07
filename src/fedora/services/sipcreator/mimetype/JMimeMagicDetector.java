package fedora.services.sipcreator.mimetype;

import java.io.File;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import beowulf.gui.Utility;
import fedora.services.sipcreator.SIPCreator;

public class JMimeMagicDetector extends MimetypeDetector {

    private Magic magic;
    
    public JMimeMagicDetector(SIPCreator newCreator) {
        super(newCreator);
        
        Logger.getLogger("net.sf.jmimemagic").setLevel(Level.OFF);
        
        String property = "sipcreator.mimetype.jmimemagic.config";
        String filename = creator.getProperties().getProperty(property);
        
        try {
            magic = new Magic(filename);
        } catch (MagicParseException mpe) {
            Utility.showExceptionDialog(creator, mpe, "JMimeMagic failed initialization");
        }
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
