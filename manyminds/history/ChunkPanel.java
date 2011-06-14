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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

public class ChunkPanel extends JPanel implements ListSelectionListener {

	private class FindPanel extends JFrame {

		public FindPanel() {
			setDefaultCloseOperation(HIDE_ON_CLOSE);

			JPanel jp = new JPanel();
			JTextField findTextField = new JTextField(40);
			findTextField.setDocument(findText);
			JButton findNextButton = new JButton("Next");
			JButton findPrevButton = new JButton("Previous");
			findTextField.addActionListener(findNext);
			findTextField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					setVisible(false);
				}
			});

			findNextButton.addActionListener(findNext);
			findNextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					setVisible(false);
				}
			});

			findPrevButton.addActionListener(findPrev);
			findPrevButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					setVisible(false);
				}
			});

			jp.add(findTextField);
			jp.add(findNextButton);
			jp.add(findPrevButton);
			setContentPane(jp);
			pack();
		}

	}

	private static final String[] tableColumns = { HistoryItem.BEGIN_DATE,
			HistoryItem.END_DATE, HistoryItem.ITEM_TYPE,
			HistoryItem.WHAT_HAPPENED, HistoryItem.CHANGE_FROM,
			HistoryItem.CHANGE_TO };

	private Action findNext = new AbstractAction("Find Next") {
		private boolean failed = false;

		public void actionPerformed(ActionEvent ae) {
			String findString = "";
			try {
				findString = findText.getText(0, findText.getLength());
			} catch (Throwable t) {
			}
			if ((findString != null) && (!findString.trim().equals(""))) {
				int row = 0;
				if (failed) {
					row = 0;
					failed = false;
				} else {
					row = myTable.getSelectedRow() + 1;
				}
				while (row < myTable.getModel().getRowCount()) {
					for (int col = 0; col <= 7; ++col) {
						if (myTable.getModel().getValueAt(row, col).toString()
								.indexOf(findString) >= 0) {
							myTable.getSelectionModel().setSelectionInterval(
									row, row);
							return;
						}
					}
					++row;
				}
			}
			failed = true;
		}
	};

	private Action findPrev = new AbstractAction("Find Previous") {
		private boolean failed = false;

		public void actionPerformed(ActionEvent ae) {
			String findString = "";
			try {
				findString = findText.getText(0, findText.getLength());
			} catch (Throwable t) {
			}
			if ((findString != null) && (!findString.trim().equals(""))) {
				int row = 0;
				if (failed) {
					row = myTable.getModel().getRowCount() - 1;
					failed = false;
				} else {
					row = myTable.getSelectedRow() - 1;
				}
				while (row > 0) {
					for (int col = 0; col <= 7; ++col) {
						if (myTable.getModel().getValueAt(row, col).toString()
								.indexOf(findString) >= 0) {
							myTable.getSelectionModel().setSelectionInterval(
									row, row);
							return;
						}
					}
					--row;
				}
			}
		}
	};

	private PlainDocument findText = new PlainDocument();

	private List myChunks = new LinkedList();

	private FindPanel myFindPanel = new FindPanel();

	private HistoryItemView myItemView;

	private JTextArea myNote, myTranscript;

	private HistoryTableView myTable;

	private HistoryTimelineView myTimeline;

	private VideoView myVideo;

	private int showingChunk = -1;

	private HistoryItem videoItem, chunkComment;

	//    private javax.swing.Timer videoTrackTimer;

	public ChunkPanel(Collection cs) {
		GridBagConstraints c = new GridBagConstraints(
				GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0);

		myChunks.addAll(cs);

		//        for (Iterator iter = myChunks.iterator(); iter.hasNext();) {
		//            HistoryChunk hc = (HistoryChunk) iter.next();
		//            hc.addListSelectionListener(this);
		//        }

		myTimeline = new HistoryTimelineView(myChunks);
		myItemView = new HistoryItemView();
		myTable = new HistoryTableView(myChunks, tableColumns);
		myTimeline.setSelectionModel((ChunkUnionSelectionModel) myTable
				.getSelectionModel());

		myTable.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent tme) {
				myTimeline.forceRepaint();
			}
		});

		myTable.getSelectionModel().addListSelectionListener(this);

		myNote = new JTextArea();
		myNote.setLineWrap(true);
		myNote.setWrapStyleWord(true);

		myVideo = new VideoView();

		//        videoTrackTimer = new javax.swing.Timer(500, new ActionListener() {
		//            public void
		//            actionPerformed(ActionEvent ae) {
		//                if ((myVideo.isPlaying()) && (myVideo.trackingVideo())) {
		//                    updateFromVideo();
		//                } else {
		//                    videoTrackTimer.stop();
		//                }
		//            }
		//        });
		//        videoTrackTimer.setRepeats(true);
		//        videoTrackTimer.setCoalesce(true);
		//
		//        myVideo.addChangeListener(new ChangeListener() {
		//            public void
		//            stateChanged(ChangeEvent ce) {
		//                if ((myVideo.isPlaying()) && (myVideo.trackingVideo())) {
		//                    videoTrackTimer.start();
		//                } else {
		//                    videoTrackTimer.stop();
		//                }
		//            }
		//        });
		//        
		myTranscript = new JTextArea();
		myTranscript.setLineWrap(true);
		myTranscript.setWrapStyleWord(true);

		myVideo.setTranscript(myTranscript);

		myTranscript.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit
						.getDefaultToolkit().getMenuShortcutKeyMask()),
				"back-five");
		myTranscript.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit
						.getDefaultToolkit().getMenuShortcutKeyMask()),
				"toggle-play");
		myTranscript.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit
						.getDefaultToolkit().getMenuShortcutKeyMask()),
				"insert-timecode");

		myTranscript.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit
						.getDefaultToolkit().getMenuShortcutKeyMask()),
				"back-five");
		myTranscript.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit
						.getDefaultToolkit().getMenuShortcutKeyMask()),
				"toggle-play");
		myTranscript.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit
						.getDefaultToolkit().getMenuShortcutKeyMask()),
				"insert-timecode");

		myTranscript.getActionMap().put("back-five",
				new AbstractAction("back-five") {
					public void actionPerformed(ActionEvent ae) {
						myVideo.backup(5);
					}
				});

		myTranscript.getActionMap().put("toggle-play",
				new AbstractAction("toggle-play") {
					public void actionPerformed(ActionEvent ae) {
						if (myVideo.isPlaying()) {
							myVideo.stopPlaying();
						} else {
							myVideo.startPlaying();
						}
					}
				});

		myTranscript.getActionMap().put("insert-timecode",
				new AbstractAction("insert-timecode") {
					public void actionPerformed(ActionEvent ae) {
						myVideo.insertTimeCode();
					}
				});

		Iterator it = ((HistoryChunk) myChunks.get(0)).videoIterator();
		if (it.hasNext()) {
			HistoryItem hi = ((HistorySpan) it.next()).getHistoryItem();
			setShowingVideo(hi);
		}

		JSplitPane topLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane bottomLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane topRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane bottomRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		topLeft.setOneTouchExpandable(true);
		bottomLeft.setOneTouchExpandable(true);
		topRight.setOneTouchExpandable(true);
		bottomRight.setOneTouchExpandable(true);

		topLeft.setTopComponent(myTimeline);
		//        topLeft.setBottomComponent(myItemView);
		topLeft.setBottomComponent(new JScrollPane(myNote));
		bottomLeft.setTopComponent(topLeft);
		bottomLeft.setBottomComponent(new JScrollPane(myTable));

		//        topRight.setTopComponent(myVideo);
		//        topRight.setBottomComponent(new JScrollPane(myTranscript));
		topRight.setTopComponent(myItemView);
		topRight.setBottomComponent(myVideo);
		bottomRight.setTopComponent(topRight);
		//        bottomRight.setBottomComponent(new JScrollPane(myNote));
		bottomRight.setBottomComponent(new JScrollPane(myTranscript));

		JSplitPane over = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				bottomLeft, bottomRight);

		setLayout(new BorderLayout());
		add(over, BorderLayout.CENTER);

		setSize(new Dimension(1200, 768));

		bottomLeft.setDividerLocation(500);
		topLeft.setDividerLocation(300);
		bottomRight.setDividerLocation(700);
		topRight.setDividerLocation(500);
		over.setDividerLocation(600);

		//top.setDividerLocation(500);
		//jsp.setDividerLocation(200);
		setVisible(true);
	}

	public void addNotify() {
		super.addNotify();
		((Window) getTopLevelAncestor()).addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				myVideo.stopPlaying();
				if (videoItem != null) {
					videoItem.setProperty(HistoryItem.VIDEO_TRANSCRIPT,
							myTranscript.getText());
				}
				if (chunkComment != null) {
					String comment = myNote.getText().trim();
					if (comment.length() > 0) {
						chunkComment.setProperty(HistoryItem.ITEM_ANNOTATION,
								comment);
					} else {
						try {
							((HistoryChunk) myChunks.get(showingChunk))
									.removeItem(chunkComment);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				} else {
					String comment = myNote.getText().trim();
					if (comment.length() > 0) {
						HistoryItem newComment = new HistoryItem();
						HistoryChunk currentChunk = (HistoryChunk) myChunks
								.get(showingChunk);
						newComment.setProperty(HistoryItem.ITEM_TYPE,
								HistoryItem.CHUNK_COMMENT);
						newComment.setProperty(HistoryItem.CHUNK_NAME,
								currentChunk.getName());
						String cc = currentChunk.getName();
						int spaceOne = cc.indexOf(" ");
						int spaceTwo = cc.indexOf(" ", spaceOne + 1);
						String computer = cc.substring(0, spaceOne).trim();
						computer = computer.substring(0, 1).toUpperCase()
								+ computer.substring(1);
						String period = cc.substring(spaceOne, spaceTwo).trim();
						newComment.setProperty(HistoryItem.COMPUTER, computer);
						newComment.setProperty(HistoryItem.PERIOD, period);
						newComment.setProperty(HistoryItem.ITEM_ANNOTATION,
								comment);
						newComment.setProperty(HistoryItem.BEGIN_DATE,
								currentChunk.getFirstTimestamp());
						newComment.setProperty(HistoryItem.END_DATE,
								currentChunk.getLastTimestamp());
						currentChunk.addHistoryItem(newComment);
					}
				}
			}
		});
	}

	public Action getCommentInsertAction() {
		return new AbstractAction("Insert Comment") {
			public void actionPerformed(ActionEvent ae) {
				try {
					HistoryItem hi = new HistoryItem();
					if ((showingChunk >= 0) && (showingChunk < myChunks.size())) {
						HistoryChunk hc = (HistoryChunk) myChunks
								.get(showingChunk);
						hi.setProperty(HistoryItem.BEGIN_DATE, hc
								.getFirstTimestamp());
						hi.setProperty(HistoryItem.END_DATE, hc
								.getFirstTimestamp());
						hi.setProperty(HistoryItem.ITEM_TYPE,
								HistoryItem.ANNOTATION);
						HistoryItem fromChunk = hc.getByIndex(0);
						hi.setProperty(HistoryItem.COMPUTER, fromChunk
								.getProperty(HistoryItem.COMPUTER));
						hi.setProperty(HistoryItem.PERIOD, fromChunk
								.getProperty(HistoryItem.PERIOD));
						hi.setProperty(HistoryItem.PROJECT, fromChunk
								.getProperty(HistoryItem.PROJECT));
						hc.addHistoryItem(hi);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
	}

	public Action getCSVAction() {
		return new AbstractAction("To CSV Table") {
			public void actionPerformed(ActionEvent ae) {
				try {
					StringBuffer retVal = new StringBuffer();
					for (Iterator iter = myChunks.iterator(); iter.hasNext();) {
						HistoryChunk element = (HistoryChunk) iter.next();
						retVal.append(element.toCSV());
					}
					System.err.println(retVal.toString());
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
	}

	public Action getDeleteAction() {
		return new AbstractAction("Delete Items") {
			public void actionPerformed(ActionEvent ae) {
				try {
					if (JOptionPane.showConfirmDialog(null, "confirm",
							"Are you sure?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						int[] rows = myTable.getSelectedRows();
						for (int i = rows.length - 1; i >= 0; --i) {
							HistoryChunk hc = (HistoryChunk) myChunks
									.get(myTable.rowToModel(rows[i]));
							hc.removeItem(myTable.offsetRowIndex(rows[i]));
						}
						if (rows[0] <= 0) {
							rows[0] = 1;
						}
						myTable.getSelectionModel().setSelectionInterval(
								rows[0] - 1, rows[0] - 1);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
	}

	public Action getFindNextAction() {
		return findNext;
	}

	public Action getFindPreviousAction() {
		return findPrev;
	}

	public Action getFindTextAction() {
		return new AbstractAction("Find Text") {
			public void actionPerformed(ActionEvent ae) {
				Component c = ((Window) getTopLevelAncestor()).getFocusOwner();
				if (c == null) {
				} else if (c instanceof JTextComponent) {
					String a = ((JTextComponent) c).getText();
					String b = ((JTextComponent) c).getSelectedText();
					if ((b != null) && (b.trim().length() > 0)) {
						a = b;
					}
					try {
						findText.remove(0, findText.getLength());
						findText.insertString(0, a, null);
					} catch (Throwable t) {
					}
				} else if (c instanceof JTable) {
					JTable jt = (JTable) c;
					if (jt.isEditing()) {
						c = jt.getEditorComponent();
						if (c instanceof JTextComponent) {
							String a = ((JTextComponent) c).getText();
							String b = ((JTextComponent) c).getSelectedText();
							if ((b != null) && (b.trim().length() > 0)) {
								a = b;
							}
							try {
								findText.remove(0, findText.getLength());
								findText.insertString(0, a, null);
							} catch (Throwable t) {
							}
						}
					} else {
						if ((jt.getSelectedRow() >= 0)
								&& (jt.getSelectedColumn() >= 0)) {
							String s = jt.getValueAt(jt.getSelectedRow(),
									jt.getSelectedColumn()).toString();
						}
					}
				}
			}
		};
	}

	public Action getImageAction() {
		return new AbstractAction("Export Timeline") {
			public void actionPerformed(ActionEvent ae) {
				try {
					JFileChooser jfc = new JFileChooser();
					jfc.setFileFilter(new FileFilter() {

						public boolean accept(File f) {
							if (f.getName().endsWith(".jpg")) {
								return true;
							} else {
								return false;
							}
						}

						public String getDescription() {
							return "JPEG files only";
						}

					});
					if (jfc.showSaveDialog(ChunkPanel.this) == JFileChooser.APPROVE_OPTION) {
						File f = jfc.getSelectedFile();
						ImageIO.write(myTimeline.toImage(), "jpg", f);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
	}

	public Action getLatexAction() {
		return new AbstractAction("To LaTeX Table") {
			public void actionPerformed(ActionEvent ae) {
				try {
					StringBuffer retVal = new StringBuffer();
					for (Iterator iter = myChunks.iterator(); iter.hasNext();) {
						HistoryChunk element = (HistoryChunk) iter.next();
						retVal.append(element.toLatex());
					}
					JFrame jf = new JFrame();
					JTextArea jta = new JTextArea();
					jta.setText(retVal.toString());
					jf.setContentPane(new JScrollPane(jta));
					jf.pack();
					jf.setVisible(true);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
	}

	public Action getShowFindPanelAction() {
		return new AbstractAction("Show Find Panel") {
			public void actionPerformed(ActionEvent ae) {
				myFindPanel.setVisible(true);
				myFindPanel.toFront();
			}
		};
	}

	public Action getVideoInsertAction() {
		return new AbstractAction("Insert Video") {
			public void actionPerformed(ActionEvent ae) {
				try {
					HistoryItem hi = new HistoryItem();
					if ((showingChunk >= 0) && (showingChunk < myChunks.size())) {
						HistoryChunk hc = (HistoryChunk) myChunks
								.get(showingChunk);
						hi.setProperty(HistoryItem.BEGIN_DATE, hc
								.getFirstTimestamp());
						hi.setProperty(HistoryItem.END_DATE, hc
								.getFirstTimestamp());
						hi.setProperty(HistoryItem.ITEM_TYPE,
								HistoryItem.VIDEO_RESOURCE);
						HistoryItem fromChunk = hc.getByIndex(0);
						hi.setProperty(HistoryItem.COMPUTER, fromChunk
								.getProperty(HistoryItem.COMPUTER));
						hi.setProperty(HistoryItem.PERIOD, fromChunk
								.getProperty(HistoryItem.PERIOD));
						hi.setProperty(HistoryItem.PROJECT, fromChunk
								.getProperty(HistoryItem.PROJECT));
						hc.addHistoryItem(hi);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
	}

	public Action getVideoSyncAction() {
		return new AbstractAction("Sync Video") {
			public void actionPerformed(ActionEvent ae) {
				if ((showingChunk >= 0) && (showingChunk < myChunks.size())) {
					HistoryChunk hc = (HistoryChunk) myChunks.get(showingChunk);
					Iterator it = hc.sectionIterator();
					if (it.hasNext()) {
						Date firstItem = ((HistorySpan) it.next()).getBegin();
						long midpointTarget = firstItem.getTime();
						long videoStart = ((Date) videoItem
								.getProperty(HistoryItem.BEGIN_DATE)).getTime();
						long currentMidpoint = ((long) myVideo.getVideoTime())
								+ videoStart;

						long videoOffset = midpointTarget - currentMidpoint;

						videoStart = videoStart + videoOffset;
						//                        videoStart = videoStart - (currentMidpoint -
						// midpointTarget);
						videoItem.setProperty(HistoryItem.BEGIN_DATE, new Date(
								videoStart));
						hc.resortHistoryItems();

					}
				}
			}
		};
	}

	public void setShowingVideo(HistoryItem hi) {
		if (hi != videoItem) {
			if (videoItem != null) {
				String videoTranscript = myTranscript.getText().trim();
				if (videoTranscript.length() > 0) {
					videoItem.setProperty(HistoryItem.VIDEO_TRANSCRIPT,
							videoTranscript);
				} else {
					videoItem.setProperty(HistoryItem.VIDEO_TRANSCRIPT, null);
				}
			}
			videoItem = hi;
			String videoFilename = videoItem.getProperty(
					HistoryItem.CHANGE_FROM).toString();
			File videoFile = new File(System.getProperty("manyminds.home")
					+ System.getProperty("file.separator") + "VideoFiles"
					+ System.getProperty("file.separator") + videoFilename);
			if (videoFile.exists()) {
				myVideo.setVideoURL("file://" + videoFile.getAbsolutePath());
				if (videoItem.getProperty(HistoryItem.VIDEO_TRANSCRIPT) != null) {
					myTranscript.setText(videoItem.getProperty(
							HistoryItem.VIDEO_TRANSCRIPT).toString());
				} else {
					myTranscript.setText("");
				}
			}
		}
	}

	public void updateFromVideo() {
		int time = myVideo.getVideoTime();
		Date videoBegin = (Date) videoItem.getProperty(HistoryItem.BEGIN_DATE);
		Date currentPlace = new Date(videoBegin.getTime() + time);
		if ((showingChunk >= 0) && (showingChunk < myChunks.size())) {
			HistoryChunk hc = (HistoryChunk) myChunks.get(showingChunk);
			int i = hc.indexOfProperty(HistoryItem.BEGIN_DATE, currentPlace,
					true);
			i = myTable.rowFromModelRow(showingChunk, i);
			myTable.setRowSelectionInterval(i, i);
			String transcriptText = myTranscript.getText();
			int bracLoc = transcriptText.indexOf("[timecode[");
			int lastTime = 0;
			int lastTimeLoc = 0;
			while (bracLoc > 0) {
				int closeBrac = transcriptText.indexOf("]]", bracLoc);
				try {
					int foundTime = Integer.parseInt(transcriptText.substring(
							bracLoc + ("[timecode[".length()), closeBrac));
					if (time < foundTime) {
						if ((time - lastTime) > (foundTime - time)) {
							myTranscript.setCaretPosition(bracLoc);
						} else {
							myTranscript.setCaretPosition(lastTimeLoc);
						}
						bracLoc = -1;
					} else {
						lastTime = foundTime;
						lastTimeLoc = bracLoc;
						bracLoc = transcriptText.indexOf("[timecode[",
								bracLoc + 1);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			if (lastTimeLoc > 0) {
				try {
					myTranscript.setCaretPosition(lastTimeLoc);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	public void valueChanged(ListSelectionEvent ce) {
		ChunkUnionSelectionModel model = (ChunkUnionSelectionModel) ce
				.getSource();
		int absRow = model.getMinSelectionIndex();
		int selectedChunkIndex = model.rowToModel(absRow);
		int relRow = model.offsetRowIndex(absRow);

		if (relRow >= 0) {

			HistoryItem selectedHI = ((HistoryChunk) myChunks
					.get(selectedChunkIndex)).getByIndex(relRow);

			//rotate HIV
			myItemView.setHistoryItem(selectedHI);

			//rotate VideoView
			if (HistoryItem.VIDEO_RESOURCE.equals(selectedHI
					.getProperty(HistoryItem.ITEM_TYPE))) {
				setShowingVideo(selectedHI);
			}
		}

		if ((relRow >= 0) && (selectedChunkIndex != showingChunk)) {

			//what to do when we have a new chunk entirely

			myTimeline.setSelectedChunk(selectedChunkIndex);

			//Rotate Chunk Comment

			if (showingChunk >= 0) {
				HistoryChunk oldSelectedChunk = (HistoryChunk) myChunks
						.get(showingChunk);
				if (chunkComment != null) {
					String comment = myNote.getText().trim();
					if (comment.length() > 0) {
						chunkComment.setProperty(HistoryItem.ITEM_ANNOTATION,
								comment);
					} else {
						oldSelectedChunk.removeItem(chunkComment);
					}
				} else {
					String comment = myNote.getText().trim();
					if (comment.length() > 0) {
						HistoryItem newComment = new HistoryItem();
						newComment.setProperty(HistoryItem.ITEM_TYPE,
								HistoryItem.CHUNK_COMMENT);
						newComment.setProperty(HistoryItem.CHUNK_NAME,
								oldSelectedChunk.getName());
						newComment.setProperty(HistoryItem.ITEM_ANNOTATION,
								comment);
						newComment.setProperty(HistoryItem.BEGIN_DATE,
								oldSelectedChunk.getFirstTimestamp());
						newComment.setProperty(HistoryItem.END_DATE,
								oldSelectedChunk.getLastTimestamp());
						String cc = oldSelectedChunk.getName();
						int spaceOne = cc.indexOf(" ");
						int spaceTwo = cc.indexOf(" ", spaceOne + 1);
						String computer = cc.substring(0, spaceOne).trim();
						computer = computer.substring(0, 1).toUpperCase()
								+ computer.substring(1);
						String period = cc.substring(spaceOne, spaceTwo).trim();
						newComment.setProperty(HistoryItem.COMPUTER, computer);
						newComment.setProperty(HistoryItem.PERIOD, period);
						oldSelectedChunk.addHistoryItem(newComment);
					}
				}
			}
			chunkComment = ((HistoryChunk) myChunks.get(selectedChunkIndex))
					.getComment();
			if (chunkComment != null) {
				myNote.setText(chunkComment.getProperty(
						HistoryItem.ITEM_ANNOTATION).toString());
			} else {
				myNote.setText("");
			}

			//Rotate Chunk video

			if (selectedChunkIndex >= 0) {
				Iterator it = ((HistoryChunk) myChunks.get(selectedChunkIndex))
						.videoIterator();
				if (it.hasNext()) {
					HistoryItem hi = ((HistorySpan) it.next()).getHistoryItem();
					setShowingVideo(hi);
				}
			}
			showingChunk = selectedChunkIndex;
		}

		myTimeline.repaint();
	}
}