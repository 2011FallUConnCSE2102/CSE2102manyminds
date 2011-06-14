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
import java.util.Hashtable;
import javax.swing.UIManager;

class CalColor {

   static char[][] presets = {
      {'a','q','u','a'},
      {'a','z','u','r','e'},
      {'a','l','i','c','e','b','l','u','e'},
      {'a','q','u','a','m','a','r','i','n','e'},
      {'a','n','t','i','q','u','e','w','h','i','t','e'},
      {'b','l','u','e'},
      {'b','e','i','g','e'},
      {'b','l','a','c','k'},
      {'b','r','o','w','n'},
      {'b','i','s','q','u','e'},
      {'b','u','r','l','y','w','o','o','d'},
      {'b','l','u','e','v','i','o','l','e','t'},
      {'b','l','a','n','c','h','e','d','a','l','m','o','n','d'},
      {'c','y','a','n'},
      {'c','o','r','a','l'},
      {'c','r','i','m','s','o','n'},
      {'c','o','r','n','s','i','l','k'},
      {'c','a','d','e','t','b','l','u','e'},
      {'c','h','o','c','o','l','a','t','e'},
      {'c','h','a','r','t','r','e','u','s','e'},
      {'c','o','r','n','f','l','o','w','e','r','b','l','u','e'},
      {'d','a','r','k','r','e','d'},
      {'d','i','m','g','r','a','y'},
      {'d','a','r','k','b','l','u','e'},
      {'d','a','r','k','c','y','a','n'},
      {'d','a','r','k','g','r','a','y'},
      {'d','e','e','p','p','i','n','k'},
      {'d','a','r','k','g','r','e','e','n'},
      {'d','a','r','k','k','h','a','k','i'},
      {'d','a','r','k','o','r','a','n','g','e'},
      {'d','a','r','k','o','r','c','h','i','d'},
      {'d','a','r','k','s','a','l','m','o','n'},
      {'d','a','r','k','v','i','o','l','e','t'},
      {'d','o','d','g','e','r','b','l','u','e'},
      {'d','a','r','k','m','a','g','e','n','t','a'},
      {'d','e','e','p','s','k','y','b','l','u','e'},
      {'d','a','r','k','s','e','a','g','r','e','e','n'},
      {'d','a','r','k','g','o','l','d','e','n','r','o','d'},
      {'d','a','r','k','s','l','a','t','e','b','l','u','e'},
      {'d','a','r','k','s','l','a','t','e','g','r','a','y'},
      {'d','a','r','k','t','u','r','q','u','o','i','s','e'},
      {'d','a','r','k','o','l','i','v','e','g','r','e','e','n'},
      {'f','u','c','h','s','i','a'},
      {'f','i','r','e','b','r','i','c','k'},
      {'f','l','o','r','a','l','w','h','i','t','e'},
      {'f','o','r','e','s','t','g','r','e','e','n'},
      {'g','o','l','d'},
      {'g','r','a','y'},
      {'g','r','e','e','n'},
      {'g','a','i','n','s','b','o','r','o'},
      {'g','o','l','d','e','n','r','o','d'},
      {'g','h','o','s','t','w','h','i','t','e'},
      {'g','r','e','e','n','y','e','l','l','o','w'},
      {'h','o','t','p','i','n','k'},
      {'h','o','n','e','y','d','e','w'},
      {'i','v','o','r','y'},
      {'i','n','d','i','g','o'},
      {'i','n','d','i','a','n','r','e','d'},
      {'k','h','a','k','i'},
      {'l','i','m','e'},
      {'l','i','n','e','n'},
      {'l','a','v','e','n','d','e','r'},
      {'l','a','w','n','g','r','e','e','n'},
      {'l','i','g','h','t','b','l','u','e'},
      {'l','i','g','h','t','c','y','a','n'},
      {'l','i','g','h','t','g','r','e','y'},
      {'l','i','g','h','t','p','i','n','k'},
      {'l','i','m','e','g','r','e','e','n'},
      {'l','i','g','h','t','c','o','r','a','l'},
      {'l','i','g','h','t','g','r','e','e','n'},
      {'l','i','g','h','t','s','a','l','m','o','n'},
      {'l','i','g','h','t','y','e','l','l','o','w'},
      {'l','e','m','o','n','c','h','i','f','f','o','n'},
      {'l','i','g','h','t','s','k','y','b','l','u','e'},
      {'l','a','v','e','n','d','e','r','b','l','u','s','h'},
      {'l','i','g','h','t','s','e','a','g','r','e','e','n'},
      {'l','i','g','h','t','s','l','a','t','e','g','r','a','y'},
      {'l','i','g','h','t','s','t','e','e','l','b','l','u','e'},
      {'l','i','g','h','t','g','o','l','d','e','n','r','o','d','y','e','l','l','o','w'},
      {'m','a','r','o','o','n'},
      {'m','a','g','e','n','t','a'},
      {'m','o','c','c','a','s','i','n'},
      {'m','i','n','t','c','r','e','a','m'},
      {'m','i','s','t','y','r','o','s','e'},
      {'m','e','d','i','u','m','b','l','u','e'},
      {'m','e','d','i','u','m','o','r','c','h','i','d'},
      {'m','e','d','i','u','m','p','u','r','p','l','e'},
      {'m','i','d','n','i','g','h','t','b','l','u','e'},
      {'m','e','d','i','u','m','s','e','a','g','r','e','e','n'},
      {'m','e','d','i','u','m','s','l','a','t','e','b','l','u','e'},
      {'m','e','d','i','u','m','t','u','r','q','u','o','i','s','e'},
      {'m','e','d','i','u','m','v','i','o','l','e','t','r','e','d'},
      {'m','e','d','i','u','m','a','q','u','a','m','a','r','i','n','e'},
      {'m','e','d','i','u','m','s','p','r','i','n','g','g','r','e','e','n'},
      {'n','a','v','y'},
      {'n','a','v','a','j','o','w','h','i','t','e'},
      {'o','l','i','v','e'},
      {'o','r','a','n','g','e'},
      {'o','r','c','h','i','d'},
      {'o','l','d','l','a','c','e'},
      {'o','l','i','v','e','d','r','a','b'},
      {'o','r','a','n','g','e','r','e','d'},
      {'p','e','r','u'},
      {'p','i','n','k'},
      {'p','l','u','m'},
      {'p','u','r','p','l','e'},
      {'p','a','l','e','g','r','e','e','n'},
      {'p','e','a','c','h','p','u','f','f'},
      {'p','a','p','a','y','a','w','h','i','p'},
      {'p','o','w','d','e','r','b','l','u','e'},
      {'p','a','l','e','g','o','l','d','e','n','r','o','d'},
      {'p','a','l','e','t','u','r','q','u','o','i','s','e'},
      {'p','a','l','e','v','i','o','l','e','t','r','e','d'},
      {'r','e','d'},
      {'r','o','s','y','b','r','o','w','n'},
      {'r','o','y','a','l','b','l','u','e'},
      {'s','n','o','w'},
      {'s','a','l','m','o','n'},
      {'s','i','e','n','n','a'},
      {'s','i','l','v','e','r'},
      {'s','k','y','b','l','u','e'},
      {'s','e','a','g','r','e','e','n'},
      {'s','e','a','s','h','e','l','l'},
      {'s','l','a','t','e','b','l','u','e'},
      {'s','l','a','t','e','g','r','a','y'},
      {'s','t','e','e','l','b','l','u','e'},
      {'s','a','n','d','y','b','r','o','w','n'},
      {'s','a','d','d','l','e','b','r','o','w','n'},
      {'s','p','r','i','n','g','g','r','e','e','n'},
      {'t','a','n'},
      {'t','e','a','l'},
      {'t','h','i','s','t','l','e'},
      {'t','o','m','a','t','o'},
      {'t','u','r','q','u','o','i','s','e'},
      {'u','i','s','h','a','d','o','w'},
      {'u','i','c','o','n','t','r','o','l'},
      {'u','i','h','i','g','h','l','i','g','h','t'},
      {'v','i','o','l','e','t'},
      {'w','h','e','a','t'},
      {'w','h','i','t','e'},
      {'w','h','i','t','e','s','m','o','k','e'},
      {'y','e','l','l','o','w'},
      {'y','e','l','l','o','w','g','r','e','e','n'}
   };


