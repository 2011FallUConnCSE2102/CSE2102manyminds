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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import manyminds.ManyMindsConstants;
import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.DataList;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.datamodel.RaterModel;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.util.ManyMindsResolver;
import manyminds.util.StringExploder;
import manyminds.util.gui.ArtifactLabel;
import manyminds.util.gui.ArtifactTextArea;
import manyminds.util.gui.RaterContainer2;
import manyminds.util.gui.RaterPanel;
import manyminds.util.gui.SuperButton;
import manyminds.util.gui.TextAreaList;

public class
ArtifactPagePrototype
implements ManyMindsConstants {

    private static Map allPrototypes = Collections.synchronizedMap(new HashMap());
    private static List allAddedRaters = Collections.synchronizedList(new LinkedList());
    private static Logger logger = Logger.getLogger("manyminds");
    private static final ImageIcon LINK_ICON = new ImageIcon(ManyMindsResolver.resolveClasspathURI("classpath://manyminds/resources/images/URLAnchor.gif"));

    public abstract static class
    Element {
        private double weight;
        private String toolTip;
        private int height;
        private int index;
        private String elementSide;
        private String elementSection;
        private String documentTitle;
    
        public
        Element() {
            weight = 0.0;
            toolTip = null;
            height = 1;
            index = -1;
            elementSide = "left-side";
            documentTitle = "";
        }
        
        public
        Element(double w, String tt, int h, int s, String d, String lorr, String sect) {
            weight = w;
            toolTip = tt;
            height = h;
            index = s;
            documentTitle = d;
            elementSide = lorr;
            elementSection = sect;
        }
    
        public double
        getWeight() {
            return weight;
        }
        
        public String
        getToolTip() {
            return toolTip;
        }
        
        public int
        getHeight() {
            return height;
        }
        
        public int
        getIndex() {
            return index;
        }
        
        public String
        getDocumentTitle() {
            return documentTitle;
        }
        
        public void
        setWeight(double w) {
            weight = w;
        }
        
        public void
        setToolTip(String tt) {
            toolTip = tt;
        }
        
        public void
        setHeight(int h) {
            height = h;
        }
        
        public void
        setIndex(int s) {
            index = s;
        }
        
        public void
        setSide(String s) {
            elementSide = s;
        }
        
        public String
        getSide() {
            return elementSide;
        }
        
        public void
        setSection(String s) {
            elementSection = s;
        }
        
        public String
        getSection() {
            return elementSection;
        }
        
        public void
        setDocumentTitle(String s) {
            documentTitle = s;
        }
        
        public boolean
        equals(Object o) {
            if (o instanceof Element) {
                Element a = (Element)o;
                if ((a.weight == weight) && (a.toolTip == toolTip)
                    && (a.height == height) && (a.index == index) && (a.elementSide == elementSide)
                    && (a.documentTitle == documentTitle)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public static class
    Label
    extends Element {
        private String linkTarget = null;
        public String
        getLinkTarget() {
            return linkTarget;
        }
        
        public void
        setLinkTarget(String s) {
            linkTarget = s;
        }
    }
    
    public static class
    TextArea
    extends Element {}
    
    public static class
    TextField
    extends Element {}
    
    public static class
    Table
    extends Element {}
    
    public static class
    AgentFace
    extends Element {
        private String linkTarget = null;
        public String
        getLinkTarget() {
            return linkTarget;
        }
        
        public void
        setLinkTarget(String s) {
            linkTarget = s;
        }
    }
    
    public static class
    History
    extends Element {
        private Map globalNames = new HashMap();
        public void
        addGlobal(String tag, String desc) {
            globalNames.put(tag,desc);
        }
        
        public Map
        getMappings() {
            return globalNames;
        }
    }
    
    public static class
    RaterBox
    extends Element {}
    
    public static class
    Rater //don't tell anyone, but I will store the raterbox name in the tooltip field
    extends Element {
        private String linkTarget = null;
        public String
        getLinkTarget() {
            return linkTarget;
        }
        
        public void
        setLinkTarget(String s) {
            linkTarget = s;
        }
    
    }
        
    private static final String TOP = "top";
    private static final String MIDDLE = "middle";
    private static final String BOTTOM = "bottom";
    private static final String RATER = "rater";
    
    private String myLongTitle = null;
    private String myShortTitle = null;
    private double topWeight = 0.0;
    private double middleWeight = 0.0;
    private double bottomWeight = 0.0;
    private List myItems;
    private List instantiatedPages;
    private HashMap dataLists;
    private boolean growable = false;
    private Color mainColor = BASE_BACKGROUND;
    private Color topColor = BASE_BACKGROUND;
    private Color unselectedColor = BASE_BACKGROUND;
    private Color bottomColor = BASE_BACKGROUND;

    public
    ArtifactPagePrototype() {
        myItems = new LinkedList();
        instantiatedPages = new LinkedList();
        dataLists = new HashMap();
    }
    
    public static
    ArtifactPagePrototype
    getPrototype(String s) {
        return (ArtifactPagePrototype)allPrototypes.get(s);
    }
    
    public void
    setTopWeight(double w) {
        topWeight = w;
    }

    public void
    setMiddleWeight(double w) {
        middleWeight = w;
    }

    public void
    setBottomWeight(double w) {
        bottomWeight = w;
    }
    
    public void
    setShortTitle(String s) {
        myShortTitle = s;
    }
    
    public void
    setLongTitle(String s) {
        myLongTitle = s;
        allPrototypes.put(s,this);
    }

    public String
    getShortTitle() {
        return myShortTitle;
    }
    
    public String
    getLongTitle() {
        return myLongTitle;
    }
    
    public void
    setGrowable(boolean b) {
        growable = b;
    }
    
    public boolean
    getGrowable() {
        return growable;
    }
    
    public void
    setTopColor(Color c) {
        topColor = c;
        Iterator it = instantiatedPages.iterator();
        while (it.hasNext()) {
            ((ArtifactPage)it.next()).setTopColor(c);
        }
    }
    
    public void
    setMainColor(Color c) {
        mainColor = c;
        Iterator it = instantiatedPages.iterator();
        while (it.hasNext()) {
            ((ArtifactPage)it.next()).setMainColor(c);
        }
    }
    
    public void
    setUnselectedColor(Color c) {
        unselectedColor = c;
        Iterator it = instantiatedPages.iterator();
        while (it.hasNext()) {
            ((ArtifactPage)it.next()).setUnselectedColor(c);
        }
    }
    
    public void
    setBottomColor(Color c) {
        bottomColor = c;
        Iterator it = instantiatedPages.iterator();
        while (it.hasNext()) {
            ((ArtifactPage)it.next()).setBottomColor(c);
        }
    }
    
    public Color
    getMainColor() {
        return mainColor;
    }
    
    public synchronized void
    addElement(ArtifactPagePrototype.Element o) {
        myItems.add(o);
        Iterator it = instantiatedPages.iterator();
        while (it.hasNext()) {
            addItemToPage(o,(ArtifactPage)it.next());
        }
    }
    
    public void
    addRater(ArtifactPagePrototype.Element o) {
        Iterator it = instantiatedPages.iterator();
        while (it.hasNext()) {
            ArtifactPage np = (ArtifactPage)it.next();
            if (np.hasRaterBox(o.getToolTip())) {
                addItemToPage(o,np);
            }
        }
    }
    
    private void
    addItemToPage(ArtifactPagePrototype.Element o, ArtifactPage np) {
        int pageIndex = np.getPageNumber();
        JComponent jc = null;
        if (o instanceof Label) {
            if (((Label)o).getLinkTarget() == null) {
                jc = new ArtifactLabel(o.getDocumentTitle());
            } else {
                jc = new JPanel();
                jc.setOpaque(false);
                String linkTarget = ((Label)o).getLinkTarget();
                if (!linkTarget.startsWith("<")) {
                    linkTarget = "<give-advice><url>"+linkTarget+"</url></give-advice>";
                }
                jc.setLayout(new BoxLayout(jc,BoxLayout.X_AXIS));
                jc.add(new ArtifactLabel(o.getDocumentTitle()));
                SuperButton linkLabel = new SuperButton(LINK_ICON,linkTarget);
                linkLabel.setBorder(BorderFactory.createEmptyBorder());
                linkLabel.setBackground(np.getMainColor());
                linkLabel.setOpaque(false);
                //jc.add(Box.createHorizontalGlue());
                jc.add(Box.createHorizontalStrut(20));
                jc.add(linkLabel);
            }
        } else if (o instanceof ArtifactPagePrototype.AgentFace) {
            try {
                String iconURL = o.getDocumentTitle();
                String linkURL = ((ArtifactPagePrototype.AgentFace)o).getLinkTarget();
                linkURL = "<give-advice><url>"+linkURL+"</url></give-advice>";
                ImageIcon agentIcon = new ImageIcon(ManyMindsResolver.resolveClasspathURI(java.net.URLDecoder.decode(iconURL,"UTF-8")));
                jc = new SuperButton(o.getToolTip(),agentIcon,linkURL);
                ((JButton)jc).setVerticalTextPosition(SwingConstants.BOTTOM);
                ((JButton)jc).setHorizontalTextPosition(SwingConstants.CENTER);
                ((JButton)jc).setHorizontalAlignment(SwingConstants.CENTER);
                ((JButton)jc).setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
                jc.setToolTipText(o.getToolTip());
                jc.setOpaque(false);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (o instanceof ArtifactPagePrototype.TextArea) {
            int index = o.getIndex();
            if (index == -1) {
                index = pageIndex;
            }
            ManyMindsDocument dataList = ManyMindsDocument.newDocument();
            String documentTitle = o.getDocumentTitle() + "-" + Integer.toHexString(index);
            ManyMindsDocument d = (ManyMindsDocument)DataContext.getContext().getSharedData()
                    .addData(documentTitle,ManyMindsDocument.newDocument());
            DataContext.getContext().getSharedData().updateLists(documentTitle);
            jc = new ArtifactTextArea(d);
            np.addOwnedData(documentTitle);
            np.addOwnedData(o.getDocumentTitle()+"-group");
        } else if (o instanceof ArtifactPagePrototype.TextField) {
            int index = o.getIndex();
            if (index == -1) {
                index = pageIndex;
            }
            ManyMindsDocument dataList = ManyMindsDocument.newDocument();
            String documentTitle = o.getDocumentTitle() + "-" + Integer.toHexString(index);
            ManyMindsDocument d = (ManyMindsDocument)DataContext.getContext().getSharedData()
                    .addData(documentTitle,ManyMindsDocument.newDocument());
            DataContext.getContext().getSharedData().updateLists(documentTitle);
            jc = new JTextField(d,null,0);
            np.addOwnedData(documentTitle);
            np.addOwnedData(o.getDocumentTitle()+"-group");
        } else if (o instanceof ArtifactPagePrototype.Table) {
           /* jc = new ArtifactTable();
            ((ArtifactTable)jc).setModel(DataContext.getContext().getSharedData().getTableModel(o.getDocumentTitle()+"-group"));*/
            np.addOwnedData(o.getDocumentTitle()+"-group");
            DataList lm = new DataList(o.getDocumentTitle()+"-group",DataContext.getContext().getSharedData());
            jc = new TextAreaList(lm);
        } else if (o instanceof ArtifactPagePrototype.RaterBox) {
            jc = new RaterContainer2(o.getDocumentTitle());
/*        } else if (o instanceof ArtifactPagePrototype.History) {
            int index = o.getIndex();
            if (index == -1) {
                index = pageIndex;
            }
            String documentTitle = o.getDocumentTitle() + "-" + Integer.toHexString(index);
            jc = new HistoryPanel(documentTitle);
            Iterator it = ((ArtifactPagePrototype.History)o).getMappings().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry)it.next();
                String tag = me.getKey().toString();
                String desc = me.getValue().toString();
                int dashLoc = tag.lastIndexOf('-');
                if (dashLoc != -1) {
                    DataList newList = null;
                    String gName = tag.substring(0,dashLoc);
                    String gInd = tag.substring(dashLoc+1);
                    if (!gInd.equals("group")) {
                        try {
                            Integer.valueOf(gInd,16);
                        } catch (NumberFormatException nfe) {
                            tag = tag+"-"+Integer.toHexString(pageIndex);
                        }
                    }
                } else {
                    tag = tag+"-"+Integer.toHexString(pageIndex);
                }
                ((HistoryPanel)jc).watchGlobal(tag,desc);
            }
            np.addOwnedData(documentTitle);*/
        } else if (o instanceof ArtifactPagePrototype.Rater) {
            try {
                if (np.hasRaterBox(o.getToolTip())) {
                    int index = o.getIndex();
                    String documentTitle;
                    RaterModel rm = null;
                    if (index == -1) {
                        index = pageIndex;
                        documentTitle = o.getDocumentTitle() + "-" + Integer.toHexString(index);
                        rm = (RaterModel)DataContext.getContext().getSharedData()
                            .addData(documentTitle,RaterModel.instantiateRaterModel(o.getDocumentTitle()));
                    } else if (index == -2) {
                        documentTitle = o.getDocumentTitle();
                        rm = (RaterModel)DataContext.getContext().getSharedData().getData(documentTitle);
                        if (rm == null) {
                            rm = RaterModel.instantiateRaterModel(o.getDocumentTitle().substring(0,o.getDocumentTitle().lastIndexOf("-")));
                            rm = (RaterModel)DataContext.getContext().getSharedData().addData(o.getDocumentTitle(),rm);
                        }
                    } else {
                        documentTitle = o.getDocumentTitle() + "-" + Integer.toHexString(index);
                        rm = (RaterModel)DataContext.getContext().getSharedData()
                            .addData(documentTitle,RaterModel.instantiateRaterModel(o.getDocumentTitle()));
                    }
                    ManyMindsDocument cd = (ManyMindsDocument)DataContext.getContext().getSharedData().addData("comments-"+documentTitle,ManyMindsDocument.newDocument());
                    DataContext.getContext().getSharedData().updateLists(documentTitle);
                    DataContext.getContext().getSharedData().updateLists("comments-"+documentTitle);
                    rm.setLinkedDocumentName("comments-"+documentTitle);
                    //rm.setLinkTarget(((ArtifactPagePrototype.Rater)o).getLinkTarget());
                    RaterPanel rp = new RaterPanel(rm);
                    np.addRater(rp,o.getToolTip());
                    np.addOwnedData(documentTitle);
                    np.addOwnedData("comments-"+documentTitle);
                    if (index != -2) {
                        np.addOwnedData(o.getDocumentTitle()+"-group");
                        np.addOwnedData("comments-"+o.getDocumentTitle()+"-group");
                    } else {
                        np.addOwnedData(o.getDocumentTitle().substring(0,o.getDocumentTitle().lastIndexOf("-")) + "-group");
                        np.addOwnedData("comments-"+o.getDocumentTitle().substring(0,o.getDocumentTitle().lastIndexOf("-")) + "-group");
                    }
                    np.revalidate();
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING,"Error adding rater "+o.getDocumentTitle()+" to artifact page prototype "+o.getToolTip(),t);
            } finally {
                return;
            }
        } else {
            logger.log(Level.INFO,"Bad element for artifact page prototype");
            return;
        }
        if (o.getToolTip() != null) {
            jc.setToolTipText(o.getToolTip());
        }
        np.addToPage(jc,o.getHeight(),o.getWeight(),o.getSide(),o.getSection());
    }
    
    public synchronized ArtifactPage
    instantiate(int index) {
        if (index  < 0) {
            index = instantiatedPages.size();
        }
        ArtifactPage np = new ArtifactPage(index);
        np.setPrototype(this);
        instantiatedPages.add(np);
        np.setTopWeight(topWeight);
        np.setMiddleWeight(middleWeight);
        np.setBottomWeight(bottomWeight);
        np.setTopColor(topColor);
        np.setMainColor(mainColor);
        np.setBottomColor(bottomColor);
        np.setLongTitle(myLongTitle);
        np.setShortTitle(myShortTitle);
        np.setUnselectedColor(unselectedColor);
        Iterator it = myItems.iterator();
        while (it.hasNext()) {
            addItemToPage((ArtifactPagePrototype.Element)it.next(),np);
        }
        it = allAddedRaters.iterator();
        while (it.hasNext()) {
            addItemToPage((ArtifactPagePrototype.Element)it.next(),np);
        }
        return np;
    }
        
    public ArtifactPage
    instantiate() {
        return instantiate(-1);
    }
    
    public void
    remove(int index) {
        if (index < instantiatedPages.size()) {
            ArtifactPage ap = (ArtifactPage)instantiatedPages.remove(index);
            ap.removeAll();
            Iterator it = ap.getDocumentNames().iterator();
            while (it.hasNext()) {
                String dataName = it.next().toString();
                if (!dataName.endsWith("-group")) {
                    Data group = DataContext.getContext().getSharedData().getDataGroup(dataName);
                    if (group != null) {
                        List l = StringExploder.explode(group.getValue());
                        l.remove("("+dataName+")");
                        group.setValue(StringExploder.implodeString(l));
                    }
                    DataContext.getContext().getSharedData().removeData(dataName);
                }
            }
        }
    }
    
    public static void
    addRaterToPrototypes(ArtifactPagePrototype.Rater r) {
        allAddedRaters.add(r);
        Iterator it = allPrototypes.values().iterator();
        while (it.hasNext()) {
            ((ArtifactPagePrototype)it.next()).addRater(r);
        }
    }
    
    public static void
    clearAllInstantiated() {
        Iterator it = allPrototypes.values().iterator();
        while (it.hasNext()) {
            ((ArtifactPagePrototype)it.next()).instantiatedPages.clear();
        }
    }
} //ArtifactPagePrototype