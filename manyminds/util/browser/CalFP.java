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

import java.util.*;
import java.io.*;
import java.net.*;

public class CalFP extends Thread implements CalCons {
  
   private char           c;
   private char           c2;
   private char[]         specialArray;
   private char[]         b;
   private char[]         utilArray;
   private Vector         loadingImages;
   private int            currTagType;
   private int            currAttrType;
   private int            attrCount;
   private int            tagStart;
   private int            attrStart;
   private int            titleStart;
   private int            charTitleStart;
   private int            bpos;
   private int            inp;
   private int            m;
   private int            n;
   private int            x;
   private int            y;
   private int            z;
   private int            pos;
   private int            tagPos;
   private int            attrPos;
   private int            charPos;
   private int            objPos;
   private int            lc;
   private int            tableNesting;
   private int            tagOpen;
   private int            cIgnore;
   private int            ignorePos;
   private int            ignoreCharPos;
   private int            currFormNo;
   private int            dataOpenPos;
   private CalFormHandler currFormHandler;
   private CalFormItem    currFormItem;
   private CalFormItem    waitingFormItem;
   private CalImage       waitingTagim;
   private CalFO          currFO;
   private CalTable       currTable;
   private CalImageMap    currMap;
   private CalTagFrameset currFrameset;
   private boolean        bodyFound;
   private boolean        tableOpen;
   private boolean        rowOpen;
   private boolean        dataOpen;
   private boolean        colGroupOpen;
   private boolean        rowGroupOpen;
   private boolean        formOpen;
   private boolean        componentOpen;
   private boolean        imagesOpen;
   private boolean        justSpaced;
   private boolean        addLineable;
   private boolean        permitLineable;
   private boolean        mapOpen;
   private boolean        isDialog;
   private boolean        reload;
   private Stack          tableStack;
   private Stack          rowGroupStack;
   private Stack          framesetStack;
   private CalRowGroup    currRowGroup;
   private boolean        preOn;
   private CalDoc         doc;
   private Reader         in;
   private CalFonts       f;
   private CalHTMLPreferences pref;
   private URL            url;
   private String         parseString;


   //File    testlog;
   //PrintWriter ps;

   public CalFP(CalDoc doc, CalFonts f, CalHTMLPreferences pref, String parseString) {
   
      this(null, doc, f, pref, parseString);   
   }
   
   public CalFP(URL url, CalDoc doc, CalFonts f, CalHTMLPreferences pref, String parseString) {

      this.url  = url;
      this.doc  = doc;
      this.f    = f;
      this.pref = pref;
      this.parseString = parseString;
      try {
         setPriority(4);
      } catch (IllegalArgumentException e) {
      } catch (SecurityException e2) {
      }
   }
   

   public void run() {
   
      String s;
      
      try {
         if ((parseString != null) && (url == null)) {
            in = new StringReader(parseString);
            parseDocument();
         } else {
            try {
               URLConnection connect = url.openConnection();
               if (reload) {
                  connect.setUseCaches(false);
               }
               URL connectURL; 
               if (parseString != null) {    //form POST, so send the form first
                  HttpURLConnection.setFollowRedirects(true);
                  HttpURLConnection htp;
                  if (("http").equals(url.getProtocol())) {
                     //disconnect in case we're already connected
                     if (connect instanceof HttpURLConnection) {
                        htp = (HttpURLConnection)connect;
                        htp.disconnect();
                     }
                  } else {
                     throw new IOException("url protocol not http");
                  }
                  connect = url.openConnection();
                  connectURL = connect.getURL();
                  if ((connectURL != null) && (!connectURL.sameFile(doc.docURL))) {
                     url        = connectURL;
                     doc.docURL = connectURL;
                     doc.url    = connectURL;
                  }
                  if (connect instanceof HttpURLConnection) {
                     htp = (HttpURLConnection)connect;
                     connect.setDoOutput(true);
                     connect.setDoInput(true);
                     connect.setAllowUserInteraction(true);
                     //System.out.println("Posting form");
                     //System.out.println(url.toExternalForm());
                     htp.setRequestMethod("POST");
                     PrintWriter out = new PrintWriter(connect.getOutputStream());
                     out.print(parseString);
                     out.flush();
                     //System.out.println("Response = " + htp.getResponseCode());
                     in = new InputStreamReader(connect.getInputStream());
                     doc.setState(CONNECTED);

                    //testlog = new File("testlog.txt");
                    //ps = new PrintWriter(new BufferedOutputStream(new FileOutputStream(testlog)));



                     parseDocument();
                  }
               } else {
                  doc.fileSize = connect.getContentLength();
      
                    //testlog = new File("testlog.txt");
                    //ps = new PrintWriter(new BufferedOutputStream(new FileOutputStream(testlog)));


                  connectURL = connect.getURL();
                  if ((connectURL != null) && (!connectURL.sameFile(doc.docURL))) {
                     url        = connectURL;
                     doc.docURL = connectURL;
                     doc.url    = connectURL;
                  }
                  s = connect.getContentType();
                  if (("image/jpeg").equals(s) || ("image/gif").equals(s)) {
                     doc.setDocumentAsImageWrapper(url);
                     doc.lineableTokens = doc.tokenCodes.length;
                     doc.setDocType(D_HTML);
                     doc.setState(PARSED);
                  } else {
                     in = new InputStreamReader(connect.getInputStream());
                     doc.setState(CONNECTED);
                     parseDocument();
                  }
               }
            } catch (IOException e3) {
               doc.setErrorMessage(e3.getMessage());
               doc.setState(PARSE_FAILED);
            } catch (NullPointerException e4) {
               doc.setErrorMessage(e4.getMessage());
               doc.setState(PARSE_FAILED);
               e4.printStackTrace();
            }
         }
      } catch (Exception ex) {
         doc.setErrorMessage(ex.getMessage());
         doc.setState(PARSE_FAILED);
         ex.printStackTrace();
      } finally {
         try {
            in.close();
            
            //ps.flush();
            //ps.close();

         } catch (IOException e6) {
         } catch (NullPointerException e7) {
         }
         currRowGroup = null;
         doc          = null;
         in           = null;
         f            = null;
         pref         = null;
         url          = null;
         currTable    = null;
         currMap      = null;
         currFormItem = null;
      }
      this.stop();
   }
   
   
   private void parseDocument() {
   
      tableStack       = new Stack();
      rowGroupStack    = new Stack();
      utilArray        = new char[8192];
      specialArray     = new char[8];
      b                = new char[256];
      doc.charArray    = new char[50000];
      doc.tokenCodes   = new int[5000];
      doc.tags         = new short[3000];
      doc.attrTypes    = new short[1000];
      doc.attrArgs     = new short[1000];
      doc.attrVals     = new int[1000];
      doc.attrStrings  = new String[1000];
      doc.objectVector = new Vector();
   
      pos            = 0;
      tagPos         = 1;  //we can't have 0 'cos tags are noted by a negative value in tokenCodes array
      attrPos        = 0;
      charPos        = 0;
      objPos         = 0;
      bpos           = 1;
      inp            = 0;
      cIgnore        = 0;
      bodyFound      = false;
      permitLineable = true;
      titleStart     = -1;
      currFormNo     = -1;
      dataOpenPos    = -1;
      String s = doc.docURL.toExternalForm();
      try {
         if ((s.length() > 4) && ((".txt").equalsIgnoreCase(s.substring(s.length() - 4)))) {
            parseTextDocument();
         } else {
            readChar();
            while (c != END) {
               if (preOn) {
                  justSpaced = false;
                  switch(c) {
                     case '\n': currTagType = BR;
                                tagOpen = 1;
                                addTag();
                                addToTag(0);   //no attributes
                                readChar();
                                if (c == '\r') {
                                   readChar();
                                }
                                break;
                     case '\r': currTagType = BR;
                                tagOpen = 1;
                                addTag();
                                addToTag(0);
                                readChar();
                                if (c == '\n') {
                                   readChar();
                                }
                                break;
                     case '\t': if ((charPos + 8) >= doc.charArray.length) {
                                   redimCharArray(charPos, charPos + 50000);
                                }
                                for (int i=0; i<8; i++) {
                                   doc.charArray[charPos++] = ' ';
                                }
                                if (pos >= doc.tokenCodes.length) {
                                   redimTokenCodes(pos, pos << 1);
                                }
                                doc.tokenCodes[pos++] = 8;
                                readChar();
                                break;
                     case '<' : parseTag()     ; break;
                      default : parsePreWord() ; break;
                  }
               } else {
                  switch(c) {
                     case '\n':
                     case '\t':
                     case '\r': 
                     case ' ' : if (pos >= doc.tokenCodes.length) {
                                   redimTokenCodes(pos, pos << 1);
                                }
                                doc.tokenCodes[pos++] = 0;
                                justSpaced = true;
                                dumpWS();
                                break;
                     case '<' : parseTag(); break;
                       default: parseWord(); justSpaced = false; break;
                  }
               }
            }

            if (cIgnore > 0) {  //user forgot to close summat like a STYLE tag. Could lose most of doc
               if (cIgnore == OPTION) {
                  currTagType = cIgnore;
                  tagOpen = -1;
                  checkIgnoreTags();
               } else {
                  pos = ignorePos;
                  charPos = ignoreCharPos;
               }
            }

            if (tableOpen) {
               for(;;) {
                  currTagType = TABLE;
                  tagOpen = -1;
                  finishTable();  //finishTable routine will add the </TABLE> tag
                  if (tableNesting == 0) {
                     break;
                  } else {
                     tableNesting--;
                     currTable = (CalTable)tableStack.pop();
                  }
               }
            }
         }
         n = (pos == 0) ? 1 : pos;
         redimTokenCodes(n, n);           //scale arrays down
         n = (charPos == 0) ? 1 : charPos;
         redimCharArray(n, n);
         doc.lastCharPos = doc.charArray.length;
         redimTagArray(tagPos, tagPos);    //tagPos started at 1 so no 0 check required
         n = (attrPos == 0) ? 1 : attrPos;
         redimAttrArrays(n, n);   //does attrTypes *and* attrStrings
         //check all images without specified dimensions have loaded
         if (loadingImages != null) {
            doc.setState(WAITING_FOR_IMAGES);
            try {
               while (true) {
                  //System.out.println("--------------------");
                  for (int i=loadingImages.size()-1; i>=0; i--) {
                     if (((CalImage)loadingImages.elementAt(i)).ready) {
                        //System.out.println("Image " + i + " READY!!!!!!!");
                        loadingImages.removeElementAt(i);
                     }  //else {
                        //System.out.println("Image " + i + " not ready");
                        //}
                  }
                  if (loadingImages.size() == 0) {
                     break;
                  } else {
                     sleep(500);
                  }
               }
            } catch (InterruptedException ie) {
               //System.out.println("Interrupted!!");
               for (int i=loadingImages.size()-1, w1, h1; i>=0; i--) {
                  CalImage calim = (CalImage)(loadingImages.elementAt(i));
                  w1 = calim.im.getWidth(calim);
                  h1 = calim.im.getHeight(calim);
                  if (calim.width == -1) {
                     calim.width = (w1 > 0) ? w1 : 40;
                  }
                  if (calim.height == -1) {
                     calim.height = (h1 > 0) ? h1 : 40;
                  }
                  if ((w1 == -1) || (h1 == -1)) {
                     calim.setStatus(IMG_BROKEN);
                  }
               }
            }
         }              
         doc.lineableTokens = (pos == 0) ? -1 : doc.tokenCodes.length;
         doc.setState(PARSED);
         if (doc.docType == 0) {
            doc.setDocType(D_HTML);
         }
      } catch (IOException e) {
         //System.err.println("IOException : " + e.getMessage());
         doc.setErrorMessage(e.getMessage());
         doc.setState(PARSE_FAILED);
      }
   }

   
   private void readChar() throws IOException {

      if (bpos >= inp) {
         inp = in.read(b);
         if (inp <= 0) {
            c = END;
            return;
         }
         bpos = 0;
      }
      c = b[bpos++];

      //ps.print(c);
      //System.out.println(c);
   }
         

