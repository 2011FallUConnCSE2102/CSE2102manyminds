package manyminds.history;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

public class LoadLogFiles extends AbstractAction {

	private HistoryTableView myTable;

	private JFileChooser myChooser = new JFileChooser(System
			.getProperty("manyminds.home")
			+ System.getProperty("file.separator") + "CapturedLogFiles");

	public LoadLogFiles(HistoryTableView htv) {
		super("Import Log Files");
		myTable = htv;
		myChooser.setMultiSelectionEnabled(true);
	}

	public void actionPerformed(ActionEvent ae) {
		int i = myChooser.showOpenDialog(myTable);
		if (i == JFileChooser.APPROVE_OPTION) {
			(new Thread(new Runnable() {
				public void run() {
					File[] selectedFiles = myChooser.getSelectedFiles();
					for (int i = 0; i < selectedFiles.length; ++i) {
						try {
							/*
							 * Iterator it =
							 * HistoryFactory.loadLogFile(selectedFiles[i].toURL()).iterator();
							 * while(it.hasNext()) {
							 * ((HistoryTableModel)myTable.getModel()).addRow((HistoryItem)it.next()); }
							 */
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				}
			})).start();
		}
	}
}