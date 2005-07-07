package fedora.services.sipcreator;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.MutableComboBoxModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import beowulf.util.DOMUtility;

public class ConversionRules extends Observable implements Observer { 
    
    public static class Namespace extends Observable {
        
        private String alias;
        
        private String uri;
       
        
        public String getAlias() {
            return alias;
        }
        
        public String getURI() {
            return uri;
        }
        
        
        public void setAlias(String newAlias) {
            alias = newAlias;
            setChanged();
            notifyObservers();
        }
        
        public void setURI(String newURI) {
            uri = newURI;
            setChanged();
            notifyObservers();
        }
        
        
        public String toString() {
            return "\tNamespace [alias=" + alias + ",uri=" + uri + "]";
        }
        
    }
    
    public static class DatastreamTemplate extends Observable {
        
        private String description;
        
        private String nodeType;
        
        private final Vector attributeNameList = new Vector();
        
        private final Vector attributeValueList = new Vector();
        
        
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
        
        
        public void setDescription(String newDescription) {
            description = newDescription;
            setChanged();
            notifyObservers();
        }
        
        public void setNodeType(String newNodeType) {
            nodeType = newNodeType;
            setChanged();
            notifyObservers();
        }
        
        public void addAttribute(String newName, String newValue) {
            insertAttribute(newName, newValue, getAttributeCount());
        }
        
        public void insertAttribute(String newName, String newValue, int index) {
            attributeNameList.insertElementAt(newName, index);
            attributeValueList.insertElementAt(newValue, index);
            setChanged();
            notifyObservers();
        }
        
        public void removeAttribute(int index) {
            attributeNameList.remove(index);
            attributeValueList.remove(index);
            setChanged();
            notifyObservers();
        }
        
        public void removeAttribute(String name) {
            removeAttribute(attributeNameList.indexOf(name));
        }
        
        
        public String toString() {
            return nodeType;
        }

    }
    
    public static class ObjectTemplate extends DatastreamTemplate implements Observer {
        
        private final Vector relationshipList = new Vector();
        
        
        public int getRelationshipCount() {
            return relationshipList.size();
        }
        
        public int indexOfRelationship(Relationship relationship) {
            return relationshipList.indexOf(relationship);
        }
        
        public Relationship getRelationship(int index) {
            return (Relationship)relationshipList.get(index);
        }
        
        
        public void addRelationship(Relationship newRelationship) {
            insertRelationship(newRelationship, relationshipList.size());
        }
        
        public void insertRelationship(Relationship newRelationship, int index) {
            relationshipList.insertElementAt(newRelationship, index);
            newRelationship.addObserver(this);
            setChanged();
            notifyObservers();
        }
        
        public void removeRelationship(int index) {
            Relationship relationship = (Relationship)relationshipList.remove(index);
            relationship.deleteObserver(this);
            setChanged();
            notifyObservers();
        }
        
        public void removeRelationship(Relationship relationship) {
            removeRelationship(relationshipList.indexOf(relationship));
        }
        
        
        public void update(Observable o, Object arg) {
            setChanged();
            notifyObservers(arg);
        }
        
    }
    
    public static class Relationship extends Observable {
        
        private String name;
        
        private final Vector targetRelationshipList = new Vector();
        
        private final Vector targetNodeTypeList = new Vector();
     
        
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
        
        
        public void setName(String newName) {
            name = newName;
            setChanged();
            notifyObservers();
        }
        
        public void addTarget(String newTargetRelationship, String newTargetNodeType) {
            insertTarget(newTargetRelationship, newTargetNodeType, getTargetCount());
        }
        
