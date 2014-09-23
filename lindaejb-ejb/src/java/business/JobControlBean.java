/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import Exceptions.JobAlreadyKilledException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import model.Job;
import model.JobProxy;
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
    private ArrayList<Tool> tools;
    private int jobCount;
    
    @EJB
    ToolResultHandlerService resultHandler;
    @EJB
    ProcessManagerBean processManager;

    /**
     * @return the toolConfig
     */
    @Override
    public String getToolConfig() {
        return toolConfig.clone().html();
    }

    /**
     * @param toolConfig the toolConfig to set
     */
    public void setToolConfig(Document toolConfig) {
        this.toolConfig = toolConfig;
    }
    
    /**
     * @return the toolConfigPath
     */
    public String getToolConfigPath() {
        return toolConfigPath;
    }

    /**
     * @param toolConfigPath the toolConfigPath to set
     */
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
    public void setTools(ArrayList<Tool> tools) {
        this.tools = tools;
    }
    /**
     * @return the jobCount
     */
    @Override
    public int getJobCount() {
        return processManager.getJobs().size();
    }

    /**
     * @param jobCount the jobCount to set
     */
    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }
    
    @PostConstruct
    public void init() {
        parseTools();
    }

    @Override
    public void parseTools() {
        tools = new ArrayList<Tool>();
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
    public void killJob(String jobid) throws JobAlreadyKilledException {
        ExecuteWatchdog watchdog = processManager.getWatchdog(jobid);
        if (watchdog != null) {
            watchdog.destroyProcess();
        } else {
            throw new JobAlreadyKilledException();
        }
        processManager.removeJob(jobid);
    }

    @Override
    public String getEmail(String jobid) {
        for (int i = 0; i < processManager.getJobs().size(); i++) {
            if (processManager.getJobs().get(i).getJobID().equals(jobid)) {
                return processManager.getJobs().get(i).getEmail();
            }
        }
        return null;
    }

    @Override
    public void addEmail(String jobid, String email) {
        for (int i = 0; i < processManager.getJobs().size(); i++) {
            Job job = processManager.getJobs().get(i);
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
        return processManager.containsJob(jobid);
    }
    
    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void sendEmail(String email, String nodeID) {
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
    public void changeToolConfig(String toolConfigText) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(getToolConfigPath()));
            out.write(toolConfigText);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(JobControlBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        parseTools();
    }

    @Override
    public ArrayList<JobProxy> getJobs() {
        ArrayList<JobProxy> proxyJobs = new ArrayList<JobProxy>();
        
        for(Job job:processManager.getJobs()){
            JobProxy proxyJob = new JobProxy(job.getJobID(), job.getEmail(), job.getPid());
            proxyJobs.add(proxyJob);
        }
        
        return proxyJobs;
    }
}
