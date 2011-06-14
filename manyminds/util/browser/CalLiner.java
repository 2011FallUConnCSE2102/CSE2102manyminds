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
import javax.swing.*;
import java.util.*;

class CalLiner extends Thread implements CalCons {

   CalViewer          viewer;
   CalFonts           f;
   CalDoc             doc;
   CalView            view;
   CalHTMLPreferences pref;
   CalStackFont       sf;
   CalStackFont       bf;
   CalStackList       cCurrList;
   CalElement         element;
   Stack              fontStack;
   Stack              listStack;
   Stack              floatStackL;
   Stack              floatStackR;
   CalElement         floatElementL;
   CalElement         floatElementR;
   CalElement         moreFloatL;
   CalElement         moreFloatR;
   Stack              alignStack;
   int     cFloatCountL;
   int     cFloatCountR;
   int     bgc;
   int     cFontColor;
   int     cNesting;
   int     cBoldCount;
   int     cItalCount;
   int     cEmCount;
   int     cStrongCount;
   int     cDefListCount;
   int     cW;
   int     cWordW;
   int     cTotWordW;
   int     cPos;
   int     cEnd;
   int     cLineStart;
   int     cLineEnd;
   int     cIndent;
   int     cWidthSoFar;
   int     cSpaceWidth;
   int     cLineHeight;
   int     cMaxLineHeight;
   int     cWaitingWordMLH;
   int     cWaitingWordMD;
   int     cDescent;
   int     cMaxDescent;
   int     cMaxAscent;
   int     cHeightSoFar;
   int     cWidth;
   int     cBaseWidth;
   int     cFinalWidth;
   int     cDocWidth;
   int     cBaseAlign;
   int     cDefaultAlign;
   int     cCurrentAlign;
   int     cBaseFontFamily;
   int     cRootFontSize;
   int     cBaseFontSize;
   int     cBaseFontStyle;
   int     cFontFamily;
   int     cFontStyle;
   int     cFontSize;
   int     cFontOverride;
   int     cOverrideCount;
   int     cCurrentFontInt;
   int     cBaseLeftMargin;
   int     cTrueLeftMargin; 
   int     cLeftMargin;
   int     cBaseRightMargin;
   int     cTrueRightMargin;
   int     cRightMargin;
   int     cDisplaySize;
   int     cNameHash;
   int     cLinkHash;
   int     cFloatXL;
   int     cFloatXR;
   int     cFloatYL;
   int     cFloatYR;
   int     cWordFlag;
   int     cMaxObjectHeight;
   int     cMaxObjectTop;
   int     cMaxObjectMiddle;
   int     cMaxObjectBottom;
   int     cMaxObjectAlign;
   int     t;
   int     tag;
   int     tagPos;
   boolean cSubOn;
   boolean cSupOn;
   boolean cFloatOn;
   boolean cBreakActive;
   boolean cDDactive;
   boolean cResizeTables;
   boolean cWordWaiting;
   boolean isThreadLiner;
   

   public CalLiner(CalViewer viewer, CalFonts f, CalDoc doc, CalView view,
                                                   CalHTMLPreferences pref, int nesting) {

      this.viewer   = viewer;
      this.f        = f;
      this.doc      = doc;
      this.view     = view;
      this.pref     = pref;
      cNesting      = nesting;
      fontStack     = new Stack();
      listStack     = new Stack();
      alignStack    = new Stack();
      cDocWidth     = view.docWidth;
      cDisplaySize  = view.displaySize;
      element       = new CalElement();
      isThreadLiner = false;
   }
   
   
   void reset(int width, CalStackFont bf, int start, int end,
                                          int align, int x, int bgc, boolean resizeTables) {
      cBoldCount      = 0;
      cItalCount      = 0;
      cEmCount        = 0;
      cStrongCount    = 0;
      cDefListCount   = 0;
      cFontOverride   = 0;
      cOverrideCount  = 0;
      cFloatCountL    = 0;
      cFloatCountR    = 0;
      cBaseWidth      = width;
      cFinalWidth     = x + width;
      cWidth          = cBaseWidth;
      this.bf         = bf;
      this.bgc        = bgc;
      cFontColor      = bf.color;
      cRootFontSize   = bf.size;
      cBaseFontFamily = bf.family;
      cBaseFontStyle  = bf.style;
      cBaseFontSize   = cRootFontSize;
      cFontSize       = cRootFontSize;
      cFontFamily     = cBaseFontFamily;
      cFontStyle      = bf.style;
      cPos            = start;
      cEnd            = end;
      cLineEnd        = view.elementPos;
      cLineStart      = cLineEnd;
      cBaseAlign      = align;
      cDefaultAlign   = cBaseAlign;
      cCurrentAlign   = cDefaultAlign;
      cBaseLeftMargin = x;
      cTrueLeftMargin = x;
      cLeftMargin     = cBaseLeftMargin;
      cBaseRightMargin= cBaseLeftMargin + cBaseWidth;
      cTrueRightMargin= cBaseRightMargin;
      cRightMargin    = cBaseRightMargin;
      cHeightSoFar    = 0;
      cTotWordW       = 0;
      cWidthSoFar     = 0;
      cNameHash       = 0;
      cLinkHash       = 0;
      cFloatXL        = cBaseLeftMargin;
      cFloatXR        = cBaseRightMargin;
      cFloatYL        = 0;
      cFloatYR        = 0;
      cWordFlag       = 0;
      cSubOn          = false;
      cSupOn          = false;
      cFloatOn        = false; 
      cBreakActive    = false;
      cDDactive       = false;
      cWordWaiting    = false;
      cResizeTables   = resizeTables;
      fontStack.removeAllElements();
      listStack.removeAllElements();
      alignStack.removeAllElements();
      floatStackL      = null;
      floatStackR      = null;
      cCurrList        = null;
      cSpaceWidth      = f.spaceW[cFontFamily][cFontStyle][cBaseFontSize];
      cLineHeight      = f.fm[cFontFamily][cFontStyle][cBaseFontSize].getHeight();
      cDescent         = f.fm[cFontFamily][cFontStyle][cBaseFontSize].getMaxDescent() + 1;
      cMaxLineHeight   = 0;
      cMaxDescent      = 0;
      cWaitingWordMLH  = 0;
      cWaitingWordMD   = 0;
      cMaxObjectHeight = 0;
      cMaxObjectTop    = 0;
      cMaxObjectMiddle = 0;
      cMaxObjectBottom = 0;
      cIndent          = LINEINDENT[cDisplaySize];
      computeFontInteger();
   }
   

