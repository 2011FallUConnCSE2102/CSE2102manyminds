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
import java.net.MalformedURLException;

class CalTagFrame implements CalCons {

   String  name;
   URL     url;
   int     width;
   int     widthType;
   int     height;
   int     heightType;
   int     frameborder;
   int     marginwidth;
   int     marginheight;
   int     scrolling;
   int     align;
   int     hspace;
   int     vspace;
   boolean noresize;


   CalTagFrame(CalDoc doc, int attrStart, int attrEnd, URL baseURL) {

      Integer   value;
      CalLength length;
      String s;
      int n;
      width        = 40;
      height       = 40;
      widthType    = PERCENT;
      heightType   = PERCENT;
      marginwidth  = -1;
      marginheight = -1;
      align        = V_BOTTOM;
      frameborder  = 1;
      scrolling    = V_AUTO;
      
      for (int i=attrStart; i<attrEnd; i++) {
         switch (doc.attrTypes[i]) {
            case A_SRC: s = doc.attrStrings[i];
                        try {
                           url = new URL(baseURL, s);
                        } catch (MalformedURLException e2) {
                           url = null;
                           s = "file:///" + s;
                           try {
                              url = new URL(baseURL, s);
                           } catch (MalformedURLException e3) {
                              url = null;
                           }
                        }
                        break;
           case A_NAME: name = doc.attrStrings[i];
                        break;
        case A_MARGINW: marginwidth = doc.attrVals[i];
                        break;
        case A_MARGINH: marginheight = doc.attrVals[i];
                        break;
      case A_SCROLLING: if (doc.attrArgs[i] == V_YES) {
                           scrolling = V_YES;
                        } else if (doc.attrArgs[i] == V_NO) {
                           scrolling = V_NO;
                        }
                        break;
       case A_NORESIZE: noresize = true;
                        break;
    case A_FRAMEBORDER: if (("0").equalsIgnoreCase(doc.attrStrings[i])) {
                           frameborder = 0;
                        }
                        break;
          case A_WIDTH: if (doc.attrArgs[i] != RELATIVE) {
                           widthType = doc.attrArgs[i];
                           width     = doc.attrVals[i];
                        }
                        break;
         case A_HEIGHT: if (doc.attrArgs[i] != RELATIVE) {
                           heightType = doc.attrArgs[i];
                           height     = doc.attrVals[i];
                        }
                        break;
          case A_ALIGN: switch (doc.attrArgs[i]) {
                           case V_LEFT: case V_RIGHT: case V_TOP: case V_MIDDLE: case V_ABSBOTTOM:
                           case V_ABSMIDDLE: case V_ABSTOP: align = doc.attrArgs[i];
                        }
                        break;
         case A_HSPACE: hspace = doc.attrVals[i];
                        break;
         case A_VSPACE: vspace = doc.attrVals[i];
                        break;
         }
      }
   }
     
   synchronized void setViewerProperties(CalViewer viewer) {
     
      viewer.url          = this.url;
      viewer.frameborder  = this.frameborder;
      if (this.marginwidth >= 0) {
         viewer.marginwidth  = this.marginwidth;
      }
      if (this.marginheight >= 0) {
         viewer.marginheight = this.marginheight;
      }
      viewer.scrolling    = this.scrolling;
      if (this.name != null) {
         viewer.name         = this.name;
      }
      viewer.noresize     = this.noresize;
      viewer.hspace       = this.hspace;
      viewer.vspace       = this.vspace;
      viewer.revertScrollBars();
   }

}   
