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
import java.net.*;
import java.util.Hashtable;


/**
 * A component class which has the capacity to display HTML-formatted content.
 * The default rendering style of the Pane and other aspects of its operation
 * are controlled by a <code>CalHTMLPreferences</code> object. The Pane
 * communicates internal events (e.g. a selected hyperlink) to a
 * <code>CalHTMLObserver</code> object so that futher processing of such events
 * can be handled by the programmer.
 * <P>Documents are displayed in the <code>JLayeredPane.DEFAULT_LAYER</code>.<BR>
 * The Pane's dialog (if used) is displayed in the <code>JLayeredPane.MODAL_LAYER</code>.<BR>
 * The Pane's test navigation bar (if used) is displayed in the <code>JLayeredPane.PALETTE_LAYER</code>.
 * <P>See the README.TXT file accompanying this document for further details on how to use
 * this class.
 * @see     calpa.html.CalHTMLPreferences
 * @see     calpa.html.CalHTMLObserver
 */
public class CalHTMLPane extends JLayeredPane implements CalCons, KeyListener {

   CalHTMLPreferences pref;
   CalHistoryManager  hist   = new CalHistoryManager();
   CalHTMLObserver    observer;
   CalViewer          viewer;
   CalDialog          dialog;
   CalNavBar          navbar;
   CalHistoryItem     waitingPostHistory;
   String             waitingPostData;
   boolean            navbarExists;
   boolean            syncLock;

   /**
   * Constructs a <code>CalHTMLPane</code> with default <code>CalHTMLPreferences</code>,
   * a default <code>CalHTMLObserver</code> and default top-level frame name.
   */    
   public CalHTMLPane() {
   
      this(null, null, null, false);
   }


   /**
   * Constucts a <code>CalHTMLPane</code> with the specified <code>CalHTMLObserver</code>,
   * <code>CalHTMLPreferences</code> and top-level frame name. If any of the arguments are
   * <code>null</code> then a default will be used.
   * @param  prefs the <code>CalHTMLPreferences</code> which determines the Pane's behaviour.
   * @param  obs the <code>CalHTMLObserver</code> that will receive updates from the Pane.
   * @param  name the name of the Pane's top-level frame.
   */    
   public CalHTMLPane(CalHTMLPreferences prefs, CalHTMLObserver obs, String name) {
   
      this(prefs, obs, name, false);
   }


   CalHTMLPane(CalHTMLPreferences p, CalHTMLObserver obs, String name, boolean dummy) { //package only

      if (p == null) {
         pref = new CalHTMLPreferences();
      } else {
         pref = p;
      }
      pref.allowChanges = false;
      if (obs == null) {
         observer = new DefaultCalHTMLObserver();
      } else {
         observer = obs;
      }
      viewer = new CalViewer(this, pref, hist, null, 0);
      if (name == null) {
         viewer.name = "*calpa*";
      } else {
         viewer.name = name;
      }
      add(viewer.sp, JLayeredPane.DEFAULT_LAYER);
      hist.setViewer(viewer);
      if (CalHTMLManager.enableCalFocusManager) {
         FocusManager fm = FocusManager.getCurrentManager();
         if ((fm == null) || (!(fm instanceof CalFocusManager))) {
            FocusManager.setCurrentManager(new CalFocusManager());
         }
         addKeyListener(this);
         addFocusListener(new PFL());
      }
      dialog = new CalDialog(this, pref);
      dialog.sp.setVisible(false);
      add(dialog.sp, JLayeredPane.MODAL_LAYER);
      if (pref.showNavBar) {
         navbar = new CalNavBar(this, pref);
         add(navbar.menuPanel, JLayeredPane.DEFAULT_LAYER);
         navbarExists = true;
      } else {
         navbarExists = false;
      }
   }


   /**
   * Public due to inheritance. Call <code>super.setBounds(x, y, w, h)</code> if you override
   * this method or the Pane may not function correctly.
   */    
   public void setBounds(int x, int y, int w, int h) {

      super.setBounds(x, y, w, h);
      if (navbarExists) {
         navbar.menuPanel.setBounds(0, h - navbar.mpHeight, w, navbar.mpHeight);
         viewer.sp.setBounds(0, 0, w, h - navbar.mpHeight);
      } else {
         viewer.sp.setBounds(0, 0, w, h);
      }
      if (dialog.sp.isVisible()) {
         Dimension d = dialog.sp.getSize();
         setDialogBounds(w, h, dialog.messageX, dialog.messageY, d.width, d.height);
      }
   }


