/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.jcr.Repository;
import org.apache.jackrabbit.rmi.remote.RemoteRepository;
import org.apache.jackrabbit.rmi.server.ServerAdapterFactory;

/**
 *
 * @author nico
 */
@Singleton
public class ContentBean implements Serializable, ContentService {

    @EJB
    RepositoryService repBean;
    
    @Resource(name = "jcr/rep1")
    private Repository repository;

    private ArrayList<LinkedHashMap<String, String>> repositoryContent;

    /**
     * Creates a new instance of ViewBean
     */
    public ContentBean() {
    }

    /**
     * @return the repositoryContent
     */
    @Override
    public ArrayList<LinkedHashMap<String, String>> getRepositoryContent() {
        return repositoryContent;
    }

    /**
     * @param repositoryContent the repositoryContent to set
     */
    public void setRepositoryContent(ArrayList<LinkedHashMap<String, String>> repositoryContent) {
        this.repositoryContent = repositoryContent;
    }

    @PostConstruct
    public void init() {
        exposeRepoThroughRMI();
        this.repositoryContent = repBean.getRepoContent();
    }

    @Lock(LockType.WRITE)
    @Override
    public void updateContent() {
        Logger.getLogger(ContentBean.class.getName()).log(Level.INFO, "updating repo content");
        this.repositoryContent = repBean.getRepoContent();
    }

    public void exposeRepoThroughRMI() {
        try {
            // Start the RMI registry
            Registry reg = LocateRegistry.createRegistry(1100);
            
            // Bind the repository reference to the registry
            ServerAdapterFactory factory = new ServerAdapterFactory();
            RemoteRepository remote = factory.getRemoteRepository(repository);
            reg.rebind("jackrabbit", remote);
        } catch (RemoteException ex) {
            Logger.getLogger(ContentBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}