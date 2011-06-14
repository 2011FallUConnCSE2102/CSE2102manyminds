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
AddSection
extends KnowledgeObject
implements Postcondition {
	
    private String myName, myWeight, myColor, myUnselectedColor;
    
    public
    AddSection(String name, String weight, String color, String unselectedColor) {
        super(null,null,null,null);
        myName = name;
        myWeight = weight;
        myColor = color;
        myUnselectedColor = unselectedColor;
    }
    
    public String
    getName() {
        return myName;
    }

    public String
    getWeight() {
        return myWeight;
    }

    public String
    getColor() {
        return myColor;
    }

    public String
    getUnselectedColor() {
        return myUnselectedColor;
    }

    public String
    getDetail() {
        return "Add Section "
                +myName
                +" to location "
                +myWeight
                +" in the artifact";
    }
    
    public Object
    doAction() {
        return this;
    }
    
    public boolean
    equals(Object o) {
        if (o instanceof AddSection) {
            AddSection ar = (AddSection)o;
            if ((ar != null)
                && (myName == ar.myName)) {
                return true;
            }
        }
        return false;
    }
    
    public String
    toXML() {
        return "<add-pages name=\""+myName+"\" weight=\""+myWeight+"\" color=\""+myColor+"\" unselected-color=\""+myUnselectedColor+"\" />";
    }
}
