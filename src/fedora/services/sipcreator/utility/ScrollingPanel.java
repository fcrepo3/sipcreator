package fedora.services.sipcreator.utility;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * This class represents a scrollable panel.  It can be added to a JScrollPane
 * and will automagically work.
 * <BR><BR>
 * @author Andy Scukanec (ags at cs dot cornell dot edu)
 * @version 1.0 11/7/2003
 */
public class ScrollingPanel extends JPanel implements Scrollable {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257565122414063928L;

	/**
	 * This is the unit increment size.
	 */
    private int smallIncrement = 5;
	
	/**
	 * This is the block increment size.
	 */
    private int largeIncrement = 50;
	
	/**
	 * Indicates whether or not height tracking is on.
	 */
    private boolean heightTracking;
	
	/**
	 * Indicates whether or not width tracking is on.
	 */
    private boolean widthTracking;
	
	/**
	 * This constructor initializes the ScrollingPanel with all indicated
	 * constructor arguments.
	 * <BR>
	 * @param layout The new components layout.
	 * @param newWidthTracking The new width tracking setting.
	 * @param newHeightTracking The new height tracking setting.
	 */
	public ScrollingPanel(LayoutManager layout, boolean newWidthTracking, 
			boolean newHeightTracking) {
		super(layout);
		heightTracking = newHeightTracking;
		widthTracking = newWidthTracking;
	}
	
	/**
	 * This constructor initializes the ScrollingPanel with the indicated
	 * height and width tracking setting and uses a <code>java.awt.FlowLayout
	 * </code>.
	 * <BR>
	 * @param newWidthTracking The new width tracking setting.
	 * @param newHeightTracking The new height tracking setting.
	 */
	public ScrollingPanel(boolean newWidthTracking, 
			boolean newHeightTracking) {
		this(new FlowLayout(), newWidthTracking, newHeightTracking);
	}
	
	/**
	 * <BR>
	 * @param layout
	 */
	public ScrollingPanel(LayoutManager layout) {
		this(layout, true, false);
	}
	
	/**
	 * This is the defualt constructor, and initializes the layout to be a
	 * <code>java.awt.FlowLayout</code>, and sets the width tracking to true
	 * and the height tracking to false.
	 */
	public ScrollingPanel() {
		this(new FlowLayout(), true, false);
	}
	
	/**
	 * This method sets the unit increment of the ScrollingPanel.
	 * <BR>
	 * @param newSmallIncrement The new unit increment of the ScrollingPanel.
	 */
	public void setSmallIncrement(int newSmallIncrement) {
		smallIncrement = newSmallIncrement;
	}
	
	/**
	 * This method sets the block increment of the ScrollingPanel.
	 * <BR>
	 * @param newLargeIncrement The new block increment of the ScrollingPanel.
	 */
	public void setLargeIncrement(int newLargeIncrement) {
		largeIncrement = newLargeIncrement;
	}
	
	/**
	 * Returns the prefferred viewport dimension.
	 * <BR>
	 * @return The prefferred viewport dimension.
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	/**
	 * Returns the current height tracking setting.
	 * <BR>
	 * @return The current height tracking setting. 
	 */
	public boolean getScrollableTracksViewportHeight() {
		return heightTracking;
	}
	
	/**
	 * Returns the current width tracking setting.
	 * <BR>
	 * @return The current width tracking setting.
	 */
	public boolean getScrollableTracksViewportWidth() {
		return widthTracking;
	}
	
	/**
	 * This method will return the number of pixels to shift the viewport when
	 * the user clicks on a scroll arrow.  This amount is called the unit
	 * incrment.
	 * <BR>
	 * @param visibleRect The visible rectangle of the ScrollingPanel.
	 * @param orientation The orientation of the ScrollingPanel.
	 * @param direction The direction which is being scrolled.
	 * @return The number of pixels to shift the viewport.
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, 
			int direction) {
		return smallIncrement;
	}
	
	/**
	 * This method will return the number of pixels to shift the viewport when
	 * the user clicks on an empty part of the scroll bar.  This amount is
	 * called the block incrment.
	 * <BR>
	 * @param visibleRect The visible rectangle of the ScrollingPanel.
	 * @param orientation The orientation of the ScrollingPanel.
	 * @param direction The direction which is being scrolled.
	 * @return The number of pixels to shift the viewport.
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, 
			int direction) {
		return largeIncrement;
	}
    
}

