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

class CalFormItem implements CalCons {

   CalFO[] options;
   int     tagType;
   int     formNo;
   int     tagPos;
   int     type;
   int     size;
   int     cols;
   int     rows;
   int     width;
   int     height;
   int     hspace;
   int     vspace;
   int     maxlength;
   int     tabIndex;
   int     align;
   int     border;
   int     bgcolor;
   int     bordercolor;
   int     arrowcolor;
   int     highlight;
   int     shadow;
   int     textcolor;
   int     style;
   int     marginwidth;
   int     marginheight;
   int     optionIndex;
   int     startPos;
   int     endPos;
   int     charStart;
   int     charEnd;
   char    accesskey;
   String  initValue;
   String  name;
   String  forTarget;
   String  id;
   String  jclass;
   String  jname;
   String  altText;
   String  acceptTypes;
   String  classid;
   boolean checked;
   boolean disabled;
   boolean readonly;
   boolean multiple;
   boolean hasImages;
   boolean uiscroll;
   boolean dropshadow;
     
   CalFormItem(CalDoc doc, int attrStart, int attrEnd, CalFonts f, int tagType, int formNo, int tagPos) {

      this.tagType = tagType;
      this.formNo  = formNo;
      this.tagPos  = tagPos;
      String s;
      type  = V_TEXT;
      align = V_FORM;
      style  = -1;
      width  = -1;
      height = -1;
      accesskey = END;
      if (tagType == LABEL) {
         marginwidth  = 3;
         marginheight = 2;
      } else if (tagType == BUTTON) {
         type = V_SUBMIT;
         marginwidth  = 5;
         marginheight = 2;
      } else {
         marginwidth  = -1;
         marginheight = -1;
      }
      for (int i=attrStart; i<attrEnd; i++) {
         switch (doc.attrTypes[i]) {
            case A_TYPE       : switch (doc.attrArgs[i]) {
                                   case V_PASSWORD: case V_CHECKBOX: case V_RADIO: case V_SUBMIT:
                                   case V_RESET: case V_FILE: case V_HIDDEN: case V_IMAGE: case V_BUTTON:
                                   case V_JCOMPONENT:
                                      type = doc.attrArgs[i];
                                }
                                break;
            case A_ALIGN      : switch (doc.attrArgs[i]) {
                                   case V_LEFT: case V_RIGHT: case V_TOP: case V_MIDDLE: case V_ABSBOTTOM:
                                   case V_ABSMIDDLE: case V_ABSTOP: case V_BOTTOM: align = doc.attrArgs[i];
                                }
                                break;
            case A_NAME       : name      = doc.attrStrings[i]; break;
            case A_ID         : id        = doc.attrStrings[i]; break;
            case A_JCLASS     : jclass    = doc.attrStrings[i]; break;
            case A_JNAME      : jname     = doc.attrStrings[i]; break;
            case A_FOR        : forTarget = doc.attrStrings[i]; break;
            case A_CLASSID    : classid   = doc.attrStrings[i]; break;
            case A_VALUE      : initValue = doc.attrStrings[i];
                                if (("!").equals(initValue)) {
                                   initValue = "";
                                }
                                break;
            case A_CHECKED    : checked      = true; break;
            case A_DISABLED   : disabled     = true; break;
            case A_READONLY   : readonly     = true; break;
            case A_MULTIPLE   : multiple     = true; break;
            case A_UISCROLL   : uiscroll     = true; break;
            case A_DROPSHADOW : dropshadow   = true; break;
            case A_SIZE       : size = Math.abs(doc.attrVals[i]); break;
            case A_BORDER     : border       = doc.attrVals[i]; break;
            case A_BGCOLOR    : bgcolor      = doc.attrVals[i]; break;
            case A_BORDCOLOR  : bordercolor  = doc.attrVals[i]; break;
            case A_BORDCOLORLT: highlight    = doc.attrVals[i]; break;
            case A_BORDCOLORDK: shadow       = doc.attrVals[i]; break;
            case A_ARROWCOLOR : arrowcolor   = doc.attrVals[i]; break;
            case A_TEXT       : textcolor    = doc.attrVals[i]; break;
            case A_TABINDEX   : tabIndex     = doc.attrVals[i]; break;
            case A_MAXLENGTH  : maxlength    = doc.attrVals[i]; break;
            case A_COLS       : cols         = doc.attrVals[i]; break;
            case A_ROWS       : rows         = doc.attrVals[i]; break;
            case A_MARGINW    : marginwidth  = doc.attrVals[i]; break;
            case A_MARGINH    : marginheight = doc.attrVals[i]; break;
            case A_STYLE      : switch (doc.attrArgs[i]) {
                                   case V_FLUSH       : style = USE_CALPA_FLUSH;    break;
                                   case V_THREEDEE    : style = USE_CALPA_THREEDEE; break;
                                   case V_LOOKANDFEEL : style = USE_LOOK_AND_FEEL;  break;
                                               default: style = -1;
                                }
                                break;
            case A_ACCESSKEY  : if (doc.attrStrings[i] != null) {
                                   accesskey = doc.attrStrings[i].charAt(0);
                                } 
                                break;
            case A_WIDTH      : if (doc.attrVals[i] >= 0) {
                                   width = doc.attrVals[i];
                                }
                                break;
            case A_HEIGHT     : if (doc.attrVals[i] >= 0) {
                                   height = doc.attrVals[i];
                                }
                                break;
            case A_HSPACE     : hspace = doc.attrVals[i]; break;
            case A_VSPACE     : vspace = doc.attrVals[i]; break;
         }
      }
      if ((type == V_RESET) && (name == null)) {
         name = "reset";
      }
   }   


   void setInitialValue(String s) {
   
      initValue = s;
   }
   
   
   void addOption(CalFO opt) {

      if (options == null) {
         options = new CalFO[5];
         options[optionIndex] = opt;
      } else {
         if (optionIndex == options.length) {
            CalFO[] a = new CalFO[2 * options.length];
            System.arraycopy(options, 0, a, 0, options.length);
            options = a;
         }
         options[optionIndex] = opt;
      }
      optionIndex++;
   }
}
