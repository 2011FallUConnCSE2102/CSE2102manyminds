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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.SwingConstants;

class CalBAB extends JButton implements CalCons, MouseListener {

   CalForm form;
   boolean pressed;
   boolean isCombo;
   int     direction;
   
   CalBAB(int direction, boolean isCombo) {
   
      super();
      this.isCombo = isCombo;
      this.direction = direction;
      setOpaque(false);
      pressed = false;
   }
   

   void setForm(CalForm form) {
   
      this.form = form;
      if (form.fItem.disabled) {
         setEnabled(false);
      } else {
         setEnabled(true);
         addMouseListener(this);
      }
   }

   public Dimension getMinimumSize() {

      return CalCons.MIN_THUMB_SIZE;
   }

   public Dimension getPreferredSize() {

      return CalCons.MIN_THUMB_SIZE;
   }

   public void mousePressed(MouseEvent e) {
   
      pressed  = true;
      repaint();
   }
   
   public void mouseReleased(MouseEvent e) {
   
      pressed = false;
      repaint();
   }
   
   
   public void paint(Graphics g) {

      int x, y, w, h;

      w = getSize().width;
      h = getSize().height;
      if (pressed && isEnabled() && (form.style == USE_CALPA_THREEDEE)) {
         x = 1;
         y = 1;
      } else {
         x = 0;
         y = 0;
      }
      if (isCombo) {
         x += (w >> 1) - 4;
         y += (h >> 1) - 2;
      }
      if (form != null) {
         if (form.style == USE_CALPA_FLUSH) {
            if (isCombo) {
               g.setColor(CalColor.colors[form.bgcolor]);
               g.fillRect(0, 0, w, h);
               g.setColor(CalColor.colors[form.outercolor]);
               g.drawRect(x - 5, y - 6, 16, 15);
               g.setColor(CalColor.colors[form.innercolor]);
               g.drawRect(x - 3, y - 4, 12, 11);
            } else {
               g.setColor(Color.black);
               g.drawRect(0, 0, w - 1, h - 1);
               g.setColor(CalColor.colors[form.innerhighlight]);
               if (direction == SwingConstants.NORTH) {
                  g.drawRect(1, 0, w - 2, h - 2);
                  g.setColor(CalColor.colors[form.bgcolor]);
                  g.fillRect(2, 1, w - 3, h - 3);
               } else {
                  g.drawRect(1, 1, w - 2, h - 2);
                  g.setColor(CalColor.colors[form.bgcolor]);
                  g.fillRect(2, 2, w - 3, h - 3);
               }
            }
         } else {
            g.setColor(CalColor.colors[form.bgcolor]);
            g.fillRect(0, 0, w, h);
            if (pressed) {
               g.setColor(CalColor.colors[form.shadow]);
               g.drawRect(0, 0, w - 1, h - 1);
            } else {
               g.setColor(CalColor.colors[form.innerhighlight]);
               g.drawLine(1, 1, w - 3, 1);
               g.drawLine(1, 2, 1, h - 2);
               g.setColor(Color.black);
               g.drawLine(0, h - 1, w - 1, h - 1);
               g.drawLine(w - 1, 0, w - 1, h - 2);
               g.setColor(CalColor.colors[form.shadow]);
               g.drawLine(1, h - 2, w - 3, h - 2);
               g.drawLine(w - 2, 1, w - 2, h - 2);
            }
         }
         g.setColor(CalColor.colors[form.arrowcolor]);
         g.translate(x, y);
         if (isCombo) {
            g.drawLine(0, 0, 6, 0);
            g.drawLine(1, 1, 5, 1);
            g.drawLine(2, 2, 4, 2);
            g.drawLine(3, 3, 3, 3);
         } else {
            if (direction == SwingConstants.NORTH) {
               g.drawLine(6, 5, 6, 5);
               g.drawLine(5, 6, 7, 6);
               g.drawLine(4, 7, 8, 7);   
            } else {
               g.drawLine(4, 5, 8, 5);
               g.drawLine(5, 6, 7, 6);
               g.drawLine(6, 7, 6, 7);
            }
         }
         g.translate(-x, -y);
      }
   }

   public boolean isFocusTraversable() {
   
      return false;
   }


   //public void removeNotify() {
   
   //   super.removeNotify();
      //form = null;
   //}   

   public int getDirection() { return direction;}
   public void setDirection(int dir) { direction = dir;}
   public void updateUI() {}
   public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);}
   public void requestFocus() {}
   public void mouseClicked(MouseEvent e) {}
   public void mouseEntered(MouseEvent e) {}
   public void mouseExited(MouseEvent e) {}

}

