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

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import manyminds.ManyMindsConstants;
import manyminds.application.WindowMenu;
import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.DataListener;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.datamodel.RaterModel;
import manyminds.util.gui.RaterPanel;

public class
AdviceRaterPane
extends JFrame {

    private class
    AdviceServed
    implements DataListener {
        public void
        valueChanged(ChangeEvent ce) {
            SwingUtilities.invokeLater(new Runnable() {
                public void
                run() {
                    String lps = DataContext.getContext().getGlobalVariables().getData("last-page-served").getValue();
                    Data rsa = DataContext.getContext().getSharedData().getData("rater-"+lps);
                    Data doc = DataContext.getContext().getSharedData().getData("comments-"+lps);
                    RaterModel rm = null;
                    String pageName = lps.substring(lps.lastIndexOf("/")+1,lps.length());
                    if ("default.html".equals(pageName)) {
                        pageName = "Top Level Page";
                    }
                    currentAdvice.setText("Comment on: "+pageName);
                    ManyMindsDocument cd = null;
                    if (rsa == null) {
                        rm = (RaterModel)DataContext.getContext().getSharedData().addData("rater-"+lps,RaterModel.instantiateRaterModel("advice-quality"));
                    } else {
                        rm = (RaterModel)rsa;
                    }
                    if (doc == null) {
                        cd = (ManyMindsDocument)DataContext.getContext().getSharedData().addData("comments-"+lps,ManyMindsDocument.newDocument());
                    } else {
                        cd = (ManyMindsDocument)doc;
                    }
                    rm.setLinkedDocumentName("comments-"+lps);
                    rm.setLinkTarget("<give-advice><url>Imogene+Improver/HTML/goals.html</url></give-advice>");
                    adviceRater.setModel(rm);
                    adviceComment.setDocument(cd);
                }
            });
        }
    }


    private JLabel currentAdvice;
    private RaterPanel adviceRater;
    private JTextArea adviceComment;
    
    public
    AdviceRaterPane() {
        super("Advice Rater");

        currentAdvice = new JLabel("foo");
        adviceRater = new RaterPanel(RaterModel.instantiateRaterModel("advice-quality"));
        adviceComment = new JTextArea();
        adviceComment.setBorder(BorderFactory.createLineBorder(ManyMindsConstants.BASE_STROKE));
        DataContext.getContext().getGlobalVariables().addData("last-page-served",ManyMindsDocument.newDocument()).addDataListener(new AdviceServed());

        JPanel cp = new JPanel();
        cp.setLayout(new BorderLayout());
        cp.add(currentAdvice,BorderLayout.NORTH);
        cp.add(adviceComment,BorderLayout.CENTER);
        cp.add(adviceRater,BorderLayout.SOUTH);

        setContentPane(cp);

        JMenuBar jmb = new JMenuBar();
        jmb.add(new WindowMenu());
        setJMenuBar(jmb);
    }
    
    public Collection
    getDocumentNames() {
        return new LinkedList();
    }
    
}