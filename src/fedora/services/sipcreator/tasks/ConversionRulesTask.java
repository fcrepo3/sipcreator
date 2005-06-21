package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fedora.services.sipcreator.ConversionRules;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.utility.GUIUtility;
import fedora.services.sipcreator.utility.HideablePanel;
import fedora.services.sipcreator.utility.ScrollingPanel;
import fedora.services.sipcreator.utility.SemiEditableTableModel;

public class ConversionRulesTask extends JPanel implements ListSelectionListener {

    private static final long serialVersionUID = 3257853168707645496L;
    
    private static final Dimension DEFAULT_VIEWPORT_SIZE = new Dimension(256, 128);
    
    private LoadConversionRulesAction loadConversionRulesAction = new LoadConversionRulesAction();
    private LoadConversionRulesWebAction loadConversionRulesWebAction = new LoadConversionRulesWebAction();
    
    //Data structures and UI components involved with the conversion rules task
    private JLabel crulesLabel = new JLabel();
    
    private ConversionRules rules;
    
    private JTextArea descriptionArea = new JTextArea();
    
    private JTable namespaceTableDisplay;
    private SemiEditableTableModel namespaceTableModel;
    
    private JList templateListDisplay;
    private DefaultListModel templateListModel = new DefaultListModel();
    private JLabel templateTypeLabel = new JLabel();
    private JTextArea templateDescriptionArea = new JTextArea();
    private JTable templateAttributeTableDisplay;
    private SemiEditableTableModel templateAttributeTableModel;
    private JList relationshipListDisplay;
    private DefaultListModel relationshipListModel = new DefaultListModel();
    private JTable relationshipTargetTableDisplay;
    private SemiEditableTableModel relationshipTargetTableModel;
    
