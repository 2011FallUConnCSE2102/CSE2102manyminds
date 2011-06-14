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

import java.awt.Toolkit;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.StringTokenizer;

class CalImage implements CalCons, ImageObserver {

   int      width;
   int      height;
   int      nameHash;
   int      linkHash;
   int      usemapHash;
   int      align;
   int      hspace;
   int      vspace;
   int      border;
   int      status;
   int      percentW;
   int      percentH;
   int      formTagPos;           
   boolean  widthGiven;
   boolean  heightGiven;
   boolean  ready;
   boolean  animated;
   boolean  active;
   boolean  finished;
   String   altText;
   String   jname;
   String[] altLines;
   URL      url;
   
   CalDoc   doc;
   CalFonts f;
   Image    im;
   
   int count=0;

   //special constructor for image wrapped in CalDoc
   CalImage(URL url, CalDoc doc) {

      this.url   = url;
      this.doc   = doc;
      formTagPos = -1;
      width      = -1;
      height     = -1;
   }


   //special constructor for background image of document
   CalImage(CalDoc doc, String s) {

      this.doc   = doc;
      url        = null;
      formTagPos = -1;
      width      = -1;
      height     = -1;
      int w, h;
      
      try {
         url = new URL(doc.url, s);
      } catch (MalformedURLException e2) {
         url = null;
         s = "file:///" + s;
         try {
            url = new URL(doc.url, s);
         } catch (MalformedURLException e3) {
            url = null;
         }
      }
      
      if (url != null) {
         im = Toolkit.getDefaultToolkit().getImage(url);
         if (im == null) {
            setStatus(IMG_BROKEN);
         } else {
            setStatus(IMG_WAITING);
            w = im.getWidth(this);
            h = im.getHeight(this);     //forces image to load
            if ((w > 0) && (h > 0)) {
               checkWidthAndHeight(w, h);
               setStatus(IMG_LOADED);
            }
         }
      } else {
         setStatus(IMG_BROKEN);
      } 
   }


   CalImage(CalDoc doc, int attrStart, int attrEnd, CalFonts f) {

      this.doc   = doc;
      this.f     = f;
      url        = null;
      width      = -1;
      height     = -1;
      formTagPos = -1;
      URL mapURL;
      String s;
      int n, w, h;
      align = V_BOTTOM;  //bottom is default for images
            
      for (int i=attrStart; i<attrEnd; i++) {
         switch (doc.attrTypes[i]) {
            case A_SRC:
                 url = getURL(doc.attrStrings[i]);
                 if (url == null) {
                    status = IMG_BROKEN;
                 }
                 break;
            case A_ALT: altText = doc.attrStrings[i];
                 break;
            case A_WIDTH:
                 switch (doc.attrArgs[i]) {
                    case PIXEL   : if (doc.attrVals[i] >= 0) {
                                      width = doc.attrVals[i];
                                      widthGiven = true;
                                   }
                                   break;
                    case PERCENT : if (doc.attrVals[i] > 0) {
                                      percentW = doc.attrVals[i];
                                   }
                                   break;
                    case RELATIVE: break;
                 }
                 break;
            case A_HEIGHT: 
                 switch (doc.attrArgs[i]) {
                    case PIXEL   : if (doc.attrVals[i] >= 0) {
                                      height = doc.attrVals[i];
                                      heightGiven = true;
                                   }
                                   break;
                    case PERCENT : if (doc.attrVals[i] > 0) {
                                      percentH = doc.attrVals[i];
                                   }
                                   break;
                    case RELATIVE: break;
                 }
                 break;
            case A_ALIGN:
                 switch (doc.attrArgs[i]) {
                    case V_LEFT: case V_RIGHT: case V_TOP: case V_MIDDLE: case V_ABSBOTTOM:
                    case V_ABSMIDDLE: case V_ABSTOP: align = doc.attrArgs[i];
                 }
                 break;
            case A_HSPACE:
                 if (doc.attrVals[i] == 0) {
                    hspace = NONE;   //user explicitly wants no hspace. Overrides default float settings
                 } else {
                    hspace = doc.attrVals[i];
                 }
                 break;
            case A_VSPACE:
                 vspace = doc.attrVals[i];
                 break;
            case A_BORDER:
                 if (doc.attrVals[i] == 0) {
                    border = NONE;  //user explicitly wants no border, even if it's a link
                 } else {
                    border = doc.attrVals[i];
                 }
                 break;
            case A_USEMAP:
                 s = doc.attrStrings[i];
                 try {
                    mapURL = new URL(doc.url, s);
                    usemapHash = mapURL.hashCode();
                    if ((s = mapURL.getRef()) != null) {
                       usemapHash += s.hashCode();
                    }
                 } catch (MalformedURLException e2) {
                    //no handling
                 }
                 break;
            case A_JNAME:
                 jname = doc.attrStrings[i];
                 break;
         }
      }
      
      //set default 2 pixel hspace for floating images
      if (((align == V_LEFT) || (align == V_RIGHT)) && (hspace == 0)) {
         hspace = 2;
      }
      
      //we don't allow altText with percentage dimensions - too hard to line
      if ((percentW > 0) || (percentH > 0)) {
         altText = null;
      }
   }
   

