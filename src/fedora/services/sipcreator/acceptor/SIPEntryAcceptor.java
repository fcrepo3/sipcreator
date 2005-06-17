package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SIPEntry;

public interface SIPEntryAcceptor {

    public abstract boolean isEntryAcceptable(SIPEntry entry);
    
}
