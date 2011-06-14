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
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import javax.swing.plaf.*;
import java.io.Serializable; 
import javax.swing.plaf.metal.*;
import javax.swing.plaf.basic.*;
import manyminds.artifact.*;
import manyminds.debug.*;
import manyminds.*;

//MultiColorToolTipUI will intelligently put linebreaks in your tooltips. It doesn't like HTML tooltip, but
//tough.

public class 
MultiColorToolTipUI
extends ToolTipUI
implements ManyMindsConstants {

    private Font smallFont;			    	     
    private JToolTip tip;
    protected Logger logger = Logger.getLogger("manyminds.artifact");

    private Hashtable map = new Hashtable();

    public MultiColorToolTipUI() {
        super();
    }

    public static ComponentUI createUI(JComponent c) {
        return new MultiColorToolTipUI();
    }

    public void installUI(JComponent c) {
	installDefaults(c);
	tip = (JToolTip)c;
	//smallFont = SMALL_FONT;
        map.put(TextAttribute.FONT,LARGE_FONT);
        //map.put(TextAttribute.SIZE, new Float(14));
        //map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
    }

    protected void installDefaults(JComponent c){
	LookAndFeel.installColorsAndFont(c, "ToolTip.background",
					 "ToolTip.foreground",
					 "ToolTip.font");
        LookAndFeel.installBorder(c, "ToolTip.border");
    }
    
    protected void uninstallDefaults(JComponent c){
	LookAndFeel.uninstallBorder(c);
    }
    
    public void paint(Graphics g, JComponent c) {

        Dimension size = c.getSize();
        g.setColor(c.getBackground());
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(c.getForeground());

	String tipText = ((JToolTip)c).getTipText().replace('\n',' ');
	if ((tipText == null) || ("".equals(tipText))) {
	    tipText = " ";
	}
        
        logger.log(Level.INFO,"Showing ToolTip",tipText);
        
        AttributedString tipString = new AttributedString(tipText,map);
        AttributedCharacterIterator tipIterator = tipString.getIterator();
    	int paragraphStart = tipIterator.getBeginIndex();
        int paragraphEnd = tipIterator.getEndIndex();
        Graphics2D graphics2D = (Graphics2D) g;
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(tipIterator,
                                                               graphics2D.getFontRenderContext());


        float formatWidth = (float) size.width;
        float drawPosY = 0;

        lineMeasurer.setPosition(paragraphStart);

        while (lineMeasurer.getPosition() < paragraphEnd) {

            TextLayout layout = lineMeasurer.nextLayout(formatWidth);
            drawPosY += layout.getAscent();
            float drawPosX;
            if (layout.isLeftToRight()) {
                drawPosX = 1;
            }
            else {
                drawPosX = formatWidth - layout.getAdvance();
            }
            layout.draw(graphics2D, drawPosX, drawPosY);
            drawPosY += layout.getDescent() + layout.getLeading();
        }
    }

    public Dimension getPreferredSize(JComponent c) {
        tip = (JToolTip)c;
        JRootPane jrp = tip.getComponent().getRootPane();
        Dimension d = jrp.getGlassPane().getSize();

        d.height = 0;
        d.width = (int)(d.width * 0.85);
        
	String tipText = ((JToolTip)c).getTipText().replace('\n',' ');
	if ((tipText == null) || ("".equals(tipText))) {
	    tipText = " ";
	}
        AttributedString tipString = new AttributedString(tipText,map);
        AttributedCharacterIterator tipIterator = tipString.getIterator();
    	int paragraphStart = tipIterator.getBeginIndex();
        int paragraphEnd = tipIterator.getEndIndex();
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(tipIterator,
                                                               new FontRenderContext(null,false,false));
        
        float formatWidth = (float) d.width;
        float drawPosY = 0;
        int lines = 0;
        
        lineMeasurer.setPosition(paragraphStart);
        TextLayout layout = null;
        while (lineMeasurer.getPosition() < paragraphEnd) {
            layout = lineMeasurer.nextLayout(formatWidth);
            drawPosY += layout.getAscent() + layout.getDescent() + layout.getLeading();
            ++lines;
        }
        
        if (lines == 1) {
            d.width = (int)layout.getAdvance();
        }
        d.height = (int)drawPosY;
        
	Insets insets = c.getInsets();
        d.width += insets.left + insets.right;
        d.height += insets.top + insets.bottom;
        return d;
    }
    
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }
    
    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }
   
}