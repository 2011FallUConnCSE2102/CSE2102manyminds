/*
 * Copyright (C) 1998-2002 Regents of the University of California This file is
 * part of ManyMinds.
 * 
 * ManyMinds is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * ManyMinds is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ManyMinds; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package manyminds.datamodel;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

import manyminds.debug.Level;
import manyminds.debug.Logger;

/**
 * Represents some kind of Data that the ManyMinds system is interested in
 * keeping persistently. Implements the fairly complex set of notification
 * events that are necessary when the Data object changes and needs to notify
 * Peered data objects.
 * 
 * @author eric
 *  
 */

public abstract class AbstractData extends UnicastRemoteObject implements
        Serializable, Data, RemoteData {

    protected static Logger logger = Logger.getLogger("manyminds.datamodel");

    protected EventListenerList myListeners = new EventListenerList();

    private Set handledEvents = Collections.synchronizedSet(new HashSet());

    private List myUpstreamPeers = new LinkedList();

    protected boolean valueChangeHalt = false;

    protected String currentChangeID = null;

    /**
     * Deals with all the notification that needs to spread out, both to local
     * Listeners and to remotely Peered data.
     *  
     */

    protected synchronized void fireValueChanged() {
        if (!valueChangeHalt) {
            try {
                // Guaranteed to return a non-null array
                Object[] listeners = myListeners.getListenerList();
                // Process the listeners last to first, notifying
                // those that are interested in this event
                ChangeEvent ce = new ChangeEvent(this);
                if (currentChangeID == null) {
                    currentChangeID = Double.toString(System
                            .currentTimeMillis())
                            + ": "
                            + Double.toString(Math.random() * Double.MAX_VALUE);
                }
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (DataListener.class
                            .isAssignableFrom((Class) listeners[i])) {
                        DataContext.getContext().deferUpdate(
                                (DataListener) listeners[i + 1], ce);
                    } else if (RemoteData.class
                            .isAssignableFrom((Class) listeners[i])) {
                        try {
                            ((RemoteData) listeners[i + 1]).peerValueChanged(
                                    getValue(), currentChangeID);
                        } catch (RemoteException re) {
                            logger.log(Level.WARNING,
                                    "Error forwarding event to remote object",
                                    re);
                            myListeners.remove((Class) listeners[i],
                                    (EventListener) listeners[i + 1]);
                        }
                    }
                }
                Object[] peers = myUpstreamPeers.toArray();
                for (int i = 0; i < peers.length; ++i) {
                    try {
                        ((RemoteData) peers[i]).peerValueChanged(getValue(),
                                currentChangeID);
                    } catch (RemoteException re) {
                        logger.log(Level.WARNING,
                                "Error forwarding event to remote object", re);
                        myUpstreamPeers.remove(i);
                    }
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Error dispatching value change", t);
            } finally {
                currentChangeID = null;
            }
        }
    }

    /**
     * Add a local data listener.
     * @param pdl the listener
     */
    
    public void addDataListener(DataListener pdl) {
        /*TODO fix myListeners.add(pdl.getClass(), pdl); tms*/
    }

    /**
     * Remove a local data listener
     * @param pdl the listener
     */
    
    public void removeDataListener(DataListener pdl) {
        /* TODO fix myListeners.remove(pdl.getClass(), pdl); tms*/
    }

    public abstract void setValue(String t);

    public abstract String getValue();

    public AbstractData() throws RemoteException {
    }

    public abstract boolean reset();

    public abstract int getType();

    public abstract String getTypeString();

    
    /**
     * Two objects are equal if they have the same value returned by getValue().  This allows
     * for remote proxy objects on different systems to be equal() without being the same
     * object (or even subtype of Data).
     * @author eric
     *
     */
    
    public boolean equals(Object o) {
        if ((o instanceof Data)
                && (((Data) o).getValue().equals(getValue()))) {
            return true;
        } else {
            return false;
        }
    }

    public String peerGetValue() throws java.rmi.RemoteException {
        return getValue();
    }

    public String peerGetTypeString() throws java.rmi.RemoteException {
        return getTypeString();
    }

    public void peerSetTypeString(String s) throws java.rmi.RemoteException {
        setTypeString(s);
    }

    public synchronized void peerValueChanged(String s, String changeID)
            throws java.rmi.RemoteException {
        if ((!handledEvents.contains(changeID)) && (s != null)
                && (!s.equals(getValue()))) {
            handledEvents.add(changeID);
            currentChangeID = changeID;
            setValue(s);
        }
    }

    public void peerConnectDownstream(RemoteData rd)
            throws java.rmi.RemoteException {
        myListeners.add(RemoteData.class, rd);
    }

    public void peerDisconnectDownstream(RemoteData rd)
            throws java.rmi.RemoteException {
        myListeners.remove(RemoteData.class, rd);
    }

    public void peerDisconnectUpstream(RemoteData rd)
            throws java.rmi.RemoteException {
        myUpstreamPeers.remove(rd);
    }

    public int peerGetType() throws java.rmi.RemoteException {
        return getType();
    }

    public synchronized void peerConnectUpstream(RemoteData rd)
            throws java.rmi.RemoteException {
        rd.peerConnectDownstream(this);
        myUpstreamPeers.add(rd);
    }

    public synchronized void forgetUpstreamPeers()
            throws java.rmi.RemoteException {
        Object[] peers = myUpstreamPeers.toArray();
        for (int i = 0; i < peers.length; ++i) {
            ((RemoteData) peers[i]).peerDisconnectDownstream(this);
        }
        myUpstreamPeers = new LinkedList();
        handledEvents = Collections.synchronizedSet(new HashSet());
    }

    public synchronized void forgetDownstreamPeers()
            throws java.rmi.RemoteException {
        Object[] listeners = myListeners.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (RemoteData.class.isAssignableFrom((Class) listeners[i])) {
                ((RemoteData) listeners[i + 1]).peerDisconnectUpstream(this);
                myListeners.remove((Class) listeners[i],
                        (RemoteData) listeners[i + 1]);
            }
        }
    }
}