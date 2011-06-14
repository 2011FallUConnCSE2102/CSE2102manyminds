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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.util.StringExploder;

/**
*	Implements the Message contract for the concrete <a href="http://www.cs.umbc.edu/kqml/">KQML</a> syntax.  
*  Is a MirroredJessObject so it can also exist as a jess "shadowed fact".
*	@author Eric M Eslinger
*	@see manyminds.communication.Message
*	@see manyminds.jess_extensions.MirroredJessObject
*/
public class KQMLMessage extends Message {
    
    
    protected transient HashMap myParameters = new HashMap();
    protected static transient RemotePostOffice myPostOffice = PostOffice.getPostOffice();
    protected static transient LinkedList myOutgoingMessages = new LinkedList();
    public final static transient String _UNSET = "__UNSET__";
    private static transient Logger logger = Logger.getLogger("manyminds");

    
/**
*	The value returned when a trivial accessor discovers a value to be unset.
*/


    private void writeObject(ObjectOutputStream out) throws IOException{
        out.defaultWriteObject();
        try {
            out.writeUTF(toString());
        } catch (Exception e) {
            logger.log(Level.INFO,"Error serializing message",e);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        try {
            in.defaultReadObject();
            String input = in.readUTF();
            fromString(input);
        } catch (Exception e) {
            logger.log(Level.INFO,"Error deserializing message",e);
        }
    }

    /**
    *	Verifies that the message is correct KQML syntax.
    *	@param expr The string expression representing a KQML message.
    *	@return true if the string is proper KQML, false otherwise.
    */

    static public boolean verify(String expr) {
        return true;
    }	

    
    /**
    *	Empty constructor, rarely used.
    */
    public KQMLMessage() {}
    
    /**
    *	Construct a message from a string representing some KQML message.
    *	@param expr The string expression representing a KQML message
    *	@exception SyntaxException if expr is poor syntax.
    */

    public KQMLMessage(String s) throws SyntaxException {
        fromString(s);
    }
    
    /**
    *	Clones the message.  Necessary for proper intra-VM delivery.
    *	@return a deep copy of the message.
    */
    public synchronized Object clone() {
        KQMLMessage retVal = new KQMLMessage();
        retVal.myParameters = (HashMap)myParameters.clone();
        return retVal;
    }
    
    /**
    *	Causes the message to deliver itself into the post office system.
    *	@return true if sucessful, false otherwise.
    */
    public synchronized boolean deliver() {
        try {
            myPostOffice.send(this);
        } catch (Exception e) {
            logger.log(Level.INFO, "Trouble sending message "+toString(), e);
        }
        return true;
    }
    
    public static boolean deliver(String m) {
        try {
            Message mess = new ROKQMLMessage(m);
            if (!MessageDispatcher.deliverMessage(mess)) {
                myPostOffice.send(mess);
            }
        } catch (Throwable t) {
            logger.log(Level.INFO, "Trouble sending message "+m, t);
        }
        return true;
    }
            
/**
*	Sets some parameter of the message.
*	@param key the parameter to set.
*	@param val the value to which key is set.
*/
    public synchronized void setParameter(String key, String val) {
        myParameters.put(key,val);
    }
    
/**
*	Gets some parameter from the message
*	@param key the parameter to look up
*	@return the value of key in the message
*	@exception NoSuchValueException if key is unset in the message
*/
    public synchronized String getParameter(String key) throws NoSuchValueException{
        Object retVal = myParameters.get(key);
        if (retVal == null) {
            throw new NoSuchValueException();
        } 
        return retVal.toString();			
    }

/**
*	Returns the string representation of the message.
*	@return the string representatoin of this message, suitable for passing to fromString() of another message.
*/
    public synchronized String toString() {
        StringBuffer retVal = new StringBuffer("(");
        retVal.append(getPerformative());
        Iterator it = myParameters.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            if (!key.equals("performative")) {
                retVal.append(" :").append(key).append(" ");
                retVal.append(myParameters.get(key).toString());
            }
        }
        retVal.append(")");
        return retVal.toString();
    }

/**
*	Sets all the message parameters based on a KQML string.
*	@param KQML the KQML string containing the message
*	@exception SyntaxException if the syntax is less than stellar
*/
    public synchronized void fromString(String KQML) throws SyntaxException {
        if (verify(KQML) == false) {
            throw new SyntaxException();
        } else {
            if (myParameters != null) {
                myParameters.clear();
            } else {
                myParameters = new HashMap();
            }
            KQML = StringExploder.stripParens(KQML);
            List strings = StringExploder.explode(KQML);
            setParameter("performative", 
                strings.get(0).toString().toLowerCase());
            String key = null;
            String val = null;
            Iterator it = strings.iterator();
            while (it.hasNext()) {
                String word = it.next().toString().trim();
                if (word.charAt(0) == ':') {
                    if ((key != null) && (val != null)) {
                        setParameter(key,val);
                    }
                    key = word.substring(1);
                    if (it.hasNext()) {
                        val = it.next().toString().trim();
                    }
                } else {
                    val = val + " " + word;
                }
            }
            setParameter(key,val);
        }
    }
    
