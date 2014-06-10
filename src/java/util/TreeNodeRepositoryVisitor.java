/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import model.ProxyItem;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author nico
 */
public class TreeNodeRepositoryVisitor implements ItemVisitor {

    private TreeNode root = new DefaultTreeNode("root", null);
    private TreeNode currentNode;

    /**
     * @return the root
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(TreeNode root) {
        this.root = root;
    }

    @Override
    public void visit(Property property) throws RepositoryException {
        String type = property.getName();
        if (type.startsWith("text")) {
            if (type.equals("text_name")) {
                ProxyItem nameItem = (ProxyItem) currentNode.getData();
                nameItem.setName(property.getString());
            } else if (!property.getString().isEmpty()) {
                TreeNode propNode = new DefaultTreeNode(new ProxyItem(type, property.getPath(), property.getString()), currentNode);
            }
        } else if (!type.startsWith("jcr") && property.getValue() != null) {
            TreeNode propNode = new DefaultTreeNode(new ProxyItem(type, property.getPath(), "File"), currentNode);
        }
    }

    @Override
    public void visit(Node node) throws RepositoryException {

        String nodename = node.getName();
        if (!nodename.equals("jcr:system")) {

            if (!nodename.isEmpty()) {
                TreeNode newNode = new DefaultTreeNode(new ProxyItem("", node.getPath(), "Node"), root);
                currentNode = newNode;
            }

            if (node.hasProperties()) {
                PropertyIterator paktuell = node.getProperties();
                while (paktuell.hasNext()) {
                    paktuell.nextProperty().accept(this);
                }
            }
            if (node.hasNodes()) {
                NodeIterator naktuell = node.getNodes();
                while (naktuell.hasNext()) {
                    Node nextNode = naktuell.nextNode();
                    nextNode.accept(this);
                }
            }
        }
    }
}
