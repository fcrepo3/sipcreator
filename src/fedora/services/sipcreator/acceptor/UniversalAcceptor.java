package fedora.services.sipcreator.acceptor;

import fedora.services.sipcreator.SelectableEntry;

/**
 * This subclass of SelectableEntryAcceptor accepts all entry objects.
 * This is particularly useful when a method requires an acceptor, but
 * the programmer just wants the full view.
 * <br><br>
 * @author Andy Scukanec - (ags at cs dot cornell dot edu)
 */
public class UniversalAcceptor extends SelectableEntryAcceptor {

    public boolean isEntryAcceptable(SelectableEntry entry) {
        return true;
    }
   
}
