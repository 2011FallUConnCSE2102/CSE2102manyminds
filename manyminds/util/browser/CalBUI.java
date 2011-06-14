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
import javax.swing.plaf.basic.*;
import javax.swing.*;

public class CalBUI extends BasicButtonUI implements CalCons {

   private CalViewer viewer;
   private CalView   specialView;
   private CalForm   form;
   private int       width;
   private int       height;
   private String    text;
   private int       baseline;
   private int       accesskeyX;
   private int       accesskeyY;
   private int       accesskeyW;
   private int       paintOffset;
   private int       leftM;
   private int       topM;
   private int       textH;
   private int       textW;
   private int       bordersize;
   private int       bordercolor;
   private boolean   isLabel;
           Dimension buttonPreferredSize;

   protected void installDefaults(AbstractButton b) {

      b.setOpaque(false);
      b.setMargin(new Insets(0,0,0,0));
      buttonPreferredSize = new Dimension(0, 0);
   }


   public void uninstallUI(JComponent c) {
   }


   void setState(CalForm form, CalViewer viewer, CalFonts f, CalHTMLPreferences pref, int displaySize) {
   
      this.form = form;
      CalFormItem fItem = form.fItem;
      this.viewer = viewer;
      bordersize = 0;
      isLabel    = false;
      if ((fItem.tagType == BUTTON) || (fItem.tagType == LABEL)) {
         if (fItem.tagType == BUTTON) {
            bordersize = 3;
         } else {
            isLabel = true;
            if (fItem.border > 0) {
               bordersize = fItem.border;
               if (fItem.bordercolor > 0) {
                  bordercolor = fItem.bordercolor;
               } else if (fItem.highlight > 0) {
                  bordercolor = fItem.highlight;
               } else if (fItem.shadow > 0) {
                  bordercolor = fItem.shadow;
               } else {
                  bordercolor = CalColor.blackIndex;
               }
            }
         }            
         topM = fItem.marginheight + bordersize;
         specialView = new CalView(form.doc, true);
         specialView.charPos = fItem.charStart;
         specialView.displaySize = displaySize;
         specialView.viewportHeight = viewer.view.viewportHeight;
         CalMetrics m = new CalMetrics(f, specialView, viewer, pref);
         CalStackFont sf = new CalStackFont(viewer.rootFontFamily,
                                                viewer.rootFontStyle, 0, viewer.rootFontSize, 0);
         m.setState(form.doc, viewer.view.docWidth, sf, displaySize, 1, form.bgcolor, true);
         m.calcMinMaxWidth(fItem.startPos, fItem.endPos,
                                                viewer.rootFontStyle, form.bgcolor, fItem.charStart);
         CalLiner liner = new CalLiner(viewer, f, form.doc, specialView, pref, 0);
         liner.reset(m.cMaxW, sf, fItem.startPos, fItem.endPos, V_CENTER,
                                                 fItem.marginwidth + bordersize, form.bgcolor, false);
         liner.lineDocument();
         specialView.linedHeight = liner.cHeightSoFar;
         specialView.finalWidth  = liner.cFinalWidth - (fItem.marginwidth + bordersize);
         specialView.finalizeArrays();
         width  = m.cMaxW + (fItem.marginwidth << 1) + (bordersize << 1);
         height = specialView.linedHeight + (fItem.marginheight << 1) + (bordersize << 1);
      } else {
         if (fItem.initValue != null) {
            text = fItem.initValue;
         } else {
            if (fItem.type == V_SUBMIT) {
               text = "Submit";
            }  else if (fItem.type == V_RESET) {
               text = "Reset";
            }
         }
         if (text == null) {
            text = "?";
         }
         if (fItem.marginheight >= 0) {
            topM = fItem.marginheight;
         } else {
            topM = viewer.isDialog ? 1 : 2;
         }
         textW = form.textfm.stringWidth(text);
         textH = form.textfm.getHeight();
         baseline = 2 + topM + textH - form.textfm.getDescent() - 1;
         if (fItem.marginwidth >= 0) {
            leftM = fItem.marginwidth;
         } else {
            leftM = (textW * 400) / 2000;
         }
         width = textW + (leftM << 1) + 6;
         height = textH + (topM << 1) + 4;
         accesskeyX = -1;
         if (fItem.accesskey != END) {
            int index, n;
            n = fItem.accesskey;
            if ((n > 96) && (n < 123)) {
               n = ('A' + (n - 'a'));
            }
            index = text.indexOf(n);
            if ((index == -1) && ((n > 64) && (n < 91))) {
               n = ('a' + (n - 'A'));
               index = text.indexOf(n);
            }
            if (index >= 0) {
               if (index == 0) {
                  accesskeyX = leftM + 3;
               } else {
                  accesskeyX = leftM + 3 + form.textfm.stringWidth(text.substring(0, index));
               }
               switch (n) {
                  case 'g': case 'j': case 'p': case 'q': case 'y': accesskeyY = baseline + 3; break;
                  default : accesskeyY = baseline + 2; break;
               }
               accesskeyW = form.textfm.charWidth(n) - 1;
            }
         }
      }
      buttonPreferredSize = new Dimension(width, height);
      paintOffset = form.style == USE_CALPA_FLUSH ? 0 : 1;
   }


