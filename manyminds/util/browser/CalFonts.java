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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.util.Hashtable;
import java.util.StringTokenizer;

class CalFonts implements CalCons {

   static String[]  fontNames = {"Serif", "SansSerif", "Monospaced", "Dialog"};
   static int[]     fontSizes = {10, 11, 12, 13, 14, 16, 18, 20, 22, 24, 28, 36, 48};
   Hashtable fontHash;
   
   Font[][][]        fonts;
   FontMetrics[][][] fm;
   int[][][]         spaceW;
   int               ALTHEIGHT;
   int               ALTBASE;

   
   public CalFonts() {

      fonts    = new Font[fontNames.length][4][fontSizes.length];
      fm       = new FontMetrics[fontNames.length][4][fontSizes.length];
      spaceW   = new int[fontNames.length][4][fontSizes.length];
      fontHash = new Hashtable();
      for (int i=0; i<fontNames.length; i++) {
         fontHash.put(fontNames[i].toLowerCase(), new Integer(i));
      }
      fontHash.put("times new roman", new Integer(0));
      fontHash.put("timesroman"     , new Integer(0));
      fontHash.put("times"          , new Integer(0));
      fontHash.put("helvetica"      , new Integer(1));
      fontHash.put("arial"          , new Integer(1));
      fontHash.put("courier"        , new Integer(2));
      fontHash.put("courier new"    , new Integer(2));
      fontHash.put("dialoginput"    , new Integer(2));
      checkFont(HELV, NORM, 1);  //need to initialise this font for IMG alt text
      ALTHEIGHT = fm[HELV][NORM][1].getHeight();
      ALTBASE   = ALTHEIGHT - fm[HELV][NORM][1].getMaxDescent();
   }


   void checkFont(int family, int style, int size) {
   
      if (fonts[family][style][size] == null) {
         fonts[family][style][size] = new Font(fontNames[family], style, fontSizes[size]);
         fm[family][style][size] = Toolkit.getDefaultToolkit().getFontMetrics(fonts[family][style][size]);
         spaceW[family][style][size] = fm[family][style][size].stringWidth(" ");
      }
   }
   
   
   int getFontFace(String s) {
   
      Integer value;
      String s2;
      
      s = s.toLowerCase();
      if ((value = (Integer)fontHash.get(s)) != null) {
         return value.intValue();
      } else {
         StringTokenizer tok = new StringTokenizer(s, ",");
         while (tok.hasMoreTokens()) {
            s2 = tok.nextToken();
            s2 = s2.trim();
            if ((value = (Integer)fontHash.get(s2)) != null) {
               fontHash.put(s, value);
               return value.intValue();
            }
         }
      }
      fontHash.put(s, new Integer(NONE));      //this is deliberately disabled until Java 1.2
      return NONE;
   }

}
