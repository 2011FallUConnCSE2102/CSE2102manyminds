/*	File RaterContainer.java
 * =============================================================================
 * 
 * A lightweight JPanel that is optimized to hold RaterPanels
 * 
 * Author Eric Eslinger
 * Copyright © 1998-2000 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * 23 May 00	EME	New Today
 *
 * =============================================================================
 */

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
 package manyminds.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.border.*;
import manyminds.util.gui.*;
import manyminds.*;

public class
RaterContainer
extends JScrollPane
implements ManyMindsConstants {
    
    private String myTag;
    private JPanel myPanel;
    private GridBagConstraints myGBC = new GridBagConstraints(GridBagConstraints.RELATIVE,GridBagConstraints.RELATIVE,GridBagConstraints.REMAINDER,1,1.0,0.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,new Insets(0,0,5,0),0,0);
    
    //private JScrollPane myScrollPane;
    
    public
    RaterContainer(String s) {
        super();
        myPanel = new JPanel();
        myTag = s;
        setViewportView(myPanel);
        setBorder(BorderFactory.createEmptyBorder());
        //setInsets(new Insets(0,0,0,0));
        myPanel.setLayout(new GridBagLayout());
        myPanel.setBackground(RATER_BACKGROUND);
        myPanel.setForeground(RATER_TEXT);
        myPanel.setOpaque(false);
        setOpaque(false);
        //this = new JScrollPane(myPanel);
        this.getViewport().setOpaque(false);
        this.setOpaque(false);
        this.setViewportBorder(BorderFactory.createEmptyBorder());
        resetScrollPane();
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //super.add(this);
        //myPanel.add(Box.createHorizontalStrut(RaterPanel.SLIDER_WIDTH));
    }
    
    private void
    resetScrollPane() {
        repaint();
    }
    
    public String
    getTag() {
        return myTag;
    }

    public Component
    add(Component c) {
        if (c instanceof RaterPanel) {
            addRater((RaterPanel)c);
            return c;
        } else {
            myPanel.add(c,myGBC);
            return c;
        }
    }

    public void
    addRater(RaterPanel rp) {
        if (myPanel.getComponentCount() == 0) {
            rp.setBorder(BorderFactory.createMatteBorder(RULE_WEIGHT, RULE_WEIGHT, RULE_WEIGHT, RULE_WEIGHT, RATER_STROKE));
        } else {
            rp.setBorder(BorderFactory.createMatteBorder(0, RULE_WEIGHT, RULE_WEIGHT, RULE_WEIGHT, RATER_STROKE));
        }
        myPanel.add(rp,myGBC);
        resetScrollPane();
    }
    
    public boolean
    removeRater(String s) {
        boolean ret_val=false;
        Component[] raters = myPanel.getComponents();
        for (int x = 0; x < raters.length; ++x) {
            Component c = raters[x];
            if (c instanceof RaterPanel) {
                if (((RaterPanel)c).getModel().getID().equals(s)) {
                    myPanel.remove(c);
                    ret_val = true;
                }
            }
        }
        myPanel.setPreferredSize(new Dimension(RaterPanel.PANEL_WIDTH,RaterPanel.PANEL_HEIGHT*myPanel.getComponentCount()));
        setPreferredSize(new Dimension(RaterPanel.PANEL_WIDTH,RaterPanel.PANEL_HEIGHT*myPanel.getComponentCount()));
        //setViewportView(myPanel);
        resetScrollPane();
        return ret_val;
    }
}