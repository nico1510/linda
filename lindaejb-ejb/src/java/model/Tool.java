/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;

/**
 *
 * @author nico
 */
public class Tool {
    
    private String toolID;
    private String displayname;
    private ArrayList<String> outputfiles;
    private ArrayList<String> inputfiles;

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
     * @return the displayname
     */
    public String getDisplayname() {
        return displayname;
    }

    /**
     * @param displayname the displayname to set
     */
    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    /**
     * @return the outputfiles
     */
    public ArrayList<String> getOutputfiles() {
        return outputfiles;
    }

    /**
     * @param outputfiles the outputfiles to set
     */
    public void setOutputfiles(ArrayList<String> outputfiles) {
        this.outputfiles = outputfiles;
    }

    /**
     * @return the inputfiles
     */
    public ArrayList<String> getInputfiles() {
        return inputfiles;
    }

    /**
     * @param inputfiles the inputfiles to set
     */
    public void setInputfiles(ArrayList<String> inputfiles) {
        this.inputfiles = inputfiles;
    }


}
