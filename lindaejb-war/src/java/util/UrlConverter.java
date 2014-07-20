/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import business.ContentService;
import java.util.LinkedHashMap;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;

/**
 *
 * @author nico
 */
@Named
@FacesConverter(forClass = LinkedHashMap.class)
public class UrlConverter implements Converter {

    @EJB
    ContentService contentBean;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        String nodeid = "/"+value;
        for (LinkedHashMap<String, String> tempfolder : contentBean.getRepositoryContent()) {
            if (tempfolder.get("text_nodeid").equals(nodeid)) {
                return tempfolder.clone();
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        LinkedHashMap<String,String> folder = (LinkedHashMap<String,String>)value;
        return folder.get("text_nodeid");
    }
}
