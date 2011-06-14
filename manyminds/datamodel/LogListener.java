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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import manyminds.debug.Level;
import manyminds.debug.Logger;

public class
LogListener
extends TimerTask
implements DataListener {
    
    private String myTag;
    private String oldValue = "";
    private Data myData;
    
    private long firstEdit = 0;
    private long lastEdit = 0;
    
    private static boolean loadingData = false;
    private static HashMap myLoggers = new HashMap();
    private static Logger logger = Logger.getLogger("manyminds.datamodel");
    private static Timer heartbeat = new Timer();
    private static long finishDelay = 2500;
    private static long pulse = finishDelay / 2;
    public static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("M/dd/yy hh:mm:ss a");
    public static final String raterChangeText = "Rater Change";
    public static final String docChangeText = "Document Change";
    
    private class MyDate
    extends Date {
        
        public MyDate(long l) {
            super(l);
        }
        
        public String
        toString() {
            return SHORT_DATE_FORMAT.format(this);
        }
    }
        
    
    protected
    LogListener(String tag, Data d) {
        myTag = tag;
        myData = d;
        setOldValue();
        d.addDataListener(this);
        heartbeat.schedule(this,pulse,pulse);
    }

    protected void
    setOldValue() {
        if (myData instanceof RaterModel) {
            oldValue = myData.getValue() +": "+((RaterModel)myData).getActiveSummary();
        } else {
            oldValue = myData.getValue();
        }
    }
    
    protected String
    getNewValue() {
        if (myData instanceof RaterModel) {
            return myData.getValue() +": "+((RaterModel)myData).getActiveSummary();
        } else {
            return myData.getValue();
        }
    }
    
    public static void
    setLoading(boolean b) {
        loadingData = b;
    }
    
    public static void
    logData(String s, Data d) {
        synchronized (myLoggers) {
            if ((!myLoggers.containsKey(s)) && (!s.endsWith("-group"))) {
                myLoggers.put(s,new LogListener(s,d));
            }
        }
    }
    
    public void
    run() {
        synchronized (this) {
            if ((firstEdit > 0) && ((System.currentTimeMillis() - finishDelay) > lastEdit)) {
                Object[] parms = {myTag,oldValue,getNewValue(),docChangeText,new MyDate(firstEdit),new MyDate(lastEdit)};
                if (myData instanceof RaterModel) {
                    parms[3] = raterChangeText;
                }
                logger.log(Level.INFO,"Value Change",parms);
                firstEdit = 0;
                lastEdit = 0;
                setOldValue();
            }
        }
    }
    
    public void
    valueChanged(javax.swing.event.ChangeEvent ce) {
        if (loadingData) {
            Object[] parms = {myTag,oldValue,getNewValue()};
            logger.log(Level.FINE,"Changing value of data",parms);
            setOldValue();
        } else {
            synchronized (this) {
                if (lastEdit > 0) {
                    lastEdit = System.currentTimeMillis();
                } else {
                    firstEdit = System.currentTimeMillis();
                    lastEdit = firstEdit;
                }
            }
        }
    }
}

