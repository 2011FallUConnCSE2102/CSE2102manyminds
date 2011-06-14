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

class CalTable implements CalCons {
   
   CalDoc doc;
   CalTableCell cells[][];
   CalTableCell currentCell;
   Vector       cellVector;
   Vector       headVector;
   Vector       footVector;
   Vector       rowVector;
   CalColGroup  colGroup;
   CalColGroup  moreColGroup;
   CalColTag    nextCol;
   int tablePrefWidthType;
   int tablePrefWidthValue;
   int tableBorder;
   int tableFrame;
   int tableRules;
   int currSpacing;
   int currPadding;
   int currRowGroupType;
   int cellSpacingType;
   int cellSpacingValue;
   int cellPaddingType;
   int cellPaddingValue;
   int[]            colWidth;
   int[]            colWidthType;
   int startPos;
   int endPos;
   int tagPos;
   int charStart;
   int charEnd;
   int captionStart;
   int captionEnd;
   int captionStartCharPos;
   int captionAlign;
   int[] captionMinW;
   int[] captionMaxW;
   int rows;
   int cols;
   int numRowGroups;
   int numColGroups;
   int[] rowGroupNo;
   int[] colGroupNo;
   int prefTotColW;
   int minPadValue;
   int[] maxPadValue;
   int[][][] minW;
   int[][][] maxW;
   int[][]   colMaxW;
   int[][]   colMinW;
   int[]     tableMinW;
   int[]     tableMaxW;
   int[]     colPrefW;
   int[]     colCurrW;
   int[]     totColCurrW;
   int[]     rowHeight;
   int[][]   cellStatus;
   int[]     prePadX;
   int[]     postPadX;
   int[]     prePadY;
   int[]     postPadY;
   int[][]   rules;
   int       nesting;
   int       numSpaces;
   int       hAlign;
   int       bgcolor;
   int       highlight;
   int       shadow;
   boolean[] sized;
   boolean[] hSpaces;
   boolean[] vSpaces;
   boolean   preSpace;
   boolean   paintBackground;
   boolean   drawTopB;
   boolean   drawBottomB;
   boolean   drawLeftB;
   boolean   drawRightB;
   
   
   public CalTable(CalDoc doc, int startPos, int tagPos,  int charStart, boolean preSpace,
                                                                           int nesting) {

      this.doc       = doc;
      this.startPos  = startPos;
      this.tagPos    = tagPos;
      this.charStart = charStart;
      this.preSpace  = preSpace;
      this.nesting   = nesting;
      endPos         = startPos;
      charEnd        = charStart;
      cellVector     = new Vector();
      rowVector      = new Vector();
      sized          = new boolean[4];
      tableMinW      = new int[3];
      tableMaxW      = new int[3];
      maxPadValue    = new int[3];
   }
   
      
   void addRow(CalTableRow r) {
   
      rowVector.addElement(r);
      currRowGroupType = r.rowGroupType;
      if ((currRowGroupType != TBODY) && (headVector == null)) {
         headVector = new Vector();
         footVector = new Vector();
      }
   }


   void addCell(CalTableCell c) {
   
      if (currRowGroupType == TBODY) {
         cellVector.addElement(c);
      } else if (currRowGroupType == THEAD) {
         headVector.addElement(c);
      } else {
         footVector.addElement(c);
      }
   }
      

   void addColGroup(int attrStart, int attrEnd) {
   
      if (colGroup == null) {
         colGroup = moreColGroup = new CalColGroup(doc, attrStart, attrEnd);
      } else {
         moreColGroup.next = new CalColGroup(doc, attrStart, attrEnd);
         moreColGroup = moreColGroup.next;
      }
   }
   
   
   void addCol(int attrStart, int attrEnd, boolean colGroupOpen) {
   
      if (colGroup == null) {   
         colGroup = moreColGroup = new CalColGroup();
         colGroup.cols = nextCol = new CalColTag(doc, attrStart, attrEnd);
      } else {
         if (colGroupOpen) {
            if (moreColGroup.cols == null) {
               moreColGroup.cols = nextCol = new CalColTag(doc, attrStart, attrEnd);
            } else {
               nextCol.next = new CalColTag(doc, attrStart, attrEnd);
               nextCol = nextCol.next;
            }
         } else {
            if (moreColGroup.isGroup) {
               moreColGroup.next = new CalColGroup();
               moreColGroup = moreColGroup.next;
               moreColGroup.cols = nextCol = new CalColTag(doc, attrStart, attrEnd);
            } else {
               nextCol.next = new CalColTag(doc, attrStart, attrEnd);
               nextCol = nextCol.next;
            }
         }
      }
   }


   void finalizeCellVector() {
   
      Vector v;
      CalTableRow r;
      
      if (headVector != null) {
         v = new Vector();
         for (int i=0, type=0; i<3; i++) {
            switch (i) {
               case 0: type = THEAD; break;
               case 1: type = TBODY; break;
               case 2: type = TFOOT; break;
            }
            for (int j=0; j<rows; j++) {
               r = (CalTableRow)rowVector.elementAt(j);
               if (r.rowGroupType == type) {
                  v.addElement(r);
               }
            }
         }
         rowVector = v;
         if (headVector.size() > 0) {
            for (int i=0; i<cellVector.size(); i++) {
               headVector.addElement(cellVector.elementAt(i));
            }
            for (int i=0; i<footVector.size(); i++) {
               headVector.addElement(footVector.elementAt(i));
            }
            cellVector = headVector;
         } else {
            for (int i=0; i<footVector.size(); i++) {
               cellVector.addElement(footVector.elementAt(i));
            }
         }
      }
   }
    

