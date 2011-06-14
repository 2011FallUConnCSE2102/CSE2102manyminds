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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import manyminds.util.CollectionListModel;

public class HistoryChunk {

	private class ChunkTableModel extends AbstractTableModel implements
			ListDataListener {

		private String[] myColumns;

		public ChunkTableModel(String[] cols) {
			myColumns = cols;
			myKeyList.addListDataListener(this);
		}

		public void contentsChanged(ListDataEvent lde) {
			fireTableDataChanged();
		}

		public int getColumnCount() {
			return myColumns.length;
		}

		public String getColumnName(int i) {
			return myColumns[i];
		}

		public int getRowCount() {
			return myData.size();
		}

		public Object getValueAt(int row, int column) {
			Object key = myKeyList.get(row);
			HistoryItem hi = (HistoryItem) myData.get(key);
			Object val = hi.getProperty(myColumns[column]);
			if (val == null) {
				val = "";
			}
			return HistoryItem.formatObject(val);
		}

		public void intervalAdded(ListDataEvent lde) {
			fireTableRowsInserted(lde.getIndex0(), lde.getIndex1());
		}

		public void intervalRemoved(ListDataEvent lde) {
			fireTableRowsDeleted(lde.getIndex0(), lde.getIndex1());
		}

		public boolean isCellEditable(int row, int column) {
			return true;
		}

		public void setValueAt(Object val, int row, int column) {
			Object key = myKeyList.get(row);
			HistoryItem hi = (HistoryItem) myData.get(key);
			hi.setProperty(myColumns[column], val);
		}
	}

	private class HistoryIterator implements Iterator {
		private int currentIndex = 0;

		private int myType;

		private HistoryItem nextItem = null;

		private HistorySpan nextSpan = null;

		public HistoryIterator(int i) {
			myType = i;
			createNextSpan();
		}

