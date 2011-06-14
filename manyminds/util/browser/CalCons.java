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

import java.awt.Point;
import java.awt.Insets;
import java.awt.Dimension;

public interface CalCons {

   static String romanNum[][] = {
                                   {"m", "mm", "mmm"},
                                   {"c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm"},
                                   {"x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc"},
                                   {"i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix"}
                                };
   static String STR_DIALOG_MESSAGE1 =
   "<TABLE width=100% bgcolor=maroon border=1 rules=none cellspacing=0><TR><TD><FONT face=helvetica color=white><B>";
   static String STR_DIALOG_MESSAGE2 =
   "</B></FONT></TD></TR></TABLE><TABLE width=100% cellspacing=8><TR><TD align=center>";
   static String STR_DIALOG_MESSAGE3 = "</TD></TR></TABLE>";
   static String STR_WARNING_IMAGE1 = "<IMG jname=cal_warning align=absbottom vspace=2><code>&nbsp;</code>";
   static String STR_CONNECT_ERROR1 = "Unable to connect";
   static String STR_CONNECT_ERROR2 = "<B>Could not connect to:</B>";
   static String STR_CONNECT_ERROR3 = "<B>Could not find a valid URL</B>";
   static String STR_CONNECT_ERROR4 = "Connection terminated";
   static String STR_CONNECT_ERROR5 = "<B>Unable to load document</B>";
   static String STR_INFO_MESSAGE1  = "Unsecure transmission";
   static String STR_INFO_MESSAGE2  = "The information you are submitting<BR>";
   static String STR_INFO_MESSAGE3  = "will not be sent via a secure connection</B>";
   static String STR_OK_BUTTON =
   "<FORM method=jform action=close_dialog><BUTTON type=submit bgcolor=uicontrol marginwidth=5 marginheight=0>OK</BUTTON>";
   static String STR_FORM_POST_CONTROLS1 =
   "<FORM method=jform action=form_post_warning>";
   static String STR_FORM_POST_CONTROLS2 =
   "<BUTTON type=submit name=proceed bgcolor=uicontrol marginwidth=5 marginheight=0>Proceed</BUTTON>";
   static String STR_FORM_POST_CONTROLS3 =
   "<BUTTON type=submit name=cancel bgcolor=uicontrol marginwidth=5 marginheight=0>Cancel</BUTTON>";
   static String STR_FORM_POST_CONTROLS4 =
   "<INPUT type=checkbox name=show bgcolor=uicontrol> Don't show this message again";

   static int[]     LINEINDENT     = {28, 33, 38};
   static char      END            = '\u0000';
   static Point     DOCTOP         = new Point(0, 0); 
   static Insets    TEXT_INSETS    = new Insets(0,2,1,2);
   static Dimension CHECKBOX_SIZE  = new Dimension(19, 18);
   static Dimension MIN_THUMB_SIZE = new Dimension(13, 13);
   
   static short    PARSE_FAILED              = 1;
   static short    CANCEL                    = 2;
   static short    PARSED                    = 3;
   static short    LINED                     = 4;
   static short    D_HTML                    = 5;
   static short    D_FRAMESET                = 6;
   static short    D_FRAMESET2               = 7;
   static short    D_FRAMESET3               = 8;
   static short    D_FRAME                   = 9;
   static short    PRE_CONNECT               = 10;
   static short    CONNECTED                 = 11;
   static short    PARSED_LINED              = 12;
   static short    PARSE_FAILED_POST_CONNECT = 13;
   static short    DOC_LOADED                = 14;
   static short    DOC_LENGTH                = 15;
   static short    WAITING_FOR_IMAGES        = 16;
   
   static short    NONE           = -1;
   static short    ISNULL         = -2;
   static short    ISBLANK        = 1;
   static short    ISCELL         = 2;
   
   static short    S_SMALL        = 0;
   static short    S_MEDIUM       = 1;
   static short    S_LARGE        = 2;
   static short    S_PREFW        = 3;
   
