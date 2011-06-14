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

public abstract class
Metaglobal
extends KnowledgeObject
implements KnowledgeListener {

    protected Global myGlobal;

    public
    Metaglobal() {
        super();
    }
    
    public
    Metaglobal(Global g) {
        super();
        setGlobal(g);
    }

    public void
    setGlobal(Global g) {
        if (myGlobal != null) {
            myGlobal.removeKnowledgeListener(this);
        }
        myGlobal = g;
        myGlobal.addKnowledgeListener(this);
    }

    public void
    detailChanged(KnowledgeEvent ke) {}
    
    public void
    URLChanged(KnowledgeEvent ke) {}

    public void
    referentChanged(KnowledgeEvent ke) {}
    
    public void
    referenceChanged(KnowledgeEvent ke) {
    }
    
    public Object getValue() {
        if ((!(this instanceof ConstMetaglobal))
            && (myGlobal.getValue() == Global.UNSET)) {
            return Global.UNSET;
        } else {
            return super.getValue();
        }
    }
    
    public String
    toXML() {
        return "";
    }
}