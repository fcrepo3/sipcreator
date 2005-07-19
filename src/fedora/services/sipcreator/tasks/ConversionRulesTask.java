package fedora.services.sipcreator.tasks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.xml.sax.InputSource;

import beowulf.gui.JGraph;
import beowulf.gui.SemiEditableTableModel;
import beowulf.gui.Utility;
import fedora.services.sipcreator.Constants;
import fedora.services.sipcreator.ConversionRules;
import fedora.services.sipcreator.ConversionRulesGraph;
import fedora.services.sipcreator.ConversionRulesJGraph;
import fedora.services.sipcreator.SIPCreator;
import fedora.services.sipcreator.ConversionRules.DatastreamTemplate;

public class ConversionRulesTask extends JPanel implements ListSelectionListener, Constants {

    private static final long serialVersionUID = 3257853168707645496L;
    
    private static final Dimension DEFAULT_VIEWPORT_SIZE = new Dimension(256, 128);
    
    private static final String ADD_ATTRIBUTE_TEXT = "Add Attribute";
    private static final String DEL_ATTRIBUTE_TEXT = "Delete Attribute";
    private static final String ADD_NAMESPACE_TEXT = "Add Namespace";
    private static final String DEL_NAMESPACE_TEXT = "Delete Namespace";
    
    private UpdateListener updater = new UpdateListener();
    
    private LoadConversionRulesAction loadConversionRulesAction;
    private LoadConversionRulesWebAction loadConversionRulesWebAction;
    private SaveConversionRulesAction saveConversionRulesAction;
    private GenerateGraphAction generateGraphAction;
    
    private ConversionRulesJGraph graphView = new ConversionRulesJGraph();
    
    //Data structures and UI components involved with the conversion rules task
    private JLabel crulesLabel = new JLabel();
    
    private ConversionRules rules = new ConversionRules();
    
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
    
    private SIPCreator creator;
    
    
    public ConversionRulesTask(SIPCreator newCreator) {
        creator = newCreator;
        
        loadConversionRulesAction = new LoadConversionRulesAction();
        loadConversionRulesWebAction = new LoadConversionRulesWebAction();
        saveConversionRulesAction = new SaveConversionRulesAction();
        generateGraphAction = new GenerateGraphAction();
        
        //Minimum sizes are explicitly set so that labels with long text entries
        //will not keep the containing JSplitPane from resizing down past the point
        //at which all the text on the label is visible
        crulesLabel.setMinimumSize(new Dimension(1, 1));

        
        descriptionArea.setBackground(getBackground());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.getDocument().addDocumentListener(updater);
        
        namespaceTableModel = new SemiEditableTableModel(new Object[]{"alias", "uri"}, 0, new int[]{1});
        namespaceTableModel.addTableModelListener(updater);
        namespaceTableDisplay = new JTable(namespaceTableModel);
        namespaceTableDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        namespaceTableDisplay.setCellSelectionEnabled(false);
        namespaceTableDisplay.setRowSelectionAllowed(true);
        namespaceTableDisplay.setPreferredScrollableViewportSize(DEFAULT_VIEWPORT_SIZE);
        namespaceTableDisplay.setBackground(getBackground());
        
        templateListDisplay = new JList(templateListModel);
        templateListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateListDisplay.addListSelectionListener(this);
        
        templateDescriptionArea.setBackground(getBackground());
        templateDescriptionArea.setLineWrap(true);
        templateDescriptionArea.setWrapStyleWord(true);
        templateDescriptionArea.getDocument().addDocumentListener(updater);
        templateDescriptionArea.setEditable(false);
        
        templateAttributeTableModel = new SemiEditableTableModel(new Object[]{"name", "value"}, 0, new int[]{1});
        templateAttributeTableModel.addTableModelListener(updater);
        templateAttributeTableDisplay = new JTable(templateAttributeTableModel);
        templateAttributeTableDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateAttributeTableDisplay.setCellSelectionEnabled(false);
        templateAttributeTableDisplay.setRowSelectionAllowed(true);
        templateAttributeTableDisplay.setPreferredScrollableViewportSize(DEFAULT_VIEWPORT_SIZE);
        templateAttributeTableDisplay.setBackground(getBackground());
        
        relationshipListDisplay = new JList(relationshipListModel);
        relationshipListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationshipListDisplay.addListSelectionListener(this);
        
        relationshipTargetTableModel = new SemiEditableTableModel(new Object[]{"Node Type", "Relationship"}, 0, new int[]{});
        relationshipTargetTableDisplay = new JTable(relationshipTargetTableModel);
        relationshipTargetTableDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationshipTargetTableDisplay.setCellSelectionEnabled(false);
        relationshipTargetTableDisplay.setRowSelectionAllowed(true);
        relationshipTargetTableDisplay.setPreferredScrollableViewportSize(DEFAULT_VIEWPORT_SIZE);

        JSplitPane centerPane = new JSplitPane();
        centerPane.setLeftComponent(createLeftPanel());
        centerPane.setRightComponent(createRightPanel());
        centerPane.setOneTouchExpandable(true);
        centerPane.setResizeWeight(0.5);
        
        setLayout(new BorderLayout());
        add(centerPane, BorderLayout.CENTER);
    }
    
