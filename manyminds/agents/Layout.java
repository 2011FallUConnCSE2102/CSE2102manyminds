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
Layout {

    public static final String BODY = "[[[body-text]]]\n";
    
    private static Map allLayouts = Collections.synchronizedMap(new HashMap());
    
    private String headerString = "";
    private String footerString = "";
    private String myName = "";
    private String myFilename = null;

    private String myParent = null;

    protected
    Layout() {}
    
    public String
    getFilename() {
        return myFilename;
    }
    
    public void
    setFilename(String s) {
        myFilename = s;
    }
    
    public String
    getName() {
        return myName;
    }
    
    protected
    Layout(String n) {
        myName = n;
        allLayouts.put(n,this);
    }
        
    public synchronized static Layout
    getLayout(String s) {
        Object o = allLayouts.get(s);
        if (o == null) {
            o = new Layout(s);
        }
        return (Layout)o;
    }
    
    public synchronized static void
    removeLayout(Layout l) {
        allLayouts.remove(l);
    }
    
    public synchronized static Set
    getLayoutNames() {
        return Collections.unmodifiableSet(allLayouts.keySet());
    }

    public void
    changeName(String newname) {
        if (!myName.equals("")) {
            allLayouts.remove(myName);
        }
        myName = newname;
        allLayouts.put(myName,this);
    }
    
    public StringBuffer
    layoutPage(AdvicePageDefinition p, StringBuffer body) {
        StringBuffer retVal = new StringBuffer(getHeader());
        retVal.append(body);
        retVal.append(getFooter());
        if (myParent != null) {
            retVal = getLayout(myParent).layoutPage(p,retVal);
        }
        return retVal;
    }

    public StringBuffer
    layoutPage(AdvicePageDefinition p) {
        return layoutPage(p,new StringBuffer(BODY));
    }

    public void
    setHeader(String s) {
        headerString = s;
    }
    
    public void
    setFooter(String s) {
        footerString = s;
    }

    public String
    getHeader() {
        return headerString;
    }
    
    public String
    getParent() {
        return myParent;
    }
    
    public void
    setParent(String s) {
        if ("NONE".equals(s)) {
            myParent = null;
        } else {
            myParent = s;
        }
    }
    
    public String
    getFooter() {
        return footerString;
    }
    
    public String
    toString() {
        return myName;
    }
    
    public String
    toXML() {
        StringBuffer retVal = new StringBuffer(XMLFactory.getXMLHeader("layout"));
        retVal.append("<layout name=\"");
        retVal.append(myName);
        retVal.append("\">\n");
        if (myParent != null) {
            retVal.append("<parent><![CDATA[");
            retVal.append(myParent);
            retVal.append("]]></parent>\n");
        }
        retVal.append("<header><![CDATA[");
        retVal.append(getHeader());
        retVal.append("]]></header>\n<footer><![CDATA[");
        retVal.append(getFooter());
        retVal.append("]]></footer>\n</layout>\n");
        return retVal.toString();
    }
}