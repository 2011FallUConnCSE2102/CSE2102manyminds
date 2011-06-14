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

import java.util.Date;

public class HistorySpan {

	private String myType = "";

	private Date myBegin;

	private Date myEnd;

	private HistoryItem myHistoryItem;

	public HistorySpan(String spanType, Date begin, Date end) {
		myType = spanType;
		myBegin = begin;
		myEnd = end;
	}

	public HistorySpan(HistoryItem hi) {
		this(hi.getProperty(HistoryItem.ITEM_TYPE).toString(), (Date) hi
				.getProperty(HistoryItem.BEGIN_DATE), (Date) hi
				.getProperty(HistoryItem.END_DATE));
		myHistoryItem = hi;
	}

	public HistorySpan(String spanType) {
		this(spanType, null, null);
	}

	public HistoryItem getHistoryItem() {
		return myHistoryItem;
	}

	public HistorySpan(String spanType, Date begin) {
		this(spanType, begin, null);
	}

	public void setBegin(Date begin) {
		myBegin = begin;
	}

	public void setEnd(Date end) {
		myEnd = end;
	}

	public void absorb(Date begin, Date end) {
		if ((myBegin == null) || (myBegin.compareTo(begin) > 0)) {
			myBegin = begin;
		}
		if ((myEnd == null) || (myEnd.compareTo(end) < 0)) {
			myEnd = end;
		}
	}

	public Date getBegin() {
		return myBegin;
	}

	public Date getEnd() {
		return myEnd;
	}

	public String getType() {
		return myType;
	}

}