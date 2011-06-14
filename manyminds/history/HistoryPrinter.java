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

import java.awt.Graphics;
//import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

public class HistoryPrinter {

	private Map myProjects;

	private static class TracePage implements Printable {

		private ArrayList myEpisodes = new ArrayList();

		private ListIterator episodeIterator = null;

		private int lastRequest = -1;

		public int print(Graphics g, PageFormat pf, int q) {
			if (q != lastRequest) {
				lastRequest = q;
				return PAGE_EXISTS;
			} else {
				if (q == 0) {
					episodeIterator = myEpisodes.listIterator();
				}
				if (!episodeIterator.hasNext()) {
					lastRequest = -1;
					return NO_SUCH_PAGE;
				} else {
					// TODO: fix this
					/*
					 * HistoryTimelineView htv = new HistoryTimelineView();
					 * HistoryChunk currentEpisode =
					 * (HistoryChunk)episodeIterator.next();
					 * htv.setChunk(currentEpisode); int lastLocation =
					 * (int)pf.getImageableY(); int predictedEnd = lastLocation;
					 * int maxImageable = (int)(pf.getImageableHeight() +
					 * pf.getImageableY()); Graphics2D g2d = (Graphics2D)g;
					 * htv.setSize((int)pf.getImageableWidth(),((int)pf.getImageableHeight()) /
					 * 5); while (predictedEnd < maxImageable) {
					 * g2d.translate((int)pf.getImageableX(), lastLocation);
					 * htv.paintComponent(g2d);
					 * g2d.translate((int)-pf.getImageableX(), -lastLocation);
					 * lastLocation += htv.getPredictedHeight(); if
					 * (episodeIterator.hasNext()) { currentEpisode =
					 * (HistoryChunk)episodeIterator.next();
					 * htv.setChunk(currentEpisode); predictedEnd = lastLocation +
					 * (int)htv.getPredictedHeight(); } else { return
					 * PAGE_EXISTS; } } episodeIterator.previous();
					 */
					return PAGE_EXISTS;
				}
			}
		}

		public void addEpisode(StudentEpisode se) {
			myEpisodes.add(se);
		}
	}

	public HistoryPrinter(Map projectList) {
		myProjects = projectList;
	}

	public void printTraces() {
		printTraces(new PrintSelector() {
			public boolean shouldPrint(StudentEpisode se) {
				return true;
			}
		});
	}

	public void printDetails() {
		printDetails(new PrintSelector() {
			public boolean shouldPrint(StudentEpisode se) {
				return true;
			}
		});
	}

	public void printTraces(PrintSelector ps) {
		Iterator it = myProjects.keySet().iterator();
		TracePage tp = new TracePage();
		while (it.hasNext()) {
			String key = it.next().toString();
			Object o = myProjects.get(key);
			if (o instanceof StudentProject) {
				Iterator epit = ((StudentProject) o).listEpisodes().iterator();
				while (epit.hasNext()) {
					StudentEpisode se = ((StudentProject) o).getEpisode(epit
							.next().toString());
					if (ps.shouldPrint(se)) {
						tp.addEpisode(se);
					}
				}
			}
		}
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(tp);
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void printDetails(PrintSelector ps) {
		Iterator it = myProjects.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next().toString();
		}
	}
}