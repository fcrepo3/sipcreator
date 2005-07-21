package fedora.services.sipcreator.acceptor;

import java.util.Vector;

import fedora.services.sipcreator.SelectableEntry;

/**
 * This class represents the intersection of many SeletableEntryAcceptor
 * objects.  It is often useful to apply multiple criterion to evaluate
 * a SelectableEntry.  This class provides that mechanism.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class IntersectionAcceptor extends SelectableEntryAcceptor {

    /** This is the list of acceptor objects to be applied */
    private Vector acceptorList = new Vector();

    /**
     * Returns the number of SelectableEntryAcceptor objects to be applied.
     * <br><br>
     * @return The number of SelectableEntryAcceptor objects to be applied.
     */
    public int getAcceptorCount() {
        return acceptorList.size();
    }
    
    /**
     * Returns the index-th acceptor object.
     * <br><br>
     * @param index The index of the acceptor object to be returned.
     * @return The index-th acceptor object.
     */
    public SelectableEntryAcceptor getAcceptor(int index) {
        return (SelectableEntryAcceptor)acceptorList.get(index);
    }
    
    /**
     * Add an acceptor to the list of acceptor objects to be applied.
     * <br><br>
     * @param acceptor The new acceptor to be applied.
     */
    public void addAcceptor(SelectableEntryAcceptor acceptor) {
        acceptorList.add(acceptor);
    }
    
    /**
     * Removes the index-th acceptor object.  Note that this could change
     * the indices of other acceptor objects.
     * <br><br>
     * @param index The index of the acceptor to be removed.
     */
    public void removeAcceptor(int index) {
        acceptorList.remove(index);
    }
    
    public boolean isEntryAcceptable(SelectableEntry entry) {
        if (acceptorList.size() == 0) {
            return false;
        }
        
        for (int ctr = 0; ctr < acceptorList.size(); ctr++) {
            if (!((SelectableEntryAcceptor)acceptorList.get(ctr)).isEntryAcceptable(entry)) {
                return false;
            }
        }
        return true;
    }

}
