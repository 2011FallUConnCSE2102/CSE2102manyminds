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

import java.awt.Color;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.ParserConfigurationException;

import manyminds.artifact.ArtifactFactory;
import manyminds.debug.Level;
import manyminds.knowledgebase.KnowledgeBase;
import manyminds.knowledgebase.KnowledgeFactory;
import manyminds.knowledgebase.KnowledgeFactory.KnowledgeDocumentHandler;
import manyminds.util.ManyMindsResolver;
import manyminds.util.XMLFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class
AgentFactory 
extends KnowledgeFactory {

    protected static class 
    AgentHandler
    extends KnowledgeDocumentHandler {
        private Agent currentAgent = null;
        private String agentName = null;
        private String myResourceBase;
        
        public
        AgentHandler(String resourceBase) {
            super();
            myResourceBase = resourceBase;
        }
        
        public void
        startElement(String namespaceURI, String localName, String qName, org.xml.sax.Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            if (name.equals("agent")) {
                try {
                    currentAgent = new Agent();
                    currentKB = new KnowledgeBase();
                    currentAgent.setKnowledgeBase(currentKB);
                    agentName = atts.getValue("name");
                    currentAgent.getFace().setColor(new Color(parseInt(atts.getValue("color"))));
                } catch (Throwable t) {
                    logger.log(Level.SEVERE,"Couldn't initialize agent",t);
                }
            } else if (name.equals("face-image")) {
                String emotion = atts.getValue("emotion");
                String substate = atts.getValue("substate");
                String path = atts.getValue("path");
                try {
                    URL imageURL = new URL(myResourceBase+path);
                    currentAgent.getFace().loadImageForState(emotion+"."+substate,imageURL);
                } catch (MalformedURLException me) {
                    logger.log(Level.WARNING,"Error creating URL for agent face: "+myResourceBase+path,me);
                }
            } else {
                super.startElement(namespaceURI, localName, qName, atts);
            }
        }
        
        public void
        endElement(String namespaceURI, String localName, String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            if (name.equals("agent")) {
                currentAgent.setDispatcherName(agentName);
                currentAgent.completedLoading();
                HashSet teamMutex = new HashSet();
                teamMutex.add("I am in the team");
                teamMutex.add("I am not in the team");
                teamMutex.add("I am heading the team");
                currentKB.mutexBeliefs(teamMutex);
            } else {
                super.endElement(namespaceURI, localName, qName);
            }
        }
        
        public Agent
        getAgent() {
            return currentAgent;
        }
        
    }
	
    public static Agent
    loadAgent(File f) 
    throws java.io.IOException {
        try {
            JarFile jf = new JarFile(f);
            Agent loadedAgent = null;
            List discoveredRaters = new LinkedList();
            List discoveredPages = new LinkedList();
            String sysID = f.toURL().toString();
            JarEntry agentDef = null;
            Enumeration entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry je = (JarEntry)entries.nextElement();
                if ((je.getName().startsWith("raters/")) && (!je.getName().equals("raters/"))) {
                    discoveredRaters.add(je);
                } else if ((je.getName().startsWith("pages/")) && (!je.getName().equals("pages/"))) {
                    discoveredPages.add(je);
                } else if (je.getName().equals("agent.xml")) {
                    agentDef = je;
                }
            }
            Iterator it = discoveredRaters.iterator();
            while (it.hasNext()) {
                JarEntry je = (JarEntry)it.next();
               // System.err.println("Loading discovered rater "+je.getName());
                InputSource is = new InputSource(new InputStreamReader(jf.getInputStream(je)));
                is.setSystemId(sysID);
                ArtifactFactory.loadRaterPrototype(is);
            }
            
            it = discoveredPages.iterator();
            while (it.hasNext()) {
                JarEntry je = (JarEntry)it.next();
             //   System.err.println("Loading discovered page "+je.getName());
                InputSource is = new InputSource(new InputStreamReader(jf.getInputStream(je)));
                is.setSystemId(sysID);
                ArtifactFactory.loadPagePrototype(is);
            }
            
            if (agentDef != null) {
                InputSource is = new InputSource(new InputStreamReader(jf.getInputStream(agentDef)));
                is.setSystemId(sysID);
                String resourceBase = "jar:"+sysID+"!/";
                XMLReader p = XMLFactory.createXMLReader(false);
                AgentHandler ah = new AgentHandler(resourceBase);
                p.setContentHandler(ah);
                p.setEntityResolver(new ManyMindsResolver());
                p.parse(is);
                loadedAgent = ah.getAgent();
                ManyMindsResolver.addAgentResource(loadedAgent.getName(),resourceBase);
            } else {
                String fileNoExt = f.getName().substring(0,f.getName().length() - 4);
                String resourceBase = "jar:"+sysID+"!/";
                ManyMindsResolver.addAgentResource(fileNoExt,resourceBase);
            }
            return loadedAgent;
        } catch (SAXException se) {
            handleError(se);
            return null;
        } catch (ParserConfigurationException pce) {
            handleError(pce);
            return null;
        }
    }
    
    public static void
    loadAgents(File f)
    throws java.io.IOException {
        File[] list = f.listFiles();
        for (int i = 0; i < list.length; ++i) {
            if (list[i].getName().endsWith(".jar")) {
                Agent a = loadAgent(list[i]);
                if (a != null) {
                    AgentList.getAgentList().addAgent(a);
                }
            }
        }
    }
}