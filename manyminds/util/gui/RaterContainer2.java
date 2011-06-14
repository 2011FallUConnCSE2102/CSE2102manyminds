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
RaterContainer2
extends JPanel
implements ManyMindsConstants {
    
    private String myTag;
    private GridBagConstraints myGBC = new GridBagConstraints(GridBagConstraints.RELATIVE,GridBagConstraints.RELATIVE,GridBagConstraints.REMAINDER,1,1.0,0.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,new Insets(0,0,5,0),0,0);
    
    
    public
    RaterContainer2(String s) {
        super();
        myTag = s;
        setBorder(BorderFactory.createEmptyBorder());
        //setInsets(new Insets(0,0,0,0));
        setLayout(new GridBagLayout());
        setBackground(RATER_BACKGROUND);
        setForeground(RATER_TEXT);
        setOpaque(false);
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
            add(c,myGBC);
            return c;
        }
    }

    public void
    addRater(RaterPanel rp) {
        if (getComponentCount() == 0) {
            rp.setBorder(BorderFactory.createMatteBorder(RULE_WEIGHT, RULE_WEIGHT, RULE_WEIGHT, RULE_WEIGHT, RATER_STROKE));
        } else {
            rp.setBorder(BorderFactory.createMatteBorder(0, RULE_WEIGHT, RULE_WEIGHT, RULE_WEIGHT, RATER_STROKE));
        }
        add(rp,myGBC);
    }
    
    public boolean
    removeRater(String s) {
        boolean ret_val=false;
        Component[] raters = getComponents();
        for (int x = 0; x < raters.length; ++x) {
            Component c = raters[x];
            if (c instanceof RaterPanel) {
                if (((RaterPanel)c).getModel().getID().equals(s)) {
                    remove(c);
                    ret_val = true;
                }
            }
        }
        //setPreferredSize(new Dimension(RaterPanel.PANEL_WIDTH,RaterPanel.PANEL_HEIGHT*myPanel.getComponentCount()));
        //setViewportView(myPanel);
        return ret_val;
    }
}