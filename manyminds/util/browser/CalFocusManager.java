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

public class CalFocusManager extends DefaultFocusManager {
   
   public void processKeyEvent(Component focusedComponent, KeyEvent e) {

      CalForm form = null;
      boolean isControl = ((e.getModifiers() & ActionEvent.CTRL_MASK) > 0);
      
      if(e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t') {
         if(focusedComponent instanceof JComponent) {
            JComponent fc = (JComponent) focusedComponent;

            if (fc.isManagingFocus()) {
               if ((fc instanceof CalHTMLPane) || (fc instanceof CalViewer) || (!isControl)) {
                  return;
               }
            }

            if(e.getID() != KeyEvent.KEY_PRESSED){
               e.consume();
               return;
            }
            
            Object obj = fc.getParent();
            if (obj != null) {
               if (obj instanceof CalForm) {
                  form = (CalForm)obj;
               } else if (obj instanceof JViewport) {
                  JViewport vp = (JViewport)obj;
                  obj = vp.getParent().getParent();
                  if (obj instanceof CalForm) {
                     form = (CalForm)obj;
                  }
               }
               if ((form != null) && (form.getParent() != null)) {
                  if (form.getParent() instanceof CalViewer) {
                     CalViewer viewer = (CalViewer)form.getParent();
                     viewer.currTabIndex = form.viewTabIndex;
                     viewer.requestFocus();
                     if (viewer.pane != null) {
                        viewer.pane.tabBackForward(viewer, e, isControl);
                     }
                  }
                  e.consume();
                  return;
               }
            }
            
            //if we reach here then we must be outside the CalViewer, so:
            super.processKeyEvent(fc, e);
         }
      }
   }
}
