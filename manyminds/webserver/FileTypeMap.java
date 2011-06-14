/*
 * FileTypeMap.java
 *
 * Written by Eric Eslinger
 * Copyright © 1998-1999 University of California
 * All Rights Reserved.
 *
 * Agenda
 *
 * History
 *	16 JUN 99 EME commented code.
 *
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
 package manyminds.webserver;

import java.util.HashMap;


/**
 *	Simple map from file extension to MIME type for use in a webserver.
 *	@author Eric M Eslinger
 */
class FileTypeMap {
	private static HashMap _map = new HashMap();

	static {
		fillMap();
	}

	/**
	 *	Add a sufix to the map.
	 *	@param key The suffix to add
	 *	@param val The mime type to map the suffix to
	 */
	static synchronized void setSuffix(String key, String val) {
		_map.put(key, val);
	}

	/**
	 *	Look up a sufix from the map.
	 *	@param key The suffix to look up
	 *	@return The mime type the suffix maps to
	 */
	static synchronized String getSuffix(String key) {
		return _map.get(key).toString();
	}
	
	
	static private void fillMap() {
		setSuffix("", "content/unknown");
		setSuffix(".uu", "application/octet-stream");
		setSuffix(".exe", "application/octet-stream");
		setSuffix(".ps", "application/postscript");
		setSuffix(".zip", "application/zip");
		setSuffix(".sh", "application/x-shar");
		setSuffix(".tar", "application/x-tar");
		setSuffix(".snd", "audio/basic");
		setSuffix(".au", "audio/basic");
		setSuffix(".wav", "audio/x-wav");
		setSuffix(".gif", "image/gif");
		setSuffix(".jpg", "image/jpeg");
		setSuffix(".jpeg", "image/jpeg");
		setSuffix(".htm", "text/html");
		setSuffix(".html", "text/html");
		setSuffix(".text", "text/plain");
		setSuffix(".c", "text/plain");
		setSuffix(".cc", "text/plain");
		setSuffix(".c++", "text/plain");
		setSuffix(".h", "text/plain");
		setSuffix(".pl", "text/plain");
		setSuffix(".txt", "text/plain");
                setSuffix(".swf", "application/x-shockwave-flash");
		setSuffix(".java", "text/plain");
	}
}