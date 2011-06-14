/*
 * ChunkUnionSelectionModel.java Created on Mar 16, 2004
 * 
 * Copyright (C) 1998-2003 Regents of the University of California This file is
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

import java.util.Collection;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ChunkUnionSelectionModel extends DefaultListSelectionModel {

	private ListSelectionModel[] myModels;

	private HistoryChunk[] myChunks;

	public ChunkUnionSelectionModel(Collection c) {
		setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		Object[] chunks = c.toArray();
		myModels = new ListSelectionModel[chunks.length];
		myChunks = new HistoryChunk[chunks.length];
		for (int i = 0; i < chunks.length; i++) {
			myChunks[i] = (HistoryChunk) chunks[i];
			HistoryChunk object = (HistoryChunk) chunks[i];
			myModels[i] = object.getSelectionModel();
			myModels[i].addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					int firstRow = e.getFirstIndex();
					int lastRow = e.getLastIndex();
					int realFirstRow = 0;
					int realLastRow = 0;
					Object source = e.getSource();
					for (int j = 0; j < myModels.length; j++) {
						ListSelectionModel tm = myModels[j];
						if (tm == source) {
							realFirstRow = rowFromModelRow(j, firstRow);
							realLastRow = rowFromModelRow(j, lastRow);
						}
					}
					fireValueChanged(realFirstRow, realLastRow, e
							.getValueIsAdjusting());
				}
			});
		}
	}

	protected void fireValueChanged(int i0, int i1, boolean isAdjusting) {
		Object[] listeners = listenerList.getListenerList();
		ListSelectionEvent e = null;
		int model = rowToModel(i0);
		int model1 = rowToModel(i1);

		//we really don't want the selection to span multiple submodels
		if (model != model1) {
			i1 = rowFromModelRow(model, myChunks[model].getSize() - 1);
		}

		//cycle through all my selectionmodels and set the appropriate
		// selection
		for (int j = 0; j < myModels.length; j++) {
			ListSelectionModel tm = myModels[j];
			if (j == model) {
				tm.setSelectionInterval(offsetRowIndex(i0), offsetRowIndex(i1));
			} else {
				tm.clearSelection();
			}
		}

		//now tell my listeners that I've changed
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListSelectionListener.class) {
				if (e == null) {
					e = new ListSelectionEvent(ChunkUnionSelectionModel.this,
							i0, i1, false);
				}
				((ListSelectionListener) listeners[i + 1]).valueChanged(e);
			}
		}
	}

	//NEED!
	public int rowToModel(int rowIndex) {
		if (rowIndex < 0) {
			return -1;
		}
		for (int i = 0; i < myChunks.length; i++) {
			HistoryChunk hc = myChunks[i];
			if (hc.getSize() <= rowIndex) {
				rowIndex -= hc.getSize();
			} else {
				return i;
			}
		}
		return -1;
	}

	public int rowFromModelRow(int model, int row) {
		int retVal = 0;
		for (int i = 0; i < model; i++) {
			HistoryChunk hc = myChunks[i];
			retVal += hc.getSize();
		}
		retVal += row;
		return retVal;
	}

	/**
	 * @param i
	 * @return
	 */
	public int offsetRowIndex(int rowIndex) {
		if (rowIndex < 0) {
			return -1;
		}
		for (int i = 0; i < myChunks.length; i++) {
			HistoryChunk hc = myChunks[i];
			if (hc.getSize() <= rowIndex) {
				rowIndex -= hc.getSize();
			} else {
				return rowIndex;
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param owningChunk
	 * @param relIndex
	 * @return
	 */
	public int rowFromModelRow(HistoryChunk owningChunk, int relIndex) {
		for (int i = 0; i < myChunks.length; i++) {
			if (myChunks[i] == owningChunk) {
				return rowFromModelRow(i, relIndex);
			}
		}
		return -1;
	}
}