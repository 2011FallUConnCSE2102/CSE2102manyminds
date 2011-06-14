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
import javax.swing.border.*;

class CalButton extends JButton implements CalCons, MouseListener, MouseMotionListener {   
   
   private CalViewer viewer;
   private CalView   specialView;
   private boolean   isEnabled;
   private boolean   pressed;
   private boolean   hasFocus;
   private int       width;
   private int       height;
           int       bordWidth;
           int       bordHeight;
           int       bordTop;
           int       paintOffset;


   CalButton(CalForm form, CalViewer viewer, CalFonts f, CalHTMLPreferences pref, int displaySize) {

      super();
      this.viewer    = viewer;
      setOpaque(true);
      if (form.fItem.bgcolor > 0) {
         setBackground(CalColor.colors[form.fItem.bgcolor]);
      }
      bordWidth  = 0;
      bordHeight = 0;
      Border border = getBorder();
      int leftIns = 0;
      if (border != null) {
         Insets ins = border.getBorderInsets(this);
         leftIns    = ins.left;
         bordWidth  = ins.left + ins.right;
         bordHeight = ins.top + ins.bottom;
         bordTop    = ins.top;
      }
      specialView = new CalView(form.doc, true);
      specialView.charPos = form.fItem.charStart;
      specialView.displaySize = displaySize;
      specialView.viewportHeight = viewer.view.viewportHeight;
      CalMetrics m = new CalMetrics(f, specialView, viewer, pref);
      CalStackFont sf = new CalStackFont(viewer.rootFontFamily,
                                         viewer.rootFontStyle, 0, viewer.rootFontSize, 0);
      m.setState(form.doc, viewer.view.docWidth, sf, displaySize, 1, form.bgcolor, true);
      m.calcMinMaxWidth(form.fItem.startPos, form.fItem.endPos,
                                         viewer.rootFontStyle, form.bgcolor, form.fItem.charStart);
      CalLiner liner = new CalLiner(viewer, f, form.doc, specialView, pref, 0);
      liner.reset(m.cMaxW, sf, form.fItem.startPos, form.fItem.endPos, V_CENTER,
                                            form.fItem.marginwidth + leftIns, form.bgcolor, false);
      liner.lineDocument();
      specialView.linedHeight = liner.cHeightSoFar;
      specialView.finalWidth  = liner.cFinalWidth - (form.fItem.marginwidth + leftIns);
      specialView.finalizeArrays();
      setMargin(new Insets(0,0,0,0));
      try {
         paintOffset = ((Integer)UIManager.get("Button.textShiftOffset")).intValue();
      } catch (Exception e) {
         System.err.println("Can't find textOffset");
         paintOffset = 0;
      }
      width  = m.cMaxW + (form.fItem.marginwidth << 1) + bordWidth;
      height = specialView.linedHeight + (form.fItem.marginheight << 1) + bordHeight;
      bordTop += form.fItem.marginheight;   //for faster painting 
      setPreferredSize(new Dimension(width, height));
      if (!form.fItem.disabled) {
         setRequestFocusEnabled(true);
         isEnabled = true;
         if (paintOffset > 0) {
            addMouseListener(this);
            addMouseMotionListener(this);
         }
      } else {
         setEnabled(false);
         isEnabled = false;
         setRequestFocusEnabled(false);
      }
   }


   public void paint(Graphics g) {
   
      super.paint(g);
      if ((specialView != null) && (specialView.lines != null) && (specialView.paintableLines > 0)) {
         int n = (pressed && isEnabled) ? paintOffset : 0;
         g.translate(n, n);
         viewer.paintLineArray(g, specialView, 0, getHeight(), bordTop, 0, specialView.paintableLines);
         g.translate(-n, -n);
      }
   }


   public void mousePressed(MouseEvent e) {
   
      pressed = true;
      repaint();
   }
   
   public void mouseReleased(MouseEvent e) {
   
      pressed = false;
      repaint();
   }
   
   public void mouseDragged(MouseEvent e) {
              
      Point p = e.getPoint();
      if ((p.x < 0) || (p.x >= width) || (p.y < 0) || (p.y >= height)) {
         if (pressed) {
            pressed = false;
            repaint();
         }
      } else {
         if (!pressed) {
            pressed = true;
            repaint();
         }
      }
   }


   public void removeNotify() {
   
      super.removeNotify();
      viewer      = null;
      specialView = null;
   }   

   public void mouseClicked(MouseEvent e) {}
   public void mouseEntered(MouseEvent e) {}
   public void mouseExited (MouseEvent e) {}
   public void mouseMoved  (MouseEvent e) {}   
}
