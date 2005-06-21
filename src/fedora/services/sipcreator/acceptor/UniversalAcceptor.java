package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SelectableEntry;

public class UniversalAcceptor implements SIPEntryAcceptor {

    public boolean isEntryAcceptable(SelectableEntry entry) {
        return true;
    }
   
}
