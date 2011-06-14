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
import java.util.HashSet;
import java.util.Iterator;

public class
TeamFormationAction
extends KnowledgeObject
implements Postcondition {
    
    private Collection teamMembers = new HashSet();
    
    public
    TeamFormationAction() {
        super();
    }
    
    public Object
    doAction() {
        return this;
    }
    
    public void
    setReference(String s) {
        addMember(s);
    }
    
    public Collection
    getMembers() {
        return teamMembers;
    }
    
    public void
    addMember(String s) {
        teamMembers.add(s);
    }
    
    public String
    getDetail() {
        Iterator it = teamMembers.iterator();
        StringBuffer retVal = new StringBuffer("Form and head a team consisting of ");
        if (it.hasNext()) {
            while (it.hasNext()) {
                retVal.append(it.next().toString());
                if (it.hasNext()) {
                    retVal.append(", ");
                }
            }
        } else {
            retVal.append("Just Me");
        }
        return retVal.toString();
    }
    
    public String
    toXML() {
        return "<team-action type=\"form\">"
            + "\n"
            + "<reference>"+getReference()+"</reference>"
            + "\n"
            + "</team-action>"
            + "\n";
    }
}