    private JComponent createRightPanel() {
        return graphView;
    }
    
    private JComponent createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        left.add(createNorthPanel(), BorderLayout.NORTH);
        left.add(createCenterPanel(), BorderLayout.CENTER);
        return left;
    }
    
    private JComponent createNorthPanel() {
        JPanel result = new JPanel(new BorderLayout());        
        result.add(crulesLabel, BorderLayout.CENTER);
        JPanel tempP2 = new JPanel(new GridLayout(1, 0));
        tempP2.add(new JButton(loadConversionRulesAction));
        tempP2.add(new JButton(loadConversionRulesWebAction));
        tempP2.add(new JButton(saveConversionRulesAction));
        tempP2.add(new JButton(generateGraphAction));
        result.add(tempP2, BorderLayout.EAST);
        return result;
    }
    
    private JComponent createCenterPanel() {
        JTabbedPane result = new JTabbedPane();

        result.addTab("Description", new JScrollPane(descriptionArea));
        result.addTab("Namespaces", getNamespaceComponent());
        result.addTab("Templates", getObjectComponent());
        
        return result;
    }
    
    private JComponent getNamespaceComponent() {
        JPanel tempP1 = new JPanel();
        JButton tempB;
        
        tempB = new JButton(ADD_NAMESPACE_TEXT);
        tempB.addActionListener(updater);
        tempP1.add(tempB);
        tempB = new JButton(DEL_NAMESPACE_TEXT);
        tempB.addActionListener(updater);
        tempP1.add(tempB);
        
        JPanel result = new JPanel(new BorderLayout());
        result.add(createScrollPane(namespaceTableDisplay), BorderLayout.CENTER);
        result.add(tempP1, BorderLayout.SOUTH);
        return result;
    }
    
    private JComponent getAttributeComponent() {
        JPanel tempP1 = new JPanel();
        JButton tempB;
        
        tempB = new JButton(ADD_ATTRIBUTE_TEXT);
        tempB.addActionListener(updater);
        tempP1.add(tempB);
        tempB = new JButton(DEL_ATTRIBUTE_TEXT);
        tempB.addActionListener(updater);
        tempP1.add(tempB);
        
        JPanel result = new JPanel(new BorderLayout());
        result.add(createScrollPane(templateAttributeTableDisplay), BorderLayout.CENTER);
        result.add(tempP1, BorderLayout.SOUTH);
        return result;
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
        tempP.add(getAttributeComponent(), BorderLayout.CENTER);
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
        tempP.add(Utility.addLabelLeft("Template Type:  ", templateTypeLabel), BorderLayout.NORTH);
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
    

    public void updateRules() {
        descriptionArea.setText(rules.getDescription());
        
        while (namespaceTableModel.getRowCount() > 0) {
            namespaceTableModel.removeRow(0);
        }
        templateListModel.clear();
        templateDescriptionArea.setText("");

        for (int ctr = 0; ctr < rules.getNamespaceCount(); ctr++) {
            ConversionRules.Namespace namespace = rules.getNamespace(ctr);
            namespaceTableModel.addRow(new Object[]{namespace.getAlias(), namespace.getURI()});
        }
        
        for (int ctr = 0; ctr < rules.getDatastreamTemplateCount(); ctr++) {
            templateListModel.addElement(rules.getDatastreamTemplate(ctr));
        }
        
        for (int ctr = 0; ctr < rules.getObjectTemplateCount(); ctr++) {
            templateListModel.addElement(rules.getObjectTemplate(ctr));
        }
    }
    
    public void updateRules(String sourceName, ConversionRules newRules) {
        rules.set(newRules);
        
        crulesLabel.setText(sourceName);
        crulesLabel.setToolTipText(sourceName);
        
        updateRules();
    }
    
    public ConversionRules getRules() {
        return rules;
    }
    
    public JGraph getGraphView() {
        return graphView;
    }
    
    public LoadConversionRulesAction getLoadConversionRulesAction() {
        return loadConversionRulesAction;
    }

    public LoadConversionRulesWebAction getLoadConversionRulesWebAction() {
        return loadConversionRulesWebAction;
    }

    public SaveConversionRulesAction getSaveConversionRulesAction() {
        return saveConversionRulesAction;
    }
    
    public GenerateGraphAction getGenerateGraphAction() {
        return generateGraphAction;
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
            if (selected == null) {
                templateDescriptionArea.setEditable(false);
                return;
            }
            templateDescriptionArea.setEditable(true);
            
            if (!(selected instanceof ConversionRules.DatastreamTemplate)) return;
            ConversionRules.DatastreamTemplate casted = (ConversionRules.DatastreamTemplate)selected;
            
            templateDescriptionArea.setText(casted.getDescription());
            for (int ctr = 0; ctr < casted.getAttributeCount(); ctr++) {
                templateAttributeTableModel.addRow
                (new Object[]{casted.getAttributeName(ctr), casted.getAttributeValue(ctr)});
            }
            
            if (selected instanceof ConversionRules.ObjectTemplate) {
                templateTypeLabel.setText("Object Template");
                ConversionRules.ObjectTemplate casted2 = (ConversionRules.ObjectTemplate)selected;
                
                for (int ctr = 0; ctr < casted2.getRelationshipCount(); ctr++) {
                    relationshipListModel.addElement(casted2.getRelationship(ctr));
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
            
            for (int ctr = 0; ctr < selected.getTargetCount(); ctr++) {
                relationshipTargetTableModel.addRow
                (new Object[]{selected.getTargetNodeType(ctr), selected.getTargetRelationship(ctr)});
            }
        }
    }
    
    
    private class UpdateListener implements DocumentListener, TableModelListener, ActionListener {
        
        public void changedUpdate(DocumentEvent e) {
            updateDocument(e);
        }

        public void insertUpdate(DocumentEvent e) {
            updateDocument(e);
        }

        public void removeUpdate(DocumentEvent e) {
            updateDocument(e);
        }

        private void updateDocument(DocumentEvent e) {
            if (e.getDocument() == descriptionArea.getDocument()) {
                rules.setDescription(descriptionArea.getText());
            } else if (e.getDocument() == templateDescriptionArea.getDocument()) {
                Object selected = templateListDisplay.getSelectedValue();
                if (selected == null) return;
                ((DatastreamTemplate)selected).setDescription(templateDescriptionArea.getText());
            }
        }
        
        
        public void tableChanged(TableModelEvent e) {
            if (e.getType() != TableModelEvent.UPDATE) return;

            if (e.getSource() == namespaceTableModel) {
                String alias = (String)namespaceTableModel.getValueAt(e.getFirstRow(), 0);
                String newURI = (String)namespaceTableModel.getValueAt(e.getFirstRow(), 1);
                rules.getNamespace(alias).setURI(newURI);
            } else if (e.getSource() == templateAttributeTableModel) {
                String name = (String)templateAttributeTableModel.getValueAt(e.getFirstRow(), 0);
                String value = (String)templateAttributeTableModel.getValueAt(e.getFirstRow(), 1);
                ConversionRules.DatastreamTemplate selected =
                    (ConversionRules.DatastreamTemplate)templateListDisplay.getSelectedValue();
                selected.addAttribute(name, value);
            }
        }

        
        public void actionPerformed(ActionEvent ae) {
            String cmd = ae.getActionCommand();
            
            if (cmd == null) {
                return;
            } else if (cmd.equals(ADD_NAMESPACE_TEXT)) {
                addNamespace();
            } else if (cmd.equals(DEL_NAMESPACE_TEXT)) {
                deleteNamespace();
            } else if (cmd.equals(ADD_ATTRIBUTE_TEXT)) {
                addAttribute();
            } else if (cmd.equals(DEL_ATTRIBUTE_TEXT)) {
                deleteAttribute();
            }
        }
        
        private void addNamespace() {
            String result = JOptionPane.showInputDialog(creator, "What is the alias of the new namespace?");
            if (result == null) return;
            
            if (rules.getNamespace(result) != null) {
                JOptionPane.showMessageDialog(creator, "A namespace of that alias already exists!");
                return;
            }
            namespaceTableModel.addRow(new Object[]{result, ""});
            rules.addNamespace(new ConversionRules.Namespace(result));
        }

        private void deleteNamespace() {
            int selectedRow = namespaceTableDisplay.getSelectedRow();
            if (selectedRow == -1) return;
            namespaceTableModel.removeRow(selectedRow);
            rules.removeNamespace(selectedRow);
        }
        
        private void addAttribute() {
            ConversionRules.DatastreamTemplate selected =
                (ConversionRules.DatastreamTemplate)templateListDisplay.getSelectedValue();
            if (selected == null) return;
            
            String result = JOptionPane.showInputDialog(creator, "What is the name of the new attribute?");
            if (result == null) return;
            
            if (selected.getAttribute(result) != null) {
                JOptionPane.showMessageDialog(creator, "An attribute of that name already exists!");
                return;
            }
            templateAttributeTableModel.addRow(new Object[]{result, ""});
            selected.addAttribute(result, "");
        }
        
        private void deleteAttribute() {
            ConversionRules.DatastreamTemplate selected =
                (ConversionRules.DatastreamTemplate)templateListDisplay.getSelectedValue();
            if (selected == null) return;
            
            int selectedRow = templateAttributeTableDisplay.getSelectedRow();
            if (selectedRow == -1) return;
            templateAttributeTableModel.removeRow(selectedRow);
            selected.removeAttribute(selectedRow);
        }
        
    }
    
    public class LoadConversionRulesAction extends AbstractAction {
        
        private static final long serialVersionUID = 3690752916960983351L;

        private LoadConversionRulesAction() {
            //putValue(Action.NAME, "Load CRules");
            URL imgURL = creator.getURL(IMAGE_DIR_NAME + "gnome-folder.png");
            putValue(Action.SMALL_ICON, new ImageIcon(creator.getImage(imgURL)));
            putValue(Action.SHORT_DESCRIPTION, "Load in a conversion rules file");
        }
        
        public void actionPerformed(ActionEvent ae) {
            System.out.println(creator.getCodeBase());
            try {
                JFileChooser fileChooser = creator.getFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(creator.getXMLFilter());
                
                int choice = fileChooser.showOpenDialog(creator);
                if (choice != JFileChooser.APPROVE_OPTION) return;
            
                InputSource is = new InputSource(new FileInputStream(fileChooser.getSelectedFile()));
                ConversionRules crules = new ConversionRules(creator.getXMLParser().parse(is));
                updateRules(fileChooser.getSelectedFile().getCanonicalPath(), crules);
            } catch (Exception e) {
                Utility.showExceptionDialog(creator, e);
            }
        }

    }
    
    public class LoadConversionRulesWebAction extends AbstractAction {
        
        private static final long serialVersionUID = -332126288068464408L;

        private LoadConversionRulesWebAction() {
            //putValue(Action.NAME, "Load CRules Web");
            URL imgURL = creator.getURL(IMAGE_DIR_NAME + "gftp.png");
            putValue(Action.SMALL_ICON, new ImageIcon(creator.getImage(imgURL)));
            putValue(Action.SHORT_DESCRIPTION, "Load in a conversion rules file from the web");
        }
        
        public void actionPerformed(ActionEvent ae) {
            try {
                String message = "What is the URL of the conversion rules file?";
                String urlString = JOptionPane.showInputDialog(creator, message);
                if (urlString == null || urlString.length() == 0) return;

                ConversionRules crules = new ConversionRules(creator.getXMLParser().parse(urlString));
                updateRules(urlString, crules);
            } catch (Exception e) {
                Utility.showExceptionDialog(creator, e);
            }
        }
        
    }

    public class SaveConversionRulesAction extends AbstractAction {
        
        private static final long serialVersionUID = 2024410009193247878L;

        private SaveConversionRulesAction() {
            //putValue(Action.NAME, "Save CRules");
            URL imgURL = creator.getURL(IMAGE_DIR_NAME + "gnome-dev-floppy.png");
            putValue(Action.SMALL_ICON, new ImageIcon(creator.getImage(imgURL)));
            putValue(Action.SHORT_DESCRIPTION, "Save the conversion rules to a file");
        }
        
        public void actionPerformed(ActionEvent ae) {
            try {
                JFileChooser fileChooser = creator.getFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(creator.getXMLFilter());
                
                int choice = fileChooser.showSaveDialog(creator);
                if (choice != JFileChooser.APPROVE_OPTION) return;

                if (fileChooser.getSelectedFile().exists()) {
                    choice = JOptionPane.showConfirmDialog(creator, "File exists.  Overwrite?");
                    if (choice != JOptionPane.YES_OPTION) return;
                }
                saveFile(fileChooser.getSelectedFile());
            } catch (Exception e) {
                Utility.showExceptionDialog(creator, e);
            }
        }

        public void saveFile(File file) throws IOException {
            PrintWriter pw = new PrintWriter(new FileOutputStream(file));
            pw.print(rules.toXML());
            pw.close();
        }
        
    }
    
    public class GenerateGraphAction extends AbstractAction {
        
        private static final long serialVersionUID = 5055044742911980919L;

        private GenerateGraphAction() {
            //putValue(Action.NAME, "Generate Graph");
            URL imgURL = creator.getURL(IMAGE_DIR_NAME + "stock_reload.png");
            putValue(Action.SMALL_ICON, new ImageIcon(creator.getImage(imgURL)));
            putValue(Action.SHORT_DESCRIPTION, "Generate a graph based on the current rules and tree");
        }
        
        public void actionPerformed(ActionEvent ae) {
            if (creator.getFileSelectTask().getRootEntry() == null) return;
            graphView.setModel(new ConversionRulesGraph(rules, creator.getFileSelectTask().getRootEntry()));
            graphView.repaint();
        }
        
    }
    
}