   /**
   * Shows the contents of the specified <code>URL</code> in the top level frame of
   * the Pane. A cached document will be used if one is available and caching is enabled. 
   * @param  url the URL of the document to be displayed.
   */    
   public void showHTMLDocument(URL url) {
   
      showHTMLDocument(url, viewer.name, false);
   }


   /**
   * Shows the contents of the specified <code>URL</code> in the named
   * target frame. If the target frame is null the document will be displayed in the top level
   * frame of the Pane. If the target frame is not a currently recognised frame:
   * <UL type=disc><LI> if <code>handleNewFrames</code> is enabled in the
   * <code>CalHTMLPreferences</code> controlling the Pane then a new <code>CalHTMLPane</code>
   * will be created to show the document.
   * <LI>if <code>handleNewFrames</code> is <EM>not</EM> enabled
   * then a <code>showNewFrameRequest</code> will be sent to the Pane's resident
   * <code>CalHTMLObserver</code>.
   * </UL>
   * <P>If <code>reload</code> is <code>false</code> a cached document will be used if one is
   * available and caching is enabled. Otherwise the document will be reloaded from the
   * specified URL.
   * @param  url   the URL of the document to be displayed.
   * @param  targetFrame the HTML frame that the document should be displayed in.
   * @param  reload if <code>true</code> forces the document to be reloaded even if a cached
   *         version is available.
   */
   public void showHTMLDocument(URL url, String targetFrame, boolean reload) {
   
      if (url != null) {
         if (targetFrame == null) {
            targetFrame = viewer.name;
         }
         showDocument(new CalHistoryItem(url, targetFrame, url.getRef(), null, null), null,
                                                                   reload, HISTORY_NEW, 0);
      }
   }


   /**
   * Formats and displays the specified String as an HTML document in the top level frame of the Pane.
   * @param  s a String to be formatted as an HTML document.
   */
   public void showHTMLDocument(String s) {
   
      showHTMLDocument(s, viewer.name);
   }


   /**
   * Formats and displays the specified String as an HTML document in the named
   * target frame. If the target frame is null the document will be displayed in the top level
   * frame of the Pane. If the target frame is not a currently recognised frame:
   * <UL type=disc><LI>if <code>handleNewFrames</code> is enabled in the
   * <code>CalHTMLPreferences</code> controlling the Pane then a new <code>CalHTMLPane</code>
   * will be created to show the document.
   * <LI>if <code>handleNewFrames</code> is <EM>not</EM> enabled
   * then a <code>showNewFrameRequest</code> will be sent to the Pane's resident
   * <code>CalHTMLObserver</code>.
   * </UL>
   * @param  s a String to be formatted as an HTML document.
   * @param  targetFrame the HTML frame that the document should be displayed in.
   */
   public void showHTMLDocument(String s, String targetFrame) {
   
      if (s != null) {
         if (targetFrame == null) {
            targetFrame = viewer.name;
         }
         showDocument(new CalHistoryItem(null, targetFrame, null, null, null), s, false, HISTORY_NEW, 0);
      }
   }


   //all the public showHTMLDocument methods feed into this one
   void showDocument(CalHistoryItem h, String s, boolean reload, int histState, int link) {
   
      if (h.targetFrame != null) {
         if (h.targetFrame.startsWith("_")) {
            if (("_blank").equalsIgnoreCase(h.targetFrame)) {
               if (pref.handleNewFrames) {
                  h.targetFrame = null;
                  showNewFrame(h, s);
               } else {
                  observer.showNewFrameRequest(this, null, h.docURL);
               }
               return;
            } else if (("_top").equalsIgnoreCase(h.targetFrame)) {
               h.targetFrame = viewer.name;
            } else if (("_dialog").equalsIgnoreCase(h.targetFrame)) {
               h.targetFrame = viewer.name;
               closeDialog();
            } else {
               return;            //illegal (_self and _parent already tested)
            }
         }
         if (histState == HISTORY_NEW) {
            saveHistory(h.targetFrame);
         }
         if (viewer.showDocument(h, s, reload, histState, link) == NOT_FOUND) {
            if (pref.handleNewFrames) {
               showNewFrame(h, s);
            } else {
               observer.showNewFrameRequest(this, h.targetFrame, h.docURL);
            }
         }
      }
   }
   

