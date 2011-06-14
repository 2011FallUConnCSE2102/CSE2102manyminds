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

class CalTableCell extends CalTableItem implements CalCons {

   boolean isBlank;
   boolean noWrap;
   int     type;
   int     start;
   int     end;
   int     charStart;
   int     rowNo;
   int     rowSpan;
   int     colSpan;
   int     prefHeight;
   
   
   CalTableCell(CalDoc doc, int attrStart, int attrEnd, int type, int start, int charStart, int rowNo) {
   
      super(doc, attrStart, attrEnd);
      if (prefWidthType == RELATIVE) {
         prefWidthType = NONE;
         prefWidth     = NONE;   //we don't allow relative widths for individual cells
      }
      this.type   = type;
      this.start  = start;
      this.charStart = charStart;
      this.rowNo  = rowNo;
      isBlank     = false;
      rowSpan     = 1;
      colSpan     = 1;
      end         = -1;
      noWrap      = false;
      prefHeight  = NONE;

      for (int i=attrStart; i<attrEnd; i++) {
         switch (doc.attrTypes[i]) {
            case A_HEIGHT:
                 prefHeight = Math.max(0, doc.attrVals[i]);
                 break;
            case A_ROWSPAN:
                 rowSpan = Math.max(1, doc.attrVals[i]);
                 break;
            case A_COLSPAN:
                 colSpan = Math.max(1, doc.attrVals[i]);
                 break;
            case A_NOWRAP:
                 noWrap = true;
                 break;
         }
      }
   }
   
   
   void setEndPos(int n) {
   
      end = n;
      if (end < start) {
         isBlank = true;
      }
   }
}
