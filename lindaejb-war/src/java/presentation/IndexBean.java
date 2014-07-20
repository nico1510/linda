/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package presentation;

import business.ContentService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author nico
 */
@ManagedBean
@ViewScoped
public class IndexBean implements Serializable {

    @EJB
    private ContentService contentBean;
    
    private ArrayList<LinkedHashMap<String, String>> repositoryContent;
    private String queryString;

    /**
     * Creates a new instance of IndexBean
     */
    @PostConstruct
    public void init() {
        this.repositoryContent = new ArrayList<LinkedHashMap<String, String>>();
        ArrayList<LinkedHashMap<String, String>> originalContent = contentBean.getRepositoryContent();
        LinkedHashMap<String, String> copiedEntry;
        for(int i=0;i<originalContent.size();i++){
            copiedEntry = new LinkedHashMap<String, String>();
            copiedEntry.putAll(originalContent.get(i));
            this.repositoryContent.add(copiedEntry);
        }
    }
    
    /**
     * @return the repositoryContent
     */
    public ArrayList<LinkedHashMap<String, String>> getRepositoryContent() {
        filterRepositoryContent();
        return repositoryContent;
    }

    /**
     * @param repositoryContent the repositoryContent to set
     */
    public void setRepositoryContent(ArrayList<LinkedHashMap<String, String>> repositoryContent) {
        this.repositoryContent = repositoryContent;
    }
    
    
    public String search(){
        Logger.getLogger(IndexBean.class.getName()).log(Level.INFO, "search for "+this.queryString);
        return "datasets.xhtml?faces-redirect=true&search="+this.queryString;
    }

    public void filterRepositoryContent() {
        if (this.queryString != null && !this.queryString.isEmpty()) {
            this.init();
            ArrayList<LinkedHashMap<String, String>> filteredContent = new ArrayList<LinkedHashMap<String, String>>();
            for(int i=0;i<repositoryContent.size();i++){
                LinkedHashMap<String, String> currentfolder = repositoryContent.get(i);
                String searchString = currentfolder.get("text_name")+" "+currentfolder.get("text_tags");
                if(Pattern.compile(Pattern.quote(queryString), Pattern.CASE_INSENSITIVE).matcher(searchString).find()){
                    filteredContent.add(currentfolder);
                }
            }
            this.repositoryContent = filteredContent;
        }
    }

    /**
     * @return the queryString
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * @param queryString the queryString to set
     */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
}
