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
 package manyminds.webserver;
/*
 * ServerThread.java
 *
 * Written by Eric Eslinger
 * Copyright © 1998-1999 University of California
 * All Rights Reserved.
 *
 * Agenda
 *
 * History
 *	16 JUN 99 EME modified initialization structure commented code.
 *
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;

import manyminds.debug.Level;
import manyminds.debug.Logger;


/**
 *	A full fledged webserver.  Written to be created by a parent process and started as a child thread.
 *	Specialized to handle ManyMinds variable replacement.  Also only understands GET and HEAD.  Will not
 *	serve html-ized directories if files do not exist.
 *	@author Eric M Eslinger
 */
 
public class ServerThread extends Thread implements HttpConstants {

    protected LinkedList myThreads;
    protected String myRoot;
    protected int myTimeout;
    protected int myPort;
    protected int myWorkers;
    protected boolean myContinue;
    private static Logger logger = Logger.getLogger("manyminds");

/**
 *	Create the serverthread.
 *	@param root A string specifying the root directory of the webspace.
 *	@param port The TCP port to listen to.  Usually 80.
 *	@param to The timeout for TCP transactions, without one, sockets will block indefinitely if the client drops.
 *	@param workers The number of worker threads to leave hanging around.
 */

    public ServerThread (String root, int port, int to, int workers) 
                    throws IOException {
        super("Web Server Main Thread");
        myRoot = root;
        myWorkers = workers;
        myThreads = new LinkedList();
        myTimeout = to;
        myPort = port;
        myContinue = true;
        //GlobalMonitor.kickStart();
        if (!myRoot.endsWith("/")) {
            myRoot = myRoot + "/";
        }
        setPriority(5);
    }

/**
 *	Kill the server.
 */
    public void halt() {
        myContinue = false;
    }

/**
 *	Get the root filespec.
 *	@return A file object referring to the root directory.
 */

    public String getRoot() {
        return myRoot;
    }
	
/**
 *	How many workers do we keep around?
 *	@return the number of worker threads maintained.
 */	
 
    public int getPoolSize() {
        return myWorkers;
    }

/**
 *	Get access to the container of workerthreads.
 *	@return a Sequence of WebWorkerThreadThread objects.
 *	@see com.objectspace.jgl.Sequence
 *	@see manyminds.webserver.WebWorkerThread
 */
    public Collection pool() {
        return myThreads;
    }

/**
 *  Main event cycle of ServerThread.  Called by the start() method, and waits for a connection
 *	and allocates a worker thread to handle the connection. 
 */
    public void run() {
        for (int i = 0; i < myWorkers; ++i) {
            WebWorkerThread w = new WebWorkerThread(this);
            w.start();
            myThreads.add(w);
        }
        try {
            ServerSocket ss = new ServerSocket(myPort);
            while (myContinue) {
                Socket s = ss.accept();
                WebWorkerThread w = null;
                synchronized (myThreads) {
                    if (myThreads.isEmpty()) {
                        w = new WebWorkerThread(this);
                    } else {
                        w = (WebWorkerThread) myThreads.removeLast();
                    }
                    w.setSocket(s);
                }
            }
        } catch (java.io.IOException e) {
            logger.log(Level.SEVERE,"Error in Webserver",e);
        }
    }
}