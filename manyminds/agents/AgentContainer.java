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

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import manyminds.ManyMindsConstants;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.helpers.PageDisplayerThread;
import manyminds.util.ManyMindsResolver;
import manyminds.util.gui.RaterPanel;


/**
 * This is the widget that holds the current team and the top (island) button.  It currently
 * isn't being displayed, as it takes up too much space, and we jettisoned the whole
 * team idea for the time being.
 * @author Eric M Eslinger
 */
public class
AgentContainer
extends JPanel
implements ManyMindsConstants {

    private JPanel agentPanel,controlPanel;
    private AdvicePanel advicePanel = new AdvicePanel();
    private JButton portholeButton;
    private static Logger logger = Logger.getLogger("manyminds");


    /**
     * Construtor
     */
    public AgentContainer() {
        controlPanel = new JPanel();
        agentPanel = new JPanel();
        setBackground(BASE_BACKGROUND);
        setForeground(BASE_TEXT);
        agentPanel.setLayout(new FlowLayout());
        controlPanel.setLayout(new GridBagLayout());
        setLayout(new GridBagLayout());
        URL imloc = ManyMindsResolver.resolveClasspathURI(System.getProperty("manyminds.portholeicon"));
        if (imloc != null) {
            try  {
                portholeButton = new JButton("Island",new ImageIcon(imloc));
                portholeButton.setVerticalTextPosition(SwingConstants.BOTTOM);
                portholeButton.setHorizontalTextPosition(SwingConstants.CENTER);
                portholeButton.setHorizontalAlignment(SwingConstants.CENTER);
                portholeButton.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            } catch (Throwable t) {
                portholeButton = new JButton(/*"Show All Agents"*/);
                logger.log(Level.WARNING,"Error loading default porthole icon "+System.getProperty("manyminds.portholeicon"),t);
            }
            portholeButton.addActionListener(new ActionListener() {
                public void
                actionPerformed(ActionEvent ae) {
                    PageDisplayerThread.pageToDisplay(System.getProperty("manyminds.allagentpage"));
                }
            });
        } else {
            logger.log(Level.WARNING,"Unable to find "+System.getProperty("manyminds.portholeicon"));
        }
        DataContext.getContext().getGlobalVariables().addData("last-page-served",ManyMindsDocument.newDocument());
        agentPanel.add(portholeButton);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridheight = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.insets = new Insets(0,0,0,0);
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        agentPanel.setOpaque(true);
        setOpaque(true);
        add(advicePanel,c);
        c.weighty = 1.0;
        add(agentPanel,c);
        c.weighty = 0.0;
    }	

    /**
     * Try to add an agent face to the display.
     * @param a
     */
    public synchronized void addAgent(final AgentFace a) {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                //a.updateBorder(false);
                agentPanel.add(a);
                revalidate();
            }
        });
    }
    
    /**
     * Get the advice preview panel (which is part of this whole thing)
     * @return
     */
    public AdvicePanel
    getAdvicePanel() {
        return advicePanel;
    }
    
    /**
     * 
     * Clear all the advice from the advice preview panel.
     */
    public void
    reset() {
        advicePanel.reset();
    }
    
    /**
     * Add a rater to the advice preview panel.  
     * @param rp
     */
    public void
    addRater(final RaterPanel rp) {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                GridBagConstraints c = new GridBagConstraints();
                c.anchor = GridBagConstraints.NORTHWEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridheight = 1;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.gridx = GridBagConstraints.RELATIVE;
                c.gridy = GridBagConstraints.RELATIVE;
                c.insets = new Insets(0,0,0,0);
                c.ipadx = 0;
                c.ipady = 0;
                c.weightx = 1.0;
                c.weighty = 0.0;
                c.gridwidth = GridBagConstraints.REMAINDER;
                controlPanel.add(rp,c);
            }
        });
    }
    
    /**
     * Remove a rater from the advice preview panel.
     * @param rp
     */
    public void
    removeRater(final RaterPanel rp) {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                controlPanel.remove(rp);
            }
        });
    }
    
    /**
     * Add an agent as the leader.  It doesn't do anything differently from adding
     * an agent as normal right now, but in the old days it mattered.
     * @param a
     */
    public synchronized void addLeader(final AgentFace a) {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                //a.updateBorder(true);
                agentPanel.add(a);
                agentPanel.revalidate();
            }
        });
    }
    
    /**
     * Remove an agent from the panel.
     * @param a
     */
    public synchronized void removeAgent(final AgentFace a) {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                Container jc = a.getParent();
                if (jc != null) {
                    jc.remove(a);
                    agentPanel.revalidate();
                } else {
                    logger.log(Level.WARNING,"Unable to remove "+a.getName()+" from panel!");
                }
            }
        });
    }
}