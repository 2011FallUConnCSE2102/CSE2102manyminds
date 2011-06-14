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

/*
 * agent.java
 *
 * The agent class extends thread and is the basic agent implementation for ManyMinds
 *
 * Written by Eric Eslinger
 * Copyright © 1998-1999 University of California
 * All Rights Reserved.
 *
 * Agenda
 *	Add self-documenting javadoc comments to the methods.
 *
 * History
 *	20 NOV 98 EME Created today.
 *  14 DEC 98 EME Got the basic event cycle working.
 *	16 JUN 99 EME modified initialization structurem commented code.
 *
 */
 
 /**
  * The ManyMinds agent object.  Each agent object allocates its own thread of execution
  * in which the event cycle processes.
  * @author Eric M Eslinger
  * @see java.lang.Thread
  */
 
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import manyminds.communication.KQMLMessage;
import manyminds.communication.Message;
import manyminds.communication.MessageDispatcher;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.knowledgebase.AddPages;
import manyminds.knowledgebase.AddRater;
import manyminds.knowledgebase.AddSection;
import manyminds.knowledgebase.BeliefAssertion;
import manyminds.knowledgebase.Concept;
import manyminds.knowledgebase.GiveAdvice;
import manyminds.knowledgebase.KnowledgeBase;
import manyminds.knowledgebase.KnowledgeEvent;
import manyminds.knowledgebase.KnowledgeListener;
import manyminds.knowledgebase.KnowledgeObject;
import manyminds.knowledgebase.RemoveRater;
import manyminds.knowledgebase.Rule;
import manyminds.knowledgebase.SetGlobal;
import manyminds.knowledgebase.TeamFormationAction;

