package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SelectableEntry;

public class SelectionAcceptor extends SIPEntryAcceptor {

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
    
    public boolean isEntryAcceptable(SelectableEntry entry) {
        return (entry.getSelectionLevel() & allowableSelectionStates) == entry.getSelectionLevel();
    }

}
