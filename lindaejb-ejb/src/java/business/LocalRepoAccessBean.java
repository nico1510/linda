/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jcr.Binary;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.ValueFactory;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author nico
 */

@Stateless
public class LocalRepoAccessBean implements Serializable, LocalRepoAccessService{
    
    @Resource(name = "jcr/rep1")
    private Repository repository;
    
    @EJB
    ContentService contentBean;
    


    @Override
    public Session createSession(boolean writeable) {
        Session session = null;

        try {
            if (writeable) {
                session = repository.login(new SimpleCredentials(UUID.randomUUID().toString(), "".toCharArray()), null);
            } else {
                session = repository.login();
            }
        } catch (LoginException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchWorkspaceException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return session;
    }

    
    
    @Override
    public String persistMeta(InputStream in, String nodeID, String fileName) {
        Session session = createSession(true);
        try {
            ValueFactory vf = session.getValueFactory();
            Binary bfile = vf.createBinary(in);
            Node datasetNode;
            
            if (!session.getRootNode().hasNode(nodeID)) {              // this is only the case for entity files if the liteq_entities node doesnt exist yet
                datasetNode = session.getRootNode().addNode(nodeID);
            } else {
                datasetNode = session.getNode(nodeID);
            }
            
            Property metaProp = datasetNode.setProperty(fileName, bfile);

            session.save();
            String metaPath = metaProp.getPath();

            return metaPath;

        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
            contentBean.updateContent();
        }
        return null;
    }
    
    @Override
    public String persistQueryResponse(String response, String queryHash) {
        Session session = createSession(true);
        try {

            Node datasetNode;
            
            if (!session.getRootNode().hasNode("liteq_cache")) {              // this is only the case if the liteq node doesnt exist or cache was reseted
                datasetNode = session.getRootNode().addNode("liteq_cache");
            } else {
                datasetNode = session.getNode("liteq_cache");
            }
            
            Property metaProp = datasetNode.setProperty(queryHash, response);

            session.save();
            String metaPath = metaProp.getPath();

            return metaPath;

        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
            contentBean.updateContent();
        }
        return null;
    }  
}
