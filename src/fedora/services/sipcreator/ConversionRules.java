package fedora.services.sipcreator;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConversionRules {

    public String description;
    
    public final Vector namespaceList = new Vector();
    
    public final Vector datastreamTemplateList = new Vector();
    
    public final Vector objectTemplateList = new Vector(); 
    
    public static class Namespace {
        
        public String alias;
        
        public String uri;
        
        public String toString() {
            return "\tNamespace [alias=" + alias + ",uri=" + uri + "]";
        }
        
    }
    
    public static class DatastreamTemplate {
        
        public String description;
        
        public String nodeType;
        
        public final Vector attributeNameList = new Vector();
        
        public final Vector attributeValueList = new Vector();
        
        public String toString() {
            return "DT, " + nodeType;
        }
        
    }
    
    public static class ObjectTemplate extends DatastreamTemplate {
        
        public final Vector relationshipList = new Vector();
        
        public String toString() {
            return "OT, " + nodeType;
        }
        
    }
    
    public static class Relationship {
        
        public String name;
        
        public final Vector targetRelationshipList = new Vector();
        
        public final Vector targetNodeTypeList = new Vector();
        
        public String toString() {
            return "Rel, " + name;
        }
        
    }
    
    public String toString() {
        String toReturn = "ConversionRules [description=" + description + "]";
        for (int ctr = 0; ctr < namespaceList.size(); ctr++) {
            toReturn += "\n" + namespaceList.get(ctr);
        }
        for (int ctr = 0; ctr < datastreamTemplateList.size(); ctr++) {
            toReturn += "\n" + datastreamToString((DatastreamTemplate)datastreamTemplateList.get(ctr));
        }
        for (int ctr = 0; ctr < objectTemplateList.size(); ctr++) {
            toReturn += "\n" + objectToString((ObjectTemplate)objectTemplateList.get(ctr));
        }
        return toReturn;
    }

    private String datastreamToString(DatastreamTemplate datastream) {
        String toReturn = "\tDatastreamTemplate [nodeType=" + datastream.nodeType;
        toReturn += ",description=" + description + "]";
        for (int ctr = 0; ctr < datastream.attributeNameList.size(); ctr++) {
            toReturn += "\n\t\tAttr Name=" + datastream.attributeNameList.get(ctr) +
                        ", Attr Value=" + datastream.attributeValueList.get(ctr);
        }
        return toReturn;
    }
      
    private String objectToString(ObjectTemplate object) {
        String toReturn = "\tObjectTemplate [nodeType=" + object.nodeType;
        toReturn += ",description=" + description + "]";
        for (int ctr = 0; ctr < object.attributeNameList.size(); ctr++) {
            toReturn += "\n\t\tAttr Name=" + object.attributeNameList.get(ctr) +
                        ", Attr Value=" + object.attributeValueList.get(ctr);
        }
        for (int ctr = 0; ctr < object.relationshipList.size(); ctr++) {
            toReturn += "\n" + relationshipToString((Relationship)object.relationshipList.get(ctr));
        }
        return toReturn;
    }
    
    private String relationshipToString(Relationship relationship) {
        String toReturn = "\t\tRelationship [Name=" + relationship.name + "]";
        
        for (int ctr = 0; ctr < relationship.targetNodeTypeList.size(); ctr++) {
            toReturn += "\n\t\t\ttarget_" + ctr + "_rel=" + relationship.targetRelationshipList.get(ctr) + 
                        ", target_" + ctr + "_typ=" + relationship.targetNodeTypeList.get(ctr); 
        }
        
        return toReturn;
    }
    
    public ConversionRules(Document input) {
        Element conversionRulesElement =
            (Element)input.getElementsByTagName("conversionRules").item(0);
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
        namespace.alias = ensureAndGetAttribute(node, "alias");
        namespace.uri = ensureAndGetAttribute(node, "uri");
        return namespace;
    }
    
    private DatastreamTemplate handleDatastream(Element node, DatastreamTemplate datastream) {
        if (datastream == null) {
        	datastream = new DatastreamTemplate();
        }
        datastream.description = getDescription(node);
        datastream.nodeType = ensureAndGetAttribute(node, "nodeType");
        
        NodeList children = node.getChildNodes();
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            if (children.item(ctr).getNodeType() != Node.ELEMENT_NODE) continue;
            Element child = (Element)children.item(ctr);
            if (!child.getNodeName().equals("attribute")) continue;
            
            datastream.attributeNameList.add(ensureAndGetAttribute(child, "name"));
            datastream.attributeValueList.add(ensureAndGetAttribute(child, "value"));
        }

        return datastream;
    }
    
    private ObjectTemplate handleObject(Element node, ObjectTemplate object) {
    	if (object == null) {
    		object = new ObjectTemplate();
    	}
    	handleDatastream(node, object);
    	
        NodeList children = node.getChildNodes();
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            if (children.item(ctr).getNodeType() != Node.ELEMENT_NODE) continue;
            Element child = (Element)children.item(ctr);
            if (!child.getNodeName().equals("relationship")) continue;
            
            object.relationshipList.add(handleRelationship(child, new Relationship()));
        }

        return object;
    }
    
    private Relationship handleRelationship(Element node, Relationship relationship) {
        if (relationship == null) {
        	relationship = new Relationship();
        }
        relationship.name = ensureAndGetAttribute(node, "name");
        
        NodeList children = node.getChildNodes();
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            if (children.item(ctr).getNodeType() != Node.ELEMENT_NODE) continue;
            Element child = (Element)children.item(ctr);
            if (!child.getNodeName().equals("target")) continue;
            
            String primitive = ensureAndGetAttribute(child, "primitiveRel");
            if (!primitive.equals("tree:child") && !primitive.equals("tree:parent") &&
            	!primitive.equals("tree:descendant") && !primitive.equals("tree:ancestor")) {
                throw new RuntimeException("Illegal value for primitiveRel attribute: " + primitive);
            }
            relationship.targetRelationshipList.add(primitive);
            relationship.targetNodeTypeList.add(ensureAndGetAttribute(child, "nodeType"));
        }

        return relationship;
    }
    
    private String ensureAndGetAttribute(Element node, String name) {
        if (!node.hasAttribute(name)) {
            throw new RuntimeException
            ("Missing " + name + " attribute in " + node.getNodeName() + " tag");
        }
        return node.getAttribute(name);
    }
    
    private String getDescription(Element node) {
        NodeList children = node.getChildNodes();
        String result = null;
        
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            if (children.item(ctr).getNodeType() != Node.ELEMENT_NODE) continue;
            Element child = (Element)children.item(ctr);
            if (!child.getNodeName().equals("description")) continue;
            
            if (result != null) {
                throw new RuntimeException("At most 1 description node allowed!");
            }
            
            if (child.getChildNodes().getLength() != 1) {
                throw new RuntimeException("Invalid description tag");
            }
            if (child.getFirstChild().getNodeType() != Node.TEXT_NODE) {
                throw new RuntimeException("Invalid description tag");
            }
            
            result = child.getFirstChild().getNodeValue(); 
        }
        
        return result == null ? "" : result;
    }
    
}
