package fedora.services.sipcreator;

import java.util.Hashtable;
import java.util.Observable;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.MutableComboBoxModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import beowulf.util.DOMUtility;
import beowulf.util.StreamUtility;

public class ConversionRules { 
    
    public static class Namespace {
        
        private final String alias;
        
        private String uri = new String();
       
        
        public Namespace(String newAlias) {
            alias = newAlias;
        }
        
        public Namespace(Namespace copy) {
            this(copy.getAlias());
            set(copy);
        }
        
        
        public void set(Namespace copy) {
            if (copy == this) return;
            uri = copy.getURI();
        }
        
        
        public String getAlias() {
            return alias;
        }
        
        public String getURI() {
            return uri;
        }
        
        
        public void setURI(String newURI) {
            uri = newURI;
        }
        
        
        public String toString() {
            return "\tNamespace [alias=" + alias + ",uri=" + uri + "]";
        }
        
    }
    
    public static class DatastreamTemplate {
        
        private String description = new String();
        
        private final String nodeType;
        
        private final Vector attributeNameList = new Vector();
        
        private final Vector attributeValueList = new Vector();

        private final Hashtable attributeMap = new Hashtable();
        
        
        public DatastreamTemplate(String newNodeType) {
            nodeType = newNodeType;
        }
        
        public DatastreamTemplate(DatastreamTemplate copy) {
            this(copy.getNodeType());
            set(copy);
        }
        
        
        public void set(DatastreamTemplate copy) {
            if (copy == this) return;
            setDescription(copy.getDescription());
            for (int ctr = 0; ctr < copy.getAttributeCount(); ctr++) {
                addAttribute(copy.getAttributeName(ctr), copy.getAttributeValue(ctr));
            }
        }
        
        
        public String getDescription() {
            return description;
        }
        
        public String getNodeType() {
            return nodeType;
        }
        
        public int getAttributeCount() {
            return attributeNameList.size();
        }
        
        public int indexOfAttribute(String name) {
            return attributeNameList.indexOf(name);
        }
        
        public String getAttributeName(int index) {
            return (String)attributeNameList.get(index);
        }
        
        public String getAttributeValue(int index) {
            return (String)attributeValueList.get(index);
        }
        
        public String getAttribute(String name) {
            return (String)attributeMap.get(name);
        }
        
        
        public void setDescription(String newDescription) {
            description = newDescription;
        }
        
        public String addAttribute(String newName, String newValue) {
            String oldValue = (String)attributeMap.put(newName, newValue);
            if (oldValue != null) {
                attributeValueList.setElementAt(newValue, indexOfAttribute(newName));
            } else {
                attributeNameList.add(newName);
                attributeValueList.add(newValue);
            }
            
            return oldValue;
        }
        
        public String removeAttribute(String name) {
            String oldValue = (String)attributeMap.remove(name);
            attributeNameList.remove(name);
            attributeValueList.remove(oldValue);
            
            return oldValue;
        }
        
        public String removeAttribute(int index) {
            return removeAttribute(getAttributeName(index));
        }
        
        
        public String toString() {
            return nodeType;
        }

    }
    
    public static class ObjectTemplate extends DatastreamTemplate {
        
        private final Vector relationshipList = new Vector();
        
        private final Hashtable relationshipMap = new Hashtable();
        
        
        public ObjectTemplate(String nodeType) {
            super(nodeType);
        }
        
        public ObjectTemplate(ObjectTemplate copy) {
            this(copy.getNodeType());
            set(copy);
        }
        
        
        public void set(ObjectTemplate copy) {
            if (copy == this) return;
            super.set(copy);
            for (int ctr = 0; ctr < copy.getRelationshipCount(); ctr++) {
                addRelationship(new Relationship(copy.getRelationship(ctr)));
            }
        }
        
        
        public int getRelationshipCount() {
            return relationshipList.size();
        }
        
        public int indexOfRelationship(Relationship relationship) {
            return relationshipList.indexOf(relationship);
        }
        
        public Relationship getRelationship(int index) {
            return (Relationship)relationshipList.get(index);
        }
        
