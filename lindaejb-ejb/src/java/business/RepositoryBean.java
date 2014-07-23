package business;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.sql.DataSource;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import util.HashMapRepositoryVisitor;
import virtuoso.jdbc3.VirtuosoExtendedString;
import virtuoso.jdbc3.VirtuosoRdfBox;

/**
 *
 * @author nico
 */
@Stateless
public class RepositoryBean implements RepositoryService, Serializable {

    @Resource(name = "jcr/rep1")
    private Repository repository;

    @Resource(name = "triplestore")
    private DataSource triplestore;
    
    @EJB
    ContentService contentBean;
    @EJB
    LocalRepoAccessService localRepoBean;
            
            
    private void fireUpdate(){
        contentBean.updateContent();
    }

    @Override
    public void saveInTripleStore(String propPath, String nodeID) {
        Connection conn = null;
        try {
            conn = triplestore.getConnection();
            Statement stmt = conn.createStatement();

            String schemafilePath = getPhysicalBinaryPath(propPath);

            String insertStmtString = "DB.DBA.TTLP (file_to_string_output ('"
                    + schemafilePath + "'), '', '"
                    + nodeID.replaceAll("/", "") + "', 512)";
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.INFO, insertStmtString);

            stmt.execute(insertStmtString);

        } catch (SQLException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Asynchronous
    private void removeFromTripleStore(String schemaPath) {
        Connection conn = null;
        try {
            conn = triplestore.getConnection();
            Statement stmt = conn.createStatement();

            String nodeID = schemaPath.split("/")[1];

            String removeStmtString = "SPARQL DROP SILENT GRAPH <"+nodeID+">";
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.INFO, removeStmtString);

            stmt.execute(removeStmtString);

        } catch (SQLException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
    }
    
    
    @Override
    public String queryTripleStore(String nodeID){
        ResultSet rs;
        nodeID = nodeID.replaceAll("/", "");
        StringBuilder response = new StringBuilder();

        Connection conn = null;
        try {
            conn = triplestore.getConnection();
            Statement stmt = conn.createStatement();

            boolean more = stmt.execute("sparql select * from <" + nodeID + "> where { ?x ?y ?z }");
            ResultSetMetaData data = stmt.getResultSet().getMetaData();
            while (more) {
                rs = stmt.getResultSet();
                while (rs.next()) {
                    for (int i = 1; i <= data.getColumnCount(); i++) {
                        String s = rs.getString(i);
                        Object o = rs.getObject(i);
                        if (o instanceof VirtuosoExtendedString) {
                            VirtuosoExtendedString vs = (VirtuosoExtendedString) o;
                            if (vs.iriType == VirtuosoExtendedString.IRI && (vs.strType & 0x01) == 0x01) {
                                response.append("<" + vs.str + "> ");
                            } else if (vs.iriType == VirtuosoExtendedString.BNODE) {
                                response.append("<" + vs.str + "> ");
                            } else {
                                response.append("\"" + vs.str + "\" ");
                            }
                        } else if (o instanceof VirtuosoRdfBox) {
                            VirtuosoRdfBox rb = (VirtuosoRdfBox) o;
                            response.append(rb.rb_box + " lang=" + rb.getLang() + " type=" + rb.getType() + " ");

                        } else if (stmt.getResultSet().wasNull()) {
                            response.append("NULL ");
                        } else {
                            response.append(s+" ");
                        }

                    }
                    response.append(".\n");
                }
                more = stmt.getMoreResults();
            }

        } catch (SQLException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return response.toString();
    }

    @Override
    public String getPhysicalBinaryPath(String propPath) {
        Session session = localRepoBean.createSession(false);
        try {
            Property p = session.getProperty(propPath);
            Binary b = p.getBinary();
            Field idField = b.getClass().getDeclaredField("identifier");
            idField.setAccessible(true);
            String identifier = (String) idField.get(b).toString();
            Field storeField = b.getClass().getDeclaredField("store");
            storeField.setAccessible(true);
            Object store = storeField.get(b);
            Field pathField = store.getClass().getDeclaredField("path");
            pathField.setAccessible(true);
            String dataStorePath = (String) pathField.get(store);

            String binaryPath = identifier.substring(0, 2) + File.separator
                    + identifier.substring(2, 4) + File.separator
                    + identifier.substring(4, 6) + File.separator
                    + identifier;

            return dataStorePath + File.separator + binaryPath;

        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
        }

        return "";

        /**
         * Value value = vf.createValue(bfile); if (bfile instanceof
         * JackrabbitValue) {
         * Logger.getLogger(RepositoryBean.class.getName()).log(Level.INFO, "it
         * is"); JackrabbitValue jv = (JackrabbitValue) bfile; String id =
         * jv.getContentIdentity();
         * Logger.getLogger(RepositoryBean.class.getName()).log(Level.INFO, id);
         * }
         */
    }

    @Override
    public ArrayList<LinkedHashMap<String, String>>  getRepoContent() {
        HashMapRepositoryVisitor repVisitor = new HashMapRepositoryVisitor();
        Session session = localRepoBean.createSession(false);
        try {
            session.getRootNode().accept(repVisitor);
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
        }
        return repVisitor.getList();
    }

    @Override
    public void changeFolder(LinkedHashMap<String, String> folder) {
        Session session = localRepoBean.createSession(true);
        String nodeID = folder.get("text_nodeid");
        try {
            Node nodeToChange = session.getNode(nodeID);
            Iterator it = folder.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String key = (String) pairs.getKey();
                String value = (String) pairs.getValue();
                if (key.startsWith("text")) {
                    nodeToChange.setProperty(key, value);
                }
                it.remove();
            }
            session.save();

        } catch (ItemNotFoundException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
            fireUpdate();
        }

    }

    @Override
    public void deleteItems(String[] itemPaths) {
        Session session = localRepoBean.createSession(true);
        try {
            for (int i = 0; i < itemPaths.length; i++) {
                if(itemPaths[i].contains("schema.nt")){
                    removeFromTripleStore(itemPaths[i]);
                }
                Item itemToDelete = session.getItem(itemPaths[i]);
                itemToDelete.remove();
            }
            session.save();

        } catch (VersionException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LockException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConstraintViolationException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccessDeniedException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
            fireUpdate();
        }
    }

    private void walkAndRemove(List<String> activeFiles, String path) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            root.delete();
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                walkAndRemove(activeFiles, f.getAbsolutePath());
            } else {
                if (!activeFiles.contains(f.getAbsoluteFile().toString())) {
                    f.delete();
                }
            }
        }
    }

    @Override
    public void cleanup() {
        Logger.getLogger(RepositoryBean.class.getName()).log(Level.INFO, "Running Repo Garbage Collection");
        // get all active files
        HashMapRepositoryVisitor repVisitor = new HashMapRepositoryVisitor();
        ArrayList<LinkedHashMap<String, String>> repositoryContent = getRepoContent();

        ArrayList<String> fileNames = new ArrayList<String>();
        for (LinkedHashMap<String, String> node : repositoryContent) {
            for (Entry<String, String> e : node.entrySet()) {
                if (!e.getKey().startsWith("text_")) {
                    fileNames.add(getPhysicalBinaryPath(e.getValue()));
                }
            }
        }

        // delete inactive files
        walkAndRemove(fileNames, "/home/glassfish/glassfish3/storage/repository/datastore");
        Logger.getLogger(RepositoryBean.class.getName()).log(Level.INFO, "Finished Repo Garbage Collection");
    }

    private InputStream decompressStream(String mimeType, InputStream in, LinkedHashMap<String, String> folder) {
        ArchiveInputStream din;
        switch (mimeType) {
            case "application/x-gzip":
                try {
                    InputStream gzIn = new BufferedInputStream(new GzipCompressorInputStream(in));
                    try {
                        din = new ArchiveStreamFactory().createArchiveInputStream(gzIn);
                        String name = din.getNextEntry().getName();
                        folder.put("text_filename", name);
                        in = din;
                    } catch (ArchiveException ex) {
                        String name = folder.get("text_filename");
                        folder.put("text_filename", FilenameUtils.getBaseName(name));
                        in = gzIn;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "application/zip":
                din = new ZipArchiveInputStream(in);
                try {
                    String name = din.getNextEntry().getName();
                    folder.put("text_filename", name);
                    in = din;
                } catch (IOException ex) {
                    Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
        return in;
    }
}
