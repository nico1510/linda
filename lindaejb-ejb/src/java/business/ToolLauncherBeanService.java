package business;

import java.util.LinkedHashMap;
import javax.ejb.Remote;

/**
 *
 * @author nico
 */

@Remote
public interface ToolLauncherBeanService {
    
    public void launchTool(String toolID, String jobID, LinkedHashMap<String, String> folder);
    
}