   private void dumpWS() throws IOException {
   
      for(;;) {
         switch (c) {
            case ' ' : case '\t': case '\n': case '\r': readChar(); break;
            default  : return;
         }
      }
   }


   private void parseTag() throws IOException {
  
      readChar();
      if (c == '/') {
         tagOpen = -1;
         readChar();
      } else if (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'))) {
         tagOpen = 1;
      } else if (c == '!') {
         parseComment();
         return;
      } else {
         if (charPos >= doc.charArray.length) {
            redimCharArray(charPos, charPos + 50000);
         }
         doc.charArray[charPos++] = '<';
         if (pos >= doc.tokenCodes.length) {
            redimTokenCodes(pos, pos << 1);
         }
         doc.tokenCodes[pos++] = 1;
         return;
      }
      while ((c != END) && (c != '>')) {
         n = 0;
         out:
         try {
            for(;;) {
               switch (c) {
                  case '>' : case ' ' : case '=' : case '\n': case '\t': case '\r': case END : break out;
                  default  : m = c;
                             if ((m > 96) && (m < 123)) {   //convert to upper case
                                m = ('A' + (m - 'a'));
                             }
                             utilArray[n++] = (char)m;
                             readChar();
               }
            }
         } catch (ArrayIndexOutOfBoundsException e) {
            n = 0;
         }
         if ((n == 0) || ((currTagType = decodeTag()) == 0)) {
            while ((c != END) && (c != '>')) {
               readChar();
            }
            readChar();  //shouldn't matter if c == END, readChar will return END once again
            if (justSpaced) {
               dumpWS();
            }
            return;     //no recognised tag found
         }

         //If we reach here we've found a supported tag. currTagType = the tag code
         tagStart  = tagPos;
         attrStart = attrPos;
         attrCount = 0;
         dumpWS();
         while ((c != END) && (c != '>')) {
            n = 0;
            out2:
            try {
               for(;;) {
                  switch (c) {
                     case '>' : case ' ' : case '=' : case '\n': case '\t': case '\r': case END : break out2;
                     default  : m = c;
                                if ((m > 64) && (m < 91)) {   //convert to lower case
                                   m = ('a' + (m - 'A'));
                                }
                                utilArray[n++] = (char)m;
                                readChar();
                  }
               }
            } catch (ArrayIndexOutOfBoundsException e2) {
              n = 0;        //if its 256 chars long it's almost certainly a user error
              readChar();
            }
            dumpWS();
            currAttrType = decodeAttr();
            if (c != '=') {
               n = 0;
            } else {
               n = 0;
               readChar();
               dumpWS();
               if ((c == '"') || (c =='\'')) {
                  c2 = c;
                  try {
                     readChar();
                     while ((c != END) && (c != c2)) {
                        if (c == '&') {
                           int temp = n;
                           parseSpecial();
                           if (n > 0) {
                              for (int i=0; i<n; i++) {
                                 utilArray[temp++] = specialArray[i];
                              }
                           }
                           n = temp;
                        } else {
                           utilArray[n++] = c;
                           readChar();
                        }
                     }
                  } catch (ArrayIndexOutOfBoundsException e3) {
                     while ((c != END) && (c != c2)) {
                        readChar();
                     }
                     n = 0;
                  }
                  if ((n == 0) && ((c == '"') || (c == '\'')) &&
                     ((currAttrType > 0) && (CalLP.attrCodes[currAttrType] == A_VALUE)) ) {
                     utilArray[n++] = '!';
                  }  
                  readChar();
               } else {
                  out3:
                  for(;;) {
                     try {
                        switch (c) {
                           case '>' : case ' ' : case '\n': case '\t': case '\r': case END : break out3;
                           case '&' : int temp2 = n;
                                      parseSpecial();
                                      if (n > 0) {
                                         for (int i=0; i<n; i++) {
                                            utilArray[temp2++] = specialArray[i];
                                         }
                                      }
                                      n = temp2;
                                      break;
                           default  : utilArray[n++] = c;
                                      readChar();
                                      break;
                        }
                     } catch (ArrayIndexOutOfBoundsException e4) {
                        n = 0;
                     }
                  }
               }
            }
            if (currAttrType != -1) {
               if (attrPos >= doc.attrTypes.length) {
                  redimAttrArrays(attrPos, attrPos << 1);
               }
               if (n > 0) {
                  switch (CalLP.attrType[currAttrType]) {
                     case AV_NONE       : n = 0; break;   //dumps any arg 'cos this tag doesn't have one
                     case AV_LENGTH     :
                     case AV_LENGTH_NEG : decodeLength(); n = 0; break;
                     case AV_ARG        : decodeArg()   ; n = 0; break;
                     case AV_COLOR      : decodeColor() ; n = 0; break;
                     case AV_STRING     : doc.attrStrings[attrPos] = new String(utilArray, 0, n); break;
                     case AV_MULTIPLE   : switch (CalLP.attrCodes[currAttrType]) {
                                             case A_TYPE:  switch(currTagType) {
                                                              case OL: case UL: case LI:
                                                                       doc.attrStrings[attrPos] = new String(
                                                                         utilArray, 0, n); break;
                                                              default: decodeArg(); n = 0; break;
                                                           }
                                                           break;
                                             case A_VALUE: switch(currTagType) {
                                                              case BUTTON:
                                                              case LABEL:
                                                              case OBJECT:
                                                              case OPTION:
                                                              case PARAM:
                                                              case INPUT:
                                                                       doc.attrStrings[attrPos] = new String(
                                                                         utilArray, 0, n); break;
                                                              default: decodeLength(); n = 0; break;
                                                           }
                                                           break;
                                             case A_ROWS :
                                             case A_COLS : if (currTagType == TEXTAREA) {
                                                              decodeLength();
                                                              n = 0;
                                                           } else {
                                                              doc.attrStrings[attrPos] = new String(
                                                                              utilArray, 0, n);
                                                           }
                                                           break;
                                          }
                  }
               } else if (CalLP.attrType[currAttrType] != AV_NONE) {
                  if (CalLP.attrCodes[currAttrType] == A_BORDER) {
                     doc.attrVals[attrPos] = 1;   //default border arg for tables to comply with old HTML
                  } else {
                     currAttrType = -1;
                  }
               }
            }
            if (currAttrType != -1) {               //above methods may have dumped this tag attr
               doc.attrTypes[attrPos] = (short)CalLP.attrCodes[currAttrType];
               attrPos++;               
               attrCount++;
            }
         }
         readChar();
         checkTags();
         if (currTagType != 0) {     //legal tags may be dumped within the checkTags() routines
            addToTag(attrCount);
            if (attrCount > 0) {     //attrCount can be set to 0 by checkTags() if tag can't have attrs
               addToTag(attrStart);
            } else {
               attrPos = attrStart;  //removes any attributes for this tag by resetting attr start
            }
         } else {
            tagPos  = tagStart;   //effectively removes the tag by resetting the current tagPos
            attrPos = attrStart;  //...and same for attributes
         }
         if (justSpaced) {
            dumpWS();
         }
         if (addLineable) {
            doc.lineableTokens = pos;
            addLineable = false;
         }
         return;
      }
      readChar();
      if (justSpaced) {
         dumpWS();
      }
   }


