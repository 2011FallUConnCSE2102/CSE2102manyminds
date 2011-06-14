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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.swing.ProgressMonitor;

public class HistoryDatabase {

	private HashMap myData;

	public HistoryDatabase() {
		myData = new HashMap();
	}

	public synchronized void addItem(HistoryItem hi) {
		Object o = hi.getProperty(HistoryItem.KEY);
		if ((o == null) || ("".equals(o))) {
			System.err.println("refusing to add " + hi.toXML());
		} else {
			myData.put(hi.getProperty(HistoryItem.KEY), hi.clone());
		}
	}

	public synchronized Long getNewKey() {
		long keylong = (long) (Math.random() * Long.MAX_VALUE);
		Long retVal = new Long(keylong);
		while (myData.containsKey(retVal)) {
			keylong = (long) (Math.random() * Long.MAX_VALUE);
			retVal = new Long(keylong);
		}
		myData.put(retVal, null);
		return retVal;
	}

	public synchronized HistoryItem checkoutItem(Long key) {
		Object o = myData.get(key);
		/*
		 * if (o == null) { o = new HistoryItem();
		 * ((HistoryItem)o).setProperty(HistoryItem.KEY,getNewKey());
		 * addItem((HistoryItem)o); }
		 */
		o = ((HistoryItem) o).clone();
		return (HistoryItem) o;
	}

	public synchronized void removeItem(HistoryItem hi) {
		Object key = hi.getProperty(HistoryItem.KEY);
		Object o = myData.remove(key);
	}

	public void initiateSave() {
		manyminds.application.DataReplayApplication.checkSave();
	}

