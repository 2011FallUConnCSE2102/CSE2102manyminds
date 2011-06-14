// Java Logging API for JDKs prior to 1.4
// Copyright (C) 2001 Brian R. Gilstrap
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details (a copy of the license
// .1 is included in the file GLPL in the doc directory of the source
// distribution).
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 package manyminds.src.manyminds.debug;

import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

public class LogRecord implements Serializable {

    private static long theNextSequenceNumber = 1;

    private synchronized static long nextSequenceNumber() {
        return ++theNextSequenceNumber;
    }

    private Level theLevel;
    private String theMessage;
    private long theSequenceNumber;
    private long theMilliseconds;
    private int theThreadID;
    private String theLoggerName;
    private Object[] theParameters;
    private ResourceBundle theResourceBundle;
    private String theResourceBundleName;
    private String theSourceClassName;
    private String theSourceMethodName;
    private Throwable theThrown;
    private String theThrownBacktrace;
    private boolean guessed;

    public LogRecord( Level aLevel, String msg ) {
           theLevel = aLevel;
           theMessage = msg;
           theSequenceNumber = nextSequenceNumber();
           theMilliseconds = System.currentTimeMillis();
           // HERE - is this safe? What if another Thread object got
           // created with the same address? A slower but safer alternative
           // is to use a ThreadLocal...
           theThreadID = Thread.currentThread().hashCode();
           // END HERE
    }

    public Level getLevel() {
        return theLevel;
    }

    public String getLoggerName() {
        return theLoggerName;
    }

    public String getMessage() {
        return theMessage;
    }

    public synchronized long getMillis() {
        return theMilliseconds;
    }

    public Object[] getParameters() {
        return theParameters;
    }

    public ResourceBundle getResourceBundle() {
        // HERE ??? check for name and try to find bundle?
        return theResourceBundle;
    }

    public String getResourceBundleName() {
        return theResourceBundleName;
    }

    public synchronized long getSequenceNumber() {
        return theSequenceNumber;
    }

    public String getSourceClassName() {
        if ( theSourceClassName == null && ! guessed ) {
            guessClassAndMethod();
        }
        return theSourceClassName;
    }

    public String getSourceMethodName() {
        if ( theSourceMethodName == null && ! guessed ) {
            guessClassAndMethod();
        }
        return theSourceMethodName;
    }

    public int getThreadID() {
        return theThreadID;
    }

    public Throwable getThrown() {
        return theThrown;
    }

/*
    HERE - Merlin handles stack backtraces correctly. How do we adjust for
    this? Especially, how do we reconcile what we do with what Merlin does so
    that we can have a program using RMI and Lumberjack talk successfully with
    a program using Merlin and RMI???
    public String getThrownBackTrace() {
        return theThrownBacktrace;
    }
*/
    public void setLevel( Level aLevel ) {
        theLevel = aLevel;
    }

    public void setLoggerName( String aLoggerName ) {
        theLoggerName = aLoggerName;
    }

    public void setMessage( String aMessage ) {
        theMessage = aMessage;
    }

    public synchronized void setMillis( long l ) {
        theMilliseconds = l;
    }

    public void setParameters( Object[] parameters ) {
        theParameters = parameters;
    }

    public void setResourceBundle( ResourceBundle bundle ) {
        theResourceBundle = bundle;
        // HERE - set resource bundle name ?
    }

    public void setResourceBundleName( String name ) {
        theResourceBundleName = name;
        // HERE - set resource bundle?
    }

    public synchronized void setSequenceNumber( long n ) {
        theSequenceNumber = n;
    }

    public void setSourceClassName( String aName ) {
        theSourceClassName = aName;
    }

    public void setSourceMethodName( String aName ) {
        theSourceMethodName = aName;
    }

    public void setThreadID( int anID ) {
        theThreadID = anID;
    }

    public void setThrown( Throwable t ) {
        theThrown = t;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        t.printStackTrace( pw );
        pw.close();
        theThrownBacktrace = sw.getBuffer().toString();
    }

    private void writeObject( ObjectOutputStream out ) {
        // HERE
/*
    private Level theLevel;
    private String theMessage;
    private long theSequenceNumber;
    private long theMilliseconds;
    private int theThreadID;
    private String theLoggerName;
    private Object[] theParameters; - toString
    private ResourceBundle theResourceBundle; - not written
    private String theResourceBundleName;
    private String theSourceClassName;
    private String theSourceMethodName;
    private Throwable theThrown;
    private String theThrownBacktrace; - must populate
*/
    }

    private void guessClassAndMethod() {
        try {
            Throwable t = new Throwable();
            t.fillInStackTrace();
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter( w );
            t.printStackTrace( pw );
            StringTokenizer tokenizer = new StringTokenizer( w.getBuffer().toString(), "\n" );
            tokenizer.nextToken();
            String line = tokenizer.nextToken();
            while ( line.indexOf( "manyminds.debug" ) >= 0 ) {
                line = tokenizer.nextToken();
            }
            int start = line.indexOf( "at " ) + "at ".length();
            int end = line.indexOf( '(' );
            String temp = line.substring( start, end );
            int lastPeriod = temp.lastIndexOf( '.' );
            theSourceClassName = temp.substring( 0, lastPeriod );
            theSourceMethodName = temp.substring( lastPeriod + 1 );
        }
        catch ( Exception e ) {
            // Ignore and let us return an empty string
            //e.printStackTrace( System.err );
        }
        guessed = true;
    }

}