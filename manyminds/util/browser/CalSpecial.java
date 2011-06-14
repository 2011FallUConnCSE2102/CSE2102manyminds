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

import java.util.Hashtable;

class CalSpecial {

   private static Hashtable specialHash;
   private static Integer value;
   private final static String special[] = 
      {"nbsp", "iexcl", "cent", "pound", "curren", "yen", "brvbar", "sect", "uml", "copy", "ordf", "laquo",
          "not", "shy", "reg", "macr", "deg", "plusmn", "sup2", "sup3", "acute", "micro", "para", "middot",
          "cedil", "sup1", "ordm", "raquo", "frac14", "frac12", "frac34 ", "iquest", "Agrave", "Aacute", "Acirc",
          "Atilde", "Auml", "Aring", "AElig", "Ccedil", "Egrave", "Eacute", "Ecirc", "Euml", "Igrave", "Iacute",
          "Icirc", "Iuml", "ETH", "Ntilde", "Ograve", "Oacute", "Ocirc", "Otilde", "Ouml", "times", "Oslash",
          "Ugrave", "Uacute", "Ucirc", "Uuml", "Yacute", "THORN", "szlig", "agrave", "aacute", "acirc", "atilde",
           "auml", "aring",  "aelig", "ccedil", "egrave", "eacute", "ecirc", "euml", "igrave", "iacute", "icirc",
           "iuml", "eth", "ntilde", "ograve", "oacute", "ocirc", "otilde", "ouml", "divide", "oslash", "ugrave",
           "uacute", "ucirc", "uuml", "yacute", "thorn", "yuml"
      };


   static {
       
      specialHash = new Hashtable();
      for (int i=0; i<special.length; i++) {
         specialHash.put(special[i], new Integer(i+160));
         specialHash.put("quot", new Integer(34));
         specialHash.put("amp", new Integer(38));
         specialHash.put("lt", new Integer(60));
         specialHash.put("gt", new Integer(62));
      }
   }
   
   
   static synchronized int getSpecialChar(String s) {
   
      if ((value = (Integer)specialHash.get(s)) != null) {
         return value.intValue();
      }
      return -1;
   }
}
