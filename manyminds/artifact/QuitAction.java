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
 package manyminds.artifact;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import manyminds.ManyMindsConstants;

public class
QuitAction
extends AbstractAction
implements ManyMindsConstants {

    public
    QuitAction() {
        super("Quit");
    }
    
    public void
    actionPerformed(ActionEvent ae) {
/*        int i = JOptionPane.showConfirmDialog(null, "Save your work?","Save",JOptionPane.YES_NO_CANCEL_OPTION);
        if (i == JOptionPane.NO_OPTION) {
            (new Thread() {
                public void run() {
                    try {
                        DataServerDispatch.getDispatch().autosaveData();
                    } catch (Throwable t) {
                        Toolkit.getDefaultToolkit().beep();
                        Logger.getLogger("manyminds.datamodel").log(Level.SEVERE,"Error autosaving data!",t);
                    } finally {
                        System.exit(0);
                    }
                }
            }).start();
        } else if (i == JOptionPane.YES_OPTION) {
            (new Thread() {
                public void run() {
                    try {
                        DataServerDispatch.getDispatch().saveAllData();
                        System.exit(0);
                    } catch (Throwable t) {
                        Toolkit.getDefaultToolkit().beep();
                        Logger.getLogger("manyminds.datamodel").log(Level.SEVERE,"Error saving data!",t);
                    }
                }
            }).start();
        } else if (i == JOptionPane.CANCEL_OPTION) {
        }*/
        System.exit(0);
    }
}