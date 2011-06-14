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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import manyminds.util.StateDateFormat;
import manyminds.util.StringExploder;

public class HistoryItem implements Cloneable {

	private PropertyChangeSupport myPCS = new PropertyChangeSupport(this);

	private boolean dirty = false;

	private HashMap myProperties = new HashMap();

	public final static String DOCUMENT_CHANGE = "Document Change";

	public final static String COMMENT_CHANGE = "Document Change: Comment Made";

	public final static String PAGE_COUNT_CHANGE = "Number of Pages Changed";

	public final static String PAGE_FLIP = "Page Flip";

	public final static String STATE = "State";

	public final static String ADVICE_ADD = "Adding advice";

	public final static String ADVICE_DISMISS = "Dismissed advice";

	public final static String ADVICE_MORE = "Reading advice";

	public final static String ADVICE_VIEW = "Page Viewed in Browser";

	public final static String RATER_CHANGE = "Rater Change";

	public final static String VIDEO_RESOURCE = "Video Resource";

	public final static String VIDEO_TRANSCRIPT = "Video Transcript";

	public final static String OTHER_RESOURCE = "Other Resource";

	public final static String CHUNK_COMMENT = "Chunk Comment";

	public final static String ANNOTATION = "Annotation";

	public final static String BEGIN_DATE = "Begin Time";

	public final static String END_DATE = "End Time";

	public final static String ITEM_TYPE = "Type";

	public final static String WHAT_HAPPENED = "What Happened";

	public final static String CHANGE_FROM = "Change From";

	public final static String CHANGE_TO = "Change To";

	public final static String CHANGE_SECTION = "Section";

	public final static String ITEM_ANNOTATION = "Annotation";

	public final static String CHUNK_NAME = "Chunk Name";

	public final static String KEY = "uniqid";

	public final static String COMPUTER = "Computer";

	public final static String PERIOD = "Period";

	public final static String PROJECT = "Project";

	public static final DateFormat DATE_PARSE_FORMAT = new StateDateFormat(
			"yyyy-MM-dd hh:mm:ss");

	public static final DateFormat SHORT_DATE_FORMAT = new StateDateFormat(
			"M/dd/yy hh:mm:ss a");

	public static final DateFormat UPDATING_DATE_FORMAT = new StateDateFormat(
			"M/dd/yy ");

	public static final DateFormat MICRO_DATE_FORMAT = new StateDateFormat(
			"hh:mm:ss a");

	public static final DateFormat NOAM_DATE_FORMAT = new StateDateFormat(
			"M/dd/yy hh:mm:ss");

	public HistoryItem() {
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean b) {
		dirty = b;
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		myPCS.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		myPCS.removePropertyChangeListener(pcl);
	}

	public static String formatObject(Object v) {
		if (v instanceof Date) {
			return SHORT_DATE_FORMAT.format((Date) v);
		} else {
			return v.toString();
		}
	}

