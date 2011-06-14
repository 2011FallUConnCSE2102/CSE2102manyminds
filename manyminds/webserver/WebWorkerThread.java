/*
 * WebWorkerThread.java
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;

import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.datamodel.RaterModel;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.util.ManyMindsResolver;

/**
 *	The worker thread for the webserver.  Actually finds the file and throws it out the port.
 *	@author Eric M Eslinger
 *	@see manyminds.webserver.ServerThread
 */
class WebWorkerThread extends Thread implements HttpConstants {
    static final byte[] _EOL = {(byte)'\r', (byte)'\n' };
   // static final String _VARTAG = "[[ sci-wise variable ";
    static final String _VARTAG = "[[ ";
    private static final String VARIABLE_TAG = "sci-wise variable";
    private static final String RATER_TAG = "sci-wise rater";
    private static final String TEXT_TAG = "sci-wise document";
    private Socket mySocket;
    private String myRoot;
    private String myGetString;
    private int myTimeout;
    ServerThread myParent;
    private static Logger logger = Logger.getLogger("manyminds");
    private static Data pageServed;


/**
 *	Initialize given a certain parent.
 *	@param p The parent ServerThread
 */
    WebWorkerThread(ServerThread p) {
        super("Web Server WebWorkerThread Thread");
        myParent = p;
        myRoot = myParent.getRoot();
        mySocket = null;
        setPriority(5);
    }


/**
 *	Set the socket to work with and wake up the Worker.
 *	@param s The socket the worker should use for commiunicating with the client.
 */
    synchronized void setSocket(Socket s) {
        this.mySocket = s;
        notify();
    }

/**
 *	The main event cycle for the worker thread.  Wait for a connection, run a bit,
 *	and on completion, either die or replace yourself in the server's available pool,
 *	depending on how many Workers are currently available.
 */

    public synchronized void run() {
        pageServed = DataContext.getContext().getGlobalVariables().addData("last-page-served",ManyMindsDocument.newDocument());
        while(true) {
            if (mySocket == null) {
                /* nothing to do */
                try {
                    wait();
                } catch (InterruptedException e) {
                    /* should not happen */
                    continue;
                }
            }
            try {
                handleClient();
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Error in Webserver",e);
            } finally {
                try {
                    mySocket.close();
                } catch (IOException exc) {
                    logger.log(Level.SEVERE,"Error in Webserver",exc);
                }
            }
            /* go back in wait queue if there's fewer
            * than numHandler connections.
            */
            mySocket = null;
            Collection pool = myParent.pool();
            synchronized (pool) {
                if (pool.size() >= myParent.getPoolSize()) {
                    /* too many threads, exit this one */
                    return;
                } else {
                    pool.add(this);
                }
            }
        }
    }

/**
 *	Take a textfile and throw it out the socket.  Does textual replacements of 
 *	ManyMinds variables.
 *	@param targ The file to serve
 *	@param ps The printstream to serve the file to
 *	@exception java.io.IOException if something goes wrong reading or writing.
 */

	void sendTextFile(InputStream is, PrintStream ps) throws IOException {
            ps.write(_EOL);
            StringBuffer buf = new StringBuffer();
            StringBuffer tagbuf = new StringBuffer();
            int n;
            int taglocation = 0;
            while ((n = is.read()) > 0) {
                if (taglocation == 0) {
                    if ((char)n == '[') {
                        buf = new StringBuffer();
                        tagbuf = new StringBuffer();
                        taglocation = 1;
                        buf.append((char)n);
                    } else {
                        ps.write((byte)n);
                    }
                } else if (taglocation < _VARTAG.length()) {
                    buf.append((char)n);
                    if (Character.toLowerCase((char)n) == _VARTAG.charAt(taglocation)) {
                        ++taglocation;
                    } else {
                        ps.write(buf.toString().getBytes());
                        taglocation = 0;
                    }
                } else if (((char) n) == ']') {
                    tagbuf.append((char)n);
                    String tag = tagbuf.toString();
                    if (tag.indexOf("]]") >= 0) {
                        tag = tag.substring(0,tag.length()-3).trim();
                        if (tag.startsWith(VARIABLE_TAG)) {
                            tag = tag.substring(VARIABLE_TAG.length()).trim();
                            Data pd = DataContext.getContext().getSharedData().getData(tag);
                            if (pd == null) {
                                pd = DataContext.getContext().getGlobalVariables().getData(tag);
                            }
                            if (pd != null) {
                                ps.write(pd.getValue().getBytes());
                            } else {
                            // ps.write(tag.getBytes());
                            }
                        } else if (tag.startsWith(RATER_TAG)) {
                            tag = tag.substring(RATER_TAG.length()).trim();
                            String s = createRaterForm(tag,myGetString);
                            ps.write(s.getBytes());
                        } else if (tag.startsWith(TEXT_TAG)) {
                            tag = tag.substring(TEXT_TAG.length()).trim();
                            String s = createTextForm(tag,myGetString);
                            ps.write(s.getBytes());
                        } else {
                            String s = "[[ "+tagbuf.toString();
                            ps.write(s.getBytes());
                        }
                        taglocation = 0;
                    } else {
//                        tagbuf.append(Character.toLowerCase((char)n));
                        tagbuf.append((char)n);
                        ++taglocation;
                    }
                } else {
//                    tagbuf.append(Character.toLowerCase((char)n));
                    tagbuf.append((char)n);
                    ++taglocation;
                }
            }
	}	

/**
 *	Take a non-text-file and throw it out the socket.
 *	@param targ The file to serve
 *	@param ps The printstream to serve the file to
 *	@exception java.io.IOException if something goes wrong reading or writing.
 */
	void sendFile(InputStream is, PrintStream ps) throws IOException {
            byte[] buf = new byte[1024];
            ps.write(_EOL);
            int n;
            while ((n = is.read(buf)) > 0) {
                ps.write(buf, 0, n);
            }
	}


/**
 *	Do the client handling.  Figure out what they want, and give it to them.
 *	@exception java.io.IOException if something goes wrong reading the file or
 *	socket.
 */

