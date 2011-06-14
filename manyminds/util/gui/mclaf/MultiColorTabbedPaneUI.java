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
 package manyminds.util.gui.mclaf;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.*;
import java.io.Serializable; 
import javax.swing.plaf.metal.*;
import manyminds.artifact.*;
import manyminds.*;
import manyminds.util.gui.*;

public class 
MultiColorTabbedPaneUI
extends MetalTabbedPaneUI
implements ManyMindsConstants {
    public static ComponentUI createUI( JComponent x ) {
        if (x instanceof ArtifactTabbedPane) {
            return new MultiColorTabbedPaneUI();
        } else {
            return new MetalTabbedPaneUI();
        }
    }  
        
    protected void paintTabBackground( Graphics g, int tabPlacement,
                                       int tabIndex, int x, int y, int w, int h, boolean isSelected ) {
        if (isSelected) {
            Component c = tabPane.getComponentAt(tabIndex);
            if (c instanceof ArtifactSection) {
                selectColor = ((ArtifactSection)c).getMainColor();
            }
        }
        super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
    }
    
    public void
    installUI(JComponent c) {
        super.installUI(c);
        if (tabPane instanceof ArtifactTabbedPane) {
            ArtifactTabbedPane stp = (ArtifactTabbedPane)tabPane;
            lightHighlight = BASE_STROKE;
            highlight = BASE_STROKE;
            selectHighlight = BASE_STROKE;
            darkShadow = BASE_STROKE;
            shadow = BASE_STROKE;
            contentBorderInsets = new Insets(1,1,1,1);
        }
    }
    
    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
                                                int selectedIndex,
                                                int x, int y, int w, int h) { 
        if (tabPlacement != BOTTOM || selectedIndex < 0 ||
            (rects[selectedIndex].y - 1 > h)) {
            g.setColor(BASE_STROKE);
            g.drawLine(x, y+h-1, x+w-1, y+h-1);
        } else {
            Rectangle selRect = rects[selectedIndex];
            g.setColor(BASE_STROKE);
            g.drawLine(x, y+h-1, selRect.x, y+h-1);
            g.setColor(tabPane.getBackgroundAt(selectedIndex));
            //g.setColor(Color.black);
            g.drawLine(selRect.x, y+h-1, selRect.x + selRect.width + 1, y+h-1);
            if (selRect.x + selRect.width < x + w - 2) {
                g.setColor(BASE_STROKE);
                g.drawLine(selRect.x + selRect.width, y+h-1, x+w-1, y+h-1);
            } 
        }
    }
    protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                       Rectangle[] rects, int tabIndex, 
                                       Rectangle iconRect, Rectangle textRect,
                                       boolean isSelected) {}
}
