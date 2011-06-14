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
import javax.swing.text.JTextComponent;

class CalForm extends JComponent implements CalCons, KeyListener, FocusListener, ActionListener { 

   JComponent   comp;
   CalDoc       doc;
   CalView      view;
   CalFormItem  fItem;
   ButtonGroup  buttonGroup;
   int          bgc;
   int          bgcolor;
   int          highlight;
   int          lthighlight;
   int          innerhighlight;
   int          shadow;
   int          innershadow;
   int          textcolor;
   int          textbgcolor;
   int          arrowcolor;
   int          outercolor;
   int          innercolor;
   int          pressedcolor;
   int          paintMode;
   int          style;
   int          viewTabIndex;
   int          clickX;
   int          clickY;
   int[]        shad;
   Font         textFont;
   FontMetrics  textfm;
   boolean      paintBorder;
   boolean      paintShadow;
   boolean      reverseColor;
   boolean      isCombo;
   boolean      isFocused;
   boolean      isSubmit;
   boolean      isReset;
   boolean      isHidden;


   CalForm(CalViewer viewer, CalDoc doc, CalView view, CalFormItem fItem, CalHTMLPreferences pref,
                                                                   CalFonts f, int displaySize, int bgc) {

      this.doc    = doc;
      this.view   = view;
      this.fItem  = fItem;
      this.bgc    = bgc;
      if (pref.optimizeDisplay == NO_OPTIMIZATION) {
         displaySize = S_LARGE;
      }
      int size    = (viewer.isDialog && ((fItem.type == V_SUBMIT) || (fItem.type == V_RESET) ||
                           (fItem.type == V_BUTTON))) ? Math.min(displaySize, 1) : displaySize; 
      style = (fItem.style != -1) ? fItem.style : pref.formRendering;
      if (fItem.tagType != OBJECT) {
         setTextFont(pref, f, size);
         if (style != USE_LOOK_AND_FEEL) {
            determineColors(pref);
         }
      }
      setLayout(new BorderLayout());
      comp = createComponent(viewer, pref, f, size);
      if (comp != null) {
         add(comp, "Center");
      }
      setOpaque(false);
      setName(Integer.toString(fItem.tagPos));
   }
   

   private void determineColors(CalHTMLPreferences pref) {
   
      Color c = CalColor.colors[bgc];
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();

      if (fItem.bgcolor > 0) {
         bgcolor = fItem.bgcolor;
         if (fItem.highlight > 0) {
            highlight   = fItem.highlight;
         } else {
            highlight = CalColor.getHighlight(bgcolor);
            if ((r < 128) && (g < 128) && (b < 128)) {
               reverseColor = true;
            }
         }
         if (fItem.shadow > 0) {
            shadow = fItem.shadow;
         } else {
            shadow = CalColor.getShadow(bgcolor);
         }
         innerhighlight = highlight;
         lthighlight = highlight;
      } else {
         bgcolor = pref.formControl;
         if (style == USE_CALPA_THREEDEE) {
            if (((r > 224) && ((g > 224) || (b > 224))) || ((g > 224) && (b > 224))) {
               paintMode = 1;
            }
            boolean black = ((r < 100) && (g < 100) && (b < 100));
            if ((paintMode == 1) && CalColor.highlightIsWhite(CalColor.colors[pref.formHighlight])) {
               highlight = CalColor.paleGrayIndex;
            } else {
               highlight = pref.formHighlight;
            }
            if (black && CalColor.shadowIsBlack(CalColor.colors[pref.formShadow])) {
               shadow = CalColor.lightGrayIndex;
            } else {
               shadow = pref.formShadow;
            }
         } else {
            highlight = pref.formHighlight;
            shadow = pref.formShadow;
            if ((r < 128) && (g < 128) && (b < 128)) {
               reverseColor = true;
            }
         }
         //this last bit overrides any of the above calcs
         if (fItem.highlight > 0) {
            highlight   = fItem.highlight;
            lthighlight = highlight;
            innerhighlight = highlight;
         } else {
            lthighlight = CalColor.whiteIndex;
            innerhighlight = pref.formHighlight;
         }
         if (fItem.shadow > 0) {
            shadow = fItem.shadow;
         }
      }
      if (style == USE_CALPA_FLUSH) {
         if ((r > 240) && (g > 240) && (b > 240)) {
            paintMode = 1;
         }
         if (reverseColor) {
            outercolor = highlight;
            innercolor = fItem.shadow > 0 ? fItem.shadow : CalColor.blackIndex;
         } else {
            outercolor = fItem.shadow > 0 ? fItem.shadow : CalColor.blackIndex;
            innercolor = highlight;
         }
      }
      textbgcolor = fItem.bordercolor == 0 ? pref.formTextBackground : fItem.bordercolor;
      textcolor   = fItem.textcolor   == 0 ? pref.formTextColor      : fItem.textcolor;
      arrowcolor  = fItem.arrowcolor  == 0 ? CalColor.blackIndex     : fItem.arrowcolor;
   }


