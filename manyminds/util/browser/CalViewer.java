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
import java.awt.print.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.FocusManager;
import javax.swing.border.Border;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Hashtable;


class CalViewer extends JPanel implements CalCons, ActionListener, FocusListener {
     
   CalHTMLPane        pane;
   String             name;
   CalDoc             doc;
   CalDoc             doc2;
   URL                url;
   Timer              timer;
   CalScrollPane      sp;
   CalViewport        viewport;
   CalFonts           f;
   CalFP              parser;
   CalHTMLPreferences pref;
   CalThreadLiner     threadLiner;
   CalFrameset        frameset;      //accessed by parent frameset
   CalViewer          parent;
   CalView            view;          //accessed by viewport
   BorderLayout       borderLayout;
   CalHistoryManager  hist;
   CalHistoryItem     newHistory;
   CalHistoryItem[]   waitingFrameState;
   CalElement         utilElement;
   boolean            waitingReload;
   Border             normBorder;
   Border             normViewportBorder;
   Border             fsetBorder;
   int                bgcolor;
   int                highlight;
   int                shadow;
   int                outercolor;
   int                textcolor;
   int                linkcolor;
   int                vlinkcolor;
   int                alinkcolor;
   int                tlinkcolor;
   Point              viewMarkPos;

   int                frameType;
   int                connectionMode;
   int                nesting;
   int                rootFontFamily;
   int                rootFontSize;
   int                rootFontStyle;
   int                activeLinkHash;
   int                activeTabLink;
   int                attemptedLink;
   int                marginwidth;
   int                marginheight;
   int                scrollbarwidth;
   int                hspace;
   int                vspace;
   int                scrolling;
   int                frameborder;
   int                vborder;
   int                hborder;
   int                currTabIndex;
   int                focusedTagNo;
   String             cCurrentTarget;
   boolean            noresize;
   boolean            keepPainting;
   boolean            imagesLoaded;
   boolean            imagesFinished;
   //boolean            enableAnimated;
   boolean            cachedDocument;
   boolean            relineFlag;
   boolean            resizedFlag;
   boolean            hasChildren;
   boolean            boundsSet;
   boolean            viewSet;
   boolean            paintFocus;
   boolean            titleUpdated;
   boolean            informedWaiting;
   boolean            isDialog;
   boolean            syncLock;


