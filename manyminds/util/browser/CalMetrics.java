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

import java.util.*;
import javax.swing.*;
import java.awt.*;

class CalMetrics implements CalCons {

   CalFonts           f;
   CalView            view;
   CalViewer          viewer;
   CalHTMLPreferences pref;
   CalTableCell       cell;
   CalDoc             doc;
   CalStackFont       bf;
   CalStackFont       sf;
   CalStackList       cCurrList;
   Stack              fontStack;
   Stack              listStack;
   int     bgc;
   int     cPos;
   int     cDisplaySize;
   int     cMinW;
   int     cMaxW;
   int     cCurrMin;
   int     cIndent;
   int     cBoldCount;
   int     cItalCount;
   int     cEmCount;
   int     cStrongCount;
   int     cDefListCount;
   int     cWidthSoFar;
   int     cSpaceWidth;
   int     cLastSpaceW;
   int     cDocWidth;
   int     cTableCols;
   int     cBaseFontFamily;
   int     cBaseFontStyle;
   int     cRootFontSize;
   int     cBaseFontSize;
   int     cFontFamily;
   int     cFontStyle;
   int     cFontSize;
   int     cMarginW;
   int     cFloatMinW;
   int     cFloatMaxW;
   int     t;
   int     tag;
   int     tagPos;
   int     charPos;
   boolean cJustSpaced;
   boolean cDDactive;
   boolean cLinkOpen;
   boolean cResizeTables;


   public CalMetrics(CalFonts f, CalView view, CalViewer viewer, CalHTMLPreferences pref) {

      this.f      = f;
      this.view   = view;
      this.viewer = viewer;
      this.pref   = pref;
      fontStack   = new Stack();
      listStack   = new Stack();
   }


   void setState(CalDoc doc, int docWidth, CalStackFont bf, int displaySize,
                                      int cols, int bgc, boolean resizeTables) {
   
      this.doc        = doc;
      cDocWidth       = docWidth;
      this.bf         = bf;
      this.bgc        = bgc;
      cDisplaySize    = displaySize;
      cTableCols      = cols;
      cBaseFontFamily = bf.family;
      cRootFontSize   = bf.size;
      cResizeTables   = resizeTables;
   }


   void calcMinMaxWidth(int start, int end, int style, int background, int charPos) {

      bgc             = background;
      cMinW           = 0;
      cMaxW           = 0;
      cCurrMin        = 0;
      cFloatMinW      = 0;
      cFloatMaxW      = 0;
      cBaseFontStyle  = style;
      cBaseFontSize   = bf.size; 
      cFontFamily     = bf.family;
      cFontStyle      = cBaseFontStyle;
      cFontSize       = bf.size;
      cBoldCount      = 0;
      cItalCount      = 0;
      cEmCount        = 0;
      cStrongCount    = 0;
      cDefListCount   = 0;
      cWidthSoFar     = 0;
      cMarginW        = 0;
      cIndent         = LINEINDENT[cDisplaySize];
      f.checkFont(cFontFamily, cFontStyle, cBaseFontSize);
      cSpaceWidth     = f.spaceW[cFontFamily][cFontStyle][cBaseFontSize];
      cLastSpaceW     = cSpaceWidth;
      fontStack.removeAllElements();
      listStack.removeAllElements();
      cCurrList       = null;

      cPos    = start;
      this.charPos = charPos;
      while (cPos <= end) {
         t = doc.tokenCodes[cPos];
         if (t >= 0) {    //a word/space
            addTokenToLine();
         } else {
            tagPos = Math.abs(t);
            tag = doc.tags[tagPos];
            if (tag > 0) {
               handleOpenTag();
            } else {
               tag = Math.abs(tag);
               handleCloseTag();
            }
         }
         cPos++;
      }
      finalizeLine();
   }


   private void addTokenToLine() {
   
      int w;
      if (t == 0) {
         if (cWidthSoFar == 0) {
            return;
         } else {
            if (cCurrMin > cMinW) {
               cMinW = cCurrMin;
            }
            cCurrMin = cMarginW;
            cWidthSoFar += cSpaceWidth;
            cLastSpaceW  = cSpaceWidth;
            cJustSpaced = true;
         }
      } else if (t > 0) {
         w = f.fm[cFontFamily][cFontStyle][cFontSize].charsWidth(doc.charArray, charPos, t);
         charPos  += t;
         cCurrMin += w;
         cWidthSoFar += w;
         cJustSpaced = false;
      }
   }


