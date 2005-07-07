package fedora.services.sipcreator;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTabbedPane;

public class MetadataView extends JTabbedPane {

    private static final long serialVersionUID = -1848000834772395409L;

    private CloseCurrentTabAction closeCurrentTabAction = new CloseCurrentTabAction();
    
    public MetadataView() {
        super(JTabbedPane.TOP);
    }
    
    
    public CloseCurrentTabAction getCloseCurrentTabAction() {
        return closeCurrentTabAction;
    }
    
    public void closeCurrentTab() {
        closeCurrentTabAction.closeCurrentTab();
    }
    
    public void closeAllTabs() {
        while (getTabCount() > 0) {
            remove(0);
        }
    }
    
    public int getIndexByToolTip(String tip) {
        int index = 0;
        for (index = 0; index < getTabCount(); index++) {
            if (getToolTipTextAt(index).equals(tip)) {
                return index;
            }
        }
        return -1;
    }
    
    public void updateMetadata() {
        for (int ctr = 0; ctr < getTabCount(); ctr++) {
            ((SelectableEntryPanel)getComponentAt(ctr)).updateMetadata();
        }
    }
    
    
    public class CloseCurrentTabAction extends AbstractAction {
        
        private static final long serialVersionUID = -1317113261942287869L;

        private CloseCurrentTabAction() {
            putValue(Action.NAME, "Close Tab");
            putValue(Action.SHORT_DESCRIPTION, "Closes the current tab");
        }
        
        public void actionPerformed(ActionEvent ae) {
            closeCurrentTab();
        }
        
        public void closeCurrentTab() {
            int index = getSelectedIndex();
            if (index < 0) return;
            
            ((SelectableEntryPanel)getComponentAt(index)).updateMetadata();
            remove(index);
        }
        
    }
    
}