   CalViewer(CalHTMLPane pane, CalHTMLPreferences pref, CalHistoryManager hist, CalViewer parent, int nesting) {

      super();
      this.pane    = pane;
      this.pref    = pref;
      this.hist    = hist;
      this.parent  = parent;
      this.nesting = nesting;
      name = "%";        //avoids null string errors
      if (nesting == 0) {
         frameborder = 1;
         scrolling  = VIEWER;
         bgcolor    = pref.bgcolor;
         textcolor  = pref.textcolor; 
         linkcolor  = pref.linkcolor;
         vlinkcolor = pref.vlinkcolor;
         alinkcolor = pref.alinkcolor;
         sp = new CalScrollPane(this, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      } else {
         scrolling  = V_AUTO;
         bgcolor    = parent.bgcolor;
         highlight  = parent.highlight;
         shadow     = parent.shadow;
         textcolor  = parent.textcolor;
         linkcolor  = parent.linkcolor;
         vlinkcolor = parent.vlinkcolor;
         alinkcolor = parent.alinkcolor;
         sp = new CalScrollPane(this, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      }
      marginwidth  = pref.marginwidth;
      marginheight = pref.marginheight;
      viewport = new CalViewport(this);
      viewport.setView(this);
      sp.setViewport(viewport);
      sp.getVerticalScrollBar().setUnitIncrement(14);
      scrollbarwidth = Math.max(sp.getVerticalScrollBar().getPreferredSize().width + 2, 12);
      borderLayout = new BorderLayout();
      setLayout(null);
      setOpaque(true);
      setBackground(CalColor.colors[pref.bgcolor]);
      f = CalHTMLManager.f;
      timer = new Timer(500, this);
      addMouseListener(new LinkListener());
      addMouseMotionListener(new CursorListener());
      connectionMode = CONNECTED;
      normBorder = sp.getBorder();
      normViewportBorder = sp.getViewportBorder();
      if (normBorder != null) {
         Insets ins = normBorder.getBorderInsets(sp);
         hborder = ins.left + ins.right;
         vborder = ins.top  + ins.bottom;
      } else {
         hborder = 0;
         vborder = 0;
      }
      utilElement = new CalElement(0);
      fsetBorder = BorderFactory.createEmptyBorder(0,0,0,0);
      addKeyListener(pane);
      addFocusListener(this);
   }
  

   //returns the current history state of this viewer, or searches children for the target
   CalHistoryItem getHistoryState(String targetFrame) {
      if (targetFrame.equals(name)) {
         if ((doc == null) || (doc.docURL == null)) {
            return null;
         }
         if (frameset == null) {
            if ((view != null) && (view.iFrames != null)) {
               return new CalHistoryItem(doc.docURL, name, null, viewport.getViewPosition(),
                                                                                view.getFrameState());
            } else {
               return new CalHistoryItem(doc.docURL, name, null, viewport.getViewPosition(), null);
            }
         } else {
            return new CalHistoryItem(doc.docURL, name, null, null, frameset.getFrameState());
         }
      } else if (frameset != null) {
         return frameset.getHistoryState(targetFrame);
      } else if ((view != null) && (view.iFrames != null)) {
         return view.getHistoryState(targetFrame);
      }

      return null;
   }  


   //this method determines whether to show the requested document in this component, or down the tree
   int showDocument(CalHistoryItem h, String s, boolean reload, int histState, int link) {

      if (h.targetFrame.equals(name)) {
         informedWaiting = false;
         imagesFinished  = false;
         if (s != null) {
            if (h.docURL == null) {
               return showHTMLDocument(h, s);      //a string HTML doc
            } else {
               return showHTMLPostDocument(h, s);    //a form POST doc
            }
         } else {
            attemptedLink = link;
            return showHTMLDocument(h, reload, histState);
         }
      } else if (frameset != null) {
         return frameset.showDocument(h, s, reload, histState, link);
      } else if ((view != null) && (view.iFrames != null)) {
         return view.showDocument(h, s, reload, histState, link);
      }
      return NOT_FOUND;
   }


   //special version of showDocument for showing an HTML string
   int showHTMLDocument(CalHistoryItem h, String parseString) {
     
      int width   = getLiningWidth();
      int height  = getLiningHeight();
      syncLock = pane.syncLock;
      stopAnimated();
      doc2 = new CalDoc(CalHTMLManager.getDummyURL(), this);
      checkParser();
      checkThreadLiner();
      doc = doc2;
      h.docURL = doc.docURL;
      newHistory = h;
      parser = new CalFP(null, doc, f, pref, parseString);
      parser.start();
      keepPainting = true;
      cachedDocument = false;
      //enableAnimated = false;
      removeAnyChildren();
      connectionMode = CONNECTED;
      pane.statusUpdate(CONNECTED, doc.docURL, nesting, null);
      relineDocument(width, height, false, true);
      if (syncLock) {
         waitForSyncLock();
      }
      return SHOWN;
   }
      

   //special version of showDocument for dealing with a form POST
   int showHTMLPostDocument(CalHistoryItem h, String formData) {
   
      int width   = getLiningWidth();
      int height  = getLiningHeight();
      doc2 = new CalDoc(h.docURL, this);
      newHistory = h;
      checkParser();
      checkThreadLiner();
      doc2.setState(PRE_CONNECT);
      parser = new CalFP(h.docURL, doc2, f, pref, formData);
      parser.setReload();
      parser.start();
      connectionMode = PRE_CONNECT;
      pane.statusUpdate(PRE_CONNECT, h.docURL, nesting, null);
      timer.setInitialDelay(50);
      timer.setDelay(50);
      if (timer.isRunning()) {
         timer.restart();
      } else {
         timer.start();
      }
      return SHOWN;
   }


   int showHTMLDocument(CalHistoryItem h, boolean reload, int histState) {
   
      if (histState != HISTORY_NEW) {
         newHistory = null;
         if (histState != HISTORY_NULL) {
            if (h.frameState == null) {
               waitingFrameState = null;
               waitingReload = false;
            } else {
               waitingFrameState = h.frameState;
               waitingReload = reload;
            }
         }
      } else {
         newHistory = h;
      }

      //if it's the same document and not a reload, use the one we've got
      if ((!reload) && (doc != null) && (doc.docURL != null) && h.docURL.sameFile(doc.docURL)) {
         if (h.ref != null) {
            if (goToLink(h.ref.hashCode())) {
               updateHistory();
               waitingFrameState = null;
               pane.viewer.updateLinks();
            }
            return SHOWN;
         } else if (h.viewportPos != null) {
            Point p = viewport.getViewPosition();
            if ((h.viewportPos.x == p.x) && (h.viewportPos.y == p.y)) {
               return ALREADY_SHOWN;
            }
            viewport.setViewPosition(h.viewportPos);
            updateHistory();
            waitingFrameState = null;
            return SHOWN;
         } else {
            return ALREADY_SHOWN;
         }
      }

      //...otherwise it's a new document or a reload
      syncLock    = pane.syncLock;
      viewMarkPos = h.viewportPos;
      viewSet     = false;
      int width   = getLiningWidth();
      int height  = getLiningHeight();

      if (CalHTMLManager.cacheDocuments) {
         if (reload) {
            CalHTMLManager.removeDocument(h.docURL);
         } else {
            if ((doc2 = CalHTMLManager.getDocument(h.docURL)) != null) {
               checkParser();
               checkThreadLiner();
               stopAnimated();
               doc = doc2;
               removeAnyChildren();
               titleUpdated = false;
               connectionMode = CONNECTED;
               pane.statusUpdate(CONNECTED, doc.docURL, nesting, null);
               //System.out.println("*********Relining cached document");
               relineDocument(width, height, true, true);
               if (syncLock) {
                  waitForSyncLock();
               }
               return SHOWN;
            }
         }
      }
      
      checkParser();
      checkThreadLiner();
      if (timer.isRunning()) {
         timer.stop();
      }
      doc2 = new CalDoc(h.docURL, this);
      doc2.setState(PRE_CONNECT);
      parser = new CalFP(h.docURL, doc2, f, pref, null);
      if (reload) {
         parser.setReload();
      }
      titleUpdated = false;
      //setWaitCursor(true);
      connectionMode = PRE_CONNECT;
      parser.start();
      pane.statusUpdate(PRE_CONNECT, h.docURL, nesting, null);
      if (syncLock) {
         waitForSyncLock();
      } else {
         timer.setInitialDelay(50);
         timer.setDelay(50);
         timer.start();
      }
      return SHOWN;
   }


   private void waitForSyncLock() {

      while (syncLock) {
         try {
            Thread.currentThread().sleep(connectionMode == PRE_CONNECT ? 100 : 500);
            checkLoadState();
         } catch (InterruptedException e) {
            stopAllProcesses();
            syncLock = false;
         }
      }
   }
   

   //called by the Timer
   public void actionPerformed(ActionEvent e) {

      checkLoadState();
   }


   private void checkLoadState() {
   
      String s;
      if (connectionMode == PRE_CONNECT) {
         if (doc2 != null) {
            if (doc2.state == PARSE_FAILED) {
               newHistory = null;
               pane.statusUpdate(PARSE_FAILED, doc2.docURL, nesting, doc2.getErrorMessage());
               doc2.setErrorMessage(null);
               checkParser();
               //setWaitCursor(false);
               doc2 = doc;    //need to do this or problems if reline called from viewport
               connectionMode = CONNECTED;   //as above
               timer.stop();
               syncLock = false;
            } else if (doc2.state != PRE_CONNECT) {
               stopAnimated();
               doc = doc2;
               keepPainting = true;
               cachedDocument = false;
               //enableAnimated = false;
               removeAnyChildren();
               connectionMode = CONNECTED;
               pane.statusUpdate(CONNECTED, doc.docURL, nesting, null);
               if (doc.fileSize >= 0) {
                  pane.statusUpdate(DOC_LENGTH, doc.docURL, doc.fileSize, null);
               }
               //setWaitCursor(false);
               relineDocument(getLiningWidth(), getLiningHeight(), false, true);
               repaint();
            }
         }
         return;
      }
      
      if ((doc == null) || (doc.state == PARSE_FAILED)) {
         timer.stop();
         syncLock = false;
         if ((doc != null) && (doc.state == PARSE_FAILED)) {
            checkParser();
            checkThreadLiner();
            pane.statusUpdate(PARSE_FAILED_POST_CONNECT, doc.docURL, nesting, doc.getErrorMessage());
            doc.setErrorMessage(null);
         }
      } else if (relineFlag) {
         relineFlag = false;
         timer.stop();
         checkThreadLiner();
         removeAnyChildren();
         relineDocument(getLiningWidth(), getLiningHeight(), doc.state == PARSED ? true : false, true);         
      } else if (doc.docType == D_HTML) {
         if (frameset != null) {
            frameset.releaseMemory();
            remove(frameset);
            frameset = null;
            setLayout(null);
            revertScrollBars();
            if (frameborder != 0) {
               sp.setBorder(normBorder);
               sp.setViewportBorder(normViewportBorder);
            } else {
               sp.setBorder(fsetBorder);
               sp.setViewportBorder(null);
            }
            sp.invalidate();
            sp.getParent().validate();
         }
         if ((doc.state == WAITING_FOR_IMAGES) && (!informedWaiting)) {
            pane.statusUpdate(WAITING_FOR_IMAGES, doc.docURL, nesting, null);
            informedWaiting = true;
         }
         if ((doc.state == PARSED) && (view.lineState == LINED)) {
            if (!titleUpdated) {
               pane.statusUpdate(TITLE, doc.docURL, nesting, doc.getTitle());
               titleUpdated = true;
            }
            if (!resizedFlag) {
               setPreferredSize(new Dimension(view.finalWidth + marginwidth, view.linedHeight + (marginheight << 1)));
               viewport.invalidate();
               sp.validate();
               if ((newHistory != null) && (newHistory.ref != null)) {
                  goToLink(newHistory.ref.hashCode());
               } else if (!viewSet) {
                  viewport.setViewPosition(viewMarkPos == null ? DOCTOP : viewMarkPos);
                  viewMarkPos = null;
               }
               updateHistory();
               waitingFrameState = null;
               sp.canCheckSize = true;
               viewport.invalidate();
               sp.validate();
               resizedFlag = true;
               currTabIndex = -1;
               activeTabLink = 0;
            }
            if (!cachedDocument) {
               if (CalHTMLManager.cacheDocuments) {
                  CalHTMLManager.addDocument(doc);
               }
               cachedDocument = true;
            }
            if (!imagesFinished) {
               if (checkImagesFinished()) {
                  imagesFinished = true;
                  pane.statusUpdate(DOC_LOADED, doc.docURL, nesting, null);
                  parser = null;
                  if (nesting == 0) {
                     System.gc();
                     //System.out.println("Total memory = " + Runtime.getRuntime().totalMemory());
                     //System.out.println("Free memory = " + Runtime.getRuntime().freeMemory());
                     //System.out.println("Documents = " + CalHTMLManager.documentCache.size());
                  }
                  syncLock = false;
                  if (keepPainting) {
                     timer.setDelay(100);
                  }
               } else {
                  timer.setDelay(1000);
               }
            } else {
               syncLock = false;
               if (!keepPainting) {
                  timer.stop();
               }
            }
            repaint();
            return;
         }
         if (!viewSet) {
            if (viewMarkPos != null) {
               if ((viewMarkPos.y + view.viewportHeight) > (view.linedHeight + marginheight)) {
                  return;   //we're not ready to display yet
               } else {
                  setPreferredSize(new Dimension(view.finalWidth + marginwidth, view.linedHeight + (marginheight << 1)));
                  viewport.invalidate();   //this next section looks daft, but it's necessary
                  sp.validate();
                  viewport.setViewPosition(viewMarkPos);
                  viewport.invalidate();
                  sp.validate();
               }
            } else {
               viewport.setViewPosition(DOCTOP);
            }
            viewSet = true;
         }
         if (view.linedHeight > 0) {
            setPreferredSize(new Dimension(view.finalWidth + marginwidth, view.linedHeight + (marginheight << 1)));
            viewport.invalidate();
            sp.validate();
            repaint();
         }
      } else if (view.lineState == D_FRAMESET2) {
         if ((!titleUpdated) && (nesting == 0)) {
            s = doc.getTitle();
            if (s == null) {
               s = "Frameset";
            }
            pane.statusUpdate(TITLE, doc.docURL, nesting, s);
            titleUpdated = true;
         }
         view.setLineState(D_FRAMESET3);
         timer.stop();
         if (frameset != null) {
            frameset.releaseMemory();
            remove(frameset);
         }
         if (view.frameset != null) {
            setFrameset(view.frameset);
            if (waitingFrameState != null) {    //means we're showing a history item
               frameset.activateFrames(waitingFrameState, waitingReload);
               waitingFrameState = null;
               waitingReload = false;
            } else {
               updateHistory();
               frameset.activateFrames(null, false);
            }
         }
         syncLock = false;
      }
   }


   void relineDocument(int width, int height, boolean alreadyParsed, boolean resizeTables) {
   
      view = new CalView(doc); 
      view.docWidth = width - (marginwidth << 1);
      setBaseParameters(width, height);
      timer.setInitialDelay(350);
      timer.setDelay(500);
      threadLiner = new CalThreadLiner(this, f, doc, view, pref, nesting);
      view.linedWidth     = width;
      view.viewportHeight = height;
      threadLiner.reset(view.docWidth, new CalStackFont(rootFontFamily, rootFontStyle,
            0, rootFontSize, 0), 0, 0, V_LEFT, marginwidth, doc.bgcolor, resizeTables);
      threadLiner.start();
      relineFlag  = false;
      resizedFlag = false;
      focusedTagNo = 0;
      if (syncLock) {
         if (timer.isRunning()) {
            timer.stop();
         }
      } else {
         if (timer.isRunning()) {
            timer.restart();
         } else {
            timer.start();
         }
      } 
   }

   
   private boolean checkImagesFinished() {

      if (!doc.hasImages) {
         return true;
      }
      CalImage tagim;
      boolean  done = true;

      for (int i=doc.objectVector.size()-1; i>=0; i--) {
         if (doc.objectVector.elementAt(i) instanceof CalImage) {
            tagim = (CalImage)doc.objectVector.elementAt(i);
            if (!tagim.finished) {
               done = false;
            }
         }
      }
      return done;
   }


   //private void setWaitCursor(boolean b) {

   //   if (b) {
   //      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
   //   } else {
   //      setCursor(Cursor.getDefaultCursor());
   //   }
   //}
   

   void updateHistory() {
   
      if (newHistory != null) {
         hist.addHistoryItem(newHistory);
         newHistory = null;
         if (attemptedLink != 0) {
            pref.addVisitedLink(attemptedLink);
            attemptedLink = 0;
         }
         if (doc != null) {
            pref.addVisitedLink(doc.linkHash);
         }  
      }
   }
   

   void updateLinks() {

      boolean update = false;
      
      if ((doc != null) && (doc.tokenCodes != null)) {
         //if (doc.hasAnimated) {
         //   enableAnimated = true;
         //   update = true;
         //}
         for (int i=0, n, tagPos; i<doc.lineableTokens; i++) {
            if (doc.tokenCodes[i] < 0) {
               tagPos = Math.abs(doc.tokenCodes[i]);
               if (doc.tags[tagPos] == A) {
                  n = doc.attrVals[doc.tags[tagPos + 2]];
                  if ((n != -1) && ((pref.visitedHash.get(new Integer(n))) != null)) {
                     update = true;
                     doc.attrVals[doc.tags[tagPos + 2]] = -1;
                  }
               }
            }
         }
         if (update) {
            repaint();
         }
      }
      if (frameset != null) {
         frameset.updateLinks();
      } else if ((view != null) && (view.iFrames != null)) {
         view.updateLinks();
      }
   }


   void stopAllProcesses() {

      if ((doc != null) && (doc.state == WAITING_FOR_IMAGES) && (parser.isAlive())) {
         parser.interrupt();
      } else {
         checkParser();
         checkThreadLiner();
         if (timer.isRunning()) {
            timer.stop();
         }
         attemptedLink = 0;
         updateHistory();      //if we don't do this we may not be able to go back in the history list
         waitingFrameState = null;
         waitingReload = false;
         attemptedLink = 0;
         viewMarkPos = null;
      }
      if (frameset != null) {
         frameset.stopAllProcesses();
      } else if (view != null) {
         if (view.iFrames != null) {
            view.stopAllProcesses();
         }
         if (view.linedHeight > 0) {
            setPreferredSize(new Dimension(view.finalWidth + marginwidth, view.linedHeight + (marginheight << 1)));
            viewport.invalidate();
            sp.validate();
            repaint();
         }
      }
   }
      

   boolean allFinished() {

      //if (timer.isRunning()) {
      //   if ((connectionMode == PRE_CONNECT) || ((doc != null) && (doc.state != PARSED)) ||
      //                  ((view != null) && (view.lineState != LINED))) {
      //      return false;
      //   }
      //} else {
      //   if (frameset != null) {
      //      return frameset.allFinished();
      //   } else if ((view != null) && (view.iFrames != null)) {
      //      return view.allFinished();
      //   }
      //}
      //return true;
      if (timer.isRunning()) {
         if ((connectionMode == PRE_CONNECT) || (!imagesFinished)) {
            return false;
         }
      }
      if (frameset != null) {
         return frameset.allFinished();
      } else if ((view != null) && (view.iFrames != null)) {
         return view.allFinished();
      }
      return true; 
   }


   void checkParser() {               //forces parser to stop if it's parsing

      if ((parser != null) && (parser.isAlive())) {
         parser.stop();
      }
   }


   void checkThreadLiner() {               //forces threadLiner to stop if it's relining

      if ((threadLiner != null) && (threadLiner.isAlive())) {
         threadLiner.stop();
      }
   }

    
   void setFrameset(CalFrameset frameset) {
   
      this.frameset = frameset;
      if (frameset != null) {
         removeAnyChildren();
         if (frameset.keepBorder) {
            sp.setBorder(normBorder);
            sp.setViewportBorder(normViewportBorder);
         } else {
            sp.setBorder(fsetBorder);
            sp.setViewportBorder(null);
         }
         setLayout(borderLayout);
         add("Center", frameset);
         sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
         setPreferredSize(viewport.getViewerSizeWhenFrameset());
         sp.invalidate();
         if (sp.getParent() != null) {
            sp.getParent().validate();
         }
      }
   }


   void removeAnyChildren() {
   
      if (hasChildren) {
         if (view != null) {
            if (view.forms != null) {
               for (int i=0; i<view.formIndex; i++) {
                  view.forms[i].comp = null;
                  view.forms[i] = null;
               }
            }
            if (view.iFrames != null) {
               for (int i=0; i<view.iFrameIndex; i++) {
                  if (view.iFrames[i].frameset != null) {
                     view.iFrames[i].frameset.releaseMemory();
                  } else {
                     view.iFrames[i].removeAnyChildren();
                  }
                  view.iFrames[i].releaseMemory();
               }
            }
         }
         this.removeAll();
         hasChildren = false;
      }
   }


   CalViewer getFrame(String target) {

      if (target.equals(name)) {
         return this;
      } else if (frameset != null) {
         return frameset.getFrame(target);
      } else if ((view != null) && (view.iFrames != null)) {
         return view.getFrame(target);
      }
      return null;
   }


   void getComponents(Hashtable table, String targetFrame) {

      boolean matches = name.equals(targetFrame);
      if (frameset != null) {
         if (!matches) {
            frameset.getComponents(table, targetFrame);
         }
      } else if (view != null) {
         view.getComponents(table, targetFrame, matches);
      }
   }

   
   void setBaseParameters(int width, int height) {

      if (pref.optimizeDisplay == NO_OPTIMIZATION) {
         view.displaySize = S_LARGE;
         rootFontSize   = pref.fontVals[S_LARGE];
         rootFontFamily = pref.famVals[S_LARGE];
         rootFontStyle  = pref.styleVals[S_LARGE];
      } else {
         if (width < pref.frameWidth[S_SMALL]) {
            view.displaySize = (pref.optimizeDisplay == OPTIMIZE_ALL) ? S_SMALL : S_LARGE;
            rootFontSize     = pref.fontVals[S_SMALL];
            rootFontFamily   = pref.famVals[S_SMALL];
            rootFontStyle    = pref.styleVals[S_SMALL];

         } else if (width < pref.frameWidth[S_MEDIUM]) {
            view.displaySize = (pref.optimizeDisplay == OPTIMIZE_ALL) ? S_MEDIUM : S_LARGE;
            rootFontSize     = pref.fontVals[S_MEDIUM];
            rootFontFamily   = pref.famVals[S_MEDIUM];
            rootFontStyle    = pref.styleVals[S_MEDIUM];
         } else {
            view.displaySize = S_LARGE;
            rootFontSize     = pref.fontVals[S_LARGE];
            rootFontFamily   = pref.famVals[S_LARGE];
            rootFontStyle    = pref.styleVals[S_LARGE];
         }
      }

      f.checkFont(rootFontFamily, rootFontStyle, rootFontSize);
   }
   
   
   public void paint(Graphics g) {
   
      if (doc == null) {
         super.paint(g);
         return;
      } else {
         setBackground(CalColor.colors[doc.bgcolor]);
      }
      if (frameset != null) {
         super.paint(g);
         return;
      }
      super.paintComponent(g);
      if ((view == null) || (view.lines == null) || (view.paintableLines <= 0)) {
         return;
      }
      boolean waitForBackground = false;

      imagesLoaded = ((doc.state == PARSED) && (view.lineState == LINED)) ? true : false;
      g.setColor(CalColor.colors[doc.bgcolor]);
      Rectangle r = viewport.getViewRect();
      g.fillRect(r.x, r.y, r.width, r.height);
      if (doc.bgImage != null) {
         if (doc.bgImage.status == IMG_LOADED) {
            if (doc.state != WAITING_FOR_IMAGES) { 
               wallpaperImage(g, doc.bgImage, r.x, r.y, r.width, r.height);
            }
         } else if (doc.bgImage.status != IMG_BROKEN) {
            waitForBackground = true;
         }
      }
      paintLineArray(g, view, r.y, r.height, marginheight, 0, view.paintableLines);
      if ((doc.state == PARSED) && (view.lineState == LINED) && (imagesLoaded) && (!waitForBackground)) {
         keepPainting = false;
      } else if ((!imagesLoaded) && (!timer.isRunning()) && (doc.state != PARSE_FAILED) && (!syncLock)) {
         timer.setDelay(100);
         timer.start();
      }
      if (hasChildren) {
         super.paintChildren(g);
      }
      if (paintFocus && (nesting > 0)) {
         g.setColor(CalColor.darkGray);
         g.drawRect(r.x, r.y, r.width-1, r.height-1);
         paintFocus = false;
      }
   }


   void paintLineArray(Graphics g, CalView vw, int top, int height, int yOffset, int start, int end) {

      int h, m, n, u, w, x, y, y2, y3, z, first, bottom;
      int[][] elm   = vw.elements;      
      int[][] lines = vw.lines;
      bottom = top + height;
      first  = getPaintStart(lines, top - yOffset, bottom - yOffset, start, end);
      if (first == -1) {    //no lines fall within paint area
         return;
      }
      for (int i=first; i<end; i++) {
         x  = lines[L_X][i];
         y  = lines[L_Y][i] + yOffset;      //the baseline
         y2 = y  + lines[L_D][i];           //the bottom of the line
         y3 = y2 - lines[L_H][i];           //the top of the line
         if (y3 > bottom) {
            break;       //rest of lines fall below paint area
         } else if (y2 >= top) {
            for (int j=lines[L_ES][i]; j<lines[L_EE][i]; j++) {
               if ((elm[E_ATTR][j] & 8) > 0) {   //special bit flag set
                  n = (elm[E_ITEM][j] & 31);
                  if (n == I_TABLE) {
                     CalTableView tView = vw.tableViews[elm[E_ITEM][j] >> 5];
                     tView.paintTable(this, vw, g, top, height, y3);
                     i = tView.lastLineIndex;
                     j = tView.lastElementIndex;
                  } else if (n == I_UL) {    //unordered list
                     drawListBullet(g, x, y, (elm[E_ITEM][j] >> 5),
                                elm[E_COLOR][j] == 0 ? doc.textcolor : elm[E_COLOR][j]);
                  } else if (n == I_NL) {    //list item on its own
                     drawListBullet(g, x + 16, y, (elm[E_ITEM][j] >> 5),
                                elm[E_COLOR][j] == 0 ? doc.textcolor : elm[E_COLOR][j]);
                     x += elm[E_WIDTH][j];
                  } else if (n == I_OL) {    //ordered list
                     drawText(g, vw.listArray, elm, j, elm[E_ITEM][j] >> 5, y2, y);
                  } else if (n == I_HR) {
                     n = y3 + 7;
                     m = n + lines[L_D][i];  //descent value is height of the rule - 1
                     if ((elm[E_ITEM][j] >> 5) > 0) {   //noshade option is set
                        g.setColor(((elm[E_ITEM][j] >> 5) == 1) ?
                               CalColor.colors[CalColor.getShadow(elm[E_COLOR][j])] :
                                       CalColor.colors[elm[E_COLOR][j]]);
                        for (int k=n; k<=m; k++) {
                           g.drawLine(x, k, x + elm[E_WIDTH][j], k);
                        }
                     } else {
                        Color c = CalColor.colors[CalColor.getShadow(elm[E_COLOR][j])];
                        g.setColor(c);
                        g.drawLine(x, n, x + elm[E_WIDTH][j], n);
                        if (m > n) {
                           g.setColor(CalColor.colors[CalColor.getHighlight(elm[E_COLOR][j])]);
                           g.drawLine(x, m, x + elm[E_WIDTH][j], m);
                           if (m > n + 1) {
                              g.drawLine(x + elm[E_WIDTH][j], n, x + elm[E_WIDTH][j], m);
                              g.setColor(c);
                              g.drawLine(x, n, x, m);
                           }
                        } 
                     }
                     break;
                  } else if (n == I_IFRAME) {
                     if ((elm[E_FONT][j]) > -1) {   //rem font isn't really a font, it's align of IFRAME
                        n = (elm[E_ITEM][j] >> 5);
                        try {
                           CalViewer v = vw.iFrames[n];
                           m = (elm[E_ATTR][j] >> 10);
                           z = getObjectY(lines[L_D][i], lines[L_A][i], lines[L_H][i],
                                                                           elm[E_FONT][j], y, y2, y3, m);
                           w = vw.iFrames[n].hspace;
                           h = vw.iFrames[n].vspace;
                           v.sp.setBounds(x + w, z + h, elm[E_WIDTH][j] - (w << 1), m - (h << 1));
                           v.sp.setVisible(true);
                           elm[E_FONT][j] = -1;
                        } catch (ArrayIndexOutOfBoundsException e3) {
                           //System.err.println("Component not found");
                        }
                     }
                     x += elm[E_WIDTH][j];
                  } else if (n == I_FORM) {
                     if ((elm[E_FONT][j]) > -1) {   //rem font isn't really a font, it's align of FORM
                        try {
                           n = (elm[E_ITEM][j] >> 5);
                           CalForm form = vw.forms[n];
                           m = (elm[E_ATTR][j] >> 10);
                           if (elm[E_FONT][j] == V_FORM) {
                              z = y + 3 - m;   //form is positioned just below baseline
                           } else {
                              z = getObjectY(lines[L_D][i], lines[L_A][i], lines[L_H][i],
                                                                           elm[E_FONT][j], y, y2, y3, m);
                           }
                           form.setBounds(x, z, elm[E_WIDTH][j], m);
                           form.setVisible(true);
                           elm[E_FONT][j] = -1;
                        } catch (ArrayIndexOutOfBoundsException e3) {
                           //System.err.println("Component not found");
                        }
                     }
                     x += elm[E_WIDTH][j];
                  } else if (n == I_IMG) {
                     CalImage tagim = (CalImage)doc.objectVector.elementAt(elm[E_ITEM][j] >> 5);
                     //if (tagim.relineFlag) {
                     //   this.relineFlag = true;
                     //   tagim.relineFlag = false;
                     //   imagesLoaded = false;
                     //}
                     //work out the y co-ord to start painting image at
                     m = elm[E_ATTR][j] >> 10;   //m is the lined height of the image
                     z = getObjectY(lines[L_D][i], lines[L_A][i], lines[L_H][i], tagim.align, y, y2, y3, m);
                     //now adjust for vspacing. u = new x co-ord to paint
                     u = x;
                     w = elm[E_WIDTH][j];
                     if (tagim.hspace > 0) {
                        u += tagim.hspace;
                        w -= (tagim.hspace << 1);
                     }
                     z += tagim.vspace;
                     h = m - (tagim.vspace << 1);
                     //draw the border if there is one
                     if ((tagim.border > 0) || ((tagim.border == 0) && (elm[E_LINKH][j] != 0))) {
                        if ((elm[E_LINKH][j] == 0) || (pref.hyperlinkHover &&
                                    (elm[E_LINKH][j] != focusedTagNo))) {
                           g.setColor(CalColor.colors[doc.textcolor]);
                        } else if ((activeTabLink > 0) && (elm[E_LINKH][j] == activeTabLink) &&
                                                                       (tagim.usemapHash == 0)) {
                           g.setColor(CalColor.colors[pref.linkTabBackground]);
                        } else if ((elm[E_LINKH][j] == activeLinkHash) && (tagim.usemapHash == 0)) {
                           g.setColor(CalColor.colors[doc.alinkcolor]);
                        } else if ((tagim.usemapHash == 0) &&
                                                       doc.attrVals[doc.tags[elm[E_LINKH][j] + 2]] == -1) {
                           g.setColor(CalColor.colors[doc.vlinkcolor]);
                        } else {
                           g.setColor(CalColor.colors[doc.linkcolor]);
                        }
                        if ((tagim.border == 0) && (elm[E_LINKH][j] != 0)) {
                           for (int k=0; k<2; k++) {
                              g.drawRect(u++, z++, w-1, h-1);
                              w -= 2;
                              h -= 2;
                           }
                        } else {
                           for (int k=0; k<tagim.border; k++) {
                              g.drawRect(u++, z++, w-1, h-1);
                              w -= 2;
                              h -= 2;
                           }
                        }
                     }
                     if (((!pref.loadImages) && (tagim.jname == null)) || (tagim.status == IMG_BROKEN) ||
                        (tagim.status == IMG_WAITING) || (tagim.im == null)) {
                        if ((pref.loadImages) && (tagim.status == IMG_WAITING)) {
                           imagesLoaded = false;
                        }
                        if ((tagim.border > 0) || (elm[E_LINKH][j] != 0)) {
                           if ((elm[E_LINKH][j] == 0) || (pref.hyperlinkHover &&
                                             (elm[E_LINKH][j] != focusedTagNo))) {
                              g.setColor(CalColor.colors[doc.textcolor]);
                           } else {
                              if ((elm[E_LINKH][j] == activeLinkHash) && (tagim.usemapHash == 0)) {
                                 g.setColor(CalColor.colors[doc.alinkcolor]);
                              } else if ((activeTabLink > 0) && (elm[E_LINKH][j] == activeTabLink) &&
                                                                       (tagim.usemapHash == 0)) {
                                 g.setColor(CalColor.colors[pref.linkTabBackground]);
                              } else if ((tagim.usemapHash == 0) &&
                                                       doc.attrVals[doc.tags[elm[E_LINKH][j] + 2]] == -1) {
                                 g.setColor(CalColor.colors[doc.vlinkcolor]);
                              } else {
                                 g.setColor(CalColor.colors[doc.linkcolor]);
                              }
                           }
                           g.drawRect(u, z, w-1, h-1);
                        } else {
                           g.setColor(CalColor.colors[CalColor.getShadow(elm[E_COLOR][j])]);
                           g.drawLine(u, z, u + w - 1, z);
                           g.drawLine(u, z, u, z + h - 1);
                           g.setColor(CalColor.colors[CalColor.getHighlight(elm[E_COLOR][j])]);
                           g.drawLine(u, z + h - 1, u + w - 1, z + h - 1);
                           g.drawLine(u + w - 1, z, u + w - 1, z + h - 1);
                        }
                        drawImageIcon(tagim, g, u, z , w, h);
                        if (tagim.altText != null) {
                           u += 28;
                           z = (h >= 22) ? z + 8 + f.ALTBASE : z + f.ALTBASE +
                                                                     ((h - f.ALTHEIGHT) >> 1);
                           g.setFont(f.fonts[HELV][NORM][1]);
                           if (elm[E_LINKH][j] == 0) {
                              g.setColor(CalColor.colors[doc.textcolor]);
                           } else if ((tagim.usemapHash == 0) &&
                                                       doc.attrVals[doc.tags[elm[E_LINKH][j] + 2]] == -1) {
                              g.setColor(CalColor.colors[doc.vlinkcolor]);
                           } else {
                              g.setColor(CalColor.colors[doc.linkcolor]);
                           }
                           if (tagim.altLines == null) {
                              g.drawString(tagim.altText, u, z);
                           } else {
                              for (int k=0; k<tagim.altLines.length; k++) {
                                 if (tagim.altLines[k] != null) {
                                    g.drawString(tagim.altLines[k], u, z);
                                    z += f.ALTHEIGHT;
                                 }
                              }
                           }
                        }
                     } else {
                        tagim.active  = true;
                        if (tagim.status != IMG_LOADED) {
                           imagesLoaded = false;
                        } 
                        if (!g.drawImage(tagim.im, u, z, w, h, tagim)) {
                           imagesLoaded = false;
                        }
                        //if (tagim.relineFlag) {
                        //   this.relineFlag = true;
                        //   tagim.relineFlag = false;
                        //   imagesLoaded = false;
                        //}
                     }
                     x += elm[E_WIDTH][j];
                  }               
               } else if (elm[E_CHARCOUNT][j] > 0) {
                  drawText(g, doc.charArray, elm, j, x, y2, y);
                  x += elm[E_WIDTH][j];
               } else if ((elm[E_ATTR][j] & 4) > 0) {
                  drawText(g, doc.charArray, elm, j, x, y2, y);
                  x += elm[E_WIDTH][j];
               } else {
                  x += elm[E_WIDTH][j];
               }
            }
         }
      }
   }


   private void wallpaperImage(Graphics g, CalImage tagim, int x, int y, int w, int h) {
   
      int imWidth  = Math.max(tagim.width, 1);   //avoids poss div by 0
      int imHeight = Math.max(tagim.height, 1);
      int paintX = (x / imWidth)  * imWidth;
      int paintY = (y / imHeight) * imHeight;
      int startX = paintX;
      int endX   = x + w;
      int endY   = y + h;
      
      while (paintY <= endY) {
         paintX = startX;
         while (paintX <= endX) {
            g.drawImage(tagim.im, paintX, paintY, tagim);
            paintX += imWidth;
         }
         paintY += imHeight;
      }
   }
   
   
   private void drawText(Graphics g, char[] charArray, int[][] elm, int j, int x, int bottom, int baseline) {
         
      int n = 0;
      int family   = (elm[E_FONT][j] >> 8);
      int override = (elm[E_FONT][j] >> 7) & 1;
      int style    = (elm[E_FONT][j] >> 4) & 7;
      int size     = (elm[E_FONT][j] & 15);
      boolean strike = false;

      boolean link   = false;
      if (elm[E_LINKH][j] != 0) {
         if (pref.hyperlinkHover) {
            link = (elm[E_LINKH][j] == focusedTagNo);
         } else {
            link = true;
         }
      }

      if (link) {
         if (elm[E_LINKH][j] == activeLinkHash) {
            g.setColor(CalColor.colors[doc.alinkcolor]);
         } else if (override == 1) {
            g.setColor(CalColor.colors[elm[E_COLOR][j]]);
         } else if (doc.attrVals[doc.tags[elm[E_LINKH][j] + 2]] == -1) {
            g.setColor(CalColor.colors[doc.vlinkcolor]);
         } else {
            g.setColor(CalColor.colors[doc.linkcolor]);
         }
      } else {
         g.setColor(elm[E_COLOR][j]== 0 ? CalColor.colors[doc.textcolor] : CalColor.colors[elm[E_COLOR][j]]);
      }

      if ((elm[E_ATTR][j] & 51) > 0) {
         n = f.fm[family][style][size].getMaxDescent();
         if ((elm[E_ATTR][j] & 1) > 0) {   //SUB
            baseline += n;
         } else if ((elm[E_ATTR][j] & 2) > 0) {  //SUP
            baseline -= (n + 2);
         }
         if ((elm[E_ATTR][j] & 32) > 0) {
            link = true;   //fools code below into underlining as a link, but in fontcolor
         }
         if ((elm[E_ATTR][j] & 16) > 0) {          //strike
            strike = true;
         }
      }

      if ((activeTabLink > 0) && (elm[E_LINKH][j] == activeTabLink)) {
         int h = f.fm[family][style][size].getHeight();
         int y = baseline + f.fm[family][style][size].getMaxDescent() - h;
         g.setColor(CalColor.colors[pref.linkTabBackground]);
         g.fillRect(x, y, elm[E_WIDTH][j], h + 1);
         g.setColor(CalColor.colors[pref.linkTabForeground]);
      }
      
      if (strike) {
         n = baseline - n;
         g.drawLine(x, n, x + elm[E_WIDTH][j] - 1, n);
      }

      if (elm[E_CHARCOUNT][j] > 0) {
         g.setFont(f.fonts[family][style][size]);
         g.drawChars(charArray, elm[E_CHARSTART][j], elm[E_CHARCOUNT][j], x, baseline);
      }
      if ((link) && (pref.underlineLinks)) {
         n = Math.min(baseline + Math.max(2, size - 6), bottom);
         int z = x + elm[E_WIDTH][j];
         g.drawLine(x, n, z-1, n);
      }
   }
   

   private void drawListBullet(Graphics g, int x, int y, int style, int c) {
   
      g.setColor(CalColor.colors[c]);
      if (style == V_DISC) {
         g.drawLine(x-14, y-6, x-12, y-6);
         g.fillRect(x-15, y-5, 5, 3);
         g.drawLine(x-14, y-2, x-12, y-2);
      } else if (style == V_CIRCLE) {
         g.drawLine(x-14, y-7, x-12, y-7);
         g.drawLine(x-16, y-5, x-15, y-6);
         g.drawLine(x-16, y-4, x-16, y-4);
         g.drawLine(x-16, y-3, x-15, y-2); 
         g.drawLine(x-14, y-1, x-12, y-1);
         g.drawLine(x-11, y-2, x-10, y-3);
         g.drawLine(x-10, y-4, x-10, y-4);
         g.drawLine(x-10, y-5, x-11, y-6);
      } else {
         g.fillRect(x-15, y-6, 5, 5);
      }
   }
   

   private void drawImageIcon(CalImage tagim, Graphics g, int x, int y, int w, int h) {
   
      if ((w < 23) || (h < 22)) {
         return;
      }
      x += 6;
      y += 6;
      g.translate(x, y);
      g.setColor(CalColor.imgLight);
      g.drawLine(0, 0, 15, 0);
      g.drawLine(0, 0, 0, 13);
      g.setColor(CalColor.imgDark);
      g.drawLine(0, 14, 15, 14);
      g.drawLine(15, 0, 15, 13);
      if (tagim.status == IMG_WAITING) {          //|| tagim.animated) {
         g.setColor(Color.black);
         g.drawRect(8, 2, 5, 5);
         g.drawRect(2, 7, 5, 5);
         g.setColor(CalColor.imgGreen);
         g.fillRect(9, 3, 4, 4);
         g.setColor(Color.red);
         g.fillRect(3, 8, 4, 4); 
      } else {
         g.setColor(Color.red);
         g.drawLine(5, 5, 6, 5);
         g.drawLine(9, 5, 10, 5);
         g.drawLine(6, 6, 9, 6);
         g.drawLine(7, 7, 8, 7);
         g.drawLine(6, 8, 9, 8);
         g.drawLine(5, 9, 6, 9);
         g.drawLine(9, 9, 10, 9);
      }
      g.translate(-x, -y);
   }
   
   
   private int getObjectY(int descent, int textascent, int height, int align, int y, int y2, int y3, int m) {

      int z = 0;
      
      switch (align) {
         case V_LEFT     :
         case V_RIGHT    :
         case V_ABSTOP   : z = y3; break;
         case V_TOP      : if (descent == 0) {
                              z = y3;
                           } else {
                              z = y - textascent; break;
                           }
         case V_MIDDLE   : if (descent == 0) {   //object only, no text
                              z = y3 + ((height - m) >> 1);
                           } else {
                              z = y - (m >> 1);
                           }
                           break;
         case V_ABSBOTTOM: z = y2 - m; break;
         case V_ABSMIDDLE: z = y3 + ((height - m) >> 1); break;
         case V_BOTTOM   :
                DEFAULT  : z = y - m; break;
      }
      if (z < y3) {        //override check here in case of bug
         z = y3;
      } else if (z + m > y2) {
         z = y2 - m;
      }
      
      return z;
   }


   private int getPaintStart(int[][] lines, int top, int bottom, int start, int end) {
   
      int y, y2;
      
      for (int i=start; i<end; i++) {
         y = lines[L_Y][i] + lines[L_D][i];
         y2 = y - lines[L_H][i];
         if (y2 > bottom) {
            break;    //all lines are below paint area
         } else {
            if (y >= top) {
               return i;         //found the start line
            }
         }
      }
      
      return -1;     //no line found
   }


   boolean getElementAtPoint(int x, int y, int index, int end) {

      int n, x2, x3, y2, y3, z, family, style, size, es, ee;
      
      while (index < end) {
         y2 = view.lines[L_Y][index] + view.lines[L_D][index];
         y3 = y2 - view.lines[L_H][index];
         if (y3 > y) {
            return false;
         }
         if ((y >= y3) && (y < y2)) {
            es = view.lines[L_ES][index];
            ee = view.lines[L_EE][index];
            x2 = view.lines[L_X][index];
            for (int i=es; i<ee; i++) {
               x3 = x2 + view.elements[E_WIDTH][i];
               if ((view.elements[E_ATTR][i] & 8) > 0) {  //special bit flag set
                  n = (view.elements[E_ITEM][i] & 31);
                  if (n == I_OL) {    //ordered list element
                     z = (view.elements[E_ITEM][i] >> 5);
                     if ((x >= z) && (x < z + view.elements[E_WIDTH][i])) {
                        utilElement.setState(view.elements, i);
                        return true;
                     }
                  } else if (n == I_TABLE) {
                     CalTableView tView = view.tableViews[view.elements[E_ITEM][i] >> 5];
                     if ((x >= x2) && (x < x3)) {
                        return tView.getElementAtPoint(x, y - y3, this);
                     } else {
                        index = tView.lastLineIndex;
                        break;
                     }
                  } else if (n == I_IMG) {
                     if ((x >= x2) && (x < x3)) {
                        //the right element but we still need to check if we're within its bounds
                        CalImage tagim = (CalImage)doc.objectVector.elementAt(
                                                                         view.elements[E_ITEM][i] >> 5);
                        int m = (view.elements[E_ATTR][i] >> 10);   //m is lined height of image
                        z = getObjectY(view.lines[L_D][index], view.lines[L_A][index],
                            view.lines[L_H][index], tagim.align, view.lines[L_Y][index], y2, y3, m);
                        if ((y >= z) && (y < z + m)) {
                           view.focusElementX = x2 + tagim.hspace;
                           view.focusElementY = z  + tagim.vspace;
                           if (tagim.border > 0) {
                              view.focusElementX += tagim.border;
                              view.focusElementY += tagim.border;
                           } else if (view.elements[E_LINKH][i] != 0) {
                              view.focusElementX += 2;
                              view.focusElementY += 2;
                           }
                           view.focusElementX = x - view.focusElementX;
                           view.focusElementY = y - view.focusElementY;
                           utilElement.setState(view.elements, i);
                           if (tagim.formTagPos >= 0) {
                              //ANDY: This is a bit dangerous. It changes item type from
                              //I_IMG to I_FORM since the former=5 and latter=7. If we ever
                              //change these codes then this may not work. Beware...
                              utilElement.item = (utilElement.item | 7);
                           }
                           return true;
                        } else {
                           return false;   //can't be any other element
                        }
                     }
                  }
               } else {
                  if ((x >= x2) && (x < x3)) {
                     //we know we've got the right element, but we still need to determine whether
                     //we are within the bounds of this bit of text
                     if ((view.elements[E_CHARCOUNT][i] > 0) || ((view.elements[E_ATTR][i] & 4) > 0)) {
                        family = (view.elements[E_FONT][i] >> 8);
                        style  = (view.elements[E_FONT][i] >> 4) & 7;
                        size   = (view.elements[E_FONT][i] & 15);
                        n = f.fm[family][style][size].getMaxDescent();
                        y2 = view.lines[L_Y][index] + n;
                        y3 = y2 - f.fm[family][style][size].getHeight();
                        if ((view.elements[E_ATTR][i] & 1) > 0) {     //SUB
                           y2 += n;
                           y3 += n;
                        } else if ((view.elements[E_ATTR][i] & 2) > 0) {   //SUP
                           y2 -= (n + 2);
                           y3 -= (n + 2);
                        }
                        if ((y >= y3) && (y <= y2)) {
                           utilElement.setState(view.elements, i);
                           return true;
                        } else {
                           return false;    //we can return 'cos it can't be any other element
                        }
                     } else {
                        utilElement.setState(view.elements, i);
                        return true;
                     }
                  }
               }
               x2 = x3;
            }
         }
         index++;
      }

      return false;
   }
   

   boolean goToLink(int hashValue) {
   
      if (view == null) {
         return false;
      }
      Rectangle r = view.getElementRect(hashValue, E_NAMEH);
      if (r != null) {
         ensureRectangleVisible(r, false);
         return true;
      }
      return false;
   }
   
   
   private class LinkListener extends MouseAdapter {
   
      public void mousePressed(MouseEvent e) {

         int b, n, x, y;
         
         if ((doc == null) || (view == null) || (view.lines == null) || (view.paintableLines <= 0)) {
            return;
         }
         
         x = e.getPoint().x;
         y = e.getPoint().y;
         if (getElementAtPoint(x, y - marginheight, 0, view.paintableLines)) {
            if (utilElement.linkHash != 0) {
               activeLinkHash = utilElement.linkHash;
            }
         }
         repaint();
      }

      public void mouseReleased(MouseEvent e) {
      
         URL url2;
         int b, n, x, y, tag, tagPos, link;
         Point p;
         String s, targetFrame, jName;
      
         if ((doc == null) || (view == null) || (view.lines == null) || (view.paintableLines <= 0)) {
            return;
         }
         
         s = null;
         targetFrame = null;
         p = e.getPoint();
         x = p.x;
         y = p.y;
         p = viewport.getViewPosition();
         b = p.y + viewport.getExtentSize().height;

         if ((pref.focusOnClick) && (!hasFocus())) {
            currTabIndex = -1;
            requestFocus();
         }

         if (!(getElementAtPoint(x, y - marginheight, 0, view.paintableLines))) {
            if (activeLinkHash != 0) {
               activeLinkHash = 0;
               repaint();
            }  
         } else {
            if ((utilElement.item & 31) == I_FORM) {
               //then it must be a <INPUT type=image>
               try {
                  CalImage tagim = (CalImage)doc.objectVector.elementAt(utilElement.item >> 5);
                  for (int i=0; i<view.formIndex; i++) {
                     if ((view.forms[i] != null) && (view.forms[i].fItem.tagPos ==
                          tagim.formTagPos) && (view.forms[i].fItem.formNo > 0)) {
                        view.forms[i].clickX = view.focusElementX;
                        view.forms[i].clickY = view.focusElementY;
                        CalFormHandler fh = (CalFormHandler)doc.objectVector.elementAt(
                                                  doc.tags[view.forms[i].fItem.formNo + 1]);
                        fh.handleSubmission(view.forms[i], CalViewer.this);
                        break;
                     }
                  }
               } catch (Exception e4) {
                  //System.err.println("Exception in form image click");
               }
               return;
            }
            link = 0;
            tagPos = utilElement.linkHash;
            activeLinkHash = 0;
            if (tagPos > 0) {
               tag = doc.tags[tagPos];
               if (tag <= 0) {
                  repaint();    //...to clear any active linkhash
                  return;
               }
               if (tag == IMG) {
                  CalImage img = (CalImage)doc.objectVector.elementAt(doc.tags[tagPos + 1]);
                  if (img.usemapHash != 0) {
                     if ((img.width >0) && (img.height > 0)) {
                        s = getImageMapLink(img.usemapHash, view.focusElementX,
                                                             view.focusElementY, img.width, img.height);
                        if (s != null) {
                           targetFrame = cCurrentTarget;
                           if ((targetFrame == null) && (doc.baseTarget != null)) {
                              targetFrame = doc.baseTarget;
                           }
                        }
                     }
                     if (s == null) {
                        repaint();    //...to clear any active linkhash
                        return;
                     }
                  }
               }
               jName = null;
               if ((s == null) && (tag == A)) {
                  link = doc.attrVals[doc.tags[tagPos + 2]];
                  s = getAttrString(A_HREF, tagPos + 3);
                  jName = getAttrString(A_JNAME, tagPos + 3);
                  targetFrame = getAttrString(A_TARGET, tagPos + 3);
               }
               if (s == null) {
                  return;
               } else {
                  if (followLink(s, targetFrame, jName, link)) {
                     checkCursor(x, y);
                     repaint();
                  }
               }
            }       
         }
      }      
   }


   boolean followLink(String s, String targetFrame, String jName, int link) {

      URL url2 = calculateURL(s);
      if (url2 == null) {
         return false;
      }
      if (targetFrame == null) {
         if (doc.baseTarget != null) {
            targetFrame = doc.baseTarget;
         } else {
            targetFrame = name;
         }
      } else {
         if (("_self").equalsIgnoreCase(targetFrame) || targetFrame.equals(name)) {
            targetFrame = name;
         } else if (("_parent").equalsIgnoreCase(targetFrame)) {
            targetFrame = getTrueParentName();
         }
      }
      pane.linkActivatedUpdate(url2, targetFrame, jName);
      if ((jName == null) && pref.followLinks) {
         pane.goToLink(new CalHistoryItem(url2, targetFrame, url2.getRef(), null, null), link);
      }
      return true;
   }


   URL calculateURL(String s) {

      URL url2 = null;
      try {
         if (s.startsWith("#") && (doc.url != null) && (("file").equals(doc.url.getProtocol()))) {
            s = doc.url.getFile() + s;
         }
         url2 = new URL(doc.url, s);
      } catch (MalformedURLException e3) {
         url2 = null;
      }
      return url2;
   }

   
   private class CursorListener extends MouseMotionAdapter {
   
      public void mouseMoved(MouseEvent e) {
      
         checkCursor(e.getX(), e.getY());
      }      
   }
   
   
   private void checkCursor(int x, int y) {
   
      int b;
      boolean found;
      int tag;
      URL url2;
      String s;
      
      if ((doc == null) || (view == null) || (view.lines == null) || (view.paintableLines <= 0)) {
         return;
      }
      b = viewport.getViewPosition().y + viewport.getExtentSize().height;
      found = getElementAtPoint(x, y - marginheight, 0, view.paintableLines);
      if (((!found) || (utilElement.linkHash == 0)) && (getCursor().getType() != Cursor.DEFAULT_CURSOR)) {
         focusedTagNo = 0;
         pane.linkFocusedUpdate(null);
         setCursor(Cursor.getDefaultCursor());
         if (pref.hyperlinkHover) {
            repaint();
         }
      } else if ((found) && (utilElement.linkHash != 0)) {
         tag = Math.abs(doc.tags[utilElement.linkHash]);
         if (tag == IMG) {
            CalImage img = (CalImage)doc.objectVector.elementAt(doc.tags[utilElement.linkHash + 1]);
            if (img.usemapHash != 0) {
               if ((img.width > 0) && (img.height > 0)) {
                  s = getImageMapLink(img.usemapHash, view.focusElementX,
                                                view.focusElementY, img.width, img.height);
                  if (s != null) {
                     if (getCursor().getType() == Cursor.DEFAULT_CURSOR) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                     }
                     if (focusedTagNo != utilElement.linkHash) {
                        focusedTagNo = utilElement.linkHash;
                        url2 = calculateURL(s);
                        pane.linkFocusedUpdate(url2);
                        if (pref.hyperlinkHover) {
                           repaint();
                        }
                     }
                     return;
                  }
               }
               if (getCursor().getType() != Cursor.DEFAULT_CURSOR) {
                  focusedTagNo = 0;
                  pane.linkFocusedUpdate(null);
                  setCursor(Cursor.getDefaultCursor());
                  if (pref.hyperlinkHover) {
                     repaint();
                  }
               }
               return;
            }
         }
         if (getCursor().getType() == Cursor.DEFAULT_CURSOR) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         }
         if (tag == A) {
            if (focusedTagNo != utilElement.linkHash) {
               focusedTagNo = utilElement.linkHash;
               url2 = calculateURL(getAttrString(A_HREF, utilElement.linkHash + 3));
               pane.linkFocusedUpdate(url2);
               if (pref.hyperlinkHover) {
                  repaint();
               }
            }
         }
      }
   }


