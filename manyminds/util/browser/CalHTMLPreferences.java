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

import java.util.Hashtable;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.Color;
import java.awt.Font;

/**
 * A class used to control the default operation and rendering methods
 * of a <code>CalHTMLPane</code>. The same <code>CalHTMLPreferences</code>
 * object can be used with multiple panes. During construction all fields of
 * the preferences object are set to default values. Individual fields 
 * can then be modified through the access methods listed. <P>Certain methods
 * can only modify fields before the preferences object has been passed to
 * a <code>CalHTMLPane</code>. Calling such methods after this point will not
 * throw an exception - the calls are simply ignored.
 * @see     calpa.html.CalHTMLPane
 * @author Andrew Moulden
 * @version 0.8beta 11/27/98
 */
 
public class CalHTMLPreferences implements CalCons {

   Hashtable visitedHash;

   int[]     fontVals            = {1, 3, 4};
   int[]     famVals             = {HELV, TIMES, TIMES};
   int[]     styleVals           = {NORM, NORM, NORM};
   int[]     formTextFontVals    = {1, 2, 3};
   int[]     formTextFamVals     = {HELV, HELV, HELV};
   int[]     formTextStyleVals   = {NORM, NORM, NORM};
   int[]     formButtonFontVals  = {1, 2, 3};
   int[]     formButtonFamVals   = {HELV, HELV, HELV};
   int[]     formButtonStyleVals = {NORM, NORM, NORM};
   int[]     frameWidth    = {250, 400};
   int       bgcolor;
   int       highlight;
   int       shadow;
   int       textcolor;
   int       linkcolor;
   int       vlinkcolor;
   int       alinkcolor;
   int       formControl;
   int       formHighlight;
   int       formShadow;
   int       formTextBackground;
   int       formTextColor;
   int       linkTabBackground;
   int       linkTabForeground;
   int       formRendering;
   int       frameSpacing;
   int       marginwidth;
   int       marginheight;
   int       optimizeDisplay;
   boolean   underlineLinks;
   boolean   hyperlinkHover;
   boolean   loadImages;
   boolean   displayErrors;
   boolean   handleFormSubmission;
   boolean   handleNewFrames;
   boolean   followLinks;
   boolean   focusOnClick;
   boolean   showNavBar;
   boolean   postWarning;
   boolean   allowChanges;
   URL       homeURL;
   
   /** 
     * Creates a new <code>CalHTMLPreferences</code> object which will have all fields
     * set to default values.
   */
   public CalHTMLPreferences() {

      visitedHash           = new Hashtable();
      bgcolor               = CalColor.getColor(0xFFFFFF);
      textcolor             = CalColor.getColor(0x000000);
      linkcolor             = CalColor.getColor(0x0000EA);
      alinkcolor            = CalColor.getColor(0xFF0000);
      vlinkcolor            = CalColor.getColor(0x800080);
      formTextBackground    = CalColor.getColor(0xFFFFFF);
      formTextColor         = CalColor.getColor(0x000000);
      linkTabBackground     = CalColor.getColor(0x300060);
      linkTabForeground     = CalColor.getColor(0xFFFFD0);
      formControl           = CalColor.controlIndex;
      formHighlight         = CalColor.highlightIndex;
      formShadow            = CalColor.shadowIndex;
      formRendering         = USE_CALPA_THREEDEE;
      frameSpacing          = 4;
      marginwidth           = 6;
      marginheight          = 6;
      underlineLinks        = true;
      loadImages            = true;
      displayErrors         = true;
      handleFormSubmission  = true;
      handleNewFrames       = true;
      followLinks           = true;
      showNavBar            = false;
      focusOnClick          = false;
      postWarning           = true;
      allowChanges          = true;
      optimizeDisplay       = NO_OPTIMIZATION;
      try {
         homeURL = new URL("http://www.w3.org/");
      } catch (MalformedURLException e) {
         System.err.println("Malformed homeURL");
      }
   }


   synchronized void addVisitedLink(int attemptedHash) {
   
      if ((attemptedHash != 0) && (attemptedHash != -1)) {
         visitedHash.put(new Integer(attemptedHash), new Long(System.currentTimeMillis()));
      }
   }
   