public class
Agent
implements KnowledgeListener, Runnable { 
    
    /**
     * Comment for <code>allFrozen</code>
     */
    private static boolean allFrozen = false;
    /**
     * Comment for <code>workQue</code>
     */
    private static List workQue = Collections.synchronizedList(new LinkedList());
    /**
     * Comment for <code>agentLogger</code>
     */
    private static Logger agentLogger = Logger.getLogger("manyminds.knowledgebase");
    /**
     * Comment for <code>logger</code>
     */
    private static Logger logger = Logger.getLogger("manyminds");
    /**
     * Comment for <code>art</code>
     */
    private static AgentReasoningThread art = new AgentReasoningThread();
    /**
     * Comment for <code>myFace</code>
     */
    private AgentFace myFace = new AgentFace();
    /**
     * Comment for <code>artifactActions</code>
     */
    private List artifactActions = new LinkedList();
    /**
     * Comment for <code>currentTeam</code>
     */
    private TeamFormationAction currentTeam = null;
    
    static {
        art.start();
    }
    
    /**
     * All Agent objects use one AgentReasoningThread to handle any knowledge event cascades that arise
     * from information from the globals (namely the artifact).
     * 
     * @author eric
     *
     */
    public static class
    AgentReasoningThread
    extends Thread {
        
        /**
         * Are we running?  Cancelling agent dispatching means setting this to false.
         */
        private boolean dispatching = true;
        /**
         * The Agent currently utilizing this thread for reasoning.
         */
        private Agent currentAgent;
        
        /**
         * Constructor
         */
        public AgentReasoningThread() {
            super("Agent Reasoning Thread");
        }
        
        /**
         * Returns the name of the agent that is currently using this thread for reasoning.
         * @return the name of that agent.
         */
        public String currentAgentName() {
            if (currentAgent != null) {
                return currentAgent.getName();
            } else {
                return "none";
            }
        }
        
        /**
         * This is the dispatch event cycle.  Agents add themselves to the workQue and 
         * signal the AgentReasoningThread.  This wakes the thread up, which provides a 
         * thread of execution for the Agent's doActions() method.
         * 
         * @see java.lang.Runnable#run()
         */
        public void
        run() {
            List innerQue = new LinkedList();
            while (dispatching) {
                synchronized (workQue) {
                    while (workQue.isEmpty()) {
                        try {
                            workQue.wait();
                        } catch (InterruptedException ie) {
                        }
                    }
                    ((Agent)workQue.remove(0)).doActions();
                }
            }
        }
    }

    /**
     * Gets the agent's events dispatched in the appropriate thread.
     * 
     * @param a the agent to be worked on, usually this.
     */
    private static void
    workOnMe(Agent a) {
        if (a.imLoaded) {
            synchronized (workQue) {
                workQue.add(a);
                workQue.notifyAll();
            }
        }
    }
    
    /**
     * Freezes the execution thread, important during DataServer peering so the agents don't go crazy.
     */
    public static void
    freezeAll() {
        allFrozen = true;
    }
    
    /**
     * Unfreezes the execution thread.
     */
    public static void
    unfreezeAll() {
        allFrozen = false;
    }
    
    /**
     * Is this agent frozen?
     * @return true if this agent or all agents are frozen.
     */
    private boolean
    isFrozen() {
        return (frozen || allFrozen);
    }
    
    /**
     * Each Agent gets its own MessageDispatcher, which watches its incoming mailbox.
     * 
     * @author eric
     *
     */
    private class
    AgentMessageDispatcher
    extends MessageDispatcher {
        public AgentMessageDispatcher() {
            super();
        }
        
        public AgentMessageDispatcher(String n) {
            super(n);
        }
	
        public void
	setDispatcherName(String n) {
            super.setDispatcherName(n);
        }
	
	
	public void
	initialize() {
            //Agent.this.waitForLoad();
	}	
	
/**
 * If there are any messages in the inbox, process them and then handle any newly active rules in 
 * the AgentReasoningThread.
 */
        public synchronized void handleMessages(List messages) {
            Iterator it = messages.iterator();
            while (it.hasNext()) {
                processMessage((Message)it.next());
            }
            if (!myActiveRules.isEmpty()) {
                workOnMe(Agent.this);
            }
        }
    }



    /**
     * This Agent's KnowledgeBase
     */
    private KnowledgeBase myKB = null;
    /**
     * This Agent's mailbox watcher.
     */
    private AgentMessageDispatcher myThread;
    /**
     * Becomes true after the load cycle is complete (so delayed rule actions can fire)
     */
    private boolean imLoaded = false;
    /**
     * Whom to notify when my emotional or advice state changes.
     */
    private Map myAgentControlSubscriptions = new HashMap();
    /**
     * Whom to notify when my active state changes
     */
    private Map myTeamControlSubscriptions = new HashMap();
    /**
     * Whom to notify when I want to modify the artifact
     */
    private Map myArtifactControlSubscriptions = new HashMap();
    /**
     * Rules that seem ready to fire
     */
    private List myActiveRules = Collections.synchronizedList(new LinkedList());
    /**
     * The name of the agent that is heading my team (deprecated, since we aren't using teams anymore)
     */
    private String myLeader = null;
    /**
     * True when I'm in the current team (also deprecated)
     */
    private boolean inTeam = false;
    /**
     * True when I'm heading the current team (deprecated some more)
     */
    private boolean headingTeam = false;
    /**
     * Set this to true when you want to halt this agent permanently  
     */
    private boolean done = false;
    /**
     * Set this to true if you want to halt this agent temporarily
     */
    private boolean frozen = false;
 
    /**
     * Constructor
     * @param n This agent's name
     */
    public
    Agent(String n) {
        this();
        setDispatcherName(n);
    }
    
    /**
     * Basic constructor
     */
    public
    Agent() {
        super();
        myAgentControlSubscriptions.put("facilitator","regulation-state");
        myTeamControlSubscriptions.put("facilitator","regulation-state");
        myArtifactControlSubscriptions.put("facilitator","regulation-state");
    }
    
    /**
     * Creates an agent
     * @param n the agent's name
     * @param fresh true if this is a brand new agent (from the editor) rather than a new agent being loaded.
     */
    public
    Agent(String n, boolean fresh) {
        this();
        setKnowledgeBase(new KnowledgeBase());
        setDispatcherName(n);
        freeze();
        if (fresh) {
            Concept emotion = new Concept("My emotion mode");
            emotion.addBelief("I feel: Happy");
            Concept mode = new Concept("My advice regulation mode");
            mode.addBelief("My advice mode is: Alert");
            mode.addBelief("My advice mode is: Respond");
            mode.addBelief("My advice mode is: Interrupt");
            myKB.mutexBeliefs(mode.getBeliefs());
            myKB.mutexBeliefs(emotion.getBeliefs());
        }
    }
    
   /**
     * Freeze this agent.
     */
     public void
    freeze() {
        frozen = true;
    }
    
    /**
     * Reset all rules to unfired.  Good idea when we get a newly peered upstream dataserver
     */
    public void
    reset() {
        Iterator it = myKB.getRules().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Rule) {
                ((Rule)o).reset();
            }
        }
    }
    
    /**
     * Fires all currently active rules.  If the rule returns an action, handle that action before
     * moving on to the next rule.
     * @return true if any rules fired.
     */
    public boolean doActions() {
        if (!isFrozen()) {
            boolean didsomething = false;
            while (!myActiveRules.isEmpty()) {
                didsomething = true;
                try {
                    Rule r = (Rule)myActiveRules.remove(0);
                    Object o = r.fireRule();
                    if (o != null) {
                        handleRuleOutput(o);
                    }
                } catch (Throwable t) {
                    logger.log(Level.WARNING,"Error handing  rule",t);
                    return false;
                }
            }
            return didsomething;
        } else {
            return false;
        }
    }
    
    /**
     * Deal with whatever a particular rule wants us to do.  
     * @param o an object returned from Rule.fire()
     */
    protected void
    handleRuleOutput(Object o) {
        if (!isFrozen()) {
           // agentLogger.log(Level.FINE,"handling rule return "+o.toString());
            if (o instanceof List) {
                Iterator it = ((List)o).iterator();
                while (it.hasNext()) {
                    handleRuleOutput(it.next());
                }
            } else if (o instanceof TeamFormationAction) {
                headTeam((TeamFormationAction)o);
            } else if (o instanceof GiveAdvice) {
                giveAdvice((GiveAdvice)o);
            } else if (o instanceof AddRater) {
                addRater((AddRater)o);
            } else if (o instanceof AddSection) {
                addSection((AddSection)o);
            } else if (o instanceof AddPages) {
                addPages((AddPages)o);
            } else if (o instanceof RemoveRater) {
                removeRater((RemoveRater)o);
            } else if (o instanceof BeliefAssertion) {
                myKB.believe(((BeliefAssertion)o).getDetail());
            } else if (o instanceof SetGlobal) {
                DataContext.getContext().getSharedData().addData(((SetGlobal)o).getURL(),ManyMindsDocument.newDocument()).setValue(((SetGlobal)o).getDetail());
            } else {
                throw new Error("Can't handle "+o.toString());
            }
        }
    }
    
    /**
     * Event cycle.  Every time there are active rules to deal with, deal with them
     * and then wait until more rules become active (as a result of messages from globals, usually)
     * 
     * @see java.lang.Runnable#run()
     */
    public void
    run() {
        while (!done) {
            synchronized (myActiveRules) {
                try {
                    doActions();
                    myActiveRules.wait();
                } catch (Throwable t) {
                    agentLogger.log(Level.WARNING,"Trouble with "+getName(),t);
                }
            }
        }
    }
    
    /**
     * 
     * 
     */
    public void
    handleActiveRules() {
    }
    
    /**
     * We don't care about this.
     * @param ke
     * @see manyminds.knowledgebase.KnowledgeListener#detailChanged(manyminds.knowledgebase.KnowledgeEvent)
     */
    public void
    detailChanged(KnowledgeEvent ke) {}
    
    /**
     * We don't care about this.
     * @param ke
     * @see manyminds.knowledgebase.KnowledgeListener#URLChanged(manyminds.knowledgebase.KnowledgeEvent)
     */
    public void
    URLChanged(KnowledgeEvent ke) {}
    
    /**
     * We don't care about this.
     * @param ke
     * @see manyminds.knowledgebase.KnowledgeListener#referenceChanged(manyminds.knowledgebase.KnowledgeEvent)
     */
    public void
    referenceChanged(KnowledgeEvent ke) {}
    
    /**
     * We don't care about this.
     * @param ke
     * @see manyminds.knowledgebase.KnowledgeListener#referentChanged(manyminds.knowledgebase.KnowledgeEvent)
     */
    public void
    referentChanged(KnowledgeEvent ke) {}
    
    /**
     * We only care when the value of a variable changes.  It is either a belief or a rule
     * because we don't register on anything else.  If it is a belief, we make sure it isn't 
     * a funny one (like advice mode or emotion) before moving on.  If it is a rule,
     * we just add it to the active rules and call workOnMe().
     * @param ke
     * @see manyminds.knowledgebase.KnowledgeListener#valueChanged(manyminds.knowledgebase.KnowledgeEvent)
     */
    public void
    valueChanged(KnowledgeEvent ke) {
        if (!isFrozen()) {
            if (ke.getSource() == myKB) {
                String belief = ke.getTag();
                if (belief != null) {
                    if ((myKB.isBelieved(belief)) && (belief.startsWith("My advice mode is: "))) {
                        String mode = belief.substring("My advice mode is: ".length()).toLowerCase();
                        changeAdviceMode(mode);
                    } else if ((myKB.isBelieved(belief)) && (belief.startsWith("I feel: "))) {
                        String mode = belief.substring("I feel: ".length()).toLowerCase();
                        changeEmotion(mode);
                    }
                }
            } else if (ke.getSource() instanceof Rule) {
                Rule r = (Rule)ke.getSource();
                if (r.getValue().equals(Boolean.TRUE)) {
                    myActiveRules.add(r);
                    workOnMe(this);
                }
            }
        }
    }

    /**
     * 
     * @return This Agent's KnowledgeBase
     */
    public KnowledgeBase
    getKnowledgeBase() {
        return myKB;
    }
    
    /**
     * Set this Agent's knowledgebase
     * @param kb the KnowledgeBase to set
     */
    public void
    setKnowledgeBase(KnowledgeBase kb) {
        myKB = kb;
    }
        
    /**
     * This is called by the agent factory when the agent is done loading.  At this
     * point, we know to look at all the rules and fire any that are already ready
     * to go.
     * 
     */
    public synchronized void
    completedLoading() {
        if (imLoaded == false) {
            imLoaded = true;
            Iterator it = myKB.getRules().iterator();
            while (it.hasNext()) {
                KnowledgeObject o = (KnowledgeObject)it.next();
                if (o instanceof Rule) {
                    if (o.getBooleanValue()) {
                        myActiveRules.add(o);
                    }
                }
            }
            myKB.addKnowledgeListener(this);
            notifyAll();
            if (!myActiveRules.isEmpty()) {
                workOnMe(this);
            }
        }
    }
    
    /**
     * Kill this agent.
     * 
     */
    public void
    stopProcessing() {
        done = true;
        //myThread.stopDispatching();
    }
        
    
    /**
     * This method will block until the agent is completely loaded and initialized.
     * So hopefully you're loading in a different thread.
     * 
     */
    public synchronized void
    waitForLoad() {
        if (imLoaded == false) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }
    
    /**
     * 
     * This should add a binary resource to this agent (an image to display, audio file, etc)
     * right now it doesn't do anything, actually, because it is part of an unimplemented
     * set of stuff that was laid out for the agent editor software.
     * 
     * @param resourceName
     * @param resourceData
     * @throws RemoteException
     */
    public void
    addResourceToAgent(String resourceName, byte[] resourceData)
    throws RemoteException {
    }

    /**
     * Creates a mailbox watcher if necessary, and starts dispatching messages.
     * @param n
     */
    public void
    setDispatcherName(String n) {
        myFace.setName(n);
        if (myThread == null) {
            myThread = new AgentMessageDispatcher(n);
        } else {
            myThread.setDispatcherName(n);
        }
    }
    
    /**
     * Get the agent's face 
     * @return the AgentFace that represents this agent.
     */
    public AgentFace
    getFace() {
        return myFace;
    }
    
    /**
     * Get the name of this agent
     * @return the name of this agent
     */
    public String
    getName()
    //throws RemoteException {
    {
        return myThread.getName();
    }
    
    /**
     * Get all the transient information in this agent's knowledgebase turned into an XML database.
     * This doesn't save the structural information (rules and whatnot), just the beliefs.
     * @return the XML representation of this agent.
     */
    public String
    toXML() {
        StringBuffer ret_val = new StringBuffer("<agent name=\"");
        ret_val.append(getName());
        ret_val.append("\">\n");
        ret_val.append("<face-image emotion=\"normal\" substate=\"active\" path=\"Media/alert_active.gif\" />\n");
        ret_val.append("<face-image emotion=\"normal\" substate=\"inactive\" path=\"Media/alert_inactive.gif\" />\n");
        ret_val.append(myKB.toXML());
        ret_val.append("</agent>\n");
        return ret_val.toString();
    }
    
                
    /**
     * Handle a message that the mailbox watcher (MessageDispatcher) has received.
     * @param m the Message to process.
     */
    protected void
    processMessage(Message m) {
        KQMLMessage mess = (KQMLMessage)m;
        if (mess.getSender().equals("global-val-box")) {
            /*if (mess.getPerformative().equals("tell")) {
                Iterator it = StringExploder.explode(mess.getContent()).iterator();
                while (it.hasNext()) {
                    String content = StringExploder.stripParens(it.next().toString());
                    int eqloc = content.indexOf("=");
                    if (eqloc > -1) {
                        String key = content.substring(0,eqloc).trim();
                        String val = content.substring(eqloc+1).trim();
                        myKB.setGlobalValue(key,val);
                    }
                }
            }*/
        } else if (mess.getPerformative().equals("subscribe")) {
            if (mess.getOntology().equals("control")) {
                if (mess.getContent().equals("team")) {
                    subscribeTeamControl(mess.getSender(),mess.getReplyWith());
                } else if (mess.getContent().equals("agent")) {
                    subscribeAgentControl(mess.getSender(),mess.getReplyWith());
                } else if (mess.getContent().equals("artifact")) {
                    subscribeArtifactControl(mess.getSender(),mess.getReplyWith());
                } else if (mess.getContent().equals("all")) {
                    subscribeTeamControl(mess.getSender(),mess.getReplyWith());
                    subscribeAgentControl(mess.getSender(),mess.getReplyWith());
                    subscribeArtifactControl(mess.getSender(),mess.getReplyWith());
                }
            }
        } else if (mess.getPerformative().equals("tell")) {
            if (mess.getOntology().equals("control")) {
                if (mess.getContent().equals("join-my-team")) {
                    joinCurrentTeam(mess.getSender());
                } else if (mess.getContent().equals("team-boot")) {
                    leaveCurrentTeam(mess.getSender());
                } else if (mess.getContent().startsWith("booting")) {
                    if (mess.getContent().endsWith(getName())) {
                        dissolveCurrentTeam();
                    }
                }
            }
        } else if (mess.getPerformative().equals("ask")) {
        }
    }
    
    /**
     * Adds a mailbox to the list of mailboxes that are interested in this agent's team
     * state.  If this is a new subscription, send the appropriate state as a reply as well.
     * @param subscriber the Mailbox address that is subscribing
     * @param replyWith the text string that goes into the :in-reply-to field of the message.
     */
    private synchronized void
    subscribeTeamControl(String subscriber, String replyWith) {
        if (myTeamControlSubscriptions.put(subscriber,replyWith) == null) {
            if (headingTeam) {
                KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                    " :sender "+getName()+" :content head-team :language java :ontology control)");
            } else if (inTeam) {
                KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                    " :sender "+getName()+" :content join-team :language java :ontology control)");
            }
        } 
    }
    
    /**
     * Adds a mailbox to the list of mailboxes that are interested in this agent's
     * state.  If this is a new subscription, send the appropriate state as a reply as well.
     * @param subscriber the Mailbox address that is subscribing
     * @param replyWith the text string that goes into the :in-reply-to field of the message.
     */
    private synchronized void
    subscribeAgentControl(String subscriber, String replyWith) {
        if (myAgentControlSubscriptions.put(subscriber,replyWith) == null) {
            if (myKB != null) {
                String state = null;
                if (myKB.isBelieved("My advice mode is: Alert")) {
                    state = "alert";
                } else if (myKB.isBelieved("My advice mode is: Respond")) {
                    state = "respond";
                } else if (myKB.isBelieved("My advice mode is: Interrupt")) {
                    state = "interrupt";
                }
                if (state != null) {
                    KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                        " :sender "+getName()+" :content (state-change "+state+") :language java :ontology control)");
                }
            }
        }
    }
    
    /**
     * Adds a mailbox to the list of mailboxes that are interested in this agent's artifact requests
     * If this is a new subscription, send the appropriate state as a reply as well.
     * @param subscriber the Mailbox address that is subscribing
     * @param replyWith the text string that goes into the :in-reply-to field of the message.
     */
    private synchronized void
    subscribeArtifactControl(String subscriber, String replyWith) {
        synchronized (artifactActions) {
            myArtifactControlSubscriptions.put(subscriber,replyWith);
            Iterator it = artifactActions.iterator();
            while (it.hasNext()) {
                KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith
                    +" :sender "+getName()+it.next().toString()+" :language java :ontology control)");
            }
        }
    }
    
    /**
     * Join the current team as a member.  This will send out the appropriate messages
     * to subscribers and handle any internal state changes necessary (including dissolving
     * the current team if this agent is heading the old team.
     * @param leader The leader of the team to join
     */
    private synchronized void
    joinCurrentTeam(String leader) {
        if (headingTeam) {
            dissolveCurrentTeam();
        }
        if (!inTeam) {
            inTeam = true;
            headingTeam = false;
            Iterator it = myTeamControlSubscriptions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry ent = (Map.Entry)it.next();
                String subscriber = ent.getKey().toString();
                String replyWith = ent.getValue().toString();
                KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                    " :sender "+getName()+" :content join-team :language java :ontology control)");
            }
        }
        myLeader = leader;
        myKB.believe("I am in the team");
    }
    
    /**
     * Creates a new team, with this agent as the head.
     * @param tn Contains all the information necessary to construct the new team.
     */
    private synchronized void
    headTeam(TeamFormationAction tn) {
        logger.log(Level.FINEST,"Heading team "+tn);
        myLeader = "me";
        inTeam = true;
        headingTeam = true;
        currentTeam = tn;
        if (currentTeam != null) {
            Iterator teamIt = tn.getMembers().iterator();
            StringBuffer teamMembers = new StringBuffer();
            while (teamIt.hasNext()) {
                String member = teamIt.next().toString();
                if (teamMembers.length() != 0) {
                    teamMembers.append(" # ");
                }
                teamMembers.append(member);
                KQMLMessage.deliver("(tell :receiver "+member+" :sender "+getName()
                        +" :content join-my-team :language java :ontology control :in-reply-to team-control)");
            }
            Iterator it = myTeamControlSubscriptions.entrySet().iterator();
            String content = "head-team ("+teamMembers.toString()+")";
            while (it.hasNext()) {
                Map.Entry ent = (Map.Entry)it.next();
                String subscriber = ent.getKey().toString();
                String replyWith = ent.getValue().toString();
                KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                    " :sender "+getName()+" :content "+content+" :language java :ontology control)");
            }
            myKB.believe("I am heading the team");
        }
    }
    
    /**
     * Take apart the current team.  Handles internal state change and message sending.
     * 
     */
    private synchronized void
    dissolveCurrentTeam() {
        if (headingTeam) {
            headingTeam = false;
            if (currentTeam != null) {
                Iterator teamIt = currentTeam.getMembers().iterator();
                StringBuffer teamMembers = new StringBuffer();
                while (teamIt.hasNext()) {
                    String member = teamIt.next().toString();
                    if (teamMembers.length() != 0) {
                        teamMembers.append(" # ");
                    }
                    teamMembers.append(member);
                    KQMLMessage.deliver("(tell :receiver "+member+" :sender "+getName()
                            +" :content team-boot :language java :ontology control :in-reply-to team-control)");
                }
                Iterator it = myTeamControlSubscriptions.entrySet().iterator();
                String content = "team-dissolve ("+teamMembers.toString()+")";
                while (it.hasNext()) {
                    Map.Entry ent = (Map.Entry)it.next();
                    String subscriber = ent.getKey().toString();
                    String replyWith = ent.getValue().toString();
                    KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                        " :sender "+getName()+" :content "+content+" :language java :ontology control)");
                }
                leaveCurrentTeam("me");
                currentTeam = null;
            }
        }
    }
   
    /**
     * Just drop out of the current team, changing internal beliefs and sending
     * the appropriate messages.
     * @param leader The leader of the team this agent should be leaving.  If it is not the current
     * team leader, nothing happens.
     */
    private synchronized void
    leaveCurrentTeam(String leader) {
        if ((inTeam) && (leader.equals(myLeader))) {
            inTeam = false;
            Iterator it = myTeamControlSubscriptions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry ent = (Map.Entry)it.next();
                String subscriber = ent.getKey().toString();
                String replyWith = ent.getValue().toString();
                KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                    " :sender "+getName()+" :content leaving-team :language java :ontology control)");
            }
            myKB.believe("I am not in the team");
            myLeader = null;
        }
    }
    
    /**
     * Try to give a piece of advice, changing internal beliefs and sending the appropriate
     * messages.
     * @param ad Encapsulates the information needed to give a piece of advice.
     */
    private synchronized void
    giveAdvice(GiveAdvice ad) {
        Iterator it = myAgentControlSubscriptions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry ent = (Map.Entry)it.next();
            String subscriber = ent.getKey().toString();
            String replyWith = ent.getValue().toString();
            KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                " :sender "+getName()+" :content (have-advice ("
                +ad.getDetail()+") ("+ad.getURL().replace('\n',' ')+"))"+
                " :language java :ontology control)");
        }
        ad.setValue(KnowledgeObject.FALSE);
    }
    
    /**
     * Sets this agent's emotion to a new value.  Sends messages.  Hopefully this Agent's
     * AgentFace can handle that new emotion.
     * @param emotion
     */
    private synchronized void
    changeEmotion(String emotion) {
        Iterator it = myAgentControlSubscriptions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry ent = (Map.Entry)it.next();
            String subscriber = ent.getKey().toString();
            String replyWith = ent.getValue().toString();
                        KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                            " :sender "+getName()+" :content (emotion-change "+emotion+") :language java :ontology control)");
        }
    }
    
    /**
     * Changes the kind of advice mode this agent is in, which affects how the 
     * various helpers deal with giveAdvice messages.
     * @param mode
     */
    private synchronized void
    changeAdviceMode(String mode) {
        Iterator it = myAgentControlSubscriptions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry ent = (Map.Entry)it.next();
            String subscriber = ent.getKey().toString();
            String replyWith = ent.getValue().toString();
                        KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith+
                            " :sender "+getName()+" :content (state-change "+mode+") :language java :ontology control)");
        }
    }
    
    /**
     * Add or remove something in the artifact (research notebook).  Called by the convenience
     * methods below (addSection, addPages, etc)
     * @param content
     */
    private void
    sendArtifactControlMessage(String content) {
        Iterator it = myArtifactControlSubscriptions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry ent = (Map.Entry)it.next();
            String subscriber = ent.getKey().toString();
            String replyWith = ent.getValue().toString();
            KQMLMessage.deliver("(tell :receiver "+subscriber+" :in-reply-to "+replyWith
                +" :sender "+getName()+content+" :language java :ontology control)");
        }
    }
    
    /**
     * Add a whole section to the artifact.
     * @param as
     */
    private synchronized void
    addSection(AddSection as) {
        String content = " :content add-section ("+as.getName()+") "+as.getWeight()+" "+as.getColor()+" "+as.getUnselectedColor();
        artifactActions.add(content);
        sendArtifactControlMessage(content);
    }
    
    /**
     * Add pages to the artifact (in a particular section)
     * @param ap
     */
    private synchronized void
    addPages(AddPages ap) {
        String content = " :content add-pages ("+ap.getLayout()+") ("+ap.getSection()+")";
        artifactActions.add(content);
        sendArtifactControlMessage(content);
    }
    
    /**
     * Add a rater to a page somewhere.
     * @param ar
     */
    private synchronized void
    addRater(AddRater ar) {
        String dataName = ar.getReference();
        String raterLoc = ar.getDetail();
        /*String linkTarget = "";
        if (ar.getURL() != null) {
            if (ar.getURL().startsWith("<")) {
                linkTarget = ar.getURL();
            } else {
                linkTarget = "<give-advice><url>"+java.net.URLEncoder.encode(myName)+"/"+ar.getURL()+"</url></give-advice>";
            }
        }
        linkTarget = "("+linkTarget.replace('\n',' ')+")";*/
        String docIndex;
        if (ar.getReference().endsWith("-group")) {
            docIndex = "-1";
            dataName = dataName.substring(0,dataName.length()-"-group".length());
        } else {
            docIndex = "-2";
        }
        String content = " :content rater-action add ("+dataName+") ("+raterLoc+") "+docIndex;//+" "+linkTarget;
        artifactActions.add(content);
        sendArtifactControlMessage(content);
    }
    
    /**
     * Remove a rater from a page somewhere in the artifact.
     * @param rr
     */
    private synchronized void
    removeRater(RemoveRater rr) {
        String dataName = rr.getReference();
        String raterLoc = rr.getURL();
        String content = " :content rater-action remove "+dataName+" "+raterLoc;
        artifactActions.add(content);
        sendArtifactControlMessage(content);
    }
}
