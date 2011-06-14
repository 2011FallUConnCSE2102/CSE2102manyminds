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

public class CalLP implements CalCons {

   static char[][] tags = {
      {'A'},
      {'A','R','E','A'},
      {'A','P','P','L','E','T'},
      {'A','D','D','R','E','S','S'},
      {'B'},
      {'B','R'},
      {'B','I','G'},
      {'B','A','S','E'},
      {'B','O','D','Y'},
      {'B','U','T','T','O','N'},
      {'B','A','S','E','F','O','N','T'},
      {'B','L','O','C','K','Q','U','O','T','E'},
      {'C','O','L'},
      {'C','I','T','E'},
      {'C','O','D','E'},
      {'C','E','N','T','E','R'},
      {'C','A','P','T','I','O','N'},
      {'C','O','L','G','R','O','U','P'},
      {'D','D'},
      {'D','L'},
      {'D','T'},
      {'D','F','N'},
      {'D','I','R'},
      {'D','I','V'},
      {'E','M'},
      {'F','O','N','T'},
      {'F','O','R','M'},
      {'F','R','A','M','E'},
      {'F','I','E','L','D','S','E','T'},
      {'F','R','A','M','E','S','E','T'},
      {'H','1'},
      {'H','2'},
      {'H','3'},
      {'H','4'},
      {'H','5'},
      {'H','6'},
      {'H','R'},
      {'H','T','M','L'},
      {'I'},
      {'I','M','G'},
      {'I','N','P','U','T'},
      {'I','F','R','A','M','E'},
      {'I','S','I','N','D','E','X'},
      {'K','B','D'},
      {'L','I'},
      {'L','A','B','E','L'},
      {'L','E','G','E','N','D'},
      {'M','A','P'},
      {'M','E','N','U'},
      {'N','O','S','C','R','I','P','T'},
      {'O','L'},
      {'O','P','T','I','O','N'},
      {'O','B','J','E','C','T'},
      {'P'},
      {'P','R','E'},
      {'P','A','R','A','M'},
      {'S','U','B'},
      {'S','U','P'},
      {'S','A','M','P'},
      {'S','P','A','N'},
      {'S','M','A','L','L'},
      {'S','T','Y','L','E'},
      {'S','C','R','I','P','T'},
      {'S','E','L','E','C','T'},
      {'S','T','R','I','K','E'},
      {'S','T','R','O','N','G'},
      {'T','D'},
      {'T','H'},
      {'T','R'},
      {'T','T'},
      {'T','A','B','L','E'},
      {'T','B','O','D','Y'},
      {'T','F','O','O','T'},
      {'T','H','E','A','D'},
      {'T','I','T','L','E'},
      {'T','E','X','T','A','R','E','A'},
      {'U'},
      {'U','L'},
      {'V','A','R'}
   };
      
