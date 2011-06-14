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
 package manyminds.agents;

import java.util.*;

import manyminds.util.XMLFactory;

public class
StaticAttributes
extends AbstractAttributes 
implements MutableAttributes {

    protected HashMap myAttributes = new HashMap();
    
    public
    StaticAttributes(boolean b) {
    	super(b);
    }
    
    public
    StaticAttributes(String n) {
        super(true);
        set("this-name",n);
    }

    public String
    attributeXML() {
        StringBuffer retVal = new StringBuffer();
        Iterator it = getParents().iterator();
        while (it.hasNext()) {
            retVal.append("<parent><![CDATA[");
            retVal.append((String)it.next());
            retVal.append("]]></parent>\n");
        }
        
        it = myAttributes.keySet().iterator();
        while (it.hasNext()) {
            String k = (String)it.next();
            String v = (String)myAttributes.get(k);
            if (!k.startsWith("volatile-")) {
                retVal.append("<attribute name=\"");
                retVal.append(k);
                retVal.append("\"><![CDATA[");
                retVal.append(v);
                retVal.append("]]></attribute>\n");
            }
        }
        return retVal.toString();
    }
    
    public void
    clearAll() {
        myAttributes.clear();
    }

    public String
    get(String s) {
        if (myAttributes.containsKey(s)) {
            return myAttributes.get(s).toString();
        } else {
            return getFromParents(s);
        }
    }
    
    public String
    getFilename() {
        return get("this-filename");
    }
    
    public Collection
    getKeys() {
        return myAttributes.keySet();
    }
    
    public void
    set(String k, Object v) {
        myAttributes.put(k,v);
    }
    
    public void
    setFilename(String s) {
        set("this-filename",s);
    }
        

    public String
    toXML() {
        StringBuffer retVal = new StringBuffer(XMLFactory.getXMLHeader("attributes"));
        retVal.append("<attributes name=\"");
        retVal.append(get("this-name"));
        retVal.append("\">\n");
        retVal.append(attributeXML());
        retVal.append("</attributes>\n");
        return retVal.toString();
    }

    
}