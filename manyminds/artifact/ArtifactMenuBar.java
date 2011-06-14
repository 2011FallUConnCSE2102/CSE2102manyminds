/*	File ArtifactMenuBar.java
 * =============================================================================
 * 
 * The menubar to be added to the ManyMinds applet.
 *
 * Author Eric Eslinger
 * Copyright © 2000 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 15 Mar 00	EME	Created
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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import manyminds.ManyMindsConstants;
import manyminds.application.ApplicationContext;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.ManyMindsDocument;

public class ArtifactMenuBar
extends JMenuBar
implements ManyMindsConstants/*, ChangeListener*/ {
    private JMenu fileMenu = new JMenu("File");
    private JMenu editMenu = new JMenu("Edit");
    private JMenu specialMenu = new JMenu("Special");
    private JMenu addMenu = new JMenu("Add");
    private JMenu removeMenu = new JMenu("Remove");
    private JMenu helpModeMenu = new JMenu("Help Mode");
    private List menuItems;
    
    private class StateAction
    extends AbstractAction {
        private String myText;
        public
        StateAction(String name, String text) {
            super(name);
            myText = text;
        }
        public void
        actionPerformed(ActionEvent ae) {
            DataContext.getContext().getSharedData().getData("user-state-0").setValue(myText);
        }
    }
        
    
    public
    ArtifactMenuBar() {
        super();
        
        menuItems = new LinkedList();

        JMenuItem reportItem = new JMenuItem(new ReportAction());
        reportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        specialMenu.add(reportItem);
        

/*        JCheckBoxMenuItem spellCheckItem = new JCheckBoxMenuItem("Check Spelling");
        //spellCheckItem.setState(false);
        spellCheckItem.addItemListener(new SpellcheckAction());*/
       /* specialMenu.add(spellCheckItem);*/

        JMenuItem quitItem = new JMenuItem(new QuitAction());
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fileMenu.add(quitItem);

        JMenuItem undoItem = new JMenuItem(ArtifactPage.getUndoAction());
        undoItem.setText("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JMenuItem redoItem = new JMenuItem(ArtifactPage.getRedoAction());
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        redoItem.setText("Redo");

        JMenuItem copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyItem.setText("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JMenuItem cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
        cutItem.setText("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JMenuItem pasteItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteItem.setText("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.add(copyItem);
        editMenu.add(cutItem);
        editMenu.add(pasteItem);

        JRadioButtonMenuItem aloneRadio = new JRadioButtonMenuItem(new StateAction("Leave me alone","assessing"));
        JRadioButtonMenuItem normalRadio = new JRadioButtonMenuItem(new StateAction("Whatever","normal"));
        JRadioButtonMenuItem helpRadio = new JRadioButtonMenuItem(new StateAction("Help me please","help"));
        
        normalRadio.setSelected(true);
        
        ButtonGroup helpModeGroup = new ButtonGroup();
        helpModeGroup.add(aloneRadio);
        helpModeGroup.add(normalRadio);
        helpModeGroup.add(helpRadio);

        helpModeMenu.add(aloneRadio);
        helpModeMenu.add(normalRadio);
        helpModeMenu.add(helpRadio);

        //resetAddRemoveActions();
        if (!System.getProperty("os.name").startsWith("Mac OS X")) {
            add(fileMenu);
        }
        add(editMenu);
        /*add(addMenu);
        add(removeMenu);*/
        add(helpModeMenu);
        add(specialMenu);
    }
    
    public void
    setLocationText (String s) {
        //location.setText(s);
    }
    
    public void
    resetAddRemoveActions() {
        menuItems.clear();
        addMenu.removeAll();
        removeMenu.removeAll();
        for (int x = 0; x < ((Artifact)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID)).getTabCount(); ++x) {
            SectionTabbedPane stp = (SectionTabbedPane)((Artifact)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID)).getComponentAt(x);
            Iterator it = stp.getGrowables().iterator();
            String title = stp.getShortTitle();
            while (it.hasNext()) {
                ArtifactPagePrototype npp = (ArtifactPagePrototype)it.next();
                AddAction newAdd = new AddAction((ManyMindsDocument)DataContext.getContext().getSharedData().getData(npp.getLongTitle()+" IN "+title),npp.getShortTitle());
                RemoveAction newRemove = new RemoveAction((ManyMindsDocument)DataContext.getContext().getSharedData().getData(npp.getLongTitle()+" IN "+title),npp.getShortTitle());
                //RemoveAction newRemove = new RemoveAction((SectionTabbedPane)rn.getComponentAt(x));
                addMenu.add(newAdd);
                removeMenu.add(newRemove);
                menuItems.add(newAdd);
                //menuItems.add(newRemove);
            }
        }
    }
    
	
}