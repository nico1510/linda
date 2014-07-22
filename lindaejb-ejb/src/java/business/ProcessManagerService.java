/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import Exceptions.JobAlreadyRunningException;
import java.util.ArrayList;
import javax.ejb.Local;
import model.Job;
import org.apache.commons.exec.ExecuteWatchdog;

/**
 *
 * @author nico
 */

@Local
public interface ProcessManagerService {

    void addWatchdog(String jobid, ExecuteWatchdog watchdog);
    ExecuteWatchdog getWatchdog(String jobid);
    void jobFinished(String nodeID, String toolID, boolean success, ArrayList<String> filePaths, String absolutePath);
    void jobStarted(ExecuteWatchdog watchdog, String jobID);
    ArrayList<Job> getJobs();
    void addJob(String jobid) throws JobAlreadyRunningException;
    void removeJob(String jobid);
    public boolean containsJob(String jobid);
    
}