	public void setProperty(String s, Object v) {
		if (v instanceof String) {
			if (KEY.equals(s)) {
				v = new Long((String) v);
			} else if ((BEGIN_DATE.equals(s)) || (END_DATE.equals(s))) {
				try {
					v = SHORT_DATE_FORMAT.parse((String) v);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		Object o = myProperties.get(s);
		myProperties.put(s, v);
		myPCS.firePropertyChange(s, o, v);
		dirty = true;

		if (VIDEO_RESOURCE.equals(myProperties.get(ITEM_TYPE))) {
			if (CHANGE_FROM.equals(s) || BEGIN_DATE.equals(s)) {
				updateVideoEnd();
			}
		}

	}

	public Object getProperty(String s) {
		Object o = myProperties.get(s);
		if (o != null) {
			return o;
		} else {
			return "";
		}
	}

	public void resolveUpdate(HistoryItem newVal) {
		myProperties.clear();
		Iterator it = newVal.myProperties.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			myProperties.put(key, newVal.myProperties.get(key));
		}
	}

	public HistoryItem(String line) {
		Iterator it = StringExploder.explode(line).iterator();
		String st = it.next().toString();
		String et = it.next().toString();
		String ty = it.next().toString();
		String what = it.next().toString();
		String from = it.next().toString();
		String to = it.next().toString();
		String cs = it.next().toString();
		String a = it.next().toString();

		setProperty(BEGIN_DATE, st);
		setProperty(END_DATE, et);
		setProperty(ITEM_TYPE, ty);
		setProperty(WHAT_HAPPENED, what);
		setProperty(CHANGE_FROM, from);
		setProperty(CHANGE_TO, to);
		setProperty(CHANGE_SECTION, cs);
		setProperty(ITEM_ANNOTATION, a);
	}

	public Object clone() {
		HistoryItem retVal = new HistoryItem();
		Iterator it = myProperties.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			retVal.myProperties.put(key, myProperties.get(key));
		}
		return retVal;
	}

	public String toCSV() {
		StringBuffer retVal = new StringBuffer();
		retVal.append("\"");
		retVal.append(SHORT_DATE_FORMAT.format((Date) getProperty(BEGIN_DATE)));
		retVal.append("\",");
		retVal.append("\"");
		retVal.append(SHORT_DATE_FORMAT.format((Date) getProperty(END_DATE)));
		retVal.append("\",");
		retVal.append("\"");
		retVal.append(getProperty(ITEM_TYPE).toString());
		retVal.append("\",");
		retVal.append("\"");
		retVal.append(getProperty(WHAT_HAPPENED).toString());
		retVal.append("\",");
		retVal.append("\"");
		retVal.append(getProperty(CHANGE_FROM).toString());
		retVal.append("\",");
		retVal.append("\"");
		retVal.append(getProperty(CHANGE_TO).toString());
		retVal.append("\",");
		retVal.append("\"");
		retVal.append(getProperty(CHANGE_SECTION).toString());
		retVal.append("\",");
		retVal.append("\"");
		retVal.append((getProperty(ITEM_ANNOTATION).toString()));
		retVal.append("\"\n");
		return retVal.toString();
	}

	public String toXML() {
		StringBuffer retVal = new StringBuffer();
		retVal.append("<history-item>\n");
		Iterator it = myProperties.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next().toString();
			Object val = myProperties.get(key);
			if (val == null) {
				val = "";
			}
			retVal.append("<property key=\"");
			retVal.append(key);
			retVal.append("\"><![CDATA[");
			if (val instanceof Date) {
				retVal.append(SHORT_DATE_FORMAT.format((Date) val));
			} else {
				retVal.append(val.toString());
			}
			retVal.append("]]></property>\n");
		}
		retVal.append("</history-item>\n");
		return retVal.toString();
	}

	public int getDuration() {
		long endTime = ((Date) getProperty(END_DATE)).getTime();
		long startTime = ((Date) getProperty(BEGIN_DATE)).getTime();
		long elapsedTime = endTime - startTime;
		return (int) (elapsedTime / 1000);
	}

	public String getSourceWS() {
		StringBuffer retVal = new StringBuffer();
		if ((getProperty(ITEM_TYPE).equals(DOCUMENT_CHANGE))
				&& (getProperty(WHAT_HAPPENED).toString().indexOf("Dispat") < 0)) {
			retVal.append("WS: ");
		} else if (getProperty(ITEM_TYPE).equals(COMMENT_CHANGE)) {
			retVal.append("RC: ");
		} else if (getProperty(ITEM_TYPE).equals(RATER_CHANGE)) {
			retVal.append("R: ");
		} else if (getProperty(ITEM_TYPE).equals(PAGE_FLIP)) {
			retVal.append("Page");
		}
		if (retVal.toString().length() > 0) {
			String sourceWS = getProperty(WHAT_HAPPENED).toString();
			if (sourceWS.startsWith("comments-")) {
				sourceWS = sourceWS.substring("comments-".length());
			}
			sourceWS = sourceWS.substring(0, sourceWS.length() - 2);
			if (retVal.toString().startsWith("Page")) {
				sourceWS = "";
			}
			retVal.append(sourceWS);
		}
		return retVal.toString();
	}

