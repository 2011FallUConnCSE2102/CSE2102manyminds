/*	File Artifact.java
 * =============================================================================
 * 
 * The tabbed pane displaying the pages in the ManyMinds Artifact.
 * 
 * Author Eric eslinger
 * Copyright © 1999-2001 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 05 Nov 99	CSS	New today (from ArtifactTabbedPane.java).
 * 10 Dec 99	CSS	Marked all instance variables as private and added a
 * 							getResearchNotes() method.
 * 17 Mar 01	EME	Adding all the load/save functionality
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

//import java.awt.*;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;

import manyminds.ManyMindsConstants;
import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.DataListener;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.util.XMLFactory;
import manyminds.util.gui.ArtifactTabbedPane;

public class
Artifact
extends ArtifactTabbedPane
implements ManyMindsConstants {

    private class
    PageGrowthListener
    implements DataListener {
        private int currentCount = 0;
        private String myLayout;
        private SectionTabbedPane mySection;
        
        public
        PageGrowthListener(String layout, SectionTabbedPane section, String initialValue) {
            myLayout = layout;
            mySection = section;
            valueChanged(XMLFactory.parseInt(initialValue));
        }
        
        public void
        valueChanged(ChangeEvent ce) {
            valueChanged(XMLFactory.parseInt(((Data)ce.getSource()).getValue()));
        }
        
        private void
        valueChanged(int i) {
            if (i > currentCount) {
                for ( ; currentCount < i ; ++currentCount) {
                    mySection.addPage(ArtifactPagePrototype.getPrototype(myLayout).instantiate());
                }
            } else if (i < currentCount) {
                for ( ; currentCount > i ; --currentCount) {
                    mySection.removePage(myLayout);
                }
            }
        }
    }

    private boolean myLoading = true;
    private List sectionList = new LinkedList();
    private List weightList = new LinkedList();
    /**
        * creates the tabbed pane displaying the pages in the ManyMinds Artifact
        */
    public
    Artifact() 
    {
        /* Call the ArtifactTabbedPane constructor
        */
        super();
        setUI(new manyminds.util.gui.mclaf.MultiColorTabbedPaneUI());
        DataContext.getContext().getGlobalVariables().addData("current-artifact-page-0",ManyMindsDocument.newDocument()).setValue("");
        DataContext.getContext().getGlobalVariables().addData("current-artifact-section-0",ManyMindsDocument.newDocument()).setValue("");
    }

    public void
    addSection(final ArtifactSection ns, int weight) {
        int insertLocation = weightList.size();
        for (int i = 0; i < weightList.size(); ++i) {
            if (((Integer)weightList.get(i)).intValue() >= weight) {
                insertLocation = i; 
                i = weightList.size();
            }
        }
        
        sectionList.add(insertLocation,ns);
        weightList.add(insertLocation,new Integer(weight));
        
        if (ns instanceof SectionTabbedPane) {
            insertTab(ns.getTabTitle(),null,(SectionTabbedPane)ns,null,insertLocation);
            ((SectionTabbedPane)ns).addComponentListener(new ComponentAdapter() {
                public void
                componentShown(ComponentEvent ce) {
                    DataContext.getContext().getGlobalVariables().getData("current-artifact-section-0").setValue(ns.getShortTitle());
                }
            });
        } else if (ns instanceof ArtifactPage) {
            insertTab(ns.getTabTitle(),null,(ArtifactPage)ns,null,insertLocation);
            ((ArtifactPage)ns).addComponentListener(new ComponentAdapter() {
                public void
                componentShown(ComponentEvent ce) {
                    DataContext.getContext().getGlobalVariables().getData("current-artifact-section-0").setValue(ns.getShortTitle());
                    DataContext.getContext().getGlobalVariables().getData("current-artifact-section-0").setValue(ns.getLongTitle());
                }
            });
        }
        setBackgroundAt(indexOfComponent((Component)ns),ns.getMainColor());
    }
    
    public void
    addPagesToSection(String layout, String section) {
        Data pd = DataContext.getContext().getSharedData().addData(layout+" IN "+section,ManyMindsDocument.newDocument("0"));
        SectionTabbedPane stp = (SectionTabbedPane)getComponentAt(indexOfTab(section));
        pd.addDataListener(new PageGrowthListener(layout,stp,pd.getValue()));
    }
    
    public ArtifactSection
    removeSectionAt(int i) {
        Object o = null;
        if (i < getTabCount()) {
            o = sectionList.remove(i);
            removeTabAt(i);
        }
        return (ArtifactSection)o;
    }
    
    public void
    removeAllSections() {
        sectionList.clear();
        removeAll();
    }

    public void
    addRater(String proto, String loc, int index) {
        ArtifactPagePrototype.Rater re = new ArtifactPagePrototype.Rater();
        re.setDocumentTitle(proto);
        re.setToolTip(loc);
        re.setIndex(index);
        ArtifactPagePrototype.addRaterToPrototypes(re);
    }
    
    public Collection
    getDocumentNames() {
        Set s = new HashSet();
        for (int i = 0; i < getTabCount(); ++i) {
            if (getComponentAt(i) instanceof ArtifactSection) {
                s.addAll(((ArtifactSection)getComponentAt(i)).getDocumentNames());
            }
        }
        return s;
    }
    
    public void
    removeRater(String proto, String loc) {
        ArtifactPagePrototype.Rater re = new ArtifactPagePrototype.Rater();
        re.setDocumentTitle(proto);
        re.setToolTip(loc);
    }
    
    public void
    setLoading(boolean l) {
        myLoading = l;
        synchronized (this) {
            if (!myLoading) {
                notifyAll();
            } else {
                removeAll();
            }
        }
    }
    
    public boolean
    isLoading() {
        return myLoading;
    }

    public String
    toXML() {

        return "";
    }
    
    public void
    fromXML() {}
    
} // Artifact
