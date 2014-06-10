/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import java.io.InputStream;
import java.util.LinkedHashMap;
import javax.ejb.Local;
import javax.jcr.ItemVisitor;
import javax.jcr.Session;
import model.ProxyFile;

/**
 *
 * @author nico
 */
@Local
public interface RepositoryService {
    
    public void changeFolder(LinkedHashMap<String,String> folder);
    
    public void deleteItems(String[] itemID);
    
    public void acceptVisitor(ItemVisitor visitor,Session session);
    
    public Session createSession(boolean writeable);

    public String getPhysicalBinaryPath(String propPath);

    public String persistDataset(InputStream in, LinkedHashMap<String,String> folder, String mimeType);

    public String persistMeta(InputStream in, String propPath, String meta);
    
    public ProxyFile downloadFile(String propPath);
    
    public void cleanup();
    
}