   static int[] presetVals = {
      0x00ffff,    //aqua
      0xf0ffff,    //azure
      0xf0f8ff,    //aliceblue
      0x7fffd4,    //aquamarine
      0xfaebd7,    //antiquewhite
      0x0000ff,    //blue
      0xf5f5dc,    //beige
      0x000000,    //black
      0xa52a2a,    //brown
      0xffe4c4,    //bisque
      0xdeb887,    //burlywood
      0x8a2be2,    //blueviolet
      0xffebcd,    //blanchedalmond
      0x00ffff,    //cyan
      0xff7f50,    //coral
      0xdc143c,    //crimson
      0xfff8dc,    //cornsilk
      0x5f9ea0,    //cadetblue
      0xd2691e,    //chocolate
      0x7fff00,    //chartreuse
      0x6495ed,    //cornflowerblue
      0x8b0000,    //darkred
      0x696969,    //dimgray
      0x00008b,    //darkblue
      0x008b8b,    //darkcyan
      0xa9a9a9,    //darkgray
      0xff1493,    //deeppink
      0x006400,    //darkgreen
      0xbdb76b,    //darkkhaki
      0xff8c00,    //darkorange
      0x9932cc,    //darkorchid
      0xe9967a,    //darksalmon
      0x9400d3,    //darkviolet
      0x1e90ff,    //dodgerblue
      0x8b008b,    //darkmagenta
      0x00bfff,    //deepskyblue
      0x8fbc8f,    //darkseagreen
      0xb8860b,    //darkgoldenrod
      0x483d8b,    //darkslateblue
      0x2f4f4f,    //darkslategray
      0x00ced1,    //darkturquoise
      0x556b2f,    //darkolivegreen
      0xff00ff,    //fuchsia
      0xb22222,    //firebrick
      0xfffaf0,    //floralwhite
      0x228b22,    //forestgreen
      0xffd700,    //gold
      0x808080,    //gray
      0x008000,    //green
      0xdcdcdc,    //gainsboro
      0xdaa520,    //goldenrod
      0xf8f8ff,    //ghostwhite
      0xadff2f,    //greenyellow
      0xff69b4,    //hotpink
      0xf0fff0,    //honeydew
      0xfffff0,    //ivory
      0x4b0082,    //indigo
      0xcd5c5c,    //indianred
      0xf0e68c,    //khaki
      0x00ff00,    //lime
      0xfaf0e6,    //linen
      0xe6e6fa,    //lavender
      0x7cfc00,    //lawngreen
      0xadd8e6,    //lightblue
      0xe0ffff,    //lightcyan
      0xd3d3d3,    //lightgrey
      0xffb6c1,    //lightpink
      0x32cd32,    //limegreen
      0xf08080,    //lightcoral
      0x90ee90,    //lightgreen
      0xffa07a,    //lightsalmon
      0xffffe0,    //lightyellow
      0xfffacd,    //lemonchiffon
      0x87cefa,    //lightskyblue
      0xfff0f5,    //lavenderblush
      0x20b2aa,    //lightseagreen
      0x778899,    //lightslategray
      0xb0c4de,    //lightsteelblue
      0xfafad2,    //lightgoldenrodyellow
      0x800000,    //maroon
      0xff00ff,    //magenta
      0xffe4b5,    //moccasin
      0xf5fffa,    //mintcream
      0xffe4e1,    //mistyrose
      0x0000cd,    //mediumblue
      0xba55d3,    //mediumorchid
      0x9370db,    //mediumpurple
      0x191970,    //midnightblue
      0x3cb371,    //mediumseagreen
      0x7b68ee,    //mediumslateblue
      0x48d1cc,    //mediumturquoise
      0xc71585,    //mediumvioletred
      0x66cdaa,    //mediumaquamarine
      0x00fa9a,    //mediumspringgreen
      0x000080,    //navy
      0xffdead,    //navajowhite
      0x808000,    //olive
      0xffa500,    //orange
      0xda70d6,    //orchid
      0xfdf5e6,    //oldlace
      0x6b8e23,    //olivedrab
      0xff4500,    //orangered
      0xcd853f,    //peru
      0xffc0cb,    //pink
      0xdda0dd,    //plum
      0x800080,    //purple
      0x98fb98,    //palegreen
      0xffdab9,    //peachpuff
      0xffefd5,    //papayawhip
      0xb0e0e6,    //powderblue
      0xeee8aa,    //palegoldenrod
      0xafeeee,    //paleturquoise
      0xdb7093,    //palevioletred
      0xff0000,    //red
      0xbc8f8f,    //rosybrown
      0x4169e1,    //royalblue
      0xfffafa,    //snow
      0xfa8072,    //salmon
      0xa0522d,    //sienna
      0xc0c0c0,    //silver
      0x87ceeb,    //skyblue
      0x2e8b57,    //seagreen
      0xfff5ee,    //seashell
      0x6a5acd,    //slateblue
      0x708090,    //slategray
      0x4682b4,    //steelblue
      0xf4a460,    //sandybrown
      0x8b4513,    //saddlebrown
      0x00ff7f,    //springgreen
      0xd2b48c,    //tan
      0x008080,    //teal
      0xd8bfd8,    //thistle
      0xff6347,    //tomato
      0x40e0d0,    //turquoise
      0x808080,    //uishadow
      0xa0a0a0,    //uicontrol
      0xe0e0e0,    //uihighlight
      0xee82ee,    //violet
      0xf5deb3,    //wheat
      0xffffff,    //white
      0xf5f5f5,    //whitesmoke
      0xffff00,    //yellow
      0x9acd32     //yellowgreen
   };

