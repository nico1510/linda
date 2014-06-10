/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author nico
 */
public class ProxyItem {
    private String name;
    private String path;
    private String content;
    
    public ProxyItem(String type,String value,String content){
        this.name = type;
        this.path = value;
        this.content = content;
    }
   

    /**
     * @return the type
     */
    public String getName() {
        return name;
    }

    /**
     * @param type the type to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getPath() {
        return path;
    }

    /**
     * @param value the value to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
}
