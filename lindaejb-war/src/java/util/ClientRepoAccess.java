/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import business.ContentService;
import business.RepositoryBean;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.ValueFactory;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.rmi.server.ServerXASession;

/**
 *
 * @author nico
 */
@Named
public class ClientRepoAccess {
    
    @EJB(name="contentBean")
    ContentService contentBean;

    
    public String persistDataset(InputStream in, LinkedHashMap<String, String> folder, String mimeType) {
        
        if (mimeType.equals("application/x-gzip") | mimeType.equals("application/zip")) {
            in = decompressStream(mimeType, in, folder);
        }

        String fileName = folder.get("text_filename");
        String rdfFormat = FilenameUtils.getExtension(fileName);
        folder.put("text_rdfformat", rdfFormat);
        Session session = null;

        try {
            Repository repository = JcrUtils.getRepository("rmi://webschemex.west.uni-koblenz.de:1100/jackrabbit");
            session = repository.login(new SimpleCredentials(UUID.randomUUID().toString(), "".toCharArray()), null);
            ValueFactory vf = session.getValueFactory();
            Binary bfile = vf.createBinary(in);

            Node root = session.getRootNode();
            Node filenode = root.addNode(UUID.randomUUID().toString().replaceAll("-", ""));
            Property p = filenode.setProperty("dataset", bfile);

            Iterator it = folder.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String key = (String) pairs.getKey();
                String value = (String) pairs.getValue();
                filenode.setProperty(key, value);
                it.remove();
            }
            String nodeID = filenode.getPath();
            session.save();
            return nodeID;

        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
            contentBean.updateContent();
        }

        return null;
    }
    

    private InputStream decompressStream(String mimeType, InputStream in, LinkedHashMap<String, String> folder) {
        ArchiveInputStream din;
        switch (mimeType) {
            case "application/x-gzip":
                try {
                    InputStream gzIn = new BufferedInputStream(new GzipCompressorInputStream(in));
                    try {
                        din = new ArchiveStreamFactory().createArchiveInputStream(gzIn);
                        String name = din.getNextEntry().getName();
                        folder.put("text_filename", name);
                        in = din;
                    } catch (ArchiveException ex) {
                        String name = folder.get("text_filename");
                        folder.put("text_filename", FilenameUtils.getBaseName(name));
                        in = gzIn;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "application/zip":
                din = new ZipArchiveInputStream(in);
                try {
                    String name = din.getNextEntry().getName();
                    folder.put("text_filename", name);
                    in = din;
                } catch (IOException ex) {
                    Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
        return in;
    }
}