    void handleClient()
    throws java.io.IOException {
        InputStream is = null;
        PrintStream ps = null;
        InputStream targStream = null;
        try {
            is = new BufferedInputStream(mySocket.getInputStream());
            ps = new PrintStream(mySocket.getOutputStream());
            /* we will only block in read for this many milliseconds
            * before we fail with java.io.InterruptedIOException,
            * at which point we will abandon the connection.
            */
            
            mySocket.setSoTimeout(myTimeout);
            mySocket.setTcpNoDelay(true);
            
            /* Create a character buffer for reading and writing */
            
            StringBuffer buf = new StringBuffer();
            
            /* We only support HTTP GET/HEAD, and don't
            * support any fancy HTTP options,
            * so we're only interested really in
            * the first line.
            */

            boolean done = false;
            while (!done) {
                int r = is.read();
                if (r == -1) {
                    return;
                } else if ((r == (int) '\n') || (r == (int) '\r')) {
                    done = true;
                } else {
                    buf.append((char) r);
                }
            }
            
            String data = buf.toString();
            
            /* are we doing a GET or just a HEAD */
            boolean doingGet = false;
            /* beginning of file name */
            if (data.substring(0,4).equals("GET ")) {
                doingGet = true;
            } else if (data.substring(0,5).equals("POST ")) {
                done = false;
                String followUp = data;
                doingGet = true;
                boolean localRequest=false;
                while (!done) {
                    int r = is.read();
                    if (r == -1) {
                        done = true;
                    } else if ((r == (int) '\n') || (r == (int) '\r')) {
                        if ((buf.toString().indexOf("Host") != -1) && (buf.toString().indexOf("127.0.0.1") != -1)) {
                            localRequest = true;
                        }
                        buf = new StringBuffer();
                    } else {
                        buf.append((char) r);
                    }
                    if (is.available() <= 0 ) {
                        done = true;
                    }
                }
                String bigData = buf.toString();
                StringTokenizer valueTokenizer = new StringTokenizer(bigData,"&");
                while (valueTokenizer.hasMoreTokens()) {
                    String token = valueTokenizer.nextToken();
                    int equalLocation = token.indexOf("=");
                    String key = "";
                    String value = "";
                    if (equalLocation == -1) {
                    } else if (equalLocation == (token.length() - 1)) {
                        key = token.substring(0,token.length() - 1);
                    } else {
                        if (localRequest) {
                            key = URLDecoder.decode(token.substring(0,equalLocation),"UTF-8");
                            value = URLDecoder.decode(token.substring(equalLocation+1,token.length()),"UTF-8");
                            Data pd = DataContext.getContext().getSharedData().getData(key);
                            if (pd == null) {
                                pd = DataContext.getContext().getGlobalVariables().getData(key);
                            }
                            if (pd != null) {
                                pd.setValue(value);
                            }
                        }
                    }
                }
                data = followUp;
            } else if (data.substring(0,5).equals("HEAD ")) {
                doingGet = false;
            } else {
                /* we don't support this method */
                ps.print("HTTP/1.0 " + HTTP_BAD_METHOD +
                " unsupported method type: ");
                ps.write(data.getBytes(), 0, 5);
                ps.write(_EOL);
                ps.flush();
                return;
            }
            
            int i = 0;
            /* find the file name, from:
            * GET /foo/bar.html HTTP/1.0
            * extract "/foo/bar.html"
            */
            int endOfWord = data.substring(4).trim().indexOf(" ");
            String fname = data.substring(4).trim().substring(0,endOfWord);
            myGetString = fname;
            if (fname.startsWith("/")) {
                fname = fname.substring(1);
            }
            fname = java.net.URLDecoder.decode(fname,"UTF-8");
            String targ = myRoot + fname;
            if (targ.endsWith("/")) {
                targ = targ + "index.html";
            }
            if (!fname.startsWith("resource/")) {
                targStream = ManyMindsResolver.resolveResource(targ);
            } else {
                byte[] resourceData = DataContext.getContext().getSharedData().getResource(URLDecoder.decode(fname.substring("resource/".length()),"UTF-8"));
                if (resourceData != null) {
                    targStream = new ByteArrayInputStream(resourceData);
                }
            }
            if (targStream == null) {
                ps.print("HTTP/1.0 " + HTTP_NOT_FOUND + " not found");
                ps.write(_EOL);
            }  else {
                ps.print("HTTP/1.0 " + HTTP_OK+" OK");
                ps.write(_EOL);
            }
            ps.print("Server: Simple java");
            ps.write(_EOL);
            ps.print("Date: " + (new Date()));
            ps.write(_EOL);
            if (targStream != null) {
                //ps.print("Content-length: "+targStream.available());
                //ps.write(_EOL);
                ps.print("Last Modified: " + (new Date()));
    // Date(targ.lastModified()) 
                ps.write(_EOL);
                int ind = targ.lastIndexOf('.');
                String ct = null;
                if (ind > 0) {
                    ct = FileTypeMap.getSuffix(targ.substring(ind));
                }
                if (ct == null) {
                    ct = "unknown/unknown";
                }
                ps.print("Content-type: " + ct);
                ps.write(_EOL);
                if (doingGet) {
                    if ((ct != null) && (ct.substring(0,4).equals("text"))) {
                        sendTextFile(targStream,ps);
                        //sendFile(targStream, ps);
                        pageServed.setValue(fname);
                    } else {
                        sendFile(targStream, ps);
                    }
                }
            } else {
                send404(ps);
            }
        } catch (java.io.InterruptedIOException iioe) {
            logger.log(Level.FINEST,"Stupid timed out read",iioe);
        } catch (Throwable t) {
            logger.log(Level.SEVERE,"Error in Webserver",t);
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (is != null) {
                is.close();
            }
            if (targStream != null) {
                targStream.close();
            }
        }
    }