   private String getImageMapLink(int maphash, int x, int y, int imW, int imH) {
   
      CalImageMap map;
      String s;
      
      cCurrentTarget = null;
      if ((map = CalHTMLManager.getImageMap(maphash)) != null) {
         s = map.getLink(x, y, imW, imH);
         if (s != null) {
            cCurrentTarget = map.cCurrentTarget;
            return s;
         }
      }

      return null;
   }


   int getLiningWidth() {
   
      if (frameType != IFRAME) {
         if (scrolling == V_NO) {
            return sp.getWidth() - hborder;
         } else {
            return sp.getWidth() - scrollbarwidth - hborder;
         }
      } else {
         int n = sp.getWidth();
         if (frameborder != 0) {
            n -= 6;
         }
         if (scrolling != V_NO) {
            return n - scrollbarwidth;
         } else {
            return n;
         }
      }

   }
   

   int getLiningHeight() {
   
     return sp.getHeight() - vborder;
   }


   void revertScrollBars() {

      if ((scrolling == VIEWER) || (scrolling == V_YES)) { 
         sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      } else if (scrolling == V_AUTO) {
         sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      } else {
         sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
      }
      if ((scrolling == VIEWER) || (scrolling == V_AUTO)) {
         sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      } else if (scrolling == V_YES) {
         sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      } else {
         sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      }
   }
   

