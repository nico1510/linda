/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import java.io.InputStream;
import java.io.Serializable;
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

            Node datasetNode = session.getNode(nodeID);
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
}
