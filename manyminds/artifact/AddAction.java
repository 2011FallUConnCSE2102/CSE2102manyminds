/*	File AddAction.java
 * =============================================================================
 * 
 * The Action object invoked when saving data.
 * 
 * Author Eric Eslinger
 * Copyright © 1999 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 14 Mar 00	EME	New today 
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
 package manyminds.artifact;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import manyminds.ManyMindsConstants;
import manyminds.datamodel.ManyMindsDocument;

public class
AddAction
extends AbstractAction
implements ManyMindsConstants, AddRemoveAction {
    private ManyMindsDocument trackingDoc;
    
    public
    AddAction(ManyMindsDocument t, String title) {
        super(title);
        trackingDoc = t;
    }
            
    public void
    actionPerformed(ActionEvent ae) {
        trackingDoc.setValue(Integer.toString(Integer.parseInt(trackingDoc.getValue()) + 1));
    }
    
    public void
    confirmActive() {
        setEnabled(true);
    }
	
}