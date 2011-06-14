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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import manyminds.util.ManyMindsResolver;

public class LogManager {

    public static final String ROOT_LEVEL_NAME = "";

    static final Level DEFAULT_ROOT_LEVEL = Level.INFO;

    private static LogManager theSingleton;

    private transient PropertyChangeSupport pcs;
    private LogEntry root;
    private Logger rootLogger;
    private Properties theProperties;
    protected LogManager() {
        theSingleton = this;
        pcs = new PropertyChangeSupport( this );
        rootLogger = new Logger();
        root = new LogEntry( DEFAULT_ROOT_LEVEL, rootLogger );
        readConfiguration( false );

    }

    public boolean addLogger( Logger l ) {
        String name = l.getName();
        if ( l == null ) {
            throw new NullPointerException( "Logger name can't be null!" );
        }
        return root.addLogger( name, l );
    }

    public void addPropertyChangeListener( PropertyChangeListener l ) {
        LoggingPermission.checkAccess();
        pcs.addPropertyChangeListener( l );
    }

/*
    public void checkAccess() {
        LoggingPermission.checkAccess();
    }
*/
    synchronized Level getLevel( String name ) {
        if ( name.length() == 0 ) {
            return root.getLevel();
        }
        else {
            return root.getLevel( name );
        }
    }

    public Logger getLogger( String name ) {
        if ( name == null ) {
            return null;
        }
        if ( name.length() == 0 ) {
            return rootLogger;
        }
        else {
            return root.getLogger( name );
        }
    }

    Logger getParentLoggerOf( String name ) {
        if ( name.equals( ROOT_LEVEL_NAME ) ) {
            return null;
        }
        Logger retval = null;
        do {
            int periodIndex = name.lastIndexOf( '.' );
            if ( periodIndex < 0 ) {
                return root.getLogger();
            }
            name = name.substring( 0, periodIndex );
            retval = getLogger( name );
        } while ( retval == null && name.length() > 0 );
        if ( retval == null ) {
            retval = root.getLogger();
        }
        return retval;
    }

    public java.util.Enumeration getLoggerNames() {
        return root.getLoggerNames();
    }

    public static LogManager getLogManager() {
        String loggerClassName = System.getProperty( "manyminds.debug.class" );
        if ( loggerClassName != null ) {
            try {
                Class loggerClass = Class.forName( loggerClassName );
                loggerClass.newInstance();
            }
            catch ( ClassNotFoundException cnfe ) {
                // NONCONFORMANT?
                System.err.println( "Warning: logger class specified ('" + loggerClassName + "') not found; using default logger" );
            }
            catch ( ClassCastException cce ) {
                // NONCONFORMANT?
                System.err.println( "Warning: logger class specified ('" + loggerClassName + "') does not extend LogManager; using default logger" );
            }
            catch ( IllegalAccessException iae ) {
                // NONCONFORMANT?
                System.err.println( "Warning: unable to call noarg constructor of logger class specified ('" + loggerClassName + "'); using default logger" );
            }
            catch ( InstantiationException ie ) {
                // NONCONFORMANT?
                System.err.println( "Warning: InstantiationException creating LogManager instance ('" + loggerClassName + "'); using default logger" );
            }
        }
        if ( theSingleton == null ) {
            theSingleton = new LogManager();
            // theSingleton.readConfiguration( false );
        }
       return theSingleton;
    }

    public String getProperty( String name ) {
        return theProperties.getProperty( name );
    }

    public void readConfiguration() {
        readConfiguration( true );
    }

