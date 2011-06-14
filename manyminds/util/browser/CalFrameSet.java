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
import javax.swing.FocusManager;
import java.net.URL;
import java.util.Hashtable;

class CalFrameset extends JPanel implements CalCons {
   
   CalViewer          parent;
   CalHTMLPreferences pref;
   CalLength[]    cRows;
   CalLength[]    cCols;
   CalViewer[]    cChildren;
   URL            baseURL;
   boolean[]      colResize;
   boolean[]      rowResize;
   boolean[]      vSpacing;
   boolean[]      hSpacing;
   int[]          colPos;
   int[]          rowPos;
   int[]          colWidth;
   int[]          rowHeight;
   int            spacing;
   int            totHSpacing;
   int            totVSpacing;
   int            numChildren;
   int            cNumRows;
   int            cNumCols;
   int            nesting;
   int            lastX;
   int            lastY;
   int            minExt;
   int            maxExt;
   int            vOffset;
   int            hOffset;
   int            splitActive;
   int            splitIndex;
   int            markPos;
   boolean        frameBorders;
   boolean        dontCalcSpans;
   boolean        keepBorder;
   Rectangle      viewRect;

   CalFrameset(CalHTMLPreferences pref, CalViewer parent, CalTagFrameset fset, int nesting,
                                                  URL baseURL, int spacing, boolean frameBorders) {
      this.pref    = pref;
      this.parent  = parent;
      this.nesting = nesting;
      this.baseURL = baseURL;
      this.spacing = spacing;
      this.frameBorders = frameBorders;
      
      setupRowsAndCols(fset);
      setLayout(new FramesetLayout());
      addMouseListener(new ResizeListener());
      addMouseMotionListener(new CursorListener());
      viewRect = new Rectangle(0,0,0,0);    //should avoid any null errors
   }      
   
   
   private void setupRowsAndCols(CalTagFrameset fset) {
   
      if (fset.spacing != -1) {
         spacing = fset.spacing;
      }
      if (fset.frameBorders == 2) {
         frameBorders = false;
      } else if (fset.frameBorders == 1) {
         frameBorders = true;
      }
      
      cRows = new CalLength[fset.cRows.length];
      System.arraycopy(fset.cRows, 0, cRows, 0, fset.cRows.length);
      cCols = new CalLength[fset.cCols.length];
      System.arraycopy(fset.cCols, 0, cCols, 0, fset.cCols.length);
      numChildren = fset.numChildren;
      cChildren = new CalViewer[numChildren];
      cNumRows  = fset.cNumRows;
      cNumCols  = fset.cNumCols;
      rowPos    = new int[cNumRows - 1];
      colPos    = new int[cNumCols - 1];
      hSpacing  = new boolean[cNumRows - 1];
      rowResize = new boolean[cNumRows - 1];
      vSpacing  = new boolean[cNumCols - 1];
      colResize = new boolean[cNumCols - 1];
   }


