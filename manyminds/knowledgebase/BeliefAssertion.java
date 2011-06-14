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
BeliefAssertion
extends KnowledgeObject
implements Postcondition {
	
    public
    BeliefAssertion(String detail) {
        super(null,null,detail,null);
    }
    
    public
    BeliefAssertion() {
        super();
    }
    
    public Object
    doAction() {
        return this;
    }
        
    public String
    toXML() {
        return "<belief-assertion>"
            + "\n"
            + "<detail>"+getDetail()+"</detail>"
            + "\n"
            + "</belief-assertion>"
            + "\n";
    }
	
}
