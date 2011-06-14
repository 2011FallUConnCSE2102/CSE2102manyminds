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

public class
BeliefRequirement
extends KnowledgeObject
implements Precondition, KnowledgeListener {

    private KnowledgeBase myKB;

    public
    BeliefRequirement(String index, KnowledgeBase kb) {
        super(null,index,null,null);
        myKB = kb;
        if (getIndex() != null) {
            myKB.addKnowledgeListener(this,getIndex());
        }
    }
    
    public
    BeliefRequirement() {
        super();
    }
    
    public void
    setKB(KnowledgeBase kb) {
        myKB = kb;
        if (getIndex() != null) {
            myKB.addKnowledgeListener(this,getIndex());
        }
    }
    
    public void
    setIndex(String s) {
        super.setIndex(s);
        if (myKB != null) {
            myKB.addKnowledgeListener(this,getIndex());
        }
    }
    
    public String
    getDetail() {
        return "I must believe " + getIndex();
    }
    
    public Object
    getValue() {
        if (getIndex() != null) {
            String s = getIndex();
            if (myKB.isBelieved(s) ^ getPolarity()) {
                return FALSE;
            } else {
                return TRUE;
            }
        } else {
            return FALSE;
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
    valueChanged(KnowledgeEvent ke) {
        fireValueChanged((Boolean)ke.getOldValue());
    }

    public String
    toXML() {
        return "<belief-requirement polarity=\""+getPolarity()+"\">"
                + "\n"
                + "<index>"+getIndex()+"</index>"
                + "\n"
                + "</belief-requirement>"
                + "\n";
    }

}
