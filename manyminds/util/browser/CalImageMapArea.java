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

class CalImageMapArea implements CalCons {

   CalImageMapArea   next;
   CalImageMapCoords coords;
   CalImageMapCoords nextcoords;
   String link;
   String targetFrame;
   int shape;
   int pointCount;
   double[] pointArrayX;
   double[] pointArrayY;
   
   CalImageMapArea(CalDoc doc, int shapeId, int coordsId, int nohrefId, int hrefId, int targetId) {
   
      shape = NONE;
      next  = null;
      
      if (shapeId != -1) {
         if (doc.attrArgs[shapeId] == V_DEFAULT) {
            shape = V_DEFAULT;
         } else if (coordsId != -1) {
            switch (doc.attrArgs[shapeId]) {
               case V_RECT    : shape = V_RECT;   parseRectCoords(doc.attrStrings[coordsId])  ; break;
               case V_CIRCLE  : shape = V_CIRCLE; parseCircleCoords(doc.attrStrings[coordsId]); break;
               case V_POLY    : shape = V_POLY;   parsePolyCoords(doc.attrStrings[coordsId])  ; break;
            }
         }
      }
      if (shape != NONE) {
         if (nohrefId != -1) {
            link = null;
         } else {
            link = (hrefId == -1) ? null : doc.attrStrings[hrefId];
            targetFrame = (targetId == -1) ? null : doc.attrStrings[targetId];
         }
      }
   }
   
   
   private void parseRectCoords(String s) {
   
      CalLength x, y, x2, y2;
      StringTokenizer tok = new StringTokenizer(s, ",");
      
      try {
         x  = CalUtilities.parseLength(tok.nextToken(), PERCENT);
         y  = CalUtilities.parseLength(tok.nextToken(), PERCENT);
         x2 = CalUtilities.parseLength(tok.nextToken(), PERCENT);
         y2 = CalUtilities.parseLength(tok.nextToken(), PERCENT);
         coords = new CalImageMapCoords(x, y, x2, y2);
      } catch (NoSuchElementException e) {
         shape = NONE;
      } catch (NumberFormatException e2) {
         shape = NONE;
      }
   }
   

   private void parseCircleCoords(String s) {
   
      CalLength x, y, rad;
      StringTokenizer tok = new StringTokenizer(s, ",");
      
      try {
         x   = CalUtilities.parseLength(tok.nextToken(), PERCENT);
         y   = CalUtilities.parseLength(tok.nextToken(), PERCENT);
         rad = CalUtilities.parseLength(tok.nextToken(), PERCENT);
         coords = new CalImageMapCoords(x, y, rad, new CalLength(NONE, NONE));
      } catch (NoSuchElementException e) {
         shape = NONE;
      } catch (NumberFormatException e2) {
         shape = NONE;
      }
   }
   

   private void parsePolyCoords(String s) {
   
      CalLength x, y, x2, y2;
      StringTokenizer tok = new StringTokenizer(s, ",");
      pointCount = 0;
      
      while (tok.hasMoreTokens() && (shape != NONE)) {
         x = x2 = y = y2 = null;
         try {
            x = CalUtilities.parseLength(tok.nextToken(), PERCENT);
            y = CalUtilities.parseLength(tok.nextToken(), PERCENT);
            pointCount++;
         } catch (NoSuchElementException e) {
            shape = NONE;            
         } catch (NumberFormatException e2) {
            shape = NONE;
         }
         if (tok.hasMoreTokens()) {
            try {
               x2 = CalUtilities.parseLength(tok.nextToken(), PERCENT);
               y2 = CalUtilities.parseLength(tok.nextToken(), PERCENT);
               pointCount++;
            } catch (NoSuchElementException e) {
               shape = NONE;
            } catch (NumberFormatException e2) {
               shape = NONE;
            }
         } else {
            x2 = new CalLength(NONE, NONE);
            y2 = new CalLength(NONE, NONE);
         }
         if (shape != NONE) {
            if (coords == null) {
               coords = nextcoords = new CalImageMapCoords(x, y, x2, y2);
            } else {
               nextcoords.next = new CalImageMapCoords(x, y, x2, y2);
               nextcoords = nextcoords.next;
            }
         }
      }
      if (shape != NONE) {
         if (pointCount > 2) {
            pointArrayX = new double[pointCount];
            pointArrayY = new double[pointCount];
         } else {
            shape = NONE;
         }
      } 
   }
       
}
