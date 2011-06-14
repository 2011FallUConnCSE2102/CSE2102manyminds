package manyminds.history;

/*
 * QuickTime for Java SDK Sample Code
 * 
 * Usage subject to restrictions in SDK License Agreement Copyright: ©
 * 1996-1999 Apple Computer, Inc.
 *  
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.BadLocationException;

import quicktime.app.view.QTComponent;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.std.movies.Movie;
import quicktime.std.movies.MovieController;
import quicktime.std.movies.media.DataRef;

public class VideoView extends JPanel implements StdQTConstants {

	private Movie myMovie;

	private MovieController myMovieController;

	private Component currentMovieJC;

	protected transient ChangeEvent changeEvent = null;

	protected EventListenerList listenerList = new EventListenerList();

	private float playbackRate = 1.0f;

	private JSpinner playbackRateSpinner = new JSpinner(new SpinnerNumberModel(
			1.0, 0.0, 5.0, 0.05));

	private JPanel myControlPanel = new JPanel();

	private JTextArea myTranscript;

	public VideoView() {
		setLayout(new BorderLayout());
		playbackRateSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float pbr = ((SpinnerNumberModel) playbackRateSpinner
						.getModel()).getNumber().floatValue();
				setPlayRate(pbr);
			}
		});

		myControlPanel.add(playbackRateSpinner);
		JButton normalSpeed = new JButton("n");
		JButton highSpeed = new JButton("h");
		JButton lowSpeed = new JButton("l");
		JButton backup = new JButton("b");
		JButton insertCode = new JButton("c");
		JButton gotoCode = new JButton("g");
		normalSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				playbackRateSpinner.setValue(new Float(1.0));
				setPlayRate(1.0f);
			}
		});
		highSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				playbackRateSpinner.setValue(new Float(1.7));
				setPlayRate(1.7f);
			}
		});
		lowSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				playbackRateSpinner.setValue(new Float(0.7));
				setPlayRate(0.7f);
			}
		});
		backup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				backup(5);
			}
		});
		insertCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				insertTimeCode();
			}
		});

		gotoCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String selectedText = myTranscript.getSelectedText();
				try {
					int timecode = Integer.parseInt(selectedText);
					int scaledTimecode = (int) (timecode * myMovie
							.getTimeScale());
					myMovie.setTimeValue(scaledTimecode);
				} catch (StdQTException e) {
					e.printStackTrace();
				}
			}
		});

		normalSpeed.setMnemonic(java.awt.event.KeyEvent.VK_N);
		highSpeed.setMnemonic(java.awt.event.KeyEvent.VK_H);
		lowSpeed.setMnemonic(java.awt.event.KeyEvent.VK_L);
		backup.setMnemonic(java.awt.event.KeyEvent.VK_B);
		insertCode.setMnemonic(java.awt.event.KeyEvent.VK_C);
		gotoCode.setMnemonic(java.awt.event.KeyEvent.VK_G);

		normalSpeed.putClientProperty("JButton.buttonType", "toolbar");
		highSpeed.putClientProperty("JButton.buttonType", "toolbar");
		lowSpeed.putClientProperty("JButton.buttonType", "toolbar");
		backup.putClientProperty("JButton.buttonType", "toolbar");
		insertCode.putClientProperty("JButton.buttonType", "toolbar");
		gotoCode.putClientProperty("JButton.buttonType", "toolbar");

		myControlPanel.add(normalSpeed);
		myControlPanel.add(highSpeed);
		myControlPanel.add(lowSpeed);
		myControlPanel.add(backup);
		myControlPanel.add(insertCode);
		myControlPanel.add(gotoCode);
	}

	public void setTranscript(JTextArea t) {
		myTranscript = t;
	}

	protected void setPlayRate(float pbr) {
		if (pbr != playbackRate) {
			playbackRate = pbr;
			try {
				if (myMovieController.getPlayRate() > 0) {
					myMovieController.play(playbackRate);
				}
				myMovie.setPreferredRate(playbackRate);
			} catch (StdQTException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void setVideoURL(String URL) {
		try {
			DataRef urlMovie = new DataRef(URL);
			myMovie = Movie
					.fromDataRef(urlMovie, StdQTConstants.newMovieActive);
			QTComponent movComponent = quicktime.app.view.QTFactory
					.makeQTComponent(myMovie);
			myMovieController = new MovieController(myMovie);
			movComponent.setMovieController(myMovieController);

			if (currentMovieJC != null) {
				remove(currentMovieJC);
			} else {
				add(myControlPanel, BorderLayout.SOUTH);
			}
			currentMovieJC = movComponent.asComponent();
			add(currentMovieJC, BorderLayout.CENTER);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	public int getVideoTime() {
		try {
			float ratio = 1000.0f / (float) myMovie.getTimeScale();
			float time = (float) myMovie.getTime() * ratio;
			return (int) time; //returns a time in msec
		} catch (Throwable t) {
			t.printStackTrace();
			return 0;
		}
	}

	public int getVideoTimeInSeconds() {
		return getVideoTime() / 1000;
	}

	protected void fireStateChanged() {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
			}
		}
	}

	public void backup(int sec) {
		try {
			float scale = ((float) myMovie.getTimeScale()) / 1000.0f;
			int scaledBackup = (int) ((float) (sec * 1000) * scale);
			int newTime = myMovie.getTime() - scaledBackup;
			if (newTime < 0) {
				newTime = 0;
			}
			myMovie.setTimeValue(newTime);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void insertTimeCode() {
		try {
			String timecode = Integer.toString(myMovie.getTime()
					/ myMovie.getTimeScale());
			String toInsert = "\n[timecode[" + timecode + "]]\n";
			myTranscript.insert(toInsert, myTranscript.getCaretPosition());
		} catch (StdQTException e) {
			e.printStackTrace();
		}
	}

	public boolean isPlaying() {
		try {
			return ((myMovie != null) && (myMovie.getRate() > 0));
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public void startPlaying() {
		try {
			if (myMovie != null) {
				myMovieController.play(playbackRate);
				//                videoTrackTimer.start();
				//                if (trackMovie.isSelected()) {
				//                    fireStateChanged();
				//                }
				//                startStop.setText("X");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void stopPlaying() {
		try {
			if (myMovie != null) {
				myMovieController.play(0.0f);
				//                videoTrackTimer.stop();
				//                if (trackMovie.isSelected()) {
				//                    fireStateChanged();
				//                }
				//                startStop.setText(">");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static int getMovieLength(String url) {
		try {
			DataRef urlMovie = new DataRef(url);
			Movie m = Movie
					.fromDataRef(urlMovie, StdQTConstants.newMovieActive);
			float scale = 1000.0f / ((float) m.getTimeScale());
			float duration = m.getDuration() * scale;
			return (int) duration;
		} catch (Throwable t) {
			t.printStackTrace();
			return 0;
		}
	}

}

