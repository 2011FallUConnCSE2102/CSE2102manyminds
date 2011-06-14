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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class HistoryItemView extends JPanel {

	private static class ItemFieldEditor extends JPanel implements
			PropertyChangeListener {

		private JTextArea myText = new JTextArea();

		private HistoryItem myHistoryItem = null;

		private String myProperty;

		public ItemFieldEditor(String p) {
			myProperty = p;
			myText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				}

				public void focusLost(FocusEvent e) {
					if (myHistoryItem != null) {
						maybeUpdate();
					}
				}
			});

			myText.setLineWrap(true);
			myText.setWrapStyleWord(true);
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = GridBagConstraints.RELATIVE;
			c.weightx = 0.0;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			JLabel nameLabel = new JLabel(p) {
				public Dimension getPreferredSize() {
					Dimension retVal = super.getPreferredSize();
					retVal.width = 100;
					return retVal;
				}
			};
			add(nameLabel, c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
			add(new JScrollPane(myText), c);
		}

		protected synchronized void maybeUpdate() {
			if (!myHistoryItem.getProperty(myProperty).toString().equals(
					myText.getText())) {
				myHistoryItem.setProperty(myProperty, myText.getText());
			}
		}

		public void propertyChange(PropertyChangeEvent pce) {
			if (pce.getPropertyName().equals(myProperty)) {
				myText
						.setText(myHistoryItem.getProperty(myProperty)
								.toString());
			}
		}

		public void setHistoryItem(HistoryItem hi) {
			if (myHistoryItem != null) {
				maybeUpdate();
				myHistoryItem.removePropertyChangeListener(this);
			}
			myHistoryItem = hi;
			if (myHistoryItem.getProperty(myProperty) != null) {
				myText
						.setText(myHistoryItem.getProperty(myProperty)
								.toString());
			}
			myHistoryItem.addPropertyChangeListener(this);
		}
	}

	private HistoryItem myHistoryItem;

	public HistoryItemView() {
		this(null);
	}

	protected void highlightChanges() {
	}

	public HistoryItemView(HistoryItem hi) {
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints(
				GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0);

		c.gridwidth = GridBagConstraints.REMAINDER;

		add(new ItemFieldEditor(HistoryItem.BEGIN_DATE), c);
		add(new ItemFieldEditor(HistoryItem.END_DATE), c);
		add(new ItemFieldEditor(HistoryItem.ITEM_TYPE), c);
		add(new ItemFieldEditor(HistoryItem.WHAT_HAPPENED), c);
		add(new ItemFieldEditor(HistoryItem.CHANGE_FROM), c);
		add(new ItemFieldEditor(HistoryItem.CHANGE_TO), c);
		add(new ItemFieldEditor(HistoryItem.CHANGE_SECTION), c);
		add(new ItemFieldEditor(HistoryItem.ITEM_ANNOTATION), c);

		if (hi != null) {
			setHistoryItem(hi);
		}

	}

	public void setHistoryItem(HistoryItem hi) {
		if (hi == null) {
			hi = new HistoryItem();
		}
		myHistoryItem = hi;
		Component comps[] = getComponents();
		for (int i = 0; i < comps.length; ++i) {
			if (comps[i] instanceof ItemFieldEditor) {
				((ItemFieldEditor) comps[i]).setHistoryItem(myHistoryItem);
			}
		}
		highlightChanges();
	}
}

