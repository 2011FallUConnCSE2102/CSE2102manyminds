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

/*
 * Message.java
 * Part of the ManyMinds multi-agent inquiry support environment
 *
 * Written by Eric Eslinger
 * Copyright © 1998-1999 University of California
 * All Rights Reserved.
 * 
 * Agenda
 *
 * History
 *	16 OCT 98 EME Created today
 *  26 FEB 99 EME Made Message serializable
 *	16 JUN 99 EME Added javadoc comments
 */ 
 

import java.io.Serializable;

import manyminds.debug.Logger;

/**
 * Defines the Message abstract base class, fundamental for message passing.  Implementations
 * of this class will include syntax specific (ie KQML) processing.
 *
 * @author Eric M Eslinger
 */

public abstract class Message extends Object implements Cloneable, Serializable {
/**
 *	Tells the message to deliver itself the best it can.
 * 	@return true if sucessful, false if not
 */
	public abstract boolean deliver();
        protected static Logger logger = Logger.getLogger("manyminds.communication");
/**
 *	Sets a parameter in the message to some value.
 *	@param key the parameter to set (sender, etc)
 *	@param val the value to set the key to
 */
	public abstract void setParameter(String key, String val);
/**
 *	Gets some parameter from the message.
 *	@param key the parameter to get
 *	@return the String value of that parameter
 *	@exception NoSuchValueException if key is not set in the message
 */
	public abstract String getParameter(String key) throws NoSuchValueException;
/**
 *	Sets the messages parameters from some string.
 *	@param mesg the string representation of the message
 *	@exception SyntaxException the string had poor syntax
 */
	public abstract void fromString(String mesg) throws SyntaxException;
/**
 *	All messages must be cloneable, in order for delivery to work appropriately.
 *	@return a deep copy of this message.
 */
	public abstract Object clone();
/**
 *	Verify the syntax of a string for a particular message type.
 *	@param mesg the string representation of a message
 *	@return true if the message has good syntax false otherwise
 */
	public static boolean verify(String mesg) {
		return true;
	}
}