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

import java.util.Vector;
import java.util.Hashtable;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

class CalView implements CalCons {

   CalDoc           doc;
   int[][]          lines;
   int[][]          elements;
   int[]            tabArray;
   char[]           listArray;
   CalFrameset      frameset;
   CalTableView[]   tableViews;
   CalViewer[]      iFrames;
   CalForm[]        forms;
   int displaySize;   
   int linedWidth;
   int docWidth;
   int finalWidth;
   int linedHeight;
   int viewportHeight;
   int paintableLines;
   int lineState;
   int focusElementX;
   int focusElementY;
   int currTableNo;
   int componentIndex;
   int iFrameIndex;
   int formIndex;
   int tabIndex;
   int elementPos;
   int linePos;
   int charPos;
   int listPos;
   boolean relineOnHChange;
   boolean updatePaintable;
   boolean lockUpdatePaintable;


   CalView(CalDoc doc) {

      this(doc, false);
   }   


   CalView(CalDoc doc, boolean mini) {

      this.doc        = doc;
      paintableLines  = 0;
      currTableNo     = -1;
      componentIndex  = 0;
      iFrameIndex     = 0;
      formIndex       = 0;
      if (mini) {
         lines        = new int[8][20];
         elements     = new int[9][100];      
      } else {
         lines        = new int[8][1000];
         elements     = new int[9][5000];
         tabArray     = new int[200];
      }
      elementPos      = 0;
      linePos         = 0;
      charPos         = 0;
      listPos         = 0;
      updatePaintable = true;
   }
   

   void setFrameset(CalFrameset frameset) {
   
      this.frameset = frameset;
   }   
   
   
   synchronized void setLineState(int n) {

      lineState = n;
   }


   void addTableView(CalTableView tView) {
   
      currTableNo++;
      tView.cIndexNo = currTableNo;
      if (currTableNo == 0) {
         tableViews = new CalTableView[2];
         tableViews[currTableNo] = tView;
      } else if (currTableNo == tableViews.length) { 
         CalTableView[] a = new CalTableView[2 * currTableNo];
         System.arraycopy(tableViews, 0, a, 0, currTableNo);
         tableViews = a;
         tableViews[currTableNo] = tView;
      } else {
         tableViews[currTableNo] = tView;
      }
   }
   

   void addIFrame(CalViewer iframe) {
   
      if (iFrames == null) {
         iFrames = new CalViewer[2];
         iFrames[iFrameIndex] = iframe;
      } else {
         if (iFrameIndex == iFrames.length) {
            CalViewer[] a = new CalViewer[2 * iFrames.length];
            System.arraycopy(iFrames, 0, a, 0, iFrames.length);
            iFrames = a;
         }
         iFrames[iFrameIndex] = iframe;
      }
   }
   

   void addForm(CalForm form) {
   
      JComponent c;
      if (forms == null) {
         forms = new CalForm[5];
         forms[formIndex] = form;
      } else {
         if (formIndex == forms.length) {
            CalForm[] a = new CalForm[2 * forms.length];
            System.arraycopy(forms, 0, a, 0, forms.length);
            forms = a;
         }
         forms[formIndex] = form;
      }
   }
   
   
   void addTabItem(int n) {
   
      if (tabIndex == tabArray.length) {
         int[] a = new int[2 * tabIndex];
         System.arraycopy(tabArray, 0, a, 0, tabIndex);
         tabArray = a;
      }
      tabArray[tabIndex++] = n;
   }
   
   
   JComponent getFocusableComponent(CalForm form) {
   
      JComponent c = null;
      if (form.comp instanceof JScrollPane) {
         JScrollPane sp = (JScrollPane)form.comp;
         c = (JComponent)sp.getViewport().getView();
      } else {
         c = form.comp;
      }
      if ((!c.isEnabled()) || (!c.isFocusTraversable()) || (form.fItem.tagType == LABEL)) {
         c = null;
      }
      return c;
   }
         

