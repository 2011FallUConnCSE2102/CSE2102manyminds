package manyminds.history;

import java.util.Collection;
//import java.util.LinkedList;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class HistoryTableView extends JTable {

	static boolean intoChunk = false;

	private class ChunkUnionTableModel extends AbstractTableModel {

		private TableModel[] myTables;

		public ChunkUnionTableModel(Collection c) {
			Object[] chunks = c.toArray();
			myTables = new TableModel[chunks.length];
			for (int i = 0; i < chunks.length; i++) {
				HistoryChunk object = (HistoryChunk) chunks[i];
				myTables[i] = object.getTableModel(columnNames);
				myTables[i].addTableModelListener(new TableModelListener() {

					public void tableChanged(TableModelEvent e) {
						int firstRow = e.getFirstRow();
						int lastRow = e.getLastRow();
						int column = e.getColumn();
						int type = e.getType();
						int realFirstRow = 0;
						int realLastRow = 0;
						Object source = e.getSource();
						for (int j = 0; j < myTables.length; j++) {
							TableModel tm = myTables[j];
							if (tm == source) {
								realFirstRow += firstRow;
								realLastRow += lastRow;
							} else {
								realFirstRow += tm.getRowCount();
								realLastRow += tm.getRowCount();
							}
						}
						fireTableChanged(new TableModelEvent(
								ChunkUnionTableModel.this, realFirstRow,
								realLastRow, column, type));
					}

				});
			}
		}

		public int rowToModel(int rowIndex) {
			if (rowIndex < 0) {
				return -1;
			}
			for (int i = 0; i < myTables.length; i++) {
				TableModel tm = myTables[i];
				if (tm.getRowCount() <= rowIndex) {
					rowIndex -= tm.getRowCount();
				} else {
					return i;
				}
			}
			return -1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			int retVal = 0;
			for (int i = 0; i < myTables.length; i++) {
				TableModel tm = myTables[i];
				retVal += tm.getRowCount();
			}
			return retVal;
		}

		public int getRowCount(int i) {
			TableModel tm = myTables[i];
			return tm.getRowCount();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			for (int i = 0; i < myTables.length; i++) {
				TableModel tm = myTables[i];
				if (tm.getRowCount() <= rowIndex) {
					rowIndex -= tm.getRowCount();
				} else {
					return tm.getValueAt(rowIndex, columnIndex);
				}
			}
			return null;
		}

		public int rowFromModelRow(int model, int row) {
			int retVal = 0;
			for (int i = 0; i < model; i++) {
				TableModel tm = myTables[i];
				retVal += tm.getRowCount();
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
			for (int i = 0; i < myTables.length; i++) {
				TableModel tm = myTables[i];
				if (tm.getRowCount() <= rowIndex) {
					rowIndex -= tm.getRowCount();
				} else {
					return rowIndex;
				}
			}
			return -1;
		}
	}

	private String[] columnNames;

	//    private LinkedList myChunks;

	public HistoryTableView(Collection c, String[] columns) {
		super();
		setSelectionModel(new ChunkUnionSelectionModel(c));
		setSelectionMode(DefaultListSelectionModel.SINGLE_INTERVAL_SELECTION);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);
		columnNames = columns;
		//        myChunks = new LinkedList(c);
		setModel(new ChunkUnionTableModel(c));
		getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						scrollRectToVisible(getCellRect(getSelectionModel()
								.getMinSelectionIndex(), 0, true));
					}
				});
		//        getSelectionModel().addListSelectionListener(new
		// ListSelectionListener() {
		//
		//            public void valueChanged(ListSelectionEvent e) {
		//                if (!getSelectionModel().getValueIsAdjusting()) {
		//                    int i = getSelectionModel().getMaxSelectionIndex();
		//                    int t = getSelectionModel().getMinSelectionIndex();
		//                    if (i >= 0) {
		//                        int m = ((ChunkUnionTableModel)getModel()).rowToModel(i);
		//                        int r = ((ChunkUnionTableModel)getModel()).offsetRowIndex(i);
		//                        int r_m = ((ChunkUnionTableModel)getModel()).offsetRowIndex(t);
		//                        int m_m = ((ChunkUnionTableModel)getModel()).rowToModel(t);
		//                        if (m_m != m) {
		//                            r_m = 0;
		//                        }
		//                        int j = 0;
		//                        for (Iterator iter = myChunks.iterator(); iter.hasNext();) {
		//                            HistoryChunk element = (HistoryChunk) iter.next();
		//                            if (j++ == m) {
		//                                element.setSelectionInterval(r_m,r);
		//                            } else {
		//                                element.clearSelectedIndex();
		//                            }
		//                        }
		//                    } else {
		//                        for (Iterator iter = myChunks.iterator(); iter.hasNext();) {
		//                            HistoryChunk hc = (HistoryChunk) iter.next();
		//                            hc.clearSelectedIndex();
		//                        }
		//                    }
		//                }
		//            }
		//        });

		//        for (Iterator iter = myChunks.iterator(); iter.hasNext();) {
		//            HistoryChunk hc = (HistoryChunk) iter.next();
		//            hc.addListSelectionListener(new ListSelectionListener() {
		//                public void valueChanged(ListSelectionEvent e) {
		//                    HistoryChunk source = (HistoryChunk)e.getSource();
		//                    int row = source.getSelectedIndex();
		//                    int mod = myChunks.indexOf(source);
		//                    if (mod != -1) {
		//                        if (row != -1) {
		//                            int x = (((ChunkUnionTableModel)getModel()).rowFromModelRow(mod,
		// row));
		//                            int csMin = getSelectionModel().getMinSelectionIndex();
		//                            int csMax = getSelectionModel().getMaxSelectionIndex();
		//                            if ((csMin > x) || (csMax < x)) {
		//                                getSelectionModel().setSelectionInterval(x,x);
		//                            }
		//                            scrollRectToVisible(getCellRect(x,0,true));
		//                        } else {
		//                            getSelectionModel().clearSelection();
		//                        }
		//                    }
		//                }
		//            });
		//        }
	}

	public int rowToModel(int rowIndex) {
		if (getModel() instanceof ChunkUnionTableModel) {
			return ((ChunkUnionTableModel) getModel()).rowToModel(rowIndex);
		} else {
			return 1;
		}
	}

	/**
	 * @param i
	 * @return
	 */
	public int offsetRowIndex(int i) {
		if (getModel() instanceof ChunkUnionTableModel) {
			return ((ChunkUnionTableModel) getModel()).offsetRowIndex(i);
		} else {
			return i;
		}
	}

	/**
	 * 
	 * @param showingChunk
	 * @param i
	 * @return
	 */
	public int rowFromModelRow(int showingChunk, int i) {
		return ((ChunkUnionTableModel) getModel()).rowFromModelRow(
				showingChunk, i);
	}

}