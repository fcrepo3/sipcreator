package fedora.services.sipcreator.acceptor;

import java.util.regex.Pattern;

import fedora.services.sipcreator.SIPEntry;

public class FilterAcceptor implements SIPEntryAcceptor {

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
    
    public boolean isEntryAcceptable(SIPEntry entry) {
//        try {
        return !enabled || (acceptDirecotries && entry.getFile().isDirectory()) ||
            Pattern.matches(filter, entry.getFile().getName());
//        } catch (PatternSyntaxException pse) {
//            return false;
//        }
    }
    
}
