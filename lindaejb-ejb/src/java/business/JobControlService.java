/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import Events.JobFinishedEvent;
import Events.JobStartedEvent;
import Exceptions.JobAlreadyKilledException;
import Exceptions.JobAlreadyRunningException;
import java.util.ArrayList;
import javax.ejb.Local;
import model.Job;
import model.Tool;
import org.apache.commons.exec.ExecuteWatchdog;
import org.jsoup.nodes.Document;

/**
 *
 * @author nico
 */

@Local
public interface JobControlService {

    void addEmail(String jobid, String email);

    void addJob(String jobid) throws JobAlreadyRunningException;

    void addWatchdog(String jobid, ExecuteWatchdog watchdog);
    
    void assignWatchdog(JobStartedEvent event);

    void jobFinished(JobFinishedEvent event);

    /**
     * @return the containsJob
     */
    boolean containsJob(String jobid);

    String getEmail(String jobid);

    /**
     * @return the jobCount
     */
    int getJobCount();

    /**
     * @return the jobs
     */
    ArrayList<Job> getJobs();

    /**
     * @return the toolConfig
     */
    Document getToolConfig();

    /**
     * @return the toolConfigPath
     */
    String getToolConfigPath();

    /**
     * @return the tools
     */
    ArrayList<Tool> getTools();

    ExecuteWatchdog getWatchdog(String jobid);

    void killJob(String jobid) throws JobAlreadyKilledException;

    void parseTools();

    void removeJob(String jobid);

    /**
     * @param jobCount the jobCount to set
     */
    void setJobCount(int jobCount);

    /**
     * @param jobs the jobs to set
     */
    void setJobs(ArrayList<Job> jobs);

    /**
     * @param toolConfig the toolConfig to set
     */
    void setToolConfig(Document toolConfig);

    /**
     * @param toolConfigPath the toolConfigPath to set
     */
    void setToolConfigPath(String toolConfigPath);

    /**
     * @param tools the tools to set
     */
    void setTools(ArrayList<Tool> tools);
    
}
