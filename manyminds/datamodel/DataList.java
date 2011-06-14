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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;

import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.util.StringExploder;

/**
 * @author eric
 *
 * An implementation of ListModel that utilizes Documents as a backing store.  There is one document
 * called foo-group that stores the structure of the list in a parenthetical list like:<p>
 * (foo-0) (foo-1) (foo-2) ... (foo-n) <p>
 * Where each of the foo-i elements are names of Data objects with actual values.  
 *
 */
public class
DataList
extends AbstractListModel
implements ListModel, DataListener {

//    private EventListenerList myListeners = new EventListenerList();
    private List myData = new ArrayList();
    private DataServer myDataSource;
    private String myName;
    private boolean firing = false;
    private boolean updating = false;
    private Set toRemove = new HashSet();
    private static Logger logger = Logger.getLogger("manyminds");
    private static Logger dataLogger = Logger.getLogger("manyminds.datamodel");
    private String oldValue;

    private class
    MyDocumentListener
    implements
    DataListener {
    
        private String myDocumentName;
        private String oldValue = "";
    
        public MyDocumentListener(String s) {
            myDocumentName = s;
        }
        
        public void valueChanged(ChangeEvent ce) {
            if (!((Data)ce.getSource()).getValue().equals(oldValue)) {
                oldValue = ((Data)ce.getSource()).getValue();
                int i = myData.indexOf(myDocumentName);
                if (i >= 0) {
                    fireContentsChanged(DataList.this,i,i);
                } else {
                    ((Data)ce.getSource()).removeDataListener(this);
                }
            }
        }
    }
        

    /**
     * Creates a DataList on a given server.  If the foo-group document already exists, we will
     * create a list that is populated already (for example when the list is created from a save
     * file).  If that document doesn't exist, it will be created.
     * @param name the base name of the datalist.  Should end in -group for my lousy data typing assumptions, otherwise creating new elements won't automatically find their way into the list.
     * @param dc The dataserver that we're using to build the list.
     */
    public
    DataList(String name, DataServer dc) {
        myDataSource = dc;
        if (!name.endsWith("-group")) {
            name = name + "-group";
        } 
        myName = name;
        ManyMindsDocument cd = ManyMindsDocument.newDocument();
        cd = (ManyMindsDocument)myDataSource.addData(name,cd);
        cd.addDataListener(this);
        Iterator it = StringExploder.explode(cd.getText()).iterator();
        oldValue = cd.getText();
        while (it.hasNext()) {
            String s = StringExploder.stripParens(it.next().toString());
            getData(s).addDataListener(new MyDocumentListener(s));
            myData.add(s);
        }
        dataLogger.log(Level.FINEST,"Making DataList from "+name+ " : " +cd.getValue());
    }
    
    /**
     * Sometimes it is useful to know which Data elements we're looking at.
     * @return a Collection of all the documents in this list.  We don't watch it, so modifications to it don't get propagated.
     */
    public Collection
    getDocumentNames() {
        Set s = new HashSet();
        s.add(myName);
        s.addAll(myData);
        return s;
    }
    
    /**
     * A useful convenienve method that will create (if necessary) a Data object and return it.
     * @param s The name of the data object (should include the index, like foo-3)
     * @return The Data if it exists, if not and foo is a named rater prototype it returns a RaterModel, otherwise a Document
     */
    protected Data
    getData(String s) {
        Data ret_val = myDataSource.getData(s);
        if (ret_val == null) {
            int i = s.lastIndexOf("-");
            if ((i != -1) && (RaterModel.isPrototyped(s.substring(0,i)))) {
                ret_val = RaterModel.instantiateRaterModel(s.substring(0,i));
            } else {
                ret_val = ManyMindsDocument.newDocument();
            }
            ret_val = myDataSource.addData(s,ret_val);
        }
        return ret_val;
    }

/*    public void
    addListDataListener(ListDataListener ldl) {
        myListeners.add(ListDataListener.class, ldl);
    }*/
    
    public DataServer
    getDataSource() {
        return myDataSource;
    }
    
    public String
    getName() {
        return myName;
    }
    
    /**
     * Gets the index of a particular value in the list.
     * @param s The value to find.
     * @return the integer index of the value if it exists (using equals()), -1 otherwise.
     */
    public int
    indexOf(String s) {
        for (int i = 0; i < getSize(); ++i) {
            if (((Data)getElementAt(i)).getValue().equals(s)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Gets the index of the document name in the list.  Foo-2 is not guaranteed to be the 3rd element in the Foo list
     * for example, so this is useful.
     * @param s the name of the document to find (Foo-2)
     * @return The location of the document in the list, -1 if it isn't there.
     */
    public int
    indexOfName(String s) {
        return myData.indexOf(s);
    }
    
    public int
    getSize() {
        return myData.size();
    }
    
    /**
     * Looks up the document at i and gets its value.  Note that it will return the Data.toString() value, not the
     * Data itself.
     * @see javax.swing.ListModel#getElementAt(int)
     * @param i The index being looked up
     * @return The string value of the data at i
     */
    public Object
    getElementAt(int i) {
        return getData(myData.get(i).toString());
    }

    /**
     * Looks up the name of the document in list position i.  This is how one would get at the actual
     * Data object there, by using getDataSource().getData(getElementNameAt(i));
     * @param i the index to look up
     * @return the name of the Data at i
     */
    public String
    getElementNameAt(int i) {
        return myData.get(i).toString();
    }
    
    /**
     * Does the list contain the document with this name?
     * @param s the name being looked up
     * @return true if that document is in the list, false otherwise.
     */
    public boolean
    contains(String s) {
        return myData.contains(s);
    }
    
    /**
     * Adds a value to the end of the list to be watched.  Note that this just changes the control list,
     * it doesn't <b>not</b> add the appropriate document to the backing store.  So, usually you want
     * to use addData instead.  This is used when the list is being built from a save file and all the documents
     * are already there.
     * @param s the document name to be added to the control list.
     */
    public void
    add(String s) {
        add(myData.size(),s);
    }
    
    /**
     * Adds a value to the list to be watched at a particular index.  Note that this just changes the control list,
     * it doesn't <b>not</b> add the appropriate document to the backing store.  So, usually you want
     * to use addData instead.  This is used when the list is being built from a save file and all the documents
     * are already there.
     * @param s the document name to be added to the control list.
     * @param i the index to insert the document name at.
     */
    public void
    add(int i, String s) {
        if (updating) {
            myData.add(i,s);
        } else {
            getData(s).addDataListener(new MyDocumentListener(s));
            List l = StringExploder.explode(getData(myName).getValue());
            l.add(i,"("+s+")");
            oldValue = StringExploder.implodeString(l);
            getData(myName).setValue(oldValue);
            myData.add(i,s);
        }
        fireIntervalAdded(this,i,i);
    }
    
    /**
     * Adds the Data to the DataServer and inserts the Data's name into the control list.  Good for
     * inserting new data into the list as it grows (as opposed to building from a savefile).  This will not
     * overwite an existing Data in the DataServer if it is already there, rather it will return a reference to 
     * the Data that is in the Server, so you should work with the return value rather than the parameter.
     * @param i the index to insert at
     * @param s the name of the data for the DataServer
     * @param pd the Data object
     * @return a reference to the Data object that is in the Server (either just added or already existing)
     */
    public Data
    addData(int i, String s, Data pd) {
        pd = myDataSource.addData(s,pd);
        add(i,s);
        return pd;
    }
    
    /**
     * Inserts a data element to the end of the list.
     * @see #addData(int, String, Data)
     * @param s the name of the data for the DataServer
     * @param pd the Data object
     * @return a reference to the Data object that is in the Server (either just added or already existing)
     */
    public Data
    addData(String s, Data pd) {
        return addData(myData.size(),s,pd);
    }
    
    /**
     * Tries to remove the named document from the list.  This does not remove the Data object from the DataServer,
     * so it is possible to orphan Data objects in this way.  Probably bad design.
     * @param s the name of the document to remove.
     */
    public void
    remove(String s) {
        int i = myData.indexOf(s);
        myData.remove(i);
        List l = StringExploder.explode(getData(myName).getValue());
        l.remove(i);
        oldValue = StringExploder.implodeString(l);
        getData(myName).setValue(oldValue);
        fireIntervalRemoved(this,i,i);
    }
    
    
    /**
     * Removes the data at i.  As other forms of remove(), it doesn't delete the Data from the DataServer.
     * @param i the index of the data to be deleted.
     */
    public void
    remove(int i) {
        if (updating) {
            myData.remove(i);
        } else {
            List l = StringExploder.explode(getData(myName).getValue());
            l.remove(i);
            oldValue = StringExploder.implodeString(l);
            myData.remove(i);
            getData(myName).setValue(oldValue);
        }
        fireIntervalRemoved(this,i,i);
    }
    
    
    
    /**
     * 
     * Handles all sorts of changes and repackages them as ListEvents.
     * 
     * @see manyminds.datamodel.DataListener#valueChanged(javax.swing.event.ChangeEvent)
     */
    public void
    valueChanged(ChangeEvent ce) {
        if (!updating) {
            try {
                updating = true;
                String s = getData(myName).getValue();
                Object[] parms = {oldValue,s};
                dataLogger.log(Level.FINEST,"list "+myName+" caught valuechanged on its data");
                if (!s.equals(oldValue)) {
                    List newList = StringExploder.explode(s);
                    List oldList = StringExploder.explode(oldValue);
                    for (int i = 0; i < newList.size(); ++i) {
                        newList.set(i,StringExploder.stripParens(newList.get(i).toString()).trim()/*.toLowerCase()*/);
                    }
                    for (int i = 0; i < oldList.size(); ++i) {
                        oldList.set(i,StringExploder.stripParens(oldList.get(i).toString()).trim()/*.toLowerCase()*/);
                    }
                    for (int i = oldList.size() - 1; i >= 0 ; --i) {
                        if (!newList.contains(oldList.get(i))) {
                            remove(i);
                        }
                    }
                    for (int i = 0; i < newList.size(); ++i) {
                        if (!oldList.contains(newList.get(i))) {
                            add(i,newList.get(i).toString());
                        }
                    }
                    oldValue = s;
                }
            } catch (Throwable t) {
                dataLogger.log(Level.WARNING,"Error when dispatching data list event",t);
            } finally {
                updating = false;
            }
        }
    }
        
}