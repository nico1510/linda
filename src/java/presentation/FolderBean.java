/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package presentation;

import java.io.Serializable;
import java.util.LinkedHashMap;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author nico
 */

@ManagedBean
@ViewScoped
public class FolderBean implements Serializable {
    
    private LinkedHashMap<String, String> folder;
    private String nodeid;
    private String name;
    private String domain;
    private String publisher;
    private String source;
    private String description;
    private String examples;
    private String tags;
    private String rdfformat;
    
    /**
     * @return the folder
     */
    public LinkedHashMap<String, String> getFolder() {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(LinkedHashMap<String, String> folder) {
        this.folder = folder;
    }

    /**
     * @return the name
     */
    public String getName() {
        return getFolder().get("text_name");
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        getFolder().put("text_name", name);
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return getFolder().get("text_description");
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        getFolder().put("text_description", description);
    }
    
    /**
     * @return the domain
     */
    public String getDomain() {
        return getFolder().get("text_domain");
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(String domain) {
        getFolder().put("text_domain", domain);
    }
    /**
     * @return the examples
     */
    public String getExamples() {
        return getFolder().get("text_examples");
    }

    /**
     * @param examples the examples to set
     */
    public void setExamples(String examples) {
        getFolder().put("text_examples", examples);
    }

    /**
     * @return the nodeid
     */
    public String getNodeid() {
        return getFolder().get("text_nodeid");
    }

    /**
     * @param nodeid the nodeid to set
     */
    public void setNodeid(String nodeid) {
        getFolder().put("text_nodeid", nodeid);
    }

    /**
     * @return the publisher
     */
    public String getPublisher() {
        return getFolder().get("text_publisher");
    }

    /**
     * @param publisher the publisher to set
     */
    public void setPublisher(String publisher) {
        getFolder().put("text_publisher", publisher);
    }

    /**
     * @return the downloadURL
     */
    public String getSource() {
        return getFolder().get("text_source");
    }

    /**
     * @param downloadURL the downloadURL to set
     */
    public void setSource(String source) {
        getFolder().put("text_source", source);
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return getFolder().get("text_tags");
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(String tags) {
        getFolder().put("text_tags", tags);
    }
    
    
    public String getRdfformat() {
        return getFolder().get("text_rdfformat");
    }

    /**
     * @param tags the tags to set
     */
    public void setRdfformat(String rdfformat) {
        getFolder().put("text_rdfformat", rdfformat);
    }

}
