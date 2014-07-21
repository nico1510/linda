/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import Exceptions.JobAlreadyKilledException;
import Exceptions.JobAlreadyRunningException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import model.Job;
import model.Tool;
import org.apache.commons.exec.ExecuteWatchdog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 *
 * @author nico
 */
@Singleton
@Lock(LockType.READ)
public class JobControlBean implements JobControlService {

    @Resource(name = "mail/myMailSession")
    private Session mailSession;
    private Document toolConfig;
    private String toolConfigPath="/home/glassfish/glassfish3/tools/tools.xml";
    private ArrayList<Job> jobs;
    private ArrayList<Tool> tools;
    private int jobCount;
    
    @EJB
    ToolResultHandlerService resultHandler;

    /**
     * @return the toolConfig
     */
    @Override
    public Document getToolConfig() {
        return toolConfig.clone();
    }

    /**
     * @param toolConfig the toolConfig to set
     */
    @Override
    public void setToolConfig(Document toolConfig) {
        this.toolConfig = toolConfig;
    }
    
    /**
     * @return the toolConfigPath
     */
    @Override
    public String getToolConfigPath() {
        return toolConfigPath;
    }

    /**
     * @param toolConfigPath the toolConfigPath to set
     */
    @Override
    public void setToolConfigPath(String toolConfigPath) {
        this.toolConfigPath = toolConfigPath;
    }
    
    /**
     * @return the tools
     */
    @Override
    public ArrayList<Tool> getTools() {
        return tools;
    }

    /**
     * @param tools the tools to set
     */
    @Override
    public void setTools(ArrayList<Tool> tools) {
        this.tools = tools;
    }
    /**
     * @return the jobs
     */
    @Override
    public ArrayList<Job> getJobs() {
        return jobs;
    }

    /**
     * @param jobs the jobs to set
     */
    @Override
    public void setJobs(ArrayList<Job> jobs) {
        this.jobs = jobs;
    }

    /**
     * @return the jobCount
     */
    @Override
    public int getJobCount() {
        return this.getJobs().size();
    }

