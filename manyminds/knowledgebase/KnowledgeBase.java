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
 package manyminds.knowledgebase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.EventListenerList;

import manyminds.debug.Logger;

public class
KnowledgeBase {
    
    private List myStorage;
    protected List myKnowledgeListeners;
    protected static Logger logger = Logger.getLogger("manyminds");
    protected static Logger knowledgeLogger = Logger.getLogger("manyminds.knowledgebase");
    
    private HashSet myBeliefs = new HashSet();
    private HashMap myBeliefListeners = new HashMap();
    private HashMap myBeliefMutexes = new HashMap();
    
    private class
    MutexListener
    extends KnowledgeAdapter {
        
        private Collection myCollection;
        private String myName;
        
        public
        MutexListener(Collection c, String s) {
            myCollection = c;
            myName = s;
        }
        
        public void
        valueChanged(KnowledgeEvent ke) {
            if (ke.getOldValue().equals(new Boolean(false))) {
                Iterator it = myCollection.iterator();
                while (it.hasNext()) {
                    String s = it.next().toString();
                    if (!s.equals(myName)) {
                        myBeliefs.remove(s);
                    }
                }
            }
        }
        
    }
    
    public
    KnowledgeBase() {
        myStorage = Collections.synchronizedList(new ArrayList());
        myKnowledgeListeners = Collections.synchronizedList(new LinkedList());
    }
        
    public boolean
    isBelieved(String s) {
        return myBeliefs.contains(s);
    }
    
    public void
    mutexBeliefs(Collection c) {
        Iterator it = c.iterator();
        while (it.hasNext()) {
            String s = it.next().toString();
            addKnowledgeListener(new MutexListener(c,s),s);
        }
    }
    
    public synchronized void
    believe(String s) {
        if (myBeliefs.add(s)) {
            fireBeliefChanged(s,false);
        }
    }
    
    public synchronized void
    unbelieve(String s) {
        if (myBeliefs.remove(s)) {
            fireBeliefChanged(s,true);
        }
    }
    
    public void
    addKnowledgeListener(KnowledgeListener kl, String belief) {
        Object o = myBeliefListeners.get(belief);
        if (o == null) {
            o = new EventListenerList();
            myBeliefListeners.put(belief,o);
        }
        ((EventListenerList)o).add(KnowledgeListener.class,kl);
    }

    public Collection
    getRules() {
        return Collections.unmodifiableCollection(myStorage);
    }
    
    
    public synchronized void
    addRule(Rule r) {
        myStorage.add(r);
        Iterator it = myKnowledgeListeners.iterator();
        while (it.hasNext()) {
            r.addKnowledgeListener((KnowledgeListener)it.next());
        }
    }
    
    
    public synchronized boolean
    removeRule(Rule r) {
        if (myStorage.remove(r) != false) {
            Iterator it = myKnowledgeListeners.iterator();
            while (it.hasNext()) {
                r.removeKnowledgeListener((KnowledgeListener)it.next());
            }
            return true;
        } else {
            return false;
        }
    }
            
    public String
    toXML() {
        StringBuffer sb = new StringBuffer();
        Iterator it = myStorage.iterator();
        while (it.hasNext()) {
            KnowledgeObject ko = (KnowledgeObject)it.next();
            sb.append(ko.toXML());
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public synchronized void
    addKnowledgeListener(KnowledgeListener kl) {
        myKnowledgeListeners.add(kl);
        Iterator it = myStorage.iterator();
        while (it.hasNext()) {
            ((KnowledgeObject)it.next()).addKnowledgeListener(kl);
        }
    }
    
    public synchronized void
    removeKnowledgeListener(KnowledgeListener kl) {
        myKnowledgeListeners.remove(kl);
        Iterator it = myStorage.iterator();
        while (it.hasNext()) {
            ((KnowledgeObject)it.next()).removeKnowledgeListener(kl);
        }
    }

    public void
    fireBeliefChanged(String belief, boolean old) {
        Object o = myBeliefListeners.get(belief);
        if (o != null) {
            Object[] listeners = ((EventListenerList)o).getListenerList();
            KnowledgeEvent ke = null;
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==KnowledgeListener.class) {
                    if (ke == null) {
                        ke = new KnowledgeEvent(this, new Boolean(old), KnowledgeEvent.VALUE_CHANGE, belief);
                    }
                    ((KnowledgeListener)listeners[i+1]).valueChanged(ke);
                }
            }
            listeners = myKnowledgeListeners.toArray(); 
            for (int i = listeners.length-1; i>=0; i-=1) {
                if (ke == null) {
                    ke = new KnowledgeEvent(this, new Boolean(old), KnowledgeEvent.VALUE_CHANGE, belief);
                }
                ((KnowledgeListener)listeners[i]).valueChanged(ke);
            }
        }
    }
}