   private int decodeTag() {
   
      z = utilArray[0] - 65;
      if ((z < 0) || (z > 25)) {
         return 0;    //only alpha chars allowed
      }
      x = CalLP.tagStart[z];
      m = Math.abs(CalLP.tagStart[z + 1]);
      if (x < 0) {
         return 0;   //we don't support a tag that starts with this letter
      } else if (n == 1) {
         if (CalLP.tagLength[x] == 1) {
            return CalLP.tagCodes[x];       //a single character match e.g. <P> or <B>
         } else {
            return 0;  //can't match to any other
         }
      }
      int type = 0;
      for (int i=x; i<m; i++) {
         if (CalLP.tagLength[i] == n) {        //strings are same length
            type = CalLP.tagCodes[i];
            for (int j=1; j<n; j++) {
               if (utilArray[j] != CalLP.tags[i][j]) {
                  type = 0;
                  break;
               }
            }
            if (type != 0) {
               break;
            }
         }
      }
      return type;
   }
   
   
   private int decodeAttr() {
   
      z = utilArray[0] - 97;
      if ((z < 0) || (z > 25)) {
         return -1;    //only alpha chars allowed
      }
      x = CalLP.attrStart[z];
      m = Math.abs(CalLP.attrStart[z + 1]);
      if (x < 0) {
         return -1;   //we don't support an attr that starts with this letter
      } else if (n == 1) {
         if (CalLP.attrLength[x] == 1) {
            return x;
         } else {
            return -1;  //can't match to any other
         }
      }
      int type = -1;
      for (int i=x; i<m; i++) {
         if (CalLP.attrLength[i] == n) {        //strings are same length
            type = i;
            for (int j=1; j<n; j++) {
               if (utilArray[j] != CalLP.attrs[i][j]) {
                  type = -1;
                  break;
               }
            }
            if (type > -1) {
               break;
            }
         }
      }
      return type;
   }


   private void decodeLength() {
   
      n = Math.min(n, 6);     //not going to risk user putting some massive number in
      x = -1;                 //x is the number
      y = 0;
      doc.attrVals[attrPos] = -1;   //default value
      if ((utilArray[0] == '-') || (utilArray[0] == '+')) {
         if (CalLP.attrType[currAttrType] == AV_LENGTH_NEG) {
            doc.attrArgs[attrPos] = (utilArray[0] == '-') ? F_MINUS : F_PLUS;
            y = 1;
         }
      }
      for (int i=y, j; i<n; i++) {
         switch (utilArray[i]) {
            case '%' : if ((x > 0) && (x <= 100)) {
                          doc.attrVals[attrPos] = x;
                          if (y == 0) {
                             doc.attrArgs[attrPos] = PERCENT;
                          }
                       } else {
                          currAttrType = -1;     //illegal construct
                       }
                       return;
            case '*' : if (x > -1) {
                          doc.attrVals[attrPos] = x;
                       }
                       if (y == 0) {
                          doc.attrArgs[attrPos] = RELATIVE;   // "*" will be marked by val=-1, arg=RELATIVE
                       }
                       return;
            default  : j = utilArray[i];
                       if ((j > 47) && (j < 58)) {
                          x = (x == -1) ? j - 48 : (x*10) + (j - 48);
                       } else {
                          if (x == -1) {
                             currAttrType = -1;   //illegal construct
                          } else {
                             doc.attrVals[attrPos] = x;
                             if (y == 0) {
                                doc.attrArgs[attrPos] = PIXEL;
                             }
                          }
                          return;
                       }
         }
      }
      if (x > -1) {
         doc.attrVals[attrPos] = x;
         if (y == 0) {
            doc.attrArgs[attrPos] = PIXEL;
         }
      } else {
         currAttrType = -1;
      }
   }
   
   
   private void decodeArg() {

      for (int i=0; i<n; i++) {
         m = utilArray[i];
         if ((m > 64) && (m < 91)) {   //convert to lower case
            m = ('a' + (m - 'A'));
            utilArray[i] = (char)m;
         }
      }
      z = utilArray[0] - 97;
      if ((z < 0) || (z > 25)) {
         currAttrType = -1;    //only alpha chars allowed
         return;
      }
      x = CalLP.argStart[z];
      m = Math.abs(CalLP.argStart[z + 1]);
      if (x < 0) {
         currAttrType = -1;   //dump tag attr - no attr arg starts with this letter
         return;   //we don't support an attr that starts with this letter
      } else if (n == 1) {
         if (CalLP.argLength[x] == 1) {
            doc.attrArgs[attrPos] = CalLP.argCodes[x];
         } else {
            currAttrType = -1;
         }
         return;
      }
      int type = -1;
      for (int i=x; i<m; i++) {
         if (CalLP.argLength[i] == n) {        //strings are same length
            type = CalLP.argCodes[i];
            for (int j=1; j<n; j++) {
               if (utilArray[j] != CalLP.args[i][j]) {
                  type = -1;
                  break;
               }
            }
            if (type > -1) {
               doc.attrArgs[attrPos] = (short)type;
               return;
            }
         }
      }
      //if we reach here, no match was found, so dump the attr
      currAttrType = -1;
   }

   
   
