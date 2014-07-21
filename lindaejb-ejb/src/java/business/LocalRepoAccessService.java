/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import java.io.InputStream;
import javax.ejb.Local;

/**
 *
 * @author nico
 */

@Local
public interface LocalRepoAccessService {

    String persistMeta(InputStream in, String nodeID, String fileName);
    
}
