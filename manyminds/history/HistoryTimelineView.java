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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import manyminds.ManyMindsConstants;

public class HistoryTimelineView extends JPanel implements
		ListSelectionListener {

	private class PaintPanel extends JPanel/* implements Scrollable */{
		public PaintPanel() {
			setBackground(Color.white);
			setForeground(Color.black);
			setOpaque(false);
			setToolTipText("text");
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			currentWidth = (double) (getWidth() - myInsets.right - myInsets.left);

			if (currentWidth != oldWidth) {
				dirty = true;
				oldWidth = currentWidth;
			}

			if (zoomFactor == 0.0D || (dirty)) {
				createPaintRegions();
			}

			paintHighlight(g);

			paintRegions(g);

			paintDrag(g);

		}

		private void paintRegions(Graphics g) {
			Iterator it = myPaintRegions.iterator();
			while (it.hasNext()) {
				((Paintable) it.next()).paint((Graphics2D) g);
			}
		}

		public String getToolTipText(MouseEvent me) {
			return HistoryTimelineView.this.getToolTipText(me);
		}

		public Dimension getSize() {
			if (dirty) {
				createPaintRegions();
			}
			Dimension retVal = new Dimension();
			retVal.width = (int) predictedWidth;
			retVal.height = (int) predictedHeight;
			return retVal;
		}

	}

	private interface ClickHandler {
		public boolean trackClicked(float p);
	}

	private interface Paintable {
		public void paint(Graphics2D g);

		/**
		 * @param bar
		 * @return
		 */
		public boolean overlaps(Shape bar);

	}

	private class ShapePaint implements Paintable {
		private Paint myBorder = null;

		private Paint myFill = null;

		private Shape myShape = null;

		private String myTooltip = null;

		private ClickHandler onClick = null;

		public ShapePaint(Shape s, Paint fill, Paint border, String toolTip,
				ClickHandler click) {
			myShape = s;
			myFill = fill;
			myBorder = border;
			myTooltip = toolTip;
			onClick = click;
		}

		public boolean doClick(Point p) {
			if (onClick != null) {
				Rectangle2D shape = myShape.getBounds2D();
				double minX = shape.getMinX();
				double maxX = shape.getMaxX();
				double clickX = p.getX();
				double ratio = (clickX - minX) / (maxX - minX);
				return onClick.trackClicked((float) ratio);
			} else {
				return false;
			}
		}

		public String getToolTip() {
			return myTooltip;
		}

		public boolean inShape(Point x) {
			return myShape.contains(x);
		}

		public boolean overlaps(Shape s) {
			return myShape.intersects(s.getBounds2D());
		}

		public void paint(Graphics2D g2d) {
			if (myFill != null) {
				g2d.setPaint(myFill);
				g2d.setStroke(FILL_STROKE);
				g2d.fill(myShape);
			}
			if (myBorder != null) {
				g2d.setPaint(myBorder);
				g2d.setStroke(BORDER_STROKE);
				g2d.draw(myShape);
			}
		}

	}

	private class TextDraw implements Paintable {
		private Point2D.Float myLocation;

		private String myText;

		public TextDraw(String s, Point2D.Float loc) {
			myLocation = loc;
			myText = s;
		}

		public void paint(Graphics2D g2d) {
			g2d.setFont(PLAIN_FONT);
			float x = myLocation.x;
			float y = myLocation.y;
			if (x < 0.0) {
				int textWidth = g2d.getFontMetrics().stringWidth(myText);
				x = (float) (-1.0 * (x + textWidth));
			}
			if (y < 0.0) {
				int textHeight = g2d.getFontMetrics().getHeight();
				y = (float) (-1.0 * (textHeight + y));
			}

			g2d.drawString(myText, x, y);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see manyminds.history.HistoryTimelineView.Paintable#overlaps(java.awt.Shape)
		 */
		public boolean overlaps(Shape bar) {
			return false;
		}
	}

	private class VideoClickHandler implements ClickHandler {
		private HistoryItem videoItem;

		private HistoryChunk owningChunk;

		public VideoClickHandler(HistoryItem hi, HistoryChunk hc) {
			videoItem = hi;
			owningChunk = hc;
		}

		public boolean trackClicked(float ratio) {
			try {
				int relIndex = owningChunk.indexOf(videoItem);
				int absIndex = mySelectionModel.rowFromModelRow(owningChunk,
						relIndex);
				mySelectionModel.setSelectionInterval(relIndex, relIndex);
				return true;
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return false;
		}
	}

	protected static final Stroke BORDER_STROKE = new BasicStroke(1.0f);

	protected static final Stroke FILL_STROKE = new BasicStroke(2.0f);

	protected static final Font PLAIN_FONT = new Font("Arial", Font.PLAIN, 10);

	protected static final double barHeight = 20.0D;

	protected static final double barPad = 5.0D;

	protected static final double flagDiam = 6.0D;

	protected static final double breakWidth = 5.0D;

	protected static final Insets myInsets = new Insets(10, 00, 10, 00);

	public static final Color LIGHT_GREEN = new Color(198, 255, 198);

	public static final Color DARKER_GREEN = new Color(138, 215, 148);

	public static final Color LIGHT_BLUE = new Color(218, 218, 255);

	public static final Color DARKER_BLUE = new Color(138, 138, 215);

	protected boolean dirty = true;

	protected double oldWidth = 0.0D;

	protected double currentWidth = 0.0D;

	protected double predictedHeight = 0.0D;

	protected double predictedWidth = 0.0D;

	protected double conversionFactor = 0.0D;

	protected double zoomFactor = 0.0D;

	protected int selectedChunk = -1;

	protected List myChunks = new LinkedList();

	protected ChunkUnionSelectionModel mySelectionModel = null;

	protected double[][] chunkCoords;

	protected List myPaintRegions = new LinkedList();

	protected JPanel paintPanel = new PaintPanel();

	protected JScrollPane myScroll = new JScrollPane(paintPanel,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS) {
		public String getToolTipText(MouseEvent me) {
			return HistoryTimelineView.this.getToolTipText(me);
		}
	};

	protected Date selectionBegin = null;

	protected Date selectionEnd = null;

	protected MouseEvent currentDragEvent;

	protected MouseEvent startDragEvent;

	public HistoryTimelineView() {
		setLayout(new BorderLayout());

		add(myScroll, BorderLayout.CENTER);

		final SpinnerNumberModel zoomModel = new SpinnerNumberModel(1.0D,
				0.01D, 100.0D, 0.1D);
		JSpinner zoomSpinner = new JSpinner(zoomModel);
		zoomSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setZoom(fitZoom() * zoomModel.getNumber().doubleValue());
			}
		});

		JButton fit = new JButton("Fit");
		fit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				zoomModel.setValue(new Double(1.0D));
			}
		});

		JPanel zoomButtons = new JPanel();
		zoomButtons.add(zoomSpinner);
		zoomButtons.add(fit);

		add(zoomButtons, BorderLayout.SOUTH);

		paintPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				boolean shouldSelect = true;
				ListIterator li = myPaintRegions.listIterator(myPaintRegions
						.size());
				while (li.hasPrevious()) {
					Paintable p = (Paintable) li.previous();
					if (p instanceof ShapePaint) {
						if (((ShapePaint) p).inShape(e.getPoint())) {
							shouldSelect = !((ShapePaint) p).doClick(e
									.getPoint());
						}
					}
				}
				if (shouldSelect) {
					for (int i = 0; i < chunkCoords.length; i++) {
						if ((chunkCoords[i][0] <= e.getPoint().getX())
								&& (chunkCoords[i][1] >= e.getPoint().getX())) {
							Date d = coordToDate(e.getPoint().getX(), i);
							int j = ((HistoryChunk) myChunks.get(i))
									.indexOfProperty(HistoryItem.BEGIN_DATE, d,
											true);
							int minRow = mySelectionModel.rowFromModelRow(i, j);
							mySelectionModel.setSelectionInterval(minRow,
									minRow);
						}
					}
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (startDragEvent != null) {
					Date begin = coordToDate(startDragEvent.getX());
					Date end = coordToDate(currentDragEvent.getX());
					if (begin.compareTo(end) < 0) {
						if ((selectedChunk >= 0)
								&& (selectedChunk < myChunks.size())) {
							HistoryChunk hc = (HistoryChunk) myChunks
									.get(selectedChunk);
							HistoryItem hi = new HistoryItem();
							HistoryItem fromChunk = hc.getByIndex(0);
							hi.setProperty(HistoryItem.BEGIN_DATE, begin);
							hi.setProperty(HistoryItem.END_DATE, end);
							hi.setProperty(HistoryItem.COMPUTER, fromChunk
									.getProperty(HistoryItem.COMPUTER));
							hi.setProperty(HistoryItem.PERIOD, fromChunk
									.getProperty(HistoryItem.PERIOD));
							hi.setProperty(HistoryItem.PROJECT, fromChunk
									.getProperty(HistoryItem.PROJECT));
							if (e.isControlDown()) {
								hi.setProperty(HistoryItem.ITEM_TYPE,
										HistoryItem.STATE);
								hi.setProperty(HistoryItem.CHANGE_FROM,
										"Working");
							} else if (e.isShiftDown()) {
								hi.setProperty(HistoryItem.ITEM_TYPE,
										HistoryItem.STATE);
								hi.setProperty(HistoryItem.CHANGE_FROM,
										"Assessing");
							} else {
								hi.setProperty(HistoryItem.ITEM_TYPE,
										HistoryItem.ANNOTATION);
							}
							hc.addHistoryItem(hi);
						}
					}
					startDragEvent = null;
					currentDragEvent = null;
					repaint();
				}
			}

		});
		paintPanel.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent me) {
				if (startDragEvent == null) {
					startDragEvent = me;
					currentDragEvent = me;
				} else {
					currentDragEvent = me;
					repaint();
				}
			}

			public void mouseMoved(MouseEvent e) {
			}
		});

	}

	public void setSelectionModel(ChunkUnionSelectionModel lsm) {
		if (mySelectionModel != null) {
			mySelectionModel.removeListSelectionListener(this);
		}
		mySelectionModel = lsm;
		mySelectionModel.addListSelectionListener(this);
	}

	public HistoryTimelineView(Collection c) {
		this();
		myChunks.addAll(c);
	}

	public double dateToCoord(Date d, int i) {
		double retVal = 0.0D;
		long beginTime = ((HistoryChunk) myChunks.get(i)).getFirstTimestamp()
				.getTime();
		long endTime = ((HistoryChunk) myChunks.get(i)).getLastTimestamp()
				.getTime();
		long locationTime = d.getTime();
		if ((beginTime <= locationTime) && (endTime >= locationTime)) {
			double begin = chunkCoords[i][0];
			double end = chunkCoords[i][1];
			double locationRatio = ((double) (locationTime - beginTime))
					/ ((double) (endTime - beginTime));
			retVal = (end - begin) * locationRatio + begin;
		}
		return retVal;
	}

	public double dateToCoord(Date d) {
		for (int i = 0; i < chunkCoords.length; i++) {
			double retVal = dateToCoord(d, i);
			if (retVal > 0.0D) {
				return retVal;
			}
		}
		return 0.0D;
	}

	public Date coordToDate(double d, int i) {
		double begin = chunkCoords[i][0];
		double end = chunkCoords[i][1];
		if ((begin <= d) && (end >= d)) {
			double location = (d - begin) / (end - begin);
			long beginTime = ((HistoryChunk) myChunks.get(i))
					.getFirstTimestamp().getTime();
			long endTime = ((HistoryChunk) myChunks.get(i)).getLastTimestamp()
					.getTime();
			long locationTime = (long) (((endTime - beginTime) * location) + beginTime);
			return new Date(locationTime);
		} else {
			return null;
		}
	}

	public Date coordToDate(double d) {
		//      double time = ((d - myInsets.left) / conversionFactor) + start;
		//      return new StatedDate((long) time, HistoryItem.SHORT_DATE_FORMAT);

		for (int i = 0; i < chunkCoords.length; i++) {
			Date retVal = coordToDate(d, i);
			if (retVal != null) {
				return retVal;
			}
		}
		return new Date();
	}

	private double fitZoom() {
		long width = HistoryTimelineView.this.getWidth();
		long totalMS = 0l;
		for (Iterator iter = myChunks.iterator(); iter.hasNext();) {
			HistoryChunk chunk = (HistoryChunk) iter.next();
			totalMS += chunk.getLastTimestamp().getTime();
			totalMS -= chunk.getFirstTimestamp().getTime();
		}

		double newZoom = ((double) (width - (myChunks.size() - 1) * breakWidth))
				/ totalMS;
		return newZoom;
	}

	public void setZoom(double d) {
		zoomFactor = d;
		forceRepaint();
	}

	public void setSelectedChunk(int n) {
		selectedChunk = n;
		if (selectedChunk >= 0) {
			double highlightStart = chunkCoords[selectedChunk][0];
			double highlightEnd = chunkCoords[selectedChunk][1];
			Rectangle bar = new Rectangle((int) highlightStart, 0,
					(int) (highlightEnd - highlightStart), 200);
			paintPanel.scrollRectToVisible(bar);
		}
		forceRepaint();
	}

	protected void createPaintRegions() {
		myPaintRegions = new LinkedList();
		chunkCoords = new double[myChunks.size()][2];

		double currentTrack = (double) myInsets.top;
		double baseTrack = (double) myInsets.top;
		double maxTrack = (double) myInsets.top;
		double startLocation = (double) myInsets.left;

		if (zoomFactor == 0.0D) {
			zoomFactor = fitZoom();
		}

		conversionFactor = zoomFactor;
		int chunkNumber = 0;

		for (Iterator iter = myChunks.iterator(); iter.hasNext();) {
			currentTrack = (double) myInsets.top;
			baseTrack = (double) myInsets.top;
			maxTrack = (double) myInsets.top;
			HistoryChunk chunk = (HistoryChunk) iter.next();
			double startOffset = (double) chunk.getFirstTimestamp().getTime();

			Iterator it = chunk.videoIterator();
			while (it.hasNext()) {
				HistorySpan hs = (HistorySpan) it.next();

				double spanWidth = ((double) (hs.getEnd().getTime() - hs
						.getBegin().getTime()))
						* conversionFactor;
				double spanStart = ((double) (hs.getBegin().getTime() - startOffset))
						* conversionFactor + startLocation;
				currentTrack = baseTrack;

				Shape bar = null;
				while (bar == null) {
					bar = new java.awt.geom.Rectangle2D.Double(spanStart,
							currentTrack, spanWidth, barHeight * 0.5);
					Iterator it2 = myPaintRegions.iterator();
					while ((it2 != null) && (it2.hasNext())) {
						if (((Paintable) it2.next()).overlaps(bar)) {
							it2 = null;
							bar = null;
							currentTrack = currentTrack + barPad + barHeight
									* 0.5;
							if (currentTrack > maxTrack) {
								maxTrack = currentTrack;
							}
						}
					}
				}
				myPaintRegions.add(new ShapePaint(bar,
						ManyMindsConstants.QUESTION_COLOR, null, "Video",
						new VideoClickHandler(hs.getHistoryItem(), chunk)));
			}

			currentTrack = maxTrack + barPad + barHeight * 0.5;

			it = chunk.sectionIterator();
			while (it.hasNext()) {
				HistorySpan hs = (HistorySpan) it.next();
				Color paintColor = Color.gray;
				if (hs.getType().equals("Question")) {
					paintColor = ManyMindsConstants.QUESTION_COLOR;
				} else if (hs.getType().equals("Hypothesize")) {
					paintColor = ManyMindsConstants.HYPOTHESIZE_COLOR;
				} else if (hs.getType().equals("Investigate")) {
					paintColor = ManyMindsConstants.INVESTIGATE_COLOR;
				} else if (hs.getType().equals("Analyze")) {
					paintColor = ManyMindsConstants.ANALYZE_COLOR;
				} else if (hs.getType().equals("Model")) {
					paintColor = ManyMindsConstants.MODEL_COLOR;
				} else if (hs.getType().equals("Evaluate")) {
					paintColor = ManyMindsConstants.EVALUATE_COLOR;
				} else if (hs.getType().equals("*")) {
					paintColor = ManyMindsConstants.BASE_BACKGROUND;
				}
				double spanWidth = ((double) (hs.getEnd().getTime() - hs
						.getBegin().getTime()))
						* conversionFactor;
				double spanStart = ((double) (hs.getBegin().getTime() - startOffset))
						* conversionFactor + startLocation;
				Shape bar = new java.awt.geom.Rectangle2D.Double(spanStart,
						currentTrack, spanWidth, barHeight);
				myPaintRegions.add(new ShapePaint(bar, paintColor, null,
						"Section: " + hs.getType(), null));
			}

			currentTrack = currentTrack + barHeight;
			baseTrack = currentTrack;
			maxTrack = currentTrack;

			it = chunk.eventIterator();
			if (it.hasNext()) {
				while (it.hasNext()) {
					HistorySpan hs = (HistorySpan) it.next();
					Color paintColor = Color.black;
					if (hs.getType().equals("Adding advice")) {
						paintColor = ManyMindsConstants.BASE_BACKGROUND;
					} else if (hs.getType().equals("Dismissed advice")) {
						paintColor = ManyMindsConstants.MODEL_COLOR;
					} else if (hs.getType().equals("Reading advice")) {
						paintColor = ManyMindsConstants.INVESTIGATE_COLOR;
					} else if (hs.getType().equals("Page Flip")) {
						paintColor = ManyMindsConstants.HYPOTHESIZE_COLOR;
					} else if (hs.getType().equals("Rater Change")) {
						paintColor = ManyMindsConstants.EVALUATE_COLOR;
					}
					double spanStart = ((double) (hs.getBegin().getTime() - startOffset))
							* conversionFactor + startLocation;
					currentTrack = baseTrack;

					Shape bar = null;
					while (bar == null) {
						bar = new java.awt.geom.Ellipse2D.Double(spanStart
								- flagDiam / 2, currentTrack, flagDiam,
								flagDiam);
						Iterator it2 = myPaintRegions.iterator();
						while ((it2 != null) && (it2.hasNext())) {
							Paintable p = (Paintable) it2.next();
							if ((p instanceof ShapePaint)
									&& (((ShapePaint) p).overlaps(bar))) {
								it2 = null;
								bar = null;
								currentTrack = currentTrack + flagDiam;
								/*
								 * if (currentTrack > maxTrack) { maxTrack =
								 * currentTrack; }
								 */
							}
						}
					}

					myPaintRegions.add(new ShapePaint(bar, paintColor, null,
							"Event: "
									+ hs.getHistoryItem().getProperty(
											HistoryItem.WHAT_HAPPENED) + " "
									+ hs.getType(), null));

				}
				//              currentTrack = maxTrack + flagDiam;
			} else {
				//              currentTrack = currentTrack + barPad;
			}

			currentTrack = baseTrack + (flagDiam * 5);

			it = chunk.actionIterator();
			while (it.hasNext()) {
				HistorySpan hs = (HistorySpan) it.next();
				Color paintColor = Color.black;
				if (hs.getType().equals("Working")) {
					paintColor = ManyMindsConstants.BASE_BACKGROUND;
				} else if (hs.getType().equals("Assessing")) {
					paintColor = ManyMindsConstants.EVALUATE_COLOR;
				}
				//                else if (hs.getType().equals("Document Change: Comment
				// Made")) {
				//                    paintColor = ManyMindsConstants.EVALUATE_COLOR;
				//                }
				else if (hs.getType().equals("Reading")) {
					paintColor = ManyMindsConstants.INVESTIGATE_COLOR;
				}
				double spanWidth = ((double) (hs.getEnd().getTime() - hs
						.getBegin().getTime()))
						* conversionFactor;
				double spanStart = ((double) (hs.getBegin().getTime() - startOffset))
						* conversionFactor + startLocation;
				Shape bar = new java.awt.geom.Rectangle2D.Double(spanStart,
						currentTrack, spanWidth, barHeight);
				myPaintRegions.add(new ShapePaint(bar, paintColor, null,
						"Action: " + hs.getType(), null));
			}

			currentTrack = currentTrack + barPad + barHeight;

			/*
			 * it = chunk.stateIterator(); maxTrack = currentTrack; baseTrack =
			 * currentTrack;
			 * 
			 * if (it.hasNext()) { while (it.hasNext()) { HistorySpan hs =
			 * (HistorySpan) it.next(); Color paintColor = Color.orange;
			 * 
			 * double spanWidth = ((double) (hs.getEnd().getTime() -
			 * hs.getBegin().getTime())) * conversionFactor; double spanStart =
			 * ((double) (hs.getBegin().getTime() - startOffset)) *
			 * conversionFactor + startLocation; currentTrack = baseTrack;
			 * 
			 * if (hs.getType().equals("Working")) { paintColor =
			 * ManyMindsConstants.BASE_BACKGROUND; } else if
			 * (hs.getType().equals("Assessing")) { paintColor =
			 * ManyMindsConstants.EVALUATE_COLOR; } else if
			 * (hs.getType().equals("Reviewing")) { paintColor =
			 * ManyMindsConstants.MODEL_COLOR; } else if
			 * (hs.getType().equals("Reading")) { paintColor =
			 * ManyMindsConstants.INVESTIGATE_COLOR; } else if
			 * (hs.getType().equals("Editing")) { paintColor =
			 * ManyMindsConstants.HYPOTHESIZE_COLOR; }
			 * 
			 * Shape bar = new java.awt.geom.Rectangle2D.Double(spanStart,
			 * currentTrack, spanWidth, barHeight * 0.5); myPaintRegions.add(new
			 * ShapePaint(bar, paintColor, null, hs.getType(), null));
			 *  }
			 * 
			 * currentTrack = maxTrack + barHeight * 0.5 + barPad; }
			 * 
			 * it = chunk.annotationIterator(); maxTrack = currentTrack;
			 * baseTrack = currentTrack;
			 * 
			 * if (it.hasNext()) { while (it.hasNext()) { HistorySpan hs =
			 * (HistorySpan) it.next(); Color paintColor = Color.orange; double
			 * spanWidth = ((double) (hs.getEnd().getTime() -
			 * hs.getBegin().getTime())) * conversionFactor; double spanStart =
			 * ((double) (hs.getBegin().getTime() - startOffset)) *
			 * conversionFactor + startLocation; currentTrack = baseTrack; Shape
			 * bar = null; while (bar == null) { bar = new
			 * java.awt.geom.Rectangle2D.Double(spanStart, currentTrack,
			 * spanWidth, barHeight * 0.5); Iterator it2 =
			 * myPaintRegions.iterator(); while ((it2 != null) &&
			 * (it2.hasNext())) { if (((Paintable) it2.next()).overlaps(bar)) {
			 * it2 = null; bar = null; currentTrack = currentTrack + barPad +
			 * barHeight * 0.5; if (currentTrack > maxTrack) { maxTrack =
			 * currentTrack; } } } }
			 * 
			 * myPaintRegions.add(new ShapePaint(bar, paintColor, null,
			 * hs.getHistoryItem().getProperty(HistoryItem.CHANGE_FROM).toString(),
			 * null));
			 *  }
			 * 
			 * currentTrack = maxTrack + barHeight * 0.5 + barPad; }
			 */

			double lineEnd = ((chunk.getLastTimestamp().getTime() - startOffset) * conversionFactor);
			Shape timeline = new java.awt.geom.Line2D.Double(startLocation,
					currentTrack, startLocation + lineEnd, currentTrack);
			myPaintRegions.add(new ShapePaint(timeline, Color.black, null,
					"The timeline", null));

			/*
			 * currentTrack = currentTrack + 15.0; String date =
			 * HistoryItem.SHORT_DATE_FORMAT.format(new Date((long)
			 * startOffset)); myPaintRegions.add(new TextDraw(date, new
			 * Point2D.Float((float) startLocation, (float) currentTrack)));
			 * 
			 * date =
			 * HistoryItem.SHORT_DATE_FORMAT.format(chunk.getLastTimestamp());
			 * myPaintRegions.add(new TextDraw(date, new
			 * Point2D.Float((float)(-1.0 * (startLocation + lineEnd)), (float)
			 * currentTrack)));
			 */

			currentTrack = currentTrack + 15.0;
			String date = chunk.getName();
			myPaintRegions.add(new TextDraw(date, new Point2D.Float(
					(float) startLocation, (float) currentTrack)));
			predictedHeight = currentTrack + 15.0D;

			chunkCoords[chunkNumber][0] = startLocation;

			startLocation += (lineEnd + breakWidth);
			chunkCoords[chunkNumber][1] = startLocation - breakWidth;
			maxTrack = currentTrack;
			++chunkNumber;
		}
		predictedWidth = startLocation;
		predictedHeight = maxTrack;
		dirty = false;
		paintPanel.setPreferredSize(paintPanel.getSize());
	}

	public void forceRepaint() {
		dirty = true;
		paintPanel.invalidate();
		paintPanel.revalidate();
	}

	public double getPredictedHeight() {
		if (dirty) {
			createPaintRegions();
		}
		return predictedHeight;
	}

	public String getToolTipText(MouseEvent e) {
		ListIterator li = myPaintRegions.listIterator(myPaintRegions.size());
		while (li.hasPrevious()) {
			Paintable p = (Paintable) li.previous();
			if (p instanceof ShapePaint) {
				if (((ShapePaint) p).inShape(e.getPoint())) {
					return ((ShapePaint) p).getToolTip();
				}
			}
		}
		return "tooltip";
	}

	private void paintDrag(Graphics g) {
		if ((startDragEvent != null) && (currentDragEvent != null)) {
			g.setColor(Color.black);
			g.drawLine(startDragEvent.getX(), startDragEvent.getY(),
					currentDragEvent.getX(), startDragEvent.getY());
		}
	}

	private void paintHighlight(Graphics g) {
		int chunkNumber = 0;

		for (Iterator iter = myChunks.iterator(); iter.hasNext();) {
			HistoryChunk hc = (HistoryChunk) iter.next();
			HistoryItem cc = hc.getComment();
			Color paintColor = null;
			if (cc != null) {
				String s = (String) cc.getProperty(HistoryItem.ANNOTATION);
				if (s != null) {
					if (s.indexOf("Day: SF") >= 0) {
						paintColor = LIGHT_BLUE;
					} else if (s.indexOf("Day: FM") >= 0) {
						paintColor = LIGHT_GREEN;
					}
				}
			}
			if (paintColor != null) {
				double highlightStart = chunkCoords[chunkNumber][0];
				double highlightEnd = chunkCoords[chunkNumber][1];
				Shape bar = new java.awt.geom.Rectangle2D.Double(
						highlightStart, 0.0D, highlightEnd - highlightStart,
						200.0D);
				((Graphics2D) g).setPaint(paintColor);
				((Graphics2D) g).fill(bar);
			}
			++chunkNumber;
		}

		int minSelectionIndex = mySelectionModel.getMinSelectionIndex();
		int maxSelectionIndex = mySelectionModel.getMaxSelectionIndex();
		if ((minSelectionIndex >= 0) && (maxSelectionIndex >= 0)) {
			int selectedChunk = mySelectionModel.rowToModel(minSelectionIndex);
			if (selectedChunk >= 0) {
				double highlightStart = chunkCoords[selectedChunk][0];
				double highlightEnd = chunkCoords[selectedChunk][1];
				Shape bar = new java.awt.geom.Rectangle2D.Double(
						highlightStart, 0.0D, highlightEnd - highlightStart,
						200.0D);
				((Graphics2D) g).setPaint(Color.darkGray);
				((Graphics2D) g).setStroke(BORDER_STROKE);
				((Graphics2D) g).draw(bar);

				HistoryChunk hc = (HistoryChunk) myChunks.get(selectedChunk);
				if (hc.getSelectedIndex() >= 0) {
					int relMinSI = mySelectionModel
							.offsetRowIndex(minSelectionIndex);
					int relMaxSI = mySelectionModel
							.offsetRowIndex(maxSelectionIndex);
					highlightStart = dateToCoord((Date) hc.getByIndex(relMinSI)
							.getProperty(HistoryItem.BEGIN_DATE));
					highlightEnd = dateToCoord((Date) hc.getByIndex(relMaxSI)
							.getProperty(HistoryItem.END_DATE));
					bar = new java.awt.geom.Rectangle2D.Double(highlightStart,
							0.0D, highlightEnd - highlightStart, 200.0D);
					((Graphics2D) g).setPaint(Color.lightGray);
					((Graphics2D) g).setStroke(FILL_STROKE);
					((Graphics2D) g).fill(bar);
				}
			}
		}
	}

	public BufferedImage toImage() {
		BufferedImage retVal = new BufferedImage((int) currentWidth,
				(int) predictedHeight + 20, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D) retVal.getGraphics();
		g2d.setPaint(Color.white);
		g2d.fillRect(0, 0, (int) currentWidth, (int) predictedHeight + 20);
		((PaintPanel) paintPanel).paintRegions(g2d);
		g2d.dispose();
		return retVal;
	}

	/**
	 * 
	 * @param e
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		forceRepaint();
	}
}