   void parseTable() {
   
      getTableAttributes();
      fillCellArray();
      fillCellAttributes();        
      checkCaption();
   }
   
   
   private void getTableAttributes() {
   
      int m = 0;
      int n = doc.tags[tagPos + 2];   //this is the index of attrCount

      boolean frameFound = false;
      boolean rulesFound = false;
      tableBorder        = 0;
      tableFrame         = V_VOID;
      tableRules         = V_NONE;
      tablePrefWidthType = NONE;
      tablePrefWidthValue= NONE;
      cellSpacingType    = PIXEL;
      cellSpacingValue   = 2;
      cellPaddingType    = PIXEL;
      cellPaddingValue   = 1;
      hAlign             = NONE;
      
      if (n == 0) {          //no attributes
         return;
      } else {
         m = doc.tags[tagPos + 3];
         n = m + n;
      }
      for (int i=m; i<n; i++) {
         switch (doc.attrTypes[i]) {
            case A_ALIGN:
                 switch(doc.attrArgs[i]) {
                    case V_LEFT: case V_RIGHT: case V_CENTER: hAlign = doc.attrArgs[i];
                 }
                 break;
            case A_WIDTH:
                 if (doc.attrArgs[i] != RELATIVE) {
                    tablePrefWidthType  = doc.attrArgs[i];
                    tablePrefWidthValue = doc.attrVals[i];
                 }
                 break;
            case A_BORDER:
                 tableBorder = doc.attrVals[i];
                 break;
            case A_FRAME:
                 frameFound = true;
                 switch (doc.attrArgs[i]) {
                    case V_BORDER: case V_ABOVE: case V_BELOW: case V_HSIDES:
                    case V_VSIDES: case V_LHS  : case V_RHS:
                       tableFrame = doc.attrArgs[i];
                       break;
                    case V_BOX: tableFrame = V_BORDER; break;
                 }
                 break;
            case A_RULES:
                 rulesFound = true;
                 switch (doc.attrArgs[i]) {
                    case V_GROUPS: case V_ROWS: case V_COLS: case V_ALL: 
                    tableRules = doc.attrArgs[i];
                 }
                 break;
            case A_CELLSPACING:
                 cellSpacingType  = (doc.attrArgs[i] == RELATIVE) ? PIXEL : doc.attrArgs[i];
                 cellSpacingValue = doc.attrVals[i]; 
                 break;
            case A_CELLPADDING:
                 cellPaddingType  = (doc.attrArgs[i] == RELATIVE) ? PIXEL : doc.attrArgs[i];
                 cellPaddingValue = doc.attrVals[i];
                 break;
            case A_BGCOLOR:
                 bgcolor = doc.attrVals[i];
                 if (bgcolor > 0) {
                    paintBackground = true;
                 }
                 break;
            case A_BORDCOLOR:
                 highlight = doc.attrVals[i];
                 shadow = highlight;
                 break;
            case A_BORDCOLORLT:
                 highlight = doc.attrVals[i];
                 break;
            case A_BORDCOLORDK:
                 shadow = doc.attrVals[i];
                 break;
         }
      }
      if ((tableRules != V_NONE) && (!frameFound)) {
         tableFrame = V_BORDER;
      }
      if (tableBorder > 0) {
         if (!frameFound) {
            tableFrame = V_BORDER;
            if (!rulesFound) {
               tableRules = V_ALL;
            }
         } else if (tableFrame == V_VOID) {
            tableBorder = 0;
         }
      } else if (tableBorder == 0) {
         if (tableFrame != V_VOID) {
            tableBorder = 1;
         }
      }

      drawTopB = ((tableFrame == V_BORDER) || (tableFrame == V_HSIDES) ||
                  (tableFrame == V_ABOVE)) ? true : false;       
      drawBottomB = ((tableFrame == V_BORDER) || (tableFrame == V_HSIDES) ||
                  (tableFrame == V_BELOW)) ? true : false;       
      drawLeftB = ((tableFrame == V_BORDER) || (tableFrame == V_VSIDES) ||
                  (tableFrame == V_LHS)) ? true : false;
      drawRightB = ((tableFrame == V_BORDER) || (tableFrame == V_VSIDES) ||
                  (tableFrame == V_RHS)) ? true : false;       
   }
      
   
   private void fillCellArray() {

      int[][] tempArray = new int[rows][20];
      CalTableCell cell;
      int currRow = 1;
      int rowIndex = ((CalTableRow)rowVector.elementAt(0)).index;
      int currCol = 0;
      cols = 0;
      int m;
      int n;
      int maxTDs = 0;
      int currTDs = 0;
            
      for (int i=0; i<cellVector.size(); i++) {
         cell = (CalTableCell)cellVector.elementAt(i);
         if (cell.rowNo != rowIndex) {
            if (currCol > cols) {
               cols  = currCol;
            }
            if (currTDs > maxTDs) {
               maxTDs = currTDs;
            }
            currTDs = 0;
            currCol = 0;
            rowIndex = ((CalTableRow)rowVector.elementAt(currRow)).index;
            currRow++;
            if (currRow > rows) {     //shouldn't ever happen
               break;
            }
         }
         currTDs++;
         while (tempArray[currRow - 1][currCol] > 0) {  //i.e. skips cols already marked
            currCol++;
            if (currCol == tempArray[currRow - 1].length) {
               int[] a = new int[currCol * 2];
               System.arraycopy(tempArray[currRow - 1], 0, a, 0, tempArray[currRow-1].length);
               tempArray[currRow - 1] = a;
            }
         }
         if (cell.rowSpan > 1) {
            cell.rowSpan = Math.min(cell.rowSpan, rows - currRow + 1);
         }
         m = currRow - 1;
         n = cell.colSpan;
         for (int j=m; j<m+cell.rowSpan; j++) {
            if (currCol + n - 1 >= tempArray[j].length) {
               int[] a = new int[(currCol + n) * 2];
               System.arraycopy(tempArray[j], 0, a, 0, tempArray[j].length);
               tempArray[j] = a;
            }
            for (int k=currCol; k<currCol+n; k++) {
               tempArray[j][k] = i+1;
            }
         }
         currCol += n;
         if (currCol >= tempArray[currRow - 1].length) {
            int[] a = new int[currCol * 2];
            System.arraycopy(tempArray[currRow - 1], 0, a, 0, tempArray[currRow-1].length);
            tempArray[currRow - 1] = a;
         }
      }
      if (currCol > cols) {
         cols = currCol;
      }

      //check cols with multispan have other cells above/below them. Shrink cols if not
      here:
      while (cols > 1) {
         for (int i=0; i<rows; i++) {
            if (tempArray[i].length >= cols) {
               if (tempArray[i][cols-1] > 0) {
                  if (tempArray[i][cols-1] != tempArray[i][cols-2]) {
                     break here;
                  }
               }
            }
         }
         cols--;
      }
      
      cells       = new CalTableCell[rows][cols];
      cellStatus  = new int[rows][cols];
      rowHeight   = new int[rows];
      minW        = new int[3][rows][cols];
      maxW        = new int[3][rows][cols];
      colMaxW     = new int[3][cols];
      colMinW     = new int[3][cols];
      colPrefW    = new int[cols];
      colCurrW    = new int[cols];
      totColCurrW = new int[cols];
      prePadX     = new int[cols];
      postPadX    = new int[cols];
      prePadY     = new int[rows];
      postPadY    = new int[rows];
      colWidth    = new int[cols + 1];
      colWidthType= new int[cols + 1];
      hSpaces     = new boolean[cols + 1];
      vSpaces     = new boolean[rows + 1];
      rules       = new int[rows][cols];
      for (int j=0; j<cols+1; j++) {
         colWidth[j]     = NONE;
         colWidthType[j] = NONE;
      }
      if (tableRules == V_GROUPS) {
         rowGroupNo = new int[rows];
         colGroupNo = new int[cols];
      }
      n = 0;
      out:
      for (int i=0; i<rows; i++) {
         for (int j=0; j<tempArray[i].length; j++) {
            if (tempArray[i][j] == n + 1) {
               cells[i][j] = (CalTableCell)cellVector.elementAt(n);
               n++;
               if (n > cellVector.size()) {
                  break out;
               }
            }
         }
      }
      cellVector = null;
   }
   

