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

import java.util.StringTokenizer;
import java.util.NoSuchElementException;

class CalTagFrameset implements CalCons {
   
   CalLength[] cRows;
   CalLength[] cCols;
   int         spacing;
   int         numChildren;
   int         cNumRows;
   int         cNumCols;
   int         frameBorders;


   CalTagFrameset(CalDoc doc, int attrStart, int attrEnd) {

      spacing = -1;
      frameBorders = -1;
      String s, s2;
      StringTokenizer tok  = null;
      StringTokenizer tok2 = null;
      int c = 0, r = 0, n;
      
      for (int i=attrStart; i<attrEnd; i++) {
         switch (doc.attrTypes[i]) {
            case A_FRAMESPACING : spacing = doc.attrVals[i];
                                  break;
            case A_BORDER       : if (doc.attrVals[i] > 0) {
                                     spacing = doc.attrVals[i];
                                  } else if (doc.attrVals[i] == 0) {
                                     frameBorders = 2;
                                  }
                                  break;
            case A_FRAMEBORDER  : if (("no").equalsIgnoreCase(doc.attrStrings[i])) {
                                     frameBorders = 2;
                                  } else if (("yes").equalsIgnoreCase(doc.attrStrings[i])) {
                                     frameBorders = 1;
                                  }
                                  break;
            case A_ROWS         : s = doc.attrStrings[i];
                                  if (s != null) {
                                     tok = new StringTokenizer(s, ",");
                                     r = tok.countTokens();
                                  }
                                  break;
            case A_COLS         : s2 = doc.attrStrings[i];
                                  if (s2 != null) {
                                     tok2 = new StringTokenizer(s2, ",");
                                     c = tok2.countTokens();
                                  }
                                  break;
         }
      }

      r = Math.max(r, 1);
      c = Math.max(c, 1);
      cRows = new CalLength[r];
      cCols = new CalLength[c];
      numChildren = r*c;
      if (r == 1) {
         cRows[0] = new CalLength(100 << 8, PERCENT);
      } else {
         for (int i=0; i<r; i++) {
            try {
               cRows[i] = CalUtilities.parseLength(tok.nextToken().trim(), RELATIVE);
               if (cRows[i].type == PERCENT) {
                  cRows[i].value = (cRows[i].value << 8);
               } else if (cRows[i].type == RELATIVE) {
                  cRows[i].value = Math.max(cRows[i].value, 1);   //avoids poss div by 0 error
               } else if ((cRows[i].type == NONE) || (cRows[i].value == NONE)) {
                  cRows[i].type = RELATIVE;
                  cRows[i].value = 1;
               }
            } catch (NoSuchElementException e) {
               cRows[i] = new CalLength(1, RELATIVE);
            }
         }
      }
      if (c == 1) {
         cCols[0] = new CalLength(100 << 8, PERCENT);
      } else {
         for (int i=0; i<c; i++) {
            try {
               cCols[i] = CalUtilities.parseLength(tok2.nextToken().trim(), RELATIVE);
               if (cCols[i].type == PERCENT) {
                  cCols[i].value = (cCols[i].value << 8);
               } else if (cCols[i].type == RELATIVE) {
                  cCols[i].value = Math.max(cCols[i].value, 1);   //avoids poss div by 0 error
               } else if ((cCols[i].type == NONE) || (cCols[i].value == NONE)) {
                  cCols[i].type = RELATIVE;
                  cCols[i].value = 1;
               }
            } catch (NoSuchElementException e) {
               cCols[i] = new CalLength(1, RELATIVE);
            }
         }
      }
      cNumRows = r;
      cNumCols = c;
   }
   
}
