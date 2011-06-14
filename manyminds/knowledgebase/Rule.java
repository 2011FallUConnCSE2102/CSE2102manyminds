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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import manyminds.debug.Level;

public class
Rule
extends KnowledgeWithChildren {
    private String myType;
    private Preconditions myPreconditions;
    private Actions myActions;
    private int fireCount;
    private static Timer resetTimer = new Timer();
    private static final long resetDelay = 1000 * 60 * 30;
    private boolean readyToFire;
    
    class
    Preconditions
    extends KnowledgeWithChildren
    implements KnowledgeListener {

        public
        Preconditions() {
            super();
        }
        
        public boolean
        removeChild(KnowledgeObject ko) {
            return false;
        }
            
        public String
        toString() {
            return "Preconditions";
        }

        public String
        getDetail() {
            return toString();
        }
        
        public void
        valueChanged(KnowledgeEvent ke) {
            calculateValue();
        }

        public void
        detailChanged(KnowledgeEvent ke) {}
        
        public void
        URLChanged(KnowledgeEvent ke) {}
        
        public void
        referenceChanged(KnowledgeEvent ke) {}
        
        public void
        referentChanged(KnowledgeEvent ke) {}
        
        void
        calculateValue() {
            Iterator it = myChildren.iterator();
            while (it.hasNext()) {
                Precondition pc = (Precondition)it.next();
                if (!pc.getBooleanValue()) {
                    setValue(FALSE);
                    
                    return;
                }
            }
            setValue(TRUE);
            return;
        }
    }
    
    class
    Actions
    extends KnowledgeWithChildren
    implements Postcondition {
        public
        Actions() {
            super();
       }
        
        public boolean
        removeChild(KnowledgeObject ko) {
            return false;
        }
    
        public Object
        doAction() {
            Iterator it = myChildren.iterator();
            List ret_val = new LinkedList();
            while (it.hasNext()) {
                Postcondition pc = (Postcondition)it.next();
                Object o = pc.doAction();
                if (o != null) {
                    ret_val.add(o);
                }
            }
            if (!ret_val.isEmpty()) {
                return ret_val;
            } else {
                return null;
            }
        }
        
        public String
        getDetail() {
            return toString();
        }
        
        public String
        toString() {
            return "Actions";
        }
    }

    public
    Rule() {
        super(null,null,null,null);
        myPreconditions = new Preconditions();
        myActions = new Actions();
        fireCount = 0;
        readyToFire = false;
        myPreconditions.addKnowledgeListener(this);
        myActions.addKnowledgeListener(this);
        setValue(FALSE);
    }
    
    public void
    addPart(KnowledgeObject ko) {
        if (ko instanceof Precondition) {
            myPreconditions.addChild(ko);
        } else if (ko instanceof Postcondition) {
            myActions.addChild(ko);
        }
    }
    
    public void
    completedLoading() {
        if (!readyToFire) {
            readyToFire = true;
            myPreconditions.calculateValue();
        }
    }
    
    public void
    setType(String s) {
        myType = s;
    }
    
    public String
    getType() {
        return myType;
    }
    
    public boolean
    removeChild(KnowledgeObject ko) {
        return false;
    }
    
    public void
    reset() {
        if ((myType.equals("normal")) && (fireCount != 0) && (myPreconditions.getLeaves().size() > 0)) {
            fireCount = 0;
            if (myPreconditions.getValue().equals(TRUE)) {
                setValue(FALSE);
                setValue(TRUE);
            }
        }
    }
    
    public Object
    fireRule() {
        if ((myPreconditions.getBooleanValue()) 
            && (myType.equals("super") || fireCount == 0)) {
            knowledgeLogger.log(Level.FINE,"RULE FIRING: "+getDetail());
            Iterator it = myActions.getLeaves().iterator();
            List ret_val = new LinkedList();
            while (it.hasNext()) {
                Postcondition po = (Postcondition)it.next();
                Object o = po.doAction();
                if (o != null) {
                    ret_val.add(o);
                }
            }
            ++fireCount;
            if (myType.equals("normal")) {
                resetTimer.schedule(new TimerTask() {
                    public void
                    run() {
                        reset();
                    }
                }, resetDelay);
            }
            if (!ret_val.isEmpty()) {
                return ret_val;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    public void
    valueChanged(KnowledgeEvent ke) {
        if (readyToFire) { // && (myType.equals("super") || fireCount == 0)) {  //only fire rules once
            setValue(myPreconditions.getValue());
        }
    }
            
    public KnowledgeWithChildren
    getPreconditions() {
        return myPreconditions;
    }
            
    public KnowledgeWithChildren
    getActions() {
        return myActions;
    }
    
    public int getChildCount() {
        int ret_val = 0;
        if (myPreconditions.getChildCount() > 0) {
            ++ret_val;
        }
        if (myActions.getChildCount() > 0) {
            ++ret_val;
        }
        return ret_val;
    }
    
    public Object
    getChild(int index) {
        Iterator it = myChildren.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof KnowledgeWithChildren) {
                if (((KnowledgeWithChildren)o).getChildCount(o) > 0) {
                    --index;
                }
            } else {
                --index;
            }
            if (index < 0) {
                return o;
            }
        }
        return null;
    }
        
    
    public String
    toXML() {
        StringBuffer sb = new StringBuffer("<rule type=\""+myType+"\">"
                                        + "\n"
                                        + "<detail>"+getDetail()+"</detail>"
                                        + "\n");
        sb.append("</rule>");
        sb.append("\n");
        return sb.toString();
    }
}