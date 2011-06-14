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

class CalElement implements CalCons {

   //attr bit values are encoded as follows:
   //Val 1  = sub
   //Val 2  = sup
   //Val 4  = space
   //Val 8  = special e.g. table
   //Val 16 = strike
   //Val 32 = underline
   // register shift >> 10 gives utility value

   //item bit values:
   //bits 1-5 = item type  (1=table, 2=UL, 3=OL 4=HR 5=IMG)
   //bits 6 and above = item index/pointer
   
   //font bit values:
   //bits 1-4 = size  5-7 = style, 8 = color override, 9 and above = family
   
   CalElement next;
   int attr;
   int item;
   int width;
   int nameHash;
   int linkHash;
   int font;
   int fontColor;
   int charStart;
   int charCount;
     
   CalElement() {
   }

   //constructor for special element
   CalElement(int name) {

      attr = 8;
      nameHash = name;
   }


   void reset(int name) {
   
      attr = 8;
      nameHash  = name;
      item      = 0;
      linkHash  = 0;
      width     = 0;
      font      = 0;
      fontColor = 0;
      charStart = 0;
      charCount = 0;
   }

   
   void setState(int attr, int item, int width, int name, int link, int font, int color, int start, int n) {

      this.attr  = attr;
      this.item  = item;
      this.width = width;
      nameHash   = name;
      linkHash   = link;
      this.font  = font;
      fontColor  = color;
      charStart  = start;
      charCount  = n;
   }
         
   
   void setState(int[][] elements, int i) {

      attr      = elements[E_ATTR][i];
      item      = elements[E_ITEM][i];
      width     = elements[E_WIDTH][i];
      nameHash  = elements[E_NAMEH][i];
      linkHash  = elements[E_LINKH][i];
      font      = elements[E_FONT][i];
      fontColor = elements[E_COLOR][i];
      charStart = elements[E_CHARSTART][i];
      charCount = elements[E_CHARCOUNT][i];
   }


   void setItem(int type, int index) {
   
      index = index << 5;
      item  = (index | type);
   }
}
