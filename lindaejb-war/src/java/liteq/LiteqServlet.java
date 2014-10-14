package liteq;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import business.RepositoryService;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 *
 * @author nico
 */
public class LiteqServlet extends HttpServlet {

    @EJB(name = "repBean")
    RepositoryService repBean;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String get = request.getParameter("get");
        String uri = URLDecoder.decode(request.getParameter("uri"), "UTF-8");
        String query = "";
        String result = "{\"response\":\"fail\"}";

        switch (get) {
            case "types":
                if (uri.equals("*")) {
                    query = "sparql select distinct ?type WHERE "
                            + "{ ?tc a <http://schemex.west.uni-koblenz.de/TypeCluster> ."
                            + "?tc <http://schemex.west.uni-koblenz.de/hasClass> ?type .}";
                    result = repBean.answerLiteqQuery(query, true);
                } else {
                    // get types for type cluster param1
                    query = "sparql select ?type WHERE "
                            + "{ <" + uri + "> a <http://schemex.west.uni-koblenz.de/TypeCluster> ."
                            + " <" + uri + "> <http://schemex.west.uni-koblenz.de/hasClass> ?type .}";
                    result = repBean.answerLiteqQuery(query, true);
                }
                break;
            case "tc":
                query = "sparql select distinct ?tc WHERE "
                        + "{ ?tc a <http://schemex.west.uni-koblenz.de/TypeCluster> ."
                        + "?tc <http://schemex.west.uni-koblenz.de/hasClass> <" + uri + "> .}";
                result = repBean.answerLiteqQuery(query, true);
                break;
            case "eqc":
                // get all equivalence classes for type cluster param1
                query = "sparql select ?eqc WHERE "
                        + "{ <" + uri + "> a <http://schemex.west.uni-koblenz.de/TypeCluster> ."
                        + " ?eqc a <http://schemex.west.uni-koblenz.de/EquivalenceClass> ."
                        + " <" + uri + "> <http://schemex.west.uni-koblenz.de/hasSubset> ?eqc .}";
                result = repBean.answerLiteqQuery(query, true);
                break;
            case "eqcForProperty":
                // get equivalence classes for property uri
                query = "sparql select ?eqc WHERE { "
                        + "?tc a <http://schemex.west.uni-koblenz.de/TypeCluster> . "
                        + "?eqc a <http://schemex.west.uni-koblenz.de/EquivalenceClass> . "
                        + "?eqc <" + uri + "> ?tc . }";
                result = repBean.answerLiteqQuery(query, true);
                break;  
            case "entities":
                result = repBean.getLiteqEntityQueryResult(uri);
                break;

            case "properties":
                // get all properties for equivalence class param1
                query = "sparql select ?prop WHERE { "
                        + "?tc a <http://schemex.west.uni-koblenz.de/TypeCluster> . "
                        + "<" + uri + "> a <http://schemex.west.uni-koblenz.de/EquivalenceClass> . "
                        + "<" + uri + "> ?prop ?tc . }";
                result = repBean.answerLiteqQuery(query, true);
                break;

            case "mappings":
                // get all mappings for equivalence class param1
                query = "sparql select ?tc, ?prop WHERE { "
                        + "?tc a <http://schemex.west.uni-koblenz.de/TypeCluster> . "
                        + "<" + uri + "> a <http://schemex.west.uni-koblenz.de/EquivalenceClass> . "
                        + "<" + uri + "> ?prop ?tc . }";
                result = convertToArray(repBean.answerLiteqQuery(query, true));
                break;
            case "reset":
                // reset cache
                if (uri.equals("cache")) {
                    repBean.resetCache();
                }
//                else if (param2.equals("entities")) {
//                    repBean.resetEntities();
//                }
                result = "{\"response\":\"cache reset\"}";
                break;
        }

        try (PrintWriter out = response.getWriter()) {
            out.println(result);
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String convertToArray(String result) {
        try {
            JSONObject jsonResult = new JSONObject(result);
            JSONObject response = jsonResult.getJSONObject("response");
            JSONArray arrayResponse = new JSONArray();
            Iterator it = response.keys();
            while (it.hasNext()) {
                String typeCluster = (String) it.next();
                JSONArray arrayKey = new JSONArray();
                arrayKey.put(typeCluster);
                JSONArray props = response.getJSONArray(typeCluster);
                JSONArray containerArray = new JSONArray();
                containerArray.put(arrayKey);
                containerArray.put(props);
                arrayResponse.put(containerArray);
            }
            jsonResult.put("response", arrayResponse);
            return jsonResult.toString();
        } catch (JSONException ex) {
            Logger.getLogger(LiteqServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
