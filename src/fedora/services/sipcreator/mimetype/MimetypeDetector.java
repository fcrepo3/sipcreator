package fedora.services.sipcreator.mimetype;

import java.io.File;

import fedora.services.sipcreator.SIPCreator;

/**
 * This interface defines the methods necessary to provide the SIPCreator
 * system with mime type information about a file.  Mime type detection
 * is a difficult task so this interface was created to allow other
 * libraries to interact relatively seamlessly with the SIPCreator system.
 * Generally, each library has it's own calling conventions, etc.  This 
 * interface overcomes these problems.  Note that any subclasses of this
 * class should provide a constructor which takes in a single argument:
 * a SIPCreator object.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public abstract class MimetypeDetector {

    /**
     * The SIP creator object.  Needed to obtain properties about the system,
     * input streams to read extra config files, etc.
     */
    private SIPCreator creator;
    
    /**
     * 
     * <br><br>
     * @param newCreator
     */
    public MimetypeDetector(SIPCreator newCreator) {
        creator = newCreator;
    }
    
    /**
     * Returns the SIP creator object.
     * <br><br>
     * @return The SIP creator object.
     */
    public SIPCreator getCreator() {
        return creator;
    }
    
    /**
     * This method should, given a File object, return a mime type associated
     * with that file.  Mime type correctness is a difficult thing, so no
     * constraints are put on the correctness of the result.  Regardless of
     * what is passed to this method, a non-null value should always be
     * returned.
     * <br><br>
     * @param file The file whose mime type to return.
     * @return The mime type of the given file.
     */
    public abstract String getMimeType(File file);
    
}
