package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SelectableEntry;

/**
 * This SelectableEntryAcceptor makes its choices based on the
 * selection status of the entry handed to it.  This class stores
 * all of the flags as a single int, which is the result of a bitwise
 * or operation on the acceptable flags.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class SelectionAcceptor extends SelectableEntryAcceptor {

    /** The bitwise or of all acceptable states */
    private int allowableSelectionStates;
    
    /**
     * This is the default constructor.  This constructor produces an
     * acceptor which accepts no entrys.
     */
    public SelectionAcceptor() {
    }
    
    /**
     * This is just a convenience constructor which passes the given
     * parameter to <code>setAllowableSelectionStates(int)</code>.
     * <br><br>
     * @param newAllowableSelectionStates The bitwise or of all allowable
     * selection states.
     */
    public SelectionAcceptor(int newAllowableSelectionStates) {
        allowableSelectionStates = newAllowableSelectionStates;
    }
    
    /**
     * Returns the bitwise or of allowable selection states.
     * <br><br>
     * @return The bitwise or of allowable selection states.
     */
    public int getAllowableSelectionStates() {
        return allowableSelectionStates;
    }
    
    /**
     * Sets the allowable selection states.  The int parameter should be
     * the result of OR'ing together all of the selection state values
     * that are acceptable.  These values are define in the SelectableEntry
     * class.
     * <br><br>
     * @param newAllowableSelectionStates The bitwise or of the acceptable
     * selection states.
     */
    public void setAllowableSelectionStates(int newAllowableSelectionStates) {
        allowableSelectionStates = newAllowableSelectionStates;
    }
    
    public boolean isEntryAcceptable(SelectableEntry entry) {
        return (entry.getSelectionLevel() & allowableSelectionStates) == entry.getSelectionLevel();
    }

}