   /**
   * Scrolls the document view to the specified anchor reference in the named target frame.
   * If the <code>targetFrame</code> argument is <code>null</code> then the reference will be
   * looked for in the document in the CalPane's top level frame.
   * <P>This method has been incorporated to assist programmers who are displaying HTML Strings
   * and who wish to navigate to internal links in their String HTML documents. For example, if
   * there is an anchor in your String such as: &lt;A name="contactinfo"&gt; then you can call
   * this method with <code>scrollToReference("contactinfo", null)</code>.
   * <P>Note that you will need to ensure that the String document has been parsed/loaded before
   * attempting to navigate to anchors within it. Calling <code>showHTMLDocument(String s)</code>
   * and then immediately calling this method may fail because the String is still being asynchronously
   * parsed and the reference has not been encountered. You can use the <code>CalHTMLObserver</code>
   * class to determine when the String has been fully parsed, or you could set the CalPane's
   * loading policy to synchronous loading.
   * <P>Programmers using URL documents need not use this method. They can create a new URL which
   * incorporates the anchor reference and call <code>showHTMLDocument(URL)</code> instead, which has
   * the advantage that no check need be made to see if the document has loaded - the CalPane will
   * automatically navigate to the anchor as soon as it is available.
   * @param  ref a named anchor reference within a document.
   * @param  targetFrame the HTML frame that contains the document with the anchor reference.
   */
   public void scrollToReference(String ref, String targetFrame) {
   
      CalViewer vw = null;
      if (ref == null) {
         return;
      } else if (!ref.startsWith("#")) {
         ref = "#" + ref;
      }
      
      if (targetFrame != null) {
         vw = viewer.getFrame(targetFrame);
      }
      if (vw == null) {
         vw = viewer;
      }
      if ((vw.doc != null) && (vw.doc.docURL != null)) {
         URL u = null;
         try {
            //workaround for Java 2 bug
            if (("file").equals(vw.doc.docURL.getProtocol())) {
               ref = vw.doc.docURL.getFile() + ref;
            }
            u = new URL(vw.doc.docURL, ref);
         } catch (MalformedURLException e) {
         }
         if (u != null) {
            showHTMLDocument(u, targetFrame, false);
         }
      }
   }


   void showNewFrame(CalHistoryItem h, String s) {
   
      JFrame frame = new JFrame();
      CalHTMLPane p = new CalHTMLPane(pref, observer, h.targetFrame);
      frame.getContentPane().add(p);
      frame.setSize(this.getSize());
      frame.setVisible(true);
      if (h.targetFrame == null) {
         h.targetFrame = p.viewer.name;
      }
      p.showDocument(h, s, false, HISTORY_NEW, 0);
   }


   void goToLink(CalHistoryItem h, int link) {
         
      if ((h.docURL != null) && (h.targetFrame != null)) {
         if (("mailto").equals(h.docURL.getProtocol())) {
            return;
         }
         showDocument(h, null, false, HISTORY_NEW, link);
      }
   }   


   private void saveHistory(String targetFrame) {
   
      CalHistoryItem current = hist.getCurrent();
      CalHistoryItem old = viewer.getHistoryState(targetFrame);
      if (old != null) {
        if ((current != null) && (current.targetFrame != null) &&
                                                          current.targetFrame.equals(targetFrame)) {
            hist.replaceCurrent(old);
         } else {
            hist.addHistoryItem(old);
         }
      }
   }


   /**
   * Show the previous document in the Pane's history list. If the Pane is already
   * at the bottom of its history then the call will be ignored.
   */
   public void goBack() {
   
      CalHistoryItem h;
      
      viewer.stopAllProcesses();
      while (true) {
         h = hist.getCurrent();
         if (h != null) {
            hist.replaceCurrent(viewer.getHistoryState(h.targetFrame));
         }
         h = hist.getBack();
         if (h == null) {
            break;
         } else {
            if (viewer.showDocument(h, null, false, HISTORY_HIST, 0) != ALREADY_SHOWN) {
               break;
            }
         }
      }
   }


   /**
   * Show the next document in the Pane's history list. If the Pane is already
   * at the top of its history then the call will be ignored.
   */
   public void goForward() {

      CalHistoryItem h;

      viewer.stopAllProcesses();
      while (true) {
         h = hist.getCurrent();
         if (h != null) {
            hist.replaceCurrent(viewer.getHistoryState(h.targetFrame));
         }
         h = hist.getForward();
         if (h == null) {
            break;
         } else {
            if (viewer.showDocument(h, null, false, HISTORY_HIST, 0) != ALREADY_SHOWN) {
               break;
            }
         }
      }
   }