   static short    NOT_FOUND      = 1;
   static short    SHOWN          = 2;
   static short    ALREADY_SHOWN  = 3;
   
   static short    HISTORY_NULL   = 0;
   static short    HISTORY_NEW    = 1;
   static short    HISTORY_HIST   = 2;
   
   static short    AT_HISTORY_MIDDLE = 0;
   static short    AT_HISTORY_TOP    = 1;
   static short    AT_HISTORY_BOTTOM = 2;
   
   static short    PIXEL          = 0;
   static short    PERCENT        = 1;
   static short    RELATIVE       = 2;

   static short    TIMES          = 0;
   static short    HELV           = 1;
   static short    MONO           = 2;
   static short    DIALOG         = 3;
   
   static short    NORM           = 0;
   static short    BOLD           = 1;
   static short    ITAL           = 2;
   static short    BOIT           = 3;

   static short    IMG_BROKEN     = 1;
   static short    IMG_LOADED     = 2;
   static short    IMG_LOADING    = 3;
   static short    IMG_NEEDWH     = 4;
   static short    IMG_WAITING    = 5;
   
   static short    AV_NONE        = 1;
   static short    AV_LENGTH      = 2;
   static short    AV_LENGTH_NEG  = 3;
   static short    AV_ARG         = 4;
   static short    AV_COLOR       = 5;
   static short    AV_STRING      = 6;
   static short    AV_MULTIPLE    = 7;

   static int      E_ATTR         = 0;
   static int      E_ITEM         = 1;
   static int      E_WIDTH        = 2;
   static int      E_NAMEH        = 3;
   static int      E_LINKH        = 4;
   static int      E_FONT         = 5;
   static int      E_COLOR        = 6;
   static int      E_CHARSTART    = 7;
   static int      E_CHARCOUNT    = 8;
   
   static int      L_X            = 0;
   static int      L_Y            = 1;
   static int      L_H            = 2;
   static int      L_D            = 3;
   static int      L_ES           = 4;
   static int      L_EE           = 5;
   static int      L_JMP          = 6;
   static int      L_A            = 7;
   
   static int      I_TABLE        = 1;
   static int      I_UL           = 2;
   static int      I_OL           = 3;
   static int      I_HR           = 4;
   static int      I_IMG          = 5;
   static int      I_IFRAME       = 6;
   static int      I_FORM         = 7;
   static int      I_NL           = 8;
   
   static int      T_START        = 8;
   static int      T_END          = 9;
   static int      T_LINEDH       = 10;
   
   static short    F_PLUS         = 1;
   static short    F_MINUS        = 2;

   static short    CLEAR_NONE     = 0;
   static short    CLEAR_LEFT     = 1;
   static short    CLEAR_RIGHT    = 2;
   static short    CLEAR_ALL      = 3;

   static short    NUM            = 1;
   static short    ALPHAL         = 2;
   static short    ALPHAU         = 3;
   static short    ROMANL         = 4;
   static short    ROMANU         = 5;
   


   static short    FORM_CONTROL         = 10;
   static short    FORM_HIGHLIGHT       = 11;
   static short    FORM_SHADOW          = 12;
   static short    FORM_TEXT_BACKGROUND = 13;
   static short    FORM_TEXT_COLOR      = 14;
   static short    TAB_FOCUS_BACKGROUND = 15;
   static short    TAB_FOCUS_FOREGROUND = 16;
   
   static int      USE_CALPA_THREEDEE  = 0;
   static int      USE_CALPA_FLUSH     = 1;
   static int      USE_LOOK_AND_FEEL   = 2;
   
   static int      NO_OPTIMIZATION     = 0;
   static int      OPTIMIZE_FONTS      = 1;
   static int      OPTIMIZE_ALL        = 2;
   
   public static short    VIEWER         = 1;

