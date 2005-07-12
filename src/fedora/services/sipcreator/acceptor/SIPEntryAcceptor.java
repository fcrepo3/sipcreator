package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SelectableEntry;

public abstract class SIPEntryAcceptor {

    private boolean acceptsMetadata;
    
    public abstract boolean isEntryAcceptable(SelectableEntry entry);
    
    public boolean acceptsMetadata() {
        return acceptsMetadata;
    }
    
    public void setAcceptsMetadata(boolean newAcceptsMetadata) {
        acceptsMetadata = newAcceptsMetadata;
    }
    
}
