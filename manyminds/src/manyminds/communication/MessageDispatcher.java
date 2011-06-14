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
 package manyminds.src.manyminds.communication;

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
  * A basic object built around a wait/process event loop.  The MessageDispatcher will wait
  * until it receives a message in its inbox.  Whenever new messages arrive, they are put into
  * a List and passed into handleMessage(List).
  * 
  * If you want to subclass MessageDispatcher, you need to implement handleMessage.  You can also
  * override initialize() and finish().  Be sure to include calls to super.initialize() and super.finish()
  * at the beginning of overriding method definitions.
  * 
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
    private static boolean isDispatching = false;
    private static boolean somethingToDo = true;
    private static boolean workerRunning = false;
    private String myName = null;
    private static List workerList = new LinkedList();
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

    /**
     *  Kickstart the dispatchers.  You must call this method at least once before the 
     * dispatcher thread will start delivering messages.  The dispatchers don't run on
     * their own automatically for performance reasons.
     */
    public synchronized static void
    startDispatching() {
        if (!workerRunning) {
            workerRunning = true;
            myWorker.start();
        }
    }

    /**
     * Until the dispatcher has a name, it cannot receive messages, because its name
     * is its address in the PostOffice running locally.  This will register your dispatcher
     * and (provided startDispatching() has been called) start it's event cycle.
     * @param n
     */
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
    
    /**
     * Find out the name of the currently running dispatcher.
     * @param n this dispatcher's name, or null if currently unnamed.
     */
    
    public String
    getName() {
        return myName;
    }

/**
*	Dispatcher constructor.  
*
*/
    public MessageDispatcher(String n) {
        setDispatcherName(n);
    }
    
/**
 * Override this if your dispatcher has some cleanup code that it needs to run when the 
 * event cycle is terminated (either by system shutdown, or for some other reason).
 * Remember to call super.finish() somewhere in your overridden version.
 *
 */
    public void finish() {
        return;
    }
    
    /**
     * Override this if your dispatcher has some startup code that it needs to run before the 
     * event cycle is started.
     * Remember to call super.initialize() somewhere in your overridden version.
     *
     */
    public void initialize() {
        return;
    }
    
    /**
     * Add a message to the outgoing queue.  Will be delivered when there's nothing more
     * important going on in the dispatch thread.
     * @param m
     * @return
     */
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
            
    
/**
 * Alert the event cycle's execution thread (which is not the main thread) that some work
 * needs to be done.
 */
    public static void
    signalDispatcher(MessageDispatcher md) {
        synchronized (myWorker) {
            dispatcherQue.add(md);
            myWorker.notifyAll();
        }
    }
    
    /**
     * Override this with whatever you want your dispatcher to do when it gets new mail.
     * There are no guarantees about where this method will run (in particular, it will not
     * run in the java event or main thread), so be careful.  Deadlocking this thread will
     * deadlock all running dispatchers (although Throwables get caught).
     * @param messages a List of Message objects to be handled.  This list will not be modified after it gets passed in.
     */
    public abstract void handleMessages(List messages);
    
}