   private void calcPressedColor() {
   
      Color c = CalColor.colors[bgcolor];
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();
      if ((r < 128) && (g < 128) && (b < 128)) {
         r = Math.min(255, (r * 120) / 100);
         g = Math.min(255, (g * 120) / 100);
         b = Math.min(255, (b * 120) / 100);
      } else {
         r = Math.max(0, (r * 85) / 100);
         g = Math.max(0, (g * 85) / 100);
         b = Math.max(0, (b * 85) / 100);
      }
      pressedcolor = CalColor.getColor(r << 16 | g << 8 | b);
   }
   

   private void setTextFont(CalHTMLPreferences pref, CalFonts f, int size) {

      if (((fItem.tagType == INPUT) && ((fItem.type == V_SUBMIT) || (fItem.type == V_RESET) ||
            (fItem.type == V_BUTTON))) ||
                 ((fItem.tagType == SELECT) && ((fItem.size == 0) && (!fItem.multiple))) ) {
         f.checkFont(pref.formButtonFamVals[size], pref.formButtonStyleVals[size],
                                                                pref.formButtonFontVals[size]);
         textFont = f.fonts[pref.formButtonFamVals[size]][pref.formButtonStyleVals[size]]
                                                              [pref.formButtonFontVals[size]];
         textfm   = f.fm[pref.formButtonFamVals[size]][pref.formButtonStyleVals[size]]
                                                                    [pref.formButtonFontVals[size]];
      } else {
         f.checkFont(pref.formTextFamVals[size], pref.formTextStyleVals[size],
                                                                pref.formTextFontVals[size]);
         textFont = f.fonts[pref.formTextFamVals[size]][pref.formTextStyleVals[size]]
                                                              [pref.formTextFontVals[size]];
         textfm   = f.fm[pref.formTextFamVals[size]][pref.formTextStyleVals[size]]
                                                                    [pref.formTextFontVals[size]];
      }
   }

     
   //main switch statement which creates the correct type of component specified by the CalFormItem
   //and adds it to this component
   private JComponent createComponent(CalViewer viewer, CalHTMLPreferences pref, CalFonts f,
                                                                            int displaySize) {
   
      switch (fItem.tagType) {
         case INPUT:    switch (fItem.type) {
                           case V_PASSWORD  :
                           case V_TEXT      : return createFormTextField();
                           case V_CHECKBOX  : return createFormCheckBox();
                           case V_RADIO     : return createFormRadio(viewer);
                           case V_JCOMPONENT: return createObjectComponent();
                           case V_SUBMIT    :
                           case V_RESET     :
                           case V_BUTTON    : return createFormInputButton(viewer, pref, f, displaySize);
                           case V_FILE      : return null;     //implement this later
                           case V_IMAGE     : isHidden = true;
                                              return new JLabel();   //avoids any null probs
                        }
                        break;
         case TEXTAREA: return createFormTextArea();
         case SELECT  : if ((fItem.size == 0) && (!fItem.multiple)) {
                           return createFormComboBox(viewer, pref, f, displaySize);
                        } else {
                           return createFormList();
                        }
         case LABEL   : style = USE_CALPA_THREEDEE;  //...and jump through
         case BUTTON  : return createFormCalButton(viewer, pref, f, displaySize);
         case OBJECT  : return createObjectComponent();
      }
      return null;
   }
   
   
   private JComponent createFormTextField() {
   
      int m, n;
      JTextField tf;

      if (fItem.type == V_PASSWORD) {
         tf = new JPasswordField();
      } else {
         tf = new JTextField();
      }
      if (fItem.initValue != null) {
         tf.setText(fItem.initValue);
      }
      n = textfm.charWidth('a');
      if (style == USE_LOOK_AND_FEEL) {
         tf.setMargin(CalCons.TEXT_INSETS);
         tf.setColumns(fItem.size == 0 ? 18 : fItem.size);
         this.setPreferredSize(tf.getPreferredSize());
         paintBorder = false;
      } else {
         tf.setFont(textFont);
         if (fItem.size > 0) {
            n = (n * fItem.size) + 10;
         } else {
            n = 150;
         }
         Dimension d;
         if (style == USE_CALPA_THREEDEE) {
            if (paintMode == 0) {
               this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
               m = 4;
            } else {
               this.setBorder(BorderFactory.createEmptyBorder(2,2,1,1));
               m = 3;
            }
            tf.setBorder(BorderFactory.createEmptyBorder(0,2,1,2));
            d = tf.getPreferredSize();
            tf.setPreferredSize(new Dimension(n + 4, d.height));
            this.setPreferredSize(new Dimension(n + 4 + m, d.height + m));     
         } else {
            if (paintMode == 1) {     //almost-white background
               tf.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
               d = tf.getPreferredSize();
               tf.setPreferredSize(new Dimension(n + 4, d.height));
               this.setBorder(BorderFactory.createEmptyBorder(1,1,3,3));
               this.setPreferredSize(new Dimension(n + 8, d.height + 4));
               checkShadColors();
            } else {
               tf.setBorder(BorderFactory.createEmptyBorder(1,2,3,2));
               d = tf.getPreferredSize();
               tf.setPreferredSize(new Dimension(n + 4, d.height));
               this.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
               this.setPreferredSize(new Dimension(n + 6, d.height + 2));
            }            
         }
         paintBorder = true;
         tf.setBackground(CalColor.colors[textbgcolor]);
         tf.setForeground(CalColor.colors[textcolor]);
      }
      if (fItem.disabled) {
         tf.setEnabled(false);
      } else if (fItem.readonly) {
         tf.setEditable(false);
      }
      if (fItem.maxlength > 0) {
         tf.addKeyListener(this);
      }
      if (fItem.accesskey != END) {
         tf.setFocusAccelerator(fItem.accesskey);
      }

      return tf;    
   }