   int fillChildren(CalDoc doc, int pos) {
   
      pos++;
      int n;
      int i = 0;
      int end = 0;
      int tag;
      int tagPos;
      boolean noResize;
      boolean hasBorder = frameBorders;
    
      while (i < numChildren) {
         if (pos >= doc.tokenCodes.length) {
            break;
         }
         if (doc.tokenCodes[pos] < 0) {
            tagPos = Math.abs(doc.tokenCodes[pos]);
            tag = doc.tags[tagPos];
            if (tag > 0) {
               if ((tag == FRAMESET) || (tag == FRAME)) {
                  CalViewer viewer = new CalViewer(parent.pane, pref, parent.hist, parent, nesting + 1);
                  viewer.sp.setFrameset(this);
                  if (tag == FRAMESET) {
                     CalTagFrameset tf = (CalTagFrameset)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                     CalFrameset fSet = new CalFrameset(pref, viewer, tf, nesting + 1,
                                                                  baseURL, spacing, frameBorders);
                     //if the child frameset has invisible frames, but this frameset hasn't got
                     //invisible frames, then keep the scrollpane border around the viewer which contains
                     //the child frameset
                     if (!fSet.frameBorders && this.frameBorders) {
                        fSet.keepBorder = true;
                     }
                     pos = fSet.fillChildren(doc, pos);
                     viewer.setFrameset(fSet);
                     viewer.frameborder = 0;
                  } else {
                     CalTagFrame tagframe = (CalTagFrame)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                     tagframe.setViewerProperties(viewer);
                     if (viewer.name.equals("%")) {
                        viewer.name = baseURL.toString() + Integer.toString(nesting) + "#" +
                                                                            Integer.toString(i);
                     }
                     if ((!frameBorders) || (viewer.frameborder == 0)) {
                        viewer.frameborder = 0;                    
                        viewer.sp.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                     }
                  }
                  add(viewer.getScrollPane());
                  cChildren[i] = viewer;
                  i++;
               }
            } else if (tag == FRAMESET) {       //closed tag
               end = pos;
               break;
            }
         }
         pos++;
      }
      if (end == 0) {
         while (pos < doc.tokenCodes.length) {
            if (doc.tokenCodes[pos] < 0) {
               tag = doc.tags[Math.abs(doc.tokenCodes[pos])];
               if ((tag < 0) && (Math.abs(tag) == FRAMESET)) {
                  end = pos;
                  break;
               }
            }
            pos++;
         }
         if (end == 0) {
            end = doc.tokenCodes.length - 1;
         }
      }

      //calc the boolean resize & spacing vals. If any frame bordering a col or row has the noresize attr
      //then that row/col cannot be resized. Similar with borders.
      if (numChildren > 0) {
         if (cNumRows > 1) {
            n = 0;
            for (i=0; i<cNumRows; i++) {
               for (int j=0; j<cNumCols; j++) {
                  if (cChildren[n] != null) {
                     noResize = !cChildren[n].noresize;
                     if (i == 0) {
                        rowResize[0] = noResize;
                        hSpacing[0] = hasBorder;
                     } else if (i == cNumRows - 1) {
                        rowResize[cNumRows - 2] = noResize;
                        hSpacing[cNumRows - 2] = hasBorder;
                     } else {
                        rowResize[i - 1] = noResize;
                        rowResize[i] = noResize;
                        hSpacing[i - 1] = hasBorder;
                        hSpacing[i] = hasBorder;
                     }
                  }
                  n++;
               }
            }
            //calc total hSpacing
            totHSpacing = 0;
            for (i=0; i<hSpacing.length; i++) {
               if (hSpacing[i]) {
                  totHSpacing += spacing;
               }
            }
         }               
         if (cNumCols > 1) {
            n = 0;
            for (i=0; i<cNumCols; i++) {
               for (int j=0; j<cNumRows; j++) {
                  if (cChildren[n] != null) {
                     noResize = !cChildren[n].noresize;
                     if (i == 0) {
                        colResize[0] = noResize;
                        vSpacing[0] = hasBorder;
                     } else if (i == cNumCols - 1) {
                        colResize[cNumCols - 2] = noResize;
                        vSpacing[cNumCols - 2] = hasBorder;
                     } else {
                        colResize[i - 1] = noResize;
                        colResize[i] = noResize;
                        vSpacing[i - 1] = hasBorder;
                        vSpacing[i] = hasBorder;
                     }
                  }
                  n++;
               }
            }
            //calc total vSpacing
            totVSpacing = 0;
            for (i=0; i<vSpacing.length; i++) {
               if (vSpacing[i]) {
                  totVSpacing += spacing;
               }
            }
         }
      }
      
      return end;
   }


   CalHistoryItem[] getFrameState() {
   
      if (cChildren == null) {
         return null;
      }      
      
      CalHistoryItem[] frameState = new CalHistoryItem[numChildren];
      for (int i=0; i<numChildren; i++) {
         if (cChildren[i] == null) {
            break;
         } else {
            if (cChildren[i].frameset != null) {
               if ((cChildren[i].doc != null) && (cChildren[i].doc.docURL != null)) {
                  frameState[i] = new CalHistoryItem(cChildren[i].doc.docURL, cChildren[i].name, null,
                                                     null, cChildren[i].frameset.getFrameState());
               } else {
                  frameState[i] = new CalHistoryItem(null, cChildren[i].name, null, null,
                                                     cChildren[i].frameset.getFrameState());
               }
            } else {
               if ((cChildren[i].doc != null) && (cChildren[i].doc.docURL != null)) {
                  if ((cChildren[i].view != null) && (cChildren[i].view.iFrames != null)) {
                     frameState[i] = new CalHistoryItem(cChildren[i].doc.docURL, cChildren[i].name,
                        null, cChildren[i].viewport.getViewPosition(), cChildren[i].view.getFrameState());
                  } else {
                     frameState[i] = new CalHistoryItem(cChildren[i].doc.docURL, cChildren[i].name,
                                              null, cChildren[i].viewport.getViewPosition(), null);
                  }
               }
            }  
         }
      }
      return frameState;
   }