   private void decodeColor() {
   
      y = 0;
      x = -1;    //x will be any number arg
      if (utilArray[0] == '#') {
         if (n == 1) {
            currAttrType = -1;
            return;             // '#' on its own is meaningless
         } else {
            y = 1;
         }
      }
      z = Math.min(n, 6 + y);    //only 6 fields allowed in expressing the number
      out:
      for (int i=y, j; i<z; i++) {
         switch (utilArray[i]) {
            case '0': j=0;  break; case '1': j=1;  break; case '2': j=2;  break; case '3': j=3;  break;
            case '4': j=4;  break; case '5': j=5;  break; case '6': j=6;  break; case '7': j=7;  break;
            case '8': j=8;  break; case '9': j=9;  break; case 'A': j=10; break; case 'a': j=10; break;
            case 'B': j=11; break; case 'b': j=11; break; case 'C': j=12; break; case 'c': j=12; break;
            case 'D': j=13; break; case 'd': j=13; break; case 'E': j=14; break; case 'e': j=14; break;
            case 'F': j=15; break; case 'f': j=15; break;
            default : x = -1; break out;
         }
         x = (x == -1) ? j : (x << 4) + j;
      }
      if (x > -1) {
         doc.attrVals[attrPos] = CalColor.getColor(x);
         return;
      } else {      //we didn't find a valid number argument, so look for preset color name
         for (int i=y; i<n; i++) {
            m = utilArray[i];
            if ((m > 64) && (m < 91)) {   //convert to lower case
               m = ('a' + (m - 'A'));
               utilArray[i] = (char)m;
            }
         }
         z = utilArray[y] - 97;
         if ((z < 0) || (z > 25)) {
            currAttrType = -1;    //only alpha chars allowed
            return;
         }
         x = CalColor.presetStart[z];
         m = Math.abs(CalColor.presetStart[z + 1]);
         if (x < 0) {
            currAttrType = -1;   //dump tag attr - no preset color starts with this letter
            return;
         } else if (n - y == 1) {
            if (CalColor.presetLength[x] == 1) {
               doc.attrVals[attrPos] = CalColor.presetCodes[x];
            } else {
               currAttrType = -1;
            }
            return;
         }
         int type = -1;
         for (int i=x; i<m; i++) {
            if (CalColor.presetLength[i] == (n - y)) {        //strings are same length
               type = CalColor.presetCodes[i];
               for (int j=y+1; j<n; j++) {
                  if (utilArray[j] != CalColor.presets[i][j-y]) {
                     type = -1;
                     break;
                  }
               }
               if (type > -1) {
                  doc.attrVals[attrPos] = type;
                  return;
               }
            }
         }
         //if we reach here, no match was found, so dump the attr
         currAttrType = -1;
      }
   }


   private void parseSpecial() throws IOException {

      int i, j;
      n = 1;
      specialArray[0] = c;     //i.e specialArray[0] = '&'

      readChar();
      try {
         for(;;) {
            switch(c) {
               case ';': case ' ': case '&': case '<': case '"': case END: case '\n':
               case '\r': return;
            }
            if (c == '#') {
               specialArray[n++] = c;
               i = j = n;
               for(;;) {
                  readChar();
                  if (!Character.isDigit(c)) {
                     if (j - i == 0) {
                        break;
                     } else {
                        try {
                           int k = Integer.parseInt(new String(specialArray, i, j - i));
                           switch (k) {
                              case 146: k = 39; break;
                              case 147: k = 34; break;
                              case 149: k = 42; break;
                              case 151: k = 45; break;
                           }
                           specialArray[0] = (char)k;
                           n = 1;
                        } catch (NumberFormatException e) {
                           specialArray[0] = '?';
                           n = 1;
                        }
                        if (c == ';') {
                           readChar();
                        }
                        return;
                     }
                  } else {
                     specialArray[j++] = c;
                  }
               }
            } else {
               specialArray[n++] = c;
               x = CalSpecial.getSpecialChar(new String(specialArray, 1, n - 1));
               if (x > 0) {
                  specialArray[0] = (char)x;
                  n = 1;
                  readChar();
                  if (c == ';') {
                     readChar();
                  }
                  return;
               }
               readChar();
            }   
         }
      } catch (ArrayIndexOutOfBoundsException e) {
         if (n >= specialArray.length) {
            n = specialArray.length;
         }
         return;
      }
   }


   private void parseWord() throws IOException {
  
      //System.out.println("Entering parseWord routine");
      lc = 0;
      for(;;) {
         switch(c) {
            case ' ': case '<': case '\n': case '\r': case END: case'\t':
                      if (pos >= doc.tokenCodes.length) {
                         redimTokenCodes(pos, pos << 1);
                      }
                      doc.tokenCodes[pos++] = lc;
                      return;
            case '&': parseSpecial();
                      if (n > 0) {
                         if ((charPos + n) >= doc.charArray.length) {
                            redimCharArray(charPos, charPos + 50000);
                         }
                         for (int i=0; i<n; i++) {
                            doc.charArray[charPos++] = specialArray[i];
                         }
                         lc += n;
                      }
                      break;
             default: if ((charPos) >= doc.charArray.length) {
                         redimCharArray(charPos, charPos + 50000);
                      }
                      doc.charArray[charPos++] = c;
                      lc++;
                      readChar();
         }
      }
   }
   

   private void parsePreWord() throws IOException {
  
      lc = 0;
      for(;;) {
         switch(c) {
            case '<': case '\n': case '\r': case END: case'\t':
                      if (pos >= doc.tokenCodes.length) {
                         redimTokenCodes(pos, pos << 1);
                      }
                      doc.tokenCodes[pos++] = lc;
                      return;
            case '&': parseSpecial();
                      if (n > 0) {
                         if ((charPos + n) >= doc.charArray.length) {
                            redimCharArray(charPos, charPos + 50000);
                         }
                         for (int i=0; i<n; i++) {
                            doc.charArray[charPos++] = specialArray[i];
                         }
                         lc += n;
                      }
                      break;
             default: if ((charPos) >= doc.charArray.length) {
                         redimCharArray(charPos, charPos + 50000);
                      }
                      doc.charArray[charPos++] = c;
                      lc++;
                      readChar();
         }
      }
   }


   //Re-enabling the commented-out lines in this method will force the parser
   //to parse comments exactly as SGML dictates. Unfortunately most HTML authors
   //don't obey these rules.
   private void parseComment() throws IOException {

      boolean open   = false;
      boolean legal  = false;
      boolean loaded = false;
      //boolean setup  = false;
      
      readChar();
      if (c == '>') {              //empty comment
         readChar();
         if (justSpaced) {
            dumpWS();
         }
         return;
      } else if (c == '-') {
         readChar();
         if (c == '-') {
            legal = true;
            open  = true;
         } else {
            legal = false;
         }
      } else {
         legal = false;
      }
      if (!legal) {       //just treat it as an unrecognised tag
         while ((c != END) && (c != '>')) {
            readChar();
         }
         readChar();
         if (justSpaced) {
            dumpWS();
         }
         return;
      }
      //if we reach here we've got <-- so far, a correctly opened comment
      while (c != END) {
         readChar();
         //if (setup) {
         //   dumpWS();       //WS is permitted between the closing -- and > comment characters
         //}
         if (c == '-') {
            if (!open) {
               if (loaded) {
                  //if (open) {
                  //   open  = false;
                  //   setup = true;
                  //} else {
                     open = true;
                  //}
                  loaded = false;
               } else {
                  loaded = true;
                  //setup = false;
               }
            }
         } else if (c == '>') {
            //if (setup) {             //then we've got a closing --> and can exit
            if (open) {        //then we've got -->
               readChar();
               //if (justSpaced) {
               //   dumpWS();
               //}
               return;
            } else {
               loaded = false;
            }
         } else {
            open = false;
            loaded = false;
            //setup  = false;
         }
      }
   }