    private void readConfiguration( boolean check ) {
        String configFile = System.getProperty( "manyminds.debug.config.file" );
        if ( configFile != null ) {
            try {
                FileInputStream fis = new FileInputStream( configFile );
                readConfiguration( new BufferedInputStream( fis ), check );
                return;
            }
            catch ( IOException ioe ) {
                // NONCONFORMANT?
                System.err.println( "Warning: manyminds.debug.LogManager.readConfiguration Either could not find or could not read config file ('" + configFile + "'); using default file" );
                configFile = null;
            }
        }
        if (configFile == null) {
            try {
                readConfiguration( ManyMindsResolver.resolveResource("classpath://manyminds/debug/logging.properties"), check );
                return;
            } catch (IOException ioe) {
                System.err.println( "Warning: manyminds.debug.LogManager.readConfiguration Either could not find or could not read config file ('" + configFile + "'); using default file" );
                configFile = null;
            }
        }
        if ( configFile == null ) {
            try {
                configFile = System.getProperty("user.dir") + System.getProperty("file.separator") + "logging.properties";
                FileInputStream fis = new FileInputStream( configFile );
                readConfiguration( new BufferedInputStream( fis ), check );
                return;
            }
            catch ( IOException ioe ) {
                // NONCONFORMANT?
                System.err.println( "Warning: manyminds.debug.LogManager.readConfiguration Either could not find or could not read config file ('" + configFile + "'); using default file" );
                configFile = null;
            }
        }
        if ( configFile == null ) {
            String javaHome = System.getProperty( "java.home" );
            configFile = javaHome + File.separator + "lib" + File.separator + "logging.properties";
        }
        try {
            FileInputStream fis = new FileInputStream( configFile );
            readConfiguration( new BufferedInputStream( fis ), check );
        }
        catch ( IOException ioe ) {
            // NONCONFORMANT?
            System.err.println( "Error: manyminds.debug.LogManager.readConfiguration Could not find or could not read config file '" + configFile + "' ("
                + ioe + ")!" );
            ioe.printStackTrace( System.err );
        }

    }

    public void readConfiguration( InputStream ins ) {
        try {
            readConfiguration( ins, true );
        }
        catch ( IOException ioe ) {
            System.err.println( "Error: manyminds.debug.LogManager.readConfiguration Could not read input stream!" );
        }
    }

    private synchronized void readConfiguration( InputStream ins, boolean check ) throws IOException {
        if ( check ) LoggingPermission.checkAccess();

        OrderedProperties p = new OrderedProperties();
        p.load( ins );
        theProperties = p;
        processConfiguration( p );

        // HERE??? - Should there be any values passed to this property change event?
        pcs.firePropertyChange(null, null, null);
    }

    private void processConfiguration( OrderedProperties p ) {

        // Process the config property
        String configPropertyValue = p.getProperty( "config" );
        if ( configPropertyValue != null ) {
            processConfigProperty( configPropertyValue );
        }

        // Now, process every other property which ends in '.level'
        Iterator loadNamesInOrder = p.getPropertiesNamesInLoadOrder();
        while ( loadNamesInOrder.hasNext() ) {
            String propertyName = (String) loadNamesInOrder.next();
            if ( propertyName.endsWith( ".level" ) ) {
                processLevelProperty( propertyName, p.getProperty( propertyName ) );
            }
        }

        // Process the handlers property
        String handlersPropertyValue = p.getProperty( "handlers" );
        if ( handlersPropertyValue != null ) {
            processHandlersProperty( handlersPropertyValue );
        }

    }

    private void processHandlersProperty( String propertyValue ) {
        try {
            StringTokenizer st = new StringTokenizer( propertyValue, " \t\n\r," );
            while ( st.hasMoreTokens() ) {
                String className = st.nextToken();
                try {
                    Class clazz = Class.forName( className );
                    Handler h = (Handler) clazz.newInstance();
                    rootLogger.addHandler( h );
                }
                catch ( Exception e ) {
                    // Ignore
                    e.printStackTrace( System.err );
                }
            }
        }
        catch ( Exception e ) {
            // Ignore
            e.printStackTrace( System.err );
        }
    }