        public Relationship getRelationship(String name) {
            return (Relationship)relationshipMap.get(name);
        }
        
        
        public Relationship addRelationship(Relationship newRelationship) {
            Relationship oldRelationship = (Relationship)relationshipMap.put(newRelationship.getName(), newRelationship);
            if (oldRelationship == null) {
                relationshipList.add(newRelationship);
            }
            
            return oldRelationship;
        }
        
        public Relationship removeRelationship(Relationship relationship) {
            relationshipList.remove(relationship);
            relationshipMap.remove(relationship.getName());
            
            return relationship;
        }
        
        public Relationship removeRelationship(int index) {
            return removeRelationship(getRelationship(index));
        }
        
        public Relationship removeRelationship(String name) {
            return removeRelationship(getRelationship(name));
        }
        
    }
    
    public static class Relationship extends Observable {
        
        private final String name;
        
        private final Vector targetRelationshipList = new Vector();
        
        private final Vector targetNodeTypeList = new Vector();

        
        public Relationship(String newName) {
            name = newName;
        }
        
        public Relationship(Relationship copy) {
            this(copy.getName());
            set(copy);
        }
        
        
        public void set(Relationship copy) {
            if (copy == this) return;
            for (int ctr = 0; ctr < copy.getTargetCount(); ctr++) {
                addTarget(copy.getTargetRelationship(ctr), copy.getTargetNodeType(ctr));
            }
        }
        
        
        public String getName() {
            return name;
        }
        
        public int getTargetCount() {
            return targetRelationshipList.size();
        }
        
        public String getTargetRelationship(int index) {
            return (String)targetRelationshipList.get(index);
        }
        
        public String getTargetNodeType(int index) {
            return (String)targetNodeTypeList.get(index);
        }
        
        
        public void addTarget(String newTargetRelationship, String newTargetNodeType) {
            targetRelationshipList.add(newTargetRelationship);
            targetNodeTypeList.add(newTargetNodeType);
            setChanged();
            notifyObservers();
        }
        
        public void removeTarget(int index) {
            targetRelationshipList.remove(index);
            targetNodeTypeList.remove(index);
            setChanged();
            notifyObservers();
        }
        
        
        public String toString() {
            return name;
        }
        
    }
    
    
    private String description = new String();
    
    private final Vector namespaceList = new Vector();
    private final Hashtable namespaceMap = new Hashtable();
    
    private final Vector datastreamList = new Vector();
    private final Hashtable datastreamMap = new Hashtable();
    
    private final Vector objectList = new Vector();
    private final Hashtable objectMap = new Hashtable();
    
    
    public String getDescription() {
        return description;
    }
    
    public int getNamespaceCount() {
        return namespaceList.size();
    }
    
    public int indexOfNamespace(Namespace namespace) {
        return namespaceList.indexOf(namespace);
    }
    
    public Namespace getNamespace(int index) {
        return (Namespace)namespaceList.get(index);
    }
    
    public Namespace getNamespace(String alias) {
        return (Namespace)namespaceMap.get(alias);
    }
    
    public int getDatastreamTemplateCount() {
        return datastreamList.size();
    }
    
    public int indexOfDatastreamTemplate(DatastreamTemplate template) {
        return datastreamList.indexOf(template);
    }
    
    public DatastreamTemplate getDatastreamTemplate(int index) {
        return (DatastreamTemplate)datastreamList.get(index);
    }
    
    public DatastreamTemplate getDatastreamTemplate(String nodeType) {
        return (DatastreamTemplate)datastreamMap.get(nodeType);
    }
    
    public int getObjectTemplateCount() {
        return objectList.size();
    }
    
    public int indexOfObjectTemplate(ObjectTemplate template) {
        return objectList.indexOf(template);
    }
    
    public ObjectTemplate getObjectTemplate(int index) {
        return (ObjectTemplate)objectList.get(index);
    }
    
    public ObjectTemplate getObjectTemplate(String nodeType) {
        return (ObjectTemplate)objectMap.get(nodeType);
    }
    
    public MutableComboBoxModel getDatastreamComboBoxModel() {
        return new DefaultComboBoxModel(datastreamList);
    }
    
    
    
    public void setDescription(String newDescription) {
        description = newDescription;
    }
    
    
    public Namespace addNamespace(Namespace newNamespace) {
        Namespace oldNamespace = (Namespace)namespaceMap.put(newNamespace.getAlias(), newNamespace);
        if (oldNamespace == null) {
            namespaceList.add(newNamespace);
        }
        
        return oldNamespace;
    }
    
