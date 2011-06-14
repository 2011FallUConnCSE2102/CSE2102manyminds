/*	File RaterModel.java
* =============================================================================
* 
*	The data model for the RaterPanel megawidget.  Handles 
*	registration of interest in itself using fairly standard event terminology
* 
* Author Eric Eslinger
* Copyright © 2000 University of California
* All Rights Reserved.
* 
* Agenda
* 
* History
* 21 FEB 00	New Today
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
 package manyminds.datamodel;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import manyminds.debug.Level;


public class RaterModel
extends AbstractData
implements Data, Serializable, Cloneable, RemoteData {
    private String criterionTitle;
    private String id;
    private String titleToolTip;
    private String myValue;
    private String myLinkTarget;
    private String myLinkedDocumentName;
    private String myType = "__UNPROTOTYPED__";
    private UpdatableListModel ratingSummaries;
    private UpdatableListModel pluralToolTips;
    private UpdatableListModel singularToolTips;
    private boolean plural;
    private BoundedRangeModel brm;
    private static Map myPrototypes = new HashMap();
    private SliderListener myListener = new SliderListener();

    class SliderListener implements ChangeListener
    {
        private int oldValue = -99;
        
        public void
        stateChanged(ChangeEvent event)
        {
            BoundedRangeModel slider = (BoundedRangeModel)event.getSource();
            if (slider.getValue() != oldValue) {
                if (!slider.getValueIsAdjusting()) {
                    oldValue = slider.getValue();
                    fireValueChanged();
                }
            }
        }
    }

    public static void
    addRaterPrototype(String id, RaterModel proto) {
        if (!myPrototypes.containsKey(id)) {
            proto.myType = id;
            myPrototypes.put(id,proto);
        }
    }
    
    public int
    getType() {
        return RSA;
    }
    
    public static boolean
    isPrototyped(String id) {
        return myPrototypes.containsKey(id);
    }
    
    public static RaterModel
    instantiateRaterModel(String id) {
        if (id == null) {
            try {
                return new RaterModel();
            } catch (RemoteException re) {
                logger.log(Level.WARNING, "Error creating blank rater model", re);
                return null;
            }
        } else if (myPrototypes.containsKey(id)) {
            return (RaterModel)((RaterModel)myPrototypes.get(id)).clone();
        } else {
            logger.log(Level.WARNING,"Unable to find rater prototype for "+id);
            return null;
        }
    }
    
    public
    RaterModel(String criterionTitle,
                            String titleToolTip,
                            String id,
                            Object ratingSummaries[],
                            Object singularToolTips[],
                            Object pluralToolTips[],
                            boolean plural)	throws IllegalArgumentException, RemoteException {
        this();
        for (int x = 0; x < ratingSummaries.length; ++x) {
                this.ratingSummaries.addElement(ratingSummaries[x].toString());
        }	
        for (int x = 0; x < singularToolTips.length; ++x) {
                this.singularToolTips.addElement(singularToolTips[x].toString());
        }	
        for (int x = 0; x < pluralToolTips.length; ++x) {
                this.pluralToolTips.addElement(pluralToolTips[x].toString());
        }	
        this.titleToolTip = titleToolTip;
        this.criterionTitle = criterionTitle ;
        this.id = id;	
        this.plural = plural;
        this.brm.setMaximum(this.ratingSummaries.getSize());
    } // RaterModel
    
    public
    RaterModel()
    throws RemoteException {
        this.ratingSummaries = new UpdatableListModel();
        this.singularToolTips = new UpdatableListModel();
        this.pluralToolTips = new UpdatableListModel();
        this.titleToolTip = "";
        this.criterionTitle = "";	
        this.id = "";
        this.singularToolTips = new UpdatableListModel();
        this.pluralToolTips = new UpdatableListModel();
        this.plural = false;
        this.brm = new DefaultBoundedRangeModel(0,0,0,1);
        this.brm.addChangeListener(myListener);
    }
    
    public Object
    clone() {
        try {
            RaterModel ret_val = new RaterModel(this.criterionTitle,
                                                this.titleToolTip,
                                                this.id,
                                                this.ratingSummaries.toArray(),
                                                this.singularToolTips.toArray(),
                                                this.pluralToolTips.toArray(),
                                                this.plural);
            ret_val.brm.removeChangeListener(ret_val.myListener);
            ret_val.brm = new DefaultBoundedRangeModel(this.brm.getValue(), this.brm.getExtent(), this.brm.getMinimum(), this.brm.getMaximum());
            ret_val.brm.addChangeListener(ret_val.myListener);
            ret_val.myType = this.myType;
            ret_val.myLinkTarget = this.myLinkTarget;
            return ret_val;
        } catch (RemoteException re) {
            return null;
        }
    }
    
    public void
    setID(String id) {
        this.id = id;
    }

    public void
    setTitle(String criterionTitle) {
        this.criterionTitle=criterionTitle;
        //fireStateChanged(TITLE_CHANGE);
    }// setTitle
    
    public void
    setTitleToolTip(String titleToolTip) {
        this.titleToolTip = titleToolTip;
        //fireStateChanged(TITLE_CHANGE);
    }// setTitle
    
    public void
    setToolTips(UpdatableListModel singularToolTips, UpdatableListModel pluralToolTips)
    throws IllegalArgumentException {
        this.singularToolTips=singularToolTips;
        this.pluralToolTips=pluralToolTips;
        //fireStateChanged(TT_CHANGE);
        brm.setMaximum(ratingSummaries.getSize());
    }// setToolTips
    
    
    public void
    setLinkTarget(String s) {
        if ((s != null) && (!s.startsWith("<"))) {
            s = "<give-advice><url>"+s+"</url></give-advice>";
        }
        myLinkTarget = s;
    }
    
    public void
    setLinkedDocumentName(String s) {
        myLinkedDocumentName = s;
    }
    
    public String
    getLinkTarget() {
        return myLinkTarget;
    }
    
    public String
    getLinkedDocumentName() {
        return myLinkedDocumentName;
    }
    
    public void
    setSummaries(UpdatableListModel ratingSummaries) throws IllegalArgumentException {
        this.ratingSummaries=ratingSummaries;
        brm.setMaximum(ratingSummaries.getSize());
    }// setTitle
    
    public String
    getActiveSummary() {
        return ratingSummaries.getElementAt(brm.getValue()).toString();
    }
    
    public String
    getTitle() {
        return criterionTitle;
    }// getTitle

    public String
    getID() {
        return id;
    }// getIndex

    public BoundedRangeModel
    getSliderModel() {
        return brm;
    }// getSliderModel

    public String
    getTitleToolTip() {
        return titleToolTip;
    }// getTitleToolTip

    public String
    getPrototypeName() {
        return myType;
    }
    
    public String
    getTypeString() {
        return myType;
    }
    
    public void
    setTypeString(String s) {
        setPrototype(s);
    }
    
    public boolean
    isPrototyped() {
        return getID().equals("__UNPROTOTYPED__");
    }
    
    public void
    setPrototype(String s) {
        if (myPrototypes.containsKey(s)) {
            RaterModel rm = (RaterModel)myPrototypes.get(s);
            this.criterionTitle = rm.criterionTitle;
            this.titleToolTip = rm.titleToolTip;
            this.id = rm.id;
            this.ratingSummaries = rm.ratingSummaries;
            this.singularToolTips = rm.singularToolTips;
            this.pluralToolTips = rm.pluralToolTips;
            this.plural = rm.plural;
            this.brm.removeChangeListener(this.myListener);
            this.brm = new DefaultBoundedRangeModel(rm.brm.getValue(), rm.brm.getExtent(), rm.brm.getMinimum(), rm.brm.getMaximum());
            this.brm.addChangeListener(this.myListener);
            this.myType = rm.myType;
        }
    }

    public UpdatableListModel
    getToolTips() {
        if (plural) {
            return pluralToolTips;
        } else {
            return singularToolTips;
        }
    }// getToolTips

    public UpdatableListModel
    getPluralToolTips() {
        return pluralToolTips;
    }
    
    public UpdatableListModel
    getSingularToolTips() {
        return singularToolTips;
    }
    
    public boolean
    getPlural() {
            return plural;
    }
    
    public void
    setPlural(boolean plural) {
        if (plural != this.plural) {
            this.plural = plural;
            //fireStateChanged(TT_CHANGE);
        }
    }

    public UpdatableListModel
    getSummaries() {
        return ratingSummaries;
    }// getSummaries
    
    public String
    getValue() {
        return Integer.toString(brm.getValue());
    }
    
    public int
    getIntValue() {
        return brm.getValue();
    }
    
    public void
    setValue(String s) {
        try {
            brm.setValue(Integer.parseInt(s));
        } catch (NumberFormatException nfe) {
            brm.setValue(0);
        }
    }
        
    public boolean
    reset() {
        setValue("0");
        return true;
    }
    
    public String
    toXML() {
        StringBuffer ret_buf = new StringBuffer();
        ret_buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        ret_buf.append("<!DOCTYPE rater SYSTEM \"manyminds.dtd\" >\n");
        ret_buf.append("<rater id=\"");
        ret_buf.append(getID());
        ret_buf.append("\">\n");
        ret_buf.append("<rater-title>\n");
        ret_buf.append(getTitle());
        ret_buf.append("</rater-title>\n");
        ret_buf.append("<rater-detail>\n<![CDATA[");
        ret_buf.append(getTitleToolTip());
        ret_buf.append("]]>\n</rater-detail>\n");
        ret_buf.append("<rating-summaries count=\"");
        ret_buf.append(Integer.toString(getSummaries().getSize()));
        ret_buf.append("\">\n");		
        for (int x = 0; x<getSummaries().getSize(); ++x) {
            ret_buf.append("<summary level=\"");
            ret_buf.append(Integer.toString(x));
            ret_buf.append("\">\n");
            ret_buf.append(getSummaries().getElementAt(x).toString());
            ret_buf.append("\n</summary>\n");
        }
        ret_buf.append("</rating-summaries>\n<rating-details count=\"");
        ret_buf.append(Integer.toString(getSummaries().getSize()));
        ret_buf.append("\">\n");		
        for (int x = 0; x<getSingularToolTips().getSize(); ++x) {
            ret_buf.append("<detail level=\"");
            ret_buf.append(Integer.toString(x));
            ret_buf.append("\" plurality=\"singular\">\n<![CDATA[");
            ret_buf.append(getSingularToolTips().getElementAt(x).toString());
            ret_buf.append("]]>\n</detail>\n");
        }
        for (int x = 0; x<getPluralToolTips().getSize(); ++x) {
            ret_buf.append("<detail level=\"");
            ret_buf.append(Integer.toString(x));
            ret_buf.append("\" plurality=\"plural\">\n<![CDATA[");
            ret_buf.append(getPluralToolTips().getElementAt(x).toString());
            ret_buf.append("]]>\n</detail>\n");
        }
        ret_buf.append("</rating-details>\n");
        ret_buf.append("</rater>\n");
        return ret_buf.toString();
    }
} // RaterModel