    private void processConfigProperty( String propertyValue ) {
        try {
            StringTokenizer st = new StringTokenizer( propertyValue );
            while ( st.hasMoreTokens() ) {
                String className = st.nextToken();
                try {
                    Class clazz = Class.forName( className );
                    clazz.newInstance();
                }
                catch ( Exception e ) {
                    // Ignore
                    e.printStackTrace( System.err );
                }
            }
        }
        catch ( Exception e ) {
            // Ignore
            e.printStackTrace( System.err );
        }
    }

    private void processLevelProperty( String propertyName, String propertyValue ) {
        try {
            Level l = Level.parse( propertyValue );
            String loggerName = propertyName.substring( 0, propertyName.length() - ".level".length() );
            setLevel( loggerName, l );
        }
        catch ( Exception e ) {
            // Ignore
            e.printStackTrace( System.err );
        }
    }

    public void removePropertyChangeListener( PropertyChangeListener l) {
        LoggingPermission.checkAccess();
        pcs.removePropertyChangeListener( l );
    }

    public synchronized void reset() {
        root.reset( Level.INFO );
    }

    void setLevel( String name, Level level) {
        LoggingPermission.checkAccess();
        if ( name.length() == 0 ) {
            root.setLevel( level );
        }
        else {
            root.setLevel( name, level );
        }
    }

    protected static class LogEntry {

        private Level theLevel;
        private Map children;
        private WeakReference refToLogger;

        public LogEntry( Level startingLevel ) {
            this( startingLevel, null );
        }

        public LogEntry( Level startingLevel, Logger logger ) {
            theLevel = startingLevel;
            children = new HashMap();
            if ( logger != null ) {
                refToLogger = new WeakReference( logger );
            }
        }

        public synchronized boolean addLogger( String nameToMatch, Logger aLogger ) {
            int periodIndex = nameToMatch.indexOf( '.' );
            if ( periodIndex >= 0 ) {
                String prefix = nameToMatch.substring( 0, periodIndex );
                String suffix = nameToMatch.substring( periodIndex + 1 );
                if ( prefix.length() == 0 ) {
                    throw new IllegalArgumentException( "Empty piece name not allowed!" );
                }
                LogEntry child = (LogEntry) children.get( prefix );
                if ( child == null ) {
                    child = new LogEntry( theLevel );
                    children.put( prefix, child );
                }
                return child.addLogger( suffix, aLogger );
            }
            else {
                LogEntry child = (LogEntry) children.get( nameToMatch );
                if ( child == null ) {
                    child = new LogEntry( theLevel, aLogger );
                    children.put( nameToMatch, child );
                    return true;
                }
                if ( child.refToLogger != null && child.refToLogger.get() != null ) {
                    return false;
                }
                else {
                    child.refToLogger = new WeakReference( aLogger );
                    child.propagateLoggerAsParent( aLogger );
                    return true;
                }
            }
        }

        private void setParent( Logger aLogger ) {
            if ( refToLogger != null ) {
                Logger l = (Logger) refToLogger.get();
                if ( l != null ) {
                    l.setParent( aLogger );
                }
            }
            propagateLoggerAsParent( aLogger );
        }

        private void propagateLoggerAsParent( Logger aLogger ) {
            if ( children == null ) {
                return;
            }
            Iterator i = children.values().iterator();
            while ( i.hasNext() ) {
                LogEntry current = (LogEntry) i.next();
                current.setParent( aLogger );
            }
        }

        public Level getLevel() {
            return theLevel;
        }

        public synchronized Level getLevel( String nameToMatch ) {
           int periodIndex = nameToMatch.indexOf( '.' );
            if ( periodIndex >= 0 ) {
                String prefix = nameToMatch.substring( 0, periodIndex );
                String suffix = nameToMatch.substring( periodIndex + 1 );
                if ( prefix.length() == 0 ) {
                    throw new IllegalArgumentException( "Empty piece name not allowed!" );
                }
                LogEntry child = (LogEntry) children.get( prefix );
                if ( child == null ) {
                    return theLevel;
                }
                return child.getLevel( suffix );
            }
            else {
                LogEntry child = (LogEntry) children.get( nameToMatch );
                if ( child == null ) {
                    return theLevel;
                }
                else {
                    Level retval = child.getLevel();
                    return retval;
                }
            }
        }