    public Namespace removeNamespace(Namespace namespace) {
        namespaceList.remove(namespace);
        namespaceMap.remove(namespace.getAlias());
        
        return namespace;
    }
    
    public Namespace removeNamespace(int index) {
        return removeNamespace(getNamespace(index));
    }
    
    public Namespace removeNamespace(String nodeType) {
        return removeNamespace(getNamespace(nodeType));
    }
    
    
    public DatastreamTemplate addDatastreamTemplate(DatastreamTemplate newDT) {
        DatastreamTemplate oldDT = (DatastreamTemplate)datastreamMap.put(newDT.getNodeType(), newDT);
        if (oldDT == null) {
            datastreamList.add(newDT);
        }
        
        return oldDT;
    }
    
    public DatastreamTemplate removeDatastreamTemplate(DatastreamTemplate template) {
        datastreamMap.remove(template.getNodeType());
        datastreamList.remove(template);
        
        return template;
    }

    public DatastreamTemplate removeDatastreamTemplate(String nodeType) {
        return removeDatastreamTemplate(getDatastreamTemplate(nodeType));
    }
    
    public DatastreamTemplate removeDatastreamTemplate(int index) {
        return removeDatastreamTemplate(getDatastreamTemplate(index));
    }    
    
    
    public ObjectTemplate addObjectTemplate(ObjectTemplate newOT) {
        ObjectTemplate oldOT = (ObjectTemplate)objectMap.put(newOT.getNodeType(), newOT);
        if (oldOT == null) {
            objectList.add(newOT);
        }
        
        return oldOT;
    }
    
    public ObjectTemplate removeObjectTemplate(ObjectTemplate template) {
        objectList.remove(template);
        objectMap.remove(template.getNodeType());
        
        return template;
    }
    
    public ObjectTemplate removeObjectTemplate(int index) {
        return removeObjectTemplate(getObjectTemplate(index));
    }

    public ObjectTemplate removeObjectTemplate(String nodeType) {
        return removeObjectTemplate(getObjectTemplate(nodeType));
    }
    
    
    
    public String toString() {
        String toReturn = "ConversionRules [description=" + getDescription() + "]";
        for (int ctr = 0; ctr < getNamespaceCount(); ctr++) {
            toReturn += "\n" + getNamespace(ctr);
        }
        for (int ctr = 0; ctr < getDatastreamTemplateCount(); ctr++) {
            toReturn += "\n" + datastreamToString(getDatastreamTemplate(ctr));
        }
        for (int ctr = 0; ctr < getObjectTemplateCount(); ctr++) {
            toReturn += "\n" + objectToString(getObjectTemplate(ctr));
        }
        return toReturn;
    }

    private String datastreamToString(DatastreamTemplate datastream) {
        String toReturn = "\tDatastreamTemplate [nodeType=" + datastream.getNodeType();
        toReturn += ",description=" + description + "]";
        for (int ctr = 0; ctr < datastream.getAttributeCount(); ctr++) {
            toReturn += "\n\t\tAttr Name=" + datastream.getAttributeName(ctr) +
                        ", Attr Value=" + datastream.getAttributeValue(ctr);
        }
        return toReturn;
    }
      
    private String objectToString(ObjectTemplate object) {
        String toReturn = "\tObjectTemplate [nodeType=" + object.getNodeType();
        toReturn += ",description=" + description + "]";
        for (int ctr = 0; ctr < object.getAttributeCount(); ctr++) {
            toReturn += "\n\t\tAttr Name=" + object.getAttributeName(ctr) +
                        ", Attr Value=" + object.getAttributeValue(ctr);
        }
        for (int ctr = 0; ctr < object.getRelationshipCount(); ctr++) {
            toReturn += "\n" + relationshipToString(object.getRelationship(ctr));
        }
        return toReturn;
    }
    
    private String relationshipToString(Relationship relationship) {
        String toReturn = "\t\tRelationship [Name=" + relationship.getName() + "]";
        
        for (int ctr = 0; ctr < relationship.getTargetCount(); ctr++) {
            toReturn += "\n\t\t\ttarget_" + ctr + "_rel=" + relationship.getTargetRelationship(ctr) + 
                        ", target_" + ctr + "_typ=" + relationship.getTargetNodeType(ctr); 
        }
        
        return toReturn;
    }
    
    
    
