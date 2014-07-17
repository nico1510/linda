/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package presentation;

import Exceptions.JobAlreadyKilledException;
import Exceptions.ToolDisabledException;
import application.UserBean;
import business.JobControlBean;
import business.RepositoryService;
import business.ToolLauncherBeanService;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import model.Tool;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;

/**
 *
 * @author nico
 */
@ManagedBean
@ViewScoped
public class DetailsBean implements Serializable {

    private ArrayList<Tool> tools;
    private TagCloudModel tagmodel;
    private boolean edit = false;
    private boolean notifierVisible = false;
    private String email;
    private String lastJob;
    @EJB
    RepositoryService repBean;
    @EJB
    JobControlBean jobBean;
    @EJB
    ToolLauncherBeanService launcherBean;
    @Inject
    private UserBean userBean;
    @ManagedProperty(value = "#{folderBean}")
    private FolderBean folderBean;

    /**
     * @return the folderBean
     */
    public FolderBean getFolderBean() {
        return folderBean;
    }

    /**
     * @param folderBean the folderBean to set
     */
    public void setFolderBean(FolderBean folderBean) {
        this.folderBean = folderBean;
    }

    /**
     * @return the tools
     */
    public ArrayList<Tool> getTools() {
        return tools;
    }

    /**
     * @param tools the tools to set
     */
    public void setTools(ArrayList<Tool> tools) {
        this.tools = tools;
    }

    /**
     * @return the model
     */
    public TagCloudModel getTagModel() {
        return tagmodel;
    }

    /**
     * @param model the model to set
     */
    public void setTagModel(TagCloudModel tagmodel) {
        this.tagmodel = tagmodel;
    }

    /**
     * @return the edit
     */
    public boolean getEdit() {
        return edit;
    }

    /**
     * @param edit the edit to set
     */
    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    /**
     * @return the notifierVisible
     */
    public boolean getNotifierVisible() {
        return notifierVisible;
    }

    /**
     * @param notifierVisible the notifierVisible to set
     */
    public void setNotifierVisible(boolean notifierVisible) {
        this.notifierVisible = notifierVisible;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the lastJob
     */
    public String getLastJob() {
        return lastJob;
    }

    /**
     * @param lastJob the lastJob to set
     */
    public void setLastJob(String lastJob) {
        this.lastJob = lastJob;
    }

    @PostConstruct
    public void init() {
        tools = jobBean.getTools();
        tagmodel = new DefaultTagCloudModel();
    }

    public String commitChanges() {
        String redirectID = folderBean.getFolder().get("text_nodeid").replaceFirst("/", "");
        if (folderBean.getFolder().get("text_nodeid") == null) {
            Logger.getLogger(DetailsBean.class.getName()).log(Level.INFO, "Dataset has been deleted or does not exist");
        } else {
            repBean.changeFolder(folderBean.getFolder());
        }
        setEdit(false);
        return "details.xhtml?faces-redirect=true&id=" + redirectID;
    }

    public void preRender() {
        if (folderBean.getFolder() == null) {
            folderBean.setFolder(new LinkedHashMap<String, String>());
        }
        if (tagmodel.getTags().isEmpty()) {
            if (folderBean.getTags() == null || folderBean.getTags().isEmpty()) {
                tagmodel.addTag(new DefaultTagCloudItem("no tags yet", 1));
            } else {
                String[] tagArray = folderBean.getTags().split(" ");
                for (int i = 0; i < tagArray.length; i++) {
                    tagmodel.addTag(new DefaultTagCloudItem(tagArray[i], 3));
                }
            }
        }
    }

    public String deleteDataset() {
        String nodeID = folderBean.getFolder().get("text_nodeid");
        String[] helperArray = new String[1];
        helperArray[0] = nodeID;
        repBean.deleteItems(helperArray);
        userBean.getUploadedDatasets().remove(nodeID);
        return "datasets";
    }

    public void closeNotifier() {
        notifierVisible = false;
    }

    public void submitEmail() {
        if (lastJob != null && email != null) {   // TODO email Adresse evaluieren?
            jobBean.addEmail(lastJob, email);
        }
        closeNotifier();
    }

    public void subscribe(String toolID) {
        this.lastJob = folderBean.getNodeid() + File.separator + toolID;
    }

    public void generateFile(Tool tool) {
        try {
            if (toolDisabled(tool)) {
                throw new ToolDisabledException();
            }
            String jobID = folderBean.getNodeid() + File.separator + tool.getToolID();
            launcherBean.launchTool(tool.getToolID(), jobID, folderBean.getFolder());
            userBean.getStartedJobs().add(jobID);
            this.setLastJob(jobID);
            this.setNotifierVisible(true);
        } catch (ToolDisabledException ex) {
            Logger.getLogger(DetailsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void killProcess(String toolID) {
        try {
            jobBean.killJob(folderBean.getNodeid() + File.separator + toolID);
        } catch (JobAlreadyKilledException ex) {
            Logger.getLogger(DetailsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public boolean containsJob(String jobID) {
        return jobBean.containsJob(jobID);
    }

    public boolean allFilesAvailable(ArrayList<String> requiredFiles) {
        for (int i = 0; i < requiredFiles.size(); i++) {
            if (folderBean.getFolder().get(requiredFiles.get(i)) == null) {
                return false;
            }
        }
        return true;
    }

    public boolean oneFileAvailable(ArrayList<String> requiredFiles) {
        for (int i = 0; i < requiredFiles.size(); i++) {
            if (folderBean.getFolder().get(requiredFiles.get(i)) != null) {
                return true;
            }
        }
        return false;
    }

    public boolean toolDisabled(Tool tool) {
        if (!this.allFilesAvailable(tool.getInputfiles()) || this.allFilesAvailable(tool.getOutputfiles()) || containsJob(folderBean.getNodeid() + File.separator + tool.getToolID())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean notGeneratedTextVisible(Tool tool) {
        if (folderBean.getFolder().get(tool.getOutputfiles().get(0)) == null && !containsJob(folderBean.getNodeid() + File.separator + tool.getToolID())) {
            return true;
        } else {
            return false;
        }
    }

    public String generateDependencyText(Tool tool) {
        ArrayList<String> inputfiles = tool.getInputfiles();
        if (allFilesAvailable(inputfiles)) {
            return "";
        } else {
            String depTools = "(requires result of tools :";
            for (int i = 0; i < inputfiles.size(); i++) {
                String inputfile = inputfiles.get(i);
                if (folderBean.getFolder().get(inputfile) == null && !inputfile.equals("dataset")) {
                    for (int j = 0; j < tools.size(); j++) {
                        if (tools.get(j).getOutputfiles().contains(inputfile)) {
                            String depTool = tools.get(j).getDisplayname();
                            if (!depTools.contains(depTool)) {
                                depTools += " " + depTool;
                            }
                        }
                    }
                } else if (inputfile.equals("dataset") && folderBean.getFolder().get("dataset") == null) {
                    return "(dataset not available anymore)";
                }
            }
            return depTools + ")";
        }
    }

    public boolean subscribeButtonVisible(String toolID) {
        return containsJob(folderBean.getNodeid() + File.separator + toolID);
    }

    public boolean abortButtonVisible(String toolID) {
        return userBean.getStartedJobs().contains(folderBean.getNodeid() + File.separator + toolID);
    }


}
