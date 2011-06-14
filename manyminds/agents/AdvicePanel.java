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
package manyminds.agents;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.datamodel.RaterModel;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.util.gui.RaterPanel;
import manyminds.util.gui.SuperButton;

/**
 * The megawidget that shows a piece of advice an agent wants to give. It has back-forward-delete-show controls
 * for navigating through all the advice given so far, and shows the agent's face and advice synopsis for the
 * current advice. <p>
 * Current design plans call for this to be removed from the ManyMinds core, we're experimenting with better
 * designs for delivering unsolicited advice.
 * 
 * @author Eric Eslinger
 *
 */
public class
AdvicePanel
extends JPanel {
    private LinkedList adviceList = new LinkedList();
    private int currentLocation = -1;
    private String currentURL;
    private boolean showingAdvice = false;
    private JButton forwardButton = new NavButton();
    private JButton backButton = new NavButton();
    private SuperButton moreButton = new NavButton();
    private JButton dismissButton = new NavButton();
    //private WordBalloon myWordBalloon = new WordBalloon();
    
    private JTextArea myText = new JTextArea();
    private JScrollPane myTextScroller = new JScrollPane(myText);
    
    private JLabel myFace = new JLabel();
    private RaterPanel myRaterPanel = new RaterPanel();
    private AttentionAnimator myAttentionAnimator = new AttentionAnimator();
    private static Logger logger = Logger.getLogger("manyminds.advice");
    
    /**
     * We subcass SuperButton to make something that actually "pushes" in, which SuperButton doesn't.
     * 
     * @author Eric M Eslinger
     *
     */
    private class
    NavButton
    extends SuperButton {
        private Border upBorder = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),BorderFactory.createEmptyBorder(2,2,2,2));
        private Border downBorder = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),BorderFactory.createEmptyBorder(2,2,2,2));
        public NavButton() {
            super();
            setupUI();
        }
        protected void
        setupUI() {
            setBorder(upBorder);
            addMouseListener(new MouseAdapter() {
                public void
                mousePressed(MouseEvent e) {
                    setBorder(downBorder);
                }
                public void
                mouseReleased(MouseEvent e) {
                    setBorder(upBorder);
                }
            });
        }
    }

    /**
     * A convenience class for packaging up a piece of advice.
     * 
     * @author eric
     *
     */
    private static class
    AdviceHolder {
        private String myText;
        private String myURL;
        private Color myColor;
        private ImageIcon myImage;
        private String originatorName;
        private RaterModel myRaterModel = null;
        private ManyMindsDocument myDocument = null;
        
        public
        AdviceHolder(String t, String u, String o, Color c, ImageIcon ii) {
            myText = t;
            myURL = u;
            myColor = c;
            myImage = ii;
            originatorName = o;
            
            if ((u != null) && (!u.startsWith("<"))) {
                myURL = "<give-advice><url>"+u+"</url></give-advice>";
                try {
                    u = java.net.URLDecoder.decode((new URL(u)).getPath(),"UTF-8");
                    u = u.substring(1,u.length());
                } catch (Throwable thrown) {}
            } else {
                u = myText;
            }
            
            Data rsa = DataContext.getContext().getSharedData().getData("rater-"+u);
            Data doc = DataContext.getContext().getSharedData().getData("comments-"+u);
            
            if (rsa == null) {
                myRaterModel = (RaterModel)DataContext.getContext().getSharedData().addData("rater-"+u,RaterModel.instantiateRaterModel("advice-quality"));
            } else {
                myRaterModel = (RaterModel)rsa;
            }
            myRaterModel.setLinkTarget("<give-advice><url>Imogene+Improver/HTML/goals.html</url></give-advice>");
            if (doc == null) {
                myDocument = (ManyMindsDocument)DataContext.getContext().getSharedData().addData("comments-"+u,ManyMindsDocument.newDocument());
            } else {
                myDocument = (ManyMindsDocument)doc;
            }
            myRaterModel.setLinkedDocumentName("comments-"+u);
        }
        
        public RaterModel
        getRaterModel() {
            return myRaterModel;
        }
        
        public ManyMindsDocument
        getDocument() {
            return myDocument;
        }
        
        public String
        getText() {
            return myText;
        }
        
        public String
        getOriginator() {
            return originatorName;
        }
        
        public String
        getURL() {
            return myURL;
        }
        
        public Color
        getColor() {
            return myColor;
        }
        
        public ImageIcon
        getImage() {
            return myImage;
        }
        
    }
    
    public
    AdvicePanel() {
        setLayout(new GridBagLayout());
        myFace.setVerticalTextPosition(SwingConstants.BOTTOM);
        myFace.setHorizontalTextPosition(SwingConstants.CENTER);
        myFace.setHorizontalAlignment(SwingConstants.CENTER);
        myFace.setOpaque(true);
        
        myText.setLineWrap(true);
        myText.setWrapStyleWord(true);
        
//        myText.setBorder(BorderFactory.createEmptyBorder());
        moreButton.setText("?");
        dismissButton.setText("X");
        forwardButton.setText(">");
        backButton.setText("<");
        
        moreButton.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                myAttentionAnimator.stopAnimation();
                showMore();
            }
        });
        
        dismissButton.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                myAttentionAnimator.stopAnimation();
                dismissCurrent();
            }
        });

        forwardButton.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                myAttentionAnimator.stopAnimation();
                goForward();
            }
        });
        
        backButton.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                myAttentionAnimator.stopAnimation();
                goBack();
            }
        });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridheight = 4;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(myFace,gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
//        add(myWordBalloon,gbc);
        //myTextScroller.setBorder(new WordBalloonBorder(0.5));
        add(myTextScroller,gbc);

        gbc.weighty = 1.0;
        gbc.weightx = 0.0;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        
        add(moreButton,gbc);
        add(dismissButton,gbc);
        add(backButton,gbc);
        add(forwardButton,gbc);
        verifyButtons();
    }
    
    /**
     * Sets the showing advice in the preview.  Called by the back/forward buttons, as well as when
     * a new piece of advice shows up.
     * @param ah the Advice to add.
     */
    private void
    setAdvice(final AdviceHolder ah) {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                if (ah != null) {
                    myText.setText(ah.getText());
//                    myWordBalloon.setText(ah.getText());
                    moreButton.setCommand(ah.getURL());
                    currentURL = ah.getURL();
                    myFace.setIcon(ah.getImage());
                    myFace.setText(ah.getOriginator());
                    myFace.setBackground(ah.getColor());
                    myRaterPanel.setModel(ah.getRaterModel());
                    showingAdvice = true;
                    revalidate();
                } else {
                    showingAdvice = false;
                    revalidate();
                }
            }
        });
    }
    
    
    /**
     * This class handles doing something interesting to the advice preview when 
     * the system wants the user's advice.  Currently it doesn't do anything.
     * 
     * @author eric
     *
     */
    public class
    AttentionAnimator
    implements ActionListener {
        int animFrame = 0;
        javax.swing.Timer timer;
        public void
        actionPerformed(ActionEvent ae) {
            if (isShowing()) {
                try {
//                    myText.setBackground(myText.getBackground().darker());
//                    myText.setBorder(BorderFactory.createLineBorder(Color.red,10-animFrame));
                    ++animFrame;
                    if (animFrame == 10) {
                        animFrame = 0;
                        stopAnimation();
//                        myText.setBackground(ManyMindsConstants.TEXT_BACKGROUND);
                    }
                } catch (Throwable t) {}
            }
        }
        
        public
        AttentionAnimator() {
            timer = new javax.swing.Timer(500, this);
            timer.setCoalesce(true);
            timer.setRepeats(true);
        }
            
        public synchronized void startAnimation() {
//            myText.setBorder(BorderFactory.createCompoundBorder(myText.getBorder(),BorderFactory.createLineBorder(Color.red,11)));
//            timer.start();
        }

        public synchronized void stopAnimation() {
            timer.stop();
            animFrame = 0;
            //myText.setBorder(BorderFactory.createCompoundBorder(myText.getBorder(),BorderFactory.createEmptyBorder()));
        }
        
    }
    
    /**
     * This is what the outside world calls when a new piece of advice should be added to the preview.
     * @param comment The synopsis of the advice
     * @param url The advice's URL (usually a local one)
     * @param o The originating agent's name (displayed below it's icon)
     * @param c The background color for the advice preview
     * @param face An icon (usually the originating agent's face icon)
     */
    public void
    addAdvice(String comment, String url, String o, Color c, ImageIcon face) {
        Object[] parms = {comment,url};
        if (url == null) {
            parms[1] = "";
        }
        logger.log(Level.INFO,"Adding advice",parms);
        AdviceHolder ah = new AdviceHolder(comment,url,o,c,face);
        synchronized (adviceList) {
            adviceList.add(ah);
            currentLocation = adviceList.size() - 1;
            setAdvice(ah);
            myAttentionAnimator.startAnimation();
            verifyButtons();
        }
    }
    
    /**
     * Make sure the appropriate buttons are enabled (can't go back past the beginnning, for example)
     */
    private void
    verifyButtons() {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                synchronized (adviceList) {
                    if (currentLocation > 0) {
                        backButton.setEnabled(true);
                    } else {
                        backButton.setEnabled(false);
                    }
                    if ((currentLocation >= 0 ) && (currentLocation < (adviceList.size() - 1))) {
                        forwardButton.setEnabled(true);
                    } else {
                        forwardButton.setEnabled(false);
                    }
                    if (currentURL != null) {
                        moreButton.setEnabled(true);
                    } else {
                        moreButton.setEnabled(false);
                    }
                }
            }
        });
    }
    
    /**
     * This actually just logs whatever the current URL is when more is pressed.  The more button is a 
     * SuperButton and handles advice dislay in the setCommand code.
     */
    private void
    showMore() {
        if (currentURL != null) {
            Object[] parms = {currentURL};
            logger.log(Level.INFO,"Reading advice",parms);
        }
    }
    
    /**
     * Gets rid of all the advice.
     */
    public void
    reset() {
        clearPanel();
    }
        
    
    /**
     * Dismisses the current advice.  Called when the dismissButton is pressed.
     */
    private void
    dismissCurrent() {
        AdviceHolder ah = null;
//        Object[] parms = {myWordBalloon.getText(),currentURL};
        Object[] parms = {myText.getText(),currentURL};
        if (currentURL == null) {
            parms[1] = "";
        }
        logger.log(Level.INFO,"Dismissed advice",parms);
        synchronized (adviceList) {
            if ((currentLocation > 0) && (currentLocation < (adviceList.size() - 1))) {
                ah = (AdviceHolder)adviceList.get(currentLocation - 1);
                adviceList.remove(currentLocation--);
            } else if ((currentLocation == 0) && (adviceList.size() > 1)) {
                ah = (AdviceHolder)adviceList.get(1);
                adviceList.remove(0);
            } else if ((adviceList.size() > 1) && (currentLocation == (adviceList.size() - 1))) {
                ah = (AdviceHolder)adviceList.get(adviceList.size() - 2);
                adviceList.remove(adviceList.size() - 1);
            } else {
                clearPanel();
            }
        }
        if (ah != null) {
            setAdvice(ah);
        }
        verifyButtons();
    }

    /**
     * Handle the back button being pressed.
     */
    private void
    goBack() {
        synchronized (adviceList) {
            if (currentLocation > 0) {
                AdviceHolder ah = (AdviceHolder)adviceList.get(--currentLocation);
                setAdvice(ah);
                verifyButtons();
            }
        }
    }
    
    /**
     * Handle the forward button.
     */
    private void
    goForward() {
        synchronized (adviceList) {
            if (currentLocation < (adviceList.size() - 1)) {
                AdviceHolder ah = (AdviceHolder)adviceList.get(++currentLocation);
                setAdvice(ah);
                verifyButtons();
            }
        }
    }
    
    /**
     * We override getPreferredSize() to return 0,0 if there are no advice pieces to show (so it hides).
     * 
     *  @see java.awt.Component#getPreferredSize()
     */
    public Dimension
    getPreferredSize() {
        if (showingAdvice) {
            return super.getPreferredSize();
        } else {
            return new Dimension(0,0);
        }
    }
    
    /**
     * Gets rid of all the advice.
     */
    private void
    clearPanel() {
        synchronized (adviceList) {
            currentLocation = -1;
            adviceList.clear();
            setAdvice(null);
            verifyButtons();
        }
    }
}