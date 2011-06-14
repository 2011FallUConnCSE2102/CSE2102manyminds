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

import java.awt.dnd.DnDConstants;
import java.io.Serializable;

import javax.swing.event.EventListenerList;

import manyminds.agents.Agent;
import manyminds.debug.Level;
import manyminds.debug.Logger;

public abstract class
KnowledgeObject
extends Object
implements Serializable {
    //public static DataFlavor knowledgeObjectFlavor
    //public static DataFlavor dataFlavorsSupported

    public final static Boolean FALSE = Boolean.FALSE;
    public final static Boolean TRUE = Boolean.TRUE;
    public final static String UNSET_INDEX = "__UNSET_ID__";

    protected static Logger logger = Logger.getLogger("manyminds");
    protected static Logger knowledgeLogger = Logger.getLogger("manyminds.knowledgebase");
    private String myURL;
    private String myIndex;
    private String myDetail;
    private boolean myPolarity;
    private Object myValue;
    protected String myReference;
    protected EventListenerList myListeners;

    public
    KnowledgeObject(String u,String i,String d,String r) {
        myURL = u;
        myIndex = i;
        myDetail = d;
        myReference = r;
        myListeners = new EventListenerList();
        if (myIndex == null) {
            myIndex = UNSET_INDEX;
        }
    }
    
    public
    KnowledgeObject() {
        this (null,null,null,null);
    }

    public abstract String
    toXML();
    
    public void
    setURL(String s) {
        String oldURL = myURL;
        myURL = s;
        knowledgeLogger.log(Level.FINEST,"Changing URL of "+getIndex()+ " to " + s);
        fireURLChanged(oldURL);
    }
    
    public void
    setIndex(String s) {
        myIndex = s;
    }
    
    public void
    setDetail(String s) {
        String oldDetail = myDetail;
        myDetail = s;
        knowledgeLogger.log(Level.FINEST,"Changing detail of "+getIndex()+ " to " + s);
        fireDetailChanged(oldDetail);
    }
    
    public void
    setReference(String s) {
        String oldReference = myReference;
        myReference = s;
        knowledgeLogger.log(Level.FINEST,"Changing reference of "+getIndex()+ " to " + s);
        fireReferenceChanged(oldReference);
    }
    
    public Object
    getValue() {
        return myValue;
    }
    
    public boolean
    getBooleanValue() {
        Object val = getValue();
        if (val instanceof Boolean) {
            return ((Boolean)val).booleanValue();
        } else {
            return false;
        }
    }
    
    public boolean
    getPolarity() {
        return myPolarity;
    }
    
    public void
    setPolarity(boolean b) {
        myPolarity = b;
    }

    public void
    setValue(Object o) {
        Object old = myValue;
        if ((o == null) || (!o.equals(old))) {
            myValue = o;
            fireValueChanged(old);
        }
    }
    
 /*   public void
    setValue(String s) {
        setValue((Object)s);
    }*/
    
    public String
    getURL() {
        return myURL;
    }

    public String
    getIndex() {
        return myIndex;
    }

    public String
    getDetail() {
        return myDetail;
    }

    public String
    getReference() {
        return myReference;
    }
    
    public void
    addKnowledgeListener(KnowledgeListener kl) {
        myListeners.add(KnowledgeListener.class,kl);
    }
    
    public void
    removeKnowledgeListener(KnowledgeListener kl) {
        myListeners.remove(KnowledgeListener.class,kl);
    }
    
    public String
    toString() {
        return getDetail();
    }
    
    public int
    knowledgeDropped(KnowledgeObject ko, KnowledgeBase kb) {
        return DnDConstants.ACTION_NONE;
    }
    
    public boolean
    isAppropriate(KnowledgeObject ko) {
        return false;
    }

/*    public boolean
    equals(Object o) {
        if (getClass().isInstance(o)) {
            KnowledgeObject ko = (KnowledgeObject)o;
            if ((getIndex() != null) && (!getIndex().equals(UNSET_INDEX))) {
                if (getIndex().equals(ko.getIndex())) {
                    return true;
                }
            } else {
                if (getReference() != null) {
                    return (getReference().equals(ko.getReference()));
                } else {
                    return this == o;
                }
            }
        }
        return false;
    }*/
    
    protected void
    fireDetailChanged(String old) {
        KnowledgeEvent ke = null;
        Object[] listeners = myListeners.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==KnowledgeListener.class) {
                if (ke == null) {
                    ke = new KnowledgeEvent(this, old, KnowledgeEvent.DETAIL_CHANGE);
                }
                ((KnowledgeListener)listeners[i+1]).detailChanged(ke);
            }
         }
     }
            
    protected void
    fireURLChanged(String old) {
        KnowledgeEvent ke = null;
        Object[] listeners = myListeners.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==KnowledgeListener.class) {
                if (ke == null) {
                    ke = new KnowledgeEvent(this, old, KnowledgeEvent.URL_CHANGE);
                }
                ((KnowledgeListener)listeners[i+1]).URLChanged(ke);
            }
        }
    }
    
    protected void
    fireValueChanged(Object old) {
        KnowledgeEvent ke = null;
        Object[] parms = {getDetail(),old,getValue()};
        String oldVal = "nothing";
        if (old != null) {oldVal = old.toString();}
        String currentName = "Unknown Agent";
        Thread t = Thread.currentThread();
        if (t instanceof Agent.AgentReasoningThread) {
            currentName = ((Agent.AgentReasoningThread)t).currentAgentName();
        }
        if (getDetail() != null) {
            knowledgeLogger.log(Level.FINE,currentName+" KnowledgeObject "+getDetail()+" changing value from "+oldVal+" to "+getValue().toString(),parms);
        }
        Object[] listeners = myListeners.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==KnowledgeListener.class) {
                if (ke == null) {
                    ke = new KnowledgeEvent(this, old, KnowledgeEvent.VALUE_CHANGE);
                }
                ((KnowledgeListener)listeners[i+1]).valueChanged(ke);
            }
        }
    }
            
    protected void
    fireReferenceChanged(Object old) {
        KnowledgeEvent ke = null;
        Object[] listeners = myListeners.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==KnowledgeListener.class) {
                if (ke == null) {
                    ke = new KnowledgeEvent(this, old, KnowledgeEvent.REFERENCE_CHANGE);
                }
                ((KnowledgeListener)listeners[i+1]).referenceChanged(ke);
            }
        }
    }
}