   private void finalizeLine() {
   
      if (cJustSpaced) {
         cWidthSoFar -= cLastSpaceW;
      }
      if (cWidthSoFar > cMaxW) {
         cMaxW = cWidthSoFar;
      }
      if (cCurrMin > cMinW) {
         cMinW = cCurrMin;
      }
      if ((cDDactive) && (cDefListCount == 0)) {
         cMarginW -= cIndent;
         cDDactive = false;
      }
      cCurrMin = cMarginW;
      cWidthSoFar = cMarginW;
      if (cFloatMinW > 0) {        //one or more floaters are waiting
         if (cFloatMinW + cMarginW > cMinW) {
            cMinW = cFloatMinW + cMarginW;
         }
         if (cFloatMaxW + cMarginW > cMaxW) {
            cMaxW = cFloatMaxW + cMarginW;
         }
         cFloatMinW = 0;
         cFloatMaxW = 0;
      }
   }


   private void handleOpenTag() {
   
      String s;
      int n, x;
      
      switch (tag) {
         case A       : if (doc.attrVals[doc.tags[tagPos + 2]] != 0) {
                           cLinkOpen = true;
                        }
                        break;
         case P       : finalizeLine();
                        break;
         case BR      : finalizeLine();
                        break;
         case VAR     :
         case CITE    :
         case DFN     :
         case I       : setFontStyle(ITAL, 1);
                        break;
         case EM      : setFontStyle(EM, 1);
                        break;
         case B       : setFontStyle(BOLD, 1);
                        break;
         case STRONG  : setFontStyle(STRONG, 1);
                        break;
         case BASEFONT: n = getAttrVal(A_SIZE, tagPos + 1);
                        if (n >= 1) {
                           n = Math.min(f.fontSizes.length - 1, Math.max(cRootFontSize + n - 3, 1));
                           cBaseFontSize = n;
                           if (fontStack.empty()) {
                              popFont();  //popFont will throw exception and set curr font to new base
                           }
                        }
                        break;         
         case FONT    : n = cFontSize;
                        x = getAttrVal(A_SIZE, tagPos + 1);
                        if (x > 0) {
                           switch(getAttrArg(A_SIZE, tagPos + 1)) {
                              case F_PLUS:
                                 x += 3 + cBaseFontSize - cRootFontSize; break;
                              case F_MINUS:
                                 x = Math.max(3 + cBaseFontSize - cRootFontSize - x, 0); break;
                           }
                           if (x > 0) {
                              x = Math.min(7, x);
                              x = getFontLevel(x);
                              x = Math.min(f.fontSizes.length - 1, Math.max(cRootFontSize + x - 3, 0));
                              n = x;
                           }
                        }
                        x = f.getFontFace(getAttrString(A_FACE, tagPos + 1));
                        if (x < 0) {
                           x = cFontFamily;
                        }
                        pushFont(x, cFontStyle, n);
                        break;                        
         case PRE     : finalizeLine();   //and continue to TT
         case CODE    :
         case KBD     :
         case SAMP    :
         case TT      : pushFont(MONO, cFontStyle, cFontSize);
                        break;
         case BIG     : pushFont(cFontFamily, cFontStyle, cFontSize + 2);
                        break;
         case SMALL   : pushFont(cFontFamily, cFontStyle, cFontSize - 2);
                        break;
         case H1      : setHeader(7);  break;
         case H2      : setHeader(4);  break;
         case H3      : setHeader(2);  break;
         case H4      : setHeader(0);  break;
         case H5      : setHeader(-2); break;
         case H6      : setHeader(-4); break;
         case HR      : finalizeLine();
                        x = getAttrVal(A_WIDTH, tagPos + 1);
                        if (x > 0) {
                           if (getAttrArg(A_WIDTH, tagPos + 1) == PIXEL) {
                              x = Math.max(1, Math.min(x, 2000));
                              cWidthSoFar += x;
                              cCurrMin = x;
                              finalizeLine();
                           }
                        }
                        break;
         case ADDRESS : finalizeLine();
                        setFontStyle(ITAL, 1);
                        break;
       case BLOCKQUOTE: cMarginW += (cIndent << 1);
                        finalizeLine();
                        break;
            case TABLE: CalTable table = (CalTable)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                        if (cResizeTables) {
                           table.sized[S_SMALL]  = false;
                           table.sized[S_MEDIUM] = false;
                           table.sized[S_LARGE]  = false;
                           table.sized[S_PREFW]  = false;
                        }
                        n = 0;
                        if (!table.sized[cDisplaySize]) {
                          table.calcPreferredWidth(f, view, viewer, pref,
                                                        cDisplaySize, bf, cDocWidth, bgc, cResizeTables);
                        }
                        if ((pref.optimizeDisplay != OPTIMIZE_ALL) && (table.tablePrefWidthType == PIXEL)) {
                           n = table.tablePrefWidthValue;
                        }
                        if ((table.hAlign == V_LEFT) || (table.hAlign == V_RIGHT)) {    //floating
                           cFloatMinW += Math.max(table.tableMinW[cDisplaySize], n);
                           cFloatMaxW += Math.max(table.tableMaxW[cDisplaySize], n);
                        } else {
                           finalizeLine();
                           n = Math.max(n, table.tableMinW[cDisplaySize]);
                           if (n > cMinW) {
                              cMinW = n;
                           }
                           n = Math.max(n, table.tableMaxW[cDisplaySize]);
                           if (n > cMaxW) {
                              cMaxW = n;
                           }
                        }
                        cPos = Math.max(table.endPos, cPos);
                        break;
             case MENU:
             case  DIR:
             case   UL: cMarginW += cIndent;
                        finalizeLine();
                        if (cCurrList != null) {
                           pushList(cCurrList);
                        }
                        cCurrList = new CalStackList(I_UL, 0, 1);
                        break;
               case OL: cMarginW += cIndent;
                        finalizeLine();
                        if (cCurrList != null) {
                           pushList(cCurrList);
                        }
                        cCurrList = new CalStackList(I_OL, 0, 1);
                        break;
               case LI: finalizeLine();
                        if (cCurrList == null) {
                           cCurrMin = 16;
                           cWidthSoFar = 16;
                           cJustSpaced = false;  
                        }
                        break;
               case DL: if (cDDactive) {
                           cMarginW -= cIndent;
                           cDDactive = false;
                        }
                        if (cDefListCount > 0) {
                           cMarginW += cIndent;
                        }
                        finalizeLine();
                        cDefListCount++;
                        break;
               case DT: if (cDDactive) {
                           cMarginW -= cIndent;
                           cDDactive = false;
                        }
                        finalizeLine();
                        break;
               case DD: if (!cDDactive) {
                           cDDactive = true;
                           cMarginW += cIndent;
                        }
                        finalizeLine();
                        break;
              case IMG: CalImage tagim = (CalImage)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                        n = 0;
                        //if (tagim.percentW > 0) {
                           //n = (cDocWidth * tagim.percentW) / 100;
                        //} else if ((tagim.width > 0) && (tagim.widthGiven)) {
                           n = tagim.width;
                        //} else if ((tagim.percentH > 0) && (tagim.width > 0) && (tagim.height > 0)) {
                           //x = (view.viewportHeight * tagim.percentH) / 100;
                           //n = (((tagim.width * 100) / tagim.height) * x) / 100;
                        //} else {
                           //n = tagim.width;
                        //}
                        if (tagim.border != NONE) {
                           if (tagim.border == 0) {
                              if ((cLinkOpen) || (tagim.usemapHash != 0)) {
                                 n += 4;
                              }
                           } else {
                              n += (tagim.border << 1);
                           }
                        }
                        if (tagim.hspace > 0) {
                           n += (tagim.hspace << 1);
                        }
                        if ((tagim.align == V_LEFT) || (tagim.align == V_RIGHT)) {    //floating
                           cFloatMinW += n;
                           cFloatMaxW += n;
                        } else {
                           if (cCurrMin > cMinW) {
                              cMinW = cCurrMin;
                           }
                           if (n > cMinW) {
                              cMinW = n;
                           }
                           cCurrMin = 0;
                           cWidthSoFar += n;
                           cJustSpaced = false;
                        }
                        break;
         case OBJECT  :
         case BUTTON  :
         case LABEL   :
         case SELECT  :
         case INPUT   :
         case TEXTAREA: CalFormItem fItem = (CalFormItem)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                        if (fItem.type == V_HIDDEN) {
                           return;
                        }
                        s = Integer.toString(fItem.tagPos);
                        CalForm form = null;
                        boolean existing = false;
                        if ((viewer.getLayout() == null) && (viewer.getComponentCount() > 0)) {
                           Component[] c = viewer.getComponents();
                           for (int i=0; i<c.length; i++) {
                              
                              if ((c[i] != null) && s.equals(c[i].getName())) {
                                 try {
                                    form = (CalForm)c[i];
                                    existing = true;
                                 } catch (Exception e5) {
                                    System.err.println("Exception thrown: removing component");
                                    viewer.remove(c[i]);
                                    form = null;
                                 }
                                 break;
                              }
                           }
                        }
                        if (form == null) {
                           form = new CalForm(viewer, doc, view, fItem, pref, f, cDisplaySize, bgc);
                        }
                        if (form.comp != null) {
                           Dimension d = form.getPreferredSize();
                           if ((fItem.align == V_LEFT) || (fItem.align == V_RIGHT)) {    //floating
                              cFloatMinW += d.width;
                              cFloatMaxW += d.width;
                           } else {
                              if (cCurrMin > cMinW) {
                                 cMinW = cCurrMin;
                              }
                              if (d.width > cMinW) {
                                 cMinW = d.width;
                              }
                              cCurrMin = 0;
                              cWidthSoFar += d.width;
                              cJustSpaced = false;
                           }
                           if (!existing) {
                              viewer.add(form);
                           }
                        }
                        if ((fItem.tagType == LABEL) || (fItem.tagType == BUTTON)) {
                           cPos = Math.max(cPos, fItem.endPos);
                           charPos = Math.max(charPos, fItem.charEnd);
                        }
                        break;
           case IFRAME: CalTagFrame tagframe =
                                      (CalTagFrame)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                        n = tagframe.width;
                        if (tagframe.widthType == PERCENT) {
                           n = (cDocWidth * n) / 100;
                        }
                        if (tagframe.hspace > 0) {
                           n += (tagframe.hspace >> 1);
                        }
                        if ((tagframe.align == V_LEFT) || (tagframe.align == V_RIGHT)) {
                           cFloatMinW += n;
                           cFloatMaxW += n;
                        } else {
                           if (cCurrMin > cMinW) {
                              cMinW = cCurrMin;
                           }
                           if (n > cMinW) {
                              cMinW = n;
                           }
                           cCurrMin = 0;
                           cWidthSoFar += n;
                           cJustSpaced = false;
                        }
                        break;
               default: break;
      }
   }


