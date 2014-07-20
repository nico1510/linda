/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import util.HashMapRepositoryVisitor;

/**
 *
 * @author nico
 */
@Singleton
public class ContentBean implements Serializable, ContentService {

    @EJB
    RepositoryService repBean;

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
    @Override
    public void setRepositoryContent(ArrayList<LinkedHashMap<String, String>> repositoryContent) {
        this.repositoryContent = repositoryContent;
    }

    @PostConstruct
    public void init() {
        HashMapRepositoryVisitor repVisitor = new HashMapRepositoryVisitor();
        repBean.acceptVisitor(repVisitor, null);
        this.repositoryContent = repVisitor.getList();
    }

   @Lock(LockType.WRITE)
    @Override
    public void updateContent() {
        Logger.getLogger(ContentBean.class.getName()).log(Level.INFO, "updating repo content");
        HashMapRepositoryVisitor repVisitor = new HashMapRepositoryVisitor();
        repBean.acceptVisitor(repVisitor,null);
        this.repositoryContent = repVisitor.getList();
    }

   
}
