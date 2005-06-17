package fedora.services.sipcreator.utility;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author ags
 */
public class GUIUtility {

    /**
     * This method will return a JPanel with a border layout. The
     * left component of the border layout will be a JLabel with the
     * provided text. The center component will be the provided component.
     * @param msg The txt for the label.
     * @param c The component to be labelled.
     * @return A JPanel encapuslating the labelled component.
     */
    public static JPanel addLabelLeft(String msg, JComponent c) {
        JPanel toReturn = new JPanel(new BorderLayout());
        toReturn.add(new JLabel(msg), BorderLayout.WEST);
        toReturn.add(c, BorderLayout.CENTER);
        return toReturn;
    }
    
    /**
     * This method will show a dialog that will give the user the option to
     * see the stack trace of the exception. This method uses JOptionPane.
     * This is a blocking call and will disable the parent component until
     * the user has navigated the dialog.
     * @param parent The parent component of the dialog to be shown.
     * @param t The throwable to display, and, possibly show the stack trace.
     */
    public static void showExceptionDialog(Component parent, Throwable t) {
        showExceptionDialog(parent, t, "An exception has been thrown:");
    }
    
    public static void showExceptionDialog(Component parent, Throwable t, String message) {
        String msg = message + "\n" +
                     t.toString() + "\n\n" +
                     "Would you like to see the stack trace?";
        int choice = JOptionPane.showConfirmDialog(parent, msg, "Exception Caught",
                                                   JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        JOptionPane.showMessageDialog(parent, getStackTrace(t));
    }
    
    /**
     * This method takes in a throwable and returns a nicely formatted String representing
     * the stack trace, formatted as it would beformatted if it were printed to an output
     * stream.
     * @param t The throwable whose stack trace is to be converted into a string
     * @return The converted stack trace.
     */
    public static String getStackTrace(Throwable t) {
        StackTraceElement[] elements = t.getStackTrace();
        String trace = "\t";
        for (int ctr = 0; ctr < elements.length; ctr++) {
            trace += elements[ctr].getClassName() + "." + elements[ctr].getMethodName() +
                "(" + elements[ctr].getFileName() + ":" + elements[ctr].getLineNumber() + ")";
            if (ctr < elements.length - 1) {
                trace += "\n\tat ";
            }
        }
        return trace;
    }
    
}
