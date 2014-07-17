/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javax.inject.Named;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 *
 * @author nico
 */

@Named
public class FormatWriteHandler extends RDFHandlerBase{
    
    private RDFWriter writer;

    /**
     * @return the writer
     */
    public RDFWriter getWriter() {
        return writer;
    }

    /**
     * @param writer the writer to set
     */
    public void setWriter(RDFWriter writer) {
        this.writer = writer;
    }
    
    
    @Override
    public void startRDF() throws RDFHandlerException {
        System.out.println("Converting");
        super.startRDF();
        writer.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        System.out.println("Finished Converting");
        super.endRDF();
        writer.endRDF();
    }

    @Override
    public void handleNamespace(String string, String string1) throws RDFHandlerException {
        super.handleNamespace(string, string1);
        writer.handleNamespace(string, string1);
    }

    @Override
    public void handleStatement(Statement stmnt) throws RDFHandlerException {
        super.handleStatement(stmnt);
        writer.handleStatement(stmnt);
    }

    @Override
    public void handleComment(String string) throws RDFHandlerException {
        super.handleComment(string);
        writer.handleComment(string);
    }

    
}