   /**
   * Returns a <code>Hashtable</code> containing references to visited <code>URLs</code>.
   * This hashtable can be passed into a new <code>CalHTMLPreferences</code> object
   * at a later date so that a user's visited links are persistent. The
   * method of storing the hashtable's contents outside of this class
   * is left to the programmer.
   * <P>The keys within the hashtable are all <CODE>Integer</CODE> objects,
   * each representing a hashcode of a visited <code>URL</code> (note that this is
   * <EM>not</EM> the hashcode returned from the <code>URL.hashcode()</code> method).
   * <BR>The values within the hashtable are all <CODE>Long</CODE> objects
   * representing the <CODE>System.currentTimeMillis()</CODE> when the
   * link was last visited. The programmer may therefore remove 'old'
   * links if so desired.
   * @return a Hashtable recording visited document URLs
   * @see     calpa.html.CalHTMLPreferences#setVisitedHash
   */
   public Hashtable getVisitedHash() {
   
      return visitedHash;
   }

   
   /**
   * Sets the <code>Hashtable</code> containing references to <code>URLs</code> which
   * will be used by a <CODE>CalHTMLPane</CODE> to mark visited
   * hyperlinks. Only pass in a hashtable which was previously
   * obtained from a <code>CalHTMLPreferences</code> object via the
   * <CODE>getVisitedHash()</CODE> method.
   * @param table a Hashtable containing Integer/Long pairs representing visited document URLs
   * @see     calpa.html.CalHTMLPreferences#getVisitedHash
   */
   public void setVisitedHash(Hashtable table) {
   
      if ((allowChanges) && (table != null)) {
         visitedHash = table;
      }
   }