   private void fillCellAttributes() {
   
      CalTableRow row;
      CalColGroup c = colGroup;
      CalColTag   c2;
      int currCol = 0;

      //first fill in inherited valign attr, bgcolor and rowgroup numbers
      //Can't fill halign attr here because of HTML4.0 inheritance rules
      for (int i=0; i<rows; i++) {
         row = (CalTableRow)rowVector.elementAt(i);
         if (tableRules == V_GROUPS) {
            rowGroupNo[i] = row.rowGroupNo;
         }
         for (int j=0; j<cols; j++) {
            if (cells[i][j] != null) {
               if (cells[i][j].vAlign == NONE) {
                  cells[i][j].vAlign = row.vAlign;
               }
               if ((row.bgcolor != 0) && (cells[i][j].bgcolor == 0)) {
                  cells[i][j].bgcolor = row.bgcolor;
               }
               if ((row.shadow != 0) && (cells[i][j].shadow == 0)) {
                  cells[i][j].shadow = row.shadow;
               }
               if ((row.highlight != 0) && (cells[i][j].highlight == 0)) {
                  cells[i][j].highlight = row.highlight;
               }

            }
         }
      }


      //next fill in COLGROUP and COL attributes and assign colGroupNos
      numColGroups = 0;
      out:
      while (c != null) {
         if (c.isGroup) {
            numColGroups++;
         }
         if ((c.isGroup) && (c.cols == null)) {
            for (int i=0; i<c.span; i++) {
               if (tableRules == V_GROUPS) {
                  colGroupNo[currCol] = numColGroups;
               }
               for (int j=0; j<rows; j++) {
                  if (cells[j][currCol] != null) {
                     if (cells[j][currCol].hAlign == NONE) {
                        cells[j][currCol].hAlign = c.hAlign;
                     }
                     if (cells[j][currCol].vAlign == NONE) {
                        cells[j][currCol].vAlign = c.vAlign;
                     }
                     if ((c.bgcolor != 0) && (cells[j][currCol].bgcolor == 0)) {
                        cells[j][currCol].bgcolor = c.bgcolor;
                     }
                     if ((c.shadow != 0) && (cells[j][currCol].shadow == 0)) {
                        cells[j][currCol].shadow = c.shadow;
                     }
                     if ((c.highlight != 0) && (cells[j][currCol].highlight == 0)) {
                        cells[j][currCol].highlight = c.highlight;
                     }
                  }
               }
               if (c.prefWidthType != NONE) {
                  colWidth[currCol]     = c.prefWidth;
                  colWidthType[currCol] = c.prefWidthType;
                  //if (c.prefWidthType == PIXEL) {
                  //   colPrefW[currCol] = c.prefWidth;
                  //}
               }
               currCol++;
               if (currCol == cols) {
                  break out;
               }
            }
         } else {
            if (c.cols != null) {
               c2 = c.cols;
               while (c2 != null) {
                  for (int i=0; i<c2.span; i++) {
                     if ((tableRules == V_GROUPS) && (c.isGroup)) {
                        colGroupNo[currCol] = numColGroups;
                     }
                     for (int j=0; j<rows; j++) {
                        if (cells[j][currCol] != null) {
                           if (cells[j][currCol].hAlign == NONE) {
                              if (c2.hAlign != NONE) {
                                 cells[j][currCol].hAlign = c2.hAlign;
                              } else if (c.isGroup) {
                                 cells[j][currCol].hAlign = c.hAlign;
                              }
                           }
                           if (cells[j][currCol].vAlign == NONE) {
                              if (c2.vAlign != NONE) {
                                 cells[j][currCol].vAlign = c2.vAlign;
                              } else if (c.isGroup) {
                                 cells[j][currCol].vAlign = c.vAlign;
                              }
                           }
                           if (cells[j][currCol].bgcolor == 0) {
                              cells[j][currCol].bgcolor = (c2.bgcolor == 0) ? c.bgcolor : c2.bgcolor;
                           }
                           if (cells[j][currCol].shadow == 0) {
                              cells[j][currCol].shadow = (c2.shadow == 0) ? c.shadow : c2.shadow;
                           }
                           if (cells[j][currCol].highlight == 0) {
                              cells[j][currCol].highlight = (c2.highlight == 0) ? c.highlight : c2.highlight;
                           }
                        }
                     }
                     if (c2.prefWidthType != NONE) {
                        colWidth[currCol]     = c2.prefWidth;
                        colWidthType[currCol] = c2.prefWidthType;
                        //if (c2.prefWidthType == PIXEL) {
                        //   colPrefW[currCol] = c2.prefWidth;
                        //}
                     } else if (c.isGroup) {
                        if (c.prefWidthType != NONE) {
                           colWidth[currCol]     = c.prefWidth;
                           colWidthType[currCol] = c.prefWidthType;
                           //if (c.prefWidthType == PIXEL) {
                           //   colPrefW[currCol] = c.prefWidth;
                           //}
                        }
                     }
                     currCol++;
                     if (currCol == cols) {
                        break out;
                     }
                  }
                  c2 = c2.next;
               }
            }
         }
         c = c.next;
      }
      colGroup     = null;
      moreColGroup = null;
      nextCol      = null;
      
      //now fill halign from row attributes
      for (int i=0; i<rows; i++) {
         row = (CalTableRow)rowVector.elementAt(i);
         for (int j=0; j<cols; j++) {
            if (cells[i][j] != null) {
               if (cells[i][j].hAlign == NONE) {
                  cells[i][j].hAlign = row.hAlign;
               }
            }
         }
      }
      rowVector = null;
      
      //finally fill default attributes. Also take this opportunity to check for illegal colspans
      for (int i=0; i<rows; i++) {
         for (int j=0; j<cols; j++) {
            if (cells[i][j] != null) {
               if (cells[i][j].colSpan > 1) {
                  cells[i][j].colSpan = Math.min(cells[i][j].colSpan, cols - j);
               }
               if (cells[i][j].isBlank) {
                  if (cellStatus[i][j] != NONE) {
                     cellStatus[i][j] = ISBLANK;
                  }
               } else {
                  cellStatus[i][j] = ISCELL;
                  if (cells[i][j].hAlign == NONE) {
                     cells[i][j].hAlign = (cells[i][j].type == TD) ? V_LEFT : V_CENTER;
                  }
                  if (cells[i][j].vAlign == NONE) {
                     cells[i][j].vAlign = V_MIDDLE;
                  }
                  if (cells[i][j].colSpan > 1) {
                     for(int k=j+1; k<j+cells[i][j].colSpan; k++) {
                        cellStatus[i][k] = NONE;  //mark cell so border lines etc won't be drawn over it
                     }
                  }
                  if (cells[i][j].rowSpan > 1) {
                     for(int k=i+1; k<i+cells[i][j].rowSpan; k++) {
                        for (int k2=j; k2<j+cells[i][j].colSpan; k2++) {
                           cellStatus[k][k2] = NONE;  //mark cell so border lines etc won't be drawn over it
                        }
                     }
                  }
               }
            } else {
               if (cellStatus[i][j] != NONE) {
                  cellStatus[i][j] = ISNULL;
               }
            }
         }
      }
      
      //recompute numColGroups
      if ((colGroupNo != null) && (numColGroups > 0)) {
         numColGroups = 1;
         if (cols > 1) {
            for (int j=1; j<cols; j++) {
               if (colGroupNo[j-1] != colGroupNo[j]) {
                  numColGroups++;
               }
            }
         }
      }

      //work out whether we need spacing between each col/row
      if (cellSpacingValue > 0) {
         numSpaces = 2; //always spacing before first and after last col
         for (int j=1; j<cols; j++) {
            for (int i=0; i<rows; i++) {
               if (cellStatus[i][j] > 0) {
                  numSpaces++;
                  hSpaces[j] = true;
                  break;
               }
            }
         }
         for (int i=1; i<rows; i++) {
            for (int j=0; j<cols; j++) {
               if (cellStatus[i][j] > 0) {
                  vSpaces[i] = true;
                  break;
               }
            }
         }
      }

      //now determine whether lines should be drawn
      //1 = drawTop 2=drawLeft 4=drawRight 8=drawBottom
      int r1 = rows - 1;
      int c1 = cols - 1;
      for (int i=0, a, m, n; i<rows; i++) {
         for (int j=0; j<cols; j++) {
            if (cells[i][j] != null) {
               m = j + cells[i][j].colSpan - 1;
               n = i + cells[i][j].rowSpan - 1;
               a = 0;
               switch (tableRules) {
                  case V_ALL : a = 15; break;
                  case V_ROWS: if ((i > 0) || drawTopB) {
                                  a = 1;
                               }
                               if ((n < r1) || drawBottomB) {
                                  a = a | 8;
                               }
                               if ((j == 0) && drawLeftB) {
                                  a = a | 2;
                               }
                               if ((m >= c1) && drawRightB) {
                                  a = a | 4;
                               }
                               break;
                  case V_COLS: if ((j > 0) || drawLeftB) {
                                  a = 2;
                               }
                               if ((m < c1) || drawRightB) {
                                  a = a | 4;
                               }
                               if ((i == 0) && drawTopB) {
                                  a = a | 1;
                               }
                               if ((n >= r1) && drawBottomB) {
                                  a = a | 8;
                               }
                               break;
                case V_GROUPS: if (i == 0) {
                                  if (drawTopB) {
                                     a = 1;
                                  }
                               } else if (rowGroupNo[i-1] != rowGroupNo[i]) {
                                  a = 1;
                               }
                               if (n >= r1) {
                                  if (drawBottomB) {
                                     a = a | 8;
                                  }
                               } else if (rowGroupNo[n+1] != rowGroupNo[n]) {
                                  a = a | 8;
                               }
                               if (j == 0) {
                                  if (drawLeftB) {
                                     a = a | 2;
                                  }
                               } else if (colGroupNo[j-1] != colGroupNo[j]) {
                                  a = a | 2;
                               }
                               if (m >= c1) {
                                  if (drawRightB) {
                                     a = a | 4;
                                  }
                               } else if (colGroupNo[m+1] != colGroupNo[m]) {
                                  a = a | 4;
                               }
                               break;
               }               
               rules[i][j] = a;
            }
         }
      }
   }
      

   private void checkCaption() {
   
      if (captionStart == 0) {
         return;
      }
      if (captionEnd <= captionStart) {
         captionStart = 0;  //kills the caption
         return;
      } else {
         captionStart++;    //captionStart now points to first element of caption
      }
      captionMinW = new int[3];
      captionMaxW = new int[3];
   }


