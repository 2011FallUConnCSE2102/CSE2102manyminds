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
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.io.Serializable;
import javax.swing.plaf.metal.*;
import manyminds.*;

public class
MultiColorIconFactory
implements ManyMindsConstants {
    
    public static Icon getHorizontalSliderThumbIcon() {
      // don't cache these, bumps don't get updated otherwise
	return new HorizontalSliderThumbIcon();
    }

    public static Icon getVerticalSliderThumbIcon() {
      // don't cache these, bumps don't get updated otherwise
	return new VerticalSliderThumbIcon();
    }

    
    private static class
    VerticalSliderThumbIcon
    implements Icon, Serializable, UIResource {
      //  protected static MetalBumps controlBumps;
       // protected static MetalBumps primaryBumps;
    
        public VerticalSliderThumbIcon() {}
    
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            JSlider slider = (JSlider)c;
                
            g.translate( x, y );
    
            // Draw the frame
            g.setColor( RATER_STROKE );
    
            g.drawLine(  1,0  ,  8,0  ); // top
            g.drawLine(  0,1  ,  0,13 ); // left
            g.drawLine(  1,14 ,  8,14 ); // bottom
            g.drawLine(  9,1  , 15,7  ); // top slant
            g.drawLine(  9,13 , 15,7  ); // bottom slant
    
            // Fill in the background
            g.setColor( RATER_BACKGROUND );
    
            g.fillRect( 1,1, 8, 13 );
    
            g.drawLine(  9,2 ,  9,12 );
            g.drawLine( 10,3 , 10,11 );
            g.drawLine( 11,4 , 11,10 );
            g.drawLine( 12,5 , 12,9 );
            g.drawLine( 13,6 , 13,8 );
            g.drawLine( 14,7 , 14,7 );


            //do the texture
            
            g.setColor(RATER_STROKE);
            for (int i = 0; i < 14; i+=4) {
                for (int j = 0; j < 13; j+=4) {
                    g.drawLine( j, i, j, i );
                    g.drawLine( j+2, i+2, j+2, i+2);
                }
            }
    
            g.translate( -x, -y );
        }
    
        public int getIconWidth() {
            return 16;
        }
    
        public int getIconHeight() {
            return 15;
        }
    }

    private static class HorizontalSliderThumbIcon implements Icon, Serializable, UIResource {
        //protected static MetalBumps controlBumps;
        //protected static MetalBumps primaryBumps;
    
        public HorizontalSliderThumbIcon() {
           /* controlBumps = new MetalBumps( 10, 6,
                                    MetalLookAndFeel.getControlHighlight(),
                                    MetalLookAndFeel.getControlInfo(),
                                    MetalLookAndFeel.getControl() );        
            primaryBumps = new MetalBumps( 10, 6,
                                    MetalLookAndFeel.getPrimaryControl(),
                                    MetalLookAndFeel.getPrimaryControlDarkShadow(),
                                    MetalLookAndFeel.getPrimaryControlShadow() );       */ 
        }
    
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            JSlider slider = (JSlider)c;
    
            g.translate( x, y );
            
            // Draw the frame
            g.setColor( Color.black );
    
            g.drawLine(  1,0  , 14,0 );  // top
            g.drawLine(  0,1  ,  0,8 );  // left
            g.drawLine( 14,1  , 14,8 );  // right
            g.drawLine(  1,9  ,  7,15 ); // left slant
            g.drawLine(  7,15 , 14,8 );  // right slant
    
            // Fill in the background
            g.setColor( RATER_STROKE );
            g.fillRect( 1,1, 13, 8 );
            
            g.drawLine( 2,9  , 12,9 );
            g.drawLine( 3,10 , 11,10 );
            g.drawLine( 4,11 , 10,11 );
            g.drawLine( 5,12 ,  9,12 );
            g.drawLine( 6,13 ,  8,13 );
            g.drawLine( 7,14 ,  7,14 );
            
            g.setColor(Color.black);
            for (int i = 0; i < 8; i+=4) {
                for (int j = 2; j < 13; j += 4) {
                    g.drawLine(j,i , j+1,i+1);
                    g.drawLine(j+2,i+2 , j+3,i+3);
                }
            }
            // Draw the highlight
           /* if ( slider.isEnabled() ) {
                g.setColor( slider.hasFocus() ? MetalLookAndFeel.getPrimaryControl()
                            : MetalLookAndFeel.getControlHighlight() );
                g.drawLine( 1, 1, 13, 1 );
                g.drawLine( 1, 1, 1, 8 );
            }*/
                
            g.translate( -x, -y );
        }
    
        public int getIconWidth() {
            return 15;
        }
    
        public int getIconHeight() {
            return 16;
        }
    }
}