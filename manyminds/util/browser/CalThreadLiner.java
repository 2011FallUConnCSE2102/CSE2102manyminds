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

class CalThreadLiner extends CalLiner {


   public CalThreadLiner(CalViewer viewer, CalFonts f, CalDoc doc, CalView view, 
                                                                CalHTMLPreferences pref, int nesting) {
   
      super(viewer, f, doc, view, pref, nesting);
      try {
         setPriority(4);
      } catch (IllegalArgumentException e) {
      } catch (SecurityException e2) {
      }
      isThreadLiner = true;
   }
   
   
   public void run() {
         
      try {
         while (((doc.state != PARSED) || (cPos < doc.lineableTokens)) && (view.lineState != CANCEL))  {
            if (doc.docType == D_HTML) {
               while (cPos < doc.lineableTokens) {
                  bgc = doc.bgcolor;
                  t = doc.tokenCodes[cPos];
                  if (t > 0) {
                     addWord();
                  } else if (t == 0) {
                     if (cWordWaiting) {
                        addWordToLine();
                        if (cWidthSoFar + cSpaceWidth > cWidth) {
                           addLine();
                        } else {
                           addSpace();
                        }
                     } else if (cLineEnd > cLineStart) {    //possibly an inline image has just been added
                        if (cWidthSoFar + cSpaceWidth > cWidth) {
                           addLine();
                        } else {
                           addSpace();
                        }
                     }               
                  } else {
                     tagPos = Math.abs(t);
                     tag = doc.tags[tagPos];
                     if (tag > 0) {
                        handleOpenTag();
                     } else {
                        tag = Math.abs(tag);
                        handleCloseTag();
                     }
                  }
                  cPos++;
                  view.linedHeight = cHeightSoFar;
                  view.finalWidth  = cFinalWidth;
               }
            }
            if ((doc.state != PARSED) && (view.lineState != CANCEL)) {
               try {
                  sleep(100);
               } catch (InterruptedException e) {
                  //no need to handle
               }
            } else {
               if (doc.docType == D_FRAMESET) {
                  break;
               }
            }
         }
         if (doc.state == PARSED) {           //finished loading
            if (doc.docType == D_FRAMESET) {
               createFrameSet();
               view.setLineState(D_FRAMESET2);
            } else {
               if (cNameHash != 0) {
                  view.addElement(cWordFlag, 0, cNameHash, cLinkHash, cCurrentFontInt, cFontColor);
                  cNameHash = 0;
                  cLineEnd++;
               }
               finalizeLine();
               if (cFloatOn) {
                  if ((floatElementL != null) || (floatElementR != null)) {
                     insertFloat();
                  }
                  clearFloat(V_ALL);
               }
               view.linedHeight = cHeightSoFar;
               view.finalWidth  = cFinalWidth;
               view.finalizeArrays();
               view.setLineState(LINED);
            }
         }
      } catch (Exception ex) {
         System.err.println("Exception thrown in ThreadLiner:" + ex.getMessage());
         ex.printStackTrace();
         //System.exit(1);
         if (doc != null) {
            doc.setErrorMessage(ex.getMessage());
            doc.setState(PARSE_FAILED);
         }
      } finally {
         releaseMemory();
      }
      this.stop();
   }


   private void createFrameSet() {
   
      CalFrameset set;
      int tag, tagPos;
      
      cPos = 0;
      while (cPos <= doc.tokenCodes.length) {
         if (doc.tokenCodes[cPos] < 0) {
            tagPos = Math.abs(doc.tokenCodes[cPos]);
            tag = doc.tags[tagPos];
            if ((tag > 0) && (tag == FRAMESET) && (doc.url != null)) {
               CalTagFrameset tf = (CalTagFrameset)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
               set = new CalFrameset(pref, viewer, tf, cNesting, doc.url, pref.frameSpacing, true);
               //keep the viewer's scrollpane border if frameset has invisible frameborders
               if (!set.frameBorders) {
                  if ((viewer.nesting == 0) || ((viewer.parent.frameset != null) &&
                                                         (viewer.parent.frameset.frameBorders))) {
                     set.keepBorder = true;
                  }
               }
               set.fillChildren(doc, cPos);
               view.setFrameset(set);
               break;
            }
         }
         cPos++;
      }
   }

}