   void activateFrames(CalHistoryItem[] frameState, boolean reload) {
   
      if (cChildren != null) {
         if ((frameState != null) && (frameState.length != cChildren.length)) {
            return;   //in theory shouldn't happen, but bug out if it does
         }
         for (int i=0; i<numChildren; i++) {
            if (cChildren[i] == null) {
               break;
            } else {
               if (cChildren[i].frameset != null) {
                  if (frameState == null) {
                     cChildren[i].frameset.activateFrames(null, false);
                  } else {
                     if (frameState[i] != null) {
                        cChildren[i].name = frameState[i].targetFrame;
                        cChildren[i].frameset.activateFrames(frameState[i].frameState, reload);
                     }
                  }
               } else {
                  if (frameState == null) {
                     if (cChildren[i].url != null) {
                        cChildren[i].showDocument(new CalHistoryItem(cChildren[i].url, cChildren[i].name,
                                   cChildren[i].url.getRef(), null, null), null, false, HISTORY_NULL, 0);
                     }
                  } else {
                     if (frameState[i] != null) {
                        cChildren[i].name = frameState[i].targetFrame;
                        cChildren[i].showDocument(frameState[i], null, reload, HISTORY_HIST, 0);
                     }
                  } 
               }
            }
         }
      }
   }


   CalHistoryItem getHistoryState(String targetFrame) {

      CalHistoryItem item;
               
      //two passes on this - first try frames, then framesets
      if (cChildren != null) {
         for (int i=0; i<numChildren; i++) {
            if (cChildren[i] == null) {
               break;
            } else if (cChildren[i].name.equals(targetFrame)) {
               if ((cChildren[i].doc == null) || (cChildren[i].doc.docURL == null)) {
                  return null;
               }
               if (cChildren[i].frameset == null) {
                  if ((cChildren[i].view != null) && (cChildren[i].view.iFrames != null)) {
                     return new CalHistoryItem(cChildren[i].doc.docURL, targetFrame, null,
                             cChildren[i].viewport.getViewPosition(), cChildren[i].view.getFrameState());
                  } else {
                     return new CalHistoryItem(cChildren[i].doc.docURL, targetFrame, null,
                                                       cChildren[i].viewport.getViewPosition(), null);
                  }
               } else {
                  return new CalHistoryItem(cChildren[i].doc.docURL, targetFrame, null, null,
                                                       cChildren[i].frameset.getFrameState());
               }
            }
         }
         for (int i=0; i<numChildren; i++) {
            if (cChildren[i] == null) {
               break;
            } else {
               if (cChildren[i].frameset != null) {
                  item = cChildren[i].frameset.getHistoryState(targetFrame);
                  if (item != null) {
                     return item;
                  }
               }
            }
         }
      }
      return null;
   }


   CalViewer getFrame(String target) {
   
      if (cChildren == null) {
         return null;
      }      
      
      CalViewer vw;
      for (int i=0; i<numChildren; i++) {
         if (cChildren[i] == null) {
            return null;
         } else {
            if ((vw = cChildren[i].getFrame(target)) != null) {
               return vw;
            } 
         }
      }
      return null;
   }


   void getComponents(Hashtable table, String targetFrame) {
   
      if (cChildren != null) {
         for (int i=0; i<numChildren; i++) {
            if (cChildren[i] != null) {
               cChildren[i].getComponents(table, targetFrame);
            } 
         }
      }
   }


   int showDocument(CalHistoryItem h, String s, boolean reload, int histState, int link) {
   
      int result;
      
      if (cChildren != null) {
         for (int i=0; i<numChildren; i++) {
            if (cChildren[i] == null) {
               break;
            } else {
               result = cChildren[i].showDocument(h, s, reload, histState, link);
               if (result != NOT_FOUND) {
                  return result;
               }
            }
         }
      }
      return NOT_FOUND;
   }


   void stopAllProcesses() {
   
      if (cChildren == null) {
         return;
      }      
      
      for (int i=0; i<numChildren; i++) {
         if (cChildren[i] == null) {
            break;
         } else {
            cChildren[i].stopAllProcesses();
         }
      }
   }


