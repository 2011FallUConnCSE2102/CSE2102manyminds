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
import java.util.Set;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import manyminds.debug.Level;
import manyminds.debug.Logger;

/**
 * @author eric
 *
 * This class attempts to implement javax.swing.TableModel using a collection of ManyMindsDocuments.
 * Each row is stored as a DataList, and the TableModel maintains an ArrayList of rows.  
 *
 */
public class
DataTable
extends AbstractTableModel
implements TableModel, ListDataListener {

    private DataServer myDataSource;
    private String myName;
    private static Logger logger = Logger.getLogger("manyminds");
    private DataList myData;  //DataList of DataLists
    private DataList headerRow;
    private ArrayList myDataLists;
    
    /**
     * 
     * @return
     */
    public Collection
    getDocumentNames() {
        Set s = new HashSet();
        s.add(myName);
        s.addAll(headerRow.getDocumentNames());
        Iterator it = myDataLists.iterator();
        while (it.hasNext()) {
            s.addAll(((DataList)it.next()).getDocumentNames());
        }
        return s;
    }
    
    public DataTable(String name, DataServer dc, int rows, int cols) {
        myDataSource = dc;
        myName = name;
        myData = new DataList(name,dc);
        myDataLists = new ArrayList();
        StringBuffer rowString = new StringBuffer();
        for (int j = 0; j < cols; ++j) {
            String t = getDataSource().addUniqueID(getName(),ManyMindsDocument.newDocument("Column "+Integer.toString(j+1)));
            getDataSource().getData(t).setValue("Column "+Integer.toString(j+1));
            rowString.append("("+t+") ");
        }
        String headerName = getDataSource().addUniqueID(getName()+"-header-group",ManyMindsDocument.newDocument(rowString.toString()));
        getDataSource().getData(headerName).setValue(rowString.toString());
        myData.add(headerName);
        headerRow = new DataList(headerName,getDataSource());
        headerRow.addListDataListener(this);
        for (int y = 0; y < rows; ++y) {
            rowString = new StringBuffer();
            for (int j = 0; j < cols; ++j) {
                String t = getDataSource().addUniqueID(getName(),ManyMindsDocument.newDocument("foo"));
                getDataSource().getData(t).setValue("");
                rowString.append("("+t+") ");
            }
            String s = getDataSource().addUniqueID(getName()+"-row-group",ManyMindsDocument.newDocument(rowString.toString()));
            getDataSource().getData(s).setValue(rowString.toString());
            myDataLists.add(new DataList(s,getDataSource()));
            myData.add(s);
        }
        myData.addListDataListener(this);
    }
    public
    DataTable(String name, DataServer dc) {
        myDataSource = dc;
        myName = name;
        myData = new DataList(name,dc);
        myDataLists = new ArrayList();
        myData.addListDataListener(this);
        rebuildTable();
    }
    
    protected void
    rebuildTable() {
        ArrayList newList = new ArrayList();
        if (myData.getSize() > 0) {
            if (headerRow != null) {
                headerRow.removeListDataListener(this);
            }
            headerRow = new DataList(myData.getElementNameAt(0),getDataSource());
        } else {
            String headerName = getDataSource().addUniqueID(getName()+"-header-row",ManyMindsDocument.newDocument());
            myData.add(headerName);
            headerRow = new DataList(headerName,getDataSource());
        }
        headerRow.addListDataListener(this);
        for (int i = 1; i < myData.getSize(); ++i) {
            if (((i - 1) < myDataLists.size()) 
                && (myData.getElementNameAt(i).equals(((DataList)myDataLists.get(i - 1)).getName()))) {
                newList.add(myDataLists.get(i - 1));
            } else {
                DataList pdl = new DataList(myData.getElementNameAt(i),getDataSource());
                pdl.addListDataListener(this);
                newList.add(pdl);
            }
        }
        Iterator it = myDataLists.iterator();
        while (it.hasNext()) {
            DataList pdl = (DataList)it.next();
            if (!newList.contains(pdl)) {
                pdl.removeListDataListener(this);
            }
        }
        myDataLists.clear();
        myDataLists = newList;
      //  fireTableDataChanged(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
        fireTableStructureChanged();
    }

    public void
    contentsChanged(ListDataEvent lde) {
        if (lde.getSource() == myData) {
        } else if (lde.getIndex0() == -1) {
            rebuildTable();
        } else if (lde.getSource() == headerRow) {
            fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
//            fireTableDataChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        } else {
            int i = myData.indexOfName(((DataList)lde.getSource()).getName());
            if (lde.getIndex0() == lde.getIndex1()) {
                int j = lde.getIndex0();
                fireTableChanged(new TableModelEvent(this,i,i,j));
//                fireTableDataChanged(new TableModelEvent(this,i,i,j));
            } else {
                fireTableChanged(new TableModelEvent(this,i));
//                fireTableDataChanged(new TableModelEvent(this,i));
            }
        }
    }

    public void
    intervalAdded(ListDataEvent lde) {
        if (lde.getSource() == myData) {
            int i = lde.getIndex0();
            int j = lde.getIndex1();
            for (int x = i; x <= j; ++x) {
                DataList pdl = new DataList(myData.getElementNameAt(i),getDataSource());
                pdl.addListDataListener(this);
                if (x > myDataLists.size()) {
                    myDataLists.add(pdl);
                } else {
                    myDataLists.add(x,pdl);
                }
            }
            fireTableRowsInserted(i - 1, j - 1);
//            fireTableDataChanged(new TableModelEvent(this,i,j,TableModelEvent.ALL_COLUMNS,TableModelEvent.INSERT));
        } else if (lde.getSource() == headerRow) {
            fireTableStructureChanged();
//            fireTableDataChanged(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
        }
    }
    
    public void
    intervalRemoved(ListDataEvent lde) {
        if (lde.getSource() == myData) {
            int i = lde.getIndex0();
            int j = lde.getIndex1();
            for (int x = i; x <= j; ++x) {
                if (x == 0) {
                    //headerRow.removeListDataListener(this);
                } else {
                    ((DataList)myDataLists.get(x-1)).removeListDataListener(this);
                    myDataLists.remove(x-1);
                }
            }
            //fireTableDataChanged(new TableModelEvent(this, i - 1, j - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
            fireTableRowsDeleted(i-1,j-1);
        } else if (lde.getSource() == headerRow) {
            fireTableStructureChanged();
            //fireTableDataChanged(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
        }
    }

    public Class
    getColumnClass(int columnIndex) {
        return String.class;
    }
    
    public int
    getColumnCount() {
        synchronized (headerRow) {
            return headerRow.getSize();
        }
    }
    
    public String
    getColumnName(int columnIndex) {
        return ((Data)headerRow.getElementAt(columnIndex)).getValue();
    }
    
    public int
    getRowCount() {
        return myData.getSize() - 1;
    }
    
    public boolean
    isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    
    public Object
    getValueAt(int rowIndex, int columnIndex) {
        try {
            synchronized (headerRow) {
                DataList pdl = (DataList)myDataLists.get(rowIndex);
                Data pd = (Data)pdl.getElementAt(columnIndex);
                return pd.getValue();
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Error looking for "+rowIndex+", "+columnIndex,t);
            return "";
        }
    }
    
    public void
    setValueAt(Object aValue, int rowIndex, int columnIndex) {
        synchronized (headerRow) {
            ((Data)((DataList)myDataLists.get(rowIndex)).getElementAt(columnIndex)).setValue(aValue.toString());
        }
    }
    
    public void
    addRow(String s) {
        synchronized (headerRow) {
            myData.add(s);
        }
    }
    
    public void
    addRow(int i) {
        synchronized (headerRow) {
            StringBuffer rowString = new StringBuffer();
            for (int j = 0; j < getColumnCount(); ++j) {
                String t = getDataSource().addUniqueID(getName(),ManyMindsDocument.newDocument());
                rowString.append("("+t+") ");
            }
            String s = getDataSource().addUniqueID(getName()+"-row-group",ManyMindsDocument.newDocument(rowString.toString()));
            getDataSource().getData(s).setValue(rowString.toString());
            myData.add(i+1,s);
        }
    }
    
    public void
    addColumn(int i) {
        synchronized (headerRow) {
            for (int j = getRowCount() - 1; j >= 0; --j) {
                String t = getDataSource().addUniqueID(getName(),ManyMindsDocument.newDocument());
                ((DataList)myDataLists.get(j)).add(i,t);
            }
            headerRow.add(i, getDataSource().addUniqueID(getName()+"-header",ManyMindsDocument.newDocument("Column "+Integer.toString(i))));
            getDataSource().getData(headerRow.getElementNameAt(i)).setValue("Column "+Integer.toString(i));
        }
    }
    
    public void
    removeRow(int i) {
        synchronized (headerRow) {
            myData.remove(i+1);
        }
    }
    
    public void
    removeColumn(int i) {
        synchronized (headerRow) {
            for (int j = getRowCount() - 1; j >= 0; --j) {
                ((DataList)myDataLists.get(j)).remove(i);
            }
            headerRow.remove(i);
        }
    }
    
    public void
    setHeaderName(int i, String s) {
        ((Data)headerRow.getElementAt(i)).setValue(s);
    }
    
    public DataServer
    getDataSource() {
        return myDataSource;
    }
    
    public String
    getName() {
        return myName;
    }

}