   CalHistoryItem getHistoryState(String targetFrame) {

      CalHistoryItem item;
               
      //two passes on this - first try frames, then iframes/framesets
      for (int i=0; i<iFrameIndex; i++) {
         if (iFrames[i] == null) {
            break;
         } else if (iFrames[i].name.equals(targetFrame)) {
            if ((iFrames[i].doc == null) || (iFrames[i].doc.docURL == null)) {
               return null;
            }
            if (iFrames[i].frameset == null) {
               if ((iFrames[i].view != null) && (iFrames[i].view.iFrames != null)) {
                  return new CalHistoryItem(iFrames[i].doc.docURL, targetFrame, null,
                             iFrames[i].viewport.getViewPosition(), iFrames[i].view.getFrameState());
               } else {
                  return new CalHistoryItem(iFrames[i].doc.docURL, targetFrame, null,
                                                    iFrames[i].viewport.getViewPosition(), null);
               }   
            } else {
               return new CalHistoryItem(iFrames[i].doc.docURL, targetFrame, null, null,
                                                       iFrames[i].frameset.getFrameState());
            }
         }
      }
      for (int i=0; i<iFrameIndex; i++) {
         if (iFrames[i] == null) {
            break;
         } else {
            if (iFrames[i].frameset != null) {
               item = iFrames[i].frameset.getHistoryState(targetFrame);
               if (item != null) {
                  return item;
               }
            } else if ((iFrames[i].view != null) && (iFrames[i].view.iFrames != null)) {
               item = iFrames[i].view.getHistoryState(targetFrame);
               if (item != null) {
                  return item;
               }
            }
         }
      }
      return null;
   }


   CalHistoryItem[] getFrameState() {
   
      //no need to check if iFrames is null - caller has already done this      
      
      CalHistoryItem[] frameState = new CalHistoryItem[iFrameIndex];
      for (int i=0; i<iFrameIndex; i++) {
         if (iFrames[i] == null) {
            break;
         } else {
            if ((iFrames[i].doc != null) && (iFrames[i].doc.docURL != null)) {
               if (iFrames[i].frameset != null) {
                  frameState[i] = new CalHistoryItem(iFrames[i].doc.docURL, iFrames[i].name, null,
                                                     null, iFrames[i].frameset.getFrameState());
               } else {
                  if ((iFrames[i].view != null) && (iFrames[i].view.iFrames != null)) {
                     frameState[i] = new CalHistoryItem(iFrames[i].doc.docURL, iFrames[i].name,
                        null, iFrames[i].viewport.getViewPosition(), iFrames[i].view.getFrameState());
                  } else {
                     frameState[i] = new CalHistoryItem(iFrames[i].doc.docURL, iFrames[i].name,
                                           null, iFrames[i].viewport.getViewPosition(), null);
                  }
               }
            }  
         }
      }
      return frameState;
   }      


   int showDocument(CalHistoryItem h, String s, boolean reload, int histState, int link) {
   
      int result;
      //rem no need to check if iFrames[] is null - caller has already done this
      for (int i=0; i<iFrameIndex; i++) {
         if (iFrames[i] == null) {
            break;
         } else {
            result = iFrames[i].showDocument(h, s, reload, histState, link);
            if (result != NOT_FOUND) {
               return result;
            }
         }
      }
      return NOT_FOUND;
   }


   void stopAllProcesses() {

      for (int i=0; i<iFrameIndex; i++) {
         if (iFrames[i] == null) {
            break;
         } else {
            iFrames[i].stopAllProcesses();
         }
      }
   }


   boolean allFinished() {

      for (int i=0; i<iFrameIndex; i++) {
         if (iFrames[i] == null) {
            break;
         } else {
            if (!iFrames[i].allFinished()) {
               return false;
            }
         }
      }
      return true;
   }


   void updateLinks() {

      for (int i=0; i<iFrameIndex; i++) {
         if (iFrames[i] == null) {
            break;
         } else {
            iFrames[i].updateLinks();
         }
      }
   }


   CalViewer getFrame(String target) {
   
      CalViewer vw;
      for (int i=0; i<iFrameIndex; i++) {
         if (iFrames[i] == null) {
            return null;
         } else {
            if ((vw = iFrames[i].getFrame(target)) != null) {
               return vw;
            } 
         }
      }
      return null;
   }


