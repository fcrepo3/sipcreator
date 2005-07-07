package fedora.services.sipcreator;

import java.util.Vector;

import fedora.services.sipcreator.acceptor.UniversalAcceptor;

public class ExploreChildren implements Runnable {
    
    private UniversalAcceptor acceptor = new UniversalAcceptor();
    
    private SelectableEntry root;
    
    public ExploreChildren(SelectableEntry newRoot) {
        root = newRoot;
    }
    
    public void run() {
        Vector queue = new Vector();
        queue.add(root);
        
        while (queue.size() > 0) {
            SelectableEntry entry = (SelectableEntry)queue.remove(0);
            
            for (int ctr = 0; ctr < entry.getChildCount(acceptor); ctr++) {
                queue.add(entry.getChildAt(ctr, acceptor));
                Thread.yield();
            }
        }
        
        System.out.println("Finished");
    }
    
}
