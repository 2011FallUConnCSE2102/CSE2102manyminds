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

class CalTableItem implements CalCons {

   int   index;
   int   hAlign;
   int   vAlign;
   int   bgcolor;
   int   shadow;
   int   highlight;
   int   prefWidth;
   int   prefWidthType;
   int   span;
     

   CalTableItem() {
   }

   CalTableItem(CalDoc doc, int attrStart, int attrEnd) {

      hAlign    = NONE;
      vAlign    = NONE;      
      span      = 1;
      prefWidth = -1;
      prefWidthType = NONE;

      for (int i=attrStart; i<attrEnd; i++) {
         switch (doc.attrTypes[i]) {
            case A_WIDTH      : prefWidthType  = doc.attrArgs[i];
                                if ((prefWidthType == RELATIVE) && (doc.attrVals[i] == -1)) {
                                   prefWidth = 1;
                                } else {
                                   prefWidth = doc.attrVals[i];
                                }
                                break;
            case A_SPAN       : span = Math.max(doc.attrVals[i], 1);
                                break;
            case A_ALIGN      : switch (doc.attrArgs[i]) {
                                  case V_LEFT: case V_RIGHT : case V_CENTER: hAlign = doc.attrArgs[i]; break;
                                  case V_MIDDLE: hAlign = V_CENTER; break;
                                }
                                break;
            case A_VALIGN:      switch (doc.attrArgs[i]) {
                                  case V_TOP: case V_BOTTOM : case V_MIDDLE: vAlign = doc.attrArgs[i]; break;
                                  case V_CENTER: vAlign = V_MIDDLE; break;
                                }
                                break;
            case A_BGCOLOR    : bgcolor = doc.attrVals[i]; break;
            case A_BORDCOLOR  : shadow  = doc.attrVals[i];
                                highlight = shadow;
                                break;
            case A_BORDCOLORLT: highlight = doc.attrVals[i];
                                break;
            case A_BORDCOLORDK: shadow = doc.attrVals[i];
                                break;
         }
      }
   }   

}
