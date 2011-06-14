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
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import manyminds.ManyMindsConstants;
import manyminds.application.ApplicationContext;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.helpers.PageDisplayerThread;

public class AgentFace
extends JButton
//extends JLabel
implements ManyMindsConstants, ActionListener {

    private HashMap stateIcons;
    private String myActiveURL;
    private String myName;
    private String myState = "alert";
    private String mySubstate = "inactive";
    private String myEmotion = "normal";
    private static Logger logger = Logger.getLogger("manyminds");
    private static final Color ALERT_COLOR = new Color(0xFF3399);
    private static final Color INTERRUPT_COLOR = new Color(0xFF0033);
    private static final Color RESPOND_COLOR = new Color(0xFF99CC);
    private static final Color LEADER_COLOR = TEXT_TEXT;
    private Color myColor = BASE_BACKGROUND;
    private Color currentBorderColor = ALERT_COLOR;
    private boolean lastBorderBoolean = false;
    private AttentionAnimator myAttentionAnimator = new AttentionAnimator();
        
    public class
    AttentionAnimator
    implements ActionListener {
        int animFrame = 0;
        Timer timer;
        public void
        actionPerformed(ActionEvent ae) {
            if (isShowing()) {
                try {
                    Graphics2D g = (Graphics2D)AgentFace.this.getGraphics();
                    g.setColor(currentBorderColor);
                    g.drawString("!",(int)(Math.random()*getSize().width),(int)(Math.random()*getSize().height));
                    ++animFrame;
                    if (animFrame == 25) {
                        animFrame = 0;
                        AgentFace.this.repaint();
                    }
                } catch (Throwable t) {}
            }
        }
        
        public
        AttentionAnimator() {
            timer = new Timer(500, this);
            timer.setCoalesce(true);
            timer.setRepeats(true);
        }
            
        public synchronized void startAnimation() {
            timer.start();
        }

        public synchronized void stopAnimation() {
            timer.stop();
        }
        
    }
    
    public AgentFace() {
        stateIcons = new HashMap();
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        addActionListener(this);
    }

    public void
    loadImageForState(String st, URL imloc) {
        if (imloc != null) {
            try  {
                ImageIcon im = new ImageIcon(imloc);
                stateIcons.put(st,im);
                if (st.equals(myEmotion+"."+mySubstate)) {
                    setIcon(im);
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING,"Error loading image "+imloc.toString(),t);
            }
        } else {
            logger.log(Level.WARNING,"Unable to find "+imloc.toString());
        }
    }
            
    public void
    setName(String n) {
        myName = n;
        setToolTipText(myName);
        int i = n.lastIndexOf(" ");
        if (i != -1) {
            setText(n.substring(i+1,n.length()));
        } else {
            setText(myName);
        }
    }
    
    public String
    getName() {
        return myName;
    }
    
    public void
    setRegulationState(String st) {
        int dotloc = st.lastIndexOf(".");
        if (dotloc != -1) {
            myState = st.substring(0,dotloc);
            mySubstate = st.substring(dotloc+1,st.length());
        } else {
            myState = st;
        }
        setIcon((ImageIcon)stateIcons.get(myEmotion + "." + mySubstate));
        //updateBorder(lastBorderBoolean);
    }

    public void
    setEmotion(String em) {
        myEmotion = em;
        setIcon((ImageIcon)stateIcons.get(myEmotion + "." + mySubstate));
    }

    public String
    getRegulationState() {
        return myState+"."+mySubstate;
    }
    
    public void
    setColor(Color c) {
        myColor = c;
    }
    
    public Color
    getColor() {
        return myColor;
    }
    
    protected void
    doYourThing() {
        if (myActiveURL != null) {
            PageDisplayerThread.pageToDisplay(myActiveURL);
            myActiveURL = null;
            setRegulationState(myState+".inactive");
            setToolTipText(myName);
            myAttentionAnimator.stopAnimation();
        } else {
            try {
                PageDisplayerThread.pageToDisplay(System.getProperty("manyminds.adviceroot")+java.net.URLEncoder.encode(myName,"UTF-8")+"/HTML/"+"default.html");
            } catch (Throwable t) {
                logger.log(Level.WARNING,"Error diplaying page",t);
            }
        }
    }
    
    public void
    actionPerformed(ActionEvent ev) {
        doYourThing();
    }
    
    public void
    updateBorder(boolean isLeader) {
        lastBorderBoolean = isLeader;
        if (isLeader) {
            currentBorderColor = LEADER_COLOR;
            setBorder(BorderFactory.createMatteBorder(2,2,2,2,LEADER_COLOR));
        } else if (myState.equals("interrupt")) {
            currentBorderColor = INTERRUPT_COLOR;
            setBorder(BorderFactory.createMatteBorder(2,2,2,2,INTERRUPT_COLOR));
        } else if (myState.equals("alert")) {
            currentBorderColor = ALERT_COLOR;
            setBorder(BorderFactory.createMatteBorder(2,2,2,2,ALERT_COLOR));
        } else if (myState.equals("respond")) {
            currentBorderColor = RESPOND_COLOR;
            setBorder(BorderFactory.createMatteBorder(2,2,2,2,RESPOND_COLOR));
        }
    }
    public void
    giveAdvice(String summary, String url) {
        if (myState.equals("interrupt")) {
            try {
                ((AdvicePanel)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ADVICE_PANEL_ID)).addAdvice(summary, System.getProperty("manyminds.adviceroot")+java.net.URLEncoder.encode(myName,"UTF-8")+"/"+url, myName, myColor, (ImageIcon)stateIcons.get(myEmotion + ".inactive"));
            } catch (Throwable t) {
                logger.log(Level.WARNING,"Error changing state",t);
            }
        } else if (myState.equals("alert")) {
            setToolTipText(summary);
            if (!url.trim().equals("")) {
                if ((url.startsWith("http")) || (url.startsWith("<"))) {
                    myActiveURL = url;
                } else {
                    try {
                        myActiveURL = System.getProperty("manyminds.adviceroot")+java.net.URLEncoder.encode(myName,"UTF-8")+"/"+url;
                    } catch (Throwable t) {
                        logger.log(Level.WARNING,"Error activating advice",t);
                    }
                }
            } else {
                myActiveURL = null;
            }
            ((AdvicePanel)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ADVICE_PANEL_ID)).addAdvice(summary, myActiveURL, myName, myColor, (ImageIcon)stateIcons.get(myEmotion + ".inactive"));
        } else {
        }
    }
}