   URL getURL(String s) {   

      URL u = null;
      try {
         u = new URL(doc.url, s);
      } catch (MalformedURLException e2) {
         u = null;
         s = "file:///" + s;
         try {
            u = new URL(doc.url, s);
         } catch (MalformedURLException e3) {
            u = null;
         }
      }
      return u;
   }
   
   
   boolean forceImageLoad() {      

      int w, h;
      ready = false;

      if (jname != null) {
         im = CalHTMLManager.getUserImage(jname);
      }
      if ((im == null) && (url != null)) {
         im = Toolkit.getDefaultToolkit().getImage(url);
      }
      if (im == null) {
         setStatus(IMG_BROKEN);
         setBrokenWidthAndHeight();
         ready = true;
      } else {
         setStatus(IMG_WAITING);
         w = im.getWidth(this);
         h = im.getHeight(this);     //forces image to load
         if ((w > 0) && (h > 0)) {
            checkWidthAndHeight(w, h);
            setStatus(IMG_LOADED);
            ready = true;
         }
      }
      if (!ready) {
         if (widthGiven && heightGiven) {         //|| ((percentW > 0) && (percentH > 0))) {
            if (altText != null) {
               if ((f.fm[HELV][NORM][1].stringWidth(altText) + 33) > width) {
                  //...then we need to line the altText
                  lineAltText();
                  if (altLines == null) {
                     altText = null;
                  }
               }
            }
            ready = true;
         }
      }
      
      return ready;
   }


   public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
   
