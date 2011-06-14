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
import javax.swing.*;
import javax.swing.border.*;

public class CalCL extends JLabel implements ListCellRenderer, CalCons {

   private Border border;
   int     normbg;
   int     selectbg;
   int     textcolor;
   boolean useDefaults;
   Font    textFont;

   public CalCL(CalForm form) {

      super();
      this.textFont = form.textFont;
      setOpaque(true);
      normbg = form.fItem.bordercolor > 0 ? form.fItem.bordercolor : CalColor.whiteIndex;
      useDefaults = form.fItem.bgcolor > 0 ? false : true;
      if ((form.fItem.textcolor > 0) && (form.style == USE_CALPA_THREEDEE)) {
         textcolor = form.fItem.textcolor;
      } else {
         textcolor = CalColor.blackIndex;
      }
      selectbg  = form.shadow;
      border = BorderFactory.createEmptyBorder(1,2,1,5);
   }
   

   public Component getListCellRendererComponent(JList list, Object value, int index,
                                                           boolean isSelected, boolean cellHasFocus) { 
      setFont(textFont);
      if (isSelected) {
         if (useDefaults) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
         } else {
            setBackground(CalColor.colors[selectbg]);
            setForeground(Color.white);
         }         
      } else {
         setBackground(CalColor.colors[normbg]);
         setForeground(CalColor.colors[textcolor]);
      }
      setBorder(border);
      setText((value == null) ? "" : value.toString());
      setEnabled(list.isEnabled());
      return this;
   }
      
}