    private SIPCreator parent;
    
    
    public ConversionRulesTask(SIPCreator newParent) {
        parent = newParent;
        rules = new ConversionRules();
        
        //Minimum sizes are explicitly set so that labels with long text entries
        //will not keep the containing JSplitPane from resizing down past the point
        //at which all the text on the label is visible
        crulesLabel.setMinimumSize(new Dimension(1, 1));

        
        descriptionArea.setBackground(getBackground());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        
        namespaceTableModel = new SemiEditableTableModel(new Object[]{"alias", "uri"}, 0, new int[]{});
        namespaceTableDisplay = new JTable(namespaceTableModel);
        namespaceTableDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        namespaceTableDisplay.setCellSelectionEnabled(false);
        namespaceTableDisplay.setRowSelectionAllowed(true);
        namespaceTableDisplay.setPreferredScrollableViewportSize(DEFAULT_VIEWPORT_SIZE);
        
        templateListDisplay = new JList(templateListModel);
        templateListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateListDisplay.addListSelectionListener(this);
        
        templateDescriptionArea.setBackground(getBackground());
        templateDescriptionArea.setEditable(false);
        templateDescriptionArea.setLineWrap(true);
        templateDescriptionArea.setWrapStyleWord(true);
        
        templateAttributeTableModel = new SemiEditableTableModel(new Object[]{"name", "value"}, 0, new int[]{});
        templateAttributeTableDisplay = new JTable(templateAttributeTableModel);
        templateAttributeTableDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateAttributeTableDisplay.setCellSelectionEnabled(false);
        templateAttributeTableDisplay.setRowSelectionAllowed(true);
        templateAttributeTableDisplay.setPreferredScrollableViewportSize(DEFAULT_VIEWPORT_SIZE);
        
        relationshipListDisplay = new JList(relationshipListModel);
        relationshipListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationshipListDisplay.addListSelectionListener(this);
        
        relationshipTargetTableModel = new SemiEditableTableModel(new Object[]{"Node Type", "Relationship"}, 0, new int[]{});
        relationshipTargetTableDisplay = new JTable(relationshipTargetTableModel);
        relationshipTargetTableDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationshipTargetTableDisplay.setCellSelectionEnabled(false);
        relationshipTargetTableDisplay.setRowSelectionAllowed(true);
        relationshipTargetTableDisplay.setPreferredScrollableViewportSize(DEFAULT_VIEWPORT_SIZE);

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(createNorthPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
    }
    
    private JComponent createNorthPanel() {
        JPanel result = new JPanel(new BorderLayout());        
        result.add(crulesLabel, BorderLayout.CENTER);
        JPanel tempP2 = new JPanel(new GridLayout(1, 2));
        tempP2.add(new JButton(loadConversionRulesAction));
        tempP2.add(new JButton(loadConversionRulesWebAction));
        result.add(tempP2, BorderLayout.EAST);
        return result;
    }
    
    private JComponent createCenterPanel() {
        HideablePanel tempP2;
        Box tempP1 = Box.createVerticalBox();
        
        tempP2 = new HideablePanel(createScrollPane(descriptionArea));
        tempP2.setTitle("Description");
        tempP1.add(tempP2); tempP1.add(Box.createVerticalStrut(5));
        
        tempP2 = new HideablePanel(createScrollPane(namespaceTableDisplay));
        tempP2.setTitle("Namespaces");
        tempP1.add(tempP2); tempP1.add(Box.createVerticalStrut(5));

        tempP2 = new HideablePanel(getObjectComponent());
        tempP2.setTitle("Templates");
        tempP1.add(tempP2);
        
        ScrollingPanel scrollingPanel = new ScrollingPanel(new BorderLayout());
        scrollingPanel.add(tempP1, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane(scrollingPanel);
        //Remove the standard etched border on the ScrollPane
        scrollPane.setBorder(null);

        //This code creates a left border of 5 pixels on the VSB.  By putting the border on the
        //left of the vertical scrollbar, rather than the right of the scrolling panel, we ensure
        //that this border will only appear when the scrollbar appears.  If this were not the case
        //then the scrolling panel would span 5 fewer pixels than it should when the VSB was
        //not visible
        JScrollBar vsb = scrollPane.getVerticalScrollBar();
        Dimension vsbPrefSize = vsb.getPreferredSize();
        vsb.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        vsb.setPreferredSize(new Dimension(vsbPrefSize.width + 5, vsbPrefSize.height));
        
        return scrollPane;
    }
    
    private JComponent getObjectComponent() {
        JPanel tempP;
        
        JPanel topLeft = new JPanel(new BorderLayout());
        topLeft.add(new JLabel("Elements"), BorderLayout.NORTH);
        topLeft.add(createScrollPane(templateListDisplay), BorderLayout.CENTER);
        topLeft.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JSplitPane topRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tempP = new JPanel(new BorderLayout());
        tempP.add(new JLabel("Description"), BorderLayout.NORTH);
        tempP.add(createScrollPane(templateDescriptionArea), BorderLayout.CENTER);
        tempP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topRight.setLeftComponent(tempP); tempP = null;
        tempP = new JPanel(new BorderLayout());
        tempP.add(new JLabel("Attributes"), BorderLayout.NORTH);
        tempP.add(createScrollPane(templateAttributeTableDisplay), BorderLayout.CENTER);
        tempP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topRight.setRightComponent(tempP); tempP = null;
        topRight.setResizeWeight(0.5);
        topRight.setOneTouchExpandable(true);
        topRight.setBorder(null);
        
        JSplitPane bottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        tempP = new JPanel(new BorderLayout());
        tempP.add(new JLabel("Relationship Elements"), BorderLayout.NORTH);
        tempP.add(createScrollPane(relationshipListDisplay), BorderLayout.CENTER);
        tempP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottom.setLeftComponent(tempP); tempP = null;
        tempP = new JPanel(new BorderLayout());
        tempP.add(new JLabel("Relationship Targets"), BorderLayout.NORTH);
        tempP.add(createScrollPane(relationshipTargetTableDisplay), BorderLayout.CENTER);
        tempP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottom.setRightComponent(tempP); tempP = null;
        bottom.setResizeWeight(0.5);
        bottom.setOneTouchExpandable(true);
        bottom.setBorder(null);
        
        JSplitPane result1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane result2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        result1.setResizeWeight(0.5);
        result1.setOneTouchExpandable(true);
        result1.setBorder(BorderFactory.createLineBorder(Color.darkGray));
        result2.setResizeWeight(0.5);
        result2.setOneTouchExpandable(true);
        result2.setBorder(null);
        
        tempP = new JPanel(new BorderLayout());
        tempP.add(GUIUtility.addLabelLeft("Template Type:  ", templateTypeLabel), BorderLayout.NORTH);
        tempP.add(result2, BorderLayout.CENTER);
        
        result1.setLeftComponent(topLeft);
        result1.setRightComponent(tempP);
        result2.setLeftComponent(topRight);
        result2.setRightComponent(bottom);
        return result1;
    }
    
    private JScrollPane createScrollPane(JComponent c) {
        JScrollPane result = new JScrollPane(c);
        result.setBorder(BorderFactory.createLineBorder(Color.darkGray));
        return result;
    }
    
    
    public void updateRules(String sourceName, ConversionRules newRules) {
        rules = newRules;
        crulesLabel.setText(sourceName);
        descriptionArea.setText(newRules.description);
        
        while (namespaceTableModel.getRowCount() > 0) namespaceTableModel.removeRow(0);
        //datastreamListModel.clear();
        templateListModel.clear();
        
        for (int ctr = 0; ctr < newRules.namespaceList.size(); ctr++) {
            ConversionRules.Namespace namespace =
                (ConversionRules.Namespace)newRules.namespaceList.get(ctr);
            namespaceTableModel.addRow(new Object[]{namespace.alias, namespace.uri});
        }
        
        for (int ctr = 0; ctr < newRules.datastreamTemplateList.size(); ctr++) {
            //datastreamListModel.addElement(newRules.datastreamTemplateList.get(ctr));
            templateListModel.addElement(newRules.datastreamTemplateList.get(ctr));
        }
        
        for (int ctr = 0; ctr < newRules.objectTemplateList.size(); ctr++) {
            templateListModel.addElement(newRules.objectTemplateList.get(ctr));
        }
    }
    
    public ConversionRules getRules() {
        return rules;
    }

    public void openURL(String value) throws SAXException, IOException {
        loadConversionRulesWebAction.openURL(value);
    }
    
    public void openFile(File file) throws SAXException, IOException {
        loadConversionRulesAction.openFile(file);
    }
    

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == templateListDisplay) {
            templateTypeLabel.setText("");
            relationshipListModel.clear();
            while (relationshipTargetTableModel.getRowCount() > 0) {
                relationshipTargetTableModel.removeRow(0);
            }
            while (templateAttributeTableModel.getRowCount() > 0) {
                templateAttributeTableModel.removeRow(0);
            }
            
            Object selected = templateListDisplay.getSelectedValue();
            if (selected == null) return;
            
            if (!(selected instanceof ConversionRules.DatastreamTemplate)) return;
            ConversionRules.DatastreamTemplate casted = (ConversionRules.DatastreamTemplate)selected;
            
            templateDescriptionArea.setText(casted.description);
            for (int ctr = 0; ctr < casted.attributeNameList.size(); ctr++) {
                templateAttributeTableModel.addRow
                (new Object[]{casted.attributeNameList.get(ctr),
                              casted.attributeValueList.get(ctr)});
            }
            
            if (selected instanceof ConversionRules.ObjectTemplate) {
                templateTypeLabel.setText("Object Template");
                ConversionRules.ObjectTemplate casted2 = (ConversionRules.ObjectTemplate)selected;
                
                for (int ctr = 0; ctr < casted2.relationshipList.size(); ctr++) {
                    relationshipListModel.addElement(casted2.relationshipList.get(ctr));
                }
            } else {
                templateTypeLabel.setText("Datastream Template");
            }
        } else if (e.getSource() == relationshipListDisplay) {
            ConversionRules.Relationship selected =
                (ConversionRules.Relationship)relationshipListDisplay.getSelectedValue();
            if (selected == null) return;
            
            while (relationshipTargetTableModel.getRowCount() > 0) {
                relationshipTargetTableModel.removeRow(0);
            }
            
            for (int ctr = 0; ctr < selected.targetNodeTypeList.size(); ctr++) {
                relationshipTargetTableModel.addRow
                (new Object[]{selected.targetNodeTypeList.get(ctr),
                              selected.targetRelationshipList.get(ctr)});
            }
        }
    }
    
    
    private class LoadConversionRulesAction extends AbstractAction {
        
