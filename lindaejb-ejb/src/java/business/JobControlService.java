/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import Exceptions.JobAlreadyKilledException;
import java.util.ArrayList;
import javax.ejb.Remote;
import model.JobProxy;
import model.Tool;

/**
 *
 * @author nico
 */

@Remote
public interface JobControlService {

    void addEmail(String jobid, String email);
    boolean containsJob(String jobid);
    String getEmail(String jobid);
    int getJobCount();
    String getToolConfig();
    ArrayList<Tool> getTools();
    void killJob(String jobid) throws JobAlreadyKilledException;
    void parseTools();
    void sendEmail(String email, String nodeID);
    void changeToolConfig(String toolConfigText);
    ArrayList<JobProxy> getJobs();
    
}