        public Logger getLogger() {
            Logger retval = null;
            if ( refToLogger != null ) {
                retval = (Logger) refToLogger.get();
            }
            return retval;
        }

        public synchronized Logger getLogger( String nameToMatch ) {
           int periodIndex = nameToMatch.indexOf( '.' );
            if ( periodIndex >= 0 ) {
                String prefix = nameToMatch.substring( 0, periodIndex );
                String suffix = nameToMatch.substring( periodIndex + 1 );
                if ( prefix.length() == 0 ) {
                    throw new IllegalArgumentException( "Empty piece name not allowed!" );
                }
                LogEntry child = (LogEntry) children.get( prefix );
                if ( child == null ) {
                    return null;
                }
                return child.getLogger( suffix );
            }
            else {
                LogEntry child = (LogEntry) children.get( nameToMatch );
                if ( child == null ) {
                    return null;
                }
                else {
                    return child.getLogger();
                }
            }
        }

        public Enumeration getLoggerNames() {
            Set names = new HashSet();
            addLoggerNames( names );
            return Collections.enumeration( names );
        }

        private synchronized void addLoggerNames( Collection c ) {
            Logger mine = (Logger) getLogger();
            if ( mine != null ) {
                c.add( mine.getName() );
            }
            Iterator i = children.values().iterator();
            while ( i.hasNext() ) {
                LogEntry entry = (LogEntry) i.next();
                entry.addLoggerNames( c );
            }
        }

        public synchronized void setLevel( String nameToMatch, Level aLevel ) {
            int periodIndex = nameToMatch.indexOf( '.' );
            if ( periodIndex >= 0 ) {
                String prefix = nameToMatch.substring( 0, periodIndex );
                String suffix = nameToMatch.substring( periodIndex + 1 );
                if ( prefix.length() == 0 ) {
                    throw new IllegalArgumentException( "Empty piece name not allowed!" );
                }
                LogEntry child = (LogEntry) children.get( prefix );
                if ( child == null ) {
                    child = new LogEntry( theLevel );
                    children.put( prefix, child );
                }
                child.setLevel( suffix, aLevel );
            }
            else {
                LogEntry child = (LogEntry) children.get( nameToMatch );
                if ( child == null ) {
                    child = new LogEntry( theLevel );
                    children.put( nameToMatch, child );
                }
                child.setLevel( aLevel );
            }
        }

        public synchronized void setLevel( Level aLevel ) {
            if ( aLevel != theLevel && ! aLevel.equals( theLevel ) ) {
                theLevel = aLevel;
                Logger l = getLogger();
                if ( l != null ) {
                    l.setEffectiveLevel( aLevel );
                }
                Iterator i = children.values().iterator();
                while ( i.hasNext() ) {
                    LogEntry entry = (LogEntry) i.next();
                    entry.setLevel( aLevel );
                }
            }
        }

        public synchronized void reset( Level firstLoggerLevel ) {
            // Reset ourselves
            theLevel = firstLoggerLevel;
            Logger l = getLogger();
            if ( l != null ) {
                l.reset( firstLoggerLevel );
            }

            // Reset any children
            Iterator i = children.values().iterator();
            while ( i.hasNext() ) {
                LogEntry child = (LogEntry) i.next();
                child.reset( null );
            }
        }

    }

    private static class OrderedProperties extends Properties {

        private List orderedKeyNames = new ArrayList();

        public Iterator getPropertiesNamesInLoadOrder() {
            return orderedKeyNames.iterator();
        }

        public Object put( Object key, Object value ) {
            Object retval = super.put( key, value );
            if ( ( key instanceof String ) && ( value instanceof String ) ) {
                orderedKeyNames.add( key );
            }
            return retval;
        }
    }

}
