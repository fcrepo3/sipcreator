package fedora.services.sipcreator.acceptor;

import java.util.regex.Pattern;

import fedora.services.sipcreator.SelectableEntry;

/**
 * This class accepts and rejects SelectableEntry objects based on their
 * name and a regular expression.  If the regular expression matches the
 * SelectableEntry name, then it is accepted.  It is possible to turn this
 * action on or off without instantiating a new FilterAcceptor.  Directory
 * entrys can also be accepted or rejected based purely on their directory
 * status.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class FilterAcceptor extends SelectableEntryAcceptor {

    /** This is the regular expression which filters entry objects */
    private String filter;
    
    /** This determines whether or not directories are accepted */
    private boolean acceptDirecotries = true;
    
    /** If this is false, then the FilterAcceptor accepts all entry objects */
    private boolean enabled;
    
    /**
     * Returns true if and only if this FilterAcceptor allows for directory
     * entrys to be accepted.
     * <br><br>
     * @return True if and only if directory entrys are accepted.
     */
    public boolean getAcceptDirectories() {
        return acceptDirecotries;
    }
    
    /**
     * Sets whether or not directory entries are accepted.
     * <br><br>
     * @param newAcceptDirectories The new directory acceptance value.
     */
    public void setAcceptDirectories(boolean newAcceptDirectories) {
        acceptDirecotries = newAcceptDirectories;
    }
    
    /**
     * Returns whether or not this filter is enabled.  If it is not enabled,
     * then all entry objects are accepted.  This is useful when it is
     * undesirable to instantiate a new SelectableEntryAcceptor, but you
     * wish to avoid applying the filter.
     * <br><br>
     * @return True if and only if this Acceptor is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Turns this filter on or off, depending on if <code>newEnabled</code>
     * is true or false respectively.
     * <br><br>
     * @param newEnabled The new status of this FilterAcceptor.
     */
    public void setEnabled(boolean newEnabled) {
        enabled = newEnabled;
    }

    /**
     * This returns the regular expression used to match against
     * SelectableEntry file names.  The syntax for the regular expression
     * is that of <code>java.util.regex.Pattern</code>.
     * <br><br>
     * @return The regular expression used for name matching.
     */
    public String getFilter() {
        return filter;
    }
    
    /**
     * This sets the new regular expression used to match against the
     * SelectableEntry objects.  The syntax for the regular expression
     * is that of <code>java.util.regex.Pattern</code>.
     * <br><br>
     * @param newFilter The new regular expression used for name matching.
     */
    public void setFilter(String newFilter) {
        filter = newFilter;
    }
    
    public boolean isEntryAcceptable(SelectableEntry entry) {
        return !enabled || (acceptDirecotries && entry.isDirectory()) ||
            Pattern.matches(filter, entry.getShortName());
    }
    
}