   private void checkTags() throws IOException {

      //System.out.println("Tag = " + currTagType);
      if (cIgnore > 0) {
         if (!checkIgnoreTags()) {
            return;
         }
      }
      
      switch (currTagType) {
         case TITLE   : if (tagOpen == 1) {              //i.e if the tag is open
                           if (titleStart < 0) {
                              titleStart = pos;
                              charTitleStart = charPos;
                              cIgnore = TITLE;
                              ignorePos = pos;
                           }
                        }
                        currTagType = 0;
                        break;
         case STYLE   : if (tagOpen == 1) {
                           cIgnore = STYLE;
                           ignorePos = pos;
                           ignoreCharPos = charPos;
                        }
                        currTagType = 0;
                        break;
         case SCRIPT  : if (tagOpen == 1) {
                           cIgnore = SCRIPT;
                           ignorePos = pos;
                           ignoreCharPos = charPos;
                        }
                        currTagType = 0;
                        break;
         case FRAMESET: if (isDialog) {
                           currTagType = 0;  //no framesets in the dialog
                           return;
                        }
                        if (tagOpen == 1) {
                           if (doc.docType == 0) {
                              doc.setDocType(D_FRAMESET);
                           }
                           addTag();
                           CalTagFrameset fset = new CalTagFrameset(doc, attrStart, attrStart + attrCount);
                           if (currFrameset != null) {
                              if (framesetStack == null) {
                                 framesetStack = new Stack();
                              }
                              framesetStack.push(currFrameset);
                           }
                           currFrameset = fset;
                           addToTag(objPos);
                           addObject(fset);
                           attrCount = 0;
                        } else {
                           addTag();
                           if ((framesetStack != null) && (!framesetStack.empty())) {
                              currFrameset = (CalTagFrameset)framesetStack.pop();
                           } else {
                              currFrameset = null;
                           }
                        }
                        break;
         case A       : parseA();
                        break;
         case BR      : if ((pos > 0) && (doc.tokenCodes[pos-1] == 0) && (pos != dataOpenPos)) {
                           pos--;    //get rid of the space before the line break
                        }
                        if (!preOn) {
                           justSpaced = true;   //gets rid of leading spaces after line break
                        }
                        addTag();
                        break;
         case PRE     : if (tagOpen == 1) {
                           preOn = true;
                           justSpaced = false;
                           if (c == '\n') {
                              readChar();
                              if (c == '\r') {
                                  readChar();
                              }
                           } else if (c == '\r') {
                              readChar();
                              if (c == '\n') {
                                 readChar();
                              }
                           }
                        } else {
                           preOn = false;
                           if (permitLineable) {
                              addLineable = true;
                           }
                        }
                        addTag();
                        attrCount = 0;
                        break;
         case TABLE   : parseTABLE()   ; break;
         case CAPTION : parseCAPTION() ; break;
         case COLGROUP: parseCOLGROUP(); break;
         case COL     : parseCOL()     ; break;
         case THEAD   :
         case TBODY   :
         case TFOOT   : parseROWGROUP(); break;
         case TR      : parseTR()      ; break;
         case TH      :
         case TD      : parseTHTD()    ; break;
         case BODY    : if (tagOpen == 1) {
                           if (doc.docType == 0) {
                              if (!bodyFound) {
                                 doc.setAttributes(attrStart, attrStart + attrCount);
                                 bodyFound = true;
                              }
                           }
                        } else {
                           preOn = false;
                        }
                        currTagType = 0;
                        break;
         case IMG     : parseIMG(-1, false);
                        break;
         case FORM    : if (tagOpen == 1) {
                           if (formOpen || componentOpen) {
                              currTagType = 0; //no nested forms, nor forms in labels etc.
                           } else {
                              formOpen = true;
                              currFormNo = tagPos;
                              addTag();
                              addToTag(objPos);
                              currFormHandler = new CalFormHandler(doc, attrStart, attrStart+attrCount, currFormNo);
                              addObject(currFormHandler);
                              checkPermitLineable();
                           }
                        } else {
                           if (formOpen) {
                              addTag();
                              currFormNo = -1;
                              formOpen = false;
                              checkPermitLineable();
                           }
                        }
                        break;
         case BUTTON  :
         case LABEL   : if (tagOpen == 1) {
                           if (currFormItem != null) {
                              currTagType = 0;
                           } else {
                              currFormItem = addFormItem();
                              currFormItem.charStart = charPos;
                              componentOpen = true;
                              currFormItem.startPos = pos;
                              checkPermitLineable();
                           }
                        } else {
                           if (componentOpen) {
                              currFormItem.endPos = pos - 1;
                              currFormItem.charEnd = charPos;
                              if ((currFormItem.align != V_LEFT) && (currFormItem.align != V_RIGHT)) {
                                 justSpaced = false;
                              }
                              currFormItem = null;
                              componentOpen = false;
                              if (waitingFormItem != null) {
                                 if (pos >= doc.tokenCodes.length) {
                                    redimTokenCodes(pos, pos << 1);
                                 }
                                 doc.tokenCodes[pos++] = 0;  //insert a space
                                 currTagType = waitingFormItem.tagType;
                                 waitingFormItem.tagPos = tagPos;
                                 tagOpen = 1;
                                 addTag();
                                 addToTag(objPos);
                                 addObject(waitingFormItem);
                                 attrCount = 0;
                                 if ((waitingFormItem.align != V_LEFT) && 
                                                       (waitingFormItem.align != V_RIGHT)) {
                                    justSpaced = false;
                                 }
                                 waitingFormItem = null;
                              } else if (waitingTagim != null) {
                                 if (pos >= doc.tokenCodes.length) {
                                    redimTokenCodes(pos, pos << 1);
                                 }
                                 doc.tokenCodes[pos++] = 0;  //insert a space
                                 currTagType = IMG;
                                 tagOpen = 1;
                                 addTag();
                                 addToTag(objPos);
                                 addObject(waitingTagim);
                                 attrCount = 0;
                                 if ((waitingTagim.align != V_LEFT) &&
                                                   (waitingTagim.align != V_RIGHT)) {
                                    justSpaced = false;
                                 }
                                 waitingTagim = null;
                              } else {
                                 currTagType = 0;
                              }
                              checkPermitLineable();
                           } else {
                              currTagType = 0;
                           }
                        }
                        break;
         case OBJECT  :
         case SELECT  : if (tagOpen == 1) {
                           if (componentOpen) {
                              if (waitingFormItem != null) {
                                 currTagType = 0;
                              } else {
                                 waitingFormItem = addFormItem();
                                 justSpaced = true;
                              }
                           } else {
                              if (currFormItem != null) {
                                 currTagType = 0; //no nested selects nor in buttons/labels etc
                              } else {
                                 currFormItem = addFormItem();
                                 justSpaced = true;
                              }
                           }
                        } else {
                           if (!componentOpen) {
                              currFormItem = null;
                              justSpaced = false;
                           }
                           currTagType = 0;   //can ditch the closed tag
                        }
                        break;
         case PARAM   :
         case OPTION  : if (tagOpen == 1) {
                           boolean isOption = (currTagType == OPTION);
                           CalFO opt = null;
                           if (componentOpen) {
                              if ((waitingFormItem != null) && ((waitingFormItem.tagType == SELECT) ||
                                    (waitingFormItem.tagType == OBJECT))) {
                                 opt = new CalFO(doc, attrStart, attrStart + attrCount);
                                 waitingFormItem.addOption(opt);
                              } else {
                                currTagType = 0;
                              }
                           } else {
                              if ((currFormItem != null) && ((currFormItem.tagType == SELECT) ||
                                        (currFormItem.tagType == OBJECT))) {
                                 opt = new CalFO(doc, attrStart, attrStart + attrCount);
                                 currFormItem.addOption(opt);
                              } else {
                                 currTagType = 0;
                              }
                           }
                           if (isOption && (opt != null)) {
                              opt.verbosePos = pos;
                              opt.charStart  = charPos;
                              currFO  = opt;
                              cIgnore = OPTION;
                              ignorePos = pos;
                           }
                        }
                        currTagType = 0;
                        break;
         case INPUT   : int i = getAttr(A_TYPE);
                        if (tagOpen == 1) {
                           if (componentOpen) {
                              if (waitingFormItem != null) {
                                 currTagType = 0;
                                 break;
                              } else {
                                 if ((i != -1) && (doc.attrArgs[i] == V_IMAGE)) {
                                    if (waitingTagim == null) {
                                       CalFormItem fItem = new CalFormItem(doc, attrStart,
                                         attrStart + attrCount, f, currTagType, currFormNo, tagPos);
                                       i = tagPos;
                                       addTag();
                                       addToTag(objPos);
                                       addObject(fItem);
                                       if (currFormHandler != null) {
                                          currFormHandler.addFormItem(fItem);
                                       }
                                       currTagType = IMG;    //we change the tag
                                       parseIMG(i, true);
                                    } else {
                                       currTagType = 0;
                                    }
                                 } else {
                                    waitingFormItem = addFormItem();
                                 }
                              }
                           } else {
                              if ((i != -1) && (doc.attrArgs[i] == V_IMAGE)) {
                                 CalFormItem fItem = new CalFormItem(doc, attrStart,
                                    attrStart + attrCount, f, currTagType, currFormNo, tagPos);
                                 i = tagPos;
                                 addTag();
                                 addToTag(objPos);
                                 addObject(fItem);
                                 if (currFormHandler != null) {
                                    currFormHandler.addFormItem(fItem);
                                 }
                                 currTagType = IMG;    //we change the tag
                                 parseIMG(i, false);
                              } else {
                                 addFormItem();
                              }
                           }
                        } else {
                           currTagType = 0;
                        }
                        break;
         case TEXTAREA: if (tagOpen == 1) {
                           CalFormItem fItem = null;
                           if (componentOpen) {
                              if (waitingFormItem != null) {
                                 currTagType = 0;
                              } else {
                                 fItem = addFormItem();
                                 waitingFormItem = fItem;
                              }
                           } else {
                              fItem = addFormItem();
                           }
                           if (fItem != null) {
                              fItem.setInitialValue(getTEXTAREAtext());
                           }
                        } else {
                           currTagType = 0;   //</TEXTAREA> handled by addFormItem routine
                        }
                        break;
         case IFRAME  : if (isDialog) {
                           currTagType = 0;  //no iframes in the dialog
                           return;
                        }
                        if ((tagOpen == 1) && (!componentOpen)) {
                           cIgnore = IFRAME;
                           addTag();
                           addToTag(objPos);
                           CalTagFrame tFrame = new CalTagFrame(doc, attrStart, attrStart + attrCount,
                                                                                                doc.url);
                           addObject(tFrame);
                           if ((tFrame.align != V_LEFT) && (tFrame.align != V_RIGHT)) {
                              justSpaced = false;
                           }
                           ignorePos = pos;
                           ignoreCharPos = charPos;
                        } else {
                           currTagType = 0;  //if iframe is already open then cIgnore will be set and
                                             //we won't reach this section with a closed tag
                        }
                        attrCount = 0;
                        break;
         case FRAME   : if (tagOpen == 1) {
                           addTag();
                           addToTag(objPos);
                           CalTagFrame fr = new CalTagFrame(doc, attrStart, attrStart + attrCount, doc.url); 
                           addObject(fr);
                           if ((fr.frameborder == 0) && (currFrameset != null)) {
                              currFrameset.frameBorders = 2;
                           }
                        } else {
                           currTagType = 0;
                        }
                        attrCount = 0;
                        break;
      case BLOCKQUOTE :
         case CENTER  :
         case ADDRESS :
         case DL      :
         case DT      :
         case DD      : addTag();
                        attrCount = 0;   //none of these tags have attributes
                        if (permitLineable && (tagOpen == 1)) {
                           addLineable = true;
                           if (doc.docType == 0) {
                              doc.setDocType(D_HTML);
                           }
                        }
                        break;
         case LI      : justSpaced = true;    //...and drop through
         case P       :
         case H1      :
         case H2      :
         case H3      :
         case H4      :
         case H5      :
         case H6      :
         case DIV     :
         case UL      :
         case OL      :
         case DIR     :
         case MENU    : addTag();
                        if (permitLineable && (tagOpen == -1)) {
                           addLineable = true;
                           if (doc.docType == 0) {
                              doc.setDocType(D_HTML);
                           }
                        }
                        break;
         case HR      :
         case FONT    : 
         case BASEFONT: addTag();
                        break;
         case B       :
         case BIG     :
         case CODE    :
         case DFN     :
         case EM      :
         case I       :
         case KBD     :
         case SUB     :
         case SUP     :
         case SAMP    :
         case SMALL   :
         case STRIKE  :
         case STRONG  :
         case TT      :
         case U       :
         case VAR     : addTag();
                        attrCount = 0;     //these tags don't have attributes
                        break;
         case HTML    : currTagType = 0;    //dump it for the moment - might need it in the future
                        break;
         case MAP     : parseMAP()     ; break;
         case AREA    : parseAREA()    ; break;
         case BASE    : parseBASE()    ; break;

      }
   }


