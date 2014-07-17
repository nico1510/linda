package business;

import javax.ejb.Local;
import java.util.LinkedHashMap;

/**
 *
 * @author nico
 */

@Local
public interface ToolLauncherBeanService {
    
    public void launchTool(String toolID, String jobID, LinkedHashMap<String, String> folder);
    
}