   void getComponents(Hashtable table, String targetFrame, boolean isTarget) {
   
      if ((forms != null) && (isTarget || (targetFrame == null))) {
         for (int i=0; i<formIndex; i++) {
            if ((forms[i] != null) && (forms[i].comp != null) && (forms[i].fItem.id != null)) {
               table.put(forms[i].fItem.id, forms[i].comp);
            }
         }
      }
      if ((iFrames != null) && ((targetFrame == null) || (!isTarget))) {
         for (int i=0; i<iFrameIndex; i++) {
            if (iFrames[i] != null) {
               iFrames[i].getComponents(table, targetFrame);
            }
         }
      }      
   }


   Rectangle getElementRect(int hashNo, int type) {

      int x;
      int n = 0;
      Rectangle r;
      boolean isHash = ((type == E_NAMEH) || (type == E_LINKH)) ? true : false; 

      for (int i=0; i<paintableLines; i++) {
         if (lines[L_EE][i] > lines[L_ES][i]) {
            x = lines[L_X][i];
            for (int j=lines[L_ES][i]; j<lines[L_EE][i]; j++) {
               if (isHash && (elements[type][j] == hashNo)) {
                  while ((j < lines[L_EE][i]) && (elements[type][j] == hashNo)) {
                     n += elements[E_WIDTH][j];
                     j++;
                  }                      
                  return new Rectangle(x, lines[L_Y][i] + lines[L_D][i] - lines[L_H][i], n, lines[L_H][i]);
               } else if ((elements[E_ATTR][j] & 8) > 0) {
                  n = (elements[E_ITEM][j] & 31);
                  if (n == I_TABLE) {
                     CalTableView tView = tableViews[elements[E_ITEM][j] >> 5];
                     r = tView.getElementRect(this, hashNo, lines[L_Y][i] - lines[L_H][i], type);
                     if (r != null) {
                        return r;
                     }
                  } else if (((n == I_FORM)&&(type == I_FORM)) || ((n == I_IFRAME)&&(type == I_IFRAME)))  {
                     n = (elements[E_ITEM][j] >> 5);
                     if (n == hashNo) {
                        return new Rectangle(x, lines[L_Y][i] + lines[L_D][i] - lines[L_H][i],
                                                                  elements[E_WIDTH][j], lines[L_H][i]);                            
                     }
                  }
               } 
               x += elements[E_WIDTH][j];
            }
         }
      }
      return null;
   }


   //method for adding a word to the element array
   void addElement(int attr, int width, int name, int link, int font, int color, int count) {

      if (elementPos >= elements[0].length) {
         redimElementArray(elementPos, elementPos << 1);
      }
      elements[0][elementPos] = attr;
      elements[1][elementPos] = 0;
      elements[2][elementPos] = width;
      elements[3][elementPos] = name;
      elements[4][elementPos] = link;
      elements[5][elementPos] = font;
      elements[6][elementPos] = color;
      elements[7][elementPos] = charPos;
      elements[8][elementPos] = count;
      
      charPos += count;
      elementPos++;
   }

   //method for adding a space to the element array
   void addElement(int attr, int width, int name, int link, int font, int color) {

      if (elementPos >= elements[0].length) {
         redimElementArray(elementPos, elementPos << 1);
      }
      attr = 4 | attr;     // bit 3 set for isSpace
      elements[0][elementPos] = attr;
      elements[1][elementPos] = 0;
      elements[2][elementPos] = width;
      elements[3][elementPos] = name;
      elements[4][elementPos] = link;
      elements[5][elementPos] = font;
      elements[6][elementPos] = color;
      elements[7][elementPos] = 0;
      elements[8][elementPos] = 0;
      elementPos++;
   }


   //method for adding a special element to the element array
   void addElement(CalElement e) {

      if (elementPos >= elements[0].length) {
         redimElementArray(elementPos, elementPos << 1);
      }
      elements[0][elementPos] = e.attr;
      elements[1][elementPos] = e.item;
      elements[2][elementPos] = e.width;
      elements[3][elementPos] = e.nameHash;
      elements[4][elementPos] = e.linkHash;
      elements[5][elementPos] = e.font;
      elements[6][elementPos] = e.fontColor;
      elements[7][elementPos] = e.charStart;
      elements[8][elementPos] = e.charCount;
      elementPos++;
   }


