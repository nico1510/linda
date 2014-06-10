/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.nquads.NQuadsParser;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 *
 * @author nico
 */
@Named
public class FormatConverter {

    public String convert(String inputFilePath, String outputFileDir, String format, String acceptedFormats) throws FileNotFoundException, IOException {
        RDFFormat desiredFormat = decideFormat(acceptedFormats);
        InputStream in = new FileInputStream(inputFilePath);

        File outputFile = new File(outputFileDir + "convertedtempfile");
        FileOutputStream out = new FileOutputStream(outputFile);
        
        FormatWriteHandler writeHandler = new FormatWriteHandler();
        writeHandler.setWriter(Rio.createWriter(desiredFormat, out));
        
        RDFParser parser = Rio.createParser(RDFFormat.forFileName("test."+format));
        parser.setRDFHandler(writeHandler);
        parser.setVerifyData(false);
        parser.setStopAtFirstError(false);
        parser.setPreserveBNodeIDs(true);
        try {
            parser.parse(in, "http:example.org");
        } catch (RDFParseException ex) {
        } catch (RDFHandlerException ex) {
            Logger.getLogger(FormatConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return outputFile.getAbsolutePath();
    }

    private RDFFormat decideFormat(String acceptedFormats) {
        if (acceptedFormats.contains("nt")) {
            return RDFFormat.NTRIPLES;
        } else if (acceptedFormats.contains("nq")) {
            return RDFFormat.NQUADS;
        } else if (acceptedFormats.contains("rdf")) {
            return RDFFormat.RDFXML;
        } else if (acceptedFormats.contains("n3") || acceptedFormats.contains("ttl")) {
            return RDFFormat.N3;
        } else {
            return null;
        }
    }
}
