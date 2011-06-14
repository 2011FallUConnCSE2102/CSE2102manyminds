/*	File ArtifactTable.java
 * =============================================================================
 * 
 * A one-column table of text fields within some page in the ManyMinds Artifact.
 * 
 * Author Chris Schneider
 * Copyright © 1999 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 16 Dec 99	CSS	New today (from ArtifactScrollTextArea.java).
 *	25 Feb 00	CSS	Tossed in some code to make sure that CellFocusListener
 *							doesn't try to get the cell editor if there's no editing
 *							row or column.
 *					CSS	First cut at a CellKeyListener that tries to add a row to
 *							the table in response to the enter key.  This doesn't work,
 *							so this listener is never installed.
 * 
 * =============================================================================
 */

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
 package manyminds.util.gui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableModel;

/**
 * A scrollable text area within some page in the current ManyMinds Artifact.
 * 
 * @author	Chris Schneider
 */
public
class ArtifactTable extends JTable
	implements OrientedScrollable
{
	/**
	 * A key listener that doesn't do what it's supposed to.
	 * 
	 * @author	Chris Schneider
	 */
	class CellKeyListener extends KeyAdapter
	{
		/**
		 * handle special keys like enter being typed in table cell
		 *
		 * @param	event	describes key typed
		
		public void
		keyTyped(				KeyEvent			event)
		{
			if (event.getKeyCode() == KeyEvent.VK_ENTER) {
				((AbstractTableModel)ArtifactTable.this.getModel()).setNumRows(ArtifactTable.this.getRowCount()+1);
			}
		} // keyTyped
		 */
	} // CellKeyListener

	/**
	 * A focus listener that confirms cell editing when the cell loses the focus.
	 * 
	 * @author	Chris Schneider
	 */
	class EndEditListener extends FocusAdapter
	{
		/**
		 * confirms cell editing in response to losing focus
		 *
		 * @param	event	describes gain of focus
		 */
		public void
		focusLost(				FocusEvent		event)
		{
			if (!event.isTemporary()) {
				int				editingRow = ArtifactTable.this.getEditingRow();
				int				editingColumn = ArtifactTable.this.getEditingColumn();
				
				if	(	(editingRow >= 0)
					&&	(editingColumn >= 0)) {
					CellEditor		theCellEditor
						= ArtifactTable.this.getCellEditor(editingRow, editingColumn);
					
					if (theCellEditor != null) {
						theCellEditor.stopCellEditing();
					}
				}
			}
		} // focusLost
		
	} // CellFocusListener
	
	/**
	 * constructs a new artifact table with dataModel underlying it
	 *
	 * @param	dataModel	table model holding contents of table
	 */
	public
	ArtifactTable(TableModel dataModel)
	{
		super(dataModel);
                setRowHeight((int)(getRowHeight() * 1.3));
		setModel(dataModel);


	} // ArtifactTable

	public
	ArtifactTable()
	{
		super();
		setTableHeader(null);
                setRowHeight((int)(getRowHeight() * 1.3));
		//addKeyListener(new CellKeyListener());

	} // ArtifactTable

	public void
	setModel(TableModel dataModel) {
		super.setModel(dataModel);
		if (getColumnModel().getColumnCount() > 0) {
			//setTableHeader(null);
			DefaultCellEditor dce = new DefaultCellEditor(new JTextField());
			dce.getComponent().addFocusListener(new EndEditListener());
			//setDefaultEditor(String.class,dce);
			getColumnModel().getColumn(0).setCellEditor(dce);
			//addKeyListener(new CellKeyListener());
		}
	}	
	
	public boolean
	editCellAt(int row, int column) {
		if (super.editCellAt(row,column)) {
			editorComp.requestFocus();
			return true;
		} else {
			return false;
		}
	}

	public boolean
	editCellAt(int row, int column, EventObject e) {
		if (super.editCellAt(row,column,e)) {
			editorComp.requestFocus();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * makes this component displayable by connecting it to a native screen interface
	 *
	 */
	public void
	addNotify()
	{
		super.addNotify();
		setPreferredScrollableViewportSize(getSize());

	} // addNotify
	
	/**
	 * returns what component wants to do about horizontal scrolling
	 *
	 *	@returns	when/whether horizontal scrolling should be allowed
	 */
	public int
	getHorizontalScrollBarPolicy()
	{
		return(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
	} // getHorizontalScrollBarPolicy
	
	/**
	 * returns what component wants to do about vertical scrolling
	 *
	 *	@returns	when/whether vertical scrolling should be allowed
	 */
	public int
	getVerticalScrollBarPolicy()
	{
		return(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
	} // getVerticalScrollBarPolicy

} // ArtifactTable

