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
 package manyminds.knowledgebase;

import java.util.EventObject;

public class
KnowledgeEvent
extends
EventObject {
    private Object oldValue;
    private KnowledgeEvent referentEvent;
    private int changeType;
    public static final int VALUE_CHANGE = 1;
    public static final int URL_CHANGE = 2;
    public static final int DETAIL_CHANGE = 4;
    public static final int REFERENCE_CHANGE = 8;
    public static final int REFERENT_CHANGE = 16;
    private String myTag = "";
    
    
    public
    KnowledgeEvent(Object source, Object old, int type) {
        super(source);
        oldValue = old;
        changeType = type;
    }
    
    public
    KnowledgeEvent(Object source, Object old, int type, String tag) {
        this(source,old,type);
        myTag = tag;
    }
    
    public
    KnowledgeEvent(Object source, KnowledgeEvent r) {
        super(source);
        referentEvent = r;
        changeType = REFERENT_CHANGE;
    }
    
    public int
    getEventType() {
        return changeType;
    }
    
    public Object
    getOldValue() {
        return oldValue;
    }
    
    public KnowledgeEvent
    getReferentEvent() {
        return referentEvent;
    }
    
    public String
    getTag() {
        return myTag;
    }
}