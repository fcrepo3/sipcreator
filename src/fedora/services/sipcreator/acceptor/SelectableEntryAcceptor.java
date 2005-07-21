package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SelectableEntry;

/**
 * This class defines the interface used by the SIPCreator program to
 * determine if a particular SelectableEntry should be visible during
 * some sort of operation.  Typically, this operation involves displaying
 * the entire tree of SelectableEntry objects.  All acceptors have the
 * option of "accepting" metadata entries or not.  This oddity is due
 * to the fact that metadata is often displayed in the same tree, but
 * is not actually wrapped in a SelectableEntry object since Metadata
 * is not individually selectable.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public abstract class SelectableEntryAcceptor {

    /** Member field which indicates acceptance of metadata entries */
    private boolean acceptsMetadata;
    
    /**
     * This method is the core method for all SelectableEntryAcceptor
     * subclasses.  Given a SelectableEntry, the acceptor object should
     * return true exactly when the entry is "accepted", and false
     * otherwise.  The semantics of being accepted are left up to
     * the calling function, and the criterion for defining which
     * SelectableEntry objects are accepted is left up to the implementing
     * subclasses.
     * <br><br>
     * @param entry The SelectableEntry object being queried.
     * @return True if and only if <code>entry</code> is accepted.
     */
    public abstract boolean isEntryAcceptable(SelectableEntry entry);
    
    /**
     * This method returns true if this acceptor object "accepts"
     * metadata entries.
     * <br><br>
     * @return True if and only if this SelectableEntryAcceptor "accepts"
     * metadata entries.
     */
    public boolean acceptsMetadata() {
        return acceptsMetadata;
    }
    
    /**
     * If <code>newAcceptsMetadata</code> is true, then this acceptor
     * object will accept metadata.  If it is false, then this acceptor
     * will reject metadata entries.
     * <br><br>
     * @param newAcceptsMetadata Whether or not to accept metadata entries.
     */
    public void setAcceptsMetadata(boolean newAcceptsMetadata) {
        acceptsMetadata = newAcceptsMetadata;
    }
    
}