    //Get accessors.  Note that since I don't want to throw exceptions during trivial accesses
    //the value __UNSET__ is returned if the parameter is unset at the time of the get call.
    
/**
*	Gets the to field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getTo() {
        String ret_val;
        try {
            ret_val=getParameter("to");
        } catch (NoSuchValueException e) {
            ret_val=_UNSET;
        }
        return ret_val;
    }

/**
*	Gets the from field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getFrom() {
        String ret_val;
        try {
            ret_val=getParameter("from");
        } catch (NoSuchValueException e) {
            ret_val=_UNSET;
        }
        return ret_val;
    }

/**
*	Gets the sender field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getSender() {
        String ret_val;
        try {
            ret_val=getParameter("sender");
        } catch (NoSuchValueException e) {
            ret_val=_UNSET;
        }
        return ret_val;
    }

/**
*	Gets the receiver field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getReceiver() {
            String ret_val;
            try {
                    ret_val=getParameter("receiver");
            } catch (NoSuchValueException e) {
                    ret_val=_UNSET;
            }
            return ret_val;
    }

/**
*	Gets the performative field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getPerformative() {
            String ret_val;
            try {
                    ret_val=getParameter("performative");
            } catch (NoSuchValueException e) {
                    ret_val=_UNSET;
            }
            return ret_val;
    }

/**
*	Gets the ontology field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getOntology() {
            String ret_val;
            try {
                    ret_val=getParameter("ontology");
            } catch (NoSuchValueException e) {
                    ret_val=_UNSET;
            }
            return ret_val;
    }

/**
*	Gets the content field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getContent() {
            String ret_val;
            try {
                    ret_val=getParameter("content");
            } catch (NoSuchValueException e) {
                    ret_val=_UNSET;
            }
            return ret_val;
    }

/**
*	Gets the language field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getLanguage() {
            String ret_val;
            try {
                    ret_val=getParameter("language");
            } catch (NoSuchValueException e) {
                    ret_val=_UNSET;
            }
            return ret_val;
    }

/**
*	Gets the reply-with field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getReplyWith() {
            String ret_val;
            try {
                    ret_val=getParameter("reply-with");
            } catch (NoSuchValueException e) {
                    ret_val=_UNSET;
            }
            return ret_val;
    }

/**
*	Gets the in-reply-to field of the message
*	@return the field, KQMLMessage._UNSET if it is unset
*/
    public synchronized String getInReplyTo() {
            String ret_val;
            try {
                    ret_val=getParameter("in-reply-to");
            } catch (NoSuchValueException e) {
                    ret_val=_UNSET;
            }
            return ret_val;
    }
    


    //Set Accessors
            
/**
*	Sets the to field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setTo(String val) {
            String tmp=getTo();
            setParameter("to",val);
            pcs.firePropertyChange("to", tmp, val);
    }
    
/**
*	Sets the from field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setFrom(String val) {
            String tmp=getFrom();
            setParameter("from",val);
            pcs.firePropertyChange("from", tmp, val);
    }
    
/**
*	Sets the sender field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setSender(String val) {
            String tmp=getSender();
            setParameter("sender",val);
            pcs.firePropertyChange("sender", tmp, val);
    }

/**
*	Sets the receiver field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setReceiver(String val) {
            String tmp=getReceiver();
            setParameter("receiver",val);
            pcs.firePropertyChange("receiver", tmp, val);
    }

/**
*	Sets the performative field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setPerformative(String val) {
            String tmp=getPerformative();
            setParameter("performative",val);
            pcs.firePropertyChange("performative", tmp, val);
    }

/**
*	Sets the ontology field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setOntology(String val) {
            String tmp=getOntology();
            setParameter("ontology",val);
            pcs.firePropertyChange("ontology", tmp, val);
    }

/**
*	Sets the language field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setLanguage(String val) {
            String tmp=getLanguage();
            setParameter("language",val);
            pcs.firePropertyChange("language", tmp, val);
    }
    
/**
*	Sets the content field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setContent(String val) {
            String tmp=getContent();
            setParameter("content",val);
            pcs.firePropertyChange("content", tmp, val);
    }
/**
*	Sets the reply-with field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setReplyWith(String val) {
        String tmp=getReplyWith();
        setParameter("reply-with",val);
        pcs.firePropertyChange("replyWith", tmp, val);
    }
    
/**
*	Sets the in-reply-to field of the message and notifies any observers of the change.
*	@param val the value to set the field to.
*/
    public synchronized void setInReplyTo(String val) {
        String tmp=getInReplyTo();
        setParameter("in-reply-to",val);
        pcs.firePropertyChange("inReplyTo", tmp, val);
    }
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
/**
*	Allows observers to add a property change listener to the object, necessary for
*	jess "shadow fact" support.
*	@param pcl the ProptertyChangeListener to add.
*/
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    
/**
*	Allows observers to remove a property change listener from the object, necessary for
*	jess "shadow fact" support.
*	@param pcl the ProptertyChangeListener to remove.
*/
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
}