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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

public class Logger {

    public static final Logger global = getLogger( "global" );

    private static final String ENTERING = "ENTRY";
    private static final String EXITING = "RETURN";
    private static final String THROWING_MESSAGE = "THROW";

    private Logger parent;
    private String theName;
    private Filter theFilter;
    private Level theLevel;
    private Level effectiveLevel;
    private boolean sendToParentLogger;
    private List handlers = new ArrayList();
    private ResourceBundle theResourceBundle;
    private String theResourceBundleName;

    public static Logger getAnonymousLogger() {
        return getAnonymousLogger( null );
    }

    public static Logger getAnonymousLogger( String resourceBundleName ) {
        Logger retval = new Logger( null, resourceBundleName );
        return retval;
    }

    public static Logger getLogger( String name ) {
        return getLogger( name, null );
    }

    public static synchronized Logger getLogger( String name, String resourceBundleName ) {
        if ( name == null ) {
            return getAnonymousLogger( resourceBundleName );
        }
        LogManager manager = LogManager.getLogManager();
        Logger retval = manager.getLogger( name );
        if ( retval == null ) {
            retval = new Logger( name, resourceBundleName );
            manager.addLogger( retval );
        }
        else {
            String existingResourceBundleName = retval.getResourceBundleName();
            boolean match = false;
            if ( existingResourceBundleName != null ) {
                if ( ! existingResourceBundleName.equals( resourceBundleName ) ) {
                    throw new IllegalArgumentException( "Logger with same name but different resource bundle already exists" );
                }
            }
            else {
                retval.theResourceBundleName = resourceBundleName;
            }

        }
        return retval;
    }

    protected Logger( String name, String resourceBundleName ) {
        theName = name;
        if ( theName == null ) {
            effectiveLevel = LogManager.getLogManager().getLevel( LogManager.ROOT_LEVEL_NAME );
            parent = LogManager.getLogManager().getLogger( LogManager.ROOT_LEVEL_NAME );
        }
        else {
            effectiveLevel = LogManager.getLogManager().getLevel( name );
            parent = LogManager.getLogManager().getParentLoggerOf( name );
        }
        sendToParentLogger = true;
        theResourceBundleName = resourceBundleName;

        // HERE - delay until needed???
        if ( theResourceBundleName != null ) {
            // HERE - need to use ContextClassLoader and SystemClassLoader and
            // walk up stack looking at calling ClassLoaders.
            theResourceBundle = ResourceBundle.getBundle( theResourceBundleName );
        }
    }

    /**
     * Used only by LogManager to create the root logger.
     */
     Logger() {
        theName = LogManager.ROOT_LEVEL_NAME;
        theLevel = effectiveLevel = LogManager.DEFAULT_ROOT_LEVEL;
        sendToParentLogger = false;
    }

    public void setParent( Logger aParent ) {
        LoggingPermission.checkAccess();
        parent = aParent;
    }

    public void addHandler( Handler handler ) {
        if ( theName != null ) LoggingPermission.checkAccess();
        synchronized( handlers ) {
            if ( ! handlers.contains( handler ) ) {
                handlers.add( handler );
            }
        }
    }

    public void config( String msg ) {
        log( Level.CONFIG, msg );
    }

    public void entering( String sourceClass, String sourceMethod ) {
        logp( Level.FINER, sourceClass, sourceMethod, ENTERING );
    }

    public void entering( String sourceClass, String sourceMethod, Object param ) {
        logp( Level.FINER, sourceClass, sourceMethod, ENTERING + " {0}", new Object[] { param } );
    }

    public void entering( String sourceClass, String sourceMethod, Object[] params ) {
        StringBuffer buf = new StringBuffer( ENTERING );
        for ( int index = 0 ; index < params.length ; index++ ) {
            buf.append( " {" );
            buf.append( index );
            buf.append( "}" );
        }
        logp( Level.FINER, sourceClass, sourceMethod, buf.toString(), params );
    }

    public void exiting( String sourceClass, String sourceMethod ) {
        logp( Level.FINER, sourceClass, sourceMethod, EXITING );
    }

    public void exiting( String sourceClass, String sourceMethod, Object result ) {
        logp( Level.FINER, sourceClass, sourceMethod, EXITING + " {0}", new Object[] { result } );
    }

    public void fine( String msg ) {
        log( Level.FINE, msg );
    }

    public void finer( String msg ) {
        log( Level.FINER, msg );
    }

    public void finest( String msg ) {
        log( Level.FINEST, msg );
    }

    public Filter getFilter() {
        return theFilter;
    }

    public Handler[] getHandlers() {
        if ( theName != null ) LoggingPermission.checkAccess();
        synchronized( handlers ) {
            Handler[] retval = new Handler[ handlers.size() ];
            return (Handler[]) handlers.toArray( retval );
        }
    }

    public Level getLevel() {
        return theLevel;
    }

    public String getName() {
        return theName;
    }

    public ResourceBundle getResourceBundle() {
        return theResourceBundle;
    }

    public String getResourceBundleName() {
        return theResourceBundleName;
    }

