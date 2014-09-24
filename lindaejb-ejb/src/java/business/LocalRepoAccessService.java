/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

import java.io.InputStream;
import javax.ejb.Local;
import javax.jcr.Session;

/**
 *
 * @author nico
 */

@Local
public interface LocalRepoAccessService {

    String persistMeta(InputStream in, String nodeID, String fileName);
    Session createSession(boolean writeable);
    String persistQueryResponse(String response, String queryHash);
}