   static int[] presetCodes = {
      1,     //aqua
      26,    //azure
      23,    //aliceblue
      25,    //aquamarine
      24,    //antiquewhite
      2,     //blue
      27,    //beige
      3,     //black
      31,    //brown
      28,    //bisque
      32,    //burlywood
      30,    //blueviolet
      29,    //blanchedalmond
      40,    //cyan
      36,    //coral
      39,    //crimson
      38,    //cornsilk
      33,    //cadetblue
      35,    //chocolate
      34,    //chartreuse
      37,    //cornflowerblue
      51,    //darkred
      60,    //dimgray
      41,    //darkblue
      42,    //darkcyan
      44,    //darkgray
      58,    //deeppink
      45,    //darkgreen
      46,    //darkkhaki
      49,    //darkorange
      50,    //darkorchid
      52,    //darksalmon
      57,    //darkviolet
      61,    //dodgerblue
      47,    //darkmagenta
      59,    //deepskyblue
      53,    //darkseagreen
      43,    //darkgoldenrod
      54,    //darkslateblue
      55,    //darkslategray
      56,    //darkturquoise
      48,    //darkolivegreen
      4,     //fuchsia
      62,    //firebrick
      63,    //floralwhite
      64,    //forestgreen
      67,    //gold
      5,     //gray
      6,     //green
      66,    //gainsboro
      68,    //goldenrod
      65,    //ghostwhite
      69,    //greenyellow
      71,    //hotpink
      70,    //honeydew
      74,    //ivory
      73,    //indigo
      72,    //indianred
      75,    //khaki
      7,     //lime
      94,    //linen
      76,    //lavender
      78,    //lawngreen
      80,    //lightblue
      82,    //lightcyan
      85,    //lightgrey
      86,    //lightpink
      93,    //limegreen
      81,    //lightcoral
      84,    //lightgreen
      87,    //lightsalmon
      92,    //lightyellow
      79,    //lemonchiffon
      89,    //lightskyblue
      77,    //lavenderblush
      88,    //lightseagreen
      90,    //lightslategray
      91,    //lightsteelblue
      83,    //lightgoldenrodyellow
      8,     //maroon
      95,    //magenta
      108,   //moccasin
      106,   //mintcream
      107,   //mistyrose
      97,    //mediumblue
      98,    //mediumorchid
      99,    //mediumpurple
      105,   //midnightblue
      100,   //mediumseagreen
      101,   //mediumslateblue
      103,   //mediumturquoise
      104,   //mediumvioletred
      96,    //mediumaquamarine
      102,   //mediumspringgreen
      9,     //navy
      109,   //navajowhite
      10,    //olive
      112,   //orange
      114,   //orchid
      110,   //oldlace
      111,   //olivedrab
      113,   //orangered
      121,   //peru
      122,   //pink
      123,   //plum
      11,    //purple
      116,   //palegreen
      120,   //peachpuff
      119,   //papayawhip
      124,   //powderblue
      115,   //palegoldenrod
      117,   //paleturquoise
      118,   //palevioletred
      12,    //red
      125,   //rosybrown
      126,   //royalblue
      136,   //snow
      128,   //salmon
      132,   //sienna
      13,    //silver
      133,   //skyblue
      130,   //seagreen
      131,   //seashell
      134,   //slateblue
      135,   //slategray
      138,   //steelblue
      129,   //sandybrown
      127,   //saddlebrown
      137,   //springgreen
      139,   //tan
      14,    //teal
      140,   //thistle
      141,   //tomato
      142,   //turquoise
      22,    //uishadow
      20,    //uicontrol
      21,    //uihighlight
      143,   //violet
      144,   //wheat
      15,    //white
      145,   //whitesmoke
      16,    //yellow
      146    //yellowgreen
   };

