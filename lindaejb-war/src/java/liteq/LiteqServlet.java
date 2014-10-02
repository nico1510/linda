/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package liteq;

import business.RepositoryService;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

        String param1 = request.getParameter("param1");
        String param2 = URLDecoder.decode(request.getParameter("param2"), "UTF-8" );
        String query = "";
        String result = "{\"response\":\"fail\"}";

        switch (param1) {
            case "types":
                if (param2.equals("*")) {
                    query = "sparql select distinct ?type WHERE "
                            + "{ ?tc a <http://schemex.west.uni-koblenz.de/TypeCluster> ."
                            + "?tc <http://schemex.west.uni-koblenz.de/hasClass> ?type .}";
                    result = repBean.answerLiteqQuery(query, true);
                } else {
                    // get types for type cluster param1
                    query = "sparql select ?type WHERE "
                            + "{ <" + param2 + "> a <http://schemex.west.uni-koblenz.de/TypeCluster> ."
                            + " <" + param2 + "> <http://schemex.west.uni-koblenz.de/hasClass> ?type .}";
                    result = repBean.answerLiteqQuery(query, true);
                }
                break;
            case "tc":
                    query = "sparql select distinct ?tc WHERE "
                            + "{ ?tc a <http://schemex.west.uni-koblenz.de/TypeCluster> ."
                            + "?tc <http://schemex.west.uni-koblenz.de/hasClass> <" + param2 + "> .}";
                    result = repBean.answerLiteqQuery(query, true);
                    break;
            case "eqc":
                // get all equivalence classes for type cluster param1
                query = "sparql select ?eqc WHERE "
                        + "{ <" + param2 + "> a <http://schemex.west.uni-koblenz.de/TypeCluster> ."
                        + " ?eqc a <http://schemex.west.uni-koblenz.de/EquivalenceClass> ."
                        + " <" + param2 + "> <http://schemex.west.uni-koblenz.de/hasSubset> ?eqc .}";
                result = repBean.answerLiteqQuery(query, true);
                break;

            case "entities":
                result = repBean.getLiteqEntityQueryResult(param1);
                break;

            case "properties":
                // get all properties for equivalence class param1
                query = "sparql select ?prop WHERE { "
                        + "?tc a <http://schemex.west.uni-koblenz.de/TypeCluster> . "
                        + "<" + param2 + "> a <http://schemex.west.uni-koblenz.de/EquivalenceClass> . "
                        + "<" + param2 + "> ?prop ?tc . }";
                result = repBean.answerLiteqQuery(query, true);
                break;

            case "mappings":
                // get all mappings for equivalence class param1
                query = "sparql select ?tc, ?prop WHERE { "
                        + "?tc a <http://schemex.west.uni-koblenz.de/TypeCluster> . "
                        + "<" + param2 + "> a <http://schemex.west.uni-koblenz.de/EquivalenceClass> . "
                        + "<" + param2 + "> ?prop ?tc . }";
                result = repBean.answerLiteqQuery(query, true);
                break;
            case "reset":
                // reset cache
                if (param2.equals("cache")) {
                    repBean.resetCache();
                } 
//                else if (param2.equals("entities")) {
//                    repBean.resetEntities();
//                }
                result = "{\"response\":\"cache reset\"}";
                break;
        }
        
        Logger.getLogger(LiteqServlet.class.getName()).log(Level.INFO, query);

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

}
