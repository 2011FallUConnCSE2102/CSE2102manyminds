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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

public class ChunkFrame extends JFrame {

	public ChunkFrame(ChunkPanel cp) {
		setContentPane(cp);

		JMenuBar jmb = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu editMenu = new JMenu("Edit");
		JMenu findMenu = new JMenu("Find");

		editMenu.add(cp.getCommentInsertAction());
		editMenu.add(cp.getDeleteAction());
		editMenu.add(cp.getVideoInsertAction());
		editMenu.add(cp.getVideoSyncAction());
		editMenu.add(cp.getLatexAction());
		editMenu.add(cp.getCSVAction());
		editMenu.add(cp.getImageAction());

		JMenuItem closeItem = new JMenuItem(new AbstractAction("Close") {
			public void actionPerformed(ActionEvent ae) {
				dispatchEvent(new WindowEvent(ChunkFrame.this,
						WindowEvent.WINDOW_CLOSING));
			}
		});
		closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		editMenu.add(closeItem);
		jmb.add(editMenu);

		findMenu.add(cp.getShowFindPanelAction());
		findMenu.add(cp.getFindNextAction());
		findMenu.add(cp.getFindPreviousAction());
		findMenu.add(cp.getFindTextAction());
		jmb.add(findMenu);
		setJMenuBar(jmb);

		//        setTitle(cp.getChunk().getName());

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setSize(1024, 768);
		setVisible(true);
	}
}

