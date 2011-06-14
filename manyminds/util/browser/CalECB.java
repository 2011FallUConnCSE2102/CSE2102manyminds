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
import javax.swing.border.*;

class CalECB extends JComboBox implements CalCons {   
   
   CalForm form;
   CalCBUI cbui;

   CalECB(CalForm form) {
   
      super();
      this.form = form;
      setEditable(false);
      setOpaque(false);
      setLightWeightPopupEnabled(true);
      setRenderer(new CalCL(form));
      setBorder(null);
      setUI(new CalCBUI());
      cbui = (CalCBUI)getUI();
      if (cbui != null) {
         cbui.setForm(form);
      }
   }
   
        
   public void updateUI() {   
   }


   public void paint(Graphics g) {
   
      super.paint(g);
      int w = getWidth();
      int h = getHeight();
      int z;
      if (form.style == USE_CALPA_FLUSH) {
         g.setColor(CalColor.colors[form.outercolor]);
         g.drawLine(1, 0, w - 2, 0);
         g.drawLine(0, 1, 0, h - 2);
         g.drawLine(w - 1, 1, w - 1, h - 2);
         g.drawLine(1, h - 1, w - 2, h - 1);
         g.setColor(CalColor.colors[form.bgcolor]);
         g.drawRect(1, 1, w - 3, h - 3);
         g.setColor(CalColor.colors[form.innercolor]);
         g.drawRect(2, 2, w - 5, h - 5);
         if (hasFocus()) {
            g.setColor(CalColor.colors[form.outercolor]);
            //g.setColor(CalColor.colors[form.bgcolor]);
            z = (w - 4) % 4;
            z = ((z == 0) || (z == 1)) ? 3 : 2;
            for (int i=z, j=w-3, k=h-3; i<j; i+=4) {
               g.drawLine(i, 2, i+1, 2);
               g.drawLine(i, k, i+1, k);
            }
            z = (h - 6) % 4;
            boolean extra = false;
            if (z == 0) {
               z = 4;
            } else {
               if (z == 3) {
                  extra = true;
               }
               z = 5;
            }
            for (int i=z, j=h-4, k=w-3; i<j; i+=4) {
               g.drawLine(2, i, 2, i+1);
               g.drawLine(k, i, k, i+1);
            }
            if (extra) {
               z = h - 4;
               g.drawLine(2, z, 2, z);
               g.drawLine(w - 3, z, w - 3, z);
            }
         }
      } else {
         g.setColor(CalColor.colors[form.shadow]);
         g.drawLine(0, 0, w - 2, 0);
         g.drawLine(0, 0, 0, h - 2);
         g.setColor(Color.black);
         g.drawLine(1, 1, w - 3, 1);
         g.drawLine(1, 2, 1, h - 2);
         g.setColor(CalColor.colors[form.highlight]);
         g.drawLine(0, h - 1, w - 1, h - 1);
         g.drawLine(w - 1, 0, w - 1, h - 2);
         g.setColor(CalColor.colors[form.bgcolor]);
         g.drawLine(w - 2, 1, w - 2, h - 2);
         if (form.paintMode == 0) {
            g.drawLine(1, h - 2, w - 3, h - 2);
         } else {
            g.drawLine(w - Math.max(h - 4, 18), h - 2, w - 3, h - 2);
         }
         if (hasFocus()) {
            int i, k;
            g.setColor(Color.white);
            for (i=2; i<w-1; i+=3) {
               g.drawLine(i, 0, i, 0);
            }
            for (i=2; i<h-1; i+=3) {
               g.drawLine(0, i, 0, i);
            }
            g.setColor(Color.black);
            for (i=1, k=h-1; i<w-1; i+=3) {
               g.drawLine(i, k, i, k);
            }
            for (i=1, k=w-1; i<h-1; i+=3) {
               g.drawLine(k, i, k, i);
            }
         }
      }      
   }


   public void removeNotify() {
   
      super.removeNotify();
      if (cbui != null) {
         cbui.releaseMemory();
      }
      form = null;
      cbui = null;
   }   


   //protected void finalize() {
      
   //   System.out.println("Finalize called for CalECB");
   //}
   
}
