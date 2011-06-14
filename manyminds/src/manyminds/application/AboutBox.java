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
 package manyminds.src.manyminds.application;

//
//	File:	AboutBox.java
//

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class AboutBox extends JFrame
                      implements ActionListener
{
    protected JButton okButton;

    public AboutBox() {
	super();
        this.getContentPane().setLayout(new BorderLayout(15, 15));
        this.setFont(new Font ("SansSerif", Font.BOLD, 14));

        JTextArea aboutText = new JTextArea ("Inquiry Island v1.1\n"+
            "ManyMinds, Copyright (C) 1998-2002 Regents of the University of California\n"+
            "Manyminds comes with ABSOLUTELY NO WARRANTY\n"+
            "This is free software, and you are welcome to redistribute it under certain conditions\n"+
            "See documentation for details");
        aboutText.setEnabled(false);
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        textPanel.add(aboutText);
        this.getContentPane().add (textPanel, BorderLayout.NORTH);
		
        okButton = new JButton("OK");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add (okButton);
        okButton.addActionListener(this);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
    }
	
    public void actionPerformed(ActionEvent newEvent) {
        setVisible(false);
    }	
	
}