        public void insertTarget(String newTargetRelationship, String newTargetNodeType, int index) {
            targetRelationshipList.insertElementAt(newTargetRelationship, index);
            targetNodeTypeList.insertElementAt(newTargetNodeType, index);
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
    
    private final Vector datastreamTemplateList = new Vector();
    
    private final Vector objectTemplateList = new Vector(); 
    
    
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
    
    public int getDatastreamTemplateCount() {
        return datastreamTemplateList.size();
    }
    
    public int indexOfDatastreamTemplate(DatastreamTemplate template) {
        return datastreamTemplateList.indexOf(template);
    }
    
    public DatastreamTemplate getDatastreamTemplate(int index) {
        return (DatastreamTemplate)datastreamTemplateList.get(index);
    }
    
    public DatastreamTemplate getDatastreamTemplate(String nodeType) {
        for (int ctr = 0; ctr < getDatastreamTemplateCount(); ctr++) {
            if (getDatastreamTemplate(ctr).getNodeType().equals(nodeType)) {
                return getDatastreamTemplate(ctr);
            }
        }
        return null;
    }
    
    public int getObjectTemplateCount() {
        return objectTemplateList.size();
    }
    
    public int indexOfObjectTemplate(ObjectTemplate template) {
        return objectTemplateList.indexOf(template);
    }
    
    public ObjectTemplate getObjectTemplate(int index) {
        return (ObjectTemplate)objectTemplateList.get(index);
    }
    
    public ObjectTemplate getObjectTemplate(String nodeType) {
        for (int ctr = 0; ctr < getObjectTemplateCount(); ctr++) {
            if (getObjectTemplate(ctr).getNodeType().equals(nodeType)) {
                return getObjectTemplate(ctr);
            }
        }
        return null;
    }
    
    public MutableComboBoxModel getDatastreamComboBoxModel() {
        return new DefaultComboBoxModel(datastreamTemplateList);
    }
    
    
    
    public void setDescription(String newDescription) {
        description = newDescription;
        setChanged();
        notifyObservers();
    }
    
    
    public void addNamespace(Namespace newNamespace) {
        insertNamespace(newNamespace, namespaceList.size());
    }
    
    public void insertNamespace(Namespace newNamespace, int index) {
        namespaceList.insertElementAt(newNamespace, index);
        newNamespace.addObserver(this);
        setChanged();
        notifyObservers();
    }
    
    public void removeNamespace(int index) {
        Namespace namespace = (Namespace)namespaceList.remove(index);
        namespace.deleteObserver(this);
        setChanged();
        notifyObservers();
    }
    
    public void removeNamespace(Namespace namespace) {
        removeNamespace(namespaceList.indexOf(namespace));
    }
    
    
    public void addDatastreamTemplate(DatastreamTemplate newTemplate) {
        insertDatastreamTemplate(newTemplate, datastreamTemplateList.size());
    }

    public void insertDatastreamTemplate(DatastreamTemplate newTemplate, int index) {
        datastreamTemplateList.insertElementAt(newTemplate, index);
        newTemplate.addObserver(this);
        setChanged();
        notifyObservers();
    }
    
    public void removeDatastreamTemplate(int index) {
        DatastreamTemplate template = (DatastreamTemplate)datastreamTemplateList.get(index);
        template.deleteObserver(this);
        setChanged();
        notifyObservers();
    }
    
    public void removeDatastreamTemplate(DatastreamTemplate template) {
        removeDatastreamTemplate(datastreamTemplateList.indexOf(template));
    }
    
    
    public void addObjectTemplate(ObjectTemplate newTemplate) {
        insertObjectTemplate(newTemplate, objectTemplateList.size());
    }

    public void insertObjectTemplate(ObjectTemplate newTemplate, int index) {
        objectTemplateList.insertElementAt(newTemplate, index);
        newTemplate.addObserver(this);
        setChanged();
        notifyObservers();
    }
    
    public void removeObjectTemplate(int index) {
        ObjectTemplate template = (ObjectTemplate)objectTemplateList.get(index);
        template.deleteObserver(this);
        setChanged();
        notifyObservers();
    }
    
    public void removeObjectTemplate(ObjectTemplate template) {
        removeObjectTemplate(objectTemplateList.indexOf(template));
    }
    
    
    
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers(arg);
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
                namespaceList.add(handleNamespace(child, new Namespace()));
            } else if (name.equals("datastreamTemplate")) {
                datastreamTemplateList.add(handleDatastream(child, new DatastreamTemplate()));
            } else if (name.equals("objectTemplate")) {
                objectTemplateList.add(handleObject(child, new ObjectTemplate()));
            }
        }
    }

    
    
    private Namespace handleNamespace(Element node, Namespace namespace) {
    	if (namespace == null) {
    		namespace = new Namespace();
    	}
        namespace.setAlias(DOMUtility.getAttribute(node, "alias"));
        namespace.setURI(DOMUtility.getAttribute(node, "uri"));
        return namespace;
    }
    
    private DatastreamTemplate handleDatastream(Element node, DatastreamTemplate datastream) {
        if (datastream == null) {
        	datastream = new DatastreamTemplate();
        }
        datastream.setDescription(getDescription(node));
        datastream.setNodeType(DOMUtility.getAttribute(node, "nodeType"));
        
        Vector children = DOMUtility.allElementsNamed(node, "attribute");
        for (int ctr = 0; ctr < children.size(); ctr++) {
            Element child = (Element)children.get(ctr);
            datastream.addAttribute(DOMUtility.getAttribute(child, "name"),
                                    DOMUtility.getAttribute(child, "value"));
        }

        return datastream;
    }
    
    private ObjectTemplate handleObject(Element node, ObjectTemplate object) {
    	if (object == null) {
    		object = new ObjectTemplate();
    	}
    	handleDatastream(node, object);
    	
        Vector children = DOMUtility.allElementsNamed(node, "relationship");
        for (int ctr = 0; ctr < children.size(); ctr++) {
            Element child = (Element)children.get(ctr);
            object.addRelationship(handleRelationship(child, new Relationship()));
        }

        return object;
    }
    
    private Relationship handleRelationship(Element node, Relationship relationship) {
        if (relationship == null) {
        	relationship = new Relationship();
        }
        relationship.setName(DOMUtility.getAttribute(node, "name"));
        
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
