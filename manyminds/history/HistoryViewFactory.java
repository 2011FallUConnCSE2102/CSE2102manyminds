/* HistoryViewFactory.java
 * Created on Dec 22, 2003
 *  
 * Copyright (C) 1998-2003 Regents of the University of California
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
package manyminds.history;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;

/**
 * @author eric
 *  
 */
public class HistoryViewFactory {

	private List myChunks = new LinkedList();

	/**
	 *  
	 */
	public HistoryViewFactory() {
		super();
	}

	public HistoryViewFactory(Collection c) {
		this();
		myChunks.addAll(c);
	}

	public void addChunk(HistoryChunk hc) {
		myChunks.add(hc);
	}

	public JComponent getTimelineView() {
		return new HistoryTimelineView(myChunks);
	}

	public JComponent getTableView() {
		return new JTable();
	}

	public JComponent getVideoView() {
		return new JPanel();
	}

	public JComponent getCommentView() {
		return new JTextArea();
	}
}