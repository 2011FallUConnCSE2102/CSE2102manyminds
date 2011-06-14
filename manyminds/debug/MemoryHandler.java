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

import java.util.Iterator;
import java.util.LinkedList;

public class MemoryHandler extends Handler {

    private static final String LEVEL_PROPERTY = "manyminds.debug.MemoryHandler.level";
    private static final String FILTER_PROPERTY = "manyminds.debug.MemoryHandler.filter";
    private static final String BUFFER_SIZE_PROPERTY = "manyminds.debug.MemoryHandler.size";
    private static final int DEFAULT_BUFFER_SIZE = 1000;
    private static final String PUSH_LEVEL_PROPERTY = "manyminds.debug.MemoryHandler.pushLevel";
    private static final Level DEFAULT_PUSH_LEVEL = Level.SEVERE;
    private static final String TARGET_HANDLER_PROPERTY = "manyminds.debug.MemoryHandler.target";

    private int theSize = DEFAULT_BUFFER_SIZE;
    private Level thePushLevel = Level.SEVERE;
    private Handler whereToPush;
    private RingBuffer theBuffer;

    public MemoryHandler() {

        LogManager theManager = LogManager.getLogManager();

        // Set up the target handler if any
        String targetHandlerClass = theManager.getProperty( TARGET_HANDLER_PROPERTY );
        if ( targetHandlerClass != null ) {
            try {
                Class clazz = Class.forName( targetHandlerClass );
                if ( ! Handler.class.isAssignableFrom( clazz ) ) {
                    reportError( "Handler class specified ('" + targetHandlerClass + "') does not implement Handler!", null, ErrorManager.GENERIC_FAILURE );
                }
                Handler h = (Handler) clazz.newInstance();
                whereToPush = h;
            }
            catch ( ClassNotFoundException cnfe ) {
                    reportError( "Unable to find specified target handler class ('" + targetHandlerClass + "').", cnfe, ErrorManager.GENERIC_FAILURE );
            }
            catch ( IllegalAccessException iae ) {
                    reportError( "Unable to access ctor of target handler class ('" + targetHandlerClass + "').", iae, ErrorManager.GENERIC_FAILURE );
            }
            catch ( InstantiationException ia ) {
                    reportError( "problem instantiating target handler class ('" + targetHandlerClass + "').", ia, ErrorManager.GENERIC_FAILURE );
            }
        }

        // Set the ring buffer size
        int aSize = DEFAULT_BUFFER_SIZE;
        String bufferSizeString = theManager.getProperty( BUFFER_SIZE_PROPERTY );
        if ( bufferSizeString != null ) {
            try {
                int temp = Integer.parseInt( bufferSizeString );
                aSize = temp;
            }
            catch ( NumberFormatException nfe ) {
                // Ignore and use default
            }
        }

        // Set the push level
        Level aLevel = DEFAULT_PUSH_LEVEL;
        String pushLevelString = theManager.getProperty( PUSH_LEVEL_PROPERTY );
        if ( pushLevelString != null ) {
            try {
                Level temp = Level.parse( pushLevelString );
                aLevel = temp;
            }
            catch ( IllegalArgumentException iae ) {
                // Ignore and use default
            }
        }

        init( aSize, aLevel );

    }

    public MemoryHandler( Handler aHandler, int aBufferSize, Level aPushLevel ) {

        whereToPush = aHandler;

        init( aBufferSize, aPushLevel );

    }

    private void init( int aBufferSize, Level aPushLevel ) {

        LogManager theManager = LogManager.getLogManager();

        // Set theLevel
        String level = theManager.getProperty( LEVEL_PROPERTY );
        if ( level != null ) {
            try {
                Level aLevel = Level.parse( level );
                setLevel( aLevel );
            }
            catch ( IllegalArgumentException iae ) {
                // Superclass default is our default so we ignore this case
            }
        }

        // Set the Filter if the property is defined.
        String filterClassName = theManager.getProperty( FILTER_PROPERTY );
        if ( filterClassName != null ) {
            try {
                Class filterClass = Class.forName( filterClassName );
                if ( ! Filter.class.isAssignableFrom( filterClass ) ) {
                    reportError( "Class specified for filter ('" + filterClassName + "') does not implement Filter!", null, ErrorManager.GENERIC_FAILURE );
                }
                else {
                        setFilter( (Filter) filterClass.newInstance() );
                }
            }
            catch ( ClassNotFoundException cnfe ) {
                reportError( "Class specified for filter ('" + filterClassName + "') not found.", cnfe, ErrorManager.GENERIC_FAILURE );
            }
            catch( IllegalAccessException iae ) {
                reportError( "Unable to access class specified for filter ('" + filterClassName + "').", iae, ErrorManager.GENERIC_FAILURE );
            }
            catch( InstantiationException ie ) {
                reportError( "Problems instantiating class specified for filter ('" + filterClassName + "').", ie, ErrorManager.GENERIC_FAILURE );
            }
        }

        theSize = aBufferSize;

        // Set the push level
        thePushLevel = aPushLevel;

        theBuffer = new RingBuffer( theSize );
    }

    public void close() {
        LoggingPermission.checkAccess();
        Handler pushee = whereToPush;
        if ( pushee != null ) {
            pushee.close();
            pushee = null;
        }
        theBuffer = null;
    }

    public Level getPushlevel() {
        return thePushLevel;
    }

    public void flush() {
        Handler pushee = whereToPush;
        if ( pushee != null ) {
            pushee.flush();
        }
    }

    public boolean isLoggable( LogRecord record ) {
//        Handler pushee = whereToPush;
        return super.isLoggable( record ); // && ( pushee == null || pushee.isLoggable( record ) );
    }

    public void publish( LogRecord record ) {
        if ( ! isLoggable( record ) || theBuffer == null /*closed*/ ) {
            return;
        }
        theBuffer.add( record );
    }

    public void push() {
        theBuffer.push( whereToPush );
    }

    public void setPushlevel( Level aLevel ) {
        LoggingPermission.checkAccess();
        thePushLevel = aLevel;
    }

    private class RingBuffer {
        private LinkedList theList = new LinkedList();
        private int theRingSize;

        public RingBuffer( int aSize ) {
            theRingSize = aSize;
        }

        public synchronized void add( LogRecord record ) {
            theList.addLast( record );
            if ( theList.size() > theRingSize ) {
                theList.removeFirst();
            }
            if ( record.getLevel().intValue() >= thePushLevel.intValue() ) {
                MemoryHandler.this.push();
            }
        }

        public synchronized void push( Handler aHandler ) {
            if ( aHandler != null ) {
                Iterator i = theList.iterator();
                while ( i.hasNext() ) {
                    LogRecord record = (LogRecord) i.next();
                    aHandler.publish( record );
                }
            }
            theList.clear();
        }
    }
}