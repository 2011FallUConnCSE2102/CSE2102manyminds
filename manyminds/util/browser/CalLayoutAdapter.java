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

public class CalLayoutAdapter implements LayoutManager {

   public Dimension defaultDimension = new Dimension(0,0);
   
   public CalLayoutAdapter() {
   }
   
   public void addLayoutComponent(String name, Component comp) {
   }
      
   public void removeLayoutComponent(Component comp) {
   }
      
   public Dimension preferredLayoutSize(Container target) {
     
      return defaultDimension;
   }
      
   public Dimension minimumLayoutSize(Container target) {
      
      return defaultDimension;
   }
      
   public void layoutContainer(Container target) {
   }      

}
