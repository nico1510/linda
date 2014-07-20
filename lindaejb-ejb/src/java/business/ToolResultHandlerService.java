/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import Events.JobFinishedEvent;
import javax.ejb.Local;

/**
 *
 * @author nico
 */

@Local
public interface ToolResultHandlerService {
    
    public void handleResult(JobFinishedEvent event);
    
}
