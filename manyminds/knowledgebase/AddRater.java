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
AddRater
extends KnowledgeObject
implements Postcondition {
	
    public
    AddRater(String kr) {
        super(null,null,"agent-panel",kr);
    }

    public
    AddRater(String s, String d,String r) {
        super(s,null,d,r);
    }
    
    public
    AddRater() {
        super();
    }
    
    public Object
    doAction() {
        return this;
    }
    
    public boolean
    equals(Object o) {
        if (o instanceof AddRater) {
            AddRater ar = (AddRater)o;
            if ((ar != null)
                && (getDetail() != null)
                && (getReference() != null)
                && (ar.getDetail() != null)
                && (ar.getReference() != null)
                && (getDetail().equals(ar.getDetail()))
                && (getReference().equals(ar.getReference()))) {
                return true;
            }
        }
        return false;
    }
    
    public String
    toXML() {
        return "<add-rater>"
                + "\n"
                + "<reference>"
                + getReference()
                + "</reference>\n"
                + "<url>"
                + getURL()
                + "</url>\n"
                + "<detail>"
                + getDetail()
                + "</detail>\n"
                + "</add-rater>"
                + "\n";
    }
}
