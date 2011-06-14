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
 package manyminds.debug;

import java.io.UnsupportedEncodingException;

public abstract class Handler {

    private Formatter theFormatter;
    private String theEncodingName;
//    private Exception lastException;
    private Filter theFilter;
    private Level theLevel;
    private ErrorManager errorManager = new ErrorManager();

    protected Handler() {
        theLevel = Level.ALL;
    }

    public abstract void close();

    public abstract void flush();

    public ErrorManager getErrorManager() {
        LoggingPermission.checkAccess();
        return errorManager;
    }

    public void setErrorManager( ErrorManager anErrorManager ) {
        LoggingPermission.checkAccess();
        anErrorManager.hashCode();
        errorManager = anErrorManager;
    }

    public void reportError( String s, Exception ex, int reason ) {
        errorManager.error( s, ex, reason );
    }

    public String getEncoding() {
        return theEncodingName;
    }

    public Filter getFilter() {
        return theFilter;
    }

    public Formatter getFormatter() {
        return theFormatter;
    }

    public Level getLevel() {
        return theLevel;
    }

    public boolean isLoggable( LogRecord record ) {
        if ( theLevel.intValue() != Level.OFF.intValue() && record.getLevel().intValue() >= theLevel.intValue() ) {
             if ( theFilter == null || theFilter.isLoggable( record ) ) {
                return true;
             }
        }
        return false;
    }

    public abstract void publish( LogRecord record );

    private final static byte[] bytes = { (byte) 'b' };

    public void setEncoding( String anEncoding ) throws UnsupportedEncodingException {
        setEncoding( anEncoding, true );
    }

    protected void setEncoding( String anEncoding, boolean check ) throws UnsupportedEncodingException {
        if ( check ) LoggingPermission.checkAccess();
        if ( anEncoding != null ) {
            // Force an UnsupportedEncodingException if the encoding specified is
            // not supported.
            new String( bytes, anEncoding );
        }
        theEncodingName = anEncoding;
    }

    void setKnownEncoding( String anEncoding ) {
        theEncodingName = anEncoding;
    }

    void clearEncoding() {
        theEncodingName = null;
    }

    public void setFilter( Filter newFilter ) {
        LoggingPermission.checkAccess();
        theFilter = newFilter;
    }

    public void setFormatter( Formatter newFormatter ) {
        LoggingPermission.checkAccess();
        theFormatter = newFormatter;
    }

    public void setLevel( Level newLevel ) {
        LoggingPermission.checkAccess();
        theLevel = newLevel;
    }
}