   /**
   * Show the contents of the <code>URL</code> specified as <code>homeURL</code>
   * by <code>CalHTMLPreferences</code> in the top level frame of the Pane. 
   */
   public void goHome() {

      if (pref.homeURL != null) {
         goToLink(new CalHistoryItem(pref.homeURL, viewer.name, pref.homeURL.getRef(), null, null), 0);
      }
   }
   
   
   /**
   * Reloads the current document.
   */
   public void reloadDocument() {
   
      CalHistoryItem h;

      viewer.stopAllProcesses();
      h = viewer.getHistoryState(viewer.name);
      if (h != null) {
         viewer.showDocument(h, null, true, HISTORY_HIST, 0);
      }
   }

   
   /**
   * Stops all processes within the Pane. In most cases cancellation of any current document loading will
   * be immediate. However, this call has a slightly different effect on a thread that is currently waiting
   * for image data, for this reason:
   * <BLOCKQUOTE>Some document authors do not specify widths and heights for images in their documents.
   * This presents a problem when parsing/loading. A temporary default image size could be used so that
   * the document could be displayed 'on the fly', but this means reformatting the whole document once the
   * true image size becomes available. This can considerably lengthen parsing time and is disconcerting
   * for the document reader. The alternative is to delay the display of the document until all image sizes
   * are known.
   * <P>The latter policy is followed by the CalHTMLPane. On occasions however the image data fails
   * to arrive, and the parsing thread then becomes 'locked' as it waits for this data, even though the
   * rest of the document has been parsed and is ready to be formatted.
   * When this method is called (usually by the user pressing a 'STOP' button) a thread which is looping
   * in this way will be freed and document parsing will continue, with default sizes being used for
   * images with incomplete data.</BLOCKQUOTE>
   */
   public void stopAll() {
   
      CalHistoryItem h;

      viewer.stopAllProcesses();
      if (navbarExists) {
         navbar.statusLabel.setText("Stopped");
         navbar.stopButton.setEnabled(false);
      }
   }


   /**
   * Sets the scrollbar policy of the Pane. 
   * The arguments which can be sent to this method are:<P>
   * <UL type=disc><LI><code>CalCons.VIEWER</code> (The default setting)<BR>  
   * &nbsp;&nbsp;&nbsp;&nbsp;Equivalent to <code>ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
   * ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED</code><BR>
   * <LI><code>CalCons.V_YES</code><BR>
   * &nbsp;&nbsp;&nbsp;&nbsp;Equivalent to <code>ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
   * ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS</code><BR>
   * <LI><code>CalCons.V_NO</code><BR>
   * &nbsp;&nbsp;&nbsp;&nbsp;Equivalent to <code>ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, 
   * ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER</code><BR>
   * <LI><code>CalCons.V_AUTO</code><BR>
   * &nbsp;&nbsp;&nbsp;&nbsp;Equivalent to <code>ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
   * ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED</code><BR>
   * </UL>
   * <P>Note that unless scrolling policy is set to <code>V_NO</code>, documents will always be formatted
   * on the assumption that a vertical scrollbar is present, even if one is not currently visible.
   * This is due to the asynchronous loading of documents. When a Pane loads a document it cannot 'know'
   * whether or not a vertical scrollbar is going to be required, and adjusting for the sudden appearance
   * of one would necessitate a reformat of the whole document.
   * @param  n a value which dictates the scrollbar policy for the Pane.
   * @return <code>true</code> if a supported scrollbar policy was sent to this method
   */
   public boolean setScrollBarPolicy(int n) {
   
      switch (n) {
         case V_YES: case V_NO: case V_AUTO: case VIEWER:
              viewer.scrolling = n;
              viewer.revertScrollBars();
              return true;
      }
      return false;
   }

   
   /**
   * Overrides <code>JComponent isManagingFocus()</code>. Programmers are advised not to
   * override this method. 
   */       
   public boolean isManagingFocus() {
   
      return CalHTMLManager.enableCalFocusManager;
   }


   /**
   * Overrides <code>JComponent isFocusTraversable()</code>. Programmers are advised not to
   * override this method. 
   */       
   public boolean isFocusTraversable() {
   
      return CalHTMLManager.enableCalFocusManager;
   }