   String getTrueParentName() {

      if (parent == null) {
         return this.name;
      } else if (parent.name.equals("%")) {
         return parent.getTrueParentName();
      } else {
         return parent.name;
      }
   }
   
   
   CalScrollPane getScrollPane() {
   
      return sp;
   }
   
   
   //returns -1 if no attribute found  
   int getAttr(int attrType, int tagIndex) {

      int n = doc.tags[tagIndex];
      if (n > 0) {      //if attrCount > 0
         int j = doc.tags[tagIndex + 1] + n;   //attrStart + attrCount
         for (int i=doc.tags[tagIndex + 1]; i<j; i++) {
            if (doc.attrTypes[i] == attrType) {
               return i;
            }
         }
      }
      return -1;
   }


   String getAttrString(int attrType, int tagIndex) {

      String s = null;
      int n = getAttr(attrType, tagIndex);
      if (n != -1) {
         s = doc.attrStrings[n];
      }
      return s;
   }

   
   public boolean isFocusTraversable() {
   
      return CalHTMLManager.enableCalFocusManager;
   }
   
   public boolean isManagingFocus() {
   
      return CalHTMLManager.enableCalFocusManager;
   }
   

   void tabBackForward(CalViewer caller, int offset, boolean newcycle) {
   
      int n;
      boolean done;
      boolean parentcaller = ((caller == null) || ((parent != null) && (caller == parent)));
      activeTabLink = 0;
      if (frameset != null) {
         if (parentcaller) {
            frameset.tabBackForward(this, offset, true);
         } else {
            frameset.tabBackForward(caller, offset, false);
         }
      } else if ((view != null) && (view.lineState == LINED)) {
         if (!parentcaller) {
            //then a child must have finished its tab cycle. Find the next child and get it to start
            //a new tab cycle
            if (view.iFrames != null) {
               n = -1;
               for (int i=0; i<view.iFrameIndex; i++) {
                  if (view.iFrames[i] == caller) {
                     n = i;
                     break;
                  }
               }
               if (n != -1) {
                  done = false;
                  if ((n == 0) && ((offset == -1) || (view.iFrameIndex == 1))) {
                     if ((offset == -1) && (view.tabIndex > 0)) {
                        requestFocus();
                        doInternalTab(-1, true);
                        return;
                     } else {
                        done = true;
                     }
                  } else if ((n == view.iFrameIndex - 1) && (offset == 1)) {
                     done = true;
                  }
                  if (done) {
                     //we've completed the tabcycle for this component
                     if (parent != null) {
                        parent.tabBackForward(this, offset, false);
                     } else if (pane != null) {
                        FocusManager.getCurrentManager().focusNextComponent(pane);
                        return;
                     }
                  } else {
                     n += offset;
                     if (view.iFrames[n] != null) {
                        if (view.iFrames[n].frameset != null) {
                           view.iFrames[n].tabBackForward(this, offset, true);
                        } else {
                           view.iFrames[n].currTabIndex = -1;
                           Rectangle r = view.getElementRect(n, I_IFRAME);
                           if (r != null) {
                              ensureRectangleVisible(r, true);
                           }
                           view.iFrames[n].requestFocus();
                           view.iFrames[n].paintFocus = true;
                           view.iFrames[n].repaint();
                        }
                        return;
                     }
                  }
               }
            }
         } else if (doInternalTab(offset, newcycle)) {
            return;
         } else {
            //if we reach here we've done the basic tabcycle for this viewer, so check any iFrames
            if ((offset == 1) && (view.iFrames != null)) {
               n = 0;
               while ((n < view.iFrameIndex) && (view.iFrames[n] == null)) {
                  n++;
               }
               if (n < view.iFrameIndex) {
                  if (view.iFrames[n].frameset != null) {
                     view.iFrames[n].tabBackForward(this, offset, true);
                  } else {
                     view.iFrames[n].currTabIndex = -1;
                     Rectangle r2 = view.getElementRect(n, I_IFRAME);
                     if (r2 != null) {
                        ensureRectangleVisible(r2, true);
                     }
                     view.iFrames[n].requestFocus();
                     view.iFrames[n].paintFocus = true;
                     view.iFrames[n].repaint();
                  }
                  return;
               }
            }
            if (("_dialog").equals(name)) {
               currTabIndex = -1;
               return;
            } else if (parent != null) {
               parent.tabBackForward(this, offset, false);
            } else {
               FocusManager.getCurrentManager().focusNextComponent(pane);
               return;
            }
         }
      } else {
         //no frameset, no view, so pass focus on
         if (parent != null) {
            parent.tabBackForward(this, offset, false);
         } else if (pane != null) {
            FocusManager.getCurrentManager().focusNextComponent(pane);
            return;
         }
      }
   }   
        

