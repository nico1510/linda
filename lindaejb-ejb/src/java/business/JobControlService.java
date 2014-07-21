/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import Exceptions.JobAlreadyKilledException;
import Exceptions.JobAlreadyRunningException;
import java.util.ArrayList;
import javax.ejb.Remote;
import model.Job;
import model.Tool;
import org.apache.commons.exec.ExecuteWatchdog;
import org.jsoup.nodes.Document;

/**
 *
 * @author nico
 */

@Remote
public interface JobControlService {

    void addEmail(String jobid, String email);

    void addJob(String jobid) throws JobAlreadyRunningException;

    void addWatchdog(String jobid, ExecuteWatchdog watchdog);

    boolean containsJob(String jobid);

    String getEmail(String jobid);

    int getJobCount();

    ArrayList<Job> getJobs();

    Document getToolConfig();

    String getToolConfigPath();

    ArrayList<Tool> getTools();

    ExecuteWatchdog getWatchdog(String jobid);

    void killJob(String jobid) throws JobAlreadyKilledException;

    void parseTools();

    void removeJob(String jobid);

    void setJobCount(int jobCount);

    void setJobs(ArrayList<Job> jobs);

    void setToolConfig(Document toolConfig);

    void setToolConfigPath(String toolConfigPath);

    void setTools(ArrayList<Tool> tools);

    void jobStarted(ExecuteWatchdog watchdog, String jobID);

    void jobFinished(String nodeID, String toolID, boolean success, ArrayList<String> filePaths, String absolutePath);
    
}
