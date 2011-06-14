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
 * MessageDispatcher.java
 *
 * The basic mailbox wait / dispatch cycle for ManyMinds
 *
 * Written by Eric Eslinger
 * Copyright © 1998-1999 University of California
 * All Rights Reserved.
 *
 * Agenda
 *
 * History
 *	01 JUL 99 EME Created today.
 *
 */
 
 /**
  *	The ManyMinds agent object.  Each agent object allocates its own thread of execution
  * in which the event cycle processes.
  * @author Eric M Eslinger
  * @see java.lang.Thread
  */
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import manyminds.debug.Level;
import manyminds.debug.Logger;
 
public abstract class
MessageDispatcher
extends Object {

    private Mailbox myMailbox = null;
    private static boolean isContinuing = true;
    private static boolean workerRunning = false;
    private String myName = null;
    private static List dispatcherQue = Collections.synchronizedList(new LinkedList());
    private static List addedDispatchers = Collections.synchronizedList(new LinkedList());
    private static List removeDispatcherList = new LinkedList();
    private static List messagesToSend = Collections.synchronizedList(new LinkedList());
    private static final long GRANULARITY = 30000;
    private static MessageDispatchWorkerThread myWorker = new MessageDispatchWorkerThread();
    protected static Logger logger = Logger.getLogger("manyminds.communication");
    
    private class
    DispatchedMailbox
    extends Mailbox {
        public
        DispatchedMailbox()
        throws java.rmi.RemoteException {
            super();
        }
            
    
        public void 
        deliver(Message msg)
        throws java.rmi.RemoteException {
            super.deliver(msg);
            signalDispatcher(MessageDispatcher.this);
        }
    }
    
    private static class
    MessageDispatchWorkerThread
    extends Thread {
        
        public
        MessageDispatchWorkerThread() {
            super("Message Event Dispatch Worker Thread");
            setPriority(2);
        }
        
        public void
        start() {
            super.start();
        }
        
        public void
        run() {
            while (isContinuing) {
                while (!addedDispatchers.isEmpty()) {
                    MessageDispatcher md = (MessageDispatcher)addedDispatchers.remove(0);
                    md.initialize();
                    dispatcherQue.add(md);
                }
                while (!messagesToSend.isEmpty()) {
                    Message m = (Message)messagesToSend.remove(0);
                    m.deliver();
                }
                while (!dispatcherQue.isEmpty()) {
                    MessageDispatcher md =  (MessageDispatcher)dispatcherQue.remove(0);
                    LinkedList messages = new LinkedList();
                    while (md.myMailbox.check(Mailbox.NONBLOCKING)) { 
                        try {
                            messages.add(md.myMailbox.get());
                        } catch (EmptyMailboxException e) {}
                    }
                    try {
                        if (!messages.isEmpty()) {
                            md.handleMessages(messages);
                        }
                    } catch (Throwable t) {
                        logger.log(Level.SEVERE, "Trouble processing message "+md.getName(), t);
                    }
                    while (!messagesToSend.isEmpty()) {
                        Message m = (Message)messagesToSend.remove(0);
                        m.deliver();
                    }
                }
                synchronized (this) {
                    if (dispatcherQue.isEmpty()) {
                        try {
                            wait(GRANULARITY);
                        } catch (InterruptedException ie) {}
                    }
                }
            }
        }
        
        protected void
        addDispatcher(MessageDispatcher md) {
            synchronized (addedDispatchers) {
                addedDispatchers.add(md);
                try {
                    PostOffice.getPostOffice().register(md.myMailbox, md.getName());
                } catch (java.rmi.RemoteException e) {
                    logger.log(Level.SEVERE, "Couldn't register MessageDispatcher " + md.getName(), e);
                }
            }
        }
        
        protected void
        removeDispatcher(MessageDispatcher md) {
            synchronized (removeDispatcherList) {
                removeDispatcherList.add(md);
            }
        }
    }

    public
    MessageDispatcher() {
    }

    public synchronized static void
    startDispatching() {
        if (!workerRunning) {
            workerRunning = true;
            myWorker.start();
        }
    }

    public void
    setDispatcherName(String n) {
        if (myName == null) {
            try {
                myMailbox = new DispatchedMailbox();
            } catch (java.rmi.RemoteException re) {
                logger.log(Level.SEVERE, "Trouble creating mailbox for "+n, re);
            }
            myName = n;
            myWorker.addDispatcher(this);
        }
    }
    
    public String
    getName() {
        return myName;
    }

/**
*	Agent constructor.  Attempts to load a file in rules/agents/name/name.clp where
*	name is the agent name passed as a string into the agent.
*
*/
    public MessageDispatcher(String n) {
        setDispatcherName(n);
    }
    
/**
*	Terminate the dispatch loop.  Will eventually lead to thread shutdown, but not
*	immediately.
*/

/**
*	The main event cycle of the dispatcher.  Called by the java.lang.Thread.start() method.
* 	The thread will block on its mailbox, dump all new messages into its handleMessages() method,
*	and then repeat.  
*/

    public void finish() {
        return;
    }
    
    public void initialize() {
        return;
    }
    
    public static boolean
    deliverMessage(Message m) {
        if (myWorker != null) {
            synchronized (myWorker) {
                messagesToSend.add(m);
                myWorker.notifyAll();
                return true;
            } 
        } else {
            return false;
        }
    }
            
    
    public static void
    signalDispatcher(MessageDispatcher md) {
        synchronized (myWorker) {
            dispatcherQue.add(md);
            myWorker.notifyAll();
        }
    }
    
    public abstract void handleMessages(List messages);
    
}