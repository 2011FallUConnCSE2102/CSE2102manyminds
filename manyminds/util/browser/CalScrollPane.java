/*================================================================================

                        Freeware License Agreement
                        ==========================

 This software is provided 'as-is', without any express or implied warranty.
 In no event will the authors be held liable for any damages arising from the
 use of this software.

 Permission is granted to anyone to use this software for any purpose, including
 commercial applications, and to alter it and redistribute it freely, subject to
 the following restrictions:

       1. The origin of this software must not be misrepresented; you must not
          claim that you wrote the original software. If you use this software
          in a product, an acknowledgment in the product documentation would be
          appreciated but is not required.

       2. Altered source versions must be plainly marked as such, and must not
          be misrepresented as being the original software.

       3. This notice may not be removed or altered from any source distribution.


 Author(s):
      Andrew Moulden 1998 (original copyright holder)

 Original package: CalHTMLPane html renderer, part of the calpa package

==================================================================================*/

package manyminds.util.browser;

import java.awt.*;
import javax.swing.JScrollPane;

public class CalScrollPane extends JScrollPane implements CalCons {
    
   CalViewer   viewer;
   CalFrameset frameset;
   boolean canCheckSize;

   public CalScrollPane(CalViewer viewer, int vsp, int hsp) {

      super(vsp, hsp);   
      this.viewer = viewer;
   }
      

   public void setBounds(int x, int y, int w, int h) {
   
      super.setBounds(x, y, w, h);      

      if ((!canCheckSize) ||(viewer.frameset != null)) {
         return;
      }

      boolean changed = false;
      
      if (w <= 40) {
         if (getVerticalScrollBarPolicy() != JScrollPane.VERTICAL_SCROLLBAR_NEVER) {
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            changed = true;
         }
      } else if ((getVerticalScrollBarPolicy() == JScrollPane.VERTICAL_SCROLLBAR_NEVER) &&
                                                                   (viewer.scrolling != V_NO)) {
         if (viewer.scrolling == V_AUTO) {
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         } else {
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         }
         changed = true;
      }
      if (h <= 40) {
         if (getHorizontalScrollBarPolicy() != JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            changed = true;
         }
      } else if ((getHorizontalScrollBarPolicy() == JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) &&
                                                                   (viewer.scrolling != V_NO)) {
         if (viewer.scrolling == V_YES) {
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         } else {
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         }
         changed = true;
      }
      
      if (changed) {
         invalidate();
         if (frameset == null) {
            getParent().validate();
         } else {
            frameset.dontCalcSpans = true;
            frameset.validate();
         }
      }

   }


   void setFrameset(CalFrameset frameset) {
   
      this.frameset = frameset;
   }
   

   public void paint(Graphics g) {
   
      super.paint(g);
      int w, h;
      if ((viewer != null) && (viewer.frameType == IFRAME) && (viewer.frameborder > 0)) {
         w = getSize().width;
         h = getSize().height;
         g.setColor(CalColor.colors[viewer.shadow]);
         g.drawLine(0, 0, w - 1, 0);
         g.drawLine(0, 0, 0, h - 1);
         g.setColor(Color.black);
         g.drawLine(1, 1, w - 2, 1);
         g.drawLine(1, 2, 1, h - 2);
         g.setColor(CalColor.colors[viewer.highlight]);
         g.drawLine(0, h - 1, w - 1, h - 1);
         g.drawLine(w - 1, 0, w - 1, h - 1);
         if (viewer.scrolling == V_NO) {
            g.setColor(CalColor.paleGray);
            g.drawLine(3, h - 3, w - 3, h - 3);
            g.drawLine(w - 3, 2, w - 3, h - 3);
         } else {
            g.setColor(Color.black);
            g.drawLine(2, h - 2, w - 2, h - 2);
            g.drawLine(w - 2, 2, w - 2, h - 2);
         }            
      } else if (viewer.isDialog) {
         w = getSize().width;
         h = getSize().height;
         g.setColor(CalColor.colors[viewer.outercolor]);
         g.drawLine(0, 0, w - 2, 0);
         g.drawLine(0, 0, 0, h - 2);
         g.setColor(CalColor.colors[viewer.highlight]);
         g.drawLine(1, 1, w - 3, 1);
         g.drawLine(1, 2, 1, h - 2);
         g.setColor(Color.black);
         g.drawLine(0, h - 1, w - 1, h - 1);
         g.drawLine(w - 1, 0, w - 1, h - 2);
         g.setColor(CalColor.colors[viewer.shadow]);
         g.drawLine(1, h - 2, w - 3, h - 2);
         g.drawLine(w - 2, 1, w - 2, h - 2);
      }
   }

}