   private void handleCloseTag() {
   
      int n;
      
      switch (tag) {
         case A      : cLinkOpen = false;
                       break;
         case P      : finalizeLine();
                       break;
        case BR      : finalizeLine();
                       break;
        case VAR     :
        case CITE    :
        case DFN     :
        case I       : setFontStyle(ITAL, -1);
                       break;
        case EM      : setFontStyle(EM, -1);
                       break;
        case B       : setFontStyle(BOLD, -1);
                       break;
        case STRONG  : setFontStyle(STRONG, -1);
                       break;
        case PRE     : finalizeLine();     //and continue to TT
        case CODE    :
        case KBD     :
        case SAMP    :
        case FONT    :
        case BIG     :
        case SMALL   :
        case TT      : popFont();
                       break;
        case BASEFONT: cBaseFontSize = cRootFontSize;
                       if (fontStack.empty()) {
                          popFont();  //popFont will throw exception and set curr font to new base
                       }
                       break;
        case H1      :
        case H2      :
        case H3      :
        case H4      :
        case H5      :
        case H6      : setFontStyle(BOLD, -1);
                       popFont();
                       finalizeLine();
                       break;
        case ADDRESS : setFontStyle(ITAL, -1);
                       finalizeLine();
                       break;
      case BLOCKQUOTE: cMarginW = Math.max(0, cMarginW - (cIndent << 1));
                       finalizeLine();
                       break;
            case MENU:
            case  DIR:
            case   UL: if ((cCurrList != null) && (cCurrList.type == I_UL)) {
                          cMarginW -= cIndent;
                          finalizeLine();
                          popList();
                       }
                       break;
              case OL: if ((cCurrList != null) && (cCurrList.type == I_OL)) {
                          cMarginW -= cIndent;
                          finalizeLine();
                          popList();
                       }
                       break;
              case DL: if (cDefListCount > 0) {
                          if (cDDactive) {
                             cMarginW -= cIndent;
                             cDDactive = false;
                          }
                          if (cDefListCount > 1) {
                             cMarginW -= cIndent;
                          }
                          finalizeLine();
                          cDefListCount--;
                       }
                       break;
              default: break;
      }
   }


