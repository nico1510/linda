/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import java.io.Serializable;
import java.util.LinkedList;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;




/**
 *
 * @author nico
 */
@Named
@SessionScoped
public class UserBean implements Serializable{

    private LinkedList<String> uploadedDatasets;
    private LinkedList<String> startedJobs;
    
    /**
     * Creates a new instance of UserBean
     */
    public UserBean() {
    }
    
    @PostConstruct
    public void init(){
        this.uploadedDatasets=new LinkedList<String>();
        this.startedJobs=new LinkedList<String>();
    }

    /**
     * @return the uploadedDatasets
     */
    public LinkedList<String> getUploadedDatasets() {
        return uploadedDatasets;
    }

    /**
     * @param uploadedDatasets the uploadedDatasets to set
     */
    public void setUploadedDatasets(LinkedList<String> uploadedDatasets) {
        this.uploadedDatasets = uploadedDatasets;
    }

    /**
     * @return the startedJobs
     */
    public LinkedList<String> getStartedJobs() {
        return startedJobs;
    }

    /**
     * @param startedJobs the startedJobs to set
     */
    public void setStartedJobs(LinkedList<String> startedJobs) {
        this.startedJobs = startedJobs;
    }
}
