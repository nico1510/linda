/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.LinkedHashMap;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 *
 * @author nico
 */
public class VoidfileParser extends RDFHandlerBase {

    boolean first = true;
    private LinkedHashMap<String, String> folder;

    /**
     * @return the folder
     */
    public LinkedHashMap<String, String> getFolder() {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(LinkedHashMap<String, String> folder) {
        this.folder = folder;
    }
    
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        String predicate = st.getPredicate().toString();
        switch (predicate) {
            case "http://rdfs.org/ns/void#triples":
                if (first) {
                    folder.put("text_triples", st.getObject().stringValue());
                }
                first=false;
                break;
            case "http://rdfs.org/ns/void#classes":
                folder.put("text_classes", st.getObject().stringValue());
                break;
            case "http://rdfs.org/ns/void#properties":
                folder.put("text_properties", st.getObject().stringValue());
                break;
        }
    }


}

