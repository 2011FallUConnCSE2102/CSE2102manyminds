/* AgentPackage.java
 * Created on Nov 24, 2003
 *  
 * Copyright (C) 1998-2003 Regents of the University of California
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * @author eric
 *
 */
public class AgentPackage {
	private String branchTag;
	private int majorVersion;
	private int minorVersion;
	private String branchComment;
    
    private HashMap myAgents = new HashMap();
	
	public Collection getAgents() {
	    return Collections.unmodifiableCollection(myAgents.values());   
    }
    
    /**
     * @return Returns the branchComment.
     */
    public String getBranchComment() {
        return branchComment;
    }

    /**
     * @param branchComment The branchComment to set.
     */
    public void setBranchComment(String branchComment) {
        this.branchComment = branchComment;
    }

    /**
     * @return Returns the branchTag.
     */
    public String getBranchTag() {
        return branchTag;
    }

    /**
     * @param branchTag The branchTag to set.
     */
    public void setBranchTag(String branchTag) {
        this.branchTag = branchTag;
    }

    /**
     * @return Returns the majorVersion.
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * @param majorVersion The majorVersion to set.
     */
    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    /**
     * @return Returns the minorVersion.
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * @param minorVersion The minorVersion to set.
     */
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }
    
    public void addAgent(AgentDefinition ad) {
        myAgents.put(ad.get("this-name"),ad);
    }
   
    public AgentDefinition getAgent(String name) {
        return (AgentDefinition) myAgents.get(name);
    }

    public static AgentPackage
    loadPackage(File f) {
        String packageFolder = f.getName();
        String qualifiedPackage = packageFolder.substring(0,packageFolder.length()-".agents".length());
        int lastDot = qualifiedPackage.lastIndexOf('.');
        int middleDot = qualifiedPackage.lastIndexOf('.',lastDot-1);
        String majorString = qualifiedPackage.substring(middleDot,lastDot); 
        String minorString = qualifiedPackage.substring(lastDot);
        String tagString = qualifiedPackage.substring(0,middleDot);
        AgentPackage retVal = new AgentPackage();
        retVal.branchTag = tagString;
        retVal.majorVersion = Integer.parseInt(majorString);
        retVal.minorVersion = Integer.parseInt(minorString);
        try {
            BufferedReader commentStream = new BufferedReader(new InputStreamReader(new FileInputStream(new File(f,"comment.txt"))));
            StringBuffer commentBuffer = new StringBuffer();
            while(commentStream.ready()) {
                commentBuffer.append(commentStream.readLine());
                commentBuffer.append("\n");
            }
            retVal.branchComment = commentBuffer.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        try {
            BufferedReader agentListStream = new BufferedReader(new InputStreamReader(new FileInputStream(new File(f,"agent_list.txt"))));
            LinkedList agentNameList = new LinkedList();
            while(agentListStream.ready()) {
                agentNameList.add(agentListStream.readLine());
            }
            for (Iterator iter = agentNameList.iterator(); iter.hasNext();) {
                try {
                    String name = (String) iter.next();
                    File agentRoot = new File(f,"agents");
                    File agentFile = new File(agentRoot,name);
                    AgentDefinition ad = AgentDefinitionFactory.loadAgentAttributes(agentFile);
                    retVal.myAgents.put(ad.get("this-name"),ad);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return retVal;
    }
    
    public void
    savePackage(File f) {
        try {
            File packDir = new File(f,branchTag+"."+majorVersion+"."+minorVersion+".agents");
            if (!packDir.exists()) {
                packDir.mkdirs();
            }
            
            File commentFile = new File(packDir,"comment.txt");
            BufferedWriter out = new BufferedWriter(new FileWriter(commentFile));
            out.write(branchComment);
            out.close();
            
            File agentRoot = new File(packDir,"agents");
            if (!agentRoot.exists()) {
                agentRoot.mkdirs();
            }
            
            File[] currentAgents = agentRoot.listFiles();
            for (int i = 0; i < currentAgents.length; i++) {
                File file = currentAgents[i];
                file.delete();
            }
            
            File agentListFile = new File(packDir,"agent_list.txt");
            out = new BufferedWriter(new FileWriter(agentListFile));
            
            for (Iterator iterator = myAgents.values().iterator(); iterator.hasNext();) {
                AgentDefinition element = (AgentDefinition) iterator.next();
                File agentJar = new File(packDir,element.get("this-name")+".jar");
                AgentDefinitionFactory.saveAgent(element,agentJar);
                out.write(agentJar.getName());
                out.write("\n");
            }
            out.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
