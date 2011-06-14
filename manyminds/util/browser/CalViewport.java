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
import javax.swing.JViewport;
import javax.swing.JScrollPane;
//import javax.swing.border.Border;

public class CalViewport extends JViewport implements CalCons {
    
   CalViewer viewer;
      
   CalViewport(CalViewer viewer) {
   
      this.viewer = viewer;
   }
      
   public void setBounds(int x, int y, int w, int h) {
   
      super.setBounds(x, y, w, h);
      if ((viewer.doc != null) && (viewer.view != null) && (!viewer.isDialog)) {
         if (viewer.doc.docType == D_HTML) {
            w = viewer.getLiningWidth();
            h = viewer.getLiningHeight();
            if ((w != viewer.view.linedWidth) || ((h != viewer.view.viewportHeight) &&
                                                                            viewer.view.relineOnHChange)) {
               //System.out.println("Calling reline doc from viewport for:" + viewer.name);
               viewer.checkThreadLiner();
               viewer.viewMarkPos = getViewPosition();
               viewer.viewSet = false;
               viewer.connectionMode = CONNECTED;
               viewer.imagesFinished = false;
               viewer.pane.statusUpdate(CONNECTED, viewer.doc.docURL, viewer.nesting, null);
               viewer.relineDocument(w, h, viewer.doc.state == PARSED ? true : false, true);
            }
         } else {
            viewer.setPreferredSize(getViewerSizeWhenFrameset());
            viewer.validate();
         }
      } else if (viewer.frameset != null) {
         viewer.setPreferredSize(getViewerSizeWhenFrameset());
         viewer.validate();
      }   
   }


   Dimension getViewerSizeWhenFrameset() {

      Dimension d = viewer.sp.getSize();
      Insets ins = viewer.sp.getInsets();
      if (ins != null) {
         d.width  -= (ins.left + ins.right);
         d.height -= (ins.top + ins.bottom);
      }
      if (viewer.sp.getViewportBorder() != null) {
         ins = viewer.sp.getViewportBorder().getBorderInsets(viewer.sp);
         d.width  -= (ins.left + ins.right);
         d.height -= (ins.top + ins.bottom);
      }      
      
      return d;
   }
         
      
   void setDocViewPosition(Point p) {
   
      if (viewer.view != null) {
         Rectangle r = getViewRect();
         int w = viewer.view.finalWidth;
         int h = viewer.view.linedHeight + (viewer.marginwidth << 1);
         p.x = Math.max(0, Math.min(p.x, w - r.width));
         p.y = Math.max(0, Math.min(p.y, h - r.height));
         setViewPosition(p);
      }
   }

}
