/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package liteq;

import business.RepositoryService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

        String param2 = request.getParameter("param2");
        String param1 = request.getParameter("param1");
        String query = "";
        String result;
        boolean resetQuery = false;

        switch (param2) {
            case "types":
                if (param1.equals("*")) {
                    query = "sparql select distinct ?cls WHERE {[] rdf:type ?cls .}";
                } else {
                    // get types for type cluster param1
                }
                break;
            case "eqc":
                // get all equivalence clusters for type cluster param1
                break;

            case "entities":
                // get all entitities for equivalence class param1
                break;

            case "properties":
                // get all properties for equivalence class param1
                break;

            case "mappings":
                // get all mappings for equivalence class param1
                break;
            case "reset":
                // reset cache
                if(param1.equals("cache")) {
                    repBean.resetCache();
                    resetQuery = true;
                }
                break;
        }

        result = resetQuery? "{\"response\":\"cache reset\"}" : repBean.answerLiteqQuery(query);
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