   static short[] tagCodes = {
      A, AREA, APPLET, ADDRESS, B, BR, BIG, BASE, BODY, BUTTON, BASEFONT, BLOCKQUOTE, COL, CITE, CODE,
      CENTER, CAPTION, COLGROUP, DD, DL, DT, DIR, DFN, DIV, EM, FONT, FORM, FRAME, FIELDSET, FRAMESET,
      H1, H2, H3, H4, H5, H6, HR, HTML, I, IMG, INPUT, IFRAME, ISINDEX, KBD, LI, LABEL, LEGEND, MAP,
      MENU, NOSCRIPT, OL, OPTION, OBJECT, P, PRE, PARAM, SUB, SUP, SAMP, SPAN, SMALL,
      STYLE, SCRIPT, SELECT, STRIKE, STRONG, TD, TH, TR, TT, TABLE, TBODY, TFOOT, THEAD, TITLE,
      TEXTAREA, U, UL, VAR
   };
   
   
   static char[][] attrs = {
      {'a','l','t'},
      {'a','l','i','g','n'},
      {'a','l','i','n','k'},
      {'a','c','c','e','p','t'},
      {'a','c','t','i','o','n'},
      {'a','c','c','e','s','s','k','e','y'},
      {'a','r','r','o','w','c','o','l','o','r'},
      {'b','o','r','d','e','r'},
      {'b','g','c','o','l','o','r'},
      {'b','a','c','k','g','r','o','u','n','d'},
      {'b','o','r','d','e','r','c','o','l','o','r'},
      {'b','o','r','d','e','r','c','o','l','o','r','d','a','r','k'},
      {'b','o','r','d','e','r','c','o','l','o','r','l','i','g','h','t'},
      {'c','o','l','s'},
      {'c','l','a','s','s'},
      {'c','l','e','a','r'},
      {'c','o','l','o','r'},
      {'c','o','o','r','d','s'},
      {'c','h','e','c','k','e','d'},
      {'c','l','a','s','s','i','d'},
      {'c','o','l','s','p','a','n'},
      {'c','e','l','l','p','a','d','d','i','n','g'},
      {'c','e','l','l','s','p','a','c','i','n','g'},
      {'d','i','s','a','b','l','e','d'},
      {'d','r','o','p','s','h','a','d','o','w'},
      {'e','n','c','t','y','p','e'},
      {'f','o','r'},
      {'f','a','c','e'},
      {'f','r','a','m','e'},
      {'f','r','a','m','e','b','o','r','d','e','r'},
      {'f','r','a','m','e','s','p','a','c','i','n','g'},
      {'h','r','e','f'},
      {'h','e','i','g','h','t'},
      {'h','s','p','a','c','e'},
      {'i','d'},
      {'j','n','a','m','e'},
      {'j','c','l','a','s','s'},
      {'l','i','n','k'},
      {'l','a','b','e','l'},
      {'m','e','t','h','o','d'},
      {'m','u','l','t','i','p','l','e'},
      {'m','a','x','l','e','n','g','t','h'},
      {'m','a','r','g','i','n','w','i','d','t','h'},
      {'m','a','r','g','i','n','h','e','i','g','h','t'},
      {'n','a','m','e'},
      {'n','o','h','r','e','f'},
      {'n','o','w','r','a','p'},
      {'n','o','s','h','a','d','e'},
      {'n','o','r','e','s','i','z','e'},
      {'r','o','w','s'},
      {'r','u','l','e','s'},
      {'r','o','w','s','p','a','n'},
      {'r','e','a','d','o','n','l','y'},
      {'s','r','c'},    
      {'s','i','z','e'},        
      {'s','p','a','n'},
      {'s','h','a','p','e'},
      {'s','t','a','r','t'},
      {'s','t','y','l','e'},
      {'s','e','l','e','c','t','e','d'},
      {'s','c','r','o','l','l','i','n','g'},
      {'t','e','x','t'},
      {'t','y','p','e'},
      {'t','a','r','g','e','t'},
      {'t','a','b','i','n','d','e','x'},
      {'u','s','e','m','a','p'},
      {'u','i','s','c','r','o','l','l','c','o','l','o','r','s'},
      {'v','a','l','u','e'},
      {'v','l','i','n','k'},
      {'v','a','l','i','g','n'},
      {'v','s','p','a','c','e'},
      {'w','i','d','t','h'}
   };
            
   static short[] attrCodes = {
      A_ALT, A_ALIGN, A_ALINK, A_ACCEPT, A_ACTION, A_ACCESSKEY, A_ARROWCOLOR, A_BORDER,
      A_BGCOLOR, A_BACKGROUND,
      A_BORDCOLOR, A_BORDCOLORDK, A_BORDCOLORLT, A_COLS, A_CLASS, A_CLEAR, A_COLOR, A_COORDS, A_CHECKED,
      A_CLASSID, A_COLSPAN, A_CELLPADDING, A_CELLSPACING, A_DISABLED, A_DROPSHADOW, A_ENCTYPE, A_FOR,
      A_FACE, A_FRAME, A_FRAMEBORDER, A_FRAMESPACING, A_HREF, A_HEIGHT, A_HSPACE, A_ID, A_JNAME, A_JCLASS,
      A_LINK, A_LABEL, A_METHOD, A_MULTIPLE, A_MAXLENGTH,
      A_MARGINW, A_MARGINH, A_NAME, A_NOHREF, A_NOWRAP, A_NOSHADE, A_NORESIZE,
      A_ROWS, A_RULES, A_ROWSPAN, A_READONLY, A_SRC, A_SIZE, A_SPAN, A_SHAPE,
      A_START, A_STYLE, A_SELECTED, A_SCROLLING, A_TEXT, A_TYPE, A_TARGET, A_TABINDEX,
      A_USEMAP, A_UISCROLL, A_VALUE, A_VLINK, A_VALIGN, A_VSPACE, A_WIDTH
   };
   
