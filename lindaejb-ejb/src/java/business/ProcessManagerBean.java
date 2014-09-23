/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import Exceptions.JobAlreadyRunningException;
import java.io.File;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import model.Job;
import org.apache.commons.exec.ExecuteWatchdog;

/**
 *
 * @author nico
 */

@Singleton
@LocalBean
public class ProcessManagerBean {
    
    @EJB 
    JobControlService jobBean;
    @EJB
    ToolResultHandlerService resultHandler;
    
    private ArrayList<Job> jobs;
    

    /**
     * @return the jobs
     */
    public ArrayList<Job> getJobs() {
        return jobs;
    }

    /**
     * @param jobs the jobs to set
     */
    public void setJobs(ArrayList<Job> jobs) {
        this.jobs = jobs;
    }
    
    @PostConstruct
    public void init() {
        jobs = new ArrayList<Job>();
    }
    
    public void addWatchdog(String jobid, ExecuteWatchdog watchdog) {
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getJobID().equals(jobid)) {
                jobs.get(i).setWatchdog(watchdog);
            }
        }
    }
    
    public ExecuteWatchdog getWatchdog(String jobid) {
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getJobID().equals(jobid)) {
                return jobs.get(i).getWatchdog();
            }
        }
        return null;
    }    
    

    public void jobStarted(ExecuteWatchdog watchdog, String jobID) {
        addWatchdog(jobID, watchdog);
    }

    public void jobFinished(String nodeID, String toolID, boolean success, ArrayList<String> filePaths, String absolutePath) {
        String jobID = nodeID + File.separator + toolID;
        if (success) {
            String email = jobBean.getEmail(jobID);
            if (email != null) {
                jobBean.sendEmail(email, nodeID);
            }
        }
        removeJob(jobID);
        resultHandler.handleResult(nodeID, toolID, success, filePaths, absolutePath);
    }

    @Lock(LockType.WRITE)
    public void addJob(String jobid) throws JobAlreadyRunningException {
        if (containsJob(jobid)) {
            throw new JobAlreadyRunningException();
        } else {
            Job job = new Job();
            job.setJobID(jobid);
            getJobs().add(job);
        }
    }

    public void removeJob(String jobid) {
        for (int i = 0; i < getJobs().size(); i++) {
            if (getJobs().get(i).getJobID().equals(jobid)) {
                getJobs().remove(getJobs().get(i));
            }
        }
    }

    public boolean containsJob(String jobid) {
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getJobID().equals(jobid)) {
                return true;
            }
        }
        return false;
    }
    
}
