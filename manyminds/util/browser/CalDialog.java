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
import java.awt.event.*;
import javax.swing.*;

class CalDialog extends CalViewer {

   String cMessageName;
   boolean propertiesSet;
   int messageW;
   int messageH;
   int messageX;
   int messageY;

   CalDialog(CalHTMLPane pane, CalHTMLPreferences pref) {   

      super(pane, pref, null, null, 0);
      nesting = 0;
      frameborder = 1;
      marginwidth  = 0;
      marginheight = 0;
      sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      sp.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      name = "_dialog";
      isDialog = true;
      bgcolor = CalColor.controlIndex;
   }
   

   void showDialogMessage(String message, String messageName, int x, int y, int w, int h) {
   
      checkParser();
      checkThreadLiner();
      doc = null;
      messageX = x;
      messageY = y;
      messageW = w;
      messageH = h;
      cMessageName = messageName;
      if (h <= 0) {
         h = 200;   //any number will do, because we'll change it afterward
      }
      if (cMessageName != null) {
         doc = CalHTMLManager.getDialogMessage(cMessageName);
      }
      if (doc == null) {
         doc = new CalDoc(CalHTMLManager.getDummyURL(), this);
         parser = new CalFP(null, doc, f, pref, message);
         parser.setIsDialog();
         parser.start();
      } else {
         cMessageName = null;   //stops doc being re-cached
      }
      keepPainting = true;
      removeAnyChildren();
      connectionMode = CONNECTED;
      propertiesSet = false;
      relineDocument(messageW, h, true, true);
   }


   void relineDocument(int w, int h, boolean a, boolean b) {
   
      super.relineDocument(w, h, a, b);
   }
   
   
   //called by the Timer
   public void actionPerformed(ActionEvent e) {
   
      if (!propertiesSet) {
         if ((doc == null) || (doc.state == PARSE_FAILED)) {
            timer.stop();
            if (doc.state == PARSE_FAILED) {
               checkParser();
               checkThreadLiner();
            }
         }
         if ((doc.state != PARSED) || (view.lineState != LINED)) {
            return;
         } else {
            if (doc.bgcolor != this.bgcolor) {
               highlight = doc.highlight;
               shadow    = doc.shadow;
               outercolor = doc.bgcolor;
               viewport.setBackground(CalColor.colors[doc.bgcolor]);
               sp.setBackground(CalColor.colors[doc.bgcolor]);
               setBackground(CalColor.colors[doc.bgcolor]);
            } else {
               highlight = CalColor.highlightIndex;;
               shadow    = CalColor.shadowIndex;
               outercolor= CalColor.controlIndex;
               sp.setBackground(CalColor.colors[CalColor.controlIndex]);
               viewport.setBackground(CalColor.colors[CalColor.controlIndex]);
               setBackground(CalColor.colors[CalColor.controlIndex]);
            }
            messageW = Math.max(messageW, view.finalWidth);
            messageW = Math.min(messageW, pane.viewer.getWidth());
            messageH = Math.max(messageH, view.linedHeight + (marginheight << 1));
            messageH = Math.min(messageH, pane.viewer.getHeight());
            setPreferredSize(new Dimension(messageW, messageH));
            pane.setDialogBounds(pane.getWidth(), pane.getHeight(),
                                         messageX, messageY, messageW + 4, messageH + 4);
            viewport.invalidate();
            sp.validate();
            sp.setVisible(true);
            requestFocus();
            currTabIndex = -1;
            propertiesSet = true;
         }
      }
      if (!keepPainting) {
         timer.stop();
         if (cMessageName != null) {
            CalHTMLManager.addDialogMessage(doc, cMessageName);
            cMessageName = null;
         }
         parser = null;
         repaint();
         return;
      } else {
         repaint();
      }
   }


   void setBaseParameters(int width, int height) {
    
      width  = pane.viewer.getWidth();
      height = pane.viewer.getHeight();
      
      if (pref.optimizeDisplay == NO_OPTIMIZATION) {
         view.displaySize = S_LARGE;
      } else {
         if (width < pref.frameWidth[S_SMALL]) {
            view.displaySize = (pref.optimizeDisplay == OPTIMIZE_ALL) ? S_SMALL : S_LARGE;
         } else if (width < pref.frameWidth[S_MEDIUM]) {
            view.displaySize = (pref.optimizeDisplay == OPTIMIZE_ALL) ? S_MEDIUM : S_LARGE;
         } else {
            view.displaySize = S_LARGE;
         }
      }
      rootFontSize   = pref.fontVals[S_SMALL];
      rootFontFamily = pref.famVals[S_SMALL];
      rootFontStyle  = pref.styleVals[S_SMALL];
      f.checkFont(rootFontFamily, rootFontStyle, rootFontSize);
   }


   void stopAllProcesses() {

      checkParser();
      checkThreadLiner();
      if (timer.isRunning()) {
         timer.stop();
      }
   }
   
}
