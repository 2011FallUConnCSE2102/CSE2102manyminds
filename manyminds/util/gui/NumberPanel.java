/*	File NumberPanel.java
 * =============================================================================
 * 
 * A panel containing a numeral, and a pair of buttons for increasing or
 * decreasing that numeral
 *
 * Author Eric Eslinger
 * Copyright © 2000 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 08 Jun 00	EME	New today
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;

import manyminds.ManyMindsConstants;

public
class NumberPanel extends JPanel implements ActionListener {
    //private JTextField numberField;
    private JLabel numberField;
    private JButton increaseButton;
    private JButton decreaseButton;
    private List listenerList;
    private int currentValue = 0;
    private int action = 0;
    private int myMin,myMax;
    
    public static final int NONE = 0;
    public static final int INCREASING = 1;
    public static final int DECREASING = 2;
    public static final int SETTING = 4;
    
    
    public static final int HEIGHT = 25;
    public static final int WIDTH = 50;

    private static class UpArrowIcon implements Icon, UIResource, Serializable, ManyMindsConstants
    {
	public void paintIcon( Component c, Graphics g, int x, int y )
	{
	    JButton b = (JButton) c;
	    ButtonModel model = b.getModel();

	    g.translate( x, y );

	    if ( !model.isEnabled() ) {
	        g.setColor( BASE_STROKE );
	    } 
	    else
	    {
                g.setColor( HIGHLIGHT_STROKE );
	    }
            
            g.drawLine( 0, 5, 7, 5 );
            g.drawLine( 1, 4, 6, 4 );
            g.drawLine( 2, 3, 5, 3 );
            g.drawLine( 3, 2, 4, 2 );

	    g.translate( -x, -y );
	}

	public int getIconWidth() { return 8; }

	public int getIconHeight() { return 8; }

    } // End class MenuArrowIcon
    
    private static class DownArrowIcon implements Icon, UIResource, Serializable, ManyMindsConstants
    {
	public void paintIcon( Component c, Graphics g, int x, int y )
	{
	    JButton b = (JButton) c;
	    ButtonModel model = b.getModel();

	    g.translate( x, y );

	    if ( !model.isEnabled() ) {
	        g.setColor( BASE_STROKE );
	    } 
	    else
	    {
                g.setColor( HIGHLIGHT_STROKE );
	    }
            
            g.drawLine( 0, 1, 7, 1 );
            g.drawLine( 1, 2, 6, 2 );
            g.drawLine( 2, 3, 5, 3 );
            g.drawLine( 3, 4, 4, 4 );

	    g.translate( -x, -y );
	}

	public int getIconWidth() { return 8; }

	public int getIconHeight() { return 8; }

    } // End class MenuArrowIcon
    
    
    public
    NumberPanel(int min, int max, int current) {
        myMin = min;
        myMax = max;
        currentValue = current;
        listenerList = new LinkedList();
        //numberField = new JTextField();
        numberField = new JLabel(Integer.toString(currentValue),SwingConstants.RIGHT);
        increaseButton = new JButton();
        increaseButton.setIcon(new UpArrowIcon());
        decreaseButton = new JButton();
        decreaseButton.setIcon(new DownArrowIcon());
        increaseButton.setPreferredSize(new Dimension(WIDTH/2,HEIGHT/2));
        decreaseButton.setPreferredSize(new Dimension(WIDTH/2,HEIGHT/2));
        numberField.setPreferredSize(new Dimension(WIDTH/2,HEIGHT));
        increaseButton.setMaximumSize(new Dimension(WIDTH/2,HEIGHT/2));
        decreaseButton.setMaximumSize(new Dimension(WIDTH/2,HEIGHT/2));
        numberField.setMaximumSize(new Dimension(WIDTH/2,HEIGHT));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 2;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,0,0,0);
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(numberField,c);
        c.gridheight = 1;
        c.gridx = 1;
        c.weightx=0.0;
        c.weighty=0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHEAST;
        add(increaseButton,c);
        c.gridy = 1;
        c.anchor = GridBagConstraints.SOUTHEAST;
        add(decreaseButton,c);
        increaseButton.addActionListener(this);
        decreaseButton.addActionListener(this);
        setValue(current);
    }
    
    public void
    actionPerformed(ActionEvent ae) {
        if (ae.getSource() == increaseButton) {
            setValue(currentValue + 1,INCREASING);
        } else if (ae.getSource() == decreaseButton) {
            setValue(currentValue - 1,DECREASING); 
        }
    }
    
    public void
    setEnabled(boolean b) {
        decreaseButton.setEnabled(b);
        increaseButton.setEnabled(b);
        numberField.setEnabled(b);
    }
    
    public void
    addChangeListener(ChangeListener cl) {
        listenerList.add(cl);
    }
    
    public void
    removeChangeListener(ChangeListener cl) {
        listenerList.remove(cl);
    }
    
    public void
    fireChange() {
        Iterator it = listenerList.iterator();
        ChangeEvent e = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }
    
    public int
    getValue() {
        return currentValue;
    }
    
    public void
    setValue(int x) {
        setValue(x,SETTING);
    }
    
    protected void
    setValue(int val, int a) {
        if (val < myMin) {
            setValue(myMin,a);
        } else if (val > myMax) {
            setValue(myMax,a);
        } else {
            action = a;
            currentValue = val;
            if (val == myMin) {
                decreaseButton.setEnabled(false);
                increaseButton.setEnabled(true);
            } else if (val == myMax) {
                decreaseButton.setEnabled(true);
                increaseButton.setEnabled(false);
            } else {
                decreaseButton.setEnabled(true);
                increaseButton.setEnabled(true);
            }
            numberField.setText(Integer.toString(currentValue));
            fireChange();
        }
    }
    
    public int
    getAction() {
        return action;
    }
    
}