   private boolean doInternalTab(int offset, boolean newcycle) {

      int n;
      if ((view.tabArray != null) && (view.tabIndex > 0)) {
         if (newcycle) {
            currTabIndex = (offset == 1) ? -1 : view.tabIndex;
         }
         while (true) {
            currTabIndex += offset;
            if ((currTabIndex >= 0) && (currTabIndex < view.tabIndex)) {
               n = view.tabArray[currTabIndex];
               if (n >= 0) {
                  Rectangle r = view.getElementRect(n, E_LINKH);
                  if (r != null) {
                     if (focusedTagNo != n) {
                        focusedTagNo = n;
                        try {
                           URL url2 = calculateURL(getAttrString(A_HREF, n + 3));
                           pane.linkFocusedUpdate(url2);
                        } catch (Exception e) {
                           //a harmless precaution
                        }
                     }
                     activeTabLink = n;
                     ensureRectangleVisible(r, true);
                     repaint();
                     return true;
                  }
               } else {
                  if (focusedTagNo > 0) {
                     focusedTagNo = 0;
                     pane.linkFocusedUpdate(null);
                  }
                  n = Math.abs(n) - 1;
                  if ((view.forms != null) && ((n >= 0) && (n < view.formIndex))) {
                     JComponent c = view.getFocusableComponent(view.forms[n]);
                     if (c != null) {
                        Rectangle r2 = view.getElementRect(n, I_FORM);
                        if (r2 != null) {
                           ensureRectangleVisible(r2, true);
                           c.requestFocus();
                           repaint();
                           return true;
                        }
                     }
                  }
               }
            } else {
               break;
            }
         }
      }
      return false;
   }