        private static final long serialVersionUID = 3690752916960983351L;

        private JFileChooser fileChooser = new JFileChooser(".");
        
        public LoadConversionRulesAction() {
            putValue(Action.NAME, "Load CRules");
            putValue(Action.SHORT_DESCRIPTION, "Load in a conversion rules file");
        }
        
        public void actionPerformed(ActionEvent ae) {
            try {
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int choice = fileChooser.showOpenDialog(parent);
                if (choice != JFileChooser.APPROVE_OPTION) return;
            
                openFile(fileChooser.getSelectedFile());
            } catch (Exception e) {
                GUIUtility.showExceptionDialog(parent, e);
            }
        }

        public void openFile(File file) throws IOException, SAXException {
            InputSource is = new InputSource(new FileInputStream(file));
            ConversionRules crules = new ConversionRules(parent.getXMLParser().parse(is));
            updateRules(file.getCanonicalPath(), crules);
        }
        
    }
    
    private class LoadConversionRulesWebAction extends AbstractAction {
        
        private static final long serialVersionUID = -332126288068464408L;

        public LoadConversionRulesWebAction() {
            putValue(Action.NAME, "Load CRules Web");
            putValue(Action.SHORT_DESCRIPTION, "Load in a conversion rules file from the web");
        }
        
        public void actionPerformed(ActionEvent ae) {
            try {
                String message = "What is the URL of the conversion rules file?";
                String urlString = JOptionPane.showInputDialog(parent, message);
                if (urlString == null || urlString.length() == 0) return;

                openURL(urlString);
            } catch (Exception e) {
                GUIUtility.showExceptionDialog(parent, e);
            }
        }
        
        public void openURL(String url) throws IOException, SAXException {
            ConversionRules crules = new ConversionRules(parent.getXMLParser().parse(url));
            updateRules(url, crules);
        }
        
    }
    
}
