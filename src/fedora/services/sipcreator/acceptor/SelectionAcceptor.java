package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SIPEntry;

public class SelectionAcceptor implements SIPEntryAcceptor {

    private int allowableSelectionStates;
    
    public SelectionAcceptor() {
    }
    
    public SelectionAcceptor(int newAllowableSelectionStates) {
        allowableSelectionStates = newAllowableSelectionStates;
    }
    
    public int getAllowableSelectionStates() {
        return allowableSelectionStates;
    }
    
    public void setAllowableSelectionStates(int newAllowableSelectionStates) {
        allowableSelectionStates = newAllowableSelectionStates;
    }
    
    public boolean isEntryAcceptable(SIPEntry entry) {
        return (entry.getSelectionLevel() & allowableSelectionStates) == entry.getSelectionLevel();
    }

}