   private void ensureRectangleVisible(Rectangle r, boolean tabbing) {

      if (r == null) {
         return;
      }
      int n, xPos, yPos;
      int newX = 0;
      int newY = 0;
      r.y += marginheight;
      Rectangle r2 = viewport.getViewRect();
      if (r.x < r2.x) {
         xPos = -1;
      } else if ((r.x + r.width) > (r2.x + r2.width)) {
         xPos = 1;
      } else {
         xPos = 0;
      }
      if (r.y < r2.y) {
         yPos = -1;
      } else if ((r.y + r.height) > (r2.y + r2.height)) {
         yPos = 1;
      } else {
         yPos = 0;
      }

      if (tabbing && (xPos == 0) && (yPos == 0)) {
         return;     //rectangle is completely within viewport
      }
      
      if (!tabbing) {
         if (xPos == 0) {
            newX = r2.x;
         } else {
            newX = r.x - marginwidth;
         }
         newY = r.y;
      } else {
         if (xPos == -1) {
            newX = r.x - marginwidth;
         } else if (xPos == 1) {
            n = (r.x + r.width) - (r2.x + r2.width); 
            newX = Math.min(r.x - marginwidth, r2.x + n + 3);
         } else {
            newX = r2.x;
         }
         if (yPos == -1) {
            newY = r.y;
         } else if (yPos == 1) {
            n = (r.y + r.height) - (r2.y + r2.height);
            newY = Math.min(r.y, r2.y + n + 3);
         } else {
            newY = r2.y;
         }
      }
      viewport.setDocViewPosition(new Point(newX, newY));
   }
   
   public void focusGained(FocusEvent e) {   
   }
   
   public void focusLost(FocusEvent e) {
   
      if (focusedTagNo > 0) {
         focusedTagNo = 0;
         pane.linkFocusedUpdate(null);
      }
      activeTabLink = 0;
      paintFocus    = false;
      repaint();
   }


   private void stopAnimated() {

      if ((doc != null) && (doc.hasAnimated)) {

         CalImage tagim;
         for (int i=doc.objectVector.size()-1; i>=0; i--) {
            if (doc.objectVector.elementAt(i) instanceof CalImage) {
               tagim = (CalImage)doc.objectVector.elementAt(i);
               tagim.active = false;
            }
         }
      }
   }


   void releaseMemory() {

      stopAnimated();
      pane     = null;
      view     = null;
      doc      = null;
      sp       = null;
      viewport = null;
      f        = null;
      pref     = null;
      frameset = null;
      parser   = null;
      parent   = null;
      if (timer.isRunning()) {
         timer.stop();
      }
      timer    = null;
   }

}
