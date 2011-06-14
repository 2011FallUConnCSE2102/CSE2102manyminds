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

//note this class serves as a holder for form options in select tags, and
//params in object tags
class CalFO implements CalCons {

   String  name;
   String  initValue;
   String  currValue;    //for later implementation
   String  verboseValue;
   boolean disabled;     //maybe for later implementation
   boolean selected;
   int verbosePos;
   int charStart;

   CalFO(CalDoc doc, int attrStart, int attrEnd) {

      String s;

      for (int i=attrStart; i<attrEnd; i++) {
         switch (doc.attrTypes[i]) {
            case A_NAME       : name = doc.attrStrings[i]; break;
            case A_VALUE      : initValue = doc.attrStrings[i];
                                if (("!").equals(initValue)) {
                                   initValue = "";
                                }
                                break;
            case A_DISABLED   : disabled = true; break;
            case A_SELECTED   : selected = true; break;
         }
      }
   }   


   void setVerboseValue(CalDoc doc, int start, int end, int charPos) {

      if (end >= start) {
         StringBuffer sb = new StringBuffer();
         for (int i=start; i<=end; i++) {
            if (doc.tokenCodes[i] > 0) {
               sb.append(doc.charArray, charPos, doc.tokenCodes[i]);
               charPos += doc.tokenCodes[i];
            } else {
               sb.append(' ');
            }
         }
         if (sb.length() > 0) {
            verboseValue = sb.toString().trim();
         }
      }
      if (verboseValue == null) {
         if (initValue != null) {
            verboseValue = initValue;
         }
      }
   }
   
}
