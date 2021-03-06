/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package presentation;

import Exceptions.JobAlreadyKilledException;
import business.ContentService;
import business.JobControlService;
import business.RepositoryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import model.JobProxy;
import model.ProxyItem;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author nico
 */
@ManagedBean
@ViewScoped
public class AdminBean {

    @EJB(name="jobBean")
    JobControlService jobBean;
    @EJB(name="repBean")
    RepositoryService repBean;
    @EJB(name="contentBean")
    ContentService contentBean;
    
    private int jobcount;
    private String toolConfigText;
    private TreeNode root;
    private TreeNode[] selectedItems;
    private ArrayList<JobProxy> runningJobs;
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
    public ArrayList<JobProxy> getRunningJobs() {
        return jobBean.getJobs();
    }

    /**
     * @param runningJobs the runningJobs to set
     */
    public void setRunningJobs(ArrayList<JobProxy> runningJobs) {
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
    
    
    public TreeNode convertToTreeNode(ArrayList<LinkedHashMap<String, String>> repositoryContent){
        TreeNode repoTree = new DefaultTreeNode("root", null);
        for(HashMap<String, String> node: repositoryContent){
            TreeNode newNode = new DefaultTreeNode(new ProxyItem(node.get("text_name"), node.get("text_nodeid"), "Node"), repoTree);
            for(Map.Entry<String, String> prop: node.entrySet()){
                String type = prop.getKey();
                String value = prop.getValue();
                if(prop.getKey().startsWith("text") && !prop.getKey().equals("text_name")){ 
                    TreeNode propNode = new DefaultTreeNode(new ProxyItem(type, node.get("text_nodeid")+"/"+type, value), newNode);
                } else {
                    TreeNode propNode = new DefaultTreeNode(new ProxyItem(type, value, "File"), newNode);
                }
            }
        }
        
        return repoTree;
    }
    
    
    @PostConstruct
    public void init() {
        this.toolConfigText = jobBean.getToolConfig();
        this.root = convertToTreeNode(contentBean.getRepositoryContent());
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
        jobBean.changeToolConfig(toolConfigText);
        this.editMode = false;
    }
    

}
