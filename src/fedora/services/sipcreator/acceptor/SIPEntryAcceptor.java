package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SelectableEntry;

public interface SIPEntryAcceptor {

    public abstract boolean isEntryAcceptable(SelectableEntry entry);
    
}