   /**
   * Public due to implementation requirements. Programmers are advised not to
   * override this method. 
   */
   public void keyPressed(KeyEvent e) {
   
      CalViewer vr = (e.getSource() instanceof CalViewer) ? (CalViewer)e.getSource() : null;
      int code = e.getKeyCode();
      if ((code == KeyEvent.VK_TAB) || (e.getKeyChar() == '\t')) {
         boolean isControl = ((e.getModifiers() & ActionEvent.CTRL_MASK) > 0);
         int offset = ((e.getModifiers() & ActionEvent.SHIFT_MASK) > 0) ? -1 : 1;
         if (vr != null) {
            if (!vr.isShowing()) {  //prob 'cos a frame or iFrame has been dumped, so start new cycle
               startNewTabCycle();
               vr = viewer;
            }
            if (isControl) {
               if ((offset == 1) && (vr.view != null) && (vr.view.tabIndex > 0)) {
                  vr.currTabIndex = vr.view.tabIndex - 1;
               } else {
                  offset = -1;
                  vr.currTabIndex = 0;
               }
            }
            vr.tabBackForward(null, offset, false);
         }
         e.consume();
      } else if (vr != null) {
         if (!vr.isShowing()) {  //prob 'cos a frame or iFrame has been dumped, so start new cycle
            startNewTabCycle();
            vr = viewer;
         }
         if (code == KeyEvent.VK_ESCAPE) {
            if (("_dialog").equals(vr.name)) {
               closeDialog();
               startNewTabCycle();
               vr = viewer;
            }
         } else if ((code == KeyEvent.VK_ENTER) || (e.getKeyChar() == '\n')) {
            if (vr.activeTabLink > 0) {
               if ((vr.doc != null) && (vr.doc.tags != null) && (vr.activeTabLink < vr.doc.tags.length)) {
                  int tag = vr.doc.tags[vr.activeTabLink];
                  if (tag == A) {
                     int link = vr.doc.attrVals[vr.doc.tags[vr.activeTabLink + 2]];
                     String s = vr.getAttrString(A_HREF, vr.activeTabLink + 3);                     
                     String jName = vr.getAttrString(A_JNAME, vr.activeTabLink + 3);    
                     String targetFrame = vr.getAttrString(A_TARGET, vr.activeTabLink + 3);
                     if (s != null) {
                        if (vr.followLink(s, targetFrame, jName, link)) {
                           vr.repaint();
                        }
                     }
                  }
               }
            }
         } else if ((code == KeyEvent.VK_DOWN) || (code == KeyEvent.VK_UP) ||
                     (code == KeyEvent.VK_LEFT) || (code == KeyEvent.VK_RIGHT) ||
                     (code == KeyEvent.VK_SPACE)) {
            if (code == KeyEvent.VK_SPACE) {
               code = ((e.getModifiers() & ActionEvent.SHIFT_MASK) > 0) ? KeyEvent.VK_UP : KeyEvent.VK_DOWN;
            }
            if (vr.viewport != null) {
               Point p = vr.viewport.getViewPosition();
               switch (code) {
                  case KeyEvent.VK_DOWN : p.y += 14; break;
                  case KeyEvent.VK_UP   : p.y -= 14; break;
                  case KeyEvent.VK_LEFT : p.x -= 14; break;
                  case KeyEvent.VK_RIGHT: p.x += 14; break;
               }
               vr.viewport.setDocViewPosition(p);
            }
         } else if ((code == KeyEvent.VK_PAGE_DOWN) || (code == KeyEvent.VK_PAGE_UP)) {
            if (vr.viewport != null) {
               Rectangle r = vr.viewport.getViewRect();
               if (code == KeyEvent.VK_PAGE_DOWN) {
                  r.y += r.height;
               } else {
                  r.y -= r.height;
               }
               vr.viewport.setDocViewPosition(new Point(r.x, r.y));
            }
         }
      }
   }


   /**
   * Public due to implementation requirements. Programmers are advised not to
   * override this method. 
   */   
   public void keyReleased(KeyEvent e) {
   
      if ((e.getKeyCode() == KeyEvent.VK_TAB) || (e.getKeyChar() == '\t')) {
         e.consume();
      }
   }


   /**
   * Public due to implementation requirements. Programmers are advised not to
   * override this method. 
   */
   public void keyTyped(KeyEvent e) {
   
      if ((e.getKeyCode() == KeyEvent.VK_TAB) || (e.getKeyChar() == '\t')) {
         e.consume();
      }
   }


   private class PFL extends FocusAdapter {
   
      public void focusGained(FocusEvent e) {
      
         startNewTabCycle();
      }
   }
   
   
   private void startNewTabCycle() {
   
      if (viewer.frameset != null) {
         viewer.tabBackForward(null, 1, true);
      } else {
         viewer.currTabIndex = -1;
         viewer.requestFocus();
      }
   }


   //called by a form component from CalFocusManager when returning focus to its parent viewer
   void tabBackForward(CalViewer vr, KeyEvent e, boolean isControl) {
   
      int offset = ((e.getModifiers() & ActionEvent.SHIFT_MASK) > 0) ? -1 : 1;
      if (isControl) {
         if ((offset == 1) && (vr.view != null) && (vr.view.tabIndex > 0)) {
            vr.currTabIndex = vr.view.tabIndex - 1;
         } else {
            offset = -1;
            vr.currTabIndex = 0;
         }
      }
      vr.tabBackForward(null, offset, false);
   }


