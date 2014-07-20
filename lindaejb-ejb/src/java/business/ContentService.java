/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.ejb.Local;

/**
 *
 * @author nico
 */

@Local
public interface ContentService {

    ArrayList<LinkedHashMap<String, String>> getRepositoryContent();
    void setRepositoryContent(ArrayList<LinkedHashMap<String, String>> repositoryContent);
    void updateContent();
    
}
