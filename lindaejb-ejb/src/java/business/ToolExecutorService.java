/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import javax.ejb.Local;

/**
 *
 * @author nico
 */

@Local
public interface ToolExecutorService {
    
    public void launchTool(String nodeID, String launchInfo, String format);
    
}
