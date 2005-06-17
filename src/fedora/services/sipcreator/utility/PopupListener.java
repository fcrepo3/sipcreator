package fedora.services.sipcreator.utility;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 * This class is a simple implementation of a mouse listener that shows
 * a popup menu upon receiving a popup trigger event.
 * @author Andy Scukanec (ags at cs dot cornell dot edu)
 */
public class PopupListener extends MouseAdapter {
    
    protected JPopupMenu popup;
    
    /**
     * Creates a popup listener that will show the given popup menu.
     * @param newPopup The popup menu to show.
     */
    public PopupListener(JPopupMenu newPopup) {
        popup = newPopup;
    }
    
    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
}