   static short    A              = 1;
   static short    AREA           = 2;
   static short    APPLET         = 3;
   static short    ADDRESS        = 4;
   static short    B              = 5;
   static short    BR             = 6;
   static short    BIG            = 7;
   static short    BASE           = 8;
   static short    BODY           = 9;
   static short    BUTTON         = 10;
   static short    BASEFONT       = 11;
   static short    BLOCKQUOTE     = 12;
   static short    COL            = 13;
   static short    CITE           = 14;
   static short    CODE           = 15;
   static short    CENTER         = 16;
   static short    CAPTION        = 17;
   static short    COLGROUP       = 18;
   static short    DD             = 19;
   static short    DL             = 20;
   static short    DT             = 21;
   static short    DFN            = 22;
   static short    DIR            = 23;
   static short    DIV            = 24;
   static short    EM             = 25;
   static short    FONT           = 26;
   static short    FORM           = 27;
   static short    FRAME          = 28;
   static short    FIELDSET       = 29;
   static short    FRAMESET       = 30;
   static short    H1             = 31;
   static short    H2             = 32;
   static short    H3             = 33;
   static short    H4             = 34;
   static short    H5             = 35;
   static short    H6             = 36;
   static short    HR             = 37;
   static short    HTML           = 38;
   static short    I              = 39;
   static short    IMG            = 40;
   static short    INPUT          = 41;
   static short    IFRAME         = 42;
   static short    ISINDEX        = 43;
   static short    KBD            = 44;
   static short    LI             = 45;
   static short    LABEL          = 46;
   static short    LEGEND         = 47;
   static short    MAP            = 48;
   static short    MENU           = 49;
   static short    NOSCRIPT       = 50;
   static short    OL             = 51;
   static short    OBJECT         = 52;
   static short    OPTION         = 53;
   static short    P              = 54;
   static short    PRE            = 55;
   static short    PARAM          = 79;
   static short    SUB            = 56;
   static short    SUP            = 57;
   static short    SAMP           = 58;
   static short    SPAN           = 59;
   static short    SMALL          = 60;
   static short    STYLE          = 61;
   static short    SCRIPT         = 62;
   static short    SELECT         = 63;
   static short    STRIKE         = 64;
   static short    STRONG         = 65;
   static short    TD             = 66;
   static short    TH             = 67;
   static short    TR             = 68;
   static short    TT             = 69;
   static short    TABLE          = 70;
   static short    TBODY          = 71;
   static short    TFOOT          = 72;
   static short    THEAD          = 73;
   static short    TITLE          = 74;
   static short    TEXTAREA       = 75;
   static short    U              = 76;
   static short    UL             = 77;
   static short    VAR            = 78;
   
      
   static short    A_ALT          = 200;
   static short    A_ALIGN        = 201;
   static short    A_ALINK        = 202;
   static short    A_ACCEPT       = 258;
   static short    A_ACTION       = 267;
   static short    A_ARCHIVE      = 274;
   static short    A_ACCESSKEY    = 261;
   static short    A_ARROWCOLOR   = 260;
   static short    A_BORDER       = 203;
   static short    A_BGCOLOR      = 204;
   static short    A_BACKGROUND   = 205;
   static short    A_BORDCOLOR    = 206;
   static short    A_BORDCOLORDK  = 207;
   static short    A_BORDCOLORLT  = 208;
   static short    A_COLS         = 209;
   static short    A_CODE         = 278;
   static short    A_CLASS        = 264;
   static short    A_CLEAR        = 210;
   static short    A_COLOR        = 211;
   static short    A_COORDS       = 212;
   static short    A_CHECKED      = 249;
   static short    A_CLASSID      = 275;
   static short    A_COLSPAN      = 213;
   static short    A_CODEBASE     = 271;
   static short    A_CODETYPE     = 276;
   static short    A_CELLPADDING  = 214;
   static short    A_CELLSPACING  = 215;
   static short    A_DATA         = 277;
   static short    A_DECLARE      = 272;
   static short    A_DISABLED     = 250;
   static short    A_DROPSHADOW   = 270;
   static short    A_ENCTYPE      = 265;
   static short    A_FOR          = 262;
   static short    A_FACE         = 216;
   static short    A_FRAME        = 217;
   static short    A_FRAMEBORDER  = 218;
   static short    A_FRAMESPACING = 219;
   static short    A_HREF         = 220;
   static short    A_HEIGHT       = 221;
   static short    A_HSPACE       = 222;
   static short    A_ID           = 263;
   static short    A_JNAME        = 268;
   static short    A_JCLASS       = 269;
   static short    A_LINK         = 223;
   static short    A_LABEL        = 255;
   static short    A_METHOD       = 266;
   static short    A_MARGINW      = 224;
   static short    A_MARGINH      = 225;
   static short    A_MULTIPLE     = 253;
   static short    A_MAXLENGTH    = 252;
   static short    A_NAME         = 226;
   static short    A_NOHREF       = 227;
   static short    A_NOWRAP       = 228;
   static short    A_NOSHADE      = 229;
   static short    A_NORESIZE     = 230;
   static short    A_ROWS         = 231;
   static short    A_RULES        = 232;
   static short    A_ROWSPAN      = 233;
   static short    A_READONLY     = 251;
   static short    A_SRC          = 234;
   static short    A_SIZE         = 235;
   static short    A_SPAN         = 236;
   static short    A_SHAPE        = 237;
   static short    A_START        = 238;
   static short    A_STYLE        = 257;
   static short    A_STANDBY      = 273;
   static short    A_SELECTED     = 256;
   static short    A_SCROLLING    = 239;
   static short    A_TEXT         = 240;
   static short    A_TYPE         = 241;
   static short    A_TARGET       = 242;
   static short    A_TABINDEX     = 254;
   static short    A_USEMAP       = 243;
   static short    A_UISCROLL     = 259;
   static short    A_VALUE        = 244;
   static short    A_VLINK        = 245;
   static short    A_VALIGN       = 246;
   static short    A_VSPACE       = 247;
   static short    A_WIDTH        = 248;

