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
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.*;

public class CalSBUI extends BasicScrollBarUI implements CalCons {   

   CalForm form;
   CalBAB  incrBAB;
   CalBAB  decrBAB;
   int     thumb;
   int     thumbH;
   int     thumbS;
   int     trackC;
   
   public static ComponentUI createUI(JComponent c) {

      return new CalSBUI();
   }  


   void setForm(CalForm form) {
   
      CalBAB bab;
      Color c;
      this.form = form;
      if (incrBAB != null) {
         incrBAB.setForm(form);
      }
      if (decrBAB != null) {
         decrBAB.setForm(form);
      }
      if (form.fItem.uiscroll) {
         c = UIManager.getColor("ScrollBar.thumb");
         if (c != null) {
            thumb = CalColor.getColor(c.getRGB());
            c = UIManager.getColor("ScrollBar.thumbShadow");
            if (c != null) {
               thumbS = CalColor.getColor(c.getRGB());
            } else {
               thumbS = CalColor.shadowIndex;
            }
            c = UIManager.getColor("ScrollBar.thumbHighlight");
            if (c != null) {
               thumbH = CalColor.getColor(c.getRGB());
            } else {
               thumbH = CalColor.highlightIndex;
            }
         } else {
            thumb  = form.bgcolor;
            thumbH = form.innerhighlight;
            thumbS = form.shadow;
         }
      } else {
         thumb  = form.bgcolor;
         thumbH = form.innerhighlight;
         thumbS = form.shadow;
      }
      c = CalColor.colors[form.bgcolor];
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();
      r = 210 + (r / 16);
      g = 210 + (g / 16);
      b = 210 + (b / 16);
      trackC = CalColor.getColor((r << 16) | (g << 8) | b);
   }


   protected Dimension getMinimumThumbSize() { 
      return CalCons.MIN_THUMB_SIZE;
   }


   public Dimension getPreferredSize(JComponent c) {

      return new Dimension(13, 48);
   }
   
   
   protected void configureScrollBarColors() {
   }


   protected JButton createDecreaseButton(int orientation)  {

      decrBAB = new CalBAB(orientation, false);
      return decrBAB;
   }


   protected JButton createIncreaseButton(int orientation)  {

      incrBAB = new CalBAB(orientation, false);
      return incrBAB;
   }


   protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
   
      g.setColor(Color.white);
      g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);	
      g.setColor(CalColor.colors[form.bgcolor]);
      int x = trackBounds.x;
      int y = trackBounds.y;
      int h;
      if (form.style == USE_CALPA_FLUSH) {
         int w = trackBounds.width + x;
         h = trackBounds.height + y;
         g.setColor(CalColor.colors[trackC]);
         g.fillRect(x + 1, y, w - 1, h);
         g.setColor(Color.black);
         g.drawLine(x, y, x, h - 1);
         g.setColor(CalColor.imgLight);
         g.drawLine(x + 1, y, x + 1, h - 1);
         g.drawLine(x + 1, y, w - 1, y);
         if (scrollbar.getValue() != scrollbar.getMaximum()) {
            int z = thumbRect.y + thumbRect.height;
            g.drawLine(x + 2, z, w - 1, z);
         }
      } else {
         h = y + trackBounds.height;
         int x2 = x + trackBounds.width;
         int y2 = y + trackBounds.width;
         int n = 2;
         while (n < trackBounds.width) {
            g.drawLine(x + n, y, x2, y2 - n);
            n += 2;
         }
         while (y2 < h) {
            g.drawLine(x, y , x2, y2);
            y  += 2;
            y2 += 2;
         }
         n = 2;
         y2 -= 2;
         y  -= 2;
         while (n < trackBounds.width) {
            g.drawLine(x, y + n, x2 - n, y2);
            n += 2;
         }
      }
   }


   protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {

      if(thumbBounds.isEmpty() || !scrollbar.isEnabled())	{
         return;
      }

      int w = thumbBounds.width;
      int h = thumbBounds.height;		

      g.translate(thumbBounds.x, thumbBounds.y);
      if (form.style == USE_CALPA_FLUSH) {
         g.setColor(Color.black);
         g.drawRect(0, 0, w - 1, h - 1);
         g.setColor(CalColor.colors[thumbH]);
         g.drawRect(1, 1, w - 2, h - 3);
         g.setColor(CalColor.colors[thumb]);
         g.fillRect(2, 2, w - 3, h - 4);      
      } else {
         g.setColor(CalColor.colors[thumb]);
         g.fillRect(0, 0, w, h);
         g.setColor(CalColor.colors[thumbH]);
         g.drawLine(1, 1, w - 3, 1);
         g.drawLine(1, 2, 1, h - 2);
         g.setColor(Color.black);
         g.drawLine(0, h - 1, w - 1, h - 1);
         g.drawLine(w - 1, 0, w - 1, h - 2);
         g.setColor(CalColor.colors[thumbS]);
         g.drawLine(1, h - 2, w - 3, h - 2);
         g.drawLine(w - 2, 1, w - 2, h - 2);
      }
   }


}
