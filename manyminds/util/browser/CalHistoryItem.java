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
import java.awt.Point;

class CalHistoryItem {

   URL    docURL;
   String targetFrame;
   String ref;
   Point  viewportPos;
   CalHistoryItem[] frameState;
      
   CalHistoryItem(URL url, String targ, String ref, Point p, CalHistoryItem[] state) {

      docURL      = url;
      targetFrame = targ;
      this.ref    = ref;
      viewportPos = p;
      frameState  = state;
  }
}