   static short    V_ALL          = 400;
   static short    V_AUTO         = 401;
   static short    V_ABOVE        = 402;
   static short    V_ABSTOP       = 431;
   static short    V_ABSBOTTOM    = 403;
   static short    V_ABSMIDDLE    = 404;
   static short    V_BOX          = 405;
   static short    V_BELOW        = 406;
   static short    V_BORDER       = 430;
   static short    V_BOTTOM       = 407;
   static short    V_BUTTON       = 441;
   static short    V_COLS         = 408;
   static short    V_CENTER       = 429;
   static short    V_CHECKBOX     = 434;
   static short    V_CIRCLE       = 409;
   static short    V_DISC         = 410;
   static short    V_DEFAULT      = 411;
   static short    V_FILE         = 438;
   static short    V_FORM         = 445;
   static short    V_FLUSH        = 442;
   static short    V_GET          = 447;
   static short    V_GROUPS       = 412;
   static short    V_HIDDEN       = 439;
   static short    V_HSIDES       = 413;
   static short    V_IMAGE        = 440;
   static short    V_JFORM        = 449;
   static short    V_JCOMPONENT   = 446;
   static short    V_LHS          = 414;
   static short    V_LEFT         = 415;
   static short    V_LOOKANDFEEL  = 444;
   static short    V_MIDDLE       = 416;
   static short    V_NO           = 417;
   static short    V_NONE         = 418;
   static short    V_POLY         = 419;
   static short    V_POST         = 448;
   static short    V_PASSWORD     = 433;
   static short    V_RADIO        = 435;
   static short    V_RHS          = 420;
   static short    V_RECT         = 428;
   static short    V_RESET        = 437;
   static short    V_ROWS         = 421;
   static short    V_RIGHT        = 422;
   static short    V_SQUARE       = 423;
   static short    V_SUBMIT       = 436;
   static short    V_TOP          = 424;
   static short    V_TEXT         = 432;
   static short    V_THREEDEE     = 443;
   static short    V_VOID         = 425;
   static short    V_VSIDES       = 426;
   static short    V_YES          = 427;

}
