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
 
package manyminds.src.manyminds.util;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;


public class
XMLEncoder {

    public static String
    encode(String s) {
        StringBuffer retVal = new StringBuffer();
        StringCharacterIterator iter = new StringCharacterIterator(s);
        for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
            if ((Character.isLetterOrDigit(c)) || (Character.isWhitespace(c))) {
                retVal.append(c);
            } else if (c == '<') {
                retVal.append("&lt;");
            } else if (c == '>') {
                retVal.append("&gt;");
            } else if (c == '&') {
                retVal.append("&amp;");
            } else {
                retVal.append(c);
            }
        }
        return retVal.toString();
    }
}