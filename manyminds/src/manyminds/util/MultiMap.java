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
 package manyminds.src.manyminds.util;

/*
 *
 *
 * Written by Eric Eslinger
 * Copyright © 1998-1999 University of California
 * All Rights Reserved.
 *
 */
 
import java.util.*;
 
public class
MultiMap
implements Map {
    
    private Map myBacking;
    
    public
    MultiMap() {
        myBacking = new HashMap();
    }
    
    public
    MultiMap(Map m) {
        myBacking = m;
    }
    
    public void
    clear() {
        myBacking.clear();
    }
    
    public boolean
    containsKey(Object key) {
        return myBacking.containsKey(key);
    }
    
    public boolean
    containsValue(Object value) {
        Iterator it = myBacking.values().iterator();
        while (it.hasNext()) {
            Iterator subit = ((List)it.next()).iterator();
            while (subit.hasNext()) {
                if (it.next().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Set
    entrySet() {
        return myBacking.entrySet();
    }
    
    public boolean
    equals(Object o) {
        if ((o instanceof MultiMap) && (((MultiMap)o).myBacking.equals(myBacking))) {
            return true;
        } else {
            return false;
        }
    }
    
    public Object
    get(Object key) {
        return get(key,0);
    }
    
    public int
    hashCode() {
        return myBacking.hashCode();
    }
    
    public boolean
    isEmpty() {
        return myBacking.isEmpty();
    }
    
    public Set
    keySet() {
        return myBacking.keySet();
    }
    
    public Object
    put(Object key, Object val) {
        Object o = myBacking.get(key);
        List l;
        if (o != null) {
            l = (List)o;
        } else {
            l = new LinkedList();
            myBacking.put(key,l);
        }
        l.add(val);
        return l;
    }
    
    public void
    putAll(Map t) {
        myBacking.putAll(t);
    }
    
    public Object
    remove(Object key) {
        return remove(key, 0);
    }
    
    public int
    size() {
        int s = 0;
        Iterator it = myBacking.values().iterator();
        while (it.hasNext()) {
            s += ((List)it.next()).size();
        }
        return s;
    }
    
    public Collection
    values() {
        return myBacking.values();
    }
 
    //Extra functionality
    
    public Object
    get(Object key, int at) {
        Object o = myBacking.get(key);
        if (o != null) {
            return ((List)o).get(at);
        } else {
            return null;
        }
    }
    
    public Object
    remove(Object key, int at) {
        Object o = myBacking.get(key);
        if (o != null) {
            return ((List)o).remove(at);
        } else {
            return null;
        }
    }
    
    public Collection
    getAll(Object key) {
        return (Collection)myBacking.get(key);
    }
    
    public void
    removeAll(Object key) {
        myBacking.remove(key);
    }
}