   boolean allFinished() {
   
      if (cChildren == null) {
         return true;
      }      

      for (int i=0; i<numChildren; i++) {
         if (cChildren[i] == null) {
            break;
         } else {
            if (!cChildren[i].allFinished()) {
               return false;
            }
         }
      }
      return true;
   }


   void updateLinks() {
   
      if (cChildren == null) {
         return;
      }      
      
      for (int i=0; i<numChildren; i++) {
         if (cChildren[i] == null) {
            break;
         } else {
            cChildren[i].updateLinks();
         }
      }
   }


   void releaseMemory() {
   
      if (cChildren == null) {
         return;
      }      
      
      for (int i=0; i<numChildren; i++) {
         if (cChildren[i] == null) {
            break;
         } else {
            if (cChildren[i].frameset != null) {
               cChildren[i].frameset.releaseMemory();
            } else {
               cChildren[i].removeAnyChildren();
            }
         }
      }
   }


   void tabBackForward(CalViewer caller, int offset, boolean newcycle) {
   
      if (cChildren == null) {
         return;
      }      
      int n = -1;
      //find index of next viewer
      if ((newcycle) || (caller == null) || ((parent != null) && (caller == parent))) {
         if (offset == 1) {
            for (int i=0; i<numChildren; i++) {
               if (cChildren[i] != null) {
                  n = i;
                  break;
               }
            }
         } else {
            for (int i=numChildren-1; i>-0; i--) {
               if (cChildren[i] != null) {
                  n = i;
                  break;
               }
            }
         }
         if (n == -1) {
            //this should never happen, so we'll bug out
            if ((parent != null) || (parent.pane != null)) {
               FocusManager.getCurrentManager().focusNextComponent(parent.pane);
            }
         }
      } else {
         for (int i=0; i<numChildren; i++) {
            if (cChildren[i] == caller) {
               n = i;
               break;
            }
         }
         if ( (n == -1) || ((n == 0) && ((offset == -1) || (numChildren == 1))) ||
                                                     ((offset == 1) && (n == numChildren - 1)) ) {
            //then there's an error or all the children for this frameset have been done
            //we need to inform the parent of this frameset's parent that we're done
            if (parent != null) {
               if ((parent.parent == null) || (n == -1)) {    //must be the nesting 0 viewer
                  if (parent.pane != null) {
                     FocusManager.getCurrentManager().focusNextComponent(parent.pane);
                  }
               } else {
                  parent.parent.tabBackForward(parent, offset, false);
               }
            }
            return;
         } else {
            n += offset;
         }
      }
      //if we reach here then we've found a new child to tab traverse
      if ((n >= 0) && (n < numChildren)) {
         if (cChildren[n].frameset != null) {
            cChildren[n].tabBackForward(null, offset, true);
         } else {
            cChildren[n].currTabIndex = -1;
            cChildren[n].requestFocus();
            cChildren[n].paintFocus = true;
            cChildren[n].repaint();
         }
      }
   }   

         
   private class ResizeListener extends MouseAdapter {
   
      public void mousePressed(MouseEvent e) {
      
         checkSplitter(e.getX(), e.getY(), true);
      }
      
      public void mouseReleased(MouseEvent e) {
      
         checkSplitter(e.getX(), e.getY(), false);
      }
   }


