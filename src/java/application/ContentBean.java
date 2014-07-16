/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import Events.UpdateEvent;
import business.RepositoryService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import util.HashMapRepositoryVisitor;

/**
 *
 * @author nico
 */
@Singleton
public class ContentBean implements Serializable {

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
        HashMapRepositoryVisitor repVisitor = new HashMapRepositoryVisitor();
        repBean.acceptVisitor(repVisitor, null);
        this.repositoryContent = repVisitor.getList();
    }

   @Lock(LockType.WRITE)
    public void updateContent(@Observes UpdateEvent event) {
        HashMapRepositoryVisitor repVisitor = new HashMapRepositoryVisitor();
        repBean.acceptVisitor(repVisitor,null);
        this.repositoryContent = repVisitor.getList();
    }

   
}