   static int[] presetLength      = new int[presets.length];
   static int[] presetStart       = new int[27];
   static Hashtable colorHash     = new Hashtable();
   static Hashtable highlightHash = new Hashtable();
   static Hashtable shadowHash    = new Hashtable();
   static Color[] colors          = new Color[250];
   static Color vLink;
   static Color imgGreen;
   static Color imgDark;
   static Color imgLight;
   static Color darkGray;
   static Color lightGray;
   static Color paleGray;
   static int   nextColor;
   static int   controlIndex;
   static int   highlightIndex;
   static int   shadowIndex;
   static int   blackIndex;
   static int   whiteIndex;
   static int   lightGrayIndex;
   static int   paleGrayIndex;
   static int   darkGrayIndex;

   static {

      int c = 0;
      int i, j;
      int index = -1;
      for (i=0; i<presets.length; i++) {
         presetLength[i] = presets[i].length;
         c = presets[i][0] - 97;
         if (c > index) {
            for (j=index+1; j<c; j++) {
               presetStart[j] = -i;
            } 
            presetStart[c] = i;
            index = c;
         }
      }
      for (i=c+1; i<26; i++) {
         presetStart[i] = -presets.length;
      }
      presetStart[26] = presets.length;
      
      colors[0]  = Color.white;   //a dummy to avoid null probs. Should never be accessed
      colors[17] = new Color(0xE0E0E0); colorHash.put(new Integer(0xE0E0E0), new Integer(17));
      colors[18] = new Color(0x404040); colorHash.put(new Integer(0x404040), new Integer(18));
      colors[19] = new Color(0XA0A0A0); colorHash.put(new Integer(0xA0A0A0), new Integer(19));

      for (i=0; i<presetVals.length; i++) {
         colors[presetCodes[i]] = new Color(presetVals[i]);
         colorHash.put(new Integer(presetVals[i]), new Integer(presetCodes[i]));
      }   
      controlIndex   = 20;
      highlightIndex = 21;
      shadowIndex    = 22;
      blackIndex     = 3;
      whiteIndex     = 15;
      lightGrayIndex = 13;
      paleGrayIndex  = 17;
      darkGrayIndex  = 5;
      nextColor      = 147;
      
      vLink     = colors[11];
      imgGreen  = colors[6];
      imgDark   = colors[18];
      imgLight  = colors[19];
      darkGray  = colors[5];
      lightGray = colors[13];
      paleGray  = colors[17];
      highlightHash.put(new Integer(15), new Integer(13));    //sets lightGray as highlight for white
      highlightHash.put(new Integer(3) , new Integer(15));    //white highlight for black
      shadowHash.put(new Integer(15), new Integer(5));        //darkGray shadow for white
      shadowHash.put(new Integer(3), new Integer(5));         //darkGray shadow for black
      colors[controlIndex] = UIManager.getColor("control");
      colors[highlightIndex] = UIManager.getColor("controlHighlight");
      colors[shadowIndex] = UIManager.getColor("controlShadow");
      CalPixels pix = new CalPixels();
      CalHTMLManager.addUserImage(pix.getWarningImage(), "cal_warning");

   }