   /**
   * Parses the sent message as HTML and displays the result in the Pane's dialog. See the
   * README.TXT file accompanying this documentation for more details regarding the Pane's dialog. 
   * @param  message a String to be formatted as HTML in the Pane's dialog.
   */
   public void showDialog(String message) {

      showDialog(message, null, -1, -1, -1, -1);
   }


   /**
   * Parses the sent message as HTML and displays the result in the Pane's dialog. <BR>
   * If a messageName is given the Pane will attempt to use a cached version of the message
   * which has already been parsed under the same name. The message sent to this method may be
   * <code>null</code> if the messageName is not <code>null</code>;
   * <P> If x >= 0 the Pane will use this value as a guide to the left-horizontal coordinate
   * for setting the dialog's bounds.
   * <BR> If y >= 0 the Pane will use this value as a guide to the top-vertical coordinate
   * for setting the dialog's bounds.
   * <BR> If w > 0 the Pane will use this value as a guide to the width of the dialog.
   * <BR> If h > 0 the Pane will use this value as a guide to the height of the dialog.
   * <P>The Pane will honour any sent bounds values where it can, but it will always try and ensure that
   * the dialog is fully visible and that the dialog's contents fit properly within its bounds.
   * <P>See the README.TXT file accompanying this documentation for more
   * details on programming the Pane's dialog. 
   * @param  message a String to be formatted as HTML in the Pane's dialog.
   * @param  messageName a name given to the message for caching purposes.
   * @param  x the left-horizontal coordinate of the dialog's bounds.
   * @param  y the top-vertical coordinate of the dialog's bounds.
   * @param  w the width of the dialog.
   * @param  h the height of the dialog.
   */
   public void showDialog(String message, String messageName, int x, int y, int w, int h) {
   
      if (message == null) {
         return;
      }
      if (dialog.sp.isVisible()) {
         closeDialog();
      }
      if (w <= 0) {
         w = Math.max(100, getWidth() / 3);
      }
      dialog.showDialogMessage(message, messageName, x, y, w, h);
   }
   
   
   /**
   * Closes the Pane's dialog if it is currently visible. Any current parsing of a dialog message
   * will be halted. If the dialog is not visible this method call is ignored.
   */
   public void closeDialog() {
   
      if (dialog.sp.isVisible()) {
         dialog.stopAllProcesses();
         dialog.sp.setVisible(false);
      }
   }         


   void setDialogBounds(int panew, int paneh, int x, int y, int w, int h) {
   
      if (x < 0) {
         x = ((panew - w) >> 1);
      } else {
         x = Math.max(2, Math.min(x, panew - w - 3));
      }
      if (y < 0) {
         y = ((paneh - h) >> 1);
      } else {
         y = Math.max(2, Math.min(y, paneh - h - 3));
      }
      dialog.sp.setBounds(x, y, w, h);
   }


   void handleFormPost(CalHistoryItem postHistory, String data) {

      if (pref.postWarning) {
         waitingPostHistory = postHistory;
         waitingPostData    = data;
         showUnsecurePostMessage();
      } else {
         doFormPost(postHistory, data);
      }
   }


   private void doFormPost(CalHistoryItem postHistory, String data) {
   
      showDocument(postHistory, data, true, HISTORY_NEW, 0);
   }


   /**
   * Returns a Hashtable containing all components in the target frame which have the HTML 'id' attribute.
   * If the target frame is null then all id components within the CalPane are returned.
   * <P>
   * An example component would be one created with the following HTML:
   * <P>
   * &nbsp;&nbsp;&nbsp;&lt;INPUT type=text name=username id=username&gt;
   * <P>
   * The keys in the Hashtable are the id values of the components.
   * This method allows programmers to get handles to HTML components after they have been created,
   * either to monitor/manipulate their state or, for example, to programmatically fire a form submission.
   * <P>Note that programmers should ensure that a document has finished loading before trying to access
   * any components within it, otherwise this method may return before the components have been created.
   * @param  targetFrame the name of the frame containing the components, or all frames if <code>null</code>
   * @return a <code>Hashtable</code> of components, with their HTML 'id' values as keys
   */
   public Hashtable getIDComponents(String targetFrame) {
   
      Hashtable table = new Hashtable();
      viewer.getComponents(table, targetFrame);
      return table;
   }


