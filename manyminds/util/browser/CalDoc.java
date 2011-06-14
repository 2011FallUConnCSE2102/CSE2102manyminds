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

import java.util.Vector;
import java.net.URL;

class CalDoc implements CalCons {

   char[]     charArray;
   int[]      tokenCodes;
   short[]    tags;
   short[]    attrTypes;
   short[]    attrArgs;
   int[]      attrVals;
   String[]   attrStrings;
   Vector     objectVector;
   CalImage   bgImage;
   
   int      bgcolor;
   int      highlight;
   int      shadow;
   int      textcolor;
   int      linkcolor;
   int      vlinkcolor;
   int      alinkcolor;
   URL      url;         //the URL that all links etc. will be resolved against
   URL      docURL;      //the actual URL of this doc. May be diff from above due to <BASE> tag
   String   baseTarget;  //a default frame target
   
   int      docType;
   int      fileSize;
   int      lineableTokens;
   int      state;
   boolean  imagesLoaded;
   boolean  hasImages;
   boolean  hasAnimated;
   int      linkHash;
   int      lastCharPos;
   String   title;
   String   errorMessage;
   
   CalDoc(URL url, CalViewer viewer) {
   
      this(url, viewer, true);
   }
   
   CalDoc(URL url, CalViewer viewer, boolean dimArray) {

      int j, ref;
      this.url       = url;
      docURL         = url;
      if (url != null) {
         char[] a = url.toExternalForm().toCharArray();
         ref = a.length;
         for (int k=a.length-1; k>4; k--) {
            if (a[k] == '#') {
               ref = k;
               break;
            }
         }               
         j = 0;
         for (int k=ref, p=0; k>0; k--) {
            j = (j * 37) + a[p++];
         }
         linkHash = j;
      }
      lineableTokens = -1;
      bgcolor        = viewer.bgcolor;
      textcolor      = viewer.textcolor; 
      linkcolor      = viewer.linkcolor;
      vlinkcolor     = viewer.vlinkcolor;
      alinkcolor     = viewer.alinkcolor;
      imagesLoaded   = true;
      hasImages      = false;
      fileSize       = -1;
   }
   
   
   synchronized void setDocumentAsImageWrapper(URL url2) {
   
      tokenCodes = new int[1];
      tags       = new short[4];
      objectVector = new Vector();
      tokenCodes[0] = -1;
      CalImage tagim = new CalImage(url2, this);
      tagim.forceImageLoad();
      objectVector.addElement(tagim);
      tags[1] = IMG;
      tags[2] = 0;   //points to object(0) in objectArray
      tags[3] = 0;   //no attributes
   }


   synchronized void setDocType(int n) {
   
      docType = n;
   }


   synchronized void setState(int n) {
   
      state = n;
   }
   

   synchronized void setBaseAddress(URL u, boolean notBase) {
   
      this.url = u;
   }


   synchronized void setBaseTarget(String s) {
   
      baseTarget = s;
   }


   synchronized void setAttributes(int attrStart, int attrEnd) {
   
      for (int i=attrStart; i<attrEnd; i++) {
         switch (attrTypes[i]) {
            case A_BGCOLOR   : bgcolor    = attrVals[i]; break;
            case A_TEXT      : textcolor  = attrVals[i]; break;
            case A_LINK      : linkcolor  = attrVals[i]; break;
            case A_VLINK     : vlinkcolor = attrVals[i]; break;
            case A_ALINK     : alinkcolor = attrVals[i]; break;
            case A_BACKGROUND: bgImage = new CalImage(this, attrStrings[i]); break;
         }
      }
      highlight = CalColor.getHighlight(bgcolor);
      shadow    = CalColor.getShadow(bgcolor);
      docType   = D_HTML;    //...because this method is only called when parser finds <BODY> tag
   }


   synchronized void setTitle(int start, int end, int charPos) {

      if (end < start) {
         title = "?";
      } else {
         StringBuffer sb = new StringBuffer();
         for (int i=start; i<=end; i++) {
            if (tokenCodes[i] > 0) {
               sb.append(charArray, charPos, tokenCodes[i]);
               charPos += tokenCodes[i];
            } else {
               sb.append(' ');
            }
         }
         title = sb.toString();
      }
   }
   
   
   synchronized String getTitle() {
   
      return title;
   }

   
   synchronized void setErrorMessage(String s) {
   
      errorMessage = s;
   }


   synchronized String getErrorMessage() {
   
      return errorMessage;
   }
   
}
