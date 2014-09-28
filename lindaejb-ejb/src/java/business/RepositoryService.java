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
    
    void changeFolder(LinkedHashMap<String,String> folder);
    void deleteItems(String[] itemID);
    ArrayList<LinkedHashMap<String, String>> getRepoContent();
    String getPhysicalBinaryPath(String propPath);    
    void cleanup();
    void saveInTripleStore(String propPath, String nodeID);
    void moveEntitiesToRepo(String nodeID);
    String queryTripleStore(String nodeID);
    String answerLiteqQuery(String query, boolean useCache);
    String getCachedLiteqQueryResult(String query);
    String getLiteqEntityQueryResult(String eqClassURI);
    void resetCache();
    void resetEntities();
        
}