	public String toLatex() {
		StringBuffer retVal = new StringBuffer();
		retVal.append(getSourceWS());
		retVal.append(" & ");
		retVal.append(getDuration());
		retVal.append(" & ");
		retVal.append(escapeForLatex(getProperty(CHANGE_TO).toString()));
		retVal.append(" \\\\");
		return retVal.toString();
	}

	public static String escapeForLatex(String s) {
		char escapedChars[] = { '\\', '_', '&', '$', '#', '%', '{', '}', '^',
				'~' };
		StringBuffer retVal = new StringBuffer(s);
		for (int i = 0; i < escapedChars.length; ++i) {
			int lastFound = 0;
			String escapingString = (new Character(escapedChars[i])).toString();
			while (lastFound >= 0) {
				lastFound = retVal.toString()
						.indexOf(escapingString, lastFound);
				if (lastFound >= 0) {
					retVal.insert(lastFound, '\\');
					lastFound += 2;
				}
			}
		}
		char breakableChars[] = { '+', '/', '.' };
		for (int i = 0; i < breakableChars.length; ++i) {
			int lastFound = 0;
			String breakingString = (new Character(breakableChars[i]))
					.toString();
			while (lastFound >= 0) {
				lastFound = retVal.toString()
						.indexOf(breakingString, lastFound);
				if (lastFound >= 0) {
					retVal.insert(lastFound, "\\-");
					lastFound += 3;
				}
			}
		}
		char mathifyChars[] = { '<', '>' };
		for (int i = 0; i < mathifyChars.length; ++i) {
			int lastFound = 0;
			String mathifyString = (new Character(mathifyChars[i])).toString();
			while (lastFound >= 0) {
				lastFound = retVal.toString().indexOf(mathifyString, lastFound);
				if (lastFound >= 0) {
					retVal.insert(lastFound + 1, "$");
					retVal.insert(lastFound, "$");
					lastFound += 3;
				}
			}
		}
		return retVal.toString();
	}

	protected Date parseDate(String s) {
		try {
			if ((!s.endsWith("AM")) && (!s.endsWith("PM"))) {
				Date tempDate = NOAM_DATE_FORMAT.parse(s, new ParsePosition(0));
				Calendar tempCal = new GregorianCalendar();
				tempCal.setTime(tempDate);
				if (tempCal.get(Calendar.HOUR_OF_DAY) < 8) {
					tempCal.set(Calendar.HOUR_OF_DAY, tempCal
							.get(Calendar.HOUR_OF_DAY) + 12);
				}
				tempDate = tempCal.getTime();
				s = SHORT_DATE_FORMAT.format(tempDate);
			}
			return SHORT_DATE_FORMAT.parse(s, new ParsePosition(0));
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	protected void updateVideoEnd() {
		try {
			if (VIDEO_RESOURCE.equals(getProperty(ITEM_TYPE))
					&& (!getProperty(CHANGE_FROM).toString().trim().equals(""))) {
				Object o = getProperty(BEGIN_DATE);
				if (o instanceof Date) {
					long begin = ((Date) o).getTime();
					File videoFile = new File(System
							.getProperty("manyminds.home")
							+ System.getProperty("file.separator")
							+ "VideoFiles"
							+ System.getProperty("file.separator")
							+ getProperty(CHANGE_FROM).toString());
					long end = begin
							+ VideoView.getMovieLength("file://"
									+ videoFile.getAbsolutePath());
					setProperty(END_DATE, new manyminds.util.StatedDate(end,
							SHORT_DATE_FORMAT));
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}