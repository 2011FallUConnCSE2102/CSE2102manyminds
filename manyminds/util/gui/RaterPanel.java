/*	File RaterPanel.java
* =============================================================================
* 
*	A panel containing a criterion title, a slider control, and a summary
*	description of its current value, allowing the user to evaluate their project
*	work thus far with respect to some criterion.  These sliders appear all over
*	the ManyMinds Artifact.
* 
* Author Chris Schneider
* Copyright © 1999 University of California
* All Rights Reserved.
* 
* Agenda
* 
* History
* 08 Dec 99	CSS	New today (from ArtifactScrollTextArea.java).
* 21 FEB 00	EME	Moved data into RaterModel.java, added change support
* =============================================================================
*/

/*  Copyright (C) 1998-2002 Regents of the University of California
 *  This file is part of ManyMinds.
 *
 *  ManyMinds is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  ManyMinds is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with ManyMinds; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package manyminds.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
import manyminds.debug.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.io.*;
import javax.swing.plaf.*;
import manyminds.datamodel.*;
import manyminds.util.*;
import manyminds.helpers.PageDisplayerThread;

/**
*	A panel containing a criterion title, a slider control, and a summary
*	description of its current value, allowing the user to evaluate their project
*	work thus far with respect to some criterion.  These sliders appear all over
*	the ManyMinds Artifact.
* 
* @author	Chris Schneider
*/
public
class RaterPanel
extends JPanel
implements manyminds.ManyMindsConstants
{
    public static final int MAJOR_TICK_SPACING	= 1;
    public static final int SLIDER_WIDTH = 90;
    public static final int PANEL_HEIGHT = 50;
    public static final int PANEL_WIDTH = SLIDER_WIDTH + 10;
    public static final int PANEL_WITH_TEXT_WIDTH = 250;
    public static final int SLIDER_HEIGHT = 16;
    
    private static final ImageIcon EMPTY_ICON = new ImageIcon(ManyMindsResolver.resolveClasspathURI("classpath://manyminds/resources/images/MakeComment.gif"));
    private static final ImageIcon FULL_ICON = new ImageIcon(ManyMindsResolver.resolveClasspathURI("classpath://manyminds/resources/images/CommentMade.gif"));
    private static final ImageIcon LINK_ICON = new ImageIcon(ManyMindsResolver.resolveClasspathURI("classpath://manyminds/resources/images/URLAnchor.gif"));
    private static final String EMPTY_TEXT = "Click here to add a note to this rater";
    
    private static java.util.List allPanels = Collections.synchronizedList(new LinkedList());
    
    private JTextArea criterionTitle = null;
    private SuperButton myLinkButton = null;
    private boolean showingComment = false;
    private JLabel ratingIcon = null;
    private JSlider slider;
    //private JLabel ratingSummary = null;
    private JTextArea ratingSummary = null;
    private SliderListener sliderListener = new SliderListener();
    private RaterModel raterModel = null;
    public static String preTag = "<html>";
    public static String postTag = "</html>";
    private int oldValue = -1;
    private boolean noComment = true;
    private String myDocumentTitle = null;
    private String linkTarget = null;
    private Document commentDocument = null;
    private JTextArea commentTextArea = null;
    private JScrollPane commentScroller = null;
    private String commentLocation = BorderLayout.SOUTH;
    
    private DataListener myCommentListener = new DataListener() {
        public void
        valueChanged(ChangeEvent ce) {
            SwingUtilities.invokeLater(new Runnable() {
                public void
                run() {
                    String s = ((ManyMindsDocument)commentDocument).getValue().trim();
                    if (s.length() != 0) {
                        if (noComment) {
                            noComment = false;
                            ratingIcon.setIcon(FULL_ICON);
                        }
                        ratingIcon.setToolTipText(((ManyMindsDocument)commentDocument).getValue());
                    } else if (!noComment) {
                        noComment = true;
                        ratingIcon.setIcon(EMPTY_ICON);
                        ratingIcon.setToolTipText(EMPTY_TEXT);
                    }
                }
            });
        }
    };

    class SliderListener implements ChangeListener
    {
        public void
        stateChanged(ChangeEvent event)
        {
            JSlider slider = (JSlider)event.getSource();
            if (slider.getModel().getValue() != oldValue) {
                ratingSummary.setText(getSliderSummary());
                if (!slider.getValueIsAdjusting()) {
                    oldValue = slider.getValue();
                    ratingSummary.setToolTipText(getSliderToolTip());
                } else {
                }
            }
        }
    }

    private class MySlider
    extends JSlider {
        public MySlider(BoundedRangeModel brm) {
            super(brm);
        }
        
        public void
        fireChangeEvent() {
            fireStateChanged();
        }
    }
    
    private String
    getSliderToolTip() {
        if (getModel().getToolTips().getSize() > slider.getValue()) {
           // return s;
            return getModel().getToolTips().getElementAt(slider.getValue()).toString();
        } else {
            return new String();
        }
    }
    
    private String
    getSliderSummary() {
        if (getModel().getSummaries().getSize() > slider.getValue()) {
            return getModel().getSummaries().getElementAt(slider.getValue()).toString();
        } else {
            return new String();
        }
    }
    
    private String
    getTitle() {
        return getModel().getTitle();
    }
    
    private String
    getToolTip() {
      return getModel().getTitleToolTip();
    }
    
    public void
    setCommentLocation(String i) {
        commentLocation = i;
    }
    
    public
    RaterPanel() {
        this(RaterModel.instantiateRaterModel(null));
    }
    
    public void
    showDocument() {
        if (!showingComment) {
            add(commentScroller,commentLocation);
            showingComment = true;
            revalidate();
        }
    }
    
    public void
    hideDocument() {
        if (showingComment) {
            remove(commentScroller);
            showingComment = false;
            revalidate();
        }
    }
    
    public void
    toggleDocument() {
        if (showingComment) {
            hideDocument();
        } else {
            synchronized (allPanels) {
                Iterator it = allPanels.iterator();
                while (it.hasNext()) {
                    ((RaterPanel)it.next()).hideDocument();
                }
            }
            showDocument();
        }
    }
    
    public
    RaterPanel(RaterModel rm) {
        JPanel jp = new JPanel();
    
        jp.setLayout(new GridBagLayout());
        jp.setBackground(RATER_BACKGROUND);
        
        GridBagConstraints gbc = new GridBagConstraints(GridBagConstraints.RELATIVE,GridBagConstraints.RELATIVE,GridBagConstraints.REMAINDER,1,1.0,0.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.RELATIVE;

        
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        myLinkButton = new SuperButton(LINK_ICON,"<give-advice><url><![CDATA["+linkTarget+"]]></url></give-advice>");
        myLinkButton.setBorder(BorderFactory.createEmptyBorder());
        myLinkButton.setBackground(RATER_BACKGROUND);
        myLinkButton.setToolTipText("Click here for more information about this slider");
        jp.add(myLinkButton,gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        criterionTitle = new JTextArea("");
        criterionTitle.setEnabled(false);
        criterionTitle.setLineWrap(true);
        criterionTitle.setWrapStyleWord(true);
        criterionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        criterionTitle.setFont(SMALL_FONT.deriveFont(Font.BOLD));
        gbc.insets = new Insets(0,3,0,0);
        jp.add(criterionTitle,gbc);
        gbc.insets = new Insets(0,0,0,0);        

        
        slider = new JSlider(rm.getSliderModel());
        slider.setMajorTickSpacing(MAJOR_TICK_SPACING);
        slider.setSnapToTicks(true);
        slider.setFont(SMALL_FONT);
        gbc.fill = GridBagConstraints.BOTH;
        jp.add(slider,gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        ratingIcon = new JLabel(EMPTY_ICON,SwingConstants.LEFT);
        jp.add(ratingIcon,gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.EAST;
        
        ratingSummary = new JTextArea("");
        ratingSummary.setEnabled(false);
        ratingSummary.setLineWrap(true);
        ratingSummary.setWrapStyleWord(true);
        ratingSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
        ratingSummary.setFont(SMALL_FONT.deriveFont(Font.ITALIC));
        gbc.insets = new Insets(0,3,0,0);
        jp.add(ratingSummary,gbc);
        gbc.insets = new Insets(0,0,0,0);        
        
        setLayout(new BorderLayout());
        add(jp,BorderLayout.CENTER);
        
        ratingIcon.addMouseListener(new MouseAdapter() {
            public void
            mouseClicked(MouseEvent me) {
                if (commentDocument != null) {
                    toggleDocument();
                }
            }
        });
        
        commentTextArea = new JTextArea(5,20);
        //commentTextArea = new JTextArea("");
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setLineWrap(true);
        commentScroller = new JScrollPane(commentTextArea);


        ratingIcon.setToolTipText(EMPTY_TEXT);
        slider.addChangeListener(sliderListener);
       // slider.getModel().addChangeListener(sliderListener);
        //rm.addRaterChangeListener(sliderListener);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        //ToolTipManager.sharedInstance().setInitialDelay(0);
        setModel(rm);
        setBackground(RATER_BACKGROUND);
        setForeground(RATER_TEXT);
        slider.setBackground(RATER_BACKGROUND);
        slider.setForeground(RATER_TEXT);
        ratingSummary.setBackground(RATER_BACKGROUND);
        ratingSummary.setForeground(RATER_TEXT);
        criterionTitle.setBackground(RATER_BACKGROUND);
        criterionTitle.setForeground(RATER_TEXT);
        
        allPanels.add(this);
        
    }
    
    /**
        * Returns the RaterModel that underlies this megawidget
        *
        * @return	RaterModel	the RaterModel data model for this widget
        */
    
    public RaterModel 
    getModel() {
        return raterModel;
    } //getModel
    
    private void
    setLinkTarget(String s) {
        myLinkButton.setCommand(s);
    }
    
    /**
        * Sets the data model for this widget, and registers this as a listener to that model
        *
        * @param model the new data model for the widget
        */
    
    public void
    setModel(RaterModel model) {
        RaterModel oldModel = getModel();
        raterModel = model;
//        if (oldModel != null) {
//            oldModel.removeRaterChangeListener(sliderListener);
//        }
        criterionTitle.setText(getTitle());
        criterionTitle.setToolTipText(getToolTip());
        slider.setModel(model.getSliderModel());
        ratingSummary.setText(getSliderSummary());
        ratingSummary.setToolTipText(getSliderToolTip());
        setLinkTarget(model.getLinkTarget());
        if (model.getLinkedDocumentName() != null) {
            Data data = DataContext.getContext().getSharedData().getData(model.getLinkedDocumentName());
            if ((data != null) && (data instanceof ManyMindsDocument)) {
                if ((commentDocument != null) && (commentDocument instanceof ManyMindsDocument)) {
                    ((ManyMindsDocument)commentDocument).removeDataListener(myCommentListener);
                }
                commentDocument = (ManyMindsDocument)data;
                if (((ManyMindsDocument)commentDocument).getValue().trim().length() == 0) {
                    noComment = true;
                    ratingIcon.setIcon(EMPTY_ICON);
                } else {
                    noComment = false;
                    ratingIcon.setIcon(FULL_ICON);
                    ratingIcon.setToolTipText(((ManyMindsDocument)commentDocument).getValue());
                }
                ((ManyMindsDocument)commentDocument).addDataListener(myCommentListener);
                commentTextArea.setDocument(commentDocument);
            }
        }
                
            
    }// setModel	
    
    /**
        * Register a change listener on the underlying JSlider element
        *
        * @param l The change listener to register with the slider
        */
    public void
    addSliderChangeListener(ChangeListener l) {
        slider.addChangeListener(l);
    }

    public Dimension
    getMaximumSize() {
        return getPreferredSize();
    }

    public Dimension
    getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension
    getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (!showingComment) {
            d.width = PANEL_WIDTH;
        } else {
            d.width = PANEL_WITH_TEXT_WIDTH;
        }
        return d;
    }

    /**
        * Unregister a change listener from the underlying JSlider element
        *
        * @param l The change listener to unregister from the slider
        */
    public void
    removeSliderChangeListener(ChangeListener l) {
        slider.removeChangeListener(l);
    }
/*	public Document
    getDocumentView() {
            return myDocument;
    }// getDocumentView*/

} // RaterPanel

