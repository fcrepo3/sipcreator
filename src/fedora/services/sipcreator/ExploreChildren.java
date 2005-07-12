package fedora.services.sipcreator;

import java.util.Vector;

import fedora.services.sipcreator.acceptor.UniversalAcceptor;

public class ExploreChildren implements Runnable {
    
    private UniversalAcceptor acceptor = new UniversalAcceptor();
    
    private SelectableEntry root;
    
    private SIPCreator creator;
    
    public ExploreChildren(SelectableEntry newRoot, SIPCreator newCreator) {
        root = newRoot;
        creator = newCreator;
    }
    
    public void run() {
        Vector queue = new Vector();
        queue.add(root);
        
        creator.getProgressBar().setIndeterminate(true);
        
        while (queue.size() > 0) {
            SelectableEntry entry = (SelectableEntry)queue.remove(0);
            
            for (int ctr = 0; ctr < entry.getChildCount(acceptor); ctr++) {
                queue.add(entry.getChildAt(ctr, acceptor));
                Thread.yield();
            }
            
            creator.getProgressBar().setValue(1);
        }
        
        creator.getProgressBar().setIndeterminate(false);
        creator.getProgressBar().setMaximum(1);
        creator.getProgressBar().setMinimum(0);
    }
    
}
