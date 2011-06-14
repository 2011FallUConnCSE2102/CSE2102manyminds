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

/**
* A class implementing this interface can be passed to a <code>CalHTMLPane</code> at
* construction time and will thereafter receive notification of events which have occured within the Pane.
* A <code>CalHTMLPane</code> always has one, and only one, attached <code>CalHTMLObserver</code>.
* <P>If a <code>CalHTMLObserver</code> is not passed to the Pane during construction, a
* <code>DefaultCalHTMLObserver</code>
* will be used. <code>DefaultCalHTMLObserver</code> implements all the methods of this
* interface as null-ops, allowing the programmer to extend the class and only implement
* those methods of interest.
* <P>Note that the same <code>CalHTMLObserver</code> may be used with several
* <code>CalHTMLPanes</code>.
* @see     calpa.html.CalHTMLPane
*/
public interface CalHTMLObserver {

   /**
   * Notification that a hyperlink has been activated via the keyboard or mouse.
   * If the jname argument is null the link will be automatically followed by the calling Pane, unless
   * the <code>URL</code> protocol is 'mailto', which the Pane cannot currently handle. 
   * If the jname argument is <EM>not</EM> null the pane will not attempt to follow the link, allowing
   * it to be handled here.
   * 
   * @param pane the <code>CalHTMLPane</code> which has called this method
   * @param url  the <code>URL</code> of the link that has been activated
   * @param targetFrame  the name of the frame where the contents of the <code>URL</code> are
   * to be displayed
   * @param jname  A name given to the link so it can be handled outside the calling Pane
   */
   public abstract void linkActivatedUpdate(CalHTMLPane pane, URL url, String targetFrame, String jname);

   
   /**
   * Notification that a hyperlink has received or lost keyboard/mouse focus.
   * This method is called when there has been a <EM>change</EM> in focus. If the <code>URL</code>
   * sent is not <code>null</code> then a new link has now received focus.
   * If the <code>URL</code> is <code>null</code> then a link which previously had focus
   * has now lost it, and no link is currently focused.
   * <P>This method can be used, for example, to update a status display which shows the currently
   * focused link on the user's screen.
   * @param pane the <code>CalHTMLPane</code> which has called this method
   * @param url  the <code>URL</code> of the focused link, or <code>null</code> if no link has the focus 
   */
   public abstract void linkFocusedUpdate(CalHTMLPane pane, URL url);


   /**
   * Gives general notifications of events or errors which are occuring within the Pane. Apart from
   * the <code>DOC_LENGTH</code> status argument listed below, the <code>value</code> argument sent
   * will be the nesting level of the frame initiating this call. A nesting of 0 indicates the
   * Pane's top level frame has made the call. Often you will only be interested in calls from the
   * top level frame. For example, if a frameset document is loading you may get the DOC_LOADED
   * status call several times, but the one that really counts is the frame 0 call, and this is
   * never sent until all sub-frames have finished loading.
   * <P>The current status arguments sent to this method are:
   * <UL>
   * <LI><code>CalCons.PRE_CONNECT</code><BR>
   * The Pane is attempting to connect to the given <code>URL</code> to receive data.
   * <LI><code>CalCons.PARSE_FAILED</code><BR>
   * The Pane was unable to connect to the given <code>URL</code> or was unable to parse the content.
   * Most likely this will be due to an incorrectly specified <code>URL</code>.
   * The message argument may contain further details of the reason for failure. 
   * <LI><code>CalCons.CONNECTED</code><BR>
   * The Pane has established a connection to the given <code>URL</code> and is receiving any content.
   * <LI><code>CalCons.DOC_LENGTH</code><BR>
   * The size of the content at the given <code>URL</code> is known and is contained
   * in the <code>value</code> argument.
   * <LI><code>CalCons.TITLE</code><BR>
   * The title of the document for the given <code>URL</code> is known and is contained
   * in the <code>message</code> argument. Only the title of a document in the Pane's top level frame
   * will be sent to this method. If the document has no name, the message will be null, unless the
   * document is a Frameset document in which case the message "Frameset" will be sent.  
   * <LI><code>CalCons.PARSE_FAILED_POST_CONNECT</code><BR>
   * An exception has been thrown during parsing of the data from the <code>URL</code>. This will most
   * likely be an <code>IOException</code> such as a server time-out.
   * <LI><code>CalCons.WAITING_FOR_IMAGES</code><BR>
   * The document has been parsed but formatting cannot be completed because the document contains images
   * of unspecified size. The parsing thread is waiting for image updates to give it the information
   * it needs to format and display the document.
   * <LI><code>CalCons.DOC_LOADED</code><BR>
   * All text and image data has been received, parsed and the document structure determined.
   * </UL>
   * @param pane  the <code>CalHTMLPane</code> which has called this method
   * @param status  the status code of the update
   * @param url     a <code>URL</code> related to the status code
   * @param value   a value related to the status code
   * @param message a message related to the status code
   */
   public abstract void statusUpdate(CalHTMLPane pane, int status, URL url, int value, String message);


