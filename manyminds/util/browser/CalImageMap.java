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

import java.net.URL;
import java.net.MalformedURLException;

class CalImageMap implements CalCons {

   CalImageMapArea area;
   CalImageMapArea nextarea;
   String name;
   String cCurrentTarget;
   URL    url;   
   
   CalImageMap(String name) {
   
      this.name = name;
      if (!this.name.startsWith("#")) {
         this.name = "#" + this.name;
      }
   }

   
   boolean setURL(CalDoc doc) {

      try {
         url = new URL(doc.url, name);
         return true;
      } catch (MalformedURLException e2) {
         System.out.println("Malformed URL for ImageMap");
         url = null;
      }
      
      return false;
   }
   
      
   void addArea(CalImageMapArea a) {
   
      if (area == null) {
         area = nextarea = a;
      } else {
         nextarea.next = a;
         nextarea = nextarea.next;
      }
   }


   String getLink(int x, int y, int w, int h) {
   
      CalImageMapArea current;
      CalImageMapArea def = null;
      String s = null;
      CalImageMapCoords coords;
      
      if (area == null) {
         return null;
      }
      cCurrentTarget = null;
      current = area;
      while (current != null) {
         if (current.shape == V_DEFAULT) {
            def = current;
         } else {
            if (current.shape == V_RECT) {
               s = getRectLink(current, x, y, w, h);
            } else if (current.shape == V_CIRCLE) {
               s = getCircleLink(current, x, y, w, h);
            } else if (current.shape == V_POLY) {
               s = getPolyLink(current, x, y, w, h);
            }
         }
         if (s != null) {
            break;
         }
         current = current.next;
      }
      if (s != null) {
         if (("?").equals(s)) {
            return null;
         } else {
            cCurrentTarget = current.targetFrame;
            return s;
         }
      } else if (def != null) {
         return def.link;
      }
      
      return null;
   }   


   private String getRectLink(CalImageMapArea current, int px, int py, int w, int h) {
   
      int x, y, x2, y2;
      CalImageMapCoords coords = current.coords;
      if (coords != null) {
         x  = (coords.x.type == PERCENT)  ? (w * coords.x.value)  / 100 : coords.x.value;
         y  = (coords.y.type == PERCENT)  ? (h * coords.y.value)  / 100 : coords.y.value;
         x2 = (coords.x2.type == PERCENT) ? (w * coords.x2.value) / 100 : coords.x2.value;
         y2 = (coords.y2.type == PERCENT) ? (h * coords.y2.value) / 100 : coords.y2.value;
         if ((px >= x) && (px <= x2) && (py >= y) && (py <= y2)) {
            if (current.link == null) {
               return "?";
            } else {
               return current.link;
            }
         }
      }
                     
      return null;   
   }

   
   private String getCircleLink(CalImageMapArea current, int px, int py, int w, int h) {
   
      int x, y;
      double rad, dist;
      CalImageMapCoords coords = current.coords;
      if (coords != null) {
         x   = (coords.x.type == PERCENT)  ? (w * coords.x.value)  / 100 : coords.x.value;
         y   = (coords.y.type == PERCENT)  ? (h * coords.y.value)  / 100 : coords.y.value;
         rad = (coords.x2.type == PERCENT) ? (double)((w * coords.x2.value) / 100) : (double)coords.x2.value;
         x = Math.abs(px - x);
         y = Math.abs(py - y);
         dist = Math.sqrt((double)((x * x) + (y * y)));
         if (dist <= rad) {
            if (current.link == null) {
               return "?";
            } else {
               return current.link;
            }
         }
      }
      return null;
   }

   
   private String getPolyLink(CalImageMapArea current, int px, int py, int w, int h) {
   
      CalImageMapCoords coords = current.coords;
      int n = 0;
      
      if (coords != null) {
         while(coords != null) {
            current.pointArrayX[n] = (coords.x.type == PERCENT)  ?
                                    (double)((w * coords.x.value)  / 100) : (double)coords.x.value;
            current.pointArrayY[n] = (coords.y.type == PERCENT)  ?
                                    (double)((h * coords.y.value)  / 100) : (double)coords.y.value;
            if (++n == current.pointCount) {
               break;
            }
            current.pointArrayX[n] = (coords.x2.type == PERCENT)  ?
                                    (double)((w * coords.x2.value)  / 100) : (double)coords.x2.value;
            current.pointArrayY[n] = (coords.y2.type == PERCENT)  ?
                                    (double)((h * coords.y2.value)  / 100) : (double)coords.y2.value;
            coords = coords.next;
            if (++n == current.pointCount) {
               break;
            }            
         }
         if (checkCrossings(current.pointCount, current.pointArrayX, current.pointArrayY,
                                                                            (double)px, (double)py)) {
            if (current.link == null) {
               return "?";
            } else {
               return current.link;
            }
         }
      }
      return null;
   }


   private boolean checkCrossings(int count, double[] xp, double[] yp, double x, double y) {
   
      boolean inside = false;
      
      for (int i=0, j=count-1; i<count; j=i++) {
         if ((((yp[i] <= y) && (y < yp[j])) || ((yp[j] <= y) && (y < yp[i]))) &&
                (x < (xp[j] - xp[i]) * (y - yp[i]) / (yp[j] - yp[i]) + xp[i])) {
            inside = !inside;
         }
      }
      
      return inside;
   }

}
