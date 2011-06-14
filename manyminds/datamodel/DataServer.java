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
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import manyminds.debug.Level;
import manyminds.debug.Logger;

public class
DataServer
extends UnicastRemoteObject
implements RemoteDataServer {
    
    private static Logger logger = Logger.getLogger("manyminds.datamodel");
    protected Map myData = new HashMap();
    private RemoteDataServer myUpstreamPeer = null;
    private Set myDownstreamPeers = new HashSet();
    private boolean shouldLogData = false;
    
    private HashMap myResources = new HashMap();
    
    public
    DataServer()
    throws RemoteException {}
    
    public void
    setDataLogged(boolean b) {
        shouldLogData = b;
    }
    
    public void
    addData(String tag, String value, String typeString) {
        Data d = null;
        if ((typeString == null) || (!RaterModel.isPrototyped(typeString))) {
            d = ManyMindsDocument.newDocument();
            if (tag.indexOf(" IN ") >= 0) {
                d.setTypeString("0");
            }
        } else {
            d = RaterModel.instantiateRaterModel(typeString);
        }
        addData(tag,d).setValue(value);
    }
    
    public Data
    addData(String tag, Data d) {
        synchronized (myData) {
            Object o = myData.get(tag);
            if (o != null) {
                return (Data)o;
            } else if (d instanceof RemoteData) {
                myData.put(tag,d);
                if (shouldLogData) {
                    LogListener.logData(tag,d);
                }
                if (myUpstreamPeer != null) {
                    try {
                        myUpstreamPeer.peerAddData(tag,(RemoteData)d);
                    } catch (RemoteException re) {
                        logger.log(Level.WARNING,"Error adding data to upstream peer",re);
                    }
                }
                return d;
            } else {
                return d;
            }
        }
    }
    
    public Data
    getData(String tag) {
        synchronized (myData) {
            if (myData.containsKey(tag)) {
                return (Data)myData.get(tag);
            } else if (myUpstreamPeer != null) {
                try {
                    RemoteData dataThere = myUpstreamPeer.peerGetData(tag);
                    if (dataThere != null) {
                        Data dataHere = null;
                        if (dataThere.peerGetType() == Data.RSA) {
                            dataHere = RaterModel.instantiateRaterModel(dataThere.peerGetTypeString());
                        } else if (dataThere.peerGetType() == Data.DOCUMENT) {
                            dataHere = ManyMindsDocument.newDocument();
                        }
                        if (dataHere == null) {
                            myData.put(tag,dataHere);
                            dataThere.peerConnectDownstream((RemoteData)dataHere);
                            ((RemoteData)dataHere).peerConnectUpstream(dataThere);
                        }
                        return dataHere;
                    } else {
                        return null;
                    }
                } catch (Throwable t) {
                    logger.log(Level.WARNING,"Error getting data from upstream peer");
                    return null;
                }
            } else {
                return null;
            }
        }
    }
    
    public void
    updateLists(String tag) {
        synchronized (myData) {
            try {
                int ldl = tag.lastIndexOf("-");
                if (ldl > 0) {
                    String listTitle = tag.substring(0,ldl)+"-group";
                    ManyMindsDocument dataList = ManyMindsDocument.newDocument();
                    dataList = (ManyMindsDocument)addData(listTitle, dataList);
                    if (dataList.getValue().indexOf(tag) == -1) {
                        dataList.addText(" (" + tag + ")");
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public Data
    getDataGroup(String tag) {
        int ldl = tag.lastIndexOf("-");
        String listTitle = tag+"-group";
        if (ldl != -1) {
            listTitle = tag.substring(0,ldl)+"-group";
        }
        return (Data)myData.get(listTitle);
    }

    public RemoteData
    peerGetData(String tag)
    throws RemoteException {
        synchronized (myData) {
            return (RemoteData)myData.get(tag);
        }
    }
    
    public void 
    peerAddData(String tag, RemoteData rdp)
    throws java.rmi.RemoteException {
        synchronized (myData) {
            RemoteData dataHere = null;
            Object o = myData.get(tag);
            if (o != null) {
                dataHere = (RemoteData)o;
            } else {
                if (rdp.peerGetType() == Data.RSA) {
                    dataHere = RaterModel.instantiateRaterModel(rdp.peerGetTypeString());
                } else if (rdp.peerGetType() == Data.DOCUMENT) {
                    dataHere = ManyMindsDocument.newDocument();
                }
                if (dataHere != null) {
                    myData.put(tag,dataHere);
                    if (shouldLogData) {
                        LogListener.logData(tag,(Data)dataHere);
                    }
                }
            }
            if (dataHere != null) {
                rdp.peerConnectUpstream(dataHere);
                dataHere.peerConnectDownstream(rdp);
                if (myUpstreamPeer != null) {
                    myUpstreamPeer.peerAddData(tag,dataHere);
                } else {
                    rdp.peerValueChanged(dataHere.peerGetValue(),Long.toString(System.currentTimeMillis()));
                }
            } else {
                logger.log(Level.WARNING,"Couldn't add data "+rdp.peerGetTypeString()+" "+tag);
            }
        }
    }
    
    public void
    peerRemoveData(String tag)
    throws java.rmi.RemoteException {
        synchronized (myData) {
            Object o = myData.remove(tag);
            if (o != null) {
                ((RemoteData)o).forgetDownstreamPeers();
                ((RemoteData)o).forgetUpstreamPeers();
            }
            if (myUpstreamPeer != null) {
                myUpstreamPeer.peerRemoveData(tag);
            }
        }
    }
    
    public void
    removeData(String tag) {
        try {
            peerRemoveData(tag);
        } catch (RemoteException re) {
            logger.log(Level.WARNING, "Error removing data "+tag,re);
        }
    }
    
    public String
    addUniqueID(String base, Data rdp) {
        synchronized (myData) {
            String tag = null;
            if (base.endsWith("-group")) {
                base = base.substring(0,base.length()-6);
                String s = Integer.toHexString((new Double(Math.random() * Integer.MAX_VALUE)).intValue());
                while (myData.containsKey(base + "-" + s + "-group")) {
                    s = Integer.toHexString((new Double(Math.random() * Integer.MAX_VALUE)).intValue());
                }
                tag = base + "-" + s + "-group";
            } else {
                String s = Integer.toHexString((new Double(Math.random() * Integer.MAX_VALUE)).intValue());
                while (myData.containsKey(base + "-" + s)) {
                    s = Integer.toHexString((new Double(Math.random() * Integer.MAX_VALUE)).intValue());
                }
                tag = base + "-" + s;
            }
            if (tag != null) {
                addData(tag,rdp);
                return tag;
            } else {
                return null;
            }
        }
    }
    
    public String[]
    listEntries()
    throws RemoteException {
        Object[] list = myData.keySet().toArray();
        String[] retVal = new String[list.length];
        for (int i = 0; i < list.length; ++i) {
            retVal[i] = list[i].toString();
        }
        return retVal;
    }
    
    public void
    upstreamConnect(RemoteDataServer rds)
    throws RemoteException {
        try {
            DataContext.getContext().setLoading(true);
            if (!rds.equals(myUpstreamPeer)) {
                myUpstreamPeer = rds;
                String[] entryNames = listEntries();
                for (int i = 0; i < entryNames.length; ++i) {
                    ((RemoteData)getData(entryNames[i])).forgetUpstreamPeers();
                    if (getData(entryNames[i]) instanceof RaterModel) {
                        RaterModel rm = (RaterModel)getData(entryNames[i]);
                        String ldName = "comments-"+entryNames[i];
                        if (ldName != null) {
                            Data linkedDoc = getData(ldName);
                            if (linkedDoc != null) {
                                linkedDoc.reset();
                            }
                        }
                    }
                    getData(entryNames[i]).reset();
                    if (rds.peerGetData(entryNames[i]) == null) {
                        rds.peerAddData(entryNames[i],(RemoteData)getData(entryNames[i]));
                    }
                }
                entryNames = rds.listEntries();
                for (int i = 0; i < entryNames.length; ++i) {
                    if (!myData.containsKey(entryNames[i])) {
                        if (rds.peerGetData(entryNames[i]).peerGetType() == Data.RSA) {
                            addData(entryNames[i],RaterModel.instantiateRaterModel(rds.peerGetData(entryNames[i]).peerGetTypeString()));
                        } else if (rds.peerGetData(entryNames[i]).peerGetType() == Data.DOCUMENT) {
                            addData(entryNames[i],ManyMindsDocument.newDocument());
                        }
                    }
                    rds.peerAddData(entryNames[i],(RemoteData)getData(entryNames[i]));
                }
                entryNames = listEntries();
                Arrays.sort(entryNames);
                for (int i = 0; i < entryNames.length; ++i) {
                    String s = entryNames[i].toString();
                    if (!s.endsWith("-group")) {
                        updateLists(s);
                    }
                }
//                myUpstreamPeer = rds;
                myUpstreamPeer.downstreamConnect(this);
            }
        } finally {
            DataContext.getContext().setLoading(false);
        }
    }
    
    public void
    downstreamDisconnect(RemoteDataServer rds)
    throws RemoteException {
        if (myDownstreamPeers.contains(rds)) {
            String[] entryNames = listEntries();
            for (int i = 0; i < entryNames.length; ++i) {
                RemoteData dataHere = (RemoteData)getData(entryNames[i]);
                RemoteData dataThere = rds.peerGetData(entryNames[i]);
                if (dataThere != null) {
                    dataHere.peerDisconnectDownstream(dataThere);
                    dataThere.peerDisconnectUpstream(dataHere);
                }
            }
        }
    }
    
    public void
    compactData(Set s) {
    }
    
    public void
    downstreamConnect(RemoteDataServer rds)
    throws RemoteException {
        if (!myDownstreamPeers.contains(rds)) {
            myDownstreamPeers.add(rds);
        }
    }
    
    public String[]
    listResources() {
        if (myUpstreamPeer == null) {
            Object[] list = myResources.keySet().toArray();
            String[] retVal = new String[list.length];
            for (int i = 0; i < list.length; ++i) {
                retVal[i] = list[i].toString();
            }
            return retVal;
        } else {
            try {
                return myUpstreamPeer.peerListResources();
            } catch (RemoteException re) {
                logger.log(Level.WARNING,"Error getting resource from upstream peer",re);
                return null;
            }
        }
    }
    
    public byte[]
    getResource(String s) {
        if (myUpstreamPeer == null) {
            if (myResources.containsKey(s)) {
                return (byte[])myResources.get(s);
            } else {
                return null;
            }
        } else {
            try {
                return myUpstreamPeer.peerGetResource(s);
            } catch (RemoteException re) {
                logger.log(Level.WARNING,"Error getting resource from upstream peer",re);
                return null;
            }
        }
    }
    
    public void
    addResource(String s, byte[] data) {
        if (myUpstreamPeer == null) {
            myResources.put(s,data);
        } else {
            try {
                myUpstreamPeer.peerAddResource(s,data);
            } catch (RemoteException re) {
                logger.log(Level.WARNING,"Error getting resource from upstream peer",re);
            }
        }
    }
    
    public void
    removeResource(String s) {
        if (myUpstreamPeer == null) {
            myResources.remove(s);
        } else {
            try {
                myUpstreamPeer.peerRemoveResource(s);
            } catch (RemoteException re) {
                logger.log(Level.WARNING,"Error getting resource from upstream peer",re);
            }
        }
    }
    
    public String[]
    peerListResources()
    throws RemoteException {
        return listResources();
    }
    
    public byte[]
    peerGetResource(String s)
    throws RemoteException {
        return getResource(s);
    }
    
    public void
    peerAddResource(String s, byte[] data) 
    throws RemoteException {
        addResource(s,data);
    }
    
    public void
    peerRemoveResource(String s) 
    throws RemoteException {
        removeResource(s);
    }
    
    public String
    toXML() {
        Iterator it = myData.keySet().iterator();
        StringBuffer retVal = new StringBuffer("<?xml version=\"1.0\" encoding=\"");
        retVal.append(((new OutputStreamWriter(System.err)).getEncoding()));
        retVal.append("\"?>\n");
        retVal.append("<!DOCTYPE global-list-data PUBLIC \"manyminds-DTD\" \"manyminds.dtd\" >\n");
        retVal.append("<dataset>\n");
        while (it.hasNext()) {
            String key = it.next().toString();
            Data pd = (Data)myData.get(key);
            retVal.append("<global>\n");
            try {
                if (pd.getTypeString() != null) {
                    retVal.append("<url><![CDATA[");
                    retVal.append(pd.getTypeString());
                    retVal.append("]]></url>\n");
                } 
            } catch (Throwable t) {}
            retVal.append("<value><![CDATA[");
            try {
                retVal.append(pd.getValue());
            } catch (Throwable t) {}
            retVal.append("]]></value>\n<index>");
            retVal.append(key);
            retVal.append("</index>\n</global>\n");
        }
        retVal.append("</dataset>\n");
        return retVal.toString();
    }
    
}