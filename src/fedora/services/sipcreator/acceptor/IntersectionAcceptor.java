package fedora.services.sipcreator.acceptor;

import java.util.Vector;

import fedora.services.sipcreator.SIPEntry;

public class IntersectionAcceptor implements SIPEntryAcceptor {

    private Vector acceptorList = new Vector();
    
    public Vector getAcceptorList() {
        return acceptorList;
    }
    
    public boolean isEntryAcceptable(SIPEntry entry) {
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