   void lineDocument() {

      while (cPos <= cEnd) {
         t = doc.tokenCodes[cPos];
         if (t > 0) {    //a word
            addWord();
         } else if (t == 0) {   //whitespace
            if (cWordWaiting) {
               addWordToLine();
               if (cWidthSoFar + cSpaceWidth > cWidth) {
                  addLine();
               } else {
                  addSpace();
               }
            } else if (cLineEnd > cLineStart) {  //possibly an inline image has just been added
               if (cWidthSoFar + cSpaceWidth > cWidth) {
                  addLine();
               } else {
                  addSpace();
               }
            }               
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
      if (cNameHash != 0) {
         view.addElement(cWordFlag, 0, cNameHash, cLinkHash, cCurrentFontInt, cFontColor);
         cNameHash = 0;
         cLineEnd++;
      }      
      finalizeLine();
      if (cFloatOn) {
         if ((floatElementL != null) || (floatElementR != null)) {
            insertFloat();
         }
         clearFloat(V_ALL);
      }
   }
   
   
   void addWord() {

      cWordW = f.fm[cFontFamily][cFontStyle][cFontSize].charsWidth(
                                                        doc.charArray, view.charPos, t);
      cTotWordW += cWordW;
      if (cLineHeight > cWaitingWordMLH) {
         cWaitingWordMLH = cLineHeight;
      }
      if (cDescent > cWaitingWordMD) {
         cWaitingWordMD = cDescent;
      }
      if (cWidthSoFar + cTotWordW > cWidth) {
         addLine();
      }
      view.addElement(cWordFlag, cWordW, cNameHash, cLinkHash, cCurrentFontInt, cFontColor, t);
      cNameHash = 0;
      if (cLeftMargin + cTotWordW > cFinalWidth) {
         cFinalWidth = cLeftMargin + cTotWordW;
      }
      cBreakActive = true;
      cWordWaiting = true;
   }
   

   void addSpace() {
   
      view.addElement(cWordFlag, cSpaceWidth, cNameHash, cLinkHash, cCurrentFontInt, cFontColor);
      cNameHash = 0;
      cWidthSoFar += cSpaceWidth;
      cLineEnd = view.elementPos;
   }
   
   
   void addWordToLine() {
   
      if (cWaitingWordMLH > cMaxLineHeight) {
         cMaxLineHeight = cWaitingWordMLH;
      }
      if (cWaitingWordMD > cMaxDescent) {
         cMaxDescent = cWaitingWordMD;
      }
      cWaitingWordMLH = 0;
      cWaitingWordMD  = 0;
      cWidthSoFar += cTotWordW;
      cTotWordW = 0;
      cBreakActive = true;
      cWordWaiting = false;
      cLineEnd = view.elementPos;
   }
   

   void addLine() {

      if (cLineEnd <= cLineStart) {               //no elements
         if ((cDDactive) && (cDefListCount == 0)) {
            reduceLeftMargin(cIndent, false);
            cDDactive = false;
         }
         return;
      }
      if ((view.elements[E_ATTR][cLineEnd - 1] & 4) > 0) {  //i.e. if it's a space
         cWidthSoFar -= view.elements[E_WIDTH][cLineEnd - 1];   //kills the space
         view.killSpace(cLineEnd - 1);
      }

      int x = cLeftMargin;
      switch (cCurrentAlign) {
         case V_LEFT  : break;
         case V_CENTER: x = Math.max(x, x + ((cWidth - cWidthSoFar) / 2)); break;
         case V_RIGHT : x = Math.max(x, x + cWidth - cWidthSoFar); break;
      }
      if (cMaxLineHeight == 0) {    //no text has been added
         cHeightSoFar += cMaxObjectHeight;
         view.addLine(x, cHeightSoFar, cMaxObjectHeight, 0, cLineStart, cLineEnd, 0, 0);
      } else {
         cMaxAscent = cMaxLineHeight - cMaxDescent;
         if (cMaxObjectHeight > 0) {        //then there's an object in the line somewhere
            finalizeObjectMetrics();
         }
         cHeightSoFar += cMaxLineHeight;
         view.addLine(x, cHeightSoFar - cMaxDescent, cMaxLineHeight, cMaxDescent,
                                                       cLineStart, cLineEnd, 0, cMaxAscent);
      }
      if (cFloatOn) {
         checkFloatEnd();
         if ((floatElementL != null) || (floatElementR != null)) {
            insertFloat();
            cFloatOn = true;   //checkFloatEnd may have set cFloatOn to false
         }
      }
      cMaxLineHeight   = cWaitingWordMLH;
      cMaxDescent      = cWaitingWordMD;
      cMaxObjectHeight = 0;
      cMaxObjectTop    = 0;
      cMaxObjectMiddle = 0;
      cMaxObjectBottom = 0;
      cWidthSoFar = 0;
      cLineStart = cLineEnd;
      if ((cDDactive) && (cDefListCount == 0)) {
         reduceLeftMargin(cIndent, false);
         cDDactive = false;
      }
   }
   
   
   void handleOpenTag() {
   
      String s;
      boolean flag;
      int m, n, x, y, w, style;

      switch (tag) {
         case A       : if (doc.tags[tagPos + 1] != 0) {
                           cNameHash = doc.attrVals[doc.tags[tagPos + 1]];
                        }
                        if (doc.tags[tagPos + 2] != 0) {
                                    //DUMP??if (doc.attrVals[doc.tags[tagPos + 2]] != 0) {
                           cLinkHash = tagPos;
                           view.addTabItem(tagPos);
                        }
                        break;
         case P       : newParagraph();
                        setAlignment(getAttrArg(A_ALIGN, tagPos + 1));
                        break;
         case CENTER  : finalizeLine();
                        cBreakActive = true;
                        pushAlignment(V_CENTER);
                        break;
         case DIV     : finalizeLine();
                        cBreakActive = true;
                        n = getAttrArg(A_ALIGN, tagPos + 1);
                        switch(n) {
                           case V_LEFT: case V_RIGHT: case V_CENTER: break; 
                           default: n = V_LEFT;
                        }
                        pushAlignment(n);
                        break;
         case BR      : addLineOrLineBreak();
                        n = getAttrArg(A_CLEAR, tagPos + 1);
                        if (n != -1) {
                           switch(n) {
                              case V_LEFT: case V_RIGHT: case V_ALL: clearFloat(n);
                           }
                        }
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
         case SUB     : cWordFlag = (cWordFlag | 1);
                        cWordFlag -= (cWordFlag & 2);   //turns off sup
                        cSubOn = true;
                        pushFont(cFontFamily, cFontStyle, cFontOverride, cFontSize - 2, cFontColor);
                        break;
         case SUP     : cWordFlag = (cWordFlag | 2);
                        cWordFlag -= (cWordFlag & 1);   //turns off sub
                        cSupOn = true;
                        pushFont(cFontFamily, cFontStyle, cFontOverride, cFontSize - 2, cFontColor);
                        break;
         case STRIKE  : cWordFlag = (cWordFlag | 16);
                        break;
         case U       : cWordFlag = (cWordFlag | 32);
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
         case FONT    : m = getAttrVal(A_COLOR, tagPos + 1);
                        if (m <= 0) {
                           m = cFontColor;
                        } else if (cLinkHash != 0) {
                           cFontOverride = 1;
                        }
                        n = cFontSize;
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
                        pushFont(x, cFontStyle, cFontOverride, n, m);
                        break;                        
         case PRE     : newParagraph();   //...and continue to TT
         case CODE    :
         case KBD     :
         case SAMP    :
         case TT      : pushFont(MONO, cFontStyle, cFontOverride, cFontSize, cFontColor);
                        break;
         case BIG     : pushFont(cFontFamily, cFontStyle, cFontOverride, cFontSize + 2, cFontColor);
                        break;
         case SMALL   : pushFont(cFontFamily, cFontStyle, cFontOverride, cFontSize - 2, cFontColor);
                        break;
         case H1      : setHeader(7);  break;
         case H2      : setHeader(4);  break;
         case H3      : setHeader(2);  break;
         case H4      : setHeader(0);  break;
         case H5      : setHeader(-2); break;
         case H6      : setHeader(-4); break;
         case HR      : finalizeLine();
                        n = getAttrVal(A_SIZE, tagPos + 1);
                        if ((n <= 0) || (n > 1000)) {
                           if (n == 0) {
                              n = 1;
                           } else {
                              n = 3;
                           }
                        }
                        y = n + 14;
                        cHeightSoFar += y;
                        m = (getAttr(A_NOSHADE, tagPos + 1) == -1) ? 0 : 1;
                        w = getAttrVal(A_WIDTH, tagPos + 1);
                        if (w > 0) {
                           if (getAttrArg(A_WIDTH, tagPos + 1) == PERCENT) {
                              w = Math.max(1, Math.min((cWidth * w) / 100, cWidth));
                           } else {
                              w = Math.max(1, Math.min(w, cWidth));
                           }
                        } else {
                           w = cWidth;
                        }
                        x = cLeftMargin;
                        int a = getAttrArg(A_ALIGN, tagPos + 1);
                        switch(a) {
                           case V_LEFT : break;
                           case V_RIGHT: x = Math.max(x, x + cWidth - w); break;
                                default: x = Math.max(x, x + ((cWidth - w) / 2)); break;
                        }
                        w--;            //width reduced by 1 'cos of g.drawLine() painting extra pixel
                        element.reset(cNameHash);
                        cNameHash = 0;
                        int c = getAttrVal(A_COLOR, tagPos + 1);
                        if (c <= 0) {
                           c = bgc;
                        } else {
                           m = 2;     //sets noshade and paint in color specified
                        }
                        element.setItem(I_HR, m);
                        element.width = w;
                        element.fontColor = c;
                        view.addElement(element);
                        cLineEnd++;
                        view.addLine(x, cHeightSoFar, y, n-1, cLineStart, cLineEnd, 0, 0);
                        cLineStart = cLineEnd;
                        if (cFloatOn) {
                           if ((floatElementL != null) || (floatElementR != null)) {
                              insertFloat();
                           }
                           checkFloatEnd();
                        }
                        cBreakActive = true;
                        cWidthSoFar = 0;
                        break;
         case ADDRESS : newParagraph();
                        setFontStyle(ITAL, 1);
                        break;
       case BLOCKQUOTE: newParagraph();
                        increaseLeftMargin(cIndent, true);
                        break;
         case TABLE   : CalTable table =
                             (CalTable)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                        if (isThreadLiner) {
                           table.sized[S_SMALL]  = false;
                           table.sized[S_MEDIUM] = false;
                           table.sized[S_LARGE]  = false;
                           table.sized[S_PREFW]  = false;
                        }
                        if (!table.sized[cDisplaySize]) {
                          table.calcPreferredWidth(f, view, viewer, pref, cDisplaySize, bf,
                                                                cDocWidth, bgc, cResizeTables);
                        }
                        CalTableView tView = new CalTableView(table);
                        view.addTableView(tView);
                        if ((table.hAlign == V_LEFT) || (table.hAlign == V_RIGHT)) { //floating
                           CalElement temp2 = new CalElement(cNameHash);
                           temp2.setItem(I_TABLE, view.currTableNo);
                           addFloatItem(temp2, table.hAlign);
                           cFloatOn = true;
                           if ((cLineEnd == cLineStart) && (!cWordWaiting)) {
                              insertFloat();
                           }
                           cPos = Math.max(table.endPos, cPos);
                           view.charPos = Math.max(table.charEnd, view.charPos);
                        } else {
                           finalizeLine();
                           w = table.getPreferredWidth(cWidth, cDisplaySize);
                           x = cLeftMargin;
                           if (x + w > cFinalWidth) {
                              cFinalWidth = x + w;
                           } else {
                              switch (table.hAlign == NONE ? cCurrentAlign : table.hAlign) {
                                 case V_LEFT  : break;
                                 case V_CENTER: x = Math.max(x, x + ((cWidth - w) / 2)); break;
                                 case V_RIGHT : x = Math.max(x, x + cWidth - w); break;
                              }
                           }
                           view.setUpdatePaintable(false);
                           view.setLockUpdatePaintable(true);
                           element.reset(cNameHash);
                           element.setItem(I_TABLE, tView.cIndexNo);
                           cLineStart = view.elementPos;
                           view.addElement(element);
                           cLineEnd = view.elementPos;
                           n = view.linePos;
                           view.addLine(0,0,0,0,0,0,0,0);
                           table.lineTable(viewer, f, view, tView, pref, cDocWidth, w, bf, cDisplaySize, x,
                                                                 cResizeTables, cNesting);
                           cHeightSoFar += tView.cTotalHeight;
                           tView.lastLineIndex = Math.max(n, view.linePos - 1);
                           tView.lastElementIndex = Math.max(cLineStart, view.elementPos - 1);
                           view.elements[E_WIDTH][cLineStart] = tView.cWidth;
                           view.setLineVals(n, tView.cStartX, cHeightSoFar,
                             tView.cTotalHeight, 0, cLineStart, cLineEnd, tView.lastLineIndex,0);
                           cLineEnd = view.elementPos;
                           cLineStart = cLineEnd;
                           if (table.nesting == 0) {
                              view.setLockUpdatePaintable(false);
                           }
                           cPos = Math.max(table.endPos, cPos);
                           view.charPos = Math.max(table.charEnd, view.charPos);
                           if (cFloatOn) {
                              if ((floatElementL != null) || (floatElementR != null)) {
                                 insertFloat();
                              }
                              checkFloatEnd();
                           }
                           cBreakActive = true;
                           cWidthSoFar = 0;
                        }
                        break;
         case MENU    :
         case DIR     :
         case UL      : if (cCurrList == null) {
                           newParagraph();
                        }
                        finalizeLine();
                        setAlignment(-1);  //i.e default align
                        increaseLeftMargin(cIndent, false);
                        if (cCurrList != null) {
                           pushList(cCurrList);
                        }
                        style = getListStyle(I_UL, tagPos + 1);
                        if (style == NONE) {
                           if (listStack.empty()) {
                              style = V_DISC;
                           } else if (listStack.size() == 1) {
                              style = V_CIRCLE;
                           } else {
                              style = V_SQUARE;
                           }
                        }
                        cCurrList = new CalStackList(I_UL, style, 1);
                        break;
         case OL      : if (cCurrList == null) {
                           newParagraph();
                        }
                        finalizeLine();
                        setAlignment(-1);  //i.e default align
                        increaseLeftMargin(cIndent, false);
                        if (cCurrList != null) {
                           pushList(cCurrList);
                        }
                        style = getListStyle(I_OL, tagPos + 1);
                        if (style == NONE) {
                           style = NUM;
                        }
                        n = getAttrVal(A_START, tagPos + 1);
                        cCurrList = new CalStackList(I_OL, style, Math.max(n, 1));
                        break;
         case LI      : finalizeLine();
                        setAlignment(-1);  //i.e default align
                        if (cCurrList == null) {
                           style = getListStyle(I_UL, tagPos + 1);
                           if (style == NONE) {
                              style = V_DISC;
                           }
                           element.reset(cNameHash);
                           element.setItem(I_NL, style);
                           element.fontColor = cFontColor;
                           element.width = 16;
                           cTotWordW = 16;
                           view.addElement(element);
                        } else if (cCurrList.type == I_UL) {
                           element.reset(cNameHash);   //i.e. special
                           style = getListStyle(I_UL, tagPos + 1);
                           if (style == NONE) {
                              style = cCurrList.style;
                           }
                           element.setItem(cCurrList.type, style);
                           element.fontColor = cFontColor;
                           view.addElement(element);
                        } else {
                           style = getListStyle(I_OL, tagPos + 1);
                           if (style == NONE) {
                              style = cCurrList.style;
                           } else {
                              cCurrList.style = style;
                           }
                           n = getAttrVal(A_VALUE, tagPos + 1);
                           if (n >= 0) {
                              cCurrList.index = n;
                           }
                           s = getListItemString();
                           cCurrList.index++;
                           w = f.fm[cFontFamily][cFontStyle][cFontSize].stringWidth(s);
                           element.setState(cWordFlag, 0, w, cNameHash, cLinkHash,
                                                          cCurrentFontInt, cFontColor, 0, 0);
                           element.attr += 8;   //sets the 'special' bit
                           element.setItem(I_OL, Math.max(cLeftMargin - w - 6 - (cDisplaySize << 1),
                                                                            cBaseLeftMargin));
                           view.addElement(element, s);
                        }
                        if (cLineHeight > cMaxLineHeight) {
                           cMaxLineHeight = cLineHeight;
                        }
                        if (cDescent > cMaxDescent) {
                           cMaxDescent = cDescent;
                        }   
                        cNameHash = 0;
                        addWordToLine();
                        break;
         case DL      : if (cDefListCount == 0) {
                           newParagraph();
                        }
                        finalizeLine();
                        setAlignment(-1);   //default
                        if (cDDactive) {
                           reduceLeftMargin(cIndent, false);
                           cDDactive = false;
                        }
                        if (cDefListCount > 0) {
                           increaseLeftMargin(cIndent, false);
                        }
                        cDefListCount++;
                        break;
         case DT      : finalizeLine();
                        setAlignment(-1);  //i.e default align
                        if (cDDactive) {
                           reduceLeftMargin(cIndent, false);
                           cDDactive = false;
                        }
                        break;
         case DD      : finalizeLine();
                        setAlignment(-1);  //i.e default align
                        if (!cDDactive) {
                           cDDactive = true;
                           increaseLeftMargin(cIndent, false);
                        }
                        break;
         case IMG     : int index = doc.tags[tagPos + 1];
                        CalImage tagim =
                             (CalImage)doc.objectVector.elementAt(index);

                        n = 0;
                        if ((tagim.percentW > 0) && isThreadLiner) {
                           w = (cDocWidth * tagim.percentW) / 100;
                           if ((tagim.percentH == 0) && (!tagim.heightGiven)) {
                              if ((tagim.height > 0) && (tagim.width > 0)) {
                                 n = (((tagim.height * 100) / tagim.width) * w) / 100;
                              } else {
                                 n = w;
                              }
                           }
                        } else {
                           w = tagim.width;
                        }
                        if (n == 0) {
                           if ((tagim.percentH > 0) && isThreadLiner) {
                              n = (view.viewportHeight * tagim.percentH) / 100;
                              if ((tagim.percentW == 0) && (!tagim.widthGiven)) {
                                 if ((tagim.width > 0) && (tagim.height > 0)) {
                                    w = (((tagim.width * 100) / tagim.height) * n) / 100;
                                 } else {
                                    w = n;
                                 }
                              }
                              view.relineOnHChange = true;
                           } else {
                              n = tagim.height;
                           }
                        }
                        if (tagim.border != NONE) {
                           if (tagim.border == 0) {
                              if ((cLinkHash != 0) || (tagim.usemapHash != 0)) {
                                 w += 4;
                                 n += 4;
                              }
                           } else {
                              w += (tagim.border << 1);
                              n += (tagim.border << 1);
                           }
                        }
                        if (tagim.hspace > 0) {
                           w += (tagim.hspace << 1);
                        }
                        n += (tagim.vspace << 1);
                        if ((tagim.align == V_LEFT) || (tagim.align == V_RIGHT)) {  //floating
                           CalElement temp = new CalElement(cNameHash);
                           temp.setItem(I_IMG, index);
                           temp.width = w;
                           temp.linkHash = (tagim.usemapHash == 0 ? cLinkHash : tagPos);
                           temp.attr = ((n << 10) | temp.attr);  //height is recorded in attr value
                           temp.fontColor = bgc;
                           addFloatItem(temp, tagim.align);
                           cFloatOn = true;
                           if ((cLineEnd == cLineStart) && (!cWordWaiting)) {
                              insertFloat();
                           }
                        } else {
                           if (cWordWaiting) {
                              addWordToLine();
                           }
                           if (cWidthSoFar + w > cWidth) {
                              addLine();
                           }
                           element.reset(cNameHash);
                           element.setItem(I_IMG, index);
                           element.width = w;
                           element.linkHash = (tagim.usemapHash == 0 ? cLinkHash : tagPos);
                           element.attr = ((n << 10) | element.attr);  //height is recorded in attr value
                           element.fontColor = bgc;
                           cTotWordW = w;
                           view.addElement(element);
                           addWordToLine();   //rem this sets cTotWordW to 0
                           checkObjectMetrics(n, tagim.align);
                           if (cLeftMargin + w > cFinalWidth) {
                              cFinalWidth = cLeftMargin + w;
                           }
                           if (cWidthSoFar > cWidth) {
                              addLine();
                           }
                        }
                        break;
         case FORM    : newParagraph();
                        break;
         case OBJECT  :
         case BUTTON  :
         case LABEL   :
         case SELECT  :
         case INPUT   :
         case TEXTAREA: CalForm form = null;
                        boolean existing = false;
                        CalFormItem fItem = null;
                        try {
                           fItem =
                                (CalFormItem)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                        } catch (Exception e12) {
                           System.err.println("fItem not found in Liner");
                           return;
                        }
                        if (fItem.type == V_HIDDEN) {
                           return;
                        }
                        s = Integer.toString(fItem.tagPos);
                        if ((viewer.getLayout() == null) && (viewer.getComponentCount() > 0)) {
                           Component[] c2 = viewer.getComponents();
                           for (int i=0; i<c2.length; i++) {
                              if ((c2[i] != null) && s.equals(c2[i].getName())) {
                                 try {
                                    form = (CalForm)c2[i];
                                    existing = true;
                                 } catch (Exception e8) {
                                    System.err.println("Error casting component in Liner");
                                    form = null;
                                    viewer.remove(c2[i]);
                                 }
                                 break;
                              }
                           }
                        }
                        if (form == null) {
                           form = new CalForm(viewer, doc, view, fItem, pref, f, cDisplaySize, bgc);
                        }
                        if (form.isHidden) {
                           view.addForm(form);
                           form.setVisible(false);
                           view.formIndex++;
                           return;
                        } else if (form.comp != null) {
                           Dimension d = form.getPreferredSize();
                           w = d.width;
                           n = d.height;
                           form.setVisible(false);
                           if (!existing) {
                              viewer.add(form);
                           }
                           form.setBounds(0,0,w,n);
                           viewer.hasChildren = true;
                           view.addForm(form);
                           form.viewTabIndex = view.tabIndex;
                           view.addTabItem(-(view.formIndex + 1));
                           if ((fItem.align == V_LEFT) || (fItem.align == V_RIGHT)) {  //floating
                              CalElement temp = new CalElement(cNameHash);
                              temp.setItem(I_FORM, view.formIndex);
                              temp.width = w;
                              temp.linkHash = cLinkHash;
                              temp.attr = ((n << 10) | temp.attr);  //height is recorded in attr value
                              temp.fontColor = bgc;
                              temp.font = fItem.align; //should be okay to use font int for this
                              addFloatItem(temp, fItem.align);
                              cFloatOn = true;
                              if ((cLineEnd == cLineStart) && (!cWordWaiting)) {
                                 insertFloat();
                              }
                           } else {
                              if (cWordWaiting) {
                                 addWordToLine();
                              }
                              if (cWidthSoFar + w > cWidth) {
                                 addLine();
                              }
                              element.reset(cNameHash);
                              element.setItem(I_FORM, view.formIndex);
                              element.width = w;
                              element.linkHash = cLinkHash;
                              element.attr = ((n << 10) | element.attr);  //height is recorded in attr value
                              element.fontColor = bgc;
                              element.font = fItem.align;  //record align in font integer
                              cTotWordW = w;
                              view.addElement(element);
                              addWordToLine();   //rem this sets cTotWordW to 0
                              if (fItem.align == V_FORM) {
                                 cMaxDescent    = Math.max(cMaxDescent, 3);
                                 cMaxLineHeight = Math.max(cMaxLineHeight, n + 3);
                              } else {
                                 checkObjectMetrics(n, fItem.align);
                              }
                              if (cLeftMargin + w > cFinalWidth) {
                                 cFinalWidth = cLeftMargin + w;
                              }
                              if (cWidthSoFar > cWidth) {
                                 addLine();
                              }
                           }
                           view.formIndex++;
                        }
                        if ((fItem.tagType == LABEL) || (fItem.tagType == BUTTON)) {
                           cPos = Math.max(cPos, fItem.endPos);
                           view.charPos = Math.max(fItem.charEnd, view.charPos);
                        }
                        break;
         case IFRAME  : CalViewer iframe = null;
                        CalTagFrame tagframe =
                              (CalTagFrame)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                        s = Integer.toString(tagPos);
                        w = tagframe.width;
                        if (tagframe.widthType == PERCENT) {
                           w = (cDocWidth * w) / 100;
                        }
                        n = tagframe.height;
                        if (tagframe.heightType == PERCENT) {
                           n = (view.viewportHeight * n) / 100;
                           view.relineOnHChange = true;
                        }
                        m = tagframe.align;

                        boolean reuse = false;
                        //we don't need to recreate this component if it already exists
                        if ((viewer.getLayout() == null) && (viewer.getComponentCount() > 0)) {
                           Component[] c3 = viewer.getComponents();
                           for (int i=0; i<c3.length; i++) {
                              if ((c3[i] != null) && s.equals(c3[i].getName())) {
                                 try {
                                    CalScrollPane csp = (CalScrollPane)c3[i];
                                    iframe = (CalViewer)(csp.getViewport().getView());
                                    reuse = true;
                                 } catch (Exception e4) {
                                    System.err.println("Error casting component in Liner");
                                    iframe = null;
                                    viewer.remove(c3[i]);
                                 }
                                 break;
                              }
                           }
                        }
                        if (!reuse) {
                           iframe = new CalViewer(viewer.pane, pref,
                                                     viewer.hist, viewer, viewer.nesting + 1);
                           tagframe.setViewerProperties(iframe);
                           iframe.frameType = IFRAME;
                           iframe.sp.setName(s);
                           if ((iframe.frameborder == 0) || (iframe.scrolling == V_NO)) {
                              iframe.sp.setBorder(null);
                           } else {
                              iframe.sp.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
                           }
                           iframe.shadow = CalColor.getShadow(bgc);
                           iframe.highlight = CalColor.getHighlight(bgc);
                           iframe.sp.setVisible(false);
                           viewer.add(iframe.sp);
                           iframe.sp.setBounds(0,0,w,n);
                           if ((viewer.waitingFrameState != null) &&
                                 (view.iFrameIndex < viewer.waitingFrameState.length) &&
                                    (viewer.waitingFrameState[view.iFrameIndex] != null)) {
                              iframe.name = viewer.waitingFrameState[view.iFrameIndex].targetFrame;
                              iframe.showDocument(viewer.waitingFrameState[view.iFrameIndex],
                                                  null, viewer.waitingReload, HISTORY_HIST, 0);
                           } else {
                              if (iframe.url != null) {
                                 if (tagframe.name != null) {
                                    iframe.name = tagframe.name;
                                 } else {
                                    x = (int)(Math.random() * 1000000000);
                                    iframe.name = Integer.toString(x);
                                 }
                                 iframe.showHTMLDocument(new CalHistoryItem(iframe.url,
                                 iframe.name, iframe.url.getRef(), null, null), false, HISTORY_NULL);
                              }
                           }
                        }
                        view.addIFrame(iframe);
                        viewer.hasChildren = true;

                        if (tagframe.hspace > 0) {
                           w += (tagframe.hspace << 1);
                        }
                        if (tagframe.vspace > 0) {
                           n += (tagframe.vspace << 1);
                        }
                        if ((m == V_LEFT) || (m == V_RIGHT)) {    //floating
                           CalElement temp = new CalElement(cNameHash);
                           temp.setItem(I_IFRAME, view.iFrameIndex);
                           temp.width = w;
                           temp.attr = ((n << 10) | temp.attr);  //height is recorded in attr value
                           temp.font = m; //should be okay to use font int for this
                           addFloatItem(temp, m);
                           cFloatOn = true;
                           if ((cLineEnd == cLineStart) && (!cWordWaiting)) {
                              insertFloat();
                           }
                        } else {
                           if (cWordWaiting) {
                              addWordToLine();
                           }
                           if (cWidthSoFar + w > cWidth) {
                              addLine();
                           }
                           element.reset(cNameHash);
                           element.setItem(I_IFRAME, view.iFrameIndex);
                           element.width = w;
                           element.attr = ((n << 10) | element.attr);  //height is recorded in attr value
                           element.font = m;  //should be okay to use font int for this 
                           cTotWordW = w;
                           view.addElement(element);
                           addWordToLine();   //rem this sets cTotWordW to 0
                           checkObjectMetrics(n, m);
                           if (cLeftMargin + w > cFinalWidth) {
                              cFinalWidth = cLeftMargin + w;
                           }
                           if (cWidthSoFar > cWidth) {
                              addLine();
                           }
                        }
                        view.iFrameIndex++;
                        break;
          default: break;
      }
   }


   void handleCloseTag() {
   
      switch (tag) {
         case A      : cLinkHash = 0;
                       if (cFontOverride > 0) {
                          cFontOverride = 0;
                          computeFontInteger();
                       }
                       break;
         case P      : newParagraph();
                       setAlignment(-1);     //default
                       break;
        case CENTER  :
        case DIV     : finalizeLine();
                       cBreakActive = true;
                       popAlignment();
                       break;
        case BR      : addLineOrLineBreak();
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
        case SUB     : if (cSubOn) {
                          cSubOn = false;
                          cWordFlag -= (cWordFlag & 1);
                          if (cSupOn) {
                             cWordFlag = (cWordFlag | 2);   //turn sup back on
                          }
                          popFont();
                       }
                       break;
        case SUP     : if (cSupOn) {
                          cSupOn = false;
                          cWordFlag -= (cWordFlag & 2);
                          if (cSubOn) {
                             cWordFlag = (cWordFlag | 1);   //turn sup back on
                          }
                          popFont();
                       }
                       break;
        case STRIKE  : cWordFlag -= (cWordFlag & 16);
                       break;
        case U       : cWordFlag -= (cWordFlag & 32);
                       break;
        case PRE     : newParagraph();   //...and continue to TT
        case FONT    :
        case BIG     :
        case SMALL   :
        case CODE    :
        case KBD     :
        case SAMP    :
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
                       newParagraph();
                       setAlignment(-1);    //default
                       break;
        case ADDRESS : setFontStyle(ITAL, -1);
                       finalizeLine();
                       break;
      case BLOCKQUOTE: newParagraph();
                       reduceLeftMargin(cIndent, true);
                       break;
        case MENU    :
        case DIR     :
        case UL      : if ((cCurrList != null) && (cCurrList.type == I_UL)) {
                          if (listStack.empty()) {
                             newParagraph();
                          }
                          finalizeLine();
                          reduceLeftMargin(cIndent, false);
                          popList();
                       }
                       break;
        case OL      : if ((cCurrList != null) && (cCurrList.type == I_OL)) {
                          if (listStack.empty()) {
                             newParagraph();
                          }
                          finalizeLine();
                          reduceLeftMargin(cIndent, false);
                          popList();
                       }
                       break;
        case DL      : if (cDefListCount > 0) {
                          if (cDefListCount == 1) {
                             newParagraph();
                          }
                          finalizeLine();
                          setAlignment(-1);    //default
                          if (cDDactive) {
                             reduceLeftMargin(cIndent, false);
                             cDDactive = false;
                          }
                          if (cDefListCount > 1) {
                             reduceLeftMargin(cIndent, false);
                          }
                          cDefListCount--;
                       }
                       break;
         case FORM   : newParagraph();
                       break;
           default: break;
      }
   }


   void finalizeLine() {
   
      if (cWordWaiting) {
         addWordToLine();
      }
      addLine();
   }


   void lineBreak() {
   
      cHeightSoFar += cLineHeight;
      if (cFloatOn) {
         checkFloatEnd();
      }
   }
   
   
   void addLineOrLineBreak() {

      if ((cLineEnd == cLineStart) && (!cWordWaiting)) { 
         lineBreak();
      } else {
         finalizeLine();
      }
   }
   

   void newParagraph() {

      if (cBreakActive) {
         finalizeLine();
         cHeightSoFar += 14;
         if (cFloatOn) {
            checkFloatEnd();
         }
         cBreakActive = false;
      }
   }
   
   
   void setHeader(int n) {
   
      newParagraph();
      setFontStyle(BOLD, 1);
      pushFont(cBaseFontFamily, cFontStyle, cFontOverride, cRootFontSize + n, cFontColor);
      setAlignment(getAttrArg(A_ALIGN, tagPos + 1));
   }
   

   void setAlignment(int n) {
   
      switch (n) {
         case V_LEFT: case V_RIGHT: case V_CENTER: cCurrentAlign = n; break;
             default: cCurrentAlign = cDefaultAlign; break;
      }
   }


   int getListStyle(int listType, int tPos) {
   
      String s = getAttrString(A_TYPE, tPos);
      int style = NONE;

      if (listType == I_UL) {
         if (("circle").equalsIgnoreCase(s)) {
            style = V_CIRCLE;
         } else if (("square").equalsIgnoreCase(s)) {
            style = V_SQUARE;
         } else if (("disc").equalsIgnoreCase(s)) {
            style = V_DISC;
         }                       
      } else {
         if (("1").equalsIgnoreCase(s)) {
            style = NUM;
         } else if (("a").equals(s)) {
            style = ALPHAL;
         } else if (("A").equals(s)) {
            style = ALPHAU;
         } else if (("i").equals(s)) {
            style = ROMANL;
         } else if (("I").equals(s)) {
            style = ROMANU;
         }                       
      }
      
      return style;
   }
   

   String getListItemString() {
   
      int m, n;
      String s = "";
      boolean upper = false;
      
      m = cCurrList.index;
      switch (cCurrList.style) {
         case ALPHAU : upper = true;
         case ALPHAL : while (true) {
                          if (m <= 26) {
                             s = String.valueOf((char)('a' + m - 1)) + s;
                             break;
                          } else {
                             n = m % 26;
                             if (n == 0) {
                                s = "z" + s;
                                m -= 26;
                                m = m / 26;
                             } else {
                                s = String.valueOf((char)('a' + n - 1)) + s;
                                m = (m - n) / 26;
                             }
                          }
                       }
                       break;
         case ROMANU : upper = true;
         case ROMANL : if (m > 3999) {
                          s = Integer.toString(m);
                       } else {
                          if (m > 999) {
                             n = m / 1000;
                             m = m % 1000;
                             s += CalCons.romanNum[0][n-1];
                          }
                          if (m > 99) {
                             n = m / 100;
                             m = m % 100;
                             s += CalCons.romanNum[1][n-1];
                          }
                          if ((n = m / 10) > 0) {
                             s += CalCons.romanNum[2][n-1];
                          }
                          if ((n = m % 10) > 0) {
                             s += CalCons.romanNum[3][n-1];
                          }  
                       }
                       break;
             default : s = Integer.toString(m);
      }      
      if (upper) {
         s = s.toUpperCase();
      }
      s += ".";
      
      return s;
   }


   int getFontLevel(int n) {
   
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

   
   void pushFont(int family, int style, int override, int size, int color) {
   
      size = Math.max(Math.min(size, f.fontSizes.length - 1), 0);
      f.checkFont(family, style, size);
      fontStack.push(new CalStackFont(family, style, override, size, color));
      if (override == 1) {
         cOverrideCount++;
      }
      cFontFamily   = family;
      cFontStyle    = style;
      cFontOverride = override;
      cFontSize     = size;
      cFontColor    = color;
      checkFontMetrics();
   }
   
 
   void popFont() {
   
      try {
         fontStack.pop();
         sf = (CalStackFont)fontStack.peek();
         cFontFamily   = sf.family;
         cFontStyle    = sf.style;
         cFontOverride = sf.override;
         cFontSize     = sf.size;
         cFontColor    = sf.color;
         if (sf.override == 1) {
            cOverrideCount--;
            if (cOverrideCount == 0) {
               cFontOverride = 0;
            }
         }
      } catch (EmptyStackException e) {
         cFontFamily = cBaseFontFamily;
         //cFontStyle  = bf.style;
         cFontOverride = 0;
         cFontSize     = cBaseFontSize;
         cFontColor    = bf.color;
         f.checkFont(cFontFamily, cFontStyle, cFontSize);
      }
      checkFontMetrics();
   }
   
   
   void setFontStyle(int style, int setOrUnset) {   //1 = set, -1 = unset
         
      int n;
      
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
      checkFontMetrics();
   }


   private void checkFontMetrics() {
   
      int n;
      
      cSpaceWidth = f.spaceW[cFontFamily][cFontStyle][cFontSize];
      cLineHeight = f.fm[cFontFamily][cFontStyle][cFontSize].getHeight();
      n = f.fm[cFontFamily][cFontStyle][cFontSize].getMaxDescent();
      cDescent = n + 1;
      if (cSubOn) {
         cLineHeight += n;
         cDescent += n;
      }
      if (cSupOn) {
         cLineHeight += n + 2;
      }
      computeFontInteger();
   }


   private void computeFontInteger() {
   
      cCurrentFontInt = (cFontFamily << 8) | (cFontOverride << 7) | 
                               (cFontStyle << 4) | (cFontSize);
   }


   void pushList(CalStackList list) {
   
      listStack.push(list);
   }


   void popList() {
   
      if (listStack.empty()) {
         cCurrList = null;
      } else {
         cCurrList = (CalStackList)listStack.pop();
      }
   }


   void addFloatItem(CalElement item, int align) {
   
      if (align == V_LEFT) {
         if (floatElementL == null) {
            floatElementL = moreFloatL = item;
         } else {
            moreFloatL.next = item;
            moreFloatL = moreFloatL.next;
         }
      } else {
         if (floatElementR == null) {
            floatElementR = moreFloatR = item;
         } else {
            moreFloatR.next = item;
            moreFloatR = moreFloatR.next;
         }
      }
   }


   void insertFloat() {

      int n, n2, x, y, w, type;
      int pad = 0;
      int a[][] = null;
      CalElement current;
      
      view.setUpdatePaintable(false);
      if (cWordWaiting) {
         //this will be very, very rare, so we can afford a cludgy slow solution to it
         n = view.elementPos - cLineEnd;
         a = new int[9][n];
         for(int i=0; i<9; i++) {
            System.arraycopy(view.elements[i], view.elementPos - n, a[i], 0, n);
         }
         view.elementPos -= n;
      }
      if (floatElementL != null) {
         current  = floatElementL;
         if (floatStackL == null) {
            floatStackL = new Stack();
         }
         while (current != null) {
            y = 0;
            type = (current.item & 31);
            if (type == I_TABLE) {
               pad = 3;
               cLineStart = view.elementPos;
               n = view.linePos;
               n2 = view.charPos;
               view.setLockUpdatePaintable(true);
               view.addLine(0,0,0,0,0,0,0,0);
               CalTableView tView = view.tableViews[current.item >> 5];
               CalTable table = tView.table;
               w = table.getPreferredWidth(cFloatXR - cFloatXL, cDisplaySize);
               current.width = w;
               view.addElement(current);
               cLineEnd = view.elementPos;
               table.lineTable(viewer, f, view, tView, pref, cDocWidth, w, bf, cDisplaySize, cFloatXL,
                                                                                 cResizeTables, cNesting);
               y = tView.cTotalHeight + cHeightSoFar;
               tView.lastLineIndex    = Math.max(n, view.linePos - 1);
               tView.lastElementIndex = Math.max(cLineStart, view.elementPos - 1);
               view.setLineVals(n, cFloatXL, y, tView.cTotalHeight, 0, cLineStart, cLineEnd,
                                                                tView.lastLineIndex, 0);
               cLineEnd = view.elementPos;
               cLineStart = cLineEnd;
               view.charPos = n2;
               if (table.nesting == 0) {
                  view.setLockUpdatePaintable(false);
               }
            } else {   //it's an image or iframe etc
               pad = (type == I_FORM) ? 3 : 0;
               y = (current.attr >> 10) + cHeightSoFar;
               cLineStart = view.elementPos;
               view.addElement(current);
               cLineEnd = view.elementPos;
               view.addLine(cFloatXL, y, y - cHeightSoFar, 0, cLineStart, cLineEnd, 0, 0);
               cLineStart = cLineEnd;
            }
            cFloatXL += current.width;
            cFloatYL = y;
            floatStackL.push(new CalStackFloatItem(cFloatXL, cFloatYL, type));
            current = current.next;
         }
         cLeftMargin = cFloatXL + pad;
         if (cLeftMargin > cFinalWidth) {
            cFinalWidth = cLeftMargin;
         }
         floatElementL = null;
      }
      if (floatElementR != null) {
         current = floatElementR;
         if (floatStackR == null) {
            floatStackR = new Stack();
         }
         while (current != null) {
            y = 0;
            type = (current.item & 31);
            if (type == I_TABLE) {
               pad = 3;
               cLineStart = view.elementPos;
               n = view.linePos;
               n2 = view.charPos;
               view.setLockUpdatePaintable(true);
               view.addLine(0,0,0,0,0,0,0,0);
               CalTableView tView = view.tableViews[current.item >> 5];
               CalTable table = tView.table;
               w = table.getPreferredWidth(cFloatXR - cFloatXL, cDisplaySize);
               current.width = w;
               view.addElement(current);
               cLineEnd = view.elementPos;
               table.lineTable(viewer, f, view, tView, pref, cDocWidth, w, bf, cDisplaySize,
                                  Math.max(cFloatXL, cFloatXR - w), cResizeTables, cNesting);
               y = tView.cTotalHeight + cHeightSoFar;
               cFloatXR = Math.max(cFloatXL, cFloatXR - current.width);
               tView.lastLineIndex    = Math.max(n, view.linePos - 1);
               tView.lastElementIndex = Math.max(cLineStart, view.elementPos - 1);
               view.setLineVals(n, cFloatXR, y, tView.cTotalHeight, 0, cLineStart, cLineEnd,
                                                            tView.lastLineIndex, 0);
               cLineEnd = view.elementPos;
               cLineStart = cLineEnd;
               view.charPos = n2;
               if (table.nesting == 0) {
                  view.setLockUpdatePaintable(false);
               }
            } else {
               pad = (type == I_FORM) ? 3 : 0;
               y = (current.attr >> 10) + cHeightSoFar;
               cLineStart = view.elementPos;
               view.addElement(current);
               cLineEnd = view.elementPos;
               cFloatXR = Math.max(cFloatXL, cFloatXR - current.width);
               view.addLine(cFloatXR, y, y - cHeightSoFar, 0, cLineStart, cLineEnd, 0, 0);
               cLineStart = cLineEnd;
            }
            cFloatYR = y;
            if (cFloatXR + current.width > cFinalWidth) {
               cFinalWidth = cFloatXR + current.width;
            }
            floatStackR.push(new CalStackFloatItem(cFloatXR, cFloatYR, type));
            current = current.next;
         }
         cRightMargin = Math.max(cFloatXL, cFloatXR - pad);
         floatElementR = null;
      }
      view.setUpdatePaintable(true);
      cWidth = cRightMargin - cLeftMargin;
      if (cWidth <= 0) {
         if ((cFloatYL > 0) && (cFloatYR > 0)) {
            cHeightSoFar = Math.min(cFloatYL, cFloatYR) + 1;
         } else {
            cHeightSoFar = (cFloatYL > 0) ? cFloatYL + 1 : cFloatYR + 1;
         }
         checkFloatEnd();
      }
      if (a != null) {     //we need to add some waiting words to elements array
         view.addElements(a);
      }
   }


   void checkFloatEnd() {

      boolean change = false;
      CalStackFloatItem d;
      
      if (cFloatYL > 0) {
         while (cHeightSoFar > cFloatYL) {
            change = true;
            if (!floatStackL.empty()) {
               floatStackL.pop();
               if (!floatStackL.empty()) {
                  d = (CalStackFloatItem)floatStackL.peek();
                  cFloatXL = d.floatX; 
                  cFloatYL = d.floatY;
                  cLeftMargin = cFloatXL +
                        ((d.floatType == I_TABLE) || (d.floatType == I_FORM) ? 3 : 0);
               } else {
                  cLeftMargin = cTrueLeftMargin;
                  cFloatXL = cTrueLeftMargin;
                  cFloatYL = 0;
                  break;
               }
            } else {
               cLeftMargin = cTrueLeftMargin;
               cFloatXL = cTrueLeftMargin;
               cFloatYL = 0;
               break;
            }
         }
      }
      if (cFloatYR > 0) {
         while (cHeightSoFar > cFloatYR) {
            change = true;
            if (!floatStackR.empty()) {
               floatStackR.pop();
               if (!floatStackR.empty()) {
                  d = (CalStackFloatItem)floatStackR.peek();
                  cFloatXR = d.floatX; 
                  cFloatYR = d.floatY;
                  cRightMargin = cFloatXR -
                            ((d.floatType == I_TABLE) || (d.floatType == I_FORM) ? 3 : 0);
               } else {
                  cRightMargin = cTrueRightMargin;
                  cFloatXR = cTrueRightMargin;
                  cFloatYR = 0;
                  break;
               }
            } else {
               cRightMargin = cTrueRightMargin;
               cFloatXR = cTrueRightMargin;
               cFloatYR = 0;
               break;
            }
         }
      }
      if (change) {
         cWidth = cRightMargin - cLeftMargin;
         if ((cFloatYL == 0) && (cFloatYR == 0)) {
            cFloatOn = false;
         }
      }
   }


   void clearFloat(int arg) {
   
      if (cFloatOn) {
         if ((arg == V_LEFT) || (arg == V_ALL)) {
            if (floatStackL != null) {
               while (!floatStackL.empty()) {
                  CalStackFloatItem d = (CalStackFloatItem)floatStackL.pop();
                  if (d.floatY > cHeightSoFar) {
                     cHeightSoFar = d.floatY;
                  }
               }
            }
            cFloatYL = 0;
            cFloatXL = cTrueLeftMargin;
            cLeftMargin = cTrueLeftMargin;
         }
         if ((arg == V_RIGHT) || (arg == V_ALL)) {
            if (floatStackR != null) {
               while (!floatStackR.empty()) {
                  CalStackFloatItem d2 = (CalStackFloatItem)floatStackR.pop();
                  if (d2.floatY > cHeightSoFar) {
                     cHeightSoFar = d2.floatY;
                  }
               }
            }
            cFloatYR = 0;
            cFloatXR = cTrueRightMargin;
            cRightMargin = cTrueRightMargin;
         }
         if ((cFloatYL == 0) && (cFloatYR == 0)) {
            cFloatOn = false;
         }
         cWidth = cRightMargin - cLeftMargin;
      }
   }
   

   private void checkObjectMetrics(int height, int align) {

      if (height > cMaxObjectHeight) {
         cMaxObjectHeight = height;
         cMaxObjectAlign  = align;
      }
      switch(align) {
         case V_TOP   : cMaxObjectTop    = Math.max(cMaxObjectTop,    height); break;
         case V_MIDDLE: cMaxObjectMiddle = Math.max(cMaxObjectMiddle, height); break;
         case V_BOTTOM: cMaxObjectBottom = Math.max(cMaxObjectBottom, height); break;
      }
   }

   
   private void finalizeObjectMetrics() {
   
      int ma, n;

      //System.out.println("running finalize: max =" + cMaxObjectTop + ", " + cMaxObjectBottom);
      cMaxDescent = Math.max(Math.max(cMaxObjectMiddle >> 1, cMaxDescent),
                                                            cMaxObjectTop - cMaxAscent);
      //System.out.println("cDescent calculated at " + cMaxDescent);
      ma = Math.max(Math.max(cMaxObjectMiddle >> 1, cMaxAscent), cMaxObjectBottom);
      //System.out.println("Max ascent calc'd at " + ma);
      cMaxLineHeight = Math.max(cMaxLineHeight, cMaxObjectHeight);
      n = cMaxDescent + ma;
      if (n > cMaxLineHeight) {
         cMaxLineHeight = n;
      } else {
         n = cMaxLineHeight - n;
         switch (cMaxObjectAlign) {
            case V_ABSTOP   : cMaxDescent += n; break;
            case V_ABSMIDDLE: cMaxDescent += (n >> 1); break;
                     default: break;     //means in effect the ascent is raised by n
         }
      }
      //System.out.println("cMaxLineHeight = " + cMaxLineHeight);
   }
   
   
   private void reduceLeftMargin(int n, boolean twice) {
   
      if (cTrueLeftMargin - n < cBaseLeftMargin) {
         n = cTrueLeftMargin - cBaseLeftMargin;
      }
      cTrueLeftMargin -= n;
      cLeftMargin = Math.max(cTrueLeftMargin, cFloatXL);
      if (twice) {
         if (cTrueRightMargin + n > cBaseRightMargin) {
            n = cBaseRightMargin - cTrueRightMargin;
         }
         cTrueRightMargin += n;
         cRightMargin = Math.min(cTrueRightMargin, cFloatXR);
      }
      cWidth = cRightMargin - cLeftMargin;
   }
   
   
   private void increaseLeftMargin(int n, boolean twice) {

      cTrueLeftMargin += n;
      cLeftMargin += n;
      if (twice) {
         cTrueRightMargin -= n;
         cRightMargin -= n;
      }
      cWidth = cRightMargin - cLeftMargin;
   }
   
   
   private void pushAlignment(int align) {
   
      alignStack.push(new Integer(cDefaultAlign));
      cDefaultAlign = align;
      cCurrentAlign = align;
   }


   private void popAlignment() {
   
      if (alignStack.empty()) {
         cDefaultAlign = cBaseAlign;
      } else {
         cDefaultAlign = ((Integer)alignStack.pop()).intValue();
         cCurrentAlign = cDefaultAlign;
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


   void releaseMemory() {

      viewer  = null;
      f       = null;
      doc     = null;
      view    = null;
      pref    = null;
      sf      = null;
      bf      = null;
      element = null;
      cCurrList = null;
      fontStack = null;
      listStack = null;
      floatStackL = null;
      floatStackR = null;
      floatElementL = null;
      floatElementR = null;
      moreFloatL    = null;
      moreFloatR    = null;
      alignStack    = null;
   }
    
   public void run() {
      releaseMemory();
      this.stop();
   }

   //protected void finalize() {

   //   System.out.println("Finalize called for liner");
   //}  
}
