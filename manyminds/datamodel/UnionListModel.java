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
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class UnionListModel extends AbstractListModel implements ListDataListener {

    private List _models;
    private int _lastIndexSearch = -1;
    private int _lastOffsetResult = -1;
    private ListModel _lastModelResult;
    
    public UnionListModel(ListModel[] m) {
        _models = new ArrayList(Arrays.asList(m));
    }
    
    public UnionListModel() {
        _models = new ArrayList();
    }

    
    private ListModel indexToModel(int index) {
        if (index != _lastIndexSearch) {
            _lastIndexSearch = index;
            for (int x = 0; x < _models.size(); ++x) {
                int newindex = index - ((ListModel)_models.get(x)).getSize();
                if (newindex < 0) {
                    _lastOffsetResult = index;
                    _lastModelResult = (ListModel)_models.get(x);
                    return (ListModel)_models.get(x);
                } else {
                    index = newindex;
                } 
            }
            _lastModelResult = null;
            _lastOffsetResult = -1;
            return null;
        } else {
            return _lastModelResult;
        }
    }
    
    private int indexToOffset(int index) {
        if (index != _lastIndexSearch) {
            _lastIndexSearch = index;
            for (int x = 0; x < _models.size(); ++x) {
                int newindex = index - ((ListModel)_models.get(x)).getSize();
                if (newindex < 0) {
                    _lastOffsetResult = index;
                    _lastModelResult = (ListModel)_models.get(x);
                    return index;
                } else {
                    index = newindex;
                } 
            }
            _lastModelResult = null;
            _lastOffsetResult = -1;
            return -1;
        } else {
            return _lastOffsetResult;
        }
    }
    
    private int modelAndOffsetToIndex(ListModel lm, int offset) {
        if (offset != -1) {
            int ind = _models.indexOf(lm);
            if (ind != -1) {
                int ret_val = 0;
                for (int x = 0; x < ind; ++x) {
                    ret_val += ((ListModel)_models.get(x)).getSize();
                }
                return ret_val + offset;
            }
        }
        return -1;
    }
    
    public Object getElementAt(int index) {
        ListModel lm = indexToModel(index);
        if (lm != null) {
            int offset = indexToOffset(index);
            if (offset != -1) {
                return lm.getElementAt(offset);
            }
        }
        return null;
    }
    
    public int getSize() {
        int ret_val = 0;
        for (int x = 0; x < _models.size(); ++x) {
            ret_val += ((ListModel)_models.get(x)).getSize();
        }
        return ret_val;
    }
    
    public void addModel(ListModel m) {
        int oldsize = getSize();
        _models.add(m);
        m.addListDataListener(this);
        fireIntervalAdded(this, oldsize, oldsize+m.getSize()-1);
    }
    
    public void removeModel(ListModel m) {
        int index = _models.indexOf(m);
        if (index != -1) {
            int oldsize = getSize();
            _models.remove(index);
            m.removeListDataListener(this);
            fireIntervalRemoved(this, oldsize, getSize()-1);
        }
    }

    public void intervalAdded(ListDataEvent e) {
        int index0 = e.getIndex0();
        int index1 = e.getIndex1();
        ListModel lm = (ListModel)e.getSource();
        fireIntervalAdded(this, modelAndOffsetToIndex(lm,index0), modelAndOffsetToIndex(lm,index0));
    }

    public void intervalRemoved(ListDataEvent e) {
        int index0 = e.getIndex0();
        int index1 = e.getIndex1();
        ListModel lm = (ListModel)e.getSource();
        fireIntervalRemoved(this, modelAndOffsetToIndex(lm,index0), modelAndOffsetToIndex(lm,index0));
    }

    public void contentsChanged(ListDataEvent e) {
        int index0 = e.getIndex0();
        int index1 = e.getIndex1();
        ListModel lm = (ListModel)e.getSource();
        fireContentsChanged(this, modelAndOffsetToIndex(lm,index0), modelAndOffsetToIndex(lm,index0));
    }
}