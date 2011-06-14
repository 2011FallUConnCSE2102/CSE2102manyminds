/*  Copyright (C) 1998-2002 Regents of the University of California
 *  This file is part of ManyMinds.
 *
 *  ManyMinds is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  ManyMinds is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with ManyMinds; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package manyminds.datamodel;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.swing.JOptionPane;

import manyminds.debug.Level;
import manyminds.debug.Logger;

public class
DataServerDispatch
extends UnicastRemoteObject
implements RemoteDataServerDispatch {

    private HashMap myDataServers = new HashMap();
    private HashMap allUserRoots = new HashMap();
    private static Logger logger = Logger.getLogger("manyminds.datamodel");
    private File myRoot, autosaveRoot;
    private static final String myTag = "ManyMinds-DataServerDispatch";
    private boolean polling = true;
    private PollReplier myPollReplier = new PollReplier();

    private static final long autosaveDelay = 1000 * 60 * 5;

    private static RemoteDataServerDispatch single = null;
    
    private class
    PollReplier
    extends Thread {
        
        private MulticastSocket pollSocket;
        
        public
        PollReplier() {
            try {
                pollSocket = new MulticastSocket(4442);
                InetAddress group = InetAddress.getByName("230.0.0.1");
                pollSocket.joinGroup(group);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        
        public void
        run() {
            while (polling) {
                try {
                    byte[] buf = new byte[512];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    pollSocket.receive(packet);
                    String s = new String(packet.getData());
                     String reply = myTag + " "+InetAddress.getLocalHost().getHostName()+":"+InetAddress.getLocalHost().getHostAddress();
                    byte[] replyBytes = reply.getBytes();
                    DatagramPacket replyPacket = new DatagramPacket(replyBytes,replyBytes.length,packet.getAddress(),packet.getPort());
                    pollSocket.send(replyPacket);      
                //} catch (InterruptedException ie) {
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            try {
                InetAddress group = InetAddress.getByName("230.0.0.1");
                pollSocket.leaveGroup(group);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
    
    static {
        try {	
            LocateRegistry.createRegistry(1099);
        } catch (Throwable t) {}
    }
    
    public synchronized static RemoteDataServerDispatch
    getDispatch() {
        return single;
    }
    
    public synchronized static void
    initializeDispatch() {
        if (single == null) {
            try {
                single = new DataServerDispatch();
                Registry r = LocateRegistry.getRegistry(1099);
                r.bind(myTag, single);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Error binding dispatch", t);
            }
        }
    }
    
    public synchronized static void
    connectToDispatch(String addy) {
        if (single == null) {
            try {
                Registry r = LocateRegistry.getRegistry(addy);
                single = (RemoteDataServerDispatch)r.lookup(myTag);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Error getting dispatch", t);
            }
        }
    }
    
    public synchronized static void
    connectOrCreateDispatch() {
        if (single == null) {
            try {
                Registry r = LocateRegistry.getRegistry(1099);
                try {
                    single = (RemoteDataServerDispatch)r.lookup(myTag);
                } catch (NotBoundException nbe) {
                    single = new DataServerDispatch();
                    try {
                        r.bind(myTag, single);
                    } catch (Throwable t) {
                        logger.log(Level.SEVERE, "Error binding dispatch", t);
                    }
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Error getting dispatch", t);
            }
        }
    }

    public String[]
    getAllDataNames() 
    throws RemoteException {
        List allNames = new LinkedList();
        Iterator it = allUserRoots.values().iterator();
        while (it.hasNext()) {
            File f = (File)it.next();
            File saveRoot = new File(f,"Inquiry Island"+System.getProperty("file.separator")+"SavedFiles");
            if (saveRoot.exists()) {
                File[] savedFiles = saveRoot.listFiles();
                for (int i = 0; i < savedFiles.length; ++i) {
                    if (!savedFiles[i].getName().startsWith(".")) {
                        try {
                            allNames.add(savedFiles[i].getCanonicalPath());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        }
        String[] retVal = new String[allNames.size()];
        it = allNames.iterator();
        int i = 0;
        while (it.hasNext()) {
            retVal[i++] = it.next().toString();
        }
        return retVal;
    }
    
    public String[]
    getAllLogfileNames() 
    throws RemoteException {
        List allNames = new LinkedList();
        Iterator it = allUserRoots.values().iterator();
        while (it.hasNext()) {
            File f = (File)it.next();
            File saveRoot = new File(f,"Inquiry Island"+System.getProperty("file.separator")+"LogFiles");
            if (saveRoot.exists()) {
                File[] savedFiles = saveRoot.listFiles();
                for (int i = 0; i < savedFiles.length; ++i) {
                    if (!savedFiles[i].getName().startsWith(".")) {
                        try {
                            allNames.add(savedFiles[i].getCanonicalPath());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        }
        String[] retVal = new String[allNames.size()];
        it = allNames.iterator();
        int i = 0;
        while (it.hasNext()) {
            retVal[i++] = it.next().toString();
        }
        return retVal;
    }
    
    public byte[]
    getFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
                byte[] b = new byte[512];
                int len;
                while ((len = in.read(b, 0, b.length)) != -1) {
                    out.write(b, 0, len);
                }
                in.close();
                return out.toByteArray();
            } catch (Throwable t) {
                t.printStackTrace();
                return new byte[0];
            }
        } else {
            return new byte[0];
        }
    }

    public String[]
    getDataNames()
    throws RemoteException {
        String retVal[] = new String[myDataServers.keySet().size()];
        Iterator it = (new TreeSet(myDataServers.keySet())).iterator();
        int i = -1;
        while (it.hasNext()) {
            retVal[++i] = it.next().toString();
        }
        return retVal;
    }
    
    protected
    DataServerDispatch()
    throws RemoteException {
        myRoot = new File(System.getProperty("manyminds.home") + System.getProperty("file.separator") + "SavedFiles");
        File[] files = myRoot.listFiles();
        autosaveRoot = new File(System.getProperty("manyminds.home") + System.getProperty("file.separator") + "Autosave");
        for (int i = 0; i < files.length; ++i) {
            try {
                if (!files[i].getName().startsWith(".")) {
                    File autosavedFile = new File(autosaveRoot,files[i].getName());
                    if (autosavedFile.exists() && (autosavedFile.lastModified() > files[i].lastModified())) {
                        int j = JOptionPane.showConfirmDialog(null, "Autosave file for "+files[i].getName()+" is newer, use it instead?","Autosave",JOptionPane.YES_NO_OPTION);
                        if (j == JOptionPane.NO_OPTION) {
                            myDataServers.put(files[i].getName(),files[i]);
                        } else if (j == JOptionPane.YES_OPTION) {
                            myDataServers.put(autosavedFile.getName(),autosavedFile);
                        }
                    } else {
                        myDataServers.put(files[i].getName(),files[i]);
                    }
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Error loading "+files[i].getName(),t);
            }
        }
        File otherRoot = new File(System.getProperty("manyminds.home"));
        otherRoot = otherRoot.getParentFile();
        if (otherRoot != null) {
            otherRoot = otherRoot.getParentFile();
        }
        if (otherRoot != null) {
            File[] userFiles = otherRoot.listFiles();
            for (int i = 0; i < userFiles.length; ++i) {
                File iiroot = new File(userFiles[i],"Inquiry Island");
                if (iiroot.exists()) {
                    allUserRoots.put(userFiles[i].getName(),userFiles[i]);
                }
            }
        }
//        myPollReplier.start();
        startAutosaving();
    }
    
    public RemoteDataServer
    getDataServer(String s)
    throws RemoteException {
        Object o = myDataServers.get(s);
        Object[] parms = {s};
        logger.log(Level.INFO,"Getting data server",parms);
        if (o instanceof RemoteDataServer) {
            return (RemoteDataServer)o;
        } else {
            try {
                logger.log(Level.INFO,"Loading Data");
                RemoteDataServer rds = DataFactory.createDataServer((File)o);
                logger.log(Level.INFO,"Done Loading Data");
                myDataServers.put(s,rds);
                return rds;
            } catch (Throwable t) {
                logger.log(Level.SEVERE,"Error loading data server",t);
                return null;
            }
        }
    }
    
    public RemoteDataServer
    newDataServer(String s) 
    throws RemoteException {
        if (!myDataServers.containsKey(s)) {
            RemoteDataServer rds = new DataServer();
            myDataServers.put(s,rds);
            return rds;
        } else {
            return getDataServer(s);
        }
    }
    
    public void
    startAutosaving() {
        java.util.Timer autosaveTimer = new java.util.Timer();
        autosaveTimer.schedule(new TimerTask() {
            public void
            run() {
                saveAllData(autosaveRoot);
            }
        },autosaveDelay, autosaveDelay);
    }
    
    public void
    autosaveData() 
    throws RemoteException {
        saveAllData(autosaveRoot);
    }
    
    public static boolean
    checkSave() {
        int i = JOptionPane.showConfirmDialog(null, "Save your work?","Save",JOptionPane.YES_NO_OPTION);
        if (single instanceof DataServerDispatch) {
            ((DataServerDispatch)single).polling = false;
 //           ((DataServerDispatch)single).myPollReplier.interrupt();
        }
        if (i == JOptionPane.NO_OPTION) {
            try {
                DataServerDispatch.getDispatch().autosaveData();
            } catch (Throwable t) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                logger.log(Level.SEVERE,"Error autosaving data!",t);
            } finally {
                return true;
            }
        } else if (i == JOptionPane.YES_OPTION) {
            try {
                DataServerDispatch.getDispatch().saveAllData();
                return true;
            } catch (Throwable t) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                logger.log(Level.SEVERE,"Error saving data!",t);
                return false;
            }
        } else if (i == JOptionPane.CANCEL_OPTION) {
            return false;
        } else {
            return false;
        }
    }
    
    protected void
    saveAllData(File root) {
        Iterator it = myDataServers.keySet().iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            Object o = myDataServers.get(s);
            if (o instanceof DataServer) {
                try {
                    FileOutputStream fos = new FileOutputStream(new File(root,s));
                    JarOutputStream out = new JarOutputStream(fos);
                    JarEntry currentJarEntry = new JarEntry("data.xml");
                    out.putNextEntry(currentJarEntry);
                    String xml = ((DataServer)o).toXML();
                    out.write(xml.getBytes());
                    String[] resources = ((DataServer)o).listResources();
                    for (int i = 0; i < resources.length; ++i) {
                        currentJarEntry = new JarEntry(resources[i]);
                        out.putNextEntry(currentJarEntry);
                        out.write(((DataServer)o).getResource(resources[i]));
                    }
                    out.close();
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Error saving "+s,t);
                }
            }
        }
    }
    

    public void
    saveAllData()
    throws RemoteException {
        saveAllData(myRoot);
    }
}