   static synchronized int getColor(int n) {
   
      Integer v;
      
      if ((v = (Integer)colorHash.get(new Integer(n))) != null) {
         return v.intValue();
      } else {
         Color c = new Color(n);
         if (nextColor >= colors.length) {
            redimColorArray();
         }
         colors[nextColor] = new Color(n);
         colorHash.put(new Integer(n), new Integer(nextColor++));
         return (nextColor - 1);
      }
   }
   

   static synchronized int getHighlight(int n) {
   
      Integer v;

      if ((v = (Integer)highlightHash.get(new Integer(n))) != null) {
         return v.intValue();
      }
      if ((n < 0) || (n > colors.length)) {
         return 15;     //returns white - this should never happen but just in case...
      }
      
      Color c;
      int i = -1;
      int r = colors[n].getRed();
      int g = colors[n].getGreen();
      int b = colors[n].getBlue();

      if ((r > 235) && (g > 235) && (b > 235)) {
         i = 17;   //paleGray
      } else if (r > 250) {
         if ((b > 250) || ((b < 50) && (g < 250)) || (g < 50)) {
            i = 15;       //white            
         } else if ((g > 250) && (b < 50)) {
            i = 17;      //paleGray
         }
      } else if (((b > 250) && ((r < 50) && (g < 50))) || ((g > 250) && ((r < 50) && (b < 50)))) {
         i = 15;       //white
      }         
      
      if (i == -1) {
         if ((r > 160) || (g > 160) || (b > 160)) {
            c = new Color(Math.min(255, (int)(r * 1.3)),
                             Math.min(255, (int)(g * 1.3)),
                                Math.min(255, (int)(b * 1.3)));
         } else if ((r > 100) || (g > 100) || (b > 100)) {
            c = new Color(r + 70, g + 70, b + 70);
         } else {
            c = new Color(r + 80, g + 80, b + 80);
         }
         i = nextColor;
         if (nextColor >= colors.length) {
            redimColorArray();
         }
         colors[nextColor++] = c;
      }
      
      highlightHash.put(new Integer(n), new Integer(i));
      return i;
   }         


