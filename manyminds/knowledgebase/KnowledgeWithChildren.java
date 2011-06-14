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


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

public abstract class
KnowledgeWithChildren
extends KnowledgeObject
implements KnowledgeListener, TreeModelListener {
    protected List myChildren;
    protected List myTreeListeners;

    public KnowledgeWithChildren() {
        myChildren = new LinkedList();
        myTreeListeners = new LinkedList();
    }
    
    public
    KnowledgeWithChildren(String u,String i,String d,String r) {
        super(u,i,d,r);
        myChildren = new LinkedList();
        myTreeListeners = new LinkedList();
    }
    
    public String
    toXML() {
        Iterator it = myChildren.iterator();
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            sb.append(((KnowledgeObject)it.next()).toXML());
        }
        return sb.toString();
    }
    
    public Collection
    getLeaves() {
        List l = new LinkedList();
        addYourLeaves(l);
        return Collections.unmodifiableList(l);
    }
    
    private void
    addYourLeaves(Collection c) {
        Iterator it = myChildren.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof KnowledgeWithChildren) {
                ((KnowledgeWithChildren)o).addYourLeaves(c);
            } else {
                c.add(o);
            }
        }
    }
    
    public int
    getIndexOfChild(Object child) {
        return myChildren.indexOf(child);
    }
    
    public Object
    getChild(int index) {
        if (index < myChildren.size()) {
            return myChildren.get(index);
        } else {
            return null;
        }
    }
    
    public void
    addChild(KnowledgeObject ko) {
        if (!myChildren.contains(ko)) {
            ko.addKnowledgeListener(this);
            if (ko instanceof KnowledgeWithChildren) {
               // ((KnowledgeWithChildren)ko).addTreeModelListener(this);
            }
            myChildren.add(ko);
            //fireChildAdded(myChildren.size() - 1);
        }
    }
    
    public boolean
    removeChild(KnowledgeObject ko) {
        int i = getIndexOfChild(ko);
        if (i != -1) {
            myChildren.remove(i);
            fireChildDeleted(i);
            return true;
        } else {
            return false;
        }
    }
    
    protected void
    fireChildModified(int index) {
        TreeModelEvent tme = null;
        Iterator it = myTreeListeners.iterator();
        while (it.hasNext()) {
            if (tme == null) {
                Object path[] = new Object[1];
                int indices[] = new int[1];
                indices[0] = index;
                path[0]=this;
                tme = new TreeModelEvent(this,path,indices,myChildren.toArray());
            }
            ((TreeModelListener)it.next()).treeNodesChanged(tme);
        }
    }
        
    protected void
    fireChildDeleted(int index) {
        TreeModelEvent tme = null;
        Iterator it = myTreeListeners.iterator();
        while (it.hasNext()) {
            if (tme == null) {
                Object path[] = new Object[1];
                int indices[] = new int[1];
                indices[0] = index;
                path[0]=this;
                tme = new TreeModelEvent(this,path,indices,myChildren.toArray());
            }
            ((TreeModelListener)it.next()).treeNodesRemoved(tme);
        }
    }
    
    protected void
    fireChildAdded(int index) {
        TreeModelEvent tme = null;
        Iterator it = myTreeListeners.iterator();
        while (it.hasNext()) {
            if (tme == null) {
                Object path[] = new Object[1];
                int indices[] = new int[1];
                Object children[] = new Object[1];
                children[0] = myChildren.get(index);
                indices[0] = index;
                path[0]=this;
                tme = new TreeModelEvent(this,path,indices,children);
            }
            ((TreeModelListener)it.next()).treeNodesInserted(tme);
        }
    }
    
    protected void
    fireTreeStructureChanged() {
        TreeModelEvent tme = null;
        Iterator it = myTreeListeners.iterator();
        while (it.hasNext()) {
            if (tme == null) {
                Object path[] = new Object[1];
                path[0]=this;
                tme = new TreeModelEvent(this,path);
            }
            ((TreeModelListener)it.next()).treeStructureChanged(tme);
        }
    }
    
    public void
    detailChanged(KnowledgeEvent ke) {}
    
    public void
    URLChanged(KnowledgeEvent ke) {}
    
    public void
    referenceChanged(KnowledgeEvent ke) {}
    
    public void
    referentChanged(KnowledgeEvent ke) {}
    
    public void
    valueChanged(KnowledgeEvent ke) {}

    public void
    addTreeModelListener(TreeModelListener tml) {
        myTreeListeners.add(tml);
    }
    
    public void
    removeTreeModelListener(TreeModelListener tml) {
        myTreeListeners.remove(tml);
    }
    
    public void treeNodesChanged(TreeModelEvent e) {
        TreePath tp = e.getTreePath();
        Object[] oar = new Object[tp.getPathCount()+1];
        oar[0] = this;
        for (int i = 0; i < tp.getPathCount(); ++i) {
            oar[i+1] = tp.getPathComponent(i);
        }
        TreeModelEvent tme = new TreeModelEvent(e.getSource(),oar,e.getChildIndices(),e.getChildren());
        fireTreeNodesChanged(tme);
    }

    protected void
    fireTreeNodesChanged(TreeModelEvent tme) {
        Iterator it = myTreeListeners.iterator();
        while (it.hasNext()) {
            ((TreeModelListener)it.next()).treeNodesChanged(tme);
        }
    }

    public void treeNodesInserted(TreeModelEvent e) {
        TreePath tp = e.getTreePath();
        Object[] oar = new Object[tp.getPathCount()+1];
        oar[0] = this;
        for (int i = 0; i < tp.getPathCount(); ++i) {
            oar[i+1] = tp.getPathComponent(i);
        }   
        TreeModelEvent tme = new TreeModelEvent(e.getSource(),oar,e.getChildIndices(),e.getChildren());
        fireTreeNodesInserted(tme);
    }

    protected void
    fireTreeNodesInserted(TreeModelEvent tme) {
        Iterator it = myTreeListeners.iterator();
        while (it.hasNext()) {
            ((TreeModelListener)it.next()).treeNodesInserted(tme);
        }
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        TreePath tp = e.getTreePath();
        Object[] oar = new Object[tp.getPathCount()+1];
        oar[0] = this;
        for (int i = 0; i < tp.getPathCount(); ++i) {
            oar[i+1] = tp.getPathComponent(i);
        }
        TreeModelEvent tme = new TreeModelEvent(e.getSource(),oar,e.getChildIndices(),e.getChildren());
        fireTreeNodesRemoved(tme);
    }

    protected void
    fireTreeNodesRemoved(TreeModelEvent tme) {
        Iterator it = myTreeListeners.iterator();
        while (it.hasNext()) {
            ((TreeModelListener)it.next()).treeNodesRemoved(tme);
        }
    }
    
    public boolean
    isAppropriate(KnowledgeObject ko) {
        return true;
    }

    public void treeStructureChanged(TreeModelEvent e) {
        TreePath tp = e.getTreePath();
        Object[] oar = new Object[tp.getPathCount()+1];
        oar[0] = this;
        for (int i = 0; i < tp.getPathCount(); ++i) {
            oar[i+1] = tp.getPathComponent(i);
        }
        TreeModelEvent tme = new TreeModelEvent(e.getSource(),oar);
        fireTreeStructureChanged(tme);
    }
    
    protected void
    fireTreeStructureChanged(TreeModelEvent tme) {
        Iterator it = myTreeListeners.iterator();
        while (it.hasNext()) {
            ((TreeModelListener)it.next()).treeStructureChanged(tme);
        }
    }

    public Object getRoot() {
        return this;
    }
    
    public Object getChild(Object parent, int index) {
        if (this == parent) {
            return getChild(index);
        } else if (parent instanceof KnowledgeWithChildren) {
            return ((KnowledgeWithChildren)parent).getChild(index);
        } else {
            return null;
        }
    }
    
    public int getChildCount() {
        return myChildren.size();
    }

    public int getChildCount(Object parent) {
        if (this == parent) {
            return getChildCount();
        } else if (parent instanceof KnowledgeWithChildren) {
            return ((KnowledgeWithChildren)parent).getChildCount(parent);
        } else {
            return 0;
        }
    }
    
    public boolean isLeaf(Object node) {
        if (node instanceof KnowledgeWithChildren) {
            return false;
        } else {
            return true;
        }
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {}
    public int getIndexOfChild(Object parent, Object child) {
        if (this == parent) {
            return myChildren.indexOf(child);
        } else if (parent instanceof KnowledgeWithChildren) {
            return ((KnowledgeWithChildren)parent).getIndexOfChild(parent,child);
        } else {
            return -1;
        }
    }
}
    
