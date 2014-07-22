/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;

/**
 *
 * @author nico
 */
public class JobProxy implements Serializable{
    
    private String jobID;
    private String email;
    private int pid;
    
    public JobProxy(String jobID, String email, int pid){
        this.jobID = jobID;
        this.email = email;
        this.pid = pid;
    }

    /**
     * @return the jobID
     */
    public String getJobID() {
        return jobID;
    }

    /**
     * @param jobID the jobID to set
     */
    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the pid
     */
    public int getPid() {
        return this.pid;
    }

    /**
     * @param pid the pid to set
     */
    public void setPid(int pid) {
        this.pid = pid;
    }
    
}
