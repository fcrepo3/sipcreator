package fedora.services.sipcreator.utility;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * This class represents a filter that accepts a file based on its
 * extension.  The extension of the file is compared to the
 * extension given to the filter on creation.  The period character,
 * ".", must begin the extension.
 * <BR><BR>
 * @author Andy Scukanec (ags at cs dot cornell dot edu)
 * @version 1.0 11/7/2003
 */
public class ExtensionFileFilter extends FileFilter {
	
	/**
	 * The extension that all acceptable files must have.
	 */
	protected String extension;
	
    protected boolean ignoreCase = true;
    
    protected boolean acceptDirectories = true;
    
	/**
	 * Creates an instance of this object with the indicated extension.  The
	 * separating period <i>must not</i> be included in the string.
	 * @param newExtension The extension that the file filter should accept.
	 */
	public ExtensionFileFilter(String newExtension) {
		extension = newExtension;
	}
	
	/**
	 * This filter will accept a file <code>f</code> if and only if the last
	 * four characters of its name are the indicated extension.  Directories
	 * are also accepted.
	 * <BR>
	 * @param f The file to accept or reject
	 * @return True if and only if the file is accepted.
	 */
	public boolean accept(File f) {
		if (acceptDirectories && f.isDirectory()) {
            return true;
        }
		
		String name = f.getName();
		int periodPosition = name.lastIndexOf('.');
		if (periodPosition == -1) {
			return extension == null;
		}
		
		String suffix = name.substring(periodPosition + 1);
		return ignoreCase ? suffix.equalsIgnoreCase(extension) : suffix.equals(extension);
	}
	
	/**
	 * This method returns a description of what files are accepted by this
	 * class.
	 * <BR>
	 * @return A description string.
	 */
	public String getDescription() {
		return extension.toUpperCase() + " Files"; 
	}

    public boolean isAcceptDirectories() {
        return acceptDirectories;
    }
    

    public void setAcceptDirectories(boolean acceptDirectories) {
        this.acceptDirectories = acceptDirectories;
    }
    

    public String getExtension() {
        return extension;
    }
    

    public void setExtension(String extension) {
        this.extension = extension;
    }
    

    public boolean isIgnoreCase() {
        return ignoreCase;
    }
    

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    
}

