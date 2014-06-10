/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package presentation;

import Exceptions.JobAlreadyKilledException;
import business.JobControlBean;
import business.RepositoryService;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import model.Job;
import model.ProxyItem;
import org.primefaces.model.TreeNode;
import util.TreeNodeRepositoryVisitor;

/**
 *
 * @author nico
 */
@ManagedBean
@ViewScoped
public class AdminBean {

    @EJB
    JobControlBean jobBean;
    @EJB
    RepositoryService repBean;
    private int jobcount;
    private String toolConfigText;
    private TreeNode root;
    private TreeNode[] selectedItems;
    private ArrayList<Job> runningJobs;
    private boolean editMode;

    /**
     * Creates a new instance of AdminBean
     */
    /**
     * @return the toolConfigText
     */
    public String getToolConfigText() {
        return toolConfigText;
    }

    /**
     * @param toolConfigText the toolConfigText to set
     */
    public void setToolConfigText(String toolConfigText) {
        this.toolConfigText = toolConfigText;
    }

    /**
     * @return the runningJobs
     */
    public ArrayList<Job> getRunningJobs() {
        return jobBean.getJobs();
    }

    /**
     * @param runningJobs the runningJobs to set
     */
    public void setRunningJobs(ArrayList<Job> runningJobs) {
        this.runningJobs = runningJobs;
    }

    /**
     * @return the root
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(TreeNode root) {
        this.root = root;
    }

    /**
     * @return the selectedItems
     */
    public TreeNode[] getSelectedItems() {
        return selectedItems;
    }

    /**
     * @param selectedItems the selectedItems to set
     */
    public void setSelectedItems(TreeNode[] selectedItems) {
        this.selectedItems = selectedItems;
    }

    /**
     * @return the jobcount
     */
    public int getJobcount() {
        return jobBean.getJobCount();
    }

    /**
     * @param jobcount the jobcount to set
     */
    public void setJobcount(int jobcount) {
        this.jobcount = jobcount;
    }
    
    /**
     * @return the editMode
     */
    public boolean getEditMode() {
        return editMode;
    }

    /**
     * @param editMode the editMode to set
     */
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
    
    
    @PostConstruct
    public void init() {
        TreeNodeRepositoryVisitor treeNodeVisitor = new TreeNodeRepositoryVisitor();
        repBean.acceptVisitor(treeNodeVisitor, null);
        this.toolConfigText = jobBean.getToolConfig().html();
        this.root = treeNodeVisitor.getRoot();
        this.editMode=false;
    }

    public String deleteItems() {
        if (selectedItems.length != 0) {
            String[] itemPaths = new String[selectedItems.length];
            for (int i = 0; i < selectedItems.length; i++) {
                ProxyItem item = (ProxyItem) selectedItems[i].getData();
                itemPaths[i] = item.getPath();
            }
            repBean.deleteItems(itemPaths);
        }
        return "admin.xhtml";
    }

    public void killProcess(String id) {
        try {
            jobBean.killJob(id);
        } catch (JobAlreadyKilledException ex) {
            Logger.getLogger(AdminBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cleanup() {
           repBean.cleanup();
    }

    public void changeToolConfig() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(jobBean.getToolConfigPath()));
            out.write(toolConfigText);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(AdminBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        jobBean.parseTools();
        this.editMode = false;
    }
    

}
