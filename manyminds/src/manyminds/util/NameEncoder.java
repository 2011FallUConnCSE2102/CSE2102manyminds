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
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.BitSet;

public class NameEncoder {
	private NameEncoder() {}

	static BitSet dontNeedEncoding;
	static final int caseDiff = ('a' - 'A');

	static {
		dontNeedEncoding = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; ++i) {
			dontNeedEncoding.set(i);
		}
		for (i = 'A'; i <= 'Z'; ++i) {
			dontNeedEncoding.set(i);
		}
		for (i = '0'; i <= '9'; ++i) {
			dontNeedEncoding.set(i);
		}
		/*dontNeedEncoding.set(' ');  encoding a space to a + is done in the encode() method */
		dontNeedEncoding.set('-');
		dontNeedEncoding.set('/');
		dontNeedEncoding.set('_');
		dontNeedEncoding.set('.');
		dontNeedEncoding.set('*');
	}

	public static String encode(String s) {
		int maxBytesPerChar = 10;
		StringBuffer out = new StringBuffer(s.length());
		ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
		OutputStreamWriter writer = new OutputStreamWriter(buf);

		for (int i = 0; i < s.length(); i++) {
			int c = (int)s.charAt(i);
			if (dontNeedEncoding.get(c)) {
				if (c == ' ') {
					c = '+';
				}
				out.append((char)c);
			} else {
				// convert to external encoding before hex conversion
				try {
					writer.write(c);
					writer.flush();
				} catch(IOException e) {
					buf.reset();
					continue;
				}
				byte[] ba = buf.toByteArray();
				for (int j = 0; j < ba.length; j++) {
					out.append('%');
					char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
					// converting to use uppercase letter as part of
					// the hex value if ch is a letter.
					if (Character.isLetter(ch)) {
						ch -= caseDiff;
					}
					out.append(ch);
					ch = Character.forDigit(ba[j] & 0xF, 16);
					if (Character.isLetter(ch)) {
						ch -= caseDiff;
					}
					out.append(ch);
				}
				buf.reset();
			}
		}
	return out.toString();
	}

	public static URL fileToURL(File f) throws MalformedURLException, IOException {
		String path = f.getCanonicalPath();
		if (File.separatorChar != '/') {
			path = path.replace(File.separatorChar, '/');
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (!path.endsWith("/") && f.isDirectory()) {
			path = path + "/";
		}
		return new URL("file","127.0.0.1",encode(path));
	}
}

