/*	File SectionTabbedPane.java
 * =============================================================================
 * 
 * A page in the ManyMinds Artifact that displays an overview of
 * its contents.
 * 
 * Author Eric Eslinger
 * Copyright © 1998-2000 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 14 APR 00	EME	Broke free from OverviewPane.java.
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

//import java.awt.*;
import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import manyminds.util.gui.ArtifactTabbedPane;

public class
SectionTabbedPane extends ArtifactTabbedPane implements ArtifactSection {
    private String shortTitle = null;
    private Set growables = new HashSet();
    private Color mainColor = BASE_BACKGROUND;
    private Color unselectedColor = BASE_BACKGROUND;
    
    public
    SectionTabbedPane(Color m, Color u) {
        setUI(new manyminds.util.gui.mclaf.MultiColorTabbedPaneUI());
        mainColor = m;
        unselectedColor = u;
    }
    
    public String
    getLongTitle() {
        return ((ArtifactPage)getSelectedComponent()).getLongTitle();
    }
    
    public String
    getTabTitle() {
        return shortTitle;
    }

    public void
    setShortTitle(String s) {
        shortTitle = s;
    }
    
    public String
    getShortTitle() {
        return shortTitle;
    }// getTabTitle
    
    public Color
    getUnselectedColor() {
        return unselectedColor;
    }
    
    public void
    addPage(ArtifactPage np) {
        addTab(np.getTabTitle(),np);
        setBackgroundAt(indexOfComponent(np),getUnselectedColor());
        setMainColor(np.getMainColor());
        ArtifactPagePrototype npp = np.getPrototype();
        if (npp.getGrowable()) {
            growables.add(npp);
        }
    }
    
    public void
    removePage(String prototypeName) {
        int lastFound = -1;
        int foundCount = -1;
        for (int i = 0; i < getTabCount(); ++i) {
            if (((ArtifactPage)getComponentAt(i)).getPrototype().getLongTitle().equals(prototypeName)) {
                lastFound = i;
                ++foundCount;
            }
        }
        if (lastFound != -1) {
            remove(lastFound);
            ArtifactPagePrototype.getPrototype(prototypeName).remove(foundCount);
        }
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
    
    public Color
    getMainColor() {
        return mainColor;
    }
    
    public void
    setMainColor(Color c) {
        mainColor = c;
        setBackground(mainColor);
    }
    
    public Collection
    getGrowables() {
        return Collections.unmodifiableCollection(growables);
    }
        
    public ArtifactPage
    getSelectedChild() {
        return ((ArtifactSection)getSelectedComponent()).getSelectedChild();
    }
    
    public boolean
    currentPageRemovable() {
        ArtifactPage np = getSelectedChild();
        if (np.getPrototype().getGrowable()) {
            return true;
        }
        return false;
    }
    
    public void
    removeCurrentPage() {
        /*if (currentPageRemovable()) {
            ArtifactPage cp = (ArtifactPage)getSelectedComponent();
            //cp.removeSelf();
            remove(cp);
            Iterator it = detailList.iterator();
            while (it.hasNext()) {
                cp = (ArtifactPage)it.next();
                setTitleAt(indexOfComponent(cp), cp.getTabTitle());
            }
        }*/
    }
    
    public String
    toXML() {
        StringBuffer ret_val = new StringBuffer("<section short-name=\"");
        ret_val.append(shortTitle);
        ret_val.append("\">\n");
        for (int x = 0; x < getTabCount(); ++x) {
            ret_val.append(((ArtifactSection)getComponentAt(x)).toXML());
        }
        ret_val.append("</section>\n");
        return ret_val.toString();
    }
}//SectionTabbedPane