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

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import manyminds.ManyMindsConstants;
import manyminds.application.ApplicationContext;
import manyminds.artifact.Artifact;
import manyminds.artifact.SectionTabbedPane;
import manyminds.communication.KQMLMessage;
import manyminds.communication.MessageDispatcher;
import manyminds.debug.Level;
import manyminds.util.StringExploder;
import manyminds.util.XMLFactory;

public class ArtifactDispatcher extends MessageDispatcher {

    private boolean myLoading = true;
    
    public ArtifactDispatcher() {
        super("ArtifactDispatcher");
        //KQMLMessage.deliver("(tell :sender ArtifactDispatcher :receiver facilitator :ontology control :language java :content able-to-control-raters :reply-with regulation-state)");
    }
    
    public void
    initialize() {
    }

    public synchronized void
    setLoading(boolean l) {
        myLoading = l;
        notifyAll();
    }
    
    public synchronized void
    waitForLoad() {
        while (isLoading()) {
            try {
                wait();
            } catch (InterruptedException ie) {}
        }
    }
    
    public synchronized boolean
    isLoading() {
        return myLoading;
    }

    public void handleMessages(List messages) {
        Iterator it = messages.iterator();
        int numDispatched = 0;
        while (it.hasNext()) {
            KQMLMessage mess = (KQMLMessage)it.next();
            try {
                if ((mess.getOntology().equals("control")) && (mess.getPerformative().equals("achieve"))) {
                    List action = StringExploder.explode(StringExploder.stripParens(mess.getContent()));
                    if (action.get(0).equals("rater-action")) {
                        String type = action.get(1).toString();
                        String proto = StringExploder.stripParens(action.get(2).toString());
                        String loc = StringExploder.stripParens(action.get(3).toString());
                        String index = action.get(4).toString();
                        /*String linkTarget = null;
                        if (action.size() > 5) {
                            linkTarget = StringExploder.stripParens(action.get(5).toString());
                        }*/
                        int sharedInt;
                        if (index.equals("n") || index.equals("N")) {
                            sharedInt = -1;
                        } else {
                            sharedInt = Integer.parseInt(index);
                        }
                        if (type.equals("add")) {
                            ((Artifact)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID)).addRater(proto,loc,sharedInt);
                        } else {
                            ((Artifact)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID)).removeRater(proto,loc);
                        }
                    } else if (action.get(0).equals("add-section")) {
                        String name = StringExploder.stripParens(action.get(1).toString());
                        int weight = XMLFactory.parseInt(action.get(2).toString());
                        int color = XMLFactory.parseInt(action.get(3).toString());
                        int unselectedColor = XMLFactory.parseInt(action.get(4).toString());
                        Artifact rn = (Artifact)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID);
                        SectionTabbedPane stp = new SectionTabbedPane(new Color(color),new Color(unselectedColor));
                        stp.setShortTitle(name);
                        rn.addSection(stp,weight);
                    } else if (action.get(0).equals("add-pages")) {
                        String layout = StringExploder.stripParens(action.get(1).toString());
                        String section = StringExploder.stripParens(action.get(2).toString());
                        Artifact rn = (Artifact)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID);
                        rn.addPagesToSection(layout,section);
                    }
                } else {
                    logger.log(Level.INFO,"Didn't know what to do with "+mess.toString());
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE,"Error dispatching message "+mess.toString(),t);
            }
        }
        if (myLoading) {
            setLoading(false);
        }
    }
}