   private void checkSplitter(int x, int y, boolean pressed) {
      
      if ((!pressed) && (splitActive == V_VSIDES)) {
         drawSplitter(V_VSIDES, 0, true, false);
         x = Math.max(Math.min(maxExt, x), minExt);
         recomputeSpans(splitIndex, x - markPos, V_VSIDES);
         splitActive = 0;
         this.invalidate();
         if (parent != null) {
            parent.validate();
         } else {
            revalidate();
         }
      } else {
         if (cNumCols > 1) {
            for (int i=0; i<colPos.length; i++) {
               if (vSpacing[i] && (x >= colPos[i] - spacing - 2) && (x <= colPos[i]) && colResize[i]) {
                  if (parent != null) {
                     viewRect = parent.viewport.getViewRect();
                     markPos = x;
                     vOffset = x - (colPos[i] - spacing - 1);
                     minExt = (i == 0) ? viewRect.x + 5 : colPos[i-1] + 5;
                     maxExt = (i == colPos.length - 1) ? viewRect.x + viewRect.width - 8 :
                                                                        colPos[i+1] - spacing - 8;
                     splitActive = V_VSIDES;
                     splitIndex = i;
                     drawSplitter(V_VSIDES, x, false, true);
                     lastX = x;
                  }
                  return;
               }
            }
         }
      }
      if ((!pressed) && (splitActive == V_HSIDES)) {
         drawSplitter(V_HSIDES, 0, true, false);
         y = Math.max(Math.min(maxExt, y), minExt);
         recomputeSpans(splitIndex, y - markPos, V_HSIDES);
         splitActive = 0;
         this.invalidate();
         if (parent != null) {
            parent.validate();
         } else {
            revalidate();
         }
      } else {
         if (cNumRows > 1) {
            for (int i=0; i<rowPos.length; i++) {
               if (hSpacing[i] && (y >= rowPos[i] - spacing - 2) && (y <= rowPos[i]) && rowResize[i]) {
                  if (parent != null) {
                     viewRect = parent.viewport.getViewRect();
                     markPos = y;
                     hOffset = y - (rowPos[i] - spacing - 1);
                     minExt = (i == 0) ? viewRect.y + 5 : rowPos[i-1] + 5;
                     maxExt = (i == rowPos.length-1) ? viewRect.y + viewRect.height - 8 :
                                                                             rowPos[i+1] - spacing - 8;
                     splitActive = V_HSIDES;
                     splitActive = V_HSIDES;
                     splitIndex = i;
                     drawSplitter(V_HSIDES, y, false, true);
                     lastY = y;
                  }
                  return;
               }
            }
         }
      }
      
      splitActive = 0;
   }   
   
   
   private void recomputeSpans(int index, int change, int orientation) {
   
      int span, n, old;
      
      if (orientation == V_VSIDES) {
         span = viewRect.width - totVSpacing;
         if (change == 0) {
            return;      //...because the splitter hasn't moved
         } else {
            old = (((colWidth[index] + colWidth[index + 1]) * 100) << 8) / span;
            cCols[index].type = PERCENT;
            cCols[index].value = (((colWidth[index] + change) * 100) << 8) / span;
            cCols[index + 1].type = PERCENT;
            cCols[index + 1].value = old - cCols[index].value;
            colWidth[index] += change;
            colPos[index] += change;
            colWidth[index + 1] -= change;
            //dontCalcSpans = true;   //ANDY - removed 'cos it was causing double-validation
                                      //Think I originally put it in to save computation time
         }
      } else {
         span = viewRect.height - totHSpacing;
         if (change == 0) {
            return;      //...because the splitter hasn't moved
         } else {
            old = (((rowHeight[index] + rowHeight[index + 1]) * 100) << 8) / span;
            cRows[index].type = PERCENT;
            cRows[index].value = (((rowHeight[index] + change) * 100) << 8) / span;
            cRows[index + 1].type = PERCENT;
            cRows[index + 1].value = old - cRows[index].value;
            rowHeight[index] += change;
            rowPos[index] += change;
            rowHeight[index + 1] -= change;
            //dontCalcSpans = true;   //ANDY - removed 'cos it was causing double-validation
                                      //Think I originally put it in to save computation time

         }
      }
   }
   
   
   private class CursorListener extends MouseMotionAdapter {
   
      public void mouseMoved(MouseEvent e) {
      
         int x = e.getX();
         int y = e.getY();
         if (cNumCols > 1) {
            for (int i=0; i<colPos.length; i++) {
               if (vSpacing[i] && (x >= (colPos[i] - spacing - 2)) && (x <= colPos[i])) {
                  if ((colResize[i]) && (getCursor().getType() != Cursor.E_RESIZE_CURSOR)) {
                     setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                  }
                  return;
               }
            }
         }
         if (cNumRows > 1) {
            for (int i=0; i<rowPos.length; i++) {
               if (hSpacing[i] && (y >= (rowPos[i] - spacing - 2)) && (y <= rowPos[i])) {
                  if ((rowResize[i]) && (getCursor().getType() != Cursor.N_RESIZE_CURSOR)) {
                     setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                  }
                  return;
               }
            }
         }
         if (getCursor().getType() != Cursor.DEFAULT_CURSOR) {
            setCursor(Cursor.getDefaultCursor());
         }
      }
      