   static synchronized int getShadow(int n) {
   
      Integer v;
      
      if ((v = (Integer)shadowHash.get(new Integer(n))) != null) {
         return v.intValue();
      }
      if ((n < 0) || (n > colors.length)) {
         return 3;     //returns black - this should never happen but just in case...
      }
      
      Color c;
      int i = -1;
      int r = colors[n].getRed();
      int g = colors[n].getGreen();
      int b = colors[n].getBlue();

      if (nextColor >= colors.length) {
         redimColorArray();
      }
      if (r > 250) {
         if ((b > 250) || ((b < 50) && (g < 250)) || (g < 50)) {     
            c = new Color((int)(r*0.7), (int)(g*0.7), (int)(b*0.7));
            i = nextColor;
            colors[nextColor++] = c;
         } else if ((g > 250) && (b < 50)) {
            i = 5;      //darkGray
         }
      } else if (((b > 250) && ((r < 50) && (g < 50))) || ((g > 250) && ((r < 50) && (b < 50)))) {
         c = new Color((int)(r*0.7), (int)(g*0.7), (int)(b*0.7));
         i = nextColor;
         colors[nextColor++] = c;
      }
      
      if (i == -1) {
         if ((r > 160) || (g > 160) || (b > 160)) {
            c = new Color(Math.max(0, (int)(r * 0.7)),
                             Math.max(0, (int)(g * 0.7)),
                                Math.max(0, (int)(b * 0.7)));
         } else if ((r > 100) || (g > 100) || (b > 100)) {
            c = new Color(Math.max(0, r - 70),
                             Math.max(0, g - 70),
                                Math.max(0, b - 70));
         } else {
            c = new Color(Math.max(0, r - 50),
                             Math.max(0, g - 50),
                                Math.max(0, b - 50));
         }
         i = nextColor;
         colors[nextColor++] = c;
      }                               

      shadowHash.put(new Integer(n), new Integer(i));
      return i;
   }


   static synchronized int getHighlight(int n, int bg) {
   
      int x = getHighlight(n);
      int r2 = colors[bg].getRed();
      int g2 = colors[bg].getGreen();
      int b2 = colors[bg].getBlue();
      if ((r2 > 235) && (g2 > 235) && (b2 > 235)) {
         int r  = colors[x].getRed();
         int g  = colors[x].getGreen();
         int b  = colors[x].getBlue();
         int r3 = colors[n].getRed();
         int g3 = colors[n].getGreen();
         int b3 = colors[n].getBlue();
         if ((r > 235) && (g > 235) && (b > 235)) {
            //following tries to ensure highlight isn't same as n 
            if (((r3 > 176) && (r3 < 208)) && ((g3 > 176) && (g3 < 208)) && ((b3 > 176) && (b3 < 208))) {
               return paleGrayIndex;
            } else {
               return lightGrayIndex;
            }
         }
      }
      return x;
   }


   private static synchronized void redimColorArray() {

      Color[] a = new Color[nextColor << 1];
      System.arraycopy(colors, 0, a, 0, nextColor);
      synchronized(colors) {
         colors = a;
      }
   }
   
   static synchronized boolean shadowIsBlack(Color c) {
   
      if (c != null) {
         if ((c.getRed() < 100) && (c.getGreen() < 100) && (c.getBlue() < 100)) {
            return true;
         }
      }
      return false;
   }


   static synchronized boolean highlightIsWhite(Color c) {
   
      if (c != null) {
         if ((c.getRed() >239) && (c.getGreen() > 239) && (c.getBlue() > 239)) {
            return true;
         }
      }
      return false;
   }

}     
