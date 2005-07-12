package fedora.services.sipcreator.acceptor;

import java.util.Vector;

import fedora.services.sipcreator.SelectableEntry;

public class IntersectionAcceptor extends SIPEntryAcceptor {

    private Vector acceptorList = new Vector();
    
    public Vector getAcceptorList() {
        return acceptorList;
    }
    
    public boolean isEntryAcceptable(SelectableEntry entry) {
        if (acceptorList.size() == 0) {
            return false;
        }
        
        for (int ctr = 0; ctr < acceptorList.size(); ctr++) {
            if (!((SIPEntryAcceptor)acceptorList.get(ctr)).isEntryAcceptable(entry)) {
                return false;
            }
        }
        return true;
    }

}
