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

// How do we synchronize Global names and values across app/applet boundary?

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.DataList;
import manyminds.datamodel.DataListener;
import manyminds.debug.Level;

public class
Global
extends KnowledgeObject
implements DataListener, ListDataListener {
    
    private static class UnsetClass extends Object {}
    
    public static final Object UNSET = new UnsetClass();
    
    public static final int DOCUMENT = 1;
    public static final int RSA = 2;
    public static final int CONTEXT = 4;
    
    private int myType;
    private boolean isGroup = false;
    private String myPrototype = "";
    private DataList myList = null;
    private List myListElements;
    
    public Global(String i)  {
        super(null,null,null,null);
        setIndex(i);
    }

    public Global(String u, String i, String d) {
        super(u,null,d,null);
        setIndex(i);
        //setValue(UNSET);
    }

    public Global() {
        super(null,null,null,null);
        setValue(UNSET);
    }
    
    public void
    setValue(Object o) {
        super.setValue(o);
    }
    
    public int
    getType() {
        return myType;
    }
    
    public void
    setGroup(boolean b) {
        isGroup = b;
    }
    
    public boolean
    isGroup() {
        return isGroup;
    }
    
    public String
    getDetail() {
        if (isGroup) {
            if (getType() == DOCUMENT) {
                return "any of the artifact documents "+getIndex();
            } else if (getType() == RSA) {
                return "any of the sliders "+getIndex();
            } else if (getType() == CONTEXT) {
                return "any of the context variables "+getIndex();
            } else {
                return getIndex();
            }
        } else {
            if (getType() == DOCUMENT) {
                return "the artifact document "+getIndex();
            } else if (getType() == RSA) {
                return "the slider "+getIndex();
            } else if (getType() == CONTEXT) {
                return "the context variable "+getIndex();
            } else {
                return getIndex();
            }
        }
    }
    
    public void
    setType(int i) {
        myType = i;
    }
    
    public void
    setIndex(String s) {
        if (getIndex() != UNSET_INDEX) {
            if (myList != null) {
                myList.removeListDataListener(this);
                Iterator it = myListElements.iterator();
                while (it.hasNext()) {
                    ((Data)it.next()).removeDataListener(this);
                }
                myListElements = null;
                Data pd = null;
                if (getType() == Global.DOCUMENT) {
                    pd = DataContext.getContext().getSharedData().getData(getIndex());
                } else if (getType() == Global.CONTEXT) {
                    pd = DataContext.getContext().getGlobalVariables().getData(getIndex());
                } else if (getType() == Global.RSA) {
                    pd = DataContext.getContext().getSharedData().getData(getIndex());
                }
                if (pd != null) {
                    pd.removeDataListener(this);
                }
            }
        }
        super.setIndex(s);
        if (s != UNSET_INDEX) {
            if (isGroup) {
                if (getType() == RSA) {
                    myList = new DataList(s,DataContext.getContext().getSharedData());
                } else if (getType() == DOCUMENT) {
                    myList = new DataList(s,DataContext.getContext().getSharedData());
                } else if (getType() == CONTEXT) {
                    myList = new DataList(s,DataContext.getContext().getGlobalVariables());
                }
                myListElements = new LinkedList();
                Data d = null;
                for (int i = 0; i < myList.getSize(); ++i) {
                    d = null;
                    if (getType() == Global.DOCUMENT) {
                        d = DataContext.getContext().getSharedData().getData(myList.getElementNameAt(i));
                    } else if (getType() == Global.CONTEXT) {
                        d = DataContext.getContext().getGlobalVariables().getData(myList.getElementNameAt(i));
                    } else if (getType() == Global.RSA) {
                        d = DataContext.getContext().getSharedData().getData(myList.getElementNameAt(i));
                    }
                    if (d != null) {
                        d.addDataListener(this);
                        myListElements.add(d);
                    }
                }
                if (d != null) {
                    setValue(d.getValue());
                }
                myList.addListDataListener(this);
            } else {
                Data pd = DataContext.getContext().getSharedData().getData(getIndex());
                if (pd == null ) {
                    pd = DataContext.getContext().getGlobalVariables().getData(getIndex());
                }
                if (pd != null) {
                    pd.addDataListener(this);
                    setValue(pd.getValue());
                }
            }
        }
    }
    
    public void
    valueChanged(ChangeEvent ce) {
        setValue(((Data)ce.getSource()).getValue());
    }

    public void
    contentsChanged(ListDataEvent e) {
    }

    public void
    intervalAdded(ListDataEvent e) {
        for (int x = e.getIndex0(); x <= e.getIndex1(); ++x) {
            Data pd = null;
            if (getType() == Global.DOCUMENT) {
                pd = DataContext.getContext().getSharedData().getData(myList.getElementNameAt(x));
            } else if (getType() == Global.CONTEXT) {
                pd = DataContext.getContext().getGlobalVariables().getData(myList.getElementNameAt(x));
            } else if (getType() == Global.RSA) {
                pd = DataContext.getContext().getSharedData().getData(myList.getElementNameAt(x));
            }
            if (pd != null) {
                pd.addDataListener(this);
                myListElements.add(x,pd);
                setValue(pd.getValue());
            } else {
                System.err.println("Couldn't find "+myList.getElementNameAt(x));
            }
        }
    }
        
    
    public String
    getTypeString() {
        return myPrototype;
    }
    
    public void
    setPrototype(String s) {
        myPrototype = s;
    }

    public void
    intervalRemoved(ListDataEvent e) {
        for (int i = e.getIndex1(); i >= e.getIndex0(); --i) {
            try {
                Data d = (Data)myListElements.remove(i);
                d.removeDataListener(this);
            } catch (Throwable t) {
                logger.log(Level.WARNING,"Error removing list element",t);
            }
        }
    }

    private String
    typeString() {
        if (getType() == RSA) {
            return "RSA";
        } else if (getType() == DOCUMENT) {
            return "DOCUMENT";
        } else {
            return "CONTEXT";
        }
    }

    public String
    toString() {
        return getIndex();
    }

    public String
    toXML() {
        return "<global type=\""+typeString()+"\" group=\""+String.valueOf(isGroup)
                +"\" prototype=\""+getTypeString()+"\">\n<index>"+getIndex()+"</index>\n<detail>"
                +getDetail()+"</detail>\n<url>"+getURL()+"</url>\n</global>";
    }
}