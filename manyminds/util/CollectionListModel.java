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

package manyminds.util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;

import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class
CollectionListModel
extends ArrayList 
implements ListModel {

    public
    CollectionListModel() {
        super();
    }
    
    public
    CollectionListModel(Collection c) {
        super(c);
    }
    
    public
    CollectionListModel(ListModel m) {
        super();
        for (int i = 0; i < m.getSize(); ++i) {
            add(m.getElementAt(i));
        }
    }
    
    public Object
    getElementAt(int i) {
        return get(i);
    }
    
    public int
    getSize() {
        return size();
    }
    
    public void
    add(int i, Object o) {
        super.add(i,o);
        fireIntervalAdded(i,i);
    }
    
    public boolean
    add(Object o) {
        int i = getSize();
        if (super.add(o)) {
            fireIntervalAdded(i,i);
            return true;
        } else {
            return false;
        }
    }
    
    public void
    clear() {
        int i = getSize() - 1;
        super.clear();
        fireIntervalRemoved(0,i);
    }
    
    public Object
    set(int i, Object o) {
        Object r = super.set(i,o);
        fireContentsChanged(i,i);
        return r;
    }
    
    public Object
    remove(int i) {
        Object o = super.remove(i);
        fireIntervalRemoved(i,i);
        return o;
    }
    
    public boolean
    remove(Object o) {
        int i = indexOf(o);
        if (i == -1) {
            return false;
        } else {
            remove(i);
            return true;
        }
    }
    
    
    protected EventListenerList listenerList = new EventListenerList();

    public void addListDataListener(ListDataListener l) {
	listenerList.add(ListDataListener.class, l);
    }

    public void removeListDataListener(ListDataListener l) {
	listenerList.remove(ListDataListener.class, l);
    }

    public void fireContentsChanged(int index0, int index1)
    {
	Object[] listeners = listenerList.getListenerList();
	ListDataEvent e = null;

	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ListDataListener.class) {
		if (e == null) {
		    e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index0, index1);
		}
		((ListDataListener)listeners[i+1]).contentsChanged(e);
	    }	       
	}
    }

    protected void fireIntervalAdded(int index0, int index1)
    {
	Object[] listeners = listenerList.getListenerList();
	ListDataEvent e = null;

	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ListDataListener.class) {
		if (e == null) {
		    e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index0, index1);
		}
		((ListDataListener)listeners[i+1]).intervalAdded(e);
	    }	       
	}
    }

    protected void fireIntervalRemoved(int index0, int index1)
    {
	Object[] listeners = listenerList.getListenerList();
	ListDataEvent e = null;

	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ListDataListener.class) {
		if (e == null) {
		    e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index0, index1);
		}
		((ListDataListener)listeners[i+1]).intervalRemoved(e);
	    }	       
	}
    }

    public EventListener[] getListeners(Class listenerType) { 
	return listenerList.getListeners(listenerType); 
    }
}
