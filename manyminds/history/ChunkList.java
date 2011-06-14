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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ChunkList extends JPanel {

	private int checkedOut;

	private synchronized void incrementCheckoutCount() {
		++checkedOut;
	}

	private synchronized void decrementCheckoutCount() {
		--checkedOut;
	}

	private void checkinAll() {
		for (int i = 0; i < getComponentCount(); i++) {
			Component array_element = getComponent(i);
			if (array_element instanceof CheckoutPanel) {
				((CheckoutPanel) array_element).checkIn();
			}
		}
	}

	private void checkoutAll(boolean b) {
		LinkedList ll = new LinkedList();
		for (int i = getComponentCount() - 1; i >= 0; --i) {
			Component array_element = getComponent(i);
			if (array_element instanceof CheckoutPanel) {
				HistoryChunk hc = ((CheckoutPanel) array_element).checkOut();
				if (b) {
					ll.addFirst(hc);
				} else {
					ll.add(hc);
					new ChunkFrame(new ChunkPanel(ll));
					ll.clear();
				}
			}
		}
		if (b) {
			new ChunkFrame(new ChunkPanel(ll));
		}
	}

	//    private synchronized boolean
	//    allCheckedIn() {
	//        return (checkedOut == 0);
	//    }

	private class CheckoutPanel extends JPanel {
		private HistoryChunk myChunk;

		private boolean isCheckedOut = false;

		private JLabel status = new JLabel("Not Checked Out");

		public void checkIn() {
			if (isCheckedOut) {
				myChunk.checkIn();
				status.setText("Checked In");
				decrementCheckoutCount();
				isCheckedOut = false;
			}
		}

		public HistoryChunk checkOut() {
			if (!isCheckedOut) {
				myChunk.checkOut();
				isCheckedOut = true;
				incrementCheckoutCount();
			}
			status.setText("Checked Out");
			return myChunk;
		}

		public CheckoutPanel(HistoryChunk c) {
			setLayout(new FlowLayout());
			myChunk = c;
			JButton checkinButton = new JButton("Check In");
			JButton checkoutButton = new JButton("Check Out");
			JLabel title = new JLabel(myChunk.getName());

			if (myChunk.getCheckoutStatus() == HistoryChunk.CHECKED_OUT) {
				status.setText("Checked Out");
				isCheckedOut = true;
			} else if (myChunk.getCheckoutStatus() == HistoryChunk.CHECKED_IN) {
				status.setText("Checked In");
				isCheckedOut = false;
			}

			checkinButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					checkIn();
				}
			});

			checkoutButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					LinkedList ll = new LinkedList();
					ll.add(checkOut());
					new ChunkFrame(new ChunkPanel(ll));
				}
			});

			add(title);
			add(status);
			add(checkoutButton);
			add(checkinButton);
		}
	}

	public ChunkList() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel buttons = new JPanel();
		JButton cia = new JButton("Check In All");
		cia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				checkinAll();
			}
		});

		final JCheckBox together = new JCheckBox("Together", false);
		JButton cio = new JButton("Check Out All");

		cio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				checkoutAll(together.isSelected());
			}
		});
		buttons.add(cio);
		buttons.add(cia);
		buttons.add(together);
		add(buttons);
	}

	public ChunkList(Collection c) {
		this();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			addChunk((HistoryChunk) it.next());
		}
	}

	public void addChunk(HistoryChunk c) {
		add(new CheckoutPanel(c));
	}

}