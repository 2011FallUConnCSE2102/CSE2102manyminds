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


import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import manyminds.communication.KQMLMessage;
import manyminds.communication.MessageDispatcher;
import manyminds.communication.NoSuchValueException;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.util.StringExploder;

/**
 *	Monitors the global variables from the artifact object in the applet.  Maintains
 *	a statically hashtable.
 *	@author Eric M Eslinger
 */
 
 
public class GlobalMonitor extends MessageDispatcher {
    
    private static GlobalMonitor _single;
    private static HashMap _values = new HashMap();
    private LinkedList pageHistory = new LinkedList();
    private int currentIndex = -1;
    private static Logger logger = Logger.getLogger("manyminds");

    static {
        //_single = new GlobalMonitor();
        //_single.start();
    }

    private GlobalMonitor() {
        super("global monitor");
       KQMLMessage.deliver("(subscribe :receiver global-val-box :sender global monitor :content "+
                            "__all__ :reply-with global-value "+
                            " :language jess :ontology global-values)");
    }
                    
    public void handleMessages(List messages) {
        Iterator it = messages.iterator();
        while (it.hasNext()) {
            KQMLMessage mess = (KQMLMessage)it.next();
            if (mess.getReplyWith().equals("global-value")) {
                try {
                    String content = mess.getParameter("content");
                    List words = StringExploder.explode(content);
                    String key = (String)words.get(0);
                    String val = new String();
                    boolean inkey = true;
                    boolean firstword = true;
                    for (int x = 1; x < words.size(); ++x) {
                        if (words.get(x).equals("=")) {
                            inkey = false;
                        } else if (inkey) {
                            key = key + " " + words.get(x);
                        } else if (firstword) {
                            val = words.get(x).toString();
                            firstword = false;
                        } else {
                            val = val + " " + words.get(x).toString();
                        }
                    }
                    setValue(key,val);
                } catch (NoSuchValueException e) {
                    logger.log(Level.INFO,"Error handling message",e);
                } catch (IndexOutOfBoundsException oob) {
                    logger.log(Level.INFO,"Error handling message",oob);
                }
            } else {
                String content = mess.getContent();
                List words = StringExploder.explode(content);
                try {
                    if (((String)words.get(0)).toLowerCase().equals("back")) {
                        int distance = Integer.parseInt((String)words.get(1));
                        KQMLMessage.deliver("(tell :receiver page displayer :sender global monitor :content "
                                                    + back(distance).toString()
                                                    + " :in-reply-to page to display "
                                                    + " :language java :ontology control)");
                    } else if (((String)words.get(0)).toLowerCase().equals("forward")) {
                        int distance = Integer.parseInt((String)words.get(1));
                        KQMLMessage.deliver("(tell :receiver page displayer :sender global monitor :content "
                                                    + forward(distance).toString()
                                                    + " :in-reply-to page to display "
                                                    + " :language java :ontology control)");
                    } else if (((String)words.get(0)).toLowerCase().equals("history")) {
                        showHistory();
                    } else if (((String)words.get(0)).toLowerCase().equals("rate")) {
                        URL currentPage = forward(0);
                        if (currentPage != null) {
                            logger.log(Level.FINEST,currentPage.toString() + " rated at " + words.get(1).toString());
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    logger.log(Level.INFO,"Error handling message",e);
                }
            } 
        }
    }
    
    static public void kickStart() {}
    
    synchronized public static void setValue(String key, String val) {
        _values.put(key,val);
    }
    
    synchronized public static String getValue(String key) {
        Object o = _values.get(key);
        if (o != null) {
            return o.toString();
        } else {
            return null;
        }
    }
    
    private synchronized void _servedPage(URL url) {
        logger.log(Level.FINEST,"Served: "+url.toString());
        if ((pageHistory.size() == 0) || (!url.equals(pageHistory.get(currentIndex)))) {
            pageHistory.add(++currentIndex, url);
        }
    }
    
    private synchronized URL back(int offset) {
        try {
            currentIndex = currentIndex - offset;
            if (currentIndex < 0) {
                currentIndex = 0;
            } else if (currentIndex >= pageHistory.size()) {
                currentIndex = pageHistory.size() - 1;
            }
            return (URL)pageHistory.get(currentIndex);
        } catch (IndexOutOfBoundsException e) {	
            return null;
        }
    }
    
    private synchronized URL forward(int offset) {
        try {
            currentIndex = currentIndex + offset;
            if (currentIndex < 0) {
                currentIndex = 0;
            } else if (currentIndex >= pageHistory.size()) {
                currentIndex = pageHistory.size() - 1;
            }
            return (URL)pageHistory.get(currentIndex);
        } catch (IndexOutOfBoundsException e) {	
            return null;
        }
    }
    private synchronized void showHistory() {
        Iterator it = pageHistory.iterator();
        int x = 0;
        while (it.hasNext()) {
            System.err.println(Integer.toString(x++) + ": " + it.next().toString());
        }
        System.err.println("Current index: "+currentIndex);
    }
    
    public static void servedPage(URL url) {
        _single._servedPage(url);
    }
}