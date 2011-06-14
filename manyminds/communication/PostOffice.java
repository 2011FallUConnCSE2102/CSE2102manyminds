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
 package manyminds.communication;

/*
 * postoffice.java
 *
 * Written by Eric Eslinger
 * Copyright © 1998-1999 University of California
 * All Rights Reserved.
 * 
 * History
 *	16 OCT 98 EME Created today
 * 	11 NOV 98 EME Fixed a little delivery bug, added debugging outputs & comments
 *	20 FEB 99 EME Major rewrite to make PostOffice a singleton pattern, and add support for
 *								RMI messsage delivery to other hosts
 *	16 JUN 99 EME Added javadoc comments
 */
 
 
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import manyminds.debug.Level;
import manyminds.debug.Logger;

/**
 *	A central mailbox registry for use in delivering messages.  A singleton pattern in that only one
 *	PostOffice should exist per process.  Also has RMI methods that can be called from other hosts in
 *	order to deliver messages from foreign hosts.
 *	@author Eric M Eslinger
 *	@see PostOfficeCoordinator
 *	@see Mailbox
 *	@see RemotePostOffice
 */

public class PostOffice extends UnicastRemoteObject implements RemotePostOffice {
    private static Map _registry = new HashMap();
    private static Logger logger = Logger.getLogger("manyminds");
    private static Logger messageLogger = Logger.getLogger("manyminds.communication");
    private static RemotePostOffice _single;
    private LinkedList _deadLetters = new LinkedList();
    
    private PostOffice() throws java.rmi.RemoteException {
    }

/**
 *	The only way to get a PostOffice object.  The constructor is private to hold to the singleton pattern.  
 *	The method will return the singleton PostOffice, creating it if necessary.
 *	@return the singleton PostOffice.
 */

    synchronized public static RemotePostOffice getPostOffice() {
    if (_single == null) {
            try {
                LocateRegistry.createRegistry(1099);
            } catch (RemoteException e) {
                logger.log(Level.FINEST, "RMI Registry already created, continuing");
            }
            try {
                Registry r = LocateRegistry.getRegistry("localhost");
                try {
                    _single = (RemotePostOffice)r.lookup("PostOffice");
                } catch (NotBoundException e) {
                    logger.log(Level.FINEST, "PostOffice not created, continuing");
                    _single = new PostOffice();
                    try {
                        r.bind("PostOffice", _single);
                    } catch (AlreadyBoundException e2) {
                        logger.log(Level.FINEST, "PostOffice already created, continuing");
                        _single = (RemotePostOffice)r.lookup("PostOffice");
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error getting PostOffice", e);
            }
        }
        return _single;			
    }
/**
 *	Register some mailbox with a name.  Multiply named mailboxes and multiple mailboxes with the name name
 *	is allowed.
 *	@param mbx the mailbox to register.
 *	@param name the name under which to register
 */
    public void register(RemoteMailbox mbx, String name) throws RemoteException {
        synchronized (_registry) {
            Object registrationList = _registry.get(name);
            if (registrationList == null) {
                registrationList = new LinkedList();
                _registry.put(name,registrationList);
            }
            ((List)registrationList).add(mbx);
        }
        synchronized (_deadLetters) {
            Iterator it = _deadLetters.iterator();
            while (it.hasNext()) {
                try {
                    Message msg = (Message)it.next();
                    if (msg.getParameter("receiver").equals(name)) {
                        mbx.deliver(msg);
                        it.remove();
                    }
                } catch (NoSuchValueException e) {
                }
            }
        }			
    }

/**
 *	Unregister all instances of a mailbox.
 *	@param mbx the mailbox to unregister.  Note that it will be removed from everywhere, if it is 
 *						 multiply registered.
 */
    public void unregister(RemoteMailbox mbx) throws RemoteException {
    }
    /**
    *	Deliver some message to your mailboxes.  Usually called by the PostOfficeCoordinator.
    *	Duplicates the message to be delivered so to prevent race conditions.
    *	@param msg The message to be delivered.
    *	@return true if sucessful, false otherwise
    *	@exception java.rmi.RemoteException Thrown during RMI failures.
    */
    public boolean send(Message msg) throws RemoteException {
        synchronized (this) {
            try {
               if (!msg.getParameter("receiver").equals("syslog")) {
                    messageLogger.log(Level.FINE,"Captured message: "+msg.toString());
                }
            } catch (NoSuchValueException e) {}	
            boolean retVal = false;
            synchronized (_registry) {
                try {
                    Object mailboxes = _registry.get(msg.getParameter("receiver"));
                    if (mailboxes != null) {
                        Iterator it = ((List)mailboxes).iterator();
                        while (it.hasNext()) {
                            retVal = true;
                            RemoteMailbox mbx = (RemoteMailbox)it.next();
                            mbx.deliver((Message)msg.clone());
                        }
                    }
                } catch (NoSuchValueException e) {}
            }
            if (retVal == false) {
                _deadLetters.addLast(msg);
                if (_deadLetters.size() > 300) {
                    _deadLetters.removeFirst();
                }
            }
            return retVal;
        }
    }
}