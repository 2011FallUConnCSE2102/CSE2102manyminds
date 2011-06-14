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
 
package manyminds.webkitcomponent_src.manyminds.util.browser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

public class
WebViewController
extends JToolBar {
    
    private WebKitComponent myWebKitComponent;
    
    private static final ImageIcon leftArrow =  new ImageIcon(ClassLoader.getSystemClassLoader().getResource("manyminds/util/browser/leftArrow.gif"));
    private static final ImageIcon rightArrow =  new ImageIcon(ClassLoader.getSystemClassLoader().getResource("manyminds/util/browser/rightArrow.gif"));
    private static final ImageIcon homeIcon =  new ImageIcon(ClassLoader.getSystemClassLoader().getResource("manyminds/util/browser/Island.gif"));

    
    public 
    WebViewController(WebKitComponent wkc) {
        myWebKitComponent = wkc;
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 1.0;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.WEST;
        
        
        JButton goBack = new JButton(/*"Back",*/leftArrow);
        goBack.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                myWebKitComponent.goBack();
            }
        });

        goBack.setVerticalTextPosition(SwingConstants.BOTTOM);
        goBack.setHorizontalTextPosition(SwingConstants.CENTER);
        goBack.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton goForward = new JButton(/*"Forward",*/rightArrow);
        goForward.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                myWebKitComponent.goForward();
            }
        });
        
        goForward.setVerticalTextPosition(SwingConstants.BOTTOM);
        goForward.setHorizontalTextPosition(SwingConstants.CENTER);
        goForward.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton home = new JButton(/*"Home",*/homeIcon);
        home.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                myWebKitComponent.loadURL(System.getProperty("manyminds.allagentpage"));
            }
        });
        
        home.setVerticalTextPosition(SwingConstants.BOTTOM);
        home.setHorizontalTextPosition(SwingConstants.CENTER);
        home.setHorizontalAlignment(SwingConstants.CENTER);
        
        setFloatable(false);
        add(goBack,c);
        //add(reload);
        add(home,c);
        add(goForward,c);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        add(Box.createHorizontalGlue(),c);
    }
}