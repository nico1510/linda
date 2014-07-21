/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import java.util.LinkedHashMap;
import javax.ejb.Local;
import javax.jcr.ItemVisitor;
import javax.jcr.Session;

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
        
    public void cleanup();

    public void saveInTripleStore(String propPath, String nodeID);
    
    public String queryTripleStore(String nodeID);
    
}