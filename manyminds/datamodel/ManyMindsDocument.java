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
 package manyminds.datamodel;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;

import manyminds.debug.Level;
import manyminds.debug.Logger;

public class
ManyMindsDocument
extends AbstractData
implements Document, Serializable, Data, RemoteData {

    private static Logger logger = Logger.getLogger("manyminds");
    private Document myDocument;
    private String myTypeString = "";

    private EventListenerList myDocumentListeners = new EventListenerList();
    
/*    private class
    WrappedDocumentEvent
    implements DocumentEvent {
        private DocumentEvent myDE;
        
        public
        WrappedDocumentEvent(DocumentEvent de) {
            myDE = de;
        }
                
        public Object
        getSource() {
            return ManyMindsDocument.this;
        }
        
        public int getOffset() {
            return myDE.getOffset();
        }
    
        public int getLength() {
            return myDE.getLength();
        }
    
        public Document getDocument() {
            return ManyMindsDocument.this;
        }
        
        public EventType getType() {
            return myDE.getType();
        }
        
        public ElementChange getChange(Element elem) {
            return myDE.getChange(elem);
        }
        
    }*/

    public void
    setValue(String t) {
        setText(t);
    }
    
    public void
    setTypeString(String t) {
        myTypeString = t;
    }
    
    public String
    getValue() {
        return getText();
    }

    public
    ManyMindsDocument(String s) 
    throws RemoteException {
        super();
        myDocument = new PlainDocument();
        myDocument.addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent de) {
                fireValueChanged();
            }
            
            public void insertUpdate(DocumentEvent de) {
                fireValueChanged();
            }
            
            public void removeUpdate(DocumentEvent de) {
                fireValueChanged();
            }
        });
        
        setValue(s);
    }
    
    public
    ManyMindsDocument()
    throws RemoteException {
        this("");
    }
    
    public static ManyMindsDocument
    newDocument() {
        try {
            return new ManyMindsDocument();
        } catch (RemoteException re) {
            logger.log(Level.WARNING,"Error creating manyminds document",re);
            return null;
        }
    }
    
    public static ManyMindsDocument
    newDocument(String s) {
        try {
            return new ManyMindsDocument(s);
        } catch (RemoteException re) {
            logger.log(Level.WARNING,"Error creating manyminds document",re);
            return null;
        }
    }
    
    public void
    setText(String newText) {
        try {
            if (!("".equals(newText))) {
                valueChangeHalt = true;
                remove(0,getLength());
                valueChangeHalt = false;
                insertString(0,newText,null);
            } else {
                remove(0,getLength());
            }
        } catch (BadLocationException ble) {
            logger.log(Level.WARNING,"Error setting text on document",ble);
        }
    }// setText
    
    public void
    addText(String newText) {
        try {
            insertString(getLength(),newText,null);
        } catch (BadLocationException ble) {
            logger.log(Level.WARNING,"Error setting text on document",ble);
        }
    }
    
    public  boolean
    reset() {
        setValue(getTypeString());
        return true;
    }
    
    public int
    getType() {
        return DOCUMENT;
    }
    
    public String
    getTypeString() {
        return "";
    }

    public String
    getText() {
        try {
            return getText(0,getLength());
        } catch (BadLocationException ble) {
            logger.log(Level.WARNING,"Error getting text on document",ble);
            return null;
        }
    }// getText
    
/*    protected void fireInsertUpdate(DocumentEvent e) {
	Object[] listeners = myListeners.getListenerList();
        //e = new WrappedDocumentEvent(e);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==DocumentListener.class) {
		((DocumentListener)listeners[i+1]).insertUpdate(e);
	    }	       
	}
    }

    protected void fireChangedUpdate(DocumentEvent e) {
        //e = new WrappedDocumentEvent(e);
	Object[] listeners = myListeners.getListenerList();
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==DocumentListener.class) {
		((DocumentListener)listeners[i+1]).changedUpdate(e);
	    }	       
	}
    }

    protected void fireRemoveUpdate(DocumentEvent e) {
        //e = new WrappedDocumentEvent(e);
	Object[] listeners = myListeners.getListenerList();
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==DocumentListener.class) {
		((DocumentListener)listeners[i+1]).removeUpdate(e);
	    }	       
	}
    }

    protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        e = new UndoableEditEvent(ManyMindsDocument.this,e.getEdit());
	Object[] listeners = myListeners.getListenerList();
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==UndoableEditListener.class) {
		((UndoableEditListener)listeners[i+1]).undoableEditHappened(e);
	    }	       
	}
    }
*/

    public int
    getLength() {
        return myDocument.getLength();
    }

    public void
    addDocumentListener(DocumentListener listener)  {
        //myListeners.add(listener.getClass(),listener);
        myDocument.addDocumentListener(listener);
    }

    public void
    removeDocumentListener(DocumentListener listener) {
        //myListeners.remove(listener.getClass(),listener);
        myDocument.removeDocumentListener(listener);
    }

    public void
    addUndoableEditListener(UndoableEditListener listener) {
        myDocument.addUndoableEditListener(listener);
    }

    public void
    removeUndoableEditListener(UndoableEditListener listener) {
        myDocument.removeUndoableEditListener(listener);
    }

    public Object
    getProperty(Object key) {
        return myDocument.getProperty(key);
    }

    public void
    putProperty(Object key, Object value) {
        myDocument.putProperty(key, value);
    }

    public void
    remove(int offs, int len)
    throws BadLocationException {
        myDocument.remove(offs, len);
    }

    public void
    insertString(int offset, String str, AttributeSet a)
    throws BadLocationException  {
        myDocument.insertString(offset, str, a);
    }

    public String
    getText(int offset, int length)
    throws BadLocationException  {
        return myDocument.getText(offset, length);
    }

    public void
    getText(int offset, int length, Segment txt)
    throws BadLocationException  {
        myDocument.getText(offset, length, txt);
    }

    public Position
    getStartPosition() {
        return myDocument.getStartPosition();
    }
    
    public Position
    getEndPosition() {
        return myDocument.getEndPosition();
    }

    public Position
    createPosition(int offs)
    throws BadLocationException {
        return myDocument.createPosition(offs);
    }

    public Element[]
    getRootElements() {
        return myDocument.getRootElements();
    }

    public Element
    getDefaultRootElement() {
        return myDocument.getDefaultRootElement();
    }

    public void
    render(Runnable r) {
        myDocument.render(r);
    }
}