   private void checkPermitLineable() {
   
      if (tableOpen || formOpen || componentOpen || imagesOpen) {
         permitLineable = false;
      } else {
         permitLineable = true;
      }
   }


   private CalFormItem addFormItem() {   

      CalFormItem fItem = new CalFormItem(doc, attrStart, attrStart + attrCount, f,
                                                            currTagType, currFormNo, tagPos);
      if (!componentOpen) {
         addTag();
         addToTag(objPos);
         addObject(fItem);
         if (currFormHandler != null) {
            currFormHandler.addFormItem(fItem);
         }
         attrCount = 0;
         if ((fItem.align != V_LEFT) && (fItem.align != V_RIGHT)) {
            justSpaced = false;
         }
      } else {
         currTagType = 0;
      }
      return fItem;
   }
   

   private boolean checkIgnoreTags() {
   
      boolean proceed = false;
      
      switch(currTagType) {
         case TITLE : if (cIgnore == TITLE) {
                         if (tagOpen == -1) {     //i.e if the tag is closed
                            doc.setTitle(titleStart, pos - 1, charTitleStart);
                            pos = titleStart;           //reset tokenCodes array
                            charPos = charTitleStart;   //...and reset charArray
                            titleStart = -1;
                            cIgnore = 0;
                         }
                      }
                      break;
         case STYLE : if ((tagOpen == -1) && (cIgnore == STYLE)) {
                         cIgnore = 0;
                         pos = ignorePos;
                         charPos = ignoreCharPos;
                      }
                      break;
         case SCRIPT: if ((tagOpen == -1) && (cIgnore == SCRIPT)) {
                         cIgnore = 0;
                         pos = ignorePos;
                         charPos = ignoreCharPos;
                      }
                      break;
         case IFRAME: if ((tagOpen == -1) && (cIgnore == IFRAME)) {
                         cIgnore = 0;
                         pos = ignorePos;
                         charPos = ignoreCharPos;
                      }
                      break;
         case FORM  :
         case SELECT:
         case OPTION: if (cIgnore == OPTION) {
                         if ((currTagType == OPTION) || (currTagType == SELECT) || (tagOpen == -1)) {
                            if (currFO != null) {
                               currFO.setVerboseValue(doc, currFO.verbosePos, pos - 1, currFO.charStart);
                               pos = currFO.verbosePos;
                               charPos = currFO.charStart;
                               cIgnore = 0;
                            }
                            if (!((currTagType == OPTION) && (tagOpen == -1))) {
                               proceed = true;
                               justSpaced = false;
                            } else {
                               justSpaced = true;
                            }
                         }
                      }
                      break;
      }
      if (!proceed) {
         currTagType = 0;       //dump the tag, no matter what type it is
      }
      
      return proceed;
   }


   private void parseA() {

      if (componentOpen) {
         currTagType = 0;
         return;
      }
      addTag();
      if (tagOpen == 1) {
         int i, ref, j=0;
         String s;
         if ((i = getAttr(A_NAME)) != -1) {
            s = doc.attrStrings[i];
            if ((s.length() > 1) && s.startsWith("#")) {
               s = s.substring(1);
            }
            if (attrPos >= doc.attrTypes.length) {
               redimAttrArrays(attrPos, attrPos << 1);
            }
            doc.attrVals[attrPos] = s.hashCode();
            addToTag(attrPos);
            attrPos++;
            attrCount++;   //(maybe we don't need this)
         } else {
            addToTag(0);  //must reserve space in tag array irrespective of whether name found
         }
         if ((i = getAttr(A_HREF)) != -1) {
            s = doc.attrStrings[i];
            try {
               URL url2 = new URL(doc.url, s);
               char[] a = url2.toExternalForm().toCharArray();
               j = 0;
               ref = a.length;
               for (int k=a.length-1; k>4; k--) {
                  if (a[k] == '#') {
                     ref = k;
                     break;
                  }
               }               
               for (int k=ref, p=0; k>0; k--) {
                  j = (j * 37) + a[p++];
               }
               if ((s = url2.getRef()) != null) {
                  j += s.hashCode();
               }
            } catch (MalformedURLException e) {
               //System.out.println("Malformed URL");
               j = 0;
            }
            if (pref.visitedHash.get(new Integer(j)) != null) {
               j = -1;
            }
            if (j != 0) {
               if (attrPos >= doc.attrTypes.length) {
                  redimAttrArrays(attrPos, attrPos << 1);
               }
               doc.attrVals[attrPos] = j;
               addToTag(attrPos);
               attrPos++;
               attrCount++;   //(don't think we need this)
            } else {
               addToTag(0); //must reserve space in tag array irrespective of whether link found
            }
         } else {
            addToTag(0);
         }
         if (mapOpen) {        //then this anchor may contain imagemap area info
            currMap.addArea(new CalImageMapArea(doc, getAttr(A_SHAPE), getAttr(A_COORDS), getAttr(A_NOHREF),
                                                                      getAttr(A_HREF), getAttr(A_TARGET)));
         }
      }
   }


