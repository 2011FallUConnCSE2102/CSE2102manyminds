/*
 * StringExploder.java
 *
 * Written by Eric Eslinger
 * Copyright © 1998-1999 University of California
 * All Rights Reserved.
 *
 * Agenda
 *
 * History
 *	15 DEC 98 EME Created today.
 *	18 JUN 99 EME Added javadoc comments.
 */
 
/*  Copyright (C) 1998-2002 Regents of the University of California
 *  This file is part of ManyMinds.
 *
 *  ManyMinds is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  ManyMinds is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with ManyMinds; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package manyminds.util;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import manyminds.debug.Level;
import manyminds.debug.Logger;

/**
 *	A function object whose task is to take a string and turn it into an array of "words".  A word is
 *	either a sequence of non-whitespace characters, or a parentheses delimited list of words.  For
 *	example, (alpha beta (gamma delta) ralph) would expand to alpha, beta, (gamma delta), ralph.
 *	@author Eric M Eslinger
 */

public class StringExploder {

    private static Logger logger = Logger.getLogger("manyminds");

    public static List explode(String in) {
        ArrayList ret_val = new ArrayList();
        in = in.trim();
        int first,last,depth=0;
        if (in.length() > 0) {
            /*if ((in.charAt(0) == '(') && (in.charAt(in.length()-1) == ')')) {
                in = in.substring(1,in.length()-1).trim();
            }*/
            first = 0;
            last = in.length() - 1;
            int y = last;
            boolean inquote = false;
            for (int x = first; x <= last; ++x) {
                if (in.charAt(x) == '\\') {
                        ++x;
                } else if (depth == 0) {
                    if ((in.charAt(x) == '\"') && (!inquote)) {
                        inquote = true;
                        y = x;
                    } else if (inquote) {
                        if (in.charAt(x) == '\"') {
                            inquote = false;
                            ret_val.add(in.substring(y+1,x));
                            y = last;
                        }
                    } else if (in.charAt(x) == '(') {
                        y = x; 
                        //y = x + 1; //don't add the enclosing parens
                        depth = 1;
                    } else if ((y != last) && (Character.isWhitespace(in.charAt(x)))) {
                        ret_val.add(in.substring(y,x));
                        y = last;
                    } else if (x == last) {
                        ret_val.add(in.substring(y,x+1));
                        y = last;
                    } else if ((!(Character.isWhitespace(in.charAt(x)))) && (y == last)) {
                        y = x;
                    }
                } else if (in.charAt(x) == '(') {
                    ++depth;
                } else if (in.charAt(x) == ')') {
                    --depth;
                    if (depth==0) {
                        ret_val.add(in.substring(y,x+1));
                        //ret_val.add(in.substring(y,x)); //don't add parens
                        y = last;
                    }
                }
            }
        }
        return ret_val;
    }

    public static String
    stripParens(String in) {
        if ((in.charAt(0) == '(') && (in.charAt(in.length()-1) == ')')) {
            in = in.substring(1,in.length()-1).trim();
        }
        return in;
    }    

    public static String
    cleanWhitespace(String in) {
        return in.replace('\n',' ').replace('\t',' ').replace('\r',' ').trim();
    }

    public static String fileToString(File infile) {
        try {
            FileReader in = new FileReader(infile);
            StringWriter sw = new StringWriter();
            int c=0;
            while ((c = in.read()) != -1) {
                sw.write(c);
            }
            in.close();
            return sw.toString();
        } catch (IOException ioe) {
            logger.log(Level.WARNING,"Trouble converting file to string"+infile.toString(),ioe);
            return ""; 
        }
    }
    
    public static String implodeString(List l) {
        Iterator it = l.iterator();
        StringBuffer retVal = new StringBuffer();
        while (it.hasNext()) {
            retVal.append(it.next().toString());
            if (it.hasNext()) {
                retVal.append(" ");
            }
        }
        return retVal.toString();
    }

}