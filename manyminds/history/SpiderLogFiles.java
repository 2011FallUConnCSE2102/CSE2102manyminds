package manyminds.history;

import java.awt.Component;
import java.awt.event.ActionEvent;
//import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

public class SpiderLogFiles extends AbstractAction {

	private Component myComp;

	private JFileChooser myChooser = new JFileChooser();

	//    private File baseRoot;

	public SpiderLogFiles(Component c) {
		super("Spider Log Files");
		myComp = c;
		myChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	/*
	 * private void spiderFolder(File root) { if (root.isDirectory()) {
	 * LinkedList toLoad = new LinkedList(); File[] files = root.listFiles();
	 * for (int i = 0; i < files.length; ++i) { if (files[i].isDirectory()) {
	 * spiderFolder(files[i]); } else if (files[i].getName().endsWith(".xml")) {
	 * toLoad.add(files[i]); } } Iterator it = toLoad.iterator();
	 * HistoryTableModel htm = new HistoryTableModel(); while (it.hasNext()) {
	 * try { Iterator it2 =
	 * HistoryFactory.loadLogFile(((File)it.next()).toURL()).iterator(); while
	 * (it2.hasNext()) { htm.addRow((HistoryItem)it2.next()); } } catch
	 * (Throwable t) { t.printStackTrace(); } } if (htm.getRowCount() > 0) { try {
	 * String s = root.getParentFile().getName()+"."+root.getName()+".csv";
	 * FileWriter fw = new FileWriter(new File(baseRoot,s));
	 * fw.write(htm.toCSV()); fw.close(); } catch (Throwable t) {
	 * t.printStackTrace(); } } } }
	 */

	public void actionPerformed(ActionEvent ae) {
		int i = myChooser.showOpenDialog(myComp);
		if (i == JFileChooser.APPROVE_OPTION) {
			(new Thread(new Runnable() {
				public void run() {
					//baseRoot = myChooser.getSelectedFile();
					//  spiderFolder(baseRoot);
					java.awt.Toolkit.getDefaultToolkit().beep();
				}
			})).start();
		}
	}
}