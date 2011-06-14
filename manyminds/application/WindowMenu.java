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
 package manyminds.application;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class
WindowMenu
extends JMenu
implements ListDataListener {
    protected static DefaultListModel myRegisteredFrames = new DefaultListModel();
    
    private class
    MyMenuItem
    extends
    JMenuItem {
        private JFrame myFrame = null;
        public MyMenuItem(JFrame jf) {
            super();
            myFrame = jf;
            setText(myFrame.getTitle());
            setAction(new AbstractAction(myFrame.getTitle()) {
                public void
                actionPerformed(ActionEvent ae) {
                    myFrame.setVisible(true);
                    myFrame.toFront();
                    myFrame.show();
                }
            });
        }
    }
    
    public
    WindowMenu() {
        super("Window");
        synchronized (myRegisteredFrames) {
            for (int x = 0; x < myRegisteredFrames.getSize(); ++x) {
                add(new MyMenuItem(((JFrame)myRegisteredFrames.getElementAt(x))));
            }
        }
        myRegisteredFrames.addListDataListener(this);
    }
    
    public static void
    addFrame(JFrame jf) {
        synchronized (myRegisteredFrames) {
            myRegisteredFrames.addElement(jf);
        }
    }
    
    public static void
    removeFrame(JFrame jf) {
        synchronized (myRegisteredFrames) {
            myRegisteredFrames.removeElement(jf);
        }
    }
    
    public static Component
    getFocusOwner() {
        synchronized (myRegisteredFrames) {
            for (int i = 0; i < myRegisteredFrames.getSize(); ++i) {
                Component c = ((JFrame)myRegisteredFrames.getElementAt(i)).getFocusOwner();
                if (c != null) {
                    return c;
                }
            }
        }
        return null;
    }
    
    public void
    contentsChanged(ListDataEvent lde) {}
    
    public void
    intervalAdded(ListDataEvent lde) {
        synchronized (myRegisteredFrames) {
            for (int x = lde.getIndex0(); x <= lde.getIndex1(); ++x) {
                add(new MyMenuItem(((JFrame)myRegisteredFrames.getElementAt(x))));
            }
        }
    }
    
    public void
    intervalRemoved(ListDataEvent lde) {
        synchronized (myRegisteredFrames) {
            for (int x = lde.getIndex0(); x <= lde.getIndex1(); ++x) {
                remove(x);
            }
        }
    }
}