    public boolean getUseParentHandlers() {
        return sendToParentLogger;
    }

    public void info( String msg ) {
        log( Level.INFO, msg );
    }

    public boolean isLoggable( Level level ) {
        return level.intValue() >= getEffectiveLevel().intValue();
    }

    public void log( Level level, String msg ) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        log( record );
    }

    public void log(Level level, String msg, Object param ) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setParameters( new Object[] { param } );
        log( record );
    }

    public void log(Level level, String msg, Object[] params) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setParameters( params );
        log( record );
    }

    public void log( Level level, String msg, Throwable thrown ) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setThrown( thrown );
        log( record );
    }

    public void log( LogRecord record ) {
        record.setLoggerName( theName );
        record.setResourceBundle( theResourceBundle );
        record.setResourceBundleName( theResourceBundleName );
        if ( theFilter != null ) {
            if ( ! theFilter.isLoggable( record ) ) {
                return;
            }
        }
        if ( sendToParentLogger && parent != null ) {
            parent.log( record );
        }
        List handlersCopy = null;
        synchronized( handlers ) {
            if ( handlers.size() > 0 ) {
                handlersCopy = new ArrayList( handlers );
            }
        }
        if ( handlersCopy != null ) {
            Iterator i = handlersCopy.iterator();
            while ( i.hasNext() ) {
                Handler h = (Handler) i.next();
                h.publish( record );
            }
        }
    }

    public void logp( Level level, String sourceClass, String sourceMethod, String msg) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setSourceClassName( sourceClass );
        record.setSourceMethodName( sourceMethod );
        log( record );
    }

    public void logp( Level level, String sourceClass, String sourceMethod, String msg, Object param) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setSourceClassName( sourceClass );
        record.setSourceMethodName( sourceMethod );
        record.setParameters( new Object[] { param } );
        log( record );
    }

    public void logp( Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setSourceClassName( sourceClass );
        record.setSourceMethodName( sourceMethod );
        record.setParameters( params );
        log( record );
    }

    public void logp( Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown ) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setSourceClassName( sourceClass );
        record.setSourceMethodName( sourceMethod );
        record.setThrown( thrown );
        log( record );
    }

    public void logrb( Level level, String sourceClass, String sourceMethod, String bundleName, String msg) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setSourceClassName( sourceClass );
        record.setSourceMethodName( sourceMethod );
        record.setResourceBundleName( bundleName );
        log( record );
    }

    public void logrb( Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object param) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setSourceClassName( sourceClass );
        record.setSourceMethodName( sourceMethod );
        record.setResourceBundleName( bundleName );
        record.setParameters( new Object[] { param } );
        log( record );
    }

    public void logrb( Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object[] params) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setSourceClassName( sourceClass );
        record.setSourceMethodName( sourceMethod );
        record.setResourceBundleName( bundleName );
        record.setParameters( params );
        log( record );
    }

    public void logrb( Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Throwable thrown ) {
        if ( ! isLoggable( level ) ) {
            return;
        }
        LogRecord record = new LogRecord( level, msg );
        record.setSourceClassName( sourceClass );
        record.setSourceMethodName( sourceMethod );
        record.setResourceBundleName( bundleName );
        record.setThrown( thrown );
        log( record );
    }

    public void removeHandler( Handler handler ) {
        if ( theName != null )  LoggingPermission.checkAccess();
        synchronized( handlers ) {
            ListIterator i = handlers.listIterator();
            while ( i.hasNext() ) {
                Handler h = (Handler) i.next();
                if ( h == handler ) {
                    i.remove();
                    return;
                }
            }
        }
    }

    public void setFilter( Filter newFilter ) {
        if ( theName != null )  LoggingPermission.checkAccess();
        theFilter = newFilter;
    }

    public void setLevel( Level newLevel ) {
        theLevel = newLevel;
        if ( theName != null ) {
            LoggingPermission.checkAccess();
            LogManager.getLogManager().setLevel( theName, newLevel );
        }
    }

    void setEffectiveLevel( Level newLevel ) {
        effectiveLevel = newLevel;
    }

    Level getEffectiveLevel() {
        if ( theLevel != null ) {
            return theLevel;
        }
        return effectiveLevel;
    }

    public void setUseParentHandlers( boolean useParentHandlers ) {
        if ( theName != null )  LoggingPermission.checkAccess();
        sendToParentLogger = useParentHandlers;
    }

    public void severe( String msg ) {
        log( Level.SEVERE, msg );
    }

    public void throwing( String sourceClass, String sourceMethod, Throwable thrown ) {
        logp( Level.FINER, sourceClass, sourceMethod, THROWING_MESSAGE, thrown );
    }

    public void warning( String msg ) {
        log( Level.WARNING, msg );
    }

    void reset( Level aLevel ) {
         Iterator i = null;
         synchronized ( this ) {
            List copy = (List) ((ArrayList)handlers).clone();
            i = copy.iterator();
            handlers = new ArrayList();
         }
         while ( i.hasNext() ) {
             Handler h = (Handler) i.next();
             h.close();
         }
        theLevel = aLevel;
        effectiveLevel = aLevel;
    }
}
