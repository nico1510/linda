/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import Exceptions.JobAlreadyRunningException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 *
 * @author nico
 */
@Stateless
public class ToolLauncherBean implements ToolLauncherBeanService, Serializable {

    @Resource(mappedName = "jms/Queue")
    Queue queue;
    @Resource(mappedName = "jms/ConnectionFactory")
    QueueConnectionFactory factory;
    @EJB
    JobControlService jobBean;
    @EJB
    RepositoryService repBean;
    @EJB
    ProcessManagerBean processManager;
    
    @Override
    public void launchTool(String toolID, String jobID, LinkedHashMap<String, String> folder) {

        String nodeID = folder.get("text_nodeid");
        String format = folder.get("text_rdfformat");

        try {
            processManager.addJob(jobID);
            
            Document toolCfg = Jsoup.parse(jobBean.getToolConfig(), "", Parser.xmlParser());

            Element tool = toolCfg.getElementsByAttributeValue("id", toolID).first();
            Elements inputs = tool.getElementsByTag("input");
            for (int i = 0; i < inputs.size(); i++) {
                String isFileString = inputs.get(i).attr("file");
                String input = inputs.get(i).text();
                String propPath = folder.get(input);
                if (!isFileString.equals("false")) {
                    if (!input.equals("dataset")) {
                        format = FilenameUtils.getExtension(input);
                    }
                    propPath = repBean.getPhysicalBinaryPath(propPath); // if the property is a File, the real phyiscal path should be sent
                } else {
                    propPath = propPath.replaceFirst("/", ""); // if the property is not a file, it's most likely some sort of dataset ID. Therefore the raw ID is extracted here : "/ID" -> "ID"
                }
                inputs.get(i).text(propPath);
            }

            String launchInfo = tool.toString();

            try {
                QueueConnection connection = factory.createQueueConnection();
                QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
                QueueSender sender = session.createSender(queue);
                sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                MapMessage msg = session.createMapMessage();
                msg.setString("text_nodeid", nodeID);
                msg.setString("launchInfo", launchInfo);
                msg.setString("format", format);
                sender.send(msg);
                session.close();
                connection.close();

            } catch (JMSException ex) {
                processManager.jobFinished(nodeID, toolID, false, null, null);
                Logger.getLogger(ToolLauncherBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (JobAlreadyRunningException ex) {
            processManager.jobFinished(nodeID, toolID, false, null, null);
            Logger.getLogger(ToolLauncherBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            processManager.jobFinished(nodeID, toolID, false, null, null);
            Logger.getLogger(ToolLauncherBean.class.getName()).log(Level.SEVERE, null, "NullPointException in ToolLauncherBean, so there's probably something wrong with tools.xml " + ex);
        }
    }
}
