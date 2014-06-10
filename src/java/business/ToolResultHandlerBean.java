/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import Events.JobFinishedEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import model.Tool;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;
import util.VoidfileParser;

/**
 *
 * @author nico
 */
@Stateless
public class ToolResultHandlerBean implements ToolResultHandlerService, Serializable {

    @EJB
    RepositoryService repBean;
    @EJB
    JobControlBean jobBean;

    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void handleResult(@Observes JobFinishedEvent event) {
        try {
            if (event.getSuccess()) {
                String toolID = event.getToolID();
                ArrayList<String> outputFiles = null;
                ArrayList<String> filesToSave = event.getFilesToSave();
                String nodeID = event.getNodeID();

                for (int i = 0; i < filesToSave.size(); i++) {
                    String filePath = filesToSave.get(i);
                    String fileName = filesToSave.get(i).split("/")[filesToSave.get(i).split("/").length - 1];
                    InputStream fin;
                    try {
                        fin = new FileInputStream(new File(filePath));
                        repBean.persistMeta(fin, nodeID, fileName);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ToolResultHandlerBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                useResult(toolID, outputFiles, nodeID);
            }
        } finally {
            if (!event.getFolderToDelete().isEmpty()) {
                File tempDir = new File(event.getFolderToDelete());
                deleteDir(tempDir);
            }
        }
    }

    public void handleVoidResult(String nodeID, ArrayList<String> outputFiles) {
        Session session = null;
        String voidfile = outputFiles.get(0);  // this is the voidfile.ttl see tools.xml
        try {
            LinkedHashMap<String, String> folder = new LinkedHashMap<String, String>();
            folder.put("text_nodeid", nodeID);

            session = repBean.createSession(false);
            Property metaProp = session.getProperty(nodeID + File.separator + voidfile);

            InputStream toolFileIn = metaProp.getBinary().getStream();

            RDFParser rdfParser = new TurtleParser();
            VoidfileParser voidfileParser = new VoidfileParser();
            voidfileParser.setFolder(folder);
            rdfParser.setRDFHandler(voidfileParser);
            try {
                rdfParser.parse(toolFileIn, "http://www.example.org/");
            } catch (RDFParseException ex) {
                Logger.getLogger(ToolResultHandlerBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RDFHandlerException ex) {
                Logger.getLogger(ToolResultHandlerBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ToolResultHandlerBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            repBean.changeFolder(voidfileParser.getFolder());

        } catch (PathNotFoundException ex) {
            Logger.getLogger(ToolResultHandlerBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(ToolResultHandlerBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
        }

    }

    public void handleSchemaResult() {
    }

    private void handleFcaResult() {
    }

    private void handleMutualResult() {
    }

    private void useResult(String toolID, ArrayList<String> outputFiles, String nodeID) {
        for (int i = 0; i < jobBean.getTools().size(); i++) {
            Tool tool = jobBean.getTools().get(i);
            if (tool.getToolID().equals(toolID)) {
                outputFiles = tool.getOutputfiles();
            }
        }
        switch (toolID) {
            case "void_desc":
                handleVoidResult(nodeID, outputFiles);
                break;
            case "schemex":
                handleSchemaResult();
                break;
            case "fca":
                handleFcaResult();
                break;
            case "mutual":
                handleMutualResult();
                break;
        }
    }

    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }
}
