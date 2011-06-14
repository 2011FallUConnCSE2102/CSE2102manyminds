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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class CalRBUI extends BasicRadioButtonUI implements CalCons {

   CalForm form;
   int     type;

   void setForm(CalForm form) {
   
      this.form = form;
      type = form.fItem.type;
   }
   
   
   protected void installDefaults(AbstractButton b){

      super.installDefaults(b);
   }


   protected void uninstallDefaults(AbstractButton b){

      super.uninstallDefaults(b);
   }


   public synchronized void paint(Graphics g, JComponent c) {

      if (type == V_CHECKBOX) {
         paintCheckBox(g, c);
      } else {
         paintRadioButton(g, c);
      }
   }


   private void paintCheckBox(Graphics g, JComponent c) {
   
      int m, n, x, y;
      AbstractButton b = (AbstractButton)c;
      ButtonModel model = b.getModel();
      g.setColor(CalColor.colors[form.bgc]);
      g.fillRect(0, 0, 19, 18);
      if (form.style == USE_CALPA_FLUSH) {
         if (form.fItem.bordercolor > 0) {
            g.setColor(CalColor.colors[form.textbgcolor]);
         } else {
            g.setColor(CalColor.colors[form.bgcolor]);
         }         
         g.fillRect(2, 2, 13, 13);
         g.setColor(CalColor.colors[form.outercolor]);
         g.drawRect(2, 2, 14, 14);
         g.setColor(CalColor.colors[form.innercolor]);
         g.drawRect(3, 3, 12, 12);
         if (model.isSelected()) {
            x = 5;
            m = 6;
            g.setColor(CalColor.colors[form.textcolor]);
            g.fillRect(x+1, m+2, 2, 5);
            g.drawLine(x+7, m, x+3, m+4);
            g.drawLine(x+7, m+1, x+3, m+5);
         }
         if (b.hasFocus()) {
            g.setColor(CalColor.colors[form.bgc]);
            for (int i=4, k=16; i<16; i+=3) {
               g.drawLine(i, 2, i, 2);
               g.drawLine(i, k, i, k);
            }
            for (int i=4, k=16; i<16; i+=3) {
               g.drawLine(2, i, 2, i);
               g.drawLine(k, i, k, i);
            }
         }
      } else {
         x = 4;
         m = (form.paintMode == 0) ? 6: 7;
         g.setColor(CalColor.colors[form.bgcolor]);
         g.fillRect(x, m-1, 10, 10);
         g.setColor(CalColor.colors[form.textbgcolor]);
         g.fillRect(x, m-1, 9, 9);
         if (model.isSelected()) {
            g.setColor(CalColor.colors[form.textcolor]);
            g.fillRect(x+1, m+2, 2, 5);
            g.drawLine(x+7, m, x+3, m+4);
            g.drawLine(x+7, m+1, x+3, m+5);
         }
         //border
         n = 14 - form.paintMode;
         y = 15;
         x -= 2;
         m -= 2;
         g.setColor(CalColor.colors[form.shadow]);
         g.drawLine(x, m-1, n, m-1);
         g.drawLine(x, m-1, x, y);
         g.setColor(Color.black);
         g.drawLine(x+1, m, n-1, m);
         g.drawLine(x+1, m+1, x+1, y-1);
         g.setColor(CalColor.colors[form.highlight]);
         g.drawLine(x, y, n, y);
         g.drawLine(n, m-1, n, y);
         if (b.hasFocus()) {
            m = 16 - form.paintMode;
            n = 2 + form.paintMode;
            g.setColor(Color.black);
            g.drawLine(0, n, 0, n);
            g.drawLine(m, n, m, n);
            g.drawLine(0, m, 0, m);
            g.drawLine(m, m, m, m);
         }
      }
   }


   private void paintRadioButton(Graphics g, JComponent c) {
   
      int m, n, x, y;
      AbstractButton b = (AbstractButton)c;
      ButtonModel model = b.getModel();
      if (form.style == USE_CALPA_FLUSH) {
         g.translate(3, 4);
         g.setColor(CalColor.colors[form.bgcolor]);
         g.fillRect(2, 1, 8, 10);
         g.fillRect(1, 2, 10, 8);
         g.setColor(CalColor.colors[form.outercolor]);
         g.drawLine(4, 0, 7, 0);
         g.drawLine(2, 1, 3, 1);
         g.drawLine(8, 1, 9, 1);
         g.drawLine(1, 2, 1, 3);
         g.drawLine(10, 2, 10, 3);
         g.drawLine(0, 4, 0, 7);
         g.drawLine(11, 4, 11, 7);
         g.drawLine(1, 8, 1, 9);
         g.drawLine(1, 2, 2, 1);
         g.drawLine(10, 8, 10, 9);
         g.drawLine(2, 10, 3, 10);
         g.drawLine(8, 10, 9, 10);
         g.drawLine(2, 10, 3, 10);
         g.drawLine(4, 11, 7, 11);
         if (model.isSelected()) {
            g.setColor(CalColor.colors[form.textbgcolor]);
            g.fillRect(4, 2, 4, 8);
            g.fillRect(2, 4, 8, 4);
            g.fillRect(3, 3, 6, 6);
            g.setColor(CalColor.colors[form.textcolor]);
            g.fillRect(5, 4, 2, 4);
            g.fillRect(4, 5, 4, 2);
         }
         g.translate(-3, -4);
      } else {
         g.setColor(CalColor.colors[form.textbgcolor]);
         g.translate(3, 4);
         g.fillRect(2, 2, 8, 8);      
         if (model.isSelected()) {
            g.setColor(Color.black);
            g.fillRect(4, 5, 4, 2);
            g.fillRect(5, 4, 2, 4);
         }
         g.setColor(CalColor.colors[form.shadow]);
         g.drawLine(0, 3, 3, 0);
         g.drawLine(0, 4, 4, 0);
         g.drawLine(0, 5, 0, 7);
         g.drawLine(5, 0, 7, 0);
         g.drawLine(8, 1, 9, 2);
         g.drawLine(3, 10, 3, 10);
         g.setColor(Color.black);
         g.drawLine(4, 1, 7, 1);
         //g.drawLine(8, 2, 8, 2);
         g.drawLine(1, 4, 3, 2);
         g.drawLine(1, 5, 1, 8);
         //g.drawLine(2, 8, 2, 9);
         g.drawLine(2, 9, 2, 9);
         g.setColor(CalColor.colors[form.highlight]);
         g.drawLine(3, 9, 3, 9);
         g.drawLine(4, 10, 8, 10);
         g.drawLine(8, 9, 9, 9);
         g.drawLine(9, 8, 9, 8);
         g.drawLine(10, 4, 10, 8);
         g.drawLine(9, 3, 9, 3);
         g.translate(-3, -4);
      }
      if (b.hasFocus()) {
         g.setColor(Color.black);
         g.drawLine(2, 3, 2, 3);
         g.drawLine(15, 3, 15, 3);
         g.drawLine(2, 16, 2, 16);
         g.drawLine(15, 16, 15, 16);
      }
   }

   
   public Dimension getPreferredSize(JComponent c) {

      return CalCons.CHECKBOX_SIZE;
   }
}
