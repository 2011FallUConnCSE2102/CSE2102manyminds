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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class StudentEpisode extends AbstractTableModel {

	private ArrayList myList = new ArrayList();

	private Document myNote = new PlainDocument();

	private ListSelectionModel mySelectionModel = null;

	private String myTagString = "";

	private static final int EVENT_ITERATOR = 1;

	private static final int SECTION_ITERATOR = 2;

	private static final int ACTION_ITERATOR = 3;

	private static final int VIDEO_ITERATOR = 4;

	private static final int ANNOTATION_ITERATOR = 5;

	private static final int RESOURCE_ITERATOR = 6;

	private static final int STATE_ITERATOR = 7;

	private java.beans.PropertyChangeListener myPCL = new java.beans.PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent pce) {
			if (pce.getPropertyName().equals("Start Time")) {
				resortHistoryItems();
			}
		}
	};

	private class EpisodeIterator implements Iterator {
		private int myType;

		private int currentIndex = 0;

		private HistorySpan nextSpan = null;

		public EpisodeIterator(int i) {
			myType = i;
			createNextSpan();
		}

		public boolean hasNext() {
			return (nextSpan != null);
		}

		public Object next() throws NoSuchElementException {
			if (nextSpan != null) {
				return createNextSpan();
			} else {
				throw new NoSuchElementException();
			}
		}

		public void remove() throws UnsupportedOperationException,
				IllegalStateException {
			throw new UnsupportedOperationException();
		}

		protected HistorySpan createNextSpan() {
			HistorySpan retVal = nextSpan;
			nextSpan = null;
			HistorySpan tempSpan = null;
			while ((nextSpan == null) && (currentIndex < myList.size())) {
				HistoryItem hi = (HistoryItem) myList.get(currentIndex++);
				if (((Date) hi.getProperty(HistoryItem.BEGIN_DATE)).getTime() <= 100L) {
				} else if (myType == EVENT_ITERATOR) {
					if (hi.getProperty(HistoryItem.ITEM_TYPE).equals(
							HistoryItem.ADVICE_ADD)
							|| hi.getProperty(HistoryItem.ITEM_TYPE).equals(
									HistoryItem.ADVICE_DISMISS)
							|| hi.getProperty(HistoryItem.ITEM_TYPE).equals(
									HistoryItem.ADVICE_MORE)
							|| hi.getProperty(HistoryItem.ITEM_TYPE).equals(
									HistoryItem.PAGE_FLIP)
							|| hi.getProperty(HistoryItem.ITEM_TYPE).equals(
									HistoryItem.RATER_CHANGE)) {
						nextSpan = new HistorySpan(hi);
					}
				} else if (myType == STATE_ITERATOR) {
					if (hi.getProperty(HistoryItem.ITEM_TYPE).equals(
							HistoryItem.STATE)) {
						nextSpan = new HistorySpan(hi.getProperty(
								HistoryItem.CHANGE_FROM).toString(), (Date) hi
								.getProperty(HistoryItem.BEGIN_DATE), (Date) hi
								.getProperty(HistoryItem.END_DATE));
					}
				} else if (myType == SECTION_ITERATOR) {
					if (!hi.getProperty(HistoryItem.CHANGE_SECTION).toString()
							.trim().equals("")) {
						if (tempSpan == null) {
							tempSpan = new HistorySpan(
									hi.getProperty(HistoryItem.CHANGE_SECTION)
											.toString(),
									(Date) hi
											.getProperty(HistoryItem.BEGIN_DATE),
									(Date) hi.getProperty(HistoryItem.END_DATE));
						} else if (tempSpan.getType().equals(
								hi.getProperty(HistoryItem.CHANGE_SECTION))) {
							tempSpan
									.absorb(
											(Date) hi
													.getProperty(HistoryItem.BEGIN_DATE),
											(Date) hi
													.getProperty(HistoryItem.END_DATE));
						} else {
							tempSpan
									.absorb(
											(Date) hi
													.getProperty(HistoryItem.BEGIN_DATE),
											(Date) hi
													.getProperty(HistoryItem.END_DATE));
							nextSpan = tempSpan;
							--currentIndex;
						}
					}
				} else if (myType == ACTION_ITERATOR) {
					if (hi.getProperty(HistoryItem.ITEM_TYPE).toString()
							.startsWith(HistoryItem.DOCUMENT_CHANGE)
							|| hi.getProperty(HistoryItem.ITEM_TYPE).equals(
									HistoryItem.RATER_CHANGE)
							|| hi.getProperty(HistoryItem.ITEM_TYPE).equals(
									HistoryItem.ADVICE_VIEW)) {
						if (tempSpan == null) {
							tempSpan = new HistorySpan(
									hi.getProperty(HistoryItem.ITEM_TYPE)
											.toString(),
									(Date) hi
											.getProperty(HistoryItem.BEGIN_DATE),
									(Date) hi.getProperty(HistoryItem.END_DATE));
						} else if (tempSpan.getType().equals(
								hi.getProperty(HistoryItem.ITEM_TYPE))) {
							tempSpan
									.absorb(
											(Date) hi
													.getProperty(HistoryItem.BEGIN_DATE),
											(Date) hi
													.getProperty(HistoryItem.END_DATE));
						} else {
							tempSpan
									.absorb(
											(Date) hi
													.getProperty(HistoryItem.BEGIN_DATE),
											(Date) hi
													.getProperty(HistoryItem.END_DATE));
							nextSpan = tempSpan;
							--currentIndex;
						}
					}
				} else if (myType == VIDEO_ITERATOR) {
					if (hi.getProperty(HistoryItem.ITEM_TYPE).equals(
							HistoryItem.VIDEO_RESOURCE)) {
						nextSpan = new HistorySpan(hi);
					}
				} else if (myType == ANNOTATION_ITERATOR) {
					if (hi.getProperty(HistoryItem.ITEM_TYPE).equals(
							HistoryItem.ANNOTATION)) {
						nextSpan = new HistorySpan(hi);
					}
				} else if (myType == RESOURCE_ITERATOR) {
					if (hi.getProperty(HistoryItem.ITEM_TYPE).equals(
							HistoryItem.OTHER_RESOURCE)) {
						nextSpan = new HistorySpan(hi);
					}
				}
			}
			if ((currentIndex == myList.size()) && (nextSpan == null)
					&& (tempSpan != null)) {
				nextSpan = tempSpan;
			}
			return retVal;
		}
	}

	public StudentEpisode() {
	}

	public int getRowCount() {
		return myList.size();
	}

	public int getColumnCount() {
		return 8;
	}

	public Document getNote() {
		return myNote;
	}

	public String getTagString() {
		return myTagString;
	}

	public void setTagString(String s) {
		myTagString = s;
	}

	public void setNote(String s) {
		try {
			myNote.remove(0, myNote.getLength());
			myNote.insertString(0, s, null);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public String getColumnName(int column) {
		if (column == 0) {
			return "Start Time";
		} else if (column == 1) {
			return "End Time";
		} else if (column == 2) {
			return "Change Type";
		} else if (column == 3) {
			return "What Changed";
		} else if (column == 4) {
			return "Change From";
		} else if (column == 5) {
			return "Change To";
		} else if (column == 6) {
			return "Current Section";
		} else if (column == 7) {
			return "Annotation";
		} else {
			return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public void setValueAt(Object o, int row, int column) {
		/*
		 * if (row < myList.size()) { HistoryItem hi =
		 * (HistoryItem)myList.get(row); if (column == 0) {
		 * hi.setStartTime(o.toString()); resortHistoryItems(); } else if
		 * (column == 1) { hi.setEndTime(o.toString()); } else if (column == 2) {
		 * hi.setType(o.toString()); } else if (column == 3) {
		 * hi.setWhatChanged(o.toString()); } else if (column == 4) {
		 * hi.setFrom(o.toString()); } else if (column == 5) {
		 * hi.setTo(o.toString()); } else if (column == 6) {
		 * hi.setSection(o.toString()); } else if (column == 7) {
		 * hi.setAnnotation(o.toString()); } if (column != 0) {
		 * fireTableCellUpdated(row, column); } }
		 */
	}

	public HistoryItem removeRow(int i) {
		HistoryItem retVal = (HistoryItem) myList.remove(i);
		fireTableRowsDeleted(i, i);
		return retVal;
	}

	public void addHistoryItem(HistoryItem o) {
		int pos = Collections.binarySearch(myList, o);
		if (pos < 0) {
			pos = -pos - 1;
		}
		insertHistoryItem(o, pos);
	}

	public void unsortAddHistoryItem(HistoryItem o) {
		insertHistoryItem(o, myList.size());
	}

	public void resortHistoryItems() {
		Collections.sort(myList);
		fireTableDataChanged();
	}

	protected void insertHistoryItem(HistoryItem o, int i) {
		o.addPropertyChangeListener(myPCL);
		myList.add(i, o);
		fireTableRowsInserted(i, i);
	}

	public Class getColumnClass(int column) {
		if ((column >= 0) && (column <= 7)) {
			return String.class;
		} else {
			return null;
		}
	}

	public HistoryItem getHistoryItemAt(int i) {
		return (HistoryItem) myList.get(i);
	}

	public Iterator iterator() {
		return Collections.unmodifiableList(myList).iterator();
	}

	public Iterator eventIterator() {
		return new EpisodeIterator(EVENT_ITERATOR);
	}

	public Iterator actionIterator() {
		return new EpisodeIterator(ACTION_ITERATOR);
	}

	public Iterator sectionIterator() {
		return new EpisodeIterator(SECTION_ITERATOR);
	}

	public Iterator videoIterator() {
		return new EpisodeIterator(VIDEO_ITERATOR);
	}

	public Iterator resourceIterator() {
		return new EpisodeIterator(RESOURCE_ITERATOR);
	}

	public Iterator stateIterator() {
		return new EpisodeIterator(STATE_ITERATOR);
	}

	public Iterator annotationIterator() {
		return new EpisodeIterator(ANNOTATION_ITERATOR);
	}

	public Object getValueAt(int row, int column) {
		if (row < myList.size()) {
			HistoryItem hi = (HistoryItem) myList.get(row);
			if (column == 0) {
				return HistoryItem.SHORT_DATE_FORMAT.format(hi
						.getProperty(HistoryItem.BEGIN_DATE));
			} else if (column == 1) {
				return HistoryItem.SHORT_DATE_FORMAT.format(hi
						.getProperty(HistoryItem.END_DATE));
			} else if (column == 2) {
				return hi.getProperty(HistoryItem.ITEM_TYPE);
			} else if (column == 3) {
				return hi.getProperty(HistoryItem.WHAT_HAPPENED);
			} else if (column == 4) {
				return hi.getProperty(HistoryItem.CHANGE_FROM);
			} else if (column == 5) {
				return hi.getProperty(HistoryItem.CHANGE_TO);
			} else if (column == 6) {
				return hi.getProperty(HistoryItem.CHANGE_SECTION);
			} else if (column == 7) {
				return hi.getProperty(HistoryItem.ITEM_ANNOTATION);
			} else {
				return null;
			}
		}
		return null;
	}

	public void selectDate(Date d) {
		HistoryItem o = new HistoryItem();
		o.setProperty(HistoryItem.BEGIN_DATE, d);
		int pos = Collections.binarySearch(myList, o);
		if (pos < 0) {
			pos = -pos - 1;
		}
		if (pos == myList.size()) {
			--pos;
		}
		mySelectionModel.setSelectionInterval(pos, pos);
	}

	public Date getFirstTimestamp() {
		if (myList.isEmpty()) {
			return new Date(1000);
		} else {
			Iterator it = myList.iterator();
			while (it.hasNext()) {
				HistoryItem hi = (HistoryItem) it.next();
				if (((Date) hi.getProperty(HistoryItem.BEGIN_DATE)).getTime() > 100) {
					return (Date) hi.getProperty(HistoryItem.BEGIN_DATE);
				}
			}
			return new Date(1000);
		}
	}

	public Date getLastTimestamp() {
		Date retVal = new Date(10000);
		Iterator it = myList.iterator();
		while (it.hasNext()) {
			Date d = (Date) ((HistoryItem) it.next())
					.getProperty(HistoryItem.END_DATE);
			if (retVal.compareTo(d) < 0) {
				retVal = d;
			}
		}
		return retVal;
	}

	public void setSelectionModel(ListSelectionModel lsm) {
		mySelectionModel = lsm;
	}

	public void absorb(StudentEpisode se) {
		myList.addAll(se.myList);
		resortHistoryItems();
	}

	public Date getSelectionStartTime() {
		if ((mySelectionModel != null)
				&& (mySelectionModel.getMinSelectionIndex() >= 0)) {
			HistorySpan selectionSpan = null;
			for (int i = mySelectionModel.getMinSelectionIndex(); i <= mySelectionModel
					.getMaxSelectionIndex(); ++i) {
				if (selectionSpan == null) {
					selectionSpan = new HistorySpan((HistoryItem) myList.get(i));
				} else {
					selectionSpan.absorb((Date) ((HistoryItem) myList.get(i))
							.getProperty(HistoryItem.BEGIN_DATE),
							(Date) ((HistoryItem) myList.get(i))
									.getProperty(HistoryItem.END_DATE));
				}
			}
			return selectionSpan.getBegin();
		} else {
			return new Date(0);
		}
	}

	public Date getSelectionEndTime() {
		if ((mySelectionModel != null)
				&& (mySelectionModel.getMaxSelectionIndex() >= 0)) {
			HistorySpan selectionSpan = null;
			for (int i = mySelectionModel.getMinSelectionIndex(); i <= mySelectionModel
					.getMaxSelectionIndex(); ++i) {
				if (selectionSpan == null) {
					selectionSpan = new HistorySpan((HistoryItem) myList.get(i));
				} else {
					selectionSpan.absorb((Date) ((HistoryItem) myList.get(i))
							.getProperty(HistoryItem.BEGIN_DATE),
							(Date) ((HistoryItem) myList.get(i))
									.getProperty(HistoryItem.END_DATE));
				}
			}
			return selectionSpan.getEnd();
		} else {
			return new Date(0);
		}
	}

	public String toLatex() {
		StringBuffer retVal = new StringBuffer();
		retVal
				.append("\\scriptsize\n\\begin{landscape}\n\\begin{longtable}{|p{0.2in}|p{0.5in}|p{0.5in}|p{1.3in}|p{1.2in}|p{2.0in}|p{2.0in}|}\n");
		Iterator it = myList.iterator();
		int lineNumber = 1;
		while (it.hasNext()) {
			retVal.append("\\hline ");
			retVal.append(lineNumber++);
			retVal.append(" & ");
			retVal.append(((HistoryItem) it.next()).toLatex());
			retVal.append("\n");
		}
		retVal
				.append("\\hline\n\\end{longtable}\n\\end{landscape}\n\\normalsize\n");
		return retVal.toString();
	}

	public String toCSV() {
		StringBuffer retVal = new StringBuffer();
		Iterator it = myList.iterator();
		while (it.hasNext()) {
			retVal.append(((HistoryItem) it.next()).toCSV());
		}
		return retVal.toString();
	}

}