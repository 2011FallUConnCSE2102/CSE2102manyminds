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
 * mailbox.java
 *
 * Written by Eric Eslinger
 * Copyright © 1998-1999 University of California
 * All Rights Reserved.
 *
 * Agenda
 *
 * History
 *	16 OCT 98 EME Created today
 *  26 FEB 99 EME Modified Mailbox to be compatible with new singleton PostOfficeCoordinator
 *	16 JUN 99 EME Added javadoc comments
 */ 
 
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

/**
 *	A mailbox is essentially a message queue.  The only ordering guarantee is that 
 *	receipt order will be preserved.
 *	@author Eric M Eslinger
 *	@see PostOfficeCoordinator
 *	@see PostOffice
 */

public class Mailbox extends UnicastRemoteObject implements RemoteMailbox {
    private LinkedList myQueue;
    
    public static final int INDEFINITE_BLOCK = -1;
    public static final int NONBLOCKING = 0;

/**
*	Create a new empty mailbox.
*/

    public Mailbox() throws java.rmi.RemoteException{
        //myQueue = Collections.synchronizedList(new LinkedList());
        myQueue = new LinkedList();
    }
    
/**
*	Check for mail.  Possibly blocking based on the parameter.  Will block until 
*	mail arrives or until timeout expires, whichever comes first.
*	@param msec number of milliseconds to block for, block indefinitely if msec < 0.
*							It is possible to block for 0 msec.
*	@return true if there is mail in the queue after blocking time has expired.
*/
    public synchronized boolean
    check(long msec) {
        synchronized (myQueue) {
            if (myQueue.size() == 0) {
                try {
                    if (msec > 0) {
                        wait(msec);
                    } else if (msec < 0) {
                        wait();
                    }
                } catch (Exception e) {}
            }
            return (myQueue.size() != 0);
        }
    }
    
/**
*	Check the length of the mail queue
*	@return the length of the mail queue at the time of the call.
*/
    
    public synchronized int
    length() {
        return myQueue.size();
    }
    
/**
*	Pop the next mail message (delivered in FIFO order).
*	@return The next message.
*	@exception EmptyMailboxException if the mailbox was empty at the time of the call.
*/
    public synchronized Message
    get()
    throws EmptyMailboxException {
        if (myQueue.size() != 0) {
            return (Message)myQueue.removeLast();
        } else {
            throw new EmptyMailboxException();
        }
    }
    
/**
*	Add a message to the end of the queue.
*	@param msg The message to add, does not clone the incoming message.
*/
    
    public void 
    deliver(Message msg)
    throws java.rmi.RemoteException {
        myQueue.addFirst(msg);
    }
}