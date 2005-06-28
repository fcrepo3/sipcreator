package fedora.services.sipcreator.utility;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtility {

    public static String getAttribute(Element node, String name) {
        if (!node.hasAttribute(name)) {
            throw new RuntimeException
            ("Missing " + name + " attribute in " + node.getNodeName() + " element");
        }
        return node.getAttribute(name);
    }
    
    public static String getAttribute(Element node, String namespace, String localName) {
        if (!node.hasAttributeNS(namespace, localName)) {
            throw new RuntimeException
            ("Missing " + namespace + ":" + localName + " attribute in " + node.getNodeName() + " element");
        }
        return node.getAttributeNS(namespace, localName);
    }
    
    public static Element fireElementNamed(Node parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            Node current = children.item(ctr);
            if (current.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!current.getNodeName().equals(name)) continue;
            return (Element)current;
        }
        throw new RuntimeException
        ("Missing " + name + " child element in " + parent.getNodeName() + " element");
    }
    
    public static Element firstElementNamed(Node parent, String name) {
        return fireElementNamed(parent, name, true);
    }
    
    public static Element fireElementNamed(Node parent, String name, boolean throwException) {
        NodeList children = parent.getChildNodes();
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            Node current = children.item(ctr);
            if (current.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!current.getNodeName().equals(name)) continue;
            return (Element)current;
        }
        if (throwException) {
            throw new RuntimeException
            ("Missing " + name + " child element in " + parent.getNodeName() + " element");
        }
        return null;
    }
    
    public static Element firstElementNamed(Node parent, String namespace, String localName) {
        return firstElementNamed(parent, namespace, localName, true);
    }
    
    public static Element firstElementNamed(Node parent, String namespace, String localName, boolean throwException) {
        NodeList children = parent.getChildNodes();
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            Node current = children.item(ctr);
            if (current.getNodeType() != Node.ELEMENT_NODE) continue;
            if (current.getNamespaceURI() == null && namespace != null) continue;
            if (current.getNamespaceURI() != null && namespace == null) continue;
            if (namespace != null && !current.getNamespaceURI().equals(namespace)) continue;
            if (!current.getLocalName().equals(localName)) continue;
            return (Element)current;
        }
        if (throwException) {
            throw new RuntimeException
            ("Missing " + namespace + ":" + localName + " child element in " + parent.getNodeName() + " element");
        }
        return null;
    }
    
    public static Vector allElementsNamed(Node parent, String name) {
        Vector result = new Vector();
        NodeList children = parent.getChildNodes();
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            Node current = children.item(ctr);
            if (current.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!current.getNodeName().equals(name)) continue;
            result.add(current);
        }
        return result;
    }
    
    public static Vector allElementsNamed(Node parent, String namespace, String localName) {
        Vector result = new Vector();
        NodeList children = parent.getChildNodes();
        for (int ctr = 0; ctr < children.getLength(); ctr++) {
            Node current = children.item(ctr);
            if (current.getNodeType() != Node.ELEMENT_NODE) continue;
            if (current.getNamespaceURI() == null && namespace != null) continue;
            if (current.getNamespaceURI() != null && namespace == null) continue;
            if (namespace != null && !current.getNamespaceURI().equals(namespace)) continue;
            if (!current.getLocalName().equals(localName)) continue;
            result.add(current);
        }
        return result;
    }
    
}
