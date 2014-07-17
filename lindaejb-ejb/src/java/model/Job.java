/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import org.apache.commons.exec.ExecuteWatchdog;

/**
 *
 * @author nico
 */
public class Job implements Serializable{
    
    private String jobID;
    private ExecuteWatchdog watchdog;
    private String email;
    private int pid;

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
     * @return the watchdog
     */
    public ExecuteWatchdog getWatchdog() {
        return watchdog;
    }

    /**
     * @param watchdog the watchdog to set
     */
    public void setWatchdog(ExecuteWatchdog watchdog) {
        this.watchdog = watchdog;
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
        return watchdog.getPid();
    }

    /**
     * @param pid the pid to set
     */
    public void setPid(int pid) {
        this.pid = pid;
    }
    
}
