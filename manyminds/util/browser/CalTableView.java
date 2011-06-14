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

public class CalTableView implements CalCons {

   //cellPos array values:
   //0 = xOffset
   //1 = yOffset
   //2 = width
   //3 = height
   //4 = x1
   //5 = y1
   //6 = x2
   //7 = y2
   //8 = startLine in view.lines array
   //9 = endLine in view.lines array
   //10 = linedHeight of the cell


   CalTable   table;
   int[][][]  cellPos;
   int currBorder;
   int cCaptionY;
   int cCaptionHeight;
   int cCaptionLineStart;
   int cCaptionLineEnd;
   int cStartX;
   int cStartY;
   int cHeight;
   int cTotalHeight;
   int cWidth;
   int cIndexNo;
   int lastLineIndex;
   int lastElementIndex;

   CalTableView(CalTable table) {
   
      this.table = table;
   }


   void paintTable(CalViewer viewer, CalView vw, Graphics g, int top, int height, int yOffset) {
   
      int x1, x2, y1, y2, m, n, m1, n1;
      int start = 0;
      int rows  = table.rows;
      int cols  = table.cols;
      int end   = rows;
      int c1    = cols - 1;
      int r1    = rows - 1;
      boolean found;

      g.translate(0, yOffset);
      n = top - yOffset;
      mark:
      while (start < rows) {
         for (int j=0; j<cols; j++) {
            if (cellPos[start][j][1] + cellPos[start][j][3] >= n) {
               break mark;
            }
         }
         start++;
      }
      n = top + height - yOffset;
      for (int i=start; i<rows; i++) {
         found = true;
         for (int j=0; j<cols; j++) {
            if (cellPos[i][j][1] <= n) {
               found = false;
               break;
            }
         }
         if (found) {
            end = i;
            break;
         }
      }
      
      //paint table background   
      if (table.paintBackground) {
         g.setColor(CalColor.colors[table.bgcolor]);
         g.fillRect(cStartX, cStartY, cWidth, cHeight);
      }
      
      //paint cell backgrounds
      for (int i=start; i<end; i++) {
         for (int j=0; j<cols; j++) {
            if ((table.cells[i][j] != null) && (table.cells[i][j].bgcolor != 0)) {
               g.setColor(CalColor.colors[table.cells[i][j].bgcolor]);
               g.fillRect(cellPos[i][j][0], cellPos[i][j][1], cellPos[i][j][2],
                                                                     cellPos[i][j][3]);
            }
         }
      }

      //paint cell borders
      if (table.tableRules != V_NONE) {
         for (int i=start; i<end; i++) {
            for (int j=0; j<cols; j++) {
               if (table.cells[i][j] != null) {
                  if ((table.rules[i][j] & 1) == 1) {
                     g.setColor(table.cells[i][j].shadow == 0 ?
                              CalColor.colors[table.shadow] : CalColor.colors[table.cells[i][j].shadow]);
                     g.drawLine(cellPos[i][j][4], cellPos[i][j][5], cellPos[i][j][6],
                                                                    cellPos[i][j][5]);
                  }
                  if ((table.rules[i][j] & 2) == 2) {
                     g.setColor(table.cells[i][j].shadow == 0 ?
                              CalColor.colors[table.shadow] : CalColor.colors[table.cells[i][j].shadow]);
                     g.drawLine(cellPos[i][j][4], cellPos[i][j][5], cellPos[i][j][4],
                                                                    cellPos[i][j][7]);
                  }
                  if ((table.rules[i][j] & 4) == 4) {
                     g.setColor(table.cells[i][j].highlight == 0 ?
                      CalColor.colors[table.highlight] : CalColor.colors[table.cells[i][j].highlight]);
                     g.drawLine(cellPos[i][j][6], cellPos[i][j][5], cellPos[i][j][6],
                                                                    cellPos[i][j][7]);
                  }
                  if ((table.rules[i][j] & 8) == 8) {
                     g.setColor(table.cells[i][j].highlight == 0 ?
                       CalColor.colors[table.highlight] : CalColor.colors[table.cells[i][j].highlight]);
                     g.drawLine(cellPos[i][j][4], cellPos[i][j][7], cellPos[i][j][6],
                                                                    cellPos[i][j][7]);
                  }
               }
            }
         }
      }

      //paint border
      m  = cellPos[0][0][0];
      m1 = cellPos[0][c1][0] + cellPos[0][c1][2] - 1;
      n  = cellPos[0][0][1];
      n1 = cellPos[r1][0][1] + cellPos[r1][0][3] - 1;

      //top horizontal
      if (start == 0) {
         if (table.drawTopB) { 
            g.setColor(CalColor.colors[table.highlight]);
            x1 = cStartX;
            x2 = cWidth + cStartX - 1;
            y1 = cStartY;
            for (int i=0; i<currBorder; i++) {
               if (table.tableFrame == V_BORDER) {
                  g.drawLine(x1++, y1, x2--, y1++);
               } else {
                  g.drawLine(x1, y1, x2, y1++);
               }
            }
            //if (table.tableRules == V_NONE) {
            //   g.setColor(CalColor.colors[table.shadow]);
            //   g.drawLine(m, n, m1, n);
            //}
         }
      }
      //left vertical
      if (table.drawLeftB) { 
         g.setColor(CalColor.colors[table.highlight]);
         x1 = cStartX;
         y1 = cStartY;
         y2 = cStartY + cHeight - 1;
         for (int i=0; i<currBorder; i++) {
            if (table.tableFrame == V_BORDER) {
               g.drawLine(x1, y1++, x1++, y2--);
            } else {
               g.drawLine(x1, y1, x1++, y2);
            }
         }
         //if (table.tableRules == V_NONE) {
         //   g.setColor(CalColor.colors[table.shadow]);
         //   g.drawLine(m, n, m, n1);
         //}
      }
      //right vertical
      if (table.drawRightB) { 
         g.setColor(CalColor.colors[table.shadow]);
         x1 = cWidth + cStartX - 1;
         y1 = cStartY;
         y2 = cStartY + cHeight - 1;
         for (int i=0; i<currBorder; i++) {
            if (table.tableFrame == V_BORDER) {
               g.drawLine(x1, y1++, x1--, y2--);
            } else {
               g.drawLine(x1, y1, x1--, y2);
            }
         }
         //if (table.tableRules == V_NONE) {
         //   g.setColor(CalColor.colors[table.highlight]);
         //   g.drawLine(m1, n, m1, n1);
         //}
      }
      //bottom horizontal
      if (end == rows) {
         if (table.drawBottomB) { 
            g.setColor(CalColor.colors[table.shadow]);
            x1 = cStartX;
            x2 = cWidth + cStartX - 1;
            y1 = cStartY + cHeight - 1;
            for (int i=0; i<currBorder; i++) {
               if (table.tableFrame == V_BORDER) {
                  g.drawLine(x1++, y1, x2--, y1--);
               } else {
                  g.drawLine(x1, y1, x2, y1--);
               }
            }
            //if (table.tableRules == V_NONE) {
            //   g.setColor(CalColor.colors[table.highlight]);
            //   g.drawLine(m, n1, m1, n1);
            //}
         }
      }

      g.translate(0, -yOffset);
      if (cCaptionLineEnd > cCaptionLineStart) {
         viewer.paintLineArray(g, vw, top, height, yOffset, cCaptionLineStart, cCaptionLineEnd);
      }

      for (int i=start; i<end; i++) {
         for (int j=0; j<cols; j++) {
            if (table.cells[i][j] != null) {
               viewer.paintLineArray(g, vw, top, height, yOffset, cellPos[i][j][T_START],
                                                                                    cellPos[i][j][T_END]);
            }
         }
      }
   }


