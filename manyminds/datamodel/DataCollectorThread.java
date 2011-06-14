package manyminds.datamodel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

/**
 * @author eric
 * 
 * One of these represents a remote ManyMinds system, and can be used to query and download
 * all sorts of interesting information.
 *
 */
public class
DataCollectorThread
extends Thread {
    private String myHostname;
    private String myAddress;
    private File myRoot;
    private RemoteDataServerDispatch myRemoteDispatch;
    private String myStatus = "Uninitialized";
    
    private BoundedRangeModel myProgress;
    
    /**
     * @param hostname The logical hostname (usually the computer's name) for humans to read.
     * @param address A dotted quad, the IP number of the remote ManyMinds
     * @param saveRoot The folder into which we should stick all of our downloaded info
     */
    public
    DataCollectorThread(String hostname, String address, File saveRoot) {
        myHostname = hostname;
        myAddress = address;
        myRoot = saveRoot;
        myProgress = new DefaultBoundedRangeModel(0,0,0,100);
    }
    
    /**
     * Used to report the status of this connection.  Nice for the GUI display.
     * @return "Uninitialized", "Connecting to xx.xx.xx.xx", "Querying Saved Files", "Getting Saved Files", "Querying Log Files", or "Getting Log Files",
     */
    public String
    getStatus() {
        return myStatus;
    }
    
    
    /**
     * @return Hostname: xx.xx.xx.xx
     * @see java.lang.Object#toString()
     */
    public String
    toString() {
        return myHostname + ": "+myStatus;
    }
    
    /**
     * Probably only used internally.
     * @param s the status string
     * @param i the value of the progress bar
     */
    protected void
    setStatus(String s, int i) {
        setStatus(s);
        myProgress.setValue(i);
    }
    
    /**
     * Used to manipulate the BoundedRangeModel associated with this connection
     * @param i The maximum value the bar will be set to
     */
    protected void
    setMaximum(int i) {
        myProgress.setMaximum(i);
    }
    
    /**
     * Used to manipulate the BoundedRangeModel associated with this connection
     * @param s the current progress string.
     */
    protected void
    setStatus(String s) {
        myStatus = s;
        //myStatusLabel.setText(myStatus);
    }
    
    /**
     * The BoundedRangeModel returned here will be updated as the collector goes through its sequence of stuff.
     * It is ideal for use in a JProgressBar
     *
     * @return The BoundedRangeModel associated with this connection
     */
    public BoundedRangeModel
    getProgressModel() {
        return myProgress;
    }
    
    /**
     * The main event sequence for the connection.  Connect, query the saved file names, get the saved files
     * individually, query the log file names, and get the log files individually.  Note that an early error
     * will kill the whole progress, because they're usually connection failures, so we expect that those failures will
     * continue once they start.
     * 
     * @see java.lang.Runnable#run()
     */
    public void
    run() {
        try {
            setStatus("Connecting to "+myAddress);
            Registry reg = LocateRegistry.getRegistry(myAddress);
            myRemoteDispatch = (RemoteDataServerDispatch)reg.lookup("ManyMinds-DataServerDispatch");
            setStatus("Querying Saved Files");
            String[] dataNames = myRemoteDispatch.getAllDataNames();
            setStatus("Getting Saved Files");
            File saveFileRoot = new File(myRoot,"CapturedSaveFiles");
            setMaximum(dataNames.length - 1);
            for (int i = 0; i < dataNames.length; ++i) {
                setStatus("Getting "+dataNames[i],i);
                if ((dataNames[i].indexOf("Users/eric") == -1) && (!dataNames[i].endsWith(".xml"))) { //ignore local files and the data.xml files that get generated when users double click savefiles.
                    byte[] datafile = myRemoteDispatch.getFile(dataNames[i]);
                    String filename = myHostname + dataNames[i].replace('/','.');
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(saveFileRoot,filename)));
                    out.write(datafile,0,datafile.length);
                    out.close();
                }
            }
            setStatus("Querying Log Files");
            String[] logNames = myRemoteDispatch.getAllLogfileNames();
            setStatus("Getting Log Files");
            File logFileRoot = new File(myRoot,"CapturedLogFiles");
            setMaximum(logNames.length - 1);
            for (int i = 0; i < logNames.length; ++i) {
                setStatus("Getting "+logNames[i],i);
                if ((logNames[i].endsWith("csv")) && (logNames[i].indexOf("Users/eric") == -1)) {  //only retreive non-local csv logs (not the xml ones)
                    byte[] datafile = myRemoteDispatch.getFile(logNames[i]);
                    String filename = myHostname + logNames[i].replace('/','.');
                    File outfile = new File(logFileRoot,filename);
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
                    out.write(datafile,0,datafile.length);
                    out.close();
                }
            }
            setStatus("Done");
        } catch (Throwable t) {
            t.printStackTrace();
            setStatus("Error!");
        }  
    }
}
