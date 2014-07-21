/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import java.util.ArrayList;
import javax.ejb.Remote;

/**
 *
 * @author nico
 */

@Remote
public interface ToolResultHandlerService {
    
    public void handleResult(String nodeID, String toolID, boolean success, ArrayList<String> filePaths, String absolutePath);
    
}
