/*	File ArtifactPage.java
 * =============================================================================
 * 
 * A pane displaying a single page in the ManyMinds Artifact.
 *
 * Author Eric Eslinger
 * Copyright © 1998-2000 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 14 APR 00	EME new today (from artifact page and pane)
 * 
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
 package manyminds.artifact;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import manyminds.ManyMindsConstants;
import manyminds.datamodel.DataContext;
import manyminds.util.gui.ArtifactLabel;
import manyminds.util.gui.ArtifactScrollPane;
import manyminds.util.gui.ArtifactTable;
import manyminds.util.gui.ArtifactTextArea;
import manyminds.util.gui.RaterContainer2;
import manyminds.util.gui.RaterPanel;
import manyminds.util.gui.SuperButton;
import manyminds.util.gui.TextAreaList;

public class
ArtifactPage
extends JPanel
//extends JSplitPane
implements ManyMindsConstants, ArtifactSection {
    private String longTitle = null;
    private String shortTitle = null;
    protected int pageIndex;
    private JPanel topSection;
    private JPanel middleSection;
    private JPanel bottomSection;
    private Map raterBoxes;
    private List ownedData;
    private GridBagConstraints topRightConstraints,topLeftConstraints;
    private GridBagConstraints middleRightConstraints,middleLeftConstraints;
    private GridBagConstraints bottomRightConstraints,bottomLeftConstraints;
    private ArtifactPagePrototype myPrototype;
    private Color mainColor = BASE_BACKGROUND;
    private Color topColor = BASE_BACKGROUND;
    private Color bottomColor = BASE_BACKGROUND;
    private Color unselectedColor = Color.white;
    private Color strokeColor = BASE_STROKE;
    protected static UndoManager undo = new UndoManager();
    protected static UndoAction myUndoAction = new UndoAction();
    protected static RedoAction myRedoAction = new RedoAction();
    
    private static final int secondaryPanelHeight = (int)(FULL_WIDTH_FULL_HEIGHT.height * 0.15);

    private static class
    UndoAction
    extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }
          
        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            myRedoAction.updateRedoState();
        }
          
        public void updateUndoState() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, "Undo");
//                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }      
    }    

    private static class
    RedoAction
    extends AbstractAction {
        public
        RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void
        actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            myUndoAction.updateUndoState();
        }

        public void
        updateRedoState() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, "Redo");
//                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }    

    public static
    Action
    getUndoAction() {
        return myUndoAction;
    }
    
    public static
    Action
    getRedoAction() {
        return myRedoAction;
    }
    
    public
    ArtifactPage (int pageIndex) {
//        super(JSplitPane.VERTICAL_SPLIT,false);
//        setOneTouchExpandable(true);
        this.pageIndex = pageIndex;
        setLayout(new GridBagLayout());
        raterBoxes = new HashMap();
        ownedData = new ArrayList();
        topSection = new JPanel(new GridBagLayout());
        middleSection = new JPanel(new GridBagLayout());
        bottomSection = new JPanel(new GridBagLayout());
       // middleSection.setBorder(BorderFactory.createMatteBorder(RULE_WEIGHT, 0, RULE_WEIGHT, 0, BASE_STROKE));
        topRightConstraints = new GridBagConstraints();
        topRightConstraints.anchor = GridBagConstraints.NORTHWEST;
        topRightConstraints.fill = GridBagConstraints.BOTH;
        topRightConstraints.gridheight = 1;
        topRightConstraints.gridwidth = GridBagConstraints.REMAINDER;
        topRightConstraints.gridx = 1;
        topRightConstraints.gridy = 0;
        topRightConstraints.insets = new Insets(0,0,0,0);
        topRightConstraints.ipadx = 0;
        topRightConstraints.ipady = 0;
        topRightConstraints.weightx = 1.0;
        topRightConstraints.weighty = 1.0;
        topLeftConstraints = new GridBagConstraints();
        topLeftConstraints.anchor = GridBagConstraints.NORTHWEST;
        topLeftConstraints.fill = GridBagConstraints.NONE;
        topLeftConstraints.gridheight = 1;
        topLeftConstraints.gridwidth = 1;
        topLeftConstraints.gridx = 0;
        topLeftConstraints.gridy = 0;
        topLeftConstraints.insets = new Insets(0,0,0,0);
        topLeftConstraints.ipadx = 0;
        topLeftConstraints.ipady = 0;
        topLeftConstraints.weightx = 0.0;
        topLeftConstraints.weighty = 1.0;
        middleRightConstraints = (GridBagConstraints)topRightConstraints.clone();
        bottomRightConstraints = (GridBagConstraints)topRightConstraints.clone();
        middleLeftConstraints = (GridBagConstraints)topLeftConstraints.clone();
        bottomLeftConstraints = (GridBagConstraints)topLeftConstraints.clone();
   /*     
        JSplitPane topTwoPanels = new JSplitPane(JSplitPane.VERTICAL_SPLIT,false,topSection,middleSection) {
            boolean isPainted = false;
            boolean hasProportionalLocation = false;
            double proportionalLocation;
            public void setDividerLocation(double proportionalLocation) {
                if (!isPainted) {
                    hasProportionalLocation = true;
                    this.proportionalLocation = proportionalLocation;
                } else {
                    super.setDividerLocation(proportionalLocation);
                }
            }
            public void paint(Graphics g) {
                if (!isPainted) {
                    if (hasProportionalLocation) {
                        super.setDividerLocation(proportionalLocation);
                    }
                    isPainted = true;
                }
                super.paint(g);
            }
        };
        topTwoPanels.setOneTouchExpandable(true);
        topTwoPanels.setBorder(BorderFactory.createEmptyBorder());*/
     /*   JSplitPane bottomPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,topTwoPanels,bottomSection) {
            boolean isPainted = false;
            boolean hasProportionalLocation = false;
            double proportionalLocation;
            public void setDividerLocation(double proportionalLocation) {
                if (!isPainted) {
                    hasProportionalLocation = true;
                    this.proportionalLocation = proportionalLocation;
                } else {
                    super.setDividerLocation(proportionalLocation);
                }
            }
            public void paint(Graphics g) {
                if (!isPainted) {
                    if (hasProportionalLocation) {
                        super.setDividerLocation(proportionalLocation);
                    }
                    isPainted = true;
                }
                super.paint(g);
            }
        };
        bottomPanel.setOneTouchExpandable(true);*/
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.insets = new Insets(0,0,0,0);
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        
//        topTwoPanels.setResizeWeight(0.0);
        /*bottomPanel.setResizeWeight(1.0);*/
        
      //  add(bottomPanel,c);
     //   topTwoPanels.setDividerLocation(0);
      //  setTopComponent(topTwoPanels);
      //  setBottomComponent(bottomSection);
        
        c.gridwidth = 1;
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        topSection.add(Box.createRigidArea(new Dimension(LEFT_COLUMN_WIDTH,0)),c);
//        middleSection.add(Box.createRigidArea(new Dimension(LEFT_COLUMN_WIDTH,0)),c);
        add(Box.createRigidArea(new Dimension(LEFT_COLUMN_WIDTH,0)),c);
        bottomSection.add(Box.createRigidArea(new Dimension(LEFT_COLUMN_WIDTH,0)),c);
        addComponentListener(new ComponentAdapter() {
            public void
            componentShown(ComponentEvent ce) {
                DataContext.getContext().getGlobalVariables().getData("current-artifact-page-0").setValue(getLongTitle());
            }
        });
    }// ArtifactPage
    

    public void
    setShortTitle(String s) {
        shortTitle = s;
    }

    public void
    setLongTitle(String s) {
        longTitle = s;
    }

    public ArtifactPagePrototype
    getPrototype() {
        return myPrototype;
    }
    
    public void
    setPrototype(ArtifactPagePrototype npp) {
        myPrototype = npp;
    }

    public String
    getTabTitle() {
        if (getPrototype().getGrowable()) {
            return shortTitle + " " + Integer.toString(pageIndex + 1);
        } else {
            return shortTitle;
        }
    }// getTabTitle
    
    public String
    getShortTitle() {
            return shortTitle;
    }// getTabTitle
    
    public String
    getLongTitle() {
        if (getPrototype().getGrowable()) {
            return longTitle + " " + Integer.toString(pageIndex + 1);
        } else {
            return longTitle;
        }
    }// getTabTitle
    
    public void
    renumberPage(int x) {
        pageIndex = x;
    }
    
    public int
    getPageNumber() {
        return pageIndex;
    }
    
    public ArtifactPage
    getSelectedChild() {
        return this;
    }
    
    public boolean
    getRemovable() {
        if (pageIndex >= 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public void
    setTopColor(Color c) {
        topColor = c;
        topSection.setBackground(topColor);
    }
    
    public void
    setMainColor(Color c) {
        mainColor = c;
        middleSection.setBackground(mainColor);
        setBackground(mainColor);
    }
    
    public void
    setUnselectedColor(Color c) {
        unselectedColor = c;
    }
    
    public Color
    getUnselectedColor() {
        return unselectedColor;
    }
    
    public void
    setBottomColor(Color c) {
        bottomColor = c;
        bottomSection.setBackground(bottomColor);
    }
    
    public void
    setStrokeColor(Color c) {
        strokeColor = c;
    }
    
    public Color
    getMainColor() {
        return mainColor;
    }

    public void
    setTopWeight(double w) {
        /*GridBagConstraints tc = ((GridBagLayout)getLayout()).getConstraints(topSection);
        tc.weighty=w;
        ((GridBagLayout)getLayout()).setConstraints(topSection,tc);
        GridBagConstraints bc = ((GridBagLayout)getLayout()).getConstraints(bottomSection);
        int topW = 0;
        int botW = 0;
        if (w > 0.0) {
            topW = RULE_WEIGHT;
        }
        if (bc.weighty > 0.0) {
            botW = RULE_WEIGHT;
        }
        middleSection.setBorder(BorderFactory.createMatteBorder(topW, 0, botW, 0, BASE_STROKE));*/
    }

    public void
    setMiddleWeight(double w) {
      /*  GridBagConstraints tc = ((GridBagLayout)getLayout()).getConstraints(middleSection);
        tc.weighty=w;
        ((GridBagLayout)getLayout()).setConstraints(middleSection,tc);*/
    }

    public void
    setBottomWeight(double w) {
       /* GridBagConstraints tc = ((GridBagLayout)getLayout()).getConstraints(bottomSection);
        tc.weighty=w;
        ((GridBagLayout)getLayout()).setConstraints(bottomSection,tc);
        GridBagConstraints bc = ((GridBagLayout)getLayout()).getConstraints(topSection);
        int topW = 0;
        int botW = 0;
        if (w > 0.0) {
            botW = RULE_WEIGHT;
        }
        if (bc.weighty > 0.0) {
            topW = RULE_WEIGHT;
        }
        middleSection.setBorder(BorderFactory.createMatteBorder(topW, 0, botW, 0, BASE_STROKE));*/
    }
    
    private void
    addUndo(Document d) {
        d.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                //Remember the edit and update the menus
                undo.addEdit(e.getEdit());
                myUndoAction.updateUndoState();
                myRedoAction.updateRedoState();
            }
        });
    }
    
    private void
    processComponent(JComponent comp) {
        if (comp instanceof RaterContainer2) {
            raterBoxes.put(((RaterContainer2)comp).getTag(),comp);
        } else if (comp instanceof JLabel) {
            comp.setForeground(Color.black);
        } else if (comp instanceof JTextComponent) {
            addUndo(((JTextComponent)comp).getDocument());
        }
    }
    
    
    public void
    addToPage(JComponent comp, int height, double weight, String side, String section) {
        processComponent(comp);
        JPanel pageSection = null;
        GridBagConstraints cgbc = null;
        if (section.equals("top-section")) {
            if (side.equals("left-side")) {
                cgbc = topLeftConstraints;
            } else {
                cgbc = topRightConstraints;
            }
            pageSection = topSection;
        } else if (section.equals("middle-section")) {
            if (side.equals("left-side")) {
                cgbc = middleLeftConstraints;
            } else {
                cgbc = middleRightConstraints;
            }
            pageSection = this;
        } else if (section.equals("bottom-section")) {
            if (side.equals("left-side")) {
                cgbc = bottomLeftConstraints;
            } else {
                cgbc = bottomRightConstraints;
            }
            pageSection = bottomSection;
        }
        int fill = GridBagConstraints.NONE;
        if (comp instanceof ArtifactLabel) {
            fill = GridBagConstraints.HORIZONTAL;
/*        } else if (comp instanceof HistoryPanel) {
            comp = HistoryFrame.getHistoryFrame().addHistory((HistoryPanel)comp);
            fill = GridBagConstraints.BOTH;*/
        } else if (comp instanceof RaterContainer2) {
            fill = GridBagConstraints.BOTH;
        } else if (comp instanceof ArtifactTextArea) {
            comp = new ArtifactScrollPane(comp);
            fill = GridBagConstraints.BOTH;
        } else if (comp instanceof TextAreaList) {
            fill = GridBagConstraints.BOTH;
        } else if (comp instanceof SuperButton) {
            fill = GridBagConstraints.NONE;
        } else if (comp instanceof ArtifactTable) {
            comp = new ArtifactScrollPane(comp);
            fill = GridBagConstraints.BOTH;
        } else if (comp instanceof JTextField) {
            fill = GridBagConstraints.BOTH;
        } else {
            fill = GridBagConstraints.HORIZONTAL;
        }
        if (cgbc != null) {
            cgbc.fill = fill;
            cgbc.weighty = weight;
            cgbc.gridheight = height;
            pageSection.add(comp,cgbc);
            cgbc.gridy += height;
        }
    }
    
    public Collection
    getDocumentNames() {
        Set s = new HashSet();
        s.addAll(ownedData);
        return s;
    }
        
    public void
    addOwnedData(String s) {
        ownedData.add(s);
    }
    
    public boolean
    hasRaterBox(String raterBox) {
        Object o = raterBoxes.get(raterBox);
        if (o != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean
    addRater(RaterPanel p, String raterBox) {
        Object o = raterBoxes.get(raterBox);
        if (o != null) {
            ((RaterContainer2)o).add(p);
            return true;
        } else {
            return false;
        }
    }

    public boolean
    removeRater(String raterProtoName, String raterBox) {
        Object o = raterBoxes.get(raterBox);
        if (o != null) {
            return ((RaterContainer2)o).removeRater(raterProtoName);
        } else {
            return false;
        }
    }
    
    public String
    toXML() {
        return "    <page-ref id=\""+longTitle+"\"/>\n";
    }
    
}// ArtifactPage