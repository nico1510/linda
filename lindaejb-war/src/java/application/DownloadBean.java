/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import business.RepositoryBean;
import business.RepositoryService;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;
import model.ProxyFile;
import org.apache.jackrabbit.commons.JcrUtils;

/**
 *
 * @author nico
 */
@ManagedBean
@RequestScoped
public class DownloadBean {
    
    @EJB
    RepositoryService repBean;
    private String nodeID;
    private String fileID;
    
    /**
     * @return the nodeID
     */
    public String getNodeID() {
        return nodeID;
    }

    /**
     * @param nodeID the nodeID to set
     */
    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    /**
     * @return the fileID
     */
    public String getFileID() {
        return fileID;
    }

    /**
     * @param fileID the fileID to set
     */
    public void setFileID(String fileID) {
        this.fileID = fileID;
    }
    
    
    public void downloadFile() throws IOException {
        
        String propertyPath = "/"+nodeID+"/"+fileID;
        ProxyFile fileToDownload = downloadFile(propertyPath);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        response.reset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
        response.setContentType(fileToDownload.getContentType()); // Check http://www.w3schools.com/media/media_mimeref.asp for all types. Use if necessary ServletContext#getMimeType() for auto-detection based on filename.
        response.setHeader("Content-Length", String.valueOf(fileToDownload.getLength()));
        response.setHeader("Content-disposition", "attachment; filename=\"" + fileToDownload.getName() + "\""); // The Save As popup magic is done here. You can give it any filename you want, this only won't work in MSIE, it will use current request URL as filename instead.

        BufferedInputStream input = null;
        BufferedOutputStream output = null;


        try {
            input = new BufferedInputStream(fileToDownload.getContent());
            output = new BufferedOutputStream(response.getOutputStream());

            byte[] buffer = new byte[10240];
            for (int length; (length = input.read(buffer)) > 0;) {
                output.write(buffer, 0, length);
            }

            facesContext.responseComplete(); // Important! Else JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
        } finally {
            output.close();
            input.close();
        }
    }

        public ProxyFile downloadFile(String propPath) {
        ProxyFile proxyFile = new ProxyFile();
        Session session = null;
            
        try {
            Repository repository = JcrUtils.getRepository("rmi://webschemex.west.uni-koblenz.de:1100/jackrabbit");
            session = repository.login();
            Property downloadProp = session.getProperty(propPath);
            Binary b = downloadProp.getBinary();

            proxyFile.setLength(b.getSize());
            proxyFile.setContent(b.getStream());
            proxyFile.setContentType("text/plain");
            proxyFile.setName(downloadProp.getName());

            if (downloadProp.getName().equals("dataset")) {
                Node parentNode = downloadProp.getParent();
                if (parentNode.hasProperty("text_filename") && !parentNode.getProperty("text_filename").getString().isEmpty()) {
                    proxyFile.setName(parentNode.getProperty("text_filename").getString());
                }
            }

            b.dispose();

            return proxyFile;
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
        }

        return null;
    }

}
