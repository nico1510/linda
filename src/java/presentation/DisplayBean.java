/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package presentation;

import business.JobControlBean;
import business.RepositoryService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author nico
 */
@ManagedBean
@ViewScoped
public class DisplayBean {

    private String nodeID;
    private String toolID;
    private String uri;
    private String answer;
    @EJB
    RepositoryService repBean;
    @EJB
    JobControlBean jobBean;

    /**
     * @return the nodeID
     */
    public String getNodeID() {
        return nodeID;
    }

    /**
     * @param nodeID the nodeID to set
     */
    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    /**
     * @return the toolID
     */
    public String getToolID() {
        return toolID;
    }

    /**
     * @param toolID the toolID to set
     */
    public void setToolID(String toolID) {
        this.toolID = toolID;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * @param answer the answer to set
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void resolveURI() {
        try {
            String subject = "http://linda.west.uni-koblenz.de/datasets/" + nodeID + "/" + toolID + "/" + uri;
            Logger.getLogger(DisplayBean.class.getName()).log(Level.INFO, subject);
            Element tool = jobBean.getToolConfig().getElementsByAttributeValue("id", toolID).first();
            Elements outputfiles = tool.getElementsByTag("save");
            Repository myRepository = new SailRepository(new MemoryStore());
            myRepository.initialize();
            RepositoryConnection con = myRepository.getConnection();

            for (int i = 0; i < outputfiles.size(); i++) {
                String propPath = "/" + nodeID + "/" + outputfiles.get(i).text();
                String physPath = repBean.getPhysicalBinaryPath(propPath);
                RDFFormat rdfFormat = RDFFormat.forFileName(outputfiles.get(i).text());
                con.add(new File(physPath), "http://example.org/local", rdfFormat);

            }

            String subjQueryString = "SELECT * FROM {subject} p {object}";
            TupleQuery subjQuery = con.prepareTupleQuery(QueryLanguage.SERQL, subjQueryString);
            subjQuery.setBinding("subject", new URIImpl(subject));
            TupleQueryResult subjQueryResult = subjQuery.evaluate();

            ByteArrayOutputStream writer = new ByteArrayOutputStream();
            RDFWriter ntWriter = Rio.createWriter(RDFFormat.NTRIPLES, writer);
            try {
                ntWriter.startRDF();
                
                while (subjQueryResult.hasNext()) {
                    BindingSet bindingSet = subjQueryResult.next();
                    Value sub = bindingSet.getValue("subject");
                    Value pred = bindingSet.getValue("p");
                    Value obj = bindingSet.getValue("object");
                    StatementImpl statement = new StatementImpl(new URIImpl(sub.stringValue()), new URIImpl(pred.stringValue()), obj);
                    ntWriter.handleStatement(statement);
                }

                ntWriter.endRDF();
            } catch (RDFHandlerException ex) {
                Logger.getLogger(DisplayBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            answer = writer.toString();


        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DisplayBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DisplayBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RDFParseException ex) {
            Logger.getLogger(DisplayBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DisplayBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(DisplayBean.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
