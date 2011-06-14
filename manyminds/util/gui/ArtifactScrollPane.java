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

import javax.swing.JScrollPane;

/**
 * An extension of JScrollPane that makes use of the OrientedScrollable interface
 *	implementing components by asking them what their scrollbar preferences are.
 * 
 * @author	Chris Schneider
 */
public
class ArtifactScrollPane extends JScrollPane
{
    /**
        * creates a scroll pane that tries to ask its component about scroll bar policy
        *
        * @param	view	component to display in viewport
        */
    public
    ArtifactScrollPane(Component view)
    {
        super(view);
        if (view instanceof OrientedScrollable) {
            setHorizontalScrollBarPolicy(((OrientedScrollable)view).getHorizontalScrollBarPolicy());
            setVerticalScrollBarPolicy(((OrientedScrollable)view).getVerticalScrollBarPolicy());
        }
    } // ArtifactScrollPane
    
    public
    ArtifactScrollPane() {
        super();
    }
    
} // ArtifactScrollPane