   private CalImage parseIMG(int formTagPos, boolean isWaiting) {
   
      CalImage tagim = null;
      if (tagOpen == 1) {
         tagim = new CalImage(doc, attrStart, attrStart + attrCount, f);
         if (formTagPos >= 0) {
            tagim.formTagPos = formTagPos;
         }
         if (componentOpen && (currFormItem != null)) {
            currFormItem.hasImages = true;
         }
         if ((pref.loadImages) || (tagim.jname != null)) {
            if (!tagim.forceImageLoad()) {
               addLoadingImage(tagim);      //don't know image dimensions, so must wait for load
               permitLineable = false;
               imagesOpen = true;
            }
            doc.hasImages = true;
         } else {
            tagim.setBrokenWidthAndHeight();
         }
         if (isWaiting) {
            waitingTagim = tagim;
            currTagType = 0;
         } else {
            addTag();
            addToTag(objPos);
            addObject(tagim);
            attrCount = 0;
            if ((tagim.align != V_LEFT) && (tagim.align != V_RIGHT)) {
               justSpaced = false;
            }
         }
      } else {
         currTagType = 0;
      }
      return tagim;
   }


   private void parseMAP() {
   
      String s = null;
      int i;
      
      if (tagOpen == 1) {
         if ((i = getAttr(A_NAME)) != -1) {
            s = doc.attrStrings[i];
         }
         if ((i == -1) || (s == null)) {
            currTagType = 0;         //dump the tag
            return;
         }
         CalImageMap map = new CalImageMap(s);
         if (map.setURL(doc)) {
            addTag();
            addToTag(objPos);
            addObject(map);
            currMap = map;
            mapOpen = true;
         } else {
            currTagType = 0;
         }
      } else {
         if (mapOpen) {
            CalHTMLManager.addImageMap(currMap);
            currMap = null;
            mapOpen = false;
         }
         currTagType = 0;
      }
   }


   private void parseAREA() {
   
      if (mapOpen && (tagOpen == 1)) {
         currMap.addArea(new CalImageMapArea(doc, getAttr(A_SHAPE), getAttr(A_COORDS), getAttr(A_NOHREF),
                                                                      getAttr(A_HREF), getAttr(A_TARGET)));
      }
      currTagType = 0;      //we don't need to retain any info on the tag
   }


   private void parseBASE() {
   
      int i;
      
      if (tagOpen == 1) {
         if ((i = getAttr(A_HREF)) != -1) {
            URL url2 = null;
            try {
               url2 = new URL(doc.attrStrings[i]);
            } catch (MalformedURLException e) {
               //System.out.println("Malformed URL");
               url2 = null;
            }
            if (url2 != null) {
               doc.setBaseAddress(url2, false);
            }
         }
         if ((i = getAttr(A_TARGET)) != -1) {
            doc.setBaseTarget(doc.attrStrings[i]);
         }
      }
      
      currTagType = 0;      //we don't need to retain any info on the tag
   }
   

   private String getTEXTAREAtext() throws IOException {

      //a TEXTAREA's initial contents are determined by the contents between the opening and closing
      //textarea tags. We just read chars in until we find the end tag, but we limit to 8000 chars
      String s = null;
      char[] a = new char[1000];
      int match, i;
      int p    = 0;
      int fin  = 0;
      int mark = 0;
      
      out:
      while(c != END) {
         if (p >= a.length) {
            if (a.length < 8000) {
               char[] b = new char[p];
               System.arraycopy(a, 0, b, 0, p);
               a = b;
            }
         }
         if (p < a.length) {
            a[p++] = c;
         }
         if (c == '<') {
            mark = 1;
            fin  = p - 1;
         } else if (mark > 0) {
            i = c;
            if ((i > 96) && (i < 123)) {   //convert to upper case
               i = ('A' + (i - 'a'));
            }
            switch (mark) {
               case 1: match = '/'; break; case 2: match = 'T'; break; case 3: match = 'E'; break;
               case 4: match = 'X'; break; case 5: match = 'T'; break; case 6: match = 'A'; break;
               case 7: match = 'R'; break; case 8: match = 'E'; break; case 9: match = 'A'; break;
               case 10: match = '>'; break; default: match = 0; break;
            }
            if (i == match) {
               if (mark == 10) {
                  readChar();
                  break out;
               } else {
                  mark++;
               }
            } else {
               mark = 0;
            }
         }
         readChar();
      }
      if (fin > 0) {
         s = new String(a, 0, fin);
      }
      return s;
   }        
      

   private void parseTABLE() {

      if (tagOpen == 1) {
         if (preOn) {
            currTagType = 0;
            return;     //can't have a table in PRE block
         }
         if (tableOpen) {      //then this is a nested table
            if (currTable.captionEnd == 0) {
               currTable.captionEnd = pos - 1;
            }
            if (!dataOpen) {
               currTagType = 0;       //not within TD or TH block, so burn it
               return;
            } else {
               tableNesting++;
               tableStack.push(currTable);
               rowGroupStack.push(currRowGroup);
            }
         }
         currTable = new CalTable(doc, pos, tagPos, charPos, justSpaced, tableNesting);
         addTag();
         addToTag(-1);  //this allocates space in tagArray for the object index of the table (put in later)
         colGroupOpen = false;
         rowGroupOpen = false;
         currRowGroup = null;
         dataOpen     = false;
         rowOpen      = false;
         tableOpen    = true;
         checkPermitLineable();
         justSpaced   = false;
      } else {
         if (!tableOpen) {
            currTagType = 0;    //illegal tag so burn it
            return;
         }
         if (preOn) {
            preOn = false;
         }
         if (currTable.captionEnd == 0) {
            currTable.captionEnd = pos - 1;
         }
         finishTable();
         if (currTable.preSpace) {
            justSpaced = true;
         }
         if (tableNesting > 0) {
            tableNesting--;
            currTable = (CalTable)tableStack.pop();
            if ((currRowGroup = (CalRowGroup)rowGroupStack.pop()) != null) {
               rowGroupOpen = true;
            }    
            colGroupOpen = false;
            rowOpen  = true;
            dataOpen = true;
         } else {
            tableOpen    = false;
            rowOpen      = false;
            dataOpen     = false;
            colGroupOpen = false;
            rowGroupOpen = false;
            currRowGroup = null;
            checkPermitLineable();
            if (permitLineable) {
               addLineable  = true;
            }
         }
      }
   }


