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
import javax.swing.plaf.*;

public class CalESP extends JScrollPane {   

   CalForm form;
   ISB     isb;
   
   public CalESP(Component c) {
   
      super(c, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);      
   }


   public CalESP(Component c, CalForm form) {
   
      super(c, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      this.form = form;
      if (isb != null) {
         CalSBUI sbui = (CalSBUI)isb.getUI();
         if (sbui != null) {
            sbui.setForm(form);
         }
      }
      
   }
   

   void setForm(CalForm form) {
   
      this.form = form;
      if (isb != null) {
         CalSBUI sbui = (CalSBUI)isb.getUI();
         if (sbui != null) {
            sbui.setForm(form);
         }
      }
   }
   
   
   public JScrollBar createVerticalScrollBar() {
   
      isb = new ISB(JScrollBar.VERTICAL);
      return isb;
   }


   class ISB extends CalESB {

      public ISB(int orientation) {

         super(orientation);
      }

      public int getUnitIncrement(int direction) {
      
         JViewport vp = getViewport();
         if ((vp != null) && (vp.getView() instanceof Scrollable)) {
            Scrollable view = (Scrollable)(vp.getView());
            Rectangle vr = vp.getViewRect();
            return view.getScrollableUnitIncrement(vr, getOrientation(), direction);
         } else {
            return super.getUnitIncrement(direction);
         }
      }


      public int getBlockIncrement(int direction) {

         JViewport vp = getViewport();
         if (vp == null) {
            return super.getBlockIncrement(direction);
         } else if (vp.getView() instanceof Scrollable) {
            Scrollable view = (Scrollable)(vp.getView());
            Rectangle vr = vp.getViewRect();
            return view.getScrollableBlockIncrement(vr, getOrientation(), direction);
         } else if (getOrientation() == VERTICAL) {
            return vp.getExtentSize().width;
         } else {
            return vp.getExtentSize().height;
         }
      }
   }


   public void removeNotify() {
   
      super.removeNotify();
      form = null;
      isb  = null;
   }   

}
