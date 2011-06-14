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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutFocusTraversalPolicy;

public class SearchingChunker extends SimpleChunkingPolicy {

	private java.util.HashMap mySets = new HashMap();

	private boolean andSearch = false;

	private boolean openAll = false;

	public static final int CONTAINS = 0;

	public static final int EQUALS = 1;

	public static final int OPEN_MAX = 30;

	public static final String[] TYPES = { "Contain", "Equal" };

	public static final String[] INVERT = { "Does", "Does Not" };

	private boolean openTogether;

	public static class CriteriaFrame extends JDialog {
		private Box matcherBox = new Box(BoxLayout.Y_AXIS);

		private boolean cancelling = false;

		private JRadioButton andButton = new JRadioButton("And");

		private JRadioButton orButton = new JRadioButton("Or");

		private JCheckBox openBox = new JCheckBox("Open", true);

		private JCheckBox togetherBox = new JCheckBox("Together", true);

		public CriteriaFrame(Frame parent) {
			super(parent, "Select Criteria", true);
			andButton.setSelected(false);
			ButtonGroup andor = new ButtonGroup();
			orButton.setSelected(true);
			andor.add(andButton);
			andor.add(orButton);

			JButton search = new JButton("Search");
			search.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					dispose();
				}
			});

			getRootPane().setDefaultButton(search);
			setFocusCycleRoot(true);
			setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
				protected boolean accept(Component c) {
					if (c instanceof JTextField) {
						return super.accept(c);
					} else {
						return false;
					}
				}
			});

			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					cancelling = true;
					dispose();
				}
			});

			JButton add = new JButton("Add Matcher");
			add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					MatcherPanel cp = new MatcherPanel();
					matcherBox.add(cp);
					matcherBox.revalidate();
					pack();
				}
			});

			JButton remove = new JButton("Remove Matcher");
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					matcherBox.remove(matcherBox.getComponents().length - 1);
					matcherBox.revalidate();
					pack();
				}
			});

			matcherBox.add(new MatcherPanel());

			JPanel topButtonPanel = new JPanel();
			topButtonPanel.add(andButton);
			topButtonPanel.add(orButton);
			topButtonPanel.add(add);
			topButtonPanel.add(remove);

			JPanel bottomButtonPanel = new JPanel();
			bottomButtonPanel.add(openBox);
			bottomButtonPanel.add(togetherBox);
			bottomButtonPanel.add(cancel);
			bottomButtonPanel.add(search);

			JPanel jp = new JPanel();
			jp.setLayout(new BorderLayout());
			jp.add(topButtonPanel, BorderLayout.NORTH);
			jp.add(bottomButtonPanel, BorderLayout.SOUTH);
			jp.add(new JScrollPane(matcherBox), BorderLayout.CENTER);

			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					cancelling = true;
					dispose();
				}
			});

			setContentPane(jp);
			pack();
		}

		public SearchingChunker showDialog() {
			show();
			if (cancelling) {
				return null;
			} else {
				LinkedList retVal = new LinkedList();
				Component[] crits = matcherBox.getComponents();
				for (int i = 0; i < crits.length; ++i) {
					if (crits[i] instanceof MatcherPanel) {
						retVal.add(((MatcherPanel) crits[i]).getMatcher());
					}
				}
				return new SearchingChunker(retVal, andButton.isSelected(),
						openBox.isSelected(), togetherBox.isSelected());
			}
		}
	}

	public static class MatcherPanel extends JPanel {
		private Box myCriteria = new Box(BoxLayout.Y_AXIS);

		private JRadioButton andButton = new JRadioButton("And");

		private JRadioButton orButton = new JRadioButton("Or");

		public MatcherPanel() {
			myCriteria.setBorder(BorderFactory.createLoweredBevelBorder());
			ButtonGroup andor = new ButtonGroup();
			andor.add(orButton);
			andor.add(andButton);
			andButton.setSelected(true);
			JButton add = new JButton("Add Criterion");
			add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					CriterionPanel cp = new CriterionPanel();
					myCriteria.add(cp);
					myCriteria.revalidate();
				}
			});

			JButton remove = new JButton("Remove Criterion");
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					myCriteria.remove(myCriteria.getComponents().length - 1);
					myCriteria.revalidate();
				}
			});

			myCriteria.add(new CriterionPanel());
			myCriteria.add(new CriterionPanel());

			JPanel topButtonPanel = new JPanel();
			topButtonPanel.add(andButton);
			topButtonPanel.add(orButton);
			topButtonPanel.add(add);
			topButtonPanel.add(remove);

			setLayout(new BorderLayout());
			add(topButtonPanel, BorderLayout.NORTH);
			add(myCriteria, BorderLayout.CENTER);
		}

		public SearchingChunkerMatcher getMatcher() {
			LinkedList retVal = new LinkedList();
			Component[] crits = myCriteria.getComponents();
			for (int i = 0; i < crits.length; ++i) {
				if (crits[i] instanceof CriterionPanel) {
					retVal.add(((CriterionPanel) crits[i]).getCriterion());
				}
			}
			return new SearchingChunkerMatcher(retVal, andButton.isSelected());
		}

	}

	public static class CriterionPanel extends JPanel {
		private JTextField keyName = new JTextField(12);

		private JTextField valueText = new JTextField(12);

		private JComboBox invert = new JComboBox(INVERT);

		private JComboBox type = new JComboBox(TYPES);

		public CriterionPanel() {
			invert.setEditable(false);
			type.setEditable(false);

			add(new JLabel("The value of"));
			add(keyName);
			add(invert);
			add(type);
			add(valueText);
		}

		public SearchingChunkerCriterion getCriterion() {
			return new SearchingChunkerCriterion(keyName.getText(), invert
					.getSelectedIndex() == 0, type.getSelectedIndex(),
					valueText.getText());
		}
	}

	public static class SearchingChunkerMatcher {
		private LinkedList myCriteria = new LinkedList();

		private boolean andSearch;

		public SearchingChunkerMatcher(java.util.List criteria, boolean and) {
			myCriteria.clear();
			myCriteria.addAll(criteria);
			andSearch = and;
		}

		public boolean okay(HistoryItem hi) {
			Iterator it = myCriteria.iterator();
			while (it.hasNext()) {
				SearchingChunkerCriterion scc = (SearchingChunkerCriterion) it
						.next();
				if (scc.okay(hi)) {
					if (!andSearch) {
						return true;
					}
				} else {
					if (andSearch) {
						return false;
					}
				}
			}
			return andSearch;
		}
	}

	public static class SearchingChunkerCriterion {

		private String myKey, myVal;

		private boolean myDoes;

		private int myType;

		public boolean okay(HistoryItem hi) {
			if (myType == CONTAINS) {
				Object o = hi.getProperty(myKey);
				if ((o != null)
						&& (o.toString().toLowerCase().indexOf(
								myVal.toLowerCase()) >= 0)) {
					return myDoes;
				} else {
					return !myDoes;
				}
			} else if (myType == EQUALS) {
				Object o = hi.getProperty(myKey);
				if ((o != null)
						&& (o.toString().toLowerCase().equals(myVal
								.toLowerCase()))) {
					return myDoes;
				} else {
					return !myDoes;
				}
			} else {
				return false;
			}
		}

		public SearchingChunkerCriterion(String key, boolean does,
				int matchType, String val) {
			myKey = key;
			myVal = val;
			myDoes = does;
			myType = matchType;
		}
	}

	public SearchingChunker(java.util.List matchers) {
		this(matchers, false, false, false);
	}

	public SearchingChunker(java.util.List matchers, boolean and) {
		this(matchers, and, false, false);
	}

	public SearchingChunker(java.util.List matchers, boolean and, boolean oa,
			boolean b) {
		andSearch = and;
		openAll = oa;
		openTogether = b;
		Iterator it = matchers.iterator();
		while (it.hasNext()) {
			mySets.put(it.next(), new HashSet());
		}
	}

	public Map chunkData(HistoryDatabase items) {
		Iterator it = items.iterator();
		TreeMap retVal = new TreeMap();
		HashMap tempChunks = new HashMap();
		/*
		 * Iterator matcherIterator = mySets.keySet().iterator(); while
		 * (matcherIterator.hasNext()) { mySets.put(matcherIterator.next(),new
		 * HashSet()); }
		 */
		while (it.hasNext()) {
			HistoryItem hi = (HistoryItem) it.next();
			if (hi != null) {
				String chunkKey = getChunkString(hi);
				if (chunkKey != null) {
					Object o = tempChunks.get(chunkKey);
					if (o == null) {
						o = new HistoryChunk(items);
						((HistoryChunk) o).setName(chunkKey);
						tempChunks.put(chunkKey, o);
					}
					((HistoryChunk) o).addItem((Long) hi
							.getProperty(HistoryItem.KEY));
				}

				Iterator criteriaIterator = mySets.keySet().iterator();
				while (criteriaIterator.hasNext()) {
					SearchingChunkerMatcher scm = (SearchingChunkerMatcher) criteriaIterator
							.next();
					if (scm.okay(hi)) {
						((Set) mySets.get(scm)).add(chunkKey);
					}
				}
			}
		}
		Iterator criteriaIterator = mySets.values().iterator();
		Set currentSet = (Set) criteriaIterator.next();
		Iterator chunkIterator = currentSet.iterator();
		while (chunkIterator.hasNext()) {
			String tempChunkTitle = chunkIterator.next().toString();
			retVal.put(tempChunkTitle, tempChunks.get(tempChunkTitle));
		}
		if (andSearch) {
			while (criteriaIterator.hasNext()) {
				currentSet = (Set) criteriaIterator.next();
				retVal.keySet().retainAll(currentSet);
			}
		} else {
			while (criteriaIterator.hasNext()) {
				chunkIterator = ((Set) criteriaIterator.next()).iterator();
				while (chunkIterator.hasNext()) {
					String tempChunkTitle = chunkIterator.next().toString();
					if (!retVal.containsKey(tempChunkTitle)) {
						retVal.put(tempChunkTitle, tempChunks
								.get(tempChunkTitle));
					}
				}
			}
		}

		if (openAll) {
			LinkedList li = new LinkedList(retVal.values());

			int tailIndex = li.size();
			if (tailIndex > OPEN_MAX) {
				tailIndex = OPEN_MAX;
			}

			if (openTogether) {
				for (Iterator iter = li.iterator(); iter.hasNext();) {
					HistoryChunk element = (HistoryChunk) iter.next();
					element.checkOut();
				}
				new ChunkFrame(new ChunkPanel(li));
			} else {

				ListIterator liter = li.listIterator(tailIndex);
				while (liter.hasPrevious()) {
					HistoryChunk hc = (HistoryChunk) liter.previous();
					hc.checkOut();
					LinkedList l = new LinkedList();
					l.add(hc);
					new ChunkFrame(new ChunkPanel(l));
				}

			}
		}
		return retVal;
	}
}