   public void paint(Graphics g, JComponent c) {
    
      AbstractButton b  = (AbstractButton) c;
      ButtonModel model = b.getModel();
      boolean pressed   = (model.isArmed() && model.isPressed());
      boolean enabled   = model.isEnabled();
      boolean hasFocus  = b.hasFocus() && (form.fItem.tagType != LABEL);
      int n, h, w, z;

      if (isLabel) {
         g.setColor(CalColor.colors[form.bgcolor]);
         g.fillRect(0, 0, width, height);
         if (bordersize > 0) {
            paintLabelBorder(g);
         }
         if ((specialView != null) && (specialView.lines != null) && (specialView.paintableLines > 0)) {
            viewer.paintLineArray(g, specialView, 0, height, topM, 0, specialView.paintableLines);
         }
         return;
      }

      if (paintOffset == 0) {        //then we're drawing flush style
         g.setColor(pressed ? CalColor.colors[form.pressedcolor] : CalColor.colors[form.bgcolor]);
         g.fillRect(1,1,width - 2, height - 2);
         g.setColor(CalColor.colors[form.outercolor]);
         g.drawLine(1, 0, width - 2, 0);
         g.drawLine(0, 1, 0, height - 2);
         g.drawLine(width - 1, 1, width - 1, height - 2);
         g.drawLine(1, height - 1, width - 2, height - 1);
         g.setColor(CalColor.colors[form.innercolor]);
         g.drawRect(2, 2, width - 5, height - 5);
         if (hasFocus) {
            g.setColor(CalColor.colors[form.outercolor]);
            z = (width - 4) % 4;
            z = ((z == 0) || (z == 1)) ? 3 : 2;
            for (int i=z, j=width-3, k=height-3; i<j; i+=4) {
               g.drawLine(i, 2, i+1, 2);
               g.drawLine(i, k, i+1, k);
            }
            z = (height - 6) % 4;
            boolean extra = false;
            if (z == 0) {
               z = 4;
            } else {
               if (z == 3) {
                  extra = true;
               }
               z = 5;
            }
            for (int i=z, j=height-4, k=width-3; i<j; i+=4) {
               g.drawLine(2, i, 2, i+1);
               g.drawLine(k, i, k, i+1);
            }
            if (extra) {
               z = height - 4;
               g.drawLine(2, z, 2, z);
               g.drawLine(width - 3, z, width - 3, z);
            }
         }
      } else {                       //drawing raised style
         g.setColor(CalColor.colors[form.bgcolor]);
         g.fillRect(0, 0, width, height);
         if ((hasFocus) || (form.paintMode == 1)) {
            g.setColor(Color.black);
            g.drawRect(0 , 0 , width - 1, height - 1);
            n = 1;
            w = width - 2;
            h = height - 2;
         } else {
            n = 0;
            w = width;
            h = height;
         }
         g.translate(n, n);
         if (pressed) {
            g.setColor(Color.black);
            g.drawLine(0, 0, w - 1, 0);
            g.drawLine(0, 0, 0, h - 1);
            g.setColor(CalColor.colors[form.shadow]);
            g.drawLine(1, 1, w - 1, 1);
            g.drawLine(1, 2, 1, h - 1);
            g.setColor(CalColor.colors[form.innerhighlight]);
            g.drawLine(2, h - 1, w - 1, h - 1);
            g.drawLine(w - 1, 2, w - 1, h - 2);
         } else {
            g.setColor(CalColor.colors[form.lthighlight]);
            g.drawLine(0, 0, w - 2, 0);
            g.drawLine(0, 0, 0, h - 2);
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
         g.translate(-n, -n);
      }
      n = pressed ? paintOffset : 0;
      g.translate(n, n);
      if ((specialView != null) && (specialView.lines != null) && (specialView.paintableLines > 0)) {
         viewer.paintLineArray(g, specialView, 0, height, topM, 0, specialView.paintableLines);
      } else if (text != null) {
         g.setFont(form.textFont);
         if (!enabled) {
            if (paintOffset > 0) {
               g.setColor(Color.white);
               g.drawString(text, leftM + 4, baseline + 1);
            }
            g.setColor(CalColor.darkGray);
         } else {
            g.setColor(CalColor.colors[form.textcolor]);
         }
         g.drawString(text, leftM + 3, baseline);
         if (accesskeyX >= 0) {
            g.drawLine(accesskeyX, accesskeyY, accesskeyX + accesskeyW, accesskeyY);
         }
      }
      if ((hasFocus) && (paintOffset > 0)) {
         //g.setColor(CalColor.colors[form.bgc]);
         g.setColor(CalColor.colors[form.highlight]);
         for (int i=2, k=height-1; i<width-1; i+=3) {
            g.drawLine(i, 0, i, 0);
            g.drawLine(i, k, i, k);
         }
         for (int i=2, k=width-1; i<height-1; i+=3) {
            g.drawLine(0, i, 0, i);
            g.drawLine(k, i, k, i);
         }
      } 
      g.translate(-n, -n);
   }


   private void paintLabelBorder(Graphics g) {
   
      g.setColor(CalColor.colors[bordercolor]);
      for (int i=0, w=width-1, h=height-1; i<bordersize; i++) {
         g.drawRect(i, i, w, h);
         w -= 2;
         h -= 2;
      }
   }
   
   
   public Dimension getPreferredSize(JComponent c) {

      return buttonPreferredSize;
   }


   void releaseMemory() {
   
      viewer = null;
      specialView = null;
      form = null;
   }
   
}