   synchronized void calcPreferredWidth(CalFonts f, CalView view, CalViewer viewer,
                 CalHTMLPreferences pref, int displaySize, CalStackFont bf, int docWidth,
                                                            int bgc, boolean resizeTables) {
      int maxMaxW, maxMinW, maxPrefW;
      int x, y, z, m1, m2, m3, t1, t2, t3, n, p1, p2;
      int cwMin, cwMax;

      if (pref.optimizeDisplay == NO_OPTIMIZATION) {
         displaySize = S_LARGE;
      }

      if (!sized[S_PREFW]) {
         for (int j=0; j<cols; j++) {
            if ((colWidth[j] > 0) && (colWidthType[j] == PIXEL)) {
               colPrefW[j] = colWidth[j];
            }
         }
      }

      //this bit has nothing to do with sizing. It's a hack to set the border etc colors of the table
      if (!sized[S_PREFW]) {
         if (paintBackground) {
            if (highlight == 0) {
               highlight = CalColor.getHighlight(bgcolor, bgc);
            }
         } else {
            bgcolor = bgc;
            if (highlight == 0) {
               highlight = CalColor.getHighlight(bgcolor);
            }
         }
         if (shadow == 0) {
            shadow = CalColor.getShadow(bgcolor);
         }
      }
      
      if (!sized[displaySize]) {
         CalMetrics m = new CalMetrics(f, view, viewer, pref);
         m.setState(doc, docWidth, bf, displaySize, cols, bgcolor, resizeTables);
         if (captionStart > 0) {
            m.calcMinMaxWidth(captionStart, captionEnd, viewer.rootFontStyle, bgcolor, captionStartCharPos);
            cwMin = m.cMinW;
            cwMax = m.cMaxW;
            if (cwMin == 0) {
               captionStart = 0;    //kill the caption - nothing found to render
            } else {
               captionMinW[displaySize] = cwMin;
               captionMaxW[displaySize] = cwMax;
            }
         }
         //must calc cell min/max in specific order to match the order they will be
         //lined later, or we get into trouble when adding form components
         //ANDY - it looks like this small section could be combined with the next one
         //to make it faster. Don't do it.
         for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
               if (cells[i][j] != null) {
                  m.calcMinMaxWidth(cells[i][j].start, cells[i][j].end,
                            cells[i][j].type == TH ? BOLD : viewer.rootFontStyle,
                    cells[i][j].bgcolor == 0 ? bgcolor : cells[i][j].bgcolor,
                                          cells[i][j].charStart);
                  minW[displaySize][i][j] = m.cMinW;
                  maxW[displaySize][i][j] = m.cMaxW;
               } else {
                  minW[displaySize][i][j] = 0;
                  maxW[displaySize][i][j] = 0;
               }
            }
         }
         //now we reverse the order - cols first then rows      
         for (int j=0; j<cols; j++) {
            maxMaxW    = 0;
            maxMinW    = 0;
            maxPrefW   = 0;
            for (int i=0; i<rows; i++) {
               if (cells[i][j] != null) {
                  cwMin = minW[displaySize][i][j];
                  cwMax = maxW[displaySize][i][j];
                  if ((cells[i][j].colSpan == 1) || (cells[i][j].rowSpan == rows)) {
                     if (cwMin > maxMinW) {
                        maxMinW = cwMin;
                     }
                     if (cwMax > maxMaxW) {
                        maxMaxW = cwMax;
                     }
                     if (cells[i][j].prefWidthType == PIXEL) {
                        maxPrefW = Math.max(maxPrefW, cells[i][j].prefWidth);
                     }
                     if (cells[i][j].noWrap) {
                        if (cells[i][j].prefWidthType == PIXEL) {
                           maxMinW = Math.max(maxMinW, cells[i][j].prefWidth);
                        } else {
                           maxMinW = Math.max(maxMinW, cwMax);
                        }
                     }
                  }
               }
            }
            colMinW[displaySize][j]    = maxMinW;
            colMaxW[displaySize][j]    = maxMaxW;
            colPrefW[j]                = Math.max(colPrefW[j], maxPrefW);
            if (colPrefW[j] > 0) {
               if (cellPaddingType == PIXEL) {
                  colPrefW[j] = Math.max(0, colPrefW[j] - (cellPaddingValue << 1));
               } else if (cellPaddingValue > 0) {
                  colPrefW[j] = colPrefW[j] - ((colPrefW[j] * cellPaddingValue) / 100);
               }
            }
         }
         
