package fedora.services.sipcreator.mimetype;

import java.io.File;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fedora.services.sipcreator.SIPCreator;

/**
 * This class uses a modified library called JMimeMagic. It is a freely
 * available library distributed under the LGPL (at the time of this writing,
 * 7/20/2005).
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class JMimeMagicDetector extends MimetypeDetector {

    /** This is the main JMimeMagic object that provides mime type data */
    private Magic magic;
    
    /**
     * This constructor produces a JMimeMagic mime type detection tool.  The
     * property "sipcreator.mimetype.jmimemagic.config" in the SIPCreator
     * properties is assumed to hold the configuration file for this object. 
     * <br><br>
     * @param newCreator The SIPCreator from which to obtain the config file
     * location.
     * @throws MagicParseException Thrown if the config file did not conform
     * to the expected syntax.
     */
    public JMimeMagicDetector(SIPCreator newCreator) throws MagicParseException {
        super(newCreator);
        
        Logger.getLogger("net.sf.jmimemagic").setLevel(Level.OFF);
        
        String property = "sipcreator.mimetype.jmimemagic.config";
        String filename = getCreator().getProperties().getProperty(property);
        
        magic = new Magic(getCreator().getInputStream(filename));
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
