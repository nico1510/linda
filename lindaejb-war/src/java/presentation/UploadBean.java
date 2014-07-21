/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package presentation;

import application.UserBean;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import util.ClientRepoAccess;

/**
 *
 * @author nico
 */
@ManagedBean
@ViewScoped
public class UploadBean implements Serializable {

    @Inject
    UserBean userBean;
    @Inject
    ClientRepoAccess repoClient;
    private String viewid;
    private int uploadOption;
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
     * @return the viewid
     */
    public String getViewid() {
        return viewid;
    }

    /**
     * @param viewid the viewid to set
     */
    public void setViewid(String viewid) {
        this.viewid = viewid;
    }

    /**
     * @return the uploadOption
     */
    public int getUploadOption() {
        return uploadOption;
    }

    /**
     * @param uploadOption the uploadOption to set
     */
    public void setUploadOption(int uploadOption) {
        this.uploadOption = uploadOption;
    }

    @PostConstruct
    public void init() {
        folderBean.setFolder(new LinkedHashMap<String, String>());
        viewid = UUID.randomUUID().toString();
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(viewid, folderBean.getFolder());
    }

    @PreDestroy
    public void destroy() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(viewid);
    }

    public void handleFileUpload(FileUploadEvent event) {

        UploadedFile uploadedFile = event.getFile();
        String datasetID = null;
        LinkedHashMap<String, String> folder = folderBean.getFolder();
        folder.put("text_filename", event.getFile().getFileName());
        try {
            datasetID = repoClient.persistDataset(uploadedFile.getInputstream(), folder, uploadedFile.getContentType());
            userBean.getUploadedDatasets().add(datasetID);
        } catch (IOException ex) {
            Logger.getLogger(UploadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        datasetID = datasetID.replace("/", "");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext extContext = facesContext.getExternalContext();
        String url = extContext.encodeActionURL(facesContext.getApplication().getViewHandler().getActionURL(facesContext, "/details.xhtml?id=" + datasetID));
        try {
            extContext.redirect(url);
        } catch (IOException ex) {
            Logger.getLogger(UploadBean.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}
