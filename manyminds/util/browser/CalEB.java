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

class CalEB extends JButton implements CalCons {   
   
   CalForm form;
   boolean cFocusPainted;
   CalBUI  bui;

   CalEB(CalForm form, CalViewer viewer, CalFonts f, CalHTMLPreferences pref, int displaySize) {
   
      super();
      this.form = form;
      setOpaque(false);
      setBorder(null);
      setUI(new CalBUI());
      bui = (CalBUI)getUI();
      if (!form.fItem.disabled) {
         setRequestFocusEnabled(true);
      } else {
         setEnabled(false);
         setRequestFocusEnabled(false);
      }
      if (bui != null) {
         bui.setState(form, viewer, f, pref, displaySize);
         setPreferredSize(bui.buttonPreferredSize);
      }
      if (form.fItem.accesskey != END) {
         if (form.fItem.tagType == LABEL) {
            if (form.fItem.forTarget != null) {
               setMnemonic(form.fItem.accesskey);
               addFocusListener(new ForTargetListener());
            }
         } else {
            setMnemonic(form.fItem.accesskey);
         }
      }
      cFocusPainted = (form.fItem.tagType == LABEL) ? false : true;
   }
   
        
   public void updateUI() {   
   }


   private class ForTargetListener extends FocusAdapter {
   
      public void focusGained(FocusEvent e) {

         String s;
         if (form.view.forms != null) {
            for (int i=0; i<form.view.formIndex; i++) {
               s = form.view.forms[i].fItem.id;
               if ((s != null) && (form.fItem.forTarget.equals(s))) {
                  if (form.view.forms[i].comp != null) {
                     form.view.forms[i].comp.requestFocus();
                  }
               }
            }
         }    
      }
   }


   public boolean isFocusPainted() {
   
      return cFocusPainted;
   }


   public void removeNotify() {
   
      super.removeNotify();
      if (bui != null) {
         bui.releaseMemory();
      }
      form = null;
      bui = null;
   }   
}
