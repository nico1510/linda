package Events;

import java.util.ArrayList;


public class JobFinishedEvent {

    private String nodeID;
    private String toolID;
    private boolean success;
    private ArrayList<String> filesToSave;
    private String folderToDelete;

    public JobFinishedEvent(String nodeID,String toolID,boolean success,ArrayList<String> filesToSave,String folderToDelete) {
        this.nodeID = nodeID;
        this.toolID = toolID;
        this.success = success;
        this.filesToSave = filesToSave;
        this.folderToDelete = folderToDelete;
    }

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
     * @return the success
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return the filesToSave
     */
    public ArrayList<String> getFilesToSave() {
        return filesToSave;
    }

    /**
     * @param filesToSave the filesToSave to set
     */
    public void setFilesToSave(ArrayList<String> filesToSave) {
        this.filesToSave = filesToSave;
    }

    /**
     * @return the folderToDelete
     */
    public String getFolderToDelete() {
        return folderToDelete;
    }

    /**
     * @param folderToDelete the folderToDelete to set
     */
    public void setFolderToDelete(String folderToDelete) {
        this.folderToDelete = folderToDelete;
    }



}
