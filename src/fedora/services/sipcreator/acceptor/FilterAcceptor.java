package fedora.services.sipcreator.acceptor;

import java.util.regex.Pattern;

import fedora.services.sipcreator.SelectableEntry;

public class FilterAcceptor extends SIPEntryAcceptor {

    private String filter;
    
    private boolean acceptDirecotries = true;
    
    private boolean enabled;
    
    public boolean getAcceptDirectories() {
        return acceptDirecotries;
    }
    
    public void setAcceptDirectories(boolean newAcceptDirectories) {
        acceptDirecotries = newAcceptDirectories;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean newEnabled) {
        enabled = newEnabled;
    }
    
    public String getFilter() {
        return filter;
    }
    
    public void setFilter(String newFilter) {
        filter = newFilter;
    }
    
    public boolean isEntryAcceptable(SelectableEntry entry) {
//        try {
        return !enabled || (acceptDirecotries && entry.isDirectory()) ||
            Pattern.matches(filter, entry.getShortName());
//        } catch (PatternSyntaxException pse) {
//            return false;
//        }
    }
    
}
