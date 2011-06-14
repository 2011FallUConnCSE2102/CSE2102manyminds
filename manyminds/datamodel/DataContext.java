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
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.event.ChangeEvent;

import manyminds.debug.Level;
import manyminds.debug.Logger;

/**
 * A singleton class that handles the creation and location of data servers.
 * @author eric
 */
public class
DataContext {

    private DataServer myDatabase;
    private DataServer myGlobalVariables;
    private String docAddy,GVAddy;
    private DataServer myDataServer;
    private DataServer myGlobalServer;

    private boolean isLoading;
    private HashSet deferredUpdates = new HashSet();

    private static DataContext single;
    private static Logger logger = Logger.getLogger("manyminds");

    static {
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {}
    }
    
    /**
     * @author eric
     * Used to package a data event.  Sometimes these events get fired right away, and sometimes they get
     * stored and processed in batches.
     * @see deferUpdate
     */
    private class
    Update {
        private DataListener listener;
        private ChangeEvent event;
        
        public
        Update(DataListener l, ChangeEvent ce) {
            listener = l;
            event = ce;
        }
        
        public DataListener
        getListener() {
            return listener;
        }
        
        public ChangeEvent
        getChangeEvent() {
            return event;
        }
        
        public boolean
        equals(Object o) {
            if (o instanceof Update) {
                if ((((Update)o).listener == listener) && (((Update)o).event.getSource() == event.getSource())) {
                    return true;
                }
            }
            return false;
        }
        
    }

    private
    DataContext() {
        try {
            myDatabase = new DataServer();
            myGlobalVariables = new DataServer();
            myDatabase.setDataLogged(true);
            myGlobalVariables.setDataLogged(true);
        } catch (RemoteException re) {
            logger.log(Level.SEVERE,"Error creating data context",re);
        }
    }
    
    /**
     * @return the singleton DataContext for this instance of ManyMinds
     */
    public static synchronized DataContext
    getContext() {
        if (single == null) {
            single = new DataContext();
        }
        return single;
    }

    /**
     * This DataServer contains permanent data (workspace and slider settings mostly).  It gets saved.
     * @return the DataServer for permanent data
     */
    public DataServer
    getSharedData() {
        return myDatabase;
    }
    
    /**
     * This DataServer contains transient data (current artifact page, etc).  It does not get saved.
     * @return the DataServer for transient data
     */
    public DataServer
    getGlobalVariables() {
        return myGlobalVariables;
    }
    
    /**
     * Decide if the local data has been modified since it has last been saved.
     * @return true always (right now), in the future will return false if you don't need to save.
     */
    public boolean
    checkSave() {
        return true;
    }
    
    /**
     * Prunes orphan entries from the Permanent database, as well as empty ones (since nonexistent entries
     * count as blanks to the DataServer).  Not necessary, but might reduce the size of your savefiles.
     */
    public void
    compactSharedData() {
    }
        
    
    public synchronized void
    deferUpdate(DataListener pdl, ChangeEvent ce) {
        if (isLoading) {
            deferredUpdates.add(new Update(pdl,ce));
        } else {
            pdl.valueChanged(ce);
        }
    }
    public synchronized boolean
    getLoading() {
        return isLoading;
    }
    
    public synchronized void
    setLoading(boolean b) {
        isLoading = b;
        if (!isLoading) {
            Iterator it = deferredUpdates.iterator();
            while (it.hasNext()) {
                Update u = (Update)it.next();
                try {
                    u.getListener().valueChanged(u.getChangeEvent());
                } catch (Throwable t) {
                    logger.log(Level.SEVERE,"Error handling deferred updates",t);
                }
            }
            deferredUpdates.clear();
        }
    }
        
    public void
    emergencyDataLoss(DataServer dc) {/*
        logger.log(Level.SEVERE,"Emergency Data Loss, rebinding master data");
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
        }
        if (dc == myDatabase) {
            myDataServer = myDatabase.getDataServer();
            try {
                Registry r = LocateRegistry.getRegistry("localhost");
                r.rebind("ManyMinds-Documents", myDataServer);
            } catch (Throwable t) {
                logger.log(Level.SEVERE,"Error rebinding master data",t);
            }
        } else if (dc == myGlobalVariables) {
            myGlobalServer = myGlobalVariables.getDataServer();
            try {
                Registry r = LocateRegistry.getRegistry("localhost");
                r.rebind("ManyMinds-Globals", myGlobalServer);
            } catch (Throwable t) {
                logger.log(Level.SEVERE,"Error rebinding master data",t);
            }
        }*/
    }
}