   static short[] attrType = {
      AV_STRING,       //alt
      AV_ARG,          //align
      AV_COLOR,        //alink
      AV_STRING,       //accept
      AV_STRING,       //action
      AV_STRING,       //accesskey
      AV_COLOR,        //arrowcolor
      AV_LENGTH,       //border
      AV_COLOR,        //bgcolor
      AV_STRING,       //background
      AV_COLOR,        //bordercolor
      AV_COLOR,        //bordercolordark
      AV_COLOR,        //bordercolorlight
      AV_MULTIPLE,     //cols
      AV_STRING,       //class
      AV_ARG,          //clear
      AV_COLOR,        //color
      AV_STRING,       //coords
      AV_NONE,         //checked
      AV_STRING,       //classid
      AV_LENGTH,       //colspan
      AV_LENGTH,       //cellpadding
      AV_LENGTH,       //cellspacing
      AV_NONE,         //disabled
      AV_NONE,         //dropshadow
      AV_STRING,       //enctype
      AV_STRING,       //for
      AV_STRING,       //face
      AV_ARG,          //frame
      AV_STRING,       //frameborder
      AV_LENGTH,       //framespacing
      AV_STRING,       //href
      AV_LENGTH,       //height
      AV_LENGTH,       //hspace
      AV_STRING,       //id
      AV_STRING,       //jname
      AV_STRING,       //jclass
      AV_COLOR,        //link
      AV_STRING,       //label
      AV_ARG,          //method
      AV_NONE,         //multiple
      AV_LENGTH,       //maxlength
      AV_LENGTH,       //marginwidth
      AV_LENGTH,       //marginheight
      AV_STRING,       //name
      AV_NONE,         //nohref
      AV_NONE,         //nowrap
      AV_NONE,         //noshade
      AV_NONE,         //noresize
      AV_MULTIPLE,     //rows
      AV_ARG,          //rules
      AV_LENGTH,       //rowspan
      AV_NONE,         //readonly
      AV_STRING,       //src
      AV_LENGTH_NEG,   //size
      AV_LENGTH,       //span
      AV_ARG,          //shape
      AV_LENGTH,       //start
      AV_ARG,          //style
      AV_NONE,         //selected
      AV_ARG,          //scrolling
      AV_COLOR,        //text
      AV_MULTIPLE,     //type
      AV_STRING,       //target
      AV_LENGTH,       //tabindex
      AV_STRING,       //usemap
      AV_NONE,         //uiscrollcolors
      AV_MULTIPLE,     //value
      AV_COLOR,        //vlink
      AV_ARG,          //valign
      AV_LENGTH,       //vspace
      AV_LENGTH        //width
   };
   