    /**
     * @param jobCount the jobCount to set
     */
    @Override
    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }
    
    @PostConstruct
    public void init() {
        jobs = new ArrayList<Job>();
        parseTools();
    }

    @Override
    public void parseTools() {
        tools = new ArrayList<Tool>();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getExternalContext().getSession(true);
        ExternalContext externalContext = facesContext.getExternalContext();
 //       InputStream in = externalContext.getResourceAsStream("/WEB-INF/tools.xml");
        InputStream in=null;
        try {
            in = new FileInputStream(new File(toolConfigPath));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JobControlBean.class.getName()).log(Level.SEVERE, null, "tools.xml was not found at "+toolConfigPath);
        }
        try {
            toolConfig = Jsoup.parse(in, null, "", Parser.xmlParser());
            toolConfig.outputSettings().prettyPrint(false);
            Elements toolspecs = toolConfig.getElementsByAttribute("id");
            for (int i = 0; i < toolspecs.size(); i++) {
                Tool tool = new Tool();
                tool.setToolID(toolspecs.get(i).getElementsByTag("tool").attr("id"));
                tool.setDisplayname(toolspecs.get(i).getElementsByTag("displayname").text());
                Elements outputfiles = toolspecs.get(i).getElementsByTag("save");
                ArrayList<String> outputFilenames = new ArrayList<String>();
                for (int j = 0; j < outputfiles.size(); j++) {
                    outputFilenames.add(outputfiles.get(j).text());
                }
                tool.setOutputfiles(outputFilenames);
                Elements inputfiles = toolspecs.get(i).getElementsByTag("input");
                ArrayList<String> inputFilenames = new ArrayList<String>();
                for (int k = 0; k < inputfiles.size(); k++) {
                    inputFilenames.add(inputfiles.get(k).text());
                }
                tool.setInputfiles(inputFilenames);
                tools.add(tool);
            }
        } catch (IOException ex) {
            Logger.getLogger(JobControlBean.class.getName()).log(Level.SEVERE, null, "tools.xml in WEB-INF folder not found");
        }

    }

    @Lock(LockType.WRITE)
    @Override
    public void addJob(String jobid) throws JobAlreadyRunningException {
        if (containsJob(jobid)) {
            throw new JobAlreadyRunningException();
        } else {
            Job job = new Job();
            job.setJobID(jobid);
            getJobs().add(job);
        }
    }

    @Override
    public void removeJob(String jobid) {
        for (int i = 0; i < getJobs().size(); i++) {
            if (getJobs().get(i).getJobID().equals(jobid)) {
                getJobs().remove(getJobs().get(i));
            }
        }
    }

    @Lock(LockType.WRITE)
    @Override
    public void killJob(String jobid) throws JobAlreadyKilledException {
        ExecuteWatchdog watchdog = getWatchdog(jobid);
        if (watchdog != null) {
            watchdog.destroyProcess();
        } else {
            throw new JobAlreadyKilledException();
        }
        removeJob(jobid);
    }

    @Override
    public void addWatchdog(String jobid, ExecuteWatchdog watchdog) {
        for (int i = 0; i < getJobs().size(); i++) {
            if (getJobs().get(i).getJobID().equals(jobid)) {
                getJobs().get(i).setWatchdog(watchdog);
            }
        }
    }

    @Override
    public ExecuteWatchdog getWatchdog(String jobid) {
        for (int i = 0; i < getJobs().size(); i++) {
            if (getJobs().get(i).getJobID().equals(jobid)) {
                return getJobs().get(i).getWatchdog();
            }
        }
        return null;
    }

    @Override
    public String getEmail(String jobid) {
        for (int i = 0; i < getJobs().size(); i++) {
            if (getJobs().get(i).getJobID().equals(jobid)) {
                return getJobs().get(i).getEmail();
            }
        }
        return null;
    }

    @Override
    public void addEmail(String jobid, String email) {
        for (int i = 0; i < getJobs().size(); i++) {
            Job job = getJobs().get(i);
            if (job.getJobID().equals(jobid)) {
                if (job.getEmail() == null) {
                    job.setEmail(email);
                } else {
                    job.setEmail(job.getEmail() + "," + email);
                }
            }
        }
    }

    /**
     * @return the containsJob
     */
    @Override
    public boolean containsJob(String jobid) {
        for (int i = 0; i < getJobs().size(); i++) {
            if (getJobs().get(i).getJobID().equals(jobid)) {
                return true;
            }
        }
        return false;
    }

    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private void sendEmail(String email, String nodeID) {
        try {
            // Create the message object
            Message message = new MimeMessage(mailSession);

            // Adjust the recipients. Here we have only one
            // recipient. The recipient's address must be
            // an object of the InternetAddress class.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(email, false));
            // Set the message's subject
            message.setSubject("Webschemex Tool analysis finished");

            // Insert the message's body
            message.setText("A job you subscribed to, is finished now."
                    + " You can view the results at http://linda.west.uni-koblenz.de/datasets"+ nodeID);

            // Adjust the date of sending the message
            Date timeStamp = new Date();
            message.setSentDate(timeStamp);

            // Use the 'send' static method of the Transport
            // class to send the message
            Transport.send(message);
        } catch (MessagingException ex) {
            Logger.getLogger(JobControlBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void jobStarted(ExecuteWatchdog watchdog, String jobID) {
        addWatchdog(jobID, watchdog);
    }

    @Override
    public void jobFinished(String nodeID, String toolID, boolean success, ArrayList<String> filePaths, String absolutePath) {
        String jobID = nodeID + File.separator + toolID;
        if (success) {
            String email = getEmail(jobID);
            if (email != null) {
                sendEmail(email, nodeID);
            }
        }
        removeJob(jobID);
        resultHandler.handleResult(nodeID, toolID, success, filePaths, absolutePath);
    }
}
