package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SIPEntry;

public class UniversalAcceptor implements SIPEntryAcceptor {

    public boolean isEntryAcceptable(SIPEntry entry) {
        return true;
    }
   
}