   static char[][] args = {
      {'a','l','l'},
      {'a','u','t','o'},
      {'a','b','o','v','e'},
      {'a','b','s','t','o','p'},
      {'a','b','s','b','o','t','t','o','m'},
      {'a','b','s','m','i','d','d','l','e'},
      {'b','o','x'},
      {'b','e','l','o','w'},
      {'b','o','r','d','e','r'},
      {'b','o','t','t','o','m'},
      {'b','u','t','t','o','n'},
      {'b','a','s','e','l','i','n','e'},
      {'c','o','l','s'},
      {'c','e','n','t','e','r'},
      {'c','i','r','c','l','e'},
      {'c','h','e','c','k','b','o','x'},
      {'d','i','s','c'},
      {'d','e','f','a','u','l','t'},
      {'f','i','l','e'},
      {'f','l','u','s','h'},
      {'g','e','t'},
      {'g','r','o','u','p','s'},
      {'h','i','d','d','e','n'},
      {'h','s','i','d','e','s'},
      {'i','m','a','g','e'},
      {'j','f','o','r','m'},
      {'j','c','o','m','p','o','n','e','n','t'},
      {'l','h','s'},
      {'l','e','f','t'},
      {'l','o','o','k','a','n','d','f','e','e','l'},
      {'m','i','d','d','l','e'},
      {'n','o'},
      {'n','o','n','e'},
      {'p','o','l','y'},
      {'p','o','s','t'},
      {'p','a','s','s','w','o','r','d'},
      {'r','h','s'},
      {'r','e','c','t'},
      {'r','o','w','s'},
      {'r','a','d','i','o'},
      {'r','e','s','e','t'},
      {'r','i','g','h','t'},
      {'s','u','b','m','i','t'},
      {'s','q','u','a','r','e'},
      {'t','o','p'},
      {'t','e','x','t'},
      {'t','e','x','t','t','o','p'},
      {'t','h','r','e','e','d','e','e'},
      {'v','o','i','d'},
      {'v','s','i','d','e','s'},
      {'y','e','s'}
   };
   
   static short[] argCodes = {
      V_ALL, V_AUTO, V_ABOVE, V_ABSTOP, V_ABSBOTTOM, V_ABSMIDDLE, V_BOX,
      V_BELOW, V_BORDER, V_BOTTOM, V_BUTTON, V_BOTTOM, V_COLS, V_CENTER,
      V_CIRCLE, V_CHECKBOX, V_DISC, V_DEFAULT, V_FILE, V_FLUSH, V_GET, V_GROUPS, V_HIDDEN,
      V_HSIDES, V_IMAGE, V_JFORM, V_JCOMPONENT, V_LHS, V_LEFT, V_LOOKANDFEEL, V_MIDDLE,
      V_NO, V_NONE, V_POLY, V_POST, V_PASSWORD, V_RHS, V_RECT, V_ROWS, V_RADIO, V_RESET, V_RIGHT,
      V_SUBMIT, V_SQUARE, V_TOP, V_TEXT, V_TOP, V_THREEDEE, V_VOID, V_VSIDES, V_YES
   }; 
      
   static int[] tagStart   = new int[27];
   static int[] attrStart  = new int[27];
   static int[] argStart   = new int[27];
   static int[] tagLength  = new int[tags.length];
   static int[] attrLength = new int[attrs.length];
   static int[] argLength  = new int[args.length];

   
   static {

      int c = 0;
      int i, j;
      int index = -1;
      for (i=0; i<tags.length; i++) {
         tagLength[i] = tags[i].length;
         c = tags[i][0] - 65;
         if (c > index) {
            for (j=index+1; j<c; j++) {
               tagStart[j] = -i;
            } 
            tagStart[c] = i;
            index = c;
         }
      }
      for (i=c + 1; i<26; i++) {
         tagStart[i] = -tags.length;
      }
      tagStart[26] = tags.length;
      c = 0;
      index = -1;
      for (i=0; i<attrs.length; i++) {
         attrLength[i] = attrs[i].length;
         c = attrs[i][0] - 97;
         if (c > index) {
            for (j=index+1; j<c; j++) {
               attrStart[j] = -i;
            } 
            attrStart[c] = i;
            index = c;
         }
      }
      for (i=c + 1; i<26; i++) {
         attrStart[i] = -attrs.length;
      }
      attrStart[26] = attrs.length;
      
      c = 0;
      index = -1;
      for (i=0; i<args.length; i++) {
         argLength[i] = args[i].length;
         c = args[i][0] - 97;
         if (c > index) {
            for (j=index+1; j<c; j++) {
               argStart[j] = -i;
            } 
            argStart[c] = i;
            index = c;
         }
      }
      for (i=c + 1; i<26; i++) {
         argStart[i] = -args.length;
      }
      argStart[26] = args.length;
   }
   

}