   Rectangle getElementRect(CalView view, int hashNo, int offset, int type) {

      int x;
      int n = 0;
      Rectangle r;
      boolean isHash = ((type == E_NAMEH) || (type == E_LINKH)) ? true : false; 

      if ((isHash) && (cCaptionLineEnd > cCaptionLineStart)) {
         for (int i=cCaptionLineStart; i<cCaptionLineEnd; i++) {
            x = view.lines[L_X][i];
            for (int j=view.lines[L_ES][i]; j<view.lines[L_EE][i]; j++) {
               if (view.elements[type][j] == hashNo) {
                  while ((j < view.lines[L_EE][i]) && (view.elements[type][j] == hashNo)) {
                     n += view.elements[E_WIDTH][j];
                     j++;
                  }                      
                  return new Rectangle(x, offset + view.lines[L_Y][i] + view.lines[L_D][i] -
                                                           view.lines[L_H][i], n, view.lines[L_H][i]);
               }
               x += view.elements[E_WIDTH][j];
            }
         }
      }
      
      for (int i=0; i<table.rows; i++) {
         for (int j=0; j<table.cols; j++) {
            if ((table.cells[i][j] != null) && (cellPos[i][j][T_END] > cellPos[i][j][T_START])) {
               for (int k=cellPos[i][j][T_START]; k<cellPos[i][j][T_END]; k++) {
                  x = view.lines[L_X][k];
                  for (int m=view.lines[L_ES][k]; m<view.lines[L_EE][k]; m++) {
                     if (isHash && (view.elements[type][m] == hashNo)) {
                        while ((m < view.lines[L_EE][k]) && (view.elements[type][m] == hashNo)) {
                           n += view.elements[E_WIDTH][m];
                           m++;
                        }                      
                        return new Rectangle(x, offset + view.lines[L_Y][k] + view.lines[L_D][k] -
                                                               view.lines[L_H][k], n, view.lines[L_H][k]);
                     } else if ((view.elements[E_ATTR][m] & 8) > 0) {
                        n = (view.elements[E_ITEM][m] & 31);
                        if (n == I_TABLE) {
                           CalTableView tView = view.tableViews[view.elements[E_ITEM][m] >> 5];
                           r = tView.getElementRect(view, hashNo, offset + view.lines[L_Y][k] -
                                                                                 view.lines[L_H][k], type);
                           if (r != null) {
                              return r;
                           }
                        } else if (((n == I_FORM) && (type == I_FORM)) ||
                                                              ((n == I_IFRAME) && (type == I_IFRAME)))  {
                           n = (view.elements[E_ITEM][m] >> 5);
                           if (n == hashNo) {
                              return new Rectangle(x, offset + view.lines[L_Y][k] + view.lines[L_D][k] -
                                    view.lines[L_H][k], view.elements[E_WIDTH][m], view.lines[L_H][k]);                            
                           }
                        }
                     }
                     x += view.elements[E_WIDTH][m];
                  }
               }
            }
         }
      }
      return null;
   }


   boolean getElementAtPoint(int x, int y, CalViewer viewer) {

      if (cCaptionLineEnd > cCaptionLineStart) {
         if ((y >= cCaptionY) && (y < cCaptionY + cCaptionHeight)) {
            return viewer.getElementAtPoint(x, y, cCaptionLineStart, cCaptionLineEnd);
         }
      }

      //Andy: in this next section it is *correct* to use <= rather than < for the x2/y2 vals. Don't change
      for (int i=0; i<table.rows; i++) {
         for (int j=0; j<table.cols; j++) {
            if (((x >= cellPos[i][j][4]) && (x <= cellPos[i][j][6])) &&
                                             ((y >= cellPos[i][j][5]) && (y <= cellPos[i][j][7]))) {
               return (viewer.getElementAtPoint(x, y, cellPos[i][j][T_START], cellPos[i][j][T_END]));
            }
         }
      }

      return false;
   }

   
}