      public void mouseDragged(MouseEvent e) {
         
         if (splitActive == V_VSIDES) {
            int x = e.getX();
            x = Math.max(Math.min(maxExt, x), minExt);
            drawSplitter(V_VSIDES, x, true, true);
            lastX = x;
         } else if (splitActive == V_HSIDES) {
            int y = e.getY();
            y = Math.max(Math.min(maxExt, y), minExt);
            drawSplitter(V_HSIDES, y, true, true);
            lastY = y;
         }
      }
   }


   private void drawSplitter(int orientation, int pos, boolean erase, boolean drawNew) {

      Graphics g = this.getGraphics();
      if (g != null) {
         g.setXORMode(CalColor.paleGray);
         if (orientation == V_VSIDES) {
            if (erase) {
               drawVerticalSplitter(g, lastX, viewRect.y, viewRect.height);
            }
            if (drawNew) {
               drawVerticalSplitter(g, pos, viewRect.y, viewRect.height);
            }
         } else {
            if (erase) {
               drawHorizontalSplitter(g, lastY, viewRect.x, viewRect.width);
            }
            if (drawNew) {
               drawHorizontalSplitter(g, pos, viewRect.x, viewRect.width);
            }
         }
      }
      g.dispose();
   }


   private void drawVerticalSplitter(Graphics g, int x, int y, int h) {

      x = x - vOffset;
      h = y + h - 2;
      y += 2;
      int x2  = x + 5;
      int y2  = y + 5;

      g.drawLine(x + 4, y, x + 5, y + 1);
      g.drawLine(x + 2, y, x + 5, y + 3);
      while (y2 < h) {
         g.drawLine(x, y , x2, y2);
         y  += 2;
         y2 += 2;
      }
      y2 -= 2;
      g.drawLine(x, y, x + 3, y2);
      g.drawLine(x, y + 2, x + 1, y2);
   }

   
   private void drawHorizontalSplitter(Graphics g, int y, int x, int w) {

      y = y - hOffset;
      w = x + w - 2;
      x += 2;
      int x2 = x + 5;
      int y2 = y + 5;
      
      g.drawLine(x, y + 1, x + 1, y);
      g.drawLine(x, y + 3, x + 3, y);
      while (x2 < w) {
         g.drawLine(x, y2 ,x2, y);
         x  += 2;
         x2 += 2;
      }
      x2 -= 2;
      g.drawLine(x, y2, x + 3, y + 2);
      g.drawLine(x + 2, y2, x + 3, y + 4);      
   }

   
   private class FramesetLayout extends CalLayoutAdapter {
              
      public void layoutContainer(Container target) {

         if (parent == null) {
            return;
         }
         
         int width  = target.getSize().width;
         int height = target.getSize().height;
         int compw;
         int comph;
         int top      = 0;
         int left     = 0;
         int n        = 0;
         Component[]  comp = CalFrameset.this.getComponents();
         
         if (comp == null) {
            return;
         }
         
         if (numChildren == 1) {
            if ((comp != null) && (comp[0] != null)) {
               comp[0].setBounds(top, left, width, height);
            }
            return;
         }
 
         if (dontCalcSpans) {
            dontCalcSpans = false;
         } else {
            calcLayout(width, height);
         }

         
         //lay out the components
         out:
         for (int i=0; i<cNumRows; i++) {
            if (cNumRows == 1) {
               comph = height;
            } else if (i == cNumRows - 1) {
               comph = height - top;
            } else {
               comph = rowHeight[i];
            }
            for (int j=0; j<cNumCols; j++) {
               if ((n == comp.length) || (comp[n] == null)) {
                  break out;
               }
               if (cNumCols == 1) {
                  compw = width;
               } else if (j == cNumCols - 1) {
                  compw = width - left;
               } else {
                  compw = colWidth[j];
               }
               comp[n++].setBounds(left, top, compw, comph);
               if ((cNumCols > 1) && (j < cNumCols - 1)) {
                  left = colPos[j];
               }
            }
            left = 0;
            if ((cNumRows > 1) && (i < cNumRows - 1)) {
               top = rowPos[i];
            }
         }
      }
   }


