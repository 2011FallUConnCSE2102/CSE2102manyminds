/*	File GlobalVariableDatabase.java
 * =============================================================================
 * 
 * A message dispatcher that keeps track of the contents of the ManyMinds Artifact
 *  so that users (e.g., the HTML server) can send KQML
 *	messages to find out what's in there.
 * 
 * Author Eric Eslinger
 * Copyright © 1999 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 *	15 Dec 99	CSS	Call new ResearchNotes accessor methods to access data models
 *								underneath Artifact elements.
 * 
 * =============================================================================
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
 package manyminds.helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import manyminds.communication.KQMLMessage;
import manyminds.communication.MessageDispatcher;
import manyminds.debug.Level;


public class GlobalVariableDatabase
extends MessageDispatcher
implements ChangeListener {

    private HashMap _globals;
    private HashMap _subscriptions;
    private TreeSet _allSubscribe;

    private static GlobalVariableDatabase _single;
    
    private class Subscription {
        private String _subscriber;
        private String _replyWith;
        
        public Subscription (String s, String rw) {
            _subscriber = s;
            _replyWith = rw;
        }
        
        public String getSubscriber() {
            return _subscriber;
        }
        
        public String getReplyWith() {
            return _replyWith;
        }
    }
    

    public static void kickStart() {
        synchronized (GlobalVariableDatabase.class) {
            if (_single == null) {
                _single = new GlobalVariableDatabase();
                //_single.start();
            }
        }
    }
    
    private GlobalVariableDatabase() {
        super("global-val-box");
        _globals = new HashMap();
        _subscriptions = new HashMap();
        _allSubscribe = new TreeSet();
       // ResearchNotes.getCurrentNotes().addChangeListener(this);
    }

    public void
    stateChanged(ChangeEvent ce) {
        String currentSection = "";//ResearchNotes.getCurrentNotes().getCurrentSection().getShortTitle();
        Object oldCD = _globals.get("Current Section");
        if ((oldCD == null) || (!currentSection.equals(oldCD.toString()))) {
            changeGlobalValue("Current Section",currentSection);
        }
    }
    
    public void handleMessages(List messages) {
        Iterator mit = messages.iterator(); 
        while (mit.hasNext()) {
            KQMLMessage mess = (KQMLMessage)mit.next(); 
            if (mess.getPerformative().equals("ask-one")) {
                Object val = _globals.get(mess.getContent());
                if (val != null) {
                    KQMLMessage.deliver("(tell :sender global-val-box :receiver " + mess.getSender()
                                            + " :ontology global-values"
                                            + " :in-reply-to " + mess.getReplyWith() + " :content (" 
                                            + mess.getContent() + " = " + val.toString() + "))");
                } else {
                    KQMLMessage.deliver("(sorry :sender global-val-box :receiver " + mess.getSender()
                                            + " :ontology global-values"
                                            + " :in-reply-to " + mess.getReplyWith() + " :content " 
                                            + ")");
                }
            } else if (mess.getPerformative().equals("ask-all")) {
                String content  = new String();
                synchronized (_globals) {
                    Iterator it = _globals.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next().toString();
                        content = content + "(" + key + " = " + _globals.get(key).toString() + ") ";
                    }
                    KQMLMessage.deliver("(tell :sender global-val-box :receiver " + mess.getSender()
                                            + " :ontology global-values"
                                            + " :in-reply-to " + mess.getReplyWith() + " :content " 
                                            + content.toString() + ")");
                }
            } else if (mess.getPerformative().equals("stream-all")) {
                synchronized (_globals) {
                    Iterator it = _globals.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next().toString();
                        KQMLMessage.deliver("(stream :sender global-val-box :receiver " + mess.getSender()
                                                + " :ontology global-values"
                                                + " :in-reply-to " + mess.getReplyWith() + " :content " 
                                                + "(" + key + " = " + _globals.get(key).toString() + "))");
                    }
                }
                KQMLMessage.deliver("(eos :sender global-val-box :receiver " + mess.getSender()
                                        + " :ontology global-values"
                                        + " :in-reply-to " + mess.getReplyWith() +")");
            } else if (mess.getPerformative().equals("subscribe")) {
                if (mess.getContent().equals("__all__")) {
                    synchronized (_globals) {
                        Iterator it = _globals.keySet().iterator();
                        while (it.hasNext()) {
                            String key = it.next().toString();
                            KQMLMessage.deliver("(tell :sender global-val-box :receiver " + mess.getSender()
                                                + " :ontology global-values"
                                                + " :in-reply-to " + mess.getReplyWith() + " :content " 
                                                + "(" + key + " = " + _globals.get(key).toString() + "))");
                            synchronized (_subscriptions) {
                                Object subscriptionList = _subscriptions.get(key);
                                if (subscriptionList == null) {
                                    subscriptionList = new LinkedList();
                                    _subscriptions.put(key,subscriptionList);
                                }
                                ((List)subscriptionList).add(new Subscription(mess.getSender(),mess.getReplyWith()));
                            }
                        }
                        _allSubscribe.add(new Subscription(mess.getSender(),mess.getReplyWith()));
                    }
                } else {
                    synchronized (_globals) {
                        String key = mess.getContent();
                        Object val = _globals.get(key);
                        if (val != null) {
                            KQMLMessage.deliver("(tell :sender global-val-box :receiver " + mess.getSender()
                                                    + " :ontology global-values"
                                                    + " :in-reply-to " + mess.getReplyWith() + " :content (" 
                                                    + mess.getContent() + " = " + val.toString() + "))");
                        }
                        synchronized (_subscriptions) {
                            Object subscriptionList = _subscriptions.get(key);
                            if (subscriptionList == null) {
                                subscriptionList = new LinkedList();
                                _subscriptions.put(key,subscriptionList);
                            }
                            ((List)subscriptionList).add(new Subscription(mess.getSender(),mess.getReplyWith()));
                        }
                    }
                }					
            } else if (mess.getPerformative().equals("unsubscribe")) {
                synchronized (_subscriptions) {
                    if (mess.getContent().equals("__all__")) {
                        //removal from the hashmap can invalidate iterators.
                        Iterator it = _subscriptions.keySet().iterator();
                        while (it.hasNext()) {
                            Iterator innerIt = ((Collection)it.next()).iterator();
                            while (innerIt.hasNext()) {
                                if (((Subscription)innerIt.next()).getSubscriber().equals(mess.getSender())) {
                                    innerIt.remove();
                                }
                            }
                        }							
                    } else {
                        Iterator it = ((Collection)_subscriptions.get(mess.getContent())).iterator();
                        while (it.hasNext()) {
                            if (((Subscription)it.next()).getSubscriber().equals(mess.getSender())) {
                                it.remove();
                            }
                        }
                    }
                }
            } else {
                logger.log(Level.INFO,"I didn't know what to do with "+mess.toString());
            }
        }
    }
    
    public static void changeValue(String key, String val) {
        kickStart();
        _single.changeGlobalValue(key,val);
    }
    
    private synchronized void changeGlobalValue(String key, String val) {
        if (_globals.put(key,val) == null) {
            Iterator it = _allSubscribe.iterator();
            Object subscriptionList = _subscriptions.get(key);
            if (subscriptionList == null) {
                subscriptionList = new LinkedList();
                _subscriptions.put(key,subscriptionList);
            }
           while (it.hasNext()) {
                Subscription s = (Subscription)it.next();
                ((List)subscriptionList).add(s);
            }
        }
        Object subscriptionList = _subscriptions.get(key);
        if (subscriptionList != null) {
            Iterator subIt = ((List)subscriptionList).iterator();
            while (subIt.hasNext()) {
                Subscription s = ((Subscription)subIt.next());
                KQMLMessage.deliver("(tell :sender global-val-box :receiver " + s.getSubscriber()
                                        + " :ontology global-values"
                                        + " :in-reply-to " + s.getReplyWith() + " :content (" 
                                        + key + " = " + val + "))");
            }
        }
        logger.log(Level.FINEST,"Global Change: "+key+" Val: "+val.replace('\n',' '));
    }
}