   /**
   * Enables/disables the synchronous loading of documents.
   * If <code>loadSynchronously</code> is enabled then <code>showHTMLDocument()</code> methods
   * will not return until a document has loaded. In addition documents
   * loading from activated hyperlinks will also load synchronously.
   * <P>
   * Loading synchronously effectively blocks the current AWT thread
   * which will reduce the responsiveness of the <code>CalHTMLPane</code> (and will freeze the rest of
   * your application), but it can be useful
   * for programmers who want to be sure that a document has loaded before proceeding with another
   * operation. Generally it is better to use the callback methods in <code>CalHTMLObserver</code>
   * to determine when a document has finished loading.
   *<P>The default policy is to load documents asynchronously.
   */
   public void setLoadSynchronously(boolean b) {
   
      syncLock = b;
   }
  

   /**
   * Returns whether documents will be loaded by the <code>CalPane</code> synchronously or asynchronously.
   * @return  <code>true</code> if documents will load synchronously.
   * @see     calpa.html.CalHTMLPane#setLoadSynchronously
   */     
   public boolean isLoadSynchronouslyEnabled() {
   
      return syncLock;
   }
   
   
   //START OF OBSERVER-RELATED UPDATES. Cal sub-components call these methods and Pane relays them to
   //the installed CalHTMLObserver

   void linkFocusedUpdate(URL u) {

      observer.linkFocusedUpdate(this, u);   
   }
   
   
   void linkActivatedUpdate(URL u, String targetFrame, String jName) {

      observer.linkActivatedUpdate(this, u, targetFrame, jName);
   }
   

   void statusUpdate(int status, URL url, int value, String message) {
      
      switch (status) {
         case PRE_CONNECT:
            if (navbarExists) {
               navbar.statusLabel.setText("Connecting...");
               if (!navbar.stopButton.isEnabled()) {
                  navbar.stopButton.setEnabled(true);
               }
            }
            break;
         case CONNECTED:
            if (navbarExists) {
               navbar.statusLabel.setText("Loading...");
               if (value == 0) {
                  if (url != null) {
                     if (url.toExternalForm().startsWith("http://www.caldoc")) {
                        navbar.tf.setText("Doc #" + url.getRef());
                     } else {
                        navbar.tf.setText(url.toExternalForm());
                     }
                  } else {
                     navbar.tf.setText("");
                  }
               }
            }
            break;
         case WAITING_FOR_IMAGES:
            if (navbarExists) {
               navbar.statusLabel.setText("Images Loading...");
            }
            break;
         case DOC_LOADED:
            boolean done = false;
            if (viewer.allFinished()) {
               done = true;
               if (value != 0) {
                  observer.statusUpdate(this, status, url, value, message);
                  if (viewer.doc != null) {
                     url = viewer.doc.docURL;    //we've sent the original status, now change it to viewer
                  }
                  value = 0;
               }
            }
            if (done) {
               viewer.updateLinks(); 
               if (navbarExists) {
                  navbar.statusLabel.setText("Finished");
                  navbar.stopButton.setEnabled(false);
               }
            }
            break;
         case PARSE_FAILED:
            if (pref.displayErrors) {
               showFailedConnectMessage(url);
            }
            if (navbarExists) {
               navbar.statusLabel.setText("Finished");
               navbar.stopButton.setEnabled(false);
            } 
            break;
         case PARSE_FAILED_POST_CONNECT:
            if (pref.displayErrors) {
               showFailedPostConnectMessage(message);
            }
            if (navbarExists) {
               navbar.statusLabel.setText("Finished");
               navbar.stopButton.setEnabled(false);
            } 
            break;
         case DOC_LENGTH:
            if (navbarExists) {
               int n = value / 1000;
               navbar.statusLabel.setText("Loading " + n + "K");
            }
            break;
      }
      observer.statusUpdate(this, status, url, value, message);
   }
   
   
   void formSubmitUpdate(URL docBaseURL, int method, String action, String enctype, String data) {

      //don't pass on enctype at the moment
      if (method == V_JFORM) {
         if (("close_dialog").equalsIgnoreCase(action)) {
            closeDialog();
         } else if (("form_post_warning").equals(action)) {
            closeDialog();
            if ((data != null) && (waitingPostHistory != null) && (waitingPostData != null)) {
               if (data.indexOf("show=on") != -1) {
                  pref.postWarning = false;
               }
               if (data.indexOf("proceed") != -1) {
                  doFormPost(waitingPostHistory, waitingPostData);
               }
               waitingPostHistory = null;
               waitingPostData    = null;
            }
         }
      }            
      observer.formSubmitUpdate(this, docBaseURL, method, action, data);
   }      


