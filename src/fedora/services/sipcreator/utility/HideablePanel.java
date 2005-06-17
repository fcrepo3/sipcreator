package fedora.services.sipcreator.utility;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class HideablePanel extends JPanel {
    
    private static final long serialVersionUID = 3258125877689988919L;

    private EventHandler handler = new EventHandler();
    
    private JPanel centerPanel;
    
    private JComponent component;
    
    private JLabel nameLabel;
    
    private JComponent grippablePanel;
    
    public HideablePanel(JComponent c) {
        this(c, true);
    }

    public HideablePanel(JComponent c, boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        
        component = c;
        
        nameLabel = new JLabel();
        
        grippablePanel = (JComponent)Box.createVerticalStrut(5);
        grippablePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        grippablePanel.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        
        JButton expandButton = new JButton("-");
        expandButton.addActionListener(handler);
        
        JPanel tempNorthPanel = new JPanel(new BorderLayout());
        tempNorthPanel.add(nameLabel, BorderLayout.WEST);
        tempNorthPanel.add(new JPanel(), BorderLayout.CENTER);
        tempNorthPanel.add(expandButton, BorderLayout.EAST);
        tempNorthPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel tempCenterPanel = new JPanel(new BorderLayout());
        tempCenterPanel.add(component, BorderLayout.CENTER);
        tempCenterPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(grippablePanel, BorderLayout.SOUTH);
        centerPanel.add(tempCenterPanel, BorderLayout.CENTER);
        
        setLayout(new BorderLayout());
        add(tempNorthPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        setBorder(BorderFactory.createLineBorder(Color.black));
        
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }
    
    public void setName(String newName) {
        nameLabel.setText(newName);
    }
    
    public String getName() {
        return nameLabel.getText();
    }
    
    private class EventHandler extends MouseAdapter implements ActionListener, MouseMotionListener {
        
        private int oldY;
        
        private boolean dragValid = false;
        
        private boolean hidden = false;
        
        public void actionPerformed(ActionEvent ae) {
        	if (!(ae.getSource() instanceof JButton)) return;
        	String cmd = ae.getActionCommand();
        	JButton source = (JButton)ae.getSource();
        	
        	if (cmd == null || !cmd.equals("-") && !cmd.equals("+")) {
        		return;
        	}
        	
        	hidden = !hidden;
        	if (cmd.equals("+")) {
        		source.setText("-");
        		add(centerPanel, BorderLayout.CENTER);
        	} else if (cmd.equals("-")) {
        		remove(centerPanel);
        		source.setText("+");
        	}
        	
        	revalidate();
        }
        
        public void mousePressed(MouseEvent me) {
            if (me.getSource() != HideablePanel.this) return;
            oldY = me.getY();
            dragValid = oldY >= getHeight() - 6;
        }

        public void mouseDragged(MouseEvent me) {
            if (!dragValid) return;
            Dimension size = component.getPreferredSize();
            size.height = Math.max(size.height + me.getY() - oldY, 0);
            oldY = me.getY();
            component.setPreferredSize(size);
            ((JComponent)component.getParent()).revalidate();
        }

        public void mouseMoved(MouseEvent e) {}
        
    }

}