   private void setHeader(int n) {

      finalizeLine();
      setFontStyle(BOLD, 1);
      pushFont(cBaseFontFamily, cFontStyle, cRootFontSize + n);
   }


   private int getFontLevel(int n) {
   
      switch (n) {
         case 1:
         case 2: if (cRootFontSize > 3) {
                    return (n - 1);
                 } else {
                    return n;
                 }
         case 4: return 5;
         case 5: return 8;
         case 6: return 10;
         case 7: return 12;
      }
      return n;
   }


   private void pushFont(int family, int style, int size) {
   
      size = Math.max(Math.min(size, f.fontSizes.length - 1), 0);
      f.checkFont(family, style, size);
      fontStack.push(new CalStackFont(family, style, 0, size, bf.color));
      cSpaceWidth = f.spaceW[family][style][size];
      cFontFamily = family;
      cFontStyle  = style;
      cFontSize   = size;
   }
   
 
   private void popFont() {
   
      try {
         fontStack.pop();
         sf = (CalStackFont)fontStack.peek();
         cFontFamily = sf.family;
         cFontStyle  = sf.style;
         cFontSize   = sf.size;
      } catch (EmptyStackException e) {
         cFontFamily = cBaseFontFamily;
         cFontSize   = cBaseFontSize;
         f.checkFont(cFontFamily, cFontStyle, cFontSize);
      }
      cSpaceWidth = f.spaceW[cFontFamily][cFontStyle][cFontSize];
   }