         //now check cells which span multiple cols to see if min/max col widths need expanding
         for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
               if (cells[i][j] != null) {
                  n = cells[i][j].colSpan;
                  if (n > 1) {
                     m1 = 0;
                     m2 = 0;
                     m3 = 0;
                     n = j + n;
                     for (int k=j; k<n; k++) {
                        m1 += colMinW[displaySize][k];
                        m2 += colMaxW[displaySize][k];
                        m3 += colPrefW[k];  
                     }
                     t1 = m1;
                     t2 = m2;
                     t3 = m3;
                     //calc padding
                     if (cellPaddingType == PIXEL) {
                        x = ((n - j - 1) * cellPaddingValue) << 1;
                        if (pref.optimizeDisplay == OPTIMIZE_ALL) {
                           m1 += (n - j - 1) * 2;   //min 2 pixels padding
                        } else {
                           m1 += x;
                        }
                        m2 += x;
                     } else {
                        m1 += (n - j - 1) * 2;   //min 2 pixels padding
                        x = 0;
                        for (int k=0; k<cols; k++) {
                           x += colMaxW[displaySize][k];
                        }
                        x = (((x * 100) / (100 - cellPaddingValue)) - x) / cols;
                        m2 += (n - j - 1) * x;
                     }
                     //calc spacing
                     if (cellSpacingType == PIXEL) {
                        x = 0;
                        for (int k=j+1; k<n; k++) {
                           x += hSpaces[k] ? cellSpacingValue : 0;
                        }
                        if (pref.optimizeDisplay != OPTIMIZE_ALL) {
                          m1 += x;
                        }
                        m2 += x;
                     }
                     if ((tableRules == V_ALL) || (tableRules == V_COLS)) {
                        x = (n - j - 1) * 2;
                        m1 += x;   //min 2 pixels cellborder
                        m2 += x;
                     } else if (tableRules == V_GROUPS) {
                        x = 0;
                        for (int k=j; k<n-1; k++) {
                           if (colGroupNo[k] != colGroupNo[k+1]) {
                              x += 2;
                           }
                        }
                     }
                     if (cells[i][j].noWrap) {
                        if (cells[i][j].prefWidthType == PIXEL) {
                           z = Math.max(cells[i][j].prefWidth, minW[displaySize][i][j]);
                        } else {
                           z = maxW[displaySize][i][j];
                        }
                     } else {
                        z = minW[displaySize][i][j];
                     }
                     if (z > m1) {
                        x = z - m1;  //x = extra space to be apportioned
                        y = x;
                        for (int k=j; k<n-1; k++) {
                           if (t1 == 0) {   //...catch div by zero problems
                              z = x / (n - j);
                           } else {
                              z = (x * (colMinW[displaySize][k] * 100 / t1)) / 100;
                           }
                           y -= z;
                           colMinW[displaySize][k] += z;
                        }
                        colMinW[displaySize][n-1] += y;
                     }
                     if (maxW[displaySize][i][j] > m2) {
                        x = maxW[displaySize][i][j] - m2;  //x = extra space to be apportioned
                        y = x;
                        for (int k=j; k<n-1; k++) {
                           if (t2 == 0) {   //...catch div by zero problems
                              z = x / (n - j);
                           } else {
                              z = (x * (colMaxW[displaySize][k] * 100 / t2)) / 100;
                           }
                           y -= z;
                           colMaxW[displaySize][k] += z;
                        }
                        colMaxW[displaySize][n-1] += y;
                     }
                     if (!sized[S_PREFW]) {
                        if (cells[i][j].prefWidthType == PIXEL) {
                           x = cells[i][j].prefWidth;
                           if (x > m3) {
                              x -= m3;   //x = extra space to be apportioned
                              y = x;
                              for (int k=j; k<n-1; k++) {
                                 if (t3 == 0) {   //...catch div by zero problems
                                    z = x / (n - j);
                                 } else {
                                    z = (x * (colPrefW[k] * 100 / t3)) / 100;
                                 }
                                 y -= z;
                                 colPrefW[k] += z;
                              }
                              colPrefW[n-1] += y;
                           }
                        }
                     }
                  }
               }       
            }
         }

         //now finalize colWidth[] arrays. Cell values may override values set in colgroups etc
         if (!sized[S_PREFW]) {
            for (int j=0; j<cols; j++) {
               if (colPrefW[j] > 0) {
                  if ((colWidthType[j] != PIXEL) || (colWidth[j] < colPrefW[j])) {
                     colWidthType[j] = PIXEL;
                     colWidth[j]     = colPrefW[j];
                  }
               } else {
                  for (int i=0; i<rows; i++) {
                     if ((cells[i][j] != null) && (cells[i][j].colSpan == 1)) {
                        if (cells[i][j].prefWidthType == PERCENT) {
                           if ((colWidthType[j] == NONE) || (colWidthType[j] == RELATIVE)) {
                              colWidthType[j] = cells[i][j].prefWidthType;
                              colWidth[j]     = cells[i][j].prefWidth;
                           } else if (cells[i][j].prefWidth > colWidth[j]) {
                              colWidthType[j] = cells[i][j].prefWidthType;
                              colWidth[j]     = cells[i][j].prefWidth;
                           }
                        }
                     }
                  }
               }
            }
            m1 = 0;         
            for (int j=0; j<cols; j++) {
               if (colWidthType[j] == RELATIVE) {
                  if (colWidth[j] == 0) {    //then user wants this col to always be min size
                     for (int k=0; k<3; k++) {
                        colMaxW[k][j] = colMinW[k][j];
                     }
                  } else {
                     m1 += colWidth[j];
                  }
               }
            }
            colWidth[cols] = m1;    //this is the total relative count
         }
         
         //now work out min and max sizes for whole table
         m1 = 0;
         m2 = 0;
         m3 = 0;
         for (int j=0; j<cols; j++) {
            m1 += colMinW[displaySize][j];
            if (colPrefW[j] > 0) {
               m2 += Math.max(colPrefW[j], colMinW[displaySize][j]);
            } else {
               m2 += colMaxW[displaySize][j];
            }
            if (!sized[S_PREFW]) {
               m3 += colPrefW[j];
            } 
         }
         if (!sized[S_PREFW]) {
            prefTotColW = m3;
         }
         
         //work out max & mins for table border, frame, rules, cellspacing and cellpadding attributes
         //first find min - only needs doing once
         if (!sized[S_PREFW]) {
            if (pref.optimizeDisplay == OPTIMIZE_ALL) { 
               x = (tableBorder == 0) ? 0 : 1;
               y = (cellPaddingValue == 0) ? 0 : 1;
               z = 0;
            } else {
               x = tableBorder;
               z = (cellSpacingType == PIXEL) ? cellSpacingValue : 0;
               if (cellPaddingType == PIXEL) {
                  y = cellPaddingValue;
               } else {
                  y = (cellPaddingValue == 0) ? 0 : 1;
               }
            }
            minPadValue = calcPadValue(x, tableFrame, PIXEL, z, PIXEL, y, 0);
         }
         //now find maxPadValue for this displaySize
         maxPadValue[displaySize] = calcPadValue(tableBorder, tableFrame, cellSpacingType,
                                   cellSpacingValue, cellPaddingType, cellPaddingValue, m2);
         maxPadValue[displaySize] = Math.max(maxPadValue[displaySize], minPadValue);
         tableMinW[displaySize] = m1 + minPadValue;
         
         if (tablePrefWidthType == PIXEL) {
            tableMaxW[displaySize] = Math.max(tablePrefWidthValue, tableMinW[displaySize]);
         } else {
            tableMaxW[displaySize] = m2 + maxPadValue[displaySize];
         }
         
         if (captionStart > 0) {
            tableMinW[displaySize] = Math.max(tableMinW[displaySize], captionMinW[displaySize]);
            tableMaxW[displaySize] = Math.max(tableMaxW[displaySize], captionMinW[displaySize]);
            //yes, it's caption*min*W in above line, not max.
         }
         
         sized[displaySize] = true;
         sized[S_PREFW]     = true;
      }
   }      
      

   //called by liner - sends total available width and table returns width desired
   //Note that value returned can be greater than availableW sent if minW of table is greater
   synchronized int getPreferredWidth(int availableW, int displaySize) {
      
      int m = 0;

      if ((tablePrefWidthType != NONE) && (tablePrefWidthType == PIXEL)) {
         for (int j=0; j<cols; j++) {
            m += Math.max(colMinW[displaySize][j], colPrefW[j]);
         }
         m += maxPadValue[displaySize];
         if ((m > tablePrefWidthValue) && (m <= availableW)) {
            return Math.max(m, tableMinW[displaySize]);
         } else {
            return Math.max(tablePrefWidthValue, tableMinW[displaySize]);
         }
      } else if (availableW <= tableMinW[displaySize]) {
         return tableMinW[displaySize];
      } else if (tablePrefWidthType != NONE) {   //then it must be PERCENT
         if (tablePrefWidthValue == 100) {
            m = availableW;
         } else {
            m = (availableW * tablePrefWidthValue) / 100;
         }
         if (prefTotColW > 0) {
            m = Math.max(m, tableMaxW[displaySize]);
         }
         if (m > 0) {
            return Math.max(Math.min(m, availableW), tableMinW[displaySize]);
         }
      }
      //System.out.println("tableMaxW = " + tableMaxW[displaySize]);
      //System.out.println("availableW = " + availableW);
      return Math.max(Math.min(tableMaxW[displaySize], availableW),
                                                           tableMinW[displaySize]);
   }
   
   
   synchronized void lineTable(CalViewer viewer, CalFonts f, CalView view,
                   CalTableView tView, CalHTMLPreferences pref, int docWidth, int width,
          CalStackFont bf, int displaySize, int xStart, boolean resizeTables, int nesting) {

      //System.out.println("Width sent = " + width);
      int m1, m, x, y, z;
      int topY;
      int xOffset;
      int yOffset;
      int maxHeight;
      int leftX = xStart;
      tView.cStartX = xStart;
      tView.cWidth  = width;
      f.checkFont(bf.family, BOLD, bf.size);
      CalStackFont bf2 = new CalStackFont(bf.family, BOLD, bf.override, bf.size, bf.color);
      x = calcCurrentPadding(tView, width, displaySize, pref.optimizeDisplay);
      calcColumnWidths(width, width - x, displaySize);
      calcTotColWidths();
      
      CalLiner liner = new CalLiner(viewer, f, doc, view, pref, nesting);

      if ((tableFrame == V_BORDER) || (tableFrame == V_VSIDES) || (tableFrame == V_LHS)) {
         leftX += tView.currBorder;
      }
      leftX += currSpacing;
      tView.cellPos = new int[rows][cols][11];      
      

      //line any caption
      if (captionStart > 0) {
         m = V_CENTER;
         if (captionAlign == V_LEFT) {
            m = V_LEFT;
         } else if (captionAlign == V_RIGHT) {
            m = V_RIGHT;
         }
         tView.cCaptionLineStart = view.linePos;
         liner.reset(width, bf, captionStart, captionEnd, m, xStart, bgcolor, resizeTables);
         view.charPos = captionStartCharPos;
         liner.lineDocument();
         tView.cCaptionLineEnd = view.linePos;
         tView.cCaptionHeight = liner.cHeightSoFar;
      }

      //line each cell
      for (int i=0; i<rows; i++) {
         xOffset = leftX;
         maxHeight = 0;
         for (int j=0, n, w, w2, h; j<cols; j++) {
            if ((cells[i][j] != null) && (!cells[i][j].isBlank)) {
               if (cells[i][j].colSpan > 1) {
                  w = 0;
                  n = cells[i][j].colSpan - 1;
                  //add all cols except last one
                  for (int k=j; k<j+n; k++) {
                     w += totColCurrW[k] + (hSpaces[k+1] ? currSpacing : 0);
                  }
                  w += totColCurrW[j+n];
                  w2 = w - prePadX[j] - postPadX[j+n];
               } else {
                  w  = totColCurrW[j];
                  w2 = colCurrW[j];
               }
               tView.cellPos[i][j][2] = w;
               liner.reset(w2, cells[i][j].type == TD ? bf : bf2, cells[i][j].start,
                   cells[i][j].end, cells[i][j].hAlign, xOffset + prePadX[j],
                       cells[i][j].bgcolor == 0 ? bgcolor : cells[i][j].bgcolor,
                                                                           resizeTables);
               tView.cellPos[i][j][T_START] = view.linePos;
               view.charPos = cells[i][j].charStart;
               liner.lineDocument();
               tView.cellPos[i][j][T_END] = view.linePos;
               tView.cellPos[i][j][T_LINEDH] = liner.cHeightSoFar;
               h = Math.max(liner.cHeightSoFar, cells[i][j].prefHeight);
               if ((h > maxHeight) && (cells[i][j].rowSpan == 1)) {
                  maxHeight = h;
               }
            } else {
               tView.cellPos[i][j][2] = totColCurrW[j];
            }
            tView.cellPos[i][j][0] = xOffset;
            xOffset += totColCurrW[j] + (hSpaces[j+1] ? currSpacing : 0);
         }
         rowHeight[i] = maxHeight + prePadY[i] + postPadY[i];
      }

      //now check cells which span multiple rows to see if row heights need expanding
      //Also add values for cellSpacing and cellPadding
      for (int i=0, h, h2; i<rows; i++) {
         for (int j=0; j<cols; j++) {
            if ((cells[i][j] != null) && (!cells[i][j].isBlank)) {
               if (cells[i][j].rowSpan > 1) {
                  h = 0;
                  m = cells[i][j].rowSpan - 1;
                  //add all rows except last one
                  for (int k=i; k<i+m; k++) {
                     h += rowHeight[k] + (vSpaces[k+1] ? currSpacing : 0);
                  }
                  //now add last and deduct outside padding
                  h += rowHeight[i+m] - prePadY[i] - postPadY[i+m];
                  h2 = Math.max(tView.cellPos[i][j][T_LINEDH], cells[i][j].prefHeight);
                  if (h2 > h) {
                     x = h2 - h;  //x = extra space to be apportioned among cols
                     y = x;
                     for (int k=i; k<i+m; k++) {
                        if (h == 0) {   //...catch div by zero problems
                           z = x / (m + 1);
                        } else {
                           z = (x * (rowHeight[k] * 100 / h)) / 100;
                        }
                        y -= z;
                        rowHeight[k] += z;
                     }
                     rowHeight[i+m] += y;
                  }
               }
            }
         }
      }
                        
      //Reset yOffsets based on the vAlign attribute.
      //Also record height and y offset of each cell in view.cellPos array
      topY = 0;
      if ((captionStart > 0) && (captionAlign != V_BOTTOM)) {
         tView.cCaptionY = 0;
         topY += tView.cCaptionHeight + 1;
      }
      tView.cStartY = topY;
      y = 0;
      if ((tableFrame == V_BORDER) || (tableFrame == V_HSIDES) || (tableFrame == V_ABOVE)) {
         topY += tView.currBorder;
      }
      topY += currSpacing;
      for (int j=0, n; j<cols; j++) {
         y = topY;
         yOffset = topY;
         n = 0;
         for (int i=0, h; i<rows; i++) {
            if ((cells[i][j] != null) && (!cells[i][j].isBlank)) {
               h = rowHeight[i];
               if (cells[i][j].rowSpan > 1) {
                  for (int k=i+1; k<i+cells[i][j].rowSpan; k++) {
                     h += rowHeight[k] + (vSpaces[k] ? currSpacing : 0);
                  }
               }
               tView.cellPos[i][j][3] = h;
               switch (cells[i][j].vAlign) {
                  case V_TOP   : yOffset += prePadY[i]; break;
                  case V_MIDDLE: yOffset += (h - tView.cellPos[i][j][T_LINEDH]) / 2; break;
                  case V_BOTTOM: yOffset +=  h - tView.cellPos[i][j][T_LINEDH] - 
                                 postPadY[i+cells[i][j].rowSpan-1]; break;
               }
               for (int k=tView.cellPos[i][j][T_START]; k<tView.cellPos[i][j][T_END]; k++) {
                  view.lines[L_Y][k] += yOffset;
                  if ((view.lines[L_EE][k] > view.lines[L_ES][k]) && (view.lines[L_JMP][k] > 0)) {
                     k = view.lines[L_JMP][k];
                  }
               }
            } else {
               tView.cellPos[i][j][3] = rowHeight[i];
            }
            tView.cellPos[i][j][1] = y;
            y += rowHeight[i] + (vSpaces[i+1] ? currSpacing : 0);
            yOffset = y;
         }
      }

      y += currSpacing;
      if ((tableFrame == V_BORDER) || (tableFrame == V_HSIDES) || (tableFrame == V_BELOW)) {
         y += tView.currBorder;
      }
      tView.cHeight = y - tView.cStartY;
      if ((captionStart <= 0) || (captionAlign != V_BOTTOM)) {
         tView.cTotalHeight = y;
      } else {
         tView.cCaptionY = y + 1;
         tView.cTotalHeight = y + tView.cCaptionHeight + 1;
      }

      calcRules(tView);
  
      liner.start();
      liner = null;
   }
   
   
   private int calcCurrentPadding(CalTableView tView, int width, int displaySize, int optimize) {
   
      if ((width <= tableMinW[displaySize]) || (optimize != OPTIMIZE_ALL)) {
         if (optimize == OPTIMIZE_ALL) {
            tView.currBorder = (tableBorder > 0) ? 1 : 0;
            currSpacing = 0;
            currPadding = (cellPaddingValue == 0) ? 0 : 1;
         } else {
            tView.currBorder = tableBorder;
            currSpacing = (cellSpacingType == PIXEL) ? cellSpacingValue : 0;
            if (cellPaddingType == PIXEL) {
               currPadding = cellPaddingValue;
            } else {
               currPadding = (cellPaddingValue == 0) ? 0 : 1;
            }
         }
         return minPadValue;
      }

      int modW = width;
      int w = 0;
      int m;
      int n = 0;
      int x = 0;
      int y = 0;
      int z = 0;
      int minSpacing, minPadding, minBorder;
      boolean vborder = false;
      boolean a, b, c;
                  
      tView.currBorder = tableBorder;
      if ((tableFrame == V_BORDER) || (tableFrame == V_VSIDES)) {
         vborder = true;
         minBorder = (optimize == OPTIMIZE_ALL) ? 1 : tableBorder;
         modW -= tableBorder << 2;
      } else if ((tableFrame == V_LHS) || (tableFrame == V_RHS)) {
         vborder = true;
         minBorder = (optimize == OPTIMIZE_ALL) ? 1 : tableBorder;
         modW -= tableBorder;
      } else {
         minBorder = 0;
      }
      
      if (cellSpacingType == PERCENT) {
         currSpacing = ((modW * cellSpacingValue) / 100) / Math.max(numSpaces, 1);
         minSpacing = 0;
      } else {
         currSpacing = cellSpacingValue;
         minSpacing = (optimize == OPTIMIZE_ALL) ? 0 : cellSpacingValue;
      }
      
      if (cellPaddingType == PERCENT) {
         minPadding = 1;
         n = currSpacing * (cols + 1);
         currPadding = Math.max(((((modW - n) * cellPaddingValue) / 100) / cols) >> 1, 1);
      } else {
         currPadding = cellPaddingValue;
         if (optimize == OPTIMIZE_ALL) {
            minPadding = (currPadding == 0) ? 0 : 1;
         } else {
            minPadding = cellPaddingValue;
         }
      }

      z = tableMinW[displaySize] - minPadValue;   //z = minW of elements
      n = width - z;                              //n = total space available for padding

      if (width >= tableMaxW[displaySize]) {         
         return calcPadValue(tView.currBorder, tableFrame, PIXEL, currSpacing, PIXEL,
                                                                            currPadding, z);
      }
            
      m = -1;
      a = b = c = false;
      while (true) {
         w = calcPadValue(tView.currBorder, tableFrame, PIXEL, currSpacing, PIXEL,
                                                                            currPadding, z);
         if ((w <= n) || (a && b && c)) {
            break;
         } else {
            while (true) {
               if (++m == 3) {
                  m = 0;
               }
               if (((m == 0) && (!a)) || ((m == 1) && (!b)) || ((m == 2) && (!c))) {
                  break;
               }
            }   
            currSpacing = Math.max(minSpacing, currSpacing - 1);
            if (currSpacing == minSpacing) {
               a = true;
            }
            if (vborder) {
               tView.currBorder = Math.max(minBorder, tView.currBorder - 1);
               if (tView.currBorder == minBorder) {
                  b = true;
               }
            }
            currPadding = Math.max(minPadding, currPadding - 1);
            if (currPadding == minPadding) {
               c = true;
            }
         }
      }
 
      return w;
   }


   private int calcPadValue(int border, int frame, int spaceType, int spacing, int padType,
                                                                       int padding, int elementsWidth) { 
      int x;
      int w  = 0;
      int p1 = cols * 2;        //utility val to speed up computation
      int p2 = (cols - 1) * 2;  //ditto
         
      if (padType == PERCENT) {
         if (padding > 0 && padding < 100) {
            w += Math.max(((elementsWidth * 100) / (100 - padding)) - elementsWidth, p1);
         }
      } else {
         if (padding > 0) {
            w += p1 * padding;
         }
      }
      if (spaceType == PERCENT) {
         if (spacing > 0 && spacing < 100) {
            w += (((elementsWidth + w) * 100) / (100 - spacing)) - (elementsWidth + w);
         }  
      } else {
         if (spacing > 0) {
            w += numSpaces * spacing;
         }
      }
      
      x = (tableRules == V_ALL) ? 0 : 1;
      if ((frame == V_BORDER) || (frame == V_VSIDES)) {
         w += (border * 2) + (x << 1);   //+2 is for cell borders
      } else if ((frame == V_LHS) || (frame == V_RHS)) {
         w += border + x;
      }

      if (tableRules == V_ALL) {
         w += p1;
      } else if (tableRules == V_COLS) {
         w += p1 - 2;   // -2 is for edge pixels
      } else if (tableRules == V_GROUPS) {
         w += (numColGroups * 2) - 2;
      }
      
      return w;
   }


   private void calcColumnWidths(int totWidth, int width, int displaySize) {

      //a boolean fullSize is determined. We need to compute this
      //so we can decide what preferred values to put in the colCurrW array below. If the
      //width sent to the table is greater than the table max width then we can freely use
      //the maxW values for each col. However it's possible that a column's max width can
      //be *less* than the minW because cellspacing which is available for cols with
      //colSpans > 0 disappears as the table shrinks. So if the available width is less
      //than the max elements width, we must set the colCurrW to the Math.max(MinW, MaxW) for
      //that column
      boolean fullSize = (totWidth >= tableMaxW[displaySize]);
   
      //if ((width <= tableMinW[displaySize] - minPadValue) && (!fullSize)) {
      //   for (int j=0; j<cols; j++) {
      //      colCurrW[j] = colMinW[displaySize][j];
      //   }
      //   return;
      //}
      //System.out.println("TotWidth= " + totWidth + ", width = " + width);

      boolean[] done     = new boolean[cols];
      int[]     values   = new int[cols];
      int[]     min      = new int[cols];
      boolean   allDone;
      int sum = 0;
      int n   = 0;
      int m   = 0;
      int x, y, z, k;

      //set min values for each column
      for (int j=0; j<cols; j++) {
         min[j] = colMinW[displaySize][j];
      }
      
      //fill the values array with each column's requested width
      sum = 0;
      m   = 0;
      n   = 0;

      allDone = true;
      for (int j=0; j<cols; j++) {
         switch(colWidthType[j]) {
            case PIXEL   : n = Math.max(min[j], colWidth[j]);
                           done[j] = true;
                           break;
            case PERCENT : n = Math.max(min[j], (width * colWidth[j]) / 100);
                           done[j] = true;
                           break;
            case NONE    : if (colWidth[cols] > 0) {    //i.e. some relative values
                              n = min[j];
                              done[j] = true;
                           } else {
                              if (!fullSize) {
                                 n = Math.max(colMaxW[displaySize][j], colMinW[displaySize][j]);
                              } else {
                                 n = colMaxW[displaySize][j];
                              }
                              done[j] = false;
                              allDone = false;
                           }
                           break;
            case RELATIVE: n = min[j];
                           if (colWidth[j] == 0) {
                              done[j] = true;
                           } else {
                              m += n;
                              done[j] = false;
                              allDone = false;
                           }
                           break;
                  default: n = 0;
                           done[j] = true;
                           break;
         }
         colCurrW[j] = n;
         values[j] = n;
         sum += n;
      }

      //System.out.println("Sum = " + sum);
      if (sum == width) {
         return;
      }

      if (sum < width) {   //..then we need to apportion extra space to columns
         if (allDone) {
            //then we didn't find any NONE or RELATIVE cols to give the extra space to
            //so we'll give it to PIXEL or PERCENT cols
            for (int j=0; j<cols; j++) {
               if ((colWidthType[j] == PIXEL) || (colWidthType[j] == PERCENT)) {
                  done[j] = false;
                  allDone = false;
               }
            }
         }
         if (allDone) {
            //then there's no columns to take the extra space so break
            return;
         }
         
         if (colWidth[cols] > 0) {
            //then extra space goes to RELATIVE cols. This is calc'd differently than other ones
            x = width - sum + m;           //x = pixels to apportion
            m = colWidth[cols];  //m = no. of RELATIVE portions
            //first cycle through cols until they can all take the full apportionment
            allDone = false;
            while ((!allDone) && (m > 0)) {   //m test avoids div by zero error
               allDone = true;
               n = 0;
               y = Math.max(x / m, 1);   //y = pixels per portion
               for (int j=0; j<cols; j++) {
                  if (!done[j]) {
                     if ((colWidth[j] * y) <= min[j]) {
                        done[j] = true;
                        x -= min[j];
                        m -= colWidth[j];
                        allDone = false;
                     } else {
                        n++;
                     }
                  }
               }
            }
            //now apportion what's left to remaining cols
            if (m <= 0) {
               return;   //shouldn't happen
            }
            y = x / m;
            for (int j=0; j<cols; j++) {
               if (!done[j]) {
                  z = Math.max(colWidth[j] * y, min[j]);
                  colCurrW[j] = z;
                  x -= z;
               }
            }
            //finally apportion any residue a pixel at a time to cols
            while (x > 0) {
               for (int j=0; j<cols; j++) {
                  if (!done[j]) {
                     colCurrW[j]++;
                     x--;
                     if (x <= 0) {
                        break;
                     }
                  }
               }
            }
            return;
         } else {
            //find divisor for apportionment
            m = 0;
            n = 0;
            for (int j=0; j<cols; j++) {
               if (!done[j]) {
                  m += values[j];
                  n++;
               }
            }
            x = width - sum;           //x = pixels to apportion
            z = x;
            if (m <= 0) {       //...looks like someone's using tables for spacing purposes
               if (n > 0) {
                  y = x / n;
                  for (int j=0; j<cols; j++) {
                     if (!done[j]) {
                        if (n == 1) {
                           colCurrW[j] += z;
                           break;
                        } else {
                           colCurrW[j] += y;
                           z -= y;
                           n--;
                        }
                     }
                  }
               }
               return;   //otherwise we'd get a div by zero error or other probs
            }
            for (int j=0; j<cols; j++) {
               if (!done[j]) {
                  if (n == 1) {
                     colCurrW[j] += z;
                     break;
                  } else {
                     y = (x * values[j]) / m;
                     colCurrW[j] += y;
                     z -= y;
                     n--;
                  }
               }
            }
         }
         return;
      }
      
      //if we reach here then sum > width and columns need adjusting to less than their desired max
      if (!allDone) {
         if (colWidth[cols] == 0) {
            //then we must have found some NONE values. See if we can adjust these cols only
            //In any case, the currColW for these cols needs resetting to the minimum
            //However we don't apportion extra space to those cols whose MinW = MaxW, otherwise
            //we'd be giving them space unnecessarily
            m = 0;
            n = 0;
            sum = 0;
            for (int j=0; j<cols; j++) {
               if (!done[j]) {
                  colCurrW[j] = min[j];
                  if (colMaxW[displaySize][j] == min[j]) {
                     done[j] = true;
                  } else {
                     n++;
                     m += values[j];
                  }
               }
               sum += colCurrW[j];
            }
            if (sum > width) {
               //then we need to adjust more than just these cols. Set allDone to true so PERCENT and
               //PIXEL cols are adjusted too
               allDone = true;
            } else {
               x = width - sum;
               z = x;
               if ((x <= 0) || (m <= 0)) {
                  return;    //shouldn't happen
               }
               for (int j=0; j<cols; j++) {
                  if (!done[j]) {
                     if (n == 1) {
                        colCurrW[j] += z;
                        break;
                     } else {
                        y = (x * values[j]) / m;
                        colCurrW[j] += y;
                        z -= y;
                        n--;
                     }
                  }
               }
               return;
            }
         } else {
            allDone = true;  //RELATIVE cols are already at minimum
         }
      }
      if (allDone) {
         //we must reset the done[] array so that PERCENT and PIXEL cols are shaved and set currColW
         //for these cols to the minimum. Also mark any !done cols as done
         m = 0;
         n = 0;
         sum = 0;
         for (int j=0; j<cols; j++) {
            if (!done[j]) {
               done[j] = true;
            } else {
               if ((colWidthType[j] == PIXEL) || (colWidthType[j] == PERCENT)) {
                  n++;
                  done[j] = false;
                  allDone = false;
                  m += values[j];
               }
            }
            sum += colCurrW[j];
         }
      }
      
      if (allDone) {
         return;      //shouldn't ever happen
      }
      x = sum - width;
      z = x;
      k = m;
      if ((x <= 0) || (m <= 0)) {
         return;    //shouldn't happen
      }
      // !done cols will now be reduced proportionately, but not below their min size
      while (true) {
         for (int j=0; j<cols; j++) {
            if (!done[j]) {
               if (n == 1) {
                  colCurrW[j] -= z;
                  colCurrW[j] = Math.max(colCurrW[j], min[j]);
                  return;
               } else {
                  y = Math.min(z, Math.max((x * values[j]) / m, 1));
                  if (colCurrW[j] - y <= min[j]) {
                     y = colCurrW[j] - min[j];
                     colCurrW[j] = min[j];
                     done[j] = true;
                     k -= values[j];
                     n--;
                  } else {
                     colCurrW[j] -= y;
                  }
                  z -= y;
               }
            }
         }
         x = z;
         m = k;
         if ((x <= 0) || (m <= 0)) {
            return;
         }
      }
   }


   //this method also computes the prePadX,postPadX, prePadY and postPadY values
   private void calcTotColWidths() {

      for (int i=0; i<rows; i++) {
         prePadY[i]  = currPadding;
         postPadY[i] = currPadding;
         if (tableRules == V_ALL) {
            prePadY[i]++;
            postPadY[i]++;
         } else if (tableRules == V_ROWS) {
            if (i == 0) {
               if (rows > 1) {
                  postPadY[i]++;
               }
            } else if (i == rows - 1) {
               if (rows > 1) {
                  prePadY[i]++;
               }
            } else {
               prePadY[i]++;
               postPadY[i]++;
            }
         }
      }

      for (int j=0; j<cols; j++) {
         prePadX[j]  = currPadding;
         postPadX[j] = currPadding;
         if (tableRules == V_ALL) {
            prePadX[j]++;
            postPadX[j]++;
         } else if (tableRules == V_COLS) {
            if (j == 0) {
               if (cols > 1) {
                  postPadX[j]++;
               }
            } else if (j == cols - 1) {
               if (cols > 1) {
                  prePadX[j]++;
               }
            } else {
               prePadX[j]++;
               postPadX[j]++;
            }
         }
      }

      if (tableRules != V_ALL) {
         if ((tableFrame == V_BORDER) || (tableFrame == V_VSIDES)) {
            prePadX[0]++;
            postPadX[cols-1]++;
         } else if (tableFrame == V_LHS) {
            prePadX[0]++;
         } else if (tableFrame == V_RHS) {
            postPadX[cols-1]++;
         }
         if ((tableFrame == V_BORDER) || (tableFrame == V_HSIDES)) {
            prePadY[0]++;
            postPadY[rows-1]++;
         } else if (tableFrame == V_ABOVE) {
            prePadY[0]++;
         } else if (tableFrame == V_BELOW) {
            postPadY[rows-1]++;
         }
      }
      
      if (tableRules == V_GROUPS) {
         if ((cols > 1) && (colGroupNo[1] != colGroupNo[0])) {
            postPadX[0]++;
         }
         for (int j=1; j<cols-1; j++) {
            if (colGroupNo[j-1] != colGroupNo[j]) {
               prePadX[j]++;
            }
            if (colGroupNo[j+1] != colGroupNo[j]) {
               postPadX[j]++;
            }
         }
         if ((cols > 1) && (colGroupNo[cols-1] != colGroupNo[cols-2])) {
            prePadX[cols-1]++;
         }

         if ((rows > 1) && (rowGroupNo[1] != rowGroupNo[0])) {
            postPadY[0]++;
         }
         for (int i=1; i<rows-1; i++) {
            if (rowGroupNo[i-1] != rowGroupNo[i]) {
               prePadY[i]++;
            }
            if (rowGroupNo[i+1] != rowGroupNo[i]) {
               postPadY[i]++;
            }
         }
         if ((rows > 1) && (rowGroupNo[rows-1] != rowGroupNo[rows-2])) {
            prePadY[rows-1]++;
         }
      }

      for (int j=0; j<cols; j++) {
         totColCurrW[j] = colCurrW[j] + prePadX[j] + postPadX[j];
      }
   }
   

   private void calcRules(CalTableView tView) {

      int x1, x2, y1, y2, m, n, a;
      int c1 = cols - 1;
      int r1 = rows - 1;

      for (int i=0; i<rows; i++) {
         for (int j=0; j<cols; j++) {
            if (cells[i][j] != null) {
               m = j + cells[i][j].colSpan - 1;
               n = i + cells[i][j].rowSpan - 1;
               //calc x and y co-ords
               x1 = ((j==0) && (!drawLeftB) && (tableRules != V_ALL)) ?
                                                      tView.cStartX : tView.cellPos[i][j][0];
               x2 = tView.cellPos[i][j][0] + tView.cellPos[i][j][2] - 1;
               if (m >= c1) {
                  if ((!drawRightB) && (tableRules != V_ALL)) {
                     x2 += currSpacing;
                  }
               } else if ((tableRules == V_ROWS) || ((tableRules == V_GROUPS) &&
                                  (colGroupNo[m] == colGroupNo[m+1]))) {
                  x2 += currSpacing;
               }
              
               y1 = ((i==0) && (!drawTopB) && (tableRules != V_ALL)) ?
                                                   tView.cStartY : tView.cellPos[i][j][1];
               y2 = tView.cellPos[i][j][1] + tView.cellPos[i][j][3] - 1;
               if (n >= r1) {
                  if ((!drawBottomB) && (tableRules != V_ALL)) {
                     y2 += currSpacing;
                  }
               } else if ((tableRules == V_COLS) || ((tableRules == V_GROUPS) &&
                                  (rowGroupNo[n] == rowGroupNo[n+1]))) {
                  y2 += currSpacing;
               }
               tView.cellPos[i][j][4] = x1;
               tView.cellPos[i][j][6] = x2;
               tView.cellPos[i][j][5] = y1;
               tView.cellPos[i][j][7] = y2;
            }
         }
      }      
   }
                     
}