   private void finishTable() {
   
      dataOpenCheck();
      currTable.endPos = pos;
      currTable.charEnd = charPos;
      addTag();
      currTable.finalizeCellVector();
      if ((currTable.endPos <= currTable.startPos) || (currTable.rows == 0) ||
                                                                 (currTable.cellVector.size() == 0)) {
         doc.tags[currTable.tagPos] = 0;     //dump table 'cos no </TABLE> tag, or no rows or cells
      } else {
         doc.tags[currTable.tagPos + 1] = (short)objPos;  //space has already have allocated in tag array
         addObject(currTable);
         currTable.parseTable();
      }
   }
   
   
   private void parseCAPTION() {
   
      if (!tableOpen) {
         currTagType = 0;
         return;
      }
      if (tagOpen == 1) {
         if (currTable.captionStart > 0) {
            currTagType = 0;    //only one caption allowed
         } else {
            currTable.captionStart = pos;
            currTable.captionStartCharPos = charPos;
            currTable.captionAlign = V_TOP;
            out:
            for (int i=attrStart; i<attrStart + attrCount; i++) {
               if (doc.attrTypes[i] == A_ALIGN) {
                  switch (doc.attrArgs[i]) {
                     case V_LEFT: case V_RIGHT: case V_BOTTOM:
                     currTable.captionAlign = doc.attrArgs[i];
                     break out;
                  }
               }
            }
            addTag();
         }
      } else {
         if ((currTable.captionStart == 0) || (currTable.captionEnd > 0)) {
            currTagType = 0;      //illegal tag
         } else {
            currTable.captionEnd = pos - 1;
            addTag();
         }
      }
   }
            

   private void parseCOLGROUP() {
     
      if (tableOpen) {
         if (tagOpen == 1) {
            colGroupOpen = true;
            currTable.addColGroup(attrStart, attrStart + attrCount);
         } else {
            colGroupOpen = false;
         }         
      }
      currTagType = 0;
   }


   private void parseROWGROUP() {
   
      if (tableOpen) {
         if (tagOpen == 1) {
            if (currTable.captionEnd == 0) {
               currTable.captionEnd = pos - 1;
            }
            dataOpenCheck();
            rowGroupOpen = true;
            currRowGroup = new CalRowGroup(doc, currTagType, attrStart, attrStart + attrCount,
                                                                             currTable.numRowGroups);
            currTable.numRowGroups++;
         } else {
            if (rowGroupOpen) {
               if (rowOpen) {
                  rowOpen = false;
                  dataOpenCheck();
               }
               rowGroupOpen = false;
               currRowGroup = null;
            }
         }
      }
      
      currTagType = 0;        //we can discard the token
   }
   
   
   private void parseCOL() {
   
      if (tableOpen && (tagOpen == 1)) {
         currTable.addCol(attrStart, attrStart + attrCount, colGroupOpen);
      }
      currTagType = 0;
   }
   

   private void parseTR() {
   
      if (!tableOpen) {
         currTagType = 0;    //illegal tag
         return;
      }
      if (tagOpen == 1) {
         if (currTable.captionEnd == 0) {
            currTable.captionEnd = pos - 1;
         }
         dataOpenCheck();
         rowOpen = true;
         currTable.rows++;
         currTable.addRow(new CalTableRow(doc, attrStart, attrStart + attrCount, currRowGroup,
                                                                                         currTable.rows));
      } else {
         if (rowOpen) {
            rowOpen = false;
            dataOpenCheck();
         }
      }
      currTagType = 0;      //we can discard tag
   }
   
   
   private void parseTHTD() {
      
      if (!tableOpen) {
         currTagType = 0;    //illegal tag
         return;
      }
      if (tagOpen == 1) {
         if (!rowOpen) {
            if (currTable.captionEnd == 0) {
               currTable.captionEnd = pos - 1;
            }
            rowOpen = true;
            currTable.rows++;
            currTable.addRow(new CalTableRow(doc, 0, -1, currRowGroup, currTable.rows));
         }
         if (currTable.currentCell != null) {
            currTable.currentCell.setEndPos(pos - 1);
            currTable.addCell(currTable.currentCell);
         }
         currTable.currentCell = new CalTableCell(doc, attrStart, attrStart + attrCount, currTagType,
                                                                         pos, charPos, currTable.rows);
         dataOpen = true;
         dataOpenPos = pos;
      } else {
         if (dataOpen) {
            dataOpenCheck();
         }
      }
      
      currTagType = 0;    //we can discard the token
   }  


   private void dataOpenCheck() {
   
      if (dataOpen) {
         currTable.currentCell.setEndPos(pos - 1);
         currTable.addCell(currTable.currentCell);
         currTable.currentCell = null;            
         dataOpen = false;
      }
   }


   //returns -1 if no attribute is found
   private int getAttr(int attrType) {

      int j = attrStart + attrCount;
      for (int i=attrStart; i<j; i++) {
         if (doc.attrTypes[i] == attrType) {
            return i;
         }
      }
      return -1;
   }
   
   
   private void addTag() {

      if (tagPos >= doc.tags.length) {
         redimTagArray(tagPos, tagPos << 1);
      }
      doc.tags[tagPos] = (short)(currTagType * tagOpen);
      if (pos >= doc.tokenCodes.length) {
         redimTokenCodes(pos, pos << 1);
      }
      doc.tokenCodes[pos++] = (-tagPos);
      tagPos++;
   }


   private void addToTag(int i) {
   
      if (tagPos >= doc.tags.length) {
         redimTagArray(tagPos, tagPos << 1);
      }
      doc.tags[tagPos++] = (short)i;
   }
   

   private void addObject(Object obj) {

      doc.objectVector.addElement(obj);
      objPos++;
   }


   private void addLoadingImage(CalImage tagim) {

      if (loadingImages == null) {
         loadingImages = new Vector();
      }
      loadingImages.addElement(tagim);
   }


   private void redimCharArray(int oldv, int newv) {
   
      //System.out.println("Redim char array called");
      char[] a = new char[newv];
      synchronized(doc) {
         System.arraycopy(doc.charArray, 0, a, 0, oldv);
         doc.charArray = a;
      }
   }


   private void redimTokenCodes(int oldv, int newv) {
   
      //System.out.println("Redim token codes called");
      int[] a = new int[newv];
      synchronized(doc) {
         System.arraycopy(doc.tokenCodes, 0, a, 0, oldv);
         doc.tokenCodes = a;
      }
   }


   private void redimTagArray(int oldv, int newv) {

      //System.out.println("Redim tag array called");
      synchronized(doc) {
         short[] a = new short[newv];
         System.arraycopy(doc.tags, 0, a, 0, oldv);
         doc.tags = a;
      }
   }
   
   
   private void redimAttrArrays(int oldv, int newv) {
   
      //System.out.println("Redim attr arrays called");
      synchronized(doc) {
         short[] a = new short[newv];
         System.arraycopy(doc.attrTypes, 0, a, 0, oldv);
         doc.attrTypes = a;
         short[] a2 = new short[newv];
         System.arraycopy(doc.attrArgs, 0, a2, 0, oldv);
         doc.attrArgs = a2;
         int[] a3 = new int[newv];
         System.arraycopy(doc.attrVals, 0, a3, 0, oldv);
         doc.attrVals = a3;
         String[] a4 = new String[newv];
         System.arraycopy(doc.attrStrings, 0, a4, 0, oldv);
         doc.attrStrings = a4;
      }
   }

   
   void setIsDialog() {

      isDialog = true;
   }


   void setReload() {

      reload = true;
   }

   

   //special routine for parsing a .txt file. Just reads all the chars in and adds
   //<BR> tags on line breaks. Also inserts a <TT> tag at the start to get monospaced font
   void parseTextDocument() throws IOException {

      lc = 0;
      tagOpen = 1;
      currTagType = TT;
      addTag();
      addToTag(0);
      readChar();
      while (c != END) {
         switch(c) {
            case '\r':
            case '\n': if (lc > 0) {
                          if (pos >= doc.tokenCodes.length) {
                             redimTokenCodes(pos, pos << 1);
                          }
                          doc.tokenCodes[pos++] = lc;
                          lc = 0;
                       }
                       currTagType = BR;
                       addTag();
                       addToTag(0);   //no attributes
                       c2 = c;
                       readChar();
                       if (((c2 == '\n') && (c == '\r')) || ((c2 == '\r') && (c == '\n'))) {
                          readChar();
                       }
                       doc.lineableTokens = pos;
                       break;
            case '\t': if ((charPos + 8) >= doc.charArray.length) {
                          redimCharArray(charPos, charPos + 50000);
                       }
                       for (int i=0; i<8; i++) {
                          doc.charArray[charPos++] = ' ';
                       }
                       lc += 8;
                       readChar();
                       break;
              default: if ((charPos) >= doc.charArray.length) {
                         redimCharArray(charPos, charPos + 50000);
                       }
                       doc.charArray[charPos++] = c;
                       lc++;
                       readChar();
                       break;
         }
      }
   }

}