   //method for adding an ordered list item to the element array.
   void addElement(CalElement e, String s) {

      int len = s.length();
      if (listArray == null) {
         listArray = new char[Math.max(len, 100)];
      } else if (listPos + len >= listArray.length) {     
         char[] a = new char[listPos + Math.max(len, 100)];
         synchronized(listArray) {
            System.arraycopy(listArray, 0, a, 0, listArray.length);
            listArray = a;
         }
      }
      if (listPos >= elements[0].length) {
         redimElementArray(elementPos, elementPos << 1);
      }
      elements[0][elementPos] = e.attr;
      elements[1][elementPos] = e.item;
      elements[2][elementPos] = e.width;
      elements[3][elementPos] = e.nameHash;
      elements[4][elementPos] = e.linkHash;
      elements[5][elementPos] = e.font;
      elements[6][elementPos] = e.fontColor;
      elements[7][elementPos] = listPos;
      elements[8][elementPos] = len;
      for (int i=0; i<len; i++) {
         listArray[listPos++] = s.charAt(i);
      }
      elementPos++;
   }


  //method for adding a block of elements to elements array - a rare case regarding floaters
   void addElements(int[][] a) {

      int len = a[0].length;
      if (elementPos + len >= elements[0].length) {     
         redimElementArray(elementPos, ((elementPos + len) << 1));
      }
      for(int i=0; i<9; i++) {
         System.arraycopy(a[i], 0, elements[i], elementPos, len);
      }
      elementPos += len;
   }


   //removes end of line space
   void killSpace(int pos) {

      elements[E_WIDTH][pos] = 0;
      elements[E_ATTR][pos] = 0;
   }
   
   
   //blocks the updating of lines even when updatePaintable(below) is true. Set by a table when lining
   void setLockUpdatePaintable(boolean b) {
   
      lockUpdatePaintable = b;
      if (!lockUpdatePaintable) {
         paintableLines = linePos;
      }      
   }


   //setting updatePaintable to false blocks the incrementing of paintable lines e.g when a table is lined
   void setUpdatePaintable(boolean b) {
   
      if (!lockUpdatePaintable) {
         updatePaintable = b;
      }
   }
   

   void addLine(int x, int y, int height, int descent, int start, int end, int jump, int textascent) {

      if (linePos >= lines[0].length) {
         redimLineArray(linePos, linePos << 1);
      }
      lines[0][linePos] = x;
      lines[1][linePos] = y;
      lines[2][linePos] = height;
      lines[3][linePos] = descent;
      lines[4][linePos] = start;
      lines[5][linePos] = end;
      lines[6][linePos] = jump;
      lines[7][linePos] = textascent;
      linePos++;
      if (updatePaintable) {
         paintableLines = linePos;
      }
   }


   //sets the fields for a particular line 
   void setLineVals(int lineNo, int x, int y, int height, int descent, int start, int end, int jump,
                                                                                   int textascent) {

      lines[0][lineNo] = x;
      lines[1][lineNo] = y;
      lines[2][lineNo] = height;
      lines[3][lineNo] = descent;
      lines[4][lineNo] = start;
      lines[5][lineNo] = end;
      lines[6][lineNo] = jump;
      lines[7][lineNo] = textascent;
   }


   //called when lining is complete - arrays are scaled down
   synchronized void finalizeArrays() {
   
      if (elementPos > 0) {
         redimElementArray(elementPos, elementPos);
      }
      if (linePos > 0) {
         redimLineArray(linePos, linePos);
      }
      paintableLines = linePos;
   }


   private synchronized void redimElementArray(int oldv, int newv) {

      if (newv > 499999) {
         elementPos--;   //try to prevent massive doc killing calpa
         return;
      }
      for (int i=0; i<elements.length; i++) {
         int[] a = new int[newv];
         System.arraycopy(elements[i], 0, a, 0, oldv);
         elements[i] = a;
      }
   }


   private synchronized void redimLineArray(int oldv, int newv) {

      for (int i=0; i<lines.length; i++) {
         int[] a = new int[newv];
         System.arraycopy(lines[i], 0, a, 0, oldv);
         lines[i] = a;
      }
   }

}
