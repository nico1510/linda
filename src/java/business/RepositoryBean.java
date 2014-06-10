package business;

import Events.UpdateEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.ValueFactory;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import model.ProxyFile;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author nico
 */
@Stateless
public class RepositoryBean implements RepositoryService, Serializable {

    @Resource(name = "jcr/rep1")
    private Repository repository;
    @Inject
    Event<UpdateEvent> eventSource;

    @Override
    public String getPhysicalBinaryPath(String propPath) {
        Session session = createSession(false);
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
    public String persistDataset(InputStream in, LinkedHashMap<String, String> folder, String mimeType) {
        if (mimeType.equals("application/x-gzip") | mimeType.equals("application/zip")) {
            in = decompressStream(mimeType, in, folder);
        }
        
        String fileName = folder.get("text_filename");
        String rdfFormat = FilenameUtils.getExtension(fileName);
        folder.put("text_rdfformat", rdfFormat);
        
        Session session = createSession(true);
        try {
            ValueFactory vf = session.getValueFactory();
            Binary bfile = vf.createBinary(in);

            Node root = session.getRootNode();
            Node filenode = root.addNode(UUID.randomUUID().toString().replaceAll("-", ""));
            Property p = filenode.setProperty("dataset", bfile);

            Iterator it = folder.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String key = (String) pairs.getKey();
                String value = (String) pairs.getValue();
                filenode.setProperty(key, value);
                it.remove();
            }
            String nodeID = filenode.getPath();
            session.save();
            return nodeID;

        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
            eventSource.fire(new UpdateEvent());
        }

        return null;
    }

    @Override
    public String persistMeta(InputStream in, String nodeID, String fileName) {
        Session session = createSession(true);
        try {
            ValueFactory vf = session.getValueFactory();
            Binary bfile = vf.createBinary(in);

            Node datasetNode = session.getNode(nodeID);

            Property metaProp = datasetNode.setProperty(fileName, bfile);

            session.save();
            String metaPath = metaProp.getPath();

            return metaPath;

        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
            eventSource.fire(new UpdateEvent());
        }
        return null;
    }

    @Override
    public ProxyFile downloadFile(String propPath) {
        ProxyFile proxyFile = new ProxyFile();
        Session session = this.createSession(false);

        try {
            Property downloadProp = session.getProperty(propPath);
            Binary b = downloadProp.getBinary();

            proxyFile.setLength(b.getSize());
            proxyFile.setContent(b.getStream());
            proxyFile.setContentType("text/plain");
            proxyFile.setName(downloadProp.getName());

            if (downloadProp.getName().equals("dataset")) {
                Node parentNode = downloadProp.getParent();
                if (parentNode.hasProperty("text_filename") && !parentNode.getProperty("text_filename").getString().isEmpty()) {
                    proxyFile.setName(parentNode.getProperty("text_filename").getString());
                }
            }

            b.dispose();

            return proxyFile;
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
        }

        return null;
    }

    @Override
    public void acceptVisitor(ItemVisitor repVisitor, Session session) {
        try {
            if (session == null || !session.isLive()) {
                session = createSession(false);
            }
            session.getRootNode().accept(repVisitor);
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session.logout();
        }

    }

    @Override
    public void changeFolder(LinkedHashMap<String, String> folder) {
        Session session = createSession(true);
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
            eventSource.fire(new UpdateEvent());
        }

    }

    @Override
    public void deleteItems(String[] itemPaths) {
        Session session = createSession(true);
        try {
            for (int i = 0; i < itemPaths.length; i++) {
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
            eventSource.fire(new UpdateEvent());
        }
    }

    @Override
    public Session createSession(boolean writeable) {
        Session session = null;

        try {
            if (writeable) {
                session = repository.login(new SimpleCredentials(UUID.randomUUID().toString(), "".toCharArray()), null);
            } else {
                session = repository.login();
            }
        } catch (LoginException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchWorkspaceException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return session;
    }

    @Override
    public void cleanup() {
        /**
         * try { JackrabbitRepositoryFactory rf = new RepositoryFactoryImpl();
         * Properties prop = new Properties(); String DIR =
         * "/home/glassfish/glassfish3/storage";
         * prop.setProperty("org.apache.jackrabbit.repository.home", DIR);
         * prop.setProperty("org.apache.jackrabbit.repository.conf", DIR +
         * "/repository.xml"); JackrabbitRepository rep = (JackrabbitRepository)
         * rf.getRepository(prop); RepositoryManager rm =
         * rf.getRepositoryManager(rep); Session session = rep.login(new
         * SimpleCredentials("", "".toCharArray())); DataStoreGarbageCollector
         * gc = rm.createDataStoreGarbageCollector(); try { gc.mark();
         * gc.sweep(); } finally { gc.close(); }
         *
         * session.logout(); rm.stop(); } catch (RepositoryException ex) {
         * Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE,
         * null, ex); }
         */
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