    public String toXML() {
        StringBuffer result = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        result.append("<conversionRules>");
        
        result.append("<description>");
        result.append(StreamUtility.enc(description));
        result.append("</description>");
        
        for (int ctr = 0; ctr < namespaceList.size(); ctr++) {
            Namespace ns = (Namespace)namespaceList.get(ctr);
            result.append("<namespace alias=\"");
            result.append(StreamUtility.enc(ns.getAlias()));
            result.append("\" uri=\"");
            result.append(StreamUtility.enc(ns.getURI()));
            result.append("\"/>");
        }
        
        for (int ctr = 0; ctr < datastreamList.size(); ctr++) {
            appendDatastream(result, (DatastreamTemplate)datastreamList.get(ctr));
        }
        
        for (int ctr = 0; ctr < objectList.size(); ctr++) {
            appendObject(result, (ObjectTemplate)objectList.get(ctr));
        }
        
        result.append("</conversionRules>");
        return result.toString();
    }
    
    private void appendDatastream(StringBuffer result, DatastreamTemplate dt) {
        result.append("<datastreamTemplate nodeType=\"");
        result.append(StreamUtility.enc(dt.getNodeType()));
        result.append("\">");
        
        if (dt.getDescription() != null) {
            result.append("<description>");
            result.append(StreamUtility.enc(dt.getDescription()));
            result.append("</description>");
        }
        
        for (int ctr = 0; ctr < dt.getAttributeCount(); ctr++) {
            result.append("<attribute name=\"");
            result.append(StreamUtility.enc(dt.getAttributeName(ctr)));
            result.append("\" value=\"");
            result.append(StreamUtility.enc(dt.getAttributeValue(ctr)));
            result.append("\"/>");
        }
        
        result.append("</datastreamTemplate>");
    }
    
    private void appendObject(StringBuffer result, ObjectTemplate ot) {
        result.append("<objectTemplate nodeType=\"");
        result.append(StreamUtility.enc(ot.getNodeType()));
        result.append("\">");
        
        if (ot.getDescription() != null) {
            result.append("<description>");
            result.append(StreamUtility.enc(ot.getDescription()));
            result.append("</description>");
        }
        
        for (int ctr = 0; ctr < ot.getAttributeCount(); ctr++) {
            result.append("<attribute name=\"");
            result.append(StreamUtility.enc(ot.getAttributeName(ctr)));
            result.append("\" value=\"");
            result.append(StreamUtility.enc(ot.getAttributeValue(ctr)));
            result.append("\"/>");
        }
        
        for (int ctr1 = 0; ctr1 < ot.getRelationshipCount(); ctr1++) {
            Relationship rel = ot.getRelationship(ctr1);
            result.append("<relationship name=\"");
            result.append(StreamUtility.enc(rel.getName()));
            result.append("\">");
            
            for (int ctr2 = 0; ctr2 < rel.getTargetCount(); ctr2++) {
                result.append("<target primitiveRel=\"");
                result.append(StreamUtility.enc(rel.getTargetRelationship(ctr2)));
                result.append("\" nodeType=\"");
                result.append(StreamUtility.enc(rel.getTargetRelationship(ctr2)));
                result.append("\"/>");
            }
            
            result.append("</relationship>");
        }
        
        result.append("</objectTemplate>");
    }
    
    
    public ConversionRules() {
    }
    
