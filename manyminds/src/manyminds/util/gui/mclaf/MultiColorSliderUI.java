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
 package manyminds.src.manyminds.util.gui.mclaf;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;
import java.io.Serializable; 
import javax.swing.plaf.*;
import manyminds.artifact.*;
import manyminds.*;

public class 
MultiColorSliderUI
extends BasicSliderUI
implements ManyMindsConstants {

    public
    MultiColorSliderUI(JSlider x) {
        super(x);
    }

    protected void scrollDueToClickInTrack(int i) {
        scrollByUnit(i);
    }

    public static ComponentUI createUI( JComponent x ) {
   /*     return new com.apple.mrj.swing.MacSliderUI((JSlider)x) {
            protected void scrollDueToClickInTrack(int i) {
                scrollByUnit(i);
            }
        };*/
        return new BasicSliderUI((JSlider)x);
    }
}