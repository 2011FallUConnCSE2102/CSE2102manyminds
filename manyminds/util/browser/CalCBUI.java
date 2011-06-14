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

class CalCBUI extends BasicComboBoxUI implements CalCons {   

   CalForm form;
   CalBAB  bab;
   CalCP   cp;
   int     bordSize;

   public static ComponentUI createUI(JComponent c) {

      return new CalCBUI();
   }  


   void setForm(CalForm form) {
   
      this.form = form;
      if (bab != null) {
         bab.setForm(form);
      }
      if (cp != null) {
         cp.setForm(form);
         cp.getList().setBackground(CalColor.colors[form.textbgcolor]);
      }
      bordSize = (form.style == USE_CALPA_FLUSH) ? 3 : 2;
   }
   

   protected void installDefaults() {};
   public void uninstallUI(JComponent c) {};


   protected JButton createArrowButton() {

      bab = new CalBAB(SwingConstants.SOUTH, true);
      return bab;
   }


   protected ComboPopup createPopup() {

      cp = new CalCP(comboBox);
      cp.getAccessibleContext().setAccessibleParent(comboBox);
      return cp;
   }
   

   protected LayoutManager createLayoutManager() {

      return new CalCBLM();
   }
      
      
   public void paintCurrentValue(Graphics g,Rectangle bounds,boolean hasFocus) {

      ListCellRenderer renderer = comboBox.getRenderer();
      Component c;
      c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
      if (form.style == USE_CALPA_FLUSH) {
         c.setBackground(CalColor.colors[form.bgcolor]);
      } else {
         c.setBackground(CalColor.colors[form.textbgcolor]);
      }
      c.setFont(comboBox.getFont());
      if (comboBox.isEnabled()) {
         c.setForeground(CalColor.colors[form.textcolor]);
      } else {
         c.setForeground(CalColor.lightGray);
      }
      currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y, bounds.width, bounds.height);
   }


   public class CalCBLM extends BasicComboBoxUI.ComboBoxLayoutManager {

      public void layoutContainer(Container parent) {

         JComboBox cb = (JComboBox)parent;
         int w = cb.getWidth();
         int h = cb.getHeight();
         
         int buttw = (form.style == USE_CALPA_FLUSH) ? Math.max(h - 6, 18) : Math.max(h - 6, 16);
         int butth = h - (bordSize << 1);
         if (bab != null) {
            bab.setBounds(w - (bordSize + buttw), bordSize, buttw, butth);
         }
      }
   }


   JComboBox getCombo() {
      return comboBox;
   }
   
   protected void selectNextPossibleValue()     { super.selectNextPossibleValue();}
   protected void selectPreviousPossibleValue() { super.selectPreviousPossibleValue();}
   void togglePopup() {toggleOpenClose();}


   protected void installKeyboardActions() {

      super.installKeyboardActions();
      
      AbstractAction altKey = new AbstractAction() {

         public boolean isEnabled() {

            return getCombo().isEnabled();
         }

         public void actionPerformed(ActionEvent e) {

            togglePopup();
         }
      };
      
      getCombo().registerKeyboardAction(altKey,
                              KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_MASK),
                                                            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      getCombo().registerKeyboardAction(altKey,
                              KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_MASK),
                                                            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


      AbstractAction downKey = new AbstractAction() {

         public boolean isEnabled() {

            return getCombo().isEnabled();
         }

         public void actionPerformed(ActionEvent e) {

            if (isPopupVisible(getCombo())) {
               selectNextPossibleValue();
            } else {
               setPopupVisible(getCombo(), true);
            }
         }
      };

      getCombo().registerKeyboardAction(downKey, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0),
                                                            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

      AbstractAction upKey = new AbstractAction() {

         public boolean isEnabled() {

            return getCombo().isEnabled() && isPopupVisible(getCombo());
         }

         public void actionPerformed(ActionEvent e) {

            selectPreviousPossibleValue();
         }
      };

      getCombo().registerKeyboardAction(upKey, KeyStroke.getKeyStroke(KeyEvent.VK_UP,0),
                                                   JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

   }


   void releaseMemory() {
   
      form = null;
      bab  = null;
      cp   = null;
   }

}
