/*
 * Copyright (C) 1998-2002 Regents of the University of California This file is
 * part of ManyMinds.
 * 
 * ManyMinds is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * ManyMinds is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ManyMinds; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package manyminds.history;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleChunkingPolicy implements ChunkingPolicy {

	public static final DateFormat CHUNK_DAY = new SimpleDateFormat("yy/MM/dd");

	public static String getChunkString(HistoryItem hi) {
		try {
			if (HistoryItem.CHUNK_COMMENT.equals(hi
					.getProperty(HistoryItem.ITEM_TYPE))) {
				return hi.getProperty(HistoryItem.CHUNK_NAME).toString();
			} else {
				StringBuffer retVal = new StringBuffer();
				retVal.append(hi.getProperty(HistoryItem.COMPUTER).toString()
						.toLowerCase());
				retVal.append(" ");
				retVal.append(hi.getProperty(HistoryItem.PERIOD).toString());
				retVal.append(" ");
				try {
					Date d = (Date) hi.getProperty(HistoryItem.BEGIN_DATE);
					retVal.append(CHUNK_DAY.format(d));
				} catch (ClassCastException cce) {
					retVal.append("Unknown Date");
				}
				return retVal.toString();
			}
		} catch (Throwable t) {
			System.err.println("Error chunking item");
			System.err.println(hi.toXML());
			t.printStackTrace();
			return null;
		}

	}

	public Map chunkData(HistoryDatabase hd) {
		Iterator it = hd.iterator();
		HashMap retVal = new HashMap();
		while (it.hasNext()) {
			HistoryItem hi = (HistoryItem) it.next();
			if (hi != null) {
				String chunkKey = getChunkString(hi);
				if (chunkKey != null) {
					Object o = retVal.get(chunkKey);
					if (o == null) {
						o = new HistoryChunk(hd);
						((HistoryChunk) o).setName(chunkKey);
						retVal.put(chunkKey, o);
					}
					((HistoryChunk) o).addItem((Long) hi
							.getProperty(HistoryItem.KEY));
				}
			}
		}
		return retVal;
	}

}