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
 package manyminds.util;

//import manyminds.communication.*;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import manyminds.debug.Level;
import manyminds.debug.Logger;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class
ManyMindsResolver
implements EntityResolver {

    protected static GarbageClass single;
    private static HashMap agentMap = new HashMap();

    protected static class
    GarbageClass
    extends Object {
    }
    
    public static void
    addAgentResource(String agentName, String jarURL) {
        synchronized (agentMap) {
            agentMap.put(agentName,jarURL);
        }
    }
    
    public InputSource
    resolveEntity(String publicID, String systemID) 
    throws org.xml.sax.SAXException, java.io.IOException {
        Logger.getLogger("manyminds").log(Level.FINE,"Looking for "+publicID+" / "+systemID);
        if (systemID.endsWith("logger.dtd")) {
            systemID = "classpath://manyminds/resources/xml/logger.dtd";
        }
        if (publicID != null) {
            if (publicID.equals("manyminds-DTD")) {
                systemID = "classpath://manyminds/resources/xml/manyminds.dtd";
            }
        }
        InputStream is = resolveResource(systemID);
        if (is != null) {
            return new InputSource(is);
        } else {
            return null;
        }
    }
	
    public static InputStream
    resolveResource(String n) {
        try {
            URL rv = resolveClasspathURI(n);
            return rv.openStream();
        } catch (Throwable t) {
            Logger.getLogger("manyminds").log(Level.WARNING,"Couldn't resolve "+n,t);
            return null;
        }
    }

    public static URL
    resolveClasspathURI(String n) {
        try {
            String s;
            if (n.startsWith("classpath://")) {
                s = n.substring(12);
                if (single == null) {
                    single = new GarbageClass();
                }
                return single.getClass().getClassLoader().getResource(s);
            } else if (n.startsWith("agent://")) {
                int slashThree = n.indexOf("/",8);
                String agentName = n.substring(8,slashThree);
                synchronized (agentMap) {
                    String prefix = (String)agentMap.get(agentName);
                    return new URL(prefix + n.substring(slashThree+1));
                }
            } else {
                return new URL(n);
            }
        } catch (Exception e) {
            Logger.getLogger("manyminds").log(Level.INFO,"Couldn't resolve"+n,e);
            return null;
        }
    }
}

