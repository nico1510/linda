/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.ejb.Remote;

/**
 *
 * @author nico
 */
@Remote
public interface RepositoryService {
    
    public void changeFolder(LinkedHashMap<String,String> folder);
    
    public void deleteItems(String[] itemID);
    
    public ArrayList<LinkedHashMap<String, String>> getRepoContent();
    
    public String getPhysicalBinaryPath(String propPath);
        
    public void cleanup();

    public void saveInTripleStore(String propPath, String nodeID);
    
    public String queryTripleStore(String nodeID);
    
    public String answerLiteqQuery(String query);
    
    public void resetCache();
        
}