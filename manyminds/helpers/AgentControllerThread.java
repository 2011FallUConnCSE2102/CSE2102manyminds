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
 package manyminds.helpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import manyminds.ManyMindsConstants;
import manyminds.agents.Agent;
import manyminds.agents.AgentContainer;
import manyminds.agents.AgentFace;
import manyminds.agents.AgentList;
import manyminds.application.ApplicationContext;
import manyminds.communication.KQMLMessage;
import manyminds.communication.MessageDispatcher;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.datamodel.RaterModel;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.util.StringExploder;
import manyminds.util.gui.RaterPanel;

public class AgentControllerThread extends MessageDispatcher {
    
    private AgentContainer myContainer;
    private String currentLeader = null;
    private static Logger logger = Logger.getLogger("manyminds");
    private HashSet myArtifactControllers;
    private List raterBacklog = new LinkedList();
    private HashSet myAdviceControllers;
    
    public AgentControllerThread() {
        super("facilitator");
        myContainer = (AgentContainer)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.AGENT_ID);
        myArtifactControllers = new HashSet();
        myArtifactControllers.add("ArtifactDispatcher");
        myAdviceControllers = new HashSet();
    }
    
    public void
    initialize() {
    }

    public void handleMessages(List messages) {
        Iterator it = messages.iterator();
        while (it.hasNext()) {
            KQMLMessage mess = (KQMLMessage)it.next();
            try {
                if (mess.getOntology().equals("control")) {
                    List action = StringExploder.explode(StringExploder.stripParens(mess.getContent()));
                    String agentname = mess.getSender();
                    Agent a = AgentList.getAgentList().getAgent(agentname);
                    if (a != null) {
                        AgentFace af = a.getFace();
                        if (action.get(0).equals("state-change")) {
                            logger.log(Level.FINER,"Changing agent state "+mess.toString());
                            af.setRegulationState(action.get(1).toString());
                        } else if (action.get(0).equals("emotion-change")) {
                            logger.log(Level.FINER,"Changing agent emotion "+mess.toString());
                            af.setEmotion(action.get(1).toString());
                        } else if (action.get(0).equals("add-section")) {
                            synchronized (myArtifactControllers) {
                                if (!myArtifactControllers.isEmpty()) {
                                    Iterator it2 = myArtifactControllers.iterator();
                                    while (it2.hasNext()) {
                                        KQMLMessage.deliver("(achieve :receiver "+it2.next().toString()+" :content "+mess.getContent()
                                            +" :sender facilitator :language java :ontology control :in-reply-to rater-control)");
                                    }
                                } else {
                                    logger.log(Level.INFO,"Adding "+mess.getContent()+" to backlog");
                                    raterBacklog.add(" :content "+mess.getContent()+ " :sender facilitator :language java :ontology control :in-reply-to rater-control)");
                                }
                            }
                        } else if (action.get(0).equals("add-pages")) {
                            synchronized (myArtifactControllers) {
                                if (!myArtifactControllers.isEmpty()) {
                                    Iterator it2 = myArtifactControllers.iterator();
                                    while (it2.hasNext()) {
                                        KQMLMessage.deliver("(achieve :receiver "+it2.next().toString()+" :content "+mess.getContent()
                                            +" :sender facilitator :language java :ontology control :in-reply-to rater-control)");
                                    }
                                } else {
                                    logger.log(Level.INFO,"Adding "+mess.getContent()+" to backlog");
                                    raterBacklog.add(" :content "+mess.getContent()+ " :sender facilitator :language java :ontology control :in-reply-to rater-control)");
                                }
                            }
                        } else if (action.get(0).equals("rater-action")) {
                            synchronized (myArtifactControllers) {
                                if (!myArtifactControllers.isEmpty()) {
                                    Iterator it2 = myArtifactControllers.iterator();
                                    while (it2.hasNext()) {
                                        KQMLMessage.deliver("(achieve :receiver "+it2.next().toString()+" :content "+mess.getContent()
                                            +" :sender facilitator :language java :ontology control :in-reply-to rater-control)");
                                    }
                                } else {
                                    logger.log(Level.INFO,"Adding "+mess.getContent()+" to backlog");
                                    raterBacklog.add(" :content "+mess.getContent()+ " :sender facilitator :language java :ontology control :in-reply-to rater-control)");
                                }
                            }
                            String type = action.get(1).toString();
                            String proto = action.get(2).toString();
                            String loc = action.get(3).toString();
                            String index = action.get(4).toString();
                            if (loc.equals("agent-panel")) {
                                if (type.equals("add")) {
                                    String documentTitle;
                                    RaterModel rm = null;
                                    if (index.equals("n") || index.equals("N") || index.equals("-1")) {
                                        documentTitle = proto + "-0";
                                        rm = (RaterModel)DataContext.getContext().getSharedData()
                                            .addData(documentTitle,RaterModel.instantiateRaterModel(proto));
                                    } else if (index.equals("-2")) {
                                        documentTitle = proto;
                                        rm = (RaterModel)DataContext.getContext().getSharedData().getData(documentTitle);
                                    } else {
                                        documentTitle = proto + "-" + index;
                                        rm = (RaterModel)DataContext.getContext().getSharedData()
                                            .addData(documentTitle,RaterModel.instantiateRaterModel(proto));
                                    }
                                    ManyMindsDocument cd = (ManyMindsDocument)DataContext.getContext().getSharedData().addData("comments-"+documentTitle,ManyMindsDocument.newDocument());
                                    DataContext.getContext().getSharedData().updateLists(documentTitle);
                                    DataContext.getContext().getSharedData().updateLists("comments-"+documentTitle);
                                    rm.setLinkedDocumentName("comments-"+documentTitle);
                                    RaterPanel rp = new RaterPanel(rm);
                                    myContainer.addRater(rp);
                                } else {
                                }
                            }
                        } else if (action.get(0).equals("have-advice")) {
                        /* Iterator it2 = myAdviceControllers.iterator();
                            while (it2.hasNext()) {
                                KQMLMessage.deliver("(achieve :receiver "+it2.next().toString()+" :content "+mess.getContent()
                                    +" :sender facilitator :language java :ontology control :in-reply-to advice-control)");
                            }*/
                            logger.log(Level.FINER,"Agent has advice "+mess.toString());
                            String sum = action.get(1).toString();
                            String url = action.get(2).toString();
                            sum = sum.substring(1,sum.length()-1);
                            url = url.substring(1,url.length()-1);
                            af.giveAdvice(sum,url);
                        } else if (action.get(0).equals("join-team")) {
                            logger.log(Level.FINER,"Agent joining team "+mess.toString());
                            myContainer.addAgent(af);
                            if (mess.getSender().equals(currentLeader)) {
                                currentLeader = null;
                            }
                        } else if (action.get(0).equals("leaving-team")) {
                            logger.log(Level.FINER,"Agent leaving team "+mess.toString());
                            if ((currentLeader != null) && (mess.getSender().equals(currentLeader))) {
                                    currentLeader = null;
                            }
                            myContainer.removeAgent(af);
                        } else if (action.get(0).equals("head-team")) {
                            if ((currentLeader != null) && (!mess.getSender().equals(currentLeader))) {
                                KQMLMessage.deliver("(tell :receiver "+currentLeader+" :ontology control :content booting "+currentLeader
                                                    + " :sender facilitator :in-reply-to leaderboot :language java)");
                            }
                            logger.log(Level.FINER,"Agent heading team "+mess.toString());
                            myContainer.addLeader(af);
                            currentLeader = mess.getSender();
                        } else if (action.get(0).equals("team-dissolve")) {
                        } else {
                            logger.log(Level.INFO,agentname+" sent bad message: "+mess.toString());
                        }
                    } else if (mess.getContent().equals("able-to-control-raters")) {
                        synchronized (myArtifactControllers) {
                            myArtifactControllers.add(mess.getSender());
                            Iterator it2 = raterBacklog.iterator();
                            while (it2.hasNext()) {
                                KQMLMessage.deliver("(achieve :receiver "+mess.getSender()+it2.next().toString()/*)*/);
                            }
                            raterBacklog.clear();
                        }
                    } else if (mess.getContent().equals("able-to-control-advice")) {
                        myAdviceControllers.add(mess.getSender());
                    } else {
                        logger.log(Level.WARNING,"Couldn't find agent for:  "+mess.toString());
                    }
                } else {
                    logger.log(Level.INFO,"Didn't know what to do with "+mess.toString());
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE,"Error dispatching message "+mess.toString(),t);
            }
        }
    }
}