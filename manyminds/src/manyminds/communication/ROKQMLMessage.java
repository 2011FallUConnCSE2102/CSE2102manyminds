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
 package manyminds.src.manyminds.communication;
import java.io.Serializable;

/**
 * related to the KQMLMessage object, but uses a read-only buffer.  Once the message is 
 * constructed, it is impossible to modify.  Because of this, passing messages around
 * can be by reference rather than passing copies of messages around.  Copying a KQMLMessage
 * isn't cheap, it needs to be turned into a string and then re-parsed into its data format, so 
 * pass by reference seems like a good idea.
 * 
*	@author Eric M Eslinger
*	@see manyminds.communication.Message
*@see manyminds.communication.KQMLMessage
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
    *	@return this message.
    */
    public synchronized Object clone() {
        return this;
    }
    
}
