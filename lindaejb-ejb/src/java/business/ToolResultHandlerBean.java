/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

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
    JobControlService jobBean;
    @EJB
    LocalRepoAccessService localRepoBean;


    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void handleResult(String nodeID, String toolID, boolean success, ArrayList<String> filePaths, String absolutePath) {
        try {
            if (success) {

                for (int i = 0; i < filePaths.size(); i++) {
                    String filePath = filePaths.get(i);
                    String fileName = filePaths.get(i).split("/")[filePaths.get(i).split("/").length - 1];
                    InputStream fin;
                    try {
                        fin = new FileInputStream(new File(filePath));
                        localRepoBean.persistMeta(fin, nodeID, fileName);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ToolResultHandlerBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                useResult(toolID, nodeID);
            }
        } finally {
            if (!absolutePath.isEmpty()) {
                File tempDir = new File(absolutePath);
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

            session = localRepoBean.createSession(false);
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

    public void handleSchemaResult(String nodeID, ArrayList<String> outputFiles) {
        String schemaFile = outputFiles.get(0);  // this is the schema.nt see tools.xml
        repBean.saveInTripleStore(nodeID + File.separator + schemaFile, nodeID);
    }

    private void handleFcaResult(String nodeID, ArrayList<String> outputFiles) {
    }

    private void handleMutualResult(String nodeID, ArrayList<String> outputFiles) {
    }

    private void useResult(String toolID, String nodeID) {
        ArrayList<String> outputFiles = null;
        
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
                handleSchemaResult(nodeID, outputFiles);
                break;
            case "fca":
                handleFcaResult(nodeID, outputFiles);
                break;
            case "mutual":
                handleMutualResult(nodeID, outputFiles);
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
