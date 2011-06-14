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
import java.io.File;
import java.net.URLDecoder;
import manyminds.util.NameEncoder;

public class
DynamicAttributes
extends AbstractAttributes {
    
    private static List myKeys = null;
    
    public
    DynamicAttributes() {
        super(true);
        synchronized (DynamicAttributes.class) {
            if (myKeys == null) {
                ArrayList al = new ArrayList();
                al.add("this-name");
                al.add("linkto");
                al.add("image");
                al.add("!");
                al.add("if");
                al.add("incr");
                al.add("decr");
                myKeys = Collections.unmodifiableList(al);
            }
        }
    }
    
    public Collection
    getKeys() {
        return myKeys;
    }
    
/*	Dynamic Attributes
	Tag Signifier			Tag Description

        !				Comment tag (returns empty string)
        
        linkto target [anchortext]	target can be URL, or agent://Agent%20Name/Page%20Name
                                        if optional anchortext is missing, the target URL, Agent Name or Page Name
                                        is used
                                        
        image URL			if URL is really a url, just an img tag.  Otherwise copy the resource into
                                        the agent's resource folder and make an img that points there.
*/

    public String
    get(String key) {
            try {
            if (key.startsWith("!")) {
                return "";
            } else if (key.equals("this-name")) {
                return "SystemAttributes";
            } else if (key.startsWith("linkto")) {
                String url = key.substring(6).trim();
                String anchortext = null;
                if (url.indexOf(" ") > -1) {
                    anchortext = url.substring(url.indexOf(" ")).trim();
                    url = url.substring(0,url.indexOf(" ")).trim();
                }
                if (url.startsWith("agent://")) {
                    url = url.substring(8);
                    if (url.endsWith("/")) {
                        url = url.substring(0,url.length() - 1);
                    }
                    String pageName, agentName;
                    if (url.indexOf("/") != -1) {
                        pageName = URLDecoder.decode(url.substring(url.indexOf("/")+1),"UTF-8");
                        agentName = URLDecoder.decode(url.substring(0,url.indexOf("/")),"UTF-8");
                    } else {
                        pageName = "Home Page";
                        agentName = URLDecoder.decode(url,"UTF-8");
                    }
                    if (anchortext == null) {
                        anchortext = pageName;
                    }
                    Attributes a = AbstractAttributes.getAttributes(agentName);
                    url = null;
                    if (a instanceof AgentDefinition) {
                        AdvicePageDefinition p = ((AgentDefinition)a).getPage(pageName);
                        if (p != null) {
                            url = p.get("page-filename");
                            if (url != null) {
                                url = "../"+NameEncoder.encode(agentName)+"/"+NameEncoder.encode(url);
                            } else {
                                return null;
                            }                        
                        } else {
                            return "";
                        }
                    }
                } else {
                    if (anchortext == null) {
                        anchortext = url;
                    }
                }
                if (url != null) {
                    return "<a href=\""+url+"\">"+anchortext+"</a>";
                } else {
                    return getFromParents(key);
                }
            } else if (key.startsWith("encode")) {
                return NameEncoder.encode(key.substring(6).trim());
            } else if (key.startsWith("image")) {
                String imloc = key.substring(5).trim();
                if (imloc.startsWith("http://")) {
                    return "<img src=\""+imloc+"\">";
                } else {
                    File imageFile = new File(System.getProperty("manyminds.home")+System.getProperty("file.separator")+"AgentResources",URLDecoder.decode(imloc,"UTF-8"));
                    if (imageFile.exists()) {
                        return "[[[copy-resource "+NameEncoder.encode(imageFile.toString())+"]]] <img src=\""+NameEncoder.encode(imageFile.getName())+"\">";
                    } else {
                        return null;
                    }
                }
            } else if (key.startsWith("incr")) {
                StringTokenizer st = new StringTokenizer(key);
                st.nextToken();
                String num = st.nextToken();
                try {
                    int i = Integer.parseInt(num);
                    return Integer.toString(i + 1);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            } else if (key.startsWith("decr")) {
                StringTokenizer st = new StringTokenizer(key);
                st.nextToken();
                String num = st.nextToken();
                try {
                    int i = Integer.parseInt(num);
                    return Integer.toString(i - 1);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            } else if (key.startsWith("file-exists")) {
                String fileName = URLDecoder.decode(key.substring("file-exists".length()).trim(),"UTF-8");
                File f = new File(System.getProperty("manyminds.home"),fileName);
                if (f.exists()) {
                    return "true";
                } else {
                    return "false";
                }
            } else if (key.startsWith("if")) {
                StringTokenizer st = new StringTokenizer(key);
                st.nextToken();
                int retVal = 0;
                String action = st.nextToken();
                if (action.equalsIgnoreCase("false")) {
                    retVal = -1;
                } else if (action.equalsIgnoreCase("true")) {
                    retVal = 1;
                } else if (action.equalsIgnoreCase("eq")) {
                    if (st.nextToken().compareTo(st.nextToken()) == 0 ) {
                        retVal = 1;
                    } else {
                        retVal = -1;
                    }
                } else if (action.equalsIgnoreCase("lt")) {
                    if (st.nextToken().compareTo(st.nextToken()) < 0 ) {
                        retVal = 1;
                    } else {
                        retVal = -1;
                    }
                } else if (action.equalsIgnoreCase("gt")) {
                    if (st.nextToken().compareTo(st.nextToken()) > 0 ) {
                        retVal = 1;
                    } else {
                        retVal = -1;
                    }
                }
                if (retVal == 0) {
                    return null;
                } else if (retVal == 1) {
                    int thenLoc = key.indexOf("THEN");
                    int endLoc = key.lastIndexOf("ELSE");
                    if (endLoc < 0) {
                        endLoc = key.length();
                    }
                    return key.substring(thenLoc+4,endLoc);
                } else if (retVal == -1) {
                    int elseLoc = key.lastIndexOf("ELSE");
                    if (elseLoc >= 0) {
                        return key.substring(elseLoc+4);
                    } else {
                        return "";
                    }
                } else {
                    return null;
                }
            } else {
                return getFromParents(key);
            }
        } catch (java.io.UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return "";
        }
    }
}