		protected HistorySpan createNextSpan() {
			HistorySpan retVal = nextSpan;
			nextSpan = null;
			HistorySpan tempSpan = null;
			while ((nextSpan == null) && (currentIndex < myKeyList.size())) {
				HistoryItem hi = (HistoryItem) myData.get(myKeyList
						.get(currentIndex++));
				if (myType == ALLTYPE_ITERATOR) {
					if (iteratedTypes.contains(hi
							.getProperty(HistoryItem.ITEM_TYPE))) {
						nextSpan = new HistorySpan(hi);
					}
				} else {
					if ((!iteratedTypes.contains(hi
							.getProperty(HistoryItem.ITEM_TYPE)))
							|| (((Date) hi.getProperty(HistoryItem.BEGIN_DATE))
									.getTime() <= 100L)) {
					} else if (myType == EVENT_ITERATOR) {
						//if
						// (hi.getProperty(HistoryItem.ITEM_TYPE).equals(HistoryItem.ADVICE_ADD)
						// ||
						// hi.getProperty(HistoryItem.ITEM_TYPE).equals(HistoryItem.ADVICE_DISMISS)
						// ||
						// hi.getProperty(HistoryItem.ITEM_TYPE).equals(HistoryItem.ADVICE_MORE)
						// ||
						// hi.getProperty(HistoryItem.ITEM_TYPE).equals(HistoryItem.PAGE_FLIP)
						// ||
						// hi.getProperty(HistoryItem.ITEM_TYPE).equals(HistoryItem.RATER_CHANGE))
						// {
						if (hi.getProperty(HistoryItem.ITEM_TYPE).equals(
								HistoryItem.ADVICE_DISMISS)
								|| hi.getProperty(HistoryItem.ITEM_TYPE)
										.equals(HistoryItem.ADVICE_MORE)
								|| hi.getProperty(HistoryItem.ITEM_TYPE)
										.equals(HistoryItem.RATER_CHANGE)) {
							nextSpan = new HistorySpan(hi);
						}
					} else if (myType == STATE_ITERATOR) {
						if (hi.getProperty(HistoryItem.ITEM_TYPE).equals(
								HistoryItem.STATE)) {
							nextSpan = new HistorySpan(
									hi.getProperty(HistoryItem.CHANGE_FROM)
											.toString(),
									(Date) hi
											.getProperty(HistoryItem.BEGIN_DATE),
									(Date) hi.getProperty(HistoryItem.END_DATE));
						}
					} else if (myType == SECTION_ITERATOR) {
						if (!hi.getProperty(HistoryItem.CHANGE_SECTION)
								.toString().trim().equals("")) {
							if (tempSpan == null) {
								tempSpan = new HistorySpan(
										hi.getProperty(
												HistoryItem.CHANGE_SECTION)
												.toString(),
										(Date) hi
												.getProperty(HistoryItem.BEGIN_DATE),
										(Date) hi
												.getProperty(HistoryItem.END_DATE));
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
						String type = hi.getProperty(HistoryItem.ITEM_TYPE)
								.toString();
						String actionType = (String) actionTypeMap.get(type);
						if (actionType != null) {
							if (tempSpan == null) {
								tempSpan = new HistorySpan(
										actionType,
										(Date) hi
												.getProperty(HistoryItem.BEGIN_DATE),
										(Date) hi
												.getProperty(HistoryItem.END_DATE));
							} else if (tempSpan.getType().equals(actionType)) {
								tempSpan
										.absorb(
												(Date) hi
														.getProperty(HistoryItem.BEGIN_DATE),
												(Date) hi
														.getProperty(HistoryItem.END_DATE));
							} else {
								//tempSpan.absorb((Date)hi.getProperty(HistoryItem.BEGIN_DATE),(Date)hi.getProperty(HistoryItem.END_DATE));
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
			}
			if ((currentIndex == myKeyList.size()) && (nextSpan == null)
					&& (tempSpan != null)) {
				nextSpan = tempSpan;
			}
			return retVal;
		}

		public boolean hasNext() {
			return (((myType == ALLTYPE_ITERATOR) && (nextItem != null)) || ((myType != ALLTYPE_ITERATOR) && (nextSpan != null)));
		}

		public Object next() throws NoSuchElementException {
			if (myType == ALLTYPE_ITERATOR) {
				HistoryItem hi = nextItem;
				createNextSpan();
				return hi;
			} else {
				if (nextSpan != null) {
					return createNextSpan();
				} else {
					throw new NoSuchElementException();
				}
			}
		}

		public void remove() throws UnsupportedOperationException,
				IllegalStateException {
			throw new UnsupportedOperationException();
		}
	}

	private static final int ACTION_ITERATOR = 3;

	private static final int ALLTYPE_ITERATOR = 8;

	private static final int ANNOTATION_ITERATOR = 5;

	public final static int CHECKED_IN = 2;

	public final static int CHECKED_OUT = 1;

	private static final int EVENT_ITERATOR = 1;

	private final static String[] iteratedTypeNames = {
			HistoryItem.COMMENT_CHANGE, HistoryItem.DOCUMENT_CHANGE,
			HistoryItem.PAGE_COUNT_CHANGE, HistoryItem.PAGE_FLIP,
			HistoryItem.STATE, HistoryItem.ADVICE_ADD,
			HistoryItem.ADVICE_DISMISS, HistoryItem.ADVICE_MORE,
			HistoryItem.ADVICE_VIEW, HistoryItem.RATER_CHANGE,
			HistoryItem.VIDEO_RESOURCE, HistoryItem.OTHER_RESOURCE,
			HistoryItem.ANNOTATION };

	private final static HashSet iteratedTypes = new HashSet(Arrays
			.asList(iteratedTypeNames));

	public final static int NOT_CHECKED_OUT = 3;

	private static final int RESOURCE_ITERATOR = 6;

	private static final int SECTION_ITERATOR = 2;

	private static final int STATE_ITERATOR = 7;

	private static final int VIDEO_ITERATOR = 4;

	private int checkoutStatus = NOT_CHECKED_OUT;

	private HistoryItem myComment = null;

	private HashMap myData = new HashMap();

	private HistoryDatabase myDatabase = null;

	private CollectionListModel myKeyList = new CollectionListModel();

	private String myName = "name-bo";

	private static HashMap actionTypeMap = new HashMap();
	static {
		actionTypeMap.put(HistoryItem.ADVICE_VIEW, "Reading");
		actionTypeMap.put(HistoryItem.DOCUMENT_CHANGE, "Working");
		actionTypeMap.put(HistoryItem.COMMENT_CHANGE, "Assessing");
		actionTypeMap.put(HistoryItem.PAGE_FLIP, "Working");
		actionTypeMap.put(HistoryItem.RATER_CHANGE, "Assessing");
		actionTypeMap.put(HistoryItem.ADVICE_MORE, "Reading");
	}

	private PropertyChangeListener myPCL = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent pce) {
			Object source = pce.getSource();
			Object key = ((HistoryItem) source).getProperty(HistoryItem.KEY);
			int i = myKeyList.indexOf(key);
			if (i != -1) {
				myKeyList.fireContentsChanged(i, i);
			} else {
				((HistoryItem) source).removePropertyChangeListener(this);
			}
		}
	};

	private DefaultListSelectionModel mySelectionModel = new DefaultListSelectionModel() {
		protected void fireValueChanged(int i0, int i1, boolean isAdjusting) {
			Object[] listeners = listenerList.getListenerList();
			ListSelectionEvent e = null;

			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ListSelectionListener.class) {
					if (e == null) {
						e = new ListSelectionEvent(HistoryChunk.this, i0, i1,
								isAdjusting);
					}
					((ListSelectionListener) listeners[i + 1]).valueChanged(e);
				}
			}
		}
	};

	private LinkedList removedItems = new LinkedList();

	public HistoryChunk(HistoryDatabase db) {
		myDatabase = db;
	}

	public Iterator actionIterator() {
		return new HistoryIterator(ACTION_ITERATOR);
	}

	public synchronized void addHistoryItem(HistoryItem o) {
		Long key = myDatabase.getNewKey();
		myData.put(key, o);
		o.setProperty(HistoryItem.KEY, key);
		o.setDirty(true);
		o.addPropertyChangeListener(myPCL);
		if (HistoryItem.CHUNK_COMMENT.equals(o
				.getProperty(HistoryItem.ITEM_TYPE))) {
			myComment = o;
		}
		resortHistoryItems();
	}

	public void addItem(Long key) {
		myData.put(key, key);
	}

	public Iterator annotationIterator() {
		return new HistoryIterator(ANNOTATION_ITERATOR);
	}

	public synchronized void checkIn() {
		Iterator it = myData.keySet().iterator();
		while (it.hasNext()) {
			Long key = (Long) it.next();
			Object o = myData.get(key);
			if (o instanceof HistoryItem) {
				HistoryItem hi = (HistoryItem) o;
				if (hi.isDirty()) {
					myDatabase.checkinItem(hi);
					hi.setDirty(false);
				}
			}
		}
		it = removedItems.iterator();
		while (it.hasNext()) {
			myDatabase.removeItem((HistoryItem) it.next());
		}
		removedItems.clear();
		//        myDatabase.initiateSave();
		checkoutStatus = CHECKED_IN;
	}

	public synchronized void checkOut() {
		Iterator it = myData.keySet().iterator();
		while (it.hasNext()) {
			Long key = (Long) it.next();
			Object o = myData.get(key);
			if (o instanceof Long) {
				HistoryItem hi = myDatabase.checkoutItem(key);
				hi.addPropertyChangeListener(myPCL);
				myData.put(key, hi);
				hi.setDirty(false);
				if (hi.getProperty(HistoryItem.ITEM_TYPE).equals(
						HistoryItem.CHUNK_COMMENT)) {
					myComment = hi;
				}
			} else {
				HistoryItem oldValue = (HistoryItem) o;
				HistoryItem newValue = myDatabase.checkoutItem(key);
				oldValue.resolveUpdate(newValue);
			}
		}
		checkoutStatus = CHECKED_OUT;
		resortHistoryItems();
	}

	public synchronized void clearSelectedIndex() {
		if (mySelectionModel.getLeadSelectionIndex() != -1) {
			mySelectionModel.clearSelection();
		}
	}

	public Iterator eventIterator() {
		return new HistoryIterator(EVENT_ITERATOR);
	}

	public HistoryItem getByIndex(int i) {
		return (HistoryItem) myData.get(myKeyList.get(i));
	}

	public int getSize() {
		return myKeyList.getSize();
	}

	public int getCheckoutStatus() {
		return checkoutStatus;
	}

	public HistoryItem getComment() {
		return myComment;
	}

	public Date getFirstTimestamp() {
		Date retVal = null;
		Iterator it = myData.values().iterator();
		while (it.hasNext()) {
			Date d = (Date) ((HistoryItem) it.next())
					.getProperty(HistoryItem.BEGIN_DATE);
			if ((retVal == null) || (d.compareTo(retVal) < 0)) {
				retVal = d;
			}
		}
		return retVal;
	}

	public Date getLastTimestamp() {
		Date retVal = new Date(0);
		Iterator it = myData.values().iterator();
		while (it.hasNext()) {
			Date d = (Date) ((HistoryItem) it.next())
					.getProperty(HistoryItem.END_DATE);
			if (retVal.compareTo(d) < 0) {
				retVal = d;
			}
		}
		return retVal;
	}

	public String getName() {
		return myName;
	}

	public synchronized Long getNewKey() {
		long keylong = (long) (Math.random() * Long.MAX_VALUE);
		Long retVal = new Long(keylong);
		while (myData.containsKey(retVal)) {
			keylong = (long) (Math.random() * Long.MAX_VALUE);
			retVal = new Long(keylong);
		}
		return retVal;
	}

	public HistoryItem getSelected() {
		return getByIndex(getSelectedIndex());
	}

	public synchronized int getSelectedIndex() {
		return mySelectionModel.getLeadSelectionIndex();
	}

	/**
	 * @return
	 */
	public ListSelectionModel getSelectionModel() {
		return mySelectionModel;
	}

	public TableModel getTableModel(String[] columns) {
		return new ChunkTableModel(columns);
	}

	public int indexOfProperty(String property, Comparable value,
			boolean bestFit) {
		Iterator it = myKeyList.iterator();
		int i = 0;
		while (it.hasNext()) {
			HistoryItem hi = (HistoryItem) myData.get(it.next());
			if (value.compareTo((Comparable) hi.getProperty(property)) <= 0) {
				return i;
			} else {
				++i;
			}
		}
		return i - 1;
	}

	public Iterator iterator() {
		return new HistoryIterator(ALLTYPE_ITERATOR);
	}

	public void removeItem(HistoryItem hi) {
		Object key = hi.getProperty(HistoryItem.KEY);
		Object o = myData.remove(key);
		if (o != null) {
			removedItems.add(o);
			myKeyList.remove(key);
			if (myComment == o) {
				myComment = null;
			}
		}
	}

	public void removeItem(int i) {
		Long key = (Long) myKeyList.get(i);
		HistoryItem hi = (HistoryItem) myData.get(key);
		removeItem(hi);
	}

	public void resortHistoryItems() {
		myKeyList.clear();
		myKeyList.addAll(myData.keySet());
		Collections.sort(myKeyList, new Comparator() {

			public int compare(Object a, Object b) {
				HistoryItem left = (HistoryItem) myData.get(a);
				HistoryItem right = (HistoryItem) myData.get(b);
				int i = ((Date) ((HistoryItem) left)
						.getProperty(HistoryItem.BEGIN_DATE))
						.compareTo((Date) ((HistoryItem) right)
								.getProperty(HistoryItem.BEGIN_DATE));
				if (i != 0) {
					return i;
				} else {
					return ((Date) ((HistoryItem) left)
							.getProperty(HistoryItem.END_DATE))
							.compareTo((Date) ((HistoryItem) right)
									.getProperty(HistoryItem.END_DATE));
				}
			}

			public boolean equals(Object o) {
				return false;
			}
		});
	}

	public Iterator resourceIterator() {
		return new HistoryIterator(RESOURCE_ITERATOR);
	}

	public Iterator sectionIterator() {
		return new HistoryIterator(SECTION_ITERATOR);
	}

	public void setName(String s) {
		myName = s;
	}

	/*
	 * public synchronized void setSelectedIndex(int i) { if
	 * (mySelectionModel.getLeadSelectionIndex() != i) {
	 * mySelectionModel.setSelectionInterval(i,i); } }
	 * 
	 * public synchronized void setSelectedItem(HistoryItem hi) { int i =
	 * myKeyList.indexOf(hi.getProperty(HistoryItem.KEY)); if (i >=0) {
	 * setSelectedIndex(i); } }
	 * 
	 * public void setSelectionInterval(int r_m, int r) {
	 * mySelectionModel.setSelectionInterval(r_m,r); }
	 */

	public Iterator stateIterator() {
		return new HistoryIterator(STATE_ITERATOR);
	}

	public String toCSV() {
		StringBuffer retVal = new StringBuffer();
		Iterator it = myData.values().iterator();
		while (it.hasNext()) {
			retVal.append(((HistoryItem) it.next()).toCSV());
		}
		return retVal.toString();
	}

	public String toLatex() {
		StringBuffer retVal = new StringBuffer();
		retVal.append("\n" + "{\\tsp \n" + "\n"
				+ "\\begin{longtable}{|p{0.2in}|p{1in}|p{0.3in}|p{3.8in}|}\n"
				+ "\\hline\n\\multicolumn{4}{|l|}{\\emph{");
		retVal.append(getName());
		retVal
				.append("}} \\\\ \n"
						+ "\\hline \\emph{\\#} & \\emph{What Changed} & \\emph{Secs} & \\emph{New Value} \\\\\n");

		int lineNumber = 1;
		String currentWS = "";
		int currentDuration = 0;
		String finalValue = "";
		for (Iterator it = myKeyList.iterator(); it.hasNext();) {
			Object key = (Object) it.next();
			HistoryItem hi = (HistoryItem) myData.get(key);
			if (hi.getSourceWS().length() > 0) {
				if ((hi.getSourceWS().equals(currentWS))
						&& (!"Page".equals(currentWS))) {
					currentDuration += hi.getDuration();
					finalValue = hi.getProperty(HistoryItem.CHANGE_TO)
							.toString();
				} else {
					retVal.append("\\hline ");
					retVal.append(lineNumber++);
					retVal.append(" & ");
					retVal.append(currentWS);
					retVal.append(" & ");
					retVal.append(currentDuration);
					retVal.append(" & ");
					retVal.append(HistoryItem.escapeForLatex(finalValue));
					retVal.append(" \\\\\n");
					currentDuration = 0;
					currentWS = hi.getSourceWS();
					finalValue = hi.getProperty(HistoryItem.CHANGE_TO)
							.toString();
				}
			}
		}
		retVal.append("\\hline ");
		retVal.append(lineNumber++);
		retVal.append(" & ");
		retVal.append(currentWS);
		retVal.append(" & ");
		retVal.append(currentDuration);
		retVal.append(" & ");
		retVal.append(finalValue);
		retVal.append(" \\\\\n");
		retVal.append("\\hline\n\\end{longtable}}\n");
		return retVal.toString();

	}

	public Iterator videoIterator() {
		return new HistoryIterator(VIDEO_ITERATOR);
	}

	/**
	 * 
	 * @param videoItem
	 * @return
	 */
	public int indexOf(HistoryItem hi) {
		Object o = hi.getProperty(HistoryItem.KEY);
		int i = myKeyList.indexOf(o);
		return i;
	}
}