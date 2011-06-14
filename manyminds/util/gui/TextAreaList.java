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
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.Document;

import manyminds.ManyMindsConstants;

public class
TextAreaList
extends JScrollPane
implements ListDataListener {
    private ListModel myListModel;
    private Container myView;

    public void
    setModel(ListModel lm) {
        if (myListModel != null) {
            myListModel.removeListDataListener(this);
        }
        myListModel = lm;
        myView.removeAll();
        for (int i = 0; i < myListModel.getSize(); ++i) {
            JComponent c = packageObject(myListModel.getElementAt(i));
            myView.add(c);
        }
        myListModel.addListDataListener(this);
        reborderAll();
    }
    
    public
    TextAreaList(ListModel lm) {
        super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        if (lm == null) {
            lm = new DefaultListModel();
        }
        myView = new Box(BoxLayout.Y_AXIS);
       /* myView.setLayout(new GridBagLayout());
        myGBC.weightx=1.0;
        myGBC.weighty=0.0;
        myGBC.fill=GridBagConstraints.HORIZONTAL;
        myGBC.gridwidth=GridBagConstraints.REMAINDER;
        myGBC.gridx=GridBagConstraints.RELATIVE;
        myGBC.gridy=GridBagConstraints.RELATIVE;
        myGBC.anchor=GridBagConstraints.NORTH;*/
        setModel(lm);
        setViewportView(myView);
    }
 
    private JComponent
    packageObject(Object o) {
        if (o instanceof Document) {
            Document d = (Document)o;
            ArtifactTextArea ta = new ArtifactTextArea(d);
            ta.setBorder(BorderFactory.createEmptyBorder());
            JScrollPane jsp = new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            return jsp;
        } else {
            return new JPanel();
        }
    } 
 
    public
    TextAreaList() {
        this(null);
    }

    public void
    contentsChanged(ListDataEvent e) {
    }

    protected void
    reborderAll() {
        Component[] comps = myView.getComponents();
        for (int i = 0; i < comps.length; ++i) {
            ((JComponent)comps[i]).setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ManyMindsConstants.BASE_STROKE), Integer.toString(i + 1)));
        }
    }

    public void
    intervalAdded(ListDataEvent e) {
        for (int i = e.getIndex0(); i <= e.getIndex1(); ++i) {
            JComponent c = packageObject(myListModel.getElementAt(i));
            myView.add(c);
            reborderAll();
        }
    }

    public void
    intervalRemoved(ListDataEvent e) {
        for (int x = e.getIndex0(); x <= e.getIndex1(); ++x) {
            myView.remove(x);
            reborderAll();
        }
    }
}