   private void setFontStyle(int style, int setOrUnset) {   //1 = set, -1 = unset
         
      switch(style) {
         case BOLD  : cBoldCount   = Math.max(cBoldCount   += setOrUnset, 0); break;
         case ITAL  : cItalCount   = Math.max(cItalCount   += setOrUnset, 0); break;
         case EM    : cEmCount     = Math.max(cEmCount     += setOrUnset, 0); break;
         case STRONG: cStrongCount = Math.max(cStrongCount += setOrUnset, 0); break;
      }
      if ((cBoldCount > 0) || (cStrongCount > 0)) {
         if ((cItalCount > 0) || (cEmCount > 0)) {
            cFontStyle = BOIT;
         } else {
            cFontStyle = BOLD;
         }
      } else if ((cItalCount > 0) || (cEmCount > 0)) {
         cFontStyle = ITAL;
      } else {
         cFontStyle = cBaseFontStyle;
      }
      f.checkFont(cFontFamily, cFontStyle, cFontSize);
      cSpaceWidth = f.spaceW[cFontFamily][cFontStyle][cFontSize];
   }

   private void pushList(CalStackList list) {
   
      listStack.push(list);
   }

   private void popList() {
   
      if (listStack.empty()) {
         cCurrList = null;
      } else {
         cCurrList = (CalStackList)listStack.pop();
      }
   }


   //returns -1 if no attribute found  
   private int getAttr(int attrType, int tagIndex) {

      int n = doc.tags[tagIndex];
      if (n > 0) {      //if attrCount > 0
         int j = doc.tags[tagIndex + 1] + n;   //attrStart + attrCount
         for (int i=doc.tags[tagIndex + 1]; i<j; i++) {
            if (doc.attrTypes[i] == attrType) {
               return i;
            }
         }
      }
      return -1;
   }


   //returns -1 if no attribute arg found  
   private int getAttrArg(int attrType, int tagIndex) {

      int n = getAttr(attrType, tagIndex);
      return (n == - 1) ? -1 : doc.attrArgs[n];
   }
   

   //returns -1 if no attribute val found  
   private int getAttrVal(int attrType, int tagIndex) {

      int n = getAttr(attrType, tagIndex);
      return (n == - 1) ? -1 : doc.attrVals[n];
   }


   //returns "" rather than a null string if no string found  
   private String getAttrString(int attrType, int tagIndex) {

      String s = null;
      int n = getAttr(attrType, tagIndex);
      if (n != -1) {
         s = doc.attrStrings[n];
      }
      return (s == null) ? "" : s;
   }

}       