    protected String
    createRaterForm(String raterName, String currentPage) {
        Data d = DataContext.getContext().getSharedData().getData(raterName);
        if ((d != null) && (d instanceof RaterModel)) {
            RaterModel rm = (RaterModel)d;
            StringBuffer retVal = new StringBuffer("<FORM METHOD=\"POST\" ACTION=\"http://127.0.0.1:28082");
            retVal.append(currentPage);
            retVal.append("\">\n");
            int currentValue = rm.getIntValue();
            int settingCount = rm.getSummaries().getSize();
            retVal.append(rm.getTitle());
            retVal.append(": ");
            retVal.append(rm.getTitleToolTip());
            retVal.append("<br>\n");
            for (int i = 0; i < settingCount; ++i) {
                retVal.append(rm.getSummaries().getElementAt(i).toString());
                retVal.append("<INPUT NAME=\"");
                retVal.append(raterName);
                retVal.append("\" TYPE=RADIO VALUE=\"");
                retVal.append(Integer.toString(i));
                retVal.append("\"");
                if (i == currentValue) {
                    retVal.append(" CHECKED");
                }
                retVal.append("><br>\n");
            }
            retVal.append("<INPUT TYPE=SUBMIT> <INPUT TYPE=RESET></FORM>\n");
            return retVal.toString();
        } else {
            return "";
        }
    }

    protected String
    createTextForm(String raterName, String currentPage) {
        Data d = DataContext.getContext().getSharedData().getData(raterName);
        if ((d != null) && (d instanceof ManyMindsDocument)) {
            StringBuffer retVal = new StringBuffer("<FORM METHOD=\"POST\" ACTION=\"http://127.0.0.1:28082");
            retVal.append(currentPage);
            retVal.append("\">\n");
            retVal.append("<TEXTAREA NAME=\"");
            retVal.append(raterName);
            retVal.append("\" cols=48 rows=4>");
            retVal.append(d.getValue());
            retVal.append("</TEXTAREA><br>\n");
            retVal.append("<INPUT TYPE=SUBMIT> <INPUT TYPE=RESET></FORM>\n");
            return retVal.toString();
        } else {
            return "";
        }
    }


/**
 *	Send a "404 Not found" message to the client.
 *	@param targ The file to serve
 *	@param ps The socket to write to
 *	@exception java.io.IOException if something goes wrong writing to the socket.
 */

    void send404(PrintStream ps) throws IOException {
        ps.write(_EOL);
        ps.write(_EOL);
        ps.println("Not Found\n\n"+"The requested resource was not found.\n");
    }

}