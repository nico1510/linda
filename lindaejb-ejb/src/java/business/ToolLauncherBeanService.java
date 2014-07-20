package business;

import java.util.LinkedHashMap;
import javax.ejb.Local;

/**
 *
 * @author nico
 */

@Local
public interface ToolLauncherBeanService {
    
    public void launchTool(String toolID, String jobID, LinkedHashMap<String, String> folder);
    
}