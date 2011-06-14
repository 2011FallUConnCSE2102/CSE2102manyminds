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
 package manyminds.communication;
import java.io.Serializable;

/**
*	Implements the Message contract for the concrete <a href="http://www.cs.umbc.edu/kqml/">KQML</a> syntax.  
*  Is a MirroredJessObject so it can also exist as a jess "shadowed fact".
*	@author Eric M Eslinger
*	@see manyminds.communication.Message
*	@see manyminds.jess_extensions.MirroredJessObject
*/
public class ROKQMLMessage extends KQMLMessage implements Serializable {
    /**
    *	Empty constructor, rarely used.
    */
    public ROKQMLMessage() {}
    private boolean buildingMyself = false;
    /**
    *	Construct a message from a string representing some KQML message.
    *	@param expr The string expression representing a KQML message
    *	@exception SyntaxException if expr is poor syntax.
    */

    public ROKQMLMessage(String s) throws SyntaxException {
        super(s);
    }

    public synchronized void fromString(String KQML) throws SyntaxException {
        buildingMyself = true;
        super.fromString(KQML);
        buildingMyself = false;
    }
    
/**
*	Sets some parameter of the message. Probably will quietly fail, actually, since the message is Read Only
*	@param key the parameter to set.
*	@param val the value to which key is set.
*/
    public synchronized void setParameter(String key, String val) {
        if (buildingMyself) {
            myParameters.put(key,val);
        }
    }
    
    /**
    *	Really doesn't clone the message.  KQML Messages are now Read Only
    *	@return a deep copy of the message.
    */
    public synchronized Object clone() {
        return this;
    }
    
}