   /**
   * Notification that a form submission has been initiated. Whether the Pane handles the
   * submission directly depends on whether <code>handleFormSubmission</code> is enabled or
   * disabled by the <code>CalHTMLPreferences</code> object controlling the Pane.
   * <P>The method argument will be either <code>CalCons.V_GET, CalCons.V_POST, or CalCons.V_JFORM</code><BR>
   * If the argument is <code>V_JFORM</code> then the Pane will take no action, irrespective of whether
   * <code>handleFormSubmission</code> is enabled. This allows the programmer to treat this method as
   * a pseudo <code>actionListener</code> for controls placed within documents.
   * <P>The data argument sent to this method is an x-www-form-urlencoded concatenated string of the
   * form results gathered from successful form controls.
   * @param pane  the <code>CalHTMLPane</code> which has called this method
   * @param docBaseURL  the <code>URL</code> of the document containing the form, possibly modified
   *                    by the &lt;BASE&gt; tag
   * @param method      a code for the form method - GET, POST or JFORM
   * @param action      the value of the action attribute (if any) specified in the FORM tag
   * @param data        the concatenated, encoded form results
   * @see     calpa.html.CalHTMLPreferences#setHandleFormSubmission
   */
   public abstract void formSubmitUpdate(CalHTMLPane pane, URL docBaseURL, int method, String action,
                                                                                         String data);   


   /**
   * Notification of a change in position within the Pane's document history.
   * This method's primary purpose is to allow the programmer to enable/disable controls which
   * navigate the Pane's history. Note that it is possible for the Pane to be simultaneously at the
   * top <EM>and</EM> bottom of its history (when the Pane has only shown a single document for example).
   * It's easiest to show how you might use this method with some simple pseudo-code: 
   * <BLOCKQUOTE>
   * <code>if (position == CalCons.AT_HISTORY_MIDDLE) { <BR>
   *            &nbsp;&nbsp;&nbsp;&nbsp;//...enable both the 'back' and 'forward' buttons <BR>
   *       } else { <BR>
   *          &nbsp;&nbsp;&nbsp;&nbsp;if ((position & CalCons.AT_HISTORY_TOP) > 0) { <BR>
   *              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//...disable the 'forward' button  <BR>
   *          &nbsp;&nbsp;&nbsp;&nbsp;} else {  <BR>
   *              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//...enable the 'forward' button  <BR>
   *          &nbsp;&nbsp;&nbsp;&nbsp;}  <BR>
   *          &nbsp;&nbsp;&nbsp;&nbsp;if ((position & CalCons.AT_HISTORY_BOTTOM) > 0) {  <BR>
   *              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//...disable the 'back' button   <BR>
   *          &nbsp;&nbsp;&nbsp;&nbsp;} else {   <BR>
   *              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//...enable the 'back' button   <BR>
   *          &nbsp;&nbsp;&nbsp;&nbsp;}   <BR>
   *       }
   * </code></BLOCKQUOTE>
   * @param pane the <code>CalHTMLPane</code> which has called this method
   * @param position a value denoting the current position within the Pane's history
   */
   public abstract void historyUpdate(CalHTMLPane pane, int position);
   
   
   /**
   * Notification for a new <code>CalHTMLPane</code> to be created with
   * the specified top-level frame name and showing the specified document. <BR>
   * <P>This method will be only be called if <code>handleNewFrames</code> is disabled in the
   * <code>CalHTMLPreferences</code> object controlling the Pane. The need for a new frame
   * occurs when an HTML anchor or other tag specifies that a URL should be displayed in a
   * frame which has a name unknown to the Pane, or when the reserved HTML name "_blank" is
   * specified.
   * @param  pane the <code>CalHTMLPane</code> which has called this method
   * @param  targetFrame  the name to be given to the top-level frame of the new Pane, 
   *         or <code>null</code> if no name has been specified
   * @param  url  the <code>URL</code> of the document to be displayed in the new Pane
   * @see     calpa.html.CalHTMLPreferences#setHandleNewFrames
   */
   public abstract void showNewFrameRequest(CalHTMLPane pane, String targetFrame, URL url);

}   
   
