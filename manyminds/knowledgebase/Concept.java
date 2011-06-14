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

import java.awt.dnd.DnDConstants;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class
Concept
extends KnowledgeObject {
    
    private LinkedList myBeliefs = new LinkedList();
    
    public
    Concept(String detail) {
        super(null,null,detail,null);
    }
    
    public
    Concept() {
        super();
    }
    
    public void
    addBelief(String kr) {
        myBeliefs.add(kr);
    }
    
    public boolean
    isAppropriate(KnowledgeObject ko) {
        return false;
    }
    
    public void
    removeBelief(String kr) {
        myBeliefs.remove(kr);
    }    
    
    public int
    knowledgeDropped(KnowledgeObject ko, KnowledgeBase kb) {
        return DnDConstants.ACTION_NONE;
    }
    
    public int
    getIndexOfChild(Object child) {
        return myBeliefs.indexOf(child);
    }
    
    public List
    getBeliefs() {
        return myBeliefs;
    }

    public void
    setReference(String s) {
        addBelief(s);
    }

    public String
    toXML() {
        StringBuffer sb =new StringBuffer("<concept>"
                                        + "\n"
                                        + "<detail>"+getDetail()+"</detail>"
                                        + "\n");
        Iterator it = myBeliefs.iterator();
        while (it.hasNext()) {
            sb.append("<reference>");
            sb.append(it.next().toString());
            sb.append("</reference>\n");
        }
        sb.append("</concept>");
        sb.append("\n");
        return sb.toString();
    }
    
}