    public ConversionRules(Document input) {
        Element conversionRulesElement = input.getDocumentElement();
        description = getDescription(conversionRulesElement);

        NodeList children = conversionRulesElement.getChildNodes();
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            if (children.item(ctr).getNodeType() != Node.ELEMENT_NODE) continue;
            Element child = (Element)children.item(ctr);
            String name = child.getNodeName();
            if (name.equals("namespace")) {
                namespaceList.add(handleNamespace(child));
            } else if (name.equals("datastreamTemplate")) {
                datastreamList.add(handleDatastream(child));
            } else if (name.equals("objectTemplate")) {
                objectList.add(handleObject(child));
            }
        }
    }

    
    public void clear() {
        setDescription(new String());
        while (getNamespaceCount() > 0) {
            removeNamespace(0);
        }
        while (getDatastreamTemplateCount() > 0) {
            removeDatastreamTemplate(0);
        }
        while (getObjectTemplateCount() > 0) {
            removeObjectTemplate(0);
        }
        System.gc();
    }

    public void set(ConversionRules copy) {
        if (copy == this) return;
        clear();
        
        setDescription(copy.getDescription());
        
        for (int ctr = 0; ctr < copy.getNamespaceCount(); ctr++) {
            addNamespace(new Namespace(copy.getNamespace(ctr)));
        }
        
        for (int ctr = 0; ctr < copy.getDatastreamTemplateCount(); ctr++) {
            addDatastreamTemplate(new DatastreamTemplate(copy.getDatastreamTemplate(ctr)));
        }
        
        for (int ctr = 0; ctr < copy.getObjectTemplateCount(); ctr++) {
            addObjectTemplate(new ObjectTemplate(copy.getObjectTemplate(ctr)));
        }
    }
    
    
    
    private Namespace handleNamespace(Element node) {
        Namespace namespace = new Namespace(DOMUtility.getAttribute(node, "alias"));
        namespace.setURI(DOMUtility.getAttribute(node, "uri"));
        return namespace;
    }
    
    private DatastreamTemplate handleDatastream(Element node) {
        DatastreamTemplate datastream = new DatastreamTemplate(DOMUtility.getAttribute(node, "nodeType"));
        datastream.setDescription(getDescription(node));
        
        Vector children = DOMUtility.allElementsNamed(node, "attribute");
        for (int ctr = 0; ctr < children.size(); ctr++) {
            Element child = (Element)children.get(ctr);
            datastream.addAttribute(DOMUtility.getAttribute(child, "name"),
                                    DOMUtility.getAttribute(child, "value"));
        }

        return datastream;
    }
    
    private ObjectTemplate handleObject(Element node) {
        ObjectTemplate object = new ObjectTemplate(DOMUtility.getAttribute(node, "nodeType"));
        object.setDescription(getDescription(node));
        
        Vector children = DOMUtility.allElementsNamed(node, "attribute");
        for (int ctr = 0; ctr < children.size(); ctr++) {
            Element child = (Element)children.get(ctr);
            object.addAttribute(DOMUtility.getAttribute(child, "name"),
                                    DOMUtility.getAttribute(child, "value"));
        }

        children = DOMUtility.allElementsNamed(node, "relationship");
        for (int ctr = 0; ctr < children.size(); ctr++) {
            Element child = (Element)children.get(ctr);
            object.addRelationship(handleRelationship(child));
        }

        return object;
    }
    
    private Relationship handleRelationship(Element node) {
        Relationship relationship = new Relationship(DOMUtility.getAttribute(node, "name"));
        
        Vector children = DOMUtility.allElementsNamed(node, "target");
        for (int ctr = 0; ctr < children.size(); ctr++) {
            Element child = (Element)children.get(ctr);
            String primitive = DOMUtility.getAttribute(child, "primitiveRel");
            
            if (!primitive.equals("tree:child") && !primitive.equals("tree:parent") &&
            	!primitive.equals("tree:descendant") && !primitive.equals("tree:ancestor")) {
                throw new RuntimeException("Illegal value for primitiveRel attribute: " + primitive);
            }
            
            relationship.addTarget(primitive, DOMUtility.getAttribute(child, "nodeType"));
        }

        return relationship;
    }
    
    private String getDescription(Element node) {
        String result = null;
        
        Vector children = DOMUtility.allElementsNamed(node, "description");
        if (children.size() == 0) {
            return "";
        }
        if (children.size() > 1) { 
            throw new RuntimeException("At most 1 description node allowed!");
        }
            
        Element child = (Element)children.get(0);
        if (child.getChildNodes().getLength() == 0) {
            return "";
        }
        if (child.getChildNodes().getLength() > 1) {
            throw new RuntimeException("Invalid description tag");
        }
        if (child.getFirstChild().getNodeType() != Node.TEXT_NODE) {
            throw new RuntimeException("Invalid description tag");
        }
            
        result = child.getFirstChild().getNodeValue(); 
        
        return result == null ? "" : result;
    }

}