   /**
   * Determines whether a <code>CalHTMLPane</code> will
   * adjust display parameters based on frame width.
   * The arguments which can be sent to this method are:<P>
   *<UL type=disc>
   * <LI><code>CalCons.NO_OPTIMIZATION</code><BR>  
   * No adjustments will be made for frame width. This is the default value.
   * <LI><code>CalCons.OPTIMIZE_FONTS</code><BR>  
   * Font size may be scaled based on frame width. 
   * <LI><code>CalCons.OPTIMIZE_ALL</code><BR>
   * In addition to font scaling, other parameters such as table cellspacing
   * and cellpadding may be adjusted in an attempt to fit the document into the current frame.
   * </UL> 
   * <P>See the README.TXT file accompanying this document for more details on optimizeDisplay. 
   * @param optimizeLevel a value indicating the level (if any) of display optimization required 
   * @see     calpa.html.CalHTMLPreferences#setFrameDisplayWidth
   * @see     calpa.html.CalHTMLPreferences#setDefaultFont
   */
   public void setOptimizeDisplay(int optimizeLevel) {
   
      optimizeDisplay = optimizeLevel;
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will
   * adjust display parameters based on frame size.
   * <P>See the README.TXT file accompanying this document for more details on optimizeDisplay. 
   * @return a value indicating the current level of display optimization
   */        
   public int getOptimizeDisplay() {
   
      return optimizeDisplay;
   }


   /**
   * Sets the width for the specified displaySize at which point a <CODE>CalHTMLPane</CODE> will
   * start scaling its display if <code>optimizeDisplay</code> is enabled.
   * The permitted displaySize arguments are:<P>
   * <UL type=disc><LI><code>CalCons.S_SMALL</code><LI><code>CalCons.S_MEDIUM</code></UL>
   * <P>There is no need to set a width for displaySize <code>CalCons.S_LARGE</code> as a
   * <code>CalHTMLPane</code> will treat any width greater than the <code>CalCons.S_MEDIUM</code>
   * width as a large display.
   * <P>Default values are: <code>S_SMALL: 250 pixels, S_MEDIUM: 400 pixels</code>
   * <P>See the README.TXT file accompanying this document for more details on optimizeDisplay. 
   */
   public void setFrameDisplayWidth(int displaySize, int width) {
   
      if ((width > 0) && ((displaySize == S_SMALL) || (displaySize == S_MEDIUM))) {
      
         frameWidth[displaySize] = width;
      }
   }


   /**
   * Returns the width for the specified displaySize at which point a <CODE>CalHTMLPane</CODE> will
   * start scaling its display if <code>optimizeDisplay</code> is enabled.
   * The permitted displaySize arguments are:<P>
   * <UL type=disc><LI><code>CalCons.S_SMALL
   * <LI>CalCons.S_MEDIUM</code></UL>
   */
   public int getFrameDisplayWidth(int displaySize) {
   
      if ((displaySize == S_SMALL) || (displaySize == S_MEDIUM)) {
         return frameWidth[displaySize];
      }
      return -1;
   }


   /**
   * @deprecated Use <code>setDefaultFont(int, Font)</code> which allows the setting of
   * system fonts.
   */
   public void setDefaultFont(int displaySize, int fontFamily, int sizeIndex) {
   
      if (((displaySize == S_SMALL) || (displaySize == S_MEDIUM) ||
           (displaySize == S_LARGE)) && ((fontFamily == TIMES) ||
             (fontFamily == HELV) || (fontFamily == MONO) || (fontFamily == DIALOG)) &&
               ((sizeIndex > 0) && (sizeIndex <= 12))) {
         fontVals[displaySize] = sizeIndex;
         famVals[displaySize]  = fontFamily;
      } 
   }
   

   /**
   * Sets a default font to be used by a <CODE>CalHTMLPane</CODE>.
   * This method takes three different size arguments:
   * <UL type=disc><LI><code>CalCons.S_SMALL
   * <LI>CalCons.S_MEDIUM
   * <LI>CalCons.S_LARGE</code><P>
   * </UL>
   * In most cases you will call this method with the <code>CalCons.S_LARGE</code> argument.
   * The <code>S_SMALL</code> and <code>S_MEDIUM</code> arguments are used in special cases
   * relating to printing (which is not yet supported in the Java 2 version of CalPane) and 
   * the <code>optimizeDisplay</code> feature of <code>CalHTMLPane</code>.
   * <P>This method replaces the now deprecated
   * <code>setDefaultFont(int displaySize, int fontFamily, int sizeIndex)</code> method because
   * Java 2 allows access to system fonts, rather than just Java's logical fonts.
   * <P>The font point sizes supported in CalPane are: 10, 11, 12, 13, 14, 16, 18, 20, 22, 24, 28, 36, 48
   * <P>If the size of the font sent to this method does not exactly match a supported size, then the
   * closest supported value will be used.
   * @see     calpa.html.CalHTMLPreferences#setOptimizeDisplay
   */
   public void setDefaultFont(int displaySize, Font font) {
   
      int n;
      if (((displaySize == S_SMALL) || (displaySize == S_MEDIUM) || (displaySize == S_LARGE)) &&
                                                                                   (font != null)) {
         //ANDY: may need to reactivate following when system fonts supported
         //int faceIndex = CalHTMLManager.f.getFontFace(font.getFontName());
         int faceIndex = CalHTMLManager.f.getFontFace(font.getName());
         if (faceIndex >= 0) {
            famVals[displaySize] = faceIndex;
            if ((n = getFontSize(font.getSize())) >= 0) {
               fontVals[displaySize] = n;
            }            
            styleVals[displaySize] = getFontStyle(font.getStyle());
         }
      }
   }
   

   /**
   * Returns a default font being used by a <code>CalHTMLPane</code>
   * @see calpa.html.CalHTMLPreferences#setDefaultFont(int, java.awt.Font)
   */
   public Font getDefaultFont(int displaySize) {

      if ((displaySize == S_SMALL) || (displaySize == S_MEDIUM) || (displaySize == S_LARGE)) {
         CalHTMLManager.f.checkFont(famVals[displaySize], styleVals[displaySize], fontVals[displaySize]);
         return CalHTMLManager.f.fonts[famVals[displaySize]][styleVals[displaySize]][fontVals[displaySize]];
      }
      return null;
   }

   
   /**
   * @deprecated Use either <code>setDefaultButtonFont(int, Font)</code> or 
   * <code>setDefaultFormTextFont(int, Font)</code>.
   */
   public void setFormFont(int displaySize, int fontFamily, int fontStyle, int sizeArrayIndex) {
   
      boolean valid = true;
      switch (displaySize) {
         case S_SMALL: case S_MEDIUM: case S_LARGE: break;
         default: valid = false;
      }
      switch (fontFamily) {
         case TIMES: case HELV: case MONO: case DIALOG: break;
         default: valid = false;
      }
      switch (fontStyle) {
         case NORM: case BOLD: case ITAL: case BOIT: break;
         default: valid = false;
      }
      if ((sizeArrayIndex < 0) || (sizeArrayIndex > 12)) {
         valid = false;
      }
      if (valid) {
         formButtonFontVals[displaySize]  = sizeArrayIndex;
         formButtonFamVals[displaySize]   = fontFamily;
         formButtonStyleVals[displaySize] = fontStyle;
         formTextFontVals[displaySize]    = sizeArrayIndex;
         formTextFamVals[displaySize]     = fontFamily;
         formTextStyleVals[displaySize]   = fontStyle;  
      }
   }


   /**
   * Sets a default font to be used within form Buttons and ComboBoxes. This method only applies
   * to <code>THREEDEE</code> and <code>FLUSH</code> form components. <code>LOOKANDFEEL</code>
   * components will not be affected.
   * This method takes three different size arguments:
   * <UL type=disc><LI><code>CalCons.S_SMALL
   * <LI>CalCons.S_MEDIUM
   * <LI>CalCons.S_LARGE</code><P>
   * </UL>
   * In most cases you will call this method with the <code>CalCons.S_LARGE</code> argument.
   * The <code>S_SMALL</code> and <code>S_MEDIUM</code> arguments are used in special cases
   * relating to the <code>optimizeDisplay</code> feature of <code>CalHTMLPane</code>.
   * <P>The font point sizes supported in CalPane are: 10, 11, 12, 13, 14, 16, 18, 20, 22, 24, 28, 36, 48
   * <P>If the size of the font sent to this method does not exactly match a supported size, then the
   * closest supported value will be used.
   * @see     calpa.html.CalHTMLPreferences#setOptimizeDisplay
   */
   public void setDefaultButtonFont(int displaySize, Font font) {
   
      int n;
      if (((displaySize == S_SMALL) || (displaySize == S_MEDIUM) || (displaySize == S_LARGE)) &&
                                                                                   (font != null)) {
         //ANDY: may need to reactivate following when system fonts supported
         //int faceIndex = CalHTMLManager.f.getFontFace(font.getFontName());
         int faceIndex = CalHTMLManager.f.getFontFace(font.getName());
         if (faceIndex >= 0) {
            formButtonFamVals[displaySize] = faceIndex;
            if ((n = getFontSize(font.getSize())) >= 0) {
               formButtonFontVals[displaySize] = n;
            }
            formButtonStyleVals[displaySize] = getFontStyle(font.getStyle());            
         }
      }
   }


   /**
   * Returns a default font being used within <code>THREEDEE</code> and <code>FLUSH</code>
   * Buttons and ComboBoxes.
   * @see calpa.html.CalHTMLPreferences#setDefaultButtonFont
   */
   public Font getDefaultButtonFont(int displaySize) {

      if ((displaySize == S_SMALL) || (displaySize == S_MEDIUM) || (displaySize == S_LARGE)) {
         CalHTMLManager.f.checkFont(formButtonFamVals[displaySize],
                            formButtonStyleVals[displaySize], formButtonFontVals[displaySize]);
         return CalHTMLManager.f.fonts[formButtonFamVals[displaySize]]
                           [formButtonStyleVals[displaySize]][formButtonFontVals[displaySize]];
      }
      return null;
   }


   /**
   * Sets a default font to be used within form TextFields, TextAreas and Lists. This method only applies
   * to <code>THREEDEE</code> and <code>FLUSH</code> form components. <code>LOOKANDFEEL</code>
   * components will not be affected.
   * This method takes three different size arguments:
   * <UL type=disc><LI><code>CalCons.S_SMALL
   * <LI>CalCons.S_MEDIUM
   * <LI>CalCons.S_LARGE</code><P>
   * </UL>
   * In most cases you will call this method with the <code>CalCons.S_LARGE</code> argument.
   * The <code>S_SMALL</code> and <code>S_MEDIUM</code> arguments are used in special cases
   * relating to the <code>optimizeDisplay</code> feature of <code>CalHTMLPane</code>.
   * <P>The font point sizes supported in CalPane are: 10, 11, 12, 13, 14, 16, 18, 20, 22, 24, 28, 36, 48
   * <P>If the size of the font sent to this method does not exactly match a supported size, then the
   * closest supported value will be used.
   * @see     calpa.html.CalHTMLPreferences#setOptimizeDisplay
   */
   public void setDefaultFormTextFont(int displaySize, Font font) {
   
      int n;
      if (((displaySize == S_SMALL) || (displaySize == S_MEDIUM) || (displaySize == S_LARGE)) &&
                                                                                   (font != null)) {
         //ANDY: may need to reactivate following when system fonts supported
         //int faceIndex = CalHTMLManager.f.getFontFace(font.getFontName());
         int faceIndex = CalHTMLManager.f.getFontFace(font.getName());
         if (faceIndex >= 0) {
            formTextFamVals[displaySize] = faceIndex;
            if ((n = getFontSize(font.getSize())) >= 0) {
               formTextFontVals[displaySize] = n;
            }
            formTextStyleVals[displaySize] = getFontStyle(font.getStyle());            
         }
      }
   }


   /**
   * Returns a default font being used within <code>THREEDEE</code> and <code>FLUSH</code>
   * Textfields, TextAreas and Lists.
   * @see calpa.html.CalHTMLPreferences#setDefaultFormTextFont
   */
   public Font getDefaultFormTextFont(int displaySize) {

      if ((displaySize == S_SMALL) || (displaySize == S_MEDIUM) || (displaySize == S_LARGE)) {
         CalHTMLManager.f.checkFont(formTextFamVals[displaySize],
                            formTextStyleVals[displaySize], formTextFontVals[displaySize]);
         return CalHTMLManager.f.fonts[formTextFamVals[displaySize]]
                           [formTextStyleVals[displaySize]][formTextFontVals[displaySize]];
      }
      return null;
   }


   private int getFontSize(int n) {
   
      int a[] = CalHTMLManager.f.fontSizes;
      int diff  = 999;
      int size  = -1;
      for (int i=a.length-1; i>=0; i--) {
         if (a[i] == n) {
            size = i;
            break;
         } else {
            if (Math.abs(a[i] - n) < diff) {
               size = i;
               diff = Math.abs(a[i] - n);
            }
         }
      }
      return ((size >= 0) && (size < a.length)) ? size : -1;
   }      


   private int getFontStyle(int n) {

      switch (n) {
         case Font.BOLD     : return BOLD;
         case Font.ITALIC   : return ITAL;
         case (Font.BOLD |
               Font.ITALIC) : return BOIT;
      }
      return NORM;
   }
   
   
   /**
   * Sets a default color. Permitted values are:
   * <P>
   * <TABLE border align=center cellpadding=3>
   * <TR><TH>attributeType</TH><TH>Default</TH><TH>Description</TH></TR>
   * <TR><TD><code>CalCons.A_BGCOLOR</code></TD>
   * <TD><code>Color.white</code></TD><TD>Document background color</TD></TR>
   * <TR><TD><code>CalCons.A_TEXT</code></TD>
   * <TD><code>Color.black</code></TD><TD>Document text color</TD></TR>
   * <TR><TD><code>CalCons.A_LINK</code></TD>
   * <TD><code>Color(0xOOOOEA)</code></TD><TD>Normal link color</TD></TR>
   * <TR><TD><code>CalCons.A_VLINK</code></TD>
   * <TD><code>Color(0x800080)</code></TD><TD>Visited link color</TD></TR>
   * <TR><TD><code>CalCons.A_ALINK</code></TD>
   * <TD><code>Color.red</code></TD><TD>Active link color</TD></TR>
   * <TR><TD><code>CalCons.FORM_CONTROL</code></TD>
   * <TD><code>UIControl</code></TD><TD>Control color of form components</TD></TR>
   * <TR><TD><code>CalCons.FORM_HIGHLIGHT</code></TD>
   * <TD><code>UIHighlight</code></TD><TD>Highlight color of form components</TD></TR>
   * <TR><TD><code>CalCons.FORM_SHADOW</code></TD>
   * <TD><code>UIShadow</code></TD><TD>Shadow color of form components</TD></TR>
   * <TR><TD><code>CalCons.FORM_TEXT_BACKGROUND</code></TD>
   * <TD><code>Color.white</code></TD><TD>Background color within form components such
   * as textfields</TD></TR>
   * <TR><TD><code>CalCons.FORM_TEXT_COLOR</code></TD>
   * <TD><code>Color.black</code></TD><TD>Text color within form components such
   * as textfields</TD></TR>
   * <TR><TD><code>CalCons.TAB_FOCUS_BACKGROUND</code></TD>
   * <TD><code>Color(0x300060)</code></TD><TD>Background color of hyperlinks which have
   * keyboard focus</TD></TR>
   * <TR><TD><code>CalCons.TAB_FOCUS_FOREGROUND</code></TD>
   * <TD><code>Color(0xFFFFD0)</code></TD><TD>Foreground color of hyperlinks which have
   * keyboard focus</TD></TR></TABLE>
   * <P>This method should be called prior to passing the
   * <code>CalHTMLPreferences</code> to a <code>CalHTMLPane</code>. Calls after this point
   * will be ignored.
   */
   public void setDefaultColor(int attributeType, Color c) {
   
      if ((allowChanges) && (c != null)) {
         int n = CalColor.getColor(c.getRGB());
         switch (attributeType) {
            case A_BGCOLOR           : bgcolor            = n; break;
            case A_TEXT              : textcolor          = n; break;
            case A_LINK              : linkcolor          = n; break;
            case A_VLINK             : vlinkcolor         = n; break;
            case A_ALINK             : alinkcolor         = n; break;
            case FORM_CONTROL        : formControl        = n; break;
            case FORM_HIGHLIGHT      : formHighlight      = n; break;
            case FORM_SHADOW         : formShadow         = n; break;
            case FORM_TEXT_BACKGROUND: formTextBackground = n; break;
            case FORM_TEXT_COLOR     : formTextColor      = n; break;
            case TAB_FOCUS_BACKGROUND: linkTabBackground  = n; break;
            case TAB_FOCUS_FOREGROUND: linkTabForeground  = n; break;
         }
      }
   }
   

   /**
   * Returns a default color used by a <code>CalHTMLPane</code> to render documents.
   * See <code>setDefaultColor()</code> for allowed attributeTypes
   * @see     calpa.html.CalHTMLPreferences#setDefaultColor
   */
   public Color getDefaultColor(int attributeType) {

      switch (attributeType) {
         case A_BGCOLOR            : return CalColor.colors[bgcolor];
         case A_TEXT               : return CalColor.colors[textcolor];
         case A_LINK               : return CalColor.colors[linkcolor];
         case A_VLINK              : return CalColor.colors[vlinkcolor];
         case A_ALINK              : return CalColor.colors[alinkcolor];
         case FORM_CONTROL         : return CalColor.colors[formControl];
         case FORM_HIGHLIGHT       : return CalColor.colors[formHighlight];
         case FORM_SHADOW          : return CalColor.colors[formShadow];
         case FORM_TEXT_BACKGROUND : return CalColor.colors[formTextBackground];
         case FORM_TEXT_COLOR      : return CalColor.colors[formTextColor];
         case TAB_FOCUS_BACKGROUND : return CalColor.colors[linkTabBackground];
         case TAB_FOCUS_FOREGROUND : return CalColor.colors[linkTabForeground];
      }
      return null;
   }

   /**
   * Sets the rendering style of <code>FORM</code> components within a <code>CalHTMLPane</code>. 
   * The permitted style values are:<P>
   * <UL type=disc><LI><code>CalCons.USE_LOOK_AND_FEEL</code>
   * <LI><code>CalCons.USE_CALPA_THREEDEE</code>
   * <LI><code>CalCons.USE_CALPA_FLUSH</code>
   * </UL>
   * <P>The <code>USE_LOOK_AND_FEEL</code> argument will use standard Swing components based
   * on the currently installed Look&Feel. Generally these will provide an acceptable
   * display, but with certain document background colors these components lose
   * style features due to a clash between the background color of the document
   * and the control, shadow, and highlight color of the component.
   * <P>The <code>USE_CALPA_THREEDEE</code> and <code>USE_CALPA_FLUSH</code> styles have
   * been incorporated to help overcome such a situation. These components support
   * author-specific color attributes which can be made to blend satisfactorily with
   * the surrounding document color.
   * <P>Note that setting the default style via this method does not prevent the
   * document author from setting individual styles within the document itself. Styles
   * can be freely mixed by specifying style attributes within the relevant document
   * form tags.
   * <P>Although this method can be called at any time during program execution,
   * existing form components will remain in their original style.
   * <P>The default rendering style of form components is <code>USE_CALPA_THREEDEE</code>
   * <P>See the README.TXT file accompanying this document for more details on form rendering.
   * @param style a value which determines the default rendering style of HTML form components
   */
   public void setFormRenderingStyle(int style) {
   
      if ((style == USE_LOOK_AND_FEEL) || (style == USE_CALPA_FLUSH) || (style == USE_CALPA_THREEDEE)) {
         formRendering = style;
      }
   }


   /**
   * Returns the default rendering style of <code>FORM</code> components within a
   * <code>CalHTMLPane</code>.
   * @return  a value indicating the default rendering style of HTML form components
   * @see     calpa.html.CalHTMLPreferences#setFormRenderingStyle
   */     
   public int getFormRenderingStyle() {
   
      return formRendering;
   }


   /**
   * Sets the default spacing in pixels between frames within a Frameset document. The default
   * setting is 4 pixels.
   * @param n the default spacing in pixels between HTML frames in a Frameset document
   */     
   public void setDefaultFrameSpacing(int n) {
   
      frameSpacing = n;
   }


   /**
   * Returns the default spacing in pixels between frames within a Frameset document.
   * @return the default spacing in pixels between HTML frames in a Frameset document
   */     
   public int getDefaultFrameSpacing() {
   
      return frameSpacing;
   }

   /**
   * Sets the default horizontal margin of a document. The default setting is 6 pixels.
   * @param n the default horizontal margin in pixels between a document and the border of the frame it
   * resides in.
   */     
   public void setDefaultMarginwidth(int n) {
   
      this.marginwidth = n;
   }


   /**
   * Returns the default horizontal margin of a document.
   * @return the default horizontal margin in pixels between a document and the border of the frame it
   * resides in.
   */     
   public int getDefaultMarginwidth() {
   
      return marginwidth;
   }


   /**
   * Sets the default vertical margin of a document. The default setting is 6 pixels.
   * @param n the default vertical margin in pixels between a document and the border of the frame it
   * resides in.
   */     
   public void setDefaultMarginheight(int n) {
   
      marginheight = n;
   }


   /**
   * Returns the default vertical margin of a document.
   * @return the default vertical margin in pixels between a document and the border of the frame it
   * resides in.
   */     
   public int getDefaultMarginheight() {
   
      return marginheight;
   }


   /**
   * Sets the <code>URL</code> which will be accessed if a <code>CalHTMLPane's
   * goHome()</code> method is called. The default setting is http://www.w3.org
   * @param url the default <code>URL</code> to be accessed.
   */
   public void setHomeURL(URL url) {
   
      if (url != null) {
         homeURL = url;
      }
   }

   
   /**
   * Returns the <code>URL</code> which will accessed if a <code>CalHTMLPane's
   * goHome()</code> method is called
   * @return the default <code>URL</code> which will be accessed.
   */        
   public URL getHomeURL() {
   
      return homeURL;
   }
   
   
   /**
   * Enables/disables the underlining of hyperlinks within a <code>CalHTMLPane</code>.
   * The default value is true.
   * @param b a flag indicating whether hyperlinks should be underlined
   */     
   public void setUnderlineLinks(boolean b) {
   
      underlineLinks = b;
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will underline hyperlinks.
   * @return <code>true</code> if hyperlinks will be underlined
   */        
   public boolean isUnderlineLinksEnabled() {
   
      return underlineLinks;
   }


   /**
   * Determines whether a hyperlink will only be marked as such when it has mouse focus. This is
   * commonly used in help systems where it is obvious that all text is a hyperlink entry, and users
   * only need to know which hyperlink is currently active.
   * The default value is false.
   * @param b a flag indicating whether hyperlinks will only be marked as such if they have mouse focus
   */     
   public void setShowHyperlinkOnMouseFocus(boolean b) {
   
      hyperlinkHover = b;
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will only show a hyperlink when the link has mouse focus.
   * @return <code>true</code> if a hyperlink will only be marked as such when it has mouse focus
   */        
   public boolean isShowHyperlinkOnMouseFocusEnabled() {
   
      return hyperlinkHover;
   }


   /**
   * Enables/disables the automatic loading of images within a <code>CalHTMLPane</code>.
   * The default value is true.
   * @param b a flag indicating whether images within documents should be loaded and displayed
   */     
   public void setLoadImages(boolean b) {
   
      loadImages = b;
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will
   * automatically load images within documents.
   * @return <code>true</code> if images within documents will be loaded and displayed
   */        
   public boolean isLoadImagesEnabled() {
   
      return loadImages;
   }


   /**
   * Enables/disables the automatic display of error dialogs within a <code>CalHTMLPane</code>.
   * At present such errors will most commonly arise from a failure to navigate to a
   * selected URL. The default value is true. If set to false, errors will not be displayed
   * to the screen but will still be passed to the resident <code>CalHTMLObserver</code>
   * @param b a flag indicating whether an dialog should be displayed if an error occurs within a Pane.
   */     
   public void setDisplayErrorDialogs(boolean b) {
   
      displayErrors = b;
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will automatically display a dialog when
   * errors such as failed hyperlinks occur.
   * @return <code>true</code> if an error dialog will be displayed if an error occurs within a Pane.
   */        
   public boolean isDisplayErrorDialogsEnabled() {
   
      return displayErrors;
   }


   /**
   * Enables/disables the display of a diagnostic navigation bar. The default value is false.
   * <P>The navigation bar is a legacy of testing the component and is very basic,
   * but it has not been removed as it can be useful at times.
   * It is expected that users will implement their own custom navigation controls if required.
   * <P>The NavBar operates independently of the <code>CalHTMLObserver</code> monitoring the
   * Pane, and will not interfere with updates to that object. Note also that the NavBar controls do not
   * receive keyboard focus when tabbing. 
   * <P>This method should be called prior to passing the
   * <code>CalHTMLPreferences</code> to a <code>CalHTMLPane</code>. Calls after this point
   * will be ignored.
   * @param b a flag indicating whether a <code>CalHTMLPane's</code> test navigation controls should be
   * displayed. 
   */
   public void setShowTestNavBar(boolean b) {
   
      if (allowChanges) {
         showNavBar = b;
      }
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will display a diagnostic navigation bar on startup.
   * @return <code>true</code> if a <code>CalHTMLPane</code> will display test navigation controls.
   */
   public boolean isShowTestNavBarEnabled() {
   
      return showNavBar;
   }


   /**
   * Enables/disables the automatic navigation of activated hyperlinks in a <code>CalHTMLPane</code>.
   * The default value is true. If set to false, activated hyperlinks will not be followed
   * but the event will still be passed to the resident <code>CalHTMLObserver</code>
   * @param b a flag indicating whether hyperlinks should be followed if activated by mouse or keyboard.
   */     
   public void setAutomaticallyFollowHyperlinks(boolean b) {
   
      followLinks = b;
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will automatically navigate activated hyperlinks.
   * @return <code>true</code> if hyperlinks should be followed if activated by keyboard or mouse.
   */        
   public boolean isAutomaticallyFollowHyperlinksEnabled() {
   
      return followLinks;
   }


   /**
   * Enables/disables the automatic processing of GET and POST html forms  
   * within a <code>CalHTMLPane</code>. The default value is true. If set to false,
   * form submissions will still be passed to the resident <code>CalHTMLObserver</code>
   * @param b a flag indicating whether http GET and POST form submissions should be handled automatically
   * by a <code>CalHTMLPane</code>
   */
   public void setHandleFormSubmission(boolean b) {
   
      handleFormSubmission = b;
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will
   * automatically handle GET and POST form submissions.
   * @return <code>true</code> if http GET and POST form submissions will be handled automatically
   * by a <code>CalHTMLPane</code>
   */        
   public boolean isHandleFormSubmissionEnabled() {
   
      return handleFormSubmission;
   }


   /**
   * Enables/disables the showing of a warning message before a form POST submission  
   * is made. The default value is true. This setting only has any meaning if the Pane is
   * handling form submissions.
   * @param b a flag indicating whether a warning message will displayed before an http form POST
   * submission
   */
   public void setShowWarningBeforePost(boolean b) {
   
      postWarning = b;
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will show a warning to the user before a form POST
   * submission is made.
   * @return <code>true</code> if a warning message will displayed before an http form POST
   * submission
   */        
   public boolean isShowWarningBeforePostEnabled() {
   
      return postWarning;
   }


   /**
   * Enables/disables the automatic creation of a new <code>CalHTMLPane</code> if
   * specified by an HTML tag. The default value is true. If set to false, a request
   * for a new frame will be sent to the resident <code>CalHTMLObserver</code>
   * @param b a flag indicating whether a new <code>CalHTMLPane</code> should automatically be
   * displayed if specified by an HTML tag.
   */
   public void setHandleNewFrames(boolean b) {
   
      handleNewFrames = b;
   }

   
   /**
   * Returns whether a new <code>CalHTMLPane</code> will
   * automatically be created if specified by an HTML tag.
   * @return <code>true</code> if a new <code>CalHTMLPane</code> will automatically be
   * displayed if specified by an HTML tag.
   */        
   public boolean isHandleNewFramesEnabled() {
   
      return handleNewFrames;
   }


   /**
   * Determines whether a <code>CalHTMLPane</code> or one of its subframes will request keyboard focus
   * if the mouse is clicked within it. The default value is false.
   * @param b a flag indicating whether a <code>CalHTMLPane</code> frame should call
   * <code>requestFocus()</code> if the mouse is clicked within it.
   */
   public void setRequestFocusOnMouseClick(boolean b) {
   
      focusOnClick = b;
   }

   
   /**
   * Returns whether a <code>CalHTMLPane</code> will request keyboard focus if the mouse is
   * clicked within it.
   * @return <code>true</code> if a <code>CalHTMLPane</code> frame will call
   * <code>requestFocus()</code> if the mouse is clicked within it.
   */        
   public boolean isRequestFocusOnMouseClickEnabled() {
   
      return focusOnClick;
   }

}