   private void calcLayout(int width, int height) {

      if ((cNumCols == 1) && (cNumRows == 1)) {
         return;
      }
      if (cNumCols > 1) {
         colWidth = getRowOrColValues(cCols, V_VSIDES, cNumCols, width);
      }
      if (cNumRows > 1) {
         rowHeight = getRowOrColValues(cRows, V_HSIDES, cNumRows, height);
      }
   }


   private int[] getRowOrColValues(CalLength lengths[], int orientation, int numCells, int span) {
   
      int values[]   = new int[numCells];
      int sum        = 0;
      int pixCount   = 0;
      int perCount   = 0;
      int relCount   = 0;
      int numRels    = 0;
      int totPixel   = 0;
      int totPercent = 0;
      int count, type, divisor, x, y, z, n = 0;

      //deduct any spacing between frames
      if (orientation == V_VSIDES) {
         span -= totVSpacing;
      } else {
         span -= totHSpacing;
      }
      
      for (int i=0; i<numCells; i++) {
         switch(lengths[i].type) {
            case PIXEL   : n = lengths[i].value;
                           pixCount++;
                           totPixel += n;
                           break;
            case PERCENT : n = Math.max(((span * lengths[i].value) >> 8) / 100, 1);
                           perCount++;
                           totPercent += n;
                           break;
            case RELATIVE: n = 1;       //all cols have min of 1 pixel
                           relCount++;
                           numRels += lengths[i].value;
                           break;
         }
         values[i] = n;
         sum += n;
         n = 0;
      }

      if (sum < span) {   //..then we need to apportion extra space to columns
         x = span - sum;    //x = pixels to apportion
         if (relCount > 0) {
            //then we simply split the remaining space between the RELATIVE cols
            y = x / numRels;
            for (int i=0; i<numCells; i++) {
               if (lengths[i].type == RELATIVE) {
                  if (relCount == 1) {
                     values[i] = x;
                     break;
                  } else {
                     z = lengths[i].value * y;
                     values[i] = z;
                     x -= z;
                     relCount--;
                  }
               }
            }
         } else {
            //we'll give extra to PERCENT cols if there are any, otherwise to PIXEL
            if (perCount > 0) {
               type    = PERCENT;
               count   = perCount;
               divisor = Math.max(totPercent, 1);   //avoids div by 0
            } else {
               type    = PIXEL;
               count   = pixCount;
               divisor = Math.max(totPixel, 1);    //avoids div by 0
            }
            z = x;
            for (int i=0; i<numCells; i++) {
               if (lengths[i].type == type) {
                  if (count == 1) {
                     values[i] += z;
                     break;
                  } else {
                     n = (x * values[i]) / divisor;
                     values[i] += n;
                     z -= n;
                     count--;
                  }
               }
            }
         }
      } else if (sum > span) {
         //then we've got to shave pixels. RELATIVE cols will have a min value of 1 to help frame spacing
         //we shave from PERCENT cols if there are any, otherwise PIXEL. However, we test first to see
         //if the totPixelW > span. If it is then percent cols must be 1, and pixel need reducing
         if ((perCount > 0) && (totPixel < span)) {
            type    = PERCENT;
            count   = perCount;
            divisor = Math.max(totPercent, 1);   //avoid div by 0
         } else {
            type    = PIXEL;
            count   = pixCount;
            divisor = Math.max(totPixel, 1);    //avoid div by 0
            if (perCount > 0) {
               for (int i=0; i<numCells; i++) {
                  if (lengths[i].type == PERCENT) {
                     sum -= (values[i] - 1);
                     values[i] = 1;
                  }
               }
            }
         }
         x = sum - span;    //x = pixels to shave
         z = x;
         for (int i=0; i<numCells; i++) {
            if (lengths[i].type == type) {
               if (count == 1) {
                  values[i] -= z;
                  break;
               } else {
                  n = (x * values[i]) / divisor;
                  values[i] -= n;
                  z -= n;
                  count--;
               }
            }
         }
      }
      //there's a third possibility: sum = width, but this requires no action
      //finally we calculate the colPos/rowPos positions
      n = 0;
      if (orientation == V_VSIDES) {
         for (int i=0; i<colPos.length; i++) {
            n += values[i];
            if (vSpacing[i]) {
               n += spacing;
            }
            colPos[i] = n;
         }
      } else {
         for (int i=0; i<rowPos.length; i++) {
            n += values[i];
            if (hSpacing[i]) {
               n += spacing;
            }
            rowPos[i] = n;
         }
      }

      return values;
   }
   
}