      count++;
      //System.out.println("imageUpdate called for " + url + " " + count);
      if (((flags & ERROR) > 0) || ((flags & ABORT) > 0)) {
         //if ((flags & ERROR) > 0) {
            //System.out.println("Image error: " + url);
         //}
         //if ((flags & ABORT) > 0) {
            //System.out.println("Image abort");
         //}
         setStatus(IMG_BROKEN);
         setBrokenWidthAndHeight();
         ready = true;
         return false;
      } else if (((flags & WIDTH) > 0) && ((flags & HEIGHT) > 0)) {
         //System.out.println("Width & Height");
         checkWidthAndHeight(w, h);
         ready = true;
      }
      if ((flags & FRAMEBITS) > 0) {
         if (!animated) {
            animated = true;
            doc.hasAnimated = true;
            setStatus(IMG_LOADED);
         }
         return true;
         //return active;
      }
      if ((flags & ALLBITS) > 0) {
         //System.out.println("IMAGE LOADED!!! " + url);
         if (!ready) {
            checkWidthAndHeight(w, h);
            ready = true;
         }
         setStatus(IMG_LOADED);
         return false;
      } else if ((flags & SOMEBITS) > 0) {
         //System.out.println("Image loading...");
         setStatus(IMG_LOADING);
      }
      return true;
   }


   private void checkWidthAndHeight(int w, int h) {

      boolean reline = false;
      
      if (width == -1) {
         if ((height > 0) && (h > 0)) {    //then width needs adjusting to keep aspect ratio
            width = (((w * 100) / h) * height) / 100;
         } else {
            width = w;
         }
      }
      if (height == -1) {
         if (widthGiven && (width > 0) && (h > 0)) {    //then height needs adjusting to keep aspect ratio
            height = (((h * 100) / w) * width) / 100;
         } else {
            height = h;
         }
      }
   }   
   

   synchronized void setStatus(int n) {
   
      status = n;
      if ((status == IMG_LOADED) || (status == IMG_BROKEN)) {
         finished = true;
      }
   }
   

   synchronized int getStatus() {
   
      return status;
   }
   
   
   private void lineAltText() {
   
      if ((height < 16) || (width < 50)) {
         return;
      }

      String s;
      boolean added = false;
      int w;
      int spacew = f.spaceW[HELV][NORM][1];
      int widthsofar  = 0;
      int heightsofar = 0;
      int linecount   = 0;
      int maxW = width - 33;
      int maxH = height - 18;
      altLines = new String[2];
      StringBuffer sb = new StringBuffer();
      StringTokenizer tok = new StringTokenizer(altText);
      boolean done;

      while ((tok.hasMoreTokens()) && (heightsofar + f.ALTHEIGHT <= maxH)) {
         done = false;
         s = tok.nextToken();
         w = f.fm[HELV][NORM][1].stringWidth(s);
         while (!done) {
            if (widthsofar + w > maxW) {
               if (!added) {     //then this word is longer than the available width so add it anyway
                  sb.append(shrinkWord(s, maxW));
                  done = true;
               }
               linecount++;
               addAltLine(sb.toString(), linecount);
               heightsofar += f.ALTHEIGHT;
               sb.setLength(0);
               widthsofar = 0;
               added = false;
            } else {
               sb.append(s);
               widthsofar += w;
               if (widthsofar + spacew > maxW) {
                  linecount++;
                  addAltLine(sb.toString(), linecount);
                  heightsofar += f.ALTHEIGHT;
                  sb.setLength(0);
                  widthsofar = 0;
                  added = false;
               } else {
                  sb.append(" ");
                  widthsofar += spacew;
                  added = true;
               }
               done = true;
            }
         }
      }
      if ((sb.length() > 0) && (heightsofar + f.ALTHEIGHT <= maxH)) {
         linecount++;
         addAltLine(sb.toString(), linecount);
      }
   }
   
   
   private void addAltLine(String s, int n) {
   
      if (n == altLines.length) {
         String[] a = new String[n + 1];
         System.arraycopy(altLines, 0, a, 0, n);
         altLines = a;
      }
      altLines[n] = s;
   }


   private String shrinkWord(String s, int width) {
   
      while (s.length() > 1) {
         s = s.substring(0, s.length() - 1);
         if (f.fm[HELV][NORM][1].stringWidth(s) <= width) {
            return s;
         }
      }
      if (f.fm[HELV][NORM][1].stringWidth(s) <= width) {
         return s;
      }
      
      return new String("");
   }
   

   void setBrokenWidthAndHeight() {

      if (altText == null) {
         if (width == -1) {
            width = (height >= 0) ? height : 40;
         }
         if (height == -1) {
            height = (width >= 0) ? width : 40;
         }
      } else {
         if (width == -1) {
            width =  f.fm[HELV][NORM][1].stringWidth(altText) + 33;
            height = Math.max(f.ALTHEIGHT + 18, height);
         } else {
            if (height == -1) {
               height = width;
            }
            if ((f.fm[HELV][NORM][1].stringWidth(altText) + 33) > width) {
               //...then we need to line the altText
               lineAltText();
               if (altLines == null) {
                  altText = null;
               }
            }
         }
      }
   }
   
}
