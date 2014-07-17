/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import business.RepositoryService;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author nico
 */
@WebServlet(name = "RequestHandler", urlPatterns = {"/request"})
@MultipartConfig
public class RequestHandler extends HttpServlet {

    @EJB
    RepositoryService repBean;
    
    @Inject
    UserBean userBean;


    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }
    
    
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!userBean.getUploadedDatasets().isEmpty()) {
            String datasetID = userBean.getUploadedDatasets().getLast();
            response.sendRedirect(response.encodeRedirectURL("http://linda.west.uni-koblenz.de/datasets"+datasetID));
        }
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String viewid = request.getParameter("viewid");
        LinkedHashMap<String,String> folder = (LinkedHashMap<String,String>) request.getSession().getAttribute(viewid);        
        Part filePart = request.getPart("item");
        String fileName = getFilename(filePart);
        folder.put("text_filename", fileName);
        InputStream filecontent = filePart.getInputStream();
        String datasetID = repBean.persistDataset(filecontent, folder, filePart.getContentType());
        userBean.getUploadedDatasets().add(datasetID);
        response.getWriter().println("SUCCESS");
        response.getWriter().close();

    }

    private static String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                String result = filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1);
                return result.replaceAll("\\s+", "_");
            }
        }
        return null;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet used for uploading files";
    }// </editor-fold>
}