   private void checkShadColors() {
   
      if (shad == null) {
         shad = new int[4];
         shad[0] = CalColor.getColor(0xB5B5B5);
         shad[1] = CalColor.getColor(0xD6D6D6);
         shad[2] = CalColor.getColor(0xDEDEDE);
         shad[3] = CalColor.getColor(0xC6C6C6);
      }
   }
   
   
   private JComponent createFormTextArea() {
   
      int n, w, h;
      Dimension d;
      
      JTextArea ta = new CalETA();
      ta.setLineWrap(true);
      ta.setWrapStyleWord(true);
      JScrollPane sp = null;
      if (fItem.initValue != null) {
         ta.setText(fItem.initValue);
      }
      if (style == USE_LOOK_AND_FEEL) {
         sp = new JScrollPane(ta, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         ta.setMargin(CalCons.TEXT_INSETS);
         ta.setRows(fItem.rows == 0 ? 2 : fItem.rows);
         ta.setColumns(fItem.cols == 0 ? 20 : fItem.cols);
         this.setPreferredSize(sp.getPreferredSize());
         paintBorder = false;
      } else {
         ta.setFont(textFont);
         sp = new CalESP(ta, this);
         sp.setViewportBorder(null);
         sp.getVerticalScrollBar().setBorder(null);
         sp.setBorder(null);
         paintBorder = true;
         n = textfm.charWidth('a');
         w = fItem.cols > 0 ? (n * fItem.cols) + 10 : 150;
         n = textfm.getHeight();
         h = fItem.rows > 0 ? n * fItem.rows : n * 2;
         ta.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
         sp.setPreferredSize(new Dimension(w + 4, h + 2));
         if (style == USE_CALPA_THREEDEE) {
            if (paintMode == 0) {
               this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
               this.setPreferredSize(new Dimension(w + 8, h + 6));
            } else {
               this.setBorder(BorderFactory.createEmptyBorder(2,2,1,1));
               this.setPreferredSize(new Dimension(w + 7, h + 5));
            }
         } else {
            if (paintMode == 1) {     //almost-white background
               this.setBorder(BorderFactory.createEmptyBorder(1,1,3,3));
               this.setPreferredSize(new Dimension(w + 8, h + 6));
               checkShadColors();
            } else {
               this.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
               this.setPreferredSize(new Dimension(w + 6, h + 4));
            }            
         }
         ta.setBackground(CalColor.colors[textbgcolor]);
         ta.setForeground(CalColor.colors[textcolor]);
      }
      if (fItem.disabled) {
         ta.setEnabled(false);
      } else if (fItem.readonly) {
         ta.setEditable(false);
      }
      if (fItem.accesskey != END) {
         ta.setFocusAccelerator(fItem.accesskey);
      }
      return sp;    
   }


   private JComponent createFormCheckBox() {

      JCheckBox jcb = null;
      if (style == USE_LOOK_AND_FEEL) {
         jcb = new JCheckBox();
         jcb.setHorizontalAlignment(SwingConstants.CENTER);
         jcb.setVerticalAlignment(SwingConstants.BOTTOM);
      } else {
         jcb = new CalEDA(this);
      }
      if ((fItem.disabled) || (fItem.readonly)) {
         jcb.setEnabled(false);
      }
      jcb.setSelected(fItem.checked);
      this.setPreferredSize(jcb.getPreferredSize());
      jcb.setOpaque(false);
      paintBorder = false;
      return jcb;
   }
      

   private JComponent createFormRadio(CalViewer viewer) {

      CalForm another = null;
      JRadioButton rb = null;
      
      if (style == USE_LOOK_AND_FEEL) {
         rb = new JRadioButton();
      } else {
         rb = new CalEDB(this);
      }
      rb.setHorizontalAlignment(SwingConstants.CENTER);
      rb.setVerticalAlignment(SwingConstants.BOTTOM);
      
      if (fItem.name != null) {
         if ((viewer.getLayout() == null) && (viewer.getComponentCount() > 0)) {
            Component[] c = viewer.getComponents();
            out:
            for (int i=0; i<c.length; i++) {
               if ((c[i] != null) && (c[i] instanceof CalForm)) {
                  another = (CalForm)c[i];
                  if (another.fItem.formNo == fItem.formNo) {
                     if ((another.fItem.type == V_RADIO) &&
                                         (another.fItem.tagPos != fItem.tagPos)) {
                        if ((another.fItem.name != null) && (another.fItem.name.equals(fItem.name))) {
                           try {
                              ButtonGroup group = null;
                              if (another.buttonGroup == null) {
                                 group = new ButtonGroup();
                                 group.add((JRadioButton)another.comp);
                                 another.buttonGroup = group;
                              } else {
                                 group = another.buttonGroup;
                              }
                              group.add(rb);
                              this.buttonGroup = group;
                              break out;
                           } catch (Exception ex) {
                              System.err.println("ButtonGroup exception in CalForm");
                           }
                        }
                     }
                  }
               }
            }
         }
      }            
      if ((fItem.disabled) || (fItem.readonly)) {
         rb.setEnabled(false);
      }
      rb.setSelected(fItem.checked);
      this.setPreferredSize(rb.getPreferredSize());
      rb.setOpaque(false);
      paintBorder = false;
      return rb;
   }


   private JComponent createFormInputButton(CalViewer viewer, CalHTMLPreferences pref, CalFonts f,
                                                                                      int displaySize) {
      JButton button = null;
      if (style == USE_LOOK_AND_FEEL) {
         JButton b = new JButton();
         if (fItem.initValue != null) {
            b.setText(fItem.initValue);
         } else {
            if (fItem.type == V_SUBMIT) {
               b.setText("Submit");
            } else if (fItem.type == V_RESET) {
               b.setText("Reset");
            }
         }
         if (fItem.accesskey != END) {
            b.setMnemonic(fItem.accesskey);
         }
         //b.setFont(textFont);
         this.setPreferredSize(b.getPreferredSize());
         paintBorder = false;
         button = b;
      } else {
         if (style == USE_CALPA_FLUSH) {
            calcPressedColor();
         }
         button = new CalEB(this, viewer, f, pref, 0);
         this.setPreferredSize(button.getPreferredSize());
      }
      if ((button != null) && (view != null)) {
         button.addActionListener(this);
         if (fItem.type == V_SUBMIT) {
            isSubmit = true;
         } else if (fItem.type == V_RESET) {
            isReset = true;
         }
      } 
      return button;
   }


   private JComponent createFormComboBox(CalViewer viewer, CalHTMLPreferences pref, CalFonts f,
                                                                                      int displaySize) {

      if (fItem.options == null) {
         return null;
      }
      JComboBox combo = null;
      if (style == USE_LOOK_AND_FEEL) {
         combo = new JComboBox();
      } else {
         combo = new CalECB(this);
         if (style == USE_CALPA_FLUSH) {
            combo.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
         } else {
            if (paintMode == 0) {
               combo.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            } else {
               combo.setBorder(BorderFactory.createEmptyBorder(2,2,1,1));
            }
         }
         combo.setFont(textFont);
      }
      for (int i=0; i<fItem.optionIndex; i++) {
         if ((fItem.options[i] != null) && (fItem.options[i].verboseValue != null)) {
            combo.addItem(fItem.options[i].verboseValue);
            if (fItem.options[i].selected) {
               combo.setSelectedIndex(combo.getItemCount() - 1);
            }
         }
      }
      if (combo.getItemCount() == 0) {
         return null;
      }
      if (fItem.disabled) {
         combo.setEnabled(false);
      }
      Dimension d = combo.getPreferredSize();
      d.width = Math.max(d.width, 40);
      if (style == USE_CALPA_FLUSH) {
         d.height = Math.max(d.height, 24);
      } else {
         d.height = Math.max(d.height, 20);
      }
      combo.setPreferredSize(d);
      this.setPreferredSize(d);
      paintBorder = false;
      isCombo = true;
      return combo;
   }


   private JComponent createFormList() {

      if (fItem.options == null) {
         return null;
      }
      int count = 0;
      int m, n;
      int[] preselected = new int[fItem.optionIndex];      
      JComponent listcomp = null;
      JScrollPane sp = null;
      DefaultListModel model = new DefaultListModel();
      
      for (int i=0; i<fItem.optionIndex; i++) {
         if ((fItem.options[i] != null) && (fItem.options[i].verboseValue != null)) {
            model.addElement(fItem.options[i].verboseValue);
            if (fItem.options[i].selected) {
               preselected[count++] = i;
            }
         }
      }
      n = model.size();
      if (n == 0) {
         return null;
      }
      JList list = new JList();
      list.setModel(model);
      if (fItem.disabled) {
         list.setEnabled(false);
      }
      if (style == USE_LOOK_AND_FEEL) {
         sp = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      } else {
         list.setFont(textFont);
         sp = new CalESP(list, this);
         sp.setViewportBorder(null);
         sp.getVerticalScrollBar().setBorder(null);
      }
      if (fItem.size > 0) {
         list.setVisibleRowCount(Math.min(fItem.size, n));
      } else {
         list.setVisibleRowCount(Math.min(n, 6));
      }
      listcomp = sp;
      if (style == USE_CALPA_FLUSH) {
         paintMode = 0;        //we don't need a shadow when scrollbar is present
      }
      if (fItem.multiple) {
         list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         if (count > 0) {
            int[] a = new int[count];
            System.arraycopy(preselected, 0, a, 0, count);
            list.setSelectedIndices(a);
         }
      } else {
         list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         if (count > 0) {
            list.setSelectedIndex(preselected[count - 1]);
         }
      }         
      if (style == USE_LOOK_AND_FEEL) {
         this.setPreferredSize(listcomp.getPreferredSize());
         paintBorder = false;
      } else {
         list.setCellRenderer(new CalCL(this));
         listcomp.setBorder(null);
         list.setBorder(null);
         paintBorder = true;
         Dimension d = listcomp.getPreferredSize();
         list.setBackground((fItem.bordercolor > 0) ? CalColor.colors[fItem.bordercolor] : Color.white);
         if (style == USE_CALPA_THREEDEE) {
            if (paintMode == 0) {
               this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
               m = 4;
            } else {
               this.setBorder(BorderFactory.createEmptyBorder(2,2,1,1));
               m = 3;
            }
            this.setPreferredSize(new Dimension(d.width + m, d.height + m));
         } else {
            this.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
            this.setPreferredSize(new Dimension(d.width + 2, d.height + 2));
         }
         list.addFocusListener(this);
      }
      
      return listcomp;
   }
   
   
   private JComponent createFormCalButton(CalViewer viewer, CalHTMLPreferences pref, CalFonts f,
                                                                                        int displaySize) {

      if (fItem.startPos > fItem.endPos) {
         return null;
      }
      JButton button = null;
      if (style == USE_LOOK_AND_FEEL) {
         button = new CalButton(this, viewer, f, pref, displaySize);
      } else {
         if (style == USE_CALPA_FLUSH) {
            calcPressedColor();
         }
         button = new CalEB(this, viewer, f, pref, displaySize);
      }
      Dimension d = button.getPreferredSize();
      if ((fItem.tagType == LABEL) && fItem.dropshadow) {
         paintBorder = true;
         this.setBorder(BorderFactory.createEmptyBorder(0,0,2,2));
         this.setPreferredSize(new Dimension(d.width + 2, d.height + 2));
         checkShadColors();
      } else {
         this.setPreferredSize(button.getPreferredSize());
         paintBorder = false;
      }
      if ((button != null) && (view != null)) {
         button.addActionListener(this);
      }
      if (fItem.type == V_SUBMIT) {
         isSubmit = true;
      } else if (fItem.type == V_RESET) {
         isReset = true;
      } 
      return button;
   }


   private JComponent createObjectComponent() {
 
      JComponent oc = null;
      if (fItem.type == V_JCOMPONENT) {
         oc = CalHTMLManager.getUserComponent(fItem.jclass, fItem.jname);
      } else {
         Object ob = CalObject.getObject(fItem);
         if ((ob != null) && (ob instanceof JComponent)) {
            oc = (JComponent)ob;
         } 
      }
      if (oc != null) {
         boolean change = false;
         Dimension d = oc.getPreferredSize();
         if (fItem.width != -1) {
            d.width = fItem.width;
            change = true;
         }
         if (fItem.height != -1) {
            d.height = fItem.height;
            change = true;
         }
         if (change) {
            oc.setPreferredSize(d);
         }
         if ((fItem.hspace > 0) || (fItem.vspace > 0)) {
            this.setBorder(
               BorderFactory.createEmptyBorder(fItem.vspace, fItem.hspace, fItem.vspace, fItem.hspace));
            d.width  += (fItem.hspace << 1);
            d.height += (fItem.vspace << 1);
         }
         this.setPreferredSize(d);
         paintBorder = false;
      }
      
      return oc;
   }


   public void keyTyped(KeyEvent e) {
      
      JTextComponent comp = (JTextComponent)e.getSource();
      if (comp.getText().length() >= fItem.maxlength) {
         e.consume();
      }
   }   
   public void keyPressed (KeyEvent e) {}
   public void keyReleased(KeyEvent e) {}


   public void focusGained(FocusEvent e) {
   
      isFocused = true;
      repaint();
   }
   
   public void focusLost(FocusEvent e) {
   
      isFocused = false;
      repaint();
   }


   public void actionPerformed(ActionEvent e) {
   
      if ((getParent() != null) && (getParent() instanceof CalViewer)) {
         try {
            CalViewer viewer = (CalViewer)getParent();
            if ((fItem.formNo > 0) && (isSubmit || isReset)) {
               CalFormHandler fh = (CalFormHandler)doc.objectVector.elementAt(doc.tags[fItem.formNo + 1]);
               if (isSubmit) {
                  fh.handleSubmission(this, viewer);
               } else {
                  fh.handleReset(this);
               }
            }
         } catch (Exception e2) {
            System.err.println("Error thrown in form handling action");
         }
      }
   }


   public void paint(Graphics g) {
   
      super.paint(g);
      if (paintBorder) {
         int x, y;
         int w = getSize().width;
         int h = getSize().height;
         if (style == USE_CALPA_FLUSH) {
            if (isCombo) {
               g.setColor(CalColor.colors[outercolor]);
               g.drawRect(0, 0, w - 1, h - 1);
               g.setColor(CalColor.colors[bgcolor]);
               g.drawRect(1, 1, w - 3, h - 3);
            } else {
               if (((fItem.tagType == INPUT) && ((fItem.type == V_TEXT) || (fItem.type == V_PASSWORD))) ||
                                              (fItem.tagType == TEXTAREA) || (fItem.tagType == SELECT)) {
                  g.setColor(Color.black);
                  if (paintMode == 0) {
                     g.drawRect(0, 0, w - 1, h - 1);
                  } else {
                     w -= 3; h -= 3;
                     g.drawRect(0, 0, w, h);
                     paintDropShadow(g, w, h);
                  }
               }
            }
         } else {
            if (fItem.tagType == LABEL) {
               paintDropShadow(g, w - 3, h - 3);
            } else {
               g.setColor(CalColor.colors[shadow]);
               g.drawLine(0, 0, w - 2 + paintMode, 0);
               g.drawLine(0, 0, 0, h - 2 + paintMode);
               g.setColor(Color.black);
               g.drawLine(1, 1, w - 3 + paintMode, 1);
               g.drawLine(1, 2, 1, h - 2);
               g.setColor(CalColor.colors[highlight]);
               g.drawLine(paintMode, h - 1, w - 1, h - 1);
               g.drawLine(w - 1, paintMode, w - 1, h - 2);
               if (paintMode == 0) {
                  g.setColor(CalColor.colors[bgcolor]);
                  g.drawLine(1, h - 2, w - 3, h - 2);
                  g.drawLine(w - 2, 1, w - 2, h - 2);
               }
            }
         }
         if (fItem.tagType == SELECT) {
            if (isFocused) {
               g.setColor(Color.white);
               for (int i=2; i<w-1; i+=3) {
                  g.drawLine(i, 0, i, 0);
               }
               for (int i=2; i<h-1; i+=3) {
                  g.drawLine(0, i, 0, i);
               }
               if (style == USE_CALPA_THREEDEE) {
                  g.setColor(Color.black);
               }
               for (int i=1, k=h-1; i<w-1; i+=3) {
                  g.drawLine(i, k, i, k);
               }
               for (int i=1, k=w-1; i<h-1; i+=3) {
                  g.drawLine(k, i, k, i);
               }
            }
         }
      }
   }


   private void paintDropShadow(Graphics g, int w, int h) {
   
      g.setColor(CalColor.colors[shad[2]]);
      g.drawLine(2, h+1, 3, h+1);
      g.drawLine(3, h+2, w, h+2);
      g.drawLine(w+1, h+1, w+1, h+1);
      g.drawLine(w+2, 3, w+2, h);
      g.setColor(CalColor.colors[shad[0]]);
      g.drawLine(4, h+1, w-1, h+1);
      g.drawLine(w+1, 5, w+1, h-1);
      g.setColor(CalColor.colors[shad[1]]);
      g.drawLine(w, h+1, w+1, h);
      g.drawLine(w+1, 2, w+1, 3);
      g.setColor(CalColor.colors[shad[3]]);
      g.drawLine(w+1, 4, w+1, 4);
   }
}
