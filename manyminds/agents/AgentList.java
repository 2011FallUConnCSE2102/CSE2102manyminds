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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class
AgentList {

    private static AgentList single = new AgentList();
        
    private LinkedList myList;
    
    public static AgentList
    getAgentList() {
        return single;
    }
    
    
    private Map myAgents = Collections.synchronizedMap(new HashMap());
    
    private
    AgentList() {
        myList = new LinkedList();
    }

    public Agent
    getAgent(String s) {
        return (Agent)myAgents.get(s);
    }
    
    public void
    addAgent(Agent a) {
        myAgents.put(a.getName(),a);
    }
    
    public Iterator
    nameIterator() {
        return myAgents.keySet().iterator();
    }

    public Iterator
    agentIterator() {
        return myAgents.values().iterator();
    }

}