	public void saveYourself(java.io.OutputStream os)
			throws java.io.IOException {
		os.write(manyminds.util.XMLFactory.getXMLHeader("history-database")
				.getBytes());
		os.write("<history-database>\n".getBytes());
		Iterator it = myData.values().iterator();
		int outCount = myData.values().size() - 1;
		int i = 0;
		ProgressMonitor pm = new ProgressMonitor(null, "Saving", "Saving", i,
				outCount);
		pm.setMillisToPopup(1000);
		while (it.hasNext()) {
			HistoryItem hi = (HistoryItem) it.next();
			pm.setProgress(i++);
			try {
				if (hi != null) {
					os.write(hi.toXML().getBytes());
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		pm.close();
		os.write("</history-database>\n".getBytes());
	}

	public String summarizeData() {
		try {
			TreeMap dateMap = null;
			TreeMap compMap = null;
			TreeMap periodMap = null;

			DateFormat df = new SimpleDateFormat("MM/dd/yy");

			Iterator it = myData.values().iterator();
			int outCount = myData.values().size() - 1;
			int i = 0;
			ProgressMonitor pm = new ProgressMonitor(null, "Outputting",
					"Stuff", i, outCount);
			pm.setMillisToPopup(1000);

			while (it.hasNext()) {
				pm.setProgress(i++);
				HistoryItem hi = (HistoryItem) it.next();
				if (hi != null) {
					Object o = hi.getProperty(HistoryItem.BEGIN_DATE);
					if (o != null) {
						String date = df.format((Date) o);
						String computer = hi.getProperty(HistoryItem.COMPUTER)
								.toString();
						String period = hi.getProperty(HistoryItem.PERIOD)
								.toString();

						if (periodMap == null) {
							periodMap = new TreeMap();
						}

						dateMap = (TreeMap) periodMap.get(period);
						if (dateMap == null) {
							dateMap = new TreeMap();
							((TreeMap) periodMap).put(period, dateMap);
						}

						compMap = (TreeMap) dateMap.get(df.parse(date));
						if (compMap == null) {
							compMap = new TreeMap();
							dateMap.put(df.parse(date), compMap);
						}

						if (!compMap.containsKey(computer)) {
							compMap.put(computer, "No Video");
						}
						if (HistoryItem.VIDEO_RESOURCE.equals(hi
								.getProperty(HistoryItem.ITEM_TYPE))) {
							compMap.put(computer, "Video");
						}
					}
				}
			}

			StringBuffer retVal = new StringBuffer();
			it = periodMap.keySet().iterator();
			retVal.append("Period, Date, Computer, Video\n");
			while (it.hasNext()) {
				String currentPeriod = it.next().toString();
				TreeMap currentDateMap = (TreeMap) periodMap.get(currentPeriod);
				Iterator dateIterator = currentDateMap.keySet().iterator();
				while (dateIterator.hasNext()) {
					String currentDate = df.format((Date) dateIterator.next());
					TreeMap currentComputerMap = (TreeMap) currentDateMap
							.get(df.parse(currentDate));
					Iterator computerIterator = currentComputerMap.keySet()
							.iterator();
					while (computerIterator.hasNext()) {
						String currentComputer = computerIterator.next()
								.toString();
						String videoStatus = currentComputerMap.get(
								currentComputer).toString();
						retVal.append(currentDate);
						retVal.append(", ");
						retVal.append(currentPeriod);
						retVal.append(", ");
						retVal.append(currentComputer);
						retVal.append(", ");
						retVal.append(videoStatus);
						retVal.append("\n");
					}
				}
			}
			it = periodMap.keySet().iterator();
			while (it.hasNext()) {
				String currentPeriod = it.next().toString();
				TreeMap currentDateMap = (TreeMap) periodMap.get(currentPeriod);
				Iterator dateIterator = currentDateMap.keySet().iterator();
				retVal.append("Period: ");
				retVal.append(currentPeriod);
				retVal.append("\n");
				while (dateIterator.hasNext()) {
					String currentDate = df.format((Date) dateIterator.next());
					TreeMap currentComputerMap = (TreeMap) currentDateMap
							.get(df.parse(currentDate));
					Iterator computerIterator = currentComputerMap.keySet()
							.iterator();
					retVal.append("\tDate: ");
					retVal.append(currentDate);
					retVal.append("\n");
					while (computerIterator.hasNext()) {
						String currentComputer = computerIterator.next()
								.toString();
						String videoStatus = currentComputerMap.get(
								currentComputer).toString();
						retVal.append("\t\t");
						retVal.append(currentComputer);
						retVal.append(": ");
						retVal.append(videoStatus);
						retVal.append("\n");
					}
				}
			}
			return retVal.toString();
		} catch (Throwable t) {
			t.printStackTrace();
			return "";
		}
	}

	public synchronized void checkinItem(HistoryItem hi) {
		Object key = hi.getProperty(HistoryItem.KEY);
		Object o = myData.get(key);
		if (o == null) {
			myData.put(key, hi);
		} else {
			((HistoryItem) o).resolveUpdate(hi);
		}
	}

	public Iterator iterator() {
		return Collections.unmodifiableCollection(myData.values()).iterator();
	}

	/**
	 * 
	 * @return
	 */
	public String aggregateTimeData() {
		StringBuffer retVal = new StringBuffer();
		HashMap students = new HashMap();

		SearchingChunker.SearchingChunkerCriterion fmCrit = new SearchingChunker.SearchingChunkerCriterion(
				"Annotation", true, SearchingChunker.CONTAINS, "Day: FM");
		LinkedList crits = new LinkedList();
		crits.add(fmCrit);
		SearchingChunker.SearchingChunkerMatcher fmMatcher = new SearchingChunker.SearchingChunkerMatcher(
				crits, true);
		LinkedList matchers = new LinkedList();
		matchers.add(fmMatcher);
		SearchingChunker sc = new SearchingChunker(matchers);
		HashMap periodMap = new HashMap();

		for (Iterator it = sc.chunkData(this).values().iterator(); it.hasNext();) {
			HistoryChunk hc = (HistoryChunk) it.next();
			hc.checkOut();
			if (hc.getSize() > 0) {
				HistoryItem hi = hc.getByIndex(0);
				String periodString = (String) hi
						.getProperty(HistoryItem.PERIOD);
				String computerString = (String) hi
						.getProperty(HistoryItem.COMPUTER);
				long[] chunkCount = null;
				HashMap computerMap = (HashMap) periodMap.get(periodString);
				if (computerMap == null) {
					computerMap = new HashMap();
					periodMap.put(periodString, computerMap);
				}

				HashMap sectionMap = (HashMap) computerMap.get(computerString);
				if (sectionMap == null) {
					sectionMap = new HashMap();
					computerMap.put(computerString, sectionMap);
				}

				ArrayList al = new ArrayList();
				for (Iterator actionIt = hc.actionIterator(); actionIt
						.hasNext();) {
					al.add(actionIt.next());
				}

				ArrayList sl = new ArrayList();
				for (Iterator sectionIt = hc.sectionIterator(); sectionIt
						.hasNext();) {
					sl.add(sectionIt.next());
				}

				if ((al.size() > 0) && (sl.size() > 0)) {

					long actionStart = 0, actionEnd = 0, sectionStart = 0, sectionEnd = 0;
					int currentAction = 0;
					int currentSection = 0;

					HistorySpan actionSpan = (HistorySpan) al
							.get(currentAction);
					HistorySpan sectionSpan = (HistorySpan) sl
							.get(currentSection);

					actionStart = actionSpan.getBegin().getTime();
					actionEnd = actionSpan.getEnd().getTime();
					sectionStart = sectionSpan.getBegin().getTime();
					sectionEnd = sectionSpan.getEnd().getTime();

					while (actionSpan != null) {

						long distance = 0;

						String section = sectionSpan.getType();
						String action = actionSpan.getType();

						if (action.equals("Document Change")) {
							action = "Working";
						} else if (action.equals("Rater Change")) {
							action = "Assessing";
						} else if (action
								.equals("Document Change: Comment Made")) {
							action = "Assessing";
						} else if (action.equals("Page Viewed in Browser")) {
							action = "Reading";
						}

						if (actionStart < sectionStart) {
							actionStart = sectionStart;
						} else if (sectionStart < actionStart) {
							sectionStart = actionStart;
						}
						if (actionEnd < actionStart) {
							distance = 0;
							if ((++currentAction) < al.size()) {
								actionSpan = (HistorySpan) al
										.get(currentAction);
								actionStart = actionSpan.getBegin().getTime();
								actionEnd = actionSpan.getEnd().getTime();
							} else {
								actionSpan = null;
								sectionSpan = null;
							}
						} else if (sectionEnd < sectionStart) {
							distance = 0;
							if ((++currentSection) < sl.size()) {
								sectionSpan = (HistorySpan) sl
										.get(currentSection);
								sectionStart = sectionSpan.getBegin().getTime();
								sectionEnd = sectionSpan.getEnd().getTime();
							} else {
								actionSpan = null;
								sectionSpan = null;
							}
						} else if (actionEnd < sectionEnd) {
							distance = actionEnd - actionStart;
							if ((++currentAction) < al.size()) {
								actionSpan = (HistorySpan) al
										.get(currentAction);
								actionStart = actionSpan.getBegin().getTime();
								actionEnd = actionSpan.getEnd().getTime();
							} else {
								actionSpan = null;
								sectionSpan = null;
							}
						} else if (actionEnd > sectionEnd) {
							distance = sectionEnd - sectionStart;
							if ((++currentSection) < sl.size()) {
								sectionSpan = (HistorySpan) sl
										.get(currentSection);
								sectionStart = sectionSpan.getBegin().getTime();
								sectionEnd = sectionSpan.getEnd().getTime();
							} else {
								actionSpan = null;
								sectionSpan = null;
							}
						} else if (actionEnd == sectionEnd) {
							distance = sectionEnd - sectionStart;
							if (((++currentSection) < sl.size())
									&& ((++currentAction) < al.size())) {
								actionSpan = (HistorySpan) al
										.get(currentAction);
								actionStart = actionSpan.getBegin().getTime();
								actionEnd = actionSpan.getEnd().getTime();
								sectionSpan = (HistorySpan) sl
										.get(currentSection);
								sectionStart = sectionSpan.getBegin().getTime();
								sectionEnd = sectionSpan.getEnd().getTime();
							} else {
								actionSpan = null;
								sectionSpan = null;
							}
						}

						HashMap actionMap = (HashMap) sectionMap.get(section);
						if (actionMap == null) {
							actionMap = new HashMap();
							sectionMap.put(section, actionMap);
						}

						Long length = (Long) actionMap.get(action);
						if (length == null) {
							length = new Long(0);
						}
						long lLength = length.longValue();
						lLength = lLength + distance;
						length = new Long(lLength);
						actionMap.put(action, length);
					}
				}
			}
		}

		retVal
				.append("Comp, Per, Q_W, Q_A, Q_R, H_W, H_A, H_R, I_W, I_A, I_R, A_W, A_A, A_R, M_W, M_A, M_R, E_W, E_A, E_R\n");

		for (Iterator periodIt = periodMap.keySet().iterator(); periodIt
				.hasNext();) {
			String period = (String) periodIt.next();
			HashMap computerMap = (HashMap) periodMap.get(period);
			for (Iterator computerIt = computerMap.keySet().iterator(); computerIt
					.hasNext();) {
				String computer = (String) computerIt.next();
				HashMap sectionMap = (HashMap) computerMap.get(computer);
				long[][] outArray = new long[6][3];
				for (Iterator sectionIt = sectionMap.keySet().iterator(); sectionIt
						.hasNext();) {
					String section = (String) sectionIt.next();
					HashMap actionMap = (HashMap) sectionMap.get(section);
					for (Iterator actionIt = actionMap.keySet().iterator(); actionIt
							.hasNext();) {
						String action = (String) actionIt.next();
						Long length = (Long) actionMap.get(action);
						int outSection = -1, outAction = -1;
						if (section.equals("Question")) {
							outSection = 0;
						} else if (section.equals("Hypothesize")) {
							outSection = 1;
						} else if (section.equals("Investigate")) {
							outSection = 2;
						} else if (section.equals("Analyze")) {
							outSection = 3;
						} else if (section.equals("Model")) {
							outSection = 4;
						} else if (section.equals("Evaluate")) {
							outSection = 5;
						}
						if (action.equals("Working")) {
							outAction = 0;
						} else if (action.equals("Assessing")) {
							outAction = 1;
						} else if (action.equals("Reading")) {
							outAction = 2;
						}
						if ((outSection >= 0) && (outAction >= 0)) {
							outArray[outSection][outAction] = length
									.longValue();
						}
					}
				}
				retVal.append("\"");
				retVal.append(computer);
				retVal.append("\", ");
				retVal.append("\"");
				retVal.append(period);
				retVal.append("\", ");
				for (int i = 0; i < 6; ++i) {
					for (int j = 0; j < 3; ++j) {
						//retVal.append("\"");
						retVal.append(outArray[i][j] / 1000);
						//retVal.append("\"");
						if ((j < 2) || (i < 5)) {
							retVal.append(", ");
						} else {
							retVal.append("\n");
						}
					}
				}
			}
		}

		return retVal.toString();
	}

}