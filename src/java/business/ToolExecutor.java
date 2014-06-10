/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import Events.JobFinishedEvent;
import Events.JobStartedEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ExecuteWatchdogBash;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import util.FormatConverter;

/**
 *
 * @author nico
 */
@MessageDriven(mappedName = "jms/Queue", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ToolExecutor implements MessageListener {

    @Inject
    FormatConverter Converter;
    @Inject
    Event<JobFinishedEvent> jobFinishedSource;
    @Inject
    Event<JobStartedEvent> watchdogProducedSource;

    public ToolExecutor() {
    }

    @Override
    public void onMessage(Message message) {
        MapMessage mmsg;
        mmsg = (MapMessage) message;
        try {
            String nodeID = mmsg.getString("text_nodeid");
            String launchInfo = mmsg.getString("launchInfo");
            String format = mmsg.getString("format");
            launchTool(nodeID, launchInfo, format);
            Logger.getLogger(ToolExecutor.class.getName()).log(Level.INFO, nodeID+"_"+launchInfo+"_"+format);

        } catch (JMSException ex) {
            Logger.getLogger(ToolExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

    public void launchTool(String nodeID, String launchInfo, String format) {

        boolean eventThrown = false;
        Document doc = Jsoup.parse(launchInfo, "", Parser.xmlParser());
        String toolID = doc.getElementsByTag("tool").attr("id");
        Elements arguments = doc.getElementsByTag("arguments").first().children();
        Boolean bash = false;
        if (!doc.getElementsByTag("bash").isEmpty()) {
            bash = true;
        }
        
        String acceptedFormats = doc.getElementsByTag("rdfFormat").first().text();
        Boolean convert = !acceptedFormats.contains(format);

        Long timeout = Long.parseLong(doc.getElementsByTag("timeout").first().text());
        CommandLine cmdLine = new CommandLine(arguments.first().text());

        //Generate unique tempfolder name
        String id = UUID.randomUUID().toString();

        //directory where all tools and scripts are stored
        File workingDir = new File("/home/glassfish/glassfish3/tools");

        //Create temporary directory for result files
        String tempDirPath = workingDir + File.separator + toolID + id;
        new File(tempDirPath).mkdir();
        File tempDir = new File(tempDirPath);

        if (convert) {
            Elements inputElements = doc.getElementsByTag("input");
            for (int i = 0; i < inputElements.size(); i++) {
                String isFileString = inputElements.get(i).attr("file");
                if (!isFileString.equals("false")) {
                    String inputFilePath = inputElements.get(i).text();
                    try {
                        String convertedPath = Converter.convert(inputFilePath, tempDir + File.separator, format, acceptedFormats);
                        inputElements.get(i).text(convertedPath);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ToolExecutor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ToolExecutor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        for (int i = 1; i < arguments.size(); i++) {
            cmdLine.addArgument(arguments.get(i).text());
        }

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog;
        if (bash) {
            watchdog = new ExecuteWatchdogBash(timeout);
        } else {
            watchdog = new ExecuteWatchdog(timeout);
        }
        executor.setWatchdog(watchdog);
        executor.setWorkingDirectory(tempDir);
        //     executor.getStreamHandler().stop();
        watchdogProducedSource.fire(new JobStartedEvent(watchdog, nodeID + File.separator + toolID));

        try {
            executor.execute(cmdLine, resultHandler);
            resultHandler.waitFor();
            
            ArrayList<String> filePaths = new ArrayList<String>();

            if (!watchdog.killedProcess()) {
                Elements filesToSave = doc.getElementsByTag("save");
                for (int i = 0; i < filesToSave.size(); i++) {
                    String fileName = filesToSave.get(i).text();
                    String fileToSave = tempDir + File.separator + fileName;
                    filePaths.add(fileToSave);
                }
                jobFinishedSource.fire(new JobFinishedEvent(nodeID, toolID, true, filePaths,tempDir.getAbsolutePath() ));
                eventThrown = true;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(ToolExecutor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecuteException ex) {
            Logger.getLogger(ToolExecutor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ToolExecutor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (!eventThrown) {
                jobFinishedSource.fire(new JobFinishedEvent(nodeID, toolID, false, null, tempDir.getAbsolutePath()));
            }
        }
    }
}
