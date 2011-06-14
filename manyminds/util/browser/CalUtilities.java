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

class CalUtilities implements CalCons {

   static synchronized CalLength parseLength(String s, int level) {
   
      int n;                 //level is equivalent to HTML4.0 length types
      int value = NONE;      //level RELATIVE = %MultiLength, PERCENT = %Length
      int type  = NONE;      //level PIXEL = %Pixel
      
      if (s == null) {
         return new CalLength(NONE, NONE);
      }
      if ((level >= RELATIVE) && ((n = s.indexOf('*')) != -1) && (n > 0)) {
         s = s.substring(0, n);
         if ((n = parseDecimalNumber(s)) >= 0) {
            value = n;
            type  = RELATIVE;
         }
      } else if ((level == RELATIVE) && s.equals("*")) {
         value = 1;
         type  = RELATIVE;
      } else if ((level >= PERCENT) && ((n = s.indexOf('%')) != -1) && (n > 0)) {
         s = s.substring(0, n);
         if ((n = parseDecimalNumber(s)) >= 0) {
            value = Math.min(100, n);
            type  = PERCENT;
         }
      } else {
         if ((n = parseNumber(s)) >= 0) {
            value = n;
            type = PIXEL;
         }
      }
      
      return new CalLength(value, type);
   }
               

   static synchronized int parseDecimalNumber(String s) {
   
      int n = NONE;
      try {
         n = Double.valueOf(s).intValue();
      } catch (NumberFormatException e) {
         n = NONE;
      }
      return n;
   }


   static synchronized int parseNumber(String s) {
   
      int n = NONE;
      try {
         n = Integer.parseInt(s);
      } catch (NumberFormatException e) {
         n = NONE;
      }
      return n;
   }


   static synchronized int hashIt(String s) {
  
      int hash  = 0;
      int prime = 37;
 
      for (int i=0; i<s.length(); i++) {
         hash += hash * prime + s.charAt(i);
      }
      
      return hash;
   }

}