   void historyUpdate(int position) {

      if (navbarExists) {
         if ((position & CalCons.AT_HISTORY_TOP) > 0) {
            if (navbar.forwardButton.isEnabled()) {
               navbar.forwardButton.setEnabled(false);
            }
         } else {
            if (!navbar.forwardButton.isEnabled()) {
               navbar.forwardButton.setEnabled(true);
            }
         }
         if ((position & CalCons.AT_HISTORY_BOTTOM) > 0) {
            if (navbar.backButton.isEnabled()) {
               navbar.backButton.setEnabled(false);
            }
         } else {
            if (!navbar.backButton.isEnabled()) {
               navbar.backButton.setEnabled(true);
            }
         }
      }
      observer.historyUpdate(this, position);
   }
   
   
   private void showFailedConnectMessage(URL url) {
   
      StringBuffer sb = new StringBuffer();
      String s;
      int n = 0;
      
      sb.append(STR_DIALOG_MESSAGE1).append(STR_CONNECT_ERROR1).append(STR_DIALOG_MESSAGE2);
      if (url != null) {
         sb.append(STR_WARNING_IMAGE1).append(STR_CONNECT_ERROR2).append("</TD></TR><TR><TD align=center>");
         s = url.toExternalForm();
         n = s.indexOf("?");
         if (n > 0) {
            s = s.substring(0, n);
         }
         dialog.f.checkFont(pref.famVals[S_SMALL], NORM, pref.fontVals[S_SMALL]);
         n = dialog.f.fm[pref.famVals[S_SMALL]][NORM][pref.fontVals[S_SMALL]].stringWidth(s) + 20;
         sb.append(s).append("</TD></TR><TR><TD align=center>");
      } else {
         sb.append(STR_WARNING_IMAGE1).append(STR_CONNECT_ERROR3).append("</TD></TR><TR><TD align=center>");
      }
      sb.append(STR_OK_BUTTON).append(STR_DIALOG_MESSAGE3);
      showDialog(sb.toString(), null, -1, -1, Math.max(n, 250), 80);
   }
   
   
   private void showFailedPostConnectMessage(String message) {
   
      StringBuffer sb = new StringBuffer();
      int n = 0;
      String s = message;
      
      sb.append(STR_DIALOG_MESSAGE1).append(STR_CONNECT_ERROR4).append(STR_DIALOG_MESSAGE2);
      sb.append(STR_WARNING_IMAGE1).append(STR_CONNECT_ERROR5).append("</TD></TR><TR><TD align=center>");
      if (s == null) {
         s = "Error while loading document";
      }
      dialog.f.checkFont(pref.famVals[S_SMALL], NORM, pref.fontVals[S_SMALL]);
      n = dialog.f.fm[pref.famVals[S_SMALL]][NORM][pref.fontVals[S_SMALL]].stringWidth(s) + 20;
      sb.append(s).append("</TD></TR><TR><TD align=center>");
      sb.append(STR_OK_BUTTON).append(STR_DIALOG_MESSAGE3);
      showDialog(sb.toString(), null, -1, -1, Math.max(n, 250), 80);
   }


   private void showUnsecurePostMessage() {
   
      StringBuffer sb = new StringBuffer();
      
      sb.append(STR_DIALOG_MESSAGE1).append(STR_INFO_MESSAGE1).append(STR_DIALOG_MESSAGE2);
      sb.append(STR_WARNING_IMAGE1).append(STR_INFO_MESSAGE2);
      sb.append(STR_INFO_MESSAGE3).append("</TD></TR><TR><TD align=center>");
      sb.append(STR_FORM_POST_CONTROLS1).append(STR_FORM_POST_CONTROLS2);
      sb.append("<code>&nbsp;&nbsp;</code>").append(STR_FORM_POST_CONTROLS3);
      sb.append("</TD></TR><TR><TD align=center>").append(STR_FORM_POST_CONTROLS4);
      sb.append(STR_DIALOG_MESSAGE3);
      showDialog(sb.toString(), "unsecure_post", -1, -1, 250, 80);
   }


   //public void printHTMLDocument() {
   
   //   PrinterJob job = PrinterJob.getPrinterJob();
   //   if ((viewer.doc != null) && (viewer.doc.docType == D_HTML) && (viewer.doc.state == PARSED)) {
   //      CalPP pp = new CalPP(viewer);
   //      job.setPrintable(pp);
   //      if (job.printDialog()) {
   //         try {
   //            job.print();
   //         } catch (Exception e) {
   //            System.err.println(e.getMessage());
   //            e.printStackTrace();
   //         }
   //      }
   //   }
   //}

}
