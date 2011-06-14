/*	File ArtifactSection.java
 * =============================================================================
 * 
 * An interface defining the important parts of a artifact section
 * 
 * Author Eric Eslinger
 * Copyright © 1999 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 05 Nov 99	CSS	New today (from QuestionPane.java).
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

import java.awt.Color;
import java.awt.event.ComponentListener;
import java.util.Collection;

public interface
ArtifactSection {
    public String getTabTitle();
    public String getShortTitle();
    public String getLongTitle();
    public void setShortTitle(String s);
    public void addComponentListener(ComponentListener cl);
    public ArtifactPage getSelectedChild();
    public String toXML();
    public Color getMainColor();
    public Collection getDocumentNames();
}// ArtifactSection