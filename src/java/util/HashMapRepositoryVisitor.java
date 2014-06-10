/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 *
 * @author nico
 */

public class HashMapRepositoryVisitor implements ItemVisitor{

    private ArrayList<LinkedHashMap<String,String>> list = new ArrayList<LinkedHashMap<String,String>>();
    private LinkedHashMap<String,String> currentFolder;


    /**
     * @return the list
     */
    public ArrayList<LinkedHashMap<String,String>> getList() {
        return list;
    }

    /**
     * @param list the list to set
     */
    public void setList(ArrayList<LinkedHashMap<String,String>> list) {
        this.list = list;
    }

    /**
     * @return the currentFolder
     */
    public LinkedHashMap<String,String> getCurrentFolder() {
        return currentFolder;
    }

    /**
     * @param currentFolder the currentFolder to set
     */
    public void setCurrentFolder(LinkedHashMap<String,String> currentFolder) {
        this.currentFolder = currentFolder;
    }

    @Override
    public void visit(Property property) throws RepositoryException {
        String type = property.getName();
        if (type.startsWith("text")) {
            currentFolder.put(type, property.getString());
        } else if (!type.startsWith("jcr")){
            currentFolder.put(type, property.getPath());
        }
        //else other possible types

    }

    @Override
    public void visit(Node node) throws RepositoryException {

        String nodename = node.getName();
        if (!nodename.equals("jcr:system")) {
            
            if(!nodename.isEmpty()){
                LinkedHashMap<String,String> newFolder = new LinkedHashMap<String,String>();
                currentFolder=newFolder;
                currentFolder.put("text_nodeid", node.getPath());
                list.add(currentFolder);
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
                    Node nextNode=naktuell.nextNode();
                    nextNode.accept(this);
                }
            }
            
        }
        
     }

}
